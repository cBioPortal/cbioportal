# User Authorization

This step is only required if you intend on running an instance of the portal that supports user authorization.

Two tables need to be populated in order to support user authorization.

## Table:  users

This table contains all the users that have authorized access to the instance of the portal.  The table requires a user's email address, name, and integer flag indicating if the account is enabled.

```
mysql> describe users;
+---------+--------------+------+-----+---------+-------+
| Field   | Type         | Null | Key | Default | Extra |
+---------+--------------+------+-----+---------+-------+
| EMAIL   | varchar(128) | NO   | PRI | NULL    |       |
| NAME    | varchar(255) | NO   |     | NULL    |       |
| ENABLED | tinyint(1)   | NO   |     | NULL    |       |
+---------+--------------+------+-----+---------+-------+
3 rows in set (0.00 sec)
```

An example entry would be:

```
mysql> select * from users where email = "john.smith@gmail.com";
+--------------------------+----------------+---------+
| EMAIL                    | NAME           | ENABLED |
+--------------------------+----------------+---------+
| john.smith@gmail.com     | John Smith     |       1 | 
+--------------------------+----------------+---------+
1 row in set (0.00 sec)
```

Note, if the ENABLED value is set to 0, the user will be able to login to the portal, but will see no studies.

You need to add users via MySQL directly.  For example:

```
INSERT INTO cbioportal.users (EMAIL, NAME, ENABLED) 
    VALUES ('john.smith@gmail.com', 'John Smith', 1);
```

## Table:  authorities

This table contains the list of cancer studies that each user is authorized to view.  The table requires a user email address and an authority (e.g. cancer study) granted to the user.

```
mysql> describe authorities;
+-----------+--------------+------+-----+---------+-------+
| Field     | Type         | Null | Key | Default | Extra |
+-----------+--------------+------+-----+---------+-------+
| EMAIL     | varchar(128) | NO   |     | NULL    |       | 
| AUTHORITY | varchar(50)  | NO   |     | NULL    |       | 
+-----------+--------------+------+-----+---------+-------+
2 rows in set (0.00 sec)
```

Some example entries would be:

```
mysql> select * from authorities where email = "john.smith@gmail.com";
+--------------------------+---------------------------+
| EMAIL                    | AUTHORITY                 |
+--------------------------+---------------------------+
| john.smith@gmail.com     | cbioportal:CANCER_STUDY_1 | 
| john.smith@gmail.com     | cbioportal:CANCER_STUDY_2 | 
| john.smith@gmail.com     | cbioportal:CANCER_STUDY_3 | 
+--------------------------+---------------------------+
5 rows in set (0.00 sec)
```
The value in the EMAIL column should be the same email address contained in the USER table.  

The value in the AUTHORITY column is made of two parts:

* The first part is the name of your portal instance.  This name should also match the `app.name` property found in the `application.properties` file.  
* Following a colon delimiter, the second part is the [cancer_study_identifier](../../File-Formats.md#cancer-study) of the cancer study this user has rights to access. 

**If the user has rights to all available cancer studies, a single entry with the keyword `app.name:` + "ALL" is sufficient (so e.g. "cbioportal:ALL").**

You need to add users via MySQL directly.  For example:

```
mysql> INSERT INTO cbioportal.authorities (EMAIL, AUTHORITY) VALUES
    ('john.smith@gmail.com', 'cbioportal:CANCER_STUDY_1');
```
**Important Note:**  The cancer study identifier is *not* CASE sEnsitive. So it can be UPPER CASE, or just how it is stored in the `cancer_study` table.
Changes to these tables become effective the *next* time the user logs in. 

## Using groups

It is also possible to define groups and assign multiple studies and users to a group. You can add a group name to the `cancer_study` table `GROUPS` column. This same group name can be used in the `AUTHORITY` column of the `authorities` table mentioned above. 

### Example: 

We want to create the group "TEST_GROUP1" and assign two existing studies to it and give our user 'john.smith@gmail.com' access to this group of studies. Steps:

1- Find your studies in table `cancer_study`
```
mysql> select CANCER_STUDY_ID, CANCER_STUDY_IDENTIFIER,GROUPS from cancer_study where CANCER_STUDY_ID in (93,94);
+-----------------+-------------------------+-------------+
| CANCER_STUDY_ID | CANCER_STUDY_IDENTIFIER | GROUPS      |
+-----------------+-------------------------+-------------+
|              93 | acc_tcga                | GROUPB      |
|              94 | brca_tcga               |             |
+-----------------+-------------------------+-------------+
```
2- Update the `GROUPS` field, adding your "TEST_GROUP1" to it. :warning: This is a `;` separated column, so if you want a study to be part of multiple groups, separate them with `;`.  
```
mysql> update cancer_study set GROUPS='TEST_GROUP1' where CANCER_STUDY_ID = 94;
```

If `GROUPS` already has a value (like for study 93 in example above) then add ";TEST_GROUP1" to ensure existing groups are not ovewritten.
```
mysql> update cancer_study set GROUPS=concat(GROUPS,';TEST_GROUP1') where CANCER_STUDY_ID = 93;
```
3- Check the result:
```
mysql> select CANCER_STUDY_ID, CANCER_STUDY_IDENTIFIER,GROUPS from cancer_study where CANCER_STUDY_ID in (93,94);
+-----------------+-------------------------+--------------------+
| CANCER_STUDY_ID | CANCER_STUDY_IDENTIFIER | GROUPS             |
+-----------------+-------------------------+--------------------+
|              93 | acc_tcga                | GROUPB;TEST_GROUP1 |
|              94 | brca_tcga               | TEST_GROUP1        |
+-----------------+-------------------------+--------------------+
```
4- Add the group to user 'john.smith@gmail.com', using `app.name:` + "TEST_GROUP1" like so:
```
mysql> INSERT INTO cbioportal.authorities (EMAIL, AUTHORITY) VALUES
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

```
+-----------------+-------------------------+--------------------+
| CANCER_STUDY_ID | CANCER_STUDY_IDENTIFIER | GROUPS             |
+-----------------+-------------------------+--------------------+
|              93 | acc_tcga                | GROUPB;TEST_GROUP1 |
|              94 | brca_tcga               | TEST_GROUP1;PUBLIC |
+-----------------+-------------------------+--------------------+
```

In this case, the study `brca_tcga` will be visible to *any authenticated user* while the study `acc_tcga` will be visible only to users configured to be part of `GROUPB` or `TEST_GROUP1`
