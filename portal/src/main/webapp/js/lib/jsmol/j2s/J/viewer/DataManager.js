Clazz.declarePackage ("J.viewer");
Clazz.load (["java.util.Hashtable", "J.constant.EnumVdw"], "J.viewer.DataManager", ["J.script.T", "J.util.ArrayUtil", "$.BS", "$.BSUtil", "$.Elements", "$.Escape", "$.Logger", "$.Parser", "$.SB"], function () {
c$ = Clazz.decorateAsClass (function () {
this.dataValues = null;
this.viewer = null;
this.userVdws = null;
this.userVdwMars = null;
this.defaultVdw = null;
this.bsUserVdws = null;
Clazz.instantialize (this, arguments);
}, J.viewer, "DataManager");
Clazz.prepareFields (c$, function () {
this.dataValues =  new java.util.Hashtable ();
this.defaultVdw = J.constant.EnumVdw.JMOL;
});
Clazz.makeConstructor (c$, 
function (viewer) {
this.viewer = viewer;
}, "J.viewer.Viewer");
$_M(c$, "clear", 
function () {
this.dataValues.clear ();
});
$_M(c$, "setData", 
function (type, data, arrayCount, actualAtomCount, matchField, matchFieldColumnCount, field, fieldColumnCount) {
if (type == null) {
this.clear ();
return;
}type = type.toLowerCase ();
if (type.equals ("element_vdw")) {
var stringData = (data[1]).trim ();
if (stringData.length == 0) {
this.userVdwMars = null;
this.userVdws = null;
this.bsUserVdws = null;
return;
}if (this.bsUserVdws == null) this.setUserVdw (this.defaultVdw);
J.util.Parser.parseFloatArrayFromMatchAndField (stringData, this.bsUserVdws, 1, 0, data[2], 2, 0, this.userVdws, 1);
for (var i = this.userVdws.length; --i >= 0; ) this.userVdwMars[i] = Clazz.doubleToInt (Math.floor (this.userVdws[i] * 1000));

return;
}if (data[2] != null && arrayCount > 0) {
var createNew = (matchField != 0 || field != -2147483648 && field != 2147483647);
var oldData = this.dataValues.get (type);
var bs;
var f = (oldData == null || createNew ?  Clazz.newFloatArray (actualAtomCount, 0) : J.util.ArrayUtil.ensureLengthA ((oldData[1]), actualAtomCount));
var depth = (data[3]).intValue ();
var stringData = (depth == 0 ? data[1] : null);
var floatData = (depth == 1 ? data[1] : null);
var strData = null;
if (field == -2147483648 && (strData = J.util.Parser.getTokens (stringData)).length > 1) field = 0;
if (field == -2147483648) {
bs = data[2];
J.util.Parser.setSelectedFloats (J.util.Parser.parseFloatStr (stringData), bs, f);
} else if (field == 0 || field == 2147483647) {
bs = data[2];
if (floatData != null) {
if (floatData.length == bs.cardinality ()) for (var i = bs.nextSetBit (0), pt = 0; i >= 0; i = bs.nextSetBit (i + 1), pt++) f[i] = floatData[pt];

 else for (var i = bs.nextSetBit (0); i >= 0; i = bs.nextSetBit (i + 1)) f[i] = floatData[i];

} else {
J.util.Parser.parseFloatArrayBsData (strData == null ? J.util.Parser.getTokens (stringData) : strData, bs, f);
}} else if (matchField <= 0) {
bs = data[2];
J.util.Parser.parseFloatArrayFromMatchAndField (stringData, bs, 0, 0, null, field, fieldColumnCount, f, 1);
} else {
var iData = data[2];
J.util.Parser.parseFloatArrayFromMatchAndField (stringData, null, matchField, matchFieldColumnCount, iData, field, fieldColumnCount, f, 1);
bs =  new J.util.BS ();
for (var i = iData.length; --i >= 0; ) if (iData[i] >= 0) bs.set (iData[i]);

}if (oldData != null && Clazz.instanceOf (oldData[2], J.util.BS) && !createNew) bs.or ((oldData[2]));
data[3] = Integer.$valueOf (1);
data[2] = bs;
data[1] = f;
if (type.indexOf ("property_atom.") == 0) {
var tok = J.script.T.getSettableTokFromString (type = type.substring (14));
if (tok == 0) {
J.util.Logger.error ("Unknown atom property: " + type);
return;
}var nValues = bs.cardinality ();
var fValues =  Clazz.newFloatArray (nValues, 0);
for (var n = 0, i = bs.nextSetBit (0); n < nValues; i = bs.nextSetBit (i + 1)) fValues[n++] = f[i];

this.viewer.setAtomProperty (bs, tok, 0, 0, null, fValues, null);
return;
}}this.dataValues.put (type, data);
}, "~S,~A,~N,~N,~N,~N,~N,~N");
$_M(c$, "getData", 
function (type) {
if (this.dataValues == null || type == null) return null;
if (!type.equalsIgnoreCase ("types")) return this.dataValues.get (type);
var info =  new Array (2);
info[0] = "types";
info[1] = "";
var n = 0;
for (var name, $name = this.dataValues.keySet ().iterator (); $name.hasNext () && ((name = $name.next ()) || true);) info[1] += (n++ > 0 ? "\n" : "") + name;

return info;
}, "~S");
$_M(c$, "getDataFloatA", 
function (label) {
if (this.dataValues == null) return null;
var data = this.getData (label);
if (data == null || (data[3]).intValue () != 1) return null;
return data[1];
}, "~S");
$_M(c$, "getDataFloat", 
function (label, atomIndex) {
if (this.dataValues != null) {
var data = this.getData (label);
if (data != null && (data[3]).intValue () == 1) {
var f = data[1];
if (atomIndex < f.length) return f[atomIndex];
}}return NaN;
}, "~S,~N");
$_M(c$, "getDataFloat2D", 
function (label) {
if (this.dataValues == null) return null;
var data = this.getData (label);
if (data == null || (data[3]).intValue () != 2) return null;
return data[1];
}, "~S");
$_M(c$, "getDataFloat3D", 
function (label) {
if (this.dataValues == null) return null;
var data = this.getData (label);
if (data == null || (data[3]).intValue () != 3) return null;
return data[1];
}, "~S");
$_M(c$, "deleteModelAtoms", 
function (firstAtomIndex, nAtoms, bsDeleted) {
if (this.dataValues == null) return;
for (var name, $name = this.dataValues.keySet ().iterator (); $name.hasNext () && ((name = $name.next ()) || true);) {
if (name.indexOf ("property_") == 0) {
var obj = this.dataValues.get (name);
J.util.BSUtil.deleteBits (obj[2], bsDeleted);
switch ((obj[3]).intValue ()) {
case 1:
obj[1] = J.util.ArrayUtil.deleteElements (obj[1], firstAtomIndex, nAtoms);
break;
case 2:
obj[1] = J.util.ArrayUtil.deleteElements (obj[1], firstAtomIndex, nAtoms);
break;
default:
break;
}
}}
}, "~N,~N,J.util.BS");
$_M(c$, "setUserVdw", 
($fz = function (mode) {
this.userVdwMars =  Clazz.newIntArray (J.util.Elements.elementNumberMax, 0);
this.userVdws =  Clazz.newFloatArray (J.util.Elements.elementNumberMax, 0);
this.bsUserVdws =  new J.util.BS ();
if (mode === J.constant.EnumVdw.USER) mode = J.constant.EnumVdw.JMOL;
for (var i = 1; i < J.util.Elements.elementNumberMax; i++) {
this.userVdwMars[i] = J.util.Elements.getVanderwaalsMar (i, mode);
this.userVdws[i] = this.userVdwMars[i] / 1000;
}
}, $fz.isPrivate = true, $fz), "J.constant.EnumVdw");
$_M(c$, "setDefaultVdw", 
function (type) {
switch (type) {
case J.constant.EnumVdw.JMOL:
case J.constant.EnumVdw.BABEL:
case J.constant.EnumVdw.RASMOL:
case J.constant.EnumVdw.AUTO:
case J.constant.EnumVdw.USER:
break;
default:
type = J.constant.EnumVdw.JMOL;
}
if (type !== this.defaultVdw && type === J.constant.EnumVdw.USER && this.bsUserVdws == null) this.setUserVdw (this.defaultVdw);
this.defaultVdw = type;
}, "J.constant.EnumVdw");
$_M(c$, "getDefaultVdwNameOrData", 
function (mode, type, bs) {
switch (mode) {
case -2147483648:
return this.defaultVdw.getVdwLabel ();
case 2147483647:
if ((bs = this.bsUserVdws) == null) return "";
type = J.constant.EnumVdw.USER;
break;
}
if (type == null || type === J.constant.EnumVdw.AUTO) type = this.defaultVdw;
if (type === J.constant.EnumVdw.USER && this.bsUserVdws == null) this.setUserVdw (this.defaultVdw);
var sb =  new J.util.SB ();
sb.append (type.getVdwLabel ()).append ("\n");
var isAll = (bs == null);
var i0 = (isAll ? 1 : bs.nextSetBit (0));
var i1 = (isAll ? J.util.Elements.elementNumberMax : bs.length ());
for (var i = i0; i < i1 && i >= 0; i = (isAll ? i + 1 : bs.nextSetBit (i + 1))) sb.appendI (i).appendC ('\t').appendF (type === J.constant.EnumVdw.USER ? this.userVdws[i] : J.util.Elements.getVanderwaalsMar (i, type) / 1000).appendC ('\t').append (J.util.Elements.elementSymbolFromNumber (i)).appendC ('\n');

return (bs == null ? sb.toString () : "\n  DATA \"element_vdw\"\n" + sb.append ("  end \"element_vdw\";\n\n").toString ());
}, "~N,J.constant.EnumVdw,J.util.BS");
c$.getInlineData = $_M(c$, "getInlineData", 
function (loadScript, strModel, isAppend, loadFilter) {
var tag = (isAppend ? "append" : "model") + " inline";
loadScript.append ("load /*data*/ data \"").append (tag).append ("\"\n").append (strModel).append ("end \"").append (tag).append (loadFilter == null || loadFilter.length == 0 ? "" : " filter" + J.util.Escape.eS (loadFilter)).append ("\";");
}, "J.util.SB,~S,~B,~S");
Clazz.defineStatics (c$,
"DATA_TYPE_STRING", 0,
"DATA_TYPE_AF", 1,
"DATA_ARRAY_FF", 2,
"DATA_ARRAY_FFF", 3,
"DATA_VALUE", 1,
"DATA_SELECTION_MAP", 2,
"DATA_TYPE", 3,
"DATA_SAVE_IN_STATE", 4);
});
