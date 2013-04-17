Clazz.declarePackage ("J.script");
Clazz.load (["java.lang.Exception"], "J.script.ScriptException", null, function () {
c$ = Clazz.decorateAsClass (function () {
this.eval = null;
this.message = null;
this.untranslated = null;
Clazz.instantialize (this, arguments);
}, J.script, "ScriptException", Exception);
Clazz.makeConstructor (c$, 
function (scriptEvaluator, msg, untranslated, isError) {
Clazz.superConstructor (this, J.script.ScriptException, []);
this.eval = scriptEvaluator;
this.message = msg;
if (!isError) return;
this.eval.setException (this, msg, untranslated);
}, "J.api.JmolScriptEvaluator,~S,~S,~B");
$_M(c$, "getErrorMessageUntranslated", 
function () {
return this.untranslated;
});
Clazz.overrideMethod (c$, "toString", 
function () {
return this.message;
});
});
