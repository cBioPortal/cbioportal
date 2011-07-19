./resetDb.pl
./prostate/gen-import-files.py
./importGenes.pl ../data/human_genes.txt
./importProfileData.pl --data $CGDS_HOME/data/prostate/PROSTATE-RAE-FINAL.txt --meta $CGDS_HOME/data/prostate/PROSTATE-RAE-META.txt --dbmsAction clobber
./generateMutationData.pl $CGDS_HOME/data/prostate/all-list.txt $CGDS_HOME/data/prostate/prostate_sequenced_genes.txt $CGDS_HOME/data/prostate/prostate_sequenced_cases.txt $CGDS_HOME/data/prostate/PROSTATE-MUTATION-RAW > ../data/prostate/PROSTATE-MUTATION-FINAL.txt
./importProfileData.pl --data $CGDS_HOME/data/prostate/PROSTATE-MUTATION-FINAL.txt --meta $CGDS_HOME/data/prostate/PROSTATE-MUTATION-META.txt --dbmsAction clobber
./importCaseList.pl $CGDS_HOME/data/prostate/PROSTATE-ALL-CASE-LIST.txt
./importCaseList.pl $CGDS_HOME/data/prostate/PROSTATE-CNA-CASE-LIST.txt
./importCaseList.pl $CGDS_HOME/data/prostate/PROSTATE-MRNA-CASE-LIST.txt
./importCaseList.pl $CGDS_HOME/data/prostate/PROSTATE-MRNA-AND-CNA-CASE-LIST.txt
./importCaseList.pl $CGDS_HOME/data/prostate/PROSTATE-MRNA-MATCHED-TUMOR-NORMAL-CASE-LIST.txt
./importCaseList.pl $CGDS_HOME/data/prostate/PROSTATE-MRNA-NORMAL-SAMPLE-LIST.txt
./importCaseList.pl $CGDS_HOME/data/prostate/PROSTATE-METASTATIC-CASE-LIST.txt
./importCaseList.pl $CGDS_HOME/data/prostate/PROSTATE-PRIMARY-CASE-LIST.txt
./importCaseList.pl $CGDS_HOME/data/prostate/PROSTATE-SEQUENCED-CASE-LIST.txt
./importClinicalData.pl $CGDS_HOME/data/prostate/PROSTATE-CLINICAL-FINAL
./importMSigDb.pl ../data/c2.kegg.v2.5.txt KEGG
./analyzeGeneSets.pl
