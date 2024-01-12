# Updating your cBioPortal installation

As of release **1.1.0** cBioPortal has a Database schema update mechanism which comes into play whenever the new version of the portal code relies on specific DB schema changes to be applied. The portal will automatically check if the DB schema is according to what it expects. It does so by comparing the version number of the portal code with the version number of the DB schema. If they are equal, it assumes the DB schema has been upgraded. If not, it will require the administrator to run a migration script. Below are the steps to ensure your DB schema is updated correctly.

## First time

The first time you update from release **1.0.4** (or lower) to release **1.1.0** (or higher), you should get a an error banner page after restarting your webserver. The error should state something like: 

```
Current DB schema version: xxx 
DB schema version expected by Portal: yyy
```
where `xxx` and `yyy` will be different version numbers. 

If you get `DB version expected by Portal: 0` (i.e. you are building the new release from source), you need to  add a new property to your `application.properties` file which is needed for this check. 

#### Step1

In your `application.properties` file (e.g. `<your_cbioportal_dir>/src/main/resources/application.properties`) add the following property:
```
# this is the *expected* DB version (expected by the code). Don't set it manually, it is filled by maven:
db.version=${db.version}
```

#### Step2

Compile your code again. After restarting the webserver the page should now state something like: `DB version expected by Portal: 1.1.0` (or higher), while the DB version remains as `Current DB version: -1`. 

## Running the migration script

First, make sure you have the DB connection properties correctly set in your application.properties file (see [DB connection settings here](/deployment/customization/application.properties-Reference.md#database-settings)).

**Dependencies:** the migration script is a Python script that depends on the `mysqlclient` library. If necessary, you can install it with the following commands (example for Ubuntu):
```console
sudo apt-get install python3-dev default-libmysqlclient-dev
sudo python3 -m pip install mysqlclient
```

For macOS, try the following:

```
brew install mysql-connector-c
sudo python3 -m pip install mysqlclient
```
and see <https://github.com/PyMySQL/mysqlclient-python/blob/master/README.md#prerequisites>
if problems occur during installation.

To run the migration script first go to the scripts folder
`<your_cbioportal_dir>/core/src/main/scripts` 
and then run the following command:
```console
$ python migrate_db.py
```
This should result in the following output:
```console
WARNING: This script will alter your database! Be sure to back up your data before running.
Continue running DB migration? (y/n) y
Running statments for version: 1.0.0
	Executing statement: CREATE TABLE info (DB_SCHEMA_VERSION VARCHAR(8));
	Executing statement: INSERT INTO info VALUES ("1.0.0");
Running statments for version: 1.1.0
	Executing statement: CREATE TABLE sample_list LIKE patient_list;
	Executing statement: INSERT sample_list SELECT * FROM patient_list;
	Executing statement: CREATE TABLE sample_list_list LIKE patient_list_list;
	Executing statement: INSERT sample_list_list SELECT * FROM patient_list_list;
	Executing statement: ALTER TABLE sample_list_list CHANGE PATIENT_ID SAMPLE_ID INT(11);
	Executing statement: UPDATE info SET DB_SCHEMA_VERSION="1.1.0";
...
etc
```

**Final step:** Restart your webserver or call the `/api/cache` endpoint with a `DELETE` http-request
(see [here](/deployment/customization/application.properties-Reference.md#evict-caches-with-the-apicache-endpoint) for more information).
