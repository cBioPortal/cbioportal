Clazz.declarePackage ("J.viewer");
Clazz.load (["java.util.Hashtable"], "J.viewer.FileManager", ["java.io.BufferedInputStream", "$.ByteArrayInputStream", "java.lang.Boolean", "java.net.URL", "$.URLEncoder", "J.api.Interface", "J.io.Base64", "$.FileReader", "$.JmolBinary", "J.util.ArrayUtil", "$.Escape", "$.JmolList", "$.Logger", "$.SB", "$.TextFormat", "J.viewer.DataManager", "$.Viewer"], function () {
c$ = Clazz.decorateAsClass (function () {
this.viewer = null;
this.pathForAllFiles = "";
this.nameAsGiven = "zapped";
this.fullPathName = null;
this.fileName = null;
this.appletDocumentBaseURL = null;
this.appletProxy = null;
this.pngjCache = null;
this.spardirCache = null;
this.cache = null;
Clazz.instantialize (this, arguments);
}, J.viewer, "FileManager");
Clazz.prepareFields (c$, function () {
this.cache =  new java.util.Hashtable ();
});
Clazz.makeConstructor (c$, 
function (viewer) {
this.viewer = viewer;
this.clear ();
}, "J.viewer.Viewer");
$_M(c$, "clear", 
function () {
this.fullPathName = this.fileName = this.nameAsGiven = this.viewer.getZapName ();
this.spardirCache = null;
});
$_M(c$, "setLoadState", 
($fz = function (htParams) {
if (this.viewer.getPreserveState ()) {
htParams.put ("loadState", this.viewer.getLoadState (htParams));
}}, $fz.isPrivate = true, $fz), "java.util.Map");
$_M(c$, "getPathForAllFiles", 
function () {
return this.pathForAllFiles;
});
$_M(c$, "setPathForAllFiles", 
function (value) {
if (value.length > 0 && !value.endsWith ("/") && !value.endsWith ("|")) value += "/";
return this.pathForAllFiles = value;
}, "~S");
$_M(c$, "setFileInfo", 
function (fileInfo) {
this.fullPathName = fileInfo[0];
this.fileName = fileInfo[1];
this.nameAsGiven = fileInfo[2];
}, "~A");
$_M(c$, "getFileInfo", 
function () {
return [this.fullPathName, this.fileName, this.nameAsGiven];
});
$_M(c$, "getFullPathName", 
function () {
return this.fullPathName != null ? this.fullPathName : this.nameAsGiven;
});
$_M(c$, "getFileName", 
function () {
return this.fileName != null ? this.fileName : this.nameAsGiven;
});
$_M(c$, "getAppletDocumentBase", 
function () {
return (this.appletDocumentBaseURL == null ? "" : this.appletDocumentBaseURL.toString ());
});
$_M(c$, "setAppletContext", 
function (documentBase) {
try {
this.appletDocumentBaseURL = (documentBase.length == 0 ? null :  new java.net.URL (Clazz.castNullAs ("java.net.URL"), documentBase, null));
} catch (e) {
if (Clazz.exceptionOf (e, java.net.MalformedURLException)) {
} else {
throw e;
}
}
}, "~S");
$_M(c$, "setAppletProxy", 
function (appletProxy) {
this.appletProxy = (appletProxy == null || appletProxy.length == 0 ? null : appletProxy);
}, "~S");
$_M(c$, "getFileTypeName", 
function (fileName) {
var pt = fileName.indexOf ("::");
if (pt >= 0) return fileName.substring (0, pt);
if (fileName.startsWith ("=")) return "pdb";
var br = this.getUnzippedReaderOrStreamFromName (fileName, null, true, false, true, true, null);
if (Clazz.instanceOf (br, java.io.BufferedReader)) return this.viewer.getModelAdapter ().getFileTypeName (br);
if (Clazz.instanceOf (br, J.api.ZInputStream)) {
var zipDirectory = this.getZipDirectoryAsString (fileName);
if (zipDirectory.indexOf ("JmolManifest") >= 0) return "Jmol";
return this.viewer.getModelAdapter ().getFileTypeName (J.io.JmolBinary.getBufferedReaderForString (zipDirectory));
}if (J.util.Escape.isAS (br)) {
return (br)[0];
}return null;
}, "~S");
$_M(c$, "getZipDirectoryAsString", 
($fz = function (fileName) {
var t = this.getBufferedInputStreamOrErrorMessageFromName (fileName, fileName, false, false, null, false);
return J.io.JmolBinary.getZipDirectoryAsStringAndClose (t);
}, $fz.isPrivate = true, $fz), "~S");
$_M(c$, "createAtomSetCollectionFromFile", 
function (name, htParams, isAppend) {
if (htParams.get ("atomDataOnly") == null) {
this.setLoadState (htParams);
}name = this.viewer.resolveDatabaseFormat (name);
var pt = name.indexOf ("::");
var nameAsGiven = (pt >= 0 ? name.substring (pt + 2) : name);
var fileType = (pt >= 0 ? name.substring (0, pt) : null);
J.util.Logger.info ("\nFileManager.getAtomSetCollectionFromFile(" + nameAsGiven + ")" + (name.equals (nameAsGiven) ? "" : " //" + name));
var names = this.classifyName (nameAsGiven, true);
if (names.length == 1) return names[0];
var fullPathName = names[0];
var fileName = names[1];
htParams.put ("fullPathName", (fileType == null ? "" : fileType + "::") + fullPathName.$replace ('\\', '/'));
if (this.viewer.getBoolean (603979880) && this.viewer.getBoolean (603979824)) this.viewer.scriptStatus ("Requesting " + fullPathName);
var fileReader =  new J.io.FileReader (this, this.viewer, fileName, fullPathName, nameAsGiven, fileType, null, htParams, isAppend);
fileReader.run ();
return fileReader.getAtomSetCollection ();
}, "~S,java.util.Map,~B");
$_M(c$, "createAtomSetCollectionFromFiles", 
function (fileNames, htParams, isAppend) {
this.setLoadState (htParams);
var fullPathNames =  new Array (fileNames.length);
var namesAsGiven =  new Array (fileNames.length);
var fileTypes =  new Array (fileNames.length);
for (var i = 0; i < fileNames.length; i++) {
var pt = fileNames[i].indexOf ("::");
var nameAsGiven = (pt >= 0 ? fileNames[i].substring (pt + 2) : fileNames[i]);
var fileType = (pt >= 0 ? fileNames[i].substring (0, pt) : null);
var names = this.classifyName (nameAsGiven, true);
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
$_M(c$, "createAtomSetCollectionFromString", 
function (strModel, loadScript, htParams, isAppend, isLoadVariable) {
if (!isLoadVariable) J.viewer.DataManager.getInlineData (loadScript, strModel, isAppend, this.viewer.getDefaultLoadFilter ());
this.setLoadState (htParams);
var isAddH = (strModel.indexOf ("Viewer.AddHydrogens") >= 0);
var fnames = (isAddH ? this.getFileInfo () : null);
var fileReader =  new J.io.FileReader (this, this.viewer, "string", "string", "string", null, J.io.JmolBinary.getBufferedReaderForString (strModel), htParams, isAppend);
fileReader.run ();
if (fnames != null) this.setFileInfo (fnames);
if (!isAppend && !(Clazz.instanceOf (fileReader.getAtomSetCollection (), String))) {
this.viewer.zap (false, true, false);
this.fullPathName = this.fileName = (strModel === "5\n\nC 0 0 0\nH .63 .63 .63\nH -.63 -.63 .63\nH -.63 .63 -.63\nH .63 -.63 -.63" ? "Jmol Model Kit" : "string");
}return fileReader.getAtomSetCollection ();
}, "~S,J.util.SB,java.util.Map,~B,~B");
$_M(c$, "createAtomSeCollectionFromStrings", 
function (arrayModels, loadScript, htParams, isAppend) {
if (!htParams.containsKey ("isData")) {
var oldSep = "\"" + this.viewer.getDataSeparator () + "\"";
var tag = "\"" + (isAppend ? "append" : "model") + " inline\"";
var sb =  new J.util.SB ();
sb.append ("set dataSeparator \"~~~next file~~~\";\ndata ").append (tag);
for (var i = 0; i < arrayModels.length; i++) {
if (i > 0) sb.append ("~~~next file~~~");
sb.append (arrayModels[i]);
}
sb.append ("end ").append (tag).append (";set dataSeparator ").append (oldSep);
loadScript.appendSB (sb);
}this.setLoadState (htParams);
J.util.Logger.info ("FileManager.getAtomSetCollectionFromStrings(string[])");
var fullPathNames =  new Array (arrayModels.length);
var readers =  new Array (arrayModels.length);
for (var i = 0; i < arrayModels.length; i++) {
fullPathNames[i] = "string[" + i + "]";
readers[i] = this.newDataReader (arrayModels[i]);
}
var filesReader = this.newFilesReader (fullPathNames, fullPathNames, null, readers, htParams, isAppend);
filesReader.run ();
return filesReader.getAtomSetCollection ();
}, "~A,J.util.SB,java.util.Map,~B");
$_M(c$, "createAtomSeCollectionFromArrayData", 
function (arrayData, htParams, isAppend) {
J.util.Logger.info ("FileManager.getAtomSetCollectionFromArrayData(Vector)");
var nModels = arrayData.size ();
var fullPathNames =  new Array (nModels);
var readers =  new Array (nModels);
for (var i = 0; i < nModels; i++) {
fullPathNames[i] = "String[" + i + "]";
readers[i] = this.newDataReader (arrayData.get (i));
}
var filesReader = this.newFilesReader (fullPathNames, fullPathNames, null, readers, htParams, isAppend);
filesReader.run ();
return filesReader.getAtomSetCollection ();
}, "java.util.List,java.util.Map,~B");
$_M(c$, "newFilesReader", 
($fz = function (fullPathNames, namesAsGiven, fileTypes, readers, htParams, isAppend) {
var fr = J.api.Interface.getOptionInterface ("io2.FilesReader");
fr.set (this, this.viewer, fullPathNames, namesAsGiven, fileTypes, readers, htParams, isAppend);
return fr;
}, $fz.isPrivate = true, $fz), "~A,~A,~A,~A,java.util.Map,~B");
$_M(c$, "newDataReader", 
($fz = function (data) {
var reader = (Clazz.instanceOf (data, String) ? "String" : J.util.Escape.isAS (data) ? "Array" : Clazz.instanceOf (data, J.util.JmolList) ? "List" : null);
if (reader == null) return null;
var dr = J.api.Interface.getOptionInterface ("io2." + reader + "DataReader");
return dr.setData (data);
}, $fz.isPrivate = true, $fz), "~O");
$_M(c$, "createAtomSetCollectionFromDOM", 
function (DOMNode, htParams) {
var aDOMReader = J.api.Interface.getOptionInterface ("io2.DOMReadaer");
aDOMReader.set (this, this.viewer, DOMNode, htParams);
aDOMReader.run ();
return aDOMReader.getAtomSetCollection ();
}, "~O,java.util.Map");
$_M(c$, "createAtomSetCollectionFromReader", 
function (fullPathName, name, reader, htParams) {
var fileReader =  new J.io.FileReader (this, this.viewer, name, fullPathName, name, null, reader, htParams, false);
fileReader.run ();
return fileReader.getAtomSetCollection ();
}, "~S,~S,~O,java.util.Map");
$_M(c$, "getBufferedInputStream", 
function (fullPathName) {
var ret = this.getBufferedReaderOrErrorMessageFromName (fullPathName,  new Array (2), true, true);
return (Clazz.instanceOf (ret, java.io.BufferedInputStream) ? ret : null);
}, "~S");
$_M(c$, "getBufferedInputStreamOrErrorMessageFromName", 
function (name, fullName, showMsg, checkOnly, outputBytes, allowReader) {
var cacheBytes = null;
if (outputBytes == null) {
cacheBytes = (fullName == null || this.pngjCache == null ? null : J.io.JmolBinary.getCachedPngjBytes (this, fullName));
if (cacheBytes == null) cacheBytes = this.cacheGet (name, true);
}var bis = null;
var ret = null;
var errorMessage = null;
try {
if (cacheBytes == null) {
var isPngjBinaryPost = (name.indexOf ("?POST?_PNGJBIN_") >= 0);
var isPngjPost = (isPngjBinaryPost || name.indexOf ("?POST?_PNGJ_") >= 0);
if (name.indexOf ("?POST?_PNG_") > 0 || isPngjPost) {
var o = this.viewer.getImageAs (isPngjPost ? "PNGJ" : "PNG", -1, 0, 0, null, null);
if (!J.util.Escape.isAB (o)) return o;
if (isPngjBinaryPost) {
outputBytes = o;
name = J.util.TextFormat.simpleReplace (name, "?_", "=_");
} else {
name =  new J.util.SB ().append (name).append ("=").appendSB (J.io.Base64.getBase64 (o)).toString ();
}}var iurl = J.viewer.FileManager.urlTypeIndex (name);
var isURL = (iurl >= 0);
var post = null;
if (isURL && (iurl = name.indexOf ("?POST?")) >= 0) {
post = name.substring (iurl + 6);
name = name.substring (0, iurl);
}var isApplet = (this.appletDocumentBaseURL != null);
var fai = this.viewer.getFileAdapter ();
if (name.indexOf (".png") >= 0 && this.pngjCache == null && this.viewer.cachePngFiles ()) J.io.JmolBinary.cachePngjFile (this, null);
if (isApplet || isURL) {
if (isApplet && isURL && this.appletProxy != null) name = this.appletProxy + "?url=" + this.urlEncode (name);
var url = (isApplet ?  new java.net.URL (this.appletDocumentBaseURL, name, null) :  new java.net.URL (Clazz.castNullAs ("java.net.URL"), name, null));
if (checkOnly) return null;
name = url.toString ();
if (showMsg && name.toLowerCase ().indexOf ("password") < 0) J.util.Logger.info ("FileManager opening 1 " + name);
ret = fai.getBufferedURLInputStream (url, outputBytes, post);
if (Clazz.instanceOf (ret, J.util.SB)) {
var sb = ret;
if (allowReader && !J.io.JmolBinary.isBase64 (sb)) return J.io.JmolBinary.getBufferedReaderForString (sb.toString ());
ret = J.io.JmolBinary.getBISForStringXBuilder (sb);
} else if (J.util.Escape.isAB (ret)) {
ret =  new java.io.BufferedInputStream ( new java.io.ByteArrayInputStream (ret));
}} else if ((cacheBytes = this.cacheGet (name, true)) == null) {
if (showMsg) J.util.Logger.info ("FileManager opening 2 " + name);
ret = fai.getBufferedFileInputStream (name);
}if (Clazz.instanceOf (ret, String)) return ret;
}if (cacheBytes == null) bis = ret;
 else bis =  new java.io.BufferedInputStream ( new java.io.ByteArrayInputStream (cacheBytes));
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
}, "~S,~S,~B,~B,~A,~B");
$_M(c$, "urlEncode", 
($fz = function (name) {
try {
return java.net.URLEncoder.encode (name, "utf-8");
} catch (e) {
if (Clazz.exceptionOf (e, java.io.UnsupportedEncodingException)) {
return name;
} else {
throw e;
}
}
}, $fz.isPrivate = true, $fz), "~S");
$_M(c$, "getEmbeddedFileState", 
function (fileName) {
var dir = null;
dir = this.getZipDirectory (fileName, false);
if (dir.length == 0) {
var state = this.viewer.getFileAsString4 (fileName, -1, false, true);
return (state.indexOf ("**** Jmol Embedded Script ****") < 0 ? "" : J.io.JmolBinary.getEmbeddedScript (state));
}for (var i = 0; i < dir.length; i++) if (dir[i].indexOf (".spt") >= 0) {
var data = [fileName + "|" + dir[i], null];
this.getFileDataOrErrorAsString (data, -1, false, false);
return data[1];
}
return "";
}, "~S");
$_M(c$, "getFullPathNameOrError", 
function (filename) {
var names = this.classifyName (filename, true);
if (names == null || names[0] == null || names.length < 2) return [null, "cannot read file name: " + filename];
var name = names[0];
var fullPath = names[0].$replace ('\\', '/');
name = J.io.JmolBinary.getZipRoot (name);
var errMsg = this.getBufferedInputStreamOrErrorMessageFromName (name, fullPath, false, true, null, false);
return [fullPath, (Clazz.instanceOf (errMsg, String) ? errMsg : null)];
}, "~S");
$_M(c$, "getBufferedReaderOrErrorMessageFromName", 
function (name, fullPathNameReturn, isBinary, doSpecialLoad) {
var data = this.cacheGet (name, false);
var isBytes = J.util.Escape.isAB (data);
var bytes = (isBytes ? data : null);
if (name.startsWith ("cache://")) {
if (data == null) return "cannot read " + name;
if (isBytes) {
bytes = data;
} else {
return J.io.JmolBinary.getBufferedReaderForString (data);
}}var names = this.classifyName (name, true);
if (names == null) return "cannot read file name: " + name;
if (fullPathNameReturn != null) fullPathNameReturn[0] = names[0].$replace ('\\', '/');
return this.getUnzippedReaderOrStreamFromName (names[0], bytes, false, isBinary, false, doSpecialLoad, null);
}, "~S,~A,~B,~B");
$_M(c$, "getUnzippedReaderOrStreamFromName", 
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
}}var sb =  new J.util.SB ();
if (fileData.get ("OUTPUT") != null) sb.append (fileData.get (fileData.get ("OUTPUT")));
var s;
for (var i = 2; i < info.length; i++) {
name = info[i];
name = this.getObjectAsSections (name, header, fileData);
J.util.Logger.info ("reading " + name);
s = fileData.get (name);
sb.append (s);
}
s = sb.toString ();
if (this.spardirCache == null) this.spardirCache =  new java.util.Hashtable ();
this.spardirCache.put (name00.$replace ('\\', '/'), s.getBytes ());
return J.io.JmolBinary.getBufferedReaderForString (s);
}}if (bytes == null && this.pngjCache != null) {
bytes = J.io.JmolBinary.getCachedPngjBytes (this, name);
if (bytes != null && htParams != null) htParams.put ("sourcePNGJ", Boolean.TRUE);
}var fullName = name;
if (name.indexOf ("|") >= 0) {
subFileList = J.util.TextFormat.splitChars (name, "|");
if (bytes == null) J.util.Logger.info ("FileManager opening 3 " + name);
name = subFileList[0];
}var t = (bytes == null ? this.getBufferedInputStreamOrErrorMessageFromName (name, fullName, true, false, null, !forceInputStream) :  new java.io.BufferedInputStream ( new java.io.ByteArrayInputStream (bytes)));
try {
if (Clazz.instanceOf (t, String)) return t;
if (Clazz.instanceOf (t, java.io.BufferedReader)) return t;
var bis = t;
if (J.io.JmolBinary.isGzipS (bis)) {
do {
bis =  new java.io.BufferedInputStream (J.io.JmolBinary.newGZIPInputStream (bis));
} while (J.io.JmolBinary.isGzipS (bis));
}if (J.io.JmolBinary.isCompoundDocumentS (bis)) {
var doc = J.api.Interface.getOptionInterface ("io2.CompoundDocument");
doc.setStream (bis, true);
return J.io.JmolBinary.getBufferedReaderForString (doc.getAllDataFiles ("Molecule", "Input").toString ());
}if (J.io.JmolBinary.isPickleS (bis)) return bis;
bis = J.io.JmolBinary.checkPngZipStream (bis);
if (J.io.JmolBinary.isZipS (bis)) {
if (allowZipStream) return J.io.JmolBinary.newZipInputStream (bis);
if (forceInputStream) return J.io.JmolBinary.getZipFileContents (bis, subFileList, 1, true);
var s = J.io.JmolBinary.getZipFileContents (bis, subFileList, 1, false);
bis.close ();
return J.io.JmolBinary.getBufferedReaderForString (s);
}return (forceInputStream ? bis : J.io.JmolBinary.getBufferedReader (bis, null));
} catch (ioe) {
if (Clazz.exceptionOf (ioe, Exception)) {
return ioe.toString ();
} else {
throw ioe;
}
}
}, "~S,~A,~B,~B,~B,~B,java.util.Map");
$_M(c$, "getSpartanFileList", 
($fz = function (name) {
if (name.endsWith (".spt")) return [null, null, null];
if (name.endsWith (".spardir.zip")) return ["SpartanSmol", "Directory Entry ", name + "|output"];
name = name.$replace ('\\', '/');
if (!name.endsWith (".spardir") && name.indexOf (".spardir/") < 0) return null;
var pt = name.lastIndexOf (".spardir");
if (pt < 0) return null;
if (name.lastIndexOf ("/") > pt) {
return ["SpartanSmol", "Directory Entry ", name + "/input", name + "/archive", name + "/Molecule:asBinaryString", name + "/proparc"];
}return ["SpartanSmol", "Directory Entry ", name + "/output"];
}, $fz.isPrivate = true, $fz), "~S");
$_M(c$, "getObjectAsSections", 
($fz = function (name, header, fileData) {
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
subFileList = J.util.TextFormat.splitChars (name, "|");
name = subFileList[0];
}var bis = null;
try {
var t = this.getBufferedInputStreamOrErrorMessageFromName (name, fullName, false, false, null, false);
if (Clazz.instanceOf (t, String)) {
fileData.put (name0, t + "\n");
return name0;
}bis = t;
if (J.io.JmolBinary.isCompoundDocumentS (bis)) {
var doc = J.api.Interface.getOptionInterface ("io2.CompoundDocument");
doc.setStream (bis, true);
doc.getAllDataMapped (name.$replace ('\\', '/'), "Molecule", fileData);
} else if (J.io.JmolBinary.isZipS (bis)) {
J.io.JmolBinary.getAllZipData (bis, subFileList, name.$replace ('\\', '/'), "Molecule", fileData);
} else if (asBinaryString) {
var bd = J.api.Interface.getOptionInterface ("io2.BinaryDocument");
bd.setStream (bis, false);
sb =  new J.util.SB ();
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
var br = J.io.JmolBinary.getBufferedReader (J.io.JmolBinary.isGzipS (bis) ?  new java.io.BufferedInputStream (J.io.JmolBinary.newGZIPInputStream (bis)) : bis, null);
var line;
sb =  new J.util.SB ();
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
}, $fz.isPrivate = true, $fz), "~S,~S,java.util.Map");
$_M(c$, "getZipDirectory", 
function (fileName, addManifest) {
var t = this.getBufferedInputStreamOrErrorMessageFromName (fileName, fileName, false, false, null, false);
return J.io.JmolBinary.getZipDirectoryAndClose (t, addManifest);
}, "~S,~B");
$_M(c$, "getFileAsBytes", 
function (name, osb, allowZip) {
if (name == null) return null;
var fullName = name;
var subFileList = null;
if (name.indexOf ("|") >= 0) {
subFileList = J.util.TextFormat.splitChars (name, "|");
name = subFileList[0];
allowZip = true;
}var t = this.getBufferedInputStreamOrErrorMessageFromName (name, fullName, false, false, null, false);
if (Clazz.instanceOf (t, String)) return "Error:" + t;
try {
var bis = t;
var bytes = (osb != null || !allowZip || subFileList == null || subFileList.length <= 1 || !J.io.JmolBinary.isZipS (bis) && !J.io.JmolBinary.isPngZipStream (bis) ? J.io.JmolBinary.getStreamAsBytes (bis, osb) : J.io.JmolBinary.getZipFileContentsAsBytes (bis, subFileList, 1));
bis.close ();
return bytes;
} catch (ioe) {
if (Clazz.exceptionOf (ioe, Exception)) {
return ioe.toString ();
} else {
throw ioe;
}
}
}, "~S,J.io.OutputStringBuilder,~B");
$_M(c$, "getFileDataOrErrorAsString", 
function (data, nBytesMax, doSpecialLoad, allowBinary) {
data[1] = "";
var name = data[0];
if (name == null) return false;
var t = this.getBufferedReaderOrErrorMessageFromName (name, data, false, doSpecialLoad);
if (Clazz.instanceOf (t, String)) {
data[1] = t;
return false;
}return J.io.JmolBinary.readAll (t, nBytesMax, allowBinary, data, 1);
}, "~A,~N,~B,~B");
$_M(c$, "loadImage", 
function (name, echoName) {
var image = null;
var info = null;
var fullPathName = "";
while (true) {
if (name == null) break;
var names = this.classifyName (name, true);
if (names == null) {
fullPathName = "cannot read file name: " + name;
break;
}var apiPlatform = this.viewer.apiPlatform;
fullPathName = names[0].$replace ('\\', '/');
if (fullPathName.indexOf ("|") > 0) {
var ret = this.getFileAsBytes (fullPathName, null, true);
if (!J.util.Escape.isAB (ret)) {
fullPathName = "" + ret;
break;
}image = (this.viewer.isJS ? ret : apiPlatform.createImage (ret));
} else if (this.viewer.isJS) {
} else if (J.viewer.FileManager.urlTypeIndex (fullPathName) >= 0) {
try {
image = apiPlatform.createImage ( new java.net.URL (Clazz.castNullAs ("java.net.URL"), fullPathName, null));
} catch (e) {
if (Clazz.exceptionOf (e, Exception)) {
fullPathName = "bad URL: " + fullPathName;
break;
} else {
throw e;
}
}
} else {
image = apiPlatform.createImage (fullPathName);
}{
info = [echoName, fullPathName];
}try {
if (!apiPlatform.waitForDisplay (info, image)) {
image = null;
break;
}{
return;
}} catch (e) {
if (Clazz.exceptionOf (e, Exception)) {
System.out.println (e.toString ());
fullPathName = e.toString () + " opening 4 " + fullPathName;
image = null;
break;
} else {
throw e;
}
}
if (apiPlatform.getImageWidth (image) < 1) {
fullPathName = "invalid or missing image " + fullPathName;
image = null;
break;
}break;
}
this.viewer.loadImageData (image, fullPathName, echoName, null);
}, "~S,~S");
c$.urlTypeIndex = $_M(c$, "urlTypeIndex", 
function (name) {
for (var i = 0; i < J.viewer.FileManager.urlPrefixes.length; ++i) {
if (name.startsWith (J.viewer.FileManager.urlPrefixes[i])) {
return i;
}}
return -1;
}, "~S");
c$.isLocal = $_M(c$, "isLocal", 
function (fileName) {
if (fileName == null) return false;
var itype = J.viewer.FileManager.urlTypeIndex (fileName);
return (itype < 0 || itype == 3);
}, "~S");
$_M(c$, "classifyName", 
function (name, isFullLoad) {
if (name == null) return [null];
var doSetPathForAllFiles = (this.pathForAllFiles.length > 0);
if (name.startsWith ("?")) {
if ((name = this.viewer.dialogAsk ("Load", name.substring (1))) == null) return [isFullLoad ? "#CANCELED#" : null];
doSetPathForAllFiles = false;
}var file = null;
var url = null;
var names = null;
if (name.startsWith ("cache://")) {
names =  new Array (3);
names[0] = names[2] = name;
names[1] = J.viewer.FileManager.stripPath (names[0]);
return names;
}name = this.viewer.resolveDatabaseFormat (name);
if (name.indexOf (":") < 0 && name.indexOf ("/") != 0) name = J.viewer.FileManager.addDirectory (this.viewer.getDefaultDirectory (), name);
if (this.appletDocumentBaseURL != null) {
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
} else {
if (J.viewer.FileManager.urlTypeIndex (name) >= 0 || this.viewer.isRestricted (J.viewer.Viewer.ACCESS.NONE) || this.viewer.isRestricted (J.viewer.Viewer.ACCESS.READSPT) && !name.endsWith (".spt") && !name.endsWith ("/")) {
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
file = this.viewer.apiPlatform.newFile (name);
names = [file.getAbsolutePath (), file.getName (), "file:/" + file.getAbsolutePath ().$replace ('\\', '/')];
}}if (url != null) {
names =  new Array (3);
names[0] = names[2] = url.toString ();
names[1] = J.viewer.FileManager.stripPath (names[0]);
}if (doSetPathForAllFiles) {
var name0 = names[0];
names[0] = this.pathForAllFiles + names[1];
J.util.Logger.info ("FileManager substituting " + name0 + " --> " + names[0]);
}if (isFullLoad && (file != null || J.viewer.FileManager.urlTypeIndex (names[0]) == 3)) {
var path = (file == null ? J.util.TextFormat.trim (names[0].substring (5), "/") : names[0]);
var pt = path.length - names[1].length - 1;
if (pt > 0) {
path = path.substring (0, pt);
J.viewer.FileManager.setLocalPath (this.viewer, path, true);
}}return names;
}, "~S,~B");
c$.addDirectory = $_M(c$, "addDirectory", 
($fz = function (defaultDirectory, name) {
if (defaultDirectory.length == 0) return name;
var ch = (name.length > 0 ? name.charAt (0) : ' ');
var s = defaultDirectory.toLowerCase ();
if ((s.endsWith (".zip") || s.endsWith (".tar")) && ch != '|' && ch != '/') defaultDirectory += "|";
return defaultDirectory + (ch == '/' || ch == '/' || (ch = defaultDirectory.charAt (defaultDirectory.length - 1)) == '|' || ch == '/' ? "" : "/") + name;
}, $fz.isPrivate = true, $fz), "~S,~S");
$_M(c$, "getDefaultDirectory", 
function (name) {
var names = this.classifyName (name, true);
if (names == null) return "";
name = J.viewer.FileManager.fixPath (names[0]);
return (name == null ? "" : name.substring (0, name.lastIndexOf ("/")));
}, "~S");
c$.fixPath = $_M(c$, "fixPath", 
($fz = function (path) {
path = path.$replace ('\\', '/');
path = J.util.TextFormat.simpleReplace (path, "/./", "/");
var pt = path.lastIndexOf ("//") + 1;
if (pt < 1) pt = path.indexOf (":/") + 1;
if (pt < 1) pt = path.indexOf ("/");
if (pt < 0) return null;
var protocol = path.substring (0, pt);
path = path.substring (pt);
while ((pt = path.lastIndexOf ("/../")) >= 0) {
var pt0 = path.substring (0, pt).lastIndexOf ("/");
if (pt0 < 0) return J.util.TextFormat.simpleReplace (protocol + path, "/../", "/");
path = path.substring (0, pt0) + path.substring (pt + 3);
}
if (path.length == 0) path = "/";
return protocol + path;
}, $fz.isPrivate = true, $fz), "~S");
$_M(c$, "getFilePath", 
function (name, addUrlPrefix, asShortName) {
var names = this.classifyName (name, false);
return (names == null || names.length == 1 ? "" : asShortName ? names[1] : addUrlPrefix ? names[2] : names[0] == null ? "" : names[0].$replace ('\\', '/'));
}, "~S,~B,~B");
c$.getLocalUrl = $_M(c$, "getLocalUrl", 
function (file) {
if (file.getName ().startsWith ("=")) return file.getName ();
var path = file.getAbsolutePath ().$replace ('\\', '/');
for (var i = 0; i < J.viewer.FileManager.urlPrefixPairs.length; i++) if (path.indexOf (J.viewer.FileManager.urlPrefixPairs[i]) == 0) return null;

for (var i = 0; i < J.viewer.FileManager.urlPrefixPairs.length; i += 2) if (path.indexOf (J.viewer.FileManager.urlPrefixPairs[i]) > 0) return J.viewer.FileManager.urlPrefixPairs[i + 1] + J.util.TextFormat.trim (path.substring (path.indexOf (J.viewer.FileManager.urlPrefixPairs[i]) + J.viewer.FileManager.urlPrefixPairs[i].length), "/");

return null;
}, "J.api.JmolFileInterface");
c$.getLocalDirectory = $_M(c$, "getLocalDirectory", 
function (viewer, forDialog) {
var localDir = viewer.getParameter (forDialog ? "currentLocalPath" : "defaultDirectoryLocal");
if (forDialog && localDir.length == 0) localDir = viewer.getParameter ("defaultDirectoryLocal");
if (localDir.length == 0) return (viewer.isApplet () ? null : viewer.apiPlatform.newFile (System.getProperty ("user.dir", ".")));
if (viewer.isApplet () && localDir.indexOf ("file:/") == 0) localDir = localDir.substring (6);
var f = viewer.apiPlatform.newFile (localDir);
return f.isDirectory () ? f : f.getParentAsFile ();
}, "J.api.JmolViewer,~B");
c$.setLocalPath = $_M(c$, "setLocalPath", 
function (viewer, path, forDialog) {
while (path.endsWith ("/") || path.endsWith ("\\")) path = path.substring (0, path.length - 1);

viewer.setStringProperty ("currentLocalPath", path);
if (!forDialog) viewer.setStringProperty ("defaultDirectoryLocal", path);
}, "J.api.JmolViewer,~S,~B");
c$.getLocalPathForWritingFile = $_M(c$, "getLocalPathForWritingFile", 
function (viewer, file) {
if (file.indexOf ("file:/") == 0) return file.substring (6);
if (file.indexOf ("/") == 0 || file.indexOf (":") >= 0) return file;
var dir = J.viewer.FileManager.getLocalDirectory (viewer, false);
return (dir == null ? file : J.viewer.FileManager.fixPath (dir.toString () + "/" + file));
}, "J.api.JmolViewer,~S");
c$.setScriptFileReferences = $_M(c$, "setScriptFileReferences", 
function (script, localPath, remotePath, scriptPath) {
if (localPath != null) script = J.viewer.FileManager.setScriptFileRefs (script, localPath, true);
if (remotePath != null) script = J.viewer.FileManager.setScriptFileRefs (script, remotePath, false);
script = J.util.TextFormat.simpleReplace (script, "\1\"", "\"");
if (scriptPath != null) {
while (scriptPath.endsWith ("/")) scriptPath = scriptPath.substring (0, scriptPath.length - 1);

for (var ipt = 0; ipt < J.viewer.FileManager.scriptFilePrefixes.length; ipt++) {
var tag = J.viewer.FileManager.scriptFilePrefixes[ipt];
script = J.util.TextFormat.simpleReplace (script, tag + ".", tag + scriptPath);
}
}return script;
}, "~S,~S,~S,~S");
c$.setScriptFileRefs = $_M(c$, "setScriptFileRefs", 
($fz = function (script, dataPath, isLocal) {
if (dataPath == null) return script;
var noPath = (dataPath.length == 0);
var fileNames =  new J.util.JmolList ();
J.io.JmolBinary.getFileReferences (script, fileNames);
var oldFileNames =  new J.util.JmolList ();
var newFileNames =  new J.util.JmolList ();
var nFiles = fileNames.size ();
for (var iFile = 0; iFile < nFiles; iFile++) {
var name0 = fileNames.get (iFile);
var name = name0;
if (isLocal == J.viewer.FileManager.isLocal (name)) {
var pt = (noPath ? -1 : name.indexOf ("/" + dataPath + "/"));
if (pt >= 0) {
name = name.substring (pt + 1);
} else {
pt = name.lastIndexOf ("/");
if (pt < 0 && !noPath) name = "/" + name;
if (pt < 0 || noPath) pt++;
name = dataPath + name.substring (pt);
}}J.util.Logger.info ("FileManager substituting " + name0 + " --> " + name);
oldFileNames.addLast ("\"" + name0 + "\"");
newFileNames.addLast ("\1\"" + name + "\"");
}
return J.util.TextFormat.replaceStrings (script, oldFileNames, newFileNames);
}, $fz.isPrivate = true, $fz), "~S,~S,~B");
c$.stripPath = $_M(c$, "stripPath", 
function (name) {
var pt = Math.max (name.lastIndexOf ("|"), name.lastIndexOf ("/"));
return name.substring (pt + 1);
}, "~S");
c$.fixFileNameVariables = $_M(c$, "fixFileNameVariables", 
function (format, fname) {
var str = J.util.TextFormat.simpleReplace (format, "%FILE", fname);
if (str.indexOf ("%LC") < 0) return str;
fname = fname.toLowerCase ();
str = J.util.TextFormat.simpleReplace (str, "%LCFILE", fname);
if (fname.length == 4) str = J.util.TextFormat.simpleReplace (str, "%LC13", fname.substring (1, 3));
return str;
}, "~S,~S");
$_M(c$, "clearPngjCache", 
function (fileName) {
if (fileName != null && (this.pngjCache == null || !this.pngjCache.containsKey (this.getCanonicalName (J.io.JmolBinary.getZipRoot (fileName))))) return;
this.pngjCache = null;
J.util.Logger.info ("PNGJ cache cleared");
}, "~S");
$_M(c$, "cachePut", 
function (key, data) {
key = key.$replace ('\\', '/');
if (J.util.Logger.debugging) J.util.Logger.debug ("cachePut " + key);
if (data == null || "".equals (data)) {
this.cache.remove (key);
return;
}this.cache.put (key, data);
J.io.JmolBinary.getCachedPngjBytes (this, key);
}, "~S,~O");
$_M(c$, "cacheGet", 
function (key, bytesOnly) {
key = key.$replace ('\\', '/');
var pt = key.indexOf ("|");
if (pt >= 0) key = key.substring (0, pt);
if (J.util.Logger.debugging) J.util.Logger.debug ("cacheGet " + key + " " + this.cache.containsKey (key));
var data = this.cache.get (key);
return (bytesOnly && (Clazz.instanceOf (data, String)) ? null : data);
}, "~S,~B");
$_M(c$, "cacheClear", 
function () {
J.util.Logger.info ("cache cleared");
this.cache.clear ();
this.clearPngjCache (null);
});
$_M(c$, "cacheFileByNameAdd", 
function (fileName, isAdd) {
if (fileName == null || !isAdd && fileName.equalsIgnoreCase ("")) {
this.cacheClear ();
return -1;
}var data;
if (isAdd) {
fileName = this.viewer.resolveDatabaseFormat (fileName);
data = this.getFileAsBytes (fileName, null, true);
if (Clazz.instanceOf (data, String)) return 0;
this.cachePut (fileName, data);
} else {
if (fileName.endsWith ("*")) return J.util.ArrayUtil.removeMapKeys (this.cache, fileName.substring (0, fileName.length - 1));
data = this.cache.remove (fileName.$replace ('\\', '/'));
}return (data == null ? 0 : Clazz.instanceOf (data, String) ? (data).length : (data).length);
}, "~S,~B");
$_M(c$, "cacheList", 
function () {
var map =  new java.util.Hashtable ();
for (var entry, $entry = this.cache.entrySet ().iterator (); $entry.hasNext () && ((entry = $entry.next ()) || true);) map.put (entry.getKey (), Integer.$valueOf (J.util.Escape.isAB (entry.getValue ()) ? (entry.getValue ()).length : entry.getValue ().toString ().length));

return map;
});
$_M(c$, "getCanonicalName", 
function (pathName) {
var names = this.classifyName (pathName, true);
return (names == null ? pathName : names[2]);
}, "~S");
Clazz.defineStatics (c$,
"URL_LOCAL", 3,
"urlPrefixes", ["http:", "https:", "ftp:", "file:"],
"urlPrefixPairs", ["http:", "http://", "www.", "http://www.", "https:", "https://", "ftp:", "ftp://", "file:", "file:///"]);
c$.scriptFilePrefixes = c$.prototype.scriptFilePrefixes = ["/*file*/\"", "FILE0=\"", "FILE1=\""];
});
