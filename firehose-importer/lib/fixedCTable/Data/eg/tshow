#!/usr/bin/perl 

=pod

tshow

A little utility that uses Data::CTable to read and display the
contents of any tabular data file(s) regardless of file format or line
endings.

This tool is part of the Data::CTable distribution.

Copyright (c) 1995-2002 Chris Thorman.  All rights reserved.  

This program is free software; you can redistribute it and/or modify
it under the same terms as Perl itself.

See Data::CTable home page for further info:

	http://christhorman.com/projects/perl/Data-CTable/

=cut

use strict;

foreach (map {glob} @ARGV) 
{
	print "$_\n";

	use Data::CTable; 
	(   Data::CTable->new($_))->out();
}

