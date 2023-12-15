# Study View Customization

## How does the study view organize the charts

Study view page is fully responsive, it will try to fit as many charts as possible based on the browser's width and height.

The layout of a chart is determined mainly based on priority. Higher priority will promote chart closer to the left-top.

In order to improve the layout, we added a layout algorithm layer. The study view page is using grid layout. All charts will be put into 2-dimensional systems. For example, pie chart, by default, takes 1 block and bar chart uses two blocks. All charts will be placed from left to right, top to bottom. In order to prevent misalignment, we promote small charts to fit into the space.

For logged-in(authenticated) users, charts layout is saved to users profile i.e, whenever user tries to re-visits the same url, previously saved layout will be loaded.
Please note for optimal performance, the portal will initially display 20 clinical charts by default. These charts are prioritized from high to low. Additional clinical charts are initially hidden but can be easily selected by users through the 'Add charts' dropdown menu.

## Study View Customization with Priority Data
Example of study view in public portal: https://www.cbioportal.org/study?id=acc_tcga,lgg_tcga#summary

## Priorities

The priority system is represented with a final score.
The higher the final (numeric) score, the higher priority assigned.

The default score is 1.

To promote certain chart in study view, please increase priority in the database to a certain number.
The higher the score, the higher priority it will be displayed in the study view.
If you want to hide chart, please set the priority to 0.
For combination chart, as long as one of the clinical attributes has been set to 0, it will be hidden.

To disable the chart, set the priority to -1.(Currently disables charts for single clinical attributes only)

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
