#! /usr/bin/env python

# ------------------------------------------------------------------------------
# Script which updates clinical_attribute table within the cBioPortal database.
# The following properties must be specified in portal.properties:
#
# db.portal_db_name
# db.user
# db.password
# db.host
# google.id
# google.pw
# importer.spreadsheet_service_appname
# importer.spreadsheet
# importer.clinical_attributes worksheet
#
# For each clinical attribute listed in the clinical_attributes google worksheet,
# the script will update the respective record in the clinical_attribute table.
#
# ------------------------------------------------------------------------------

import os
import sys
import getopt
import MySQLdb

import smtplib
import gdata.docs.client
import gdata.docs.service
import gdata.spreadsheet.service

import httplib2
from oauth2client import client
from oauth2client.file import Storage
from oauth2client.client import flow_from_clientsecrets
from oauth2client.tools import run_flow, argparser

# ------------------------------------------------------------------------------
# globals

# some file descriptors
ERROR_FILE = sys.stderr
OUTPUT_FILE = sys.stdout

# fields in portal.properties
CGDS_DATABASE_HOST = 'db.host'
CGDS_DATABASE_NAME = 'db.portal_db_name'
CGDS_DATABASE_USER = 'db.user'
CGDS_DATABASE_PW = 'db.password'
GOOGLE_ID = 'google.id'
GOOGLE_PW = 'google.pw'
IMPORTER_SPREADSHEET = 'importer.spreadsheet'
CLINICAL_ATTRIBUTES_WORKSHEET = 'importer.clinical_attributes_worksheet'
IMPORTER_SPREADSHEET_SERVICE_APPNAME = 'importer.spreadsheet_service_appname'

# column constants on google spreadsheet
CLINICAL_ATTRIBUTES_KEY = 'normalizedcolumnheader'
CLINICAL_ATTRIBUTES_DISPLAY_NAME = 'displayname'
CLINICAL_ATTRIBUTES_DESCRIPTION = 'descriptions'
CLINICAL_ATTRIBUTES_DATATYPE = 'datatype'
CLINICAL_ATTRIBUTES_ATTRIBUTE_TYPE = 'attributetype'
CLINICAL_ATTRIBUTES_PRIORITY = 'priority'

CLINICAL_ATTRIBUTE_TYPE_PATIENT = "PATIENT"

# ------------------------------------------------------------------------------
# class definitions

class PortalProperties(object):
    def __init__(self,
                 cgds_database_host,
                 cgds_database_name, cgds_database_user, cgds_database_pw,
                 google_id, google_pw, google_spreadsheet, google_worksheet, app_name):
        self.cgds_database_host = cgds_database_host
        self.cgds_database_name = cgds_database_name
        self.cgds_database_user = cgds_database_user
        self.cgds_database_pw = cgds_database_pw
        self.google_id = google_id
        self.google_pw = google_pw
        self.google_spreadsheet = google_spreadsheet
        self.google_worksheet = google_worksheet
        self.app_name = app_name

class ClinicalAttribute(object):
    def __init__(self, normalized_column_header, display_name, description, datatype, patient_attribute, priority):
        self.normalized_column_header = normalized_column_header
        self.display_name = display_name
        self.description = description
        self.datatype = datatype
	self.patient_attribute = patient_attribute
	self.priority = priority

# ------------------------------------------------------------------------------
# sub-routines

# ------------------------------------------------------------------------------
# logs into google spreadsheet client

def get_gdata_credentials(secrets, creds, scope, force=False):
    storage = Storage(creds)
    credentials = storage.get()
    if credentials is None or credentials.invalid or force:
      credentials = run_flow(flow_from_clientsecrets(secrets, scope=scope), storage, argparser.parse_args([]))
      
    if credentials.access_token_expired:
        credentials.refresh(httplib2.Http())
        
    return credentials

def google_login(secrets, creds, user, pw, app_name):

	credentials = get_gdata_credentials(secrets, creds, ["https://spreadsheets.google.com/feeds"], False)
	client = gdata.spreadsheet.service.SpreadsheetsService(additional_headers={'Authorization' : 'Bearer %s' % credentials.access_token})

	# google spreadsheet
	client.email = user
	client.password = pw
	client.source = app_name
	client.ProgrammaticLogin()

	return client

