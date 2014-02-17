Clazz.declarePackage ("J.i18n");
Clazz.load (null, "J.i18n.Resource", ["J.util.TextFormat"], function () {
c$ = Clazz.decorateAsClass (function () {
this.resource = null;
this.className = null;
Clazz.instantialize (this, arguments);
}, J.i18n, "Resource");
Clazz.makeConstructor (c$, 
($fz = function (resource, className) {
this.resource = resource;
this.className = className;
}, $fz.isPrivate = true, $fz), "~O,~S");
c$.getResource = $_M(c$, "getResource", 
function (className, name) {
var poData = null;
{
var base = ClazzLoader.fastGetJ2SLibBase(); var fname = base +
"/trans/" + name + ".po"; poData = Jmol._doAjax(fname, null,
null); if (!poData) return null;
poData = poData.toString();
}try {
return (poData == null ? null : J.i18n.Resource.getResourceFromPO (poData));
} catch (e) {
if (Clazz.exceptionOf (e, Exception)) {
return null;
} else {
throw e;
}
}
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
language = Jmol.featureDetection.getDefaultLanguage().replace(/-/g,'_');
}return language;
});
c$.getResourceFromPO = $_M(c$, "getResourceFromPO", 
function (data) {
{
if (data == null || data.length == 0) return null;
var map = null; try {
var lines = J.util.TextFormat.split(data, '\n');
map = new java.util.Hashtable();
var mode = 0; var msgstr = ""; var msgid = "";
for (var i = 0; i < lines.length; i++) {
var line = lines[i];
if (line.length <= 2) {
if (mode == 2 && msgstr.length != 0 && msgid.length != 0) map.put(msgid, msgstr);
} else if (line.indexOf("msgid") == 0) {
mode = 1; msgid = J.i18n.Resource.fix(line);
} else if (line.indexOf("msgstr") == 0) {
mode = 2; msgstr = J.i18n.Resource.fix(line);
} else if (mode == 1) {
msgid += J.i18n.Resource.fix(line);
} else if (mode == 2) {
msgstr += J.i18n.Resource.fix(line);
}
}
} catch (e) { }
return (map == null || map.size() == 0 ? null : new J.i18n.Resource(map));
}}, "~S");
c$.fix = $_M(c$, "fix", 
function (line) {
return J.util.TextFormat.simpleReplace (line.substring (line.indexOf ("\"") + 1, line.lastIndexOf ("\"")), "\\n", "\n");
}, "~S");
});
