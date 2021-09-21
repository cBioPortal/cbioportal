package org.cbioportal.security.spring;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.MultiThreadedHttpConnectionManager;
import org.cbioportal.security.spring.authentication.PortalSavedRequestAwareAuthenticationSuccessHandler;
import org.opensaml.saml2.metadata.provider.FilesystemMetadataProvider;
import org.opensaml.saml2.metadata.provider.MetadataProviderException;
import org.opensaml.xml.parse.StaticBasicParserPool;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.saml.SAMLAuthenticationProvider;
import org.springframework.security.saml.SAMLDiscovery;
import org.springframework.security.saml.SAMLEntryPoint;
import org.springframework.security.saml.SAMLLogoutFilter;
import org.springframework.security.saml.SAMLLogoutProcessingFilter;
import org.springframework.security.saml.SAMLProcessingFilter;
import org.springframework.security.saml.SAMLWebSSOHoKProcessingFilter;
import org.springframework.security.saml.context.SAMLContextProvider;
import org.springframework.security.saml.context.SAMLContextProviderImpl;
import org.springframework.security.saml.key.JKSKeyManager;
import org.springframework.security.saml.log.SAMLDefaultLogger;
import org.springframework.security.saml.metadata.CachingMetadataManager;
import org.springframework.security.saml.metadata.ExtendedMetadata;
import org.springframework.security.saml.metadata.ExtendedMetadataDelegate;
import org.springframework.security.saml.metadata.MetadataDisplayFilter;
import org.springframework.security.saml.metadata.MetadataGenerator;
import org.springframework.security.saml.metadata.MetadataGeneratorFilter;
import org.springframework.security.saml.parser.ParserPoolHolder;
import org.springframework.security.saml.processor.HTTPArtifactBinding;
import org.springframework.security.saml.processor.HTTPPAOS11Binding;
import org.springframework.security.saml.processor.HTTPPostBinding;
import org.springframework.security.saml.processor.HTTPRedirectDeflateBinding;
import org.springframework.security.saml.processor.HTTPSOAP11Binding;
import org.springframework.security.saml.processor.SAMLProcessorImpl;
import org.springframework.security.saml.userdetails.SAMLUserDetailsService;
import org.springframework.security.saml.util.VelocityFactory;
import org.springframework.security.saml.websso.ArtifactResolutionProfileImpl;
import org.springframework.security.saml.websso.SingleLogoutProfileImpl;
import org.springframework.security.saml.websso.WebSSOProfileConsumerHoKImpl;
import org.springframework.security.saml.websso.WebSSOProfileConsumerImpl;
import org.springframework.security.saml.websso.WebSSOProfileECPImpl;
import org.springframework.security.saml.websso.WebSSOProfileHoKImpl;
import org.springframework.security.saml.websso.WebSSOProfileImpl;
import org.springframework.security.saml.websso.WebSSOProfileOptions;
import org.springframework.security.web.DefaultSecurityFilterChain;
import org.springframework.security.web.FilterChainProxy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.channel.ChannelProcessingFilter;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationFailureHandler;
import org.springframework.security.web.authentication.logout.LogoutHandler;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.security.web.authentication.logout.SimpleUrlLogoutSuccessHandler;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

@Configuration
@ConditionalOnProperty(value = "authenticate", havingValue = "saml")
public class SamlSecurityConfig extends WebSecurityConfigurerAdapter {

    @Value("${saml.logout.url}")
    private URL logoutUrl;
    @Value("${saml.keystore.location}")
    private Resource samlKeystoreLocation;
    @Value("${saml.keystore.password}")
    private String samlKeystorePassword;
    @Value("${saml.keystore.private-key.key}")
    private String samlPrivateKeyKey;
    @Value("${saml.keystore.private-key.password}")
    private String samlPrivateKeyPassword;
    @Value("${saml.keystore.default-key}")
    private String samlKeystoreDefaultKey;
    @Value("${saml.idp.comm.binding.type}")
    private String samlIdpBindingType;
    @Value("${saml.idp.comm.binding.settings}")
    private String samlIdpBindingSettings; // should be 'defaultBinding' or 'specificBinding'
    @Value("${saml.sp.metadata.entityid}")
    private String samlSPMetadataEntityId;
    @Value("${saml.sp.metadata.entityBaseURL}")
    private URL samlSPMetadataEntityBaseUrl;
    @Value("${saml.sp.metadata.wantassertionssigned:true}")
    private boolean samlSPMetadataWantAssertionsSigned;
    @Value("${saml.idp.metadata.location}")
    private File samlIdpMetadataLocation;
    @Value("${saml.custom.userservice.class:org.cbioportal.security.spring.authentication.saml.SAMLUserDetailsServiceImpl}")
    private String samlCustomUserServiceClass;
    
