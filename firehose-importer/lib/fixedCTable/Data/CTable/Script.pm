#!/usr/bin/perl
## Emacs: -*- tab-width: 4; -*-

use strict;

package   Data::CTable::Script;	

use vars qw($VERSION);				$VERSION = '1.03';

=pod

=head1 NAME

Data::CTable::Script - CTable virtual subclass to support shell scripts

=head1 SYNOPSIS

	## Call from a shell script:
	use 	 Data::CTable::Script;
	exit	!Data::CTable::Script->script();

	## But more likely, you'll want to subclass first:
	use 	 Data::CTable::MyScript;
	exit	!Data::CTable::MyScript->script();

This is an OO implementation of the outermost structure and utlility
routines that would be needed by most any perl/shell script that wants
to use Data::CTable functionality.  

See Data::CTable::Lister for a sample subclass that uses this
superstructure to implement a command-line tool that makes a table
containing file listings and then lets the user manipulate it using
various command-line options and then output it in various interesting
ways.  

See Data::CTable for the superclass.

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

use       Data::CTable;  use vars qw(@ISA);
@ISA = qw(Data::CTable);

=pod 

=head1 METHODS

	$Class->usage()                     ## Don't subclass
	$Class->usage_message($ScriptName)  ## Subclass this

usage() figures out the name of the script being called and passes it
to usage_message (designed to be sublcassed), which can the print the
message including the name of the script.

=cut

sub usage
{
	my $this = shift;

	## This inserts actual name of tool into the documentation.
	use                        File::Basename;
	my $ScriptName = join('', (File::Basename::fileparse($0))[0,2]);

	return($this->usage_message($ScriptName));
}

sub usage_message
{
	my $this = shift;
	my ($ScriptName) = @_;

	return(do{(my $doc = << 'END') =~ s/_SCR_/$ScriptName/g; $doc}); 
	
_SCR_ [options]

This is an empty help message for the _SCR_ script.  Please subclass
this module and override the usage_message() method.
	
END
{};
}

=pod 

	$Class->optionspec()

Specification for command-line option parsing for the script.  Meant
to be subclassed.

Should return a hash mapping GetOpt::Long-style specifications to
default values.  This base class implementation returns the following
spec entries.  Subclasses could replace these entirely or add to them:

	## Common options
	"help"			=>	0 ,
	"verbose"		=>	0 ,
	
	## Which fields are included in output
	"fields=s"		=>	[],
	
	## Sorting
	"sort=s"		=>	[],
	
	## Output method
	"output=s"		=>	[],

In the above specs "=s" means a string argument, and [] means multiple
values are allowed and will be collected in an array, whose initial
contents are empty.  0 means the option defaults to off; a default of
foo => 1 would allow the --nofoo switch to turn off the foo option.

=cut
{};	

sub optionspec
{
	my $Class	= shift;

	my $Spec	= {(
					## Common options
					"help"			=>	0 ,
					"verbose"		=>	0 ,
					
					## Which fields are included in output
					"fields=s"		=>	[],
					
					## Sorting
					"sort=s"		=>	[],
					
					## Output method
					"output=s"		=>	[],
					)};
	
	return($Spec);
}

=pod
	
	$Class->script()

Class method: main entry point for the script.  Parses options,
presents usage(), instantiates an object and lets it do its work.
Returns a Boolean success value.  (A perl script should exit() the
opposite of this value: i.e. exit(0) means success.)

=cut

sub script
{
	my $Class			= shift;
	
	my $Success;

	my $OptSpec			= $Class->optionspec();
	
	my ($Opts, $Args)	= $Class->get_opts_hash(%$OptSpec);
	
	print ($Class->usage()), goto done if $Opts->{help};
	
	## Place all remaining arguments into the "args" option
	$Opts->{args}		= $Args;
	
	print $ {$Class->run($Opts)};
	
	$Success = 1;
  done:
	return($Success);
}


=pod
	
	$Class->run()

Main entry point for the script. Instantiates an object and lets it do
its work.  Returns a reference to a scalar which will be printed
before the script exits.  (Pass \ '' for no output).

=cut
{};	

sub run
{
	my $Class			= shift;
	my ($Opts)			= @_;
	
	use Data::CTable qw(path_info);
	
	## Create an empty options hash in case we didn't get one.
	$Opts	||= {};

	## Instantiate an object of this class.
	my $this = $Class->new({_Options => $Opts});
	
	## Do nothing in this base class.
	return(\ '');
}

=pod 

	$Class->get_opts_hash()

Internal method to process command-line options using GetOpt::Long and
a few enhancements, most importantly: any multi-valued field is
post-processed to treat any values separated by commas or spaces as
multiple values.

=cut

sub get_opts_hash
{
	my $Class			= shift;
	my (@Specs)			= @_;
	
	use Getopt::Long qw(GetOptions);
	
	my $Opts	= {};
	my $mkspec	= sub 
	{
		my ($Spec, $Default) = @_;
		my ($Opt  ) = ($Spec =~ /(\w+)/)[0];
		$Opts->{$Opt} = $Default;
		($Spec => (ref($Opts->{$Opt}) ?  $Opts->{$Opt} : \ $Opts->{$Opt}));
	};
	
	## Extract all arguments that seem to be GetOpt-style arguments.
	GetOptions(map {&$mkspec(@Specs[($_*2),($_*2)+1])} (0..int($#Specs/2)));
	
	## Allow commas and/or spaces to separate values in any
	## multi-valued options. (Not tabs -- we might want to accept a
	## tab as a valid input character.)

	## This goes a bit beyond the customary Getopt::Long paradigm, but
	## is convenient since it allows something like -f=f1,f2,f3 -f=f4

	foreach (grep {ref $Opts->{$_} eq 'ARRAY'} keys %$Opts) 
	{$Opts->{$_} = [map {split(/[ ,]+/)} @{$Opts->{$_}}]};
	
	## Get any remaining arguments.
	my $Args = [@ARGV];
	
	## Debugging
	## use Data::Dumper; print &Dumper($Opts, $Args);

	return($Opts, $Args);
}

1;
