package org.cbioportal.application.security.token.uuid;

import org.cbioportal.legacy.model.UserAuthorities;
import org.cbioportal.legacy.persistence.SecurityRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.AuthorityUtils;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

public class UuidTokenAuthenticationProvider implements AuthenticationProvider {
    private static final Logger log = LoggerFactory.getLogger(UuidTokenAuthenticationProvider.class);

    private final SecurityRepository<Authentication> securityRepository;
    
    public UuidTokenAuthenticationProvider(final SecurityRepository<Authentication> securityRepository) {
        this.securityRepository = securityRepository;
    }
    
    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        String user = (String) authentication.getPrincipal();
        log.debug("Attempt to grab user Authorities for user: {}", user);

        // TODO: we should probably document what attributes are being sent based on the provided auth method
        UserAuthorities authorities = securityRepository.getPortalUserAuthorities(user, authentication);
        Set<GrantedAuthority> mappedAuthorities = new HashSet<>();
        if (!Objects.isNull(authorities)) {
            mappedAuthorities.addAll(AuthorityUtils.createAuthorityList(authorities.getAuthorities()));
        }
        return new UsernamePasswordAuthenticationToken(user, "does not match unused", mappedAuthorities);
    }

    @Override
    public boolean supports(Class<?> authentication) {
        return authentication.isAssignableFrom(UsernamePasswordAuthenticationToken.class);
    }
}