    @Autowired
    private PortalSavedRequestAwareAuthenticationSuccessHandler successHandler;
    
    @Autowired
    private SAMLUserDetailsService samlUserDetailService;
    
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
    public SAMLDefaultLogger samlDefaultLogger() {
        return new SAMLDefaultLogger();
    }
    
    @Bean
    public JKSKeyManager keyManager() {
        Map<String, String> passwords = new HashMap<>();
        passwords.put(samlPrivateKeyKey, samlPrivateKeyPassword);
        return new JKSKeyManager(samlKeystoreLocation, samlKeystorePassword,
            passwords, samlKeystoreDefaultKey);
    }
    
    @Bean
    public SAMLEntryPoint samlEntryPoint() {
        // TODO  defaultBinding param is useless - consider removing it
        WebSSOProfileOptions options = new WebSSOProfileOptions();
        options.setIncludeScoping(false);
        if (samlIdpBindingSettings.equals("specificBinding")) {
            options.setBinding(samlIdpBindingType);
        }
        SAMLEntryPoint samlEntryPoint = new SAMLEntryPoint();
        samlEntryPoint.setDefaultProfileOptions(options);
        return samlEntryPoint;
    }
    
    @Bean
    public SAMLDiscovery samlDiscovery() {
        return new SAMLDiscovery();
    }
    
    @Bean
    public MetadataGeneratorFilter metadataGeneratorFilter() {
        MetadataGenerator mdg = new MetadataGenerator();
        mdg.setEntityId(samlSPMetadataEntityId);
        mdg.setWantAssertionSigned(samlSPMetadataWantAssertionsSigned);
        mdg.setEntityBaseURL(samlSPMetadataEntityBaseUrl.toString());
        ExtendedMetadata md = new ExtendedMetadata();
        md.setIdpDiscoveryEnabled(true);
        md.setSignMetadata(samlSPMetadataWantAssertionsSigned);
        mdg.setExtendedMetadata(md);
        return new MetadataGeneratorFilter(mdg);
    }
    
    @Bean
    public CachingMetadataManager cachingMetadataManager() throws MetadataProviderException {
        FilesystemMetadataProvider filesystemMetadataProvider = new FilesystemMetadataProvider(samlIdpMetadataLocation);
        filesystemMetadataProvider.setParserPool(parserPool());
        ExtendedMetadata extendedMetadata = new ExtendedMetadata();
        ExtendedMetadataDelegate metadataProvider = new ExtendedMetadataDelegate(filesystemMetadataProvider, extendedMetadata);
        metadataProvider.setMetadataTrustCheck(false);
        return new CachingMetadataManager(Arrays.asList(metadataProvider));
    }
    
    @Bean
    public StaticBasicParserPool parserPool() {
        StaticBasicParserPool staticBasicParserPool = new StaticBasicParserPool();
        Map<String, Boolean> builderFeatures = new HashMap<>();
        builderFeatures.put("http://apache.org/xml/features/dom/defer-node-expansion", false);
        staticBasicParserPool.setBuilderFeatures(builderFeatures);
        return staticBasicParserPool;
    }
    
    @Bean
    public MetadataDisplayFilter metadataDisplayFilter() {
        return new MetadataDisplayFilter();
    }

    @Bean
    public SAMLAuthenticationProvider samlAuthenticationProvider() {
        SAMLAuthenticationProvider authenticationProvider = new SAMLAuthenticationProvider();
        authenticationProvider.setUserDetails(samlUserDetailService);
        authenticationProvider.setForcePrincipalAsString(false);
        return authenticationProvider;
    }
    
    @Bean
    public SAMLUserDetailsService samlUserDetailsService()
        throws ClassNotFoundException, NoSuchMethodException, InvocationTargetException,
        InstantiationException, IllegalAccessException {
        // allows users to specify a new service class in the properties file
        return (SAMLUserDetailsService) Class.forName(samlCustomUserServiceClass).getDeclaredConstructor().newInstance();
    }
    
    @Bean
    public SAMLContextProvider samlContextProvider() {
        return new SAMLContextProviderImpl();
    }
    
    @Bean
    public SAMLProcessingFilter samlWebSSOProcessingFilter() throws Exception {
        SAMLProcessingFilter filter = new SAMLProcessingFilter();
        filter.setAuthenticationManager(authenticationManager());
        filter.setAuthenticationSuccessHandler(successHandler);
        filter.setAuthenticationFailureHandler(failureHandler());
        return filter;
    }

