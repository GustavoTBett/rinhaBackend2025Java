package bett.gustavo.rinhaBackend2025Model.config;

import bett.gustavo.rinhaBackend2025Model.model.Payment;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.ReactiveRedisConnectionFactory;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;

@Configuration
public class RedisConfig {
    @Value("${spring.redis.host}")
    private String redisHost;

    @Value("${spring.redis.port}")
    private String redisPort;

    @Value("${spring.redis.password}")
    private String redisPassword;

    @Value("${spring.redis.database}")
    private String redisDatabase;

    @Bean
    public LettuceConnectionFactory redisConnectionFactory() {
        RedisStandaloneConfiguration configuration = new RedisStandaloneConfiguration(redisHost, Integer.parseInt(redisPort));
        configuration.setDatabase(Integer.parseInt(redisDatabase));
        configuration.setPassword(redisPassword);

        return new LettuceConnectionFactory(configuration);
    }

    @Bean
    public RedisTemplate<String, Payment> redisTemplateZset(RedisConnectionFactory connectionFactory) {
        RedisTemplate<String, Payment> template = new RedisTemplate<>();
        template.setConnectionFactory(connectionFactory);

        StringRedisSerializer keySerializer = new StringRedisSerializer();
        Jackson2JsonRedisSerializer<Payment> valueSerializer = new Jackson2JsonRedisSerializer<>(Payment.class);

        template.setKeySerializer(keySerializer);
        template.setValueSerializer(valueSerializer);
        template.setHashKeySerializer(keySerializer);
        template.setHashValueSerializer(valueSerializer);

        template.afterPropertiesSet();
        return template;
    }

    @Bean(name = "reactiveRedisTemplatePayment")
    public ReactiveRedisTemplate<String, Payment> reactiveRedisTemplatePayment(ReactiveRedisConnectionFactory factory) {
        RedisSerializationContext<String, Payment> context = RedisSerializationContext
                .<String, Payment>newSerializationContext(new StringRedisSerializer())
                .value(new Jackson2JsonRedisSerializer<>(Payment.class))
                .build();

        return new ReactiveRedisTemplate<>(factory, context);
    }

}
