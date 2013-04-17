Clazz.declarePackage ("J.io2");
Clazz.load (null, ["J.io2.Huffman", "$.JpegEncoder", "$.JpegInfo", "$.DCT"], ["java.io.BufferedOutputStream", "$.ByteArrayOutputStream", "J.util.Logger"], function () {
c$ = Clazz.decorateAsClass (function () {
this.outStream = null;
this.JpegObj = null;
this.Huf = null;
this.dct = null;
this.Quality = 0;
Clazz.instantialize (this, arguments);
}, J.io2, "JpegEncoder");
Clazz.makeConstructor (c$, 
function (apiPlatform, image, quality, out, comment) {
this.Quality = quality;
this.JpegObj =  new J.io2.JpegInfo (apiPlatform, image, comment);
this.outStream =  new java.io.BufferedOutputStream (out);
this.dct =  new J.io2.DCT (this.Quality);
this.Huf =  new J.io2.Huffman (this.JpegObj.imageWidth, this.JpegObj.imageHeight);
}, "J.api.ApiPlatform,~O,~N,java.io.OutputStream,~S");
c$.getBytes = $_M(c$, "getBytes", 
function (apiPlatform, image, quality, comment) {
var os =  new java.io.ByteArrayOutputStream ();
J.io2.JpegEncoder.write (apiPlatform, image, quality, os, comment);
try {
os.flush ();
os.close ();
} catch (e) {
if (Clazz.exceptionOf (e, java.io.IOException)) {
} else {
throw e;
}
}
return os.toByteArray ();
}, "J.api.ApiPlatform,~O,~N,~S");
c$.write = $_M(c$, "write", 
function (apiPlatform, image, quality, os, comment) {
( new J.io2.JpegEncoder (apiPlatform, image, quality, os, comment)).Compress ();
}, "J.api.ApiPlatform,~O,~N,java.io.OutputStream,~S");
$_M(c$, "setQuality", 
function (quality) {
this.dct =  new J.io2.DCT (quality);
}, "~N");
$_M(c$, "Compress", 
function () {
if (this.JpegObj == null) return;
var longState = J.io2.JpegEncoder.WriteHeaders (this.outStream, this.JpegObj, this.dct);
J.io2.JpegEncoder.WriteCompressedData (this.outStream, this.JpegObj, this.dct, this.Huf);
J.io2.JpegEncoder.WriteEOI (this.outStream);
if (longState != null) try {
var b = longState.getBytes ();
this.outStream.write (b, 0, b.length);
} catch (e1) {
if (Clazz.exceptionOf (e1, java.io.IOException)) {
System.out.println ("ERROR WRITING COMMENT");
} else {
throw e1;
}
}
try {
this.outStream.flush ();
} catch (e) {
if (Clazz.exceptionOf (e, java.io.IOException)) {
J.util.Logger.errorEx ("IO Error", e);
} else {
throw e;
}
}
});
c$.WriteCompressedData = $_M(c$, "WriteCompressedData", 
($fz = function (outStream, JpegObj, dct, Huf) {
var i;
var j;
var r;
var c;
var a;
var b;
var comp;
var xpos;
var ypos;
var xblockoffset;
var yblockoffset;
var inputArray;
var dctArray1 =  Clazz.newFloatArray (8, 8, 0);
var dctArray2 =  Clazz.newDoubleArray (8, 8, 0);
var dctArray3 =  Clazz.newIntArray (64, 0);
var lastDCvalue =  Clazz.newIntArray (JpegObj.NumberOfComponents, 0);
var MinBlockWidth;
var MinBlockHeight;
MinBlockWidth = ((Huf.ImageWidth % 8 != 0) ? Clazz.doubleToInt (Math.floor (Huf.ImageWidth / 8.0) + 1) * 8 : Huf.ImageWidth);
MinBlockHeight = ((Huf.ImageHeight % 8 != 0) ? Clazz.doubleToInt (Math.floor (Huf.ImageHeight / 8.0) + 1) * 8 : Huf.ImageHeight);
for (comp = 0; comp < JpegObj.NumberOfComponents; comp++) {
MinBlockWidth = Math.min (MinBlockWidth, JpegObj.BlockWidth[comp]);
MinBlockHeight = Math.min (MinBlockHeight, JpegObj.BlockHeight[comp]);
}
xpos = 0;
for (r = 0; r < MinBlockHeight; r++) {
for (c = 0; c < MinBlockWidth; c++) {
xpos = c * 8;
ypos = r * 8;
for (comp = 0; comp < JpegObj.NumberOfComponents; comp++) {
inputArray = JpegObj.Components[comp];
var VsampF = JpegObj.VsampFactor[comp];
var HsampF = JpegObj.HsampFactor[comp];
var QNumber = JpegObj.QtableNumber[comp];
var DCNumber = JpegObj.DCtableNumber[comp];
var ACNumber = JpegObj.ACtableNumber[comp];
for (i = 0; i < VsampF; i++) {
for (j = 0; j < HsampF; j++) {
xblockoffset = j * 8;
yblockoffset = i * 8;
for (a = 0; a < 8; a++) {
for (b = 0; b < 8; b++) {
dctArray1[a][b] = inputArray[ypos + yblockoffset + a][xpos + xblockoffset + b];
}
}
dctArray2 = J.io2.DCT.forwardDCT (dctArray1);
dctArray3 = J.io2.DCT.quantizeBlock (dctArray2, dct.divisors[QNumber]);
Huf.HuffmanBlockEncoder (outStream, dctArray3, lastDCvalue[comp], DCNumber, ACNumber);
lastDCvalue[comp] = dctArray3[0];
}
}
}
}
}
Huf.flushBuffer (outStream);
}, $fz.isPrivate = true, $fz), "java.io.BufferedOutputStream,J.io2.JpegInfo,J.io2.DCT,J.io2.Huffman");
c$.WriteEOI = $_M(c$, "WriteEOI", 
($fz = function (out) {
var EOI = [0xFF, 0xD9];
J.io2.JpegEncoder.WriteMarker (EOI, out);
}, $fz.isPrivate = true, $fz), "java.io.BufferedOutputStream");
c$.WriteHeaders = $_M(c$, "WriteHeaders", 
($fz = function (out, JpegObj, dct) {
var i;
var j;
var index;
var offset;
var tempArray;
var SOI = [0xFF, 0xD8];
J.io2.JpegEncoder.WriteMarker (SOI, out);
var JFIF =  Clazz.newByteArray (18, 0);
JFIF[0] = 0xff;
JFIF[1] = 0xe0;
JFIF[2] = 0;
JFIF[3] = 16;
JFIF[4] = 0x4a;
JFIF[5] = 0x46;
JFIF[6] = 0x49;
JFIF[7] = 0x46;
JFIF[8] = 0x00;
JFIF[9] = 0x01;
JFIF[10] = 0x00;
JFIF[11] = 0x00;
JFIF[12] = 0x00;
JFIF[13] = 0x01;
JFIF[14] = 0x00;
JFIF[15] = 0x01;
JFIF[16] = 0x00;
JFIF[17] = 0x00;
J.io2.JpegEncoder.writeArray (JFIF, out);
var comment = null;
if (JpegObj.Comment.length > 0) J.io2.JpegEncoder.writeString (JpegObj.Comment, 0xE1, out);
J.io2.JpegEncoder.writeString ("JPEG Encoder Copyright 1998, James R. Weeks and BioElectroMech.\n\n", 0xFE, out);
var DQT =  Clazz.newByteArray (134, 0);
DQT[0] = 0xFF;
DQT[1] = 0xDB;
DQT[2] = 0;
DQT[3] = 132;
offset = 4;
for (i = 0; i < 2; i++) {
DQT[offset++] = ((0) + i);
tempArray = dct.quantum[i];
for (j = 0; j < 64; j++) {
DQT[offset++] = tempArray[J.io2.Huffman.jpegNaturalOrder[j]];
}
}
J.io2.JpegEncoder.writeArray (DQT, out);
var SOF =  Clazz.newByteArray (19, 0);
SOF[0] = 0xFF;
SOF[1] = 0xC0;
SOF[2] = 0;
SOF[3] = 17;
SOF[4] = JpegObj.Precision;
SOF[5] = ((JpegObj.imageHeight >> 8) & 0xFF);
SOF[6] = ((JpegObj.imageHeight) & 0xFF);
SOF[7] = ((JpegObj.imageWidth >> 8) & 0xFF);
SOF[8] = ((JpegObj.imageWidth) & 0xFF);
SOF[9] = JpegObj.NumberOfComponents;
index = 10;
for (i = 0; i < SOF[9]; i++) {
SOF[index++] = JpegObj.CompID[i];
SOF[index++] = ((JpegObj.HsampFactor[i] << 4) + JpegObj.VsampFactor[i]);
SOF[index++] = JpegObj.QtableNumber[i];
}
J.io2.JpegEncoder.writeArray (SOF, out);
J.io2.JpegEncoder.WriteDHTHeader (J.io2.Huffman.bitsDCluminance, J.io2.Huffman.valDCluminance, out);
J.io2.JpegEncoder.WriteDHTHeader (J.io2.Huffman.bitsACluminance, J.io2.Huffman.valACluminance, out);
J.io2.JpegEncoder.WriteDHTHeader (J.io2.Huffman.bitsDCchrominance, J.io2.Huffman.valDCchrominance, out);
J.io2.JpegEncoder.WriteDHTHeader (J.io2.Huffman.bitsACchrominance, J.io2.Huffman.valACchrominance, out);
var SOS =  Clazz.newByteArray (14, 0);
SOS[0] = 0xFF;
SOS[1] = 0xDA;
SOS[2] = 0;
SOS[3] = 12;
SOS[4] = JpegObj.NumberOfComponents;
index = 5;
for (i = 0; i < SOS[4]; i++) {
SOS[index++] = JpegObj.CompID[i];
SOS[index++] = ((JpegObj.DCtableNumber[i] << 4) + JpegObj.ACtableNumber[i]);
}
SOS[index++] = JpegObj.Ss;
SOS[index++] = JpegObj.Se;
SOS[index++] = ((JpegObj.Ah << 4) + JpegObj.Al);
J.io2.JpegEncoder.writeArray (SOS, out);
return comment;
}, $fz.isPrivate = true, $fz), "java.io.BufferedOutputStream,J.io2.JpegInfo,J.io2.DCT");
c$.writeString = $_M(c$, "writeString", 
($fz = function (s, id, out) {
var len = s.length;
var i0 = 0;
var suffix = " #Jmol...\u0000";
while (i0 < len) {
var nBytes = len - i0;
if (nBytes > 65510) {
nBytes = 65500;
var pt = s.lastIndexOf ('\n', i0 + nBytes);
if (pt > i0 + 1) nBytes = pt - i0;
}if (i0 + nBytes == len) suffix = "";
J.io2.JpegEncoder.writeTag (nBytes + suffix.length, id, out);
J.io2.JpegEncoder.writeArray (s.substring (i0, i0 + nBytes).getBytes (), out);
if (suffix.length > 0) J.io2.JpegEncoder.writeArray (suffix.getBytes (), out);
i0 += nBytes;
}
}, $fz.isPrivate = true, $fz), "~S,~N,java.io.BufferedOutputStream");
c$.writeTag = $_M(c$, "writeTag", 
($fz = function (length, id, out) {
length += 2;
var COM =  Clazz.newByteArray (4, 0);
COM[0] = 0xFF;
COM[1] = id;
COM[2] = ((length >> 8) & 0xFF);
COM[3] = (length & 0xFF);
J.io2.JpegEncoder.writeArray (COM, out);
}, $fz.isPrivate = true, $fz), "~N,~N,java.io.BufferedOutputStream");
c$.WriteDHTHeader = $_M(c$, "WriteDHTHeader", 
function (bits, val, out) {
var DHT1;
var DHT2;
var DHT3;
var DHT4;
var bytes;
var temp;
var oldindex;
var intermediateindex;
var index = 4;
oldindex = 4;
DHT1 =  Clazz.newByteArray (17, 0);
DHT4 =  Clazz.newByteArray (4, 0);
DHT4[0] = 0xFF;
DHT4[1] = 0xC4;
bytes = 0;
DHT1[index++ - oldindex] = bits[0];
for (var j = 1; j < 17; j++) {
temp = bits[j];
DHT1[index++ - oldindex] = temp;
bytes += temp;
}
intermediateindex = index;
DHT2 =  Clazz.newByteArray (bytes, 0);
for (var j = 0; j < bytes; j++) {
DHT2[index++ - intermediateindex] = val[j];
}
DHT3 =  Clazz.newByteArray (index, 0);
java.lang.System.arraycopy (DHT4, 0, DHT3, 0, oldindex);
java.lang.System.arraycopy (DHT1, 0, DHT3, oldindex, 17);
java.lang.System.arraycopy (DHT2, 0, DHT3, oldindex + 17, bytes);
DHT4 = DHT3;
oldindex = index;
DHT4[2] = (((index - 2) >> 8) & 0xFF);
DHT4[3] = ((index - 2) & 0xFF);
J.io2.JpegEncoder.writeArray (DHT4, out);
}, "~A,~A,java.io.BufferedOutputStream");
c$.WriteMarker = $_M(c$, "WriteMarker", 
function (data, out) {
try {
out.write (data, 0, 2);
} catch (e) {
if (Clazz.exceptionOf (e, java.io.IOException)) {
J.util.Logger.errorEx ("IO Error", e);
} else {
throw e;
}
}
}, "~A,java.io.BufferedOutputStream");
c$.writeArray = $_M(c$, "writeArray", 
function (data, out) {
try {
out.write (data, 0, data.length);
} catch (e) {
if (Clazz.exceptionOf (e, java.io.IOException)) {
J.util.Logger.errorEx ("IO Error", e);
} else {
throw e;
}
}
}, "~A,java.io.BufferedOutputStream");
Clazz.defineStatics (c$,
"CONTINUE_MAX", 65500,
"CONTINUE_MAX_BUFFER", 65510);
c$ = Clazz.decorateAsClass (function () {
this.quantum = null;
this.divisors = null;
this.quantum_luminance = null;
this.DivisorsLuminance = null;
this.quantum_chrominance = null;
this.DivisorsChrominance = null;
Clazz.instantialize (this, arguments);
}, J.io2, "DCT");
Clazz.prepareFields (c$, function () {
this.quantum =  Clazz.newIntArray (2, 0);
this.divisors =  Clazz.newDoubleArray (2, 0);
this.quantum_luminance =  Clazz.newIntArray (64, 0);
this.DivisorsLuminance =  Clazz.newDoubleArray (64, 0);
this.quantum_chrominance =  Clazz.newIntArray (64, 0);
this.DivisorsChrominance =  Clazz.newDoubleArray (64, 0);
});
Clazz.makeConstructor (c$, 
function (quality) {
this.initMatrix (quality);
}, "~N");
$_M(c$, "initMatrix", 
($fz = function (quality) {
quality = (quality < 1 ? 1 : quality > 100 ? 100 : quality);
quality = (quality < 50 ? Clazz.doubleToInt (5000 / quality) : 200 - quality * 2);
this.quantum_luminance[0] = 16;
this.quantum_luminance[1] = 11;
this.quantum_luminance[2] = 10;
this.quantum_luminance[3] = 16;
this.quantum_luminance[4] = 24;
this.quantum_luminance[5] = 40;
this.quantum_luminance[6] = 51;
this.quantum_luminance[7] = 61;
this.quantum_luminance[8] = 12;
this.quantum_luminance[9] = 12;
this.quantum_luminance[10] = 14;
this.quantum_luminance[11] = 19;
this.quantum_luminance[12] = 26;
this.quantum_luminance[13] = 58;
this.quantum_luminance[14] = 60;
this.quantum_luminance[15] = 55;
this.quantum_luminance[16] = 14;
this.quantum_luminance[17] = 13;
this.quantum_luminance[18] = 16;
this.quantum_luminance[19] = 24;
this.quantum_luminance[20] = 40;
this.quantum_luminance[21] = 57;
this.quantum_luminance[22] = 69;
this.quantum_luminance[23] = 56;
this.quantum_luminance[24] = 14;
this.quantum_luminance[25] = 17;
this.quantum_luminance[26] = 22;
this.quantum_luminance[27] = 29;
this.quantum_luminance[28] = 51;
this.quantum_luminance[29] = 87;
this.quantum_luminance[30] = 80;
this.quantum_luminance[31] = 62;
this.quantum_luminance[32] = 18;
this.quantum_luminance[33] = 22;
this.quantum_luminance[34] = 37;
this.quantum_luminance[35] = 56;
this.quantum_luminance[36] = 68;
this.quantum_luminance[37] = 109;
this.quantum_luminance[38] = 103;
this.quantum_luminance[39] = 77;
this.quantum_luminance[40] = 24;
this.quantum_luminance[41] = 35;
this.quantum_luminance[42] = 55;
this.quantum_luminance[43] = 64;
this.quantum_luminance[44] = 81;
this.quantum_luminance[45] = 104;
this.quantum_luminance[46] = 113;
this.quantum_luminance[47] = 92;
this.quantum_luminance[48] = 49;
this.quantum_luminance[49] = 64;
this.quantum_luminance[50] = 78;
this.quantum_luminance[51] = 87;
this.quantum_luminance[52] = 103;
this.quantum_luminance[53] = 121;
this.quantum_luminance[54] = 120;
this.quantum_luminance[55] = 101;
this.quantum_luminance[56] = 72;
this.quantum_luminance[57] = 92;
this.quantum_luminance[58] = 95;
this.quantum_luminance[59] = 98;
this.quantum_luminance[60] = 112;
this.quantum_luminance[61] = 100;
this.quantum_luminance[62] = 103;
this.quantum_luminance[63] = 99;
J.io2.DCT.AANscale (this.DivisorsLuminance, this.quantum_luminance, quality);
for (var i = 4; i < 64; i++) this.quantum_chrominance[i] = 99;

this.quantum_chrominance[0] = 17;
this.quantum_chrominance[1] = 18;
this.quantum_chrominance[2] = 24;
this.quantum_chrominance[3] = 47;
this.quantum_chrominance[8] = 18;
this.quantum_chrominance[9] = 21;
this.quantum_chrominance[10] = 26;
this.quantum_chrominance[11] = 66;
this.quantum_chrominance[16] = 24;
this.quantum_chrominance[17] = 26;
this.quantum_chrominance[18] = 56;
this.quantum_chrominance[24] = 47;
this.quantum_chrominance[25] = 66;
J.io2.DCT.AANscale (this.DivisorsChrominance, this.quantum_chrominance, quality);
this.quantum[0] = this.quantum_luminance;
this.quantum[1] = this.quantum_chrominance;
this.divisors[0] = this.DivisorsLuminance;
this.divisors[1] = this.DivisorsChrominance;
}, $fz.isPrivate = true, $fz), "~N");
c$.AANscale = $_M(c$, "AANscale", 
($fz = function (divisors, values, quality) {
for (var j = 0; j < 64; j++) {
var temp = Clazz.doubleToInt ((values[j] * quality + 50) / 100);
values[j] = (temp < 1 ? 1 : temp > 255 ? 255 : temp);
}
for (var i = 0, index = 0; i < 8; i++) for (var j = 0; j < 8; j++, index++) divisors[index] = (0.125 / (values[index] * J.io2.DCT.AANscaleFactor[i] * J.io2.DCT.AANscaleFactor[j]));


}, $fz.isPrivate = true, $fz), "~A,~A,~N");
c$.forwardDCT = $_M(c$, "forwardDCT", 
function (input) {
var output =  Clazz.newDoubleArray (8, 8, 0);
var tmp0;
var tmp1;
var tmp2;
var tmp3;
var tmp4;
var tmp5;
var tmp6;
var tmp7;
var tmp10;
var tmp11;
var tmp12;
var tmp13;
var z1;
var z2;
var z3;
var z4;
var z5;
var z11;
var z13;
for (var i = 0; i < 8; i++) for (var j = 0; j < 8; j++) output[i][j] = (input[i][j] - 128.0);


for (var i = 0; i < 8; i++) {
tmp0 = output[i][0] + output[i][7];
tmp7 = output[i][0] - output[i][7];
tmp1 = output[i][1] + output[i][6];
tmp6 = output[i][1] - output[i][6];
tmp2 = output[i][2] + output[i][5];
tmp5 = output[i][2] - output[i][5];
tmp3 = output[i][3] + output[i][4];
tmp4 = output[i][3] - output[i][4];
tmp10 = tmp0 + tmp3;
tmp13 = tmp0 - tmp3;
tmp11 = tmp1 + tmp2;
tmp12 = tmp1 - tmp2;
output[i][0] = tmp10 + tmp11;
output[i][4] = tmp10 - tmp11;
z1 = (tmp12 + tmp13) * 0.707106781;
output[i][2] = tmp13 + z1;
output[i][6] = tmp13 - z1;
tmp10 = tmp4 + tmp5;
tmp11 = tmp5 + tmp6;
tmp12 = tmp6 + tmp7;
z5 = (tmp10 - tmp12) * 0.382683433;
z2 = 0.541196100 * tmp10 + z5;
z4 = 1.306562965 * tmp12 + z5;
z3 = tmp11 * 0.707106781;
z11 = tmp7 + z3;
z13 = tmp7 - z3;
output[i][5] = z13 + z2;
output[i][3] = z13 - z2;
output[i][1] = z11 + z4;
output[i][7] = z11 - z4;
}
for (var i = 0; i < 8; i++) {
tmp0 = output[0][i] + output[7][i];
tmp7 = output[0][i] - output[7][i];
tmp1 = output[1][i] + output[6][i];
tmp6 = output[1][i] - output[6][i];
tmp2 = output[2][i] + output[5][i];
tmp5 = output[2][i] - output[5][i];
tmp3 = output[3][i] + output[4][i];
tmp4 = output[3][i] - output[4][i];
tmp10 = tmp0 + tmp3;
tmp13 = tmp0 - tmp3;
tmp11 = tmp1 + tmp2;
tmp12 = tmp1 - tmp2;
output[0][i] = tmp10 + tmp11;
output[4][i] = tmp10 - tmp11;
z1 = (tmp12 + tmp13) * 0.707106781;
output[2][i] = tmp13 + z1;
output[6][i] = tmp13 - z1;
tmp10 = tmp4 + tmp5;
tmp11 = tmp5 + tmp6;
tmp12 = tmp6 + tmp7;
z5 = (tmp10 - tmp12) * 0.382683433;
z2 = 0.541196100 * tmp10 + z5;
z4 = 1.306562965 * tmp12 + z5;
z3 = tmp11 * 0.707106781;
z11 = tmp7 + z3;
z13 = tmp7 - z3;
output[5][i] = z13 + z2;
output[3][i] = z13 - z2;
output[1][i] = z11 + z4;
output[7][i] = z11 - z4;
}
return output;
}, "~A");
c$.quantizeBlock = $_M(c$, "quantizeBlock", 
function (inputData, divisorsCode) {
var outputData =  Clazz.newIntArray (64, 0);
for (var i = 0, index = 0; i < 8; i++) for (var j = 0; j < 8; j++, index++) outputData[index] = (Math.round (inputData[i][j] * divisorsCode[index]));


return outputData;
}, "~A,~A");
Clazz.defineStatics (c$,
"N", 8,
"NN", 64,
"AANscaleFactor", [1.0, 1.387039845, 1.306562965, 1.175875602, 1.0, 0.785694958, 0.541196100, 0.275899379]);
c$ = Clazz.decorateAsClass (function () {
this.bufferPutBits = 0;
this.bufferPutBuffer = 0;
this.ImageHeight = 0;
this.ImageWidth = 0;
this.DC_matrix0 = null;
this.AC_matrix0 = null;
this.DC_matrix1 = null;
this.AC_matrix1 = null;
this.DC_matrix = null;
this.AC_matrix = null;
this.NumOfDCTables = 0;
this.NumOfACTables = 0;
Clazz.instantialize (this, arguments);
}, J.io2, "Huffman");
Clazz.makeConstructor (c$, 
function (Width, Height) {
this.initHuf ();
this.ImageWidth = Width;
this.ImageHeight = Height;
}, "~N,~N");
$_M(c$, "HuffmanBlockEncoder", 
function (outStream, zigzag, prec, DCcode, ACcode) {
var temp;
var temp2;
var nbits;
var k;
var r;
var i;
this.NumOfDCTables = 2;
this.NumOfACTables = 2;
var matrixDC = this.DC_matrix[DCcode];
var matrixAC = this.AC_matrix[ACcode];
temp = temp2 = zigzag[0] - prec;
if (temp < 0) {
temp = -temp;
temp2--;
}nbits = 0;
while (temp != 0) {
nbits++;
temp >>= 1;
}
this.bufferIt (outStream, matrixDC[nbits][0], matrixDC[nbits][1]);
if (nbits != 0) {
this.bufferIt (outStream, temp2, nbits);
}r = 0;
for (k = 1; k < 64; k++) {
if ((temp = zigzag[J.io2.Huffman.jpegNaturalOrder[k]]) == 0) {
r++;
} else {
while (r > 15) {
this.bufferIt (outStream, matrixAC[0xF0][0], matrixAC[0xF0][1]);
r -= 16;
}
temp2 = temp;
if (temp < 0) {
temp = -temp;
temp2--;
}nbits = 1;
while ((temp >>= 1) != 0) {
nbits++;
}
i = (r << 4) + nbits;
this.bufferIt (outStream, matrixAC[i][0], matrixAC[i][1]);
this.bufferIt (outStream, temp2, nbits);
r = 0;
}}
if (r > 0) {
this.bufferIt (outStream, matrixAC[0][0], matrixAC[0][1]);
}}, "java.io.BufferedOutputStream,~A,~N,~N,~N");
$_M(c$, "bufferIt", 
function (outStream, code, size) {
var PutBuffer = code;
var PutBits = this.bufferPutBits;
PutBuffer &= (1 << size) - 1;
PutBits += size;
PutBuffer <<= 24 - PutBits;
PutBuffer |= this.bufferPutBuffer;
while (PutBits >= 8) {
var c = ((PutBuffer >> 16) & 0xFF);
try {
outStream.write (c);
} catch (e) {
if (Clazz.exceptionOf (e, java.io.IOException)) {
J.util.Logger.errorEx ("IO Error", e);
} else {
throw e;
}
}
if (c == 0xFF) {
try {
outStream.write (0);
} catch (e) {
if (Clazz.exceptionOf (e, java.io.IOException)) {
J.util.Logger.errorEx ("IO Error", e);
} else {
throw e;
}
}
}PutBuffer <<= 8;
PutBits -= 8;
}
this.bufferPutBuffer = PutBuffer;
this.bufferPutBits = PutBits;
}, "java.io.BufferedOutputStream,~N,~N");
$_M(c$, "flushBuffer", 
function (outStream) {
var PutBuffer = this.bufferPutBuffer;
var PutBits = this.bufferPutBits;
while (PutBits >= 8) {
var c = ((PutBuffer >> 16) & 0xFF);
try {
outStream.write (c);
} catch (e) {
if (Clazz.exceptionOf (e, java.io.IOException)) {
J.util.Logger.errorEx ("IO Error", e);
} else {
throw e;
}
}
if (c == 0xFF) {
try {
outStream.write (0);
} catch (e) {
if (Clazz.exceptionOf (e, java.io.IOException)) {
J.util.Logger.errorEx ("IO Error", e);
} else {
throw e;
}
}
}PutBuffer <<= 8;
PutBits -= 8;
}
if (PutBits > 0) {
var c = ((PutBuffer >> 16) & 0xFF);
try {
outStream.write (c);
} catch (e) {
if (Clazz.exceptionOf (e, java.io.IOException)) {
J.util.Logger.errorEx ("IO Error", e);
} else {
throw e;
}
}
}}, "java.io.BufferedOutputStream");
$_M(c$, "initHuf", 
($fz = function () {
this.DC_matrix0 =  Clazz.newIntArray (12, 2, 0);
this.DC_matrix1 =  Clazz.newIntArray (12, 2, 0);
this.AC_matrix0 =  Clazz.newIntArray (255, 2, 0);
this.AC_matrix1 =  Clazz.newIntArray (255, 2, 0);
this.DC_matrix =  Clazz.newIntArray (2, 0);
this.AC_matrix =  Clazz.newIntArray (2, 0);
var p;
var l;
var i;
var lastp;
var si;
var code;
var huffsize =  Clazz.newIntArray (257, 0);
var huffcode =  Clazz.newIntArray (257, 0);
p = 0;
for (l = 1; l <= 16; l++) {
for (i = J.io2.Huffman.bitsDCchrominance[l]; --i >= 0; ) {
huffsize[p++] = l;
}
}
huffsize[p] = 0;
lastp = p;
code = 0;
si = huffsize[0];
p = 0;
while (huffsize[p] != 0) {
while (huffsize[p] == si) {
huffcode[p++] = code;
code++;
}
code <<= 1;
si++;
}
for (p = 0; p < lastp; p++) {
this.DC_matrix1[J.io2.Huffman.valDCchrominance[p]][0] = huffcode[p];
this.DC_matrix1[J.io2.Huffman.valDCchrominance[p]][1] = huffsize[p];
}
p = 0;
for (l = 1; l <= 16; l++) {
for (i = J.io2.Huffman.bitsACchrominance[l]; --i >= 0; ) {
huffsize[p++] = l;
}
}
huffsize[p] = 0;
lastp = p;
code = 0;
si = huffsize[0];
p = 0;
while (huffsize[p] != 0) {
while (huffsize[p] == si) {
huffcode[p++] = code;
code++;
}
code <<= 1;
si++;
}
for (p = 0; p < lastp; p++) {
this.AC_matrix1[J.io2.Huffman.valACchrominance[p]][0] = huffcode[p];
this.AC_matrix1[J.io2.Huffman.valACchrominance[p]][1] = huffsize[p];
}
p = 0;
for (l = 1; l <= 16; l++) {
for (i = J.io2.Huffman.bitsDCluminance[l]; --i >= 0; ) {
huffsize[p++] = l;
}
}
huffsize[p] = 0;
lastp = p;
code = 0;
si = huffsize[0];
p = 0;
while (huffsize[p] != 0) {
while (huffsize[p] == si) {
huffcode[p++] = code;
code++;
}
code <<= 1;
si++;
}
for (p = 0; p < lastp; p++) {
this.DC_matrix0[J.io2.Huffman.valDCluminance[p]][0] = huffcode[p];
this.DC_matrix0[J.io2.Huffman.valDCluminance[p]][1] = huffsize[p];
}
p = 0;
for (l = 1; l <= 16; l++) {
for (i = J.io2.Huffman.bitsACluminance[l]; --i >= 0; ) {
huffsize[p++] = l;
}
}
huffsize[p] = 0;
lastp = p;
code = 0;
si = huffsize[0];
p = 0;
while (huffsize[p] != 0) {
while (huffsize[p] == si) {
huffcode[p++] = code;
code++;
}
code <<= 1;
si++;
}
for (var q = 0; q < lastp; q++) {
this.AC_matrix0[J.io2.Huffman.valACluminance[q]][0] = huffcode[q];
this.AC_matrix0[J.io2.Huffman.valACluminance[q]][1] = huffsize[q];
}
this.DC_matrix[0] = this.DC_matrix0;
this.DC_matrix[1] = this.DC_matrix1;
this.AC_matrix[0] = this.AC_matrix0;
this.AC_matrix[1] = this.AC_matrix1;
}, $fz.isPrivate = true, $fz));
Clazz.defineStatics (c$,
"bitsDCluminance", [0x00, 0, 1, 5, 1, 1, 1, 1, 1, 1, 0, 0, 0, 0, 0, 0, 0],
"valDCluminance", [0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11],
"bitsDCchrominance", [0x01, 0, 3, 1, 1, 1, 1, 1, 1, 1, 1, 1, 0, 0, 0, 0, 0],
"valDCchrominance", [0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11],
"bitsACluminance", [0x10, 0, 2, 1, 3, 3, 2, 4, 3, 5, 5, 4, 4, 0, 0, 1, 0x7d],
"valACluminance", [0x01, 0x02, 0x03, 0x00, 0x04, 0x11, 0x05, 0x12, 0x21, 0x31, 0x41, 0x06, 0x13, 0x51, 0x61, 0x07, 0x22, 0x71, 0x14, 0x32, 0x81, 0x91, 0xa1, 0x08, 0x23, 0x42, 0xb1, 0xc1, 0x15, 0x52, 0xd1, 0xf0, 0x24, 0x33, 0x62, 0x72, 0x82, 0x09, 0x0a, 0x16, 0x17, 0x18, 0x19, 0x1a, 0x25, 0x26, 0x27, 0x28, 0x29, 0x2a, 0x34, 0x35, 0x36, 0x37, 0x38, 0x39, 0x3a, 0x43, 0x44, 0x45, 0x46, 0x47, 0x48, 0x49, 0x4a, 0x53, 0x54, 0x55, 0x56, 0x57, 0x58, 0x59, 0x5a, 0x63, 0x64, 0x65, 0x66, 0x67, 0x68, 0x69, 0x6a, 0x73, 0x74, 0x75, 0x76, 0x77, 0x78, 0x79, 0x7a, 0x83, 0x84, 0x85, 0x86, 0x87, 0x88, 0x89, 0x8a, 0x92, 0x93, 0x94, 0x95, 0x96, 0x97, 0x98, 0x99, 0x9a, 0xa2, 0xa3, 0xa4, 0xa5, 0xa6, 0xa7, 0xa8, 0xa9, 0xaa, 0xb2, 0xb3, 0xb4, 0xb5, 0xb6, 0xb7, 0xb8, 0xb9, 0xba, 0xc2, 0xc3, 0xc4, 0xc5, 0xc6, 0xc7, 0xc8, 0xc9, 0xca, 0xd2, 0xd3, 0xd4, 0xd5, 0xd6, 0xd7, 0xd8, 0xd9, 0xda, 0xe1, 0xe2, 0xe3, 0xe4, 0xe5, 0xe6, 0xe7, 0xe8, 0xe9, 0xea, 0xf1, 0xf2, 0xf3, 0xf4, 0xf5, 0xf6, 0xf7, 0xf8, 0xf9, 0xfa],
"bitsACchrominance", [0x11, 0, 2, 1, 2, 4, 4, 3, 4, 7, 5, 4, 4, 0, 1, 2, 0x77],
"valACchrominance", [0x00, 0x01, 0x02, 0x03, 0x11, 0x04, 0x05, 0x21, 0x31, 0x06, 0x12, 0x41, 0x51, 0x07, 0x61, 0x71, 0x13, 0x22, 0x32, 0x81, 0x08, 0x14, 0x42, 0x91, 0xa1, 0xb1, 0xc1, 0x09, 0x23, 0x33, 0x52, 0xf0, 0x15, 0x62, 0x72, 0xd1, 0x0a, 0x16, 0x24, 0x34, 0xe1, 0x25, 0xf1, 0x17, 0x18, 0x19, 0x1a, 0x26, 0x27, 0x28, 0x29, 0x2a, 0x35, 0x36, 0x37, 0x38, 0x39, 0x3a, 0x43, 0x44, 0x45, 0x46, 0x47, 0x48, 0x49, 0x4a, 0x53, 0x54, 0x55, 0x56, 0x57, 0x58, 0x59, 0x5a, 0x63, 0x64, 0x65, 0x66, 0x67, 0x68, 0x69, 0x6a, 0x73, 0x74, 0x75, 0x76, 0x77, 0x78, 0x79, 0x7a, 0x82, 0x83, 0x84, 0x85, 0x86, 0x87, 0x88, 0x89, 0x8a, 0x92, 0x93, 0x94, 0x95, 0x96, 0x97, 0x98, 0x99, 0x9a, 0xa2, 0xa3, 0xa4, 0xa5, 0xa6, 0xa7, 0xa8, 0xa9, 0xaa, 0xb2, 0xb3, 0xb4, 0xb5, 0xb6, 0xb7, 0xb8, 0xb9, 0xba, 0xc2, 0xc3, 0xc4, 0xc5, 0xc6, 0xc7, 0xc8, 0xc9, 0xca, 0xd2, 0xd3, 0xd4, 0xd5, 0xd6, 0xd7, 0xd8, 0xd9, 0xda, 0xe2, 0xe3, 0xe4, 0xe5, 0xe6, 0xe7, 0xe8, 0xe9, 0xea, 0xf2, 0xf3, 0xf4, 0xf5, 0xf6, 0xf7, 0xf8, 0xf9, 0xfa],
"jpegNaturalOrder", [0, 1, 8, 16, 9, 2, 3, 10, 17, 24, 32, 25, 18, 11, 4, 5, 12, 19, 26, 33, 40, 48, 41, 34, 27, 20, 13, 6, 7, 14, 21, 28, 35, 42, 49, 56, 57, 50, 43, 36, 29, 22, 15, 23, 30, 37, 44, 51, 58, 59, 52, 45, 38, 31, 39, 46, 53, 60, 61, 54, 47, 55, 62, 63]);
c$ = Clazz.decorateAsClass (function () {
this.Comment = null;
this.imageobj = null;
this.imageHeight = 0;
this.imageWidth = 0;
this.BlockWidth = null;
this.BlockHeight = null;
this.Precision = 8;
this.NumberOfComponents = 3;
this.Components = null;
this.CompID = null;
this.HsampFactor = null;
this.VsampFactor = null;
this.QtableNumber = null;
this.DCtableNumber = null;
this.ACtableNumber = null;
this.lastColumnIsDummy = null;
this.lastRowIsDummy = null;
this.Ss = 0;
this.Se = 63;
this.Ah = 0;
this.Al = 0;
this.compWidth = null;
this.compHeight = null;
this.MaxHsampFactor = 0;
this.MaxVsampFactor = 0;
Clazz.instantialize (this, arguments);
}, J.io2, "JpegInfo");
Clazz.prepareFields (c$, function () {
this.CompID = [1, 2, 3];
this.HsampFactor = [1, 1, 1];
this.VsampFactor = [1, 1, 1];
this.QtableNumber = [0, 1, 1];
this.DCtableNumber = [0, 1, 1];
this.ACtableNumber = [0, 1, 1];
this.lastColumnIsDummy = [false, false, false];
this.lastRowIsDummy = [false, false, false];
});
Clazz.makeConstructor (c$, 
function (apiPlatform, image, comment) {
this.Components =  Clazz.newFloatArray (this.NumberOfComponents, 0);
this.compWidth =  Clazz.newIntArray (this.NumberOfComponents, 0);
this.compHeight =  Clazz.newIntArray (this.NumberOfComponents, 0);
this.BlockWidth =  Clazz.newIntArray (this.NumberOfComponents, 0);
this.BlockHeight =  Clazz.newIntArray (this.NumberOfComponents, 0);
this.imageobj = image;
this.imageWidth = apiPlatform.getImageWidth (image);
this.imageHeight = apiPlatform.getImageHeight (image);
this.Comment = comment;
this.getYCCArray (apiPlatform);
}, "J.api.ApiPlatform,~O,~S");
$_M(c$, "getYCCArray", 
($fz = function (apiPlatform) {
var r;
var g;
var b;
var y;
var x;
this.MaxHsampFactor = 1;
this.MaxVsampFactor = 1;
for (y = 0; y < this.NumberOfComponents; y++) {
this.MaxHsampFactor = Math.max (this.MaxHsampFactor, this.HsampFactor[y]);
this.MaxVsampFactor = Math.max (this.MaxVsampFactor, this.VsampFactor[y]);
}
for (y = 0; y < this.NumberOfComponents; y++) {
this.compWidth[y] = (Clazz.doubleToInt (((this.imageWidth % 8 != 0) ? (Clazz.doubleToInt (Math.ceil (this.imageWidth / 8.0))) * 8 : this.imageWidth) / this.MaxHsampFactor)) * this.HsampFactor[y];
if (this.compWidth[y] != ((Clazz.doubleToInt (this.imageWidth / this.MaxHsampFactor)) * this.HsampFactor[y])) {
this.lastColumnIsDummy[y] = true;
}this.BlockWidth[y] = Clazz.doubleToInt (Math.ceil (this.compWidth[y] / 8.0));
this.compHeight[y] = (Clazz.doubleToInt (((this.imageHeight % 8 != 0) ? (Clazz.doubleToInt (Math.ceil (this.imageHeight / 8.0))) * 8 : this.imageHeight) / this.MaxVsampFactor)) * this.VsampFactor[y];
if (this.compHeight[y] != ((Clazz.doubleToInt (this.imageHeight / this.MaxVsampFactor)) * this.VsampFactor[y])) {
this.lastRowIsDummy[y] = true;
}this.BlockHeight[y] = Clazz.doubleToInt (Math.ceil (this.compHeight[y] / 8.0));
}
var pixels;
{
pixels = null;
}pixels = apiPlatform.grabPixels (this.imageobj, this.imageWidth, this.imageHeight, pixels, 0, this.imageHeight);
var Y =  Clazz.newFloatArray (this.compHeight[0], this.compWidth[0], 0);
var Cr1 =  Clazz.newFloatArray (this.compHeight[0], this.compWidth[0], 0);
var Cb1 =  Clazz.newFloatArray (this.compHeight[0], this.compWidth[0], 0);
var index = 0;
for (y = 0; y < this.imageHeight; ++y) {
for (x = 0; x < this.imageWidth; ++x) {
r = ((pixels[index] >> 16) & 0xff);
g = ((pixels[index] >> 8) & 0xff);
b = (pixels[index] & 0xff);
Y[y][x] = ((0.299 * r + 0.587 * g + 0.114 * b));
Cb1[y][x] = 128 + ((-0.16874 * r - 0.33126 * g + 0.5 * b));
Cr1[y][x] = 128 + ((0.5 * r - 0.41869 * g - 0.08131 * b));
index++;
}
}
this.Components[0] = Y;
this.Components[1] = Cb1;
this.Components[2] = Cr1;
}, $fz.isPrivate = true, $fz), "J.api.ApiPlatform");
});
