Clazz.declarePackage ("J.io");
Clazz.load (null, "J.io.JmolBinary", ["java.io.BufferedInputStream", "$.BufferedReader", "$.ByteArrayInputStream", "$.InputStreamReader", "$.StringReader", "J.api.Interface", "J.io.Base64", "$.Encoding", "$.LimitedLineReader", "J.util.ArrayUtil", "$.Logger", "$.Parser", "$.SB", "$.TextFormat", "J.viewer.FileManager"], function () {
c$ = Clazz.declareType (J.io, "JmolBinary");
c$.determineSurfaceTypeIs = $_M(c$, "determineSurfaceTypeIs", 
function (is) {
var br;
try {
br = J.io.JmolBinary.getBufferedReader ( new java.io.BufferedInputStream (is), "ISO-8859-1");
} catch (e) {
if (Clazz.exceptionOf (e, java.io.IOException)) {
return null;
} else {
throw e;
}
}
return J.io.JmolBinary.determineSurfaceFileType (br);
}, "java.io.InputStream");
c$.determineSurfaceFileType = $_M(c$, "determineSurfaceFileType", 
function (bufferedReader) {
var line = null;
var br = null;
try {
br =  new J.io.LimitedLineReader (bufferedReader, 16000);
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
var tokens = J.util.Parser.getTokens (line);
var line2 = br.readLineWithNewline ();
if (tokens.length == 2 && J.util.Parser.parseInt (tokens[0]) == 3 && J.util.Parser.parseInt (tokens[1]) != -2147483648) {
tokens = J.util.Parser.getTokens (line2);
if (tokens.length == 3 && J.util.Parser.parseInt (tokens[0]) != -2147483648 && J.util.Parser.parseInt (tokens[1]) != -2147483648 && J.util.Parser.parseInt (tokens[2]) != -2147483648) return "PltFormatted";
}var line3 = br.readLineWithNewline ();
if (line.startsWith ("v ") && line2.startsWith ("v ") && line3.startsWith ("v ")) return "Obj";
var nAtoms = J.util.Parser.parseInt (line3);
if (nAtoms == -2147483648) return (line3.indexOf ("+") == 0 ? "Jvxl+" : null);
if (nAtoms >= 0) return "Cube";
nAtoms = -nAtoms;
for (var i = 4 + nAtoms; --i >= 0; ) if ((line = br.readLineWithNewline ()) == null) return null;

var nSurfaces = J.util.Parser.parseInt (line);
if (nSurfaces == -2147483648) return null;
return (nSurfaces < 0 ? "Jvxl" : "Cube");
}, "java.io.BufferedReader");
c$.getUTFEncodingForStream = $_M(c$, "getUTFEncodingForStream", 
($fz = function (is) {
{
is.resetStream();
}var abMagic =  Clazz.newByteArray (4, 0);
abMagic[3] = 1;
try {
is.mark (5);
} catch (e) {
if (Clazz.exceptionOf (e, Exception)) {
return J.io.Encoding.NONE;
} else {
throw e;
}
}
is.read (abMagic, 0, 4);
is.reset ();
return J.io.JmolBinary.getUTFEncoding (abMagic);
}, $fz.isPrivate = true, $fz), "java.io.BufferedInputStream");
c$.fixUTF = $_M(c$, "fixUTF", 
function (bytes) {
var encoding = J.io.JmolBinary.getUTFEncoding (bytes);
if (encoding !== J.io.Encoding.NONE) try {
var s =  String.instantialize (bytes, encoding.name ().$replace ('_', '-'));
switch (encoding) {
case J.io.Encoding.UTF8:
case J.io.Encoding.UTF_16BE:
case J.io.Encoding.UTF_16LE:
s = s.substring (1);
break;
default:
break;
}
return s;
} catch (e) {
if (Clazz.exceptionOf (e, java.io.UnsupportedEncodingException)) {
System.out.println (e);
} else {
throw e;
}
}
return  String.instantialize (bytes);
}, "~A");
c$.getUTFEncoding = $_M(c$, "getUTFEncoding", 
($fz = function (bytes) {
if (bytes.length >= 3 && bytes[0] == 0xEF && bytes[1] == 0xBB && bytes[2] == 0xBF) return J.io.Encoding.UTF8;
if (bytes.length >= 4 && bytes[0] == 0 && bytes[1] == 0 && bytes[2] == 0xFE && bytes[3] == 0xFF) return J.io.Encoding.UTF_32BE;
if (bytes.length >= 4 && bytes[0] == 0xFF && bytes[1] == 0xFE && bytes[2] == 0 && bytes[3] == 0) return J.io.Encoding.UTF_32LE;
if (bytes.length >= 2 && bytes[0] == 0xFF && bytes[1] == 0xFE) return J.io.Encoding.UTF_16LE;
if (bytes.length >= 2 && bytes[0] == 0xFE && bytes[1] == 0xFF) return J.io.Encoding.UTF_16BE;
return J.io.Encoding.NONE;
}, $fz.isPrivate = true, $fz), "~A");
c$.getMagic = $_M(c$, "getMagic", 
($fz = function (is, n) {
var abMagic =  Clazz.newByteArray (n, 0);
{
is.resetStream();
}try {
is.mark (n + 1);
is.read (abMagic, 0, n);
} catch (e) {
if (Clazz.exceptionOf (e, java.io.IOException)) {
} else {
throw e;
}
}
try {
is.reset ();
} catch (e) {
if (Clazz.exceptionOf (e, java.io.IOException)) {
} else {
throw e;
}
}
return abMagic;
}, $fz.isPrivate = true, $fz), "java.io.InputStream,~N");
c$.isCompoundDocumentS = $_M(c$, "isCompoundDocumentS", 
function (is) {
return J.io.JmolBinary.isCompoundDocumentB (J.io.JmolBinary.getMagic (is, 8));
}, "java.io.InputStream");
c$.isCompoundDocumentB = $_M(c$, "isCompoundDocumentB", 
function (bytes) {
return (bytes.length >= 8 && bytes[0] == 0xD0 && bytes[1] == 0xCF && bytes[2] == 0x11 && bytes[3] == 0xE0 && bytes[4] == 0xA1 && bytes[5] == 0xB1 && bytes[6] == 0x1A && bytes[7] == 0xE1);
}, "~A");
c$.isGzipS = $_M(c$, "isGzipS", 
function (is) {
return J.io.JmolBinary.isGzipB (J.io.JmolBinary.getMagic (is, 2));
}, "java.io.InputStream");
c$.isGzipB = $_M(c$, "isGzipB", 
function (bytes) {
return (bytes != null && bytes.length >= 2 && bytes[0] == 0x1F && bytes[1] == 0x8B);
}, "~A");
c$.isPickleS = $_M(c$, "isPickleS", 
function (is) {
return J.io.JmolBinary.isPickleB (J.io.JmolBinary.getMagic (is, 2));
}, "java.io.InputStream");
c$.isPickleB = $_M(c$, "isPickleB", 
function (bytes) {
return (bytes != null && bytes.length >= 2 && bytes[0] == 0x7D && bytes[1] == 0x71);
}, "~A");
c$.isZipS = $_M(c$, "isZipS", 
function (is) {
return J.io.JmolBinary.isZipB (J.io.JmolBinary.getMagic (is, 4));
}, "java.io.InputStream");
c$.isZipB = $_M(c$, "isZipB", 
function (bytes) {
return (bytes.length >= 4 && bytes[0] == 0x50 && bytes[1] == 0x4B && bytes[2] == 0x03 && bytes[3] == 0x04);
}, "~A");
c$.isPngZipStream = $_M(c$, "isPngZipStream", 
function (is) {
if (J.io.JmolBinary.isZipS (is)) return false;
var abMagic = J.io.JmolBinary.getMagic (is, 55);
return (abMagic[51] == 0x50 && abMagic[52] == 0x4E && abMagic[53] == 0x47 && abMagic[54] == 0x4A);
}, "java.io.InputStream");
c$.getZipRoot = $_M(c$, "getZipRoot", 
function (fileName) {
var pt = fileName.indexOf ("|");
return (pt < 0 ? fileName : fileName.substring (0, pt));
}, "~S");
c$.getStreamBytes = $_M(c$, "getStreamBytes", 
function (is, n) {
var buflen = (n > 0 && n < 1024 ? n : 1024);
var buf =  Clazz.newByteArray (buflen, 0);
var bytes =  Clazz.newByteArray (n < 0 ? 4096 : n, 0);
var len = 0;
var totalLen = 0;
while ((n < 0 || totalLen < n) && (len = is.read (buf, 0, buflen)) > 0) {
totalLen += len;
if (totalLen > bytes.length) bytes = J.util.ArrayUtil.ensureLengthByte (bytes, totalLen * 2);
System.arraycopy (buf, 0, bytes, totalLen - len, len);
}
if (totalLen == bytes.length) return bytes;
buf =  Clazz.newByteArray (totalLen, 0);
System.arraycopy (bytes, 0, buf, 0, totalLen);
return buf;
}, "java.io.InputStream,~N");
c$.getEmbeddedScript = $_M(c$, "getEmbeddedScript", 
function (script) {
if (script == null) return script;
var pt = script.indexOf ("**** Jmol Embedded Script ****");
if (pt < 0) return script;
var pt1 = script.lastIndexOf ("/*", pt);
var pt2 = script.indexOf ((script.charAt (pt1 + 2) == '*' ? "*" : "") + "*/", pt);
if (pt1 >= 0 && pt2 >= pt) script = script.substring (pt + "**** Jmol Embedded Script ****".length, pt2) + "\n";
while ((pt1 = script.indexOf (" #Jmol...\u0000")) >= 0) script = script.substring (0, pt1) + script.substring (pt1 + " #Jmol...\u0000".length + 4);

if (J.util.Logger.debugging) J.util.Logger.debug (script);
return script;
}, "~S");
c$.getJzu = $_M(c$, "getJzu", 
($fz = function () {
return (J.io.JmolBinary.jzu == null ? ($t$ = J.io.JmolBinary.jzu = J.api.Interface.getOptionInterface ("io2.ZipUtil"), J.io.JmolBinary.prototype.jzu = J.io.JmolBinary.jzu, $t$) : J.io.JmolBinary.jzu);
}, $fz.isPrivate = true, $fz));
c$.getZipDirectoryAsStringAndClose = $_M(c$, "getZipDirectoryAsStringAndClose", 
function (t) {
return J.io.JmolBinary.getJzu ().getZipDirectoryAsStringAndClose (t);
}, "java.io.BufferedInputStream");
c$.newGZIPInputStream = $_M(c$, "newGZIPInputStream", 
function (bis) {
return J.io.JmolBinary.getJzu ().newGZIPInputStream (bis);
}, "java.io.BufferedInputStream");
c$.getGzippedBytesAsString = $_M(c$, "getGzippedBytesAsString", 
function (t) {
return J.io.JmolBinary.getJzu ().getGzippedBytesAsString (t);
}, "~A");
c$.newZipInputStream = $_M(c$, "newZipInputStream", 
function ($in) {
return J.io.JmolBinary.getJzu ().newZipInputStream ($in);
}, "java.io.InputStream");
c$.getZipFileContents = $_M(c$, "getZipFileContents", 
function (bis, subFileList, listPtr, asBufferedInputStream) {
return J.io.JmolBinary.getJzu ().getZipFileContents (bis, subFileList, listPtr, asBufferedInputStream);
}, "java.io.BufferedInputStream,~A,~N,~B");
c$.getZipDirectoryAndClose = $_M(c$, "getZipDirectoryAndClose", 
function (t, addManifest) {
return J.io.JmolBinary.getJzu ().getZipDirectoryAndClose (t, addManifest);
}, "java.io.BufferedInputStream,~B");
c$.getAllZipData = $_M(c$, "getAllZipData", 
function (bis, subFileList, replace, string, fileData) {
J.io.JmolBinary.getJzu ().getAllZipData (bis, subFileList, replace, string, fileData);
}, "java.io.BufferedInputStream,~A,~S,~S,java.util.Map");
c$.getZipFileContentsAsBytes = $_M(c$, "getZipFileContentsAsBytes", 
function (bis, subFileList, i) {
return J.io.JmolBinary.getJzu ().getZipFileContentsAsBytes (bis, subFileList, i);
}, "java.io.BufferedInputStream,~A,~N");
c$.createZipSet = $_M(c$, "createZipSet", 
function (privateKey, fm, viewer, fileName, script, scripts, includeRemoteFiles) {
return J.io.JmolBinary.getJzu ().createZipSet (privateKey, fm, viewer, fileName, script, scripts, includeRemoteFiles);
}, "~N,J.viewer.FileManager,J.viewer.Viewer,~S,~S,~A,~B");
c$.getStreamAsBytes = $_M(c$, "getStreamAsBytes", 
function (bis, osb) {
var buf =  Clazz.newByteArray (1024, 0);
var bytes = (osb == null ?  Clazz.newByteArray (4096, 0) : null);
var len = 0;
var totalLen = 0;
while ((len = bis.read (buf, 0, 1024)) > 0) {
totalLen += len;
if (osb == null) {
if (totalLen >= bytes.length) bytes = J.util.ArrayUtil.ensureLengthByte (bytes, totalLen * 2);
System.arraycopy (buf, 0, bytes, totalLen - len, len);
} else {
osb.write (buf, 0, len);
}}
bis.close ();
if (osb == null) {
return J.util.ArrayUtil.arrayCopyByte (bytes, totalLen);
}return totalLen + " bytes";
}, "java.io.BufferedInputStream,J.io.OutputStringBuilder");
c$.writeZipFile = $_M(c$, "writeZipFile", 
function (privateKey, fm, viewer, outFileName, fileNamesAndByteArrays, msg) {
return J.io.JmolBinary.getJzu ().writeZipFile (privateKey, fm, viewer, outFileName, fileNamesAndByteArrays, msg);
}, "~N,J.viewer.FileManager,J.viewer.Viewer,~S,J.util.JmolList,~S");
c$.postByteArray = $_M(c$, "postByteArray", 
function (fm, outFileName, bytes) {
var ret = fm.getBufferedInputStreamOrErrorMessageFromName (outFileName, null, false, false, bytes, false);
if (Clazz.instanceOf (ret, String)) return ret;
try {
ret = J.io.JmolBinary.getStreamAsBytes (ret, null);
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
return J.io.JmolBinary.fixUTF (ret);
}, "J.viewer.FileManager,~S,~A");
c$.isBase64 = $_M(c$, "isBase64", 
function (sb) {
return (sb.indexOf (";base64,") == 0);
}, "J.util.SB");
c$.getBytesFromSB = $_M(c$, "getBytesFromSB", 
function (sb) {
return (J.io.JmolBinary.isBase64 (sb) ? J.io.Base64.decodeBase64 (sb.substring (8)) : sb.toBytes (0, -1));
}, "J.util.SB");
c$.getBISForStringXBuilder = $_M(c$, "getBISForStringXBuilder", 
function (sb) {
return  new java.io.BufferedInputStream ( new java.io.ByteArrayInputStream (J.io.JmolBinary.getBytesFromSB (sb)));
}, "J.util.SB");
c$.getBufferedReaderForString = $_M(c$, "getBufferedReaderForString", 
function (string) {
return  new java.io.BufferedReader ( new java.io.StringReader (string));
}, "~S");
c$.getSceneScript = $_M(c$, "getSceneScript", 
function (scenes, htScenes, list) {
return J.io.JmolBinary.getJzu ().getSceneScript (scenes, htScenes, list);
}, "~A,java.util.Map,J.util.JmolList");
c$.getCachedPngjBytes = $_M(c$, "getCachedPngjBytes", 
function (fm, pathName) {
return (pathName.indexOf (".png") < 0 ? null : J.io.JmolBinary.getJzu ().getCachedPngjBytes (fm, pathName));
}, "J.viewer.FileManager,~S");
c$.cachePngjFile = $_M(c$, "cachePngjFile", 
function (fm, data) {
return J.io.JmolBinary.getJzu ().cachePngjFile (fm, data);
}, "J.viewer.FileManager,~A");
c$.getAtomSetCollectionOrBufferedReaderFromZip = $_M(c$, "getAtomSetCollectionOrBufferedReaderFromZip", 
function (adapter, is, fileName, zipDirectory, htParams, asBufferedReader) {
return J.io.JmolBinary.getJzu ().getAtomSetCollectionOrBufferedReaderFromZip (adapter, is, fileName, zipDirectory, htParams, 1, asBufferedReader);
}, "J.api.JmolAdapter,java.io.InputStream,~S,~A,java.util.Map,~B");
c$.spartanFileList = $_M(c$, "spartanFileList", 
function (name, zipDirectory) {
return J.io.JmolBinary.getJzu ().spartanFileList (name, zipDirectory);
}, "~S,~S");
c$.getFileReferences = $_M(c$, "getFileReferences", 
function (script, fileList) {
for (var ipt = 0; ipt < J.viewer.FileManager.scriptFilePrefixes.length; ipt++) {
var tag = J.viewer.FileManager.scriptFilePrefixes[ipt];
var i = -1;
while ((i = script.indexOf (tag, i + 1)) >= 0) {
var s = J.util.Parser.getQuotedStringAt (script, i);
if (s.indexOf ("::") >= 0) s = J.util.TextFormat.splitChars (s, "::")[1];
if (Clazz.instanceOf (fileList, J.util.JmolList)) (fileList).addLast (s);
 else fileList.add (s);
}
}
}, "~S,java.util.List");
c$.checkPngZipStream = $_M(c$, "checkPngZipStream", 
function (bis) {
if (!J.io.JmolBinary.isPngZipStream (bis)) return bis;
var data = null;
bis.mark (75);
try {
data = J.io.JmolBinary.getStreamBytes (bis, 74);
bis.reset ();
var pt = 0;
for (var i = 64, f = 1; --i > 54; f *= 10) pt += (data[i] - 48) * f;

var n = 0;
for (var i = 74, f = 1; --i > 64; f *= 10) n += (data[i] - 48) * f;

while (pt > 0) pt -= bis.skip (pt);

data = J.io.JmolBinary.getStreamBytes (bis, n);
bis.close ();
} catch (e) {
data =  Clazz.newByteArray (0, 0);
}
return  new java.io.BufferedInputStream ( new java.io.ByteArrayInputStream (data));
}, "java.io.BufferedInputStream");
c$.getBufferedReader = $_M(c$, "getBufferedReader", 
function (bis, charSet) {
if (J.io.JmolBinary.getUTFEncodingForStream (bis) === J.io.Encoding.NONE) return  new java.io.BufferedReader ( new java.io.InputStreamReader (bis, (charSet == null ? "UTF-8" : charSet)));
var bytes = J.io.JmolBinary.getStreamBytes (bis, -1);
bis.close ();
return J.io.JmolBinary.getBufferedReaderForString (charSet == null ? J.io.JmolBinary.fixUTF (bytes) :  String.instantialize (bytes, charSet));
}, "java.io.BufferedInputStream,~S");
c$.getManifestScriptPath = $_M(c$, "getManifestScriptPath", 
function (manifest) {
if (manifest.indexOf ("$SCRIPT_PATH$") >= 0) return "";
var ch = (manifest.indexOf ('\n') >= 0 ? '\n' : '\r');
if (manifest.indexOf (".spt") >= 0) {
var s = J.util.TextFormat.split (manifest, ch);
for (var i = s.length; --i >= 0; ) if (s[i].indexOf (".spt") >= 0) return "|" + J.util.TextFormat.trim (s[i], "\r\n \t");

}return null;
}, "~S");
c$.getBinaryType = $_M(c$, "getBinaryType", 
function (name) {
if (name == null) return null;
var i = name.lastIndexOf (".");
if (i < 0 || (i = ";pse=PyMOL;".indexOf (";" + name.substring (i + 1) + "=")) < 0) return null;
i = ";pse=PyMOL;".indexOf ("=", i);
name = ";pse=PyMOL;".substring (i + 1);
return name.substring (0, name.indexOf (";"));
}, "~S");
c$.checkBinaryType = $_M(c$, "checkBinaryType", 
function (fileTypeIn) {
return (";pse=PyMOL;".indexOf ("=" + fileTypeIn + ";") >= 0);
}, "~S");
c$.StreamToString = $_M(c$, "StreamToString", 
function (bis) {
var data =  new Array (1);
try {
J.io.JmolBinary.readAll (J.io.JmolBinary.getBufferedReader (bis, "UTF-8"), -1, true, data, 0);
} catch (e) {
if (Clazz.exceptionOf (e, java.io.IOException)) {
} else {
throw e;
}
}
return data[0];
}, "java.io.BufferedInputStream");
c$.readAll = $_M(c$, "readAll", 
function (br, nBytesMax, allowBinary, data, i) {
try {
var sb = J.util.SB.newN (8192);
var line;
if (nBytesMax < 0) {
line = br.readLine ();
if (allowBinary || line != null && line.indexOf ('\0') < 0 && (line.length != 4 || line.charCodeAt (0) != 65533 || line.indexOf ("PNG") != 1)) {
sb.append (line).appendC ('\n');
while ((line = br.readLine ()) != null) sb.append (line).appendC ('\n');

}} else {
var n = 0;
var len;
while (n < nBytesMax && (line = br.readLine ()) != null) {
if (nBytesMax - n < (len = line.length) + 1) line = line.substring (0, nBytesMax - n - 1);
sb.append (line).appendC ('\n');
n += len + 1;
}
}br.close ();
data[i] = sb.toString ();
return true;
} catch (ioe) {
if (Clazz.exceptionOf (ioe, Exception)) {
data[i] = ioe.toString ();
return false;
} else {
throw ioe;
}
}
}, "java.io.BufferedReader,~N,~B,~A,~N");
Clazz.defineStatics (c$,
"JPEG_CONTINUE_STRING", " #Jmol...\0",
"PMESH_BINARY_MAGIC_NUMBER", "PM\1\0",
"DELPHI_BINARY_MAGIC_NUMBER", "\24\0\0\0",
"jzu", null);
});
