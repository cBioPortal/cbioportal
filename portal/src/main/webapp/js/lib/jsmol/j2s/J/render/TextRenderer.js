Clazz.declarePackage ("J.render");
Clazz.load (null, "J.render.TextRenderer", ["java.lang.Float", "J.modelset.Text"], function () {
c$ = Clazz.declareType (J.render, "TextRenderer");
c$.render = $_M(c$, "render", 
function (text, viewer, g3d, scalePixelsPerMicron, imageFontScaling, isExact, boxXY, xy) {
if (text == null || text.image == null && text.lines == null) return;
var showText = g3d.setColix (text.colix);
if (!showText && (text.image == null && (text.bgcolix == 0 || !g3d.setColix (text.bgcolix)))) return;
text.setPosition (viewer, g3d.getRenderWidth (), g3d.getRenderHeight (), scalePixelsPerMicron, imageFontScaling, isExact, boxXY);
if (text.image == null && text.bgcolix != 0) {
if (showText) g3d.setColix (text.bgcolix);
J.render.TextRenderer.showBox (g3d, text.colix, Clazz.floatToInt (text.boxX), Clazz.floatToInt (text.boxY) + text.boxYoff2 * 2, text.z + 2, text.zSlab, Clazz.floatToInt (text.boxWidth), Clazz.floatToInt (text.boxHeight), text.fontScale, text.isLabelOrHover);
if (!showText) return;
}if (text.image == null) {
for (var i = 0; i < text.lines.length; i++) {
text.setXYA (xy, i);
g3d.drawString (text.lines[i], text.font, Clazz.floatToInt (xy[0]), Clazz.floatToInt (xy[1]), text.z, text.zSlab, text.bgcolix);
}
} else {
g3d.drawImage (text.image, Clazz.floatToInt (text.boxX), Clazz.floatToInt (text.boxY), text.z, text.zSlab, text.bgcolix, Clazz.floatToInt (text.boxWidth), Clazz.floatToInt (text.boxHeight));
}J.render.TextRenderer.drawPointer (text, g3d);
return;
}, "J.modelset.Text,J.viewer.Viewer,J.api.JmolRendererInterface,~N,~N,~B,~A,~A");
c$.drawPointer = $_M(c$, "drawPointer", 
function (text, g3d) {
if ((text.pointer & 1) == 0 || !g3d.setColix ((text.pointer & 2) != 0 && text.bgcolix != 0 ? text.bgcolix : text.colix)) return;
var w = text.boxWidth;
var h = text.boxHeight;
var pt = NaN;
var x = text.boxX + (text.boxX > text.atomX + w ? 0 : text.boxX + w < text.atomX - w ? w : (pt = w / 2));
var setY = !Float.isNaN (pt);
var y = text.boxY + (setY && text.boxY > text.atomY ? 0 : setY && text.boxY + h < text.atomY ? h : h / 2);
g3d.drawLineXYZ (text.atomX, text.atomY, text.atomZ, Clazz.floatToInt (x), Clazz.floatToInt (y), text.zSlab);
}, "J.modelset.Text,J.api.JmolRendererInterface");
c$.renderSimpleLabel = $_M(c$, "renderSimpleLabel", 
function (g3d, font, strLabel, colix, bgcolix, boxXY, z, zSlab, xOffset, yOffset, ascent, descent, doPointer, pointerColix, isExact) {
var boxWidth = font.stringWidth (strLabel) + 8;
var boxHeight = ascent + descent + 8;
var x0 = Clazz.floatToInt (boxXY[0]);
var y0 = Clazz.floatToInt (boxXY[1]);
J.modelset.Text.setBoxXY (boxWidth, boxHeight, xOffset, yOffset, boxXY, isExact);
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
});
