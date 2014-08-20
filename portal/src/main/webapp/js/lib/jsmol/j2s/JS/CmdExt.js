Clazz.declarePackage ("JS");
Clazz.load (["JS.JmolCmdExtension"], "JS.CmdExt", ["java.lang.Boolean", "$.Float", "$.Long", "$.Short", "java.util.Hashtable", "JU.AU", "$.BS", "$.Base64", "$.Lst", "$.M3", "$.M4", "$.Measure", "$.P3", "$.PT", "$.Quat", "$.SB", "$.V3", "J.api.Interface", "J.atomdata.RadiusData", "J.c.AXES", "$.STER", "$.VDW", "J.i18n.GT", "JM.Atom", "$.AtomCollection", "$.BondSet", "$.LabelToken", "JS.SV", "$.ScriptCompiler", "$.ScriptError", "$.ScriptEval", "$.ScriptInterruption", "$.ScriptMathProcessor", "$.ScriptParam", "$.T", "JU.BSUtil", "$.BoxInfo", "$.C", "$.Edge", "$.Elements", "$.Escape", "$.Logger", "$.Parser", "$.Point3fi", "$.TempArray", "$.Txt", "JV.FileManager", "$.JC", "$.StateManager", "$.Viewer"], function () {
c$ = Clazz.decorateAsClass (function () {
this.vwr = null;
this.e = null;
this.sm = null;
this.chk = false;
this.fullCommand = null;
this.thisCommand = null;
this.st = null;
this.slen = 0;
this.lastData = null;
Clazz.instantialize (this, arguments);
}, JS, "CmdExt", null, JS.JmolCmdExtension);
Clazz.makeConstructor (c$, 
function () {
});
Clazz.overrideMethod (c$, "init", 
function (se) {
this.e = se;
this.vwr = this.e.vwr;
this.sm = this.e.sm;
return this;
}, "~O");
Clazz.overrideMethod (c$, "dispatch", 
function (iTok, b, st) {
this.chk = this.e.chk;
this.fullCommand = this.e.fullCommand;
this.thisCommand = this.e.thisCommand;
this.slen = this.e.slen;
this.st = st;
switch (iTok) {
case 4098:
this.assign ();
break;
case 135270423:
this.cache ();
break;
case 4102:
this.calculate ();
break;
case 4103:
this.capture ();
break;
case 4105:
this.centerAt ();
break;
case 135270405:
this.compare ();
break;
case 528395:
this.console ();
break;
case 4106:
this.connect (1);
break;
case 1095766024:
this.configuration ();
break;
case 135270408:
this.data ();
break;
case 1612189718:
this.connect (0);
break;
case 1052700:
this.mapProperty ();
break;
case 4126:
this.minimize ();
break;
case 1276121113:
this.modulation ();
break;
case 4131:
this.navigate ();
break;
case 4133:
case 135270418:
case 1052714:
this.plot (st);
break;
case 135270926:
this.show ();
break;
case 528443:
this.stereo ();
break;
case 135270422:
this.write (null);
break;
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
return this.isosurface (iTok);
case 26:
return this.lcaoCartoon ();
case 6:
this.measure ();
return true;
case 27:
return this.mo (b);
case 21:
return this.polyhedra ();
case 20:
this.ellipsoid ();
break;
case 4:
return this.struts ();
}
return false;
}, "~N,~B,~A");
Clazz.defineMethod (c$, "atomExpressionAt", 
 function (i) {
return this.e.atomExpressionAt (i);
}, "~N");
Clazz.defineMethod (c$, "checkLength", 
 function (i) {
this.e.checkLength (i);
}, "~N");
Clazz.defineMethod (c$, "error", 
 function (err) {
this.e.error (err);
}, "~N");
Clazz.defineMethod (c$, "invArg", 
 function () {
this.e.invArg ();
});
Clazz.defineMethod (c$, "invPO", 
 function () {
this.error (23);
});
Clazz.defineMethod (c$, "getShapeProperty", 
 function (shapeType, propertyName) {
return this.e.getShapeProperty (shapeType, propertyName);
}, "~N,~S");
Clazz.defineMethod (c$, "paramAsStr", 
 function (i) {
return this.e.paramAsStr (i);
}, "~N");
Clazz.defineMethod (c$, "centerParameter", 
 function (i) {
return this.e.centerParameter (i);
}, "~N");
Clazz.defineMethod (c$, "floatParameter", 
 function (i) {
return this.e.floatParameter (i);
}, "~N");
Clazz.defineMethod (c$, "getPoint3f", 
 function (i, allowFractional) {
return this.e.getPoint3f (i, allowFractional);
}, "~N,~B");
Clazz.defineMethod (c$, "getPoint4f", 
 function (i) {
return this.e.getPoint4f (i);
}, "~N");
Clazz.defineMethod (c$, "intParameter", 
 function (index) {
return this.e.intParameter (index);
}, "~N");
Clazz.defineMethod (c$, "isFloatParameter", 
 function (index) {
return this.e.isFloatParameter (index);
}, "~N");
Clazz.defineMethod (c$, "setShapeProperty", 
 function (shapeType, propertyName, propertyValue) {
this.e.setShapeProperty (shapeType, propertyName, propertyValue);
}, "~N,~S,~O");
Clazz.defineMethod (c$, "showString", 
 function (s) {
this.e.showString (s);
}, "~S");
Clazz.defineMethod (c$, "stringParameter", 
 function (index) {
return this.e.stringParameter (index);
}, "~N");
Clazz.defineMethod (c$, "getToken", 
 function (i) {
return this.e.getToken (i);
}, "~N");
Clazz.defineMethod (c$, "tokAt", 
 function (i) {
return this.e.tokAt (i);
}, "~N");
Clazz.defineMethod (c$, "cache", 
 function () {
var tok = this.tokAt (1);
var fileName = null;
var n = 2;
switch (tok) {
case 1276118017:
case 1073742119:
fileName = this.e.optParameterAsString (n++);
case 1073741882:
this.checkLength (n);
if (!this.chk) {
if ("all".equals (fileName)) fileName = null;
var nBytes = this.vwr.cacheFileByName (fileName, tok == 1276118017);
this.showString (nBytes < 0 ? "cache cleared" : nBytes + " bytes " + (tok == 1276118017 ? " cached" : " removed"));
}break;
default:
this.invArg ();
}
});
Clazz.defineMethod (c$, "calculate", 
 function () {
var isSurface = false;
var asDSSP = false;
var bs1 = null;
var bs2 = null;
var n = -2147483648;
if ((this.e.iToken = this.e.slen) >= 2) {
this.e.clearDefinedVariableAtomSets ();
switch (this.getToken (1).tok) {
case 1073741824:
this.checkLength (2);
break;
case 1632634891:
this.checkLength (2);
if (this.chk) return;
n = this.vwr.calculateFormalCharges (null);
this.showString (J.i18n.GT.i (J.i18n.GT._ ("{0} charges modified"), n));
return;
case 1076887572:
this.checkLength (2);
if (!this.chk) this.vwr.ms.assignAromaticBonds ();
return;
case 1612189718:
if (this.e.slen != 2) {
asDSSP = (this.tokAt (++this.e.iToken) == 1641025539);
if (asDSSP) bs1 = this.vwr.bsA ();
 else bs1 = this.atomExpressionAt (this.e.iToken);
if (!asDSSP && !(asDSSP = (this.tokAt (++this.e.iToken) == 1641025539))) bs2 = this.atomExpressionAt (this.e.iToken);
}if (this.chk) return;
n = this.vwr.autoHbond (bs1, bs2, false);
if (n != -2147483648) this.e.report (J.i18n.GT.i (J.i18n.GT._ ("{0} hydrogen bonds"), Math.abs (n)));
return;
case 1613758476:
bs1 = (this.slen == 2 ? null : this.atomExpressionAt (2));
this.e.checkLast (this.e.iToken);
if (!this.chk) this.vwr.addHydrogens (bs1, false, false);
return;
case 1112541196:
this.e.iToken = 1;
bs1 = (this.slen == 2 ? null : this.atomExpressionAt (2));
this.e.checkLast (this.e.iToken);
if (!this.chk) this.vwr.calculatePartialCharges (bs1);
return;
case 1073742102:
if (!this.chk) this.showString (this.vwr.calculatePointGroup ());
return;
case 1112539150:
this.checkLength (2);
if (!this.chk) {
this.vwr.calculateStraightness ();
this.vwr.addStateScript ("set quaternionFrame '" + this.vwr.getQuaternionFrame () + "'; calculate straightness", false, true);
}return;
case 1641025539:
bs1 = (this.slen < 4 ? null : this.atomExpressionAt (2));
switch (this.tokAt (++this.e.iToken)) {
case 1052714:
break;
case 1073741916:
if (this.chk) return;
this.e.showString (this.vwr.getAnnotationParser ().calculateDSSRStructure (this.vwr, bs1));
return;
case 1073741915:
asDSSP = true;
break;
case 0:
asDSSP = this.vwr.getBoolean (603979826);
break;
default:
this.invArg ();
}
if (!this.chk) this.showString (this.vwr.calculateStructures (bs1, asDSSP, true));
return;
case 1708058:
bs1 = (this.e.iToken + 1 < this.slen ? this.atomExpressionAt (++this.e.iToken) : null);
bs2 = (this.e.iToken + 1 < this.slen ? this.atomExpressionAt (++this.e.iToken) : null);
this.checkLength (++this.e.iToken);
if (!this.chk) {
n = this.vwr.calculateStruts (bs1, bs2);
if (n > 0) {
this.setShapeProperty (1, "type", Integer.$valueOf (32768));
this.e.setShapePropertyBs (1, "color", Integer.$valueOf (0x0FFFFFF), null);
this.e.setShapeTranslucency (1, "", "translucent", 0.5, null);
this.setShapeProperty (1, "type", Integer.$valueOf (1023));
}this.showString (J.i18n.GT.i (J.i18n.GT._ ("{0} struts added"), n));
}return;
case 3145756:
isSurface = true;
case 1112539151:
var isFrom = false;
switch (this.tokAt (2)) {
case 135266325:
this.e.iToken++;
break;
case 0:
isFrom = !isSurface;
break;
case 1073741952:
isFrom = true;
this.e.iToken++;
break;
default:
isFrom = true;
}
bs1 = (this.e.iToken + 1 < this.slen ? this.atomExpressionAt (++this.e.iToken) : this.vwr.bsA ());
this.checkLength (++this.e.iToken);
if (!this.chk) this.vwr.calculateSurface (bs1, (isFrom ? 3.4028235E38 : -1));
return;
}
}this.e.errorStr2 (53, "CALCULATE", "aromatic? hbonds? hydrogen? formalCharge? partialCharge? pointgroup? straightness? structure? struts? surfaceDistance FROM? surfaceDistance WITHIN?");
});
Clazz.defineMethod (c$, "capture", 
 function () {
if (!this.chk && !this.vwr.allowCapture ()) {
this.showString ("Cannot capture on this platform");
return;
}var fps = this.vwr.getInt (553648132);
var endTime = 10;
var mode = 0;
var fileName = "";
var params = this.vwr.captureParams;
var looping = !this.vwr.am.animationReplayMode.name ().equals ("ONCE");
var tok = this.tokAt (1);
var sfps = "";
switch (tok) {
case 0:
mode = 1150985;
break;
case 4:
fileName = this.e.optParameterAsString (1);
if (fileName.length == 0) {
mode = 1150985;
break;
}if (!fileName.endsWith (".gif")) fileName += ".gif";
var s = null;
var axis = "y";
var i = 2;
switch (this.tokAt (i)) {
case 1073742129:
looping = true;
i = 3;
axis = (this.tokAt (3) == 2 ? "y" : this.e.optParameterAsString (i++).toLowerCase ());
var n = (this.tokAt (i) == 0 ? 5 : this.intParameter (i++));
s = "; rotate Y 10 10;delay 2.0; rotate Y -10 -10; delay 2.0;rotate Y -10 -10; delay 2.0;rotate Y 10 10;delay 2.0";
s = JU.PT.rep (s, "10", "" + n);
break;
case 1611141175:
looping = true;
i = 3;
axis = this.e.optParameterAsString (i).toLowerCase ();
if (axis.length > 0) i++;
s = "; rotate Y 360 30;delay 15.0;";
if (this.tokAt (i) == 2) sfps = " " + (fps = this.intParameter (i++));
break;
case 3:
endTime = this.floatParameter (2);
break;
case 2:
fps = this.intParameter (2);
break;
}
if (s != null) {
if (!this.chk) this.vwr.setNavigationMode (false);
if (axis === "" || "xyz".indexOf (axis) < 0) axis = "y";
s = JU.PT.rep (s, "Y", axis);
s = "capture " + JU.PT.esc (fileName) + sfps + s + ";capture;";
this.e.cmdScript (0, null, s);
return;
}if (params != null) params =  new java.util.Hashtable ();
mode = 1073742032;
params =  new java.util.Hashtable ();
if (!looping) this.showString (J.i18n.GT.o (J.i18n.GT._ ("Note: Enable looping using {0}"), ["ANIMATION MODE LOOP"]));
this.showString (J.i18n.GT.o (J.i18n.GT._ ("Animation delay based on: {0}"), ["ANIMATION FPS " + fps]));
params.put ("captureFps", Integer.$valueOf (fps));
break;
case 1073741874:
case 1048589:
case 1048588:
this.checkLength (2);
mode = tok;
break;
default:
this.invArg ();
}
if (this.chk || params == null) return;
params.put ("type", "GIF");
params.put ("fileName", fileName);
params.put ("quality", Integer.$valueOf (-1));
params.put ("endTime", Long.$valueOf (System.currentTimeMillis () + Clazz.floatToLong (endTime * 1000)));
params.put ("captureMode", JS.T.nameOf (mode).toLowerCase ());
params.put ("captureLooping", looping ? Boolean.TRUE : Boolean.FALSE);
var msg = this.vwr.processWriteOrCapture (params);
JU.Logger.info (msg);
});
Clazz.defineMethod (c$, "centerAt", 
 function () {
var tok = this.getToken (1).tok;
switch (tok) {
case 1073741826:
case 96:
case 1679429641:
break;
default:
this.invArg ();
}
var pt = JU.P3.new3 (0, 0, 0);
if (this.slen == 5) {
pt.x = this.floatParameter (2);
pt.y = this.floatParameter (3);
pt.z = this.floatParameter (4);
} else if (this.e.isCenterParameter (2)) {
pt = this.centerParameter (2);
this.e.checkLast (this.e.iToken);
} else {
this.checkLength (2);
}if (!this.chk && !this.vwr.isJmolDataFrame ()) this.vwr.tm.setCenterAt (tok, pt);
});
Clazz.defineMethod (c$, "cgo", 
 function () {
var eval = this.e;
this.sm.loadShape (23);
if (this.tokAt (1) == 1073742001 && this.listIsosurface (23)) return false;
var iptDisplayProperty = 0;
var thisId = this.initIsosurface (23);
var idSeen = (thisId != null);
var isWild = (idSeen && this.getShapeProperty (23, "ID") == null);
var isInitialized = false;
var data = null;
var translucentLevel = 3.4028235E38;
var colorArgb = [-2147483648];
var intScale = 0;
for (var i = eval.iToken; i < this.slen; ++i) {
var propertyName = null;
var propertyValue = null;
switch (this.getToken (i).tok) {
case 7:
case 269484096:
case 1073742195:
if (data != null || isWild) this.invArg ();
data = eval.listParameter (i, 2, 2147483647);
i = eval.iToken;
continue;
case 1073742138:
if (++i >= this.slen) this.error (34);
switch (this.getToken (i).tok) {
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
translucentLevel = this.getColorTrans (eval, i, false, colorArgb);
i = eval.iToken;
idSeen = true;
continue;
case 1074790550:
thisId = this.setShapeId (23, ++i, idSeen);
isWild = (this.getShapeProperty (23, "ID") == null);
i = eval.iToken;
break;
default:
if (!eval.setMeshDisplayProperty (23, 0, eval.theTok)) {
if (eval.theTok == 269484209 || JS.T.tokAttr (eval.theTok, 1073741824)) {
thisId = this.setShapeId (23, i, idSeen);
i = eval.iToken;
break;
}this.invArg ();
}if (iptDisplayProperty == 0) iptDisplayProperty = i;
i = eval.iToken;
continue;
}
idSeen = (eval.theTok != 12291);
if (data != null && !isInitialized) {
propertyName = "points";
propertyValue = Integer.$valueOf (intScale);
isInitialized = true;
intScale = 0;
}if (propertyName != null) this.setShapeProperty (23, propertyName, propertyValue);
}
this.finalizeObject (23, colorArgb[0], translucentLevel, intScale, data != null, data, iptDisplayProperty, null);
return true;
});
Clazz.defineMethod (c$, "compare", 
 function () {
var isQuaternion = false;
var doRotate = false;
var doTranslate = false;
var doAnimate = false;
var isFlexFit = false;
var data1 = null;
var data2 = null;
var bsAtoms1 = null;
var bsAtoms2 = null;
var vAtomSets = null;
var vQuatSets = null;
this.e.iToken = 0;
var nSeconds = (this.isFloatParameter (1) ? this.floatParameter (++this.e.iToken) : NaN);
var bsFrom = this.atomExpressionAt (++this.e.iToken);
var coordTo = null;
var bsTo = null;
if (this.e.isArrayParameter (++this.e.iToken)) {
coordTo = this.e.getPointArray (this.e.iToken, -1);
} else if (this.tokAt (this.e.iToken) != 1141899265) {
bsTo = this.atomExpressionAt (this.e.iToken);
}var bsSubset = null;
var isSmiles = false;
var strSmiles = null;
var bs = JU.BSUtil.copy (bsFrom);
if (bsTo != null) bs.or (bsTo);
var isToSubsetOfFrom = (coordTo == null && bsTo != null && bs.equals (bsFrom));
var isFrames = isToSubsetOfFrom;
for (var i = this.e.iToken + 1; i < this.slen; ++i) {
switch (this.getToken (i).tok) {
case 4115:
isFrames = true;
break;
case 135267336:
isSmiles = true;
if (this.tokAt (i + 1) != 4) {
strSmiles = "*";
break;
}case 135267335:
strSmiles = this.stringParameter (++i);
break;
case 1678770178:
isFlexFit = true;
doRotate = true;
strSmiles = this.paramAsStr (++i);
if (strSmiles.equalsIgnoreCase ("SMILES")) {
isSmiles = true;
strSmiles = "*";
}break;
case 3:
case 2:
nSeconds = Math.abs (this.floatParameter (i));
if (nSeconds > 0) doAnimate = true;
break;
case 269484080:
break;
case 3158024:
bsSubset = this.atomExpressionAt (++i);
i = this.e.iToken;
break;
case 10:
case 1048577:
if (vQuatSets != null) this.invArg ();
bsAtoms1 = this.atomExpressionAt (this.e.iToken);
var tok = (isToSubsetOfFrom ? 0 : this.tokAt (this.e.iToken + 1));
bsAtoms2 = (coordTo == null && this.e.isArrayParameter (this.e.iToken + 1) ? null : (tok == 10 || tok == 1048577 ? this.atomExpressionAt (++this.e.iToken) : JU.BSUtil.copy (bsAtoms1)));
if (bsSubset != null) {
bsAtoms1.and (bsSubset);
if (bsAtoms2 != null) bsAtoms2.and (bsSubset);
}if (bsAtoms2 == null) coordTo = this.e.getPointArray (++this.e.iToken, -1);
 else bsAtoms2.and (bsTo);
if (vAtomSets == null) vAtomSets =  new JU.Lst ();
vAtomSets.addLast ([bsAtoms1, bsAtoms2]);
i = this.e.iToken;
break;
case 7:
if (vAtomSets != null) this.invArg ();
isQuaternion = true;
data1 = this.e.getQuaternionArray ((this.e.theToken).getList (), 1073742001);
this.getToken (++i);
data2 = this.e.getQuaternionArray ((this.e.theToken).getList (), 1073742001);
if (vQuatSets == null) vQuatSets =  new JU.Lst ();
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
bsAtoms1 = (isAtoms ? this.vwr.getAtomBitSet ("spine") :  new JU.BS ());
if (bsAtoms1.nextSetBit (0) < 0) {
bsAtoms1 = bsFrom;
bsAtoms2 = bsTo;
} else {
bsAtoms2 = JU.BSUtil.copy (bsAtoms1);
bsAtoms1.and (bsFrom);
bsAtoms2.and (bsTo);
}} else {
bsAtoms1 = JU.BSUtil.copy (bsFrom);
bsAtoms2 = JU.BSUtil.copy (bsTo);
bsAtoms1.and (bsSubset);
bsAtoms2.and (bsSubset);
bsAtoms1.and (bsFrom);
bsAtoms2.and (bsTo);
}vAtomSets =  new JU.Lst ();
vAtomSets.addLast ([bsAtoms1, bsAtoms2]);
}var bsFrames;
if (isFrames) {
var bsModels = this.vwr.ms.getModelBS (bsFrom, false);
bsFrames =  new Array (bsModels.cardinality ());
for (var i = 0, iModel = bsModels.nextSetBit (0); iModel >= 0; iModel = bsModels.nextSetBit (iModel + 1), i++) bsFrames[i] = this.vwr.getModelUndeletedAtomsBitSet (iModel);

} else {
bsFrames = [bsFrom];
}for (var iFrame = 0; iFrame < bsFrames.length; iFrame++) {
bsFrom = bsFrames[iFrame];
var retStddev =  Clazz.newFloatArray (2, 0);
var q = null;
var vQ =  new JU.Lst ();
var centerAndPoints = null;
var vAtomSets2 = (isFrames ?  new JU.Lst () : vAtomSets);
for (var i = 0; i < vAtomSets.size (); ++i) {
var bss = vAtomSets.get (i);
if (isFrames) vAtomSets2.addLast (bss = [JU.BSUtil.copy (bss[0]), bss[1]]);
bss[0].and (bsFrom);
}
var center = null;
var translation = null;
if (isAtoms) {
if (coordTo != null) {
vAtomSets2.clear ();
vAtomSets2.addLast ([bsAtoms1, coordTo]);
}try {
centerAndPoints = this.vwr.getCenterAndPoints (vAtomSets2, true);
} catch (ex) {
if (Clazz.exceptionOf (ex, Exception)) {
this.invArg ();
} else {
throw ex;
}
}
var n = centerAndPoints[0].length - 1;
for (var i = 1; i <= n; i++) {
var aij = centerAndPoints[0][i];
var bij = centerAndPoints[1][i];
if (!(Clazz.instanceOf (aij, JM.Atom)) || !(Clazz.instanceOf (bij, JM.Atom))) break;
JU.Logger.info (" atom 1 " + (aij).getInfo () + "\tatom 2 " + (bij).getInfo ());
}
q = JU.Measure.calculateQuaternionRotation (centerAndPoints, retStddev);
var r0 = (Float.isNaN (retStddev[1]) ? NaN : Math.round (retStddev[0] * 100) / 100);
var r1 = (Float.isNaN (retStddev[1]) ? NaN : Math.round (retStddev[1] * 100) / 100);
this.showString ("RMSD " + r0 + " --> " + r1 + " Angstroms");
} else if (isQuaternion) {
if (vQuatSets == null) {
for (var i = 0; i < vAtomSets2.size (); i++) {
var bss = vAtomSets2.get (i);
data1 = this.vwr.getAtomGroupQuaternions (bss[0], 2147483647);
data2 = this.vwr.getAtomGroupQuaternions (bss[1], 2147483647);
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
q = JU.Quat.sphereMean (data1, retStddev, 0.0001);
this.showString ("RMSD = " + retStddev[0] + " degrees");
} else {
var m4 =  new JU.M4 ();
center =  new JU.P3 ();
if (("*".equals (strSmiles) || "".equals (strSmiles)) && bsFrom != null) try {
strSmiles = this.vwr.getSmiles (bsFrom);
} catch (ex) {
if (Clazz.exceptionOf (ex, Exception)) {
this.e.evalError (ex.getMessage (), null);
} else {
throw ex;
}
}
if (isFlexFit) {
var list;
if (bsFrom == null || bsTo == null || (list = this.e.getSmilesExt ().getFlexFitList (bsFrom, bsTo, strSmiles, !isSmiles)) == null) return;
this.vwr.setDihedrals (list, null, 1);
}var stddev = this.e.getSmilesExt ().getSmilesCorrelation (bsFrom, bsTo, strSmiles, null, null, m4, null, !isSmiles, false, null, center, false, false);
if (Float.isNaN (stddev)) {
this.showString ("structures do not match");
return;
}if (doTranslate) {
translation =  new JU.V3 ();
m4.getTranslation (translation);
}if (doRotate) {
var m3 =  new JU.M3 ();
m4.getRotationScale (m3);
q = JU.Quat.newM (m3);
}this.showString ("RMSD = " + stddev + " Angstroms");
}if (centerAndPoints != null) center = centerAndPoints[0][0];
if (center == null) {
centerAndPoints = this.vwr.getCenterAndPoints (vAtomSets2, true);
center = centerAndPoints[0][0];
}var pt1 =  new JU.P3 ();
var endDegrees = NaN;
if (doTranslate) {
if (translation == null) translation = JU.V3.newVsub (centerAndPoints[1][0], center);
endDegrees = 1e10;
}if (doRotate) {
if (q == null) this.e.evalError ("option not implemented", null);
pt1.add2 (center, q.getNormal ());
endDegrees = q.getTheta ();
if (endDegrees == 0 && doTranslate) {
if (translation.length () > 0.01) endDegrees = 1e10;
 else doRotate = doTranslate = doAnimate = false;
}}if (Float.isNaN (endDegrees) || Float.isNaN (pt1.x)) continue;
var ptsB = null;
if (doRotate && doTranslate && nSeconds != 0) {
var ptsA = this.vwr.ms.getAtomPointVector (bsFrom);
var m4 = JS.ScriptMathProcessor.getMatrix4f (q.getMatrix (), translation);
ptsB = JU.Measure.transformPoints (ptsA, m4, center);
}if (!this.e.useThreads ()) doAnimate = false;
if (this.vwr.rotateAboutPointsInternal (this.e, center, pt1, endDegrees / nSeconds, endDegrees, doAnimate, bsFrom, translation, ptsB, null) && doAnimate && this.e.isJS) throw  new JS.ScriptInterruption (this.e, "compare", 1);
}
});
Clazz.defineMethod (c$, "configuration", 
 function () {
var bsAtoms;
var bsSelected = this.vwr.bsA ();
if (this.slen == 1) {
bsAtoms = this.vwr.ms.setConformation (bsSelected);
this.vwr.ms.addStateScript ("select", null, bsSelected, null, "configuration", true, false);
} else {
var n = this.intParameter (this.e.checkLast (1));
if (this.chk) return;
bsAtoms = this.vwr.getConformation (this.vwr.am.cmi, n - 1, true);
this.vwr.addStateScript ("configuration " + n + ";", true, false);
}if (this.chk) return;
this.setShapeProperty (1, "type", Integer.$valueOf (30720));
this.e.setShapeSizeBs (1, 0, bsAtoms);
this.vwr.autoHbond (bsAtoms, bsAtoms, true);
this.vwr.select (bsAtoms, false, 0, this.e.tQuiet);
});
Clazz.defineMethod (c$, "measure", 
 function () {
var eval = this.e;
var id = null;
var pt = 1;
var colix = 0;
var offset = null;
if (this.slen == 2) switch (this.tokAt (1)) {
case 1048588:
this.setShapeProperty (6, "hideAll", Boolean.TRUE);
return;
case 12291:
if (!this.chk) this.vwr.clearAllMeasurements ();
return;
}
this.vwr.shm.loadShape (6);
switch (this.tokAt (1)) {
case 135267335:
var smarts = this.stringParameter (this.slen == 3 ? 2 : 4);
if (this.chk) return;
var atoms = this.vwr.ms.at;
var ac = this.vwr.getAtomCount ();
var maps = null;
try {
maps = this.vwr.getSmilesMatcher ().getCorrelationMaps (smarts, atoms, ac, this.vwr.bsA (), true, false);
} catch (ex) {
if (Clazz.exceptionOf (ex, Exception)) {
eval.evalError (ex.getMessage (), null);
} else {
throw ex;
}
}
if (maps == null) return;
this.setShapeProperty (6, "maps", maps);
return;
}
switch (this.slen) {
case 2:
switch (this.getToken (pt).tok) {
case 0:
case 1048589:
this.vwr.shm.loadShape (6);
this.setShapeProperty (6, "hideAll", Boolean.FALSE);
return;
case 1073742001:
if (!this.chk) eval.showStringPrint (this.vwr.getMeasurementInfoAsString (), false);
return;
case 4:
this.setShapeProperty (6, "setFormats", this.stringParameter (1));
return;
}
eval.errorStr (24, "ON, OFF, DELETE");
break;
case 3:
switch (this.getToken (1).tok) {
case 12291:
if (this.getToken (2).tok == 1048579) {
if (!this.chk) this.vwr.clearAllMeasurements ();
} else {
var i = this.intParameter (2) - 1;
if (!this.chk) this.vwr.deleteMeasurement (i);
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
var points =  new JU.Lst ();
var bs =  new JU.BS ();
var value = null;
var tickInfo = null;
var nBitSets = 0;
var mad = 0;
for (var i = 1; i < this.slen; ++i) {
switch (this.getToken (i).tok) {
case 1074790550:
if (i != 1) this.invArg ();
id = eval.optParameterAsString (++i);
continue;
case 1073741824:
eval.errorStr (24, "ALL, ALLCONNECTED, DELETE");
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
isAllConnected = (eval.theTok == 1073741834);
atomIndex = -1;
isAll = true;
if (isAllConnected && isNotConnected) this.invArg ();
break;
case 1766856708:
colix = JU.C.getColix (eval.getArgbParam (++i));
i = eval.iToken;
break;
case 1073742066:
if (eval.isPoint3f (++i)) {
var p = this.getPoint3f (i, false);
offset = [1, p.x, p.y, p.z, 0, 0, 0];
} else {
offset = eval.floatParameterSet (i, 7, 7);
}i = eval.iToken;
break;
case 1666189314:
case 1073741917:
mad = Clazz.floatToInt ((eval.theTok == 1666189314 ? 2000 : 1000) * this.floatParameter (++i));
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
var fontface = this.paramAsStr (++i);
var fontstyle = this.paramAsStr (++i);
if (!this.chk) font = this.vwr.getFont3D (fontface, fontstyle, fontsize);
break;
case 2:
var iParam = this.intParameter (i);
if (isAll) {
isRange = true;
ptFloat = (ptFloat + 1) % 2;
rangeMinMax[ptFloat] = iParam;
} else {
atomIndex = this.vwr.getAtomIndexFromAtomNumber (iParam);
if (!this.chk && atomIndex < 0) return;
if (value != null) this.invArg ();
if ((countPlusIndexes[0] = ++nAtoms) > 4) eval.bad ();
countPlusIndexes[nAtoms] = atomIndex;
}break;
case 1095761935:
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
intramolecular = Boolean.$valueOf (eval.theTok == 1073741989);
isAll = true;
isNotConnected = (eval.theTok == 1073741990);
break;
case 1649412120:
if (ptFloat >= 0) this.invArg ();
rd = eval.encodeRadiusParameter (i, false, true);
if (rd == null) return;
rd.values = rangeMinMax;
i = eval.iToken;
isNotConnected = true;
isAll = true;
intramolecular = Boolean.$valueOf (false);
if (nBitSets == 1) {
nBitSets++;
nAtoms++;
var bs2 = JU.BSUtil.copy (bs);
JU.BSUtil.invertInPlace (bs2, this.vwr.getAtomCount ());
bs2.and (this.vwr.ms.getAtomsWithinRadius (5, bs, false, null));
points.addLast (bs2);
}break;
case 10:
case 1048577:
case 1048586:
case 8:
case 1048582:
if (eval.theTok == 10 || eval.theTok == 1048577) nBitSets++;
if (atomIndex >= 0) this.invArg ();
eval.expressionResult = Boolean.FALSE;
value = this.centerParameter (i);
if (Clazz.instanceOf (eval.expressionResult, JU.BS)) {
value = bs = eval.expressionResult;
if (!this.chk && bs.length () == 0) return;
}if (Clazz.instanceOf (value, JU.P3)) {
var v =  new JU.Point3fi ();
v.setT (value);
v.mi = modelIndex;
value = v;
}if ((nAtoms = ++expressionCount) > 4) eval.bad ();
i = eval.iToken;
points.addLast (value);
break;
case 4:
strFormat = this.stringParameter (i);
break;
case 1073742164:
tickInfo = eval.tickParamAsStr (i, false, true, true);
i = eval.iToken;
tokAction = 1060866;
break;
}
}
if (rd != null && (ptFloat >= 0 || nAtoms != 2) || nAtoms < 2 && id == null && (tickInfo == null || nAtoms == 1)) eval.bad ();
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
if (font != null) text = (J.api.Interface.getInterface ("JM.Text")).newLabel (this.vwr.gdata, font, "", colix, 0, 0, 0, null);
if (text != null) text.pymolOffset = offset;
this.setShapeProperty (6, "measure", this.vwr.newMeasurementData (id, points).set (tokAction, null, rd, strFormat, null, tickInfo, isAllConnected, isNotConnected, intramolecular, isAll, mad, colix, text));
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
});
Clazz.defineMethod (c$, "connect", 
 function (index) {
var eval = this.e;
var distances =  Clazz.newFloatArray (2, 0);
var atomSets =  new Array (2);
atomSets[0] = atomSets[1] = this.vwr.bsA ();
var radius = NaN;
var colorArgb = [-2147483648];
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
var bsBonds =  new JU.BS ();
var isBonds = false;
var expression2 = 0;
var ptColor = 0;
var energy = 0;
var addGroup = false;
if (this.slen == 1) {
if (!this.chk) this.vwr.rebondState (eval.$isStateScript);
return;
}for (var i = index; i < this.slen; ++i) {
switch (this.getToken (i).tok) {
case 1048589:
case 1048588:
this.checkLength (2);
if (!this.chk) this.vwr.rebondState (eval.$isStateScript);
return;
case 2:
case 3:
if (nAtomSets > 0) {
if (haveType || isColorOrRadius) eval.error (23);
bo = JU.Edge.getBondOrderFromFloat (this.floatParameter (i));
if (bo == 131071) this.invArg ();
bondOrder = bo;
haveType = true;
break;
}if (++nDistances > 2) eval.bad ();
var dist = this.floatParameter (i);
if (this.tokAt (i + 1) == 269484210) {
dist = -dist / 100;
i++;
}distances[distanceCount++] = dist;
break;
case 10:
case 1048577:
if (nAtomSets > 2 || isBonds && nAtomSets > 0) eval.bad ();
if (haveType || isColorOrRadius) this.invArg ();
atomSets[nAtomSets++] = this.atomExpressionAt (i);
isBonds = eval.isBondSet;
if (nAtomSets == 2) {
var pt = eval.iToken;
for (var j = i; j < pt; j++) if (this.tokAt (j) == 1073741824 && this.paramAsStr (j).equals ("_1")) {
expression2 = i;
break;
}
eval.iToken = pt;
}i = eval.iToken;
break;
case 1087373318:
addGroup = true;
break;
case 1766856708:
case 603979967:
case 1073742074:
isColorOrRadius = true;
translucentLevel = this.getColorTrans (eval, i, false, colorArgb);
i = eval.iToken;
break;
case 1074790662:
var isAuto = (this.tokAt (2) == 1073741852);
this.checkLength (isAuto ? 3 : 2);
if (this.chk) return;
this.vwr.clearModelDependentObjects ();
this.vwr.ms.deleteAllBonds ();
var bsExclude =  new JU.BS ();
this.vwr.ms.setPdbConectBonding (0, 0, bsExclude);
if (isAuto) {
var isLegacy = eval.$isStateScript && this.vwr.g.legacyAutoBonding;
this.vwr.ms.autoBondBs4 (null, null, bsExclude, null, this.vwr.getMadBond (), isLegacy);
this.vwr.addStateScript ((isLegacy ? "set legacyAutoBonding TRUE;connect PDB AUTO;set legacyAutoBonding FALSE;" : "connect PDB auto;"), false, true);
return;
}this.vwr.addStateScript ("connect PDB;", false, true);
return;
case 1073741830:
case 1073741852:
case 1073741904:
case 1073742025:
case 1073742026:
haveOperation = true;
if (++i != this.slen) this.invArg ();
operation = eval.theTok;
if (operation == 1073741852 && !(bondOrder == 131071 || bondOrder == 2048 || bondOrder == 515)) this.invArg ();
break;
case 1708058:
if (!isColorOrRadius) {
colorArgb[0] = 0xFFFFFF;
translucentLevel = 0.5;
radius = this.vwr.getFloat (570425406);
isColorOrRadius = true;
}if (!haveOperation) operation = 1073742026;
haveOperation = true;
case 1073741824:
if (eval.isColorParam (i)) {
ptColor = -i;
break;
}case 1076887572:
case 1612189718:
var cmd = this.paramAsStr (i);
if ((bo = JS.ScriptParam.getBondOrderFromString (cmd)) == 131071) {
this.invArg ();
}if (haveType) eval.error (18);
haveType = true;
switch (bo) {
case 33:
switch (this.tokAt (i + 1)) {
case 3:
bo = JS.ScriptParam.getPartialBondOrderFromFloatEncodedInt (this.st[++i].intValue);
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
if (++i != this.slen) this.invArg ();
operation = 12291;
isDelete = true;
isColorOrRadius = false;
break;
default:
ptColor = i;
break;
}
if (i > 0) {
if (ptColor == -i || ptColor == i && eval.isColorParam (i)) {
isColorOrRadius = true;
colorArgb[0] = eval.getArgbParam (i);
i = eval.iToken;
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
var bs =  new JU.BS ();
eval.definedAtomSets.put ("_1", bs);
var bs0 = atomSets[0];
for (var atom1 = bs0.nextSetBit (0); atom1 >= 0; atom1 = bs0.nextSetBit (atom1 + 1)) {
bs.set (atom1);
result = this.vwr.makeConnections (distances[0], distances[1], bondOrder, operation, bs, this.atomExpressionAt (expression2), bsBonds, isBonds, false, 0);
nNew += Math.abs (result[0]);
nModified += result[1];
bs.clear (atom1);
}
} else {
result = this.vwr.makeConnections (distances[0], distances[1], bondOrder, operation, atomSets[0], atomSets[1], bsBonds, isBonds, addGroup, energy);
nNew += Math.abs (result[0]);
nModified += result[1];
}var report = eval.doReport ();
if (isDelete) {
if (report) eval.report (J.i18n.GT.i (J.i18n.GT._ ("{0} connections deleted"), nModified));
return;
}if (isColorOrRadius) {
this.vwr.selectBonds (bsBonds);
if (!Float.isNaN (radius)) eval.setShapeSizeBs (1, Math.round (radius * 2000), null);
this.finalizeObject (1, colorArgb[0], translucentLevel, 0, false, null, 0, bsBonds);
this.vwr.selectBonds (null);
}if (report) eval.report (J.i18n.GT.o (J.i18n.GT._ ("{0} new bonds; {1} modified"), [Integer.$valueOf (nNew), Integer.$valueOf (nModified)]));
}, "~N");
Clazz.defineMethod (c$, "console", 
 function () {
switch (this.getToken (1).tok) {
case 1048588:
if (!this.chk) this.vwr.showConsole (false);
break;
case 1048589:
if (!this.chk) this.vwr.showConsole (true);
break;
case 1073741882:
if (!this.chk) this.vwr.sm.clearConsole ();
break;
case 135270422:
this.showString (this.stringParameter (2));
break;
default:
this.invArg ();
}
});
Clazz.defineMethod (c$, "contact", 
 function () {
var eval = this.e;
this.sm.loadShape (25);
if (this.tokAt (1) == 1073742001 && this.listIsosurface (25)) return false;
var iptDisplayProperty = 0;
eval.iToken = 1;
var thisId = this.initIsosurface (25);
var idSeen = (thisId != null);
var isWild = (idSeen && this.getShapeProperty (25, "ID") == null);
var bsA = null;
var bsB = null;
var bs = null;
var rd = null;
var params = null;
var colorDensity = false;
var sbCommand =  new JU.SB ();
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
var okNoAtoms = (eval.iToken > 1);
for (var i = eval.iToken; i < this.slen; ++i) {
switch (tok = this.getToken (i).tok) {
default:
okNoAtoms = true;
if (!eval.setMeshDisplayProperty (25, 0, eval.theTok)) {
if (eval.theTok != 269484209 && !JS.T.tokAttr (eval.theTok, 1073741824)) this.invArg ();
thisId = this.setShapeId (25, i, idSeen);
i = eval.iToken;
break;
}if (iptDisplayProperty == 0) iptDisplayProperty = i;
i = eval.iToken;
continue;
case 1074790550:
okNoAtoms = true;
this.setShapeId (25, ++i, idSeen);
isWild = (this.getShapeProperty (25, "ID") == null);
i = eval.iToken;
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
eval.setMeshDisplayProperty (25, i, eval.theTok);
i = eval.iToken;
break;
case 554176565:
okNoAtoms = true;
userSlabObject = this.getCapSlabObject (i, false);
this.setShapeProperty (25, "slab", userSlabObject);
i = eval.iToken;
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
case 135266325:
case 1276118018:
distance = this.floatParameter (++i);
sbCommand.append (" within ").appendF (distance);
break;
case 269484193:
case 2:
case 3:
rd = eval.encodeRadiusParameter (i, false, false);
if (rd == null) return false;
sbCommand.append (" ").appendO (rd);
i = eval.iToken;
break;
case 1073741990:
case 1073741989:
intramolecular = (tok == 1073741989 ? Boolean.TRUE : Boolean.FALSE);
sbCommand.append (" ").appendO (eval.theToken.value);
break;
case 1073742020:
minSet = this.intParameter (++i);
break;
case 1612189718:
case 1073741881:
case 1649412120:
contactType = tok;
sbCommand.append (" ").appendO (eval.theToken.value);
break;
case 1073742135:
if (this.isFloatParameter (i + 1)) saProbeRadius = this.floatParameter (++i);
case 1074790451:
case 1073742036:
case 3145756:
localOnly = false;
case 1276117512:
case 1073741961:
case 135266319:
case 4106:
displayType = tok;
sbCommand.append (" ").appendO (eval.theToken.value);
if (tok == 1073742135) sbCommand.append (" ").appendF (saProbeRadius);
break;
case 1073742083:
params = eval.floatParameterSet (++i, 1, 10);
i = eval.iToken;
break;
case 10:
case 1048577:
if (isWild || bsB != null) this.invArg ();
bs = JU.BSUtil.copy (this.atomExpressionAt (i));
i = eval.iToken;
if (bsA == null) bsA = bs;
 else bsB = bs;
sbCommand.append (" ").append (JU.Escape.eBS (bs));
break;
}
idSeen = (eval.theTok != 12291);
}
if (!okNoAtoms && bsA == null) this.error (13);
if (this.chk) return false;
if (bsA != null) {
if (contactType == 1649412120 && rd == null) rd =  new J.atomdata.RadiusData (null, 0, J.atomdata.RadiusData.EnumType.OFFSET, J.c.VDW.AUTO);
var rd1 = (rd == null ?  new J.atomdata.RadiusData (null, 0.26, J.atomdata.RadiusData.EnumType.OFFSET, J.c.VDW.AUTO) : rd);
if (displayType == 1073742036 && bsB == null && intramolecular != null && intramolecular.booleanValue ()) bsB = bsA;
 else bsB = eval.getMathExt ().setContactBitSets (bsA, bsB, localOnly, distance, rd1, true);
switch (displayType) {
case 1074790451:
case 1073742135:
var bsSolvent = eval.lookupIdentifierValue ("solvent");
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
params = (params == null ?  Clazz.newFloatArray (2, 0) : JU.AU.ensureLengthA (params, 2));
params[1] = (intramolecular.booleanValue () ? 1 : 2);
}if (params != null) sbCommand.append (" parameters ").append (JU.Escape.eAF (params));
this.setShapeProperty (25, "set", [Integer.$valueOf (contactType), Integer.$valueOf (displayType), Boolean.$valueOf (colorDensity), Boolean.$valueOf (colorByType), bsA, bsB, rd, Float.$valueOf (saProbeRadius), params, sbCommand.toString ()]);
if (colorpt > 0) eval.setMeshDisplayProperty (25, colorpt, 0);
}if (iptDisplayProperty > 0) {
if (!eval.setMeshDisplayProperty (25, iptDisplayProperty, 0)) this.invArg ();
}if (userSlabObject != null && bsA != null) this.setShapeProperty (25, "slab", userSlabObject);
if (bsA != null && (displayType == 1073742036 || localOnly)) {
var volume = this.getShapeProperty (25, "volume");
if (JU.PT.isAD (volume)) {
var vs = volume;
var v = 0;
for (var i = 0; i < vs.length; i++) v += Math.abs (vs[i]);

volume = Float.$valueOf (v);
}var nsets = (this.getShapeProperty (25, "nSets")).intValue ();
if (colorDensity || displayType != 1276117512) {
this.showString ((nsets == 0 ? "" : nsets + " contacts with ") + "net volume " + volume + " A^3");
}}return true;
});
Clazz.defineMethod (c$, "dipole", 
 function () {
var eval = this.e;
var propertyName = null;
var propertyValue = null;
var iHaveAtoms = false;
var iHaveCoord = false;
var idSeen = false;
this.sm.loadShape (17);
if (this.tokAt (1) == 1073742001 && this.listIsosurface (17)) return false;
this.setShapeProperty (17, "init", null);
if (this.slen == 1) {
this.setShapeProperty (17, "thisID", null);
return false;
}for (var i = 1; i < this.slen; ++i) {
propertyName = null;
propertyValue = null;
switch (this.getToken (i).tok) {
case 1048579:
propertyName = "all";
break;
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
if (this.tokAt (i + 1) == 10) {
this.setShapeProperty (17, "startSet", this.atomExpressionAt (i++));
} else {
propertyName = "atomBitset";
}case 1048577:
if (propertyName == null) propertyName = (iHaveAtoms || iHaveCoord ? "endSet" : "startSet");
propertyValue = this.atomExpressionAt (i);
i = eval.iToken;
if (this.tokAt (i + 1) == 0 && propertyName === "startSet") propertyName = "atomBitset";
iHaveAtoms = true;
break;
case 1048586:
case 8:
var pt = this.getPoint3f (i, true);
i = eval.iToken;
propertyName = (iHaveAtoms || iHaveCoord ? "endCoord" : "startCoord");
propertyValue = pt;
iHaveCoord = true;
break;
case 1678770178:
propertyName = "bonds";
break;
case 4102:
propertyName = "calculate";
if (this.tokAt (i + 1) == 10 || this.tokAt (i + 1) == 1048577) {
propertyValue = this.atomExpressionAt (++i);
i = eval.iToken;
}break;
case 1074790550:
this.setShapeId (17, ++i, idSeen);
i = eval.iToken;
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
if (this.isFloatParameter (i + 1)) {
var v = this.floatParameter (++i);
if (eval.theTok == 2) {
propertyName = "offsetPercent";
propertyValue = Integer.$valueOf (Clazz.floatToInt (v));
} else {
propertyName = "offset";
propertyValue = Float.$valueOf (v);
}} else {
propertyName = "offsetPt";
propertyValue = this.centerParameter (++i);
i = eval.iToken;
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
if (eval.theTok == 269484209 || JS.T.tokAttr (eval.theTok, 1073741824)) {
this.setShapeId (17, i, idSeen);
i = eval.iToken;
break;
}this.invArg ();
}
idSeen = (eval.theTok != 12291 && eval.theTok != 4102);
if (propertyName != null) this.setShapeProperty (17, propertyName, propertyValue);
}
if (iHaveCoord || iHaveAtoms) this.setShapeProperty (17, "set", null);
return true;
});
Clazz.defineMethod (c$, "draw", 
 function () {
var eval = this.e;
this.sm.loadShape (22);
switch (this.tokAt (1)) {
case 1073742001:
if (this.listIsosurface (22)) return false;
break;
case 1073742102:
var pt = 2;
var type = (this.tokAt (pt) == 1073742138 ? "" : this.e.optParameterAsString (pt));
if (type.equals ("chemicalShift")) type = "cs";
var scale = 1;
var index = 0;
if (type.length > 0) {
if (this.isFloatParameter (++pt)) index = this.intParameter (pt++);
}if (this.tokAt (pt) == 1073742138) scale = this.floatParameter (++pt);
if (!this.chk) this.e.runScript (this.vwr.getPointGroupAsString (true, type, index, scale));
return false;
case 137363467:
case 135270418:
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
var colorArgb = [-2147483648];
var intScale = 0;
var swidth = "";
var iptDisplayProperty = 0;
var center = null;
var thisId = this.initIsosurface (22);
var idSeen = (thisId != null);
var isWild = (idSeen && this.getShapeProperty (22, "ID") == null);
var connections = null;
var iConnect = 0;
for (var i = eval.iToken; i < this.slen; ++i) {
var propertyName = null;
var propertyValue = null;
switch (this.getToken (i).tok) {
case 1614417948:
case 1679429641:
if (this.chk) break;
var vp = this.vwr.getPlaneIntersection (eval.theTok, null, intScale / 100, 0);
intScale = 0;
propertyName = "polygon";
propertyValue = vp;
havePoints = true;
break;
case 4106:
connections =  Clazz.newIntArray (4, 0);
iConnect = 4;
var farray = eval.floatParameterSet (++i, 4, 4);
i = eval.iToken;
for (var j = 0; j < 4; j++) connections[j] = Clazz.floatToInt (farray[j]);

havePoints = true;
break;
case 1678770178:
case 1141899265:
if (connections == null || iConnect > (eval.theTok == 1095761924 ? 2 : 3)) {
iConnect = 0;
connections = [-1, -1, -1, -1];
}connections[iConnect++] = this.atomExpressionAt (++i).nextSetBit (0);
i = eval.iToken;
connections[iConnect++] = (eval.theTok == 1678770178 ? this.atomExpressionAt (++i).nextSetBit (0) : -1);
i = eval.iToken;
havePoints = true;
break;
case 554176565:
switch (this.getToken (++i).tok) {
case 1048582:
propertyName = "slab";
propertyValue = eval.objectNameParameter (++i);
i = eval.iToken;
havePoints = true;
break;
default:
this.invArg ();
}
break;
case 135267842:
switch (this.getToken (++i).tok) {
case 1614417948:
case 1679429641:
tokIntersect = eval.theTok;
isIntersect = true;
continue;
case 1048582:
propertyName = "intersect";
propertyValue = eval.objectNameParameter (++i);
i = eval.iToken;
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
var v =  new JU.Lst ();
var nVertices = 0;
var nTriangles = 0;
var points = null;
var vpolygons = null;
if (eval.isArrayParameter (++i)) {
points = eval.getPointArray (i, -1);
nVertices = points.length;
} else {
nVertices = Math.max (0, this.intParameter (i));
points =  new Array (nVertices);
for (var j = 0; j < nVertices; j++) points[j] = this.centerParameter (++eval.iToken);

}switch (this.getToken (++eval.iToken).tok) {
case 11:
case 12:
var sv = JS.SV.newT (eval.theToken);
sv.toArray ();
vpolygons = sv.getList ();
nTriangles = vpolygons.size ();
break;
case 7:
vpolygons = (eval.theToken).getList ();
nTriangles = vpolygons.size ();
break;
default:
nTriangles = Math.max (0, this.intParameter (eval.iToken));
}
var polygons = JU.AU.newInt2 (nTriangles);
for (var j = 0; j < nTriangles; j++) {
var f = (vpolygons == null ? eval.floatParameterSet (++eval.iToken, 3, 4) : JS.SV.flistValue (vpolygons.get (j), 0));
if (f.length < 3 || f.length > 4) this.invArg ();
polygons[j] = [Clazz.floatToInt (f[0]), Clazz.floatToInt (f[1]), Clazz.floatToInt (f[2]), (f.length == 3 ? 7 : Clazz.floatToInt (f[3]))];
}
if (nVertices > 0) {
v.addLast (points);
v.addLast (polygons);
} else {
v = null;
}propertyValue = v;
i = eval.iToken;
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
xyz = JS.SV.sValue (this.getToken (i));
break;
case 2:
default:
if (!eval.isCenterParameter (i)) iSym = this.intParameter (i++);
if (eval.isCenterParameter (i)) center = this.centerParameter (i);
if (eval.isCenterParameter (eval.iToken + 1)) target = this.centerParameter (++eval.iToken);
if (this.chk) return false;
i = eval.iToken;
}
var bsAtoms = null;
if (center == null && i + 1 < this.slen) {
center = this.centerParameter (++i);
bsAtoms = (this.tokAt (i) == 10 || this.tokAt (i) == 1048577 ? this.atomExpressionAt (i) : null);
i = eval.iToken + 1;
}eval.checkLast (eval.iToken);
if (!this.chk) {
var s = this.vwr.getSymmetryInfo (bsAtoms, xyz, iSym, center, target, thisId, 135176);
eval.runScript (s.length > 0 ? s : "draw ID \"sym_" + thisId + "*\" delete");
}return false;
case 4115:
isFrame = true;
continue;
case 1048586:
case 9:
case 8:
if (eval.theTok == 9 || !eval.isPoint3f (i)) {
propertyValue = this.getPoint4f (i);
if (isFrame) {
eval.checkLast (eval.iToken);
if (!this.chk) eval.runScript (JU.Escape.drawQuat (JU.Quat.newP4 (propertyValue), (thisId == null ? "frame" : thisId), " " + swidth, (center == null ?  new JU.P3 () : center), intScale / 100));
return false;
}propertyName = "planedef";
} else {
propertyValue = center = this.getPoint3f (i, true);
propertyName = "coord";
}i = eval.iToken;
havePoints = true;
break;
case 135267841:
case 135266319:
if (!havePoints && !isIntersect && tokIntersect == 0 && eval.theTok != 135267841) {
propertyName = "plane";
break;
}if (eval.theTok == 135266319) {
plane = eval.planeParameter (i);
} else {
plane = eval.hklParameter (++i);
}i = eval.iToken;
if (tokIntersect != 0) {
if (this.chk) break;
var vpc = this.vwr.getPlaneIntersection (tokIntersect, plane, intScale / 100, 0);
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
propertyValue = eval.floatParameterSet (++i, 0, 2147483647);
i = eval.iToken;
havePoints = true;
break;
case 10:
case 1048577:
propertyName = "atomSet";
propertyValue = this.atomExpressionAt (i);
if (isFrame) center = this.centerParameter (i);
i = eval.iToken;
havePoints = true;
break;
case 7:
propertyName = "modelBasedPoints";
propertyValue = JS.SV.strListValue (eval.theToken);
havePoints = true;
break;
case 1073742195:
case 269484080:
break;
case 269484096:
propertyValue = eval.xypParameter (i);
if (propertyValue != null) {
i = eval.iToken;
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
case 1095761935:
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
if (++i >= this.slen) this.error (34);
switch (this.getToken (i).tok) {
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
i = eval.iToken;
break;
case 1073742027:
propertyName = "fixed";
propertyValue = Boolean.FALSE;
break;
case 1060869:
propertyName = "fixed";
propertyValue = Boolean.TRUE;
break;
case 1073742066:
var pt = this.getPoint3f (++i, true);
i = eval.iToken;
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
case 1073741917:
var isRadius = (eval.theTok == 1666189314);
var f = this.floatParameter (++i);
if (isRadius) f *= 2;
propertyValue = Float.$valueOf (f);
propertyName = (isRadius || this.tokAt (i) == 3 ? "width" : "diameter");
swidth = propertyName + (this.tokAt (i) == 3 ? " " + f : " " + (Clazz.floatToInt (f)));
break;
case 1048582:
if ((this.tokAt (i + 2) == 269484096 || isFrame)) {
var pto = center = this.centerParameter (i);
i = eval.iToken;
propertyName = "coord";
propertyValue = pto;
havePoints = true;
break;
}propertyValue = eval.objectNameParameter (++i);
propertyName = "identifier";
havePoints = true;
break;
case 1766856708:
case 603979967:
case 1073742074:
idSeen = true;
translucentLevel = this.getColorTrans (eval, i, false, colorArgb);
i = eval.iToken;
continue;
default:
if (!eval.setMeshDisplayProperty (22, 0, eval.theTok)) {
if (eval.theTok == 269484209 || JS.T.tokAttr (eval.theTok, 1073741824)) {
thisId = this.setShapeId (22, i, idSeen);
i = eval.iToken;
break;
}this.invArg ();
}if (iptDisplayProperty == 0) iptDisplayProperty = i;
i = eval.iToken;
continue;
}
idSeen = (eval.theTok != 12291);
if (havePoints && !isInitialized && !isFrame) {
this.setShapeProperty (22, "points", Integer.$valueOf (intScale));
isInitialized = true;
intScale = 0;
}if (havePoints && isWild) this.invArg ();
if (propertyName != null) this.setShapeProperty (22, propertyName, propertyValue);
}
this.finalizeObject (22, colorArgb[0], translucentLevel, intScale, havePoints, connections, iptDisplayProperty, null);
return true;
});
Clazz.defineMethod (c$, "data", 
function () {
var eval = this.e;
var dataString = null;
var dataLabel = null;
var isOneValue = false;
var i;
switch (eval.iToken = this.slen) {
case 5:
dataString = this.paramAsStr (2);
case 4:
case 2:
dataLabel = this.paramAsStr (1);
if (dataLabel.equalsIgnoreCase ("clear")) {
if (!this.chk) this.vwr.setData (null, null, 0, 0, 0, 0, 0);
return;
}if ((i = dataLabel.indexOf ("@")) >= 0) {
dataString = "" + eval.getParameter (dataLabel.substring (i + 1), 4, true);
dataLabel = dataLabel.substring (0, i).trim ();
} else if (dataString == null && (i = dataLabel.indexOf (" ")) >= 0) {
dataString = dataLabel.substring (i + 1).trim ();
dataLabel = dataLabel.substring (0, i).trim ();
isOneValue = true;
}break;
default:
eval.bad ();
}
var dataType = dataLabel + " ";
dataType = dataType.substring (0, dataType.indexOf (" ")).toLowerCase ();
if (dataType.equals ("model") || dataType.equals ("append")) {
eval.cmdLoad ();
return;
}if (this.chk) return;
var isDefault = (dataLabel.toLowerCase ().indexOf ("(default)") >= 0);
if (dataType.equals ("connect_atoms")) {
this.vwr.connect (this.parseDataArray (dataString, false));
return;
}if (dataType.indexOf ("ligand_") == 0) {
this.vwr.setLigandModel (dataLabel.substring (7).toUpperCase () + "_data", dataString.trim ());
return;
}if (dataType.indexOf ("file_") == 0) {
this.vwr.setLigandModel (dataLabel.substring (5) + "_file", dataString.trim ());
return;
}var d = this.lastData =  new Array (4);
if (dataType.equals ("element_vdw")) {
d[0] = dataType;
d[1] = dataString.$replace (';', '\n');
var n = JU.Elements.elementNumberMax;
var eArray =  Clazz.newIntArray (n + 1, 0);
for (var ie = 1; ie <= n; ie++) eArray[ie] = ie;

d[2] = eArray;
d[3] = Integer.$valueOf (0);
this.vwr.setData ("element_vdw", d, n, 0, 0, 0, 0);
return;
}if (dataType.indexOf ("data2d_") == 0) {
d[0] = dataLabel;
d[1] = this.parseDataArray (dataString, false);
d[3] = Integer.$valueOf (2);
this.vwr.setData (dataLabel, d, 0, 0, 0, 0, 0);
return;
}if (dataType.indexOf ("data3d_") == 0) {
d[0] = dataLabel;
d[1] = this.parseDataArray (dataString, true);
d[3] = Integer.$valueOf (3);
this.vwr.setData (dataLabel, d, 0, 0, 0, 0, 0);
return;
}var tokens = JU.PT.getTokens (dataLabel);
if (dataType.indexOf ("property_") == 0 && !(tokens.length == 2 && tokens[1].equals ("set"))) {
var bs = this.vwr.bsA ();
d[0] = dataType;
var atomNumberField = (isOneValue ? 0 : (this.vwr.getP ("propertyAtomNumberField")).intValue ());
var atomNumberFieldColumnCount = (isOneValue ? 0 : (this.vwr.getP ("propertyAtomNumberColumnCount")).intValue ());
var propertyField = (isOneValue ? -2147483648 : (this.vwr.getP ("propertyDataField")).intValue ());
var propertyFieldColumnCount = (isOneValue ? 0 : (this.vwr.getP ("propertyDataColumnCount")).intValue ());
if (!isOneValue && dataLabel.indexOf (" ") >= 0) {
if (tokens.length == 3) {
dataLabel = tokens[0];
atomNumberField = JU.PT.parseInt (tokens[1]);
propertyField = JU.PT.parseInt (tokens[2]);
}if (tokens.length == 5) {
dataLabel = tokens[0];
atomNumberField = JU.PT.parseInt (tokens[1]);
atomNumberFieldColumnCount = JU.PT.parseInt (tokens[2]);
propertyField = JU.PT.parseInt (tokens[3]);
propertyFieldColumnCount = JU.PT.parseInt (tokens[4]);
}}if (atomNumberField < 0) atomNumberField = 0;
if (propertyField < 0) propertyField = 0;
var ac = this.vwr.getAtomCount ();
var atomMap = null;
var bsTemp = JU.BS.newN (ac);
if (atomNumberField > 0) {
atomMap =  Clazz.newIntArray (ac + 2, 0);
for (var j = 0; j <= ac; j++) atomMap[j] = -1;

for (var j = bs.nextSetBit (0); j >= 0; j = bs.nextSetBit (j + 1)) {
var atomNo = this.vwr.getAtomNumber (j);
if (atomNo > ac + 1 || atomNo < 0 || bsTemp.get (atomNo)) continue;
bsTemp.set (atomNo);
atomMap[atomNo] = j;
}
d[2] = atomMap;
} else {
d[2] = JU.BSUtil.copy (bs);
}d[1] = dataString;
d[3] = Integer.$valueOf (0);
this.vwr.setData (dataType, d, ac, atomNumberField, atomNumberFieldColumnCount, propertyField, propertyFieldColumnCount);
return;
}var userType = JM.AtomCollection.getUserSettableType (dataType);
if (userType >= 0) {
this.vwr.setAtomData (userType, dataType, dataString, isDefault);
return;
}d[0] = dataLabel;
d[1] = dataString;
d[3] = Integer.$valueOf (0);
this.vwr.setData (dataType, d, 0, 0, 0, 0, 0);
});
Clazz.defineMethod (c$, "ellipsoid", 
 function () {
var eval = this.e;
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
this.setShapeProperty (20, "select", this.paramAsStr (2));
i = eval.iToken;
checkMore = true;
isSet = true;
break;
case 1074790550:
case 269484209:
case 1073741824:
this.sm.loadShape (20);
if (eval.theTok == 1074790550) i++;
this.setShapeId (20, i, false);
i = eval.iToken;
checkMore = true;
break;
default:
this.invArg ();
}
if (!checkMore) {
eval.setShapeSizeBs (20, mad, null);
return;
}var colorArgb = [-2147483648];
while (++i < this.slen) {
var key = this.paramAsStr (i);
var value = null;
this.getToken (i);
if (!isSet) switch (eval.theTok) {
case 1048582:
key = "points";
var data =  new Array (3);
data[0] = eval.objectNameParameter (++i);
if (this.chk) continue;
eval.getShapePropertyData (24, "getVertices", data);
value = data;
break;
case 1611272194:
var axes =  new Array (3);
for (var j = 0; j < 3; j++) {
axes[j] =  new JU.V3 ();
axes[j].setT (this.centerParameter (++i));
i = eval.iToken;
}
value = axes;
break;
case 12289:
value = this.centerParameter (++i);
i = eval.iToken;
break;
case 1095761935:
value = Integer.$valueOf (this.intParameter (++i));
break;
case 12291:
value = Boolean.TRUE;
this.checkLength (i + 1);
break;
}
if (value == null) switch (eval.theTok) {
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
i = eval.iToken;
break;
case 1766856708:
case 603979967:
case 1073742074:
translucentLevel = this.getColorTrans (eval, i, true, colorArgb);
i = eval.iToken;
continue;
case 1073742075:
value = this.paramAsStr (++i);
break;
}
if (value == null) this.invArg ();
this.setShapeProperty (20, key.toLowerCase (), value);
}
this.finalizeObject (20, colorArgb[0], translucentLevel, 0, false, null, 0, null);
this.setShapeProperty (20, "thisID", null);
});
Clazz.defineMethod (c$, "isosurface", 
 function (iShape) {
var eval = this.e;
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
var isFrontOnly = false;
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
var sbCommand =  new JU.SB ();
var pt;
var plane = null;
var lattice = null;
var pts;
var color = 0;
var str = null;
var modelIndex = (this.chk ? 0 : -2147483648);
eval.setCursorWait (true);
var idSeen = (this.initIsosurface (iShape) != null);
var isWild = (idSeen && this.getShapeProperty (iShape, "ID") == null);
var isColorSchemeTranslucent = false;
var isInline = false;
var onlyOneModel = null;
var translucency = null;
var colorScheme = null;
var mepOrMlp = null;
var symops = null;
var discreteColixes = null;
var propertyList =  new JU.Lst ();
var defaultMesh = false;
if (isPmesh || isPlot3d) this.addShapeProperty (propertyList, "fileType", "Pmesh");
for (var i = eval.iToken; i < this.slen; ++i) {
var propertyName = null;
var propertyValue = null;
this.getToken (i);
if (eval.theTok == 1073741824) str = this.paramAsStr (i);
switch (eval.theTok) {
case 603979871:
smoothing = (this.getToken (++i).tok == 1048589 ? Boolean.TRUE : eval.theTok == 1048588 ? Boolean.FALSE : null);
if (smoothing == null) this.invArg ();
continue;
case 553648149:
smoothingPower = this.intParameter (++i);
continue;
case 4128:
propertyName = "moveIsosurface";
if (this.tokAt (++i) != 12) this.invArg ();
propertyValue = this.getToken (i++).value;
break;
case 1297090050:
var ff = this.floatArraySet (i + 2, this.intParameter (i + 1), 16);
symops =  new Array (ff.length);
for (var j = symops.length; --j >= 0; ) symops[j] = JU.M4.newA16 (ff[j]);

i = eval.iToken;
break;
case 1089470478:
if (modelIndex < 0) modelIndex = Math.min (this.vwr.am.cmi, 0);
var needIgnore = (bsIgnore == null);
if (bsSelect == null) bsSelect = JU.BSUtil.copy (this.vwr.bsA ());
bsSelect.and (this.vwr.ms.getAtoms (1297090050, Integer.$valueOf (1)));
if (!needIgnore) bsSelect.andNot (bsIgnore);
this.addShapeProperty (propertyList, "select", bsSelect);
if (needIgnore) {
bsIgnore = JU.BSUtil.copy (bsSelect);
JU.BSUtil.invertInPlace (bsIgnore, this.vwr.getAtomCount ());
isFrontOnly = true;
this.addShapeProperty (propertyList, "ignore", bsIgnore);
sbCommand.append (" ignore ").append (JU.Escape.eBS (bsIgnore));
}sbCommand.append (" symmetry");
if (color == 0) this.addShapeProperty (propertyList, "colorRGB", Integer.$valueOf (1297090050));
symops = this.vwr.ms.getSymMatrices (modelIndex);
break;
case 1073742066:
propertyName = "offset";
propertyValue = this.centerParameter (++i);
i = eval.iToken;
break;
case 528432:
propertyName = "rotate";
propertyValue = (this.tokAt (eval.iToken = ++i) == 1048587 ? null : this.getPoint4f (i));
i = eval.iToken;
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
propertyName = eval.theToken.value.toString ();
sbCommand.append (" ").appendO (eval.theToken.value);
propertyValue = this.centerParameter (++i);
sbCommand.append (" ").append (JU.Escape.eP (propertyValue));
i = eval.iToken;
break;
case 1679429641:
if (this.fullCommand.indexOf ("# BBOX=") >= 0) {
var bbox = JU.PT.split (JU.PT.getQuotedAttribute (this.fullCommand, "# BBOX"), ",");
pts = [JU.Escape.uP (bbox[0]), JU.Escape.uP (bbox[1])];
} else if (eval.isCenterParameter (i + 1)) {
pts = [this.getPoint3f (i + 1, true), this.getPoint3f (eval.iToken + 1, true)];
i = eval.iToken;
} else {
pts = this.vwr.ms.getBBoxVertices ();
}sbCommand.append (" boundBox " + JU.Escape.eP (pts[0]) + " " + JU.Escape.eP (pts[pts.length - 1]));
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
bs =  new JU.BS ();
} else if (this.tokAt (eval.iToken + 1) == 1048577 || this.tokAt (eval.iToken + 1) == 10) {
bs = this.atomExpressionAt (++eval.iToken);
bs.and (this.vwr.ms.getAtomsWithinRadius (5.0, bsSelect, false, null));
} else {
bs = this.vwr.ms.getAtomsWithinRadius (5.0, bsSelect, true, null);
bs.andNot (this.vwr.ms.getAtoms (1095761936, bsSelect));
}bs.andNot (bsSelect);
sbCommand.append (" intersection ").append (JU.Escape.eBS (bsSelect)).append (" ").append (JU.Escape.eBS (bs));
i = eval.iToken;
if (this.tokAt (i + 1) == 135368713) {
i++;
var f = this.getToken (++i).value;
sbCommand.append (" function ").append (JU.PT.esc (f));
if (!this.chk) this.addShapeProperty (propertyList, "func", (f.equals ("a+b") || f.equals ("a-b") ? f : this.createFunction ("__iso__", "a,b", f)));
} else {
haveIntersection = true;
}propertyName = "intersection";
propertyValue = [bsSelect, bs];
break;
case 1610625028:
case 135266325:
var isDisplay = (eval.theTok == 1610625028);
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
sbCommand.append (" ").append (JU.Escape.eBS (propertyValue));
}eval.checkLast (i);
break;
} else if (tok != 135266325) {
eval.iToken = i;
this.invArg ();
}} else {
ptWithin = i;
}var distance;
var ptc = null;
bs = null;
var havePt = false;
if (this.tokAt (i + 1) == 1048577) {
distance = this.floatParameter (i + 3);
if (eval.isPoint3f (i + 4)) {
ptc = this.centerParameter (i + 4);
havePt = true;
eval.iToken = eval.iToken + 2;
} else if (eval.isPoint3f (i + 5)) {
ptc = this.centerParameter (i + 5);
havePt = true;
eval.iToken = eval.iToken + 2;
} else {
bs = eval.atomExpression (this.st, i + 5, this.slen, true, false, false, true);
if (bs == null) this.invArg ();
}} else {
distance = this.floatParameter (++i);
ptc = this.centerParameter (++i);
}if (isDisplay) eval.checkLast (eval.iToken);
i = eval.iToken;
if (this.fullCommand.indexOf ("# WITHIN=") >= 0) bs = JU.BS.unescape (JU.PT.getQuotedAttribute (this.fullCommand, "# WITHIN"));
 else if (!havePt) bs = (Clazz.instanceOf (eval.expressionResult, JU.BS) ? eval.expressionResult : null);
if (!this.chk) {
if (bs != null && modelIndex >= 0) {
bs.and (this.vwr.getModelUndeletedAtomsBitSet (modelIndex));
}if (ptc == null) ptc = (bs == null ?  new JU.P3 () : this.vwr.ms.getAtomSetCenter (bs));
this.getWithinDistanceVector (propertyList, distance, ptc, bs, isDisplay);
sbCommand.append (" within ").appendF (distance).append (" ").append (bs == null ? JU.Escape.eP (ptc) : JU.Escape.eBS (bs));
}continue;
case 1073742083:
propertyName = "parameters";
var fparams = eval.floatParameterSet (++i, 1, 10);
i = eval.iToken;
propertyValue = fparams;
sbCommand.append (" parameters ").append (JU.Escape.eAF (fparams));
break;
case 1716520985:
case 1073742190:
onlyOneModel = eval.theToken.value;
var isVariable = (eval.theTok == 1073742190);
var tokProperty = this.tokAt (i + 1);
if (mepOrMlp == null) {
if (!surfaceObjectSeen && !isMapped && !planeSeen) {
this.addShapeProperty (propertyList, "sasurface", Float.$valueOf (0));
sbCommand.append (" vdw");
surfaceObjectSeen = true;
}propertyName = "property";
if (smoothing == null) {
var allowSmoothing = JS.T.tokAttr (tokProperty, 1112539136);
smoothing = (allowSmoothing && this.vwr.getIsosurfacePropertySmoothing (false) == 1 ? Boolean.TRUE : Boolean.FALSE);
}this.addShapeProperty (propertyList, "propertySmoothing", smoothing);
sbCommand.append (" isosurfacePropertySmoothing " + smoothing);
if (smoothing === Boolean.TRUE) {
if (smoothingPower == 2147483647) smoothingPower = this.vwr.getIsosurfacePropertySmoothing (true);
this.addShapeProperty (propertyList, "propertySmoothingPower", Integer.$valueOf (smoothingPower));
sbCommand.append (" isosurfacePropertySmoothingPower " + smoothingPower);
}if (this.vwr.g.rangeSelected) this.addShapeProperty (propertyList, "rangeSelected", Boolean.TRUE);
} else {
propertyName = mepOrMlp;
}str = this.paramAsStr (i);
sbCommand.append (" ").append (str);
if (str.toLowerCase ().indexOf ("property_") == 0) {
data =  Clazz.newFloatArray (this.vwr.getAtomCount (), 0);
if (this.chk) continue;
data = this.vwr.getDataFloat (str);
if (data == null) this.invArg ();
this.addShapeProperty (propertyList, propertyName, data);
continue;
}var ac = this.vwr.getAtomCount ();
data =  Clazz.newFloatArray (ac, 0);
if (isVariable) {
var vname = this.paramAsStr (++i);
if (vname.length == 0) {
data = eval.floatParameterSet (i, ac, ac);
} else {
data =  Clazz.newFloatArray (ac, 0);
if (!this.chk) JU.Parser.parseStringInfestedFloatArray ("" + eval.getParameter (vname, 4, true), null, data);
}if (!this.chk) sbCommand.append (" \"\" ").append (JU.Escape.eAF (data));
} else {
this.getToken (++i);
if (!this.chk) {
sbCommand.append (" " + eval.theToken.value);
var atoms = this.vwr.ms.at;
this.vwr.autoCalculate (tokProperty);
if (tokProperty != 1766856708) {
pt =  new JU.P3 ();
for (var iAtom = ac; --iAtom >= 0; ) data[iAtom] = JM.Atom.atomPropertyFloat (this.vwr, atoms[iAtom], tokProperty, pt);

}}if (tokProperty == 1766856708) colorScheme = "inherit";
if (this.tokAt (i + 1) == 135266325) {
var d = this.floatParameter (i = i + 2);
sbCommand.append (" within " + d);
this.addShapeProperty (propertyList, "propertyDistanceMax", Float.$valueOf (d));
}}propertyValue = data;
break;
case 1095761935:
case 1095766030:
if (surfaceObjectSeen) this.invArg ();
modelIndex = (eval.theTok == 1095761935 ? this.intParameter (++i) : eval.modelNumberParameter (++i));
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
i = eval.iToken;
var isOnly = (this.tokAt (i + 1) == 1073742072);
if (isOnly) {
i++;
bsIgnore = JU.BSUtil.copy (bs1);
JU.BSUtil.invertInPlace (bsIgnore, this.vwr.getAtomCount ());
this.addShapeProperty (propertyList, "ignore", bsIgnore);
sbCommand.append (" ignore ").append (JU.Escape.eBS (bsIgnore));
isFrontOnly = true;
}if (surfaceObjectSeen || isMapped) {
sbCommand.append (" select " + JU.Escape.eBS (bs1));
} else {
bsSelect = propertyValue;
if (modelIndex < 0 && bsSelect.nextSetBit (0) >= 0) modelIndex = this.vwr.getAtomModelIndex (bsSelect.nextSetBit (0));
}break;
case 1085443:
thisSetNumber = this.intParameter (++i);
break;
case 12289:
propertyName = "center";
propertyValue = this.centerParameter (++i);
sbCommand.append (" center " + JU.Escape.eP (propertyValue));
i = eval.iToken;
break;
case 1073742147:
case 1766856708:
idSeen = true;
var isSign = (eval.theTok == 1073742147);
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
}if (this.getToken (i + 1).tok == 4) {
colorScheme = this.paramAsStr (++i);
if (colorScheme.indexOf (" ") > 0) {
discreteColixes = JU.C.getColixArray (colorScheme);
if (discreteColixes == null) this.error (4);
}} else if (eval.theTok == 1073742018) {
i++;
sbCommand.append (" color mesh");
color = eval.getArgbParam (++i);
this.addShapeProperty (propertyList, "meshcolor", Integer.$valueOf (color));
sbCommand.append (" ").append (JU.Escape.escapeColor (color));
i = eval.iToken;
continue;
}if ((eval.theTok = this.tokAt (i + 1)) == 603979967 || eval.theTok == 1073742074) {
sbCommand.append (" color");
translucency = this.setColorOptions (sbCommand, i + 1, 24, -2);
i = eval.iToken;
continue;
}switch (this.tokAt (i + 1)) {
case 1073741826:
case 1073742114:
this.getToken (++i);
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
if (eval.isColorParam (i + 1)) {
color = eval.getArgbParam (i + 1);
if (this.tokAt (i + 2) == 1074790746) {
colorScheme = eval.getColorRange (i + 1);
i = eval.iToken;
break;
}}sbCommand.append (" color");
}if (eval.isColorParam (i + 1)) {
color = eval.getArgbParam (++i);
sbCommand.append (" ").append (JU.Escape.escapeColor (color));
i = eval.iToken;
this.addShapeProperty (propertyList, "colorRGB", Integer.$valueOf (color));
idSeen = true;
if (eval.isColorParam (i + 1)) {
color = eval.getArgbParam (++i);
i = eval.iToken;
this.addShapeProperty (propertyList, "colorRGB", Integer.$valueOf (color));
sbCommand.append (" ").append (JU.Escape.escapeColor (color));
isBicolor = true;
} else if (isSign) {
this.invPO ();
}} else if (!isSign && discreteColixes == null) {
this.invPO ();
}continue;
case 135270423:
if (!isIsosurface) this.invArg ();
toCache = !this.chk;
continue;
case 1229984263:
if (this.tokAt (i + 1) != 4) this.invPO ();
continue;
case 1112541195:
case 1649412120:
sbCommand.append (" ").appendO (eval.theToken.value);
var rd = eval.encodeRadiusParameter (i, false, true);
if (rd == null) return false;
sbCommand.append (" ").appendO (rd);
if (Float.isNaN (rd.value)) rd.value = 100;
propertyValue = rd;
propertyName = "radius";
haveRadius = true;
if (isMapped) surfaceObjectSeen = false;
i = eval.iToken;
break;
case 135266319:
planeSeen = true;
propertyName = "plane";
propertyValue = eval.planeParameter (i);
i = eval.iToken;
sbCommand.append (" plane ").append (JU.Escape.eP4 (propertyValue));
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
i = eval.iToken;
sbCommand.append (" ellipsoid ").append (JU.Escape.eP4 (propertyValue));
break;
case 135267841:
planeSeen = true;
propertyName = "plane";
propertyValue = eval.hklParameter (++i);
i = eval.iToken;
sbCommand.append (" plane ").append (JU.Escape.eP4 (propertyValue));
break;
case 135182:
surfaceObjectSeen = true;
var lcaoType = this.paramAsStr (++i);
this.addShapeProperty (propertyList, "lcaoType", lcaoType);
sbCommand.append (" lcaocartoon ").append (JU.PT.esc (lcaoType));
switch (this.getToken (++i).tok) {
case 10:
case 1048577:
propertyName = "lcaoCartoon";
bs = this.atomExpressionAt (i);
i = eval.iToken;
if (this.chk) continue;
var atomIndex = bs.nextSetBit (0);
if (atomIndex < 0) this.error (14);
sbCommand.append (" ({").appendI (atomIndex).append ("})");
modelIndex = this.vwr.getAtomModelIndex (atomIndex);
this.addShapeProperty (propertyList, "modelIndex", Integer.$valueOf (modelIndex));
var axes = [ new JU.V3 (),  new JU.V3 (), JU.V3.newV (this.vwr.getAtomPoint3f (atomIndex)),  new JU.V3 ()];
if (!lcaoType.equalsIgnoreCase ("s") && this.vwr.getHybridizationAndAxes (atomIndex, axes[0], axes[1], lcaoType) == null) return false;
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
eval.bad ();
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
i = eval.iToken;
sbCommand.append (" mo " + (isNegOffset ? "-" : "") + "HOMO ");
if (offset > 0) sbCommand.append ("+");
if (offset != 0) sbCommand.appendI (offset);
break;
case 2:
moNumber = this.intParameter (i);
sbCommand.append (" mo ").appendI (moNumber);
break;
default:
if (eval.isArrayParameter (i)) {
linearCombination = eval.floatParameterSet (i, 1, 2147483647);
i = eval.iToken;
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
var isMep = (eval.theTok == 1073742016);
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
sbCommand.append (" /*file*/" + JU.PT.esc (fname));
} else if (this.tokAt (i + 1) == 1716520985) {
mepOrMlp = propertyName;
continue;
}if (!this.chk) try {
data = (fname == null && isMep ? this.vwr.getPartialCharges () : this.getAtomicPotentials (bsSelect, bsIgnore, fname));
} catch (ex) {
if (Clazz.exceptionOf (ex, Exception)) {
} else {
throw ex;
}
}
if (!this.chk && data == null) this.error (32);
propertyValue = data;
break;
case 1313866249:
doCalcVolume = !this.chk;
sbCommand.append (" volume");
break;
case 1074790550:
this.setShapeId (iShape, ++i, idSeen);
isWild = (this.getShapeProperty (iShape, "ID") == null);
i = eval.iToken;
break;
case 1073741888:
if (this.tokAt (i + 1) == 603979967) {
isColorSchemeTranslucent = true;
i++;
}colorScheme = this.paramAsStr (++i).toLowerCase ();
if (colorScheme.equals ("sets")) {
sbCommand.append (" colorScheme \"sets\"");
} else if (eval.isColorParam (i)) {
colorScheme = eval.getColorRange (i);
i = eval.iToken;
}break;
case 1073741828:
propertyName = "addHydrogens";
propertyValue = Boolean.TRUE;
sbCommand.append (" mp.addHydrogens");
break;
case 1073741836:
propertyName = "angstroms";
sbCommand.append (" angstroms");
break;
case 1073741837:
propertyName = "anisotropy";
propertyValue = this.getPoint3f (++i, false);
sbCommand.append (" anisotropy").append (JU.Escape.eP (propertyValue));
i = eval.iToken;
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
propertyName = eval.theToken.value;
propertyValue = this.getCapSlabObject (i, false);
i = eval.iToken;
break;
case 1073741876:
if (!isIsosurface) this.invArg ();
isCavity = true;
if (this.chk) continue;
var cavityRadius = (this.isFloatParameter (i + 1) ? this.floatParameter (++i) : 1.2);
var envelopeRadius = (this.isFloatParameter (i + 1) ? this.floatParameter (++i) : 10);
if (envelopeRadius > 10) {
eval.integerOutOfRange (0, 10);
return false;
}sbCommand.append (" cavity ").appendF (cavityRadius).append (" ").appendF (envelopeRadius);
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
propertyValue = eval.floatParameterSet (i + 2, 1, 2147483647);
sbCommand.append (" discrete ").append (JU.Escape.eAF (propertyValue));
i = eval.iToken;
break;
case 1073741981:
pt = this.getPoint3f (i + 2, false);
if (pt.z <= 0 || pt.y < pt.x) this.invArg ();
if (pt.z == Clazz.floatToInt (pt.z) && pt.z > (pt.y - pt.x)) pt.z = (pt.y - pt.x) / pt.z;
propertyValue = pt;
i = eval.iToken;
sbCommand.append (" increment ").append (JU.Escape.eP (pt));
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
if (eval.theTok == 1073741910) i++;
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
propertyValue = eval.floatParameterSet (i, 2, 2);
this.addShapeProperty (propertyList, "cutoff", Float.$valueOf (0));
sbCommand.append (JU.Escape.eAF (propertyValue));
i = eval.iToken;
}break;
case 1073741928:
propertyName = "downsample";
propertyValue = Integer.$valueOf (this.intParameter (++i));
sbCommand.append (" downsample ").appendO (propertyValue);
break;
case 1073741930:
propertyName = "eccentricity";
propertyValue = this.getPoint4f (++i);
sbCommand.append (" eccentricity ").append (JU.Escape.eP4 (propertyValue));
i = eval.iToken;
break;
case 1074790508:
sbCommand.append (" ed");
this.setMoData (propertyList, -1, null, 0, false, modelIndex, null);
surfaceObjectSeen = true;
continue;
case 536870916:
case 1073742041:
sbCommand.append (" ").appendO (eval.theToken.value);
propertyName = "debug";
propertyValue = (eval.theTok == 536870916 ? Boolean.TRUE : Boolean.FALSE);
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
var isFxyz = (eval.theTok == 1073741968);
propertyName = "" + eval.theToken.value;
var vxy =  new JU.Lst ();
propertyValue = vxy;
isFxy = surfaceObjectSeen = true;
sbCommand.append (" ").append (propertyName);
var name = this.paramAsStr (++i);
if (name.equals ("=")) {
sbCommand.append (" =");
name = this.paramAsStr (++i);
sbCommand.append (" ").append (JU.PT.esc (name));
vxy.addLast (name);
if (!this.chk) this.addShapeProperty (propertyList, "func", this.createFunction ("__iso__", "x,y,z", name));
break;
}var dName = JU.PT.getQuotedAttribute (this.fullCommand, "# DATA" + (isFxy ? "2" : ""));
if (dName == null) dName = "inline";
 else name = dName;
var isXYZ = (name.indexOf ("data2d_") == 0);
var isXYZV = (name.indexOf ("data3d_") == 0);
isInline = name.equals ("inline");
sbCommand.append (" inline");
vxy.addLast (name);
var pt3 = this.getPoint3f (++i, false);
sbCommand.append (" ").append (JU.Escape.eP (pt3));
vxy.addLast (pt3);
var pt4;
ptX = ++eval.iToken;
vxy.addLast (pt4 = this.getPoint4f (ptX));
sbCommand.append (" ").append (JU.Escape.eP4 (pt4));
nX = Clazz.floatToInt (pt4.x);
ptY = ++eval.iToken;
vxy.addLast (pt4 = this.getPoint4f (ptY));
sbCommand.append (" ").append (JU.Escape.eP4 (pt4));
nY = Clazz.floatToInt (pt4.x);
vxy.addLast (pt4 = this.getPoint4f (++eval.iToken));
sbCommand.append (" ").append (JU.Escape.eP4 (pt4));
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
xyzdata = this.floatArraySetXYZ (++eval.iToken, nX, nY, nZ);
} else if (isXYZV) {
xyzdata = this.vwr.getDataFloat3D (name);
} else {
xyzdata = this.vwr.functionXYZ (name, nX, nY, nZ);
}nX = Math.abs (nX);
nY = Math.abs (nY);
nZ = Math.abs (nZ);
if (xyzdata == null) {
eval.iToken = ptX;
eval.errorStr (53, "xyzdata is null.");
}if (xyzdata.length != nX || xyzdata[0].length != nY || xyzdata[0][0].length != nZ) {
eval.iToken = ptX;
eval.errorStr (53, "xyzdata[" + xyzdata.length + "][" + xyzdata[0].length + "][" + xyzdata[0][0].length + "] is not of size [" + nX + "][" + nY + "][" + nZ + "]");
}vxy.addLast (xyzdata);
sbCommand.append (" ").append (JU.Escape.e (xyzdata));
} else {
if (isInline) {
nX = Math.abs (nX);
nY = Math.abs (nY);
fdata = this.floatArraySet (++eval.iToken, nX, nY);
} else if (isXYZ) {
fdata = this.vwr.getDataFloat2D (name);
nX = (fdata == null ? 0 : fdata.length);
nY = 3;
} else {
fdata = this.vwr.functionXY (name, nX, nY);
nX = Math.abs (nX);
nY = Math.abs (nY);
}if (fdata == null) {
eval.iToken = ptX;
eval.errorStr (53, "fdata is null.");
}if (fdata.length != nX && !isXYZ) {
eval.iToken = ptX;
eval.errorStr (53, "fdata length is not correct: " + fdata.length + " " + nX + ".");
}for (var j = 0; j < nX; j++) {
if (fdata[j] == null) {
eval.iToken = ptY;
eval.errorStr (53, "fdata[" + j + "] is null.");
}if (fdata[j].length != nY) {
eval.iToken = ptY;
eval.errorStr (53, "fdata[" + j + "] is not the right length: " + fdata[j].length + " " + nY + ".");
}}
vxy.addLast (fdata);
sbCommand.append (" ").append (JU.Escape.e (fdata));
}}i = eval.iToken;
break;
case 1073741970:
propertyName = "gridPoints";
sbCommand.append (" gridPoints");
break;
case 1073741976:
propertyName = "ignore";
propertyValue = bsIgnore = this.atomExpressionAt (++i);
sbCommand.append (" ignore ").append (JU.Escape.eBS (bsIgnore));
i = eval.iToken;
break;
case 1073741984:
propertyName = "insideOut";
sbCommand.append (" insideout");
break;
case 1073741988:
case 1073741986:
case 1073742100:
sbCommand.append (" ").appendO (eval.theToken.value);
propertyName = "pocket";
propertyValue = (eval.theTok == 1073742100 ? Boolean.TRUE : Boolean.FALSE);
break;
case 1073742002:
propertyName = "lobe";
propertyValue = this.getPoint4f (++i);
i = eval.iToken;
sbCommand.append (" lobe ").append (JU.Escape.eP4 (propertyValue));
surfaceObjectSeen = true;
break;
case 1073742004:
case 1073742006:
propertyName = "lp";
propertyValue = this.getPoint4f (++i);
i = eval.iToken;
sbCommand.append (" lp ").append (JU.Escape.eP4 (propertyValue));
surfaceObjectSeen = true;
break;
case 1052700:
if (isMapped || this.slen == i + 1) this.invArg ();
isMapped = true;
if ((isCavity || haveRadius || haveIntersection) && !surfaceObjectSeen) {
surfaceObjectSeen = true;
this.addShapeProperty (propertyList, "bsSolvent", (haveRadius || haveIntersection ?  new JU.BS () : eval.lookupIdentifierValue ("solvent")));
this.addShapeProperty (propertyList, "sasurface", Float.$valueOf (0));
}if (sbCommand.length () == 0) {
plane = this.getShapeProperty (24, "plane");
if (plane == null) {
if (this.getShapeProperty (24, "contours") != null) {
this.addShapeProperty (propertyList, "nocontour", null);
}} else {
this.addShapeProperty (propertyList, "plane", plane);
sbCommand.append ("plane ").append (JU.Escape.eP4 (plane));
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
i = eval.iToken;
sbCommand.append (" radical ").append (JU.Escape.eP4 (propertyValue));
break;
case 1073742027:
propertyName = "fixed";
propertyValue = Boolean.FALSE;
sbCommand.append (" modelBased");
break;
case 1073742028:
case 1073742135:
case 1613758488:
onlyOneModel = eval.theToken.value;
var radius;
if (eval.theTok == 1073742028) {
propertyName = "molecular";
sbCommand.append (" molecular");
radius = (this.isFloatParameter (i + 1) ? this.floatParameter (++i) : 1.4);
} else {
this.addShapeProperty (propertyList, "bsSolvent", eval.lookupIdentifierValue ("solvent"));
propertyName = (eval.theTok == 1073742135 ? "sasurface" : "solvent");
sbCommand.append (" ").appendO (eval.theToken.value);
radius = (this.isFloatParameter (i + 1) ? this.floatParameter (++i) : this.vwr.getFloat (570425394));
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
sbCommand.append (" phase ").append (JU.PT.esc (propertyValue));
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
if (isPmesh) str = JU.PT.replaceWithCharacter (str, "{,}|", ' ');
if (eval.debugHigh) JU.Logger.debug ("pmesh inline data:\n" + str);
propertyValue = (this.chk ? null : str);
this.addShapeProperty (propertyList, "fileName", "");
sbCommand.append (" INLINE ").append (JU.PT.esc (str));
surfaceObjectSeen = true;
break;
case 4:
var firstPass = (!surfaceObjectSeen && !planeSeen);
propertyName = (firstPass && !isMapped ? "readFile" : "mapColor");
var filename = this.paramAsStr (i);
if (filename.startsWith ("=") && filename.length > 1) {
var info = this.vwr.setLoadFormat (filename, '_', false);
filename = info[0];
var strCutoff = (!firstPass || !Float.isNaN (cutoff) ? null : info[1]);
if (strCutoff != null && !this.chk) {
cutoff = NaN;
try {
var sfdat = this.vwr.getFileAsString (strCutoff, false);
JU.Logger.info (sfdat);
sfdat = JU.PT.split (sfdat, "MAP_SIGMA_DENS")[1];
cutoff = JU.PT.parseFloat (sfdat);
this.showString ("using cutoff = " + cutoff);
} catch (e) {
if (Clazz.exceptionOf (e, Exception)) {
JU.Logger.error ("MAP_SIGMA_DENS -- could  not read " + info[1]);
} else {
throw e;
}
}
if (cutoff > 0) {
if (!Float.isNaN (sigma)) {
cutoff *= sigma;
sigma = NaN;
this.addShapeProperty (propertyList, "sigma", Float.$valueOf (sigma));
}this.addShapeProperty (propertyList, "cutoff", Float.$valueOf (cutoff));
sbCommand.append (" cutoff ").appendF (cutoff);
}}if (ptWithin == 0) {
onlyOneModel = "=xxxx";
if (modelIndex < 0) modelIndex = this.vwr.am.cmi;
bs = this.vwr.getModelUndeletedAtomsBitSet (modelIndex);
if (bs.nextSetBit (0) >= 0) {
this.getWithinDistanceVector (propertyList, 2.0, null, bs, false);
sbCommand.append (" within 2.0 ").append (JU.Escape.eBS (bs));
}}if (firstPass) defaultMesh = true;
}if (firstPass && this.vwr.getP ("_fileType").equals ("Pdb") && Float.isNaN (sigma) && Float.isNaN (cutoff)) {
this.addShapeProperty (propertyList, "sigma", Float.$valueOf (-1));
sbCommand.append (" sigma -1.0");
}if (filename.length == 0) {
if (modelIndex < 0) modelIndex = this.vwr.am.cmi;
filename = eval.getFullPathName ();
propertyValue = this.vwr.getModelAuxiliaryInfoValue (modelIndex, "jmolSurfaceInfo");
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
filename = JU.PT.getQuotedAttribute (this.fullCommand, "# FILE" + nFiles);
if (this.tokAt (i + 1) == 1073741848) i += 2;
} else if (this.tokAt (i + 1) == 1073741848) {
localName = this.vwr.getFilePath (this.stringParameter (eval.iToken = (i = i + 2)), false);
fullPathNameOrError = this.vwr.getFullPathNameOrError (localName);
localName = fullPathNameOrError[0];
if (this.vwr.getPathForAllFiles () !== "") {
filename = localName;
localName = null;
} else {
this.addShapeProperty (propertyList, "localName", localName);
}}}if (!filename.startsWith ("cache://") && stype == null) {
fullPathNameOrError = this.vwr.getFullPathNameOrError (filename);
filename = fullPathNameOrError[0];
if (fullPathNameOrError[1] != null) eval.errorStr (17, filename + ":" + fullPathNameOrError[1]);
}this.showString ("reading isosurface data from " + filename);
if (stype != null) {
propertyValue = this.vwr.cacheGet (filename);
this.addShapeProperty (propertyList, "calculationType", stype);
}if (propertyValue == null) {
this.addShapeProperty (propertyList, "fileName", filename);
if (localName != null) filename = localName;
if (fileIndex >= 0) sbCommand.append (" ").appendI (fileIndex);
}sbCommand.append (" /*file*/").append (JU.PT.esc (filename));
if (stype != null) sbCommand.append (" ").append (JU.PT.esc (stype));
break;
case 4106:
propertyName = "connections";
switch (this.tokAt (++i)) {
case 10:
case 1048577:
propertyValue = [this.atomExpressionAt (i).nextSetBit (0)];
break;
default:
propertyValue = [Clazz.floatToInt (eval.floatParameterSet (i, 1, 1)[0])];
break;
}
i = eval.iToken;
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
pt = this.getPoint3f (eval.iToken + 1, false);
i = eval.iToken;
if (pt.x <= 0 || pt.y <= 0 || pt.z <= 0) break;
pt.x = Clazz.floatToInt (pt.x);
pt.y = Clazz.floatToInt (pt.y);
pt.z = Clazz.floatToInt (pt.z);
sbCommand.append (" lattice ").append (JU.Escape.eP (pt));
if (isMapped) {
propertyName = "mapLattice";
propertyValue = pt;
} else {
lattice = pt;
}break;
default:
if (eval.theTok == 1073741824) {
propertyName = "thisID";
propertyValue = str;
}if (!eval.setMeshDisplayProperty (iShape, 0, eval.theTok)) {
if (JS.T.tokAttr (eval.theTok, 1073741824) && !idSeen) {
this.setShapeId (iShape, i, idSeen);
i = eval.iToken;
break;
}this.invArg ();
}if (iptDisplayProperty == 0) iptDisplayProperty = i;
i = this.slen - 1;
break;
}
idSeen = (eval.theTok != 12291);
if (isWild && surfaceObjectSeen) this.invArg ();
if (propertyName != null) this.addShapeProperty (propertyList, propertyName, propertyValue);
}
if (!this.chk) {
if ((isCavity || haveRadius) && !surfaceObjectSeen) {
surfaceObjectSeen = true;
this.addShapeProperty (propertyList, "bsSolvent", (haveRadius ?  new JU.BS () : eval.lookupIdentifierValue ("solvent")));
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
var ce = this.vwr.cm.getColorEncoder (colorScheme);
if (ce != null) {
ce.isTranslucent = isColorSchemeTranslucent;
ce.hi = 3.4028235E38;
this.addShapeProperty (propertyList, "remapColor", ce);
}}if (surfaceObjectSeen && !isLcaoCartoon && sbCommand.indexOf (";") != 0) {
propertyList.add (0, ["newObject", null]);
var needSelect = (bsSelect == null);
if (needSelect) bsSelect = JU.BSUtil.copy (this.vwr.bsA ());
if (modelIndex < 0) modelIndex = this.vwr.am.cmi;
bsSelect.and (this.vwr.getModelUndeletedAtomsBitSet (modelIndex));
if (onlyOneModel != null) {
var bsModels = this.vwr.ms.getModelBS (bsSelect, false);
if (bsModels.cardinality () > 1) eval.errorStr (30, "ISOSURFACE " + onlyOneModel);
if (needSelect) {
propertyList.add (0, ["select", bsSelect]);
if (sbCommand.indexOf ("; isosurface map") == 0) {
sbCommand =  new JU.SB ().append ("; isosurface map select ").append (JU.Escape.eBS (bsSelect)).append (sbCommand.substring (16));
}}}}if (haveIntersection && !haveSlab) {
if (!surfaceObjectSeen) this.addShapeProperty (propertyList, "sasurface", Float.$valueOf (0));
if (!isMapped) {
this.addShapeProperty (propertyList, "map", Boolean.TRUE);
this.addShapeProperty (propertyList, "select", bs);
this.addShapeProperty (propertyList, "sasurface", Float.$valueOf (0));
}this.addShapeProperty (propertyList, "slab", this.getCapSlabObject (-100, false));
}var timeMsg = (surfaceObjectSeen && this.vwr.getBoolean (603979934));
if (timeMsg) JU.Logger.startTimer ("isosurface");
this.setShapeProperty (iShape, "setProperties", propertyList);
if (timeMsg) this.showString (JU.Logger.getTimerMsg ("isosurface", 0));
if (defaultMesh) {
this.setShapeProperty (iShape, "token", Integer.$valueOf (1073742018));
this.setShapeProperty (iShape, "token", Integer.$valueOf (1073742046));
isFrontOnly = true;
sbCommand.append (" mesh nofill frontOnly");
}}if (lattice != null) this.setShapeProperty (iShape, "lattice", lattice);
if (symops != null) this.setShapeProperty (iShape, "symops", symops);
if (isFrontOnly) this.setShapeProperty (iShape, "token", Integer.$valueOf (1073741960));
if (iptDisplayProperty > 0) {
if (!eval.setMeshDisplayProperty (iShape, iptDisplayProperty, 0)) this.invArg ();
}if (this.chk) return false;
var area = null;
var volume = null;
if (doCalcArea) {
area = this.getShapeProperty (iShape, "area");
if (Clazz.instanceOf (area, Float)) this.vwr.setFloatProperty ("isosurfaceArea", (area).floatValue ());
 else this.vwr.g.setUserVariable ("isosurfaceArea", JS.SV.getVariableAD (area));
}if (doCalcVolume) {
volume = (doCalcVolume ? this.getShapeProperty (iShape, "volume") : null);
if (Clazz.instanceOf (volume, Float)) this.vwr.setFloatProperty ("isosurfaceVolume", (volume).floatValue ());
 else this.vwr.g.setUserVariable ("isosurfaceVolume", JS.SV.getVariableAD (volume));
}if (!isLcaoCartoon) {
var s = null;
if (isMapped && !surfaceObjectSeen) {
this.setShapeProperty (iShape, "finalize", sbCommand.toString ());
} else if (surfaceObjectSeen) {
cmd = sbCommand.toString ();
this.setShapeProperty (iShape, "finalize", (cmd.indexOf ("; isosurface map") == 0 ? "" : " select " + JU.Escape.eBS (bsSelect) + " ") + cmd);
s = this.getShapeProperty (iShape, "ID");
if (s != null && !eval.tQuiet) {
cutoff = (this.getShapeProperty (iShape, "cutoff")).floatValue ();
if (Float.isNaN (cutoff) && !Float.isNaN (sigma)) {
JU.Logger.error ("sigma not supported");
}s += " created";
if (isIsosurface) s += " with cutoff=" + cutoff;
var minMax = this.getShapeProperty (iShape, "minMaxInfo");
if (minMax[0] != 3.4028235E38) s += " min=" + minMax[0] + " max=" + minMax[1];
s += "; " + JV.JC.shapeClassBases[iShape].toLowerCase () + " count: " + this.getShapeProperty (iShape, "count");
s += eval.getIsosurfaceDataRange (iShape, "\n");
}}var sarea;
var svol;
if (doCalcArea || doCalcVolume) {
sarea = (doCalcArea ? "isosurfaceArea = " + (Clazz.instanceOf (area, Float) ? "" + area : JU.Escape.eAD (area)) : null);
svol = (doCalcVolume ? "isosurfaceVolume = " + (Clazz.instanceOf (volume, Float) ? "" + volume : JU.Escape.eAD (volume)) : null);
if (s == null) {
if (doCalcArea) this.showString (sarea);
if (doCalcVolume) this.showString (svol);
} else {
if (doCalcArea) s += "\n" + sarea;
if (doCalcVolume) s += "\n" + svol;
}}if (s != null) this.showString (s);
}if (translucency != null) this.setShapeProperty (iShape, "translucency", translucency);
this.setShapeProperty (iShape, "clear", null);
if (toCache) this.setShapeProperty (iShape, "cache", null);
if (iShape != 26) this.listIsosurface (iShape);
return true;
}, "~N");
Clazz.defineMethod (c$, "lcaoCartoon", 
 function () {
var eval = this.e;
this.sm.loadShape (26);
if (this.tokAt (1) == 1073742001 && this.listIsosurface (26)) return false;
this.setShapeProperty (26, "init", this.fullCommand);
if (this.slen == 1) {
this.setShapeProperty (26, "lcaoID", null);
return false;
}var idSeen = false;
var translucency = null;
for (var i = 1; i < this.slen; i++) {
var propertyName = null;
var propertyValue = null;
switch (this.getToken (i).tok) {
case 1074790451:
case 554176565:
propertyName = eval.theToken.value;
if (this.tokAt (i + 1) == 1048588) eval.iToken = i + 1;
propertyValue = this.getCapSlabObject (i, true);
i = eval.iToken;
break;
case 12289:
this.isosurface (26);
return false;
case 528432:
var degx = 0;
var degy = 0;
var degz = 0;
switch (this.getToken (++i).tok) {
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
propertyValue = JU.V3.new3 (degx, degy, degz);
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
i = eval.iToken;
break;
case 1766856708:
translucency = this.setColorOptions (null, i + 1, 26, -2);
if (translucency != null) this.setShapeProperty (26, "settranslucency", translucency);
i = eval.iToken;
idSeen = true;
continue;
case 603979967:
case 1073742074:
eval.setMeshDisplayProperty (26, i, eval.theTok);
i = eval.iToken;
idSeen = true;
continue;
case 1113200651:
case 4:
propertyValue = this.paramAsStr (i).toLowerCase ();
if (propertyValue.equals ("spacefill")) propertyValue = "cpk";
propertyName = "create";
if (eval.optParameterAsString (i + 1).equalsIgnoreCase ("molecular")) {
i++;
propertyName = "molecular";
}break;
case 135280132:
if (this.tokAt (i + 1) == 10 || this.tokAt (i + 1) == 1048577) {
propertyName = "select";
propertyValue = this.atomExpressionAt (i + 1);
i = eval.iToken;
} else {
propertyName = "selectType";
propertyValue = this.paramAsStr (++i);
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
case 1073742028:
propertyName = "molecular";
break;
case 1073741904:
propertyValue = this.paramAsStr (++i);
propertyName = "create";
if (eval.optParameterAsString (i + 1).equalsIgnoreCase ("molecular")) {
i++;
propertyName = "molecular";
}break;
case 1074790550:
propertyValue = eval.setShapeNameParameter (++i);
i = eval.iToken;
if (idSeen) this.invArg ();
propertyName = "lcaoID";
break;
default:
if (eval.theTok == 269484209 || JS.T.tokAttr (eval.theTok, 1073741824)) {
if (eval.theTok != 269484209) propertyValue = this.paramAsStr (i);
if (idSeen) this.invArg ();
propertyName = "lcaoID";
break;
}break;
}
if (eval.theTok != 12291) idSeen = true;
if (propertyName == null) this.invArg ();
this.setShapeProperty (26, propertyName, propertyValue);
}
this.setShapeProperty (26, "clear", null);
return true;
});
Clazz.defineMethod (c$, "mapProperty", 
 function () {
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
bsFrom = this.vwr.bsA ();
bsTo = this.atomExpressionAt (2);
property1 = property2 = "selected";
} else {
bsFrom = this.atomExpressionAt (1);
if (this.tokAt (++this.e.iToken) != 1048583 || !JS.T.tokAttr (tokProp1 = this.tokAt (++this.e.iToken), 1078984704)) break;
property1 = this.paramAsStr (this.e.iToken);
bsTo = this.atomExpressionAt (++this.e.iToken);
if (this.tokAt (++this.e.iToken) != 1048583 || !JS.T.tokAttr (tokProp2 = this.tokAt (++this.e.iToken), 2048)) break;
property2 = this.paramAsStr (this.e.iToken);
}if (JS.T.tokAttr (tokKey = this.tokAt (this.e.iToken + 1), 1078984704)) mapKey = this.paramAsStr (++this.e.iToken);
 else mapKey = JS.T.nameOf (tokKey = 1095763969);
this.e.checkLast (this.e.iToken);
if (this.chk) return;
var bsOut = null;
this.showString ("mapping " + property1.toUpperCase () + " for " + bsFrom.cardinality () + " atoms to " + property2.toUpperCase () + " for " + bsTo.cardinality () + " atoms using " + mapKey.toUpperCase ());
if (JS.T.tokAttrOr (tokProp1, 1095761920, 1112539136) && JS.T.tokAttrOr (tokProp2, 1095761920, 1112539136) && JS.T.tokAttrOr (tokKey, 1095761920, 1112539136)) {
var data1 = this.e.getBitsetPropertyFloat (bsFrom, tokProp1 | 224, NaN, NaN);
var data2 = this.e.getBitsetPropertyFloat (bsFrom, tokKey | 224, NaN, NaN);
var data3 = this.e.getBitsetPropertyFloat (bsTo, tokKey | 224, NaN, NaN);
var isProperty = (tokProp2 == 1716520985);
var dataOut =  Clazz.newFloatArray (isProperty ? this.vwr.getAtomCount () : data3.length, 0);
bsOut =  new JU.BS ();
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
if (isProperty) this.vwr.setData (property2, [property2, dataOut, bsOut, Integer.$valueOf (1), Boolean.TRUE], this.vwr.getAtomCount (), 0, 0, 2147483647, 0);
 else this.vwr.setAtomProperty (bsOut, tokProp2, 0, 0, null, dataOut, null);
}}if (bsOut == null) {
var format = "{" + mapKey + "=%[" + mapKey + "]}." + property2 + " = %[" + property1 + "]";
var data = this.getBitsetIdent (bsFrom, format, null, false, 2147483647, false);
var sb =  new JU.SB ();
for (var i = 0; i < data.length; i++) if (data[i].indexOf ("null") < 0) sb.append (data[i]).appendC ('\n');

if (JU.Logger.debugging) JU.Logger.debug (sb.toString ());
var bsSubset = JU.BSUtil.copy (this.vwr.getSelectionSubset ());
this.vwr.setSelectionSubset (bsTo);
try {
this.e.runScript (sb.toString ());
} catch (e$$) {
if (Clazz.exceptionOf (e$$, Exception)) {
var ex = e$$;
{
this.vwr.setSelectionSubset (bsSubset);
this.e.errorStr (-1, "Error: " + ex.getMessage ());
}
} else if (Clazz.exceptionOf (e$$, Error)) {
var er = e$$;
{
this.vwr.setSelectionSubset (bsSubset);
this.e.errorStr (-1, "Error: " + er.toString ());
}
} else {
throw e$$;
}
}
this.vwr.setSelectionSubset (bsSubset);
}this.showString ("DONE");
return;
}
this.invArg ();
});
Clazz.defineMethod (c$, "minimize", 
 function () {
var bsSelected = null;
var steps = 2147483647;
var crit = 0;
var addHydrogen = false;
var isSilent = false;
var bsFixed = null;
var isOnly = false;
var minimizer = this.vwr.getMinimizer (false);
for (var i = 1; i < this.slen; i++) switch (this.getToken (i).tok) {
case 1073741828:
addHydrogen = true;
continue;
case 1073741874:
case 1073742162:
this.checkLength (2);
if (this.chk || minimizer == null) return;
minimizer.setProperty (this.paramAsStr (i), null);
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
i = this.e.iToken + 1;
}
aList[0] = n;
if (n == 1) this.invArg ();
targetValue = this.floatParameter (this.e.checkLast (i));
}if (!this.chk) this.vwr.getMinimizer (true).setProperty ("constraint", [aList,  Clazz.newIntArray (n, 0), Float.$valueOf (targetValue)]);
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
i = this.e.iToken;
if (!this.chk) this.vwr.getMinimizer (true).setProperty ("fixed", bsFixed);
if (i + 1 == this.slen) return;
continue;
case 10:
case 1048577:
isOnly = true;
case 135280132:
if (this.e.theTok == 135280132) i++;
bsSelected = this.atomExpressionAt (i);
i = this.e.iToken;
if (this.tokAt (i + 1) == 1073742072) {
i++;
isOnly = true;
}continue;
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

if (!this.chk) this.vwr.minimize (steps, crit, bsSelected, bsFixed, 0, addHydrogen, isOnly, isSilent, false);
});
Clazz.defineMethod (c$, "mo", 
 function (isInitOnly) {
var eval = this.e;
var offset = 2147483647;
var isNegOffset = false;
var bsModels = this.vwr.getVisibleFramesBitSet ();
var propertyList =  new JU.Lst ();
var i0 = 1;
if (this.tokAt (1) == 1095766030 || this.tokAt (1) == 4115) {
i0 = eval.modelNumberParameter (2);
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
switch (this.getToken (i).tok) {
case 1074790451:
case 554176565:
propertyName = eval.theToken.value;
propertyValue = this.getCapSlabObject (i, false);
i = eval.iToken;
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
propertyValue = eval.planeParameter (i);
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
if (i + 1 < this.slen && this.tokAt (i + 1) == 4) {
propertyName = "titleFormat";
propertyValue = this.paramAsStr (i + 1);
}break;
case 1073741824:
this.invArg ();
break;
default:
if (eval.isArrayParameter (i)) {
linearCombination = eval.floatParameterSet (i, 1, 2147483647);
if (this.tokAt (eval.iToken + 1) == 1073742156) {
this.addShapeProperty (propertyList, "squareLinear", Boolean.TRUE);
eval.iToken++;
}break;
}var ipt = eval.iToken;
if (!eval.setMeshDisplayProperty (27, 0, eval.theTok)) this.invArg ();
this.setShapeProperty (27, "setProperties", propertyList);
eval.setMeshDisplayProperty (27, ipt, this.tokAt (ipt));
return true;
}
if (propertyName != null) this.addShapeProperty (propertyList, propertyName, propertyValue);
if (moNumber != 2147483647 || linearCombination != null) {
if (this.tokAt (eval.iToken + 1) == 4) title = this.paramAsStr (++eval.iToken);
eval.setCursorWait (true);
this.setMoData (propertyList, moNumber, linearCombination, offset, isNegOffset, iModel, title);
this.addShapeProperty (propertyList, "finalize", null);
}if (propertyList.size () > 0) this.setShapeProperty (27, "setProperties", propertyList);
propertyList.clear ();
}
return true;
}, "~B");
Clazz.defineMethod (c$, "modulation", 
 function () {
var qtOffset = null;
var mod = true;
var isQ = false;
var bs = null;
switch (this.getToken (1).tok) {
case 1048588:
mod = false;
case 0:
case 1048589:
break;
case 10:
case 1048577:
bs = this.atomExpressionAt (1);
switch (this.tokAt (this.e.iToken + 1)) {
case 0:
break;
case 1048588:
mod = false;
case 1048589:
this.e.iToken++;
break;
}
this.e.checkLast (this.e.iToken);
break;
case 1048586:
case 8:
qtOffset = this.e.getPoint3f (1, false);
isQ = (this.tokAt (this.e.iToken + 1) == 1048589);
break;
case 3:
var t1 = this.floatParameter (1);
qtOffset = JU.P3.new3 (t1, t1, t1);
break;
case 2:
var t = this.intParameter (1);
qtOffset = JU.P3.new3 (t, t, t);
isQ = true;
break;
case 1073742138:
var scale = this.floatParameter (2);
if (!this.chk) this.vwr.setFloatProperty ("modulationScale", scale);
return;
default:
this.invArg ();
}
if (!this.chk) {
this.vwr.setVibrationOff ();
this.vwr.setModulation (bs, mod, qtOffset, isQ);
}});
Clazz.defineMethod (c$, "navigate", 
function () {
var eval = this.e;
if (this.slen == 1) {
eval.setBooleanProperty ("navigationMode", true);
return;
}var rotAxis = JU.V3.new3 (0, 1, 0);
var list =  new JU.Lst ();
var pt;
if (this.slen == 2) {
switch (this.getToken (1).tok) {
case 1048589:
case 1048588:
if (this.chk) return;
eval.setObjectMad (33, "axes", 1);
this.setShapeProperty (33, "position", JU.P3.new3 (50, 50, 3.4028235E38));
eval.setBooleanProperty ("navigationMode", true);
this.vwr.tm.setNavOn (eval.theTok == 1048589);
return;
case 1073742162:
if (!this.chk) this.vwr.tm.setNavXYZ (0, 0, 0);
return;
case 8:
case 1113200654:
break;
default:
this.invArg ();
}
}if (!this.chk && !this.vwr.getBoolean (603979887)) eval.setBooleanProperty ("navigationMode", true);
for (var i = 1; i < this.slen; i++) {
var timeSec = (this.isFloatParameter (i) ? this.floatParameter (i++) : 2);
if (timeSec < 0) this.invArg ();
if (!this.chk && timeSec > 0) eval.refresh (false);
switch (this.getToken (i).tok) {
case 8:
case 1048586:
pt = this.getPoint3f (i, true);
eval.iToken++;
if (eval.iToken != this.slen) this.invArg ();
if (!this.chk) this.vwr.tm.setNavXYZ (pt.x, pt.y, pt.z);
return;
case 554176526:
var depth = this.floatParameter (++i);
if (!this.chk) list.addLast ([Integer.$valueOf (554176526), Float.$valueOf (timeSec), Float.$valueOf (depth)]);
continue;
case 12289:
pt = this.centerParameter (++i);
i = eval.iToken;
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
i = eval.iToken + 1;
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
i = eval.iToken;
if (!this.chk) list.addLast ([Integer.$valueOf (4160), Float.$valueOf (timeSec), pt]);
continue;
}
}if (!this.chk) list.addLast ([Integer.$valueOf (269484210), Float.$valueOf (timeSec), Float.$valueOf (x), Float.$valueOf (y)]);
continue;
case 269484208:
continue;
case 1113200654:
var pathGuide;
var vp =  new JU.Lst ();
var bs;
if (this.tokAt (i + 1) == 10 || this.tokAt (i + 1) == 1048577) {
bs = this.atomExpressionAt (++i);
i = eval.iToken;
} else {
bs = this.vwr.bsA ();
}if (this.chk) return;
this.vwr.getPolymerPointsAndVectors (bs, vp);
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
if (this.getToken (i + 1).tok == 1048582) {
i++;
var pathID = eval.objectNameParameter (++i);
if (this.chk) return;
this.setShapeProperty (22, "thisID", pathID);
path = this.getShapeProperty (22, "vertices");
eval.refresh (false);
if (path == null) this.invArg ();
var indexStart = Clazz.floatToInt (this.isFloatParameter (i + 1) ? this.floatParameter (++i) : 0);
var indexEnd = Clazz.floatToInt (this.isFloatParameter (i + 1) ? this.floatParameter (++i) : 2147483647);
list.addLast ([Integer.$valueOf (1073742084), Float.$valueOf (timeSec), path, theta, [indexStart, indexEnd]]);
continue;
}var v =  new JU.Lst ();
while (eval.isCenterParameter (i + 1)) {
v.addLast (this.centerParameter (++i));
i = eval.iToken;
}
if (v.size () > 0) {
path = v.toArray ( new Array (v.size ()));
if (!this.chk) list.addLast ([Integer.$valueOf (1073742084), Float.$valueOf (timeSec), path, theta, [0, 2147483647]]);
continue;
}default:
this.invArg ();
}
}
if (!this.chk && !this.vwr.isJmolDataFrame ()) this.vwr.tm.navigateList (eval, list);
});
Clazz.overrideMethod (c$, "plot", 
function (args) {
this.st = this.e.st;
this.chk = this.e.chk;
var modelIndex = this.vwr.am.cmi;
if (modelIndex < 0) this.e.errorStr (30, "plot");
modelIndex = this.vwr.ms.getJmolDataSourceFrame (modelIndex);
var pt = args.length - 1;
var isReturnOnly = (args !== this.st);
var statementSave = this.st;
if (isReturnOnly) this.e.st = this.st = args;
var tokCmd = (isReturnOnly ? 135270926 : args[0].tok);
var pt0 = (isReturnOnly || tokCmd == 135270418 || tokCmd == 1052714 ? 0 : 1);
var filename = null;
var makeNewFrame = true;
var isDraw = false;
switch (tokCmd) {
case 4133:
case 135270418:
case 1052714:
break;
case 135176:
makeNewFrame = false;
isDraw = true;
break;
case 135270926:
makeNewFrame = false;
break;
case 135270422:
makeNewFrame = false;
if (JS.CmdExt.tokAtArray (pt, args) == 4) {
filename = this.stringParameter (pt--);
} else if (JS.CmdExt.tokAtArray (pt - 1, args) == 1048583) {
filename = this.paramAsStr (pt - 2) + "." + this.paramAsStr (pt);
pt -= 3;
} else {
this.e.st = this.st = statementSave;
this.e.iToken = this.st.length;
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
var bs = JU.BSUtil.copy (this.vwr.bsA ());
var preSelected = "; select " + JU.Escape.eBS (bs) + ";\n ";
var type = this.e.optParameterAsString (pt).toLowerCase ();
var minXYZ = null;
var maxXYZ = null;
var tok = JS.CmdExt.tokAtArray (pt0, args);
if (tok == 4) tok = JS.T.getTokFromName (args[pt0].value);
switch (tok) {
default:
this.e.iToken = 1;
this.invArg ();
break;
case 135270408:
this.e.iToken = 1;
type = "data";
preSelected = "";
break;
case 1716520985:
this.e.iToken = pt0 + 1;
if (!JS.T.tokAttr (propertyX = this.tokAt (this.e.iToken++), 1078984704) || !JS.T.tokAttr (propertyY = this.tokAt (this.e.iToken++), 1078984704)) this.invArg ();
if (JS.T.tokAttr (propertyZ = this.tokAt (this.e.iToken), 1078984704)) this.e.iToken++;
 else propertyZ = 0;
if (this.tokAt (this.e.iToken) == 32) {
minXYZ = this.getPoint3f (++this.e.iToken, false);
this.e.iToken++;
}if (this.tokAt (this.e.iToken) == 64) {
maxXYZ = this.getPoint3f (++this.e.iToken, false);
this.e.iToken++;
}type = "property " + JS.T.nameOf (propertyX) + " " + JS.T.nameOf (propertyY) + (propertyZ == 0 ? "" : " " + JS.T.nameOf (propertyZ));
if (bs.nextSetBit (0) < 0) bs = this.vwr.getModelUndeletedAtomsBitSet (modelIndex);
stateScript = "select " + JU.Escape.eBS (bs) + ";\n ";
break;
case 1052714:
if (type.equalsIgnoreCase ("draw")) {
isDraw = true;
type = this.e.optParameterAsString (--pt).toLowerCase ();
}isRamachandranRelative = (pt > pt0 && type.startsWith ("r"));
type = "ramachandran" + (isRamachandranRelative ? " r" : "") + (tokCmd == 135176 ? " draw" : "");
break;
case 135270418:
case 137363467:
qFrame = " \"" + this.vwr.getQuaternionFrame () + "\"";
stateScript = "set quaternionFrame" + qFrame + ";\n  ";
isQuaternion = true;
if (type.equalsIgnoreCase ("draw")) {
isDraw = true;
type = this.e.optParameterAsString (--pt).toLowerCase ();
}isDerivative = (type.startsWith ("deriv") || type.startsWith ("diff"));
isSecondDerivative = (isDerivative && type.indexOf ("2") > 0);
if (isDerivative) pt--;
if (type.equalsIgnoreCase ("helix") || type.equalsIgnoreCase ("axis")) {
isDraw = true;
isDerivative = true;
pt = -1;
}type = ((pt <= pt0 ? "" : this.e.optParameterAsString (pt)) + "w").substring (0, 1);
if (type.equals ("a") || type.equals ("r")) isDerivative = true;
if (!JU.PT.isOneOf (type, ";w;x;y;z;r;a;")) this.e.evalError ("QUATERNION [w,x,y,z,a,r] [difference][2]", null);
type = "quaternion " + type + (isDerivative ? " difference" : "") + (isSecondDerivative ? "2" : "") + (isDraw ? " draw" : "");
break;
}
this.st = statementSave;
if (this.chk) return "";
if (makeNewFrame) {
stateScript += "plot " + type;
var ptDataFrame = this.vwr.ms.getJmolDataFrameIndex (modelIndex, stateScript);
if (ptDataFrame > 0 && tokCmd != 135270422 && tokCmd != 135270926) {
this.vwr.setCurrentModelIndexClear (ptDataFrame, true);
return "";
}}var dataX = null;
var dataY = null;
var dataZ = null;
var factors = JU.P3.new3 (1, 1, 1);
if (tok == 1716520985) {
dataX = this.e.getBitsetPropertyFloat (bs, propertyX | 224, (minXYZ == null ? NaN : minXYZ.x), (maxXYZ == null ? NaN : maxXYZ.x));
dataY = this.e.getBitsetPropertyFloat (bs, propertyY | 224, (minXYZ == null ? NaN : minXYZ.y), (maxXYZ == null ? NaN : maxXYZ.y));
if (propertyZ != 0) dataZ = this.e.getBitsetPropertyFloat (bs, propertyZ | 224, (minXYZ == null ? NaN : minXYZ.z), (maxXYZ == null ? NaN : maxXYZ.z));
if (minXYZ == null) minXYZ = JU.P3.new3 (this.getPlotMinMax (dataX, false, propertyX), this.getPlotMinMax (dataY, false, propertyY), this.getPlotMinMax (dataZ, false, propertyZ));
if (maxXYZ == null) maxXYZ = JU.P3.new3 (this.getPlotMinMax (dataX, true, propertyX), this.getPlotMinMax (dataY, true, propertyY), this.getPlotMinMax (dataZ, true, propertyZ));
JU.Logger.info ("plot min/max: " + minXYZ + " " + maxXYZ);
var center =  new JU.P3 ();
center.ave (maxXYZ, minXYZ);
factors.sub2 (maxXYZ, minXYZ);
factors.set (factors.x / 200, factors.y / 200, factors.z / 200);
if (JS.T.tokAttr (propertyX, 1095761920)) {
factors.x = 1;
center.x = 0;
} else if (factors.x > 0.1 && factors.x <= 10) {
factors.x = 1;
}if (JS.T.tokAttr (propertyY, 1095761920)) {
factors.y = 1;
center.y = 0;
} else if (factors.y > 0.1 && factors.y <= 10) {
factors.y = 1;
}if (JS.T.tokAttr (propertyZ, 1095761920)) {
factors.z = 1;
center.z = 0;
} else if (factors.z > 0.1 && factors.z <= 10) {
factors.z = 1;
}if (propertyZ == 0) center.z = minXYZ.z = maxXYZ.z = factors.z = 0;
for (var i = 0; i < dataX.length; i++) dataX[i] = (dataX[i] - center.x) / factors.x;

for (var i = 0; i < dataY.length; i++) dataY[i] = (dataY[i] - center.y) / factors.y;

if (propertyZ != 0) for (var i = 0; i < dataZ.length; i++) dataZ[i] = (dataZ[i] - center.z) / factors.z;

parameters = [bs, dataX, dataY, dataZ, minXYZ, maxXYZ, factors, center];
}if (tokCmd == 135270422) return this.vwr.writeFileData (filename, "PLOT_" + type, modelIndex, parameters);
var data = (type.equals ("data") ? "1 0 H 0 0 0 # Jmol PDB-encoded data" : this.vwr.getPdbData (modelIndex, type, null, parameters, null, true));
if (tokCmd == 135270926) return data;
if (JU.Logger.debugging) JU.Logger.debug (data);
if (tokCmd == 135176) {
this.e.runScript (data);
return "";
}var savedFileInfo = this.vwr.getFileInfo ();
var oldAppendNew = this.vwr.getBoolean (603979792);
this.vwr.g.appendNew = true;
var isOK = (data != null && this.vwr.openStringInlineParamsAppend (data, null, true) == null);
this.vwr.g.appendNew = oldAppendNew;
this.vwr.setFileInfo (savedFileInfo);
if (!isOK) return "";
var modelCount = this.vwr.getModelCount ();
this.vwr.ms.setJmolDataFrame (stateScript, modelIndex, modelCount - 1);
if (tok != 1716520985) stateScript += ";\n" + preSelected;
var ss = this.vwr.addStateScript (stateScript, true, false);
var radius = 150;
var script;
switch (tok) {
default:
script = "frame 0.0; frame last; reset;select visible;wireframe only;";
radius = 10;
break;
case 1716520985:
this.vwr.setFrameTitle (modelCount - 1, type + " plot for model " + this.vwr.getModelNumberDotted (modelIndex));
var f = 3;
script = "frame 0.0; frame last; reset;select visible; spacefill " + f + "; wireframe 0;" + "draw plotAxisX" + modelCount + " {100 -100 -100} {-100 -100 -100} \"" + JS.T.nameOf (propertyX) + "\";" + "draw plotAxisY" + modelCount + " {-100 100 -100} {-100 -100 -100} \"" + JS.T.nameOf (propertyY) + "\";";
if (propertyZ != 0) script += "draw plotAxisZ" + modelCount + " {-100 -100 100} {-100 -100 -100} \"" + JS.T.nameOf (propertyZ) + "\";";
break;
case 1052714:
this.vwr.setFrameTitle (modelCount - 1, "ramachandran plot for model " + this.vwr.getModelNumberDotted (modelIndex));
script = "frame 0.0; frame last; reset;select visible; color structure; spacefill 3.0; wireframe 0;draw ramaAxisX" + modelCount + " {100 0 0} {-100 0 0} \"phi\";" + "draw ramaAxisY" + modelCount + " {0 100 0} {0 -100 0} \"psi\";";
break;
case 135270418:
case 137363467:
this.vwr.setFrameTitle (modelCount - 1, type.$replace ('w', ' ') + qFrame + " for model " + this.vwr.getModelNumberDotted (modelIndex));
var color = (JU.C.getHexCode (this.vwr.getColixBackgroundContrast ()));
script = "frame 0.0; frame last; reset;select visible; wireframe 0; spacefill 3.0; isosurface quatSphere" + modelCount + " color " + color + " sphere 100.0 mesh nofill frontonly translucent 0.8;" + "draw quatAxis" + modelCount + "X {100 0 0} {-100 0 0} color red \"x\";" + "draw quatAxis" + modelCount + "Y {0 100 0} {0 -100 0} color green \"y\";" + "draw quatAxis" + modelCount + "Z {0 0 100} {0 0 -100} color blue \"z\";" + "color structure;" + "draw quatCenter" + modelCount + "{0 0 0} scale 0.02;";
break;
}
this.e.runScript (script + preSelected);
ss.setModelIndex (this.vwr.am.cmi);
this.vwr.setRotationRadius (radius, true);
this.sm.loadShape (30);
this.showString ("frame " + this.vwr.getModelNumberDotted (modelCount - 1) + (type.length > 0 ? " created: " + type + (isQuaternion ? qFrame : "") : ""));
return "";
}, "~A");
Clazz.defineMethod (c$, "polyhedra", 
 function () {
var eval = this.e;
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
var colorArgb = [-2147483648];
for (var i = 1; i < this.slen; ++i) {
var propertyName = null;
var propertyValue = null;
switch (this.getToken (i).tok) {
case 12291:
case 1048589:
case 1048588:
if (i + 1 != this.slen || needsGenerating || nAtomSets > 1 || nAtomSets == 0 && "to".equals (setPropertyName)) this.error (18);
propertyName = (eval.theTok == 1048588 ? "off" : eval.theTok == 1048589 ? "on" : "delete");
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
if (eval.theTok == 2) {
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
if (++nAtomSets > 2) eval.bad ();
if ("to".equals (setPropertyName)) needsGenerating = true;
propertyName = setPropertyName;
setPropertyName = "to";
propertyValue = this.atomExpressionAt (i);
i = eval.iToken;
break;
case 1074790746:
if (nAtomSets > 1) this.invPO ();
if (this.tokAt (i + 1) == 10 || this.tokAt (i + 1) == 1048577 && !needsGenerating) {
propertyName = "toBitSet";
propertyValue = this.atomExpressionAt (++i);
i = eval.iToken;
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
translucentLevel = this.getColorTrans (eval, i, true, colorArgb);
i = eval.iToken;
continue;
case 1073741886:
case 1073741948:
propertyName = "collapsed";
propertyValue = (eval.theTok == 1073741886 ? Boolean.TRUE : Boolean.FALSE);
if (typeSeen) this.error (18);
typeSeen = true;
break;
case 1073742044:
case 1073741933:
case 1073741956:
if (edgeParameterSeen) this.error (18);
propertyName = this.paramAsStr (i);
edgeParameterSeen = true;
break;
case 1073741964:
lighting = eval.theTok;
continue;
default:
if (eval.isColorParam (i)) {
colorArgb[0] = eval.getArgbParam (i);
i = eval.iToken;
continue;
}this.invArg ();
}
this.setShapeProperty (21, propertyName, propertyValue);
if (onOffDelete) return false;
}
if (!needsGenerating && !typeSeen && !edgeParameterSeen && lighting == 0) this.error (19);
if (needsGenerating) this.setShapeProperty (21, "generate", null);
if (colorArgb[0] != -2147483648) this.setShapeProperty (21, "colorThis", Integer.$valueOf (colorArgb[0]));
if (translucentLevel != 3.4028235E38) eval.setShapeTranslucency (21, "", "translucentThis", translucentLevel, null);
if (lighting != 0) this.setShapeProperty (21, "token", Integer.$valueOf (lighting));
this.setShapeProperty (21, "init", Boolean.FALSE);
return true;
});
Clazz.overrideMethod (c$, "evalParallel", 
function (context, shapeManager) {
var se =  new JS.ScriptEval ().setViewer (this.vwr);
se.historyDisabled = true;
se.compiler =  new JS.ScriptCompiler (this.vwr);
se.sm = shapeManager;
try {
se.restoreScriptContext (context, true, false, false);
se.allowJSThreads = false;
se.dispatchCommands (false, false);
} catch (ex) {
if (Clazz.exceptionOf (ex, Exception)) {
this.e.vwr.setStringProperty ("_errormessage", "" + ex);
if (se.thisContext == null) {
JU.Logger.error ("Error evaluating context " + ex);
if (!this.vwr.isJS) ex.printStackTrace ();
}return false;
} else {
throw ex;
}
}
return true;
}, "JS.ScriptContext,JV.ShapeManager");
Clazz.overrideMethod (c$, "write", 
function (args) {
var pt = 0;
var pt0 = 0;
var isCommand;
var isShow;
if (args == null) {
args = this.st;
pt = pt0 = 1;
isCommand = true;
isShow = (this.vwr.isApplet () && !this.vwr.isSignedApplet () || !this.vwr.haveAccess (JV.Viewer.ACCESS.ALL) || this.vwr.getPathForAllFiles ().length > 0);
} else {
isCommand = false;
isShow = true;
}var argCount = (isCommand ? this.slen : args.length);
var len = 0;
var nVibes = 0;
var width = -1;
var height = -1;
var quality = -2147483648;
var timeMsg = this.vwr.getBoolean (603979934);
var driverList = this.vwr.getExportDriverList ();
var sceneType = "PNGJ";
var data = null;
var type2 = "";
var fileName = null;
var localPath = null;
var remotePath = null;
var val = null;
var msg = null;
var tVar = null;
var fullPath =  new Array (1);
var isCoord = false;
var isExport = false;
var isImage = false;
var bsFrames = null;
var scripts = null;
var params;
var type = "SPT";
var tok = (isCommand && args.length == 1 ? 1073741884 : JS.CmdExt.tokAtArray (pt, args));
switch (tok) {
case 0:
break;
case 135271429:
if (this.e.isArrayParameter (pt + 1)) {
scripts = this.e.stringParameterSet (++pt);
localPath = ".";
remotePath = ".";
pt0 = pt = this.e.iToken + 1;
tok = this.tokAt (pt);
}break;
default:
type = JS.SV.sValue (this.tokenAt (pt, args)).toUpperCase ();
}
if (isCommand && this.tokAt (this.slen - 2) == 1073741848) {
type = this.paramAsStr (this.slen - 1).toUpperCase ();
pt0 = argCount;
argCount -= 2;
tok = 0;
}switch (tok) {
case 0:
break;
case 15:
case 6:
type = "VAR";
tVar = this.tokenAt (pt++, args);
break;
case 135270418:
case 1052714:
case 1716520985:
msg = this.plot (args);
if (!isCommand) return msg;
break;
case 1073741983:
type = "INLINE";
data = JS.SV.sValue (this.tokenAt (++pt, args));
pt++;
break;
case 1073742102:
type = "PGRP";
pt++;
type2 = JS.SV.sValue (this.tokenAt (pt, args)).toLowerCase ();
if (type2.equals ("draw")) pt++;
break;
case 1048581:
pt++;
isCoord = true;
break;
case 1073742158:
case 135271429:
val = JS.SV.sValue (this.tokenAt (++pt, args)).toLowerCase ();
while (val.equals ("localpath") || val.equals ("remotepath")) {
if (val.equals ("localpath")) localPath = JS.SV.sValue (this.tokenAt (++pt, args));
 else remotePath = JS.SV.sValue (this.tokenAt (++pt, args));
val = JS.SV.sValue (this.tokenAt (++pt, args)).toLowerCase ();
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
case 1073741991:
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
nVibes = this.e.intParameterRange (++pt, 1, 10);
if (nVibes == 2147483647) return "";
if (!this.chk) {
this.vwr.setVibrationOff ();
if (!this.e.isJS) this.e.delayScript (100);
}pt++;
break;
case 4115:
var bsAtoms;
if (pt + 1 < argCount && args[++pt].tok == 1048577 || args[pt].tok == 10) {
bsAtoms = this.e.atomExpression (args, pt, 0, true, false, true, true);
pt = this.e.iToken + 1;
} else {
bsAtoms = this.vwr.getAllAtoms ();
}if (!this.chk) bsFrames = this.vwr.ms.getModelBS (bsAtoms, true);
break;
case 1073742139:
val = JS.SV.sValue (this.tokenAt (++pt, args)).toUpperCase ();
if (JU.PT.isOneOf (val, ";PNG;PNGJ;")) {
sceneType = val;
pt++;
}break;
default:
tok = 1073741979;
break;
}
if (tok == 1073741979) {
var t = JS.T.getTokenFromName (JS.SV.sValue (args[pt]).toLowerCase ());
if (t != null) {
type = JS.SV.sValue (t).toUpperCase ();
isCoord = (t.tok == 1048581);
if (isCoord) pt++;
}if (JU.PT.isOneOf (type, driverList.toUpperCase ())) {
pt++;
type = type.substring (0, 1).toUpperCase () + type.substring (1).toLowerCase ();
isExport = true;
if (isCommand) fileName = "Jmol." + type.toLowerCase ();
break;
} else if (JU.PT.isOneOf (type, ";ZIP;ZIPALL;SPT;STATE;")) {
pt++;
break;
} else if (!isCoord) {
type = "(image)";
}}if (JS.CmdExt.tokAtArray (pt, args) == 2) {
width = JS.SV.iValue (this.tokenAt (pt++, args));
height = JS.SV.iValue (this.tokenAt (pt++, args));
}break;
}
if (msg == null) {
if (pt0 < argCount) {
val = JS.SV.sValue (this.tokenAt (pt, args));
if (val.equalsIgnoreCase ("clipboard")) {
if (this.chk) return "";
} else if (JU.PT.isOneOf (val.toLowerCase (), ";jpg;jpeg;jpg64;jpeg64;gif;pdf;ppm;png;pngj;pngt;")) {
if (JS.CmdExt.tokAtArray (pt + 1, args) == 2 && JS.CmdExt.tokAtArray (pt + 2, args) == 2) {
width = JS.SV.iValue (this.tokenAt (++pt, args));
height = JS.SV.iValue (this.tokenAt (++pt, args));
}if (JS.CmdExt.tokAtArray (pt + 1, args) == 2) quality = JS.SV.iValue (this.tokenAt (++pt, args));
} else if (JU.PT.isOneOf (val.toLowerCase (), ";xyz;xyzrn;xyzvib;mol;sdf;v2000;v3000;json;pdb;pqr;cml;")) {
type = val.toUpperCase ();
if (pt + 1 == argCount) pt++;
}if (type.equals ("(image)") && JU.PT.isOneOf (val.toLowerCase (), ";jpg;jpeg;jpg64;jpeg64;gif;pdf;ppm;png;pngj;pngt;scene;")) {
type = val.toUpperCase ();
pt++;
}}if (pt + 2 == argCount) {
var s = JS.SV.sValue (this.tokenAt (++pt, args));
if (s.length > 0 && s.charAt (0) != '.') type = val.toUpperCase ();
}switch (JS.CmdExt.tokAtArray (pt, args)) {
case 0:
isShow = true;
break;
case 1073741884:
break;
case 1073741824:
case 4:
fileName = JS.SV.sValue (this.tokenAt (pt, args));
if (pt == argCount - 3 && JS.CmdExt.tokAtArray (pt + 1, args) == 1048583) {
fileName += "." + JS.SV.sValue (this.tokenAt (pt + 2, args));
}if (type !== "VAR" && pt == pt0 && !isCoord) type = "IMAGE";
 else if (fileName.length > 0 && fileName.charAt (0) == '.' && (pt == pt0 + 1 || pt == pt0 + 2)) {
fileName = JS.SV.sValue (this.tokenAt (pt - 1, args)) + fileName;
if (type !== "VAR" && pt == pt0 + 1) type = "IMAGE";
}if (fileName.equalsIgnoreCase ("clipboard") || !this.vwr.haveAccess (JV.Viewer.ACCESS.ALL)) fileName = null;
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
}if (type.equals ("COORD") || type.equals ("COORDS")) type = (fileName != null && fileName.indexOf (".") >= 0 ? fileName.substring (fileName.lastIndexOf (".") + 1).toUpperCase () : "XYZ");
isImage = JU.PT.isOneOf (type.toLowerCase (), ";jpg;jpeg;jpg64;jpeg64;gif;pdf;ppm;png;pngj;pngt;scene;");
if (scripts != null) {
if (type.equals ("PNG")) type = "PNGJ";
if (!type.equals ("PNGJ") && !type.equals ("ZIPALL")) this.invArg ();
}if (!isImage && !isExport && !JU.PT.isOneOf (type, ";SCENE;JMOL;ZIP;ZIPALL;SPT;HISTORY;MO;ISOSURFACE;MESH;PMESH;VAR;FILE;FUNCTION;CML;JSON;XYZ;XYZRN;XYZVIB;MENU;MOL;PDB;PGRP;PQR;QUAT;RAMA;SDF;V2000;V3000;INLINE;")) this.e.errorStr2 (54, "COORDS|FILE|FUNCTIONS|HISTORY|IMAGE|INLINE|ISOSURFACE|JMOL|MENU|MO|POINTGROUP|QUATERNION [w,x,y,z] [derivative]|RAMACHANDRAN|SPT|STATE|VAR x|ZIP|ZIPALL  CLIPBOARD", "CML|GIF|JPG|JPG64|JMOL|JVXL|MESH|MOL|PDB|PMESH|PNG|PNGJ|PNGT|PPM|PQR|SDF|CD|JSON|V2000|V3000|SPT|XJVXL|XYZ|XYZRN|XYZVIB|ZIP" + driverList.toUpperCase ().$replace (';', '|'));
if (this.chk) return "";
var bytes = null;
var doDefer = false;
if (data == null || isExport) {
data = type.intern ();
if (isExport) {
if (timeMsg) JU.Logger.startTimer ("export");
var eparams =  new java.util.Hashtable ();
eparams.put ("type", data);
if (fileName != null) eparams.put ("fileName", fileName);
if (isCommand || fileName != null) eparams.put ("fullPath", fullPath);
eparams.put ("width", Integer.$valueOf (width));
eparams.put ("height", Integer.$valueOf (height));
data = this.vwr.generateOutputForExport (eparams);
if (data == null || data.length == 0) return "";
if (!isCommand) return data;
if ((type.equals ("Povray") || type.equals ("Idtf")) && fullPath[0] != null) {
var ext = (type.equals ("Idtf") ? ".tex" : ".ini");
fileName = fullPath[0] + ext;
params =  new java.util.Hashtable ();
params.put ("fileName", fileName);
params.put ("type", ext);
params.put ("text", data);
params.put ("fullPath", fullPath);
msg = this.vwr.processWriteOrCapture (params);
if (type.equals ("Idtf")) data = data.substring (0, data.indexOf ("\\begin{comment}"));
data = "Created " + fullPath[0] + ":\n\n" + data;
if (timeMsg) this.showString (JU.Logger.getTimerMsg ("export", 0));
} else {
msg = data;
}if (msg != null) {
if (!msg.startsWith ("OK")) this.e.evalError (msg, null);
this.e.report (data);
}return "";
} else if (data === "MENU") {
data = this.vwr.getMenu ("");
} else if (data === "PGRP") {
data = this.vwr.getPointGroupAsString (type2.equals ("draw"), null, 0, 1.0);
} else if (data === "PDB" || data === "PQR") {
if (isShow) {
data = this.vwr.getPdbAtomData (null, null);
} else {
doDefer = true;
}} else if (data === "FILE") {
if (isShow) data = this.vwr.getCurrentFileAsString ();
 else doDefer = true;
if ("?".equals (fileName)) fileName = "?Jmol." + this.vwr.getP ("_fileType");
} else if ((data === "SDF" || data === "MOL" || data === "V2000" || data === "V3000" || data === "CD" || data === "JSON") && isCoord) {
data = this.vwr.getModelExtract ("selected", true, false, data);
if (data.startsWith ("ERROR:")) bytes = data;
} else if (data === "XYZ" || data === "XYZRN" || data === "XYZVIB" || data === "MOL" || data === "SDF" || data === "V2000" || data === "V3000" || data === "CML" || data === "CD" || data === "JSON") {
data = this.vwr.getData ("selected", data);
if (data.startsWith ("ERROR:")) bytes = data;
} else if (data === "FUNCTION") {
data = this.vwr.getFunctionCalls (null);
type = "TXT";
} else if (data === "VAR") {
if (tVar == null) {
tVar = this.e.getParameter (JS.SV.sValue (this.tokenAt (isCommand ? 2 : 1, args)), 1073742190, true);
}var v = null;
if (tVar.tok == 15) {
v =  new JU.Lst ();
v.addLast ((tVar.value).data);
} else if (tVar.tok == 6) {
var m = tVar.value;
if (m.containsKey ("$_BINARY_$")) {
v =  new JU.Lst ();
if (fileName != null) for (var e, $e = m.entrySet ().iterator (); $e.hasNext () && ((e = $e.next ()) || true);) {
var key = e.getKey ();
if (key.equals ("$_BINARY_$")) continue;
var o = e.getValue ();
bytes = (o.tok == 15 ? (o.value).data : null);
if (bytes == null) {
var s = o.asString ();
bytes = (s.startsWith (";base64,") ? JU.Base64.decodeBase64 (s) : s.getBytes ());
}if (key.equals ("_DATA_")) {
v = null;
if (bytes == null) bytes = (o.value).data;
break;
} else if (key.equals ("_IMAGE_")) {
v.add (0, key);
v.add (1, bytes);
} else {
v.addLast (key);
v.addLast (null);
v.addLast (bytes);
}}
}}if (v == null) {
if (bytes == null) {
data = tVar.asString ();
type = "TXT";
}} else {
if (fileName != null && (bytes = data = this.vwr.createZip (fileName, v.size () == 1 ? "BINARY" : "ZIPDATA", v)) == null) this.e.evalError ("#CANCELED#", null);
}} else if (data === "SPT") {
if (isCoord) {
var tainted = this.vwr.ms.getTaintedAtoms (2);
this.vwr.setAtomCoordsRelative (JU.P3.new3 (0, 0, 0), null);
data = this.vwr.getStateInfo ();
this.vwr.ms.setTaintedAtoms (tainted, 2);
} else {
data = this.vwr.getStateInfo ();
if (localPath != null || remotePath != null) data = JV.FileManager.setScriptFileReferences (data, localPath, remotePath, null);
}} else if (data === "ZIP" || data === "ZIPALL") {
if (fileName != null && (bytes = data = this.vwr.createZip (fileName, type, scripts)) == null) this.e.evalError ("#CANCELED#", null);
} else if (data === "HISTORY") {
data = this.vwr.getSetHistory (2147483647);
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
this.e.refresh (false);
if (width < 0) width = this.vwr.getScreenWidth ();
if (height < 0) height = this.vwr.getScreenHeight ();
}}if (!isCommand) return data;
if (isShow) {
this.e.showStringPrint (data, true);
return "";
}if (bytes != null && Clazz.instanceOf (bytes, String)) {
{
if (bytes.indexOf("OK") != 0)alert(bytes);
}this.e.report (bytes);
return bytes;
}if (type.equals ("SCENE")) bytes = sceneType;
 else if (bytes == null && (!isImage || fileName != null)) bytes = data;
if (timeMsg) JU.Logger.startTimer ("write");
if (doDefer) {
msg = this.vwr.writeFileData (fileName, type, 0, null);
} else {
params =  new java.util.Hashtable ();
if (fileName != null) params.put ("fileName", fileName);
params.put ("type", type);
if (Clazz.instanceOf (bytes, String) && quality == -2147483648) params.put ("text", bytes);
 else if (Clazz.instanceOf (bytes, Array)) params.put ("bytes", bytes);
if (scripts != null) params.put ("scripts", scripts);
if (bsFrames != null) params.put ("bsFrames", bsFrames);
params.put ("fullPath", fullPath);
params.put ("quality", Integer.$valueOf (quality));
params.put ("width", Integer.$valueOf (width));
params.put ("height", Integer.$valueOf (height));
params.put ("nVibes", Integer.$valueOf (nVibes));
msg = this.vwr.processWriteOrCapture (params);
}if (timeMsg) this.showString (JU.Logger.getTimerMsg ("write", 0));
}if (!this.chk && msg != null) {
if (!msg.startsWith ("OK")) {
this.e.evalError (msg, null);
{
alert(msg);
}}this.e.report (msg + (isImage ? "; width=" + width + "; height=" + height : ""));
return msg;
}return "";
}, "~A");
Clazz.defineMethod (c$, "show", 
 function () {
var value = null;
var str = this.paramAsStr (1);
var msg = null;
var name = null;
var len = 2;
var token = this.getToken (1);
var tok = (Clazz.instanceOf (token, JS.SV) ? 0 : token.tok);
if (tok == 4) {
token = JS.T.getTokenFromName (str.toLowerCase ());
if (token != null) tok = token.tok;
}if (tok != 1297090050 && tok != 1073742158) this.checkLength (-3);
if (this.slen == 2 && str.indexOf ("?") >= 0) {
this.showString (this.vwr.getAllSettings (str.substring (0, str.indexOf ("?"))));
return;
}switch (tok) {
case 0:
if (!this.chk) msg = (this.e.theToken).escape ();
break;
case 1073741925:
this.e.checkLength23 ();
len = this.st.length;
if (!this.chk) {
var d = this.vwr.ms.getInfo (this.vwr.am.cmi, "domains");
if (Clazz.instanceOf (d, JS.SV)) msg = this.vwr.getAnnotationInfo (d, this.e.optParameterAsString (2), 1073741925);
 else msg = "domain information has not been loaded";
}break;
case 1073742189:
this.e.checkLength23 ();
len = this.st.length;
if (!this.chk) {
var d = this.vwr.ms.getInfo (this.vwr.am.cmi, "validation");
if (Clazz.instanceOf (d, JS.SV)) msg = this.vwr.getAnnotationInfo (d, this.e.optParameterAsString (2), 1073742189);
 else msg = "validation information has not been loaded";
}break;
case 135270423:
if (!this.chk) msg = JU.Escape.e (this.vwr.cacheList ());
break;
case 1073741916:
this.e.checkLength23 ();
len = this.st.length;
if (!this.chk) {
var d = this.vwr.ms.getInfo (this.vwr.am.cmi, "dssr");
if (d == null) msg = "no DSSR information has been read";
 else if (len > 2) msg = JS.SV.getVariable (this.vwr.extractProperty (d, this.stringParameter (2), -1)).asString ();
 else msg = "" + JS.SV.getVariable (d).asString ();
}break;
case 1073741915:
this.checkLength (2);
if (!this.chk) msg = this.vwr.calculateStructures (null, true, false);
break;
case 545259571:
this.checkLength (2);
if (!this.chk) msg = this.vwr.getPathForAllFiles ();
break;
case 1073742038:
if (this.e.optParameterAsString (2).equalsIgnoreCase ("1H")) {
len = 3;
if (!this.chk) msg = this.vwr.getNMRPredict (false);
break;
}if (!this.chk) this.vwr.getNMRPredict (true);
return;
case 135267336:
case 1073741929:
case 1073741879:
this.checkLength (tok == 1073741879 ? 3 : 2);
if (this.chk) return;
try {
msg = this.vwr.getSmiles (null);
} catch (ex) {
if (Clazz.exceptionOf (ex, Exception)) {
msg = ex.getMessage ();
} else {
throw ex;
}
}
switch (tok) {
case 1073741929:
if (msg.length > 0) {
this.vwr.show2D (msg);
return;
}msg = "Could not show drawing -- Either insufficient atoms are selected or the model is a PDB file.";
break;
case 1073741879:
len = 3;
if (msg.length > 0) {
msg = this.vwr.getChemicalInfo (msg, this.getToken (2));
if (msg.indexOf ("FileNotFound") >= 0) msg = "?";
} else {
msg = "Could not show name -- Either insufficient atoms are selected or the model is a PDB file.";
}}
break;
case 1297090050:
var type;
var iop = 0;
var pt1 = null;
var pt2 = null;
if (this.slen > 3 && this.tokAt (3) != 4) {
pt1 = this.centerParameter (2);
pt2 = this.centerParameter (++this.e.iToken);
} else {
iop = (this.tokAt (2) == 2 ? this.intParameter (2) : 0);
}type = (this.tokAt (this.e.iToken + 1) == 4 ? this.stringParameter (++this.e.iToken) : null);
this.checkLength (len = ++this.e.iToken);
if (!this.chk) msg = this.vwr.getSymmetryOperation (iop, pt1, pt2, type);
break;
case 1649412120:
var vdwType = null;
if (this.slen > 2) {
vdwType = J.c.VDW.getVdwType (this.paramAsStr (2));
if (vdwType == null) this.invArg ();
}if (!this.chk) this.showString (this.vwr.getDefaultVdwNameOrData (0, vdwType, null));
return;
case 135368713:
this.e.checkLength23 ();
if (!this.chk) this.showString (this.vwr.getFunctionCalls (this.e.optParameterAsString (2)));
return;
case 1085443:
this.checkLength (2);
if (!this.chk) this.showString (this.vwr.getAllSettings (null));
return;
case 1074790760:
if ((len = this.slen) == 2) {
if (!this.chk) this.vwr.showUrl (this.e.getFullPathName ());
return;
}name = this.paramAsStr (2);
if (!this.chk) this.vwr.showUrl (name);
return;
case 1766856708:
str = "defaultColorScheme";
break;
case 1610612740:
str = "scaleAngstromsPerInch";
break;
case 135270418:
case 1052714:
if (this.chk) return;
var modelIndex = this.vwr.am.cmi;
if (modelIndex < 0) this.e.errorStr (30, "show " + this.e.theToken.value);
msg = this.plot (this.st);
len = this.slen;
break;
case 14:
case 1113200654:
if (!this.chk) msg = this.getContext (false);
break;
case 1073741888:
name = this.e.optParameterAsString (2);
if (name.length > 0) len = 3;
if (!this.chk) value = this.vwr.getColorSchemeList (name);
break;
case 1073742192:
if (!this.chk) msg = this.vwr.getAtomDefs (this.e.definedAtomSets) + this.vwr.g.getVariableList () + this.getContext (true);
break;
case 536870926:
if (!this.chk) msg = this.vwr.getTrajectoryState ();
break;
case 553648148:
value = "" + this.e.commandHistoryLevelMax;
break;
case 553648150:
value = "" + JU.Logger.getLogLevel ();
break;
case 603979825:
value = "" + this.vwr.getBoolean (603979825);
break;
case 553648178:
msg = "set strandCountForStrands " + this.vwr.getStrandCount (12) + "; set strandCountForMeshRibbon " + this.vwr.getStrandCount (13);
break;
case 536875070:
msg = this.vwr.showTimeout ((len = this.slen) == 2 ? null : this.paramAsStr (2));
break;
case 536870918:
value = JU.Escape.eP (this.vwr.getDefaultLattice ());
break;
case 4126:
if (!this.chk) msg = this.vwr.getMinimizationInfo ();
break;
case 1611272194:
switch (this.vwr.g.axesMode) {
case J.c.AXES.UNITCELL:
msg = "set axesUnitcell";
break;
case J.c.AXES.BOUNDBOX:
msg = "set axesWindow";
break;
default:
msg = "set axesMolecular";
}
break;
case 1610612737:
msg = "set bondMode " + (this.vwr.getBoolean (603979812) ? "OR" : "AND");
break;
case 1650071565:
if (!this.chk) msg = "set strandCountForStrands " + this.vwr.getStrandCount (12) + "; set strandCountForMeshRibbon " + this.vwr.getStrandCount (13);
break;
case 1612189718:
msg = "set hbondsBackbone " + this.vwr.getBoolean (603979852) + ";set hbondsSolid " + this.vwr.getBoolean (603979854);
break;
case 1611141175:
if (!this.chk) msg = this.vwr.getSpinState ();
break;
case 1611141176:
msg = "set ssbondsBackbone " + this.vwr.getBoolean (603979952);
break;
case 1610625028:
case 1611141171:
msg = "selectionHalos " + (this.vwr.getSelectionHaloEnabled (false) ? "ON" : "OFF");
break;
case 1613758470:
msg = "set selectHetero " + this.vwr.getBoolean (1613758470);
break;
case 1073741828:
msg = JU.Escape.eAP (this.vwr.getAdditionalHydrogens (null, true, true, null));
break;
case 1613758476:
msg = "set selectHydrogens " + this.vwr.getBoolean (1613758476);
break;
case 553648130:
case 553648142:
case 536870924:
case 553648176:
case 553648172:
case 1073741995:
if (!this.chk) msg = this.vwr.getSpecularState ();
break;
case 1073742136:
case 4146:
if (!this.chk) msg = this.vwr.stm.listSavedStates ();
break;
case 1614417948:
if (!this.chk) msg = this.vwr.getUnitCellInfoText ();
break;
case 1048581:
if ((len = this.slen) == 2) {
if (!this.chk) msg = this.vwr.getCoordinateState (this.vwr.bsA ());
break;
}var nameC = this.paramAsStr (2);
if (!this.chk) msg = this.vwr.stm.getSavedCoordinates (nameC);
break;
case 1073742158:
if (!this.chk && this.e.outputBuffer == null) this.vwr.sm.clearConsole ();
if ((len = this.slen) == 2) {
if (!this.chk) msg = this.vwr.getStateInfo ();
break;
}name = this.paramAsStr (2);
if (name.equals ("/") && (len = this.slen) == 4) {
name = this.paramAsStr (3).toLowerCase ();
if (!this.chk) {
var info = JU.PT.split (this.vwr.getStateInfo (), "\n");
var sb =  new JU.SB ();
for (var i = 0; i < info.length; i++) if (info[i].toLowerCase ().indexOf (name) >= 0) sb.append (info[i]).appendC ('\n');

msg = sb.toString ();
}break;
} else if (this.tokAt (2) == 1229984263 && (len = this.slen) == 4) {
if (!this.chk) msg = this.vwr.getEmbeddedFileState (this.paramAsStr (3), true);
break;
}len = 3;
if (!this.chk) msg = this.vwr.stm.getSavedState (name);
break;
case 1641025539:
if ((len = this.slen) == 2) {
if (!this.chk) msg = this.vwr.getProteinStructureState ();
break;
}var shape = this.paramAsStr (2);
if (!this.chk) msg = this.vwr.stm.getSavedStructure (shape);
break;
case 135270408:
type = ((len = this.slen) == 3 ? this.paramAsStr (2) : null);
if (!this.chk) {
var data = (type == null ? this.lastData : this.vwr.getData (type));
msg = (data == null ? "no data" : JU.Escape.encapsulateData (data[0], data[1], (data[3]).intValue ()));
}break;
case 1073742152:
var info = null;
if ((len = this.slen) == 2) {
if (!this.chk) {
info = this.vwr.getSpaceGroupInfo (null);
}} else {
var sg = this.paramAsStr (2);
if (!this.chk) info = this.vwr.getSpaceGroupInfo (JU.PT.rep (sg, "''", "\""));
}if (info != null) msg = "" + info.get ("spaceGroupInfo") + info.get ("symmetryInfo");
break;
case 1048582:
len = 3;
msg = this.e.setObjectProperty ();
break;
case 1679429641:
if (!this.chk) {
msg = this.vwr.ms.getBoundBoxCommand (true);
}break;
case 12289:
if (!this.chk) msg = "center " + JU.Escape.eP (this.vwr.tm.getRotationCenter ());
break;
case 135176:
if (!this.chk) msg = this.getShapeProperty (22, "command");
break;
case 1229984263:
if (!this.chk) this.vwr.sm.clearConsole ();
if (this.slen == 2) {
if (!this.chk) msg = this.vwr.getCurrentFileAsString ();
if (msg == null) msg = "<unavailable>";
break;
}len = 3;
value = this.paramAsStr (2);
if (!this.chk) msg = this.vwr.getFileAsString (value, true);
break;
case 4115:
if (this.tokAt (2) == 1048579 && (len = 3) > 0) msg = this.vwr.getModelFileInfoAll ();
 else msg = this.vwr.getModelFileInfo ();
break;
case 1610616855:
var n = ((len = this.slen) == 2 ? 2147483647 : this.intParameter (2));
if (n < 1) this.invArg ();
if (!this.chk) {
this.vwr.sm.clearConsole ();
if (this.e.scriptLevel == 0) this.vwr.removeCommand ();
msg = this.vwr.getSetHistory (n);
}break;
case 135180:
if (!this.chk) msg = this.getShapeProperty (24, "jvxlDataXml");
break;
case 1183762:
if (this.e.optParameterAsString (2).equalsIgnoreCase ("list")) {
msg = this.vwr.getMoInfo (-1);
len = 3;
} else {
var ptMO = ((len = this.slen) == 2 ? -2147483648 : this.intParameter (2));
if (!this.chk) msg = this.getMoJvxl (ptMO);
}break;
case 1095766030:
if (!this.chk) msg = this.vwr.ms.getModelInfoAsString ();
break;
case 537006096:
if (!this.chk) msg = this.vwr.getMeasurementInfoAsString ();
break;
case 1073741863:
len = 3;
if (!this.chk && this.slen == len) msg = this.vwr.getOrientationText (this.tokAt (2), null);
break;
case 1073742132:
tok = this.tokAt (2);
if (tok == 0) tok = 1073742132;
 else len = 3;
case 1073742178:
case 4130:
if (!this.chk) msg = this.vwr.getOrientationText (tok, null);
break;
case 1073742077:
len = 2;
if (this.slen > 3) break;
switch (tok = this.tokAt (2)) {
case 1073742178:
case 1073742132:
case 4130:
case 0:
if (!this.chk) msg = this.vwr.getOrientationText (tok, null);
break;
default:
name = this.e.optParameterAsString (2);
msg = this.vwr.getOrientationText (1073742035, name);
}
len = this.slen;
break;
case 1073742088:
if (!this.chk) msg = this.vwr.getPDBHeader ();
break;
case 1073742102:
if (!this.chk) this.showString (this.vwr.getPointGroupAsString (false, null, 0, 0));
return;
case 1089470478:
if (!this.chk) msg = this.vwr.ms.getSymmetryInfoAsString ();
break;
case 1073742176:
if (!this.chk) msg = "transform:\n" + this.vwr.tm.getTransformText ();
break;
case 4168:
msg = "zoom " + (this.vwr.tm.zoomEnabled ? ("" + this.vwr.tm.getZoomSetting ()) : "off");
break;
case 1611272202:
msg = (this.vwr.getShowFrank () ? "frank ON" : "frank OFF");
break;
case 1666189314:
str = "solventProbeRadius";
break;
case 1087373320:
if ((len = this.slen) == 3 && this.tokAt (2) == 1048588) tok = 1087373319;
case 1073741864:
case 1087373316:
case 1073742120:
case 1114638363:
case 1087373318:
case 1141899265:
case 1073741982:
if (!this.chk) msg = this.vwr.getChimeInfo (tok);
break;
case 537022465:
case 1610612738:
case 1716520985:
case 20482:
case 1613758488:
value = "?";
break;
case 1073742031:
var qualifiers = ((len = this.slen) == 2 ? null : this.paramAsStr (2));
if (!this.chk) msg = this.vwr.getBindingInfo (qualifiers);
break;
case 1073742015:
if (!this.chk) value = this.vwr.getMenu ("");
break;
case 1073741824:
if (str.equalsIgnoreCase ("fileHeader")) {
if (!this.chk) msg = this.vwr.getPDBHeader ();
}break;
case 1073741992:
case 36868:
str = this.paramAsStr (len++);
var v = this.e.getParameter (str, 1073742190, true);
if (!this.chk) if (tok == 1073741992) {
msg = v.toJSON ();
} else {
msg = v.escape ();
}break;
}
this.checkLength (len);
if (this.chk) return;
if (msg != null) this.showString (msg);
 else if (value != null) this.showString (str + " = " + value);
 else if (str != null) {
if (str.indexOf (" ") >= 0) this.showString (str);
 else this.showString (str + " = " + (this.e.getParameter (str, 1073742190, true)).escape ());
}});
Clazz.defineMethod (c$, "stereo", 
 function () {
var stereoMode = J.c.STER.DOUBLE;
var degrees = -5;
var degreesSeen = false;
var colors = null;
var colorpt = 0;
for (var i = 1; i < this.slen; ++i) {
if (this.e.isColorParam (i)) {
if (colorpt > 1) this.e.bad ();
if (colorpt == 0) colors =  Clazz.newIntArray (2, 0);
if (!degreesSeen) degrees = 3;
colors[colorpt] = this.e.getArgbParam (i);
if (colorpt++ == 0) colors[1] = ~colors[0];
i = this.e.iToken;
continue;
}switch (this.getToken (i).tok) {
case 1048589:
this.e.checkLast (this.e.iToken = 1);
this.e.iToken = 1;
break;
case 1048588:
this.e.checkLast (this.e.iToken = 1);
stereoMode = J.c.STER.NONE;
break;
case 2:
case 3:
degrees = this.floatParameter (i);
degreesSeen = true;
break;
case 1073741824:
if (!degreesSeen) degrees = 3;
stereoMode = J.c.STER.getStereoMode (this.paramAsStr (i));
if (stereoMode != null) break;
default:
this.invArg ();
}
}
if (this.chk) return;
this.vwr.setStereoMode (colors, stereoMode, degrees);
});
Clazz.defineMethod (c$, "struts", 
 function () {
var eval = this.e;
var defOn = (this.tokAt (1) == 1073742072 || this.tokAt (1) == 1048589 || this.slen == 1);
var mad = eval.getMadParameter ();
if (mad == 2147483647) return false;
if (defOn) mad = Math.round (this.vwr.getFloat (570425406) * 2000);
this.setShapeProperty (1, "type", Integer.$valueOf (32768));
eval.setShapeSizeBs (1, mad, null);
this.setShapeProperty (1, "type", Integer.$valueOf (1023));
return true;
});
Clazz.defineMethod (c$, "addShapeProperty", 
 function (propertyList, key, value) {
if (this.chk) return;
propertyList.addLast ([key, value]);
}, "JU.Lst,~S,~O");
Clazz.defineMethod (c$, "assign", 
 function () {
var atomsOrBonds = this.tokAt (1);
var index = this.atomExpressionAt (2).nextSetBit (0);
var index2 = -1;
var type = null;
if (index < 0) return;
if (atomsOrBonds == 4106) {
index2 = this.atomExpressionAt (++this.e.iToken).nextSetBit (0);
} else {
type = this.paramAsStr (++this.e.iToken);
}var pt = (++this.e.iToken < this.slen ? this.centerParameter (this.e.iToken) : null);
if (this.chk) return;
switch (atomsOrBonds) {
case 1141899265:
this.e.clearDefinedVariableAtomSets ();
this.assignAtom (index, pt, type);
break;
case 1678770178:
this.assignBond (index, (type + "p").charAt (0));
break;
case 4106:
this.assignConnect (index, index2);
}
});
Clazz.defineMethod (c$, "assignAtom", 
 function (atomIndex, pt, type) {
if (type.equals ("X")) this.vwr.setRotateBondIndex (-1);
if (this.vwr.ms.at[atomIndex].mi != this.vwr.ms.mc - 1) return;
this.vwr.clearModelDependentObjects ();
var ac = this.vwr.ms.getAtomCount ();
if (pt == null) {
this.vwr.sm.modifySend (atomIndex, this.vwr.ms.at[atomIndex].mi, 1, this.e.fullCommand);
this.vwr.ms.assignAtom (atomIndex, type, true);
if (!JU.PT.isOneOf (type, ";Mi;Pl;X;")) this.vwr.ms.setAtomNamesAndNumbers (atomIndex, -ac, null);
this.vwr.sm.modifySend (atomIndex, this.vwr.ms.at[atomIndex].mi, -1, "OK");
this.vwr.refresh (3, "assignAtom");
return;
}var atom = this.vwr.ms.at[atomIndex];
var bs = JU.BSUtil.newAndSetBit (atomIndex);
var pts = [pt];
var vConnections =  new JU.Lst ();
vConnections.addLast (atom);
var modelIndex = atom.mi;
this.vwr.sm.modifySend (atomIndex, modelIndex, 3, this.e.fullCommand);
try {
bs = this.vwr.addHydrogensInline (bs, vConnections, pts);
atomIndex = bs.nextSetBit (0);
this.vwr.ms.assignAtom (atomIndex, type, false);
} catch (ex) {
if (Clazz.exceptionOf (ex, Exception)) {
} else {
throw ex;
}
}
this.vwr.ms.setAtomNamesAndNumbers (atomIndex, -ac, null);
this.vwr.sm.modifySend (atomIndex, modelIndex, -3, "OK");
}, "~N,JU.P3,~S");
Clazz.defineMethod (c$, "assignBond", 
 function (bondIndex, type) {
var modelIndex = -1;
try {
modelIndex = this.vwr.getAtomModelIndex (this.vwr.ms.bo[bondIndex].getAtomIndex1 ());
this.vwr.sm.modifySend (bondIndex, modelIndex, 2, this.e.fullCommand);
var bsAtoms = this.vwr.ms.setBondOrder (bondIndex, type);
if (bsAtoms == null || type == '0') this.vwr.refresh (3, "setBondOrder");
 else this.vwr.addHydrogens (bsAtoms, false, true);
this.vwr.sm.modifySend (bondIndex, modelIndex, -2, "" + type);
} catch (ex) {
if (Clazz.exceptionOf (ex, Exception)) {
JU.Logger.error ("assignBond failed");
this.vwr.sm.modifySend (bondIndex, modelIndex, -2, "ERROR " + ex);
} else {
throw ex;
}
}
}, "~N,~S");
Clazz.defineMethod (c$, "assignConnect", 
 function (index, index2) {
this.vwr.clearModelDependentObjects ();
var connections = JU.AU.newFloat2 (1);
connections[0] = [index, index2];
var modelIndex = this.vwr.ms.at[index].mi;
this.vwr.sm.modifySend (index, modelIndex, 2, this.e.fullCommand);
this.vwr.ms.connect (connections);
this.vwr.ms.assignAtom (index, ".", true);
this.vwr.ms.assignAtom (index2, ".", true);
this.vwr.sm.modifySend (index, modelIndex, -2, "OK");
this.vwr.refresh (3, "assignConnect");
}, "~N,~N");
Clazz.defineMethod (c$, "getContext", 
 function (withVariables) {
var sb =  new JU.SB ();
var context = this.e.thisContext;
while (context != null) {
if (withVariables) {
if (context.vars != null) {
sb.append (this.getScriptID (context));
sb.append (JV.StateManager.getVariableList (context.vars, 80, true, false));
}} else {
sb.append (JS.ScriptError.getErrorLineMessage (context.functionName, context.scriptFileName, this.e.getLinenumber (context), context.pc, JS.ScriptEval.statementAsString (this.vwr, context.statement, -9999, this.e.debugHigh)));
}context = context.parentContext;
}
if (withVariables) {
if (this.e.contextVariables != null) {
sb.append (this.getScriptID (null));
sb.append (JV.StateManager.getVariableList (this.e.contextVariables, 80, true, false));
}} else {
sb.append (this.e.getErrorLineMessage2 ());
}return sb.toString ();
}, "~B");
Clazz.defineMethod (c$, "getAtomicPotentials", 
 function (bsSelected, bsIgnore, fileName) {
var potentials =  Clazz.newFloatArray (this.vwr.getAtomCount (), 0);
var m = J.api.Interface.getOption ("quantum.MlpCalculation");
m.set (this.vwr);
var data = (fileName == null ? null : this.vwr.getFileAsString (fileName, false));
m.assignPotentials (this.vwr.ms.at, potentials, this.vwr.getSmartsMatch ("a", bsSelected), this.vwr.getSmartsMatch ("/noAromatic/[$(C=O),$(O=C),$(NC=O)]", bsSelected), bsIgnore, data);
return potentials;
}, "JU.BS,JU.BS,~S");
Clazz.defineMethod (c$, "getColorTrans", 
 function (eval, i, allowNone, ret) {
var translucentLevel = 3.4028235E38;
if (eval.theTok != 1766856708) --i;
switch (this.tokAt (i + 1)) {
case 603979967:
i++;
translucentLevel = (this.isFloatParameter (i + 1) ? eval.getTranslucentLevel (++i) : this.vwr.getFloat (570425354));
break;
case 1073742074:
i++;
translucentLevel = 0;
break;
}
if (eval.isColorParam (i + 1)) {
ret[0] = eval.getArgbParam (++i);
} else if (this.tokAt (i + 1) == 1048587) {
ret[0] = 0;
eval.iToken = i + 1;
} else if (translucentLevel == 3.4028235E38) {
this.invArg ();
} else {
ret[0] = -2147483648;
}i = eval.iToken;
return translucentLevel;
}, "JS.ScriptEval,~N,~B,~A");
Clazz.defineMethod (c$, "getCapSlabObject", 
 function (i, isLcaoCartoon) {
if (i < 0) {
return JU.TempArray.getSlabWithinRange (i, 0);
}var eval = this.e;
var data = null;
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
if (eval.isColorParam (i + 1)) {
slabColix = Short.$valueOf (JU.C.getColixTranslucent3 (JU.C.getColix (eval.getArgbParam (i + 1)), slabTranslucency != 0, slabTranslucency));
i = eval.iToken;
} else {
slabColix = Short.$valueOf (JU.C.getColixTranslucent3 (1, slabTranslucency != 0, slabTranslucency));
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
case 1048588:
eval.iToken = i + 1;
return Integer.$valueOf (-2147483648);
case 1048587:
eval.iToken = i + 1;
break;
case 1048582:
i++;
data = [Float.$valueOf (1), this.paramAsStr (++i)];
tok = 1073742018;
break;
case 135266325:
i++;
if (this.tokAt (++i) == 1073742114) {
d = this.floatParameter (++i);
d2 = this.floatParameter (++i);
data = [Float.$valueOf (d), Float.$valueOf (d2)];
tok = 1073742114;
} else if (this.isFloatParameter (i)) {
d = this.floatParameter (i);
if (eval.isCenterParameter (++i)) {
var pt = this.centerParameter (i);
if (this.chk || !(Clazz.instanceOf (eval.expressionResult, JU.BS))) {
pts = [pt];
} else {
var atoms = this.vwr.ms.at;
bs = eval.expressionResult;
pts =  new Array (bs.cardinality ());
for (var k = 0, j = bs.nextSetBit (0); j >= 0; j = bs.nextSetBit (j + 1), k++) pts[k] = atoms[j];

}} else {
pts = eval.getPointArray (i, -1);
}if (pts.length == 0) {
eval.iToken = i;
this.invArg ();
}data = [Float.$valueOf (d), pts, bs];
} else {
data = eval.getPointArray (i, 4);
tok = 1679429641;
}break;
case 1679429641:
eval.iToken = i + 1;
data = JU.BoxInfo.getCriticalPoints (this.vwr.ms.getBBoxVertices (), null);
break;
case 1073741872:
case 1614417948:
eval.iToken = i + 1;
var unitCell = this.vwr.getCurrentUnitCell ();
if (unitCell == null) {
if (tok == 1614417948) this.invArg ();
} else {
pts = JU.BoxInfo.getCriticalPoints (unitCell.getUnitCellVertices (), unitCell.getCartesianOffset ());
var iType = Clazz.floatToInt (unitCell.getUnitCellInfoType (6));
var v1 = null;
var v2 = null;
switch (iType) {
case 3:
break;
case 1:
v2 = JU.V3.newVsub (pts[2], pts[0]);
v2.scale (1000);
case 2:
v1 = JU.V3.newVsub (pts[1], pts[0]);
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
case 10:
case 1048577:
data = this.atomExpressionAt (i + 1);
tok = 3;
if (!eval.isCenterParameter (++eval.iToken)) break;
data = null;
default:
if (!isLcaoCartoon && isSlab && this.isFloatParameter (i + 1)) {
d = this.floatParameter (++i);
if (!this.isFloatParameter (i + 1)) return Integer.$valueOf (Clazz.floatToInt (d));
d2 = this.floatParameter (++i);
data = [Float.$valueOf (d), Float.$valueOf (d2)];
tok = 1073742114;
break;
}plane = eval.planeParameter (++i);
var off = (this.isFloatParameter (eval.iToken + 1) ? this.floatParameter (++eval.iToken) : NaN);
if (!Float.isNaN (off)) plane.w -= off;
data = plane;
tok = 135266319;
}
var colorData = (slabMeshType == null ? null : [slabMeshType, slabColix]);
return JU.TempArray.getSlabObjectType (tok, data, !isSlab, colorData);
}, "~N,~B");
Clazz.defineMethod (c$, "getIsosurfaceJvxl", 
 function (asMesh, iShape) {
if (this.chk) return "";
return this.getShapeProperty (iShape, asMesh ? "jvxlMeshX" : "jvxlDataXml");
}, "~B,~N");
Clazz.defineMethod (c$, "getMoJvxl", 
 function (ptMO) {
this.sm.loadShape (27);
var modelIndex = this.vwr.am.cmi;
if (modelIndex < 0) this.e.errorStr (30, "MO isosurfaces");
var moData = this.vwr.getModelAuxiliaryInfoValue (modelIndex, "moData");
if (moData == null) this.error (27);
var n = this.getShapeProperty (27, "moNumber");
if (n == null || n.intValue () == 0) {
this.setShapeProperty (27, "init", Integer.$valueOf (modelIndex));
}this.setShapeProperty (27, "moData", moData);
return this.getShapePropertyIndex (27, "showMO", ptMO);
}, "~N");
Clazz.defineMethod (c$, "getScriptID", 
 function (context) {
var fuName = (context == null ? this.e.functionName : "function " + context.functionName);
var fiName = (context == null ? this.e.scriptFileName : context.scriptFileName);
return "\n# " + fuName + " (file " + fiName + (context == null ? "" : " context " + context.id) + ")\n";
}, "JS.ScriptContext");
Clazz.defineMethod (c$, "getShapePropertyIndex", 
 function (shapeType, propertyName, index) {
return this.sm.getShapePropertyIndex (shapeType, propertyName, index);
}, "~N,~S,~N");
Clazz.defineMethod (c$, "tokenAt", 
 function (i, args) {
return (i < args.length ? args[i] : null);
}, "~N,~A");
c$.tokAtArray = Clazz.defineMethod (c$, "tokAtArray", 
 function (i, args) {
return (i < args.length && args[i] != null ? args[i].tok : 0);
}, "~N,~A");
Clazz.defineMethod (c$, "finalizeObject", 
 function (shapeID, colorArgb, translucentLevel, intScale, doSet, data, iptDisplayProperty, bs) {
if (doSet) {
this.setShapeProperty (shapeID, "set", data);
}if (colorArgb != -2147483648) this.e.setShapePropertyBs (shapeID, "color", Integer.$valueOf (colorArgb), bs);
if (translucentLevel != 3.4028235E38) this.e.setShapeTranslucency (shapeID, "", "translucent", translucentLevel, bs);
if (intScale != 0) {
this.setShapeProperty (shapeID, "scale", Integer.$valueOf (intScale));
}if (iptDisplayProperty > 0) {
if (!this.e.setMeshDisplayProperty (shapeID, iptDisplayProperty, 0)) this.invArg ();
}}, "~N,~N,~N,~N,~B,~O,~N,JU.BS");
Clazz.defineMethod (c$, "moCombo", 
 function (propertyList) {
if (this.tokAt (this.e.iToken + 1) != 1073742156) return null;
this.addShapeProperty (propertyList, "squareLinear", Boolean.TRUE);
this.e.iToken++;
return  Clazz.newFloatArray (0, 0);
}, "JU.Lst");
Clazz.defineMethod (c$, "moOffset", 
 function (index) {
var isHomo = (this.getToken (index).tok == 1073741973);
var offset = (isHomo ? 0 : 1);
var tok = this.tokAt (++index);
if (tok == 2 && this.intParameter (index) < 0) offset += this.intParameter (index);
 else if (tok == 269484193) offset += this.intParameter (++index);
 else if (tok == 269484192) offset -= this.intParameter (++index);
return offset;
}, "~N");
Clazz.defineMethod (c$, "setMoData", 
 function (propertyList, moNumber, lc, offset, isNegOffset, modelIndex, title) {
var eval = this.e;
if (this.chk) return;
if (modelIndex < 0) {
modelIndex = this.vwr.am.cmi;
if (modelIndex < 0) eval.errorStr (30, "MO isosurfaces");
}var moData = this.vwr.getModelAuxiliaryInfoValue (modelIndex, "moData");
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
}JU.Logger.info ("MO " + moNumber);
}if (moNumber < 1 || moNumber > nOrb) eval.errorStr (26, "" + nOrb);
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
var bs = JU.BS.newN (nOrb);
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
}, "JU.Lst,~N,~A,~N,~B,~N,~S");
Clazz.defineMethod (c$, "getPlotMinMax", 
 function (data, isMax, tok) {
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
}, "~A,~B,~N");
Clazz.defineMethod (c$, "initIsosurface", 
 function (iShape) {
var eval = this.e;
this.setShapeProperty (iShape, "init", this.fullCommand);
eval.iToken = 0;
var tok1 = this.tokAt (1);
var tok2 = this.tokAt (2);
if (tok1 == 12291 || tok2 == 12291 && this.tokAt (++eval.iToken) == 1048579) {
this.setShapeProperty (iShape, "delete", null);
eval.iToken += 2;
if (this.slen > eval.iToken) {
this.setShapeProperty (iShape, "init", this.fullCommand);
this.setShapeProperty (iShape, "thisID", "+PREVIOUS_MESH+");
}return null;
}eval.iToken = 1;
if (!eval.setMeshDisplayProperty (iShape, 0, tok1)) {
this.setShapeProperty (iShape, "thisID", "+PREVIOUS_MESH+");
if (iShape != 22) this.setShapeProperty (iShape, "title", [this.thisCommand]);
if (tok1 != 1074790550 && (tok2 == 269484209 || tok1 == 269484209 && eval.setMeshDisplayProperty (iShape, 0, tok2))) {
var id = this.setShapeId (iShape, 1, false);
eval.iToken++;
return id;
}}return null;
}, "~N");
Clazz.defineMethod (c$, "getWithinDistanceVector", 
 function (propertyList, distance, ptc, bs, isShow) {
var v =  new JU.Lst ();
var pts =  new Array (2);
if (bs == null) {
var pt1 = JU.P3.new3 (distance, distance, distance);
var pt0 = JU.P3.newP (ptc);
pt0.sub (pt1);
pt1.add (ptc);
pts[0] = pt0;
pts[1] = pt1;
v.addLast (ptc);
} else {
var bbox = this.vwr.ms.getBoxInfo (bs, -Math.abs (distance));
pts[0] = bbox.getBoundBoxVertices ()[0];
pts[1] = bbox.getBoundBoxVertices ()[7];
if (bs.cardinality () == 1) v.addLast (this.vwr.getAtomPoint3f (bs.nextSetBit (0)));
}if (v.size () == 1 && !isShow) {
this.addShapeProperty (propertyList, "withinDistance", Float.$valueOf (distance));
this.addShapeProperty (propertyList, "withinPoint", v.get (0));
}this.addShapeProperty (propertyList, (isShow ? "displayWithin" : "withinPoints"), [Float.$valueOf (distance), pts, bs, v]);
}, "JU.Lst,~N,JU.P3,JU.BS,~B");
Clazz.defineMethod (c$, "setColorOptions", 
 function (sb, index, iShape, nAllowed) {
var eval = this.e;
this.getToken (index);
var translucency = "opaque";
if (eval.theTok == 603979967) {
translucency = "translucent";
if (nAllowed < 0) {
var value = (this.isFloatParameter (index + 1) ? this.floatParameter (++index) : 3.4028235E38);
eval.setShapeTranslucency (iShape, null, "translucent", value, null);
if (sb != null) {
sb.append (" translucent");
if (value != 3.4028235E38) sb.append (" ").appendF (value);
}} else {
eval.setMeshDisplayProperty (iShape, index, eval.theTok);
}} else if (eval.theTok == 1073742074) {
if (nAllowed >= 0) eval.setMeshDisplayProperty (iShape, index, eval.theTok);
} else {
eval.iToken--;
}nAllowed = Math.abs (nAllowed);
for (var i = 0; i < nAllowed; i++) {
if (eval.isColorParam (eval.iToken + 1)) {
var color = eval.getArgbParam (++eval.iToken);
this.setShapeProperty (iShape, "colorRGB", Integer.$valueOf (color));
if (sb != null) sb.append (" ").append (JU.Escape.escapeColor (color));
} else if (eval.iToken < index) {
this.invArg ();
} else {
break;
}}
return translucency;
}, "JU.SB,~N,~N,~N");
Clazz.defineMethod (c$, "createFunction", 
 function (fname, xyz, ret) {
var e = ( new JS.ScriptEval ()).setViewer (this.vwr);
try {
e.compileScript (null, "function " + fname + "(" + xyz + ") { return " + ret + "}", false);
var params =  new JU.Lst ();
for (var i = 0; i < xyz.length; i += 2) params.addLast (JS.SV.newV (3, Float.$valueOf (0)).setName (xyz.substring (i, i + 1)));

return [e.aatoken[0][1].value, params];
} catch (ex) {
if (Clazz.exceptionOf (ex, Exception)) {
return null;
} else {
throw ex;
}
}
}, "~S,~S,~S");
Clazz.defineMethod (c$, "floatArraySet", 
 function (i, nX, nY) {
var tok = this.tokAt (i++);
if (tok == 1073742195) tok = this.tokAt (i++);
if (tok != 269484096) this.invArg ();
var fparams = JU.AU.newFloat2 (nX);
var n = 0;
while (tok != 269484097) {
tok = this.getToken (i).tok;
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
}, "~N,~N,~N");
Clazz.defineMethod (c$, "floatArraySetXYZ", 
 function (i, nX, nY, nZ) {
var eval = this.e;
var tok = this.tokAt (i++);
if (tok == 1073742195) tok = this.tokAt (i++);
if (tok != 269484096 || nX <= 0) this.invArg ();
var fparams = JU.AU.newFloat3 (nX, -1);
var n = 0;
while (tok != 269484097) {
tok = this.getToken (i).tok;
switch (tok) {
case 1073742195:
case 269484097:
continue;
case 269484080:
i++;
break;
case 269484096:
fparams[n++] = this.floatArraySet (i, nY, nZ);
i = ++eval.iToken;
tok = 0;
if (n == nX && this.tokAt (i) != 269484097) this.invArg ();
break;
default:
this.invArg ();
}
}
return fparams;
}, "~N,~N,~N,~N");
Clazz.overrideMethod (c$, "getBitsetIdent", 
function (bs, label, tokenValue, useAtomMap, index, isExplicitlyAll) {
var isAtoms = !(Clazz.instanceOf (tokenValue, JM.BondSet));
if (isAtoms) {
if (label == null) label = this.vwr.getStandardLabelFormat (0);
 else if (label.length == 0) label = "%[label]";
}var pt = (label == null ? -1 : label.indexOf ("%"));
var haveIndex = (index != 2147483647);
if (bs == null || this.chk || isAtoms && pt < 0) {
if (label == null) label = "";
return isExplicitlyAll ? [label] : label;
}var modelSet = this.vwr.ms;
var n = 0;
var labeler = modelSet.getLabeler ();
var indices = (isAtoms || !useAtomMap ? null : (tokenValue).getAssociatedAtoms ());
if (indices == null && label != null && label.indexOf ("%D") > 0) indices = this.vwr.ms.getAtomIndices (bs);
var asIdentity = (label == null || label.length == 0);
var htValues = (isAtoms || asIdentity ? null : JM.LabelToken.getBondLabelValues ());
var tokens = (asIdentity ? null : isAtoms ? labeler.compile (this.vwr, label, '\0', null) : labeler.compile (this.vwr, label, '\1', htValues));
var nmax = (haveIndex ? 1 : JU.BSUtil.cardinalityOf (bs));
var sout =  new Array (nmax);
var ptTemp =  new JU.P3 ();
for (var j = (haveIndex ? index : bs.nextSetBit (0)); j >= 0; j = bs.nextSetBit (j + 1)) {
var str;
if (isAtoms) {
if (asIdentity) str = modelSet.at[j].getInfo ();
 else str = labeler.formatLabelAtomArray (this.vwr, modelSet.at[j], tokens, '\0', indices, ptTemp);
} else {
var bond = modelSet.getBondAt (j);
if (asIdentity) str = bond.getIdentity ();
 else str = labeler.formatLabelBond (this.vwr, bond, tokens, htValues, indices, ptTemp);
}str = JU.Txt.formatStringI (str, "#", (n + 1));
sout[n++] = str;
if (haveIndex) break;
}
return nmax == 1 && !isExplicitlyAll ? sout[0] : sout;
}, "JU.BS,~S,~O,~B,~N,~B");
Clazz.defineMethod (c$, "listIsosurface", 
 function (iShape) {
var s = (this.slen > 3 ? "0" : this.tokAt (2) == 0 ? "" : " " + this.getToken (2).value);
if (!this.chk) this.showString (this.getShapeProperty (iShape, "list" + s));
return true;
}, "~N");
Clazz.defineMethod (c$, "setShapeId", 
 function (iShape, i, idSeen) {
if (idSeen) this.invArg ();
var name = this.e.setShapeNameParameter (i).toLowerCase ();
this.setShapeProperty (iShape, "thisID", name);
return name;
}, "~N,~N,~B");
Clazz.defineMethod (c$, "parseDataArray", 
 function (str, is3D) {
str = JU.Parser.fixDataString (str);
var lines = JU.Parser.markLines (str, '\n');
var nLines = lines.length;
if (!is3D) {
var data = JU.AU.newFloat2 (nLines);
for (var iLine = 0, pt = 0; iLine < nLines; pt = lines[iLine++]) {
var tokens = JU.PT.getTokens (str.substring (pt, lines[iLine]));
JU.PT.parseFloatArrayData (tokens, data[iLine] =  Clazz.newFloatArray (tokens.length, 0));
}
return data;
}var tokens = JU.PT.getTokens (str.substring (0, lines[0]));
if (tokens.length != 3) return  Clazz.newFloatArray (0, 0, 0, 0);
var nX = JU.PT.parseInt (tokens[0]);
var nY = JU.PT.parseInt (tokens[1]);
var nZ = JU.PT.parseInt (tokens[2]);
if (nX < 1 || nY < 1 || nZ < 1) return  Clazz.newFloatArray (1, 1, 1, 0);
var data = JU.AU.newFloat3 (nX, nY);
var iX = 0;
var iY = 0;
for (var iLine = 1, pt = lines[0]; iLine < nLines && iX < nX; pt = lines[iLine++]) {
tokens = JU.PT.getTokens (str.substring (pt, lines[iLine]));
if (tokens.length < nZ) continue;
JU.PT.parseFloatArrayData (tokens, data[iX][iY] =  Clazz.newFloatArray (tokens.length, 0));
if (++iY == nY) {
iX++;
iY = 0;
}}
if (iX != nX) {
System.out.println ("Error reading 3D data -- nX = " + nX + ", but only " + iX + " blocks read");
return  Clazz.newFloatArray (1, 1, 1, 0);
}return data;
}, "~S,~B");
Clazz.defineStatics (c$,
"ERROR_invalidArgument", 22);
});
