Clazz.declarePackage ("J.renderspecial");
Clazz.load (["J.render.ShapeRenderer", "JU.P3", "$.P3i", "$.V3"], "J.renderspecial.VectorsRenderer", ["J.shape.Shape", "JU.Vibration"], function () {
c$ = Clazz.decorateAsClass (function () {
this.pointVectorStart = null;
this.pointVectorEnd = null;
this.pointArrowHead = null;
this.screenVectorStart = null;
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
this.vectorsCentered = false;
this.standardVector = true;
this.vibrationOn = false;
this.drawCap = false;
Clazz.instantialize (this, arguments);
}, J.renderspecial, "VectorsRenderer", J.render.ShapeRenderer);
Clazz.prepareFields (c$, function () {
this.pointVectorStart =  new JU.P3 ();
this.pointVectorEnd =  new JU.P3 ();
this.pointArrowHead =  new JU.P3 ();
this.screenVectorStart =  new JU.P3i ();
this.screenVectorEnd =  new JU.P3i ();
this.screenArrowHead =  new JU.P3i ();
this.headOffsetVector =  new JU.V3 ();
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
this.vectorScale = this.vwr.getFloat (1649410049);
this.vectorSymmetry = this.vwr.getBoolean (603979973);
this.vectorsCentered = this.vwr.getBoolean (603979972);
this.vibrationOn = this.vwr.tm.vibrationOn;
this.headScale = -0.2;
if (this.vectorScale < 0) this.headScale = -this.headScale;
for (var i = this.ms.getAtomCount (); --i >= 0; ) {
var atom = atoms[i];
if (!this.isVisibleForMe (atom)) continue;
var vibrationVector = this.ms.getVibration (i, false);
if (vibrationVector == null) continue;
if (!this.transform (mads[i], atom, vibrationVector)) continue;
if (!this.g3d.setC (J.shape.Shape.getColix (colixes, i, atom))) {
needTranslucent = true;
continue;
}this.renderVector (atom);
if (this.vectorSymmetry) {
if (this.vibTemp == null) this.vibTemp =  new JU.Vibration ();
this.vibTemp.setT (vibrationVector);
this.vibTemp.scale (-1);
this.transform (mads[i], atom, this.vibTemp);
this.renderVector (atom);
}}
return needTranslucent;
});
Clazz.defineMethod (c$, "transform", 
 function (mad, atom, vib) {
var isMod = (vib.modDim >= 0);
var isSpin = (vib.modDim == -2);
this.drawCap = true;
if (!isMod) {
var len = vib.length ();
if (Math.abs (len * this.vectorScale) < 0.01) return false;
this.standardVector = true;
this.doShaft = (0.1 + Math.abs (this.headScale / len) < Math.abs (this.vectorScale));
this.headOffsetVector.setT (vib);
this.headOffsetVector.scale (this.headScale / len);
}if (isMod) {
this.standardVector = false;
this.doShaft = true;
this.pointVectorStart.setT (atom);
this.pointVectorEnd.setT (atom);
var mod = vib;
if (!mod.isEnabled ()) {
mod.addTo (this.pointVectorEnd, 1);
} else {
if (this.vibrationOn) this.vwr.tm.getVibrationPoint (vib, this.pointVectorEnd, NaN);
mod.addTo (this.pointVectorStart, -1);
}this.headOffsetVector.sub2 (this.pointVectorEnd, this.pointVectorStart);
var len = this.headOffsetVector.length ();
this.drawCap = (len + -0.2 > 0.001);
this.doShaft = (len > 0.01);
this.headOffsetVector.scale (this.headScale / this.headOffsetVector.length ());
} else if (this.vectorsCentered || isSpin) {
this.standardVector = false;
this.pointVectorEnd.scaleAdd2 (0.5 * this.vectorScale, vib, atom);
this.pointVectorStart.scaleAdd2 (-0.5 * this.vectorScale, vib, atom);
} else {
this.pointVectorEnd.scaleAdd2 (this.vectorScale, vib, atom);
this.screenVectorEnd.setT (this.vibrationOn ? this.tm.transformPtVib (this.pointVectorEnd, vib) : this.tm.transformPt (this.pointVectorEnd));
this.pointArrowHead.add2 (this.pointVectorEnd, this.headOffsetVector);
if (atom.getAtomNumber () == 16) System.out.println ("vecrend " + vib + atom.x + " " + atom.y + " " + atom.z + " ptH=" + this.pointVectorEnd);
this.screenArrowHead.setT (this.vibrationOn ? this.tm.transformPtVib (this.pointArrowHead, vib) : this.tm.transformPt (this.pointArrowHead));
}if (!this.standardVector) {
this.screenVectorEnd.setT (this.tm.transformPt (this.pointVectorEnd));
this.screenVectorStart.setT (this.tm.transformPt (this.pointVectorStart));
if (this.drawCap) this.pointArrowHead.add2 (this.pointVectorEnd, this.headOffsetVector);
 else this.pointArrowHead.setT (this.pointVectorEnd);
this.screenArrowHead.setT (this.tm.transformPt (this.pointArrowHead));
}this.diameter = Clazz.floatToInt (mad < 0 ? -mad : mad < 1 ? 1 : this.vwr.tm.scaleToScreen (this.screenVectorEnd.z, mad));
this.headWidthPixels = this.diameter << 1;
if (this.headWidthPixels < this.diameter + 2) this.headWidthPixels = this.diameter + 2;
return true;
}, "~N,JM.Atom,JU.Vibration");
Clazz.defineMethod (c$, "renderVector", 
 function (atom) {
if (this.doShaft) {
if (this.standardVector) this.g3d.fillCylinderScreen (1, this.diameter, atom.sX, atom.sY, atom.sZ, this.screenArrowHead.x, this.screenArrowHead.y, this.screenArrowHead.z);
 else this.g3d.fillCylinderScreen (2, this.diameter, this.screenVectorStart.x, this.screenVectorStart.y, this.screenVectorStart.z, this.screenArrowHead.x, this.screenArrowHead.y, this.screenArrowHead.z);
}if (this.drawCap) this.g3d.fillConeScreen (2, this.headWidthPixels, this.screenArrowHead, this.screenVectorEnd, false);
}, "JM.Atom");
Clazz.defineStatics (c$,
"arrowHeadOffset", -0.2);
});
