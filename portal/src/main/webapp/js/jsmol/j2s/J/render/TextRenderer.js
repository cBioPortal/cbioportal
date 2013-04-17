Clazz.declarePackage ("J.render");
Clazz.load (null, "J.render.TextRenderer", ["J.shape.Text"], function () {
c$ = Clazz.declareType (J.render, "TextRenderer");
c$.render = $_M(c$, "render", 
function (text, g3d, scalePixelsPerMicron, imageFontScaling, isExact, boxXY) {
if (text == null || text.image == null && text.lines == null) return;
text.setPosition (g3d.getRenderWidth (), g3d.getRenderHeight (), scalePixelsPerMicron, imageFontScaling, isExact, boxXY);
if (text.image == null && text.bgcolix != 0) {
if (g3d.setColix (text.bgcolix)) J.render.TextRenderer.showBox (g3d, text.colix, Clazz.floatToInt (text.boxX), Clazz.floatToInt (text.boxY), text.z + 2, text.zSlab, Clazz.floatToInt (text.boxWidth), Clazz.floatToInt (text.boxHeight), text.fontScale, text.isLabelOrHover);
}if (g3d.setColix (text.colix)) {
if (text.image == null) {
var xy =  Clazz.newFloatArray (3, 0);
for (var i = 0; i < text.lines.length; i++) {
text.setXYA (xy, i);
g3d.drawString (text.lines[i], text.font, Clazz.floatToInt (xy[0]), Clazz.floatToInt (xy[1]), text.z, text.zSlab, text.bgcolix);
}
} else {
g3d.drawImage (text.image, Clazz.floatToInt (text.boxX), Clazz.floatToInt (text.boxY), text.z, text.zSlab, text.bgcolix, Clazz.floatToInt (text.boxWidth), Clazz.floatToInt (text.boxHeight));
}J.render.TextRenderer.drawPointer (text, g3d);
}return;
}, "J.shape.Text,J.api.JmolRendererInterface,~N,~N,~B,~A");
c$.drawPointer = $_M(c$, "drawPointer", 
function (text, g3d) {
if ((text.pointer & 1) != 0) {
if (!g3d.setColix ((text.pointer & 2) != 0 && text.bgcolix != 0 ? text.bgcolix : text.colix)) return;
if (text.boxX > text.movableX) g3d.drawLineXYZ (text.movableX, text.movableY, text.zSlab, Clazz.floatToInt (text.boxX), Clazz.floatToInt (text.boxY + text.boxHeight / 2), text.zSlab);
 else if (text.boxX + text.boxWidth < text.movableX) g3d.drawLineXYZ (text.movableX, text.movableY, text.zSlab, Clazz.floatToInt (text.boxX + text.boxWidth), Clazz.floatToInt (text.boxY + text.boxHeight / 2), text.zSlab);
}}, "J.shape.Text,J.api.JmolRendererInterface");
c$.showBox = $_M(c$, "showBox", 
($fz = function (g3d, colix, x, y, z, zSlab, boxWidth, boxHeight, imageFontScaling, atomBased) {
g3d.fillRect (x, y, z, zSlab, boxWidth, boxHeight);
g3d.setColix (colix);
if (!atomBased) return;
if (imageFontScaling >= 2) {
g3d.drawRect (x + 3, y + 3, z - 1, zSlab, boxWidth - 6, boxHeight - 6);
} else {
g3d.drawRect (x + 1, y + 1, z - 1, zSlab, boxWidth - 2, boxHeight - 2);
}}, $fz.isPrivate = true, $fz), "J.api.JmolRendererInterface,~N,~N,~N,~N,~N,~N,~N,~N,~B");
c$.renderSimpleLabel = $_M(c$, "renderSimpleLabel", 
function (g3d, font, strLabel, colix, bgcolix, boxXY, z, zSlab, xOffset, yOffset, ascent, descent, doPointer, pointerColix, isExact) {
var boxWidth = font.stringWidth (strLabel) + 8;
var boxHeight = ascent + descent + 8;
var x0 = Clazz.floatToInt (boxXY[0]);
var y0 = Clazz.floatToInt (boxXY[1]);
J.shape.Text.setBoxXY (boxWidth, boxHeight, xOffset, yOffset, boxXY, isExact);
var x = boxXY[0];
var y = boxXY[1];
if (bgcolix != 0 && g3d.setColix (bgcolix)) J.render.TextRenderer.showBox (g3d, colix, Clazz.floatToInt (x), Clazz.floatToInt (y), z, zSlab, Clazz.floatToInt (boxWidth), Clazz.floatToInt (boxHeight), 1, true);
 else g3d.setColix (colix);
g3d.drawString (strLabel, font, Clazz.floatToInt (x + 4), Clazz.floatToInt (y + 4 + ascent), z - 1, zSlab, bgcolix);
if (doPointer) {
g3d.setColix (pointerColix);
if (xOffset > 0) g3d.drawLineXYZ (x0, y0, zSlab, Clazz.floatToInt (x), Clazz.floatToInt (y + boxHeight / 2), zSlab);
 else if (xOffset < 0) g3d.drawLineXYZ (x0, y0, zSlab, Clazz.floatToInt (x + boxWidth), Clazz.floatToInt (y + boxHeight / 2), zSlab);
}}, "J.api.JmolRendererInterface,J.util.JmolFont,~S,~N,~N,~A,~N,~N,~N,~N,~N,~N,~B,~N,~B");
});
