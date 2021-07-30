package uk.gov.di.ipv.core.back.config;

import io.pivotal.cfenv.core.CfEnv;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import redis.clients.jedis.Jedis;

@Slf4j
@Configuration
public class RedisConfig {

    private final String redisEndpoint;

    public RedisConfig(@Value("${redis.endpoint}") String redisEndpoint) {
        this.redisEndpoint = redisEndpoint;
    }

    @Bean("redis-client")
    Jedis jedisClient() {
        if (!redisEndpoint.contains("cfenv")) {
            log.info("Using local redis session");
            var split = redisEndpoint.split(":");

            if (split.length == 2) {
                return new Jedis(split[0], Integer.parseInt(split[1]));
            }

            return new Jedis(redisEndpoint, 6379);
        }

        // If not using localhost, use cf env to grab the VCAP service
        var cfEnv = new CfEnv();
        var redisHost = cfEnv.findCredentialsByName("session-cache").getHost();
        var redisPort = cfEnv.findCredentialsByName("session-cache").getPort();
        var redisPassword = cfEnv.findCredentialsByName("session-cache").getPassword();

        var client = new Jedis(redisHost, Integer.parseInt(redisPort), true);

        if (redisPassword != null) {
            client.auth(redisPassword);
        }

        return client;
    }
}
