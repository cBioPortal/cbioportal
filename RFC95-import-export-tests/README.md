# RFC95 import-export tests

### Inlcuded studies:
- acc_2019
- all_phase2_target_2018_pub
- aml_ohsu_2022
- brca_cptac_2020
- brca_tcga_pub
- brca_tcga_pub2015
- mel_mskimpact_2020
- nsclc_tcga_broad_2016
- msk_chord_2024
- msk_met_2021
- ov_tcga_pub
- pancan_pcawg_2020
- prad_msk_stopsack_2021

### Testing steps
1. Manually downloaded studies from datahub.
2. Manually deleted all files related to mirna data in brca_tcga_pub, ov_tcga_pub, pancan_pcawg_2020.
3. Manually uploaded studies to local cBioPortal.
4. Manually exported studies using `curl -O http://localhost:<port>/export/study/<study_id>.zip` and saved them in `downloaded_studies` folder.
5. Changed all study ids in downloaded files to add the prefix `exp_`. Within meta and case list files `cancer_study_identifier: ` was changed to `cancer_study_identifier: exp_`, and in case list files `stable_id: ` was changed to `stable_id: exp_`.
6. Manually uploaded new `exp_` studies into local cBioPortal. Import logs are located in `import_logs` directory.
7. Manually exported studies using `curl -O http://localhost:<port>/export/study/exp_<study_id>.zip` and saved them in `re-downloaded_studies` folder.
8. Ran `./diff.sh` for each study_id and saved output to `diff_outputs`.


### Encountered issues

- Unable to upload `exp_msk_chord_2024`:\
Error when validating: `ERROR: data_clinical_timeline.txt: lines [2, 3, 4, (297132 more)]: column 15: Invalid STYLE_COLOR; value encountered: ''`

- Unable to upload `msk_met_2021`:\
Crashes while loading `data_clinical_sample_attributes.txt`

- Values of column `dbSNP_RS` in mutations data file vary between original study, downloaded and re-downloaded files.

- Values of column `HGVSp_Short` are dropped in downloaded mutations file, so mutations are not plotted and `Protein Change` column values are all `MUTATED` in mutations tab.
![Example Image](images/mutations.png)