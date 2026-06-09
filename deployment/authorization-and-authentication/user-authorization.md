# User Authorization

This step is only required if you intend on running an instance of the portal that supports user authorization.

Two tables need to be populated in order to support user authorization.

## Table:  users

This table contains all the users that have authorized access to the instance of the portal.

| column | type |
|--------|------|
| email | String |
| name | String |
| enabled | Bool |

An example entry would be: `email = john.smith@gmail.com`, `name = John Smith`, `enabled = true`.

Note, if the `enabled` value is set to 0, the user will be able to login to the portal, but will see no studies.

You need to add users via the database directly.  For example:

```
INSERT INTO cbioportal.users (email, name, enabled) 
    VALUES ('john.smith@gmail.com', 'John Smith', 1);
```

## Table:  authorities

This table contains the list of cancer studies that each user is authorized to view.  The table requires a user email address and an authority (e.g. cancer study) granted to the user.

| column | type |
|--------|------|
| email | String |
| authority | String |

Some example entries would be:

| email | authority |
|-------|-----------|
| john.smith@gmail.com | cbioportal:CANCER_STUDY_1 |
| john.smith@gmail.com | cbioportal:CANCER_STUDY_2 |
| john.smith@gmail.com | cbioportal:CANCER_STUDY_3 |

The value in the EMAIL column should be the same email address contained in the USER table.  

The value in the AUTHORITY column is made of two parts:

* The first part is the name of your portal instance.  This name should also match the `app.name` property found in the `application.properties` file.  
* Following a colon delimiter, the second part is the [cancer_study_identifier](../../File-Formats.md#cancer-study) of the cancer study this user has rights to access. 

**If the user has rights to all available cancer studies, a single entry with the keyword `app.name:` + "ALL" is sufficient (so e.g. "cbioportal:ALL").**

You need to add users via the database directly.  For example:

```sql
INSERT INTO cbioportal.authorities (email, authority) VALUES
    ('john.smith@gmail.com', 'cbioportal:CANCER_STUDY_1');
```
**Important Note:**  The cancer study identifier is *not* CASE sEnsitive. So it can be UPPER CASE, or just how it is stored in the `cancer_study` table.
Changes to these tables become effective the *next* time the user logs in. 

## Using groups

It is also possible to define groups and assign multiple studies and users to a group. You can add a group name to the `cancer_study` table `GROUPS` column. This same group name can be used in the `AUTHORITY` column of the `authorities` table mentioned above. 

### Example: 

We want to create the group "TEST_GROUP1" and assign two existing studies to it and give our user 'john.smith@gmail.com' access to this group of studies. Steps:

1- Find your studies in table `cancer_study`

| cancer_study_id | cancer_study_identifier | groups |
|-----------------|-------------------------|--------|
| 93 | acc_tcga | GROUPB |
| 94 | brca_tcga | |

2- Update the `groups` field in the `cancer_study` table, adding your "TEST_GROUP1" to it. :warning: This is a `;` separated column, so if you want a study to be part of multiple groups, separate them with `;`.

If `groups` already has a value (like for study 93 in example above) then add ";TEST_GROUP1" to ensure existing groups are not ovewritten.

3- Check the result:

| cancer_study_id | cancer_study_identifier | groups |
|-----------------|-------------------------|--------|
| 93 | acc_tcga | GROUPB;TEST_GROUP1 |
| 94 | brca_tcga | TEST_GROUP1 |

4- Add the group to user 'john.smith@gmail.com', using `app.name:` + "TEST_GROUP1" like so:

```sql
INSERT INTO cbioportal.authorities (email, authority) VALUES
    ('john.smith@gmail.com', 'cbioportal:TEST_GROUP1');
```

After **next login**, the user 'john.smith@gmail.com' will have access to these two studies. 

## Configuring PUBLIC studies

To enable a set of public studies that should be visible to all users, without the need to configure this for each user in the `authorities` and `users` tables, you can set the property `always_show_study_group` in **application.properties** file. For example, you can set:

```
always_show_study_group=PUBLIC
```

This will enable the word "PUBLIC" to be used in the column `GROUPS` of the table `cancer_study` to indicate which studies should be always shown to *any authenticated user*, regardless of authorization configurations.

### Example:

To reuse the example table above, let's assume the property `always_show_study_group` is set as indicated above and the `cancer_study` table contents are set to the following: 

| cancer_study_id | cancer_study_identifier | groups |
|-----------------|-------------------------|--------|
| 93 | acc_tcga | GROUPB;TEST_GROUP1 |
| 94 | brca_tcga | TEST_GROUP1;PUBLIC |

In this case, the study `brca_tcga` will be visible to *any authenticated user* while the study `acc_tcga` will be visible only to users configured to be part of `GROUPB` or `TEST_GROUP1`
