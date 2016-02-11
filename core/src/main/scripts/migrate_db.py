#! /usr/bin/env python

# imports
import os
import sys
import getopt
import MySQLdb

# Globals

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
        self.database_host = database_host
        self.database_name = database_name
        self.database_user = database_user
        self.database_pw = database_pw

def get_db_cursor(portal_properties):
    """ Establishes a MySQL connection """

    try:
        connection = MySQLdb.connect(host=portal_properties.database_host, 
            port = 3306, 
            user = portal_properties.database_user,
            passwd = portal_properties.database_pw,
            db = portal_properties.database_name)
    except MySQLdb.Error, msg:
        print >> ERROR_FILE, msg
        return None

    if connection is not None:
        return connection, connection.cursor()

def get_portal_properties(properties_filename):
    """ Returns a properties object """
    
    properties = {}
    properties_file = open(properties_filename, 'r')

    for line in properties_file:
        line = line.strip()

        # skip line if its blank or a comment
        if len(line) == 0 or line.startswith('#'):
            continue
        
        # store name/value
        property = line.split('=')
        if len(property) != 2:
            print >> ERROR_FILE, 'Skipping invalid entry in proeprty file: ' + line
            continue
        properties[property[0]] = property[1].strip()
    properties_file.close()

    if (DATABASE_HOST not in properties or len(properties[DATABASE_HOST]) == 0 or
        DATABASE_NAME not in properties or len(properties[DATABASE_NAME]) == 0 or
        DATABASE_USER not in properties or len(properties[DATABASE_USER]) == 0 or
        DATABASE_PW not in properties or len(properties[DATABASE_PW]) == 0):
        print >> ERROR_FILE, 'Missing one or more required properties, please check property file'
        return none
    
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
    except MySQLdb.Error, msg:
        print >> ERROR_FILE, msg
        return None
    
    if not version_table_exists:
        return (0,0,0)

    # Now query the table for the version number
    try:
        cursor.execute('select ' + VERSION_FIELD + ' from ' + VERSION_TABLE)
        for row in cursor.fetchall():
            version = tuple(map(int, row[0].strip().split('.')))
    except MySQLdb.Error, msg:
        print >> ERROR_FILE, msg
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
    sql_version = (0,0,0)
    run_line = False
    statements = {}
    statement = ''
    for line in sql_file:
        if line.startswith('##'):
            sql_version = tuple(map(int, line.split(':')[1].strip().split('.')))
            run_line = is_version_larger(sql_version, db_version)
            continue

        # skip blank lines
        if len(line.strip()) < 1 or line.startswith('#'):
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
    run_statements(statements, connection, cursor)
def run_statements(statements, connection, cursor):
    try:
        cursor.execute('SET autocommit=0;')
    except MySQLdb.Error, msg:
        print >> ERROR_FILE, msg
        sys.exit(1)

    for version,statement_list in statements.iteritems():
        print >> OUTPUT_FILE, 'Running statments for version: ' + '.'.join(map(str,version))
        for statement in statement_list:
            print >> OUTPUT_FILE, '\tExecuting statement: ' + statement.strip()
            try:
                cursor.execute(statement.strip())
            except MySQLdb.Error, msg:
                print >> ERROR_FILE, msg
                sys.exit(1)
        connection.commit();

def warn_user():
    """

    warn the user before the script runs, give them a chance to
    back up their database if desired
    """
    response = raw_input('WARNING: This script will alter your database! Be sure to back up your data before running.\nContinue running DB migration? (y/n) ').strip()
    while response is not 'y' and response is not 'n':
        response = raw_input('Did not recognize response.\nContinue running DB migration? (y/n) ').strip()
    if response is 'n':
        sys.exit()

def usage():
    print >> OUTPUT_FILE, 'migrate_db.py --properties-file [portal properties file] --sql [sql migration file]'

def main():
    """ main function to run mysql migration """
    
    warn_user()

    try:
        opts, args = getopt.getopt(sys.argv[1:], '', ['properties-file=', 'sql='])
    except getopt.error, msg:
            print >> ERROR_FILE, msg
            usage()
            sys.exit(2)

    properties_filename = ''
    sql_filename = ''

    for o, a in opts:
        if o == '--properties-file':
            properties_filename = a
        if o == '--sql':
            sql_filename = a

    # check existence of properties file
    if not os.path.exists(properties_filename):
        print >> ERROR_FILE, 'properties file cannot be found'
        usage()
        sys.exit(2)
    if not os.path.exists(sql_filename):
        print >> ERROR_FILE, 'sql file cannot be found'
        usage()
        sys.exit(2)

    # set up - get properties and db cursor
    portal_properties = get_portal_properties(properties_filename)
    connection, cursor = get_db_cursor(portal_properties)

    if cursor is None:
        print >> ERROR_FILE, 'failure connecting to sql database'
        sys.exit(1)

    # execute - get the database version and run the migration
    db_version = get_db_version(cursor)
    run_migration(db_version, sql_filename, connection, cursor)
    connection.close();
    

# do main
if __name__ == '__main__':
    main()
