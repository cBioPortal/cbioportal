#!/usr/bin/perl -pi.bak -0000

## fix_html.pl

## A little perl script to clean up some of the things I don't like
## about what pod2html does to my pod.

## This script is run on each .html file produced by pod2html as part
## of the make process.

## Remove bizarre hyperlinks around some subroutine calls.

s{<A HREF="#.*?">(.*?)</A>}{$1}gs;

## Remove anchors since we no longer have internal links.

s{<A NAME=".*?">(.*?)</A>}{$1}gs;	

## Change the annoying ``...'' to use real double-quotes.

s{``(.+?'?)''}{&\#147\;$1&\#148\;}g;

## Fix broken URL-detection that messes up question-marks in URLs.

s{<A HREF="(.*?)&amp">\1&amp</A>;(.*?)</PRE>}
 {<A HREF="$1&$2">$1&amp;$2</A></PRE>}g;
