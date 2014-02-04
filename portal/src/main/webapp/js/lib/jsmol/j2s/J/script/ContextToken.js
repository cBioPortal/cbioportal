Clazz.declarePackage ("J.script");
Clazz.load (["J.script.T"], "J.script.ContextToken", ["java.util.Hashtable", "J.script.SV"], function () {
c$ = Clazz.decorateAsClass (function () {
this.contextVariables = null;
this.name0 = null;
Clazz.instantialize (this, arguments);
}, J.script, "ContextToken", J.script.T);
c$.newContext = $_M(c$, "newContext", 
function (isOpen) {
var ct = (isOpen ? J.script.ContextToken.newCmd (266280, "{") : J.script.ContextToken.newCmd (266278, "}"));
ct.intValue = 0;
return ct;
}, "~B");
c$.newCmd = $_M(c$, "newCmd", 
function (tok, value) {
var ct =  new J.script.ContextToken ();
ct.tok = tok;
ct.value = value;
return ct;
}, "~N,~O");
$_M(c$, "addName", 
function (name) {
if (this.contextVariables == null) this.contextVariables =  new java.util.Hashtable ();
this.contextVariables.put (name, J.script.SV.newVariable (4, "").setName (name));
}, "~S");
});
