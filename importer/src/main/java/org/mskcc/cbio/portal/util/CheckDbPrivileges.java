/*
 * Copyright (c) 2026 Memorial Sloan Kettering Cancer Center.
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package org.mskcc.cbio.portal.util;

import java.util.ArrayList;
import java.util.Deque;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Pattern;
import java.util.regex.Matcher;
import java.util.Set;
import org.mskcc.cbio.portal.dao.DaoException;
import org.mskcc.cbio.portal.dao.DaoDbServerSessionInfo;
import org.mskcc.cbio.portal.util.ProgressMonitor;

/**
 * Retrieve and check the privilege grants given to the current user. Actual privileges for the current user
 * are analyzed with regard to a set of recommended privileges. Any uncovered recommendations are output
 * in a warning message.
 */
public class CheckDbPrivileges {

    /**
     * Represents a Recommended Privilege. Data member 'subsumingPrivileges' holds a list of privileges which
     * (if held) subsume the recommended privilege. For example, a user who holds 'GRANT SHOW ON *.*' also inherently
     * holds 'GRANT SHOW TABLE ON *.*' because that is subsumed by 'GRANT SHOW ON *.*'.
     *
    */
    public static class RecommendedPrivilege {
        String name;
        List<String> subsumingPrivileges;
        String onDatabase;
        String onTable;
        Integer equivalenceGroup;

        public static String CURRENT_DATABASE_SPECIAL_VALUE = "currentDatabase()";
        public static Integer EQUIVALENCE_GROUP_NONE = -1;
        public static Integer EQUIVALENCE_GROUP_REMOTE = 1;

        public RecommendedPrivilege(
                String name,
                List<String> subsumingPrivileges,
                String onDatabase,
                String onTable,
                Integer equivalenceGroup) {
            this.name = name;
            this.subsumingPrivileges = new ArrayList<>(subsumingPrivileges);
            this.onDatabase = onDatabase;
            this.onTable = onTable;
            this.equivalenceGroup = equivalenceGroup;
        }

        public RecommendedPrivilege(
                String name,
                List<String> subsumingPrivileges,
                String onDatabase,
                String onTable) {
            this(name, subsumingPrivileges, onDatabase, onTable, RecommendedPrivilege.EQUIVALENCE_GROUP_NONE);
        }

        public boolean hasAnEquivalenceGroup() {
            if (equivalenceGroup == null) {
                return false;
            }
            if (equivalenceGroup == RecommendedPrivilege.EQUIVALENCE_GROUP_NONE) {
                return false;
            }
            return true;
        }
    };

    private DaoDbServerSessionInfo daoDbServerSessionInfo; // dependency (initialized on constrution to allow mocking)

    /**
     * A set of all Recommended privileges. Because some privileges are version and
     * server_setting specific, those recommnedations are represented as a linked equivalence group across
     * version variations. Satisfaction of any one of the recommended privileges in the group satisfies
     * the entire group.
     */
    private Set<RecommendedPrivilege> recommendedPrivilegeSet = new HashSet<>();

    /**
     * Singleton pattern : all functionality accessed through a singleton object rather than
     * static class methods. This enables convenient mocking for unit testing.
     */
    private static CheckDbPrivileges checkDbPrivileges = null;

    /**
     * Gets Global Singleton Instance.
     *
     * @return CheckDbPrivileges Singleton object.
     */
    public static CheckDbPrivileges getInstance() {
        if (checkDbPrivileges == null) {
            // create singleton
            checkDbPrivileges = new CheckDbPrivileges(DaoDbServerSessionInfo.getInstance());
        }
        return checkDbPrivileges;
    }

    /**
     * Constructor, which initializes our dependency and initializes recommendation list
     */
    public CheckDbPrivileges(DaoDbServerSessionInfo daoDbServerSessionInfo) {
        this.daoDbServerSessionInfo = daoDbServerSessionInfo;
        this.initializeRecommendedPrivileges();
    }

    /**
     * Constructor - prohibit uninitialized construction
     */
    private CheckDbPrivileges() {
    }

