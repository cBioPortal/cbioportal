#! /usr/bin/env python

# ------------------------------------------------------------------------------
# Script which imports portal data.
#
# ------------------------------------------------------------------------------

import os
import sys
import getopt
import MySQLdb
import xml.etree.ElementTree as ET
from cbioportal_common import *

# ------------------------------------------------------------------------------
# globals

# commands
IMPORT_CANCER_TYPE = "import-cancer-type"
IMPORT_STUDY = "import-study"
REMOVE_STUDY = "remove-study"
IMPORT_STUDY_DATA = "import-study-data"
IMPORT_CASE_LIST = "import-case-list"

DATABASE_HOST = 'db.host'
DATABASE_NAME = 'db.portal_db_name'
DATABASE_USER = 'db.user'
DATABASE_PW = 'db.password'
VERSION_TABLE = 'info'
VERSION_FIELD = 'DB_SCHEMA_VERSION'

COMMANDS = [IMPORT_CANCER_TYPE, IMPORT_STUDY, REMOVE_STUDY, IMPORT_STUDY_DATA, IMPORT_CASE_LIST]

PORTAL_HOME = "PORTAL_HOME"

POM_FILENAME = 'pom.xml'
DB_VERSION = 'db.version'

class PortalProperties(object):
    
    def __init__(self, database_host, database_name, database_user, database_pw):
        self.database_host = database_host
        self.database_name = database_name
        self.database_user = database_user
        self.database_pw = database_pw

# ------------------------------------------------------------------------------
# sub-routines

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

def import_cancer_type(jvm_args, meta_filename):
	args = jvm_args.split(' ')
	args.append(IMPORT_CANCER_TYPE_CLASS);
	args.append(meta_filename)
	args.append("false") # don't clobber existing table
	run_java(*args)

def import_study(jvm_args, meta_filename):
	args = jvm_args.split(' ')
	args.append(IMPORT_STUDY_CLASS);
	args.append(meta_filename)
	run_java(*args)

def remove_study(jvm_args, meta_filename):
	args = jvm_args.split(' ')
	args.append(REMOVE_STUDY_CLASS);
	metastudy_properties = get_metastudy_properties(meta_filename)
	args.append(metastudy_properties.cancer_study_identifier)
	run_java(*args)
	
def import_study_data(jvm_args, meta_filename, data_filename):
    args = jvm_args.split(' ')
    metafile_properties = get_metafile_properties(meta_filename)
    importer = IMPORTER_CLASSNAME_BY_ALTERATION_TYPE[metafile_properties.genetic_alteration_type]
    args.append(importer)
    if IMPORTER_REQUIRES_METADATA[importer]:
        args.append("--meta")
        args.append(meta_filename)
        args.append("--loadMode")
        args.append("bulkload")
    if metafile_properties.genetic_alteration_type == 'CLINICAL':
        args.append(data_filename)
        args.append(metafile_properties.cancer_study_identifier)
    else:
        args.append("--data")
        args.append(data_filename)
    run_java(*args)

def import_case_list(jvm_args, meta_filename):
	args = jvm_args.split(' ')
	args.append(IMPORT_CASE_LIST_CLASS);
	args.append(meta_filename)
	run_java(*args)

def process_command(jvm_args, command, meta_filename, data_filename):
	if command == IMPORT_CANCER_TYPE:
		import_cancer_type(jvm_args, meta_filename)
	elif command == IMPORT_STUDY:
		import_study(jvm_args, meta_filename)
	elif command == REMOVE_STUDY:
		remove_study(jvm_args, meta_filename)
	elif command == IMPORT_STUDY_DATA:
		import_study_data(jvm_args, meta_filename, data_filename)
	elif command == IMPORT_CASE_LIST:
		import_case_list(jvm_args, meta_filename)

def usage():
    print >> OUTPUT_FILE, ('cbioportalImporter.py --jvm-args (args to jvm) ' +
							'--command [%s] --meta-filename <path to metafile> ' +
							'--data-filename <path to datafile> ' +
                            '--properties-filename <path to properties file> ' % (COMMANDS))

def check_args(command, jvm_args, meta_filename, data_filename):
    if (jvm_args == '' or command not in COMMANDS or meta_filename == ''):
        usage()
        sys.exit(2)
    if (command == IMPORT_STUDY_DATA and data_filename == ''):
        usage()
        sys.exit(2)

def check_files(meta_filename, data_filename):
    # check existence of file
    if len(meta_filename) > 0 and not os.path.exists(meta_filename):
        print >> ERROR_FILE, 'meta-file cannot be found: ' + meta_filename
        sys.exit(2)
    if len(data_filename) > 0 and not os.path.exists(data_filename):
        print >> ERROR_FILE, 'data-file cannot be found: ' + data_filename
        sys.exit(2)

def check_db(portal_properties):
    cursor = get_db_cursor(portal_properties)
    db_version = ""
    if cursor is not None:
        db_version = '.'.join(map(str,get_db_version(cursor))).strip()
    portal_db_version = get_portal_db_version().strip()
    
    if portal_db_version != db_version:
        print >> OUTPUT_FILE, 'This version of the portal is out of sync with the database. You must run the database migration script located at PORTAL_HOME/core/src/main/scripts/migrate_db.py before continuing'
        print >> OUTPUT_FILE, 'Portal Version of DB: ' + portal_db_version
        print >> OUTPUT_FILE, 'DB Version: ' + db_version
        sys.exit()

def get_portal_db_version():
    portal_home = os.environ[PORTAL_HOME]
    pom_filename = os.path.join(portal_home, POM_FILENAME)

    if not os.path.exists(pom_filename):
        print >> ERROR_FILE, 'could not find pom.xml. Ensure that PORTAL_HOME environment variable is set.'
        sys.exit(1)
    
    for event, ele in ET.iterparse(pom_filename):
        if DB_VERSION in ele.tag:
            return ele.text.strip()
        ele.clear()

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

def main():
    # parse command line options
    try:
        opts, args = getopt.getopt(sys.argv[1:], '', ['jvm-args=', 'command=', 'meta-filename=', 'data-filename=', 'properties-filename='])
    except getopt.error, msg:
        print >> ERROR_FILE, msg
        usage()
        sys.exit(2)

    # process the options
    command = ''
    jvm_args = ''
    meta_filename = ''
    data_filename = ''
    properties_filename = ''

    for o, a in opts:
        if o == '--jvm-args':
            jvm_args = a
        elif o == '--command':
            command = a
        elif o == '--meta-filename':
            meta_filename = a
        elif o == '--data-filename':
            data_filename = a
        elif o == '--properties-filename':
            properties_filename = a

    if not os.path.exists(properties_filename):
        print >> ERROR_FILE, 'properties file cannot be found'
        usage()
        sys.exit(2)

    portal_properties = get_portal_properties(properties_filename)
    check_db(portal_properties)
    check_args(command, jvm_args, meta_filename, data_filename)
    check_files(meta_filename, data_filename)
    process_command(jvm_args, command, meta_filename, data_filename)

# ------------------------------------------------------------------------------
# ready to roll

if __name__ == '__main__':
    main()
