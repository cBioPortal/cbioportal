Clazz.declarePackage ("JS");
Clazz.load (["JS.ScriptTokenParser", "JU.Lst"], "JS.ScriptCompiler", ["java.lang.Boolean", "$.Character", "$.Float", "java.util.Hashtable", "JU.AU", "$.BS", "$.M34", "$.M4", "$.PT", "$.SB", "J.api.Interface", "J.i18n.GT", "J.io.JmolBinary", "JM.BondSet", "$.Group", "JS.ContextToken", "$.SV", "$.ScriptContext", "$.ScriptError", "$.ScriptFlowContext", "$.ScriptFunction", "$.ScriptManager", "$.ScriptParam", "$.T", "JU.Escape", "$.Logger", "JV.Viewer"], function () {
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
this.afterWhite = 0;
this.isDotDot = false;
this.ident = null;
this.identLC = null;
this.vPush = null;
this.pushCount = 0;
this.chFirst = '\0';
this.afterMath = 0;
Clazz.instantialize (this, arguments);
}, JS, "ScriptCompiler", JS.ScriptTokenParser);
Clazz.prepareFields (c$, function () {
this.vPush =  new JU.Lst ();
});
Clazz.makeConstructor (c$, 
function (vwr) {
Clazz.superConstructor (this, JS.ScriptCompiler, []);
this.vwr = vwr;
}, "JV.Viewer");
Clazz.defineMethod (c$, "compile", 
function (filename, script, isPredefining, isSilent, debugScript, isCheckOnly) {
this.isCheckOnly = isCheckOnly;
this.filename = filename;
this.isSilent = isSilent;
this.script = script;
this.logMessages = (!isSilent && !isPredefining && debugScript);
this.preDefining = (filename === "#predefine");
var doFull = true;
var isOK = this.compile0 (doFull);
this.atokenInfix = null;
if (!isOK) this.handleError ();
var sc =  new JS.ScriptContext ();
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
sc.vars = this.contextVariables;
return sc;
}, "~S,~S,~B,~B,~B,~B");
Clazz.defineMethod (c$, "newContextVariable", 
 function (ident) {
this.theToken = JS.T.o (1073741824, ident);
if (this.pushCount > 0) {
var ct = this.vPush.get (this.pushCount - 1);
ct.addName (ident);
if (ct.tok != 364558) return;
}if (this.thisFunction == null) {
if (this.contextVariables == null) this.contextVariables =  new java.util.Hashtable ();
JS.ScriptCompiler.addContextVariable (this.contextVariables, ident);
} else {
this.thisFunction.addVariable (ident, false);
}}, "~S");
c$.addContextVariable = Clazz.defineMethod (c$, "addContextVariable", 
function (contextVariables, name) {
contextVariables.put (name, JS.SV.newS ("").setName (name));
}, "java.util.Map,~S");
Clazz.defineMethod (c$, "isContextVariable", 
 function (ident) {
for (var i = this.vPush.size (); --i >= 0; ) {
var ct = this.vPush.get (i);
if (ct.contextVariables != null && ct.contextVariables.containsKey (ident)) return true;
}
return (this.thisFunction != null ? this.thisFunction.isVariable (ident) : this.contextVariables != null && this.contextVariables.containsKey (ident));
}, "~S");
Clazz.defineMethod (c$, "cleanScriptComments", 
 function (script) {
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
}, "~S");
Clazz.defineMethod (c$, "addTokenToPrefix", 
 function (token) {
if (this.logMessages) JU.Logger.info ("addTokenToPrefix" + token);
this.ltoken.addLast (token);
if (token.tok != 0) this.lastToken = token;
}, "JS.T");
Clazz.defineMethod (c$, "compile0", 
 function (isFull) {
this.vFunctionStack =  new JU.Lst ();
this.htUserFunctions =  new java.util.Hashtable ();
this.script = this.cleanScriptComments (this.script);
this.ichToken = this.script.indexOf ("# Jmol state version ");
this.isStateScript = (this.ichToken >= 0);
if (this.isStateScript) {
this.ptSemi = this.script.indexOf (";", this.ichToken);
if (this.ptSemi >= this.ichToken) JS.ScriptManager.setStateScriptVersion (this.vwr, this.script.substring (this.ichToken + "# Jmol state version ".length, this.ptSemi).trim ());
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
this.lastToken = JS.T.tokenOff;
this.vBraces =  new JU.Lst ();
this.vPush =  new JU.Lst ();
this.pushCount = 0;
this.iBrace = 0;
this.braceCount = 0;
this.parenCount = 0;
this.isDotDot = false;
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
this.lltoken =  new JU.Lst ();
this.ltoken =  new JU.Lst ();
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
return (this.flowContext == null || this.errorStr (11, JS.T.nameOf (this.flowContext.token.tok)));
}if (this.nTokens > 0 && !this.isDotDot) {
switch (this.checkSpecialParameterSyntax ()) {
case 2:
continue;
case 4:
return false;
}
}if (this.lookingAtLookupToken (this.ichToken)) {
switch (this.parseKnownToken ()) {
case 2:
continue;
case 4:
return false;
}
switch (this.parseCommandParameter ()) {
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
this.addTokenToPrefix (this.setCommand (JS.T.tokenScript));
this.cchToken = 0;
continue;
}if (this.lookingAtImpliedString (true, true, true)) this.ichEnd = this.ichToken + this.cchToken;
}return this.commandExpected ();
}return this.errorStr (19, this.script.substring (this.ichToken, this.ichToken + 1));
}
}, "~B");
Clazz.defineMethod (c$, "setAaTokenCompiled", 
 function () {
this.aatokenCompiled = this.lltoken.toArray ( new Array (this.lltoken.size ()));
});
Clazz.defineMethod (c$, "lookingAtLeadingWhitespace", 
 function () {
var ichT = this.ichToken;
while (JS.ScriptCompiler.isSpaceOrTab (this.charAt (ichT))) ++ichT;

if (this.isLineContinuation (ichT, true)) ichT += 1 + this.nCharNewLine (ichT + 1);
this.cchToken = ichT - this.ichToken;
if (this.cchToken == 0) return false;
this.afterWhite = ichT;
return true;
});
Clazz.defineMethod (c$, "isLineContinuation", 
 function (ichT, checkMathop) {
var isEscaped = (ichT + 2 < this.cchScript && this.script.charAt (ichT) == '\\' && this.nCharNewLine (ichT + 1) > 0 || checkMathop && this.lookingAtMathContinuation (ichT));
if (isEscaped) this.lineCurrent++;
return isEscaped;
}, "~N,~B");
Clazz.defineMethod (c$, "lookingAtMathContinuation", 
 function (ichT) {
var n;
if ((n = this.nCharNewLine (ichT)) == 0 || this.lastToken.tok == 1048586) return false;
if (this.parenCount > 0 || this.bracketCount > 0) return true;
if ((this.tokCommand != 1085443 || !this.isNewSet) && this.tokCommand != 36865 && this.tokCommand != 36869) return false;
if (this.lastToken.tok == this.tokLastMath) return true;
ichT += n;
while (JS.ScriptCompiler.isSpaceOrTab (this.charAt (ichT))) ++ichT;

return (this.lookingAtLookupToken (ichT) && this.tokLastMath == 1);
}, "~N");
Clazz.defineMethod (c$, "lookingAtEndOfLine", 
 function () {
if (this.ichToken >= this.cchScript) {
this.ichEnd = this.cchScript;
return true;
}return ((this.cchToken = this.nCharNewLine (this.ichEnd = this.ichToken)) > 0);
});
Clazz.defineMethod (c$, "nCharNewLine", 
 function (ichT) {
var ch;
return ((ch = this.charAt (ichT)) != '\r' ? (ch == '\n' ? 1 : 0) : this.charAt (++ichT) == '\n' ? 2 : 1);
}, "~N");
Clazz.defineMethod (c$, "lookingAtTerminator", 
 function () {
var isSemi = (this.script.charAt (this.ichToken) == ';');
if (isSemi && this.nTokens > 0) this.ptSemi = this.nTokens;
if (!isSemi || this.nSemiSkip-- > 0) return false;
this.cchToken = 1;
return true;
});
Clazz.defineMethod (c$, "lookingAtComment", 
 function () {
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
if (isSharp && this.cchScript > this.ichToken + 3 && this.script.charAt (this.ichToken + 1) == 'j' && this.script.charAt (this.ichToken + 2) == 'x' && JS.ScriptCompiler.isSpaceOrTab (this.script.charAt (this.ichToken + 3))) {
this.cchToken = 4;
return 2;
}if (ichT == this.ichToken) return 0;
this.cchToken = ichT - this.ichToken;
return (this.nTokens == 0 ? 1 : 2);
});
Clazz.defineMethod (c$, "charAt", 
 function (i) {
return (i < this.cchScript ? this.script.charAt (i) : '\0');
}, "~N");
Clazz.defineMethod (c$, "processTokenList", 
 function (iLine, doCompile) {
if (this.nTokens > 0 || this.comment != null) {
if (this.nTokens == 0) {
this.ichCurrentCommand = this.ichToken;
if (this.comment != null) {
this.isComment = true;
this.addTokenToPrefix (JS.T.o (0, this.comment));
}} else if (this.setBraceCount > 0 && this.endOfLine && this.ichToken < this.cchScript) {
return 2;
}if (this.wasImpliedScript ()) return 2;
if (this.isNewSet && this.nTokens > 2 && this.tokAt (2) == 1048583 && (this.tokAt (3) == 1276117011 || this.tokAt (3) == 1141899269 || this.tokAt (3) == 1276384259 || this.tokAt (3) == 1276383249)) {
this.ltoken.set (0, JS.T.tokenSet);
this.ltoken.add (1, this.tokAt (3) == 1276383249 ? JS.T.tokenAll : this.ltoken.get (1));
} else if (this.tokInitialPlusPlus != 0) {
if (!this.isNewSet) this.checkNewSetCommand ();
this.tokenizePlusPlus (this.tokInitialPlusPlus, true);
this.ichCurrentCommand -= 2;
}this.iCommand = this.lltoken.size ();
if (this.thisFunction != null && this.thisFunction.cmdpt0 < 0) {
this.thisFunction.cmdpt0 = this.iCommand;
}if (this.nTokens == 1 && this.braceCount == 1) {
if (this.lastFlowCommand == null) {
this.parenCount = this.setBraceCount = this.braceCount = 0;
this.ltoken.remove (0);
this.iBrace++;
var t = JS.ContextToken.newContext (true);
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
this.addTokenToPrefix (JS.T.tokenRightParen);
this.needRightParen = false;
}if (this.tokAt (1) == 1074790550 && JS.T.tokAttr (this.tokCommand, 135168)) {
switch (this.tokAt (2)) {
case 0:
case 4:
case 1060866:
break;
default:
var t = this.ltoken.remove (2);
this.ltoken.add (2, JS.T.o (4, t.tok == 2 ? "" + t.intValue : t.value.toString ()));
}
}if (this.ltoken.size () > 0) {
if (doCompile && !this.compileCommand ()) return 4;
if (this.logMessages) {
JU.Logger.debug ("-------------------------------------");
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
this.lineNumbers = JU.AU.doubleLengthShort (this.lineNumbers);
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
this.setCommand (JS.T.tokenAll);
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
}, "~N,~B");
Clazz.defineMethod (c$, "wasImpliedScript", 
 function () {
if (this.nTokens >= 2 && this.tokCommand == 135271429 && this.checkImpliedScriptCmd) {
var s = (this.nTokens == 2 ? this.lastToken.value.toString ().toUpperCase () : null);
if (this.nTokens > 2 ? !(this.tokAt (2) == 269484048 && this.ltoken.get (1).value.toString ().endsWith (".spt")) : s.endsWith (".SORT") || s.endsWith (".REVERSE") || s.endsWith (".POP") || s.indexOf (".SORT(") >= 0 || s.indexOf (".REVERSE(") >= 0 || s.indexOf (".POP(") >= 0 || s.indexOf (".PUSH(") >= 0 || s.endsWith ("++") || s.endsWith ("--") || s.endsWith ("=") || this.tokInitialPlusPlus != 0) {
this.ichToken = this.ichCurrentCommand;
this.nTokens = 0;
this.ltoken.clear ();
this.cchToken = 0;
this.tokCommand = 0;
return true;
}}return false;
});
Clazz.defineMethod (c$, "compileCommand", 
 function () {
switch (this.ltoken.size ()) {
case 0:
this.atokenInfix =  new Array (0);
return true;
case 4:
if (this.isNewSet && this.tokenAt (2).value.equals (".") && this.tokenAt (3).value.equals ("spt")) {
var fname = this.tokenAt (1).value + "." + this.tokenAt (3).value;
this.ltoken.clear ();
this.addTokenToPrefix (JS.T.tokenScript);
this.addTokenToPrefix (JS.T.o (4, fname));
this.isNewSet = false;
}}
this.setCommand (this.tokenAt (0));
var size = this.ltoken.size ();
if (size == 1 && JS.T.tokAttr (this.tokCommand, 524288)) this.addTokenToPrefix (JS.T.tokenOn);
if (this.tokenAndEquals != null) {
var j;
var i = 0;
for (i = 1; i < size; i++) {
if ((j = this.tokAt (i)) == 269484242) break;
}
size = i;
i++;
if (this.ltoken.size () < i) {
JU.Logger.error ("COMPILER ERROR! - andEquals ");
} else {
for (j = 1; j < size; j++, i++) this.ltoken.add (i, this.tokenAt (j));

this.ltoken.set (size, JS.T.tokenEquals);
this.ltoken.add (i, this.tokenAndEquals);
this.ltoken.add (++i, JS.T.tokenLeftParen);
this.addTokenToPrefix (JS.T.tokenRightParen);
}}this.atokenInfix = this.ltoken.toArray ( new Array (size = this.ltoken.size ()));
if (this.logMessages) {
JU.Logger.debug ("token list:");
for (var i = 0; i < this.atokenInfix.length; i++) JU.Logger.debug (i + ": " + this.atokenInfix[i]);

JU.Logger.debug ("vBraces list:");
for (var i = 0; i < this.vBraces.size (); i++) JU.Logger.debug (i + ": " + this.vBraces.get (i));

JU.Logger.debug ("-------------------------------------");
}return this.compileExpressions ();
});
Clazz.defineMethod (c$, "tokenAt", 
 function (i) {
return this.ltoken.get (i);
}, "~N");
Clazz.overrideMethod (c$, "tokAt", 
function (i) {
return (i < this.ltoken.size () ? this.tokenAt (i).tok : 0);
}, "~N");
Clazz.defineMethod (c$, "setCommand", 
 function (token) {
this.tokenCommand = token;
if (token == null) {
this.tokCommand = 0;
} else {
this.tokCommand = this.tokenCommand.tok;
this.isMathExpressionCommand = (this.tokCommand == 1073741824 || JS.T.tokAttr (this.tokCommand, 36864));
this.isSetOrDefine = (this.tokCommand == 1085443 || this.tokCommand == 1060866);
this.isCommaAsOrAllowed = JS.T.tokAttr (this.tokCommand, 12288);
this.implicitString = JS.T.tokAttr (this.tokCommand, 20480);
}return token;
}, "JS.T");
Clazz.defineMethod (c$, "replaceCommand", 
 function (token) {
this.ltoken.remove (0);
this.ltoken.add (0, this.setCommand (token));
}, "JS.T");
Clazz.defineMethod (c$, "getPrefixToken", 
 function () {
this.ident = this.script.substring (this.ichToken, this.ichToken + this.cchToken);
this.identLC = this.ident.toLowerCase ();
var isUserVar = (this.isContextVariable (this.identLC));
if (this.nTokens == 0) this.isUserToken = isUserVar;
if (this.nTokens == 1 && (this.tokCommand == 135368713 || this.tokCommand == 102436 || this.tokCommand == 36868) || this.nTokens != 0 && isUserVar && this.lastToken.tok != 1048583 || this.isUserFunction (this.identLC) && (this.thisFunction == null || !this.thisFunction.name.equals (this.identLC))) {
this.ident = this.identLC;
this.theToken = null;
} else if (this.ident.length == 1 || this.lastToken.tok == 269484066) {
if ((this.theToken = JS.T.getTokenFromName (this.ident)) == null && (this.theToken = JS.T.getTokenFromName (this.identLC)) != null) this.theToken = JS.T.tv (this.theToken.tok, this.theToken.intValue, this.ident);
} else {
this.theToken = JS.T.getTokenFromName (this.identLC);
if (this.theToken != null && (this.lastToken.tok == 1048583 || this.lastToken.tok == 269484096)) this.theToken = JS.T.o (this.theToken.tok, this.ident);
}if (this.theToken == null) {
if (this.identLC.indexOf ("property_") == 0) this.theToken = JS.T.o (1716520985, this.identLC);
 else this.theToken = JS.T.o (1073741824, this.ident);
}this.theTok = this.theToken.tok;
});
Clazz.defineMethod (c$, "checkSpecialParameterSyntax", 
 function () {
if (this.lookingAtString (!this.implicitString)) {
if (this.cchToken < 0) return this.ERROR (4);
var str = this.getUnescapedStringLiteral (this.lastToken != null && !this.iHaveQuotedString && this.lastToken.tok != 1073741983 && (this.tokCommand == 1085443 && this.nTokens == 2 && this.lastToken.tok == 545259546 || this.tokCommand == 135271426 || this.tokCommand == 1610616835 || this.tokCommand == 135271429));
this.iHaveQuotedString = true;
if (this.tokCommand == 135271426 && this.lastToken.tok == 135270408 || this.tokCommand == 135270408 && str.indexOf ("@") < 0) {
if (!this.getData (str)) {
return this.ERROR (11, "data");
}} else {
this.addTokenToPrefix (JS.T.o (4, str));
if (this.implicitString) {
this.ichEnd = this.ichToken + this.cchToken;
this.isEndOfCommand = true;
}}return 2;
}var ch;
if (this.nTokens == this.ptNewSetModifier) {
ch = this.script.charAt (this.ichToken);
var isAndEquals = ("+-\\*/&|=".indexOf (ch) >= 0);
var isOperation = (isAndEquals || ch == '.' || ch == '[');
var ch2 = this.charAt (this.ichToken + 1);
if (!this.isNewSet && this.isUserToken && isOperation && (ch == '=' || ch2 == ch || ch2 == '=')) {
this.isNewSet = true;
}if (this.isNewSet || this.tokCommand == 1085443 || JS.T.tokAttr (this.tokCommand, 536870912)) {
if (ch == '=') this.setEqualPt = this.ichToken;
if (JS.T.tokAttr (this.tokCommand, 536870912) && ch == '=' || (this.isNewSet || this.isSetBrace) && isOperation) {
this.setCommand (isAndEquals ? JS.T.tokenSet : ch == '[' && !this.isSetBrace || ch == '.' && ch2 == '.' ? JS.T.tokenSetArray : JS.T.tokenSetProperty);
this.ltoken.add (0, this.tokenCommand);
this.cchToken = 1;
switch (ch) {
case '[':
this.addTokenToPrefix (JS.T.o (269484096, "["));
this.bracketCount++;
return 2;
case '.':
if (ch2 == '.') {
this.addTokenToPrefix (JS.T.o (269484096, "["));
this.cchToken = 2;
this.isDotDot = true;
return 2;
}this.addTokenToPrefix (JS.T.o (1048583, "."));
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
this.lastToken = JS.T.tokenMinus;
return 2;
}
}}}switch (this.tokCommand) {
case 135270926:
if (this.nTokens != 2 || this.lastToken.tok != 1073741925 && this.lastToken.tok != 1073742189) return 0;
case 135271426:
case 135271429:
case 1276121098:
if (this.script.charAt (this.ichToken) == '@') {
this.iHaveQuotedString = true;
return 0;
}if (this.tokCommand == 135271426) {
if (this.nTokens == 1 || this.nTokens == 2 && this.tokAt (1) == 1073741839) {
var isDataBase = JV.Viewer.isDatabaseCode (this.charAt (this.ichToken));
if (this.lookingAtLoadFormat (isDataBase)) {
var strFormat = this.script.substring (this.ichToken, this.ichToken + this.cchToken);
var token = JS.T.getTokenFromName (strFormat.toLowerCase ());
switch (token == null ? 0 : token.tok) {
case 1073742015:
case 1073742077:
case 1073741839:
if (this.nTokens != 1) return 4;
case 135270408:
case 1229984263:
case 1073741983:
case 1095766030:
case 135267336:
case 536870926:
case 1073741849:
this.addTokenToPrefix (token);
break;
default:
var tok = (isDataBase ? 4 : JU.PT.isOneOf (strFormat = strFormat.toLowerCase (), ";xyz;vxyz;vibration;temperature;occupancy;partialcharge;") ? 1073741824 : 0);
if (tok != 0) {
this.addTokenToPrefix (JS.T.o (tok, strFormat));
this.iHaveQuotedString = (tok == 4);
}}
return 2;
}}var bs;
if (this.script.charAt (this.ichToken) == '{' || this.parenCount > 0) break;
if ((bs = this.lookingAtBitset ()) != null) {
this.addTokenToPrefix (JS.T.o (10, bs));
return 2;
}}if (!this.iHaveQuotedString && this.lookingAtImpliedString (this.tokCommand == 135270926, this.tokCommand == 135271426, this.nTokens > 1 || this.tokCommand != 135271429)) {
var str = this.script.substring (this.ichToken, this.ichToken + this.cchToken);
if (this.tokCommand == 135271429) {
if (str.startsWith ("javascript:")) {
this.lookingAtImpliedString (true, true, true);
str = this.script.substring (this.ichToken, this.ichToken + this.cchToken);
} else if (str.toUpperCase ().indexOf (".PUSH(") >= 0) {
this.cchToken = 0;
this.iHaveQuotedString = true;
return 2;
}}this.iHaveQuotedString = true;
this.addTokenToPrefix (JS.T.o (4, str));
return 2;
}break;
case 4156:
if (this.nTokens == 1 && this.lookForSyncID ()) {
var ident = this.script.substring (this.ichToken, this.ichToken + this.cchToken);
var iident = JU.PT.parseInt (ident);
if (iident == -2147483648 || Math.abs (iident) < 1000) this.addTokenToPrefix (JS.T.o (1073741824, ident));
 else this.addTokenToPrefix (JS.T.i (iident));
return 2;
}break;
case 135270422:
if (this.nTokens == 2 && this.lastToken.tok == 4115) this.iHaveQuotedString = true;
if (!this.iHaveQuotedString) {
if (this.script.charAt (this.ichToken) == '@') {
this.iHaveQuotedString = true;
return 0;
}if (this.lookingAtImpliedString (true, true, true)) {
var pt = this.cchToken;
var str = this.script.substring (this.ichToken, this.ichToken + this.cchToken);
if (str.indexOf (" ") < 0) {
this.addTokenToPrefix (JS.T.o (4, str));
this.iHaveQuotedString = true;
return 2;
}this.cchToken = pt;
}}break;
}
this.implicitString = new Boolean (this.implicitString & (this.nTokens == 1)).valueOf ();
if (this.implicitString && !(this.tokCommand == 135271429 && this.iHaveQuotedString) && this.lookingAtImpliedString (true, true, true)) {
var str = this.script.substring (this.ichToken, this.ichToken + this.cchToken);
if (this.tokCommand == 1826248716 && JU.PT.isOneOf (str.toLowerCase (), ";on;off;hide;display;")) this.addTokenToPrefix (JS.T.getTokenFromName (str.toLowerCase ()));
 else this.addTokenToPrefix (JS.T.o (4, str));
return 2;
}if (this.lookingAtObjectID ()) {
this.addTokenToPrefix (JS.T.getTokenFromName ("$"));
this.addTokenToPrefix (JS.T.o (1073741824, this.script.substring (this.ichToken, this.ichToken + this.cchToken)));
return 2;
}var value;
if (!Float.isNaN (value = this.lookingAtExponential ())) {
this.addNumber (3, 2147483647, Float.$valueOf (value));
return 2;
}if (this.lookingAtDecimal ()) {
value = JU.PT.fVal (this.script.substring (this.ichToken, this.ichToken + this.cchToken));
var intValue = (JS.ScriptParam.getFloatEncodedInt (this.script.substring (this.ichToken, this.ichToken + this.cchToken)));
this.addNumber (3, intValue, Float.$valueOf (value));
return 2;
}if (this.lookingAtSeqcode ()) {
ch = this.script.charAt (this.ichToken);
try {
var seqNum = (ch == '*' || ch == '^' ? 2147483647 : Integer.parseInt (this.script.substring (this.ichToken, this.ichToken + this.cchToken - 2)));
var insertionCode = this.script.charAt (this.ichToken + this.cchToken - 1);
if (insertionCode == '^') insertionCode = ' ';
if (seqNum < 0) {
seqNum = -seqNum;
this.addTokenToPrefix (JS.T.tokenMinus);
}var seqcode = JM.Group.getSeqcodeFor (seqNum, insertionCode);
this.addTokenToPrefix (JS.T.tv (5, seqcode, "seqcode"));
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
}if (val == 0 && intString.equals ("-0")) this.addTokenToPrefix (JS.T.tokenMinus);
this.addNumber (2, val, intString);
return 2;
}if (!this.isMathExpressionCommand && this.parenCount == 0 || this.lastToken.tok != 1073741824 && !JS.ScriptTokenParser.tokenAttr (this.lastToken, 135266304)) {
var isBondOrMatrix = (this.script.charAt (this.ichToken) == '[');
var bs = this.lookingAtBitset ();
if (bs != null) {
this.addTokenToPrefix (JS.T.o (10, isBondOrMatrix ?  new JM.BondSet (bs) : bs));
return 2;
}if (isBondOrMatrix) {
var m = this.lookingAtMatrix ();
if (Clazz.instanceOf (m, JU.M34)) {
this.addTokenToPrefix (JS.T.o ((Clazz.instanceOf (m, JU.M4) ? 12 : 11), m));
return 2;
}}}return 0;
});
Clazz.defineMethod (c$, "addNumber", 
 function (tok, i, v) {
this.addTokenToPrefix (this.afterWhite == this.ichToken ? JS.SV.newSV (tok, i, v) : JS.T.tv (tok, i, v));
}, "~N,~N,~O");
Clazz.defineMethod (c$, "lookingAtMatrix", 
 function () {
var ipt;
var m;
if (this.ichToken + 4 >= this.cchScript || this.script.charAt (this.ichToken) != '[' || this.script.charAt (this.ichToken + 1) != '[' || (ipt = this.script.indexOf ("]]", this.ichToken)) < 0 || (m = JU.Escape.unescapeMatrix (this.script.substring (this.ichToken, ipt + 2))) == null) return null;
this.cchToken = ipt + 2 - this.ichToken;
return m;
});
Clazz.defineMethod (c$, "parseKnownToken", 
 function () {
this.getPrefixToken ();
var token;
if (this.isDotDot) {
this.addTokenToPrefix (JS.T.o (4, this.ident));
this.addTokenToPrefix (JS.T.o (269484097, "]"));
this.isDotDot = false;
return 2;
}if (this.tokLastMath != 0) this.tokLastMath = this.theTok;
if (this.flowContext != null && this.flowContext.token.tok == 102410 && this.flowContext.$var != null && this.theTok != 102411 && this.theTok != 102413 && this.lastToken.tok != 102410) return this.ERROR (1, this.ident);
if (this.lastToken.tok == 1060866 && this.theTok != 1048586 && this.nTokens != 1) {
this.addTokenToPrefix (JS.T.o (4, this.ident));
return 2;
}switch (this.theTok) {
case 1073741824:
if (this.nTokens == 0 && !this.checkImpliedScriptCmd) {
if (this.ident.charAt (0) == '\'') {
this.addTokenToPrefix (this.setCommand (JS.T.tokenScript));
this.cchToken = 0;
return 2;
}if (this.charAt (this.ichToken + this.cchToken) == '.') {
this.addTokenToPrefix (this.setCommand (JS.T.tokenScript));
this.nTokens = 1;
this.cchToken = 0;
this.checkImpliedScriptCmd = true;
return 2;
}}break;
case 269484242:
if (this.nSemiSkip == this.forPoint3 && this.nTokens == this.ptSemi + 2) {
token = this.lastToken;
this.addTokenToPrefix (JS.T.tokenEquals);
this.addTokenToPrefix (token);
token = JS.T.getTokenFromName (this.ident.substring (0, 1));
this.addTokenToPrefix (token);
this.addTokenToPrefix (JS.T.tokenLeftParen);
this.needRightParen = true;
return 2;
}this.checkNewSetCommand ();
if (this.tokCommand == 1085443) {
this.tokenAndEquals = JS.T.getTokenFromName (this.ident.substring (0, 1));
this.setEqualPt = this.ichToken;
return 0;
}if (this.tokCommand == 554176565 || this.tokCommand == 554176526) {
this.addTokenToPrefix (this.tokenCommand);
this.replaceCommand (JS.T.tokenSet);
this.tokenAndEquals = JS.T.getTokenFromName (this.ident.substring (0, 1));
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
if (this.nTokens > 1 && this.tokCommand != 1085443 && this.nSemiSkip == 0) {
this.isEndOfCommand = true;
if (this.flowContext != null) this.flowContext.forceEndIf = true;
this.cchToken = 0;
return 2;
}break;
case 269484225:
case 269484226:
if (this.afterWhite == this.ichToken || this.afterMath == this.ichToken) this.theToken = JS.T.tv (this.theToken.tok, -1, this.theToken.value);
if (!this.isNewSet && this.nTokens == 1) this.checkNewSetCommand ();
if (this.isNewSet && this.parenCount == 0 && this.bracketCount == 0 && this.ichToken <= this.setEqualPt) {
this.tokenizePlusPlus (this.theTok, false);
return 2;
} else if (this.nSemiSkip == this.forPoint3 && this.nTokens == this.ptSemi + 2) {
token = this.lastToken;
this.addTokenToPrefix (JS.T.tokenEquals);
this.addTokenToPrefix (token);
this.addTokenToPrefix (this.theTok == 269484225 ? JS.T.tokenMinus : JS.T.tokenPlus);
this.addTokenToPrefix (JS.T.i (1));
return 2;
}break;
case 269484436:
if (this.parenCount == 0 && this.bracketCount == 0) this.setEqualPt = this.ichToken;
break;
case 1048583:
if (this.tokCommand == 1085443 && this.parenCount == 0 && this.bracketCount == 0 && this.ichToken < this.setEqualPt && this.ltoken.size () > 1 && this.ltoken.get (1).tok == 1048586) {
this.ltoken.set (0, JS.T.tokenSetProperty);
this.ltoken.add (1, JS.T.tokenExpressionBegin);
this.addTokenToPrefix (JS.T.tokenExpressionEnd);
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
if (!this.wasImpliedScript ()) {
this.braceCount = this.parenCount = this.nSemiSkip = 0;
if (this.theToken.tok != 102411 && this.theToken.tok != 102413) this.vBraces.addLast (this.theToken);
this.iBrace++;
this.isEndOfCommand = true;
this.ichEnd = this.ichToken;
}return 2;
}}this.braceCount--;
case 269484049:
this.parenCount--;
if (this.parenCount < 0) return this.ERROR (16, this.ident);
if (this.parenCount == 0) this.nSemiSkip = 0;
if (this.needRightParen) {
this.addTokenToPrefix (JS.T.tokenRightParen);
this.needRightParen = false;
}break;
case 269484096:
if (this.ichToken > 0 && Character.isWhitespace (this.script.charAt (this.ichToken - 1))) this.addTokenToPrefix (JS.T.tokenSpaceBeforeSquare);
this.bracketCount++;
break;
case 269484097:
this.bracketCount--;
if (this.bracketCount < 0) return this.ERROR (16, "]");
break;
case 1048584:
this.isDotDot = true;
this.addTokenToPrefix (JS.T.o (269484096, "["));
return 2;
}
return 0;
});
Clazz.defineMethod (c$, "tokenizePlusPlus", 
 function (tok, isPlusPlusX) {
if (isPlusPlusX) {
this.setCommand (JS.T.tokenSet);
if (this.nTokens == 1) this.ltoken.add (0, this.tokenCommand);
}this.nTokens = this.ltoken.size ();
this.addTokenToPrefix (JS.T.tokenEquals);
this.setEqualPt = 0;
for (var i = 1; i < this.nTokens; i++) this.addTokenToPrefix (this.ltoken.get (i));

this.addTokenToPrefix (tok == 269484225 ? JS.T.tokenMinus : JS.T.tokenPlus);
this.addTokenToPrefix (JS.T.i (1));
}, "~N,~B");
Clazz.defineMethod (c$, "checkNewSetCommand", 
 function () {
var name = this.ltoken.get (0).value.toString ();
if (!this.isContextVariable (name.toLowerCase ())) return false;
var t = this.setNewSetCommand (false, name);
this.setCommand (JS.T.tokenSet);
this.ltoken.add (0, this.tokenCommand);
this.ltoken.set (1, t);
return true;
});
Clazz.defineMethod (c$, "parseCommandParameter", 
 function () {
this.nTokens = this.ltoken.size ();
switch (this.tokCommand) {
case 0:
this.lastToken = JS.T.tokenOff;
this.ichCurrentCommand = this.ichEnd = this.ichToken;
this.setCommand (this.theToken);
if (JS.T.tokAttr (this.tokCommand, 102400)) {
this.lastFlowCommand = this.tokenCommand;
}var ret = this.checkFlowEndBrace ();
if (ret == 4) return 4;
 else if (ret == 2) {
this.isEndOfCommand = true;
this.cchToken = 0;
return 2;
}if (JS.T.tokAttr (this.tokCommand, 102400)) {
if (!this.checkFlowCommand (this.tokenCommand.value)) return 4;
this.theToken = this.tokenCommand;
if (this.theTok == 102411) {
this.addTokenToPrefix (this.tokenCommand);
this.theToken = JS.T.tokenLeftParen;
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
if (this.theTok == 269484128) {
this.setCommand (this.theToken = JS.T.o (4143, "resume"));
this.addTokenToPrefix (this.theToken);
this.theToken = JS.T.o (14, "context");
return 0;
}if (JS.T.tokAttr (this.tokCommand, 4096)) break;
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
if (!JS.T.tokAttr (this.theTok, 1073741824) && !JS.T.tokAttr (this.theTok, 536870912) && !this.isContextVariable (this.identLC)) {
this.commandExpected ();
return 4;
}}
}this.theToken = this.setNewSetCommand (this.isSetBrace, this.ident);
break;
case 102412:
switch (this.nTokens) {
case 1:
if (this.theTok != 269484048) return this.ERROR (15, "(");
break;
case 2:
if (this.theTok != 269484049) (this.tokenCommand).name0 = this.ident;
this.newContextVariable (this.ident);
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
this.tokenCommand.value = this.ident;
return 2;
}if (this.nTokens == 1) {
if (this.thisFunction != null) this.vFunctionStack.add (0, this.thisFunction);
this.thisFunction = (this.tokCommand == 102436 ? JS.ScriptCompiler.newScriptParallelProcessor (this.ident, this.tokCommand) :  new JS.ScriptFunction (this.ident, this.tokCommand));
this.htUserFunctions.put (this.ident, Boolean.TRUE);
this.flowContext.setFunction (this.thisFunction);
break;
}if (this.nTokens == 2) {
if (this.theTok != 269484048) return this.ERROR (15, "(");
break;
}if (this.nTokens == 3 && this.theTok == 269484049) break;
if (this.nTokens % 2 == 0) {
if (this.theTok != 269484080 && this.theTok != 269484049) return this.ERROR (15, ")");
break;
}this.thisFunction.addVariable (this.ident, true);
break;
case 102411:
if (this.nTokens > 1 && this.parenCount == 0 && this.braceCount == 0 && this.theTok == 269484066) {
this.addTokenToPrefix (JS.T.tokenRightParen);
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
this.replaceCommand (this.flowContext.token = JS.ContextToken.newCmd (102402, "elseif"));
this.tokCommand = 102402;
return 2;
case 1150985:
if (this.nTokens != 1) return this.ERROR (0);
if (!this.checkFlowEnd (this.theTok, this.ident, this.ichCurrentCommand)) return 4;
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
if (this.theTok != 269484048) return this.ERROR (19, this.ident);
this.forPoint3 = this.nSemiSkip = 0;
this.nSemiSkip += 2;
} else if (this.nTokens == 3 && this.tokAt (2) == 36868) {
this.newContextVariable (this.ident);
} else if ((this.nTokens == 3 || this.nTokens == 4) && this.theTok == 1276116993) {
this.nSemiSkip -= 2;
this.forPoint3 = 2;
this.addTokenToPrefix (this.theToken);
this.theToken = JS.T.tokenLeftParen;
} else if (this.braceCount == 0 && this.parenCount == 0) {
this.isEndOfCommand = true;
this.ichEnd = this.ichToken + 1;
this.flowContext.setLine ();
}break;
case 1085443:
case 36868:
if (this.tokCommand == 36868) {
if (this.nTokens == 1) {
this.replaceCommand (JS.T.tokenSetVar);
this.newContextVariable (this.ident);
break;
} else if (this.ident.equals (",")) {
return 2;
} else if (!Character.isLetter (this.ident.charAt (0))) {
if (this.nTokens != 2) return this.ERROR (0);
this.replaceCommand (JS.T.tokenSet);
} else {
this.newContextVariable (this.ident);
break;
}}if (this.theTok == 1048586) this.setBraceCount++;
 else if (this.theTok == 1048590) {
this.setBraceCount--;
if (this.isSetBrace && this.setBraceCount == 0 && this.ptNewSetModifier == 2147483647) this.ptNewSetModifier = this.nTokens + 1;
}if (this.nTokens == this.ptNewSetModifier) {
var token = this.tokenAt (0);
if (this.theTok == 269484048 || this.isUserFunction (token.value.toString ())) {
this.ltoken.set (0, this.setCommand (JS.T.tv (1073741824, 0, token.value)));
this.setBraceCount = 0;
break;
}if (this.theTok != 1073741824 && this.theTok != 269484242 && this.theTok != 1060866 && (!JS.T.tokAttr (this.theTok, 536870912))) {
if (this.isNewSet) this.commandExpected ();
 else this.errorIntStr2 (18, "SET", ": " + this.ident);
return 4;
}if (this.nTokens == 1 && (this.lastToken.tok == 269484226 || this.lastToken.tok == 269484225)) {
this.replaceCommand (JS.T.tokenSet);
this.addTokenToPrefix (this.lastToken);
break;
}}break;
case 135271426:
if (this.theTok == 1060866 && (this.nTokens == 1 || this.lastToken.tok == 1073741940 || this.lastToken.tok == 1073742152)) {
this.addTokenToPrefix (JS.T.tokenDefineString);
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
if (!JS.T.tokAttr (this.theTok, 3145728)) {
this.errorStr2 ("ERROR IN Token.java or JmolConstants.java -- the following term was used in JmolConstants.java but not listed as predefinedset in Token.java: " + this.ident, null);
return 4;
}} else if (JS.T.tokAttr (this.theTok, 3145728)) {
JU.Logger.warn ("WARNING: predefined term '" + this.ident + "' has been redefined by the user until the next file load.");
} else if (!this.isCheckOnly && this.ident.length > 1) {
JU.Logger.warn ("WARNING: redefining " + this.ident + "; was " + this.theToken + "not all commands may continue to be functional for the life of the applet!");
this.theTok = this.theToken.tok = 1073741824;
JS.T.addToken (this.ident, this.theToken);
}}this.addTokenToPrefix (this.theToken);
this.lastToken = JS.T.tokenComma;
return 2;
}if (this.nTokens == 2) {
if (this.theTok == 269484436) {
this.ltoken.add (0, JS.T.tokenSet);
return 2;
}}}if (this.bracketCount == 0 && this.theTok != 1073741824 && !JS.T.tokAttr (this.theTok, 1048576) && !JS.T.tokAttr (this.theTok, 1073741824) && (this.theTok & 480) != this.theTok) return this.ERROR (9, this.ident);
break;
case 12289:
if (this.theTok != 1073741824 && this.theTok != 1048582 && !JS.T.tokAttr (this.theTok, 1048576)) return this.ERROR (9, this.ident);
break;
case 135190:
case 135188:
case 135180:
var ch = this.charAt (this.ichToken + this.cchToken);
if (this.parenCount == 0 && this.bracketCount == 0 && ".:/\\+-!?".indexOf (ch) >= 0 && !(ch == '-' && this.ident.equals ("="))) this.checkUnquotedFileName ();
break;
case 135270926:
if (this.nTokens == 2 && this.tokAt (1) == 1073742158 && this.theTok == 269484208) this.implicitString = true;
break;
}
return 0;
});
c$.newScriptParallelProcessor = Clazz.defineMethod (c$, "newScriptParallelProcessor", 
 function (name, tok) {
var jpp = J.api.Interface.getInterface ("JS.ScriptParallelProcessor");
jpp.set (name, tok);
return jpp;
}, "~S,~N");
Clazz.defineMethod (c$, "setNewSetCommand", 
 function (isSetBrace, ident) {
this.tokCommand = 1085443;
this.isNewSet = (!isSetBrace && !this.isUserFunction (ident));
this.setBraceCount = (isSetBrace ? 1 : 0);
this.bracketCount = 0;
this.setEqualPt = 2147483647;
this.ptNewSetModifier = (this.isNewSet ? (ident.equals ("(") ? 2 : 1) : 2147483647);
return ((isSetBrace || this.theToken.tok == 536870918 || this.theToken.tok == 269484226 || this.theToken.tok == 269484225) ? this.theToken : JS.T.o (1073741824, ident));
}, "~B,~S");
Clazz.defineMethod (c$, "checkUnquotedFileName", 
 function () {
var ichT = this.ichToken;
var ch;
while (++ichT < this.cchScript && !Character.isWhitespace (ch = this.script.charAt (ichT)) && ch != '#' && ch != ';' && ch != '}') {
}
var name = this.script.substring (this.ichToken, ichT).$replace ('\\', '/');
this.cchToken = ichT - this.ichToken;
this.theToken = JS.T.o (4, name);
});
Clazz.defineMethod (c$, "checkFlowStartBrace", 
 function (atEnd) {
if ((!JS.T.tokAttr (this.tokCommand, 102400) || this.tokCommand == 102407 || this.tokCommand == 102408)) return false;
if (atEnd) {
if (this.tokenCommand.tok != 102411 && this.tokenCommand.tok != 102413) {
this.iBrace++;
this.vBraces.addLast (this.tokenCommand);
this.lastFlowCommand = null;
}this.parenCount = this.braceCount = 0;
}return true;
}, "~B");
Clazz.defineMethod (c$, "checkFlowEndBrace", 
 function () {
if (this.iBrace <= 0 || this.vBraces.get (this.iBrace - 1).tok != 1048590) return 0;
this.vBraces.remove (--this.iBrace);
var token = this.vBraces.remove (--this.iBrace);
if (this.theTok == 1048586) {
this.braceCount--;
this.parenCount--;
}if (token.tok == 1276384259) {
this.vPush.remove (--this.pushCount);
this.addTokenToPrefix (this.setCommand (JS.ContextToken.newContext (false)));
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
});
Clazz.defineMethod (c$, "forceFlowEnd", 
 function (token) {
var t0 = this.tokenCommand;
this.setCommand (JS.T.o (1150985, "end"));
if (!this.checkFlowCommand ("end")) return 0;
this.addTokenToPrefix (this.tokenCommand);
switch (token.tok) {
case 135369225:
case 364547:
case 102402:
token = JS.T.tokenIf;
break;
case 102413:
case 102411:
token = JS.T.tokenSwitch;
break;
default:
token = JS.T.getTokenFromName (token.value);
break;
}
if (!this.checkFlowEnd (token.tok, token.value, this.ichBrace)) return 4;
if (token.tok != 135368713 && token.tok != 102436 && token.tok != 364558) this.addTokenToPrefix (token);
this.setCommand (t0);
return 2;
}, "JS.T");
c$.isBreakableContext = Clazz.defineMethod (c$, "isBreakableContext", 
function (tok) {
return tok == 135369224 || tok == 102439 || tok == 102406 || tok == 102411 || tok == 102413;
}, "~N");
Clazz.defineMethod (c$, "checkFlowCommand", 
 function (ident) {
var pt = this.lltoken.size ();
var isEnd = false;
var isNew = true;
switch (this.tokCommand) {
case 135368713:
case 102436:
if (this.flowContext != null) return this.errorStr (1, JS.T.nameOf (this.tokCommand));
break;
case 1150985:
if (this.flowContext == null) return this.errorStr (1, ident);
isEnd = true;
if (this.flowContext.token.tok != 135368713 && this.flowContext.token.tok != 102436 && this.flowContext.token.tok != 364558) this.setCommand (JS.T.tv (this.tokCommand, (this.flowContext.ptDefault > 0 ? this.flowContext.ptDefault : -this.flowContext.pt0), ident));
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
this.setCommand (JS.T.tv (this.tokCommand, f.pt0, ident));
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
var ct = JS.ContextToken.newCmd (this.tokCommand, this.tokenCommand.value);
if (this.tokCommand == 102410) ct.addName ("_var");
this.setCommand (ct);
switch (this.tokCommand) {
case 364558:
this.flowContext =  new JS.ScriptFlowContext (this, ct, pt, this.flowContext);
if (this.thisFunction != null) this.vFunctionStack.add (0, this.thisFunction);
this.thisFunction = JS.ScriptCompiler.newScriptParallelProcessor ("", this.tokCommand);
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
this.flowContext =  new JS.ScriptFlowContext (this, ct, pt, this.flowContext);
break;
}
}return true;
}, "~S");
Clazz.defineMethod (c$, "checkFlowEnd", 
 function (tok, ident, pt1) {
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
this.addTokenToPrefix (JS.T.o (tok, this.thisFunction));
JS.ScriptFunction.setFunction (this.thisFunction, this.script, pt1, this.lltoken.size (), this.lineNumbers, this.lineIndices, this.lltoken);
}this.thisFunction = (this.vFunctionStack.size () == 0 ? null : this.vFunctionStack.remove (0));
this.tokenCommand.intValue = 0;
if (tok == 364558) this.vPush.remove (--this.pushCount);
break;
default:
return this.errorStr (19, "end " + ident);
}
this.flowContext = this.flowContext.getParent ();
return true;
}, "~N,~S,~N");
Clazz.defineMethod (c$, "getData", 
 function (key) {
this.addTokenToPrefix (JS.T.o (4, key));
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
this.addTokenToPrefix (JS.T.o (135270408, str));
this.addTokenToPrefix (JS.T.o (1073741824, "end"));
this.addTokenToPrefix (JS.T.o (4, key));
this.cchToken = i - this.ichToken + key.length + 6;
return true;
}, "~S");
Clazz.defineMethod (c$, "incrementLineCount", 
 function (str) {
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
}, "~S");
c$.isSpaceOrTab = Clazz.defineMethod (c$, "isSpaceOrTab", 
 function (ch) {
return ch == ' ' || ch == '\t';
}, "~S");
Clazz.defineMethod (c$, "eol", 
 function (ch) {
return (ch == '\0' || ch == '\r' || ch == '\n' || ch == ';' && this.nSemiSkip <= 0);
}, "~S");
Clazz.defineMethod (c$, "lookingAtSetBraceSyntax", 
 function () {
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
if (this.charAt (ichT + 1) == '[') ichT++;
 else nParen--;
break;
}
}
if (this.charAt (ichT) == '.' && nParen == 0) {
return true;
}return false;
});
Clazz.defineMethod (c$, "lookingAtString", 
 function (allowPrime) {
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
}, "~B");
Clazz.defineMethod (c$, "getUnescapedStringLiteral", 
 function (isFileName) {
if (isFileName) {
var s = this.script.substring (this.ichToken + 1, this.ichToken + this.cchToken - 1);
if (s.indexOf ("\\u") >= 0) s = JU.Escape.unescapeUnicode (s);
if (s.indexOf (";base64,") != 0) return s;
}var sb = JU.SB.newN (this.cchToken - 2);
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
var hexit = JU.Escape.getHexitValue (chT);
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
}, "~B");
Clazz.defineMethod (c$, "lookingAtLoadFormat", 
 function (allchar) {
var ichT = this.ichToken;
var ch;
while ((Character.isLetterOrDigit (ch = this.charAt (ichT)) && (allchar || Character.isLetter (ch)) || allchar && (!this.eol (ch) && !Character.isWhitespace (ch)))) ++ichT;

if (!allchar && ichT == this.ichToken || !JS.ScriptCompiler.isSpaceOrTab (ch)) return false;
this.cchToken = ichT - this.ichToken;
return true;
}, "~B");
Clazz.defineMethod (c$, "lookingAtImpliedString", 
 function (allowSpace, allowEquals, allowSptParen) {
var ichT = this.ichToken;
var ch = this.script.charAt (ichT);
var isID = (this.lastToken.tok == 1074790550);
var passVariableToString = (JS.T.tokAttr (this.tokCommand, 20480) && (this.tokCommand & 1) == 1);
var isVariable = (ch == '@');
var isMath = (isVariable && ichT + 3 < this.cchScript && this.script.charAt (ichT + 1) == '{');
if (isMath && (isID || !passVariableToString)) return false;
var ptSpace = -1;
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
}default:
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
}, "~B,~B,~B");
Clazz.defineMethod (c$, "lookingAtExponential", 
 function () {
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
return JU.PT.dVal (this.script.substring (pt0, ichT));
});
Clazz.defineMethod (c$, "lookingAtDecimal", 
 function () {
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
});
Clazz.defineMethod (c$, "lookingAtSeqcode", 
 function () {
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
});
Clazz.defineMethod (c$, "lookingAtInteger", 
 function () {
if (this.ichToken == this.cchScript) return 2147483647;
var ichT = this.ichToken;
if (this.script.charAt (this.ichToken) == '-') ++ichT;
var ichBeginDigits = ichT;
while (Character.isDigit (this.charAt (ichT))) ++ichT;

if (ichBeginDigits == ichT) return 2147483647;
this.cchToken = ichT - this.ichToken;
try {
var val = Integer.parseInt (this.ident = this.script.substring (this.ichToken, ichT));
return val;
} catch (e) {
if (Clazz.exceptionOf (e, NumberFormatException)) {
} else {
throw e;
}
}
return 2147483647;
});
Clazz.defineMethod (c$, "lookingAtBitset", 
function () {
if (this.script.indexOf ("({null})", this.ichToken) == this.ichToken) {
this.cchToken = 8;
return  new JU.BS ();
}var ichT;
if (this.ichToken + 4 > this.cchScript || this.script.charAt (this.ichToken + 1) != '{' || (ichT = this.script.indexOf ("}", this.ichToken)) < 0 || ichT + 1 == this.cchScript) return null;
var bs = JU.BS.unescape (this.script.substring (this.ichToken, ichT + 2));
if (bs != null) this.cchToken = ichT + 2 - this.ichToken;
return bs;
});
Clazz.defineMethod (c$, "lookingAtObjectID", 
 function () {
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
});
Clazz.defineMethod (c$, "lookingAtLookupToken", 
 function (ichT) {
if (ichT == this.cchScript) return false;
var ichT0 = ichT;
this.afterMath = (this.tokLastMath != 0 ? ichT : 0);
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
break;
case '.':
if (this.charAt (ichT) == '.') ++ichT;
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
if (!Character.isLetter (ch) && !this.isDotDot) return false;
case '~':
case '_':
case '\'':
case '?':
if (ch == '?') this.tokLastMath = 1;
while (Character.isLetterOrDigit (ch = this.charAt (ichT)) || ch == '_' || ch == '*' && this.charAt (ichT - 1) == '?' || ch == '?' || ch == '~' || ch == '\'' || ch == '\\' && this.charAt (ichT + 1) == '?' || ch == '^' && ichT > ichT0 && Character.isDigit (this.charAt (ichT - 1))) ++ichT;

break;
}
this.cchToken = ichT - ichT0;
return true;
}, "~N");
Clazz.defineMethod (c$, "lookForSyncID", 
 function () {
var ch;
if ((ch = this.charAt (this.ichToken)) == '"' || ch == '@' || ch == '\0') return false;
var ichT = this.ichToken;
while (!JS.ScriptCompiler.isSpaceOrTab (ch = this.charAt (ichT)) && ch != '#' && ch != '}' && !this.eol (ch)) ++ichT;

this.cchToken = ichT - this.ichToken;
return true;
});
Clazz.defineMethod (c$, "ERROR", 
 function (error) {
this.errorIntStr2 (error, null, null);
return 4;
}, "~N");
Clazz.defineMethod (c$, "ERROR", 
 function (error, value) {
this.errorStr (error, value);
return 4;
}, "~N,~S");
Clazz.defineMethod (c$, "handleError", 
 function () {
this.errorType = this.errorMessage;
this.errorLine = this.script.substring (this.ichCurrentCommand, this.ichEnd <= this.ichCurrentCommand ? this.ichToken : this.ichEnd);
var lineInfo = (this.ichToken < this.ichEnd ? this.errorLine.substring (0, this.ichToken - this.ichCurrentCommand) + " >>>> " + this.errorLine.substring (this.ichToken - this.ichCurrentCommand) : this.errorLine) + " <<<<";
this.errorMessage = J.i18n.GT._ ("script compiler ERROR: ") + this.errorMessage + JS.ScriptError.getErrorLineMessage (null, this.filename, this.lineCurrent, this.iCommand, lineInfo);
if (!this.isSilent) {
this.ichToken = Math.max (this.ichEnd, this.ichToken);
while (!this.lookingAtEndOfLine () && !this.lookingAtTerminator ()) this.ichToken++;

this.errorLine = this.script.substring (this.ichCurrentCommand, this.ichToken);
this.vwr.addCommand (this.errorLine + "#??");
JU.Logger.error (this.errorMessage);
}return false;
});
Clazz.defineStatics (c$,
"OK", 0,
"OK2", 1,
"CONTINUE", 2,
"EOL", 3,
"$ERROR", 4);
});