    private void initializeRecommendedPrivileges() {
        this.recommendedPrivilegeSet.add(new CheckDbPrivileges.RecommendedPrivilege( // SHOW TABLES ON current_database.*
                "SHOW TABLES",
                new ArrayList<String>(List.of("ALL", "SHOW")),
                RecommendedPrivilege.CURRENT_DATABASE_SPECIAL_VALUE, "*"));
        this.recommendedPrivilegeSet.add(new CheckDbPrivileges.RecommendedPrivilege( // SHOW COLUMNS ON current_database.*
                "SHOW COLUMNS",
                new ArrayList<String>(List.of("ALL", "SHOW")),
                RecommendedPrivilege.CURRENT_DATABASE_SPECIAL_VALUE, "*"));
        this.recommendedPrivilegeSet.add(new CheckDbPrivileges.RecommendedPrivilege( // SELECT ON current_database.*
                "SELECT",
                new ArrayList<String>(List.of("ALL")),
                RecommendedPrivilege.CURRENT_DATABASE_SPECIAL_VALUE, "*"));
        this.recommendedPrivilegeSet.add(new CheckDbPrivileges.RecommendedPrivilege( // INSERT ON current_database.*
                "INSERT",
                new ArrayList<String>(List.of("ALL")),
                RecommendedPrivilege.CURRENT_DATABASE_SPECIAL_VALUE, "*"));
        this.recommendedPrivilegeSet.add(new CheckDbPrivileges.RecommendedPrivilege( // ALTER ON current_database.* (for ALTER TABLE, ALTER DELETE, ALTER UPDATE)
                "ALTER",
                new ArrayList<String>(List.of("ALL")),
                RecommendedPrivilege.CURRENT_DATABASE_SPECIAL_VALUE, "*"));
        this.recommendedPrivilegeSet.add(new CheckDbPrivileges.RecommendedPrivilege( // CREATE TABLE ON current_database.*
                "CREATE TABLE",
                new ArrayList<String>(List.of("ALL", "CREATE")),
                RecommendedPrivilege.CURRENT_DATABASE_SPECIAL_VALUE, "*"));
        this.recommendedPrivilegeSet.add(new CheckDbPrivileges.RecommendedPrivilege( // CREATE VIEW ON current_database.*
                "CREATE VIEW",
                new ArrayList<String>(List.of("ALL", "CREATE")),
                RecommendedPrivilege.CURRENT_DATABASE_SPECIAL_VALUE, "*"));
        this.recommendedPrivilegeSet.add(new CheckDbPrivileges.RecommendedPrivilege( // DROP TABLE ON current_database.*
                "DROP TABLE",
                new ArrayList<String>(List.of("ALL", "DROP")),
                RecommendedPrivilege.CURRENT_DATABASE_SPECIAL_VALUE, "*"));
        this.recommendedPrivilegeSet.add(new CheckDbPrivileges.RecommendedPrivilege( // DROP VIEW ON current_database.*
                "DROP VIEW",
                new ArrayList<String>(List.of("ALL", "DROP")),
                RecommendedPrivilege.CURRENT_DATABASE_SPECIAL_VALUE, "*"));
        this.recommendedPrivilegeSet.add(new CheckDbPrivileges.RecommendedPrivilege( // TRUNCATE ON current_database.*
                "TRUNCATE",
                new ArrayList<String>(List.of("ALL")),
                RecommendedPrivilege.CURRENT_DATABASE_SPECIAL_VALUE, "*"));
        this.recommendedPrivilegeSet.add(new CheckDbPrivileges.RecommendedPrivilege( // OPTIMIZE ON current_database.*
                "OPTIMIZE",
                new ArrayList<String>(List.of("ALL")),
                RecommendedPrivilege.CURRENT_DATABASE_SPECIAL_VALUE, "*"));
        this.recommendedPrivilegeSet.add(new CheckDbPrivileges.RecommendedPrivilege( // SHOW COLUMNS ON system.tables
                "SHOW COLUMNS",
                new ArrayList<String>(List.of("ALL", "SHOW")),
                "system", "tables"));
        this.recommendedPrivilegeSet.add(new CheckDbPrivileges.RecommendedPrivilege( // SHOW COLUMNS ON system.parts
                "SHOW COLUMNS",
                new ArrayList<String>(List.of("ALL", "SHOW")),
                "system", "parts"));
        this.recommendedPrivilegeSet.add(new CheckDbPrivileges.RecommendedPrivilege( // SHOW COLUMNS ON system.mutations
                "SHOW COLUMNS",
                new ArrayList<String>(List.of("ALL", "SHOW")),
                "system", "mutations"));
        this.recommendedPrivilegeSet.add(new CheckDbPrivileges.RecommendedPrivilege( // SHOW COLUMNS ON system.one
                "SHOW COLUMNS",
                new ArrayList<String>(List.of("ALL", "SHOW")),
                "system", "one"));
        this.recommendedPrivilegeSet.add(new CheckDbPrivileges.RecommendedPrivilege( // SELECT ON system.tables
                "SELECT",
                new ArrayList<String>(List.of("ALL")),
                "system", "tables"));
        this.recommendedPrivilegeSet.add(new CheckDbPrivileges.RecommendedPrivilege( // SELECT ON system.parts
                "SELECT",
                new ArrayList<String>(List.of("ALL")),
                "system", "parts"));
        this.recommendedPrivilegeSet.add(new CheckDbPrivileges.RecommendedPrivilege( // SELECT ON system.mutations
                "SELECT",
                new ArrayList<String>(List.of("ALL")),
                "system", "mutations"));
        this.recommendedPrivilegeSet.add(new CheckDbPrivileges.RecommendedPrivilege( // SELECT ON system.one
                "SELECT",
                new ArrayList<String>(List.of("ALL")),
                "system", "one"));
        this.recommendedPrivilegeSet.add(new CheckDbPrivileges.RecommendedPrivilege( // REMOTE ON *.*
                "REMOTE",
                new ArrayList<String>(List.of("SOURCES")),
                "*", "*",
                RecommendedPrivilege.EQUIVALENCE_GROUP_REMOTE));
        this.recommendedPrivilegeSet.add(new CheckDbPrivileges.RecommendedPrivilege( // READ ON REMOTE
                "READ",
                new ArrayList<String>(),
                "REMOTE", "",
                RecommendedPrivilege.EQUIVALENCE_GROUP_REMOTE));
        this.recommendedPrivilegeSet.add(new CheckDbPrivileges.RecommendedPrivilege( // READ ON REMOTE (subsubsumed by READ ON SOURCES)
                "READ",
                new ArrayList<String>(),
                "SOURCES", "",
                RecommendedPrivilege.EQUIVALENCE_GROUP_REMOTE));
    }

