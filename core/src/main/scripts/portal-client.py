#! /usr/bin/python

#
# This scipt is a test of portal/cgds access from external client 
# while spring-security is in place.
#
# The client takes two credentials: a valid webservice url, and a user credential
# for authentication
#
# example url: http://miso-dev.cbio.mskcc.org:58080/oauth-portal/webservice.do?cmd=getCancerTypes
# example credential: B039E875B4BFD4C4493B7C1323F510F6
#

# ------------------------------------------------------------------------------
# imports

import sys
import getopt
import urllib
import urllib2

# ------------------------------------------------------------------------------
# globals

ERROR_FILE = sys.stderr
OUTPUT_FILE = sys.stdout

# ------------------------------------------------------------------------------
# functions

# ------------------------------------------------------------------------------
# executes a call to the portal
def execute_portal_call(url, credential):

    try:
        urlOpener = urllib2.build_opener()
        urlOpener.addheaders.append(('Cookie', 'JSESSIONID=' + credential))
        response = urlOpener.open(url)
    except IOError:
        print >> ERROR_FILE, "error fetching url: " + url
        return

    print >> OUTPUT_FILE, response.read()

# ------------------------------------------------------------------------------
# The big deal main.

def main():

    help_text = 'portal-client.py --url [properly constructed portal url] --credential [user credential]'

	# parse command line options
    try:
        opts, args = getopt.getopt(sys.argv[1:], "", ["url=", "credential="])
    except getopt.error, msg:
        print >> ERROR_FILE, help_text
        sys.exit(2)

	# process the options  
    url = ''
    credential = ''
    for o, a in opts:
        if o == "--url":
            url = a
        elif o == "--credential":
            credential = a

    if url == '' or credential == '':
        print >> ERROR_FILE, help_text
        sys.exit(2)

    execute_portal_call(url, credential)


# ------------------------------------------------------------------------------
# lets shoot this f'in film...
# ------------------------------------------------------------------------------
if __name__ == '__main__':
	main()
