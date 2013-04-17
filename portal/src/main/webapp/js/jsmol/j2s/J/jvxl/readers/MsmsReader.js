Clazz.declarePackage ("J.jvxl.readers");
Clazz.load (["J.jvxl.readers.PmeshReader"], "J.jvxl.readers.MsmsReader", ["J.io.JmolBinary", "J.util.Logger", "$.TextFormat"], function () {
c$ = Clazz.decorateAsClass (function () {
this.fileName = null;
Clazz.instantialize (this, arguments);
}, J.jvxl.readers, "MsmsReader", J.jvxl.readers.PmeshReader);
Clazz.makeConstructor (c$, 
function () {
Clazz.superConstructor (this, J.jvxl.readers.MsmsReader, []);
});
$_M(c$, "init2", 
function (sg, br) {
Clazz.superCall (this, J.jvxl.readers.MsmsReader, "init2", [sg, br]);
this.fileName = (sg.getReaderData ())[0];
this.type = "msms";
this.onePerLine = true;
this.fixedCount = 3;
this.vertexBase = 1;
this.setHeader ();
}, "J.jvxl.readers.SurfaceGenerator,java.io.BufferedReader");
$_M(c$, "readVertices", 
function () {
this.skipHeader ();
return Clazz.superCall (this, J.jvxl.readers.MsmsReader, "readVertices", []);
});
$_M(c$, "readPolygons", 
function () {
this.br.close ();
this.fileName = J.util.TextFormat.simpleReplace (this.fileName, ".vert", ".face");
J.util.Logger.info ("reading from file " + this.fileName);
try {
this.br = J.io.JmolBinary.getBufferedReader (this.sg.getAtomDataServer ().getBufferedInputStream (this.fileName), null);
} catch (e) {
if (Clazz.exceptionOf (e, Exception)) {
J.util.Logger.info ("Note: file " + this.fileName + " was not found");
this.br = null;
return true;
} else {
throw e;
}
}
this.sg.addRequiredFile (this.fileName);
this.skipHeader ();
return Clazz.superCall (this, J.jvxl.readers.MsmsReader, "readPolygons", []);
});
$_M(c$, "skipHeader", 
($fz = function () {
while (this.readLine () != null && this.line.indexOf ("#") >= 0) {
}
this.tokens = this.getTokens ();
this.iToken = 0;
}, $fz.isPrivate = true, $fz));
});
