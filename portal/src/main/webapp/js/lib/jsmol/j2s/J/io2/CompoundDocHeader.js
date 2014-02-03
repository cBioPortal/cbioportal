Clazz.declarePackage ("J.io2");
Clazz.load (null, "J.io2.CompoundDocHeader", ["J.util.Logger"], function () {
c$ = Clazz.decorateAsClass (function () {
this.cd = null;
this.magicNumbers = null;
this.uniqueID16 = null;
this.revNumber = 0;
this.verNumber = 0;
this.sectorPower = 0;
this.shortSectorPower = 0;
this.unused = null;
this.nSATsectors = 0;
this.SID_DIR_start = 0;
this.minBytesStandardStream = 0;
this.SID_SSAT_start = 0;
this.nSSATsectors = 0;
this.SID_MSAT_next = 0;
this.nAdditionalMATsectors = 0;
this.MSAT0 = null;
Clazz.instantialize (this, arguments);
}, J.io2, "CompoundDocHeader");
Clazz.prepareFields (c$, function () {
this.magicNumbers =  Clazz.newByteArray (8, 0);
this.uniqueID16 =  Clazz.newByteArray (16, 0);
this.unused =  Clazz.newByteArray (10, 0);
this.MSAT0 =  Clazz.newIntArray (109, 0);
});
Clazz.makeConstructor (c$, 
function (compoundDocument) {
this.cd = compoundDocument;
}, "J.io2.CompoundDocument");
$_M(c$, "readData", 
function () {
try {
this.cd.readByteArray (this.magicNumbers, 0, 8);
if (this.magicNumbers[0] != 0xD0 || this.magicNumbers[1] != 0xCF || this.magicNumbers[2] != 0x11 || this.magicNumbers[3] != 0xE0 || this.magicNumbers[4] != 0xA1 || this.magicNumbers[5] != 0xB1 || this.magicNumbers[6] != 0x1A || this.magicNumbers[7] != 0xE1) return false;
this.cd.readByteArray (this.uniqueID16, 0, 16);
this.revNumber = this.cd.readByte ();
this.cd.readByte ();
this.verNumber = this.cd.readByte ();
this.cd.readByte ();
var b1 = this.cd.readByte ();
var b2 = this.cd.readByte ();
this.cd.isBigEndian = (b1 == -1 && b2 == -2);
this.sectorPower = this.cd.readShort ();
this.shortSectorPower = this.cd.readShort ();
this.cd.readByteArray (this.unused, 0, 10);
this.nSATsectors = this.cd.readInt ();
this.SID_DIR_start = this.cd.readInt ();
this.cd.readByteArray (this.unused, 0, 4);
this.minBytesStandardStream = this.cd.readInt ();
this.SID_SSAT_start = this.cd.readInt ();
this.nSSATsectors = this.cd.readInt ();
this.SID_MSAT_next = this.cd.readInt ();
this.nAdditionalMATsectors = this.cd.readInt ();
for (var i = 0; i < 109; i++) this.MSAT0[i] = this.cd.readInt ();

} catch (e) {
if (Clazz.exceptionOf (e, Exception)) {
J.util.Logger.errorEx (null, e);
return false;
} else {
throw e;
}
}
return true;
});
});
