Clazz.declarePackage ("J.exportjs");
Clazz.load (["J.export.image.GenericImageCreator"], "J.exportjs.JSImageCreator", null, function () {
c$ = Clazz.declareType (J.exportjs, "JSImageCreator", J["export"].image.GenericImageCreator);
Clazz.makeConstructor (c$, 
function () {
Clazz.superConstructor (this, J.exportjs.JSImageCreator, []);
});
});
