#!/usr/bin/perl

use Fcntl qw/:seek/;
use Getopt::Long;

my ($CancerFile, $LatestRunFile, $ConverterSummaryFile);
my ($y, $m, $d) = (localtime)[5,4,3];

$y += 1900;
$m += 1;

$prad_mskcc_link = "<a href=\"http:\/\/www.cell.com/cancer-cell/fulltext/S1535-6108(10)00238-2\"><b>Prostate Cancer<\/b> (MSKCC)<\/a>";
$sarc_mskcc_link = "<a href=\"http:\/\/www.nature.com/ng/journal/vaop/ncurrent/full/ng.619.html\"><b>Sarcoma<\/b> (MSKCC/Broad)<\/a>";
$gbm_tcga_link = "<a href=\"http:\/\/www.nature.com/nature/journal/v455/n7216/full/nature07385.html\"><b>Glioblastoma<\/b> (TCGA)<\/a>";
$ov_tcga_link = "<a href=\"http:\/\/www.nature.com/nature/journal/v474/n7353/full/nature10166.html\"><b>Ovarian Cancer<\/b> (TCGA)<\/a>";
#$coadread_tcga_data = 

$pie_data = "";

# Get Args
GetOptions("CancerFile=s" => \$CancerFile,
		   "LatestRunFile=s" => \$LatestRunFile,
		   "ConverterSummaryFile=s" => \$ConverterSummaryFile);

foreach my $arg (qw(CancerFile LatestRunFile ConverterSummaryFile)) {
  if (!eval 'defined($' . $arg . ')') {
	print STDERR "Error: $arg is required\n";
	print STDERR "usage: public_data_table.pl --CancerFile <cancer list> --LatestRunFile <firehose LATEST_RUN> --ConverterSummaryFile <convertFirehoseData.out>\n";
	exit(1);
  }
}

open (IN1, $CancerFile);
while ($line = <IN1>) {
	chomp $line;
	@data = split (/ \: /,$line);
	$data[0] =~ tr/a-z/A-Z/;
	$cancers{$data[0]} = $data[1];
#	print "$data[0]\t$data[1]\n";
}


open (IN2, $LatestRunFile);
<IN2>;
$line = <IN2>;
chomp $line;
@data=split(/00$/,$line);
$date=$data[0];

open (IN3, $ConverterSummaryFile);

open (OUT1,">data_sets_public.html");
open (OUT2,">data_sets_public_right_column.markdown");

# compute cancer count from summary file, not cancer file (becauese cancer file does not take into account multiple studies per tumor type
my $cancer_count = -1; # first line in file is summary, start at -1
while ($line = <IN3>) {
    $cancer_count++;
}
# reset file pointer to start for processing below
seek IN3, 0, SEEK_SET

print OUT1 "<P><p>The portal currently contains data from $cancer_count cancer genomics studies. The table below lists the number of available samples per cancer study and data type.<br><br>\n";
#print OUT1 "<table border=\"1\" cellpadding=\"2\" cellspacing=\"0\" bordercolor=\"#808080\">\n";
print OUT1 "<table>\n";
print OUT1 "\t<tr>\n";
print OUT1 "\t\t<th>Cancer Study</th>\n";
print OUT1 "\t\t<th>All</th>\n";
print OUT1 "\t\t<th>Sequenced</th>\n";
print OUT1 "\t\t<th>aCGH</th>\n";
print OUT1 "\t\t<th>Tumor mRNA (RNA-Seq)</th>\n";
print OUT1 "\t\t<th>Tumor mRNA (microarray)</th>\n";
print OUT1 "\t\t<th>Normal mRNA</th>\n";
print OUT1 "\t\t<th>Tumor miRNA</th>\n";
print OUT1 "\t\t<th>Methylation</th>\n";
print OUT1 "\t\t<th>RPPA</th>\n";
print OUT1 "\t\t<th>Complete</th>\n";
print OUT1 "\t</tr>\n\n";

# print OUT2 "<table width=150>\n";
# print OUT2 "<tr>\n";
# print OUT2 "<td><b>Cancer</b></th>\n";
# print OUT2 "<td align=right><b>Cases</b></th>\n";
# print OUT2 "</tr>\n\n";


<IN3>;
$line_ct = 0;
$all_count = 0;

