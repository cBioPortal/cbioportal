Clazz.declarePackage ("J.adapter.readers.more");
Clazz.load (["J.adapter.smarter.AtomSetCollectionReader"], "J.adapter.readers.more.BinaryReader", null, function () {
c$ = Clazz.decorateAsClass (function () {
this.binaryDoc = null;
Clazz.instantialize (this, arguments);
}, J.adapter.readers.more, "BinaryReader", J.adapter.smarter.AtomSetCollectionReader);
Clazz.makeConstructor (c$, 
function () {
Clazz.superConstructor (this, J.adapter.readers.more.BinaryReader, []);
this.isBinary = true;
});
Clazz.overrideMethod (c$, "processBinaryDocument", 
function (doc) {
this.binaryDoc = doc;
this.readDocument ();
}, "J.api.JmolDocument");
});
