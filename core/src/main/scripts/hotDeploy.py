#! /usr/bin/env python

# ------------------------------------------------------------------------------
# Script which hot deploys cbio portal fixes.  Script assumes
# properties file argument lives within $PORTAL_HOME/portal.  This
# script will clobber any existing portal.properties file within $PORTAL_HOME/portal.

# ------------------------------------------------------------------------------
# imports
import os
import sys
import getopt
import subprocess
from mercurial import commands, ui, hg

# ------------------------------------------------------------------------------
# globals

# some file descriptors
ERROR_FILE = sys.stderr
OUTPUT_FILE = sys.stdout

# portal home
PORTAL_HOME = os.environ['PORTAL_HOME']
if PORTAL_HOME is None:
	print >> OUTPUT_FILE, 'PORTAL_HOME environment variable is not set, exiting.'
	sys.exit(1)
PORTAL_PROJECT = PORTAL_HOME + os.sep + "portal"

WAR_FILE_DEST = "/srv/www/sander-tomcat/tomcat6/webapps/"

# fields in credentials - should match portal portal.properties
CGDS_DATABASE_USER = 'db.user'
CGDS_DATABASE_PW = 'db.password'
BITLY_USER = 'bitly.user'
BITLY_KEY = 'bitly.api_key'
CGDS_USERS_SPREADSHEET = 'users.spreadsheet'

# ------------------------------------------------------------------------------
# class definitions

class Credentials(object):
	def __init__(self,
				 cgds_database_user, cgds_database_pw,
				 bitly_user, bitly_key):
		self.cgds_database_user = cgds_database_user
		self.cgds_database_pw = cgds_database_pw
		self.bitly_user = bitly_user
		self.bitly_key = bitly_key

# ------------------------------------------------------------------------------
# functions

# ------------------------------------------------------------------------------
# parse credentials

def get_portal_credentials(credentials):

	properties = {}
	credentials_file = open(credentials, 'r')
	for line in credentials_file:
		line = line.strip()
		# skip line if its blank or a comment
		if len(line) == 0 or line.startswith('#') or line.startswith(CGDS_USERS_SPREADSHEET):
			continue
		# store name/value
		property = line.split('=')
		if (len(property) != 2):
			print >> ERROR_FILE, 'Skipping invalid entry in property file: ' + line
			continue
		properties[property[0]] = property[1].strip()
	credentials_file.close()

    # error check
	if (CGDS_DATABASE_USER not in properties or len(properties[CGDS_DATABASE_USER]) == 0 or
		CGDS_DATABASE_PW not in properties or len(properties[CGDS_DATABASE_PW]) == 0 or
		BITLY_USER not in properties or len(properties[BITLY_USER]) == 0 or
		BITLY_KEY not in properties or len(properties[BITLY_KEY]) == 0):
		print >> ERROR_FILE, 'Missing one or more required properties, please check property file'
		return None

	# return an instance of Credentials
	return Credentials(properties[CGDS_DATABASE_USER],
					   properties[CGDS_DATABASE_PW],
					   properties[BITLY_USER],
					   properties[BITLY_KEY])

# ------------------------------------------------------------------------------
# build war with given properties file

def deploy_war(host, user):

	# war file to scp
	war_file = PORTAL_PROJECT + os.sep + "build" + os.sep + "war" +	os.sep + "*.war"
	# setup the scp command & execute
	scp_command = "scp %s %s@%s:%s" % (war_file, user, host, WAR_FILE_DEST)
	scp_process = subprocess.call(scp_command, shell=True)
	if scp_process != 0:
		print >> ERROR_FILE, "Error secure copying file, aborting."
		sys.exit(1)

# ------------------------------------------------------------------------------
# build war with given credentials & portal properties file

