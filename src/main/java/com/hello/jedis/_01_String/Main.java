package com.hello.jedis._01_String;

import redis.clients.jedis.Jedis;

public class Main {
    public static void main(String[] args) {
        Jedis jedis = new Jedis("localhost", 6379);

        String flushResult = jedis.flushDB();
        System.out.println(flushResult);

        String setResult = jedis.set("test-key", "test-value");
        System.out.println(setResult);

        long removeResult = jedis.del("test-key");
        System.out.println(removeResult);

        jedis.set("number", "1");
        jedis.incr("number");
        System.out.println(jedis.get("number"));
    }
}
