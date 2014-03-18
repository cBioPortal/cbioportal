Clazz.declarePackage ("J.jvxl.readers");
Clazz.load (["J.jvxl.readers.SurfaceReader"], "J.jvxl.readers.SurfaceFileReader", ["J.api.Interface", "J.util.Parser"], function () {
c$ = Clazz.decorateAsClass (function () {
this.br = null;
this.binarydoc = null;
this.os = null;
this.line = null;
this.next = null;
Clazz.instantialize (this, arguments);
}, J.jvxl.readers, "SurfaceFileReader", J.jvxl.readers.SurfaceReader);
Clazz.prepareFields (c$, function () {
this.next =  Clazz.newIntArray (1, 0);
});
Clazz.makeConstructor (c$, 
function () {
Clazz.superConstructor (this, J.jvxl.readers.SurfaceFileReader, []);
});
Clazz.overrideMethod (c$, "init", 
function (sg) {
this.initSR (sg);
}, "J.jvxl.readers.SurfaceGenerator");
$_M(c$, "init2", 
function (sg, br) {
this.init2SFR (sg, br);
}, "J.jvxl.readers.SurfaceGenerator,java.io.BufferedReader");
$_M(c$, "init2SFR", 
function (sg, br) {
this.init (sg);
this.br = br;
}, "J.jvxl.readers.SurfaceGenerator,java.io.BufferedReader");
$_M(c$, "newBinaryDocument", 
function () {
return J.api.Interface.getOptionInterface ("io2.BinaryDocument");
});
Clazz.overrideMethod (c$, "setOutputStream", 
function (os) {
if (this.binarydoc == null) this.os = os;
 else this.sg.setOutputStream (this.binarydoc, os);
}, "java.io.OutputStream");
Clazz.overrideMethod (c$, "closeReader", 
function () {
this.closeReaderSFR ();
});
$_M(c$, "closeReaderSFR", 
function () {
if (this.br != null) try {
this.br.close ();
} catch (e) {
if (Clazz.exceptionOf (e, java.io.IOException)) {
} else {
throw e;
}
}
if (this.os != null) try {
this.os.flush ();
this.os.close ();
} catch (e) {
if (Clazz.exceptionOf (e, java.io.IOException)) {
} else {
throw e;
}
}
if (this.binarydoc != null) this.binarydoc.close ();
});
Clazz.overrideMethod (c$, "discardTempData", 
function (discardAll) {
this.closeReader ();
this.discardTempDataSR (discardAll);
}, "~B");
$_M(c$, "getTokens", 
function () {
return J.util.Parser.getTokensAt (this.line, 0);
});
$_M(c$, "parseFloat", 
function () {
return J.util.Parser.parseFloatNext (this.line, this.next);
});
$_M(c$, "parseFloatStr", 
function (s) {
this.next[0] = 0;
return J.util.Parser.parseFloatNext (s, this.next);
}, "~S");
$_M(c$, "parseFloatRange", 
function (s, iStart, iEnd) {
this.next[0] = iStart;
return J.util.Parser.parseFloatRange (s, iEnd, this.next);
}, "~S,~N,~N");
$_M(c$, "parseInt", 
function () {
return J.util.Parser.parseIntNext (this.line, this.next);
});
$_M(c$, "parseIntStr", 
function (s) {
this.next[0] = 0;
return J.util.Parser.parseIntNext (s, this.next);
}, "~S");
$_M(c$, "parseIntNext", 
function (s) {
return J.util.Parser.parseIntNext (s, this.next);
}, "~S");
$_M(c$, "parseFloatArrayStr", 
function (s) {
this.next[0] = 0;
return J.util.Parser.parseFloatArrayNext (s, this.next, null, null, null);
}, "~S");
$_M(c$, "parseFloatArray", 
function (a, strStart, strEnd) {
return J.util.Parser.parseFloatArrayNext (this.line, this.next, a, strStart, strEnd);
}, "~A,~S,~S");
$_M(c$, "getQuotedStringNext", 
function () {
return J.util.Parser.getQuotedStringNext (this.line, this.next);
});
$_M(c$, "skipTo", 
function (info, what) {
if (info != null) while (this.readLine ().indexOf (info) < 0) {
}
if (what != null) this.next[0] = this.line.indexOf (what) + what.length + 2;
}, "~S,~S");
$_M(c$, "readLine", 
function () {
this.line = this.br.readLine ();
if (this.line != null) {
this.nBytes += this.line.length;
if (this.os != null) {
var b = this.line.getBytes ();
this.os.write (b, 0, b.length);
{
this.os.writeByteAsInt(0x0A);
}}}return this.line;
});
});
