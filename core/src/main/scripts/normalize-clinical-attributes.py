#! /usr/bin/env python

# ------------------------------------------------------------------------------
# Script iterates over all data_clinical_*.txt files and replaces column headers
# found under the EXTERNAL_COLUMN_HEADER of the portal_importer_configuration -
# clinical_attributes_namespace with the value found under the
# NORMALIZED_COLUMN_HEADER column in the same worksheet. 

# ------------------------------------------------------------------------------
# imports

import os
import re
import sys
import getopt
import fileinput

import gdata.docs.client
import gdata.docs.service
import gdata.spreadsheet.service

# ------------------------------------------------------------------------------
# globals

ERROR_FILE = sys.stderr
OUTPUT_FILE = sys.stdout

GOOGLE_ID = 'google.id'
GOOGLE_PW = 'google.pw'
PORTAL_IMPORTER_CONFIGURATION_SPREADSHEET = 'importer.spreadsheet'
CLINICAL_ATTRIBUTES_NAMESPACE_WORKSHEET = 'importer.clinical_attributes_namespace_worksheet'

CLINICAL_DATA_FILENAME_PATTERN = 'data_clinical_.*\.txt'
KEY_COLUMN_HEADER = 'externalcolumnheader'
VALUE_COLUMN_HEADER = 'normalizedcolumnheader'

GOOGLE_SPREADSHEET_CLIENT = gdata.spreadsheet.service.SpreadsheetsService()

# ------------------------------------------------------------------------------
# class definitions

class PortalProperties(object):
    def __init__(self,
                 google_id, google_pw, google_spreadsheet, google_worksheet):
        self.google_id = google_id
        self.google_pw = google_pw
        self.google_spreadsheet = google_spreadsheet
        self.google_worksheet = google_worksheet

# ------------------------------------------------------------------------------
# routines

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
		# spreadsheet url contains an '=' sign
		if line.startswith(PORTAL_IMPORTER_CONFIGURATION_SPREADSHEET):
			property = [property[0], line[line.index('=')+1:len(line)]]
		if (len(property) != 2):
			continue
		properties[property[0]] = property[1].strip()
	portal_properties_file.close()

    # error check
	if (GOOGLE_ID not in properties or len(properties[GOOGLE_ID]) == 0 or
		GOOGLE_PW not in properties or len(properties[GOOGLE_PW]) == 0 or
		PORTAL_IMPORTER_CONFIGURATION_SPREADSHEET not in properties or len(properties[PORTAL_IMPORTER_CONFIGURATION_SPREADSHEET]) == 0 or
		CLINICAL_ATTRIBUTES_NAMESPACE_WORKSHEET not in properties or len(properties[CLINICAL_ATTRIBUTES_NAMESPACE_WORKSHEET]) == 0):
		print >> ERROR_FILE, 'Missing one or more required properties, please check property file'
		return None

    # return an instance of PortalProperties
	return PortalProperties(properties[GOOGLE_ID],
                            properties[GOOGLE_PW],
                            properties[PORTAL_IMPORTER_CONFIGURATION_SPREADSHEET],
                            properties[CLINICAL_ATTRIBUTES_NAMESPACE_WORKSHEET])

# ------------------------------------------------------------------------------
# logs into google spreadsheet client

def google_login(user, pw):

	# google spreadsheet
	GOOGLE_SPREADSHEET_CLIENT.email = user
	GOOGLE_SPREADSHEET_CLIENT.password = pw
	GOOGLE_SPREADSHEET_CLIENT.source = sys.argv[0]
	GOOGLE_SPREADSHEET_CLIENT.ProgrammaticLogin()

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

def get_worksheet_feed(ss, ws):

    ss_id = get_feed_id(GOOGLE_SPREADSHEET_CLIENT.GetSpreadsheetsFeed(), ss)
    ws_id = get_feed_id(GOOGLE_SPREADSHEET_CLIENT.GetWorksheetsFeed(ss_id), ws)
    
    return GOOGLE_SPREADSHEET_CLIENT.GetListFeed(ss_id, ws_id)

