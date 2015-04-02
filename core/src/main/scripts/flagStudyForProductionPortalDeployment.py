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

# ------------------------------------------------------------------------------
# globals

# some file descriptors
ERROR_FILE = sys.stderr
OUTPUT_FILE = sys.stdout

# a ref to the google spreadsheet client - used for all i/o to google spreadsheet
GOOGLE_SPREADSHEET_CLIENT = gdata.spreadsheet.service.SpreadsheetsService()

# column constants on google spreadsheet
TRIAGE_PORTAL_KEY = "triage-portal"
MSK_AUTOMATION_PORTAL_KEY = "msk-automation-portal"

# ------------------------------------------------------------------------------
# subroutines

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
# Flags a study on the given worksheet
# for deployment into the msk automation portal.

def flag_study_for_production_portal_deployment(worksheet_feed, cancer_study_id, remove_from_triage):

    for entry in worksheet_feed.entry:
        for key in entry.custom:  
            if entry.custom[key].text == cancer_study_id:
                GOOGLE_SPREADSHEET_CLIENT.UpdateRow(entry,
                                                    get_row_data(entry, remove_from_triage))
                return

# ------------------------------------------------------------------------------
# constructs new row entry

def get_row_data(entry, remove_from_triage):

    dict = {}
    for key in entry.custom:
        if key == TRIAGE_PORTAL_KEY and remove_from_triage == 't':
            dict[key] = 'R'
        elif key == MSK_AUTOMATION_PORTAL_KEY:
            dict[key] = 'x'
        else:
            dict[key] = entry.custom[key].text
    return dict

# ------------------------------------------------------------------------------
# displays program usage (invalid args)

def usage():
    print >> OUTPUT_FILE, ('flagStudyForProductionPortalDeployment.py --google-id --google-password ' +
                           '--google-spreadsheet --google-worksheet --cancer-study-id [STABLE_ID] [--remove-from-triage [t/f]]')

# ------------------------------------------------------------------------------
# the big deal main.

def main():

    # process command line options
    try:
        opts, args = getopt.getopt(sys.argv[1:], '',
                                   ['google-id=', 'google-password=',
                                   'google-spreadsheet=', 'google-worksheet=',
                                   'cancer-study-id=', 'remove-from-triage='])
    except getopt.error, msg:
        print >> ERROR_FILE, msg
        usage()
        sys.exit(2)

    google_id = ''
    google_password = ''
    google_spreadsheet = ''
    google_worksheet = ''
    cancer_study_id = ''
    remove_from_triage = ''

    for o, a in opts:
        if o == '--google-id':
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

    if (google_id == '' or google_password == '' or 
        google_spreadsheet == '' or google_worksheet == '' or cancer_study_id == ''):
        usage()
        sys.exit(2)

    # the point of the script
    google_login(google_id, google_password)
    worksheet_feed = get_worksheet_feed(google_spreadsheet, google_worksheet)
    flag_study_for_production_portal_deployment(worksheet_feed, cancer_study_id, remove_from_triage)

# ------------------------------------------------------------------------------
# ready to roll

if __name__ == '__main__':
    main()
