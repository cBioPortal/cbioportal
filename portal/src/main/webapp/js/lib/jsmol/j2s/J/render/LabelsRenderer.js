Clazz.declarePackage ("J.render");
Clazz.load (["J.render.FontLineShapeRenderer", "JU.P3", "$.P3i"], "J.render.LabelsRenderer", ["JM.Text", "J.render.TextRenderer", "J.shape.Labels", "JV.JC"], function () {
c$ = Clazz.decorateAsClass (function () {
this.minZ = null;
this.ascent = 0;
this.descent = 0;
this.sppm = 0;
this.xy = null;
this.screen = null;
this.fidPrevious = 0;
this.zCutoff = 0;
this.pTemp = null;
this.bgcolix = 0;
this.labelColix = 0;
this.fid = 0;
this.atom = null;
this.atomPt = null;
this.isExact = false;
this.offset = 0;
this.textAlign = 0;
this.pointer = 0;
this.zSlab = -2147483648;
this.zBox = 0;
this.boxXY = null;
this.scalePixelsPerMicron = 0;
Clazz.instantialize (this, arguments);
}, J.render, "LabelsRenderer", J.render.FontLineShapeRenderer);
Clazz.prepareFields (c$, function () {
this.minZ =  Clazz.newIntArray (1, 0);
this.xy =  Clazz.newFloatArray (3, 0);
this.screen =  new JU.P3i ();
this.pTemp =  new JU.P3 ();
});
Clazz.overrideMethod (c$, "render", 
function () {
this.fidPrevious = 0;
this.zCutoff = this.tm.getZShadeStart ();
var labels = this.shape;
var labelStrings = labels.strings;
var bgcolixes = labels.bgcolixes;
if (this.isExport) bgcolixes = this.g3d.getBgColixes (bgcolixes);
var fids = labels.fids;
var offsets = labels.offsets;
if (labelStrings == null) return false;
var atoms = this.ms.at;
var backgroundColixContrast = this.vwr.getColixBackgroundContrast ();
var backgroundColor = this.vwr.getBackgroundArgb ();
this.sppm = this.vwr.getScalePixelsPerAngstrom (true);
this.scalePixelsPerMicron = (this.vwr.getBoolean (603979845) ? this.sppm * 10000 : 0);
this.imageFontScaling = this.vwr.getImageFontScaling ();
var iGroup = -1;
this.minZ[0] = 2147483647;
var isAntialiased = this.g3d.isAntialiased ();
for (var i = labelStrings.length; --i >= 0; ) {
this.atomPt = this.atom = atoms[i];
if (!this.isVisibleForMe (this.atom)) continue;
var label = labelStrings[i];
if (label == null || label.length == 0 || labels.mads != null && labels.mads[i] < 0) continue;
this.labelColix = labels.getColix2 (i, this.atom, false);
this.bgcolix = labels.getColix2 (i, this.atom, true);
if (this.bgcolix == 0 && this.g3d.getColorArgbOrGray (this.labelColix) == backgroundColor) this.labelColix = backgroundColixContrast;
this.fid = ((fids == null || i >= fids.length || fids[i] == 0) ? labels.zeroFontId : fids[i]);
var offsetFull = (offsets == null || i >= offsets.length ? 0 : offsets[i]);
var labelsFront = ((offsetFull & 32) != 0);
var labelsGroup = ((offsetFull & 16) != 0);
this.isExact = ((offsetFull & 128) != 0);
this.offset = offsetFull >> 8;
this.textAlign = J.shape.Labels.getAlignment (offsetFull);
this.pointer = offsetFull & 3;
this.zSlab = this.atom.sZ - Clazz.doubleToInt (this.atom.sD / 2) - 3;
if (this.zCutoff > 0 && this.zSlab > this.zCutoff) continue;
if (this.zSlab < 1) this.zSlab = 1;
this.zBox = this.zSlab;
if (labelsGroup) {
var group = this.atom.getGroup ();
var ig = group.getGroupIndex ();
if (ig != iGroup) {
group.getMinZ (atoms, this.minZ);
iGroup = ig;
}this.zBox = this.minZ[0];
} else if (labelsFront) {
this.zBox = 1;
}if (this.zBox < 1) this.zBox = 1;
var text = labels.getLabel (i);
this.boxXY = (!this.isExport || this.vwr.creatingImage ? labels.getBox (i) :  Clazz.newFloatArray (5, 0));
if (this.boxXY == null) labels.putBox (i, this.boxXY =  Clazz.newFloatArray (5, 0));
text = this.renderLabelOrMeasure (text, label);
if (text != null) labels.putLabel (i, text);
if (isAntialiased) {
this.boxXY[0] /= 2;
this.boxXY[1] /= 2;
}this.boxXY[4] = this.zBox;
}
return false;
});
Clazz.defineMethod (c$, "renderLabelOrMeasure", 
function (text, label) {
var newText = false;
if (text != null) {
if (text.font == null) text.setFontFromFid (this.fid);
text.atomX = this.atomPt.sX;
text.atomY = this.atomPt.sY;
text.atomZ = this.zSlab;
if (text.pymolOffset == null) {
text.setXYZs (this.atomPt.sX, this.atomPt.sY, this.zBox, this.zSlab);
text.setColix (this.labelColix);
text.setBgColix (this.bgcolix);
} else {
if (text.pymolOffset[0] == 1) this.pTemp.setT (this.atomPt);
 else this.pTemp.set (0, 0, 0);
this.pTemp.add3 (text.pymolOffset[4], text.pymolOffset[5], text.pymolOffset[6]);
this.tm.transformPtScr (this.pTemp, this.screen);
text.setXYZs (this.screen.x, this.screen.y, this.screen.z, this.zSlab);
text.setScalePixelsPerMicron (this.sppm);
}} else {
var isLeft = (this.textAlign == 1 || this.textAlign == 0);
if (this.fid != this.fidPrevious || this.ascent == 0) {
this.g3d.setFontFid (this.fid);
this.fidPrevious = this.fid;
this.font3d = this.g3d.getFont3DCurrent ();
if (isLeft) {
this.ascent = this.font3d.getAscent ();
this.descent = this.font3d.getDescent ();
}}var isSimple = isLeft && (this.imageFontScaling == 1 && this.scalePixelsPerMicron == 0 && label.indexOf ("|") < 0 && label.indexOf ("<su") < 0);
if (isSimple) {
var doPointer = ((this.pointer & 1) != 0);
var pointerColix = ((this.pointer & 2) != 0 && this.bgcolix != 0 ? this.bgcolix : this.labelColix);
this.boxXY[0] = this.atomPt.sX;
this.boxXY[1] = this.atomPt.sY;
J.render.TextRenderer.renderSimpleLabel (this.g3d, this.font3d, label, this.labelColix, this.bgcolix, this.boxXY, this.zBox, this.zSlab, JV.JC.getXOffset (this.offset), JV.JC.getYOffset (this.offset), this.ascent, this.descent, doPointer, pointerColix, this.isExact);
this.atomPt = null;
} else {
text = JM.Text.newLabel (this.g3d.getGData (), this.font3d, label, this.labelColix, this.bgcolix, this.textAlign, 0, null);
text.atomX = this.atomPt.sX;
text.atomY = this.atomPt.sY;
text.atomZ = this.zSlab;
text.setXYZs (this.atomPt.sX, this.atomPt.sY, this.zBox, this.zSlab);
newText = true;
}}if (this.atomPt != null) {
if (text.pymolOffset == null) {
text.setOffset (this.offset);
if (this.textAlign != 0) text.setAlignment (this.textAlign);
}text.setPointer (this.pointer);
J.render.TextRenderer.render (text, this.vwr, this.g3d, this.scalePixelsPerMicron, this.imageFontScaling, this.isExact, this.boxXY, this.xy);
}return (newText ? text : null);
}, "JM.Text,~S");
});
