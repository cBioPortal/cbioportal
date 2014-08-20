Clazz.declarePackage ("JM");
Clazz.load (null, "JM.LabelToken", ["java.lang.Character", "$.Float", "java.util.Hashtable", "JU.PT", "$.SB", "JM.Atom", "JS.T"], function () {
c$ = Clazz.decorateAsClass (function () {
this.text = null;
this.key = null;
this.data = null;
this.tok = 0;
this.pt = -1;
this.ch1 = '\0';
this.width = 0;
this.precision = 2147483647;
this.alignLeft = false;
this.zeroPad = false;
this.intAsFloat = false;
Clazz.instantialize (this, arguments);
}, JM, "LabelToken");
Clazz.makeConstructor (c$, 
function () {
});
Clazz.defineMethod (c$, "set", 
 function (text, pt) {
this.text = text;
this.pt = pt;
return this;
}, "~S,~N");
c$.isLabelPropertyTok = Clazz.defineMethod (c$, "isLabelPropertyTok", 
 function (tok) {
for (var i = JM.LabelToken.labelTokenIds.length; --i >= 0; ) if (JM.LabelToken.labelTokenIds[i] == tok) return true;

return false;
}, "~N");
c$.compile = Clazz.defineMethod (c$, "compile", 
function (vwr, strFormat, chAtom, htValues) {
if (strFormat == null || strFormat.length == 0) return null;
if (strFormat.indexOf ("%") < 0 || strFormat.length < 2) return [ new JM.LabelToken ().set (strFormat, -1)];
var n = 0;
var ich = -1;
var cch = strFormat.length;
while (++ich < cch && (ich = strFormat.indexOf ('%', ich)) >= 0) n++;

var tokens =  new Array (n * 2 + 1);
var ichPercent;
var i = 0;
for (ich = 0; (ichPercent = strFormat.indexOf ('%', ich)) >= 0; ) {
if (ich != ichPercent) tokens[i++] =  new JM.LabelToken ().set (strFormat.substring (ich, ichPercent), -1);
var lt = tokens[i++] =  new JM.LabelToken ().set (null, ichPercent);
vwr.autoCalculate (lt.tok);
ich = JM.LabelToken.setToken (vwr, strFormat, lt, cch, chAtom.charCodeAt (0), htValues);
}
if (ich < cch) tokens[i++] =  new JM.LabelToken ().set (strFormat.substring (ich), -1);
return tokens;
}, "JV.Viewer,~S,~S,java.util.Map");
Clazz.defineMethod (c$, "formatLabel", 
function (vwr, atom, strFormat, ptTemp) {
return (strFormat == null || strFormat.length == 0 ? null : JM.LabelToken.formatLabelAtomArray (vwr, atom, JM.LabelToken.compile (vwr, strFormat, '\0', null), '\0', null, ptTemp));
}, "JV.Viewer,JM.Atom,~S,JU.P3");
c$.formatLabelAtomArray = Clazz.defineMethod (c$, "formatLabelAtomArray", 
function (vwr, atom, tokens, chAtom, indices, ptTemp) {
if (atom == null) return null;
var strLabel = (chAtom > '0' ? null :  new JU.SB ());
if (tokens != null) for (var i = 0; i < tokens.length; i++) {
var t = tokens[i];
if (t == null) break;
if (chAtom > '0' && t.ch1 != chAtom) continue;
if (t.tok <= 0 || t.key != null) {
if (strLabel != null) {
strLabel.append (t.text);
if (t.ch1 != '\0') strLabel.appendC (t.ch1);
}} else {
JM.LabelToken.appendAtomTokenValue (vwr, atom, t, strLabel, indices, ptTemp);
}}
return (strLabel == null ? null : strLabel.toString ().intern ());
}, "JV.Viewer,JM.Atom,~A,~S,~A,JU.P3");
c$.getBondLabelValues = Clazz.defineMethod (c$, "getBondLabelValues", 
function () {
var htValues =  new java.util.Hashtable ();
htValues.put ("#", "");
htValues.put ("ORDER", "");
htValues.put ("TYPE", "");
htValues.put ("LENGTH", Float.$valueOf (0));
htValues.put ("ENERGY", Float.$valueOf (0));
return htValues;
});
c$.formatLabelBond = Clazz.defineMethod (c$, "formatLabelBond", 
function (vwr, bond, tokens, values, indices, ptTemp) {
values.put ("#", "" + (bond.index + 1));
values.put ("ORDER", "" + bond.getOrderNumberAsString ());
values.put ("TYPE", bond.getOrderName ());
values.put ("LENGTH", Float.$valueOf (bond.atom1.distance (bond.atom2)));
values.put ("ENERGY", Float.$valueOf (bond.getEnergy ()));
JM.LabelToken.setValues (tokens, values);
JM.LabelToken.formatLabelAtomArray (vwr, bond.atom1, tokens, '1', indices, ptTemp);
JM.LabelToken.formatLabelAtomArray (vwr, bond.atom2, tokens, '2', indices, ptTemp);
return JM.LabelToken.getLabel (tokens);
}, "JV.Viewer,JM.Bond,~A,java.util.Map,~A,JU.P3");
c$.formatLabelMeasure = Clazz.defineMethod (c$, "formatLabelMeasure", 
function (vwr, m, label, value, units) {
var htValues =  new java.util.Hashtable ();
htValues.put ("#", "" + (m.index + 1));
htValues.put ("VALUE", Float.$valueOf (value));
htValues.put ("UNITS", units);
var tokens = JM.LabelToken.compile (vwr, label, '\1', htValues);
if (tokens == null) return "";
JM.LabelToken.setValues (tokens, htValues);
var atoms = m.ms.at;
var indices = m.countPlusIndices;
for (var i = indices[0]; i >= 1; --i) if (indices[i] >= 0) JM.LabelToken.formatLabelAtomArray (vwr, atoms[indices[i]], tokens, String.fromCharCode (48 + i), null, null);

label = JM.LabelToken.getLabel (tokens);
return (label == null ? "" : label);
}, "JV.Viewer,JM.Measurement,~S,~N,~S");
c$.setValues = Clazz.defineMethod (c$, "setValues", 
function (tokens, values) {
for (var i = 0; i < tokens.length; i++) {
var lt = tokens[i];
if (lt == null) break;
if (lt.key == null) continue;
var value = values.get (lt.key);
lt.text = (Clazz.instanceOf (value, Float) ? lt.format ((value).floatValue (), null, null) : lt.format (NaN, value, null));
}
}, "~A,java.util.Map");
c$.getLabel = Clazz.defineMethod (c$, "getLabel", 
function (tokens) {
var sb =  new JU.SB ();
for (var i = 0; i < tokens.length; i++) {
var lt = tokens[i];
if (lt == null) break;
sb.append (lt.text);
}
return sb.toString ();
}, "~A");
c$.setToken = Clazz.defineMethod (c$, "setToken", 
 function (vwr, strFormat, lt, cch, chAtom, htValues) {
var ich = lt.pt + 1;
if (ich >= cch) return ich;
var ch;
if (strFormat.charAt (ich) == '-') {
lt.alignLeft = true;
++ich;
}if (ich < cch && strFormat.charAt (ich) == '0') {
lt.zeroPad = true;
++ich;
}while (ich < cch && Character.isDigit (ch = strFormat.charAt (ich))) {
lt.width = (10 * lt.width) + (ch.charCodeAt (0) - 48);
++ich;
}
lt.precision = 2147483647;
var isNegative = false;
if (ich < cch && strFormat.charAt (ich) == '.') {
++ich;
if (ich < cch && (ch = strFormat.charAt (ich)) == '-') {
isNegative = true;
++ich;
}if (ich < cch && Character.isDigit (ch = strFormat.charAt (ich))) {
lt.precision = ch.charCodeAt (0) - 48;
if (isNegative) lt.precision = -1 - lt.precision;
++ich;
}}if (ich < cch && htValues != null) for (var key, $key = htValues.keySet ().iterator (); $key.hasNext () && ((key = $key.next ()) || true);) if (strFormat.indexOf (key) == ich) return ich + (lt.key = key).length;

if (ich < cch) switch (ch = strFormat.charAt (ich++)) {
case '%':
lt.text = "%";
return ich;
case '[':
var ichClose = strFormat.indexOf (']', ich);
if (ichClose < ich) {
ich = cch;
break;
}var propertyName = strFormat.substring (ich, ichClose).toLowerCase ();
if (propertyName.startsWith ("property_")) {
lt.text = propertyName;
lt.tok = 135270408;
lt.data = vwr.getDataFloat (lt.text);
} else if (propertyName.startsWith ("validation.")) {
lt.text = propertyName.substring (11);
lt.tok = 1073742189;
lt.data = vwr.getDataFloat ("property_" + lt.text);
} else {
var token = JS.T.getTokenFromName (propertyName);
if (token != null && JM.LabelToken.isLabelPropertyTok (token.tok)) lt.tok = token.tok;
}ich = ichClose + 1;
break;
case '{':
var ichCloseBracket = strFormat.indexOf ('}', ich);
if (ichCloseBracket < ich) {
ich = cch;
break;
}lt.text = strFormat.substring (ich, ichCloseBracket);
lt.data = vwr.getDataFloat (lt.text);
if (lt.data == null) {
lt.data = vwr.getData (lt.text);
if (Clazz.instanceOf (lt.data, Array)) {
lt.data = (lt.data)[1];
if (Clazz.instanceOf (lt.data, String)) lt.data = JU.PT.split (lt.data, "\n");
if (!(JU.PT.isAS (lt.data))) lt.data = null;
}lt.tok = (lt.data == null ? 4 : 135266306);
} else {
lt.tok = 135270408;
}ich = ichCloseBracket + 1;
break;
default:
var i;
var i1;
if (ich < cch && (i = "fuv".indexOf (ch)) >= 0 && (i1 = "xyz".indexOf (strFormat.charAt (ich))) >= 0) {
lt.tok = JM.LabelToken.twoCharLabelTokenIds[i * 3 + i1];
ich++;
} else if ((i = "AaBbCcDEefGgIiLlMmNnoPpQqRrSsTtUuVvWXxxYyyZzz%%%gqW".indexOf (ch)) >= 0) {
lt.tok = JM.LabelToken.labelTokenIds[i];
}}
lt.text = strFormat.substring (lt.pt, ich);
if (ich < cch && chAtom != 0 && Character.isDigit (ch = strFormat.charAt (ich))) {
ich++;
lt.ch1 = ch;
if (ch.charCodeAt (0) != chAtom && chAtom != 1) lt.tok = 0;
}return ich;
}, "JV.Viewer,~S,JM.LabelToken,~N,~N,java.util.Map");
c$.appendAtomTokenValue = Clazz.defineMethod (c$, "appendAtomTokenValue", 
 function (vwr, atom, t, strLabel, indices, ptTemp) {
var strT = null;
var floatT = NaN;
var ptT = null;
try {
switch (t.tok) {
case 1095761923:
strT = "" + (indices == null ? atom.i : indices[atom.i]);
break;
case 1766856708:
ptT = JM.Atom.atomPropertyTuple (atom, t.tok, ptTemp);
break;
case 135270408:
case 1073742189:
if (t.data != null) {
floatT = (t.data)[atom.i];
if (t.tok == 1073742189 && floatT != 1 && floatT != 0) {
var o = vwr.getAtomValidation (t.text.substring (13, t.text.length - 1), atom);
if (o == null) {
System.out.println ("?? o is null ??");
} else if (o.size () == 1) {
floatT = o.get (0).floatValue ();
} else {
floatT = NaN;
strT = "";
for (var i = 0, n = o.size (); i < n; i++) {
strT += "," + o.get (i);
}
if (strT.length > 1) strT = strT.substring (1);
}}}break;
case 135266306:
if (t.data != null) {
var sdata = t.data;
strT = (atom.i < sdata.length ? sdata[atom.i] : "");
}break;
case 1632634891:
var formalCharge = atom.getFormalCharge ();
if (formalCharge > 0) strT = "" + formalCharge + "+";
 else if (formalCharge < 0) strT = "" + -formalCharge + "-";
 else strT = "";
break;
case 'g':
strT = "" + atom.getSelectedGroupIndexWithinChain ();
break;
case 1095766030:
strT = atom.getModelNumberForLabel ();
break;
case 1129318401:
strT = "" + JM.Atom.atomPropertyInt (atom, t.tok);
break;
case 'Q':
floatT = atom.getOccupancy100 () / 100;
break;
case 1666189314:
floatT = JM.Atom.atomPropertyFloat (vwr, atom, t.tok, ptTemp);
break;
case 'r':
strT = atom.getSeqcodeString ();
break;
case 1087373324:
strT = atom.getStructureId ();
break;
case 1095761942:
var id = atom.getStrucNo ();
strT = (id <= 0 ? "" : "" + id);
break;
case 1112539150:
floatT = atom.getGroupParameter (1112539150);
if (Float.isNaN (floatT)) strT = "null";
break;
case 4:
strT = vwr.ms.getAtomProp (atom, t.text.substring (2, t.text.length - 1));
break;
case 1641025539:
case 1238369286:
strT = JM.Atom.atomPropertyString (vwr, atom, t.tok);
break;
case 'W':
strT = atom.getIdentityXYZ (false, ptTemp);
break;
default:
switch (t.tok & 1137704960) {
case 1095761920:
if (t.intAsFloat) floatT = JM.Atom.atomPropertyInt (atom, t.tok);
 else strT = "" + JM.Atom.atomPropertyInt (atom, t.tok);
break;
case 1112539136:
floatT = JM.Atom.atomPropertyFloat (vwr, atom, t.tok, ptTemp);
break;
case 1087373312:
strT = JM.Atom.atomPropertyString (vwr, atom, t.tok);
break;
case 1078984704:
ptT = JM.Atom.atomPropertyTuple (atom, t.tok, ptTemp);
break;
default:
}
}
} catch (ioobe) {
if (Clazz.exceptionOf (ioobe, IndexOutOfBoundsException)) {
floatT = NaN;
strT = null;
ptT = null;
} else {
throw ioobe;
}
}
strT = t.format (floatT, strT, ptT);
if (strLabel == null) t.text = strT;
 else strLabel.append (strT);
}, "JV.Viewer,JM.Atom,JM.LabelToken,JU.SB,~A,JU.P3");
Clazz.defineMethod (c$, "format", 
 function (floatT, strT, ptT) {
if (!Float.isNaN (floatT)) {
return JU.PT.formatF (floatT, this.width, this.precision, this.alignLeft, this.zeroPad);
} else if (strT != null) {
return JU.PT.formatS (strT, this.width, this.precision, this.alignLeft, this.zeroPad);
} else if (ptT != null) {
if (this.width == 0 && this.precision == 2147483647) {
this.width = 6;
this.precision = 2;
}return JU.PT.formatF (ptT.x, this.width, this.precision, false, false) + JU.PT.formatF (ptT.y, this.width, this.precision, false, false) + JU.PT.formatF (ptT.z, this.width, this.precision, false, false);
} else {
return this.text;
}}, "~N,~S,JU.T3");
Clazz.defineStatics (c$,
"labelTokenParams", "AaBbCcDEefGgIiLlMmNnoPpQqRrSsTtUuVvWXxxYyyZzz%%%gqW",
"labelTokenIds", [1087373315, 1087375362, 1087375361, 1112541199, 1632634891, 1087373316, 1095761923, 1087373322, 1087375365, 1112539145, 1095761933, 'g', 1112541195, 1095763969, 1095761938, 1095763978, 1095766030, 1087373319, 1095761936, 1087373318, 1089470478, 1112541196, 1112539146, 'Q', 1129318401, 1095761939, 'r', 1095761941, 1087373316, 1112539150, 1112541199, 1087373321, 1112539151, 1649412120, 1146095631, 'W', 1112541188, 1112541185, 1112541205, 1112541189, 1112541186, 1112541206, 1112541190, 1112541187, 1112541207, 1115297793, 1113200642, 1113198595, 1113198596, 1113198597, 1113200646, 1113200647, 1113200649, 1113200650, 1113200652, 1650071565, 1113200654, 1112539137, 1112539138, 1095761922, 1095761924, 1766856708, 1095761932, 1112539140, 1229984263, 1288701959, 1826248716, 1112539143, 1095761935, 1112539141, 1112539144, 1095761937, 1716520985, 1666189314, 1114638363, 1087373323, 1087373320, 1113200651, 1641025539, 1238369286, 1095761942, 1087373324, 1087375373, 1112539152, 1112539153, 1112539154, 1112539155, 1095763991, 1649410049, 1112541202, 1112541203, 1112541204, 1313866249, 1146093582, 1146095627, 1146095626, 1146095629, 1112541191, 1112541192, 1112541193, 1114638362, 1112539147, 1112539148, 1112539149, 1146095628, 1112539142, 1112539139, 1095761927, 1095761940],
"STANDARD_LABEL", "%[identify]",
"twoCharLabelTokenParams", "fuv",
"twoCharLabelTokenIds", [1112541188, 1112541189, 1112541190, 1112539153, 1112539154, 1112539155, 1112541202, 1112541203, 1112541204]);
});
