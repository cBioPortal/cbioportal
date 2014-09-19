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

import smtplib
import gdata.docs.client
import gdata.docs.service
import gdata.spreadsheet.service

from email.MIMEMultipart import MIMEMultipart
from email.MIMEBase import MIMEBase
from email.MIMEText import MIMEText
from email.Utils import COMMASPACE, formatdate
from email import Encoders

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

# a ref to the google spreadsheet client - used for all i/o to google spreadsheet
GOOGLE_SPREADSHEET_CLIENT = gdata.spreadsheet.service.SpreadsheetsService()

# column constants on google spreadsheet
CANCER_STUDY_STABLE_ID_KEY = 'stableid'
CANCER_STUDY_GROUPS_KEY = 'groups'

# consts used in email
SMTP_SERVER = "cbio.mskcc.org"
MESSAGE_FROM = "cbioportal@cbio.mskcc.org"
MESSAGE_RECIPIENTS = []
MESSAGE_RECIPIENTS_ON_ERROR = []
MESSAGE_SUBJECT = "cBioPortal cancer study updates"
MESSAGE_BODY_ERROR = "There was an error attempting to update cancer study groups in the portal database."
MESSAGE_BODY_SUCCESS = "Successfully updated the following cancer study group permissions:\n\n"
MESSAGE_BODY_NO_UPDATES_REQUIRED = "No cancer studies require updates."

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
    def __init__(self, cancer_study_id, cancer_study_stable_id, groups):
        self.cancer_study_id = cancer_study_id
        self.cancer_study_stable_id = cancer_study_stable_id
        self.groups = "" if groups is None else groups

# ------------------------------------------------------------------------------
# functions

# ------------------------------------------------------------------------------
# Uses smtplib to send email.

def send_mail(to, subject, body, server=SMTP_SERVER):

    assert type(to)==list

    msg = MIMEMultipart()
    msg['Subject'] = subject
    msg['From'] = MESSAGE_FROM
    msg['To'] = COMMASPACE.join(to)
    msg['Date'] = formatdate(localtime=True)

    msg.attach(MIMEText(body))

    # combine to and bcc lists for sending
    combined_to_list = []
    for to_name in to:
        combined_to_list.append(to_name)

    smtp = smtplib.SMTP(server)
    smtp.sendmail(MESSAGE_FROM, combined_to_list, msg.as_string() )
    smtp.close()



# ------------------------------------------------------------------------------
# logs into google spreadsheet client

def google_login(user, pw, app_name):

    # google spreadsheet
    GOOGLE_SPREADSHEET_CLIENT.email = user
    GOOGLE_SPREADSHEET_CLIENT.password = pw
    GOOGLE_SPREADSHEET_CLIENT.source = app_name
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
        cursor.execute('select cancer_study_id, cancer_study_identifier, groups from cancer_study')
        for row in cursor.fetchall():
            to_return[row[1]] = CancerStudy(row[0], row[1], row[2])
    except MySQLdb.Error, msg:
        print >> ERROR_FILE, msg
        return None

    return to_return

# ------------------------------------------------------------------------------
# get cancer studies from google worksheet

def get_worksheet_cancer_studies(worksheet_feed):

    # map that we are returning
    # key is the cancer study stable id and value is a CancerStudy object
    to_return = {}

    for entry in worksheet_feed.entry:
        cancer_study_id = 'not_used_here'
        cancer_study_stable_id = entry.custom[CANCER_STUDY_STABLE_ID_KEY].text
        groups = entry.custom[CANCER_STUDY_GROUPS_KEY].text
        to_return[cancer_study_stable_id] = CancerStudy(cancer_study_id, cancer_study_stable_id, groups)

    return to_return

# ------------------------------------------------------------------------------
# updates the cancer study record in the portal cancer_study database table
# returns boolean indicating success or failure

def update_cancer_studies_in_db(cursor, cancer_studies):

    try:
        cursor.executemany("update cancer_study set groups=%s where cancer_study_id=%s",
        [(cancer_study.groups, cancer_study.cancer_study_id) for cancer_study in cancer_studies])
    except MySQLdb.Error, msg:
        print >> ERROR_FILE, msg
        return False

    return True

# ------------------------------------------------------------------------------
# comparse gets list of cancer studies needing update 

# ------------------------------------------------------------------------------
# gets list of cancer studies needing update 

def get_cancer_studies_to_update(worksheet_cancer_studies, database_cancer_studies):

    # map that we are returning
    # key is the cancer study stable id and value is a CancerStudy object
    to_return = {}

    for db_cancer_study in database_cancer_studies.values():
        worksheet_cancer_study = worksheet_cancer_studies.get(db_cancer_study.cancer_study_stable_id)
        if (worksheet_cancer_study is None): continue
        if set(worksheet_cancer_study.groups.split(';')).symmetric_difference(set(db_cancer_study.groups.split(';'))):
            to_return[db_cancer_study.cancer_study_stable_id] = CancerStudy(db_cancer_study.cancer_study_id,
                                                                            db_cancer_study.cancer_study_stable_id,
                                                                            worksheet_cancer_study.groups)

    return to_return

    