def build_war(portal_credentials, portal_properties):

	# setup portal.properties
	portal_properties = PORTAL_PROJECT + os.sep + "portal.properties"
	if os.path.exists(portal_properties):
		print >> OUTPUT_FILE, "Clobbering %s" % portal_properties

	# we are going to create a new portal.properties file using
	# portal_properties as the template and get credentials from portal_credentials
	portal_properties_file = open(portal_properties, 'w')
	portal_properties_file = open(portal_properties, 'r')
	for line in portal_properties_file:
		new_line = line
		if line.startswith(CGDS_DATABASE_USER):
			new_line = '%s=%s\n' % (CGDS_DATABASE_USER, portal_credentials.cgds_database_user)
		elif line.startswith(CGDS_DATABASE_PW):
			new_line = '%s=%s\n' % (CGDS_DATABASE_PW, portal_credentials.cgds_database_pw)
		elif line.startswith(BITLY_USER):
			new_line = '%s=%s\n' % (BITLY_USER, portal_credentials.bitly_user)
		elif line.startswith(BITLY_KEY):
			new_line = '%s=%s\n' % (BITLY_KEY, portal_credentials.bitly_key)
		portal_properties_file.write(new_line);
	portal_properties_file.close()
	portal_properties_file.close()

	# run ant
	os.chdir(PORTAL_PROJECT)

	ant_process = subprocess.call('ant clean war', shell=True)
	if ant_process != 0:
		print >> ERROR_FILE, "Error building war file, aborting."
		sys.exit(1)

# ------------------------------------------------------------------------------
# update repos to proper tag

def update_repos(rev):

	try:
		print >> OUTPUT_FILE, 'accessing repository: %s' % PORTAL_HOME
		repos = hg.repository(ui.ui(), PORTAL_HOME)
		print >> OUTPUT_FILE, 'updating to revision: %s' % rev
		commands.update(ui.ui(), repos, rev=rev, check=True)
	except Exception, e:
		print >> ERROR_FILE, "Error: %s" % e
		print >> ERROR_FILE, "Aborting."
		sys.exit(1)

# ------------------------------------------------------------------------------
# displays program usage (invalid args)

def usage():
	print >> OUTPUT_FILE, ('hotDeploy.py --credentials [credentials] ' +
						   '--portal-properties-file [build props] ' + 
						   '--rev [tag] --host [deploy location] --user [user on host]')

# ------------------------------------------------------------------------------
# the big deal main.

def main():

	# parse command line options
	try:
		opts, args = getopt.getopt(sys.argv[1:], '', ['credentials=', 'portal-properties=', 'rev=', 'host=', 'user='])
	except getopt.error, msg:
		print >> ERROR_FILE, msg
		usage()
		sys.exit(1)

    # process the options
	credentials = ''
	portal_properties = ''
	rev = ''
	host = ''
	user = ''

	for o, a in opts:
		if o == '--credentials':
			credentials = a
		elif o == '--portal-properties':
			portal_properties = a
		elif o == '--rev':
			rev = a
		elif o == '--host':
			host = a
		elif o == '--user':
			user = a
	if (credentials == '' or portal_properties == '' or rev == '' or host == '' or user == ''):
		usage()
		sys.exit(1)

    # check existence of credentials
	if not os.path.exists(credentials):
		print >> ERROR_FILE, 'credentials file does not exist: ' + credentials
		sys.exit(2)

    # check existence of portal properties
	if not os.path.exists(portal_properties):
		print >> ERROR_FILE, 'portal properties does not exist: ' + portal_properties
		sys.exit(2)

	# get portal credentials
	print >> OUTPUT_FILE, 'Reading credentials file: ' + credentials
	portal_credentials = get_portal_credentials(credentials)
	if not portal_credentials:
		print >> ERROR_FILE, 'Error reading %s, exiting' % credentials
		sys.exit(2)

	# lets update our portal repos to proper tag and build a new war
	print >> OUTPUT_FILE, 'Updating mercurial repository to rev: %s' % rev
	update_repos(rev)

	# build war
	print >> OUTPUT_FILE, 'Building war file using properties file:	%s' % portal_properties
	build_war(portal_credentials, portal_properties)

	# deploy
	print >> OUTPUT_FILE, 'Deploying war file to: %s@%s:%s' % (user, host, WAR_FILE_DEST)
	deploy_war(host, user)

# ------------------------------------------------------------------------------
# ready to roll

if __name__ == '__main__':
    main()
