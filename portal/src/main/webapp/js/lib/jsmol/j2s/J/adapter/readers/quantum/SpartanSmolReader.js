Clazz.declarePackage ("J.adapter.readers.quantum");
Clazz.load (["J.adapter.readers.quantum.SpartanInputReader"], "J.adapter.readers.quantum.SpartanSmolReader", ["java.lang.Boolean", "java.util.Hashtable", "J.adapter.readers.quantum.SpartanArchive", "J.io2.BinaryDocument", "J.util.Logger", "$.Parser", "$.SB"], function () {
c$ = Clazz.decorateAsClass (function () {
this.iHaveModelStatement = false;
this.isCompoundDocument = false;
this.inputOnly = false;
this.espCharges = false;
this.endCheck = "END Directory Entry ";
this.title = null;
this.spartanArchive = null;
this.titles = null;
this.haveCharges = false;
Clazz.instantialize (this, arguments);
}, J.adapter.readers.quantum, "SpartanSmolReader", J.adapter.readers.quantum.SpartanInputReader);
Clazz.overrideMethod (c$, "initializeReader", 
function () {
this.modelName = "Spartan file";
this.isCompoundDocument = (this.readLine ().indexOf ("Compound Document File Directory") >= 0);
this.inputOnly = this.checkFilterKey ("INPUT");
this.espCharges = !this.checkFilterKey ("MULLIKEN");
});
Clazz.overrideMethod (c$, "checkLine", 
function () {
var lcline;
if (this.isCompoundDocument && (lcline = this.line.toLowerCase ()).equals ("begin directory entry molecule") || this.line.indexOf ("JMOL_MODEL") >= 0 && !this.line.startsWith ("END")) {
if (this.modelNumber > 0) this.applySymmetryAndSetTrajectory ();
this.iHaveModelStatement = true;
var modelNo = this.getModelNumber ();
this.modelNumber = (this.bsModels == null && modelNo != -2147483648 ? modelNo : this.modelNumber + 1);
this.bondData = "";
if (!this.doGetModel (this.modelNumber, null)) return this.checkLastModel ();
if (this.modelAtomCount == 0) this.atomSetCollection.newAtomSet ();
this.moData =  new java.util.Hashtable ();
this.moData.put ("isNormalized", Boolean.TRUE);
if (modelNo == -2147483648) {
modelNo = this.modelNumber;
this.title = "Model " + modelNo;
} else {
this.title = this.titles.get ("Title" + modelNo);
this.title = "Profile " + modelNo + (this.title == null ? "" : ": " + this.title);
}J.util.Logger.info (this.title);
this.atomSetCollection.setAtomSetName (this.title);
this.atomSetCollection.setAtomSetAuxiliaryInfo ("isPDB", Boolean.FALSE);
this.atomSetCollection.setCurrentAtomSetNumber (modelNo);
if (this.isCompoundDocument) this.readTransform ();
return true;
}if (this.iHaveModelStatement && !this.doProcessLines) return true;
if ((this.line.indexOf ("BEGIN") == 0)) {
lcline = this.line.toLowerCase ();
if (lcline.endsWith ("input")) {
this.bondData = "";
this.readInputRecords ();
if (this.atomSetCollection.errorMessage != null) {
this.continuing = false;
return false;
}if (this.title != null) this.atomSetCollection.setAtomSetName (this.title);
this.setCharges ();
if (this.inputOnly) {
this.continuing = false;
return false;
}} else if (lcline.endsWith ("_output")) {
return true;
} else if (lcline.endsWith ("output")) {
this.readOutput ();
return false;
} else if (lcline.endsWith ("molecule") || lcline.endsWith ("molecule:asbinarystring")) {
this.readTransform ();
return false;
} else if (lcline.endsWith ("proparc") || lcline.endsWith ("propertyarchive")) {
this.readProperties ();
return false;
} else if (lcline.endsWith ("archive")) {
this.readArchive ();
return false;
}return true;
}if (this.line.indexOf ("5D shell") >= 0) this.moData.put ("calculationType", this.calculationType = this.line);
return true;
});
Clazz.overrideMethod (c$, "finalizeReader", 
function () {
this.finalizeReaderASCR ();
if (this.atomCount > 0 && this.spartanArchive != null && this.atomSetCollection.getBondCount () == 0 && this.bondData != null) this.spartanArchive.addBonds (this.bondData, 0);
if (this.moData != null) {
var n = this.atomSetCollection.getAtomSetCollectionAuxiliaryInfo ("HOMO_N");
if (n != null) this.moData.put ("HOMO", Integer.$valueOf (n.intValue ()));
}});
$_M(c$, "readTransform", 
($fz = function () {
var $private = Clazz.checkPrivateMethod (arguments);
if ($private != null) {
return $private.apply (this, arguments);
}
var mat;
var binaryCodes = this.readLine ();
var tokens = J.adapter.smarter.AtomSetCollectionReader.getTokensStr (binaryCodes.trim ());
if (tokens.length < 16) return;
var bytes =  Clazz.newByteArray (tokens.length, 0);
for (var i = 0; i < tokens.length; i++) bytes[i] = J.util.Parser.parseIntRadix (tokens[i], 16);

mat =  Clazz.newFloatArray (16, 0);
for (var i = 16, j = bytes.length - 8; --i >= 0; j -= 8) mat[i] = J.io2.BinaryDocument.bytesToDoubleToFloat (bytes, j, false);

this.setTransform (mat[0], mat[1], mat[2], mat[4], mat[5], mat[6], mat[8], mat[9], mat[10]);
}, $fz.isPrivate = true, $fz));
$_M(c$, "readOutput", 
($fz = function () {
this.titles =  new java.util.Hashtable ();
var header =  new J.util.SB ();
var pt;
while (this.readLine () != null && !this.line.startsWith ("END ")) {
header.append (this.line).append ("\n");
if ((pt = this.line.indexOf (")")) > 0) this.titles.put ("Title" + this.parseIntRange (this.line, 0, pt), (this.line.substring (pt + 1).trim ()));
}
this.atomSetCollection.setAtomSetCollectionAuxiliaryInfo ("fileHeader", header.toString ());
}, $fz.isPrivate = true, $fz));
$_M(c$, "readArchive", 
($fz = function () {
this.spartanArchive =  new J.adapter.readers.quantum.SpartanArchive (this, this.bondData, this.endCheck);
if (this.readArchiveHeader ()) {
this.modelAtomCount = this.spartanArchive.readArchive (this.line, false, this.atomCount, false);
if (this.atomCount == 0 || !this.isTrajectory) this.atomCount += this.modelAtomCount;
}}, $fz.isPrivate = true, $fz));
$_M(c$, "setCharges", 
($fz = function () {
if (this.haveCharges || this.atomSetCollection.getAtomCount () == 0) return;
this.haveCharges = (this.espCharges && this.atomSetCollection.setAtomSetCollectionPartialCharges ("ESPCHARGES") || this.atomSetCollection.setAtomSetCollectionPartialCharges ("MULCHARGES") || this.atomSetCollection.setAtomSetCollectionPartialCharges ("Q1_CHARGES") || this.atomSetCollection.setAtomSetCollectionPartialCharges ("ESPCHARGES"));
}, $fz.isPrivate = true, $fz));
$_M(c$, "readProperties", 
($fz = function () {
if (this.spartanArchive == null) {
this.readLine ();
return;
}this.spartanArchive.readProperties ();
this.readLine ();
this.setCharges ();
}, $fz.isPrivate = true, $fz));
$_M(c$, "getModelNumber", 
($fz = function () {
try {
var pt = this.line.indexOf ("JMOL_MODEL ") + 11;
return this.parseIntAt (this.line, pt);
} catch (e) {
if (Clazz.exceptionOf (e, NumberFormatException)) {
return 0;
} else {
throw e;
}
}
}, $fz.isPrivate = true, $fz));
$_M(c$, "readArchiveHeader", 
($fz = function () {
var modelInfo = this.readLine ();
if (J.util.Logger.debugging) J.util.Logger.debug (modelInfo);
if (modelInfo.indexOf ("Error:") == 0) return false;
this.atomSetCollection.setCollectionName (modelInfo);
this.modelName = this.readLine ();
if (J.util.Logger.debugging) J.util.Logger.debug (this.modelName);
this.readLine ();
return true;
}, $fz.isPrivate = true, $fz));
});
