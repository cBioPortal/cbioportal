package org.mskcc.cgds.scripts;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;

import org.mskcc.cgds.dao.DaoCancerStudy;
import org.mskcc.cgds.dao.DaoUser;
import org.mskcc.cgds.dao.DaoUserAccessRight;
import org.mskcc.cgds.model.User;
import org.mskcc.cgds.model.UserAccessRight;
import org.mskcc.cgds.util.ConsoleUtil;
import org.mskcc.cgds.util.ProgressMonitor;

/**
 * Import a file of user access rights. For convenience, creates the users if
 * they don't exist. 
 * File contains the fields email address\tstudyId\tuser name
 * user name is optional 
 * studyId must refer to the field cancer_study.CANCER_STUDY_ID
 * 
 * @author Arthur Goldberg goldberg@cbio.mskcc.org
 */
public class ImportUserAccessRights {

   public static void main(String[] args) throws Exception {
      if (args.length == 0) {
         System.out.println("command line usage: importUserAccessRights.pl <access_rights_file.txt>");
         System.exit(1);
      }

      ProgressMonitor pMonitor = new ProgressMonitor();
      pMonitor.setConsoleMode(true);

      File file = new File(args[0]);
      FileReader reader = new FileReader(file);
      BufferedReader buf = new BufferedReader(reader);
      String line = buf.readLine();
      int count = 0;
      while (line != null) {
         if (pMonitor != null) {
            pMonitor.incrementCurValue();
            ConsoleUtil.showProgress(pMonitor);
         }
         if (!line.startsWith("#") && line.trim().length() > 0) {
            try {
               addStudy(line);
               count++;
            } catch (Exception e) {
               System.err.println("Could not add line '" + line + "'. " + e);
            }
         }
         line = buf.readLine();
      }
      System.err.println("Added " + count + " user access rights.");
      ConsoleUtil.showWarnings(pMonitor);
      System.err.println("Done.");
   }

   private static void addStudy(String line) throws Exception {
      line = line.trim();
      String parts[] = line.split("\t");
      String email = parts[0];
      String cancerStudyId = parts[1];
      String name = null;
      if (2 < parts.length) {
         name = parts[2];
      }

      // if user doesn't exist create them
      User user = DaoUser.getUserByEmail(email);
      if (null == user) {
         user = new User(email, name);
         DaoUser.addUser(user);
      }

      // check that study exists
      int intID = -1;
      intID = Integer.valueOf(cancerStudyId);
      if (!DaoCancerStudy.doesCancerStudyExistByInternalId(intID)) {
         throw new Exception("no study identified by " + cancerStudyId);
      }

      // add access right
      UserAccessRight userAccessRight = new UserAccessRight(email, intID);
      DaoUserAccessRight.addUserAccessRight(userAccessRight);
   }
}