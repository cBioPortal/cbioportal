#!/usr/bin/env python3

import os
import re
import sys
import contextlib
import argparse
from collections import OrderedDict

from importer.cbioportal_common import get_database_properties, get_db_cursor

import MySQLdb
from pathlib import Path

# globals
ERROR_FILE = sys.stderr
OUTPUT_FILE = sys.stdout
VERSION_TABLE = 'info'
VERSION_FIELD = 'DB_SCHEMA_VERSION'
ALLOWABLE_GENOME_REFERENCES = ['37', 'hg19', 'GRCh37', '38', 'hg38', 'GRCh38', 'mm10', 'GRCm38']
DEFAULT_GENOME_REFERENCE = 'hg19'
MULTI_REFERENCE_GENOME_SUPPORT_MIGRATION_STEP = (2, 11, 0)
GENERIC_ASSAY_MIGRATION_STEP = (2, 12, 1)
SAMPLE_FK_MIGRATION_STEP = (2, 12, 9)
FUSIONS_VERBOTEN_STEP = (2, 12, 14)


class PortalProperties(object):
    """ Properties object class, just has fields for db conn """

    def __init__(self, database_user, database_pw, database_url):
        self.database_user = database_user
        self.database_pw = database_pw
        self.database_url = database_url


def get_db_version(cursor):
    """ gets the version number of the database """
    # First, see if the version table exists
    version_table_exists = False
    try:
        cursor.execute('select table_name from information_schema.tables')
        for row in cursor.fetchall():
            if VERSION_TABLE == row[0].lower().strip():
                version_table_exists = True
    except MySQLdb.Error as msg:
        print(msg, file=ERROR_FILE)
        return None
    if not version_table_exists:
        return (0, 0, 0)
    # Now query the table for the version number
    try:
        cursor.execute('select ' + VERSION_FIELD + ' from ' + VERSION_TABLE)
        for row in cursor.fetchall():
            version = tuple(map(int, row[0].strip().split('.')))
    except MySQLdb.Error as msg:
        print(msg, file=ERROR_FILE)
        return None
    return version


def is_version_larger(version1, version2):
    """ Checks if version 1 is larger than version 2 """
    if version1[0] > version2[0]:
        return True
    if version2[0] > version1[0]:
        return False
    if version1[1] > version2[1]:
        return True
    if version2[1] > version1[1]:
        return False
    if version1[2] > version2[2]:
        return True
    return False


def is_version_equal(version1, version2):
    """ Checks if version 1 is equal to version 2"""
    return version1[0] == version2[0] and version1[1] == version2[1] and version1[2] == version2[2]


def print_all_check_reference_genome_warnings(warnings, force_migration):
    """ Format warnings for output according to mode, and print to ERROR_FILE """
    space = ' '
    indent = 28 * space
    allowable_reference_genome_string = ','.join(ALLOWABLE_GENOME_REFERENCES)
    clean_up_string = ' Please clean up the mutation_event table and ensure it only contains references to one of the valid reference genomes (%s).' % (
        allowable_reference_genome_string)
    use_default_string = 'the default reference genome (%s) will be used in place of invalid reference genomes and the first encountered reference genome will be used.' % (
        DEFAULT_GENOME_REFERENCE)
    use_force_string = 'OR use the "--force" option to override this warning, then %s' % (use_default_string)
    forcing_string = '--force option in effect : %s' % (use_default_string)
    for warning in warnings:
        if force_migration:
            print('%s%s\n%s%s\n' % (indent, warning, indent, forcing_string), file=ERROR_FILE)
        else:
            print('%s%s%s\n%s%s\n' % (indent, warning, clean_up_string, indent, use_force_string), file=ERROR_FILE)


def validate_reference_genome_values_for_study(warnings, ncbi_to_count, study):
    """ check if there are unrecognized or varied ncbi_build values for the study, add to warnings if problems are found """
    if len(ncbi_to_count) == 1:
        for retrieved_ncbi_build in ncbi_to_count:  # single iteration
            if retrieved_ncbi_build.upper() not in [x.upper() for x in ALLOWABLE_GENOME_REFERENCES]:
                msg = 'WARNING: Study %s contains mutation_event records with unsupported NCBI_BUILD value %s.' % (
                study, retrieved_ncbi_build)
                warnings.append(msg)
    elif len(ncbi_to_count) > 1:
        msg = 'WARNING: Study %s contains mutation_event records with %s NCBI_BUILD values {ncbi_build:record_count,...} %s.' % (
        study, len(ncbi_to_count), ncbi_to_count)
        warnings.append(msg)


