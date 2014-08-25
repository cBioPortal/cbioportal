Clazz.declarePackage ("J.shapecgo");
Clazz.load (["J.shapespecial.DrawMesh", "JU.Lst"], "J.shapecgo.CGOMesh", ["java.lang.Float", "$.Short", "JU.BS", "$.CU", "JU.C", "$.Logger", "$.Normix"], function () {
c$ = Clazz.decorateAsClass (function () {
this.cmds = null;
this.nList = null;
this.cList = null;
Clazz.instantialize (this, arguments);
}, J.shapecgo, "CGOMesh", J.shapespecial.DrawMesh);
Clazz.prepareFields (c$, function () {
this.nList =  new JU.Lst ();
this.cList =  new JU.Lst ();
});
c$.getSize = Clazz.defineMethod (c$, "getSize", 
function (i) {
return (i >= 0 && i < J.shapecgo.CGOMesh.sizes.length ? J.shapecgo.CGOMesh.sizes[i] : -1);
}, "~N");
Clazz.defineMethod (c$, "clear", 
function (meshType) {
Clazz.superCall (this, J.shapecgo.CGOMesh, "clear", [meshType]);
this.useColix = false;
}, "~S");
Clazz.defineMethod (c$, "set", 
function (list) {
this.width = 200;
this.diameter = 0;
this.useColix = true;
this.bsTemp =  new JU.BS ();
try {
if (Clazz.instanceOf (list.get (0), Float)) {
this.cmds = list;
} else {
this.cmds = list.get (1);
if (this.cmds == null) this.cmds = list.get (0);
this.cmds = this.cmds.get (1);
}var n = this.cmds.size ();
for (var i = 0; i < n; i++) {
var type = (this.cmds.get (i)).intValue ();
var len = J.shapecgo.CGOMesh.getSize (type);
if (len < 0) {
JU.Logger.error ("CGO unknown type: " + type);
return false;
}switch (type) {
case 1:
len = 8;
break;
case 0:
return true;
case 5:
this.addNormix (i);
break;
case 6:
this.addColix (i);
this.useColix = false;
break;
case 14:
this.addColix (i + 7);
this.addColix (i + 10);
break;
case 8:
this.addNormix (i + 9);
this.addNormix (i + 12);
this.addNormix (i + 15);
this.addColix (i + 18);
this.addColix (i + 21);
this.addColix (i + 24);
break;
}
i += len;
}
return true;
} catch (e) {
if (Clazz.exceptionOf (e, Exception)) {
JU.Logger.error ("CGOMesh error: " + e);
this.cmds = null;
return false;
} else {
throw e;
}
}
}, "JU.Lst");
Clazz.defineMethod (c$, "addColix", 
 function (i) {
this.getPoint (i, this.vTemp);
this.cList.addLast (Short.$valueOf (JU.C.getColix (JU.CU.colorPtToFFRGB (this.vTemp))));
}, "~N");
Clazz.defineMethod (c$, "addNormix", 
 function (i) {
this.getPoint (i, this.vTemp);
this.nList.addLast (Short.$valueOf (JU.Normix.get2SidedNormix (this.vTemp, this.bsTemp)));
}, "~N");
Clazz.defineMethod (c$, "getPoint", 
function (i, pt) {
pt.set (this.getFloat (++i), this.getFloat (++i), this.getFloat (++i));
}, "~N,JU.T3");
Clazz.defineMethod (c$, "getInt", 
function (i) {
return (this.cmds.get (i)).intValue ();
}, "~N");
Clazz.defineMethod (c$, "getFloat", 
function (i) {
return (this.cmds.get (i)).floatValue ();
}, "~N");
Clazz.defineStatics (c$,
"GL_POINTS", 0,
"GL_LINES", 1,
"GL_LINE_LOOP", 2,
"GL_LINE_STRIP", 3,
"GL_TRIANGLES", 4,
"GL_TRIANGLE_STRIP", 5,
"GL_TRIANGLE_FAN", 6,
"sizes", [0, 0, 1, 0, 3, 3, 3, 4, 27, 13, 1, 1, 1, 1, 13, 15, 1, 35, 13, 3, 2, 3, 9, 1, 2, 1, 14, 16, 1, 2],
"STOP", 0,
"SIMPLE_LINE", 1,
"BEGIN", 2,
"END", 3,
"VERTEX", 4,
"NORMAL", 5,
"COLOR", 6,
"SPHERE", 7,
"TRICOLOR_TRIANGLE", 8,
"CYLINDER", 9,
"LINEWIDTH", 10,
"WIDTHSCALE", 11,
"ENABLE", 12,
"DISABLE", 13,
"SAUSAGE", 14,
"CUSTOM_CYLINDER", 15,
"DOTWIDTH", 16,
"ALPHA_TRIANGLE", 17,
"ELLIPSOID", 18,
"FONT", 19,
"FONT_SCALE", 20,
"FONT_VERTEX", 21,
"FONT_AXES", 22,
"CHAR", 23,
"INDENT", 24,
"ALPHA", 25,
"QUADRIC", 26,
"CONE", 27,
"RESET_NORMAL", 28,
"PICK_COLOR", 29);
});
