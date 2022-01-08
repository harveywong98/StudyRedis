package com.hello.jedis._03_ClientSharded;

import org.apache.commons.pool2.impl.GenericObjectPoolConfig;
import redis.clients.jedis.*;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class Main {
    public static void main(String[] args) {
        List<JedisShardInfo> shards = new ArrayList<>();

        JedisShardInfo shardInfo1 = new JedisShardInfo("127.0.0.1", 6379);
        JedisShardInfo shardInfo2 = new JedisShardInfo("127.0.0.1", 16379);
        JedisShardInfo shardInfo3 = new JedisShardInfo("127.0.0.1", 26379);

        GenericObjectPoolConfig config = new GenericObjectPoolConfig();


        shards.add(shardInfo1);
        shards.add(shardInfo2);
        shards.add(shardInfo3);

        ShardedJedisPool shardedJedisPool = new ShardedJedisPool(config, shards);

        ShardedJedis jedis = shardedJedisPool.getResource();
        for (int i = 0; i < 10000; i++) {
            jedis.set(String.valueOf(i), UUID.randomUUID().toString());
        }
    }
}