    @Bean
    public SAMLWebSSOHoKProcessingFilter samlWebSSOHoKProcessingFilter() throws Exception {
        SAMLWebSSOHoKProcessingFilter filter = new SAMLWebSSOHoKProcessingFilter();
        filter.setAuthenticationManager(authenticationManager());
        filter.setAuthenticationSuccessHandler(successHandler);
        filter.setAuthenticationFailureHandler(failureHandler());
        return filter;
    }
    
    @Bean
    public SecurityContextLogoutHandler securityContextLogoutHandler() {
        SecurityContextLogoutHandler logoutHandler = new SecurityContextLogoutHandler();
        logoutHandler.setInvalidateHttpSession(true);
        return logoutHandler;
    }
    
    @Bean
    public SAMLLogoutFilter samlLogoutFilter() {
        return new SAMLLogoutFilter(
            logoutHandler(),
            new LogoutHandler[]{securityContextLogoutHandler()},
            new LogoutHandler[]{securityContextLogoutHandler()}
        );
    }
    
    @Bean
    public SAMLLogoutProcessingFilter samlLogoutProcessingFilter() {
        return new SAMLLogoutProcessingFilter(logoutHandler(), securityContextLogoutHandler());
    }
    
    @Bean
    public SAMLProcessorImpl samlProcessor() {
        return new SAMLProcessorImpl(Arrays.asList(
            new HTTPRedirectDeflateBinding(parserPool()),
            new HTTPPostBinding(parserPool(), VelocityFactory.getEngine()),
            new HTTPArtifactBinding(parserPool(), VelocityFactory.getEngine(), new ArtifactResolutionProfileImpl(
                new HttpClient(new MultiThreadedHttpConnectionManager())
            )),
            new HTTPSOAP11Binding(parserPool()),
            new HTTPPAOS11Binding(parserPool())
        ));
    }
    
    @Bean
    public ParserPoolHolder parserPoolHolder() {
        return new ParserPoolHolder();
    }
    
    @Bean
    public WebSSOProfileConsumerImpl webSSOProfileConsumer() {
        return new WebSSOProfileConsumerImpl();
    }

    @Bean
    public WebSSOProfileConsumerHoKImpl webSSOProfileConsumerHoK() {
        return new WebSSOProfileConsumerHoKImpl();
    }

    @Bean
    public WebSSOProfileImpl webSSOProfile() {
        return new WebSSOProfileImpl();
    }

    @Bean
    public WebSSOProfileHoKImpl webSSOProfileHoK() {
        return new WebSSOProfileHoKImpl();
    }

    @Bean
    public WebSSOProfileECPImpl webSSOProfileECP() {
        return new WebSSOProfileECPImpl();
    }

    @Bean
    public SingleLogoutProfileImpl singleLogoutProfile() {
        return new SingleLogoutProfileImpl();
    }
    
    @Bean
    public SAMLDiscovery samlIdpDiscovery() {
        SAMLDiscovery samlDiscovery = new SAMLDiscovery();
        samlDiscovery.setIdpSelectionPath("/login.jsp");
        return samlDiscovery;
    }
    
    @Bean
    public FilterChainProxy samlFilterChain() throws Exception {
        List<SecurityFilterChain> filterChains = Arrays.asList(
            new DefaultSecurityFilterChain(new AntPathRequestMatcher("/saml/login/**"),
                samlEntryPoint()),
            new DefaultSecurityFilterChain(new AntPathRequestMatcher("/saml/logout/**"),
                samlLogoutFilter()),
            new DefaultSecurityFilterChain(new AntPathRequestMatcher("/saml/metadata/**"),
                metadataDisplayFilter()),
            new DefaultSecurityFilterChain(new AntPathRequestMatcher("/saml/SSO/**"),
                samlWebSSOProcessingFilter()),
            new DefaultSecurityFilterChain(new AntPathRequestMatcher("/saml/SSOHoK/**"),
                samlWebSSOHoKProcessingFilter()),
            new DefaultSecurityFilterChain(new AntPathRequestMatcher("/saml/SSOHoK/**"),
                samlWebSSOHoKProcessingFilter()),
            new DefaultSecurityFilterChain(new AntPathRequestMatcher("/saml/SingleLogout/**"),
                samlLogoutFilter()),
            new DefaultSecurityFilterChain(new AntPathRequestMatcher("/saml/discovery/**"),
                samlIdpDiscovery())
        );
        return new FilterChainProxy(filterChains);
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http
            .antMatcher("/**")
            .csrf().disable()
            .addFilterBefore(metadataGeneratorFilter(), ChannelProcessingFilter.class)
            .addFilterAfter(samlFilterChain(), BasicAuthenticationFilter.class)
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
        builder.authenticationProvider(samlAuthenticationProvider());
    }

}
