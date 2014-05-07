Clazz.declarePackage ("J.script");
Clazz.load (["J.script.ScriptException"], "J.script.ScriptInterruption", null, function () {
c$ = Clazz.decorateAsClass (function () {
this.willResume = false;
Clazz.instantialize (this, arguments);
}, J.script, "ScriptInterruption", J.script.ScriptException);
Clazz.makeConstructor (c$, 
function (eval, why, millis) {
Clazz.superConstructor (this, J.script.ScriptInterruption, [eval, why, "!", eval.viewer.autoExit]);
this.willResume = (millis != 2147483647);
if (why.equals ("delay")) eval.delayScript (millis);
}, "J.script.ScriptEvaluator,~S,~N");
});
