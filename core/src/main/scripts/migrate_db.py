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
VERSION_TABLE = 'version'

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
        return connection.cursor()

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
            if VERSION_TABLE in row[0].lower():
                version_table_exists = True
    except MySQLdb.Error, msg:
        print >> ERROR_FILE, msg
        return None
    
    if not version_table_exists:
        return (0,0,0)

    # Now query the table for the version number
    try:
        cursor.execute('select version_number from version')
        for row in cursor.fetchall():
            version = tuple(map(int, row[0].strip().split('.')))
    except MySQLdb.Error, msg:
        print >> ERROR_FILE, msg
        return None

    return version

def is_version_larger(version1, version2):
    """ Checks if version 1 is larger than version 2 """

    print version1, version2
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

def run_migration(db_version, sql_filename, cursor):
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

    for line in sql_file:
        if line.startswith('#'):
            sql_version = tuple(map(int, line.split(':')[1].strip().split('.')))
            run_line = is_version_larger(sql_version, db_version)
            continue

        # skip blank lines
        if len(line.strip()) < 1:
            continue
        # only execute sql line if the last version seen in the file is greater than the db_version
        if run_line:
            try:
                cursor.execute(line.strip())
            except MySQLdb.Error, msg:
                print >> ERROR_FILE, msg
                sys.exit(1)
            

def usage():
    print >> OUTPUT_FILE, 'migrate_db.py --properties-file [portal properties file] --sql [sql migration file]'

def main():
    """ main function to run mysql migration """
    
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
    cursor = get_db_cursor(portal_properties)

    if cursor is None:
        print >> ERROR_FILE, 'failure connecting to sql database'
        sys.exit(1)

    # execute - get the database version and run the migration
    db_version = get_db_version(cursor)
    run_migration(db_version, sql_filename, cursor)

# do main
if __name__ == '__main__':
    main()
