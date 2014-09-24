Clazz.declarePackage ("J.adapter.readers.pdb");
Clazz.load (["J.adapter.readers.pdb.PdbReader"], "J.adapter.readers.pdb.PqrReader", null, function () {
c$ = Clazz.decorateAsClass (function () {
this.$gromacsWideFormat = false;
Clazz.instantialize (this, arguments);
}, J.adapter.readers.pdb, "PqrReader", J.adapter.readers.pdb.PdbReader);
Clazz.defineMethod (c$, "initializeReader", 
function () {
this.isPQR = true;
Clazz.superCall (this, J.adapter.readers.pdb.PqrReader, "initializeReader", []);
});
});
