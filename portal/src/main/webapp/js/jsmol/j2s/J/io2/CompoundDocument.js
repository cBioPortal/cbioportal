Clazz.declarePackage ("J.io2");
Clazz.load (["J.io2.BinaryDocument", "$.CompoundDocHeader", "J.util.JmolList"], "J.io2.CompoundDocument", ["java.io.DataInputStream", "J.io2.CompoundDocDirEntry", "$.ZipData", "J.util.Logger", "$.SB"], function () {
c$ = Clazz.decorateAsClass (function () {
this.header = null;
this.directory = null;
this.rootEntry = null;
this.SAT = null;
this.SSAT = null;
this.sectorSize = 0;
this.shortSectorSize = 0;
this.nShortSectorsPerStandardSector = 0;
this.nIntPerSector = 0;
this.nDirEntriesperSector = 0;
this.data = null;
Clazz.instantialize (this, arguments);
}, J.io2, "CompoundDocument", J.io2.BinaryDocument);
Clazz.prepareFields (c$, function () {
this.header =  new J.io2.CompoundDocHeader (this);
this.directory =  new J.util.JmolList ();
});
Clazz.makeConstructor (c$, 
function () {
Clazz.superConstructor (this, J.io2.CompoundDocument);
this.isBigEndian = true;
});
Clazz.overrideMethod (c$, "setStream", 
function (bis, isBigEndian) {
if (!this.isRandom) {
this.stream =  new java.io.DataInputStream (bis);
}this.stream.mark (2147483647);
if (!this.readHeader ()) return;
this.getSectorAllocationTable ();
this.getShortSectorAllocationTable ();
this.getDirectoryTable ();
}, "java.io.BufferedInputStream,~B");
$_M(c$, "getDirectory", 
function () {
return this.directory;
});
$_M(c$, "getDirectoryListing", 
function (separator) {
var str = "";
for (var i = 0; i < this.directory.size (); i++) {
var thisEntry = this.directory.get (i);
if (!thisEntry.isEmpty) str += separator + thisEntry.entryName + "\tlen=" + thisEntry.lenStream + "\tSID=" + thisEntry.SIDfirstSector + (thisEntry.isStandard ? "\tfileOffset=" + this.getOffset (thisEntry.SIDfirstSector) : "");
}
return str;
}, "~S");
$_M(c$, "getAllData", 
function () {
return this.getAllDataFiles (null, null);
});
Clazz.overrideMethod (c$, "getAllDataMapped", 
function (prefix, binaryFileList, fileData) {
fileData.put ("#Directory_Listing", this.getDirectoryListing ("|"));
binaryFileList = "|" + binaryFileList + "|";
for (var i = 0; i < this.directory.size (); i++) {
var thisEntry = this.directory.get (i);
if (!thisEntry.isEmpty && thisEntry.entryType != 5) {
var name = thisEntry.entryName;
J.util.Logger.info ("CompoundDocument file " + name);
var isBinary = (binaryFileList.indexOf ("|" + name + "|") >= 0);
if (isBinary) name += ":asBinaryString";
var data =  new J.util.SB ();
data.append ("BEGIN Directory Entry ").append (name).append ("\n");
data.appendSB (this.getEntryAsString (thisEntry, isBinary));
data.append ("\nEND Directory Entry ").append (name).append ("\n");
fileData.put (prefix + "/" + name, data.toString ());
}}
this.close ();
}, "~S,~S,java.util.Map");
Clazz.overrideMethod (c$, "getAllDataFiles", 
function (binaryFileList, firstFile) {
if (firstFile != null) {
for (var i = 0; i < this.directory.size (); i++) {
var thisEntry = this.directory.get (i);
if (thisEntry.entryName.equals (firstFile)) {
this.directory.remove (i);
this.directory.add (1, thisEntry);
break;
}}
}this.data =  new J.util.SB ();
this.data.append ("Compound Document File Directory: ");
this.data.append (this.getDirectoryListing ("|"));
this.data.append ("\n");
binaryFileList = "|" + binaryFileList + "|";
for (var i = 0; i < this.directory.size (); i++) {
var thisEntry = this.directory.get (i);
J.util.Logger.info ("reading " + thisEntry.entryName);
if (!thisEntry.isEmpty && thisEntry.entryType != 5) {
var name = thisEntry.entryName;
if (name.endsWith (".gz")) name = name.substring (0, name.length - 3);
this.data.append ("BEGIN Directory Entry ").append (name).append ("\n");
this.data.appendSB (this.getEntryAsString (thisEntry, binaryFileList.indexOf ("|" + thisEntry.entryName + "|") >= 0));
this.data.append ("\n");
this.data.append ("END Directory Entry ").append (thisEntry.entryName).append ("\n");
}}
this.close ();
return this.data;
}, "~S,~S");
$_M(c$, "getFileAsString", 
function (entryName) {
for (var i = 0; i < this.directory.size (); i++) {
var thisEntry = this.directory.get (i);
if (thisEntry.entryName.equals (entryName)) return this.getEntryAsString (thisEntry, false);
}
return  new J.util.SB ();
}, "~S");
$_M(c$, "getOffset", 
($fz = function (SID) {
return (SID + 1) * this.sectorSize;
}, $fz.isPrivate = true, $fz), "~N");
$_M(c$, "gotoSector", 
($fz = function (SID) {
this.seek (this.getOffset (SID));
}, $fz.isPrivate = true, $fz), "~N");
$_M(c$, "readHeader", 
($fz = function () {
if (!this.header.readData ()) return false;
this.sectorSize = 1 << this.header.sectorPower;
this.shortSectorSize = 1 << this.header.shortSectorPower;
this.nShortSectorsPerStandardSector = Clazz.doubleToInt (this.sectorSize / this.shortSectorSize);
this.nIntPerSector = Clazz.doubleToInt (this.sectorSize / 4);
this.nDirEntriesperSector = Clazz.doubleToInt (this.sectorSize / 128);
if (J.util.Logger.debugging) {
J.util.Logger.debug ("compound document: revNum=" + this.header.revNumber + " verNum=" + this.header.verNumber + " isBigEndian=" + this.isBigEndian + " bytes per standard/short sector=" + this.sectorSize + "/" + this.shortSectorSize);
}return true;
}, $fz.isPrivate = true, $fz));
$_M(c$, "getSectorAllocationTable", 
($fz = function () {
var nSID = 0;
var thisSID;
this.SAT =  Clazz.newIntArray (this.header.nSATsectors * this.nIntPerSector + 109, 0);
try {
for (var i = 0; i < 109; i++) {
thisSID = this.header.MSAT0[i];
if (thisSID < 0) break;
this.gotoSector (thisSID);
for (var j = 0; j < this.nIntPerSector; j++) {
this.SAT[nSID++] = this.readInt ();
}
}
var nMaster = this.header.nAdditionalMATsectors;
thisSID = this.header.SID_MSAT_next;
var MSAT =  Clazz.newIntArray (this.nIntPerSector, 0);
out : while (nMaster-- > 0 && thisSID >= 0) {
this.gotoSector (thisSID);
for (var i = 0; i < this.nIntPerSector; i++) MSAT[i] = this.readInt ();

for (var i = 0; i < this.nIntPerSector - 1; i++) {
thisSID = MSAT[i];
if (thisSID < 0) break out;
this.gotoSector (thisSID);
for (var j = this.nIntPerSector; --j >= 0; ) this.SAT[nSID++] = this.readInt ();

}
thisSID = MSAT[this.nIntPerSector - 1];
}
} catch (e) {
if (Clazz.exceptionOf (e, Exception)) {
J.util.Logger.errorEx (null, e);
} else {
throw e;
}
}
}, $fz.isPrivate = true, $fz));
$_M(c$, "getShortSectorAllocationTable", 
($fz = function () {
var nSSID = 0;
var thisSID = this.header.SID_SSAT_start;
var nMax = this.header.nSSATsectors * this.nIntPerSector;
this.SSAT =  Clazz.newIntArray (nMax, 0);
try {
while (thisSID > 0 && nSSID < nMax) {
this.gotoSector (thisSID);
for (var j = 0; j < this.nIntPerSector; j++) {
this.SSAT[nSSID++] = this.readInt ();
}
thisSID = this.SAT[thisSID];
}
} catch (e) {
if (Clazz.exceptionOf (e, Exception)) {
J.util.Logger.errorEx (null, e);
} else {
throw e;
}
}
}, $fz.isPrivate = true, $fz));
$_M(c$, "getDirectoryTable", 
($fz = function () {
var thisSID = this.header.SID_DIR_start;
var thisEntry;
this.rootEntry = null;
try {
while (thisSID > 0) {
this.gotoSector (thisSID);
for (var j = this.nDirEntriesperSector; --j >= 0; ) {
thisEntry =  new J.io2.CompoundDocDirEntry (this);
thisEntry.readData ();
if (thisEntry.lenStream > 0) {
this.directory.addLast (thisEntry);
}if (thisEntry.entryType == 5) this.rootEntry = thisEntry;
}
thisSID = this.SAT[thisSID];
}
} catch (e) {
if (Clazz.exceptionOf (e, Exception)) {
J.util.Logger.errorEx (null, e);
} else {
throw e;
}
}
if (J.util.Logger.debugging) J.util.Logger.debug ("CompoundDocument directory entry: \n" + this.getDirectoryListing ("\n"));
}, $fz.isPrivate = true, $fz));
$_M(c$, "getEntryAsString", 
($fz = function (thisEntry, asBinaryString) {
if (thisEntry.isEmpty) return  new J.util.SB ();
return (thisEntry.isStandard ? this.getStandardStringData (thisEntry.SIDfirstSector, thisEntry.lenStream, asBinaryString) : this.getShortStringData (thisEntry.SIDfirstSector, thisEntry.lenStream, asBinaryString));
}, $fz.isPrivate = true, $fz), "J.io2.CompoundDocDirEntry,~B");
$_M(c$, "getStandardStringData", 
($fz = function (thisSID, nBytes, asBinaryString) {
var data =  new J.util.SB ();
var byteBuf =  Clazz.newByteArray (this.sectorSize, 0);
var gzipData =  new J.io2.ZipData (nBytes);
try {
while (thisSID > 0 && nBytes > 0) {
this.gotoSector (thisSID);
nBytes = this.getSectorData (data, byteBuf, this.sectorSize, nBytes, asBinaryString, gzipData);
thisSID = this.SAT[thisSID];
}
if (nBytes == -9999) return  new J.util.SB ();
} catch (e) {
if (Clazz.exceptionOf (e, Exception)) {
J.util.Logger.errorEx (null, e);
} else {
throw e;
}
}
if (gzipData.isEnabled) gzipData.addTo (data);
return data;
}, $fz.isPrivate = true, $fz), "~N,~N,~B");
$_M(c$, "getSectorData", 
($fz = function (data, byteBuf, nSectorBytes, nBytes, asBinaryString, gzipData) {
this.readByteArray (byteBuf, 0, byteBuf.length);
var n = gzipData.addBytes (byteBuf, nSectorBytes, nBytes);
if (n >= 0) return n;
if (asBinaryString) {
for (var i = 0; i < nSectorBytes; i++) {
data.append (Integer.toHexString (byteBuf[i] & 0xFF)).appendC (' ');
if (--nBytes < 1) break;
}
} else {
for (var i = 0; i < nSectorBytes; i++) {
if (byteBuf[i] == 0) return -9999;
data.appendC (String.fromCharCode (byteBuf[i]));
if (--nBytes < 1) break;
}
}return nBytes;
}, $fz.isPrivate = true, $fz), "J.util.SB,~A,~N,~N,~B,J.io2.ZipData");
$_M(c$, "getShortStringData", 
($fz = function (shortSID, nBytes, asBinaryString) {
var data =  new J.util.SB ();
if (this.rootEntry == null) return data;
var thisSID = this.rootEntry.SIDfirstSector;
var ptShort = 0;
var byteBuf =  Clazz.newByteArray (this.shortSectorSize, 0);
var gzipData =  new J.io2.ZipData (nBytes);
try {
while (thisSID >= 0 && shortSID >= 0 && nBytes > 0) {
while (shortSID - ptShort >= this.nShortSectorsPerStandardSector) {
ptShort += this.nShortSectorsPerStandardSector;
thisSID = this.SAT[thisSID];
}
this.seek (this.getOffset (thisSID) + (shortSID - ptShort) * this.shortSectorSize);
nBytes = this.getSectorData (data, byteBuf, this.shortSectorSize, nBytes, asBinaryString, gzipData);
shortSID = this.SSAT[shortSID];
}
} catch (e) {
if (Clazz.exceptionOf (e, Exception)) {
J.util.Logger.error (data.toString ());
J.util.Logger.errorEx (null, e);
} else {
throw e;
}
}
if (gzipData.isEnabled) gzipData.addTo (data);
return data;
}, $fz.isPrivate = true, $fz), "~N,~N,~B");
});
