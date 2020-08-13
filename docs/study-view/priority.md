# Study View Customization with Priority Data
Example of study view in public portal: https://www.cbioportal.org/study?id=acc_tcga,lgg_tcga#summary

## Priorities

The priority system is represented with a final score.
The higher the final (numeric) score, the higher priority assigned.

The default score is 1.

To promote certain chart in study view, please increase priority in the database to a certain number.
The higher the score, the higher priority it will be displayed in the study view.
If you want to hide chart, please set the priority to 0.
For combination chart, as long as one of the clinical attributes has been set to 0, it will be hidden.

Currently, we preassigned priority to few charts, but as long as you assign a priority in the database except than 1, these preassigned priorities will be overwritten.

| Chart name(clinical attribute ID)                          	| Frontend default priority 	| Additional Info                                   |
|------------------------------------------------------------	|---------------------------	|-------------------------------------------------	|
| CANCER_TYPE                                                	| 3000                      	|                                                 	|
| CANCER_TYPE_DETAILED                                       	| 2000                      	|                                                 	|
| Overall Survival Plot                                      	| 400                       	| This is combination of OS_MONTH and OS_STATUS   	|
| Disease Free Survival Plot                                 	| 300                       	| This is combination of DFS_MONTH and DFS_STATUS 	|
| Mutation Count vs. Fraction of Genome Altered Density Plot 	| 200                       	|                                                 	|
| Mutated Genes Table                                        	| 90                        	|                                                 	|
| CNA Genes Table                                            	| 80                        	|                                                 	|
| Cancer Studies                                             	| 70                        	|                                                 	|
| Number of Samples Per Patient                                 | 40                        	|                                                 	|
| Mutation Count Bar Chart                                   	| 30                        	|                                                 	|
| CNA Bar Chart                                              	| 20                        	|                                                 	|
| GENDER, SEX                                                	| 9                         	|                                                 	|
| AGE                                                        	| 8                         	|                                                 	|
