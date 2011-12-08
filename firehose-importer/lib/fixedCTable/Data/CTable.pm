#!/usr/bin/perl
## Emacs: -*- tab-width: 4; -*-

use strict;

package Data::CTable;

use vars qw($VERSION);              $VERSION = '1.03';

=pod

=head1 NAME

Data::CTable - Read, write, manipulate tabular data

=head1 SYNOPSIS

    ## Read some data files in various tabular formats
    use          Data::CTable;
    my $People = Data::CTable->new("people.merge.mac.txt");
    my $Stats  = Data::CTable->new("stats.tabs.unix.txt");

    ## Clean stray whitespace in fields
    $People->clean_ws();
    $Stats ->clean_ws();

    ## Retrieve columns
    my $First = $People->col('FirstName');  
    my $Last  = $People->col('LastName' );

    ## Calculate a new column based on two others
    my $Full  = [map {"$First->[$_] $Last->[$_]"} @{$People->all()}];

    ## Add new column to the table
    $People->col(FullName => $Full);

    ## Another way to calculate a new column
    $People->col('Key');
    $People->calc(sub {no strict 'vars'; $Key = "$Last,$First";});

    ## "Left join" records matching Stats:PersonID to People:Key
    $Stats->join($People, PersonID => 'Key');

    ## Find certain records
    $Stats->select_all();
    $Stats->select(Department => sub {/Sale/i  });  ## Sales depts
    $Stats->omit  (Department => sub {/Resale/i});  ## not Resales
    $Stats->select(UsageIndex => sub {$_ > 20.0});  ## high usage

    ## Sort the found records
    $Stats->sortspec('DeptNum'   , {SortType => 'Integer'});
    $Stats->sortspec('UsageIndex', {SortType => 'Number' });
    $Stats->sort([qw(DeptNum UsageIndex Last First)]);

    ## Make copy of table with only found/sorted data, in order
    my $Report = $Stats->snapshot();

    ## Write an output file
    $Report->write(_FileName => "Rept.txt", _LineEnding => "mac");

    ## Print a final progress message.
    $Stats->progress("Done!");

    ## Dozens more methods and parameters available...

=head1 OVERVIEW

Data::CTable is a comprehensive utility for reading, writing,
manipulating, cleaning and otherwise transforming tabular data. The
distribution includes several illustrative subclasses and utility
scripts.

A Columnar Table represents a table as a hash of data columns, making
it easy to do data cleanup, formatting, searching, calculations,
joins, or other complex operations.

The object's hash keys are the field names and the hash values hold
the data columns (as array references).

Tables also store a "selection" -- a list of selected / sorted record
numbers, and a "field list" -- an ordered list of all or some fields
to be operated on.  Select() and sort() methods manipulate the
selection list.  Later, you can optionally rewrite the table in memory
or on disk to reflect changes in the selection list or field list.

Data::CTable reads and writes any tabular text file format including
Merge, CSV, Tab-delimited, and variants.  It transparently detects,
reads, and preserves Unix, Mac, and/or DOS line endings and tab or
comma field delimiters -- regardless of the runtime platform.

In addition to reading data files, CTable is a good way to gather,
store, and operate on tabular data in memory, and to export data to
delimited text files to be read by other programs or interactive
productivity applications.

To achieve extremely fast data loading, CTable caches data file
contents using the Storable module.  This can be helpful in CGI
environments or when operating on very large data files.  CTable can
read an entire cached table of about 120 megabytes into memory in
about 10 seconds on an average mid-range computer.

For simple data-driven applications needing to store and quickly
retrieve simple tabular data sets, CTable provides a credible
alternative to DBM files or SQL.

For data hygiene applications, CTable forms the foundation for writing
utility scripts or compilers to transfer data from external sources,
such as FileMaker, Excel, Access, personal organizers, etc. into
compiled or validated formats -- or even as a gateway to loading data
into SQL databases or other destinations.  You can easily write short,
repeatable scripts in Perl to do reporting, error checking, analysis,
or validation that would be hard to duplicate in less-flexible
application environments.

The data representation is simple and open so you can directly access
the data in the object if you feel like it -- or you can use accessors
to request "clean" structures containing only the data or copies of
it.  Or you can build your own columns in memory and then when you're
ready, turn them into a table object using the very flexible new()
method.

The highly factored interface and implementation allow fine-grained
subclassing so you can easily create useful lightweight subclasses.
Several subclasses are included with the distribution.

Most defaults and parameters can be customized by subclassing,
overridden at the instance level (avoiding the need to subclass too
often), and further overridden via optional named-parameter arguments
to most major method calls.

=head2 Similar / related modules on CPAN

The Data::Table module by Yingyao Zhou & Guangzhou Zou offers similar
functionality, but uses a different underlying data representation
(2-dimensional array), and has a somewhat different feature set.
Check it out.  Maybe you will prefer it for your application.

    http://search.cpan.org/search?mode=module&query=Data::Table

The Data::ShowTable module renders tables in various viewable formats.
CTable relies on ShowTable's ShowBoxTable method to implement its own
format() and out() methods.

    http://search.cpan.org/search?mode=module&query=Data::ShowTable


=head2 Prerequisites

The CTable documentation, source code, and examples assume familiarity
with large nested data structures, object-oriented syntax and
terminology, and comfort working with array and hash references and
array and hash slice syntax.  

See the perlref man page for more on these topics.


=head2 How to learn more

Dozens more methods, parameters, and examples are described below.

See the full source code in CTable.pm.

Or, after installing, read the man page using:

    man     Data::CTable
    perldoc Data::CTable

See the eg/ (examples) folder in the Data::CTable distribution and the
test.pl script for scripts demonstrating every CTable method.

For latest version and other news, check the Data::CTable home page:

    http://christhorman.com/projects/perl/Data-CTable/

Or search CPAN:

    http://search.cpan.org/search?mode=module&query=Data::CTable

=head1 INSTALLATION

Using CPAN module:

    perl -MCPAN -e 'install Data::CTable'

Or manually:

    tar xzvf Data-CTable*gz
    cd Data-CTable-?.??
    perl Makefile.PL
    make
    make test
    make install

=head1 INCLUDED SUBCLASSES AND UTILITIES

In addition to the module itself, there are a number of subclasses and
simple utilities included with the Data::CTable distribution.

=head2 Subclases

The Data::CTable distribution includes these example subclasses.  Each
is installed in your Perl environment along with the main module, and
so may be used by your scripts.  Each has its own man/perldoc page
containing more detail.

B<Data::CTable::ProgressLogger> is a subclass that logs all progress
messages to a list within the object itself rather than (merely)
echoing them to STDERR.  Later, you may retrieve and examine the list.

B<Data::CTable::Script> is a virtual subclass that includes class and
object methods that make it easy to write a simple interactive
command-line program that parses options and outputs a table.

B<Data::CTable::Listing> is a very useful subclass of
Data::CTable::Script that implements a souped-up Unix-like "ls" (file
listing) command -- it first gets an optionally-recursive listing of
any number of files and/or directories, builds a list of their full
absolute or relative paths, then build a Data::CTable::Listing object
that contains all the paths, plus about 25+ other pieces of useful
information about each file or directory.  

The "tls" utility, below, is simply a command-line cover for this
class, but you could use this class in your own scripts in order to
get detailed file listings.

=head2 Utilities

Each of these utilities is provided mainly so you can see mature
examples of how to use Data::CTable in real-world scripts.  

But each is also genuinely useful, too, and you may enjoy adding them
to your regular bag of tricks or using them as an easily-modifiable
basis for scripts of your own.

On most systems, these will be installed in an appropriate directory
in your path when you install CTable, and hence will be executable
just by typing their file name.  (On Windows, they'll be wrapped by a
.bat file and installed in C:\Perl\bin or equivalent.  On *nix,
they'll be in /usr/bin/ or equivalent.)

B<tls> is a command-line utility that wraps Data::CTable::Listing to
implement a variant on the classic Unix "ls" command in which an
internal CTable object is used to hold and calculate a very large
amount of meta-data about each file and directory and then output that
data in formatted tables, or as any kind of delimited text file, and
with much more flexibility and control over included data, and sort
and sub-sort order than with ls.

B<tshow> is a command-line utility that reads each of its arguments as
a Data::CTable file and then calls the out() method to display its
entire contents.  (Warning: out() is slow with very large data sets.)

B<getweather> is a command-line utility that takes a US zip code,
grabs the local weather report from a popular weather web site and
uses a CTable object to store, process, and clean, and present the
table of weather data that results in a simple text format.

=cut
    
## Required dependencies

use IO::File;
use Config;         qw(%Config);

use Carp            qw(croak carp confess cluck);
use Storable        qw(store nstore retrieve dclone);
use File::Basename  qw(fileparse);


## Optional dependencies

my $HaveDumper;
my $HaveShowTable;

BEGIN 
{ 
    eval "
        use Data::Dumper    qw(Dumper);
        use Data::ShowTable qw(ShowBoxTable);
    ";
    
    $HaveDumper     =        $Data::Dumper::VERSION;
    $HaveShowTable  = exists($Data::ShowTable::{ShowBoxTable});
};


## We optionally export a few general-purpose utility routines.

use Exporter; use vars qw(@ISA @EXPORT_OK); @ISA=qw(Exporter);
@EXPORT_OK = qw(
                &ISORoman8859_1ToMacRoman
                &MacRomanToISORoman8859_1
                
                &ISORoman8859_1ToMacRoman_clean
                &MacRomanToISORoman8859_1_clean

                &guess_endings
                &guess_delimiter

                &path_info
                &path_is_absolute

                &min
                &max
                );

=pod 

=head1 CREATING TABLE OBJECTS

    ## Create an object / read file(s) / override params

    use  Data::CTable;

    $t = Data::CTable->new()
    $t = Data::CTable->new($File)
    $t = Data::CTable->new($File1, $File2....)
    $t = Data::CTable->new($Params)
    $t = Data::CTable->new($Params, $File1)
    $t = Data::CTable->new($Params, $File1, $File2....)

    ## Internal initializer (subclassable): called for you by new()

    $t->initialize()

If the first argument to new() is a hash ref, it is the $Params hash
of initial parameters and/or data columns which, if supplied will form
the starting point for the object being created.  Any non-hash and/or
further arguments to new() are treated as file names to be opened.

If supplied, data in the $Params hash will be shallowly copied -- the
original hash object passed will not be used, but any sub-structures
within it will now "belong" to the resulting new object which will
feel free to manipulate them or discard them.

Then, any parameters not supplied (usually most of them) will be
defaulted for you because new() will call the internal method
initialize() before the object is finished and returned.

See the PARAMETER REFERENCE section below for the parameters you can
choose to supply or have defaulted for you.

Any file name arguments will be read and appended into a single
object.  If any of the files fails to be read, then new() will fail
(return a false value) and no object will be created.

initialize() makes sure there is a legal and consistent value for
every internal parameter in the object.  Generally, initialize()
leaves alone any parameters you supplied to new(), and simply sets
default values for any that were not yet supplied.

You should never need to call initialize() directly.

After calling initialize(), new() then calls the append_files_new()
method to process the filename arguments.  This method then calls
read() on the first filename, and then append_file on all subsequent
file names, appending them to the first, in sequence.  

See append() for an explanation of how the data from multiple files is
combined into a single table.

=head2 Advanced: Using a template object

    ## Calling new() with a template object

    $v =           $t->new()                           
    $v =           $t->new($File)                      
    $v =           $t->new($File1, $File2....)         
    $v =           $t->new($Params)                    
    $v =           $t->new($Params, $File1)                    
    $v =           $t->new($Params, $File1, $File2....)

You can also call $t->new() to use an existing object, $t, as a
template for the new object.  $t->new() will create a new object of
the same class or subclass as the template object.  Furthermore, the
template object, if provided, will be used as a starting point for the
resulting object -- in fact, it will initially share shallow copies of
all data columns, if any, and all internal parameters and data
structures, if any.

This advanced shared-data technique could be used to create two
separate table objects that share and operate on the same underlying
data columns in memory but have different custom field lists, custom
selections, sort behavior, etc.  But don't do this unless you're sure
you understand what you're doing, because changing data in one table
would change it in the other.

=head1 PARAMETER REFERENCE

The parameters listed here are recognized by new(), initialize(), and
by many functions that use the named-parameter calling convention,
such as read(), write(), sort(), and others.

Any parameter listed may be specified when using new().  

Most parameters should not be directly accessed or manipulated once
the object has been created, except those that have appropriate
accessor methods described throughout in this documentation.

Each parameter in the lists below is listed along with its defaulting
logic as performed by new() via initialize().

=head2 Custom field list and custom selection (record list)

=over 4

=item _FieldList ||= undef;

This is the ordered list (array reference) of columns / fields present
in the object.  It is set by read() to reflect the names and order of
the fields encountered or actually read from in the incoming data
file, if any.  Initially this list is undefined, and if removed or
left undefined, then the de-facto field list will be a list of all
columns present in the table object, in alphabetical order (see
fieldlist() and fieldlist_all()).

Normally, all fields present in the table object would be listed in
the field list.

However, this parameter may be set in the object (or overridden in
named-parameter function calls like read(), write(), etc.) to cause a
subset of fields to be read, written or otherwise used.  If a subset
field list is specified before reading a data file, then ONLY fields
listed will be read -- this is a way to read just certain fields from
a very large file, but may not always be what you want.

Specifying the field list before calling read() is required if the
data file has no header row giving names to its fields -- the names
you specify in the field list, in order, will be applied to the data
file being read.  See the _HeaderRow parameter, below.

If a subset field list is used after columns are already loaded in
memory, the columns not listed in the field list will still be present
in the object (and can be listed by calling fieldlist_all()), but they
will be omitted from most operations that iterate over fields.

=item _Selection ||= undef;

This is a list of the record numbers of "selected" records in the
table, possibly indicating sorted order.

If absent, then all records are considered to be selected, in
"natural" order -- i.e. the order they occur in the file.

You can create and set your own selection list or get and modify an
existing one.  Deleting it resets the selection (for example, by
calling select_all()).

Calling sort() will create a _Selection if none existed.  Otherwise it
operates by modifying the existing _Selection, which may be a subset
of all record numbers.

=back 

=head2 Cache behavior controls

See sections related to Cacheing, below.

=over 4 

=item _CacheOnRead   = 1 unless exists

Boolean: whether data files read by the read() method should be cached
after reading.  Once cached, the data will be read from the cache
instead of the original file the NEXT TIME READ() IS CALLED, but only
if: 1) the cache file is found, and 2) its date is later than the
original.  Otherwise, the cache file is ignored or re-written.
Cacheing can be up to 10x faster than parsing the file, so it's almost
always worth doing in any situation where you'll be reading a data
file more often than writing it.

This parameter defaults to true.

=item _CacheOnWrite  = 0 unless exists

Boolean: whether tables written by the write() method should be cached
after writing.  This defaults to false on the assumption that the
program won't need to re-read a file it just wrote.  However, this
behavior would be useful if a later step in your program or another
program will be reading the file that was written and would benefit
from having the cacheing already done.  Cacheing a file after writing
is quite fast since the data is already in memory and of course it
speeds up subsequent read() operations by up to 10x.

=item _CacheExtension = ".cache" unless exists

This is the file name extension that is added to a file's name to
determine the name of its corresponding cache file.  (First, any
existing extension, if any, is removed.)  If this extension is empty,
then the cache file will be named the same as the original assuming
the cache file is being stored in a different directory.  (See next
setting.)

=item _CacheSubDir    =  "cache" unless exists

This is the absolute or relative path to the subdirectory that should
be used to store the cache files.  The default value is the relative
path to a directory called "cache".  Relative paths will be appended
to the directory path containing the original file being read.

Absolute cache paths (such as /tmp or c:\temp\) can also be used.

Override _CacheExtension and _CacheSubdir in a subclass, in each
object, or in each call to read() or write() in order to have the
cache files stored elsewhere.  But remember: unless you use the same
cache settings next time you read the same file, the cache files will
be orphaned.

=back 

=head2 Progress routine / setting

=over 4 

=item _Progress       = undef unless exists

The _Progress setting controls the routing of diagnostic messages.
Four possible settings are recognized:

    undef (default)        The class's progress settings are used.
    subroutine reference   Your own custom progress routine.
    true                   Built-in progress_default() method used.
    0/false                No progress messages for this object.

See the PROGRESS section for a description of the interface of custom
progress routines and for details on how the builtin one works.

=back

=head2 File format settings

=over 4 

=item _LineEnding    ||= undef;

_LineEnding indicates the line ending string or setting to be used to
read a file, the setting that actually I<was> used to read a file,
and/or the line ending that will be used to write a file.

Set this parameter to force a particular encoding to be used.

Otherwise, leave it undef.  The program will Do What You Mean.

If _LineEnding is undef when read() is called, read() will try to
guess the line ending type by inspecting the first file it reads.
Then it will set this setting for you.  It can detect DOS, Unix, and
Mac line endings.

If _LineEnding is undef when write() is called, write() will use
C<"\n">, which yields different strings depending on the current
runtime platform: \x0A on Unix; \x0D in MacPerl, \x0D\x0A on DOS.

Otherwise, write() uses the value defined in _LineEnding, which would
match the value filled in by read() if this object's data originally
had been read from a file.  So if you read a file and then later write
it out, the line endings in the written file will match the format of
original unless you override _LineEnding specifically.

Since Data::CTable supports reading and writing all common endings,
base your decision on line ending format during write() on the needs
of other programs you might be using.

For example: FileMaker Pro and Excel crash / hang if Unix line endings
are used, so be sure to use the ending format that matches the needs
of the other programs you plan to use.

As a convenience, you may specify and retrieve the _LineEnding setting
using the mnemonic symbols "mac", "dos" and "unix."  These special
values are converted to the string values shown in this chart:

     symbol   string value  chars  decimal     octal     control
    -------------------------------------------------------------
      dos     "\x0D\x0A"    CR/LF   13,10    "\015\012"   ^M^J
      mac     "\x0D"        CR      13       "\015"       ^M
      unix    "\x0A"        LF      10       "\012"       ^J

See the section LINE ENDINGS, below, for accessor methods and
conversion utilities that help you get/set this parameter in either
symbolic format or string format as you prefer.

=item _FDelimiter    ||= undef;     

_FDelimiter is the field delimiter between field names in the header
row (if any) and also between fields in the body of the file.  If
undef, read() will try to guess whether it is tab C<"\t"> or comma
<",">, and set this parameter accordingly.  If there is only one field
in the file, then comma is assumed by read() and will be used by
write().

To guess the delimiter, the program looks for the first comma or tab
character in the header row (if present) or in the first record.
Whichever character is found first is assumed to be the delimiter.

If you don't want the program to guess, or you have a data file format
that uses a custom delimiter, specify the delimiter explicitly in the
object or when calling read() or make a subclass that initializes this
value differently.  On write(), this will default to comma if it is
empty or undef.

=item _QuoteFields    = undef unless exists

_QuoteFields controls how field values are quoted by write() when
writing the table to a delimited text file.

An undef value (the default) means "auto" -- each field is checked
individually and if it contains either the _FDelimiter character or a
double-quote character, the field value will be surrounded by
double-quotes as it is written to the file.  This method is slower to
write but faster to read, and may make the output easier for humans to
read.

A true value means always put double-quotes around every field value.
This mode is faster to write but slower to read.

A zero value means never to use double-quotes around field values and
not to check for the need to use them.  This method is the fastest to
read and write.  You may use it when you are certain that your data
can't contain any special characters.  However, if you're wrong, this
mode will produce a corrupted file in the event that one of the fields
does contain the active delimiter (such as comma or tab) or a quote.

=item _HeaderRow      = 1 unless exists

_HeaderRow is a boolean that says whether to expect a header row in
data files.  The default is true: a header row is required.  If false,
_FieldList MUST be present before calling read() or an error will be
generated.  In this latter case, _FieldList will be assumed to give
the correct names of the fields in the file, in order, before the file
is read.  In other words, the object expects that either a) it can get
the field names from the file's header row or b) you will supply them
before read() opens the file.

=back 

=head2 Encoding of return characters within fields

=over 4 

=item _ReturnMap       = 1 unless exists

_ReturnMap says that returns embedded in fields should be decoded on
read() and encoded again on write().  The industry-standard encoding
for embedded returns is ^K (ascii 11 -- but see next setting to change
it).  This defaults to true but can be turned off if you want data
untouched by read().  This setting has no effect on data files where
no fields contain embedded returns.  However, it is vital to leave
this option ON when writing any data file whose fields could contain
embedded returns -- if you have such data and call write() with
_ReturnMap turned off, the resulting file will be an invalid Merge/CSV
file and might not be re-readable.

When these fields are decoded on read(), encoded returns are converted
to C<"\n"> in memory, whatever its interpretation may be on the current
platform (\x0A on Unix or DOS; \x0D on MacPerl).

IMPORTANT NOTE: When these fields are encoded by write(), any
occurrence of the current _LineEnding being used to write the file is
searched and encoded FIRST, and THEN, any occurrence of "\n" is also
searched and encoded.  For example, if using mac line endings (^M) to
write a file on a Unix machine, any ^M characters in fields will be
encoded, and then any "\n" (^J) characters will ALSO be encoded.  This
may not be what you want, so be sure you know how your data is encoded
in cases where your field values might contain any ^J and/or ^M
characters.

IMPORTANT NOTE: If you turn _ReturnMap off, fields with returns in
them will still be double-quoted correctly.  Some parsers of tab- or
comma-delimited files are able to support reading such files.
HOWEVER, the parser in this module's read() method DOES NOT currently
support reading files in which a single field value appears to span
multiple lines in the file.  If you have a need to read such a file,
you may need to write your own parser as a subclass of this module.

=item _ReturnEncoding    ||= "\x0B";

This is the default encoding to assume when embedding return
characters within fields.  The industry standard is "\x0B" (ascii 11 /
octal \013 / ^K) so you should probably not ever change this setting.

When fields are encoded on write(), C<"\n"> is converted to this
value.  Note that different platforms use different ascii values for
C<"\n">, which is another good reason to leave the ReturnEncoding
feature enabled when calling write().  

To summarize: this module likes to assume, and you should too, that
returns in data files on disk are encoded as "\x0B", but once loaded
into memory, they are encoded as the current platform's value of
C<"\n">.

=item _MacRomanMap       = undef unless exists

Data::CTable assumes by default that you want field data in memory to
be in the ISO 8859-1 character set (the standard for Latin 1 Roman
characters on Unix and Windows in the English and Western European
languages -- and also the default encoding for HTML Web pages).

_MacRomanMap controls the module's optional mapping of Roman
characters from Mac format on disk to ISO format in memory when
reading and writing data files.  These settings are recognized:

    undef   ## Auto: Read/write Mac chars if using Mac line endings  
    1       ## On:   Assume Mac char set in all fields
    0       ## Off:  Don't do any character mapping at all

The default setting is undef, which enables "Auto" mode: files found
to contain Mac line endings will be assumed to contain Mac upper-ASCII
characters and will be mapped to ISO on read(); and files to be
written with Mac line endings will mapped back from ISO to Mac format
on write().

If your data uses any non-Latin-1 character sets, or binary data, or
you really want Mac upper-ASCII characters in memory, or you just
don't want this module messing with your encodings, set this option to
0 (Off) or make a subclass that always sets it to 0.

See also the clean() methods that can help you translate just the
columns you want after reading a file or before writing it, which may
be faster for you if only a few fields might contain high-ASCII
characters.

=item _FileName          ||= undef;

This is the name of the file that should be read from or WAS read
from.  (read() will set _FileName to the value it used to read the
file, even if _FileName was only supplied as a named parameter.)

This name will also be used, unless overridden, to re-write the file
again, but with an optional extension added.  (See next setting.)

=item _WriteExtension = ".out" unless exists

The _WriteExtension is provided so that CTable won't overwrite your
input data file unless you tell it to.

_WriteExtension will be added to the object's _FileName setting to
create a new, related file name, before writing....  UNLESS _FileName
is supplied as an direct or named parameter when calling write().  

In the latter case, write() uses the file name you supply and adds no
extension, even if this would mean overwriting the original data file.

To add _WriteExtension, write() places it prior to any existing final
extension in the _FileName:

    _FileName             default file name used by write()
    --------------------------------------------------------------
    People.merge.txt      People.merge.out.txt
    People                People.out

If you want to always overwrite the original file without having to
supply _FileName each time, simply set _WriteExtension to undef in a
subclass or in each instance.

If _CacheOnWrite is true, then the _WriteExtension logic is applied
first to arrive at the actual name of the file to be written, and then
the _CacheExtension logic is applied to that name to arrive at the
name of the cache file to be written.

=back 

=head2 Sorting-related parameters

=over 4 

=item _SortOrder ||= undef;

_SortOrder is the list of fields which should be used as primary,
secondary, etc. sort keys when sort() is called.  Like other
parameters, it may be initialized by a subclass, stored in the object,
or provided as a named parameter on each call to sort().

If _SortOrder is empty or undefined, then sort() sorts the records by
record number (i.e. they are returned to their "natural" order).

=item _SortSpecs ||= {};

_SortSpecs is a hash of specifications for the SortType and
SortDirection of fields on which sorting may be done.  For any field
missing a sort spec or the SortType or SortDirection components of its
sort spec, the _DefaultSortType and _DefaultSortDirection settings
will be used.  So, for example, if all fields are of type String and
you want them to sort Ascending, then you don't need to worry about
_SortSpecs.  You only need to provide specs for fields that don't take
the default settings.

_SortSpecs might look like this:

    {Age      => {SortType => 'Integer'}, 
     NameKey  => {SortType => 'Text', SortDirection => -1}}

=item _SRoutines ||= {}; 

_SRoutines is a hash mapping any new SortTypes invented by you to your
custom subroutines for sorting that type of data.  (See the section on
sort routines, below, for a full discussion.)

=back 

=head2 Sorting defaults

=over 4 

=item _DefaultSortType      ||= 'String';

If you sort using a field with no sort spec supplied, or whose sort
spec omits the SortType, it will get its SortType from this parameter.

See the sections below on SORT TYPES and SORT ROUTINES.

=item _DefaultSortDirection ||= 1;

If you sort using a field with no sort spec supplied, or whose sort
spec omits the SortDirection, it will get its SortDirection from this
parameter.

Legal sort directions are: 1 (Ascending) or -1 (Descending).

See the section below on DEFAULT SORT DIRECTION.

=back 

=head2 Miscellaneous parameters

=over 4 

=item _ErrorMsg  ||= "";

This parameter is set by read() or write() methods that encounter an
error (usually a parameter error or file-system error) that prevents
them from completing.  If those methods or any methods that call them
return a false value indicating failure, then _ErrorMsg will contain a
string explaining the problem.  The message will also have been passed
to the progress() method for possible console feedback.

=item _Subset   

This parameter is set to 1 (true) by read() if the last call to read()
brought in a subset of the fields available in the file; 0 otherwise.

The object uses this field internally so it knows to abandon any cache
files that might not contain all requested fields upon read().

=back

=head1 SUBCLASSING

Most subclasses will override initialize() to set default values for
the parameters of the parent class and then they may provide default
values for other subclass-specific parameters.  Then, the subclass's
initialize() should call SUPER::initialize() to let the parent
class(es) take care of the remaining ones.

Every initialize() method should always allow for parameters to have
already been provided by the $Params hash or template object.  It
should not overwrite any valid values that already exist.

The following sample subclass changes the default setting of the
_Progress parameter from undef to 1 and then overrides the
progress_default() method to log all progress messages into a new
"_ProgrLog" (progress log) parameter stored in the object.

    BEGIN
    {   ## Data::CTable::ProgressLogger: store messages in the object

        package Data::CTable::ProgressLogger;
        use vars qw(@ISA); @ISA=qw(Data::CTable);

        sub initialize       ## Add a new param; change one default
        {
            my $this           = shift;
            $this->{_Progress} = 1 unless exists($this->{_Progress});
            $this->{_ProgrLog} ||= [];
            $this->SUPER::initialize();
        }

        sub progress_default ## Log message to object's ProgMsgs list
        {
            my $this            = shift;
            my ($msg)           = @_;
            chomp                                       $msg;
            push @{$this->{_ProgrLog}}, localtime() . " $msg";

            return(1);
        }

        sub show_log         ## Use Dumper to spit out the log list
        {
            my $this            = shift;
            $this->dump($this->{_ProgrLog});
        }
    }

    ## Later...

    my $Table = Data::CTable::ProgressLogger->new("mydata.txt");
    # ... do stuff...
    $Table->write();
    $Table->show_log();

=cut
    
{}; ## Get emacs to indent correctly.

sub new
{
    ## First arg to new is always either class name or a template
    ## object.  This allows $obj->new() or CLASS->new().

    ## Second argument (if and only if it is a hash ref or an object
    ## whose underlying representation is a hash ref) is an optional
    ## anonymous hash of parameters which if supplied, will override
    ## any parameters already found in the template object, if any.

    ## See the initialize method, below, for a list of parameters that
    ## can be supplied (and will be defaulted for you if not
    ## supplied).

    ## Note that the template object and the params hash will be
    ## SHALLOWLY copied -- the original hash objects passed will not
    ## be used, but any sub-structures within them will now "belong"
    ## to the resulting new object which will feel free to manipulate
    ## them, possibly invalidating the integrity of the original
    ## template object.

    my $ClassOrObj  = shift;
    my ($Params)    = {%{shift()}} if UNIVERSAL::isa($_[0], 'HASH');

    ## Shallow-copy all params from template object and/or optional
    ## $Params hash into new hash.  DON'T re-use caller's obj or hash.

    my $this = 
    {%{(UNIVERSAL::isa($ClassOrObj, 'HASH') ? $ClassOrObj : {})},
     %{(UNIVERSAL::isa($Params,     'HASH') ? $Params     : {})}};
    
    ## Bless the new object into the class
    
    my $class = ref($ClassOrObj) || $ClassOrObj;
    bless $this, $class;
    
    my $Success;

    ## Run the subclassable initialize() method to create default
    ## settings for any private parameters.

    goto done unless $this->initialize();

    ## Finally, process any (other) arguments to new(), if any.
    
    my $RemainingArgs = [@_];
    
    goto done unless $this->process_new_args($RemainingArgs, $Params);
    
    $Success = 1;
  done:
    return ($Success ? $this : undef);
}

### process_new_args

### Any optional remaining (non-HASH ref) arguments to new() are
### treated as file names of files to open and append to the in-memory
### table, creating new columns as necessary.  We call the
### subclassable append_files_new() method to process these.

sub process_new_args
{
    my $this                        = shift;
    my ($RemainingArgs, $Params)    = @_;

    my $Success;

    $Success = $this->append_files_new($RemainingArgs, $Params) or goto done;
    
    $Success = 1;
  done:
    return ($Success);
}

### initialize

### Assumptions made by initialize() (and all other methods, too):

### The blessed object is a hash ref.

### All hash keys beginning with _ are reserved for non-data columns.

### Hash keys beginning with a single _ are reserved for future
### versions of this parent class implementation.  Subclasses might
### want to use double-underscore for additional slots.

### All other hash keys are field names; their values are data
### columns (array references).

### initialize() sets / validates initial settings for all parameters
### recognized by this parent class.  It exercises caution to not
### override any legal values previously set by the
### subclass::initialize() or by new().

