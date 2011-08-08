package org.mskcc.cgds.test.util;

import junit.framework.TestCase;
import org.mskcc.cgds.dao.DaoCancerStudy;
import org.mskcc.cgds.dao.DaoException;
import org.mskcc.cgds.dao.DaoUser;
import org.mskcc.cgds.dao.DaoUserAccessRight;
import org.mskcc.cgds.model.CancerStudy;
import org.mskcc.cgds.model.User;
import org.mskcc.cgds.model.UserAccessRight;
import org.mskcc.cgds.scripts.ImportTypesOfCancers;
import org.mskcc.cgds.scripts.ResetDatabase;
import org.mskcc.cgds.util.AccessControl;
import org.mskcc.cgds.util.internal.AccessControlImpl;
import org.mskcc.cgds.util.ProgressMonitor;
import org.mskcc.cgds.web_api.ProtocolException;

import java.io.IOException;
import java.io.File;

public class TestAccessControl extends TestCase {

    CancerStudy publicCancerStudy;
    CancerStudy privateCancerStudy1;
    CancerStudy privateCancerStudy2;
    User user1;
    User user2;
    String clearTextKey;
	AccessControl accessControl = new AccessControlImpl();

    public void testSecretKeys() throws Exception {
        ResetDatabase.resetDatabase();
        clearTextKey = "aSecretKey";
        assertFalse(accessControl.checkKey(clearTextKey));
        accessControl.createSecretKey(clearTextKey);
        assertTrue(accessControl.checkKey(clearTextKey));
    }

    public void testVariousUtilities() throws Exception {
        setUpDBMS();

        UserAccessRight userAccessRight = new UserAccessRight(user1.getEmail(), privateCancerStudy1.getStudyId());
        DaoUserAccessRight.addUserAccessRight(userAccessRight);

        userAccessRight = new UserAccessRight(user2.getEmail(), privateCancerStudy2.getStudyId());
        DaoUserAccessRight.addUserAccessRight(userAccessRight);

        // test accessControl.checkAccess
        // assertFalse( accessControl.checkAccess( "", "", CancerStudy.NO_SUCH_STUDY ) );

        // test access to a public study (id = 3)
        assertTrue(accessControl.checkAccess("", "", publicCancerStudy.getCancerStudyIdentifier()));
        assertTrue(accessControl.checkAccess(null, null, publicCancerStudy.getCancerStudyIdentifier()));
        assertTrue(accessControl.checkAccess("blah", "blah", publicCancerStudy.getCancerStudyIdentifier()));
        assertFalse(accessControl.checkAccess("", clearTextKey + "_NOT_KEY", privateCancerStudy1.getCancerStudyIdentifier()));

        assertTrue(accessControl.checkAccess(user1.getEmail(), clearTextKey, privateCancerStudy1.getCancerStudyIdentifier()));
        assertTrue(accessControl.checkAccess(user2.getEmail(), clearTextKey, privateCancerStudy2.getCancerStudyIdentifier()));
        assertFalse(accessControl.checkAccess(user2.getEmail(), clearTextKey, privateCancerStudy1.getCancerStudyIdentifier()));
        assertFalse(accessControl.checkAccess(user1.getEmail(), clearTextKey, privateCancerStudy2.getCancerStudyIdentifier()));

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
        DaoCancerStudy.deleteCancerStudy(publicCancerStudy.getStudyId());
        try {
            studies = accessControl.getCancerStudies("no such email", clearTextKey);
            fail("Should throw ProtocolException.");
        } catch (ProtocolException e) {
            assertEquals("No cancer studies accessible; either provide credentials to access private studies, " +
                    "or ask administrator to load public ones.\n",
                    e.getMsg());

        }
    }

    private void setUpDBMS() throws DaoException, IOException {
        ResetDatabase.resetDatabase();

        user1 = new User("artg@gmail.com", "Arthur");
        DaoUser.addUser(user1);
        user2 = new User("joe@gmail.com", "J");
        DaoUser.addUser(user2);

        // load cancers
        ImportTypesOfCancers.load(new ProgressMonitor(), new File("testData/cancers.txt"));
        
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

    }
}