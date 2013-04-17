Clazz.declarePackage ("J.adapter.readers.more");
Clazz.load (["J.adapter.readers.cifpdb.PdbReader"], "J.adapter.readers.more.PqrReader", null, function () {
c$ = Clazz.decorateAsClass (function () {
this.$gromacsWideFormat = false;
Clazz.instantialize (this, arguments);
}, J.adapter.readers.more, "PqrReader", J.adapter.readers.cifpdb.PdbReader);
$_M(c$, "initializeReader", 
function () {
this.isPQR = true;
Clazz.superCall (this, J.adapter.readers.more.PqrReader, "initializeReader", []);
});
});
