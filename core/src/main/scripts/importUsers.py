#! /usr/bin/env python

# ------------------------------------------------------------------------------
# Script which adds new users fram google spreadsheet into the the cgds
# user table.  The following properties must be specified in portal.properties:
#
# db.name
# db.user
# db.password
# db.host
# google.id
# google.pw
# users.spreadsheet
# users.worksheet
#
# The script considers all users in the google spreadsheet, currently found:
#
# https://docs.google.com/spreadsheet/ccc?key=0Ag6KD6MljCkYdDQwbEpVQTR6UWNCSjZfUE9NTnBDaXc&hl=en_US#gid=0
#
# that have an "APPROVED" value in the "Status (APPROVED or BLANK)" column.  If that
# user does not exist in the user table of the cgds database, the user will be added
# to both the user table and authority table.  In addition, a confirmation email will
# be sent to the user notifying them of their acct activation.
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
CGDS_USERS_SPREADSHEET = 'users.spreadsheet'
CGDS_USERS_WORKSHEET = 'users.worksheet'

# Google spreadsheet name - used as keys to email subjects/body below
GDAC_USER_SPREADSHEET = 'Request Access to the cBio GDAC Cancer Genomics Portal'
SU2C_USER_SPREADSHEET = 'Request Access to the cBio SU2C Cancer Genomics Portal'
PROSTATE_USER_SPREADSHEET = 'Request Access to the cBio Prostate Cancer Genomics Portal'
GLIOMA_USER_SPREADSHEET = 'Request Access to the cBio Glioma Cancer Genomics Portal'
ACC_USER_SPREADSHEET = 'Request Access to the cBio ACC Cancer Genomics Portal'
TARGET_USER_SPREADSHEET = 'Request Access to the cBio TARGET Cancer Genomics Portal'

# portal name
PORTAL_NAME = { GDAC_USER_SPREADSHEET : "gdac-portal",
                PROSTATE_USER_SPREADSHEET : "prostate-portal",
                GLIOMA_USER_SPREADSHEET : "glioma-portal",
                ACC_USER_SPREADSHEET : "acc-portal",
                SU2C_USER_SPREADSHEET : "su2c-portal",
                TARGET_USER_SPREADSHEET : "target-portal" }

# a ref to the google spreadsheet client - used for all i/o to google spreadsheet
GOOGLE_SPREADSHEET_CLIENT = gdata.spreadsheet.service.SpreadsheetsService()

# column constants on google spreadsheet
FULLNAME_KEY = "fullname"
INST_EMAIL_KEY = "institutionalemailaddress"
OPENID_EMAIL_KEY = "googleoropenidaddress"
STATUS_KEY = "statusapprovedorblank"
AUTHORITIES_KEY = "authoritiesalloralltcgaandorsemicolondelimitedcancerstudylist"

# possible values in status column
STATUS_APPROVED = "APPROVED"

# consts used in email
SMTP_SERVER = "cbio.mskcc.org"
MESSAGE_FROM = "cbioportal@cbio.mskcc.org"
MESSAGE_BCC = ["jgao@cbio.mskcc.org", "schultz@cbio.mskcc.org", "grossb@cbio.mskcc.org"]
MESSAGE_SUBJECT = { GDAC_USER_SPREADSHEET : "cBioPortal for TCGA Access",
                    PROSTATE_USER_SPREADSHEET : "cBioPortal for Prostate Cancer Access",
                    SU2C_USER_SPREADSHEET : "cBioPortal for SU2C Access",
                    TARGET_USER_SPREADSHEET : "cBioPortal for NCI-TARGET" }
GDAC_MESSAGE_BODY = """Thank you for your interest in the cBioPortal for TCGA. We have granted you access. You can login at http://cbioportal.org/gdac-portal/. Please let us know if you have any problems logging in.

Please keep in mind that the data provided in this Portal are preliminary and subject to change. The data are only available to researchers funded through TCGA or involved in the TCGA Disease and Analysis Working Groups.
"""

SU2C_MESSAGE_BODY = """Thank you for your interest in the cBioPortal for SU2C. We have granted you access. You can login at http://cbioportal.org/su2c-portal/. Please let us know if you have any problems logging in.

Please keep in mind that the most of the data provided in this Portal are preliminary, unpublished and subject to change.
"""

PROSTATE_MESSAGE_BODY = """Thank you for your interest in the cBioPortal for Prostate. We have granted you access. You can login at http://cbioportal.org/prostate-portal/. Please let us know if you have any problems logging in.

Please keep in mind that the most of the data provided in this Portal are preliminary, unpublished and subject to change.
"""

GLIOMA_MESSAGE_BODY = """Thank you for your interest in the cBioPortal for Glioma. We have granted you access. You can login at http://cbioportal.org/glioma-portal/. Please let us know if you have any problems logging in.

Please keep in mind that the most of the data provided in this Portal are preliminary, unpublished and subject to change.
"""

