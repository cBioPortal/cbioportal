#! /usr/bin/env python

# ------------------------------------------------------------------------------
# Script which flags a study within the triage portal for deployment into 
# production portal.
# ------------------------------------------------------------------------------


# ------------------------------------------------------------------------------
# imports
import sys
import getopt
import cbioportal.google_ss

# ------------------------------------------------------------------------------
# globals

# some file descriptors
ERROR_FILE = sys.stderr
OUTPUT_FILE = sys.stdout

def main():

    # process command line options
    try:
        opts, args = getopt.getopt(sys.argv[1:], '',
                                   ['google-id=', 'google-password=',
                                   'google-spreadsheet=', 'google-worksheet='])
    except getopt.error, msg:
        print >> ERROR_FILE, msg
        sys.exit(2)

    google_id = ''
    google_password = ''
    google_spreadsheet = ''
    google_worksheet = ''

    for o, a in opts:
        if o == '--google-id':
            google_id = a
        elif o == '--google-password':
            google_password = a
        elif o == '--google-spreadsheet':
            google_spreadsheet = a
        elif o == '--google-worksheet':
            google_worksheet = a

    if (google_id == '' or google_password == '' or 
        google_spreadsheet == '' or google_worksheet == ''):
        sys.exit(2)

    # the point of the script
    #worksheet = cbioportal.google_ss.get_worksheet_data(google_id, google_password,
    #                                                    google_spreadsheet, google_worksheet)
    #for row in worksheet:
    #    for entry in row:
    #        print >> OUTPUT_FILE, entry,
    #    print >> OUTPUT_FILE

# ------------------------------------------------------------------------------
# ready to roll

if __name__ == '__main__':
    main()
