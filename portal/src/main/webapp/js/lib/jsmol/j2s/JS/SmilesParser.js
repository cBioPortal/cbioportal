Clazz.declarePackage ("JS");
Clazz.load (["java.util.Hashtable"], "JS.SmilesParser", ["java.lang.Character", "JU.Lst", "$.PT", "$.SB", "JS.InvalidSmilesException", "$.SmilesAtom", "$.SmilesBond", "$.SmilesMeasure", "$.SmilesSearch", "JU.Elements", "$.Logger", "$.Txt"], function () {
c$ = Clazz.decorateAsClass (function () {
this.isSmarts = false;
this.isBioSequence = false;
this.bioType = '\0';
this.ringBonds = null;
this.braceCount = 0;
this.branchLevel = 0;
this.flags = 0;
this.htMeasures = null;
Clazz.instantialize (this, arguments);
}, JS, "SmilesParser");
Clazz.prepareFields (c$, function () {
this.ringBonds =  new java.util.Hashtable ();
this.htMeasures =  new java.util.Hashtable ();
});
c$.getMolecule = Clazz.defineMethod (c$, "getMolecule", 
function (pattern, isSmarts) {
return ( new JS.SmilesParser (isSmarts)).parse (pattern);
}, "~S,~B");
Clazz.makeConstructor (c$, 
function (isSmarts) {
this.isSmarts = isSmarts;
}, "~B");
Clazz.defineMethod (c$, "reset", 
function () {
this.braceCount = 0;
this.branchLevel = 0;
});
Clazz.defineMethod (c$, "parse", 
function (pattern) {
if (pattern == null) throw  new JS.InvalidSmilesException ("SMILES expressions must not be null");
var search =  new JS.SmilesSearch ();
if (pattern.indexOf ("$(select") >= 0) pattern = this.parseNested (search, pattern, "select");
pattern = JS.SmilesParser.cleanPattern (pattern);
while (pattern.startsWith ("/")) {
var strFlags = JS.SmilesParser.getSubPattern (pattern, 0, '/').toUpperCase ();
pattern = pattern.substring (strFlags.length);
this.flags = 0;
if (strFlags.indexOf ("NOAROMATIC") >= 0) this.flags |= 1;
if (strFlags.indexOf ("AROMATICSTRICT") >= 0) this.flags |= 4;
if (strFlags.indexOf ("AROMATICDEFINED") >= 0) this.flags |= 8;
if (strFlags.indexOf ("AROMATICDOUBLE") >= 0) this.flags |= 16;
if (strFlags.indexOf ("NOSTEREO") >= 0) this.flags |= 2;
}
if (pattern.indexOf ("$") >= 0) pattern = this.parseVariables (pattern);
if (this.isSmarts && pattern.indexOf ("[$") >= 0) pattern = this.parseVariableLength (pattern);
if (pattern.indexOf ("||") >= 0) {
var patterns = JU.PT.split (pattern, "||");
var toDo = "";
search.subSearches =  new Array (patterns.length);
for (var i = 0; i < patterns.length; i++) {
var key = "|" + patterns[i] + "|";
if (toDo.indexOf (key) < 0) {
search.subSearches[i] = this.getSearch (search, patterns[i], this.flags);
toDo += key;
}}
JU.Logger.info (toDo);
return search;
}return this.getSearch (search, pattern, this.flags);
}, "~S");
Clazz.defineMethod (c$, "parseVariableLength", 
 function (pattern) {
var sout =  new JU.SB ();
var len = pattern.length - 1;
var nParen = 0;
var haveInternalOr = false;
for (var i = 0; i < len; i++) {
switch (pattern.charAt (i)) {
case '(':
nParen++;
break;
case ')':
nParen--;
break;
case '|':
if (nParen > 0) {
haveInternalOr = true;
if (pattern.charAt (i + 1) == '|') {
pattern = pattern.substring (0, i) + pattern.substring (i + 1);
len--;
}}break;
}
}
if (pattern.indexOf ("||") >= 0) {
var patterns = JU.PT.split (pattern, "||");
for (var i = 0; i < patterns.length; i++) sout.append ("||").append (this.parseVariableLength (patterns[i]));

} else {
var pt = -1;
var ret =  Clazz.newIntArray (1, 0);
var isOK = true;
var bracketed = null;
while ((pt = pattern.indexOf ("[$", pt + 1)) >= 0) {
var pt0 = pt;
var min = -2147483648;
var max = -2147483648;
pt = JS.SmilesParser.getDigits (pattern, pt + 2, ret);
min = ret[0];
if (min != -2147483648) {
if (JS.SmilesParser.getChar (pattern, pt) == '-') {
pt = JS.SmilesParser.getDigits (pattern, pt + 1, ret);
max = ret[0];
}}if (JS.SmilesParser.getChar (pattern, pt) != '(') continue;
bracketed = JS.SmilesParser.getSubPattern (pattern, pt0, '[');
if (!bracketed.endsWith (")")) continue;
var pt1 = pt0 + bracketed.length + 2;
var repeat = JS.SmilesParser.getSubPattern (pattern, pt, '(');
var pt2 = pt;
bracketed = JS.SmilesParser.getSubPattern (pattern, pt, '[');
pt += 1 + repeat.length;
if (repeat.indexOf (':') >= 0 && repeat.indexOf ('|') < 0) {
var parenCount = 0;
var n = repeat.length;
var ptColon = -1;
for (var i = 0; i < n; i++) {
switch (repeat.charAt (i)) {
case '[':
case '(':
parenCount++;
break;
case ')':
case ']':
parenCount--;
break;
case '.':
if (ptColon >= 0 && parenCount == 0) n = i;
break;
case ':':
if (ptColon < 0 && parenCount == 0) ptColon = i;
break;
}
}
if (ptColon > 0) repeat = repeat.substring (0, ptColon) + "(" + repeat.substring (ptColon, n) + ")" + repeat.substring (n);
}if (min == -2147483648) {
var ptOr = repeat.indexOf ("|");
if (ptOr >= 0) return this.parseVariableLength (pattern.substring (0, pt0) + "[$1" + pattern.substring (pt2, pt2 + ptOr + 1) + ")]" + pattern.substring (pt1) + "||" + pattern.substring (0, pt0) + "[$1(" + pattern.substring (pt2 + ptOr + 2) + pattern.substring (pt1));
continue;
}if (max == -2147483648) max = min;
if (repeat.indexOf ("|") >= 0) repeat = "[$(" + repeat + ")]";
for (var i = min; i <= max; i++) {
var sb =  new JU.SB ();
sb.append ("||").append (pattern.substring (0, pt0));
for (var j = 0; j < i; j++) sb.append (repeat);

sb.append (pattern.substring (pt1));
sout.appendSB (sb);
}
}
if (!isOK) throw  new JS.InvalidSmilesException ("bad variable expression: " + bracketed);
}return (haveInternalOr ? this.parseVariableLength (sout.substring (2)) : sout.length () < 2 ? pattern : sout.substring (2));
}, "~S");
Clazz.defineMethod (c$, "getSearch", 
function (parent, pattern, flags) {
this.htMeasures =  new java.util.Hashtable ();
var molecule =  new JS.SmilesSearch ();
molecule.setTop (parent);
molecule.isSmarts = this.isSmarts;
molecule.pattern = pattern;
molecule.flags = flags;
if (pattern.indexOf ("$(") >= 0) pattern = this.parseNested (molecule, pattern, "");
this.parseSmiles (molecule, pattern, null, false);
if (this.braceCount != 0) throw  new JS.InvalidSmilesException ("unmatched '{'");
if (!this.ringBonds.isEmpty ()) throw  new JS.InvalidSmilesException ("Open ring");
molecule.setAtomArray ();
for (var i = molecule.ac; --i >= 0; ) {
var atom = molecule.patternAtoms[i];
atom.setBondArray ();
if (!this.isSmarts && atom.bioType == '\0' && !atom.setHydrogenCount (molecule)) throw  new JS.InvalidSmilesException ("unbracketed atoms must be one of: B, C, N, O, P, S, F, Cl, Br, I,");
}
if (this.isSmarts) for (var i = molecule.ac; --i >= 0; ) {
var atom = molecule.patternAtoms[i];
this.checkNested (molecule, atom, flags);
for (var k = 0; k < atom.nAtomsOr; k++) this.checkNested (molecule, atom.atomsOr[k], flags);

for (var k = 0; k < atom.nPrimitives; k++) this.checkNested (molecule, atom.primitives[k], flags);

}
if (!this.isSmarts && !this.isBioSequence) molecule.elementCounts[1] = molecule.getMissingHydrogenCount ();
this.fixChirality (molecule);
return molecule;
}, "JS.SmilesSearch,~S,~N");
Clazz.defineMethod (c$, "checkNested", 
 function (molecule, atom, flags) {
if (atom.iNested > 0) {
var o = molecule.getNested (atom.iNested);
if (Clazz.instanceOf (o, String)) {
var s = o;
if (s.startsWith ("select")) return;
if (s.charAt (0) != '~' && atom.bioType != '\0') s = "~" + atom.bioType + "~" + s;
var search = this.getSearch (molecule, s, flags);
if (search.ac > 0 && search.patternAtoms[0].selected) atom.selected = true;
molecule.setNested (atom.iNested, search);
}}}, "JS.SmilesSearch,JS.SmilesAtom,~N");
Clazz.defineMethod (c$, "fixChirality", 
 function (molecule) {
for (var i = molecule.ac; --i >= 0; ) {
var sAtom = molecule.patternAtoms[i];
var stereoClass = sAtom.getChiralClass ();
if (stereoClass == -2147483648) continue;
var nBonds = sAtom.missingHydrogenCount;
if (nBonds < 0) nBonds = 0;
nBonds += sAtom.getBondCount ();
switch (stereoClass) {
case 0:
switch (nBonds) {
case 2:
stereoClass = (sAtom.getValence () == 3 ? 3 : 2);
break;
case 3:
case 4:
case 5:
case 6:
stereoClass = nBonds;
break;
}
break;
case 8:
if (nBonds != 4) stereoClass = 0;
break;
case 2:
case 6:
case 4:
case 5:
if (nBonds != stereoClass) stereoClass = 0;
break;
}
if (stereoClass == 0) throw  new JS.InvalidSmilesException ("Incorrect number of bonds for stereochemistry descriptor");
sAtom.setChiralClass (stereoClass);
}
}, "JS.SmilesSearch");
Clazz.defineMethod (c$, "parseSmiles", 
 function (molecule, pattern, currentAtom, isBranchAtom) {
var ret =  Clazz.newIntArray (1, 0);
var pt = 0;
var ch;
var bond = null;
while (pattern != null && pattern.length != 0) {
var index = 0;
if (currentAtom == null || bond != null && bond.order == 0) {
if (this.isBioSequence) molecule.top.needAromatic = false;
index = this.checkBioType (pattern, 0);
}ch = JS.SmilesParser.getChar (pattern, index);
var haveOpen = this.checkBrace (molecule, ch, '{');
if (haveOpen) ch = JS.SmilesParser.getChar (pattern, ++index);
if (ch == '(') {
var isMeasure = (JS.SmilesParser.getChar (pattern, index + 1) == '.');
if (currentAtom == null) throw  new JS.InvalidSmilesException ("No previous atom for " + (isMeasure ? "measure" : "branch"));
var subString = JS.SmilesParser.getSubPattern (pattern, index, '(');
if (subString.startsWith (".")) {
this.parseMeasure (molecule, subString.substring (1), currentAtom);
} else if (subString.length == 0 && this.isBioSequence) {
currentAtom.notCrossLinked = true;
} else {
this.branchLevel++;
this.parseSmiles (molecule, subString, currentAtom, true);
this.branchLevel--;
}index = subString.length + 2;
ch = JS.SmilesParser.getChar (pattern, index);
if (ch == '}' && this.checkBrace (molecule, ch, '}')) index++;
} else {
pt = index;
while (JS.SmilesBond.isBondType (ch, this.isSmarts, this.isBioSequence)) ch = JS.SmilesParser.getChar (pattern, ++index);

bond = this.parseBond (molecule, null, pattern.substring (pt, index), null, currentAtom, false, isBranchAtom);
if (haveOpen && bond.order != -1) index = pt;
ch = JS.SmilesParser.getChar (pattern, index);
if (this.checkBrace (molecule, ch, '{')) ch = JS.SmilesParser.getChar (pattern, ++index);
if (ch == '~' && bond.order == 0) {
index = this.checkBioType (pattern, index);
ch = JS.SmilesParser.getChar (pattern, index);
}if (ch == '\0' && bond.order == 0) return;
var isRing = (Character.isDigit (ch) || ch == '%');
var isAtom = (!isRing && (ch == '_' || ch == '[' || ch == '*' || Character.isLetter (ch)));
if (isRing) {
var ringNumber;
switch (ch) {
case '%':
if (JS.SmilesParser.getChar (pattern, index + 1) == '(') {
var subPattern = JS.SmilesParser.getSubPattern (pattern, index + 1, '(');
JS.SmilesParser.getDigits (subPattern, 0, ret);
index += subPattern.length + 3;
if (ret[0] < 0) throw  new JS.InvalidSmilesException ("Invalid ring designation: " + subPattern);
} else {
if (index + 3 <= pattern.length) index = JS.SmilesParser.getDigits (pattern.substring (0, index + 3), index + 1, ret);
if (ret[0] < 10) throw  new JS.InvalidSmilesException ("Two digits must follow the % sign");
}ringNumber = ret[0];
break;
default:
ringNumber = ch.charCodeAt (0) - 48;
index++;
}
this.parseRing (molecule, ringNumber, currentAtom, bond);
} else if (isAtom) {
switch (ch) {
case '[':
case '_':
var subPattern = JS.SmilesParser.getSubPattern (pattern, index, ch);
index += subPattern.length + (ch == '[' ? 2 : 0);
if (this.isBioSequence && ch == '[' && subPattern.indexOf (".") < 0 && subPattern.indexOf ("_") < 0) subPattern += ".0";
currentAtom = this.parseAtom (molecule, null, subPattern, currentAtom, bond, ch == '[', false, isBranchAtom);
if (bond.order != -1 && bond.order != 0) bond.set2a (null, currentAtom);
break;
default:
var ch2 = (!this.isBioSequence && Character.isUpperCase (ch) ? JS.SmilesParser.getChar (pattern, index + 1) : '\0');
if (ch != 'X' || ch2 != 'x') if (!Character.isLowerCase (ch2) || JU.Elements.elementNumberFromSymbol (pattern.substring (index, index + 2), true) == 0) ch2 = '\0';
if (ch2 != '\0' && "NA CA BA PA SC AC".indexOf (pattern.substring (index, index + 2)) >= 0) {
ch2 = '\0';
}var size = (Character.isUpperCase (ch) && Character.isLowerCase (ch2) ? 2 : 1);
currentAtom = this.parseAtom (molecule, null, pattern.substring (index, index + size), currentAtom, bond, false, false, isBranchAtom);
index += size;
}
} else {
throw  new JS.InvalidSmilesException ("Unexpected character: " + JS.SmilesParser.getChar (pattern, index));
}ch = JS.SmilesParser.getChar (pattern, index);
if (ch == '}' && this.checkBrace (molecule, ch, '}')) index++;
}pattern = pattern.substring (index);
isBranchAtom = false;
}
}, "JS.SmilesSearch,~S,JS.SmilesAtom,~B");
Clazz.defineMethod (c$, "checkBioType", 
 function (pattern, index) {
this.isBioSequence = (pattern.charAt (index) == '~');
if (this.isBioSequence) {
index++;
this.bioType = '*';
var ch = JS.SmilesParser.getChar (pattern, 2);
if (ch == '~' && ((ch = pattern.charAt (1)) == '*' || Character.isLowerCase (ch))) {
this.bioType = ch;
index = 3;
}}return index;
}, "~S,~N");
Clazz.defineMethod (c$, "parseMeasure", 
 function (molecule, strMeasure, currentAtom) {
var pt = strMeasure.indexOf (":");
var isNot = false;
var id = (pt < 0 ? strMeasure : strMeasure.substring (0, pt));
while (pt != 0) {
var len = id.length;
if (len == 1) id += "0";
var m = this.htMeasures.get (id);
if ((m == null) == (pt < 0)) break;
try {
if (pt > 0) {
var type = ("__dat".indexOf (id.charAt (0)));
if (type < 2) break;
var ret =  Clazz.newIntArray (1, 0);
var index = JS.SmilesParser.getDigits (id, 1, ret);
var pt2 = strMeasure.indexOf (",", pt);
if (pt2 < 0) pt2 = strMeasure.indexOf ("-", pt + 1);
if (pt2 < 0) break;
var s = strMeasure.substring (pt + 1, pt2);
if (s.startsWith ("!")) {
isNot = true;
s = s.substring (1);
}var min = (pt + 1 == pt2 ? 0 : JU.PT.fVal (s));
s = strMeasure.substring (pt2 + 1);
var max = (s.length == 0 ? 3.4028235E38 : JU.PT.fVal (s));
m =  new JS.SmilesMeasure (molecule, index, type, min, max, isNot);
molecule.measures.addLast (m);
if (index > 0) this.htMeasures.put (id, m);
 else if (index == 0 && JU.Logger.debugging) JU.Logger.debug ("measure created: " + m);
} else {
if (!m.addPoint (currentAtom.index)) break;
if (m.nPoints == m.type) {
this.htMeasures.remove (id);
if (JU.Logger.debugging) JU.Logger.debug ("measure created: " + m);
}return;
}if (!m.addPoint (currentAtom.index)) break;
} catch (e) {
if (Clazz.exceptionOf (e, NumberFormatException)) {
break;
} else {
throw e;
}
}
return;
}
throw  new JS.InvalidSmilesException ("invalid measure: " + strMeasure);
}, "JS.SmilesSearch,~S,JS.SmilesAtom");
Clazz.defineMethod (c$, "checkBrace", 
 function (molecule, ch, type) {
switch (ch) {
case '{':
if (ch != type) break;
this.braceCount++;
molecule.top.haveSelected = true;
return true;
case '}':
if (ch != type) break;
if (this.braceCount > 0) {
this.braceCount--;
return true;
}break;
default:
return false;
}
throw  new JS.InvalidSmilesException ("Unmatched '}'");
}, "JS.SmilesSearch,~S,~S");
Clazz.defineMethod (c$, "parseNested", 
 function (molecule, pattern, prefix) {
var index;
prefix = "$(" + prefix;
while ((index = pattern.lastIndexOf (prefix)) >= 0) {
var s = JS.SmilesParser.getSubPattern (pattern, index + 1, '(');
var pt = index + s.length + 3;
pattern = pattern.substring (0, index) + "_" + molecule.addNested (s) + "_" + pattern.substring (pt);
}
return pattern;
}, "JS.SmilesSearch,~S,~S");
Clazz.defineMethod (c$, "parseVariables", 
 function (pattern) {
var keys =  new JU.Lst ();
var values =  new JU.Lst ();
var index;
var ipt = 0;
var iptLast = -1;
while ((index = pattern.indexOf ("$", ipt)) >= 0) {
if (JS.SmilesParser.getChar (pattern, ipt + 1) == '(') break;
ipt = JS.SmilesParser.skipTo (pattern, index, '=');
if (ipt <= index + 1 || JS.SmilesParser.getChar (pattern, ipt + 1) != '\"') break;
var key = pattern.substring (index, ipt);
if (key.lastIndexOf ('$') > 0 || key.indexOf (']') > 0) throw  new JS.InvalidSmilesException ("Invalid variable name: " + key);
var s = JS.SmilesParser.getSubPattern (pattern, ipt + 1, '\"');
keys.addLast ("[" + key + "]");
values.addLast (s);
ipt += s.length + 2;
ipt = JS.SmilesParser.skipTo (pattern, ipt, ';');
iptLast = ++ipt;
}
if (iptLast < 0) return pattern;
return JU.Txt.replaceStrings (pattern.substring (iptLast), keys, values);
}, "~S");
Clazz.defineMethod (c$, "parseAtom", 
 function (molecule, atomSet, pattern, currentAtom, bond, isBracketed, isPrimitive, isBranchAtom) {
if (pattern == null || pattern.length == 0) throw  new JS.InvalidSmilesException ("Empty atom definition");
var newAtom = (atomSet == null ? molecule.addAtom () : isPrimitive ? atomSet.addPrimitive () : atomSet.addAtomOr ());
if (this.braceCount > 0) newAtom.selected = true;
if (!this.checkLogic (molecule, pattern, newAtom, null, currentAtom, isPrimitive, isBranchAtom)) {
var ret =  Clazz.newIntArray (1, 0);
if (this.isBioSequence && pattern.length == 1) pattern += ".0";
var ch = pattern.charAt (0);
var index = 0;
var isNot = false;
if (this.isSmarts && ch == '!') {
ch = JS.SmilesParser.getChar (pattern, ++index);
if (ch == '\0') throw  new JS.InvalidSmilesException ("invalid '!'");
newAtom.not = isNot = true;
}var hydrogenCount = -2147483648;
var biopt = pattern.indexOf ('.');
if (biopt >= 0) {
var name = pattern.substring (index, biopt);
if (name.length == 0) name = "*";
if (name.length > 1) newAtom.residueName = name.toUpperCase ();
 else if (!name.equals ("*")) newAtom.residueChar = name;
name = pattern.substring (biopt + 1).toUpperCase ();
if ((biopt = name.indexOf ("#")) >= 0) {
JS.SmilesParser.getDigits (name, biopt + 1, ret);
newAtom.elementNumber = ret[0];
name = name.substring (0, biopt);
}if (name.length == 0) name = "*";
if (!name.equals ("*")) newAtom.setAtomName (name);
ch = '\0';
}newAtom.setBioAtom (this.bioType);
while (ch != '\0') {
newAtom.setAtomName (this.isBioSequence ? "0" : "");
if (Character.isDigit (ch)) {
index = JS.SmilesParser.getDigits (pattern, index, ret);
var mass = ret[0];
if (mass == -2147483648) throw  new JS.InvalidSmilesException ("Non numeric atomic mass");
if (JS.SmilesParser.getChar (pattern, index) == '?') {
index++;
mass = -mass;
}newAtom.setAtomicMass (mass);
} else {
switch (ch) {
case '"':
var type = JU.PT.getQuotedStringAt (pattern, index);
index += type.length + 2;
newAtom.setAtomType (type);
break;
case '_':
index = JS.SmilesParser.getDigits (pattern, index + 1, ret) + 1;
if (ret[0] == -2147483648) throw  new JS.InvalidSmilesException ("Invalid SEARCH primitive: " + pattern.substring (index));
newAtom.iNested = ret[0];
if (this.isBioSequence && isBracketed) {
if (index != pattern.length) throw  new JS.InvalidSmilesException ("invalid characters: " + pattern.substring (index));
}break;
case '=':
index = JS.SmilesParser.getDigits (pattern, index + 1, ret);
newAtom.jmolIndex = ret[0];
break;
case '#':
index = JS.SmilesParser.getDigits (pattern, index + 1, ret);
newAtom.elementNumber = ret[0];
break;
case '-':
case '+':
index = this.checkCharge (pattern, index, newAtom);
break;
case '@':
molecule.haveAtomStereochemistry = true;
index = this.checkChirality (pattern, index, molecule.patternAtoms[newAtom.index]);
break;
default:
var nextChar = JS.SmilesParser.getChar (pattern, index + 1);
var sym2 = pattern.substring (index + 1, index + (Character.isLowerCase (nextChar) && (!isBracketed || !Character.isDigit (JS.SmilesParser.getChar (pattern, index + 2))) ? 2 : 1));
var symbol = Character.toUpperCase (ch) + sym2;
var mustBeSymbol = true;
var checkForPrimitive = (isBracketed && Character.isLetter (ch));
if (checkForPrimitive) {
if (!isNot && (isPrimitive ? atomSet : newAtom).hasSymbol) {
mustBeSymbol = false;
} else if (ch == 'H') {
mustBeSymbol = !Character.isDigit (nextChar) || JS.SmilesParser.getChar (pattern, index + 2) == '?';
} else if ("DdhRrvXx".indexOf (ch) >= 0 && Character.isDigit (nextChar)) {
mustBeSymbol = false;
} else if (!symbol.equals ("A") && !symbol.equals ("Xx")) {
mustBeSymbol = (JU.Elements.elementNumberFromSymbol (symbol, true) > 0);
if (!mustBeSymbol && sym2 !== "") {
sym2 = "";
symbol = symbol.substring (0, 1);
mustBeSymbol = (JU.Elements.elementNumberFromSymbol (symbol, true) > 0);
}}}if (mustBeSymbol) {
if (!isBracketed && !this.isSmarts && !this.isBioSequence && !JS.SmilesAtom.allowSmilesUnbracketed (symbol) || !newAtom.setSymbol (symbol = ch + sym2)) throw  new JS.InvalidSmilesException ("Invalid atom symbol: " + symbol);
if (isPrimitive) atomSet.hasSymbol = true;
index += symbol.length;
} else {
index = JS.SmilesParser.getDigits (pattern, index + 1, ret);
var val = ret[0];
switch (ch) {
default:
throw  new JS.InvalidSmilesException ("Invalid SEARCH primitive: " + pattern.substring (index));
case 'D':
newAtom.setDegree (val == -2147483648 ? 1 : val);
break;
case 'd':
newAtom.setNonhydrogenDegree (val == -2147483648 ? 1 : val);
break;
case 'H':
hydrogenCount = (val == -2147483648 ? 1 : val);
break;
case 'h':
newAtom.setImplicitHydrogenCount (val == -2147483648 ? -1 : val);
break;
case 'R':
if (val == -2147483648) val = -1;
newAtom.setRingMembership (val);
molecule.top.needRingData = true;
break;
case 'r':
if (val == -2147483648) {
val = -1;
newAtom.setRingMembership (val);
} else {
newAtom.setRingSize (val);
switch (val) {
case 500:
val = 5;
break;
case 600:
val = 6;
break;
}
if (val > molecule.ringDataMax) molecule.ringDataMax = val;
}molecule.top.needRingData = true;
break;
case 'v':
newAtom.setValence (val == -2147483648 ? 1 : val);
break;
case 'X':
newAtom.setConnectivity (val == -2147483648 ? 1 : val);
break;
case 'x':
newAtom.setRingConnectivity (val == -2147483648 ? -1 : val);
molecule.top.needRingData = true;
break;
}
}}
}ch = JS.SmilesParser.getChar (pattern, index);
if (isNot && ch != '\0') throw  new JS.InvalidSmilesException ("'!' may only involve one primitive.");
}
if (hydrogenCount == -2147483648 && isBracketed) hydrogenCount = -2147483647;
newAtom.setExplicitHydrogenCount (hydrogenCount);
molecule.patternAtoms[newAtom.index].setExplicitHydrogenCount (hydrogenCount);
}if (currentAtom != null && bond.order == 0) {
newAtom.notBondedIndex = currentAtom.index;
}if (currentAtom != null && bond.order != 0) {
if (bond.order == -1) bond.order = (this.isBioSequence && isBranchAtom ? 112 : this.isSmarts || currentAtom.isAromatic () && newAtom.isAromatic () ? 81 : 1);
if (!isBracketed) bond.set2a (null, newAtom);
if (this.branchLevel == 0 && (bond.order == 17 || bond.order == 112)) this.branchLevel++;
}if (this.branchLevel == 0) molecule.lastChainAtom = newAtom;
return newAtom;
}, "JS.SmilesSearch,JS.SmilesAtom,~S,JS.SmilesAtom,JS.SmilesBond,~B,~B,~B");
Clazz.defineMethod (c$, "parseRing", 
 function (molecule, ringNum, currentAtom, bond) {
var r = Integer.$valueOf (ringNum);
var bond0 = this.ringBonds.get (r);
if (bond0 == null) {
this.ringBonds.put (r, bond);
return;
}this.ringBonds.remove (r);
switch (bond.order) {
case -1:
bond.order = (bond0.order != -1 ? bond0.order : this.isSmarts || currentAtom.isAromatic () && bond0.getAtom1 ().isAromatic () ? 81 : 1);
break;
case 257:
bond.order = 513;
break;
case 513:
bond.order = 257;
break;
}
if (bond0.order != -1 && bond0.order != bond.order) throw  new JS.InvalidSmilesException ("Incoherent bond type for ring");
bond0.set (bond);
currentAtom.bondCount--;
bond0.setAtom2 (currentAtom);
}, "JS.SmilesSearch,~N,JS.SmilesAtom,JS.SmilesBond");
Clazz.defineMethod (c$, "checkCharge", 
 function (pattern, index, newAtom) {
var len = pattern.length;
var ch = pattern.charAt (index);
var count = 1;
++index;
if (index < len) {
var nextChar = pattern.charAt (index);
if (Character.isDigit (nextChar)) {
var ret =  Clazz.newIntArray (1, 0);
index = JS.SmilesParser.getDigits (pattern, index, ret);
count = ret[0];
if (count == -2147483648) throw  new JS.InvalidSmilesException ("Non numeric charge");
} else {
while (index < len && pattern.charAt (index) == ch) {
index++;
count++;
}
}}newAtom.setCharge (ch == '+' ? count : -count);
return index;
}, "~S,~N,JS.SmilesAtom");
Clazz.defineMethod (c$, "checkChirality", 
 function (pattern, index, newAtom) {
var stereoClass = 0;
var order = -2147483648;
var len = pattern.length;
var ch;
stereoClass = 0;
order = 1;
if (++index < len) {
switch (ch = pattern.charAt (index)) {
case '@':
order = 2;
index++;
break;
case 'H':
break;
case 'A':
case 'D':
case 'E':
case 'O':
case 'S':
case 'T':
stereoClass = (index + 1 < len ? JS.SmilesAtom.getChiralityClass (pattern.substring (index, index + 2)) : -1);
index += 2;
break;
default:
order = (Character.isDigit (ch) ? 1 : -1);
}
var pt = index;
if (order == 1) {
while (pt < len && Character.isDigit (pattern.charAt (pt))) pt++;

if (pt > index) {
try {
order = Integer.parseInt (pattern.substring (index, pt));
} catch (e) {
if (Clazz.exceptionOf (e, NumberFormatException)) {
order = -1;
} else {
throw e;
}
}
index = pt;
}}if (order < 1 || stereoClass < 0) throw  new JS.InvalidSmilesException ("Invalid stereochemistry descriptor");
}newAtom.setChiralClass (stereoClass);
newAtom.setChiralOrder (order);
if (JS.SmilesParser.getChar (pattern, index) == '?') {
JU.Logger.info ("Ignoring '?' in stereochemistry");
index++;
}return index;
}, "~S,~N,JS.SmilesAtom");
Clazz.defineMethod (c$, "parseBond", 
 function (molecule, bondSet, pattern, bond, currentAtom, isPrimitive, isBranchAtom) {
var ch = JS.SmilesParser.getChar (pattern, 0);
if (ch == '.') {
if (bond != null || bondSet != null) throw  new JS.InvalidSmilesException ("invalid '.'");
this.isBioSequence = (JS.SmilesParser.getChar (pattern, 1) == '~');
return  new JS.SmilesBond (null, null, 0, false);
}if (ch == '+' && bondSet != null) throw  new JS.InvalidSmilesException ("invalid '+'");
var newBond = (bondSet == null ? (bond == null ?  new JS.SmilesBond (currentAtom, null, (this.isBioSequence && currentAtom != null ? (isBranchAtom ? 112 : 96) : -1), false) : bond) : isPrimitive ? bondSet.addPrimitive () : bondSet.addBondOr ());
if (ch != '\0' && !this.checkLogic (molecule, pattern, null, newBond, currentAtom, isPrimitive, false)) {
var isBondNot = (ch == '!');
if (isBondNot) {
ch = JS.SmilesParser.getChar (pattern, 1);
if (ch == '\0' || ch == '!') throw  new JS.InvalidSmilesException ("invalid '!'");
}var bondType = JS.SmilesBond.getBondTypeFromCode (ch);
if (bondType == 65) molecule.top.needRingMemberships = true;
if (currentAtom == null && bondType != 0) throw  new JS.InvalidSmilesException ("Bond without a previous atom");
switch (bondType) {
case 769:
case 1025:
if (isBondNot) {
isBondNot = false;
bondType = (bondType == 769 ? 1025 : 769);
}molecule.haveBondStereochemistry = true;
break;
case 257:
case 513:
molecule.haveBondStereochemistry = true;
break;
case 17:
break;
case 2:
case 1:
if (currentAtom.isAromatic ()) molecule.top.needRingData = true;
break;
}
newBond.set2 (bondType, isBondNot);
if (this.isBioSequence && bondSet != null) bondSet.set2 (bondType, isBondNot);
}return newBond;
}, "JS.SmilesSearch,JS.SmilesBond,~S,JS.SmilesBond,JS.SmilesAtom,~B,~B");
Clazz.defineMethod (c$, "checkLogic", 
 function (molecule, pattern, atom, bond, currentAtom, isPrimitive, isBranchAtom) {
var pt = pattern.indexOf (',');
var len = pattern.length;
while (true) {
var haveOr = (pt > 0);
if (haveOr && !this.isSmarts || pt == 0) break;
var props = "";
pt = pattern.indexOf (';');
if (pt >= 0) {
if (!this.isSmarts || pt == 0) break;
props = "&" + pattern.substring (pt + 1);
pattern = pattern.substring (0, pt);
if (!haveOr) {
pattern += props;
props = "";
}}var index = 0;
if (haveOr) {
pattern += ",";
while ((pt = pattern.indexOf (',', index)) > 0 && pt <= len) {
var s = pattern.substring (index, pt) + props;
if (s.length == 0) throw  new JS.InvalidSmilesException ("missing " + (bond == null ? "atom" : "bond") + " token");
if (bond == null) this.parseAtom (molecule, atom, s, null, null, true, false, isBranchAtom);
 else this.parseBond (molecule, bond, s, null, currentAtom, false, false);
index = pt + 1;
}
} else if ((pt = pattern.indexOf ('&')) >= 0 || bond != null && len > 1 && !isPrimitive) {
if (!this.isSmarts || pt == 0) break;
if (bond != null && pt < 0) {
if (len > 1) {
var sNew =  new JU.SB ();
for (var i = 0; i < len; ) {
var ch = pattern.charAt (i++);
sNew.appendC (ch);
if (ch != '!' && i < len) sNew.appendC ('&');
}
pattern = sNew.toString ();
len = pattern.length;
}}pattern += "&";
while ((pt = pattern.indexOf ('&', index)) > 0 && pt <= len) {
var s = pattern.substring (index, pt) + props;
if (bond == null) this.parseAtom (molecule, atom, s, null, null, true, true, isBranchAtom);
 else this.parseBond (molecule, bond, s, null, currentAtom, true, false);
index = pt + 1;
}
} else {
return false;
}return true;
}
var ch = pattern.charAt (pt);
throw  new JS.InvalidSmilesException ((this.isSmarts ? "invalid placement for '" + ch + "'" : "[" + ch + "] notation only valid with SMARTS, not SMILES,") + " in " + pattern);
}, "JS.SmilesSearch,~S,JS.SmilesAtom,JS.SmilesBond,JS.SmilesAtom,~B,~B");
c$.getSubPattern = Clazz.defineMethod (c$, "getSubPattern", 
 function (pattern, index, ch) {
var ch2;
var margin = 1;
switch (ch) {
case '[':
ch2 = ']';
break;
case '"':
case '%':
ch2 = ch;
break;
case '(':
ch2 = ')';
break;
default:
ch2 = ch;
margin = 0;
}
var len = pattern.length;
var pCount = 1;
for (var pt = index + 1; pt < len; pt++) {
var ch1 = pattern.charAt (pt);
if (ch1 == ch2) {
pCount--;
if (pCount == 0) return pattern.substring (index + margin, pt + 1 - margin);
} else if (ch1 == ch) {
pCount++;
}}
throw  new JS.InvalidSmilesException ("Unmatched " + ch);
}, "~S,~N,~S");
c$.getChar = Clazz.defineMethod (c$, "getChar", 
 function (pattern, i) {
return (i < pattern.length ? pattern.charAt (i) : '\0');
}, "~S,~N");
c$.getDigits = Clazz.defineMethod (c$, "getDigits", 
 function (pattern, index, ret) {
var pt = index;
var len = pattern.length;
while (pt < len && Character.isDigit (pattern.charAt (pt))) pt++;

try {
ret[0] = Integer.parseInt (pattern.substring (index, pt));
} catch (e) {
if (Clazz.exceptionOf (e, NumberFormatException)) {
ret[0] = -2147483648;
} else {
throw e;
}
}
return pt;
}, "~S,~N,~A");
c$.skipTo = Clazz.defineMethod (c$, "skipTo", 
 function (pattern, index, ch0) {
var pt = index;
var ch;
while ((ch = JS.SmilesParser.getChar (pattern, ++pt)) != ch0 && ch != '\0') {
}
return (ch == '\0' ? -1 : pt);
}, "~S,~N,~S");
c$.getRingPointer = Clazz.defineMethod (c$, "getRingPointer", 
function (i) {
return (i < 10 ? "" + i : i < 100 ? "%" + i : "%(" + i + ")");
}, "~N");
c$.cleanPattern = Clazz.defineMethod (c$, "cleanPattern", 
function (pattern) {
pattern = JU.PT.replaceAllCharacters (pattern, " \t\n\r", "");
pattern = JU.PT.rep (pattern, "^^", "'");
var i = 0;
var i2 = 0;
while ((i = pattern.indexOf ("//*")) >= 0 && (i2 = pattern.indexOf ("*//")) >= i) pattern = pattern.substring (0, i) + pattern.substring (i2 + 3);

pattern = JU.PT.rep (pattern, "//", "");
return pattern;
}, "~S");
});
