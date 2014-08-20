Clazz.declarePackage ("J.io");
Clazz.load (["J.api.JmolZipUtilities"], "J.io.JmolUtil", ["java.io.BufferedInputStream", "$.BufferedReader", "java.lang.Character", "java.net.URL", "java.util.Hashtable", "$.StringTokenizer", "JU.LimitedLineReader", "$.Lst", "$.PT", "$.Rdr", "$.SB", "$.ZipTools", "J.adapter.smarter.AtomSetCollection", "J.api.Interface", "J.io.JmolBinary", "JU.Escape", "$.Logger", "JV.FileManager"], function () {
c$ = Clazz.declareType (J.io, "JmolUtil", null, J.api.JmolZipUtilities);
Clazz.makeConstructor (c$, 
function () {
});
c$.checkSpecialData = Clazz.defineMethod (c$, "checkSpecialData", 
 function (zpt, is, zipDirectory) {
var isSpartan = false;
for (var i = 1; i < zipDirectory.length; i++) {
if (zipDirectory[i].endsWith (".spardir/") || zipDirectory[i].indexOf ("_spartandir") >= 0) {
isSpartan = true;
break;
}}
if (!isSpartan) return null;
var data =  new JU.SB ();
data.append ("Zip File Directory: ").append ("\n").append (JU.Escape.eAS (zipDirectory, true)).append ("\n");
var fileData =  new java.util.Hashtable ();
zpt.getAllZipData (is, [], "", "Molecule", fileData);
var prefix = "|";
var outputData = fileData.get (prefix + "output");
if (outputData == null) outputData = fileData.get ((prefix = "|" + zipDirectory[1]) + "output");
data.append (outputData);
var files = J.io.JmolUtil.getSpartanFileList (prefix, J.io.JmolUtil.getSpartanDirs (outputData));
for (var i = 2; i < files.length; i++) {
var name = files[i];
if (fileData.containsKey (name)) data.append (fileData.get (name));
 else data.append (name + "\n");
}
return data;
}, "javajs.api.GenericZipTools,java.io.InputStream,~A");
c$.checkSpecialInZip = Clazz.defineMethod (c$, "checkSpecialInZip", 
function (zipDirectory) {
var name;
return (zipDirectory.length < 2 ? null : (name = zipDirectory[1]).endsWith (".spardir/") || zipDirectory.length == 2 ? ["", (name.endsWith ("/") ? name.substring (0, name.length - 1) : name)] : null);
}, "~A");
c$.getSpartanDirs = Clazz.defineMethod (c$, "getSpartanDirs", 
 function (outputFileData) {
if (outputFileData == null) return [];
var v =  new JU.Lst ();
var token;
var lasttoken = "";
if (!outputFileData.startsWith ("java.io.FileNotFoundException") && !outputFileData.startsWith ("FILE NOT FOUND") && outputFileData.indexOf ("<html") < 0) try {
var tokens =  new java.util.StringTokenizer (outputFileData, " \t\r\n");
while (tokens.hasMoreTokens ()) {
if ((token = tokens.nextToken ()).equals (")")) v.addLast (lasttoken);
 else if (token.equals ("Start-") && tokens.nextToken ().equals ("Molecule")) v.addLast (JU.PT.split (tokens.nextToken (), "\"")[1]);
lasttoken = token;
}
} catch (e) {
if (Clazz.exceptionOf (e, Exception)) {
} else {
throw e;
}
}
return (v.size () == 0 ? ["M0001"] : v.toArray ( new Array (v.size ())));
}, "~S");
c$.getSpartanFileList = Clazz.defineMethod (c$, "getSpartanFileList", 
 function (name, dirNums) {
var files =  new Array (2 + dirNums.length * 5);
files[0] = "SpartanSmol";
files[1] = "Directory Entry ";
var pt = 2;
name = name.$replace ('\\', '/');
if (name.endsWith ("/")) name = name.substring (0, name.length - 1);
var sep = (name.endsWith (".zip") ? "|" : "/");
for (var i = 0; i < dirNums.length; i++) {
var path = name + sep;
path += (Character.isDigit (dirNums[i].charAt (0)) ? "Profile." + dirNums[i] : dirNums[i]) + "/";
files[pt++] = path + "#JMOL_MODEL " + dirNums[i];
files[pt++] = path + "input";
files[pt++] = path + "archive";
files[pt++] = path + "Molecule:asBinaryString";
files[pt++] = path + "proparc";
}
return files;
}, "~S,~A");
c$.shortSceneFilename = Clazz.defineMethod (c$, "shortSceneFilename", 
 function (pathName) {
var pt = pathName.indexOf ("_scene_") + 7;
if (pt < 7) return pathName;
var s = "";
if (pathName.endsWith ("|state.spt")) {
var pt1 = pathName.indexOf ('.', pt);
if (pt1 < 0) return pathName;
s = pathName.substring (pt, pt1);
}var pt2 = pathName.lastIndexOf ("|");
return pathName.substring (0, pt) + s + (pt2 > 0 ? pathName.substring (pt2) : "");
}, "~S");
Clazz.overrideMethod (c$, "cachePngjFile", 
function (jmb, data) {
data[0] = JU.Rdr.getZipRoot (data[0]);
var shortName = J.io.JmolUtil.shortSceneFilename (data[0]);
try {
data[1] = JU.Rdr.getJzt ().cacheZipContents (JU.Rdr.getPngZipStream (jmb.fm.getBufferedInputStreamOrErrorMessageFromName (data[0], null, false, false, null, false, true), true), shortName, jmb.pngjCache, false);
} catch (e) {
if (Clazz.exceptionOf (e, Exception)) {
return false;
} else {
throw e;
}
}
if (data[1] == null) return false;
var bytes = data[1].getBytes ();
jmb.pngjCache.put (jmb.fm.getCanonicalName (data[0]), bytes);
if (shortName.indexOf ("_scene_") >= 0) {
jmb.pngjCache.put (J.io.JmolUtil.shortSceneFilename (data[0]), bytes);
bytes = jmb.pngjCache.remove (shortName + "|state.spt");
if (bytes != null) jmb.pngjCache.put (J.io.JmolUtil.shortSceneFilename (data[0] + "|state.spt"), bytes);
}for (var key, $key = jmb.pngjCache.keySet ().iterator (); $key.hasNext () && ((key = $key.next ()) || true);) System.out.println (key);

return true;
}, "J.io.JmolBinary,~A");
Clazz.overrideMethod (c$, "determineSurfaceFileType", 
function (bufferedReader) {
var line = null;
var br = null;
try {
br =  new JU.LimitedLineReader (bufferedReader, 16000);
line = br.getHeader (0);
} catch (e) {
if (Clazz.exceptionOf (e, Exception)) {
} else {
throw e;
}
}
if (br == null || line == null || line.length == 0) return null;
switch (line.charAt (0)) {
case '@':
if (line.indexOf ("@text") == 0) return "Kinemage";
break;
case '#':
if (line.indexOf (".obj") >= 0) return "Obj";
if (line.indexOf ("MSMS") >= 0) return "Msms";
break;
case '&':
if (line.indexOf ("&plot") == 0) return "Jaguar";
break;
case '\r':
case '\n':
if (line.indexOf ("ZYX") >= 0) return "Xplor";
break;
}
if (line.indexOf ("Here is your gzipped map") >= 0) return "UPPSALA" + line;
if (line.startsWith ("4MESHC")) return "Pmesh4";
if (line.indexOf ("! nspins") >= 0) return "CastepDensity";
if (line.indexOf ("<jvxl") >= 0 && line.indexOf ("<?xml") >= 0) return "JvxlXml";
if (line.indexOf ("#JVXL+") >= 0) return "Jvxl+";
if (line.indexOf ("#JVXL") >= 0) return "Jvxl";
if (line.indexOf ("<efvet ") >= 0) return "Efvet";
if (line.indexOf ("usemtl") >= 0) return "Obj";
if (line.indexOf ("# object with") == 0) return "Nff";
if (line.indexOf ("BEGIN_DATAGRID_3D") >= 0 || line.indexOf ("BEGIN_BANDGRID_3D") >= 0) return "Xsf";
var pt0 = line.indexOf ('\0');
if (pt0 >= 0) {
if (line.indexOf ("PM\u0001\u0000") == 0) return "Pmesh";
if (line.indexOf ("\u0014\u0000\u0000\u0000") == 0) return "DelPhi";
if (line.indexOf ("MAP ") == 208) return "Mrc";
if (line.length > 37 && (line.charCodeAt (36) == 0 && line.charCodeAt (37) == 100 || line.charCodeAt (36) == 0 && line.charCodeAt (37) == 100)) {
return "Dsn6";
}}if (line.indexOf (" 0.00000e+00 0.00000e+00      0      0\n") >= 0) return "Uhbd";
line = br.readLineWithNewline ();
if (line.indexOf ("object 1 class gridpositions counts") == 0) return "Apbs";
var tokens = JU.PT.getTokens (line);
var line2 = br.readLineWithNewline ();
if (tokens.length == 2 && JU.PT.parseInt (tokens[0]) == 3 && JU.PT.parseInt (tokens[1]) != -2147483648) {
tokens = JU.PT.getTokens (line2);
if (tokens.length == 3 && JU.PT.parseInt (tokens[0]) != -2147483648 && JU.PT.parseInt (tokens[1]) != -2147483648 && JU.PT.parseInt (tokens[2]) != -2147483648) return "PltFormatted";
}var line3 = br.readLineWithNewline ();
if (line.startsWith ("v ") && line2.startsWith ("v ") && line3.startsWith ("v ")) return "Obj";
var nAtoms = JU.PT.parseInt (line3);
if (nAtoms == -2147483648) return (line3.indexOf ("+") == 0 ? "Jvxl+" : null);
if (nAtoms >= 0) return (line3.length < 60 ? "Cube" : null);
nAtoms = -nAtoms;
for (var i = 4 + nAtoms; --i >= 0; ) if ((line = br.readLineWithNewline ()) == null) return null;

var nSurfaces = JU.PT.parseInt (line);
if (nSurfaces == -2147483648) return null;
return (nSurfaces < 0 ? "Jvxl" : "Cube");
}, "java.io.BufferedReader");
Clazz.overrideMethod (c$, "getAtomSetCollectionOrBufferedReaderFromZip", 
function (zpt, adapter, is, fileName, zipDirectory, htParams, subFilePtr, asBufferedReader) {
var doCombine = (subFilePtr == 1);
htParams.put ("zipSet", fileName);
var subFileList = htParams.get ("subFileList");
if (subFileList == null) subFileList = J.io.JmolUtil.checkSpecialInZip (zipDirectory);
var subFileName = (subFileList == null || subFilePtr >= subFileList.length ? null : subFileList[subFilePtr]);
if (subFileName != null && (subFileName.startsWith ("/") || subFileName.startsWith ("\\"))) subFileName = subFileName.substring (1);
var selectedFile = 0;
if (subFileName == null && htParams.containsKey ("modelNumber")) {
selectedFile = (htParams.get ("modelNumber")).intValue ();
if (selectedFile > 0 && doCombine) htParams.remove ("modelNumber");
}var manifest = htParams.get ("manifest");
var useFileManifest = (manifest == null);
if (useFileManifest) manifest = (zipDirectory.length > 0 ? zipDirectory[0] : "");
var haveManifest = (manifest.length > 0);
if (haveManifest) {
if (JU.Logger.debugging) JU.Logger.debug ("manifest for  " + fileName + ":\n" + manifest);
}var ignoreErrors = (manifest.indexOf ("IGNORE_ERRORS") >= 0);
var selectAll = (manifest.indexOf ("IGNORE_MANIFEST") >= 0);
var exceptFiles = (manifest.indexOf ("EXCEPT_FILES") >= 0);
if (selectAll || subFileName != null) haveManifest = false;
if (useFileManifest && haveManifest) {
var path = J.io.JmolBinary.getManifestScriptPath (manifest);
if (path != null) return "NOTE: file recognized as a script file: " + fileName + path + "\n";
}var vCollections =  new JU.Lst ();
var htCollections = (haveManifest ?  new java.util.Hashtable () : null);
var nFiles = 0;
var ret = J.io.JmolUtil.checkSpecialData (zpt, is, zipDirectory);
if (Clazz.instanceOf (ret, String)) return ret;
var data = ret;
try {
if (data != null) {
var reader = JU.Rdr.getBR (data.toString ());
if (asBufferedReader) return reader;
ret = adapter.getAtomSetCollectionFromReader (fileName, reader, htParams);
if (Clazz.instanceOf (ret, String)) return ret;
if (Clazz.instanceOf (ret, J.adapter.smarter.AtomSetCollection)) {
var atomSetCollection = ret;
if (atomSetCollection.errorMessage != null) {
if (ignoreErrors) return null;
return atomSetCollection.errorMessage;
}return atomSetCollection;
}if (ignoreErrors) return null;
return "unknown reader error";
}if (Clazz.instanceOf (is, java.io.BufferedInputStream)) is = JU.Rdr.getPngZipStream (is, true);
var zis = JU.Rdr.newZipInputStream (is);
var ze;
if (haveManifest) manifest = '|' + manifest.$replace ('\r', '|').$replace ('\n', '|') + '|';
while ((ze = zis.getNextEntry ()) != null && (selectedFile <= 0 || vCollections.size () < selectedFile)) {
if (ze.isDirectory ()) continue;
var thisEntry = ze.getName ();
if (subFileName != null && !thisEntry.equals (subFileName)) continue;
if (subFileName != null) htParams.put ("subFileName", subFileName);
if (thisEntry.startsWith ("JmolManifest") || haveManifest && exceptFiles == manifest.indexOf ("|" + thisEntry + "|") >= 0) continue;
var bytes = JU.Rdr.getLimitedStreamBytes (zis, ze.getSize ());
if (JU.Rdr.isGzipB (bytes)) bytes = JU.Rdr.getLimitedStreamBytes (JU.ZipTools.getUnGzippedInputStream (bytes), -1);
if (JU.Rdr.isZipB (bytes) || JU.Rdr.isPngZipB (bytes)) {
var bis = JU.Rdr.getBIS (bytes);
var zipDir2 = JU.Rdr.getZipDirectoryAndClose (bis, "JmolManifest");
bis = JU.Rdr.getBIS (bytes);
var atomSetCollections = this.getAtomSetCollectionOrBufferedReaderFromZip (zpt, adapter, bis, fileName + "|" + thisEntry, zipDir2, htParams, ++subFilePtr, asBufferedReader);
if (Clazz.instanceOf (atomSetCollections, String)) {
if (ignoreErrors) continue;
return atomSetCollections;
} else if (Clazz.instanceOf (atomSetCollections, J.adapter.smarter.AtomSetCollection) || Clazz.instanceOf (atomSetCollections, JU.Lst)) {
if (haveManifest && !exceptFiles) htCollections.put (thisEntry, atomSetCollections);
 else vCollections.addLast (atomSetCollections);
} else if (Clazz.instanceOf (atomSetCollections, java.io.BufferedReader)) {
if (doCombine) zis.close ();
return atomSetCollections;
} else {
if (ignoreErrors) continue;
zis.close ();
return "unknown zip reader error";
}} else if (JU.Rdr.isPickleB (bytes)) {
var bis = JU.Rdr.getBIS (bytes);
if (doCombine) zis.close ();
return bis;
} else {
var sData;
if (JU.Rdr.isCompoundDocumentB (bytes)) {
var jd = J.api.Interface.getInterface ("JU.CompoundDocument");
jd.setStream (JU.Rdr.getBIS (bytes), true);
sData = jd.getAllDataFiles ("Molecule", "Input").toString ();
} else {
sData = JU.Rdr.fixUTF (bytes);
}var reader = JU.Rdr.getBR (sData);
if (asBufferedReader) {
if (doCombine) zis.close ();
return reader;
}var fname = fileName + "|" + ze.getName ();
ret = adapter.getAtomSetCollectionFromReader (fname, reader, htParams);
if (!(Clazz.instanceOf (ret, J.adapter.smarter.AtomSetCollection))) {
if (ignoreErrors) continue;
zis.close ();
return "" + ret;
}if (haveManifest && !exceptFiles) htCollections.put (thisEntry, ret);
 else vCollections.addLast (ret);
var a = ret;
if (a.errorMessage != null) {
if (ignoreErrors) continue;
zis.close ();
return a.errorMessage;
}}}
if (doCombine) zis.close ();
if (haveManifest && !exceptFiles) {
var list = JU.PT.split (manifest, "|");
for (var i = 0; i < list.length; i++) {
var file = list[i];
if (file.length == 0 || file.indexOf ("#") == 0) continue;
if (htCollections.containsKey (file)) vCollections.addLast (htCollections.get (file));
 else if (JU.Logger.debugging) JU.Logger.debug ("manifested file " + file + " was not found in " + fileName);
}
}if (!doCombine) return vCollections;
var result = (vCollections.size () == 1 && Clazz.instanceOf (vCollections.get (0), J.adapter.smarter.AtomSetCollection) ? vCollections.get (0) :  new J.adapter.smarter.AtomSetCollection ("Array", null, null, vCollections));
if (result.errorMessage != null) {
if (ignoreErrors) return null;
return result.errorMessage;
}if (nFiles == 1) selectedFile = 1;
if (selectedFile > 0 && selectedFile <= vCollections.size ()) return vCollections.get (selectedFile - 1);
return result;
} catch (e$$) {
if (Clazz.exceptionOf (e$$, Exception)) {
var e = e$$;
{
if (ignoreErrors) return null;
JU.Logger.error ("" + e);
return "" + e;
}
} else if (Clazz.exceptionOf (e$$, Error)) {
var er = e$$;
{
JU.Logger.errorEx (null, er);
return "" + er;
}
} else {
throw e$$;
}
}
}, "javajs.api.GenericZipTools,J.api.JmolAdapter,java.io.InputStream,~S,~A,java.util.Map,~N,~B");
Clazz.overrideMethod (c$, "getCachedPngjBytes", 
function (jmb, pathName) {
if (pathName.startsWith ("file:///")) pathName = "file:" + pathName.substring (7);
JU.Logger.info ("JmolUtil checking PNGJ cache for " + pathName);
var shortName = J.io.JmolUtil.shortSceneFilename (pathName);
if (jmb.pngjCache == null && !jmb.clearAndCachePngjFile ([pathName, null])) return null;
var isMin = (pathName.indexOf (".min.") >= 0);
if (!isMin) {
var cName = jmb.fm.getCanonicalName (JU.Rdr.getZipRoot (pathName));
if (!jmb.pngjCache.containsKey (cName) && !jmb.clearAndCachePngjFile ([pathName, null])) return null;
if (pathName.indexOf ("|") < 0) shortName = cName;
}if (jmb.pngjCache.containsKey (shortName)) {
JU.Logger.info ("FileManager using memory cache " + shortName);
return jmb.pngjCache.get (shortName);
}if (!isMin || !jmb.clearAndCachePngjFile ([pathName, null])) return null;
JU.Logger.info ("FileManager using memory cache " + shortName);
return jmb.pngjCache.get (shortName);
}, "J.io.JmolBinary,~S");
Clazz.overrideMethod (c$, "spartanFileList", 
function (zpt, name, type) {
var dirNums = J.io.JmolUtil.getSpartanDirs (type);
if (dirNums.length == 0 && name.endsWith (".spardir.zip") && type.indexOf (".zip|output") >= 0) {
var sname = name.$replace ('\\', '/');
var pt = name.lastIndexOf (".spardir");
pt = sname.lastIndexOf ("/");
sname = name + "|" + name.substring (pt + 1, name.length - 4);
return ["SpartanSmol", sname, sname + "/output"];
}return J.io.JmolUtil.getSpartanFileList (name, dirNums);
}, "javajs.api.GenericZipTools,~S,~S");
Clazz.overrideMethod (c$, "getImage", 
function (vwr, fullPathNameOrBytes, echoName) {
var image = null;
var info = null;
var apiPlatform = vwr.apiPlatform;
var createImage = false;
var fullPathName = "" + fullPathNameOrBytes;
if (Clazz.instanceOf (fullPathNameOrBytes, String)) {
if (fullPathName.indexOf ("|") > 0) {
var ret = vwr.fm.getFileAsBytes (fullPathName, null, true);
if (!JU.PT.isAB (ret)) return "" + ret;
image = (vwr.isJS ? ret : apiPlatform.createImage (ret));
} else if (vwr.isJS) {
} else if (JV.FileManager.urlTypeIndex (fullPathName) >= 0) {
try {
image = apiPlatform.createImage ( new java.net.URL (Clazz.castNullAs ("java.net.URL"), fullPathName, null));
} catch (e) {
if (Clazz.exceptionOf (e, Exception)) {
return "bad URL: " + fullPathName;
} else {
throw e;
}
}
} else {
createImage = true;
}} else if (vwr.isJS) {
image = fullPathNameOrBytes;
} else {
createImage = true;
}if (createImage) image = apiPlatform.createImage (fullPathNameOrBytes);
{
info = [echoName, fullPathNameOrBytes];
}try {
if (!apiPlatform.waitForDisplay (info, image)) return null;
{
return null;
}} catch (e) {
if (Clazz.exceptionOf (e, Exception)) {
return e.toString () + " opening " + fullPathName;
} else {
throw e;
}
}
}, "JV.Viewer,~O,~S");
Clazz.defineStatics (c$,
"DELPHI_BINARY_MAGIC_NUMBER", "\24\0\0\0");
});