# ------------------------------------------------------------------------------
# creates map KEY_COLUMN_HEADER -> VALUE_COLUMN_HEADER

def get_attribute_map(worksheet_feed, key_column_header, value_column_header):

    # map that we are returning
    to_return = {}

    for entry in worksheet_feed.entry:
		key_value = entry.custom[key_column_header].text
		value_value = entry.custom[value_column_header].text
		if key_value is not None and len(key_value) > 0 and value_value is not None and len(value_value) > 0: 
 			to_return[key_value.strip()] = value_value.strip()

    return to_return


# ------------------------------------------------------------------------------
# for each column header in clinical data file, replace column headers
# found under the EXTERNAL_COLUMN_HEADER of the portal_importer_configuration -
# clinical_attributes_namespace with the value found under the
# NORMALIZED_COLUMN_HEADER column in the same worksheet. 

def process_clinical(clinical_attribute_map, filename):

	print >>  OUTPUT_FILE, "processing: " + filename
	data_clinical_file = fileinput.input(filename, inplace=1)

	should_process_header = True
	for line in data_clinical_file:
		line = line.strip()
		if len(line) == 0:
			continue
		# strip out headers
		if line[0] == '#':
			continue
		if should_process_header:
			parts = [part.strip() for part in line.split('\t')]
			ct = 0
			for part in parts:
				ct = ct + 1
				if len(part) > 0 and clinical_attribute_map.has_key(part):
					new_header = clinical_attribute_map[part]
					print >> OUTPUT_FILE, "replacing: %s --> %s" % (part, new_header) 
				else:
					new_header = part
				if ct < len(parts):
					new_header = new_header + '\t' 
				print new_header,
			print
			should_process_header = False
		else:
			print line

	data_clinical_file.close()

def usage():
    print >> ERROR_FILE, 'normal-clinical-attributes.py --properties-file [properties file] --directory [directory to traverse]'

def main():

	# get options to script
	try:
		opts, args = getopt.getopt(sys.argv[1:], '', ['properties-file=', 'directory='])
	except getopt.error, msg:
		print >> ERROR_FILE, msg
		usage()
		sys.exit(2)

	# process the options
	properties_filename = ''
	directory_name = ''
	for o, a in opts:
		if o == '--properties-file':
		    properties_filename = a
		elif o == '--directory':
			directory_name = a
	if (properties_filename == '' or directory_name == ''):
		usage()
		sys.exit(2)

	# sanity check
	if not os.path.exists(properties_filename):
	    print >> ERROR_FILE, 'properties file cannot be found: ' + properties_filename
	    sys.exit(2)
	if not os.path.isdir(directory_name):
		print >> ERROR_FILE, 'directory cannot be found: ' + directory_name
		sys.exit(2)

	# parse/get relevant portal properties
	print >> OUTPUT_FILE, 'Reading portal properties file: ' + properties_filename
	portal_properties = get_portal_properties(properties_filename)
	if not portal_properties:
		print >> OUTPUT_FILE, 'Error reading %s, exiting' % properties_filename
		return

	# login to google and get spreadsheet feed & create clinical attribute map
	google_login(portal_properties.google_id, portal_properties.google_pw)
	worksheet_feed = get_worksheet_feed(portal_properties.google_spreadsheet,
										portal_properties.google_worksheet)
	clinical_attributes_map = get_attribute_map(worksheet_feed, KEY_COLUMN_HEADER, VALUE_COLUMN_HEADER)

	# iterate over the directory
	data_clinical_re = re.compile(CLINICAL_DATA_FILENAME_PATTERN)
	for root, dirs, files in os.walk(directory_name):
		for name in files:
			filename = os.path.join(root, name)
			if (data_clinical_re.match(name)):
				process_clinical(clinical_attributes_map, filename)

# ------------------------------------------------------------------------------
# lets shoot this f'in film...
# ------------------------------------------------------------------------------
if __name__ == '__main__':
	main()
