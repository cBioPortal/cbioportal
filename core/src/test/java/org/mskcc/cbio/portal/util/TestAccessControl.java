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

import java.io.IOException;
import org.junit.Test;
import org.mskcc.cbio.portal.dao.DaoException;
import org.mskcc.cbio.portal.model.CancerStudy;
import org.mskcc.cbio.portal.model.User;

/**
 * JUnit test for AccessControl class.
 */
public class TestAccessControl {
    private CancerStudy publicCancerStudy;
    private CancerStudy privateCancerStudy1;
    private CancerStudy privateCancerStudy2;
    private User user1;
    private User user2;
    private String clearTextKey;

    @Test
    public void testVariousUtilities() throws Exception {
        /* TBD: Recoded when we provide granualar access

        setUpDBMS();

        UserAccessRight userAccessRight = new UserAccessRight(user1.getEmail(), privateCancerStudy1.getInternalId());
        DaoUserAccessRight.addUserAccessRight(userAccessRight);

        userAccessRight = new UserAccessRight(user2.getEmail(), privateCancerStudy2.getInternalId());
        DaoUserAccessRight.addUserAccessRight(userAccessRight);

        // test accessControl.checkAccess
        // assertFalse( accessControl.checkAccess( "", "", CancerStudy.NO_SUCH_STUDY ) );

        // test access to a public study (id = 3)
        assertTrue(accessControl.checkAccess("", "", publicCancerStudy.getCancerStudyStableId()));
        assertTrue(accessControl.checkAccess(null, null, publicCancerStudy.getCancerStudyStableId()));
        assertTrue(accessControl.checkAccess("blah", "blah", publicCancerStudy.getCancerStudyStableId()));
        assertFalse(accessControl.checkAccess("", clearTextKey + "_NOT_KEY",
                        privateCancerStudy1.getCancerStudyStableId()));

        assertTrue(accessControl.checkAccess(user1.getEmail(), clearTextKey,
                                        privateCancerStudy1.getCancerStudyStableId()));
        assertTrue(accessControl.checkAccess(user2.getEmail(), clearTextKey,
                        privateCancerStudy2.getCancerStudyStableId()));
        assertFalse(accessControl.checkAccess(user2.getEmail(), clearTextKey,
                        privateCancerStudy1.getCancerStudyStableId()));
        assertFalse(accessControl.checkAccess(user1.getEmail(), clearTextKey,
                        privateCancerStudy2.getCancerStudyStableId()));

        // test accessControl.getCancerStudies
        // just public studies
        String studies = accessControl.getCancerStudies("no such email", clearTextKey + "_NOT_KEY");
        assertEquals("cancer_study_id\tname\tdescription\nstudy3\tpublic name\tdescription\n", studies);

        studies = accessControl.getCancerStudies(null, null);
        assertEquals("cancer_study_id\tname\tdescription\nstudy3\tpublic name\tdescription\n", studies);

        // public and private studies
        studies = accessControl.getCancerStudies(user1.getEmail(), clearTextKey);
        assertEquals("cancer_study_id\tname\tdescription\n" +
                "study1\tname\tdescription\nstudy3\tpublic name\tdescription\n",
                studies);

        // no studies; delete the public one
        DaoCancerStudy.deleteCancerStudy(publicCancerStudy.getInternalId());
        try {
            studies = accessControl.getCancerStudies("no such email", clearTextKey);
            fail("Should throw ProtocolException.");
        } catch (ProtocolException e) {
            assertEquals("No cancer studies accessible; either provide credentials to access private studies, " +
                    "or ask administrator to load public ones.\n",
                    e.getMsg());

        }
        */
    }

    private void setUpDBMS() throws DaoException, IOException {
        /*
        ResetDatabase.resetDatabase();

        user1 = new User("artg@gmail.com", "Arthur", true);
        DaoUser.addUser(user1);
        user2 = new User("joe@gmail.com", "J", true);
        DaoUser.addUser(user2);

        // load cancers
        ImportTypesOfCancers.load(new File("/cancers.txt"));
        
        // make a couple of private studies (1 and 2)
        privateCancerStudy1 = new CancerStudy("name", "description", "study1", "brca", false);
        DaoCancerStudy.addCancerStudy(privateCancerStudy1);  // 1
        privateCancerStudy2 = new CancerStudy("other name", "other description", "study2", "brca", false);
        DaoCancerStudy.addCancerStudy(privateCancerStudy2);  // 2

        publicCancerStudy = new CancerStudy("public name", "description", "study3", "brca", true);
        DaoCancerStudy.addCancerStudy(publicCancerStudy);  // 3

        clearTextKey = "aSecretKey";
        accessControl.createSecretKey(clearTextKey);
        accessControl.createSecretKey("another Example key");
        */
    }
}
