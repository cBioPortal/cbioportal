Clazz.declarePackage ("J.util");
Clazz.load (["java.util.ArrayList"], "J.util.JmolList", null, function () {
c$ = Clazz.declareType (J.util, "JmolList", java.util.ArrayList);
$_M(c$, "addLast", 
function (v) {
{
return this.add1(v);
}}, "~O");
$_M(c$, "removeObj", 
function (v) {
{
return this.removeObject(v);
}}, "~O");
});
