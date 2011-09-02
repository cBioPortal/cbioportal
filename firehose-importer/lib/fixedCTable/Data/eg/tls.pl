#!/usr/bin/perl
## Emacs: -*- tab-width: 4; -*-

=pod

tls [path path path] [-options]

Call this perl script with --help for a detailed help message.

----------

This script is part of the Data::CTable distribution.

Most of the script's work is done by the subclassed CTable object in
the perl module Data/CTable/Lister.pm.

See the source code of module for detailed documentation of this
script's behavior, including the usage() message that prints when
--help is requested.

You can use or subclass Data::CTable::Lister directly if you'd like to
use any aspect of this script's functionality in your own perl tools.

Copyright (c) 1995-2002 Chris Thorman.  All rights reserved.

This program is free software; you can redistribute it and/or modify
it under the same terms as Perl itself.

See Data::CTable home page for further info:

	http://christhorman.com/projects/perl/Data-CTable/

=cut

use strict;

use 	 Data::CTable::Listing;
exit	!Data::CTable::Listing->script();

