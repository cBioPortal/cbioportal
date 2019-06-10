package org.mskcc.cbio.portal.util;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.beans.factory.annotation.Value;

import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.sql.SparkSession;

@Configuration
@PropertySource("classpath:portal.properties")
public class SparkConfiguration {

    @Value("${spark.app.name}")
    private String appName;
    @Value("${spark.home}")
    private String sparkHome;
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
    
    @Bean
    public SparkConf sparkConf() {
        SparkConf sparkConf = new SparkConf()
            .setAppName(appName)
            .setSparkHome(sparkHome)
            .setMaster(masterUri)
            .set("spark.driver.bindAddress", bindAddress)
            .set("spark.driver.host", driverHost)
            .set("spark.driver.memory", driverMemory)
            .set("spark.executor.memory", executorMemory);

        return sparkConf;
    }

    @Bean
    public JavaSparkContext javaSparkContext() {
        return new JavaSparkContext(sparkConf());
    }

    @Bean
    public SparkSession sparkSession() {
        return SparkSession
            .builder()
            .sparkContext(javaSparkContext().sc())
            .appName(appName)
            .getOrCreate();
    }
}
