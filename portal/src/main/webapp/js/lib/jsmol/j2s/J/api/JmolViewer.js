Clazz.declarePackage ("J.api");
Clazz.load (null, "J.api.JmolViewer", ["java.util.Hashtable"], function () {
c$ = Clazz.decorateAsClass (function () {
this.menuStructure = null;
this.apiPlatform = null;
this.fileAdapter = null;
Clazz.instantialize (this, arguments);
}, J.api, "JmolViewer");
c$.allocateViewer = $_M(c$, "allocateViewer", 
function (display, modelAdapter, fullName, documentBase, codeBase, commandOptions, statusListener, implementedPlatform) {
var info =  new java.util.Hashtable ();
if (display != null) info.put ("display", display);
if (modelAdapter != null) info.put ("adapter", modelAdapter);
if (statusListener != null) info.put ("statusListener", statusListener);
if (implementedPlatform != null) info.put ("platform", implementedPlatform);
if (commandOptions != null) info.put ("options", commandOptions);
if (fullName != null) info.put ("fullName", fullName);
if (documentBase != null) info.put ("documentBase", documentBase);
if (codeBase != null) info.put ("codeBase", codeBase);
return  new J.viewer.Viewer (info);
}, "~O,J.api.JmolAdapter,~S,java.net.URL,java.net.URL,~S,J.api.JmolStatusListener,J.api.ApiPlatform");
c$.allocateViewer = $_M(c$, "allocateViewer", 
function (container, jmolAdapter) {
return J.api.JmolViewer.allocateViewer (container, jmolAdapter, null, null, null, null, null, null);
}, "~O,J.api.JmolAdapter");
c$.allocateViewer = $_M(c$, "allocateViewer", 
function (display, modelAdapter, fullName, documentBase, codeBase, commandOptions, statusListener) {
return J.api.JmolViewer.allocateViewer (display, modelAdapter, fullName, documentBase, codeBase, commandOptions, statusListener, null);
}, "~O,J.api.JmolAdapter,~S,java.net.URL,java.net.URL,~S,J.api.JmolStatusListener");
$_M(c$, "setConsole", 
function (console) {
this.getProperty ("DATA_API", "getAppConsole", console);
}, "J.api.JmolAppConsoleInterface");
c$.getJmolVersion = $_M(c$, "getJmolVersion", 
function () {
return J.viewer.Viewer.getJmolVersion ();
});
c$.checkOption = $_M(c$, "checkOption", 
function (viewer, option) {
var testFlag = viewer.getParameter (option);
return (Clazz.instanceOf (testFlag, Boolean) && (testFlag).booleanValue () || Clazz.instanceOf (testFlag, Integer) && (testFlag).intValue () != 0);
}, "J.api.JmolViewer,~S");
$_M(c$, "openFileAsync", 
function (fileName) {
this.openFileAsyncPDB (fileName, false);
}, "~S");
$_M(c$, "mouseEvent", 
function (id, x, y, modifiers, when) {
this.handleOldJvm10Event (id, x, y, modifiers, when);
}, "~N,~N,~N,~N,~N");
$_M(c$, "getFileAdapter", 
function () {
return (this.fileAdapter == null ? this.fileAdapter = this.apiPlatform.getFileAdapter () : this.fileAdapter);
});
$_M(c$, "renderScreenImage", 
function (g, currentSize, rectClip) {
this.apiPlatform.renderScreenImage (g, currentSize);
}, "~O,~O,~O");
$_M(c$, "getJsObjectInfo", 
function (jsObject, method, args) {
return this.apiPlatform.getJsObjectInfo (jsObject, method, args);
}, "~A,~S,~A");
c$.getJmolValueAsString = $_M(c$, "getJmolValueAsString", 
function (jmolViewer, $var) {
return (jmolViewer == null ? "" : "" + jmolViewer.getParameter ($var));
}, "J.api.JmolViewer,~S");
});
