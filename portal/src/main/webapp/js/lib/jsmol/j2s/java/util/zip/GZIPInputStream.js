Clazz.declarePackage ("java.util.zip");
Clazz.load (["java.util.zip.InflaterInputStream", "$.CRC32"], "java.util.zip.GZIPInputStream", ["java.io.EOFException", "$.IOException", "java.util.zip.CheckedInputStream", "$.Inflater", "$.ZipException"], function () {
c$ = Clazz.decorateAsClass (function () {
this.crc = null;
this.eos = false;
this.$closed = false;
this.tmpbuf = null;
Clazz.instantialize (this, arguments);
}, java.util.zip, "GZIPInputStream", java.util.zip.InflaterInputStream);
Clazz.prepareFields (c$, function () {
this.crc =  new java.util.zip.CRC32 ();
this.tmpbuf =  Clazz.newByteArray (128, 0);
});
$_M(c$, "ensureOpen", 
($fz = function () {
if (this.$closed) {
throw  new java.io.IOException ("Stream closed");
}}, $fz.isPrivate = true, $fz));
Clazz.makeConstructor (c$, 
function ($in, size) {
Clazz.superConstructor (this, java.util.zip.GZIPInputStream, [$in,  new java.util.zip.Inflater ().init (0, true), size]);
this.readHeader ($in);
}, "java.io.InputStream,~N");
Clazz.overrideMethod (c$, "read", 
function (buf, off, len) {
this.ensureOpen ();
if (this.eos) {
return -1;
}var n = this.readInf (buf, off, len);
if (n == -1) {
if (this.readTrailer ()) this.eos = true;
 else return this.read (buf, off, len);
} else {
this.crc.update (buf, off, n);
}return n;
}, "~A,~N,~N");
$_M(c$, "close", 
function () {
if (!this.$closed) {
Clazz.superCall (this, java.util.zip.GZIPInputStream, "close", []);
this.eos = true;
this.$closed = true;
}});
$_M(c$, "readHeader", 
($fz = function (this_in) {
var $in =  new java.util.zip.CheckedInputStream (this_in, this.crc);
this.crc.reset ();
if (this.readUShort ($in) != 35615) {
throw  new java.util.zip.ZipException ("Not in GZIP format");
}if (this.readUByte ($in) != 8) {
throw  new java.util.zip.ZipException ("Unsupported compression method");
}var flg = this.readUByte ($in);
this.skipBytes ($in, 6);
var n = 10;
if ((flg & 4) == 4) {
var m = this.readUShort ($in);
this.skipBytes ($in, m);
n += m + 2;
}if ((flg & 8) == 8) {
do {
n++;
} while (this.readUByte ($in) != 0);
}if ((flg & 16) == 16) {
do {
n++;
} while (this.readUByte ($in) != 0);
}if ((flg & 2) == 2) {
var v = this.crc.getValue () & 0xffff;
if (this.readUShort ($in) != v) {
throw  new java.util.zip.ZipException ("Corrupt GZIP header");
}n += 2;
}this.crc.reset ();
return n;
}, $fz.isPrivate = true, $fz), "java.io.InputStream");
$_M(c$, "readTrailer", 
($fz = function () {
return true;
}, $fz.isPrivate = true, $fz));
$_M(c$, "readUShort", 
($fz = function ($in) {
var b = this.readUByte ($in);
return (this.readUByte ($in) << 8) | b;
}, $fz.isPrivate = true, $fz), "java.io.InputStream");
$_M(c$, "readUByte", 
($fz = function ($in) {
var b = $in.readByteAsInt ();
if (b == -1) {
throw  new java.io.EOFException ();
}if (b < -1 || b > 255) {
throw  new java.io.IOException (this.$in.getClass ().getName () + ".read() returned value out of range -1..255: " + b);
}return b;
}, $fz.isPrivate = true, $fz), "java.io.InputStream");
$_M(c$, "skipBytes", 
($fz = function ($in, n) {
while (n > 0) {
var len = $in.read (this.tmpbuf, 0, n < this.tmpbuf.length ? n : this.tmpbuf.length);
if (len == -1) {
throw  new java.io.EOFException ();
}n -= len;
}
}, $fz.isPrivate = true, $fz), "java.io.InputStream,~N");
Clazz.defineStatics (c$,
"GZIP_MAGIC", 0x8b1f,
"FHCRC", 2,
"FEXTRA", 4,
"FNAME", 8,
"FCOMMENT", 16);
});
