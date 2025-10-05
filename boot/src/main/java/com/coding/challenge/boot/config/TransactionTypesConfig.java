package com.coding.challenge.boot.config;

import java.util.Set;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.coding.challenge.infrastructure.common.ValidTransactionTypeProps;


import lombok.Setter;

@Configuration
@Setter
@ConfigurationProperties(prefix = "transactions")
public class TransactionTypesConfig {

    private Set<String> allowedTypes;

    @Bean
    public ValidTransactionTypeProps validTransactionTypeProps(){
        return new ValidTransactionTypeProps(allowedTypes);
    }
}
