#!/usr/bin/env python3

# imports

import os
import sys
import contextlib
import argparse
from collections import OrderedDict

import MySQLdb


# globals

ERROR_FILE = sys.stderr
OUTPUT_FILE = sys.stdout

DATABASE_HOST = 'db.host'
DATABASE_NAME = 'db.portal_db_name'
DATABASE_USER = 'db.user'
DATABASE_PW = 'db.password'
VERSION_TABLE = 'info'
VERSION_FIELD = 'DB_SCHEMA_VERSION'

class PortalProperties(object):
    """ Properties object class, just has fields for db conn """

    def __init__(self, database_host, database_name, database_user, database_pw):
        # default port:
        self.database_port = 3306
        # if there is a port added to the host name, split and use this one:
        if ':' in database_host:
            host_and_port = database_host.split(':')
            self.database_host = host_and_port[0]
            if self.database_host.strip() == 'localhost':
                print(
                    "Invalid host config '" + database_host + "' in properties file. If you want to specify a port on local host use '127.0.0.1' instead of 'localhost'",
                    file=ERROR_FILE)
                sys.exit(1)
            self.database_port = int(host_and_port[1])
        else:
            self.database_host = database_host
        self.database_name = database_name
        self.database_user = database_user
        self.database_pw = database_pw

def get_db_cursor(portal_properties):
    """ Establishes a MySQL connection """

    try:
        
        connection = MySQLdb.connect(host=portal_properties.database_host, 
            port = portal_properties.database_port, 
            user = portal_properties.database_user,
            passwd = portal_properties.database_pw,
            db = portal_properties.database_name)
    except MySQLdb.Error as exception:
        print(exception, file=ERROR_FILE)
        port_info = ''
        if portal_properties.database_host.strip() != 'localhost':
            # only add port info if host is != localhost (since with localhost apparently sockets are used and not the given port) TODO - perhaps this applies for all names vs ips?
            port_info = " on port " + str(portal_properties.database_port)
        message = (
            "--> Error connecting to server "
            + portal_properties.database_host
            + port_info)
        print(message, file=ERROR_FILE)
        raise ConnectionError(message) from exception

    if connection is not None:
        return connection, connection.cursor()

def get_portal_properties(properties_filename):
    """ Returns a properties object """
    
    properties = {}
    with open(properties_filename, 'r') as properties_file:
        for line in properties_file:
            line = line.strip()

            # skip line if its blank or a comment
            if len(line) == 0 or line.startswith('#'):
                continue

            try:
                name, value = line.split('=', maxsplit=1)
            except ValueError:
                print(
                    'Skipping invalid entry in property file: ' + line,
                    file=ERROR_FILE)
                continue
            properties[name] = value.strip()

    if (DATABASE_HOST not in properties or len(properties[DATABASE_HOST]) == 0 or
        DATABASE_NAME not in properties or len(properties[DATABASE_NAME]) == 0 or
        DATABASE_USER not in properties or len(properties[DATABASE_USER]) == 0 or
        DATABASE_PW not in properties or len(properties[DATABASE_PW]) == 0):
        print(
            'Missing one or more required properties, please check property file',
            file=ERROR_FILE)
        return None
    
    # return an instance of PortalProperties
    return PortalProperties(properties[DATABASE_HOST],
                            properties[DATABASE_NAME],
                            properties[DATABASE_USER],
                            properties[DATABASE_PW])

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

def run_migration(db_version, sql_filename, connection, cursor):
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
            statement = statement + ' ' + line
            if line.endswith(';'):
                if sql_version not in statements:
                    statements[sql_version] = [statement]
                else:
                    statements[sql_version].append(statement)
                statement = ''
    if len(statements) > 0:
        run_statements(statements, connection, cursor)
    else:
        print('Everything up to date, nothing to migrate.')
    
def run_statements(statements, connection, cursor):
    try:
        cursor.execute('SET autocommit=0;')
    except MySQLdb.Error as msg:
        print(msg, file=ERROR_FILE)
        sys.exit(1)

    for version, statement_list in statements.items():
        print(
            'Running statements for version: ' + '.'.join(map(str, version)),
            file=OUTPUT_FILE)
        for statement in statement_list:
            print(
                '\tExecuting statement: ' + statement.strip(),
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
        'WARNING: This script will alter your database! Be sure to back up your data before running.\nContinue running DB migration? (y/n) '
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
    parser.add_argument('-p', '--properties-file', type=str, required=True,
                        help='Path to portal.properties file')
    parser.add_argument('-s', '--sql', type=str, required=True,
                        help='Path to official migration.sql script.')
    parser = parser.parse_args()

    properties_filename = parser.properties_file
    sql_filename = parser.sql

    # check existence of properties file
    if not os.path.exists(properties_filename):
        print(
            'properties file ' + properties_filename + ' cannot be found',
            file=ERROR_FILE)
        usage()
        sys.exit(2)
    if not os.path.exists(sql_filename):
        print('sql file ' + sql_filename + ' cannot be found', file=ERROR_FILE)
        usage()
        sys.exit(2)

    warn_user()
    
    # set up - get properties and db cursor
    portal_properties = get_portal_properties(properties_filename)
    connection, cursor = get_db_cursor(portal_properties)

    if cursor is None:
        print('failure connecting to sql database', file=ERROR_FILE)
        sys.exit(1)

    # execute - get the database version and run the migration
    with contextlib.closing(connection):
        db_version = get_db_version(cursor)
        run_migration(db_version, sql_filename, connection, cursor)
    print('Finished.')
    

# do main
if __name__ == '__main__':
    main()
