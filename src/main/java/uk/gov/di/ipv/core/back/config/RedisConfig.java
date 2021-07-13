package uk.gov.di.ipv.core.back.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import redis.clients.jedis.Jedis;

@Configuration
public class RedisConfig {

    private final String redisEndpoint;

    public RedisConfig(@Value("${redis.endpoint}") String redisEndpoint) {
        this.redisEndpoint = redisEndpoint;
    }

    @Bean("redis-client")
    Jedis jedisClient() {
        return new Jedis(redisEndpoint);
    }
}
