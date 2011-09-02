#!/usr/bin/perl
## Emacs: -*- tab-width: 4; -*-

use strict;

package Data::CTable::ProgressLogger;

use vars qw($VERSION);				$VERSION = '1.03';

=pod

=head1 NAME

Data::CTable::ProgressLogger - CTable that stores messages in the object

=head1 SYNOPSIS

	my $Table = Data::CTable::ProgressLogger->new("mydata.txt");
	# ... do stuff...
	$Table->write();
	$Table->show_log();

=head1 OVERVIEW

ProgressLogger is a subclass of Data::CTable.

The only difference is that it enables per-instance progress by
defaul, but it stores progress messages in the object instead of
sending them to STDERR.

Later, they can be gotten in an array by calling the log() method
or dumped with show_log().

=cut

use                        Data::CTable;
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

sub log
{
	my $this            = shift;

	return($this->{_ProgrLog});
}

sub show_log		 ## Use Dumper to spit out the log list
{
	my $this            = shift;
	$this->dump($this->log());
}

1;
