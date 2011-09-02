#!/usr/bin/perl
## Emacs: -*- tab-width: 4; -*-

use strict;

package   Data::CTable::Listing;	

use vars qw($VERSION);				$VERSION = '1.03';

=pod

=head1 NAME

Data::CTable::Listing - CTable holding file and directory listings

=head1 SYNOPSIS

	## Call from a shell script:
	use 	 Data::CTable::Listing;
	exit	!Data::CTable::Listing->script();

This is an OO implementation of the guts of the "tls" perl script that
comes with the Data::CTable distribution.

Please see Listing.pm for the full usage() message, or run the tls
perl script with the --help option.

=head1 FURTHER INFO

See the Data::CTable home page:

	http://christhorman.com/projects/perl/Data-CTable/

=head1 AUTHOR

Chris Thorman <chthorman@cpan.org>

Copyright (c) 1995-2002 Chris Thorman.  All rights reserved.  

This program is free software; you can redistribute it and/or modify
it under the same terms as Perl itself.

=cut
{};

use       Data::CTable::Script;  use vars qw(@ISA);
@ISA = qw(Data::CTable::Script);

sub usage_message
{
	my $this = shift();
	my ($ScriptName) = @_;

	return(do{(my $doc = << 'END') =~ s/_SCR_/$ScriptName/g; $doc}); 
	
_SCR_ [path path path...] [options]

_SCR_ is a command-line tool similar to the Unix "ls" (directory
listing) command.  It should run on any platform that has perl.

It uses Data::CTable (Data::CTable::Listing sublcass) and
Data::ShowTable to build in memory a table of information about
specified files and directories and then output it to the console or a
file.

Each command line path must be either a file or directory to be
included in the listing.

If no paths are specified at all, the current directory is assumed.

For each path argument that is a directory, an entry for each file in
that directory will also be included in the listing unless the
--nochildren option is specified.  (You may suppress the top-level
item itself by specifying --notop).  Other than the default single
level of children listed, recursion is not done unless you specify
--recurse.

Options may appear in any order, before, after, or intermixed with the
path arguments.  See below for options.

In addition to the Path field which contains the absolute or relative
path name of each listed item, _SCR_ also derives a number of
additional fields which you may include in the listing.

Available fields are:

    Main path field:

	Path         Absolute or relative path of specified item

    Fields from stat($Path) (see --fields, --all, or --info):
             
	Device   0 dev     device number of filesystem                                        
	INode    1 ino     inode number                                                       
	Mode     2 mode    file mode  (type and permissions)                                  
	NLink    3 nlink   number of (hard) links to the file                                 
	UID      4 uid     numeric user ID of file's owner                                    
	GID      5 gid     numeric group ID of file's owner                                   
	RDev     6 rdev    the device identifier (special files only)                         
	Size     7 size    total size of file, in bytes                                       
	ATime    8 atime   last access time in seconds since the epoch                        
	MTime    9 mtime   last modify time in seconds since the epoch                        
	CTime   10 ctime   inode change time (NOT creation time)
	BlkSize 11 blksize preferred block size for file system I/O                           
	Blocks  12 blocks  actual number of blocks allocated                                  

    Fields from localtime($MTime) (see --fields, --all, or --localtime):
                                  (see man ctime or struct tm):

	Sec   0 tm_sec   The number of seconds after the minute, normally in 
	                 the range 0 to 59, but can be up to 61 to allow for
	                 leap seconds.
	Min   1 tm_min   The number of minutes after the hour, in the range 0 to 59.
	Hour  2 tm_hour  The number of hours past midnight, in the range 0 to 23.  
	Day   3 tm_mday  The day of the month, in the range 1 to 31.               
	TMon  4 tm_mon   The number of months since January, in the range 0 to 11. 
	TYear 5 tm_year  The number of years since 1900 (range   69 to  138)
	WDay  6 tm_wday  The number of days since Sunday, in the range 0 to 6.     
	YDay  7 tm_yday  The number of days since January 1, in the range 0 to 365.
	IsDST 8 tm_isdst A flag that indicates whether daylight saving
	                 time is in effect at the time described.  The
	                 value is positive if daylight saving time is in
	                 effect, zero if it is not, and negative if the
	                 information is not available.

    Fields from File::Basename (see --fields, --all, or --base):

	Dir        Directory component of Path
	Base       Base component of file name (without the Ext)
	Ext        Extension (any non-. characters after last . plus the . itself)

    Other derived fields (see --fields, --all, or --derived):

	File       Filename component of Path (= Base + Ext)
	Type       File or directory

	Perms      Permissions (from Mode), specified as an octal string  
	Owner      Unix owner name: getpwuid(UID) (empty on Win)                         
	Group      Unix owner name: getgrgid(GID) (empty on Win)                       

	Mon        TMon  + 1                          (range    1 to   12)
	Year       TYear + 1900                       (range 1900 to 2038)

	RTime      ModTime as human-readable string (localtime(MTIME)."") 
	Stamp      String-sortable semi-numeric time stamp (based on MTIME)   

Field names are always built and output using mixed-case as indicated.

However, case is ignored by the --fields or --sort options (you may
specify the field names in all lower-case, for example).  You may also
specify any shorter form of any field name as long as it is not
ambiguous.  (For example "fi" for File or "pa" for Path.)

Available command-line options are:

    Miscellaneous
    -------------		

	Ignore all other options and print this help message instead. 

	--help
	-h

	Don't turn off progress() calls in the table.

	--verbose
	-v


    Controlling files (rows) included in listing
    --------------------------------------------

	Don't include children of specified directories.

	--nochildren
	--nochild
	--noc

	Don't list specified directories themselves (but do list their
	children unless --noc).

	--notop
	--not

	Recursively include all sub-directories in listings (overrides
	--nochildren).

	--recurse
	-r

	Restrict listing to only directories (d) or files (f).

	--type=d
	-ty=d

	--type=f
	-ty=f


    Setting which fields (columns) to include in output
    ---------------------------------------------------

	Use the options below to specify the fields to be listed in the
	output table, in order.  If no fields are specified, the following
	fields will be used:

	Path Owner Group Size RTime  (except Path when --nopath)

	--fields f1,f2
	--field f1 --field f2
	-f=f1,f2,f3
	-f="f1 f2 f3"

	Force all available fields to be included in output (overrides --fields).

	--all
	-a

	Force all localtime-derived fields to be included in output (adds
	any not yet specified in --fields).

	--localtime
	-l

	Force all stat-derived fields to be included in output (adds
	any not yet specified in --fields).

	--info
	-i

	Force all File::Basename-derived fields to be included in output
	(adds any not yet specified in --fields).

	--base
	-b

	Force all other derived fields to be included in output.

	--derived
	-de

	Don't automatically put the Path as first field in output (but do
	include it if no other fields are specified).

	--nopath
	--nop


    Sort order
    ----------

	Specify ordered list of field names to use to sort and sub-sort
	the output.  

	--sort f1 --sort f2
	--sort f1,f2
	-s=f1,f2,f3
	-s="f1 f2 f3"

	If none are specified, the table is sorted ascending by Path.

	In fact, Path is always the LAST field in the sort order, whether
	you specify it explicity or not.  This guarantees that the sort
	order is always unambiguous since no two items will have the same
	Path.

	In the sort order, each field will be sorted ascending unless a
	"+" is appended to its name (mnemonic: + sorts highest items
	first).  For example:

	-s=Size+
	-s=WDay+,Size+

    Controlling the output method and its arguments
    --------------------------------------------

	Specify name of method Data::CTable::Listing method to call in
	order to output data, and any arguments to that method, separated
	by commas, spaces or by multiple --output or -o options, .

	Default is the "format" method, which prints a table using the
	Data::ShowTable, which of course must be installed to work.  

	The write() method is another alternative.  With no arguments, it
	will write to STDOUT (and may be redirected to a file or other
	script). With a single argument, it will write to a file by that
	name.  Multiple arguments are processed as named-parameter
	arguments; and any documented named-parameter option to
	Data::CTable::write() may be specified this way.

	Note: the default delimiter character for write() is comma.  To
	get tab-delimited output, see the --tabs option below.

	--output=write
	-o=write,foo.txt
	-o=write,_FileName,foo.txt,_LineEnding,mac,HeaderRow,0

	Shortcut to specify using the write method to write to STDOUT.
	This is equivalent to "-o=write".

	--write
	-w		

	Specify tab-delimited output.  This forces the "write" method
	instead of the "format" method to be used to output the table.

	This is equivalent to specifying '--output=write,_FDelimiter,"\t"'
	except it's briefer and easier to get your shell script to accept
	it.

	--tabs
	-ta		

	Suppress a header row showing field names.  This forces the
	"write" method instead of the "format" method to be used to output
	the table.

	This is equivalent to specifying '--output=write,_HeaderRow,0'.

	--noheader
	-noh

	Specify line-endings.  Endings by default will be whatever "\n" is
	on the current system.  To force them, specify one of the
	following options.  These options force the "write" method and are
	equivalent to specifying equivalent to specifying
	'--output=write,_LineEnding,mac', ...unix, ...dos, etc.

	--mac
	-m
	--unix
	-u
	--dos
	-do

