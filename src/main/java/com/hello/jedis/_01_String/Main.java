package com.hello.jedis;

import redis.clients.jedis.Jedis;

public class Main {
    public static void main(String[] args) {
        Jedis jedis = new Jedis("localhost", 63798);

        String setResult = jedis.set("test-key", "test-value");
        System.out.println(setResult);

        long removeResult = jedis.del("test-key");
        System.out.println(removeResult);
    }
}
