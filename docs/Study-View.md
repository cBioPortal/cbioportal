# Study View Customization with Priority Data
Example of study view in public portal: http://www.cbioportal.org/study.do?cancer_study_id=lgg_tcga#summary

## Priorities

The priority system is represented with a final score.
The higher the final (numeric) score, the higher priority assigned.

The default score is 1.

To promote certain chart in study view, please increase priority in the database to a certain number.
The higher the score, the higher priority it will be displayed in the study view.
If you want to hide chart, please set the priority to 0.
For combination chart, as long as one of the clinical attributes has been set to 0, it will be hidden.

Currently, we preassigned priority to few charts, but as long as you assign a priority in the database except than 1, these preassigned priorities will be overwritten.

**CANCER_TYPE**:          3000,
**CANCER_TYPE_DETAILED**: 2000

**Overall survival plot**:                400 (This is combination of OS_MONTH and OS_STATUS),
**Disease Free Survival Plot**:           300 (This is combination of DFS_MONTH and DFS_STATUS),
**Mutation Count vs. CNA Scatter Plot**:  200

**Mutated Genes Table**:          90,
**CNA Genes Table**:              80,
**study_id**:                     70,
**With Mutation Data Pie Chart**: 60,
**With CNA Data Pie Chart**:      50,
**`#` of Samples Per Patient**:     40,
**Mutation Count Bar Chart**:     30,
**CNA Bar Chart**:                20

**GENDER, SEX**:  9,
**AGE**:          8

## Layout
The layout is determined mainly based on priority. Higher priority will promote chart closer to the left-top. But if the layout solely based on priority, sometimes the layout may not be displayed as expected. See the example below. 
![image](https://user-images.githubusercontent.com/2900303/28311265-626d4cb2-6baf-11e7-8486-7873a4c42734.png)

In order to improve the layout, we added a layout algorithm layer. The algorithm tries to fit all charts into a 2 by 2 matrix (Mutated Genes Table occupies 2 by 2 space). When a chart can not be fitted in the first matrix, the second matrixed will be generated. And the second matrix will have lower priority than the first one. If later chart can fit into the first matrix, then its priority will be promoted.

![image](https://user-images.githubusercontent.com/5400599/28329358-8e91312a-6bb7-11e7-97aa-a56de2abf505.png)

In the example above, `With Mutated Data` and `With CNA Data`  will then be promoted to the empty space (The dashed border boxes in the above image) and the following image shows the expected result.

![image](https://user-images.githubusercontent.com/2900303/28310997-88430a54-6bae-11e7-880d-c7c99d8a2e56.png)
