package org.cbioportal.application.security.config;

import java.util.ArrayList;
import java.util.List;
import org.cbioportal.application.security.token.RestAuthenticationEntryPoint;
import org.cbioportal.application.security.token.TokenAuthenticationFilter;
import org.cbioportal.application.security.token.TokenAuthenticationSuccessHandler;
import org.cbioportal.legacy.service.DataAccessTokenService;
import org.cbioportal.legacy.utils.config.annotation.ConditionalOnProperty;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.lang.Nullable;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;
import org.springframework.security.web.context.SecurityContextHolderFilter;
import org.springframework.security.web.util.matcher.AndRequestMatcher;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.security.web.util.matcher.NegatedRequestMatcher;
import org.springframework.security.web.util.matcher.OrRequestMatcher;
import org.springframework.security.web.util.matcher.RequestMatcher;

@Configuration
@ConditionalOnProperty(
    name = "authenticate",
    havingValue = {"false", "optional_oauth2"},
    isNot = true)
public class ApiSecurityConfig {

  @Value("${api.access.token.required:false}")
  private boolean accessTokenRequired;

  static final String[] PUBLIC_API_Matchers = {
    "/api/swagger-resources/**",
    "/api/swagger-ui.html",
    "/api/health",
    "/api/public_virtual_studies/**",
    "/api/cache/**"
  };

  @Bean
  @Order(Ordered.HIGHEST_PRECEDENCE)
  public SecurityFilterChain securityFilterChain(
      HttpSecurity http, @Nullable DataAccessTokenService tokenService) throws Exception {
    http.csrf(AbstractHttpConfigurer::disable)
        // This filter chain only grabs requests to the '/api' path.
        .securityMatcher("/api/**", "/webservice.do")
        .authorizeHttpRequests(
            authorize ->
                authorize
                    .requestMatchers(PUBLIC_API_Matchers)
                    .permitAll()
                    .anyRequest()
                    .authenticated())
        .sessionManagement(
            sessionManagement -> sessionManagement.sessionFixation().migrateSession())
        .exceptionHandling(
            eh ->
                eh.defaultAuthenticationEntryPointFor(
                    new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED),
                    AntPathRequestMatcher.antMatcher("/api/**")));
    // When dat.method is not 'none' and a tokenService bean is present,
    // the apiTokenAuthenticationFilter is added to the filter chain.
    if (tokenService != null) {
      http.apply(ApiTokenFilterDsl.tokenFilterDsl(tokenService, accessTokenRequired));
    }
    return http.build();
  }

  @Autowired
  public void buildAuthenticationManager(
      AuthenticationManagerBuilder authenticationManagerBuilder,
      @Nullable AuthenticationProvider... authenticationProviders) {
    if (authenticationProviders != null) {
      for (AuthenticationProvider authenticationProvider : authenticationProviders) {
        authenticationManagerBuilder.authenticationProvider(authenticationProvider);
      }
    }
  }

  @Bean
  public RestAuthenticationEntryPoint restAuthenticationEntryPoint() {
    return new RestAuthenticationEntryPoint();
  }
}

class ApiTokenFilterDsl extends AbstractHttpConfigurer<ApiTokenFilterDsl, HttpSecurity> {

  private final boolean accessTokenRequired;
  private final DataAccessTokenService tokenService;

  private ApiTokenFilterDsl(DataAccessTokenService tokenService, boolean accessTokenRequired) {
    this.tokenService = tokenService;
    this.accessTokenRequired = accessTokenRequired;
  }

  @Override
  public void configure(HttpSecurity http) {
    AuthenticationManager authenticationManager = http.getSharedObject(AuthenticationManager.class);
    TokenAuthenticationSuccessHandler tokenAuthenticationSuccessHandler =
        new TokenAuthenticationSuccessHandler();
    TokenAuthenticationFilter filter =
        new TokenAuthenticationFilter(
            "/**", authenticationManager, tokenService, accessTokenRequired);

    // Explicitly set the request matcher to exclude public paths if enforcement is enabled
    if (accessTokenRequired) {
      // Filter applies to /api/** BUT NOT the public paths
      List<RequestMatcher> matchers = new ArrayList<>();
      matchers.add(new AntPathRequestMatcher("/api/**"));

      List<RequestMatcher> publicMatchers = new ArrayList<>();
      for (String pattern : ApiSecurityConfig.PUBLIC_API_Matchers) {
        publicMatchers.add(new AntPathRequestMatcher(pattern));
      }
      matchers.add(new NegatedRequestMatcher(new OrRequestMatcher(publicMatchers)));

      filter.setRequiresAuthenticationRequestMatcher(new AndRequestMatcher(matchers));
    }

    filter.setAuthenticationSuccessHandler(tokenAuthenticationSuccessHandler);
    http.addFilterAfter(filter, SecurityContextHolderFilter.class);
  }

  public static ApiTokenFilterDsl tokenFilterDsl(
      DataAccessTokenService tokenService, boolean accessTokenRequired) {
    return new ApiTokenFilterDsl(tokenService, accessTokenRequired);
  }
}
