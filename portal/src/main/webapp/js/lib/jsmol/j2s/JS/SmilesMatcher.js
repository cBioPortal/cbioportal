Clazz.declarePackage ("JS");
Clazz.load (["J.api.SmilesMatcherInterface"], "JS.SmilesMatcher", ["JU.AU", "$.BS", "$.PT", "JS.InvalidSmilesException", "$.SmilesGenerator", "$.SmilesParser", "JU.BSUtil"], function () {
c$ = Clazz.declareType (JS, "SmilesMatcher", null, J.api.SmilesMatcherInterface);
Clazz.overrideMethod (c$, "getLastException", 
function () {
return JS.InvalidSmilesException.getLastError ();
});
Clazz.overrideMethod (c$, "getMolecularFormula", 
function (pattern, isSmarts) {
JS.InvalidSmilesException.clear ();
var search = JS.SmilesParser.getMolecule (pattern, isSmarts);
search.createTopoMap (null);
search.nodes = search.jmolAtoms;
return search.getMolecularFormula (!isSmarts);
}, "~S,~B");
Clazz.overrideMethod (c$, "getSmiles", 
function (atoms, ac, bsSelected, asBioSmiles, bioAllowUnmatchedRings, bioAddCrossLinks, bioComment, explicitH) {
JS.InvalidSmilesException.clear ();
if (asBioSmiles) return ( new JS.SmilesGenerator ()).getBioSmiles (atoms, ac, bsSelected, bioAllowUnmatchedRings, bioAddCrossLinks, bioComment);
return ( new JS.SmilesGenerator ()).getSmiles (atoms, ac, bsSelected, explicitH);
}, "~A,~N,JU.BS,~B,~B,~B,~S,~B");
Clazz.defineMethod (c$, "areEqual", 
function (smiles1, smiles2) {
var result = this.find (smiles1, smiles2, false, false);
return (result == null ? -1 : result.length);
}, "~S,~S");
Clazz.defineMethod (c$, "areEqual", 
function (smiles, molecule) {
var ret = this.find (smiles, molecule, false, true, true);
return (ret != null && ret.length == 1);
}, "~S,JS.SmilesSearch");
Clazz.defineMethod (c$, "find", 
function (pattern, smiles, isSmarts, firstMatchOnly) {
JS.InvalidSmilesException.clear ();
var search = JS.SmilesParser.getMolecule (smiles, false);
return this.find (pattern, search, isSmarts, !isSmarts, firstMatchOnly);
}, "~S,~S,~B,~B");
Clazz.overrideMethod (c$, "getRelationship", 
function (smiles1, smiles2) {
if (smiles1 == null || smiles2 == null || smiles1.length == 0 || smiles2.length == 0) return "";
var mf1 = this.getMolecularFormula (smiles1, false);
var mf2 = this.getMolecularFormula (smiles2, false);
if (!mf1.equals (mf2)) return "none";
var check;
var n1 = JS.SmilesMatcher.countStereo (smiles1);
var n2 = JS.SmilesMatcher.countStereo (smiles2);
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
smiles = JU.PT.rep (smiles, "@@", "!@");
smiles = JU.PT.rep (smiles, "@", "@@");
smiles = JU.PT.rep (smiles, "!@@", "@");
smiles = JU.PT.rep (smiles, "@@SP", "@SP");
smiles = JU.PT.rep (smiles, "@@OH", "@OH");
smiles = JU.PT.rep (smiles, "@@TB", "@TB");
return smiles;
}, "~S");
Clazz.overrideMethod (c$, "getSubstructureSet", 
function (pattern, atoms, ac, bsSelected, isSmarts, firstMatchOnly) {
return this.match (pattern, atoms, ac, bsSelected, null, isSmarts, false, firstMatchOnly, 1);
}, "~S,~A,~N,JU.BS,~B,~B");
Clazz.overrideMethod (c$, "getSubstructureSets", 
function (smarts, atoms, ac, flags, bsSelected, ret, vRings) {
JS.InvalidSmilesException.clear ();
var sp =  new JS.SmilesParser (true);
var search = null;
search = sp.parse ("");
search.firstMatchOnly = false;
search.matchAllAtoms = false;
search.jmolAtoms = atoms;
search.jmolAtomCount = Math.abs (ac);
search.setSelected (bsSelected);
search.getRingData (true, flags, vRings);
search.asVector = false;
search.subSearches =  new Array (1);
search.getSelections ();
var bsDone =  new JU.BS ();
for (var i = 0; i < smarts.length; i++) {
if (smarts[i] == null || smarts[i].length == 0 || smarts[i].startsWith ("#")) {
ret.addLast (null);
continue;
}search.clear ();
var ss = sp.getSearch (search, JS.SmilesParser.cleanPattern (smarts[i]), flags);
search.subSearches[0] = ss;
var bs = JU.BSUtil.copy (search.search (false));
ret.addLast (bs);
bsDone.or (bs);
if (bsDone.cardinality () == ac) return;
}
}, "~A,~A,~N,~N,JU.BS,JU.Lst,~A");
Clazz.overrideMethod (c$, "getSubstructureSetArray", 
function (pattern, atoms, ac, bsSelected, bsAromatic, isSmarts, firstMatchOnly) {
return this.match (pattern, atoms, ac, bsSelected, bsAromatic, isSmarts, false, firstMatchOnly, 2);
}, "~S,~A,~N,JU.BS,JU.BS,~B,~B");
Clazz.overrideMethod (c$, "getCorrelationMaps", 
function (pattern, atoms, ac, bsSelected, isSmarts, firstMatchOnly) {
return this.match (pattern, atoms, ac, bsSelected, null, isSmarts, false, firstMatchOnly, 3);
}, "~S,~A,~N,JU.BS,~B,~B");
Clazz.defineMethod (c$, "find", 
 function (pattern, search, isSmarts, matchAllAtoms, firstMatchOnly) {
var bsAromatic =  new JU.BS ();
search.createTopoMap (bsAromatic);
return this.match (pattern, search.jmolAtoms, -search.jmolAtoms.length, null, bsAromatic, isSmarts, matchAllAtoms, firstMatchOnly, 2);
}, "~S,JS.SmilesSearch,~B,~B,~B");
Clazz.defineMethod (c$, "match", 
 function (pattern, atoms, ac, bsSelected, bsAromatic, isSmarts, matchAllAtoms, firstMatchOnly, mode) {
JS.InvalidSmilesException.clear ();
try {
var search = JS.SmilesParser.getMolecule (pattern, isSmarts);
search.jmolAtoms = atoms;
search.jmolAtomCount = Math.abs (ac);
if (ac < 0) search.isSmilesFind = true;
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
return vl.toArray (JU.AU.newInt2 (vl.size ()));
}
} catch (e) {
if (Clazz.exceptionOf (e, Exception)) {
if (JS.InvalidSmilesException.getLastError () == null) JS.InvalidSmilesException.clear ();
throw  new JS.InvalidSmilesException (JS.InvalidSmilesException.getLastError ());
} else {
throw e;
}
}
return null;
}, "~S,~A,~N,JU.BS,JU.BS,~B,~B,~B,~N");
c$.countStereo = Clazz.defineMethod (c$, "countStereo", 
 function (s) {
s = JU.PT.rep (s, "@@", "@");
var i = s.lastIndexOf ('@') + 1;
var n = 0;
for (; --i >= 0; ) if (s.charAt (i) == '@') n++;

return n;
}, "~S");
Clazz.defineStatics (c$,
"MODE_BITSET", 1,
"MODE_ARRAY", 2,
"MODE_MAP", 3);
});
