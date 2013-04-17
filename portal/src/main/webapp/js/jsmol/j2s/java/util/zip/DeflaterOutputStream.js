Clazz.declarePackage ("java.util.zip");
Clazz.load (["JZ.DeflaterOutputStream"], "java.util.zip.DeflaterOutputStream", null, function () {
c$ = Clazz.declareType (java.util.zip, "DeflaterOutputStream", JZ.DeflaterOutputStream);
Clazz.makeConstructor (c$, 
function (out, deflater) {
Clazz.superConstructor (this, java.util.zip.DeflaterOutputStream, [out, deflater, 0, true]);
}, "java.io.OutputStream,java.util.zip.Deflater");
});