    /**
     * find all roles which the current user has been granted. All grants to current user
     * are examined, normal grants are ignored and role grants are noted. For every role
     * grant, the grants for that role are also retrieved and added to the list of grants
     * to be examined. To prevent infinite looping if "cyclical role grants" are present,
     * roles are only expanded once.
     */
    private List<String> apparentGrantedRoles(List<String> privilegesList) {
        Deque<String> unexpandedRawPrivileges = new LinkedList<String>(privilegesList);
        Set<String> expandedRoles = new HashSet<String>();
        if (privilegesList == null || privilegesList.size() == 0) {
            return new ArrayList<String>();
        }
        Pattern nonRolePrivilegePattern = Pattern.compile("^(.*)\\s\\s*[Oo][Nn]\\s\\s*(\\S\\S*)\\s\\s*[Tt][Oo]\\s.*$");
        Pattern rolePrivilegePattern = Pattern.compile("^\\s*[Gg][Rr][Aa][Nn][Tt]\\s\\s*(.*)\\s\\s*[Tt][Oo]\\s.*$");
        while (!unexpandedRawPrivileges.isEmpty()) {
            String privilegeItem = unexpandedRawPrivileges.pop();
            Matcher nonRolePrivilegeMatcher = nonRolePrivilegePattern.matcher(privilegeItem);
            Matcher rolePrivilegeMatcher = rolePrivilegePattern.matcher(privilegeItem);
            if (nonRolePrivilegeMatcher.matches() || !rolePrivilegeMatcher.matches()) {
                continue; // ignore all non-Role grants
            }
            String[] splitRoles = rolePrivilegeMatcher.group(1).split(",");
            for (int i = 0; i < splitRoles.length; i = i + 1) {
                String splitRole = splitRoles[i].strip();
                if (expandedRoles.contains(splitRole)) {
                    continue; // ignore already expanded roles (possible due to cyclical reference)
                }
                try {
                    // expand
                    List<String> privilegesForRole = daoDbServerSessionInfo.getPrivilegesForRole(splitRole);
                    unexpandedRawPrivileges.addAll(privilegesForRole);
                } catch (DaoException e) {
                    // failures to retrieve role grants do not cause halt - we continue to try to verify recommended privileges
                }
                expandedRoles.add(splitRole);
            }
        }
        return new ArrayList<String>(expandedRoles);
    }

