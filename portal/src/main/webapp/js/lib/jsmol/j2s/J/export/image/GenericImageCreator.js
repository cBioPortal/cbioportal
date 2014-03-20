Clazz.declarePackage ("J.export.image");
Clazz.load (["J.api.JmolImageCreatorInterface"], "J.export.image.GenericImageCreator", ["java.io.IOException", "java.lang.Error", "J.export.image.GenericPngEncoder", "J.io.Base64", "J.io2.JpegEncoder", "J.util.Escape", "$.Logger", "J.viewer.Viewer"], function () {
c$ = Clazz.decorateAsClass (function () {
this.viewer = null;
this.privateKey = 0;
Clazz.instantialize (this, arguments);
}, J["export"].image, "GenericImageCreator", null, J.api.JmolImageCreatorInterface);
Clazz.makeConstructor (c$, 
function () {
});
Clazz.overrideMethod (c$, "setViewer", 
function (viewer, privateKey) {
this.viewer = viewer;
this.privateKey = privateKey;
return this;
}, "J.api.JmolViewer,~N");
Clazz.overrideMethod (c$, "createImage", 
function (fileName, type, text, bytes_or_image, scripts, quality) {
var isBytes = (bytes_or_image != null);
var isText = (!isBytes && quality == -2147483648);
var os = null;
var len = -1;
try {
if (!this.viewer.checkPrivateKey (this.privateKey)) return "NO SECURITY";
if ("OutputStream".equals (type)) return this.viewer.openOutputChannel (this.privateKey, fileName, false);
if (isBytes) {
if (Clazz.instanceOf (bytes_or_image, Array)) {
len = (bytes_or_image).length;
os = this.viewer.openOutputChannel (this.privateKey, fileName, false);
var b = bytes_or_image;
os.write (b, 0, b.length);
os.flush ();
os.close ();
os = null;
} else {
this.getImageBytes (type, quality, fileName, scripts, bytes_or_image, null);
return fileName;
}} else if (isText) {
if (text == null) return "NO DATA";
var bw = this.viewer.openOutputChannel (this.privateKey, fileName, true);
len = text.length;
bw.write (text);
bw.close ();
} else {
len = 1;
var bytesOrError = this.getImageBytes (type, quality, fileName, scripts, null, null);
if (Clazz.instanceOf (bytesOrError, String)) return bytesOrError;
var bytes = bytesOrError;
if (bytes != null) return (fileName == null ? bytes :  String.instantialize (bytes));
len = this.viewer.getFileLength (this.privateKey, fileName);
}} catch (exc) {
if (Clazz.exceptionOf (exc, java.io.IOException)) {
J.util.Logger.errorEx ("IO Exception", exc);
return exc.toString ();
} else {
throw exc;
}
} finally {
if (os != null) {
try {
os.close ();
} catch (e) {
if (Clazz.exceptionOf (e, java.io.IOException)) {
} else {
throw e;
}
}
}}
return (len < 0 ? "Creation of " + fileName + " failed: " + this.viewer.getErrorMessageUn () : "OK " + type + " " + (len > 0 ? len + " " : "") + fileName + (quality == -2147483648 ? "" : "; quality=" + quality));
}, "~S,~S,~S,~O,~A,~N");
Clazz.overrideMethod (c$, "getImageBytes", 
function (type, quality, fileName, scripts, objImage, os) {
var bytes = null;
var errMsg = null;
type = type.toUpperCase ();
var isPDF = type.equals ("PDF");
var isOsTemp = (os == null && fileName != null && !isPDF);
var asBytes = (os == null && fileName == null && !isPDF);
var isImage = (objImage != null);
var image = (isImage ? objImage : this.viewer.getScreenImageBuffer (null, true));
try {
if (image == null) {
errMsg = this.viewer.getErrorMessage ();
} else {
var ret = null;
var includeState = (type.equals ("PNGJ") || !asBytes);
if (type.equals ("PNGJ") && includeState) ret = this.viewer.getWrappedState (fileName, scripts, true, true, this.viewer.apiPlatform.getImageWidth (image), this.viewer.apiPlatform.getImageHeight (image));
if (isOsTemp) os = this.viewer.openOutputChannel (this.privateKey, fileName, false);
if (type.equals ("JPEG") || type.equals ("JPG")) {
if (quality <= 0) quality = 100;
if (asBytes) {
bytes = J.io2.JpegEncoder.getBytes (this.viewer.apiPlatform, image, quality, J.viewer.Viewer.getJmolVersion ());
} else {
var caption = (includeState ? this.viewer.getWrappedState (null, null, true, false, this.viewer.apiPlatform.getImageWidth (image), this.viewer.apiPlatform.getImageHeight (image)) : J.viewer.Viewer.getJmolVersion ());
J.io2.JpegEncoder.write (this.viewer.apiPlatform, image, quality, os, caption);
}} else if (type.equals ("JPG64") || type.equals ("JPEG64")) {
if (quality <= 0) quality = 75;
bytes = J.io2.JpegEncoder.getBytes (this.viewer.apiPlatform, image, quality, J.viewer.Viewer.getJmolVersion ());
if (asBytes) {
bytes = J.io.Base64.getBytes64 (bytes);
} else {
J.io.Base64.write (bytes, os);
bytes = null;
}} else if (type.startsWith ("PNG")) {
if (quality < 0) quality = 2;
 else if (quality > 9) quality = 9;
var bgcolor = (type.equals ("PNGT") ? this.viewer.getBackgroundArgb () : 0);
var ptJmol =  Clazz.newIntArray (1, 0);
bytes = J["export"].image.GenericPngEncoder.getBytesType (this.viewer.apiPlatform, image, quality, bgcolor, type, ptJmol);
var b = null;
if (includeState) {
var nPNG = bytes.length;
b = bytes;
if (ret == null) ret = this.viewer.getWrappedState (null, scripts, true, false, this.viewer.apiPlatform.getImageWidth (image), this.viewer.apiPlatform.getImageHeight (image));
bytes = (J.util.Escape.isAB (ret) ? ret : (ret).getBytes ());
var nState = bytes.length;
J["export"].image.GenericPngEncoder.setJmolTypeText (ptJmol[0], b, nPNG, nState, type);
}if (!asBytes) {
if (b != null) os.write (b, 0, b.length);
os.write (bytes, 0, bytes.length);
b = bytes = null;
} else if (b != null) {
var bt =  Clazz.newByteArray (b.length + bytes.length, 0);
System.arraycopy (b, 0, bt, 0, b.length);
System.arraycopy (bytes, 0, bt, b.length, bytes.length);
bytes = bt;
b = bt = null;
}} else {
var errRet =  new Array (1);
bytes = this.getOtherBytes (fileName, image, type, asBytes, os, errRet);
errMsg = errRet[0];
}if (os != null) os.flush ();
if (isOsTemp) os.close ();
}} catch (e$$) {
if (Clazz.exceptionOf (e$$, java.io.IOException)) {
var e = e$$;
{
if (!isImage) this.viewer.releaseScreenImage ();
throw  new java.io.IOException ("" + e);
}
} else if (Clazz.exceptionOf (e$$, Error)) {
var er = e$$;
{
if (!isImage) this.viewer.releaseScreenImage ();
throw  new Error (er);
}
} else {
throw e$$;
}
}
if (!isImage) this.viewer.releaseScreenImage ();
if (errMsg != null) return errMsg;
return bytes;
}, "~S,~N,~S,~A,~O,java.io.OutputStream");
$_M(c$, "getOtherBytes", 
function (fileName, objImage, type, asBytes, os, errRet) {
errRet[0] = "file type " + type + " not available on this platform";
return null;
}, "~S,~O,~S,~B,java.io.OutputStream,~A");
Clazz.overrideMethod (c$, "clipImage", 
function (viewer, text) {
return null;
}, "J.api.JmolViewer,~S");
Clazz.overrideMethod (c$, "getClipboardText", 
function () {
return null;
});
});
