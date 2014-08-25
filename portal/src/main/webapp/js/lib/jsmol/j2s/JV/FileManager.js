Clazz.declarePackage ("JV");
Clazz.load (["javajs.api.BytePoster", "java.util.Hashtable"], "JV.FileManager", ["java.io.BufferedInputStream", "$.BufferedReader", "java.lang.Boolean", "java.net.URL", "$.URLEncoder", "java.util.Map", "JU.AU", "$.BArray", "$.Base64", "$.Lst", "$.PT", "$.Rdr", "$.SB", "J.api.Interface", "J.io.FileReader", "$.JmolBinary", "JS.SV", "JU.Logger", "$.Txt", "JV.Viewer"], function () {
c$ = Clazz.decorateAsClass (function () {
this.vwr = null;
this.jmb = null;
this.pathForAllFiles = "";
this.nameAsGiven = "zapped";
this.fullPathName = null;
this.lastFullPathName = null;
this.lastNameAsGiven = "zapped";
this.fileName = null;
this.appletDocumentBaseURL = null;
this.appletProxy = null;
this.cache = null;
Clazz.instantialize (this, arguments);
}, JV, "FileManager", null, javajs.api.BytePoster);
Clazz.prepareFields (c$, function () {
this.cache =  new java.util.Hashtable ();
});
Clazz.makeConstructor (c$, 
function (vwr) {
this.vwr = vwr;
this.jmb =  new J.io.JmolBinary (this);
this.clear ();
}, "JV.Viewer");
Clazz.defineMethod (c$, "clear", 
function () {
this.setFileInfo ([this.vwr.getZapName ()]);
this.jmb.spardirCache = null;
});
Clazz.defineMethod (c$, "getSpardirCache", 
function () {
return this.jmb.spardirCache;
});
Clazz.defineMethod (c$, "clearPngjCache", 
function (fileName) {
this.jmb.clearPngjCache (fileName == null ? null : this.getCanonicalName (JU.Rdr.getZipRoot (fileName)));
}, "~S");
Clazz.defineMethod (c$, "setLoadState", 
 function (htParams) {
if (this.vwr.getPreserveState ()) {
htParams.put ("loadState", this.vwr.g.getLoadState (htParams));
}}, "java.util.Map");
Clazz.defineMethod (c$, "getPathForAllFiles", 
function () {
return this.pathForAllFiles;
});
Clazz.defineMethod (c$, "setPathForAllFiles", 
function (value) {
if (value.length > 0 && !value.endsWith ("/") && !value.endsWith ("|")) value += "/";
return this.pathForAllFiles = value;
}, "~S");
Clazz.defineMethod (c$, "setFileInfo", 
function (fileInfo) {
this.fullPathName = fileInfo[0];
this.fileName = fileInfo[Math.min (1, fileInfo.length - 1)];
this.nameAsGiven = fileInfo[Math.min (2, fileInfo.length - 1)];
if (!this.nameAsGiven.equals ("zapped")) {
this.lastNameAsGiven = this.nameAsGiven;
this.lastFullPathName = this.fullPathName;
}}, "~A");
Clazz.defineMethod (c$, "getFileInfo", 
function () {
return [this.fullPathName, this.fileName, this.nameAsGiven];
});
Clazz.defineMethod (c$, "getFullPathName", 
function (orPrevious) {
var f = (this.fullPathName != null ? this.fullPathName : this.nameAsGiven);
return (!orPrevious || !f.equals ("zapped") ? f : this.lastFullPathName != null ? this.lastFullPathName : this.lastNameAsGiven);
}, "~B");
Clazz.defineMethod (c$, "getFileName", 
function () {
return this.fileName != null ? this.fileName : this.nameAsGiven;
});
Clazz.defineMethod (c$, "getAppletDocumentBase", 
function () {
return (this.appletDocumentBaseURL == null ? "" : this.appletDocumentBaseURL.toString ());
});
Clazz.defineMethod (c$, "setAppletContext", 
function (documentBase) {
try {
System.out.println ("setting document base to \"" + documentBase + "\"");
this.appletDocumentBaseURL = (documentBase.length == 0 ? null :  new java.net.URL (Clazz.castNullAs ("java.net.URL"), documentBase, null));
} catch (e) {
if (Clazz.exceptionOf (e, java.net.MalformedURLException)) {
System.out.println ("error setting document base to " + documentBase);
} else {
throw e;
}
}
}, "~S");
Clazz.defineMethod (c$, "setAppletProxy", 
function (appletProxy) {
this.appletProxy = (appletProxy == null || appletProxy.length == 0 ? null : appletProxy);
}, "~S");
Clazz.defineMethod (c$, "createAtomSetCollectionFromFile", 
function (name, htParams, isAppend) {
if (htParams.get ("atomDataOnly") == null) {
this.setLoadState (htParams);
}name = this.vwr.resolveDatabaseFormat (name);
var pt = name.indexOf ("::");
var nameAsGiven = (pt >= 0 ? name.substring (pt + 2) : name);
var fileType = (pt >= 0 ? name.substring (0, pt) : null);
JU.Logger.info ("\nFileManager.getAtomSetCollectionFromFile(" + nameAsGiven + ")" + (name.equals (nameAsGiven) ? "" : " //" + name));
var names = this.getClassifiedName (nameAsGiven, true);
if (names.length == 1) return names[0];
var fullPathName = names[0];
var fileName = names[1];
htParams.put ("fullPathName", (fileType == null ? "" : fileType + "::") + fullPathName.$replace ('\\', '/'));
if (this.vwr.getBoolean (603979880) && this.vwr.getBoolean (603979825)) this.vwr.scriptStatus ("Requesting " + fullPathName);
var fileReader =  new J.io.FileReader (this, this.vwr, fileName, fullPathName, nameAsGiven, fileType, null, htParams, isAppend);
fileReader.run ();
return fileReader.getAtomSetCollection ();
}, "~S,java.util.Map,~B");
Clazz.defineMethod (c$, "createAtomSetCollectionFromFiles", 
function (fileNames, htParams, isAppend) {
this.setLoadState (htParams);
var fullPathNames =  new Array (fileNames.length);
var namesAsGiven =  new Array (fileNames.length);
var fileTypes =  new Array (fileNames.length);
for (var i = 0; i < fileNames.length; i++) {
var pt = fileNames[i].indexOf ("::");
var nameAsGiven = (pt >= 0 ? fileNames[i].substring (pt + 2) : fileNames[i]);
var fileType = (pt >= 0 ? fileNames[i].substring (0, pt) : null);
var names = this.getClassifiedName (nameAsGiven, true);
if (names.length == 1) return names[0];
fullPathNames[i] = names[0];
fileNames[i] = names[0].$replace ('\\', '/');
fileTypes[i] = fileType;
namesAsGiven[i] = nameAsGiven;
}
htParams.put ("fullPathNames", fullPathNames);
htParams.put ("fileTypes", fileTypes);
var filesReader = this.newFilesReader (fullPathNames, namesAsGiven, fileTypes, null, htParams, isAppend);
filesReader.run ();
return filesReader.getAtomSetCollection ();
}, "~A,java.util.Map,~B");
Clazz.defineMethod (c$, "createAtomSetCollectionFromString", 
function (strModel, htParams, isAppend) {
this.setLoadState (htParams);
var isAddH = (strModel.indexOf ("Viewer.AddHydrogens") >= 0);
var fnames = (isAddH ? this.getFileInfo () : null);
var fileReader =  new J.io.FileReader (this, this.vwr, "string", "string", "string", null, JU.Rdr.getBR (strModel), htParams, isAppend);
fileReader.run ();
if (fnames != null) this.setFileInfo (fnames);
if (!isAppend && !(Clazz.instanceOf (fileReader.getAtomSetCollection (), String))) {
this.vwr.zap (false, true, false);
this.setFileInfo ([strModel === "5\n\nC 0 0 0\nH .63 .63 .63\nH -.63 -.63 .63\nH -.63 .63 -.63\nH .63 -.63 -.63" ? "Jmol Model Kit" : "string"]);
}return fileReader.getAtomSetCollection ();
}, "~S,java.util.Map,~B");
Clazz.defineMethod (c$, "createAtomSeCollectionFromStrings", 
function (arrayModels, loadScript, htParams, isAppend) {
if (!htParams.containsKey ("isData")) {
var oldSep = "\"" + this.vwr.getDataSeparator () + "\"";
var tag = "\"" + (isAppend ? "append" : "model") + " inline\"";
var sb =  new JU.SB ();
sb.append ("set dataSeparator \"~~~next file~~~\";\ndata ").append (tag);
for (var i = 0; i < arrayModels.length; i++) {
if (i > 0) sb.append ("~~~next file~~~");
sb.append (arrayModels[i]);
}
sb.append ("end ").append (tag).append (";set dataSeparator ").append (oldSep);
loadScript.appendSB (sb);
}this.setLoadState (htParams);
JU.Logger.info ("FileManager.getAtomSetCollectionFromStrings(string[])");
var fullPathNames =  new Array (arrayModels.length);
var readers =  new Array (arrayModels.length);
for (var i = 0; i < arrayModels.length; i++) {
fullPathNames[i] = "string[" + i + "]";
readers[i] = JV.FileManager.newDataReader (arrayModels[i]);
}
var filesReader = this.newFilesReader (fullPathNames, fullPathNames, null, readers, htParams, isAppend);
filesReader.run ();
return filesReader.getAtomSetCollection ();
}, "~A,JU.SB,java.util.Map,~B");
Clazz.defineMethod (c$, "createAtomSeCollectionFromArrayData", 
function (arrayData, htParams, isAppend) {
JU.Logger.info ("FileManager.getAtomSetCollectionFromArrayData(Vector)");
var nModels = arrayData.size ();
var fullPathNames =  new Array (nModels);
var readers =  new Array (nModels);
for (var i = 0; i < nModels; i++) {
fullPathNames[i] = "String[" + i + "]";
readers[i] = JV.FileManager.newDataReader (arrayData.get (i));
}
var filesReader = this.newFilesReader (fullPathNames, fullPathNames, null, readers, htParams, isAppend);
filesReader.run ();
return filesReader.getAtomSetCollection ();
}, "JU.Lst,java.util.Map,~B");
c$.newDataReader = Clazz.defineMethod (c$, "newDataReader", 
function (data) {
var reader = (Clazz.instanceOf (data, String) ? "String" : JU.PT.isAS (data) ? "Array" : Clazz.instanceOf (data, JU.Lst) ? "List" : null);
if (reader == null) return null;
var dr = J.api.Interface.getInterface ("JU." + reader + "DataReader");
return dr.setData (data);
}, "~O");
Clazz.defineMethod (c$, "newFilesReader", 
 function (fullPathNames, namesAsGiven, fileTypes, readers, htParams, isAppend) {
var fr = J.api.Interface.getOption ("io.FilesReader");
fr.set (this, this.vwr, fullPathNames, namesAsGiven, fileTypes, readers, htParams, isAppend);
return fr;
}, "~A,~A,~A,~A,java.util.Map,~B");
Clazz.defineMethod (c$, "createAtomSetCollectionFromDOM", 
function (DOMNode, htParams) {
var aDOMReader = J.api.Interface.getOption ("io.DOMReadaer");
aDOMReader.set (this, this.vwr, DOMNode, htParams);
aDOMReader.run ();
return aDOMReader.getAtomSetCollection ();
}, "~O,java.util.Map");
Clazz.defineMethod (c$, "createAtomSetCollectionFromReader", 
function (fullPathName, name, reader, htParams) {
var fileReader =  new J.io.FileReader (this, this.vwr, name, fullPathName, name, null, reader, htParams, false);
fileReader.run ();
return fileReader.getAtomSetCollection ();
}, "~S,~S,~O,java.util.Map");
Clazz.defineMethod (c$, "getBufferedInputStream", 
function (fullPathName) {
var ret = this.getBufferedReaderOrErrorMessageFromName (fullPathName,  new Array (2), true, true);
return (Clazz.instanceOf (ret, java.io.BufferedInputStream) ? ret : null);
}, "~S");
Clazz.defineMethod (c$, "getBufferedInputStreamOrErrorMessageFromName", 
function (name, fullName, showMsg, checkOnly, outputBytes, allowReader, allowCached) {
var cacheBytes = null;
if (allowCached && outputBytes == null) {
cacheBytes = (fullName == null || this.jmb.pngjCache == null ? null : this.getCachedPngjBytes (fullName));
if (cacheBytes == null) cacheBytes = this.cacheGet (name, true);
}var bis = null;
var ret = null;
var errorMessage = null;
try {
if (cacheBytes == null) {
var isPngjBinaryPost = (name.indexOf ("?POST?_PNGJBIN_") >= 0);
var isPngjPost = (isPngjBinaryPost || name.indexOf ("?POST?_PNGJ_") >= 0);
if (name.indexOf ("?POST?_PNG_") > 0 || isPngjPost) {
var errMsg =  new Array (1);
var bytes = this.vwr.getImageAsBytes (isPngjPost ? "PNGJ" : "PNG", 0, 0, -1, errMsg);
if (errMsg[0] != null) return errMsg[0];
if (isPngjBinaryPost) {
outputBytes = bytes;
name = JU.PT.rep (name, "?_", "=_");
} else {
name =  new JU.SB ().append (name).append ("=").appendSB (JU.Base64.getBase64 (bytes)).toString ();
}}var iurl = JV.FileManager.urlTypeIndex (name);
var isURL = (iurl >= 0);
var post = null;
if (isURL && (iurl = name.indexOf ("?POST?")) >= 0) {
post = name.substring (iurl + 6);
name = name.substring (0, iurl);
}var isApplet = (this.appletDocumentBaseURL != null);
if (allowCached && name.indexOf (".png") >= 0 && this.jmb.pngjCache == null && this.vwr.cachePngFiles ()) this.jmb.clearAndCachePngjFile (null);
if (isApplet || isURL) {
if (isApplet && isURL && this.appletProxy != null) name = this.appletProxy + "?url=" + this.urlEncode (name);
var url = (isApplet ?  new java.net.URL (this.appletDocumentBaseURL, name, null) :  new java.net.URL (Clazz.castNullAs ("java.net.URL"), name, null));
if (checkOnly) return null;
name = url.toString ();
if (showMsg && name.toLowerCase ().indexOf ("password") < 0) JU.Logger.info ("FileManager opening 1 " + name);
ret = this.vwr.apiPlatform.getURLContents (url, outputBytes, post, false);
var bytes = null;
if (Clazz.instanceOf (ret, JU.SB)) {
var sb = ret;
if (allowReader && !JU.Rdr.isBase64 (sb)) return JU.Rdr.getBR (sb.toString ());
bytes = JU.Rdr.getBytesFromSB (sb);
} else if (JU.PT.isAB (ret)) {
bytes = ret;
}if (bytes != null) ret = JU.Rdr.getBIS (bytes);
} else if (!allowCached || (cacheBytes = this.cacheGet (name, true)) == null) {
if (showMsg) JU.Logger.info ("FileManager opening 2 " + name);
ret = this.vwr.apiPlatform.getBufferedFileInputStream (name);
}if (Clazz.instanceOf (ret, String)) return ret;
}bis = (cacheBytes == null ? ret : JU.Rdr.getBIS (cacheBytes));
if (checkOnly) {
bis.close ();
bis = null;
}return bis;
} catch (e) {
if (Clazz.exceptionOf (e, Exception)) {
try {
if (bis != null) bis.close ();
} catch (e1) {
if (Clazz.exceptionOf (e1, java.io.IOException)) {
} else {
throw e1;
}
}
errorMessage = "" + e;
} else {
throw e;
}
}
return errorMessage;
}, "~S,~S,~B,~B,~A,~B,~B");
Clazz.defineMethod (c$, "urlEncode", 
 function (name) {
try {
return java.net.URLEncoder.encode (name, "utf-8");
} catch (e) {
if (Clazz.exceptionOf (e, java.io.UnsupportedEncodingException)) {
return name;
} else {
throw e;
}
}
}, "~S");
Clazz.defineMethod (c$, "getEmbeddedFileState", 
function (fileName, allowCached) {
var dir = null;
dir = this.getZipDirectory (fileName, false, allowCached);
if (dir.length == 0) {
var state = this.vwr.getFileAsString4 (fileName, -1, false, true, false);
return (state.indexOf ("**** Jmol Embedded Script ****") < 0 ? "" : J.io.JmolBinary.getEmbeddedScript (state));
}for (var i = 0; i < dir.length; i++) if (dir[i].indexOf (".spt") >= 0) {
var data = [fileName + "|" + dir[i], null];
this.getFileDataOrErrorAsString (data, -1, false, false, false);
return data[1];
}
return "";
}, "~S,~B");
Clazz.defineMethod (c$, "getFullPathNameOrError", 
function (filename, getStream, ret) {
var names = this.getClassifiedName (filename, true);
if (names == null || names[0] == null || names.length < 2) return [null, "cannot read file name: " + filename];
var name = names[0];
var fullPath = names[0].$replace ('\\', '/');
name = JU.Rdr.getZipRoot (name);
var errMsg = this.getBufferedInputStreamOrErrorMessageFromName (name, fullPath, false, !getStream, null, false, !getStream);
ret[0] = fullPath;
if (Clazz.instanceOf (errMsg, String)) ret[1] = errMsg;
return errMsg;
}, "~S,~B,~A");
Clazz.defineMethod (c$, "getBufferedReaderOrErrorMessageFromName", 
function (name, fullPathNameReturn, isBinary, doSpecialLoad) {
var data = this.cacheGet (name, false);
var isBytes = JU.PT.isAB (data);
var bytes = (isBytes ? data : null);
if (name.startsWith ("cache://")) {
if (data == null) return "cannot read " + name;
if (isBytes) {
bytes = data;
} else {
return JU.Rdr.getBR (data);
}}var names = this.getClassifiedName (name, true);
if (names == null) return "cannot read file name: " + name;
if (fullPathNameReturn != null) fullPathNameReturn[0] = names[0].$replace ('\\', '/');
return this.getUnzippedReaderOrStreamFromName (names[0], bytes, false, isBinary, false, doSpecialLoad, null);
}, "~S,~A,~B,~B");
Clazz.defineMethod (c$, "getUnzippedReaderOrStreamFromName", 
function (name, bytes, allowZipStream, forceInputStream, isTypeCheckOnly, doSpecialLoad, htParams) {
var subFileList = null;
var info = (bytes == null && doSpecialLoad ? this.getSpartanFileList (name) : null);
var name00 = name;
if (info != null) {
if (isTypeCheckOnly) return info;
if (info[2] != null) {
var header = info[1];
var fileData =  new java.util.Hashtable ();
if (info.length == 3) {
var name0 = this.getObjectAsSections (info[2], header, fileData);
fileData.put ("OUTPUT", name0);
info = J.io.JmolBinary.spartanFileList (name, fileData.get (name0));
if (info.length == 3) {
name0 = this.getObjectAsSections (info[2], header, fileData);
fileData.put ("OUTPUT", name0);
info = J.io.JmolBinary.spartanFileList (info[1], fileData.get (name0));
}}var sb =  new JU.SB ();
if (fileData.get ("OUTPUT") != null) sb.append (fileData.get (fileData.get ("OUTPUT")));
var s;
for (var i = 2; i < info.length; i++) {
name = info[i];
name = this.getObjectAsSections (name, header, fileData);
JU.Logger.info ("reading " + name);
s = fileData.get (name);
sb.append (s);
}
s = sb.toString ();
this.jmb.spardirPut (name00.$replace ('\\', '/'), s.getBytes ());
return JU.Rdr.getBR (s);
}}if (bytes == null && this.jmb.pngjCache != null) {
bytes = this.getCachedPngjBytes (name);
if (bytes != null && htParams != null) htParams.put ("sourcePNGJ", Boolean.TRUE);
}var fullName = name;
if (name.indexOf ("|") >= 0) {
subFileList = JU.PT.split (name.$replace ('\\', '/'), "|");
if (bytes == null) JU.Logger.info ("FileManager opening 3 " + name);
name = subFileList[0];
}var t = (bytes == null ? this.getBufferedInputStreamOrErrorMessageFromName (name, fullName, true, false, null, !forceInputStream, true) : JU.Rdr.getBIS (bytes));
try {
if (Clazz.instanceOf (t, String)) return t;
if (Clazz.instanceOf (t, java.io.BufferedReader)) return t;
var bis = JU.Rdr.getUnzippedInputStream (t);
if (JU.Rdr.isCompoundDocumentS (bis)) {
var doc = J.api.Interface.getInterface ("JU.CompoundDocument");
doc.setStream (bis, true);
return JU.Rdr.getBR (doc.getAllDataFiles ("Molecule", "Input").toString ());
}if (JU.Rdr.isPickleS (bis)) return bis;
bis = JU.Rdr.getPngZipStream (bis, true);
if (JU.Rdr.isZipS (bis)) {
if (allowZipStream) return JU.Rdr.newZipInputStream (bis);
var o = JU.Rdr.getZipFileDirectory (bis, subFileList, 1, forceInputStream);
return (Clazz.instanceOf (o, String) ? JU.Rdr.getBR (o) : o);
}return (forceInputStream ? bis : JU.Rdr.getBufferedReader (bis, null));
} catch (ioe) {
if (Clazz.exceptionOf (ioe, Exception)) {
return ioe.toString ();
} else {
throw ioe;
}
}
}, "~S,~A,~B,~B,~B,~B,java.util.Map");
Clazz.defineMethod (c$, "getSpartanFileList", 
 function (name) {
if (name.endsWith (".spt")) return [null, null, null];
if (name.endsWith (".spardir.zip")) return ["SpartanSmol", "Directory Entry ", name + "|output"];
name = name.$replace ('\\', '/');
if (!name.endsWith (".spardir") && name.indexOf (".spardir/") < 0) return null;
var pt = name.lastIndexOf (".spardir");
if (pt < 0) return null;
if (name.lastIndexOf ("/") > pt) {
return ["SpartanSmol", "Directory Entry ", name + "/input", name + "/archive", name + "/Molecule:asBinaryString", name + "/proparc"];
}return ["SpartanSmol", "Directory Entry ", name + "/output"];
}, "~S");
Clazz.defineMethod (c$, "getObjectAsSections", 
 function (name, header, fileData) {
if (name == null) return null;
var subFileList = null;
var asBinaryString = false;
var name0 = name.$replace ('\\', '/');
if (name.indexOf (":asBinaryString") >= 0) {
asBinaryString = true;
name = name.substring (0, name.indexOf (":asBinaryString"));
}var sb = null;
if (fileData.containsKey (name0)) return name0;
if (name.indexOf ("#JMOL_MODEL ") >= 0) {
fileData.put (name0, name0 + "\n");
return name0;
}var fullName = name;
if (name.indexOf ("|") >= 0) {
subFileList = JU.PT.split (name, "|");
name = subFileList[0];
}var bis = null;
try {
var t = this.getBufferedInputStreamOrErrorMessageFromName (name, fullName, false, false, null, false, true);
if (Clazz.instanceOf (t, String)) {
fileData.put (name0, t + "\n");
return name0;
}bis = t;
if (JU.Rdr.isCompoundDocumentS (bis)) {
var doc = J.api.Interface.getInterface ("JU.CompoundDocument");
doc.setStream (bis, true);
doc.getAllDataMapped (name.$replace ('\\', '/'), "Molecule", fileData);
} else if (JU.Rdr.isZipS (bis)) {
JU.Rdr.getAllZipData (bis, subFileList, name.$replace ('\\', '/'), "Molecule", fileData);
} else if (asBinaryString) {
var bd = J.api.Interface.getInterface ("JU.BinaryDocument");
bd.setStream (bis, false);
sb =  new JU.SB ();
if (header != null) sb.append ("BEGIN Directory Entry " + name0 + "\n");
try {
while (true) sb.append (Integer.toHexString (bd.readByte () & 0xFF)).appendC (' ');

} catch (e1) {
if (Clazz.exceptionOf (e1, Exception)) {
sb.appendC ('\n');
} else {
throw e1;
}
}
if (header != null) sb.append ("\nEND Directory Entry " + name0 + "\n");
fileData.put (name0, sb.toString ());
} else {
var br = JU.Rdr.getBufferedReader (JU.Rdr.isGzipS (bis) ?  new java.io.BufferedInputStream (JU.Rdr.newGZIPInputStream (bis)) : bis, null);
var line;
sb =  new JU.SB ();
if (header != null) sb.append ("BEGIN Directory Entry " + name0 + "\n");
while ((line = br.readLine ()) != null) {
sb.append (line);
sb.appendC ('\n');
}
br.close ();
if (header != null) sb.append ("\nEND Directory Entry " + name0 + "\n");
fileData.put (name0, sb.toString ());
}} catch (ioe) {
if (Clazz.exceptionOf (ioe, Exception)) {
fileData.put (name0, ioe.toString ());
} else {
throw ioe;
}
}
if (bis != null) try {
bis.close ();
} catch (e) {
if (Clazz.exceptionOf (e, Exception)) {
} else {
throw e;
}
}
if (!fileData.containsKey (name0)) fileData.put (name0, "FILE NOT FOUND: " + name0 + "\n");
return name0;
}, "~S,~S,java.util.Map");
Clazz.defineMethod (c$, "getZipDirectory", 
function (fileName, addManifest, allowCached) {
var t = this.getBufferedInputStreamOrErrorMessageFromName (fileName, fileName, false, false, null, false, allowCached);
return JU.Rdr.getZipDirectoryAndClose (t, addManifest ? "JmolManifest" : null);
}, "~S,~B,~B");
Clazz.defineMethod (c$, "getFileAsBytes", 
function (name, out, allowZip) {
if (name == null) return null;
var fullName = name;
var subFileList = null;
if (name.indexOf ("|") >= 0) {
subFileList = JU.PT.split (name, "|");
name = subFileList[0];
allowZip = true;
}var t = this.getBufferedInputStreamOrErrorMessageFromName (name, fullName, false, false, null, false, true);
if (Clazz.instanceOf (t, String)) return "Error:" + t;
try {
var bis = t;
var bytes = (out != null || !allowZip || subFileList == null || subFileList.length <= 1 || !JU.Rdr.isZipS (bis) && !JU.Rdr.isPngZipStream (bis) ? JU.Rdr.getStreamAsBytes (bis, out) : JU.Rdr.getZipFileContentsAsBytes (bis, subFileList, 1));
bis.close ();
return bytes;
} catch (ioe) {
if (Clazz.exceptionOf (ioe, Exception)) {
return ioe.toString ();
} else {
throw ioe;
}
}
}, "~S,JU.OC,~B");
Clazz.defineMethod (c$, "getFileAsMap", 
function (name) {
var bdata =  new java.util.Hashtable ();
var t;
if (name == null) {
var errMsg =  new Array (1);
var bytes = this.vwr.getImageAsBytes ("PNGJ", -1, -1, -1, errMsg);
if (errMsg[0] != null) {
bdata.put ("_ERROR_", errMsg[0]);
return bdata;
}t = JU.Rdr.getBIS (bytes);
} else {
var data =  new Array (2);
t = this.getFullPathNameOrError (name, true, data);
if (Clazz.instanceOf (t, String)) {
bdata.put ("_ERROR_", t);
return bdata;
}if (!this.checkSecurity (data[0])) {
bdata.put ("_ERROR_", "java.io. Security exception: cannot read file " + data[0]);
return bdata;
}}try {
JU.Rdr.readFileAsMap (t, bdata, name);
} catch (e) {
if (Clazz.exceptionOf (e, Exception)) {
bdata.clear ();
bdata.put ("_ERROR_", "" + e);
} else {
throw e;
}
}
return bdata;
}, "~S");
Clazz.defineMethod (c$, "getFileDataOrErrorAsString", 
function (data, nBytesMax, doSpecialLoad, allowBinary, checkProtected) {
data[1] = "";
var name = data[0];
if (name == null) return false;
var t = this.getBufferedReaderOrErrorMessageFromName (name, data, false, doSpecialLoad);
if (Clazz.instanceOf (t, String)) {
data[1] = t;
return false;
}if (checkProtected && !this.checkSecurity (data[0])) {
data[1] = "java.io. Security exception: cannot read file " + data[0];
return false;
}try {
return JU.Rdr.readAllAsString (t, nBytesMax, allowBinary, data, 1);
} catch (e) {
if (Clazz.exceptionOf (e, Exception)) {
return false;
} else {
throw e;
}
}
}, "~A,~N,~B,~B,~B");
Clazz.defineMethod (c$, "checkSecurity", 
 function (f) {
if (!f.startsWith ("file:")) return true;
var pt = f.lastIndexOf ('/');
if (f.lastIndexOf (":/") == pt - 1 || f.indexOf ("/.") >= 0 || f.lastIndexOf ('.') < f.lastIndexOf ('/')) return false;
return true;
}, "~S");
Clazz.defineMethod (c$, "loadImage", 
function (nameOrBytes, echoName) {
var image = null;
var nameOrError = null;
var bytes = null;
if (Clazz.instanceOf (nameOrBytes, java.util.Map)) {
if ((nameOrBytes).containsKey ("_DATA_")) nameOrBytes = (nameOrBytes).get ("_DATA_");
 else nameOrBytes = (nameOrBytes).get ("_IMAGE_");
}if (Clazz.instanceOf (nameOrBytes, JS.SV)) nameOrBytes = (nameOrBytes).value;
var name = (Clazz.instanceOf (nameOrBytes, String) ? nameOrBytes : null);
if (name != null && name.startsWith (";base64,")) {
bytes = JU.Base64.decodeBase64 (name);
} else if (Clazz.instanceOf (nameOrBytes, JU.BArray)) {
bytes = (nameOrBytes).data;
} else {
var names = this.getClassifiedName (nameOrBytes, true);
nameOrError = (names == null ? "cannot read file name: " + nameOrBytes : names[0].$replace ('\\', '/'));
if (names != null) image = this.jmb.getImage (this.vwr, nameOrError, echoName);
}if (bytes != null) image = this.jmb.getImage (this.vwr, bytes, echoName);
if (Clazz.instanceOf (image, String)) {
nameOrError = image;
image = null;
}if (!this.vwr.isJS) {
if (image != null && bytes != null) nameOrError = ";base64," + JU.Base64.getBase64 (bytes).toString ();
this.vwr.loadImageData (image, nameOrError, echoName, null);
}}, "~O,~S");
c$.urlTypeIndex = Clazz.defineMethod (c$, "urlTypeIndex", 
function (name) {
if (name == null) return -2;
for (var i = 0; i < JV.FileManager.urlPrefixes.length; ++i) {
if (name.startsWith (JV.FileManager.urlPrefixes[i])) {
return i;
}}
return -1;
}, "~S");
c$.isLocal = Clazz.defineMethod (c$, "isLocal", 
function (fileName) {
if (fileName == null) return false;
var itype = JV.FileManager.urlTypeIndex (fileName);
return (itype < 0 || itype == 4);
}, "~S");
Clazz.defineMethod (c$, "getClassifiedName", 
 function (name, isFullLoad) {
if (name == null) return [null];
var doSetPathForAllFiles = (this.pathForAllFiles.length > 0);
if (name.startsWith ("?") || name.startsWith ("http://?")) {
if ((name = this.vwr.dialogAsk ("Load", name)) == null) return [isFullLoad ? "#CANCELED#" : null];
doSetPathForAllFiles = false;
}var file = null;
var url = null;
var names = null;
if (name.startsWith ("cache://")) {
names =  new Array (3);
names[0] = names[2] = name;
names[1] = JV.FileManager.stripPath (names[0]);
return names;
}name = this.vwr.resolveDatabaseFormat (name);
if (name.indexOf (":") < 0 && name.indexOf ("/") != 0) name = JV.FileManager.addDirectory (this.vwr.getDefaultDirectory (), name);
if (this.appletDocumentBaseURL == null) {
if (JV.FileManager.urlTypeIndex (name) >= 0 || this.vwr.haveAccess (JV.Viewer.ACCESS.NONE) || this.vwr.haveAccess (JV.Viewer.ACCESS.READSPT) && !name.endsWith (".spt") && !name.endsWith ("/")) {
try {
url =  new java.net.URL (Clazz.castNullAs ("java.net.URL"), name, null);
} catch (e) {
if (Clazz.exceptionOf (e, java.net.MalformedURLException)) {
return [isFullLoad ? e.toString () : null];
} else {
throw e;
}
}
} else {
file = this.vwr.apiPlatform.newFile (name);
var s = file.getFullPath ();
var fname = file.getName ();
names = [(s == null ? fname : s), fname, (s == null ? fname : "file:/" + s.$replace ('\\', '/'))];
}} else {
try {
if (name.indexOf (":\\") == 1 || name.indexOf (":/") == 1) name = "file:/" + name;
url =  new java.net.URL (this.appletDocumentBaseURL, name, null);
} catch (e) {
if (Clazz.exceptionOf (e, java.net.MalformedURLException)) {
return [isFullLoad ? e.toString () : null];
} else {
throw e;
}
}
}if (url != null) {
names =  new Array (3);
names[0] = names[2] = url.toString ();
names[1] = JV.FileManager.stripPath (names[0]);
}if (doSetPathForAllFiles) {
var name0 = names[0];
names[0] = this.pathForAllFiles + names[1];
JU.Logger.info ("FileManager substituting " + name0 + " --> " + names[0]);
}if (isFullLoad && (file != null || JV.FileManager.urlTypeIndex (names[0]) == 4)) {
var path = (file == null ? JU.PT.trim (names[0].substring (5), "/") : names[0]);
var pt = path.length - names[1].length - 1;
if (pt > 0) {
path = path.substring (0, pt);
JV.FileManager.setLocalPath (this.vwr, path, true);
}}return names;
}, "~S,~B");
c$.addDirectory = Clazz.defineMethod (c$, "addDirectory", 
 function (defaultDirectory, name) {
if (defaultDirectory.length == 0) return name;
var ch = (name.length > 0 ? name.charAt (0) : ' ');
var s = defaultDirectory.toLowerCase ();
if ((s.endsWith (".zip") || s.endsWith (".tar")) && ch != '|' && ch != '/') defaultDirectory += "|";
return defaultDirectory + (ch == '/' || ch == '/' || (ch = defaultDirectory.charAt (defaultDirectory.length - 1)) == '|' || ch == '/' ? "" : "/") + name;
}, "~S,~S");
Clazz.defineMethod (c$, "getDefaultDirectory", 
function (name) {
var names = this.getClassifiedName (name, true);
if (names == null) return "";
name = JV.FileManager.fixPath (names[0]);
return (name == null ? "" : name.substring (0, name.lastIndexOf ("/")));
}, "~S");
c$.fixPath = Clazz.defineMethod (c$, "fixPath", 
 function (path) {
path = path.$replace ('\\', '/');
path = JU.PT.rep (path, "/./", "/");
var pt = path.lastIndexOf ("//") + 1;
if (pt < 1) pt = path.indexOf (":/") + 1;
if (pt < 1) pt = path.indexOf ("/");
if (pt < 0) return null;
var protocol = path.substring (0, pt);
path = path.substring (pt);
while ((pt = path.lastIndexOf ("/../")) >= 0) {
var pt0 = path.substring (0, pt).lastIndexOf ("/");
if (pt0 < 0) return JU.PT.rep (protocol + path, "/../", "/");
path = path.substring (0, pt0) + path.substring (pt + 3);
}
if (path.length == 0) path = "/";
return protocol + path;
}, "~S");
Clazz.defineMethod (c$, "getFilePath", 
function (name, addUrlPrefix, asShortName) {
var names = this.getClassifiedName (name, false);
return (names == null || names.length == 1 ? "" : asShortName ? names[1] : addUrlPrefix ? names[2] : names[0] == null ? "" : names[0].$replace ('\\', '/'));
}, "~S,~B,~B");
c$.getLocalDirectory = Clazz.defineMethod (c$, "getLocalDirectory", 
function (vwr, forDialog) {
var localDir = vwr.getP (forDialog ? "currentLocalPath" : "defaultDirectoryLocal");
if (forDialog && localDir.length == 0) localDir = vwr.getP ("defaultDirectoryLocal");
if (localDir.length == 0) return (vwr.isApplet () ? null : vwr.apiPlatform.newFile (System.getProperty ("user.dir", ".")));
if (vwr.isApplet () && localDir.indexOf ("file:/") == 0) localDir = localDir.substring (6);
var f = vwr.apiPlatform.newFile (localDir);
try {
return f.isDirectory () ? f : f.getParentAsFile ();
} catch (e) {
if (Clazz.exceptionOf (e, Exception)) {
return null;
} else {
throw e;
}
}
}, "JV.Viewer,~B");
c$.setLocalPath = Clazz.defineMethod (c$, "setLocalPath", 
function (vwr, path, forDialog) {
while (path.endsWith ("/") || path.endsWith ("\\")) path = path.substring (0, path.length - 1);

vwr.setStringProperty ("currentLocalPath", path);
if (!forDialog) vwr.setStringProperty ("defaultDirectoryLocal", path);
}, "JV.Viewer,~S,~B");
c$.getLocalPathForWritingFile = Clazz.defineMethod (c$, "getLocalPathForWritingFile", 
function (vwr, file) {
if (file.startsWith ("http://")) return file;
file = JU.PT.rep (file, "?", "");
if (file.indexOf ("file:/") == 0) return file.substring (6);
if (file.indexOf ("/") == 0 || file.indexOf (":") >= 0) return file;
var dir = null;
try {
dir = JV.FileManager.getLocalDirectory (vwr, false);
} catch (e) {
if (Clazz.exceptionOf (e, Exception)) {
} else {
throw e;
}
}
return (dir == null ? file : JV.FileManager.fixPath (dir.toString () + "/" + file));
}, "JV.Viewer,~S");
c$.setScriptFileReferences = Clazz.defineMethod (c$, "setScriptFileReferences", 
function (script, localPath, remotePath, scriptPath) {
if (localPath != null) script = JV.FileManager.setScriptFileRefs (script, localPath, true);
if (remotePath != null) script = JV.FileManager.setScriptFileRefs (script, remotePath, false);
script = JU.PT.rep (script, "\1\"", "\"");
if (scriptPath != null) {
while (scriptPath.endsWith ("/")) scriptPath = scriptPath.substring (0, scriptPath.length - 1);

for (var ipt = 0; ipt < JV.FileManager.scriptFilePrefixes.length; ipt++) {
var tag = JV.FileManager.scriptFilePrefixes[ipt];
script = JU.PT.rep (script, tag + ".", tag + scriptPath);
}
}return script;
}, "~S,~S,~S,~S");
c$.setScriptFileRefs = Clazz.defineMethod (c$, "setScriptFileRefs", 
 function (script, dataPath, isLocal) {
if (dataPath == null) return script;
var noPath = (dataPath.length == 0);
var fileNames =  new JU.Lst ();
J.io.JmolBinary.getFileReferences (script, fileNames);
var oldFileNames =  new JU.Lst ();
var newFileNames =  new JU.Lst ();
var nFiles = fileNames.size ();
for (var iFile = 0; iFile < nFiles; iFile++) {
var name0 = fileNames.get (iFile);
var name = name0;
if (isLocal == JV.FileManager.isLocal (name)) {
var pt = (noPath ? -1 : name.indexOf ("/" + dataPath + "/"));
if (pt >= 0) {
name = name.substring (pt + 1);
} else {
pt = name.lastIndexOf ("/");
if (pt < 0 && !noPath) name = "/" + name;
if (pt < 0 || noPath) pt++;
name = dataPath + name.substring (pt);
}}JU.Logger.info ("FileManager substituting " + name0 + " --> " + name);
oldFileNames.addLast ("\"" + name0 + "\"");
newFileNames.addLast ("\1\"" + name + "\"");
}
return JU.Txt.replaceStrings (script, oldFileNames, newFileNames);
}, "~S,~S,~B");
c$.stripPath = Clazz.defineMethod (c$, "stripPath", 
function (name) {
var pt = Math.max (name.lastIndexOf ("|"), name.lastIndexOf ("/"));
return name.substring (pt + 1);
}, "~S");
c$.fixFileNameVariables = Clazz.defineMethod (c$, "fixFileNameVariables", 
function (format, fname) {
var str = JU.PT.rep (format, "%FILE", fname);
if (str.indexOf ("%LC") < 0) return str;
fname = fname.toLowerCase ();
str = JU.PT.rep (str, "%LCFILE", fname);
if (fname.length == 4) str = JU.PT.rep (str, "%LC13", fname.substring (1, 3));
return str;
}, "~S,~S");
Clazz.defineMethod (c$, "cachePut", 
function (key, data) {
key = key.$replace ('\\', '/');
if (JU.Logger.debugging) JU.Logger.debug ("cachePut " + key);
if (data == null || "".equals (data)) {
this.cache.remove (key);
return;
}this.cache.put (key, data);
this.getCachedPngjBytes (key);
}, "~S,~O");
Clazz.defineMethod (c$, "getCachedPngjBytes", 
 function (key) {
return this.jmb.getCachedPngjBytes (key);
}, "~S");
Clazz.defineMethod (c$, "cacheGet", 
function (key, bytesOnly) {
key = key.$replace ('\\', '/');
var pt = key.indexOf ("|");
if (pt >= 0) key = key.substring (0, pt);
var data = null;
{
(data = Jmol.Cache.get(key)) || (data = this.cache.get(key));
}return (bytesOnly && (Clazz.instanceOf (data, String)) ? null : data);
}, "~S,~B");
Clazz.defineMethod (c$, "cacheClear", 
function () {
JU.Logger.info ("cache cleared");
this.cache.clear ();
this.clearPngjCache (null);
});
Clazz.defineMethod (c$, "cacheFileByNameAdd", 
function (fileName, isAdd) {
if (fileName == null || !isAdd && fileName.equalsIgnoreCase ("")) {
this.cacheClear ();
return -1;
}var data;
if (isAdd) {
fileName = this.vwr.resolveDatabaseFormat (fileName);
data = this.getFileAsBytes (fileName, null, true);
if (Clazz.instanceOf (data, String)) return 0;
this.cachePut (fileName, data);
} else {
if (fileName.endsWith ("*")) return JU.AU.removeMapKeys (this.cache, fileName.substring (0, fileName.length - 1));
data = this.cache.remove (fileName.$replace ('\\', '/'));
}return (data == null ? 0 : Clazz.instanceOf (data, String) ? (data).length : (data).length);
}, "~S,~B");
Clazz.defineMethod (c$, "cacheList", 
function () {
var map =  new java.util.Hashtable ();
for (var entry, $entry = this.cache.entrySet ().iterator (); $entry.hasNext () && ((entry = $entry.next ()) || true);) map.put (entry.getKey (), Integer.$valueOf (JU.PT.isAB (entry.getValue ()) ? (entry.getValue ()).length : entry.getValue ().toString ().length));

return map;
});
Clazz.defineMethod (c$, "getCanonicalName", 
function (pathName) {
var names = this.getClassifiedName (pathName, true);
return (names == null ? pathName : names[2]);
}, "~S");
Clazz.overrideMethod (c$, "postByteArray", 
function (fileName, bytes) {
var ret = this.getBufferedInputStreamOrErrorMessageFromName (fileName, null, false, false, bytes, false, true);
if (Clazz.instanceOf (ret, String)) return ret;
try {
ret = JU.Rdr.getStreamAsBytes (ret, null);
} catch (e) {
if (Clazz.exceptionOf (e, java.io.IOException)) {
try {
(ret).close ();
} catch (e1) {
if (Clazz.exceptionOf (e1, java.io.IOException)) {
} else {
throw e1;
}
}
} else {
throw e;
}
}
return (ret == null ? "" : JU.Rdr.fixUTF (ret));
}, "~S,~A");
Clazz.defineStatics (c$,
"SIMULATION_PROTOCOL", "http://SIMULATION/",
"URL_LOCAL", 4,
"urlPrefixes", ["http:", "https:", "sftp:", "ftp:", "file:"]);
c$.scriptFilePrefixes = c$.prototype.scriptFilePrefixes = ["/*file*/\"", "FILE0=\"", "FILE1=\""];
});
