/** Copyright (c) 2012 Memorial Sloan-Kettering Cancer Center.
 *
 * This library is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY, WITHOUT EVEN THE IMPLIED WARRANTY OF
 * MERCHANTABILITY OR FITNESS FOR A PARTICULAR PURPOSE.  The software and
 * documentation provided hereunder is on an "as is" basis, and
 * Memorial Sloan-Kettering Cancer Center 
 * has no obligations to provide maintenance, support,
 * updates, enhancements or modifications.  In no event shall
 * Memorial Sloan-Kettering Cancer Center
 * be liable to any party for direct, indirect, special,
 * incidental or consequential damages, including lost profits, arising
 * out of the use of this software and its documentation, even if
 * Memorial Sloan-Kettering Cancer Center 
 * has been advised of the possibility of such damage.
*/
package org.mskcc.cbio.portal.authentication.activedirectory;

import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import javax.servlet.*;
import javax.servlet.http.*;
import java.io.IOException;

public class ADAuthFilter extends UsernamePasswordAuthenticationFilter
{

	@Override
	protected void successfulAuthentication(HttpServletRequest request, HttpServletResponse response,
	                                        FilterChain filterChain, Authentication authResult) throws IOException, ServletException
	{
		super.successfulAuthentication(request, response, filterChain, authResult);
		String username = super.obtainUsername(request);
		System.out.println("Authenticated as: " + username);
	}
}