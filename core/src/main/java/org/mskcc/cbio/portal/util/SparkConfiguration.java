package org.mskcc.cbio.portal.util;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.sql.SparkSession;

@Configuration
@PropertySource("classpath:portal.properties")
public class SparkConfiguration {
    
    private String appName = GlobalProperties.getProperty("spark.app.name");
    private String sparkHome = GlobalProperties.getProperty("spark.home");
    private String masterUri = GlobalProperties.getProperty("spark.master.uri");
    private String bindAddress = GlobalProperties.getProperty("spark.driver.bindAddress");
    private String driverHost = GlobalProperties.getProperty("spark.driver.host");
    private String driverMemory = GlobalProperties.getProperty("spark.driver.memory");
    private String executorMemory = GlobalProperties.getProperty("spark.executor.memory");
    
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
