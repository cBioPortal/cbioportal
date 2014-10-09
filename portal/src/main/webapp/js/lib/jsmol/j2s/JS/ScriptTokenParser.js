Clazz.declarePackage ("JS");
Clazz.load (null, "JS.ScriptTokenParser", ["java.lang.Float", "JU.Lst", "$.P3", "$.PT", "J.i18n.GT", "JS.ScriptParam", "$.T", "JU.Logger", "$.SimpleUnitCell", "JV.JC"], function () {
c$ = Clazz.decorateAsClass (function () {
this.vwr = null;
this.script = null;
this.isStateScript = false;
this.lineCurrent = 0;
this.iCommand = 0;
this.ichCurrentCommand = 0;
this.ichComment = 0;
this.ichEnd = 0;
this.ichToken = 0;
this.theToken = null;
this.lastFlowCommand = null;
this.tokenCommand = null;
this.lastToken = null;
this.tokenAndEquals = null;
this.theTok = 0;
this.nTokens = 0;
this.tokCommand = 0;
this.ptNewSetModifier = 0;
this.isNewSet = false;
this.logMessages = false;
this.atokenInfix = null;
this.itokenInfix = 0;
this.isSetBrace = false;
this.isMathExpressionCommand = false;
this.isSetOrDefine = false;
this.ltokenPostfix = null;
this.isEmbeddedExpression = false;
this.isCommaAsOrAllowed = false;
this.theValue = null;
this.htUserFunctions = null;
this.haveString = false;
this.residueSpecCodeGenerated = false;
this.errorMessage = null;
this.errorMessageUntranslated = null;
this.errorLine = null;
this.errorType = null;
Clazz.instantialize (this, arguments);
}, JS, "ScriptTokenParser");
Clazz.defineMethod (c$, "compileExpressions", 
function () {
var isScriptExpression = (this.tokCommand == 135271429 && this.tokAt (2) == 269484048);
this.isEmbeddedExpression = (isScriptExpression || (this.tokCommand != 0 && (this.tokCommand != 135368713 && this.tokCommand != 102436 && this.tokCommand != 364558 && this.tokCommand != 102412 || this.tokenCommand.intValue != 2147483647) && this.tokCommand != 1150985 && !JS.T.tokAttr (this.tokCommand, 12288) && (this.nTokens > 2 || !JS.T.tokAttr (this.tokCommand, 20480))));
this.isMathExpressionCommand = (this.tokCommand == 1073741824 || isScriptExpression || JS.T.tokAttr (this.tokCommand, 36864));
var checkExpression = this.isEmbeddedExpression || (JS.T.tokAttr (this.tokCommand, 12288));
if (this.tokAt (1) == 1048582 && JS.T.tokAttr (this.tokCommand, 12288)) checkExpression = false;
if (checkExpression && !this.compileExpression ()) return false;
var size = this.atokenInfix.length;
var nDefined = 0;
for (var i = 1; i < size; i++) {
if (this.tokAt (i) == 1060866) nDefined++;
}
size -= nDefined;
if (this.isNewSet) {
if (size == 1) {
this.atokenInfix[0] = JS.T.tv (135368713, 0, this.atokenInfix[0].value);
this.isNewSet = false;
}}return ((this.isNewSet || this.isSetBrace) && size < this.ptNewSetModifier + 2 ? this.commandExpected () : size == 1 || !JS.T.tokAttr (this.tokCommand, 262144) ? true : this.error (0));
});
Clazz.defineMethod (c$, "compileExpression", 
function () {
var firstToken = (this.isSetOrDefine && !this.isSetBrace ? 2 : 1);
this.ltokenPostfix =  new JU.Lst ();
this.itokenInfix = 0;
var tokenBegin = null;
var tok = this.tokAt (1);
switch (this.tokCommand) {
case 1060866:
if (this.tokAt (1) == 2 && this.tokAt (2) == 1048583 && this.tokAt (4) == 269484436) {
this.tokCommand = 1085443;
this.isSetBrace = true;
this.ptNewSetModifier = 4;
this.isMathExpressionCommand = true;
this.isEmbeddedExpression = true;
this.addTokenToPostfixToken (JS.T.tokenSetProperty);
this.addTokenToPostfixToken (JS.T.tokenExpressionBegin);
this.addNextToken ();
this.addNextToken ();
this.addTokenToPostfixToken (JS.T.tokenExpressionEnd);
firstToken = 0;
}break;
case 12295:
if (tok == 1678770178) firstToken = 2;
break;
case 135280132:
switch (tok) {
case 1048589:
case 1048588:
tok = this.tokAt (++firstToken);
break;
}
case 12294:
case 1610625028:
switch (tok) {
case 1276118017:
case 1073742119:
tok = this.tokAt (++firstToken);
break;
}
if (tok == 1087373318 && !JS.T.tokAttr (this.tokAt (firstToken + 1), 269484032)) firstToken++;
}
for (var i = 0; i < firstToken && this.addNextToken (); i++) {
}
while (this.moreTokens ()) {
if (this.isEmbeddedExpression) {
while (!this.isExpressionNext ()) {
if (this.tokPeekIs (1073741824) && !(this.tokCommand == 135271426 && this.itokenInfix == 1)) {
var name = this.atokenInfix[this.itokenInfix].value;
var t = JS.T.getTokenFromName (name);
if (t != null) if (!this.isMathExpressionCommand && this.lastToken.tok != 1060866 || (this.lastToken.tok == 1048583 || this.tokAt (this.itokenInfix + 1) == 269484048) && !this.isUserFunction (name)) {
this.atokenInfix[this.itokenInfix] = t;
}}if (!this.addNextToken ()) break;
}
if (!this.moreTokens ()) break;
}if (this.lastToken.tok == 1060866) {
if (!this.clauseDefine (true, false)) return false;
continue;
}if (!this.isMathExpressionCommand) this.addTokenToPostfixToken (tokenBegin = JS.T.o (1048577, "implicitExpressionBegin"));
if (!this.clauseOr (this.isCommaAsOrAllowed || !this.isMathExpressionCommand && this.tokPeekIs (269484048))) return false;
if (!this.isMathExpressionCommand && !(this.isEmbeddedExpression && this.lastToken === JS.T.tokenCoordinateEnd)) {
this.addTokenToPostfixToken (JS.T.tokenExpressionEnd);
}if (this.moreTokens ()) {
if (this.tokCommand != 135280132 && this.tokCommand != 12291 && !this.isEmbeddedExpression) return this.error (5);
if (this.tokCommand == 135280132) {
tokenBegin.intValue = 0;
this.tokCommand = 0;
this.isEmbeddedExpression = true;
this.isMathExpressionCommand = true;
this.isCommaAsOrAllowed = false;
}}}
this.atokenInfix = this.ltokenPostfix.toArray ( new Array (this.ltokenPostfix.size ()));
return true;
});
Clazz.defineMethod (c$, "isUserFunction", 
function (name) {
return (!this.isStateScript && (this.vwr.isFunction (name) || this.htUserFunctions.containsKey (name)));
}, "~S");
Clazz.defineMethod (c$, "isExpressionNext", 
 function () {
return this.tokPeekIs (1048586) && !(this.tokAt (this.itokenInfix + 1) == 4 && this.tokAt (this.itokenInfix + 2) == 269484066) || !this.isMathExpressionCommand && this.tokPeekIs (269484048);
});
c$.tokenAttr = Clazz.defineMethod (c$, "tokenAttr", 
function (token, tok) {
return token != null && JS.T.tokAttr (token.tok, tok);
}, "JS.T,~N");
Clazz.defineMethod (c$, "moreTokens", 
 function () {
return (this.itokenInfix < this.atokenInfix.length);
});
Clazz.defineMethod (c$, "tokAt", 
function (i) {
return (i < this.atokenInfix.length ? this.atokenInfix[i].tok : 0);
}, "~N");
Clazz.defineMethod (c$, "tokPeek", 
 function () {
return (this.itokenInfix >= this.atokenInfix.length ? 0 : this.atokenInfix[this.itokenInfix].tok);
});
Clazz.defineMethod (c$, "tokPeekIs", 
 function (tok) {
return (this.tokAt (this.itokenInfix) == tok);
}, "~N");
Clazz.defineMethod (c$, "intPeek", 
 function () {
return (this.itokenInfix >= this.atokenInfix.length ? 2147483647 : this.atokenInfix[this.itokenInfix].intValue);
});
Clazz.defineMethod (c$, "valuePeek", 
 function () {
return (this.moreTokens () ? this.atokenInfix[this.itokenInfix].value : "");
});
Clazz.defineMethod (c$, "tokenNext", 
 function () {
return (this.itokenInfix >= this.atokenInfix.length ? null : this.atokenInfix[this.itokenInfix++]);
});
Clazz.defineMethod (c$, "tokenNextTok", 
 function (tok) {
var token = this.tokenNext ();
return (token != null && token.tok == tok);
}, "~N");
Clazz.defineMethod (c$, "returnToken", 
 function () {
this.itokenInfix--;
return false;
});
Clazz.defineMethod (c$, "getToken", 
 function () {
this.theValue = ((this.theToken = this.tokenNext ()) == null ? null : this.theToken.value);
return this.theToken;
});
Clazz.defineMethod (c$, "isToken", 
 function (tok) {
return this.theToken != null && this.theToken.tok == tok;
}, "~N");
Clazz.defineMethod (c$, "getNumericalToken", 
 function () {
return (this.getToken () != null && (this.isToken (2) || this.isToken (3)));
});
Clazz.defineMethod (c$, "floatValue", 
 function () {
switch (this.theToken.tok) {
case 2:
return this.theToken.intValue;
case 3:
return (this.theValue).floatValue ();
}
return 0;
});
Clazz.defineMethod (c$, "addTokenToPostfix", 
 function (tok, value) {
return this.addTokenToPostfixToken (JS.T.o (tok, value));
}, "~N,~O");
Clazz.defineMethod (c$, "addTokenToPostfixInt", 
 function (tok, intValue, value) {
return this.addTokenToPostfixToken (JS.T.tv (tok, intValue, value));
}, "~N,~N,~O");
Clazz.defineMethod (c$, "addTokenToPostfixToken", 
 function (token) {
if (token == null) return false;
if (this.logMessages) JU.Logger.debug ("addTokenToPostfix" + token);
this.ltokenPostfix.addLast (token);
this.lastToken = token;
return true;
}, "JS.T");
Clazz.defineMethod (c$, "addNextToken", 
 function () {
return this.addTokenToPostfixToken (this.tokenNext ());
});
Clazz.defineMethod (c$, "addNextTokenIf", 
 function (tok) {
return (this.tokPeekIs (tok) && this.addNextToken ());
}, "~N");
Clazz.defineMethod (c$, "addSubstituteTokenIf", 
 function (tok, token) {
if (!this.tokPeekIs (tok)) return false;
this.itokenInfix++;
return this.addTokenToPostfixToken (token);
}, "~N,JS.T");
Clazz.defineMethod (c$, "clauseOr", 
 function (allowComma) {
this.haveString = false;
if (!this.clauseAnd ()) return false;
if (this.isEmbeddedExpression && this.lastToken.tok == 1048578) return true;
var tok;
while ((tok = this.tokPeek ()) == 269484112 || tok == 269484113 || tok == 269484114 || allowComma && tok == 269484080) {
if (tok == 269484080 && !this.haveString) this.addSubstituteTokenIf (269484080, JS.T.tokenOr);
 else this.addNextToken ();
if (!this.clauseAnd ()) return false;
if (allowComma && (this.lastToken.tok == 1048590 || this.lastToken.tok == 10)) this.haveString = true;
}
return true;
}, "~B");
Clazz.defineMethod (c$, "clauseAnd", 
 function () {
if (!this.clauseNot ()) return false;
if (this.isEmbeddedExpression && this.lastToken.tok == 1048578) return true;
while (this.tokPeekIs (269484128)) {
this.addNextToken ();
if (!this.clauseNot ()) return false;
}
return true;
});
Clazz.defineMethod (c$, "clauseNot", 
 function () {
if (this.tokPeekIs (269484144)) {
this.addNextToken ();
return this.clauseNot ();
}return (this.clausePrimitive ());
});
Clazz.defineMethod (c$, "clausePrimitive", 
 function () {
var tok = this.tokPeek ();
switch (tok) {
case 1073742195:
this.itokenInfix++;
return this.clausePrimitive ();
case 0:
return this.error (4);
case 1048579:
case 10:
case 269484208:
case 137363467:
case 3145736:
case 3145735:
case 3145738:
case 1048585:
case 1048587:
case 3145760:
return this.addNextToken ();
case 4:
this.haveString = true;
return this.addNextToken ();
case 3:
return this.addTokenToPostfixInt (1048611, this.fixModelSpec (this.getToken ()), this.theValue);
case 1095761925:
case 1095761926:
return this.clauseCell (tok);
case 135266310:
return this.clauseConnected ();
case 135267335:
case 135267336:
return this.clauseSubstructure ();
case 135266325:
case 135402505:
return this.clauseWithin (tok == 135266325);
case 1060866:
return this.clauseDefine (false, false);
case 1678770178:
case 1746538509:
this.addNextToken ();
if (this.tokPeekIs (10)) this.addNextToken ();
 else if (this.tokPeekIs (1060866)) return this.clauseDefine (false, false);
return true;
case 269484048:
this.addNextToken ();
if (!this.clauseOr (true)) return false;
if (!this.addNextTokenIf (269484049)) return this.errorStr (15, ")");
return this.checkForItemSelector (true);
case 1048586:
return this.checkForCoordinate (this.isMathExpressionCommand);
default:
if (this.clauseResidueSpec ()) return true;
if (this.isError ()) return false;
if (JS.T.tokAttr (tok, 1078984704)) {
var itemp = this.itokenInfix;
var isOK = this.clauseComparator (true);
if (isOK || this.itokenInfix != itemp) return isOK;
if (tok == 1238369286) {
return this.clauseSubstructure ();
}}return this.addNextToken ();
}
});
Clazz.defineMethod (c$, "checkForCoordinate", 
 function (isImplicitExpression) {
var isCoordinate = false;
var pt = this.ltokenPostfix.size ();
if (isImplicitExpression) {
this.addTokenToPostfixToken (JS.T.tokenExpressionBegin);
this.tokenNext ();
} else if (this.isEmbeddedExpression) {
this.tokenNext ();
pt--;
} else {
this.addNextToken ();
}var isHash = this.tokPeekIs (4);
if (isHash) {
isImplicitExpression = false;
this.returnToken ();
this.ltokenPostfix.remove (this.ltokenPostfix.size () - 1);
this.addNextToken ();
var nBrace = 1;
while (nBrace != 0) {
if (this.tokPeekIs (1048586)) {
if (this.isExpressionNext ()) {
this.addTokenToPostfixToken (JS.T.o (1048577, "implicitExpressionBegin"));
if (!this.clauseOr (true)) return false;
if (this.lastToken !== JS.T.tokenCoordinateEnd) {
this.addTokenToPostfixToken (JS.T.tokenExpressionEnd);
}} else {
nBrace++;
}}if (this.tokPeekIs (1048590)) nBrace--;
this.addNextToken ();
}
} else {
if (!this.tokPeekIs (1048590) && !this.clauseOr (false)) return false;
var n = 1;
while (!this.tokPeekIs (1048590)) {
var haveComma = this.addNextTokenIf (269484080);
if (!this.clauseOr (false)) return (haveComma || n < 3 ? false : this.errorStr (15, "}"));
n++;
}
isCoordinate = (n >= 2);
}if (isCoordinate && (isImplicitExpression || this.isEmbeddedExpression)) {
this.ltokenPostfix.set (pt, JS.T.tokenCoordinateBegin);
this.addTokenToPostfixToken (JS.T.tokenCoordinateEnd);
this.tokenNext ();
} else if (isImplicitExpression) {
this.addTokenToPostfixToken (JS.T.tokenExpressionEnd);
this.tokenNext ();
} else if (this.isEmbeddedExpression && !isHash) {
this.tokenNext ();
} else {
this.addNextToken ();
}return this.checkForItemSelector (!isHash);
}, "~B");
Clazz.defineMethod (c$, "checkForItemSelector", 
 function (allowNumeric) {
var tok;
if ((tok = this.tokAt (this.itokenInfix + 1)) == 269484096 || allowNumeric && tok == 1048586) return true;
while (true) {
if (!this.addNextTokenIf (269484096)) break;
if (!this.clauseItemSelector ()) return false;
if (!this.addNextTokenIf (269484097)) return this.errorStr (15, "]");
}
return true;
}, "~B");
Clazz.defineMethod (c$, "clauseWithin", 
 function (isWithin) {
this.addNextToken ();
if (!this.addNextTokenIf (269484048)) return false;
if (this.getToken () == null) return false;
var distance = 3.4028235E38;
var key = null;
var allowComma = isWithin;
var tok;
var tok0 = this.theToken.tok;
if (!isWithin) {
tok = -1;
for (var i = this.itokenInfix; tok != 0; i++) {
switch (tok = this.tokAt (i)) {
case 269484080:
tok = 0;
break;
case 1048586:
case 269484048:
case 269484049:
distance = 100;
this.returnToken ();
tok0 = tok = 0;
break;
}
}
}switch (tok0) {
case 269484192:
if (this.getToken () == null) return false;
if (this.theToken.tok != 2) return this.error (12);
distance = -this.theToken.intValue;
break;
case 2:
case 3:
distance = this.floatValue ();
break;
case 1060866:
this.addTokenToPostfixToken (this.theToken);
if (!this.clauseDefine (true, false)) return false;
key = "";
allowComma = false;
break;
}
if (isWithin && distance == 3.4028235E38) switch (tok0) {
case 1060866:
break;
case 1073741916:
case 135267335:
case 135267336:
case 1238369286:
case 1073741925:
case 1073742189:
this.addTokenToPostfix (4, this.theValue);
if (!this.addNextTokenIf (269484080)) return false;
allowComma = false;
tok = this.tokPeek ();
switch (tok) {
case 0:
return false;
case 4:
this.addNextToken ();
key = "";
break;
case 1060866:
if (!this.clauseDefine (false, true)) return false;
key = "";
break;
default:
return false;
}
break;
case 1048580:
allowComma = false;
case 1087375361:
case 1087375362:
case 1073741864:
case 1679429641:
case 1087373316:
case 1048581:
case 1087375365:
case 1087373318:
case 137363467:
case 1095766030:
case 1095761936:
case 135266319:
case 135267841:
case 1095761937:
case 1087373320:
case 3145760:
case 1095761941:
case 1641025539:
case 4:
case 1649412120:
key = this.theValue;
break;
case 1073741824:
key = (this.theValue).toLowerCase ();
break;
default:
return this.errorIntStr2 (18, "WITHIN", ": " + this.theToken.value);
}
if (key == null) this.addTokenToPostfix (3, Float.$valueOf (distance));
 else if (key.length > 0) this.addTokenToPostfix (4, key);
var done = false;
while (!done) {
if (tok0 != 0 && !this.addNextTokenIf (269484080)) break;
if (tok0 == 0) tok0 = 135402505;
var isCoordOrPlane = false;
tok = this.tokPeek ();
if (isWithin) {
switch (tok0) {
case 2:
case 3:
if (tok == 1048589 || tok == 1048588) {
this.addTokenToPostfixToken (this.getToken ());
if (!this.addNextTokenIf (269484080)) break;
tok = this.tokPeek ();
}break;
}
if (key == null) {
switch (tok) {
case 135267841:
case 1048581:
case 135266319:
isCoordOrPlane = true;
this.addNextToken ();
break;
case 1048582:
this.getToken ();
this.getToken ();
this.addTokenToPostfix (4, "$" + this.theValue);
done = true;
break;
case 1087373318:
case 1649412120:
this.getToken ();
this.addTokenToPostfix (4, JS.T.nameOf (tok));
break;
case 1048586:
this.returnToken ();
isCoordOrPlane = true;
this.addTokenToPostfixToken (JS.T.getTokenFromName (distance == 3.4028235E38 ? "plane" : "coord"));
}
if (!done) this.addNextTokenIf (269484080);
}}tok = this.tokPeek ();
if (done) break;
if (isCoordOrPlane) {
while (!this.tokPeekIs (269484049)) {
switch (this.tokPeek ()) {
case 0:
return this.error (4);
case 269484048:
this.addTokenToPostfixToken (JS.T.tokenExpressionBegin);
this.addNextToken ();
if (!this.clauseOr (false)) return this.errorIntStr2 (18, "WITHIN", ": ?");
if (!this.addNextTokenIf (269484049)) return this.errorStr (15, ", / )");
this.addTokenToPostfixToken (JS.T.tokenExpressionEnd);
break;
case 1060866:
if (!this.clauseDefine (false, false)) return false;
break;
default:
this.addTokenToPostfixToken (this.getToken ());
}
}
} else if (!this.clauseOr (allowComma)) {
}}
if (!this.addNextTokenIf (269484049)) return this.errorStr (15, ")");
return true;
}, "~B");
Clazz.defineMethod (c$, "clauseConnected", 
 function () {
this.addNextToken ();
if (!this.addNextTokenIf (269484048)) {
this.addTokenToPostfixToken (JS.T.tokenLeftParen);
this.addTokenToPostfixToken (JS.T.tokenRightParen);
return true;
}while (true) {
if (this.addNextTokenIf (2)) if (!this.addNextTokenIf (269484080)) break;
if (this.addNextTokenIf (2)) if (!this.addNextTokenIf (269484080)) break;
if (this.addNextTokenIf (3)) if (!this.addNextTokenIf (269484080)) break;
if (this.addNextTokenIf (3)) if (!this.addNextTokenIf (269484080)) break;
var o = this.getToken ().value;
var strOrder = (Clazz.instanceOf (o, String) ? o : " ");
var intType = JS.ScriptParam.getBondOrderFromString (strOrder);
if (intType == 131071) {
this.returnToken ();
} else {
this.addTokenToPostfix (4, strOrder);
if (!this.addNextTokenIf (269484080)) break;
}if (this.addNextTokenIf (269484049)) return true;
if (!this.clauseOr (this.tokPeekIs (269484048))) return false;
if (this.addNextTokenIf (269484049)) return true;
if (!this.addNextTokenIf (269484080)) return false;
if (!this.clauseOr (this.tokPeekIs (269484048))) return false;
break;
}
if (!this.addNextTokenIf (269484049)) return this.errorStr (15, ")");
return true;
});
Clazz.defineMethod (c$, "clauseSubstructure", 
 function () {
this.addNextToken ();
if (!this.addNextTokenIf (269484048)) return false;
if (this.tokPeekIs (1060866)) {
if (!this.clauseDefine (false, true)) return false;
} else if (!this.addNextTokenIf (4)) {
return this.errorStr (15, "\"...\"");
}if (this.addNextTokenIf (269484080)) if (!this.clauseOr (this.tokPeekIs (269484048))) return false;
if (!this.addNextTokenIf (269484049)) return this.errorStr (15, ")");
return true;
});
Clazz.defineMethod (c$, "clauseItemSelector", 
 function () {
var tok;
var nparen = 0;
while ((tok = this.tokPeek ()) != 0 && tok != 269484097) {
this.addNextToken ();
if (tok == 269484096) nparen++;
if (this.tokPeek () == 269484097 && nparen-- > 0) this.addNextToken ();
}
return true;
});
Clazz.defineMethod (c$, "clauseComparator", 
 function (isOptional) {
var tokenAtomProperty = this.tokenNext ();
var tokenComparator = this.tokenNext ();
if (!JS.ScriptTokenParser.tokenAttr (tokenComparator, 269484288)) {
if (!isOptional) return this.errorStr (15, "== != < > <= >=");
if (tokenComparator != null) this.returnToken ();
this.returnToken ();
return false;
}if (JS.ScriptTokenParser.tokenAttr (tokenAtomProperty, 1087373312) && tokenComparator.tok != 269484436 && tokenComparator.tok != 269484438 && tokenComparator.tok != 269484437) return this.errorStr (15, "== !=");
if (this.tokPeek () == 269484096) {
this.getToken ();
this.addTokenToPostfixToken (JS.T.tokenLeftParen);
while (true) {
if (!this.addCompare (tokenAtomProperty, tokenComparator)) return false;
if (this.tokPeek () == 269484080) this.getToken ();
 else if (this.tokPeek () == 269484097) break;
this.addTokenToPostfixToken (tokenComparator.tok == 269484437 ? JS.T.tokenAnd : JS.T.tokenOr);
}
this.getToken ();
this.addTokenToPostfixToken (JS.T.tokenRightParen);
return true;
}return this.addCompare (tokenAtomProperty, tokenComparator);
}, "~B");
Clazz.defineMethod (c$, "addCompare", 
 function (tokenAtomProperty, tokenComparator) {
if (this.getToken () == null) return this.errorStr (17, "" + this.valuePeek ());
var isNegative = (this.isToken (269484192));
if (isNegative && this.getToken () == null) return this.error (12);
switch (this.theToken.tok) {
case 2:
case 3:
case 1073741824:
case 4:
case 1048586:
case 1060866:
break;
default:
if (!JS.T.tokAttr (this.theToken.tok, 1073741824)) return this.error (13);
}
this.addTokenToPostfixInt (tokenComparator.tok, tokenAtomProperty.tok, tokenComparator.value + (isNegative ? " -" : ""));
if (tokenAtomProperty.tok == 1716520985) this.addTokenToPostfixToken (tokenAtomProperty);
if (this.isToken (1048586)) {
this.returnToken ();
return this.clausePrimitive ();
}this.addTokenToPostfixToken (this.theToken);
if (this.theToken.tok == 1060866) return this.clauseDefine (true, false);
return true;
}, "JS.T,JS.T");
Clazz.defineMethod (c$, "clauseCell", 
 function (tok) {
var cell =  new JU.P3 ();
this.tokenNext ();
if (!this.tokenNextTok (269484436)) return this.errorStr (15, "=");
if (this.getToken () == null) return this.error (3);
if (this.isToken (2)) {
JU.SimpleUnitCell.ijkToPoint3f (this.theToken.intValue, cell, 1);
return this.addTokenToPostfix (tok, cell);
}if (!this.isToken (1048586) || !this.getNumericalToken ()) return this.error (3);
cell.x = this.floatValue ();
if (this.tokPeekIs (269484080)) this.tokenNext ();
if (!this.getNumericalToken ()) return this.error (3);
cell.y = this.floatValue ();
if (this.tokPeekIs (269484080)) this.tokenNext ();
if (!this.getNumericalToken () || !this.tokenNextTok (1048590)) return this.error (3);
cell.z = this.floatValue ();
return this.addTokenToPostfix (tok, cell);
}, "~N");
Clazz.defineMethod (c$, "clauseDefine", 
 function (haveToken, forceString) {
if (!haveToken) {
var token = this.tokenNext ();
if (forceString) token = JS.T.tokenDefineString;
this.addTokenToPostfixToken (token);
}if (this.tokPeek () == 0) return this.error (4);
if (!this.addSubstituteTokenIf (1048586, JS.T.tokenExpressionBegin)) return this.addNextToken () && this.checkForItemSelector (true);
while (this.moreTokens () && !this.tokPeekIs (1048590)) {
if (this.tokPeekIs (1048586)) {
if (!this.checkForCoordinate (true)) return false;
} else {
this.addNextToken ();
}}
return this.addSubstituteTokenIf (1048590, JS.T.tokenExpressionEnd) && this.checkForItemSelector (true);
}, "~B,~B");
Clazz.defineMethod (c$, "generateResidueSpecCode", 
 function (token) {
if (this.residueSpecCodeGenerated) this.addTokenToPostfixToken (JS.T.tokenAndSpec);
this.addTokenToPostfixToken (token);
this.residueSpecCodeGenerated = true;
return true;
}, "JS.T");
Clazz.defineMethod (c$, "clauseResidueSpec", 
 function () {
var tok = this.tokPeek ();
this.residueSpecCodeGenerated = false;
var checkResNameSpec = false;
switch (tok) {
case 0:
case 3145732:
case 3145750:
return false;
case 269484066:
case 2:
case 269484210:
case 5:
break;
case 269484209:
case 269484096:
case 1073741824:
checkResNameSpec = true;
break;
default:
if (JS.T.tokAttr (tok, 269484288)) return false;
var str = "" + this.valuePeek ();
checkResNameSpec = (str.length == 2 || str.length == 3);
if (!checkResNameSpec) return false;
}
var specSeen = false;
if (checkResNameSpec) {
if (!this.clauseResNameSpec ()) return false;
specSeen = true;
tok = this.tokPeek ();
if (JS.T.tokAttr (tok, 269484288)) {
this.returnToken ();
this.ltokenPostfix.remove (this.ltokenPostfix.size () - 1);
return false;
}}var wasInteger = false;
if (tok == 269484209 || tok == 2 || tok == 5) {
wasInteger = (tok == 2);
if (this.tokPeekIs (269484209)) this.getToken ();
 else if (!this.clauseSequenceSpec ()) return false;
specSeen = true;
tok = this.tokPeek ();
}if (tok == 269484066 || tok == 269484209 || tok == 1073741824 || tok == 1112541205 || tok == 1112541206 || tok == 1112541207 || tok == 1141899280 || tok == 2 && !wasInteger) {
if (!this.clauseChainSpec (tok)) return false;
specSeen = true;
tok = this.tokPeek ();
}if (tok == 1048583) {
if (!this.clauseAtomSpec ()) return false;
specSeen = true;
tok = this.tokPeek ();
}if (tok == 269484210) {
if (!this.clauseAlternateSpec ()) return false;
specSeen = true;
tok = this.tokPeek ();
}if (tok == 269484066 || tok == 269484208) {
if (!this.clauseModelSpec ()) return false;
specSeen = true;
tok = this.tokPeek ();
}if (!specSeen) return this.error (14);
if (!this.residueSpecCodeGenerated) {
this.addTokenToPostfixToken (JS.T.tokenAll);
}return true;
});
Clazz.defineMethod (c$, "clauseResNameSpec", 
 function () {
this.getToken ();
switch (this.theToken.tok) {
case 269484209:
return true;
case 269484096:
var strSpec = "";
while (this.getToken () != null && !this.isToken (269484097)) strSpec += this.theValue;

if (!this.isToken (269484097)) return false;
if (strSpec === "") return true;
var pt;
if (strSpec.length > 0 && (pt = strSpec.indexOf ("*")) >= 0 && pt != strSpec.length - 1) return this.error (14);
strSpec = strSpec.toUpperCase ();
return this.generateResidueSpecCode (JS.T.o (1048612, strSpec));
default:
var res = this.theValue;
if (this.tokPeekIs (269484209)) {
res = this.theValue + "*";
this.getToken ();
}return this.generateResidueSpecCode (JS.T.o (1073741824, res));
}
});
Clazz.defineMethod (c$, "clauseSequenceSpec", 
 function () {
var seqToken = this.getSequenceCode (false);
if (seqToken == null) return false;
var tok = this.tokPeek ();
if (tok == 269484192 || tok == 2 && this.intPeek () < 0) {
if (tok == 269484192) {
this.tokenNext ();
} else {
var i = -this.intPeek ();
this.tokenNext ().intValue = i;
this.returnToken ();
}seqToken.tok = 1048615;
this.generateResidueSpecCode (seqToken);
return this.addTokenToPostfixToken (this.getSequenceCode (true));
}return this.generateResidueSpecCode (seqToken);
});
Clazz.defineMethod (c$, "getSequenceCode", 
 function (isSecond) {
var seqcode = 2147483647;
var seqvalue = 2147483647;
var tokPeek = this.tokPeek ();
if (tokPeek == 5) seqcode = this.tokenNext ().intValue;
 else if (tokPeek == 2) seqvalue = this.tokenNext ().intValue;
 else if (!isSecond) {
return null;
}return JS.T.tv (1048614, seqvalue, Integer.$valueOf (seqcode));
}, "~B");
Clazz.defineMethod (c$, "clauseChainSpec", 
 function (tok) {
if (tok == 269484066) {
this.tokenNext ();
tok = this.tokPeek ();
if (this.isSpecTerminator (tok)) return this.generateResidueSpecCode (JS.T.tv (1048609, 0, "spec_chain"));
}var chain;
switch (tok) {
case 269484209:
return (this.getToken () != null);
case 2:
this.getToken ();
var val = this.theToken.intValue;
if (val < 0 || val > 9999) return this.error (8);
chain = this.vwr.getChainID ("" + val);
break;
default:
var strChain = "" + this.getToken ().value;
if (strChain.equals ("?")) return true;
chain = this.vwr.getChainID (strChain);
break;
}
return this.generateResidueSpecCode (JS.T.tv (1048609, chain, "spec_chain"));
}, "~N");
Clazz.defineMethod (c$, "isSpecTerminator", 
 function (tok) {
switch (tok) {
case 0:
case 269484208:
case 269484128:
case 269484112:
case 269484144:
case 269484080:
case 269484210:
case 269484049:
case 1048590:
return true;
}
return false;
}, "~N");
Clazz.defineMethod (c$, "clauseAlternateSpec", 
 function () {
this.tokenNext ();
var tok = this.tokPeek ();
if (this.isSpecTerminator (tok)) return this.generateResidueSpecCode (JS.T.o (1048607, null));
var alternate = this.getToken ().value;
switch (this.theToken.tok) {
case 269484209:
case 4:
case 2:
case 1073741824:
break;
default:
return this.error (10);
}
return this.generateResidueSpecCode (JS.T.o (1048607, alternate));
});
Clazz.defineMethod (c$, "clauseModelSpec", 
 function () {
this.getToken ();
if (this.tokPeekIs (269484209)) {
this.getToken ();
return true;
}switch (this.tokPeek ()) {
case 2:
return this.generateResidueSpecCode (JS.T.o (1048610, Integer.$valueOf (this.getToken ().intValue)));
case 3:
return this.generateResidueSpecCode (JS.T.tv (1048610, this.fixModelSpec (this.getToken ()), this.theValue));
case 269484080:
case 1048590:
case 0:
return this.generateResidueSpecCode (JS.T.o (1048610, Integer.$valueOf (1)));
}
return this.error (10);
});
Clazz.defineMethod (c$, "fixModelSpec", 
 function (token) {
var ival = token.intValue;
if (ival == 2147483647) {
var f = (this.theValue).floatValue ();
if (f == Clazz.floatToInt (f)) ival = (Clazz.floatToInt (f)) * 1000000;
if (ival < 0) ival = 2147483647;
}return ival;
}, "JS.T");
Clazz.defineMethod (c$, "clauseAtomSpec", 
 function () {
if (!this.tokenNextTok (1048583)) return this.error (7);
if (this.getToken () == null) return true;
var atomSpec = "";
if (this.isToken (2)) {
atomSpec += "" + this.theToken.intValue;
if (this.getToken () == null) return this.error (7);
}switch (this.theToken.tok) {
case 269484209:
return true;
}
atomSpec += "" + this.theToken.value;
if (this.tokPeekIs (269484209)) {
this.tokenNext ();
atomSpec += "'";
}var atomID = JV.JC.lookupSpecialAtomID (atomSpec.toUpperCase ());
return this.generateResidueSpecCode (JS.T.tv (1048608, atomID, atomSpec));
});
c$.errorString = Clazz.defineMethod (c$, "errorString", 
function (iError, value, more, translated) {
var doTranslate = false;
if (!translated && (doTranslate = J.i18n.GT.getDoTranslate ()) == true) J.i18n.GT.setDoTranslate (false);
var msg;
switch (iError) {
default:
msg = "Unknown compiler error message number: " + iError;
break;
case 0:
msg = J.i18n.GT._ ("bad argument count");
break;
case 1:
msg = J.i18n.GT._ ("invalid context for {0}");
break;
case 2:
msg = J.i18n.GT._ ("command expected");
break;
case 3:
msg = J.i18n.GT._ ("{ number number number } expected");
break;
case 4:
msg = J.i18n.GT._ ("unexpected end of script command");
break;
case 5:
msg = J.i18n.GT._ ("end of expression expected");
break;
case 6:
msg = J.i18n.GT._ ("identifier or residue specification expected");
break;
case 7:
msg = J.i18n.GT._ ("invalid atom specification");
break;
case 8:
msg = J.i18n.GT._ ("invalid chain specification");
break;
case 9:
msg = J.i18n.GT._ ("invalid expression token: {0}");
break;
case 10:
msg = J.i18n.GT._ ("invalid model specification");
break;
case 11:
msg = J.i18n.GT._ ("missing END for {0}");
break;
case 12:
msg = J.i18n.GT._ ("number expected");
break;
case 13:
msg = J.i18n.GT._ ("number or variable name expected");
break;
case 14:
msg = J.i18n.GT._ ("residue specification (ALA, AL?, A*) expected");
break;
case 15:
msg = J.i18n.GT._ ("{0} expected");
break;
case 16:
msg = J.i18n.GT._ ("{0} unexpected");
break;
case 17:
msg = J.i18n.GT._ ("unrecognized expression token: {0}");
break;
case 18:
msg = J.i18n.GT._ ("unrecognized {0} parameter");
break;
case 19:
msg = J.i18n.GT._ ("unrecognized token: {0}");
break;
}
if (msg.indexOf ("{0}") < 0) {
if (value != null) msg += ": " + value;
} else {
msg = JU.PT.rep (msg, "{0}", value);
if (msg.indexOf ("{1}") >= 0) msg = JU.PT.rep (msg, "{1}", more);
 else if (more != null) msg += ": " + more;
}if (!translated) J.i18n.GT.setDoTranslate (doTranslate);
return msg;
}, "~N,~S,~S,~B");
Clazz.defineMethod (c$, "commandExpected", 
function () {
this.ichToken = this.ichCurrentCommand;
return this.error (2);
});
Clazz.defineMethod (c$, "error", 
function (error) {
return this.errorIntStr2 (error, null, null);
}, "~N");
Clazz.defineMethod (c$, "errorStr", 
function (error, value) {
return this.errorIntStr2 (error, value, null);
}, "~N,~S");
Clazz.defineMethod (c$, "errorIntStr2", 
function (iError, value, more) {
var strError = JS.ScriptTokenParser.errorString (iError, value, more, true);
var strUntranslated = (J.i18n.GT.getDoTranslate () ? JS.ScriptTokenParser.errorString (iError, value, more, false) : null);
return this.errorStr2 (strError, strUntranslated);
}, "~N,~S,~S");
Clazz.defineMethod (c$, "isError", 
 function () {
return this.errorMessage != null;
});
Clazz.defineMethod (c$, "errorStr2", 
function (errorMessage, strUntranslated) {
this.errorMessage = errorMessage;
this.errorMessageUntranslated = strUntranslated;
return false;
}, "~S,~S");
Clazz.defineStatics (c$,
"ERROR_badArgumentCount", 0,
"ERROR_badContext", 1,
"ERROR_commandExpected", 2,
"ERROR_endOfCommandUnexpected", 4,
"ERROR_invalidExpressionToken", 9,
"ERROR_missingEnd", 11,
"ERROR_tokenExpected", 15,
"ERROR_tokenUnexpected", 16,
"ERROR_unrecognizedParameter", 18,
"ERROR_unrecognizedToken", 19,
"ERROR_coordinateExpected", 3,
"ERROR_endOfExpressionExpected", 5,
"ERROR_identifierOrResidueSpecificationExpected", 6,
"ERROR_invalidAtomSpecification", 7,
"ERROR_invalidChainSpecification", 8,
"ERROR_invalidModelSpecification", 10,
"ERROR_numberExpected", 12,
"ERROR_numberOrVariableNameExpected", 13,
"ERROR_residueSpecificationExpected", 14,
"ERROR_unrecognizedExpressionToken", 17);
});