def check_reference_genome(portal_properties, cursor, force_migration):
    """ query database for ncbi_build values, aggregate per study, then validate and report problems """
    print('Checking database contents for reference genome information', file=OUTPUT_FILE)
    """ Retrieve reference genomes from database """
    warnings = []
    try:
        sql_statement = """
                           select NCBI_BUILD, count(NCBI_BUILD), CANCER_STUDY_IDENTIFIER
                           from mutation_event
                           join mutation on mutation.MUTATION_EVENT_ID = mutation_event.MUTATION_EVENT_ID
                           join genetic_profile on genetic_profile.GENETIC_PROFILE_ID = mutation.GENETIC_PROFILE_ID
                           join cancer_study on cancer_study.CANCER_STUDY_ID = genetic_profile.CANCER_STUDY_ID
                           group by CANCER_STUDY_IDENTIFIER, NCBI_BUILD
                       """
        cursor.execute(sql_statement)
        study_to_ncbi_to_count = {}  # {cancer_study_identifier : {ncbi_build  : record_count}}
        for row in cursor.fetchall():
            retrieved_ncbi_build, ref_count, study = row
            if study in study_to_ncbi_to_count:
                study_to_ncbi_to_count[study][retrieved_ncbi_build] = ref_count
            else:
                study_to_ncbi_to_count[study] = {retrieved_ncbi_build: ref_count}
        for study in study_to_ncbi_to_count:
            validate_reference_genome_values_for_study(warnings, study_to_ncbi_to_count[study], study)
    except MySQLdb.Error as msg:
        print(msg, file=ERROR_FILE)
        sys.exit(1)
    if warnings:
        print_all_check_reference_genome_warnings(warnings, force_migration)
        if not force_migration:
            sys.exit(1)


def check_and_exit_if_fusions(cursor):
    try:
        cursor.execute(
            """
                SELECT COUNT(*)
                FROM mutation_event
                WHERE MUTATION_TYPE = "Fusion";
            """)
        fusion_count = cursor.fetchone()
        if (fusion_count[0] >= 1):
            print(
                'Found %i records in the mutation_event table where the mutation_type was "Fusion". The latest database schema does not allow records in the mutation table where mutation_type is set to "Fusion". Studies linked to existing records of this type should be deleted in order to migrate to DB version 2.12.14' % (
                    fusion_count), file=ERROR_FILE)
            # get the list of studies that need to be cleaned up
            cursor.execute(
                """
                    SELECT cancer_study.CANCER_STUDY_IDENTIFIER, COUNT(mutation.MUTATION_EVENT_ID)
                    FROM cancer_study,
                        genetic_profile
                    LEFT JOIN mutation ON genetic_profile.GENETIC_PROFILE_ID = mutation.GENETIC_PROFILE_ID
                    LEFT JOIN mutation_event ON mutation.MUTATION_EVENT_ID = mutation_event.MUTATION_EVENT_ID
                    WHERE 
                        genetic_profile.CANCER_STUDY_ID = cancer_study.CANCER_STUDY_ID
                        AND mutation_event.MUTATION_TYPE = "Fusion"
                    GROUP BY cancer_study.CANCER_STUDY_IDENTIFIER
                    HAVING count(mutation.MUTATION_EVENT_ID) > 0
                """)
            rows = cursor.fetchall()
            print("The following studies have fusions in the mutation_event table:", file=ERROR_FILE)
            for row in rows:
                print("\t%s" % (row[0]), file=ERROR_FILE)
            sys.exit(1)

    except MySQLdb.Error as msg:
        print(msg, file=ERROR_FILE)
        sys.exit(1)


# TODO: remove this after we update mysql version
def check_and_remove_invalid_foreign_keys(cursor):
    try:
        # if genetic_alteration_ibfk_2 exists
        cursor.execute(
            """
                SELECT *
                FROM INFORMATION_SCHEMA.TABLE_CONSTRAINTS
                    WHERE CONSTRAINT_TYPE = 'FOREIGN KEY'
                    AND TABLE_SCHEMA = DATABASE()
                    AND CONSTRAINT_NAME = 'genetic_alteration_ibfk_2'
            """)
        rows = cursor.fetchall()
        if (len(rows) >= 1):
            # if genetic_alteration_fk_2 also exists, delete it
            cursor.execute(
                """
                    SELECT *
                    FROM INFORMATION_SCHEMA.TABLE_CONSTRAINTS
                        WHERE CONSTRAINT_TYPE = 'FOREIGN KEY'
                        AND TABLE_SCHEMA = DATABASE()
                        AND CONSTRAINT_NAME = 'genetic_alteration_fk_2'
                """)
            rows = cursor.fetchall()
            if (len(rows) >= 1):
                print('Invalid foreign key found.', file=OUTPUT_FILE)
                cursor.execute(
                    """
                        ALTER TABLE `genetic_alteration` DROP FOREIGN KEY genetic_alteration_fk_2;
                    """)
                print('Invalid foreign key has been deleted.', file=OUTPUT_FILE)
    except MySQLdb.Error as msg:
        print(msg, file=ERROR_FILE)
        sys.exit(1)


