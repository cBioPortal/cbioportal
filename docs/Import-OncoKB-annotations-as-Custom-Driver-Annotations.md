# Import OncoKB annotations as custom driver annotations

The _Annotation Configuration_ menu in Study View and Group Comparison is available only when _custom driver annotations_
are present in the cBioPortal database for the genes in the study (or studies). In order to use OncoKB annotations to filter
mutations and discrete copy number alteration in Study View and Group Comparison, OncoKB annotations can be added to the 
respective data files of a study prior to import into the database. This page describes how to import OncoKB annotations 
as custom driver annotations. It assumes the following requirements have been satisfied:

1. The cBioPortal software has been correctly [built from source](Build-from-Source.md).
2. The user is able to successfully import a [study into the database](Data-Loading.md).
3. The study subjected to OncoKB import is [confirmed to be valid](Data-Loading.md#validating-the-study)

## Import of OncoKB annotations when loading a study

OncoKB annotations can be added automatically to the study files when the study is loaded into the database by adding the
`--import_oncokb` parameter to the [metaImport.py](core/src/main/scripts/importer/metaImport.py) script like so:

```
python metaImport.py --import_oncokb -u http://cbioportal:8080 -s study/lgg_ucsf_2014/
```

This will add OncoKB data to the mutation and discrete CNA files of a study, revalidate the results and load the study
into the database.

The addition of mutation and discrete CNA files is explained in detail below.

## Update of MAF file with OncoKB annotations

OncoKB annotations can be added to the MAF file by running [importOncokbMutation.py](https://github.com/cBioPortal/cbioportal/tree/master/core/src/main/scripts/importer/importOncokbMutation.py) like so:

```
python importOncokbMutation.py -s study/lgg_ucsf_2014/ -u https://cbioportal.org
```

Where `-s` is the path to the directory of the MAF file and `-u` is the URL to 
a cBioPortal instance (needed for resolution of gene identifiers).

[importOncokbMutation.py](core/src/main/scripts/importer/importOncokbMutation.py) will add OncoKB annotations as
[custom driver annotation columns](File-Formats.md#custom-driver-annotations) in the MAF file. The unmodified MAF file
will be stored in the study directory with the _ONCOKB_IMPORT_BACKUP__ prefix.


## Update of Discrete Copy Number file with OncoKB annotations

OncoKB annotations can be added to the Discrete Copy Number data by running [importOncokbDiscreteCNA.py](https://github.com/cBioPortal/cbioportal/tree/master/core/src/main/scripts/importer/importOncokbDiscreteCNA.py) like so:

```
python importOncokbDiscreteCNA.py -s /tmp/study -u https://cbioportal.org
```

Where `-s` is the path to the directory of the iscrete Copy Number data file and `-u` is the URL to 
a cBioPortal instance (needed for resolution of gene identifiers).

[importOncokbDiscreteCNA.py](core/src/main/scripts/importer/importOncokbMutation.py) will create a [custom driver annotation file](File-Formats.md#custom-driver-annotations-file)
with name _data_cna_pd_annotation.txt_ in the study directory. It will add a field `pd_annotations_filename` field in the 
CNA meta file that references the newly created custom driver annotation file. The unmodified CNA meta file will be stored
in the study directory with the _ONCOKB_IMPORT_BACKUP__ prefix.
