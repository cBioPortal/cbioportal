Clazz.declarePackage ("J.adapter.readers.quantum");
Clazz.load (["J.adapter.readers.quantum.SpartanInputReader"], "J.adapter.readers.quantum.OdysseyReader", null, function () {
c$ = Clazz.declareType (J.adapter.readers.quantum, "OdysseyReader", J.adapter.readers.quantum.SpartanInputReader);
Clazz.overrideMethod (c$, "initializeReader", 
function () {
this.modelName = "Odyssey file";
this.readInputRecords ();
this.continuing = false;
});
});
