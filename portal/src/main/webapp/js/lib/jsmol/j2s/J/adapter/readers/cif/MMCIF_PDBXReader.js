Clazz.declarePackage ("J.adapter.readers.cif");
Clazz.load (["J.adapter.readers.cif.CifReader"], "J.adapter.readers.cif.MMCIF_PDBXReader", null, function () {
c$ = Clazz.declareType (J.adapter.readers.cif, "MMCIF_PDBXReader", J.adapter.readers.cif.CifReader);
Clazz.overrideMethod (c$, "initializeReader", 
function () {
this.isPDBX = true;
this.initializeReaderCif ();
});
});
