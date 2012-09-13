# Advanced cancer genomic data visualization: The Onco Query Language (OQL)

You can use the Onco Query Language (OQL) to select and define genetic alterations for all output
on the cBio Cancer Genomics Portal, including the OncoPrint, heat map, and data downloads.

## Genetic Alterations

Users can define genetic alterations for three data types:

<table>
    <tr>
        <th>Data Type</th>
        <th>Keyword</th>
        <th>Categories and Levels</th>
        <th>Default</th>
    </tr>
    <tr>
        <td>Copy Number Alterations</td>
        <td><TT>CNA</TT></td>
        <td><TT>AMP </TT> Amplified<BR>
            <TT>HOMDEL </TT> Homozygously Deleted<BR>
            <TT>GAIN </TT> Gained<BR>
            <TT>HETLOSS </TT> Hemizygously Deleted
        </td>
        <td><TT>AMP</TT> and <TT>HOMDEL</TT></td>
    </tr>
    <tr>
        <td>Mutations</td>
        <td><TT>MUT</TT></td>
        <td><TT>MUT </TT> Show mutated cases<BR>
            <TT>MUT = X</TT> Specific mutations or mutation types.
        </td>
        <td>All somatic, non-synonymous mutations</td>
    </tr>
    <tr>
        <td>mRNA Expression</td>
        <td><TT>EXP</TT></td>
        <td><TT>EXP &lt; -x </TT> Under-expression is less than <TT>x</TT> SDs below the mean.<BR>
		<TT>EXP &gt; x </TT> Over-expression is greater than <TT>x</TT> SDs above the mean.<BR>
			The comparison operators <TT>&lt;=</TT> and <TT>&gt;=</TT> also work.<BR>
	</td>
        <td>At least 2 standard deviations (SD) from the mean.</td>
    </tr>
    <tr>
        <td>Protein/phosphoprotein level (RPPA)</td>
        <td><TT>PROT</TT></td>
        <td><TT>PROT &lt; -x </TT> Protein-level under-expression is less than <TT>x</TT> SDs below the mean.<BR>
		<TT>PROT &gt; x </TT> Protein-level over-expression is greater than <TT>x</TT> SDs above the mean.<BR>
			The comparison operators <TT>&lt;=</TT> and <TT>&gt;=</TT> also work.<BR>
        </td>
        <td>At least 1 RPPA score from the median.</td>
    </tr>
</table>

## Basic Usage

Assuming you have selected mutations, copy number data, and mRNA expression data in step 2 of
your query, you can use OQL to view only amplified cases in CCNE1:

     CCNE1: AMP

or amplified and gained cases:

     CCNE1:  CNA >= GAIN

which can also be written:

     CCNE1:  GAIN AMP

To view cases with specific mutations:

     BRAF: MUT = V600E

or mutations on specific position only:

     BRAF: MUT = V600

or mutations of a specific type:

     TP53: MUT = <TYPE>

&lt;TYPE&gt; could be

* MISSENSE
* NONSENSE
* NONSTART
* NONSTOP
* FRAMESHIFT
* INFRAME
* SPLICE
* TRUNC

e.g., to view TP53 truncating mutations and in-frame insertions/deletions:

     TP53: MUT = TRUNC MUT = INFRAME

To view amplified and mutated cases:

     CCNE1:  AMP MUT

but to define over-expressed cases as those with mRNA expression greater than 3 standard deviations above the mean:

     CCNE1: EXP > 3

To query cases that are over expressed in RPPA protein/phopshoprotein level:

     EGFR: PROT > 1

or

     EGFR_PY992: prot > 1

Hint: inputing RPPA-PROTEIN or RPPA-PHOSPHO in the query will allow you to select from all proteins or phopshoproteins that have RPPA levels.

In general, any combination of OQL keywords and/or categories can annotate any gene.

## Example:  RB Pathway

### Using the Defaults

Assuming these data types are selected in Step 2 of your query:

* Mutations
* Copy-number alterations
* mRNA expression

Selecting ovarian cancer and inputting the following three genes in the RB1 pathway

	CCNE1 RB1 CDKN2A

displays the default visualization:

![Example 1](images/example_oncoPrint_for_instructions_1.png)

### Greater Insight with the OQL Language

Given what is known about the RB pathway, the events that are most likely selected for
in the tumors are CCNE1 amplification, RB1 deletions or mutations, and loss of expression
of CDKN2A.  To investigate this hypothesis, we use OQL to display only
these events:

	CCNE1: AMP MUTATED
	RB1: HOMDEL MUTATED
	CDKN2A: HOMDEL EXP < -1

![Example 1](images/example_oncoPrint_for_instructions_2.png)

This shows that alterations in these genes are almost entirely mutually-exclusive --
no cases are altered in all three genes, and only 8 are altered in two genes.
This supports the theory that the tumor has selected for these events. 

## The DATATYPES Command

To save copying and pasting, the DATATYPES command sets the genetic annotation for all subsequent genes. Thus,

	DATATYPES: AMP GAIN HOMDEL EXP > 1.5 EXP<=-1.5;	CDKN2A MDM2 TP53

is equivalent to

	CDKN2A : AMP GAIN HOMDEL EXP<=-1.5 EXP>1.5;
	MDM2   : AMP GAIN HOMDEL EXP<=-1.5 EXP>1.5;
	TP53   : AMP GAIN HOMDEL EXP<=-1.5 EXP>1.5;

Note that the order of datatype specifications is immaterial,
and that a ': sequence of data specifications ' command can be terminated by an end-of-line, a semicolon or both.

Please share any questions or feedback on this language with us.