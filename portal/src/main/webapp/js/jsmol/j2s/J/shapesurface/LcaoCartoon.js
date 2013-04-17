Clazz.declarePackage ("J.shapesurface");
Clazz.load (["J.shapesurface.Isosurface"], "J.shapesurface.LcaoCartoon", ["java.lang.Float", "J.util.C", "$.Escape", "$.SB", "$.TextFormat", "$.V3"], function () {
c$ = Clazz.decorateAsClass (function () {
this.thisType = null;
this.myColorPt = 0;
this.lcaoID = null;
this.thisSet = null;
this.isMolecular = false;
this.rotationAxis = null;
this.lcaoScale = null;
this.isTranslucent = false;
this.$translucentLevel = 0;
this.lcaoColorPos = null;
this.lcaoColorNeg = null;
this.isLonePair = false;
this.isRadical = false;
this.cappingObject = null;
this.slabbingObject = null;
this.fullCommand = null;
Clazz.instantialize (this, arguments);
}, J.shapesurface, "LcaoCartoon", J.shapesurface.Isosurface);
$_M(c$, "initShape", 
function () {
Clazz.superCall (this, J.shapesurface.LcaoCartoon, "initShape", []);
this.myType = "lcaoCartoon";
this.allowMesh = false;
});
Clazz.overrideMethod (c$, "setProperty", 
function (propertyName, value, bs) {
var setInfo = false;
if ("init" === propertyName) {
this.myColorPt = 0;
this.lcaoID = null;
this.thisSet = bs;
this.isMolecular = this.isLonePair = this.isRadical = false;
this.thisType = null;
this.rotationAxis = null;
this.fullCommand = value;
this.setPropI ("init", null, null);
return;
}if ("lcaoID" === propertyName) {
this.lcaoID = value;
return;
}if ("thisID" === propertyName) {
this.lcaoID = value;
}if ("selectType" === propertyName) {
this.thisType = value;
return;
}if ("rotationAxis" === propertyName) {
this.rotationAxis = value;
return;
}if ("scale" === propertyName) {
this.lcaoScale = value;
}if ("colorRGB" === propertyName) {
this.lcaoColorPos = value;
if (this.myColorPt++ == 0) this.lcaoColorNeg = this.lcaoColorPos;
}if ("select" === propertyName) {
this.thisSet = value;
}if ("translucentLevel" === propertyName) {
this.$translucentLevel = (value).floatValue ();
}if ("settranslucency" === propertyName) {
this.isTranslucent = ((value).equals ("translucent"));
return;
}if ("translucency" === propertyName) {
this.isTranslucent = ((value).equals ("translucent"));
if (this.lcaoID == null) return;
}if ("molecular" === propertyName) {
this.isMolecular = true;
if (value == null) return;
propertyName = "create";
}if ("create" === propertyName) {
this.myColorPt = 0;
this.thisType = value;
this.createLcaoCartoon ();
return;
}if ("lonePair" === propertyName) {
this.isLonePair = true;
return;
}if ("lp" === propertyName) {
this.isLonePair = setInfo = true;
}if ("radical" === propertyName) {
this.isRadical = true;
return;
}if ("rad" === propertyName) {
this.isRadical = setInfo = true;
}if ("delete" === propertyName) {
this.deleteLcaoCartoon ();
return;
}if ("on" === propertyName) {
this.setLcaoOn (true);
return;
}if ("off" === propertyName) {
this.setLcaoOn (false);
return;
}if ("slab" === propertyName) {
this.slabbingObject = value;
}if ("cap" === propertyName) {
this.cappingObject = value;
}if ("lobe" === propertyName || "sphere" === propertyName) {
this.getCapSlabInfo (this.fullCommand);
}this.setPropI (propertyName, value, bs);
if (setInfo || "lobe" === propertyName || "sphere" === propertyName) {
this.setScriptInfo (null);
}}, "~S,~O,J.util.BS");
$_M(c$, "setLcaoOn", 
($fz = function (TF) {
if (J.util.TextFormat.isWild (this.lcaoID)) {
var key = this.lcaoID.toLowerCase ();
for (var i = this.meshCount; --i >= 0; ) {
if (J.util.TextFormat.isMatch (this.meshes[i].thisID.toLowerCase (), key, true, true)) this.meshes[i].visible = TF;
}
return;
}var atomCount = this.viewer.getAtomCount ();
for (var i = atomCount; --i >= 0; ) if (this.lcaoID != null || this.thisSet.get (i)) this.setLcaoOn (i, TF);

}, $fz.isPrivate = true, $fz), "~B");
$_M(c$, "setLcaoOn", 
($fz = function (iAtom, TF) {
var id = this.getID (this.lcaoID, iAtom);
for (var i = this.meshCount; --i >= 0; ) if (this.meshes[i].thisID.indexOf (id) == 0) this.meshes[i].visible = TF;

}, $fz.isPrivate = true, $fz), "~N,~B");
$_M(c$, "deleteLcaoCartoon", 
($fz = function () {
if (J.util.TextFormat.isWild (this.lcaoID)) {
this.deleteMeshKey (this.lcaoID);
return;
}var atomCount = this.viewer.getAtomCount ();
for (var i = atomCount; --i >= 0; ) if (this.lcaoID != null || this.thisSet.get (i)) this.deleteLcaoCartoon (i);

}, $fz.isPrivate = true, $fz));
$_M(c$, "deleteLcaoCartoon", 
($fz = function (iAtom) {
var id = this.getID (this.lcaoID, iAtom);
for (var i = this.meshCount; --i >= 0; ) if (this.meshes[i].thisID.indexOf (id) == 0) this.deleteMeshI (i);

}, $fz.isPrivate = true, $fz), "~N");
$_M(c$, "createLcaoCartoon", 
($fz = function () {
this.isMolecular = (this.isMolecular && (this.thisType.indexOf ("px") >= 0 || this.thisType.indexOf ("py") >= 0 || this.thisType.indexOf ("pz") >= 0));
var lcaoID0 = this.lcaoID;
for (var i = this.thisSet.nextSetBit (0); i >= 0; i = this.thisSet.nextSetBit (i + 1)) {
this.createLcaoCartoon (i);
this.lcaoID = lcaoID0;
}
}, $fz.isPrivate = true, $fz));
$_M(c$, "createLcaoCartoon", 
($fz = function (iAtom) {
var id = this.getID (this.lcaoID, iAtom);
var isCpk = (this.thisType.equals ("cpk"));
for (var i = this.meshCount; --i >= 0; ) if (this.meshes[i].thisID.indexOf (id) == 0) this.deleteMeshI (i);

this.setPropI ("init", null, null);
this.setPropI ("thisID", id, null);
if (this.lcaoScale != null) this.setPropI ("scale", this.lcaoScale, null);
if (isCpk) {
this.setPropI ("colorRGB", Integer.$valueOf (this.viewer.getAtomArgb (iAtom)), null);
} else if (this.lcaoColorNeg != null) {
this.setPropI ("colorRGB", this.lcaoColorNeg, null);
this.setPropI ("colorRGB", this.lcaoColorPos, null);
}if (this.slabbingObject != null) this.setPropI ("slab", this.slabbingObject, null);
if (this.cappingObject != null) this.setPropI ("cap", this.cappingObject, null);
this.setPropI ("lcaoType", this.thisType, null);
this.setPropI ("atomIndex", Integer.$valueOf (iAtom), null);
var axes = [ new J.util.V3 (),  new J.util.V3 (), J.util.V3.newV (this.modelSet.atoms[iAtom]),  new J.util.V3 ()];
if (this.rotationAxis != null) axes[3].setT (this.rotationAxis);
if (this.isMolecular) {
if (this.thisType.indexOf ("px") >= 0) {
axes[0].set (0, -1, 0);
axes[1].set (1, 0, 0);
} else if (this.thisType.indexOf ("py") >= 0) {
axes[0].set (-1, 0, 0);
axes[1].set (0, 0, 1);
} else if (this.thisType.indexOf ("pz") >= 0) {
axes[0].set (0, 0, 1);
axes[1].set (1, 0, 0);
}if (this.thisType.indexOf ("-") == 0) axes[0].scale (-1);
}if (this.isMolecular || isCpk || this.thisType.equalsIgnoreCase ("s") || this.viewer.getHybridizationAndAxes (iAtom, axes[0], axes[1], this.thisType) != null) {
this.setPropI ((this.isRadical ? "radical" : this.isLonePair ? "lonePair" : "lcaoCartoon"), axes, null);
}if (isCpk) {
var colix = this.viewer.getModelSet ().getAtomColix (iAtom);
if (J.util.C.isColixTranslucent (colix)) {
this.setPropI ("translucentLevel", Float.$valueOf (J.util.C.getColixTranslucencyLevel (colix)), null);
this.setPropI ("translucency", "translucent", null);
}} else if (this.isTranslucent) for (var i = this.meshCount; --i >= 0; ) if (this.meshes[i].thisID.indexOf (id) == 0) this.meshes[i].setTranslucent (true, this.$translucentLevel);

}, $fz.isPrivate = true, $fz), "~N");
$_M(c$, "getID", 
($fz = function (id, i) {
return (id != null ? id : (this.isLonePair || this.isRadical ? "lp_" : "lcao_") + (i + 1) + "_") + (this.thisType == null ? "" : J.util.TextFormat.simpleReplace (this.thisType, "-", (this.thisType.indexOf ("-p") == 0 ? "" : "_")));
}, $fz.isPrivate = true, $fz), "~S,~N");
$_M(c$, "getShapeState", 
function () {
var sb =  new J.util.SB ();
if (this.lcaoScale != null) J.shape.Shape.appendCmd (sb, "lcaoCartoon scale " + this.lcaoScale.floatValue ());
if (this.lcaoColorNeg != null) J.shape.Shape.appendCmd (sb, "lcaoCartoon color " + J.util.Escape.escapeColor (this.lcaoColorNeg.intValue ()) + " " + J.util.Escape.escapeColor (this.lcaoColorPos.intValue ()));
if (this.isTranslucent) J.shape.Shape.appendCmd (sb, "lcaoCartoon translucent " + this.$translucentLevel);
for (var i = this.meshCount; --i >= 0; ) if (!this.meshes[i].visible) J.shape.Shape.appendCmd (sb, "lcaoCartoon ID " + this.meshes[i].thisID + " off");

return Clazz.superCall (this, J.shapesurface.LcaoCartoon, "getShapeState", []) + sb.toString ();
});
$_M(c$, "merge", 
function (shape) {
var lc = shape;
this.lcaoScale = lc.lcaoScale;
this.lcaoColorNeg = lc.lcaoColorNeg;
this.isTranslucent = lc.isTranslucent;
Clazz.superCall (this, J.shapesurface.LcaoCartoon, "merge", [shape]);
}, "J.shape.Shape");
});
