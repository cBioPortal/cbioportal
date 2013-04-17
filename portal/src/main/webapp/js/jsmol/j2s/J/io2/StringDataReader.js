Clazz.declarePackage ("J.io2");
Clazz.load (["J.io.DataReader"], "J.io2.StringDataReader", ["java.io.StringReader"], function () {
c$ = Clazz.declareType (J.io2, "StringDataReader", J.io.DataReader);
Clazz.makeConstructor (c$, 
function (data) {
Clazz.superConstructor (this, J.io2.StringDataReader, [ new java.io.StringReader (data)]);
}, "~S");
Clazz.overrideMethod (c$, "setData", 
function (data) {
return  new J.io2.StringDataReader (data);
}, "~O");
});
