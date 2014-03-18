Clazz.declarePackage ("J.awtjs2d");
Clazz.load (["J.api.JmolFileAdapterInterface"], "J.awtjs2d.JmolFileAdapter", ["java.net.UnknownServiceException", "J.awtjs2d.WebOutputChannel"], function () {
c$ = Clazz.declareType (J.awtjs2d, "JmolFileAdapter", null, J.api.JmolFileAdapterInterface);
Clazz.overrideMethod (c$, "getBufferedFileInputStream", 
function (name) {
try {
throw  new java.net.UnknownServiceException ("No local file reading in JavaScript version of Jmol");
} catch (e) {
if (Clazz.exceptionOf (e, java.io.IOException)) {
return e.toString ();
} else {
throw e;
}
}
}, "~S");
Clazz.overrideMethod (c$, "getBufferedURLInputStream", 
function (url, outputBytes, post) {
try {
var conn = url.openConnection ();
if (outputBytes != null) conn.outputBytes (outputBytes);
 else if (post != null) conn.outputString (post);
return conn.getStringXBuilder ();
} catch (e) {
if (Clazz.exceptionOf (e, java.io.IOException)) {
return e.toString ();
} else {
throw e;
}
}
}, "java.net.URL,~A,~S");
Clazz.overrideMethod (c$, "openOutputChannel", 
function (privateKey, fm, fileName, asWriter) {
return  new J.awtjs2d.WebOutputChannel (fileName);
}, "~N,J.viewer.FileManager,~S,~B");
Clazz.overrideMethod (c$, "openFileInputStream", 
function (privateKey, fileName) {
return null;
}, "~N,~S");
Clazz.overrideMethod (c$, "getAbsolutePath", 
function (privateKey, fileName) {
return fileName;
}, "~N,~S");
Clazz.overrideMethod (c$, "getFileLength", 
function (privateKey, fileName) {
return 0;
}, "~N,~S");
Clazz.overrideMethod (c$, "openLogFile", 
function (privateKey, logFileName, asAppend) {
return null;
}, "~N,~S,~B");
});
