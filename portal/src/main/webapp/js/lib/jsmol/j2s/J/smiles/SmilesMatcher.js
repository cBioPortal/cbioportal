Clazz.declarePackage ("J.smiles");
Clazz.load (["J.api.SmilesMatcherInterface"], "J.smiles.SmilesMatcher", ["J.smiles.InvalidSmilesException", "$.SmilesGenerator", "$.SmilesParser", "J.util.ArrayUtil", "$.BS", "$.BSUtil", "$.TextFormat"], function () {
c$ = Clazz.declareType (J.smiles, "SmilesMatcher", null, J.api.SmilesMatcherInterface);
Clazz.overrideMethod (c$, "getLastException", 
function () {
return J.smiles.InvalidSmilesException.getLastError ();
});
Clazz.overrideMethod (c$, "getMolecularFormula", 
function (pattern, isSmarts) {
J.smiles.InvalidSmilesException.setLastError (null);
try {
var search = J.smiles.SmilesParser.getMolecule (pattern, isSmarts);
search.createTopoMap (null);
search.nodes = search.jmolAtoms;
return search.getMolecularFormula (!isSmarts);
} catch (e) {
if (Clazz.exceptionOf (e, J.smiles.InvalidSmilesException)) {
if (J.smiles.InvalidSmilesException.getLastError () == null) J.smiles.InvalidSmilesException.setLastError (e.toString ());
return null;
} else {
throw e;
}
}
}, "~S,~B");
Clazz.overrideMethod (c$, "getSmiles", 
function (atoms, atomCount, bsSelected, asBioSmiles, allowUnmatchedRings, addCrossLinks, comment) {
J.smiles.InvalidSmilesException.setLastError (null);
try {
if (asBioSmiles) return ( new J.smiles.SmilesGenerator ()).getBioSmiles (atoms, atomCount, bsSelected, allowUnmatchedRings, addCrossLinks, comment);
return ( new J.smiles.SmilesGenerator ()).getSmiles (atoms, atomCount, bsSelected);
} catch (e) {
if (Clazz.exceptionOf (e, J.smiles.InvalidSmilesException)) {
if (J.smiles.InvalidSmilesException.getLastError () == null) J.smiles.InvalidSmilesException.setLastError (e.toString ());
return null;
} else {
throw e;
}
}
}, "~A,~N,J.util.BS,~B,~B,~B,~S");
$_M(c$, "areEqual", 
function (smiles1, smiles2) {
var result = this.find (smiles1, smiles2, false, false);
return (result == null ? -1 : result.length);
}, "~S,~S");
$_M(c$, "areEqual", 
function (smiles, molecule) {
var ret = this.find (smiles, molecule, false, true, true);
return (ret != null && ret.length == 1);
}, "~S,J.smiles.SmilesSearch");
$_M(c$, "find", 
function (pattern, smiles, isSmarts, firstMatchOnly) {
J.smiles.InvalidSmilesException.setLastError (null);
try {
var search = J.smiles.SmilesParser.getMolecule (smiles, false);
return this.find (pattern, search, isSmarts, !isSmarts, firstMatchOnly);
} catch (e) {
if (Clazz.exceptionOf (e, Exception)) {
if (J.smiles.InvalidSmilesException.getLastError () == null) J.smiles.InvalidSmilesException.setLastError (e.toString ());
System.out.println (e.toString ());
return null;
} else {
throw e;
}
}
}, "~S,~S,~B,~B");
Clazz.overrideMethod (c$, "getRelationship", 
function (smiles1, smiles2) {
if (smiles1 == null || smiles2 == null || smiles1.length == 0 || smiles2.length == 0) return "";
var mf1 = this.getMolecularFormula (smiles1, false);
var mf2 = this.getMolecularFormula (smiles2, false);
if (!mf1.equals (mf2)) return "none";
var check;
var n1 = this.countStereo (smiles1);
var n2 = this.countStereo (smiles2);
check = (n1 == n2 && this.areEqual (smiles2, smiles1) > 0);
if (!check) {
var s = smiles1 + smiles2;
if (s.indexOf ("/") >= 0 || s.indexOf ("\\") >= 0 || s.indexOf ("@") >= 0) {
if (n1 == n2 && n1 > 0) {
smiles1 = this.reverseChirality (smiles1);
check = (this.areEqual (smiles1, smiles2) > 0);
if (check) return "enantiomers";
}check = (this.areEqual ("/nostereo/" + smiles2, smiles1) > 0);
if (check) return (n1 == n2 ? "diastereomers" : "ambiguous stereochemistry!");
}return "constitutional isomers";
}return "identical";
}, "~S,~S");
Clazz.overrideMethod (c$, "reverseChirality", 
function (smiles) {
smiles = J.util.TextFormat.simpleReplace (smiles, "@@", "!@");
smiles = J.util.TextFormat.simpleReplace (smiles, "@", "@@");
smiles = J.util.TextFormat.simpleReplace (smiles, "!@@", "@");
smiles = J.util.TextFormat.simpleReplace (smiles, "@@SP", "@SP");
smiles = J.util.TextFormat.simpleReplace (smiles, "@@OH", "@OH");
smiles = J.util.TextFormat.simpleReplace (smiles, "@@TB", "@TB");
return smiles;
}, "~S");
Clazz.overrideMethod (c$, "getSubstructureSet", 
function (pattern, atoms, atomCount, bsSelected, isSmarts, firstMatchOnly) {
return this.match (pattern, atoms, atomCount, bsSelected, null, isSmarts, false, firstMatchOnly, 1);
}, "~S,~A,~N,J.util.BS,~B,~B");
Clazz.overrideMethod (c$, "getSubstructureSets", 
function (smarts, atoms, atomCount, flags, bsSelected, ret, vRings) {
J.smiles.InvalidSmilesException.setLastError (null);
var sp =  new J.smiles.SmilesParser (true);
var search = null;
try {
search = sp.parse ("");
search.firstMatchOnly = false;
search.matchAllAtoms = false;
search.jmolAtoms = atoms;
search.jmolAtomCount = Math.abs (atomCount);
search.setSelected (bsSelected);
search.getRingData (true, flags, vRings);
search.asVector = false;
search.subSearches =  new Array (1);
search.getSelections ();
} catch (e) {
if (Clazz.exceptionOf (e, J.smiles.InvalidSmilesException)) {
} else {
throw e;
}
}
var bsDone =  new J.util.BS ();
for (var i = 0; i < smarts.length; i++) {
if (smarts[i] == null || smarts[i].length == 0 || smarts[i].startsWith ("#")) {
ret.addLast (null);
continue;
}try {
search.clear ();
var ss = sp.getSearch (search, J.smiles.SmilesParser.cleanPattern (smarts[i]), flags);
search.subSearches[0] = ss;
var bs = J.util.BSUtil.copy (search.search (false));
ret.addLast (bs);
bsDone.or (bs);
if (bsDone.cardinality () == atomCount) return;
} catch (e) {
if (Clazz.exceptionOf (e, Exception)) {
if (J.smiles.InvalidSmilesException.getLastError () == null) J.smiles.InvalidSmilesException.setLastError (e.toString ());
System.out.println (e.toString ());
} else {
throw e;
}
}
}
}, "~A,~A,~N,~N,J.util.BS,J.util.JmolList,~A");
Clazz.overrideMethod (c$, "getSubstructureSetArray", 
function (pattern, atoms, atomCount, bsSelected, bsAromatic, isSmarts, firstMatchOnly) {
return this.match (pattern, atoms, atomCount, bsSelected, bsAromatic, isSmarts, false, firstMatchOnly, 2);
}, "~S,~A,~N,J.util.BS,J.util.BS,~B,~B");
Clazz.overrideMethod (c$, "getCorrelationMaps", 
function (pattern, atoms, atomCount, bsSelected, isSmarts, firstMatchOnly) {
return this.match (pattern, atoms, atomCount, bsSelected, null, isSmarts, false, firstMatchOnly, 3);
}, "~S,~A,~N,J.util.BS,~B,~B");
$_M(c$, "find", 
($fz = function (pattern, search, isSmarts, matchAllAtoms, firstMatchOnly) {
var bsAromatic =  new J.util.BS ();
search.createTopoMap (bsAromatic);
return this.match (pattern, search.jmolAtoms, -search.jmolAtoms.length, null, bsAromatic, isSmarts, matchAllAtoms, firstMatchOnly, 2);
}, $fz.isPrivate = true, $fz), "~S,J.smiles.SmilesSearch,~B,~B,~B");
$_M(c$, "match", 
($fz = function (pattern, atoms, atomCount, bsSelected, bsAromatic, isSmarts, matchAllAtoms, firstMatchOnly, mode) {
J.smiles.InvalidSmilesException.setLastError (null);
try {
var search = J.smiles.SmilesParser.getMolecule (pattern, isSmarts);
search.jmolAtoms = atoms;
search.jmolAtomCount = Math.abs (atomCount);
if (atomCount < 0) search.isSmilesFind = true;
search.setSelected (bsSelected);
search.getSelections ();
search.bsRequired = null;
search.setRingData (bsAromatic);
search.firstMatchOnly = firstMatchOnly;
search.matchAllAtoms = matchAllAtoms;
switch (mode) {
case 1:
search.asVector = false;
return search.search (false);
case 2:
search.asVector = true;
var vb = search.search (false);
return vb.toArray ( new Array (vb.size ()));
case 3:
search.getMaps = true;
var vl = search.search (false);
return vl.toArray (J.util.ArrayUtil.newInt2 (vl.size ()));
}
} catch (e) {
if (Clazz.exceptionOf (e, Exception)) {
if (J.smiles.InvalidSmilesException.getLastError () == null) J.smiles.InvalidSmilesException.setLastError (e.toString ());
System.out.println (e.toString ());
} else {
throw e;
}
}
return null;
}, $fz.isPrivate = true, $fz), "~S,~A,~N,J.util.BS,J.util.BS,~B,~B,~B,~N");
$_M(c$, "countStereo", 
($fz = function (s) {
s = J.util.TextFormat.simpleReplace (s, "@@", "@");
var i = s.lastIndexOf ('@') + 1;
var n = 0;
for (; --i >= 0; ) if (s.charAt (i) == '@') n++;

return n;
}, $fz.isPrivate = true, $fz), "~S");
Clazz.defineStatics (c$,
"MODE_BITSET", 1,
"MODE_ARRAY", 2,
"MODE_MAP", 3);
});
