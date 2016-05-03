This page describes the main properties within portal.properties.

# Database Settings

```
db.user=
db.password=
db.host=[e.g. localhost to connect via socket, or e.g. 127.0.0.1:3307 to connect to a different port like 3307. Used by Java data import layer]
db.portal_db_name=[the database name in myslq, e.g. cbiodb]
db.driver=[this is the name of your JDBC driver, e.g., com.mysql.jdbc.Driver]
db.connection_string=jdbc:mysql://localhost/
db.tomcat_resource_name=jdbc/cbioportal
```
db.tomcat_resource_name is required in order to work with the tomcat database connection pool and should have the default value jdbc/cbioportal in order to work correctly with the your WAR file.

# Segment File URL

This is a root URL to where segment files can be found.  This is used when you want to provide segment file viewing via external tools such as [IGV](http://www.broadinstitute.org/igv/).

```
segfile.url=
```

# Bitly API Username and Key

The following properties are used to provide shortened bookmarks to the cBioPortal:

```
bitly.user=
bitly.api_key=
```

To obtain a bitly username and key, first register at:  https://bitly.com/

Then, go to:  https://bitly.com/a/your_api_key

**Note:**  If you are developing on a local machine, and using localhost, the bitly URL shortening service will not work.  This is because bitly will not shorten URLs for localhost.  Once you deploy to your final server, the issue should go away.

# Google Analytics

If you so desire, the following property is used to track your site's usage via google analytics.
```
google_analytics_profile_id
```

# Password Authentication

The portal supports password authentication via Google+.  To active password authentication, then the following properties are required:
```
app.name=
authenticate=googleplus
authorization=true
```
app.name should be set to the name of the portal instance referenced in the "AUTHORITY" column of the "AUTHORITIES" table.  See the [User Authorization](User-Authorization.md) for more information.

