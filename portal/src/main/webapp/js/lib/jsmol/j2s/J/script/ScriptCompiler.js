Clazz.declarePackage ("J.script");
Clazz.load (["J.script.ScriptCompilationTokenParser", "J.util.JmolList"], "J.script.ScriptCompiler", ["java.lang.Boolean", "$.Character", "$.Float", "java.util.Hashtable", "J.api.Interface", "J.i18n.GT", "J.io.JmolBinary", "J.modelset.Bond", "$.Group", "J.script.ContextToken", "$.SV", "$.ScriptContext", "$.ScriptEvaluator", "$.ScriptFlowContext", "$.ScriptFunction", "$.T", "J.util.ArrayUtil", "$.BS", "$.Escape", "$.Logger", "$.Parser", "$.SB", "$.TextFormat", "J.viewer.Viewer"], function () {
c$ = Clazz.decorateAsClass (function () {
this.filename = null;
this.isSilent = false;
this.contextVariables = null;
this.aatokenCompiled = null;
this.lineNumbers = null;
this.lineIndices = null;
this.lnLength = 8;
this.preDefining = false;
this.isShowScriptOutput = false;
this.isCheckOnly = false;
this.haveComments = false;
this.scriptExtensions = null;
this.thisFunction = null;
this.flowContext = null;
this.ltoken = null;
this.lltoken = null;
this.vBraces = null;
this.ichBrace = 0;
this.cchToken = 0;
this.cchScript = 0;
this.nSemiSkip = 0;
this.parenCount = 0;
this.braceCount = 0;
this.setBraceCount = 0;
this.bracketCount = 0;
this.ptSemi = 0;
this.forPoint3 = 0;
this.setEqualPt = 0;
this.iBrace = 0;
this.iHaveQuotedString = false;
this.isEndOfCommand = false;
this.needRightParen = false;
this.endOfLine = false;
this.comment = null;
this.tokLastMath = 0;
this.checkImpliedScriptCmd = false;
this.vFunctionStack = null;
this.allowMissingEnd = false;
this.isShowCommand = false;
this.isComment = false;
this.isUserToken = false;
this.implicitString = false;
this.tokInitialPlusPlus = 0;
this.vPush = null;
this.pushCount = 0;
this.chFirst = '\0';
Clazz.instantialize (this, arguments);
}, J.script, "ScriptCompiler", J.script.ScriptCompilationTokenParser);
Clazz.prepareFields (c$, function () {
this.vPush =  new J.util.JmolList ();
});
Clazz.makeConstructor (c$, 
function (viewer) {
Clazz.superConstructor (this, J.script.ScriptCompiler, []);
this.viewer = viewer;
}, "J.viewer.Viewer");
$_M(c$, "compile", 
function (filename, script, isPredefining, isSilent, debugScript, isCheckOnly) {
this.isCheckOnly = isCheckOnly;
this.filename = filename;
this.isSilent = isSilent;
this.script = script;
this.logMessages = (!isSilent && !isPredefining && debugScript);
this.preDefining = (filename === "#predefine");
var doFull = true;
var isOK = this.compile0 (doFull);
if (!isOK) this.handleError ();
var sc =  new J.script.ScriptContext ();
isOK = (this.iBrace == 0 && this.parenCount == 0 && this.braceCount == 0 && this.bracketCount == 0);
sc.isComplete = isOK;
sc.script = script;
sc.scriptExtensions = this.scriptExtensions;
sc.errorType = this.errorType;
if (this.errorType != null) {
sc.iCommandError = this.iCommand;
this.setAaTokenCompiled ();
}sc.aatoken = this.aatokenCompiled;
sc.errorMessage = this.errorMessage;
sc.errorMessageUntranslated = (this.errorMessageUntranslated == null ? this.errorMessage : this.errorMessageUntranslated);
if (this.allowMissingEnd && sc.errorMessage != null && sc.errorMessageUntranslated.indexOf ("missing END") >= 0) sc.errorMessage = sc.errorMessageUntranslated;
sc.lineIndices = this.lineIndices;
sc.lineNumbers = this.lineNumbers;
sc.contextVariables = this.contextVariables;
return sc;
}, "~S,~S,~B,~B,~B,~B");
$_M(c$, "addContextVariable", 
($fz = function (ident) {
this.theToken = J.script.T.o (1073741824, ident);
if (this.pushCount > 0) {
var ct = this.vPush.get (this.pushCount - 1);
ct.addName (ident);
if (ct.tok != 364558) return;
}if (this.thisFunction == null) {
if (this.contextVariables == null) this.contextVariables =  new java.util.Hashtable ();
J.script.ScriptCompiler.addContextVariable (this.contextVariables, ident);
} else {
this.thisFunction.addVariable (ident, false);
}}, $fz.isPrivate = true, $fz), "~S");
c$.addContextVariable = $_M(c$, "addContextVariable", 
function (contextVariables, name) {
contextVariables.put (name, J.script.SV.newVariable (4, "").setName (name));
}, "java.util.Map,~S");
$_M(c$, "isContextVariable", 
($fz = function (ident) {
for (var i = this.vPush.size (); --i >= 0; ) {
var ct = this.vPush.get (i);
if (ct.contextVariables != null && ct.contextVariables.containsKey (ident)) return true;
}
return (this.thisFunction != null ? this.thisFunction.isVariable (ident) : this.contextVariables != null && this.contextVariables.containsKey (ident));
}, $fz.isPrivate = true, $fz), "~S");
$_M(c$, "cleanScriptComments", 
($fz = function (script) {
if (script.indexOf ('\u201C') >= 0) script = script.$replace ('\u201C', '"');
if (script.indexOf ('\u201D') >= 0) script = script.$replace ('\u201D', '"');
if (script.indexOf ('\uFEFF') >= 0) script = script.$replace ('\uFEFF', ' ');
var pt = (script.indexOf ("\1##"));
if (pt >= 0) {
this.scriptExtensions = script.substring (pt + 1);
script = script.substring (0, pt);
this.allowMissingEnd = (this.scriptExtensions.indexOf ("##noendcheck") >= 0);
}this.haveComments = (script.indexOf ("#") >= 0);
return J.io.JmolBinary.getEmbeddedScript (script);
}, $fz.isPrivate = true, $fz), "~S");
$_M(c$, "addTokenToPrefix", 
($fz = function (token) {
if (this.logMessages) J.util.Logger.debug ("addTokenToPrefix" + token);
this.ltoken.addLast (token);
if (token.tok != 0) this.lastToken = token;
}, $fz.isPrivate = true, $fz), "J.script.T");
$_M(c$, "compile0", 
($fz = function (isFull) {
this.vFunctionStack =  new J.util.JmolList ();
this.htUserFunctions =  new java.util.Hashtable ();
this.script = this.cleanScriptComments (this.script);
this.ichToken = this.script.indexOf ("# Jmol state version ");
this.isStateScript = (this.ichToken >= 0);
if (this.isStateScript) {
this.ptSemi = this.script.indexOf (";", this.ichToken);
if (this.ptSemi >= this.ichToken) this.viewer.setStateScriptVersion (this.script.substring (this.ichToken + "# Jmol state version ".length, this.ptSemi).trim ());
}this.cchScript = this.script.length;
this.contextVariables = null;
this.lineNumbers = null;
this.lineIndices = null;
this.aatokenCompiled = null;
this.thisFunction = null;
this.flowContext = null;
this.errorType = null;
this.errorMessage = null;
this.errorMessageUntranslated = null;
this.errorLine = null;
this.nSemiSkip = 0;
this.ichToken = 0;
this.ichCurrentCommand = 0;
this.ichComment = 0;
this.ichBrace = 0;
this.lineCurrent = 1;
this.iCommand = 0;
this.tokLastMath = 0;
this.lastToken = J.script.T.tokenOff;
this.vBraces =  new J.util.JmolList ();
this.vPush =  new J.util.JmolList ();
this.pushCount = 0;
this.iBrace = 0;
this.braceCount = 0;
this.parenCount = 0;
this.ptSemi = -10;
this.cchToken = 0;
this.lnLength = 8;
this.lineNumbers =  Clazz.newShortArray (this.lnLength, 0);
this.lineIndices =  Clazz.newIntArray (this.lnLength, 2, 0);
this.isNewSet = this.isSetBrace = false;
this.ptNewSetModifier = 1;
this.isShowScriptOutput = false;
this.iHaveQuotedString = false;
this.checkImpliedScriptCmd = false;
this.lltoken =  new J.util.JmolList ();
this.ltoken =  new J.util.JmolList ();
this.tokCommand = 0;
this.lastFlowCommand = null;
this.tokenAndEquals = null;
this.tokInitialPlusPlus = 0;
this.setBraceCount = 0;
this.bracketCount = 0;
this.forPoint3 = -1;
this.setEqualPt = 2147483647;
this.endOfLine = false;
this.comment = null;
this.isEndOfCommand = false;
this.needRightParen = false;
this.theTok = 0;
var iLine = 1;
for (; true; this.ichToken += this.cchToken) {
if ((this.nTokens = this.ltoken.size ()) == 0) {
if (this.thisFunction != null && this.thisFunction.chpt0 == 0) this.thisFunction.chpt0 = this.ichToken;
this.ichCurrentCommand = this.ichToken;
iLine = this.lineCurrent;
}if (this.lookingAtLeadingWhitespace ()) continue;
this.endOfLine = false;
if (!this.isEndOfCommand) {
this.endOfLine = this.lookingAtEndOfLine ();
switch (this.endOfLine ? 0 : this.lookingAtComment ()) {
case 2:
continue;
case 3:
this.isEndOfCommand = true;
continue;
case 1:
this.isEndOfCommand = true;
this.comment = this.script.substring (this.ichToken, this.ichToken + this.cchToken).trim ();
break;
}
this.isEndOfCommand = this.isEndOfCommand || this.endOfLine || this.lookingAtTerminator ();
}if (this.isEndOfCommand) {
this.isEndOfCommand = false;
switch (this.processTokenList (iLine, isFull)) {
case 2:
continue;
case 4:
return false;
}
this.checkImpliedScriptCmd = false;
if (this.ichToken < this.cchScript) continue;
this.setAaTokenCompiled ();
return (this.flowContext == null || this.errorStr (11, J.script.T.nameOf (this.flowContext.token.tok)));
}if (this.nTokens > 0) {
switch (this.checkSpecialParameterSyntax ()) {
case 2:
continue;
case 4:
return false;
}
}if (this.lookingAtLookupToken (this.ichToken)) {
var ident = this.getPrefixToken ();
switch (this.parseKnownToken (ident)) {
case 2:
continue;
case 4:
return false;
}
switch (this.parseCommandParameter (ident)) {
case 2:
continue;
case 4:
return false;
}
this.addTokenToPrefix (this.theToken);
continue;
}if (this.nTokens == 0 || (this.isNewSet || this.isSetBrace) && this.nTokens == this.ptNewSetModifier) {
if (this.nTokens == 0) {
if (this.lookingAtString (true)) {
this.addTokenToPrefix (this.setCommand (J.script.T.tokenScript));
this.cchToken = 0;
continue;
}if (this.lookingAtImpliedString (true, true, true)) this.ichEnd = this.ichToken + this.cchToken;
}return this.commandExpected ();
}return this.errorStr (19, this.script.substring (this.ichToken, this.ichToken + 1));
}
}, $fz.isPrivate = true, $fz), "~B");
$_M(c$, "setAaTokenCompiled", 
($fz = function () {
this.aatokenCompiled = this.lltoken.toArray ( new Array (this.lltoken.size ()));
}, $fz.isPrivate = true, $fz));
$_M(c$, "lookingAtLeadingWhitespace", 
($fz = function () {
var ichT = this.ichToken;
while (J.script.ScriptCompiler.isSpaceOrTab (this.charAt (ichT))) ++ichT;

if (this.isLineContinuation (ichT, true)) ichT += 1 + this.nCharNewLine (ichT + 1);
this.cchToken = ichT - this.ichToken;
return this.cchToken > 0;
}, $fz.isPrivate = true, $fz));
$_M(c$, "isLineContinuation", 
($fz = function (ichT, checkMathop) {
var isEscaped = (ichT + 2 < this.cchScript && this.script.charAt (ichT) == '\\' && this.nCharNewLine (ichT + 1) > 0 || checkMathop && this.lookingAtMathContinuation (ichT));
if (isEscaped) this.lineCurrent++;
return isEscaped;
}, $fz.isPrivate = true, $fz), "~N,~B");
$_M(c$, "lookingAtMathContinuation", 
($fz = function (ichT) {
var n;
if ((n = this.nCharNewLine (ichT)) == 0 || this.lastToken.tok == 1048586) return false;
if (this.parenCount > 0 || this.bracketCount > 0) return true;
if ((this.tokCommand != 1085443 || !this.isNewSet) && this.tokCommand != 36865 && this.tokCommand != 36869) return false;
if (this.lastToken.tok == this.tokLastMath) return true;
ichT += n;
while (J.script.ScriptCompiler.isSpaceOrTab (this.charAt (ichT))) ++ichT;

return (this.lookingAtLookupToken (ichT) && this.tokLastMath == 1);
}, $fz.isPrivate = true, $fz), "~N");
$_M(c$, "lookingAtEndOfLine", 
($fz = function () {
if (this.ichToken >= this.cchScript) {
this.ichEnd = this.cchScript;
return true;
}return ((this.cchToken = this.nCharNewLine (this.ichEnd = this.ichToken)) > 0);
}, $fz.isPrivate = true, $fz));
$_M(c$, "nCharNewLine", 
($fz = function (ichT) {
var ch;
return ((ch = this.charAt (ichT)) != '\r' ? (ch == '\n' ? 1 : 0) : this.charAt (++ichT) == '\n' ? 2 : 1);
}, $fz.isPrivate = true, $fz), "~N");
$_M(c$, "lookingAtTerminator", 
($fz = function () {
var isSemi = (this.script.charAt (this.ichToken) == ';');
if (isSemi && this.nTokens > 0) this.ptSemi = this.nTokens;
if (!isSemi || this.nSemiSkip-- > 0) return false;
this.cchToken = 1;
return true;
}, $fz.isPrivate = true, $fz));
$_M(c$, "lookingAtComment", 
($fz = function () {
var ch = this.script.charAt (this.ichToken);
var ichT = this.ichToken;
var ichFirstSharp = -1;
if (this.ichToken == this.ichCurrentCommand && ch == '$' && (this.isShowScriptOutput || this.ichToken == 0)) {
this.isShowScriptOutput = true;
this.isShowCommand = true;
if (this.charAt (++ichT) == '[') while (ch != ']' && !this.eol (ch = this.charAt (ichT))) ++ichT;

this.cchToken = ichT - this.ichToken;
return 2;
} else if (this.isShowScriptOutput && !this.isShowCommand) {
ichFirstSharp = ichT;
}if (ch == '/' && ichT + 1 < this.cchScript) switch (this.script.charAt (++ichT)) {
case '/':
ichFirstSharp = this.ichToken;
this.ichEnd = ichT - 1;
break;
case '*':
this.ichEnd = ichT - 1;
var terminator = ((ch = this.charAt (++ichT)) == '*' ? "**/" : "*/");
ichT = this.script.indexOf (terminator, this.ichToken + 2);
if (ichT < 0) {
this.ichToken = this.cchScript;
return 3;
}this.incrementLineCount (this.script.substring (this.ichToken, ichT));
this.cchToken = ichT + (ch == '*' ? 3 : 2) - this.ichToken;
return 2;
default:
return 0;
}
var isSharp = (ichFirstSharp < 0);
if (isSharp && !this.haveComments) return 0;
if (this.ichComment > ichT) ichT = this.ichComment;
for (; ichT < this.cchScript; ichT++) {
if (this.eol (ch = this.script.charAt (ichT))) {
this.ichEnd = ichT;
if (ichT > 0 && this.isLineContinuation (ichT - 1, false)) {
ichT += this.nCharNewLine (ichT);
continue;
}if (!isSharp && ch == ';') continue;
break;
}if (ichFirstSharp >= 0) continue;
if (ch == '#') ichFirstSharp = ichT;
}
if (ichFirstSharp < 0) return 0;
this.ichComment = ichFirstSharp;
if (isSharp && this.nTokens == 0 && this.cchScript - ichFirstSharp >= 3 && this.script.charAt (ichFirstSharp + 1) == 'j' && this.script.charAt (ichFirstSharp + 2) == 'c') {
this.cchToken = ichT - this.ichToken;
return 2;
}if (ichFirstSharp != this.ichToken) return 0;
if (isSharp && this.cchScript > this.ichToken + 3 && this.script.charAt (this.ichToken + 1) == 'j' && this.script.charAt (this.ichToken + 2) == 'x' && J.script.ScriptCompiler.isSpaceOrTab (this.script.charAt (this.ichToken + 3))) {
this.cchToken = 4;
return 2;
}if (ichT == this.ichToken) return 0;
this.cchToken = ichT - this.ichToken;
return (this.nTokens == 0 ? 1 : 2);
}, $fz.isPrivate = true, $fz));
$_M(c$, "charAt", 
($fz = function (i) {
return (i < this.cchScript ? this.script.charAt (i) : '\0');
}, $fz.isPrivate = true, $fz), "~N");
$_M(c$, "processTokenList", 
($fz = function (iLine, doCompile) {
if (this.nTokens > 0 || this.comment != null) {
if (this.nTokens == 0) {
this.ichCurrentCommand = this.ichToken;
if (this.comment != null) {
this.isComment = true;
this.addTokenToPrefix (J.script.T.o (0, this.comment));
}} else if (this.setBraceCount > 0 && this.endOfLine && this.ichToken < this.cchScript) {
return 2;
}if (this.tokCommand == 135271429 && this.checkImpliedScriptCmd) {
var s = (this.nTokens == 2 ? this.lastToken.value.toString ().toUpperCase () : null);
if (this.nTokens > 2 && !(this.tokAt (2) == 269484048 && this.ltoken.get (1).value.toString ().endsWith (".spt")) || s != null && (s.endsWith (".SORT") || s.endsWith (".REVERSE") || s.indexOf (".SORT(") >= 0 || s.indexOf (".REVERSE(") >= 0)) {
this.ichToken = this.ichCurrentCommand;
this.nTokens = 0;
this.ltoken.clear ();
this.cchToken = 0;
this.tokCommand = 0;
return 2;
}}if (this.isNewSet && this.nTokens > 2 && this.tokAt (2) == 1048584 && (this.tokAt (3) == 1276117010 || this.tokAt (3) == 1141899269)) {
this.ltoken.set (0, J.script.T.tokenSet);
this.ltoken.add (1, this.ltoken.get (1));
} else if (this.tokInitialPlusPlus != 0) {
if (!this.isNewSet) this.checkNewSetCommand ();
this.tokenizePlusPlus (this.tokInitialPlusPlus, true);
}this.iCommand = this.lltoken.size ();
if (this.thisFunction != null && this.thisFunction.cmdpt0 < 0) {
this.thisFunction.cmdpt0 = this.iCommand;
}if (this.nTokens == 1 && this.braceCount == 1) {
if (this.lastFlowCommand == null) {
this.parenCount = this.setBraceCount = this.braceCount = 0;
this.ltoken.remove (0);
this.iBrace++;
var t = J.script.ContextToken.newContext (true);
this.addTokenToPrefix (this.setCommand (t));
this.pushCount++;
this.vPush.addLast (t);
this.vBraces.addLast (this.tokenCommand);
} else {
this.parenCount = this.setBraceCount = 0;
this.setCommand (this.lastFlowCommand);
if (this.lastFlowCommand.tok != 102439 && (this.tokAt (0) == 1048586)) this.ltoken.remove (0);
this.lastFlowCommand = null;
}}if (this.bracketCount > 0 || this.setBraceCount > 0 || this.parenCount > 0 || this.braceCount == 1 && !this.checkFlowStartBrace (true)) {
this.error (this.nTokens == 1 ? 2 : 4);
return 4;
}if (this.needRightParen) {
this.addTokenToPrefix (J.script.T.tokenRightParen);
this.needRightParen = false;
}if (this.ltoken.size () > 0) {
if (doCompile && !this.compileCommand ()) return 4;
if (this.logMessages) {
J.util.Logger.debug ("-------------------------------------");
}var doEval = true;
switch (this.tokCommand) {
case 364558:
case 102436:
case 135368713:
case 1150985:
doEval = (this.atokenInfix.length > 0 && this.atokenInfix[0].intValue != 2147483647);
break;
}
if (doEval) {
if (this.iCommand == this.lnLength) {
this.lineNumbers = J.util.ArrayUtil.doubleLengthShort (this.lineNumbers);
var lnI =  Clazz.newIntArray (this.lnLength * 2, 2, 0);
System.arraycopy (this.lineIndices, 0, lnI, 0, this.lnLength);
this.lineIndices = lnI;
this.lnLength *= 2;
}this.lineNumbers[this.iCommand] = iLine;
this.lineIndices[this.iCommand][0] = this.ichCurrentCommand;
this.lineIndices[this.iCommand][1] = Math.max (this.ichCurrentCommand, Math.min (this.cchScript, this.ichEnd == this.ichCurrentCommand ? this.ichToken : this.ichEnd));
this.lltoken.addLast (this.atokenInfix);
this.iCommand = this.lltoken.size ();
}if (this.tokCommand == 1085443) this.lastFlowCommand = null;
}this.setCommand (null);
this.comment = null;
this.iHaveQuotedString = this.isNewSet = this.isSetBrace = this.needRightParen = false;
this.ptNewSetModifier = 1;
this.ltoken.clear ();
this.nTokens = this.nSemiSkip = 0;
this.tokInitialPlusPlus = 0;
this.tokenAndEquals = null;
this.ptSemi = -10;
this.forPoint3 = -1;
this.setEqualPt = 2147483647;
}if (this.endOfLine) {
if (this.flowContext != null && this.flowContext.checkForceEndIf ()) {
if (!this.isComment) this.forceFlowEnd (this.flowContext.token);
this.isEndOfCommand = true;
this.cchToken = 0;
this.ichCurrentCommand = this.ichToken;
this.lineCurrent--;
return 2;
}this.isComment = false;
this.isShowCommand = false;
++this.lineCurrent;
}if (this.ichToken >= this.cchScript) {
this.setCommand (J.script.T.tokenAll);
this.theTok = 0;
switch (this.checkFlowEndBrace ()) {
case 4:
return 4;
case 2:
this.isEndOfCommand = true;
this.cchToken = 0;
return 2;
}
this.ichToken = this.cchScript;
return 0;
}return 0;
}, $fz.isPrivate = true, $fz), "~N,~B");
$_M(c$, "compileCommand", 
($fz = function () {
switch (this.ltoken.size ()) {
case 0:
this.atokenInfix =  new Array (0);
return true;
case 4:
if (this.isNewSet && this.tokenAt (2).value.equals (".") && this.tokenAt (3).value.equals ("spt")) {
var fname = this.tokenAt (1).value + "." + this.tokenAt (3).value;
this.ltoken.clear ();
this.addTokenToPrefix (J.script.T.tokenScript);
this.addTokenToPrefix (J.script.T.o (4, fname));
this.isNewSet = false;
}}
this.setCommand (this.tokenAt (0));
var size = this.ltoken.size ();
if (size == 1 && J.script.T.tokAttr (this.tokCommand, 524288)) this.addTokenToPrefix (J.script.T.tokenOn);
if (this.tokenAndEquals != null) {
var j;
var i = 0;
for (i = 1; i < size; i++) {
if ((j = this.tokAt (i)) == 269484242) break;
}
size = i;
i++;
if (this.ltoken.size () < i) {
J.util.Logger.error ("COMPILER ERROR! - andEquals ");
} else {
for (j = 1; j < size; j++, i++) this.ltoken.add (i, this.tokenAt (j));

this.ltoken.set (size, J.script.T.tokenEquals);
this.ltoken.add (i, this.tokenAndEquals);
this.ltoken.add (++i, J.script.T.tokenLeftParen);
this.addTokenToPrefix (J.script.T.tokenRightParen);
}}this.atokenInfix = this.ltoken.toArray ( new Array (size = this.ltoken.size ()));
if (this.logMessages) {
J.util.Logger.debug ("token list:");
for (var i = 0; i < this.atokenInfix.length; i++) J.util.Logger.debug (i + ": " + this.atokenInfix[i]);

J.util.Logger.debug ("vBraces list:");
for (var i = 0; i < this.vBraces.size (); i++) J.util.Logger.debug (i + ": " + this.vBraces.get (i));

J.util.Logger.debug ("-------------------------------------");
}return this.compileExpressions ();
}, $fz.isPrivate = true, $fz));
$_M(c$, "tokenAt", 
($fz = function (i) {
return this.ltoken.get (i);
}, $fz.isPrivate = true, $fz), "~N");
Clazz.overrideMethod (c$, "tokAt", 
function (i) {
return (i < this.ltoken.size () ? this.tokenAt (i).tok : 0);
}, "~N");
$_M(c$, "setCommand", 
($fz = function (token) {
this.tokenCommand = token;
if (token == null) {
this.tokCommand = 0;
} else {
this.tokCommand = this.tokenCommand.tok;
this.isMathExpressionCommand = (this.tokCommand == 1073741824 || J.script.T.tokAttr (this.tokCommand, 36864));
this.isSetOrDefine = (this.tokCommand == 1085443 || this.tokCommand == 1060866);
this.isCommaAsOrAllowed = J.script.T.tokAttr (this.tokCommand, 12288);
this.implicitString = J.script.T.tokAttr (this.tokCommand, 20480);
}return token;
}, $fz.isPrivate = true, $fz), "J.script.T");
$_M(c$, "replaceCommand", 
($fz = function (token) {
this.ltoken.remove (0);
this.ltoken.add (0, this.setCommand (token));
}, $fz.isPrivate = true, $fz), "J.script.T");
$_M(c$, "getPrefixToken", 
($fz = function () {
var ident = this.script.substring (this.ichToken, this.ichToken + this.cchToken);
var identLC = ident.toLowerCase ();
var isUserVar = this.isContextVariable (identLC);
if (this.nTokens == 0) this.isUserToken = isUserVar;
if (this.nTokens == 1 && (this.tokCommand == 135368713 || this.tokCommand == 102436 || this.tokCommand == 36868) || this.nTokens != 0 && isUserVar || this.isUserFunction (identLC) && (this.thisFunction == null || !this.thisFunction.name.equals (identLC))) {
ident = identLC;
this.theToken = null;
} else if (ident.length == 1 || this.lastToken.tok == 269484066) {
if ((this.theToken = J.script.T.getTokenFromName (ident)) == null && (this.theToken = J.script.T.getTokenFromName (identLC)) != null) this.theToken = J.script.T.tv (this.theToken.tok, this.theToken.intValue, ident);
} else {
ident = identLC;
this.theToken = J.script.T.getTokenFromName (ident);
}if (this.theToken == null) {
if (ident.indexOf ("property_") == 0) this.theToken = J.script.T.o (1716520985, ident);
 else this.theToken = J.script.T.o (1073741824, ident);
}this.theTok = this.theToken.tok;
return ident;
}, $fz.isPrivate = true, $fz));
$_M(c$, "checkSpecialParameterSyntax", 
($fz = function () {
if (this.lookingAtString (!this.implicitString)) {
if (this.cchToken < 0) return this.ERROR (4);
var str = this.getUnescapedStringLiteral (this.lastToken != null && !this.iHaveQuotedString && this.lastToken.tok != 1073741983 && (this.tokCommand == 1085443 && this.nTokens == 2 && this.lastToken.tok == 545259546 || this.tokCommand == 135271426 || this.tokCommand == 1610616835 || this.tokCommand == 135271429));
this.iHaveQuotedString = true;
if (this.tokCommand == 135271426 && this.lastToken.tok == 135270407 || this.tokCommand == 135270407 && str.indexOf ("@") < 0) {
if (!this.getData (str)) {
return this.ERROR (11, "data");
}} else {
this.addTokenToPrefix (J.script.T.o (4, str));
if (this.implicitString) this.isEndOfCommand = true;
}return 2;
}if (this.lastToken.tok == 1074790550 && this.lookingAtImpliedString (false, false, false)) {
this.addTokenToPrefix (J.script.T.o (4, this.script.substring (this.ichToken, this.ichToken + this.cchToken)));
return 2;
}var ch;
if (this.nTokens == this.ptNewSetModifier) {
ch = this.script.charAt (this.ichToken);
var isAndEquals = ("+-\\*/&|=".indexOf (ch) >= 0);
var isOperation = (isAndEquals || ch == '.' || ch == '[');
var ch2 = this.charAt (this.ichToken + 1);
if (!this.isNewSet && this.isUserToken && isOperation && (ch == '=' || ch2 == ch || ch2 == '=')) {
this.isNewSet = true;
}if (this.isNewSet || this.tokCommand == 1085443 || J.script.T.tokAttr (this.tokCommand, 536870912)) {
if (ch == '=') this.setEqualPt = this.ichToken;
if (J.script.T.tokAttr (this.tokCommand, 536870912) && ch == '=' || (this.isNewSet || this.isSetBrace) && isOperation) {
this.setCommand (isAndEquals ? J.script.T.tokenSet : ch == '[' && !this.isSetBrace ? J.script.T.tokenSetArray : J.script.T.tokenSetProperty);
this.ltoken.add (0, this.tokenCommand);
this.cchToken = 1;
switch (ch) {
case '[':
this.addTokenToPrefix (J.script.T.o (269484096, "["));
this.bracketCount++;
return 2;
case '.':
this.addTokenToPrefix (J.script.T.o (1048584, "."));
return 2;
case '-':
case '+':
case '*':
case '/':
case '\\':
case '&':
case '|':
if (ch2.charCodeAt (0) == 0) return this.ERROR (4);
if (ch2 != ch && ch2 != '=') return this.ERROR (1, "\"" + ch + "\"");
break;
default:
this.lastToken = J.script.T.tokenMinus;
return 2;
}
}}}switch (this.tokCommand) {
case 135271426:
case 135271429:
case 135270410:
if (this.script.charAt (this.ichToken) == '@') {
this.iHaveQuotedString = true;
return 0;
}if (this.tokCommand == 135271426) {
if ((this.nTokens == 1 || this.nTokens == 2 && this.tokAt (1) == 1073741839) && this.lookingAtLoadFormat ()) {
var strFormat = this.script.substring (this.ichToken, this.ichToken + this.cchToken);
var token = J.script.T.getTokenFromName (strFormat.toLowerCase ());
switch (token == null ? 0 : token.tok) {
case 1073742015:
case 1073741839:
if (this.nTokens != 1) return 4;
case 135270407:
case 1229984263:
case 1073741983:
case 1095766028:
case 135267336:
case 536870926:
case 4156:
this.addTokenToPrefix (token);
break;
default:
var tok = (strFormat.indexOf ("=") == 0 || strFormat.indexOf ("$") == 0 ? 4 : J.util.Parser.isOneOf (strFormat = strFormat.toLowerCase (), ";xyz;vxyz;vibration;temperature;occupancy;partialcharge;") ? 1073741824 : 0);
if (tok != 0) {
this.addTokenToPrefix (J.script.T.o (tok, strFormat));
this.iHaveQuotedString = (tok == 4);
}}
return 2;
}var bs;
if (this.script.charAt (this.ichToken) == '{' || this.parenCount > 0) break;
if ((bs = this.lookingAtBitset ()) != null) {
this.addTokenToPrefix (J.script.T.o (10, bs));
return 2;
}}if (!this.iHaveQuotedString && this.lookingAtImpliedString (false, this.tokCommand == 135271426, this.nTokens > 1 || this.tokCommand != 135271429)) {
var str = this.script.substring (this.ichToken, this.ichToken + this.cchToken);
if (this.tokCommand == 135271429 && str.startsWith ("javascript:")) {
this.lookingAtImpliedString (true, true, true);
str = this.script.substring (this.ichToken, this.ichToken + this.cchToken);
}this.iHaveQuotedString = true;
this.addTokenToPrefix (J.script.T.o (4, str));
return 2;
}break;
case 4156:
if (this.nTokens == 1 && this.lookForSyncID ()) {
var ident = this.script.substring (this.ichToken, this.ichToken + this.cchToken);
var iident = J.util.Parser.parseInt (ident);
if (iident == -2147483648 || Math.abs (iident) < 1000) this.addTokenToPrefix (J.script.T.o (1073741824, ident));
 else this.addTokenToPrefix (J.script.T.i (iident));
return 2;
}break;
case 135270421:
if (this.nTokens == 2 && this.lastToken.tok == 4115) this.iHaveQuotedString = true;
if (!this.iHaveQuotedString) {
if (this.script.charAt (this.ichToken) == '@') {
this.iHaveQuotedString = true;
return 0;
}if (this.lookingAtImpliedString (true, true, true)) {
var pt = this.cchToken;
var str = this.script.substring (this.ichToken, this.ichToken + this.cchToken);
if (str.indexOf (" ") < 0) {
this.addTokenToPrefix (J.script.T.o (4, str));
this.iHaveQuotedString = true;
return 2;
}this.cchToken = pt;
}}break;
}
if (this.implicitString && !(this.tokCommand == 135271429 && this.iHaveQuotedString) && this.lookingAtImpliedString (true, true, true)) {
var str = this.script.substring (this.ichToken, this.ichToken + this.cchToken);
if (this.tokCommand == 1826248715 && J.util.Parser.isOneOf (str.toLowerCase (), "on;off;hide;display")) this.addTokenToPrefix (J.script.T.getTokenFromName (str.toLowerCase ()));
 else this.addTokenToPrefix (J.script.T.o (4, str));
return 2;
}if (this.lookingAtObjectID ()) {
this.addTokenToPrefix (J.script.T.getTokenFromName ("$"));
this.addTokenToPrefix (J.script.T.o (1073741824, this.script.substring (this.ichToken, this.ichToken + this.cchToken)));
return 2;
}var value;
if (!Float.isNaN (value = this.lookingAtExponential ())) {
this.addTokenToPrefix (J.script.T.o (3, Float.$valueOf (value)));
return 2;
}if (this.lookingAtDecimal ()) {
value = J.util.Parser.fVal (this.script.substring (this.ichToken, this.ichToken + this.cchToken));
var intValue = (J.script.ScriptEvaluator.getFloatEncodedInt (this.script.substring (this.ichToken, this.ichToken + this.cchToken)));
this.addTokenToPrefix (J.script.T.tv (3, intValue, Float.$valueOf (value)));
return 2;
}if (this.lookingAtSeqcode ()) {
ch = this.script.charAt (this.ichToken);
try {
var seqNum = (ch == '*' || ch == '^' ? 2147483647 : Integer.parseInt (this.script.substring (this.ichToken, this.ichToken + this.cchToken - 2)));
var insertionCode = this.script.charAt (this.ichToken + this.cchToken - 1);
if (insertionCode == '^') insertionCode = ' ';
if (seqNum < 0) {
seqNum = -seqNum;
this.addTokenToPrefix (J.script.T.tokenMinus);
}var seqcode = J.modelset.Group.getSeqcodeFor (seqNum, insertionCode);
this.addTokenToPrefix (J.script.T.tv (5, seqcode, "seqcode"));
} catch (nfe) {
if (Clazz.exceptionOf (nfe, NumberFormatException)) {
return this.ERROR (9, "" + ch);
} else {
throw nfe;
}
}
return 2;
}var val = this.lookingAtInteger ();
if (val != 2147483647) {
var intString = this.script.substring (this.ichToken, this.ichToken + this.cchToken);
if (this.tokCommand == 102407 || this.tokCommand == 102408) {
if (this.nTokens != 1) return this.ERROR (0);
var f = (this.flowContext == null ? null : this.flowContext.getBreakableContext (val = Math.abs (val)));
if (f == null) return this.ERROR (1, this.tokenCommand.value);
this.tokenAt (0).intValue = f.pt0;
}if (val == 0 && intString.equals ("-0")) this.addTokenToPrefix (J.script.T.tokenMinus);
this.addTokenToPrefix (J.script.T.tv (2, val, intString));
return 2;
}if (!this.isMathExpressionCommand && this.parenCount == 0 || this.lastToken.tok != 1073741824 && !J.script.ScriptCompilationTokenParser.tokenAttr (this.lastToken, 135266304)) {
var isBondOrMatrix = (this.script.charAt (this.ichToken) == '[');
var bs = this.lookingAtBitset ();
if (bs != null) {
this.addTokenToPrefix (J.script.T.o (10, isBondOrMatrix ?  new J.modelset.Bond.BondSet (bs) : bs));
return 2;
}if (isBondOrMatrix) {
var m = this.lookingAtMatrix ();
if (Clazz.instanceOf (m, J.util.Matrix3f) || Clazz.instanceOf (m, J.util.Matrix4f)) {
this.addTokenToPrefix (J.script.T.o ((Clazz.instanceOf (m, J.util.Matrix3f) ? 11 : 12), m));
return 2;
}}}return 0;
}, $fz.isPrivate = true, $fz));
$_M(c$, "lookingAtMatrix", 
($fz = function () {
var ipt;
var m;
if (this.ichToken + 4 >= this.cchScript || this.script.charAt (this.ichToken) != '[' || this.script.charAt (this.ichToken + 1) != '[' || (ipt = this.script.indexOf ("]]", this.ichToken)) < 0 || (m = J.util.Escape.unescapeMatrix (this.script.substring (this.ichToken, ipt + 2))) == null) return null;
this.cchToken = ipt + 2 - this.ichToken;
return m;
}, $fz.isPrivate = true, $fz));
$_M(c$, "parseKnownToken", 
($fz = function (ident) {
var token;
if (this.tokLastMath != 0) this.tokLastMath = this.theTok;
if (this.flowContext != null && this.flowContext.token.tok == 102410 && this.flowContext.$var != null && this.theTok != 102411 && this.theTok != 102413 && this.lastToken.tok != 102410) return this.ERROR (1, ident);
if (this.lastToken.tok == 1060866 && this.theTok != 1048586 && this.nTokens != 1) {
this.addTokenToPrefix (J.script.T.o (4, ident));
return 2;
}switch (this.theTok) {
case 1073741824:
if (this.nTokens == 0 && !this.checkImpliedScriptCmd) {
if (ident.charAt (0) == '\'') {
this.addTokenToPrefix (this.setCommand (J.script.T.tokenScript));
this.cchToken = 0;
return 2;
}if (this.charAt (this.ichToken + this.cchToken) == '.') {
this.addTokenToPrefix (this.setCommand (J.script.T.tokenScript));
this.nTokens = 1;
this.cchToken = 0;
this.checkImpliedScriptCmd = true;
return 2;
}}break;
case 269484242:
if (this.nSemiSkip == this.forPoint3 && this.nTokens == this.ptSemi + 2) {
token = this.lastToken;
this.addTokenToPrefix (J.script.T.tokenEquals);
this.addTokenToPrefix (token);
token = J.script.T.getTokenFromName (ident.substring (0, 1));
this.addTokenToPrefix (token);
this.addTokenToPrefix (J.script.T.tokenLeftParen);
this.needRightParen = true;
return 2;
}this.checkNewSetCommand ();
if (this.tokCommand == 1085443) {
this.tokenAndEquals = J.script.T.getTokenFromName (ident.substring (0, 1));
this.setEqualPt = this.ichToken;
return 0;
}if (this.tokCommand == 554176565 || this.tokCommand == 554176526) {
this.addTokenToPrefix (this.tokenCommand);
this.replaceCommand (J.script.T.tokenSet);
this.tokenAndEquals = J.script.T.getTokenFromName (ident.substring (0, 1));
this.setEqualPt = this.ichToken;
return 0;
}return 2;
case 1150985:
case 364548:
if (this.flowContext != null) this.flowContext.forceEndIf = false;
case 364547:
if (this.nTokens > 0) {
this.isEndOfCommand = true;
this.cchToken = 0;
return 2;
}break;
case 135369224:
if (this.bracketCount > 0) break;
case 102411:
case 102413:
case 102402:
case 135369225:
case 102410:
case 102406:
case 102412:
if (this.nTokens > 1 && this.tokCommand != 1085443) {
this.isEndOfCommand = true;
if (this.flowContext != null) this.flowContext.forceEndIf = true;
this.cchToken = 0;
return 2;
}break;
case 269484225:
case 269484226:
if (!this.isNewSet && this.nTokens == 1) this.checkNewSetCommand ();
if (this.isNewSet && this.parenCount == 0 && this.bracketCount == 0 && this.ichToken <= this.setEqualPt) {
this.tokenizePlusPlus (this.theTok, false);
return 2;
} else if (this.nSemiSkip == this.forPoint3 && this.nTokens == this.ptSemi + 2) {
token = this.lastToken;
this.addTokenToPrefix (J.script.T.tokenEquals);
this.addTokenToPrefix (token);
this.addTokenToPrefix (this.theTok == 269484225 ? J.script.T.tokenMinus : J.script.T.tokenPlus);
this.addTokenToPrefix (J.script.T.i (1));
return 2;
}break;
case 269484436:
if (this.parenCount == 0 && this.bracketCount == 0) this.setEqualPt = this.ichToken;
break;
case 1048584:
if (this.tokCommand == 1085443 && this.parenCount == 0 && this.bracketCount == 0 && this.ichToken < this.setEqualPt) {
this.ltoken.add (1, J.script.T.tokenExpressionBegin);
this.addTokenToPrefix (J.script.T.tokenExpressionEnd);
this.ltoken.set (0, J.script.T.tokenSetProperty);
this.setEqualPt = 0;
}break;
case 1048586:
this.braceCount++;
if (this.braceCount == 1 && this.parenCount == 0 && this.checkFlowStartBrace (false)) {
this.isEndOfCommand = true;
if (this.flowContext != null) this.flowContext.forceEndIf = false;
return 2;
}case 269484048:
this.parenCount++;
if (this.nTokens > 1 && (this.lastToken.tok == 135280132 || this.lastToken.tok == 135369224 || this.lastToken.tok == 135369225)) this.nSemiSkip += 2;
break;
case 1048590:
if (this.iBrace > 0 && this.parenCount == 0 && this.braceCount == 0) {
this.ichBrace = this.ichToken;
if (this.nTokens == 0) {
this.braceCount = this.parenCount = 1;
} else {
this.braceCount = this.parenCount = this.nSemiSkip = 0;
if (this.theToken.tok != 102411 && this.theToken.tok != 102413) this.vBraces.addLast (this.theToken);
this.iBrace++;
this.isEndOfCommand = true;
this.ichEnd = this.ichToken;
return 2;
}}this.braceCount--;
case 269484049:
this.parenCount--;
if (this.parenCount < 0) return this.ERROR (16, ident);
if (this.parenCount == 0) this.nSemiSkip = 0;
if (this.needRightParen) {
this.addTokenToPrefix (J.script.T.tokenRightParen);
this.needRightParen = false;
}break;
case 269484096:
if (this.ichToken > 0 && Character.isWhitespace (this.script.charAt (this.ichToken - 1))) this.addTokenToPrefix (J.script.T.tokenSpaceBeforeSquare);
this.bracketCount++;
break;
case 269484097:
this.bracketCount--;
if (this.bracketCount < 0) return this.ERROR (16, "]");
}
return 0;
}, $fz.isPrivate = true, $fz), "~S");
$_M(c$, "tokenizePlusPlus", 
($fz = function (tok, isPlusPlusX) {
if (isPlusPlusX) {
this.setCommand (J.script.T.tokenSet);
this.ltoken.add (0, this.tokenCommand);
}this.nTokens = this.ltoken.size ();
this.addTokenToPrefix (J.script.T.tokenEquals);
this.setEqualPt = 0;
for (var i = 1; i < this.nTokens; i++) this.addTokenToPrefix (this.ltoken.get (i));

this.addTokenToPrefix (tok == 269484225 ? J.script.T.tokenMinus : J.script.T.tokenPlus);
this.addTokenToPrefix (J.script.T.i (1));
}, $fz.isPrivate = true, $fz), "~N,~B");
$_M(c$, "checkNewSetCommand", 
($fz = function () {
var name = this.ltoken.get (0).value.toString ();
if (!this.isContextVariable (name.toLowerCase ())) return false;
var t = this.setNewSetCommand (false, name);
this.setCommand (J.script.T.tokenSet);
this.ltoken.add (0, this.tokenCommand);
this.ltoken.set (1, t);
return true;
}, $fz.isPrivate = true, $fz));
$_M(c$, "parseCommandParameter", 
($fz = function (ident) {
this.nTokens = this.ltoken.size ();
switch (this.tokCommand) {
case 0:
this.lastToken = J.script.T.tokenOff;
this.ichCurrentCommand = this.ichEnd = this.ichToken;
this.setCommand (this.theToken);
if (J.script.T.tokAttr (this.tokCommand, 102400)) {
this.lastFlowCommand = this.tokenCommand;
}var ret = this.checkFlowEndBrace ();
if (ret == 4) return 4;
 else if (ret == 2) {
this.isEndOfCommand = true;
this.cchToken = 0;
return 2;
}if (J.script.T.tokAttr (this.tokCommand, 102400)) {
if (!this.checkFlowCommand (this.tokenCommand.value)) return 4;
this.theToken = this.tokenCommand;
if (this.theTok == 102411) {
this.addTokenToPrefix (this.tokenCommand);
this.theToken = J.script.T.tokenLeftParen;
}break;
}if (this.theTok == 269484066) {
this.braceCount++;
this.isEndOfCommand = true;
break;
}if (this.theTok == 1048590) {
this.vBraces.addLast (this.tokenCommand);
this.iBrace++;
this.tokCommand = 0;
return 2;
}if (this.theTok != 1048586) this.lastFlowCommand = null;
if (J.script.T.tokAttr (this.tokCommand, 4096)) break;
this.isSetBrace = (this.theTok == 1048586);
if (this.isSetBrace) {
if (!this.lookingAtSetBraceSyntax ()) {
this.isEndOfCommand = true;
if (this.flowContext != null) this.flowContext.forceEndIf = false;
}} else {
switch (this.theTok) {
case 269484226:
case 269484225:
this.tokInitialPlusPlus = this.theTok;
this.tokCommand = 0;
return 2;
case 1073741824:
case 36868:
case 1060866:
case 269484048:
break;
default:
if (!J.script.T.tokAttr (this.theTok, 1073741824) && !J.script.T.tokAttr (this.theTok, 536870912) && !this.isContextVariable (ident)) {
this.commandExpected ();
return 4;
}}
}this.theToken = this.setNewSetCommand (this.isSetBrace, ident);
break;
case 102412:
switch (this.nTokens) {
case 1:
if (this.theTok != 269484048) return this.ERROR (15, "(");
break;
case 2:
if (this.theTok != 269484049) (this.tokenCommand).name0 = ident;
this.addContextVariable (ident);
break;
case 3:
if (this.theTok != 269484049) return this.ERROR (15, ")");
this.isEndOfCommand = true;
this.ichEnd = this.ichToken + 1;
this.flowContext.setLine ();
break;
default:
return this.ERROR (0);
}
break;
case 102436:
case 135368713:
if (this.tokenCommand.intValue == 0) {
if (this.nTokens != 1) break;
this.tokenCommand.value = ident;
return 2;
}if (this.nTokens == 1) {
if (this.thisFunction != null) this.vFunctionStack.add (0, this.thisFunction);
this.thisFunction = (this.tokCommand == 102436 ? J.script.ScriptCompiler.newScriptParallelProcessor (ident, this.tokCommand) :  new J.script.ScriptFunction (ident, this.tokCommand));
this.htUserFunctions.put (ident, Boolean.TRUE);
this.flowContext.setFunction (this.thisFunction);
break;
}if (this.nTokens == 2) {
if (this.theTok != 269484048) return this.ERROR (15, "(");
break;
}if (this.nTokens == 3 && this.theTok == 269484049) break;
if (this.nTokens % 2 == 0) {
if (this.theTok != 269484080 && this.theTok != 269484049) return this.ERROR (15, ")");
break;
}this.thisFunction.addVariable (ident, true);
break;
case 102411:
if (this.nTokens > 1 && this.parenCount == 0 && this.braceCount == 0 && this.theTok == 269484066) {
this.addTokenToPrefix (J.script.T.tokenRightParen);
this.braceCount = 1;
this.isEndOfCommand = true;
this.cchToken = 0;
return 2;
}break;
case 102413:
if (this.nTokens > 1) {
this.braceCount = 1;
this.isEndOfCommand = true;
this.cchToken = 0;
return 2;
}break;
case 364547:
if (this.nTokens == 1 && this.theTok != 135369225) {
this.isEndOfCommand = true;
this.cchToken = 0;
return 2;
}if (this.nTokens != 1 || this.theTok != 135369225 && this.theTok != 1048586) return this.ERROR (0);
this.replaceCommand (this.flowContext.token = J.script.ContextToken.newCmd (102402, "elseif"));
this.tokCommand = 102402;
return 2;
case 36868:
if (this.nTokens != 1) break;
this.addContextVariable (ident);
this.replaceCommand (J.script.T.tokenSetVar);
this.tokCommand = 1085443;
break;
case 1150985:
if (this.nTokens != 1) return this.ERROR (0);
if (!this.checkFlowEnd (this.theTok, ident, this.ichCurrentCommand)) return 4;
if (this.theTok == 135368713 || this.theTok == 102436) {
return 2;
}break;
case 102410:
case 102406:
if (this.nTokens > 2 && this.braceCount == 0 && this.parenCount == 0) {
this.isEndOfCommand = true;
this.ichEnd = this.ichToken + 1;
this.flowContext.setLine ();
}break;
case 102402:
case 135369225:
if (this.nTokens > 2 && this.braceCount == 0 && this.parenCount == 0) {
this.isEndOfCommand = true;
this.ichEnd = this.ichToken + 1;
this.flowContext.setLine ();
}break;
case 102439:
this.isEndOfCommand = true;
this.ichEnd = this.ichToken + 1;
this.flowContext.setLine ();
break;
case 135369224:
if (this.nTokens == 1) {
if (this.theTok != 269484048) return this.ERROR (19, ident);
this.forPoint3 = this.nSemiSkip = 0;
this.nSemiSkip += 2;
} else if (this.nTokens == 3 && this.tokAt (2) == 36868) {
this.addContextVariable (ident);
} else if ((this.nTokens == 3 || this.nTokens == 4) && this.theTok == 1073741980) {
this.nSemiSkip -= 2;
this.forPoint3 = 2;
this.addTokenToPrefix (this.theToken);
this.theToken = J.script.T.tokenLeftParen;
} else if (this.braceCount == 0 && this.parenCount == 0) {
this.isEndOfCommand = true;
this.ichEnd = this.ichToken + 1;
this.flowContext.setLine ();
}break;
case 1085443:
if (this.theTok == 1048586) this.setBraceCount++;
 else if (this.theTok == 1048590) {
this.setBraceCount--;
if (this.isSetBrace && this.setBraceCount == 0 && this.ptNewSetModifier == 2147483647) this.ptNewSetModifier = this.nTokens + 1;
}if (this.nTokens == this.ptNewSetModifier) {
var token = this.tokenAt (0);
if (this.theTok == 269484048 || this.isUserFunction (token.value.toString ())) {
this.ltoken.set (0, this.setCommand (J.script.T.tv (1073741824, 0, token.value)));
this.setBraceCount = 0;
break;
}if (this.theTok != 1073741824 && this.theTok != 269484242 && this.theTok != 1060866 && (!J.script.T.tokAttr (this.theTok, 536870912))) {
if (this.isNewSet) this.commandExpected ();
 else this.errorIntStr2 (18, "SET", ": " + ident);
return 4;
}if (this.nTokens == 1 && (this.lastToken.tok == 269484226 || this.lastToken.tok == 269484225)) {
this.replaceCommand (J.script.T.tokenSet);
this.addTokenToPrefix (this.lastToken);
break;
}}break;
case 135271426:
if (this.theTok == 1060866 && (this.nTokens == 1 || this.lastToken.tok == 1073741940 || this.lastToken.tok == 1073742152)) {
this.addTokenToPrefix (J.script.T.tokenDefineString);
return 2;
}if (this.theTok == 1073741848) this.iHaveQuotedString = false;
break;
case 1610625028:
case 12294:
case 12295:
case 135280132:
case 12291:
case 1060866:
if (this.tokCommand == 1060866) {
if (this.nTokens == 1) {
if (this.theTok != 1073741824) {
if (this.preDefining) {
if (!J.script.T.tokAttr (this.theTok, 3145728)) {
this.errorStr2 ("ERROR IN Token.java or JmolConstants.java -- the following term was used in JmolConstants.java but not listed as predefinedset in Token.java: " + ident, null);
return 4;
}} else if (J.script.T.tokAttr (this.theTok, 3145728)) {
J.util.Logger.warn ("WARNING: predefined term '" + ident + "' has been redefined by the user until the next file load.");
} else if (!this.isCheckOnly && ident.length > 1) {
J.util.Logger.warn ("WARNING: redefining " + ident + "; was " + this.theToken + "not all commands may continue to be functional for the life of the applet!");
this.theTok = this.theToken.tok = 1073741824;
J.script.T.addToken (ident, this.theToken);
}}this.addTokenToPrefix (this.theToken);
this.lastToken = J.script.T.tokenComma;
return 2;
}if (this.nTokens == 2) {
if (this.theTok == 269484436) {
this.ltoken.add (0, J.script.T.tokenSet);
return 2;
}}}if (this.bracketCount == 0 && this.theTok != 1073741824 && !J.script.T.tokAttr (this.theTok, 1048576) && !J.script.T.tokAttr (this.theTok, 1073741824) && (this.theTok & 480) != this.theTok) return this.ERROR (9, ident);
break;
case 12289:
if (this.theTok != 1073741824 && this.theTok != 1048583 && !J.script.T.tokAttr (this.theTok, 1048576)) return this.ERROR (9, ident);
break;
case 135190:
case 135188:
case 135180:
var ch = this.charAt (this.ichToken + this.cchToken);
if (this.parenCount == 0 && this.bracketCount == 0 && ".:/\\+-!?".indexOf (ch) >= 0 && !(ch == '-' && ident.equals ("="))) this.checkUnquotedFileName ();
break;
case 4148:
if (this.nTokens == 2 && this.tokAt (1) == 1073742158 && this.theTok == 269484208) this.implicitString = true;
break;
}
return 0;
}, $fz.isPrivate = true, $fz), "~S");
c$.newScriptParallelProcessor = $_M(c$, "newScriptParallelProcessor", 
($fz = function (name, tok) {
var jpp = J.api.Interface.getOptionInterface ("parallel.ScriptParallelProcessor");
jpp.set (name, tok);
return jpp;
}, $fz.isPrivate = true, $fz), "~S,~N");
$_M(c$, "setNewSetCommand", 
($fz = function (isSetBrace, ident) {
this.tokCommand = 1085443;
this.isNewSet = (!isSetBrace && !this.isUserFunction (ident));
this.setBraceCount = (isSetBrace ? 1 : 0);
this.bracketCount = 0;
this.setEqualPt = 2147483647;
this.ptNewSetModifier = (this.isNewSet ? (ident.equals ("(") ? 2 : 1) : 2147483647);
return ((isSetBrace || this.theToken.tok == 269484226 || this.theToken.tok == 269484225) ? this.theToken : J.script.T.o (1073741824, ident));
}, $fz.isPrivate = true, $fz), "~B,~S");
$_M(c$, "checkUnquotedFileName", 
($fz = function () {
var ichT = this.ichToken;
var ch;
while (++ichT < this.cchScript && !Character.isWhitespace (ch = this.script.charAt (ichT)) && ch != '#' && ch != ';' && ch != '}') {
}
var name = this.script.substring (this.ichToken, ichT).$replace ('\\', '/');
this.cchToken = ichT - this.ichToken;
this.theToken = J.script.T.o (4, name);
}, $fz.isPrivate = true, $fz));
$_M(c$, "checkFlowStartBrace", 
($fz = function (atEnd) {
if ((!J.script.T.tokAttr (this.tokCommand, 102400) || this.tokCommand == 102407 || this.tokCommand == 102408)) return false;
if (atEnd) {
if (this.tokenCommand.tok != 102411 && this.tokenCommand.tok != 102413) {
this.iBrace++;
this.vBraces.addLast (this.tokenCommand);
this.lastFlowCommand = null;
}this.parenCount = this.braceCount = 0;
}return true;
}, $fz.isPrivate = true, $fz), "~B");
$_M(c$, "checkFlowEndBrace", 
($fz = function () {
if (this.iBrace <= 0 || this.vBraces.get (this.iBrace - 1).tok != 1048590) return 0;
this.vBraces.remove (--this.iBrace);
var token = this.vBraces.remove (--this.iBrace);
if (this.theTok == 1048586) {
this.braceCount--;
this.parenCount--;
}if (token.tok == 266280) {
this.vPush.remove (--this.pushCount);
this.addTokenToPrefix (this.setCommand (J.script.ContextToken.newContext (true)));
this.isEndOfCommand = true;
return 2;
}switch (this.flowContext == null ? 0 : this.flowContext.token.tok) {
case 135369225:
case 102402:
case 364547:
if (this.tokCommand == 364547 || this.tokCommand == 102402) return 0;
break;
case 102410:
case 102411:
case 102413:
if (this.tokCommand == 102411 || this.tokCommand == 102413) return 0;
}
return this.forceFlowEnd (token);
}, $fz.isPrivate = true, $fz));
$_M(c$, "forceFlowEnd", 
($fz = function (token) {
var t0 = this.tokenCommand;
this.setCommand (J.script.T.o (1150985, "end"));
if (!this.checkFlowCommand ("end")) return 0;
this.addTokenToPrefix (this.tokenCommand);
switch (token.tok) {
case 135369225:
case 364547:
case 102402:
token = J.script.T.tokenIf;
break;
case 102413:
case 102411:
token = J.script.T.tokenSwitch;
break;
default:
token = J.script.T.getTokenFromName (token.value);
break;
}
if (!this.checkFlowEnd (token.tok, token.value, this.ichBrace)) return 4;
if (token.tok != 135368713 && token.tok != 102436 && token.tok != 364558) this.addTokenToPrefix (token);
this.setCommand (t0);
return 2;
}, $fz.isPrivate = true, $fz), "J.script.T");
c$.isBreakableContext = $_M(c$, "isBreakableContext", 
function (tok) {
return tok == 135369224 || tok == 102439 || tok == 102406 || tok == 102411 || tok == 102413;
}, "~N");
$_M(c$, "checkFlowCommand", 
($fz = function (ident) {
var pt = this.lltoken.size ();
var isEnd = false;
var isNew = true;
switch (this.tokCommand) {
case 135368713:
case 102436:
if (this.flowContext != null) return this.errorStr (1, J.script.T.nameOf (this.tokCommand));
break;
case 1150985:
if (this.flowContext == null) return this.errorStr (1, ident);
isEnd = true;
if (this.flowContext.token.tok != 135368713 && this.flowContext.token.tok != 102436 && this.flowContext.token.tok != 364558) this.setCommand (J.script.T.tv (this.tokCommand, (this.flowContext.ptDefault > 0 ? this.flowContext.ptDefault : -this.flowContext.pt0), ident));
break;
case 364558:
case 102412:
break;
case 135369224:
case 135369225:
case 102439:
case 102410:
case 102406:
break;
case 364548:
isEnd = true;
if (this.flowContext == null || this.flowContext.token.tok != 135369225 && this.flowContext.token.tok != 102439 && this.flowContext.token.tok != 364547 && this.flowContext.token.tok != 102402) return this.errorStr (1, ident);
break;
case 364547:
if (this.flowContext == null || this.flowContext.token.tok != 135369225 && this.flowContext.token.tok != 102402) return this.errorStr (1, ident);
this.flowContext.token.intValue = this.flowContext.setPt0 (pt, false);
break;
case 102407:
case 102408:
isNew = false;
var f = (this.flowContext == null ? null : this.flowContext.getBreakableContext (0));
if (this.tokCommand == 102408) while (f != null && f.token.tok != 135369224 && f.token.tok != 102406) f = f.getParent ();

if (f == null) return this.errorStr (1, ident);
this.setCommand (J.script.T.tv (this.tokCommand, f.pt0, ident));
break;
case 102413:
if (this.flowContext == null || this.flowContext.token.tok != 102410 && this.flowContext.token.tok != 102411 && this.flowContext.ptDefault > 0) return this.errorStr (1, ident);
this.flowContext.token.intValue = this.flowContext.setPt0 (pt, true);
break;
case 102411:
if (this.flowContext == null || this.flowContext.token.tok != 102410 && this.flowContext.token.tok != 102411 && this.flowContext.token.tok != 102413) return this.errorStr (1, ident);
this.flowContext.token.intValue = this.flowContext.setPt0 (pt, false);
break;
case 102402:
if (this.flowContext == null || this.flowContext.token.tok != 135369225 && this.flowContext.token.tok != 102402 && this.flowContext.token.tok != 364547) return this.errorStr (1, "elseif");
this.flowContext.token.intValue = this.flowContext.setPt0 (pt, false);
break;
}
if (isEnd) {
this.flowContext.token.intValue = (this.tokCommand == 102412 ? -pt : pt);
if (this.tokCommand == 364548) this.flowContext = this.flowContext.getParent ();
} else if (isNew) {
var ct = J.script.ContextToken.newCmd (this.tokCommand, this.tokenCommand.value);
if (this.tokCommand == 102410) ct.addName ("_var");
this.setCommand (ct);
switch (this.tokCommand) {
case 364558:
this.flowContext =  new J.script.ScriptFlowContext (this, ct, pt, this.flowContext);
if (this.thisFunction != null) this.vFunctionStack.add (0, this.thisFunction);
this.thisFunction = J.script.ScriptCompiler.newScriptParallelProcessor ("", this.tokCommand);
this.flowContext.setFunction (this.thisFunction);
this.pushCount++;
this.vPush.addLast (ct);
break;
case 364547:
case 102402:
this.flowContext.token = ct;
break;
case 102411:
case 102413:
ct.contextVariables = this.flowContext.token.contextVariables;
this.flowContext.token = ct;
break;
case 102439:
case 135369224:
case 102406:
case 102412:
this.pushCount++;
this.vPush.addLast (ct);
case 135369225:
case 102410:
default:
this.flowContext =  new J.script.ScriptFlowContext (this, ct, pt, this.flowContext);
break;
}
}return true;
}, $fz.isPrivate = true, $fz), "~S");
$_M(c$, "checkFlowEnd", 
($fz = function (tok, ident, pt1) {
if (this.flowContext == null || this.flowContext.token.tok != tok) {
var isOK = true;
switch (tok) {
case 135369225:
isOK = (this.flowContext.token.tok == 364547 || this.flowContext.token.tok == 102402);
break;
case 102410:
isOK = (this.flowContext.token.tok == 102411 || this.flowContext.token.tok == 102413);
break;
default:
isOK = false;
}
if (!isOK) return this.errorStr (1, "end " + ident);
}switch (tok) {
case 135369225:
case 102410:
break;
case 102412:
case 135369224:
case 102439:
case 102406:
this.vPush.remove (--this.pushCount);
break;
case 102436:
case 135368713:
case 364558:
if (!this.isCheckOnly) {
this.addTokenToPrefix (J.script.T.o (tok, this.thisFunction));
J.script.ScriptFunction.setFunction (this.thisFunction, this.script, pt1, this.lltoken.size (), this.lineNumbers, this.lineIndices, this.lltoken);
}this.thisFunction = (this.vFunctionStack.size () == 0 ? null : this.vFunctionStack.remove (0));
this.tokenCommand.intValue = 0;
if (tok == 364558) this.vPush.remove (--this.pushCount);
break;
default:
return this.errorStr (19, "end " + ident);
}
this.flowContext = this.flowContext.getParent ();
return true;
}, $fz.isPrivate = true, $fz), "~N,~S,~N");
$_M(c$, "getData", 
($fz = function (key) {
this.addTokenToPrefix (J.script.T.o (4, key));
this.ichToken += key.length + 2;
if (this.charAt (this.ichToken) == '\r') {
this.lineCurrent++;
this.ichToken++;
}if (this.charAt (this.ichToken) == '\n') {
this.lineCurrent++;
this.ichToken++;
}var i = this.script.indexOf (this.chFirst + key + this.chFirst, this.ichToken) - 4;
if (i < 0 || !this.script.substring (i, i + 4).equalsIgnoreCase ("END ")) return false;
var str = this.script.substring (this.ichToken, i);
this.incrementLineCount (str);
this.addTokenToPrefix (J.script.T.o (135270407, str));
this.addTokenToPrefix (J.script.T.o (1073741824, "end"));
this.addTokenToPrefix (J.script.T.o (4, key));
this.cchToken = i - this.ichToken + key.length + 6;
return true;
}, $fz.isPrivate = true, $fz), "~S");
$_M(c$, "incrementLineCount", 
($fz = function (str) {
var ch;
var pt = str.indexOf ('\r');
var pt2 = str.indexOf ('\n');
if (pt < 0 && pt2 < 0) return 0;
var n = this.lineCurrent;
if (pt < 0 || pt2 < pt) pt = pt2;
for (var i = str.length; --i >= pt; ) {
if ((ch = str.charAt (i)) == '\n' || ch == '\r') this.lineCurrent++;
}
return this.lineCurrent - n;
}, $fz.isPrivate = true, $fz), "~S");
c$.isSpaceOrTab = $_M(c$, "isSpaceOrTab", 
($fz = function (ch) {
return ch == ' ' || ch == '\t';
}, $fz.isPrivate = true, $fz), "~S");
$_M(c$, "eol", 
($fz = function (ch) {
return (ch == '\0' || ch == '\r' || ch == '\n' || ch == ';' && this.nSemiSkip <= 0);
}, $fz.isPrivate = true, $fz), "~S");
$_M(c$, "lookingAtSetBraceSyntax", 
($fz = function () {
var ichT = this.ichToken;
var nParen = 1;
while (++ichT < this.cchScript && nParen > 0) {
switch (this.script.charAt (ichT)) {
case '{':
nParen++;
break;
case '}':
nParen--;
break;
}
}
if (this.charAt (ichT) == '[' && ++nParen == 1) while (++ichT < this.cchScript && nParen > 0) {
switch (this.script.charAt (ichT)) {
case '[':
nParen++;
break;
case ']':
nParen--;
break;
}
}
if (this.charAt (ichT) == '.' && nParen == 0) {
return true;
}return false;
}, $fz.isPrivate = true, $fz));
$_M(c$, "lookingAtString", 
($fz = function (allowPrime) {
if (this.ichToken + 2 > this.cchScript) return false;
this.chFirst = this.script.charAt (this.ichToken);
if (this.chFirst != '"' && (!allowPrime || this.chFirst != '\'')) return false;
var ichT = this.ichToken;
var ch;
var previousCharBackslash = false;
while (++ichT < this.cchScript) {
ch = this.script.charAt (ichT);
if (ch == this.chFirst && !previousCharBackslash) break;
previousCharBackslash = (ch == '\\' ? !previousCharBackslash : false);
}
if (ichT == this.cchScript) {
this.cchToken = -1;
this.ichEnd = this.cchScript;
} else {
this.cchToken = ++ichT - this.ichToken;
}return true;
}, $fz.isPrivate = true, $fz), "~B");
$_M(c$, "getUnescapedStringLiteral", 
($fz = function (isFileName) {
if (isFileName) {
var s = this.script.substring (this.ichToken + 1, this.ichToken + this.cchToken - 1);
if (s.indexOf ("\\u") >= 0) s = J.util.Escape.unescapeUnicode (s);
return s;
}var sb = J.util.SB.newN (this.cchToken - 2);
var ichMax = this.ichToken + this.cchToken - 1;
var ich = this.ichToken + 1;
while (ich < ichMax) {
var ch = this.script.charAt (ich++);
if (ch == '\\' && ich < ichMax) {
ch = this.script.charAt (ich++);
switch (ch) {
case 'n':
ch = '\n';
break;
case 't':
ch = '\t';
break;
case 'r':
ch = '\r';
case '"':
case '\\':
case '\'':
break;
case 'x':
case 'u':
var digitCount = ch == 'x' ? 2 : 4;
if (ich < ichMax) {
var unicode = 0;
for (var k = digitCount; --k >= 0 && ich < ichMax; ) {
var chT = this.script.charAt (ich);
var hexit = J.util.Escape.getHexitValue (chT);
if (hexit < 0) break;
unicode <<= 4;
unicode += hexit;
++ich;
}
ch = String.fromCharCode (unicode);
}}
}sb.appendC (ch);
}
return sb.toString ();
}, $fz.isPrivate = true, $fz), "~B");
$_M(c$, "lookingAtLoadFormat", 
($fz = function () {
var ichT = this.ichToken;
var allchar = J.viewer.Viewer.isDatabaseCode (this.charAt (ichT));
var ch;
while ((Character.isLetterOrDigit (ch = this.charAt (ichT)) && (allchar || Character.isLetter (ch)) || allchar && (!this.eol (ch) && !Character.isWhitespace (ch)))) ++ichT;

if (!allchar && ichT == this.ichToken || !J.script.ScriptCompiler.isSpaceOrTab (ch)) return false;
this.cchToken = ichT - this.ichToken;
return true;
}, $fz.isPrivate = true, $fz));
$_M(c$, "lookingAtImpliedString", 
($fz = function (allowSpace, allowEquals, allowSptParen) {
var ichT = this.ichToken;
var ch = this.script.charAt (ichT);
var isID = (this.lastToken.tok == 1074790550);
var parseVariables = (isID || !J.script.T.tokAttr (this.tokCommand, 20480) && (this.tokCommand & 1) == 1);
var isVariable = (ch == '@');
var isMath = (isVariable && ichT + 3 < this.cchScript && this.script.charAt (ichT + 1) == '{');
if (isMath && parseVariables) {
ichT = J.util.TextFormat.ichMathTerminator (this.script, this.ichToken + 1, this.cchScript);
return (!isID && ichT != this.cchScript && (this.cchToken = ichT + 1 - this.ichToken) > 0);
}var ptSpace = -1;
var ptLastChar = -1;
var isOK = true;
var parenpt = 0;
while (isOK && !this.eol (ch = this.charAt (ichT))) {
switch (ch) {
case '(':
if (!allowSptParen) {
if (ichT >= 5 && (this.script.substring (ichT - 4, ichT).equals (".spt") || this.script.substring (ichT - 4, ichT).equals (".png") || this.script.substring (ichT - 5, ichT).equals (".pngj"))) {
isOK = false;
continue;
}}break;
case '=':
if (!allowEquals) {
isOK = false;
continue;
}break;
case '{':
parenpt++;
break;
case '}':
parenpt--;
if (parenpt < 0 && (this.braceCount > 0 || this.iBrace > 0)) {
isOK = false;
continue;
}break;
default:
if (Character.isWhitespace (ch)) {
if (ptSpace < 0) ptSpace = ichT;
} else {
ptLastChar = ichT;
}break;
}
++ichT;
}
if (allowSpace) ichT = ptLastChar + 1;
 else if (ptSpace > 0) ichT = ptSpace;
if (isVariable && (!allowSpace || ptSpace < 0 && parenpt <= 0 && ichT - this.ichToken > 1)) {
return false;
}return (this.cchToken = ichT - this.ichToken) > 0;
}, $fz.isPrivate = true, $fz), "~B,~B,~B");
$_M(c$, "lookingAtExponential", 
($fz = function () {
if (this.ichToken == this.cchScript) return NaN;
var ichT = this.ichToken;
var pt0 = ichT;
if (this.script.charAt (ichT) == '-') ++ichT;
var isOK = false;
var ch = 'X';
while (Character.isDigit (ch = this.charAt (ichT))) {
++ichT;
isOK = true;
}
if (ichT < this.cchScript && ch == '.') ++ichT;
while (Character.isDigit (ch = this.charAt (ichT))) {
++ichT;
isOK = true;
}
if (ichT == this.cchScript || !isOK) return NaN;
isOK = (ch != 'E' && ch != 'e');
if (isOK || ++ichT == this.cchScript) return NaN;
ch = this.script.charAt (ichT);
if (ch == '-' || ch == '+') ichT++;
while (Character.isDigit (this.charAt (ichT))) {
ichT++;
isOK = true;
}
if (!isOK) return NaN;
this.cchToken = ichT - this.ichToken;
return J.util.Parser.dVal (this.script.substring (pt0, ichT));
}, $fz.isPrivate = true, $fz));
$_M(c$, "lookingAtDecimal", 
($fz = function () {
if (this.ichToken == this.cchScript) return false;
var ichT = this.ichToken;
if (this.script.charAt (ichT) == '-') ++ichT;
var digitSeen = false;
var ch;
while (Character.isDigit (ch = this.charAt (ichT++))) digitSeen = true;

if (ch != '.') return false;
var ch1;
if (!this.eol (ch1 = this.charAt (ichT))) {
if (Character.isLetter (ch1) || ch1 == '?' || ch1 == '*') return false;
if (Character.isLetter (ch1 = this.charAt (ichT + 1)) || ch1 == '?') return false;
}while (Character.isDigit (this.charAt (ichT))) {
++ichT;
digitSeen = true;
}
this.cchToken = ichT - this.ichToken;
return digitSeen;
}, $fz.isPrivate = true, $fz));
$_M(c$, "lookingAtSeqcode", 
($fz = function () {
var ichT = this.ichToken;
var ch;
if (this.charAt (ichT + 1) == '^' && this.script.charAt (ichT) == '*') {
ch = '^';
++ichT;
} else {
if (this.script.charAt (ichT) == '-') ++ichT;
while (Character.isDigit (ch = this.charAt (ichT))) ++ichT;

}if (ch != '^') return false;
ichT++;
if (ichT == this.cchScript) ch = ' ';
 else ch = this.script.charAt (ichT++);
if (ch != ' ' && ch != '*' && ch != '?' && !Character.isLetter (ch)) return false;
this.cchToken = ichT - this.ichToken;
return true;
}, $fz.isPrivate = true, $fz));
$_M(c$, "lookingAtInteger", 
($fz = function () {
if (this.ichToken == this.cchScript) return 2147483647;
var ichT = this.ichToken;
if (this.script.charAt (this.ichToken) == '-') ++ichT;
var ichBeginDigits = ichT;
while (Character.isDigit (this.charAt (ichT))) ++ichT;

if (ichBeginDigits == ichT) return 2147483647;
this.cchToken = ichT - this.ichToken;
try {
var val = Integer.parseInt (this.script.substring (this.ichToken, ichT));
return val;
} catch (e) {
if (Clazz.exceptionOf (e, NumberFormatException)) {
} else {
throw e;
}
}
return 2147483647;
}, $fz.isPrivate = true, $fz));
$_M(c$, "lookingAtBitset", 
function () {
if (this.script.indexOf ("({null})", this.ichToken) == this.ichToken) {
this.cchToken = 8;
return  new J.util.BS ();
}var ichT;
if (this.ichToken + 4 > this.cchScript || this.script.charAt (this.ichToken + 1) != '{' || (ichT = this.script.indexOf ("}", this.ichToken)) < 0 || ichT + 1 == this.cchScript) return null;
var bs = J.util.Escape.uB (this.script.substring (this.ichToken, ichT + 2));
if (bs != null) this.cchToken = ichT + 2 - this.ichToken;
return bs;
});
$_M(c$, "lookingAtObjectID", 
($fz = function () {
var allowWildID = (this.nTokens == 1);
var ichT = this.ichToken;
if (this.charAt (ichT) != '$') return false;
if (this.charAt (++ichT) == '"') return false;
while (ichT < this.cchScript) {
var ch;
if (Character.isWhitespace (ch = this.script.charAt (ichT))) {
if (ichT == this.ichToken + 1) return false;
break;
}if (!Character.isLetterOrDigit (ch)) {
switch (ch) {
default:
return false;
case '*':
if (!allowWildID) return false;
break;
case '~':
case '_':
break;
}
}ichT++;
}
this.cchToken = ichT - (++this.ichToken);
return true;
}, $fz.isPrivate = true, $fz));
$_M(c$, "lookingAtLookupToken", 
($fz = function (ichT) {
if (ichT == this.cchScript) return false;
var ichT0 = ichT;
this.tokLastMath = 0;
var ch;
switch (ch = this.script.charAt (ichT++)) {
case '-':
case '+':
case '&':
case '|':
case '*':
if (ichT < this.cchScript) {
if (this.script.charAt (ichT) == ch) {
++ichT;
if (ch == '-' || ch == '+') break;
if (ch == '&' && this.charAt (ichT) == ch) ++ichT;
} else if (this.script.charAt (ichT) == '=') {
++ichT;
}}this.tokLastMath = 1;
break;
case '/':
if (this.charAt (ichT) == '/') break;
case '\\':
case '!':
if (this.charAt (ichT) == '=') ++ichT;
this.tokLastMath = 1;
break;
case ')':
case ']':
case '}':
case '.':
break;
case '@':
case '{':
this.tokLastMath = 2;
break;
case ':':
this.tokLastMath = 1;
break;
case '(':
case ',':
case '$':
case ';':
case '[':
case '%':
this.tokLastMath = 1;
break;
case '<':
case '=':
case '>':
if ((ch = this.charAt (ichT)) == '<' || ch == '=' || ch == '>') ++ichT;
this.tokLastMath = 1;
break;
default:
if (!Character.isLetter (ch)) return false;
case '~':
case '_':
case '\'':
case '?':
if (ch == '?') this.tokLastMath = 1;
while (Character.isLetterOrDigit (ch = this.charAt (ichT)) || ch == '_' || ch == '?' || ch == '~' || ch == '\'' || ch == '\\' && this.charAt (ichT + 1) == '?' || ch == '^' && ichT > ichT0 && Character.isDigit (this.charAt (ichT - 1))) ++ichT;

break;
}
this.cchToken = ichT - ichT0;
return true;
}, $fz.isPrivate = true, $fz), "~N");
$_M(c$, "lookForSyncID", 
($fz = function () {
var ch;
if ((ch = this.charAt (this.ichToken)) == '"' || ch == '@' || ch == '\0') return false;
var ichT = this.ichToken;
while (!J.script.ScriptCompiler.isSpaceOrTab (ch = this.script.charAt (ichT)) && ch != '#' && ch != '}' && !this.eol (ch)) ++ichT;

this.cchToken = ichT - this.ichToken;
return true;
}, $fz.isPrivate = true, $fz));
$_M(c$, "ERROR", 
($fz = function (error) {
this.errorIntStr2 (error, null, null);
return 4;
}, $fz.isPrivate = true, $fz), "~N");
$_M(c$, "ERROR", 
($fz = function (error, value) {
this.errorStr (error, value);
return 4;
}, $fz.isPrivate = true, $fz), "~N,~S");
$_M(c$, "handleError", 
($fz = function () {
this.errorType = this.errorMessage;
this.errorLine = this.script.substring (this.ichCurrentCommand, this.ichEnd <= this.ichCurrentCommand ? this.ichToken : this.ichEnd);
var lineInfo = (this.ichToken < this.ichEnd ? this.errorLine.substring (0, this.ichToken - this.ichCurrentCommand) + " >>>> " + this.errorLine.substring (this.ichToken - this.ichCurrentCommand) : this.errorLine) + " <<<<";
this.errorMessage = J.i18n.GT._ ("script compiler ERROR: ") + this.errorMessage + J.script.ScriptEvaluator.getErrorLineMessage (null, this.filename, this.lineCurrent, this.iCommand, lineInfo);
if (!this.isSilent) {
this.ichToken = Math.max (this.ichEnd, this.ichToken);
while (!this.lookingAtEndOfLine () && !this.lookingAtTerminator ()) this.ichToken++;

this.errorLine = this.script.substring (this.ichCurrentCommand, this.ichToken);
this.viewer.addCommand (this.errorLine + "#??");
J.util.Logger.error (this.errorMessage);
}return false;
}, $fz.isPrivate = true, $fz));
Clazz.defineStatics (c$,
"OK", 0,
"OK2", 1,
"CONTINUE", 2,
"EOL", 3,
"$ERROR", 4);
});
