Clazz.declarePackage ("J.dssx");
Clazz.load (["J.api.JmolAnnotationParser"], "J.dssx.AnnotationParser", ["java.lang.Boolean", "$.Character", "$.Float", "java.util.Hashtable", "JU.AU", "$.BS", "$.Lst", "$.P3", "$.PT", "$.Rdr", "$.SB", "JM.HBond", "JM.BasePair", "$.NucleicPolymer", "JS.SV", "JU.BSUtil", "$.Logger", "JV.JC"], function () {
c$ = Clazz.decorateAsClass (function () {
this.reader = null;
this.line = null;
this.dssr = null;
this.htTemp = null;
this.message = null;
this.htPar = null;
this.basePairs = null;
this.next = null;
Clazz.instantialize (this, arguments);
}, J.dssx, "AnnotationParser", null, J.api.JmolAnnotationParser);
Clazz.prepareFields (c$, function () {
this.next =  Clazz.newIntArray (1, 0);
});
Clazz.makeConstructor (c$, 
function () {
});
Clazz.overrideMethod (c$, "processDSSR", 
function (info, reader, line0, htGroup1) {
info.put ("dssr", this.dssr =  new java.util.Hashtable ());
this.htTemp =  new java.util.Hashtable ();
this.htPar =  new java.util.Hashtable ();
this.htPar.put ("bp", ["bpShear", "bpStretch", "bpStagger", "bpPropeller", "bpBuckle", "bpOpening"]);
this.htPar.put ("bpChiLambda", ["bpChi1", "bpLambda1", "bpChi2", "bpLambda2"]);
this.htPar.put ("bpDistTor", ["bpDistC1C1", "bpDistNN", "bpDistC6C8", "bpTorCNNC"]);
this.htPar.put ("step", ["stShift", "stSlide", "stRise", "stTilt", "stRoll", "stTwist"]);
this.htPar.put ("hel", ["heXDisp", "heYDisp", "heRise", "heIncl", "heTip", "heTwist"]);
this.reader = reader;
this.message =  new JU.SB ();
this.line = (line0 == null ? "" : line0.trim ());
this.skipTo ("DSSR:", false);
this.addToMessages (null);
var haveHeader = false;
while (this.rd () != null) {
if (this.line.startsWith ("List of")) {
var n = JU.PT.parseInt (this.line.substring (8));
if (n < 0 || this.line.endsWith ("files")) continue;
this.addMessage (this.line);
this.line = JU.PT.rep (JU.PT.trim (this.line, "s"), " interaction", "");
var pt = "pair elix lice plet stem tack loop ulge tion ment otif pper turn bond file".indexOf (this.line.trim ().substring (this.line.length - 4));
switch (pt) {
case 0:
this.readPairs (n);
break;
case 5:
case 10:
this.getHelixOrStem (n, "helices", "helix", true);
break;
case 15:
this.readNTList (null, "multiplets", n);
break;
case 20:
this.getHelixOrStem (n, "stems", "stem", false);
break;
case 25:
this.readStacks (n);
break;
case 30:
this.readLoops (n);
break;
case 35:
this.readBulges (n);
break;
case 40:
this.readJunctions (n);
break;
case 45:
this.readNTList (null, "singleStranded", n);
break;
case 50:
this.readMotifs (n);
break;
case 55:
this.readNTList (null, "riboseZippers", n);
break;
case 60:
this.readTurns (n);
break;
case 65:
this.readHBonds (n);
break;
case 70:
break;
default:
this.addMessage ("DSSRParser ignored: " + this.line);
break;
}
} else if (!haveHeader && this.line.startsWith ("Date and time")) {
haveHeader = true;
this.addToMessages ("");
} else if (this.line.startsWith ("Secondary structures in dot-bracket")) {
this.readStructure ();
} else if (this.line.startsWith ("Mapping of")) {
this.mapGroups (htGroup1);
}}
this.dssr.put ("summary", this.message.toString ());
return this.message.toString ();
}, "java.util.Map,javajs.api.GenericLineReader,~S,java.util.Map");
Clazz.defineMethod (c$, "mapGroups", 
 function (map) {
this.rd ();
var n = 0;
var s = "";
if (JM.NucleicPolymer.htGroup1 == null) JM.NucleicPolymer.htGroup1 =  new java.util.Hashtable ();
while (this.rd () != null && this.line.length > 21) {
var g3 = this.line.substring (9, 12).trim ();
var g1 = this.line.substring (21, 22);
if ("ACGTU".indexOf (g1) < 0) {
JM.NucleicPolymer.htGroup1.put (g3, g1);
var key = " " + g3 + "(" + g1 + ")";
if (s.indexOf (key) < 0) {
n++;
s += key;
}if (map != null) map.put (g3, g1);
}}
if (n > 0) this.addMessage (n + " nonstandard base" + (n > 1 ? "s" : "") + ":" + s);
}, "java.util.Map");
Clazz.defineMethod (c$, "readStructure", 
 function () {
this.addMessage ("");
this.addMessage (this.line);
this.addMessage (this.rd ());
this.dssr.put ("seq", this.rd ());
this.addMessage (this.line);
this.dssr.put ("dbn", this.rd ());
this.addToMessages (this.line);
this.addMessage ("");
});
Clazz.defineMethod (c$, "readHBonds", 
 function (n) {
var list = this.newList ("hBonds");
for (var i = 0; i < n; i++) {
var data =  new java.util.Hashtable ();
var tokens = JU.PT.getTokens (this.rd ());
data.put ("atno1", Integer.$valueOf (JU.PT.parseInt (tokens[0])));
data.put ("atno2", Integer.$valueOf (JU.PT.parseInt (tokens[1])));
data.put ("id", tokens[2]);
data.put ("hbType", tokens[3]);
data.put ("distAng", Float.$valueOf (tokens[4]));
var pt = (tokens.length > 8 ? 6 : 5);
data.put ("energy", Float.$valueOf (pt == 6 ? tokens[5] : "0"));
data.put ("label", tokens[pt++]);
data.put ("atom1", this.fix (tokens[pt++], true));
data.put ("atom2", this.fix (tokens[pt++], true));
if (pt < tokens.length) data.put ("primary", Boolean.$valueOf (tokens[pt++].equals ("primary")));
list.addLast (data);
}
}, "~N");
Clazz.defineMethod (c$, "addToMessages", 
 function (s) {
if (s != null) this.addMessage (s);
while (this.line != null && this.line.length > 0 && this.line.indexOf ("****") < 0) {
this.addMessage (s == null ? this.line.trim () : this.line);
this.rd ();
}
}, "~S");
Clazz.defineMethod (c$, "addMessage", 
 function (s) {
this.message.append (s).append ("\n");
}, "~S");
Clazz.defineMethod (c$, "newList", 
 function (name) {
var list =  new JU.Lst ();
if (name != null) this.dssr.put (name, list);
return list;
}, "~S");
Clazz.defineMethod (c$, "readStacks", 
 function (n) {
var list = this.newList ("coaxialStacks");
for (var i = 0; i < n; i++) {
var data =  new java.util.Hashtable ();
var tokens = JU.PT.getTokens (this.rd ());
data.put ("helix", tokens[1]);
data.put ("stemCount", Integer.$valueOf (tokens[3]));
data.put ("stems", tokens[5]);
data.put ("basePairs", this.getLinkNTList (tokens[5], "stem", null));
list.addLast (data);
}
}, "~N");
Clazz.defineMethod (c$, "readInfo", 
 function (key, n) {
var list =  new JU.Lst ();
if (key != null) this.dssr.put (key, list);
for (var i = 0; i < n; i++) list.addLast (this.rd ());

return list;
}, "~S,~N");
Clazz.defineMethod (c$, "readLoops", 
 function (n) {
if (this.line.indexOf ("internal") >= 0) {
this.readSets ("internalLoops", n, 2, 4);
} else if (this.line.indexOf ("hairpin") >= 0) {
this.readSets ("hairpinLoops", n, 1, 3);
} else if (this.line.indexOf ("kissing") >= 0) {
this.readSets ("kissingLoops", n, -1, -1);
}}, "~N");
Clazz.defineMethod (c$, "readJunctions", 
 function (n) {
this.readSets ("junctions", n, 0, 3);
}, "~N");
Clazz.defineMethod (c$, "readBulges", 
 function (n) {
this.readSets ("bulges", n, 2, 2);
}, "~N");
Clazz.defineMethod (c$, "readSets", 
 function (key, n, nway, ptnts) {
var sets = this.newList (key);
var isJunction = (nway == 0);
var isKissingLoop = (ptnts == -1);
for (var i = 0; i < n; i++) {
var set =  new java.util.Hashtable ();
var tokens = JU.PT.getTokens (this.rd ());
set.put ("id", tokens[0]);
this.htTemp.put (key + tokens[0], set);
var lst =  new JU.Lst ();
set.put ("desc", this.line);
if (isKissingLoop) {
this.getNTs (this.getLinkNTList (tokens[2], "stem", null), lst, true, false);
this.getNTs (this.getLinkNTList (tokens[6], "hairpinLoops", null), lst, false, false);
this.getNTs (this.getLinkNTList (tokens[8], "hairpinLoops", null), lst, false, false);
set.put ("nts", lst);
lst =  new JU.Lst ();
this.getNTs (this.getLinkNTList (tokens[2], "stem", null), lst, true, true);
this.getNTs (this.getLinkNTList (tokens[6], "hairpinLoops", null), lst, false, true);
this.getNTs (this.getLinkNTList (tokens[8], "hairpinLoops", null), lst, false, true);
set.put ("resnos", lst);
} else {
set.put ("dssrType", tokens[1]);
if (isJunction) nway = JU.PT.parseInt (tokens[1].substring (0, tokens[1].indexOf ("-")));
set.put ("nway", Integer.$valueOf (nway));
set.put ("n", Integer.$valueOf (JU.PT.trim (tokens[ptnts], ";").substring (4)));
set.put ("linkedBy", this.getLinkNTList (tokens[ptnts + 4], "stem", lst));
set.put ("basePairs", this.readNTList (key + "#" + (i + 1), null, nway + 1));
}sets.addLast (set);
}
}, "~S,~N,~N,~N");
Clazz.defineMethod (c$, "getNTs", 
 function (linkNTList, lst, isStem, isResno) {
var o = linkNTList.get (0);
var n = o.size ();
var key = (!isResno ? "nt" : isStem ? "res" : "resno");
var nts = (isStem ?  new JU.Lst () : null);
for (var i = 0; i < n; i++) {
var m = o.get (i);
if (isStem) {
nts.addLast (m.get (key + "1"));
nts.addLast (m.get (key + "2"));
} else {
lst.addLast (m.get (key + "s"));
}}
if (isStem) {
lst.addLast (nts);
}}, "JU.Lst,JU.Lst,~B,~B");
Clazz.defineMethod (c$, "getLinkNTList", 
 function (linkStr, type, list) {
if (list == null) list =  new JU.Lst ();
var tokens = JU.PT.getTokens (JU.PT.replaceAllCharacters (linkStr, "[,]", " "));
for (var i = 0; i < tokens.length; i++) list.addLast (this.htTemp.get ((tokens[i].startsWith ("-") ? "" : type) + tokens[i]));

return list;
}, "~S,~S,JU.Lst");
Clazz.defineMethod (c$, "readMotifs", 
 function (n) {
var motifs = this.newList ("aMinorMotifs");
for (var i = 0; i < n; i++) {
var motif =  new java.util.Hashtable ();
var tokens = JU.PT.getTokens (this.rd ());
motif.put ("motiftype", this.after (tokens[1], "=") + " " + tokens[2]);
motif.put ("info", this.line);
motif.put ("data", this.readInfo (null, 2));
motifs.addLast (motif);
}
}, "~N");
Clazz.defineMethod (c$, "readTurns", 
 function (n) {
var turns = this.newList ("kinkTurns");
for (var i = 0; i < n; i++) {
var turn =  new java.util.Hashtable ();
var tokens = JU.PT.getTokens (this.rd ());
turn.put ("turnType", tokens[1]);
turn.put ("info", this.line);
turn.put ("details", this.rd ());
turn.put ("basePairs", this.readNTList (null, null, 2));
turns.addLast (turn);
}
}, "~N");
Clazz.defineMethod (c$, "readPairs", 
 function (n) {
var pairs;
if (this.line.indexOf ("lone ") >= 0) {
this.rd ();
this.skipHeader ();
pairs = this.newList ("lonePairs");
for (var i = 0; i < n; i++) {
var tokens = JU.PT.getTokens (this.line);
var data = this.htTemp.get (tokens[1] + tokens[2]);
this.htTemp.put ("#" + this.line.substring (0, 5).trim (), data);
data.put ("lonePair", Boolean.TRUE);
pairs.addLast (data);
this.rd ();
}
return;
}this.basePairs = this.newList ("basePairs");
this.skipHeader ();
for (var i = 0; i < n; i++) this.getBPData (0, null, true);

}, "~N");
Clazz.defineMethod (c$, "getBPData", 
 function (i0, type, readParams) {
var tokens = JU.PT.getTokens (this.line);
var nt12 = tokens[1] + tokens[2];
var data;
data = this.htTemp.get (nt12);
var i = i0;
if (data == null) {
data =  new java.util.Hashtable ();
i = JU.PT.parseInt (tokens[0]);
if (type != null) i = -((this.htTemp.get (tokens[2] + tokens[1])).get ("id")).intValue ();
data.put ("id", Integer.$valueOf (i));
var nt1 = this.fix (tokens[1], true);
var nt2 = this.fix (tokens[2], true);
data.put ("key", nt1 + " " + nt2);
data.put ("nt1", nt1);
data.put ("nt2", nt2);
data.put ("nt2", this.fix (tokens[2], true));
data.put ("res1", this.fix (tokens[1], false));
data.put ("res2", this.fix (tokens[2], false));
var bp = tokens[3];
data.put ("bp", bp);
data.put ("g1", bp.substring (0, 1));
data.put ("g2", bp.substring (2, 3));
var pt = (tokens.length == 8 ? 5 : 4);
data.put ("name", pt == 5 ? tokens[4] : "?");
var pt1 = tokens[pt].indexOf ("-");
data.put ("Saenger", Integer.$valueOf (pt1 > 0 ? tokens[pt].substring (0, pt1) : "0"));
data.put ("LW", tokens[++pt]);
data.put ("DSSR", tokens[++pt]);
this.htTemp.put (nt12, data);
this.basePairs.addLast (data);
}if (type != null) data.put (type + "Id", Integer.$valueOf (i0));
if (readParams) this.readMore (data, type == null, i < 0);
 else this.skipHeader ();
return data;
}, "~N,~S,~B");
Clazz.defineMethod (c$, "readMore", 
 function (data, isBP, isRev) {
var info = "";
while (this.isHeader (this.rd ())) {
var pt = this.line.indexOf ("[");
this.line = JU.PT.rep (this.line, "_pars", "-pars");
if (isBP) {
if (this.line.indexOf ("bp-pars:") >= 0) {
this.addArray (data, "bp", JU.PT.parseFloatArray (this.line.substring (pt + 1)));
} else if (this.line.indexOf ("lambda") >= 0) {
this.extractFloats (data, this.htPar.get ("bpChiLambda"));
} else if (this.line.indexOf ("tor(") >= 0) {
this.extractFloats (data, this.htPar.get ("bpDistTor"));
}info += this.line + "\n";
} else {
if (isRev && this.line.indexOf ("bp1-pars:") >= 0) {
this.addArray (data, "bp", JU.PT.parseFloatArray (this.line.substring (pt + 1)));
} else if (this.line.indexOf ("heli-pars:") >= 0) {
this.addArray (data, "hel", JU.PT.parseFloatArray (this.line.substring (pt + 1)));
} else if (this.line.indexOf ("step-pars:") >= 0) {
this.addArray (data, "step", JU.PT.parseFloatArray (this.line.substring (pt + 1)));
} else if ((pt = this.line.indexOf ("h-rise=")) >= 0) {
this.addFloat (data, "heRiseC1", pt + 7);
this.addFloat (data, "heTwistC1", this.line.indexOf ("h-twist=") + 8);
} else if ((pt = this.line.indexOf ("rise=")) >= 0) {
this.addFloat (data, "stRiseC1", pt + 5);
this.addFloat (data, "stTwistC1", this.line.indexOf ("twist=") + 6);
}}}
if (isBP) data.put ("info", info);
}, "java.util.Map,~B,~B");
Clazz.defineMethod (c$, "extractFloats", 
 function (data, names) {
this.line = this.line.$replace ('[', '=').$replace ('(', ' ').$replace (']', ' ');
this.next[0] = -1;
var n = names.length;
for (var i = 0, pt = 0; i < n; i++) {
if ((this.next[0] = pt = this.line.indexOf ("=", pt) + 1) == 0) break;
data.put (names[i], Float.$valueOf (JU.PT.parseFloatNext (this.line, this.next)));
}
}, "java.util.Map,~A");
Clazz.defineMethod (c$, "addArray", 
 function (data, key, f) {
var keys = this.htPar.get (key);
var n = Math.min (f.length, keys == null ? f.length : keys.length);
for (var i = 0; i < n; i++) data.put (keys == null ? key + (i + 1) : keys[i], Float.$valueOf (f[i]));

}, "java.util.Map,~S,~A");
Clazz.defineMethod (c$, "addFloat", 
 function (data, key, pt) {
data.put (key, Float.$valueOf (JU.PT.parseFloat (this.line.substring (pt, Math.min (this.line.length, pt + 10)))));
}, "java.util.Map,~S,~N");
Clazz.defineMethod (c$, "readNTList", 
 function (ntsKey, type, n) {
var isHairpin = (n == 2);
var list = this.newList (type);
if (ntsKey != null) this.htTemp.put (ntsKey, list);
if (isHairpin) this.rd ();
for (var i = (isHairpin ? 1 : 0); i < n; i++) list.addLast (this.getNTList ());

return list;
}, "~S,~S,~N");
Clazz.defineMethod (c$, "getHelixOrStem", 
 function (n, key, type, isHelix) {
var list = this.newList (key);
for (var i = 0; i < n; i++) {
this.skipTo ("  " + type + "#", true);
var bps = JU.PT.parseInt (this.after (this.line, "="));
var data =  new java.util.Hashtable ();
var header = this.getHeader ();
data.put ("info", header);
data.put ("bpCount", Integer.$valueOf (bps));
if (isHelix) {
var lines = JU.PT.split (header, "\n");
if (lines.length == 8) {
data.put ("helicalAxisData", this.after (lines[5], "s"));
data.put ("p1", this.getPoint (lines[6]));
data.put ("p2", this.getPoint (lines[7]));
}}list.addLast (data);
var pairs = this.newList (null);
data.put ("basePairs", pairs);
this.htTemp.put (type + "#" + (i + 1), pairs);
for (var j = 0; j < bps; j++) pairs.addLast (this.getBPData (i + 1, type, isHelix));

}
}, "~N,~S,~S,~B");
Clazz.defineMethod (c$, "getPoint", 
 function (data) {
var a = JU.PT.parseFloatArray (this.after (data, ":"));
return JU.P3.new3 (a[0], a[1], a[2]);
}, "~S");
Clazz.defineMethod (c$, "getNTList", 
 function () {
var data =  new java.util.Hashtable ();
var tokens = JU.PT.getTokens (this.rd ());
var pt = (tokens[0].startsWith ("nts") ? 0 : 1);
if (tokens.length > pt + 2) {
data.put ("nres", Integer.$valueOf (JU.PT.replaceAllCharacters (this.after (tokens[pt], "="), "*;", "")));
data.put ("seq", tokens[++pt]);
data.put ("nts", this.getNT (tokens[++pt], false));
data.put ("resnos", this.getNT (tokens[pt], true));
}return data;
});
Clazz.defineMethod (c$, "getNT", 
 function (s, isResno) {
var tokens = JU.PT.split (s, ",");
var list =  new JU.Lst ();
for (var i = 0; i < tokens.length; i++) list.addLast (this.fix (tokens[i], !isResno));

return list;
}, "~S,~B");
Clazz.defineMethod (c$, "getHeader", 
 function () {
var header =  new JU.SB ();
header.append (this.line).append ("\n");
while (this.isHeader (this.rd ())) header.append (this.line).append ("\n");

return header.toString ();
});
Clazz.defineMethod (c$, "skipHeader", 
 function () {
while (this.isHeader (this.rd ())) {
}
});
Clazz.defineMethod (c$, "isHeader", 
 function (line) {
return line.length < 6 || line.charAt (3) == ' ' || line.charAt (5) == ' ';
}, "~S");
Clazz.defineMethod (c$, "skipTo", 
 function (key, startsWith) {
while (!(startsWith ? this.line.startsWith (key) : this.line.contains (key))) {
this.rd ();
}
}, "~S,~B");
Clazz.defineMethod (c$, "fix", 
 function (nt, withName) {
var pt1;
if (nt.startsWith ("[")) {
if ((pt1 = nt.indexOf ("/")) >= 0) nt = nt.substring (0, pt1);
if (withName) return nt;
if ((pt1 = nt.indexOf (".")) >= 0) nt = nt.substring (0, pt1);
return (nt.substring (nt.indexOf ("]") + 1));
}pt1 = nt.indexOf (".");
var chain = nt.substring (0, pt1);
var pt = nt.length;
var ch;
while (Character.isDigit (ch = nt.charAt (--pt))) {
}
var ptn = chain.indexOf ("@");
if (ptn >= 0) chain = chain.substring (ptn + 1) + (withName ? "." + chain.substring (0, ptn) : "");
var pt2 = (ch == '/' ? pt : pt + 1);
return (withName ? "[" + nt.substring (pt1 + 1, pt2) + "]" : "") + nt.substring (pt + 1) + ":" + chain;
}, "~S,~B");
Clazz.defineMethod (c$, "after", 
 function (s, key) {
return s.substring (s.indexOf (key) + 1);
}, "~S,~S");
Clazz.defineMethod (c$, "rd", 
 function () {
this.line = this.reader.readNextLine ();
if (JU.Logger.debugging) JU.Logger.info (this.line);
return this.line;
});
Clazz.overrideMethod (c$, "setAllDSSRParametersForModel", 
function (vwr, modelIndex) {
var dssr = vwr.ms.getInfo (modelIndex, "dssr");
var lst = (dssr == null ? null : dssr.get ("basePairs"));
var lst1 = (dssr == null ? null : dssr.get ("singleStranded"));
if (lst == null && lst1 == null) {
var m = vwr.ms.am[modelIndex];
var n = m.getBioPolymerCount ();
for (var i = n; --i >= 0; ) {
var bp = m.getBioPolymer (i);
if (bp.isNucleic ()) (bp).isDssrSet = true;
}
return;
}var htChains =  new java.util.Hashtable ();
var bs =  new JU.BS ();
if (lst != null) {
for (var i = lst.size (); --i >= 0; ) {
var bpInfo = lst.get (i);
JM.BasePair.add (bpInfo, this.setDSSRPhos (vwr, 1, bpInfo, bs, htChains), this.setDSSRPhos (vwr, 2, bpInfo, bs, htChains));
}
}if (lst1 != null) for (var i = lst1.size (); --i >= 0; ) {
var bp = lst1.get (i);
var resnos = bp.get ("resnos");
for (var j = resnos.size (); --j >= 0; ) this.setDSSRRes (vwr, resnos.get (j), bs, htChains);

}
}, "JV.Viewer,~N");
Clazz.defineMethod (c$, "setDSSRPhos", 
 function (vwr, n, bp, bs, htChains) {
return this.setDSSRRes (vwr, bp.get ("res" + n), bs, htChains);
}, "JV.Viewer,~N,java.util.Map,JU.BS,java.util.Map");
Clazz.defineMethod (c$, "setDSSRRes", 
 function (vwr, res, bs, htChains) {
bs.clearAll ();
this.getDSSRAtoms (vwr, res, null, bs, htChains);
var group = vwr.ms.at[bs.nextSetBit (0)].getGroup ();
(group.bioPolymer).isDssrSet = true;
return group;
}, "JV.Viewer,~S,JU.BS,java.util.Map");
Clazz.overrideMethod (c$, "getHBonds", 
function (ms, modelIndex, vHBonds, doReport) {
var info = ms.getInfo (modelIndex, "dssr");
if (info != null) info = (info).get ("hBonds");
if (info == null) return "no DSSR hydrogen-bond data";
var list = info;
var a0 = ms.am[modelIndex].firstAtomIndex - 1;
try {
for (var i = list.size (); --i >= 0; ) {
var hbond = list.get (i);
var a1 = (hbond.get ("atno1")).intValue () + a0;
var a2 = (hbond.get ("atno2")).intValue () + a0;
var energy = (hbond.containsKey ("energy") ? (hbond.get ("energy")).floatValue () : 0);
vHBonds.addLast ( new JM.HBond (ms.at[a1], ms.at[a2], 2048, 1, 0, energy));
}
} catch (e) {
if (Clazz.exceptionOf (e, Exception)) {
JU.Logger.error ("Exception " + e + " in DSSRParser.getHBonds");
} else {
throw e;
}
}
return "DSSR reports " + list.size () + " hydrogen bonds";
}, "JM.ModelSet,~N,JU.Lst,~B");
Clazz.overrideMethod (c$, "calculateDSSRStructure", 
function (vwr, bsAtoms) {
var bs = vwr.ms.getModelBS (bsAtoms == null ? vwr.bsA () : bsAtoms, true);
var s = "";
for (var i = bs.nextSetBit (0); i >= 0; i = bs.nextSetBit (i + 1)) s += this.getDSSRForModel (vwr, i);

return s;
}, "JV.Viewer,JU.BS");
Clazz.defineMethod (c$, "getDSSRForModel", 
 function (vwr, modelIndex) {
var info = null;
var out = null;
while (true) {
if (!vwr.ms.am[modelIndex].isBioModel) break;
info = vwr.ms.getModelAuxiliaryInfo (modelIndex);
if (info.containsKey ("dssr")) break;
var bs = vwr.getModelUndeletedAtomsBitSet (modelIndex);
bs.and (vwr.ms.getAtoms (3145742, null));
if (bs.nextClearBit (0) < 0) {
info = null;
break;
}try {
var name = vwr.setLoadFormat ("=dssrModel/", '=', false);
name = JU.PT.rep (name, "%20", " ");
JU.Logger.info ("fetching " + name + "[pdb data]");
var data = vwr.getPdbAtomData (bs, null);
data = vwr.getFileAsString (name + data, false);
this.processDSSR (info,  new JU.Rdr (JU.Rdr.getBR (data)), null, null);
} catch (e) {
if (Clazz.exceptionOf (e, Exception)) {
info = null;
out = "" + e;
} else {
throw e;
}
}
break;
}
return (info != null ? (info.get ("dssr")).get ("summary") : out == null ? "model has no nucleotides" : out);
}, "JV.Viewer,~N");
Clazz.defineMethod (c$, "getAnnotationKVPairs", 
 function (a, match, dotPath, sb, pre, showDetail, isMappingOnly, type) {
var map = a.getMap ();
if (map == null || map.isEmpty ()) return;
if (map.containsKey ("_map")) map = map.get ("_map").getMap ();
var detailKey = this.getDataKey (type);
if (showDetail && map.containsKey (detailKey)) {
if (match == null || dotPath.indexOf (match) >= 0) sb.append (map.get (detailKey).asString ()).append ("\n");
return;
}for (var e, $e = map.entrySet ().iterator (); $e.hasNext () && ((e = $e.next ()) || true);) {
var key = e.getKey ();
if (key.equals (detailKey)) continue;
if (key.equals ("metadata")) sb.append ("\n");
var val = e.getValue ();
if (val.tok == 6) {
if (type == 1073742189 && !showDetail) {
sb.append (key).append ("\n");
} else {
this.getAnnotationKVPairs (val, match, (dotPath.length == 0 ? "" : dotPath + ".") + key, sb, (pre.length == 0 ? "" : pre + "\t") + key, showDetail, isMappingOnly, type);
}} else {
var s = val.asString ();
if (match == null || s.indexOf (match) >= 0 || pre.indexOf (match) >= 0 || key.indexOf (match) >= 0 || dotPath.indexOf (match) >= 0) {
if (showDetail && isMappingOnly) continue;
if (pre.length > 0) sb.append (pre).append ("\t");
sb.append (key).append ("=");
sb.append (s).append ("\n");
}}}
}, "JS.SV,~S,~S,JU.SB,~S,~B,~B,~N");
Clazz.defineMethod (c$, "getDataKey", 
 function (type) {
switch (type) {
case 1073741925:
return "mappings";
case 1073742189:
return "outliers";
}
return null;
}, "~N");
Clazz.overrideMethod (c$, "catalogValidations", 
function (viewer, map0, modelAtomIndices, resMap, atomMap, modelMap) {
var data = map0.getMap ();
if (data == null) return null;
var retProperties =  new JU.Lst ();
var nModels = modelAtomIndices.length - 1;
try {
data = this.getMainItem (data).getMap ();
map0.getMap ().put ("_map", JS.SV.newV (6, data));
var list =  new JU.Lst ();
map0.getMap ().put ("_list", JS.SV.newV (7, list));
var set = data.entrySet ();
var sv;
var map;
for (var e, $e = set.iterator (); $e.hasNext () && ((e = $e.next ()) || true);) {
var floats = JU.AU.newFloat2 (nModels);
for (var m = nModels; --m >= 0; ) floats[m] =  Clazz.newFloatArray (modelAtomIndices[m + 1] - modelAtomIndices[m], 0);

sv = e.getValue ();
var outliers = sv.getList ();
if (outliers == null) {
map = sv.getMap ();
if (map != null && (sv = map.get ("outliers")) != null) outliers = sv.getList ();
}if (outliers != null) {
var hasUnit = false;
var key = e.getKey ();
var svPath = JS.SV.newS (key);
var isRes = false;
for (var j = outliers.size (); --j >= 0; ) {
var out = outliers.get (j);
map = out.getMap ();
sv = map.get ("units");
var svv = map.get ("value");
var val = (svv == null ? 1 : JS.SV.fValue (svv));
var units = (val == 0 || sv == null || sv.tok == 7 ? sv.getList () : sv.tok == 4 ?  new JU.Lst () : null);
if (units != null) {
if (sv.tok == 4) {
var svl = JU.PT.split (sv.asString (), ",");
for (var i = svl.length; --i >= 0; ) units.addLast (JS.SV.newS (svl[i].trim ()));

}if (units.size () > 0) {
var bsAtoms =  new JU.BS ();
map.put ("_atoms", JS.SV.getVariable (bsAtoms));
map.put ("_path", svPath);
hasUnit = true;
list.addLast (out);
for (var k = units.size (); --k >= 0; ) {
var ret = this.catalogUnit (viewer, floats, units.get (k).asString (), val, bsAtoms, modelAtomIndices, resMap, atomMap, modelMap);
if (ret) map.put ("_isres", JS.SV.vT);
isRes = new Boolean (isRes | ret).valueOf ();
}
}}}
if (hasUnit) {
for (var m = nModels; --m >= 0; ) if (floats[m] != null) {
retProperties.addLast (key);
retProperties.addLast (floats[m]);
retProperties.addLast (Integer.$valueOf (m));
retProperties.addLast (Boolean.$valueOf (isRes));
}
}}}
return retProperties;
} catch (e) {
if (Clazz.exceptionOf (e, Exception)) {
JU.Logger.info (e + " while cataloging validations");
return null;
} else {
throw e;
}
}
}, "JV.Viewer,JS.SV,~A,java.util.Map,java.util.Map,java.util.Map");
Clazz.defineMethod (c$, "getMainItem", 
 function (data) {
for (var e, $e = data.entrySet ().iterator (); $e.hasNext () && ((e = $e.next ()) || true);) {
var key = e.getKey ();
if (!key.contains ("metadata")) return e.getValue ();
}
return null;
}, "java.util.Map");
Clazz.overrideMethod (c$, "initializeAnnotation", 
function (objAnn, type, modelIndex) {
var map = objAnn.getMap ();
var _list = map.get ("_list");
if (_list != null) return _list.getList ();
var dataKey = this.getDataKey (type);
var main = this.getMainItem (map);
map.put ("_map", main);
var noSingles = true;
var _cat =  new java.util.Hashtable ();
map.put ("_cat", JS.SV.newV (6, _cat));
var list =  new JU.Lst ();
map.put ("_list", _list = JS.SV.newV (7, list));
for (var e, $e = main.getMap ().entrySet ().iterator (); $e.hasNext () && ((e = $e.next ()) || true);) {
var _dbName = e.getKey ();
var _dbMap = e.getValue ();
_cat.putAll (_dbMap.getMap ());
for (var e2, $e2 = _dbMap.getMap ().entrySet ().iterator (); $e2.hasNext () && ((e2 = $e2.next ()) || true);) {
var _domainName = e2.getKey ();
var _domainMap = e2.getValue ();
var _domainList = _domainMap.mapGet (dataKey);
var _mapList = _domainList.getList ();
for (var i = _mapList.size (); --i >= 0; ) {
var mapping = _mapList.get (i);
list.addLast (mapping);
var mmap = mapping.getMap ();
var _chain = mmap.get ("chain_id");
var start = mmap.get ("start");
var end = mmap.get ("end");
var res1 = 0;
var res2 = 0;
var rescode = "modelIndex=" + modelIndex + "&chain='" + _chain.value + "'";
if (start != null && end != null) {
res1 = start.getMap ().get ("residue_number").intValue;
res2 = end.getMap ().get ("residue_number").intValue;
rescode += "&seqid>=" + res1 + "&seqid<=" + res2;
} else {
res2 = 1;
rescode += "&seqid>0";
}var _atoms = (noSingles && res1 >= res2 ? JS.SV.getVariable ( new JU.BS ()) : _cat.get (rescode));
if (_atoms == null) _cat.put (rescode, _atoms = JS.SV.newS (rescode));
mmap.put ("_atoms", _atoms);
mmap.put ("_path", JS.SV.newS (_dbName + "." + _domainName));
mmap.put ("domain", _domainMap);
}
}
}
return list;
}, "JS.SV,~N,~N");
Clazz.defineMethod (c$, "findAnnotationAtoms", 
 function (vwr, name, _list, key, bs) {
if (_list == null) return;
System.out.println ("Checking " + name + " for " + key);
var data = vwr.extractProperty (_list, "[" + key + "]", -1);
var list = null;
if (Clazz.instanceOf (data, JU.Lst)) {
list = data;
} else if (Clazz.instanceOf (data, JS.SV)) {
list = (data).getList ();
}if (list == null) return;
for (var i = 0, n = list.size (); i < n; i++) {
var o = list.get (i);
var mapping = (Clazz.instanceOf (o, JS.SV) ? (o).getMap () : o);
if (mapping == null) return;
bs.or (this.setAnnotationAtoms (vwr, mapping, i));
}
}, "JV.Viewer,~S,JU.Lst,~S,JU.BS");
Clazz.defineMethod (c$, "setAnnotationAtoms", 
 function (vwr, mapping, i) {
var _atoms = mapping.get ("_atoms");
if (_atoms.tok != 10) {
var bs2 = vwr.getAtomBitSet (_atoms.value);
if (i >= 0) JU.Logger.info ("#" + (i + 1) + " found " + bs2.cardinality () + " atoms for " + _atoms.value);
_atoms.tok = 10;
_atoms.value = bs2;
}return _atoms.value;
}, "JV.Viewer,java.util.Map,~N");
Clazz.defineMethod (c$, "catalogUnit", 
 function (viewer, vals, unitID, val, bsAtoms, modelAtomIndices, resMap, atomMap, modelMap) {
var s = JU.PT.split (unitID + "|||", "|");
if (s.length < 8 || s[1].length == 0 || s[2].length == 0 || s[3].length == 0 || s[4].length == 0) return false;
var sm = (s[1].length == 0 ? "1" : s[1]);
var m = (modelMap == null ? JU.PT.parseInt (sm) - 1 : -1);
var im = (m >= 0 ? null : modelMap.get (sm));
if (im != null) m = im.intValue ();
if (m >= modelAtomIndices.length) return false;
var res = s[1] + "_" + viewer.getChainID (s[2]) + "_" + s[4] + "_" + s[7].toLowerCase ();
var i0 = modelAtomIndices[m];
var isRes = (atomMap == null || s[5].length == 0);
if (isRes) {
var a2 = resMap.get (res);
if (a2 != null) for (var j = a2[1], j0 = a2[0]; --j >= j0; ) {
bsAtoms.set (i0 + j);
vals[m][j] += Math.abs (val);
}
} else {
if (s[5].charAt (0) == 'H') s[5] = this.getAttachedAtomForPDBH (s[3], s[5]);
var atom = res + "_" + s[5] + "_" + s[6].toLowerCase ();
var ia = atomMap.get (atom);
if (ia != null) {
var j = ia.intValue ();
bsAtoms.set (i0 + j);
vals[m][j] += Math.abs (val);
}}return isRes;
}, "JV.Viewer,~A,~S,~N,JU.BS,~A,java.util.Map,java.util.Map,java.util.Map");
Clazz.defineMethod (c$, "fixKeyDSSR", 
 function (key) {
var s = key.toLowerCase ();
if (s.indexOf ("pairs") < 0 && s.indexOf ("kissingloops") < 0 && s.indexOf ("linkedby") < 0 && s.indexOf ("multiplets") < 0 && s.indexOf ("singlestrand") < 0) key += ".basePairs";
if (s.indexOf (".nt") < 0 && s.indexOf (".res") < 0 && s.indexOf ("[select res") < 0 && s.indexOf ("[select nt") < 0) key += ".res*";
return key;
}, "~S");
Clazz.defineMethod (c$, "getDSSRAtoms", 
 function (vwr, res, lst, bs, htChains) {
var tokens;
if (lst == null) {
tokens = JU.PT.getTokens (JU.PT.replaceAllCharacters (res.toString (), "=[,]", " "));
} else if (lst.size () == 0) {
return;
} else {
tokens =  new Array (lst.size ());
for (var i = lst.size (); --i >= 0; ) {
var o = lst.get (i);
if (Clazz.instanceOf (o, JS.SV)) o = (o).value;
if (Clazz.instanceOf (o, JU.Lst)) {
this.getDSSRAtoms (vwr, null, o, bs, htChains);
} else {
var s = (Clazz.instanceOf (o, JS.SV) ? (o).asString () : o.toString ());
tokens[i] = (s.startsWith ("[") ? s.substring (s.indexOf ("]") + 1) : s);
}}
}for (var j = tokens.length; --j >= 0; ) {
var t = tokens[j];
if (t == null) continue;
var pt = t.indexOf (":");
if (pt < 0 || pt + 1 == t.length) continue;
var chain = t.substring (pt + 1);
var bsChain = htChains.get (chain);
try {
if (bsChain == null) htChains.put (chain, bsChain = vwr.ms.getAtoms (1048609, Integer.$valueOf (vwr.getChainID (chain))));
var bsRes = vwr.ms.getAtoms (1095761939, Integer.$valueOf (t.substring (0, pt)));
bsRes.and (bsChain);
bs.or (bsRes);
} catch (e) {
if (Clazz.exceptionOf (e, Exception)) {
} else {
throw e;
}
}
}
}, "JV.Viewer,~S,JU.Lst,JU.BS,java.util.Map");
Clazz.overrideMethod (c$, "getAtomBits", 
function (vwr, key, dbObj, annotationCache, type, modelIndex, bsModel) {
if (dbObj == null) return  new JU.BS ();
var isDomains = (type == 1073741925);
var isValidation = (type == 1073742189);
var isDSSR = (type == 1073741916);
var doCache = !key.contains ("NOCACHE");
if (!doCache) {
key = JU.PT.rep (key, "NOCACHE", "").trim ();
}if (!isDomains && !isValidation) key = this.fixKeyDSSR (key);
var bs = (doCache ? annotationCache.get (key) : null);
if (bs != null) return bs;
bs =  new JU.BS ();
if (doCache) annotationCache.put (key, bs);
try {
if (isDSSR) {
var data = vwr.extractProperty (dbObj, key, -1);
if (Clazz.instanceOf (data, JU.Lst)) {
var htChains =  new java.util.Hashtable ();
this.getDSSRAtoms (vwr, null, data, bs, htChains);
}return bs;
}var list = this.initializeAnnotation (dbObj, type, modelIndex);
var pt = key.toLowerCase ().indexOf (" where ");
var path = JU.PT.rep ((pt < 0 ? key : key.substring (0, pt)), " ", "");
var newKey = (pt < 0 ? "" : key.substring (pt + 7).trim ());
if (path.indexOf (".") < 0) {
path = " _path like '" + path + "*'";
} else {
path = " _path='" + path + "'";
}newKey = "select * where " + (pt < 0 ? path : "(" + newKey + ") and (" + path + ")");
JU.Logger.info ("looking for " + newKey);
this.findAnnotationAtoms (vwr, path, list, newKey, bs);
bs.and (bsModel);
} catch (e) {
if (Clazz.exceptionOf (e, Exception)) {
System.out.println (e.toString () + " in AnnotationParser");
bs.clearAll ();
} else {
throw e;
}
}
return bs;
}, "JV.Viewer,~S,~O,java.util.Map,~N,~N,JU.BS");
Clazz.overrideMethod (c$, "getAtomValidation", 
function (vwr, type, atom) {
var i = 0;
var n = 0;
var l = null;
var map = null;
var list = null;
try {
var ia = atom.i;
l =  new JU.Lst ();
list = (vwr.ms.getModelAuxiliaryInfo (atom.mi).get ("validation")).getMap ().get ("_list").getList ();
for (i = 0, n = list.size (); i < n; i++) {
map = list.get (i).getMap ();
if (map.get ("_path").value.equals (type) && (map.get ("_atoms").value).get (ia)) {
var v = map.get ("value");
l.addLast (v.tok == 3 ? v.value : Float.$valueOf (v.asFloat ()));
}}
return l;
} catch (e) {
if (Clazz.exceptionOf (e, Exception)) {
return null;
} else {
throw e;
}
}
}, "JV.Viewer,~S,JM.Atom");
Clazz.overrideMethod (c$, "getAnnotationInfo", 
function (vwr, a, match, type, modelIndex) {
var sb =  new JU.SB ();
if ("".equals (match)) match = null;
var isDetail = (match != null && (match.equals ("all") || match.endsWith (" all")));
if (isDetail) {
var _list = this.initializeAnnotation (a, type, modelIndex);
for (var i = _list.size (); --i >= 0; ) this.setAnnotationAtoms (vwr, _list.get (i).getMap (), -1);

match = match.substring (0, Math.max (0, match.length - 4)).trim ();
}if ("".equals (match)) match = null;
if (type == 1073742189 && !isDetail && match == null) return a.getMap ().get ("_note").asString ();
var isMappingOnly = (match != null && match.indexOf (".") >= 0 && match.indexOf (".*") < 0);
match = JU.PT.rep (match, "*", "");
try {
this.getAnnotationKVPairs (a, match, "", sb, "", isDetail, isMappingOnly, type);
} catch (e) {
if (Clazz.exceptionOf (e, Exception)) {
{
System.out.println(e);
}} else {
throw e;
}
}
return sb.toString ();
}, "JV.Viewer,JS.SV,~S,~N,~N");
Clazz.defineMethod (c$, "getAttachedAtomForPDBH", 
function (group3, name) {
if (name.charAt (0) == 'H') {
if (J.dssx.AnnotationParser.pdbAtomForH == null) {
J.dssx.AnnotationParser.pdbAtomForH =  new java.util.Hashtable ();
this.assignPDBH ("", "N H H1 H2 H3 CB HB2 HB3 CD HD2 HD3 CG HG2 HG3 C2' H2'' H2' C5' H5'' H5' OXT HXT");
for (var i = JV.JC.pdbBondInfo.length; --i >= 1; ) {
this.assignPDBH (JV.JC.group3Names[i], JV.JC.pdbBondInfo[i]);
}
}var a = J.dssx.AnnotationParser.pdbAtomForH.get (name);
if (a == null) a = J.dssx.AnnotationParser.pdbAtomForH.get (group3 + name);
if (a != null) return a;
}return name;
}, "~S,~S");
Clazz.defineMethod (c$, "assignPDBH", 
 function (group3, sNames) {
var names = JU.PT.getTokens (JU.PT.rep (sNames, "@", " "));
var a = null;
for (var i = 0, n = names.length; i < n; i++) {
var s = names[i];
if (s.charAt (0) != 'H') {
a = s;
continue;
}s = group3 + s;
if (s.indexOf ("?") >= 0) {
s = s.substring (0, s.length - 1);
J.dssx.AnnotationParser.pdbAtomForH.put (s + "1", a);
J.dssx.AnnotationParser.pdbAtomForH.put (s + "2", a);
J.dssx.AnnotationParser.pdbAtomForH.put (s + "3", a);
} else {
J.dssx.AnnotationParser.pdbAtomForH.put (s, a);
}}
}, "~S,~S");
Clazz.overrideMethod (c$, "fixAtoms", 
function (modelIndex, dbObj, bsAddedMask, type, margin) {
var _list = this.initializeAnnotation (dbObj, type, modelIndex);
for (var i = _list.size (); --i >= 0; ) {
var m = _list.get (i).getMap ();
var _atoms = m.get ("_atoms");
if (_atoms != null && _atoms.tok == 10) JU.BSUtil.shiftBits (_atoms.value, bsAddedMask, _list.get (i).getMap ().containsKey ("_isres"), (_atoms.value).length () + margin);
}
}, "~N,JS.SV,JU.BS,~N,~N");
Clazz.defineStatics (c$,
"pdbAtomForH", null);
});
