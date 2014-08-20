Clazz.declarePackage ("JV");
Clazz.load (["J.api.JmolPropertyManager", "java.util.Hashtable"], "JV.PropertyManager", ["java.lang.Boolean", "$.Character", "$.Double", "$.Float", "java.util.Arrays", "$.Date", "$.Map", "JU.AU", "$.BArray", "$.BS", "$.Base64", "$.Lst", "$.M3", "$.M4", "$.P3", "$.PT", "$.SB", "$.V3", "J.api.Interface", "JM.Atom", "$.BondSet", "$.LabelToken", "JS.SV", "$.T", "JU.BSUtil", "$.C", "$.Edge", "$.Elements", "$.Escape", "$.JmolMolecule", "$.Logger", "$.Txt", "JV.ActionManager", "$.JC", "$.Viewer", "JV.binding.Binding"], function () {
c$ = Clazz.decorateAsClass (function () {
this.vwr = null;
this.map = null;
Clazz.instantialize (this, arguments);
}, JV, "PropertyManager", null, J.api.JmolPropertyManager);
Clazz.prepareFields (c$, function () {
this.map =  new java.util.Hashtable ();
});
Clazz.makeConstructor (c$, 
function () {
});
Clazz.overrideMethod (c$, "setViewer", 
function (vwr) {
this.vwr = vwr;
for (var i = 0, p = 0; i < JV.PropertyManager.propertyTypes.length; i += 3) this.map.put (JV.PropertyManager.propertyTypes[i].toLowerCase (), Integer.$valueOf (p++));

}, "JV.Viewer");
Clazz.overrideMethod (c$, "getPropertyNumber", 
function (infoType) {
var n = this.map.get (infoType == null ? "" : infoType.toLowerCase ());
return (n == null ? -1 : n.intValue ());
}, "~S");
Clazz.overrideMethod (c$, "getDefaultPropertyParam", 
function (propID) {
return (propID < 0 ? "" : JV.PropertyManager.propertyTypes[propID * 3 + 2]);
}, "~N");
Clazz.overrideMethod (c$, "checkPropertyParameter", 
function (name) {
var propID = this.getPropertyNumber (name);
var type = JV.PropertyManager.getParamType (propID);
return (type.length > 0 && type !== "<atom selection>");
}, "~S");
Clazz.overrideMethod (c$, "getProperty", 
function (returnType, infoType, paramInfo) {
if (JV.PropertyManager.propertyTypes.length != 132) JU.Logger.warn ("propertyTypes is not the right length: " + JV.PropertyManager.propertyTypes.length + " != " + 132);
var info;
if (infoType.indexOf (".") >= 0 || infoType.indexOf ("[") >= 0) {
var args = this.getArguments (infoType);
info = this.extractProperty (this.getPropertyAsObject (args[0].asString (), paramInfo, null), args, 1, null, false);
} else {
info = this.getPropertyAsObject (infoType, paramInfo, returnType);
}if (returnType == null) return info;
var requestedReadable = returnType.equalsIgnoreCase ("readable");
if (requestedReadable) returnType = (JV.PropertyManager.isReadableAsString (infoType) ? "String" : "JSON");
if (returnType.equalsIgnoreCase ("String")) return (info == null ? "" : info.toString ());
if (requestedReadable) return JU.Escape.toReadable (infoType, info);
 else if (returnType.equalsIgnoreCase ("JSON")) return "{" + JU.PT.toJSON (infoType, info) + "}";
return info;
}, "~S,~S,~O");
Clazz.defineMethod (c$, "getArguments", 
 function (propertyName) {
var lc = propertyName.toLowerCase ();
var pt = -1;
while ((pt = lc.indexOf ("[select ", ++pt)) >= 0) {
var pt2 = lc.indexOf (" where ", pt);
var pt3 = lc.indexOf ("]", pt);
if (pt2 < 0 || pt2 > pt3) continue;
propertyName = propertyName.substring (0, pt2) + propertyName.substring (pt2, pt3).$replace ('.', '\1') + propertyName.substring (pt3);
}
propertyName = propertyName.$replace (']', '\0').$replace ('[', '\0').$replace ('.', '\0').$replace ('\1', '.');
propertyName = JU.PT.rep (propertyName, "\0\0", "\0");
var names = JU.PT.split (JU.PT.trim (propertyName, "\0"), "\0");
var args =  new Array (names.length);
for (var i = 0, n; i < names.length; i++) args[i] = (names[i].startsWith ("'") || names[i].startsWith ("\"") ? JS.SV.newS (JU.PT.trim (names[i], "'\"")) : (n = JU.PT.parseInt (names[i])) == -2147483648 ? JS.SV.newS (names[i]) : JS.SV.newI (n));

return args;
}, "~S");
Clazz.overrideMethod (c$, "extractProperty", 
function (prop, args, ptr, v2, isCompiled) {
if (ptr < 0) {
args = this.getArguments (args);
ptr = 0;
}if (ptr >= (args).length) return prop;
if (!isCompiled) args = this.compileSelect (args);
var pt;
var arg = (args)[ptr++];
var property = this.getObj (prop);
switch (arg.tok) {
case 2:
pt = arg.intValue - 1;
if (Clazz.instanceOf (property, JU.Lst)) {
var v = property;
if (pt < 0) pt += v.size ();
return (pt >= 0 && pt < v.size () ? this.extractProperty (v.get (pt), args, ptr, null, true) : "");
}if (Clazz.instanceOf (property, JU.M3)) {
var m = property;
var f = [[m.m00, m.m01, m.m02], [m.m10, m.m11, m.m12], [m.m20, m.m21, m.m22]];
if (pt < 0) pt += 3;
if (pt >= 0 && pt < 3) return this.extractProperty (f, args, --ptr, null, true);
return "";
}if (Clazz.instanceOf (property, JU.M4)) {
var m = property;
var f = [[m.m00, m.m01, m.m02, m.m03], [m.m10, m.m11, m.m12, m.m13], [m.m20, m.m21, m.m22, m.m23], [m.m30, m.m31, m.m32, m.m33]];
if (pt < 0) pt += 4;
if (pt >= 0 && pt < 4) return this.extractProperty (f, args, --ptr, null, true);
return "";
}if (JU.PT.isAI (property)) {
var ilist = property;
if (pt < 0) pt += ilist.length;
if (pt >= 0 && pt < ilist.length) return Integer.$valueOf (ilist[pt]);
return "";
}if (JU.PT.isAD (property)) {
var dlist = property;
if (pt < 0) pt += dlist.length;
if (pt >= 0 && pt < dlist.length) return Double.$valueOf (dlist[pt]);
return "";
}if (JU.PT.isAF (property)) {
var flist = property;
if (pt < 0) pt += flist.length;
if (pt >= 0 && pt < flist.length) return Float.$valueOf (flist[pt]);
return "";
}if (JU.PT.isAII (property)) {
var iilist = property;
if (pt < 0) pt += iilist.length;
if (pt >= 0 && pt < iilist.length) return this.extractProperty (iilist[pt], args, ptr, null, true);
return "";
}if (JU.PT.isAFF (property)) {
var fflist = property;
if (pt < 0) pt += fflist.length;
if (pt >= 0 && pt < fflist.length) return this.extractProperty (fflist[pt], args, ptr, null, true);
return "";
}if (JU.PT.isAS (property)) {
var slist = property;
if (pt < 0) pt += slist.length;
if (pt >= 0 && pt < slist.length) return slist[pt];
return "";
}if (Clazz.instanceOf (property, Array)) {
var olist = property;
if (pt < 0) pt += olist.length;
if (pt >= 0 && pt < olist.length) return olist[pt];
return "";
}break;
case 135280132:
case 4:
if (Clazz.instanceOf (property, java.util.Map)) {
var h = property;
var key;
if (arg.tok == 135280132) {
key = arg.myName;
if (!this.vwr.checkSelect (property, arg.value)) return "";
} else {
key = arg.asString ();
if (key.equalsIgnoreCase ("keys")) {
var keys =  new JU.Lst ();
for (var k, $k = h.keySet ().iterator (); $k.hasNext () && ((k = $k.next ()) || true);) keys.addLast (k);

return this.extractProperty (keys, args, ptr, null, true);
}}var isWild = (key.startsWith ("*") || key.endsWith ("*") || key.indexOf (",") >= 0);
if (isWild && v2 == null) v2 =  new JU.Lst ();
if (isWild && key.length == 1) {
if (ptr == (args).length) {
v2.addLast (property);
return v2;
}return this.extractProperty (property, args, ptr, v2, true);
}if (key.contains (",")) {
var mapNew = null;
mapNew =  new java.util.Hashtable ();
var tokens = JU.PT.split (key, ",");
for (var i = tokens.length; --i >= 0; ) JU.PT.getMapSubset (h, tokens[i], mapNew);

if (ptr == (args).length) {
v2.addLast (mapNew);
return v2;
}return this.extractProperty (mapNew, args, ptr, v2, true);
}key = this.checkMap (h, key, isWild, v2, args, ptr);
return (key != null && !isWild ? this.extractProperty (h.get (key), args, ptr, null, true) : isWild ? v2 : "");
}if (Clazz.instanceOf (property, JU.Lst)) {
var v = property;
if (v2 == null) v2 =  new JU.Lst ();
ptr--;
for (pt = 0; pt < v.size (); pt++) {
var o = v.get (pt);
if (Clazz.instanceOf (o, java.util.Map) || Clazz.instanceOf (o, JU.Lst) || (Clazz.instanceOf (o, JS.SV)) && ((o).getMap () != null || (o).getList () != null)) this.extractProperty (o, args, ptr, v2, true);
}
return v2;
}break;
}
return prop;
}, "~O,~O,~N,JU.Lst,~B");
Clazz.defineMethod (c$, "compileSelect", 
 function (args) {
var argsNew = null;
for (var i = args.length; --i >= 0; ) {
if (args[i].tok == 4) {
var key = args[i].value;
if (key.toUpperCase ().startsWith ("SELECT ")) {
if (argsNew == null) argsNew = JU.AU.arrayCopyObject (args, args.length);
key = key.substring (6).trim ();
if (key.toUpperCase ().startsWith ("WHERE ")) key = "* " + key;
var pt = key.toUpperCase ().indexOf (" WHERE ");
if (pt < 0) {
argsNew[i].value = key;
} else {
argsNew[i] = JS.SV.newV (135280132, this.vwr.compileExpr (key.substring (pt + 6).trim ()));
argsNew[i].myName = key.substring (0, pt).trim ();
}}}}
return (argsNew == null ? args : argsNew);
}, "~A");
Clazz.defineMethod (c$, "checkMap", 
 function (h, key, isWild, v2, args, ptr) {
var isOK = (v2 == null && h.containsKey (key));
if (!isOK) {
var lckey = (isWild ? key.toLowerCase () : null);
for (var k, $k = h.keySet ().iterator (); $k.hasNext () && ((k = $k.next ()) || true);) {
if (k.equalsIgnoreCase (key) || lckey != null && JU.PT.isLike (k.toLowerCase (), lckey)) {
if (v2 == null) return k;
v2.addLast (this.extractProperty (h.get (k), args, ptr, null, true));
if (!isWild) return null;
}}
}return (isOK ? key : null);
}, "java.util.Map,~S,~B,JU.Lst,~O,~N");
Clazz.defineMethod (c$, "getObj", 
 function (prop) {
return (Clazz.instanceOf (prop, JS.SV) ? JS.SV.oValue (prop) : prop);
}, "~O");
c$.getPropertyName = Clazz.defineMethod (c$, "getPropertyName", 
 function (propID) {
return (propID < 0 ? "" : JV.PropertyManager.propertyTypes[propID * 3]);
}, "~N");
c$.getParamType = Clazz.defineMethod (c$, "getParamType", 
 function (propID) {
return (propID < 0 ? "" : JV.PropertyManager.propertyTypes[propID * 3 + 1]);
}, "~N");
c$.isReadableAsString = Clazz.defineMethod (c$, "isReadableAsString", 
 function (infoType) {
for (var i = JV.PropertyManager.readableTypes.length; --i >= 0; ) if (infoType.equalsIgnoreCase (JV.PropertyManager.readableTypes[i])) return true;

return false;
}, "~S");
Clazz.defineMethod (c$, "getPropertyAsObject", 
 function (infoType, paramInfo, returnType) {
if (infoType.equals ("tokenList")) {
return JS.T.getTokensLike (paramInfo);
}var id = this.getPropertyNumber (infoType);
var iHaveParameter = (paramInfo != null && paramInfo.toString ().length > 0);
var myParam = (iHaveParameter ? paramInfo : this.getDefaultPropertyParam (id));
switch (id) {
case 0:
return this.getAppletInfo ();
case 5:
return this.getAnimationInfo ();
case 13:
return this.vwr.getAtomBitSetVector (myParam);
case 14:
return this.getAllAtomInfo (this.vwr.getAtomBitSet (myParam));
case 24:
return this.getAuxiliaryInfo (myParam);
case 15:
return this.getAllBondInfo (myParam);
case 25:
return this.getBoundBoxInfo ();
case 10:
return this.vwr.tm.getRotationCenter ();
case 16:
return this.getAllChainInfo (this.vwr.getAtomBitSet (myParam));
case 37:
return this.vwr.getProperty ("DATA_API", "consoleText", null);
case 26:
return this.vwr.getData (myParam.toString ());
case 33:
return this.vwr.getErrorMessageUn ();
case 28:
return this.vwr.evaluateExpression (myParam.toString ());
case 20:
return this.vwr.getModelExtract (myParam, true, false, "MOL");
case 32:
return JV.PropertyManager.getFileInfo (this.vwr.getFileData (), myParam.toString ());
case 1:
return this.vwr.getFullPathName (false);
case 2:
return this.vwr.getFileHeader ();
case 4:
case 3:
return (iHaveParameter ? this.vwr.getFileAsString (myParam.toString (), true) : this.vwr.getCurrentFileAsString ());
case 27:
var params = myParam.toString ().toLowerCase ();
return this.getImage (params, params.indexOf ("g64") < 0 && params.indexOf ("base64") < 0 && (returnType == null || returnType.equalsIgnoreCase ("java")));
case 35:
return this.vwr.getShapeProperty (24, "getInfo");
case 36:
return this.vwr.getShapeProperty (24, "getData");
case 40:
return this.vwr.getNMRCalculation ().getInfo (myParam.toString ());
case 41:
return this.getVariables (myParam.toString ());
case 21:
return this.vwr.getStatusChanged (myParam.toString ());
case 22:
return this.vwr;
case 38:
return this.vwr.getJspecViewProperties (myParam);
case 7:
return this.getLigandInfo (this.vwr.getAtomBitSet (myParam));
case 9:
return this.getMeasurementInfo ();
case 29:
return this.vwr.getMenu (myParam.toString ());
case 23:
return this.vwr.sm.getMessageQueue ();
case 30:
return this.vwr.getMinimizationInfo ();
case 6:
return this.getModelInfo (this.vwr.getAtomBitSet (myParam));
case 18:
return this.getMoleculeInfo (this.vwr.getAtomBitSet (myParam));
case 34:
return this.getMouseInfo ();
case 11:
return this.vwr.tm.getOrientationInfo ();
case 31:
return this.vwr.getPointGroupInfo (myParam);
case 17:
return this.getAllPolymerInfo (this.vwr.getAtomBitSet (myParam));
case 39:
return this.vwr.getScriptQueueInfo ();
case 8:
return this.getShapeInfo ();
case 19:
return this.vwr.getStateInfo3 (myParam.toString (), 0, 0);
case 12:
return this.vwr.tm.getMatrixRotate ();
case 42:
return this.getAnnotationInfo (myParam, 1073741925);
case 43:
return this.getAnnotationInfo (myParam, 1073742189);
}
var data =  new Array (44);
for (var i = 0; i < 44; i++) {
var paramType = JV.PropertyManager.getParamType (i);
var paramDefault = this.getDefaultPropertyParam (i);
var name = JV.PropertyManager.getPropertyName (i);
data[i] = (name.charAt (0) == 'X' ? "" : name + (paramType !== "" ? " " + JV.PropertyManager.getParamType (i) + (paramDefault !== "" ? " #default: " + this.getDefaultPropertyParam (i) : "") : ""));
}
java.util.Arrays.sort (data);
var info =  new JU.SB ();
info.append ("getProperty ERROR\n").append (infoType).append ("?\nOptions include:\n");
for (var i = 0; i < 44; i++) if (data[i].length > 0) info.append ("\n getProperty ").append (data[i]);

return info.toString ();
}, "~S,~O,~S");
Clazz.defineMethod (c$, "getImage", 
 function (params, asBytes) {
var height = -1;
var width = -1;
var pt;
if ((pt = params.indexOf ("height=")) >= 0) height = JU.PT.parseInt (params.substring (pt + 7));
if ((pt = params.indexOf ("width=")) >= 0) width = JU.PT.parseInt (params.substring (pt + 6));
if (width < 0 && height < 0) height = width = -1;
 else if (width < 0) width = height;
 else height = width;
var type = "JPG";
if (params.indexOf ("type=") >= 0) type = JU.PT.getTokens (JU.PT.replaceWithCharacter (params.substring (params.indexOf ("type=") + 5), ";,", ' '))[0];
var errMsg =  new Array (1);
var bytes = this.vwr.getImageAsBytes (type.toUpperCase (), width, height, -1, errMsg);
return (errMsg[0] != null ? errMsg[0] : asBytes ?  new JU.BArray (bytes) : JU.Base64.getBase64 (bytes).toString ());
}, "~S,~B");
Clazz.defineMethod (c$, "getVariables", 
 function (name) {
return (name.toLowerCase ().equals ("all") ? this.vwr.g.getAllVariables () : this.vwr.evaluateExpressionAsVariable (name));
}, "~S");
c$.getFileInfo = Clazz.defineMethod (c$, "getFileInfo", 
function (objHeader, type) {
var ht =  new java.util.Hashtable ();
if (objHeader == null) return ht;
var haveType = (type != null && type.length > 0);
if (Clazz.instanceOf (objHeader, java.util.Map)) {
return (haveType ? (objHeader).get (type) : objHeader);
}var lines = JU.PT.split (objHeader, "\n");
if (lines.length == 0 || lines[0].length < 6 || lines[0].charAt (6) != ' ' || !lines[0].substring (0, 6).equals (lines[0].substring (0, 6).toUpperCase ())) {
ht.put ("fileHeader", objHeader);
return ht;
}var keyLast = "";
var sb =  new JU.SB ();
if (haveType) type = type.toUpperCase ();
var key = "";
for (var i = 0; i < lines.length; i++) {
var line = lines[i];
if (line.length < 12) continue;
key = line.substring (0, 6).trim ();
var cont = line.substring (7, 10).trim ();
if (key.equals ("REMARK")) {
key += cont;
}if (!key.equals (keyLast)) {
if (haveType && keyLast.equals (type)) return sb.toString ();
if (!haveType) {
ht.put (keyLast, sb.toString ());
sb =  new JU.SB ();
}keyLast = key;
}if (!haveType || key.equals (type)) sb.append (line).appendC ('\n');
}
if (!haveType) {
ht.put (keyLast, sb.toString ());
}if (haveType) return (key.equals (type) ? sb.toString () : "");
return ht;
}, "~O,~S");
Clazz.defineMethod (c$, "getMoleculeInfo", 
function (atomExpression) {
var bsAtoms = this.vwr.getAtomBitSet (atomExpression);
var molecules = this.vwr.ms.getMolecules ();
var V =  new JU.Lst ();
var bsTemp =  new JU.BS ();
for (var i = 0; i < molecules.length; i++) {
bsTemp = JU.BSUtil.copy (bsAtoms);
var m = molecules[i];
bsTemp.and (m.atomList);
if (bsTemp.length () > 0) {
var info =  new java.util.Hashtable ();
info.put ("mf", m.getMolecularFormula (false));
info.put ("number", Integer.$valueOf (m.moleculeIndex + 1));
info.put ("modelNumber", this.vwr.ms.getModelNumberDotted (m.modelIndex));
info.put ("numberInModel", Integer.$valueOf (m.indexInModel + 1));
info.put ("nAtoms", Integer.$valueOf (m.ac));
info.put ("nElements", Integer.$valueOf (m.nElements));
V.addLast (info);
}}
return V;
}, "~O");
Clazz.overrideMethod (c$, "getModelInfo", 
function (atomExpression) {
var bsModels = this.vwr.ms.getModelBS (this.vwr.getAtomBitSet (atomExpression), false);
var m = this.vwr.ms;
var info =  new java.util.Hashtable ();
info.put ("modelSetName", m.modelSetName);
info.put ("modelCount", Integer.$valueOf (m.mc));
info.put ("isTainted", Boolean.$valueOf (m.tainted != null));
info.put ("canSkipLoad", Boolean.$valueOf (m.canSkipLoad));
info.put ("modelSetHasVibrationVectors", Boolean.$valueOf (m.modelSetHasVibrationVectors ()));
if (m.modelSetProperties != null) {
info.put ("modelSetProperties", m.modelSetProperties);
}info.put ("modelCountSelected", Integer.$valueOf (JU.BSUtil.cardinalityOf (bsModels)));
info.put ("modelsSelected", bsModels);
var vModels =  new JU.Lst ();
m.getMolecules ();
for (var i = bsModels.nextSetBit (0); i >= 0; i = bsModels.nextSetBit (i + 1)) {
var model =  new java.util.Hashtable ();
model.put ("_ipt", Integer.$valueOf (i));
model.put ("num", Integer.$valueOf (m.getModelNumber (i)));
model.put ("file_model", m.getModelNumberDotted (i));
model.put ("name", m.getModelName (i));
var s = m.getModelTitle (i);
if (s != null) model.put ("title", s);
s = m.getModelFileName (i);
if (s != null) model.put ("file", s);
s = m.getInfo (i, "modelID");
if (s != null) model.put ("id", s);
model.put ("vibrationVectors", Boolean.$valueOf (this.vwr.modelHasVibrationVectors (i)));
var mi = m.am[i];
model.put ("atomCount", Integer.$valueOf (mi.ac));
model.put ("bondCount", Integer.$valueOf (mi.getBondCount ()));
model.put ("groupCount", Integer.$valueOf (mi.getGroupCount ()));
model.put ("moleculeCount", Integer.$valueOf (mi.moleculeCount));
model.put ("polymerCount", Integer.$valueOf (mi.getBioPolymerCount ()));
model.put ("chainCount", Integer.$valueOf (m.getChainCountInModelWater (i, true)));
if (mi.properties != null) {
model.put ("modelProperties", mi.properties);
}var energy = m.getInfo (i, "Energy");
if (energy != null) {
model.put ("energy", energy);
}model.put ("atomCount", Integer.$valueOf (mi.ac));
vModels.addLast (model);
}
info.put ("models", vModels);
return info;
}, "~O");
Clazz.overrideMethod (c$, "getLigandInfo", 
function (atomExpression) {
var bsAtoms = this.vwr.getAtomBitSet (atomExpression);
var bsSolvent = this.vwr.getAtomBitSet ("solvent");
var info =  new java.util.Hashtable ();
var ligands =  new JU.Lst ();
info.put ("ligands", ligands);
var ms = this.vwr.ms;
var bsExclude = JU.BSUtil.copyInvert (bsAtoms, ms.ac);
bsExclude.or (bsSolvent);
var atoms = ms.at;
for (var i = bsAtoms.nextSetBit (0); i >= 0; i = bsAtoms.nextSetBit (i + 1)) if (atoms[i].isProtein () || atoms[i].isNucleic ()) bsExclude.set (i);

var bsModelAtoms =  new Array (ms.mc);
for (var i = ms.mc; --i >= 0; ) {
bsModelAtoms[i] = this.vwr.getModelUndeletedAtomsBitSet (i);
bsModelAtoms[i].andNot (bsExclude);
}
var molList = JU.JmolMolecule.getMolecules (atoms, bsModelAtoms, null, bsExclude);
for (var i = 0; i < molList.length; i++) {
var bs = molList[i].atomList;
var ligand =  new java.util.Hashtable ();
ligands.addLast (ligand);
ligand.put ("atoms", JU.Escape.eBS (bs));
var names = "";
var sep = "";
var lastGroup = null;
var iChainLast = 0;
var sChainLast = null;
var reslist = "";
var model = "";
var resnolast = 2147483647;
var resnofirst = 2147483647;
for (var j = bs.nextSetBit (0); j >= 0; j = bs.nextSetBit (j + 1)) {
var atom = atoms[j];
if (lastGroup === atom.group) continue;
lastGroup = atom.group;
var resno = atom.getResno ();
var chain = atom.getChainID ();
if (resnolast != resno - 1) {
if (reslist.length != 0 && resnolast != resnofirst) reslist += "-" + resnolast;
chain = -1;
resnofirst = resno;
}model = "/" + ms.getModelNumberDotted (atom.mi);
if (iChainLast != 0 && chain != iChainLast) reslist += ":" + sChainLast + model;
if (chain == -1) reslist += " " + resno;
resnolast = resno;
iChainLast = atom.getChainID ();
sChainLast = atom.getChainIDStr ();
names += sep + atom.getGroup3 (false);
sep = "-";
}
reslist += (resnofirst == resnolast ? "" : "-" + resnolast) + (iChainLast == 0 ? "" : ":" + sChainLast) + model;
ligand.put ("groupNames", names);
ligand.put ("residueList", reslist.substring (1));
}
return info;
}, "~O");
Clazz.overrideMethod (c$, "getSymmetryInfo", 
function (bsAtoms, xyz, op, pt, pt2, id, type) {
var iModel = -1;
if (bsAtoms == null) {
iModel = this.vwr.am.cmi;
if (iModel < 0) return "";
bsAtoms = this.vwr.getModelUndeletedAtomsBitSet (iModel);
}var iAtom = bsAtoms.nextSetBit (0);
if (iAtom < 0) return "";
iModel = this.vwr.ms.at[iAtom].mi;
var uc = this.vwr.ms.am[iModel].biosymmetry;
if (uc == null) uc = this.vwr.ms.getUnitCell (iModel);
if (uc == null) return "";
return uc.getSymmetryInfo (this.vwr.ms, iModel, iAtom, uc, xyz, op, pt, pt2, id, type);
}, "JU.BS,~S,~N,JU.P3,JU.P3,~S,~N");
Clazz.overrideMethod (c$, "getModelExtract", 
function (bs, doTransform, isModelKit, type) {
var asV3000 = type.equalsIgnoreCase ("V3000");
var asSDF = type.equalsIgnoreCase ("SDF");
var asXYZVIB = type.equalsIgnoreCase ("XYZVIB");
var asJSON = type.equalsIgnoreCase ("JSON") || type.equalsIgnoreCase ("CD");
var mol =  new JU.SB ();
var ms = this.vwr.ms;
if (!asXYZVIB && !asJSON) {
mol.append (isModelKit ? "Jmol Model Kit" : this.vwr.getFullPathName (false).$replace ('\\', '/'));
var version = JV.Viewer.getJmolVersion ();
mol.append ("\n__Jmol-").append (version.substring (0, 2));
var cMM;
var cDD;
var cYYYY;
var cHH;
var cmm;
{
var c = new Date();
cMM = c.getMonth();
cDD = c.getDate();
cYYYY = c.getFullYear();
cHH = c.getHours();
cmm = c.getMinutes();
}JU.Txt.rightJustify (mol, "_00", "" + (1 + cMM));
JU.Txt.rightJustify (mol, "00", "" + cDD);
mol.append (("" + cYYYY).substring (2, 4));
JU.Txt.rightJustify (mol, "00", "" + cHH);
JU.Txt.rightJustify (mol, "00", "" + cmm);
mol.append ("3D 1   1.00000     0.00000     0");
mol.append ("\nJmol version ").append (JV.Viewer.getJmolVersion ()).append (" EXTRACT: ").append (JU.Escape.eBS (bs)).append ("\n");
}var bsAtoms = JU.BSUtil.copy (bs);
var atoms = ms.at;
for (var i = bs.nextSetBit (0); i >= 0; i = bs.nextSetBit (i + 1)) if (doTransform && atoms[i].isDeleted ()) bsAtoms.clear (i);

var bsBonds = JV.PropertyManager.getCovalentBondsForAtoms (ms.bo, ms.bondCount, bsAtoms);
if (!asXYZVIB && bsAtoms.cardinality () == 0) return "";
var isOK = true;
var q = (doTransform ? this.vwr.tm.getRotationQuaternion () : null);
if (asSDF) {
var header = mol.toString ();
mol =  new JU.SB ();
var bsModels = this.vwr.ms.getModelBS (bsAtoms, true);
for (var i = bsModels.nextSetBit (0); i >= 0; i = bsModels.nextSetBit (i + 1)) {
mol.append (header);
var bsTemp = JU.BSUtil.copy (bsAtoms);
bsTemp.and (ms.getModelAtomBitSetIncludingDeleted (i, false));
bsBonds = JV.PropertyManager.getCovalentBondsForAtoms (ms.bo, ms.bondCount, bsTemp);
if (!(isOK = this.addMolFile (mol, bsTemp, bsBonds, false, false, q))) break;
mol.append ("$$$$\n");
}
} else if (asXYZVIB) {
var tokens1 = JM.LabelToken.compile (this.vwr, "%-2e %10.5x %10.5y %10.5z %10.5vx %10.5vy %10.5vz\n", '\0', null);
var tokens2 = JM.LabelToken.compile (this.vwr, "%-2e %10.5x %10.5y %10.5z\n", '\0', null);
var bsModels = this.vwr.ms.getModelBS (bsAtoms, true);
var ptTemp =  new JU.P3 ();
for (var i = bsModels.nextSetBit (0); i >= 0; i = bsModels.nextSetBit (i + 1)) {
var bsTemp = JU.BSUtil.copy (bsAtoms);
bsTemp.and (ms.getModelAtomBitSetIncludingDeleted (i, false));
if (bsTemp.cardinality () == 0) continue;
mol.appendI (bsTemp.cardinality ()).appendC ('\n');
var props = ms.am[i].properties;
mol.append ("Model[" + (i + 1) + "]: ");
if (ms.frameTitles[i] != null && ms.frameTitles[i].length > 0) {
mol.append (ms.frameTitles[i].$replace ('\n', ' '));
} else if (props == null) {
mol.append ("Jmol " + JV.Viewer.getJmolVersion ());
} else {
var sb =  new JU.SB ();
var e = props.propertyNames ();
var path = null;
while (e.hasMoreElements ()) {
var propertyName = e.nextElement ();
if (propertyName.equals (".PATH")) path = props.getProperty (propertyName);
 else sb.append (";").append (propertyName).append ("=").append (props.getProperty (propertyName));
}
if (path != null) sb.append (";PATH=").append (path);
path = sb.substring (sb.length () > 0 ? 1 : 0);
mol.append (path.$replace ('\n', ' '));
}mol.appendC ('\n');
for (var j = bsTemp.nextSetBit (0); j >= 0; j = bsTemp.nextSetBit (j + 1)) mol.append (JM.LabelToken.formatLabelAtomArray (this.vwr, atoms[j], (ms.getVibration (j, false) == null ? tokens2 : tokens1), '\0', null, ptTemp));

}
} else {
isOK = this.addMolFile (mol, bsAtoms, bsBonds, asV3000, asJSON, q);
}return (isOK ? mol.toString () : "ERROR: Too many atoms or bonds -- use V3000 format.");
}, "JU.BS,~B,~B,~S");
Clazz.defineMethod (c$, "addMolFile", 
 function (mol, bsAtoms, bsBonds, asV3000, asJSON, q) {
var nAtoms = bsAtoms.cardinality ();
var nBonds = bsBonds.cardinality ();
if (!asV3000 && !asJSON && (nAtoms > 999 || nBonds > 999)) return false;
var ms = this.vwr.ms;
var atomMap =  Clazz.newIntArray (ms.ac, 0);
var pTemp =  new JU.P3 ();
if (asV3000) {
mol.append ("  0  0  0  0  0  0            999 V3000");
} else if (asJSON) {
mol.append ("{\"mol\":{\"createdBy\":\"Jmol " + JV.Viewer.getJmolVersion () + "\",\"a\":[");
} else {
JU.Txt.rightJustify (mol, "   ", "" + nAtoms);
JU.Txt.rightJustify (mol, "   ", "" + nBonds);
mol.append ("  0  0  0  0              1 V2000");
}if (!asJSON) mol.append ("\n");
if (asV3000) {
mol.append ("M  V30 BEGIN CTAB\nM  V30 COUNTS ").appendI (nAtoms).append (" ").appendI (nBonds).append (" 0 0 0\n").append ("M  V30 BEGIN ATOM\n");
}var ptTemp =  new JU.P3 ();
for (var i = bsAtoms.nextSetBit (0), n = 0; i >= 0; i = bsAtoms.nextSetBit (i + 1)) this.getAtomRecordMOL (ms, mol, atomMap[i] = ++n, ms.at[i], q, pTemp, ptTemp, asV3000, asJSON);

if (asV3000) {
mol.append ("M  V30 END ATOM\nM  V30 BEGIN BOND\n");
} else if (asJSON) {
mol.append ("],\"b\":[");
}for (var i = bsBonds.nextSetBit (0), n = 0; i >= 0; i = bsBonds.nextSetBit (i + 1)) this.getBondRecordMOL (mol, ++n, ms.bo[i], atomMap, asV3000, asJSON);

if (asV3000) {
mol.append ("M  V30 END BOND\nM  V30 END CTAB\n");
}if (asJSON) mol.append ("]}}");
 else {
mol.append ("M  END\n");
}if (!asJSON && !asV3000) {
var pc = ms.getPartialCharges ();
if (pc != null) {
mol.append ("> <JMOL_PARTIAL_CHARGES>\n").appendI (nAtoms).appendC ('\n');
for (var i = bsAtoms.nextSetBit (0), n = 0; i >= 0; i = bsAtoms.nextSetBit (i + 1)) mol.appendI (++n).append (" ").appendF (pc[i]).appendC ('\n');

}}return true;
}, "JU.SB,JU.BS,JU.BS,~B,~B,JU.Quat");
c$.getCovalentBondsForAtoms = Clazz.defineMethod (c$, "getCovalentBondsForAtoms", 
 function (bonds, bondCount, bsAtoms) {
var bsBonds =  new JU.BS ();
for (var i = 0; i < bondCount; i++) {
var bond = bonds[i];
if (bsAtoms.get (bond.atom1.i) && bsAtoms.get (bond.atom2.i) && bond.isCovalent ()) bsBonds.set (i);
}
return bsBonds;
}, "~A,~N,JU.BS");
Clazz.defineMethod (c$, "getAtomRecordMOL", 
 function (ms, mol, n, a, q, pTemp, ptTemp, asV3000, asJSON) {
if (ms.am[a.mi].isTrajectory) a.setFractionalCoordPt (ptTemp, ms.trajectorySteps.get (a.mi)[a.i - ms.am[a.mi].firstAtomIndex], true);
 else pTemp.setT (a);
if (q != null) q.transformP2 (pTemp, pTemp);
var elemNo = a.getElementNumber ();
var sym = (a.isDeleted () ? "Xx" : JU.Elements.elementSymbolFromNumber (elemNo));
var iso = a.getIsotopeNumber ();
var charge = a.getFormalCharge ();
if (asV3000) {
mol.append ("M  V30 ").appendI (n).append (" ").append (sym).append (" ").appendF (pTemp.x).append (" ").appendF (pTemp.y).append (" ").appendF (pTemp.z).append (" 0");
if (charge != 0) mol.append (" CHG=").appendI (charge);
if (iso != 0) mol.append (" MASS=").appendI (iso);
mol.append ("\n");
} else if (asJSON) {
if (n != 1) mol.append (",");
mol.append ("{");
if (a.getElementNumber () != 6) mol.append ("\"l\":\"").append (a.getElementSymbol ()).append ("\",");
if (charge != 0) mol.append ("\"c\":").appendI (charge).append (",");
if (iso != 0 && iso != JU.Elements.getNaturalIsotope (elemNo)) mol.append ("\"m\":").appendI (iso).append (",");
mol.append ("\"x\":").appendF (a.x).append (",\"y\":").appendF (a.y).append (",\"z\":").appendF (a.z).append ("}");
} else {
mol.append (JU.Txt.sprintf ("%10.5p%10.5p%10.5p", "p", [pTemp]));
mol.append (" ").append (sym);
if (sym.length == 1) mol.append (" ");
if (iso > 0) iso -= JU.Elements.getNaturalIsotope (a.getElementNumber ());
mol.append (" ");
JU.Txt.rightJustify (mol, "  ", "" + iso);
JU.Txt.rightJustify (mol, "   ", "" + (charge == 0 ? 0 : 4 - charge));
mol.append ("  0  0  0  0\n");
}}, "JM.ModelSet,JU.SB,~N,JM.Atom,JU.Quat,JU.P3,JU.P3,~B,~B");
Clazz.defineMethod (c$, "getBondRecordMOL", 
 function (mol, n, b, atomMap, asV3000, asJSON) {
var a1 = atomMap[b.atom1.i];
var a2 = atomMap[b.atom2.i];
var order = b.getValence ();
if (order > 3) order = 1;
switch (b.order & -131073) {
case 515:
order = (asJSON ? -3 : 4);
break;
case 66:
order = (asJSON ? -3 : 5);
break;
case 513:
order = (asJSON ? 1 : 6);
break;
case 514:
order = (asJSON ? 2 : 7);
break;
case 33:
order = (asJSON ? -1 : 8);
break;
}
if (asV3000) {
mol.append ("M  V30 ").appendI (n).append (" ").appendI (order).append (" ").appendI (a1).append (" ").appendI (a2).appendC ('\n');
} else if (asJSON) {
if (n != 1) mol.append (",");
mol.append ("{\"b\":").appendI (a1 - 1).append (",\"e\":").appendI (a2 - 1);
if (order != 1) {
mol.append (",\"o\":");
if (order < 0) {
mol.appendF (-order / 2);
} else {
mol.appendI (order);
}}mol.append ("}");
} else {
JU.Txt.rightJustify (mol, "   ", "" + a1);
JU.Txt.rightJustify (mol, "   ", "" + a2);
mol.append ("  ").appendI (order).append ("  0  0  0\n");
}}, "JU.SB,~N,JM.Bond,~A,~B,~B");
Clazz.overrideMethod (c$, "getChimeInfo", 
function (tok, bs) {
switch (tok) {
case 1073741982:
break;
case 1073741864:
return this.getBasePairInfo (bs);
default:
return this.getChimeInfoA (this.vwr.ms.at, tok, bs);
}
var sb =  new JU.SB ();
this.vwr.ms.am[0].getChimeInfo (sb, 0);
return sb.appendC ('\n').toString ().substring (1);
}, "~N,JU.BS");
Clazz.defineMethod (c$, "getChimeInfoA", 
 function (atoms, tok, bs) {
var info =  new JU.SB ();
info.append ("\n");
var s = "";
var clast = null;
var glast = null;
var modelLast = -1;
var n = 0;
if (bs != null) for (var i = bs.nextSetBit (0); i >= 0; i = bs.nextSetBit (i + 1)) {
var a = atoms[i];
switch (tok) {
default:
return "";
case 1114638363:
s = a.getInfo ();
break;
case 1141899265:
s = "" + a.getAtomNumber ();
break;
case 1087373318:
s = a.getGroup3 (false);
break;
case 1087373316:
case 1073742120:
case 1087373320:
case 1087373319:
var id = a.getChainID ();
s = (id == 0 ? " " : a.getChainIDStr ());
if (id > 255) s = JU.PT.esc (s);
switch (tok) {
case 1073742120:
s = "[" + a.getGroup3 (false) + "]" + a.getSeqcodeString () + ":" + s;
break;
case 1087373320:
case 1087373319:
if (a.getModelIndex () != modelLast) {
info.appendC ('\n');
n = 0;
modelLast = a.getModelIndex ();
info.append ("Model " + a.getModelNumber ());
glast = null;
clast = null;
}if (a.getChain () !== clast) {
info.appendC ('\n');
n = 0;
clast = a.getChain ();
info.append ("Chain " + s + ":\n");
glast = null;
}var g = a.getGroup ();
if (g !== glast) {
glast = g;
if (tok == 1087373319) {
info.append (a.getGroup1 ('?'));
} else {
if ((n++) % 5 == 0 && n > 1) info.appendC ('\n');
JU.Txt.leftJustify (info, "          ", "[" + a.getGroup3 (false) + "]" + a.getResno () + " ");
}}continue;
}
break;
}
if (info.indexOf ("\n" + s + "\n") < 0) info.append (s).appendC ('\n');
}
if (tok == 1087373320) info.appendC ('\n');
return info.toString ().substring (1);
}, "~A,~N,JU.BS");
Clazz.overrideMethod (c$, "getModelFileInfo", 
function (frames) {
var ms = this.vwr.ms;
var sb =  new JU.SB ();
for (var i = 0; i < ms.mc; ++i) {
if (frames != null && !frames.get (i)) continue;
var s = "[\"" + ms.getModelNumberDotted (i) + "\"] = ";
sb.append ("\n\nfile").append (s).append (JU.PT.esc (ms.getModelFileName (i)));
var id = ms.getInfo (i, "modelID");
if (id != null) sb.append ("\nid").append (s).append (JU.PT.esc (id));
sb.append ("\ntitle").append (s).append (JU.PT.esc (ms.getModelTitle (i)));
sb.append ("\nname").append (s).append (JU.PT.esc (ms.getModelName (i)));
sb.append ("\ntype").append (s).append (JU.PT.esc (ms.getModelFileType (i)));
}
return sb.toString ();
}, "JU.BS");
Clazz.defineMethod (c$, "getAllAtomInfo", 
function (bs) {
var V =  new JU.Lst ();
var ptTemp =  new JU.P3 ();
for (var i = bs.nextSetBit (0); i >= 0; i = bs.nextSetBit (i + 1)) {
V.addLast (this.getAtomInfoLong (i, ptTemp));
}
return V;
}, "JU.BS");
Clazz.defineMethod (c$, "getAtomInfoLong", 
 function (i, ptTemp) {
var ms = this.vwr.ms;
var atom = ms.at[i];
var info =  new java.util.Hashtable ();
ms.getAtomIdentityInfo (i, info, ptTemp);
info.put ("element", ms.getElementName (i));
info.put ("elemno", Integer.$valueOf (ms.getElementNumber (i)));
info.put ("x", Float.$valueOf (atom.x));
info.put ("y", Float.$valueOf (atom.y));
info.put ("z", Float.$valueOf (atom.z));
info.put ("coord", JU.P3.newP (atom));
if (ms.vibrations != null && ms.vibrations[i] != null) ms.vibrations[i].getInfo (info);
info.put ("bondCount", Integer.$valueOf (atom.getCovalentBondCount ()));
info.put ("radius", Float.$valueOf ((atom.getRasMolRadius () / 120.0)));
info.put ("model", atom.getModelNumberForLabel ());
var shape = JM.Atom.atomPropertyString (this.vwr, atom, 1087373323);
if (shape != null) info.put ("shape", shape);
info.put ("visible", Boolean.$valueOf (atom.checkVisible ()));
info.put ("clickabilityFlags", Integer.$valueOf (atom.clickabilityFlags));
info.put ("visibilityFlags", Integer.$valueOf (atom.shapeVisibilityFlags));
info.put ("spacefill", Float.$valueOf (atom.getRadius ()));
var strColor = JU.Escape.escapeColor (this.vwr.getColorArgbOrGray (atom.colixAtom));
if (strColor != null) info.put ("color", strColor);
info.put ("colix", Integer.$valueOf (atom.colixAtom));
var isTranslucent = atom.isTranslucent ();
if (isTranslucent) info.put ("translucent", Boolean.$valueOf (isTranslucent));
info.put ("formalCharge", Integer.$valueOf (atom.getFormalCharge ()));
info.put ("partialCharge", Float.$valueOf (atom.getPartialCharge ()));
var d = atom.getSurfaceDistance100 () / 100;
if (d >= 0) info.put ("surfaceDistance", Float.$valueOf (d));
if (ms.am[atom.mi].isBioModel) {
info.put ("resname", atom.getGroup3 (false));
var insCode = atom.getInsertionCode ();
var seqNum = atom.getResno ();
if (seqNum > 0) info.put ("resno", Integer.$valueOf (seqNum));
if (insCode.charCodeAt (0) != 0) info.put ("insertionCode", "" + insCode);
info.put ("name", ms.getAtomName (i));
info.put ("chain", atom.getChainIDStr ());
info.put ("atomID", Integer.$valueOf (atom.atomID));
info.put ("groupID", Integer.$valueOf (atom.getGroupID ()));
if (atom.altloc != '\0') info.put ("altLocation", "" + atom.altloc);
info.put ("structure", Integer.$valueOf (atom.getProteinStructureType ().getId ()));
info.put ("polymerLength", Integer.$valueOf (atom.getPolymerLength ()));
info.put ("occupancy", Integer.$valueOf (atom.getOccupancy100 ()));
var temp = atom.getBfactor100 ();
info.put ("temp", Integer.$valueOf (Clazz.doubleToInt (temp / 100)));
}return info;
}, "~N,JU.P3");
Clazz.defineMethod (c$, "getAllBondInfo", 
function (bsOrArray) {
var v =  new JU.Lst ();
var ms = this.vwr.ms;
var bondCount = ms.bondCount;
var bonds = ms.bo;
var bs1;
if (Clazz.instanceOf (bsOrArray, String)) {
bsOrArray = this.vwr.getAtomBitSet (bsOrArray);
}var ptTemp =  new JU.P3 ();
if (Clazz.instanceOf (bsOrArray, Array)) {
bs1 = (bsOrArray)[0];
var bs2 = (bsOrArray)[1];
for (var i = 0; i < bondCount; i++) {
var ia = bonds[i].atom1.i;
var ib = bonds[i].atom2.i;
if (bs1.get (ia) && bs2.get (ib) || bs2.get (ia) && bs1.get (ib)) v.addLast (this.getBondInfo (i, ptTemp));
}
} else if (Clazz.instanceOf (bsOrArray, JM.BondSet)) {
bs1 = bsOrArray;
for (var i = bs1.nextSetBit (0); i >= 0 && i < bondCount; i = bs1.nextSetBit (i + 1)) v.addLast (this.getBondInfo (i, ptTemp));

} else if (Clazz.instanceOf (bsOrArray, JU.BS)) {
bs1 = bsOrArray;
var thisAtom = (bs1.cardinality () == 1 ? bs1.nextSetBit (0) : -1);
for (var i = 0; i < bondCount; i++) {
if (thisAtom >= 0 ? (bonds[i].atom1.i == thisAtom || bonds[i].atom2.i == thisAtom) : bs1.get (bonds[i].atom1.i) && bs1.get (bonds[i].atom2.i)) v.addLast (this.getBondInfo (i, ptTemp));
}
}return v;
}, "~O");
Clazz.defineMethod (c$, "getBondInfo", 
 function (i, ptTemp) {
var bond = this.vwr.ms.bo[i];
var atom1 = bond.atom1;
var atom2 = bond.atom2;
var info =  new java.util.Hashtable ();
info.put ("_bpt", Integer.$valueOf (i));
var infoA =  new java.util.Hashtable ();
this.vwr.ms.getAtomIdentityInfo (atom1.i, infoA, ptTemp);
var infoB =  new java.util.Hashtable ();
this.vwr.ms.getAtomIdentityInfo (atom2.i, infoB, ptTemp);
info.put ("atom1", infoA);
info.put ("atom2", infoB);
info.put ("order", Float.$valueOf (JU.PT.fVal (JU.Edge.getBondOrderNumberFromOrder (bond.order))));
info.put ("type", JU.Edge.getBondOrderNameFromOrder (bond.order));
info.put ("radius", Float.$valueOf ((bond.mad / 2000.)));
info.put ("length_Ang", Float.$valueOf (atom1.distance (atom2)));
info.put ("visible", Boolean.$valueOf (bond.shapeVisibilityFlags != 0));
var strColor = JU.Escape.escapeColor (this.vwr.getColorArgbOrGray (bond.colix));
if (strColor != null) info.put ("color", strColor);
info.put ("colix", Integer.$valueOf (bond.colix));
if (JU.C.isColixTranslucent (bond.colix)) info.put ("translucent", Boolean.TRUE);
return info;
}, "~N,JU.P3");
Clazz.defineMethod (c$, "getAllChainInfo", 
function (bs) {
var finalInfo =  new java.util.Hashtable ();
var modelVector =  new JU.Lst ();
var modelCount = this.vwr.ms.mc;
for (var i = 0; i < modelCount; ++i) {
var modelInfo =  new java.util.Hashtable ();
var info = this.getChainInfo (i, bs);
if (info.size () > 0) {
modelInfo.put ("modelIndex", Integer.$valueOf (i));
modelInfo.put ("chains", info);
modelVector.addLast (modelInfo);
}}
finalInfo.put ("models", modelVector);
return finalInfo;
}, "JU.BS");
Clazz.defineMethod (c$, "getChainInfo", 
 function (modelIndex, bs) {
var model = this.vwr.ms.am[modelIndex];
var nChains = model.getChainCount (true);
var infoChains =  new JU.Lst ();
var ptTemp =  new JU.P3 ();
for (var i = 0; i < nChains; i++) {
var chain = model.getChainAt (i);
var infoChain =  new JU.Lst ();
var nGroups = chain.getGroupCount ();
var arrayName =  new java.util.Hashtable ();
for (var igroup = 0; igroup < nGroups; igroup++) {
var group = chain.getGroup (igroup);
if (bs.get (group.firstAtomIndex)) infoChain.addLast (group.getGroupInfo (igroup, ptTemp));
}
if (!infoChain.isEmpty ()) {
arrayName.put ("residues", infoChain);
infoChains.addLast (arrayName);
}}
return infoChains;
}, "~N,JU.BS");
Clazz.defineMethod (c$, "getAllPolymerInfo", 
 function (bs) {
var finalInfo =  new java.util.Hashtable ();
var modelVector =  new JU.Lst ();
var modelCount = this.vwr.ms.mc;
var models = this.vwr.ms.am;
for (var i = 0; i < modelCount; ++i) if (models[i].isBioModel) models[i].getAllPolymerInfo (bs, finalInfo, modelVector);

finalInfo.put ("models", modelVector);
return finalInfo;
}, "JU.BS");
Clazz.defineMethod (c$, "getBasePairInfo", 
 function (bs) {
var info =  new JU.SB ();
var vHBonds =  new JU.Lst ();
this.vwr.ms.calcRasmolHydrogenBonds (bs, bs, vHBonds, true, 1, false, null);
for (var i = vHBonds.size (); --i >= 0; ) {
var b = vHBonds.get (i);
JV.PropertyManager.getAtomResidueInfo (info, b.atom1);
info.append (" - ");
JV.PropertyManager.getAtomResidueInfo (info, b.atom2);
info.append ("\n");
}
return info.toString ();
}, "JU.BS");
c$.getAtomResidueInfo = Clazz.defineMethod (c$, "getAtomResidueInfo", 
 function (info, atom) {
info.append ("[").append (atom.getGroup3 (false)).append ("]").append (atom.getSeqcodeString ()).append (":");
var id = atom.getChainID ();
info.append (id == 0 ? " " : atom.getChainIDStr ());
}, "JU.SB,JM.Atom");
Clazz.defineMethod (c$, "getAppletInfo", 
 function () {
var info =  new java.util.Hashtable ();
info.put ("htmlName", this.vwr.htmlName);
info.put ("syncId", this.vwr.syncId);
info.put ("fullName", this.vwr.fullName);
info.put ("codeBase", "" + JV.Viewer.appletCodeBase);
if (this.vwr.isApplet ()) {
info.put ("documentBase", JV.Viewer.appletDocumentBase);
info.put ("registry", this.vwr.sm.getRegistryInfo ());
}info.put ("version", JV.JC.version);
info.put ("date", JV.JC.date);
info.put ("javaVendor", JV.Viewer.strJavaVendor);
info.put ("javaVersion", JV.Viewer.strJavaVersion + (!this.vwr.isJS ? "" : this.vwr.isWebGL ? "(WebGL)" : "(HTML5)"));
info.put ("operatingSystem", JV.Viewer.strOSName);
return info;
});
Clazz.defineMethod (c$, "getAnimationInfo", 
 function () {
var am = this.vwr.am;
var info =  new java.util.Hashtable ();
info.put ("firstModelIndex", Integer.$valueOf (am.firstFrameIndex));
info.put ("lastModelIndex", Integer.$valueOf (am.lastFrameIndex));
info.put ("animationDirection", Integer.$valueOf (am.animationDirection));
info.put ("currentDirection", Integer.$valueOf (am.currentDirection));
info.put ("displayModelIndex", Integer.$valueOf (am.cmi));
if (am.animationFrames != null) {
info.put ("isMovie", Boolean.TRUE);
info.put ("frames", JU.Escape.eAI (am.animationFrames));
info.put ("currentAnimationFrame", Integer.$valueOf (am.caf));
}info.put ("displayModelNumber", this.vwr.getModelNumberDotted (am.cmi));
info.put ("displayModelName", (am.cmi >= 0 ? this.vwr.getModelName (am.cmi) : ""));
info.put ("animationFps", Integer.$valueOf (am.animationFps));
info.put ("animationReplayMode", am.animationReplayMode.name ());
info.put ("firstFrameDelay", Float.$valueOf (am.firstFrameDelay));
info.put ("lastFrameDelay", Float.$valueOf (am.lastFrameDelay));
info.put ("animationOn", Boolean.$valueOf (am.animationOn));
info.put ("animationPaused", Boolean.$valueOf (am.animationPaused));
return info;
});
Clazz.defineMethod (c$, "getBoundBoxInfo", 
 function () {
var pts = this.vwr.ms.getBoxInfo (null, 1).getBoundBoxPoints (true);
var info =  new java.util.Hashtable ();
info.put ("center", JU.P3.newP (pts[0]));
info.put ("vector", JU.V3.newV (pts[1]));
info.put ("corner0", JU.P3.newP (pts[2]));
info.put ("corner1", JU.P3.newP (pts[3]));
return info;
});
Clazz.defineMethod (c$, "getShapeInfo", 
 function () {
var info =  new java.util.Hashtable ();
var commands =  new JU.SB ();
var shapes = this.vwr.shm.shapes;
if (shapes != null) for (var i = 0; i < 36; ++i) {
var shape = shapes[i];
if (shape != null) {
var shapeType = JV.JC.shapeClassBases[i];
var shapeDetail = shape.getShapeDetail ();
if (shapeDetail != null) info.put (shapeType, shapeDetail);
}}
if (commands.length () > 0) info.put ("shapeCommands", commands.toString ());
return info;
});
Clazz.defineMethod (c$, "getAuxiliaryInfo", 
 function (atomExpression) {
return this.vwr.ms.getAuxiliaryInfo (this.vwr.ms.getModelBS (this.vwr.getAtomBitSet (atomExpression), false));
}, "~O");
Clazz.defineMethod (c$, "getAnnotationInfo", 
 function (atomExpression, type) {
var bsAtoms = this.vwr.getAtomBitSet (atomExpression);
var iModel = this.vwr.ms.getModelBS (bsAtoms, false).nextSetBit (0);
if (iModel < 0) return null;
var modelinfo = this.vwr.ms.getModelAuxiliaryInfo (iModel);
var objAnn = modelinfo.get (type == 1073741925 ? "domains" : "validation");
if (objAnn == null || objAnn.tok != 6) return null;
this.vwr.getAnnotationParser ().initializeAnnotation (objAnn, type, iModel);
return objAnn.getMap ().get ("_list");
}, "~O,~N");
Clazz.defineMethod (c$, "getMeasurementInfo", 
 function () {
return this.vwr.getShapeProperty (6, "info");
});
Clazz.defineMethod (c$, "getMouseInfo", 
 function () {
if (!this.vwr.haveDisplay) return null;
var info =  new java.util.Hashtable ();
var list =  new JU.Lst ();
var am = this.vwr.actionManager;
for (var obj, $obj = am.b.getBindings ().values ().iterator (); $obj.hasNext () && ((obj = $obj.next ()) || true);) {
if (Clazz.instanceOf (obj, Boolean)) continue;
if (JU.PT.isAI (obj)) {
var binding = obj;
obj = [JV.binding.Binding.getMouseActionName (binding[0], false), JV.ActionManager.getActionName (binding[1])];
}list.addLast (obj);
}
info.put ("bindings", list);
info.put ("bindingName", am.b.name);
info.put ("actionNames", JV.ActionManager.actionNames);
info.put ("actionInfo", JV.ActionManager.actionInfo);
info.put ("bindingInfo", JU.PT.split (am.getBindingInfo (null), "\n"));
return info;
});
Clazz.overrideMethod (c$, "getPdbAtomData", 
function (bs, out) {
if (this.vwr.ms.ac == 0 || bs.nextSetBit (0) < 0) return "";
if (out == null) out = this.vwr.getOutputChannel (null, null);
var atoms = this.vwr.ms.at;
var models = this.vwr.ms.am;
var iModel = atoms[bs.nextSetBit (0)].mi;
var iModelLast = -1;
var isPQR = "PQR".equals (out.getType ());
var occTemp = "%6.2Q%6.2b          ";
if (isPQR) {
occTemp = "%8.4P%7.4V       ";
var charge = 0;
for (var i = bs.nextSetBit (0); i >= 0; i = bs.nextSetBit (i + 1)) charge += atoms[i].getPartialCharge ();

out.append ("REMARK   1 PQR file generated by Jmol " + JV.Viewer.getJmolVersion ()).append ("\nREMARK   1 " + "created " + ( new java.util.Date ())).append ("\nREMARK   1 Forcefield Used: unknown\nREMARK   1").append ("\nREMARK   5").append ("\nREMARK   6 Total charge on this protein: " + charge + " e\nREMARK   6\n");
}var lastAtomIndex = bs.length () - 1;
var showModels = (iModel != atoms[lastAtomIndex].mi);
var sbCONECT = (showModels ? null :  new JU.SB ());
var isMultipleBondPDB = models[iModel].isPdbWithMultipleBonds;
var tokens;
var ptTemp =  new JU.P3 ();
for (var i = bs.nextSetBit (0); i >= 0; i = bs.nextSetBit (i + 1)) {
var a = atoms[i];
if (showModels && a.mi != iModelLast) {
if (iModelLast != -1) out.append ("ENDMDL\n");
iModelLast = a.mi;
out.append ("MODEL     " + (iModelLast + 1) + "\n");
}var sa = a.getAtomName ();
var leftJustify = (a.getElementSymbol ().length == 2 || sa.length >= 4 || Character.isDigit (sa.charAt (0)));
var isBiomodel = models[a.mi].isBioModel;
var isHetero = a.isHetero ();
if (!isBiomodel) tokens = (leftJustify ? JM.LabelToken.compile (this.vwr, "HETATM%5.-5i %-4.4a%1AUNK %1c   1%1E   %8.3x%8.3y%8.3z" + occTemp, '\0', null) : JM.LabelToken.compile (this.vwr, "HETATM%5.-5i  %-3.3a%1AUNK %1c   1%1E   %8.3x%8.3y%8.3z" + occTemp, '\0', null));
 else if (isHetero) tokens = (leftJustify ? JM.LabelToken.compile (this.vwr, "HETATM%5.-5i %-4.4a%1A%3.-3n %1c%4.-4R%1E   %8.3x%8.3y%8.3z" + occTemp, '\0', null) : JM.LabelToken.compile (this.vwr, "HETATM%5.-5i  %-3.3a%1A%3.-3n %1c%4.-4R%1E   %8.3x%8.3y%8.3z" + occTemp, '\0', null));
 else tokens = (leftJustify ? JM.LabelToken.compile (this.vwr, "ATOM  %5.-5i %-4.4a%1A%3.-3n %1c%4.-4R%1E   %8.3x%8.3y%8.3z" + occTemp, '\0', null) : JM.LabelToken.compile (this.vwr, "ATOM  %5.-5i  %-3.3a%1A%3.-3n %1c%4.-4R%1E   %8.3x%8.3y%8.3z" + occTemp, '\0', null));
var XX = a.getElementSymbolIso (false).toUpperCase ();
out.append (JM.LabelToken.formatLabelAtomArray (this.vwr, a, tokens, '\0', null, ptTemp)).append (XX.length == 1 ? " " + XX : XX.substring (0, 2)).append ("  \n");
if (!showModels && (!isBiomodel || isHetero || isMultipleBondPDB)) {
var bonds = a.getBonds ();
if (bonds != null) for (var j = 0; j < bonds.length; j++) {
var iThis = a.getAtomNumber ();
var a2 = bonds[j].getOtherAtom (a);
if (!bs.get (a2.i)) continue;
var n = bonds[j].getCovalentOrder ();
if (n == 1 && isMultipleBondPDB && !isHetero) continue;
var iOther = a2.getAtomNumber ();
switch (n) {
case 2:
case 3:
if (iOther < iThis) continue;
case 1:
sbCONECT.append ("CONECT").append (JU.Txt.formatStringI ("%5i", "i", iThis));
for (var k = 0; k < n; k++) sbCONECT.append (JU.Txt.formatStringI ("%5i", "i", iOther));

sbCONECT.appendC ('\n');
break;
}
}
}}
if (showModels) out.append ("ENDMDL\n");
 else out.append (sbCONECT.toString ());
return out.toString ();
}, "JU.BS,JU.OC");
Clazz.overrideMethod (c$, "getPdbData", 
function (modelIndex, type, bsSelected, parameters, out, addStructure) {
if (this.vwr.ms.isJmolDataFrameForModel (modelIndex)) modelIndex = this.vwr.ms.getJmolDataSourceFrame (modelIndex);
if (modelIndex < 0) return "";
var model = this.vwr.ms.am[modelIndex];
var isPDB = model.isBioModel;
if (parameters == null && !isPDB) return null;
if (out == null) out = this.vwr.getOutputChannel (null, null);
var pdbCONECT =  new JU.SB ();
var isDraw = (type.indexOf ("draw") >= 0);
var bsAtoms = null;
var bsWritten =  new JU.BS ();
var ctype = '\u0000';
var tokens = this.vwr.ms.getLabeler ().compile (this.vwr, "ATOM  %-6i%4a%1A%3n %1c%4R%1E   ", '\0', null);
if (parameters == null) {
ctype = (type.length > 11 && type.indexOf ("quaternion ") >= 0 ? type.charAt (11) : 'R');
model.getPdbData (this.vwr, type, ctype, isDraw, bsSelected, out, tokens, pdbCONECT, bsWritten);
bsAtoms = this.vwr.getModelUndeletedAtomsBitSet (modelIndex);
} else {
bsAtoms = parameters[0];
var dataX = parameters[1];
var dataY = parameters[2];
var dataZ = parameters[3];
var haveZ = (dataZ != null);
var minXYZ = parameters[4];
var maxXYZ = parameters[5];
var factors = parameters[6];
var center = parameters[7];
out.append ("REMARK   6 Jmol PDB-encoded data: ").append (type).append (";\n");
out.append ("REMARK   6 Jmol data").append (" min = ").append (JU.Escape.eP (minXYZ)).append (" max = ").append (JU.Escape.eP (maxXYZ)).append (" unScaledXyz = xyz * ").append (JU.Escape.eP (factors)).append (" + ").append (JU.Escape.eP (center)).append (";\n");
var strExtra = "";
var atomLast = null;
var atoms = this.vwr.ms.at;
var ptTemp =  new JU.P3 ();
for (var i = bsAtoms.nextSetBit (0), n = 0; i >= 0; i = bsAtoms.nextSetBit (i + 1), n++) {
var x = dataX[n];
var y = dataY[n];
var z = (haveZ ? dataZ[n] : 0);
if (Float.isNaN (x) || Float.isNaN (y) || Float.isNaN (z)) continue;
var a = atoms[i];
out.append (JM.LabelToken.formatLabelAtomArray (this.vwr, a, tokens, '\0', null, ptTemp));
if (isPDB) bsWritten.set (i);
out.append (JU.Txt.sprintf ("%-8.2f%-8.2f%-10.2f    %6.3f          %2s    %s\n", "ssF", [a.getElementSymbolIso (false).toUpperCase (), strExtra, [x, y, z, 0]]));
if (atomLast != null && atomLast.getPolymerIndexInModel () == a.getPolymerIndexInModel ()) pdbCONECT.append ("CONECT").append (JU.Txt.formatStringI ("%5i", "i", atomLast.getAtomNumber ())).append (JU.Txt.formatStringI ("%5i", "i", a.getAtomNumber ())).appendC ('\n');
atomLast = a;
}
}out.append (pdbCONECT.toString ());
if (isDraw) return out.toString ();
bsSelected.and (bsAtoms);
if (isPDB && addStructure) out.append ("\n\n" + this.vwr.ms.getProteinStructureState (bsWritten, false, ctype == 'R', 1));
return out.toString ();
}, "~N,~S,JU.BS,~A,JU.OC,~B");
Clazz.overrideMethod (c$, "getModelCml", 
function (bs, atomsMax, addBonds) {
var sb =  new JU.SB ();
var nAtoms = JU.BSUtil.cardinalityOf (bs);
if (nAtoms == 0) return "";
J.api.Interface.getInterface ("JU.XmlUtil");
JU.XmlUtil.openTag (sb, "molecule");
JU.XmlUtil.openTag (sb, "atomArray");
var bsAtoms =  new JU.BS ();
var atoms = this.vwr.ms.at;
for (var i = bs.nextSetBit (0); i >= 0; i = bs.nextSetBit (i + 1)) {
if (--atomsMax < 0) break;
var atom = atoms[i];
var name = atom.getAtomName ();
JU.PT.rep (name, "\"", "''");
bsAtoms.set (atom.i);
JU.XmlUtil.appendTag (sb, "atom/", ["id", "a" + (atom.i + 1), "title", atom.getAtomName (), "elementType", atom.getElementSymbol (), "x3", "" + atom.x, "y3", "" + atom.y, "z3", "" + atom.z]);
}
JU.XmlUtil.closeTag (sb, "atomArray");
if (addBonds) {
JU.XmlUtil.openTag (sb, "bondArray");
var bondCount = this.vwr.getBondCount ();
var bonds = this.vwr.ms.bo;
for (var i = 0; i < bondCount; i++) {
var bond = bonds[i];
var a1 = bond.atom1;
var a2 = bond.atom2;
if (!bsAtoms.get (a1.i) || !bsAtoms.get (a2.i)) continue;
var order = JU.Edge.getCmlBondOrder (bond.order);
if (order == null) continue;
JU.XmlUtil.appendTag (sb, "bond/", ["atomRefs2", "a" + (bond.atom1.i + 1) + " a" + (bond.atom2.i + 1), "order", order]);
}
JU.XmlUtil.closeTag (sb, "bondArray");
}JU.XmlUtil.closeTag (sb, "molecule");
return sb.toString ();
}, "JU.BS,~N,~B");
Clazz.defineStatics (c$,
"atomExpression", "<atom selection>");
c$.propertyTypes = c$.prototype.propertyTypes = ["appletInfo", "", "", "fileName", "", "", "fileHeader", "", "", "fileContents", "<pathname>", "", "fileContents", "", "", "animationInfo", "", "", "modelInfo", "<atom selection>", "{*}", "ligandInfo", "<atom selection>", "{*}", "shapeInfo", "", "", "measurementInfo", "", "", "centerInfo", "", "", "orientationInfo", "", "", "transformInfo", "", "", "atomList", "<atom selection>", "(visible)", "atomInfo", "<atom selection>", "(visible)", "bondInfo", "<atom selection>", "(visible)", "chainInfo", "<atom selection>", "(visible)", "polymerInfo", "<atom selection>", "(visible)", "moleculeInfo", "<atom selection>", "(visible)", "stateInfo", "<state type>", "all", "extractModel", "<atom selection>", "(visible)", "jmolStatus", "statusNameList", "", "jmolViewer", "", "", "messageQueue", "", "", "auxiliaryInfo", "<atom selection>", "{*}", "boundBoxInfo", "", "", "dataInfo", "<data type>", "types", "image", "<width=www,height=hhh>", "", "evaluate", "<expression>", "", "menu", "<type>", "current", "minimizationInfo", "", "", "pointGroupInfo", "<atom selection>", "(visible)", "fileInfo", "<type>", "", "errorMessage", "", "", "mouseInfo", "", "", "isosurfaceInfo", "", "", "isosurfaceData", "", "", "consoleText", "", "", "JSpecView", "<key>", "", "scriptQueueInfo", "", "", "nmrInfo", "<elementSymbol> or 'all' or 'shifts'", "all", "variableInfo", "<name>", "all", "domainInfo", "<atom selection>", "{visible}", "validationInfo", "<atom selection>", "{visible}"];
Clazz.defineStatics (c$,
"PROP_APPLET_INFO", 0,
"PROP_FILENAME", 1,
"PROP_FILEHEADER", 2,
"PROP_FILECONTENTS_PATH", 3,
"PROP_FILECONTENTS", 4,
"PROP_ANIMATION_INFO", 5,
"PROP_MODEL_INFO", 6,
"PROP_LIGAND_INFO", 7,
"PROP_SHAPE_INFO", 8,
"PROP_MEASUREMENT_INFO", 9,
"PROP_CENTER_INFO", 10,
"PROP_ORIENTATION_INFO", 11,
"PROP_TRANSFORM_INFO", 12,
"PROP_ATOM_LIST", 13,
"PROP_ATOM_INFO", 14,
"PROP_BOND_INFO", 15,
"PROP_CHAIN_INFO", 16,
"PROP_POLYMER_INFO", 17,
"PROP_MOLECULE_INFO", 18,
"PROP_STATE_INFO", 19,
"PROP_EXTRACT_MODEL", 20,
"PROP_JMOL_STATUS", 21,
"PROP_JMOL_VIEWER", 22,
"PROP_MESSAGE_QUEUE", 23,
"PROP_AUXILIARY_INFO", 24,
"PROP_BOUNDBOX_INFO", 25,
"PROP_DATA_INFO", 26,
"PROP_IMAGE", 27,
"PROP_EVALUATE", 28,
"PROP_MENU", 29,
"PROP_MINIMIZATION_INFO", 30,
"PROP_POINTGROUP_INFO", 31,
"PROP_FILE_INFO", 32,
"PROP_ERROR_MESSAGE", 33,
"PROP_MOUSE_INFO", 34,
"PROP_ISOSURFACE_INFO", 35,
"PROP_ISOSURFACE_DATA", 36,
"PROP_CONSOLE_TEXT", 37,
"PROP_JSPECVIEW", 38,
"PROP_SCRIPT_QUEUE_INFO", 39,
"PROP_NMR_INFO", 40,
"PROP_VAR_INFO", 41,
"PROP_DOM_INFO", 42,
"PROP_VAL_INFO", 43,
"PROP_COUNT", 44,
"readableTypes", ["", "stateinfo", "extractmodel", "filecontents", "fileheader", "image", "menu", "minimizationInfo"]);
});
