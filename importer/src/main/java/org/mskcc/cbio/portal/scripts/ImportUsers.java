/*
 * Copyright (c) 2015 Memorial Sloan-Kettering Cancer Center.
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package org.mskcc.cbio.portal.scripts;

import java.io.*;
import java.util.*;
import org.mskcc.cbio.portal.dao.DaoUser;
import org.mskcc.cbio.portal.dao.DaoUserAuthorities;
import org.mskcc.cbio.portal.model.User;
import org.mskcc.cbio.portal.model.UserAuthorities;
import org.mskcc.cbio.portal.util.ConsoleUtil;
import org.mskcc.cbio.portal.util.ProgressMonitor;
import org.mskcc.cbio.portal.util.TsvUtil;

/**
 * Import a file of users and their authorities.
 *
 * File contains the fields:
 *
 * EMAIL_ADDRESS\tUSERNAME\tENABLED\tAUTHORITIES
 *
 * AUTHORITIES is semicolon separated list of authorites
 * 
 * @author Arthur Goldberg goldberg@cbio.mskcc.org
 * @author Benjamin Gross
 */
public class ImportUsers {

    public static void main(String[] args) throws Exception {
        if (args.length == 0) {
            System.out.println("command line usage: java org.mskcc.cbio.portal.scripts.ImportUsers <users_file.txt>");
            return;
        }

        ProgressMonitor.setConsoleMode(true);

        File file = new File(args[0]);
        int count = 0;
        try (FileReader reader = new FileReader(file);
             BufferedReader buf = new BufferedReader(reader)) {
            String line = buf.readLine();
            while (line != null) {
                ProgressMonitor.incrementCurValue();
                ConsoleUtil.showProgress();
                if (TsvUtil.isDataLine(line)) {
                    try {
                        addUser(line);
                        count++;
                    } catch (Exception e) {
                        System.err.println("Could not add line '" + line + "'. " + e);
                    }
                }
                line = buf.readLine();
            }
        }
        System.err.println("Added " + count + " user access rights.");
        ConsoleUtil.showWarnings();
        System.err.println("Done.");
    }

    private static void addUser(String line) throws Exception {
        line = line.trim();
        String parts[] = line.split("\t");
        if (parts.length != 4) {
            throw new IllegalArgumentException("Missing a user attribute, parts: " + parts.length);
        }
        String email = parts[0];
        String name = parts[1];
        Boolean enabled = Boolean.valueOf(parts[2]);
        List authorities = Arrays.asList(parts[3].split(";"));

        // if user doesn't exist create them
        User user = DaoUser.getUserByEmail(email);
        if (null == user) {
            user = new User(email, name, enabled);
            DaoUser.addUser(user);
        }

        // if exist, delete user authorities
        UserAuthorities currentAuthorities = DaoUserAuthorities.getUserAuthorities(user);
        if (currentAuthorities != null) {
            DaoUserAuthorities.removeUserAuthorities(user);
        }

        // add new authorities
        UserAuthorities userAuthorities = new UserAuthorities(email, authorities);
        DaoUserAuthorities.addUserAuthorities(userAuthorities);
    }
}
