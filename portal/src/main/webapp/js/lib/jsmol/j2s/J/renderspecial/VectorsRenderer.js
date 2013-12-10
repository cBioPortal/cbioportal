Clazz.declarePackage ("J.renderspecial");
Clazz.load (["J.render.ShapeRenderer", "J.util.P3", "$.P3i", "$.V3"], "J.renderspecial.VectorsRenderer", ["J.shape.Shape", "J.util.Vibration"], function () {
c$ = Clazz.decorateAsClass (function () {
this.pointVectorEnd = null;
this.pointArrowHead = null;
this.screenVectorEnd = null;
this.screenArrowHead = null;
this.headOffsetVector = null;
this.diameter = 0;
this.headWidthPixels = 0;
this.vectorScale = 0;
this.vectorSymmetry = false;
this.headScale = 0;
this.doShaft = false;
this.vibTemp = null;
Clazz.instantialize (this, arguments);
}, J.renderspecial, "VectorsRenderer", J.render.ShapeRenderer);
Clazz.prepareFields (c$, function () {
this.pointVectorEnd =  new J.util.P3 ();
this.pointArrowHead =  new J.util.P3 ();
this.screenVectorEnd =  new J.util.P3i ();
this.screenArrowHead =  new J.util.P3i ();
this.headOffsetVector =  new J.util.V3 ();
});
Clazz.overrideMethod (c$, "render", 
function () {
var vectors = this.shape;
if (!vectors.isActive) return false;
var mads = vectors.mads;
if (mads == null) return false;
var atoms = vectors.atoms;
var colixes = vectors.colixes;
var needTranslucent = false;
this.vectorScale = this.viewer.getFloat (1649410049);
this.vectorSymmetry = this.viewer.getBoolean (603979973);
for (var i = this.modelSet.getAtomCount (); --i >= 0; ) {
var atom = atoms[i];
if (!atom.isVisible (this.myVisibilityFlag)) continue;
var vibrationVector = this.viewer.getVibration (i);
if (vibrationVector == null) continue;
if (!this.transform (mads[i], atom, vibrationVector)) continue;
if (!this.g3d.setColix (J.shape.Shape.getColix (colixes, i, atom))) {
needTranslucent = true;
continue;
}this.renderVector (atom);
if (this.vectorSymmetry) {
if (this.vibTemp == null) this.vibTemp =  new J.util.Vibration ();
this.vibTemp.setT (vibrationVector);
this.vibTemp.scale (-1);
this.transform (mads[i], atom, this.vibTemp);
this.renderVector (atom);
}}
return needTranslucent;
});
$_M(c$, "transform", 
($fz = function (mad, atom, vibrationVector) {
var len = vibrationVector.length ();
if (Math.abs (len * this.vectorScale) < 0.01) return false;
this.headScale = -0.2;
if (this.vectorScale < 0) this.headScale = -this.headScale;
this.doShaft = (0.1 + Math.abs (this.headScale / len) < Math.abs (this.vectorScale));
this.headOffsetVector.setT (vibrationVector);
this.headOffsetVector.scale (this.headScale / len);
this.pointVectorEnd.scaleAdd2 (this.vectorScale, vibrationVector, atom);
this.pointArrowHead.setT (this.pointVectorEnd);
this.pointArrowHead.add (this.headOffsetVector);
this.screenArrowHead.setT (this.viewer.transformPtVib (this.pointArrowHead, vibrationVector));
this.screenVectorEnd.setT (this.viewer.transformPtVib (this.pointVectorEnd, vibrationVector));
this.diameter = Clazz.floatToInt (mad < 1 ? 1 : mad <= 20 ? mad : this.viewer.scaleToScreen (this.screenVectorEnd.z, mad));
this.headWidthPixels = this.diameter << 1;
if (this.headWidthPixels < this.diameter + 2) this.headWidthPixels = this.diameter + 2;
return true;
}, $fz.isPrivate = true, $fz), "~N,J.modelset.Atom,J.util.Vibration");
$_M(c$, "renderVector", 
($fz = function (atom) {
if (this.doShaft) this.g3d.fillCylinderScreen (1, this.diameter, atom.screenX, atom.screenY, atom.screenZ, this.screenArrowHead.x, this.screenArrowHead.y, this.screenArrowHead.z);
this.g3d.fillConeScreen (2, this.headWidthPixels, this.screenArrowHead, this.screenVectorEnd, false);
}, $fz.isPrivate = true, $fz), "J.modelset.Atom");
Clazz.defineStatics (c$,
"arrowHeadOffset", -0.2);
});