# ------------------------------------------------------------------------------
# adds new groups to cancer studies from the google spreadsheet into the cgds portal database
# return a list of two elements, first is email body, second is updated cancer study list

def update_cancer_studies(cursor, worksheet_feed):

    # get map of cancer studies from worksheet
    print >> OUTPUT_FILE, 'Getting list of cancer studies from google worksheet'
    worksheet_cancer_studies_map = get_worksheet_cancer_studies(worksheet_feed)
    if worksheet_cancer_studies_map is not None:
        print >> OUTPUT_FILE, 'We have found %s cancer studies in worksheet' % len(worksheet_cancer_studies_map)
    else:
        print >> OUTPUT_FILE, 'Error reading cancer studies from worksheet'
        return (MESSAGE_BODY_ERROR, None)

    # get map of cancer studies from database
    print >> OUTPUT_FILE, 'Getting list of cancer studies from portal database' 
    database_cancer_studies_map = get_db_cancer_studies(cursor)
    if database_cancer_studies_map is not None:
        print >> OUTPUT_FILE, 'We have found %s cancer studies in portal database' % len(database_cancer_studies_map)
    else:
        print >> OUTPUT_FILE, 'Error reading cancer studies from portal database'
        return (MESSAGE_BODY_ERROR, None)

    # get list of cancer studies to update
    print >> OUTPUT_FILE, 'Checking for cancer studies that require updates'
    cancer_studies_needing_update_map = get_cancer_studies_to_update(worksheet_cancer_studies_map, database_cancer_studies_map)

    # update cancer studies as needed
    if (len(cancer_studies_needing_update_map) > 0):
        print >> OUTPUT_FILE, 'We have %s cancer studies that require an update' % len(cancer_studies_needing_update_map)
        success = update_cancer_studies_in_db(cursor, cancer_studies_needing_update_map.values())
        if success:
            print >> OUTPUT_FILE, 'Successfully updated cancer studies in database'
            return (MESSAGE_BODY_SUCCESS, cancer_studies_needing_update_map)
        else:
            print >> OUTPUT_FILE, 'Error updating cancer studies in database'
            return (MESSAGE_BODY_ERROR, None)
    else:
        print >> OUTPUT_FILE, 'No cancer studies to update, exiting'
        return (MESSAGE_BODY_NO_UPDATES_REQUIRED, None)

# ------------------------------------------------------------------------------
# displays program usage (invalid args)

def usage():
    print >> OUTPUT_FILE, 'updateCancerStudies.py --properties-file [properties file] --send-email-confirm [true or false]'

# ------------------------------------------------------------------------------
# the big deal main.

def main():

    # parse command line options
    try:
        opts, args = getopt.getopt(sys.argv[1:], '', ['properties-file=', 'send-email-confirm='])
    except getopt.error, msg:
        print >> ERROR_FILE, msg
        usage()
        sys.exit(2)

    # process the options
    properties_filename = ''
    send_email_confirm = ''

    for o, a in opts:
        if o == '--properties-file':
            properties_filename = a
        elif o == '--send-email-confirm':
            send_email_confirm = a
    if (properties_filename == '' or send_email_confirm == '' or
        (send_email_confirm != 'true' and send_email_confirm != 'false')):
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
    google_login(portal_properties.google_id, portal_properties.google_pw, portal_properties.app_name)

    print >> OUTPUT_FILE, 'Updating ' + portal_properties.google_spreadsheet
    worksheet_feed = get_worksheet_feed(portal_properties.google_spreadsheet,
                                        portal_properties.google_worksheet)

    message_body, cancer_studies_updated_map = update_cancer_studies(cursor, worksheet_feed)

    # clean up
    cursor.close()
    connection.commit()
    connection.close()

    # sending emails
    if send_email_confirm == 'true':
        if message_body is MESSAGE_BODY_ERROR:
            send_mail(MESSAGE_RECIPIENTS_ON_ERROR, MESSAGE_SUBJECT + " ERROR", message_body)
        elif message_body is MESSAGE_BODY_SUCCESS:
            for cancer_study in cancer_studies_updated_map.values():
                message_body += "%s [%s]\n" % (cancer_study.cancer_study_stable_id, cancer_study.groups)
            send_mail(MESSAGE_RECIPIENTS, MESSAGE_SUBJECT, message_body)

# ------------------------------------------------------------------------------
# ready to roll

if __name__ == '__main__':
    main()
