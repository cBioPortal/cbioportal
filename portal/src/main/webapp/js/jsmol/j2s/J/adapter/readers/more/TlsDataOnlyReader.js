Clazz.declarePackage ("J.adapter.readers.more");
Clazz.load (["J.adapter.smarter.AtomSetCollectionReader"], "J.adapter.readers.more.TlsDataOnlyReader", ["java.lang.Float", "java.util.Hashtable", "J.util.Escape", "$.JmolList", "$.Logger", "$.P3", "$.Parser", "$.SB"], function () {
c$ = Clazz.decorateAsClass (function () {
this.vTlsModels = null;
this.sbTlsErrors = null;
this.tlsGroupID = 0;
Clazz.instantialize (this, arguments);
}, J.adapter.readers.more, "TlsDataOnlyReader", J.adapter.smarter.AtomSetCollectionReader);
Clazz.overrideMethod (c$, "initializeReader", 
function () {
this.readTlsData ();
this.continuing = false;
});
$_M(c$, "readTlsData", 
($fz = function () {
this.vTlsModels =  new J.util.JmolList ();
var tlsGroups;
var tlsGroup = null;
var ranges = null;
var range = null;
tlsGroups =  new J.util.JmolList ();
while (this.readLine () != null) {
var tokens = J.adapter.smarter.AtomSetCollectionReader.getTokensStr (this.line.$replace ('\'', ' '));
if (tokens.length == 0) continue;
if (tokens[0].equals ("TLS")) {
tlsGroup =  new java.util.Hashtable ();
ranges =  new J.util.JmolList ();
tlsGroup.put ("ranges", ranges);
tlsGroups.addLast (tlsGroup);
tlsGroup.put ("id", Integer.$valueOf (++this.tlsGroupID));
} else if (tokens[0].equals ("RANGE")) {
range =  new java.util.Hashtable ();
var chain1 = tokens[1].charAt (0);
var chain2 = tokens[3].charAt (0);
var res1 = J.util.Parser.parseInt (tokens[2]);
var res2 = J.util.Parser.parseInt (tokens[4]);
if (chain1 == chain2) {
range.put ("chains", "" + chain1 + chain2);
if (res1 <= res2) {
range.put ("residues", [res1, res2]);
ranges.addLast (range);
} else {
this.tlsAddError (" TLS group residues are not in order (range ignored)");
}} else {
this.tlsAddError (" TLS group chains are different (range ignored)");
}} else if (tokens[0].equals ("ORIGIN")) {
var origin =  new J.util.P3 ();
tlsGroup.put ("origin", origin);
origin.set (this.parseFloatStr (tokens[1]), this.parseFloatStr (tokens[2]), this.parseFloatStr (tokens[3]));
if (Float.isNaN (origin.x) || Float.isNaN (origin.y) || Float.isNaN (origin.z)) {
origin.set (NaN, NaN, NaN);
this.tlsAddError ("invalid origin: " + this.line);
}} else if (tokens[0].equals ("T") || tokens[0].equals ("L") || tokens[0].equals ("S")) {
var tensorType = tokens[0].charAt (0);
var nn = (tensorType == 'S' ? J.adapter.readers.more.TlsDataOnlyReader.Snn : J.adapter.readers.more.TlsDataOnlyReader.TLnn);
var tensor =  Clazz.newFloatArray (3, 3, 0);
tlsGroup.put ("t" + tensorType, tensor);
for (var i = 1; i < tokens.length; i++) {
var ti = nn[i].charCodeAt (0) - 49;
var tj = nn[i].charCodeAt (1) - 49;
tensor[ti][tj] = this.parseFloatStr (tokens[++i]);
if (ti < tj) tensor[tj][ti] = tensor[ti][tj];
}
if (tensorType == 'S') tensor[0][0] = -tensor[0][0];
for (var i = 0; i < 3; i++) for (var j = 0; j < 3; j++) if (Float.isNaN (tensor[i][j])) {
this.tlsAddError ("invalid tensor: " + J.util.Escape.escapeFloatAA (tensor, false));
}

}}
J.util.Logger.info (this.tlsGroupID + " TLS groups read");
var groups =  new java.util.Hashtable ();
groups.put ("groupCount", Integer.$valueOf (this.tlsGroupID));
groups.put ("groups", tlsGroups);
this.vTlsModels.addLast (groups);
this.htParams.put ("vTlsModels", this.vTlsModels);
}, $fz.isPrivate = true, $fz));
$_M(c$, "tlsAddError", 
($fz = function (error) {
if (this.sbTlsErrors == null) this.sbTlsErrors =  new J.util.SB ();
this.sbTlsErrors.append (this.fileName).appendC ('\t').append ("TLS group ").appendI (this.tlsGroupID).appendC ('\t').append (error).appendC ('\n');
}, $fz.isPrivate = true, $fz), "~S");
c$.TLnn = c$.prototype.TLnn = ["11", "22", "33", "12", "13", "23"];
c$.Snn = c$.prototype.Snn = ["22", "11", "12", "13", "23", "21", "31", "32"];
});
