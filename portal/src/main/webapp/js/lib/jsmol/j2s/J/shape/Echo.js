Clazz.declarePackage ("J.shape");
Clazz.load (["J.shape.TextShape"], "J.shape.Echo", ["J.modelset.Object2d", "$.Text", "J.util.TextFormat"], function () {
c$ = Clazz.declareType (J.shape, "Echo", J.shape.TextShape);
$_M(c$, "initShape", 
function () {
Clazz.superCall (this, J.shape.Echo, "initShape", []);
this.setProperty ("target", "top", null);
});
Clazz.overrideMethod (c$, "setProperty", 
function (propertyName, value, bs) {
if ("scalereference" === propertyName) {
if (this.currentObject != null) {
var val = (value).floatValue ();
this.currentObject.setScalePixelsPerMicron (val == 0 ? 0 : 10000 / val);
}return;
}if ("point" === propertyName) {
if (this.currentObject == null) return;
var t = this.currentObject;
t.pointerPt = (value == null ? null : value);
t.pointer = (value == null ? 0 : 1);
return;
}if ("xyz" === propertyName) {
if (this.currentObject != null && this.viewer.getBoolean (603979845)) this.currentObject.setScalePixelsPerMicron (this.viewer.getScalePixelsPerAngstrom (false) * 10000);
}if ("scale" === propertyName) {
if (this.currentObject == null) {
if (this.isAll) for (var t, $t = this.objects.values ().iterator (); $t.hasNext () && ((t = $t.next ()) || true);) t.setScale ((value).floatValue ());

return;
}(this.currentObject).setScale ((value).floatValue ());
return;
}if ("image" === propertyName) {
if (this.currentObject == null) {
if (this.isAll) for (var t, $t = this.objects.values ().iterator (); $t.hasNext () && ((t = $t.next ()) || true);) t.setImage (value);

return;
}(this.currentObject).setImage (value);
return;
}if ("thisID" === propertyName) {
var target = value;
this.currentObject = this.objects.get (target);
if (this.currentObject == null && J.util.TextFormat.isWild (target)) this.thisID = target.toUpperCase ();
return;
}if ("hidden" === propertyName) {
var isHidden = (value).booleanValue ();
if (this.currentObject == null) {
if (this.isAll || this.thisID != null) for (var t, $t = this.objects.values ().iterator (); $t.hasNext () && ((t = $t.next ()) || true);) if (this.isAll || J.util.TextFormat.isMatch (t.target.toUpperCase (), this.thisID, true, true)) t.hidden = isHidden;

return;
}(this.currentObject).hidden = isHidden;
return;
}if (J.modelset.Object2d.setProperty (propertyName, value, this.currentObject)) return;
if ("target" === propertyName) {
this.thisID = null;
var target = (value).intern ().toLowerCase ();
if (target === "none" || target === "all") {
} else {
this.isAll = false;
var text = this.objects.get (target);
if (text == null) {
var valign = 0;
var halign = 1;
if ("top" === target) {
valign = 1;
halign = 2;
} else if ("middle" === target) {
valign = 3;
halign = 2;
} else if ("bottom" === target) {
valign = 2;
}text = J.modelset.Text.newEcho (this.viewer, this.gdata, this.gdata.getFont3DFS ("Serif", 20), target, 10, valign, halign, 0);
text.setAdjustForWindow (true);
this.objects.put (target, text);
if (this.currentFont != null) text.setFont (this.currentFont, true);
if (this.currentColor != null) text.setColixO (this.currentColor);
if (this.currentBgColor != null) text.setBgColixO (this.currentBgColor);
if (this.currentTranslucentLevel != 0) text.setTranslucent (this.currentTranslucentLevel, false);
if (this.currentBgTranslucentLevel != 0) text.setTranslucent (this.currentBgTranslucentLevel, true);
}this.currentObject = text;
return;
}}this.setPropTS (propertyName, value, null);
}, "~S,~O,J.util.BS");
Clazz.overrideMethod (c$, "getPropertyData", 
function (property, data) {
if ("currentTarget" === property) {
return (this.currentObject != null && (data[0] = this.currentObject.target) != null);
}if (property === "checkID") {
var key = (data[0]).toUpperCase ();
var isWild = J.util.TextFormat.isWild (key);
for (var t, $t = this.objects.values ().iterator (); $t.hasNext () && ((t = $t.next ()) || true);) {
var id = t.target;
if (id.equalsIgnoreCase (key) || isWild && J.util.TextFormat.isMatch (id.toUpperCase (), key, true, true)) {
data[1] = id;
return true;
}}
return false;
}return false;
}, "~S,~A");
Clazz.overrideMethod (c$, "getShapeState", 
function () {
return this.viewer.getShapeState (this);
});
Clazz.defineStatics (c$,
"FONTFACE", "Serif",
"FONTSIZE", 20,
"COLOR", 10);
});
