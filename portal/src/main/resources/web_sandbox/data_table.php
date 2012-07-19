<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01//EN" "http://www.w3.org/TR/html4/strict.dtd">
<html>
	<head>
		<meta http-equiv="content-type" content="text/html; charset=utf-8" />
		<link rel="shortcut icon" type="image/ico" href="http://www.datatables.net/favicon.ico" />
		
		<title>DataTables example</title>
		<style type="text/css" title="currentStyle">
			@import "css/data_table.css";
			@import "css/data_table_ColVis.css"
		</style>
		<script type="text/javascript" language="javascript" src="js/jquery-1.4.2.min.js"></script>

		<script type="text/javascript" language="javascript" src="js/jquery.dataTables.min.js"></script>
		<script type="text/javascript" charset="utf-8">
			$(document).ready(function() {
				$('#example').dataTable();
			} );
		</script>
	</head>
	<body id="dt_example">
		<div id="container">
			<div class="full_width big">
				DataTables zero configuration example
			</div>

			
			<h1>Preamble</h1>
			<p>DataTables has most features enabled by default, so all you need to do to use it with one of your own tables is to call the construction function (as shown below).</p>
			
			<h1>Live example</h1>
			<div id="demo" style="font-size:65%; font-family:arial;">
<table cellpadding="0" cellspacing="0" border="0" class="display" id="example">
	<thead>	
	<tr>
	<th>Case ID</th>
	<th>Mutation Status</th>
	<th>Mutation Type</th>
	<th>Validation Status</th>
	<th>Sequencing Center</th>
	<th>Amino Acid Change</th>
	<th>Predicted Functional Impact**</th>
	<th>Alignment</th>
	<th>Structure</th>
	<th>Nucleotide Position *</th>
	<th>Notes</th>
	</tr>
	</thead>
	<tbody>
	<tr>
	<td>TCGA-04-1331</td>
	<td><span class='somatic'>Somatic</span></td>
	<td>Nonsense_Mutation</td>
	<td><span class='valid'>Valid</span></td>
	<td>Broad</td>
	<td>p.C711*</td>
	<td>&nbsp;</td>
	<td>&nbsp;</td>
	<td>&nbsp;</td>
	<td>2361</td>
	<td>&nbsp;</td>
	</tr>

	<tr>
	<td>TCGA-04-1336</td>
	<td><span class='germline'>Germline</span></td>
	<td>Frame_Shift_Del</td>
	<td><span class='valid'>Valid</span></td>
	<td>WashU</td>
	<td>p.T1738fs</td>
	<td>&nbsp;</td>
	<td>&nbsp;</td>
	<td>&nbsp;</td>
	<td>5442</td>
	<td>&nbsp;</td>
	</tr>

	<tr>
	<td>TCGA-04-1367</td>
	<td><span class='germline'>Germline</span></td>
	<td>Nonsense_Mutation</td>
	<td>Unknown</td>
	<td>Broad</td>
	<td>p.E294*</td>
	<td>&nbsp;</td>
	<td>&nbsp;</td>
	<td>&nbsp;</td>
	<td>1108</td>
	<td>&nbsp;</td>
	</tr>

	<tr>
	<td>TCGA-09-2050</td>
	<td><span class='somatic'>Somatic</span></td>
	<td>Nonsense_Mutation</td>
	<td><span class='valid'>Valid</span></td>
	<td>Broad</td>
	<td>p.S1882*</td>
	<td>&nbsp;</td>
	<td>&nbsp;</td>
	<td>&nbsp;</td>
	<td>5873</td>
	<td>&nbsp;</td>
	</tr>

	<tr>
	<td>TCGA-13-0726</td>
	<td><span class='germline'>Germline</span></td>
	<td>Nonsense_Mutation</td>
	<td><span class='valid'>Valid</span></td>
	<td>WashU</td>
	<td>p.R2394*</td>
	<td>&nbsp;</td>
	<td>&nbsp;</td>
	<td>&nbsp;</td>
	<td>7408</td>
	<td>&nbsp;</td>
	</tr>

	<tr>
	<td>TCGA-13-0792</td>
	<td><span class='somatic'>Somatic</span></td>
	<td>Missense_Mutation</td>
	<td><span class='valid'>Valid</span></td>
	<td>Baylor</td>
	<td>p.E1143D</td>
	<td><span class='oma_link oma_medium'><a href='omaRedirect.do?site=mutationassessor.org&cm=var&var=13,31809921,A,T&fts=all'>Medium</a></span></td>
	<td><a href='omaRedirect.do?site=mutationassessor.org/&cm=msa&ty=f&p=BRCA2_HUMAN&rb=1037&re=1211&var=E1143D'>Alignment</a></td>
	<td>&nbsp;</td>
	<td>3657</td>
	<td>&nbsp;</td>
	</tr>

	<tr>
	<td>TCGA-13-0793</td>
	<td><span class='germline'>Germline</span></td>
	<td>Frame_Shift_Del</td>
	<td>Unknown</td>
	<td>Baylor</td>
	<td>p.G3086fs</td>
	<td>&nbsp;</td>
	<td>&nbsp;</td>
	<td>&nbsp;</td>
	<td>-1</td>
	<td>&nbsp;</td>
	</tr>

	<tr>
	<td>TCGA-13-0885</td>
	<td><span class='somatic'>Somatic</span></td>
	<td>Frame_Shift_Del</td>
	<td><span class='valid'>Valid</span></td>
	<td>WashU</td>
	<td>p.K1406fs</td>
	<td>&nbsp;</td>
	<td>&nbsp;</td>
	<td>&nbsp;</td>
	<td>4447</td>
	<td>&nbsp;</td>
	</tr>

	<tr>
	<td>TCGA-13-0886</td>
	<td><span class='germline'>Germline</span></td>
	<td>Frame_Shift_Del</td>
	<td><span class='valid'>Valid</span></td>
	<td>Broad</td>
	<td>p.S1982fs</td>
	<td>&nbsp;</td>
	<td>&nbsp;</td>
	<td>&nbsp;</td>
	<td>6174</td>
	<td>Known BRCA2 6174delT founder mutation.</td>
	</tr>

	<tr>
	<td>TCGA-13-0890</td>
	<td><span class='somatic'>Somatic</span></td>
	<td>Frame_Shift_Del</td>
	<td><span class='valid'>Valid</span></td>
	<td>WashU</td>
	<td>p.S1230fs</td>
	<td>&nbsp;</td>
	<td>&nbsp;</td>
	<td>&nbsp;</td>
	<td>3914</td>
	<td>&nbsp;</td>
	</tr>

	<tr>
	<td>TCGA-13-0900</td>
	<td><span class='germline'>Germline</span></td>
	<td>Frame_Shift_Del</td>
	<td>Unknown</td>
	<td>Baylor</td>
	<td>p.N257fs</td>
	<td>&nbsp;</td>
	<td>&nbsp;</td>
	<td>&nbsp;</td>
	<td>999</td>
	<td>&nbsp;</td>
	</tr>

	<tr>
	<td>TCGA-13-0913</td>
	<td><span class='germline'>Germline</span></td>
	<td>Frame_Shift_Del</td>
	<td><span class='valid'>Valid</span></td>
	<td>Broad</td>
	<td>p.E1857fs</td>
	<td>&nbsp;</td>
	<td>&nbsp;</td>
	<td>&nbsp;</td>
	<td>5801</td>
	<td>&nbsp;</td>
	</tr>

	<tr>
	<td>TCGA-13-1481</td>
	<td><span class='somatic'>Somatic</span></td>
	<td>Frame_Shift_Del</td>
	<td><span class='valid'>Valid</span></td>
	<td>WashU</td>
	<td>p.S2697fs</td>
	<td>&nbsp;</td>
	<td>&nbsp;</td>
	<td>&nbsp;</td>
	<td>8330</td>
	<td>&nbsp;</td>
	</tr>

	<tr>
	<td>TCGA-13-1498</td>
	<td><span class='germline'>Germline</span></td>
	<td>Frame_Shift_Del</td>
	<td><span class='valid'>Valid</span></td>
	<td>WashU</td>
	<td>p.S1982fs</td>
	<td>&nbsp;</td>
	<td>&nbsp;</td>
	<td>&nbsp;</td>
	<td>6174</td>
	<td>Known BRCA2 6174delT founder mutation.</td>
	</tr>

	<tr>
	<td>TCGA-13-1499</td>
	<td><span class='germline'>Germline</span></td>
	<td>Frame_Shift_Del</td>
	<td><span class='valid'>Valid</span></td>
	<td>WashU</td>
	<td>p.S1982fs</td>
	<td>&nbsp;</td>
	<td>&nbsp;</td>
	<td>&nbsp;</td>
	<td>6174</td>
	<td>Known BRCA2 6174delT founder mutation.</td>
	</tr>

	<tr>
	<td>TCGA-13-1512</td>
	<td><span class='germline'>Germline</span></td>
	<td>Nonsense_Mutation</td>
	<td>Unknown</td>
	<td>Broad</td>
	<td>p.K3326*</td>
	<td>&nbsp;</td>
	<td>&nbsp;</td>
	<td>&nbsp;</td>
	<td>10204</td>
	<td>&nbsp;</td>
	</tr>

	<tr>
	<td>TCGA-23-1026</td>
	<td><span class='germline'>Germline</span></td>
	<td>Nonsense_Mutation</td>
	<td>Unknown</td>
	<td>Baylor</td>
	<td>p.K3326*</td>
	<td>&nbsp;</td>
	<td>&nbsp;</td>
	<td>&nbsp;</td>
	<td>10204</td>
	<td>&nbsp;</td>
	</tr>

	<tr>
	<td>TCGA-23-1030</td>
	<td><span class='somatic'>Somatic</span></td>
	<td>Missense_Mutation</td>
	<td><span class='valid'>Valid</span></td>
	<td>Broad</td>
	<td>p.T1354M</td>
	<td><span class='oma_link oma_neutral'><a href='omaRedirect.do?site=mutationassessor.org&cm=var&var=13,31810553,C,T&fts=all'>Neutral</a></span></td>
	<td><a href='omaRedirect.do?site=mutationassessor.org/&cm=msa&ty=f&p=BRCA2_HUMAN&rb=1259&re=1409&var=T1354M'>Alignment</a></td>
	<td>&nbsp;</td>
	<td>4289</td>
	<td>&nbsp;</td>
	</tr>

	<tr>
	<td>TCGA-23-1120</td>
	<td><span class='somatic'>Somatic</span></td>
	<td>Frame_Shift_Del</td>
	<td><span class='valid'>Valid</span></td>
	<td>Baylor</td>
	<td>p.P3278fs</td>
	<td>&nbsp;</td>
	<td>&nbsp;</td>
	<td>&nbsp;</td>
	<td>10059</td>
	<td>&nbsp;</td>
	</tr>

	<tr>
	<td>TCGA-24-0975</td>
	<td><span class='germline'>Germline</span></td>
	<td>Splice_Site</td>
	<td>Unknown</td>
	<td>Broad</td>
	<td>e6+2</td>
	<td>&nbsp;</td>
	<td>&nbsp;</td>
	<td>&nbsp;</td>
	<td>-1</td>
	<td>&nbsp;</td>
	</tr>

	<tr>
	<td>TCGA-24-1103</td>
	<td><span class='somatic'>Somatic</span></td>
	<td>Missense_Mutation</td>
	<td><span class='valid'>Valid</span></td>
	<td>WashU</td>
	<td>p.K1638E</td>
	<td><span class='oma_link oma_low'><a href='omaRedirect.do?site=mutationassessor.org&cm=var&var=13,31811404,A,G&fts=all'>Low</a></span></td>
	<td><a href='omaRedirect.do?site=mutationassessor.org/&cm=msa&ty=f&p=BRCA2_HUMAN&rb=1552&re=1663&var=K1638E'>Alignment</a></td>
	<td>&nbsp;</td>
	<td>5140</td>
	<td>&nbsp;</td>
	</tr>

	<tr>
	<td>TCGA-24-1417</td>
	<td><span class='germline'>Germline</span></td>
	<td>Frame_Shift_Del</td>
	<td>Unknown</td>
	<td>WashU</td>
	<td>p.N1706fs</td>
	<td>&nbsp;</td>
	<td>&nbsp;</td>
	<td>&nbsp;</td>
	<td>5343</td>
	<td>&nbsp;</td>
	</tr>

	<tr>
	<td>TCGA-24-1463</td>
	<td><span class='germline'>Germline</span></td>
	<td>Frame_Shift_Ins</td>
	<td>Unknown</td>
	<td>WashU</td>
	<td>p.I605fs</td>
	<td>&nbsp;</td>
	<td>&nbsp;</td>
	<td>&nbsp;</td>
	<td>2034</td>
	<td>&nbsp;</td>
	</tr>

	<tr>
	<td>TCGA-24-1555</td>
	<td><span class='somatic'>Somatic</span></td>
	<td>Frame_Shift_Del</td>
	<td><span class='valid'>Valid</span></td>
	<td>Baylor</td>
	<td>p.P2608fs</td>
	<td>&nbsp;</td>
	<td>&nbsp;</td>
	<td>&nbsp;</td>
	<td>8049</td>
	<td>&nbsp;</td>
	</tr>

	<tr>
	<td>TCGA-24-1555</td>
	<td><span class='germline'>Germline</span></td>
	<td>Splice_Site</td>
	<td>Unknown</td>
	<td>Baylor</td>
	<td>e20-1</td>
	<td>&nbsp;</td>
	<td>&nbsp;</td>
	<td>&nbsp;</td>
	<td>-1</td>
	<td>&nbsp;</td>
	</tr>

	<tr>
	<td>TCGA-24-1562</td>
	<td><span class='germline'>Germline</span></td>
	<td>Nonsense_Mutation</td>
	<td><span class='valid'>Valid</span></td>
	<td>WashU</td>
	<td>p.K3326*</td>
	<td>&nbsp;</td>
	<td>&nbsp;</td>
	<td>&nbsp;</td>
	<td>10204</td>
	<td>&nbsp;</td>
	</tr>

	<tr>
	<td>TCGA-24-2024</td>
	<td><span class='germline'>Germline</span></td>
	<td>Frame_Shift_Del</td>
	<td><span class='valid'>Valid</span></td>
	<td>Broad</td>
	<td>p.Y1710fs</td>
	<td>&nbsp;</td>
	<td>&nbsp;</td>
	<td>&nbsp;</td>
	<td>5359</td>
	<td>&nbsp;</td>
	</tr>

	<tr>
	<td>TCGA-24-2280</td>
	<td><span class='germline'>Germline</span></td>
	<td>Frame_Shift_Del</td>
	<td><span class='valid'>Valid</span></td>
	<td>Broad</td>
	<td>p.S1982fs</td>
	<td>&nbsp;</td>
	<td>&nbsp;</td>
	<td>&nbsp;</td>
	<td>6174</td>
	<td>Known BRCA2 6174delT founder mutation.</td>
	</tr>

	<tr>
	<td>TCGA-24-2288</td>
	<td><span class='germline'>Germline</span></td>
	<td>Frame_Shift_Del</td>
	<td><span class='valid'>Valid</span></td>
	<td>Broad</td>
	<td>p.V220fs</td>
	<td>&nbsp;</td>
	<td>&nbsp;</td>
	<td>&nbsp;</td>
	<td>886</td>
	<td>&nbsp;</td>
	</tr>

	<tr>
	<td>TCGA-24-2293</td>
	<td><span class='germline'>Germline</span></td>
	<td>Nonsense_Mutation</td>
	<td>Unknown</td>
	<td>Broad</td>
	<td>p.R2520*</td>
	<td>&nbsp;</td>
	<td>&nbsp;</td>
	<td>&nbsp;</td>
	<td>7786</td>
	<td>&nbsp;</td>
	</tr>

	<tr>
	<td>TCGA-25-1318</td>
	<td><span class='germline'>Germline</span></td>
	<td>Frame_Shift_Del</td>
	<td>Unknown</td>
	<td>Baylor</td>
	<td>p.L1491fs</td>
	<td>&nbsp;</td>
	<td>&nbsp;</td>
	<td>&nbsp;</td>
	<td>4703</td>
	<td>&nbsp;</td>
	</tr>

	<tr>
	<td>TCGA-25-1634</td>
	<td><span class='germline'>Germline</span></td>
	<td>Splice_Site</td>
	<td>Unknown</td>
	<td>Baylor</td>
	<td>e20-1</td>
	<td>&nbsp;</td>
	<td>&nbsp;</td>
	<td>&nbsp;</td>
	<td>-1</td>
	<td>&nbsp;</td>
	</tr>

	<tr>
	<td>TCGA-25-2404</td>
	<td><span class='germline'>Germline</span></td>
	<td>Frame_Shift_Del</td>
	<td><span class='valid'>Valid</span></td>
	<td>Broad</td>
	<td>p.K343fs</td>
	<td>&nbsp;</td>
	<td>&nbsp;</td>
	<td>&nbsp;</td>
	<td>1253</td>
	<td>&nbsp;</td>
	</tr>

	<tr>
	<td>TCGA-57-1584</td>
	<td><span class='germline'>Germline</span></td>
	<td>Frame_Shift_Del</td>
	<td>Unknown</td>
	<td>Baylor</td>
	<td>p.N1784fs</td>
	<td>&nbsp;</td>
	<td>&nbsp;</td>
	<td>&nbsp;</td>
	<td>5574</td>
	<td>&nbsp;</td>
	</tr>

	<tr>
	<td>TCGA-59-2351</td>
	<td><span class='germline'>Germline</span></td>
	<td>Frame_Shift_Del</td>
	<td><span class='valid'>Valid</span></td>
	<td>Broad</td>
	<td>p.S1982fs</td>
	<td>&nbsp;</td>
	<td>&nbsp;</td>
	<td>&nbsp;</td>
	<td>6174</td>
	<td>Known BRCA2 6174delT founder mutation.</td>
	</tr>
	</tbody>
	</table>
		</div>
	</body>
</html>