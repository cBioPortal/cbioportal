# cBioPortal Database Versioning

We follow the following logic when deciding how/when to increment the version of cBioPortal database. It's a complete modification of semantic versioning (MAJOR.MINOR.PATCH) more suitable for our purposes:

**MAJOR** : A non-backward compatible significant change in the database. Which requires the maintainer to reload and re-import all studies in the database entirely.

**MINOR** : Triggered by any of the following changes:

- Deleting a table
- Dropping a column
- Adding a constraint
- Dropping a constraint
- Renaming a table
- Renaming a column

**PATCH** : Changes that don't change existing database schemes but add new tables or columns, manipulating data.

- Creating a new table
- Adding a new column
- Removing all data from a table (truncate)
- Deleting rows from a table
- Updating data in a table
- Inserting rows into a table