    /**
     * expand the basic list of grants for the current user to include the grants for
     * all roles which the user has access to. Because the grants per role have been
     * expanded into the list of grants, the role grants themselves are filtered out
     * (including any transitive role grants).
     */
    private List<String> expandGrantedRolePrivileges(List<String> privilegesForCurrentUser) {
        List<String> expandedPrivileges = new ArrayList<String>();
        // filter out role grants
        Pattern nonRolePrivilegePattern = Pattern.compile("^(.*)\\s\\s*[Oo][Nn]\\s\\s*(\\S\\S*)\\s\\s*[Tt][Oo]\\s.*$");
        Pattern rolePrivilegePattern = Pattern.compile("^\\s*[Gg][Rr][Aa][Nn][Tt]\\s\\s*(.*)\\s\\s*[Tt][Oo]\\s.*$");
        for (String privilege : privilegesForCurrentUser) {
            Matcher nonRolePrivilegeMatcher = nonRolePrivilegePattern.matcher(privilege);
            Matcher rolePrivilegeMatcher = rolePrivilegePattern.matcher(privilege);
            if (rolePrivilegeMatcher.matches() && !nonRolePrivilegeMatcher.matches() ) {
                continue; // ignore all role grants
            }
            expandedPrivileges.add(privilege);
        }
        // add in non-role grants for apparent roles
        List<String> apparentRoles = apparentGrantedRoles(privilegesForCurrentUser);
        for (String apparentRole : apparentRoles) {
            try {
                List<String> privilegesForRole = daoDbServerSessionInfo.getPrivilegesForRole(apparentRole);
                for (String privilege : privilegesForRole) {
                    Matcher nonRolePrivilegeMatcher = nonRolePrivilegePattern.matcher(privilege);
                    Matcher rolePrivilegeMatcher = rolePrivilegePattern.matcher(privilege);
                    if (nonRolePrivilegeMatcher.matches() || !rolePrivilegeMatcher.matches()) {
                        expandedPrivileges.add(privilege);
                    }
                }
            } catch (DaoException e) {
                // failures to retrieve role grants do not cause halt - we continue to try to verify recommended privileges
            }
        }
        return expandedPrivileges;
    }

    /**
     * break comma separated grants into individual (single) privilege grants. This function also
     * silently drops/ignores any unparsable simple direct privilege grants (role grants should have
     * already been filtered from the input, but would also be filtered here if still present)
     */
    private List<String> splitCombinedPrivilegeGrantStrings(List<String> combinedPrivilegesForCurrentUser) {
        List<String> splitPrivileges = new ArrayList<String>();
        if (combinedPrivilegesForCurrentUser == null || combinedPrivilegesForCurrentUser.size() == 0) {
            return splitPrivileges;
        }
        Pattern combinedPrivilegePattern = Pattern.compile("^(.*)\\s\\s*[Oo][Nn]\\s\\s*(\\S\\S*)\\s\\s*[Tt][Oo]\\s.*$");
        for (String combinedPrivilege : combinedPrivilegesForCurrentUser) {
            if (combinedPrivilege == null || combinedPrivilege.strip().isEmpty()) {
                continue;
            }
            String[] parts = combinedPrivilege.split(",");
            parts[0] = parts[0].replaceFirst("^\\s*[Gg][Rr][Aa][Nn][Tt]\\s*", "");
            Matcher combinedPrivilegeMatcher = combinedPrivilegePattern.matcher(parts[parts.length - 1]);
            if (!combinedPrivilegeMatcher.matches()) {
                continue;
            }
            parts[parts.length - 1] = combinedPrivilegeMatcher.group(1);
            String onClause = String.format(" ON %s", combinedPrivilegeMatcher.group(2));
            // add the "on clause" to all of the split privileges, and accumulate
            for (int i = 0; i < parts.length; i = i + 1) {
                splitPrivileges.add(parts[i].strip() + onClause);
            }
        }
        return splitPrivileges;
    }

    private String collapseWhitespace(String s) {
        return s.replaceAll("\\s\\s*", " ");
    }