sub initialize
{
    my $this = shift or goto done;

    my $Success;

    ## Reading / writing

    $this->{_FileName}      ||= undef;    ## Path of file that was read

    $this->{_WriteExtension} = ".out"   unless exists($this->{_WriteExtension});

    ## Cache settings

    $this->{_CacheOnRead}    = 1 unless exists($this->{_CacheOnRead});
    $this->{_CacheOnWrite}   = 0 unless exists($this->{_CacheOnWrite});
    $this->{_CacheExtension} = ".cache" unless exists($this->{_CacheExtension});
    $this->{_CacheSubDir}    =  "cache" unless exists($this->{_CacheSubDir});

    ## File format settings

    $this->{_LineEnding}    ||= undef;     
    $this->{_FDelimiter}    ||= undef;     
    $this->{_QuoteFields}   =  undef    unless exists ($this->{_QuoteFields});
    $this->{_HeaderRow}     = 1         unless exists ($this->{_HeaderRow});

    ## Return encodings

    $this->{_ReturnMap}      = 1        unless exists ($this->{_ReturnMap});
    $this->{_ReturnEncoding} ||= "\x0B";   ## Char to use for return chars
    $this->{_MacRomanMap}    = undef    unless exists ($this->{_MacRomanMap});

    ## Sorting defaults

    $this->{_DefaultSortType}      ||= 'String';
    $this->{_DefaultSortDirection} ||= 1; ## Ascending (-1 = desc)

    ## Progress routine / setting

    $this->{_Progress}      = undef unless exists ($this->{_Progress});

    ## Internal meta-structures

    $this->{_FieldList} ||= undef;  ## List of fields; undef means all fields, alpha order
    $this->{_Selection} ||= undef;  ## List of rec #s; undef means all records, natural order
    $this->{_SortOrder} ||= undef;  ## List of fields; undef/empty means sort by record number

    $this->{_SortSpecs} ||= {};     ## Hash: FieldName => Sortspec
    $this->{_SRoutines} ||= {};     ## Hash: SortType  => custom sort routine for type

    ## Miscellaneous

    $this->{_ErrorMsg}  ||= "";     ## Explains last read/write failure
    $this->{_Subset}    ||= 0;      ## Flag indicating subset of available fields were read

    $Success = 1;
  done:
    return($Success);
}

=pod 

=head1 FIELD LIST

    ## Getting / setting the object's _FieldList

    $t->fieldlist()             ## Get _FieldList or fieldlist_all()
    $t->fieldlist_get()         
    $t->fieldlist_hash()        ## Get fieldlist() as keys in a hash
    
    $t->fieldlist_all()         ## Get all fields (ignore _FieldList)
    
    $t->fieldlist($MyList)      ## Set field list (_FieldList param)
    $t->fieldlist_set($MyList)

    $t->fieldlist(0)            ## Remove field list (use default)
    $t->fieldlist_set()

    $t->fieldlist_force($MyList)## Set list; remove non-matching cols

    $t->fieldlist_truncate()    ## Just remove nonmatching cols

    $t->fieldlist_default()     ## Default field list (alpha-sorted)

    $t->fieldlist_add($MyName)    ## Append new name to custom list.
    $t->fieldlist_delete($MyName) ## Delete name from custom list.

A CTable object can optionally have a custom field list.  The custom
field list can store both the ORDER of the fields (which otherwise
would be unordered since they are stored as keys in a hash), and also
can be a subset of the fields actually in the object, allowing you to
temporarily ignore certain effectively-hidden fields for the benefit
of certain operations.  The custom field list can be changed or
removed at any time.

The custom field list is stored in the private _FieldList parameter.

fieldlist() always returns a list (reference).  The list is either the
same list as _FieldList, if present, or it is the result of calling
fieldlist_default().  In CTable, fieldlist_default() in turn calls
fieldlist_all() -- hence fieldlist() would yield an auto-generated
list of all fields in alphabetical order.

fieldlist_all() can be called directly to get a list of all fields
present regardless of the presence of a _FieldList parameter.  The
list is an alphabetical case-insensitively sorted list of all hash
keys whose names do not begin with an underscore.  

You could override this method if you want a different behavior. Or,
you could create your own custom field list by calling fieldlist_all()
and removing fields or ordering them differently.

To set a custom field list (in _FieldList), call fieldlist() or
fieldlist_set() with a list (reference).  The list must be a list of
strings (field names) that do not begin with underscore.  The object
owns the list you supply.

To remove a custom field list (and let the default be used), call
fieldlist(0) or fieldlist_set() with no arguments (these will return
the fieldlist that was deleted, if any).

fieldlist_freeze() "freezes" the fieldlist in its current state.  This
is equivalent to the following:

    $t->fieldlist_set($t->fieldlist());

... which would force the fieldlist to $t->fieldlist_all() if and only
if there is not already a custom _FieldList present.

IMPORTANT NOTE ABOUT PARTIAL FIELD LISTS: When setting a field list,
the object ensures that all fields (columns) mentioned in the list are
present in the object -- it creates empty columns of the correct
length as necessary.  However, it does NOT delete any fields not
mentioned in the field list.  This allows you to manipulate the field
list in order to have certain fields be temporarily ignored by all
other methods, then alter, restore, or remove it (allow it to revert
to default) and they will be effectively unhidden again.  Some methods
(such as cols(), write(), etc.) also allow you to specify a custom
field list that will override any other list just during the execution
of that method call but will not modify the object itself.

Call fieldlist_force() to set the list AND have any non-listed fields
also deleted at the same time (by calling fieldlist_truncate()
internally).  You can also just delete individual columns one-by-one,
of course, using the column-manipulation methods and the custom
fieldlist, if any, will be appropriately updated for you.

fieldlist_truncate() deletes any fields found in the table but not
currently present in _FieldList.  A hash of the deleted columns is
returned to the caller.  If there is no _FieldList, then this method
does nothing.

fieldlist_default() just calls fieldlist_all() in this implementation,
but could be changed in subclasses.

fieldlist_add() is the internal method that adds a new field name to
the custom field list (if present) and if the field name was not
already on the list.  It is called by other methods any time a new
column is added to the table.  Don't call it directly unless you know
what you're doing because the corresponding column won't be created.
(Instead, use col().)  The field name is appended to the end of the
existing custom field list.  If there is no custom field list, nothing
is done.

fieldlist_delete() is the internal method that deletes a field name
from the custom field list (if present).  It is called by other
methods when columns are deleted, but it does not actually delete the
columns themselves, so use with caution: deleting a field from the
custom field list effectively hides the field.  This method has no
effect, however, if there is no custom field list present.  So don't
call this method directly unless you know what you're doing.

=cut

sub fieldlist_all
{
    my $this        = shift;
    my $FieldList   = [sort {lc($a) cmp lc($b)} grep {!/^_/} keys %$this];
    
    return($FieldList);
}

sub fieldlist_default  ## Same as fieldlist_all() in this class.
{
    my $this        = shift;
    my $FieldList   = $this->fieldlist_all();

    return($FieldList);
}

sub fieldlist
{
    my $this        = shift;
    my ($FieldList) = @_;

    ## Set if specified.
    $this->fieldlist_set($FieldList) if defined($FieldList);

    ## Get and return.
    $FieldList      = $this->fieldlist_get();

    return($FieldList);
}

sub fieldlist_get
{
    my $this        = shift;
    my $FieldList   = $this->{_FieldList} || $this->fieldlist_default();

    return($FieldList);
}

sub fieldlist_hash ## ([$FieldList])
{
    my $this        = shift;
    my ($FieldList) = @_;
    $FieldList      ||= $this->fieldlist();
    my $FieldHash   = {}; @$FieldHash{@$FieldList} = undef;
    
    return($FieldHash);
}

sub fieldlist_set
{
    my $this        = shift;
    my ($FieldList) = @_;

    return($this->fieldlist_set_internal($FieldList, 0));
}

sub fieldlist_freeze
{
    my $this        = shift;
    return($this->fieldlist_set($this->fieldlist()));
}

sub fieldlist_force
{
    my $this        = shift;
    my ($FieldList) = @_;

    return($this->fieldlist_set_internal($FieldList, 1));
}

sub fieldlist_set_internal
{
    my $this        = shift;
    my ($FieldList, $Force) = @_;

    if (ref($FieldList) eq 'ARRAY')
    {

        ## Whether forcing or not, ensure all fields mentioned in the
        ## list actually exist and are the correct length.
        $this->fieldlist_check($FieldList);

        ## Set the custom list
        $this->{_FieldList} = [@$FieldList];
        
        ## In "force" mode, remove any non-listed columns.
        $this->fieldlist_truncate() if ($Force);
    }
    else
    {
        ## Remove the custom field list.
        $FieldList = delete $this->{_FieldList};
    }

    return($FieldList); ## Return the one that was set or deleted.
}

sub fieldlist_check
{
    my $this        = shift;
    my ($FieldList) = @_;
    
    $FieldList      ||= $this->fieldlist();

    ## Visit each field name in the current list.  Make sure it is
    ## present.

    foreach my $FieldName (@$FieldList)
    {
        ## The col method will the column exist if not present.
        $this->col($FieldName); 
    }
}

sub fieldlist_truncate
{
    my $this        = shift;

    my $FieldList   = $this->fieldlist();
    my $AllFields   = $this->fieldlist_all();
    my $FieldHash   = {}; @$FieldHash{@$FieldList} = undef;

    my $DeletedCols = {};
    
    foreach my $FieldName (@$AllFields)
    {
        if (!exists($FieldHash->{$FieldName}))
        {
            $DeletedCols->{$FieldName} = delete $this->{$FieldName};
        }
    }
    
    return($DeletedCols);
}

sub fieldlist_add
{
    my $this        = shift;
    my ($FieldName) = @_;
    
    if (ref($this->{_FieldList}) eq 'ARRAY')
    {
        my $FieldList   = $this->{_FieldList};
        my $FieldHash   = {}; @$FieldHash{@$FieldList} = undef;
        
        if (!exists($FieldHash->{$FieldName}))
        {
            push @$FieldList, $FieldName;
        }
    }
}

sub fieldlist_delete
{
    my $this        = shift;
    my ($FieldName) = @_;
    
    if (ref($this->{_FieldList}) eq 'ARRAY')
    {
        $this->{_FieldList} = [grep {$_ ne $FieldName} @{$this->{_FieldList}}];
    }
}

=pod 

=head1 DATA COLUMNS (FIELD DATA)

    ## Getting or setting data in entire columns

    $t->{$ColName}                ## Get a column you know exists
    $t->col($ColName)             ## Get a column or make empty one.
    $t->col_get($ColName)

    $t->col($ColName, $ListRef)   ## Set all of a column all at once.
    $t->col_set($ColName, $ListRef)
    $t->col_force($ColName, $ListRef) ## Add but don't check size or
                                      ##  add to custom field list

    $t->col_set($ColName, undef)  ## Delete a column completely
    $t->col_delete($ColName)      

    $t->col_empty()            ## An empty col presized for table
    $t->col_empty(22)          ## An empty col of another length
    $t->col_empty($Col)        ## An empty col sized to match another

    $t->col_default()          ## Default if req. column not found.

    $t->col_exists($Field)     ## Check existence of column
    $t->col_active($Field)     ## Restrict check to fieldlist()

    $t->cols($ColList)         ## Get list of multiple named columns
    $t->cols_hash($ColList)    ## Get hash " " "

    $t->col_rename($Old => $New) ## Change name of columns
    $t->col_rename($Old1 => $New1, $Old2 => $New2) ## Change several

A "column" is a field in the table and all its data.  The column's
field name is a key in the object itself, and may also optionally be
listed in a custom field list if present.  The column's data is the
key's value in the hash and is an array ref of values presumed to be
of the same data type (e. g. string, integer, etc.)

Sometimes the terms "column" and "field" are used interchangeably in
this documentation.

If you already know that a column exists (because you got it from the
fieldlist() method and you've not previously manipulated _FieldList
directly but instead carefully used the method calls available for
that), then you can safely get the column by just looking it up in the
object itself.

The col() method does the same thing, but forces the column to spring
into existence if it did not already (which can also have the
potentially unwanted side-effect of hiding coding errors in which you
retreive mis-named columns: so beware).  Columns brought into
existence this way will automatically be pre-sized (i.e. they will
will be created and set to whatever col_default() returns).

The col() or col_set() methods can also be used to set a column.  When
the column is set, the list you pass is automatically sized
(lengthened or truncated) to match the current length of the table.
If this is not what you want, then call col_force() which will not
check whether the new column matches the size of the others.

No matter how you set it, the object now "owns" the list you gave it.

As a convenience, col(), col_set() and col_force() return the column
that was set.  They silently discard any previous column.

All three methods of column setting will append the column to the
custom field list if one is present and the column name is not already
listed there (by calling fieldlist_add()).  They will also call the
extend() method to ensure all columns have the same length (either
others will be extended to match the length of the new one, or the new
one will be extended to match the length of the others).

col_delete() deletes a column.

col_empty() returns an anonymous list reference that is pre-sized to
the length of the table (by default).  You could use it to get an
empty column that you intend to fill up and then later insert into the
table or use to hold the results of an operation on other columns.  If
you want a different length, specify it as a number or as an array ref
whose length should be matched.

col_default() is the internal method that implements the "springing
into existence" of missing columns.  Currently it just calls
col_empty().  Other subclasses might want to have it return undef or a
string like "NO_SUCH_COLUMN" in order to help track programming errors
where nonexistent columns are requested.

cols($FieldList) returns an ordered list of the requested column
names.  If no list is given, then fieldlist() is used. 

cols_hash($FieldList) does the same as cols(), but the result is a
hash whose keys are the field names and whose values are the columns
-- much like the original object itself, but not blessed into the
class.  The resulting hash, however, could be used as the prototype
for a new Data::CTable object (by calling the new() method).  However,
be warned that both objects will think they "own" the resulting shared
so be careful what you do..... which brings us to this:

IMPORTANT NOTE ABOUT GETTING COLUMNS: The columns you retrieve from a
table are still "owned" by the table object as long as it lives.  If
you modify them, you are modifying the table's data.  If you change
their length, then you may be invalidating the table's own
expectations that all its columns have the same length.  So beware.

Just make yourself a copy of the data if that isn't what you want.
For example, instead of this:

    my $Foo =    $Table->col('Foo');   ## Reference to actual column

Do this:

    my $Foo = [@{$Table->col('Foo')}]; ## Shallow copy of the column

=cut

sub col ## ($ColName, [$Vector])
{
    my $this                = shift;
    my ($ColName, $Vector)  = @_;

    ## Set if specified.
    my $FoundVector = $this->col_set($ColName, $Vector) if defined($Vector);

    ## Get and return.
    ## If not specified, create it with col_default()
    my $Col         = $this->col_get($ColName);

    return($Col);
}

sub col_get 
{
    my $this                = shift;
    my ($ColName)           = @_;

    my $Col                 = ($this->{$ColName} || $this->col_add($ColName));

    return($Col);
}

sub col_add
{
    my $this                = shift;
    my ($ColName)           = @_;
    my $Col                 = $this->{$ColName}     = $this->col_empty();

    $this->fieldlist_add($ColName);
    return($Col);
}

sub col_set_internal    ## ($ColName, [$Vector], [$Force])
{
    my $this                        = shift;
    my ($ColName, $Vector, $Force)  = @_;

    my $Valid                       = (ref($Vector)           eq 'ARRAY');
    my $Existing                    = (ref($this->{$ColName}) eq 'ARRAY');
    
    ## Delete existing vector by this name...
    if (!$Valid && $Existing)
    {
        $Vector = delete $this->{$ColName}; ## Delete and save to return to caller.
        $this->fieldlist_delete($ColName);  ## Delete from field list if needed.
    }
    ## ...or add one...
    elsif ($Valid && !$Existing)
    {
        $this->{$ColName}       = $Vector;

        if (!$Force)
        {
            $this->extend();                ## Extend all vectors as needed to ensure same length.
            $this->fieldlist_add($ColName); ## Add to custom field list if needed.
        }
    }
    ## ...otherwise replace.
    elsif ($Valid)
    {
        $this->{$ColName}       = $Vector;

        if (!$Force)
        {
            $this->extend();        ## Extend all vectors as needed to ensure same length.
        }
    }
    
    return($Vector);            ## Return added or deleted vector for convenience.
}

sub col_delete ## ($ColName)
{
    my $this                = shift;
    my ($ColName)           = @_;

    return($this->col_set_internal($ColName));
}

sub col_set ## ($ColName, $Vector)
{
    my $this                = shift;
    my ($ColName, $Vector)  = @_;

    return($this->col_set_internal($ColName, $Vector));
}

sub col_force ## ($ColName, $Vector)
{
    my $this                = shift;
    my ($ColName, $Vector)  = @_;

    return($this->col_set_internal($ColName, $Vector, 1));
}

sub col_empty
{
    my $this        = shift;
    my ($Length)    = @_;

    ## Default to table length.  Or get length from sample column.
    $Length         = $this->length() unless defined($Length);
    $Length         = @$Length if ref($Length) eq 'ARRAY';

    my $Col         = [];
    $#$Col          = $Length - 1;

    return($Col);
}

sub col_default
{
    my $this                = shift;
    my $Col                 = $this->col_empty();

    return($Col);
}

sub cols ## ($ColNames)
{
    my $this        = shift;
    my ($ColNames)  = @_;
    $ColNames       ||= $this->fieldlist();
    my $Cols        = [map {$this->col($_)} @$ColNames];

    return($Cols);
}

sub cols_hash ## ($ColNames)
{
    my $this        = shift;
    my ($ColNames)  = @_;
    $ColNames       ||= $this->fieldlist();
    my $Cols        = $this->cols($ColNames);
    my $ColsHash    = {}; @$ColsHash{@$ColNames} = @$Cols;

    return($ColsHash);
}

sub col_exists ## ($ColName, [$FieldList])
{
    my $this                    = shift;
    my ($ColName, $FieldList)   = @_;

    ## Default list to search is ALL fields in object.
    $FieldList                  ||= $this->fieldlist_all();

    ## Disallow column names starting with underscore.
    return(0) if $ColName =~ /^_/;

    my $FieldHash               = $this->fieldlist_hash($FieldList);
    my $Exists                  = exists($FieldHash->{$ColName});

    return($Exists);
}

sub col_active ## ($ColName, [$FieldList])
{
    my $this                    = shift;
    my ($ColName, $FieldList)   = @_;

    ## Default list to search is only ACTIVE fields in object.
    $FieldList                  ||= $this->fieldlist();

    ## Disallow column names starting with underscore.
    return(0) if $ColName =~ /^_/;

    my $FieldHash               = $this->fieldlist_hash($FieldList);
    my $Exists                  = exists($FieldHash->{$ColName});

    return($Exists);
}

sub col_rename ## ($Old => $New, [$Old => New...])
{
    my $this                = shift;
    
    my $Success;

    my $Fields              = $this->fieldlist_all();

    my ($Old, $New);
    while (($Old, $New)     = splice(@_, 0, 2))
    {
        $this->warn("Invalid column name: $New"), next
            unless ($New =~ /^[^_]+/);
        
        $this->warn("Column to be renamed does not exist: $Old"), next
            unless  $this->col_exists($Old, $Fields);
        
        (($Old ne $New) && $this->warn("Failed to rename column $Old to $New: $New exists.")), next
            if      $this->col_exists($New, $Fields);
        
        my $Col             = $this->col($Old); ## Creates if not present.
        
        ## Rename the column...
        $this->{$New}       = delete $this->{$Old};
        
        ## Then make the same change to _FieldList, _SortOrder, _SortSpecs
        
        $this->{_FieldList} = [map {$_ = $New if $_ eq $Old; $_} @{$this->{_FieldList}}] if (defined($this->{_FieldList}));
        $this->{_SortOrder} = [map {$_ = $New if $_ eq $Old; $_} @{$this->{_SortOrder}}] if (defined($this->{_SortOrder}));
        $this->{_SortSpecs}->{$New} = delete $this->{_SortSpecs}->{$Old}                 if (defined($this->{_SortSpecs}) && 
                                                                                             (       $this->{_SortSpecs}->{$Old}));
    }
    
    $Success = 1;
  done:
    return($Success);
}

=pod

=head1 CLEANUP AND VALIDATION

    ## Performing your own cleanups or validations

    $t->clean($Sub)           ## Clean with custom subroutine
    $t->clean($Sub, $Fields)  ## Clean specified columns only

    ## Cleaning whitespace

    $t->clean_ws()        ## Clean whitespace in fieldlist() cols
    $t->clean_ws($Fields) ## Clean whitespace in specified cols

    ## Cleaning methods that map character sets

    $t->clean_mac_to_iso8859()
    $t->clean_mac_to_iso8859($Fields) 

    $t->clean_iso8859_to_mac()
    $t->clean_iso8859_to_mac($Fields) 

    ## Character mapping utilities (not methods)

    use Data::CTable qw(
                        ISORoman8859_1ToMacRoman
                        MacRomanToISORoman8859_1

                        ISORoman8859_1ToMacRoman_clean
                        MacRomanToISORoman8859_1_clean
                        );

    &ISORoman8859_1ToMacRoman(\ $Str)  ## Pass pointer to buffer
    &MacRomanToISORoman8859_1(\ $Str)  ## Pass pointer to buffer

    &ISORoman8859_1ToMacRoman_clean()  ## Operates on $_
    &MacRomanToISORoman8859_1_clean()  ## Operates on $_

One of the most important things you can do with your data once it's
been placed in a table in Perl is to use the power of Perl to scrub it
like crazy.

The built-in clean_ws() method applies a standard white-space cleanup
to selected records in every field in the fieldlist() or other list of
fields you optionally supply (such as fieldlist_all()).

It does the following cleanups that are deemed correct for the
majority of data out there:

    - Remove all leading whitespace, including returns (\n)
    - Remove all trailing whitespace, including returns (\n)
    - Convert runs of spaces to a single space
    - Convert empty string values back to undef to save space

Of course, depending on your data, clean_ws() might just be the first
thing you do in your cleanup pass.  There might be many more cleanups
you'd like to apply.

clean() is like clean_ws() except you supply as the first argument
your own cleaning subroutine (code reference).  It should do its work
by modifying $_.

Both clean_ws() and clean() apply cleaning ONLY to selected records.
If this isn't what you want, then select_all() before cleaning.

Since a cleanup subroutine can do ANY modifications to a field that it
likes, you can imagine some cleanup routines that also supply default
values and do other validations.

For example, a cleanup routine could convert every value in each field
to an integer, or apply minimum or maximum numerical limits:

    sub {$_ =     int($_)      }
    sub {$_ = max(int($_), 0)  }
    sub {$_ = min(int($_), 200)}

Or your cleanup routine could use regular expressions to do
capitalizations or other regularizations of data:

    sub Capitalize {/\b([a-z])([a-z]+)\b)/\U$1\E$2/g}

    $t->clean(\ &Capitalize , ['FirstName', 'LastName']);
    $t->clean(\ &PhoneFormat, ['Phone', 'Fax'         ]);
    $t->clean(\ &LegalUSZip,  ['HomeZip', 'WorkZip'   ]);

... and so on.  Cleanups are easy to write and quick and easy to apply
with Data::CTable.  Do them early!  Do them often!

=head2 Hints for writing cleanup routines

If your cleanup routine may be used to clean up fields that could be
empty/undef and empty/undef is a legal value, it should not touch any
undef values (unintentionally converting them to strings).

Finally, instead of setting any values to the empty string, it should
set them to undef instead.  This includes any values it might have
left empty during cleanup.  (Using undef instead of empty string to
represent empty values is one way that Data::CTable likes to save
memory in tables that may have lots of those.)

For an example of a well-behaved cleanup routine, consider the
following implementation of the builtin CleanWhitespace behavior:

    sub CleanWhitespace
    {
        return unless defined;    ## Empty/undef values stay that way
        s/ \s+$//sx;              ## Remove trailing whitespace
        s/^\s+ //sx;              ## Remove leading whitespace
        s/ +/ /g;                 ## Runs of spaces to single space
        $_ = undef unless length; ## (Newly?) empty strings to undef
    }

=head2 Roman character set mapping

The character set mapping cleanup routines can be used to convert
upper-ASCII characters bidirectionally between two popular Roman
Character sets -- Mac Roman 1 and ISO 8859-1 (also sometimes called
ISO Latin 1) -- i.e. the Western European Roman character sets.

By default, read() converts all incoming data fields in data files
with Mac line endings to ISO format when reading in.  Conversely,
write() does the reverse mapping (ISO to Mac) when writing a file with
Mac line endings.

However, you may wish to turn off these default behaviors and instead
apply the mappings manually, possibly just to certain fields.

For example, if a table contains fields with non-Roman character sets,
you would definitely not want to apply these mappings, and instead
might want to apply some different ones that you create yourself.

=head2 Utility routines for character mapping

This module can optionally export four utility routines for mapping
character Latin 1 character sets.  Always be sure to map the correct
direction -- otherwise you'll end up with garbage!  Be careful to only
pass Western Roman strings -- not double-byte strings or strings
encoded in any single-byte Eastern European Roman or non-Roman
character set.

    &ISORoman8859_1ToMacRoman(\ $Str)  ## Pass pointer to buffer
    &MacRomanToISORoman8859_1(\ $Str)  ## Pass pointer to buffer

These routines translate characters whose values are 128-255 from one
Western Roman encoding to another.  The argument is a string buffer of
any size passed by reference.

The functions return a count of the number of characters that were
mapped (zero or undef if none were).

    &ISORoman8859_1ToMacRoman_clean()  ## Operates on $_
    &MacRomanToISORoman8859_1_clean()  ## Operates on $_

These routines are variants of the above, but they're versions that
are compatible with clean() -- they operate on $_ and will take care
to leave undefined values undefined.  They do not have return values.

=head2 More advanced cleaning and validation

Unfortunately, clean() only lets you operate on a single field value
at a time -- and there's no way to know the record number or other
useful information inside the cleaning routine.

For really powerful cleaning and validation involving access to all
fields of a record as well as record numbers, see the discussion of
the calc() method and other methods for doing complex field
calculations in the next section.

=cut

sub clean
{
    my $this            = shift;
    my ($Sub, $Fields)  = @_;
    
    ## Default is fields in the list.
    $Fields             ||= $this->fieldlist();

    my $Sel             = $this->selection();
    
    foreach (@$Fields) {foreach (@{$this->col($_)}[@$Sel]) {&$Sub()}};
}

sub clean_ws
{
    my $this            = shift;
    return($this->clean(\ &CleanWhitespace, @_));
}

sub CleanWhitespace
{
    return unless defined;    ## Empty/undef values stay that way
    s/ \s+$//sx;              ## Remove trailing whitespace
    s/^\s+ //sx;              ## Remove leading whitespace
    s/ +/ /g;                 ## Runs of spaces to single space
    $_ = undef unless length; ## (Newly?) empty strings to undef
}

sub clean_mac_to_iso8859
{
    my $this            = shift;
    return($this->clean(\ &MacRomanToISORoman8859_1_clean, @_));
}

sub clean_iso8859_to_mac
{
    my $this            = shift;
    return($this->clean(\ &ISORoman8859_1ToMacRoman_clean, @_));
}

sub ISORoman8859_1ToMacRoman
{
    return($ {$_[0]} =~ 
    
    tr/\x80-\xFF/\xDE\xDF\xE2\xC4\xE3\xC9\xA0\xE0\xF6\xE4\xBA\xDC\xCE\xAD\xB3\xB2\xB0\xD4\xD5\xD2\xD3\xA5\xF8\xD1\xF7\xAA\xF9\xDD\xCF\xF0\xDA\xD9\xCA\xC1\xA2\xA3\xDB\xB4\xF5\xA4\xAC\xA9\xBB\xC7\xC2\xD0\xA8\xC3\xA1\xB1\xFA\xFE\xAB\xB5\xA6\xE1\xFC\xFF\xBC\xC8\xC5\xFD\xFB\xC0\xCB\xE7\xE5\xCC\x80\x81\xAE\x82\xE9\x83\xE6\xE8\xED\xEA\xEB\xEC\xC6\x84\xF1\xEE\xEF\xCD\x85\xD7\xAF\xF4\xF2\xF3\x86\xB7\xB8\xA7\x88\x87\x89\x8B\x8A\x8C\xBE\x8D\x8F\x8E\x90\x91\x93\x92\x94\x95\xB6\x96\x98\x97\x99\x9B\x9A\xD6\xBF\x9D\x9C\x9E\x9F\xBD\xB9\xD8/);

}

sub ISORoman8859_1ToMacRoman_clean
{
    return unless defined;    ## Empty/undef values stay that way

    tr/\x80-\xFF/\xDE\xDF\xE2\xC4\xE3\xC9\xA0\xE0\xF6\xE4\xBA\xDC\xCE\xAD\xB3\xB2\xB0\xD4\xD5\xD2\xD3\xA5\xF8\xD1\xF7\xAA\xF9\xDD\xCF\xF0\xDA\xD9\xCA\xC1\xA2\xA3\xDB\xB4\xF5\xA4\xAC\xA9\xBB\xC7\xC2\xD0\xA8\xC3\xA1\xB1\xFA\xFE\xAB\xB5\xA6\xE1\xFC\xFF\xBC\xC8\xC5\xFD\xFB\xC0\xCB\xE7\xE5\xCC\x80\x81\xAE\x82\xE9\x83\xE6\xE8\xED\xEA\xEB\xEC\xC6\x84\xF1\xEE\xEF\xCD\x85\xD7\xAF\xF4\xF2\xF3\x86\xB7\xB8\xA7\x88\x87\x89\x8B\x8A\x8C\xBE\x8D\x8F\x8E\x90\x91\x93\x92\x94\x95\xB6\x96\x98\x97\x99\x9B\x9A\xD6\xBF\x9D\x9C\x9E\x9F\xBD\xB9\xD8/;
}

sub MacRomanToISORoman8859_1
{
    return($ {$_[0]} =~ 
        
    tr/\x80-\xFF/\xC4\xC5\xC7\xC9\xD1\xD6\xDC\xE1\xE0\xE2\xE4\xE3\xE5\xE7\xE9\xE8\xEA\xEB\xED\xEC\xEE\xEF\xF1\xF3\xF2\xF4\xF6\xF5\xFA\xF9\xFB\xFC\x86\xB0\xA2\xA3\xA7\x95\xB6\xDF\xAE\xA9\x99\xB4\xA8\x8D\xC6\xD8\x90\xB1\x8F\x8E\xA5\xB5\xF0\xDD\xDE\xFE\x8A\xAA\xBA\xFD\xE6\xF8\xBF\xA1\xAC\xAF\x83\xBC\xD0\xAB\xBB\x85\xA0\xC0\xC3\xD5\x8C\x9C\xAD\x97\x93\x94\x91\x92\xF7\xD7\xFF\x9F\x9E\xA4\x8B\x9B\x80\x81\x87\xB7\x82\x84\x89\xC2\xCA\xC1\xCB\xC8\xCD\xCE\xCF\xCC\xD3\xD4\x9D\xD2\xDA\xDB\xD9\xA6\x88\x98\x96\x9A\xB2\xBE\xB8\xBD\xB3\xB9/);

}

sub MacRomanToISORoman8859_1_clean
{
    return unless defined;    ## Empty/undef values stay that way

    tr/\x80-\xFF/\xC4\xC5\xC7\xC9\xD1\xD6\xDC\xE1\xE0\xE2\xE4\xE3\xE5\xE7\xE9\xE8\xEA\xEB\xED\xEC\xEE\xEF\xF1\xF3\xF2\xF4\xF6\xF5\xFA\xF9\xFB\xFC\x86\xB0\xA2\xA3\xA7\x95\xB6\xDF\xAE\xA9\x99\xB4\xA8\x8D\xC6\xD8\x90\xB1\x8F\x8E\xA5\xB5\xF0\xDD\xDE\xFE\x8A\xAA\xBA\xFD\xE6\xF8\xBF\xA1\xAC\xAF\x83\xBC\xD0\xAB\xBB\x85\xA0\xC0\xC3\xD5\x8C\x9C\xAD\x97\x93\x94\x91\x92\xF7\xD7\xFF\x9F\x9E\xA4\x8B\x9B\x80\x81\x87\xB7\x82\x84\x89\xC2\xCA\xC1\xCB\xC8\xCD\xCE\xCF\xCC\xD3\xD4\x9D\xD2\xDA\xDB\xD9\xA6\x88\x98\x96\x9A\xB2\xBE\xB8\xBD\xB3\xB9/;

}