ACC_MESSAGE_BODY = """Thank you for your interest in the cBioPortal for ACC. We have granted you access. You can login at http://cbioportal.org/acc-portal/. Please let us know if you have any problems logging in.

Please keep in mind that the most of the data provided in this Portal are preliminary, unpublished and subject to change.
"""

TARGET_MESSAGE_BODY = """Thank you for your interest in the cBioPortal for NCI-TARGET. We have granted you access. You can login at http://cbioportal.org/target-portal/. Please let us know if you have any problems logging in.

Please keep in mind that the most of the data provided in this Portal are preliminary, unpublished and subject to change.
"""

MESSAGE_BODY = { GDAC_USER_SPREADSHEET : GDAC_MESSAGE_BODY,
                 PROSTATE_USER_SPREADSHEET : PROSTATE_MESSAGE_BODY,
                 GLIOMA_USER_SPREADSHEET : GLIOMA_MESSAGE_BODY,
                 ACC_USER_SPREADSHEET : ACC_MESSAGE_BODY,
                 SU2C_USER_SPREADSHEET : SU2C_MESSAGE_BODY,
                 TARGET_USER_SPREADSHEET : TARGET_MESSAGE_BODY }


# ------------------------------------------------------------------------------
# class definitions

class PortalProperties(object):
    def __init__(self,
                 cgds_database_host,
                 cgds_database_name, cgds_database_user, cgds_database_pw,
                 google_id, google_pw, google_spreadsheet, google_worksheet):
        self.cgds_database_host = cgds_database_host
        self.cgds_database_name = cgds_database_name
        self.cgds_database_user = cgds_database_user
        self.cgds_database_pw = cgds_database_pw
        self.google_id = google_id
        self.google_pw = google_pw
        self.google_spreadsheet = google_spreadsheet
        self.google_worksheet = google_worksheet

class User(object):
    def __init__(self, inst_email, openid_email, name, enabled, authorities):
        self.inst_email = inst_email
        self.openid_email = openid_email
        self.name = name
        self.enabled = enabled
        self.authorities = authorities

# ------------------------------------------------------------------------------
# functions

#
# Uses smtplib to send email.
#
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
    for bcc_name in MESSAGE_BCC:
        combined_to_list.append(bcc_name)

    smtp = smtplib.SMTP(server)
    smtp.sendmail(MESSAGE_FROM, combined_to_list, msg.as_string() )
    smtp.close()


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
# insert new users into table - this list does not contain users already in table

def insert_new_users(cursor, new_user_list):

    try:
        cursor.executemany("insert into users values(%s, %s, %s)",
                           [(user.openid_email, user.name, user.enabled) for user in new_user_list])
        for user in new_user_list:
            # authorities is semicolon delimited
            authorities = user.authorities
            cursor.executemany("insert into authorities values(%s, %s)",
                               [(user.openid_email, authority) for authority in authorities])
    except MySQLdb.Error, msg:
        print >> ERROR_FILE, msg
        return False

    return True

# ------------------------------------------------------------------------------
# get current users from database

def get_current_user_map(cursor):

    # map that we are returning
    # key is the email address of the user (primary key) and value is a User object
    to_return = {}

    # recall each tuple in user table is ['EMAIL', 'NAME', 'ENABLED'] &
    # no tuple can contain nulls
    try:
        cursor.execute('select * from users')
        for row in cursor.fetchall():
            to_return[row[0]] = User('not_used_here', row[0], row[1], row[2], 'not_used_here')
    except MySQLdb.Error, msg:
        print >> ERROR_FILE, msg
        return None

    return to_return

# ------------------------------------------------------------------------------
# get current user authorities

def get_user_authorities(cursor, openid_email):

        # list of authorities (cancer studies) we are returning -- as a set
        to_return = []

        # recall each tuple in authorities table is ['EMAIL', 'AUTHORITY']
        # no tuple can contain nulls
        try:
                cursor.execute('select * from authorities where email = (%s)', openid_email)
                for row in cursor.fetchall():
                        to_return.append(row[1])
        except MySQLdb.Error, msg:
                print >> ERROR_FILE, msg
                return None

        return to_return

# ------------------------------------------------------------------------------
# get current users

