Clazz.declarePackage ("J.rendersurface");
Clazz.load (["J.rendersurface.IsosurfaceRenderer"], "J.rendersurface.MolecularOrbitalRenderer", null, function () {
c$ = Clazz.declareType (J.rendersurface, "MolecularOrbitalRenderer", J.rendersurface.IsosurfaceRenderer);
Clazz.overrideMethod (c$, "render", 
function () {
this.imageFontScaling = this.vwr.getImageFontScaling ();
this.renderIso ();
return this.needTranslucent;
});
Clazz.overrideMethod (c$, "renderInfo", 
function () {
if (this.vwr.am.cmi < 0 || this.mesh.title == null || !this.g3d.setC (this.vwr.getColixBackgroundContrast ())) return;
var fid = this.g3d.getFontFidFS ("Serif", 14 * this.imageFontScaling);
this.g3d.setFontFid (fid);
var lineheight = Math.round (15 * this.imageFontScaling);
var x = Math.round (5 * this.imageFontScaling);
var y = lineheight;
for (var i = 0; i < this.mesh.title.length; i++) if (this.mesh.title[i].length > 0) {
this.g3d.drawStringNoSlab (this.mesh.title[i], null, x, y, 0, 0);
y += lineheight;
}
});
});
