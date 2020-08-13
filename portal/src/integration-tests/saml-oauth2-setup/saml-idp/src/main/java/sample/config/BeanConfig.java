package sample.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.saml.provider.SamlServerConfiguration;
import org.springframework.security.saml.provider.identity.AssertionEnhancer;
import org.springframework.security.saml.provider.identity.config.SamlIdentityProviderServerBeanConfiguration;
import org.springframework.security.saml.saml2.attribute.Attribute;

@Configuration
public class BeanConfig extends SamlIdentityProviderServerBeanConfiguration {
	private final AppConfig config;

	public BeanConfig(AppConfig config) {
		this.config = config;
	}

	@Override
	protected SamlServerConfiguration getDefaultHostSamlServerConfiguration() {
		return config;
	}

	@Bean(name = "samlAssertionEnhancer")
	public AssertionEnhancer samlAssertionEnhancer(@Value("${user.email}") String testEmail) {
		return assertion -> {
			Attribute emailAttribute = new Attribute();
			emailAttribute.setName("User.email");
			emailAttribute.addValues(testEmail);
			assertion.addAttribute(emailAttribute);
			return assertion;
		};
	}

	@Bean
	public UserDetailsService userDetailsService() {
		UserDetails userDetails = User.withDefaultPasswordEncoder()
			.username("user")
			.password("password")
			.roles("USER")
			.build();
		return new InMemoryUserDetailsManager(userDetails);
	}
}
