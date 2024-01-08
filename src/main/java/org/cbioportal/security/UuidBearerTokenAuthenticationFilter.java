package org.cbioportal.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.cbioportal.model.UserAuthorities;
import org.cbioportal.persistence.SecurityRepository;
import org.cbioportal.service.DataAccessTokenService;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

import static com.google.common.net.HttpHeaders.AUTHORIZATION;


public class UuidBearerTokenAuthenticationFilter extends OncePerRequestFilter {

    private static final String BEARER = "Bearer";
    
    private final DataAccessTokenService tokenService;
    private final SecurityRepository securityRepository;

    public UuidBearerTokenAuthenticationFilter(DataAccessTokenService tokenService, SecurityRepository securityRepository) {
        this.securityRepository = securityRepository;
        this.tokenService = tokenService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        String token = extractHeaderToken(request);

        if (token == null) {
            this.logger.error("No token was found in request header.");
            filterChain.doFilter(request, response);
        }

        boolean isTokenValid = tokenService.isValid(token);
        if(isTokenValid) {
            var user = tokenService.getUsername(token);
            UserAuthorities authorities = securityRepository.getPortalUserAuthorities(user);
            Set<GrantedAuthority> mappedAuthorities = new HashSet<>();
            if (!Objects.isNull(authorities)) {
                mappedAuthorities.addAll(AuthorityUtils.createAuthorityList(authorities.getAuthorities()));
            }
            Authentication auth = new UsernamePasswordAuthenticationToken(user, "does not match unused", mappedAuthorities);
            SecurityContextHolder.getContext().setAuthentication(auth); 
        } else {
            logger.debug("Token not Valid");
        }
        
        filterChain.doFilter(request, response);
    }

    private String extractHeaderToken(HttpServletRequest request) {
        String authorizationHeader = request.getHeader(AUTHORIZATION);
        if (!authorizationHeader.isEmpty() && (authorizationHeader.toLowerCase().startsWith(BEARER.toLowerCase()))) {
            return authorizationHeader.substring(BEARER.length()).trim();
        }

        return null;
    }

    @Override
    protected boolean shouldNotFilterAsyncDispatch() {
        return true;
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String param = request.getHeader(AUTHORIZATION);
        if (param == null) {
            logger.debug("attemptAuthentication(), authorization header is null, continue on to other security filters");
            return true;
        }
        return false;
    }
}