    private boolean actualGrantMatchesOrSubsumesRecommendation(String actualGrant, CheckDbPrivileges.RecommendedPrivilege recommended) {
        if (actualGrant.equals(recommended.name)) {
            return true;
        }
        for (String subsumer : recommended.subsumingPrivileges) {
            if (actualGrant.equals(subsumer)) {
                return true;
            }
        }
        return false;
    }

    private boolean actualDatabaseCoversRecommendedDatabase(String actualDatabase, CheckDbPrivileges.RecommendedPrivilege recommended, String currentDatabase) {
        if (actualDatabase.equals("*")) {
            return true; // everything is covered by this actual rule
        }
        if (recommended.onDatabase.equals(RecommendedPrivilege.CURRENT_DATABASE_SPECIAL_VALUE)) {
            return actualDatabase.equals(currentDatabase);
        } else {
            return actualDatabase.equals(recommended.onDatabase);
        }
    }

    private boolean actualTableCoversRecommendedTable(String actualTable, CheckDbPrivileges.RecommendedPrivilege recommended) {
        if (actualTable.equals("*")) {
            return true; // everything is covered by this actual rule
        }
        return actualTable.equals(recommended.onTable);
    }

    /**
     * determine whether a particular actual privilege grant satisfies a particular privilege recommendation.
     */
    private boolean actualPrivilegeSatisfiesRecommendation(String privilegeString, CheckDbPrivileges.RecommendedPrivilege recommended, String currentDatabase) {
        int onPosition = privilegeString.lastIndexOf(" ON ");
        if (onPosition == -1) {
            return false;
        }
        String actualGrant = collapseWhitespace(privilegeString.substring(0, onPosition));
        String actualDatabaseAndTable = privilegeString.substring(onPosition + 4);
        String actualDatabase = null;
        String actualTable = null;
        int dotPosition = actualDatabaseAndTable.lastIndexOf(".");
        if (dotPosition == -1) {
            // no table specified
            actualDatabase = actualDatabaseAndTable.strip();
            actualTable = "";
        } else {
            actualDatabase = actualDatabaseAndTable.substring(0, dotPosition).strip();
            actualTable = actualDatabaseAndTable.substring(dotPosition + 1).strip();
        }
        boolean privilegeMatchedOrSubsumed = actualGrantMatchesOrSubsumesRecommendation(actualGrant.toUpperCase(), recommended);
        boolean privilegeDatabaseCovered = actualDatabaseCoversRecommendedDatabase(actualDatabase, recommended, currentDatabase);
        boolean privilegeTableCovered = actualTableCoversRecommendedTable(actualTable, recommended);
        return privilegeMatchedOrSubsumed && privilegeDatabaseCovered && privilegeTableCovered;
    }

    private void removeAllEquivalentRecommendations(
            Set<RecommendedPrivilege> unsatisfiedRecommendations,
            Integer equivalenceGroup) {
        List<RecommendedPrivilege> equivalentMembers = new ArrayList<>();
        for (CheckDbPrivileges.RecommendedPrivilege member : unsatisfiedRecommendations) {
            if (member.equivalenceGroup == equivalenceGroup) {
                equivalentMembers.add(member);
            }
        }
        for (RecommendedPrivilege member : equivalentMembers) {
            unsatisfiedRecommendations.remove(member);
        }
    }