def check_and_remove_type_of_cancer_id_foreign_key(cursor):
    """The TYPE_OF_CANCER_ID foreign key in the sample table can be either sample_ibfk_1 or sample_ibfk_2. Figure out which one it is and remove it"""
    try:
        # if sample_ibfk_1 exists
        cursor.execute(
            """
                SELECT *
                FROM INFORMATION_SCHEMA.REFERENTIAL_CONSTRAINTS
                    WHERE CONSTRAINT_SCHEMA = DATABASE()
                    AND TABLE_NAME = 'sample'
                    AND REFERENCED_TABLE_NAME = 'type_of_cancer'
                    AND CONSTRAINT_NAME = 'sample_ibfk_1'
            """)
        rows = cursor.fetchall()
        if (len(rows) >= 1):
            print(
                'sample_ibfk_1 is the foreign key in table sample for type_of_cancer_id column in table type_of_cancer.',
                file=OUTPUT_FILE)
            cursor.execute(
                """
                    ALTER TABLE `sample` DROP FOREIGN KEY sample_ibfk_1;
                """)
            print('sample_ibfk_1 foreign key has been deleted.', file=OUTPUT_FILE)
        # if sample_ibfk_2 exists
        cursor.execute(
            """
                SELECT *
                FROM INFORMATION_SCHEMA.REFERENTIAL_CONSTRAINTS
                    WHERE CONSTRAINT_SCHEMA = DATABASE()
                    AND TABLE_NAME = 'sample'
                    AND REFERENCED_TABLE_NAME = 'type_of_cancer'
                    AND CONSTRAINT_NAME = 'sample_ibfk_2'
            """)
        rows = cursor.fetchall()
        if (len(rows) >= 1):
            print(
                'sample_ibfk_2 is the foreign key in table sample for type_of_cancer_id column in table type_of_cancer.',
                file=OUTPUT_FILE)
            cursor.execute(
                """
                    ALTER TABLE `sample` DROP FOREIGN KEY sample_ibfk_2;
                """)
            print('sample_ibfk_2 foreign key has been deleted.', file=OUTPUT_FILE)
    except MySQLdb.Error as msg:
        print(msg, file=ERROR_FILE)
        sys.exit(1)


def strip_trailing_comment_from_line(line):
    line_parts = re.split("--\s", line)
    return line_parts[0]


def run_migration(db_version, sql_filename, connection, cursor, no_transaction, stop_at_version=None):
    """
        Goes through the sql and runs lines based on the version numbers. SQL version should be stated as follows:

        ##version: 1.0.0
        INSERT INTO ...

        ##version: 1.1.0
        CREATE TABLE ...
    """
    sql_file = open(sql_filename, 'r')
    sql_version = (0, 0, 0)
    run_line = False
    statements = OrderedDict()
    statement = ''
    for line in sql_file:
        if line.startswith('##'):
            sql_version = tuple(map(int, line.split(':')[1].strip().split('.')))
            # stop at the version specified
            if stop_at_version is not None and is_version_equal(sql_version, stop_at_version):
                break
            else:
                run_line = is_version_larger(sql_version, db_version)
                continue
        # skip blank lines
        if len(line.strip()) < 1:
            continue
        # skip comments
        if line.startswith('#'):
            continue
        # skip sql comments
        if line.startswith('--') and len(line) > 2 and line[2].isspace():
            continue
        # only execute sql line if the last version seen in the file is greater than the db_version
        if run_line:
            line = line.strip()
            simplified_line = strip_trailing_comment_from_line(line)
            statement = statement + ' ' + simplified_line
            if simplified_line.endswith(';'):
                if sql_version not in statements:
                    statements[sql_version] = [statement]
                else:
                    statements[sql_version].append(statement)
                statement = ''
    if len(statements) > 0:
        run_statements(statements, connection, cursor, no_transaction)
    else:
        print('Everything up to date, nothing to migrate.', file=OUTPUT_FILE)


def run_statements(statements, connection, cursor, no_transaction):
    try:
        if no_transaction:
            cursor.execute('SET autocommit=1;')
        else:
            cursor.execute('SET autocommit=0;')
    except MySQLdb.Error as msg:
        print(msg, file=ERROR_FILE)
        sys.exit(1)
    for version, statement_list in statements.items():
        print(
            'Running statements for version: %s' % ('.'.join(map(str, version))),
            file=OUTPUT_FILE)
        for statement in statement_list:
            print(
                '\tExecuting statement: %s' % (statement.strip()),
                file=OUTPUT_FILE)
            try:
                cursor.execute(statement.strip())
            except MySQLdb.Error as msg:
                print(msg, file=ERROR_FILE)
                sys.exit(1)
        connection.commit()