This script is part of the Data::CTable distribution.

Most of the script's work is done by the subclassed CTable object in
Data::CTable::Lister.

You can use or subclass Data::CTable::Lister directly if you'd like to
use any aspect of this script's functionality in your own perl tools.

See that module for detailed documentation of this script's behavior,
including the usage() message that prints when --help is requested.

Copyright (c) 1995-2002 Chris Thorman.  All rights reserved.

This program is free software; you can redistribute it and/or modify
it under the same terms as Perl itself.

See Data::CTable home page for further info:

	http://christhorman.com/projects/perl/Data-CTable/

END
{};
}

sub optionspec
{
	my $Class	= shift;

	my $Spec	= {(
					##### Common options from the parent class..
					
					%{$Class->SUPER::optionspec},

					##### Listed here for redundancy...

					## Common options
					"help"			=>	0 ,
					"verbose"		=>	0 ,
					
					## Filtd list
					"fields=s"		=>	[],
					
					## Sorting
					"sort=s"		=>	[],
					
					## Output method
					"output=s"		=>	[],

					##### Plus the following options unique to this script:
					
					## Data::CTable::Listing-specific options
					
					# Options controlling which files are listed
					"notop"				=>		0,
					"nochildren"		=>		0,
					"recurse"			=>		0,
					"type=s"			=>		"",

					# Options controlling which fields are shown
					"all"				=>		0,
					"localtime"			=>		0,
					"info"				=>		0,
					"base"				=>		0,
					"derived"			=>		0,
					"nopath"			=>		0,

					# Output options
					"write"				=>		0,
					"tabs"				=>		0,
					"noheader"			=>		0,
					"mac"				=>		0,
					"unix"				=>		0,
					"dos"				=>		0,

					)};
	
	return($Spec);
}

