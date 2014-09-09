Clazz.declarePackage ("JS");
Clazz.load (null, "JS.ScriptFlowContext", ["JS.ScriptCompiler"], function () {
c$ = Clazz.decorateAsClass (function () {
this.compiler = null;
this.token = null;
this.pt0 = 0;
this.ptDefault = 0;
this.$function = null;
this.$var = null;
this.parent = null;
this.lineStart = 0;
this.commandStart = 0;
this.ptLine = 0;
this.ptCommand = 0;
this.forceEndIf = true;
this.ident = null;
Clazz.instantialize (this, arguments);
}, JS, "ScriptFlowContext");
Clazz.makeConstructor (c$, 
function (compiler, token, pt0, parent) {
this.compiler = compiler;
this.token = token;
this.ident = token.value;
this.pt0 = pt0;
this.parent = parent;
this.lineStart = this.ptLine = this.compiler.lineCurrent;
this.commandStart = this.ptCommand = this.compiler.iCommand;
}, "JS.ScriptCompiler,JS.ContextToken,~N,JS.ScriptFlowContext");
Clazz.defineMethod (c$, "getBreakableContext", 
function (nLevelsUp) {
var f = this;
while (f != null && (!JS.ScriptCompiler.isBreakableContext (f.token.tok) || nLevelsUp-- > 0)) f = f.getParent ();

return f;
}, "~N");
Clazz.defineMethod (c$, "checkForceEndIf", 
function () {
var test = this.forceEndIf && this.ptCommand < this.compiler.iCommand && this.ptLine == this.compiler.lineCurrent;
if (test) this.forceEndIf = false;
return test;
});
Clazz.defineMethod (c$, "setPt0", 
function (pt0, isDefault) {
this.pt0 = pt0;
if (isDefault) this.ptDefault = pt0;
this.setLine ();
return pt0;
}, "~N,~B");
Clazz.defineMethod (c$, "setLine", 
function () {
this.ptLine = this.compiler.lineCurrent;
this.ptCommand = this.compiler.iCommand + 1;
});
Clazz.overrideMethod (c$, "toString", 
function () {
return "ident " + this.ident + " line " + this.lineStart + " command " + this.commandStart;
});
Clazz.defineMethod (c$, "getParent", 
function () {
return this.parent;
});
Clazz.defineMethod (c$, "path", 
function () {
var s = "";
var f = this;
while (f != null) {
s = f.ident + "-" + s;
f = f.parent;
}
return "[" + s + "]";
});
Clazz.defineMethod (c$, "setFunction", 
function ($function) {
this.$function = $function;
}, "JS.ScriptFunction");
});
