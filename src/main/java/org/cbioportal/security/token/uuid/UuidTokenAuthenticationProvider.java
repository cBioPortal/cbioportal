package org.cbioportal.security.token.uuid;

import org.cbioportal.model.UserAuthorities;
import org.cbioportal.persistence.SecurityRepository;
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

    private final SecurityRepository securityRepository;
    
    public UuidTokenAuthenticationProvider(final SecurityRepository securityRepository) {
        this.securityRepository = securityRepository;
    }
    
    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        String user = (String) authentication.getPrincipal();
        log.debug("Attempt to grab user Authorities for user: {}", user);
        UserAuthorities authorities = securityRepository.getPortalUserAuthorities(user);
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
