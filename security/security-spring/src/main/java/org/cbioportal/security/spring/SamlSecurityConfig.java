package org.cbioportal.security.spring;

import java.net.URL;
import org.cbioportal.security.spring.authentication.PortalSavedRequestAwareAuthenticationSuccessHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.saml.SAMLAuthenticationProvider;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationFailureHandler;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.authentication.logout.SimpleUrlLogoutSuccessHandler;

@Configuration
@ConditionalOnProperty(value = "authenticate", havingValue = "saml")
public class SamlSecurityConfig extends WebSecurityConfigurerAdapter {

    @Value("${saml.logout.url}")
    private URL logoutUrl;
    
    @Autowired
    private PortalSavedRequestAwareAuthenticationSuccessHandler successHandler;
    
    @Bean
    public SimpleUrlAuthenticationFailureHandler failureHandler() {
        return new SimpleUrlAuthenticationFailureHandler("/login.jsp?login_error=true");
    }
    
    @Bean
    public SimpleUrlLogoutSuccessHandler logoutHandler() {
        SimpleUrlLogoutSuccessHandler logoutSuccessHandler =
            new SimpleUrlLogoutSuccessHandler();
        logoutSuccessHandler.setDefaultTargetUrl(logoutUrl.toString());
        return logoutSuccessHandler;
    }
    
    @Bean
    public SimpleUrlLogoutSuccessHandler logoutHandler() {
        SimpleUrlLogoutSuccessHandler logoutSuccessHandler =
            new SimpleUrlLogoutSuccessHandler();
        logoutSuccessHandler.setDefaultTargetUrl(logoutUrl.toString());
        return logoutSuccessHandler;
    }

    @Bean
    public SAMLAuthenticationProvider samlAuthenticationProvider() {
        SAMLAuthenticationProvider authenticationProvider = new SAMLAuthenticationProvider();
        return authenticationProvider;
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http
            .antMatcher("/**")
            .csrf().disable()
            // this was before preauthentication filter in original xml config; I do not see a good reason for this
            .addFilterBefore(samlAuthenticationFilter(), UsernamePasswordAuthenticationFilter.class)
            .sessionManagement().sessionFixation().none()
            .and()
            .authorizeRequests()
            // TODO should this one not be handled by the APISecurityConfig?
                .antMatchers("/webservice.do*")
                    .access("isAuthenticated() or hasIpAddress('127.0.0.1')")
                .antMatchers("/**")
                    .authenticated();

    }

    // Add the samlAuthenticationProvider to the AuthenticationManager that 
    // contains the tokenAuthenticationProvider created in AuthenticatedWebSecurityConfig
    // (see: "Customizing Authentication Managers" @ https://spring.io/guides/topicals/spring-security-architecture
    @Override
    public void configure(AuthenticationManagerBuilder builder) {
        builder.authenticationProvider(samlAuthenticationProvider);
    }

}
