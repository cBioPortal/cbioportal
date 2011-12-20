#!/usr/bin/perl

my ($y, $m, $d) = (localtime)[5,4,3];

$y += 1900;
$m += 1;

$prad_mskcc_link = "<a href=\"http:\/\/www.cell.com/cancer-cell/fulltext/S1535-6108(10)00238-2\"><b>Prostate Cancer<\/b> (MSKCC)<\/a>";
$sarc_mskcc_link = "<a href=\"http:\/\/www.nature.com/ng/journal/vaop/ncurrent/full/ng.619.html\"><b>Sarcoma<\/b> (MSKCC/Broad)<\/a>";
$gbm_tcga_link = "<a href=\"http:\/\/www.nature.com/nature/journal/v455/n7216/full/nature07385.html\"><b>Glioblastoma<\/b> (TCGA)<\/a>";
$ov_tcga_link = "<a href=\"http:\/\/www.nature.com/nature/journal/v474/n7353/full/nature10166.html\"><b>Ovarian Cancer<\/b> (TCGA)<\/a>";
#$coadread_tcga_data = 

open (IN1,"cancers.txt");

$cancer_count = 0;
while ($line = <IN1>) {
	$cancer_count++;
	chomp $line;
	@data = split (/ \: /,$line);
	$data[0] =~ tr/a-z/A-Z/;
	$cancers{$data[0]} = $data[1];
#	print "$data[0]\t$data[1]\n";
}


open (IN2,"LATEST_RUN.txt");
<IN2>;
$line = <IN2>;
chomp $line;
@data=split(/00/,$line);
$date=$data[0];

open (IN3,"convertFirehoseData.out");

open (OUT1,">data_sets_public.html");
open (OUT2,">data_sets_public_right_column.markdown");

print OUT1 "<P><p>The portal currently contains data from 18 cancer genomics studies. The table below lists the number of available samples per cancer study and data type.<br><br>\n";
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
print OUT1 "\t\t<th>Methylation</th>\n";
print OUT1 "\t\t<th>RPPA</th>\n";
print OUT1 "\t\t<th>Complete</th>\n";
print OUT1 "\t</tr>\n\n";

print OUT2 "<table width=150>\n";
print OUT2 "<tr>\n";
print OUT2 "<td><b>Cancer</b></th>\n";
print OUT2 "<td align=right><b>Cases</b></th>\n";
print OUT2 "</tr>\n\n";


<IN3>;
$line_ct = 0;
$all_count = 0;

while ($line = <IN3>) {

	chomp $line;
	@data = split (/\t/,$line);
	$data[0] =~ tr/a-z/A-Z/;
	@study = split (/_/,$data[0]);

	unless ($data[10]==0) {
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
		print OUT1 "\t\t<td style=\"text-align: center;\"><b>$data[10]</b></td>\n";
		if ($data[0] eq "PRAD_MSKCC") { print OUT1 "\t\t<td style=\"text-align: center;\">$91</td>\n"; }
		elsif ($data[0] eq "SARC_MSKCC") { print OUT1 "\t\t<td style=\"text-align: center;\">207</td>\n"; }
		else { print OUT1 "\t\t<td style=\"text-align: center;\">$data[8]</td>\n"; }
		print OUT1 "\t\t<td style=\"text-align: center;\">$data[3]</td>\n";
		print OUT1 "\t\t<td style=\"text-align: center;\">$data[5]</td>\n";
		print OUT1 "\t\t<td style=\"text-align: center;\">$data[6]</td>\n";
		if ($data[0] eq "PRAD_MSKCC") { print OUT1 "\t\t<td style=\"text-align: center;\">$30</td>\n"; }
		elsif ($data[0] eq "SARC_MSKCC") { print OUT1 "\t\t<td style=\"text-align: center;\">8</td>\n"; }
		else { print OUT1 "\t\t<td style=\"text-align: center;\">$data[9]</td>\n"; }
		print OUT1 "\t\t<td style=\"text-align: center;\">$data[2]</td>\n";
		print OUT1 "\t\t<td style=\"text-align: center;\">$data[1]</td>\n";
		print OUT1 "\t\t<td style=\"text-align: center;\"><b>$data[7]</b></td>\n";
		print OUT1 "\t</tr>\n\n";
		$all_count = $all_count + $data[10];
	}
	
	unless ($data[10]==0) {
		print OUT2 "<tr>\n";
		print OUT2 "<td class=\"Tips1\" title=\"$cancers{$study[0]}\">$study[0] ($study[1])</td>\n";
		print OUT2 "<td style=\"text-align: right;\">$data[10]</td>\n";
		print OUT2 "</tr>\n";
	}
}

print OUT1 "</table>";
#print OUT1 "\n<br>Last update: $m/$d/$y<br>";

$date =~ /(\d\d\d\d)(\d\d)(\d\d)/;
$dateOut = "$2/$3/$1";

print OUT1 "<br>Total number of samples: <b><a href=\"data_sets.jsp\">$all_count</a></b><br><br>Based on data from five published or submitted studies and the Firehose run from $dateOut.</p>";

print OUT2 "</table>\n";
#print OUT2 "\n<p>Last update: $m/$d/$y.<br><a href=\"data_sets.jsp\">More...</a></p>";
print OUT2 "Five published studies & the Firehose run from $dateOut.</p>";

close (IN1);
close (IN2);
close (IN3);
close (OUT1);
close (OUT2);

