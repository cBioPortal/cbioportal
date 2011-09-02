use ExtUtils::MakeMaker;
# See lib/ExtUtils/MakeMaker.pm for details of how to influence
# the contents of the Makefile that is written.

use strict;

my $Top = 'Data';
my $Mod = 'CTable';

my $Programs_To_Install		= [qw(getweather tls tshow)];

WriteMakefile(
			  NAME			=> "${Top}::${Mod}",
			  DISTNAME		=> "${Top}-${Mod}",
			  VERSION_FROM	=> "$Mod.pm",
			  ABSTRACT_FROM	=> "$Mod.pm",
			  EXE_FILES     => [ map {"eg/$_"} @$Programs_To_Install ],
			  PREREQ_PM		=> {
				  ## 'Storable'			=> 0, 
				  ## 'Carp'				=> 0, 
				  ## 'IO::File'			=> 0, 
				  ## 'File::Basename'	=> 0,
				  },
			  PMLIBDIRS		=> ["$Mod"],
			  clean			=> {FILES => ".AppleDouble test/.AppleDouble test/cache/* test/cache/ test/*.out* *.bak $Mod.html"},
			  );

## This creates *.html from *.pm and does some cleanup by calling
## fix_html.perl (which is so-named because if it were called
## fix_html.pl it would get grabbed and installed by MakeMaker).

## Also creates the README as the text-format perldoc output from the
## pod in the main module's source file.

sub MY::htmlifypods
{
	"	
htmlifypods::
	pod2html --noindex --css=/stylesheet/ --title=$Top::$Mod --infile=$Mod.pm --outfile=$Mod.html
	perl fix_html.perl $Mod.html

	pod2text $Mod.pm > README

";
};

