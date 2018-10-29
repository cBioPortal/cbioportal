# Advanced cancer genomic data visualization: Onco Query Language (OQL)

You can use the Onco Query Language (OQL) to select specific types of alterations on the cBioPortal for Cancer Genomics. OQL-specified alterations will be reflected on most tabs, including OncoPrint, but are not currently reflected on the Plots, Mutations, Co-Expression or Expression tabs. Note that OQL assumes any word that it doesn't recognize is a mutation code.

<!-- TOC depthFrom:2 depthTo:6 withLinks:1 updateOnSave:1 orderedList:0 -->

- [OQL Keywords](#oql-keywords)
- [Basic Usage](#basic-usage)
- [Mutations](#mutations)
- [CNA](#cna)
- [Expression](#expression)
- [Protein](#protein)
- [The DATATYPES Command](#the-datatypes-command)
- [Merged Gene Tracks](#merged-gene-tracks)
- [Example: RB Pathway Alterations](#example-rb-pathway-alterations)
	- [Using the Defaults](#using-the-defaults)
	- [Greater Insight with OQL](#greater-insight-with-oql)
- [Questions? Feedback?](#questions-feedback)

<!-- /TOC -->

## OQL Keywords
Users can define specific subsets of genetic alterations for four data types:

Data Type | Keyword | Categories and Levels | Default*
--------- | ------- | --------------------- | --------
Copy Number Alterations | `CNA` | `AMP` Amplified <br> `HOMDEL` Deep Deletion <br> `GAIN` Gained <br> `HETLOSS` Shallow Deletion <br> Comparison operators can also be used (e.g. `CNA >= GAIN` is the same as `AMP GAIN`) | `AMP` <br> `HOMDEL`
Mutations | `MUT` | `MUT` All somatic, non-synonymous mutations <br> `MUT = <protein change>` Specific amino acid changes (e.g. `V600E` or `V600`) <br> `MUT = <mutation type>` Acceptable values are: `MISSENSE, NONSENSE, NONSTART, NONSTOP, FRAMESHIFT, INFRAME, SPLICE, TRUNC` <br> `FUSION` Show cases with fusions | `MUT` <br> `FUSION`
mRNA Expression | `EXP` | `EXP < -x` Under-expression is less than `x` standard deviations (SD) below the mean <br> `EXP > x` Over-expression is greater than `x` SD above the mean <br> The comparison operators `<=` and `>=` also work | `EXP >= 2` <br> `EXP <= -2`
Protein/phosphoprotein level (RPPA) | `PROT` | `PROT < -x` Protein-level under-expression is less than `x` standard deviations (SD) below the mean <br> `PROT > x` Protein-level over-expression is greater than `x` SD above the mean <br> The comparison operators `<=` and `>=` also work | `PROT >= 2` <br> `PROT <= -2`

\* These are the default OQL keywords used for each data type when a gene is queried without any explicit OQL.

## Basic Usage
When querying a gene without providing any OQL specifications, cBioPortal will default to these OQL terms for a query with Mutation and Copy Number selected in the Genomic Profiles section:
`MUT FUSION AMP HOMDEL`

![image of basic query](https://user-images.githubusercontent.com/1334004/47438017-a0950180-d777-11e8-9bb3-36758c7d6301.png)

You can see the OQL terms applied by hovering over the gene name in OncoPrint:

![image of basic query oncoprint](https://user-images.githubusercontent.com/1334004/47438015-9ffc6b00-d777-11e8-9d19-cbca5405b26a.png)


If you select RNA and/or Protein in the "Genomic Profiles" section of the query, the default settings are:

RNA: `EXP >= 2 EXP <= -2`

Protein: `PROT >= 2 PROT <= -2`

![image of exp prot query oncoprint](https://user-images.githubusercontent.com/1334004/47438014-9ffc6b00-d777-11e8-87ad-dea52965a5e8.png)

You must select the relevant Genomic Profile in order for OQL to query that data type. For example, you can't just add `EXP > 2` to the query without also selecting an RNA profile.


Proper formatting for OQL is straightforward: gene name, followed by a colon, followed by any OQL keywords and ending in a semicolon, an end-of-line, or both.
```
GENE1: OQL KEYWORDS;
GENE2: OQL KEYWORDS;
```
In general, any combination of OQL keywords and/or categories can annotate any gene, and the order of the keywords is immaterial.



Below we will go into greater detail about each data type.


## Mutations
To view cases with specific mutations, provide the specific amino acid change of interest:
```
BRAF: MUT = V600E
```

You can also view all mutations at a particular position:
```
BRAF: MUT = V600
```

Or all mutations of a specific type:
```
TP53: MUT = <TYPE>
```

`<TYPE>` can be one or more of:
* `MISSENSE`
* `NONSENSE`
* `NONSTART`
* `NONSTOP`
* `FRAMESHIFT`
* `INFRAME`
* `SPLICE`
* `TRUNC`

For example, to view TP53 truncating mutations and in-frame insertions/deletions:
```
TP53: MUT = TRUNC INFRAME
```


## CNA
To view cases with specific copy number alterations, provide the appropriate keywords for the copy number alterations of interest. For example, to see amplifications:
```
CCNE1: AMP
```

Or amplified and gained cases:
```
CCNE1: CNA >= GAIN
```

Which can also be written as:
```
CCNE1: GAIN AMP
```


## Expression
Over- or under-expression of a gene is determined by the number of standard deviations (SD) from the mean. For example, to see cases where mRNA for CCNE1 is greater than 3 SD above the mean:
```
CCNE1: EXP > 3
```


## Protein
Protein over- or under-expression is similarly determined by the number of SD from the mean. For example, to see cases that are over-expressed in RPPA protein level by 2 SD above the mean:
```
EGFR: PROT > 2
```

Or over-expressed at the phospho-protein level:
```
EGFR_PY992: PROT > 2
```

## The DATATYPES Command
To save copying and pasting, the `DATATYPES` command sets the genetic annotation for all subsequent genes. Thus,
```
DATATYPES: AMP GAIN HOMDEL EXP > 1.5 EXP < -1.5; CDKN2A MDM2 TP53
```

is equivalent to:
```
CDKN2A: AMP GAIN HOMDEL EXP > 1.5 EXP < -1.5
MDM2: AMP GAIN HOMDEL EXP > 1.5 EXP < -1.5
TP53: AMP GAIN HOMDEL EXP > 1.5 EXP < -1.5
```

## Merged Gene Tracks
OQL can be used to create a merged gene track in OncoPrint, in which alterations in multiple genes appear as a single track. This is done by enclosing a list of genes in square brackets. By default, the track will be labeled by the gene names, separated by '/'. To instead specify a label, type the desired label within double quotes at the beginning of the square brackets. For example:
```
["CDK INHIBITORS" CDKN2A CDKN2B]
[MDM2 MDM4]
```

The resulting merged gene track will be visible in OncoPrint and can be expanded to view the individual gene tracks. For example:

![Image of merged genes in OncoPrint](https://user-images.githubusercontent.com/1334004/47438018-a0950180-d777-11e8-9ab3-b6c8d0b23db1.png)


It is possible to include OQL for specific alterations in merged gene tracks, as well as querying a combination of single and merged gene tracks.

Note that merged gene tracks will only appear in OncoPrint. All other pages will show the individual genes.

## Example: RB Pathway Alterations
### Using the Defaults
Select Ovarian Serous Cystadenocarcinoma (TCGA, Nature 2011) with the following data types:
* Mutations
* Putative copy-number alterations (GISTIC)
* mRNA expression (mRNA expression Z-scores (all genes))

Input the following three genes in the RB pathway:
* CCNE1
* RB1
* CDKN2A

![image of rb query](https://user-images.githubusercontent.com/1334004/47438021-a12d9800-d777-11e8-910c-7b4bcf8a9394.png)

Submit this query and note how many samples have alterations in multiple of these genes:

![image of rb oncoprint](https://user-images.githubusercontent.com/1334004/47438020-a12d9800-d777-11e8-8150-4a3ab15d0ca0.png)

<http://www.cbioportal.org/index.do?session_id=59c16a64498e5df2e2957f19&show_samples=false&>


### Greater Insight with OQL
Given what is known about the RB pathway, the events that are most likely selected for in the tumors are CCNE1 amplification, RB1 deletions or mutations, and loss of expression of CDKN2A. To investigate this hypothesis, we can use OQL to display only these events. Modify the query to reflect this:
```
CCNE1: AMP MUT
RB1: HOMDEL MUT
CDKN2A: HOMDEL EXP < -1
```

Examine the updated OncoPrint:

![image of modified rb oncoprint](https://user-images.githubusercontent.com/1334004/47438019-a0950180-d777-11e8-8934-21f3fbe61662.png)

<http://www.cbioportal.org/index.do?session_id=59c16ab4498e5df2e2957f1f&show_samples=false&>

This shows that alterations in these genes are almost entirely mutually-exclusive -- no cases are altered in all three genes and only six are altered in two genes. This supports the theory that the tumor has selected for these events.



## Questions? Feedback?
Please share any questions or feedback on OQL with us: <http://groups.google.com/group/cbioportal>
