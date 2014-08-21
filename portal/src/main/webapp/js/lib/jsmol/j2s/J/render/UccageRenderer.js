Clazz.declarePackage ("J.render");
Clazz.load (["J.render.CageRenderer", "JU.P3"], "J.render.UccageRenderer", ["JU.DF", "$.PT", "JU.BoxInfo", "$.C", "$.SimpleUnitCell"], function () {
c$ = Clazz.decorateAsClass (function () {
this.fid = 0;
this.verticesT = null;
this.fset0 = null;
this.cell0 = null;
this.cell1 = null;
this.offset = null;
this.offsetT = null;
Clazz.instantialize (this, arguments);
}, J.render, "UccageRenderer", J.render.CageRenderer);
Clazz.prepareFields (c$, function () {
this.verticesT =  new Array (8);
this.fset0 = JU.P3.new3 (555, 555, 1);
this.cell0 =  new JU.P3 ();
this.cell1 =  new JU.P3 ();
this.offset =  new JU.P3 ();
this.offsetT =  new JU.P3 ();
});
Clazz.overrideMethod (c$, "initRenderer", 
function () {
for (var i = 8; --i >= 0; ) this.verticesT[i] =  new JU.P3 ();

this.tickEdges = JU.BoxInfo.uccageTickEdges;
this.draw000 = false;
});
Clazz.overrideMethod (c$, "render", 
function () {
this.imageFontScaling = this.vwr.getImageFontScaling ();
this.font3d = this.g3d.getFont3DScaled ((this.shape).font3d, this.imageFontScaling);
var mad = this.vwr.getObjectMad (5);
if (mad == 0 || this.vwr.isJmolDataFrame () || this.tm.isNavigating () && this.vwr.getBoolean (603979888)) return false;
this.colix = this.vwr.getObjectColix (5);
var needTranslucent = JU.C.isColixTranslucent (this.colix);
if (!this.isExport && needTranslucent != this.g3d.isPass2 ()) return needTranslucent;
this.render1 (mad);
return false;
});
Clazz.defineMethod (c$, "render1", 
 function (mad) {
this.g3d.setC (this.colix);
var unitcell = this.vwr.getCurrentUnitCell ();
if (unitcell == null) return;
this.isPolymer = unitcell.isPolymer ();
this.isSlab = unitcell.isSlab ();
var vertices = unitcell.getUnitCellVertices ();
this.offset.setT (unitcell.getCartesianOffset ());
this.offsetT.set (0, 0, 0);
unitcell.toCartesian (this.offsetT, true);
this.offset.sub (this.offsetT);
var fset = unitcell.getUnitCellMultiplier ();
var haveMultiple = (fset != null);
if (!haveMultiple) fset = this.fset0;
JU.SimpleUnitCell.ijkToPoint3f (Clazz.floatToInt (fset.x), this.cell0, 0);
JU.SimpleUnitCell.ijkToPoint3f (Clazz.floatToInt (fset.y), this.cell1, 1);
var firstLine;
var allow0;
var allow1;
if (fset.z < 0) {
this.cell0.scale (-1 / fset.z);
this.cell1.scale (-1 / fset.z);
}var scale = Math.abs (fset.z);
var axisPoints = this.vwr.getAxisPoints ();
var drawAllLines = (this.vwr.getObjectMad (1) == 0 || this.vwr.getFloat (570425346) < 2 || axisPoints == null);
var aPoints = axisPoints;
if (fset.z == 0) {
this.offsetT.setT (this.cell0);
unitcell.toCartesian (this.offsetT, true);
this.offsetT.add (this.offset);
aPoints = (this.cell0.x == 0 && this.cell0.y == 0 && this.cell0.z == 0 ? axisPoints : null);
firstLine = 0;
allow0 = 0xFF;
allow1 = 0xFF;
var pts = JU.BoxInfo.unitCubePoints;
for (var i = 8; --i >= 0; ) {
var v = JU.P3.new3 (pts[i].x * (this.cell1.x - this.cell0.x), pts[i].y * (this.cell1.y - this.cell0.y), pts[i].z * (this.cell1.z - this.cell0.z));
unitcell.toCartesian (v, true);
this.verticesT[i].add2 (v, this.offsetT);
}
this.renderCage (mad, this.verticesT, aPoints, firstLine, allow0, allow1, 1);
} else for (var x = Clazz.floatToInt (this.cell0.x); x < this.cell1.x; x++) {
for (var y = Clazz.floatToInt (this.cell0.y); y < this.cell1.y; y++) {
for (var z = Clazz.floatToInt (this.cell0.z); z < this.cell1.z; z++) {
if (haveMultiple) {
this.offsetT.set (x, y, z);
this.offsetT.scale (scale);
unitcell.toCartesian (this.offsetT, true);
this.offsetT.add (this.offset);
aPoints = (x == 0 && y == 0 && z == 0 ? axisPoints : null);
firstLine = (drawAllLines || aPoints == null ? 0 : 3);
} else {
this.offsetT.setT (this.offset);
firstLine = (drawAllLines ? 0 : 3);
}allow0 = 0xFF;
allow1 = 0xFF;
for (var i = 8; --i >= 0; ) this.verticesT[i].add2 (vertices[i], this.offsetT);

this.renderCage (mad, this.verticesT, aPoints, firstLine, allow0, allow1, scale);
}
}
}
if (this.vwr.getBoolean (603979828) && !this.vwr.isPreviewOnly () && !unitcell.isPeriodic ()) this.renderInfo (unitcell);
}, "~N");
Clazz.defineMethod (c$, "nfformat", 
 function (x) {
return (JU.DF.formatDecimal (x, 3));
}, "~N");
Clazz.defineMethod (c$, "renderInfo", 
 function (symmetry) {
if (this.isExport || !this.g3d.setC (this.vwr.getColixBackgroundContrast ()) || !this.vwr.getBoolean (603979938)) return;
this.fid = this.g3d.getFontFidFS ("Monospaced", 14 * this.imageFontScaling);
this.g3d.setFontFid (this.fid);
var lineheight = Clazz.doubleToInt (Math.floor (15 * this.imageFontScaling));
var x = Clazz.doubleToInt (Math.floor (5 * this.imageFontScaling));
var y = lineheight;
var sgName = symmetry.getSpaceGroupName ();
if (this.isPolymer) sgName = "polymer";
 else if (this.isSlab) sgName = "slab";
 else if (sgName != null && sgName.startsWith ("cell=!")) sgName = "cell=inverse[" + sgName.substring (6) + "]";
sgName = JU.PT.rep (sgName, ";0,0,0", "");
if ( new Boolean (sgName != null & !sgName.equals ("-- [--]")).valueOf ()) {
y += lineheight;
this.g3d.drawStringNoSlab (sgName, null, x, y, 0, 0);
}var info = symmetry.getMoreInfo ();
if (info != null) for (var i = 0; i < info.size (); i++) {
y += lineheight;
this.g3d.drawStringNoSlab (info.get (i), null, x, y, 0, 0);
}
if (!this.vwr.getBoolean (603979937)) return;
y += lineheight;
this.g3d.drawStringNoSlab ("a=" + this.nfformat (symmetry.getUnitCellInfoType (0)) + "\u00C5", null, x, y, 0, 0);
if (!this.isPolymer) {
y += lineheight;
this.g3d.drawStringNoSlab ("b=" + this.nfformat (symmetry.getUnitCellInfoType (1)) + "\u00C5", null, x, y, 0, 0);
}if (!this.isPolymer && !this.isSlab) {
y += lineheight;
this.g3d.drawStringNoSlab ("c=" + this.nfformat (symmetry.getUnitCellInfoType (2)) + "\u00C5", null, x, y, 0, 0);
}if (!this.isPolymer) {
if (!this.isSlab) {
y += lineheight;
this.g3d.drawStringNoSlab ("\u03B1=" + this.nfformat (symmetry.getUnitCellInfoType (3)) + "\u00B0", null, x, y, 0, 0);
y += lineheight;
this.g3d.drawStringNoSlab ("\u03B2=" + this.nfformat (symmetry.getUnitCellInfoType (4)) + "\u00B0", null, x, y, 0, 0);
}y += lineheight;
this.g3d.drawStringNoSlab ("\u03B3=" + this.nfformat (symmetry.getUnitCellInfoType (5)) + "\u00B0", null, x, y, 0, 0);
}}, "J.api.SymmetryInterface");
});