def warn_user():
    """Warn the user to back up their database before the script runs."""
    response = input(
        'WARNING: This script will alter your database! Be sure to back up your data before running.\n'
        'Continue running DB migration? (y/n) '
    ).strip()
    while response.lower() != 'y' and response.lower() != 'n':
        response = input(
            'Did not recognize response.\nContinue running DB migration? (y/n) '
        ).strip()
    if response.lower() == 'n':
        sys.exit()


def usage():
    print(
        'migrate_db.py --properties-file [portal properties file] --sql [sql migration file]',
        file=OUTPUT_FILE)


def main():
    """ main function to run mysql migration """
    parser = argparse.ArgumentParser(description='cBioPortal DB migration script')
    parser.add_argument('-y', '--suppress_confirmation', default=False, action='store_true')
    parser.add_argument('-p', '--properties-file', type=str, required=False,
                        help='Path to portal.properties file (default: locate it '
                             'relative to the script)')
    parser.add_argument('-s', '--sql', type=str, required=False,
                        help='Path to official migration.sql script. (default: locate it '
                             'relative to the script)')
    parser.add_argument('-f', '--force', default=False, action='store_true', help='Force to run database migration')
    parser.add_argument('--no-transaction', default=False, action='store_true', help="""
        Do not run migration in a single transaction. Only use this when you known what you are doing!!!
    """)
    parser = parser.parse_args()

    properties_filename = parser.properties_file
    if properties_filename is None:
        # get the directory name of the currently running script,
        # resolving any symlinks
        script_dir = Path(__file__).resolve().parent
        # go up from cbioportal/core/src/main/scripts/ to cbioportal/
        src_root = script_dir.parent.parent.parent.parent
        properties_filename = src_root / 'portal.properties'
                
    sql_filename = parser.sql
    if sql_filename is None:
        # get the directory name of the currently running script,
        # resolving any symlinks
        script_dir = Path(__file__).resolve().parent
        # go up from cbioportal/core/src/main/scripts/ to cbioportal/
        src_root = script_dir.parent.parent.parent.parent
        sql_filename = src_root / 'db-scripts/src/main/resources/migration.sql'

    if not os.path.exists(sql_filename):
        print('sql file %s cannot be found' % (sql_filename), file=ERROR_FILE)
        usage()
        sys.exit(2)

    # parse properties file
    portal_properties = get_database_properties(properties_filename)
    if portal_properties is None:
        print('failure reading properties file (%s)' % properties_filename, file=ERROR_FILE)
        sys.exit(1)

    # warn user
    if not parser.suppress_confirmation:
        warn_user()

    # set up - get db cursor
    connection, cursor = get_db_cursor(portal_properties)
    if cursor is None:
        print('failure connecting to sql database', file=ERROR_FILE)
        sys.exit(1)

    # execute - get the database version and run the migration
    with contextlib.closing(connection):
        db_version = get_db_version(cursor)
        if is_version_larger(MULTI_REFERENCE_GENOME_SUPPORT_MIGRATION_STEP, db_version):
            run_migration(db_version, sql_filename, connection, cursor, parser.no_transaction,
                          stop_at_version=MULTI_REFERENCE_GENOME_SUPPORT_MIGRATION_STEP)
            # retrieve reference genomes from database
            check_reference_genome(portal_properties, cursor, parser.force)
            db_version = get_db_version(cursor)
        if is_version_larger(SAMPLE_FK_MIGRATION_STEP, db_version):
            run_migration(db_version, sql_filename, connection, cursor, parser.no_transaction,
                          stop_at_version=SAMPLE_FK_MIGRATION_STEP)
            check_and_remove_type_of_cancer_id_foreign_key(cursor)
            db_version = get_db_version(cursor)
        if is_version_larger(FUSIONS_VERBOTEN_STEP, db_version):
            run_migration(db_version, sql_filename, connection, cursor, parser.no_transaction,
                          stop_at_version=FUSIONS_VERBOTEN_STEP)
            check_and_exit_if_fusions(cursor)
            db_version = get_db_version(cursor)
        run_migration(db_version, sql_filename, connection, cursor, parser.no_transaction)
        # TODO: remove this after we update mysql version
        # check invalid foreign key only when current db version larger or qeuals to GENERIC_ASSAY_MIGRATION_STEP
        if not is_version_larger(GENERIC_ASSAY_MIGRATION_STEP, db_version):
            check_and_remove_invalid_foreign_keys(cursor)
    print('Finished.', file=OUTPUT_FILE)


# do main
if __name__ == '__main__':
    main()
