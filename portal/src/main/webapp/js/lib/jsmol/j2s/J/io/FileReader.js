Clazz.declarePackage ("J.io");
Clazz.load (null, "J.io.FileReader", ["java.io.BufferedInputStream", "$.BufferedReader", "$.Reader", "javajs.api.GenericBinaryDocument", "$.ZInputStream", "JU.PT", "J.api.Interface", "J.io.JmolBinary", "JU.Logger"], function () {
c$ = Clazz.decorateAsClass (function () {
this.fm = null;
this.vwr = null;
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
function (fileManager, vwr, fileName, fullPathName, nameAsGiven, type, reader, htParams, isAppend) {
this.fm = fileManager;
this.vwr = vwr;
this.fileNameIn = fileName;
this.fullPathNameIn = fullPathName;
this.nameAsGivenIn = nameAsGiven;
this.fileTypeIn = type;
this.reader = (Clazz.instanceOf (reader, java.io.BufferedReader) ? reader : Clazz.instanceOf (reader, java.io.Reader) ?  new java.io.BufferedReader (reader) : null);
this.bytes = (JU.PT.isAB (reader) ? reader : null);
this.htParams = htParams;
this.isAppend = isAppend;
}, "JV.FileManager,JV.Viewer,~S,~S,~S,~S,~O,java.util.Map,~B");
Clazz.defineMethod (c$, "run", 
function () {
if (!this.isAppend && this.vwr.displayLoadErrors) this.vwr.zap (false, true, false);
var errorMessage = null;
var t = null;
if (this.reader == null) {
t = this.fm.getUnzippedReaderOrStreamFromName (this.fullPathNameIn, this.bytes, true, false, false, true, this.htParams);
if (t == null || Clazz.instanceOf (t, String)) {
errorMessage = (t == null ? "error opening:" + this.nameAsGivenIn : t);
if (!errorMessage.startsWith ("NOTE:")) JU.Logger.error ("file ERROR: " + this.fullPathNameIn + "\n" + errorMessage);
this.atomSetCollection = errorMessage;
return;
}if (Clazz.instanceOf (t, java.io.BufferedReader)) {
this.reader = t;
} else if (Clazz.instanceOf (t, javajs.api.ZInputStream)) {
var name = this.fullPathNameIn;
var subFileList = null;
name = name.$replace ('\\', '/');
if (name.indexOf ("|") >= 0 && !name.endsWith (".zip")) {
subFileList = JU.PT.split (name, "|");
name = subFileList[0];
}if (subFileList != null) this.htParams.put ("subFileList", subFileList);
var zis = t;
var zipDirectory = this.fm.getZipDirectory (name, true, true);
this.atomSetCollection = t = J.io.JmolBinary.getAtomSetCollectionOrBufferedReaderFromZip (this.vwr.getModelAdapter (), zis, name, zipDirectory, this.htParams, false);
try {
zis.close ();
} catch (e) {
if (Clazz.exceptionOf (e, Exception)) {
} else {
throw e;
}
}
}}if (Clazz.instanceOf (t, java.io.BufferedInputStream)) {
var bd = J.api.Interface.getInterface ("JU.BinaryDocument");
bd.setStream (t, true);
this.reader = bd;
}if (this.reader != null) {
this.atomSetCollection = this.vwr.getModelAdapter ().getAtomSetCollectionReader (this.fullPathNameIn, this.fileTypeIn, this.reader, this.htParams);
if (!(Clazz.instanceOf (this.atomSetCollection, String))) this.atomSetCollection = this.vwr.getModelAdapter ().getAtomSetCollection (this.atomSetCollection);
try {
if (Clazz.instanceOf (this.reader, java.io.BufferedReader)) (this.reader).close ();
 else if (Clazz.instanceOf (this.reader, javajs.api.GenericBinaryDocument)) (this.reader).close ();
} catch (e) {
if (Clazz.exceptionOf (e, java.io.IOException)) {
} else {
throw e;
}
}
}if (Clazz.instanceOf (this.atomSetCollection, String)) return;
if (!this.isAppend && !this.vwr.displayLoadErrors) this.vwr.zap (false, true, false);
this.fm.setFileInfo ([this.fullPathNameIn, this.fileNameIn, this.nameAsGivenIn]);
});
Clazz.defineMethod (c$, "getAtomSetCollection", 
function () {
return this.atomSetCollection;
});
});