# ------------------------------------------------------------------------------
# given a feed & feed name, returns its id
#
def get_feed_id(feed, name):

    to_return = ''

    for entry in feed.entry:
        if entry.title.text.strip() == name:
            id_parts = entry.id.text.split('/')
            to_return = id_parts[len(id_parts) - 1]

    return to_return

# ------------------------------------------------------------------------------
# gets a worksheet feed

def get_worksheet_feed(client, ss, ws):

    ss_id = get_feed_id(client.GetSpreadsheetsFeed(), ss)
    ws_id = get_feed_id(client.GetWorksheetsFeed(ss_id), ws)

    return client.GetListFeed(ss_id, ws_id)


# ------------------------------------------------------------------------------
# get db connection
def get_db_connection(portal_properties):

    # try and create a connection to the db
    try:
        connection = MySQLdb.connect(host=portal_properties.cgds_database_host, port=3306,
                                     user=portal_properties.cgds_database_user,
                                     passwd=portal_properties.cgds_database_pw,
                                     db=portal_properties.cgds_database_name)
    except MySQLdb.Error, msg:
        print >> ERROR_FILE, msg
        return None

    return connection

# ------------------------------------------------------------------------------
# parse portal.properties

def get_portal_properties(portal_properties_filename):

    properties = {}
    portal_properties_file = open(portal_properties_filename, 'r')
    for line in portal_properties_file:
        line = line.strip()
        # skip line if its blank or a comment
        if len(line) == 0 or line.startswith('#'):
            continue
        # store name/value
        property = line.split('=')
        if len(property) != 2:
            print >> ERROR_FILE, 'Skipping invalid entry in property file: ' + line
            continue
        properties[property[0]] = property[1].strip()
    portal_properties_file.close()

    # error check
    if (CGDS_DATABASE_HOST not in properties or len(properties[CGDS_DATABASE_HOST]) == 0 or
        CGDS_DATABASE_NAME not in properties or len(properties[CGDS_DATABASE_NAME]) == 0 or
        CGDS_DATABASE_USER not in properties or len(properties[CGDS_DATABASE_USER]) == 0 or
        CGDS_DATABASE_PW not in properties or len(properties[CGDS_DATABASE_PW]) == 0 or
        GOOGLE_ID not in properties or len(properties[GOOGLE_ID]) == 0 or
        GOOGLE_PW not in properties or len(properties[GOOGLE_PW]) == 0 or
        IMPORTER_SPREADSHEET not in properties or len(properties[IMPORTER_SPREADSHEET]) == 0 or
        CLINICAL_ATTRIBUTES_WORKSHEET not in properties or len(properties[CLINICAL_ATTRIBUTES_WORKSHEET]) == 0 or
        IMPORTER_SPREADSHEET_SERVICE_APPNAME not in properties or len(properties[IMPORTER_SPREADSHEET_SERVICE_APPNAME]) == 0):
        print >> ERROR_FILE, 'Missing one or more required properties, please check property file'
        return None

    # return an instance of PortalProperties
    return PortalProperties(properties[CGDS_DATABASE_HOST],
                            properties[CGDS_DATABASE_NAME],
                            properties[CGDS_DATABASE_USER],
                            properties[CGDS_DATABASE_PW],
                            properties[GOOGLE_ID],
                            properties[GOOGLE_PW],
                            properties[IMPORTER_SPREADSHEET],
                            properties[CLINICAL_ATTRIBUTES_WORKSHEET],
                            properties[IMPORTER_SPREADSHEET_SERVICE_APPNAME])


# ------------------------------------------------------------------------------
# get clinical attributes from clinical_attribute portal database table

def get_db_clinical_attributes(cursor):

    # map that we are returning
    # key is the clinical attribute name and value is a ClinicalAtttribute object
    to_return = {}

    try:
        cursor.execute('select * from clinical_attribute')
        for row in cursor.fetchall():
            to_return[row[0]] = ClinicalAttribute(row[0], row[1], row[2], row[3], row[4], row[5])
    except MySQLdb.Error, msg:
        print >> ERROR_FILE, msg
        return None

    return to_return

