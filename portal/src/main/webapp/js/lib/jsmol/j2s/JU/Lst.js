Clazz.declarePackage ("JU");
Clazz.load (["java.util.ArrayList"], "JU.Lst", null, function () {
c$ = Clazz.declareType (JU, "Lst", java.util.ArrayList);
Clazz.defineMethod (c$, "addLast", 
function (v) {
{
return this.add1(v);
}}, "~O");
Clazz.defineMethod (c$, "removeObj", 
function (v) {
{
return this.removeObject(v);
}}, "~O");
});
