/*
 * Copyright (c) 2015 Memorial Sloan-Kettering Cancer Center.
 *
 * This library is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY, WITHOUT EVEN THE IMPLIED WARRANTY OF MERCHANTABILITY OR FITNESS
 * FOR A PARTICULAR PURPOSE. The software and documentation provided hereunder
 * is on an "as is" basis, and Memorial Sloan-Kettering Cancer Center has no
 * obligations to provide maintenance, support, updates, enhancements or
 * modifications. In no event shall Memorial Sloan-Kettering Cancer Center be
 * liable to any party for direct, indirect, special, incidental or
 * consequential damages, including lost profits, arising out of the use of this
 * software and its documentation, even if Memorial Sloan-Kettering Cancer
 * Center has been advised of the possibility of such damage.
 */

/*
 * This file is part of cBioPortal.
 *
 * cBioPortal is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
*/

package org.mskcc.cbio.portal.util;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.context.support.GenericXmlApplicationContext;
import org.springframework.stereotype.Component;

@Component
public class SpringUtil
{
    private static final Log log = LogFactory.getLog(SpringUtil.class);

    private static AccessControl accessControl;
    private static ApplicationContext context;
    private static GenericXmlApplicationContext applicationContext;

    @Autowired
    public void setAccessControl(AccessControl accessControl) {
        log.debug("Setting access control");
        SpringUtil.accessControl = accessControl;
    }

    public static AccessControl getAccessControl()
    {
        return accessControl;
    }

    public static synchronized void initDataSource()
    {
        if (SpringUtil.context == null) {
            context = new ClassPathXmlApplicationContext("classpath:applicationContext-persistenceConnections.xml", "classpath:applicationContext-ehcache.xml", "classpath:applicationContext-rediscache.xml");
        }
    }

    /**
     * Get the app context as initialized or refreshed by initDataSource()
     *
     * @return the Spring Framework application context
     */
    public static ApplicationContext getApplicationContext() {
        return context;
    }

    /**
     * setter to allow override by unit test classes (which run in different context, connecting
     * to test DB).
     *
     * @param context
     */
    public static void setApplicationContext(ApplicationContext context) {
        SpringUtil.context = context;
    }

    /**
     * Directly injects a context into the class, so we don't need to open
     * any more XML files.
     *
     * @param context
     */
    public static synchronized void initDataSource(ApplicationContext context)
    {
        SpringUtil.context = context;
    }
}
