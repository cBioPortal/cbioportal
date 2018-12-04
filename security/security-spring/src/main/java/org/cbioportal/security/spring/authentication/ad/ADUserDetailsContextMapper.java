package org.cbioportal.security.spring.authentication.ad;

import java.text.MessageFormat;
import java.util.Collection;
import java.util.List;

import javax.naming.directory.Attribute;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.cbioportal.model.User;
import org.cbioportal.model.UserAuthorities;
import org.cbioportal.persistence.SecurityRepository;
import org.cbioportal.security.spring.authentication.PortalUserDetails;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ldap.core.DirContextAdapter;
import org.springframework.ldap.core.DirContextOperations;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.ldap.userdetails.UserDetailsContextMapper;

/**
 * Created by Jonathan Smith on 29/11/2018
 */
public class ADUserDetailsContextMapper implements UserDetailsContextMapper {

    /**
     * {@link Log} to use
     */
    private static final Log LOG = LogFactory.getLog(ADUserDetailsContextMapper.class);

    /**
     * data access object to use for retrieving the authorities from the database
     */
    @Autowired
    private SecurityRepository securityRepository;

    @Override
    public UserDetails mapUserFromContext(DirContextOperations ctx, String username, Collection<? extends GrantedAuthority> authorities) {
        String name = ctx.getAttributeSortedStringSet("name").first();
        String email = ctx.getAttributeSortedStringSet("mail").first();
        String givenName = ctx.getAttributeSortedStringSet("givenName").first();
        String lastName = ctx.getAttributeSortedStringSet("sn").first();

        // Construct the resulting PortalUserDetails object.)
        PortalUserDetails result = new PortalUserDetails(username, getGrantedAuthorities(email));
//        PortalUserDetails result = new PortalUserDetails(username, AuthorityUtils.createAuthorityList("cbioportal:ALL"));
        result.setName(name);
        result.setEmail(email);
        result.setName(MessageFormat.format("{0} {1}", givenName, lastName));
        return result;
    }

    @Override
    public void mapUserToContext(UserDetails user, DirContextAdapter ctx) {
        throw new UnsupportedOperationException();
    }

    /**
     * Retrieve List of {@link GrantedAuthority} objects for a user's email.
     * <p>
     * This list is loaded from the cBioPortal database.
     *
     * @param email email to use for the querying
     * @return resulting list of granted authorities
     */
    private List<GrantedAuthority> getGrantedAuthorities(String email) {
        User user = null;
        try {
            user = securityRepository.getPortalUser(email);
        }
        catch (Exception e) {
            // ignore, handle below with user == null
        }

        if (user == null || !user.isEnabled()) {
            LOG.debug("user not found or not enabled " + email);
            return AuthorityUtils.createAuthorityList(new String[0]);
        }
        else {
            LOG.debug("getGrantedAuthorities(), attempting to fetch portal user authorities, email: " + email);
            UserAuthorities authorities = null;
            try {
                authorities = securityRepository.getPortalUserAuthorities(email);
            }
            catch (Exception e) {
                LOG.debug("problem with database query:" + e.getMessage());
                throw new UsernameNotFoundException("Could not retrieve authorities for " + email, e);
            }
            if (authorities != null)
                return AuthorityUtils.createAuthorityList(
                    authorities.getAuthorities().toArray(new String[authorities.getAuthorities().size()]));
            else
                return AuthorityUtils.createAuthorityList(new String[0]);
        }
    }
}
