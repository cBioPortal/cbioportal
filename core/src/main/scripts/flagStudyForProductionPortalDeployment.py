#! /usr/bin/env python

# ------------------------------------------------------------------------------
# Script which flags a study within the triage portal for deployment into 
# production portal.
# ------------------------------------------------------------------------------


# ------------------------------------------------------------------------------
# imports
import sys
import getopt

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

# column constants on google spreadsheet
TRIAGE_PORTAL_KEY = "triage-portal"
MSK_AUTOMATION_PORTAL_KEY = "msk-automation-portal"

# ------------------------------------------------------------------------------
# subroutines

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
# Flags a study on the given worksheet
# for deployment into the msk automation portal.

def flag_study_for_production_portal_deployment(client, worksheet_feed, cancer_study_id, remove_from_triage):

    for entry in worksheet_feed.entry:
        for key in entry.custom:  
            if entry.custom[key].text == cancer_study_id:
                client.UpdateRow(entry, get_row_data(entry, remove_from_triage))
                return

# ------------------------------------------------------------------------------
# constructs new row entry

def get_row_data(entry, remove_from_triage):

    dict = {}
    for key in entry.custom:
        if key == TRIAGE_PORTAL_KEY:
            if remove_from_triage == 't':
                dict[key] = 'r'
            else:
                dict[key] = ''
        elif key == MSK_AUTOMATION_PORTAL_KEY:
            dict[key] = 'x'
        else:
            dict[key] = entry.custom[key].text
    return dict

# ------------------------------------------------------------------------------
# displays program usage (invalid args)

def usage():
    print >> OUTPUT_FILE, ('flagStudyForProductionPortalDeployment.py --secrets-file [google secrets.json] --creds-file [oauth creds filename] --google-id --google-password ' +
                           '--google-spreadsheet --google-worksheet --cancer-study-id [STABLE_ID] [--remove-from-triage [t/f]]')

# ------------------------------------------------------------------------------
# the big deal main.

def main():

    # process command line options
    try:
        opts, args = getopt.getopt(sys.argv[1:], '',
                                   ['secrets-file=', 'creds-file=', 'google-id=', 'google-password=',
                                   'google-spreadsheet=', 'google-worksheet=',
                                   'cancer-study-id=', 'remove-from-triage='])
    except getopt.error, msg:
        print >> ERROR_FILE, msg
        usage()
        sys.exit(2)

	secrets_filename = ''
	creds_filename = ''
    google_id = ''
    google_password = ''
    google_spreadsheet = ''
    google_worksheet = ''
    cancer_study_id = ''
    remove_from_triage = ''

    for o, a in opts:
		if o == '--secrets-file':
			secrets_filename = a
		elif o == '--creds-file':
			creds_filename = a
		elif o == '--google-id':
			google_id = a
		elif o == '--google-password':
			google_password = a
		elif o == '--google-spreadsheet':
			google_spreadsheet = a
		elif o == '--google-worksheet':
			google_worksheet = a
		elif o == '--cancer-study-id':
			cancer_study_id = a
		elif o == '--remove-from-triage':
			remove_from_triage = a

    if (secrets_filename == '' or creds_filename == '' or google_id == '' or google_password == '' or 
        google_spreadsheet == '' or google_worksheet == '' or cancer_study_id == ''):
        usage()
        sys.exit(2)

    # the point of the script
    client = google_login(secrets_filename, creds_filename, google_id, google_password, sys.argv[1])
    worksheet_feed = get_worksheet_feed(client, google_spreadsheet, google_worksheet)
    flag_study_for_production_portal_deployment(client, worksheet_feed, cancer_study_id, remove_from_triage)

# ------------------------------------------------------------------------------
# ready to roll

if __name__ == '__main__':
    main()
