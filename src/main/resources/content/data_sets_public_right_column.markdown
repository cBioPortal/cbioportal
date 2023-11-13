<P>The Portal contains data for <b>5046 tumor samples from 20 cancer studies.</b> [<a href='data_sets.jsp'>Details.</a>]</p>
<script type='text/javascript' src='https://www.google.com/jsapi'></script>
<script type='text/javascript'>
google.load('visualization', '1.0', {'packages':['corechart']});
google.setOnLoadCallback(drawChart);
function drawChart() {
var data = new google.visualization.DataTable();
data.addColumn('string', 'Cancer Study');
data.addColumn('number', 'Samples');
data.addRows([['Bladder Urothelial Carcinoma (TCGA)', 35],
['Breast Invasive Carcinoma (TCGA)', 813],
['Cervical Squamous Cell Carcinoma (TCGA)', 36],
['Colon and Rectum Adenocarcinoma (TCGA)', 575],
['Glioblastoma Multiforme (TCGA)', 577],
['Head and Neck Squamous Cell Carcinoma (TCGA)', 165],
['Kidney Renal Clear Cell Carcinoma (TCGA)', 501],
['Kidney Renal Papillary Cell Carcinoma (TCGA)', 43],
['Brain Lower Grade Glioma (TCGA)', 80],
['Liver Hepatocellular Carcinoma (TCGA)', 53],
['Lung Adenocarcinoma (TCGA)', 267],
['Lung Squamous Cell Carcinoma (TCGA)', 212],
['Ovarian Serous Cystadenocarcinoma (TCGA)', 563],
['Pancreatic Adenocarcinoma (TCGA)', 14],
['Prostate Adenocarcinoma (MSKCC)', 216],
['Prostate Adenocarcinoma (TCGA)', 82],
['Sarcoma (Multiple subtypes) (MSKCC)', 207],
['Stomach Adenocarcinoma (TCGA)', 149],
['Thyroid Carcinoma (TCGA)', 85],
['Uterine Corpus Endometrioid Carcinoma (TCGA)', 373]
]);
var options = {
'backgroundColor':'#F1F6FE',
'is3D':false,
'pieSliceText':'value',
'tooltip':{'text':'value'},
'width':300,
'legend':{'position':'none'},
'left':0,'top':0,
'height':300};
var chart = new google.visualization.PieChart(document.getElementById('chart_div1'));
chart.draw(data, options);
}
</script>
<div id='chart_div1'></div>
