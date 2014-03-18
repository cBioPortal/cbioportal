Clazz.declarePackage ("J.shape");
Clazz.load (["J.shape.TextShape"], "J.shape.Hover", ["J.modelset.Text", "J.util.ArrayUtil", "$.C"], function () {
c$ = Clazz.decorateAsClass (function () {
this.hoverText = null;
this.atomIndex = -1;
this.xy = null;
this.text = null;
this.labelFormat = "%U";
this.atomFormats = null;
this.specialLabel = null;
Clazz.instantialize (this, arguments);
}, J.shape, "Hover", J.shape.TextShape);
$_M(c$, "initShape", 
function () {
Clazz.superCall (this, J.shape.Hover, "initShape", []);
this.isHover = true;
var font3d = this.gdata.getFont3DFSS ("SansSerif", "Plain", 12);
var bgcolix = J.util.C.getColixS ("#FFFFC3");
var colix = 4;
this.currentObject = this.hoverText = J.modelset.Text.newLabel (this.gdata, font3d, null, colix, bgcolix, 1, 0, null);
this.hoverText.setAdjustForWindow (true);
});
Clazz.overrideMethod (c$, "setProperty", 
function (propertyName, value, bsSelected) {
if ("target" === propertyName) {
if (value == null) this.atomIndex = -1;
 else {
this.atomIndex = (value).intValue ();
}return;
}if ("text" === propertyName) {
this.text = value;
if (this.text != null && this.text.length == 0) this.text = null;
return;
}if ("specialLabel" === propertyName) {
this.specialLabel = value;
return;
}if ("atomLabel" === propertyName) {
var text = value;
if (text != null && text.length == 0) text = null;
var count = this.viewer.getAtomCount ();
if (this.atomFormats == null || this.atomFormats.length < count) this.atomFormats =  new Array (count);
for (var i = bsSelected.nextSetBit (0); i >= 0; i = bsSelected.nextSetBit (i + 1)) this.atomFormats[i] = text;

return;
}if ("xy" === propertyName) {
this.xy = value;
return;
}if ("label" === propertyName) {
this.labelFormat = value;
if (this.labelFormat != null && this.labelFormat.length == 0) this.labelFormat = null;
return;
}if (propertyName === "deleteModelAtoms") {
if (this.atomFormats != null) {
var firstAtomDeleted = ((value)[2])[1];
var nAtomsDeleted = ((value)[2])[2];
this.atomFormats = J.util.ArrayUtil.deleteElements (this.atomFormats, firstAtomDeleted, nAtomsDeleted);
}this.atomIndex = -1;
return;
}this.setPropTS (propertyName, value, null);
}, "~S,~O,J.util.BS");
Clazz.overrideMethod (c$, "getShapeState", 
function () {
return this.viewer.getShapeState (this);
});
Clazz.defineStatics (c$,
"FONTFACE", "SansSerif",
"FONTSTYLE", "Plain",
"FONTSIZE", 12);
});
