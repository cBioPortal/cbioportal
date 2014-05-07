Clazz.declarePackage ("J.jvxl.data");
Clazz.load (null, "J.jvxl.data.JvxlCoder", ["java.lang.Character", "$.Float", "J.io.XmlUtil", "J.jvxl.data.VolumeData", "J.util.BS", "$.BSUtil", "$.C", "$.Escape", "$.JmolList", "$.Logger", "$.P3", "$.Parser", "$.SB", "$.TextFormat"], function () {
c$ = Clazz.declareType (J.jvxl.data, "JvxlCoder");
c$.jvxlGetFile = $_M(c$, "jvxlGetFile", 
function (volumeData, jvxlData, title) {
var counts = volumeData.getVoxelCounts ();
jvxlData.nPointsX = counts[0];
jvxlData.nPointsY = counts[1];
jvxlData.nPointsZ = counts[2];
jvxlData.jvxlVolumeDataXml = volumeData.setVolumetricXml ();
return J.jvxl.data.JvxlCoder.jvxlGetFile (jvxlData, null, title, null, true, 1, null, null);
}, "J.jvxl.data.VolumeData,J.jvxl.data.JvxlData,~A");
c$.jvxlGetFile = $_M(c$, "jvxlGetFile", 
function (jvxlData, meshData, title, msg, includeHeader, nSurfaces, state, comment) {
return J.jvxl.data.JvxlCoder.jvxlGetFileXml (jvxlData, meshData, title, msg, includeHeader, nSurfaces, state, comment);
}, "J.jvxl.data.JvxlData,J.jvxl.data.MeshData,~A,~S,~B,~N,~S,~S");
c$.jvxlGetFileXml = $_M(c$, "jvxlGetFileXml", 
($fz = function (jvxlData, meshData, title, msg, includeHeader, nSurfaces, state, comment) {
var data =  new J.util.SB ();
if ("TRAILERONLY".equals (msg)) {
J.io.XmlUtil.closeTag (data, "jvxlSurfaceSet");
J.io.XmlUtil.closeTag (data, "jvxl");
return data.toString ();
}var vertexDataOnly = (meshData != null);
var isHeaderOnly = ("HEADERONLY".equals (msg));
if (includeHeader) {
J.io.XmlUtil.openDocument (data);
J.io.XmlUtil.openTagAttr (data, "jvxl", ["version", "2.3", "jmolVersion", jvxlData.version, "xmlns", "http://jmol.org/jvxl_schema", "xmlns:cml", "http://www.xml-cml.org/schema"]);
if (jvxlData.jvxlFileTitle != null) J.io.XmlUtil.appendCdata (data, "jvxlFileTitle", null, "\n" + jvxlData.jvxlFileTitle);
if (jvxlData.moleculeXml != null) data.append (jvxlData.moleculeXml);
var volumeDataXml = (vertexDataOnly ? null : jvxlData.jvxlVolumeDataXml);
if (volumeDataXml == null) volumeDataXml = ( new J.jvxl.data.VolumeData ()).setVolumetricXml ();
data.append (volumeDataXml);
J.io.XmlUtil.openTagAttr (data, "jvxlSurfaceSet", ["count", "" + (nSurfaces > 0 ? nSurfaces : 1)]);
if (isHeaderOnly) return data.toString ();
}var sb;
var type = (vertexDataOnly ? "pmesh" : jvxlData.jvxlPlane == null ? "isosurface" : "plane");
if (jvxlData.jvxlColorData != null && jvxlData.jvxlColorData.length > 0) type = "mapped " + type;
J.io.XmlUtil.openTagAttr (data, "jvxlSurface", ["type", type]);
data.append (J.jvxl.data.JvxlCoder.jvxlGetInfoData (jvxlData, vertexDataOnly));
J.jvxl.data.JvxlCoder.jvxlAppendCommandState (data, comment, state);
if (title != null || msg != null && msg.length > 0) {
sb =  new J.util.SB ();
if (msg != null && msg.length > 0) sb.append (msg).append ("\n");
if (title != null) for (var i = 0; i < title.length; i++) sb.append (title[i]).appendC ('\n');

J.io.XmlUtil.appendCdata (data, "jvxlSurfaceTitle", null, sb.toString ());
}sb =  new J.util.SB ();
J.io.XmlUtil.openTagAttr (sb, "jvxlSurfaceData", (vertexDataOnly || jvxlData.jvxlPlane == null ? null : jvxlData.mapLattice == null ? ["plane", J.util.Escape.eP4 (jvxlData.jvxlPlane)] : ["plane", J.util.Escape.eP4 (jvxlData.jvxlPlane), "maplattice", J.util.Escape.eP (jvxlData.mapLattice)]));
if (vertexDataOnly) {
J.jvxl.data.JvxlCoder.appendXmlVertexOnlyData (sb, jvxlData, meshData, true);
} else if (jvxlData.jvxlPlane == null) {
if (jvxlData.jvxlEdgeData == null) return "";
J.jvxl.data.JvxlCoder.appendXmlEdgeData (sb, jvxlData);
J.jvxl.data.JvxlCoder.appendXmlColorData (sb, jvxlData.jvxlColorData, true, jvxlData.isJvxlPrecisionColor, jvxlData.valueMappedToRed, jvxlData.valueMappedToBlue);
} else {
J.jvxl.data.JvxlCoder.appendXmlColorData (sb, jvxlData.jvxlColorData, true, jvxlData.isJvxlPrecisionColor, jvxlData.valueMappedToRed, jvxlData.valueMappedToBlue);
}J.jvxl.data.JvxlCoder.appendEncodedBitSetTag (sb, "jvxlInvalidatedVertexData", jvxlData.jvxlExcluded[1], -1, null);
if (jvxlData.excludedVertexCount > 0) {
J.jvxl.data.JvxlCoder.appendEncodedBitSetTag (sb, "jvxlExcludedVertexData", jvxlData.jvxlExcluded[0], jvxlData.excludedVertexCount, null);
J.jvxl.data.JvxlCoder.appendEncodedBitSetTag (sb, "jvxlExcludedPlaneData", jvxlData.jvxlExcluded[2], -1, null);
}J.jvxl.data.JvxlCoder.appendEncodedBitSetTag (sb, "jvxlExcludedTriangleData", jvxlData.jvxlExcluded[3], jvxlData.excludedTriangleCount, null);
J.io.XmlUtil.closeTag (sb, "jvxlSurfaceData");
var len = sb.length ();
data.appendSB (sb);
if (jvxlData.vContours != null && jvxlData.vContours.length > 0) {
J.jvxl.data.JvxlCoder.jvxlEncodeContourData (jvxlData.vContours, data);
}if (jvxlData.vertexColorMap != null) {
if (jvxlData.baseColor == null) J.io.XmlUtil.openTag (data, "jvxlVertexColorData");
 else J.io.XmlUtil.openTagAttr (data, "jvxlVertexColorData", ["baseColor", jvxlData.baseColor]);
for (var entry, $entry = jvxlData.vertexColorMap.entrySet ().iterator (); $entry.hasNext () && ((entry = $entry.next ()) || true);) J.jvxl.data.JvxlCoder.appendEncodedBitSetTag (data, "jvxlColorMap", entry.getValue (), -1, ["color", entry.getKey ()]);

jvxlData.vertexColorMap = null;
J.io.XmlUtil.closeTag (data, "jvxlVertexColorData");
}J.io.XmlUtil.closeTag (data, "jvxlSurface");
if (includeHeader) {
J.io.XmlUtil.closeTag (data, "jvxlSurfaceSet");
J.io.XmlUtil.closeTag (data, "jvxl");
}return J.jvxl.data.JvxlCoder.jvxlSetCompressionRatio (data, jvxlData, len);
}, $fz.isPrivate = true, $fz), "J.jvxl.data.JvxlData,J.jvxl.data.MeshData,~A,~S,~B,~N,~S,~S");
c$.appendEncodedBitSetTag = $_M(c$, "appendEncodedBitSetTag", 
($fz = function (sb, name, bs, count, attribs) {
if (count < 0) count = J.util.BSUtil.cardinalityOf (bs);
if (count == 0) return;
var sb1 =  new J.util.SB ();
sb1.append ("\n ");
J.jvxl.data.JvxlCoder.jvxlEncodeBitSetBuffer (bs, -1, sb1);
J.io.XmlUtil.appendTagObj (sb, name, [attribs, "bsEncoding", "base90+35", "count", "" + count, "len", "" + bs.length ()], J.jvxl.data.JvxlCoder.jvxlCompressString (sb1.toString (), true));
}, $fz.isPrivate = true, $fz), "J.util.SB,~S,J.util.BS,~N,~A");
c$.jvxlSetCompressionRatio = $_M(c$, "jvxlSetCompressionRatio", 
($fz = function (data, jvxlData, len) {
var s = data.toString ();
var r = Clazz.floatToInt (jvxlData.nBytes > 0 ? (jvxlData.nBytes) / len : ((jvxlData.nPointsX * jvxlData.nPointsY * jvxlData.nPointsZ * 13)) / len);
return J.util.TextFormat.simpleReplace (s, "\"not calculated\"", (r > 0 ? "\"" + r + ":1\"" : "\"?\""));
}, $fz.isPrivate = true, $fz), "J.util.SB,J.jvxl.data.JvxlData,~N");
c$.appendXmlEdgeData = $_M(c$, "appendXmlEdgeData", 
($fz = function (sb, jvxlData) {
J.io.XmlUtil.appendTagObj (sb, "jvxlEdgeData", ["count", "" + (jvxlData.jvxlEdgeData.length - 1), "encoding", "base90f1", "bsEncoding", "base90+35c", "isXLowToHigh", "" + jvxlData.isXLowToHigh, "data", J.jvxl.data.JvxlCoder.jvxlCompressString (jvxlData.jvxlEdgeData, true)], "\n" + J.jvxl.data.JvxlCoder.jvxlCompressString (jvxlData.jvxlSurfaceData, true));
}, $fz.isPrivate = true, $fz), "J.util.SB,J.jvxl.data.JvxlData");
c$.jvxlAppendCommandState = $_M(c$, "jvxlAppendCommandState", 
($fz = function (data, cmd, state) {
if (cmd != null) J.io.XmlUtil.appendCdata (data, "jvxlIsosurfaceCommand", null, "\n" + (cmd.indexOf ("#") < 0 ? cmd : cmd.substring (0, cmd.indexOf ("#"))) + "\n");
if (state != null) {
if (state.indexOf ("** XML ** ") >= 0) {
state = J.util.TextFormat.splitChars (state, "** XML **")[1].trim ();
J.io.XmlUtil.appendTag (data, "jvxlIsosurfaceState", "\n" + state + "\n");
} else {
J.io.XmlUtil.appendCdata (data, "jvxlIsosurfaceState", null, "\n" + state);
}}}, $fz.isPrivate = true, $fz), "J.util.SB,~S,~S");
c$.appendXmlColorData = $_M(c$, "appendXmlColorData", 
($fz = function (sb, data, isEncoded, isPrecisionColor, value1, value2) {
var n;
if (data == null || (n = data.length - 1) < 0) return;
if (isPrecisionColor) n /= 2;
J.io.XmlUtil.appendTagObj (sb, "jvxlColorData", ["count", "" + n, "encoding", (isEncoded ? "base90f" + (isPrecisionColor ? "2" : "1") : "none"), "min", "" + value1, "max", "" + value2, "data", J.jvxl.data.JvxlCoder.jvxlCompressString (data, true)], null);
}, $fz.isPrivate = true, $fz), "J.util.SB,~S,~B,~B,~N,~N");
c$.jvxlGetInfo = $_M(c$, "jvxlGetInfo", 
function (jvxlData) {
return J.jvxl.data.JvxlCoder.jvxlGetInfoData (jvxlData, jvxlData.vertexDataOnly);
}, "J.jvxl.data.JvxlData");
c$.jvxlGetInfoData = $_M(c$, "jvxlGetInfoData", 
function (jvxlData, vertexDataOnly) {
if (jvxlData.jvxlSurfaceData == null) return "";
var attribs =  new J.util.JmolList ();
var nSurfaceInts = jvxlData.nSurfaceInts;
var bytesUncompressedEdgeData = (vertexDataOnly ? 0 : jvxlData.jvxlEdgeData.length - 1);
var nColorData = (jvxlData.jvxlColorData == null ? -1 : (jvxlData.jvxlColorData.length - 1));
if (!vertexDataOnly) {
J.jvxl.data.JvxlCoder.addAttrib (attribs, "\n  cutoff", "" + jvxlData.cutoff);
J.jvxl.data.JvxlCoder.addAttrib (attribs, "\n  isCutoffAbsolute", "" + jvxlData.isCutoffAbsolute);
J.jvxl.data.JvxlCoder.addAttrib (attribs, "\n  pointsPerAngstrom", "" + jvxlData.pointsPerAngstrom);
var n = jvxlData.jvxlSurfaceData.length + bytesUncompressedEdgeData + nColorData + 1;
if (n > 0) J.jvxl.data.JvxlCoder.addAttrib (attribs, "\n  nBytesData", "" + n);
J.jvxl.data.JvxlCoder.addAttrib (attribs, "\n  isXLowToHigh", "" + jvxlData.isXLowToHigh);
if (jvxlData.jvxlPlane == null) {
J.jvxl.data.JvxlCoder.addAttrib (attribs, "\n  nSurfaceInts", "" + nSurfaceInts);
J.jvxl.data.JvxlCoder.addAttrib (attribs, "\n  nBytesUncompressedEdgeData", "" + bytesUncompressedEdgeData);
}if (nColorData > 0) J.jvxl.data.JvxlCoder.addAttrib (attribs, "\n  nBytesUncompressedColorData", "" + nColorData);
}jvxlData.excludedVertexCount = J.util.BSUtil.cardinalityOf (jvxlData.jvxlExcluded[0]);
jvxlData.excludedTriangleCount = J.util.BSUtil.cardinalityOf (jvxlData.jvxlExcluded[3]);
if (jvxlData.excludedVertexCount > 0) J.jvxl.data.JvxlCoder.addAttrib (attribs, "\n  nExcludedVertexes", "" + jvxlData.excludedVertexCount);
if (jvxlData.excludedTriangleCount > 0) J.jvxl.data.JvxlCoder.addAttrib (attribs, "\n  nExcludedTriangles", "" + jvxlData.excludedTriangleCount);
var n = J.util.BSUtil.cardinalityOf (jvxlData.jvxlExcluded[1]);
if (n > 0) J.jvxl.data.JvxlCoder.addAttrib (attribs, "\n  nInvalidatedVertexes", "" + n);
if (jvxlData.slabInfo != null) J.jvxl.data.JvxlCoder.addAttrib (attribs, "\n  slabInfo", jvxlData.slabInfo);
if (jvxlData.isJvxlPrecisionColor) J.jvxl.data.JvxlCoder.addAttrib (attribs, "\n  precisionColor", "true");
if (jvxlData.colorDensity) J.jvxl.data.JvxlCoder.addAttrib (attribs, "\n  colorDensity", "true");
if (!Float.isNaN (jvxlData.pointSize)) J.jvxl.data.JvxlCoder.addAttrib (attribs, "\n  pointSize", "" + jvxlData.pointSize);
 else if (jvxlData.diameter != 0) J.jvxl.data.JvxlCoder.addAttrib (attribs, "\n  diameter", "" + jvxlData.diameter);
if (!jvxlData.allowVolumeRender) J.jvxl.data.JvxlCoder.addAttrib (attribs, "\n  allowVolumeRender", "false");
if (jvxlData.jvxlPlane == null || vertexDataOnly) {
if (jvxlData.isContoured) {
J.jvxl.data.JvxlCoder.addAttrib (attribs, "\n  contoured", "true");
J.jvxl.data.JvxlCoder.addAttrib (attribs, "\n  colorMapped", "true");
} else if (jvxlData.isBicolorMap) {
J.jvxl.data.JvxlCoder.addAttrib (attribs, "\n  bicolorMap", "true");
J.jvxl.data.JvxlCoder.addAttrib (attribs, "\n  colorNegative", J.util.C.getHexCode (jvxlData.minColorIndex));
J.jvxl.data.JvxlCoder.addAttrib (attribs, "\n  colorPositive", J.util.C.getHexCode (jvxlData.maxColorIndex));
} else if (nColorData > 0) {
J.jvxl.data.JvxlCoder.addAttrib (attribs, "\n  colorMapped", "true");
}if (jvxlData.vContours != null && jvxlData.vContours.length > 0) J.jvxl.data.JvxlCoder.addAttrib (attribs, "\n  nContourData", "" + jvxlData.vContours.length);
} else {
if (jvxlData.mapLattice != null) J.jvxl.data.JvxlCoder.addAttrib (attribs, "\n  mapLattice", "" + jvxlData.mapLattice);
if (jvxlData.scale3d != 0) J.jvxl.data.JvxlCoder.addAttrib (attribs, "\n  scale3d", "" + jvxlData.scale3d);
if (nColorData > 0) J.jvxl.data.JvxlCoder.addAttrib (attribs, "\n  colorMapped", "true");
J.jvxl.data.JvxlCoder.addAttrib (attribs, "\n  plane", J.util.Escape.eP4 (jvxlData.jvxlPlane));
}if (jvxlData.color != null && jvxlData.color.indexOf ("null") < 0) J.jvxl.data.JvxlCoder.addAttrib (attribs, "\n  color", jvxlData.color);
J.jvxl.data.JvxlCoder.addAttrib (attribs, "\n  translucency", "" + jvxlData.translucency);
if (jvxlData.meshColor != null) J.jvxl.data.JvxlCoder.addAttrib (attribs, "\n  meshColor", jvxlData.meshColor);
if (jvxlData.colorScheme != null) J.jvxl.data.JvxlCoder.addAttrib (attribs, "\n  colorScheme", jvxlData.colorScheme);
if (jvxlData.rendering != null) J.jvxl.data.JvxlCoder.addAttrib (attribs, "\n  rendering", jvxlData.rendering);
if (jvxlData.thisSet >= 0) J.jvxl.data.JvxlCoder.addAttrib (attribs, "\n  set", "" + (jvxlData.thisSet + 1));
if (jvxlData.slabValue != -2147483648) J.jvxl.data.JvxlCoder.addAttrib (attribs, "\n  slabValue", "" + jvxlData.slabValue);
if (jvxlData.isSlabbable) J.jvxl.data.JvxlCoder.addAttrib (attribs, "\n  slabbable", "true");
if (jvxlData.nVertexColors > 0) J.jvxl.data.JvxlCoder.addAttrib (attribs, "\n  nVertexColors", "" + jvxlData.nVertexColors);
var min = (jvxlData.mappedDataMin == 3.4028235E38 ? 0 : jvxlData.mappedDataMin);
var blue = (jvxlData.isColorReversed ? jvxlData.valueMappedToRed : jvxlData.valueMappedToBlue);
var red = (jvxlData.isColorReversed ? jvxlData.valueMappedToBlue : jvxlData.valueMappedToRed);
if (jvxlData.jvxlColorData != null && jvxlData.jvxlColorData.length > 0 && !jvxlData.isBicolorMap) {
J.jvxl.data.JvxlCoder.addAttrib (attribs, "\n  dataMinimum", "" + min);
J.jvxl.data.JvxlCoder.addAttrib (attribs, "\n  dataMaximum", "" + jvxlData.mappedDataMax);
J.jvxl.data.JvxlCoder.addAttrib (attribs, "\n  valueMappedToRed", "" + red);
J.jvxl.data.JvxlCoder.addAttrib (attribs, "\n  valueMappedToBlue", "" + blue);
}if (jvxlData.isContoured) {
if (jvxlData.contourValues == null || jvxlData.contourColixes == null) {
if (jvxlData.vContours == null) J.jvxl.data.JvxlCoder.addAttrib (attribs, "\n  nContours", "" + Math.abs (jvxlData.nContours));
} else {
if (jvxlData.jvxlPlane != null) J.jvxl.data.JvxlCoder.addAttrib (attribs, "\n  contoured", "true");
J.jvxl.data.JvxlCoder.addAttrib (attribs, "\n  nContours", "" + jvxlData.contourValues.length);
J.jvxl.data.JvxlCoder.addAttrib (attribs, "\n  contourValues", J.util.Escape.eAF (jvxlData.contourValuesUsed == null ? jvxlData.contourValues : jvxlData.contourValuesUsed));
J.jvxl.data.JvxlCoder.addAttrib (attribs, "\n  contourColors", jvxlData.contourColors);
}}if (jvxlData.insideOut) J.jvxl.data.JvxlCoder.addAttrib (attribs, "\n  insideOut", "true");
if (jvxlData.vertexDataOnly) J.jvxl.data.JvxlCoder.addAttrib (attribs, "\n  note", "vertex/face data only");
 else if (jvxlData.isXLowToHigh) J.jvxl.data.JvxlCoder.addAttrib (attribs, "\n  note", "progressive JVXL+ -- X values read from low(0) to high(" + (jvxlData.nPointsX - 1) + ")");
J.jvxl.data.JvxlCoder.addAttrib (attribs, "\n  xyzMin", J.util.Escape.eP (jvxlData.boundingBox[0]));
J.jvxl.data.JvxlCoder.addAttrib (attribs, "\n  xyzMax", J.util.Escape.eP (jvxlData.boundingBox[1]));
J.jvxl.data.JvxlCoder.addAttrib (attribs, "\n  approximateCompressionRatio", "not calculated");
J.jvxl.data.JvxlCoder.addAttrib (attribs, "\n  jmolVersion", jvxlData.version);
var info =  new J.util.SB ();
J.io.XmlUtil.openTagAttr (info, "jvxlSurfaceInfo", attribs.toArray ( new Array (attribs.size ())));
J.io.XmlUtil.closeTag (info, "jvxlSurfaceInfo");
return info.toString ();
}, "J.jvxl.data.JvxlData,~B");
c$.addAttrib = $_M(c$, "addAttrib", 
($fz = function (attribs, name, value) {
attribs.addLast ([name, value]);
}, $fz.isPrivate = true, $fz), "J.util.JmolList,~S,~S");
c$.jvxlEncodeContourData = $_M(c$, "jvxlEncodeContourData", 
($fz = function (contours, sb) {
J.io.XmlUtil.openTagAttr (sb, "jvxlContourData", ["count", "" + contours.length]);
for (var i = 0; i < contours.length; i++) {
if (contours[i].size () < 6) {
continue;
}var nPolygons = (contours[i].get (0)).intValue ();
var sb1 =  new J.util.SB ();
sb1.append ("\n");
var bs = contours[i].get (1);
J.jvxl.data.JvxlCoder.jvxlEncodeBitSetBuffer (bs, nPolygons, sb1);
J.io.XmlUtil.appendTagObj (sb, "jvxlContour", ["index", "" + i, "value", "" + contours[i].get (2), "color", J.util.Escape.escapeColor ((contours[i].get (4))[0]), "count", "" + bs.length (), "encoding", "base90iff1", "bsEncoding", "base90+35c", "data", J.jvxl.data.JvxlCoder.jvxlCompressString (contours[i].get (5).toString (), true)], J.jvxl.data.JvxlCoder.jvxlCompressString (sb1.toString (), true));
}
J.io.XmlUtil.closeTag (sb, "jvxlContourData");
}, $fz.isPrivate = true, $fz), "~A,J.util.SB");
c$.set3dContourVector = $_M(c$, "set3dContourVector", 
function (v, polygonIndexes, vertices) {
if (v.size () < 6) return;
var fData = v.get (5);
var bs = v.get (1);
var pt = 0;
var nBuf = fData.length ();
var type = 0;
var c1 = ' ';
var c2 = ' ';
for (var i = bs.nextSetBit (0); i >= 0; i = bs.nextSetBit (i + 1)) {
var vertexIndexes = polygonIndexes[i];
while (pt < nBuf && !Character.isDigit (c1 = fData.charAt (pt++))) {
}
type = c1.charCodeAt (0) - 48;
while (pt < nBuf && Character.isWhitespace (c1 = fData.charAt (pt++))) {
}
while (pt < nBuf && Character.isWhitespace (c2 = fData.charAt (pt++))) {
}
var f1 = J.jvxl.data.JvxlCoder.jvxlFractionFromCharacter (c1.charCodeAt (0), 35, 90, 0);
var f2 = J.jvxl.data.JvxlCoder.jvxlFractionFromCharacter (c2.charCodeAt (0), 35, 90, 0);
var i1;
var i2;
var i3;
var i4;
if ((type & 1) == 0) {
i1 = vertexIndexes[1];
i2 = i3 = vertexIndexes[2];
i4 = vertexIndexes[0];
} else {
i1 = vertexIndexes[0];
i2 = vertexIndexes[1];
if ((type & 2) != 0) {
i3 = i2;
i4 = vertexIndexes[2];
} else {
i3 = vertexIndexes[2];
i4 = i1;
}}v.addLast (J.jvxl.data.JvxlCoder.getContourPoint (vertices, i1, i2, f1));
v.addLast (J.jvxl.data.JvxlCoder.getContourPoint (vertices, i3, i4, f2));
}
}, "J.util.JmolList,~A,~A");
c$.getContourPoint = $_M(c$, "getContourPoint", 
($fz = function (vertices, i, j, f) {
var pt =  new J.util.P3 ();
pt.setT (vertices[j]);
pt.sub (vertices[i]);
pt.scale (f);
pt.add (vertices[i]);
return pt;
}, $fz.isPrivate = true, $fz), "~A,~N,~N,~N");
c$.appendContourTriangleIntersection = $_M(c$, "appendContourTriangleIntersection", 
function (type, f1, f2, fData) {
fData.appendI (type);
fData.appendC (J.jvxl.data.JvxlCoder.jvxlFractionAsCharacter (f1));
fData.appendC (J.jvxl.data.JvxlCoder.jvxlFractionAsCharacter (f2));
}, "~N,~N,~N,J.util.SB");
c$.jvxlCreateColorData = $_M(c$, "jvxlCreateColorData", 
function (jvxlData, vertexValues) {
if (vertexValues == null) {
jvxlData.jvxlColorData = "";
return;
}var writePrecisionColor = jvxlData.isJvxlPrecisionColor;
var doTruncate = jvxlData.isTruncated;
var colorFractionBase = jvxlData.colorFractionBase;
var colorFractionRange = jvxlData.colorFractionRange;
var valueBlue = jvxlData.valueMappedToBlue;
var valueRed = jvxlData.valueMappedToRed;
var vertexCount = (jvxlData.saveVertexCount > 0 ? jvxlData.saveVertexCount : jvxlData.vertexCount);
if (vertexCount > vertexValues.length) System.out.println ("JVXLCODER ERROR");
var min = jvxlData.mappedDataMin;
var max = jvxlData.mappedDataMax;
var list1 =  new J.util.SB ();
var list2 =  new J.util.SB ();
if (vertexValues.length < vertexCount) System.out.println ("JVXLCOLOR OHOHO");
for (var i = 0; i < vertexCount; i++) {
var value = vertexValues[i];
if (Float.isNaN (value)) value = min;
if (doTruncate) value = (value > 0 ? 0.999 : -0.999);
if (writePrecisionColor) J.jvxl.data.JvxlCoder.jvxlAppendCharacter2 (value, min, max, colorFractionBase, colorFractionRange, list1, list2);
 else list1.appendC (J.jvxl.data.JvxlCoder.jvxlValueAsCharacter (value, valueRed, valueBlue, colorFractionBase, colorFractionRange));
}
jvxlData.jvxlColorData = list1.appendSB (list2).appendC ('\n').toString ();
}, "J.jvxl.data.JvxlData,~A");
c$.appendXmlVertexOnlyData = $_M(c$, "appendXmlVertexOnlyData", 
($fz = function (sb, jvxlData, meshData, escapeXml) {
var vertexIdNew =  Clazz.newIntArray (meshData.vertexCount, 0);
if (J.jvxl.data.JvxlCoder.appendXmlTriangleData (sb, meshData.polygonIndexes, meshData.polygonCount, meshData.bsSlabDisplay, vertexIdNew, escapeXml)) J.jvxl.data.JvxlCoder.appendXmlVertexData (sb, jvxlData, vertexIdNew, meshData.vertices, meshData.vertexValues, meshData.vertexCount, meshData.polygonColorData, meshData.polygonCount, meshData.bsSlabDisplay, jvxlData.vertexColors, jvxlData.jvxlColorData.length > 0, escapeXml);
}, $fz.isPrivate = true, $fz), "J.util.SB,J.jvxl.data.JvxlData,J.jvxl.data.MeshData,~B");
c$.appendXmlTriangleData = $_M(c$, "appendXmlTriangleData", 
($fz = function (sb, triangles, nData, bsSlabDisplay, vertexIdNew, escapeXml) {
var list1 =  new J.util.SB ();
var list2 =  new J.util.SB ();
var ilast = 1;
var p = 0;
var inew = 0;
var addPlus = false;
var nTri = 0;
var removeSlabbed = (bsSlabDisplay != null);
for (var i = 0; i < nData; ) {
if (triangles[i] == null || (removeSlabbed && !bsSlabDisplay.get (i))) {
i++;
continue;
}var idata = triangles[i][p];
if (vertexIdNew[idata] > 0) {
idata = vertexIdNew[idata];
} else {
idata = vertexIdNew[idata] = ++inew;
}var diff = idata - ilast;
ilast = idata;
if (diff == 0) {
list1.appendC ('!');
addPlus = false;
} else if (diff > 32) {
if (addPlus) list1.appendC ('+');
list1.appendI (diff);
addPlus = true;
} else if (diff < -32) {
list1.appendI (diff);
addPlus = true;
} else {
list1.appendC (String.fromCharCode (92 + diff));
addPlus = false;
}if (++p % 3 == 0) {
list2.appendI (triangles[i][3]);
p = 0;
i++;
nTri++;
}}
if (list1.length () == 0) return true;
J.io.XmlUtil.appendTagObj (sb, "jvxlTriangleData", ["count", "" + nTri, "encoding", "jvxltdiff", "data", J.jvxl.data.JvxlCoder.jvxlCompressString (list1.toString (), escapeXml)], null);
J.io.XmlUtil.appendTagObj (sb, "jvxlTriangleEdgeData", ["count", "" + nTri, "encoding", "jvxlsc", "data", J.jvxl.data.JvxlCoder.jvxlCompressString (list2.toString (), escapeXml)], null);
return true;
}, $fz.isPrivate = true, $fz), "J.util.SB,~A,~N,J.util.BS,~A,~B");
c$.appendXmlVertexData = $_M(c$, "appendXmlVertexData", 
($fz = function (sb, jvxlData, vertexIdNew, vertices, vertexValues, vertexCount, polygonColorData, polygonCount, bsSlabDisplay, vertexColors, addColorData, escapeXml) {
var colorFractionBase = jvxlData.colorFractionBase;
var colorFractionRange = jvxlData.colorFractionRange;
var p;
var min = jvxlData.boundingBox[0];
var max = jvxlData.boundingBox[1];
var list1 =  new J.util.SB ();
var list2 =  new J.util.SB ();
var vertexIdOld = null;
var removeSlabbed = (bsSlabDisplay != null);
if (polygonCount > 0) {
if (removeSlabbed) polygonCount = bsSlabDisplay.cardinality ();
removeSlabbed = false;
vertexIdOld =  Clazz.newIntArray (vertexCount, 0);
for (var i = 0; i < vertexCount; i++) if (vertexIdNew[i] > 0) vertexIdOld[vertexIdNew[i] - 1] = i;

}var n = 0;
for (var i = 0; i < vertexCount; i++) if (!removeSlabbed || bsSlabDisplay.get (i)) {
n++;
p = vertices[(polygonCount == 0 ? i : vertexIdOld[i])];
J.jvxl.data.JvxlCoder.jvxlAppendCharacter2 (p.x, min.x, max.x, colorFractionBase, colorFractionRange, list1, list2);
J.jvxl.data.JvxlCoder.jvxlAppendCharacter2 (p.y, min.y, max.y, colorFractionBase, colorFractionRange, list1, list2);
J.jvxl.data.JvxlCoder.jvxlAppendCharacter2 (p.z, min.z, max.z, colorFractionBase, colorFractionRange, list1, list2);
}
list1.appendSB (list2);
J.io.XmlUtil.appendTagObj (sb, "jvxlVertexData", ["count", "" + n, "min", J.util.Escape.eP (min), "max", J.util.Escape.eP (max), "encoding", "base90xyz2", "data", J.jvxl.data.JvxlCoder.jvxlCompressString (list1.toString (), escapeXml)], null);
if (polygonColorData != null) J.io.XmlUtil.appendTagObj (sb, "jvxlPolygonColorData", ["encoding", "jvxlnc", "count", "" + polygonCount], "\n" + polygonColorData);
if (!addColorData) return;
list1 =  new J.util.SB ();
list2 =  new J.util.SB ();
if (vertexColors == null) {
for (var i = 0; i < vertexCount; i++) if (!removeSlabbed || bsSlabDisplay.get (i)) {
var value = vertexValues[polygonCount == 0 ? i : vertexIdOld[i]];
J.jvxl.data.JvxlCoder.jvxlAppendCharacter2 (value, jvxlData.mappedDataMin, jvxlData.mappedDataMax, colorFractionBase, colorFractionRange, list1, list2);
}
} else {
var lastColor = 0;
list1.appendI (n).append (" ");
for (var i = 0; i < vertexCount; i++) if (!removeSlabbed || bsSlabDisplay.get (i)) {
var c = vertexColors[polygonCount == 0 ? i : vertexIdOld[i]];
if (c == lastColor) c = 0;
 else lastColor = c;
list1.appendI (c);
list1.append (" ");
}
}J.jvxl.data.JvxlCoder.appendXmlColorData (sb, list1.appendSB (list2).append ("\n").toString (), (vertexColors == null), true, jvxlData.valueMappedToRed, jvxlData.valueMappedToBlue);
}, $fz.isPrivate = true, $fz), "J.util.SB,J.jvxl.data.JvxlData,~A,~A,~A,~N,~S,~N,J.util.BS,~A,~B,~B");
c$.jvxlFractionAsCharacter = $_M(c$, "jvxlFractionAsCharacter", 
function (fraction) {
return J.jvxl.data.JvxlCoder.jvxlFractionAsCharacterRange (fraction, 35, 90);
}, "~N");
c$.jvxlFractionAsCharacterRange = $_M(c$, "jvxlFractionAsCharacterRange", 
function (fraction, base, range) {
if (fraction > 0.9999) fraction = 0.9999;
 else if (Float.isNaN (fraction)) fraction = 1.0001;
var ich = Clazz.doubleToInt (Math.floor (fraction * range + base));
if (ich < base) return String.fromCharCode (base);
if (ich == 92) return '!';
return String.fromCharCode (ich);
}, "~N,~N,~N");
c$.jvxlAppendCharacter2 = $_M(c$, "jvxlAppendCharacter2", 
($fz = function (value, min, max, base, range, list1, list2) {
var fraction = (min == max ? value : (value - min) / (max - min));
var ch1 = J.jvxl.data.JvxlCoder.jvxlFractionAsCharacterRange (fraction, base, range);
list1.appendC (ch1);
fraction -= J.jvxl.data.JvxlCoder.jvxlFractionFromCharacter (ch1.charCodeAt (0), base, range, 0);
list2.appendC (J.jvxl.data.JvxlCoder.jvxlFractionAsCharacterRange (fraction * range, base, range));
}, $fz.isPrivate = true, $fz), "~N,~N,~N,~N,~N,J.util.SB,J.util.SB");
c$.jvxlFractionFromCharacter = $_M(c$, "jvxlFractionFromCharacter", 
function (ich, base, range, fracOffset) {
if (ich == base + range) return NaN;
if (ich < base) ich = 92;
var fraction = (ich - base + fracOffset) / range;
if (fraction < 0) return 0;
if (fraction > 1) return 0.999999;
return fraction;
}, "~N,~N,~N,~N");
c$.jvxlFractionFromCharacter2 = $_M(c$, "jvxlFractionFromCharacter2", 
function (ich1, ich2, base, range) {
var fraction = J.jvxl.data.JvxlCoder.jvxlFractionFromCharacter (ich1, base, range, 0);
var remains = J.jvxl.data.JvxlCoder.jvxlFractionFromCharacter (ich2, base, range, 0.5);
return fraction + remains / range;
}, "~N,~N,~N,~N");
c$.jvxlValueAsCharacter = $_M(c$, "jvxlValueAsCharacter", 
function (value, min, max, base, range) {
var fraction = (min == max ? value : (value - min) / (max - min));
return J.jvxl.data.JvxlCoder.jvxlFractionAsCharacterRange (fraction, base, range);
}, "~N,~N,~N,~N,~N");
c$.jvxlValueFromCharacter2 = $_M(c$, "jvxlValueFromCharacter2", 
function (ich, ich2, min, max, base, range) {
var fraction = J.jvxl.data.JvxlCoder.jvxlFractionFromCharacter2 (ich, ich2, base, range);
return (max == min ? fraction : min + fraction * (max - min));
}, "~N,~N,~N,~N,~N,~N");
c$.jvxlEncodeBitSet0 = $_M(c$, "jvxlEncodeBitSet0", 
function (bs, nPoints, sb) {
var dataCount = 0;
var prevCount = -1;
var nPrev = 0;
if (nPoints < 0) nPoints = bs.length ();
var n = 0;
var isset = false;
var lastPoint = nPoints - 1;
for (var i = 0; i < nPoints; ++i) {
if (isset == bs.get (i)) {
dataCount++;
} else {
if (dataCount == prevCount && i != lastPoint) {
nPrev++;
} else {
if (nPrev > 0) {
sb.appendC (' ').appendI (-nPrev);
nPrev = 0;
n++;
}sb.appendC (' ').appendI (dataCount);
n++;
prevCount = dataCount;
}dataCount = 1;
isset = !isset;
}}
sb.appendC (' ').appendI (dataCount).appendC ('\n');
return n;
}, "J.util.BS,~N,J.util.SB");
c$.jvxlEncodeBitSet = $_M(c$, "jvxlEncodeBitSet", 
function (bs) {
var sb =  new J.util.SB ();
J.jvxl.data.JvxlCoder.jvxlEncodeBitSetBuffer (bs, -1, sb);
return sb.toString ();
}, "J.util.BS");
c$.jvxlEncodeBitSetBuffer = $_M(c$, "jvxlEncodeBitSetBuffer", 
function (bs, nPoints, sb) {
var dataCount = 0;
var n = 0;
var isset = false;
if (nPoints < 0) nPoints = bs.length ();
if (nPoints == 0) return 0;
sb.append ("-");
for (var i = 0; i < nPoints; ++i) {
if (isset == bs.get (i)) {
dataCount++;
} else {
J.jvxl.data.JvxlCoder.jvxlAppendEncodedNumber (sb, dataCount, 35, 90);
n++;
dataCount = 1;
isset = !isset;
}}
J.jvxl.data.JvxlCoder.jvxlAppendEncodedNumber (sb, dataCount, 35, 90);
sb.appendC ('\n');
return n;
}, "J.util.BS,~N,J.util.SB");
c$.jvxlAppendEncodedNumber = $_M(c$, "jvxlAppendEncodedNumber", 
function (sb, n, base, range) {
var isInRange = (n < range);
if (n == 0) sb.appendC (String.fromCharCode (base));
 else if (!isInRange) sb.appendC (String.fromCharCode (base + range));
while (n > 0) {
var n1 = Clazz.doubleToInt (n / range);
var x = base + n - n1 * range;
if (x == 92) x = 33;
sb.appendC (String.fromCharCode (x));
n = n1;
}
if (!isInRange) sb.append (" ");
}, "J.util.SB,~N,~N,~N");
c$.jvxlDecodeBitSetRange = $_M(c$, "jvxlDecodeBitSetRange", 
function (data, base, range) {
var bs =  new J.util.BS ();
var dataCount = 0;
var ptr = 0;
var isset = false;
var next =  Clazz.newIntArray (1, 0);
while ((dataCount = J.jvxl.data.JvxlCoder.jvxlParseEncodedInt (data, base, range, next)) != -2147483648) {
if (isset) bs.setBits (ptr, ptr + dataCount);
ptr += dataCount;
isset = !isset;
}
return bs;
}, "~S,~N,~N");
c$.jvxlParseEncodedInt = $_M(c$, "jvxlParseEncodedInt", 
function (str, offset, base, next) {
var digitSeen = false;
var value = 0;
var ich = next[0];
var ichMax = str.length;
if (ich < 0) return -2147483648;
while (ich < ichMax && Character.isWhitespace (str.charAt (ich))) ++ich;

if (ich >= ichMax) return -2147483648;
var factor = 1;
var isLong = (str.charCodeAt (ich) == (offset + base));
if (isLong) ich++;
while (ich < ichMax && !Character.isWhitespace (str.charAt (ich))) {
var i = str.charCodeAt (ich);
if (i < offset) i = 92;
value += (i - offset) * factor;
digitSeen = true;
++ich;
if (!isLong) break;
factor *= base;
}
if (!digitSeen) value = -2147483648;
next[0] = ich;
return value;
}, "~S,~N,~N,~A");
c$.jvxlDecodeBitSet = $_M(c$, "jvxlDecodeBitSet", 
function (data) {
if (data.startsWith ("-")) return J.jvxl.data.JvxlCoder.jvxlDecodeBitSetRange (J.jvxl.data.JvxlCoder.jvxlDecompressString (data.substring (1)), 35, 90);
var bs =  new J.util.BS ();
var dataCount = 0;
var lastCount = 0;
var nPrev = 0;
var ptr = 0;
var isset = false;
var next =  Clazz.newIntArray (1, 0);
while (true) {
dataCount = (nPrev++ < 0 ? dataCount : J.util.Parser.parseIntNext (data, next));
if (dataCount == -2147483648) break;
if (dataCount < 0) {
nPrev = dataCount;
dataCount = lastCount;
continue;
}if (isset) bs.setBits (ptr, ptr + dataCount);
ptr += dataCount;
lastCount = dataCount;
isset = !isset;
}
return bs;
}, "~S");
c$.jvxlCompressString = $_M(c$, "jvxlCompressString", 
function (data, escapeXml) {
if (data.indexOf ("~") >= 0) return data;
var dataOut =  new J.util.SB ();
var chLast = '\u0000';
var escaped = false;
var lastEscaped = false;
var nLast = 0;
var n = data.length;
for (var i = 0; i <= n; i++) {
var ch = (i == n ? '\0' : data.charAt (i));
switch (ch) {
case '\n':
case '\r':
continue;
case '&':
case '<':
escaped = escapeXml;
break;
default:
escaped = false;
}
if (ch == chLast) {
++nLast;
ch = '\0';
} else if (nLast > 0 || lastEscaped) {
if (nLast < 4 && !lastEscaped || chLast == ' ' || chLast == '\t') {
while (--nLast >= 0) dataOut.appendC (chLast);

} else {
if (lastEscaped) lastEscaped = false;
 else dataOut.appendC ('~');
dataOut.appendI (nLast);
dataOut.appendC (' ');
}nLast = 0;
}if (ch != '\0') {
if (escaped) {
lastEscaped = true;
escaped = false;
dataOut.appendC ('~');
chLast = ch;
(ch = String.fromCharCode (ch.charCodeAt (0) - 1));
} else {
chLast = ch;
}dataOut.appendC (ch);
}}
return dataOut.toString ();
}, "~S,~B");
c$.jvxlDecompressString = $_M(c$, "jvxlDecompressString", 
function (data) {
if (data.indexOf ("~") < 0) return data;
var dataOut =  new J.util.SB ();
var chLast = '\u0000';
var next =  Clazz.newIntArray (1, 0);
for (var i = 0; i < data.length; i++) {
var ch = data.charAt (i);
if (ch == '~') {
next[0] = ++i;
switch (ch = data.charAt (i)) {
case ';':
case '%':
next[0]++;
dataOut.appendC (chLast = (ch = String.fromCharCode (ch.charCodeAt (0) + 1)));
case '1':
case '2':
case '3':
case '4':
case '5':
case '6':
case '7':
case '8':
case '9':
var nChar = J.util.Parser.parseIntNext (data, next);
for (var c = 0; c < nChar; c++) dataOut.appendC (chLast);

i = next[0];
continue;
case '~':
--i;
break;
default:
J.util.Logger.error ("Error uncompressing string " + data.substring (0, i) + "?");
}
}dataOut.appendC (ch);
chLast = ch;
}
return dataOut.toString ();
}, "~S");
c$.jvxlCreateHeaderWithoutTitleOrAtoms = $_M(c$, "jvxlCreateHeaderWithoutTitleOrAtoms", 
function (v, bs) {
J.jvxl.data.JvxlCoder.jvxlCreateHeader (v, bs);
}, "J.jvxl.data.VolumeData,J.util.SB");
c$.jvxlCreateHeader = $_M(c$, "jvxlCreateHeader", 
function (v, sb) {
v.setVolumetricXml ();
if (sb.length () == 0) sb.append ("Line 1\nLine 2\n");
}, "J.jvxl.data.VolumeData,J.util.SB");
Clazz.defineStatics (c$,
"JVXL_VERSION1", "2.0",
"JVXL_VERSION_XML", "2.3",
"CONTOUR_NPOLYGONS", 0,
"CONTOUR_BITSET", 1,
"CONTOUR_VALUE", 2,
"CONTOUR_COLIX", 3,
"CONTOUR_COLOR", 4,
"CONTOUR_FDATA", 5,
"CONTOUR_POINTS", 6,
"defaultEdgeFractionBase", 35,
"defaultEdgeFractionRange", 90,
"defaultColorFractionBase", 35,
"defaultColorFractionRange", 90);
});
