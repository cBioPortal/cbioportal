Clazz.declarePackage ("J.modelset");
Clazz.load (null, "J.modelset.LabelToken", ["java.lang.Character", "$.Float", "java.util.Hashtable", "J.modelset.Atom", "J.script.T", "J.util.Escape", "$.SB", "$.TextFormat"], function () {
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
}, J.modelset, "LabelToken");
c$.isLabelPropertyTok = $_M(c$, "isLabelPropertyTok", 
($fz = function (tok) {
for (var i = J.modelset.LabelToken.labelTokenIds.length; --i >= 0; ) if (J.modelset.LabelToken.labelTokenIds[i] == tok) return true;

return false;
}, $fz.isPrivate = true, $fz), "~N");
Clazz.makeConstructor (c$, 
($fz = function (text, pt) {
this.text = text;
this.pt = pt;
}, $fz.isPrivate = true, $fz), "~S,~N");
c$.compile = $_M(c$, "compile", 
function (viewer, strFormat, chAtom, htValues) {
if (strFormat == null || strFormat.length == 0) return null;
if (strFormat.indexOf ("%") < 0 || strFormat.length < 2) return [ new J.modelset.LabelToken (strFormat, -1)];
var n = 0;
var ich = -1;
var cch = strFormat.length;
while (++ich < cch && (ich = strFormat.indexOf ('%', ich)) >= 0) n++;

var tokens =  new Array (n * 2 + 1);
var ichPercent;
var i = 0;
for (ich = 0; (ichPercent = strFormat.indexOf ('%', ich)) >= 0; ) {
if (ich != ichPercent) tokens[i++] =  new J.modelset.LabelToken (strFormat.substring (ich, ichPercent), -1);
var lt = tokens[i++] =  new J.modelset.LabelToken (null, ichPercent);
viewer.autoCalculate (lt.tok);
ich = J.modelset.LabelToken.setToken (viewer, strFormat, lt, cch, chAtom.charCodeAt (0), htValues);
}
if (ich < cch) tokens[i++] =  new J.modelset.LabelToken (strFormat.substring (ich), -1);
return tokens;
}, "J.viewer.Viewer,~S,~S,java.util.Map");
c$.formatLabel = $_M(c$, "formatLabel", 
function (viewer, atom, strFormat) {
if (strFormat == null || strFormat.length == 0) return null;
var tokens = J.modelset.LabelToken.compile (viewer, strFormat, '\0', null);
return J.modelset.LabelToken.formatLabelAtomArray (viewer, atom, tokens, '\0', null);
}, "J.viewer.Viewer,J.modelset.Atom,~S");
c$.formatLabelAtomArray = $_M(c$, "formatLabelAtomArray", 
function (viewer, atom, tokens, chAtom, indices) {
if (atom == null) return null;
var strLabel = (chAtom > '0' ? null :  new J.util.SB ());
if (tokens != null) for (var i = 0; i < tokens.length; i++) {
var t = tokens[i];
if (t == null) break;
if (chAtom > '0' && t.ch1 != chAtom) continue;
if (t.tok <= 0 || t.key != null) {
if (strLabel != null) {
strLabel.append (t.text);
if (t.ch1 != '\0') strLabel.appendC (t.ch1);
}} else {
J.modelset.LabelToken.appendAtomTokenValue (viewer, atom, t, strLabel, indices);
}}
return (strLabel == null ? null : strLabel.toString ().intern ());
}, "J.viewer.Viewer,J.modelset.Atom,~A,~S,~A");
c$.getBondLabelValues = $_M(c$, "getBondLabelValues", 
function () {
var htValues =  new java.util.Hashtable ();
htValues.put ("#", "");
htValues.put ("ORDER", "");
htValues.put ("TYPE", "");
htValues.put ("LENGTH", Float.$valueOf (0));
htValues.put ("ENERGY", Float.$valueOf (0));
return htValues;
});
c$.formatLabelBond = $_M(c$, "formatLabelBond", 
function (viewer, bond, tokens, values, indices) {
values.put ("#", "" + (bond.index + 1));
values.put ("ORDER", "" + bond.getOrderNumberAsString ());
values.put ("TYPE", bond.getOrderName ());
values.put ("LENGTH", Float.$valueOf (bond.atom1.distance (bond.atom2)));
values.put ("ENERGY", Float.$valueOf (bond.getEnergy ()));
J.modelset.LabelToken.setValues (tokens, values);
J.modelset.LabelToken.formatLabelAtomArray (viewer, bond.atom1, tokens, '1', indices);
J.modelset.LabelToken.formatLabelAtomArray (viewer, bond.atom2, tokens, '2', indices);
return J.modelset.LabelToken.getLabel (tokens);
}, "J.viewer.Viewer,J.modelset.Bond,~A,java.util.Map,~A");
c$.formatLabelMeasure = $_M(c$, "formatLabelMeasure", 
function (viewer, m, label, value, units) {
var htValues =  new java.util.Hashtable ();
htValues.put ("#", "" + (m.index + 1));
htValues.put ("VALUE", Float.$valueOf (value));
htValues.put ("UNITS", units);
var tokens = J.modelset.LabelToken.compile (viewer, label, '\1', htValues);
if (tokens == null) return "";
J.modelset.LabelToken.setValues (tokens, htValues);
var atoms = m.modelSet.atoms;
var indices = m.getCountPlusIndices ();
for (var i = indices[0]; i >= 1; --i) if (indices[i] >= 0) J.modelset.LabelToken.formatLabelAtomArray (viewer, atoms[indices[i]], tokens, String.fromCharCode (48 + i), null);

label = J.modelset.LabelToken.getLabel (tokens);
return (label == null ? "" : label);
}, "J.viewer.Viewer,J.modelset.Measurement,~S,~N,~S");
c$.setValues = $_M(c$, "setValues", 
function (tokens, values) {
for (var i = 0; i < tokens.length; i++) {
var lt = tokens[i];
if (lt == null) break;
if (lt.key == null) continue;
var value = values.get (lt.key);
lt.text = (Clazz.instanceOf (value, Float) ? lt.format ((value).floatValue (), null, null) : lt.format (NaN, value, null));
}
}, "~A,java.util.Map");
c$.getLabel = $_M(c$, "getLabel", 
function (tokens) {
var sb =  new J.util.SB ();
for (var i = 0; i < tokens.length; i++) {
var lt = tokens[i];
if (lt == null) break;
sb.append (lt.text);
}
return sb.toString ();
}, "~A");
c$.setToken = $_M(c$, "setToken", 
($fz = function (viewer, strFormat, lt, cch, chAtom, htValues) {
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
lt.tok = 135270407;
lt.data = viewer.getDataFloat (lt.text);
} else {
var token = J.script.T.getTokenFromName (propertyName);
if (token != null && J.modelset.LabelToken.isLabelPropertyTok (token.tok)) lt.tok = token.tok;
}ich = ichClose + 1;
break;
case '{':
var ichCloseBracket = strFormat.indexOf ('}', ich);
if (ichCloseBracket < ich) {
ich = cch;
break;
}lt.text = strFormat.substring (ich, ichCloseBracket);
lt.data = viewer.getDataFloat (lt.text);
if (lt.data == null) {
lt.data = viewer.getData (lt.text);
if (Clazz.instanceOf (lt.data, Array)) {
lt.data = (lt.data)[1];
if (Clazz.instanceOf (lt.data, String)) lt.data = J.util.TextFormat.split (lt.data, '\n');
if (!(J.util.Escape.isAS (lt.data))) lt.data = null;
}lt.tok = (lt.data == null ? 4 : 135266306);
} else {
lt.tok = 135270407;
}ich = ichCloseBracket + 1;
break;
default:
var i;
var i1;
if (ich < cch && (i = "fuv".indexOf (ch)) >= 0 && (i1 = "xyz".indexOf (strFormat.charAt (ich))) >= 0) {
lt.tok = J.modelset.LabelToken.twoCharLabelTokenIds[i * 3 + i1];
ich++;
} else if ((i = "AaBbCcDEefGgIiLlMmNnoPpQqRrSsTtUuVvWXxYyZz%%%gqW".indexOf (ch)) >= 0) {
lt.tok = J.modelset.LabelToken.labelTokenIds[i];
}}
lt.text = strFormat.substring (lt.pt, ich);
if (ich < cch && chAtom != 0 && Character.isDigit (ch = strFormat.charAt (ich))) {
ich++;
lt.ch1 = ch;
if (ch.charCodeAt (0) != chAtom && chAtom != 1) lt.tok = 0;
}return ich;
}, $fz.isPrivate = true, $fz), "J.viewer.Viewer,~S,J.modelset.LabelToken,~N,~N,java.util.Map");
c$.appendAtomTokenValue = $_M(c$, "appendAtomTokenValue", 
($fz = function (viewer, atom, t, strLabel, indices) {
var strT = null;
var floatT = NaN;
var ptT = null;
try {
switch (t.tok) {
case 1095761923:
strT = "" + (indices == null ? atom.index : indices[atom.index]);
break;
case 1766856708:
ptT = J.modelset.Atom.atomPropertyTuple (atom, t.tok);
break;
case 135270407:
if (t.data != null) {
floatT = (t.data)[atom.index];
}break;
case 135266306:
if (t.data != null) {
var sdata = t.data;
strT = (atom.index < sdata.length ? sdata[atom.index] : "");
}break;
case 1632634889:
var formalCharge = atom.getFormalCharge ();
if (formalCharge > 0) strT = "" + formalCharge + "+";
 else if (formalCharge < 0) strT = "" + -formalCharge + "-";
 else strT = "";
break;
case 'g':
strT = "" + atom.getSelectedGroupIndexWithinChain ();
break;
case 1095766028:
strT = atom.getModelNumberForLabel ();
break;
case 1129318401:
strT = "" + J.modelset.Atom.atomPropertyInt (atom, t.tok);
break;
case 'Q':
floatT = atom.getOccupancy100 () / 100;
break;
case 1666189314:
floatT = J.modelset.Atom.atomPropertyFloat (viewer, atom, t.tok);
break;
case 'r':
strT = atom.getSeqcodeString ();
break;
case 1087373324:
strT = atom.getStructureId ();
break;
case 1095761939:
var id = atom.getStrucNo ();
strT = (id <= 0 ? "" : "" + id);
break;
case 1112539150:
floatT = atom.getGroupParameter (1112539150);
if (Float.isNaN (floatT)) strT = "null";
break;
case 4:
strT = viewer.getModelAtomProperty (atom, t.text.substring (2, t.text.length - 1));
break;
case 1641025539:
case 1238369286:
strT = J.modelset.Atom.atomPropertyString (viewer, atom, t.tok);
break;
case 'W':
strT = atom.getIdentityXYZ (false);
break;
default:
switch (t.tok & 1137704960) {
case 1095761920:
if (t.intAsFloat) floatT = J.modelset.Atom.atomPropertyInt (atom, t.tok);
 else strT = "" + J.modelset.Atom.atomPropertyInt (atom, t.tok);
break;
case 1112539136:
floatT = J.modelset.Atom.atomPropertyFloat (viewer, atom, t.tok);
break;
case 1087373312:
strT = J.modelset.Atom.atomPropertyString (viewer, atom, t.tok);
break;
case 1078984704:
ptT = J.modelset.Atom.atomPropertyTuple (atom, t.tok);
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
}, $fz.isPrivate = true, $fz), "J.viewer.Viewer,J.modelset.Atom,J.modelset.LabelToken,J.util.SB,~A");
$_M(c$, "format", 
($fz = function (floatT, strT, ptT) {
if (!Float.isNaN (floatT)) {
return J.util.TextFormat.formatF (floatT, this.width, this.precision, this.alignLeft, this.zeroPad);
} else if (strT != null) {
return J.util.TextFormat.formatS (strT, this.width, this.precision, this.alignLeft, this.zeroPad);
} else if (ptT != null) {
if (this.width == 0 && this.precision == 2147483647) {
this.width = 6;
this.precision = 2;
}return J.util.TextFormat.formatF (ptT.x, this.width, this.precision, false, false) + J.util.TextFormat.formatF (ptT.y, this.width, this.precision, false, false) + J.util.TextFormat.formatF (ptT.z, this.width, this.precision, false, false);
} else {
return this.text;
}}, $fz.isPrivate = true, $fz), "~N,~S,J.util.Tuple3f");
Clazz.defineStatics (c$,
"labelTokenParams", "AaBbCcDEefGgIiLlMmNnoPpQqRrSsTtUuVvWXxYyZz%%%gqW",
"labelTokenIds", [1087373315, 1087375362, 1087375361, 1112541199, 1632634889, 1087373316, 1095761923, 1087373322, 1087375365, 1112539145, 1095761931, 'g', 1112541195, 1095763969, 1095761936, 1095763976, 1095766028, 1087373319, 1095761934, 1087373318, 1089470478, 1112541196, 1112539146, 'Q', 1129318401, 1095761937, 'r', 1095761938, 1087373316, 1112539150, 1112541199, 1087373321, 1112539151, 1649412120, 1146095631, 'W', 1112541188, 1112541185, 1112541189, 1112541186, 1112541190, 1112541187, 1115297793, 1113200642, 1113198595, 1113198596, 1113198597, 1113200646, 1113200647, 1113200649, 1113200650, 1113200652, 1650071565, 1113200654, 1112539137, 1112539138, 1095761922, 1095761924, 1766856708, 1095761930, 1112539140, 1229984263, 1288701960, 1826248715, 1112539143, 1095761933, 1112539141, 1112539144, 1095761935, 1716520985, 1666189314, 1114638363, 1087373323, 1087373320, 1113200651, 1641025539, 1238369286, 1095761939, 1087373324, 1087375373, 1112539152, 1112539153, 1112539154, 1112539155, 1095763988, 1649410049, 1112541202, 1112541203, 1112541204, 1313866247, 1146093582, 1146095627, 1146095626, 1146095629, 1112541191, 1112541192, 1112541193, 1114638362, 1112539147, 1112539148, 1112539149, 1146095628, 1112539142, 1112539139],
"STANDARD_LABEL", "%[identify]",
"twoCharLabelTokenParams", "fuv",
"twoCharLabelTokenIds", [1112541188, 1112541189, 1112541190, 1112539153, 1112539154, 1112539155, 1112541202, 1112541203, 1112541204]);
});
