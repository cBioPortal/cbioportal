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

The portal supports password authentication via Google+. Before you start you need to setup a google account that will own the authentication API. Follow https://developers.google.com/identity/sign-in/web/devconsole-project to get clientID and secret. Fill it in portal.properties:
```
googleplus.consumer.key=195047654890-499gl89hj65j8d2eorqe0jvjnfaxcln0.apps.googleusercontent.com 
googleplus.consumer.secret=2jCfg4SPWdGfXF44WC588dK
```
(note: these are just examples, you need to get your own) You will also need to go to "Google+ API" and click Enable button. In case of problems make sure to enable DEBUG logging for org.springframework.social and org.springframework.security.web.authentication.


To active password authentication, then the following properties are required:
```
app.name=
authenticate=googleplus
authorization=true
```
app.name should be set to the name of the portal instance referenced in the "AUTHORITY" column of the "AUTHORITIES" table.  See the [User Authorization](User-Authorization.md) for more information.

# OncoPrint

The default view in OncoPrint ("patient" or "sample") can be set with the following option. The default is "patient".
```
oncoprint.defaultview=sample
```

## Custom annotation of driver and passenger mutations
cBioPortal allows for two different formats of custom annotation of driver and passenger mutations in the MAF files (see [file format documentation](File-Formats.md#extending-the-maf-format)). To enable the view of those annotations (just one or all both), you must specify a name for the label(s) that you want to enable (those labels appear in the oncoprint "Mutation color" menu):
```
oncoprint.custom_driver_annotation.binary.menu_label=Custom driver annotation
oncoprint.custom_driver_annotation.tiers.menu_label=Custom tiers
```

By default, when at least one of the custom annotations is enabled in portal.properties and the required columns are present in MAF files, custom annotations are automatically selected, instead of OncoKB and Hotspots. If you wish to change this default behavior and have OncoKB and Hotspots as the default selected annotations, set this parameter to false:
```
 oncoprint.custom_driver_annotation.default=false
```

By default, when at least one of the custom annotations is enabled in portal.properties and the required columns are present in MAF files, custom annotations are automatically selected. You can change this behaviour and set the following properties to "false" to automatically unselect one or both custom driver/passenger mutations (by default, those properties are set to true):
```
oncoprint.custom_driver_annotation.default=false
oncoprint.custom_driver_tiers_annotation.default=false
```

Independently of the custom annotations, OncoKB and Hotspots are always automatically selected as annotation source. If you want to disable them, set the following property to false:
```
oncoprint.oncokb_hotspots.default=false
``` 
However, you can also set the same property to "custom". This will automatically select OncoKB and Hotspots only if there are no custom annotation driver and passenger mutations:
```
oncoprint.oncokb_hotspots.default=custom
```

By default, the putative passenger mutations are not hidden. If you want to automatically hide Putative Passenger Mutations, set this property to true:
```
oncoprint.hide_passenger.default=true
```

# Civic integration

Civic integration can be turned on or off with the following property (default: true):
```
show.civic=true|false
```
The Civic API url is set to https://civic.genome.wustl.edu/api/ by default. It can be overridden using the following property:
```
civic.url=
```
