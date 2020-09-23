# Onco Query Language (OQL)


<!-- TOC depthFrom:2 depthTo:6 withLinks:1 updateOnSave:1 orderedList:0 -->

- [Introduction to OQL](#introduction)
- [OQL Keywords](#oql-keywords)
    - [OQL Modifiers](#oql-modifiers)
- [Basic Usage](#basic-usage)
- [Mutations](#mutations)
- [Copy Number Alterations](#cna)
- [Expression](#expression)
- [Protein](#protein)
- [Modifiers](#modifiers)
    - [Driver](#driver)
    - [Germline/Somatic](#germline-somatic)
- [The DATATYPES Command](#the-datatypes-command)
- [Merged Gene Tracks](#merged-gene-tracks)
- [Example: RB Pathway Alterations](#example-rb-pathway-alterations)
    - [Using the Defaults](#using-the-defaults)
    - [Greater Insight with OQL](#greater-insight-with-oql)
- [Questions? Feedback?](#questions-feedback)

<!-- /TOC -->

<a name="introduction"></a>
## Introduction to OQL
The Onco Query Language (OQL) is used to define which specific types of alterations are included in a query on the cBioPortal for Cancer Genomics. By default, querying for a gene includes mutations, fusions, amplifications and deep deletions. OQL can be used to specify specific mutations (e.g. BRAF V600E) or types of mutations (e.g. BRCA1 truncating mutations), lower level copy number alterations (e.g. CDKN2A shallow deletions), changes in mRNA or protein expression, and more.

OQL-specified alterations will be reflected on most tabs, including OncoPrint, but are not currently reflected on the Plots, Co-Expression or Expression tabs.

Note that OQL assumes any word that it doesn't recognize is a mutation code.

Additional explanation and examples using OQL are available in the [OQL tutorial](https://www.cbioportal.org/tutorials#oql).

<a name="oql-keywords"></a>
## OQL Keywords
Users can define specific subsets of genetic alterations for five data types:

Data Type | Keywords and Syntax | Default*
--------- | ------------------- | --------
Mutations | `MUT` All non-synonymous mutations <br> `MUT = <protein change>` Specific amino acid changes (e.g. `V600E` or `V600`) <br> `MUT = <mutation type>` Acceptable values are: `MISSENSE, NONSENSE, NONSTART, NONSTOP, FRAMESHIFT, INFRAME, SPLICE, TRUNC` | `MUT`
Fusions | `FUSION` All fusions (note that many studies lack fusion data) | `FUSION`
Copy Number Alterations | `AMP` Amplifications <br> `HOMDEL` Deep Deletions <br> `GAIN` Gains <br> `HETLOSS` Shallow Deletions <br> Comparison operators can also be used with `CNA` (e.g. `CNA >= GAIN` is the same as `AMP GAIN`) | `AMP` <br> `HOMDEL`
mRNA Expression | `EXP < -x` mRNA expression is less than `x` standard deviations (SD) below the mean <br> `EXP > x` mRNA expression is greater than `x` SD above the mean <br> The comparison operators `<=` and `>=` also work | `EXP >= 2` <br> `EXP <= -2`
Protein/phosphoprotein level | `PROT < -x` Protein expression is less than `x` standard deviations (SD) below the mean <br> `PROT > x` Protein expression is greater than `x` SD above the mean <br> The comparison operators `<=` and `>=` also work | `PROT >= 2` <br> `PROT <= -2`

\* These are the default OQL keywords used for each data type when a gene is queried without any explicit OQL.

<a name="oql-modifiers"></a>
### OQL modifiers
Mutations and copy number alterations can be further refined using modifiers:

Keyword | Applicable Data Type | Explanation
------- | -------------------- | -----------
`DRIVER` | Mutations <br> Fusions <br> Copy Number Alterations | Include only mutations, fusions and copy number alterations which are driver events, as defined in OncoPrint (default: OncoKB and CancerHotspots).
`GERMLINE` | Mutations | Include only mutations that are defined as germline events by the study.
`SOMATIC` | Mutations | Include all mutations that are not defined as germline.
`(a-b)` (protein position range) | Mutations | Include all mutations that overlap with the protein position range `a-b`, where `a` and `b` are integers. If you add a `*` (i.e. `(a-b*)`) then it will only include those mutations that are fully contained inside `a-b`. The open-ended ranges `(a-)` and `(-b)` are also allowed. 

<br>

<a name="basic-usage"></a>
## Basic Usage
When querying a gene without providing any OQL specifications, cBioPortal will default to these OQL terms for a query with Mutation and Copy Number selected in the Genomic Profiles section:
`MUT FUSION AMP HOMDEL`

![image of basic query](https://raw.githubusercontent.com/cBioPortal/cbioportal/master/docs/images/OQL/basic_query.png)

You can see the OQL terms applied by hovering over the gene name in OncoPrint:

![image of basic query oncoprint](https://raw.githubusercontent.com/cBioPortal/cbioportal/master/docs/images/OQL/basic_query_oncoprint.png)


If you select RNA and/or Protein in the "Genomic Profiles" section of the query, the default settings are:

RNA: `EXP >= 2 EXP <= -2`

Protein: `PROT >= 2 PROT <= -2`

![image of exp prot query oncoprint](https://raw.githubusercontent.com/cBioPortal/cbioportal/master/docs/images/OQL/basic_query_oncoprint_exp_prot.png)

You must select the relevant Genomic Profile in order for OQL to query that data type. For example, you can't add `EXP > 2` to the query without also selecting an RNA profile.


Proper formatting for OQL is straightforward: gene name, followed by a colon, followed by any OQL keywords and ending in a semicolon, an end-of-line, or both.
```
GENE1: OQL KEYWORDS;
GENE2: OQL KEYWORDS
```
In general, any combination of OQL keywords and/or expressions can annotate any gene, and the order of the keywords is immaterial.



Below we will go into greater detail about each data type.

<br>

<a name="mutations"></a>
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
TP53: MUT = <mutation type>
```

`<mutation type>` can be one or more of:
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

OQL for mutations can also be written without `MUT =`. The following examples are identical:
```
BRAF: MUT = V600E
BRAF: V600E
TP53: MUT = TRUNC INFRAME
TP53: TRUNC INFRAME
```

OQL can also be used to exclude a specific protein change, position or type of mutation. For example, below are examples to query all EGFR mutations except T790M, all BRAF mutations except those at V600 and all TP53 mutations except missense:
```
EGFR: MUT != T790M
BRAF: MUT != V600
TP53: MUT != MISSENSE
```
Note that this will only work to exclude a single event. Because OQL uses 'OR' logic, excluding multiple mutations or excluding a mutation while including another mutation (e.g. `BRAF: MUT=V600 MUT!=V600E`) will result in querying all mutations.


<br>

<a name="cna"></a>
## Copy Number Alterations
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

<br>

<a name="expression"></a>
## Expression
High or low mRNA expression of a gene is determined by the number of standard deviations (SD) from the mean. For example, to see cases where mRNA for CCNE1 is greater than 3 SD above the mean:
```
CCNE1: EXP > 3
```

<br>

<a name="protein"></a>
## Protein
High or low protein expression is similarly determined by the number of SD from the mean. For example, to see cases where protein expression is 2 SD above the mean:
```
EGFR: PROT > 2
```

Protein expression can also be queried at the phospho-protein level:
```
EGFR_PY992: PROT > 2
```

<br>

<a name="modifiers"></a>
## Modifiers
Modifiers can be used on their own or in combination with other OQL terms for mutations, fusions and copy number alterations to further refine the query. Modifiers can be combined with other OQL terms using an underscore. The order in which terms are combined is immaterial.

<a name="driver"></a>
### Driver
The `DRIVER` modifier applies to mutations, fusions and copy number alterations. The definition of what qualifies as a driver alteration comes from the "Mutation Color" menu in OncoPrint. By default, drivers are defined as mutations, fusions and copy number alterations in <a href="https://www.oncokb.org">OncoKB</a> or <a href="https://www.cancerhotspots.org">CancerHotspots</a>.

On its own, the `DRIVER` modifier includes driver mutations, fusions and copy number alterations:
```
EGFR: DRIVER
```

Or it can be used in combination with another OQL term. For example, to see only driver fusion events:
```
EGFR: FUSION_DRIVER
```

Or driver missense mutations:
```
EGFR: MUT = MISSENSE_DRIVER
```

When combining `DRIVER` with another OQL term, the order doesn't matter: `MUT_DRIVER` and `DRIVER_MUT` are equivalent. `DRIVER` can be combined with:
* `MUT`
* `MUT = <mutation type>` or `MUT = <protein change>`
* `FUSION`
* `CNA`
* `AMP` or `GAIN` or `HETLOSS` or `HOMDEL`
* `GERMLINE` or `SOMATIC` (see below)


<a name="germline-somatic"></a>
### Germline/Somatic
The `GERMLINE` and `SOMATIC` modifiers only apply to mutations. A mutation can be explicitly defined as germline during the data curation process. Note that very few studies on the public cBioPortal contain germline data.

`GERMLINE` or `SOMATIC` can be combined with:
* `MUT`
* `MUT = <mutation type>` or `MUT = <protein change>`
* `DRIVER`

To see all germline BRCA1 mutations:
```
BRCA1: GERMLINE
```

Or to see specifically truncating germline mutations:
```
BRCA1: TRUNC_GERMLINE
BRCA1: GERMLINE_TRUNC
```
The order is immaterial; both options produce identical results.

Or to see somatic missense mutations:
```
BRCA1: MUT = MISSENSE_SOMATIC
``` 

`GERMLINE` or `SOMATIC` can also be combined with `DRIVER` and, optionally, a more specific mutation term (e.g. `NONSENSE`):
```
BRCA1: NONSENSE_GERMLINE_DRIVER
```

<br>

<a name="the-datatypes-command"></a>
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

<br>

<a name="merged-gene-tracks"></a>
## Merged Gene Tracks
OQL can be used to create a merged gene track in OncoPrint, in which alterations in multiple genes appear as a single track. This is done by enclosing a list of genes in square brackets. By default, the track will be labeled by the gene names, separated by '/'. To instead specify a label, type the desired label within double quotes at the beginning of the square brackets. For example:
```
["CDK INHIBITORS" CDKN2A CDKN2B]
[MDM2 MDM4]
```

The resulting merged gene track will be visible in OncoPrint and can be expanded to view the individual gene tracks. For example:

![Image of merged genes in OncoPrint](https://raw.githubusercontent.com/cBioPortal/cbioportal/master/docs/images/OQL/merged_genes_oncoprint.png)

<https://www.cbioportal.org/results/oncoprint?session_id=5c1966e2e4b05228701f958e>

It is possible to include OQL for specific alterations in merged gene tracks, as well as querying a combination of single and merged gene tracks.

Note that merged gene tracks only appear in OncoPrint. All other pages show the individual genes.

<br>

<a name="example-rb-pathway-alterations"></a>
## Example: RB Pathway Alterations

Provided below is one example of the power of using OQL. Additional examples are available in the [OQL tutorial](https://www.cbioportal.org/tutorials#oql).

<a name="using-the-defaults"></a>
### Using the Defaults
Select Ovarian Serous Cystadenocarcinoma (TCGA, Nature 2011) with the following data types:
* Mutations
* Putative copy-number alterations (GISTIC)
* mRNA expression (mRNA expression Z-scores (all genes))

Input the following three genes in the RB pathway:
* CCNE1
* RB1
* CDKN2A

![image of rb query](https://raw.githubusercontent.com/cBioPortal/cbioportal/master/docs/images/OQL/rb_query.png)

Submit this query and note how many samples have alterations in multiple of these genes:

![image of rb oncoprint](https://raw.githubusercontent.com/cBioPortal/cbioportal/master/docs/images/OQL/rb_query_oncoprint.png)

<https://www.cbioportal.org/results/oncoprint?session_id=5c1966cee4b05228701f958d>


<a name="greater-insight-with-oql"></a>
### Greater Insight with OQL
Given what is known about the RB pathway, the events that are most likely selected for in the tumors are CCNE1 amplification, RB1 deletions or mutations, and loss of expression of CDKN2A. To investigate this hypothesis, we can use OQL to display only these events. Modify the query to reflect this:
```
CCNE1: AMP MUT
RB1: HOMDEL MUT
CDKN2A: HOMDEL EXP < -1
```

Examine the updated OncoPrint:

![image of modified rb oncoprint](https://raw.githubusercontent.com/cBioPortal/cbioportal/master/docs/images/OQL/rb_query_oncoprint_modified.png)

<https://www.cbioportal.org/results/oncoprint?session_id=5c1966aee4b05228701f958c>

This shows that alterations in these genes are almost entirely mutually-exclusive -- no cases are altered in all three genes and only six are altered in two genes. This supports the theory that the tumor has selected for these events.

<br>

<a name="questions-feedback"></a>
## Questions? Feedback?
Please share any questions or feedback on OQL with us: <https://groups.google.com/group/cbioportal>

Also note that additional explanation and examples using OQL are available in the [OQL tutorial](https://www.cbioportal.org/tutorials#oql).
