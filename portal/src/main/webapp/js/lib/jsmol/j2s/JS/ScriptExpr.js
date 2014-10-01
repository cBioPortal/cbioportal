Clazz.declarePackage ("JS");
Clazz.load (["JS.ScriptParam"], "JS.ScriptExpr", ["java.lang.Boolean", "$.Float", "java.util.Hashtable", "$.Map", "JU.BArray", "$.BS", "$.CU", "$.Lst", "$.M34", "$.M4", "$.Measure", "$.P3", "$.P4", "$.PT", "$.SB", "J.api.Interface", "JM.Atom", "$.BondSet", "$.Group", "$.ModelSet", "JS.SV", "$.ScriptContext", "$.ScriptMathProcessor", "$.T", "JU.BSUtil", "$.Elements", "$.Escape", "$.Logger", "$.Txt"], function () {
c$ = Clazz.decorateAsClass (function () {
this.debugHigh = false;
this.cmdExt = null;
this.tempStatement = null;
this.ptTemp = null;
Clazz.instantialize (this, arguments);
}, JS, "ScriptExpr", JS.ScriptParam);
Clazz.defineMethod (c$, "getCmdExt", 
function () {
return (this.cmdExt == null ? (this.cmdExt = this.getExt ("Cmd")).init (this) : this.cmdExt);
});
Clazz.defineMethod (c$, "getExt", 
function (type) {
return J.api.Interface.getInterface ("JS." + type + "Ext");
}, "~S");
Clazz.overrideMethod (c$, "parameterExpressionList", 
function (pt, ptAtom, isArrayItem) {
return this.parameterExpression (pt, -1, null, true, true, ptAtom, isArrayItem, null, null, false);
}, "~N,~N,~B");
Clazz.defineMethod (c$, "parameterExpressionString", 
function (pt, ptMax) {
return this.parameterExpression (pt, ptMax, "", true, false, -1, false, null, null, false);
}, "~N,~N");
Clazz.defineMethod (c$, "parameterExpressionBoolean", 
function (pt, ptMax) {
return (this.parameterExpression (pt, ptMax, null, true, false, -1, false, null, null, false)).booleanValue ();
}, "~N,~N");
Clazz.defineMethod (c$, "parameterExpressionToken", 
function (pt) {
var result = this.parameterExpressionList (pt, -1, false);
return (result.size () > 0 ? result.get (0) : JS.SV.newS (""));
}, "~N");
Clazz.defineMethod (c$, "parameterExpressionSelect", 
function (h, where) {
this.st = where;
this.slen = this.st.length;
return (this.parameterExpression (2, -2147483648, null, true, false, -1, false, h, null, false)).booleanValue ();
}, "java.util.Map,~A");
Clazz.defineMethod (c$, "parameterExpression", 
 function (pt, ptMax, key, ignoreComma, asVector, ptAtom, isArrayItem, localVars, localVar, isSpecialAssignment) {
var isImplicitAtomProperty = (localVar != null);
var isOneExpressionOnly = (pt < 0);
var returnBoolean = (!asVector && key == null);
var returnString = (!asVector && key != null && key.length == 0);
if (isOneExpressionOnly) pt = -pt;
var allContext = (localVars == null || ptMax != -2147483648);
if (ptMax < pt) ptMax = this.slen;
var ptEq = (isSpecialAssignment ? 0 : 1);
var rpn =  new JS.ScriptMathProcessor (this, isSpecialAssignment, isArrayItem, asVector, false, false, key);
var v;
var res;
var nSquare = 0;
var nParen = 0;
var topLevel = true;
out : for (var i = pt; i < ptMax; i++) {
v = null;
var tok = this.getToken (i).tok;
if (isImplicitAtomProperty && this.tokAt (i + 1) != 1048583) {
var token = (localVars != null && localVars.containsKey (this.theToken.value) ? null : this.getBitsetPropertySelector (i, false, false));
if (token != null) {
rpn.addX (localVars.get (localVar));
if (!rpn.addOpAllowMath (token, (this.tokAt (i + 1) == 269484048))) this.invArg ();
if ((token.intValue == 135368713 || token.intValue == 102436) && this.tokAt (this.iToken + 1) != 269484048) {
rpn.addOp (JS.T.tokenLeftParen);
rpn.addOp (JS.T.tokenRightParen);
}i = this.iToken;
continue;
}}switch (tok) {
case 269484097:
case 1048590:
if (!ignoreComma && topLevel) break out;
if (tok == 1048590) this.invArg ();
if (isSpecialAssignment && nSquare == 1 && this.tokAt (i + 1) == 269484436) isSpecialAssignment = rpn.endAssignment ();
}
switch (tok) {
case 1060866:
if (this.tokAt (++i) == 1048577) {
v = this.parameterExpressionToken (++i);
i = this.iToken;
} else if (this.tokAt (i) == 2) {
v = this.vwr.ms.getAtoms (1095763969, Integer.$valueOf (this.st[i].intValue));
break;
} else {
v = this.getParameter (JS.SV.sValue (this.st[i]), 1073742190, true);
}v = this.getParameter ((v).asString (), 1073742190, true);
break;
case 135369225:
if (this.getToken (++i).tok != 269484048) this.invArg ();
if (localVars == null) localVars =  new java.util.Hashtable ();
res = this.parameterExpression (++i, -1, null, ignoreComma, false, -1, false, localVars, localVar, false);
var TF = (res).booleanValue ();
var iT = this.iToken;
if (this.getToken (iT++).tok != 1048591) this.invArg ();
this.parameterExpressionBoolean (iT, -1);
var iF = this.iToken;
if (this.tokAt (iF++) != 1048591) this.invArg ();
this.parameterExpression (-iF, -1, null, ignoreComma, false, 1, false, localVars, localVar, false);
var iEnd = this.iToken;
if (this.tokAt (iEnd) != 269484049) this.invArg ();
v = this.parameterExpression (TF ? iT : iF, TF ? iF : iEnd, "XXX", ignoreComma, false, 1, false, localVars, localVar, false);
i = this.iToken = iEnd;
break;
case 135369224:
case 135280132:
var isFunctionOfX = (pt > 0);
var isFor = (isFunctionOfX && tok == 135369224);
var dummy;
if (isFunctionOfX) {
if (this.getToken (++i).tok != 269484048 || !JS.T.tokAttr (this.getToken (++i).tok, 1073741824)) this.invArg ();
dummy = this.paramAsStr (i);
if (this.getToken (++i).tok != 1048591) this.invArg ();
} else {
dummy = "_x";
}v = this.parameterExpressionToken (-(++i)).value;
if (!(Clazz.instanceOf (v, JU.BS))) this.invArg ();
var bsAtoms = v;
i = this.iToken;
if (isFunctionOfX && this.getToken (i++).tok != 1048591) this.invArg ();
var bsSelect =  new JU.BS ();
var bsX =  new JU.BS ();
var sout = (isFor ?  new Array (JU.BSUtil.cardinalityOf (bsAtoms)) : null);
if (localVars == null) localVars =  new java.util.Hashtable ();
bsX.set (0);
var t = JS.SV.newV (10, bsX);
t.index = 0;
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
res = this.parameterExpression (i, pt2, (isFor ? "XXX" : null), ignoreComma, isFor, j, false, localVars, isFunctionOfX ? null : dummy, false);
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
return this.listBS (bsSelect);
}i = this.iToken + 1;
break;
case 1048591:
break out;
case 2:
case 3:
case 1048614:
rpn.addXNum (this.theToken);
break;
case 135266319:
if (this.tokAt (this.iToken + 1) == 269484048) {
if (!rpn.addOpAllowMath (this.theToken, true)) this.invArg ();
break;
}case 1087375362:
case 1087375361:
case 1048580:
case 1679429641:
case 1087373316:
case 1048581:
case 1087375365:
case 1087373318:
case 1095766030:
case 1095761936:
case 1087373320:
case 1095761941:
case 135267335:
case 135267336:
case 1238369286:
case 1641025539:
case 1073741916:
case 1048589:
case 1048588:
case 4:
case 8:
case 9:
case 11:
case 12:
case 7:
case 10:
case 6:
case 14:
rpn.addX (JS.SV.newT (this.theToken));
break;
case 1048582:
this.ignoreError = true;
var ptc;
try {
ptc = this.centerParameter (i);
rpn.addX (JS.SV.newV (8, ptc));
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
if (this.tokAt (i + 1) == 4) {
if (this.tokAt (i + 2) == 1048590) {
v = (this.chk ?  new JU.BS () : this.getAtomBitSet (this.stringParameter (i + 1)));
i += 2;
break;
}v = this.getAssocArray (i);
} else {
v = this.getPointOrPlane (i, false, true, true, false, 3, 4);
}i = this.iToken;
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
if (tok == 1048579) v = this.vwr.getAllAtoms ();
 else v = this.atomExpression (this.st, i, 0, true, true, true, true);
i = this.iToken;
if (nParen == 0 && isOneExpressionOnly) {
this.iToken++;
return this.listBS (v);
}break;
case 1073742195:
rpn.addOp (this.theToken);
continue;
case 1048578:
i++;
break out;
case 269484080:
if (!ignoreComma && topLevel) break out;
if (!rpn.addOp (this.theToken)) this.invArg ();
break;
case 1048584:
case 1048583:
if (isSpecialAssignment && topLevel && this.tokAt (i + 2) == 269484436) isSpecialAssignment = rpn.endAssignment ();
if (ptEq == 0 && topLevel) {
switch (this.tokAt (i + 1)) {
case 0:
break;
case 1141899270:
case 1141899281:
case 1141899272:
if (tok == 1048583) break;
default:
rpn.addOp (JS.T.o (269484096, "["));
rpn.addXStr (this.optParameterAsString (++i));
rpn.addOp (JS.T.o (269484097, "]"));
continue;
}
}var $var = this.getBitsetPropertySelector (i + 1, false, false);
if ($var == null) this.invArg ();
var isUserFunction = ($var.intValue == 135368713);
var allowMathFunc = true;
var tok2 = this.tokAt (this.iToken + 2);
if (this.tokAt (this.iToken + 1) == 1048583) {
switch (tok2) {
case 1048579:
tok2 = 480;
if (this.tokAt (this.iToken + 3) == 1048583 && this.tokAt (this.iToken + 4) == 1276118529) tok2 = 224;
case 32:
case 64:
case 192:
case 128:
case 160:
case 96:
allowMathFunc = (isUserFunction || $var.intValue == 1276118018 || tok2 == 480 || tok2 == 224);
$var.intValue |= tok2;
this.getToken (this.iToken + 2);
}
}allowMathFunc = new Boolean (allowMathFunc & (this.tokAt (this.iToken + 1) == 269484048 || isUserFunction)).valueOf ();
if (!rpn.addOpAllowMath ($var, allowMathFunc)) this.invArg ();
i = this.iToken;
if ($var.intValue == 135368713 && this.tokAt (i + 1) != 269484048) {
rpn.addOp (JS.T.tokenLeftParen);
rpn.addOp (JS.T.tokenRightParen);
}break;
default:
if (this.theTok == 269484096 && this.tokAt (i + 2) == 269484066) {
v = this.getAssocArray (i);
i = this.iToken;
break;
}if (JS.T.tokAttr (this.theTok, 269484032) || JS.T.tokAttr (this.theTok, 135266304) && this.tokAt (this.iToken + 1) == 269484048) {
if (!rpn.addOp (this.theToken)) {
if (ptAtom >= 0) {
break out;
}this.invArg ();
}switch (this.theTok) {
case 269484436:
if (topLevel) ptEq = i;
break;
case 269484048:
nParen++;
topLevel = false;
break;
case 269484049:
if (--nParen <= 0 && nSquare == 0) {
if (isOneExpressionOnly) {
this.iToken++;
break out;
}topLevel = true;
}break;
case 269484096:
nSquare++;
topLevel = false;
break;
case 269484097:
if (--nSquare == 0 && nParen == 0) {
if (isOneExpressionOnly) {
this.iToken++;
break out;
}topLevel = true;
}break;
}
} else {
var name = this.paramAsStr (i).toLowerCase ();
var haveParens = (this.tokAt (i + 1) == 269484048);
if (this.chk) {
v = name;
} else if (!haveParens && (localVars == null || (v = JU.PT.getMapValueNoCase (localVars, name)) == null && allContext)) {
v = this.getContextVariableAsVariable (name);
}if (v == null) {
if (JS.T.tokAttr (this.theTok, 1073741824) && this.vwr.isFunction (name)) {
if (!rpn.addOp (JS.SV.newV (135368713, this.theToken.value))) this.invArg ();
if (!haveParens) {
rpn.addOp (JS.T.tokenLeftParen);
rpn.addOp (JS.T.tokenRightParen);
}} else {
$var = this.vwr.g.getOrSetNewVariable (name, false);
switch ($var.tok) {
case 2:
case 3:
if (this.noCopy (i, -1) || this.noCopy (i, 1)) break;
rpn.addXCopy ($var);
continue;
default:
}
rpn.addX ($var);
}}}}
if (v != null) {
if (Clazz.instanceOf (v, JU.BS)) rpn.addXBs (v);
 else rpn.addXObj (v);
}}
var result = rpn.getResult ();
if (result == null) {
if (!this.chk) rpn.dumpStacks ("null result");
this.error (13);
}if (result.tok == 135198) return result.value;
if (this.chk) {
if (returnBoolean) return Boolean.TRUE;
if (returnString) return "";
} else {
if (returnBoolean) return Boolean.$valueOf (result.asBoolean ());
if (returnString) {
if (result.tok == 4) result.intValue = 2147483647;
return result.asString ();
}}switch (result.tok) {
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
}, "~N,~N,~S,~B,~B,~N,~B,java.util.Map,~S,~B");
Clazz.overrideMethod (c$, "atomExpressionAt", 
function (index) {
if (!this.checkToken (index)) {
this.iToken = index;
this.bad ();
}return this.atomExpression (this.st, index, 0, true, false, true, true);
}, "~N");
Clazz.overrideMethod (c$, "atomExpression", 
function (code, pcStart, pcStop, allowRefresh, allowUnderflow, mustBeBitSet, andNotDeleted) {
this.isBondSet = false;
if (code !== this.st) {
this.tempStatement = this.st;
this.st = code;
}var rpn =  new JS.ScriptMathProcessor (this, false, false, false, mustBeBitSet, allowUnderflow, null);
var val;
var refreshed = false;
this.iToken = 1000;
var ignoreSubset = (pcStart < 0);
var isInMath = false;
var nExpress = 0;
var ac = this.vwr.getAtomCount ();
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
if (pc > 0 && code[pc - 1].tok == 1048586) rpn.addXBs ( new JU.BS ());
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
rpn.addX (JS.SV.newT (instruction));
rpn.addX (JS.SV.newV (9, this.hklParameter (pc + 2)));
pc = this.iToken;
break;
case 135266319:
rpn.addX (JS.SV.newT (instruction));
rpn.addX (JS.SV.newV (9, this.planeParameter (pc + 2)));
pc = this.iToken;
break;
case 1048581:
rpn.addX (JS.SV.newT (instruction));
rpn.addXPt (this.getPoint3f (pc + 2, true));
pc = this.iToken;
break;
case 4:
var s = value;
if (s.indexOf ("({") == 0) {
var bs = JU.BS.unescape (s);
if (bs != null) {
rpn.addXBs (bs);
break;
}}rpn.addX (JS.SV.newT (instruction));
if (s.equals ("hkl")) {
rpn.addX (JS.SV.newV (9, this.hklParameter (pc + 2)));
pc = this.iToken;
}break;
case 135267336:
case 135267335:
case 1238369286:
case 135266325:
case 135402505:
case 135266310:
case 269484080:
rpn.addOp (instruction);
break;
case 1048579:
rpn.addXBs (this.vwr.getAllAtoms ());
break;
case 1048587:
rpn.addXBs ( new JU.BS ());
break;
case 1048589:
case 1048588:
rpn.addX (JS.SV.newT (instruction));
break;
case 1114638363:
rpn.addXBs (JU.BSUtil.copy (this.vwr.bsA ()));
break;
case 3145770:
rpn.addXBs (JU.BSUtil.copy (this.vwr.slm.getHiddenSet ()));
break;
case 1060869:
rpn.addXBs (JU.BSUtil.copy (this.vwr.getMotionFixedAtoms ()));
break;
case 3145768:
rpn.addXBs (JU.BSUtil.copyInvert (this.vwr.slm.getHiddenSet (), ac));
break;
case 3145776:
rpn.addXBs (this.vwr.getBaseModelBitSet ());
break;
case 3145774:
if (!this.chk && !refreshed) this.vwr.setModelVisibility ();
refreshed = true;
rpn.addXBs (this.vwr.ms.getVisibleSet ());
break;
case 3145766:
if (!this.chk && allowRefresh) this.refresh (false);
rpn.addXBs (this.vwr.ms.getClickableSet ());
break;
case 1048608:
if (this.vwr.allowSpecAtom ()) {
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
case 3145741:
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
if (!this.vwr.haveFileSet ()) {
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
if (isInMath) rpn.addXNum (instruction);
 else rpn.addXBs (this.getAtomBits (1048614, Integer.$valueOf (JS.ScriptExpr.getSeqCode (instruction))));
break;
case 1048615:
if (isInMath) {
rpn.addXNum (instruction);
rpn.addOp (JS.T.tokenMinus);
rpn.addXNum (code[++pc]);
break;
}var chainID = (pc + 3 < code.length && code[pc + 2].tok == 269484160 && code[pc + 3].tok == 1048609 ? code[pc + 3].intValue : -1);
rpn.addXBs (this.getAtomBits (1048615, [JS.ScriptExpr.getSeqCode (instruction), JS.ScriptExpr.getSeqCode (code[++pc]), chainID]));
if (chainID != -1) pc += 2;
break;
case 1095761926:
case 1095761925:
var pt = value;
rpn.addXBs (this.getAtomBits (instruction.tok, [Clazz.doubleToInt (Math.floor (pt.x * 1000)), Clazz.doubleToInt (Math.floor (pt.y * 1000)), Clazz.doubleToInt (Math.floor (pt.z * 1000))]));
break;
case 3145758:
rpn.addXBs (this.vwr.getModelUndeletedAtomsBitSet (this.vwr.am.cmi));
break;
case 1613758476:
case 3145730:
case 1115297793:
case 1613758488:
case 137363467:
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
case 269484437:
case 269484438:
var tok = instruction.tok;
var tokWhat = instruction.intValue;
if (tokWhat == 1095766024 && tok != 269484436) this.invArg ();
var data = null;
if (tokWhat == 1716520985) {
if (pc + 2 == code.length) this.invArg ();
if (!this.chk) data = this.vwr.getDataFloat (code[++pc].value);
}if (++pc == code.length) this.invArg ();
rpn.addXBs (this.chk ?  new JU.BS () : this.getComparison (code[pc], tokWhat, tok, value, data));
break;
case 3:
case 2:
rpn.addXNum (instruction);
break;
case 10:
var bs1 = JU.BSUtil.copy (value);
rpn.addXBs (bs1);
break;
case 8:
rpn.addXPt (value);
break;
default:
if (JS.T.tokAttr (instruction.tok, 269484032)) {
if (!rpn.addOp (instruction)) this.invArg ();
break;
}if (!(Clazz.instanceOf (value, String))) {
rpn.addXObj (value);
break;
}val = this.getParameter (value, 0, true);
if (isInMath) {
rpn.addXObj (val);
break;
}if (Clazz.instanceOf (val, String)) val = this.getStringObjectAsVariable (val, null);
if (Clazz.instanceOf (val, JU.Lst)) {
var bs = JS.SV.unEscapeBitSetArray (val, true);
val = (bs == null ? "" : val);
}if (Clazz.instanceOf (val, String)) val = this.lookupIdentifierValue (value);
rpn.addXObj (val);
break;
}
}
this.expressionResult = rpn.getResult ();
if (this.expressionResult == null) {
if (allowUnderflow) return null;
if (!this.chk) rpn.dumpStacks ("after getResult");
this.error (13);
}this.expressionResult = (this.expressionResult).value;
if (Clazz.instanceOf (this.expressionResult, String) && (mustBeBitSet || (this.expressionResult).startsWith ("({"))) {
this.expressionResult = (this.chk ?  new JU.BS () : this.getAtomBitSet (this.expressionResult));
}if (!mustBeBitSet && !(Clazz.instanceOf (this.expressionResult, JU.BS))) return null;
var bs = (Clazz.instanceOf (this.expressionResult, JU.BS) ? this.expressionResult :  new JU.BS ());
this.isBondSet = (Clazz.instanceOf (this.expressionResult, JM.BondSet));
if (!this.isBondSet && this.vwr.excludeAtoms (bs, ignoreSubset).length () > this.vwr.getAtomCount ()) bs.clearAll ();
if (this.tempStatement != null) {
this.st = this.tempStatement;
this.tempStatement = null;
}return bs;
}, "~A,~N,~N,~B,~B,~B,~B");
Clazz.defineMethod (c$, "getComparison", 
 function (t, tokWhat, tokOp, strOp, data) {
var tokValue = t.tok;
if (tokValue == 7) {
var bs =  new JU.BS ();
if (tokOp != 269484436) bs.setBits (0, this.vwr.getAtomCount ());
var lst = (t).getList ();
for (var i = lst.size (); --i >= 0; ) {
var res = this.getComparison (lst.get (i), tokWhat, tokOp, strOp, data);
if (tokOp == 269484436) bs.or (res);
 else bs.and (res);
}
return bs;
}var comparisonInt = t.intValue;
var comparisonFloat = NaN;
var isModel = (tokWhat == 1095766030);
var isIntProperty = JS.T.tokAttr (tokWhat, 1095761920);
var isFloatProperty = (JS.T.tokAttr (tokWhat, 1112539136) || (tokWhat & 1137704960) == 1078984704);
var isIntOrFloat = isIntProperty && isFloatProperty;
var isStringProperty = !isIntProperty && JS.T.tokAttr (tokWhat, 1087373312);
if (tokWhat == 1087375365) isIntProperty = !(isStringProperty = false);
var val = t.value;
if (JS.T.tokAttr (tokValue, 1073741824)) {
if ("_modelNumber".equalsIgnoreCase (val)) {
var modelIndex = this.vwr.am.cmi;
val = Integer.$valueOf (comparisonInt = (modelIndex < 0 ? 0 : this.vwr.getModelFileNumber (modelIndex)));
} else {
var v = this.getParameter (val, 1073742190, false);
if (v != null) {
if (v.tok == 7) return this.getComparison (v, tokWhat, tokOp, strOp, data);
comparisonInt = v.intValue;
val = (isStringProperty ? JS.SV.sValue (v) : JS.SV.nValue (v));
}}}if (Clazz.instanceOf (val, JU.P3)) {
if (tokWhat == 1766856708) {
comparisonInt = JU.CU.colorPtToFFRGB (val);
tokValue = 2;
isIntProperty = true;
}} else if (Clazz.instanceOf (val, String)) {
if (tokWhat == 1766856708) {
comparisonInt = JU.CU.getArgbFromString (val);
if (comparisonInt == 0 && JS.T.tokAttr (tokValue, 1073741824)) {
val = this.getStringParameter (val, true);
if ((val).startsWith ("{")) {
val = JU.Escape.uP (val);
if (Clazz.instanceOf (val, JU.P3)) comparisonInt = JU.CU.colorPtToFFRGB (val);
 else comparisonInt = 0;
} else {
comparisonInt = JU.CU.getArgbFromString (val);
}}tokValue = 2;
isIntProperty = true;
} else if (!isStringProperty) {
if (tokWhat == 1641025539 || tokWhat == 1238369286 || tokWhat == 1087375365) isStringProperty = !(isIntProperty = (comparisonInt != 2147483647));
 else val = JS.SV.nValue (t);
if (Clazz.instanceOf (val, Integer)) comparisonFloat = comparisonInt = (val).intValue ();
 else if (Clazz.instanceOf (val, Float) && isModel) comparisonInt = JM.ModelSet.modelFileNumberFromFloat ((val).floatValue ());
}}if (isStringProperty && !(Clazz.instanceOf (val, String))) {
val = "" + val;
}if (Clazz.instanceOf (val, Integer) || tokValue == 2) {
if (isModel) {
if (comparisonInt >= 1000000) tokWhat = -1095766030;
} else if (isIntOrFloat) {
isFloatProperty = false;
} else if (isFloatProperty) {
comparisonFloat = comparisonInt;
}} else if (Clazz.instanceOf (val, Float)) {
if (isModel) {
tokWhat = -1095766030;
} else {
comparisonFloat = (val).floatValue ();
if (isIntOrFloat) {
isIntProperty = false;
} else if (isIntProperty) {
comparisonInt = Clazz.floatToInt (comparisonFloat);
}}} else if (!isStringProperty) {
this.iToken++;
this.invArg ();
}if (isModel && comparisonInt >= 1000000 && comparisonInt % 1000000 == 0) {
comparisonInt /= 1000000;
tokWhat = 1229984263;
isModel = false;
}if (tokWhat == -1095766030 && tokOp == 269484436) {
return this.bitSetForModelFileNumber (comparisonInt);
}if (strOp != null && strOp.indexOf ("-") >= 0) {
if (isIntProperty) comparisonInt = -comparisonInt;
 else if (!Float.isNaN (comparisonFloat)) comparisonFloat = -comparisonFloat;
}return (isIntProperty ? this.compareInt (tokWhat, tokOp, comparisonInt) : isStringProperty ? this.compareString (tokWhat, tokOp, val) : this.compareFloatData (tokWhat, data, tokOp, comparisonFloat));
}, "JS.T,~N,~N,~S,~A");
Clazz.defineMethod (c$, "noCopy", 
function (i, dir) {
switch (this.tokAt (i + dir)) {
case 269484226:
case 269484225:
return ((this.st[i + dir].intValue == -1) == (dir == -1));
default:
return false;
}
}, "~N,~N");
Clazz.defineMethod (c$, "getAssocArray", 
function (i) {
var ht =  new java.util.Hashtable ();
var closer = (this.tokAt (i) == 1048586 ? 1048590 : 269484097);
for (i = i + 1; i < this.slen; i++) {
if (this.tokAt (i) == closer) break;
var key = this.optParameterAsString (i++);
if (this.tokAt (i++) != 269484066) this.invArg ();
var v = this.parameterExpression (i, 0, null, false, true, -1, false, null, null, false);
ht.put (key, v.get (0));
i = this.iToken;
if (this.tokAt (i) != 269484080) break;
}
this.iToken = i;
if (this.tokAt (i) != closer) this.invArg ();
return ht;
}, "~N");
Clazz.defineMethod (c$, "listBS", 
function (bs) {
var l =  new JU.Lst ();
l.addLast (JS.SV.newV (10, bs));
return l;
}, "JU.BS");
Clazz.defineMethod (c$, "compareFloatData", 
function (tokWhat, data, tokOperator, comparisonFloat) {
var bs =  new JU.BS ();
var ac = this.vwr.getAtomCount ();
var modelSet = this.vwr.ms;
var atoms = modelSet.at;
var propertyFloat = 0;
this.vwr.autoCalculate (tokWhat);
var isProp = (tokWhat == 1716520985);
if (!isProp && this.ptTemp == null) this.ptTemp =  new JU.P3 ();
for (var i = ac; --i >= 0; ) {
var match = false;
var atom = atoms[i];
if (isProp) {
if (data == null || data.length <= i) continue;
propertyFloat = data[i];
} else {
propertyFloat = JM.Atom.atomPropertyFloat (this.vwr, atom, tokWhat, this.ptTemp);
}match = this.compareFloat (tokOperator, propertyFloat, comparisonFloat);
if (match) bs.set (i);
}
return bs;
}, "~N,~A,~N,~N");
Clazz.defineMethod (c$, "compareFloat", 
function (tokOperator, a, b) {
switch (tokOperator) {
case 269484435:
return a < b;
case 269484434:
return a <= b;
case 269484433:
return a >= b;
case 269484432:
return a > b;
case 269484436:
return a == b;
case 269484437:
return a != b;
}
return false;
}, "~N,~N,~N");
Clazz.defineMethod (c$, "compareString", 
 function (tokWhat, tokOperator, comparisonString) {
var bs =  new JU.BS ();
var atoms = this.vwr.ms.at;
var ac = this.vwr.getAtomCount ();
var isCaseSensitive = (tokOperator == 269484438 || tokWhat == 1087373316 && this.vwr.getBoolean (603979823));
if (!isCaseSensitive) comparisonString = comparisonString.toLowerCase ();
for (var i = ac; --i >= 0; ) {
var propertyString = JM.Atom.atomPropertyString (this.vwr, atoms[i], tokWhat);
if (!isCaseSensitive) propertyString = propertyString.toLowerCase ();
if (this.compareStringValues (tokOperator, propertyString, comparisonString)) bs.set (i);
}
return bs;
}, "~N,~N,~S");
Clazz.defineMethod (c$, "compareStringValues", 
 function (tokOperator, propertyValue, comparisonValue) {
switch (tokOperator) {
case 269484436:
case 269484437:
return (JU.Txt.isMatch (propertyValue, comparisonValue, true, true) == (tokOperator == 269484436));
case 269484438:
return JU.PT.isLike (propertyValue, comparisonValue);
default:
this.invArg ();
}
return false;
}, "~N,~S,~S");
Clazz.defineMethod (c$, "compareInt", 
 function (tokWhat, tokOperator, ival) {
var ia = 2147483647;
var propertyBitSet = null;
var bitsetComparator = tokOperator;
var bitsetBaseValue = ival;
var ac = this.vwr.getAtomCount ();
var modelSet = this.vwr.ms;
var atoms = modelSet.at;
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
return JU.BSUtil.newBitSet2 (0, ival);
case 269484434:
return JU.BSUtil.newBitSet2 (0, ival + 1);
case 269484433:
return JU.BSUtil.newBitSet2 (ival, ac);
case 269484432:
return JU.BSUtil.newBitSet2 (ival + 1, ac);
case 269484436:
return (ival < ac ? JU.BSUtil.newBitSet2 (ival, ival + 1) :  new JU.BS ());
case 269484437:
default:
bs = JU.BSUtil.setAll (ac);
if (ival >= 0) bs.clear (ival);
return bs;
}
} catch (e) {
if (Clazz.exceptionOf (e, Exception)) {
return  new JU.BS ();
} else {
throw e;
}
}
}
bs = JU.BS.newN (ac);
for (var i = 0; i < ac; ++i) {
var match = false;
var atom = atoms[i];
switch (tokWhat) {
default:
ia = JM.Atom.atomPropertyInt (atom, tokWhat);
break;
case 1095766024:
return JU.BSUtil.copy (this.vwr.getConformation (-1, ival - 1, false));
case 1297090050:
propertyBitSet = atom.getAtomSymmetry ();
if (propertyBitSet == null) continue;
if (atom.getModelIndex () != iModel) {
iModel = atom.getModelIndex ();
cellRange = modelSet.getModelCellRange (iModel);
nOps = modelSet.getModelSymmetryCount (iModel);
}if (bitsetBaseValue >= 200) {
if (cellRange == null) continue;
ival = bitsetBaseValue % 1000;
var symop = Clazz.doubleToInt (bitsetBaseValue / 1000) - 1;
if (symop < 0) {
match = true;
} else if (nOps == 0 || symop >= 0 && !(match = propertyBitSet.get (symop))) {
continue;
}bitsetComparator = 1048587;
if (symop < 0) ia = atom.getCellTranslation (ival, cellRange, nOps);
 else ia = atom.getSymmetryTranslation (symop, cellRange, nOps);
} else if (nOps > 0) {
if (ival > nOps) {
if (bitsetComparator != 269484435 && bitsetComparator != 269484434) continue;
}if (bitsetComparator == 269484437) {
if (ival > 0 && ival <= nOps && !propertyBitSet.get (ival)) {
bs.set (i);
}continue;
}var bs1 = JU.BSUtil.copy (propertyBitSet);
bs1.clearBits (nOps, bs1.length ());
propertyBitSet = bs1;
}switch (bitsetComparator) {
case 269484435:
imax = ival - 1;
break;
case 269484434:
imax = ival;
break;
case 269484433:
imin = ival - 1;
break;
case 269484432:
imin = ival;
break;
case 269484436:
imax = ival;
imin = ival - 1;
break;
case 269484437:
match = !propertyBitSet.get (ival);
break;
}
if (imin < 0) imin = 0;
if (imin < imax) {
var pt = propertyBitSet.nextSetBit (imin);
if (pt >= 0 && pt < imax) match = true;
}if (!match || ia == 2147483647) tokOperator = 1048587;
}
switch (tokOperator) {
case 1048587:
break;
case 269484435:
match = (ia < ival);
break;
case 269484434:
match = (ia <= ival);
break;
case 269484433:
match = (ia >= ival);
break;
case 269484432:
match = (ia > ival);
break;
case 269484436:
match = (ia == ival);
break;
case 269484437:
match = (ia != ival);
break;
}
if (match) bs.set (i);
}
return bs;
}, "~N,~N,~N");
Clazz.defineMethod (c$, "getBitsetPropertySelector", 
 function (i, mustBeSettable, isExpression) {
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
if (JS.T.tokAttrOr (tok, 1078984704, 1141899264)) break;
if (tok != 806354977 && !JS.T.tokAttr (tok, 1073741824)) return null;
var name = this.paramAsStr (i);
if (!mustBeSettable && this.vwr.isFunction (name)) {
tok = 135368713;
break;
}if (isExpression && !name.endsWith ("?")) return null;
if (isExpression) tok = 1073741824;
}
if (mustBeSettable && isExpression && !JS.T.tokAttr (tok, 2048)) return null;
return JS.SV.newSV (269484241, tok, this.paramAsStr (i));
}, "~N,~B,~B");
Clazz.defineMethod (c$, "getBitsetPropertyFloat", 
function (bs, tok, min, max) {
var data = this.getBitsetProperty (bs, tok, null, null, null, null, false, 2147483647, false);
if (!Float.isNaN (min)) for (var i = 0; i < data.length; i++) if (data[i] < min) data[i] = NaN;

if (!Float.isNaN (max)) for (var i = 0; i < data.length; i++) if (data[i] > max) data[i] = NaN;

return data;
}, "JU.BS,~N,~N,~N");
Clazz.defineMethod (c$, "getBitsetProperty", 
function (bs, tok, ptRef, planeRef, tokenValue, opValue, useAtomMap, index, asVectorIfAll) {
var haveIndex = (index != 2147483647);
var isAtoms = haveIndex || !(Clazz.instanceOf (tokenValue, JM.BondSet));
var minmaxtype = tok & 480;
var selectedFloat = (minmaxtype == 224);
var ac = this.vwr.getAtomCount ();
var fout = (minmaxtype == 256 ?  Clazz.newFloatArray (ac, 0) : null);
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
isInt = JS.T.tokAttr (tok, 1095761920) && !JS.T.tokAttr (tok, 1112539136);
isString = !isInt && JS.T.tokAttr (tok, 1087373312);
}
var pt = (isPt || !isAtoms ?  new JU.P3 () : null);
if (isExplicitlyAll || isString && !haveIndex && minmaxtype != 256 && minmaxtype != 32) minmaxtype = 1048579;
var vout = (minmaxtype == 1048579 ?  new JU.Lst () : null);
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
bsNew = (tok == 1141899265 ? (isAtoms ? bs : this.vwr.ms.getAtoms (1678770178, bs)) : (isAtoms ?  new JM.BondSet (this.vwr.getBondsForSelectedAtoms (bs)) : bs));
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
return this.getCmdExt ().getBitsetIdent (bs, null, tokenValue, useAtomMap, index, isExplicitlyAll);
}
return "";
case 135368713:
userFunction = (opValue)[0];
params = (opValue)[1];
bsAtom = JU.BS.newN (ac);
tokenAtom = JS.SV.newV (10, bsAtom);
break;
case 1112539150:
case 1112539151:
this.vwr.autoCalculate (tok);
break;
case 1276118018:
if (ptRef == null && planeRef == null) return  new JU.P3 ();
break;
case 1766856708:
ptT =  new JU.P3 ();
break;
case 1716520985:
data = this.vwr.getDataFloat (opValue);
break;
}
var n = 0;
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
var modelSet = this.vwr.ms;
var mode = (isPt ? 3 : isString ? 2 : isInt ? 1 : 0);
if (isAtoms) {
var haveBitSet = (bs != null);
var i0;
var i1;
if (haveIndex) {
i0 = index;
i1 = index + 1;
} else if (haveBitSet) {
i0 = bs.nextSetBit (0);
i1 = Math.min (ac, bs.length ());
} else {
i0 = 0;
i1 = ac;
}if (this.chk) i1 = 0;
for (var i = i0; i >= 0 && i < i1; i = (haveBitSet ? bs.nextSetBit (i + 1) : i + 1)) {
n++;
var atom = modelSet.at[i];
switch (mode) {
case 0:
var fv = 3.4028235E38;
switch (tok) {
case 135368713:
bsAtom.set (i);
fv = JS.SV.fValue (this.getUserFunctionResult (userFunction, params, tokenAtom));
bsAtom.clear (i);
break;
case 1716520985:
fv = (data == null ? 0 : data[i]);
break;
case 1276118018:
if (planeRef != null) fv = JU.Measure.distanceToPlane (planeRef, atom);
 else fv = atom.distance (ptRef);
break;
default:
fv = JM.Atom.atomPropertyFloat (this.vwr, atom, tok, this.ptTemp);
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
case 1095766024:
case 1095761925:
this.errorStr (45, JS.T.nameOf (tok));
break;
default:
iv = JM.Atom.atomPropertyInt (atom, tok);
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
var s = JM.Atom.atomPropertyString (this.vwr, atom, tok);
switch (minmaxtype) {
case 256:
fout[i] = JU.PT.parseFloat (s);
break;
default:
if (vout == null) return s;
vout.addLast (s);
}
break;
case 3:
var t = JM.Atom.atomPropertyTuple (atom, tok, this.ptTemp);
if (t == null) this.errorStr (45, JS.T.nameOf (tok));
switch (minmaxtype) {
case 256:
fout[i] = Math.sqrt (t.x * t.x + t.y * t.y + t.z * t.z);
break;
case 1048579:
vout.addLast (JU.P3.newP (t));
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
var i1 = this.vwr.getBondCount ();
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
pt.ave (bond.getAtom1 (), bond.getAtom2 ());
vout.addLast (JU.P3.newP (pt));
break;
default:
pt.add (bond.getAtom1 ());
pt.add (bond.getAtom2 ());
n++;
}
break;
case 1766856708:
JU.CU.toRGBpt (this.vwr.getColorArgbOrGray (bond.colix), ptT);
switch (minmaxtype) {
case 1048579:
vout.addLast (JU.P3.newP (ptT));
break;
default:
pt.add (ptT);
}
break;
default:
this.errorStr (46, JS.T.nameOf (tok));
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
fout[i] = JU.PT.parseFloat (v);
break;
case 3:
fout[i] = (v).length ();
break;
}
}
return fout;
}if (tok == 1087373320) {
var sb =  new JU.SB ();
for (var i = 0; i < len; i++) sb.append (vout.get (i));

return sb.toString ();
}var sout =  new Array (len);
for (var i = len; --i >= 0; ) {
var v = vout.get (i);
if (Clazz.instanceOf (v, JU.P3)) sout[i] = JU.Escape.eP (v);
 else sout[i] = "" + vout.get (i);
}
return sout;
}if (isPt) return (n == 0 ? pt : JU.P3.new3 (pt.x / n, pt.y / n, pt.z / n));
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
}, "JU.BS,~N,JU.P3,JU.P4,~O,~O,~B,~N,~B");
Clazz.defineMethod (c$, "bitSetForModelFileNumber", 
 function (m) {
var bs = JU.BS.newN (this.vwr.getAtomCount ());
if (this.chk) return bs;
var modelCount = this.vwr.getModelCount ();
var haveFileSet = this.vwr.haveFileSet ();
if (m < 1000000 && haveFileSet) m *= 1000000;
var pt = m % 1000000;
if (pt == 0) {
var model1 = this.vwr.ms.getModelNumberIndex (m + 1, false, false);
if (model1 < 0) return bs;
var model2 = (m == 0 ? modelCount : this.vwr.ms.getModelNumberIndex (m + 1000001, false, false));
if (model1 < 0) model1 = 0;
if (model2 < 0) model2 = modelCount;
if (this.vwr.ms.isTrajectory (model1)) model2 = model1 + 1;
for (var j = model1; j < model2; j++) bs.or (this.vwr.getModelUndeletedAtomsBitSet (j));

} else {
var modelIndex = this.vwr.ms.getModelNumberIndex (m, false, true);
if (modelIndex >= 0) bs.or (this.vwr.getModelUndeletedAtomsBitSet (modelIndex));
}return bs;
}, "~N");
Clazz.defineMethod (c$, "getStringObjectAsVariable", 
 function (s, key) {
if (s == null || s.length == 0) return s;
var v = JS.SV.unescapePointOrBitsetAsVariable (s);
return (Clazz.instanceOf (v, String) && key != null ? this.vwr.g.setUserVariable (key, JS.SV.newS (v)) : v);
}, "~S,~S");
Clazz.defineMethod (c$, "getAtomBits", 
function (tokType, specInfo) {
return (this.chk ?  new JU.BS () : this.vwr.ms.getAtoms (tokType, specInfo));
}, "~N,~O");
c$.getSeqCode = Clazz.defineMethod (c$, "getSeqCode", 
function (instruction) {
return (instruction.intValue == 2147483647 ? (instruction.value).intValue () : JM.Group.getSeqcodeFor (instruction.intValue, ' '));
}, "JS.T");
Clazz.defineMethod (c$, "setVariable", 
function (pt, ptMax, key, isSet) {
var bs = null;
var propertyName = "";
var settingData = key.startsWith ("property_");
var isThrown = key.equals ("thrown_value");
var isExpression = (this.tokAt (1) == 1048577);
var t = (settingData ? null : this.getContextVariableAsVariable (key));
if (isSet && !isExpression) {
switch (this.tokAt (2)) {
case 1073742195:
case 269484096:
if (this.st[0].intValue == 61) pt = 2;
break;
case 1048583:
case 1048584:
break;
case 269484436:
pt = 3;
break;
default:
pt = 2;
break;
}
if (pt == 1) key = null;
}var nv = 0;
var v = this.parameterExpression (pt, ptMax, key, true, true, -1, false, null, null, isSet && pt == 1);
nv = v.size ();
if (nv == 0) this.invArg ();
if (this.chk) return null;
var tv = JS.SV.newS ("").setv (v.get (nv - 1));
if (nv > 1) {
var sel = (nv > 2 ? v.get (1) : null);
t = v.get (0);
var selectOne = false;
switch (t.tok) {
case 6:
case 14:
if (nv > 3) this.invArg ();
t.mapPut (sel.asString (), tv);
break;
case 7:
if (nv > 2 + (sel == null ? 0 : 1)) this.invArg ();
if (sel == null) {
sel = t;
} else {
t = JS.SV.selectItemVar (t);
}selectOne = true;
break;
case 4:
if (sel.tok != 2) {
t.value = JU.PT.rep (t.asString (), sel.asString (), tv.asString ());
t.intValue = 2147483647;
break;
}case 11:
case 12:
if (t.intValue == 2147483647) selectOne = true;
 else t.setSelectedValue (t.intValue, sel.asInt (), tv);
break;
case 8:
var p = (t.value = JU.P3.newP (t.value));
var f = tv.asFloat ();
switch (JS.T.getTokFromName (sel.asString ())) {
case 1112541205:
p.x = f;
break;
case 1112541206:
p.y = f;
break;
case 1112541207:
p.z = f;
break;
}
break;
case 10:
propertyName = sel.asString ();
bs = JS.SV.getBitSet (t, true);
var nAtoms = this.vwr.getAtomCount ();
var nbs = bs.cardinality ();
if (propertyName.startsWith ("property_")) {
var obj = (tv.tok == 7 ? JS.SV.flistValue (tv, tv.getList ().size () == nbs ? nbs : nAtoms) : tv.asString ());
this.vwr.setData (propertyName, [propertyName, obj, JU.BSUtil.copy (bs), Integer.$valueOf (-1)], nAtoms, 0, 0, tv.tok == 7 ? 2147483647 : -2147483648, 0);
break;
}this.setBitsetProperty (bs, JS.T.getTokFromName (propertyName), tv.asInt (), tv.asFloat (), tv);
}
if (selectOne) t.setSelectedValue (sel.intValue, 2147483647, tv);
return null;
}var needVariable = (!settingData && t == null && (isThrown || !(Clazz.instanceOf (tv.value, String) || tv.tok == 2 || Clazz.instanceOf (tv.value, Integer) || Clazz.instanceOf (tv.value, Float) || Clazz.instanceOf (tv.value, Boolean))));
if (needVariable && key != null) {
if (key.startsWith ("_") || (t = this.vwr.g.getOrSetNewVariable (key, true)) == null) this.errorStr (22, key);
}if (t != null) {
t.setv (tv);
t.setModified (true);
return t;
}var vv = JS.SV.oValue (tv);
if (settingData) {
if (tv.tok == 7) vv = tv.asString ();
this.vwr.setData (key, [key, "" + vv, JU.BSUtil.copy (this.vwr.bsA ()), Integer.$valueOf (0)], this.vwr.getAtomCount (), 0, 0, -2147483648, 0);
return null;
}if (Clazz.instanceOf (vv, Boolean)) {
this.setBooleanProperty (key, (vv).booleanValue ());
} else if (Clazz.instanceOf (vv, Integer)) {
this.setIntProperty (key, (vv).intValue ());
} else if (Clazz.instanceOf (vv, Float)) {
this.setFloatProperty (key, (vv).floatValue ());
} else if (Clazz.instanceOf (vv, String)) {
this.setStringProperty (key, vv);
} else {
JU.Logger.error ("ERROR -- return from propertyExpression was " + vv);
}return tv;
}, "~N,~N,~S,~B");
Clazz.defineMethod (c$, "setBitsetProperty", 
 function (bs, tok, iValue, fValue, tokenValue) {
if (this.chk || JU.BSUtil.cardinalityOf (bs) == 0) return;
var list = null;
var sValue = null;
var fvalues = null;
var pt;
var sv = null;
var nValues = 0;
var isStrProperty = JS.T.tokAttr (tok, 1087373312);
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
this.vwr.setAtomCoords (bs, tok, tokenValue.value);
break;
case 7:
this.theToken = tokenValue;
this.vwr.setAtomCoords (bs, tok, this.getPointArray (-1, nValues));
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
pt = JS.SV.ptValue (svi);
if (pt != null) {
values[i] = JU.CU.colorPtToFFRGB (pt);
} else if (svi.tok == 2) {
values[i] = svi.intValue;
} else {
values[i] = JU.CU.getArgbFromString (svi.asString ());
if (values[i] == 0) values[i] = svi.asInt ();
}if (values[i] == 0) this.errorStr2 (50, "ARRAY", svi.asString ());
}
value = values;
prop = "colorValues";
break;
case 8:
value = Integer.$valueOf (JU.CU.colorPtToFFRGB (tokenValue.value));
break;
case 4:
value = tokenValue.value;
break;
default:
value = Integer.$valueOf (JS.SV.iValue (tokenValue));
break;
}
this.setAtomProp (prop, value, bs);
return;
case 1826248716:
case 1288701959:
if (tokenValue.tok != 7) sValue = JS.SV.sValue (tokenValue);
break;
case 1087375365:
case 1095763978:
this.clearDefinedVariableAtomSets ();
isStrProperty = false;
break;
}
switch (tokenValue.tok) {
case 7:
if (isStrProperty) list = JS.SV.strListValue (tokenValue);
 else fvalues = JS.SV.flistValue (tokenValue, nValues);
break;
case 4:
if (sValue == null) list = JU.PT.getTokens (JS.SV.sValue (tokenValue));
break;
}
if (list != null) {
nValues = list.length;
if (!isStrProperty) {
fvalues =  Clazz.newFloatArray (nValues, 0);
for (var i = nValues; --i >= 0; ) fvalues[i] = (tok == 1087375365 ? JU.Elements.elementNumberFromSymbol (list[i], false) : JU.PT.parseFloat (list[i]));

}if (tokenValue.tok != 7 && nValues == 1) {
if (isStrProperty) sValue = list[0];
 else fValue = fvalues[0];
iValue = Clazz.floatToInt (fValue);
list = null;
fvalues = null;
}}this.vwr.setAtomProperty (bs, tok, iValue, fValue, sValue, fvalues, list);
}, "JU.BS,~N,~N,~N,JS.T");
Clazz.defineMethod (c$, "setStatement", 
function (st0) {
this.st = st0;
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
var $var = this.paramAsStr (i);
var isClauseDefine = (this.tokAt (i) == 1048577);
var isSetAt = (j == 1 && this.st[0] === JS.T.tokenSetCmd);
if (isClauseDefine) {
var vt = this.parameterExpressionToken (++i);
i = this.iToken;
v = (vt.tok == 7 ? vt : JS.SV.oValue (vt));
} else {
if (this.tokAt (i) == 2) {
v = this.vwr.ms.getAtoms (1095763969, Integer.$valueOf (this.st[i].intValue));
} else {
v = this.getParameter ($var, 0, true);
}if (!isExpression && !isSetAt) isClauseDefine = true;
}tok = this.tokAt (0);
forceString = new Boolean (forceString | (JS.T.tokAttr (tok, 20480) || tok == 135271429)).valueOf ();
if (Clazz.instanceOf (v, JS.SV)) {
fixed[j] = v;
if (isExpression && fixed[j].tok == 7) {
var bs = JS.SV.getBitSet (v, true);
fixed[j] = JS.SV.newV (10, bs == null ? this.getAtomBitSet (JS.SV.sValue (fixed[j])) : bs);
}} else if (Clazz.instanceOf (v, Boolean)) {
fixed[j] = ((v).booleanValue () ? JS.T.tokenOn : JS.T.tokenOff);
} else if (Clazz.instanceOf (v, Integer)) {
fixed[j] = JS.T.tv (2, (v).intValue (), v);
} else if (Clazz.instanceOf (v, Float)) {
fixed[j] = JS.T.tv (3, JS.ScriptParam.getFloatEncodedInt ("" + v), v);
} else if (Clazz.instanceOf (v, String)) {
if (!forceString && !isExpression) {
if ((tok != 1085443 || j > 1 && this.st[1].tok != 537022465) && JS.T.tokAttr (tok, 36864)) {
v = this.getParameter (v, 1073742190, true);
}if (Clazz.instanceOf (v, String)) {
v = this.getStringObjectAsVariable (v, null);
}}if (Clazz.instanceOf (v, JS.SV)) {
fixed[j] = v;
} else {
s = v;
if (isExpression && !forceString) {
fixed[j] = (JS.T.tokAttr (fixed[j - 1].tok, 269484288) ? JS.T.o (4, s) : JS.T.o (10, this.getAtomBitSet (s)));
} else {
tok = (isSetAt ? JS.T.getTokFromName (s) : isClauseDefine || forceString || s.length == 0 || s.indexOf (".") >= 0 || s.indexOf (" ") >= 0 || s.indexOf ("=") >= 0 || s.indexOf (";") >= 0 || s.indexOf ("[") >= 0 || s.indexOf ("{") >= 0 ? 4 : 1073741824);
fixed[j] = JS.T.o (tok, v);
}}} else if (Clazz.instanceOf (v, JU.BArray)) {
fixed[j] = JS.SV.newV (15, v);
} else if (Clazz.instanceOf (v, JU.BS)) {
fixed[j] = JS.SV.newV (10, v);
} else if (Clazz.instanceOf (v, JU.P3)) {
fixed[j] = JS.SV.newV (8, v);
} else if (Clazz.instanceOf (v, JU.P4)) {
fixed[j] = JS.SV.newV (9, v);
} else if (Clazz.instanceOf (v, JU.M34)) {
fixed[j] = JS.SV.newV (Clazz.instanceOf (v, JU.M4) ? 12 : 11, v);
} else if (Clazz.instanceOf (v, java.util.Map)) {
fixed[j] = JS.SV.newV (6, v);
} else if (Clazz.instanceOf (v, JS.ScriptContext)) {
fixed[j] = JS.SV.newV (6, (v).getFullMap ());
} else if (Clazz.instanceOf (v, JU.Lst)) {
var sv = v;
var bs = null;
for (var k = 0; k < sv.size (); k++) {
var svk = sv.get (k);
if (svk.tok != 10) {
bs = null;
break;
}if (bs == null) bs =  new JU.BS ();
bs.or (svk.value);
}
fixed[j] = (bs == null ? JS.SV.getVariable (v) : JS.T.o (10, bs));
} else {
var center = this.getObjectCenter ($var, -2147483648, -2147483648);
if (center == null) this.invArg ();
fixed[j] = JS.T.o (8, center);
}if (isSetAt && !JS.T.tokAttr (fixed[j].tok, 536870912)) this.invArg ();
break;
}
j++;
}
this.st = fixed;
for (i = j; i < this.st.length; i++) this.st[i] = null;

this.slen = j;
return true;
}, "~A");
});
