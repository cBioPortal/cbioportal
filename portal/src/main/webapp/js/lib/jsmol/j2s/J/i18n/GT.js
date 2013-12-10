Clazz.declarePackage ("J.i18n");
Clazz.load (["java.util.Hashtable", "J.i18n.Language", "$.Resource"], "J.i18n.GT", ["java.text.MessageFormat", "J.util.Logger"], function () {
c$ = Clazz.decorateAsClass (function () {
this.resources = null;
this.resourceCount = 0;
this.doTranslate = true;
this.language = null;
Clazz.instantialize (this, arguments);
}, J.i18n, "GT");
Clazz.makeConstructor (c$, 
function () {
});
Clazz.makeConstructor (c$, 
function (langCode) {
{
}this.resources = null;
this.resourceCount = 0;
($t$ = J.i18n.GT.$getTextWrapper = this, J.i18n.GT.prototype.$getTextWrapper = J.i18n.GT.$getTextWrapper, $t$);
if (langCode != null && langCode.length == 0) langCode = "none";
if (langCode != null) this.language = langCode;
if ("none".equals (this.language)) this.language = null;
if (this.language == null) this.language = J.i18n.Resource.getLanguage ();
if (this.language == null) this.language = "en";
var la = this.language;
var la_co = null;
var la_co_va = null;
var i = this.language.indexOf ("_");
if (i >= 0) {
la = la.substring (0, i);
la_co = this.language;
if ((i = la_co.indexOf ("_", ++i)) >= 0) {
la_co = la_co.substring (0, i);
la_co_va = this.language;
}}if ((this.language = this.getSupported (la_co_va)) == null && (this.language = this.getSupported (la_co)) == null && (this.language = this.getSupported (la)) == null) {
this.language = "en";
System.out.println (this.language + " not supported -- using en");
return;
}la_co_va = null;
la_co = null;
switch (this.language.length) {
default:
la_co_va = this.language;
la_co = this.language.substring (0, 5);
la = this.language.substring (0, 2);
break;
case 5:
la_co = this.language;
la = this.language.substring (0, 2);
break;
case 2:
la = this.language;
break;
}
la_co = this.getSupported (la_co);
la = this.getSupported (la);
if (la === la_co || "en_US".equals (la)) la = null;
if (la_co === la_co_va) la_co = null;
if ("en_US".equals (la_co)) return;
if (J.i18n.GT.allowDebug && J.util.Logger.debugging) J.util.Logger.debug ("Instantiating gettext wrapper for " + this.language + " using files for language:" + la + " country:" + la_co + " variant:" + la_co_va);
if (!J.i18n.GT.$ignoreApplicationBundle) this.addBundles ("Jmol", la_co_va, la_co, la);
this.addBundles ("JmolApplet", la_co_va, la_co, la);
}, "~S");
c$.getLanguageList = $_M(c$, "getLanguageList", 
function (gt) {
if (J.i18n.GT.languageList == null) {
if (gt == null) gt = J.i18n.GT.getTextWrapper ();
gt.createLanguageList ();
}return J.i18n.GT.languageList;
}, "J.i18n.GT");
c$.getLanguage = $_M(c$, "getLanguage", 
function () {
return J.i18n.GT.getTextWrapper ().language;
});
c$.ignoreApplicationBundle = $_M(c$, "ignoreApplicationBundle", 
function () {
($t$ = J.i18n.GT.$ignoreApplicationBundle = true, J.i18n.GT.prototype.$ignoreApplicationBundle = J.i18n.GT.$ignoreApplicationBundle, $t$);
});
c$.setDoTranslate = $_M(c$, "setDoTranslate", 
function (TF) {
var b = J.i18n.GT.getDoTranslate ();
J.i18n.GT.getTextWrapper ().doTranslate = TF;
return b;
}, "~B");
c$.getDoTranslate = $_M(c$, "getDoTranslate", 
function () {
return J.i18n.GT.getTextWrapper ().doTranslate;
});
c$._ = $_M(c$, "_", 
function (string) {
return J.i18n.GT.getTextWrapper ().getString (string, null);
}, "~S");
c$._ = $_M(c$, "_", 
function (string, item) {
return J.i18n.GT.getTextWrapper ().getString (string, [item]);
}, "~S,~S");
c$._ = $_M(c$, "_", 
function (string, item) {
return J.i18n.GT.getTextWrapper ().getString (string, [Integer.$valueOf (item)]);
}, "~S,~N");
c$._ = $_M(c$, "_", 
function (string, objects) {
return J.i18n.GT.getTextWrapper ().getString (string, objects);
}, "~S,~A");
c$.escapeHTML = $_M(c$, "escapeHTML", 
function (msg) {
var ch;
for (var i = msg.length; --i >= 0; ) if (((ch = msg.charAt (i))).charCodeAt (0) > 0x7F) {
msg = msg.substring (0, i) + "&#" + ((ch).charCodeAt (0)) + ";" + msg.substring (i + 1);
}
return msg;
}, "~S");
c$.getTextWrapper = $_M(c$, "getTextWrapper", 
($fz = function () {
return (J.i18n.GT.$getTextWrapper == null ? ($t$ = J.i18n.GT.$getTextWrapper =  new J.i18n.GT (null), J.i18n.GT.prototype.$getTextWrapper = J.i18n.GT.$getTextWrapper, $t$) : J.i18n.GT.$getTextWrapper);
}, $fz.isPrivate = true, $fz));
$_M(c$, "createLanguageList", 
($fz = function () {
var wasTranslating = this.doTranslate;
this.doTranslate = false;
($t$ = J.i18n.GT.languageList = J.i18n.Language.getLanguageList (), J.i18n.GT.prototype.languageList = J.i18n.GT.languageList, $t$);
this.doTranslate = wasTranslating;
}, $fz.isPrivate = true, $fz));
$_M(c$, "getSupported", 
($fz = function (code) {
if (code == null) return null;
var s = J.i18n.GT.htLanguages.get (code);
if (s != null) return (s.length == 0 ? null : s);
s = J.i18n.Language.getSupported (J.i18n.GT.getLanguageList (this), code);
J.i18n.GT.htLanguages.put (code, (s == null ? "" : s));
return s;
}, $fz.isPrivate = true, $fz), "~S");
$_M(c$, "addBundles", 
($fz = function (type, la_co_va, la_co, la) {
try {
var className = "J.translation." + type + ".";
if (la_co_va != null) this.addBundle (className, la_co_va);
if (la_co != null) this.addBundle (className, la_co);
if (la != null) this.addBundle (className, la);
} catch (exception) {
if (Clazz.exceptionOf (exception, Exception)) {
if (J.i18n.GT.allowDebug) J.util.Logger.errorEx ("Some exception occurred!", exception);
this.resources = null;
this.resourceCount = 0;
} else {
throw exception;
}
}
}, $fz.isPrivate = true, $fz), "~S,~S,~S,~S");
$_M(c$, "addBundle", 
($fz = function (className, name) {
var resource = J.i18n.Resource.getResource (className, name);
if (resource != null) {
if (this.resources == null) {
this.resources =  new Array (8);
this.resourceCount = 0;
}this.resources[this.resourceCount] = resource;
this.resourceCount++;
if (J.i18n.GT.allowDebug) J.util.Logger.debug ("GT adding " + className);
}}, $fz.isPrivate = true, $fz), "~S,~S");
$_M(c$, "getString", 
($fz = function (string, objects) {
var trans = null;
if (this.doTranslate) {
for (var bundle = this.resourceCount; --bundle >= 0; ) {
trans = this.resources[bundle].getString (string);
if (trans != null) {
string = trans;
break;
}}
if (this.resourceCount > 0 && trans == null && J.i18n.GT.allowDebug && J.util.Logger.debugging) J.util.Logger.debug ("No trans, using default: " + string);
}if (trans == null) {
if (string.startsWith ("[")) string = string.substring (string.indexOf ("]") + 1);
 else if (string.endsWith ("]")) string = string.substring (0, string.indexOf ("["));
}return (objects == null ? string : java.text.MessageFormat.format (string, objects));
}, $fz.isPrivate = true, $fz), "~S,~A");
Clazz.defineStatics (c$,
"$ignoreApplicationBundle", false,
"$getTextWrapper", null,
"languageList", null,
"allowDebug", false);
c$.htLanguages = c$.prototype.htLanguages =  new java.util.Hashtable ();
});