    /**
     * Obtains the actual grants for the current user and prints warnings if any recommended privilege is not granted.
     * Implemented by iterating actual grants (after splitting them into individual grants) and checking each one
     * against all (not-yet) satisfied recommendations. If any recommendation(s) is satisfied by the checked acutal
     * grant, the recommendation(s) is removed from the unsatisfied list. After all actual grants have been checked
     * a warning will be printed if any recommendations remain as unsatisfied.
     */
    public void logWarningIfRecommendedPrivilegeIsAbsent() {
        try {
            String currentDatabase = daoDbServerSessionInfo.getDatabaseInUse();
            String currentUser = daoDbServerSessionInfo.getDatabaseCurrentUser();
            List<String> privilegesForCurrentUser = daoDbServerSessionInfo.getPrivilegesForCurrentUser();
            List<String> roleExpandedPrivilegesForCurrentUser = expandGrantedRolePrivileges(privilegesForCurrentUser);
            List<String> singlePrivilegesForCurrentUser = splitCombinedPrivilegeGrantStrings(roleExpandedPrivilegesForCurrentUser);
            Set<CheckDbPrivileges.RecommendedPrivilege> unsatisfiedRecommendations = new HashSet(recommendedPrivilegeSet);
            for (String p : singlePrivilegesForCurrentUser) {
                Set <CheckDbPrivileges.RecommendedPrivilege> satisfiedRecommendations = new HashSet<>();
                for (CheckDbPrivileges.RecommendedPrivilege recommended : unsatisfiedRecommendations) {
                    if (actualPrivilegeSatisfiesRecommendation(p, recommended, currentDatabase)) {
                        satisfiedRecommendations.add(recommended);
                    }
                }
                for (CheckDbPrivileges.RecommendedPrivilege satisfied : satisfiedRecommendations) {
                    if (satisfied.hasAnEquivalenceGroup()) {
                        removeAllEquivalentRecommendations(unsatisfiedRecommendations, satisfied.equivalenceGroup);
                    } else {
                        unsatisfiedRecommendations.remove(satisfied);
                    }
                }
            }
            if (unsatisfiedRecommendations.size() > 0) {
                ProgressMonitor.setCurrentMessage("warning : recommended database privileges have not been granted; database interactions may fail.");
                ProgressMonitor.setCurrentMessage("          Executing the following SQL statements when connected to the database with a user that");
                ProgressMonitor.setCurrentMessage("          has the 'WITH GRANT' option enabled for all needed privileges may resolve this issue:");
                boolean remotePrivilegeInvolvement = false;
                for (CheckDbPrivileges.RecommendedPrivilege recommended : unsatisfiedRecommendations) {
                    if (recommended.equivalenceGroup == RecommendedPrivilege.EQUIVALENCE_GROUP_REMOTE) {
                        remotePrivilegeInvolvement = true;
                        continue; // Give a single message at the end instead
                    }
                    String onClauseDatabase = recommended.onDatabase;
                    if (onClauseDatabase.equals(RecommendedPrivilege.CURRENT_DATABASE_SPECIAL_VALUE)) {
                        onClauseDatabase = currentDatabase;
                    }
                    String onClause = String.format("%s.%s", onClauseDatabase, recommended.onTable);
                    String grantCommand = String.format("    GRANT %s ON %s TO %s", recommended.name, onClause, currentUser);
                    ProgressMonitor.setCurrentMessage(grantCommand);
                }
                if (remotePrivilegeInvolvement) {
                    String dbServerVersion = daoDbServerSessionInfo.getServerVersion();
                    ProgressMonitor.setCurrentMessage("    Special case:");
                    ProgressMonitor.setCurrentMessage("        Depending on your installation of Clickhouse (which version,");
                    ProgressMonitor.setCurrentMessage("        and which system settings you are using), you are recommended");
                    ProgressMonitor.setCurrentMessage("        to either use 'GRANT READ ON REMOTE TO " + currentUser + "'");
                    ProgressMonitor.setCurrentMessage("        (especially with clickhouse.cloud deployments); or to use");
                    ProgressMonitor.setCurrentMessage("        'GRANT REMOTE ON *.* TO " + currentUser + "' for earlier versions");
                    ProgressMonitor.setCurrentMessage("        of Clickhouse, or those which have not enabled server setting");
                    ProgressMonitor.setCurrentMessage("        'access_control_improvements.enable_read_write_grants'.");
                    ProgressMonitor.setCurrentMessage("        If either GRANT is successfully applied, database updates");
                    ProgressMonitor.setCurrentMessage("        should proceed without privilege problems.");
                    ProgressMonitor.setCurrentMessage("        Your Clickhouse sever version is " + dbServerVersion);
                    ProgressMonitor.setCurrentMessage("        Clickhouse server version 25.7/25.8 introduced and expanded the ");
                    ProgressMonitor.setCurrentMessage("        'GRANT READ ON REMOTE' syntax (initially disabled in");
                    ProgressMonitor.setCurrentMessage("        non-clickhouse.cloud deployments).");
                }
                ProgressMonitor.setCurrentMessage("    Note : if you are running cBioPortal in a configuration which uses multiple databases,");
                ProgressMonitor.setCurrentMessage("           all involved databases should be updated in a similar way.");
            }
        } catch (DaoException e) {
            String msg = "Error : exception occurred during CheckDbPrivileges.logWarningIfRecommendedPrivilegeIsAbsent() : " + e.getMessage();
            ProgressMonitor.setCurrentMessage(msg);
        }
    }

}
