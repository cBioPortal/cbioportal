Clazz.declarePackage ("J.parallel");
c$ = Clazz.decorateAsClass (function () {
this.processName = null;
this.context = null;
Clazz.instantialize (this, arguments);
}, J.parallel, "ScriptProcess");
Clazz.makeConstructor (c$, 
function (name, context) {
this.processName = name;
this.context = context;
}, "~S,J.script.ScriptContext");
