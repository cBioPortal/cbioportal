package org.cbioportal.security.spring.authentication.saml;

import java.util.List;
import java.util.stream.Collectors;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.cbioportal.security.spring.authentication.PortalUserDetails;
import org.opensaml.core.xml.schema.XSString;
import org.opensaml.core.xml.schema.impl.XSAnyImpl;
import org.opensaml.saml.saml2.core.Assertion;
import org.opensaml.saml.saml2.core.AttributeStatement;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.convert.converter.Converter;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.AuthenticatedPrincipal;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.saml2.provider.service.authentication.OpenSaml4AuthenticationProvider;
import org.springframework.security.saml2.provider.service.authentication.Saml2Authentication;
import org.springframework.util.Assert;

// This class handles the extraction of user roles from SAML assertion or the database
// when the user was authenticated with success by SAML IDP.
public class SamlResponseAuthenticationConverter implements
    Converter<OpenSaml4AuthenticationProvider.ResponseToken, AbstractAuthenticationToken> {

    private static Log log = LogFactory.getLog(SamlResponseAuthenticationConverter.class);

    @Value("${saml.idp.metadata.attribute.email:}")
    private String assertionAttributeEmail;
    
    @Value("${saml.idp.metadata.attribute.role:}")
    private String assertionAttributeRoles;

    @Value("${app.name:}")
    private String appName;
    
    @Value("${saml.roles-from-database:false}")
    private boolean rolesFromDatabase;
    
    // TODO This filter_groups_by_appname feature is unclear to me.
    // We should write some tests to establish correct logic.
    @Value("${filter_groups_by_appname:true}")
    private String filterGroupsByAppName;

    private UserDetailsService userDetailsService;

    public SamlResponseAuthenticationConverter(
        UserDetailsService userDetailsService) {
        Assert.notNull(userDetailsService, "userDetailService cannot be null");
        this.userDetailsService = userDetailsService;
    }

    @Override
    public AbstractAuthenticationToken convert(
        OpenSaml4AuthenticationProvider.ResponseToken responseToken) {

        Assertion assertion = responseToken.getResponse().getAssertions().get(0);
        // TODO add this logic to docs
        // By default, the email address is taken from the Subject::NameID attribute.
        // Alternatively, the email address can be read from an attribute when configured.
        String username = assertion.getSubject().getNameID().getValue();
        if (!assertionAttributeEmail.isEmpty()) {
            String[] usernames = extractAttributeValues(assertionAttributeEmail,
                assertion.getAttributeStatements());
            if (usernames.length > 1) {
                String message =
                    "Multiple email addresses provided in the SAML assertion under attribute name '" +
                        assertionAttributeEmail + "'";
                log.error(message);
                throw new IllegalArgumentException(message);
            }
            username = usernames[0];
        }
        log.debug("Using '" + username + "' as username for the logged in user.");

        // When configured, lookup roles from the database. By default, get roles from the assertion.
        List<GrantedAuthority> grantedAuthorities;
        if (rolesFromDatabase) {
            UserDetails userDetails = userDetailsService.loadUserByUsername(username);
            grantedAuthorities =
                userDetails.getAuthorities().stream().collect(Collectors.toList());
        } else {
            grantedAuthorities = AuthorityUtils.createAuthorityList(
                extractAttributeValues(assertionAttributeRoles, assertion.getAttributeStatements())
            );
        }
        log.debug("Using " + grantedAuthorities.stream().toArray() +
            " as roles for the logged in user.");

        PortalUserDetails portalUser = new PortalUserDetails(username, grantedAuthorities);
        final UserDetails user = User.withUserDetails(portalUser)
            .authorities(grantedAuthorities)
            .build();

        // Get the Authentication from the provider. When no exception is thrown this 
        // means that the user is allowed access. 
        Saml2Authentication samlAuthentication = OpenSaml4AuthenticationProvider
            .createDefaultResponseAuthenticationConverter()
            .convert(responseToken);

        Saml2Authentication authentication =
            new Saml2Authentication((AuthenticatedPrincipal) samlAuthentication.getPrincipal(),
                samlAuthentication.getSaml2Response(), grantedAuthorities);
        authentication.setDetails(user);

        return authentication;
    }

    // Returns array of unique values.
    private String[] extractAttributeValues(String attributeName,
                                            List<AttributeStatement> attributeStatements) {
        return attributeStatements.stream()
            .flatMap(attributeStatement -> attributeStatement.getAttributes().stream())
            .filter(attribute -> attribute.getName().equals(attributeName))
            .flatMap(attribute -> attribute.getAttributeValues().stream())
            .map(xmlObj -> {
                if (xmlObj == null) {
                    return null;
                }
                if (xmlObj instanceof XSString) {
                    return ((XSString) xmlObj).getValue();
                }
                if (xmlObj instanceof XSAnyImpl) {
                    return ((XSAnyImpl) xmlObj).getTextContent();
                }
                return xmlObj.toString();
            })
            .distinct()
            .toArray(String[]::new);
    }

}
