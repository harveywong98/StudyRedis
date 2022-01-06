package com.hello.jedis._02_lock;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisSentinelPool;
import redis.clients.jedis.params.SetParams;
import redis.clients.jedis.providers.ShardedConnectionProvider;

import java.util.Collections;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Main {
    /* https://www.jianshu.com/p/47fd7f86c848 */

    static JedisPool jedisPool = new JedisPool();
    String lock_key = "redis_lock"; //锁键
    long internalLockLeaseTime = 30000;//锁过期时间
    long timeout = 999999; //获取锁的超时时间
    SetParams params = SetParams.setParams().nx().px(internalLockLeaseTime);

    static int count = 0;

    public static void main(String[] args) {
        Main main = new Main();

        int clientcount = 1000;
        CountDownLatch countDownLatch = new CountDownLatch(clientcount);

        ExecutorService executorService = Executors.newFixedThreadPool(clientcount);
        long start = System.currentTimeMillis();
        for (int i = 0; i < clientcount; i++) {
            executorService.execute(() -> {

                //通过Snowflake算法获取唯一的ID字符串
                String id = UUID.randomUUID().toString();
                try {
                    main.lock(id);
                    count++;
                } finally {
                    main.unlock(id);
                }
                countDownLatch.countDown();
            });
        }
        try {
            countDownLatch.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        long end = System.currentTimeMillis();
        System.out.printf("执行线程数:{},总耗时:{},count数为:{}", clientcount, end - start, count);
    }

    public boolean lock(String id) {
        Jedis jedis = jedisPool.getResource();
        long start = System.currentTimeMillis();

        try {
            for (; ; ) {
                String lock = jedis.set(lock_key, id, params);
                if ("OK".equals(lock)) {
                    return true;
                }

                long l = System.currentTimeMillis() - start;
                if (l > timeout) {
                    return false;
                }
                try {
                    Thread.sleep(30000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        } finally {
            jedis.close();
        }

    }

    public boolean unlock(String id) {
        Jedis jedis = jedisPool.getResource();
        String script = "if redis.call('get',KEYS[1]) == ARGV[1] then" +
                "   return redis.call('del',KEYS[1]) " +
                "else" +
                "   return 0 " +
                "end";
        try {
            Object result = jedis.eval(script, Collections.singletonList(lock_key), Collections.singletonList(id));
            if ("1".equals(result.toString())) {
                return true;
            }
            return false;
        } finally {
            jedis.close();
        }
    }
}
