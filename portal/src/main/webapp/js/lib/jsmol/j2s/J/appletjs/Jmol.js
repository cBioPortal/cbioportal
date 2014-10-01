Clazz.declarePackage ("J.appletjs");
Clazz.load (["javajs.api.JSInterface", "JU.GenericApplet", "java.util.Hashtable"], "J.appletjs.Jmol", ["JU.PT", "JU.Logger", "$.Parser"], function () {
c$ = Clazz.decorateAsClass (function () {
this.htParams = null;
Clazz.instantialize (this, arguments);
}, J.appletjs, "Jmol", JU.GenericApplet, javajs.api.JSInterface);
Clazz.prepareFields (c$, function () {
this.htParams =  new java.util.Hashtable ();
});
Clazz.makeConstructor (c$, 
function (vwrOptions) {
Clazz.superConstructor (this, J.appletjs.Jmol, []);
if (vwrOptions == null) vwrOptions =  new java.util.Hashtable ();
this.vwrOptions = vwrOptions;
for (var entry, $entry = vwrOptions.entrySet ().iterator (); $entry.hasNext () && ((entry = $entry.next ()) || true);) this.htParams.put (entry.getKey ().toLowerCase (), entry.getValue ());

this.documentBase = "" + vwrOptions.get ("documentBase");
this.codeBase = "" + vwrOptions.get ("codePath");
this.isJS = true;
this.init (this);
}, "java.util.Map");
Clazz.overrideMethod (c$, "setStereoGraphics", 
function (isStereo) {
{
if (isStereo)
return vwr.apiPlatform.context;
}return null;
}, "~B");
Clazz.overrideMethod (c$, "initOptions", 
function () {
this.vwrOptions.remove ("debug");
this.vwrOptions.put ("fullName", this.fullName);
this.haveDocumentAccess = "true".equalsIgnoreCase ("" + this.getValue ("allowjavascript", "true"));
this.mayScript = true;
});
Clazz.overrideMethod (c$, "getJmolParameter", 
function (paramName) {
var o = this.htParams.get (paramName.toLowerCase ());
return (o == null ? null : "" + o);
}, "~S");
Clazz.overrideMethod (c$, "doSendJsTextStatus", 
function (message) {
System.out.println (message);
}, "~S");
Clazz.overrideMethod (c$, "doSendJsTextareaStatus", 
function (message) {
System.out.println (message);
}, "~S");
Clazz.overrideMethod (c$, "doFunctionXY", 
function (functionName, nX, nY) {
var fxy =  Clazz.newFloatArray (Math.abs (nX), Math.abs (nY), 0);
if (!this.mayScript || !this.haveDocumentAccess || nX == 0 || nY == 0) return fxy;
try {
if (nX > 0 && nY > 0) {
for (var i = 0; i < nX; i++) for (var j = 0; j < nY; j++) {
{
fxy[i][j] = eval(functionName)(this.htmlName, i, j);
}}

} else if (nY > 0) {
var data;
{
data = eval(functionName)(this.htmlName, nX, nY);
}nX = Math.abs (nX);
var fdata =  Clazz.newFloatArray (nX * nY, 0);
JU.Parser.parseStringInfestedFloatArray (data, null, fdata);
for (var i = 0, ipt = 0; i < nX; i++) {
for (var j = 0; j < nY; j++, ipt++) {
fxy[i][j] = fdata[ipt];
}
}
} else {
{
data = eval(functionName)(this.htmlName, nX, nY, fxy);
}}} catch (e) {
if (Clazz.exceptionOf (e, Exception)) {
JU.Logger.error ("Exception " + e + " with nX, nY: " + nX + " " + nY);
} else {
throw e;
}
}
return fxy;
}, "~S,~N,~N");
Clazz.overrideMethod (c$, "doFunctionXYZ", 
function (functionName, nX, nY, nZ) {
var fxyz =  Clazz.newFloatArray (Math.abs (nX), Math.abs (nY), Math.abs (nZ), 0);
if (!this.mayScript || !this.haveDocumentAccess || nX == 0 || nY == 0 || nZ == 0) return fxyz;
try {
{
eval(functionName)(this.htmlName, nX, nY, nZ, fxyz);
}} catch (e) {
if (Clazz.exceptionOf (e, Exception)) {
JU.Logger.error ("Exception " + e + " for " + functionName + " with nX, nY, nZ: " + nX + " " + nY + " " + nZ);
} else {
throw e;
}
}
return fxyz;
}, "~S,~N,~N,~N");
Clazz.overrideMethod (c$, "doShowDocument", 
function (url) {
{
window.open(url.toString());
}}, "java.net.URL");
Clazz.overrideMethod (c$, "doSendCallback", 
function (callback, data, strInfo) {
if (callback == null || callback.length == 0) {
} else if (callback.equals ("alert")) {
{
alert(strInfo); return "";
}} else {
var tokens = JU.PT.split (callback, ".");
{
try{
var o = window[tokens[0]];
for (var i = 1; i < tokens.length; i++)
o = o[tokens[i]];
for (var i = 0; i < data.length; i++)
data[i] && data[i].booleanValue && (data[i] = data[i].booleanValue());
return o(data[0],data[1],data[2],data[3],data[4],data[5],data[6],data[7]);
} catch (e) { System.out.println(callback + " failed " + e); }
}}return "";
}, "~S,~A,~S");
Clazz.overrideMethod (c$, "doEval", 
function (strEval) {
try {
{
return "" + eval(strEval);
}} catch (e) {
if (Clazz.exceptionOf (e, Exception)) {
JU.Logger.error ("# error evaluating " + strEval + ":" + e.toString ());
} else {
throw e;
}
}
return "";
}, "~S");
Clazz.overrideMethod (c$, "doShowStatus", 
function (message) {
try {
System.out.println (message);
} catch (e) {
if (Clazz.exceptionOf (e, Exception)) {
} else {
throw e;
}
}
}, "~S");
Clazz.overrideMethod (c$, "cacheFileByName", 
function (fileName, isAdd) {
return this.viewer.cacheFileByName (fileName, isAdd);
}, "~S,~B");
Clazz.overrideMethod (c$, "cachePut", 
function (key, data) {
this.viewer.cachePut (key, data);
}, "~S,~O");
Clazz.overrideMethod (c$, "getGLmolView", 
function () {
return this.viewer.getGLmolView ();
});
Clazz.overrideMethod (c$, "getFullName", 
function () {
return this.fullName;
});
Clazz.overrideMethod (c$, "processMouseEvent", 
function (id, x, y, modifiers, time) {
return this.viewer.processMouseEvent (id, x, y, modifiers, time);
}, "~N,~N,~N,~N,~N");
Clazz.overrideMethod (c$, "setDisplay", 
function (canvas) {
this.viewer.setDisplay (canvas);
}, "~O");
Clazz.overrideMethod (c$, "setStatusDragDropped", 
function (mode, x, y, fileName) {
return this.viewer.setStatusDragDropped (mode, x, y, fileName);
}, "~N,~N,~N,~S");
Clazz.overrideMethod (c$, "startHoverWatcher", 
function (enable) {
this.viewer.startHoverWatcher (enable);
}, "~B");
Clazz.overrideMethod (c$, "update", 
function () {
this.viewer.updateJS ();
});
Clazz.overrideMethod (c$, "openFile", 
function (fileName) {
return this.viewer.openFile (fileName);
}, "~S");
Clazz.overrideMethod (c$, "openFileAsyncSpecial", 
function (fileName, flags) {
this.viewer.openFileAsyncSpecial (fileName, flags);
}, "~S,~N");
Clazz.overrideMethod (c$, "processTwoPointGesture", 
function (touches) {
this.viewer.processTwoPointGesture (touches);
}, "~A");
Clazz.overrideMethod (c$, "setScreenDimension", 
function (width, height) {
this.viewer.setScreenDimension (width, height);
}, "~N,~N");
});