# ------------------------------------------------------------------------------
# checks validity of google worksheet record
def valid_worksheet_entry(normalized_column_header, display_name, description, datatype, priority):
	if normalized_column_header == None or len(normalized_column_header) == 0:
		return False
	if display_name == None or len(display_name) == 0:
		return False
	if description == None or len(description) == 0:
		return False
	if datatype == None or len(datatype) == 0:
		return False
	if priority == None or len(priority) == 0:
		return False
	return True

# ------------------------------------------------------------------------------
# get clinical attributes from google worksheet

def get_worksheet_clinical_attributes(worksheet_feed):

    # map that we are returning
    # key is the clinical attribute name (tormalized column header) and value is a ClinicalAttribute object
	to_return = {}

	for entry in worksheet_feed.entry:
		normalized_column_header = entry.custom[CLINICAL_ATTRIBUTES_KEY].text
		display_name = entry.custom[CLINICAL_ATTRIBUTES_DISPLAY_NAME].text
		description = entry.custom[CLINICAL_ATTRIBUTES_DESCRIPTION].text
		datatype = entry.custom[CLINICAL_ATTRIBUTES_DATATYPE].text
		if entry.custom[CLINICAL_ATTRIBUTES_ATTRIBUTE_TYPE].text == CLINICAL_ATTRIBUTE_TYPE_PATIENT:
			patient_attribute = 1
		else:
			patient_attribute = 0
		priority = entry.custom[CLINICAL_ATTRIBUTES_PRIORITY].text
		if valid_worksheet_entry(normalized_column_header, display_name, description, datatype, priority):	
			to_return[normalized_column_header] = ClinicalAttribute(normalized_column_header, display_name, description, datatype, patient_attribute, priority)
		else:
			print >> OUTPUT_FILE, "An attribute from the worksheet is missing a value, skipping: %s" % entry

	return to_return


# ------------------------------------------------------------------------------
# updates the clinical attribute record in the portal clinical_attribute database table
# returns boolean indicating success or failure

def update_clinical_attributes_in_db(cursor, clinical_attributes):

    try:
	cursor.executemany("update clinical_attribute set display_name=%s, description=%s," +
				" datatype=%s, patient_attribute=%s, priority=%s where attr_id = %s",
	[(clinical_attribute.display_name, clinical_attribute.description, clinical_attribute.datatype,
	clinical_attribute.patient_attribute, clinical_attribute.priority, clinical_attribute.normalized_column_header) for clinical_attribute in clinical_attributes])
    except MySQLdb.Error, msg:
        print >> ERROR_FILE, msg
        return False

    return True

# ------------------------------------------------------------------------------
# gets list of clinical attributes that need updating

def get_clinical_attributes_to_update(worksheet_clinical_attributes, database_clinical_attributes):

    # map that we are returning
    # key is the clinical attribute name and value is a ClinicalAttribute object
    to_return = {}

    for db_clinical_attribute in database_clinical_attributes.values():
        worksheet_clinical_attribute = worksheet_clinical_attributes.get(db_clinical_attribute.normalized_column_header)
        if (worksheet_clinical_attribute is None): continue
	if (db_clinical_attribute.display_name != worksheet_clinical_attribute.display_name or
		db_clinical_attribute.description != worksheet_clinical_attribute.description or
		db_clinical_attribute.datatype != worksheet_clinical_attribute.datatype or
		db_clinical_attribute.patient_attribute != worksheet_clinical_attribute.patient_attribute or
		db_clinical_attribute.priority != worksheet_clinical_attribute.priority):
		to_return[worksheet_clinical_attribute.normalized_column_header] = ClinicalAttribute(worksheet_clinical_attribute.normalized_column_header,
													worksheet_clinical_attribute.display_name,
													worksheet_clinical_attribute.description,
													worksheet_clinical_attribute.datatype,
													worksheet_clinical_attribute.patient_attribute,
													worksheet_clinical_attribute.priority)
    return to_return

# ------------------------------------------------------------------------------
# updates clinical attribute records in the clinical_attribute table in the
# portal database with values from the clinical_attributes google worksheet 

