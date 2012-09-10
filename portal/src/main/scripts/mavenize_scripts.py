#!/usr/bin/python

# Gideon Dresdner
# dresdnerg@cbio.mskcc.org
# September 2012
#
# Some directories changed in the process of mavenization, plus we now use
# maven to link libraries for runtimer.  This script makes these changes for
# all perl scripts it is given

import re
import sys, os

regexp = "(org.mskcc.cbio.cgds.scripts.*)(@ARGV)"

for file in sys.argv:

    # exceptions to the rule, importGistic was done manually as a test
    if file == "mavenize.py" or \
            file == "importGistic.pl" or \
            file == "importProfileData.pl" or \
            os.path.isdir(file):
        continue

    if '.pl' not in file:
        print 'skipping ', file
        continue

    f = open(file, 'r+')
    new_f = ''

    # mavenized command
    new_cmd = '''my $cmd = join(' ', @ARGV);
    $cmd = 'mvn -f $PORTAL_HOME/pom.xml exec:java -Dexec.mainClass="org.mskcc.cbio.cgds.scripts.ImportGisticData" -Dexec.args="' . $cmd . '"';
    system($cmd);'''

    for line in f.readlines():
        #print file,":", re.findall(regexp, line)
        java_class = re.findall(regexp, line)
        if java_class != []:
            java_class = java_class[0][0].strip()

            new_cmd = \
'''$cmd = join(' ', @ARGV);
$cmd = \'mvn -f $PORTAL_HOME/pom.xml exec:java -Dexec.mainClass=\"%s\" \
-Dexec.args="' . $cmd . '"';
system($cmd);\n''' %(java_class)
            new_f += new_cmd
        else:
            new_f += line

    # overrwrite file with new file
    f.seek(0)
    f.write(new_f)
    f.truncate()
    f.close()

    #print new_f

