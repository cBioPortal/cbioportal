package org.cbioportal;

import java.util.concurrent.Executor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.AsyncConfigurer;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

@Configuration
@EnableAsync
public class AsyncConfig implements AsyncConfigurer {

  @Value("${multithread.core_pool_size:#{T(java.lang.Runtime).getRuntime().availableProcessors()}}")
  private int corePoolSize;

  @Override
  public Executor getAsyncExecutor() {
    ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
    executor.setCorePoolSize(corePoolSize);
    executor.setThreadNamePrefix("ThreadPoolTaskExecutor-");
    executor.initialize();
    return executor;
  }
}
