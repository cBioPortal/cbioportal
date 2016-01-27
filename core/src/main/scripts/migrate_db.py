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
        # spreadsheet url contains an '=' sign
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
        return "0"

    # Now query the table for the version number
    try:
        cursor.execute('select version_number from version')
        for row in cursor.fetchall():
            version = row[0].lower()
    except MySQLdb.Error, msg:
        print >> ERROR_FILE, msg
        return None

    return version

def run_script(script, cursor):
    """ runs the MySQL script """

    script_file = open(script, 'rU')
    
    try:
        for line in script_file:
            line = line.strip()
            cursor.execute(line)
    except MySQLdb.Error, msg:
        print >>ERROR_FILE, msg
        return None

def run_migration(db_version, sql_scripts, cursor):
    """ checks script version against db version and runs them in order """

    for script in sorted(sql_scripts):
        script_version = '.'.join(os.path.basename(script).split('.')[0:-1])
        print script_version
        if script_version > db_version:
            run_script(script, cursor)

def usage():
    print >> OUTPUT_FILE, 'migrate_db.py --properties-file [portal properties file] --sql-directory [directory of sql scripts]'

def main():
    """ main function to run mysql migration """
    
    try:
        opts, args = getopt.getopt(sys.argv[1:], '', ['properties-file=', 'sql-directory='])
    except getopt.error, msg:
            print >> ERROR_FILE, msg
            usage()
            sys.exit(2)

    properties_filename = ''
    sql_directory = ''

    for o, a in opts:
        if o == '--properties-file':
            properties_filename = a
        if o == '--sql-directory':
            sql_directory = a

    # check existence of properties file
    if not os.path.exists(properties_filename):
        print >> ERROR_FILE, 'properties file cannot be found'
        usage()
        sys.exit(2)
    if not os.path.isdir(sql_directory):
        print >> ERROR_FILE, 'sql directory cannot be found'
        usage()
        sys.exit(2)
   
    sql_scripts = [os.path.join(sql_directory, x) for x in os.listdir(sql_directory) if x.endswith(".sql")]
    
    portal_properties = get_portal_properties(properties_filename)
    cursor = get_db_cursor(portal_properties)
    if cursor is not None:
        db_version = get_db_version(cursor)
    run_migration(db_version, sql_scripts, cursor)
# do main
if __name__ == '__main__':
    main()