=pod

=head1 CALCULATIONS USING calc()

    ## Calculate a new field's values based on two others

    $t->calc($Sub)              ## Run $Sub for each row, with 
                                ##  fields bound to local vars

    $t->calc($Sub,  $Sel)           ## Use these row nums
    $t->calc($Sub, undef, $Fields)  ## Use only these fields
    $t->calc($Sub,  $Sel, $Fields)  ## Use custom rows, fields

    my $Col = $t->calc($Sub)    ## Gather return vals in vector


    ## Example 1: Overwrite values in an existing column.

    $t->calc(sub{no strict 'vars'; $Size = (stat($Path))[7]});


    ## Example 2: Create empty column; fill fields 1 by 1

    $t->col('PersonID');
    $t->calc(sub{no strict 'vars'; $PersonID = "$Last$First"});


    ## Example 3: Calculate values; put into to table if desired

    $PersonID = $t->calc(sub{no strict 'vars'; "$Last$First"});
    $t->sel('PersonID', $PersonID);


    ## Example 4: Using fully-qualified variable names

    $t->calc(sub{$main::PersonID = "$main::Last$main::First"});

calc() runs your custom calculation subroutine $Sub once for every row
in the current selection() or other list of row numbers that you
specify in the optional $Sel argument.

This lets you apply a complex calculation to every record in a table
in a single statement, storing the results in one or more columns, or
retrieving them as a list.

Your custom subroutine may refer to the value in any field in the
current row by using a global variable with the field's name:

For example, if the table has fields First, Last, and Age, then $Sub
may use, modify, and set the variables $First, $Last, $Age.  (Also
known as $main::First, $main::Last, $main::Age).

Modifying any of these specially-bound variables actually modifies the
data in the correct record and field within the table.

By default, the fields available to $Sub are all fields in the table.
calc() must bind all the field names for you for each row, which can
be time-consuming for tables with very large numbers of fields.

You can speed up the operation of calc() by listing only the fields
your $Sub needs in the optional parameter $Fields.  Any field names
you don't mention won't be available to $Sub.  Conversely, calc() will
run faster because it can bind only the fields you actually need.

If you include non-existent fields in your custom $Fields list, calc()
creates them for you before $Sub runs the first time.  Then your $Sub
can store field values into the new column, referring to it by name.

Variables in $Sub are in the "main" package.  So you should set $Sub
to use pacakge "main" in case the rest of your code is not in "main".

Similarly, if you "use strict", Perl will complain about global
variables in $Sub.  So you may need to assert "no strict 'vars'".

    {   package Foo; use strict;
    
        $t = ...;
        $t->calc(sub {package main; no strict 'vars'; 
                      $Age = int($Age)});
    }

    ## Or this:

    {   package Foo;  use strict;

        $t = ...;
        {   package main; no strict 'vars';
            my $Sub = sub {$Age = int($Age)};
        }
        $t->calc($Sub);
    }

You may be able to get around both problems more easily by prefixing
each variable reference in $Sub with "main::".  This takes care of the
package name issue and bypasses "use strict" at the same time, at the
slight cost of making the calculation itself a bit harder to read.

    $t->calc(sub {$main::Age = int($main::Age)}); ## OK in any package

In addition to the field names, the following three values are defined
during each invocation of $Sub:

    $_r ($main::_r) -- the row number in the entire table
    $_s ($main::_s) -- the item number in selection or $Recs
    $_t ($main::_t) -- the table object itself

You could use these values to print diagnostic information or to
access any of the data, parameters, or methods of the table itself
from within $Sub.  Or you could even calculate field values using $_r
or $_s.

For example, after searching & sorting, you could make a field which
preserves the resulting sort order for future reference:

    $t->col('Ranking');      ## Create the empty column first.
    $t->calc(sub{$main::Ranking = $main::_s});

