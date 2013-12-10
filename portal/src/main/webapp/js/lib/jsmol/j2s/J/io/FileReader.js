Clazz.declarePackage ("J.io");
Clazz.load (null, "J.io.FileReader", ["java.io.BufferedReader", "J.api.Interface", "J.io.JmolBinary", "J.util.Escape", "$.Logger", "$.TextFormat"], function () {
c$ = Clazz.decorateAsClass (function () {
this.fm = null;
this.viewer = null;
this.fileNameIn = null;
this.fullPathNameIn = null;
this.nameAsGivenIn = null;
this.fileTypeIn = null;
this.atomSetCollection = null;
this.reader = null;
this.htParams = null;
this.isAppend = false;
this.bytes = null;
Clazz.instantialize (this, arguments);
}, J.io, "FileReader");
Clazz.makeConstructor (c$, 
function (fileManager, viewer, fileName, fullPathName, nameAsGiven, type, reader, htParams, isAppend) {
this.fm = fileManager;
this.viewer = viewer;
this.fileNameIn = fileName;
this.fullPathNameIn = fullPathName;
this.nameAsGivenIn = nameAsGiven;
this.fileTypeIn = type;
this.reader = (Clazz.instanceOf (reader, java.io.BufferedReader) ? reader : Clazz.instanceOf (reader, java.io.Reader) ?  new java.io.BufferedReader (reader) : null);
this.bytes = (J.util.Escape.isAB (reader) ? reader : null);
this.htParams = htParams;
this.isAppend = isAppend;
}, "J.viewer.FileManager,J.viewer.Viewer,~S,~S,~S,~S,~O,java.util.Map,~B");
$_M(c$, "run", 
function () {
if (!this.isAppend && this.viewer.displayLoadErrors) this.viewer.zap (false, true, false);
var errorMessage = null;
var t = null;
if (this.reader == null) {
t = this.fm.getUnzippedReaderOrStreamFromName (this.fullPathNameIn, this.bytes, true, false, false, true, this.htParams);
if (t == null || Clazz.instanceOf (t, String)) {
errorMessage = (t == null ? "error opening:" + this.nameAsGivenIn : t);
if (!errorMessage.startsWith ("NOTE:")) J.util.Logger.error ("file ERROR: " + this.fullPathNameIn + "\n" + errorMessage);
this.atomSetCollection = errorMessage;
return;
}if (Clazz.instanceOf (t, java.io.BufferedReader)) {
this.reader = t;
} else if (Clazz.instanceOf (t, J.api.ZInputStream)) {
var name = this.fullPathNameIn;
var subFileList = null;
if (name.indexOf ("|") >= 0 && !name.endsWith (".zip")) {
subFileList = J.util.TextFormat.splitChars (name, "|");
name = subFileList[0];
}if (subFileList != null) this.htParams.put ("subFileList", subFileList);
var zis = t;
var zipDirectory = this.fm.getZipDirectory (name, true);
this.atomSetCollection = t = J.io.JmolBinary.getAtomSetCollectionOrBufferedReaderFromZip (this.viewer.getModelAdapter (), zis, name, zipDirectory, this.htParams, false);
try {
zis.close ();
} catch (e) {
if (Clazz.exceptionOf (e, Exception)) {
} else {
throw e;
}
}
}}if (Clazz.instanceOf (t, java.io.BufferedInputStream)) {
var bd = J.api.Interface.getOptionInterface ("io2.BinaryDocument");
bd.setStream (t, true);
this.reader = bd;
}if (this.reader != null) {
this.atomSetCollection = this.viewer.getModelAdapter ().getAtomSetCollectionReader (this.fullPathNameIn, this.fileTypeIn, this.reader, this.htParams);
if (!(Clazz.instanceOf (this.atomSetCollection, String))) this.atomSetCollection = this.viewer.getModelAdapter ().getAtomSetCollection (this.atomSetCollection);
try {
if (Clazz.instanceOf (this.reader, java.io.BufferedReader)) (this.reader).close ();
 else if (Clazz.instanceOf (this.reader, J.api.JmolDocument)) (this.reader).close ();
} catch (e) {
if (Clazz.exceptionOf (e, java.io.IOException)) {
} else {
throw e;
}
}
}if (Clazz.instanceOf (this.atomSetCollection, String)) return;
if (!this.isAppend && !this.viewer.displayLoadErrors) this.viewer.zap (false, true, false);
this.fm.fullPathName = this.fullPathNameIn;
this.fm.nameAsGiven = this.nameAsGivenIn;
this.fm.fileName = this.fileNameIn;
});
$_M(c$, "getAtomSetCollection", 
function () {
return this.atomSetCollection;
});
});
