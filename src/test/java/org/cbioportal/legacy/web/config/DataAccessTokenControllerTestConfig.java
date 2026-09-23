package org.cbioportal.legacy.web.config;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.NoOpPasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.security.web.context.SecurityContextRepository;

@TestConfiguration
@EnableWebSecurity
public class DataAccessTokenControllerTestConfig {

  private final SecurityContextRepository securityContextRepository =
      new HttpSessionSecurityContextRepository();

  @Bean
  protected SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
    http.securityContext(
            securityContext -> securityContext.securityContextRepository(securityContextRepository))
        .authorizeHttpRequests((authz) -> authz.anyRequest().authenticated())
        .httpBasic(httpBasic -> httpBasic.authenticationEntryPoint(restAuthenticationEntryPoint()))
        .apply(new TestFilterDsl());
    return http.build();
  }

  @Bean
  public InMemoryUserDetailsManager userDetailsService() {
    UserDetails user =
        User.withUsername("MOCK_USER")
            .password(noopPasswordEncoder().encode("MOCK_PASSWORD"))
            .authorities("PLACEHOLDER_ROLE")
            .build();
    UserDetails unauthorizedUser =
        User.withUsername("UNAUTHORIZED_MOCK_USER")
            .password(noopPasswordEncoder().encode("UNAUTHORIZED_MOCK_PASSWORD"))
            .authorities("SOME_OTHER_ROLE")
            .build();
    return new InMemoryUserDetailsManager(user, unauthorizedUser);
  }

  @Bean
  public static NoOpPasswordEncoder noopPasswordEncoder() {
    return (NoOpPasswordEncoder) NoOpPasswordEncoder.getInstance();
  }

  private class TestFilterDsl extends AbstractHttpConfigurer<TestFilterDsl, HttpSecurity> {
    @Override
    public void configure(HttpSecurity http) {
      AuthenticationManager authenticationManager =
          http.getSharedObject(AuthenticationManager.class);
      UsernamePasswordAuthenticationFilter filter = new UsernamePasswordAuthenticationFilter();
      filter.setSecurityContextRepository(securityContextRepository);
      filter.setPostOnly(false);
      filter.setFilterProcessesUrl("/j_spring_security_check");
      filter.setUsernameParameter("j_username");
      filter.setPasswordParameter("j_password");
      filter.setAuthenticationManager(authenticationManager);
      filter.setAuthenticationSuccessHandler(tokenAuthenticationSuccessHandler());
      http.addFilter(filter);
    }
  }

  @Bean
  public RestAuthenticationEntryPoint restAuthenticationEntryPoint() {
    return new RestAuthenticationEntryPoint();
  }

  @Bean
  public TokenAuthenticationSuccessHandler tokenAuthenticationSuccessHandler() {
    return new TokenAuthenticationSuccessHandler();
  }
}
