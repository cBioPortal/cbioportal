Clazz.declarePackage ("J.render");
Clazz.load (["J.render.LabelsRenderer"], "J.render.HoverRenderer", ["J.modelset.LabelToken", "J.render.TextRenderer"], function () {
c$ = Clazz.declareType (J.render, "HoverRenderer", J.render.LabelsRenderer);
Clazz.overrideMethod (c$, "render", 
function () {
if (this.viewer.isNavigating ()) return false;
var hover = this.shape;
var antialias = this.g3d.isAntialiased ();
var text = hover.hoverText;
if (hover.atomIndex >= 0) {
var atom = this.modelSet.atoms[hover.atomIndex];
var label = (hover.specialLabel != null ? hover.specialLabel : hover.atomFormats != null && hover.atomFormats[hover.atomIndex] != null ? J.modelset.LabelToken.formatLabel (this.viewer, atom, hover.atomFormats[hover.atomIndex]) : hover.labelFormat != null ? J.modelset.LabelToken.formatLabel (this.viewer, atom, this.fixLabel (atom, hover.labelFormat)) : null);
if (label == null) return false;
text.setText (label);
text.setXYZs (atom.screenX, atom.screenY, 1, -2147483648);
} else if (hover.text != null) {
text.setText (hover.text);
text.setXYZs (hover.xy.x, hover.xy.y, 1, -2147483648);
} else {
return true;
}J.render.TextRenderer.render (text, this.viewer, this.g3d, 0, antialias ? 2 : 1, false, null, this.xy);
return true;
});
$_M(c$, "fixLabel", 
function (atom, label) {
if (label == null) return null;
return (this.viewer.isJmolDataFrameForModel (atom.getModelIndex ()) && label.equals ("%U") ? "%W" : label);
}, "J.modelset.Atom,~S");
});
