Clazz.declarePackage ("J.appletjs");
Clazz.load (["java.util.Hashtable"], "J.appletjs.JmolAppletRegistry", ["J.util.Logger", "$.TextFormat"], function () {
c$ = Clazz.declareType (J.appletjs, "JmolAppletRegistry");
c$.checkIn = $_M(c$, "checkIn", 
function (name, applet) {
{
if (Jmol._htRegistry) {J.appletjs.JmolAppletRegistry.htRegistry = Jmol._htRegistry} else {Jmol._htRegistry = J.appletjs.JmolAppletRegistry.htRegistry};
}J.appletjs.JmolAppletRegistry.cleanRegistry ();
if (name != null) {
J.util.Logger.info ("AppletRegistry.checkIn(" + name + ")");
J.appletjs.JmolAppletRegistry.htRegistry.put (name, applet);
}if (J.util.Logger.debugging) {
for (var entry, $entry = J.appletjs.JmolAppletRegistry.htRegistry.entrySet ().iterator (); $entry.hasNext () && ((entry = $entry.next ()) || true);) {
var theApplet = entry.getKey ();
J.util.Logger.debug (theApplet + " " + entry.getValue ());
}
}}, "~S,J.api.JmolSyncInterface");
c$.checkOut = $_M(c$, "checkOut", 
function (name) {
J.appletjs.JmolAppletRegistry.htRegistry.remove (name);
}, "~S");
c$.findApplets = $_M(c$, "findApplets", 
function (appletName, mySyncId, excludeName, apps) {
if (appletName != null && appletName.indexOf (",") >= 0) {
var names = J.util.TextFormat.split (appletName, ',');
for (var i = 0; i < names.length; i++) J.appletjs.JmolAppletRegistry.findApplets (names[i], mySyncId, excludeName, apps);

return;
}var ext = "__" + mySyncId + "__";
if (appletName == null || appletName.equals ("*") || appletName.equals (">")) {
for (var appletName2, $appletName2 = J.appletjs.JmolAppletRegistry.htRegistry.keySet ().iterator (); $appletName2.hasNext () && ((appletName2 = $appletName2.next ()) || true);) {
if (!appletName2.equals (excludeName)) {
apps.addLast (appletName2);
}}
return;
}if (appletName.indexOf ("__") < 0) appletName += ext;
if (!J.appletjs.JmolAppletRegistry.htRegistry.containsKey (appletName)) appletName = "jmolApplet" + appletName;
if (!appletName.equals (excludeName) && J.appletjs.JmolAppletRegistry.htRegistry.containsKey (appletName)) {
apps.addLast (appletName);
}}, "~S,~S,~S,J.util.JmolList");
c$.cleanRegistry = $_M(c$, "cleanRegistry", 
($fz = function () {
}, $fz.isPrivate = true, $fz));
c$.htRegistry = c$.prototype.htRegistry =  new java.util.Hashtable ();
});
