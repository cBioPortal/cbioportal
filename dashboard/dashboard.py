import feedparser
import urllib2

d = feedparser.parse('http://ec2-50-19-51-200.compute-1.amazonaws.com:8080/rssLatest')
buildStatus = d.entries[0].title
print "<?php"
print "$portal_build_status=",
if (buildStatus.find("broken") > -1):
	print "'red'",
else:
	print "'green'",
print ";"

d = feedparser.parse('http://code.google.com/feeds/p/cbio-cancer-genomics-portal/hgchanges/basic')
print "$portal_latest_code=\"<ul class='ticker' id='latest_ticker'>",
for i in range(0, 3):
	lastCheckInTitle = d.entries[i].title
	print ("<li>" + lastCheckInTitle + "</li>"),
print "</ul>\";"

response = urllib2.urlopen('http://code.google.com/p/cbio-cancer-genomics-portal/issues/csv')
text = response.read().strip()
lines = text.split("\n")
print "$portal_num_bugs=",
print ("%d" % (len(lines) -1)),
print ";"

print "?>"