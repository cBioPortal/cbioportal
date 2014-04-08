Clazz.declarePackage ("J.script");
c$ = Clazz.decorateAsClass (function () {
this.aatoken = null;
this.allowJSThreads = false;
this.chk = false;
this.contextPath = " >> ";
this.contextVariables = null;
this.displayLoadErrorsSave = false;
this.errorMessage = null;
this.errorMessageUntranslated = null;
this.errorType = null;
this.executionPaused = false;
this.executionStepping = false;
this.functionName = null;
this.iCommandError = -1;
this.id = 0;
this.isComplete = true;
this.isFunction = false;
this.isJSThread = false;
this.isStateScript = false;
this.isTryCatch = false;
this.iToken = 0;
this.lineEnd = 2147483647;
this.lineIndices = null;
this.lineNumbers = null;
this.mustResumeEval = false;
this.outputBuffer = null;
this.parallelProcessor = null;
this.parentContext = null;
this.pc = 0;
this.pcEnd = 2147483647;
this.script = null;
this.scriptExtensions = null;
this.scriptFileName = null;
this.scriptLevel = 0;
this.statement = null;
this.htFileCache = null;
this.statementLength = 0;
this.token = null;
this.tryPt = 0;
Clazz.instantialize (this, arguments);
}, J.script, "ScriptContext");
Clazz.makeConstructor (c$, 
function () {
this.id = ($t$ = ++ J.script.ScriptContext.contextCount, J.script.ScriptContext.prototype.contextCount = J.script.ScriptContext.contextCount, $t$);
});
Clazz.defineStatics (c$,
"contextCount", 0);
