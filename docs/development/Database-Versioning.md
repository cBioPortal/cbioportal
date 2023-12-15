# cBioPortal Database Versioning

We follow the following logic when deciding how/when to increment the version of cBioPortal database. It's a complete modification of semantic versioning (MAJOR.MINOR.PATCH) more suitable for our purposes:

**MAJOR** : A non-backward compatible significant change in the database. Which requires the maintainer to reload and re-import all studies in the database entirely.

**MINOR** : Including deleting, renaming tables or columns, or changing constraints

Delete a table

```SQL
DROP TABLE t ;
```

Drop column c from the table

```SQL
ALTER TABLE t DROP COLUMN c ;
```

Add a constraint

```SQL
ALTER TABLE t ADD constraint;
```

Drop a constraint

```SQL
ALTER TABLE t DROP constraint;
```

Rename a table from t1 to t2

```SQL
ALTER TABLE t1 RENAME TO t2;
```

Rename column c1 to c2

```SQL
ALTER TABLE t1 RENAME c1 TO c2 ;
```

**PATCH** : Changes that don't change existing database schemes but add new tables or columns, manipulating data.

Create a new table

```SQL
CREATE TABLE t (
     id INT PRIMARY KEY,
);
```

Add a new column to the table

```SQL
ALTER TABLE t ADD column;
```

Remove all data in a table

```SQL
TRUNCATE TABLE t;
```

Delete data from a table:

```SQL
DELETE from TABLE t;
```

Update data in a table:

```SQL
UPDATE TABLE t SET ...;
```

Insert multiple rows into a table

```SQL
INSERT INTO t(column_list)
VALUES (value_list), 
       (value_list), …;
```