def get_new_user_map(worksheet_feed, current_user_map, portal_name):

    # map that we are returning
    # key is the institutional email address + openid (in case 1 use wants multiple openids)
    # of the user and value is a User object
    to_return = {}

    for entry in worksheet_feed.entry:
        # we are only concerned with 'APPROVED' entries
        if (entry.custom[STATUS_KEY].text is not None and
            entry.custom[STATUS_KEY].text.strip() == STATUS_APPROVED):
            inst_email = entry.custom[INST_EMAIL_KEY].text.strip()
            openid_email = entry.custom[OPENID_EMAIL_KEY].text.strip().lower()
            name = entry.custom[FULLNAME_KEY].text.strip()
            authorities = entry.custom[AUTHORITIES_KEY].text.strip()
            # do not add entry if this entry is a current user
            if openid_email not in current_user_map:
                if authorities[-1:] == ';':
                    authorities = authorities[:-1]
                to_return[openid_email] = User(inst_email, openid_email, name, 1,
                    [portal_name + ':' + au for au in authorities.split(';')])

    return to_return
    
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
        # spreadsheet url contains an '=' sign
        if line.startswith(CGDS_USERS_SPREADSHEET):
            property = [property[0], line[line.index('=')+1:len(line)]]
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
        CGDS_USERS_SPREADSHEET not in properties or len(properties[CGDS_USERS_SPREADSHEET]) == 0 or
        CGDS_USERS_WORKSHEET not in properties or len(properties[CGDS_USERS_WORKSHEET]) == 0):
        print >> ERROR_FILE, 'Missing one or more required properties, please check property file'
        return None
    
    # return an instance of PortalProperties
    return PortalProperties(properties[CGDS_DATABASE_HOST],
                            properties[CGDS_DATABASE_NAME],
                            properties[CGDS_DATABASE_USER],
                            properties[CGDS_DATABASE_PW],
                            properties[GOOGLE_ID],
                            properties[GOOGLE_PW],
                            properties[CGDS_USERS_SPREADSHEET],
                            properties[CGDS_USERS_WORKSHEET])

# ------------------------------------------------------------------------------
# adds new users from the google spreadsheet into the cgds portal database
# returns new user map if users have been inserted, None otherwise

def manage_users(cursor, worksheet_feed, portal_name):

    # get map of current portal users
    print >> OUTPUT_FILE, 'Getting list of current portal users'
    current_user_map = get_current_user_map(cursor)
    if current_user_map is not None:
        print >> OUTPUT_FILE, 'We have found %s current portal users' % len(current_user_map)
    else:
        print >> OUTPUT_FILE, 'Error reading user table'
        return None

    # get list of new users and insert
    print >> OUTPUT_FILE, 'Checking for new users'
    new_user_map = get_new_user_map(worksheet_feed, current_user_map, portal_name)
    if (len(new_user_map) > 0):
        print >> OUTPUT_FILE, 'We have %s new user(s) to add' % len(new_user_map)
        success = insert_new_users(cursor, new_user_map.values())
        if success:
            print >> OUTPUT_FILE, 'Successfully inserted new users in database'
            return new_user_map
        else:
            print >> OUTPUT_FILE, 'Error inserting new users in database'
            return None
    else:
        print >> OUTPUT_FILE, 'No new users to insert, exiting'
        return None

# ------------------------------------------------------------------------------
# updates user study access
def update_user_authorities(cursor, worksheet_feed, portal_name):

        # get map of current portal users
        print >> OUTPUT_FILE, 'Getting list of current portal users from spreadsheet'
        all_user_map = get_new_user_map(worksheet_feed, {}, portal_name)
        if all_user_map is None:
                return None;
        print >> OUTPUT_FILE, 'Updating authorities for each user in current portal user list'
        for user in all_user_map.values():
                worksheet_authorities = set(user.authorities)
                db_authorities = set(get_user_authorities(cursor, user.openid_email))
                try:
                        cursor.executemany("insert into authorities values(%s, %s)",
                                           [(user.openid_email, authority) for authority in worksheet_authorities - db_authorities])
                        #cursor.executemany("delete from authorities where email = (%s) and authority = (%s)",
                        #                   [(user.openid_email, authority) for authority in db_authorities - worksheet_authorities])
                except MySQLdb.Error, msg:
                        print >> ERROR_FILE, msg

# ------------------------------------------------------------------------------
# displays program usage (invalid args)

def usage():
    print >> OUTPUT_FILE, 'importUsers.py --properties-file [properties file] --send-email-confirm [true or false]'

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
    google_login(portal_properties.google_id, portal_properties.google_pw)

    google_spreadsheets = portal_properties.google_spreadsheet.split(';')

    for google_spreadsheet in google_spreadsheets:
        print >> OUTPUT_FILE, 'Importing ' + google_spreadsheet + ' ...'

        worksheet_feed = get_worksheet_feed(google_spreadsheet,
                                            portal_properties.google_worksheet)

        # the 'guts' of the script
        new_user_map = manage_users(cursor, worksheet_feed, PORTAL_NAME[google_spreadsheet])

        # update user authorities
        update_user_authorities(cursor, worksheet_feed, PORTAL_NAME[google_spreadsheet])

        # sending emails
        if new_user_map is not None:
            if send_email_confirm == 'true':
                for new_user_key in new_user_map.keys():
                    new_user = new_user_map[new_user_key]
                    print >> OUTPUT_FILE, ('Sending confirmation email to new user: %s at %s' %
                                           (new_user.name, new_user.inst_email))
                    send_mail([new_user.inst_email],
                              MESSAGE_SUBJECT[google_spreadsheet],
                              MESSAGE_BODY[google_spreadsheet])


    # clean up
    cursor.close()
    connection.commit()
    connection.close()


# ------------------------------------------------------------------------------
# ready to roll

if __name__ == '__main__':
    main()
