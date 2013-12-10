Clazz.declarePackage ("J.shape");
Clazz.load (["J.shape.AtomShape"], "J.shape.Halos", ["J.util.BSUtil", "$.C"], function () {
c$ = Clazz.decorateAsClass (function () {
this.colixSelection = 2;
this.bsHighlight = null;
this.colixHighlight = 10;
Clazz.instantialize (this, arguments);
}, J.shape, "Halos", J.shape.AtomShape);
$_M(c$, "initState", 
function () {
this.translucentAllowed = false;
});
Clazz.overrideMethod (c$, "setProperty", 
function (propertyName, value, bs) {
if ("translucency" === propertyName) return;
if ("argbSelection" === propertyName) {
this.colixSelection = J.util.C.getColix ((value).intValue ());
return;
}if ("argbHighlight" === propertyName) {
this.colixHighlight = J.util.C.getColix ((value).intValue ());
return;
}if ("highlight" === propertyName) {
this.bsHighlight = value;
return;
}if (propertyName === "deleteModelAtoms") {
J.util.BSUtil.deleteBits (this.bsHighlight, bs);
}this.setPropAS (propertyName, value, bs);
}, "~S,~O,J.util.BS");
Clazz.overrideMethod (c$, "setVisibilityFlags", 
function (bs) {
var bsSelected = (this.viewer.getSelectionHaloEnabled (false) ? this.viewer.getSelectionSet (false) : null);
for (var i = this.atomCount; --i >= 0; ) {
var isVisible = bsSelected != null && bsSelected.get (i) || (this.mads != null && this.mads[i] != 0);
this.atoms[i].setShapeVisibility (this.myVisibilityFlag, isVisible);
}
}, "J.util.BS");
Clazz.overrideMethod (c$, "getShapeState", 
function () {
return this.viewer.getShapeState (this);
});
});
