package org.cbioportal.persistence.spark;

import org.apache.spark.sql.SparkSession;
import org.cbioportal.persistence.spark.util.ParquetLoader;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import org.springframework.test.util.ReflectionTestUtils;

@Configuration
public class SparkTestConfiguration {

    @Bean
    public SparkSession spark() {
        return SparkSession
            .builder()
            .config("spark.master", "local[*]")
            .config("spark.sql.shuffle.partitions", "2")
            .appName("cBioPortal")
            .getOrCreate();
    }

    @Bean
    public static PropertySourcesPlaceholderConfigurer propertySourcesPlaceholderConfigurer() {
        return new PropertySourcesPlaceholderConfigurer();
    }
    
    @Bean
    public ParquetLoader parquetLoader() {
        ParquetLoader parquetLoader = new ParquetLoader();
        ReflectionTestUtils.setField(parquetLoader, "PARQUET_DIR", "src/test/resources/parquet/");
        return parquetLoader;
    }
    
    @Bean
    public DiscreteCopyNumberSparkRepository discreteCopyNumberSparkRepository() {
        return new DiscreteCopyNumberSparkRepository();
    }
}
