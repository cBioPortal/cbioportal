Clazz.declarePackage ("J.script");
Clazz.load (["J.api.JmolScriptEvaluator"], "J.script.ScriptEvaluator", ["java.lang.Boolean", "$.Float", "$.NullPointerException", "$.Thread", "java.util.Hashtable", "J.api.Interface", "J.atomdata.RadiusData", "J.constant.EnumAnimationMode", "$.EnumAxesMode", "$.EnumPalette", "$.EnumStereoMode", "$.EnumStructure", "$.EnumVdw", "J.i18n.GT", "J.io.JmolBinary", "J.modelset.Atom", "$.AtomCollection", "$.Bond", "$.Group", "$.LabelToken", "$.MeasurementData", "$.ModelCollection", "$.Object2d", "$.Text", "$.TickInfo", "J.script.FileLoadThread", "$.SV", "$.ScriptCompiler", "$.ScriptContext", "$.ScriptDelayThread", "$.ScriptException", "$.ScriptInterruption", "$.ScriptMathProcessor", "$.T", "J.util.BS", "$.BSUtil", "$.C", "$.ColorEncoder", "$.ColorUtil", "$.Elements", "$.Escape", "$.GData", "$.JmolEdge", "$.JmolList", "$.Logger", "$.Matrix3f", "$.Matrix4f", "$.Measure", "$.P3", "$.P4", "$.Parser", "$.Point3fi", "$.Quaternion", "$.SB", "$.TextFormat", "$.V3", "J.viewer.ActionManager", "$.FileManager", "$.JC", "$.StateManager", "$.Viewer"], function () {
c$ = Clazz.decorateAsClass (function () {
this.allowJSThreads = true;
this.listCommands = false;
this.isJS = false;
this.tQuiet = false;
this.chk = false;
this.isCmdLine_C_Option = false;
this.isCmdLine_c_or_C_Option = false;
this.historyDisabled = false;
this.logMessages = false;
this.debugScript = false;
this.executionStopped = false;
this.executionPaused = false;
this.executionStepping = false;
this.executing = false;
this.timeBeginExecution = 0;
this.timeEndExecution = 0;
this.mustResumeEval = false;
this.sm = null;
this.currentThread = null;
this.viewer = null;
this.compiler = null;
this.definedAtomSets = null;
this.outputBuffer = null;
this.contextPath = "";
this.scriptFileName = null;
this.functionName = null;
this.isStateScript = false;
this.scriptLevel = 0;
this.scriptReportingLevel = 0;
this.commandHistoryLevelMax = 0;
this.aatoken = null;
this.lineNumbers = null;
this.lineIndices = null;
this.contextVariables = null;
this.$script = null;
this.pc = 0;
this.thisCommand = null;
this.fullCommand = null;
this.st = null;
this.slen = 0;
this.iToken = 0;
this.lineEnd = 0;
this.pcEnd = 0;
this.scriptExtensions = null;
this.forceNoAddHydrogens = false;
this.parallelProcessor = null;
this.thisContext = null;
this.$error = false;
this.errorMessage = null;
this.errorMessageUntranslated = null;
this.errorType = null;
this.iCommandError = 0;
this.ignoreError = false;
this.tempStatement = null;
this.isBondSet = false;
this.expressionResult = null;
this.theTok = 0;
this.theToken = null;
this.coordinatesAreFractional = false;
this.fractionalPoint = null;
this.scriptExt = null;
this.$data = null;
this.colorArgb = null;
this.scriptDelayThread = null;
this.fileLoadThread = null;
Clazz.instantialize (this, arguments);
}, J.script, "ScriptEvaluator", null, J.api.JmolScriptEvaluator);
Clazz.prepareFields (c$, function () {
this.colorArgb = [-2147483648];
});
Clazz.overrideMethod (c$, "getAllowJSThreads", 
function () {
return this.allowJSThreads;
});
Clazz.overrideMethod (c$, "setViewer", 
function (viewer) {
this.viewer = viewer;
this.compiler = (this.compiler == null ? viewer.compiler : this.compiler);
this.isJS = viewer.isSingleThreaded;
this.definedAtomSets = viewer.definedAtomSets;
return this;
}, "J.viewer.Viewer");
Clazz.makeConstructor (c$, 
function () {
this.currentThread = Thread.currentThread ();
($t$ = J.script.ScriptEvaluator.evalID ++, J.script.ScriptEvaluator.prototype.evalID = J.script.ScriptEvaluator.evalID, $t$);
});
Clazz.overrideMethod (c$, "setCompiler", 
function () {
this.viewer.compiler = this.compiler =  new J.script.ScriptCompiler (this.viewer);
});
Clazz.overrideMethod (c$, "compileScriptString", 
function (script, tQuiet) {
this.clearState (tQuiet);
this.contextPath = "[script]";
return this.compileScript (null, script, this.debugScript);
}, "~S,~B");
Clazz.overrideMethod (c$, "compileScriptFile", 
function (filename, tQuiet) {
this.clearState (tQuiet);
this.contextPath = filename;
return this.compileScriptFileInternal (filename, null, null, null);
}, "~S,~B");
Clazz.overrideMethod (c$, "evaluateCompiledScript", 
function (isCmdLine_c_or_C_Option, isCmdLine_C_Option, historyDisabled, listCommands, outputBuffer, allowThreads) {
var tempOpen = this.isCmdLine_C_Option;
this.isCmdLine_C_Option = isCmdLine_C_Option;
this.chk = this.isCmdLine_c_or_C_Option = isCmdLine_c_or_C_Option;
this.historyDisabled = historyDisabled;
this.outputBuffer = outputBuffer;
this.currentThread = Thread.currentThread ();
this.allowJSThreads = allowThreads;
this.listCommands = listCommands;
this.startEval ();
this.isCmdLine_C_Option = tempOpen;
this.viewer.setStateScriptVersion (null);
}, "~B,~B,~B,~B,J.util.SB,~B");
$_M(c$, "useThreads", 
($fz = function () {
return (!this.viewer.autoExit && this.viewer.haveDisplay && this.outputBuffer == null && this.allowJSThreads);
}, $fz.isPrivate = true, $fz));
$_M(c$, "startEval", 
($fz = function () {
this.timeBeginExecution = System.currentTimeMillis ();
this.executionStopped = this.executionPaused = false;
this.executionStepping = false;
this.executing = true;
this.viewer.pushHoldRepaint ("runEval");
this.setScriptExtensions ();
this.executeCommands (false);
}, $fz.isPrivate = true, $fz));
$_M(c$, "executeCommands", 
($fz = function (isTry) {
var haveError = false;
try {
if (!this.dispatchCommands (false, false)) return;
} catch (e$$) {
if (Clazz.exceptionOf (e$$, Error)) {
var er = e$$;
{
this.viewer.handleError (er, false);
this.setErrorMessage ("" + er + " " + this.viewer.getShapeErrorState ());
this.errorMessageUntranslated = "" + er;
this.scriptStatusOrBuffer (this.errorMessage);
haveError = true;
}
} else if (Clazz.exceptionOf (e$$, J.script.ScriptException)) {
var e = e$$;
{
if (Clazz.instanceOf (e, J.script.ScriptInterruption)) {
return;
}if (isTry) {
this.viewer.setStringProperty ("_errormessage", "" + e);
return;
}this.setErrorMessage (e.toString ());
this.errorMessageUntranslated = e.getErrorMessageUntranslated ();
this.scriptStatusOrBuffer (this.errorMessage);
this.viewer.notifyError ((this.errorMessage != null && this.errorMessage.indexOf ("java.lang.OutOfMemoryError") >= 0 ? "Error" : "ScriptException"), this.errorMessage, this.errorMessageUntranslated);
haveError = true;
}
} else {
throw e$$;
}
}
if (haveError || !this.isJS || !this.allowJSThreads) {
this.viewer.setTainted (true);
this.viewer.popHoldRepaint ("executeCommands" + " " + (this.scriptLevel > 0 ? "\u0001## REPAINT_IGNORE ##" : ""));
}this.timeEndExecution = System.currentTimeMillis ();
if (this.errorMessage == null && this.executionStopped) this.setErrorMessage ("execution interrupted");
 else if (!this.tQuiet && !this.chk) this.viewer.scriptStatus ("Script completed");
this.executing = this.chk = this.isCmdLine_c_or_C_Option = this.historyDisabled = false;
var msg = this.getErrorMessageUntranslated ();
this.viewer.setErrorMessage (this.errorMessage, msg);
if (!this.tQuiet) this.viewer.setScriptStatus ("Jmol script terminated", this.errorMessage, 1 + this.getExecutionWalltime (), msg);
}, $fz.isPrivate = true, $fz), "~B");
Clazz.overrideMethod (c$, "resumeEval", 
function (sc) {
this.setErrorMessage (null);
if (this.executionStopped || sc == null || !sc.mustResumeEval) {
this.viewer.setTainted (true);
this.viewer.popHoldRepaint ("resumeEval");
this.viewer.queueOnHold = false;
return;
}if (!this.executionPaused) sc.pc++;
this.thisContext = sc;
if (sc.scriptLevel > 0) this.scriptLevel = sc.scriptLevel - 1;
this.restoreScriptContext (sc, true, false, false);
this.executeCommands (sc.isTryCatch);
}, "J.script.ScriptContext");
Clazz.overrideMethod (c$, "runScriptBuffer", 
function (script, outputBuffer) {
this.pushContext (null, "runScriptBuffer");
this.contextPath += " >> script() ";
this.outputBuffer = outputBuffer;
this.allowJSThreads = false;
if (this.compileScript (null, script + "\u0001## EDITOR_IGNORE ##" + "\u0001## REPAINT_IGNORE ##", false)) this.dispatchCommands (false, false);
this.popContext (false, false);
}, "~S,J.util.SB");
Clazz.overrideMethod (c$, "checkScriptSilent", 
function (script) {
var sc = this.compiler.compile (null, script, false, true, false, true);
if (sc.errorType != null) return sc;
this.restoreScriptContext (sc, false, false, false);
this.chk = true;
this.isCmdLine_c_or_C_Option = this.isCmdLine_C_Option = false;
this.pc = 0;
try {
this.dispatchCommands (false, false);
} catch (e) {
if (Clazz.exceptionOf (e, J.script.ScriptException)) {
this.setErrorMessage (e.toString ());
sc = this.getScriptContext ("checkScriptSilent");
} else {
throw e;
}
}
this.chk = false;
return sc;
}, "~S");
c$.getContextTrace = $_M(c$, "getContextTrace", 
function (viewer, sc, sb, isTop) {
if (sb == null) sb =  new J.util.SB ();
sb.append (J.script.ScriptEvaluator.getErrorLineMessage (sc.functionName, sc.scriptFileName, sc.lineNumbers[sc.pc], sc.pc, J.script.ScriptEvaluator.statementAsString (viewer, sc.statement, (isTop ? sc.iToken : 9999), false)));
if (sc.parentContext != null) J.script.ScriptEvaluator.getContextTrace (viewer, sc.parentContext, sb, false);
return sb;
}, "J.viewer.Viewer,J.script.ScriptContext,J.util.SB,~B");
Clazz.overrideMethod (c$, "setDebugging", 
function () {
this.debugScript = this.viewer.getBoolean (603979824);
this.logMessages = (this.debugScript && J.util.Logger.debugging);
});
$_M(c$, "getExecutionWalltime", 
($fz = function () {
return (this.timeEndExecution - this.timeBeginExecution);
}, $fz.isPrivate = true, $fz));
Clazz.overrideMethod (c$, "haltExecution", 
function () {
this.resumePausedExecution ();
this.executionStopped = true;
});
Clazz.overrideMethod (c$, "pauseExecution", 
function (withDelay) {
if (this.chk || this.viewer.isHeadless ()) return;
if (withDelay && !this.isJS) this.delayScript (-100);
this.viewer.popHoldRepaint ("pauseExecution " + withDelay);
this.executionStepping = false;
this.executionPaused = true;
}, "~B");
Clazz.overrideMethod (c$, "stepPausedExecution", 
function () {
this.executionStepping = true;
this.executionPaused = false;
});
Clazz.overrideMethod (c$, "resumePausedExecution", 
function () {
this.executionPaused = false;
this.executionStepping = false;
});
Clazz.overrideMethod (c$, "isExecuting", 
function () {
return this.executing && !this.executionStopped;
});
Clazz.overrideMethod (c$, "isPaused", 
function () {
return this.executionPaused;
});
Clazz.overrideMethod (c$, "isStepping", 
function () {
return this.executionStepping;
});
Clazz.overrideMethod (c$, "isStopped", 
function () {
return this.executionStopped || !this.isJS && this.currentThread !== Thread.currentThread ();
});
Clazz.overrideMethod (c$, "getNextStatement", 
function () {
return (this.pc < this.aatoken.length ? J.script.ScriptEvaluator.getErrorLineMessage (this.functionName, this.scriptFileName, this.getLinenumber (null), this.pc, J.script.ScriptEvaluator.statementAsString (this.viewer, this.aatoken[this.pc], -9999, this.logMessages)) : "");
});
$_M(c$, "getCommand", 
($fz = function (pc, allThisLine, addSemi) {
if (pc >= this.lineIndices.length) return "";
if (allThisLine) {
var pt0 = -1;
var pt1 = this.$script.length;
for (var i = 0; i < this.lineNumbers.length; i++) if (this.lineNumbers[i] == this.lineNumbers[pc]) {
if (pt0 < 0) pt0 = this.lineIndices[i][0];
pt1 = this.lineIndices[i][1];
} else if (this.lineNumbers[i] == 0 || this.lineNumbers[i] > this.lineNumbers[pc]) {
break;
}
if (pt1 == this.$script.length - 1 && this.$script.endsWith ("}")) pt1++;
return (pt0 == this.$script.length || pt1 < pt0 ? "" : this.$script.substring (Math.max (pt0, 0), Math.min (this.$script.length, pt1)));
}var ichBegin = this.lineIndices[pc][0];
var ichEnd = this.lineIndices[pc][1];
var s = "";
if (ichBegin < 0 || ichEnd <= ichBegin || ichEnd > this.$script.length) return "";
try {
s = this.$script.substring (ichBegin, ichEnd);
if (s.indexOf ("\\\n") >= 0) s = J.util.TextFormat.simpleReplace (s, "\\\n", "  ");
if (s.indexOf ("\\\r") >= 0) s = J.util.TextFormat.simpleReplace (s, "\\\r", "  ");
if (s.length > 0 && !s.endsWith (";")) s += ";";
} catch (e) {
if (Clazz.exceptionOf (e, Exception)) {
J.util.Logger.error ("darn problem in Eval getCommand: ichBegin=" + ichBegin + " ichEnd=" + ichEnd + " len = " + this.$script.length + "\n" + e);
} else {
throw e;
}
}
return s;
}, $fz.isPrivate = true, $fz), "~N,~B,~B");
$_M(c$, "logDebugScript", 
($fz = function (ifLevel) {
if (this.logMessages) {
if (this.st.length > 0) J.util.Logger.debug (this.st[0].toString ());
for (var i = 1; i < this.slen; ++i) J.util.Logger.debug (this.st[i].toString ());

}this.iToken = -9999;
if (this.logMessages) {
var strbufLog =  new J.util.SB ();
var s = (ifLevel > 0 ? "                          ".substring (0, ifLevel * 2) : "");
strbufLog.append (s).append (J.script.ScriptEvaluator.statementAsString (this.viewer, this.st, this.iToken, this.logMessages));
this.viewer.scriptStatus (strbufLog.toString ());
} else {
var cmd = this.getCommand (this.pc, false, false);
if (cmd !== "") this.viewer.scriptStatus (cmd);
}}, $fz.isPrivate = true, $fz), "~N");
Clazz.overrideMethod (c$, "evaluateExpression", 
function (expr, asVariable) {
var e = ( new J.script.ScriptEvaluator ()).setViewer (this.viewer);
try {
e.pushContext (null, "evalExp");
e.allowJSThreads = false;
} catch (e1) {
if (Clazz.exceptionOf (e1, J.script.ScriptException)) {
} else {
throw e1;
}
}
return (e.evaluate (expr, asVariable));
}, "~O,~B");
$_M(c$, "evaluate", 
($fz = function (expr, asVariable) {
try {
if (Clazz.instanceOf (expr, String)) {
if (this.compileScript (null, "e_x_p_r_e_s_s_i_o_n" + " = " + expr, false)) {
this.contextVariables = this.viewer.getContextVariables ();
this.setStatement (0);
return (asVariable ? this.parameterExpressionList (2, -1, false).get (0) : this.parameterExpressionString (2, 0));
}} else if (Clazz.instanceOf (expr, Array)) {
this.contextVariables = this.viewer.getContextVariables ();
var bs = this.atomExpression (expr, 0, 0, true, false, true, false);
return (asVariable ? J.script.SV.newScriptVariableBs (bs, -1) : bs);
}} catch (ex) {
if (Clazz.exceptionOf (ex, Exception)) {
J.util.Logger.error ("Error evaluating: " + expr + "\n" + ex);
} else {
throw ex;
}
}
return (asVariable ? J.script.SV.getVariable ("ERROR") : "ERROR");
}, $fz.isPrivate = true, $fz), "~O,~B");
Clazz.overrideMethod (c$, "evaluateParallel", 
function (context, shapeManager) {
var e =  new J.script.ScriptEvaluator ();
e.setViewer (this.viewer);
e.historyDisabled = true;
e.compiler =  new J.script.ScriptCompiler (this.viewer);
e.sm = shapeManager;
try {
e.restoreScriptContext (context, true, false, false);
e.allowJSThreads = false;
e.dispatchCommands (false, false);
} catch (ex) {
if (Clazz.exceptionOf (ex, Exception)) {
this.viewer.setStringProperty ("_errormessage", "" + ex);
if (e.thisContext == null) {
J.util.Logger.error ("Error evaluating context " + ex);
if (!this.viewer.isJS) ex.printStackTrace ();
}return false;
} else {
throw ex;
}
}
return true;
}, "J.script.ScriptContext,J.viewer.ShapeManager");
Clazz.overrideMethod (c$, "getAtomBitSet", 
function (atomExpression) {
if (Clazz.instanceOf (atomExpression, J.util.BS)) return atomExpression;
var bs =  new J.util.BS ();
try {
this.pushContext (null, "getAtomBitSet");
var scr = "select (" + atomExpression + ")";
scr = J.util.TextFormat.replaceAllCharacters (scr, "\n\r", "),(");
scr = J.util.TextFormat.simpleReplace (scr, "()", "(none)");
if (this.compileScript (null, scr, false)) {
this.st = this.aatoken[0];
bs = this.atomExpression (this.st, 1, 0, false, false, true, true);
}this.popContext (false, false);
} catch (ex) {
if (Clazz.exceptionOf (ex, Exception)) {
J.util.Logger.error ("getAtomBitSet " + atomExpression + "\n" + ex);
} else {
throw ex;
}
}
return bs;
}, "~O");
Clazz.overrideMethod (c$, "getAtomBitSetVector", 
function (atomCount, atomExpression) {
var V =  new J.util.JmolList ();
var bs = this.getAtomBitSet (atomExpression);
for (var i = bs.nextSetBit (0); i >= 0; i = bs.nextSetBit (i + 1)) {
V.addLast (Integer.$valueOf (i));
}
return V;
}, "~N,~O");
$_M(c$, "parameterExpressionList", 
($fz = function (pt, ptAtom, isArrayItem) {
return this.parameterExpression (pt, -1, null, true, true, ptAtom, isArrayItem, null, null);
}, $fz.isPrivate = true, $fz), "~N,~N,~B");
$_M(c$, "parameterExpressionString", 
($fz = function (pt, ptMax) {
return this.parameterExpression (pt, ptMax, "", true, false, -1, false, null, null);
}, $fz.isPrivate = true, $fz), "~N,~N");
$_M(c$, "parameterExpressionBoolean", 
($fz = function (pt, ptMax) {
return (this.parameterExpression (pt, ptMax, null, true, false, -1, false, null, null)).booleanValue ();
}, $fz.isPrivate = true, $fz), "~N,~N");
$_M(c$, "parameterExpressionToken", 
($fz = function (pt) {
var result = this.parameterExpressionList (pt, -1, false);
return (result.size () > 0 ? result.get (0) : J.script.SV.newVariable (4, ""));
}, $fz.isPrivate = true, $fz), "~N");
$_M(c$, "parameterExpression", 
($fz = function (pt, ptMax, key, ignoreComma, asVector, ptAtom, isArrayItem, localVars, localVar) {
var v;
var res;
var isImplicitAtomProperty = (localVar != null);
var isOneExpressionOnly = (pt < 0);
var returnBoolean = (!asVector && key == null);
var returnString = (!asVector && key != null && key.length == 0);
var nSquare = 0;
if (isOneExpressionOnly) pt = -pt;
var nParen = 0;
var rpn =  new J.script.ScriptMathProcessor (this, isArrayItem, asVector, false);
if (pt == 0 && ptMax == 0) pt = 2;
if (ptMax < pt) ptMax = this.slen;
out : for (var i = pt; i < ptMax; i++) {
v = null;
var tok = this.getToken (i).tok;
if (isImplicitAtomProperty && this.tokAt (i + 1) != 1048584) {
var token = (localVars != null && localVars.containsKey (this.theToken.value) ? null : this.getBitsetPropertySelector (i, false));
if (token != null) {
rpn.addXVar (localVars.get (localVar));
if (!rpn.addOpAllowMath (token, (this.tokAt (i + 1) == 269484048))) this.invArg ();
if ((token.intValue == 135368713 || token.intValue == 102436) && this.tokAt (this.iToken + 1) != 269484048) {
rpn.addOp (J.script.T.tokenLeftParen);
rpn.addOp (J.script.T.tokenRightParen);
}i = this.iToken;
continue;
}}switch (tok) {
case 1060866:
if (this.tokAt (++i) == 1048577) {
v = this.parameterExpressionToken (++i);
i = this.iToken;
} else if (this.tokAt (i) == 2) {
v = this.viewer.getAtomBits (1095763969, Integer.$valueOf (this.st[i].intValue));
break;
} else {
v = this.getParameter (J.script.SV.sValue (this.st[i]), 1073742190);
}v = this.getParameter ((v).asString (), 1073742190);
break;
case 135369225:
if (this.getToken (++i).tok != 269484048) this.invArg ();
if (localVars == null) localVars =  new java.util.Hashtable ();
res = this.parameterExpression (++i, -1, null, ignoreComma, false, -1, false, localVars, localVar);
var TF = (res).booleanValue ();
var iT = this.iToken;
if (this.getToken (iT++).tok != 1048591) this.invArg ();
this.parameterExpressionBoolean (iT, -1);
var iF = this.iToken;
if (this.tokAt (iF++) != 1048591) this.invArg ();
this.parameterExpression (-iF, -1, null, ignoreComma, false, 1, false, localVars, localVar);
var iEnd = this.iToken;
if (this.tokAt (iEnd) != 269484049) this.invArg ();
v = this.parameterExpression (TF ? iT : iF, TF ? iF : iEnd, "XXX", ignoreComma, false, 1, false, localVars, localVar);
i = iEnd;
break;
case 135369224:
case 135280132:
var isFunctionOfX = (pt > 0);
var isFor = (isFunctionOfX && tok == 135369224);
var dummy;
if (isFunctionOfX) {
if (this.getToken (++i).tok != 269484048 || !J.script.T.tokAttr (this.getToken (++i).tok, 1073741824)) this.invArg ();
dummy = this.parameterAsString (i);
if (this.getToken (++i).tok != 1048591) this.invArg ();
} else {
dummy = "_x";
}v = this.parameterExpressionToken (-(++i)).value;
if (!(Clazz.instanceOf (v, J.util.BS))) this.invArg ();
var bsAtoms = v;
i = this.iToken;
if (isFunctionOfX && this.getToken (i++).tok != 1048591) this.invArg ();
var bsSelect =  new J.util.BS ();
var bsX =  new J.util.BS ();
var sout = (isFor ?  new Array (J.util.BSUtil.cardinalityOf (bsAtoms)) : null);
if (localVars == null) localVars =  new java.util.Hashtable ();
bsX.set (0);
var t = J.script.SV.newScriptVariableBs (bsX, 0);
localVars.put (dummy, t.setName (dummy));
var pt2 = -1;
if (isFunctionOfX) {
pt2 = i - 1;
var np = 0;
var tok2;
while (np >= 0 && ++pt2 < ptMax) {
if ((tok2 = this.tokAt (pt2)) == 269484049) np--;
 else if (tok2 == 269484048) np++;
}
}var p = 0;
var jlast = 0;
var j = bsAtoms.nextSetBit (0);
if (j < 0) {
this.iToken = pt2 - 1;
} else if (!this.chk) {
for (; j >= 0; j = bsAtoms.nextSetBit (j + 1)) {
if (jlast >= 0) bsX.clear (jlast);
jlast = j;
bsX.set (j);
t.index = j;
res = this.parameterExpression (i, pt2, (isFor ? "XXX" : null), ignoreComma, isFor, j, false, localVars, isFunctionOfX ? null : dummy);
if (isFor) {
if (res == null || (res).size () == 0) this.invArg ();
sout[p++] = ((res).get (0)).asString ();
} else if ((res).booleanValue ()) {
bsSelect.set (j);
}}
}if (isFor) {
v = sout;
} else if (isFunctionOfX) {
v = bsSelect;
} else {
return this.bitsetVariableVector (bsSelect);
}i = this.iToken + 1;
break;
case 1048591:
break out;
case 3:
rpn.addXNum (J.script.SV.newVariable (3, this.theToken.value));
break;
case 1048614:
case 2:
rpn.addXNum (J.script.SV.newScriptVariableInt (this.theToken.intValue));
break;
case 135266319:
if (this.tokAt (this.iToken + 1) == 269484048) {
if (!rpn.addOpAllowMath (this.theToken, true)) this.invArg ();
break;
}rpn.addXVar (J.script.SV.newScriptVariableToken (this.theToken));
break;
case 1087375362:
case 1087375361:
case 1048580:
case 1679429641:
case 1087373316:
case 1048582:
case 1087375365:
case 1087373318:
case 1095766028:
case 1095761934:
case 1087373320:
case 1095761938:
case 135267335:
case 135267336:
case 1238369286:
case 1641025539:
case 1048589:
case 1048588:
case 4:
case 8:
case 9:
case 11:
case 12:
case 10:
case 6:
rpn.addXVar (J.script.SV.newScriptVariableToken (this.theToken));
break;
case 1048583:
this.ignoreError = true;
var ptc;
try {
ptc = this.centerParameter (i);
rpn.addXVar (J.script.SV.newVariable (8, ptc));
} catch (e) {
if (Clazz.exceptionOf (e, Exception)) {
rpn.addXStr ("");
} else {
throw e;
}
}
this.ignoreError = false;
i = this.iToken;
break;
case 1048586:
if (this.tokAt (i + 1) == 4) v = this.getHash (i);
 else v = this.getPointOrPlane (i, false, true, true, false, 3, 4);
i = this.iToken;
break;
case 1048577:
if (this.tokAt (i + 1) == 1048578) {
v =  new java.util.Hashtable ();
i++;
break;
} else if (this.tokAt (i + 1) == 1048579 && this.tokAt (i + 2) == 1048578) {
tok = 1048579;
this.iToken += 2;
}case 1048579:
if (tok == 1048579) v = this.viewer.getModelUndeletedAtomsBitSet (-1);
 else v = this.atomExpression (this.st, i, 0, true, true, true, true);
i = this.iToken;
if (nParen == 0 && isOneExpressionOnly) {
this.iToken++;
return this.bitsetVariableVector (v);
}break;
case 1073742195:
rpn.addOp (this.theToken);
continue;
case 1048578:
i++;
break out;
case 1048590:
if (!ignoreComma && nParen == 0 && nSquare == 0) break out;
this.invArg ();
break;
case 269484080:
if (!ignoreComma && nParen == 0 && nSquare == 0) {
break out;
}if (!rpn.addOp (this.theToken)) this.invArg ();
break;
case 1048584:
var token = this.getBitsetPropertySelector (i + 1, false);
if (token == null) this.invArg ();
var isUserFunction = (token.intValue == 135368713);
var allowMathFunc = true;
var tok2 = this.tokAt (this.iToken + 2);
if (this.tokAt (this.iToken + 1) == 1048584) {
switch (tok2) {
case 1048579:
tok2 = 480;
if (this.tokAt (this.iToken + 3) == 1048584 && this.tokAt (this.iToken + 4) == 1276118529) tok2 = 224;
case 32:
case 64:
case 192:
case 128:
case 160:
case 96:
allowMathFunc = (isUserFunction || token.intValue == 1276118018 || tok2 == 480 || tok2 == 224);
token.intValue |= tok2;
this.getToken (this.iToken + 2);
}
}allowMathFunc = new Boolean (allowMathFunc & (this.tokAt (this.iToken + 1) == 269484048 || isUserFunction)).valueOf ();
if (!rpn.addOpAllowMath (token, allowMathFunc)) this.invArg ();
i = this.iToken;
if (token.intValue == 135368713 && this.tokAt (i + 1) != 269484048) {
rpn.addOp (J.script.T.tokenLeftParen);
rpn.addOp (J.script.T.tokenRightParen);
}break;
default:
if (J.script.T.tokAttr (this.theTok, 269484032) || J.script.T.tokAttr (this.theTok, 135266304) && this.tokAt (this.iToken + 1) == 269484048) {
if (!rpn.addOp (this.theToken)) {
if (ptAtom >= 0) {
break out;
}this.invArg ();
}switch (this.theTok) {
case 269484048:
nParen++;
break;
case 269484049:
if (--nParen <= 0 && nSquare == 0 && isOneExpressionOnly) {
this.iToken++;
break out;
}break;
case 269484096:
nSquare++;
break;
case 269484097:
if (--nSquare == 0 && nParen == 0 && isOneExpressionOnly) {
this.iToken++;
break out;
}break;
}
} else {
var name = this.parameterAsString (i).toLowerCase ();
var haveParens = (this.tokAt (i + 1) == 269484048);
if (this.chk) {
v = name;
} else if (!haveParens && (localVars == null || (v = localVars.get (name)) == null)) {
v = this.getContextVariableAsVariable (name);
}if (v == null) {
if (J.script.T.tokAttr (this.theTok, 1073741824) && this.viewer.isFunction (name)) {
if (!rpn.addOp (J.script.SV.newVariable (135368713, this.theToken.value))) this.invArg ();
if (!haveParens) {
rpn.addOp (J.script.T.tokenLeftParen);
rpn.addOp (J.script.T.tokenRightParen);
}} else {
rpn.addXVar (this.viewer.getOrSetNewVariable (name, false));
}}}}
if (v != null) {
if (Clazz.instanceOf (v, J.util.BS)) rpn.addXBs (v);
 else rpn.addXObj (v);
}}
var result = rpn.getResult (false);
if (result == null) {
if (!this.chk) rpn.dumpStacks ("null result");
this.error (13);
}if (result.tok == 135198) return result.value;
if (returnBoolean) return Boolean.$valueOf (result.asBoolean ());
if (returnString) {
if (result.tok == 4) result.intValue = 2147483647;
return result.asString ();
}switch (result.tok) {
case 1048589:
case 1048588:
return Boolean.$valueOf (result.intValue == 1);
case 2:
return Integer.$valueOf (result.intValue);
case 10:
case 3:
case 4:
case 8:
default:
return result.value;
}
}, $fz.isPrivate = true, $fz), "~N,~N,~S,~B,~B,~N,~B,java.util.Map,~S");
$_M(c$, "getHash", 
($fz = function (i) {
var ht =  new java.util.Hashtable ();
for (i = i + 1; i < this.slen; i++) {
if (this.tokAt (i) == 1048590) break;
var key = this.stringParameter (i++);
if (this.tokAt (i++) != 269484066) this.invArg ();
var v = this.parameterExpression (i, 0, null, false, true, -1, false, null, null);
ht.put (key, v.get (0));
i = this.iToken;
if (this.tokAt (i) != 269484080) break;
}
this.iToken = i;
if (this.tokAt (i) != 1048590) this.invArg ();
return ht;
}, $fz.isPrivate = true, $fz), "~N");
$_M(c$, "bitsetVariableVector", 
function (v) {
var resx =  new J.util.JmolList ();
if (Clazz.instanceOf (v, J.util.BS)) {
resx.addLast (J.script.SV.newVariable (10, v));
}return resx;
}, "~O");
$_M(c$, "getBitsetIdent", 
function (bs, label, tokenValue, useAtomMap, index, isExplicitlyAll) {
var isAtoms = !(Clazz.instanceOf (tokenValue, J.modelset.Bond.BondSet));
if (isAtoms) {
if (label == null) label = this.viewer.getStandardLabelFormat (0);
 else if (label.length == 0) label = "%[label]";
}var pt = (label == null ? -1 : label.indexOf ("%"));
var haveIndex = (index != 2147483647);
if (bs == null || this.chk || isAtoms && pt < 0) {
if (label == null) label = "";
return isExplicitlyAll ? [label] : label;
}var modelSet = this.viewer.modelSet;
var n = 0;
var indices = (isAtoms || !useAtomMap ? null : (tokenValue).getAssociatedAtoms ());
if (indices == null && label != null && label.indexOf ("%D") > 0) indices = this.viewer.getAtomIndices (bs);
var asIdentity = (label == null || label.length == 0);
var htValues = (isAtoms || asIdentity ? null : J.modelset.LabelToken.getBondLabelValues ());
var tokens = (asIdentity ? null : isAtoms ? J.modelset.LabelToken.compile (this.viewer, label, '\0', null) : J.modelset.LabelToken.compile (this.viewer, label, '\1', htValues));
var nmax = (haveIndex ? 1 : J.util.BSUtil.cardinalityOf (bs));
var sout =  new Array (nmax);
for (var j = (haveIndex ? index : bs.nextSetBit (0)); j >= 0; j = bs.nextSetBit (j + 1)) {
var str;
if (isAtoms) {
if (asIdentity) str = modelSet.atoms[j].getInfo ();
 else str = J.modelset.LabelToken.formatLabelAtomArray (this.viewer, modelSet.atoms[j], tokens, '\0', indices);
} else {
var bond = modelSet.getBondAt (j);
if (asIdentity) str = bond.getIdentity ();
 else str = J.modelset.LabelToken.formatLabelBond (this.viewer, bond, tokens, htValues, indices);
}str = J.util.TextFormat.formatStringI (str, "#", (n + 1));
sout[n++] = str;
if (haveIndex) break;
}
return nmax == 1 && !isExplicitlyAll ? sout[0] : sout;
}, "J.util.BS,~S,~O,~B,~N,~B");
$_M(c$, "getBitsetPropertySelector", 
($fz = function (i, mustBeSettable) {
var tok = this.getToken (i).tok;
switch (tok) {
case 32:
case 64:
case 96:
case 192:
case 128:
case 160:
case 1716520985:
break;
default:
if (J.script.T.tokAttrOr (tok, 1078984704, 1141899264)) break;
if (tok != 806354977 && !J.script.T.tokAttr (tok, 1073741824)) return null;
var name = this.parameterAsString (i);
if (!mustBeSettable && this.viewer.isFunction (name)) {
tok = 135368713;
break;
}if (!name.endsWith ("?")) return null;
tok = 1073741824;
}
if (mustBeSettable && !J.script.T.tokAttr (tok, 2048)) return null;
return J.script.SV.newScriptVariableIntValue (269484241, tok, this.parameterAsString (i).toLowerCase ());
}, $fz.isPrivate = true, $fz), "~N,~B");
$_M(c$, "getBitsetPropertyFloat", 
function (bs, tok, min, max) {
var data = this.getBitsetProperty (bs, tok, null, null, null, null, false, 2147483647, false);
if (!Float.isNaN (min)) for (var i = 0; i < data.length; i++) if (data[i] < min) data[i] = NaN;

if (!Float.isNaN (max)) for (var i = 0; i < data.length; i++) if (data[i] > max) data[i] = NaN;

return data;
}, "J.util.BS,~N,~N,~N");
$_M(c$, "getBitsetProperty", 
function (bs, tok, ptRef, planeRef, tokenValue, opValue, useAtomMap, index, asVectorIfAll) {
var haveIndex = (index != 2147483647);
var isAtoms = haveIndex || !(Clazz.instanceOf (tokenValue, J.modelset.Bond.BondSet));
var minmaxtype = tok & 480;
var selectedFloat = (minmaxtype == 224);
var atomCount = this.viewer.getAtomCount ();
var fout = (minmaxtype == 256 ?  Clazz.newFloatArray (atomCount, 0) : null);
var isExplicitlyAll = (minmaxtype == 480 || selectedFloat);
tok &= -481;
if (tok == 0) tok = (isAtoms ? 1141899265 : 1678770178);
var isPt = false;
var isInt = false;
var isString = false;
switch (tok) {
case 1146095626:
case 1146095631:
case 1146095627:
case 1146095629:
case 1146093582:
case 1766856708:
case 1146095628:
isPt = true;
break;
case 135368713:
case 1276118018:
break;
default:
isInt = J.script.T.tokAttr (tok, 1095761920) && !J.script.T.tokAttr (tok, 1112539136);
isString = !isInt && J.script.T.tokAttr (tok, 1087373312);
}
var pt = (isPt || !isAtoms ?  new J.util.P3 () : null);
if (isExplicitlyAll || isString && !haveIndex && minmaxtype != 256 && minmaxtype != 32) minmaxtype = 1048579;
var vout = (minmaxtype == 1048579 ?  new J.util.JmolList () : null);
var bsNew = null;
var userFunction = null;
var params = null;
var bsAtom = null;
var tokenAtom = null;
var ptT = null;
var data = null;
switch (tok) {
case 1141899265:
case 1678770178:
if (this.chk) return bs;
bsNew = (tok == 1141899265 ? (isAtoms ? bs : this.viewer.getAtomBits (1678770178, bs)) : (isAtoms ?  new J.modelset.Bond.BondSet (this.viewer.getBondsForSelectedAtoms (bs)) : bs));
var i;
switch (minmaxtype) {
case 32:
i = bsNew.nextSetBit (0);
break;
case 64:
i = bsNew.length () - 1;
break;
case 192:
case 128:
case 160:
return Float.$valueOf (NaN);
default:
return bsNew;
}
bsNew.clearAll ();
if (i >= 0) bsNew.set (i);
return bsNew;
case 1087373321:
switch (minmaxtype) {
case 0:
case 1048579:
return this.getBitsetIdent (bs, null, tokenValue, useAtomMap, index, isExplicitlyAll);
}
return "";
case 135368713:
userFunction = (opValue)[0];
params = (opValue)[1];
bsAtom = J.util.BSUtil.newBitSet (atomCount);
tokenAtom = J.script.SV.newVariable (10, bsAtom);
break;
case 1112539150:
case 1112539151:
this.viewer.autoCalculate (tok);
break;
case 1276118018:
if (ptRef == null && planeRef == null) return  new J.util.P3 ();
break;
case 1766856708:
ptT =  new J.util.P3 ();
break;
case 1716520985:
data = this.viewer.getDataFloat (opValue);
break;
}
var n = 0;
var ivvMinMax = 0;
var ivMinMax = 0;
var fvMinMax = 0;
var sum = 0;
var sum2 = 0;
switch (minmaxtype) {
case 32:
ivMinMax = 2147483647;
fvMinMax = 3.4028235E38;
break;
case 64:
ivMinMax = -2147483648;
fvMinMax = -3.4028235E38;
break;
}
var modelSet = this.viewer.modelSet;
var mode = (isPt ? 3 : isString ? 2 : isInt ? 1 : 0);
if (isAtoms) {
var haveBitSet = (bs != null);
var iModel = -1;
var i0;
var i1;
if (haveIndex) {
i0 = index;
i1 = index + 1;
} else if (haveBitSet) {
i0 = bs.nextSetBit (0);
i1 = Math.min (atomCount, bs.length ());
} else {
i0 = 0;
i1 = atomCount;
}if (this.chk) i1 = 0;
for (var i = i0; i >= 0 && i < i1; i = (haveBitSet ? bs.nextSetBit (i + 1) : i + 1)) {
n++;
var atom = modelSet.atoms[i];
switch (mode) {
case 0:
var fv = 3.4028235E38;
switch (tok) {
case 135368713:
bsAtom.set (i);
fv = J.script.SV.fValue (this.runFunctionRet (null, userFunction, params, tokenAtom, true, true, false));
bsAtom.clear (i);
break;
case 1716520985:
fv = (data == null ? 0 : data[i]);
break;
case 1276118018:
if (planeRef != null) fv = J.util.Measure.distanceToPlane (planeRef, atom);
 else fv = atom.distance (ptRef);
break;
default:
fv = J.modelset.Atom.atomPropertyFloat (this.viewer, atom, tok);
}
if (fv == 3.4028235E38 || Float.isNaN (fv) && minmaxtype != 1048579) {
n--;
continue;
}switch (minmaxtype) {
case 32:
if (fv < fvMinMax) fvMinMax = fv;
break;
case 64:
if (fv > fvMinMax) fvMinMax = fv;
break;
case 256:
fout[i] = fv;
break;
case 1048579:
vout.addLast (Float.$valueOf (fv));
break;
case 160:
case 192:
sum2 += (fv) * fv;
case 128:
default:
sum += fv;
}
break;
case 1:
var iv = 0;
switch (tok) {
case 1297090050:
if (atom.getModelIndex () != iModel) iModel = atom.getModelIndex ();
var bsSym = atom.getAtomSymmetry ();
if (bsSym == null) break;
var p = 0;
switch (minmaxtype) {
case 32:
ivvMinMax = 2147483647;
break;
case 64:
ivvMinMax = -2147483648;
break;
}
for (var k = bsSym.nextSetBit (0); k >= 0; k = bsSym.nextSetBit (k + 1)) {
iv += k + 1;
switch (minmaxtype) {
case 32:
ivvMinMax = Math.min (ivvMinMax, k + 1);
break;
case 64:
ivvMinMax = Math.max (ivvMinMax, k + 1);
break;
}
p++;
}
switch (minmaxtype) {
case 32:
case 64:
iv = ivvMinMax;
}
n += p - 1;
break;
case 1095766022:
case 1095761925:
this.errorStr (45, J.script.T.nameOf (tok));
break;
default:
iv = J.modelset.Atom.atomPropertyInt (atom, tok);
}
switch (minmaxtype) {
case 32:
if (iv < ivMinMax) ivMinMax = iv;
break;
case 64:
if (iv > ivMinMax) ivMinMax = iv;
break;
case 256:
fout[i] = iv;
break;
case 1048579:
vout.addLast (Integer.$valueOf (iv));
break;
case 160:
case 192:
sum2 += (iv) * iv;
case 128:
default:
sum += iv;
}
break;
case 2:
var s = J.modelset.Atom.atomPropertyString (this.viewer, atom, tok);
switch (minmaxtype) {
case 256:
fout[i] = J.util.Parser.parseFloatStr (s);
break;
default:
if (vout == null) return s;
vout.addLast (s);
}
break;
case 3:
var t = J.modelset.Atom.atomPropertyTuple (atom, tok);
if (t == null) this.errorStr (45, J.script.T.nameOf (tok));
switch (minmaxtype) {
case 256:
fout[i] = Math.sqrt (t.x * t.x + t.y * t.y + t.z * t.z);
break;
case 1048579:
vout.addLast (J.util.P3.newP (t));
break;
default:
pt.add (t);
}
break;
}
if (haveIndex) break;
}
} else {
var isAll = (bs == null);
var i0 = (isAll ? 0 : bs.nextSetBit (0));
var i1 = this.viewer.getBondCount ();
for (var i = i0; i >= 0 && i < i1; i = (isAll ? i + 1 : bs.nextSetBit (i + 1))) {
n++;
var bond = modelSet.getBondAt (i);
switch (tok) {
case 1141899267:
var fv = bond.getAtom1 ().distance (bond.getAtom2 ());
switch (minmaxtype) {
case 32:
if (fv < fvMinMax) fvMinMax = fv;
break;
case 64:
if (fv > fvMinMax) fvMinMax = fv;
break;
case 1048579:
vout.addLast (Float.$valueOf (fv));
break;
case 160:
case 192:
sum2 += fv * fv;
case 128:
default:
sum += fv;
}
break;
case 1146095626:
switch (minmaxtype) {
case 1048579:
pt.setT (bond.getAtom1 ());
pt.add (bond.getAtom2 ());
pt.scale (0.5);
vout.addLast (J.util.P3.newP (pt));
break;
default:
pt.add (bond.getAtom1 ());
pt.add (bond.getAtom2 ());
n++;
}
break;
case 1766856708:
J.util.ColorUtil.colorPointFromInt (this.viewer.getColorArgbOrGray (bond.colix), ptT);
switch (minmaxtype) {
case 1048579:
vout.addLast (J.util.P3.newP (ptT));
break;
default:
pt.add (ptT);
}
break;
default:
this.errorStr (46, J.script.T.nameOf (tok));
}
}
}if (minmaxtype == 256) return fout;
if (minmaxtype == 1048579) {
if (asVectorIfAll) return vout;
var len = vout.size ();
if (isString && !isExplicitlyAll && len == 1) return vout.get (0);
if (selectedFloat) {
fout =  Clazz.newFloatArray (len, 0);
for (var i = len; --i >= 0; ) {
var v = vout.get (i);
switch (mode) {
case 0:
fout[i] = (v).floatValue ();
break;
case 1:
fout[i] = (v).floatValue ();
break;
case 2:
fout[i] = J.util.Parser.parseFloatStr (v);
break;
case 3:
fout[i] = (v).length ();
break;
}
}
return fout;
}if (tok == 1087373320) {
var sb =  new J.util.SB ();
for (var i = 0; i < len; i++) sb.append (vout.get (i));

return sb.toString ();
}var sout =  new Array (len);
for (var i = len; --i >= 0; ) {
var v = vout.get (i);
if (Clazz.instanceOf (v, J.util.P3)) sout[i] = J.util.Escape.eP (v);
 else sout[i] = "" + vout.get (i);
}
return sout;
}if (isPt) return (n == 0 ? pt : J.util.P3.new3 (pt.x / n, pt.y / n, pt.z / n));
if (n == 0 || n == 1 && minmaxtype == 192) return Float.$valueOf (NaN);
if (isInt) {
switch (minmaxtype) {
case 32:
case 64:
return Integer.$valueOf (ivMinMax);
case 160:
case 192:
break;
case 128:
return Integer.$valueOf (Clazz.doubleToInt (sum));
default:
if (sum / n == Clazz.doubleToInt (sum / n)) return Integer.$valueOf (Clazz.doubleToInt (sum / n));
return Float.$valueOf ((sum / n));
}
}switch (minmaxtype) {
case 32:
case 64:
sum = fvMinMax;
break;
case 128:
break;
case 160:
sum = sum2;
break;
case 192:
sum = Math.sqrt ((sum2 - sum * sum / n) / (n - 1));
break;
default:
sum /= n;
break;
}
return Float.$valueOf (sum);
}, "J.util.BS,~N,J.util.P3,J.util.P4,~O,~O,~B,~N,~B");
$_M(c$, "setBitsetProperty", 
($fz = function (bs, tok, iValue, fValue, tokenValue) {
if (this.chk || J.util.BSUtil.cardinalityOf (bs) == 0) return;
var list = null;
var sValue = null;
var fvalues = null;
var pt;
var sv = null;
var nValues = 0;
var isStrProperty = J.script.T.tokAttr (tok, 1087373312);
if (tokenValue.tok == 7) {
sv = (tokenValue).getList ();
if ((nValues = sv.size ()) == 0) return;
}switch (tok) {
case 1146095626:
case 1146095627:
case 1146095629:
case 1146095631:
switch (tokenValue.tok) {
case 8:
this.viewer.setAtomCoords (bs, tok, tokenValue.value);
break;
case 7:
this.theToken = tokenValue;
this.viewer.setAtomCoords (bs, tok, this.getPointArray (-1, nValues));
break;
}
return;
case 1766856708:
var value = null;
var prop = "color";
switch (tokenValue.tok) {
case 7:
var values =  Clazz.newIntArray (nValues, 0);
for (var i = nValues; --i >= 0; ) {
var svi = sv.get (i);
pt = J.script.SV.ptValue (svi);
if (pt != null) {
values[i] = J.util.ColorUtil.colorPtToInt (pt);
} else if (svi.tok == 2) {
values[i] = svi.intValue;
} else {
values[i] = J.util.ColorUtil.getArgbFromString (svi.asString ());
if (values[i] == 0) values[i] = svi.asInt ();
}if (values[i] == 0) this.errorStr2 (50, "ARRAY", svi.asString ());
}
value = values;
prop = "colorValues";
break;
case 8:
value = Integer.$valueOf (J.util.ColorUtil.colorPtToInt (tokenValue.value));
break;
case 4:
value = tokenValue.value;
break;
default:
value = Integer.$valueOf (J.script.SV.iValue (tokenValue));
break;
}
this.setShapePropertyBs (0, prop, value, bs);
return;
case 1826248715:
case 1288701960:
if (tokenValue.tok != 7) sValue = J.script.SV.sValue (tokenValue);
break;
case 1087375365:
case 1095763976:
this.clearDefinedVariableAtomSets ();
isStrProperty = false;
break;
}
switch (tokenValue.tok) {
case 7:
if (isStrProperty) list = J.script.SV.listValue (tokenValue);
 else fvalues = J.script.SV.flistValue (tokenValue, nValues);
break;
case 4:
if (sValue == null) list = J.util.Parser.getTokens (J.script.SV.sValue (tokenValue));
break;
}
if (list != null) {
nValues = list.length;
if (!isStrProperty) {
fvalues =  Clazz.newFloatArray (nValues, 0);
for (var i = nValues; --i >= 0; ) fvalues[i] = (tok == 1087375365 ? J.util.Elements.elementNumberFromSymbol (list[i], false) : J.util.Parser.parseFloatStr (list[i]));

}if (tokenValue.tok != 7 && nValues == 1) {
if (isStrProperty) sValue = list[0];
 else fValue = fvalues[0];
iValue = Clazz.floatToInt (fValue);
list = null;
fvalues = null;
}}this.viewer.setAtomProperty (bs, tok, iValue, fValue, sValue, fvalues, list);
}, $fz.isPrivate = true, $fz), "J.util.BS,~N,~N,~N,J.script.T");
Clazz.overrideMethod (c$, "getDefinedAtomSets", 
function () {
return this.definedAtomSets;
});
Clazz.overrideMethod (c$, "getContextVariables", 
function () {
return this.contextVariables;
});
Clazz.overrideMethod (c$, "getScript", 
function () {
return this.$script;
});
$_M(c$, "compileScript", 
function (filename, strScript, debugCompiler) {
this.scriptFileName = filename;
strScript = this.fixScriptPath (strScript, filename);
this.restoreScriptContext (this.compiler.compile (filename, strScript, false, false, debugCompiler, false), false, false, false);
this.isStateScript = (this.$script.indexOf ("# Jmol state version ") >= 0);
this.forceNoAddHydrogens = (this.isStateScript && this.$script.indexOf ("pdbAddHydrogens") < 0);
var s = this.$script;
this.pc = this.setScriptExtensions ();
if (!this.chk && this.viewer.scriptEditorVisible && strScript.indexOf ("\u0001## EDITOR_IGNORE ##") < 0) this.viewer.scriptStatus ("");
this.$script = s;
return !this.$error;
}, "~S,~S,~B");
$_M(c$, "fixScriptPath", 
($fz = function (strScript, filename) {
if (filename != null && strScript.indexOf ("$SCRIPT_PATH$") >= 0) {
var path = filename;
var pt = Math.max (filename.lastIndexOf ("|"), filename.lastIndexOf ("/"));
path = path.substring (0, pt + 1);
strScript = J.util.TextFormat.simpleReplace (strScript, "$SCRIPT_PATH$/", path);
strScript = J.util.TextFormat.simpleReplace (strScript, "$SCRIPT_PATH$", path);
}return strScript;
}, $fz.isPrivate = true, $fz), "~S,~S");
$_M(c$, "setScriptExtensions", 
($fz = function () {
var extensions = this.scriptExtensions;
if (extensions == null) return 0;
var pt = extensions.indexOf ("##SCRIPT_STEP");
if (pt >= 0) {
this.executionStepping = true;
}pt = extensions.indexOf ("##SCRIPT_START=");
if (pt < 0) return 0;
pt = J.util.Parser.parseInt (extensions.substring (pt + 15));
if (pt == -2147483648) return 0;
for (this.pc = 0; this.pc < this.lineIndices.length; this.pc++) {
if (this.lineIndices[this.pc][0] > pt || this.lineIndices[this.pc][1] >= pt) break;
}
if (this.pc > 0 && this.pc < this.lineIndices.length && this.lineIndices[this.pc][0] > pt) --this.pc;
return this.pc;
}, $fz.isPrivate = true, $fz));
Clazz.overrideMethod (c$, "runScript", 
function (script) {
if (!this.viewer.isPreviewOnly ()) this.runScriptBuffer (script, this.outputBuffer);
}, "~S");
$_M(c$, "compileScriptFileInternal", 
($fz = function (filename, localPath, remotePath, scriptPath) {
if (filename.toLowerCase ().indexOf ("javascript:") == 0) return this.compileScript (filename, this.viewer.jsEval (filename.substring (11)), this.debugScript);
var data =  new Array (2);
data[0] = filename;
if (!this.viewer.getFileAsStringBin (data)) {
this.setErrorMessage ("io error reading " + data[0] + ": " + data[1]);
return false;
}if (("\n" + data[1]).indexOf ("\nJmolManifest.txt\n") >= 0) {
var path;
if (filename.endsWith (".all.pngj") || filename.endsWith (".all.png")) {
path = "|state.spt";
filename += "|";
} else {
data[0] = filename += "|JmolManifest.txt";
if (!this.viewer.getFileAsStringBin (data)) {
this.setErrorMessage ("io error reading " + data[0] + ": " + data[1]);
return false;
}path = J.io.JmolBinary.getManifestScriptPath (data[1]);
}if (path != null && path.length > 0) {
data[0] = filename = filename.substring (0, filename.lastIndexOf ("|")) + path;
if (!this.viewer.getFileAsStringBin (data)) {
this.setErrorMessage ("io error reading " + data[0] + ": " + data[1]);
return false;
}}}this.scriptFileName = filename;
data[1] = J.io.JmolBinary.getEmbeddedScript (data[1]);
var script = this.fixScriptPath (data[1], data[0]);
if (scriptPath == null) {
scriptPath = this.viewer.getFilePath (filename, false);
scriptPath = scriptPath.substring (0, Math.max (scriptPath.lastIndexOf ("|"), scriptPath.lastIndexOf ("/")));
}script = J.viewer.FileManager.setScriptFileReferences (script, localPath, remotePath, scriptPath);
return this.compileScript (filename, script, this.debugScript);
}, $fz.isPrivate = true, $fz), "~S,~S,~S,~S");
$_M(c$, "getParameter", 
function (key, tokType) {
var v = this.getContextVariableAsVariable (key);
if (v == null) v = this.viewer.getParameter (key);
switch (tokType) {
case 1073742190:
return J.script.SV.getVariable (v);
case 4:
if (!(Clazz.instanceOf (v, J.util.JmolList))) break;
var sv = v;
var sb =  new J.util.SB ();
for (var i = 0; i < sv.size (); i++) sb.append (sv.get (i).asString ()).appendC ('\n');

return sb.toString ();
}
return (Clazz.instanceOf (v, J.script.SV) ? J.script.SV.oValue (v) : v);
}, "~S,~N");
$_M(c$, "getParameterEscaped", 
($fz = function ($var) {
var v = this.getContextVariableAsVariable ($var);
return (v == null ? "" + this.viewer.getParameterEscaped ($var) : v.escape ());
}, $fz.isPrivate = true, $fz), "~S");
$_M(c$, "getStringParameter", 
($fz = function ($var, orReturnName) {
var v = this.getContextVariableAsVariable ($var);
if (v != null) return v.asString ();
var val = "" + this.viewer.getParameter ($var);
return (val.length == 0 && orReturnName ? $var : val);
}, $fz.isPrivate = true, $fz), "~S,~B");
$_M(c$, "getNumericParameter", 
($fz = function ($var) {
if ($var.equalsIgnoreCase ("_modelNumber")) {
var modelIndex = this.viewer.getCurrentModelIndex ();
return Integer.$valueOf (modelIndex < 0 ? 0 : this.viewer.getModelFileNumber (modelIndex));
}var v = this.getContextVariableAsVariable ($var);
if (v == null) {
var val = this.viewer.getParameter ($var);
if (!(Clazz.instanceOf (val, String))) return val;
v = J.script.SV.newVariable (4, val);
}return J.script.SV.nValue (v);
}, $fz.isPrivate = true, $fz), "~S");
$_M(c$, "getContextVariableAsVariable", 
($fz = function ($var) {
if ($var.equals ("expressionBegin")) return null;
$var = $var.toLowerCase ();
if (this.contextVariables != null && this.contextVariables.containsKey ($var)) return this.contextVariables.get ($var);
var context = this.thisContext;
while (context != null) {
if (context.isFunction == true) return null;
if (context.contextVariables != null && context.contextVariables.containsKey ($var)) return context.contextVariables.get ($var);
context = context.parentContext;
}
return null;
}, $fz.isPrivate = true, $fz), "~S");
$_M(c$, "getStringObjectAsVariable", 
($fz = function (s, key) {
if (s == null || s.length == 0) return s;
var v = J.script.SV.unescapePointOrBitsetAsVariable (s);
if (Clazz.instanceOf (v, String) && key != null) v = this.viewer.setUserVariable (key, J.script.SV.newVariable (4, v));
return v;
}, $fz.isPrivate = true, $fz), "~S,~S");
Clazz.overrideMethod (c$, "evalFunctionFloat", 
function (func, params, values) {
try {
var p = params;
for (var i = 0; i < values.length; i++) p.get (i).value = Float.$valueOf (values[i]);

var f = func;
return J.script.SV.fValue (this.runFunctionRet (f, f.name, p, null, true, false, false));
} catch (e) {
if (Clazz.exceptionOf (e, Exception)) {
return NaN;
} else {
throw e;
}
}
}, "~O,~O,~A");
$_M(c$, "runFunctionRet", 
function ($function, name, params, tokenAtom, getReturn, setContextPath, allowThreads) {
if ($function == null) {
$function = this.viewer.getFunction (name);
if ($function == null) return null;
if (setContextPath) this.contextPath += " >> function " + name;
} else if (setContextPath) {
this.contextPath += " >> " + name;
}this.pushContext (null, "funcRet");
if (this.allowJSThreads) this.allowJSThreads = allowThreads;
var isTry = ($function.getTok () == 364558);
this.thisContext.isTryCatch = isTry;
this.thisContext.isFunction = !isTry;
this.functionName = name;
if (isTry) {
this.viewer.resetError ();
this.thisContext.displayLoadErrorsSave = this.viewer.displayLoadErrors;
this.thisContext.tryPt = ($t$ = ++ J.script.ScriptEvaluator.tryPt, J.script.ScriptEvaluator.prototype.tryPt = J.script.ScriptEvaluator.tryPt, $t$);
this.viewer.displayLoadErrors = false;
this.restoreFunction ($function, params, tokenAtom);
this.contextVariables.put ("_breakval", J.script.SV.newScriptVariableInt (2147483647));
this.contextVariables.put ("_errorval", J.script.SV.newVariable (4, ""));
var cv = this.contextVariables;
this.executeCommands (true);
while (this.thisContext.tryPt > J.script.ScriptEvaluator.tryPt) this.popContext (false, false);

this.processTry (cv);
return null;
} else if (Clazz.instanceOf ($function, J.api.JmolParallelProcessor)) {
{
this.parallelProcessor = $function;
this.restoreFunction ($function, params, tokenAtom);
this.dispatchCommands (false, true);
($function).runAllProcesses (this.viewer);
}} else {
this.restoreFunction ($function, params, tokenAtom);
this.dispatchCommands (false, true);
}var v = (getReturn ? this.getContextVariableAsVariable ("_retval") : null);
this.popContext (false, false);
return v;
}, "J.api.JmolScriptFunction,~S,J.util.JmolList,J.script.SV,~B,~B,~B");
$_M(c$, "processTry", 
($fz = function (cv) {
this.viewer.displayLoadErrors = this.thisContext.displayLoadErrorsSave;
this.popContext (false, false);
var err = this.viewer.getParameter ("_errormessage");
if (err.length > 0) {
cv.put ("_errorval", J.script.SV.newVariable (4, err));
this.viewer.resetError ();
}cv.put ("_tryret", cv.get ("_retval"));
var ret = cv.get ("_tryret");
if (ret.value != null || ret.intValue != 2147483647) {
this.returnCmd (ret);
return;
}var errMsg = (cv.get ("_errorval")).value;
if (errMsg.length == 0) {
var iBreak = (cv.get ("_breakval")).intValue;
if (iBreak != 2147483647) {
this.breakCmd (this.pc - iBreak);
return;
}}if (this.pc + 1 < this.aatoken.length && this.aatoken[this.pc + 1][0].tok == 102412) {
var ct = this.aatoken[this.pc + 1][0];
if (ct.contextVariables != null && ct.name0 != null) ct.contextVariables.put (ct.name0, J.script.SV.newVariable (4, errMsg));
ct.intValue = (errMsg.length > 0 ? 1 : -1) * Math.abs (ct.intValue);
}}, $fz.isPrivate = true, $fz), "java.util.Map");
$_M(c$, "restoreFunction", 
($fz = function (f, params, tokenAtom) {
var $function = f;
this.aatoken = $function.aatoken;
this.lineNumbers = $function.lineNumbers;
this.lineIndices = $function.lineIndices;
this.$script = $function.script;
this.pc = 0;
if ($function.names != null) {
this.contextVariables =  new java.util.Hashtable ();
$function.setVariables (this.contextVariables, params);
}if (tokenAtom != null) this.contextVariables.put ("_x", tokenAtom);
}, $fz.isPrivate = true, $fz), "J.api.JmolScriptFunction,J.util.JmolList,J.script.SV");
$_M(c$, "clearDefinedVariableAtomSets", 
($fz = function () {
this.definedAtomSets.remove ("# variable");
}, $fz.isPrivate = true, $fz));
$_M(c$, "defineSets", 
($fz = function () {
if (!this.definedAtomSets.containsKey ("# static")) {
for (var i = 0; i < J.viewer.JC.predefinedStatic.length; i++) this.defineAtomSet (J.viewer.JC.predefinedStatic[i]);

this.defineAtomSet ("# static");
}if (this.definedAtomSets.containsKey ("# variable")) return;
for (var i = 0; i < J.viewer.JC.predefinedVariable.length; i++) this.defineAtomSet (J.viewer.JC.predefinedVariable[i]);

for (var i = J.util.Elements.elementNumberMax; --i >= 0; ) {
var definition = " elemno=" + i;
this.defineAtomSet ("@" + J.util.Elements.elementNameFromNumber (i) + definition);
this.defineAtomSet ("@_" + J.util.Elements.elementSymbolFromNumber (i) + definition);
}
for (var i = 4; --i >= 0; ) {
var definition = "@" + J.util.Elements.altElementNameFromIndex (i) + " _e=" + J.util.Elements.altElementNumberFromIndex (i);
this.defineAtomSet (definition);
}
for (var i = J.util.Elements.altElementMax; --i >= 4; ) {
var ei = J.util.Elements.altElementNumberFromIndex (i);
var def = " _e=" + ei;
var definition = "@_" + J.util.Elements.altElementSymbolFromIndex (i);
this.defineAtomSet (definition + def);
definition = "@_" + J.util.Elements.altIsotopeSymbolFromIndex (i);
this.defineAtomSet (definition + def);
definition = "@_" + J.util.Elements.altIsotopeSymbolFromIndex2 (i);
this.defineAtomSet (definition + def);
definition = "@" + J.util.Elements.altElementNameFromIndex (i);
if (definition.length > 1) this.defineAtomSet (definition + def);
var e = J.util.Elements.getElementNumber (ei);
ei = J.util.Elements.getNaturalIsotope (e);
if (ei > 0) {
def = J.util.Elements.elementSymbolFromNumber (e);
this.defineAtomSet ("@_" + def + ei + " _e=" + e);
this.defineAtomSet ("@_" + ei + def + " _e=" + e);
}}
this.defineAtomSet ("# variable");
}, $fz.isPrivate = true, $fz));
$_M(c$, "defineAtomSet", 
($fz = function (script) {
if (script.indexOf ("#") == 0) {
this.definedAtomSets.put (script, Boolean.TRUE);
return;
}var sc = this.compiler.compile ("#predefine", script, true, false, false, false);
if (sc.errorType != null) {
this.viewer.scriptStatus ("JmolConstants.java ERROR: predefined set compile error:" + script + "\ncompile error:" + sc.errorMessageUntranslated);
return;
}if (sc.aatoken.length != 1) {
this.viewer.scriptStatus ("JmolConstants.java ERROR: predefinition does not have exactly 1 command:" + script);
return;
}var statement = sc.aatoken[0];
if (statement.length <= 2) {
this.viewer.scriptStatus ("JmolConstants.java ERROR: bad predefinition length:" + script);
return;
}var tok = statement[1].tok;
if (!J.script.T.tokAttr (tok, 1073741824) && !J.script.T.tokAttr (tok, 3145728)) {
this.viewer.scriptStatus ("JmolConstants.java ERROR: invalid variable name:" + script);
return;
}var name = (statement[1].value).toLowerCase ();
if (name.startsWith ("dynamic_")) name = "!" + name.substring (8);
this.definedAtomSets.put (name, statement);
}, $fz.isPrivate = true, $fz), "~S");
$_M(c$, "lookupIdentifierValue", 
function (identifier) {
var bs = this.lookupValue (identifier, false);
if (bs != null) return J.util.BSUtil.copy (bs);
bs = this.getAtomBits (1073741824, identifier);
return (bs == null ?  new J.util.BS () : bs);
}, "~S");
$_M(c$, "lookupValue", 
($fz = function (setName, plurals) {
if (this.chk) {
return  new J.util.BS ();
}this.defineSets ();
setName = setName.toLowerCase ();
var value = this.definedAtomSets.get (setName);
var isDynamic = false;
if (value == null) {
value = this.definedAtomSets.get ("!" + setName);
isDynamic = (value != null);
}if (Clazz.instanceOf (value, J.util.BS)) return value;
if (Clazz.instanceOf (value, Array)) {
this.pushContext (null, "lookupValue");
var bs = this.atomExpression (value, -2, 0, true, false, true, true);
this.popContext (false, false);
if (!isDynamic) this.definedAtomSets.put (setName, bs);
return bs;
}if (plurals) return null;
var len = setName.length;
if (len < 5) return null;
if (setName.charAt (len - 1) != 's') return null;
if (setName.endsWith ("ies")) setName = setName.substring (0, len - 3) + 'y';
 else setName = setName.substring (0, len - 1);
return this.lookupValue (setName, true);
}, $fz.isPrivate = true, $fz), "~S,~B");
Clazz.overrideMethod (c$, "deleteAtomsInVariables", 
function (bsDeleted) {
for (var entry, $entry = this.definedAtomSets.entrySet ().iterator (); $entry.hasNext () && ((entry = $entry.next ()) || true);) {
var value = entry.getValue ();
if (Clazz.instanceOf (value, J.util.BS)) {
J.util.BSUtil.deleteBits (value, bsDeleted);
if (!entry.getKey ().startsWith ("!")) this.viewer.setUserVariable ("@" + entry.getKey (), J.script.SV.newVariable (10, value));
}}
}, "J.util.BS");
$_M(c$, "setStatement", 
($fz = function (pc) {
this.st = this.aatoken[pc];
this.slen = this.st.length;
if (this.slen == 0) return true;
var fixed;
var i;
var tok;
for (i = 1; i < this.slen; i++) {
if (this.st[i] == null) {
this.slen = i;
return true;
}if (this.st[i].tok == 1060866) break;
}
if (i == this.slen) return i == this.slen;
switch (this.st[0].tok) {
case 102436:
case 135368713:
case 1073741824:
if (this.tokAt (1) == 269484048) return true;
}
fixed =  new Array (this.slen);
fixed[0] = this.st[0];
var isExpression = false;
var j = 1;
for (i = 1; i < this.slen; i++) {
if (this.st[i] == null) continue;
switch (tok = this.getToken (i).tok) {
default:
fixed[j] = this.st[i];
break;
case 1048577:
case 1048578:
isExpression = (tok == 1048577);
fixed[j] = this.st[i];
break;
case 1060866:
if (++i == this.slen) this.invArg ();
var v;
var forceString = (this.theToken.intValue == 4);
var s;
var $var = this.parameterAsString (i);
var isClauseDefine = (this.tokAt (i) == 1048577);
var isSetAt = (j == 1 && this.st[0] === J.script.T.tokenSetCmd);
if (isClauseDefine) {
var vt = this.parameterExpressionToken (++i);
i = this.iToken;
v = (vt.tok == 7 ? vt : J.script.SV.oValue (vt));
} else {
if (this.tokAt (i) == 2) {
v = this.viewer.getAtomBits (1095763969, Integer.$valueOf (this.st[i].intValue));
} else {
v = this.getParameter ($var, 0);
}if (!isExpression && !isSetAt) isClauseDefine = true;
}tok = this.tokAt (0);
forceString = new Boolean (forceString | (J.script.T.tokAttr (tok, 20480) || tok == 135271429)).valueOf ();
if (Clazz.instanceOf (v, J.script.SV)) {
fixed[j] = v;
if (isExpression && fixed[j].tok == 7) {
var bs = J.script.SV.getBitSet (v, true);
fixed[j] = J.script.SV.newVariable (10, bs == null ? this.getAtomBitSet (J.script.SV.sValue (fixed[j])) : bs);
}} else if (Clazz.instanceOf (v, Boolean)) {
fixed[j] = ((v).booleanValue () ? J.script.T.tokenOn : J.script.T.tokenOff);
} else if (Clazz.instanceOf (v, Integer)) {
fixed[j] = J.script.T.tv (2, (v).intValue (), v);
} else if (Clazz.instanceOf (v, Float)) {
fixed[j] = J.script.T.tv (3, J.script.ScriptEvaluator.getFloatEncodedInt ("" + v), v);
} else if (Clazz.instanceOf (v, String)) {
if (!forceString) {
if ((tok != 1085443 || j > 1 && this.st[1].tok != 537022465) && J.script.T.tokAttr (tok, 36864)) {
v = this.getParameter (v, 1073742190);
}if (Clazz.instanceOf (v, String)) {
v = this.getStringObjectAsVariable (v, null);
}}if (Clazz.instanceOf (v, J.script.SV)) {
fixed[j] = v;
} else {
s = v;
if (isExpression && !forceString) {
fixed[j] = J.script.T.o (10, this.getAtomBitSet (s));
} else {
tok = (isSetAt ? J.script.T.getTokFromName (s) : isClauseDefine || forceString || s.length == 0 || s.indexOf (".") >= 0 || s.indexOf (" ") >= 0 || s.indexOf ("=") >= 0 || s.indexOf (";") >= 0 || s.indexOf ("[") >= 0 || s.indexOf ("{") >= 0 ? 4 : 1073741824);
fixed[j] = J.script.T.o (tok, v);
}}} else if (Clazz.instanceOf (v, J.util.BS)) {
fixed[j] = J.script.SV.newVariable (10, v);
} else if (Clazz.instanceOf (v, J.util.P3)) {
fixed[j] = J.script.SV.newVariable (8, v);
} else if (Clazz.instanceOf (v, J.util.P4)) {
fixed[j] = J.script.SV.newVariable (9, v);
} else if (Clazz.instanceOf (v, J.util.Matrix3f)) {
fixed[j] = J.script.SV.newVariable (11, v);
} else if (Clazz.instanceOf (v, J.util.Matrix4f)) {
fixed[j] = J.script.SV.newVariable (12, v);
} else if (Clazz.instanceOf (v, java.util.Map)) {
fixed[j] = J.script.SV.newVariable (6, v);
} else if (Clazz.instanceOf (v, J.util.JmolList)) {
var sv = v;
var bs = null;
for (var k = 0; k < sv.size (); k++) {
var svk = sv.get (k);
if (svk.tok != 10) {
bs = null;
break;
}if (bs == null) bs =  new J.util.BS ();
bs.or (svk.value);
}
fixed[j] = (bs == null ? J.script.SV.getVariable (v) : J.script.T.o (10, bs));
} else {
var center = this.getObjectCenter ($var, -2147483648, -2147483648);
if (center == null) this.invArg ();
fixed[j] = J.script.T.o (8, center);
}if (isSetAt && !J.script.T.tokAttr (fixed[j].tok, 536870912)) this.invArg ();
break;
}
j++;
}
this.st = fixed;
for (i = j; i < this.st.length; i++) this.st[i] = null;

this.slen = j;
return true;
}, $fz.isPrivate = true, $fz), "~N");
$_M(c$, "clearState", 
($fz = function (tQuiet) {
this.thisContext = null;
this.scriptLevel = 0;
this.setErrorMessage (null);
this.contextPath = "";
this.tQuiet = tQuiet;
}, $fz.isPrivate = true, $fz), "~B");
Clazz.overrideMethod (c$, "getThisContext", 
function () {
return this.thisContext;
});
Clazz.overrideMethod (c$, "pushContextDown", 
function (why) {
this.scriptLevel--;
this.pushContext2 (null, why);
}, "~S");
$_M(c$, "pushContext", 
($fz = function (token, why) {
if (this.scriptLevel == 100) this.error (44);
this.pushContext2 (token, why);
}, $fz.isPrivate = true, $fz), "J.script.ContextToken,~S");
$_M(c$, "pushContext2", 
($fz = function (token, why) {
this.thisContext = this.getScriptContext (why);
this.thisContext.token = token;
if (token == null) {
this.scriptLevel = ++this.thisContext.scriptLevel;
} else {
this.thisContext.scriptLevel = -1;
this.contextVariables =  new java.util.Hashtable ();
if (token.contextVariables != null) for (var key, $key = token.contextVariables.keySet ().iterator (); $key.hasNext () && ((key = $key.next ()) || true);) J.script.ScriptCompiler.addContextVariable (this.contextVariables, key);

}if (this.debugScript || this.isCmdLine_c_or_C_Option) J.util.Logger.info ("-->>-------------".substring (0, Math.max (17, this.scriptLevel + 5)) + this.scriptLevel + " " + this.scriptFileName + " " + token + " " + this.thisContext.id);
}, $fz.isPrivate = true, $fz), "J.script.ContextToken,~S");
Clazz.overrideMethod (c$, "getScriptContext", 
function (why) {
var context =  new J.script.ScriptContext ();
if (this.debugScript) J.util.Logger.info ("creating context " + context.id + " for " + why);
context.scriptLevel = this.scriptLevel;
context.parentContext = this.thisContext;
context.contextPath = this.contextPath;
context.scriptFileName = this.scriptFileName;
context.parallelProcessor = this.parallelProcessor;
context.functionName = this.functionName;
context.script = this.$script;
context.lineNumbers = this.lineNumbers;
context.lineIndices = this.lineIndices;
context.aatoken = this.aatoken;
context.statement = this.st;
context.statementLength = this.slen;
context.pc = this.pc;
context.lineEnd = this.lineEnd;
context.pcEnd = this.pcEnd;
context.iToken = this.iToken;
context.outputBuffer = this.outputBuffer;
context.contextVariables = this.contextVariables;
context.isStateScript = this.isStateScript;
context.errorMessage = this.errorMessage;
context.errorType = this.errorType;
context.iCommandError = this.iCommandError;
context.chk = this.chk;
context.executionStepping = this.executionStepping;
context.executionPaused = this.executionPaused;
context.scriptExtensions = this.scriptExtensions;
context.mustResumeEval = this.mustResumeEval;
context.allowJSThreads = this.allowJSThreads;
return context;
}, "~S");
$_M(c$, "popContext", 
function (isFlowCommand, statementOnly) {
if (this.thisContext == null) return;
if (this.thisContext.scriptLevel > 0) this.scriptLevel = this.thisContext.scriptLevel - 1;
var scTemp = (isFlowCommand ? this.getScriptContext ("popFlow") : null);
this.restoreScriptContext (this.thisContext, true, isFlowCommand, statementOnly);
if (scTemp != null) this.restoreScriptContext (scTemp, true, false, true);
if (this.debugScript || this.isCmdLine_c_or_C_Option) J.util.Logger.info ("--<<-------------".substring (0, Math.max (17, this.scriptLevel + 5)) + this.scriptLevel + " " + this.scriptFileName + " " + (this.thisContext == null ? "" : "" + this.thisContext.id));
}, "~B,~B");
$_M(c$, "restoreScriptContext", 
($fz = function (context, isPopContext, isFlowCommand, statementOnly) {
this.executing = !this.chk;
if (context == null) return;
if (this.debugScript || this.isCmdLine_c_or_C_Option) J.util.Logger.info ("--<<-------------".substring (0, Math.max (17, this.scriptLevel + 5)) + this.scriptLevel + " " + this.scriptFileName + " isPop " + isPopContext + " " + context.id);
if (!isFlowCommand) {
this.st = context.statement;
this.slen = context.statementLength;
this.pc = context.pc;
this.lineEnd = context.lineEnd;
this.pcEnd = context.pcEnd;
if (statementOnly) return;
}this.mustResumeEval = context.mustResumeEval;
this.$script = context.script;
this.lineNumbers = context.lineNumbers;
this.lineIndices = context.lineIndices;
this.aatoken = context.aatoken;
this.contextVariables = context.contextVariables;
this.scriptExtensions = context.scriptExtensions;
if (isPopContext) {
this.contextPath = context.contextPath;
this.scriptFileName = context.scriptFileName;
this.parallelProcessor = context.parallelProcessor;
this.functionName = context.functionName;
this.iToken = context.iToken;
this.outputBuffer = context.outputBuffer;
this.isStateScript = context.isStateScript;
this.thisContext = context.parentContext;
this.allowJSThreads = context.allowJSThreads;
} else {
this.$error = (context.errorType != null);
this.errorMessage = context.errorMessage;
this.errorMessageUntranslated = context.errorMessageUntranslated;
this.iCommandError = context.iCommandError;
this.errorType = context.errorType;
}}, $fz.isPrivate = true, $fz), "J.script.ScriptContext,~B,~B,~B");
$_M(c$, "getContext", 
($fz = function (withVariables) {
var sb =  new J.util.SB ();
var context = this.thisContext;
while (context != null) {
if (withVariables) {
if (context.contextVariables != null) {
sb.append (this.getScriptID (context));
sb.append (J.viewer.StateManager.getVariableList (context.contextVariables, 80, true, false));
}} else {
sb.append (J.script.ScriptEvaluator.getErrorLineMessage (context.functionName, context.scriptFileName, this.getLinenumber (context), context.pc, J.script.ScriptEvaluator.statementAsString (this.viewer, context.statement, -9999, this.logMessages)));
}context = context.parentContext;
}
if (withVariables) {
if (this.contextVariables != null) {
sb.append (this.getScriptID (null));
sb.append (J.viewer.StateManager.getVariableList (this.contextVariables, 80, true, false));
}} else {
sb.append (J.script.ScriptEvaluator.getErrorLineMessage (this.functionName, this.scriptFileName, this.getLinenumber (null), this.pc, J.script.ScriptEvaluator.statementAsString (this.viewer, this.st, -9999, this.logMessages)));
}return sb.toString ();
}, $fz.isPrivate = true, $fz), "~B");
$_M(c$, "getLinenumber", 
($fz = function (c) {
return (c == null ? this.lineNumbers[this.pc] : c.lineNumbers[c.pc]);
}, $fz.isPrivate = true, $fz), "J.script.ScriptContext");
$_M(c$, "getScriptID", 
($fz = function (context) {
var fuName = (context == null ? this.functionName : "function " + context.functionName);
var fiName = (context == null ? this.scriptFileName : context.scriptFileName);
return "\n# " + fuName + " (file " + fiName + (context == null ? "" : " context " + context.id) + ")\n";
}, $fz.isPrivate = true, $fz), "J.script.ScriptContext");
Clazz.overrideMethod (c$, "setException", 
function (sx, msg, untranslated) {
sx.untranslated = (untranslated == null ? msg : untranslated);
this.errorType = msg;
this.iCommandError = this.pc;
if (sx.message == null) {
sx.message = "";
return;
}var s = J.script.ScriptEvaluator.getContextTrace (this.viewer, this.getScriptContext ("setException"), null, true).toString ();
while (this.thisContext != null && !this.thisContext.isTryCatch) this.popContext (false, false);

sx.message += s;
sx.untranslated += s;
if (this.thisContext != null || this.chk || msg.indexOf ("file recognized as a script file:") >= 0) return;
J.util.Logger.error ("eval ERROR: " + this.toString ());
if (this.viewer.autoExit) this.viewer.exitJmol ();
}, "J.script.ScriptException,~S,~S");
Clazz.overrideMethod (c$, "getErrorMessage", 
function () {
return this.errorMessage;
});
Clazz.overrideMethod (c$, "getErrorMessageUntranslated", 
function () {
return this.errorMessageUntranslated == null ? this.errorMessage : this.errorMessageUntranslated;
});
$_M(c$, "setErrorMessage", 
($fz = function (err) {
this.errorMessageUntranslated = null;
if (err == null) {
this.$error = false;
this.errorType = null;
this.errorMessage = null;
this.iCommandError = -1;
return;
}this.$error = true;
if (this.errorMessage == null) this.errorMessage = J.i18n.GT._ ("script ERROR: ");
this.errorMessage += err;
}, $fz.isPrivate = true, $fz), "~S");
$_M(c$, "planeExpected", 
($fz = function () {
this.errorMore (38, "{a b c d}", "\"xy\" \"xz\" \"yz\" \"x=...\" \"y=...\" \"z=...\"", "$xxxxx");
}, $fz.isPrivate = true, $fz));
$_M(c$, "integerOutOfRange", 
function (min, max) {
this.errorStr2 (21, "" + min, "" + max);
}, "~N,~N");
$_M(c$, "numberOutOfRange", 
($fz = function (min, max) {
this.errorStr2 (36, "" + min, "" + max);
}, $fz.isPrivate = true, $fz), "~N,~N");
$_M(c$, "errorAt", 
function (iError, i) {
this.iToken = i;
this.errorOrWarn (iError, null, null, null, false);
}, "~N,~N");
$_M(c$, "invArg", 
($fz = function () {
this.error (22);
}, $fz.isPrivate = true, $fz));
$_M(c$, "error", 
function (iError) {
this.errorOrWarn (iError, null, null, null, false);
}, "~N");
$_M(c$, "errorStr", 
function (iError, value) {
this.errorOrWarn (iError, value, null, null, false);
}, "~N,~S");
$_M(c$, "errorStr2", 
function (iError, value, more) {
this.errorOrWarn (iError, value, more, null, false);
}, "~N,~S,~S");
$_M(c$, "errorMore", 
function (iError, value, more, more2) {
this.errorOrWarn (iError, value, more, more2, false);
}, "~N,~S,~S,~S");
$_M(c$, "warning", 
($fz = function (iError, value, more) {
this.errorOrWarn (iError, value, more, null, true);
}, $fz.isPrivate = true, $fz), "~N,~S,~S");
$_M(c$, "errorOrWarn", 
function (iError, value, more, more2, warningOnly) {
var strError = this.ignoreError ? null : J.script.ScriptEvaluator.errorString (iError, value, more, more2, true);
var strUntranslated = (!this.ignoreError && J.i18n.GT.getDoTranslate () ? J.script.ScriptEvaluator.errorString (iError, value, more, more2, false) : null);
if (!warningOnly) this.evalError (strError, strUntranslated);
this.showString (strError);
}, "~N,~S,~S,~S,~B");
$_M(c$, "evalError", 
function (message, strUntranslated) {
if (this.ignoreError) throw  new NullPointerException ();
if (!this.chk) {
this.setCursorWait (false);
this.viewer.setBooleanProperty ("refreshing", true);
this.viewer.setStringProperty ("_errormessage", strUntranslated);
}throw  new J.script.ScriptException (this, message, strUntranslated, true);
}, "~S,~S");
c$.errorString = $_M(c$, "errorString", 
function (iError, value, more, more2, translated) {
var doTranslate = false;
if (!translated && (doTranslate = J.i18n.GT.getDoTranslate ()) == true) J.i18n.GT.setDoTranslate (false);
var msg;
switch (iError) {
default:
msg = "Unknown error message number: " + iError;
break;
case 0:
msg = J.i18n.GT._ ("x y z axis expected");
break;
case 1:
msg = J.i18n.GT._ ("{0} not allowed with background model displayed");
break;
case 2:
msg = J.i18n.GT._ ("bad argument count");
break;
case 3:
msg = J.i18n.GT._ ("Miller indices cannot all be zero.");
break;
case 4:
msg = J.i18n.GT._ ("bad [R,G,B] color");
break;
case 5:
msg = J.i18n.GT._ ("boolean expected");
break;
case 6:
msg = J.i18n.GT._ ("boolean or number expected");
break;
case 7:
msg = J.i18n.GT._ ("boolean, number, or {0} expected");
break;
case 56:
msg = J.i18n.GT._ ("cannot set value");
break;
case 8:
msg = J.i18n.GT._ ("color expected");
break;
case 9:
msg = J.i18n.GT._ ("a color or palette name (Jmol, Rasmol) is required");
break;
case 10:
msg = J.i18n.GT._ ("command expected");
break;
case 11:
msg = J.i18n.GT._ ("{x y z} or $name or (atom expression) required");
break;
case 12:
msg = J.i18n.GT._ ("draw object not defined");
break;
case 13:
msg = J.i18n.GT._ ("unexpected end of script command");
break;
case 14:
msg = J.i18n.GT._ ("valid (atom expression) expected");
break;
case 15:
msg = J.i18n.GT._ ("(atom expression) or integer expected");
break;
case 16:
msg = J.i18n.GT._ ("filename expected");
break;
case 17:
msg = J.i18n.GT._ ("file not found");
break;
case 18:
msg = J.i18n.GT._ ("incompatible arguments");
break;
case 19:
msg = J.i18n.GT._ ("insufficient arguments");
break;
case 20:
msg = J.i18n.GT._ ("integer expected");
break;
case 21:
msg = J.i18n.GT._ ("integer out of range ({0} - {1})");
break;
case 22:
msg = J.i18n.GT._ ("invalid argument");
break;
case 23:
msg = J.i18n.GT._ ("invalid parameter order");
break;
case 24:
msg = J.i18n.GT._ ("keyword expected");
break;
case 25:
msg = J.i18n.GT._ ("no MO coefficient data available");
break;
case 26:
msg = J.i18n.GT._ ("An MO index from 1 to {0} is required");
break;
case 27:
msg = J.i18n.GT._ ("no MO basis/coefficient data available for this frame");
break;
case 28:
msg = J.i18n.GT._ ("no MO occupancy data available");
break;
case 29:
msg = J.i18n.GT._ ("Only one molecular orbital is available in this file");
break;
case 30:
msg = J.i18n.GT._ ("{0} require that only one model be displayed");
break;
case 55:
msg = J.i18n.GT._ ("{0} requires that only one model be loaded");
break;
case 31:
msg = J.i18n.GT._ ("No data available");
break;
case 32:
msg = J.i18n.GT._ ("No partial charges were read from the file; Jmol needs these to render the MEP data.");
break;
case 33:
msg = J.i18n.GT._ ("No unit cell");
break;
case 34:
msg = J.i18n.GT._ ("number expected");
break;
case 35:
msg = J.i18n.GT._ ("number must be ({0} or {1})");
break;
case 36:
msg = J.i18n.GT._ ("decimal number out of range ({0} - {1})");
break;
case 37:
msg = J.i18n.GT._ ("object name expected after '$'");
break;
case 38:
msg = J.i18n.GT._ ("plane expected -- either three points or atom expressions or {0} or {1} or {2}");
break;
case 39:
msg = J.i18n.GT._ ("property name expected");
break;
case 40:
msg = J.i18n.GT._ ("space group {0} was not found.");
break;
case 41:
msg = J.i18n.GT._ ("quoted string expected");
break;
case 42:
msg = J.i18n.GT._ ("quoted string or identifier expected");
break;
case 43:
msg = J.i18n.GT._ ("too many rotation points were specified");
break;
case 44:
msg = J.i18n.GT._ ("too many script levels");
break;
case 45:
msg = J.i18n.GT._ ("unrecognized atom property");
break;
case 46:
msg = J.i18n.GT._ ("unrecognized bond property");
break;
case 47:
msg = J.i18n.GT._ ("unrecognized command");
break;
case 48:
msg = J.i18n.GT._ ("runtime unrecognized expression");
break;
case 49:
msg = J.i18n.GT._ ("unrecognized object");
break;
case 50:
msg = J.i18n.GT._ ("unrecognized {0} parameter");
break;
case 51:
msg = J.i18n.GT._ ("unrecognized {0} parameter in Jmol state script (set anyway)");
break;
case 52:
msg = J.i18n.GT._ ("unrecognized SHOW parameter --  use {0}");
break;
case 53:
msg = "{0}";
break;
case 54:
msg = J.i18n.GT._ ("write what? {0} or {1} \"filename\"");
break;
}
if (msg.indexOf ("{0}") < 0) {
if (value != null) msg += ": " + value;
} else {
msg = J.util.TextFormat.simpleReplace (msg, "{0}", value);
if (msg.indexOf ("{1}") >= 0) msg = J.util.TextFormat.simpleReplace (msg, "{1}", more);
 else if (more != null) msg += ": " + more;
if (msg.indexOf ("{2}") >= 0) msg = J.util.TextFormat.simpleReplace (msg, "{2}", more);
}if (doTranslate) J.i18n.GT.setDoTranslate (true);
return msg;
}, "~N,~S,~S,~S,~B");
c$.getErrorLineMessage = $_M(c$, "getErrorLineMessage", 
function (functionName, filename, lineCurrent, pcCurrent, lineInfo) {
var err = "\n----";
if (filename != null || functionName != null) err += "line " + lineCurrent + " command " + (pcCurrent + 1) + " of " + (functionName == null ? filename : functionName.equals ("try") ? "try" : "function " + functionName) + ":";
err += "\n         " + lineInfo;
return err;
}, "~S,~S,~N,~N,~S");
$_M(c$, "toString", 
function () {
var str =  new J.util.SB ();
str.append ("Eval\n pc:");
str.appendI (this.pc);
str.append ("\n");
str.appendI (this.aatoken.length);
str.append (" statements\n");
for (var i = 0; i < this.aatoken.length; ++i) {
str.append ("----\n");
var atoken = this.aatoken[i];
for (var j = 0; j < atoken.length; ++j) {
str.appendO (atoken[j]);
str.appendC ('\n');
}
str.appendC ('\n');
}
str.append ("END\n");
return str.toString ();
});
c$.statementAsString = $_M(c$, "statementAsString", 
function (viewer, statement, iTok, doLogMessages) {
if (statement.length == 0) return "";
var sb =  new J.util.SB ();
var tok = statement[0].tok;
switch (tok) {
case 0:
return statement[0].value;
case 1150985:
if (statement.length == 2 && (statement[1].tok == 135368713 || statement[1].tok == 102436)) return ((statement[1].value)).toString ();
}
var useBraces = true;
var inBrace = false;
var inClauseDefine = false;
var setEquals = (statement.length > 1 && tok == 1085443 && statement[0].value.equals ("") && (statement[0].intValue == 61 || statement[0].intValue == 35) && statement[1].tok != 1048577);
var len = statement.length;
for (var i = 0; i < len; ++i) {
var token = statement[i];
if (token == null) {
len = i;
break;
}if (iTok == i - 1) sb.append (" <<");
if (i != 0) sb.appendC (' ');
if (i == 2 && setEquals) {
if ((setEquals = (token.tok != 269484436)) || statement[0].intValue == 35) {
sb.append (setEquals ? "= " : "== ");
if (!setEquals) continue;
}}if (iTok == i && token.tok != 1048578) sb.append (">> ");
switch (token.tok) {
case 1048577:
if (useBraces) sb.append ("{");
continue;
case 1048578:
if (inClauseDefine && i == statement.length - 1) useBraces = false;
if (useBraces) sb.append ("}");
continue;
case 269484096:
case 269484097:
break;
case 1048586:
case 1048590:
inBrace = (token.tok == 1048586);
break;
case 1060866:
if (i > 0 && (token.value).equals ("define")) {
sb.append ("@");
if (i + 1 < statement.length && statement[i + 1].tok == 1048577) {
if (!useBraces) inClauseDefine = true;
useBraces = true;
}continue;
}break;
case 1048589:
sb.append ("true");
continue;
case 1048588:
sb.append ("false");
continue;
case 135280132:
break;
case 2:
sb.appendI (token.intValue);
continue;
case 8:
case 9:
case 10:
sb.append (J.script.SV.sValue (token));
continue;
case 7:
case 6:
sb.append ((token).escape ());
continue;
case 5:
sb.appendC ('^');
continue;
case 1048615:
if (token.intValue != 2147483647) sb.appendI (token.intValue);
 else sb.append (J.modelset.Group.getSeqcodeStringFor (J.script.ScriptEvaluator.getSeqCode (token)));
token = statement[++i];
sb.appendC (' ');
sb.append (inBrace ? "-" : "- ");
case 1048614:
if (token.intValue != 2147483647) sb.appendI (token.intValue);
 else sb.append (J.modelset.Group.getSeqcodeStringFor (J.script.ScriptEvaluator.getSeqCode (token)));
continue;
case 1048609:
sb.append ("*:");
sb.append (viewer.getChainIDStr (token.intValue));
continue;
case 1048607:
sb.append ("*%");
if (token.value != null) sb.append (token.value.toString ());
continue;
case 1048610:
sb.append ("*/");
case 1048611:
case 3:
if (token.intValue < 2147483647) {
sb.append (J.util.Escape.escapeModelFileNumber (token.intValue));
} else {
sb.append ("" + token.value);
}continue;
case 1048613:
sb.appendC ('[');
sb.append (J.modelset.Group.getGroup3For (token.intValue));
sb.appendC (']');
continue;
case 1048612:
sb.appendC ('[');
sb.appendO (token.value);
sb.appendC (']');
continue;
case 1048608:
sb.append ("*.");
break;
case 1095761925:
if (Clazz.instanceOf (token.value, J.util.P3)) {
var pt = token.value;
sb.append ("cell=").append (J.util.Escape.eP (pt));
continue;
}break;
case 4:
sb.append ("\"").appendO (token.value).append ("\"");
continue;
case 269484436:
case 269484434:
case 269484433:
case 269484432:
case 269484435:
case 269484438:
if (token.intValue == 1716520985) {
sb.append (statement[++i].value).append (" ");
} else if (token.intValue != 2147483647) sb.append (J.script.T.nameOf (token.intValue)).append (" ");
break;
case 364558:
continue;
case 1150985:
sb.append ("end");
continue;
default:
if (J.script.T.tokAttr (token.tok, 1073741824) || !doLogMessages) break;
sb.appendC ('\n').append (token.toString ()).appendC ('\n');
continue;
}
if (token.value != null) sb.append (token.value.toString ());
}
if (iTok >= len - 1 && iTok != 9999) sb.append (" <<");
return sb.toString ();
}, "J.viewer.Viewer,~A,~N,~B");
$_M(c$, "getShapeProperty", 
function (shapeType, propertyName) {
return this.sm.getShapePropertyIndex (shapeType, propertyName, -2147483648);
}, "~N,~S");
$_M(c$, "getShapePropertyData", 
($fz = function (shapeType, propertyName, data) {
return this.sm.getShapePropertyData (shapeType, propertyName, data);
}, $fz.isPrivate = true, $fz), "~N,~S,~A");
$_M(c$, "getShapePropertyIndex", 
($fz = function (shapeType, propertyName, index) {
return this.sm.getShapePropertyIndex (shapeType, propertyName, index);
}, $fz.isPrivate = true, $fz), "~N,~S,~N");
$_M(c$, "setObjectMad", 
($fz = function (iShape, name, mad) {
if (this.chk) return;
this.viewer.setObjectMad (iShape, name, mad);
}, $fz.isPrivate = true, $fz), "~N,~S,~N");
$_M(c$, "setObjectArgb", 
($fz = function (str, argb) {
if (this.chk) return;
this.viewer.setObjectArgb (str, argb);
}, $fz.isPrivate = true, $fz), "~S,~N");
$_M(c$, "setShapeProperty", 
function (shapeType, propertyName, propertyValue) {
if (!this.chk) this.sm.setShapePropertyBs (shapeType, propertyName, propertyValue, null);
}, "~N,~S,~O");
$_M(c$, "setShapePropertyBs", 
($fz = function (iShape, propertyName, propertyValue, bs) {
if (!this.chk) this.sm.setShapePropertyBs (iShape, propertyName, propertyValue, bs);
}, $fz.isPrivate = true, $fz), "~N,~S,~O,J.util.BS");
$_M(c$, "setShapeSizeBs", 
function (shapeType, size, bs) {
if (this.chk) return;
this.sm.setShapeSizeBs (shapeType, size, null, bs);
}, "~N,~N,J.util.BS");
$_M(c$, "setShapeSize", 
($fz = function (shapeType, rd) {
if (this.chk) return;
this.sm.setShapeSizeBs (shapeType, 0, rd, null);
}, $fz.isPrivate = true, $fz), "~N,J.atomdata.RadiusData");
$_M(c$, "setBooleanProperty", 
($fz = function (key, value) {
if (!this.chk) this.viewer.setBooleanProperty (key, value);
}, $fz.isPrivate = true, $fz), "~S,~B");
$_M(c$, "setIntProperty", 
($fz = function (key, value) {
if (!this.chk) this.viewer.setIntProperty (key, value);
return true;
}, $fz.isPrivate = true, $fz), "~S,~N");
$_M(c$, "setFloatProperty", 
($fz = function (key, value) {
if (!this.chk) this.viewer.setFloatProperty (key, value);
return true;
}, $fz.isPrivate = true, $fz), "~S,~N");
$_M(c$, "setStringProperty", 
($fz = function (key, value) {
if (!this.chk) this.viewer.setStringProperty (key, value);
}, $fz.isPrivate = true, $fz), "~S,~S");
$_M(c$, "showString", 
function (str) {
this.showStringPrint (str, false);
}, "~S");
$_M(c$, "showStringPrint", 
($fz = function (str, isPrint) {
if (this.chk || str == null) return;
if (this.outputBuffer != null) this.outputBuffer.append (str).appendC ('\n');
 else this.viewer.showString (str, isPrint);
}, $fz.isPrivate = true, $fz), "~S,~B");
$_M(c$, "scriptStatusOrBuffer", 
($fz = function (s) {
if (this.chk) return;
if (this.outputBuffer != null) {
this.outputBuffer.append (s).appendC ('\n');
return;
}this.viewer.scriptStatus (s);
}, $fz.isPrivate = true, $fz), "~S");
$_M(c$, "atomExpressionAt", 
function (index) {
if (!this.checkToken (index)) this.errorAt (2, index);
return this.atomExpression (this.st, index, 0, true, false, true, true);
}, "~N");
$_M(c$, "atomExpression", 
function (code, pcStart, pcStop, allowRefresh, allowUnderflow, mustBeBitSet, andNotDeleted) {
this.isBondSet = false;
if (code !== this.st) {
this.tempStatement = this.st;
this.st = code;
}var rpn =  new J.script.ScriptMathProcessor (this, false, false, mustBeBitSet);
var val;
var comparisonValue = 2147483647;
var refreshed = false;
this.iToken = 1000;
var ignoreSubset = (pcStart < 0);
var isInMath = false;
var nExpress = 0;
var atomCount = this.viewer.getAtomCount ();
if (ignoreSubset) pcStart = -pcStart;
ignoreSubset = new Boolean (ignoreSubset | this.chk).valueOf ();
if (pcStop == 0 && code.length > pcStart) pcStop = pcStart + 1;
expression_loop : for (var pc = pcStart; pc < pcStop; ++pc) {
this.iToken = pc;
var instruction = code[pc];
if (instruction == null) break;
var value = instruction.value;
switch (instruction.tok) {
case 1048577:
pcStart = pc;
pcStop = code.length;
nExpress++;
break;
case 1048578:
nExpress--;
if (nExpress > 0) continue;
break expression_loop;
case 1048586:
if (this.isPoint3f (pc)) {
var pt = this.getPoint3f (pc, true);
if (pt != null) {
rpn.addXPt (pt);
pc = this.iToken;
break;
}}break;
case 1048590:
if (pc > 0 && code[pc - 1].tok == 1048586) rpn.addXBs ( new J.util.BS ());
break;
case 269484096:
isInMath = true;
rpn.addOp (instruction);
break;
case 269484097:
isInMath = false;
rpn.addOp (instruction);
break;
case 1060866:
rpn.addXBs (this.getAtomBitSet (value));
break;
case 135267841:
rpn.addXVar (J.script.SV.newScriptVariableToken (instruction));
rpn.addXVar (J.script.SV.newVariable (9, this.hklParameter (pc + 2)));
pc = this.iToken;
break;
case 135266319:
rpn.addXVar (J.script.SV.newScriptVariableToken (instruction));
rpn.addXVar (J.script.SV.newVariable (9, this.planeParameter (pc + 2)));
pc = this.iToken;
break;
case 1048582:
rpn.addXVar (J.script.SV.newScriptVariableToken (instruction));
rpn.addXPt (this.getPoint3f (pc + 2, true));
pc = this.iToken;
break;
case 4:
var s = value;
if (s.indexOf ("({") == 0) {
var bs = J.util.Escape.uB (s);
if (bs != null) {
rpn.addXBs (bs);
break;
}}rpn.addXVar (J.script.SV.newScriptVariableToken (instruction));
if (s.equals ("hkl")) {
rpn.addXVar (J.script.SV.newVariable (9, this.hklParameter (pc + 2)));
pc = this.iToken;
}break;
case 135267336:
case 135267335:
case 1238369286:
case 135266324:
case 135402505:
case 135266310:
case 269484080:
rpn.addOp (instruction);
break;
case 1048579:
rpn.addXBs (this.viewer.getModelUndeletedAtomsBitSet (-1));
break;
case 1048587:
rpn.addXBs ( new J.util.BS ());
break;
case 1048589:
case 1048588:
rpn.addXVar (J.script.SV.newScriptVariableToken (instruction));
break;
case 1114638363:
rpn.addXBs (J.util.BSUtil.copy (this.viewer.getSelectionSet (false)));
break;
case 3145770:
rpn.addXBs (J.util.BSUtil.copy (this.viewer.getHiddenSet ()));
break;
case 1060869:
rpn.addXBs (J.util.BSUtil.copy (this.viewer.getMotionFixedAtoms ()));
break;
case 3145768:
rpn.addXBs (J.util.BSUtil.copyInvert (this.viewer.getHiddenSet (), atomCount));
break;
case 3145776:
rpn.addXBs (this.viewer.getBaseModelBitSet ());
break;
case 3145774:
if (!this.chk && !refreshed) this.viewer.setModelVisibility ();
refreshed = true;
rpn.addXBs (this.viewer.getVisibleSet ());
break;
case 3145766:
if (!this.chk && allowRefresh) this.refresh ();
rpn.addXBs (this.viewer.getClickableSet ());
break;
case 1048608:
if (this.viewer.allowSpecAtom ()) {
var atomID = instruction.intValue;
if (atomID > 0) rpn.addXBs (this.compareInt (1095761922, 269484436, atomID));
 else rpn.addXBs (this.getAtomBits (instruction.tok, value));
} else {
rpn.addXBs (this.lookupIdentifierValue ("_" + value));
}break;
case 3145764:
case 3145732:
case 1613758470:
case 1048585:
case 3145742:
case 3145744:
case 3145746:
case 3145748:
case 3145750:
case 1048612:
case 1048607:
case 3145772:
case 1089470478:
case 3145778:
case 1614417948:
rpn.addXBs (this.getAtomBits (instruction.tok, value));
break;
case 1048610:
case 1048611:
var iModel = instruction.intValue;
if (iModel == 2147483647 && Clazz.instanceOf (value, Integer)) {
iModel = (value).intValue ();
if (!this.viewer.haveFileSet ()) {
rpn.addXBs (this.getAtomBits (1048610, Integer.$valueOf (iModel)));
break;
}if (iModel <= 2147) iModel = iModel * 1000000;
}rpn.addXBs (this.bitSetForModelFileNumber (iModel));
break;
case 1048613:
case 1048609:
rpn.addXBs (this.getAtomBits (instruction.tok, Integer.$valueOf (instruction.intValue)));
break;
case 1048614:
if (isInMath) rpn.addXNum (J.script.SV.newScriptVariableInt (instruction.intValue));
 else rpn.addXBs (this.getAtomBits (1048614, Integer.$valueOf (J.script.ScriptEvaluator.getSeqCode (instruction))));
break;
case 1048615:
if (isInMath) {
rpn.addXNum (J.script.SV.newScriptVariableInt (instruction.intValue));
rpn.addOp (J.script.T.tokenMinus);
rpn.addXNum (J.script.SV.newScriptVariableInt (code[++pc].intValue));
break;
}var chainID = (pc + 3 < code.length && code[pc + 2].tok == 269484160 && code[pc + 3].tok == 1048609 ? code[pc + 3].intValue : -1);
rpn.addXBs (this.getAtomBits (1048615, [J.script.ScriptEvaluator.getSeqCode (instruction), J.script.ScriptEvaluator.getSeqCode (code[++pc]), chainID]));
if (chainID != -1) pc += 2;
break;
case 1095761926:
case 1095761925:
var pt = value;
rpn.addXBs (this.getAtomBits (instruction.tok, [Clazz.doubleToInt (Math.floor (pt.x * 1000)), Clazz.doubleToInt (Math.floor (pt.y * 1000)), Clazz.doubleToInt (Math.floor (pt.z * 1000))]));
break;
case 3145758:
rpn.addXBs (this.viewer.getModelUndeletedAtomsBitSet (this.viewer.getCurrentModelIndex ()));
break;
case 1613758476:
case 3145730:
case 1115297793:
case 1613758488:
case 137363468:
case 3145735:
case 3145736:
case 3145738:
case 3145754:
case 3145756:
rpn.addXBs (this.lookupIdentifierValue (value));
break;
case 269484435:
case 269484434:
case 269484433:
case 269484432:
case 269484436:
case 269484438:
if (pc + 1 == code.length) this.invArg ();
val = code[++pc].value;
var tokOperator = instruction.tok;
var tokWhat = instruction.intValue;
var property = (tokWhat == 1716520985 ? val : null);
if (property != null) {
if (pc + 1 == code.length) this.invArg ();
val = code[++pc].value;
}if (tokWhat == 1095766022 && tokOperator != 269484436) this.invArg ();
if (this.chk) {
rpn.addXBs ( new J.util.BS ());
break;
}var isModel = (tokWhat == 1095766028);
var isIntProperty = J.script.T.tokAttr (tokWhat, 1095761920);
var isFloatProperty = J.script.T.tokAttr (tokWhat, 1112539136);
var isIntOrFloat = isIntProperty && isFloatProperty;
var isStringProperty = !isIntProperty && J.script.T.tokAttr (tokWhat, 1087373312);
if (tokWhat == 1087375365) isIntProperty = !(isStringProperty = false);
var tokValue = code[pc].tok;
comparisonValue = code[pc].intValue;
var comparisonFloat = NaN;
if (Clazz.instanceOf (val, J.util.P3)) {
if (tokWhat == 1766856708) {
comparisonValue = J.util.ColorUtil.colorPtToInt (val);
tokValue = 2;
isIntProperty = true;
}} else if (Clazz.instanceOf (val, String)) {
if (tokWhat == 1766856708) {
comparisonValue = J.util.ColorUtil.getArgbFromString (val);
if (comparisonValue == 0 && J.script.T.tokAttr (tokValue, 1073741824)) {
val = this.getStringParameter (val, true);
if ((val).startsWith ("{")) {
val = J.util.Escape.uP (val);
if (Clazz.instanceOf (val, J.util.P3)) comparisonValue = J.util.ColorUtil.colorPtToInt (val);
 else comparisonValue = 0;
} else {
comparisonValue = J.util.ColorUtil.getArgbFromString (val);
}}tokValue = 2;
isIntProperty = true;
} else if (isStringProperty) {
if (J.script.T.tokAttr (tokValue, 1073741824)) val = this.getStringParameter (val, true);
} else {
if (J.script.T.tokAttr (tokValue, 1073741824)) val = this.getNumericParameter (val);
if (Clazz.instanceOf (val, String)) {
if (tokWhat == 1641025539 || tokWhat == 1238369286 || tokWhat == 1087375365) isStringProperty = !(isIntProperty = (comparisonValue != 2147483647));
 else val = J.script.SV.nValue (code[pc]);
}if (Clazz.instanceOf (val, Integer)) comparisonFloat = comparisonValue = (val).intValue ();
 else if (Clazz.instanceOf (val, Float) && isModel) comparisonValue = J.modelset.ModelCollection.modelFileNumberFromFloat ((val).floatValue ());
}}if (isStringProperty && !(Clazz.instanceOf (val, String))) {
val = "" + val;
}if (Clazz.instanceOf (val, Integer) || tokValue == 2) {
if (isModel) {
if (comparisonValue >= 1000000) tokWhat = -1095766028;
} else if (isIntOrFloat) {
isFloatProperty = false;
} else if (isFloatProperty) {
comparisonFloat = comparisonValue;
}} else if (Clazz.instanceOf (val, Float)) {
if (isModel) {
tokWhat = -1095766028;
} else {
comparisonFloat = (val).floatValue ();
if (isIntOrFloat) {
isIntProperty = false;
} else if (isIntProperty) {
comparisonValue = Clazz.floatToInt (comparisonFloat);
}}} else if (!isStringProperty) {
this.iToken++;
this.invArg ();
}if (isModel && comparisonValue >= 1000000 && comparisonValue % 1000000 == 0) {
comparisonValue /= 1000000;
tokWhat = 1229984263;
isModel = false;
}if (tokWhat == -1095766028 && tokOperator == 269484436) {
rpn.addXBs (this.bitSetForModelFileNumber (comparisonValue));
break;
}if (value != null && (value).indexOf ("-") >= 0) {
if (isIntProperty) comparisonValue = -comparisonValue;
 else if (!Float.isNaN (comparisonFloat)) comparisonFloat = -comparisonFloat;
}var data = (tokWhat == 1716520985 ? this.viewer.getDataFloat (property) : null);
rpn.addXBs (isIntProperty ? this.compareInt (tokWhat, tokOperator, comparisonValue) : isStringProperty ? this.compareString (tokWhat, tokOperator, val) : this.compareFloatData (tokWhat, data, tokOperator, comparisonFloat));
break;
case 3:
case 2:
rpn.addXNum (J.script.SV.newScriptVariableToken (instruction));
break;
case 10:
var bs1 = J.util.BSUtil.copy (value);
rpn.addXBs (bs1);
break;
case 8:
rpn.addXPt (value);
break;
default:
if (J.script.T.tokAttr (instruction.tok, 269484032)) {
if (!rpn.addOp (instruction)) this.invArg ();
break;
}if (!(Clazz.instanceOf (value, String))) {
rpn.addXObj (value);
break;
}val = this.getParameter (value, 0);
if (isInMath) {
rpn.addXObj (val);
break;
}if (Clazz.instanceOf (val, String)) val = this.getStringObjectAsVariable (val, null);
if (Clazz.instanceOf (val, J.util.JmolList)) {
var bs = J.script.SV.unEscapeBitSetArray (val, true);
if (bs == null) val = value;
 else val = bs;
}if (Clazz.instanceOf (val, String)) val = this.lookupIdentifierValue (value);
rpn.addXObj (val);
break;
}
}
this.expressionResult = rpn.getResult (allowUnderflow);
if (this.expressionResult == null) {
if (allowUnderflow) return null;
if (!this.chk) rpn.dumpStacks ("after getResult");
this.error (13);
}this.expressionResult = (this.expressionResult).value;
if (Clazz.instanceOf (this.expressionResult, String) && (mustBeBitSet || (this.expressionResult).startsWith ("({"))) {
this.expressionResult = (this.chk ?  new J.util.BS () : this.getAtomBitSet (this.expressionResult));
}if (!mustBeBitSet && !(Clazz.instanceOf (this.expressionResult, J.util.BS))) return null;
var bs = (Clazz.instanceOf (this.expressionResult, J.util.BS) ? this.expressionResult :  new J.util.BS ());
this.isBondSet = (Clazz.instanceOf (this.expressionResult, J.modelset.Bond.BondSet));
if (!this.isBondSet) {
this.viewer.excludeAtoms (bs, ignoreSubset);
if (bs.length () > this.viewer.getAtomCount ()) bs.clearAll ();
}if (this.tempStatement != null) {
this.st = this.tempStatement;
this.tempStatement = null;
}return bs;
}, "~A,~N,~N,~B,~B,~B,~B");
$_M(c$, "compareFloatData", 
($fz = function (tokWhat, data, tokOperator, comparisonFloat) {
var bs =  new J.util.BS ();
var atomCount = this.viewer.getAtomCount ();
var modelSet = this.viewer.modelSet;
var atoms = modelSet.atoms;
var propertyFloat = 0;
this.viewer.autoCalculate (tokWhat);
for (var i = atomCount; --i >= 0; ) {
var match = false;
var atom = atoms[i];
switch (tokWhat) {
default:
propertyFloat = J.modelset.Atom.atomPropertyFloat (this.viewer, atom, tokWhat);
break;
case 1716520985:
if (data == null || data.length <= i) continue;
propertyFloat = data[i];
}
match = J.script.ScriptEvaluator.compareFloat (tokOperator, propertyFloat, comparisonFloat);
if (match) bs.set (i);
}
return bs;
}, $fz.isPrivate = true, $fz), "~N,~A,~N,~N");
$_M(c$, "compareString", 
($fz = function (tokWhat, tokOperator, comparisonString) {
var bs =  new J.util.BS ();
var atoms = this.viewer.modelSet.atoms;
var atomCount = this.viewer.getAtomCount ();
var isCaseSensitive = (tokWhat == 1087373316 && this.viewer.getBoolean (603979822));
if (!isCaseSensitive) comparisonString = comparisonString.toLowerCase ();
for (var i = atomCount; --i >= 0; ) {
var propertyString = J.modelset.Atom.atomPropertyString (this.viewer, atoms[i], tokWhat);
if (!isCaseSensitive) propertyString = propertyString.toLowerCase ();
if (this.compareStringValues (tokOperator, propertyString, comparisonString)) bs.set (i);
}
return bs;
}, $fz.isPrivate = true, $fz), "~N,~N,~S");
$_M(c$, "compareInt", 
function (tokWhat, tokOperator, comparisonValue) {
var propertyValue = 2147483647;
var propertyBitSet = null;
var bitsetComparator = tokOperator;
var bitsetBaseValue = comparisonValue;
var atomCount = this.viewer.getAtomCount ();
var modelSet = this.viewer.modelSet;
var atoms = modelSet.atoms;
var imax = -1;
var imin = 0;
var iModel = -1;
var cellRange = null;
var nOps = 0;
var bs;
switch (tokWhat) {
case 1297090050:
switch (bitsetComparator) {
case 269484433:
case 269484432:
imax = 2147483647;
break;
}
break;
case 1095761923:
try {
switch (tokOperator) {
case 269484435:
return J.util.BSUtil.newBitSet2 (0, comparisonValue);
case 269484434:
return J.util.BSUtil.newBitSet2 (0, comparisonValue + 1);
case 269484433:
return J.util.BSUtil.newBitSet2 (comparisonValue, atomCount);
case 269484432:
return J.util.BSUtil.newBitSet2 (comparisonValue + 1, atomCount);
case 269484436:
return (comparisonValue < atomCount ? J.util.BSUtil.newBitSet2 (comparisonValue, comparisonValue + 1) :  new J.util.BS ());
case 269484438:
default:
bs = J.util.BSUtil.setAll (atomCount);
if (comparisonValue >= 0) bs.clear (comparisonValue);
return bs;
}
} catch (e) {
if (Clazz.exceptionOf (e, Exception)) {
return  new J.util.BS ();
} else {
throw e;
}
}
}
bs = J.util.BSUtil.newBitSet (atomCount);
for (var i = 0; i < atomCount; ++i) {
var match = false;
var atom = atoms[i];
switch (tokWhat) {
default:
propertyValue = J.modelset.Atom.atomPropertyInt (atom, tokWhat);
break;
case 1095766022:
return J.util.BSUtil.copy (this.viewer.getConformation (-1, comparisonValue - 1, false));
case 1297090050:
propertyBitSet = atom.getAtomSymmetry ();
if (propertyBitSet == null) continue;
if (atom.getModelIndex () != iModel) {
iModel = atom.getModelIndex ();
cellRange = modelSet.getModelCellRange (iModel);
nOps = modelSet.getModelSymmetryCount (iModel);
}if (bitsetBaseValue >= 200) {
if (cellRange == null) continue;
comparisonValue = bitsetBaseValue % 1000;
var symop = Clazz.doubleToInt (bitsetBaseValue / 1000) - 1;
if (symop < 0) {
match = true;
} else if (nOps == 0 || symop >= 0 && !(match = propertyBitSet.get (symop))) {
continue;
}bitsetComparator = 1048587;
if (symop < 0) propertyValue = atom.getCellTranslation (comparisonValue, cellRange, nOps);
 else propertyValue = atom.getSymmetryTranslation (symop, cellRange, nOps);
} else if (nOps > 0) {
if (comparisonValue > nOps) {
if (bitsetComparator != 269484435 && bitsetComparator != 269484434) continue;
}if (bitsetComparator == 269484438) {
if (comparisonValue > 0 && comparisonValue <= nOps && !propertyBitSet.get (comparisonValue)) {
bs.set (i);
}continue;
}}switch (bitsetComparator) {
case 269484435:
imax = comparisonValue - 1;
break;
case 269484434:
imax = comparisonValue;
break;
case 269484433:
imin = comparisonValue - 1;
break;
case 269484432:
imin = comparisonValue;
break;
case 269484436:
imax = comparisonValue;
imin = comparisonValue - 1;
break;
case 269484438:
match = !propertyBitSet.get (comparisonValue);
break;
}
if (imin < 0) imin = 0;
if (imin < imax) {
var pt = propertyBitSet.nextSetBit (imin);
if (pt >= 0 && pt < imax) match = true;
}if (!match || propertyValue == 2147483647) tokOperator = 1048587;
}
switch (tokOperator) {
case 1048587:
break;
case 269484435:
match = (propertyValue < comparisonValue);
break;
case 269484434:
match = (propertyValue <= comparisonValue);
break;
case 269484433:
match = (propertyValue >= comparisonValue);
break;
case 269484432:
match = (propertyValue > comparisonValue);
break;
case 269484436:
match = (propertyValue == comparisonValue);
break;
case 269484438:
match = (propertyValue != comparisonValue);
break;
}
if (match) bs.set (i);
}
return bs;
}, "~N,~N,~N");
$_M(c$, "compareStringValues", 
($fz = function (tokOperator, propertyValue, comparisonValue) {
switch (tokOperator) {
case 269484436:
case 269484438:
return (J.util.TextFormat.isMatch (propertyValue, comparisonValue, true, true) == (tokOperator == 269484436));
default:
this.invArg ();
}
return false;
}, $fz.isPrivate = true, $fz), "~N,~S,~S");
c$.compareFloat = $_M(c$, "compareFloat", 
($fz = function (tokOperator, propertyFloat, comparisonFloat) {
switch (tokOperator) {
case 269484435:
return propertyFloat < comparisonFloat;
case 269484434:
return propertyFloat <= comparisonFloat;
case 269484433:
return propertyFloat >= comparisonFloat;
case 269484432:
return propertyFloat > comparisonFloat;
case 269484436:
return propertyFloat == comparisonFloat;
case 269484438:
return propertyFloat != comparisonFloat;
}
return false;
}, $fz.isPrivate = true, $fz), "~N,~N,~N");
$_M(c$, "getAtomBits", 
($fz = function (tokType, specInfo) {
return (this.chk ?  new J.util.BS () : this.viewer.getAtomBits (tokType, specInfo));
}, $fz.isPrivate = true, $fz), "~N,~O");
c$.getSeqCode = $_M(c$, "getSeqCode", 
($fz = function (instruction) {
return (instruction.intValue == 2147483647 ? (instruction.value).intValue () : J.modelset.Group.getSeqcodeFor (instruction.intValue, ' '));
}, $fz.isPrivate = true, $fz), "J.script.T");
$_M(c$, "checkLast", 
function (i) {
return this.checkLength (i + 1) - 1;
}, "~N");
$_M(c$, "checkLength", 
($fz = function (length) {
if (length >= 0) return this.checkLengthErrorPt (length, 0);
if (this.slen > -length) {
this.iToken = -length;
this.error (2);
}return this.slen;
}, $fz.isPrivate = true, $fz), "~N");
$_M(c$, "checkLengthErrorPt", 
($fz = function (length, errorPt) {
if (this.slen != length) {
this.iToken = errorPt > 0 ? errorPt : this.slen;
this.error (errorPt > 0 ? 22 : 2);
}return this.slen;
}, $fz.isPrivate = true, $fz), "~N,~N");
$_M(c$, "checkLength23", 
function () {
this.iToken = this.slen;
if (this.slen != 2 && this.slen != 3) this.error (2);
return this.slen;
});
$_M(c$, "checkLength34", 
($fz = function () {
this.iToken = this.slen;
if (this.slen != 3 && this.slen != 4) this.error (2);
return this.slen;
}, $fz.isPrivate = true, $fz));
$_M(c$, "getToken", 
function (i) {
if (!this.checkToken (i)) this.error (13);
this.theToken = this.st[i];
this.theTok = this.theToken.tok;
return this.theToken;
}, "~N");
$_M(c$, "tokAt", 
function (i) {
return (i < this.slen && this.st[i] != null ? this.st[i].tok : 0);
}, "~N");
c$.tokAtArray = $_M(c$, "tokAtArray", 
function (i, args) {
return (i < args.length && args[i] != null ? args[i].tok : 0);
}, "~N,~A");
$_M(c$, "tokenAt", 
($fz = function (i, args) {
return (i < args.length ? args[i] : null);
}, $fz.isPrivate = true, $fz), "~N,~A");
$_M(c$, "checkToken", 
($fz = function (i) {
return (this.iToken = i) < this.slen;
}, $fz.isPrivate = true, $fz), "~N");
$_M(c$, "modelNumberParameter", 
function (index) {
var iFrame = 0;
var useModelNumber = false;
switch (this.tokAt (index)) {
case 2:
useModelNumber = true;
case 3:
iFrame = this.getToken (index).intValue;
break;
case 4:
iFrame = J.script.ScriptEvaluator.getFloatEncodedInt (this.stringParameter (index));
break;
default:
this.invArg ();
}
return this.viewer.getModelNumberIndex (iFrame, useModelNumber, true);
}, "~N");
$_M(c$, "optParameterAsString", 
function (i) {
if (i >= this.slen) return "";
return this.parameterAsString (i);
}, "~N");
$_M(c$, "parameterAsString", 
function (i) {
this.getToken (i);
if (this.theToken == null) this.error (13);
return J.script.SV.sValue (this.theToken);
}, "~N");
$_M(c$, "intParameter", 
function (index) {
if (this.checkToken (index)) if (this.getToken (index).tok == 2) return this.theToken.intValue;
this.error (20);
return 0;
}, "~N");
$_M(c$, "intParameterRange", 
($fz = function (i, min, max) {
var val = this.intParameter (i);
if (val < min || val > max) this.integerOutOfRange (min, max);
return val;
}, $fz.isPrivate = true, $fz), "~N,~N,~N");
$_M(c$, "isFloatParameter", 
function (index) {
switch (this.tokAt (index)) {
case 2:
case 3:
return true;
}
return false;
}, "~N");
$_M(c$, "floatParameterRange", 
($fz = function (i, min, max) {
var val = this.floatParameter (i);
if (val < min || val > max) this.numberOutOfRange (min, max);
return val;
}, $fz.isPrivate = true, $fz), "~N,~N,~N");
$_M(c$, "floatParameter", 
function (index) {
if (this.checkToken (index)) {
this.getToken (index);
switch (this.theTok) {
case 1048615:
return -this.theToken.intValue;
case 1048614:
case 2:
return this.theToken.intValue;
case 1048611:
case 3:
return (this.theToken.value).floatValue ();
}
}this.error (34);
return 0;
}, "~N");
$_M(c$, "listParameter", 
function (i, nMin, nMax) {
var v =  new J.util.JmolList ();
var pt;
var tok = this.tokAt (i);
if (tok == 1073742195) tok = this.tokAt (++i);
var haveBrace = (tok == 1048586);
var haveSquare = (tok == 269484096);
if (haveBrace || haveSquare) i++;
var n = 0;
while (n < nMax) {
tok = this.tokAt (i);
if (haveBrace && tok == 1048590 || haveSquare && tok == 269484097) break;
switch (tok) {
case 269484080:
case 1048586:
case 1048590:
break;
case 4:
break;
case 8:
pt = this.getPoint3f (i, false);
v.addLast (Float.$valueOf (pt.x));
v.addLast (Float.$valueOf (pt.y));
v.addLast (Float.$valueOf (pt.z));
n += 3;
break;
case 9:
var pt4 = this.getPoint4f (i);
v.addLast (Float.$valueOf (pt4.x));
v.addLast (Float.$valueOf (pt4.y));
v.addLast (Float.$valueOf (pt4.z));
v.addLast (Float.$valueOf (pt4.w));
n += 4;
break;
default:
v.addLast (Float.$valueOf (this.floatParameter (i)));
n++;
if (n == nMax && haveSquare && this.tokAt (i + 1) == 1048590) i++;
}
i++;
}
if (haveBrace && this.tokAt (i++) != 1048590 || haveSquare && this.tokAt (i++) != 269484097 || n < nMin || n > nMax) this.invArg ();
this.iToken = i - 1;
return v;
}, "~N,~N,~N");
$_M(c$, "floatParameterSet", 
function (i, nMin, nMax) {
var v = null;
var fparams = null;
var n = 0;
var s = null;
this.iToken = i;
switch (this.tokAt (i)) {
case 4:
s = J.script.SV.sValue (this.st[i]);
s = J.util.TextFormat.replaceAllCharacter (s, "{},[]\"'", ' ');
fparams = J.util.Parser.parseFloatArray (s);
n = fparams.length;
break;
case 7:
fparams = J.script.SV.flistValue (this.st[i], 0);
n = fparams.length;
break;
default:
v = this.listParameter (i, nMin, nMax);
n = v.size ();
}
if (n < nMin || n > nMax) this.invArg ();
if (fparams == null) {
fparams =  Clazz.newFloatArray (n, 0);
for (var j = 0; j < n; j++) fparams[j] = (v.get (j)).floatValue ();

}return fparams;
}, "~N,~N,~N");
$_M(c$, "isArrayParameter", 
function (i) {
switch (this.tokAt (i)) {
case 7:
case 11:
case 12:
case 1073742195:
case 269484096:
return true;
}
return false;
}, "~N");
$_M(c$, "getPointArray", 
function (i, nPoints) {
var points = (nPoints < 0 ? null :  new Array (nPoints));
var vp = (nPoints < 0 ?  new J.util.JmolList () : null);
var tok = (i < 0 ? 7 : this.getToken (i++).tok);
switch (tok) {
case 7:
var v = (this.theToken).getList ();
if (nPoints >= 0 && v.size () != nPoints) this.invArg ();
nPoints = v.size ();
if (points == null) points =  new Array (nPoints);
for (var j = 0; j < nPoints; j++) if ((points[j] = J.script.SV.ptValue (v.get (j))) == null) this.invArg ();

return points;
case 1073742195:
tok = this.tokAt (i++);
break;
}
if (tok != 269484096) this.invArg ();
var n = 0;
while (tok != 269484097 && tok != 0) {
tok = this.getToken (i).tok;
switch (tok) {
case 0:
case 269484097:
break;
case 269484080:
i++;
break;
default:
if (nPoints >= 0 && n == nPoints) {
tok = 0;
break;
}var pt = this.getPoint3f (i, true);
if (points == null) vp.addLast (pt);
 else points[n] = pt;
n++;
i = this.iToken + 1;
}
}
if (tok != 269484097) this.invArg ();
if (points == null) points = vp.toArray ( new Array (vp.size ()));
return points;
}, "~N,~N");
$_M(c$, "stringParameter", 
function (index) {
if (!this.checkToken (index) || this.getToken (index).tok != 4) this.error (41);
return this.theToken.value;
}, "~N");
$_M(c$, "stringParameterSet", 
($fz = function (i) {
switch (this.tokAt (i)) {
case 4:
var s = this.stringParameter (i);
if (s.startsWith ("[\"")) {
var o = this.viewer.evaluateExpression (s);
if (Clazz.instanceOf (o, String)) return J.util.TextFormat.split (o, '\n');
}return [s];
case 1073742195:
i += 2;
break;
case 269484096:
++i;
break;
case 7:
return J.script.SV.listValue (this.getToken (i));
default:
this.invArg ();
}
var tok;
var v =  new J.util.JmolList ();
while ((tok = this.tokAt (i)) != 269484097) {
switch (tok) {
case 269484080:
break;
case 4:
v.addLast (this.stringParameter (i));
break;
default:
case 0:
this.invArg ();
}
i++;
}
this.iToken = i;
var n = v.size ();
var sParams =  new Array (n);
for (var j = 0; j < n; j++) {
sParams[j] = v.get (j);
}
return sParams;
}, $fz.isPrivate = true, $fz), "~N");
$_M(c$, "objectNameParameter", 
function (index) {
if (!this.checkToken (index)) this.error (37);
return this.parameterAsString (index);
}, "~N");
$_M(c$, "booleanParameter", 
($fz = function (i) {
if (this.slen == i) return true;
switch (this.getToken (this.checkLast (i)).tok) {
case 1048589:
return true;
case 1048588:
return false;
default:
this.error (5);
}
return false;
}, $fz.isPrivate = true, $fz), "~N");
$_M(c$, "atomCenterOrCoordinateParameter", 
($fz = function (i) {
switch (this.getToken (i).tok) {
case 10:
case 1048577:
var bs = this.atomExpression (this.st, i, 0, true, false, false, true);
if (bs != null && bs.cardinality () == 1) return this.viewer.getAtomPoint3f (bs.nextSetBit (0));
if (bs != null) return this.viewer.getAtomSetCenter (bs);
if (Clazz.instanceOf (this.expressionResult, J.util.P3)) return this.expressionResult;
this.invArg ();
break;
case 1048586:
case 8:
return this.getPoint3f (i, true);
}
this.invArg ();
return null;
}, $fz.isPrivate = true, $fz), "~N");
$_M(c$, "isCenterParameter", 
function (i) {
var tok = this.tokAt (i);
return (tok == 1048583 || tok == 1048586 || tok == 1048577 || tok == 8 || tok == 10);
}, "~N");
$_M(c$, "centerParameter", 
function (i) {
return this.centerParameterForModel (i, -2147483648);
}, "~N");
$_M(c$, "centerParameterForModel", 
($fz = function (i, modelIndex) {
var center = null;
this.expressionResult = null;
if (this.checkToken (i)) {
switch (this.getToken (i).tok) {
case 1048583:
var id = this.objectNameParameter (++i);
var index = -2147483648;
if (this.tokAt (i + 1) == 269484096) {
index = this.parameterExpressionList (-i - 1, -1, true).get (0).asInt ();
if (this.getToken (--this.iToken).tok != 269484097) this.invArg ();
}if (this.chk) return  new J.util.P3 ();
if (this.tokAt (i + 1) == 1048584 && (this.tokAt (i + 2) == 1141899267 || this.tokAt (i + 2) == 1141899270)) {
index = 2147483647;
this.iToken = i + 2;
}if ((center = this.getObjectCenter (id, index, modelIndex)) == null) this.errorStr (12, id);
break;
case 10:
case 1048577:
case 1048586:
case 8:
center = this.atomCenterOrCoordinateParameter (i);
break;
}
}if (center == null) this.error (11);
return center;
}, $fz.isPrivate = true, $fz), "~N,~N");
$_M(c$, "planeParameter", 
function (i) {
var vAB =  new J.util.V3 ();
var vAC =  new J.util.V3 ();
var plane = null;
var isNegated = (this.tokAt (i) == 269484192);
if (isNegated) i++;
if (i < this.slen) switch (this.getToken (i).tok) {
case 9:
plane = J.util.P4.newPt (this.theToken.value);
break;
case 1048583:
var id = this.objectNameParameter (++i);
if (this.chk) return  new J.util.P4 ();
var shapeType = this.sm.getShapeIdFromObjectName (id);
switch (shapeType) {
case 22:
this.setShapeProperty (22, "thisID", id);
var points = this.getShapeProperty (22, "vertices");
if (points == null || points.length < 3 || points[0] == null || points[1] == null || points[2] == null) break;
J.util.Measure.getPlaneThroughPoints (points[0], points[1], points[2],  new J.util.V3 (), vAB, vAC, plane =  new J.util.P4 ());
break;
case 24:
this.setShapeProperty (24, "thisID", id);
plane = this.getShapeProperty (24, "plane");
break;
}
break;
case 1112541205:
if (!this.checkToken (++i) || this.getToken (i++).tok != 269484436) this.evalError ("x=?", null);
plane = J.util.P4.new4 (1, 0, 0, -this.floatParameter (i));
break;
case 1112541206:
if (!this.checkToken (++i) || this.getToken (i++).tok != 269484436) this.evalError ("y=?", null);
plane = J.util.P4.new4 (0, 1, 0, -this.floatParameter (i));
break;
case 1112541207:
if (!this.checkToken (++i) || this.getToken (i++).tok != 269484436) this.evalError ("z=?", null);
plane = J.util.P4.new4 (0, 0, 1, -this.floatParameter (i));
break;
case 1073741824:
case 4:
var str = this.parameterAsString (i);
if (str.equalsIgnoreCase ("xy")) return J.util.P4.new4 (0, 0, 1, 0);
if (str.equalsIgnoreCase ("xz")) return J.util.P4.new4 (0, 1, 0, 0);
if (str.equalsIgnoreCase ("yz")) return J.util.P4.new4 (1, 0, 0, 0);
this.iToken += 2;
break;
case 1048586:
if (!this.isPoint3f (i)) {
plane = this.getPoint4f (i);
break;
}case 10:
case 1048577:
var pt1 = this.atomCenterOrCoordinateParameter (i);
if (this.getToken (++this.iToken).tok == 269484080) ++this.iToken;
var pt2 = this.atomCenterOrCoordinateParameter (this.iToken);
if (this.getToken (++this.iToken).tok == 269484080) ++this.iToken;
var pt3 = this.atomCenterOrCoordinateParameter (this.iToken);
i = this.iToken;
var norm =  new J.util.V3 ();
var w = J.util.Measure.getNormalThroughPoints (pt1, pt2, pt3, norm, vAB, vAC);
plane =  new J.util.P4 ();
plane.set (norm.x, norm.y, norm.z, w);
if (!this.chk && J.util.Logger.debugging) J.util.Logger.debug ("points: " + pt1 + pt2 + pt3 + " defined plane: " + plane);
break;
}
if (plane == null) this.planeExpected ();
if (isNegated) {
plane.scale (-1);
}return plane;
}, "~N");
$_M(c$, "hklParameter", 
function (i) {
if (!this.chk && this.viewer.getCurrentUnitCell () == null) this.error (33);
var pt = this.getPointOrPlane (i, false, true, false, true, 3, 3);
var p = this.getHklPlane (pt);
if (p == null) this.error (3);
if (!this.chk && J.util.Logger.debugging) J.util.Logger.debug ("defined plane: " + p);
return p;
}, "~N");
$_M(c$, "getHklPlane", 
function (pt) {
var vAB =  new J.util.V3 ();
var vAC =  new J.util.V3 ();
var pt1 = J.util.P3.new3 (pt.x == 0 ? 1 : 1 / pt.x, 0, 0);
var pt2 = J.util.P3.new3 (0, pt.y == 0 ? 1 : 1 / pt.y, 0);
var pt3 = J.util.P3.new3 (0, 0, pt.z == 0 ? 1 : 1 / pt.z);
if (pt.x == 0 && pt.y == 0 && pt.z == 0) {
return null;
} else if (pt.x == 0 && pt.y == 0) {
pt1.set (1, 0, pt3.z);
pt2.set (0, 1, pt3.z);
} else if (pt.y == 0 && pt.z == 0) {
pt2.set (pt1.x, 0, 1);
pt3.set (pt1.x, 1, 0);
} else if (pt.z == 0 && pt.x == 0) {
pt3.set (0, pt2.y, 1);
pt1.set (1, pt2.y, 0);
} else if (pt.x == 0) {
pt1.set (1, pt2.y, 0);
} else if (pt.y == 0) {
pt2.set (0, 1, pt3.z);
} else if (pt.z == 0) {
pt3.set (pt1.x, 0, 1);
}this.viewer.toCartesian (pt1, false);
this.viewer.toCartesian (pt2, false);
this.viewer.toCartesian (pt3, false);
var plane =  new J.util.V3 ();
var w = J.util.Measure.getNormalThroughPoints (pt1, pt2, pt3, plane, vAB, vAC);
var pt4 =  new J.util.P4 ();
pt4.set (plane.x, plane.y, plane.z, w);
return pt4;
}, "J.util.P3");
$_M(c$, "getMadParameter", 
function () {
var mad = 1;
switch (this.getToken (1).tok) {
case 1073742072:
this.restrictSelected (false, false);
break;
case 1048589:
break;
case 1048588:
mad = 0;
break;
case 2:
var radiusRasMol = this.intParameterRange (1, 0, 750);
mad = radiusRasMol * 4 * 2;
break;
case 3:
mad = Clazz.doubleToInt (Math.floor (this.floatParameterRange (1, -3, 3) * 1000 * 2));
if (mad < 0) {
this.restrictSelected (false, false);
mad = -mad;
}break;
default:
this.error (6);
}
return mad;
});
$_M(c$, "getSetAxesTypeMad", 
($fz = function (index) {
if (index == this.slen) return 1;
switch (this.getToken (this.checkLast (index)).tok) {
case 1048589:
return 1;
case 1048588:
return 0;
case 1073741926:
return -1;
case 2:
return this.intParameterRange (index, -1, 19);
case 3:
var angstroms = this.floatParameterRange (index, 0, 2);
return Clazz.doubleToInt (Math.floor (angstroms * 1000 * 2));
}
this.errorStr (7, "\"DOTTED\"");
return 0;
}, $fz.isPrivate = true, $fz), "~N");
$_M(c$, "isColorParam", 
function (i) {
var tok = this.tokAt (i);
return (tok == 570425378 || tok == 1073742195 || tok == 269484096 || tok == 7 || tok == 8 || this.isPoint3f (i) || (tok == 4 || J.script.T.tokAttr (tok, 1073741824)) && J.util.ColorUtil.getArgbFromString (this.st[i].value) != 0);
}, "~N");
$_M(c$, "getArgbParam", 
function (index) {
return this.getArgbParamOrNone (index, false);
}, "~N");
$_M(c$, "getArgbParamLast", 
($fz = function (index, allowNone) {
var icolor = this.getArgbParamOrNone (index, allowNone);
this.checkLast (this.iToken);
return icolor;
}, $fz.isPrivate = true, $fz), "~N,~B");
$_M(c$, "getArgbParamOrNone", 
($fz = function (index, allowNone) {
var pt = null;
if (this.checkToken (index)) {
switch (this.getToken (index).tok) {
default:
if (!J.script.T.tokAttr (this.theTok, 1073741824)) break;
case 570425378:
case 4:
return J.util.ColorUtil.getArgbFromString (this.parameterAsString (index));
case 1073742195:
return this.getColorTriad (index + 2);
case 269484096:
return this.getColorTriad (++index);
case 7:
var rgb = J.script.SV.flistValue (this.theToken, 3);
if (rgb != null && rgb.length != 3) pt = J.util.P3.new3 (rgb[0], rgb[1], rgb[2]);
break;
case 8:
pt = this.theToken.value;
break;
case 1048586:
pt = this.getPoint3f (index, false);
break;
case 1048587:
if (allowNone) return 0;
}
}if (pt == null) this.error (8);
return J.util.ColorUtil.colorPtToInt (pt);
}, $fz.isPrivate = true, $fz), "~N,~B");
$_M(c$, "getColorTriad", 
($fz = function (i) {
var colors =  Clazz.newFloatArray (3, 0);
var n = 0;
var hex = "";
this.getToken (i);
var pt = null;
var val = 0;
out : switch (this.theTok) {
case 2:
case 1048614:
case 3:
for (; i < this.slen; i++) {
switch (this.getToken (i).tok) {
case 269484080:
continue;
case 1073741824:
if (n != 1 || colors[0] != 0) this.error (4);
hex = "0" + this.parameterAsString (i);
break out;
case 3:
if (n > 2) this.error (4);
val = this.floatParameter (i);
break;
case 2:
if (n > 2) this.error (4);
val = this.theToken.intValue;
break;
case 1048614:
if (n > 2) this.error (4);
val = (this.theToken.value).intValue () % 256;
break;
case 269484097:
if (n != 3) this.error (4);
--i;
pt = J.util.P3.new3 (colors[0], colors[1], colors[2]);
break out;
default:
this.error (4);
}
colors[n++] = val;
}
this.error (4);
break;
case 8:
pt = this.theToken.value;
break;
case 1073741824:
hex = this.parameterAsString (i);
break;
default:
this.error (4);
}
if (this.getToken (++i).tok != 269484097) this.error (4);
if (pt != null) return J.util.ColorUtil.colorPtToInt (pt);
if ((n = J.util.ColorUtil.getArgbFromString ("[" + hex + "]")) == 0) this.error (4);
return n;
}, $fz.isPrivate = true, $fz), "~N");
$_M(c$, "isPoint3f", 
function (i) {
var isOK;
if ((isOK = (this.tokAt (i) == 8)) || this.tokAt (i) == 9 || this.isFloatParameter (i + 1) && this.isFloatParameter (i + 2) && this.isFloatParameter (i + 3) && this.isFloatParameter (i + 4)) return isOK;
this.ignoreError = true;
var t = this.iToken;
isOK = true;
try {
this.getPoint3f (i, true);
} catch (e) {
if (Clazz.exceptionOf (e, Exception)) {
isOK = false;
} else {
throw e;
}
}
this.ignoreError = false;
this.iToken = t;
return isOK;
}, "~N");
$_M(c$, "getPoint3f", 
function (i, allowFractional) {
return this.getPointOrPlane (i, false, allowFractional, true, false, 3, 3);
}, "~N,~B");
$_M(c$, "getPoint4f", 
function (i) {
return this.getPointOrPlane (i, false, false, false, false, 4, 4);
}, "~N");
$_M(c$, "getPointOrPlane", 
($fz = function (index, integerOnly, allowFractional, doConvert, implicitFractional, minDim, maxDim) {
var coord =  Clazz.newFloatArray (6, 0);
var n = 0;
this.coordinatesAreFractional = implicitFractional;
if (this.tokAt (index) == 8) {
if (minDim <= 3 && maxDim >= 3) return this.getToken (index).value;
this.invArg ();
}if (this.tokAt (index) == 9) {
if (minDim <= 4 && maxDim >= 4) return this.getToken (index).value;
this.invArg ();
}var multiplier = 1;
out : for (var i = index; i < this.st.length; i++) {
switch (this.getToken (i).tok) {
case 1048586:
case 269484080:
case 269484128:
case 269484160:
break;
case 1048590:
break out;
case 269484192:
multiplier = -1;
break;
case 1048615:
if (n == 6) this.invArg ();
coord[n++] = this.theToken.intValue;
multiplier = -1;
break;
case 2:
case 1048614:
if (n == 6) this.invArg ();
coord[n++] = this.theToken.intValue * multiplier;
multiplier = 1;
break;
case 269484208:
case 1048610:
if (!allowFractional) this.invArg ();
if (this.theTok == 269484208) this.getToken (++i);
n--;
if (n < 0 || integerOnly) this.invArg ();
if (Clazz.instanceOf (this.theToken.value, Integer) || this.theTok == 2) {
coord[n++] /= (this.theToken.intValue == 2147483647 ? (this.theToken.value).intValue () : this.theToken.intValue);
} else if (Clazz.instanceOf (this.theToken.value, Float)) {
coord[n++] /= (this.theToken.value).floatValue ();
}this.coordinatesAreFractional = true;
break;
case 3:
case 1048611:
if (integerOnly) this.invArg ();
if (n == 6) this.invArg ();
coord[n++] = (this.theToken.value).floatValue ();
break;
default:
this.invArg ();
}
}
if (n < minDim || n > maxDim) this.invArg ();
if (n == 3) {
var pt = J.util.P3.new3 (coord[0], coord[1], coord[2]);
if (this.coordinatesAreFractional && doConvert) {
this.fractionalPoint = J.util.P3.newP (pt);
if (!this.chk) this.viewer.toCartesian (pt, !this.viewer.getBoolean (603979848));
}return pt;
}if (n == 4) {
if (this.coordinatesAreFractional) this.invArg ();
var plane = J.util.P4.new4 (coord[0], coord[1], coord[2], coord[3]);
return plane;
}return coord;
}, $fz.isPrivate = true, $fz), "~N,~B,~B,~B,~B,~N,~N");
$_M(c$, "xypParameter", 
function (index) {
var tok = this.tokAt (index);
if (tok == 1073742195) tok = this.tokAt (++index);
if (tok != 269484096 || !this.isFloatParameter (++index)) return null;
var pt =  new J.util.P3 ();
pt.x = this.floatParameter (index);
if (this.tokAt (++index) == 269484080) index++;
if (!this.isFloatParameter (index)) return null;
pt.y = this.floatParameter (index);
var isPercent = (this.tokAt (++index) == 269484210);
if (isPercent) ++index;
if (this.tokAt (index) != 269484097) return null;
this.iToken = index;
pt.z = (isPercent ? -1 : 1) * 3.4028235E38;
return pt;
}, "~N");
$_M(c$, "isCommandDisplayable", 
($fz = function (i) {
if (i >= this.aatoken.length || i >= this.pcEnd || this.aatoken[i] == null) return false;
return (this.lineIndices[i][1] > this.lineIndices[i][0]);
}, $fz.isPrivate = true, $fz), "~N");
$_M(c$, "checkContinue", 
($fz = function () {
if (this.executionStopped) return false;
if (this.executionStepping && this.isCommandDisplayable (this.pc)) {
this.viewer.setScriptStatus ("Next: " + this.getNextStatement (), "stepping -- type RESUME to continue", 0, null);
this.executionPaused = true;
} else if (!this.executionPaused) {
return true;
}if (J.util.Logger.debugging) {
J.util.Logger.debug ("script execution paused at command " + (this.pc + 1) + " level " + this.scriptLevel + ": " + this.thisCommand);
}this.refresh ();
while (this.executionPaused) {
this.viewer.popHoldRepaint ("pause \u0001## REPAINT_IGNORE ##");
var script = this.viewer.getInsertedCommand ();
if (script !== "") {
this.resumePausedExecution ();
this.setErrorMessage (null);
var scSave = this.getScriptContext ("script insertion");
this.pc--;
try {
this.runScript (script);
} catch (e$$) {
if (Clazz.exceptionOf (e$$, Exception)) {
var e = e$$;
{
this.setErrorMessage ("" + e);
}
} else if (Clazz.exceptionOf (e$$, Error)) {
var er = e$$;
{
this.setErrorMessage ("" + er);
}
} else {
throw e$$;
}
}
if (this.$error) {
this.scriptStatusOrBuffer (this.errorMessage);
this.setErrorMessage (null);
}this.restoreScriptContext (scSave, true, false, false);
this.pauseExecution (false);
}this.doDelay (-100);
this.viewer.pushHoldRepaint ("pause");
}
this.notifyResumeStatus ();
return !this.$error && !this.executionStopped;
}, $fz.isPrivate = true, $fz));
Clazz.overrideMethod (c$, "notifyResumeStatus", 
function () {
if (!this.chk && !this.executionStopped && !this.executionStepping) {
this.viewer.scriptStatus ("script execution " + (this.$error || this.executionStopped ? "interrupted" : "resumed"));
}if (J.util.Logger.debugging) J.util.Logger.debug ("script execution resumed");
});
$_M(c$, "doDelay", 
($fz = function (millis) {
if (!this.useThreads ()) return;
if (this.isJS && this.allowJSThreads) throw  new J.script.ScriptInterruption (this, "delay", millis);
this.delayScript (millis);
}, $fz.isPrivate = true, $fz), "~N");
$_M(c$, "dispatchCommands", 
($fz = function (isSpt, fromFunc) {
var timeBegin = 0;
if (this.sm == null) this.sm = this.viewer.getShapeManager ();
this.debugScript = this.logMessages = false;
if (!this.chk) this.setDebugging ();
if (this.logMessages) {
timeBegin = System.currentTimeMillis ();
this.viewer.scriptStatus ("Eval.dispatchCommands():" + timeBegin);
this.viewer.scriptStatus (this.$script);
}if (this.pcEnd == 0) this.pcEnd = 2147483647;
if (this.lineEnd == 0) this.lineEnd = 2147483647;
if (this.aatoken == null) return true;
this.commandLoop (fromFunc);
if (this.chk) return true;
var script = this.viewer.getInsertedCommand ();
if (script !== "") {
this.runScriptBuffer (script, null);
} else if (isSpt && this.debugScript && this.viewer.getBoolean (603979880)) {
this.viewer.scriptStatus ("script <exiting>");
}if (!this.isJS || !this.allowJSThreads || fromFunc) return true;
if (this.mustResumeEval || this.thisContext == null) {
var done = (this.thisContext == null);
this.resumeEval (this.thisContext);
this.mustResumeEval = false;
return done;
}return true;
}, $fz.isPrivate = true, $fz), "~B,~B");
$_M(c$, "commandLoop", 
($fz = function (fromFunc) {
var lastCommand = "";
var isForCheck = false;
var vProcess = null;
var lastTime = System.currentTimeMillis ();
for (; this.pc < this.aatoken.length && this.pc < this.pcEnd; this.pc++) {
if (!this.chk && this.isJS && this.allowJSThreads && !fromFunc) {
if (!this.executionPaused && System.currentTimeMillis () - lastTime > 100) {
this.pc--;
this.doDelay (-1);
}lastTime = System.currentTimeMillis ();
}if (!this.chk && !this.checkContinue ()) break;
if (this.lineNumbers[this.pc] > this.lineEnd) break;
if (this.debugScript && !this.chk) J.util.Logger.info ("Command " + this.pc);
this.theToken = (this.aatoken[this.pc].length == 0 ? null : this.aatoken[this.pc][0]);
if (!this.historyDisabled && !this.chk && this.scriptLevel <= this.commandHistoryLevelMax && !this.tQuiet) {
var cmdLine = this.getCommand (this.pc, true, true);
if (this.theToken != null && cmdLine.length > 0 && !cmdLine.equals (lastCommand) && (this.theToken.tok == 135368713 || this.theToken.tok == 102436 || !J.script.T.tokAttr (this.theToken.tok, 102400))) this.viewer.addCommand (lastCommand = cmdLine);
}if (!this.chk) {
var script = this.viewer.getInsertedCommand ();
if (script !== "") this.runScript (script);
}if (!this.setStatement (this.pc)) {
J.util.Logger.info (this.getCommand (this.pc, true, false) + " -- STATEMENT CONTAINING @{} SKIPPED");
continue;
}this.thisCommand = this.getCommand (this.pc, false, true);
var nextCommand = this.getCommand (this.pc + 1, false, true);
this.fullCommand = this.thisCommand + (nextCommand.startsWith ("#") ? nextCommand : "");
this.getToken (0);
this.iToken = 0;
if ((this.listCommands || !this.chk && this.scriptLevel > 0) && !this.isJS) {
var milliSecDelay = this.viewer.getInt (536870922);
if (this.listCommands || milliSecDelay > 0) {
if (milliSecDelay > 0) this.delayScript (-milliSecDelay);
this.viewer.scriptEcho ("$[" + this.scriptLevel + "." + this.lineNumbers[this.pc] + "." + (this.pc + 1) + "] " + this.thisCommand);
}}if (vProcess != null && (this.theTok != 1150985 || this.slen < 2 || this.st[1].tok != 102439)) {
vProcess.addLast (this.st);
continue;
}if (this.chk) {
if (this.isCmdLine_c_or_C_Option) J.util.Logger.info (this.thisCommand);
if (this.slen == 1 && this.st[0].tok != 135368713 && this.st[0].tok != 102436) continue;
} else {
if (this.debugScript) this.logDebugScript (0);
if (this.scriptLevel == 0 && this.viewer.global.logCommands) this.viewer.log (this.thisCommand);
if (this.logMessages && this.theToken != null) J.util.Logger.debug (this.theToken.toString ());
}if (this.theToken == null) continue;
if (J.script.T.tokAttr (this.theToken.tok, 135168)) this.processShapeCommand (this.theToken.tok);
 else switch (this.theToken.tok) {
case 0:
if (this.chk || !this.viewer.getBoolean (603979880)) break;
var s = this.theToken.value;
if (s == null) break;
if (this.outputBuffer == null) this.viewer.showMessage (s);
this.scriptStatusOrBuffer (s);
break;
case 266280:
this.pushContext (this.theToken, "PUSH");
break;
case 266278:
this.popContext (true, false);
break;
case 269484066:
break;
case 20500:
case 528410:
if (this.viewer.isHeadless ()) break;
case 102412:
case 102407:
case 102408:
case 364547:
case 102402:
case 1150985:
case 364548:
case 135369224:
case 135369225:
case 102410:
case 102411:
case 102413:
case 102406:
isForCheck = this.flowControl (this.theToken.tok, isForCheck, vProcess);
if (this.theTok == 102439) vProcess = null;
break;
case 4097:
this.animation ();
break;
case 4098:
this.assign ();
break;
case 1610616835:
this.background (1);
break;
case 4100:
this.bind ();
break;
case 4101:
this.bondorder ();
break;
case 4102:
this.calculate ();
break;
case 135270422:
this.cache ();
break;
case 1069064:
this.cd ();
break;
case 12289:
this.center (1);
break;
case 4105:
this.centerAt ();
break;
case 1766856708:
this.color ();
break;
case 135270405:
this.compare ();
break;
case 1095766022:
this.configuration ();
break;
case 4106:
this.connect (1);
break;
case 528395:
this.console ();
break;
case 135270407:
this.data ();
break;
case 1060866:
this.define ();
break;
case 528397:
this.delay ();
break;
case 12291:
this.$delete ();
break;
case 554176526:
this.slab (true);
break;
case 1610625028:
this.display (true);
break;
case 266255:
case 266281:
if (this.chk) break;
if (this.pc > 0 && this.theToken.tok == 266255) this.viewer.clearScriptQueue ();
this.executionStopped = (this.pc > 0 || !this.viewer.global.useScriptQueue);
break;
case 266256:
if (this.chk) return;
this.viewer.exitJmol ();
break;
case 1229984263:
this.file ();
break;
case 1060869:
this.fixed ();
break;
case 4114:
this.font (-1, 0);
break;
case 4115:
case 1095766028:
this.model (1);
break;
case 102436:
case 135368713:
case 1073741824:
this.$function ();
break;
case 135270410:
this.getProperty ();
break;
case 20482:
this.help ();
break;
case 12294:
this.display (false);
break;
case 1612189718:
this.hbond ();
break;
case 1610616855:
this.history (1);
break;
case 544771:
this.hover ();
break;
case 266264:
if (!this.chk) this.viewer.initialize (!this.isStateScript);
break;
case 4121:
this.invertSelected ();
break;
case 135287308:
this.script (135287308, null);
break;
case 135271426:
this.load ();
break;
case 36869:
this.log ();
break;
case 1052700:
this.mapProperty ();
break;
case 20485:
this.message ();
break;
case 4126:
this.minimize ();
break;
case 135270424:
this.modulation ();
break;
case 4128:
this.move ();
break;
case 4130:
this.moveto ();
break;
case 4131:
this.navigate ();
break;
case 20487:
this.pause ();
break;
case 36865:
this.print ();
break;
case 102439:
this.pushContext (this.theToken, "PROCESS");
if (this.parallelProcessor != null) vProcess =  new J.util.JmolList ();
break;
case 135304707:
this.prompt ();
break;
case 4139:
case 4165:
this.undoRedoMove ();
break;
case 266284:
this.refresh ();
break;
case 4141:
this.reset ();
break;
case 4142:
this.restore ();
break;
case 12295:
this.restrict ();
break;
case 266287:
if (!this.chk) this.resumePausedExecution ();
break;
case 36866:
this.returnCmd (null);
break;
case 528432:
this.rotate (false, false);
break;
case 4145:
this.rotate (false, true);
break;
case 4146:
this.save ();
break;
case 1085443:
this.set ();
break;
case 135271429:
this.script (135271429, null);
break;
case 135280132:
this.select (1);
break;
case 1611141171:
this.selectionHalo (1);
break;
case 4148:
this.show ();
break;
case 554176565:
this.slab (false);
break;
case 1611141175:
this.rotate (true, false);
break;
case 1611141176:
this.ssbond ();
break;
case 266298:
if (this.pause ()) this.stepPausedExecution ();
break;
case 528443:
this.stereo ();
break;
case 1641025539:
this.structure ();
break;
case 3158024:
this.subset ();
break;
case 4156:
this.sync ();
break;
case 536875070:
this.timeout (1);
break;
case 4160:
this.translate (false);
break;
case 4162:
this.translate (true);
break;
case 4164:
this.unbind ();
break;
case 4166:
this.vibration ();
break;
case 135270421:
this.write (null);
break;
case 1060873:
this.zap (true);
break;
case 4168:
this.zoom (false);
break;
case 4170:
this.zoom (true);
break;
case 4133:
case 135270417:
case 1052714:
this.getExtension ().plot (this.st);
break;
default:
this.error (47);
}
this.setCursorWait (false);
if (this.executionStepping) {
this.executionPaused = (this.isCommandDisplayable (this.pc + 1));
}}
}, $fz.isPrivate = true, $fz), "~B");
$_M(c$, "cache", 
($fz = function () {
var tok = this.tokAt (1);
var fileName = null;
var n = 2;
switch (tok) {
case 1276118017:
case 1073742119:
fileName = this.optParameterAsString (n++);
case 1073741882:
this.checkLength (n);
if (!this.chk) {
if ("all".equals (fileName)) fileName = null;
var nBytes = this.viewer.cacheFileByName (fileName, tok == 1276118017);
this.showString (nBytes < 0 ? "cache cleared" : nBytes + " bytes " + (tok == 1276118017 ? " cached" : " removed"));
}break;
default:
this.invArg ();
}
}, $fz.isPrivate = true, $fz));
$_M(c$, "setCursorWait", 
function (TF) {
if (!this.chk) this.viewer.setCursor (TF ? 4 : 0);
}, "~B");
$_M(c$, "processShapeCommand", 
($fz = function (tok) {
var iShape = 0;
switch (tok) {
case 1611272194:
iShape = 31;
break;
case 1115297793:
iShape = 9;
break;
case 1679429641:
iShape = 32;
break;
case 1113200642:
iShape = 11;
break;
case 135174:
iShape = 23;
break;
case 135402505:
iShape = 25;
break;
case 135175:
iShape = 17;
break;
case 1113198595:
iShape = 16;
break;
case 135176:
iShape = 22;
break;
case 537022465:
iShape = 30;
break;
case 1113198596:
iShape = 20;
break;
case 1611272202:
iShape = 35;
break;
case 1113198597:
iShape = 19;
break;
case 1113200646:
iShape = 8;
break;
case 135180:
iShape = 24;
break;
case 1826248715:
iShape = 5;
break;
case 135182:
iShape = 26;
break;
case 537006096:
case 1746538509:
iShape = 6;
break;
case 1113200647:
iShape = 13;
break;
case 1183762:
iShape = 27;
break;
case 135190:
iShape = 29;
break;
case 135188:
iShape = 28;
break;
case 135192:
iShape = 21;
break;
case 1113200649:
iShape = 14;
break;
case 1113200650:
iShape = 15;
break;
case 1113200651:
iShape = 0;
break;
case 1113200652:
iShape = 7;
break;
case 1650071565:
iShape = 12;
break;
case 1708058:
iShape = 4;
break;
case 1113200654:
iShape = 10;
break;
case 1614417948:
iShape = 33;
break;
case 135198:
iShape = 18;
break;
case 659488:
iShape = 1;
break;
default:
this.error (47);
}
if (this.sm.getShape (iShape) == null && this.slen == 2) {
switch (this.st[1].tok) {
case 1048588:
case 12291:
case 1048587:
return;
}
}switch (tok) {
case 1115297793:
case 1113200642:
case 1113200647:
case 1113200649:
case 1113200650:
case 1650071565:
case 1113200654:
this.proteinShape (iShape);
return;
case 1113198595:
case 1113198597:
this.dots (iShape);
return;
case 1113198596:
this.ellipsoid ();
return;
case 1113200646:
case 1113200651:
case 1113200652:
this.setAtomShapeSize (iShape, (tok == 1113200646 ? -1.0 : 1));
return;
case 1826248715:
this.label (1);
return;
case 135198:
this.vector ();
return;
case 659488:
this.wireframe ();
return;
}
switch (tok) {
case 1611272194:
this.axes (1);
return;
case 1679429641:
this.boundbox (1);
return;
case 537022465:
this.echo (1, null, false);
return;
case 1611272202:
this.frank (1);
return;
case 537006096:
case 1746538509:
this.measure ();
return;
case 1614417948:
this.unitcell (1);
return;
case 135174:
case 135402505:
case 135175:
case 135176:
case 135180:
case 135182:
case 1183762:
case 135190:
case 135188:
case 135192:
case 1708058:
this.getExtension ().dispatch (iShape, false, this.st);
return;
}
}, $fz.isPrivate = true, $fz), "~N");
$_M(c$, "getExtension", 
($fz = function () {
return (this.scriptExt == null ? (this.scriptExt = J.api.Interface.getOptionInterface ("scriptext.ScriptExt")).init (this) : this.scriptExt);
}, $fz.isPrivate = true, $fz));
$_M(c$, "flowControl", 
($fz = function (tok, isForCheck, vProcess) {
var ct;
switch (tok) {
case 20500:
this.gotoCmd (this.parameterAsString (this.checkLast (1)));
return isForCheck;
case 528410:
if (!this.chk) this.pc = -1;
this.delay ();
return isForCheck;
}
var pt = this.st[0].intValue;
var isDone = (pt < 0 && !this.chk);
var isOK = true;
var ptNext = 0;
switch (tok) {
case 102412:
ct = this.theToken;
this.pushContext (ct, "CATCH");
if (!isDone && ct.name0 != null) this.contextVariables.put (ct.name0, ct.contextVariables.get (ct.name0));
isOK = !isDone;
break;
case 102410:
case 102413:
case 102411:
ptNext = Math.abs (this.aatoken[Math.abs (pt)][0].intValue);
switch (isDone ? 0 : this.switchCmd (this.theToken, tok)) {
case 0:
ptNext = -ptNext;
isOK = false;
break;
case -1:
isOK = false;
break;
case 1:
}
this.aatoken[this.pc][0].intValue = Math.abs (pt);
this.theToken = this.aatoken[Math.abs (pt)][0];
if (this.theToken.tok != 1150985) this.theToken.intValue = ptNext;
break;
case 135369225:
case 102402:
isOK = (!isDone && this.ifCmd ());
if (this.chk) break;
ptNext = Math.abs (this.aatoken[Math.abs (pt)][0].intValue);
ptNext = (isDone || isOK ? -ptNext : ptNext);
this.aatoken[Math.abs (pt)][0].intValue = ptNext;
if (tok == 102412) this.aatoken[this.pc][0].intValue = -pt;
break;
case 364547:
this.checkLength (1);
if (pt < 0 && !this.chk) this.pc = -pt - 1;
break;
case 364548:
this.checkLength (1);
break;
case 102406:
if (!isForCheck) this.pushContext (this.theToken, "WHILE");
isForCheck = false;
if (!this.ifCmd () && !this.chk) {
this.pc = pt;
this.popContext (true, false);
}break;
case 102407:
if (!this.chk) {
this.breakCmd (pt);
break;
}if (this.slen == 1) break;
var n = this.intParameter (this.checkLast (1));
if (this.chk) break;
for (var i = 0; i < n; i++) this.popContext (true, false);

break;
case 102408:
isForCheck = true;
if (!this.chk) this.pc = pt - 1;
if (this.slen > 1) this.intParameter (this.checkLast (1));
break;
case 135369224:
var token = this.theToken;
var pts =  Clazz.newIntArray (2, 0);
var j = 0;
var bsOrList = null;
for (var i = 1, nSkip = 0; i < this.slen && j < 2; i++) {
switch (this.tokAt (i)) {
case 1048591:
if (nSkip > 0) nSkip--;
 else pts[j++] = i;
break;
case 1073741980:
nSkip -= 2;
if (this.tokAt (++i) == 1048577 || this.tokAt (i) == 10) {
bsOrList = this.atomExpressionAt (i);
if (this.isBondSet) bsOrList =  new J.modelset.Bond.BondSet (bsOrList);
} else {
var what = this.parameterExpressionList (-i, 1, false);
if (what == null || what.size () < 1) this.invArg ();
var vl = what.get (0);
switch (vl.tok) {
case 10:
bsOrList = J.script.SV.getBitSet (vl, false);
break;
case 7:
bsOrList = vl.getList ();
break;
default:
this.invArg ();
}
}i = this.iToken;
break;
case 135280132:
nSkip += 2;
break;
}
}
if (isForCheck) {
j = (bsOrList == null ? pts[1] + 1 : 2);
} else {
this.pushContext (token, "FOR");
j = 2;
}if (this.tokAt (j) == 36868) j++;
var key = this.parameterAsString (j);
var isMinusMinus = key.equals ("--") || key.equals ("++");
if (isMinusMinus) {
key = this.parameterAsString (++j);
}var v = null;
if (J.script.T.tokAttr (this.tokAt (j), 1073741824) || (v = this.getContextVariableAsVariable (key)) != null) {
if (bsOrList == null && !isMinusMinus && this.getToken (++j).tok != 269484436) this.invArg ();
if (bsOrList == null) {
if (isMinusMinus) j -= 2;
this.setVariable (++j, this.slen - 1, key, 0);
} else {
isOK = true;
var key_incr = (key + "_incr");
if (v == null) v = this.getContextVariableAsVariable (key_incr);
if (v == null) {
if (key.startsWith ("_")) this.invArg ();
v = this.viewer.getOrSetNewVariable (key_incr, true);
}if (!isForCheck || v.tok != 10 && v.tok != 7 || v.intValue == 2147483647) {
if (isForCheck) {
isOK = false;
} else {
v.setv (J.script.SV.getVariable (bsOrList), false);
v.intValue = 1;
}} else {
v.intValue++;
}isOK = isOK && (Clazz.instanceOf (bsOrList, J.util.BS) ? J.script.SV.bsSelectVar (v).cardinality () == 1 : v.intValue <= v.getList ().size ());
if (isOK) {
v = J.script.SV.selectItemVar (v);
var t = this.getContextVariableAsVariable (key);
if (t == null) t = this.viewer.getOrSetNewVariable (key, true);
t.setv (v, false);
}}}if (bsOrList == null) isOK = this.parameterExpressionBoolean (pts[0] + 1, pts[1]);
pt++;
if (!isOK) this.popContext (true, false);
isForCheck = false;
break;
case 1150985:
switch (this.getToken (this.checkLast (1)).tok) {
case 364558:
var trycmd = this.getToken (1).value;
if (this.chk) return false;
this.runFunctionRet (trycmd, "try", null, null, true, true, true);
return false;
case 102412:
this.popContext (true, false);
break;
case 135368713:
case 102436:
this.viewer.addFunction (this.theToken.value);
return isForCheck;
case 102439:
this.addProcess (vProcess, pt, this.pc);
this.popContext (true, false);
break;
case 102410:
if (pt > 0 && this.switchCmd (this.aatoken[pt][0], 0) == -1) {
for (; pt < this.pc; pt++) if ((tok = this.aatoken[pt][0].tok) != 102413 && tok != 102411) break;

isOK = (this.pc == pt);
}break;
}
if (isOK) isOK = (this.theTok == 102412 || this.theTok == 102439 || this.theTok == 135369225 || this.theTok == 102410);
isForCheck = (this.theTok == 135369224 || this.theTok == 102406);
break;
}
if (!isOK && !this.chk) this.pc = Math.abs (pt) - 1;
return isForCheck;
}, $fz.isPrivate = true, $fz), "~N,~B,J.util.JmolList");
$_M(c$, "gotoCmd", 
($fz = function (strTo) {
var pcTo = (strTo == null ? this.aatoken.length - 1 : -1);
var s = null;
for (var i = pcTo + 1; i < this.aatoken.length; i++) {
var tokens = this.aatoken[i];
var tok = tokens[0].tok;
switch (tok) {
case 20485:
case 0:
s = tokens[tokens.length - 1].value;
if (tok == 0) s = s.substring (s.startsWith ("#") ? 1 : 2);
break;
default:
continue;
}
if (s.equalsIgnoreCase (strTo)) {
pcTo = i;
break;
}}
if (pcTo < 0) this.invArg ();
if (strTo == null) pcTo = 0;
var di = (pcTo < this.pc ? 1 : -1);
var nPush = 0;
for (var i = pcTo; i != this.pc; i += di) {
switch (this.aatoken[i][0].tok) {
case 266280:
case 102439:
case 135369224:
case 102412:
case 102406:
nPush++;
break;
case 266278:
nPush--;
break;
case 1150985:
switch (this.aatoken[i][1].tok) {
case 102439:
case 135369224:
case 102412:
case 102406:
nPush--;
}
break;
}
}
if (strTo == null) {
pcTo = 2147483647;
for (; nPush > 0; --nPush) this.popContext (false, false);

}if (nPush != 0) this.invArg ();
if (!this.chk) this.pc = pcTo - 1;
}, $fz.isPrivate = true, $fz), "~S");
$_M(c$, "breakCmd", 
($fz = function (pt) {
if (pt < 0) {
this.getContextVariableAsVariable ("_breakval").intValue = -pt;
this.pcEnd = this.pc;
return;
}this.pc = Math.abs (this.aatoken[pt][0].intValue);
var tok = this.aatoken[pt][0].tok;
if (tok == 102411 || tok == 102413) {
this.theToken = this.aatoken[this.pc--][0];
var ptNext = Math.abs (this.theToken.intValue);
if (this.theToken.tok != 1150985) this.theToken.intValue = -ptNext;
} else {
while (this.thisContext != null && !J.script.ScriptCompiler.isBreakableContext (this.thisContext.token.tok)) this.popContext (true, false);

this.popContext (true, false);
}}, $fz.isPrivate = true, $fz), "~N");
$_M(c$, "addProcess", 
($fz = function (vProcess, pc, pt) {
if (this.parallelProcessor == null) return;
var statements =  new Array (pt);
for (var i = 0; i < vProcess.size (); i++) statements[i + 1 - pc] = vProcess.get (i);

var context = this.getScriptContext ("addProcess");
context.aatoken = statements;
context.pc = 1 - pc;
context.pcEnd = pt;
this.parallelProcessor.addProcess ("p" + (($t$ = ++ J.script.ScriptEvaluator.iProcess, J.script.ScriptEvaluator.prototype.iProcess = J.script.ScriptEvaluator.iProcess, $t$)), context);
}, $fz.isPrivate = true, $fz), "J.util.JmolList,~N,~N");
$_M(c$, "switchCmd", 
($fz = function (c, tok) {
if (tok == 102410) c.addName ("_var");
var $var = c.contextVariables.get ("_var");
if ($var == null) return 1;
if (tok == 0) {
c.contextVariables.remove ("_var");
return -1;
}if (tok == 102413) return -1;
var v = this.parameterExpressionToken (1);
if (tok == 102411) {
var isOK = J.script.SV.areEqual ($var, v);
if (isOK) c.contextVariables.remove ("_var");
return isOK ? 1 : -1;
}c.contextVariables.put ("_var", v);
return 1;
}, $fz.isPrivate = true, $fz), "J.script.ContextToken,~N");
$_M(c$, "ifCmd", 
($fz = function () {
return this.parameterExpressionBoolean (1, 0);
}, $fz.isPrivate = true, $fz));
$_M(c$, "returnCmd", 
($fz = function (tv) {
var t = this.getContextVariableAsVariable ("_retval");
if (t == null) {
if (!this.chk) this.gotoCmd (null);
return;
}var v = (tv != null || this.slen == 1 ? null : this.parameterExpressionToken (1));
if (this.chk) return;
if (tv == null) tv = (v == null ? J.script.SV.newScriptVariableInt (0) : v);
t.value = tv.value;
t.intValue = tv.intValue;
t.tok = tv.tok;
this.gotoCmd (null);
}, $fz.isPrivate = true, $fz), "J.script.SV");
$_M(c$, "help", 
($fz = function () {
if (this.chk) return;
var what = this.optParameterAsString (1).toLowerCase ();
var pt = 0;
if (what.startsWith ("mouse") && (pt = what.indexOf (" ")) >= 0 && pt == what.lastIndexOf (" ")) {
this.showString (this.viewer.getBindingInfo (what.substring (pt + 1)));
return;
}if (J.script.T.tokAttr (J.script.T.getTokFromName (what), 4096)) what = "?command=" + what;
this.viewer.getHelp (what);
}, $fz.isPrivate = true, $fz));
$_M(c$, "move", 
($fz = function () {
this.checkLength (-11);
var dRot = J.util.V3.new3 (this.floatParameter (1), this.floatParameter (2), this.floatParameter (3));
var dZoom = this.floatParameter (4);
var dTrans = J.util.V3.new3 (this.intParameter (5), this.intParameter (6), this.intParameter (7));
var dSlab = this.floatParameter (8);
var floatSecondsTotal = this.floatParameter (9);
var fps = (this.slen == 11 ? this.intParameter (10) : 30);
if (this.chk) return;
this.refresh ();
if (!this.useThreads ()) floatSecondsTotal = 0;
this.viewer.move (this, dRot, dZoom, dTrans, dSlab, floatSecondsTotal, fps);
if (floatSecondsTotal > 0 && this.isJS) throw  new J.script.ScriptInterruption (this, "move", 1);
}, $fz.isPrivate = true, $fz));
$_M(c$, "moveto", 
($fz = function () {
if (this.slen == 2 && this.tokAt (1) == 1073742162) {
if (!this.chk) this.viewer.stopMotion ();
return;
}var floatSecondsTotal;
if (this.slen == 2 && this.isFloatParameter (1)) {
floatSecondsTotal = this.floatParameter (1);
if (this.chk) return;
if (!this.useThreads ()) floatSecondsTotal = 0;
if (floatSecondsTotal > 0) this.refresh ();
this.viewer.moveTo (this, floatSecondsTotal, null, J.viewer.JC.axisZ, 0, null, 100, 0, 0, 0, null, NaN, NaN, NaN, NaN, NaN, NaN);
if (this.isJS && floatSecondsTotal > 0 && this.viewer.global.waitForMoveTo) throw  new J.script.ScriptInterruption (this, "moveTo", 1);
return;
}var axis = J.util.V3.new3 (NaN, 0, 0);
var center = null;
var i = 1;
floatSecondsTotal = (this.isFloatParameter (i) ? this.floatParameter (i++) : 2.0);
var degrees = 90;
var bsCenter = null;
var isChange = true;
var xTrans = 0;
var yTrans = 0;
var zoom = NaN;
var rotationRadius = NaN;
var zoom0 = this.viewer.getZoomSetting ();
var navCenter = null;
var xNav = NaN;
var yNav = NaN;
var navDepth = NaN;
var cameraDepth = NaN;
var cameraX = NaN;
var cameraY = NaN;
var pymolView = null;
switch (this.getToken (i).tok) {
case 1073742110:
pymolView = this.floatParameterSet (++i, 18, 21);
i = this.iToken + 1;
if (this.chk && this.checkLength (i) > 0) return;
break;
case 135270417:
var q;
var isMolecular = false;
if (this.tokAt (++i) == 1073742029) {
isMolecular = true;
i++;
}if (this.tokAt (i) == 10 || this.tokAt (i) == 1048577) {
isMolecular = true;
center = this.centerParameter (i);
if (!(Clazz.instanceOf (this.expressionResult, J.util.BS))) this.invArg ();
bsCenter = this.expressionResult;
q = (this.chk ?  new J.util.Quaternion () : this.viewer.getAtomQuaternion (bsCenter.nextSetBit (0)));
} else {
q = this.getQuaternionParameter (i);
}i = this.iToken + 1;
if (q == null) this.invArg ();
var aa = q.toAxisAngle4f ();
axis.set (aa.x, aa.y, aa.z);
degrees = (isMolecular ? -1 : 1) * (aa.angle * 180.0 / 3.141592653589793);
break;
case 9:
case 8:
case 1048586:
if (this.isPoint3f (i)) {
axis.setT (this.getPoint3f (i, true));
i = this.iToken + 1;
degrees = this.floatParameter (i++);
} else {
var pt4 = this.getPoint4f (i);
i = this.iToken + 1;
axis.set (pt4.x, pt4.y, pt4.z);
degrees = (pt4.x == 0 && pt4.y == 0 && pt4.z == 0 ? NaN : pt4.w);
}break;
case 1073741954:
axis.set (1, 0, 0);
degrees = 0;
this.checkLength (++i);
break;
case 1073741859:
axis.set (0, 1, 0);
degrees = 180;
this.checkLength (++i);
break;
case 1073741996:
axis.set (0, 1, 0);
this.checkLength (++i);
break;
case 1073742128:
axis.set (0, -1, 0);
this.checkLength (++i);
break;
case 1074790748:
axis.set (1, 0, 0);
this.checkLength (++i);
break;
case 1073741871:
axis.set (-1, 0, 0);
this.checkLength (++i);
break;
default:
axis = J.util.V3.new3 (this.floatParameter (i++), this.floatParameter (i++), this.floatParameter (i++));
degrees = this.floatParameter (i++);
}
if (Float.isNaN (axis.x) || Float.isNaN (axis.y) || Float.isNaN (axis.z)) axis.set (0, 0, 0);
 else if (axis.length () == 0 && degrees == 0) degrees = NaN;
isChange = !this.viewer.isInPosition (axis, degrees);
if (this.isFloatParameter (i)) zoom = this.floatParameter (i++);
if (this.isFloatParameter (i) && !this.isCenterParameter (i)) {
xTrans = this.floatParameter (i++);
yTrans = this.floatParameter (i++);
if (!isChange && Math.abs (xTrans - this.viewer.getTranslationXPercent ()) >= 1) isChange = true;
if (!isChange && Math.abs (yTrans - this.viewer.getTranslationYPercent ()) >= 1) isChange = true;
}if (bsCenter == null && i != this.slen) {
center = this.centerParameter (i);
if (Clazz.instanceOf (this.expressionResult, J.util.BS)) bsCenter = this.expressionResult;
i = this.iToken + 1;
}if (center != null) {
if (!isChange && center.distance (this.viewer.getRotationCenter ()) >= 0.1) isChange = true;
if (this.isFloatParameter (i)) rotationRadius = this.floatParameter (i++);
if (!this.isCenterParameter (i)) {
if ((rotationRadius == 0 || Float.isNaN (rotationRadius)) && (zoom == 0 || Float.isNaN (zoom))) {
var newZoom = Math.abs (this.getZoom (0, i, bsCenter, (zoom == 0 ? 0 : zoom0)));
i = this.iToken + 1;
zoom = newZoom;
} else {
if (!isChange && Math.abs (rotationRadius - this.viewer.getFloat (570425388)) >= 0.1) isChange = true;
}}if (zoom == 0 || Float.isNaN (zoom)) zoom = 100;
if (Float.isNaN (rotationRadius)) rotationRadius = 0;
if (!isChange && Math.abs (zoom - zoom0) >= 1) isChange = true;
if (i != this.slen) {
navCenter = this.centerParameter (i);
i = this.iToken + 1;
if (i != this.slen) {
xNav = this.floatParameter (i++);
yNav = this.floatParameter (i++);
}if (i != this.slen) navDepth = this.floatParameter (i++);
if (i != this.slen) {
cameraDepth = this.floatParameter (i++);
if (!isChange && Math.abs (cameraDepth - this.viewer.getCameraDepth ()) >= 0.01) isChange = true;
}if (i + 1 < this.slen) {
cameraX = this.floatParameter (i++);
cameraY = this.floatParameter (i++);
if (!isChange && Math.abs (cameraX - this.viewer.getCamera ().x) >= 0.01) isChange = true;
if (!isChange && Math.abs (cameraY - this.viewer.getCamera ().y) >= 0.01) isChange = true;
}}}this.checkLength (i);
if (this.chk) return;
if (!isChange) floatSecondsTotal = 0;
if (floatSecondsTotal > 0) this.refresh ();
if (!this.useThreads ()) floatSecondsTotal = 0;
if (cameraDepth == 0) {
cameraDepth = cameraX = cameraY = NaN;
}if (pymolView != null) this.viewer.movePyMOL (this, floatSecondsTotal, pymolView);
 else this.viewer.moveTo (this, floatSecondsTotal, center, axis, degrees, null, zoom, xTrans, yTrans, rotationRadius, navCenter, xNav, yNav, navDepth, cameraDepth, cameraX, cameraY);
if (this.isJS && floatSecondsTotal > 0 && this.viewer.global.waitForMoveTo) throw  new J.script.ScriptInterruption (this, "moveTo", 1);
}, $fz.isPrivate = true, $fz));
$_M(c$, "navigate", 
($fz = function () {
if (this.slen == 1) {
this.setBooleanProperty ("navigationMode", true);
return;
}var rotAxis = J.util.V3.new3 (0, 1, 0);
var list =  new J.util.JmolList ();
var pt;
if (this.slen == 2) {
switch (this.getToken (1).tok) {
case 1048589:
case 1048588:
if (this.chk) return;
this.setObjectMad (31, "axes", 1);
this.setShapeProperty (31, "position", J.util.P3.new3 (50, 50, 3.4028235E38));
this.setBooleanProperty ("navigationMode", true);
this.viewer.setNavOn (this.theTok == 1048589);
return;
case 1073742162:
if (!this.chk) this.viewer.setNavXYZ (0, 0, 0);
return;
case 8:
break;
default:
this.invArg ();
}
}if (!this.chk && !this.viewer.getBoolean (603979887)) this.setBooleanProperty ("navigationMode", true);
for (var i = 1; i < this.slen; i++) {
var timeSec = (this.isFloatParameter (i) ? this.floatParameter (i++) : 2);
if (timeSec < 0) this.invArg ();
if (!this.chk && timeSec > 0) this.refresh ();
switch (this.getToken (i).tok) {
case 8:
case 1048586:
pt = this.getPoint3f (i, true);
this.iToken++;
if (this.iToken != this.slen) this.invArg ();
if (!this.chk) this.viewer.setNavXYZ (pt.x, pt.y, pt.z);
return;
case 554176526:
var depth = this.floatParameter (++i);
if (!this.chk) list.addLast ([Integer.$valueOf (554176526), Float.$valueOf (timeSec), Float.$valueOf (depth)]);
continue;
case 12289:
pt = this.centerParameter (++i);
i = this.iToken;
if (!this.chk) list.addLast ([Integer.$valueOf (135266320), Float.$valueOf (timeSec), pt]);
continue;
case 528432:
switch (this.getToken (++i).tok) {
case 1112541205:
rotAxis.set (1, 0, 0);
i++;
break;
case 1112541206:
rotAxis.set (0, 1, 0);
i++;
break;
case 1112541207:
rotAxis.set (0, 0, 1);
i++;
break;
case 8:
case 1048586:
rotAxis.setT (this.getPoint3f (i, true));
i = this.iToken + 1;
break;
case 1073741824:
this.invArg ();
break;
}
var degrees = this.floatParameter (i);
if (!this.chk) list.addLast ([Integer.$valueOf (528432), Float.$valueOf (timeSec), rotAxis, Float.$valueOf (degrees)]);
continue;
case 4160:
var x = NaN;
var y = NaN;
if (this.isFloatParameter (++i)) {
x = this.floatParameter (i);
y = this.floatParameter (++i);
} else {
switch (this.tokAt (i)) {
case 1112541205:
x = this.floatParameter (++i);
break;
case 1112541206:
y = this.floatParameter (++i);
break;
default:
pt = this.centerParameter (i);
i = this.iToken;
if (!this.chk) list.addLast ([Integer.$valueOf (4160), Float.$valueOf (timeSec), pt]);
continue;
}
}if (!this.chk) list.addLast ([Integer.$valueOf (269484210), Float.$valueOf (timeSec), Float.$valueOf (x), Float.$valueOf (y)]);
continue;
case 269484208:
continue;
case 1113200654:
var pathGuide;
var vp =  new J.util.JmolList ();
var bs = this.atomExpressionAt (++i);
i = this.iToken;
if (this.chk) return;
this.viewer.getPolymerPointsAndVectors (bs, vp);
var n;
if ((n = vp.size ()) > 0) {
pathGuide =  new Array (n);
for (var j = 0; j < n; j++) {
pathGuide[j] = vp.get (j);
}
list.addLast ([Integer.$valueOf (1113200654), Float.$valueOf (timeSec), pathGuide]);
continue;
}break;
case 1073742084:
var path;
var theta = null;
if (this.getToken (i + 1).tok == 1048583) {
i++;
var pathID = this.objectNameParameter (++i);
if (this.chk) return;
this.setShapeProperty (22, "thisID", pathID);
path = this.getShapeProperty (22, "vertices");
this.refresh ();
if (path == null) this.invArg ();
var indexStart = Clazz.floatToInt (this.isFloatParameter (i + 1) ? this.floatParameter (++i) : 0);
var indexEnd = Clazz.floatToInt (this.isFloatParameter (i + 1) ? this.floatParameter (++i) : 2147483647);
list.addLast ([Integer.$valueOf (1073742084), Float.$valueOf (timeSec), path, theta, [indexStart, indexEnd]]);
continue;
}var v =  new J.util.JmolList ();
while (this.isCenterParameter (i + 1)) {
v.addLast (this.centerParameter (++i));
i = this.iToken;
}
if (v.size () > 0) {
path = v.toArray ( new Array (v.size ()));
if (!this.chk) list.addLast ([Integer.$valueOf (1073742084), Float.$valueOf (timeSec), path, theta, [0, 2147483647]]);
continue;
}default:
this.invArg ();
}
}
if (!this.chk) this.viewer.navigateList (this, list);
}, $fz.isPrivate = true, $fz));
$_M(c$, "bondorder", 
($fz = function () {
this.checkLength (-3);
var order = 0;
switch (this.getToken (1).tok) {
case 2:
case 3:
if ((order = J.util.JmolEdge.getBondOrderFromFloat (this.floatParameter (1))) == 131071) this.invArg ();
break;
default:
if ((order = J.script.ScriptEvaluator.getBondOrderFromString (this.parameterAsString (1))) == 131071) this.invArg ();
if (order == 33 && this.tokAt (2) == 3) {
order = J.script.ScriptEvaluator.getPartialBondOrderFromFloatEncodedInt (this.st[2].intValue);
}}
this.setShapeProperty (1, "bondOrder", Integer.$valueOf (order));
}, $fz.isPrivate = true, $fz));
$_M(c$, "console", 
($fz = function () {
switch (this.getToken (1).tok) {
case 1048588:
if (!this.chk) this.viewer.showConsole (false);
break;
case 1048589:
if (!this.chk) this.viewer.showConsole (true);
break;
case 1073741882:
if (!this.chk) this.viewer.clearConsole ();
break;
case 135270421:
this.showString (this.stringParameter (2));
break;
default:
this.invArg ();
}
}, $fz.isPrivate = true, $fz));
$_M(c$, "centerAt", 
($fz = function () {
var relativeTo = null;
switch (this.getToken (1).tok) {
case 1073741826:
relativeTo = "absolute";
break;
case 96:
relativeTo = "average";
break;
case 1679429641:
relativeTo = "boundbox";
break;
default:
this.invArg ();
}
var pt = J.util.P3.new3 (0, 0, 0);
if (this.slen == 5) {
pt.x = this.floatParameter (2);
pt.y = this.floatParameter (3);
pt.z = this.floatParameter (4);
} else if (this.isCenterParameter (2)) {
pt = this.centerParameter (2);
this.checkLast (this.iToken);
} else {
this.checkLength (2);
}if (!this.chk) this.viewer.setCenterAt (relativeTo, pt);
}, $fz.isPrivate = true, $fz));
$_M(c$, "stereo", 
($fz = function () {
var stereoMode = J.constant.EnumStereoMode.DOUBLE;
var degrees = -5;
var degreesSeen = false;
var colors = null;
var colorpt = 0;
for (var i = 1; i < this.slen; ++i) {
if (this.isColorParam (i)) {
if (colorpt > 1) this.error (2);
if (colorpt == 0) colors =  Clazz.newIntArray (2, 0);
if (!degreesSeen) degrees = 3;
colors[colorpt] = this.getArgbParam (i);
if (colorpt++ == 0) colors[1] = ~colors[0];
i = this.iToken;
continue;
}switch (this.getToken (i).tok) {
case 1048589:
this.checkLast (this.iToken = 1);
this.iToken = 1;
break;
case 1048588:
this.checkLast (this.iToken = 1);
stereoMode = J.constant.EnumStereoMode.NONE;
break;
case 2:
case 3:
degrees = this.floatParameter (i);
degreesSeen = true;
break;
case 1073741824:
if (!degreesSeen) degrees = 3;
stereoMode = J.constant.EnumStereoMode.getStereoMode (this.parameterAsString (i));
if (stereoMode != null) break;
default:
this.invArg ();
}
}
if (this.chk) return;
this.viewer.setStereoMode (colors, stereoMode, degrees);
}, $fz.isPrivate = true, $fz));
$_M(c$, "compare", 
($fz = function () {
var isQuaternion = false;
var doRotate = false;
var doTranslate = false;
var doAnimate = false;
var data1 = null;
var data2 = null;
var bsAtoms1 = null;
var bsAtoms2 = null;
var vAtomSets = null;
var vQuatSets = null;
this.iToken = 0;
var nSeconds = (this.isFloatParameter (1) ? this.floatParameter (++this.iToken) : NaN);
var bsFrom = this.atomExpressionAt (++this.iToken);
var coordTo = null;
var bsTo = null;
if (this.isArrayParameter (++this.iToken)) {
coordTo = this.getPointArray (this.iToken, -1);
} else if (this.tokAt (this.iToken) != 1141899265) {
bsTo = this.atomExpressionAt (this.iToken);
}var bsSubset = null;
var isSmiles = false;
var strSmiles = null;
var bs = J.util.BSUtil.copy (bsFrom);
if (bsTo != null) bs.or (bsTo);
var isToSubsetOfFrom = (coordTo == null && bsTo != null && bs.equals (bsFrom));
var isFrames = isToSubsetOfFrom;
for (var i = this.iToken + 1; i < this.slen; ++i) {
switch (this.getToken (i).tok) {
case 4115:
isFrames = true;
break;
case 135267336:
isSmiles = true;
case 135267335:
strSmiles = this.stringParameter (++i);
break;
case 3:
case 2:
nSeconds = Math.abs (this.floatParameter (i));
if (nSeconds > 0) doAnimate = true;
break;
case 269484080:
break;
case 3158024:
bsSubset = this.atomExpressionAt (++i);
i = this.iToken;
break;
case 10:
case 1048577:
if (vQuatSets != null) this.invArg ();
bsAtoms1 = this.atomExpressionAt (this.iToken);
var tok = (isToSubsetOfFrom ? 0 : this.tokAt (this.iToken + 1));
bsAtoms2 = (coordTo == null && this.isArrayParameter (this.iToken + 1) ? null : (tok == 10 || tok == 1048577 ? this.atomExpressionAt (++this.iToken) : J.util.BSUtil.copy (bsAtoms1)));
if (bsSubset != null) {
bsAtoms1.and (bsSubset);
if (bsAtoms2 != null) bsAtoms2.and (bsSubset);
}if (bsAtoms2 == null) coordTo = this.getPointArray (++this.iToken, -1);
 else bsAtoms2.and (bsTo);
if (vAtomSets == null) vAtomSets =  new J.util.JmolList ();
vAtomSets.addLast ([bsAtoms1, bsAtoms2]);
i = this.iToken;
break;
case 7:
if (vAtomSets != null) this.invArg ();
isQuaternion = true;
data1 = J.script.ScriptMathProcessor.getQuaternionArray ((this.theToken).getList (), 1073742001);
this.getToken (++i);
data2 = J.script.ScriptMathProcessor.getQuaternionArray ((this.theToken).getList (), 1073742001);
if (vQuatSets == null) vQuatSets =  new J.util.JmolList ();
vQuatSets.addLast ([data1, data2]);
break;
case 1073742077:
isQuaternion = true;
break;
case 135266320:
case 1141899265:
isQuaternion = false;
break;
case 528432:
doRotate = true;
break;
case 4160:
doTranslate = true;
break;
default:
this.invArg ();
}
}
if (this.chk) return;
if (isFrames) nSeconds = 0;
if (Float.isNaN (nSeconds) || nSeconds < 0) nSeconds = 1;
 else if (!doRotate && !doTranslate) doRotate = doTranslate = true;
doAnimate = (nSeconds != 0);
var isAtoms = (!isQuaternion && strSmiles == null || coordTo != null);
if (vAtomSets == null && vQuatSets == null) {
if (bsSubset == null) {
bsAtoms1 = (isAtoms ? this.viewer.getAtomBitSet ("spine") :  new J.util.BS ());
if (bsAtoms1.nextSetBit (0) < 0) {
bsAtoms1 = bsFrom;
bsAtoms2 = bsTo;
} else {
bsAtoms2 = J.util.BSUtil.copy (bsAtoms1);
bsAtoms1.and (bsFrom);
bsAtoms2.and (bsTo);
}} else {
bsAtoms1 = J.util.BSUtil.copy (bsFrom);
bsAtoms2 = J.util.BSUtil.copy (bsTo);
bsAtoms1.and (bsSubset);
bsAtoms2.and (bsSubset);
bsAtoms1.and (bsFrom);
bsAtoms2.and (bsTo);
}vAtomSets =  new J.util.JmolList ();
vAtomSets.addLast ([bsAtoms1, bsAtoms2]);
}var bsFrames;
if (isFrames) {
var bsModels = this.viewer.getModelBitSet (bsFrom, false);
bsFrames =  new Array (bsModels.cardinality ());
for (var i = 0, iModel = bsModels.nextSetBit (0); iModel >= 0; iModel = bsModels.nextSetBit (iModel + 1), i++) bsFrames[i] = this.viewer.getModelUndeletedAtomsBitSet (iModel);

} else {
bsFrames = [bsFrom];
}for (var iFrame = 0; iFrame < bsFrames.length; iFrame++) {
bsFrom = bsFrames[iFrame];
var retStddev =  Clazz.newFloatArray (2, 0);
var q = null;
var vQ =  new J.util.JmolList ();
var centerAndPoints = null;
var vAtomSets2 = (isFrames ?  new J.util.JmolList () : vAtomSets);
for (var i = 0; i < vAtomSets.size (); ++i) {
var bss = vAtomSets.get (i);
if (isFrames) vAtomSets2.addLast (bss = [J.util.BSUtil.copy (bss[0]), bss[1]]);
bss[0].and (bsFrom);
}
var center = null;
var translation = null;
if (isAtoms) {
if (coordTo != null) {
vAtomSets2.clear ();
vAtomSets2.addLast ([bsAtoms1, coordTo]);
}try {
centerAndPoints = this.viewer.getCenterAndPoints (vAtomSets2, true);
} catch (e) {
if (Clazz.exceptionOf (e, Exception)) {
this.invArg ();
} else {
throw e;
}
}
q = J.util.Measure.calculateQuaternionRotation (centerAndPoints, retStddev, true);
var r0 = (Float.isNaN (retStddev[1]) ? NaN : Math.round (retStddev[0] * 100) / 100);
var r1 = (Float.isNaN (retStddev[1]) ? NaN : Math.round (retStddev[1] * 100) / 100);
this.showString ("RMSD " + r0 + " --> " + r1 + " Angstroms");
} else if (isQuaternion) {
if (vQuatSets == null) {
for (var i = 0; i < vAtomSets2.size (); i++) {
var bss = vAtomSets2.get (i);
data1 = this.viewer.getAtomGroupQuaternions (bss[0], 2147483647);
data2 = this.viewer.getAtomGroupQuaternions (bss[1], 2147483647);
for (var j = 0; j < data1.length && j < data2.length; j++) {
vQ.addLast (data2[j].div (data1[j]));
}
}
} else {
for (var j = 0; j < data1.length && j < data2.length; j++) {
vQ.addLast (data2[j].div (data1[j]));
}
}retStddev[0] = 0;
data1 = vQ.toArray ( new Array (vQ.size ()));
q = J.util.Quaternion.sphereMean (data1, retStddev, 0.0001);
this.showString ("RMSD = " + retStddev[0] + " degrees");
} else {
var m4 =  new J.util.Matrix4f ();
center =  new J.util.P3 ();
var stddev = this.getSmilesCorrelation (bsFrom, bsTo, strSmiles, null, null, m4, null, !isSmiles, false, null, center);
if (Float.isNaN (stddev)) this.invArg ();
if (doTranslate) {
translation =  new J.util.V3 ();
m4.get (translation);
}if (doRotate) {
var m3 =  new J.util.Matrix3f ();
m4.getRotationScale (m3);
q = J.util.Quaternion.newM (m3);
}this.showString ("RMSD = " + stddev + " Angstroms");
}if (centerAndPoints != null) center = centerAndPoints[0][0];
if (center == null) {
centerAndPoints = this.viewer.getCenterAndPoints (vAtomSets2, true);
center = centerAndPoints[0][0];
}var pt1 =  new J.util.P3 ();
var endDegrees = NaN;
if (doTranslate) {
if (translation == null) translation = J.util.V3.newVsub (centerAndPoints[1][0], center);
endDegrees = 0;
}if (doRotate) {
if (q == null) this.evalError ("option not implemented", null);
pt1.setT (center);
pt1.add (q.getNormal ());
endDegrees = q.getTheta ();
}if (Float.isNaN (endDegrees) || Float.isNaN (pt1.x)) continue;
var ptsB = null;
if (doRotate && doTranslate && nSeconds != 0) {
var ptsA = this.viewer.getAtomPointVector (bsFrom);
var m4 = J.script.ScriptMathProcessor.getMatrix4f (q.getMatrix (), translation);
ptsB = J.util.Measure.transformPoints (ptsA, m4, center);
}if (!this.useThreads ()) doAnimate = false;
if (this.viewer.rotateAboutPointsInternal (this, center, pt1, endDegrees / nSeconds, endDegrees, doAnimate, bsFrom, translation, ptsB, null) && doAnimate && this.isJS) throw  new J.script.ScriptInterruption (this, "compare", 1);
}
}, $fz.isPrivate = true, $fz));
$_M(c$, "getSmilesCorrelation", 
function (bsA, bsB, smiles, ptsA, ptsB, m4, vReturn, isSmarts, asMap, mapSet, center) {
var tolerance = (mapSet == null ? 0.1 : 3.4028235E38);
try {
if (ptsA == null) {
ptsA =  new J.util.JmolList ();
ptsB =  new J.util.JmolList ();
}var m =  new J.util.Matrix4f ();
var c =  new J.util.P3 ();
var atoms = this.viewer.modelSet.atoms;
var atomCount = this.viewer.getAtomCount ();
var maps = this.viewer.getSmilesMatcher ().getCorrelationMaps (smiles, atoms, atomCount, bsA, isSmarts, true);
if (maps == null) this.evalError (this.viewer.getSmilesMatcher ().getLastException (), null);
if (maps.length == 0) return NaN;
var mapA = maps[0];
for (var i = 0; i < mapA.length; i++) ptsA.addLast (atoms[mapA[i]]);

maps = this.viewer.getSmilesMatcher ().getCorrelationMaps (smiles, atoms, atomCount, bsB, isSmarts, false);
if (maps == null) this.evalError (this.viewer.getSmilesMatcher ().getLastException (), null);
if (maps.length == 0) return NaN;
if (asMap) {
for (var i = 0; i < maps.length; i++) for (var j = 0; j < maps[i].length; j++) ptsB.addLast (atoms[maps[i][j]]);


return 0;
}var lowestStdDev = 3.4028235E38;
var mapB = null;
for (var i = 0; i < maps.length; i++) {
ptsB.clear ();
for (var j = 0; j < maps[i].length; j++) ptsB.addLast (atoms[maps[i][j]]);

var stddev = J.util.Measure.getTransformMatrix4 (ptsA, ptsB, m, c);
J.util.Logger.info ("getSmilesCorrelation stddev=" + stddev);
if (vReturn != null) {
if (stddev < tolerance) {
var bs =  new J.util.BS ();
for (var j = 0; j < maps[i].length; j++) bs.set (maps[i][j]);

vReturn.addLast (bs);
}}if (stddev < lowestStdDev) {
mapB = maps[i];
if (m4 != null) m4.setM (m);
if (center != null) center.setT (c);
lowestStdDev = stddev;
}}
if (mapSet != null) {
mapSet[0] = mapA;
mapSet[1] = mapB;
}ptsB.clear ();
for (var i = 0; i < mapB.length; i++) ptsB.addLast (atoms[mapB[i]]);

return lowestStdDev;
} catch (e) {
if (Clazz.exceptionOf (e, Exception)) {
this.evalError (e.toString (), null);
return 0;
} else {
throw e;
}
}
}, "J.util.BS,J.util.BS,~S,J.util.JmolList,J.util.JmolList,J.util.Matrix4f,J.util.JmolList,~B,~B,~A,J.util.P3");
$_M(c$, "getSmilesMatches", 
function (pattern, smiles, bsSelected, bsMatch3D, isSmarts, asOneBitset) {
if (this.chk) {
if (asOneBitset) return  new J.util.BS ();
return ["({})"];
}if (pattern.length == 0) {
var isBioSmiles = (!asOneBitset);
var ret = this.viewer.getSmiles (0, 0, bsSelected, isBioSmiles, false, true, true);
if (ret == null) this.evalError (this.viewer.getSmilesMatcher ().getLastException (), null);
return ret;
}var asAtoms = true;
var b;
if (bsMatch3D == null) {
asAtoms = (smiles == null);
if (asAtoms) b = this.viewer.getSmilesMatcher ().getSubstructureSetArray (pattern, this.viewer.modelSet.atoms, this.viewer.getAtomCount (), bsSelected, null, isSmarts, false);
 else b = this.viewer.getSmilesMatcher ().find (pattern, smiles, isSmarts, false);
if (b == null) {
this.showStringPrint (this.viewer.getSmilesMatcher ().getLastException (), false);
if (!asAtoms && !isSmarts) return Integer.$valueOf (-1);
return "?";
}} else {
var vReturn =  new J.util.JmolList ();
var stddev = this.getSmilesCorrelation (bsMatch3D, bsSelected, pattern, null, null, null, vReturn, isSmarts, false, null, null);
if (Float.isNaN (stddev)) {
if (asOneBitset) return  new J.util.BS ();
return [];
}this.showString ("RMSD " + stddev + " Angstroms");
b = vReturn.toArray ( new Array (vReturn.size ()));
}if (asOneBitset) {
var bs =  new J.util.BS ();
for (var j = 0; j < b.length; j++) bs.or (b[j]);

if (asAtoms) return bs;
if (!isSmarts) return Integer.$valueOf (bs.cardinality ());
var iarray =  Clazz.newIntArray (bs.cardinality (), 0);
var pt = 0;
for (var i = bs.nextSetBit (0); i >= 0; i = bs.nextSetBit (i + 1)) iarray[pt++] = i + 1;

return iarray;
}var matches =  new Array (b.length);
for (var j = 0; j < b.length; j++) matches[j] = (asAtoms ? J.util.Escape.eBS (b[j]) : J.util.Escape.eBond (b[j]));

return matches;
}, "~S,~S,J.util.BS,J.util.BS,~B,~B");
$_M(c$, "connect", 
($fz = function (index) {
var distances =  Clazz.newFloatArray (2, 0);
var atomSets =  new Array (2);
atomSets[0] = atomSets[1] = this.viewer.getSelectionSet (false);
var radius = NaN;
this.colorArgb[0] = -2147483648;
var distanceCount = 0;
var bondOrder = 131071;
var bo;
var operation = 1073742026;
var isDelete = false;
var haveType = false;
var haveOperation = false;
var translucentLevel = 3.4028235E38;
var isColorOrRadius = false;
var nAtomSets = 0;
var nDistances = 0;
var bsBonds =  new J.util.BS ();
var isBonds = false;
var expression2 = 0;
var ptColor = 0;
var energy = 0;
var addGroup = false;
if (this.slen == 1) {
if (!this.chk) this.viewer.rebondState (this.isStateScript);
return;
}for (var i = index; i < this.slen; ++i) {
switch (this.getToken (i).tok) {
case 1048589:
case 1048588:
this.checkLength (2);
if (!this.chk) this.viewer.rebondState (this.isStateScript);
return;
case 2:
case 3:
if (nAtomSets > 0) {
if (haveType || isColorOrRadius) this.error (23);
bo = J.util.JmolEdge.getBondOrderFromFloat (this.floatParameter (i));
if (bo == 131071) this.invArg ();
bondOrder = bo;
haveType = true;
break;
}if (++nDistances > 2) this.error (2);
var dist = this.floatParameter (i);
if (this.tokAt (i + 1) == 269484210) {
dist = -dist / 100;
i++;
}distances[distanceCount++] = dist;
break;
case 10:
case 1048577:
if (nAtomSets > 2 || isBonds && nAtomSets > 0) this.error (2);
if (haveType || isColorOrRadius) this.error (23);
atomSets[nAtomSets++] = this.atomExpressionAt (i);
isBonds = this.isBondSet;
if (nAtomSets == 2) {
var pt = this.iToken;
for (var j = i; j < pt; j++) if (this.tokAt (j) == 1073741824 && this.parameterAsString (j).equals ("_1")) {
expression2 = i;
break;
}
this.iToken = pt;
}i = this.iToken;
break;
case 1087373318:
addGroup = true;
break;
case 1766856708:
case 603979967:
case 1073742074:
isColorOrRadius = true;
translucentLevel = this.getColorTrans (i, false);
i = this.iToken;
break;
case 1074790662:
var isAuto = (this.tokAt (2) == 1073741852);
this.checkLength (isAuto ? 3 : 2);
if (!this.chk) this.viewer.setPdbConectBonding (isAuto, this.isStateScript);
return;
case 1073741830:
case 1073741852:
case 1073741904:
case 1073742025:
case 1073742026:
haveOperation = true;
if (++i != this.slen) this.error (23);
operation = this.theTok;
if (this.theTok == 1073741852 && !(bondOrder == 131071 || bondOrder == 2048 || bondOrder == 515)) this.invArg ();
break;
case 1708058:
if (!isColorOrRadius) {
this.colorArgb[0] = 0xFFFFFF;
translucentLevel = 0.5;
radius = this.viewer.getFloat (570425406);
isColorOrRadius = true;
}if (!haveOperation) operation = 1073742026;
haveOperation = true;
case 1073741824:
if (this.isColorParam (i)) {
ptColor = -i;
break;
}case 1076887572:
case 1612189718:
var cmd = this.parameterAsString (i);
if ((bo = J.script.ScriptEvaluator.getBondOrderFromString (cmd)) == 131071) {
this.invArg ();
}if (haveType) this.error (18);
haveType = true;
switch (bo) {
case 33:
switch (this.tokAt (i + 1)) {
case 3:
bo = J.script.ScriptEvaluator.getPartialBondOrderFromFloatEncodedInt (this.st[++i].intValue);
break;
case 2:
bo = this.intParameter (++i);
break;
}
break;
case 2048:
if (this.tokAt (i + 1) == 2) {
bo = (this.intParameter (++i) << 11);
energy = this.floatParameter (++i);
}break;
}
bondOrder = bo;
break;
case 1666189314:
radius = this.floatParameter (++i);
isColorOrRadius = true;
break;
case 1048587:
case 12291:
if (++i != this.slen) this.error (23);
operation = 12291;
isDelete = true;
isColorOrRadius = false;
break;
default:
ptColor = i;
break;
}
if (i > 0) {
if (ptColor == -i || ptColor == i && this.isColorParam (i)) {
isColorOrRadius = true;
this.colorArgb[0] = this.getArgbParam (i);
i = this.iToken;
} else if (ptColor == i) {
this.invArg ();
}}}
if (this.chk) return;
if (distanceCount < 2) {
if (distanceCount == 0) distances[0] = 1.0E8;
distances[1] = distances[0];
distances[0] = 0.1;
}if (isColorOrRadius) {
if (!haveType) bondOrder = 65535;
if (!haveOperation) operation = 1073742025;
}var nNew = 0;
var nModified = 0;
var result;
if (expression2 > 0) {
var bs =  new J.util.BS ();
this.definedAtomSets.put ("_1", bs);
var bs0 = atomSets[0];
for (var atom1 = bs0.nextSetBit (0); atom1 >= 0; atom1 = bs0.nextSetBit (atom1 + 1)) {
bs.set (atom1);
result = this.viewer.makeConnections (distances[0], distances[1], bondOrder, operation, bs, this.atomExpressionAt (expression2), bsBonds, isBonds, false, 0);
nNew += Math.abs (result[0]);
nModified += result[1];
bs.clear (atom1);
}
} else {
result = this.viewer.makeConnections (distances[0], distances[1], bondOrder, operation, atomSets[0], atomSets[1], bsBonds, isBonds, addGroup, energy);
nNew += Math.abs (result[0]);
nModified += result[1];
}if (isDelete) {
if (!(this.tQuiet || this.scriptLevel > this.scriptReportingLevel)) this.scriptStatusOrBuffer (J.i18n.GT._ ("{0} connections deleted", nModified));
return;
}if (isColorOrRadius) {
this.viewer.selectBonds (bsBonds);
if (!Float.isNaN (radius)) this.setShapeSizeBs (1, Math.round (radius * 2000), null);
this.finalizeObject (1, this.colorArgb[0], translucentLevel, 0, false, null, 0, bsBonds);
this.viewer.selectBonds (null);
}if (!(this.tQuiet || this.scriptLevel > this.scriptReportingLevel)) this.scriptStatusOrBuffer (J.i18n.GT._ ("{0} new bonds; {1} modified", [Integer.$valueOf (nNew), Integer.$valueOf (nModified)]));
}, $fz.isPrivate = true, $fz), "~N");
$_M(c$, "getTranslucentLevel", 
($fz = function (i) {
var f = this.floatParameter (i);
return (this.theTok == 2 && f > 0 && f < 9 ? f + 1 : f);
}, $fz.isPrivate = true, $fz), "~N");
$_M(c$, "getProperty", 
($fz = function () {
if (this.chk) return;
var retValue = "";
var property = this.optParameterAsString (1);
var name = property;
if (name.indexOf (".") >= 0) name = name.substring (0, name.indexOf ("."));
if (name.indexOf ("[") >= 0) name = name.substring (0, name.indexOf ("["));
var propertyID = this.viewer.getPropertyNumber (name);
var param = "";
switch (this.tokAt (2)) {
default:
param = this.optParameterAsString (2);
break;
case 1048577:
case 10:
param = this.atomExpressionAt (2);
if (property.equalsIgnoreCase ("bondInfo")) {
switch (this.tokAt (++this.iToken)) {
case 1048577:
case 10:
param = [param, this.atomExpressionAt (this.iToken)];
break;
}
}break;
}
if (property.length > 0 && propertyID < 0) {
property = "";
param = "";
} else if (propertyID >= 0 && this.slen < 3) {
param = this.viewer.getDefaultPropertyParam (propertyID);
if (param.equals ("(visible)")) {
this.viewer.setModelVisibility ();
param = this.viewer.getVisibleSet ();
}} else if (propertyID == this.viewer.getPropertyNumber ("fileContents")) {
var s = param.toString ();
for (var i = 3; i < this.slen; i++) s += this.parameterAsString (i);

param = s;
}retValue = this.viewer.getProperty ("readable", property, param);
this.showString (retValue);
}, $fz.isPrivate = true, $fz));
$_M(c$, "background", 
($fz = function (i) {
this.getToken (i);
var argb;
if (this.theTok == 1073741979) {
var file = this.parameterAsString (this.checkLast (++i));
if (!this.chk && !file.equalsIgnoreCase ("none") && file.length > 0) this.viewer.loadImage (file, null);
return;
}if (this.isColorParam (i) || this.theTok == 1048587) {
argb = this.getArgbParamLast (i, true);
if (this.chk) return;
this.setObjectArgb ("background", argb);
this.viewer.setBackgroundImage (null, null);
return;
}var iShape = this.getShapeType (this.theTok);
this.colorShape (iShape, i + 1, true);
}, $fz.isPrivate = true, $fz), "~N");
$_M(c$, "center", 
($fz = function (i) {
if (this.slen == 1) {
this.viewer.setNewRotationCenter (null);
return;
}var center = this.centerParameter (i);
if (center == null) this.invArg ();
if (!this.chk) this.viewer.setNewRotationCenter (center);
}, $fz.isPrivate = true, $fz), "~N");
$_M(c$, "setObjectProperty", 
($fz = function () {
var id = this.getShapeNameParameter (2);
if (this.chk) return "";
var iTok = this.iToken;
var tokCommand = this.tokAt (0);
return this.setObjectProp (id, tokCommand, iTok);
}, $fz.isPrivate = true, $fz));
Clazz.overrideMethod (c$, "setObjectPropSafe", 
function (id, tokCommand, iTok) {
try {
return this.setObjectProp (id, tokCommand, iTok);
} catch (e) {
if (Clazz.exceptionOf (e, J.script.ScriptException)) {
return null;
} else {
throw e;
}
}
}, "~S,~N,~N");
$_M(c$, "setObjectProp", 
function (id, tokCommand, iTok) {
var data = [id, null];
var s = "";
var isWild = J.util.TextFormat.isWild (id);
for (var iShape = 17; ; ) {
if (iShape != 27 && this.getShapePropertyData (iShape, "checkID", data)) {
this.setShapeProperty (iShape, "thisID", id);
switch (tokCommand) {
case 12291:
this.setShapeProperty (iShape, "delete", null);
break;
case 12294:
case 1610625028:
this.setShapeProperty (iShape, "hidden", tokCommand == 1610625028 ? Boolean.FALSE : Boolean.TRUE);
break;
case 4148:
s += this.getShapeProperty (iShape, "command") + "\n";
break;
case 1766856708:
if (iTok >= 0) this.colorShape (iShape, iTok + 1, false);
break;
}
if (!isWild) break;
}if (iShape == 17) iShape = 31;
if (--iShape < 22) break;
}
return s;
}, "~S,~N,~N");
$_M(c$, "color", 
($fz = function () {
var i = 1;
if (this.isColorParam (1)) {
this.theTok = 1141899265;
} else {
var argb = 0;
i = 2;
var tok = this.getToken (1).tok;
switch (tok) {
case 1048583:
this.setObjectProperty ();
return;
case 1087373315:
case 3145730:
case 1087373316:
case 1073741946:
case 1632634889:
case 1087373318:
case 1114638362:
case 1087373322:
case 1073741992:
case 1095761934:
case 1073742030:
case 1048587:
case 1073742074:
case 1112541196:
case 1095761935:
case 1716520985:
case 1073742116:
case 1113200651:
case 1073742144:
case 1112539150:
case 1641025539:
case 1112539151:
case 1112541199:
case 603979967:
case 1073742186:
case 1649412120:
this.theTok = 1141899265;
i = 1;
break;
case 4:
i = 1;
var strColor = this.stringParameter (i++);
if (this.isArrayParameter (i)) {
strColor = strColor += "=" + J.script.SV.sValue (J.script.SV.getVariableAS (this.stringParameterSet (i))).$replace ('\n', ' ');
i = this.iToken + 1;
}var isTranslucent = (this.tokAt (i) == 603979967);
if (!this.chk) this.viewer.setPropertyColorScheme (strColor, isTranslucent, true);
if (isTranslucent) ++i;
if (this.tokAt (i) == 1073742114 || this.tokAt (i) == 1073741826) {
var min = this.floatParameter (++i);
var max = this.floatParameter (++i);
if (!this.chk) this.viewer.setCurrentColorRange (min, max);
}return;
case 1073742114:
case 1073741826:
var min = this.floatParameter (2);
var max = this.floatParameter (this.checkLast (3));
if (!this.chk) this.viewer.setCurrentColorRange (min, max);
return;
case 1610616835:
argb = this.getArgbParamLast (2, true);
if (!this.chk) this.setObjectArgb ("background", argb);
return;
case 10:
case 1048577:
i = -1;
this.theTok = 1141899265;
break;
case 1073742134:
argb = this.getArgbParamLast (2, false);
if (!this.chk) this.viewer.setRubberbandArgb (argb);
return;
case 536870920:
case 1611141171:
i = 2;
if (this.tokAt (2) == 1073742074) i++;
argb = this.getArgbParamLast (i, true);
if (this.chk) return;
this.sm.loadShape (8);
this.setShapeProperty (8, (tok == 1611141171 ? "argbSelection" : "argbHighlight"), Integer.$valueOf (argb));
return;
case 1611272194:
case 1679429641:
case 1614417948:
case 1073741824:
case 1613758476:
var str = this.parameterAsString (1);
if (this.checkToken (2)) {
switch (this.getToken (2).tok) {
case 1073742116:
argb = 1073742116;
break;
case 1048587:
case 1073741992:
argb = 1073741992;
break;
default:
argb = this.getArgbParam (2);
}
}if (argb == 0) this.error (9);
this.checkLast (this.iToken);
if (str.equalsIgnoreCase ("axes") || J.viewer.StateManager.getObjectIdFromName (str) >= 0) {
this.setObjectArgb (str, argb);
return;
}if (this.changeElementColor (str, argb)) return;
this.invArg ();
break;
case 135180:
case 135402505:
this.setShapeProperty (J.viewer.JC.shapeTokenIndex (tok), "thisID", "+PREVIOUS_MESH+");
break;
}
}this.colorShape (this.getShapeType (this.theTok), i, false);
}, $fz.isPrivate = true, $fz));
$_M(c$, "changeElementColor", 
($fz = function (str, argb) {
for (var i = J.util.Elements.elementNumberMax; --i >= 0; ) {
if (str.equalsIgnoreCase (J.util.Elements.elementNameFromNumber (i))) {
if (!this.chk) this.viewer.setElementArgb (i, argb);
return true;
}}
for (var i = J.util.Elements.altElementMax; --i >= 0; ) {
if (str.equalsIgnoreCase (J.util.Elements.altElementNameFromIndex (i))) {
if (!this.chk) this.viewer.setElementArgb (J.util.Elements.altElementNumberFromIndex (i), argb);
return true;
}}
if (str.charAt (0) != '_') return false;
for (var i = J.util.Elements.elementNumberMax; --i >= 0; ) {
if (str.equalsIgnoreCase ("_" + J.util.Elements.elementSymbolFromNumber (i))) {
if (!this.chk) this.viewer.setElementArgb (i, argb);
return true;
}}
for (var i = J.util.Elements.altElementMax; --i >= 4; ) {
if (str.equalsIgnoreCase ("_" + J.util.Elements.altElementSymbolFromIndex (i))) {
if (!this.chk) this.viewer.setElementArgb (J.util.Elements.altElementNumberFromIndex (i), argb);
return true;
}if (str.equalsIgnoreCase ("_" + J.util.Elements.altIsotopeSymbolFromIndex (i))) {
if (!this.chk) this.viewer.setElementArgb (J.util.Elements.altElementNumberFromIndex (i), argb);
return true;
}}
return false;
}, $fz.isPrivate = true, $fz), "~S,~N");
$_M(c$, "colorShape", 
($fz = function (shapeType, index, isBackground) {
var translucency = null;
var colorvalue = null;
var colorvalue1 = null;
var bs = null;
var prefix = (index == 2 && this.tokAt (1) == 1073741860 ? "ball" : "");
var isColor = false;
var isIsosurface = (shapeType == 24 || shapeType == 25);
var typeMask = 0;
var doClearBondSet = false;
var translucentLevel = 3.4028235E38;
if (index < 0) {
bs = this.atomExpressionAt (-index);
index = this.iToken + 1;
if (this.isBondSet) {
doClearBondSet = true;
shapeType = 1;
}}var tok = this.getToken (index).tok;
if (isBackground) this.getToken (index);
 else if ((isBackground = (tok == 1610616835)) == true) this.getToken (++index);
if (isBackground) prefix = "bg";
 else if (isIsosurface) {
switch (this.theTok) {
case 1073742018:
this.getToken (++index);
prefix = "mesh";
break;
case 1073742094:
var argb = this.getArgbParamOrNone (++index, false);
colorvalue1 = (argb == 0 ? null : Integer.$valueOf (argb));
this.getToken (index = this.iToken + 1);
break;
case 10:
case 1048577:
if (Clazz.instanceOf (this.theToken.value, J.modelset.Bond.BondSet)) {
bs = this.theToken.value;
prefix = "vertex";
} else {
bs = this.atomExpressionAt (index);
prefix = "atom";
}this.getToken (index = this.iToken + 1);
break;
}
}if (!this.chk && shapeType == 27 && !this.getExtension ().dispatch (27, true, this.st)) return;
var isTranslucent = (this.theTok == 603979967);
if (isTranslucent || this.theTok == 1073742074) {
if (translucentLevel == 1.4E-45) this.invArg ();
translucency = this.parameterAsString (index++);
if (isTranslucent && this.isFloatParameter (index)) translucentLevel = this.getTranslucentLevel (index++);
}tok = 0;
if (index < this.slen && this.tokAt (index) != 1048589 && this.tokAt (index) != 1048588) {
isColor = true;
tok = this.getToken (index).tok;
if ((!isIsosurface || this.tokAt (index + 1) != 1074790746) && this.isColorParam (index)) {
var argb = this.getArgbParamOrNone (index, false);
colorvalue = (argb == 0 ? null : Integer.$valueOf (argb));
if (translucency == null && this.tokAt (index = this.iToken + 1) != 0) {
this.getToken (index);
isTranslucent = (this.theTok == 603979967);
if (isTranslucent || this.theTok == 1073742074) {
translucency = this.parameterAsString (index);
if (isTranslucent && this.isFloatParameter (index + 1)) translucentLevel = this.getTranslucentLevel (++index);
} else if (this.isColorParam (index)) {
argb = this.getArgbParamOrNone (index, false);
colorvalue1 = (argb == 0 ? null : Integer.$valueOf (argb));
}}} else if (shapeType == 26) {
this.iToken--;
} else {
var name = this.parameterAsString (index).toLowerCase ();
var isByElement = (name.indexOf ("byelement") == 0);
var isColorIndex = (isByElement || name.indexOf ("byresidue") == 0);
var pal = (isColorIndex || isIsosurface ? J.constant.EnumPalette.PROPERTY : tok == 1113200651 ? J.constant.EnumPalette.CPK : J.constant.EnumPalette.getPalette (name));
if (pal === J.constant.EnumPalette.UNKNOWN || (pal === J.constant.EnumPalette.TYPE || pal === J.constant.EnumPalette.ENERGY) && shapeType != 2) this.invArg ();
var data = null;
var bsSelected = (pal !== J.constant.EnumPalette.PROPERTY && pal !== J.constant.EnumPalette.VARIABLE || !this.viewer.global.rangeSelected ? null : this.viewer.getSelectionSet (false));
if (pal === J.constant.EnumPalette.PROPERTY) {
if (isColorIndex) {
if (!this.chk) {
data = this.getBitsetPropertyFloat (bsSelected, (isByElement ? 1095763976 : 1095761930) | 256, NaN, NaN);
}} else {
if (!isColorIndex && !isIsosurface) index++;
if (name.equals ("property") && J.script.T.tokAttr ((tok = this.getToken (index).tok), 1078984704) && !J.script.T.tokAttr (tok, 1087373312)) {
if (!this.chk) {
data = this.getBitsetPropertyFloat (bsSelected, this.getToken (index++).tok | 256, NaN, NaN);
}}}} else if (pal === J.constant.EnumPalette.VARIABLE) {
index++;
name = this.parameterAsString (index++);
data =  Clazz.newFloatArray (this.viewer.getAtomCount (), 0);
J.util.Parser.parseStringInfestedFloatArray ("" + this.getParameter (name, 4), null, data);
pal = J.constant.EnumPalette.PROPERTY;
}if (pal === J.constant.EnumPalette.PROPERTY) {
var scheme = null;
if (this.tokAt (index) == 4) {
scheme = this.parameterAsString (index++).toLowerCase ();
if (this.isArrayParameter (index)) {
scheme += "=" + J.script.SV.sValue (J.script.SV.getVariableAS (this.stringParameterSet (index))).$replace ('\n', ' ');
index = this.iToken + 1;
}} else if (isIsosurface && this.isColorParam (index)) {
scheme = this.getColorRange (index);
index = this.iToken + 1;
}if (scheme != null && !isIsosurface) {
this.setStringProperty ("propertyColorScheme", (isTranslucent && translucentLevel == 3.4028235E38 ? "translucent " : "") + scheme);
isColorIndex = (scheme.indexOf ("byelement") == 0 || scheme.indexOf ("byresidue") == 0);
}var min = 0;
var max = 3.4028235E38;
if (!isColorIndex && (this.tokAt (index) == 1073741826 || this.tokAt (index) == 1073742114)) {
min = this.floatParameter (index + 1);
max = this.floatParameter (index + 2);
index += 3;
if (min == max && isIsosurface) {
var range = this.getShapeProperty (shapeType, "dataRange");
if (range != null) {
min = range[0];
max = range[1];
}} else if (min == max) {
max = 3.4028235E38;
}}if (!this.chk) {
if (isIsosurface) {
} else if (data == null) {
this.viewer.setCurrentColorRange (name);
} else {
this.viewer.setCurrentColorRangeData (data, bsSelected);
}if (isIsosurface) {
this.checkLength (index);
isColor = false;
var ce = this.viewer.getColorEncoder (scheme);
if (ce == null) return;
ce.isTranslucent = (isTranslucent && translucentLevel == 3.4028235E38);
ce.setRange (min, max, min > max);
if (max == 3.4028235E38) ce.hi = max;
this.setShapeProperty (shapeType, "remapColor", ce);
this.showString (this.getIsosurfaceDataRange (shapeType, ""));
if (translucentLevel == 3.4028235E38) return;
} else if (max != 3.4028235E38) {
this.viewer.setCurrentColorRange (min, max);
}}} else {
index++;
}this.checkLength (index);
colorvalue = pal;
}}if (this.chk || shapeType < 0) return;
switch (shapeType) {
case 4:
typeMask = 32768;
break;
case 2:
typeMask = 30720;
break;
case 3:
typeMask = 256;
break;
case 1:
typeMask = 1023;
break;
default:
typeMask = 0;
}
if (typeMask == 0) {
this.sm.loadShape (shapeType);
if (shapeType == 5) this.setShapeProperty (5, "setDefaults", this.viewer.getNoneSelected ());
} else {
if (bs != null) {
this.viewer.selectBonds (bs);
bs = null;
}shapeType = 1;
this.setShapeProperty (shapeType, "type", Integer.$valueOf (typeMask));
}if (isColor) {
switch (tok) {
case 1112539151:
case 1112539150:
this.viewer.autoCalculate (tok);
break;
case 1112541199:
if (this.viewer.global.rangeSelected) this.viewer.clearBfactorRange ();
break;
case 1087373318:
this.viewer.calcSelectedGroupsCount ();
break;
case 1095761935:
case 1073742030:
this.viewer.calcSelectedMonomersCount ();
break;
case 1095761934:
this.viewer.calcSelectedMoleculesCount ();
break;
}
if (colorvalue1 != null && (isIsosurface || shapeType == 11 || shapeType == 14)) this.setShapeProperty (shapeType, "colorPhase", [colorvalue1, colorvalue]);
 else if (bs == null) this.setShapeProperty (shapeType, prefix + "color", colorvalue);
 else this.setShapePropertyBs (shapeType, prefix + "color", colorvalue, bs);
}if (translucency != null) this.setShapeTranslucency (shapeType, prefix, translucency, translucentLevel, bs);
if (typeMask != 0) this.setShapeProperty (1, "type", Integer.$valueOf (1023));
if (doClearBondSet) this.viewer.selectBonds (null);
if (shapeType == 0) this.viewer.checkInheritedShapes ();
}, $fz.isPrivate = true, $fz), "~N,~N,~B");
$_M(c$, "setShapeTranslucency", 
function (shapeType, prefix, translucency, translucentLevel, bs) {
if (translucentLevel == 3.4028235E38) translucentLevel = this.viewer.getFloat (570425354);
this.setShapeProperty (shapeType, "translucentLevel", Float.$valueOf (translucentLevel));
if (prefix == null) return;
if (bs == null) this.setShapeProperty (shapeType, prefix + "translucency", translucency);
 else if (!this.chk) this.setShapePropertyBs (shapeType, prefix + "translucency", translucency, bs);
}, "~N,~S,~S,~N,J.util.BS");
$_M(c$, "cd", 
($fz = function () {
if (this.chk) return;
var dir = (this.slen == 1 ? null : this.parameterAsString (1));
this.showString (this.viewer.cd (dir));
}, $fz.isPrivate = true, $fz));
$_M(c$, "mapProperty", 
($fz = function () {
var bsFrom;
var bsTo;
var property1;
var property2;
var mapKey;
var tokProp1 = 0;
var tokProp2 = 0;
var tokKey = 0;
while (true) {
if (this.tokAt (1) == 1114638363) {
bsFrom = this.viewer.getSelectionSet (false);
bsTo = this.atomExpressionAt (2);
property1 = property2 = "selected";
} else {
bsFrom = this.atomExpressionAt (1);
if (this.tokAt (++this.iToken) != 1048584 || !J.script.T.tokAttr (tokProp1 = this.tokAt (++this.iToken), 1078984704)) break;
property1 = this.parameterAsString (this.iToken);
bsTo = this.atomExpressionAt (++this.iToken);
if (this.tokAt (++this.iToken) != 1048584 || !J.script.T.tokAttr (tokProp2 = this.tokAt (++this.iToken), 2048)) break;
property2 = this.parameterAsString (this.iToken);
}if (J.script.T.tokAttr (tokKey = this.tokAt (this.iToken + 1), 1078984704)) mapKey = this.parameterAsString (++this.iToken);
 else mapKey = J.script.T.nameOf (tokKey = 1095763969);
this.checkLast (this.iToken);
if (this.chk) return;
var bsOut = null;
this.showString ("mapping " + property1.toUpperCase () + " for " + bsFrom.cardinality () + " atoms to " + property2.toUpperCase () + " for " + bsTo.cardinality () + " atoms using " + mapKey.toUpperCase ());
if (J.script.T.tokAttrOr (tokProp1, 1095761920, 1112539136) && J.script.T.tokAttrOr (tokProp2, 1095761920, 1112539136) && J.script.T.tokAttrOr (tokKey, 1095761920, 1112539136)) {
var data1 = this.getBitsetPropertyFloat (bsFrom, tokProp1 | 224, NaN, NaN);
var data2 = this.getBitsetPropertyFloat (bsFrom, tokKey | 224, NaN, NaN);
var data3 = this.getBitsetPropertyFloat (bsTo, tokKey | 224, NaN, NaN);
var isProperty = (tokProp2 == 1716520985);
var dataOut =  Clazz.newFloatArray (isProperty ? this.viewer.getAtomCount () : data3.length, 0);
bsOut =  new J.util.BS ();
if (data1.length == data2.length) {
var ht =  new java.util.Hashtable ();
for (var i = 0; i < data1.length; i++) {
ht.put (Float.$valueOf (data2[i]), Float.$valueOf (data1[i]));
}
var pt = -1;
var nOut = 0;
for (var i = 0; i < data3.length; i++) {
pt = bsTo.nextSetBit (pt + 1);
var F = ht.get (Float.$valueOf (data3[i]));
if (F == null) continue;
bsOut.set (pt);
dataOut[(isProperty ? pt : nOut)] = F.floatValue ();
nOut++;
}
if (isProperty) this.viewer.setData (property2, [property2, dataOut, bsOut, Integer.$valueOf (0)], this.viewer.getAtomCount (), 0, 0, 2147483647, 0);
 else this.viewer.setAtomProperty (bsOut, tokProp2, 0, 0, null, dataOut, null);
}}if (bsOut == null) {
var format = "{" + mapKey + "=%[" + mapKey + "]}." + property2 + " = %[" + property1 + "]";
var data = this.getBitsetIdent (bsFrom, format, null, false, 2147483647, false);
var sb =  new J.util.SB ();
for (var i = 0; i < data.length; i++) if (data[i].indexOf ("null") < 0) sb.append (data[i]).appendC ('\n');

if (J.util.Logger.debugging) J.util.Logger.debug (sb.toString ());
var bsSubset = J.util.BSUtil.copy (this.viewer.getSelectionSubset ());
this.viewer.setSelectionSubset (bsTo);
try {
this.runScript (sb.toString ());
} catch (e$$) {
if (Clazz.exceptionOf (e$$, Exception)) {
var e = e$$;
{
this.viewer.setSelectionSubset (bsSubset);
this.errorStr (-1, "Error: " + e.toString ());
}
} else if (Clazz.exceptionOf (e$$, Error)) {
var er = e$$;
{
this.viewer.setSelectionSubset (bsSubset);
this.errorStr (-1, "Error: " + er.toString ());
}
} else {
throw e$$;
}
}
this.viewer.setSelectionSubset (bsSubset);
}this.showString ("DONE");
return;
}
this.invArg ();
}, $fz.isPrivate = true, $fz));
$_M(c$, "data", 
($fz = function () {
var dataString = null;
var dataLabel = null;
var isOneValue = false;
var i;
switch (this.iToken = this.slen) {
case 5:
dataString = this.parameterAsString (2);
case 4:
case 2:
dataLabel = this.parameterAsString (1);
if (dataLabel.equalsIgnoreCase ("clear")) {
if (!this.chk) this.viewer.setData (null, null, 0, 0, 0, 0, 0);
return;
}if ((i = dataLabel.indexOf ("@")) >= 0) {
dataString = "" + this.getParameter (dataLabel.substring (i + 1), 4);
dataLabel = dataLabel.substring (0, i).trim ();
} else if (dataString == null && (i = dataLabel.indexOf (" ")) >= 0) {
dataString = dataLabel.substring (i + 1).trim ();
dataLabel = dataLabel.substring (0, i).trim ();
isOneValue = true;
}break;
default:
this.error (2);
}
var dataType = dataLabel + " ";
dataType = dataType.substring (0, dataType.indexOf (" ")).toLowerCase ();
if (dataType.equals ("model") || dataType.equals ("append")) {
this.load ();
return;
}if (this.chk) return;
var isDefault = (dataLabel.toLowerCase ().indexOf ("(default)") >= 0);
this.$data =  new Array (4);
if (dataType.equals ("element_vdw")) {
this.$data[0] = dataType;
this.$data[1] = dataString.$replace (';', '\n');
var n = J.util.Elements.elementNumberMax;
var eArray =  Clazz.newIntArray (n + 1, 0);
for (var ie = 1; ie <= n; ie++) eArray[ie] = ie;

this.$data[2] = eArray;
this.$data[3] = Integer.$valueOf (0);
this.viewer.setData ("element_vdw", this.$data, n, 0, 0, 0, 0);
return;
}if (dataType.equals ("connect_atoms")) {
this.viewer.connect (J.util.Parser.parseFloatArray2d (dataString));
return;
}if (dataType.indexOf ("ligand_") == 0) {
this.viewer.setLigandModel (dataLabel.substring (7).toUpperCase () + "_data", dataString.trim ());
return;
}if (dataType.indexOf ("file_") == 0) {
this.viewer.setLigandModel (dataLabel.substring (5).toUpperCase () + "_file", dataString.trim ());
return;
}if (dataType.indexOf ("data2d_") == 0) {
this.$data[0] = dataLabel;
this.$data[1] = J.util.Parser.parseFloatArray2d (dataString);
this.$data[3] = Integer.$valueOf (2);
this.viewer.setData (dataLabel, this.$data, 0, 0, 0, 0, 0);
return;
}if (dataType.indexOf ("data3d_") == 0) {
this.$data[0] = dataLabel;
this.$data[1] = J.util.Parser.parseFloatArray3d (dataString);
this.$data[3] = Integer.$valueOf (3);
this.viewer.setData (dataLabel, this.$data, 0, 0, 0, 0, 0);
return;
}var tokens = J.util.Parser.getTokens (dataLabel);
if (dataType.indexOf ("property_") == 0 && !(tokens.length == 2 && tokens[1].equals ("set"))) {
var bs = this.viewer.getSelectionSet (false);
this.$data[0] = dataType;
var atomNumberField = (isOneValue ? 0 : (this.viewer.getParameter ("propertyAtomNumberField")).intValue ());
var atomNumberFieldColumnCount = (isOneValue ? 0 : (this.viewer.getParameter ("propertyAtomNumberColumnCount")).intValue ());
var propertyField = (isOneValue ? -2147483648 : (this.viewer.getParameter ("propertyDataField")).intValue ());
var propertyFieldColumnCount = (isOneValue ? 0 : (this.viewer.getParameter ("propertyDataColumnCount")).intValue ());
if (!isOneValue && dataLabel.indexOf (" ") >= 0) {
if (tokens.length == 3) {
dataLabel = tokens[0];
atomNumberField = J.util.Parser.parseInt (tokens[1]);
propertyField = J.util.Parser.parseInt (tokens[2]);
}if (tokens.length == 5) {
dataLabel = tokens[0];
atomNumberField = J.util.Parser.parseInt (tokens[1]);
atomNumberFieldColumnCount = J.util.Parser.parseInt (tokens[2]);
propertyField = J.util.Parser.parseInt (tokens[3]);
propertyFieldColumnCount = J.util.Parser.parseInt (tokens[4]);
}}if (atomNumberField < 0) atomNumberField = 0;
if (propertyField < 0) propertyField = 0;
var atomCount = this.viewer.getAtomCount ();
var atomMap = null;
var bsTemp = J.util.BSUtil.newBitSet (atomCount);
if (atomNumberField > 0) {
atomMap =  Clazz.newIntArray (atomCount + 2, 0);
for (var j = 0; j <= atomCount; j++) atomMap[j] = -1;

for (var j = bs.nextSetBit (0); j >= 0; j = bs.nextSetBit (j + 1)) {
var atomNo = this.viewer.getAtomNumber (j);
if (atomNo > atomCount + 1 || atomNo < 0 || bsTemp.get (atomNo)) continue;
bsTemp.set (atomNo);
atomMap[atomNo] = j;
}
this.$data[2] = atomMap;
} else {
this.$data[2] = J.util.BSUtil.copy (bs);
}this.$data[1] = dataString;
this.$data[3] = Integer.$valueOf (0);
this.viewer.setData (dataType, this.$data, atomCount, atomNumberField, atomNumberFieldColumnCount, propertyField, propertyFieldColumnCount);
return;
}var userType = J.modelset.AtomCollection.getUserSettableType (dataType);
if (userType >= 0) {
this.viewer.setAtomData (userType, dataType, dataString, isDefault);
return;
}this.$data[0] = dataLabel;
this.$data[1] = dataString;
this.$data[3] = Integer.$valueOf (0);
this.viewer.setData (dataType, this.$data, 0, 0, 0, 0, 0);
}, $fz.isPrivate = true, $fz));
$_M(c$, "define", 
($fz = function () {
if (this.slen < 3 || !(Clazz.instanceOf (this.getToken (1).value, String))) this.invArg ();
var setName = (this.getToken (1).value).toLowerCase ();
if (J.util.Parser.parseInt (setName) != -2147483648) this.invArg ();
if (this.chk) return;
var isSite = setName.startsWith ("site_");
var isDynamic = (setName.indexOf ("dynamic_") == 0);
if (isDynamic || isSite) {
var code =  new Array (this.slen);
for (var i = this.slen; --i >= 0; ) code[i] = this.st[i];

this.definedAtomSets.put ("!" + (isSite ? setName : setName.substring (8)), code);
} else {
var bs = this.atomExpressionAt (2);
this.definedAtomSets.put (setName, bs);
if (!this.chk) this.viewer.setUserVariable ("@" + setName, J.script.SV.newVariable (10, bs));
}}, $fz.isPrivate = true, $fz));
$_M(c$, "echo", 
($fz = function (index, id, isImage) {
if (this.chk) return;
var text = this.optParameterAsString (index);
if (this.viewer.getEchoStateActive ()) {
if (isImage) {
this.viewer.loadImage (text, id);
return;
} else if (text.startsWith ("\1")) {
text = text.substring (1);
isImage = true;
}if (text != null) this.setShapeProperty (30, "text", text);
}if (!isImage && this.viewer.getRefreshing ()) this.showString (this.viewer.formatText (text));
}, $fz.isPrivate = true, $fz), "~N,~S,~B");
$_M(c$, "message", 
($fz = function () {
var text = this.parameterAsString (this.checkLast (1));
if (this.chk) return;
var s = this.viewer.formatText (text);
if (this.outputBuffer == null) this.viewer.showMessage (s);
if (!s.startsWith ("_")) this.scriptStatusOrBuffer (s);
}, $fz.isPrivate = true, $fz));
$_M(c$, "log", 
($fz = function () {
if (this.slen == 1) this.error (2);
if (this.chk) return;
var s = this.parameterExpressionString (1, 0);
if (this.tokAt (1) == 1048588) this.setStringProperty ("logFile", "");
 else this.viewer.log (s);
}, $fz.isPrivate = true, $fz));
$_M(c$, "label", 
($fz = function (index) {
if (this.chk) return;
this.sm.loadShape (5);
var strLabel = null;
switch (this.getToken (index).tok) {
case 1048589:
strLabel = this.viewer.getStandardLabelFormat (0);
break;
case 1048588:
break;
case 12294:
case 1610625028:
this.setShapeProperty (5, "display", this.theTok == 1610625028 ? Boolean.TRUE : Boolean.FALSE);
return;
default:
strLabel = this.parameterAsString (index);
}
this.sm.setLabel (strLabel, this.viewer.getSelectionSet (false));
}, $fz.isPrivate = true, $fz), "~N");
$_M(c$, "hover", 
($fz = function () {
if (this.chk) return;
var strLabel = this.parameterAsString (1);
if (strLabel.equalsIgnoreCase ("on")) strLabel = "%U";
 else if (strLabel.equalsIgnoreCase ("off")) strLabel = null;
this.viewer.setHoverLabel (strLabel);
}, $fz.isPrivate = true, $fz));
$_M(c$, "load", 
($fz = function () {
var doLoadFiles = (!this.chk || this.isCmdLine_C_Option);
var isAppend = false;
var isInline = false;
var isSmiles = false;
var isData = false;
var bsModels;
var i = (this.tokAt (0) == 135270407 ? 0 : 1);
var appendNew = this.viewer.getBoolean (603979792);
var filter = null;
var firstLastSteps = null;
var modelCount0 = this.viewer.getModelCount () - (this.viewer.getFileName ().equals ("zapped") ? 1 : 0);
var atomCount0 = this.viewer.getAtomCount ();
var loadScript =  new J.util.SB ().append ("load");
var nFiles = 1;
var htParams =  new java.util.Hashtable ();
if (this.isStateScript) {
htParams.put ("isStateScript", Boolean.TRUE);
if (this.forceNoAddHydrogens) htParams.put ("doNotAddHydrogens", Boolean.TRUE);
}var modelName = null;
var filenames = null;
var tempFileInfo = null;
var errMsg = null;
var sOptions = "";
var tokType = 0;
var tok;
if (this.slen == 1) {
i = 0;
} else {
modelName = this.parameterAsString (i);
if (this.slen == 2 && !this.chk) {
if (modelName.endsWith (".spt") || modelName.endsWith (".png") || modelName.endsWith (".pngj")) {
this.script (0, modelName);
return;
}}switch (tok = this.tokAt (i)) {
case 1073742015:
var m = this.parameterAsString (this.checkLast (2));
if (!this.chk) this.viewer.setMenu (m, true);
return;
case 135270407:
isData = true;
loadScript.append (" /*data*/ data");
var key = this.stringParameter (++i).toLowerCase ();
loadScript.append (" ").append (J.util.Escape.eS (key));
isAppend = key.startsWith ("append");
var strModel = (key.indexOf ("@") >= 0 ? "" + this.getParameter (key.substring (key.indexOf ("@") + 1), 4) : this.parameterAsString (++i));
strModel = J.viewer.Viewer.fixInlineString (strModel, this.viewer.getInlineChar ());
htParams.put ("fileData", strModel);
htParams.put ("isData", Boolean.TRUE);
loadScript.appendC ('\n');
loadScript.append (strModel);
if (key.indexOf ("@") < 0) {
loadScript.append (" end ").append (J.util.Escape.eS (key));
i += 2;
}break;
case 1073741839:
isAppend = true;
loadScript.append (" append");
modelName = this.optParameterAsString (++i);
tok = J.script.T.getTokFromName (modelName);
break;
case 1073741824:
i++;
loadScript.append (" " + modelName);
tokType = (tok == 1073741824 && J.util.Parser.isOneOf (modelName.toLowerCase (), ";xyz;vxyz;vibration;temperature;occupancy;partialcharge;") ? J.script.T.getTokFromName (modelName) : 0);
if (tokType != 0) {
htParams.put ("atomDataOnly", Boolean.TRUE);
htParams.put ("modelNumber", Integer.$valueOf (1));
if (tokType == 4166) tokType = 1146095631;
tempFileInfo = this.viewer.getFileInfo ();
isAppend = true;
}}
switch (tok) {
case 1229984263:
i++;
loadScript.append (" " + modelName);
if (this.tokAt (i) == 7) {
filenames = this.stringParameterSet (i);
i = this.iToken;
if (i + 1 != this.slen) this.invArg ();
if (filenames != null) nFiles = filenames.length;
}break;
case 1073741983:
isInline = true;
i++;
loadScript.append (" " + modelName);
break;
case 135267336:
isSmiles = true;
i++;
break;
case 4156:
htParams.put ("async", Boolean.TRUE);
i++;
break;
case 536870926:
case 1095766028:
i++;
loadScript.append (" " + modelName);
if (tok == 536870926) htParams.put ("isTrajectory", Boolean.TRUE);
if (this.isPoint3f (i)) {
var pt = this.getPoint3f (i, false);
i = this.iToken + 1;
htParams.put ("firstLastStep", [Clazz.floatToInt (pt.x), Clazz.floatToInt (pt.y), Clazz.floatToInt (pt.z)]);
loadScript.append (" " + J.util.Escape.eP (pt));
} else if (this.tokAt (i) == 10) {
bsModels = this.getToken (i++).value;
htParams.put ("bsModels", bsModels);
loadScript.append (" " + J.util.Escape.eBS (bsModels));
} else {
htParams.put ("firstLastStep", [0, -1, 1]);
}break;
case 1073741824:
break;
default:
modelName = "fileset";
}
if (filenames == null && this.getToken (i).tok != 4) this.error (16);
}var filePt = i;
var localName = null;
if (this.tokAt (filePt + 1) == 1073741848) {
localName = this.stringParameter (i = i + 2);
if (this.viewer.getPathForAllFiles () !== "") {
localName = null;
filePt = i;
}}var filename = null;
var appendedData = null;
var appendedKey = null;
if (this.slen == i + 1) {
if (i == 0 || filenames == null && (filename = this.parameterAsString (filePt)).length == 0) filename = this.viewer.getFullPathName ();
if (filename == null && filenames == null) {
this.zap (false);
return;
}if (filenames == null && !isInline) {
if (isSmiles) {
filename = "$" + filename;
} else {
if (filename.indexOf ("[]") >= 0) return;
if (filename.indexOf ("[") == 0) {
filenames = J.util.Escape.unescapeStringArray (filename);
if (filenames != null) {
if (i == 1) loadScript.append (" files");
nFiles = filenames.length;
}}}}if (filenames != null) for (var j = 0; j < nFiles; j++) loadScript.append (" /*file*/").append (J.util.Escape.eS (filenames[j]));

} else if (this.getToken (i + 1).tok == 1073742010 || this.theTok == 2 || this.theTok == 7 || this.theTok == 269484096 || this.theTok == 1073742195 || this.theTok == 1048586 || this.theTok == 8 || this.theTok == 1073742080 || this.theTok == 1095761926 || this.theTok == 1073742163 || this.theTok == 1073742114 || this.theTok == 1073742152 || this.theTok == 1614417948 || this.theTok == 1073742066 || this.theTok == 1073741940 && this.tokAt (i + 3) != 1048582 || this.theTok == 1073741839 || this.theTok == 1073741824 && this.tokAt (i + 3) != 1048582) {
if ((filename = this.parameterAsString (filePt)).length == 0 && (filename = this.viewer.getFullPathName ()) == null) {
this.zap (false);
return;
}if (filePt == i) i++;
if (filename.indexOf ("[]") >= 0) return;
if ((tok = this.tokAt (i)) == 1073742010) {
var manifest = this.stringParameter (++i);
htParams.put ("manifest", manifest);
sOptions += " MANIFEST " + J.util.Escape.eS (manifest);
tok = this.tokAt (++i);
}switch (tok) {
case 2:
var n = this.intParameter (i);
sOptions += " " + n;
if (n < 0) htParams.put ("vibrationNumber", Integer.$valueOf (-n));
 else htParams.put ("modelNumber", Integer.$valueOf (n));
tok = this.tokAt (++i);
break;
case 7:
case 269484096:
case 1073742195:
var data = this.floatParameterSet (i, 1, 2147483647);
i = this.iToken;
var bs =  new J.util.BS ();
for (var j = 0; j < data.length; j++) if (data[j] >= 1 && data[j] == Clazz.floatToInt (data[j])) bs.set (Clazz.floatToInt (data[j]) - 1);

htParams.put ("bsModels", bs);
var iArray =  Clazz.newIntArray (bs.cardinality (), 0);
for (var pt = 0, j = bs.nextSetBit (0); j >= 0; j = bs.nextSetBit (j + 1)) iArray[pt++] = j + 1;

sOptions += " " + J.util.Escape.eAI (iArray);
tok = this.tokAt (i);
break;
}
var lattice = null;
if (tok == 1048586 || tok == 8) {
lattice = this.getPoint3f (i, false);
i = this.iToken + 1;
tok = this.tokAt (i);
}switch (tok) {
case 1073742080:
case 1095761926:
case 1073742163:
case 1073742114:
case 1073742152:
case 1614417948:
if (lattice == null) lattice = J.util.P3.new3 (555, 555, -1);
this.iToken = i - 1;
}
var offset = null;
if (lattice != null) {
htParams.put ("lattice", lattice);
i = this.iToken + 1;
sOptions += " {" + Clazz.floatToInt (lattice.x) + " " + Clazz.floatToInt (lattice.y) + " " + Clazz.floatToInt (lattice.z) + "}";
if (this.tokAt (i) == 1073742080) {
htParams.put ("packed", Boolean.TRUE);
sOptions += " PACKED";
i++;
}if (this.tokAt (i) == 1095761926) {
htParams.put ("centroid", Boolean.TRUE);
sOptions += " CENTROID";
i++;
if (this.tokAt (i) == 1073742080 && !htParams.containsKey ("packed")) {
htParams.put ("packed", Boolean.TRUE);
sOptions += " PACKED";
i++;
}}if (this.tokAt (i) == 1073742163) {
var supercell;
if (this.isPoint3f (++i)) {
var pt = this.getPoint3f (i, false);
if (pt.x != Clazz.floatToInt (pt.x) || pt.y != Clazz.floatToInt (pt.y) || pt.z != Clazz.floatToInt (pt.z) || pt.x < 1 || pt.y < 1 || pt.z < 1) {
this.iToken = i;
this.invArg ();
}supercell = pt;
i = this.iToken + 1;
} else {
supercell = this.stringParameter (i++);
}htParams.put ("supercell", supercell);
}var distance = 0;
if (this.tokAt (i) == 1073742114) {
i++;
distance = this.floatParameter (i++);
sOptions += " range " + distance;
}htParams.put ("symmetryRange", Float.$valueOf (distance));
var spacegroup = null;
var sg;
var iGroup = -2147483648;
if (this.tokAt (i) == 1073742152) {
++i;
spacegroup = J.util.TextFormat.simpleReplace (this.parameterAsString (i++), "''", "\"");
sOptions += " spacegroup " + J.util.Escape.eS (spacegroup);
if (spacegroup.equalsIgnoreCase ("ignoreOperators")) {
iGroup = -999;
} else {
if (spacegroup.length == 0) {
sg = this.viewer.getCurrentUnitCell ();
if (sg != null) spacegroup = sg.getSpaceGroupName ();
} else {
if (spacegroup.indexOf (",") >= 0) if ((lattice.x < 9 && lattice.y < 9 && lattice.z == 0)) spacegroup += "#doNormalize=0";
}htParams.put ("spaceGroupName", spacegroup);
iGroup = -2;
}}var fparams = null;
if (this.tokAt (i) == 1614417948) {
++i;
if (this.optParameterAsString (i).length == 0) {
sg = this.viewer.getCurrentUnitCell ();
if (sg != null) {
fparams = sg.getUnitCellAsArray (true);
offset = sg.getCartesianOffset ();
}} else {
fparams = this.floatParameterSet (i, 6, 9);
}if (fparams == null || fparams.length != 6 && fparams.length != 9) this.invArg ();
sOptions += " unitcell {";
for (var j = 0; j < fparams.length; j++) sOptions += (j == 0 ? "" : " ") + fparams[j];

sOptions += "}";
htParams.put ("unitcell", fparams);
if (iGroup == -2147483648) iGroup = -1;
i = this.iToken + 1;
}if (iGroup != -2147483648) htParams.put ("spaceGroupIndex", Integer.$valueOf (iGroup));
}if (offset != null) this.coordinatesAreFractional = false;
 else if (this.tokAt (i) == 1073742066) offset = this.getPoint3f (++i, true);
if (offset != null) {
if (this.coordinatesAreFractional) {
offset.setT (this.fractionalPoint);
htParams.put ("unitCellOffsetFractional", (this.coordinatesAreFractional ? Boolean.TRUE : Boolean.FALSE));
sOptions += " offset {" + offset.x + " " + offset.y + " " + offset.z + "/1}";
} else {
sOptions += " offset " + J.util.Escape.eP (offset);
}htParams.put ("unitCellOffset", offset);
i = this.iToken + 1;
}if (this.tokAt (i) == 1073741839) {
if (this.tokAt (++i) == 135270407) {
i += 2;
appendedData = this.getToken (i++).value;
appendedKey = this.stringParameter (++i);
++i;
} else {
appendedKey = this.stringParameter (i++);
appendedData = this.stringParameter (i++);
}htParams.put (appendedKey, appendedData);
}if (this.tokAt (i) == 1073741940) filter = this.stringParameter (++i);
} else {
if (i == 1) {
i++;
loadScript.append (" " + modelName);
}var pt = null;
var bs = null;
var fNames =  new J.util.JmolList ();
while (i < this.slen) {
switch (this.tokAt (i)) {
case 1073741940:
filter = this.stringParameter (++i);
++i;
continue;
case 1048582:
htParams.remove ("isTrajectory");
if (firstLastSteps == null) {
firstLastSteps =  new J.util.JmolList ();
pt = J.util.P3.new3 (0, -1, 1);
}if (this.isPoint3f (++i)) {
pt = this.getPoint3f (i, false);
i = this.iToken + 1;
} else if (this.tokAt (i) == 10) {
bs = this.getToken (i).value;
pt = null;
i = this.iToken + 1;
}break;
case 1073741824:
this.invArg ();
}
fNames.addLast (filename = this.parameterAsString (i++));
if (pt != null) {
firstLastSteps.addLast ([Clazz.floatToInt (pt.x), Clazz.floatToInt (pt.y), Clazz.floatToInt (pt.z)]);
loadScript.append (" COORD " + J.util.Escape.eP (pt));
} else if (bs != null) {
firstLastSteps.addLast (bs);
loadScript.append (" COORD " + J.util.Escape.eBS (bs));
}loadScript.append (" /*file*/$FILENAME" + fNames.size () + "$");
}
if (firstLastSteps != null) {
htParams.put ("firstLastSteps", firstLastSteps);
}nFiles = fNames.size ();
filenames = fNames.toArray ( new Array (nFiles));
}if (!doLoadFiles) return;
if (filenames != null) filename = "fileSet";
if (appendedData != null) {
sOptions += " APPEND data \"" + appendedKey + "\"\n" + appendedData + (appendedData.endsWith ("\n") ? "" : "\n") + "end \"" + appendedKey + "\"";
}if (filter == null) filter = this.viewer.getDefaultLoadFilter ();
if (filter.length > 0) {
if (filter.toUpperCase ().indexOf ("DOCACHE") >= 0) {
if (!this.isStateScript && !isAppend) this.viewer.cacheClear ();
}htParams.put ("filter", filter);
if (filter.equalsIgnoreCase ("2d")) filter = "2D-noMin";
sOptions += " FILTER " + J.util.Escape.eS (filter);
}var isVariable = false;
if (filenames == null) {
if (isInline) {
htParams.put ("fileData", filename);
} else if (filename.startsWith ("@") && filename.length > 1) {
isVariable = true;
var s = this.getStringParameter (filename.substring (1), false);
htParams.put ("fileData", s);
loadScript =  new J.util.SB ().append ("{\n    var ").append (filename.substring (1)).append (" = ").append (J.util.Escape.eS (s)).append (";\n    ").appendSB (loadScript);
} else if (filename.startsWith ("?") && this.viewer.isJS) {
localName = null;
filename = this.loadFileAsync ("LOAD" + (isAppend ? "_APPEND_" : "_"), filename, i, !isAppend);
}}var os = null;
if (localName != null) {
if (localName.equals (".")) localName = this.viewer.getFilePath (filename, true);
if (localName.length == 0 || this.viewer.getFilePath (localName, false).equalsIgnoreCase (this.viewer.getFilePath (filename, false))) this.invArg ();
var fullPath = [localName];
os = this.viewer.getOutputStream (localName, fullPath);
if (os == null) J.util.Logger.error ("Could not create output stream for " + fullPath[0]);
 else htParams.put ("OutputStream", os);
}if (filenames == null && tokType == 0) {
loadScript.append (" ");
if (isVariable || isInline) {
loadScript.append (J.util.Escape.eS (filename));
} else if (!isData) {
if (!filename.equals ("string") && !filename.equals ("string[]")) loadScript.append ("/*file*/");
if (localName != null) localName = this.viewer.getFilePath (localName, false);
loadScript.append ((localName != null ? J.util.Escape.eS (localName) : "$FILENAME$"));
}if (sOptions.length > 0) loadScript.append (" /*options*/ ").append (sOptions);
if (isVariable) loadScript.append ("\n  }");
htParams.put ("loadScript", loadScript);
}this.setCursorWait (true);
var timeMsg = this.viewer.getBoolean (603979934);
if (timeMsg) J.util.Logger.startTimer ("load");
errMsg = this.viewer.loadModelFromFile (null, filename, filenames, null, isAppend, htParams, loadScript, tokType);
if (os != null) try {
this.viewer.setFileInfo ([localName, localName, localName]);
J.util.Logger.info (J.i18n.GT._ ("file {0} created", localName));
this.showString (this.viewer.getFilePath (localName, false) + " created");
os.close ();
} catch (e) {
if (Clazz.exceptionOf (e, java.io.IOException)) {
J.util.Logger.error ("error closing file " + e.toString ());
} else {
throw e;
}
}
if (tokType > 0) {
this.viewer.setFileInfo (tempFileInfo);
if (errMsg != null && !this.isCmdLine_c_or_C_Option) this.evalError (errMsg, null);
return;
}if (errMsg != null && !this.isCmdLine_c_or_C_Option) {
if (errMsg.indexOf ("NOTE: file recognized as a script file: ") == 0) {
filename = errMsg.substring ("NOTE: file recognized as a script file: ".length).trim ();
this.script (0, filename);
return;
}this.evalError (errMsg, null);
}if (isAppend && (appendNew || nFiles > 1)) {
this.viewer.setAnimationRange (-1, -1);
this.viewer.setCurrentModelIndex (modelCount0);
}if (this.scriptLevel == 0 && !isAppend && nFiles < 2) this.showString (this.viewer.getModelSetAuxiliaryInfoValue ("modelLoadNote"));
if (this.logMessages) this.scriptStatusOrBuffer ("Successfully loaded:" + (filenames == null ? htParams.get ("fullPathName") : modelName));
var info = this.viewer.getModelSetAuxiliaryInfo ();
if (info != null && info.containsKey ("centroidMinMax") && this.viewer.getAtomCount () > 0) {
var bs = J.util.BSUtil.newBitSet2 (isAppend ? atomCount0 : 0, this.viewer.getAtomCount ());
this.viewer.setCentroid (bs, info.get ("centroidMinMax"));
}var script = this.viewer.getDefaultLoadScript ();
var msg = "";
if (script.length > 0) msg += "\nUsing defaultLoadScript: " + script;
if (info != null && this.viewer.allowEmbeddedScripts ()) {
var embeddedScript = info.remove ("jmolscript");
if (embeddedScript != null && embeddedScript.length > 0) {
msg += "\nAdding embedded #jmolscript: " + embeddedScript;
script += ";" + embeddedScript;
this.setStringProperty ("_loadScript", script);
script = "allowEmbeddedScripts = false;try{" + script + "} allowEmbeddedScripts = true;";
}} else {
this.setStringProperty ("_loadScript", "");
}this.logLoadInfo (msg);
var siteScript = (info == null ? null : info.remove ("sitescript"));
if (siteScript != null) script = siteScript + ";" + script;
if (script.length > 0 && !this.isCmdLine_c_or_C_Option) this.runScript (script);
if (timeMsg) this.showString (J.util.Logger.getTimerMsg ("load", 0));
}, $fz.isPrivate = true, $fz));
$_M(c$, "loadFileAsync", 
function (prefix, filename, i, doClear) {
prefix = "cache://local" + prefix;
var key = this.pc + "_" + i;
var cacheName;
if (this.thisContext == null || this.thisContext.htFileCache == null) {
this.pushContext (null, "loadFileAsync");
this.thisContext.htFileCache =  new java.util.Hashtable ();
}cacheName = this.thisContext.htFileCache.get (key);
if (cacheName != null && cacheName.length > 0) {
this.fileLoadThread = null;
this.popContext (false, false);
this.viewer.queueOnHold = false;
if ("#CANCELED#".equals (this.viewer.cacheGet (cacheName))) this.evalError ("#CANCELED#", null);
return cacheName;
}this.thisContext.htFileCache.put (key, cacheName = prefix + System.currentTimeMillis ());
if (this.fileLoadThread != null) this.evalError ("#CANCELED#", null);
if (doClear) this.viewer.cacheFileByName (prefix + "*", false);
this.fileLoadThread =  new J.script.FileLoadThread (this, this.viewer, filename, key, cacheName);
this.fileLoadThread.run ();
throw  new J.script.ScriptInterruption (this, "load", 1);
}, "~S,~S,~N,~B");
$_M(c$, "logLoadInfo", 
($fz = function (msg) {
if (msg.length > 0) J.util.Logger.info (msg);
var sb =  new J.util.SB ();
var modelCount = this.viewer.getModelCount ();
if (modelCount > 1) sb.append ((this.viewer.isMovie () ? this.viewer.getFrameCount () + " frames" : modelCount + " models") + "\n");
for (var i = 0; i < modelCount; i++) {
var moData = this.viewer.getModelAuxiliaryInfoValue (i, "moData");
if (moData == null) continue;
sb.appendI ((moData.get ("mos")).size ()).append (" molecular orbitals in model ").append (this.viewer.getModelNumberDotted (i)).append ("\n");
}
if (sb.length () > 0) this.showString (sb.toString ());
}, $fz.isPrivate = true, $fz), "~S");
$_M(c$, "getFullPathName", 
function () {
var filename = (!this.chk || this.isCmdLine_C_Option ? this.viewer.getFullPathName () : "test.xyz");
if (filename == null) this.invArg ();
return filename;
});
$_M(c$, "measure", 
($fz = function () {
var id = null;
var pt = 1;
var colix = 0;
var offset = null;
switch (this.tokAt (1)) {
case 135267335:
var smarts = this.stringParameter (this.slen == 3 ? 2 : 4);
if (this.chk) return;
var atoms = this.viewer.modelSet.atoms;
var atomCount = this.viewer.getAtomCount ();
var maps = this.viewer.getSmilesMatcher ().getCorrelationMaps (smarts, atoms, atomCount, this.viewer.getSelectionSet (false), true, false);
if (maps == null) return;
this.setShapeProperty (6, "maps", maps);
return;
}
switch (this.slen) {
case 2:
switch (this.getToken (pt).tok) {
case 0:
case 1048589:
this.setShapeProperty (6, "hideAll", Boolean.FALSE);
return;
case 1048588:
this.setShapeProperty (6, "hideAll", Boolean.TRUE);
return;
case 1073742001:
if (!this.chk) this.showStringPrint (this.viewer.getMeasurementInfoAsString (), false);
return;
case 12291:
if (!this.chk) this.viewer.clearAllMeasurements ();
return;
case 4:
this.setShapeProperty (6, "setFormats", this.stringParameter (1));
return;
}
this.errorStr (24, "ON, OFF, DELETE");
break;
case 3:
switch (this.getToken (1).tok) {
case 12291:
if (this.getToken (2).tok == 1048579) {
if (!this.chk) this.viewer.clearAllMeasurements ();
} else {
var i = this.intParameter (2) - 1;
if (!this.chk) this.viewer.deleteMeasurement (i);
}return;
}
}
var nAtoms = 0;
var expressionCount = 0;
var modelIndex = -1;
var atomIndex = -1;
var ptFloat = -1;
var countPlusIndexes =  Clazz.newIntArray (5, 0);
var rangeMinMax = [3.4028235E38, 3.4028235E38];
var isAll = false;
var isAllConnected = false;
var isNotConnected = false;
var isRange = true;
var rd = null;
var intramolecular = null;
var tokAction = 269484114;
var strFormat = null;
var font = null;
var points =  new J.util.JmolList ();
var bs =  new J.util.BS ();
var value = null;
var tickInfo = null;
var nBitSets = 0;
var mad = 0;
for (var i = 1; i < this.slen; ++i) {
switch (this.getToken (i).tok) {
case 1074790550:
if (i != 1) this.invArg ();
id = this.optParameterAsString (++i);
continue;
case 1073741824:
this.errorStr (24, "ALL, ALLCONNECTED, DELETE");
break;
default:
this.error (15);
break;
case 269484144:
if (this.tokAt (i + 1) != 135266310) this.invArg ();
i++;
isNotConnected = true;
break;
case 135266310:
case 1073741834:
case 1048579:
isAllConnected = (this.theTok == 1073741834);
atomIndex = -1;
isAll = true;
if (isAllConnected && isNotConnected) this.invArg ();
break;
case 1766856708:
colix = J.util.C.getColix (this.getArgbParam (++i));
i = this.iToken;
break;
case 1073742066:
if (this.isPoint3f (++i)) {
var p = this.getPoint3f (i, false);
offset = [1, p.x, p.y, p.z, 0, 0, 0];
} else {
offset = this.floatParameterSet (i, 7, 7);
}i = this.iToken;
break;
case 1666189314:
case 1073741916:
mad = Clazz.floatToInt ((this.theTok == 1666189314 ? 2000 : 1000) * this.floatParameter (++i));
if (id != null && mad <= 0) mad = -1;
break;
case 3:
if (rd != null) this.invArg ();
isAll = true;
isRange = true;
ptFloat = (ptFloat + 1) % 2;
rangeMinMax[ptFloat] = this.floatParameter (i);
break;
case 12291:
if (tokAction != 269484114) this.invArg ();
tokAction = 12291;
break;
case 4114:
var fontsize = this.floatParameter (++i);
var fontface = this.parameterAsString (++i);
var fontstyle = this.parameterAsString (++i);
if (!this.chk) font = this.viewer.getFont3D (fontface, fontstyle, fontsize);
break;
case 2:
var iParam = this.intParameter (i);
if (isAll) {
isRange = true;
ptFloat = (ptFloat + 1) % 2;
rangeMinMax[ptFloat] = iParam;
} else {
atomIndex = this.viewer.getAtomIndexFromAtomNumber (iParam);
if (!this.chk && atomIndex < 0) return;
if (value != null) this.invArg ();
if ((countPlusIndexes[0] = ++nAtoms) > 4) this.error (2);
countPlusIndexes[nAtoms] = atomIndex;
}break;
case 1095761933:
modelIndex = this.intParameter (++i);
break;
case 1048588:
if (tokAction != 269484114) this.invArg ();
tokAction = 1048588;
break;
case 1048589:
if (tokAction != 269484114) this.invArg ();
tokAction = 1048589;
break;
case 1073742114:
isAll = true;
isRange = true;
atomIndex = -1;
break;
case 1073741989:
case 1073741990:
intramolecular = Boolean.$valueOf (this.theTok == 1073741989);
isAll = true;
isNotConnected = (this.theTok == 1073741990);
break;
case 1649412120:
if (ptFloat >= 0) this.invArg ();
rd = this.encodeRadiusParameter (i, false, true);
rd.values = rangeMinMax;
i = this.iToken;
isNotConnected = true;
isAll = true;
intramolecular = Boolean.$valueOf (false);
if (nBitSets == 1) {
nBitSets++;
nAtoms++;
var bs2 = J.util.BSUtil.copy (bs);
J.util.BSUtil.invertInPlace (bs2, this.viewer.getAtomCount ());
bs2.and (this.viewer.getAtomsWithinRadius (5, bs, false, null));
points.addLast (bs2);
}break;
case 10:
case 1048577:
case 1048586:
case 8:
case 1048583:
if (this.theTok == 10 || this.theTok == 1048577) nBitSets++;
if (atomIndex >= 0) this.invArg ();
this.expressionResult = Boolean.FALSE;
value = this.centerParameter (i);
if (Clazz.instanceOf (this.expressionResult, J.util.BS)) {
value = bs = this.expressionResult;
if (!this.chk && bs.length () == 0) return;
}if (Clazz.instanceOf (value, J.util.P3)) {
var v =  new J.util.Point3fi ();
v.setT (value);
v.modelIndex = modelIndex;
value = v;
}if ((nAtoms = ++expressionCount) > 4) this.error (2);
i = this.iToken;
points.addLast (value);
break;
case 4:
strFormat = this.stringParameter (i);
break;
case 1073742164:
tickInfo = this.checkTicks (i, false, true, true);
i = this.iToken;
tokAction = 1060866;
break;
}
}
if (rd != null && (ptFloat >= 0 || nAtoms != 2) || nAtoms < 2 && id == null && (tickInfo == null || nAtoms == 1)) this.error (2);
if (strFormat != null && strFormat.indexOf (nAtoms + ":") != 0) strFormat = nAtoms + ":" + strFormat;
if (isRange) {
if (rangeMinMax[1] < rangeMinMax[0]) {
rangeMinMax[1] = rangeMinMax[0];
rangeMinMax[0] = (rangeMinMax[1] == 3.4028235E38 ? 3.4028235E38 : -200);
}}if (this.chk) return;
if (value != null || tickInfo != null) {
if (rd == null) rd =  new J.atomdata.RadiusData (rangeMinMax, 0, null, null);
if (value == null) tickInfo.id = "default";
if (value != null && strFormat != null && tokAction == 269484114) tokAction = 1060866;
var text = null;
if (font != null) text = J.modelset.Text.newLabel (this.viewer.getGraphicsData (), font, "", colix, 0, 0, 0, null);
if (text != null) text.pymolOffset = offset;
this.setShapeProperty (6, "measure", ( new J.modelset.MeasurementData (id, this.viewer, points)).set (tokAction, null, rd, strFormat, null, tickInfo, isAllConnected, isNotConnected, intramolecular, isAll, mad, colix, text));
return;
}var propertyValue = (id == null ? countPlusIndexes : id);
switch (tokAction) {
case 12291:
this.setShapeProperty (6, "delete", propertyValue);
break;
case 1048589:
this.setShapeProperty (6, "show", propertyValue);
break;
case 1048588:
this.setShapeProperty (6, "hide", propertyValue);
break;
default:
this.setShapeProperty (6, (strFormat == null ? "toggle" : "toggleOn"), propertyValue);
if (strFormat != null) this.setShapeProperty (6, "setFormats", strFormat);
}
}, $fz.isPrivate = true, $fz));
$_M(c$, "pause", 
($fz = function () {
if (this.chk || this.isJS && !this.allowJSThreads) return false;
var msg = this.optParameterAsString (1);
if (!this.viewer.getBooleanProperty ("_useCommandThread")) {
}if (this.viewer.autoExit || !this.viewer.haveDisplay && !this.viewer.isWebGL) return false;
if (this.scriptLevel == 0 && this.pc == this.aatoken.length - 1) {
this.viewer.scriptStatus ("nothing to pause: " + msg);
return false;
}msg = (msg.length == 0 ? ": RESUME to continue." : ": " + this.viewer.formatText (msg));
this.pauseExecution (true);
this.viewer.scriptStatusMsg ("script execution paused" + msg, "script paused for RESUME");
return true;
}, $fz.isPrivate = true, $fz));
$_M(c$, "print", 
($fz = function () {
if (this.slen == 1) this.error (2);
this.showStringPrint (this.parameterExpressionString (1, 0), true);
}, $fz.isPrivate = true, $fz));
$_M(c$, "prompt", 
($fz = function () {
var msg = null;
if (this.slen == 1) {
if (!this.chk) msg = J.script.ScriptEvaluator.getContextTrace (this.viewer, this.getScriptContext ("prompt"), null, true).toString ();
} else {
msg = this.parameterExpressionString (1, 0);
}if (!this.chk) this.viewer.prompt (msg, "OK", null, true);
}, $fz.isPrivate = true, $fz));
$_M(c$, "refresh", 
($fz = function () {
if (this.chk) return;
this.viewer.setTainted (true);
this.viewer.requestRepaintAndWait ("refresh cmd");
}, $fz.isPrivate = true, $fz));
$_M(c$, "reset", 
($fz = function () {
if (this.slen == 3 && this.tokAt (1) == 135368713) {
if (!this.chk) this.viewer.removeFunction (this.stringParameter (2));
return;
}this.checkLength (-2);
if (this.chk) return;
if (this.slen == 1) {
this.viewer.reset (false);
return;
}switch (this.tokAt (1)) {
case 135270422:
this.viewer.cacheClear ();
return;
case 1073741935:
this.viewer.resetError ();
return;
case 1087373323:
this.viewer.resetShapes (true);
return;
case 135368713:
this.viewer.clearFunctions ();
return;
case 1641025539:
var bsAllAtoms =  new J.util.BS ();
this.runScript (this.viewer.getDefaultStructure (null, bsAllAtoms));
this.viewer.resetBioshapes (bsAllAtoms);
return;
case 1649412120:
this.viewer.setData ("element_vdw", [null, ""], 0, 0, 0, 0, 0);
return;
case 1076887572:
this.viewer.resetAromatic ();
return;
case 1611141175:
this.viewer.reset (true);
return;
}
var $var = this.parameterAsString (1);
if ($var.charAt (0) == '_') this.invArg ();
this.viewer.unsetProperty ($var);
}, $fz.isPrivate = true, $fz));
$_M(c$, "restrict", 
($fz = function () {
var isBond = (this.tokAt (1) == 1678770178);
this.select (isBond ? 2 : 1);
this.restrictSelected (isBond, true);
}, $fz.isPrivate = true, $fz));
$_M(c$, "restrictSelected", 
($fz = function (isBond, doInvert) {
if (!this.chk) this.sm.restrictSelected (isBond, doInvert);
}, $fz.isPrivate = true, $fz), "~B,~B");
$_M(c$, "rotate", 
($fz = function (isSpin, isSelected) {
if (this.slen == 2) switch (this.getToken (1).tok) {
case 1048589:
if (!this.chk) this.viewer.setSpinOn (true);
return;
case 1048588:
if (!this.chk) this.viewer.setSpinOn (false);
return;
}
var bsAtoms = null;
var degreesPerSecond = 1.4E-45;
var nPoints = 0;
var endDegrees = 3.4028235E38;
var isMolecular = false;
var haveRotation = false;
var dihedralList = null;
var ptsA = null;
var points =  new Array (2);
var rotAxis = J.util.V3.new3 (0, 1, 0);
var translation = null;
var m4 = null;
var m3 = null;
var direction = 1;
var tok;
var q = null;
var helicalPath = false;
var ptsB = null;
var bsCompare = null;
var invPoint = null;
var invPlane = null;
var axesOrientationRasmol = this.viewer.getBoolean (603979806);
for (var i = 1; i < this.slen; ++i) {
switch (tok = this.getToken (i).tok) {
case 10:
case 1048577:
case 1048586:
case 8:
case 1048583:
if (tok == 10 || tok == 1048577) {
if (translation != null || q != null || nPoints == 2) {
bsAtoms = this.atomExpressionAt (i);
ptsB = null;
isSelected = true;
break;
}}haveRotation = true;
if (nPoints == 2) nPoints = 0;
var pt1 = this.centerParameterForModel (i, this.viewer.getCurrentModelIndex ());
if (!this.chk && tok == 1048583 && this.tokAt (i + 2) != 269484096) {
isMolecular = true;
rotAxis = this.getDrawObjectAxis (this.objectNameParameter (++i), this.viewer.getCurrentModelIndex ());
}points[nPoints++] = pt1;
break;
case 1611141175:
isSpin = true;
continue;
case 1073741988:
case 1073742029:
isMolecular = true;
continue;
case 1114638363:
isSelected = true;
break;
case 269484080:
continue;
case 2:
case 3:
if (isSpin) {
if (degreesPerSecond == 1.4E-45) {
degreesPerSecond = this.floatParameter (i);
continue;
} else if (endDegrees == 3.4028235E38) {
endDegrees = degreesPerSecond;
degreesPerSecond = this.floatParameter (i);
continue;
}} else {
if (endDegrees == 3.4028235E38) {
endDegrees = this.floatParameter (i);
continue;
} else if (degreesPerSecond == 1.4E-45) {
degreesPerSecond = this.floatParameter (i);
isSpin = true;
continue;
}}this.invArg ();
break;
case 269484192:
direction = -1;
continue;
case 1112541205:
haveRotation = true;
rotAxis.set (direction, 0, 0);
continue;
case 1112541206:
haveRotation = true;
rotAxis.set (0, direction, 0);
continue;
case 1112541207:
haveRotation = true;
rotAxis.set (0, 0, (axesOrientationRasmol && !isMolecular ? -direction : direction));
continue;
case 9:
case 135270417:
case 1073741863:
if (tok == 135270417) i++;
haveRotation = true;
q = this.getQuaternionParameter (i);
if (q != null) {
if (tok == 1073741863 && !(isMolecular = isSelected)) q = q.mulQ (this.viewer.getRotationQuaternion ().mul (-1));
rotAxis.setT (q.getNormal ());
endDegrees = q.getTheta ();
}break;
case 135266307:
haveRotation = true;
if (this.isPoint3f (++i)) {
rotAxis.setT (this.centerParameter (i));
break;
}var p4 = this.getPoint4f (i);
rotAxis.set (p4.x, p4.y, p4.z);
endDegrees = p4.w;
q = J.util.Quaternion.newVA (rotAxis, endDegrees);
break;
case 1048580:
isSelected = true;
isMolecular = true;
haveRotation = true;
if (this.isArrayParameter (++i)) {
dihedralList = this.floatParameterSet (i, 6, 2147483647);
i = this.iToken;
isSpin = true;
} else {
var iAtom1 = this.atomExpressionAt (i).nextSetBit (0);
var iAtom2 = this.atomExpressionAt (++this.iToken).nextSetBit (0);
if (iAtom1 < 0 || iAtom2 < 0) return;
bsAtoms = this.viewer.getBranchBitSet (iAtom2, iAtom1, true);
points[0] = this.viewer.getAtomPoint3f (iAtom1);
points[1] = this.viewer.getAtomPoint3f (iAtom2);
nPoints = 2;
}break;
case 4160:
translation = J.util.V3.newV (this.centerParameter (++i));
isMolecular = isSelected = true;
break;
case 137363468:
helicalPath = true;
continue;
case 1297090050:
var symop = this.intParameter (++i);
if (this.chk) continue;
var info = this.viewer.getSpaceGroupInfo (null);
var op = (info == null ? null : info.get ("operations"));
if (symop == 0 || op == null || op.length < Math.abs (symop)) this.invArg ();
op = op[Math.abs (symop) - 1];
translation = op[5];
invPoint = op[6];
points[0] = op[7];
if (op[8] != null) rotAxis = op[8];
endDegrees = (op[9]).intValue ();
if (symop < 0) {
endDegrees = -endDegrees;
if (translation != null) translation.scale (-1);
}if (endDegrees == 0 && points[0] != null) {
rotAxis.normalize ();
J.util.Measure.getPlaneThroughPoint (points[0], rotAxis, invPlane =  new J.util.P4 ());
}q = J.util.Quaternion.newVA (rotAxis, endDegrees);
nPoints = (points[0] == null ? 0 : 1);
isMolecular = true;
haveRotation = true;
isSelected = true;
continue;
case 135270405:
case 12:
case 11:
haveRotation = true;
if (tok == 135270405) {
bsCompare = this.atomExpressionAt (++i);
ptsA = this.viewer.getAtomPointVector (bsCompare);
if (ptsA == null) this.errorAt (22, i);
i = this.iToken;
ptsB = this.getPointVector (this.getToken (++i), i);
if (ptsB == null || ptsA.size () != ptsB.size ()) this.errorAt (22, i);
m4 =  new J.util.Matrix4f ();
points[0] =  new J.util.P3 ();
nPoints = 1;
var stddev = (this.chk ? 0 : J.util.Measure.getTransformMatrix4 (ptsA, ptsB, m4, points[0]));
if (stddev > 0.001) ptsB = null;
} else if (tok == 12) {
m4 = this.theToken.value;
}m3 =  new J.util.Matrix3f ();
if (m4 != null) {
translation =  new J.util.V3 ();
m4.get (translation);
m4.getRotationScale (m3);
} else {
m3 = this.theToken.value;
}q = (this.chk ?  new J.util.Quaternion () : J.util.Quaternion.newM (m3));
rotAxis.setT (q.getNormal ());
endDegrees = q.getTheta ();
isMolecular = true;
break;
default:
this.invArg ();
}
i = this.iToken;
}
if (this.chk) return;
if (isSelected && bsAtoms == null) bsAtoms = this.viewer.getSelectionSet (false);
if (bsCompare != null) {
isSelected = true;
if (bsAtoms == null) bsAtoms = bsCompare;
}var rate = (degreesPerSecond == 1.4E-45 ? 10 : endDegrees == 3.4028235E38 ? degreesPerSecond : (degreesPerSecond < 0) == (endDegrees > 0) ? -endDegrees / degreesPerSecond : degreesPerSecond);
if (q != null) {
if (nPoints == 0 && translation != null) points[0] = this.viewer.getAtomSetCenter (bsAtoms != null ? bsAtoms : isSelected ? this.viewer.getSelectionSet (false) : this.viewer.getModelUndeletedAtomsBitSet (-1));
if (helicalPath && translation != null) {
points[1] = J.util.P3.newP (points[0]);
points[1].add (translation);
var ret = J.util.Measure.computeHelicalAxis (null, 135266306, points[0], points[1], q);
points[0] = ret[0];
var theta = (ret[3]).x;
if (theta != 0) {
translation = ret[1];
rotAxis = J.util.V3.newV (translation);
if (theta < 0) rotAxis.scale (-1);
}m4 = null;
}if (isSpin && m4 == null) m4 = J.script.ScriptMathProcessor.getMatrix4f (q.getMatrix (), translation);
if (points[0] != null) nPoints = 1;
}if (invPoint != null) {
this.viewer.invertAtomCoordPt (invPoint, bsAtoms);
if (rotAxis == null) return;
}if (invPlane != null) {
this.viewer.invertAtomCoordPlane (invPlane, bsAtoms);
if (rotAxis == null) return;
}if (nPoints < 2 && dihedralList == null) {
if (!isMolecular) {
if (isSpin && bsAtoms == null && !this.useThreads ()) return;
if (this.viewer.rotateAxisAngleAtCenter (this, points[0], rotAxis, rate, endDegrees, isSpin, bsAtoms) && this.isJS && isSpin && bsAtoms == null) throw  new J.script.ScriptInterruption (this, "rotate", 1);
return;
}if (nPoints == 0) points[0] =  new J.util.P3 ();
points[1] = J.util.P3.newP (points[0]);
points[1].add (rotAxis);
nPoints = 2;
}if (nPoints == 0) points[0] =  new J.util.P3 ();
if (nPoints < 2 || points[0].distance (points[1]) == 0) {
points[1] = J.util.P3.newP (points[0]);
points[1].y += 1.0;
}if (endDegrees == 3.4028235E38) endDegrees = 0;
if (endDegrees != 0 && translation != null && !haveRotation) translation.scale (endDegrees / translation.length ());
if (isSpin && translation != null && (endDegrees == 0 || degreesPerSecond == 0)) {
endDegrees = 0.01;
rate = (degreesPerSecond == 1.4E-45 ? 0.01 : degreesPerSecond < 0 ? -endDegrees / degreesPerSecond : degreesPerSecond * 0.01 / translation.length ());
degreesPerSecond = 0.01;
}if (bsAtoms != null && isSpin && ptsB == null && m4 != null) {
ptsA = this.viewer.getAtomPointVector (bsAtoms);
ptsB = J.util.Measure.transformPoints (ptsA, m4, points[0]);
}if (bsAtoms != null && !isSpin && ptsB != null) {
this.viewer.setAtomCoords (bsAtoms, 1146095626, ptsB);
} else {
if (!this.useThreads ()) return;
if (this.viewer.rotateAboutPointsInternal (this, points[0], points[1], rate, endDegrees, isSpin, bsAtoms, translation, ptsB, dihedralList) && this.isJS && isSpin) throw  new J.script.ScriptInterruption (this, "rotate", 1);
}}, $fz.isPrivate = true, $fz), "~B,~B");
$_M(c$, "getQuaternionParameter", 
($fz = function (i) {
switch (this.tokAt (i)) {
case 7:
var sv = (this.getToken (i)).getList ();
var p4 = null;
if (sv.size () == 0 || (p4 = J.script.SV.pt4Value (sv.get (0))) == null) this.invArg ();
return J.util.Quaternion.newP4 (p4);
case 1073741863:
return (this.chk ? null : J.util.Quaternion.newP4 (J.util.Escape.uP (this.viewer.getOrientationText (1073741863, null))));
default:
return J.util.Quaternion.newP4 (this.getPoint4f (i));
}
}, $fz.isPrivate = true, $fz), "~N");
$_M(c$, "getPointVector", 
function (t, i) {
switch (t.tok) {
case 10:
return this.viewer.getAtomPointVector (t.value);
case 7:
var data =  new J.util.JmolList ();
var pt;
var pts = (t).getList ();
for (var j = 0; j < pts.size (); j++) if ((pt = J.script.SV.ptValue (pts.get (j))) != null) data.addLast (pt);
 else return null;

return data;
}
if (i > 0) return this.viewer.getAtomPointVector (this.atomExpressionAt (i));
return null;
}, "J.script.T,~N");
$_M(c$, "getObjectCenter", 
($fz = function (axisID, index, modelIndex) {
var data = [axisID, Integer.$valueOf (index), Integer.$valueOf (modelIndex)];
return (this.getShapePropertyData (22, "getCenter", data) || this.getShapePropertyData (24, "getCenter", data) || this.getShapePropertyData (28, "getCenter", data) || this.getShapePropertyData (25, "getCenter", data) || this.getShapePropertyData (27, "getCenter", data) ? data[2] : null);
}, $fz.isPrivate = true, $fz), "~S,~N,~N");
$_M(c$, "getObjectBoundingBox", 
($fz = function (id) {
var data = [id, null, null];
return (this.getShapePropertyData (24, "getBoundingBox", data) || this.getShapePropertyData (28, "getBoundingBox", data) || this.getShapePropertyData (25, "getBoundingBox", data) || this.getShapePropertyData (27, "getBoundingBox", data) ? data[2] : null);
}, $fz.isPrivate = true, $fz), "~S");
$_M(c$, "getDrawObjectAxis", 
($fz = function (axisID, index) {
var data = [axisID, Integer.$valueOf (index), null];
return (this.getShapePropertyData (22, "getSpinAxis", data) ? data[2] : null);
}, $fz.isPrivate = true, $fz), "~S,~N");
$_M(c$, "script", 
($fz = function (tok, filename) {
var loadCheck = true;
var isCheck = false;
var doStep = false;
var lineNumber = 0;
var pc = 0;
var lineEnd = 0;
var pcEnd = 0;
var i = 2;
var theScript = null;
var localPath = null;
var remotePath = null;
var scriptPath = null;
var params = null;
if (tok == 135287308) {
this.checkLength (2);
if (!this.chk) this.viewer.jsEval (this.parameterAsString (1));
return;
}if (filename == null) {
tok = this.tokAt (1);
if (tok != 4) this.error (16);
filename = this.parameterAsString (1);
if (filename.equalsIgnoreCase ("applet")) {
var appID = this.parameterAsString (2);
theScript = this.parameterExpressionString (3, 0);
this.checkLast (this.iToken);
if (this.chk) return;
if (appID.length == 0 || appID.equals ("all")) appID = "*";
if (!appID.equals (".")) {
this.viewer.jsEval (appID + "\1" + theScript);
if (!appID.equals ("*")) return;
}} else {
tok = this.tokAt (this.slen - 1);
doStep = (tok == 266298);
if (filename.equalsIgnoreCase ("inline")) {
theScript = this.parameterExpressionString (2, (doStep ? this.slen - 1 : 0));
i = this.iToken + 1;
}while (filename.equalsIgnoreCase ("localPath") || filename.equalsIgnoreCase ("remotePath") || filename.equalsIgnoreCase ("scriptPath")) {
if (filename.equalsIgnoreCase ("localPath")) localPath = this.parameterAsString (i++);
 else if (filename.equalsIgnoreCase ("scriptPath")) scriptPath = this.parameterAsString (i++);
 else remotePath = this.parameterAsString (i++);
filename = this.parameterAsString (i++);
}
if (filename.startsWith ("?") && this.viewer.isJS) {
filename = this.loadFileAsync ("SCRIPT_", filename, i, true);
}if ((tok = this.tokAt (i)) == 1073741878) {
isCheck = true;
tok = this.tokAt (++i);
}if (tok == 1073742050) {
loadCheck = false;
tok = this.tokAt (++i);
}if (tok == 1073741998 || tok == 1141899268) {
i++;
lineEnd = lineNumber = Math.max (this.intParameter (i++), 0);
if (this.checkToken (i)) {
if (this.getToken (i).tok == 269484192) lineEnd = (this.checkToken (++i) ? this.intParameter (i++) : 0);
 else lineEnd = -this.intParameter (i++);
if (lineEnd <= 0) this.invArg ();
}} else if (tok == 1073741890 || tok == 1073741892) {
i++;
pc = Math.max (this.intParameter (i++) - 1, 0);
pcEnd = pc + 1;
if (this.checkToken (i)) {
if (this.getToken (i).tok == 269484192) pcEnd = (this.checkToken (++i) ? this.intParameter (i++) : 0);
 else pcEnd = -this.intParameter (i++);
if (pcEnd <= 0) this.invArg ();
}}if (this.tokAt (i) == 269484048) {
params = this.parameterExpressionList (i, -1, false);
i = this.iToken + 1;
}this.checkLength (doStep ? i + 1 : i);
}}if (this.chk && !this.isCmdLine_c_or_C_Option) return;
if (this.isCmdLine_c_or_C_Option) isCheck = true;
var wasSyntaxCheck = this.chk;
var wasScriptCheck = this.isCmdLine_c_or_C_Option;
if (isCheck) this.chk = this.isCmdLine_c_or_C_Option = true;
this.pushContext (null, "SCRIPT");
this.contextPath += " >> " + filename;
if (theScript == null ? this.compileScriptFileInternal (filename, localPath, remotePath, scriptPath) : this.compileScript (null, theScript, false)) {
this.pcEnd = pcEnd;
this.lineEnd = lineEnd;
while (pc < this.lineNumbers.length && this.lineNumbers[pc] < lineNumber) pc++;

this.pc = pc;
var saveLoadCheck = this.isCmdLine_C_Option;
this.isCmdLine_C_Option = new Boolean (this.isCmdLine_C_Option & loadCheck).valueOf ();
this.executionStepping = new Boolean (this.executionStepping | doStep).valueOf ();
this.contextVariables =  new java.util.Hashtable ();
this.contextVariables.put ("_arguments", (params == null ? J.script.SV.getVariableAI ([]) : J.script.SV.getVariableList (params)));
if (isCheck) this.listCommands = true;
var timeMsg = this.viewer.getBoolean (603979934);
if (timeMsg) J.util.Logger.startTimer ("script");
this.dispatchCommands (false, false);
if (timeMsg) this.showString (J.util.Logger.getTimerMsg ("script", 0));
this.isCmdLine_C_Option = saveLoadCheck;
this.popContext (false, false);
} else {
J.util.Logger.error (J.i18n.GT._ ("script ERROR: ") + this.errorMessage);
this.popContext (false, false);
if (wasScriptCheck) {
this.setErrorMessage (null);
} else {
this.evalError (null, null);
}}this.chk = wasSyntaxCheck;
this.isCmdLine_c_or_C_Option = wasScriptCheck;
}, $fz.isPrivate = true, $fz), "~N,~S");
$_M(c$, "$function", 
($fz = function () {
if (this.chk && !this.isCmdLine_c_or_C_Option) return;
var name = this.getToken (0).value;
if (!this.viewer.isFunction (name)) this.error (10);
var params = (this.slen == 1 || this.slen == 3 && this.tokAt (1) == 269484048 && this.tokAt (2) == 269484049 ? null : this.parameterExpressionList (1, -1, false));
if (this.chk) return;
this.runFunctionRet (null, name, params, null, false, true, true);
}, $fz.isPrivate = true, $fz));
$_M(c$, "sync", 
($fz = function () {
this.checkLength (-3);
var text = "";
var applet = "";
switch (this.slen) {
case 1:
applet = "*";
text = "ON";
break;
case 2:
applet = this.parameterAsString (1);
if (applet.indexOf ("jmolApplet") == 0 || J.util.Parser.isOneOf (applet, ";*;.;^;")) {
text = "ON";
if (!this.chk) this.viewer.syncScript (text, applet, 0);
applet = ".";
break;
}if (this.tokAt (1) == 2) {
if (!this.chk) this.viewer.syncScript (null, null, this.intParameter (1));
return;
}text = applet;
applet = "*";
break;
case 3:
if (this.chk) return;
applet = this.parameterAsString (1);
text = (this.tokAt (2) == 528443 ? "GET_GRAPHICS" : this.parameterAsString (2));
if (this.tokAt (1) == 2) {
this.viewer.syncScript (text, null, this.intParameter (1));
return;
}break;
}
if (this.chk) return;
this.viewer.syncScript (text, applet, 0);
}, $fz.isPrivate = true, $fz));
$_M(c$, "history", 
($fz = function (pt) {
if (this.slen == 1) {
this.showString (this.viewer.getSetHistory (2147483647));
return;
}if (pt == 2) {
var n = this.intParameter (this.checkLast (2));
if (n < 0) this.invArg ();
if (!this.chk) this.viewer.getSetHistory (n == 0 ? 0 : -2 - n);
return;
}switch (this.getToken (this.checkLast (1)).tok) {
case 1048589:
case 1073741882:
if (!this.chk) this.viewer.getSetHistory (-2147483648);
return;
case 1048588:
if (!this.chk) this.viewer.getSetHistory (0);
break;
default:
this.errorStr (24, "ON, OFF, CLEAR");
}
}, $fz.isPrivate = true, $fz), "~N");
$_M(c$, "display", 
($fz = function (isDisplay) {
var bs = null;
var addRemove = 0;
var i = 1;
var tok;
switch (tok = this.tokAt (1)) {
case 1276118017:
case 1073742119:
addRemove = tok;
tok = this.tokAt (++i);
break;
}
var isGroup = (tok == 1087373318);
if (isGroup) tok = this.tokAt (++i);
switch (tok) {
case 1048583:
this.setObjectProperty ();
return;
case 0:
break;
default:
if (this.slen == 4 && this.tokAt (2) == 1678770178) bs =  new J.modelset.Bond.BondSet (J.util.BSUtil.newBitSet2 (0, this.viewer.modelSet.bondCount));
 else bs = this.atomExpressionAt (i);
}
if (this.chk) return;
if (Clazz.instanceOf (bs, J.modelset.Bond.BondSet)) {
this.viewer.displayBonds (bs, isDisplay);
return;
}this.viewer.displayAtoms (bs, isDisplay, isGroup, addRemove, this.tQuiet);
}, $fz.isPrivate = true, $fz), "~B");
$_M(c$, "$delete", 
($fz = function () {
if (this.tokAt (1) == 1048583) {
this.setObjectProperty ();
return;
}var bs = (this.slen == 1 ? null : this.atomExpression (this.st, 1, 0, true, false, true, false));
if (this.chk) return;
if (bs == null) bs = this.viewer.getModelUndeletedAtomsBitSet (-1);
var nDeleted = this.viewer.deleteAtoms (bs, false);
if (!(this.tQuiet || this.scriptLevel > this.scriptReportingLevel)) this.scriptStatusOrBuffer (J.i18n.GT._ ("{0} atoms deleted", nDeleted));
}, $fz.isPrivate = true, $fz));
$_M(c$, "minimize", 
($fz = function () {
var bsSelected = null;
var steps = 2147483647;
var crit = 0;
var addHydrogen = false;
var isSilent = false;
var bsFixed = null;
var minimizer = this.viewer.getMinimizer (false);
for (var i = 1; i < this.slen; i++) switch (this.getToken (i).tok) {
case 1073741828:
addHydrogen = true;
continue;
case 1073741874:
case 1073742162:
this.checkLength (2);
if (this.chk || minimizer == null) return;
minimizer.setProperty (this.parameterAsString (i), null);
return;
case 1073741882:
this.checkLength (2);
if (this.chk || minimizer == null) return;
minimizer.setProperty ("clear", null);
return;
case 1073741894:
if (i != 1) this.invArg ();
var n = 0;
var targetValue = 0;
var aList =  Clazz.newIntArray (5, 0);
if (this.tokAt (++i) == 1073741882) {
this.checkLength (3);
} else {
while (n < 4 && !this.isFloatParameter (i)) {
aList[++n] = this.atomExpressionAt (i).nextSetBit (0);
i = this.iToken + 1;
}
aList[0] = n;
if (n == 1) this.invArg ();
targetValue = this.floatParameter (this.checkLast (i));
}if (!this.chk) this.viewer.getMinimizer (true).setProperty ("constraint", [aList,  Clazz.newIntArray (n, 0), Float.$valueOf (targetValue)]);
return;
case 1073741905:
crit = this.floatParameter (++i);
continue;
case 1073741934:
steps = 0;
continue;
case 1060869:
if (i != 1) this.invArg ();
bsFixed = this.atomExpressionAt (++i);
if (bsFixed.nextSetBit (0) < 0) bsFixed = null;
i = this.iToken;
if (!this.chk) this.viewer.getMinimizer (true).setProperty ("fixed", bsFixed);
if (i + 1 == this.slen) return;
continue;
case 135280132:
bsSelected = this.atomExpressionAt (++i);
i = this.iToken;
continue;
case 1073742148:
isSilent = true;
break;
case 266298:
steps = this.intParameter (++i);
continue;
default:
this.invArg ();
break;
}

if (!this.chk) this.viewer.minimize (steps, crit, bsSelected, bsFixed, 0, addHydrogen, isSilent, false);
}, $fz.isPrivate = true, $fz));
$_M(c$, "select", 
($fz = function (i) {
if (this.slen == 1) {
this.viewer.select (null, false, 0, this.tQuiet || this.scriptLevel > this.scriptReportingLevel);
return;
}if (this.slen == 2 && this.tokAt (1) == 1073742072) return;
this.viewer.setNoneSelected (this.slen == 4 && this.tokAt (2) == 1048587);
if (this.tokAt (2) == 10 && Clazz.instanceOf (this.getToken (2).value, J.modelset.Bond.BondSet) || this.getToken (2).tok == 1678770178 && this.getToken (3).tok == 10) {
if (this.slen == this.iToken + 2) {
if (!this.chk) this.viewer.selectBonds (this.theToken.value);
return;
}this.invArg ();
}if (this.getToken (2).tok == 1746538509) {
if (this.slen == 5 && this.getToken (3).tok == 10) {
if (!this.chk) this.setShapeProperty (6, "select", this.theToken.value);
return;
}this.invArg ();
}var bs;
var addRemove = 0;
var isGroup = false;
if (this.getToken (1).intValue == 0) {
var v = this.parameterExpressionToken (0).value;
if (!(Clazz.instanceOf (v, J.util.BS))) this.invArg ();
this.checkLast (this.iToken);
bs = v;
} else {
var tok = this.tokAt (i);
switch (tok) {
case 1276118017:
case 1073742119:
addRemove = tok;
tok = this.tokAt (++i);
}
isGroup = (tok == 1087373318);
if (isGroup) tok = this.tokAt (++i);
bs = this.atomExpressionAt (i);
}if (this.chk) return;
if (this.isBondSet) {
this.viewer.selectBonds (bs);
} else {
if (bs.length () > this.viewer.getAtomCount ()) {
var bs1 = this.viewer.getModelUndeletedAtomsBitSet (-1);
bs1.and (bs);
bs = bs1;
}this.viewer.select (bs, isGroup, addRemove, this.tQuiet || this.scriptLevel > this.scriptReportingLevel);
}}, $fz.isPrivate = true, $fz), "~N");
$_M(c$, "subset", 
($fz = function () {
var bs = null;
if (!this.chk) this.viewer.setSelectionSubset (null);
if (this.slen != 1 && (this.slen != 4 || !this.getToken (2).value.equals ("off"))) bs = this.atomExpressionAt (1);
if (!this.chk) this.viewer.setSelectionSubset (bs);
}, $fz.isPrivate = true, $fz));
$_M(c$, "invertSelected", 
($fz = function () {
var pt = null;
var plane = null;
var bs = null;
var iAtom = -2147483648;
switch (this.tokAt (1)) {
case 0:
if (this.chk) return;
bs = this.viewer.getSelectionSet (false);
pt = this.viewer.getAtomSetCenter (bs);
this.viewer.invertAtomCoordPt (pt, bs);
return;
case 528443:
iAtom = this.atomExpressionAt (2).nextSetBit (0);
bs = this.atomExpressionAt (this.iToken + 1);
break;
case 135266320:
pt = this.centerParameter (2);
break;
case 135266319:
plane = this.planeParameter (2);
break;
case 135267841:
plane = this.hklParameter (2);
break;
}
this.checkLengthErrorPt (this.iToken + 1, 1);
if (plane == null && pt == null && iAtom == -2147483648) this.invArg ();
if (this.chk) return;
if (iAtom == -1) return;
this.viewer.invertSelected (pt, plane, iAtom, bs);
}, $fz.isPrivate = true, $fz));
$_M(c$, "translate", 
($fz = function (isSelected) {
var bs = null;
var i = 1;
var i0 = 0;
if (this.tokAt (1) == 1114638363) {
isSelected = true;
i0 = 1;
i = 2;
}if (this.isPoint3f (i)) {
var pt = this.getPoint3f (i, true);
bs = (!isSelected && this.iToken + 1 < this.slen ? this.atomExpressionAt (++this.iToken) : null);
this.checkLast (this.iToken);
if (!this.chk) this.viewer.setAtomCoordsRelative (pt, bs);
return;
}var xyz = this.parameterAsString (i).toLowerCase ().charAt (0);
if ("xyz".indexOf (xyz) < 0) this.error (0);
var amount = this.floatParameter (++i);
var type;
switch (this.tokAt (++i)) {
case 0:
case 10:
case 1048577:
type = '\0';
break;
default:
type = (this.optParameterAsString (i).toLowerCase () + '\0').charAt (0);
}
if (amount == 0 && type != '\0') return;
this.iToken = i0 + (type == '\0' ? 2 : 3);
bs = (isSelected ? this.viewer.getSelectionSet (false) : this.iToken + 1 < this.slen ? this.atomExpressionAt (++this.iToken) : null);
this.checkLast (this.iToken);
if (!this.chk) this.viewer.translate (xyz, amount, type, bs);
}, $fz.isPrivate = true, $fz), "~B");
$_M(c$, "zap", 
($fz = function (isZapCommand) {
if (this.slen == 1 || !isZapCommand) {
var doAll = (isZapCommand && !this.isStateScript);
if (doAll) this.viewer.cacheFileByName (null, false);
this.viewer.zap (true, doAll, true);
this.refresh ();
return;
}var bs = this.atomExpressionAt (1);
if (this.chk) return;
var nDeleted = this.viewer.deleteAtoms (bs, true);
var isQuiet = (this.tQuiet || this.scriptLevel > this.scriptReportingLevel);
if (!isQuiet) this.scriptStatusOrBuffer (J.i18n.GT._ ("{0} atoms deleted", nDeleted));
this.viewer.select (null, false, 0, isQuiet);
}, $fz.isPrivate = true, $fz), "~B");
$_M(c$, "zoom", 
($fz = function (isZoomTo) {
if (!isZoomTo) {
var tok = (this.slen > 1 ? this.getToken (1).tok : 1048589);
switch (tok) {
case 1073741980:
case 1073742079:
break;
case 1048589:
case 1048588:
if (this.slen > 2) this.error (2);
if (!this.chk) this.setBooleanProperty ("zoomEnabled", tok == 1048589);
return;
}
}var center = null;
var i = 1;
var floatSecondsTotal = (isZoomTo ? (this.isFloatParameter (i) ? this.floatParameter (i++) : 2) : 0);
if (floatSecondsTotal < 0) {
i--;
floatSecondsTotal = 0;
}var ptCenter = 0;
var bsCenter = null;
if (this.isCenterParameter (i)) {
ptCenter = i;
center = this.centerParameter (i);
if (Clazz.instanceOf (this.expressionResult, J.util.BS)) bsCenter = this.expressionResult;
i = this.iToken + 1;
} else if (this.tokAt (i) == 2 && this.getToken (i).intValue == 0) {
bsCenter = this.viewer.getAtomBitSet ("visible");
center = this.viewer.getAtomSetCenter (bsCenter);
}var isSameAtom = false;
var zoom = this.viewer.getZoomSetting ();
var newZoom = this.getZoom (ptCenter, i, bsCenter, zoom);
i = this.iToken + 1;
var xTrans = NaN;
var yTrans = NaN;
if (i != this.slen) {
xTrans = this.floatParameter (i++);
yTrans = this.floatParameter (i++);
}if (i != this.slen) this.invArg ();
if (newZoom < 0) {
newZoom = -newZoom;
if (isZoomTo) {
if (this.slen == 1 || isSameAtom) newZoom *= 2;
 else if (center == null) newZoom /= 2;
}}var max = this.viewer.getMaxZoomPercent ();
if (newZoom < 5 || newZoom > max) this.numberOutOfRange (5, max);
if (!this.viewer.isWindowCentered ()) {
if (center != null) {
var bs = this.atomExpressionAt (ptCenter);
if (!this.chk) this.viewer.setCenterBitSet (bs, false);
}center = this.viewer.getRotationCenter ();
if (Float.isNaN (xTrans)) xTrans = this.viewer.getTranslationXPercent ();
if (Float.isNaN (yTrans)) yTrans = this.viewer.getTranslationYPercent ();
}if (this.chk) return;
if (isSameAtom && Math.abs (zoom - newZoom) < 1) floatSecondsTotal = 0;
this.viewer.moveTo (this, floatSecondsTotal, center, J.viewer.JC.center, NaN, null, newZoom, xTrans, yTrans, NaN, null, NaN, NaN, NaN, NaN, NaN, NaN);
if (this.isJS && floatSecondsTotal > 0 && this.viewer.global.waitForMoveTo) throw  new J.script.ScriptInterruption (this, "zoomTo", 1);
}, $fz.isPrivate = true, $fz), "~B");
$_M(c$, "getZoom", 
($fz = function (ptCenter, i, bs, currentZoom) {
var zoom = (this.isFloatParameter (i) ? this.floatParameter (i++) : NaN);
if (zoom == 0 || currentZoom == 0) {
var r = NaN;
if (bs == null) {
if (this.tokAt (ptCenter) == 1048583) {
var bbox = this.getObjectBoundingBox (this.objectNameParameter (ptCenter + 1));
if (bbox == null || (r = bbox[0].distance (bbox[1]) / 2) == 0) this.invArg ();
}} else {
r = this.viewer.calcRotationRadiusBs (bs);
}if (Float.isNaN (r)) this.invArg ();
currentZoom = this.viewer.getFloat (570425388) / r * 100;
zoom = NaN;
}if (zoom < 0) {
zoom += currentZoom;
} else if (Float.isNaN (zoom)) {
var tok = this.tokAt (i);
switch (tok) {
case 1073742079:
case 1073741980:
zoom = currentZoom * (tok == 1073742079 ? 0.5 : 2);
i++;
break;
case 269484208:
case 269484209:
case 269484193:
var value = this.floatParameter (++i);
i++;
switch (tok) {
case 269484208:
zoom = currentZoom / value;
break;
case 269484209:
zoom = currentZoom * value;
break;
case 269484193:
zoom = currentZoom + value;
break;
}
break;
default:
zoom = (bs == null ? -currentZoom : currentZoom);
}
}this.iToken = i - 1;
return zoom;
}, $fz.isPrivate = true, $fz), "~N,~N,J.util.BS,~N");
$_M(c$, "delay", 
($fz = function () {
var millis = 0;
switch (this.getToken (1).tok) {
case 1048589:
millis = 1;
break;
case 2:
millis = this.intParameter (1) * 1000;
break;
case 3:
millis = Clazz.floatToInt (this.floatParameter (1) * 1000);
break;
default:
this.error (34);
}
if (this.chk || this.viewer.isHeadless () || this.viewer.autoExit) return;
this.refresh ();
this.doDelay (Math.abs (millis));
}, $fz.isPrivate = true, $fz));
$_M(c$, "slab", 
($fz = function (isDepth) {
var TF = false;
var plane = null;
var str;
if (this.isCenterParameter (1) || this.tokAt (1) == 9) plane = this.planeParameter (1);
 else switch (this.getToken (1).tok) {
case 2:
var percent = this.intParameter (this.checkLast (1));
if (!this.chk) if (isDepth) this.viewer.depthToPercent (percent);
 else this.viewer.slabToPercent (percent);
return;
case 1048589:
this.checkLength (2);
TF = true;
case 1048588:
this.checkLength (2);
this.setBooleanProperty ("slabEnabled", TF);
return;
case 4141:
this.checkLength (2);
if (this.chk) return;
this.viewer.slabReset ();
this.setBooleanProperty ("slabEnabled", true);
return;
case 1085443:
this.checkLength (2);
if (this.chk) return;
this.viewer.setSlabDepthInternal (isDepth);
this.setBooleanProperty ("slabEnabled", true);
return;
case 269484192:
str = this.parameterAsString (2);
if (str.equalsIgnoreCase ("hkl")) plane = this.hklParameter (3);
 else if (str.equalsIgnoreCase ("plane")) plane = this.planeParameter (3);
if (plane == null) this.invArg ();
plane.scale (-1);
break;
case 135266319:
switch (this.getToken (2).tok) {
case 1048587:
break;
default:
plane = this.planeParameter (2);
}
break;
case 135267841:
plane = (this.getToken (2).tok == 1048587 ? null : this.hklParameter (2));
break;
case 1073742118:
return;
default:
this.invArg ();
}
if (!this.chk) this.viewer.slabInternal (plane, isDepth);
}, $fz.isPrivate = true, $fz), "~B");
$_M(c$, "ellipsoid", 
($fz = function () {
var mad = 0;
var i = 1;
var translucentLevel = 3.4028235E38;
var checkMore = false;
var isSet = false;
this.setShapeProperty (20, "thisID", null);
switch (this.getToken (1).tok) {
case 1048589:
mad = 2147483647;
break;
case 1048588:
break;
case 2:
mad = this.intParameter (1);
break;
case 1085443:
this.sm.loadShape (20);
this.setShapeProperty (20, "select", this.parameterAsString (2));
i = this.iToken;
checkMore = true;
isSet = true;
break;
case 1074790550:
case 269484209:
case 1073741824:
this.sm.loadShape (20);
if (this.theTok == 1074790550) i++;
this.setShapeId (20, i, false);
i = this.iToken;
checkMore = true;
break;
default:
this.invArg ();
}
if (!checkMore) {
this.setShapeSizeBs (20, mad, null);
return;
}while (++i < this.slen) {
var key = this.parameterAsString (i);
var value = null;
this.getToken (i);
if (!isSet) switch (this.theTok) {
case 1048583:
key = "points";
var data =  new Array (3);
data[0] = this.objectNameParameter (++i);
if (this.chk) continue;
this.getShapePropertyData (24, "getVertices", data);
value = data;
break;
case 1611272194:
var axes =  new Array (3);
for (var j = 0; j < 3; j++) {
axes[j] =  new J.util.V3 ();
axes[j].setT (this.centerParameter (++i));
i = this.iToken;
}
value = axes;
break;
case 12289:
value = this.centerParameter (++i);
i = this.iToken;
break;
case 1095761933:
value = Integer.$valueOf (this.intParameter (++i));
break;
case 12291:
value = Boolean.TRUE;
this.checkLength (i + 1);
break;
}
if (value == null) switch (this.theTok) {
case 1048589:
key = "on";
value = Boolean.TRUE;
break;
case 1048588:
key = "on";
value = Boolean.FALSE;
break;
case 1073742138:
value = Float.$valueOf (this.floatParameter (++i));
break;
case 10:
case 1048577:
key = "atoms";
value = this.atomExpressionAt (i);
i = this.iToken;
break;
case 1766856708:
case 603979967:
case 1073742074:
translucentLevel = this.getColorTrans (i, true);
i = this.iToken;
continue;
case 1073742075:
value = this.parameterAsString (++i);
break;
}
if (value == null) this.invArg ();
this.setShapeProperty (20, key.toLowerCase (), value);
}
this.finalizeObject (20, this.colorArgb[0], translucentLevel, 0, false, null, 0, null);
this.setShapeProperty (20, "thisID", null);
}, $fz.isPrivate = true, $fz));
$_M(c$, "getShapeNameParameter", 
function (i) {
var id = this.parameterAsString (i);
var isWild = id.equals ("*");
if (id.length == 0) this.invArg ();
if (isWild) {
switch (this.tokAt (i + 1)) {
case 0:
case 1048589:
case 1048588:
case 3145768:
case 3145770:
case 1766856708:
case 12291:
break;
default:
if (this.setMeshDisplayProperty (-1, 0, this.tokAt (i + 1))) break;
id += this.optParameterAsString (++i);
}
}if (this.tokAt (i + 1) == 269484209) id += this.parameterAsString (++i);
this.iToken = i;
return id;
}, "~N");
$_M(c$, "setShapeId", 
function (iShape, i, idSeen) {
if (idSeen) this.invArg ();
var name = this.getShapeNameParameter (i).toLowerCase ();
this.setShapeProperty (iShape, "thisID", name);
return name;
}, "~N,~N,~B");
$_M(c$, "setAtomShapeSize", 
($fz = function (shape, scale) {
var rd = null;
var tok = this.tokAt (1);
var isOnly = false;
switch (tok) {
case 1073742072:
this.restrictSelected (false, false);
break;
case 1048589:
break;
case 1048588:
scale = 0;
break;
case 3:
isOnly = (this.floatParameter (1) < 0);
case 2:
default:
rd = this.encodeRadiusParameter (1, isOnly, true);
if (Float.isNaN (rd.value)) this.invArg ();
}
if (rd == null) rd =  new J.atomdata.RadiusData (null, scale, J.atomdata.RadiusData.EnumType.FACTOR, J.constant.EnumVdw.AUTO);
if (isOnly) this.restrictSelected (false, false);
this.setShapeSize (shape, rd);
}, $fz.isPrivate = true, $fz), "~N,~N");
$_M(c$, "encodeRadiusParameter", 
function (index, isOnly, allowAbsolute) {
var value = NaN;
var factorType = J.atomdata.RadiusData.EnumType.ABSOLUTE;
var vdwType = null;
var tok = (index == -1 ? 1649412120 : this.getToken (index).tok);
switch (tok) {
case 1112539137:
case 1112539138:
case 1112541195:
case 1114638362:
case 1112541199:
case 1649412120:
value = 1;
factorType = J.atomdata.RadiusData.EnumType.FACTOR;
vdwType = (tok == 1649412120 ? null : J.constant.EnumVdw.getVdwType2 (J.script.T.nameOf (tok)));
tok = this.tokAt (++index);
break;
}
switch (tok) {
case 4141:
return this.viewer.getDefaultRadiusData ();
case 1073741852:
case 1073742116:
case 1073741856:
case 1073741858:
case 1073741992:
value = 1;
factorType = J.atomdata.RadiusData.EnumType.FACTOR;
this.iToken = index - 1;
break;
case 269484193:
case 2:
case 3:
if (tok == 269484193) {
index++;
} else if (this.tokAt (index + 1) == 269484210) {
value = Math.round (this.floatParameter (index));
this.iToken = ++index;
factorType = J.atomdata.RadiusData.EnumType.FACTOR;
if (value < 0 || value > 200) this.integerOutOfRange (0, 200);
value /= 100;
break;
} else if (tok == 2) {
value = this.intParameter (index);
if (value > 749 || value < -200) this.integerOutOfRange (-200, 749);
if (value > 0) {
value /= 250;
factorType = J.atomdata.RadiusData.EnumType.ABSOLUTE;
} else {
value /= -100;
factorType = J.atomdata.RadiusData.EnumType.FACTOR;
}break;
}value = this.floatParameterRange (index, (isOnly || !allowAbsolute ? -16 : 0), 16);
if (tok == 269484193 || !allowAbsolute) {
factorType = J.atomdata.RadiusData.EnumType.OFFSET;
} else {
factorType = J.atomdata.RadiusData.EnumType.ABSOLUTE;
vdwType = J.constant.EnumVdw.NADA;
}if (isOnly) value = -value;
break;
default:
if (value == 1) index--;
}
if (vdwType == null) {
vdwType = J.constant.EnumVdw.getVdwType (this.optParameterAsString (++this.iToken));
if (vdwType == null) {
this.iToken = index;
vdwType = J.constant.EnumVdw.AUTO;
}}return  new J.atomdata.RadiusData (null, value, factorType, vdwType);
}, "~N,~B,~B");
$_M(c$, "structure", 
($fz = function () {
var type = J.constant.EnumStructure.getProteinStructureType (this.parameterAsString (1));
if (type === J.constant.EnumStructure.NOT) this.invArg ();
var bs = null;
switch (this.tokAt (2)) {
case 10:
case 1048577:
bs = this.atomExpressionAt (2);
this.checkLast (this.iToken);
break;
default:
this.checkLength (2);
}
if (this.chk) return;
this.clearDefinedVariableAtomSets ();
this.viewer.setProteinType (type, bs);
}, $fz.isPrivate = true, $fz));
$_M(c$, "wireframe", 
($fz = function () {
var mad = -2147483648;
if (this.tokAt (1) == 4141) this.checkLast (1);
 else mad = this.getMadParameter ();
if (this.chk) return;
this.setShapeProperty (1, "type", Integer.$valueOf (1023));
this.setShapeSizeBs (1, mad == -2147483648 ? 300 : mad, null);
}, $fz.isPrivate = true, $fz));
$_M(c$, "ssbond", 
($fz = function () {
var mad = this.getMadParameter ();
this.setShapeProperty (1, "type", Integer.$valueOf (256));
this.setShapeSizeBs (1, mad, null);
this.setShapeProperty (1, "type", Integer.$valueOf (1023));
}, $fz.isPrivate = true, $fz));
$_M(c$, "hbond", 
($fz = function () {
if (this.slen == 2 && this.getToken (1).tok == 4102) {
if (this.chk) return;
var n = this.viewer.autoHbond (null, null, false);
this.scriptStatusOrBuffer (J.i18n.GT._ ("{0} hydrogen bonds", Math.abs (n)));
return;
}if (this.slen == 2 && this.getToken (1).tok == 12291) {
if (this.chk) return;
this.connect (0);
return;
}var mad = this.getMadParameter ();
this.setShapeProperty (1, "type", Integer.$valueOf (30720));
this.setShapeSizeBs (1, mad, null);
this.setShapeProperty (1, "type", Integer.$valueOf (1023));
}, $fz.isPrivate = true, $fz));
$_M(c$, "configuration", 
($fz = function () {
var bsAtoms;
if (this.slen == 1) {
bsAtoms = this.viewer.setConformation ();
this.viewer.addStateScriptRet ("select", null, this.viewer.getSelectionSet (false), null, "configuration", true, false);
} else {
var n = this.intParameter (this.checkLast (1));
if (this.chk) return;
bsAtoms = this.viewer.getConformation (this.viewer.getCurrentModelIndex (), n - 1, true);
this.viewer.addStateScript ("configuration " + n + ";", true, false);
}if (this.chk) return;
this.setShapeProperty (1, "type", Integer.$valueOf (30720));
this.setShapeSizeBs (1, 0, bsAtoms);
this.viewer.autoHbond (bsAtoms, bsAtoms, true);
this.viewer.select (bsAtoms, false, 0, this.tQuiet);
}, $fz.isPrivate = true, $fz));
$_M(c$, "vector", 
($fz = function () {
var type = J.atomdata.RadiusData.EnumType.SCREEN;
var value = 1;
this.checkLength (-3);
switch (this.iToken = this.slen) {
case 1:
break;
case 2:
switch (this.getToken (1).tok) {
case 1048589:
break;
case 1048588:
value = 0;
break;
case 2:
value = this.intParameterRange (1, 0, 19);
break;
case 3:
type = J.atomdata.RadiusData.EnumType.ABSOLUTE;
value = this.floatParameterRange (1, 0, 3);
break;
default:
this.error (6);
}
break;
case 3:
if (this.tokAt (1) == 1073742138) {
this.setFloatProperty ("vectorScale", this.floatParameterRange (2, -100, 100));
return;
}}
this.setShapeSize (18,  new J.atomdata.RadiusData (null, value, type, null));
}, $fz.isPrivate = true, $fz));
$_M(c$, "vibration", 
($fz = function () {
this.checkLength (-3);
var period = 0;
switch (this.getToken (1).tok) {
case 1048589:
this.checkLength (2);
period = this.viewer.getFloat (570425412);
break;
case 1048588:
this.checkLength (2);
period = 0;
break;
case 2:
case 3:
this.checkLength (2);
period = this.floatParameter (1);
break;
case 1073742138:
this.setFloatProperty ("vibrationScale", this.floatParameterRange (2, -10, 10));
return;
case 1073742090:
this.setFloatProperty ("vibrationPeriod", this.floatParameter (2));
return;
case 1073741824:
this.invArg ();
break;
default:
period = -1;
}
if (period < 0) this.invArg ();
if (this.chk) return;
if (period == 0) {
this.viewer.setVibrationOff ();
return;
}this.viewer.setVibrationPeriod (-period);
}, $fz.isPrivate = true, $fz));
$_M(c$, "calculate", 
($fz = function () {
var isSurface = false;
var asDSSP = false;
var bs1 = null;
var bs2 = null;
var n = -2147483648;
if ((this.iToken = this.slen) >= 2) {
this.clearDefinedVariableAtomSets ();
switch (this.getToken (1).tok) {
case 1073741824:
this.checkLength (2);
break;
case 1632634889:
this.checkLength (2);
if (this.chk) return;
n = this.viewer.calculateFormalCharges (null);
this.showString (J.i18n.GT._ ("{0} charges modified", n));
return;
case 1076887572:
this.checkLength (2);
if (!this.chk) this.viewer.assignAromaticBonds ();
return;
case 1612189718:
if (this.slen != 2) {
asDSSP = (this.tokAt (++this.iToken) == 1641025539);
if (asDSSP) bs1 = this.viewer.getSelectionSet (false);
 else bs1 = this.atomExpressionAt (this.iToken);
if (!asDSSP && !(asDSSP = (this.tokAt (++this.iToken) == 1641025539))) bs2 = this.atomExpressionAt (this.iToken);
}if (this.chk) return;
n = this.viewer.autoHbond (bs1, bs2, false);
if (n != -2147483648) this.scriptStatusOrBuffer (J.i18n.GT._ ("{0} hydrogen bonds", Math.abs (n)));
return;
case 1613758476:
bs1 = (this.slen == 2 ? null : this.atomExpressionAt (2));
this.checkLast (this.iToken);
if (!this.chk) this.viewer.addHydrogens (bs1, false, false);
return;
case 1112541196:
this.iToken = 1;
bs1 = (this.slen == 2 ? null : this.atomExpressionAt (2));
this.checkLast (this.iToken);
if (!this.chk) this.viewer.calculatePartialCharges (bs1);
return;
case 1073742102:
this.pointGroup ();
return;
case 1112539150:
this.checkLength (2);
if (!this.chk) {
this.viewer.calculateStraightness ();
this.viewer.addStateScript ("set quaternionFrame '" + this.viewer.getQuaternionFrame () + "'; calculate straightness", false, true);
}return;
case 1641025539:
bs1 = (this.slen < 4 ? null : this.atomExpressionAt (2));
switch (this.tokAt (++this.iToken)) {
case 1052714:
break;
case 1073741915:
asDSSP = true;
break;
case 0:
asDSSP = this.viewer.getBoolean (603979825);
break;
default:
this.invArg ();
}
if (!this.chk) this.showString (this.viewer.calculateStructures (bs1, asDSSP, true));
return;
case 1708058:
bs1 = (this.iToken + 1 < this.slen ? this.atomExpressionAt (++this.iToken) : null);
bs2 = (this.iToken + 1 < this.slen ? this.atomExpressionAt (++this.iToken) : null);
this.checkLength (++this.iToken);
if (!this.chk) {
n = this.viewer.calculateStruts (bs1, bs2);
if (n > 0) {
this.setShapeProperty (1, "type", Integer.$valueOf (32768));
this.setShapePropertyBs (1, "color", Integer.$valueOf (0x0FFFFFF), null);
this.setShapeTranslucency (1, "", "translucent", 0.5, null);
this.setShapeProperty (1, "type", Integer.$valueOf (1023));
}this.showString (J.i18n.GT._ ("{0} struts added", n));
}return;
case 3145756:
isSurface = true;
case 1112539151:
var isFrom = false;
switch (this.tokAt (2)) {
case 135266324:
this.iToken++;
break;
case 0:
isFrom = !isSurface;
break;
case 1073741952:
isFrom = true;
this.iToken++;
break;
default:
isFrom = true;
}
bs1 = (this.iToken + 1 < this.slen ? this.atomExpressionAt (++this.iToken) : this.viewer.getSelectionSet (false));
this.checkLength (++this.iToken);
if (!this.chk) this.viewer.calculateSurface (bs1, (isFrom ? 3.4028235E38 : -1));
return;
}
}this.errorStr2 (53, "CALCULATE", "aromatic? hbonds? hydrogen? formalCharge? partialCharge? pointgroup? straightness? structure? struts? surfaceDistance FROM? surfaceDistance WITHIN?");
}, $fz.isPrivate = true, $fz));
$_M(c$, "pointGroup", 
function () {
switch (this.tokAt (0)) {
case 4102:
if (!this.chk) this.showString (this.viewer.calculatePointGroup ());
return;
case 4148:
if (!this.chk) this.showString (this.viewer.getPointGroupAsString (false, null, 0, 0));
return;
}
var pt = 2;
var type = (this.tokAt (pt) == 1073742138 ? "" : this.optParameterAsString (pt));
if (type.equals ("chemicalShift")) type = "cs";
var scale = 1;
var index = 0;
if (type.length > 0) {
if (this.isFloatParameter (++pt)) index = this.intParameter (pt++);
}if (this.tokAt (pt) == 1073742138) scale = this.floatParameter (++pt);
if (!this.chk) this.runScript (this.viewer.getPointGroupAsString (true, type, index, scale));
});
$_M(c$, "dots", 
($fz = function (iShape) {
if (!this.chk) this.sm.loadShape (iShape);
this.setShapeProperty (iShape, "init", null);
var value = NaN;
var type = J.atomdata.RadiusData.EnumType.ABSOLUTE;
var ipt = 1;
while (true) {
switch (this.getToken (ipt).tok) {
case 1073742072:
this.restrictSelected (false, false);
value = 1;
type = J.atomdata.RadiusData.EnumType.FACTOR;
break;
case 1048589:
value = 1;
type = J.atomdata.RadiusData.EnumType.FACTOR;
break;
case 1048588:
value = 0;
break;
case 1073741976:
this.setShapeProperty (iShape, "ignore", this.atomExpressionAt (ipt + 1));
ipt = this.iToken + 1;
continue;
case 2:
var dotsParam = this.intParameter (ipt);
if (this.tokAt (ipt + 1) == 1666189314) {
ipt++;
this.setShapeProperty (iShape, "atom", Integer.$valueOf (dotsParam));
this.setShapeProperty (iShape, "radius", Float.$valueOf (this.floatParameter (++ipt)));
if (this.tokAt (++ipt) == 1766856708) {
this.setShapeProperty (iShape, "colorRGB", Integer.$valueOf (this.getArgbParam (++ipt)));
ipt++;
}if (this.getToken (ipt).tok != 10) this.invArg ();
this.setShapeProperty (iShape, "dots", this.st[ipt].value);
return;
}break;
}
break;
}
var rd = (Float.isNaN (value) ? this.encodeRadiusParameter (ipt, false, true) :  new J.atomdata.RadiusData (null, value, type, J.constant.EnumVdw.AUTO));
if (Float.isNaN (rd.value)) this.invArg ();
this.setShapeSize (iShape, rd);
}, $fz.isPrivate = true, $fz), "~N");
$_M(c$, "proteinShape", 
($fz = function (shapeType) {
var mad = 0;
switch (this.getToken (1).tok) {
case 1073742072:
if (this.chk) return;
this.restrictSelected (false, false);
mad = -1;
break;
case 1048589:
mad = -1;
break;
case 1048588:
break;
case 1641025539:
mad = -2;
break;
case 1112541199:
case 1073741922:
mad = -4;
break;
case 2:
mad = (this.intParameterRange (1, 0, 1000) * 8);
break;
case 3:
mad = Math.round (this.floatParameterRange (1, -4.0, 4.0) * 2000);
if (mad < 0) {
this.restrictSelected (false, false);
mad = -mad;
}break;
case 10:
if (!this.chk) this.sm.loadShape (shapeType);
this.setShapeProperty (shapeType, "bitset", this.theToken.value);
return;
default:
this.error (6);
}
this.setShapeSizeBs (shapeType, mad, null);
}, $fz.isPrivate = true, $fz), "~N");
$_M(c$, "modulation", 
($fz = function () {
var mod = false;
switch (this.getToken (1).tok) {
case 1048589:
mod = true;
case 1048588:
if (!this.chk) this.viewer.setModulation (mod, null, 2147483647, false);
break;
case 2:
if (!this.chk) this.viewer.setModulation (true, [this.intParameter (1)], 2147483647, false);
break;
case 1074790526:
if (!this.chk) this.viewer.setModulationFps (this.floatParameter (2));
break;
case 1073742096:
if (!this.chk) this.viewer.setModulation (true, [this.intParameter (2)], this.intParameter (3), false);
break;
}
}, $fz.isPrivate = true, $fz));
$_M(c$, "animation", 
($fz = function () {
var animate = false;
switch (this.getToken (1).tok) {
case 1048589:
animate = true;
case 1048588:
if (!this.chk) this.viewer.setAnimationOn (animate);
break;
case 1073742031:
var morphCount = Clazz.floatToInt (this.floatParameter (2));
if (!this.chk) this.viewer.setAnimMorphCount (Math.abs (morphCount));
break;
case 1610625028:
this.iToken = 2;
var bs = (this.tokAt (2) == 1048579 ? null : this.atomExpressionAt (2));
this.checkLength (this.iToken + 1);
if (!this.chk) this.viewer.setAnimDisplay (bs);
return;
case 4115:
if (this.isArrayParameter (2)) {
var f = this.floatParameterSet (2, 0, 2147483647);
this.checkLength (this.iToken + 1);
if (this.chk) return;
var frames =  Clazz.newIntArray (f.length, 0);
for (var i = f.length; --i >= 0; ) frames[i] = Clazz.floatToInt (f[i]);

var movie =  new java.util.Hashtable ();
movie.put ("frames", frames);
movie.put ("currentFrame", Integer.$valueOf (0));
this.viewer.setMovie (movie);
} else {
this.model (2);
}break;
case 1073742024:
var startDelay = 1;
var endDelay = 1;
if (this.slen > 5) this.error (2);
var animationMode = null;
switch (this.getToken (2).tok) {
case 1073742070:
animationMode = J.constant.EnumAnimationMode.ONCE;
startDelay = endDelay = 0;
break;
case 528410:
animationMode = J.constant.EnumAnimationMode.LOOP;
break;
case 1073742082:
animationMode = J.constant.EnumAnimationMode.PALINDROME;
break;
default:
this.invArg ();
}
if (this.slen >= 4) {
startDelay = endDelay = this.floatParameter (3);
if (this.slen == 5) endDelay = this.floatParameter (4);
}if (!this.chk) this.viewer.setAnimationReplayMode (animationMode, startDelay, endDelay);
break;
case 1073741918:
var i = 2;
var direction = 0;
switch (this.tokAt (i)) {
case 269484192:
direction = -this.intParameter (++i);
break;
case 269484193:
direction = this.intParameter (++i);
break;
case 2:
direction = this.intParameter (i);
if (direction > 0) direction = 0;
break;
default:
this.invArg ();
}
this.checkLength (++i);
if (direction != 1 && direction != -1) this.errorStr2 (35, "-1", "1");
if (!this.chk) this.viewer.setAnimationDirection (direction);
break;
case 1074790526:
this.setIntProperty ("animationFps", this.intParameter (this.checkLast (2)));
break;
default:
this.frameControl (1);
}
}, $fz.isPrivate = true, $fz));
$_M(c$, "assign", 
($fz = function () {
var atomsOrBonds = this.tokAt (1);
var index = this.atomExpressionAt (2).nextSetBit (0);
var index2 = -1;
var type = null;
if (index < 0) return;
if (atomsOrBonds == 4106) {
index2 = this.atomExpressionAt (++this.iToken).nextSetBit (0);
} else {
type = this.parameterAsString (++this.iToken);
}var pt = (++this.iToken < this.slen ? this.centerParameter (this.iToken) : null);
if (this.chk) return;
switch (atomsOrBonds) {
case 1141899265:
this.clearDefinedVariableAtomSets ();
this.viewer.assignAtom (index, pt, type);
break;
case 1678770178:
this.viewer.assignBond (index, (type + "p").charAt (0));
break;
case 4106:
this.viewer.assignConnect (index, index2);
}
}, $fz.isPrivate = true, $fz));
$_M(c$, "file", 
($fz = function () {
var file = this.intParameter (this.checkLast (1));
if (this.chk) return;
var modelIndex = this.viewer.getModelNumberIndex (file * 1000000 + 1, false, false);
var modelIndex2 = -1;
if (modelIndex >= 0) {
modelIndex2 = this.viewer.getModelNumberIndex ((file + 1) * 1000000 + 1, false, false);
if (modelIndex2 < 0) modelIndex2 = this.viewer.getModelCount ();
modelIndex2--;
}this.viewer.setAnimationOn (false);
this.viewer.setAnimationDirection (1);
this.viewer.setAnimationRange (modelIndex, modelIndex2);
this.viewer.setCurrentModelIndex (-1);
}, $fz.isPrivate = true, $fz));
$_M(c$, "fixed", 
($fz = function () {
var bs = (this.slen == 1 ? null : this.atomExpressionAt (1));
if (this.chk) return;
this.viewer.setMotionFixedAtoms (bs);
}, $fz.isPrivate = true, $fz));
$_M(c$, "model", 
($fz = function (offset) {
var isFrame = (this.theTok == 4115);
var useModelNumber = true;
if (this.slen == 1 && offset == 1) {
var modelIndex = this.viewer.getCurrentModelIndex ();
var m;
if (!this.chk && modelIndex >= 0 && (m = this.viewer.getJmolDataSourceFrame (modelIndex)) >= 0) this.viewer.setCurrentModelIndex (m == modelIndex ? -2147483648 : m);
return;
}switch (this.tokAt (1)) {
case 2:
if (isFrame && this.slen == 2) {
if (!this.chk) this.viewer.setFrame (this.intParameter (1));
return;
}break;
case 1048577:
case 10:
var i = this.atomExpressionAt (1).nextSetBit (0);
this.checkLength (this.iToken + 1);
if (this.chk || i < 0) return;
var bsa =  new J.util.BS ();
bsa.set (i);
this.viewer.setCurrentModelIndex (this.viewer.getModelBitSet (bsa, false).nextSetBit (0));
return;
case 1073741904:
this.iToken = 1;
var n = (this.tokAt (2) == 2 ? this.intParameter (++this.iToken) : 1);
this.checkLength (this.iToken + 1);
if (!this.chk && n > 0) this.viewer.createModels (n);
return;
case 1074790550:
this.checkLength (3);
var id = this.stringParameter (2);
if (!this.chk) this.viewer.setCurrentModelID (id);
return;
case 528397:
var millis = 0;
this.checkLength (3);
switch (this.getToken (2).tok) {
case 2:
case 3:
millis = Clazz.floatToLong (this.floatParameter (2) * 1000);
break;
default:
this.error (20);
}
if (!this.chk) this.viewer.setFrameDelayMs (millis);
return;
case 1073742166:
if (this.checkLength23 () > 0) if (!this.chk) this.viewer.setFrameTitleObj (this.slen == 2 ? "@{_modelName}" : (this.tokAt (2) == 7 ? J.script.SV.listValue (this.st[2]) : this.parameterAsString (2)));
return;
case 1073741832:
var bs = (this.slen == 2 || this.tokAt (2) == 1048587 ? null : this.atomExpressionAt (2));
if (!this.chk) this.viewer.setFrameOffsets (bs);
return;
}
if (this.getToken (offset).tok == 269484192) {
++offset;
if (this.getToken (this.checkLast (offset)).tok != 2 || this.intParameter (offset) != 1) this.invArg ();
if (!this.chk) this.viewer.setAnimation (1073742108);
return;
}var isPlay = false;
var isRange = false;
var isAll = false;
var isHyphen = false;
var frameList = [-1, -1];
var nFrames = 0;
var fFrame = 0;
var haveFileSet = this.viewer.haveFileSet ();
for (var i = offset; i < this.slen; i++) {
switch (this.getToken (i).tok) {
case 1048579:
case 269484209:
this.checkLength (offset + (isRange ? 2 : 1));
isAll = true;
break;
case 269484192:
if (nFrames != 1) this.invArg ();
isHyphen = true;
break;
case 1048587:
this.checkLength (offset + 1);
break;
case 3:
useModelNumber = false;
if ((fFrame = this.floatParameter (i)) < 0) {
this.checkLength (i + 1);
if (!this.chk) this.viewer.morph (-fFrame);
return;
}case 2:
case 4:
if (nFrames == 2) this.invArg ();
var iFrame = (this.theTok == 4 ? J.script.ScriptEvaluator.getFloatEncodedInt (this.theToken.value) : this.theToken.intValue);
if (iFrame < 0 && nFrames == 1) {
isHyphen = true;
iFrame = -iFrame;
if (haveFileSet && iFrame < 1000000) iFrame *= 1000000;
}if (this.theTok == 3 && haveFileSet && fFrame == Clazz.floatToInt (fFrame)) iFrame = Clazz.floatToInt (fFrame) * 1000000;
if (iFrame == 2147483647) {
if (i == 1) {
var id = this.theToken.value.toString ();
var modelIndex = (this.chk ? -1 : this.viewer.getModelIndexFromId (id));
if (modelIndex >= 0) {
this.checkLength (2);
this.viewer.setCurrentModelIndex (modelIndex);
return;
}}iFrame = 0;
}if (iFrame == -1) {
this.checkLength (offset + 1);
if (!this.chk) this.viewer.setAnimation (1073742108);
return;
}if (iFrame >= 1000 && iFrame < 1000000 && haveFileSet) iFrame = (Clazz.doubleToInt (iFrame / 1000)) * 1000000 + (iFrame % 1000);
if (!useModelNumber && iFrame == 0 && nFrames == 0) isAll = true;
if (iFrame >= 1000000) useModelNumber = false;
frameList[nFrames++] = iFrame;
break;
case 1073742096:
isPlay = true;
break;
case 1073742114:
isRange = true;
break;
default:
this.frameControl (offset);
return;
}
}
if (isRange && nFrames == 0) isAll = true;
if (this.chk) return;
if (isAll) {
this.viewer.setAnimationOn (false);
this.viewer.setAnimationRange (-1, -1);
if (!isRange) this.viewer.setCurrentModelIndex (-1);
return;
}if (nFrames == 2 && !isRange) isHyphen = true;
if (haveFileSet) useModelNumber = false;
 else if (useModelNumber) for (var i = 0; i < nFrames; i++) if (frameList[i] >= 0) frameList[i] %= 1000000;

var modelIndex = this.viewer.getModelNumberIndex (frameList[0], useModelNumber, false);
var modelIndex2 = -1;
if (haveFileSet && modelIndex < 0 && frameList[0] != 0) {
if (frameList[0] < 1000000) frameList[0] *= 1000000;
if (nFrames == 2 && frameList[1] < 1000000) frameList[1] *= 1000000;
if (frameList[0] % 1000000 == 0) {
frameList[0]++;
modelIndex = this.viewer.getModelNumberIndex (frameList[0], false, false);
if (modelIndex >= 0) {
var i2 = (nFrames == 1 ? frameList[0] + 1000000 : frameList[1] == 0 ? -1 : frameList[1] % 1000000 == 0 ? frameList[1] + 1000001 : frameList[1] + 1);
modelIndex2 = this.viewer.getModelNumberIndex (i2, false, false);
if (modelIndex2 < 0) modelIndex2 = this.viewer.getModelCount ();
modelIndex2--;
if (isRange) nFrames = 2;
 else if (!isHyphen && modelIndex2 != modelIndex) isHyphen = true;
isRange = isRange || modelIndex == modelIndex2;
}} else {
return;
}}if (!isPlay && !isRange || modelIndex >= 0) this.viewer.setCurrentModelIndexClear (modelIndex, false);
if (isPlay && nFrames == 2 || isRange || isHyphen) {
if (modelIndex2 < 0) modelIndex2 = this.viewer.getModelNumberIndex (frameList[1], useModelNumber, false);
this.viewer.setAnimationOn (false);
this.viewer.setAnimationDirection (1);
this.viewer.setAnimationRange (modelIndex, modelIndex2);
this.viewer.setCurrentModelIndexClear (isHyphen && !isRange ? -1 : modelIndex >= 0 ? modelIndex : 0, false);
}if (isPlay) this.viewer.setAnimation (266287);
}, $fz.isPrivate = true, $fz), "~N");
$_M(c$, "bitSetForModelFileNumber", 
function (m) {
var bs = J.util.BSUtil.newBitSet (this.viewer.getAtomCount ());
if (this.chk) return bs;
var modelCount = this.viewer.getModelCount ();
var haveFileSet = this.viewer.haveFileSet ();
if (m < 1000000 && haveFileSet) m *= 1000000;
var pt = m % 1000000;
if (pt == 0) {
var model1 = this.viewer.getModelNumberIndex (m + 1, false, false);
if (model1 < 0) return bs;
var model2 = (m == 0 ? modelCount : this.viewer.getModelNumberIndex (m + 1000001, false, false));
if (model1 < 0) model1 = 0;
if (model2 < 0) model2 = modelCount;
if (this.viewer.isTrajectory (model1)) model2 = model1 + 1;
for (var j = model1; j < model2; j++) bs.or (this.viewer.getModelUndeletedAtomsBitSet (j));

} else {
var modelIndex = this.viewer.getModelNumberIndex (m, false, true);
if (modelIndex >= 0) bs.or (this.viewer.getModelUndeletedAtomsBitSet (modelIndex));
}return bs;
}, "~N");
$_M(c$, "frameControl", 
($fz = function (i) {
switch (this.getToken (this.checkLast (i)).tok) {
case 1073742098:
case 1073742096:
case 266287:
case 20487:
case 1073742037:
case 1073742108:
case 1073742126:
case 1073741942:
case 1073741993:
if (!this.chk) this.viewer.setAnimation (this.theTok);
return;
}
this.invArg ();
}, $fz.isPrivate = true, $fz), "~N");
$_M(c$, "getShapeType", 
($fz = function (tok) {
var iShape = J.viewer.JC.shapeTokenIndex (tok);
if (iShape < 0) this.error (49);
return iShape;
}, $fz.isPrivate = true, $fz), "~N");
$_M(c$, "font", 
($fz = function (shapeType, fontsize) {
var fontface = "SansSerif";
var fontstyle = "Plain";
var sizeAdjust = 0;
var scaleAngstromsPerPixel = -1;
switch (this.iToken = this.slen) {
case 6:
scaleAngstromsPerPixel = this.floatParameter (5);
if (scaleAngstromsPerPixel >= 5) scaleAngstromsPerPixel = this.viewer.getZoomSetting () / scaleAngstromsPerPixel / this.viewer.getScalePixelsPerAngstrom (false);
case 5:
if (this.getToken (4).tok != 1073741824) this.invArg ();
fontstyle = this.parameterAsString (4);
case 4:
if (this.getToken (3).tok != 1073741824) this.invArg ();
fontface = this.parameterAsString (3);
if (!this.isFloatParameter (2)) this.error (34);
fontsize = this.floatParameter (2);
shapeType = this.getShapeType (this.getToken (1).tok);
break;
case 3:
if (!this.isFloatParameter (2)) this.error (34);
if (shapeType == -1) {
shapeType = this.getShapeType (this.getToken (1).tok);
fontsize = this.floatParameter (2);
} else {
if (fontsize >= 1) fontsize += (sizeAdjust = 5);
}break;
case 2:
default:
if (shapeType == 5) {
fontsize = 13;
break;
}this.error (2);
}
if (shapeType == 5) {
if (fontsize < 0 || fontsize >= 1 && (fontsize < 6 || fontsize > 63)) this.integerOutOfRange (6 - sizeAdjust, 63 - sizeAdjust);
this.setShapeProperty (5, "setDefaults", this.viewer.getNoneSelected ());
}if (this.chk) return;
if (J.util.GData.getFontStyleID (fontface) >= 0) {
fontstyle = fontface;
fontface = "SansSerif";
}var font3d = this.viewer.getFont3D (fontface, fontstyle, fontsize);
this.sm.loadShape (shapeType);
this.setShapeProperty (shapeType, "font", font3d);
if (scaleAngstromsPerPixel >= 0) this.setShapeProperty (shapeType, "scalereference", Float.$valueOf (scaleAngstromsPerPixel));
}, $fz.isPrivate = true, $fz), "~N,~N");
$_M(c$, "set", 
($fz = function () {
if (this.slen == 1) {
this.showString (this.viewer.getAllSettings (null));
return;
}var isJmolSet = (this.parameterAsString (0).equals ("set"));
var key = this.optParameterAsString (1);
if (isJmolSet && this.slen == 2 && key.indexOf ("?") >= 0) {
this.showString (this.viewer.getAllSettings (key.substring (0, key.indexOf ("?"))));
return;
}var tok = this.getToken (1).tok;
var newTok = 0;
var sval;
var ival = 2147483647;
var showing = (!this.chk && !this.tQuiet && this.scriptLevel <= this.scriptReportingLevel && !(this.st[0].value).equals ("var"));
switch (tok) {
case 1611272194:
this.axes (2);
return;
case 1610616835:
this.background (2);
return;
case 1679429641:
this.boundbox (2);
return;
case 1611272202:
this.frank (2);
return;
case 1610616855:
this.history (2);
return;
case 1826248715:
this.label (2);
return;
case 1614417948:
this.unitcell (2);
return;
case 536870920:
this.sm.loadShape (8);
this.setShapeProperty (8, "highlight", (this.tokAt (2) == 1048588 ? null : this.atomExpressionAt (2)));
return;
case 1610625028:
case 1611141171:
this.selectionHalo (2);
return;
case 536875070:
this.timeout (2);
return;
}
switch (tok) {
case 1641025539:
var type = J.constant.EnumStructure.getProteinStructureType (this.parameterAsString (2));
if (type === J.constant.EnumStructure.NOT) this.invArg ();
var data = this.floatParameterSet (3, 0, 2147483647);
if (data.length % 4 != 0) this.invArg ();
this.viewer.setStructureList (data, type);
this.checkLast (this.iToken);
return;
case 545259526:
ival = this.getArgbParam (2);
if (!this.chk) this.setObjectArgb ("axes", ival);
return;
case 1610612737:
this.setBondmode ();
return;
case 536870916:
if (this.chk) return;
var iLevel = (this.tokAt (2) == 1048588 || this.tokAt (2) == 2 && this.intParameter (2) == 0 ? 4 : 5);
J.util.Logger.setLogLevel (iLevel);
this.setIntProperty ("logLevel", iLevel);
if (iLevel == 4) {
this.viewer.setDebugScript (false);
if (showing) this.viewer.showParameter ("debugScript", true, 80);
}this.setDebugging ();
if (showing) this.viewer.showParameter ("logLevel", true, 80);
return;
case 537022465:
this.setEcho ();
return;
case 1610612738:
this.font (5, this.checkLength23 () == 2 ? 0 : this.floatParameter (2));
return;
case 1612189718:
this.setHbond ();
return;
case 1746538509:
case 537006096:
this.setMonitor ();
return;
case 1611141176:
this.setSsbond ();
return;
case 1610612741:
this.setLabel ("toggle");
return;
case 536870930:
this.setUserColors ();
return;
case 553648188:
this.setZslab ();
return;
}
var justShow = true;
switch (tok) {
case 536870914:
if (this.slen > 2) {
var modelDotted = this.stringSetting (2, false);
var modelNumber;
var useModelNumber = false;
if (modelDotted.indexOf (".") < 0) {
modelNumber = J.util.Parser.parseInt (modelDotted);
useModelNumber = true;
} else {
modelNumber = J.script.ScriptEvaluator.getFloatEncodedInt (modelDotted);
}if (this.chk) return;
var modelIndex = this.viewer.getModelNumberIndex (modelNumber, useModelNumber, true);
this.viewer.setBackgroundModelIndex (modelIndex);
return;
}break;
case 1649412120:
if (this.chk) return;
this.viewer.setAtomProperty (this.viewer.getModelUndeletedAtomsBitSet (-1), 1649412120, -1, NaN, null, null, null);
switch (this.tokAt (2)) {
case 1073742109:
this.runScript ("#VDW radii for PROBE;{_H}.vdw = 1.0;{_H and connected(_C) and not connected(within(smiles,\'[a]\'))}.vdw = 1.17;{_C}.vdw = 1.75;{_C and connected(3) and connected(_O)}.vdw = 1.65;{_N}.vdw = 1.55;{_O}.vdw = 1.4;{_P}.vdw = 1.8;{_S}.vdw = 1.8;message VDW radii for H, C, N, O, P, and S set according to Word, et al., J. Mol. Biol. (1999) 285, 1711-1733");
return;
}
newTok = 545259555;
case 545259555:
if (this.slen > 2) {
sval = (this.slen == 3 && J.constant.EnumVdw.getVdwType (this.parameterAsString (2)) == null ? this.stringSetting (2, false) : this.parameterAsString (2));
if (J.constant.EnumVdw.getVdwType (sval) == null) this.invArg ();
this.setStringProperty (key, sval);
}break;
case 536870918:
if (this.slen > 2) {
var pt;
var $var = this.parameterExpressionToken (2);
if ($var.tok == 8) pt = $var.value;
 else {
var ijk = $var.asInt ();
if (ijk < 555) pt =  new J.util.P3 ();
 else pt = this.viewer.getSymmetry ().ijkToPoint3f (ijk + 111);
}if (!this.chk) this.viewer.setDefaultLattice (pt);
}break;
case 545259552:
case 545259545:
if (this.slen > 2) {
if ((this.theTok = this.tokAt (2)) == 1073741992 || this.theTok == 1073742116) {
sval = this.parameterAsString (this.checkLast (2));
} else {
sval = this.stringSetting (2, false);
}this.setStringProperty (key, sval);
}break;
case 1632634889:
ival = this.intSetting (2);
if (ival == -2147483648) this.invArg ();
if (!this.chk) this.viewer.setFormalCharges (ival);
return;
case 553648148:
ival = this.intSetting (2);
if (!this.chk) {
if (ival != -2147483648) this.commandHistoryLevelMax = ival;
this.setIntProperty (key, ival);
}break;
case 545259564:
if (this.slen > 2) this.setStringProperty (key, this.stringSetting (2, isJmolSet));
break;
case 545259568:
case 545259558:
if (this.slen > 2) this.setUnits (this.stringSetting (2, isJmolSet), tok);
break;
case 545259572:
if (!this.chk) this.viewer.setPicked (-1);
if (this.slen > 2) {
this.setPicking ();
return;
}break;
case 545259574:
if (this.slen > 2) {
this.setPickingStyle ();
return;
}break;
case 1716520985:
break;
case 553648168:
ival = this.intSetting (2);
if (!this.chk && ival != -2147483648) this.setIntProperty (key, this.scriptReportingLevel = ival);
break;
case 536870924:
ival = this.intSetting (2);
if (ival == -2147483648 || ival == 0 || ival == 1) {
justShow = false;
break;
}tok = 553648174;
key = "specularPercent";
this.setIntProperty (key, ival);
break;
case 1650071565:
tok = 553648178;
key = "strandCount";
this.setIntProperty (key, this.intSetting (2));
break;
default:
justShow = false;
}
if (justShow && !showing) return;
var isContextVariable = (!justShow && !isJmolSet && this.getContextVariableAsVariable (key) != null);
if (!justShow && !isContextVariable) {
switch (tok) {
case 1678770178:
newTok = 603979928;
break;
case 1613758470:
newTok = 603979908;
break;
case 1613758476:
newTok = 603979910;
break;
case 1610612739:
newTok = 603979879;
break;
case 1666189314:
newTok = 570425394;
this.setFloatProperty ("solventProbeRadius", this.floatSetting (2));
justShow = true;
break;
case 1610612740:
newTok = 570425390;
break;
case 1613758488:
newTok = 603979948;
break;
case 1766856708:
newTok = 545259545;
break;
case 1611141175:
sval = this.parameterAsString (2).toLowerCase ();
switch ("x;y;z;fps".indexOf (sval + ";")) {
case 0:
newTok = 570425398;
break;
case 2:
newTok = 570425400;
break;
case 4:
newTok = 570425402;
break;
case 6:
newTok = 570425396;
break;
default:
this.errorStr2 (50, "set SPIN ", sval);
}
if (!this.chk) this.viewer.setSpin (sval, Clazz.floatToInt (this.floatParameter (this.checkLast (3))));
justShow = true;
break;
}
}if (newTok != 0) {
key = J.script.T.nameOf (tok = newTok);
} else if (!justShow && !isContextVariable) {
if (key.length == 0 || key.charAt (0) == '_') this.error (56);
var lckey = key.toLowerCase ();
if (lckey.indexOf ("label") == 0 && J.util.Parser.isOneOf (key.substring (5).toLowerCase (), ";front;group;atom;offset;offsetexact;pointer;alignment;toggle;scalereference;")) {
if (this.setLabel (key.substring (5))) return;
}if (isJmolSet && lckey.indexOf ("shift_") == 0) {
var f = this.floatParameter (2);
this.checkLength (3);
if (!this.chk) this.viewer.getNMRCalculation ().setChemicalShiftReference (lckey.substring (6), f);
return;
}if (lckey.endsWith ("callback")) tok = 536870912;
}if (isJmolSet && !J.script.T.tokAttr (tok, 536870912)) {
this.iToken = 1;
if (!this.isStateScript) this.errorStr2 (50, "SET", key);
this.warning (51, "SET", key);
}if (!justShow && isJmolSet) {
switch (this.slen) {
case 2:
this.setBooleanProperty (key, true);
justShow = true;
break;
case 3:
if (ival != 2147483647) {
this.setIntProperty (key, ival);
justShow = true;
}break;
}
}if (!justShow && !isJmolSet && this.tokAt (2) == 1048587) {
if (!this.chk) this.viewer.removeUserVariable (key.toLowerCase ());
justShow = true;
}if (!justShow) {
var tok2 = (this.tokAt (1) == 1048577 ? 0 : this.tokAt (2));
var setType = this.st[0].intValue;
var pt = (tok2 == 269484436 ? 3 : setType == 61 && !key.equals ("return") && tok2 != 269484436 ? 0 : 2);
this.setVariable (pt, 0, key, setType);
if (!isJmolSet) return;
}if (showing) this.viewer.showParameter (key, true, 80);
}, $fz.isPrivate = true, $fz));
$_M(c$, "setZslab", 
($fz = function () {
var pt = null;
if (this.isFloatParameter (2)) {
this.checkLength (3);
this.setIntProperty ("zSlab", Clazz.floatToInt (this.floatParameter (2)));
} else {
if (!this.isCenterParameter (2)) this.invArg ();
pt = this.centerParameter (2);
this.checkLength (this.iToken + 1);
}if (!this.chk) this.viewer.setZslabPoint (pt);
}, $fz.isPrivate = true, $fz));
$_M(c$, "setBondmode", 
($fz = function () {
var bondmodeOr = false;
switch (this.getToken (this.checkLast (2)).tok) {
case 269484128:
break;
case 269484112:
bondmodeOr = true;
break;
default:
this.invArg ();
}
this.setBooleanProperty ("bondModeOr", bondmodeOr);
}, $fz.isPrivate = true, $fz));
$_M(c$, "setEcho", 
($fz = function () {
var propertyName = null;
var propertyValue = null;
var id = null;
var echoShapeActive = true;
var pt = 2;
switch (this.getToken (2).tok) {
case 1048588:
id = propertyName = "allOff";
this.checkLength (++pt);
break;
case 1048587:
echoShapeActive = false;
case 1048579:
id = this.parameterAsString (2);
this.checkLength (++pt);
break;
case 1073741996:
case 12289:
case 1073742128:
case 1074790748:
case 1073742019:
case 1073741871:
case 1073741824:
case 4:
case 1074790550:
if (this.theTok == 1074790550) pt++;
id = this.parameterAsString (pt++);
break;
}
if (!this.chk) {
this.viewer.setEchoStateActive (echoShapeActive);
this.sm.loadShape (30);
if (id != null) this.setShapeProperty (30, propertyName == null ? "target" : propertyName, id);
}if (pt < this.slen) {
switch (this.getToken (pt++).tok) {
case 1073741832:
propertyName = "align";
switch (this.getToken (pt).tok) {
case 1073741996:
case 1073742128:
case 12289:
propertyValue = this.parameterAsString (pt++);
break;
default:
this.invArg ();
}
break;
case 12289:
case 1073741996:
case 1073742128:
propertyName = "align";
propertyValue = this.parameterAsString (pt - 1);
break;
case 554176526:
propertyName = "%zpos";
propertyValue = Integer.$valueOf (Clazz.floatToInt (this.floatParameter (pt++)));
break;
case 1610625028:
case 3145768:
case 1048589:
propertyName = "hidden";
propertyValue = Boolean.FALSE;
break;
case 12294:
case 3145770:
propertyName = "hidden";
propertyValue = Boolean.TRUE;
break;
case 1095766028:
var modelIndex = (this.chk ? 0 : this.modelNumberParameter (pt++));
if (modelIndex >= this.viewer.getModelCount ()) this.invArg ();
propertyName = "model";
propertyValue = Integer.$valueOf (modelIndex);
break;
case 269484096:
case 1073742195:
propertyName = "xypos";
propertyValue = this.xypParameter (--pt);
if (propertyValue == null) pt--;
 else pt = this.iToken + 1;
break;
case 2:
pt--;
var posx = this.intParameter (pt++);
var namex = "xpos";
if (this.tokAt (pt) == 269484210) {
namex = "%xpos";
pt++;
}propertyName = "ypos";
propertyValue = Integer.$valueOf (this.intParameter (pt++));
if (this.tokAt (pt) == 269484210) {
propertyName = "%ypos";
pt++;
}this.checkLength (pt);
this.setShapeProperty (30, namex, Integer.$valueOf (posx));
break;
case 1048588:
propertyName = "off";
break;
case 1073742138:
propertyName = "scale";
propertyValue = Float.$valueOf (this.floatParameter (pt++));
break;
case 135271429:
propertyName = "script";
propertyValue = this.parameterAsString (pt++);
break;
case 4:
case 1073741979:
var isImage = (this.theTok == 1073741979);
if (isImage) pt++;
this.checkLength (pt);
if (id == null && isImage) {
var data =  new Array (1);
this.getShapePropertyData (30, "currentTarget", data);
id = data[0];
}this.echo (pt - 1, id, isImage);
return;
case 135266320:
propertyName = "point";
propertyValue = (this.isCenterParameter (pt) ? this.centerParameter (pt) : null);
pt = this.iToken + 1;
break;
default:
if (this.isCenterParameter (pt - 1)) {
propertyName = "xyz";
propertyValue = this.centerParameter (pt - 1);
pt = this.iToken + 1;
break;
}this.invArg ();
}
}this.checkLength (pt);
if (!this.chk && propertyName != null) this.setShapeProperty (30, propertyName, propertyValue);
}, $fz.isPrivate = true, $fz));
$_M(c$, "intSetting", 
($fz = function (pt) {
if (pt == this.slen) return -2147483648;
return this.parameterExpressionToken (pt).asInt ();
}, $fz.isPrivate = true, $fz), "~N");
$_M(c$, "floatSetting", 
($fz = function (pt) {
if (pt == this.slen) return NaN;
return J.script.SV.fValue (this.parameterExpressionToken (pt));
}, $fz.isPrivate = true, $fz), "~N");
$_M(c$, "stringSetting", 
($fz = function (pt, isJmolSet) {
if (isJmolSet && this.slen == pt + 1) return this.parameterAsString (pt);
return this.parameterExpressionToken (pt).asString ();
}, $fz.isPrivate = true, $fz), "~N,~B");
$_M(c$, "setLabel", 
($fz = function (str) {
this.sm.loadShape (5);
var propertyValue = null;
this.setShapeProperty (5, "setDefaults", this.viewer.getNoneSelected ());
while (true) {
if (str.equals ("scalereference")) {
var scaleAngstromsPerPixel = this.floatParameter (2);
if (scaleAngstromsPerPixel >= 5) scaleAngstromsPerPixel = this.viewer.getZoomSetting () / scaleAngstromsPerPixel / this.viewer.getScalePixelsPerAngstrom (false);
propertyValue = Float.$valueOf (scaleAngstromsPerPixel);
break;
}if (str.equals ("offset") || str.equals ("offsetexact")) {
if (this.isPoint3f (2)) {
var pt = this.getPoint3f (2, false);
propertyValue = [1, pt.x, pt.y, pt.z, 0, 0, 0];
} else if (this.isArrayParameter (2)) {
propertyValue = this.floatParameterSet (2, 7, 7);
} else {
var xOffset = this.intParameterRange (2, -127, 127);
var yOffset = this.intParameterRange (3, -127, 127);
propertyValue = Integer.$valueOf (J.modelset.Object2d.getOffset (xOffset, yOffset));
}break;
}if (str.equals ("alignment")) {
switch (this.getToken (2).tok) {
case 1073741996:
case 1073742128:
case 12289:
str = "align";
propertyValue = this.theToken.value;
break;
default:
this.invArg ();
}
break;
}if (str.equals ("pointer")) {
var flags = 0;
switch (this.getToken (2).tok) {
case 1048588:
case 1048587:
break;
case 1610616835:
flags |= 2;
case 1048589:
flags |= 1;
break;
default:
this.invArg ();
}
propertyValue = Integer.$valueOf (flags);
break;
}if (str.equals ("toggle")) {
this.iToken = 1;
var bs = (this.slen == 2 ? null : this.atomExpressionAt (2));
this.checkLast (this.iToken);
if (!this.chk) this.viewer.togglePickingLabel (bs);
return true;
}this.iToken = 1;
var TF = (this.slen == 2 || this.getToken (2).tok == 1048589);
if (str.equals ("front") || str.equals ("group")) {
if (!TF && this.tokAt (2) != 1048588) this.invArg ();
if (!TF) str = "front";
propertyValue = (TF ? Boolean.TRUE : Boolean.FALSE);
break;
}if (str.equals ("atom")) {
if (!TF && this.tokAt (2) != 1048588) this.invArg ();
str = "front";
propertyValue = (TF ? Boolean.FALSE : Boolean.TRUE);
break;
}return false;
}
var bs = (this.iToken + 1 < this.slen ? this.atomExpressionAt (++this.iToken) : null);
this.checkLast (this.iToken);
if (this.chk) return true;
if (bs == null) this.setShapeProperty (5, str, propertyValue);
 else this.setShapePropertyBs (5, str, propertyValue, bs);
return true;
}, $fz.isPrivate = true, $fz), "~S");
$_M(c$, "setMonitor", 
($fz = function () {
var tok = this.tokAt (this.checkLast (2));
switch (tok) {
case 1048589:
case 1048588:
this.setBooleanProperty ("measurementlabels", tok == 1048589);
return;
case 1073741926:
case 2:
case 3:
this.setShapeSizeBs (6, this.getSetAxesTypeMad (2), null);
return;
}
this.setUnits (this.parameterAsString (2), 545259568);
}, $fz.isPrivate = true, $fz));
$_M(c$, "setUnits", 
($fz = function (units, tok) {
if (tok == 545259568 && (units.endsWith ("hz") || J.util.Parser.isOneOf (units.toLowerCase (), ";angstroms;au;bohr;nanometers;nm;picometers;pm;vanderwaals;vdw;"))) {
if (!this.chk) this.viewer.setUnits (units, true);
} else if (tok == 545259558 && J.util.Parser.isOneOf (units.toLowerCase (), ";kcal;kj;")) {
if (!this.chk) this.viewer.setUnits (units, false);
} else {
this.errorStr2 (50, "set " + J.script.T.nameOf (tok), units);
}return true;
}, $fz.isPrivate = true, $fz), "~S,~N");
$_M(c$, "setSsbond", 
($fz = function () {
var ssbondsBackbone = false;
switch (this.tokAt (this.checkLast (2))) {
case 1115297793:
ssbondsBackbone = true;
break;
case 3145754:
break;
default:
this.invArg ();
}
this.setBooleanProperty ("ssbondsBackbone", ssbondsBackbone);
}, $fz.isPrivate = true, $fz));
$_M(c$, "setHbond", 
($fz = function () {
var bool = false;
switch (this.tokAt (this.checkLast (2))) {
case 1115297793:
bool = true;
case 3145754:
this.setBooleanProperty ("hbondsBackbone", bool);
break;
case 1073742150:
bool = true;
case 1073741926:
this.setBooleanProperty ("hbondsSolid", bool);
break;
default:
this.invArg ();
}
}, $fz.isPrivate = true, $fz));
$_M(c$, "setPicking", 
($fz = function () {
if (this.slen == 2) {
this.setStringProperty ("picking", "identify");
return;
}if (this.slen > 4 || this.tokAt (2) == 4) {
this.setStringProperty ("picking", this.stringSetting (2, false));
return;
}var i = 2;
var type = "SELECT";
switch (this.getToken (2).tok) {
case 135280132:
case 1746538509:
case 1611141175:
if (this.checkLength34 () == 4) {
type = this.parameterAsString (2).toUpperCase ();
if (type.equals ("SPIN")) this.setIntProperty ("pickingSpinRate", this.intParameter (3));
 else i = 3;
}break;
case 12291:
break;
default:
this.checkLength (3);
}
var str = this.parameterAsString (i);
switch (this.getToken (i).tok) {
case 1048589:
case 1073742056:
str = "identify";
break;
case 1048588:
case 1048587:
str = "off";
break;
case 135280132:
str = "atom";
break;
case 1826248715:
str = "label";
break;
case 1678770178:
str = "bond";
break;
case 12291:
this.checkLength (4);
if (this.tokAt (3) != 1678770178) this.invArg ();
str = "deleteBond";
break;
}
var mode = ((mode = str.indexOf ("_")) >= 0 ? mode : str.length);
mode = J.viewer.ActionManager.getPickingMode (str.substring (0, mode));
if (mode < 0) this.errorStr2 (50, "SET PICKING " + type, str);
this.setStringProperty ("picking", str);
}, $fz.isPrivate = true, $fz));
$_M(c$, "setPickingStyle", 
($fz = function () {
if (this.slen > 4 || this.tokAt (2) == 4) {
this.setStringProperty ("pickingStyle", this.stringSetting (2, false));
return;
}var i = 2;
var isMeasure = false;
var type = "SELECT";
switch (this.getToken (2).tok) {
case 1746538509:
isMeasure = true;
type = "MEASURE";
case 135280132:
if (this.checkLength34 () == 4) i = 3;
break;
default:
this.checkLength (3);
}
var str = this.parameterAsString (i);
switch (this.getToken (i).tok) {
case 1048587:
case 1048588:
str = (isMeasure ? "measureoff" : "toggle");
break;
case 1048589:
if (isMeasure) str = "measure";
break;
}
if (J.viewer.ActionManager.getPickingStyleIndex (str) < 0) this.errorStr2 (50, "SET PICKINGSTYLE " + type, str);
this.setStringProperty ("pickingStyle", str);
}, $fz.isPrivate = true, $fz));
$_M(c$, "timeout", 
($fz = function (index) {
var name = null;
var script = null;
var mSec = 0;
if (this.slen == index) {
this.showString (this.viewer.showTimeout (null));
return;
}for (var i = index; i < this.slen; i++) switch (this.getToken (i).tok) {
case 1074790550:
name = this.parameterAsString (++i);
if (this.slen == 3) {
if (!this.chk) this.viewer.triggerTimeout (name);
return;
}break;
case 1048588:
break;
case 2:
mSec = this.intParameter (i);
break;
case 3:
mSec = Math.round (this.floatParameter (i) * 1000);
break;
default:
if (name == null) name = this.parameterAsString (i);
 else if (script == null) script = this.parameterAsString (i);
 else this.invArg ();
break;
}

if (!this.chk) this.viewer.setTimeout (name, mSec, script);
}, $fz.isPrivate = true, $fz), "~N");
$_M(c$, "setUserColors", 
($fz = function () {
var v =  new J.util.JmolList ();
for (var i = 2; i < this.slen; i++) {
var argb = this.getArgbParam (i);
v.addLast (Integer.$valueOf (argb));
i = this.iToken;
}
if (this.chk) return;
var n = v.size ();
var scale =  Clazz.newIntArray (n, 0);
for (var i = n; --i >= 0; ) scale[i] = v.get (i).intValue ();

this.viewer.setUserScale (scale);
}, $fz.isPrivate = true, $fz));
$_M(c$, "setVariable", 
($fz = function (pt, ptMax, key, setType) {
var bs = null;
var propertyName = "";
var tokProperty = 0;
var isArrayItem = (setType == 91);
var settingProperty = false;
var isExpression = false;
var settingData = (key.startsWith ("property_"));
var t = (settingData ? null : this.getContextVariableAsVariable (key));
var isUserVariable = (t != null);
if (pt > 0 && this.tokAt (pt - 1) == 1048577) {
bs = this.atomExpressionAt (pt - 1);
pt = this.iToken + 1;
isExpression = true;
}if (this.tokAt (pt) == 1048584) {
settingProperty = true;
var token = this.getBitsetPropertySelector (++pt, true);
if (token == null) this.invArg ();
if (this.tokAt (++pt) != 269484436) this.invArg ();
pt++;
tokProperty = token.intValue;
propertyName = token.value;
}if (isExpression && !settingProperty) this.invArg ();
var v = this.parameterExpression (pt, ptMax, key, true, true, -1, isArrayItem, null, null);
var nv = v.size ();
if (nv == 0 || !isArrayItem && nv > 1 || isArrayItem && (nv < 3 || nv % 2 != 1)) this.invArg ();
if (this.chk) return;
var tv = v.get (isArrayItem ? v.size () - 1 : 0);
var needVariable = (!isUserVariable && !isExpression && !settingData && (isArrayItem || settingProperty || !(Clazz.instanceOf (tv.value, String) || tv.tok == 2 || Clazz.instanceOf (tv.value, Integer) || Clazz.instanceOf (tv.value, Float) || Clazz.instanceOf (tv.value, Boolean))));
if (needVariable) {
if (key.startsWith ("_")) this.errorStr (22, key);
t = this.viewer.getOrSetNewVariable (key, true);
isUserVariable = true;
}if (isArrayItem) {
var tnew = (J.script.SV.newVariable (4, "")).setv (tv, false);
var nParam = Clazz.doubleToInt (v.size () / 2);
for (var i = 0; i < nParam; i++) {
var isLast = (i + 1 == nParam);
var vv = v.get (i * 2);
if (t.tok == 10) {
t.tok = 6;
t.value =  new java.util.Hashtable ();
}if (t.tok == 6) {
var hkey = vv.asString ();
var tmap = t.value;
if (isLast) {
tmap.put (hkey, tnew);
break;
}t = tmap.get (hkey);
} else {
var ipt = vv.asInt ();
if (t.tok == 7) t = J.script.SV.selectItemVar (t);
switch (t.tok) {
case 7:
var list = t.getList ();
if (ipt > list.size () || isLast) break;
if (ipt <= 0) ipt = list.size () + ipt;
if (--ipt < 0) ipt = 0;
t = list.get (ipt);
continue;
case 11:
case 12:
var dim = (t.tok == 11 ? 3 : 4);
if (nParam == 1 && Math.abs (ipt) >= 1 && Math.abs (ipt) <= dim && tnew.tok == 7 && tnew.getList ().size () == dim) break;
if (nParam == 2) {
var ipt2 = v.get (2).asInt ();
if (ipt2 >= 1 && ipt2 <= dim && (tnew.tok == 2 || tnew.tok == 3)) {
i++;
ipt = ipt * 10 + ipt2;
break;
}}t.toArray ();
--i;
continue;
}
t.setSelectedValue (ipt, tnew);
break;
}}
return;
}if (settingProperty) {
if (!isExpression) {
bs = J.script.SV.getBitSet (t, true);
if (bs == null) this.invArg ();
}if (propertyName.startsWith ("property_")) {
this.viewer.setData (propertyName, [propertyName, (tv.tok == 7 ? J.script.SV.flistValue (tv, (tv.value).size () == bs.cardinality () ? bs.cardinality () : this.viewer.getAtomCount ()) : tv.asString ()), J.util.BSUtil.copy (bs), Integer.$valueOf (tv.tok == 7 ? 1 : 0), Boolean.FALSE], this.viewer.getAtomCount (), 0, 0, tv.tok == 7 ? 2147483647 : -2147483648, 0);
return;
}this.setBitsetProperty (bs, tokProperty, tv.asInt (), tv.asFloat (), tv);
return;
}if (isUserVariable) {
t.setv (tv, false);
return;
}var vv = J.script.SV.oValue (tv);
if (key.startsWith ("property_")) {
if (tv.tok == 7) vv = tv.asString ();
this.viewer.setData (key, [key, "" + vv, J.util.BSUtil.copy (this.viewer.getSelectionSet (false)), Integer.$valueOf (0)], this.viewer.getAtomCount (), 0, 0, -2147483648, 0);
return;
}if (Clazz.instanceOf (vv, Boolean)) {
this.setBooleanProperty (key, (vv).booleanValue ());
} else if (Clazz.instanceOf (vv, Integer)) {
this.setIntProperty (key, (vv).intValue ());
} else if (Clazz.instanceOf (vv, Float)) {
this.setFloatProperty (key, (vv).floatValue ());
} else if (Clazz.instanceOf (vv, String)) {
this.setStringProperty (key, vv);
} else if (Clazz.instanceOf (vv, J.modelset.Bond.BondSet)) {
this.setStringProperty (key, J.util.Escape.eBond (vv));
} else if (Clazz.instanceOf (vv, J.util.BS) || Clazz.instanceOf (vv, J.util.P3) || Clazz.instanceOf (vv, J.util.P4)) {
this.setStringProperty (key, J.util.Escape.e (vv));
} else {
J.util.Logger.error ("ERROR -- return from propertyExpression was " + vv);
}}, $fz.isPrivate = true, $fz), "~N,~N,~S,~N");
$_M(c$, "axes", 
($fz = function (index) {
var tickInfo = this.checkTicks (index, true, true, false);
index = this.iToken + 1;
var tok = this.tokAt (index);
var type = this.optParameterAsString (index).toLowerCase ();
if (this.slen == index + 1 && J.util.Parser.isOneOf (type, ";window;unitcell;molecular;")) {
this.setBooleanProperty ("axes" + type, true);
return;
}switch (tok) {
case 12289:
var center = this.centerParameter (index + 1);
this.setShapeProperty (31, "origin", center);
this.checkLast (this.iToken);
return;
case 1073742138:
this.setFloatProperty ("axesScale", this.floatParameter (this.checkLast (++index)));
return;
case 1826248715:
switch (tok = this.tokAt (index + 1)) {
case 1048588:
case 1048589:
this.checkLength (index + 2);
this.setShapeProperty (31, "labels" + (tok == 1048589 ? "On" : "Off"), null);
return;
}
var sOrigin = null;
switch (this.slen - index) {
case 7:
this.setShapeProperty (31, "labels", [this.parameterAsString (++index), this.parameterAsString (++index), this.parameterAsString (++index), this.parameterAsString (++index), this.parameterAsString (++index), this.parameterAsString (++index)]);
break;
case 5:
sOrigin = this.parameterAsString (index + 4);
case 4:
this.setShapeProperty (31, "labels", [this.parameterAsString (++index), this.parameterAsString (++index), this.parameterAsString (++index), sOrigin]);
break;
default:
this.error (2);
}
return;
}
if (type.equals ("position")) {
var xyp;
if (this.tokAt (++index) == 1048588) {
xyp =  new J.util.P3 ();
} else {
xyp = this.xypParameter (index);
if (xyp == null) this.invArg ();
index = this.iToken;
}this.setShapeProperty (31, "position", xyp);
return;
}var mad = this.getSetAxesTypeMad (index);
if (this.chk) return;
this.setObjectMad (31, "axes", mad);
if (tickInfo != null) this.setShapeProperty (31, "tickInfo", tickInfo);
}, $fz.isPrivate = true, $fz), "~N");
$_M(c$, "boundbox", 
($fz = function (index) {
var tickInfo = this.checkTicks (index, false, true, false);
index = this.iToken + 1;
var scale = 1;
if (this.tokAt (index) == 1073742138) {
scale = this.floatParameter (++index);
if (!this.chk && scale == 0) this.invArg ();
index++;
if (index == this.slen) {
if (!this.chk) this.viewer.setBoundBox (null, null, true, scale);
return;
}}var byCorner = (this.tokAt (index) == 1073741902);
if (byCorner) index++;
if (this.isCenterParameter (index)) {
this.expressionResult = null;
var index0 = index;
var pt1 = this.centerParameter (index);
index = this.iToken + 1;
if (byCorner || this.isCenterParameter (index)) {
var pt2 = (byCorner ? this.centerParameter (index) : this.getPoint3f (index, true));
index = this.iToken + 1;
if (!this.chk) this.viewer.setBoundBox (pt1, pt2, byCorner, scale);
} else if (this.expressionResult != null && Clazz.instanceOf (this.expressionResult, J.util.BS)) {
if (!this.chk) this.viewer.calcBoundBoxDimensions (this.expressionResult, scale);
} else if (this.expressionResult == null && this.tokAt (index0) == 1048583) {
if (this.chk) return;
var bbox = this.getObjectBoundingBox (this.objectNameParameter (++index0));
if (bbox == null) this.invArg ();
this.viewer.setBoundBox (bbox[0], bbox[1], true, scale);
index = this.iToken + 1;
} else {
this.invArg ();
}if (index == this.slen) return;
}var mad = this.getSetAxesTypeMad (index);
if (this.chk) return;
if (tickInfo != null) this.setShapeProperty (32, "tickInfo", tickInfo);
this.setObjectMad (32, "boundbox", mad);
}, $fz.isPrivate = true, $fz), "~N");
$_M(c$, "checkTicks", 
($fz = function (index, allowUnitCell, allowScale, allowFirst) {
this.iToken = index - 1;
if (this.tokAt (index) != 1073742164) return null;
var tickInfo;
var str = " ";
switch (this.tokAt (index + 1)) {
case 1112541205:
case 1112541206:
case 1112541207:
str = this.parameterAsString (++index).toLowerCase ();
break;
case 1073741824:
this.invArg ();
}
if (this.tokAt (++index) == 1048587) {
tickInfo =  new J.modelset.TickInfo (null);
tickInfo.type = str;
this.iToken = index;
return tickInfo;
}tickInfo =  new J.modelset.TickInfo (this.getPointOrPlane (index, false, true, false, false, 3, 3));
if (this.coordinatesAreFractional || this.tokAt (this.iToken + 1) == 1614417948) {
tickInfo.scale = J.util.P3.new3 (NaN, NaN, NaN);
allowScale = false;
}if (this.tokAt (this.iToken + 1) == 1614417948) this.iToken++;
tickInfo.type = str;
if (this.tokAt (this.iToken + 1) == 1288701960) tickInfo.tickLabelFormats = this.stringParameterSet (this.iToken + 2);
if (!allowScale) return tickInfo;
if (this.tokAt (this.iToken + 1) == 1073742138) {
if (this.isFloatParameter (this.iToken + 2)) {
var f = this.floatParameter (this.iToken + 2);
tickInfo.scale = J.util.P3.new3 (f, f, f);
} else {
tickInfo.scale = this.getPoint3f (this.iToken + 2, true);
}}if (allowFirst) if (this.tokAt (this.iToken + 1) == 1073741942) tickInfo.first = this.floatParameter (this.iToken + 2);
return tickInfo;
}, $fz.isPrivate = true, $fz), "~N,~B,~B,~B");
$_M(c$, "unitcell", 
($fz = function (index) {
var icell = 2147483647;
var mad = 2147483647;
var pt = null;
var tickInfo = this.checkTicks (index, true, false, false);
index = this.iToken;
var id = null;
var points = null;
switch (this.tokAt (index + 1)) {
case 4:
id = this.objectNameParameter (++index);
break;
case 1048583:
index++;
id = this.objectNameParameter (++index);
break;
case 12289:
++index;
switch (this.tokAt (++index)) {
case 10:
case 1048577:
pt = J.util.P3.newP (this.viewer.getAtomSetCenter (this.atomExpressionAt (index)));
this.viewer.toFractional (pt, true);
index = this.iToken;
break;
default:
if (this.isCenterParameter (index)) {
pt = this.centerParameter (index);
index = this.iToken;
break;
}this.invArg ();
}
pt.x -= 0.5;
pt.y -= 0.5;
pt.z -= 0.5;
break;
default:
if (this.isArrayParameter (index + 1)) {
points = this.getPointArray (++index, 4);
index = this.iToken;
} else if (this.slen == index + 2) {
if (this.getToken (index + 1).tok == 2 && this.intParameter (index + 1) >= 111) icell = this.intParameter (++index);
} else if (this.slen > index + 1) {
pt = this.getPointOrPlane (++index, false, true, false, true, 3, 3);
index = this.iToken;
}}
mad = this.getSetAxesTypeMad (++index);
this.checkLast (this.iToken);
if (this.chk) return;
if (icell != 2147483647) this.viewer.setCurrentUnitCellOffset (icell);
 else if (id != null) this.viewer.setCurrentCage (id);
 else if (points != null) this.viewer.setCurrentCagePts (points);
this.setObjectMad (33, "unitCell", mad);
if (pt != null) this.viewer.setCurrentUnitCellOffsetPt (pt);
if (tickInfo != null) this.setShapeProperty (33, "tickInfo", tickInfo);
}, $fz.isPrivate = true, $fz), "~N");
$_M(c$, "frank", 
($fz = function (index) {
this.setBooleanProperty ("frank", this.booleanParameter (index));
}, $fz.isPrivate = true, $fz), "~N");
$_M(c$, "selectionHalo", 
($fz = function (pt) {
var showHalo = false;
switch (pt == this.slen ? 1048589 : this.getToken (pt).tok) {
case 1048589:
case 1114638363:
showHalo = true;
case 1048588:
case 1048587:
case 1073742056:
this.setBooleanProperty ("selectionHalos", showHalo);
break;
default:
this.invArg ();
}
}, $fz.isPrivate = true, $fz), "~N");
$_M(c$, "save", 
($fz = function () {
if (this.slen > 1) {
var saveName = this.optParameterAsString (2);
switch (this.tokAt (1)) {
case 1073742077:
case 1073742132:
if (!this.chk) this.viewer.saveOrientation (saveName, null);
return;
case 1678770178:
if (!this.chk) this.viewer.saveBonds (saveName);
return;
case 1073742158:
if (!this.chk) this.viewer.saveState (saveName);
return;
case 1641025539:
if (!this.chk) this.viewer.saveStructure (saveName);
return;
case 1048582:
if (!this.chk) this.viewer.saveCoordinates (saveName, this.viewer.getSelectionSet (false));
return;
case 1073742140:
if (!this.chk) this.viewer.saveSelection (saveName);
return;
}
}this.errorStr2 (53, "SAVE", "bonds? coordinates? orientation? selection? state? structure?");
}, $fz.isPrivate = true, $fz));
$_M(c$, "restore", 
($fz = function () {
if (this.slen > 1) {
var saveName = this.optParameterAsString (2);
var tok = this.tokAt (1);
switch (tok) {
case 1073742077:
case 1073742132:
case 1073742139:
var floatSecondsTotal = (this.slen > 3 ? this.floatParameter (3) : 0);
if (floatSecondsTotal < 0) this.invArg ();
if (this.chk) return;
var type = "";
switch (tok) {
case 1073742077:
type = "Orientation";
this.viewer.restoreOrientation (saveName, floatSecondsTotal);
break;
case 1073742132:
type = "Rotation";
this.viewer.restoreRotation (saveName, floatSecondsTotal);
break;
case 1073742139:
type = "Scene";
this.viewer.restoreScene (saveName, floatSecondsTotal);
break;
}
if (this.isJS && floatSecondsTotal > 0 && this.viewer.global.waitForMoveTo) throw  new J.script.ScriptInterruption (this, "restore" + type, 1);
return;
}
this.checkLength23 ();
switch (tok) {
case 1678770178:
if (!this.chk) this.viewer.restoreBonds (saveName);
return;
case 1048582:
if (this.chk) return;
var script = this.viewer.getSavedCoordinates (saveName);
if (script == null) this.invArg ();
this.runScript (script);
this.viewer.checkCoordinatesChanged ();
return;
case 1073742140:
if (!this.chk) this.viewer.restoreSelection (saveName);
return;
case 1073742158:
if (this.chk) return;
var state = this.viewer.getSavedState (saveName);
if (state == null) this.invArg ();
this.runScript (state);
return;
case 1641025539:
if (this.chk) return;
var shape = this.viewer.getSavedStructure (saveName);
if (shape == null) this.invArg ();
this.runScript (shape);
return;
}
}this.errorStr2 (53, "RESTORE", "bonds? coords? orientation? rotation? scene? selection? state? structure?");
}, $fz.isPrivate = true, $fz));
$_M(c$, "write", 
function (args) {
var pt = 0;
var pt0 = 0;
var isCommand;
var isShow;
if (args == null) {
args = this.st;
pt = pt0 = 1;
isCommand = true;
isShow = (this.viewer.isApplet () && !this.viewer.isSignedApplet () || !this.viewer.isRestricted (J.viewer.Viewer.ACCESS.ALL) || this.viewer.getPathForAllFiles ().length > 0);
} else {
isCommand = false;
isShow = true;
}var argCount = (isCommand ? this.slen : args.length);
var len = 0;
var nVibes = 0;
var width = -1;
var height = -1;
var quality = -2147483648;
var timeMsg = this.viewer.getBoolean (603979934);
var driverList = this.viewer.getExportDriverList ();
var sceneType = "PNGJ";
var data = null;
var type2 = "";
var fileName = null;
var localPath = null;
var remotePath = null;
var val = null;
var msg = null;
var fullPath =  new Array (1);
var isCoord = false;
var isExport = false;
var isImage = false;
var bsFrames = null;
var scripts = null;
var type = "SPT";
var tok = (isCommand && args.length == 1 ? 1073741884 : J.script.ScriptEvaluator.tokAtArray (pt, args));
switch (tok) {
case 0:
break;
case 135271429:
if (this.isArrayParameter (pt + 1)) {
scripts = this.stringParameterSet (++pt);
localPath = ".";
remotePath = ".";
pt0 = pt = this.iToken + 1;
tok = this.tokAt (pt);
}break;
default:
type = J.script.SV.sValue (this.tokenAt (pt, args)).toUpperCase ();
}
switch (tok) {
case 0:
break;
case 135270417:
case 1052714:
case 1716520985:
msg = this.getExtension ().plot (args);
if (!isCommand) return msg;
break;
case 1073741983:
type = "INLINE";
data = J.script.SV.sValue (this.tokenAt (++pt, args));
pt++;
break;
case 1073742102:
type = "PGRP";
pt++;
type2 = J.script.SV.sValue (this.tokenAt (pt, args)).toLowerCase ();
if (type2.equals ("draw")) pt++;
break;
case 1048582:
pt++;
isCoord = true;
break;
case 1073742158:
case 135271429:
val = J.script.SV.sValue (this.tokenAt (++pt, args)).toLowerCase ();
while (val.equals ("localpath") || val.equals ("remotepath")) {
if (val.equals ("localpath")) localPath = J.script.SV.sValue (this.tokenAt (++pt, args));
 else remotePath = J.script.SV.sValue (this.tokenAt (++pt, args));
val = J.script.SV.sValue (this.tokenAt (++pt, args)).toLowerCase ();
}
type = "SPT";
break;
case 1229984263:
case 135368713:
case 1610616855:
case 135180:
case 1073742015:
case 1073742018:
case 1183762:
case 135188:
pt++;
break;
case 1073741992:
type = "ZIPALL";
pt++;
break;
case 36868:
type = "VAR";
pt += 2;
break;
case 4115:
case 1073741824:
case 1073741979:
case 1073742139:
case 4:
case 4166:
switch (tok) {
case 1073741979:
pt++;
break;
case 4166:
nVibes = this.intParameterRange (++pt, 1, 10);
if (!this.chk) {
this.viewer.setVibrationOff ();
if (!this.isJS) this.delayScript (100);
}pt++;
break;
case 4115:
var bsAtoms;
if (pt + 1 < argCount && args[++pt].tok == 1048577 || args[pt].tok == 10) {
bsAtoms = this.atomExpression (args, pt, 0, true, false, true, true);
pt = this.iToken + 1;
} else {
bsAtoms = this.viewer.getModelUndeletedAtomsBitSet (-1);
}if (!this.chk) bsFrames = this.viewer.getModelBitSet (bsAtoms, true);
break;
case 1073742139:
val = J.script.SV.sValue (this.tokenAt (++pt, args)).toUpperCase ();
if (J.util.Parser.isOneOf (val, ";PNG;PNGJ;")) {
sceneType = val;
pt++;
}break;
default:
tok = 1073741979;
break;
}
if (tok == 1073741979) {
var t = J.script.T.getTokenFromName (J.script.SV.sValue (args[pt]).toLowerCase ());
if (t != null) type = J.script.SV.sValue (t).toUpperCase ();
if (J.util.Parser.isOneOf (type, driverList.toUpperCase ())) {
pt++;
type = type.substring (0, 1).toUpperCase () + type.substring (1).toLowerCase ();
isExport = true;
if (isCommand) fileName = "Jmol." + type.toLowerCase ();
break;
} else if (type.equals ("ZIP") || type.equals ("ZIPALL")) {
pt++;
break;
} else {
type = "(image)";
}}if (J.script.ScriptEvaluator.tokAtArray (pt, args) == 2) {
width = J.script.SV.iValue (this.tokenAt (pt++, args));
height = J.script.SV.iValue (this.tokenAt (pt++, args));
}break;
}
if (msg == null) {
val = J.script.SV.sValue (this.tokenAt (pt, args));
if (val.equalsIgnoreCase ("clipboard")) {
if (this.chk) return "";
} else if (J.util.Parser.isOneOf (val.toLowerCase (), ";jpg;jpeg;jpg64;jpeg64;gif;ppm;png;pngj;pngt;")) {
if (J.script.ScriptEvaluator.tokAtArray (pt + 1, args) == 2 && J.script.ScriptEvaluator.tokAtArray (pt + 2, args) == 2) {
width = J.script.SV.iValue (this.tokenAt (++pt, args));
height = J.script.SV.iValue (this.tokenAt (++pt, args));
}if (J.script.ScriptEvaluator.tokAtArray (pt + 1, args) == 2) quality = J.script.SV.iValue (this.tokenAt (++pt, args));
} else if (J.util.Parser.isOneOf (val.toLowerCase (), ";xyz;xyzrn;xyzvib;mol;sdf;v2000;v3000;cd;pdb;pqr;cml;")) {
type = val.toUpperCase ();
if (pt + 1 == argCount) pt++;
}if (type.equals ("(image)") && J.util.Parser.isOneOf (val.toLowerCase (), ";jpg;jpeg;jpg64;jpeg64;gif;ppm;png;pngj;pngt;scene;")) {
type = val.toUpperCase ();
pt++;
}if (pt + 2 == argCount) {
data = J.script.SV.sValue (this.tokenAt (++pt, args));
if (data.length > 0 && data.charAt (0) != '.') type = val.toUpperCase ();
}switch (J.script.ScriptEvaluator.tokAtArray (pt, args)) {
case 0:
isShow = true;
break;
case 1073741884:
break;
case 1073741824:
case 4:
fileName = J.script.SV.sValue (this.tokenAt (pt, args));
if (pt == argCount - 3 && J.script.ScriptEvaluator.tokAtArray (pt + 1, args) == 1048584) {
fileName += "." + J.script.SV.sValue (this.tokenAt (pt + 2, args));
}if (type !== "VAR" && pt == pt0) type = "IMAGE";
 else if (fileName.length > 0 && fileName.charAt (0) == '.' && (pt == pt0 + 1 || pt == pt0 + 2)) {
fileName = J.script.SV.sValue (this.tokenAt (pt - 1, args)) + fileName;
if (type !== "VAR" && pt == pt0 + 1) type = "IMAGE";
}if (fileName.equalsIgnoreCase ("clipboard") || !this.viewer.isRestricted (J.viewer.Viewer.ACCESS.ALL)) fileName = null;
break;
default:
this.invArg ();
}
if (type.equals ("IMAGE") || type.equals ("(image)") || type.equals ("FRAME") || type.equals ("VIBRATION")) {
type = (fileName != null && fileName.indexOf (".") >= 0 ? fileName.substring (fileName.lastIndexOf (".") + 1).toUpperCase () : "JPG");
}if (type.equals ("MNU")) {
type = "MENU";
} else if (type.equals ("WRL") || type.equals ("VRML")) {
type = "Vrml";
isExport = true;
} else if (type.equals ("X3D")) {
type = "X3d";
isExport = true;
} else if (type.equals ("IDTF")) {
type = "Idtf";
isExport = true;
} else if (type.equals ("MA")) {
type = "Maya";
isExport = true;
} else if (type.equals ("JS")) {
type = "Js";
isExport = true;
} else if (type.equals ("OBJ")) {
type = "Obj";
isExport = true;
} else if (type.equals ("JVXL")) {
type = "ISOSURFACE";
} else if (type.equals ("XJVXL")) {
type = "ISOSURFACE";
} else if (type.equals ("JMOL")) {
type = "ZIPALL";
} else if (type.equals ("HIS")) {
type = "HISTORY";
}if (type.equals ("COORD")) type = (fileName != null && fileName.indexOf (".") >= 0 ? fileName.substring (fileName.lastIndexOf (".") + 1).toUpperCase () : "XYZ");
isImage = J.util.Parser.isOneOf (type.toLowerCase (), ";jpg;jpeg;jpg64;jpeg64;gif;ppm;png;pngj;pngt;scene;");
if (scripts != null) {
if (type.equals ("PNG")) type = "PNGJ";
if (!type.equals ("PNGJ") && !type.equals ("ZIPALL")) this.invArg ();
}if (isImage && isShow) type = "JPG64";
 else if (!isImage && !isExport && !J.util.Parser.isOneOf (type, ";SCENE;JMOL;ZIP;ZIPALL;SPT;HISTORY;MO;ISOSURFACE;MESH;PMESH;VAR;FILE;FUNCTION;CD;CML;XYZ;XYZRN;XYZVIB;MENU;MOL;PDB;PGRP;PQR;QUAT;RAMA;SDF;V2000;V3000;INLINE;")) this.errorStr2 (54, "COORDS|FILE|FUNCTIONS|HISTORY|IMAGE|INLINE|ISOSURFACE|JMOL|MENU|MO|POINTGROUP|QUATERNION [w,x,y,z] [derivative]|RAMACHANDRAN|SPT|STATE|VAR x|ZIP|ZIPALL  CLIPBOARD", "CML|GIF|JPG|JPG64|JMOL|JVXL|MESH|MOL|PDB|PMESH|PNG|PNGJ|PNGT|PPM|PQR|SDF|V2000|V3000|SPT|XJVXL|XYZ|XYZRN|XYZVIB|ZIP" + driverList.toUpperCase ().$replace (';', '|'));
if (this.chk) return "";
var bytes = null;
var doDefer = false;
if (data == null || isExport) {
data = type.intern ();
if (isExport) {
fullPath[0] = fileName;
if (timeMsg) J.util.Logger.startTimer ("export");
data = this.viewer.generateOutputForExport (data, isCommand || fileName != null ? fullPath : null, width, height);
if (data == null || data.length == 0) return "";
if (!isCommand) return data;
if ((type.equals ("Povray") || type.equals ("Idtf")) && fullPath[0] != null) {
var ext = (type.equals ("Idtf") ? ".tex" : ".ini");
fileName = fullPath[0] + ext;
msg = this.viewer.createImageSet (fileName, ext, data, null, null, -2147483648, 0, 0, null, 0, fullPath);
if (type.equals ("Idtf")) data = data.substring (0, data.indexOf ("\\begin{comment}"));
data = "Created " + fullPath[0] + ":\n\n" + data;
if (timeMsg) this.showString (J.util.Logger.getTimerMsg ("export", 0));
} else {
msg = data;
}if (msg != null) {
if (!msg.startsWith ("OK")) this.evalError (msg, null);
this.scriptStatusOrBuffer (data);
}return "";
} else if (data === "MENU") {
data = this.viewer.getMenu ("");
} else if (data === "PGRP") {
data = this.viewer.getPointGroupAsString (type2.equals ("draw"), null, 0, 1.0);
} else if (data === "PDB" || data === "PQR") {
if (isShow) {
data = this.viewer.getPdbAtomData (null, null);
} else {
doDefer = true;
}} else if (data === "FILE") {
if (isShow) data = this.viewer.getCurrentFileAsString ();
 else doDefer = true;
if ("?".equals (fileName)) fileName = "?Jmol." + this.viewer.getParameter ("_fileType");
} else if ((data === "SDF" || data === "MOL" || data === "V2000" || data === "V3000" || data === "CD") && isCoord) {
data = this.viewer.getModelExtract ("selected", true, false, data);
if (data.startsWith ("ERROR:")) bytes = data;
} else if (data === "XYZ" || data === "XYZRN" || data === "XYZVIB" || data === "MOL" || data === "SDF" || data === "V2000" || data === "V3000" || data === "CML" || data === "CD") {
data = this.viewer.getData ("selected", data);
if (data.startsWith ("ERROR:")) bytes = data;
} else if (data === "FUNCTION") {
data = this.viewer.getFunctionCalls (null);
type = "TXT";
} else if (data === "VAR") {
data = (this.getParameter (J.script.SV.sValue (this.tokenAt (isCommand ? 2 : 1, args)), 1073742190)).asString ();
type = "TXT";
} else if (data === "SPT") {
if (isCoord) {
var tainted = this.viewer.getTaintedAtoms (2);
this.viewer.setAtomCoordsRelative (J.util.P3.new3 (0, 0, 0), null);
data = this.viewer.getStateInfo ();
this.viewer.setTaintedAtoms (tainted, 2);
} else {
data = this.viewer.getStateInfo ();
if (localPath != null || remotePath != null) data = J.viewer.FileManager.setScriptFileReferences (data, localPath, remotePath, null);
}} else if (data === "ZIP" || data === "ZIPALL") {
data = this.viewer.getStateInfo ();
bytes = this.viewer.createZip (fileName, type, data, scripts);
if (bytes == null) this.evalError ("#CANCELED#", null);
} else if (data === "HISTORY") {
data = this.viewer.getSetHistory (2147483647);
type = "SPT";
} else if (data === "MO") {
data = this.getMoJvxl (2147483647);
type = "XJVXL";
} else if (data === "PMESH") {
if ((data = this.getIsosurfaceJvxl (true, 28)) == null) this.error (31);
type = "XJVXL";
} else if (data === "ISOSURFACE" || data === "MESH") {
if ((data = this.getIsosurfaceJvxl (data === "MESH", 24)) == null) this.error (31);
type = (data.indexOf ("<?xml") >= 0 ? "XJVXL" : "JVXL");
if (!isShow) this.showString (this.getShapeProperty (24, "jvxlFileInfo"));
} else {
len = -1;
if (quality < 0) quality = -1;
}if (data == null && !doDefer) data = "";
if (len == 0 && !doDefer) len = (bytes == null ? data.length : Clazz.instanceOf (bytes, String) ? (bytes).length : (bytes).length);
if (isImage) {
this.refresh ();
if (width < 0) width = this.viewer.getScreenWidth ();
if (height < 0) height = this.viewer.getScreenHeight ();
}}if (!isCommand) return data;
if (isShow) {
this.showStringPrint (data, true);
return "";
}if (bytes != null && Clazz.instanceOf (bytes, String)) {
{
if (bytes.indexOf("OK") != 0)alert(bytes);
}this.scriptStatusOrBuffer (bytes);
return bytes;
}if (type.equals ("SCENE")) bytes = sceneType;
 else if (bytes == null && (!isImage || fileName != null)) bytes = data;
if (timeMsg) J.util.Logger.startTimer ("write");
if (doDefer) msg = this.viewer.writeFileData (fileName, type, 0, null);
 else msg = this.viewer.createImageSet (fileName, type, (Clazz.instanceOf (bytes, String) ? bytes : null), (Clazz.instanceOf (bytes, Array) ? bytes : null), scripts, quality, width, height, bsFrames, nVibes, fullPath);
if (timeMsg) this.showString (J.util.Logger.getTimerMsg ("write", 0));
}if (!this.chk && msg != null) {
if (!msg.startsWith ("OK")) {
this.evalError (msg, null);
{
alert(msg);
}}this.scriptStatusOrBuffer (msg + (isImage ? "; width=" + width + "; height=" + height : ""));
return msg;
}return "";
}, "~A");
$_M(c$, "show", 
($fz = function () {
var value = null;
var str = this.parameterAsString (1);
var msg = null;
var name = null;
var len = 2;
var token = this.getToken (1);
var tok = (Clazz.instanceOf (token, J.script.SV) ? 0 : token.tok);
if (tok == 4) {
token = J.script.T.getTokenFromName (str.toLowerCase ());
if (token != null) tok = token.tok;
}if (tok != 1297090050 && tok != 1073742158) this.checkLength (-3);
if (this.slen == 2 && str.indexOf ("?") >= 0) {
this.showString (this.viewer.getAllSettings (str.substring (0, str.indexOf ("?"))));
return;
}switch (tok) {
case 0:
if (!this.chk) msg = (this.theToken).escape ();
break;
case 135270422:
if (!this.chk) msg = J.util.Escape.e (this.viewer.cacheList ());
break;
case 1073741915:
this.checkLength (2);
if (!this.chk) msg = this.viewer.calculateStructures (null, true, false);
break;
case 545259571:
this.checkLength (2);
if (!this.chk) msg = this.viewer.getPathForAllFiles ();
break;
case 1073742038:
if (this.optParameterAsString (2).equalsIgnoreCase ("1H")) {
len = 3;
if (!this.chk) msg = this.viewer.getNMRPredict (false);
break;
}if (!this.chk) this.viewer.getNMRPredict (true);
return;
case 135267336:
case 1073741929:
case 1073741879:
this.checkLength (tok == 1073741879 ? 3 : 2);
if (this.chk) return;
msg = this.viewer.getSmiles (0, 0, this.viewer.getSelectionSet (false), false, true, false, false);
switch (tok) {
case 1073741929:
if (msg.length > 0) {
this.viewer.show2D (msg);
return;
}msg = "Could not show drawing -- Either insufficient atoms are selected or the model is a PDB file.";
break;
case 1073741879:
len = 3;
var info = null;
if (msg.length > 0) {
var type = '/';
switch (this.getToken (2).tok) {
case 1073741977:
type = 'I';
break;
case 1073741978:
type = 'K';
break;
case 1073742035:
type = 'N';
break;
default:
info = this.parameterAsString (2);
}
msg = this.viewer.getChemicalInfo (msg, type, info);
if (msg.indexOf ("FileNotFound") >= 0) msg = "?";
} else {
msg = "Could not show name -- Either insufficient atoms are selected or the model is a PDB file.";
}}
break;
case 1297090050:
if (this.slen > 3) {
var pt1 = this.centerParameter (2);
var pt2 = this.centerParameter (++this.iToken);
if (!this.chk) msg = this.viewer.getSymmetryOperation (null, 0, pt1, pt2, false);
len = ++this.iToken;
} else {
var iop = (this.checkLength23 () == 2 ? 0 : this.intParameter (2));
if (!this.chk) msg = this.viewer.getSymmetryOperation (null, iop, null, null, false);
len = -3;
}break;
case 1649412120:
var vdwType = null;
if (this.slen > 2) {
vdwType = J.constant.EnumVdw.getVdwType (this.parameterAsString (2));
if (vdwType == null) this.invArg ();
}if (!this.chk) this.showString (this.viewer.getDefaultVdwTypeNameOrData (0, vdwType));
return;
case 135368713:
this.checkLength23 ();
if (!this.chk) this.showString (this.viewer.getFunctionCalls (this.optParameterAsString (2)));
return;
case 1085443:
this.checkLength (2);
if (!this.chk) this.showString (this.viewer.getAllSettings (null));
return;
case 1074790760:
if ((len = this.slen) == 2) {
if (!this.chk) this.viewer.showUrl (this.getFullPathName ());
return;
}name = this.parameterAsString (2);
if (!this.chk) this.viewer.showUrl (name);
return;
case 1766856708:
str = "defaultColorScheme";
break;
case 1610612740:
str = "scaleAngstromsPerInch";
break;
case 135270417:
case 1052714:
if (this.chk) return;
var modelIndex = this.viewer.getCurrentModelIndex ();
if (modelIndex < 0) this.errorStr (30, "show " + this.theToken.value);
msg = this.getExtension ().plot (this.st);
len = this.slen;
break;
case 1113200654:
if (!this.chk) msg = this.getContext (false);
break;
case 1073741888:
name = this.optParameterAsString (2);
if (name.length > 0) len = 3;
if (!this.chk) value = this.viewer.getColorSchemeList (name);
break;
case 1073742192:
if (!this.chk) msg = this.viewer.getAtomDefs (this.definedAtomSets) + this.viewer.getVariableList () + this.getContext (true);
break;
case 536870926:
if (!this.chk) msg = this.viewer.getTrajectoryState ();
break;
case 553648148:
value = "" + this.commandHistoryLevelMax;
break;
case 553648150:
value = "" + J.util.Logger.getLogLevel ();
break;
case 603979824:
value = "" + this.viewer.getBoolean (603979824);
break;
case 553648178:
msg = "set strandCountForStrands " + this.viewer.getStrandCount (12) + "; set strandCountForMeshRibbon " + this.viewer.getStrandCount (13);
break;
case 536875070:
msg = this.viewer.showTimeout ((len = this.slen) == 2 ? null : this.parameterAsString (2));
break;
case 536870918:
value = J.util.Escape.eP (this.viewer.getDefaultLattice ());
break;
case 4126:
if (!this.chk) msg = this.viewer.getMinimizationInfo ();
break;
case 1611272194:
switch (this.viewer.getAxesMode ()) {
case J.constant.EnumAxesMode.UNITCELL:
msg = "set axesUnitcell";
break;
case J.constant.EnumAxesMode.BOUNDBOX:
msg = "set axesWindow";
break;
default:
msg = "set axesMolecular";
}
break;
case 1610612737:
msg = "set bondMode " + (this.viewer.getBoolean (603979812) ? "OR" : "AND");
break;
case 1650071565:
if (!this.chk) msg = "set strandCountForStrands " + this.viewer.getStrandCount (12) + "; set strandCountForMeshRibbon " + this.viewer.getStrandCount (13);
break;
case 1612189718:
msg = "set hbondsBackbone " + this.viewer.getBoolean (603979852) + ";set hbondsSolid " + this.viewer.getBoolean (603979854);
break;
case 1611141175:
if (!this.chk) msg = this.viewer.getSpinState ();
break;
case 1611141176:
msg = "set ssbondsBackbone " + this.viewer.getBoolean (603979952);
break;
case 1610625028:
case 1611141171:
msg = "selectionHalos " + (this.viewer.getSelectionHaloEnabled (false) ? "ON" : "OFF");
break;
case 1613758470:
msg = "set selectHetero " + this.viewer.getBoolean (1613758470);
break;
case 1073741828:
msg = J.util.Escape.eAP (this.viewer.getAdditionalHydrogens (null, true, true, null));
break;
case 1613758476:
msg = "set selectHydrogens " + this.viewer.getBoolean (1613758476);
break;
case 553648130:
case 553648142:
case 536870924:
case 553648176:
case 553648172:
case 1073741995:
if (!this.chk) msg = this.viewer.getSpecularState ();
break;
case 4146:
if (!this.chk) msg = this.viewer.listSavedStates ();
break;
case 1614417948:
if (!this.chk) msg = this.viewer.getUnitCellInfoText ();
break;
case 1048582:
if ((len = this.slen) == 2) {
if (!this.chk) msg = this.viewer.getCoordinateState (this.viewer.getSelectionSet (false));
break;
}var nameC = this.parameterAsString (2);
if (!this.chk) msg = this.viewer.getSavedCoordinates (nameC);
break;
case 1073742158:
if (!this.chk) this.viewer.clearConsole ();
if ((len = this.slen) == 2) {
if (!this.chk) msg = this.viewer.getStateInfo ();
break;
}name = this.parameterAsString (2);
if (name.equals ("/") && (len = this.slen) == 4) {
name = this.parameterAsString (3).toLowerCase ();
if (!this.chk) {
var info = J.util.TextFormat.split (this.viewer.getStateInfo (), '\n');
var sb =  new J.util.SB ();
for (var i = 0; i < info.length; i++) if (info[i].toLowerCase ().indexOf (name) >= 0) sb.append (info[i]).appendC ('\n');

msg = sb.toString ();
}break;
} else if (this.tokAt (2) == 1229984263 && (len = this.slen) == 4) {
if (!this.chk) msg = this.viewer.getEmbeddedFileState (this.parameterAsString (3));
break;
}len = 3;
if (!this.chk) msg = this.viewer.getSavedState (name);
break;
case 1641025539:
if ((len = this.slen) == 2) {
if (!this.chk) msg = this.viewer.getProteinStructureState ();
break;
}var shape = this.parameterAsString (2);
if (!this.chk) msg = this.viewer.getSavedStructure (shape);
break;
case 135270407:
var type = ((len = this.slen) == 3 ? this.parameterAsString (2) : null);
if (!this.chk) {
var data = (type == null ? this.$data : this.viewer.getData (type));
msg = (data == null ? "no data" : J.util.Escape.encapsulateData (data[0], data[1], (data[3]).intValue ()));
}break;
case 1073742152:
var info = null;
if ((len = this.slen) == 2) {
if (!this.chk) {
info = this.viewer.getSpaceGroupInfo (null);
}} else {
var sg = this.parameterAsString (2);
if (!this.chk) info = this.viewer.getSpaceGroupInfo (J.util.TextFormat.simpleReplace (sg, "''", "\""));
}if (info != null) msg = "" + info.get ("spaceGroupInfo") + info.get ("symmetryInfo");
break;
case 1048583:
len = 3;
msg = this.setObjectProperty ();
break;
case 1679429641:
if (!this.chk) {
msg = this.viewer.getBoundBoxCommand (true);
}break;
case 12289:
if (!this.chk) msg = "center " + J.util.Escape.eP (this.viewer.getRotationCenter ());
break;
case 135176:
if (!this.chk) msg = this.getShapeProperty (22, "command");
break;
case 1229984263:
if (!this.chk) this.viewer.clearConsole ();
if (this.slen == 2) {
if (!this.chk) msg = this.viewer.getCurrentFileAsString ();
if (msg == null) msg = "<unavailable>";
break;
}len = 3;
value = this.parameterAsString (2);
if (!this.chk) msg = this.viewer.getFileAsString (value);
break;
case 4115:
if (this.tokAt (2) == 1048579 && (len = 3) > 0) msg = this.viewer.getModelFileInfoAll ();
 else msg = this.viewer.getModelFileInfo ();
break;
case 1610616855:
var n = ((len = this.slen) == 2 ? 2147483647 : this.intParameter (2));
if (n < 1) this.invArg ();
if (!this.chk) {
this.viewer.clearConsole ();
if (this.scriptLevel == 0) this.viewer.removeCommand ();
msg = this.viewer.getSetHistory (n);
}break;
case 135180:
if (!this.chk) msg = this.getShapeProperty (24, "jvxlDataXml");
break;
case 1183762:
if (this.optParameterAsString (2).equalsIgnoreCase ("list")) {
msg = this.viewer.getMoInfo (-1);
len = 3;
} else {
var ptMO = ((len = this.slen) == 2 ? -2147483648 : this.intParameter (2));
if (!this.chk) msg = this.getMoJvxl (ptMO);
}break;
case 1095766028:
if (!this.chk) msg = this.viewer.getModelInfoAsString ();
break;
case 537006096:
if (!this.chk) msg = this.viewer.getMeasurementInfoAsString ();
break;
case 1073742132:
tok = this.tokAt (2);
if (tok == 0) tok = 1073742132;
 else len = 3;
case 1073742178:
case 4130:
if (!this.chk) msg = this.viewer.getOrientationText (tok, null);
break;
case 1073742077:
len = 2;
if (this.slen > 3) break;
switch (tok = this.tokAt (2)) {
case 1073742178:
case 1073742132:
case 4130:
case 0:
if (!this.chk) msg = this.viewer.getOrientationText (tok, null);
break;
default:
name = this.optParameterAsString (2);
msg = this.viewer.getOrientationText (1073742035, name);
}
len = this.slen;
break;
case 1073742088:
if (!this.chk) msg = this.viewer.getPDBHeader ();
break;
case 1073742102:
this.pointGroup ();
return;
case 1089470478:
if (!this.chk) msg = this.viewer.getSymmetryInfoAsString ();
break;
case 1073742176:
if (!this.chk) msg = "transform:\n" + this.viewer.getTransformText ();
break;
case 4168:
msg = "zoom " + (this.viewer.getZoomEnabled () ? ("" + this.viewer.getZoomSetting ()) : "off");
break;
case 1611272202:
msg = (this.viewer.getShowFrank () ? "frank ON" : "frank OFF");
break;
case 1666189314:
str = "solventProbeRadius";
break;
case 1073741864:
case 1087373316:
case 1087373320:
case 1073742120:
case 1114638363:
case 1087373318:
case 1141899265:
case 1073741982:
msg = this.viewer.getChimeInfo (tok);
break;
case 537022465:
case 1610612738:
case 1716520985:
case 20482:
case 1613758488:
value = "?";
break;
case 1073741824:
if (str.equalsIgnoreCase ("fileHeader")) {
if (!this.chk) msg = this.viewer.getPDBHeader ();
} else if (str.equalsIgnoreCase ("menu")) {
if (!this.chk) value = this.viewer.getMenu ("");
} else if (str.equalsIgnoreCase ("mouse")) {
var qualifiers = ((len = this.slen) == 2 ? null : this.parameterAsString (2));
if (!this.chk) msg = this.viewer.getBindingInfo (qualifiers);
}break;
}
this.checkLength (len);
if (this.chk) return;
if (msg != null) this.showString (msg);
 else if (value != null) this.showString (str + " = " + value);
 else if (str != null) {
if (str.indexOf (" ") >= 0) this.showString (str);
 else this.showString (str + " = " + this.getParameterEscaped (str));
}}, $fz.isPrivate = true, $fz));
$_M(c$, "getIsosurfaceJvxl", 
($fz = function (asMesh, iShape) {
if (this.chk) return "";
return this.getShapeProperty (iShape, asMesh ? "jvxlMeshX" : "jvxlDataXml");
}, $fz.isPrivate = true, $fz), "~B,~N");
$_M(c$, "getMoJvxl", 
($fz = function (ptMO) {
this.sm.loadShape (27);
var modelIndex = this.viewer.getCurrentModelIndex ();
if (modelIndex < 0) this.errorStr (30, "MO isosurfaces");
var moData = this.viewer.getModelAuxiliaryInfoValue (modelIndex, "moData");
if (moData == null) this.error (27);
var n = this.getShapeProperty (27, "moNumber");
if (n == null || n.intValue () == 0) {
this.setShapeProperty (27, "init", Integer.$valueOf (modelIndex));
}this.setShapeProperty (27, "moData", moData);
return this.getShapePropertyIndex (27, "showMO", ptMO);
}, $fz.isPrivate = true, $fz), "~N");
$_M(c$, "getColorTrans", 
function (i, allowNone) {
var translucentLevel = 3.4028235E38;
if (this.theTok != 1766856708) --i;
switch (this.tokAt (i + 1)) {
case 603979967:
i++;
translucentLevel = (this.isFloatParameter (i + 1) ? this.getTranslucentLevel (++i) : this.viewer.getFloat (570425354));
break;
case 1073742074:
i++;
translucentLevel = 0;
break;
}
if (this.isColorParam (i + 1)) {
this.colorArgb[0] = this.getArgbParam (++i);
} else if (this.tokAt (i + 1) == 1048587) {
this.colorArgb[0] = 0;
this.iToken = i + 1;
} else if (translucentLevel == 3.4028235E38) {
this.invArg ();
} else {
this.colorArgb[0] = -2147483648;
}i = this.iToken;
return translucentLevel;
}, "~N,~B");
$_M(c$, "finalizeObject", 
function (shapeID, colorArgb, translucentLevel, intScale, doSet, data, iptDisplayProperty, bs) {
if (doSet) {
this.setShapeProperty (shapeID, "set", data);
}if (colorArgb != -2147483648) this.setShapePropertyBs (shapeID, "color", Integer.$valueOf (colorArgb), bs);
if (translucentLevel != 3.4028235E38) this.setShapeTranslucency (shapeID, "", "translucent", translucentLevel, bs);
if (intScale != 0) {
this.setShapeProperty (shapeID, "scale", Integer.$valueOf (intScale));
}if (iptDisplayProperty > 0) {
if (!this.setMeshDisplayProperty (shapeID, iptDisplayProperty, 0)) this.invArg ();
}}, "~N,~N,~N,~N,~B,~O,~N,J.util.BS");
$_M(c$, "setContactBitSets", 
function (bsA, bsB, localOnly, distance, rd, warnMultiModel) {
var withinAllModels;
var bs;
if (bsB == null) {
bsB = J.util.BSUtil.setAll (this.viewer.getAtomCount ());
J.util.BSUtil.andNot (bsB, this.viewer.getDeletedAtoms ());
bsB.andNot (bsA);
withinAllModels = false;
} else {
bs = J.util.BSUtil.copy (bsA);
bs.or (bsB);
var nModels = this.viewer.getModelBitSet (bs, false).cardinality ();
withinAllModels = (nModels > 1);
if (warnMultiModel && nModels > 1 && !this.tQuiet) this.showString (J.i18n.GT._ ("Note: More than one model is involved in this contact!"));
}if (!bsA.equals (bsB)) {
var setBfirst = (!localOnly || bsA.cardinality () < bsB.cardinality ());
if (setBfirst) {
bs = this.viewer.getAtomsWithinRadius (distance, bsA, withinAllModels, (Float.isNaN (distance) ? rd : null));
bsB.and (bs);
}if (localOnly) {
bs = this.viewer.getAtomsWithinRadius (distance, bsB, withinAllModels, (Float.isNaN (distance) ? rd : null));
bsA.and (bs);
if (!setBfirst) {
bs = this.viewer.getAtomsWithinRadius (distance, bsA, withinAllModels, (Float.isNaN (distance) ? rd : null));
bsB.and (bs);
}bs = J.util.BSUtil.copy (bsB);
bs.and (bsA);
if (bs.equals (bsA)) bsB.andNot (bsA);
 else if (bs.equals (bsB)) bsA.andNot (bsB);
}}return bsB;
}, "J.util.BS,J.util.BS,~B,~N,J.atomdata.RadiusData,~B");
$_M(c$, "getColorRange", 
function (i) {
var color1 = this.getArgbParam (i);
if (this.tokAt (++this.iToken) != 1074790746) this.invArg ();
var color2 = this.getArgbParam (++this.iToken);
var nColors = (this.tokAt (this.iToken + 1) == 2 ? this.intParameter (++this.iToken) : 0);
return J.util.ColorEncoder.getColorSchemeList (J.util.ColorEncoder.getPaletteAtoB (color1, color2, nColors));
}, "~N");
$_M(c$, "getIsosurfaceDataRange", 
function (iShape, sep) {
var dataRange = this.getShapeProperty (iShape, "dataRange");
return (dataRange != null && dataRange[0] != 3.4028235E38 && dataRange[0] != dataRange[1] ? sep + "isosurface" + " full data range " + dataRange[0] + " to " + dataRange[1] + " with color scheme spanning " + dataRange[2] + " to " + dataRange[3] : "");
}, "~N,~S");
$_M(c$, "setMeshDisplayProperty", 
function (shape, i, tok) {
var propertyName = null;
var propertyValue = null;
var allowCOLOR = (shape == 25);
var checkOnly = (i == 0);
if (!checkOnly) tok = this.getToken (i).tok;
switch (tok) {
case 1766856708:
if (allowCOLOR) this.iToken++;
 else break;
case 1073742074:
case 603979967:
if (!checkOnly) this.colorShape (shape, this.iToken, false);
return true;
case 0:
case 12291:
case 1048589:
case 1048588:
case 12294:
case 3145770:
case 1610625028:
case 3145768:
if (this.iToken == 1 && shape >= 0 && this.tokAt (2) == 0) this.setShapeProperty (shape, "thisID", null);
if (tok == 0) return (this.iToken == 1);
if (checkOnly) return true;
switch (tok) {
case 12291:
this.setShapeProperty (shape, "delete", null);
return true;
case 3145770:
case 12294:
tok = 1048588;
break;
case 3145768:
tok = 1048589;
break;
case 1610625028:
if (i + 1 == this.slen) tok = 1048589;
break;
}
case 1073741958:
case 1073741862:
case 1073741964:
case 1073741898:
case 1073742039:
case 1113198595:
case 1073742042:
case 1073742018:
case 1073742052:
case 1073741938:
case 1073742046:
case 1073742182:
case 1073742060:
case 1073741960:
case 1073742058:
propertyName = "token";
propertyValue = Integer.$valueOf (tok);
break;
}
if (propertyName == null) return false;
if (checkOnly) return true;
this.setShapeProperty (shape, propertyName, propertyValue);
if ((this.tokAt (this.iToken + 1)) != 0) {
if (!this.setMeshDisplayProperty (shape, ++this.iToken, 0)) --this.iToken;
}return true;
}, "~N,~N,~N");
$_M(c$, "bind", 
($fz = function () {
var mouseAction = this.stringParameter (1);
var name = this.parameterAsString (2);
this.checkLength (3);
if (!this.chk) this.viewer.bindAction (mouseAction, name);
}, $fz.isPrivate = true, $fz));
$_M(c$, "unbind", 
($fz = function () {
if (this.slen != 1) this.checkLength23 ();
var mouseAction = this.optParameterAsString (1);
var name = this.optParameterAsString (2);
if (mouseAction.length == 0 || this.tokAt (1) == 1048579) mouseAction = null;
if (name.length == 0 || this.tokAt (2) == 1048579) name = null;
if (name == null && mouseAction != null && J.viewer.ActionManager.getActionFromName (mouseAction) >= 0) {
name = mouseAction;
mouseAction = null;
}if (!this.chk) this.viewer.unBindAction (mouseAction, name);
}, $fz.isPrivate = true, $fz));
$_M(c$, "undoRedoMove", 
($fz = function () {
var n = 1;
var len = 2;
switch (this.tokAt (1)) {
case 0:
len = 1;
break;
case 1048579:
n = 0;
break;
case 2:
n = this.intParameter (1);
break;
default:
this.invArg ();
}
this.checkLength (len);
if (!this.chk) this.viewer.undoMoveAction (this.tokAt (0), n);
}, $fz.isPrivate = true, $fz));
$_M(c$, "getAtomsNearSurface", 
function (distance, surfaceId) {
var data = [surfaceId, null, null];
if (this.chk) return  new J.util.BS ();
if (this.getShapePropertyData (24, "getVertices", data)) return this.viewer.getAtomsNearPts (distance, data[1], data[2]);
data[1] = Integer.$valueOf (0);
data[2] = Integer.$valueOf (-1);
if (this.getShapePropertyData (22, "getCenter", data)) return this.viewer.getAtomsNearPt (distance, data[2]);
return  new J.util.BS ();
}, "~N,~S");
c$.getFloatEncodedInt = $_M(c$, "getFloatEncodedInt", 
function (strDecimal) {
var pt = strDecimal.indexOf (".");
if (pt < 1 || strDecimal.charAt (0) == '-' || strDecimal.endsWith (".") || strDecimal.contains (".0")) return 2147483647;
var i = 0;
var j = 0;
if (pt > 0) {
try {
i = Integer.parseInt (strDecimal.substring (0, pt));
if (i < 0) i = -i;
} catch (e) {
if (Clazz.exceptionOf (e, NumberFormatException)) {
i = -1;
} else {
throw e;
}
}
}if (pt < strDecimal.length - 1) try {
j = Integer.parseInt (strDecimal.substring (pt + 1));
} catch (e) {
if (Clazz.exceptionOf (e, NumberFormatException)) {
} else {
throw e;
}
}
i = i * 1000000 + j;
return (i < 0 ? 2147483647 : i);
}, "~S");
c$.getPartialBondOrderFromFloatEncodedInt = $_M(c$, "getPartialBondOrderFromFloatEncodedInt", 
function (bondOrderInteger) {
return (((Clazz.doubleToInt (bondOrderInteger / 1000000)) % 6) << 5) + ((bondOrderInteger % 1000000) & 0x1F);
}, "~N");
c$.getBondOrderFromString = $_M(c$, "getBondOrderFromString", 
function (s) {
return (s.indexOf (' ') < 0 ? J.util.JmolEdge.getBondOrderFromString (s) : s.toLowerCase ().indexOf ("partial ") == 0 ? J.script.ScriptEvaluator.getPartialBondOrderFromString (s.substring (8).trim ()) : 131071);
}, "~S");
c$.getPartialBondOrderFromString = $_M(c$, "getPartialBondOrderFromString", 
($fz = function (s) {
return J.script.ScriptEvaluator.getPartialBondOrderFromFloatEncodedInt (J.script.ScriptEvaluator.getFloatEncodedInt (s));
}, $fz.isPrivate = true, $fz), "~S");
Clazz.overrideMethod (c$, "addHydrogensInline", 
function (bsAtoms, vConnections, pts) {
var modelIndex = this.viewer.getAtomModelIndex (bsAtoms.nextSetBit (0));
if (modelIndex != this.viewer.modelSet.modelCount - 1) return  new J.util.BS ();
var bsA = this.viewer.getModelUndeletedAtomsBitSet (modelIndex);
this.viewer.setAppendNew (false);
var atomIndex = this.viewer.modelSet.getAtomCount ();
var atomno = this.viewer.modelSet.getAtomCountInModel (modelIndex);
var sbConnect =  new J.util.SB ();
for (var i = 0; i < vConnections.size (); i++) {
var a = vConnections.get (i);
sbConnect.append (";  connect 0 100 ").append ("({" + (atomIndex++) + "}) ").append ("({" + a.index + "}) group;");
}
var sb =  new J.util.SB ();
sb.appendI (pts.length).append ("\n").append ("Viewer.AddHydrogens").append ("#noautobond").append ("\n");
for (var i = 0; i < pts.length; i++) sb.append ("H ").appendF (pts[i].x).append (" ").appendF (pts[i].y).append (" ").appendF (pts[i].z).append (" - - - - ").appendI (++atomno).appendC ('\n');

this.viewer.openStringInlineParamsAppend (sb.toString (), null, true);
this.runScriptBuffer (sbConnect.toString (), null);
var bsB = this.viewer.getModelUndeletedAtomsBitSet (modelIndex);
bsB.andNot (bsA);
return bsB;
}, "J.util.BS,J.util.JmolList,~A");
Clazz.overrideMethod (c$, "stopScriptThreads", 
function () {
if (this.scriptDelayThread != null) {
this.scriptDelayThread.interrupt ();
this.scriptDelayThread = null;
}if (this.fileLoadThread != null) {
this.fileLoadThread.interrupt ();
this.fileLoadThread.resumeEval ();
if (this.thisContext != null) this.popContext (false, false);
this.fileLoadThread = null;
}});
$_M(c$, "delayScript", 
function (millis) {
if (this.viewer.autoExit) return;
this.stopScriptThreads ();
this.scriptDelayThread =  new J.script.ScriptDelayThread (this, this.viewer, millis);
this.scriptDelayThread.run ();
}, "~N");
Clazz.defineStatics (c$,
"EXPRESSION_KEY", "e_x_p_r_e_s_s_i_o_n",
"scriptLevelMax", 100,
"evalID", 0,
"tryPt", 0,
"ERROR_axisExpected", 0,
"ERROR_backgroundModelError", 1,
"ERROR_badArgumentCount", 2,
"ERROR_badMillerIndices", 3,
"ERROR_badRGBColor", 4,
"ERROR_booleanExpected", 5,
"ERROR_booleanOrNumberExpected", 6,
"ERROR_booleanOrWhateverExpected", 7,
"ERROR_colorExpected", 8,
"ERROR_colorOrPaletteRequired", 9,
"ERROR_commandExpected", 10,
"ERROR_coordinateOrNameOrExpressionRequired", 11,
"ERROR_drawObjectNotDefined", 12,
"ERROR_endOfStatementUnexpected", 13,
"ERROR_expressionExpected", 14,
"ERROR_expressionOrIntegerExpected", 15,
"ERROR_filenameExpected", 16,
"ERROR_fileNotFoundException", 17,
"ERROR_incompatibleArguments", 18,
"ERROR_insufficientArguments", 19,
"ERROR_integerExpected", 20,
"ERROR_integerOutOfRange", 21,
"ERROR_invalidArgument", 22,
"ERROR_invalidParameterOrder", 23,
"ERROR_keywordExpected", 24,
"ERROR_moCoefficients", 25,
"ERROR_moIndex", 26,
"ERROR_moModelError", 27,
"ERROR_moOccupancy", 28,
"ERROR_moOnlyOne", 29,
"ERROR_multipleModelsDisplayedNotOK", 30,
"ERROR_noData", 31,
"ERROR_noPartialCharges", 32,
"ERROR_noUnitCell", 33,
"ERROR_numberExpected", 34,
"ERROR_numberMustBe", 35,
"ERROR_numberOutOfRange", 36,
"ERROR_objectNameExpected", 37,
"ERROR_planeExpected", 38,
"ERROR_propertyNameExpected", 39,
"ERROR_spaceGroupNotFound", 40,
"ERROR_stringExpected", 41,
"ERROR_stringOrIdentifierExpected", 42,
"ERROR_tooManyPoints", 43,
"ERROR_tooManyScriptLevels", 44,
"ERROR_unrecognizedAtomProperty", 45,
"ERROR_unrecognizedBondProperty", 46,
"ERROR_unrecognizedCommand", 47,
"ERROR_unrecognizedExpression", 48,
"ERROR_unrecognizedObject", 49,
"ERROR_unrecognizedParameter", 50,
"ERROR_unrecognizedParameterWarning", 51,
"ERROR_unrecognizedShowParameter", 52,
"ERROR_what", 53,
"ERROR_writeWhat", 54,
"ERROR_multipleModelsNotOK", 55,
"ERROR_cannotSet", 56,
"iProcess", 0);
});