This last example is equivalent to:

    $t->sel(Ranking => [0 .. $#{$t->selection()}]); ## See sel() below


=head1 "MANUAL" CALCULATIONS

calc() (see previous secion) is the briefer, more elegant way to do
batch calculations on entire columns in a table, but it can be
slightly slower than doing the calculations yourself.

If you have extremely large tables, and you notice the processing time
for your calculations taking more than a second, you might want to
rewrite your calculations to use the more efficient techniques shown
here.

You will often need to create new calculated columns based on one or
more existing ones, and then either insert the columns back in the
tables or use them for further calculations or indexing.

Examples 1a and 1b create a new field 'NameOK' containing either the
string "OK" or undef (empty) depending on whether the field 'Name' is
empty.  Just use map() to iterate over the existing values in the
other column, binding $_ to each value in turn.

    ### Example 1a: Calculation based on one other column

    ## Retrieve column
    my $Name    = $t->col('Name');
    
    ## Make new column
    my $NameOK  = [map {!!length && 'OK'} @$Name];

    ## Insert column back into table:
    $t->col(NameOK => $NameOK);

    ### Example 1b: Same calculation, in a single statement:

    $t->col(NameOK => [map {!!length && 'OK'} @{$t->col('Name')}]);

In order to iterate over MULTIPLE columns at once, you need a list of
the row numbers generated by $t->all() so you can index the two
columns in tandem.  Then, you use map to bind $_ to each row number,
and then use the expression $t->col($ColName)->[$_] to retreive each
value.

Examples 2a and 2b demonstrate this method.  They create a new field
'FullName' which is a string joining the values in the 'First' and
'Last' columns with a space between.

    ### Example 2a: Calculation based on multiple columns

    ## Retrieve columns
    my $First = $t->col('First');  
    my $Last  = $t->col('Last' );

    ## Retreive row nums
    my $Nums  = $t->all();

    ## Calculate a new column based on two others
    my $Full  = [map {"$First->[$_] $Last->[$_]"} @$Nums];

    ## Add new column to the table
    $t->col(FullName => $Full);

    ### Example 2b: Same calculation, in a single statement:

    $t->col(FullName => 
            [map {"$t->col('First')->[$_] t->col('Last')->[$_]"} 
             @{$t->all()}]);

In examples 1 and 2, you create entirely new columns and then add or
replace them in the table.

Using the approach in Examples 3a and 3b, you can assign calculated
results directly into each value of an existing column as you go.

    ## Example 3a: Calculate by assigning directly into fields...

    my $A = $t->col->('A'); ## This column will be modified
    my $B = $t->col->('B');
    my $C = $t->col->('C');

    foreach @($t->all()) {$A->[$_] = $B->[$_] + $C->[$_];}

    ## Example 3b: Same calculation, in a single statement:

    foreach @($t->all()) {($t->col('A')->[$_] =
                           $t->col('B')->[$_] + 
                           $t->col('C')->[$_])};

Before writing your code, think about which calculation paradigms best
suit your needs and your data set.

Just as Perl Hackers know: There's More Than One Way To Do It!

=cut

{}; ## Get emacs to indent correctly.

sub calc
{
    ## We operate in package main for this subroutine so the local
    ## object and row number can be available to the caller's $Sub.

    ## The following local vars will be available to $Sub
    ## $_r ($main::_r) -- the row number in the entire table
    ## $_s ($main::_s) -- the row number in selection or $Recs
    ## $_t ($main::_t) -- the table object itself

    package main;           
    use vars qw($_r  $_s  $_t);
    local      ($_r, $_s, $_t);

    $_t                         = shift;    
    my ($Sub, $Recs, $Fields)   = @_;

    ## These optional params default to current field and current sel

    $Recs                       ||= $_t->selection();
    $Fields                     ||= $_t->fieldlist_all();
    
    ## Local copy of symbol table.  Didn't seem to help.  Odd.
    ## local %main::;   
    
    ## We'll build a column of return values from $Sub if needed.
    my $WantVals                = defined(wantarray);
    my $Vals                    = $_t->col_empty() if $WantVals;

    ## Call col() on each field in list to make sure it exists.
    foreach (@$Fields) {$_t->col($_)};

    ## Don't overwrite fields in package main.
    ## First save them
    no strict 'refs';
    my %tmp;
    $tmp{$_} = \${$_} for(@$Fields);    

    foreach $_s (0..$#$Recs)
    {
        $_r = $Recs->[$_s];

        ## Bind $FieldName1, $FieldName2, (etc. for each field name in
        ## $Fields) point to address of the current value for that
        ## field in this record.

        no strict 'refs';
        foreach my $F (@$Fields) {*{$F} = \ $_t->{$F}->[$_r]};

        ## Now $Sub may refer to $_r, $_s, $_t, and ${any field name}

        ## Call $Sub and capture return values iff caller wants them
        ($WantVals ? $Vals->[$_r] = &$Sub() : &$Sub());
    }
    
    ## Restore fields in main
    *{$_} = $tmp{$_} for(@$Fields);    

    ## Return scalar column ref unless return context is undef
    return($WantVals ? $Vals : ());
}

=pod

=head1 INDEXES

    ## Make indexes of columns or just selected data in columns

    my $Index1 = $t->index_all($Key);  ## entire column
    my $Index2 = $t->index_sel($Key);  ## selected data only

    ## Make hashes of 2 columns or just selected data in columns

    my $Index1 = $t->hash_all($KeyFld, $ValFld);  ## entire column
    my $Index2 = $t->hash_sel($KeyFld, $ValFld);  ## selected data

index_all() creates and returns a hash (reference) that maps keys
found in the column called $Key to corresponding record numbers.

Ideally, values in $Key would be unique (that's up to you).

If any values in $Key are NOT unique, then later values (higher record
numbers) will be ignored.

index_sel() creates and returns a hash (ref) that maps keys found in
the SELECTED RECORDS of column $Key to corresponding record numbers.

Any keys in unselected records are ignored.  Otherwise, the behavior
is equivalent to index_all().

hash_all() and hash_sel() are similar, except they create and return
hashes whose keys are taken from column $KeyFld, but whose values are
from $ValFld in the corresponding records.

So, for example, imagine you have a tab-delimited file on disk with
just a single tab per line (2 fields) and no header row.  The entries
on the left side of the tab on each line are keys and the right side
are values.  You could convert that file into a hash in memory like
this:

    my $t = Data::CTable->new({_HeaderRow=>0, _FieldList=>[qw(F1 F2)]}, 
                              "DeptLookup.txt");

    my $DeptLookup = $t->hash_all(qw(F1 F2));

=head2 Reverse indexes

If you'd like an index mapping record number to key, just get
$t->col($Key).  That's what the data columns in Data::CTable are.

=cut

sub index_all
{
    my $this        = shift;
    my ($Key)       = @_;

    my $Index       = {}; @$Index{reverse @{$this->col($Key)}} = reverse @{$this->all()};

    return($Index);
}

sub index_sel
{
    my $this        = shift;
    my ($Key)       = @_;
    
    my $Index       = {}; @$Index{reverse @{$this->sel($Key)}} = reverse @{$this->selection()};
    
    return($Index);
}   


sub hash_all
{
    my $this        = shift;
    my ($Key, $Val) = @_;

    my $Hash        = {}; @$Hash{reverse @{$this->col($Key)}} = reverse @{$this->col($Val)};

    return($Hash);
}

sub hash_sel
{
    my $this        = shift;
    my ($Key, $Val) = @_;
    
    my $Hash        = {}; @$Hash{reverse @{$this->sel($Key)}} = reverse @{$this->sel($Val)};
    
    return($Hash);
}   


=pod 

=head1 DATA ROWS (RECORDS)

    ## Getting or setting rows / records

    $t->row($Num)             ## Get a row or make empty one.
    $t->row_get($Num)

    $t->row($Num, $HashRef)   ## Set all of a row all at once.
    $t->row_set($Num, $HashRef) 

    $t->row_set($Num, undef)  ## Delete a row completely
    $t->row_delete($Num)      
    $t->row_delete($Beg, $End)## Delete a range of rows      

    $t->row_move($Old, $New)   ## Move a row to before $New

    $t->row_empty()            ## An empty hash
    $t->row_exists($Num)       ## True if $Num < $t->length()

    $t->rows($RowList)         ## Get list of multiple row nums

    $t->row_list($Num)          ## Get row vals as a list
    $t->row_list($Num, $Fields) ## Get row vals: specified fields

    $t->row_list_set($Num, undef, $Vals)   ## Set row vals as a list
    $t->row_list_set($Num, $Fields, $Vals) ## Set row vals as a list
    $t->row_list_set($Num, $Fields)        ## Set vals to empty/undef

Usually, when working with Data::CTable objects, you are operating on
entire columns or tables at a time (after all: any transformation you
do on one record you almost always want to do on all records or all
selected ones).  

You should very rarely need to access data by retrieving rows or
setting rows, moving them around individually, and so on.  (It's much
cleaner, and much more efficient to manipulate the selection() (list
of selected row numbers) instead -- just delete a row number from the
selection, for example, and then for most operations it's almost as if
the row is gone from the table, except the data is really still
there.)  

However, if on rare occasions you really do need direct row
operations, you're reading the right section.

A row is generally accessed as a hash.  The hash you provide or get
back is not saved by the object in any way.  Data values are always
copied in or out of it, so you always "own" the hash.

Rows are specified by $Num -- the row number with in the unsorted
columns (the raw data in the table).  These numbers are just array
indices into the data columns, and so their legal range is:

    [0 .. ($t->length() - 1)]     ##   (Zero-based row numbering.)

The row hash (or "record") has keys that are field names and values
that are copies of the scalar values stored in the data columns within
the table.

row() always copies only the fields in fieldlist(), except for
row_list() which allows you to specify an optional $Fields parameter
which can override the current fieldlist().

If the fieldlist happens to be a subset of all fields, but you really
want to get all fields in your record, then call fieldlist_set(0)
first to permanently or temporarily delete it.

row() and row_get() always return a hash.

row($Num, $Hash), row_set() take a hash and set just the fields you
specify in the hash (in the given row of course).  Any non-existent
field names in the hash are created, so be careful.

In fact, in general with either getting or setting rows, any
non-existent fields mentioned will be created for you (by internally
calling col()).  So you could build a whole table of 100 rows by
starting with an empty, new, table and setting row 99 from a hash that
gives the field names.

Setting a row number higher than any existing row number with row(),
row_set() or row_force() will automatically set the new length of the
entire table to match (extending all the columns with empty rows as
necessary).

IMPORTANT: IF YOU SIMPLY MUST ADD ROWS SEQUENTIALLY, do not let the
table auto-extend by one with each row you set.  This is slow and gets
rapidly slower if there's lots of data because the arrays holding the
data columns will keep getting reallocated on every insert.  Instead,
first pre-extend the table to your highest row number by calling
length($Len), and then set your rows.  Or easier: if convenient just
set your rows starting with the highest-numbered one first.  If you
don't know how many you'll have, guess or estimate and pre-extend to
the estimated number and then cut back later.  This will be faster
than extending all columns by one each time.

row_delete() removes a row or range of rows completely from the table.
Any rows above the deleted ones will move down and the table's
length() will decrease.  If the data columns are very large, this
could be a bit slow because a lot of data could be moved around.  The
low and high row numbers will be limited for you to 0 and length() -
1, respectively.  Null ranges are OK and are silently ignored.  The
range is inclusive, so to delete just row 99, call row_delete(99) or
row_delete(99,99).

EFFICIENCY NOTE: Don't call row_delete() to remove lots of individual
rows.  Instead, select those row numbers by setting the selection (if
not already selected), and then invert the selection using
selection_invert(), so the undesired rows are deselected, and then use
the cull() method to rewrite the entire table at once.  The deselected
rows will be omitted very efficiently this way.

row_move() moves a row from its $Old row number to the position before
the row currently in row $New (specify $New = length() to move the row
to the end).  Again, in shuffling data in columns, lots of data could
get moved around by this operation, so expect it to be slow.  If as
with row_delete(), if you will be doing several moves, consider
building an appropriate selection() first, and then using cull()
instead.

Using row_delete() and row_move() to shift records around changes the
record numbers of the affected records and many others in the table.
The record numbers in the custom selection, if any, are updated to
reflect these changes, so the records that were selected before will
still be selected after the move (except those that were deleted of
course).  If you had a private copy of the selection, your copy will
likely become outdated after these operations.  You should get it
again by calling selection().

row_empty() returns a hash whose keys are the entries in fieldlist()
and whose values are undef.  (You could use it to fill in values
before calling row_set()).  Note: in this class, row_empty() does
exactly the same thing as fieldlist_hash() when the latter is called
with no arguments.

row_exists() returns true if C<(($Num E<gt>= 0) && ($Num E<lt> $t-E<gt>length()))>.

rows() calls row() for each row num in a list and returns a list of
the resulting hashes.

row_list() gets row values as a list instead of a hash.  They appear
in the order specified in fieldlist() unless you supply an optional
$Fields parameter listing the fields you want to get.

row_list_set() sets row values as a list instead of a hash.  Pass your
own $Fields list or undef and fieldlist() will be used.  $Values
should be a list with the same number of values as fields expected;
any shortage will result in undef/empty values being set.

=cut

{}; ## Get emacs to indent correctly.

sub row
{
    my $this        = shift;
    my ($Num, $Row) = @_;

    ## Set if specified.
    return($this->row_set($Num, $Row)) if defined($Row);

    ## Else get.
    return($this->row_get($Num));
}

sub row_get
{
    my $this    = shift;
    my ($Num)   = @_;

    my $Fields  = $this->fieldlist();
    my $Row     = {}; @$Row{@$Fields} = map {$this->col($_)->[$Num]} @$Fields;

    return($Row);
}

sub row_set
{
    my $this        = shift;
    my ($Num, $Row) = @_;

    ## We thoughtfully sort the keys in case columns will get created
    ## in this order.

    my $Fields      = [sort keys %$Row];    

    ## Pre-extend the table to accommodate row $Num if necessary.
    ## This will do nothing if there are not yet any fields in the
    ## table (and the length will still be effectively zero).

    $this->length($Num + 1) unless $this->length() >= $Num + 1;
    
    ## Insert into columns, creating them if necessary.
    foreach (@$Fields) {$this->col($_)->[$Num] = $Row->{$_}};
    
    return($Row);   ## Why not?
}

sub row_delete
{
    my $this            = shift;
    my ($First, $Last)  = @_;

    ## Nothing to do if $First not specified.
    return() unless defined($First);

    ## Default is $Last is same as first (remove one row only)
    $Last = $First unless defined($Last);

    my $LastIndex       = $this->length() - 1;

    ## Restrict the range to meaningful values.
    $First = max($First, 0         );   ## First could be very high, to indicate a null range.
    $Last  = min($Last,  $LastIndex);   ## Last could be negative, like -1, to indicate null range.

    ## Nothing to do if the range is empty.
    return() if $Last < $First;

    my $Fields          = $this->fieldlist();

    my $RangeSize       = ($Last - $First + 1);
    
    foreach (@$Fields) {splice @{$this->col($_)}, $First, $RangeSize};
    
    ## Here we could have trapped all the list segments we spliced out
    ## and return them in nice CTable-ish hash.  Maybe we will some
    ## day.  This would be a way to split a range of rows out of a
    ## table object to create another table object.  We could even
    ## call it the "split" method...
    
    ## After deleting the rows, we need to adjust the _Selection if
    ## present.  Deleted row numbers in the selection need to be
    ## omitted; row numbers greater than the range of deleted ones
    ## need to be decreased by the size of the range reduction, and
    ## others need to be left untouched.
    
    $this->{_Selection} = 
        [map 
         {
             ($_  <  $First ? $_ :  ## Before range: pass through.
              ($_ <= $Last  ? () :  ## In range: omit.
               $_ - $RangeSize))    ## After range: reduce by range size
             } @{   $this->{_Selection}}] 
                 if $this->{_Selection};
}

sub row_move
{
    my $this        = shift;
    my ($Old, $New) = @_;
    
    ## $Old and $New are required params and must not be undef.
    goto done unless defined($Old) && defined($New);
    
    ## If $Old and $New are the same or one apart, there's nothing to do.
    goto done if ($New == $Old);        ## This would mean a no-op.
    goto done if ($New == $Old + 1);    ## This would mean a no-op.
    
    my $Length      = $this->length();
    
    ## Ensure both $Old and $New are legal indices.
    goto done if (($Old < 0) || ($Old > $Length - 1));
    goto done if (($New < 0) || ($New > $Length    ));  ## New has a range up to $Length, meaning move to end.

    my $Fields      = $this->fieldlist_all();

    if ($Old < $New)    ## Move forward (to higher / later row num)
    {
        ## Delete from the lower position and insert into higher -- MINUS ONE to account for prior shortening.
        my $Row = {}; 
        foreach (@$Fields) {$Row->{$_} = splice(@{$this->col($_)}, $Old    , 1            )};
        foreach (@$Fields) {             splice(@{$this->col($_)}, $New - 1, 0, $Row->{$_})};

    }
    else                ## Move backward (to lower / earlier row num)
    {
        ## Delete from the higher position and insert into lower.
        my $Row = {}; 
        foreach (@$Fields) {$Row->{$_} = splice(@{$this->col($_)}, $Old    , 1            )};
        foreach (@$Fields) {             splice(@{$this->col($_)}, $New    , 0, $Row->{$_})};

    }

    ## After moving the rows, we need to adjust the _Selection if
    ## present.  Row numbers outside the shuffled range stay the same;
    ## the moved row number(s) change; others (inside the range) get
    ## shifted down or up by 1.

    if ($Old < $New)    ## Move forward / higher / later
    {
        $this->{_Selection} = 
            [map 
             {
                 ($_   == $Old ? $New - 1 : ## Moved row: change num to new - 1
                  ($_  <  $Old ? $_       : ## Less  than $Old: no change
                   ($_ >= $New ? $_       : ## Grtr= than $New: no change
                    $_ - 1)))               ## In range: shift down by 1.
                 } @{   $this->{_Selection}}] 
                     if $this->{_Selection};
        
    }
    else    ## Move backward / lower / earlier
    {
        $this->{_Selection} = 
            [map 
             {
                 ($_   == $Old ? $New     : ## Moved row: change num to new
                  ($_  >= $Old ? $_       : ## Grtr= than $Old: no change
                   ($_ <  $New ? $_       : ## Less  than $New: no change
                    $_ + 1)))               ## In range: shift up by 1.
                 } @{   $this->{_Selection}}] 
                     if $this->{_Selection};
    }

  done:
    return;
}

sub row_empty
{
    my $this        = shift;

    my $Fields      = $this->fieldlist();
    my $Row         = {}; @$Row{@$Fields} = undef;

    return($Row);
}

sub row_exists
{
    my $this        = shift;
    my ($Num)       = @_;

    return(($Num >= 0) && ($Num < $this->length()));
}

sub rows
{
    my $this        = shift;
    my ($Nums)      = @_;
    
    return([map {$this->row($_)} @$Nums]);
}

sub row_list
{
    my $this            = shift;
    my ($Num, $Fields)  = @_;

    ## $Fields argument is optional and defaults to fieldlist();
    $Fields             ||= $this->fieldlist();

    my $Row             = [map {$this->col($_)->[$Num]} @$Fields];
    return($Row);
}

sub row_list_set
{
    my $this                    = shift;
    my ($Num, $Fields, $Vals)   = @_;

    ## $Fields argument is optional and defaults to fieldlist();
    $Fields             ||= $this->fieldlist();

    ## $Vals is optional and defaults to [].
    $Vals               ||= [];

    ## Pre-extend the table to accommodate row $Num if necessary.
    $this->length($Num + 1) unless $this->length() >= $Num + 1;
    
    ## Set the $Vals in row $Num in the order given by $Fields.
    foreach (0..$#$Fields) {$this->col($Fields->[$_])->[$Num] = $Vals->[$_]}
}

=pod

=head1 ROW / RECORD COUNT (TABLE LENGTH)

    ## Getting or setting table length

    $t->length()        ## Get length
    $t->length_get()

    $t->length(22)      ## Set length (truncate or pre-extend)
    $t->length_set(22)

    $t->extend()        ## Set length of all columns to match longest

The length* methods assume the table already has columns of equal
length.  So the length of the table is the length of any field taken
at random.  We choose the first one in the field list.

Setting the length will truncate or pre-extend every column in the
table to a given length as required.  

(Pre-extending means setting each column's length via $# so that it
has the correct number of entries already allocated (and filled with
undef) so that operations that fill up the table can be done much more
quickly than with push().

However, if a new column has been added directly, or a table has been
constructed out of columns whose length may not initially match, the
extend() method may be (should be) called to inspect all columns and
extend them all to match the longest one.  Note that extend() operates
on all fields in the object, ignoring the custom _FieldList if any.

The length of a table with no columns is zero.

=cut

sub length 
{
    my $this        = shift;
    my ($Length)    = @_;
    
    return(defined($Length) ? 
           $this->length_set($Length) :
           $this->length_get());
}

sub length_get
{
    my $this        = shift;

    my $FieldList   = $this->fieldlist();
    my $FirstField  = $FieldList->[0];
    my $Col         = $this->{$FirstField};
    my $Length      = (ref($Col) eq 'ARRAY' ? @$Col+0 : 0);

    return($Length);
}

sub length_set
{
    my $this        = shift;
    my ($Length)    = @_;

    ## Apply the length-setting logic to any field found in the hash
    ## OR listed in the field list.  They will all be created if not
    ## already present.

    my $FieldList   = [@{$this->fieldlist_all()}, @{$this->fieldlist()}];
    
    foreach my $FieldName (@$FieldList) 
    {
        $#{$this->col($FieldName)} = ($Length - 1); ## $Length = 0 => $# = -1 => empty list.
    };

    ## Since records might have been deleted, re-validate the
    ## _Selection, if it is present.

    $this->selection_validate();
    
    return($Length);
}

sub extend
{
    my $this        = shift;
    my $Length      = 0;

    ## Find the length of the longest vector...
    
    foreach (@{$this->fieldlist_all()}) {$Length = max($Length, $#{$this->{$_}} + 1)};

    ## ...and set them all to be that length.

    $this->length_set($Length);
}

=pod 

=head1 SELECTIONS

    ## Getting or setting the custom selection list itself (_Selection)

    $t->selection()               ## Get sel if any; else all()
    $t->selection_get()

    $t->selection($List)          ## Set sel (list of rec nums)
    $t->selection_set($List)
    
    $t->selection(0)              ## Remove sel (select all)
    $t->selection_set(undef)      
    $t->selection_delete()
    $t->select_all()

    $t->selection_inverse()       ## Get inverse copy of selection
    $t->select_inverse()          ## Invert the selection

    $t->selection_validate()      ## Remove invalid #s from sel

    ## List of all rec nums present (regardless of selection)

    $t->all()                     

    ## Getting or setting just selected fields in columns
    ## (as contrasted with col() and friends).

    $t->sel($ColName)             ## Get col but only records in sel
    $t->sel_get($ColName)

    $t->sel($ColName, $ListRef)   ## Set selected fields in col...
    $t->sel_set($ColName, $ListRef) ##... in selection order

    $t->sel_set($ColName)         ## Set selected fields to undef
    $t->sel_clear($ColName)

    $t->sels($ColList)            ## Like cols, but selected fields
    $t->sels_hash($ColList)       ## " "   cols_hash()... " " " "

    ## Finding out size of selection (number of rows)

    $t->sel_len()                 ## Get number of selected rows.

A selection is an ordered list of record numbers.  The record numbers
in the selection may be a subset of available records.  Furthermore,
they may be in non-record-number order, indicating that the records
have been sorted.

Record numbers are numeric array indices into the columns in the
table.  It is an error for any selection list to contain an index less
than zero or greater than (length() - 1), so if you set a selection
explicitly, be careful.

Any selection list you get or set belongs to the object.  Be careful
of modifying its contents.

The custom selection, if any, is stored internally in the _Selection
parameter.  If this parameter is absent, the selection defaults to
all() -- i.e. a list of all record numbers, in order:
[0..($this->length() - 1)] (which becomes [] if length() is 0).

REMEMBER: length() is one-based, but record numbers are zero-based.

Removing the selection (that is, removing the LIST itself of which
records are selected), is the same as selecting all records.
consequently, selection(0), selection_delete(), and select_all() are
all synonymous.

selection_validate() removes any entries from the current _Selection
list (if any) that are not valid record numbers -- i.e. it removes any
record whose integer value is < 0 or greater than length() - 1.  This
routine is mainly used by other methods that might delete records,
such as length_set().

Getting or setting just selected data from columns

Sometimes, you don't want to get/set entire columns, you instead want
to get or set data in just the selected fields in a column.

The sel(), sel_get(), sel_set(), sels() and sels_hash() methods are
analagous to the corresponding col(), ... cols_hash() methods except
in these two ways:

- the 'sels' variants get or set just selected data, as determined by
the current selection(), which gives an ordered list of the selected /
sorted records.

- the 'sels' variants all make COPIES of the data you request or
supply -- the data is copied out of or into the correspnding column.
So, you "own" any vector you pass or receive in reply.

So, for example, imagine you have just set selection() to only list
record numbers where the LastName field is not empty.  Then you have
called sort() to sort those record numbers by the LastName field.  You
could then call $t->sel('LastName') to get a sorted list of all
non-empty last names.

It might be helpful to think of "sel" as short for "selected".  So
$t->sel('LastName') would mean "get the selected field values from the
LastName field".

=cut

sub selection
{
    my $this        = shift;
    my ($Selection) = @_;

    ## Set if specified.
    $this->selection_set($Selection) if defined($Selection);
    
    ## Get and return.
    $Selection = $this->selection_get();

    return($Selection);
}

sub selection_get
{
    my $this = shift;

    my $Selection = $this->{_Selection} || $this->selection_default();

    return($Selection);
}

sub selection_set
{
    my $this        = shift;
    my ($Selection) = @_;

    if (ref($Selection) eq 'ARRAY')
    {
        ## Set if specified...
        $this->{_Selection} = $Selection;
    }
    else
    {
        ## Otherwise, delete and return original if any.
        $Selection = delete $this->{_Selection};
    }
    
    return($Selection);
}

sub selection_delete
{
    my $this        = shift;
    $this->selection_set();
}

sub select_all
{
    my $this        = shift;
    $this->selection_set();
}

sub select_none
{
    my $this        = shift;
    $this->selection_set([]);
}

sub selection_default
{
    my $this = shift;

    my $Selection   = $this->all();

    return($Selection);
}

sub all
{
    my $this        = shift;
    my $RowNums     = [0..($this->length() - 1)];

    return($RowNums);
}

sub selection_inverse
{
    my $this        = shift;
    my $Sel         = $this->selection();
    my $All         = $this->all();

    @$All[@$Sel]    = undef;
    $All            = [grep {defined} @$All];

    return($All);
}

sub select_inverse
{
    my $this            = shift;

    return($this->{_Selection}  = $this->selection_inverse());
}

sub selection_validate
{
    my $this        = shift;

    if (ref($this->{_Selection}) eq 'ARRAY')
    {
        $this->{_Selection} = $this->selection_validate_internal($this->{_Selection});
    }
}

sub selection_validate_internal
{
    my $this        = shift;
    my ($Selection) = @_;

    my $Length      = $this->length();

    $Selection = [grep {(($_ >= 0) && ($_ < $Length))} @$Selection];

    return($Selection);
}

sub sel ## ($ColName, [$Vector])
{
    my $this                = shift;
    my ($ColName, $Vector)  = @_;

    ## Set if specified.
    if (defined($Vector))
    {
        $this->sel_set($ColName, $Vector);
        ## Nothing to return.
    }
    ## Get and return.
    else
    {
        my $Sel             = $this->sel_get($ColName);
        return($Sel);
    }
}

sub sel_get
{
    my $this                    = shift;
    my ($ColName, $Selection)   = @_;

    my $Col                     = $this->col($ColName);
    $Selection                  ||= $this->selection();
    my $Sel                     = [@$Col[@$Selection]];
    
    return($Sel);
}

sub sel_set ## ($ColName, [$Vector])
{
    my $this                        = shift;
    my ($ColName, $Vector)          = @_;

    my $Col                 = $this->col($ColName);
    my $Selection           = $this->selection();

    if (defined($Vector) && (ref($Vector) eq 'ARRAY'))
    {
        @$Col[@$Selection]      = @$Vector;
    }
    else
    {
        @$Col[@$Selection]      = undef;
    }
}

sub sel_clear ## ($ColName)
{
    my $this                = shift;
    my ($ColName)           = @_;
    
    $this->sel_set($ColName);
}

sub sel_len
{
    my $this                = shift;
    
    return(ref($this->{_Selection}) eq 'ARRAY' ?
           @{$this->{_Selection}}+0 : 
           $this->length());
}

sub sels ## ($ColNames)
{
    my $this        = shift;
    my ($ColNames)  = @_;
    $ColNames       ||= $this->fieldlist();
    my $Sels        = [map {$this->sel($_)} @$ColNames];

    return($Sels);
}

sub sels_hash ## ($ColNames)
{
    my $this        = shift;
    my ($ColNames)  = @_;
    $ColNames       ||= $this->fieldlist();
    my $Sels        = $this->sels($ColNames);
    my $SelsHash    = {}; @$SelsHash{@$ColNames} = @$Sels;

    return($SelsHash);
}

=pod 

=head1 SEARCHING / SELECTING RECORDS

    ## Modifying the table's custom selection (_Selection)

    $t->select_all()      ## Set _Selection = $t->all() or undef
    $t->select_none()     ## Set _Selection = []
    $t->select_inverse()  ## Invert the curr. sel. (and get it) 

    ## Specific searches: "the select() methods"

    $t->select($Field1=>$Sub1, ## Del nonmatching recs from sel.
               $Field2=>$Sub2, ## i.e. narrow sel. to match
               ...);

    $t->omit  ($Field1=>$Sub1, ## Del matching recs from sel.
               $Field2=>$Sub2, 
               ...);

    $t->add   ($Field1=>$Sub1, ## Add matching recs to sel.
               $Field2=>$Sub2,    
               ...);

    $t->but   ($Field1=>$Sub1, ## Add nonmatching recs to sel.
               $Field2=>$Sub2,    
               ...);

    ## Getting useful lists of record numbers...

    $t->all()                  ## Get "full" sel. (all record #s)
    $t->selection()            ## Get current selection
    $t->selection_inverse()    ## Get inverse copy of curr. sel.

    ## Example 1: Refine a selection by narrowing down...

    $t->select_all()
    $t->select(Field1 => sub {$_});
    $t->select(Field2 => sub {$_});
    $t->select(Field3 => sub {$_});

    ## Example 2: Manually refine and set the selection...

    $Sel = [grep {$t->col($Field1)->[$_]} @{$t->all      ()}];
    $Sel = [grep {$t->col($Field2)->[$_]} @$Sel];
    $Sel = [grep {$t->col($Field3)->[$_]} @$Sel];
    $t->selection($Sel);    ## Set the selection when done.

    ## Example 3: Complex manual search using calculated value
    
    my $A = $t->col('A');
    my $B = $t->col('B');
    my $S = [grep 
             {my $X = $A->[$_] + $B->[$_]; ($X > 100 && $X < 200);} 
             @{$t->all()}]; ## Or could start with $t->selection().
    $t->selection($S);      ## Set the selection when done.

    ## Example 4: Refine a selection by building up...

    $t->select_none()
    $t->add(Field1 => sub {$_});
    $t->add(Field2 => sub {$_});
    $t->add(Field3 => sub {$_});

    ## Example 5: Combining the select() methods to build a query...

    $t->select_all()
    $t->select(Status  => sub {/prime/i   });
    $t->omit  (DueDate => sub {$_ > $Today});
    $t->add   (Force   => sub {$_         });

select() and its friends omit(), add(), and but(), known collectively
as "the select() methods," all work similarly: they take a series of
one or more pairs indicating matches to be done, where each match is
specified as (FieldName => Subroutine).

In addition to the field names already present in the table, the
FieldName in any Spec may also be one of these two special
pseudo-fields:

=over 4

=item _RecNum

the record number of the record being compared

=item _SelNum

the numerical position of the record being compared within the
previous selection (only usable with select() and omit() since add()
and but() by definition operate on non-selected records).

=back

For example:

    ## Match 2nd 100 rec numbers
    $t->select(_RecNum => sub {$_ >= 100 && $_ <= 199});

    ## Match 2nd 100 currently selected/sorted items
    $t->select(_SelNum => sub {$_ >= 100 && $_ <= 199});

Be careful when using _SelNum in a search. In the above _SelNum search
example, since the selection itself will be modified by select(), the
items that were formerly selection items 100 - 199 will now be _SelNum
0 - 99 in the new selection.  

The Subroutine is an anonymous grep-style predicate that operates on
$_ and should return true/false to indicate a match with an element of
the field FieldName.

The order of multiple matches in a single method call is significant
only in that the searches can be faster if the field that will match
the fewest records is listed first.

A given FieldName may be listed in the specs more than once if it has
multiple search criteria that you prefer to execute as multiple
subroutines (though it would be more efficient on very large tables to
combine their logic into one subroutine joined with "&&").

Each field match will be applied (with an implied AND joining them) to
determine whether the record itself matches.  Then, based on whether
the record itself matches, it will either be added or deleted from the
selection based on which method is being called:

    method...  operates on...     action....
    ------------------------------------------------------------------
    select()   selected records   Keep only recs that DO     match
    omit()     selected records   Keep only recs that DO NOT match
    add()      non-selected recs  Add       recs that DO     match
    but()      non-selected recs  Add       recs that DO NOT match

Here's how to think about what's going on:

    methods... think...
    ------------------------------------------------------------------
    select()   "SELECT things matching this"...
    omit()     "... then OMIT those matching this."

    select()   "SELECT things matching this"...
    add()      "... and ADD any others matching this."

    select()   "SELECT things matching this"...
    but()      "... and add any others BUT those matching this."

select() and omit() both NARROW the selection.

add() and but() both INCREASE the selection.

IMPORTANT: You DO NOT need to use these select() routines to work with
selections.  It may be much easier for you to clarify your logic, or
more efficient to express your search, using a single grep or series
of grep operations as in Examples 2 or 3 above.

Building the selection manually is required if you want to filter
based on any COMPLEX RELATIONSHIPS BETWEEN FIELDS.  For example, if
you want to add two fields and match or reject the record based on the
sum of the fields.

In Example 3 above, we add the values in fields "A" and "B" and then
match the record only if the SUM is between 100 and 199.  By grepping
to produce a subset of @{$t->all()}, you end up with a Selection -- a
list of record numbers you want "selected".  Then you call
$t->selection() to put the selection you built into the object.

If you had instead wanted to narrow an existing selection in the above
example, you would start with $t->selection() (which defaults to
$t->all()) instead of starting with $t->all().

Each of the select() methods returns $this->selection() as a
convenience.

=head2 The effects of modifying a sorted selection

Generally, you should sort AFTER finding, and you should not generally
rely on sort order after doing a find.  But in case you want to know,
the following chart explains what happens to the sort order after the
various select() commands are called (at least in the current
implementation, which may change without notice):

    method... effect on an existing sort order...
    ------------------------------------------------------------------
    select()  relative sort order is preserved (stay sorted)
    omit()    all selected recs restored to "natural" order (unsorted)
    add()     orig. recs preserved; new recs appended: "natural" order
    but()     orig. recs preserved; new recs appended: "natural" order

In other words, you could sort() first and then call select() to
narrow down the selection repeatedly without disrupting the sort
order.  However, any of the other methods will disrupt the sort order
and you would need to re-sort.  The preservation of order when using
select(), and other sort order effects, are likely but not guaranteed
to be preserved in future implementations.

=head2 Hints about Boolean logic

Consider the following example and the alternative below it.  You
might initially think these are equivalent, but they're not:

    ## Case 1:

    my $Sel = $t->add(Force  => sub {$_ == 1      });
    my $Sel = $t->add(Status => sub {$_ eq 'Prime'});

    ## Case 2:

    my $Sel = $t->add(Force  => sub {$_ == 1      },
                      Status => sub {$_ eq 'Prime'});

Case 1 extends the selection by adding all records where Force == 1,
and then extends it again by adding all additional records where
Status eq 'Prime'.

Case 2 adds only those records where: Force == 1 AND ALSO, IN THE SAME
RECORD, Status eq 'Prime'.

One final note about logic.  This is not SQL and these select() etc.
routines are not meant to replace the full power of a programming
language.  

If you want full Boolean expressions, use the power of Perl to form
your own arbitrarily complex query using grep as in Example 3 above.

Writing your own grep is also almost always faster than chaining the
builtin select() methods or using multiple Field / Sub specifications,
so keep that in mind when working with extremely large data sets.

With tables of only a few thousand records or so, you probably won't
notice the difference in efficiency.

=cut

{}; ## Get emacs to indent correctly.

sub select  {my $this = shift; return($this->select_internal(!'Add', !'Not', @_))}
sub omit    {my $this = shift; return($this->select_internal(!'Add',  'Not', @_))}
sub add     {my $this = shift; return($this->select_internal( 'Add', !'Not', @_))}
sub but     {my $this = shift; return($this->select_internal( 'Add',  'Not', @_))}

sub select_internal ## Implements all 4 "select() methods"
{
    my $this                    = shift;
    my ($Add, $Not, @Specs)     = @_;
    
    ## In "Add" mode, we only operate on not-yet-selected records.
    ## Otherwise, we operate on the current selection.

    ## Either way, start out with all of one or the other.

    my $Start       = ($Add ? $this->selection_inverse() : $this->selection());

    ## Then grep repeatedly for each Field/Sub spec we were given, in
    ## order.  For a record to match, ALL specs must match -- i.e. the
    ## record number must make it through the grep gauntlet once for
    ## each Field/Sub in the Specs.
    
    my $Pseudo      = {};   ## hold pseudo-columns _RecNum, _SelNum if requested.
    
    my $Matches     = $Start;
    
    my $i = 0;
    while ($i < (@Specs - 1))
    {
        my ($Field, $Sub) = @Specs[$i++, $i++];
        
        next unless ((length($Field)) && (ref($Sub) eq 'CODE'));

        ## Create pseudo-fields _RecNum / _SelNum if needed, but at
        ## most once per invocation.

        $Pseudo->{_RecNum} ||= $this->all()          if ($Field eq '_RecNum');
        $Pseudo->{_SelNum} ||= $this->selnum_mask()  if ($Field eq '_SelNum');
        
        ## Narrow down $Matches using this Field/Spec, then move on to next.
        $Matches = 
            [grep
             {
                 ## Locally bind $_ to value of field in this column / this record.
                 local $_ = $ {($this  ->{$Field} || ## 98% of time: good field name
                                $Pseudo->{$Field} || ## 1% of time: _RecNum or _SelNum
                                ($this ->warn("Bad field name: $Field"), 
                                 $this ->col_empty())
                                )}[$_];       ## look up value in record $_ of column
                 
                 ## Call the sub & let it yield the Boolean value.
                 &$Sub();
             }
             @$Matches];
    }
    
    ## IMPLEMENTATION NOTE: 
    
    ## The logic below to support "Not" looks complicated, and indeed
    ## it could be made cleaner if we were to process the "Not" logic
    ## during the previous step.  However, doing that would make the
    ## above nested loop(s) above much less efficient because we'd
    ## have to move the while() loop inside the grep -- repeating that
    ## loop up to several times for each record instead of just a few
    ## times total.  So the logic below will actually save execution
    ## time.  Besides, using array-slicing to achieve the selection
    ## masking is quite fast.

    ## "Add" means append the matching record numbers to the existing
    ## selection, if any.
    
    ## sub select()
    if    (!$Not && !$Add) ## ... i.e. remove unfound (i.e. keep (only) the ones we found)...
    {       
        $this->{_Selection} =        $Matches;    ## sort order is preserved...
    }
    
    ## sub add()           ## ... i.e. add the ones we found...
    elsif (!$Not &&  $Add) 
    {
        push @{$this->selection()},  @$Matches;   ## order preserved in first part only
    }
    
    ## ! "Add" means replace the existing selection with those that
    ## matched (effectively removing any non-matching ones).

    ## "Not" means select the opposite of the set of records we just
    ## matched.

    ## sub omit()       
    elsif ( $Not && !$Add)  ## ... i.e. remove the opposite of the ones we found...
    {
        my $Sel             = $this->col_empty();       ## Start with empty mask (all entries undef).
        @$Sel[@$Start]      = @$Start;                  ## Mask in those in the original selection.
        @$Sel[@$Matches]    = undef;                    ## Mask out those we found.
        my $NonMatches      = [grep {defined} @$Sel];   ## The remaining ones are the non-matches.

        $this->{_Selection} = $NonMatches;              ## The new selection IS the non-matches.

        ## selection order not preserved
    }

    ## sub but()
    elsif ( $Not &&  $Add)  ## ... i.e. add the opposite of the ones we found...
    {
        my $Sel                      = $this->all();            ## Start with a full selection mask.
        @$Sel[@{$this->selection()}] = undef;                   ## Mask out those in original selection.
        @$Sel[@$Matches            ] = undef;                   ## Mask out those we found.
        my $NonMatches               = [grep {defined} @$Sel];  ## The remaining ones are the non-matches.
        
        push @{$this->selection()}, @$NonMatches;               ## Add the non-matches to the selection.

        ## order preserved in first part only
    }

    return($this->selection());
}

sub selnum_mask         ## Create mask mapping RecNum -> selected item num or undef if not selected
{
    my $this            = shift;
    my $Mask            = $this->col_empty();
    my $Sel             = $this->selection();
    @$Mask[@$Sel]       = [0..$#$Sel];

    return($Mask);
}

=pod

=head1 SORTING

    ## Sort the current table's _Selection

    $t->sort()                       ## Use existing/default params
    $t->sort([qw(Last First Phone)]) ## Specify _SortOrder (fields)
    $t->sort(                        ## Named-parameter call:
             _SortOrder => [...],    ##  override sort-related params.
             _Selection => [...],    ##  (See param lists above).
             _SortSpecs => {...},
             _SRoutines => {...},
             _DefaultSortType=>'Integer',
             _DefaultSortDirection=>-1,
             );

The sort() method modifies the _Selection (creating one with all
records if it was missing, undef, or not supplied by caller) so that
the record numbers listed there are sorted according to the criteria
implied by _SortOrder, _SortSpecs, _SRoutines, etc.

For example, before sorting, a table's "natural" order might be:

    Rec# First  Last Age State
    0    Chris  Zack 43  CA
    1    Marco  Bart 22  NV
    2    Pearl  Muth 15  HI

... and its selection() method would yield: [0, 1, 2] -- which is a
list of all the records, in order.

After calling $t->sort([Last]), selection() would yield [1, 2, 0].  So
displaying the table in "selection" order would yield:

    Rec# First  Last Age State
    1    Marco  Bart 22  NV
    2    Pearl  Muth 15  HI
    0    Chris  Zack 43  CA

IMPORTANT: sorting does not alter any data in the table.  It merely
alters the _Selection parameter (which you can then get and set using
the selection() methods described above).  

If you want to permanently alter the table's data in memory so that
the new sorted order becomes the "natural" order, you can use the
cull() method to modify the original object, the snapshot() method to
make a new object, or use the write() method to write the data to disk
in selected/sorted order and then read() it back again.

=head2 Using the Named-parameter calling convention with sort()

You may specify any combination of the parameters listed above when
calling sort().  Any you specify will be used IN PLACE OF the
corresponding parameters already found in the object.

If you specify _Selection using the named-parameter calling, the
sort() method reserves the right to "own" the list you provide, and
use it as the object's new _Selection, possibly discarding the
previous _Selection, if any and modifying the one you provided.  So
don't make any assumptions about ownership of that list object after
calling sort().  Luckily, you will rarely need to provide _Selection
explicitly since generally you'll want to be sorting the selection()
already inherent in the object.

sort() returns the _Selection list owned by the object (the same list
that would be returned if you called the selection() method
immediately after calling sort()).

See the next sections for complete descriptions of _SortOrder and
other sorting parameters.

=cut

{}; ## Get emacs to indent correctly.

sub sort
{
    my $this        = shift;
    my $Params      = (@_ == 1 ? {_SortOrder => $_[0]} : {@_});

    my($SortOrder, $Selection, $SortSpecs, $SRoutines, $DefaultSortType, $DefaultSortDirection)
        = map {$this->getparam($Params, $_)} 
    qw(_SortOrder  _Selection  _SortSpecs  _SRoutines  _DefaultSortType  _DefaultSortDirection);
    
    ## Validate / rectify all parameters...

    ## Default sort order is Record Number

    $SortOrder          = [qw(_RecNum)] unless ((ref($SortOrder) eq 'ARRAY') && @$SortOrder);

    ## Note if we're going to sort on _RecNum ( requires extra work).

    my $NeedRecNum      = grep {$_ eq '_RecNum'} @$SortOrder;

    ## Default list of record numbers is all of them.

    $Selection          = $this->selection() unless (ref($Selection) eq 'ARRAY');
    $Selection          = $this->selection_validate_internal($Selection);

    ## Our private copy of SortSpecs includes a spec for _RecNum

    $SortSpecs          = {} unless (ref($SortSpecs) eq 'HASH');
    $SortSpecs          = {%$SortSpecs};
    $SortSpecs          ->{_RecNum} ||= {SortType => '_RecNum', SortDirection => 1} if $NeedRecNum;

    ## Our private copy of SRoutines also has the builtin entries
    ## added in (including one for _RecNum)

    $SRoutines          = {} unless (ref($SRoutines) eq 'HASH');
    $SRoutines          = {%$SRoutines, %{$this->sortroutines_builtin()}};

    ## Ensure that DefaultSortType has a reasonable value for which we
    ## have a sort routine.
    
    $DefaultSortType    = 'String' unless (length($DefaultSortType) && 
                                           exists($SRoutines->{$DefaultSortType}));
    
    ## Ensure that DefaultSortDirection has a legal value (1 or -1;
    ## undef/0 will be treated as -1 (descending))
    
    $DefaultSortDirection = (max(min(int($DefaultSortDirection), 1), -1) || -1);
    
    ## Make some optimized lists of things to speed sorting.

    ## Get a hash of all data columns in $this plus a temporary one
    ## for _RecNum if needed.
    my $Cols            = {%{$this->cols_hash($this->fieldlist_all())}, 
                           ($NeedRecNum ? (_RecNum => $this->all()) : ())};
    
    ## Get a list mapping field numbers in $SortOrder to data columns
    ## Get a list mapping field numbers in $SortOrder to sort directions
    ## Get a list mapping field numbers in $SortOrder to sort types
    ## Get a list mapping field numbers in $SortOrder to sort routines
    ## Get a list of the field numbers in $SortOrder

    my $SortCols  = [map {        $Cols->{$_}                     || $this->col($_)       } @$SortOrder];
    my $SortDirs  = [map {$ {$SortSpecs->{$_}||{}}{SortDirection} || $DefaultSortDirection} @$SortOrder];
    my $SortTypes = [map {$ {$SortSpecs->{$_}||{}}{SortType     } || $DefaultSortType     } @$SortOrder];
    my $SortSubs  = [map {   $SRoutines->{$_} || $SRoutines->{'String'} || sub {0}        } @$SortTypes];
    my $FieldNums = [0 ..                                                                  $#$SortOrder];

    ## Construct a sort subroutine that sorts record numbers by
    ## examining values in given fields in the table, in the order
    ## specified in $SortOrder.

    ## If a given field's sort routine produces a zero $CmpVal, it
    ## means that the values are considered equal, and so to
    ## disambiguate, we keep trying the next fields in the sort order,
    ## until we've found one that compares non-zero or exhausted all
    ## the fields.  If we get through all the specified sort fields
    ## and still get zeroes, the values must be equal in all the
    ## fields, and so the records are considered equal, so return 0.

    my $ProgCount;
    my $ShowedProgress;
    $Selection = 
        [sort   
         {       
             my $CmpVal;
             foreach (@$FieldNums)
             {
                 ## $a and $b are record numbers to be compared.
                 ## $_ is the number of a field in the above lists.
                 
                 $CmpVal = (&{ $SortSubs->[$_]      }   ## Call the sort routine for  field $_ with...
                            (\ $SortCols->[$_]->[$a],   ##   1st arg: ref to value in field $_, record $a
                             \ $SortCols->[$_]->[$b])   ##   2nd arg: ref to value in field $_, record $b.
                            *  $SortDirs->[$_]       ); ## Then invert cmp value if descending (-1)
                 
                 # print "($_, $SortCols->[$_]->[$a], $SortCols->[$_]->[$b]) ==> $CmpVal\n";
                 
                 last if $CmpVal;                       ## Keep going if $CmpVal == 0 (same)
             }
             
             ## Maybe show timed progress (only after 2 seconds have elapsed)
             my $Did = $this->progress_timed("Sorting", $ProgCount, undef, undef, 1) 
                 if ((($ProgCount++) % 200) == 0);
             $ShowedProgress ||= $Did;

             $CmpVal;   
         }
         @$Selection];

    ## If no progress shown yet (sort took less than 2 seconds or 200
    ## operations), show a message now.

    $this->progress("Sorted.") unless $ShowedProgress;
    
    ## Replace any existing selection with the new, sorted, one.
    $this->{_Selection} = $Selection;

  done:
    return($Selection);
}

=pod

=head1 SORT ORDER

    ## Getting / Setting table's default _SortOrder

    $t->sortorder()         ## Get sortorder (default is [])
    
    my $Order = [qw(Last First State Zip)];
    $t->sortorder($Order)   ## Set sortorder (use [] for none)

    $t->sortorder_default() ## Get the object's default sort order ([])

The sort order is an optional list of field names on which to sort and
sub-sort the data when sorting is requested.  The field names must be
the names of actual columns in the table.  The names in the sort order
do not necessarily need to coincide with the custom fieldlist if any.

There is one special value that can be included: _RecNum.  This sorts
on the imaginary "record number" field.  So for example, you could
specify a sort order this way:

    [qw(Last First _RecNum)]

(There is no point in putting _RecNum anywhere except at the end of
the sort order because no two records will ever have the same record
number so there will be no further need to disambiguate by referring
to additional fields.)

Sorting by _RecNum adds a bit of computational overhead because sort()
first builds a record number vector for use in sorting, so for very
large tables, don't do it unless you really need it.

A sort order can be specified each time the object is sorted (see the
sort() method for details).

Or, the object's sort order can be set once, and then sort() will use
that sort order when no other sort order is specified.

If sorting is done when there is no sort order present in the object
or specifed for the sort() method, the selection is sorted by record
number (i.e. it is "unsorted" or returned to its "natural" order).

In order words, a sortorder that is undef or [] is considered the same
as: [qw(_RecNum)].  This is sometimes called "unsorting".

In order to decide how values in each field should be compared, sort()
is informed by SortSpecs (specifying SortType and SortDirection for
each field) and by SortRoutines, each of which may similarly either be
pre-set for the object or specified when calling sort() -- see below
for further details.

=cut

{}; ## Get emacs to indent correctly.

sub sortorder
{
    my $this        = shift;
    my ($SortOrder) = @_;

    my $Valid = ((ref($SortOrder) eq 'ARRAY') && (@$SortOrder > 0));

    ## Set if specified.
    if (defined($SortOrder) && $Valid)
    {
        $this->{_SortOrder} = $SortOrder;
    }
    elsif (defined($SortOrder))
    {
        $this->{_SortOrder} = undef;    ## Store undef instead of []
    }
    
    ## Get and return.
    $SortOrder  = $this->{_SortOrder} || $this->sortorder_default();
    
    return($SortOrder);
}

sub sortorder_default
{
    my $this = shift;

    my $SortOrder   = [];

    return($SortOrder);
}

sub sortorder_check
{
    my $this        = shift;
    my $FieldsHash  = $this->fieldlist_hash();

    ## Remove any bogus field names from the sort order, if any.

    $this->{_SortOrder} = [grep {exists($FieldsHash->{$_})} 
                           @{$this->{_SortOrder}}] if defined $this->{_SortOrder};
}

=pod 

=head1 SORT SPECIFICATIONS

    ## Getting / Setting table's default _SortSpecs

    $t->sortspecs()         ## Get sortspecs (default is {} -- none)

    my $Specs = {Last => {SortType       => 'String' , 
                          SortDirection  => -1        },
                 Zip  => {SortType       => 'Integer' }};

    $t->sortspecs($Specs)   ## Set sortspecs

    $t->sortspecs_default() ## Get the object's default sort specs ({})

The sortspecs are an optional hash mapping field names to "sort
specifications".

Each field's sort specification may specify zero or more of these
values:

=over 4

=item SortType

the sort type to use (For example: String, Integer)

=item SortDirection

the sort direction (1: ascending, -1: descending)

=back

Sortspecs can be specified when calling the sort() routine, or, a set
of specs can be placed beforehand into the object itself and those
will be used by sort() if no other specs are given.

For any field listed in the sort order at the time of sorting, but
lacking a sort spec or any component of the sort spec, the object's
default sort type (see sorttype_default()) and default sort direction
(see sortdirection_default()) will be used.

In addition to getting/setting sort specs as a whole, they may be
gotten/set on a per-field basis, too:

    sortspec($Field)       ## Get sortspec for $Field or default spec

    my $Spec   = {SortType => 'Integer', SortDirection => -1};
    sortspec('Zip', $Spec) ## Set sortspec

    sortspec_default()     ## Get a sortspec with all defaults filled in

For any $Field not found in the object's sortspecs, sortspec($Field)
returns the same thing returned by sortspec_default(), which is a
sortspec filled in with the default sort type and sort direction (see
below).

For a list of available built-in SortTypes, and instructions for how
to define your own, see SORT ROUTINES, below.

=cut

{}; ## Get emacs to indent correctly.

sub sortspecs
{
    my $this        = shift;
    my ($SortSpecs) = @_;

    ## Set if specified.
    $this->{_SortSpecs} = $SortSpecs if $SortSpecs; 
    
    ## Get and return
    $SortSpecs      = $this->{_SortSpecs} || $this->sortspecs_default();

    return($SortSpecs);
}

sub sortspecs_default
{
    my $this = shift;

    my $SortSpecs   = {};

    return($SortSpecs);
}

sub sortspec
{
    my $this        = shift;
    my ($FieldName, $SortSpec) = @_;

    ## Set if specified.
    $this->{_SortSpecs}->{$FieldName} = $SortSpec if $SortSpec; 
    
    ## Get and return.
    my $SortSpec    = ($this->{_SortSpecs}->{$FieldName} || 
                       $this->sortspec_default($FieldName));
    
    ## Provide defaults for needed fields of sort spec.
    $SortSpec->{SortType}       ||= $this->sorttype_default     ();
    $SortSpec->{SortDirection}    = $this->sortdirection_default() 
        unless defined($SortSpec->{SortDirection});
    
    return($SortSpec);
}

sub sortspec_default
{
    my $this = shift;
    my ($FieldName) = @_;

    ## Default sortspec for a field

    my $SortType        = $this->sorttype_default     ();
    my $SortDir         = $this->sortdirection_default();
    
    my $Spec            = {SortType => $SortType, SortDirection => $SortDir};

    return($Spec);
}


=pod 

=head1 DEFAULT SORT DIRECTION

    ## Getting / Setting table's default _DefaultSortDirection

    $t->sortdirection_default()     ## Get default sort direction

    $t->sortdirection_default(-1)   ## Set default sort direction

Each element in a sort specification can optionally specify a sort
direction.  

1 = ascending, -1 = descending

For any sort specs that don't specify a direction, the object's
default sort direction will be used.  Use these routines to get/set
the default sort direction.

=cut

{}; ## Get emacs to indent correctly.

sub sortdirection_default
{
    my $this = shift;
    my ($DefaultSortDir) = @_;
    
    if (defined($DefaultSortDir))
    {
        ## Set if specified.  Force to 1 or -1.  Treat 0 as -1 (descending).
        $this->{_DefaultSortDirection} = 
            (max(min(int($DefaultSortDir), 1), -1) || -1);
    }
    
    ## Get and return.  If not present, then 1 (ascending) is the default.
    my $SortDir = (defined($this->{_DefaultSortDirection}) ? 
                   $this->{_DefaultSortDirection} : 
                   1);
    return($SortDir);
}

=pod 

=head1 DEFAULT SORT TYPE

    ## Getting / Setting table's default _DefaultSortType

    $t->sorttype_default()          ## Get default sort type

    $t->sorttype_default('Integer') ## Set default sort type

Each element in a sort specification can optionally specify a sort
type.  The sort type is a string (like 'String' or 'Integer' or
'Date') that selects from one or more sort routines.  (See Sort
Routines, below).

There are several sort routines built into the CTable object, and you
can also add as many of your own routines (and hence Sort Types) as
you like or need.  This allows for very flexible sorting.

For any sort specs that don't specify a type, the object's default
sort type will be used.  Use these routines to get/set the default
sort type, which initially is 'String'.

=cut

{}; ## Get emacs to indent correctly.

sub sorttype_default
{
    my $this = shift;
    my ($DefaultSortType) = @_;
    
    if (defined($DefaultSortType))
    {
        ## Set if specified.  
        $this->{_DefaultSortType} = "$DefaultSortType";
    }

    ## Get and return.  If not present, then 'String' is the default.
    my $SortDir = (defined($this->{_DefaultSortType}) ? 
                   $this->{_DefaultSortType} : 
                   'String');
    return($SortDir);
}

=pod 

=head1 SORT ROUTINES: BUILTIN AND CUSTOM

    ## Getting / Setting table's custom sort routines (_SRoutines)

    $t->sortroutine($Type)       ## Get a sort routine for $Type

    $t->sortroutine($Type, $Sub) ## Set a sort routine for $Type

    $t->sortroutine($Type, 0   ) ## Remove sort routine for $Type
    $t->sortroutine_set($Type)
    
    $t->sortroutines()           ## Get hash of any sort routines
    
    $t->sortroutines_builtin()   ## Get hash of builtin routines

Each SortType in the sortspecs should have a corresponding sort
routine (any unrecognized type will be sorted using the 'String' sort
routine).

The sort() command looks up the appropriate sort routine for each
field it is asked to sort, based on the SortType for that field, as
given in the sortspecs, as described above.

Builtin sort types, recognized and implemented natively by this
module, are:

    String   ## Fastest case-sensitive compare (data is string)
    Text     ## Insensitive compare (lowercases, then compares)
    Number   ## Number works for floats or integers
    Integer  ## Faster than floats.  Uses "use integer"
    DateSecs ## Same as integer; assumes date in seconds
    Boolean  ## Treats item as a Perlish boolean (empty/undef = false)

The above sort types are always recognized.  Additional sort types may
be added by subclasses (and could shadow the builtin implementations
of the above types if desired) and/or may be added to instances (and
again could shadow the above implementations), and/or may be specified
when the sort() method is called, once again optionally shadowing any
deeper definitions.

=head1 CUSTOM SORT ROUTINE INTERFACE

A custom sort routine is called with two arguments, each of which is a
pointer to a scalar.  The sort routine should dereference each pointer
and compare the resulting scalars, returning -1 if the first scalar is
smaller than the second, 1 if it is larger, and 0 if they are
considered equal.

For example, here is the built-in comparison routine for 'String':

    sub {   $ {$_[0]}  cmp    $ {$_[1]} }

NOTE: Your custom sort routines should NOT compare $a and $b as with
Perl's builtin sort() command.

Examine the variable $BuiltinSortRoutines in this module's
implementation to see some additional examples of sort routines.

Internally, sort() calls the sortroutines() method to get a hash that
should consist of all builtin sort routines with the per-object sort
routines, if any, overlaid.  sortroutines() in turn calls the
sortroutines_builtin() method to get a copy of the hash of all builtin
sort routines for the object.  (So a subclass could easily add
additional SortTypes or reimplement them by just overriding
sortroutines_builtin() and adding its own additional routines to the
resulting hash.)

sortroutine() may be called to get or set a custom sort routine for a
given type in the given object.  

There is no way to directly manipulate the builtin sort routines for
the entire class.  To accomplish that, you should define and use a
subclass that extends sortroutines_builtin() to add its own routines.

For example: 

    BEGIN
    {   ## A subclass of Data::CTable with an INetAddr SortType.
        package IATable;    use vars qw(@ISA);    @ISA = qw(Data::CTable);

        sub sortroutines_builtin
        {
            my $this = shift;
            my $CustomRoutines = 
            {INetAddr => 
             sub {use integer; ip2int($ {$_[0]}) <=> ip2int($ {$_[1]})}};
            my $AllRoutines = 
            {%{$this->SUPER::sortroutines_builtin()} %$CustomRoutines};
            return($AllRoutines);
        };

        sub ip2int {.....}  $# Could memoize & inline for efficiency
    }

    my $Table = IATable::new(......);

The IATable class would then have all the same features of
Data::CTable but would then also support the INetAddr SortType.

=cut

{}; ## Get emacs to indent correctly.

BEGIN
{
    my $BuiltinSortRoutines = 
    {(
      String   => sub {               $ {$_[0]}  cmp    $ {$_[1]} },
      Text     => sub {            lc($ {$_[0]}) cmp lc($ {$_[1]})},
      Number   => sub {               $ {$_[0]}  <=>    $ {$_[1]} },
      Integer  => sub {use integer;   $ {$_[0]}  <=>    $ {$_[1]} },
      DateSecs => sub {use integer;   $ {$_[0]}  <=>    $ {$_[1]} },
      _RecNum  => sub {use integer;   $ {$_[0]}  <=>    $ {$_[1]} },
      Boolean  => sub {             !!$ {$_[0]}  <=>  !!$ {$_[1]} },
      )};
    
    sub sortroutines_builtin                ## Class or instance method
    {
        return({%$BuiltinSortRoutines});    ## Copy of above private hash.
    }
}

sub sortroutine
{
    my $this                = shift;
    my ($Type, $Routine)    = @_;

    if (defined($Routine))
    {
        ## Set if $Routine provided.
        $this->sortroutine_set($Type, $Routine);
    }

    ## Get and return.
    $Routine = $this->sortroutine_get($Type);

    return($Routine);
}

sub sortroutine_get
{
    my $this                = shift;
    my ($Type)              = @_;
    my $Routines            = $this->sortroutines();
    my $Routine             = $Routines->{$Type} || $Routines->{'String'};
    
    return($Routine);
}

sub sortroutine_set
{
    my $this                = shift;
    my ($Type, $Routine)    = @_;

    my $Valid = (ref($Routine) eq 'CODE');

    if ($Valid)
    {
        ## Add / replace if a routine was supplied.
        $ {$this->{_SRoutines} ||= {}}{$Type} = $Routine;
    }
    else
    {
        ## Otherwise delete.
        $Routine = delete $ {$this->{_SRoutines} ||= {}}{$Type};
    }
    
    return($Routine);
}

sub sortroutines
{
    my $this                = shift;
    
    my $Routines            = {%{$this->sortroutines_builtin()}, ## First builtin ones
                               %{$this->{_SRoutines} || {}}};    ## Shadow with object's own
    
    return($Routines);
}

=pod 

=head1 FREEZING SELECTION & FIELD LIST

    ## Freeze data layout: re-order columns; omit unused fields
    
            $t->cull(...params...)     ## Rebuild table in order
    my $s = $t->snapshot(...params...) ## Make copy as if rebuilt

The cull() method re-writes all data in the table to be in the order
indicated in _Selection (if present).  This will cause any records not
listed in _Selection to be omitted (unless selection is null in which
case all records are retained in original order).  

In addition, if there is a custom field list present, it removes any
fields NOT mentioned in _FieldList.

The snapshot() method is similar, except instead of modifying the
object itself, it makes a copy of the object that's equivalent to what
cull() would have created, and returns that new object, leaving the
original untouched.  (All data structures are deep-copied from the old
object to the new one, leaving the objects totally independent.)

cull() and snapshot() both take two optional named parameters:
_FieldList and/or _Selection to be used in place of the corresponding
parameters found in the object.

If only a single argument is supplied, it is assumed to be _Selection.

=cut

sub cull
{
    my $this        = shift;
    my $Params      = (@_ == 1 ? {_Selection => $_[0]} : {@_});
    
    my($Selection, $FieldList) = map {$this->getparam($Params, $_)} 
    qw(_Selection  _FieldList);
    
    $FieldList ||= $this->{_FieldList};
    $Selection ||= $this->{_Selection};

    ## First cull any fields/columns not mentioned in _FieldList, if any.
    if ($FieldList) 
    {
        my $FieldHash   = {}; @$FieldHash{@$FieldList} = undef;
        my $DeadFields  = [grep {!exists($FieldHash->{$_})} @{$this->fieldlist_all()}];
        delete @$this{@$DeadFields};

        ## Set the (possibly) new field list in the object.
        $this->fieldlist_set($FieldList);
    }

    ## Then cull / rearrange all the columns
    if ($Selection)
    {
        ## Temporarily set the selection() to be the one we may have been given...
        $this->selection($Selection);
        
        ## Get the de-facto field list if we don't already have it explicitly
        $FieldList ||= $this->fieldlist();
        
        ## Rearrange each column
        foreach my $FieldName (@$FieldList)
        {
            $this->{$FieldName} = $this->sel($FieldName);
        }

        ## Remove the _Selection since it is no longer valid.
        $this->selection_delete();
    }
}

sub snapshot
{
    my $this        = shift;
    my $Params      = (@_ == 1 ? {_Selection => $_[0]} : {@_});
    
    my($Selection, $FieldList) = map {$this->getparam($Params, $_)} 
    qw(_Selection  _FieldList);
    
    $FieldList ||= $this->{_FieldList};
    $Selection ||= $this->{_Selection};

    ## First make a shallow copy of $this
    my $copy = {%$this};

    ## Then delete any (references to) data columns owned by $this...
    delete @$copy{@{$this->fieldlist_all()}};

    ## Then deep-copy all other parameters from $this...
    $copy = dclone($copy);
    
    ## Then new/bless/initialize the copy into the same class as $this...
    $copy = ref($this)->new($copy);

    ## Temporarily override selection if necessary.
    my $OldSel      = $this->{_Selection};
    $this->selection($Selection);

    ## Then insert all the rearranged columns into $copy...
    @$copy{@$FieldList} = @{$this->sels($FieldList)};
    
    ## Restore old selection, if any.
    $this->selection_set($OldSel);

    ## Remove the selection in copy.
    delete $copy->{_Selection};

    ## Set copy's fieldlist to a copy of the one we used.
    $copy->{_FieldList} = [@$FieldList];

    return($copy);
}

=pod

=head1 LINE ENDINGS

    ## Get current value

    $t->lineending()          ## Get actual setting: string or symbol
    $t->lineending_symbol()   ## Get setting's symbolic value if possible
    $t->lineending_string()   ## Get setting's string value if possible

    ## Set value

    $t->lineending($Ending)   ## Will be converted internally to symbol

    ## Convert a value to symbol or string form

    $t->lineending_symbol($L) ## Convert string form to symbolic form
    $t->lineending_string($L) ## Convert symbol form to string form

    ## Get internal conversion hash tables

    $t->lineending_symbols()  ## Hash ref mapping known strings to symbols
    $t->lineending_strings()  ## Hash ref mapping known symbols to strings

Use these accessor functions to get/set the _LineEnding parameter.

You can set the parameter in either string or symbol form as you wish.
You can get it in its raw, as-stored, form, or, you can get it in
string form or symbol form as desired.

Finally, some utility conversion calls allows you to convert a string
you have on hand to a symbolic form.  For example:

    $L = "\x0D";
    print ("This file uses " . $t->lineending_symbol($L) . " endings.");

This would print:
    
    This file uses mac endings.

=cut

{}; ## Get emacs to indent correctly.

BEGIN
{
    ## Map any recognized _LineEnding value to its actual string
    my $LineEnding_Strings =    
    {(
      dos           => "\x0D\x0A",
      mac           => "\x0D",
      unix          => "\x0A",
      "\x0D\x0A"    => "\x0D\x0A",
      "\x0D"        => "\x0D",
      "\x0A"        => "\x0A",
      )};
    
    ## Map any recognized _LineEnding value to its logical form
    my $LineEnding_Symbols = 
    {(
      "\x0D\x0A"    => "dos",
      "\x0D"        => "mac",
      "\x0A"        => "unix",
      dos           => "dos",
      mac           => "mac",
      unix          => "unix",
      )};
    
    sub lineending_strings              ## Class or instance method
    {
        return({%$LineEnding_Strings}); ## Copy of above private hash.
    }

    sub lineending_symbols              ## Class or instance method
    {
        return({%$LineEnding_Symbols}); ## Copy of above private hash.
    }
}

sub lineending()
{
    my $this            = shift;
    my ($LineEnding)    = @_;
    
    ## Set if specified.  Try to convert to symbolic form if possible.
    $this->{_LineEnding} = $this->lineending_symbol($LineEnding) || $LineEnding if $LineEnding;

    ## Otherwise / either case, get whatever value we have and return it....
    my $LineEnding      = $this->{_LineEnding};

    return($LineEnding);
}

sub lineending_symbol
{
    my $this            = shift;
    my ($LineEnding)    = @_;

    $LineEnding         ||= $this->{_LineEnding};

    return($ {$this->lineending_symbols()}{$LineEnding} || $LineEnding);
}


sub lineending_string
{
    my $this            = shift;
    my ($LineEnding)    = @_;

    $LineEnding         ||= $this->{_LineEnding};

    return($ {$this->lineending_strings()}{$LineEnding} || $LineEnding);
}

=pod

=head1 AUTOMATIC CACHEING

By default, Data::CTable makes cached versions of files it reads so it
can read them much more quickly the next time.  Optionally, it can
also cache any file it writes for quicker re-reading later.

On Unix systems, cache files are always created with 0666
(world-write) permissions for easy cleanup.

When reading files, Data::CTable checks the _CacheOnRead parameter.
If that parameter is true, which it is by default, the module tries to
find an up-to-date cache file to read instead of the original.
Reading a cache file can be 10x faster than reading and parsing the
original text file.

In order to look for the cache file, it must first calculate the path
where the cache file should be located, based on the _FileName of the
file to be read.

The path of the cache file is calculated as follows:

If the _CacheSubDir parameter is a RELATIVE PATH, then it is appended
to the directory component of _FileName to arrive at the directory to
use to store the cache file.  If it is an ABSOLUTE PATH, then
_CacheSubDir is used by itself.  (The trailing path separator is
optional and an appropriate one will be added by Data::CTable if it is
missing.)

The file name of the cache file is calculated as follows:

If the _CacheExtension parameter is specified, it is appended to the
base file name component from the _FileName parameter.  If you want
the cached file name to be the same as the name of the original file,
you can set _CacheExtension to "", which is not recommended.

Then, the cache path and cache file name are joined to arrive at the
name of the cache file.  If both _CacheSubDir and _CacheExtension were
empty, then the cache file path will be the same as the _FileName, and
Data::CTable will refuse to either read or write a cache file, so
setting these fields both to empty is equivalent to setting
_CacheOnRead to false.

The cache file contains a highly-efficient representation of all the
following data that would otherwise have to be determined by reading
and parsing the entire text file:

    - All the data columns (field values)
    - _FieldList:  The list of fields, in order
    - _HeaderRow:  Whether a header row is / should be present
    - _LineEnding: The line ending setting
    - _FDelimiter: The field delimiter setting

If found prior to a read(), AND, the date of the cache file is LATER
than the date of the original file, the cache file is used instead.
(If the date is EARLIER, then the cache file is ignored because it can
be presumed that the data inside the text file is newer.)

If cacheing is ON, then after successfully reading the text file
(either because there was no cache file yet or the cache file was out
of date or corrupted or otherwise unusable), read() will then try to
create a cache file.  This, of course, takes some time, but the time
taken will be more than made up in the speedup of the next read()
operation on the same file.

If creating the cache file fails (for example, because file
permissions didn't allow the cache directory to be created or the
cache file to be written), read() generates a warning explaining why
cacheing failed, but the read() operation itself still succeeds.

No parameters in the object itself are set or modified to indicate the
success or failure of writing the cache file.  

Similarly, there is no way to tell whether a successful read()
operation read from the cache or from the original data file.  If you
want to be SURE the reading was from the data file, either turn off
_CacheOnRead, or call the read_file() method instead of read().

NOTE: because the name of the cache file to be used is calculated just
before the read() is actually done, the cache file can only be found
if the _CacheSubDir and _CacheExtension are the same as they were when
the cache was last created.  If you change these parameters after
having previously cached a file, the older caches could be "orphaned"
and just sit around wasting disk space.

=head2 Cacheing on write()

You may optionally set _CacheOnWrite (default = false) to true.  If
done, then a cache file will be saved for files written using the
write() command.  Read about write() below for more about why you
might want to do this.

=head1 AUTOMATIC DIRECTORY CREATION

When Data::CTable needs to write a file, (a cache file or a data
file), it automatically tries to create any directories or
subdirectories you specify in the _FileName or _CacheSubDir
parameters.  

If it fails while writing a data file, write() will fail (and you will
be warned).  If it fails to create a directory while writing a cache
file, a warning will be issued, but the overall read() or write()
operation will still return a result indicating success.

Any directories created will have the permissions 0777 (world-write)
for easy cleanup.

Generally, the only directory the module will have to create is a
subdirectory to hold cache files.

However, since other directories could be created, be sure to exercise
caution when allowing the module to create any directories for you on
any system where security might be an issue.

Also, if the 0666 permissions on the cache files themselves are too
liberal, you can either 1) turn off cacheing, or 2) call the
prep_cache_file() method to get the name of the cache file that would
have been written, if any, and then restrict its permissions:

    chmod (0600, $this->prep_cache_file());

=head1 READING DATA FILES

    ## Replacing data in table with data read from a file

    $t->read($Path)     ## Simple calling convention

    $t->read(           ## Named-parameter convention

         ## Params that override params in the object if supplied...

         _FileName      => $Path, ## Full or partial path of file to read

         _FieldList     => [...], ## Fields to read; others to be discarded

         _HeaderRow     => 0,     ## No header row (_FieldList required!)

         _LineEnding    => undef, ## Text line ending (undef means guess)
         _FDelimiter    => undef, ## Field delimiter (undef means guess)

         _ReturnMap     => 1,     ## Whether to decode internal returns
         _ReturnEncoding=>"\x0B", ## How to decode returns.
         _MacRomanMap   => undef, ## Whether/when to read Mac char set 

         _CacheOnRead   => 0,     ## Enable/disable cacheing behavior
         _CacheExtension=> ".x",  ## Extension to add to cache file name
         _CacheSubDir   => "",    ## (Sub-)dir, if any, for cache files

         ## Params specific to the read()/write() methods...

         _MaxRecords    => 200,   ## Limit on how many records to read
         )

    $t->read_file()     ## Internal: same as read(); ignores cacheing

read() opens a Merge, CSV, or Tab-delimited file and reads in all or
some fields, and all or some records, REPLACING ANY EXISTING DATA in
the CTable object.

Using the simple calling convention, just pass it a file name.  All
other parameters will come from the object (or will be defaulted if
absent).  To specify additional parameters or override any parameters
in the object while reading, use the named-parameter calling
convention.

See the full PARAMETER LIST, above, or read on for some extra details:

_ReturnMap controls whether return characters encoded as ASCII 11
should be mapped back to real newlines (C<"\n">) when read into memory.
If false, they are left as ASCII 11 characters. (default is "true")

_ReturnEncoding controls the character that returns are encoded as, if
different from ASCII 11.

_FieldList is an array (reference) listing the names of fields to
import, in order (and will become the object's _FieldList upon
successful completion of the read() operation).  If not provided and
not found in the object, or empty, then all fields found in the file
are imported and the object's field list will be set from those found
in the file, in the order found there.  If _HeaderRow is false, then
this parameter is required (either in the object or as a formal
parameter) and is assumed to give the correct names for the fields as
they actually occur in the file.  If _HeaderRow is true and _FieldList
is provided, then _FieldList specifies the (sub-)set of fields to be
read from the file and others will be ignored.

_HeaderRow, which defaults to true, if set to false, tells read() to
not expect a header row showing the field names in the file.  Instead,
it assumes that the _FieldList gives those (and _FieldList must
therefore be specified either as a parameter or an existing parameter
in the object).

_MaxRecords (optional) is an upper limit on the number of fields to
import.  If not specified, or zero, or undef, then there is no limit;
all records will be imported or memory will be exhausted.

read() returns a Boolean "success" code.  

If read() returns false, then it will also have set the _ErrorMsg
parameter in the object.  It may or may not have partially altered
data in the object if an error is encountered.

After a successful read: 

fieldlist() (the object's _FieldList parameter) tells which
fields were actually read, in what order.  It may omit any fields
requested in _FieldList that were not actually found in the file for
whatever reason.

length() tells how many fields were read.

The selection() is reset to no selection (all selected / unsorted)

The object's _FileName parameter contains the path to the file
that was read.  If the _FileName you specified did not have a path,
then _FileName will be prepended with a path component indicating
"current directory" (e.g. "./" on Unix).

_FDelimiter will contain the actual delimiter character that was
used to read the file (either tab or comma if the delimiter was
guessed, or whatever delimiter you specified).

_LineEnding will contain the actual line-ending setting used to
read the file.  This will be either "mac" ("\x0D"), "unix" ("\x0D"),
or "dos" ("\x0D\x0A") if the line endings were guessed by read().
Otherwise it will be whatever _LineEnding you specified.


=head1 FILE FORMAT NOTES

As mentioned, read() allows the following flexibilities in reading
text-based tabular data files:

You may specify the line endings (record delimiters), or it can
guess them (mac, unix, dos are supported).

You may specify the field delimiters, or it can guess them (tab
and comma are supported).

It can get field names from a header row, or, if there is no
header row, you can tell it the field names, in order.

You can tell it whether or not to decode embedded returns in
data fields, and if so, which character they were encoded as.

Beyond supporting the above flexible options, read() makes the
following non-flexible assumptions:

Fields must NOT contain unencoded returns -- that is: whatever
character sequence is specified for _LineEnding will NEVER occur
inside a field in the text file; in addition, the current platform's
definition of C<"\n"> will NEVER occur; these characters if present in
field data, MUST have been encoded to some safe character string
before the file was created.

Each field may OPTIONALLY be surrounded with double-quote marks.
However, if the field data itself contains either a double-quote
character (C<">) or the current file's field delimiter (such as tab or
comma), then the field MUST be surrounded with double-quotes.
(Currently, all data written by Data::CTable have all field values
surrounded by double-quotes, but a more selective policy may be used
in the future.)

If a field contains a double-quote character, then each double-quote
character in the field must be encoded as C<""> -- i.e. each C<"> in the
original data becomes C<""> in the text file.

Data files may not mix line-ending types or field delimiter types.
Once determined, the same endings and delimiters will be used to read
the entire file.

The fields recognized on each line will either be determined by
the header row or the _FieldList provided by the caller.  Any extra
fields on any given line will be ignored.  Any missing fields will be
treated as undef/empty.

If you are having trouble reading a delimited text file, check that
all data in the file obeys these assumptions.

=cut

sub read            ## Read, cacheing if possible
{
    my $this        = shift;
    return($this->read_file_or_cache(@_));
}

sub read_file       ## Read, ignoring cacheing
{
    my $this        = shift;
    my $Params      = (@_ == 1 ? {_FileName => $_[0]} : {@_});

    my($FileName, $FieldList, $MaxRecords, $LineEnding, $FDelimiter, $ReturnMap, $ReturnEncoding, $MacRomanMap, $HeaderRow) 
        = map {$this->getparam($Params, $_)} 
    qw(_FileName  _FieldList  _MaxRecords  _LineEnding  _FDelimiter  _ReturnMap  _ReturnEncoding  _MacRomanMap  _HeaderRow);

    my $Success;

    ## Default error message is none.
    $this->{_ErrorMsg} = "";

    ## Default for HeaderRow is true.
    $HeaderRow          = 1         unless defined($HeaderRow);

    ## Default for ReturnEncoding is "\x0B" (control-K; ASCII 11)
    $ReturnEncoding     = "\x0B"    unless length($ReturnEncoding);

    ## Default for ReturnMap is true.
    $ReturnMap          = 1         unless defined($ReturnMap);

    ## Default for MacRomanMap is undef ("Auto");
    $MacRomanMap        = undef     unless defined($MacRomanMap);

    ## Default for MaxRecords is 0 (meaning import all records)
    $MaxRecords         = 0         unless (int($MaxRecords) == $MaxRecords);

    ## Precompile a regex for the return encoding since we'll call it often (on each field!) later.
    my $RetRegex        = qr/$ReturnEncoding/;

    $this->progress("Reading $FileName...");

    ## Open the data file.
    my $File = IO::File->new("<$FileName") or 
        do {$this->{_ErrorMsg} = "Failed to open $FileName: $!"; goto done};
    
    ## Get its total file size (useful for estimating table size later on).
    my $FileSize = (stat($File))[7] or 
        $this->{_ErrorMsg} = "File $FileName contains no data.", goto done;
    
    ## Convert from optional "dos", "mac", "unix" symbolic values.
    $LineEnding = $this->lineending_string($LineEnding);

    ## Default for LineEnding is found by inspecting data in the file.
    $LineEnding ||= guess_endings($File) or 
        $this->{_ErrorMsg} = "Could not find any line endings in the file $FileName.", goto done;
    
    ## DoMacMapping is the actual setting for auto charset mapping
    my $DoMacMapping    = 
        ((!defined($MacRomanMap) && ($LineEnding eq "\x0D")) || ## Auto
         ($MacRomanMap));                                       ## On

    $this->progress("Will convert upper-ascii characters if any, from Mac Roman to ISO 8859-1.") if $DoMacMapping;

    ## FieldList is usable is it is a list and has at least one entry.
    my $FieldListValid = ((ref($FieldList) eq 'ARRAY') && @$FieldList);
    
    ## Set <$File> to use the line ending sequence we no known we are looking for.
    local $/ = $LineEnding;
    
    ## We use $_ explicitly, so must localize.
    local $_;
    
    my $IncomingFields;

    if ($HeaderRow)
    {
        ## Get the list of fields available in the file (first line of file).

        $_ = <$File> or
            $this->{_ErrorMsg} = "Could not find a first line with field names in $FileName.", goto done;

        ## Try to guess file delimiter from the header row if not yet specified.
        $FDelimiter ||= guess_delimiter($_) or 
            $this->{_ErrorMsg} = "Could not find comma or tab delimiters in $FileName.", goto done;
        
        ## Maybe convert entire line (all records) Mac to ISO before splitting.
        &MacRomanToISORoman8859_1(\ $_) if $DoMacMapping;

        chomp;
        
        s/^\"//; s/\"$//;     ## remove possible leading, trailing quotes surrounding header row (rare)

        ## Split header row into field names, removing optional "" around each at the same time.
        $IncomingFields = [split(/\"?$FDelimiter\"?/, $_)];

        ## Strip any leading and/or trailing control chars or spaces from field names in header.
        $IncomingFields = [map {s{(?:\A[\x00-\x20]+)|(?:[\x00-\x20]+\Z)}{}g; $_;} @$IncomingFields];

    }
    else
    {
        ## Otherwise, require that the caller specifies it in _FieldList

        $this->{_ErrorMsg} = "Must specify a _FieldList if _HeaderRow says no header row is present.", goto done 
            unless $FieldListValid;
        
        $IncomingFields = [@$FieldList];
    }

    ## Remove any leading underscores in the names of the incoming
    ## fields (not allowed because such field names are reserved for
    ## other object data).  Note: this could result in
    ## duplicate/overwritten field names that were otherwise
    ## apparently unique in the incoming data file.

    $IncomingFields = [map {(/^_*(.*)/)[0]} @$IncomingFields];
        
    ## Make a hash that can be used to map these fields' names to their numbers.
    my $IncomingFieldNameToNum = {}; @$IncomingFieldNameToNum{@$IncomingFields} = ($[ .. $#$IncomingFields);
        
    ## Make a list of the fields we'll be importing (by taking the
    ## list the caller requested, and paring it down to only those
    ## fields that are actually available in the table.)

    my $FieldsToGet = 
        [grep {exists($IncomingFieldNameToNum->{$_})}
         ($FieldListValid ? @$FieldList : @$IncomingFields)];

    ## Make a note of whether we're getting a subset of available
    ## fields because the caller requested such.  If we are, we'll add
    ## a _Subset => 1 marker to the data for use later in ensuring the
    ## cache is OK.
    
    my $GettingSubset = ($FieldListValid && ("@{[sort @$IncomingFields]}" ne 
                                             "@{[sort @$FieldList  ]}"));
    
    ## Make an array of the incoming indices of these fields.

    ## Allocate a list of empty arrays into which we can import the
    ## data.  Initially they'll each have 100 empty slots for data;
    ## after we have imported 100 records, we'll re-consider the size
    ## estimate.  When we're all done, we'll prune them back.

    my $FieldNums       = [@$IncomingFieldNameToNum{@$FieldsToGet}];
    my $FieldVectors    = []; foreach (@$FieldNums) {$#{$FieldVectors->[$_] = []} = 100};

    ## We want to be cool and support any embedded NULL (ascii zero)
    ## characters should they exist in the data, even though we are
    ## going to use NULL chars to encode embedded delimiters before we
    ## split....

    ## First we create a sufficiently obscure placeholder for any
    ## ascii zero characters in the input text (a rare occurrence
    ## anyway).

    my $ZeroMarker = "\001ASCII_ZERO\001";
    
    ## Now ready to go through the file line-by-line (record-by-record)

    my $WroteProg;
    my $RecordsRead = 0;
    while (<$File>)
    {
        ## Try to guess file delimiter from the header row if not yet specified.
        $FDelimiter ||= guess_delimiter($_) or 
            $this->{_ErrorMsg} = "Could not find comma or tab delimiters in $FileName.", goto done;
        
        ## Maybe convert entire line (all records) ISO to Mac before splitting.
        &MacRomanToISORoman8859_1(\ $_) if $DoMacMapping;
        
        ## Manipulate the single line of data fields into a splittable format.
        
        chomp;
        
        ## Replace any delimiters inside quotes with ASCII 0.
        ## Split fields on delimiters.
        ## Delete leading or trailing quote marks from each field.
        ## Restore delimiters ASCII 0 back to delimiters.
        
        ## Protect delimiters inside fields.
        s/\000/$ZeroMarker/go;                  ## Preserve genuine ASCII 0 chars.
        my $InQuote = 0;                        ## Initialize InQuote flag to zero.
        s/(\")|($FDelimiter)/                   ## Replace delimiters inside quotes with ASCII 0 ...
            ($1 ? do{$InQuote^=1; $1} :         ##  ... if quote char, toggle InQuote flag
             ($InQuote ? "\000" : $2))/eg;      ##  ... if delimiter, InQuote sez whether to replace or retain.

        ## Split record into fields, then clean each field.

        s/^\"//; s/\"$//;                       ## Kill leading, trailing quotes surrounding each record
        my @FieldVals = 
            map 
            {if (length($_))
             {
                 s/\"\"/\"/g;                   ## Restore Merge format's quoted double-quotes. ("" ==> ")
                 s/\000/$FDelimiter/g;          ## Restore delimiters inside fields
                 s/\Q$ZeroMarker\E/\000/go;     ## Restore preserved ASCII 0 chars.
                 s/$RetRegex/\n/g if $ReturnMap;## Restore return characters that were coded as ASCII 11 (^K)
             }
             $_;}                               ## Return field val after above mods.
        split(/\"?$FDelimiter\"?/, $_);         ## Split on delimiters, killing optional surrounding quotes at same time.
        
        ## Put the data into the vectors
        foreach (@$FieldNums)
        {
            $FieldVectors->[$_]->[$RecordsRead] = $FieldVals[$_] if (length($FieldVals[$_]));
        }
        $RecordsRead++;
        
        ## Stop if we've read all the records we wanted.
        last if ($MaxRecords && ($RecordsRead >= $MaxRecords));
        
        ## Optimization:

        ## After importing 100, 200, 300, 400, etc. records, we
        ## re-estimate the size of the table.  To help make the field
        ## insertion more efficient (by avoiding frequent
        ## array-resizing), we can set the sizes of the field vectors
        ## to hold at least our estimated number of records).
        ## Ideally, this estimation/resize step will happen at most 2
        ## or 3 times no matter how big the incoming data file is.

        my $EstTotalRecords;
        if ((($RecordsRead % 100) == 0) &&                  ## If we're on a record divisble by 100...
            (($RecordsRead + 100 > $#{$FieldVectors->[$FieldNums->[0]]})))  ## ... and we're getting close to max size...
        {
            ## Then estimate the size we'd like to resize it to.
            $EstTotalRecords = (100 + int($RecordsRead * ($FileSize / tell($File))));
            $EstTotalRecords = $MaxRecords if ($MaxRecords && ($MaxRecords < $EstTotalRecords));
            
            ## If this size is greater than the actual size...
            if ($EstTotalRecords > $#{$FieldVectors->[$FieldNums->[0]]})    
            {
                ## Then resize all the vectors.
                ## $this->progress("$RecordsRead: Resizing to $EstTotalRecords...\n");  ## Debugging
                foreach (@$FieldNums) {$#{$FieldVectors->[$_]} = $EstTotalRecords};
            }
        }

        ## Try doing timed (throttled to 1 per 2 secs) progress at
        ## most every 100th record.
        my $Did = ($EstTotalRecords ? 
                   $this->progress_timed("Reading", "$RecordsRead of $EstTotalRecords (est.)", tell($File), $FileSize, 1) :
                   $this->progress_timed("Reading", "$RecordsRead"                           , tell($File), $FileSize, 1))
            if (($RecordsRead % 100) == 0);
        $WroteProg ||= $Did;
    }

    ## If we wrote timed progress but didn't get to give the 100%
    ## message yet, print the 100% message now.

    if ($WroteProg)
    {
        $this->progress_timed("Reading", "$RecordsRead of $RecordsRead", $FileSize, $FileSize, 1) 
            unless (($RecordsRead % 100) == 0);
    }

    ## Print the regular Done message.
    $this->progress("Read    $FileName.");

    ## Set the field vectors' length to the exact length we really
    ## read.

    ## $this->progress("$RecordsRead: Truncating to @{[$RecordsRead - 1]}... \n");  ## Debugging
    foreach (@$FieldNums) {$#{$FieldVectors->[$_]} = ($RecordsRead - 1)};
    
    ## Delete any existing columns in the object.
    delete @$this{@{$this->fieldlist_all()}};

    ## Put the new columns into the object.
    @$this{@$FieldsToGet} = @$FieldVectors[@$FieldNums];

    ## Set fieldlist to fields we actually read, in order.
    $this->fieldlist($FieldsToGet);

    ## Remember the line ending char or chars that were successfully
    ## used to read the file.  The same ending will be used by default
    ## to write any file based on this object.
    $this->{_LineEnding} = $this->lineending_symbol($LineEnding);
    
    ## Remember the field delimiter that was successfully used to read
    ## the file.  The same delimiter will be used by default to write
    ## any file based on this object.
    $this->{_FDelimiter} = $FDelimiter;

    ## Remember the header row setting.
    $this->{_HeaderRow} = $HeaderRow;

    ## Remember the filename used for reading.
    $this->{_FileName} = $FileName;

    ## Remember whether we read (and maybe will cache) a subset of available fields.
    $this->{_Subset} = $GettingSubset || 0;

    ## Clean out _Selection and verify _SortOrder to ensure compatibility
    ## with current _FieldList.
    $this->read_postcheck();

    ## Other informational data and options, like sort specs, sort
    ## routines and so on, need not be changed or replaced when data
    ## changes.

    $Success = 1;

  done:
    
    $this->warn("FAILURE: $this->{_ErrorMsg}") unless $Success;

    close $File if $File;
    return($Success);
}

sub read_postcheck  ## Called to clean up after a successful read
{
    my $this        = shift;

    ## Run select_all to empty out the _Selection.
    $this->select_all();

    ## Remove any bogus field names from the sort order, if any.
    $this->sortorder_check();
}

sub read_file_or_cache  ## Read, cacheing if possible
{
    my $this        = shift;
    my $Params      = (@_ == 1 ? {_FileName => $_[0]} : {@_});
    
    my($FileName, $FieldList, $CacheOnRead, $CacheExtension, $CacheSubDir) = map {$this->getparam($Params, $_)} 
    qw(_FileName  _FieldList  _CacheOnRead  _CacheExtension  _CacheSubDir);

    my $Success;

    ## If cacheing is turned off, just bail prematurely and treat this
    ## as a call to read_file().

    return($this->read_file(%$Params)) unless $CacheOnRead;

    ## Otherwise... check if cacheing is possible.

    ## Calculate the cache file name.  If it comes back empty, it
    ## means the cache directory probably could not be created, or the
    ## cache file itself either does not exist or could not be
    ## preflighted (either read or touched/deleted).

    my $CacheFileName   = $this->prep_cache_file($FileName, $CacheExtension, $CacheSubDir);

    ## If the cache file preflight failed, treat this as a regular
    ## read_file() without cacheing.
    
    return($this->read_file(%$Params)) unless length($CacheFileName);
    
    ## At this point we believe we'll either be able to read or write
    ## the cache file as needed.

    ## Try to read the cache if both files exist and the mod date is
    ## later.

    my $Data;
    if ((-e                                   $FileName      ) &&
        (-e     $CacheFileName                               ) &&
        (((stat($CacheFileName))[9]) > ((stat($FileName))[9]))    )
    {
        $this->progress("Thawing $CacheFileName...");
        eval 
        {
            $Data = &retrieve($CacheFileName);
        };
        $this-warn("Cache restore from $CacheFileName failed: $!"), unlink($CacheFileName)
            unless defined ($Data);
    }
    
    if (ref($Data) eq 'HASH')
    {
        ## Retrieval succeeded.  
        
        ## Verify that the data in the cache is usable.

        ## First, check newline-encoding compatibility.
        (
         $this->warn("Abandoning cache due to incompatible newline encoding"), 
         unlink $CacheFileName, goto cache_failed) unless $Data->{_Newline} eq "\n";
        
        ## Simulate an actual read_file() using data
        ## from the cache instead of the original file.

        if ((ref($FieldList) eq 'ARRAY') && @$FieldList)
        {
            ## If $FieldList requests fields not found in the cache,
            ## we abandon (delete and maybe rewrite) the cache: maybe
            ## we previously cached a different subset of fields from
            ## a previous request and so the cache is no longer
            ## adequate.

            my $MissingFields = [grep {!exists($Data->{$_})} @$FieldList];
            (
             ## $this->warn("Abandoning cache due to change in requested field list"), 
             unlink $CacheFileName, goto cache_failed) if @$MissingFields;
            
            ## If there was a _FieldList supplied in $Params or $this,
            ## we might need to omit any fields read from cache but
            ## not mentioned (just as read_file() would have done).

            my $FieldHash   = {}; @$FieldHash{@$FieldList} = undef;
            my $OmitFields  = [grep {!exists($FieldHash->{$_})} grep {!/^_/} keys %$Data];
            
            delete @$Data{@$OmitFields};
            
            ## Finally, also pare down _FieldList to only mention
            ## those fields that were desired....
            
            my $AvailFields     = $Data->{_FieldList};
            $Data->{_FieldList} = [grep {exists($Data->{$_})} @$FieldList];

            ## We might have ended up reading a subset of the
            ## available fields in the cache.  (This is logically
            ## equivalent to reading a subset of the available fields
            ## in the real file.)  If so, then change the value of
            ## $Data->{_Subset} to indicate that.

            my $GettingSubset   = (@$OmitFields && 1 || 0);         
            $Data->{_Subset}    ||= $GettingSubset;
        }
        elsif ($Data->{_Subset})
        {

            ## Conversely, if no field list was specified (hence all
            ## fields are desired), but a subset of fields has
            ## previously been cached, we have to delete / abandon the
            ## cache and re-read the file so we are getting all
            ## fields.

            (
             ## $this->warn("Abandoning partial cache due to request of full field list"), 
             unlink $CacheFileName, goto cache_failed);
        }

        ## Copy all elements from $Data, including possibly overridden
        ## _FieldList element if any, but excepting the cache-only
        ## _Newline element, into $this.

        delete $Data->{_Newline};
        @$this{keys %$Data} = values %$Data;

        ## If more records were written to the cache than are now
        ## desired, warn and truncate.  This is potentially not good
        ## because in the reverse case (reading/saving the first time
        ## with a limit then re-reading with no limit) would not be
        ## caught.

        if ($this->{_MaxRecords} && $this->length() > $this->{_MaxRecords})
        {
            $this->warn("Truncating length of cached table (@{[$this->length()]}) to requested length (@{[$this->{_MaxRecords}]})");
            $this->length($this->{_MaxRecords});
        }
        
        ## Set the file name to the name of the original (not the
        ## cache) file, just as read() would have done.

        $this->{_FileName} = $FileName;

        ## Run the "read post-check" -- the same things we do in
        ## read_file() after completing the read process: Clean out
        ## _Selection and verify _SortOrder to ensure compatibility
        ## with current _FieldList.

        $this->read_postcheck();

        $this->progress("Thawed  $FileName.");

        $Success = 1;
        goto done;         ## Successful completion: we read from the cache.
    }

    ## Could not retrieve for whatever reason (maybe cache did not
    ## exist yet or was out of date or had to be abandoned).  So just
    ## read normally and possibly write the cache.

  cache_failed:
    {
        $Success = $this->read_file(%$Params) or goto done;

        ## Now, having read successfully, we try to write the cache
        ## for next time.  Writing the cache is optional; failing to
        ## write it is not a failure of the method.

        {   ## Code in this block may fail and that's OK.

            ## First, pre-flight.
            $this->warn("Cache file $CacheFileName cannot be created/overwritten: $!"), 
            goto done                                           ## Successful completion.
                unless $this->try_file_write($CacheFileName);

            ## The data to be stored is: 

            ##   1) All data columns read by read_file()
            ##   2) Any parameters set by read_file()
            ##   3) _Subset param indicating partial fieldlist was read from file.
            ##   4) _Newline setting so we know if it's compatabile when read back.

            ## No other parameters should be cached because we want a
            ## read from the cache to produce exactly the same result
            ## as a read from the file itself would have produced.

            ## After a read, fieldlist() will contain the fields
            ## actually read, so cols_hash() WILL yield all the
            ## columns.

            my $Data = {(
                         ## Refs to each column read by read_file()
                         %{                 $this->cols_hash() },   

                         ## Other parameters set by read_file()
                         _FieldList     =>  $this->{_FieldList },   
                         _LineEnding    =>  $this->{_LineEnding},
                         _FDelimiter    =>  $this->{_FDelimiter},
                         _HeaderRow     =>  $this->{_HeaderRow },
                         _Subset        =>  $this->{_Subset    },
                         _Newline       =>  "\n",
                         )};
            
            $this->warn("Failed to cache $CacheFileName"), 
            unlink($CacheFileName), 
            goto done                                    ## Successful completion.
                unless $this->write_cache($Data, $CacheFileName);
            chmod 0666, $CacheFileName;                  ## Liberal perms if possible.
        }

        goto done;    ## Successful completion: we read from the file & maybe saved cache.
    }
    
  done:
    return ($Success);
}

=pod

=head1 WRITING DATA FILES

    ## Writing some or all data from table into a data file

    $t->write($Path)              ## Simple calling convention

    $t->write(                    ## Named-parameter convention

         ## Params that override params in the object if supplied...

         _FileName      => $Path, ## "Base path"; see _WriteExtension

         _WriteExtension=> ".out",## Insert/append extension to _FileName

         _FieldList     => [...], ## Fields to write; others ignored
         _Selection     => [...], ## Record (#s) to write; others ignored

         _HeaderRow     => 0,     ## Include header row in file

         _LineEnding    => undef, ## Record delimiter (default is "\n")
         _FDelimiter    => undef, ## Field delimiter (default is comma)

         _ReturnMap     => 1,     ## Whether to encode internal returns
         _ReturnEncoding=>"\x0B", ## How to encode returns
         _MacRomanMap   => undef, ## Whether/when to write Mac char set 


         _CacheOnWrite  => 1,     ## Enable saving cache after write()
         _CacheExtension=> ".x",  ## Extension to add to cache file name
         _CacheSubDir   => "",    ## (Sub-)dir, if any, for cache files

         ## Params specific to the read()/write() methods...

         _MaxRecords    => 200,   ## Limit on how many records to write
         )

    $t->write_file()    ## Internal: same as write(); ignores cacheing

write() writes a Merge, CSV, or Tab-delimited file.

It uses parameters as described above.  Any parameters not supplied
will be gotten from the object.

Using the simple calling convention, just pass it a path which will
override the _FileName parameter in the object, if any.

All other parameters will come from the object (or will be defaulted
if absent).  

If no _FileName or path is specified, or it is the special string "-"
(dash), then the file handle \ * STDOUT will be used by default (and
you could redirect it to a file).  You can supply any open file handle
or IO::File object of your own for the _FileName parameter.

If write() is writing to a file handle by default or because you
specified one, then no write-cacheing will occur.

To specify additional parameters or override any parameters in the
object while reading, use the named-parameter calling convention.

If the object's data was previously filled in using new() or read(),
then the file format parameters from the previous read() method will
still be in the object, so the format of the written file will
correspond as much as possible to the file that was read().

write() returns the path name of the file actually written, or the
empty string if a supplied file handle or STDOUT was written to, or
undef if there was a failure.

If write() returns undef, then it will also have set the _ErrorMsg
parameter in the object.

write() never modifies any data in the object itself.

Consequently, if you specify a _FieldList or a _Selection, only those
fields or records will be written, but the corresponding parameters in
the object itself will be left untouched.

=head2 How write() calculates the Path

The _FileName parameter is shared with the read() method.  This
parameter is set by read() and may be overridden when calling write().

In the base implementation of Data::CTable, write() will try not to
overwrite the same file that was read, which could possibly cause data
loss.

To avoid this, it does not use the _FileName parameter directly.
Instead, it starts with _FileName and inserts or appends the value of
the _WriteExtension parameter (which defaults to ".out") into the file
name before writing.

If the _FileName already has an extension at the end, write() will
place the _WriteExtension BEFORE the final extension; otherwise the
_WriteExtension will be placed at the end of the _FileName.

For example:

    Foobar.txt        ==>  Foobar.out.txt
    Foobar.merge.txt  ==>  Foobar.merge.out.txt
    My_Merge_Data     ==>  My_Merge_Data.out

If you DON'T want write() to add a _WriteExtension to _FileName before
it writes the file, then you must set _WriteExtension to empty/undef
either in the object or when calling write().  Or, you could make a
subclass that initializes _WriteExtension to be empty.  If
_WriteExtension is empty, then _FileName will be used exactly, which
may result in overwriting the original data file.

Remember: write() returns the path name it actually used to
successfully write the file.  Just as with read(), if the _FileName
you specified did not have a path, then write() will prepend a path
component indicating "current directory" (e.g. "./" on Unix) and this
will be part of the return value.


=head2 Cacheing with write()

By default, Data::CTable only creates a cached version of a file when
it reads that file for the first time (on the assumption that it will
need to read the file again more often than the file's data will
change.)

But by default, it does not create a cached version of a file when
writing it, on the assumption that the current program probably will
not be re-reading the written file and any other program that wants to
read it can cache it at that time.

However, if you want write() to create a cache for its output file, it
is much faster to create it on write() than waiting for the next
read() because the next read() will be able to use the cache the very
first time.

To enable write-cacheing, set _CacheOnWrite to true.  Then, after the
write() successfully completes (and only if it does), the cached
version will be written.

=head1 FORMATTED TABLES (Using Data::ShowTable)

    ## Get formatted data in memory

    my $StringRef = $t->format();     ## Format same data as write()
    my $StringRef = $t->format(10);   ## Limit records to 10
    my $StringRef = $t->format(...);  ## Specify arbitrary params
    print $$StringRef;

    ## Write formatted table to file or terminal

    $t->out($Dest, ....);## $Dest as follows; other params to format()
    $t->out($Dest, 10, ....) ## Limit recs to 10; params to format()

    $t->out()            ## print formatted data to STDOUT
    $t->out(\*STDERR)    ## print to STDERR (or any named handle)
    $t->out("Foo.txt")   ## print to any path (file to be overwritten)
    $t->out($FileObj)    ## print to any object with a print() method

out() takes a first argument specifying a destination for the output,
then passes all other arguments to format() to create a nice-looking
table designed to be human-readable; it takes the resulting buffer and
print()s it to the destination you specified.

Sample output:

     +-------+------+-----+-------+
     | First | Last | Age | State |
     +-------+------+-----+-------+
     | Chris | Zack | 43  | CA    |
     | Marco | Bart | 22  | NV    |
     | Pearl | Muth | 15  | HI    |
     +-------+------+-----+-------+

    (Note extra space character before each line.)

The destination may be a file handle (default if undef is \*STDOUT), a
string (treated as a path to be overwritten), or any object that has a
print() method, especially an object of type IO::File.

The main purpose of out() is to give you a quick way to dump a table
when debugging.  out() calls format() to create the output, so read
on...

format() produces a human-readable version of a table, in the form of
a reference to a string buffer (which could be very large), and
returns the buffer to you.  Dereference the resulting string reference
before using.

If format() is given one argument, that argument is the _MaxRecords
parameter, which limits the length of the output.

Otherwise, format() takes the following named-parameter arguments,
which can optionally override the corresponding parameters, if any, in
the object:

    _FieldList        ## Fields to include in table
    _Selection        ## Records to be included, in order

    _SortSpecs        ## SortType controls number formatting
    _DefaultSortType

    _MaxRecords       ## Limit number of records output

    _MaxWidth         ## Limit width of per-col. data in printout

format() will obey _MaxRecords, if you'd like to limit the number of
rows to be output.  _MaxRecords can also be a single argument to
format(), or a second argument to out() if no other parameters are
passed.

format() also recognizes the _SortSpecs->{SortType} and
_DefaultSortType parameters to help it determine the data types of the
fields being formatted.  Fields of type "Number" are output as
right-justified floats; "Integer" or "Boolean" are output as
right-justified integers, and all others (including the default:
String) are output as left-justified strings.

In addition, there is one parameter uniquely supported by format() and
out():

=over 4

=item _MaxWidth  ||= 15;

=back

_MaxWidth specifies the maximum width of columns.  If unspecifed, this
will be 15; the minimum legal value is 2.  Each column may actually
take up 3 more characters than _MaxWidth due to divider characters.

The data to be output will be examined, and only the necessary width
will be used for each column.  _MaxWidth just limits the upper bound,
not the lower.

Data values that are too wide to fit in _MaxWidth spaces will be
truncated and the tilde character "~" will appear as the last
character to indicate the truncation.

Data values with internal returns will have the return characters
mapped to slashes for display.

format() and out() will NOT wrap entries onto a second line,
like you may have seen Data::ShowTable::ShowBoxTable do in some cases.
Each record will get exactly one line.

format() and out() ignore the _HeaderRow parameter.  A header
row showing the field names is always printed.

format() and out() make no attempt to map upper-ascii characters from
or to any particular dataset.  The encoding used in memory (generally
ISO 8859-1 by default) is the encoding used in the output.  If you
want to manipulate the encoding, first call format(), then change the
encoding, then format the resulting table.

=cut

sub write                   ## Write, cacheing afterward if possible
{
    my $this        = shift;
    return($this->write_file_and_cache(@_));
}

sub write_file_and_cache    ## Write, cacheing afterward if possible
{
    my $this            = shift;

    my $Params          = (@_ == 1 ? {_FileName => $_[0]} : {@_});
    
    my($FieldList, $LineEnding, $FDelimiter, $HeaderRow, $CacheOnWrite, $CacheExtension, $CacheSubDir) = map {$this->getparam($Params, $_)} 
    qw($FieldList  _LineEnding  _FDelimiter  _HeaderRow  _CacheOnWrite  _CacheExtension  _CacheSubDir);

    ## First write the file and go to done if it failed.
    my $WriteFileName   = $this->write_file(@_) or goto done;

    ## Only try to cache if we got a non-empty $WriteFileName back.
    ## We won't get a name back in the case where we wrote directly to
    ## an open file handle.
    
    goto done unless $WriteFileName;
    
    ## Only try to cache if $CacheOnWrite has been turned ON.
    goto done unless $CacheOnWrite;

    ## Now, having written successfully, we try to write the cache for
    ## next time.  Writing the cache is always optional; failing to
    ## write it is not a failure of this method.  Consequently, any
    ## "goto done" statements beyond this point will still result in a
    ## successful outcome since $WriteFileName will have a value.

    ## Calculate the name of the cache file and fail the directory
    ## creation fails.  prep_cache_file will have generated a warning
    ## if an attempt to create needed subdirectories has failed.

    my $CacheFileName   = $this->prep_cache_file($WriteFileName, $CacheExtension, $CacheSubDir)
        or goto done;
    
    ## Pre-flight the cache file for writing.
    $this->warn("Cache file $CacheFileName cannot be created/overwritten: $!"), 
    goto done                                           ## Successful completion.
        unless $this->try_file_write($CacheFileName);

    ## The data to be stored is:
    
    ##   1) All data columns written by write_file()
    ##   2) Any file format parameters used by write_file()

    ## Calculate the main writing-related parameters using the same
    ## logic that write_file() uses...

    ## Default for FieldList is all fields.
    $FieldList          ||= $this->fieldlist();
    
    ## Convert from optional "dos", "mac", "unix" symbolic values.
    $LineEnding = $this->lineending_string($LineEnding);
    
    ## Default for LineEnding is "\n" (CR on Mac; LF on Unix; CR/LF on DOS)
    $LineEnding         = "\n"      unless length($LineEnding);
    
    ## Default for FDelimiter is comma
    $FDelimiter         = ','       unless length($FDelimiter);

    ## Default for HeaderRow is true.
    $HeaderRow          = 1         unless defined($HeaderRow);

    ## No other parameters should be cached because we want a
    ## read from the cache to produce exactly the same result
    ## as a read from the file itself would have produced.
    
    my $Data = {(
                 ## Refs to each column written
                 %{                 $this->cols_hash($FieldList)},  
                 
                 ## Other relevant file-format parameters
                 _FieldList     =>  $FieldList,
                 _LineEnding    =>  $LineEnding,
                 _FDelimiter    =>  $FDelimiter,
                 _HeaderRow     =>  $HeaderRow,
                 _Subset        =>  $this->{_Subset} || 0,
                 _Newline       =>  "\n",
                 
                 ## We don't need to save _ReturnMap and
                 ## _ReturnEncoding because those only are relevant
                 ## when reading physical files. Cached data has the
                 ## return chars already encoded as returns.
                 
                 )};
    
    $this->warn("Failed to cache $CacheFileName"), 
    unlink($CacheFileName),                      ## Delete cache if failure
    goto done                                    ## Successful completion.
        unless $this->write_cache($Data, $CacheFileName);
    chmod 0666, $CacheFileName;                  ## Liberal perms if possible.
    
  done:
    return($WriteFileName);
}

sub write_cache
{
    my $this                    = shift;
    my ($Data, $CacheFileName)  = @_;
    
    $this->progress("Storing $CacheFileName...");
    
    my $Success;
    eval 
    {
        $Success = nstore($Data, $CacheFileName);
    };      

    $this->progress("Stored  $CacheFileName.") if $Success;
    
  done:
    return($Success);
}

sub write_file      ## Just write; don't worry about cacheing
{
    my $this        = shift;
    my $Params      = (@_ == 1 ? {_FileName => $_[0]} : {@_});

    my($FileName, $FieldList, $Selection, $MaxRecords, $LineEnding, $FDelimiter, $QuoteFields, $ReturnMap, $ReturnEncoding, $MacRomanMap, $HeaderRow, $WriteExtension) 
        = map {$this->getparam($Params, $_)} 
    qw(_FileName  _FieldList  _Selection  _MaxRecords  _LineEnding  _FDelimiter  _QuoteFields  _ReturnMap  _ReturnEncoding  _MacRomanMap  _HeaderRow  _WriteExtension);

    my $Success;
    
    $this->{_ErrorMsg} = "";

    ## if FileName is unspecified, or is the single character "-",
    ## then default to STDOUT.
    $FileName = \ *STDOUT if ($FileName =~ /^-?$/);
    
    ## If we have a regular file handle, bless it into IO::File.
    $FileName = bless ($FileName, 'IO::File') if ref($FileName) =~ /(HANDLE)|(GLOB)/;
    
    ## If we have a file handle either passed or constructed, make note of that fact.
    my $GotHandle = ref($FileName) eq 'IO::File';
    
    $this->{_ErrorMsg} = "FileName must be specified for write()", goto done
        unless $GotHandle or length($FileName);
    
    ## Default for FieldList is all fields.
    $FieldList          ||= $this->fieldlist();
    
    ## Default for Selection is all records.
    $Selection          ||= $this->selection();

    ## Default for MaxRecords is 0 (meaning write all records)
    $MaxRecords         = 0         unless (int($MaxRecords) == $MaxRecords);

    ## Convert from optional "dos", "mac", "unix" symbolic values.
    $LineEnding = $this->lineending_string($LineEnding);

    ## Default for LineEnding is "\n" (CR on Mac; LF on Unix; CR/LF on DOS)
    $LineEnding         = "\n"      unless length($LineEnding);

    ## Default for FDelimiter is comma
    $FDelimiter         = ','       unless length($FDelimiter);

    ## Default for QuoteFields is undef (auto)
    $QuoteFields        = undef     unless defined($QuoteFields);

    ## "QuoteCheck" mode means check each field -- this is the "auto"
    ## mode that kicks in when _QuoteFields is undef.
    my $QuoteCheck      = (!defined($QuoteFields));
    
    ## Default for ReturnMap is true.
    $ReturnMap          = 1         unless defined($ReturnMap);

    ## Default for MacRomanMap is undef ("Auto");
    $MacRomanMap        = undef     unless defined($MacRomanMap);

    ## DoMacMapping is the actual setting for auto charset mapping
    my $DoMacMapping    = 
        ((!defined($MacRomanMap) && ($LineEnding eq "\x0D")) || ## Auto
         ($MacRomanMap));                                       ## On
    
    $this->progress("Will convert upper-ascii characters if any, from ISO-8859-1 to Mac Roman.") if $DoMacMapping;

    ## Default for ReturnEncoding is "\x0B" (control-K; ASCII 11)
    $ReturnEncoding     = "\x0B"    unless length($ReturnEncoding);

    ## Default for HeaderRow is true.
    $HeaderRow          = 1         unless defined($HeaderRow);

    ## Default for $WriteExtension is "" (none) -- meaning use exact $FileName
    $WriteExtension     = ""        unless defined($WriteExtension);

    ## Get a hash of fields actually present...

    my $AllFields       = $this->fieldlist_all();
    my $AllFieldsHash   = {}; @$AllFieldsHash{@$AllFields} = undef;

    ## Cull $FieldList to only include fields we have...

    $FieldList          = [grep {exists($AllFieldsHash->{$_})} @$FieldList];
    
    ## Ensure $Selection contains only valid record numbers...

    $Selection          = $this->selection_validate_internal($Selection);

    ## Get an ordered list of the columns.
    my $Columns         = [@$this{@$FieldList}];

    ## Calculate the name of the file we'll write to, if any, and get
    ## the file handle either from the one we were given or by opening
    ## the specified file for writing.

    my $WriteFileName;
    my $OutFile;

    if ($GotHandle)
    {
        $WriteFileName  = "";
        $OutFile        = $FileName; ## Actually a handle.
    }
    else
    {
        ## Calculate the name of the file we're going to try to write to.
        $WriteFileName  = 
            
            ## Use file name exactly if specified in write() call.
            ($Params->{_FileName} ? $FileName : 
             
             ## Otherwise, calculate the name by adding/appending the _WriteExtension.
             $this->write_file_name($FileName, $WriteExtension));
         
        ## Ensure the directory that will hold the file actually exists.
        use File::Basename         qw(fileparse);
        my ($Basename, $Path, $Ext) = fileparse($WriteFileName, '\.[^\.]+');
        my ($Sep, $Up, $Cur) = @{$this->path_info()}{qw(sep up cur)};
        $Path ||= $Cur; ## Once again, default $Path to cwd just in case.
        
        $this->{_ErrorMsg} = "Can't make directory $Path to save $WriteFileName: $!", goto done 
            unless $this->verify_or_create_path($Path, $Sep);
        
        ## Ensure the directory is writeable and the file is overwriteable
        ## if it exists.
        $this->{_ErrorMsg} = "Directory $Path is not writeable.", goto done 
            unless (-w $Path);
        
        $this->{_ErrorMsg} = "File $WriteFileName cannot be overwritten.", goto done 
            if (-e $WriteFileName && !(-w $WriteFileName));
        
        ## Open the file for write.
        
        use        IO::File;
        $OutFile = IO::File->new(">$WriteFileName");
        $this->{_ErrorMsg} = "Failed to open $WriteFileName for writing: $!", goto done
            unless $OutFile;
        
        $this->progress("Writing $WriteFileName...");
    }

    ## Figure out the line initiator & ender strings, and delimiter sequence.

    my ($LineStartQuote, $Delim, $LineEndQuote);
    if ($QuoteFields)
    {
        ## In "forced" quote mode, we just do the quoting by always
        ## putting them at the start and end of lines and in between
        ## each field.

        $LineStartQuote = "\"";
        $LineEndQuote   = "\"";
        $Delim  = "\"$FDelimiter\"";
    }
    else
    {
        ## In no-quote or auto-quote mode, we don't put the quotes in
        ## these places; they'll either be omitted entirely or
        ## inserted per-field.

        $LineStartQuote = '';
        $LineEndQuote   = '';
        $Delim  = $FDelimiter;
    }

    ## Precompile a regex that checks for quotes, the field delimiter
    ## sequence, the line ending sequence, or any line-ending-ish
    ## characters at all.  In $QuoteCheck mode, we'll use this to
    ## identify fields needing double-quotes.

    my $QuoteOrDelimCheck   = qr{(?:\Q$LineEnding\E)|(?:\Q$FDelimiter\E)|[\"\x0D\x0A]} if $QuoteCheck;

    ## Precompile a return-map-checking regex that checks first for
    ## the line ending actively in use and then for the platform's
    ## "\n".  These may be the same, but are not necessarily always
    ## the same.  We don't return-map any old \x0D or \x0A because,
    ## for example in a pure Unix world, \x0D would not be interpreted
    ## as line-ending-related and hence is a valid character in a
    ## field.

    my $ReturnMapCheck      = qr{(?:\Q$LineEnding\E)|(?:\n)} if $ReturnMap;

    ## Print out the header row that lists the field names in order.
    
    if ($HeaderRow)
    {
        my $Line = ($LineStartQuote .
                    join($Delim,
                         map 
                         {
                             ## Quote any " character as ""
                             (my $X = $_) =~ s/\"/\"\"/g;
                             
                             ## In QuoteCheck mode _QuoteFields =>
                             ## undef ("auto"): put quotes around
                             ## field only if required.
                             
                             $X = "\"$X\"" if $QuoteCheck && $X =~ $QuoteOrDelimCheck;
                             
                             ## Convert returns back to \x0B
                             $X =~ s/$ReturnMapCheck/$ReturnEncoding/g if $ReturnMap;
                             
                             $X;
                         } @$FieldList) .
                    $LineEndQuote . 
                    $LineEnding);
        
        ## Maybe convert entire line (all records) ISO to Mac before writing it.
        &ISORoman8859_1ToMacRoman(\ $Line) if $DoMacMapping;
        
        $OutFile->print($Line) if $HeaderRow;
    }
    
    ## Print out each row (record).  Fields are output in $FieldList
    ## order (same order as they were in the header row, if any).
    ## Records are printed in the order specified in $Selection.
    
    my $WroteProg;
    my $TotalLen        = @$Selection+0;
    my $RecordsToWrite  = ($MaxRecords ? min($MaxRecords, $TotalLen) : $TotalLen);
    my $RecordsWritten  = 0;

    foreach my $i (@$Selection)
    {
        my $Line    = ($LineStartQuote .
                       join($Delim,
                            map 
                            {
                                ## Quote any " character as ""
                                (my $X = $_->[$i]) =~ s/\"/\"\"/g;
                                
                                ## In QuoteCheck mode _QuoteFields =>
                                ## undef ("auto"): put quotes around
                                ## field only if required.
                                
                                $X = "\"$X\"" if $QuoteCheck && $X =~ $QuoteOrDelimCheck;
                                
                                ## Convert returns back to \x0B
                                $X =~ s/$ReturnMapCheck/$ReturnEncoding/g if $ReturnMap;
                             
                                $X;
                            } @$Columns) . 
                       $LineEndQuote . 
                       $LineEnding);
        
        ## Maybe convert entire line (all records) ISO to Mac before writing it.
        &ISORoman8859_1ToMacRoman(\ $Line) if $DoMacMapping;
        
        $OutFile->print($Line);
        
        $RecordsWritten++;

        ## Try doing timed (throttled to 1 per 2 secs) progress at
        ## most every 100th record.
        my $Did = $this->progress_timed("Writing", $RecordsWritten, $RecordsWritten, $RecordsToWrite, 1) 
            if (($RecordsWritten % 100) == 0);  
        $WroteProg ||= $Did;
        
        ## Stop if we have written all the records we wanted.
        last if ($RecordsWritten >= $RecordsToWrite);
    }
    
    ## If we wrote timed progress but didn't get to give the 100%
    ## message yet, print the 100% message now.
    if ($WroteProg)
    {
        my $FinalProg = $this->progress_timed("Writing", $RecordsWritten, $RecordsWritten, $RecordsToWrite, 1) 
            unless (($RecordsWritten % 100) == 0);  
    }
    
    ## Print the regular Done message.
    if ($GotHandle)
    {
        $this->progress("Done writing.");
        $Success = 1;
    }
    else
    {
        $this->progress("Wrote   $WriteFileName.");
    }

    if (!$GotHandle)
    {
        ## Close the file and check the exit code.
        $OutFile->close();
        $Success = (($?>>8) == 0);
        
        $this->{_ErrorMsg} = "Unexpected failure writing $WriteFileName ($!)", goto done
            unless $Success;
    }

  done:
    $this->warn("FAILURE: $this->{_ErrorMsg}") unless $Success;

    return($Success ? $WriteFileName : undef);
}

sub write_file_name         ## Calculate the name of the file to be written.
{
    my $this                        = shift;
    my ($FileName, $WriteExtension) = @_;

    ## Break the path into its parts...
    use File::Basename         qw(fileparse);
    my ($Basename, $Path, $Ext) = fileparse($FileName, '\.[^\.]+');

    ## If no directory is explicitly named, set $Path to be the
    ## implicit "current directory" (e.g. "./")
    
    my ($Sep, $Up, $Cur) = @{$this->path_info()}{qw(sep up cur)};
    $Path ||= $Cur;
    
    ## If $WriteExtension is empty, which is allowed, then the result
    ## will be the same as $FileName, which could in some cases result
    ## in the overwriting of the same file that was read in (and may
    ## be what is intended).

    my $WriteFileName               = "$Path$Basename$WriteExtension$Ext";

    return($WriteFileName);
}

sub out
{
    my $this            = shift;
    my $Dest            = shift;

    my $Success;

    ## First do the formatting (or fail) -- format() will warn if needed.
    my $Data            = $this->format(@_) or goto done;

    ## If $Dest is empty or not defined, use STDOUT.
    $Dest ||= \*STDOUT;
    
    ## If given an IO handle (such as \*STDERR), bless and use it.
    if (ref($Dest) eq 'HANDLE') {$Dest  = bless($Dest     , 'IO::File')};
    
    ## If it is not an object, treat it as a file name to be opened.
    if (!ref($Dest))     {$Dest = (IO::File->new(">$Dest") or 
                                   $this->warn("Can't open file $Dest: $!"), goto done)};

    ## At this point treat $Dest as an object with a print method and
    ## complain if the print method doesn't return a true value.
    
    $Dest->print($$Data) or $this->warn("Had trouble writing file: $!"), goto done;
    
    $Success = 1;
  done:
    return($Success);
}

sub format  ## use Data::ShowTable to format the table in a pretty way.
{
    my $this            = shift;
    my $Params          = (@_ == 1 ? {_MaxRecords => $_[0]} : {@_});

    my($Selection, $FieldList, $SortSpecs, $DefaultSortType, $MaxRecords, $MaxWidth)    = map {$this->getparam($Params, $_)} 
    qw(_Selection  _FieldList  _SortSpecs  _DefaultSortType  _MaxRecords  _MaxWidth);

    ## This method relies on Data::ShowTable.
    $this->warn("@{[__PACKAGE__]}::show() requires optional Data::ShowTable module."), goto done
        unless $HaveShowTable;
    
    $FieldList          ||= $this->fieldlist();
    $Selection          ||= $this->selection();
    $SortSpecs          ||= {};
    $DefaultSortType    = 'String' unless (length($DefaultSortType));
    $MaxRecords         ||= 0;  ## Default is no maximum (all records).
    $MaxWidth           ||= 15; ## Zero or undef means use default.
    $MaxWidth           = max(2, $MaxWidth);    ## MaxWidth must not be less than 2
    
    my $TypeMap         = {qw(string char 
                              text text 
                              integer int 
                              number numeric 
                              boolean int)};

    my $Types           = [map {$TypeMap->{lc(@{$SortSpecs->{$_} || {}}{SortType} || 
                                              $DefaultSortType)                  } || 'string'}  
                           @$FieldList];

    my $TotalLen        = @$Selection+0;
    my $RecordsToWrite  = ($MaxRecords ? min($MaxRecords, $TotalLen) : $TotalLen);

    ## The row-yielder subroutine and its private state variables.
    my $SelNum          = 0;
    my $RowSub          = sub   ## A closure over the local vars in this subroutine.
    {
        ## We might be asked to rewind.
        my ($Rewind)    = @_;
        $SelNum = 0, return(1) if ($Rewind);
        
        ## Done if we've written all rows.
        return() if $SelNum >= $RecordsToWrite;
        
        ## Otherwise, yield a row if we still can.
        my $List = [map 
                    {
                        ## Truncate as needed.
                        my $X = (length > $MaxWidth ?   
                                 (substr($_, 0, ($MaxWidth - 1)) . '>') : $_);

                        ## Encode returns, tabs as carets.
                        $X =~ s{(?:\x0D\x0A)|[\x0D\x0A\x09]}{^}g;   
                        
                        $X;
                    }
                    @{$this->row_list($Selection->[$SelNum++], $FieldList)}];
        
        return(@$List);
    };

    ## Locally replace put() and out() in Data::ShowTable so we can
    ## gather the data into memory instead of having it go right out
    ## to STDOUT before we may want it to.

    ## Too bad Data::ShowTable is not a subclassable object instead.

    my $Data            = [""];     ## Array to hold output from out() and put()
    {
        local *{Data::ShowTable::out} = sub     ## See sub out in ShowTable.pm
        {
            my $fmt         = shift;
            $fmt            .= "\n" unless $fmt =~ /\n$/;
            $Data->[-1]     .= sprintf($fmt, @_); 
            push            @$Data, "";
        };
        
        local *{Data::ShowTable::put} = sub     ## See sub put in ShowTable.pm
        {
            my $fmt         = shift();
            $Data->[-1]     .= sprintf($fmt, @_);
        };
        
        &ShowBoxTable({titles   => $FieldList, 
                       types    => $Types,
                       row_sub  => $RowSub,
                       widths   => [],          ## Will calculate from the data
                   });
    }
    
    ## Remove spurious extra newline entries at end of $Data
    pop @$Data if $Data->[-1] =~ /^\s*$/s;
    pop @$Data if $Data->[-1] =~ /^\s*$/s;

    my $Formatted = join("", @$Data);

  done:
    return(\ $Formatted);
}


=pod

=head1 APPENDING / MERGING / JOINING TABLES

    ## Append all records from a second table

    $t->append($Other)                ## Append records from $Other
    $t->append_file($File, $Params)   ## Append from new($Params, $File)
    $t->append_files($Files, $Params) ## Call append_file for all files
    $t->append_files_new($Files, $Params) ## Internal helper routine

    ## Combine all fields from a second table

    $t->combine($Other)                ## Combine fields from $Other
    $t->combine_file($File, $Params)   ## Combine new($Params, $File)
    $t->combine_files($Files, $Params) ## combine_file on each file

    ## Left-join records from a second table (lookup field vals)

    $t->join      ($Other,          $KeyField1, [$KeyField2, $Fields]) 
    $t->join_file ($File,  $Params, $KeyField1, [$KeyField2, $Fields])
    $t->join_files($Files, $Params, $KeyField1, [$KeyField2, $Fields])

The append() method concatenates all the records from two CTable
objects together -- even if the two tables didn't start out with
exactly the same fields (or even any of the same fields).

It takes all the data records from another CTable object and appends
them into the present table.  Any columns present in the $Other table
but not in the first table, are created (and the corresponding field
values in the first table will all be empty/undef).  Similarly, any
columns present in $t but not present in $Other will be extended
to the correct new length as necessary and the field values in the
original columns will be empty/undef.  Columns present in both will,
of course, have all the data from both the original sets of data.

All data from the second table is brought into the first one.  No
attempt whatsoever is made to eliminate any duplicate records that
might result.

The number of records (length()) after this call is the sum of the
length() of each of the tables before the operation.

IMPORTANT NOTE: The data from the $Other object is COPIED in memory
into the new object.  This could be hard on memory if $Other is big.
Might want to be sure to discard $Other when you're done with it.

$Other is left untouched by the operation.

All columns from both tables are combined whether or not they are
mentioned in the custom field list of either.

The custom field lists, if present in either table object, are
concatenated into this object's custom field list, but with
duplications eliminated, and order retained.

Any existing custom selections, custom sort order, sort specs, and/or
sort routines are also combined appropriately, with settings from this
object taking precedence over those from $Other anywhere the two have
conflicting settings.

append_file() takes a file name and optional $Params hash.  It uses
those to create a new() object with data read from the file.  Then,
the new table is appended to $t using append() and then the new table
is discarded.

append_files() is a convenience function that calls append_file() on
each file in a list, using the same optional $Params for each.

append_files_new() is the internal routine that implements the
processing done by new() on the optional list of files to be read.  It
does the following: It calls read() on the first file in the list.
Then, it calls append_files() to read the remaining into their own
new() objects of the same class as $t and using the same $Params to
new() (if any were supplied).  Then each of these is append()-ed in
turn to $t and discarded.  The final result will be that $t will hold
a concatenation of all the data in all the files mentioned.  However,
consistent with the behavior of append(), the _FileName parameter and
other read()-controlled settings will correspond to the first file
read.  The intermediate objects are discarded.

NOTE: As with new() and read(), if a non-empty _FieldList Param is
specified, the read() methods called internally by the append_file*()
methods will read only the fields mentioned and will ignore any other
fields in the files.

=head2 Combining tables

combine() adds columns from a second table into the current one.  

CAUTION: You should only use combine() when you have two tables where
all the (possibly selected) records in the second table line up
perfectly with all the (unselected) records in the first table -- in
other words, each table before combine() should contain a few of the
columns of the new table -- for example, maybe one table contains a
column of file names, and the other contains columns of corresponding
file sizes and modification times.  If you don't understand the
consequences of combine, don't use it or you could end up with some
records whose field values don't refer to the same object.  (Maybe you
meant to use append() or join() instead.)

If the second table has a custom field list, only those columns are
brought in.

If any column in the second table has the same name as one in the
current table, the incoming column replaces the one by the same name.

All columns are COPIED from the second table, so the first table owns
the new data exclusively.

If the second table has a selection, only those records are copied, in
selection order.  (select_all() first if that's not what you want.)

The selection in the first table, if any, is ignored during the
combine.  If this isn't what you want, then consider using cull()
before combine().

Field list and sort order are concatenated (but retaining uniqueness:
second mentions of a field in the combined lists are omitted).

Custom sort routines and sort specs are combined, with those in the
first table taking precedence over any copied in with the same name.

The custom _Selection from the first table, if any, is retained.  (It
will initially omit any records added by extend()).

All other parameters from the first table are retained, and from the
second table are ignored.

combine() calls extend() after combining to ensure that all columns
have the same length: if either the older or newer columns were
shorter, they will all be set to the length of the longest columns in
the table -- creating some empty field values at the end of the
lengthened columns.

combine_file() does the same as combine() except starting with a file
name, first creating the $Other object by creating it using
new($Params, $File), then discarding it after combining.

=head2 Joining tables (Looking up data from another table)

join() looks up field values from a second table, based on common
values in key fields which may have different or the same names in
each table.  It adds columns to the current table if necessary to hold
any new field values that must be brought in.

join() never adds any new or non-matching records to the table:
records where the lookup fails will simply have empty/undef values in
the corresponding columns.

    ## Example:

    $t->join     ($People,          'FullName', 'FirstAndLast'); ## or
    $t->join_file("People.txt", {}, 'FullName', 'FirstAndLast');


Here's how join() calculates the list of fields to bring in:

    - Legal field names from the optional $Fields list, if supplied
    - Otherwise, the fieldlist() from second table
    - ... minus any fields with same name as $KeyField1 or $KeyField2

Join starts by adding new empty columns in the first table for any
field to be brought in from the second but not yet present in the
first.

Here's how join() calculates the records eligible for lookup:

    - Join only modifies the selected records in the first table
    - Join only looks up values from selected records in second table

(If you want all records to be used in both or either table, call the
table's select_all() method before calling join().)

Then, for every selected record in $t (using the example above), join
examines the FullName field ($KeyField1), and looks up a corresponding
entry (must be 'eq') in the FirstAndLast field ($KeyField2) in the
second table.

IMPORTANT NOTE ABOUT KEY LENGTH: To speed lookup, hash-based indices
are made.  The strings in $Key1 and $Key2 fields should not be so long
that the hash lookups bog down or things could get ugly fast.  There
is no fixed limit to hash key length in Perl, but fewer than 128
characters in length is longer than customary for such things.  (Many
systems require text-based keys to be no longer than 31 characters.)
So be judicious about the values in $Key1 and $Key2 fields.

The first record found in the second table's selection with a matching
value in the key field is then copied over (but only the appropriate
fields are copied, as explained above).  Any field values being
brought over will REPLACE corresponding field values in the first
table, possibly overwriting any previous values if the field being
looked up was already present in the first table and contained data.

The first table's _FieldList is updated to reflect new fields added.

Its _Selection is untouched.

Its _SortOrder is untouched.

Its _SortSpecs are augmented to include any entries from the second
table that should be brought over due to the field additions.

Its _SRoutines are augmented to add new ones from the second table.

All other parameters of table 1 are untouched.

The second table is not modified.  No data structures will be shared
between the tables.  Data is only copied.

join_file() calls join() after creating a seond table from your $File.

join_files() calls join_file() repeatedly for each file in a list, but
it is important to note that each file in the list of files to be
joined must have a $Key2 field -- AND, that any values looked up from
the second file will overwrite any values of the same key found in the
first file, and so on.  You probably will not ever need join_files().
It is mainly here for completeness.

=cut
 
{}; ## Get emacs to indent correctly.

sub append ## ($this, $OtherCTable)
{
    my $this        = shift;
    my ($that)      = @_;

    my $Success;

    ## Get all fields in $this, but only selected ones in $that
    my $ThisFieldsAll   = $this->fieldlist_all();
    my $ThatFields      = $that->fieldlist();

    ## Figure out how many data fields in each of the tables.
    my $ThisFieldCount  = @$ThisFieldsAll+0;
    my $ThatFieldCount  = @$ThatFields   +0;

    ## We're going to bring over only the selected records in $that.
    my $ThisSel         = $this->selection();
    my $ThatSel         = $that->selection();

    ## Figure out how many records there were to start with.
    my $ThisLength      = $this->length();
    my $ThatLength      = @$ThatSel+0;

    ## New record count is sum of the other two.
    my $NewLength       = $ThisLength + $ThatLength;

    ## Create any missing columns not yet present in $this and,
    ## whether new or not, presize all vectors to the new length,
    ## which will create empty/undef entries as necessary.

    foreach (@$ThisFieldsAll, @$ThatFields) {$#{$this->{$_} ||= []} = ($NewLength - 1)};

    ## Then copy the field data from the second table into the already
    ## pre-sized columns in this one.

    foreach my $FieldName (@$ThatFields)
    {
        my $NewVector   = $this->{$FieldName};
        my $OldVector   = $that->sel_get($FieldName, $ThatSel);
        
        foreach my $RecordNum (0..$#$OldVector) 
        {($NewVector->[$ThisLength + $RecordNum] = 
          $OldVector->[              $RecordNum])};
    }

    ## Now all the data columns have been combined.  We just have to
    ## combine any custom metadata.
    
    ## If either table had a custom fieldlist, then make a new custom
    ## field list which is the result of concatenating both field
    ## lists together, without duplicates, and of course preserving
    ## the original order as completely as possible (with the order
    ## given in the first table taking precedence).

    if (defined($this->{_FieldList}) || 
        defined($that->{_FieldList}))
        
    {
        my $ThisFields      = $this->fieldlist();
        my $ThatFields      = $that->fieldlist();

        ## Make a hash mapping field names from both tables to the
        ## order they should appear

        my $FieldOrderHash  = {};
        foreach (@$ThisFields, @$ThatFields) {$FieldOrderHash->{$_} ||= (keys %$FieldOrderHash) + 1};

        my $FieldList       = [sort {$FieldOrderHash->{$a} <=> $FieldOrderHash->{$b}} keys %$FieldOrderHash];
        
        $this->{_FieldList} = $FieldList;
    }       

    ## If either table had a custom sortorder, then make a new custom
    ## sort order which is the result of concatenating both orders
    ## together, without duplicates, and of course preserving the
    ## original order as completely as possible (with the order given
    ## in the first table taking precedence).
    
    if (defined($this->{_SortOrder}) || 
        defined($that->{_SortOrder}))
    {
        my $ThisOrder       = $this->sortorder();
        my $ThatOrder       = $that->sortorder();

        ## Make a hash mapping field names from both lists to the
        ## order they should appear

        my $OrderHash       = {};
        foreach (@$ThisOrder, @$ThatOrder) {$OrderHash->{$_} ||= (keys %$OrderHash) + 1};
        
        my $OrderList       = [sort {$OrderHash->{$a} <=> $OrderHash->{$b}} keys %$OrderHash];
        
        $this->{_SortOrder} = $OrderList;
    }       

    ## If either table had a custom selection, then create a new
    ## selection which is the concatenation of the two selections.

    if (defined($this->{_Selection}) || 
        defined($that->{_Selection}))
    {
        $this->{_Selection} = [@$ThisSel,   ## Original selected records...
                               ## Plus an adjusted entry for newly-added ones.
                               ($ThisLength .. ($ThisLength + @$ThatSel - 1))
                               ];
    }

    ## If either table had custom sortspecs, then create a new
    ## sortspecs hash by starting with all the entries from the other
    ## table and adding/overwriting with those from this table.

    if (defined($this->{_SortSpecs}) || 
        defined($that->{_SortSpecs}))
    {
        my $ThisSpecs       = $this->sortspecs();
        my $ThatSpecs       = $that->sortspecs();

        $this->{_SortSpecs} = {%$ThatSpecs, %$ThisSpecs};
    }

    ## If either table had custom sortroutines, then create a new
    ## sortroutines hash by starting with all the entries from the
    ## other table and adding/overwriting with those from this table.

    if (defined($this->{_SRoutines}) || 
        defined($that->{_SRoutines}))
    {
        my $ThisRoutines    = $this->{_SRoutines} || {};
        my $ThatRoutines    = $that->{_SRoutines} || {};

        $this->{_SRoutines} = {%$ThatRoutines, %$ThisRoutines};
    }

    ## All other settings are kept from $this and those from $that are
    ## ignored.

    $Success = 1;
done:
    return($Success);
}

sub append_file
{
    my $this                = shift;
    my ($FileName, $Params) = @_;

    my $Success;

    ## $Params argument is optional.  If supplied, it must be a hash.
    $Params ||= {};

    ## Create a new empty table object of the same class as $this and
    ## read just the specified file into it. (note: this could be a
    ## recursive call here).

    my $that                = ref($this)->new($Params, $FileName) or goto done;
    
    ## Append the data from $that table into this one.

    $this->append($that) or goto done;

    $Success = 1;
  done:
    return($Success);
}

sub append_files
{
    my $this                    = shift;
    my ($FileNames, $Params)    = @_;
    
    my $Success;
    
    foreach my $FileName (@$FileNames) 
    {
        goto done unless $this->append_file($FileName, $Params);
    }
    
    $Success = 1;
  done:
    return($Success);
}

sub append_files_new    ## Called by new() to process its file name args.
{
    my $this                    = shift;
    my ($FileNames, $Params)    = @_;

    my $Success;

    ## First we read the first file, if any, into this object using
    ## the read() method.

    my $FirstFile = shift @$FileNames;
    if (defined($FirstFile))
    {
        goto done unless $this->read(%{$Params || {}}, _FileName => $FirstFile);
    }
    
    goto done unless $this->append_files($FileNames, $Params);

    $Success = 1;
  done:
    return($Success);
}

sub combine
{
    my $this            = shift;
    my ($that)          = @_;

    my $Success;

    ## Get a snapshot of field lists before any modifications.
    my $ThisFields      = $this->fieldlist();
    my $ThatFields      = $that->fieldlist();

    ## Bring in all (listed) fields from other table.
    my $IncomingFields  = $ThatFields;

    ## Preserve any previous non-selection and force one to be saved
    ## in the interim.  This will prevent $that->sel() from
    ## recalculating the selection each time if there is none.
    my $OldSel          = $that->{_Selection};
    $that->{_Selection} = $that->selection();   ## Might be a no-op
    
    ## Copy columns from $that in selection order; (re)place into $this
    foreach (@$IncomingFields) {$this->col_set($_, $that->sel($_))};
    
    ## Restore the possibly-undef selection in other table.
    $that->{_Selection} = $OldSel;              ## Might be a no-op
    
    ## Extend any short columns (whether originating from other table
    ## or from this one) to be the same length as all others.
    
    $this->extend();

    ## If either table had a custom fieldlist, then make a new custom
    ## field list which is the result of concatenating both field
    ## lists together, without duplicates, and of course preserving
    ## the original order as completely as possible (with the order
    ## given in the first table taking precedence).
    
    if (defined($this->{_FieldList}) || 
        defined($that->{_FieldList}))
    {
        ## Make a hash mapping field names from both tables to the
        ## order they should appear
        my $FieldOrderHash  = {};
        foreach (@$ThisFields, @$ThatFields) {$FieldOrderHash->{$_} ||= (keys %$FieldOrderHash) + 1};
        
        my $FieldList       = [sort {$FieldOrderHash->{$a} <=> $FieldOrderHash->{$b}} keys %$FieldOrderHash];

        $this->{_FieldList} = $FieldList;
    }       

    ## If either table had a custom sortorder, then make a new custom
    ## sort order which is the result of concatenating both orders
    ## together, without duplicates, and of course preserving the
    ## original order as completely as possible (with the order given
    ## in the first table taking precedence).
    
    if (defined($this->{_SortOrder}) || 
        defined($that->{_SortOrder}))
    {
        my $ThisOrder       = $this->sortorder();
        my $ThatOrder       = $that->sortorder();

        ## Make a hash mapping field names from both lists to the
        ## order they should appear

        my $OrderHash       = {};
        foreach (@$ThisOrder, @$ThatOrder) {$OrderHash->{$_} ||= (keys %$OrderHash) + 1};
        
        my $OrderList       = [sort {$OrderHash->{$a} <=> $OrderHash->{$b}} keys %$OrderHash];
        
        $this->{_SortOrder} = $OrderList;
    }       

    ## If either table had custom sortspecs, then create a new
    ## sortspecs hash by starting with all the entries from the other
    ## table and adding/overwriting with those from this table.

    if (defined($this->{_SortSpecs}) || 
        defined($that->{_SortSpecs}))
    {
        my $ThisSpecs       = $this->sortspecs();
        my $ThatSpecs       = $that->sortspecs();

        $this->{_SortSpecs} = {%$ThatSpecs, %$ThisSpecs};
    }

    ## If either table had custom sortroutines, then create a new
    ## sortroutines hash by starting with all the entries from the
    ## other table and adding/overwriting with those from this table.

    if (defined($this->{_SRoutines}) || 
        defined($that->{_SRoutines}))
    {
        my $ThisRoutines    = $this->{_SRoutines} || {};
        my $ThatRoutines    = $that->{_SRoutines} || {};

        $this->{_SRoutines} = {%$ThatRoutines, %$ThisRoutines};
    }

    ## All other settings are kept from $this and those from $that are
    ## ignored.

    $Success = 1;
done:
    return($Success);
}

sub combine_file
{
    my $this                = shift;
    my ($FileName, $Params) = @_;

    my $Success;

    ## $Params argument is optional.  If supplied, it must be a hash.
    $Params ||= {};

    ## Create a new empty table object of the same class as $this and
    ## read just the specified file into it.

    my $that                = ref($this)->new($Params, $FileName) or goto done;

    ## Combine the data from $that table into this one.

    $this->combine($that) or goto done;

    $Success = 1;
  done:
    return($Success);
}

sub combine_files
{
    my $this                    = shift;
    my ($FileNames, $Params)    = @_;
    
    my $Success;
    
    foreach my $FileName (@$FileNames) 
    {
        goto done unless $this->combine_file($FileName, $Params);
    }
    
    $Success = 1;
  done:
    return($Success);
}

sub join
{
    my $this                            = shift;
    my ($that, $Key1, $Key2, $Fields)   = @_;

    my $Success;

    ## $Key1 is required.
    $this->warn("Key1 is required for join()"), goto done
        unless ($Key1);

    ## $Key2 defaults to the same as $Key1.
    $Key2 ||= $Key1;
    
    ## The fields we'll be getting can optionally be overridden by
    ## caller; otherwise, they're the field list of the other table.
    my $IncomingFields  = $Fields || $that->fieldlist();
    
    ## Cull $Key1 and $Key2 from the incoming field list.
    $IncomingFields = [grep {($_ ne $Key1) && ($_ ne $Key2)} @$IncomingFields];
    
    ## Preserve any previous non-selection and force one to be saved
    ## in the interim.  This will prevent $that->sel() from
    ## recalculating the selection each time if there is none.
    my $OldSel          = $that->{_Selection};
    $that->{_Selection} = $that->selection();   ## Might be a no-op
    
    ## Make an index mapping values in $Key2 to record numbers in
    ## $that.  We reverse the order of insertion into the $Index
    ## because we want the items earliest in selection order to have
    ## precedence in case keys are not unique as they should be.
    
    my $Index = {}; @$Index{reverse @{$that->sel($Key2)}} = reverse @{$that->{_Selection}};

    ## Get a list of record numbers in $this that we'll be copying data into.
    my $Recs1   = $this->selection();

    ## Get a corresponding list of keys we're going to look up.
    my $Key1s   = $this->sel($Key1);

    ## The default "record number" in table 2 is $that->length()
    ## ... i.e. an invalid record number past the end of the table.
    ## This will ensure that failed lookups result in lookups to this
    ## illegal record number, correctly producing undef in the
    ## corresponding joined fields, whereas looking up "undef" would
    ## have produced record number zero.

    my $DefaultRecNum = $that->length();

    ## Look up @$Key1s in @$Index to get a list of data-source record
    ## numbers in $that.  Failed lookups map to $DefaultRecNum.
    
    my $Recs2   = [map {defined() ? $_ : $DefaultRecNum} @$Index{@$Key1s}];

    ## Copy data into selected positions within columns of $this, one
    ## column at a time, creating pre-sized columns in $this as
    ## necessary (col()).  These array slice operations are very, very
    ## fast.

    foreach my $Field (@$IncomingFields)
    {
        (@{$this->col($Field)}[@$Recs1] = ## Put values into selected records of $this
         @{$that->col($Field)}[@$Recs2]); ## Get values from looked-up records of $that
    }

    ## Restore the possibly-undef selection in other table.
    $that->{_Selection} = $OldSel;              ## Might be a no-op

    ## If this table had a custom fieldlist, then make a new custom
    ## field list which is the result of concatenating both field
    ## lists together, without duplicates, and of course preserving
    ## the original order as completely as possible (with the order
    ## given in the first table taking precedence).

    if (defined($this->{_FieldList}))
    {
        my $ThisFields      = $this->fieldlist();
        my $ThatFields      = $IncomingFields;

        ## Make a hash mapping field names from both tables to the
        ## order they should appear

        my $FieldOrderHash  = {};
        foreach (@$ThisFields, @$ThatFields) {$FieldOrderHash->{$_} ||= (keys %$FieldOrderHash) + 1};

        my $FieldList       = [sort {$FieldOrderHash->{$a} <=> $FieldOrderHash->{$b}} keys %$FieldOrderHash];
        
        $this->{_FieldList} = $FieldList;
    }       

    ## If either table had custom sortspecs, then create a new
    ## sortspecs hash by starting with all the entries from the other
    ## table and adding/overwriting with those from this table.

    if (defined($this->{_SortSpecs}) || 
        defined($that->{_SortSpecs}))
    {
        my $ThisSpecs       = $this->sortspecs();
        my $ThatSpecs       = $that->sortspecs();

        $this->{_SortSpecs} = {%$ThatSpecs, %$ThisSpecs};
    }

    ## If either table had custom sortroutines, then create a new
    ## sortroutines hash by starting with all the entries from the
    ## other table and adding/overwriting with those from this table.

    if (defined($this->{_SRoutines}) || 
        defined($that->{_SRoutines}))
    {
        my $ThisRoutines    = $this->{_SRoutines} || {};
        my $ThatRoutines    = $that->{_SRoutines} || {};

        $this->{_SRoutines} = {%$ThatRoutines, %$ThisRoutines};
    }

    ## All other settings are kept from $this and those from $that are
    ## ignored.

    $Success = 1;
done:
    return($Success);
}

sub join_file
{
    my $this                = shift;
    my ($FileName, $Params, $Key1, $Key2, $Fields)  = @_;

    my $Success;

    ## $Params argument may be undef.  If supplied, it must be a hash.
    $Params ||= {};

    ## Create a new empty table object of the same class as $this and
    ## read just the specified file into it.

    my $that                = ref($this)->new($Params, $FileName) or goto done;
    
    ## Join the data from $that table into this one.

    $this->join($that, $Key1, $Key2, $Fields) or goto done;

    $Success = 1;
  done:
    return($Success);
}

sub join_files
{
    my $this                    = shift;
    my ($FileNames, $Params, $Key1, $Key2, $Fields) = @_;
    
    my $Success;
    
    foreach my $FileName (@$FileNames) 
    {
        goto done unless $this->join_file($FileName, $Params, $Key1, $Key2, $Fields);
    }
    
    $Success = 1;
  done:
    return($Success);
}

=pod

=head1 INVERTING A TABLE'S ROWS/COLUMNS

    ## Re-orient table's data using vals from $ColName as field names...
    $t-invert($ColName)

Sometimes a situation gives you a table that's initially organized
with column data in rows, and field names in one of the columns, so
you need to flip the table in order to be able to work meaningfully
with it.

"Inverting" a table means to rewrite each row as a column. One row is
designated to be used as the field names.

For example, consider this table:

    F01    F02   F03   F04
    ------------------------
    First  Chris Agnes James
    Last   Bart  Marco Nixon
    Age    22    33    44

Calling invert() using field names from "F01"...

    $t->invert('F01');

... would change the table to look like this:
    
    First  Last  Age
    ----------------
    Chris  Bart  22
    Agnes  Marco 33
    James  Nixon 44

The field F01 which formerly contained the field names, is now gone,
and the remaining data columns have been converted from their old row
orientation into a column orientation.

=cut

sub invert
{
    my $this                    = shift;
    my ($HeaderField)           = @_;

    my $Success;
    
    ## Get new field names from an existing column and delete it at the same time.
    my $NewColNames             = $this->col_delete($HeaderField) or
        $this->warn("Invalid field name given to invert() method"), goto done;
    
    ## Get a hash of all existing (remaining) data columns.
    my $OldColNames             = $this->fieldlist_all();
    my $OldCols                 = $this->cols_hash($OldColNames);
    
    ## Make the new columns...
    my $NewCols = [map {$this->row_list($_, $OldColNames)} (0..$#$NewColNames)];
    
    ## Delete old columns from the object.
    delete @$this{@$OldColNames};
    
    ## Add new columns
    @$this{@$NewColNames}       = @$NewCols;

    ## Set the field name list...
    $this->{_FieldList}         = $NewColNames;

    $Success = 1;
  done:
    return($Success);
}

=pod 

=head1 PROGRESS MESSAGES

    ## Printing a progress message....
    
    $t->progress($Msg)       ## Print a message per current settings

    ## Progress settings applying to this object only...

    $t->progress_get()       ## Get current progress setting

    $t->progress_set(1)      ## Use progress_default() method
    $t->progress_set($Sub)   ## Set a custom progress routine
    $t->progress_set(0)      ## Disable progress
    $t->progress_set(undef)  ## Use class's settings (default)...

    ## Class's settings (for instances with _Progress == undef)

    $t->progress_class()     ## Get current setting.

    $t->progress_class(1)    ## Use progress_default() method
    $t->progress_class($Sub) ## Set shared custom prog routine
    $t->progress_class(0)    ## Disable class-default progress
    
    Data::CTable->progress_class(..) ## Call without an object

    ## Call builtin default progress method regardless of settings

    $t->progress_default($Msg) ## Default prog. routine for class

    ## Generate a warning (used internally by other methods)

    $t->warn($Msg)   ## In this class, calls progress_default()

    ## Timed progress: print msg to start, then at most once/2 sec

    $t->progress_timed($Op, $Item)  ## Re-print msg every 2 sec
    $t->progress_timed($Op, $Item, $Pos, $Tot) ##... with % readout
    $t->progress_timed($Op, $Item, $Pos, $Tot, $Wait) ## Not 1st x

    $t->progress_timed_default($Msg)  ## Called by progress_timed

Data::CTable is especially useful in creating batch-oriented
applications for processing data.  As such, routines that may perform
time-consuming tasks will, by default, generate helpful progress
messages.  The progress mechanism is highly customizable, however, to
suit the needs of applications that don't require this output, or that
require the output to go somewhere other than STDERR or the console.

The default progress routine is one that prints a message with a
date/time stamp to STDERR if and only if STDERR is an interactive
terminal, and otherwise is silent.

You could write a custom progress routine that does something else or
something in addition (e.g. logs to a file or syslog).  The custom
routine could either be implemented by overriding the
progress_default() method in a subclass, or by calling progress_set()
in any instance.

The custom progress routine, if any, is stored in the _Progress
parameter of the object.  But use progress_set() and progress_get() to
access it.

The interface for your custom progress routine should be:

    sub MyProgress {my ($Obj, $Message) = @_; chomp $Message; .....}

In other words, the routine takes a single message which may or may
not have a trailing newline.  It should always chomp the newline if
present, and then do its business... which generally will include
printing or logging a message (usually with a newline added).

The default, built-in progress routine for Data::CTable is:

    sub progress_default
    {
        my ($this, $msg) = @_; 
        chomp $msg; 
    
        print STDERR (localtime() . " $msg\n") if -t STDERR;

        return(1);   ## Indicate progress actually completed
    }

Of course, you are free to call this method directly at any time, and
it will do its thing regardless of other progress-enabling settings.
But the preferred way is to first set the settings and then call
progress().

The warn() method always calls progress_default() -- i.e. warnings
will display even if progress is otherwise disabled or overridden at
the object or class level.  However, you could create a subclass that
changes warn()'s behavior if desired.  (For example, it could just
call perl's builtin warn function, or be even more forceful,
generating warnings even if STDERR is not a terminal, for example.)

The progress_set() method may be used to override the progress routine
for an individual object (set to 1/true for default behavior, or
0/undef/false to disable progress for that object entirely).

Call progress_class() to set similar values to control the global
default behavior (e.g. turning on/off default progress behavior for
all instances), but be cautious about using this approach in any
environment where other programs might be accessing the same loaded
class data, since the setting is stored in a class-owned global
($Data::CTable::DefaultProgress).

Manipulating the class-default settings is only recommended in batch
or shell-script environments, not in mod_perl Web applications where
the module stays loaded into the Perl environment across multiple
invocations, for example.

If you want a particular method (e.g. read() but not write()) to be
silent, you could make a subclass and could override that method with
an implementation that first disables progress, calls the SUPER::
method, and then restores the progress setting to its original
setting.

=head2 Timed progress

Timed progress is a way of printing periodically-recurring progress
messages about potentially time-consuming processes to the terminal.

For example, consider the following messages which might appear every
2 seconds during a lengthy read() operation:

    Reading... 0 (0%)
    Reading... 2000 (4%)
    ...
    Reading... 38000 (96%)
    Reading... 40000 (100%)

The progress_timed() method is called internally by potentially
time-consuming processes (read(), write(), and sort()), and you may
want to call it yourself from your own scripts, to produce
weary-programmer-soothing visual output during otherwise
panic-producing long delays.

Generally, progress_timed() is called with the $Wait parameter set to
true, which delays the display of any messages until 2 seconds have
passed, so no messages will be displayed unless the process actually
does end up being slower than 2 seconds.

Parameters are:

    $Op    The string that identifies the "operation" taking place
    $Item  A milestone such as a number or datum to indicate progress
    $Pos   A numeric position against the (maybe estimated) baseline
    $Tot   The baseline.  If estimated, don't re-estimate too often
    $Wait  If true, skip printing the first message for this $Op

All parameters except $Op are optional.

progress_timed() has a throttle that keeps it from re-triggering more
often than every 2 seconds for any given sequence of the same $Op.
The clock is restarted each time you call it with a different $Op or
$Tot from the previous call (on the assumption that if the operation
or the baseline changes then that fact should be noted).

The messages printed will start with "$Op... ".

If you supply $Item, which could be a number or a string, the messages
will then show the $Item after the $Op.

If you supply BOTH $Pos and $Tot, then a percentage will be calculated
and added to the readout; otherwise omitted.

If you supply $Wait, the first message (only) that uses this $Op will
be skipped, and the next one won't appear for at least 2 seconds.

If using $Pos and $Tot to display percentages for your user, be sure
to call progress_timed() one final time when $Pos == $Tot so your user
sees the satisfying 100% milestone.  This "completion" call will not
be skipped even if 2 seconds have not passed since the previous timed
progress message was printed.

Althought progress_timed() is designed to cut down on too much visual
output when called often in a tight loop, remember that it still takes
some processing time to call it and so if you call it too frequently,
you're slowing down the very loop you wish were running faster.

So, you might want to call it every tenth or 100th or even 1000th time
through a tight loop, instead of every time through, using the mod (%)
operator:

    $t->progress_timed(....) if ($LoopCount % 100) == 0;

progress_timed_default() is the method called internally by
progress_timed() to actually print the messages it has prepared.  In
this implementation, progress_timed_default() just calls
progress_default().  That is, it ignores all other progress-inhibiting
or -enhancing settings so delay-soothing messages will print on the
terminal even if other messages are turned off.

This is because the author assumes that even if you don't want all
those other progress messages, you might still want these ones that
explain long delays.  If you REALLY don't, then just make yourself a
lightweight subclass where progress_timed_default() is a no-op, or
maybe calls regular progress().  For example:

    BEGIN {package Data::CTable::Silent; use vars qw(@ISA); 
           @ISA=qw(Data::CTable); sub progress_timed_default{}}

    ## Later...
    my $t = Data::CTable::Silent->new(...);


=cut

$Data::CTable::DefaultProgress = 1;

sub progress_set
{
    my $this            = shift;
    my ($ProgSetting)   = @_;
    
    $this->{_Progress} = $ProgSetting;
}

sub progress_class
{
    my $Ignored         = shift;
    my ($ProgSetting)   = @_;

    ## Set if specified...
    $Data::CTable::DefaultProgress = $ProgSetting if defined($ProgSetting);

    ## Return..
    return($Data::CTable::DefaultProgress);
}

sub progress_get
{
    my $this            = shift;
    
    my $ProgSetting = $this->{_Progress};

    return($ProgSetting);
}

sub progress_default
{   
    my ($this, $msg)    = @_; 
    chomp $msg; 
    
    print STDERR (localtime() . " $msg\n") if -t STDERR;

    return(1);  ## Indicate progress actually completed
}

sub progress
{
    my $this        = shift;
    my ($msg)       = @_;
    
    my $Prog1       = $this->{_Progress};                       ## First check object's progress setting
    my $Prog2       = $this->progress_class();                  ## Then check class's setting

    ## Calling regular progress resets the timers & ops in
    ## progress_timed...

    delete $this->{_ProgTimeInfo};

    ## First examine object setting to find a progress routine...

    return(&$Prog1($this, $msg))            if ref($Prog1) eq 'CODE';       ## Code ref: return it.
    return($this->progress_default($msg))   if $Prog1;                      ## true: use default progress.
    return(undef)                           if defined($Prog1);             ## false but defined: no progress.
    
    ## undef: fall through to class settings...
    
    return(&$Prog2($this, $msg))            if ref($Prog2) eq 'CODE';       ## Code ref: return it.
    return($this->progress_default($msg))   if $Prog2;                      ## true: use default progress.
    return(undef);                                                          ## false/undef: no progress.
}

sub progress_timed
{
    my $this                            = shift;
    my ($Op, $Item, $Pos, $Tot, $Wait)  = @_;

    ## Get params from previous call if any.
    my ($LastOp, $LastItem, $LastPos, $LastTot, $LastTime) = @{$this->{_ProgTimeInfo} || []};

    ## print &Dumper([$Op, $Item, $Pos, $Tot, $Wait], [$LastOp, $LastItem, $LastPos, $LastTot, $LastTime]);

    ## Get elapsed time.
    my $Time                = time();
    my $Elapsed             = $Time - ($LastTime || $Time);

    ## We're on the "same" operation if the $Op name is the same and
    ## the total (baseline) is the same.  Otherwise treat as new op.

    my $SameOp              = (($Op eq $LastOp));
    my $SameOpAndTot        = ($SameOp            && ($Tot == $LastTot));
    my $Finished            = ($Tot && ($Pos == $Tot));

    ## We trigger a message to print if we've been on the same op for
    ## 2 seconds or more, OR this is a new op.

    my $Trigger             = (($SameOpAndTot && ($Elapsed >= 2)) || ## Yes if same op & time has passed...
                               ($SameOpAndTot &&  $Finished) ||      ## Yes if we're finished (100%).
                               !$SameOpAndTot);                      ## Yes if new op
    
    ## Quit now if nothing to do.
    goto done unless $Trigger;
    
    ## Otherwise print message and save details for next time around.
    my $Percent             = sprintf("(%2d\%)", int(($Pos * 100) / $Tot)) if (defined($Pos) && $Tot);

    ## If we've been asked to "wait", we skip actually printing the
    ## message this the first time, but act as if we did (starting the timer).
    
    my $RetVal              = $this->progress_timed_default("$Op... $Item $Percent") 
        unless (!$SameOp && $Wait);     ## Skip first-time message if $Wait.
    
    $this->{_ProgTimeInfo} = [$Op, $Item, $Pos, $Tot, $Time];
    
  done: 
    return($RetVal);
}

sub progress_timed_default
{
    my $this        = shift;
    my ($msg)       = @_;

    return($this->progress_default("$msg"));
}

sub warn
{
    my $this        = shift;
    my ($msg)       = @_;

    return($this->progress_default("WARNING: $msg"));
}

=pod

=head1 Rejecting or reporting on groups of records and continuing

Use utility methods omit_warn() and omit_note() to conditionally omit
some records from a table and warn (or "note") if any were affected.

Use select_extract() to do the same thing but without actually
removing the extracted records from the table, and restoring the
original selection before select_extract was called.

If you supply a file name as the 4th argument, the omitted records
will be extracted to a file for later reference.

If you supply a message prefix as the 5th argument, a string other
than "WARNING" or "Note" may be specified.

    # Reject with a progress message prefixed by "WARNING:"

    $t->omit_warn(FirstName => sub{!length($_)}, "First name is empty");

        Mon Aug 23 08:24:15 2004 WARNING: Omitting 2 of 78243 records (now 78241): First name is empty.

    # Reject with a progress message prefixed by "Note:", with output to a file

    $t->omit_note(FirstName => sub{!length($_)}, "First name is empty", "empty.names.txt");

        Mon Aug 23 08:24:15 2004 Note: Omitting 2 of 78243 records (now 78241): First name is empty.
        Mon Aug 23 08:24:15 2004 Writing bad.firstname.txt...
        Mon Aug 23 08:24:15 2004 Wrote   bad.firstname.txt.

    # Extract some items, leaving original selection intact

    $t->select_extract(FirstName => sub{!length($_)}, "First name is empty", "empty.names.txt");

        Mon Aug 23 08:24:15 2004 Note: Extracting 2 of 78243 records: First name is empty.

=cut

sub omit_warn
{
    my $this = shift;
    my ($SelectField, $SelectSub, $Message, $DebugFile, $MessagePrefix, $ExtractFieldList) = @_;

    $MessagePrefix = 'WARNING' if !defined($MessagePrefix);

    $this->extract_with_message($SelectField, $SelectSub, $Message, $DebugFile, $MessagePrefix, $ExtractFieldList, 'DoOmit');
}

sub omit_note
{
    my $this = shift;
    my ($SelectField, $SelectSub, $Message, $DebugFile, $MessagePrefix, $ExtractFieldList) = @_;

    $MessagePrefix = 'Note' if !defined($MessagePrefix);

    $this->extract_with_message($SelectField, $SelectSub, $Message, $DebugFile, $MessagePrefix, $ExtractFieldList, 'DoOmit');
}

sub select_extract
{
    my $this = shift;
    my ($SelectField, $SelectSub, $Message, $DebugFile, $MessagePrefix, $ExtractFieldList) = @_;

    $MessagePrefix = 'Note' if !defined($MessagePrefix);

    $this->extract_with_message($SelectField, $SelectSub, $Message, $DebugFile, $MessagePrefix, $ExtractFieldList, !'DoOmit');
}

sub extract_with_message
{
    my $this = shift;
    my ($SelectField, $SelectSub, $Message, $DebugFile, $MessagePrefix, $ExtractFieldList, $DoOmit) = @_;
    
    ## Find omittable items -- save and restore the selection.

    my $LengthBefore        = $this->sel_len();
    my $SelBefore           = $this->selection();

    $this->select($SelectField, $SelectSub);
    my $OmitCount           = $this->sel_len();
    my $SelOmitted          = [@{$this->selection()}];

    $this->selection($SelBefore);
    
    ## If we have some, perform the omit and report on the omitted ones.

    if ($OmitCount)
    {
        my $Sel             = $this->col_empty();       ## Start with empty mask (all entries undef).
        @$Sel[@$SelBefore]  = @$SelBefore;              ## Mask in those in the original selection.
        @$Sel[@$SelOmitted] = undef;                    ## Mask out those we found.
        my $NewSel          = [grep {defined} @$Sel];
        my $LengthAfter     = @$NewSel + 0;

        ## Only actually alter the table if requested.
        $this->{_Selection} =  $NewSel if $DoOmit;      ## The remaining ones are the new selection.
        
        $this->progress("$MessagePrefix: @{[$DoOmit ? 'Omitting' : 'Extracting']} @{[$OmitCount]} of @{[$LengthBefore]} records@{[$DoOmit ? qq{ (now $LengthAfter)} : '']}: $Message");
        $this->write(_FileName      => "$DebugFile", 
                     _FDelimiter    => "\t", 
                     _LineEnding    => undef,
                     _Selection     => $SelOmitted,
                     ($ExtractFieldList ? 
                      (_FieldList   => $ExtractFieldList) : ()),
                     ) if $DebugFile;
    }
    else
    {
        unlink $DebugFile;
    }
    
    return($OmitCount);
}

=pod

=head1 DEBUGGING / DUMPING

    ## Print some debugging output...

    $t->out()             ## Pretty-print $t using Data::ShowTable

    $t->dump()            ## Dump $t using Data::Dumper
    $t->dump($x, $y)      ## Dump anything else using Data::Dumper

    ## Print some debugging output and then die.

    die $t->out()         ## Same but die afterwards.

    die $t->dump()        ## Same but die afterwards.
    die $t->dump($x, $y)  ## Same but die afterwards.

These two methods can be very helpful in debugging your scripts.

The out() method, which has many options, is described in complete
detail in the section below titled "FORMATTED TABLES".  In short, it
prints a nicely-formatted diagram of $t, obeying the custom field list
if any and custom selection if any.

The dump() method uses the Data::Dumper module to call &Dumper() on
the table itself (by default) and prints the result to STDERR.  If you
specify any number of other values, those will be dumped instead using
a single call to &Dumper (rather than individually).

=head2 Optional module dependencies

These methods require the otherwise-optional modules shown here:

    out()       Data::ShowTable  
    dump()      Data::Dumper

You'll get a warning at runtime if you try to call either method
without the appropriate module installed on your system.

=cut

sub dump
{
    my $this        = shift;
    my (@Things)    = @_;

    ## Default is to dump the object.
    @Things = ($this) unless @Things;

    if ($HaveDumper)
    {
        print STDERR &Dumper(@Things);
    }
    else
    {
        carp("Data::Dumper module is not installed.  Can't dump.");
    }
    
    return(1);
}

=pod

=head1 MISCELLANEOUS UTILITY METHODS

The following utilities are methods of the Data::CTable object.  They
may be called directly by clients, subclassed, or used by subclass
implementations as needed.

    ## Get cache file path (all args optional: defaulted from $t)

    $f  = $t->prep_cache_file($FileName, $CacheExtension, $CacheSubDir)

    ## Verify all directories in a path, creating any needed ones.

    $ok = $t->verify_or_create_path($DirPath, $Sep)

    ## Testing readability / writeability of a proposed file

    $ok = $t->try_file_read ($Path); ## Opens for read; then closes
    $ok = $t->try_file_write($Path); ## Opens for write; then deletes

    ## Getting parameters from object with optional overrides

    $param = $t->getparam($Params, $Param)

prep_cache_file() is the internal method used by both read() and
write() to calculate the name of a cache file to be used for a given
$FileName.

It calculates the path to the cache file that corresponds to the given
$FileName (which may be a bare file name, a relative path, or a
partial path, as long as it obeys the current platform's path format
rules).  All arguments are optional and if absent (undef), will be
defaulted from the corresponding parameters in $t.

In addition to calculating the path and file name, it also prepends
the "current directory" path if there was no path.  Then it checks
that all directories mentioned in the path actually exist.  If not, it
fails.  Then, it checks that EITHER the file exists and is readable,
OR it does not exist but would be writeable in that directory.  If any
of these directory creations or file checks fails, then undef is
returned (and there would be no cache file).

You may call it with no arguments on a file that has been read() to
find the path to the cache file that may have been used and/or
created, if any.

You may call it with a file name that was written to, to see what the
corresponding written cache file would be.

For example:

    ## Get name of cache file used or created by read and delete it.

    $RCache = $t->prep_cache_file() and unlink($RCache);

    ## Cache on write() and get name of file and delete it.

    $Written = $t->write(_CacheOnWrite=>1, _FileName=>"Foo.txt");
    $WCache  = $t->prep_cache_file($Written) and unlink($WCache);

verify_or_create_path() is the internal routine used by read(),
write(), and the cache-writing logic, that makes sure a requested file
path exists (by creating it if necessary and possible) before any file
is written by this module.  

(If you don't like this module's tendency to try to create
directories, make yourself a subclass in which this routine simply
checks -d on its $Path argument and returns the result.)

It must be called with a full or partial path TO A DIRECTORY, NOT A
FILE.  You may supply $Sep, a platform-appropriate separator character
(which defaults correctly for the runtime platform if you don't).

Returns true if the path verification and/or creation ultimately
succeeded, false otherwise (meaning that, after this call, there is no
such directory on the system and so you should not try to write a file
there).

try_file_read() and try_file_write() are the internal methods called
by prep_cache_file() as well as by read() and write() to preflight
proposed file reading and writing locations.

try_file_read() opens a file for read and closes it again; returns
true if the open was possible.

try_file_write() opens a file for write and closes it again, deleting
it if successful.  Returns true if the open for write and the delete
were successful.  (Be aware that this call will actually delete any
existing file by this name.)  

The reason that failure to delete causes try_file_write() to fail is
that successful cacheing depends on the ability to delete cache files
as well as create them or write to them.  A file in a location that
couldn't be deleted will not be used for cacheing.

getparam() looks up a named parameter in a params hash if it exists
there, otherwise looks it up in the object, thereby allowing $Params
to shadow any parameters in $this.

This internal routine is used by any methods that allow overriding of
parameters in the object when using a named-parameter calling
interface.  It should be used by any subclasses that also wish to use
a named-parameter calling convention.  For example:

    my $this        = shift;
    my $Params      = (@_ == 1 ? {_FieldList => $_[0]} : {@_});

    my($FieldList, $Selection) = map {$this->getparam($Params, $_)} 
    qw(_FieldList  _Selection);

=cut

{}; ## Get emacs to indent correctly.

sub prep_cache_file
{
    my $this                                        = shift;
    my($FileName, $CacheExtension, $CacheSubDir)    = @_;
    
    my $Success;

    $FileName           ||= $this->{_FileName};
    $CacheExtension     ||= $this->{_CacheExtension};
    $CacheSubDir        ||= $this->{_CacheSubDir};

    ## Break the path into its parts...
    use File::Basename         qw(fileparse);
    my ($Basename, $Path, $Ext) = fileparse($FileName, '\.[^\.]+');
    
    ## Figure out what the path separator should be...
    my ($Sep, $Up, $Cur) = @{$this->path_info()}{qw(sep up cur)};

    ## FileDir is guaranteed to either be empty or have a trailing
    ## separator (see: man File::Basename).  If empty, we set it to
    ## $Cur (the current directory).

    my $FileDir         = (length($Path) ? $Path : $Cur);

    ## Ensure $CacheSubDir is either empty or has a trailing separator...

    $CacheSubDir        =~ s/([^\Q$Sep\E])$/$1$Sep/;

    ## Check whether it's an absolute path (and not really a "sub"-dir)
    my $Absolute        = &path_is_absolute($CacheSubDir);

    ## $CacheDir is $FileDir with $CacheSubDir appended.  $CacheSubDir
    ## may be empty, meaning $FileDir is to be used for the cache
    ## files.  But if it's an absolute path, it stands alone.

    my $CacheDir        = ($Absolute ? $CacheSubDir : "$FileDir$CacheSubDir");

    ## Now we need to make sure that CacheDir exists OR try to create
    ## it.  (Warning will have already happened if necessary.)
    goto done unless $this->verify_or_create_path($CacheDir, $Sep);

    ## Verify that the dir is writeable.
    $this->warn("Cache directory $CacheDir is read-only"), goto done 
        unless -r $CacheDir;

    ## Full path: note $CacheExtension and $Ext may be empty.
    my $CacheFilePath   = "$CacheDir$Basename$Ext$CacheExtension";

    ## If the cache path and the full path end up being the same
    ## (probably because the _CacheSubDir and _CacheExtension are both
    ## empty), we bail.  Obviously, we don't want to risk overwriting
    ## the original data with the cache (or trying to read cache data
    ## from a text file).

    $this->warn("Can't cache $FileName without either _CacheSubDir or _CacheExtension"), goto done 
        if $CacheFilePath eq $FileName;
    
    ## Pre-flight the cache file: ensure we can either read it or
    ## touch and then delete it.
    
    $this->warn("Cache file $CacheFilePath cannot be created/overwritten: $!"), goto done 
        unless ($this->try_file_read ($CacheFilePath) || 
                $this->try_file_write($CacheFilePath));
    
    $Success = 1;
  done:
    return($Success ? $CacheFilePath : undef);
}

sub verify_or_create_path
{
    my $this        = shift;
    my ($Dir, $Sep) = @_;       

    ## Get default value for $Sep if not supplied.
    $Sep            ||= ${$this->path_info()}{sep};

    ## $Dir might end in $Sep; split strips trailing one if so.
    my $Parts = [split(/\Q$Sep\E/, $Dir)];  

    my $WholePath = "";
    foreach (@$Parts)
    {
        $WholePath = "$WholePath$_$Sep";
        next if -d $WholePath;

        ## Directory does not exist.  We need to make it.

        ## mkdir($WholePath, 0777) or
        ##    $this->warn("Failed to create directory '$WholePath': $!"), last;
        
        ## On some platforms (e.g. Darwin), perl's mkdir fails if
        ## there's a trailing separator.  Others tolerate its absence,
        ## so we remove it.
        
        (my $TryDir = $WholePath) =~ s/\Q$Sep\E$//;
        
        mkdir($TryDir, 0777) or
            $this->warn("Failed to create directory '$TryDir': $!"), last;
    }    

    return(-d $Dir);
}

sub try_file_write  ## like a "touch" but deletes the file if it succeeds.
{
    my $this        = shift;
    my ($Path)      = @_;       

    my $Success;

    ## Try creating it.
    use        IO::File;
    my $File = IO::File->new(">$Path");
    
    ## Created: close and delete it.
    $File->close(), unlink($Path) if $File;

    ## Failed: bail.
    goto done unless $File;

    ## If we couldn't unlink, fail.  The ability to delete a failed or
    ## part-written cache file is a critical part of cacheing.

    goto done if -e $Path;

    $Success = 1;
  done:
    return($Success);
}

sub try_file_read   ## Verifies that a file exists / can be read...
{
    my $this        = shift;
    my ($Path)      = @_;       

    my $Success;

    ## Try opening it.
    use        IO::File; 
    my $File = IO::File->new("<$Path") or goto done;

    $Success = 1;
  done:
    return($Success);
}

sub getparam
{
    my $this = shift;
    my ($Params, $Param) = @_;

    return(exists($Params->{$Param}) ? 
           (      $Params->{$Param}) : 
           (        $this->{$Param})  );
}



=pod

=head1 GENERAL-PURPOSE UTILITY FUNCTIONS

These general-purpose utility routines are defined in the Data::CTable
module but are not method calls.  You may optionally import them or
call them by their fully-qualified name.

    use Data::CTable qw(
                        guess_endings
                        guess_delimiter
                        path_info
                        path_is_absolute
                        min
                        max
                        );

    ## File-format guessing

    my $E = &guess_endings($IOHandle) ## Guess txt file line endings
    my $D = &guess_delimiter($String) ## Tab if found, else comma

    ## Cross-platform file path analysis

    my $Info = path_info();   ## Hash: 3 of platform's path values:
    my ($Sep,                 ## ... path separator (  / on Unix)
        $Up,                  ## ... "up" component (../ on Unix)
        $Cur) =               ## ... curr. dir path ( ./ on Unix)
    @$Info{qw(sep up cur)};

    my $Abs = path_is_absolute($Path)  ## Check path type

    ## Our old favorites min and max

    $x = max($x, 0);          ## Should have been part of Perl...
    $x = min($x, 100);

guess_endings() tries to figure out whether an open IO::File handle
has DOS, Mac, or Unix file endings.  It reads successively large
blocks of the file until it either finds evidence of at least two
separate line endings (of any type, but presumably they are the same),
or until it reaches the end of the file.  Then, it takes the resulting
block and searches for the first qualifying line ending sequence it
finds, if any.  This sequence is then returned to the caller.  If it
returns undef, it was not able to find any evidence of line endings in
the file.

guess_delimiter() takes a string buffer and returns a "," unless it
finds a tab character before the first comma in the $String, if any,
in which case a tab is returned.

path_info() returns a hash of three helpful strings for building and
parsing paths on the current platform.  Knows about Mac, Dos/Win, and
otherwise defaults to Unix.

path_is_absolute($Path) returns true if it thinks the given path
string is an absolute path on the current platform.

=cut
    
{}; ## Get emacs to indent correctly.

sub guess_endings
{
    my ($File)          = @_;

    my $Ending          = undef;

    my $ReadCount       = 0;
    my $BlockSize       = 512;

    my $Buf;
    my $Actual;

    while ($File->seek(0, 0), $Actual = $File->read($Buf, ($BlockSize * ++$ReadCount)))
    {
        ## Break out of the loop if it appears a line ending match is
        ## found (but disallow initial match at very end of buffer).
        
        last if $Buf =~ /((?:\x0D\x0A)|(?:\x0D)|(?:\x0A))[^\x0D\x0A]/;
        
        ## Break out of the loop if we just read any less than
        ## attempted (we are probably at the end of a very short,
        ## maybe one-line or even zero-line, file).
        
        last if $Actual < ($BlockSize * $ReadCount);
    }

    ## We can presume that the buffer we now have must either have
    ## line endings in it, or there is no line ending in the file at
    ## all.  So we extract the first one we come to, (trying the DOS
    ## ending first since it contains the other two), if any, and we
    ## return it.

    my $Ending = ($Buf =~ /((\x0D\x0A)|(\x0D)|(\x0A))/)[0];
    
    ## &progress_default(undef, "DOS  line endings") if $2; ## Debugging.
    ## &progress_default(undef, "Mac  line endings") if $3; ## Debugging.
    ## &progress_default(undef, "Unix line endings") if $4; ## Debugging.

  done:

    ## We always seek back to zero when done.
    $File->seek(0, 0);

    return($Ending);
}

sub guess_delimiter
{
    my ($String)        = @_;
    
    return(($String =~ /([,\t])/)[0] || ",");
}

sub path_info
{
    use Config      qw(%Config);
    my $OSName      =  $Config{osname};
    
    return({sep =>':' , up =>'::'  , cur =>':'  })  if $OSName =~ /mac                  /ix;
    return({sep =>'\\', up =>'..\\', cur =>'.\\'})  if $OSName =~ /(?<!dar)((win)|(dos))/ix;
    return({sep =>'/' , up =>'../' , cur =>'./' })                                         ;
}

sub path_is_absolute
{
    my ($Path)          = @_;

    use Config      qw(%Config);
    my $OSName      =  $Config{osname};
    
    return($Path =~ /^[^:]/)                        if $OSName =~ /mac                  /ix;
    return($Path =~ /^(([a-z][:])|(\\\\))/i)        if $OSName =~ /(?<!dar)((win)|(dos))/ix;
    return($Path =~ /^\//)                                                                 ;
}

### min and max <nostalgic sigh>

sub min {return($_[0] < $_[1] ? $_[0] : $_[1])}
sub max {return($_[0] > $_[1] ? $_[0] : $_[1])}


=pod

=head1 IMPLEMENTATION LIMITATIONS

=over 4

=item Column (field) names must not start with underscore

This object is implemented as a blessed hash reference.  By
convention, keys that do not start with underscore are data columns
and the key is the field name.  Keys that do start with underscore
refer to parameters or other data structures stored in the object.

Consequently, no field names may start with underscore.  When a file
is read from disk, any field names that DO start with underscores will
have the leading underscores stripped off.  Strange things could then
occur if the field names are then no longer unique.  For example,
field "A" and "_A" in the data file would be treated as the single
field "A" after the file was read.

=item Field values are always read as strings

Field values when written to a file are necessarily converted to
strings.  When read back in, they are read as strings, regardless of
original format.  The sole exception is the empty string which is read
back in as undef for efficiency.

An exception is when the _CacheOnWrite feature is used: field values
stored internally as integers or other scalar types may be saved and
later restored as such.  However, you should not rely on this
behavior.

=item Undef vs. empty

Empty field values are stored as "undef" for efficiency.  This means
that programs should generally not rely on any differences between ""
and undef in field values.  However, when working with large but
sparse tables, programs should take care not to convert undef values
to empty strings unnecessarily since the separate string objects
consume considerably more memory than undef.

=back

=head1 CONTRIBUTIONS

Corrections, bug reports, bug fixes, or feature additions are
encouraged.  Please send additions or patches with a clear explanation
of their purpose.  Consider making additions in the form of a subclass
if possible.

I'm committed to bundling useful subclasses contributed by myself or
others with this main distribution.

So, if you've got a subclass of Data::CTable (which should have a name
like Data::CTable::YourClassName) and you would like it included in
the main distribution, please send it along with a test script and
I'll review the code and add it (at my discretion).

If you've got a module that uses, augments, or complements this one,
let me know that, too, and I'll make appropriate mention of it.

=head1 SEE ALSO

The Data::CTable home page:

    http://christhorman.com/projects/perl/Data-CTable/

The implementation in CTable.pm.

The test.pl script, other subclasses, and examples.

The Data::ShowTable module.

The Data::Table module by Yingyao Zhou & Guangzhou Zou.

The perlref manual page.

=head1 AUTHOR

Chris Thorman <chthorman@cpan.org>

Copyright (c) 1995-2002 Chris Thorman.  All rights reserved.  

This program is free software; you can redistribute it and/or modify
it under the same terms as Perl itself.

=cut

1;

