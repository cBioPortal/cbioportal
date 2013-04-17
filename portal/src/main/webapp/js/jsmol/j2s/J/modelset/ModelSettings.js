Clazz.declarePackage ("J.modelset");
Clazz.load (null, "J.modelset.ModelSettings", ["java.lang.Boolean", "$.Float", "J.util.BSUtil", "$.Escape"], function () {
c$ = Clazz.decorateAsClass (function () {
this.id = 0;
this.bsAtoms = null;
this.info = null;
this.size = -1;
this.colixes = null;
this.colors = null;
this.argb = 0;
this.translucency = 0;
this.rd = null;
Clazz.instantialize (this, arguments);
}, J.modelset, "ModelSettings");
Clazz.makeConstructor (c$, 
function (id, bsAtoms, info) {
this.id = id;
this.bsAtoms = bsAtoms;
this.info = info;
}, "~N,J.util.BS,~O");
$_M(c$, "offset", 
function (modelOffset, atomOffset) {
if (atomOffset <= 0) return;
if (this.id == 1073742032) {
var movie = this.info;
movie.put ("baseModel", Integer.$valueOf (modelOffset));
var aStates = movie.get ("states");
for (var i = aStates.size (); --i >= 0; ) J.util.BSUtil.offset (aStates.get (i), 0, atomOffset);

return;
}if (this.id == 1060866) {
var defs = this.info;
for (var i = defs.size (); --i >= 0; ) J.util.BSUtil.offset (defs.get (i), 0, atomOffset);

return;
}if (this.bsAtoms != null) J.util.BSUtil.offset (this.bsAtoms, 0, atomOffset);
if (this.colixes != null) {
var c =  Clazz.newShortArray (this.colixes.length + atomOffset, 0);
System.arraycopy (this.colixes, 0, c, atomOffset, this.colixes.length);
this.colixes = c;
}}, "~N,~N");
$_M(c$, "createShape", 
function (m) {
var sm = m.shapeManager;
var modelIndex = this.getModelIndex (m);
switch (this.id) {
case 1073742032:
sm.viewer.setMovie (this.info);
return;
case 4115:
var frame = (this.info).intValue ();
if (frame > 0) sm.viewer.setCurrentModelIndex (frame + modelIndex - 1);
 else {
sm.viewer.setAnimationRange (-1, -1);
sm.viewer.setCurrentModelIndex (-1);
}return;
case 3145770:
sm.viewer.displayAtoms (this.bsAtoms, false, false, Boolean.TRUE, true);
return;
case 1060866:
sm.viewer.defineAtomSets (this.info);
return;
case 23:
if (modelIndex < 0) return;
sm.setShapePropertyBs (0, "colors", this.colors, this.bsAtoms);
var s = this.info.toString ().$replace ('\'', '_').$replace ('"', '_');
s = "script('isosurface ID \"" + s + "\"  model " + m.models[modelIndex].getModelNumberDotted () + " select (" + J.util.Escape.eBS (this.bsAtoms) + " and not solvent) only solvent " + (this.size / 1000) + " map property color')";
if (this.translucency > 0) s += " translucent " + this.translucency;
System.out.println ("shapeSettings: " + s);
sm.viewer.evaluateExpression (s);
return;
case 5:
sm.loadShape (this.id);
sm.setShapePropertyBs (this.id, "labels", this.info, this.bsAtoms);
return;
case 16:
sm.loadShape (this.id);
sm.setShapePropertyBs (this.id, "ignore", J.util.BSUtil.copyInvert (this.bsAtoms, sm.viewer.getAtomCount ()), null);
break;
case 6:
if (modelIndex < 0) return;
sm.loadShape (this.id);
var md = this.info;
md.setModelSet (m);
var points = md.points;
for (var i = points.size (); --i >= 0; ) (points.get (i)).modelIndex = modelIndex;

sm.setShapePropertyBs (this.id, "measure", md, this.bsAtoms);
if (this.size != -1) sm.setShapeSizeBs (this.id, this.size, null, null);
return;
}
if (this.size != -1 || this.rd != null) sm.setShapeSizeBs (this.id, this.size, this.rd, this.bsAtoms);
if (this.argb != 0) sm.setShapePropertyBs (this.id, "color", Integer.$valueOf (this.argb), this.bsAtoms);
 else if (this.colors != null) sm.setShapePropertyBs (this.id, "colors", this.colors, this.bsAtoms);
}, "J.modelset.ModelSet");
$_M(c$, "getModelIndex", 
($fz = function (m) {
if (this.bsAtoms == null) return -1;
var iAtom = this.bsAtoms.nextSetBit (0);
return (iAtom < 0 ? -1 : m.atoms[iAtom].modelIndex);
}, $fz.isPrivate = true, $fz), "J.modelset.ModelSet");
$_M(c$, "setColors", 
function (colixes, translucency) {
this.colixes = colixes;
this.colors = [colixes, Float.$valueOf (translucency)];
}, "~A,~N");
$_M(c$, "setSize", 
function (size) {
this.size = Clazz.floatToInt (size * 1000);
}, "~N");
});
