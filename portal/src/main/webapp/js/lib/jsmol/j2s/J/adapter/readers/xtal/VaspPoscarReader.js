Clazz.declarePackage ("J.adapter.readers.xtal");
Clazz.load (["J.adapter.smarter.AtomSetCollectionReader", "JU.Lst"], "J.adapter.readers.xtal.VaspPoscarReader", ["JU.SB", "JU.Logger"], function () {
c$ = Clazz.decorateAsClass (function () {
this.atomLabels = null;
this.ac = 0;
Clazz.instantialize (this, arguments);
}, J.adapter.readers.xtal, "VaspPoscarReader", J.adapter.smarter.AtomSetCollectionReader);
Clazz.prepareFields (c$, function () {
this.atomLabels =  new JU.Lst ();
});
Clazz.overrideMethod (c$, "initializeReader", 
function () {
this.readJobTitle ();
this.readUnitCellVectors ();
this.readMolecularFormula ();
this.readCoordinates ();
this.continuing = false;
});
Clazz.defineMethod (c$, "readJobTitle", 
 function () {
this.asc.setAtomSetName (this.rd ().trim ());
});
Clazz.defineMethod (c$, "readUnitCellVectors", 
 function () {
this.setSpaceGroupName ("P1");
this.setFractionalCoordinates (true);
var scaleFac = this.parseFloatStr (this.rd ().trim ());
var unitCellData =  Clazz.newFloatArray (9, 0);
this.fillFloatArray (null, 0, unitCellData);
if (scaleFac != 1) for (var i = 0; i < unitCellData.length; i++) unitCellData[i] *= scaleFac;

this.addPrimitiveLatticeVector (0, unitCellData, 0);
this.addPrimitiveLatticeVector (1, unitCellData, 3);
this.addPrimitiveLatticeVector (2, unitCellData, 6);
});
Clazz.defineMethod (c$, "readMolecularFormula", 
 function () {
var elementLabel = J.adapter.smarter.AtomSetCollectionReader.getTokensStr (this.discardLinesUntilNonBlank ());
var elementCounts = J.adapter.smarter.AtomSetCollectionReader.getTokensStr (this.rd ());
var mf =  new JU.SB ();
for (var i = 0; i < elementCounts.length; i++) {
var n = Integer.parseInt (elementCounts[i]);
this.ac += n;
var label = elementLabel[i];
mf.append (" ").append (label).appendI (n);
for (var j = n; --j >= 0; ) this.atomLabels.addLast (label);

}
var s = mf.toString ();
JU.Logger.info ("VaspPoscar reader: " + this.ac + " atoms identified for" + s);
this.appendLoadNote (s);
this.asc.newAtomSet ();
this.asc.setAtomSetName (s);
});
Clazz.defineMethod (c$, "readCoordinates", 
 function () {
if (this.discardLinesUntilNonBlank ().toLowerCase ().contains ("selective")) this.rd ();
if (this.line.toLowerCase ().contains ("cartesian")) this.setFractionalCoordinates (false);
for (var i = 0; i < this.ac; i++) this.addAtomXYZSymName (J.adapter.smarter.AtomSetCollectionReader.getTokensStr (this.rd ()), 0, null, this.atomLabels.get (i));

});
});
