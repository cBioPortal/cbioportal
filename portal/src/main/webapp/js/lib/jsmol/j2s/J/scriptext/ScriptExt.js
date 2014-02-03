Clazz.declarePackage ("J.scriptext");
Clazz.load (["J.script.JmolScriptExtension"], "J.scriptext.ScriptExt", ["java.lang.Boolean", "$.Float", "$.Short", "J.atomdata.RadiusData", "J.constant.EnumVdw", "J.modelset.Atom", "J.script.SV", "$.ScriptEvaluator", "$.T", "J.util.ArrayUtil", "$.BS", "$.BSUtil", "$.BoxInfo", "$.C", "$.Escape", "$.JmolList", "$.Logger", "$.MeshSurface", "$.P3", "$.Parser", "$.Quaternion", "$.SB", "$.TextFormat", "$.V3", "J.viewer.JC"], function () {
c$ = Clazz.decorateAsClass (function () {
this.viewer = null;
this.eval = null;
this.sm = null;
this.chk = false;
this.fullCommand = null;
this.thisCommand = null;
this.st = null;
Clazz.instantialize (this, arguments);
}, J.scriptext, "ScriptExt", null, J.script.JmolScriptExtension);
Clazz.makeConstructor (c$, 
function () {
});
Clazz.overrideMethod (c$, "init", 
function (se) {
this.eval = se;
this.viewer = this.eval.viewer;
this.sm = this.eval.sm;
return this;
}, "~O");
Clazz.overrideMethod (c$, "dispatch", 
function (iShape, b, st) {
this.chk = this.eval.chk;
this.fullCommand = this.eval.fullCommand;
this.thisCommand = this.eval.thisCommand;
this.st = st;
switch (iShape) {
case 23:
return this.cgo ();
case 25:
return this.contact ();
case 17:
return this.dipole ();
case 22:
return this.draw ();
case 24:
case 29:
case 28:
return this.isosurface (iShape);
case 26:
return this.lcaoCartoon ();
case 27:
return this.mo (b);
case 21:
return this.polyhedra ();
case 4:
return this.struts ();
}
return false;
}, "~N,~B,~A");
$_M(c$, "atomExpressionAt", 
($fz = function (i) {
return this.eval.atomExpressionAt (i);
}, $fz.isPrivate = true, $fz), "~N");
$_M(c$, "error", 
($fz = function (err) {
this.eval.error (err);
}, $fz.isPrivate = true, $fz), "~N");
$_M(c$, "invArg", 
($fz = function () {
this.error (22);
}, $fz.isPrivate = true, $fz));
$_M(c$, "invPO", 
($fz = function () {
this.error (23);
}, $fz.isPrivate = true, $fz));
$_M(c$, "getShapeProperty", 
($fz = function (shapeType, propertyName) {
return this.eval.getShapeProperty (shapeType, propertyName);
}, $fz.isPrivate = true, $fz), "~N,~S");
$_M(c$, "parameterAsString", 
($fz = function (i) {
return this.eval.parameterAsString (i);
}, $fz.isPrivate = true, $fz), "~N");
$_M(c$, "centerParameter", 
($fz = function (i) {
return this.eval.centerParameter (i);
}, $fz.isPrivate = true, $fz), "~N");
$_M(c$, "floatParameter", 
($fz = function (i) {
return this.eval.floatParameter (i);
}, $fz.isPrivate = true, $fz), "~N");
$_M(c$, "getPoint3f", 
($fz = function (i, allowFractional) {
return this.eval.getPoint3f (i, allowFractional);
}, $fz.isPrivate = true, $fz), "~N,~B");
$_M(c$, "getPoint4f", 
($fz = function (i) {
return this.eval.getPoint4f (i);
}, $fz.isPrivate = true, $fz), "~N");
$_M(c$, "intParameter", 
($fz = function (index) {
return this.eval.intParameter (index);
}, $fz.isPrivate = true, $fz), "~N");
$_M(c$, "isFloatParameter", 
($fz = function (index) {
return this.eval.isFloatParameter (index);
}, $fz.isPrivate = true, $fz), "~N");
$_M(c$, "setShapeId", 
($fz = function (iShape, i, idSeen) {
return this.eval.setShapeId (iShape, i, idSeen);
}, $fz.isPrivate = true, $fz), "~N,~N,~B");
$_M(c$, "setShapeProperty", 
($fz = function (shapeType, propertyName, propertyValue) {
this.eval.setShapeProperty (shapeType, propertyName, propertyValue);
}, $fz.isPrivate = true, $fz), "~N,~S,~O");
$_M(c$, "stringParameter", 
($fz = function (index) {
return this.eval.stringParameter (index);
}, $fz.isPrivate = true, $fz), "~N");
$_M(c$, "tokAt", 
($fz = function (i) {
return this.eval.tokAt (i);
}, $fz.isPrivate = true, $fz), "~N");
$_M(c$, "cgo", 
($fz = function () {
this.sm.loadShape (23);
if (this.tokAt (1) == 1073742001 && this.listIsosurface (23)) return false;
var iptDisplayProperty = 0;
var thisId = this.initIsosurface (23);
var idSeen = (thisId != null);
var isWild = (idSeen && this.getShapeProperty (23, "ID") == null);
var isInitialized = false;
var data = null;
var translucentLevel = 3.4028235E38;
this.eval.colorArgb[0] = -2147483648;
var intScale = 0;
for (var i = this.eval.iToken; i < this.eval.slen; ++i) {
var propertyName = null;
var propertyValue = null;
switch (this.eval.getToken (i).tok) {
case 7:
case 269484096:
case 1073742195:
if (data != null || isWild) this.invArg ();
data = this.eval.listParameter (i, 2, 2147483647);
i = this.eval.iToken;
continue;
case 1073742138:
if (++i >= this.eval.slen) this.error (34);
switch (this.eval.getToken (i).tok) {
case 2:
intScale = this.intParameter (i);
continue;
case 3:
intScale = Math.round (this.floatParameter (i) * 100);
continue;
}
this.error (34);
break;
case 1766856708:
case 603979967:
case 1073742074:
translucentLevel = this.eval.getColorTrans (i, false);
i = this.eval.iToken;
idSeen = true;
continue;
case 1074790550:
thisId = this.setShapeId (23, ++i, idSeen);
isWild = (this.getShapeProperty (23, "ID") == null);
i = this.eval.iToken;
break;
default:
if (!this.eval.setMeshDisplayProperty (23, 0, this.eval.theTok)) {
if (this.eval.theTok == 269484209 || J.script.T.tokAttr (this.eval.theTok, 1073741824)) {
thisId = this.setShapeId (23, i, idSeen);
i = this.eval.iToken;
break;
}this.invArg ();
}if (iptDisplayProperty == 0) iptDisplayProperty = i;
i = this.eval.iToken;
continue;
}
idSeen = (this.eval.theTok != 12291);
if (data != null && !isInitialized) {
propertyName = "points";
propertyValue = Integer.$valueOf (intScale);
isInitialized = true;
intScale = 0;
}if (propertyName != null) this.setShapeProperty (23, propertyName, propertyValue);
}
this.eval.finalizeObject (23, this.eval.colorArgb[0], translucentLevel, intScale, data != null, data, iptDisplayProperty, null);
return true;
}, $fz.isPrivate = true, $fz));
$_M(c$, "contact", 
($fz = function () {
this.sm.loadShape (25);
if (this.tokAt (1) == 1073742001 && this.listIsosurface (25)) return false;
var iptDisplayProperty = 0;
this.eval.iToken = 1;
var thisId = this.initIsosurface (25);
var idSeen = (thisId != null);
var isWild = (idSeen && this.getShapeProperty (25, "ID") == null);
var bsA = null;
var bsB = null;
var bs = null;
var rd = null;
var params = null;
var colorDensity = false;
var sbCommand =  new J.util.SB ();
var minSet = 2147483647;
var displayType = 135266319;
var contactType = 0;
var distance = NaN;
var saProbeRadius = NaN;
var localOnly = true;
var intramolecular = null;
var userSlabObject = null;
var colorpt = 0;
var colorByType = false;
var tok;
var okNoAtoms = (this.eval.iToken > 1);
for (var i = this.eval.iToken; i < this.eval.slen; ++i) {
switch (tok = this.eval.getToken (i).tok) {
default:
okNoAtoms = true;
if (!this.eval.setMeshDisplayProperty (25, 0, this.eval.theTok)) {
if (this.eval.theTok != 269484209 && !J.script.T.tokAttr (this.eval.theTok, 1073741824)) this.invArg ();
thisId = this.setShapeId (25, i, idSeen);
i = this.eval.iToken;
break;
}if (iptDisplayProperty == 0) iptDisplayProperty = i;
i = this.eval.iToken;
continue;
case 1074790550:
okNoAtoms = true;
this.setShapeId (25, ++i, idSeen);
isWild = (this.getShapeProperty (25, "ID") == null);
i = this.eval.iToken;
break;
case 1766856708:
switch (this.tokAt (i + 1)) {
case 1073741914:
tok = 0;
colorDensity = true;
sbCommand.append (" color density");
i++;
break;
case 1141899272:
tok = 0;
colorByType = true;
sbCommand.append (" color type");
i++;
break;
}
if (tok == 0) break;
case 603979967:
case 1073742074:
okNoAtoms = true;
if (colorpt == 0) colorpt = i;
this.eval.setMeshDisplayProperty (25, i, this.eval.theTok);
i = this.eval.iToken;
break;
case 554176565:
okNoAtoms = true;
userSlabObject = this.getCapSlabObject (i, false);
this.setShapeProperty (25, "slab", userSlabObject);
i = this.eval.iToken;
break;
case 1073741914:
colorDensity = true;
sbCommand.append (" density");
if (this.isFloatParameter (i + 1)) {
if (params == null) params =  Clazz.newFloatArray (1, 0);
params[0] = -Math.abs (this.floatParameter (++i));
sbCommand.append (" " + -params[0]);
}break;
case 1073742122:
var resolution = this.floatParameter (++i);
if (resolution > 0) {
sbCommand.append (" resolution ").appendF (resolution);
this.setShapeProperty (25, "resolution", Float.$valueOf (resolution));
}break;
case 135266324:
case 1276118018:
distance = this.floatParameter (++i);
sbCommand.append (" within ").appendF (distance);
break;
case 269484193:
case 2:
case 3:
rd = this.eval.encodeRadiusParameter (i, false, false);
sbCommand.append (" ").appendO (rd);
i = this.eval.iToken;
break;
case 1073741990:
case 1073741989:
intramolecular = (tok == 1073741989 ? Boolean.TRUE : Boolean.FALSE);
sbCommand.append (" ").appendO (this.eval.theToken.value);
break;
case 1073742020:
minSet = this.intParameter (++i);
break;
case 1612189718:
case 1073741881:
case 1649412120:
contactType = tok;
sbCommand.append (" ").appendO (this.eval.theToken.value);
break;
case 1073742136:
if (this.isFloatParameter (i + 1)) saProbeRadius = this.floatParameter (++i);
case 1074790451:
case 1073742036:
case 3145756:
localOnly = false;
case 1276117510:
case 1073741961:
case 135266319:
case 4106:
displayType = tok;
sbCommand.append (" ").appendO (this.eval.theToken.value);
if (tok == 1073742136) sbCommand.append (" ").appendF (saProbeRadius);
break;
case 1073742083:
params = this.eval.floatParameterSet (++i, 1, 10);
i = this.eval.iToken;
break;
case 10:
case 1048577:
if (isWild || bsB != null) this.invArg ();
bs = J.util.BSUtil.copy (this.atomExpressionAt (i));
i = this.eval.iToken;
if (bsA == null) bsA = bs;
 else bsB = bs;
sbCommand.append (" ").append (J.util.Escape.eBS (bs));
break;
}
idSeen = (this.eval.theTok != 12291);
}
if (!okNoAtoms && bsA == null) this.error (13);
if (this.chk) return false;
if (bsA != null) {
if (contactType == 1649412120 && rd == null) rd =  new J.atomdata.RadiusData (null, 0, J.atomdata.RadiusData.EnumType.OFFSET, J.constant.EnumVdw.AUTO);
var rd1 = (rd == null ?  new J.atomdata.RadiusData (null, 0.26, J.atomdata.RadiusData.EnumType.OFFSET, J.constant.EnumVdw.AUTO) : rd);
if (displayType == 1073742036 && bsB == null && intramolecular != null && intramolecular.booleanValue ()) bsB = bsA;
 else bsB = this.eval.setContactBitSets (bsA, bsB, localOnly, distance, rd1, true);
switch (displayType) {
case 1074790451:
case 1073742136:
var bsSolvent = this.eval.lookupIdentifierValue ("solvent");
bsA.andNot (bsSolvent);
bsB.andNot (bsSolvent);
bsB.andNot (bsA);
break;
case 3145756:
bsB.andNot (bsA);
break;
case 1073742036:
if (minSet == 2147483647) minSet = 100;
this.setShapeProperty (25, "minset", Integer.$valueOf (minSet));
sbCommand.append (" minSet ").appendI (minSet);
if (params == null) params = [0.5, 2];
}
if (intramolecular != null) {
params = (params == null ?  Clazz.newFloatArray (2, 0) : J.util.ArrayUtil.ensureLengthA (params, 2));
params[1] = (intramolecular.booleanValue () ? 1 : 2);
}if (params != null) sbCommand.append (" parameters ").append (J.util.Escape.eAF (params));
this.setShapeProperty (25, "set", [Integer.$valueOf (contactType), Integer.$valueOf (displayType), Boolean.$valueOf (colorDensity), Boolean.$valueOf (colorByType), bsA, bsB, rd, Float.$valueOf (saProbeRadius), params, sbCommand.toString ()]);
if (colorpt > 0) this.eval.setMeshDisplayProperty (25, colorpt, 0);
}if (iptDisplayProperty > 0) {
if (!this.eval.setMeshDisplayProperty (25, iptDisplayProperty, 0)) this.invArg ();
}if (userSlabObject != null && bsA != null) this.setShapeProperty (25, "slab", userSlabObject);
if (bsA != null && (displayType == 1073742036 || localOnly)) {
var volume = this.getShapeProperty (25, "volume");
if (J.util.Escape.isAD (volume)) {
var vs = volume;
var v = 0;
for (var i = 0; i < vs.length; i++) v += Math.abs (vs[i]);

volume = Float.$valueOf (v);
}var nsets = (this.getShapeProperty (25, "nSets")).intValue ();
if (colorDensity || displayType != 1276117510) {
this.eval.showString ((nsets == 0 ? "" : nsets + " contacts with ") + "net volume " + volume + " A^3");
}}return true;
}, $fz.isPrivate = true, $fz));
$_M(c$, "dipole", 
($fz = function () {
var propertyName = null;
var propertyValue = null;
var iHaveAtoms = false;
var iHaveCoord = false;
var idSeen = false;
this.sm.loadShape (17);
if (this.tokAt (1) == 1073742001 && this.listIsosurface (17)) return false;
this.setShapeProperty (17, "init", null);
if (this.eval.slen == 1) {
this.setShapeProperty (17, "thisID", null);
return false;
}for (var i = 1; i < this.eval.slen; ++i) {
propertyName = null;
propertyValue = null;
switch (this.eval.getToken (i).tok) {
case 1048589:
propertyName = "on";
break;
case 1048588:
propertyName = "off";
break;
case 12291:
propertyName = "delete";
break;
case 2:
case 3:
propertyName = "value";
propertyValue = Float.$valueOf (this.floatParameter (i));
break;
case 10:
propertyName = "atomBitset";
case 1048577:
if (propertyName == null) propertyName = (iHaveAtoms || iHaveCoord ? "endSet" : "startSet");
propertyValue = this.atomExpressionAt (i);
i = this.eval.iToken;
iHaveAtoms = true;
break;
case 1048586:
case 8:
var pt = this.getPoint3f (i, true);
i = this.eval.iToken;
propertyName = (iHaveAtoms || iHaveCoord ? "endCoord" : "startCoord");
propertyValue = pt;
iHaveCoord = true;
break;
case 1678770178:
propertyName = "bonds";
break;
case 4102:
propertyName = "calculate";
break;
case 1074790550:
this.setShapeId (17, ++i, idSeen);
i = this.eval.iToken;
break;
case 135267329:
propertyName = "cross";
propertyValue = Boolean.TRUE;
break;
case 1073742040:
propertyName = "cross";
propertyValue = Boolean.FALSE;
break;
case 1073742066:
var v = this.floatParameter (++i);
if (this.eval.theTok == 2) {
propertyName = "offsetPercent";
propertyValue = Integer.$valueOf (Clazz.floatToInt (v));
} else {
propertyName = "offset";
propertyValue = Float.$valueOf (v);
}break;
case 1073742068:
propertyName = "offsetSide";
propertyValue = Float.$valueOf (this.floatParameter (++i));
break;
case 1073742188:
propertyName = "value";
propertyValue = Float.$valueOf (this.floatParameter (++i));
break;
case 1073742196:
propertyName = "width";
propertyValue = Float.$valueOf (this.floatParameter (++i));
break;
default:
if (this.eval.theTok == 269484209 || J.script.T.tokAttr (this.eval.theTok, 1073741824)) {
this.setShapeId (17, i, idSeen);
i = this.eval.iToken;
break;
}this.invArg ();
}
idSeen = (this.eval.theTok != 12291 && this.eval.theTok != 4102);
if (propertyName != null) this.setShapeProperty (17, propertyName, propertyValue);
}
if (iHaveCoord || iHaveAtoms) this.setShapeProperty (17, "set", null);
return true;
}, $fz.isPrivate = true, $fz));
$_M(c$, "draw", 
($fz = function () {
this.sm.loadShape (22);
switch (this.tokAt (1)) {
case 1073742001:
if (this.listIsosurface (22)) return false;
break;
case 1073742102:
this.eval.pointGroup ();
return false;
case 137363468:
case 135270417:
case 1052714:
this.plot (this.st);
return false;
}
var havePoints = false;
var isInitialized = false;
var isSavedState = false;
var isIntersect = false;
var isFrame = false;
var plane;
var tokIntersect = 0;
var translucentLevel = 3.4028235E38;
this.eval.colorArgb[0] = -2147483648;
var intScale = 0;
var swidth = "";
var iptDisplayProperty = 0;
var center = null;
var thisId = this.initIsosurface (22);
var idSeen = (thisId != null);
var isWild = (idSeen && this.getShapeProperty (22, "ID") == null);
var connections = null;
var iConnect = 0;
for (var i = this.eval.iToken; i < this.eval.slen; ++i) {
var propertyName = null;
var propertyValue = null;
switch (this.eval.getToken (i).tok) {
case 1614417948:
case 1679429641:
if (this.chk) break;
var vp = this.viewer.getPlaneIntersection (this.eval.theTok, null, intScale / 100, 0);
intScale = 0;
propertyName = "polygon";
propertyValue = vp;
havePoints = true;
break;
case 4106:
connections =  Clazz.newIntArray (4, 0);
iConnect = 4;
var farray = this.eval.floatParameterSet (++i, 4, 4);
i = this.eval.iToken;
for (var j = 0; j < 4; j++) connections[j] = Clazz.floatToInt (farray[j]);

havePoints = true;
break;
case 1678770178:
case 1141899265:
if (connections == null || iConnect > (this.eval.theTok == 1095761924 ? 2 : 3)) {
iConnect = 0;
connections = [-1, -1, -1, -1];
}connections[iConnect++] = this.atomExpressionAt (++i).nextSetBit (0);
i = this.eval.iToken;
connections[iConnect++] = (this.eval.theTok == 1678770178 ? this.atomExpressionAt (++i).nextSetBit (0) : -1);
i = this.eval.iToken;
havePoints = true;
break;
case 554176565:
switch (this.eval.getToken (++i).tok) {
case 1048583:
propertyName = "slab";
propertyValue = this.eval.objectNameParameter (++i);
i = this.eval.iToken;
havePoints = true;
break;
default:
this.invArg ();
}
break;
case 135267842:
switch (this.eval.getToken (++i).tok) {
case 1614417948:
case 1679429641:
tokIntersect = this.eval.theTok;
isIntersect = true;
continue;
case 1048583:
propertyName = "intersect";
propertyValue = this.eval.objectNameParameter (++i);
i = this.eval.iToken;
isIntersect = true;
havePoints = true;
break;
default:
this.invArg ();
}
break;
case 1073742106:
propertyName = "polygon";
havePoints = true;
var v =  new J.util.JmolList ();
var nVertices = 0;
var nTriangles = 0;
var points = null;
var vpolygons = null;
if (this.eval.isArrayParameter (++i)) {
points = this.eval.getPointArray (i, -1);
nVertices = points.length;
} else {
nVertices = Math.max (0, this.intParameter (i));
points =  new Array (nVertices);
for (var j = 0; j < nVertices; j++) points[j] = this.centerParameter (++this.eval.iToken);

}switch (this.eval.getToken (++this.eval.iToken).tok) {
case 11:
case 12:
var sv = J.script.SV.newScriptVariableToken (this.eval.theToken);
sv.toArray ();
vpolygons = sv.getList ();
nTriangles = vpolygons.size ();
break;
case 7:
vpolygons = (this.eval.theToken).getList ();
nTriangles = vpolygons.size ();
break;
default:
nTriangles = Math.max (0, this.intParameter (this.eval.iToken));
}
var polygons = J.util.ArrayUtil.newInt2 (nTriangles);
for (var j = 0; j < nTriangles; j++) {
var f = (vpolygons == null ? this.eval.floatParameterSet (++this.eval.iToken, 3, 4) : J.script.SV.flistValue (vpolygons.get (j), 0));
if (f.length < 3 || f.length > 4) this.invArg ();
polygons[j] = [Clazz.floatToInt (f[0]), Clazz.floatToInt (f[1]), Clazz.floatToInt (f[2]), (f.length == 3 ? 7 : Clazz.floatToInt (f[3]))];
}
if (nVertices > 0) {
v.addLast (points);
v.addLast (polygons);
} else {
v = null;
}propertyValue = v;
i = this.eval.iToken;
break;
case 1297090050:
var xyz = null;
var iSym = 0;
plane = null;
var target = null;
switch (this.tokAt (++i)) {
case 4:
xyz = this.stringParameter (i);
break;
case 12:
xyz = J.script.SV.sValue (this.eval.getToken (i));
break;
case 2:
default:
if (!this.eval.isCenterParameter (i)) iSym = this.intParameter (i++);
if (this.eval.isCenterParameter (i)) center = this.centerParameter (i);
if (this.eval.isCenterParameter (this.eval.iToken + 1)) target = this.centerParameter (++this.eval.iToken);
if (this.chk) return false;
i = this.eval.iToken;
}
var bsAtoms = null;
if (center == null && i + 1 < this.eval.slen) {
center = this.centerParameter (++i);
bsAtoms = (this.tokAt (i) == 10 || this.tokAt (i) == 1048577 ? this.atomExpressionAt (i) : null);
i = this.eval.iToken + 1;
}this.eval.checkLast (this.eval.iToken);
if (!this.chk) this.eval.runScript (this.viewer.getSymmetryInfo (bsAtoms, xyz, iSym, center, target, thisId, 135176));
return false;
case 4115:
isFrame = true;
continue;
case 1048586:
case 9:
case 8:
if (this.eval.theTok == 9 || !this.eval.isPoint3f (i)) {
propertyValue = this.getPoint4f (i);
if (isFrame) {
this.eval.checkLast (this.eval.iToken);
if (!this.chk) this.eval.runScript ((J.util.Quaternion.newP4 (propertyValue)).draw ((thisId == null ? "frame" : thisId), " " + swidth, (center == null ?  new J.util.P3 () : center), intScale / 100));
return false;
}propertyName = "planedef";
} else {
propertyValue = center = this.getPoint3f (i, true);
propertyName = "coord";
}i = this.eval.iToken;
havePoints = true;
break;
case 135267841:
case 135266319:
if (!havePoints && !isIntersect && tokIntersect == 0 && this.eval.theTok != 135267841) {
propertyName = "plane";
break;
}if (this.eval.theTok == 135266319) {
plane = this.eval.planeParameter (++i);
} else {
plane = this.eval.hklParameter (++i);
}i = this.eval.iToken;
if (tokIntersect != 0) {
if (this.chk) break;
var vpc = this.viewer.getPlaneIntersection (tokIntersect, plane, intScale / 100, 0);
intScale = 0;
propertyName = "polygon";
propertyValue = vpc;
} else {
propertyValue = plane;
propertyName = "planedef";
}havePoints = true;
break;
case 1073742000:
propertyName = "lineData";
propertyValue = this.eval.floatParameterSet (++i, 0, 2147483647);
i = this.eval.iToken;
havePoints = true;
break;
case 10:
case 1048577:
propertyName = "atomSet";
propertyValue = this.atomExpressionAt (i);
if (isFrame) center = this.centerParameter (i);
i = this.eval.iToken;
havePoints = true;
break;
case 7:
propertyName = "modelBasedPoints";
propertyValue = J.script.SV.listValue (this.eval.theToken);
havePoints = true;
break;
case 1073742195:
case 269484080:
break;
case 269484096:
propertyValue = this.eval.xypParameter (i);
if (propertyValue != null) {
i = this.eval.iToken;
propertyName = "coord";
havePoints = true;
break;
}if (isSavedState) this.invArg ();
isSavedState = true;
break;
case 269484097:
if (!isSavedState) this.invArg ();
isSavedState = false;
break;
case 1141899269:
propertyName = "reverse";
break;
case 4:
propertyValue = this.stringParameter (i);
propertyName = "title";
break;
case 135198:
propertyName = "vector";
break;
case 1141899267:
propertyValue = Float.$valueOf (this.floatParameter (++i));
propertyName = "length";
break;
case 3:
propertyValue = Float.$valueOf (this.floatParameter (i));
propertyName = "length";
break;
case 1095761933:
propertyName = "modelIndex";
propertyValue = Integer.$valueOf (this.intParameter (++i));
break;
case 2:
if (isSavedState) {
propertyName = "modelIndex";
propertyValue = Integer.$valueOf (this.intParameter (i));
} else {
intScale = this.intParameter (i);
}break;
case 1073742138:
if (++i >= this.eval.slen) this.error (34);
switch (this.eval.getToken (i).tok) {
case 2:
intScale = this.intParameter (i);
continue;
case 3:
intScale = Math.round (this.floatParameter (i) * 100);
continue;
}
this.error (34);
break;
case 1074790550:
thisId = this.setShapeId (22, ++i, idSeen);
isWild = (this.getShapeProperty (22, "ID") == null);
i = this.eval.iToken;
break;
case 1073742028:
propertyName = "fixed";
propertyValue = Boolean.FALSE;
break;
case 1060869:
propertyName = "fixed";
propertyValue = Boolean.TRUE;
break;
case 1073742066:
var pt = this.getPoint3f (++i, true);
i = this.eval.iToken;
propertyName = "offset";
propertyValue = pt;
break;
case 1073741906:
propertyName = "crossed";
break;
case 1073742196:
propertyValue = Float.$valueOf (this.floatParameter (++i));
propertyName = "width";
swidth = propertyName + " " + propertyValue;
break;
case 1073741998:
propertyName = "line";
propertyValue = Boolean.TRUE;
break;
case 1073741908:
propertyName = "curve";
break;
case 1074790416:
propertyName = "arc";
break;
case 1073741846:
propertyName = "arrow";
break;
case 1073741880:
propertyName = "circle";
break;
case 1073741912:
propertyName = "cylinder";
break;
case 1073742194:
propertyName = "vertices";
break;
case 1073742048:
propertyName = "nohead";
break;
case 1073741861:
propertyName = "isbarb";
break;
case 1073742130:
propertyName = "rotate45";
break;
case 1073742092:
propertyName = "perp";
break;
case 1666189314:
case 1073741916:
var isRadius = (this.eval.theTok == 1666189314);
var f = this.floatParameter (++i);
if (isRadius) f *= 2;
propertyValue = Float.$valueOf (f);
propertyName = (isRadius || this.tokAt (i) == 3 ? "width" : "diameter");
swidth = propertyName + (this.tokAt (i) == 3 ? " " + f : " " + (Clazz.floatToInt (f)));
break;
case 1048583:
if ((this.tokAt (i + 2) == 269484096 || isFrame)) {
var pto = center = this.centerParameter (i);
i = this.eval.iToken;
propertyName = "coord";
propertyValue = pto;
havePoints = true;
break;
}propertyValue = this.eval.objectNameParameter (++i);
propertyName = "identifier";
havePoints = true;
break;
case 1766856708:
case 603979967:
case 1073742074:
idSeen = true;
translucentLevel = this.eval.getColorTrans (i, false);
i = this.eval.iToken;
continue;
default:
if (!this.eval.setMeshDisplayProperty (22, 0, this.eval.theTok)) {
if (this.eval.theTok == 269484209 || J.script.T.tokAttr (this.eval.theTok, 1073741824)) {
thisId = this.setShapeId (22, i, idSeen);
i = this.eval.iToken;
break;
}this.invArg ();
}if (iptDisplayProperty == 0) iptDisplayProperty = i;
i = this.eval.iToken;
continue;
}
idSeen = (this.eval.theTok != 12291);
if (havePoints && !isInitialized && !isFrame) {
this.setShapeProperty (22, "points", Integer.$valueOf (intScale));
isInitialized = true;
intScale = 0;
}if (havePoints && isWild) this.invArg ();
if (propertyName != null) this.setShapeProperty (22, propertyName, propertyValue);
}
this.eval.finalizeObject (22, this.eval.colorArgb[0], translucentLevel, intScale, havePoints, connections, iptDisplayProperty, null);
return true;
}, $fz.isPrivate = true, $fz));
$_M(c$, "isosurface", 
($fz = function (iShape) {
this.sm.loadShape (iShape);
if (this.tokAt (1) == 1073742001 && this.listIsosurface (iShape)) return false;
var iptDisplayProperty = 0;
var isIsosurface = (iShape == 24);
var isPmesh = (iShape == 28);
var isPlot3d = (iShape == 29);
var isLcaoCartoon = (iShape == 26);
var surfaceObjectSeen = false;
var planeSeen = false;
var isMapped = false;
var isBicolor = false;
var isPhased = false;
var doCalcArea = false;
var doCalcVolume = false;
var isCavity = false;
var haveRadius = false;
var toCache = false;
var isFxy = false;
var haveSlab = false;
var haveIntersection = false;
var data = null;
var cmd = null;
var thisSetNumber = -2147483648;
var nFiles = 0;
var nX;
var nY;
var nZ;
var ptX;
var ptY;
var sigma = NaN;
var cutoff = NaN;
var ptWithin = 0;
var smoothing = null;
var smoothingPower = 2147483647;
var bs = null;
var bsSelect = null;
var bsIgnore = null;
var sbCommand =  new J.util.SB ();
var pt;
var plane = null;
var lattice = null;
var pts;
var str = null;
var modelIndex = (this.chk ? 0 : -2147483648);
this.eval.setCursorWait (true);
var idSeen = (this.initIsosurface (iShape) != null);
var isWild = (idSeen && this.getShapeProperty (iShape, "ID") == null);
var isColorSchemeTranslucent = false;
var isInline = false;
var onlyOneModel = null;
var translucency = null;
var colorScheme = null;
var mepOrMlp = null;
var discreteColixes = null;
var propertyList =  new J.util.JmolList ();
var defaultMesh = false;
if (isPmesh || isPlot3d) this.addShapeProperty (propertyList, "fileType", "Pmesh");
for (var i = this.eval.iToken; i < this.eval.slen; ++i) {
var propertyName = null;
var propertyValue = null;
this.eval.getToken (i);
if (this.eval.theTok == 1073741824) str = this.parameterAsString (i);
switch (this.eval.theTok) {
case 603979871:
smoothing = (this.eval.getToken (++i).tok == 1048589 ? Boolean.TRUE : this.eval.theTok == 1048588 ? Boolean.FALSE : null);
if (smoothing == null) this.invArg ();
continue;
case 553648149:
smoothingPower = this.intParameter (++i);
continue;
case 4128:
propertyName = "moveIsosurface";
if (this.tokAt (++i) != 12) this.invArg ();
propertyValue = this.eval.getToken (i++).value;
break;
case 1073742066:
propertyName = "offset";
propertyValue = this.centerParameter (++i);
i = this.eval.iToken;
break;
case 528432:
propertyName = "rotate";
propertyValue = (this.tokAt (this.eval.iToken = ++i) == 1048587 ? null : this.getPoint4f (i));
i = this.eval.iToken;
break;
case 1610612740:
propertyName = "scale3d";
propertyValue = Float.$valueOf (this.floatParameter (++i));
break;
case 1073742090:
sbCommand.append (" periodic");
propertyName = "periodic";
break;
case 1073742078:
case 266298:
case 135266320:
propertyName = this.eval.theToken.value.toString ();
sbCommand.append (" ").appendO (this.eval.theToken.value);
propertyValue = this.centerParameter (++i);
sbCommand.append (" ").append (J.util.Escape.eP (propertyValue));
i = this.eval.iToken;
break;
case 1679429641:
if (this.fullCommand.indexOf ("# BBOX=") >= 0) {
var bbox = J.util.TextFormat.split (J.util.Parser.getQuotedAttribute (this.fullCommand, "# BBOX"), ',');
pts = [J.util.Escape.uP (bbox[0]), J.util.Escape.uP (bbox[1])];
} else if (this.eval.isCenterParameter (i + 1)) {
pts = [this.getPoint3f (i + 1, true), this.getPoint3f (this.eval.iToken + 1, true)];
i = this.eval.iToken;
} else {
pts = this.viewer.getBoundBoxVertices ();
}sbCommand.append (" boundBox " + J.util.Escape.eP (pts[0]) + " " + J.util.Escape.eP (pts[pts.length - 1]));
propertyName = "boundingBox";
propertyValue = pts;
break;
case 135188:
isPmesh = true;
sbCommand.append (" pmesh");
propertyName = "fileType";
propertyValue = "Pmesh";
break;
case 135267842:
bsSelect = this.atomExpressionAt (++i);
if (this.chk) {
bs =  new J.util.BS ();
} else if (this.tokAt (this.eval.iToken + 1) == 1048577 || this.tokAt (this.eval.iToken + 1) == 10) {
bs = this.atomExpressionAt (++this.eval.iToken);
bs.and (this.viewer.getAtomsWithinRadius (5.0, bsSelect, false, null));
} else {
bs = this.viewer.getAtomsWithinRadius (5.0, bsSelect, true, null);
bs.andNot (this.viewer.getAtomBits (1095761934, bsSelect));
}bs.andNot (bsSelect);
sbCommand.append (" intersection ").append (J.util.Escape.eBS (bsSelect)).append (" ").append (J.util.Escape.eBS (bs));
i = this.eval.iToken;
if (this.tokAt (i + 1) == 135368713) {
i++;
var f = this.eval.getToken (++i).value;
sbCommand.append (" function ").append (J.util.Escape.eS (f));
if (!this.chk) this.addShapeProperty (propertyList, "func", (f.equals ("a+b") || f.equals ("a-b") ? f : this.createFunction ("__iso__", "a,b", f)));
} else {
haveIntersection = true;
}propertyName = "intersection";
propertyValue = [bsSelect, bs];
break;
case 1610625028:
case 135266324:
var isDisplay = (this.eval.theTok == 1610625028);
if (isDisplay) {
sbCommand.append (" display");
iptDisplayProperty = i;
var tok = this.tokAt (i + 1);
if (tok == 0) continue;
i++;
this.addShapeProperty (propertyList, "token", Integer.$valueOf (1048589));
if (tok == 10 || tok == 1048579) {
propertyName = "bsDisplay";
if (tok == 1048579) {
sbCommand.append (" all");
} else {
propertyValue = this.st[i].value;
sbCommand.append (" ").append (J.util.Escape.eBS (propertyValue));
}this.eval.checkLast (i);
break;
} else if (tok != 135266324) {
this.eval.iToken = i;
this.invArg ();
}} else {
ptWithin = i;
}var distance;
var ptc = null;
bs = null;
var havePt = false;
if (this.tokAt (i + 1) == 1048577) {
distance = this.floatParameter (i + 3);
if (this.eval.isPoint3f (i + 4)) {
ptc = this.centerParameter (i + 4);
havePt = true;
this.eval.iToken = this.eval.iToken + 2;
} else if (this.eval.isPoint3f (i + 5)) {
ptc = this.centerParameter (i + 5);
havePt = true;
this.eval.iToken = this.eval.iToken + 2;
} else {
bs = this.eval.atomExpression (this.st, i + 5, this.eval.slen, true, false, false, true);
if (bs == null) this.invArg ();
}} else {
distance = this.floatParameter (++i);
ptc = this.centerParameter (++i);
}if (isDisplay) this.eval.checkLast (this.eval.iToken);
i = this.eval.iToken;
if (this.fullCommand.indexOf ("# WITHIN=") >= 0) bs = J.util.Escape.uB (J.util.Parser.getQuotedAttribute (this.fullCommand, "# WITHIN"));
 else if (!havePt) bs = (Clazz.instanceOf (this.eval.expressionResult, J.util.BS) ? this.eval.expressionResult : null);
if (!this.chk) {
if (bs != null && modelIndex >= 0) {
bs.and (this.viewer.getModelUndeletedAtomsBitSet (modelIndex));
}if (ptc == null) ptc = this.viewer.getAtomSetCenter (bs);
this.getWithinDistanceVector (propertyList, distance, ptc, bs, isDisplay);
sbCommand.append (" within ").appendF (distance).append (" ").append (bs == null ? J.util.Escape.eP (ptc) : J.util.Escape.eBS (bs));
}continue;
case 1073742083:
propertyName = "parameters";
var fparams = this.eval.floatParameterSet (++i, 1, 10);
i = this.eval.iToken;
propertyValue = fparams;
sbCommand.append (" parameters ").append (J.util.Escape.eAF (fparams));
break;
case 1716520985:
case 1073742190:
onlyOneModel = this.eval.theToken.value;
var isVariable = (this.eval.theTok == 1073742190);
var tokProperty = this.tokAt (i + 1);
if (mepOrMlp == null) {
if (!surfaceObjectSeen && !isMapped && !planeSeen) {
this.addShapeProperty (propertyList, "sasurface", Float.$valueOf (0));
sbCommand.append (" vdw");
surfaceObjectSeen = true;
}propertyName = "property";
if (smoothing == null) {
var allowSmoothing = true;
switch (tokProperty) {
case 1095761923:
case 1095763969:
case 1095763976:
case 1766856708:
case 1095761937:
allowSmoothing = false;
break;
}
smoothing = (allowSmoothing && this.viewer.getIsosurfacePropertySmoothing (false) == 1 ? Boolean.TRUE : Boolean.FALSE);
}this.addShapeProperty (propertyList, "propertySmoothing", smoothing);
sbCommand.append (" isosurfacePropertySmoothing " + smoothing);
if (smoothing === Boolean.TRUE) {
if (smoothingPower == 2147483647) smoothingPower = this.viewer.getIsosurfacePropertySmoothing (true);
this.addShapeProperty (propertyList, "propertySmoothingPower", Integer.$valueOf (smoothingPower));
sbCommand.append (" isosurfacePropertySmoothingPower " + smoothingPower);
}if (this.viewer.global.rangeSelected) this.addShapeProperty (propertyList, "rangeSelected", Boolean.TRUE);
} else {
propertyName = mepOrMlp;
}str = this.parameterAsString (i);
sbCommand.append (" ").append (str);
if (str.toLowerCase ().indexOf ("property_") == 0) {
data =  Clazz.newFloatArray (this.viewer.getAtomCount (), 0);
if (this.chk) continue;
data = this.viewer.getDataFloat (str);
if (data == null) this.invArg ();
this.addShapeProperty (propertyList, propertyName, data);
continue;
}var atomCount = this.viewer.getAtomCount ();
data =  Clazz.newFloatArray (atomCount, 0);
if (isVariable) {
var vname = this.parameterAsString (++i);
if (vname.length == 0) {
data = this.eval.floatParameterSet (i, atomCount, atomCount);
} else {
data =  Clazz.newFloatArray (atomCount, 0);
if (!this.chk) J.util.Parser.parseStringInfestedFloatArray ("" + this.eval.getParameter (vname, 4), null, data);
}if (!this.chk) sbCommand.append (" \"\" ").append (J.util.Escape.eAF (data));
} else {
this.eval.getToken (++i);
if (!this.chk) {
sbCommand.append (" " + this.eval.theToken.value);
var atoms = this.viewer.modelSet.atoms;
this.viewer.autoCalculate (tokProperty);
if (tokProperty != 1766856708) for (var iAtom = atomCount; --iAtom >= 0; ) data[iAtom] = J.modelset.Atom.atomPropertyFloat (this.viewer, atoms[iAtom], tokProperty);

}if (tokProperty == 1766856708) colorScheme = "inherit";
if (this.tokAt (i + 1) == 135266324) {
var d = this.floatParameter (i = i + 2);
sbCommand.append (" within " + d);
this.addShapeProperty (propertyList, "propertyDistanceMax", Float.$valueOf (d));
}}propertyValue = data;
break;
case 1095761933:
case 1095766028:
if (surfaceObjectSeen) this.invArg ();
modelIndex = (this.eval.theTok == 1095761933 ? this.intParameter (++i) : this.eval.modelNumberParameter (++i));
sbCommand.append (" modelIndex " + modelIndex);
if (modelIndex < 0) {
propertyName = "fixed";
propertyValue = Boolean.TRUE;
break;
}propertyName = "modelIndex";
propertyValue = Integer.$valueOf (modelIndex);
break;
case 135280132:
propertyName = "select";
var bs1 = this.atomExpressionAt (++i);
propertyValue = bs1;
i = this.eval.iToken;
var isOnly = (this.tokAt (i + 1) == 1073742072);
if (isOnly) {
i++;
var bs2 = J.util.BSUtil.copy (bs1);
J.util.BSUtil.invertInPlace (bs2, this.viewer.getAtomCount ());
this.addShapeProperty (propertyList, "ignore", bs2);
sbCommand.append (" ignore ").append (J.util.Escape.eBS (bs2));
}if (surfaceObjectSeen || isMapped) {
sbCommand.append (" select " + J.util.Escape.eBS (bs1));
} else {
bsSelect = propertyValue;
if (modelIndex < 0 && bsSelect.nextSetBit (0) >= 0) modelIndex = this.viewer.getAtomModelIndex (bsSelect.nextSetBit (0));
}break;
case 1085443:
thisSetNumber = this.intParameter (++i);
break;
case 12289:
propertyName = "center";
propertyValue = this.centerParameter (++i);
sbCommand.append (" center " + J.util.Escape.eP (propertyValue));
i = this.eval.iToken;
break;
case 1073742147:
case 1766856708:
var color;
idSeen = true;
var isSign = (this.eval.theTok == 1073742147);
if (isSign) {
sbCommand.append (" sign");
this.addShapeProperty (propertyList, "sign", Boolean.TRUE);
} else {
if (this.tokAt (i + 1) == 1073741914) {
i++;
propertyName = "colorDensity";
sbCommand.append (" color density");
if (this.isFloatParameter (i + 1)) {
var ptSize = this.floatParameter (++i);
sbCommand.append (" " + ptSize);
propertyValue = Float.$valueOf (ptSize);
}break;
}if (this.eval.getToken (i + 1).tok == 4) {
colorScheme = this.parameterAsString (++i);
if (colorScheme.indexOf (" ") > 0) {
discreteColixes = J.util.C.getColixArray (colorScheme);
if (discreteColixes == null) this.error (4);
}} else if (this.eval.theTok == 1073742018) {
i++;
sbCommand.append (" color mesh");
color = this.eval.getArgbParam (++i);
this.addShapeProperty (propertyList, "meshcolor", Integer.$valueOf (color));
sbCommand.append (" ").append (J.util.Escape.escapeColor (color));
i = this.eval.iToken;
continue;
}if ((this.eval.theTok = this.tokAt (i + 1)) == 603979967 || this.eval.theTok == 1073742074) {
sbCommand.append (" color");
translucency = this.setColorOptions (sbCommand, i + 1, 24, -2);
i = this.eval.iToken;
continue;
}switch (this.tokAt (i + 1)) {
case 1073741826:
case 1073742114:
this.eval.getToken (++i);
sbCommand.append (" color range");
this.addShapeProperty (propertyList, "rangeAll", null);
if (this.tokAt (i + 1) == 1048579) {
i++;
sbCommand.append (" all");
continue;
}var min = this.floatParameter (++i);
var max = this.floatParameter (++i);
this.addShapeProperty (propertyList, "red", Float.$valueOf (min));
this.addShapeProperty (propertyList, "blue", Float.$valueOf (max));
sbCommand.append (" ").appendF (min).append (" ").appendF (max);
continue;
}
if (this.eval.isColorParam (i + 1)) {
color = this.eval.getArgbParam (i + 1);
if (this.tokAt (i + 2) == 1074790746) {
colorScheme = this.eval.getColorRange (i + 1);
i = this.eval.iToken;
break;
}}sbCommand.append (" color");
}if (this.eval.isColorParam (i + 1)) {
color = this.eval.getArgbParam (++i);
sbCommand.append (" ").append (J.util.Escape.escapeColor (color));
i = this.eval.iToken;
this.addShapeProperty (propertyList, "colorRGB", Integer.$valueOf (color));
idSeen = true;
if (this.eval.isColorParam (i + 1)) {
color = this.eval.getArgbParam (++i);
i = this.eval.iToken;
this.addShapeProperty (propertyList, "colorRGB", Integer.$valueOf (color));
sbCommand.append (" ").append (J.util.Escape.escapeColor (color));
isBicolor = true;
} else if (isSign) {
this.invPO ();
}} else if (!isSign && discreteColixes == null) {
this.invPO ();
}continue;
case 135270422:
if (!isIsosurface) this.invArg ();
toCache = !this.chk;
continue;
case 1229984263:
if (this.tokAt (i + 1) != 4) this.invPO ();
continue;
case 1112541195:
case 1649412120:
sbCommand.append (" ").appendO (this.eval.theToken.value);
var rd = this.eval.encodeRadiusParameter (i, false, true);
sbCommand.append (" ").appendO (rd);
if (Float.isNaN (rd.value)) rd.value = 100;
propertyValue = rd;
propertyName = "radius";
haveRadius = true;
if (isMapped) surfaceObjectSeen = false;
i = this.eval.iToken;
break;
case 135266319:
planeSeen = true;
propertyName = "plane";
propertyValue = this.eval.planeParameter (++i);
i = this.eval.iToken;
sbCommand.append (" plane ").append (J.util.Escape.eP4 (propertyValue));
break;
case 1073742138:
propertyName = "scale";
propertyValue = Float.$valueOf (this.floatParameter (++i));
sbCommand.append (" scale ").appendO (propertyValue);
break;
case 1048579:
if (idSeen) this.invArg ();
propertyName = "thisID";
break;
case 1113198596:
surfaceObjectSeen = true;
++i;
propertyValue = this.getPoint4f (i);
propertyName = "ellipsoid";
i = this.eval.iToken;
sbCommand.append (" ellipsoid ").append (J.util.Escape.eP4 (propertyValue));
break;
case 135267841:
planeSeen = true;
propertyName = "plane";
propertyValue = this.eval.hklParameter (++i);
i = this.eval.iToken;
sbCommand.append (" plane ").append (J.util.Escape.eP4 (propertyValue));
break;
case 135182:
surfaceObjectSeen = true;
var lcaoType = this.parameterAsString (++i);
this.addShapeProperty (propertyList, "lcaoType", lcaoType);
sbCommand.append (" lcaocartoon ").append (J.util.Escape.eS (lcaoType));
switch (this.eval.getToken (++i).tok) {
case 10:
case 1048577:
propertyName = "lcaoCartoon";
bs = this.atomExpressionAt (i);
i = this.eval.iToken;
if (this.chk) continue;
var atomIndex = bs.nextSetBit (0);
if (atomIndex < 0) this.error (14);
sbCommand.append (" ({").appendI (atomIndex).append ("})");
modelIndex = this.viewer.getAtomModelIndex (atomIndex);
this.addShapeProperty (propertyList, "modelIndex", Integer.$valueOf (modelIndex));
var axes = [ new J.util.V3 (),  new J.util.V3 (), J.util.V3.newV (this.viewer.getAtomPoint3f (atomIndex)),  new J.util.V3 ()];
if (!lcaoType.equalsIgnoreCase ("s") && this.viewer.getHybridizationAndAxes (atomIndex, axes[0], axes[1], lcaoType) == null) return false;
propertyValue = axes;
break;
default:
this.error (14);
}
break;
case 1183762:
var moNumber = 2147483647;
var offset = 2147483647;
var isNegOffset = (this.tokAt (i + 1) == 269484192);
if (isNegOffset) i++;
var linearCombination = null;
switch (this.tokAt (++i)) {
case 0:
this.error (2);
break;
case 1073741914:
sbCommand.append ("mo [1] squared ");
this.addShapeProperty (propertyList, "squareLinear", Boolean.TRUE);
linearCombination = [1];
offset = moNumber = 0;
i++;
break;
case 1073741973:
case 1073742008:
offset = this.moOffset (i);
moNumber = 0;
i = this.eval.iToken;
sbCommand.append (" mo " + (isNegOffset ? "-" : "") + "HOMO ");
if (offset > 0) sbCommand.append ("+");
if (offset != 0) sbCommand.appendI (offset);
break;
case 2:
moNumber = this.intParameter (i);
sbCommand.append (" mo ").appendI (moNumber);
break;
default:
if (this.eval.isArrayParameter (i)) {
linearCombination = this.eval.floatParameterSet (i, 1, 2147483647);
i = this.eval.iToken;
}}
var squared = (this.tokAt (i + 1) == 1073742156);
if (squared) {
this.addShapeProperty (propertyList, "squareLinear", Boolean.TRUE);
sbCommand.append (" squared");
if (linearCombination == null) linearCombination =  Clazz.newFloatArray (0, 0);
} else if (this.tokAt (i + 1) == 135266320) {
++i;
var monteCarloCount = this.intParameter (++i);
var seed = (this.tokAt (i + 1) == 2 ? this.intParameter (++i) : (-System.currentTimeMillis ()) % 10000);
this.addShapeProperty (propertyList, "monteCarloCount", Integer.$valueOf (monteCarloCount));
this.addShapeProperty (propertyList, "randomSeed", Integer.$valueOf (seed));
sbCommand.append (" points ").appendI (monteCarloCount).appendC (' ').appendI (seed);
}this.setMoData (propertyList, moNumber, linearCombination, offset, isNegOffset, modelIndex, null);
surfaceObjectSeen = true;
continue;
case 1073742036:
propertyName = "nci";
sbCommand.append (" " + propertyName);
var tok = this.tokAt (i + 1);
var isPromolecular = (tok != 1229984263 && tok != 4 && tok != 1073742033);
propertyValue = Boolean.$valueOf (isPromolecular);
if (isPromolecular) surfaceObjectSeen = true;
break;
case 1073742016:
case 1073742022:
var isMep = (this.eval.theTok == 1073742016);
propertyName = (isMep ? "mep" : "mlp");
sbCommand.append (" " + propertyName);
var fname = null;
var calcType = -1;
surfaceObjectSeen = true;
if (this.tokAt (i + 1) == 2) {
calcType = this.intParameter (++i);
sbCommand.append (" " + calcType);
this.addShapeProperty (propertyList, "mepCalcType", Integer.$valueOf (calcType));
}if (this.tokAt (i + 1) == 4) {
fname = this.stringParameter (++i);
sbCommand.append (" /*file*/" + J.util.Escape.eS (fname));
} else if (this.tokAt (i + 1) == 1716520985) {
mepOrMlp = propertyName;
continue;
}if (!this.chk) try {
data = (fname == null && isMep ? this.viewer.getPartialCharges () : this.viewer.getAtomicPotentials (isMep, bsSelect, bsIgnore, fname));
} catch (e) {
if (Clazz.exceptionOf (e, Exception)) {
} else {
throw e;
}
}
if (!this.chk && data == null) this.error (32);
propertyValue = data;
break;
case 1313866247:
doCalcVolume = !this.chk;
sbCommand.append (" volume");
break;
case 1074790550:
this.setShapeId (iShape, ++i, idSeen);
isWild = (this.getShapeProperty (iShape, "ID") == null);
i = this.eval.iToken;
break;
case 1073741888:
if (this.tokAt (i + 1) == 603979967) {
isColorSchemeTranslucent = true;
i++;
}colorScheme = this.parameterAsString (++i).toLowerCase ();
if (colorScheme.equals ("sets")) {
sbCommand.append (" colorScheme \"sets\"");
} else if (this.eval.isColorParam (i)) {
colorScheme = this.eval.getColorRange (i);
i = this.eval.iToken;
}break;
case 1073741828:
propertyName = "addHydrogens";
propertyValue = Boolean.TRUE;
sbCommand.append (" addHydrogens");
break;
case 1073741836:
propertyName = "angstroms";
sbCommand.append (" angstroms");
break;
case 1073741838:
propertyName = "anisotropy";
propertyValue = this.getPoint3f (++i, false);
sbCommand.append (" anisotropy").append (J.util.Escape.eP (propertyValue));
i = this.eval.iToken;
break;
case 1073741842:
doCalcArea = !this.chk;
sbCommand.append (" area");
break;
case 1073741850:
case 1073742076:
surfaceObjectSeen = true;
if (isBicolor && !isPhased) {
sbCommand.append (" phase \"_orb\"");
this.addShapeProperty (propertyList, "phase", "_orb");
}var nlmZprs =  Clazz.newFloatArray (7, 0);
nlmZprs[0] = this.intParameter (++i);
nlmZprs[1] = this.intParameter (++i);
nlmZprs[2] = this.intParameter (++i);
nlmZprs[3] = (this.isFloatParameter (i + 1) ? this.floatParameter (++i) : 6);
sbCommand.append (" atomicOrbital ").appendI (Clazz.floatToInt (nlmZprs[0])).append (" ").appendI (Clazz.floatToInt (nlmZprs[1])).append (" ").appendI (Clazz.floatToInt (nlmZprs[2])).append (" ").appendF (nlmZprs[3]);
if (this.tokAt (i + 1) == 135266320) {
i += 2;
nlmZprs[4] = this.intParameter (i);
nlmZprs[5] = (this.tokAt (i + 1) == 3 ? this.floatParameter (++i) : 0);
nlmZprs[6] = (this.tokAt (i + 1) == 2 ? this.intParameter (++i) : (-System.currentTimeMillis ()) % 10000);
sbCommand.append (" points ").appendI (Clazz.floatToInt (nlmZprs[4])).appendC (' ').appendF (nlmZprs[5]).appendC (' ').appendI (Clazz.floatToInt (nlmZprs[6]));
}propertyName = "hydrogenOrbital";
propertyValue = nlmZprs;
break;
case 1073741866:
sbCommand.append (" binary");
continue;
case 1073741868:
sbCommand.append (" blockData");
propertyName = "blockData";
propertyValue = Boolean.TRUE;
break;
case 1074790451:
case 554176565:
haveSlab = true;
propertyName = this.eval.theToken.value;
propertyValue = this.getCapSlabObject (i, false);
i = this.eval.iToken;
break;
case 1073741876:
if (!isIsosurface) this.invArg ();
isCavity = true;
if (this.chk) continue;
var cavityRadius = (this.isFloatParameter (i + 1) ? this.floatParameter (++i) : 1.2);
var envelopeRadius = (this.isFloatParameter (i + 1) ? this.floatParameter (++i) : 10);
if (envelopeRadius > 10) this.eval.integerOutOfRange (0, 10);
sbCommand.append (" cavity ").appendF (cavityRadius).append (" ").appendF (envelopeRadius);
this.addShapeProperty (propertyList, "envelopeRadius", Float.$valueOf (envelopeRadius));
this.addShapeProperty (propertyList, "cavityRadius", Float.$valueOf (cavityRadius));
propertyName = "cavity";
break;
case 1073741896:
case 1073741900:
propertyName = "contour";
sbCommand.append (" contour");
switch (this.tokAt (i + 1)) {
case 1073741920:
propertyValue = this.eval.floatParameterSet (i + 2, 1, 2147483647);
sbCommand.append (" discrete ").append (J.util.Escape.eAF (propertyValue));
i = this.eval.iToken;
break;
case 1073741981:
pt = this.getPoint3f (i + 2, false);
if (pt.z <= 0 || pt.y < pt.x) this.invArg ();
if (pt.z == Clazz.floatToInt (pt.z) && pt.z > (pt.y - pt.x)) pt.z = (pt.y - pt.x) / pt.z;
propertyValue = pt;
i = this.eval.iToken;
sbCommand.append (" increment ").append (J.util.Escape.eP (pt));
break;
default:
propertyValue = Integer.$valueOf (this.tokAt (i + 1) == 2 ? this.intParameter (++i) : 0);
sbCommand.append (" ").appendO (propertyValue);
}
break;
case 3:
case 2:
case 269484193:
case 1073741910:
sbCommand.append (" cutoff ");
if (this.eval.theTok == 1073741910) i++;
if (this.tokAt (i) == 269484193) {
propertyName = "cutoffPositive";
propertyValue = Float.$valueOf (cutoff = this.floatParameter (++i));
sbCommand.append ("+").appendO (propertyValue);
} else if (this.isFloatParameter (i)) {
propertyName = "cutoff";
propertyValue = Float.$valueOf (cutoff = this.floatParameter (i));
sbCommand.appendO (propertyValue);
} else {
propertyName = "cutoffRange";
propertyValue = this.eval.floatParameterSet (i, 2, 2);
this.addShapeProperty (propertyList, "cutoff", Float.$valueOf (0));
sbCommand.append (J.util.Escape.eAF (propertyValue));
i = this.eval.iToken;
}break;
case 1073741928:
propertyName = "downsample";
propertyValue = Integer.$valueOf (this.intParameter (++i));
sbCommand.append (" downsample ").appendO (propertyValue);
break;
case 1073741930:
propertyName = "eccentricity";
propertyValue = this.getPoint4f (++i);
sbCommand.append (" eccentricity ").append (J.util.Escape.eP4 (propertyValue));
i = this.eval.iToken;
break;
case 1074790508:
sbCommand.append (" ed");
this.setMoData (propertyList, -1, null, 0, false, modelIndex, null);
surfaceObjectSeen = true;
continue;
case 536870916:
case 1073742041:
sbCommand.append (" ").appendO (this.eval.theToken.value);
propertyName = "debug";
propertyValue = (this.eval.theTok == 536870916 ? Boolean.TRUE : Boolean.FALSE);
break;
case 1060869:
sbCommand.append (" fixed");
propertyName = "fixed";
propertyValue = Boolean.TRUE;
break;
case 1073741962:
sbCommand.append (" fullPlane");
propertyName = "fullPlane";
propertyValue = Boolean.TRUE;
break;
case 1073741966:
case 1073741968:
var isFxyz = (this.eval.theTok == 1073741968);
propertyName = "" + this.eval.theToken.value;
var vxy =  new J.util.JmolList ();
propertyValue = vxy;
isFxy = surfaceObjectSeen = true;
sbCommand.append (" ").append (propertyName);
var name = this.parameterAsString (++i);
if (name.equals ("=")) {
sbCommand.append (" =");
name = this.parameterAsString (++i);
sbCommand.append (" ").append (J.util.Escape.eS (name));
vxy.addLast (name);
if (!this.chk) this.addShapeProperty (propertyList, "func", this.createFunction ("__iso__", "x,y,z", name));
break;
}var dName = J.util.Parser.getQuotedAttribute (this.fullCommand, "# DATA" + (isFxy ? "2" : ""));
if (dName == null) dName = "inline";
 else name = dName;
var isXYZ = (name.indexOf ("data2d_") == 0);
var isXYZV = (name.indexOf ("data3d_") == 0);
isInline = name.equals ("inline");
sbCommand.append (" inline");
vxy.addLast (name);
var pt3 = this.getPoint3f (++i, false);
sbCommand.append (" ").append (J.util.Escape.eP (pt3));
vxy.addLast (pt3);
var pt4;
ptX = ++this.eval.iToken;
vxy.addLast (pt4 = this.getPoint4f (ptX));
sbCommand.append (" ").append (J.util.Escape.eP4 (pt4));
nX = Clazz.floatToInt (pt4.x);
ptY = ++this.eval.iToken;
vxy.addLast (pt4 = this.getPoint4f (ptY));
sbCommand.append (" ").append (J.util.Escape.eP4 (pt4));
nY = Clazz.floatToInt (pt4.x);
vxy.addLast (pt4 = this.getPoint4f (++this.eval.iToken));
sbCommand.append (" ").append (J.util.Escape.eP4 (pt4));
nZ = Clazz.floatToInt (pt4.x);
if (nX == 0 || nY == 0 || nZ == 0) this.invArg ();
if (!this.chk) {
var fdata = null;
var xyzdata = null;
if (isFxyz) {
if (isInline) {
nX = Math.abs (nX);
nY = Math.abs (nY);
nZ = Math.abs (nZ);
xyzdata = this.floatArraySetXYZ (++this.eval.iToken, nX, nY, nZ);
} else if (isXYZV) {
xyzdata = this.viewer.getDataFloat3D (name);
} else {
xyzdata = this.viewer.functionXYZ (name, nX, nY, nZ);
}nX = Math.abs (nX);
nY = Math.abs (nY);
nZ = Math.abs (nZ);
if (xyzdata == null) {
this.eval.iToken = ptX;
this.eval.errorStr (53, "xyzdata is null.");
}if (xyzdata.length != nX || xyzdata[0].length != nY || xyzdata[0][0].length != nZ) {
this.eval.iToken = ptX;
this.eval.errorStr (53, "xyzdata[" + xyzdata.length + "][" + xyzdata[0].length + "][" + xyzdata[0][0].length + "] is not of size [" + nX + "][" + nY + "][" + nZ + "]");
}vxy.addLast (xyzdata);
sbCommand.append (" ").append (J.util.Escape.e (xyzdata));
} else {
if (isInline) {
nX = Math.abs (nX);
nY = Math.abs (nY);
fdata = this.floatArraySet (++this.eval.iToken, nX, nY);
} else if (isXYZ) {
fdata = this.viewer.getDataFloat2D (name);
nX = (fdata == null ? 0 : fdata.length);
nY = 3;
} else {
fdata = this.viewer.functionXY (name, nX, nY);
nX = Math.abs (nX);
nY = Math.abs (nY);
}if (fdata == null) {
this.eval.iToken = ptX;
this.eval.errorStr (53, "fdata is null.");
}if (fdata.length != nX && !isXYZ) {
this.eval.iToken = ptX;
this.eval.errorStr (53, "fdata length is not correct: " + fdata.length + " " + nX + ".");
}for (var j = 0; j < nX; j++) {
if (fdata[j] == null) {
this.eval.iToken = ptY;
this.eval.errorStr (53, "fdata[" + j + "] is null.");
}if (fdata[j].length != nY) {
this.eval.iToken = ptY;
this.eval.errorStr (53, "fdata[" + j + "] is not the right length: " + fdata[j].length + " " + nY + ".");
}}
vxy.addLast (fdata);
sbCommand.append (" ").append (J.util.Escape.e (fdata));
}}i = this.eval.iToken;
break;
case 1073741970:
propertyName = "gridPoints";
sbCommand.append (" gridPoints");
break;
case 1073741976:
propertyName = "ignore";
propertyValue = bsIgnore = this.atomExpressionAt (++i);
sbCommand.append (" ignore ").append (J.util.Escape.eBS (bsIgnore));
i = this.eval.iToken;
break;
case 1073741984:
propertyName = "insideOut";
sbCommand.append (" insideout");
break;
case 1073741988:
case 1073741986:
case 1073742100:
sbCommand.append (" ").appendO (this.eval.theToken.value);
propertyName = "pocket";
propertyValue = (this.eval.theTok == 1073742100 ? Boolean.TRUE : Boolean.FALSE);
break;
case 1073742002:
propertyName = "lobe";
propertyValue = this.getPoint4f (++i);
i = this.eval.iToken;
sbCommand.append (" lobe ").append (J.util.Escape.eP4 (propertyValue));
surfaceObjectSeen = true;
break;
case 1073742004:
case 1073742006:
propertyName = "lp";
propertyValue = this.getPoint4f (++i);
i = this.eval.iToken;
sbCommand.append (" lp ").append (J.util.Escape.eP4 (propertyValue));
surfaceObjectSeen = true;
break;
case 1052700:
if (isMapped || this.eval.slen == i + 1) this.invArg ();
isMapped = true;
if ((isCavity || haveRadius || haveIntersection) && !surfaceObjectSeen) {
surfaceObjectSeen = true;
this.addShapeProperty (propertyList, "bsSolvent", (haveRadius || haveIntersection ?  new J.util.BS () : this.eval.lookupIdentifierValue ("solvent")));
this.addShapeProperty (propertyList, "sasurface", Float.$valueOf (0));
}if (sbCommand.length () == 0) {
plane = this.getShapeProperty (24, "plane");
if (plane == null) {
if (this.getShapeProperty (24, "contours") != null) {
this.addShapeProperty (propertyList, "nocontour", null);
}} else {
this.addShapeProperty (propertyList, "plane", plane);
sbCommand.append ("plane ").append (J.util.Escape.eP4 (plane));
planeSeen = true;
plane = null;
}} else if (!surfaceObjectSeen && !planeSeen) {
this.invArg ();
}sbCommand.append ("; isosurface map");
this.addShapeProperty (propertyList, "map", (surfaceObjectSeen ? Boolean.TRUE : Boolean.FALSE));
break;
case 1073742014:
propertyName = "maxset";
propertyValue = Integer.$valueOf (this.intParameter (++i));
sbCommand.append (" maxSet ").appendO (propertyValue);
break;
case 1073742020:
propertyName = "minset";
propertyValue = Integer.$valueOf (this.intParameter (++i));
sbCommand.append (" minSet ").appendO (propertyValue);
break;
case 1073742112:
surfaceObjectSeen = true;
propertyName = "rad";
propertyValue = this.getPoint4f (++i);
i = this.eval.iToken;
sbCommand.append (" radical ").append (J.util.Escape.eP4 (propertyValue));
break;
case 1073742028:
propertyName = "fixed";
propertyValue = Boolean.FALSE;
sbCommand.append (" modelBased");
break;
case 1073742029:
case 1073742136:
case 1613758488:
onlyOneModel = this.eval.theToken.value;
var radius;
if (this.eval.theTok == 1073742029) {
propertyName = "molecular";
sbCommand.append (" molecular");
radius = (this.isFloatParameter (i + 1) ? this.floatParameter (++i) : 1.4);
} else {
this.addShapeProperty (propertyList, "bsSolvent", this.eval.lookupIdentifierValue ("solvent"));
propertyName = (this.eval.theTok == 1073742136 ? "sasurface" : "solvent");
sbCommand.append (" ").appendO (this.eval.theToken.value);
radius = (this.isFloatParameter (i + 1) ? this.floatParameter (++i) : this.viewer.getFloat (570425394));
}sbCommand.append (" ").appendF (radius);
propertyValue = Float.$valueOf (radius);
if (this.tokAt (i + 1) == 1073741961) {
this.addShapeProperty (propertyList, "doFullMolecular", null);
sbCommand.append (" full");
i++;
}surfaceObjectSeen = true;
break;
case 1073742033:
this.addShapeProperty (propertyList, "fileType", "Mrc");
sbCommand.append (" mrc");
continue;
case 1073742064:
case 1073742062:
this.addShapeProperty (propertyList, "fileType", "Obj");
sbCommand.append (" obj");
continue;
case 1073742034:
this.addShapeProperty (propertyList, "fileType", "Msms");
sbCommand.append (" msms");
continue;
case 1073742094:
if (surfaceObjectSeen) this.invArg ();
propertyName = "phase";
isPhased = true;
propertyValue = (this.tokAt (i + 1) == 4 ? this.stringParameter (++i) : "_orb");
sbCommand.append (" phase ").append (J.util.Escape.eS (propertyValue));
break;
case 1073742104:
case 1073742122:
propertyName = "resolution";
propertyValue = Float.$valueOf (this.floatParameter (++i));
sbCommand.append (" resolution ").appendO (propertyValue);
break;
case 1073742124:
propertyName = "reverseColor";
propertyValue = Boolean.TRUE;
sbCommand.append (" reversecolor");
break;
case 1073742146:
propertyName = "sigma";
propertyValue = Float.$valueOf (sigma = this.floatParameter (++i));
sbCommand.append (" sigma ").appendO (propertyValue);
break;
case 1113198597:
propertyName = "geodesic";
propertyValue = Float.$valueOf (this.floatParameter (++i));
sbCommand.append (" geosurface ").appendO (propertyValue);
surfaceObjectSeen = true;
break;
case 1073742154:
propertyName = "sphere";
propertyValue = Float.$valueOf (this.floatParameter (++i));
sbCommand.append (" sphere ").appendO (propertyValue);
surfaceObjectSeen = true;
break;
case 1073742156:
propertyName = "squareData";
propertyValue = Boolean.TRUE;
sbCommand.append (" squared");
break;
case 1073741983:
propertyName = (!surfaceObjectSeen && !planeSeen && !isMapped ? "readFile" : "mapColor");
str = this.stringParameter (++i);
if (str == null) this.invArg ();
if (isPmesh) str = J.util.TextFormat.replaceAllCharacter (str, "{,}|", ' ');
if (this.eval.logMessages) J.util.Logger.debug ("pmesh inline data:\n" + str);
propertyValue = (this.chk ? null : str);
this.addShapeProperty (propertyList, "fileName", "");
sbCommand.append (" INLINE ").append (J.util.Escape.eS (str));
surfaceObjectSeen = true;
break;
case 4:
var firstPass = (!surfaceObjectSeen && !planeSeen);
propertyName = (firstPass && !isMapped ? "readFile" : "mapColor");
var filename = this.parameterAsString (i);
if (filename.startsWith ("=") && filename.length > 1) {
var info = this.viewer.setLoadFormat (filename, '_', false);
filename = info[0];
var strCutoff = (!firstPass || !Float.isNaN (cutoff) ? null : info[1]);
if (strCutoff != null && !this.chk) {
cutoff = J.script.SV.fValue (J.script.SV.getVariable (this.viewer.evaluateExpression (strCutoff)));
if (cutoff > 0) {
if (!Float.isNaN (sigma)) {
cutoff *= sigma;
sigma = NaN;
this.addShapeProperty (propertyList, "sigma", Float.$valueOf (sigma));
}this.addShapeProperty (propertyList, "cutoff", Float.$valueOf (cutoff));
sbCommand.append (" cutoff ").appendF (cutoff);
}}if (ptWithin == 0) {
onlyOneModel = "=xxxx";
if (modelIndex < 0) modelIndex = this.viewer.getCurrentModelIndex ();
bs = this.viewer.getModelUndeletedAtomsBitSet (modelIndex);
this.getWithinDistanceVector (propertyList, 2.0, null, bs, false);
sbCommand.append (" within 2.0 ").append (J.util.Escape.eBS (bs));
}if (firstPass) defaultMesh = true;
}if (firstPass && this.viewer.getParameter ("_fileType").equals ("Pdb") && Float.isNaN (sigma) && Float.isNaN (cutoff)) {
this.addShapeProperty (propertyList, "sigma", Float.$valueOf (-1));
sbCommand.append (" sigma -1.0");
}if (filename.length == 0) {
if (modelIndex < 0) modelIndex = this.viewer.getCurrentModelIndex ();
filename = this.eval.getFullPathName ();
propertyValue = this.viewer.getModelAuxiliaryInfoValue (modelIndex, "jmolSurfaceInfo");
}var fileIndex = -1;
if (propertyValue == null && this.tokAt (i + 1) == 2) this.addShapeProperty (propertyList, "fileIndex", Integer.$valueOf (fileIndex = this.intParameter (++i)));
var stype = (this.tokAt (i + 1) == 4 ? this.stringParameter (++i) : null);
surfaceObjectSeen = true;
if (this.chk) {
break;
}var fullPathNameOrError;
var localName = null;
if (propertyValue == null) {
if (this.fullCommand.indexOf ("# FILE" + nFiles + "=") >= 0) {
filename = J.util.Parser.getQuotedAttribute (this.fullCommand, "# FILE" + nFiles);
if (this.tokAt (i + 1) == 1073741848) i += 2;
} else if (this.tokAt (i + 1) == 1073741848) {
localName = this.viewer.getFilePath (this.stringParameter (this.eval.iToken = (i = i + 2)), false);
fullPathNameOrError = this.viewer.getFullPathNameOrError (localName);
localName = fullPathNameOrError[0];
if (this.viewer.getPathForAllFiles () !== "") {
filename = localName;
localName = null;
} else {
this.addShapeProperty (propertyList, "localName", localName);
this.viewer.setPrivateKeyForShape (iShape);
}}}if (!filename.startsWith ("cache://") && stype == null) {
fullPathNameOrError = this.viewer.getFullPathNameOrError (filename);
filename = fullPathNameOrError[0];
if (fullPathNameOrError[1] != null) this.eval.errorStr (17, filename + ":" + fullPathNameOrError[1]);
}J.util.Logger.info ("reading isosurface data from " + filename);
if (stype != null) {
propertyValue = this.viewer.cacheGet (filename);
this.addShapeProperty (propertyList, "calculationType", stype);
}if (propertyValue == null) {
this.addShapeProperty (propertyList, "fileName", filename);
if (localName != null) filename = localName;
if (fileIndex >= 0) sbCommand.append (" ").appendI (fileIndex);
}sbCommand.append (" /*file*/").append (J.util.Escape.eS (filename));
if (stype != null) sbCommand.append (" ").append (J.util.Escape.eS (stype));
break;
case 4106:
propertyName = "connections";
switch (this.tokAt (++i)) {
case 10:
case 1048577:
propertyValue = [this.atomExpressionAt (i).nextSetBit (0)];
break;
default:
propertyValue = [Clazz.floatToInt (this.eval.floatParameterSet (i, 1, 1)[0])];
break;
}
i = this.eval.iToken;
break;
case 1095761923:
propertyName = "atomIndex";
propertyValue = Integer.$valueOf (this.intParameter (++i));
break;
case 1073741999:
propertyName = "link";
sbCommand.append (" link");
break;
case 1073741994:
if (iShape != 24) this.invArg ();
pt = this.getPoint3f (this.eval.iToken + 1, false);
i = this.eval.iToken;
if (pt.x <= 0 || pt.y <= 0 || pt.z <= 0) break;
pt.x = Clazz.floatToInt (pt.x);
pt.y = Clazz.floatToInt (pt.y);
pt.z = Clazz.floatToInt (pt.z);
sbCommand.append (" lattice ").append (J.util.Escape.eP (pt));
if (isMapped) {
propertyName = "mapLattice";
propertyValue = pt;
} else {
lattice = pt;
}break;
default:
if (this.eval.theTok == 1073741824) {
propertyName = "thisID";
propertyValue = str;
}if (!this.eval.setMeshDisplayProperty (iShape, 0, this.eval.theTok)) {
if (J.script.T.tokAttr (this.eval.theTok, 1073741824) && !idSeen) {
this.setShapeId (iShape, i, idSeen);
i = this.eval.iToken;
break;
}this.invArg ();
}if (iptDisplayProperty == 0) iptDisplayProperty = i;
i = this.eval.slen - 1;
break;
}
idSeen = (this.eval.theTok != 12291);
if (isWild && surfaceObjectSeen) this.invArg ();
if (propertyName != null) this.addShapeProperty (propertyList, propertyName, propertyValue);
}
if (!this.chk) {
if ((isCavity || haveRadius) && !surfaceObjectSeen) {
surfaceObjectSeen = true;
this.addShapeProperty (propertyList, "bsSolvent", (haveRadius ?  new J.util.BS () : this.eval.lookupIdentifierValue ("solvent")));
this.addShapeProperty (propertyList, "sasurface", Float.$valueOf (0));
}if (planeSeen && !surfaceObjectSeen && !isMapped) {
this.addShapeProperty (propertyList, "nomap", Float.$valueOf (0));
surfaceObjectSeen = true;
}if (thisSetNumber >= -1) this.addShapeProperty (propertyList, "getSurfaceSets", Integer.$valueOf (thisSetNumber - 1));
if (discreteColixes != null) {
this.addShapeProperty (propertyList, "colorDiscrete", discreteColixes);
} else if ("sets".equals (colorScheme)) {
this.addShapeProperty (propertyList, "setColorScheme", null);
} else if (colorScheme != null) {
var ce = this.viewer.getColorEncoder (colorScheme);
if (ce != null) {
ce.isTranslucent = isColorSchemeTranslucent;
ce.hi = 3.4028235E38;
this.addShapeProperty (propertyList, "remapColor", ce);
}}if (surfaceObjectSeen && !isLcaoCartoon && sbCommand.indexOf (";") != 0) {
propertyList.add (0, ["newObject", null]);
var needSelect = (bsSelect == null);
if (needSelect) bsSelect = J.util.BSUtil.copy (this.viewer.getSelectionSet (false));
if (modelIndex < 0) modelIndex = this.viewer.getCurrentModelIndex ();
bsSelect.and (this.viewer.getModelUndeletedAtomsBitSet (modelIndex));
if (onlyOneModel != null) {
var bsModels = this.viewer.getModelBitSet (bsSelect, false);
if (bsModels.cardinality () != 1) this.eval.errorStr (30, "ISOSURFACE " + onlyOneModel);
if (needSelect) {
propertyList.add (0, ["select", bsSelect]);
if (sbCommand.indexOf ("; isosurface map") == 0) {
sbCommand =  new J.util.SB ().append ("; isosurface map select ").append (J.util.Escape.eBS (bsSelect)).append (sbCommand.substring (16));
}}}}if (haveIntersection && !haveSlab) {
if (!surfaceObjectSeen) this.addShapeProperty (propertyList, "sasurface", Float.$valueOf (0));
if (!isMapped) {
this.addShapeProperty (propertyList, "map", Boolean.TRUE);
this.addShapeProperty (propertyList, "select", bs);
this.addShapeProperty (propertyList, "sasurface", Float.$valueOf (0));
}this.addShapeProperty (propertyList, "slab", this.getCapSlabObject (-100, false));
}var timeMsg = (surfaceObjectSeen && this.viewer.getBoolean (603979934));
if (timeMsg) J.util.Logger.startTimer ("isosurface");
this.setShapeProperty (iShape, "setProperties", propertyList);
if (timeMsg) this.eval.showString (J.util.Logger.getTimerMsg ("isosurface", 0));
if (defaultMesh) {
this.setShapeProperty (iShape, "token", Integer.$valueOf (1073742018));
this.setShapeProperty (iShape, "token", Integer.$valueOf (1073742046));
this.setShapeProperty (iShape, "token", Integer.$valueOf (1073741960));
sbCommand.append (" mesh nofill frontOnly");
}}if (lattice != null) this.setShapeProperty (24, "lattice", lattice);
if (iptDisplayProperty > 0) {
if (!this.eval.setMeshDisplayProperty (iShape, iptDisplayProperty, 0)) this.invArg ();
}if (this.chk) return false;
var area = null;
var volume = null;
if (doCalcArea) {
area = this.getShapeProperty (iShape, "area");
if (Clazz.instanceOf (area, Float)) this.viewer.setFloatProperty ("isosurfaceArea", (area).floatValue ());
 else this.viewer.setUserVariable ("isosurfaceArea", J.script.SV.getVariableAD (area));
}if (doCalcVolume) {
volume = (doCalcVolume ? this.getShapeProperty (iShape, "volume") : null);
if (Clazz.instanceOf (volume, Float)) this.viewer.setFloatProperty ("isosurfaceVolume", (volume).floatValue ());
 else this.viewer.setUserVariable ("isosurfaceVolume", J.script.SV.getVariableAD (volume));
}if (!isLcaoCartoon) {
var s = null;
if (isMapped && !surfaceObjectSeen) {
this.setShapeProperty (iShape, "finalize", sbCommand.toString ());
} else if (surfaceObjectSeen) {
cmd = sbCommand.toString ();
this.setShapeProperty (iShape, "finalize", (cmd.indexOf ("; isosurface map") == 0 ? "" : " select " + J.util.Escape.eBS (bsSelect) + " ") + cmd);
s = this.getShapeProperty (iShape, "ID");
if (s != null && !this.eval.tQuiet) {
cutoff = (this.getShapeProperty (iShape, "cutoff")).floatValue ();
if (Float.isNaN (cutoff) && !Float.isNaN (sigma)) {
J.util.Logger.error ("sigma not supported");
}s += " created";
if (isIsosurface) s += " with cutoff=" + cutoff;
var minMax = this.getShapeProperty (iShape, "minMaxInfo");
if (minMax[0] != 3.4028235E38) s += " min=" + minMax[0] + " max=" + minMax[1];
s += "; " + J.viewer.JC.shapeClassBases[iShape].toLowerCase () + " count: " + this.getShapeProperty (iShape, "count");
s += this.eval.getIsosurfaceDataRange (iShape, "\n");
}}var sarea;
var svol;
if (doCalcArea || doCalcVolume) {
sarea = (doCalcArea ? "isosurfaceArea = " + (Clazz.instanceOf (area, Float) ? "" + area : J.util.Escape.eAD (area)) : null);
svol = (doCalcVolume ? "isosurfaceVolume = " + (Clazz.instanceOf (volume, Float) ? "" + volume : J.util.Escape.eAD (volume)) : null);
if (s == null) {
if (doCalcArea) this.eval.showString (sarea);
if (doCalcVolume) this.eval.showString (svol);
} else {
if (doCalcArea) s += "\n" + sarea;
if (doCalcVolume) s += "\n" + svol;
}}if (s != null) this.eval.showString (s);
}if (translucency != null) this.setShapeProperty (iShape, "translucency", translucency);
this.setShapeProperty (iShape, "clear", null);
if (toCache) this.setShapeProperty (iShape, "cache", null);
return true;
}, $fz.isPrivate = true, $fz), "~N");
$_M(c$, "lcaoCartoon", 
($fz = function () {
this.sm.loadShape (26);
if (this.tokAt (1) == 1073742001 && this.listIsosurface (26)) return false;
this.setShapeProperty (26, "init", this.fullCommand);
if (this.eval.slen == 1) {
this.setShapeProperty (26, "lcaoID", null);
return false;
}var idSeen = false;
var translucency = null;
for (var i = 1; i < this.eval.slen; i++) {
var propertyName = null;
var propertyValue = null;
switch (this.eval.getToken (i).tok) {
case 1074790451:
case 554176565:
propertyName = this.eval.theToken.value;
if (this.tokAt (i + 1) == 1048588) this.eval.iToken = i + 1;
propertyValue = this.getCapSlabObject (i, true);
i = this.eval.iToken;
break;
case 12289:
this.isosurface (26);
return false;
case 528432:
var degx = 0;
var degy = 0;
var degz = 0;
switch (this.eval.getToken (++i).tok) {
case 1112541205:
degx = this.floatParameter (++i) * 0.017453292;
break;
case 1112541206:
degy = this.floatParameter (++i) * 0.017453292;
break;
case 1112541207:
degz = this.floatParameter (++i) * 0.017453292;
break;
default:
this.invArg ();
}
propertyName = "rotationAxis";
propertyValue = J.util.V3.new3 (degx, degy, degz);
break;
case 1048589:
case 1610625028:
case 3145768:
propertyName = "on";
break;
case 1048588:
case 12294:
case 3145770:
propertyName = "off";
break;
case 12291:
propertyName = "delete";
break;
case 10:
case 1048577:
propertyName = "select";
propertyValue = this.atomExpressionAt (i);
i = this.eval.iToken;
break;
case 1766856708:
translucency = this.setColorOptions (null, i + 1, 26, -2);
if (translucency != null) this.setShapeProperty (26, "settranslucency", translucency);
i = this.eval.iToken;
idSeen = true;
continue;
case 603979967:
case 1073742074:
this.eval.setMeshDisplayProperty (26, i, this.eval.theTok);
i = this.eval.iToken;
idSeen = true;
continue;
case 1113200651:
case 4:
propertyValue = this.parameterAsString (i).toLowerCase ();
if (propertyValue.equals ("spacefill")) propertyValue = "cpk";
propertyName = "create";
if (this.eval.optParameterAsString (i + 1).equalsIgnoreCase ("molecular")) {
i++;
propertyName = "molecular";
}break;
case 135280132:
if (this.tokAt (i + 1) == 10 || this.tokAt (i + 1) == 1048577) {
propertyName = "select";
propertyValue = this.atomExpressionAt (i + 1);
i = this.eval.iToken;
} else {
propertyName = "selectType";
propertyValue = this.parameterAsString (++i);
if (propertyValue.equals ("spacefill")) propertyValue = "cpk";
}break;
case 1073742138:
propertyName = "scale";
propertyValue = Float.$valueOf (this.floatParameter (++i));
break;
case 1073742004:
case 1073742006:
propertyName = "lonePair";
break;
case 1073742112:
case 1073742111:
propertyName = "radical";
break;
case 1073742029:
propertyName = "molecular";
break;
case 1073741904:
propertyValue = this.parameterAsString (++i);
propertyName = "create";
if (this.eval.optParameterAsString (i + 1).equalsIgnoreCase ("molecular")) {
i++;
propertyName = "molecular";
}break;
case 1074790550:
propertyValue = this.eval.getShapeNameParameter (++i);
i = this.eval.iToken;
if (idSeen) this.invArg ();
propertyName = "lcaoID";
break;
default:
if (this.eval.theTok == 269484209 || J.script.T.tokAttr (this.eval.theTok, 1073741824)) {
if (this.eval.theTok != 269484209) propertyValue = this.parameterAsString (i);
if (idSeen) this.invArg ();
propertyName = "lcaoID";
break;
}break;
}
if (this.eval.theTok != 12291) idSeen = true;
if (propertyName == null) this.invArg ();
this.setShapeProperty (26, propertyName, propertyValue);
}
this.setShapeProperty (26, "clear", null);
return true;
}, $fz.isPrivate = true, $fz));
$_M(c$, "getCapSlabObject", 
($fz = function (i, isLcaoCartoon) {
if (i < 0) {
return J.util.MeshSurface.getSlabWithinRange (i, 0);
}var data = null;
var tok0 = this.tokAt (i);
var isSlab = (tok0 == 554176565);
var tok = this.tokAt (i + 1);
var plane = null;
var pts = null;
var d;
var d2;
var bs = null;
var slabColix = null;
var slabMeshType = null;
if (tok == 603979967) {
var slabTranslucency = (this.isFloatParameter (++i + 1) ? this.floatParameter (++i) : 0.5);
if (this.eval.isColorParam (i + 1)) {
slabColix = Short.$valueOf (J.util.C.getColixTranslucent3 (J.util.C.getColix (this.eval.getArgbParam (i + 1)), slabTranslucency != 0, slabTranslucency));
i = this.eval.iToken;
} else {
slabColix = Short.$valueOf (J.util.C.getColixTranslucent3 (1, slabTranslucency != 0, slabTranslucency));
}switch (tok = this.tokAt (i + 1)) {
case 1073742018:
case 1073741938:
slabMeshType = Integer.$valueOf (tok);
tok = this.tokAt (++i + 1);
break;
default:
slabMeshType = Integer.$valueOf (1073741938);
break;
}
}switch (tok) {
case 10:
case 1048577:
data = this.atomExpressionAt (i + 1);
tok = 3;
this.eval.iToken++;
break;
case 1048588:
this.eval.iToken = i + 1;
return Integer.$valueOf (-2147483648);
case 1048587:
this.eval.iToken = i + 1;
break;
case 1048583:
i++;
data = [Float.$valueOf (1), this.parameterAsString (++i)];
tok = 1073742018;
break;
case 135266324:
i++;
if (this.tokAt (++i) == 1073742114) {
d = this.floatParameter (++i);
d2 = this.floatParameter (++i);
data = [Float.$valueOf (d), Float.$valueOf (d2)];
tok = 1073742114;
} else if (this.isFloatParameter (i)) {
d = this.floatParameter (i);
if (this.eval.isCenterParameter (++i)) {
var pt = this.centerParameter (i);
if (this.chk || !(Clazz.instanceOf (this.eval.expressionResult, J.util.BS))) {
pts = [pt];
} else {
var atoms = this.viewer.modelSet.atoms;
bs = this.eval.expressionResult;
pts =  new Array (bs.cardinality ());
for (var k = 0, j = bs.nextSetBit (0); j >= 0; j = bs.nextSetBit (j + 1), k++) pts[k] = atoms[j];

}} else {
pts = this.eval.getPointArray (i, -1);
}if (pts.length == 0) {
this.eval.iToken = i;
this.invArg ();
}data = [Float.$valueOf (d), pts, bs];
} else {
data = this.eval.getPointArray (i, 4);
tok = 1679429641;
}break;
case 1679429641:
this.eval.iToken = i + 1;
data = J.util.BoxInfo.getCriticalPoints (this.viewer.getBoundBoxVertices (), null);
break;
case 1073741872:
case 1614417948:
this.eval.iToken = i + 1;
var unitCell = this.viewer.getCurrentUnitCell ();
if (unitCell == null) {
if (tok == 1614417948) this.invArg ();
} else {
pts = J.util.BoxInfo.getCriticalPoints (unitCell.getUnitCellVertices (), unitCell.getCartesianOffset ());
var iType = Clazz.floatToInt (unitCell.getUnitCellInfoType (6));
var v1 = null;
var v2 = null;
switch (iType) {
case 3:
break;
case 1:
v2 = J.util.V3.newVsub (pts[2], pts[0]);
v2.scale (1000);
case 2:
v1 = J.util.V3.newVsub (pts[1], pts[0]);
v1.scale (1000);
pts[0].sub (v1);
pts[1].scale (2000);
if (iType == 1) {
pts[0].sub (v2);
pts[2].scale (2000);
}break;
}
data = pts;
}break;
default:
if (!isLcaoCartoon && isSlab && this.isFloatParameter (i + 1)) {
d = this.floatParameter (++i);
if (!this.isFloatParameter (i + 1)) return Integer.$valueOf (Clazz.floatToInt (d));
d2 = this.floatParameter (++i);
data = [Float.$valueOf (d), Float.$valueOf (d2)];
tok = 1073742114;
break;
}plane = this.eval.planeParameter (++i);
var off = (this.isFloatParameter (this.eval.iToken + 1) ? this.floatParameter (++this.eval.iToken) : NaN);
if (!Float.isNaN (off)) plane.w -= off;
data = plane;
tok = 135266319;
}
var colorData = (slabMeshType == null ? null : [slabMeshType, slabColix]);
return J.util.MeshSurface.getSlabObject (tok, data, !isSlab, colorData);
}, $fz.isPrivate = true, $fz), "~N,~B");
$_M(c$, "mo", 
($fz = function (isInitOnly) {
var offset = 2147483647;
var isNegOffset = false;
var bsModels = this.viewer.getVisibleFramesBitSet ();
var propertyList =  new J.util.JmolList ();
var i0 = 1;
if (this.tokAt (1) == 1095766028 || this.tokAt (1) == 4115) {
i0 = this.eval.modelNumberParameter (2);
if (i0 < 0) this.invArg ();
bsModels.clearAll ();
bsModels.set (i0);
i0 = 3;
}for (var iModel = bsModels.nextSetBit (0); iModel >= 0; iModel = bsModels.nextSetBit (iModel + 1)) {
this.sm.loadShape (27);
var i = i0;
if (this.tokAt (i) == 1073742001 && this.listIsosurface (27)) return true;
this.setShapeProperty (27, "init", Integer.$valueOf (iModel));
var title = null;
var moNumber = (this.getShapeProperty (27, "moNumber")).intValue ();
var linearCombination = this.getShapeProperty (27, "moLinearCombination");
if (isInitOnly) return true;
if (moNumber == 0) moNumber = 2147483647;
var propertyName = null;
var propertyValue = null;
switch (this.eval.getToken (i).tok) {
case 1074790451:
case 554176565:
propertyName = this.eval.theToken.value;
propertyValue = this.getCapSlabObject (i, false);
i = this.eval.iToken;
break;
case 1073741914:
propertyName = "squareLinear";
propertyValue = Boolean.TRUE;
linearCombination = [1];
offset = moNumber = 0;
break;
case 2:
moNumber = this.intParameter (i);
linearCombination = this.moCombo (propertyList);
if (linearCombination == null && moNumber < 0) linearCombination = [-100, -moNumber];
break;
case 269484192:
switch (this.tokAt (++i)) {
case 1073741973:
case 1073742008:
break;
default:
this.invArg ();
}
isNegOffset = true;
case 1073741973:
case 1073742008:
if ((offset = this.moOffset (i)) == 2147483647) this.invArg ();
moNumber = 0;
linearCombination = this.moCombo (propertyList);
break;
case 1073742037:
moNumber = 1073742037;
linearCombination = this.moCombo (propertyList);
break;
case 1073742108:
moNumber = 1073742108;
linearCombination = this.moCombo (propertyList);
break;
case 1766856708:
this.setColorOptions (null, i + 1, 27, 2);
break;
case 135266319:
propertyName = "plane";
propertyValue = this.eval.planeParameter (i + 1);
break;
case 135266320:
this.addShapeProperty (propertyList, "randomSeed", this.tokAt (i + 2) == 2 ? Integer.$valueOf (this.intParameter (i + 2)) : null);
propertyName = "monteCarloCount";
propertyValue = Integer.$valueOf (this.intParameter (i + 1));
break;
case 1073742138:
propertyName = "scale";
propertyValue = Float.$valueOf (this.floatParameter (i + 1));
break;
case 1073741910:
if (this.tokAt (i + 1) == 269484193) {
propertyName = "cutoffPositive";
propertyValue = Float.$valueOf (this.floatParameter (i + 2));
} else {
propertyName = "cutoff";
propertyValue = Float.$valueOf (this.floatParameter (i + 1));
}break;
case 536870916:
propertyName = "debug";
break;
case 1073742054:
propertyName = "plane";
break;
case 1073742104:
case 1073742122:
propertyName = "resolution";
propertyValue = Float.$valueOf (this.floatParameter (i + 1));
break;
case 1073742156:
propertyName = "squareData";
propertyValue = Boolean.TRUE;
break;
case 1073742168:
if (i + 1 < this.eval.slen && this.tokAt (i + 1) == 4) {
propertyName = "titleFormat";
propertyValue = this.parameterAsString (i + 1);
}break;
case 1073741824:
this.invArg ();
break;
default:
if (this.eval.isArrayParameter (i)) {
linearCombination = this.eval.floatParameterSet (i, 1, 2147483647);
if (this.tokAt (this.eval.iToken + 1) == 1073742156) {
this.addShapeProperty (propertyList, "squareLinear", Boolean.TRUE);
this.eval.iToken++;
}break;
}var ipt = this.eval.iToken;
if (!this.eval.setMeshDisplayProperty (27, 0, this.eval.theTok)) this.invArg ();
this.setShapeProperty (27, "setProperties", propertyList);
this.eval.setMeshDisplayProperty (27, ipt, this.tokAt (ipt));
return true;
}
if (propertyName != null) this.addShapeProperty (propertyList, propertyName, propertyValue);
if (moNumber != 2147483647 || linearCombination != null) {
if (this.tokAt (this.eval.iToken + 1) == 4) title = this.parameterAsString (++this.eval.iToken);
this.eval.setCursorWait (true);
this.setMoData (propertyList, moNumber, linearCombination, offset, isNegOffset, iModel, title);
this.addShapeProperty (propertyList, "finalize", null);
}if (propertyList.size () > 0) this.setShapeProperty (27, "setProperties", propertyList);
propertyList.clear ();
}
return true;
}, $fz.isPrivate = true, $fz), "~B");
$_M(c$, "moCombo", 
($fz = function (propertyList) {
if (this.tokAt (this.eval.iToken + 1) != 1073742156) return null;
this.addShapeProperty (propertyList, "squareLinear", Boolean.TRUE);
this.eval.iToken++;
return  Clazz.newFloatArray (0, 0);
}, $fz.isPrivate = true, $fz), "J.util.JmolList");
$_M(c$, "moOffset", 
($fz = function (index) {
var isHomo = (this.eval.getToken (index).tok == 1073741973);
var offset = (isHomo ? 0 : 1);
var tok = this.tokAt (++index);
if (tok == 2 && this.intParameter (index) < 0) offset += this.intParameter (index);
 else if (tok == 269484193) offset += this.intParameter (++index);
 else if (tok == 269484192) offset -= this.intParameter (++index);
return offset;
}, $fz.isPrivate = true, $fz), "~N");
$_M(c$, "setMoData", 
($fz = function (propertyList, moNumber, lc, offset, isNegOffset, modelIndex, title) {
if (this.chk) return;
if (modelIndex < 0) {
modelIndex = this.viewer.getCurrentModelIndex ();
if (modelIndex < 0) this.eval.errorStr (30, "MO isosurfaces");
}var moData = this.viewer.getModelAuxiliaryInfoValue (modelIndex, "moData");
var mos = null;
var mo;
var f;
var nOrb = 0;
if (lc == null || lc.length < 2) {
if (lc != null && lc.length == 1) offset = 0;
if (moData == null) this.error (27);
var lastMoNumber = (moData.containsKey ("lastMoNumber") ? (moData.get ("lastMoNumber")).intValue () : 0);
var lastMoCount = (moData.containsKey ("lastMoCount") ? (moData.get ("lastMoCount")).intValue () : 1);
if (moNumber == 1073742108) moNumber = lastMoNumber - 1;
 else if (moNumber == 1073742037) moNumber = lastMoNumber + lastMoCount;
mos = (moData.get ("mos"));
nOrb = (mos == null ? 0 : mos.size ());
if (nOrb == 0) this.error (25);
if (nOrb == 1 && moNumber > 1) this.error (29);
if (offset != 2147483647) {
if (moData.containsKey ("HOMO")) {
moNumber = (moData.get ("HOMO")).intValue () + offset;
} else {
moNumber = -1;
for (var i = 0; i < nOrb; i++) {
mo = mos.get (i);
if ((f = mo.get ("occupancy")) != null) {
if (f.floatValue () < 0.5) {
moNumber = i;
break;
}continue;
} else if ((f = mo.get ("energy")) != null) {
if (f.floatValue () > 0) {
moNumber = i;
break;
}continue;
}break;
}
if (moNumber < 0) this.error (28);
moNumber += offset;
}J.util.Logger.info ("MO " + moNumber);
}if (moNumber < 1 || moNumber > nOrb) this.eval.errorStr (26, "" + nOrb);
}moNumber = Math.abs (moNumber);
moData.put ("lastMoNumber", Integer.$valueOf (moNumber));
moData.put ("lastMoCount", Integer.$valueOf (1));
if (isNegOffset && lc == null) lc = [-100, moNumber];
if (lc != null && lc.length < 2) {
mo = mos.get (moNumber - 1);
if ((f = mo.get ("energy")) == null) {
lc = [100, moNumber];
} else {
var energy = f.floatValue ();
var bs = J.util.BS.newN (nOrb);
var n = 0;
var isAllElectrons = (lc.length == 1 && lc[0] == 1);
for (var i = 0; i < nOrb; i++) {
if ((f = mos.get (i).get ("energy")) == null) continue;
var e = f.floatValue ();
if (isAllElectrons ? e <= energy : e == energy) {
bs.set (i + 1);
n += 2;
}}
lc =  Clazz.newFloatArray (n, 0);
for (var i = 0, pt = 0; i < n; i += 2) {
lc[i] = 1;
lc[i + 1] = (pt = bs.nextSetBit (pt + 1));
}
moData.put ("lastMoNumber", Integer.$valueOf (bs.nextSetBit (0)));
moData.put ("lastMoCount", Integer.$valueOf (Clazz.doubleToInt (n / 2)));
}this.addShapeProperty (propertyList, "squareLinear", Boolean.TRUE);
}this.addShapeProperty (propertyList, "moData", moData);
if (title != null) this.addShapeProperty (propertyList, "title", title);
this.addShapeProperty (propertyList, "molecularOrbital", lc != null ? lc : Integer.$valueOf (Math.abs (moNumber)));
this.addShapeProperty (propertyList, "clear", null);
}, $fz.isPrivate = true, $fz), "J.util.JmolList,~N,~A,~N,~B,~N,~S");
Clazz.overrideMethod (c$, "plot", 
function (args) {
var modelIndex = this.viewer.getCurrentModelIndex ();
if (modelIndex < 0) this.eval.errorStr (30, "plot");
modelIndex = this.viewer.getJmolDataSourceFrame (modelIndex);
var pt = args.length - 1;
var isReturnOnly = (args !== this.st);
var statementSave = this.st;
if (isReturnOnly) this.st = args;
var tokCmd = (isReturnOnly ? 4148 : args[0].tok);
var pt0 = (isReturnOnly || tokCmd == 135270417 || tokCmd == 1052714 ? 0 : 1);
var filename = null;
var makeNewFrame = true;
var isDraw = false;
switch (tokCmd) {
case 4133:
case 135270417:
case 1052714:
break;
case 135176:
makeNewFrame = false;
isDraw = true;
break;
case 4148:
makeNewFrame = false;
break;
case 135270421:
makeNewFrame = false;
if (J.script.ScriptEvaluator.tokAtArray (pt, args) == 4) {
filename = this.stringParameter (pt--);
} else if (J.script.ScriptEvaluator.tokAtArray (pt - 1, args) == 1048584) {
filename = this.parameterAsString (pt - 2) + "." + this.parameterAsString (pt);
pt -= 3;
} else {
this.st = statementSave;
this.eval.iToken = this.st.length;
this.error (13);
}break;
}
var qFrame = "";
var parameters = null;
var stateScript = "";
var isQuaternion = false;
var isDerivative = false;
var isSecondDerivative = false;
var isRamachandranRelative = false;
var propertyX = 0;
var propertyY = 0;
var propertyZ = 0;
var bs = J.util.BSUtil.copy (this.viewer.getSelectionSet (false));
var preSelected = "; select " + J.util.Escape.eBS (bs) + ";\n ";
var type = this.eval.optParameterAsString (pt).toLowerCase ();
var minXYZ = null;
var maxXYZ = null;
var tok = J.script.ScriptEvaluator.tokAtArray (pt0, args);
if (tok == 4) tok = J.script.T.getTokFromName (args[pt0].value);
switch (tok) {
default:
this.eval.iToken = 1;
this.invArg ();
break;
case 135270407:
this.eval.iToken = 1;
type = "data";
preSelected = "";
break;
case 1716520985:
this.eval.iToken = pt0 + 1;
if (!J.script.T.tokAttr (propertyX = this.tokAt (this.eval.iToken++), 1078984704) || !J.script.T.tokAttr (propertyY = this.tokAt (this.eval.iToken++), 1078984704)) this.invArg ();
if (J.script.T.tokAttr (propertyZ = this.tokAt (this.eval.iToken), 1078984704)) this.eval.iToken++;
 else propertyZ = 0;
if (this.tokAt (this.eval.iToken) == 32) {
minXYZ = this.getPoint3f (++this.eval.iToken, false);
this.eval.iToken++;
}if (this.tokAt (this.eval.iToken) == 64) {
maxXYZ = this.getPoint3f (++this.eval.iToken, false);
this.eval.iToken++;
}type = "property " + J.script.T.nameOf (propertyX) + " " + J.script.T.nameOf (propertyY) + (propertyZ == 0 ? "" : " " + J.script.T.nameOf (propertyZ));
if (bs.nextSetBit (0) < 0) bs = this.viewer.getModelUndeletedAtomsBitSet (modelIndex);
stateScript = "select " + J.util.Escape.eBS (bs) + ";\n ";
break;
case 1052714:
if (type.equalsIgnoreCase ("draw")) {
isDraw = true;
type = this.eval.optParameterAsString (--pt).toLowerCase ();
}isRamachandranRelative = (pt > pt0 && type.startsWith ("r"));
type = "ramachandran" + (isRamachandranRelative ? " r" : "") + (tokCmd == 135176 ? " draw" : "");
break;
case 135270417:
case 137363468:
qFrame = " \"" + this.viewer.getQuaternionFrame () + "\"";
stateScript = "set quaternionFrame" + qFrame + ";\n  ";
isQuaternion = true;
if (type.equalsIgnoreCase ("draw")) {
isDraw = true;
type = this.eval.optParameterAsString (--pt).toLowerCase ();
}isDerivative = (type.startsWith ("deriv") || type.startsWith ("diff"));
isSecondDerivative = (isDerivative && type.indexOf ("2") > 0);
if (isDerivative) pt--;
if (type.equalsIgnoreCase ("helix") || type.equalsIgnoreCase ("axis")) {
isDraw = true;
isDerivative = true;
pt = -1;
}type = ((pt <= pt0 ? "" : this.eval.optParameterAsString (pt)) + "w").substring (0, 1);
if (type.equals ("a") || type.equals ("r")) isDerivative = true;
if (!J.util.Parser.isOneOf (type, ";w;x;y;z;r;a;")) this.eval.evalError ("QUATERNION [w,x,y,z,a,r] [difference][2]", null);
type = "quaternion " + type + (isDerivative ? " difference" : "") + (isSecondDerivative ? "2" : "") + (isDraw ? " draw" : "");
break;
}
this.st = statementSave;
if (this.chk) return "";
if (makeNewFrame) {
stateScript += "plot " + type;
var ptDataFrame = this.viewer.getJmolDataFrameIndex (modelIndex, stateScript);
if (ptDataFrame > 0 && tokCmd != 135270421 && tokCmd != 4148) {
this.viewer.setCurrentModelIndexClear (ptDataFrame, true);
return "";
}}var dataX = null;
var dataY = null;
var dataZ = null;
var factors = J.util.P3.new3 (1, 1, 1);
if (tok == 1716520985) {
dataX = this.eval.getBitsetPropertyFloat (bs, propertyX | 224, (minXYZ == null ? NaN : minXYZ.x), (maxXYZ == null ? NaN : maxXYZ.x));
dataY = this.eval.getBitsetPropertyFloat (bs, propertyY | 224, (minXYZ == null ? NaN : minXYZ.y), (maxXYZ == null ? NaN : maxXYZ.y));
if (propertyZ != 0) dataZ = this.eval.getBitsetPropertyFloat (bs, propertyZ | 224, (minXYZ == null ? NaN : minXYZ.z), (maxXYZ == null ? NaN : maxXYZ.z));
if (minXYZ == null) minXYZ = J.util.P3.new3 (J.scriptext.ScriptExt.getMinMax (dataX, false, propertyX), J.scriptext.ScriptExt.getMinMax (dataY, false, propertyY), J.scriptext.ScriptExt.getMinMax (dataZ, false, propertyZ));
if (maxXYZ == null) maxXYZ = J.util.P3.new3 (J.scriptext.ScriptExt.getMinMax (dataX, true, propertyX), J.scriptext.ScriptExt.getMinMax (dataY, true, propertyY), J.scriptext.ScriptExt.getMinMax (dataZ, true, propertyZ));
J.util.Logger.info ("plot min/max: " + minXYZ + " " + maxXYZ);
var center = J.util.P3.newP (maxXYZ);
center.add (minXYZ);
center.scale (0.5);
factors.setT (maxXYZ);
factors.sub (minXYZ);
factors.set (factors.x / 200, factors.y / 200, factors.z / 200);
if (J.script.T.tokAttr (propertyX, 1095761920)) {
factors.x = 1;
center.x = 0;
} else if (factors.x > 0.1 && factors.x <= 10) {
factors.x = 1;
}if (J.script.T.tokAttr (propertyY, 1095761920)) {
factors.y = 1;
center.y = 0;
} else if (factors.y > 0.1 && factors.y <= 10) {
factors.y = 1;
}if (J.script.T.tokAttr (propertyZ, 1095761920)) {
factors.z = 1;
center.z = 0;
} else if (factors.z > 0.1 && factors.z <= 10) {
factors.z = 1;
}if (propertyZ == 0) center.z = minXYZ.z = maxXYZ.z = factors.z = 0;
for (var i = 0; i < dataX.length; i++) dataX[i] = (dataX[i] - center.x) / factors.x;

for (var i = 0; i < dataY.length; i++) dataY[i] = (dataY[i] - center.y) / factors.y;

if (propertyZ != 0) for (var i = 0; i < dataZ.length; i++) dataZ[i] = (dataZ[i] - center.z) / factors.z;

parameters = [bs, dataX, dataY, dataZ, minXYZ, maxXYZ, factors, center];
}if (tokCmd == 135270421) return this.viewer.writeFileData (filename, "PLOT_" + type, modelIndex, parameters);
var data = (type.equals ("data") ? "1 0 H 0 0 0 # Jmol PDB-encoded data" : this.viewer.getPdbData (modelIndex, type, parameters));
if (tokCmd == 4148) return data;
if (J.util.Logger.debugging) J.util.Logger.debug (data);
if (tokCmd == 135176) {
this.eval.runScript (data);
return "";
}var savedFileInfo = this.viewer.getFileInfo ();
var oldAppendNew = this.viewer.getBoolean (603979792);
this.viewer.setAppendNew (true);
var isOK = (data != null && this.viewer.openStringInlineParamsAppend (data, null, true) == null);
this.viewer.setAppendNew (oldAppendNew);
this.viewer.setFileInfo (savedFileInfo);
if (!isOK) return "";
var modelCount = this.viewer.getModelCount ();
this.viewer.setJmolDataFrame (stateScript, modelIndex, modelCount - 1);
if (tok != 1716520985) stateScript += ";\n" + preSelected;
var ss = this.viewer.addStateScript (stateScript, true, false);
var radius = 150;
var script;
switch (tok) {
default:
script = "frame 0.0; frame last; reset;select visible;wireframe only;";
radius = 10;
break;
case 1716520985:
this.viewer.setFrameTitle (modelCount - 1, type + " plot for model " + this.viewer.getModelNumberDotted (modelIndex));
var f = 3;
script = "frame 0.0; frame last; reset;select visible; spacefill " + f + "; wireframe 0;" + "draw plotAxisX" + modelCount + " {100 -100 -100} {-100 -100 -100} \"" + J.script.T.nameOf (propertyX) + "\";" + "draw plotAxisY" + modelCount + " {-100 100 -100} {-100 -100 -100} \"" + J.script.T.nameOf (propertyY) + "\";";
if (propertyZ != 0) script += "draw plotAxisZ" + modelCount + " {-100 -100 100} {-100 -100 -100} \"" + J.script.T.nameOf (propertyZ) + "\";";
break;
case 1052714:
this.viewer.setFrameTitle (modelCount - 1, "ramachandran plot for model " + this.viewer.getModelNumberDotted (modelIndex));
script = "frame 0.0; frame last; reset;select visible; color structure; spacefill 3.0; wireframe 0;draw ramaAxisX" + modelCount + " {100 0 0} {-100 0 0} \"phi\";" + "draw ramaAxisY" + modelCount + " {0 100 0} {0 -100 0} \"psi\";";
break;
case 135270417:
case 137363468:
this.viewer.setFrameTitle (modelCount - 1, type.$replace ('w', ' ') + qFrame + " for model " + this.viewer.getModelNumberDotted (modelIndex));
var color = (J.util.C.getHexCode (this.viewer.getColixBackgroundContrast ()));
script = "frame 0.0; frame last; reset;select visible; wireframe 0; spacefill 3.0; isosurface quatSphere" + modelCount + " color " + color + " sphere 100.0 mesh nofill frontonly translucent 0.8;" + "draw quatAxis" + modelCount + "X {100 0 0} {-100 0 0} color red \"x\";" + "draw quatAxis" + modelCount + "Y {0 100 0} {0 -100 0} color green \"y\";" + "draw quatAxis" + modelCount + "Z {0 0 100} {0 0 -100} color blue \"z\";" + "color structure;" + "draw quatCenter" + modelCount + "{0 0 0} scale 0.02;";
break;
}
this.eval.runScript (script + preSelected);
ss.setModelIndex (this.viewer.getCurrentModelIndex ());
this.viewer.setRotationRadius (radius, true);
this.sm.loadShape (30);
this.eval.showString ("frame " + this.viewer.getModelNumberDotted (modelCount - 1) + (type.length > 0 ? " created: " + type + (isQuaternion ? qFrame : "") : ""));
return "";
}, "~A");
c$.getMinMax = $_M(c$, "getMinMax", 
($fz = function (data, isMax, tok) {
if (data == null) return 0;
switch (tok) {
case 1112539144:
case 1112539145:
case 1112539146:
return (isMax ? 180 : -180);
case 1112539141:
case 1112539152:
return (isMax ? 360 : 0);
case 1112539150:
return (isMax ? 1 : -1);
}
var fmax = (isMax ? -1.0E10 : 1E10);
for (var i = data.length; --i >= 0; ) {
var f = data[i];
if (Float.isNaN (f)) continue;
if (isMax == (f > fmax)) fmax = f;
}
return fmax;
}, $fz.isPrivate = true, $fz), "~A,~B,~N");
$_M(c$, "polyhedra", 
($fz = function () {
var needsGenerating = false;
var onOffDelete = false;
var typeSeen = false;
var edgeParameterSeen = false;
var isDesignParameter = false;
var lighting = 0;
var nAtomSets = 0;
this.sm.loadShape (21);
this.setShapeProperty (21, "init", Boolean.TRUE);
var setPropertyName = "centers";
var decimalPropertyName = "radius_";
var translucentLevel = 3.4028235E38;
this.eval.colorArgb[0] = -2147483648;
for (var i = 1; i < this.eval.slen; ++i) {
var propertyName = null;
var propertyValue = null;
switch (this.eval.getToken (i).tok) {
case 12291:
case 1048589:
case 1048588:
if (i + 1 != this.eval.slen || needsGenerating || nAtomSets > 1 || nAtomSets == 0 && "to".equals (setPropertyName)) this.error (18);
propertyName = (this.eval.theTok == 1048588 ? "off" : this.eval.theTok == 1048589 ? "on" : "delete");
onOffDelete = true;
break;
case 269484436:
case 269484080:
continue;
case 1678770178:
if (nAtomSets > 0) this.invPO ();
needsGenerating = true;
propertyName = "bonds";
break;
case 1666189314:
decimalPropertyName = "radius";
continue;
case 2:
case 3:
if (nAtomSets > 0 && !isDesignParameter) this.invPO ();
if (this.eval.theTok == 2) {
if (decimalPropertyName === "radius_") {
propertyName = "nVertices";
propertyValue = Integer.$valueOf (this.intParameter (i));
needsGenerating = true;
break;
}}propertyName = (decimalPropertyName === "radius_" ? "radius" : decimalPropertyName);
propertyValue = Float.$valueOf (this.floatParameter (i));
decimalPropertyName = "radius_";
isDesignParameter = false;
needsGenerating = true;
break;
case 10:
case 1048577:
if (typeSeen) this.invPO ();
if (++nAtomSets > 2) this.error (2);
if ("to".equals (setPropertyName)) needsGenerating = true;
propertyName = setPropertyName;
setPropertyName = "to";
propertyValue = this.atomExpressionAt (i);
i = this.eval.iToken;
break;
case 1074790746:
if (nAtomSets > 1) this.invPO ();
if (this.tokAt (i + 1) == 10 || this.tokAt (i + 1) == 1048577 && !needsGenerating) {
propertyName = "toBitSet";
propertyValue = this.atomExpressionAt (++i);
i = this.eval.iToken;
needsGenerating = true;
break;
} else if (!needsGenerating) {
this.error (19);
}setPropertyName = "to";
continue;
case 1073741937:
if (!needsGenerating) this.error (19);
decimalPropertyName = "faceCenterOffset";
isDesignParameter = true;
continue;
case 1073741924:
if (nAtomSets == 0) this.error (19);
decimalPropertyName = "distanceFactor";
isDesignParameter = true;
continue;
case 1766856708:
case 603979967:
case 1073742074:
translucentLevel = this.eval.getColorTrans (i, true);
i = this.eval.iToken;
continue;
case 1073741886:
case 1073741948:
propertyName = "collapsed";
propertyValue = (this.eval.theTok == 1073741886 ? Boolean.TRUE : Boolean.FALSE);
if (typeSeen) this.error (18);
typeSeen = true;
break;
case 1073742044:
case 1073741933:
case 1073741956:
if (edgeParameterSeen) this.error (18);
propertyName = this.parameterAsString (i);
edgeParameterSeen = true;
break;
case 1073741964:
lighting = this.eval.theTok;
continue;
default:
if (this.eval.isColorParam (i)) {
this.eval.colorArgb[0] = this.eval.getArgbParam (i);
i = this.eval.iToken;
continue;
}this.invArg ();
}
this.setShapeProperty (21, propertyName, propertyValue);
if (onOffDelete) return false;
}
if (!needsGenerating && !typeSeen && !edgeParameterSeen && lighting == 0) this.error (19);
if (needsGenerating) this.setShapeProperty (21, "generate", null);
if (this.eval.colorArgb[0] != -2147483648) this.setShapeProperty (21, "colorThis", Integer.$valueOf (this.eval.colorArgb[0]));
if (translucentLevel != 3.4028235E38) this.eval.setShapeTranslucency (21, "", "translucentThis", translucentLevel, null);
if (lighting != 0) this.setShapeProperty (21, "token", Integer.$valueOf (lighting));
this.setShapeProperty (21, "init", Boolean.FALSE);
return true;
}, $fz.isPrivate = true, $fz));
$_M(c$, "struts", 
($fz = function () {
var defOn = (this.tokAt (1) == 1073742072 || this.tokAt (1) == 1048589 || this.eval.slen == 1);
var mad = this.eval.getMadParameter ();
if (defOn) mad = Math.round (this.viewer.getFloat (570425406) * 2000);
this.setShapeProperty (1, "type", Integer.$valueOf (32768));
this.eval.setShapeSizeBs (1, mad, null);
this.setShapeProperty (1, "type", Integer.$valueOf (1023));
return true;
}, $fz.isPrivate = true, $fz));
$_M(c$, "initIsosurface", 
($fz = function (iShape) {
this.setShapeProperty (iShape, "init", this.fullCommand);
this.eval.iToken = 0;
var tok1 = this.tokAt (1);
var tok2 = this.tokAt (2);
if (tok1 == 12291 || tok2 == 12291 && this.tokAt (++this.eval.iToken) == 1048579) {
this.setShapeProperty (iShape, "delete", null);
this.eval.iToken += 2;
if (this.eval.slen > this.eval.iToken) {
this.setShapeProperty (iShape, "init", this.fullCommand);
this.setShapeProperty (iShape, "thisID", "+PREVIOUS_MESH+");
}return null;
}this.eval.iToken = 1;
if (!this.eval.setMeshDisplayProperty (iShape, 0, tok1)) {
this.setShapeProperty (iShape, "thisID", "+PREVIOUS_MESH+");
if (iShape != 22) this.setShapeProperty (iShape, "title", [this.thisCommand]);
if (tok1 != 1074790550 && (tok2 == 269484209 || tok1 == 269484209 && this.eval.setMeshDisplayProperty (iShape, 0, tok2))) {
var id = this.setShapeId (iShape, 1, false);
this.eval.iToken++;
return id;
}}return null;
}, $fz.isPrivate = true, $fz), "~N");
$_M(c$, "getWithinDistanceVector", 
($fz = function (propertyList, distance, ptc, bs, isShow) {
var v =  new J.util.JmolList ();
var pts =  new Array (2);
if (bs == null) {
var pt1 = J.util.P3.new3 (distance, distance, distance);
var pt0 = J.util.P3.newP (ptc);
pt0.sub (pt1);
pt1.add (ptc);
pts[0] = pt0;
pts[1] = pt1;
v.addLast (ptc);
} else {
var bbox = this.viewer.getBoxInfo (bs, -Math.abs (distance));
pts[0] = bbox.getBoundBoxVertices ()[0];
pts[1] = bbox.getBoundBoxVertices ()[7];
if (bs.cardinality () == 1) v.addLast (this.viewer.getAtomPoint3f (bs.nextSetBit (0)));
}if (v.size () == 1 && !isShow) {
this.addShapeProperty (propertyList, "withinDistance", Float.$valueOf (distance));
this.addShapeProperty (propertyList, "withinPoint", v.get (0));
}this.addShapeProperty (propertyList, (isShow ? "displayWithin" : "withinPoints"), [Float.$valueOf (distance), pts, bs, v]);
}, $fz.isPrivate = true, $fz), "J.util.JmolList,~N,J.util.P3,J.util.BS,~B");
$_M(c$, "setColorOptions", 
($fz = function (sb, index, iShape, nAllowed) {
this.eval.getToken (index);
var translucency = "opaque";
if (this.eval.theTok == 603979967) {
translucency = "translucent";
if (nAllowed < 0) {
var value = (this.isFloatParameter (index + 1) ? this.floatParameter (++index) : 3.4028235E38);
this.eval.setShapeTranslucency (iShape, null, "translucent", value, null);
if (sb != null) {
sb.append (" translucent");
if (value != 3.4028235E38) sb.append (" ").appendF (value);
}} else {
this.eval.setMeshDisplayProperty (iShape, index, this.eval.theTok);
}} else if (this.eval.theTok == 1073742074) {
if (nAllowed >= 0) this.eval.setMeshDisplayProperty (iShape, index, this.eval.theTok);
} else {
this.eval.iToken--;
}nAllowed = Math.abs (nAllowed);
for (var i = 0; i < nAllowed; i++) {
if (this.eval.isColorParam (this.eval.iToken + 1)) {
var color = this.eval.getArgbParam (++this.eval.iToken);
this.setShapeProperty (iShape, "colorRGB", Integer.$valueOf (color));
if (sb != null) sb.append (" ").append (J.util.Escape.escapeColor (color));
} else if (this.eval.iToken < index) {
this.invArg ();
} else {
break;
}}
return translucency;
}, $fz.isPrivate = true, $fz), "J.util.SB,~N,~N,~N");
$_M(c$, "addShapeProperty", 
($fz = function (propertyList, key, value) {
if (this.chk) return;
propertyList.addLast ([key, value]);
}, $fz.isPrivate = true, $fz), "J.util.JmolList,~S,~O");
$_M(c$, "createFunction", 
($fz = function (fname, xyz, ret) {
var e = ( new J.script.ScriptEvaluator ());
e.setViewer (this.viewer);
try {
e.compileScript (null, "function " + fname + "(" + xyz + ") { return " + ret + "}", false);
var params =  new J.util.JmolList ();
for (var i = 0; i < xyz.length; i += 2) params.addLast (J.script.SV.newVariable (3, Float.$valueOf (0)).setName (xyz.substring (i, i + 1)));

return [e.aatoken[0][1].value, params];
} catch (ex) {
if (Clazz.exceptionOf (ex, Exception)) {
return null;
} else {
throw ex;
}
}
}, $fz.isPrivate = true, $fz), "~S,~S,~S");
$_M(c$, "floatArraySet", 
($fz = function (i, nX, nY) {
var tok = this.tokAt (i++);
if (tok == 1073742195) tok = this.tokAt (i++);
if (tok != 269484096) this.invArg ();
var fparams = J.util.ArrayUtil.newFloat2 (nX);
var n = 0;
while (tok != 269484097) {
tok = this.eval.getToken (i).tok;
switch (tok) {
case 1073742195:
case 269484097:
continue;
case 269484080:
i++;
break;
case 269484096:
i++;
var f =  Clazz.newFloatArray (nY, 0);
fparams[n++] = f;
for (var j = 0; j < nY; j++) {
f[j] = this.floatParameter (i++);
if (this.tokAt (i) == 269484080) i++;
}
if (this.tokAt (i++) != 269484097) this.invArg ();
tok = 0;
if (n == nX && this.tokAt (i) != 269484097) this.invArg ();
break;
default:
this.invArg ();
}
}
return fparams;
}, $fz.isPrivate = true, $fz), "~N,~N,~N");
$_M(c$, "floatArraySetXYZ", 
($fz = function (i, nX, nY, nZ) {
var tok = this.tokAt (i++);
if (tok == 1073742195) tok = this.tokAt (i++);
if (tok != 269484096 || nX <= 0) this.invArg ();
var fparams = J.util.ArrayUtil.newFloat3 (nX, -1);
var n = 0;
while (tok != 269484097) {
tok = this.eval.getToken (i).tok;
switch (tok) {
case 1073742195:
case 269484097:
continue;
case 269484080:
i++;
break;
case 269484096:
fparams[n++] = this.floatArraySet (i, nY, nZ);
i = ++this.eval.iToken;
tok = 0;
if (n == nX && this.tokAt (i) != 269484097) this.invArg ();
break;
default:
this.invArg ();
}
}
return fparams;
}, $fz.isPrivate = true, $fz), "~N,~N,~N,~N");
$_M(c$, "listIsosurface", 
($fz = function (iShape) {
this.eval.checkLength23 ();
if (!this.chk) this.eval.showString (this.getShapeProperty (iShape, "list" + (this.tokAt (2) == 0 ? "" : " " + this.eval.getToken (2).value)));
return true;
}, $fz.isPrivate = true, $fz), "~N");
Clazz.defineStatics (c$,
"ERROR_invalidArgument", 22);
});