=pod

=head1 METHODS
	
	$Class->run()

Main entry point for the "tls" or "tls.pl" script included with the
Data::CTable distribution.

=cut
{};	

sub run
{
	my $Class			= shift;
	my ($Opts)			= @_;
	
	use Data::CTable qw(path_info);
	
	## Create an empty options hash in case we didn't get one.
	$Opts	||= {};

	## Find the paths and create a new file table using them.
	my $this = $Class->make_file_table($Opts);
	
	## Run the appropriate output method.
	return($this->make_output());
}

=pod

	
	$Class->make_file_table()

Instantiates a table object, gets and inserts file listing and other
fields, processes user's arguments, and generates output.

=cut
{};	

sub make_file_table
{
	my $Class			= shift;
	my ($Opts)	= @_;
	
	use Data::CTable qw(min max);
	
	## Make a table...
	##	Default sort order is by path
	##  Path sort is done as Text (case-insensitive)
	##  All other fields sorted (and justified) as Integer by default
	##  Max column width is max path length
	##  Silent sorting
	##  Path field is all paths specified
	##  Fields S00..S12 contain results of calling stat()

	my $t = $Class->new
	({
		(		_SortOrder			=>	[qw(Path)]),
		(		_SortSpecs			=>	{(	 (Path	=> {SortType=>'Text'  }),

											 (File	=> {SortType=>'String'}),
											 (Type	=> {SortType=>'String'}),

											 (Dir	=> {SortType=>'String'}),
											 (Base	=> {SortType=>'String'}),
											 (Ext	=> {SortType=>'String'}),

											 (Perms	=> {SortType=>'String'}),
											 (Owner	=> {SortType=>'String'}),
											 (Group	=> {SortType=>'String'}),

											 (RTime	=> {SortType=>'String'}),
											 (Stamp	=> {SortType=>'String'}),

											 )}),
		(		_DefaultSortType	=>	'Integer'),
		($Opts->{verbose} ? () : 
		 (		_Progress			=>	0)),
		(		_Opts				=>	$Opts),
		(		Path				=>	[]),
	});
	
	## Print a progress mess that will be ignored unless we're in verbose mode.
	$t->progress("Searching..."); 

	## Expand/filter, etc. the caller's specified paths and/or glob
	## expressions.
	my $Paths = $Class->process_path_args($Opts);

	## Find longest file path.
	my $Longest; foreach (@$Paths) {$Longest = max($Longest, length)};

	## Set the MaxWidth to something that will accommodate all
	## possible fields (longer of path, RTime)

	$t->{_MaxWidth}			=	max(25, $Longest);	## 25 = allow for RTime field

	## Set the Path column
	$t->col(Path => $Paths);

	## Build the "stat" fields...
	foreach my $FNum (-1..$#$Paths)
	{
		## Get stats for the file...
		my $Stats = [stat($Paths->[$FNum])];
		foreach my $n ('00'..'12')
		{
			## First time through (and only time through if no records
			## present), just make the columns.
			$t->col("S$n"), next if $FNum == -1;

			## Fill stat values into pre-sized vectors in the table.
			$t->col("S$n")->[$FNum] = $Stats->[$n];
		}
	}

	## Fix the order of Path, S00..S12 fields.
	$t->fieldlist_freeze();

	## Change some field names.
	$t->col_rename(qw(	S00	Device
						S01	INode
						S02	Mode
						S03	NLink
						S04	UID
						S05	GID
						S06	RDev
						S07	Size
						S08	ATime
						S09	MTime
						S10	CTime
						S11	BlkSize
						S12	Blocks
						));

	## Build the "localtime" fields...
	my $Times = $t->col('MTime');
	foreach my $FNum (-1..$#$Times)
	{
		## Get stats for the file...
		my $TInfo = [localtime($Times->[$FNum])];
		foreach my $n ('00'..'08')
		{
			## First time through (and only time through if no records
			## present), just make the columns.
			$t->col("T$n"), next if $FNum == -1;

			## Fill stat values into pre-sized vectors in the table.
			$t->col("T$n")->[$FNum] = $TInfo->[$n];
		}
	}

	## Change some field names.
	$t->col_rename(qw(	T00	Sec
						T01	Min
						T02	Hour
						T03	Day
						T04	TMon
						T05	TYear
						T06	WDay
						T07	YDay
						T08	IsDST
						));

	## Build the "basename" fields...
	foreach my $FNum (-1..$#$Paths)
	{
		## Get stats for the file...
		use File::Basename qw(fileparse);
		my $FInfo = [(fileparse($Paths->[$FNum], '\.[^\.]+'))[1,0,2]] 
			unless $FNum == -1;
		
		foreach my $n ('00'..'02')
		{
			## First time through (and only time through if no records
			## present), just make the columns.
			$t->col("F$n"), next if $FNum == -1;

			## Fill stat values into pre-sized vectors in the table.
			$t->col("F$n")->[$FNum] = $FInfo->[$n];
		}
	}

	## Change some field names.
	$t->col_rename(qw(	F00	Dir
						F01	Base
						F02	Ext
						));

	## Add a column showing the full file name.
	$t->col(File => $t->calc(sub{"$main::Base$main::Ext"}));
	$t->col(Type => $t->calc(sub{-d $main::Path ? "D" : "F"}));

	## Add a Perms field showing permission component of (stat)[2]
	## formatted as a 4-digit octal number.

	$t->col(Perms => [map {sprintf("%04o", $_ & 0777)} @{$t->col('Mode')}]);

	## Add columns translating the UID and GID into their string form.

	$t->col(Owner => [map {((eval{getpwuid($_)})[0])   } @{$t->col('UID' )}]);
	$t->col(Group => [map {((eval{getgrgid($_)})[0])   } @{$t->col('GID' )}]);

	## Add a column showing the full file name.
	$t->col(Mon  => $t->calc(sub{$main::TMon  +    1}));
	$t->col(Year => $t->calc(sub{$main::TYear + 1900}));

	## Add some time stamps -- one human-readable and one computer-sortable.
	$t->col(RTime => $t->calc(sub{localtime($main::MTime).""}));
	use POSIX;
	$t->col(Stamp => $t->calc(sub{strftime("%Y%m%d%H%M%S", localtime($main::MTime))}));
	
	return($t);
}

=pod

	$Class->process_path_args()

Generates the file listing by interpreting the file-related
command-line options.

=cut
{};	

sub process_path_args
{
	my $Class			= shift;
	my ($Opts)			= @_;
	
	## Get the paths option.
	my $Paths = $Opts->{args};
	
	## Get details about paths on the current platform
	my ($Sep, $Up, $Cur) = @{path_info()}{qw(sep up cur)};
	
	## Default file list is the current directory.
	$Paths	= [$Cur] unless (ref($Paths) eq 'ARRAY') && @$Paths;
	
	## Glob the paths to support shell-like wildcards on all
	## platforms or when calling from a script.

	$Paths = [map {my $x=[glob $_]; @$x ? @$x : $_} @$Paths];

	## Validate / canonicalize path arguments.
	$Paths = [map 
			  {
				  ## Ensure trailing $Sep on dir names
				  ($_) =~ s{([^\Q$Sep\E])$ }{$1$Sep}ox if -d;
				  
				  ## Filter/warn re: existence of items we're going to list
				  (-e $_ ? 
				   $_ :
				   do{$Class->warn("No such file or directory '$_'"); ()});
				  
			  } @$Paths];
	
	my $Recurse		= $Opts->{recurse};
	my $NoTop		= $Opts->{notop};
	my $NoChildren	= $Opts->{nochildren} && !$Recurse;
	my $Type		= $Opts->{type};

	## Expand directory paths into their constituent components.
	$Paths	= [map 
			   {
				   my $Path = $_;
				   (-d $Path ? 
					
					## Get a listing with $Path prepended to each element
					@{$Class->list_dir($Path, $Sep, $Cur, $Recurse, $NoTop, $NoChildren)} : 
					
					## Otherwise it's just a bare file name.
					$Path
					);
			   } @$Paths];

	$Paths	= [grep {-d} @$Paths] if $Type eq 'd';
	$Paths	= [grep {-f} @$Paths] if $Type eq 'f';
	
	return($Paths);
}

=pod

	$t->make_output()

Parses and processes the sorting, fieldlist, and output options, then
calls the appropriate output method.  Returns the scalar buffer
reference returned by that method, if any; otherwise a ref to an empty
buffer.

=cut
{};	

sub make_output
{
	my $this			= shift;

	## Assume parsed Opts have been stored in the object in the _Opts slot.
	my $Opts			= $this->{_Opts}	|| {};

	## Retrieve various output options
	my $Write		= $Opts->{write};
	my $Tabs		= $Opts->{tabs};
	my $Mac			= $Opts->{mac}	&& 'mac';
	my $Unix		= $Opts->{unix}	&& 'unix';
	my $Dos			= $Opts->{dos}	&& 'dos';
	my $NoHeader	= $Opts->{noheader};

	## Retrieve the output --fields and --sort fields requested by the user.
	my $Fields	  		= $Opts->{fields}	|| [];
	my $Sorts	  		= $Opts->{sort  }	|| [];

	## First extract the specified sort directions into a hash so we can pull them in later.
	my $SortDirs		= {}; @$SortDirs{map {(/(\w+)/)[0]} @$Sorts} = map {(/\w+(\W)/)[0]} @$Sorts;
	my $Sorts			= [map {(/(\w+)/)[0]} @$Sorts];

	## Allow for weird casing in the user's field lists...
	my $FieldsAll		= $this->fieldlist_all();
	my $FieldCaseMap	= {}; @$FieldCaseMap {map {lc} @$FieldsAll} = @$FieldsAll;

	## Allow for shortened but unambiguous forms of field names to be used.
	foreach my $Field (@$FieldsAll)
	{
		foreach (1..length($Field) - 1)
		{
			my $Str = lc(substr($Field, 0, $_));
			(exists($FieldCaseMap->{$Str}) ?		
			 (delete $FieldCaseMap->{$Str}) :		## Existing therefore ambiguous; disallow
			 (       $FieldCaseMap->{$Str} = $Field));	## Not existing; add.
		}
	}
	
	## Map all the field names...
	$Fields	= [map {(exists($FieldCaseMap->{lc($_)}) ?		
					 $FieldCaseMap->{lc($_)} :
					 do{$this->warn("Unrecognized output field name (ignored): $_"); ()})} @$Fields];
	
	## Map all the sort field names...
	$Sorts	= [map {(exists($FieldCaseMap->{lc($_)}) ?		
					 $FieldCaseMap->{lc($_)} :
					 do{$this->warn("Unrecognized sort field name (ignored): $_"); ()})} @$Sorts];
	
	@$SortDirs{@$FieldCaseMap{keys %$SortDirs}} = values %$SortDirs;

	## Add additional fields as requested.

	my $InfoFields = [qw(	Device
							INode 
							Mode  
							NLink 
							UID   
							GID   
							RDev  
							Size  
							ATime 
							MTime 
							CTime 
							BlkSize
							Blocks 
							)];

	my $TimeFields = [qw(	Sec   
							Min   
							Hour  
							Day   
							TMon  
							TYear 
							WDay  
							YDay  
							IsDST 
							)];

	my $BaseFields = [qw(	Dir
							Base
							Ext)];

	my $DervFields = [qw(	File       
							Type       
							Perms      
							Owner      
							Group      
							Mon        
							Year       
							RTime      
							Stamp      
							)];
	
	push @$Fields, @$InfoFields												if $Opts->{info};
	push @$Fields, @$TimeFields												if $Opts->{localtime};
	push @$Fields, @$BaseFields												if $Opts->{base};
	push @$Fields, @$DervFields												if $Opts->{derived};
	push @$Fields, (@$InfoFields, @$TimeFields, @$BaseFields, @$DervFields)	if $Opts->{all};

	## If the field list is empty, populate it.
	$Fields	= [($Opts->{nopath} ? () : 'Path'), qw(Owner Group Size RTime)] unless @$Fields;

	## If the field list doesn't include "Path" already, then prepend
	## it unless --nopath was specified
	unshift @$Fields, 'Path' unless ((grep {$_ eq 'Path'} @$Fields) || (@$Fields && $Opts->{nopath}));
	
	## After adding in the above field groups, remove any duplicates
	my $Fs = {}; $Fields = [grep {$Fs->{$_}++ ? () : $_} @$Fields];

	## If the sort list doesn't include "Path" already, then append it.
	push @$Sorts, 'Path' unless grep {$_ eq 'Path'} @$Sorts;

	## Add a sort-direction specifier to the table for each field
	## based on the + signs we got or didn't get in the --sort fields
	## list.

	foreach my $Field (@$Sorts)
	{
		$this->sortspec($Field, {%{$this->sortspec($Field)}, 
								 SortDirection =>
									 ($SortDirs->{$Field} eq '+' ? -1 : 1)});
	}
	
	## die $this->dump($Sorts, $FieldCaseMap, $SortDirs, $this->sortspecs());
	
	## Sort the table.
	$this->sortorder($Sorts);
	$this->sort();
	
	## Set the field list for output.
	$this->fieldlist($Fields);

	## Process the output method
	my $OutputArgs	= $Opts->{output};	## Already an array ref
	my $Method		= shift @$OutputArgs || "format";

	## If "write" was specified, then we're going to force the "write"
	## method.  The FileName "-" (meaning STDOUT) will override any
	## other name that may have been redundantly specified with
	## -o=write.

	if ($Write)
	{
		$Method		= "write";
		unshift @$OutputArgs, "_FileName" if @$OutputArgs == 1;
		push @$OutputArgs, (_FileName => "-");
	}

	## If "tabs" was specified, then we're going to force the "write" method
	if ($Tabs)
	{
		$Method		= "write";
		unshift @$OutputArgs, "_FileName" if @$OutputArgs == 1;
		push @$OutputArgs, (_FDelimiter => "\t");
	}

	if ($NoHeader)
	{
		$Method		= "write";
		unshift @$OutputArgs, "_FileName" if @$OutputArgs == 1;
		push @$OutputArgs, (_HeaderRow => 0);
	}

	if ($Mac || $Unix || $Dos)
	{
		$Method		= "write";
		unshift @$OutputArgs, "_FileName" if @$OutputArgs == 1;
		push @$OutputArgs, (_LineEnding => ($Mac || $Unix || $Dos));
	}

	## Do the output.
	my $Result		= $this->$Method(@$OutputArgs);

	
	return(ref($Result) eq 'SCALAR' ? $Result : \ '');
}

=pod

	## Class-level utility method
	$Class->list_dir($Path, $Sep, $Cur, $Recurse, $NoTop, $NoChildren)

Platform-neutral recursive directory lister routine.  

Caller must supply $Sep (platform's separator character) and $Cur
(platform's string meaning "current directory").

If $Dir is empty/undef, then $Cur will be used as the starting point.

$Recurse means recurse to list subdirectories.

$NoTop means don't include $Dir itself in the listing.

$NoChildren means don't include children of $Dir.  This option is
ignored if $Recurse is true.

=cut

sub list_dir
{
	my $Class												= shift;
	my ($Dir, $Sep, $Cur, $Recurse, $NoTop, $NoChildren)	= @_;
	
	my $Names	= [];

	## Ensure trailing $Sep on path given by user.
	($Dir ||= $Cur) =~ s{([^\Q$Sep\E])$ }{$1$Sep}ox; 
	
	my $D		= \*DIR;	## We like file handle references.
	
	opendir($D, $Dir) 
		or $Class->warn("Can't open directory $Dir: $!"), goto done;
	
	$Names		= [($NoTop		? () : $Dir),	## Include $Dir itself in listing.
				   ($NoChildren	? () :
					map  {my  $Sub = "$Dir$_";
						  (-d $Sub ?
						   ("$Sub$Sep", 
							($Recurse ? 
							 @{$Class->list_dir($Sub, $Sep, $Cur, $Recurse, 'NoTop')} :
							 ())) : 
						   $Sub)} 
					grep {!/^\.\.?$/} 	## Omit . and .. entries.
					readdir($D))];
	closedir $D;
	
  done:	
	return($Names);
}

1;
