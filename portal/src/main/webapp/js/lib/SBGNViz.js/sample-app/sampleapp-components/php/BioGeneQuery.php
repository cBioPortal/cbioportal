<?php

//Biogene url
$url = "http://cbio.mskcc.org/biogene/retrieve.do";

//Biogene query parameters
$query = urlencode($_POST["query"]);
$format = urlencode($_POST["format"]);
$organism = urlencode($_POST["org"]);

// Query string
$queryDataArray = array("query"=>$query,"format"=>$format,"org"=>$organism);
$url = $url . '?' . http_build_query($queryDataArray);

// Get JSON from BioGene
$output = file_get_contents($url);

echo $output;

?>