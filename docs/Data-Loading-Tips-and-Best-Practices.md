Here we describe some Tips and Best Practices.

## Running GISTIC 2.0
To generate discrete copy number data file you may need to run GISTIC 2.0. GISTIC 2.0 can be [installed](https://www.broadinstitute.org/cgi-bin/cancer/publications/pub_paper.cgi?mode=view&paper_id=216&p=t) or run online using the GISTIC 2.0 module on [GenePattern](https://cloud.genepattern.org). 
Running GISTIC 2.0 requires two input files: 

1. A segmentation file, which contains the segmented data 
2. A marker file, which identifies the marker names and positions of the markers in the original dataset (before segmentation). 

In some cases the marker file may not be available. Your can create one as follows:
Using your segmentation file, create a line for each start and end position. E.g. if your seg file contains

<table>
<tr>
<td>Sample</td><td>Chrom</td><td>Start</td><td>Stop</td><td>#Mark</td><td>Seg.CN</td>
</tr>
<tr>
<td>S1</td><td>1</td><td>61735</td><td>77473</td><td>10</td><td>-0.1234 </td>
</tr>
</table>

In your markerfile this becomes

<table>
<tr>
<td>Marker Name</td><td>Chrom</td><td>Pos</td>
</tr>
<tr>
<td>S1</td><td>1</td><td>61735</td>
</tr>
<tr>
<td>S1</td><td>1</td><td>77473</td>
</tr>
</table>


## Effect of cBioPortal instance on validation
When validating data, you can decide against which server to validate your data with the -u flag. The selected server can have a significant effect on the validation results in the following ways:

1. Genes may or may not be available on a specific server
2. Clinical data and its description may vary per server
3. ...

It is advised to use the server where you plan on upload your data as validation server. 
