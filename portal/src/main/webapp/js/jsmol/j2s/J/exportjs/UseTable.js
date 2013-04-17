Clazz.declarePackage ("J.exportjs");
Clazz.load (["java.util.Hashtable"], "J.exportjs.UseTable", null, function () {
c$ = Clazz.decorateAsClass (function () {
this.iObj = 0;
Clazz.instantialize (this, arguments);
}, J.exportjs, "UseTable", java.util.Hashtable);
Clazz.makeConstructor (c$, 
function () {
Clazz.superConstructor (this, J.exportjs.UseTable, []);
});
$_M(c$, "getDef", 
function (key, ret) {
if (this.containsKey (key)) {
ret[0] = this.get (key);
return true;
}var id = "_" + key.charAt (0) + (this.iObj++);
this.put (key, id);
ret[0] = id;
return false;
}, "~S,~A");
});
