package org.mskcc.cbio.portal.authentication.oidc;

import java.util.ArrayList;
import java.util.Collection;

import org.mitre.openid.connect.client.OIDCAuthenticationProvider;
import org.mitre.openid.connect.model.OIDCAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.mskcc.cbio.portal.authentication.oidc.PortalOIDCAuthoritiesMapper;

public class PortalOIDCAuthenticationProvider extends OIDCAuthenticationProvider {
	
	private PortalOIDCAuthoritiesMapper authoritiesMapper;

	public PortalOIDCAuthoritiesMapper getAuthoritiesMapper() {
		return authoritiesMapper;
	}

	public void setAuthoritiesMapper(PortalOIDCAuthoritiesMapper authoritiesMapper) {
		this.authoritiesMapper = authoritiesMapper;
	}

	@Override
	public Authentication authenticate(Authentication authentication) throws AuthenticationException {
		Authentication result = super.authenticate(authentication);
		
		if (result instanceof OIDCAuthenticationToken && result.isAuthenticated()) {

			OIDCAuthenticationToken token = (OIDCAuthenticationToken) result;
			
			String username = token.getUserInfo().getEmail();

			Collection<GrantedAuthority> portalAuthorities = new ArrayList<GrantedAuthority>();
			
			portalAuthorities = authoritiesMapper.getPortalAuthorities(username);

			return new OIDCAuthenticationToken(token.getSub(),
					token.getIssuer(),
					token.getUserInfo(), portalAuthorities,
					token.getIdTokenValue(), token.getAccessTokenValue(), token.getRefreshTokenValue());
		} else {
			return result;
		}
		
	}
}