while ($line = <IN3>) {

	chomp $line;
	@data = split (/\t/,$line);
	$id = $data[0];
	$data[0] =~ tr/a-z/A-Z/;
	@study = split (/_/,$data[0]);

	unless ($data[11]==0) {
      if ($line_ct++ % 2) {
        print OUT1 "\t<tr>\n";
      }
      else {
		print OUT1 "\t<tr class=\"rowcolor\">\n";
      }
#		print OUT1 "\t\t<td><b>$study[0] ($study[1])</b></td>\n";
		if ($data[0] eq "PRAD_MSKCC") { print OUT1 "\t\t<td style=\"text-align: left;\">$prad_mskcc_link"; }
		elsif ($data[0] eq "SARC_MSKCC") { print OUT1 "\t\t<td style=\"text-align: left;\">$sarc_mskcc_link"; }
		elsif ($data[0] eq "OV_TCGA") { print OUT1 "\t\t<td style=\"text-align: left;\">$ov_tcga_link"; }
		elsif ($data[0] eq "GBM_TCGA") { print OUT1 "\t\t<td style=\"text-align: left;\">$gbm_tcga_link"; }
		else { print OUT1 "\t\t<td style=\"text-align: left;\"><b>$cancers{$study[0]} ($study[1])</b></td>\n"; }
		$pie_data .= "['$cancers{$study[0]} ($study[1])', $data[11]],\n";
		print OUT1 "\t\t<td style=\"text-align: center;\"><b>$data[11]</b></td>\n";
		if ($data[0] eq "PRAD_MSKCC") { print OUT1 "\t\t<td style=\"text-align: center;\">91</td>\n"; }
		elsif ($data[0] eq "SARC_MSKCC") { print OUT1 "\t\t<td style=\"text-align: center;\">207</td>\n"; }
		else { print OUT1 "\t\t<td style=\"text-align: center;\">$data[8]</td>\n"; }
		print OUT1 "\t\t<td style=\"text-align: center;\"><a href=\"http:\/\/cbio.mskcc.org/cancergenomics/public-portal/seg/$id.seg\">$data[3]</a></td>\n";
		print OUT1 "\t\t<td style=\"text-align: center;\">$data[5]</td>\n";
		print OUT1 "\t\t<td style=\"text-align: center;\">$data[6]</td>\n";
		if ($data[0] eq "PRAD_MSKCC") { print OUT1 "\t\t<td style=\"text-align: center;\">30</td>\n"; }
		elsif ($data[0] eq "SARC_MSKCC") { print OUT1 "\t\t<td style=\"text-align: center;\">8</td>\n"; }
		else { print OUT1 "\t\t<td style=\"text-align: center;\">$data[9]</td>\n"; }
		print OUT1 "\t\t<td style=\"text-align: center;\">$data[10]</td>\n";
		print OUT1 "\t\t<td style=\"text-align: center;\">$data[2]</td>\n";
		print OUT1 "\t\t<td style=\"text-align: center;\">$data[1]</td>\n";
		if ($data[0] eq "PRAD_MSKCC") { print OUT1 "\t\t<td style=\"text-align: center;\"><b>85</b></td>\n"; }
		elsif ($data[0] eq "SARC_MSKCC") { print OUT1 "\t\t<td style=\"text-align: center;\"><b>149</b></td>\n"; }
		else { print OUT1 "\t\t<td style=\"text-align: center;\"><b>$data[7]</b></td>\n"; }
		print OUT1 "\t</tr>\n\n";
		$all_count = $all_count + $data[11];
	}
	
	unless ($data[11]==0) {
		# print OUT2 "<tr>\n";
		# print OUT2 "<td class=\"Tips1\" title=\"$cancers{$study[0]}\">$study[0] ($study[1])</td>\n";
		# print OUT2 "<td style=\"text-align: right;\">$data[11]</td>\n";
		# print OUT2 "</tr>\n";
	}
}

print OUT1 "</table>";
#print OUT1 "\n<br>Last update: $m/$d/$y<br>";

$date =~ /(\d\d\d\d)(\d\d)(\d\d)/;
$dateOut = "$2/$3/$1";

print OUT1 "<br>Total number of samples: <b>$all_count</b><br><br>Based on data from five published or submitted studies and the Broad Institute's TCGA Firehose run from $dateOut.</p>";

# Lop off the last comma (otherwise, chart will not load in Internet Explorer).
$pie_data = substr ($pie_data, 0, -2)."\n";

#print OUT2 "</table>\n";
print OUT2 "<P>The Portal contains data for <b>$all_count tumor samples from $cancer_count cancer studies.</b> [<a href='data_sets.jsp'>Details.</a>]</p>\n";
print OUT2 "<script type='text/javascript' src='https://www.google.com/jsapi'></script>\n";
print OUT2 "<script type='text/javascript'>\n";
print OUT2 "google.load('visualization', '1.0', {'packages':['corechart']});\n";
print OUT2 "google.setOnLoadCallback(drawChart);\n";
print OUT2 "function drawChart() {\n";
print OUT2 "var data = new google.visualization.DataTable();\n";
print OUT2 "data.addColumn('string', 'Cancer Study');\n";
print OUT2 "data.addColumn('number', 'Samples');\n";
printf OUT2 ("data.addRows([%s]);\n", $pie_data);
print OUT2 "var options = {\n";
print OUT2 "'backgroundColor':'#F1F6FE',\n";
print OUT2 "'is3D':false,\n";
print OUT2 "'pieSliceText':'value',\n";
print OUT2 "'tooltip':{'text':'value'},\n";
print OUT2 "'width':300,\n";
print OUT2 "'legend':{'position':'none'},\n";
print OUT2 "'left':0,'top':0,\n";
print OUT2 "'height':300};\n";
print OUT2 "var chart = new google.visualization.PieChart(document.getElementById('chart_div1'));\n";
print OUT2 "chart.draw(data, options);\n";
print OUT2 "}\n";
print OUT2 "</script>\n";
print OUT2 "<div id='chart_div1'></div>\n";
close (IN1);
close (IN2);
close (IN3);
close (OUT1);
close (OUT2);
#print $pie_data;


