#!/usr/bin/perl

# run all tests
use TAP::Harness;

my $harness = TAP::Harness->new(  );
my @tests = qw( 
	CaseIDs.t
	ConvertFirehoseData.t
	CreateDataFiles.t
	CustomizeFirehoseData.t
	FirehoseFileMetadata.t
	FirehoseTransformationWorkflow.t
	GeneIdentifiers.t
	LoadCGDSdata.t
	Utilities.t
);
$harness->runtests(@tests);