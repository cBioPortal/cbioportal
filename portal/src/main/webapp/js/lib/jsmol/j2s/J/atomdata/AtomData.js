Clazz.declarePackage ("J.atomdata");
c$ = Clazz.decorateAsClass (function () {
this.programInfo = null;
this.fileName = null;
this.modelName = null;
this.modelIndex = 0;
this.bsSelected = null;
this.bsIgnored = null;
this.bsMolecules = null;
this.radiusData = null;
this.firstAtomIndex = 0;
this.firstModelIndex = 0;
this.lastModelIndex = 0;
this.hAtomRadius = 0;
this.atomIndex = null;
this.atomXyz = null;
this.atomRadius = null;
this.atomicNumber = null;
this.atomMolecule = null;
this.hAtoms = null;
this.atomCount = 0;
this.hydrogenAtomCount = 0;
this.adpMode = 0;
Clazz.instantialize (this, arguments);
}, J.atomdata, "AtomData");
Clazz.makeConstructor (c$, 
function () {
});
Clazz.defineStatics (c$,
"MODE_FILL_COORDS", 1,
"MODE_FILL_RADII", 2,
"MODE_FILL_MOLECULES", 4,
"MODE_GET_ATTACHED_HYDROGENS", 8,
"MODE_FILL_MULTIMODEL", 16);
