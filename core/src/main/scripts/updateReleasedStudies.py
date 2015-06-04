#! /usr/bin/env python

# ------------------------------------------------------------------------------
# Script which updates cancer study group information within
# portal db - cancer_study table.  Group information is read from 
# the portal_importer_configuration google spreadsheet.
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
# importer.cancer_studies_worksheet
#
# For each cancer study listed in the cancer_studies google worksheet,
# the script will diff the 'Groups' column in the worksheet with the 
# respective GROUPS column in the cancer_study table.  If the values
# differ, the script will update the record in the table.
#
# ------------------------------------------------------------------------------
# imports
import os
import sys
import getopt
import MySQLdb

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
CANCER_STUDIES_WORKSHEET = 'importer.cancer_studies_worksheet'
IMPORTER_SPREADSHEET_SERVICE_APPNAME = 'importer.spreadsheet_service_appname'

# column constants on google spreadsheet
CANCER_STUDIES_KEY = 'cancerstudies'
CANCER_STUDY_STABLE_ID_KEY = 'stableid'

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

class CancerStudy(object):
    def __init__(self, cancer_study_id, cancer_study_stable_id):
        self.cancer_study_id = cancer_study_id
        self.cancer_study_stable_id = cancer_study_stable_id

# ------------------------------------------------------------------------------
# functions

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
        if (len(property) != 2):
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
        CANCER_STUDIES_WORKSHEET not in properties or len(properties[CANCER_STUDIES_WORKSHEET]) == 0 or
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
                            properties[CANCER_STUDIES_WORKSHEET],
                            properties[IMPORTER_SPREADSHEET_SERVICE_APPNAME])

# ------------------------------------------------------------------------------
# get cancer studies from cancer_study portal database table

def get_db_cancer_studies(cursor):

    # map that we are returning
    # key is the cancer study stable id and value is a CancerStudy object
    to_return = {}

    try:
        cursor.execute('select cancer_study_id, cancer_study_identifier from cancer_study')
        for row in cursor.fetchall():
            to_return[row[1]] = CancerStudy(row[0], row[1])
    except MySQLdb.Error, msg:
        print >> ERROR_FILE, msg
        return None

    return to_return

# ------------------------------------------------------------------------------
# adds 'x' under given portal name column - cancer study on cancer studies worksheet 

def update_cancer_studies(client, cursor, worksheet_feed, portal_name):

	# get map of cancer studies from database
	print >> OUTPUT_FILE, 'Getting list of cancer studies from portal database'
	database_cancer_studies_map = get_db_cancer_studies(cursor)
	if database_cancer_studies_map is not None:
		print >> OUTPUT_FILE, 'We have found %s cancer studies in portal database' % len(database_cancer_studies_map)
	else:
		print >> OUTPUT_FILE, 'Error reading cancer studies from portal database'

	# for each cancer study from db, update portal_name column on worksheet
	for entry in worksheet_feed.entry:
		for key in entry.custom:
			if entry.custom[key].text in database_cancer_studies_map.keys():
				row_data = get_row_data(entry, portal_name)
				if row_data is not None:
					print >> OUTPUT_FILE, 'Updating cancer study entry on worksheet, setting %s:%s to "x"' % (entry.custom[key].text, portal_name)
					client.UpdateRow(entry, row_data)

# ------------------------------------------------------------------------------
# constructs new row entry

def get_row_data(entry, portal_name):

	dict = {}
	return_dict = False;
	for key in entry.custom:
		if key == portal_name and entry.custom[key].text != 'x':
			dict[key] = 'x'
			return_dict = True
		else:
			dict[key] = entry.custom[key].text
	if return_dict == True:
		return dict
	return None

# ------------------------------------------------------------------------------
# displays program usage (invalid args)

def usage():
    print >> OUTPUT_FILE, 'updateReleaseStudies.py --secrets-file [google secrets.json] --creds-file [oauth creds filename] --properties-file [properties file] --portal-name [worksheet column]'

# ------------------------------------------------------------------------------
# the big deal main.

def main():

	# parse command line options
	try:
		opts, args = getopt.getopt(sys.argv[1:], '', ['secrets-file=', 'creds-file=', 'properties-file=', 'portal-name='])
	except getopt.error, msg:
		print >> ERROR_FILE, msg
		usage()
		sys.exit(2)


	secrets_filename = ''
	creds_filename = ''
	properties_filename = ''
	portal_name = ''

	for o, a in opts:
		if o == '--secrets-file':
			secrets_filename = a
		elif o == '--creds-file':
			creds_filename = a
		elif o == '--properties-file':
			properties_filename = a
		elif o == '--portal-name':
			portal_name = a

	if (secrets_filename == '' or creds_filename == '' or properties_filename == '' or portal_name == ''):
		usage()
		sys.exit(2)

	# check existence of file
	if not os.path.exists(properties_filename):
		print >> ERROR_FILE, 'properties file cannot be found: ' + properties_filename
		sys.exit(2)

    # parse/get relevant portal properties
	print >> OUTPUT_FILE, 'Reading portal properties file: ' + properties_filename
	portal_properties = get_portal_properties(properties_filename)
	if portal_properties is None:
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

	print >> OUTPUT_FILE, 'Updating ' + portal_properties.google_spreadsheet
	worksheet_feed = get_worksheet_feed(client, portal_properties.google_spreadsheet,
										portal_properties.google_worksheet)
	update_cancer_studies(client, cursor, worksheet_feed, portal_name)

    # clean up
	if cursor is not None:
		cursor.close()
	if connection is not None:
		connection.close()

# ------------------------------------------------------------------------------
# ready to roll

if __name__ == '__main__':
    main()
