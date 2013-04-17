Clazz.declarePackage ("J.io");
Clazz.load (null, "J.io.JmolResource", ["java.util.Hashtable", "J.util.TextFormat"], function () {
c$ = Clazz.decorateAsClass (function () {
this.resource = null;
Clazz.instantialize (this, arguments);
}, J.io, "JmolResource");
Clazz.makeConstructor (c$, 
($fz = function (resource) {
this.resource = resource;
}, $fz.isPrivate = true, $fz), "~O");
c$.getResource = $_M(c$, "getResource", 
function (className, name) {
var poData = null;
{
var base = ClazzLoader.fastGetJ2SLibBase();
var fname = base + "/trans/" + name + ".po";
poData = Jmol._doAjax(fname, null, null);
}return (poData == null || !(Clazz.instanceOf (poData, Array)) ? null : J.io.JmolResource.getResourceFromPO (poData));
}, "~S,~S");
$_M(c$, "getString", 
function (string) {
{
return this.resource.get(string);
}}, "~S");
c$.getLanguage = $_M(c$, "getLanguage", 
function () {
var language = null;
{
language = (navigator.language || navigator.userLanguage);
}return language;
});
c$.getResourceFromPO = $_M(c$, "getResourceFromPO", 
function (bytes) {
if (bytes == null || bytes.length == 0) return null;
var map = null;
try {
var lines = J.util.TextFormat.split ( String.instantialize (bytes, "UTF-8"), '\n');
map =  new java.util.Hashtable ();
var mode = 0;
var msgstr = "";
var msgid = "";
for (var i = 0; i < lines.length; i++) {
var line = lines[i];
if (line.length <= 2) {
if (mode == 2 && msgstr.length != 0 && msgid.length != 0) map.put (msgid, msgstr);
} else if (line.indexOf ("msgid") == 0) {
mode = 1;
msgid = J.io.JmolResource.fix (line);
} else if (line.indexOf ("msgstr") == 0) {
mode = 2;
msgstr = J.io.JmolResource.fix (line);
} else if (mode == 1) {
msgid += J.io.JmolResource.fix (line);
} else if (mode == 2) {
msgstr += J.io.JmolResource.fix (line);
}}
} catch (e) {
if (Clazz.exceptionOf (e, java.io.UnsupportedEncodingException)) {
} else {
throw e;
}
}
return (map == null || map.size () == 0 ? null :  new J.io.JmolResource (map));
}, "~A");
c$.fix = $_M(c$, "fix", 
($fz = function (line) {
return J.util.TextFormat.simpleReplace (line.substring (line.indexOf ("\"") + 1, line.lastIndexOf ("\"")), "\\n", "\n");
}, $fz.isPrivate = true, $fz), "~S");
});
