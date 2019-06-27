package org.cbioportal.persistence.spark.util;

import org.apache.spark.sql.SparkSession;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SparkConfiguration {
    
    @Value("${spark.app.name}")
    private String appName;

    @Value("${spark.master.uri}")
    private String masterUri;

    @Value("${spark.driver.bindAddress}")
    private String bindAddress;

    @Value("${spark.driver.host}")
    private String driverHost;

    @Value("${spark.driver.memory}")
    private String driverMemory;

    @Value("${spark.executor.memory}")
    private String executorMemory;

    @Value("${spark.shuffle.partitions}")
    private String shufflePartitions;

    @Value("${spark.default.parallelism}")
    private String sparkParallelism;


    @Bean
    public SparkSession sparkSession() {
        return SparkSession
            .builder()
            .config("spark.master", masterUri)
            .config("spark.driver.bindAddress", bindAddress)
            .config("spark.driver.host", driverHost)
            .config("spark.driver.memory", driverMemory)
            .config("spark.executor.memory", executorMemory)
            .config("spark.sql.shuffle.partitions", shufflePartitions)
            .config("spark.default.parallelism", sparkParallelism)
            .appName(appName)
            .getOrCreate();
    }
}
