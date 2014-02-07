Clazz.declarePackage ("J.jvxl.readers");
Clazz.load (["J.jvxl.readers.MapFileReader"], "J.jvxl.readers.MrcBinaryReader", ["java.lang.Exception", "$.Float", "J.util.Logger", "$.SB"], function () {
c$ = Clazz.decorateAsClass (function () {
this.labels = null;
Clazz.instantialize (this, arguments);
}, J.jvxl.readers, "MrcBinaryReader", J.jvxl.readers.MapFileReader);
Clazz.makeConstructor (c$, 
function () {
Clazz.superConstructor (this, J.jvxl.readers.MrcBinaryReader, []);
});
Clazz.overrideMethod (c$, "init2", 
function (sg, brNull) {
var fileName = (sg.getReaderData ())[0];
this.init2MFR (sg, this.br);
this.binarydoc = this.newBinaryDocument ();
this.binarydoc.setStream (sg.getAtomDataServer ().getBufferedInputStream (fileName), true);
this.nSurfaces = 1;
if (this.params.thePlane == null) this.params.insideOut = !this.params.insideOut;
this.allowSigma = true;
}, "J.jvxl.readers.SurfaceGenerator,java.io.BufferedReader");
Clazz.overrideMethod (c$, "readParameters", 
function () {
var ispg;
var nsymbt;
var extra =  Clazz.newByteArray (100, 0);
var map =  Clazz.newByteArray (4, 0);
var machst =  Clazz.newByteArray (4, 0);
var rmsDeviation;
var nlabel;
this.nx = this.binarydoc.readInt ();
if (this.nx < 0 || this.nx > 256) {
this.binarydoc.setStream (null, false);
this.nx = this.binarydoc.swapBytesI (this.nx);
if (this.params.thePlane == null) this.params.insideOut = !this.params.insideOut;
if (this.nx < 0 || this.nx > 1000) {
J.util.Logger.info ("nx=" + this.nx + " not displayable as MRC file");
throw  new Exception ("MRC file type not readable");
}J.util.Logger.info ("reading nonstandard little-endian MRC file");
}this.ny = this.binarydoc.readInt ();
this.nz = this.binarydoc.readInt ();
this.mode = this.binarydoc.readInt ();
if (this.mode < 0 || this.mode > 6) {
this.binarydoc.setStream (null, false);
this.nx = this.binarydoc.swapBytesI (this.nx);
this.ny = this.binarydoc.swapBytesI (this.ny);
this.nz = this.binarydoc.swapBytesI (this.nz);
this.mode = this.binarydoc.swapBytesI (this.mode);
}J.util.Logger.info ("MRC header: mode: " + this.mode);
J.util.Logger.info ("MRC header: nx ny nz: " + this.nx + " " + this.ny + " " + this.nz);
this.nxyzStart[0] = this.binarydoc.readInt ();
this.nxyzStart[1] = this.binarydoc.readInt ();
this.nxyzStart[2] = this.binarydoc.readInt ();
J.util.Logger.info ("MRC header: nxyzStart: " + this.nxyzStart[0] + " " + this.nxyzStart[1] + " " + this.nxyzStart[2]);
this.na = this.binarydoc.readInt ();
this.nb = this.binarydoc.readInt ();
this.nc = this.binarydoc.readInt ();
if (this.na == 0) this.na = this.nx - 1;
if (this.nb == 0) this.nb = this.ny - 1;
if (this.nc == 0) this.nc = this.nz - 1;
J.util.Logger.info ("MRC header: na nb nc: " + this.na + " " + this.nb + " " + this.nc);
this.a = this.binarydoc.readFloat ();
this.b = this.binarydoc.readFloat ();
this.c = this.binarydoc.readFloat ();
this.alpha = this.binarydoc.readFloat ();
this.beta = this.binarydoc.readFloat ();
this.gamma = this.binarydoc.readFloat ();
this.mapc = this.binarydoc.readInt ();
this.mapr = this.binarydoc.readInt ();
this.maps = this.binarydoc.readInt ();
J.util.Logger.info ("MRC header: mapc mapr maps: " + this.mapc + " " + this.mapr + " " + this.maps);
if (this.mapc != 1 && this.params.thePlane == null) this.params.dataXYReversed = true;
this.dmin = this.binarydoc.readFloat ();
this.dmax = this.binarydoc.readFloat ();
this.dmean = this.binarydoc.readFloat ();
J.util.Logger.info ("MRC header: dmin,dmax,dmean: " + this.dmin + "," + this.dmax + "," + this.dmean);
ispg = this.binarydoc.readInt ();
nsymbt = this.binarydoc.readInt ();
J.util.Logger.info ("MRC header: ispg,nsymbt: " + ispg + "," + nsymbt);
this.binarydoc.readByteArray (extra, 0, extra.length);
this.origin.x = this.binarydoc.readFloat ();
this.origin.y = this.binarydoc.readFloat ();
this.origin.z = this.binarydoc.readFloat ();
J.util.Logger.info ("MRC header: origin: " + this.origin);
this.binarydoc.readByteArray (map, 0, map.length);
this.binarydoc.readByteArray (machst, 0, machst.length);
rmsDeviation = this.binarydoc.readFloat ();
J.util.Logger.info ("MRC header: rms: " + rmsDeviation);
nlabel = this.binarydoc.readInt ();
J.util.Logger.info ("MRC header: labels: " + nlabel);
this.labels =  new Array (nlabel);
if (nlabel > 0) this.labels[0] = "Jmol MrcBinaryReader";
for (var i = 0; i < 10; i++) {
var s = this.binarydoc.readString (80).trim ();
if (i < nlabel) {
this.labels[i] = s;
J.util.Logger.info (this.labels[i]);
}}
for (var i = 0; i < nsymbt; i += 80) {
var position = this.binarydoc.getPosition ();
var s = this.binarydoc.readString (80).trim ();
if (s.indexOf ('\0') != s.lastIndexOf ('\0')) {
J.util.Logger.error ("File indicates " + nsymbt + " symmetry lines, but " + i + " found!");
this.binarydoc.seek (position);
break;
}J.util.Logger.info ("MRC file symmetry information: " + s);
}
J.util.Logger.info ("MRC header: bytes read: " + this.binarydoc.getPosition () + "\n");
this.getVectorsAndOrigin ();
if (this.params.thePlane == null && (this.params.cutoffAutomatic || !Float.isNaN (this.params.sigma))) {
var sigma = (this.params.sigma < 0 || Float.isNaN (this.params.sigma) ? 1 : this.params.sigma);
this.params.cutoff = rmsDeviation * sigma + this.dmean;
J.util.Logger.info ("Cutoff set to (mean + rmsDeviation*" + sigma + " = " + this.params.cutoff + ")\n");
}this.jvxlFileHeaderBuffer =  new J.util.SB ();
this.jvxlFileHeaderBuffer.append ("MRC DATA ").append (nlabel > 0 ? this.labels[0] : "").append ("\n");
this.jvxlFileHeaderBuffer.append ("see http://ami.scripps.edu/software/mrctools/mrc_specification.php\n");
});
Clazz.overrideMethod (c$, "nextVoxel", 
function () {
var voxelValue;
switch (this.mode) {
case 0:
voxelValue = this.binarydoc.readByte ();
break;
case 1:
voxelValue = this.binarydoc.readShort ();
break;
default:
case 2:
voxelValue = this.binarydoc.readFloat ();
break;
case 3:
voxelValue = this.binarydoc.readShort ();
this.binarydoc.readShort ();
break;
case 4:
voxelValue = this.binarydoc.readFloat ();
this.binarydoc.readFloat ();
break;
case 6:
voxelValue = this.binarydoc.readUnsignedShort ();
break;
}
this.nBytes = this.binarydoc.getPosition ();
return voxelValue;
});
Clazz.overrideMethod (c$, "skipData", 
function (nPoints) {
for (var i = 0; i < nPoints; i++) switch (this.mode) {
case 0:
this.binarydoc.readByte ();
break;
case 1:
case 6:
this.binarydoc.readByteArray (J.jvxl.readers.MrcBinaryReader.b8, 0, 2);
break;
default:
case 2:
case 3:
this.binarydoc.readByteArray (J.jvxl.readers.MrcBinaryReader.b8, 0, 4);
break;
case 4:
this.binarydoc.readByteArray (J.jvxl.readers.MrcBinaryReader.b8, 0, 8);
break;
}

}, "~N");
Clazz.defineStatics (c$,
"b8",  Clazz.newByteArray (8, 0));
});