def update_clinical_attributes(cursor, worksheet_feed):

    # get map of cancer studies from worksheet
    print >> OUTPUT_FILE, 'Getting list of clinical attributes from google worksheet'
    worksheet_clinical_attributes_map = get_worksheet_clinical_attributes(worksheet_feed)
    if worksheet_clinical_attributes_map is not None:
        print >> OUTPUT_FILE, 'We have found %s clinical attributes in worksheet' % len(worksheet_clinical_attributes_map)
    else:
        print >> OUTPUT_FILE, 'Error reading clinical attributes from worksheet'

    # get map of clinical attributes from database
    print >> OUTPUT_FILE, 'Getting list of clinical attributes from portal database'
    database_clinical_attributes_map = get_db_clinical_attributes(cursor)
    if database_clinical_attributes_map is not None:
        print >> OUTPUT_FILE, 'We have found %s clinical attributes in portal database' % len(database_clinical_attributes_map)
    else:
        print >> OUTPUT_FILE, 'Error reading clinical attributes from portal database'

    # get list of clinical attributes to update
    print >> OUTPUT_FILE, 'Checking for clinical attributes that require updates'
    clinical_attributes_needing_update_map = get_clinical_attributes_to_update(worksheet_clinical_attributes_map, database_clinical_attributes_map)

    # update clinical attributes as needed
    if len(clinical_attributes_needing_update_map) > 0:
        print >> OUTPUT_FILE, 'We have %s clinical attributes that require an update' % len(clinical_attributes_needing_update_map)
	for attr_id in clinical_attributes_needing_update_map.keys():
		print >> OUTPUT_FILE, 'Updating "%s" clinical attribute' % attr_id 
        success = update_clinical_attributes_in_db(cursor, clinical_attributes_needing_update_map.values())
        if success:
            print >> OUTPUT_FILE, 'Successfully updated clinical attributes in database'
        else:
            print >> OUTPUT_FILE, 'Error updating clinical attributes in database'
    else:
        print >> OUTPUT_FILE, 'No clinical attributes to update, exiting'

# ------------------------------------------------------------------------------
# displays program usage (invalid args)

def usage():
    print >> OUTPUT_FILE, 'updateClinicalAttributesTable.py  --secrets-file [google secrets.json] --creds-file [oauth creds filename] --properties-file [properties file]'


# ------------------------------------------------------------------------------
# the big deal main.

def main():

    # parse command line options
    try:
        opts, args = getopt.getopt(sys.argv[1:], '', ['secrets-file=', 'creds-file=', 'properties-file='])
    except getopt.error, msg:
        print >> ERROR_FILE, msg
        usage()
        sys.exit(2)

    # process the options
	secrets_filename = ''
	creds_filename = ''
    properties_filename = ''

    for o, a in opts:
		if o == '--secrets-file':
			secrets_filename = a
		elif o == '--creds-file':
			creds_filename = a
		elif o == '--properties-file':
			properties_filename = a
    if secrets_filename == '' or creds_filename == '' or properties_filename == '':
        usage()
        sys.exit(2)

    # check existence of file
    if not os.path.exists(properties_filename):
        print >> ERROR_FILE, 'properties file cannot be found: ' + properties_filename
        sys.exit(2)

    # parse/get relevant portal properties
    print >> OUTPUT_FILE, 'Reading portal properties file: ' + properties_filename
    portal_properties = get_portal_properties(properties_filename)
    if not portal_properties:
        print >> OUTPUT_FILE, 'Error reading %s, exiting' % properties_filename
        return

    # get db connection & create cursor
    print >> OUTPUT_FILE, 'Connecting to database: ' + portal_properties.cgds_database_name
    connection = get_db_connection(portal_properties)
    if connection is not None:
        cursor = connection.cursor()
    else:
        print >> OUTPUT_FILE, 'Error connecting to database, exiting'
        return

    # login to google and get spreadsheet feed
    client = google_login(secrets_filename, creds_filename, portal_properties.google_id, portal_properties.google_pw, portal_properties.app_name)
    worksheet_feed = get_worksheet_feed(client, portal_properties.google_spreadsheet,
                                        portal_properties.google_worksheet)

	# update the clinical attributes
    update_clinical_attributes(cursor, worksheet_feed)

    # clean up
    cursor.close()
    connection.commit()
    connection.close()

# ------------------------------------------------------------------------------
# ready to roll

if __name__ == '__main__':
    main()
