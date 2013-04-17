Clazz.declarePackage ("J.popup");
Clazz.load (null, "J.popup.PopupResource", ["java.io.BufferedReader", "$.StringReader", "java.util.Properties", "J.i18n.GT"], function () {
c$ = Clazz.decorateAsClass (function () {
this.structure = null;
this.words = null;
Clazz.instantialize (this, arguments);
}, J.popup, "PopupResource");
Clazz.makeConstructor (c$, 
function (menuStructure, menuText) {
this.structure =  new java.util.Properties ();
this.words =  new java.util.Properties ();
this.buildStructure (menuStructure);
this.localize (menuStructure != null, menuText);
}, "~S,java.util.Properties");
$_M(c$, "getMenuAsText", 
function (title) {
return null;
}, "~S");
$_M(c$, "getStructure", 
function (key) {
return this.structure.getProperty (key);
}, "~S");
$_M(c$, "getWord", 
function (key) {
var str = this.words.getProperty (key);
return (str == null ? key : str);
}, "~S");
$_M(c$, "setStructure", 
function (slist) {
if (slist == null) return;
var br =  new java.io.BufferedReader ( new java.io.StringReader (slist));
var line;
var pt;
try {
while ((line = br.readLine ()) != null) {
if (line.length == 0 || line.charAt (0) == '#') continue;
pt = line.indexOf ("=");
if (pt < 0) {
pt = line.length;
line += "=";
}var name = line.substring (0, pt).trim ();
var value = line.substring (pt + 1).trim ();
var label = null;
if ((pt = name.indexOf ("|")) >= 0) {
label = name.substring (pt + 1).trim ();
name = name.substring (0, pt).trim ();
}if (name.length == 0) continue;
if (value.length > 0) this.structure.setProperty (name, value);
if (label != null && label.length > 0) this.words.setProperty (name, J.i18n.GT._ (label));
}
} catch (e) {
if (Clazz.exceptionOf (e, Exception)) {
} else {
throw e;
}
}
try {
br.close ();
} catch (e) {
if (Clazz.exceptionOf (e, Exception)) {
} else {
throw e;
}
}
}, "~S");
$_M(c$, "addItems", 
function (itemPairs) {
var previous = "";
for (var i = 0; i < itemPairs.length; i++) {
var str = itemPairs[i][1];
if (str == null) str = previous;
previous = str;
this.structure.setProperty (itemPairs[i][0], str);
}
}, "~A");
$_M(c$, "localize", 
($fz = function (haveUserMenu, menuText) {
var wordContents = this.getWordContents ();
for (var i = 0; i < wordContents.length; i++) {
var item = wordContents[i++];
var word = this.words.getProperty (item);
if (word == null) word = wordContents[i];
this.words.setProperty (item, word);
if (menuText != null && item.indexOf ("Text") >= 0) menuText.setProperty (item, word);
}
}, $fz.isPrivate = true, $fz), "~B,java.util.Properties");
});
