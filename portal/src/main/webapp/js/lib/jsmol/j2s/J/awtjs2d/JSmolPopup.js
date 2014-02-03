Clazz.declarePackage ("J.awtjs2d");
Clazz.load (["J.awtjs2d.JSPopup"], "J.awtjs2d.JSmolPopup", ["J.i18n.GT", "J.popup.MainPopupResourceBundle"], function () {
c$ = Clazz.declareType (J.awtjs2d, "JSmolPopup", J.awtjs2d.JSPopup);
Clazz.makeConstructor (c$, 
function () {
Clazz.superConstructor (this, J.awtjs2d.JSmolPopup, []);
});
Clazz.overrideMethod (c$, "jpiInitialize", 
function (viewer, menu) {
var doTranslate = J.i18n.GT.setDoTranslate (true);
var bundle =  new J.popup.MainPopupResourceBundle (this.strMenuStructure = menu, this.menuText);
this.initialize (viewer, bundle, bundle.getMenuName ());
J.i18n.GT.setDoTranslate (doTranslate);
}, "J.viewer.Viewer,~S");
});
