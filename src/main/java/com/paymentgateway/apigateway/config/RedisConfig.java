package com.paymentgateway.apigateway.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.paymentgateway.apigateway.model.MerchantContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.ReactiveRedisConnectionFactory;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;

@Configuration
public class RedisConfig {

    @Bean
    public ReactiveRedisTemplate<String, MerchantContext> merchantRedisTemplate(
            ReactiveRedisConnectionFactory factory) {

        StringRedisSerializer keySerializer = new StringRedisSerializer();

        Jackson2JsonRedisSerializer<MerchantContext> valueSerializer =
                new Jackson2JsonRedisSerializer<>(new ObjectMapper(), MerchantContext.class);

        RedisSerializationContext<String, MerchantContext> context =
                RedisSerializationContext.<String, MerchantContext>newSerializationContext(keySerializer)
                        .value(valueSerializer)
                        .build();

        return new ReactiveRedisTemplate<>(factory, context);
    }
}