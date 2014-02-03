Clazz.declarePackage ("J.viewer");
Clazz.load (["J.constant.EnumStereoMode", "J.util.AxisAngle4f", "$.Matrix3f", "$.Matrix4f", "$.P3", "$.P3i", "$.V3"], "J.viewer.TransformManager", ["java.lang.Boolean", "$.Float", "java.util.Hashtable", "J.api.Interface", "J.util.Escape", "$.Logger", "$.P4", "$.Quaternion", "$.SB"], function () {
c$ = Clazz.decorateAsClass (function () {
this.viewer = null;
this.perspectiveModel = 11;
this.cameraScaleFactor = 0;
this.referencePlaneOffset = 0;
this.aperatureAngle = 0;
this.cameraDistanceFromCenter = 0;
this.modelCenterOffset = 0;
this.modelRadius = 0;
this.modelRadiusPixels = 0;
this.navigationCenter = null;
this.navigationOffset = null;
this.navigationShiftXY = null;
this.navigationDepth = 0;
this.matrixTemp = null;
this.vectorTemp = null;
this.haveNotifiedNaN = false;
this.spinX = 0;
this.spinY = 30;
this.spinZ = 0;
this.spinFps = 30;
this.navX = 0;
this.navY = 0;
this.navZ = 0;
this.navFps = NaN;
this.isSpinInternal = false;
this.isSpinFixed = false;
this.isSpinSelected = false;
this.fixedRotationOffset = null;
this.fixedRotationCenter = null;
this.perspectiveOffset = null;
this.perspectiveShiftXY = null;
this.rotationCenterDefault = null;
this.rotationRadiusDefault = 0;
this.fixedRotationAxis = null;
this.internalRotationAxis = null;
this.internalTranslation = null;
this.internalRotationCenter = null;
this.internalRotationAngle = 0;
this.matrixRotate = null;
this.matrixTemp3 = null;
this.matrixTemp4 = null;
this.axisangleT = null;
this.vectorT = null;
this.vectorT2 = null;
this.pointT2 = null;
this.rotationAxis = null;
this.rotationRate = 0;
this.arcBall0 = null;
this.arcBall1 = null;
this.arcBallAxis = null;
this.arcBall0Rotation = null;
this.fixedTranslation = null;
this.camera = null;
this.cameraSetting = null;
this.xTranslationFraction = 0.5;
this.yTranslationFraction = 0.5;
this.prevZoomSetting = 0;
this.previousX = 0;
this.previousY = 0;
this.zoomEnabled = true;
this.zoomPercent = 100;
this.zoomPercentSetting = 100;
this.zoomRatio = 0;
this.slabEnabled = false;
this.internalSlab = false;
this.zShadeEnabled = false;
this.slabPercentSetting = 0;
this.depthPercentSetting = 0;
this.zSlabPercentSetting = 50;
this.zDepthPercentSetting = 0;
this.zSlabPoint = null;
this.slabValue = 0;
this.depthValue = 0;
this.zSlabValue = 0;
this.zDepthValue = 0;
this.slabRange = 0;
this.slabPlane = null;
this.depthPlane = null;
this.perspectiveDepth = true;
this.scale3D = false;
this.cameraDepth = 3;
this.cameraDepthSetting = 3;
this.visualRange = 0;
this.cameraDistance = 1000;
this.width = 0;
this.height = 0;
this.screenPixelCount = 0;
this.scalePixelsPerAngstrom = 0;
this.scaleDefaultPixelsPerAngstrom = 0;
this.scale3DAngstromsPerInch = 0;
this.antialias = false;
this.useZoomLarge = false;
this.zoomHeight = false;
this.screenWidth = 0;
this.screenHeight = 0;
this.matrixTransform = null;
this.matrixTransformInv = null;
this.point3fScreenTemp = null;
this.point3iScreenTemp = null;
this.ptVibTemp = null;
this.navigating = false;
this.mode = 0;
this.defaultMode = 0;
this.pointTsp = null;
this.untransformedPoint = null;
this.ptTest1 = null;
this.ptTest2 = null;
this.ptTest3 = null;
this.aaTest1 = null;
this.matrixTest = null;
this.motion = null;
this.spinOn = false;
this.navOn = false;
this.spinThread = null;
this.spinIsGesture = false;
this.vibrationOn = false;
this.vibrationPeriod = 0;
this.vibrationPeriodMs = 0;
this.vibrationScale = 0;
this.vibrationT = 0;
this.vibrationThread = null;
this.stereoMode = null;
this.stereoColors = null;
this.stereoDegrees = NaN;
this.stereoRadians = 0;
this.stereoFrame = false;
this.matrixStereo = null;
this.windowCentered = false;
this.frameOffsets = null;
this.frameOffset = null;
this.bsSelectedAtoms = null;
this.ptOffset = null;
this.navMode = 1;
this.zoomFactor = 3.4028235E38;
this.navigationSlabOffset = 0;
this.nav = null;
Clazz.instantialize (this, arguments);
}, J.viewer, "TransformManager");
Clazz.prepareFields (c$, function () {
this.navigationCenter =  new J.util.P3 ();
this.navigationOffset =  new J.util.P3 ();
this.navigationShiftXY =  new J.util.P3 ();
this.matrixTemp =  new J.util.Matrix4f ();
this.vectorTemp =  new J.util.V3 ();
this.fixedRotationOffset =  new J.util.P3 ();
this.fixedRotationCenter =  new J.util.P3 ();
this.perspectiveOffset =  new J.util.P3 ();
this.perspectiveShiftXY =  new J.util.P3 ();
this.rotationCenterDefault =  new J.util.P3 ();
this.fixedRotationAxis =  new J.util.AxisAngle4f ();
this.internalRotationAxis =  new J.util.AxisAngle4f ();
this.internalRotationCenter = J.util.P3.new3 (0, 0, 0);
this.matrixRotate =  new J.util.Matrix3f ();
this.matrixTemp3 =  new J.util.Matrix3f ();
this.matrixTemp4 =  new J.util.Matrix4f ();
this.axisangleT =  new J.util.AxisAngle4f ();
this.vectorT =  new J.util.V3 ();
this.vectorT2 =  new J.util.V3 ();
this.pointT2 =  new J.util.P3 ();
this.rotationAxis =  new J.util.V3 ();
this.arcBall0 =  new J.util.V3 ();
this.arcBall1 =  new J.util.V3 ();
this.arcBallAxis =  new J.util.V3 ();
this.arcBall0Rotation =  new J.util.Matrix3f ();
this.fixedTranslation =  new J.util.P3 ();
this.camera =  new J.util.P3 ();
this.cameraSetting =  new J.util.P3 ();
this.matrixTransform =  new J.util.Matrix4f ();
this.matrixTransformInv =  new J.util.Matrix4f ();
this.point3fScreenTemp =  new J.util.P3 ();
this.point3iScreenTemp =  new J.util.P3i ();
this.ptVibTemp =  new J.util.P3 ();
this.pointTsp =  new J.util.P3 ();
this.untransformedPoint =  new J.util.P3 ();
this.ptTest1 =  new J.util.P3 ();
this.ptTest2 =  new J.util.P3 ();
this.ptTest3 =  new J.util.P3 ();
this.aaTest1 =  new J.util.AxisAngle4f ();
this.matrixTest =  new J.util.Matrix3f ();
this.stereoMode = J.constant.EnumStereoMode.NONE;
this.matrixStereo =  new J.util.Matrix3f ();
this.frameOffset =  new J.util.P3 ();
this.ptOffset =  new J.util.P3 ();
});
Clazz.makeConstructor (c$, 
function (viewer, width, height) {
this.setViewer (viewer, width, height);
}, "J.viewer.Viewer,~N,~N");
$_M(c$, "setViewer", 
($fz = function (viewer, width, height) {
this.viewer = viewer;
this.setScreenParameters (width, height, true, false, true, true);
}, $fz.isPrivate = true, $fz), "J.viewer.Viewer,~N,~N");
$_M(c$, "homePosition", 
function (resetSpin) {
if (resetSpin) this.setSpinOff ();
this.setNavOn (false);
this.navFps = 10;
this.navX = this.navY = this.navZ = 0;
this.rotationCenterDefault.setT (this.viewer.getBoundBoxCenter ());
this.setFixedRotationCenter (this.rotationCenterDefault);
this.rotationRadiusDefault = this.setRotationRadius (0, true);
this.windowCentered = true;
this.setRotationCenterAndRadiusXYZ (null, true);
this.matrixRotate.setIdentity ();
var m = this.viewer.getModelSetAuxiliaryInfoValue ("defaultOrientationMatrix");
if (m != null) this.matrixRotate.setM (m);
this.setZoomEnabled (true);
this.zoomToPercent (this.viewer.global.modelKitMode ? 50 : 100);
this.zoomPercent = this.zoomPercentSetting;
this.slabReset ();
this.resetFitToScreen (true);
if (this.viewer.isJmolDataFrame ()) {
this.fixedRotationCenter.set (0, 0, 0);
} else {
if (this.viewer.global.axesOrientationRasmol) this.rotateX (3.141592653589793);
}this.viewer.saveOrientation ("default", null);
if (this.mode == 1) this.setNavigationMode (true);
}, "~B");
$_M(c$, "clear", 
function () {
this.clearVibration ();
this.clearSpin ();
this.stopMotion ();
this.fixedRotationCenter.set (0, 0, 0);
this.navigating = false;
this.slabPlane = null;
this.depthPlane = null;
this.zSlabPoint = null;
this.resetNavigationPoint (true);
});
$_M(c$, "setFixedRotationCenter", 
($fz = function (center) {
if (center == null) return;
this.fixedRotationCenter.setT (center);
}, $fz.isPrivate = true, $fz), "J.util.P3");
$_M(c$, "setRotationPointXY", 
function (center) {
var newCenterScreen = this.transformPoint (center);
this.fixedTranslation.set (newCenterScreen.x, newCenterScreen.y, 0);
}, "J.util.P3");
$_M(c$, "spinXYBy", 
function (xDelta, yDelta, speed) {
if (xDelta == 0 && yDelta == 0) {
if (this.spinThread != null && this.spinIsGesture) this.clearSpin ();
return;
}this.clearSpin ();
var pt1 = J.util.P3.newP (this.fixedRotationCenter);
var ptScreen =  new J.util.P3 ();
this.transformPoint2 (pt1, ptScreen);
var pt2 = J.util.P3.new3 (-yDelta, xDelta, 0);
pt2.add (ptScreen);
this.unTransformPoint (pt2, pt2);
this.viewer.setInMotion (false);
this.rotateAboutPointsInternal (null, pt2, pt1, 10 * speed, NaN, false, true, null, true, null, null, null);
}, "~N,~N,~N");
$_M(c$, "rotateArcBall", 
function (x, y, factor) {
var radius2 = (this.screenPixelCount >> 2) * this.screenPixelCount;
x -= this.fixedTranslation.x;
y -= this.fixedTranslation.y;
var z = radius2 - x * x - y * y;
z = (z < 0 ? -1 : 1) * Math.sqrt (Math.abs (z));
if (factor == 0) {
this.arcBall0Rotation.setM (this.matrixRotate);
this.arcBall0.set (x, -y, z);
if (!Float.isNaN (z)) this.arcBall0.normalize ();
return;
}if (Float.isNaN (this.arcBall0.z) || Float.isNaN (z)) return;
this.arcBall1.set (x, -y, z);
this.arcBall1.normalize ();
this.arcBallAxis.cross (this.arcBall0, this.arcBall1);
this.axisangleT.setVA (this.arcBallAxis, factor * Math.acos (this.arcBall0.dot (this.arcBall1)));
this.matrixRotate.setM (this.arcBall0Rotation);
this.rotateAxisAngle2 (this.axisangleT, null);
}, "~N,~N,~N");
$_M(c$, "rotateXYBy", 
function (xDelta, yDelta, bsAtoms) {
this.rotateXRadians (yDelta * 0.017453292, bsAtoms);
this.rotateYRadians (xDelta * 0.017453292, bsAtoms);
}, "~N,~N,J.util.BS");
$_M(c$, "rotateZBy", 
function (zDelta, x, y) {
if (x != 2147483647 && y != 2147483647) this.resetXYCenter (x, y);
this.rotateZRadians ((zDelta / 57.29577951308232));
}, "~N,~N,~N");
$_M(c$, "rotateFront", 
function () {
this.matrixRotate.setIdentity ();
});
$_M(c$, "rotateX", 
function (angleRadians) {
this.matrixRotate.rotX (angleRadians);
}, "~N");
$_M(c$, "rotateY", 
function (angleRadians) {
this.matrixRotate.rotY (angleRadians);
}, "~N");
$_M(c$, "rotateZ", 
function (angleRadians) {
this.matrixRotate.rotZ (angleRadians);
}, "~N");
$_M(c$, "applyRotation", 
($fz = function (mNew, isInternal, bsAtoms, translation) {
if (bsAtoms == null) {
this.matrixRotate.mul2 (mNew, this.matrixRotate);
return;
}this.viewer.moveAtoms (mNew, this.matrixRotate, translation, this.internalRotationCenter, isInternal, bsAtoms);
if (translation != null) {
this.internalRotationCenter.add (translation);
}}, $fz.isPrivate = true, $fz), "J.util.Matrix3f,~B,J.util.BS,J.util.V3");
$_M(c$, "rotateXRadians", 
function (angleRadians, bsAtoms) {
this.matrixTemp3.rotX (angleRadians);
this.applyRotation (this.matrixTemp3, false, bsAtoms, null);
}, "~N,J.util.BS");
$_M(c$, "rotateYRadians", 
function (angleRadians, bsAtoms) {
this.matrixTemp3.rotY (angleRadians);
this.applyRotation (this.matrixTemp3, false, bsAtoms, null);
}, "~N,J.util.BS");
$_M(c$, "rotateZRadians", 
function (angleRadians) {
this.matrixTemp3.rotZ (angleRadians);
this.applyRotation (this.matrixTemp3, false, null, null);
}, "~N");
$_M(c$, "rotateAxisAngle", 
function (rotAxis, radians) {
this.axisangleT.setVA (rotAxis, radians);
this.rotateAxisAngle2 (this.axisangleT, null);
}, "J.util.V3,~N");
$_M(c$, "rotateAxisAngle2", 
($fz = function (axisAngle, bsAtoms) {
this.matrixTemp3.setAA (axisAngle);
this.applyRotation (this.matrixTemp3, false, bsAtoms, null);
}, $fz.isPrivate = true, $fz), "J.util.AxisAngle4f,J.util.BS");
$_M(c$, "rotateAxisAngleAtCenter", 
function (eval, rotCenter, rotAxis, degreesPerSecond, endDegrees, isSpin, bsAtoms) {
if (rotCenter != null) this.moveRotationCenter (rotCenter, true);
this.setSpinOff ();
this.setNavOn (false);
if (this.viewer.isHeadless ()) {
if (isSpin && endDegrees == 3.4028235E38) return false;
isSpin = false;
}if (Float.isNaN (degreesPerSecond) || degreesPerSecond == 0 || endDegrees == 0) return false;
if (rotCenter != null) {
this.setRotationPointXY (rotCenter);
}this.setFixedRotationCenter (rotCenter);
this.rotationAxis.setT (rotAxis);
this.rotationRate = degreesPerSecond;
if (isSpin) {
this.fixedRotationAxis.setVA (rotAxis, degreesPerSecond * 0.017453292);
this.isSpinInternal = false;
this.isSpinFixed = true;
this.isSpinSelected = (bsAtoms != null);
this.setSpin (eval, true, endDegrees, null, null, bsAtoms, false);
return false;
}var radians = endDegrees * 0.017453292;
this.fixedRotationAxis.setVA (rotAxis, endDegrees);
this.rotateAxisAngleRadiansFixed (radians, bsAtoms);
return true;
}, "J.api.JmolScriptEvaluator,J.util.P3,J.util.V3,~N,~N,~B,J.util.BS");
$_M(c$, "rotateAxisAngleRadiansFixed", 
function (angleRadians, bsAtoms) {
this.axisangleT.setAA (this.fixedRotationAxis);
this.axisangleT.angle = angleRadians;
this.rotateAxisAngle2 (this.axisangleT, bsAtoms);
}, "~N,J.util.BS");
$_M(c$, "rotateAboutPointsInternal", 
function (eval, point1, point2, degreesPerSecond, endDegrees, isClockwise, isSpin, bsAtoms, isGesture, translation, finalPoints, dihedralList) {
this.setSpinOff ();
this.setNavOn (false);
if (this.viewer.isHeadless ()) {
if (isSpin && endDegrees == 3.4028235E38) return false;
isSpin = false;
}if (dihedralList == null && (translation == null || translation.length () < 0.001) && (!isSpin || endDegrees == 0 || Float.isNaN (degreesPerSecond) || degreesPerSecond == 0) && (isSpin || endDegrees == 0)) return false;
var axis = J.util.V3.newVsub (point2, point1);
if (isClockwise) axis.scale (-1.0);
this.internalRotationCenter.setT (point1);
this.rotationAxis.setT (axis);
if (translation == null) {
this.internalTranslation = null;
} else {
this.internalTranslation = J.util.V3.newV (translation);
}var isSelected = (bsAtoms != null);
if (isSpin) {
if (dihedralList == null) {
var nFrames = Clazz.doubleToInt (Math.abs (endDegrees) / Math.abs (degreesPerSecond) * this.spinFps + 0.5);
if (Float.isNaN (endDegrees)) {
this.rotationRate = degreesPerSecond;
} else {
this.rotationRate = degreesPerSecond = endDegrees / nFrames * this.spinFps;
if (translation != null) this.internalTranslation.scale (1 / (nFrames));
}this.internalRotationAxis.setVA (axis, this.rotationRate * 0.017453292);
this.isSpinInternal = true;
this.isSpinFixed = false;
this.isSpinSelected = isSelected;
} else {
endDegrees = degreesPerSecond;
}this.setSpin (eval, true, endDegrees, finalPoints, dihedralList, bsAtoms, isGesture);
return (dihedralList != null || bsAtoms != null);
}var radians = endDegrees * 0.017453292;
this.internalRotationAxis.setVA (axis, radians);
this.rotateAxisAngleRadiansInternal (radians, bsAtoms);
return false;
}, "J.api.JmolScriptEvaluator,J.util.P3,J.util.P3,~N,~N,~B,~B,J.util.BS,~B,J.util.V3,J.util.JmolList,~A");
$_M(c$, "rotateAxisAngleRadiansInternal", 
function (radians, bsAtoms) {
this.internalRotationAngle = radians;
this.vectorT.set (this.internalRotationAxis.x, this.internalRotationAxis.y, this.internalRotationAxis.z);
this.matrixRotate.transform2 (this.vectorT, this.vectorT2);
this.axisangleT.setVA (this.vectorT2, radians);
this.matrixTemp3.setAA (this.axisangleT);
this.applyRotation (this.matrixTemp3, true, bsAtoms, this.internalTranslation);
if (bsAtoms == null) this.getNewFixedRotationCenter ();
}, "~N,J.util.BS");
$_M(c$, "getNewFixedRotationCenter", 
function () {
this.axisangleT.setAA (this.internalRotationAxis);
this.axisangleT.angle = -this.internalRotationAngle;
this.matrixTemp4.setAA (this.axisangleT);
this.vectorT.setT (this.internalRotationCenter);
this.pointT2.setT (this.fixedRotationCenter);
this.pointT2.sub (this.vectorT);
var pt =  new J.util.P3 ();
this.matrixTemp4.transform2 (this.pointT2, pt);
pt.add (this.vectorT);
this.setRotationCenterAndRadiusXYZ (pt, false);
});
$_M(c$, "setTranslationFractions", 
function () {
this.xTranslationFraction = this.fixedTranslation.x / this.width;
this.yTranslationFraction = this.fixedTranslation.y / this.height;
});
$_M(c$, "centerAt", 
function (x, y, pt) {
if (pt == null) {
this.translateXYBy (x, y);
return;
}if (this.windowCentered) this.viewer.setBooleanProperty ("windowCentered", false);
this.fixedTranslation.x = x;
this.fixedTranslation.y = y;
this.setFixedRotationCenter (pt);
}, "~N,~N,J.util.P3");
$_M(c$, "percentToPixels", 
function (xyz, percent) {
switch (xyz) {
case 'x':
return Clazz.doubleToInt (Math.floor (percent / 100 * this.width));
case 'y':
return Clazz.doubleToInt (Math.floor (percent / 100 * this.height));
case 'z':
return Clazz.doubleToInt (Math.floor (percent / 100 * this.screenPixelCount));
}
return 0;
}, "~S,~N");
$_M(c$, "angstromsToPixels", 
function (distance) {
return Clazz.doubleToInt (Math.floor (this.scalePixelsPerAngstrom * distance));
}, "~N");
$_M(c$, "translateXYBy", 
function (xDelta, yDelta) {
this.fixedTranslation.x += xDelta;
this.fixedTranslation.y += yDelta;
this.setTranslationFractions ();
}, "~N,~N");
$_M(c$, "setCamera", 
function (x, y) {
this.cameraSetting.set (x, y, (x == 0 && y == 0 ? 0 : 1));
}, "~N,~N");
$_M(c$, "translateToPercent", 
function (type, percent) {
switch (type) {
case 'x':
this.xTranslationFraction = 0.5 + percent / 100;
this.fixedTranslation.x = this.width * this.xTranslationFraction;
return;
case 'y':
this.yTranslationFraction = 0.5 + percent / 100;
this.fixedTranslation.y = this.height * this.yTranslationFraction;
return;
case 'z':
if (this.mode == 1) this.setNavigationDepthPercent (percent);
return;
}
}, "~S,~N");
$_M(c$, "getTranslationXPercent", 
function () {
return (this.width == 0 ? 0 : (this.fixedTranslation.x - this.width / 2) * 100 / this.width);
});
$_M(c$, "getTranslationYPercent", 
function () {
return (this.height == 0 ? 0 : (this.fixedTranslation.y - this.height / 2) * 100 / this.height);
});
$_M(c$, "getTranslationZPercent", 
function () {
return 0;
});
$_M(c$, "getTranslationScript", 
function () {
var info = "";
var f = this.getTranslationXPercent ();
if (f != 0.0) info += "translate x " + f + ";";
f = this.getTranslationYPercent ();
if (f != 0.0) info += "translate y " + f + ";";
return info;
});
$_M(c$, "getOrientationText", 
function (type) {
switch (type) {
case 4130:
return this.getMoveToText (1, false);
case 1073742132:
return this.getRotationQuaternion ().toString ();
case 1073742178:
var sb =  new J.util.SB ();
J.viewer.TransformManager.truncate2 (sb, this.getTranslationXPercent ());
J.viewer.TransformManager.truncate2 (sb, this.getTranslationYPercent ());
return sb.toString ();
default:
return this.getMoveToText (1, true) + "\n#OR\n" + this.getRotateZyzText (true);
}
}, "~N");
$_M(c$, "getOrientationInfo", 
function () {
var info =  new java.util.Hashtable ();
info.put ("moveTo", this.getMoveToText (1, false));
info.put ("center", "center " + this.getCenterText ());
info.put ("centerPt", this.fixedRotationCenter);
var aa =  new J.util.AxisAngle4f ();
this.getAxisAngle (aa);
info.put ("axisAngle", aa);
info.put ("quaternion", J.util.Quaternion.newAA (aa).toPoint4f ());
info.put ("rotationMatrix", this.matrixRotate);
info.put ("rotateZYZ", this.getRotateZyzText (false));
info.put ("rotateXYZ", this.getRotateXyzText ());
info.put ("transXPercent", Float.$valueOf (this.getTranslationXPercent ()));
info.put ("transYPercent", Float.$valueOf (this.getTranslationYPercent ()));
info.put ("zoom", Float.$valueOf (this.zoomPercent));
info.put ("modelRadius", Float.$valueOf (this.modelRadius));
if (this.mode == 1) {
info.put ("navigationCenter", "navigate center " + J.util.Escape.eP (this.navigationCenter));
info.put ("navigationOffsetXPercent", Float.$valueOf (this.getNavigationOffsetPercent ('X')));
info.put ("navigationOffsetYPercent", Float.$valueOf (this.getNavigationOffsetPercent ('Y')));
info.put ("navigationDepthPercent", Float.$valueOf (this.getNavigationDepthPercent ()));
}return info;
});
$_M(c$, "getAxisAngle", 
function (axisAngle) {
axisAngle.setM (this.matrixRotate);
}, "J.util.AxisAngle4f");
$_M(c$, "getTransformText", 
function () {
return this.matrixRotate.toString ();
});
$_M(c$, "getMatrixRotate", 
function () {
return this.matrixRotate;
});
$_M(c$, "setRotation", 
function (matrixRotation) {
if (!Float.isNaN (matrixRotation.m00)) this.matrixRotate.setM (matrixRotation);
}, "J.util.Matrix3f");
$_M(c$, "getRotation", 
function (matrixRotation) {
matrixRotation.setM (this.matrixRotate);
}, "J.util.Matrix3f");
$_M(c$, "zoomBy", 
function (pixels) {
if (pixels > 20) pixels = 20;
 else if (pixels < -20) pixels = -20;
var deltaPercent = pixels * this.zoomPercentSetting / 50;
if (deltaPercent == 0) deltaPercent = (pixels > 0 ? 1 : (deltaPercent < 0 ? -1 : 0));
this.zoomRatio = (deltaPercent + this.zoomPercentSetting) / this.zoomPercentSetting;
this.zoomPercentSetting += deltaPercent;
}, "~N");
$_M(c$, "getZoomPercentFloat", 
function () {
return this.zoomPercent;
});
$_M(c$, "zoomToPercent", 
function (percentZoom) {
this.zoomPercentSetting = percentZoom;
this.zoomRatio = 0;
}, "~N");
$_M(c$, "translateZBy", 
function (pixels) {
if (pixels >= this.screenPixelCount) return;
var sppa = this.scalePixelsPerAngstrom / (1 - pixels * 1.0 / this.screenPixelCount);
if (sppa >= this.screenPixelCount) return;
var newZoomPercent = sppa / this.scaleDefaultPixelsPerAngstrom * 100;
this.zoomRatio = newZoomPercent / this.zoomPercentSetting;
this.zoomPercentSetting = newZoomPercent;
}, "~N");
$_M(c$, "zoomByFactor0", 
function (factor, x, y) {
if (factor <= 0 || !this.zoomEnabled) return;
this.zoomRatio = factor;
this.zoomPercentSetting *= factor;
this.resetXYCenter (x, y);
}, "~N,~N,~N");
$_M(c$, "resetXYCenter", 
($fz = function (x, y) {
if (x == 2147483647 || y == 2147483647) return;
if (this.windowCentered) this.viewer.setBooleanProperty ("windowCentered", false);
var pt =  new J.util.P3 ();
this.transformPoint2 (this.fixedRotationCenter, pt);
pt.set (x, y, pt.z);
this.unTransformPoint (pt, pt);
this.fixedTranslation.set (x, y, 0);
this.setFixedRotationCenter (pt);
}, $fz.isPrivate = true, $fz), "~N,~N");
$_M(c$, "zoomByPercent", 
function (percentZoom) {
var deltaPercent = percentZoom * this.zoomPercentSetting / 100;
if (deltaPercent == 0) deltaPercent = (percentZoom < 0) ? -1 : 1;
this.zoomRatio = (deltaPercent + this.zoomPercentSetting) / this.zoomPercentSetting;
this.zoomPercentSetting += deltaPercent;
}, "~N");
$_M(c$, "setScaleAngstromsPerInch", 
function (angstromsPerInch) {
this.scale3D = (angstromsPerInch > 0);
if (this.scale3D) this.scale3DAngstromsPerInch = angstromsPerInch;
this.perspectiveDepth = !this.scale3D;
}, "~N");
$_M(c$, "setZslabPoint", 
function (pt) {
this.zSlabPoint = (pt == null ? null : J.util.P3.newP (pt));
}, "J.util.P3");
$_M(c$, "getZShadeStart", 
function () {
return (this.zShadeEnabled ? this.zDepthValue : 0);
});
$_M(c$, "setSlabRange", 
function (value) {
this.slabRange = value;
}, "~N");
$_M(c$, "setSlabEnabled", 
function (slabEnabled) {
this.slabEnabled = slabEnabled;
this.viewer.getGlobalSettings ().setB ("slabEnabled", slabEnabled);
}, "~B");
$_M(c$, "setZShadeEnabled", 
function (zShadeEnabled) {
this.zShadeEnabled = zShadeEnabled;
this.viewer.getGlobalSettings ().setB ("zShade", zShadeEnabled);
}, "~B");
$_M(c$, "setZoomEnabled", 
function (zoomEnabled) {
this.zoomEnabled = zoomEnabled;
this.viewer.getGlobalSettings ().setB ("zoomEnabled", zoomEnabled);
}, "~B");
$_M(c$, "slabReset", 
function () {
this.slabToPercent (100);
this.depthToPercent (0);
this.depthPlane = null;
this.slabPlane = null;
this.setSlabEnabled (false);
this.setZShadeEnabled (false);
});
$_M(c$, "getSlabPercentSetting", 
function () {
return this.slabPercentSetting;
});
$_M(c$, "slabByPercentagePoints", 
function (percentage) {
this.slabPlane = null;
this.slabPercentSetting += percentage;
this.slabDepthChanged ();
if (this.depthPercentSetting >= this.slabPercentSetting) this.depthPercentSetting = this.slabPercentSetting - 1;
}, "~N");
$_M(c$, "slabDepthChanged", 
($fz = function () {
this.viewer.getGlobalSettings ().setI ("slab", this.slabPercentSetting);
this.viewer.getGlobalSettings ().setI ("depth", this.depthPercentSetting);
}, $fz.isPrivate = true, $fz));
$_M(c$, "depthByPercentagePoints", 
function (percentage) {
this.depthPlane = null;
this.depthPercentSetting += percentage;
if (this.slabPercentSetting <= this.depthPercentSetting) this.slabPercentSetting = this.depthPercentSetting + 1;
this.slabDepthChanged ();
}, "~N");
$_M(c$, "slabDepthByPercentagePoints", 
function (percentage) {
this.slabPlane = null;
this.depthPlane = null;
this.slabPercentSetting += percentage;
this.depthPercentSetting += percentage;
this.slabDepthChanged ();
}, "~N");
$_M(c$, "slabToPercent", 
function (percentSlab) {
this.viewer.setFloatProperty ("slabRange", 0);
this.slabPercentSetting = percentSlab;
this.slabPlane = null;
if (this.depthPercentSetting >= this.slabPercentSetting) this.depthPercentSetting = this.slabPercentSetting - 1;
this.slabDepthChanged ();
}, "~N");
$_M(c$, "depthToPercent", 
function (percentDepth) {
this.viewer.getGlobalSettings ().setI ("depth", percentDepth);
this.depthPercentSetting = percentDepth;
if (this.slabPercentSetting <= this.depthPercentSetting) this.slabPercentSetting = this.depthPercentSetting + 1;
this.slabDepthChanged ();
}, "~N");
$_M(c$, "zSlabToPercent", 
function (percentSlab) {
this.zSlabPercentSetting = percentSlab;
if (this.zDepthPercentSetting > this.zSlabPercentSetting) this.zDepthPercentSetting = percentSlab;
}, "~N");
$_M(c$, "zDepthToPercent", 
function (percentDepth) {
this.zDepthPercentSetting = percentDepth;
if (this.zDepthPercentSetting > this.zSlabPercentSetting) this.zSlabPercentSetting = percentDepth;
}, "~N");
$_M(c$, "slabInternal", 
function (plane, isDepth) {
if (isDepth) {
this.depthPlane = plane;
this.depthPercentSetting = 0;
} else {
this.slabPlane = plane;
this.slabPercentSetting = 100;
}}, "J.util.P4,~B");
$_M(c$, "setSlabDepthInternal", 
function (isDepth) {
this.finalizeTransformParameters ();
if (isDepth) this.depthPlane = null;
 else this.slabPlane = null;
this.slabInternal (this.getSlabDepthPlane (isDepth), isDepth);
}, "~B");
$_M(c$, "getSlabDepthPlane", 
function (isDepth) {
if (isDepth) {
if (this.depthPlane != null) return this.depthPlane;
} else {
if (this.slabPlane != null) return this.slabPlane;
}var m = this.matrixTransform;
return J.util.P4.new4 (-m.m20, -m.m21, -m.m22, -m.m23 + (isDepth ? this.depthValue : this.slabValue));
}, "~B");
$_M(c$, "getCameraFactors", 
function () {
this.aperatureAngle = (Math.atan2 (this.screenPixelCount / 2, this.referencePlaneOffset) * 2 * 180 / 3.141592653589793);
this.cameraDistanceFromCenter = this.referencePlaneOffset / this.scalePixelsPerAngstrom;
var ptRef = J.util.P3.new3 (Clazz.doubleToInt (this.screenWidth / 2), Clazz.doubleToInt (this.screenHeight / 2), this.referencePlaneOffset);
this.unTransformPoint (ptRef, ptRef);
var ptCamera = J.util.P3.new3 (Clazz.doubleToInt (this.screenWidth / 2), Clazz.doubleToInt (this.screenHeight / 2), 0);
this.viewer.unTransformPoint (ptCamera, ptCamera);
ptCamera.sub (this.fixedRotationCenter);
var pt = J.util.P3.new3 (Clazz.doubleToInt (this.screenWidth / 2), Clazz.doubleToInt (this.screenHeight / 2), this.cameraDistanceFromCenter * this.scalePixelsPerAngstrom);
this.viewer.unTransformPoint (pt, pt);
pt.sub (this.fixedRotationCenter);
ptCamera.add (pt);
return [ptRef, ptCamera, this.fixedRotationCenter, J.util.P3.new3 (this.cameraDistanceFromCenter, this.aperatureAngle, this.scalePixelsPerAngstrom)];
});
$_M(c$, "getFrontPlane", 
function () {
return Clazz.floatToInt (this.cameraDistance);
});
$_M(c$, "setPerspectiveDepth", 
function (perspectiveDepth) {
if (this.perspectiveDepth == perspectiveDepth) return;
this.perspectiveDepth = perspectiveDepth;
this.viewer.global.setB ("perspectiveDepth", perspectiveDepth);
this.resetFitToScreen (false);
}, "~B");
$_M(c$, "getPerspectiveDepth", 
function () {
return this.perspectiveDepth;
});
$_M(c$, "setCameraDepthPercent", 
function (percent, resetSlab) {
this.resetNavigationPoint (resetSlab);
var screenMultiples = (percent < 0 ? -percent / 100 : percent);
if (screenMultiples == 0) return;
this.cameraDepthSetting = screenMultiples;
this.viewer.global.setF ("cameraDepth", this.cameraDepthSetting);
if (this.mode == 1) this.cameraDepth = NaN;
}, "~N,~B");
$_M(c$, "getCameraDepth", 
function () {
return this.cameraDepthSetting;
});
$_M(c$, "setVisualRange", 
function (angstroms) {
this.visualRange = angstroms;
}, "~N");
$_M(c$, "getUnscaledTransformMatrix", 
function () {
var unscaled =  new J.util.Matrix4f ();
unscaled.setIdentity ();
this.vectorTemp.setT (this.fixedRotationCenter);
this.matrixTemp.setZero ();
this.matrixTemp.setTranslation (this.vectorTemp);
unscaled.sub (this.matrixTemp);
this.matrixTemp.setM3 (this.matrixRotate);
unscaled.mul2 (this.matrixTemp, unscaled);
return unscaled;
});
$_M(c$, "setScreenParameters0", 
($fz = function (screenWidth, screenHeight, useZoomLarge, antialias, resetSlab, resetZoom) {
if (screenWidth == 2147483647) return;
this.screenWidth = screenWidth;
this.screenHeight = screenHeight;
this.useZoomLarge = useZoomLarge;
this.antialias = antialias;
this.width = (antialias ? screenWidth * 2 : screenWidth);
this.height = (antialias ? screenHeight * 2 : screenHeight);
this.scaleFitToScreen (false, useZoomLarge, resetSlab, resetZoom);
}, $fz.isPrivate = true, $fz), "~N,~N,~B,~B,~B,~B");
$_M(c$, "setAntialias", 
function (TF) {
var isNew = (this.antialias != TF);
this.antialias = TF;
this.width = (this.antialias ? this.screenWidth * 2 : this.screenWidth);
this.height = (this.antialias ? this.screenHeight * 2 : this.screenHeight);
if (isNew) this.scaleFitToScreen (false, this.useZoomLarge, false, false);
}, "~B");
$_M(c$, "defaultScaleToScreen", 
function (radius) {
return this.screenPixelCount / 2 / radius;
}, "~N");
$_M(c$, "resetFitToScreen", 
($fz = function (andCenter) {
this.scaleFitToScreen (andCenter, this.viewer.global.zoomLarge, true, true);
}, $fz.isPrivate = true, $fz), "~B");
$_M(c$, "scaleFitToScreen", 
function (andCenter, zoomLarge, resetSlab, resetZoom) {
if (this.width == 0 || this.height == 0) {
this.screenPixelCount = 1;
} else {
this.fixedTranslation.set (this.width * (andCenter ? 0.5 : this.xTranslationFraction), this.height * (andCenter ? 0.5 : this.yTranslationFraction), 0);
this.setTranslationFractions ();
if (andCenter) this.camera.set (0, 0, 0);
if (resetZoom) this.resetNavigationPoint (resetSlab);
if (this.zoomHeight) zoomLarge = (this.height > this.width);
this.screenPixelCount = (zoomLarge == (this.height > this.width) ? this.height : this.width);
}if (this.screenPixelCount > 2) this.screenPixelCount -= 2;
this.scaleDefaultPixelsPerAngstrom = this.defaultScaleToScreen (this.modelRadius);
}, "~B,~B,~B,~B");
$_M(c$, "scaleToScreen", 
function (z, milliAngstroms) {
if (milliAngstroms == 0 || z < 2) return 0;
var pixelSize = this.scaleToPerspective (z, milliAngstroms * this.scalePixelsPerAngstrom / 1000);
return (pixelSize > 0 ? pixelSize : 1);
}, "~N,~N");
$_M(c$, "unscaleToScreen", 
function (z, screenDistance) {
var d = screenDistance / this.scalePixelsPerAngstrom;
return (this.perspectiveDepth ? d / this.getPerspectiveFactor (z) : d);
}, "~N,~N");
$_M(c$, "scaleToPerspective", 
function (z, sizeAngstroms) {
return (this.perspectiveDepth ? sizeAngstroms * this.getPerspectiveFactor (z) : sizeAngstroms);
}, "~N,~N");
$_M(c$, "getMatrixtransform", 
function () {
return this.matrixTransform;
});
$_M(c$, "setNavigationMode", 
function (TF) {
if (TF && this.canNavigate ()) this.mode = 1;
 else this.mode = this.defaultMode;
this.resetNavigationPoint (true);
}, "~B");
$_M(c$, "isNavigating", 
function () {
return this.navigating || this.navOn;
});
$_M(c$, "finalizeTransformParameters", 
function () {
this.haveNotifiedNaN = false;
this.fixedRotationOffset.setT (this.fixedTranslation);
this.camera.setT (this.cameraSetting);
this.internalSlab = this.slabEnabled && (this.slabPlane != null || this.depthPlane != null);
var newZoom = this.getZoomSetting ();
if (this.zoomPercent != newZoom) {
this.zoomPercent = newZoom;
if (!this.viewer.global.fontCaching) this.viewer.getGraphicsData ().clearFontCache ();
}this.calcCameraFactors ();
this.calcTransformMatrix ();
if (this.mode == 1) this.calcNavigationPoint ();
 else this.calcSlabAndDepthValues ();
});
$_M(c$, "getZoomSetting", 
function () {
if (this.zoomPercentSetting < 5) this.zoomPercentSetting = 5;
if (this.zoomPercentSetting > 200000) this.zoomPercentSetting = 200000;
return (this.zoomEnabled || this.mode == 1 ? this.zoomPercentSetting : 100);
});
$_M(c$, "calcSlabAndDepthValues", 
function () {
if (this.slabRange < 1) this.slabValue = this.zValueFromPercent (this.slabPercentSetting);
 else this.slabValue = Clazz.doubleToInt (Math.floor (this.modelCenterOffset * this.slabRange / (2 * this.modelRadius) * (this.zoomPercentSetting / 100)));
this.depthValue = this.zValueFromPercent (this.depthPercentSetting);
if (this.zSlabPercentSetting == this.zDepthPercentSetting) {
this.zSlabValue = this.slabValue;
this.zDepthValue = this.depthValue;
} else {
this.zSlabValue = this.zValueFromPercent (this.zSlabPercentSetting);
this.zDepthValue = this.zValueFromPercent (this.zDepthPercentSetting);
}if (this.zSlabPoint != null) {
try {
this.transformPoint2 (this.zSlabPoint, this.pointT2);
this.zSlabValue = Clazz.floatToInt (this.pointT2.z);
} catch (e) {
if (Clazz.exceptionOf (e, Exception)) {
} else {
throw e;
}
}
}this.viewer.getGlobalSettings ().setS ("_slabPlane", J.util.Escape.eP4 (this.getSlabDepthPlane (false)));
this.viewer.getGlobalSettings ().setS ("_depthPlane", J.util.Escape.eP4 (this.getSlabDepthPlane (true)));
if (this.slabEnabled) return;
this.slabValue = 0;
this.depthValue = 2147483647;
});
$_M(c$, "zValueFromPercent", 
function (zPercent) {
return Clazz.doubleToInt (Math.floor ((1 - zPercent / 50) * this.modelRadiusPixels + this.modelCenterOffset));
}, "~N");
$_M(c$, "calcTransformMatrix", 
function () {
this.matrixTransform.setIdentity ();
this.vectorTemp.setT (this.fixedRotationCenter);
this.vectorTemp.sub (this.frameOffset);
this.matrixTemp.setZero ();
this.matrixTemp.setTranslation (this.vectorTemp);
this.matrixTransform.sub (this.matrixTemp);
this.matrixTemp.setM3 (this.stereoFrame ? this.matrixStereo : this.matrixRotate);
this.matrixTransform.mul2 (this.matrixTemp, this.matrixTransform);
this.matrixTemp.setIdentity ();
this.matrixTemp.m00 = this.matrixTemp.m11 = this.matrixTemp.m22 = this.scalePixelsPerAngstrom;
this.matrixTemp.m11 = this.matrixTemp.m22 = -this.scalePixelsPerAngstrom;
this.matrixTransform.mul2 (this.matrixTemp, this.matrixTransform);
this.matrixTransform.m23 += this.modelCenterOffset;
try {
this.matrixTransformInv.invertM (this.matrixTransform);
} catch (e) {
if (Clazz.exceptionOf (e, Exception)) {
System.out.println ("ERROR INVERTING matrixTransform!");
} else {
throw e;
}
}
});
$_M(c$, "rotatePoint", 
function (pt, ptRot) {
this.matrixRotate.transform2 (pt, ptRot);
ptRot.y = -ptRot.y;
}, "J.util.P3,J.util.P3");
$_M(c$, "transformPoints", 
function (count, angstroms, screens) {
for (var i = count; --i >= 0; ) screens[i].setT (this.transformPoint (angstroms[i]));

}, "~N,~A,~A");
$_M(c$, "transformPointScr", 
function (pointAngstroms, pointScreen) {
pointScreen.setT (this.transformPoint (pointAngstroms));
}, "J.util.P3,J.util.P3i");
$_M(c$, "transformPointNoClip2", 
function (pointAngstroms, pointScreen) {
pointScreen.setT (this.transformPointNoClip (pointAngstroms));
}, "J.util.P3,J.util.P3");
$_M(c$, "transformPoint", 
function (pointAngstroms) {
if (pointAngstroms.z == 3.4028235E38 || pointAngstroms.z == -3.4028235E38) return this.transformScreenPoint (pointAngstroms);
this.getTemporaryScreenPoint (pointAngstroms, (this.internalSlab ? pointAngstroms : null));
return this.point3iScreenTemp;
}, "J.util.P3");
$_M(c$, "transformScreenPoint", 
($fz = function (ptXyp) {
if (ptXyp.z == -3.4028235E38) {
this.point3iScreenTemp.x = Clazz.doubleToInt (Math.floor (ptXyp.x / 100 * this.screenWidth));
this.point3iScreenTemp.y = Clazz.doubleToInt (Math.floor ((1 - ptXyp.y / 100) * this.screenHeight));
} else {
this.point3iScreenTemp.x = Clazz.floatToInt (ptXyp.x);
this.point3iScreenTemp.y = (this.screenHeight - Clazz.floatToInt (ptXyp.y));
}if (this.antialias) {
this.point3iScreenTemp.x <<= 1;
this.point3iScreenTemp.y <<= 1;
}this.matrixTransform.transform2 (this.fixedRotationCenter, this.pointTsp);
this.point3iScreenTemp.z = Clazz.floatToInt (this.pointTsp.z);
return this.point3iScreenTemp;
}, $fz.isPrivate = true, $fz), "J.util.P3");
$_M(c$, "transformPointNoClip", 
function (pointAngstroms) {
this.getTemporaryScreenPoint (pointAngstroms, null);
return this.point3fScreenTemp;
}, "J.util.P3");
$_M(c$, "transformPointVib", 
function (pointAngstroms, v) {
this.ptVibTemp.setT (pointAngstroms);
if (this.vibrationOn && v != null) v.setTempPoint (this.ptVibTemp, this.vibrationT, this.vibrationScale);
this.getTemporaryScreenPoint (this.ptVibTemp, pointAngstroms);
return this.point3iScreenTemp;
}, "J.util.P3,J.util.Vibration");
$_M(c$, "transformPoint2", 
function (pointAngstroms, screen) {
this.getTemporaryScreenPoint (pointAngstroms, pointAngstroms);
screen.setT (this.point3fScreenTemp);
}, "J.util.P3,J.util.P3");
$_M(c$, "transformVector", 
function (vectorAngstroms, vectorTransformed) {
this.matrixTransform.transformV2 (vectorAngstroms, vectorTransformed);
}, "J.util.V3,J.util.V3");
$_M(c$, "move", 
function (eval, dRot, dZoom, dTrans, dSlab, floatSecondsTotal, fps) {
var motion = J.api.Interface.getOptionInterface ("thread.MoveThread");
motion.setManager (this, this.viewer, [dRot, dTrans, [dZoom, dSlab, floatSecondsTotal, fps]]);
if (floatSecondsTotal > 0) motion.setEval (eval);
motion.run ();
}, "J.api.JmolScriptEvaluator,J.util.V3,~N,J.util.V3,~N,~N,~N");
$_M(c$, "isInPosition", 
function (axis, degrees) {
if (Float.isNaN (degrees)) return true;
this.aaTest1.setVA (axis, (degrees / 57.29577951308232));
this.ptTest1.set (4.321, 1.23456, 3.14159);
this.getRotation (this.matrixTest);
this.matrixTest.transform2 (this.ptTest1, this.ptTest2);
this.matrixTest.setAA (this.aaTest1);
this.matrixTest.transform2 (this.ptTest1, this.ptTest3);
return (this.ptTest3.distance (this.ptTest2) < 0.1);
}, "J.util.V3,~N");
$_M(c$, "moveToPyMOL", 
function (eval, floatSecondsTotal, pymolView) {
var m3 = J.util.Matrix3f.newA (pymolView);
m3.invert ();
var cameraX = pymolView[9];
var cameraY = -pymolView[10];
var pymolDistanceToCenter = -pymolView[11];
var center = J.util.P3.new3 (pymolView[12], pymolView[13], pymolView[14]);
var pymolDistanceToSlab = pymolView[15];
var pymolDistanceToDepth = pymolView[16];
var fov = pymolView[17];
var isOrtho = (fov >= 0);
this.setPerspectiveDepth (!isOrtho);
var theta = Math.abs (fov) / 2;
var tan = Math.tan (theta * 3.141592653589793 / 180);
var rotationRadius = pymolDistanceToCenter * tan;
var jmolCameraToCenter = 0.5 / tan;
var cameraDepth = jmolCameraToCenter - 0.5;
var f = 50 / rotationRadius;
if (pymolDistanceToSlab > 0) {
var slab = 50 + Clazz.floatToInt ((pymolDistanceToCenter - pymolDistanceToSlab) * f);
var depth = 50 + Clazz.floatToInt ((pymolDistanceToCenter - pymolDistanceToDepth) * f);
this.setSlabEnabled (true);
this.slabToPercent (slab);
this.depthToPercent (depth);
if (pymolView.length == 21) {
var depthCue = (pymolView[18] != 0);
var fog = (pymolView[19] != 0);
var fogStart = pymolView[20];
this.setZShadeEnabled (depthCue);
if (depthCue) {
if (fog) {
this.viewer.setIntProperty ("zSlab", Clazz.floatToInt (Math.min (100, slab + fogStart * (depth - slab))));
} else {
this.viewer.setIntProperty ("zSlab", Clazz.floatToInt ((slab + depth) / 2));
}this.viewer.setIntProperty ("zDepth", depth);
}}}this.moveTo (eval, floatSecondsTotal, center, null, 0, m3, 100, NaN, NaN, rotationRadius, null, NaN, NaN, NaN, cameraDepth, cameraX, cameraY);
}, "J.api.JmolScriptEvaluator,~N,~A");
$_M(c$, "moveTo", 
function (eval, floatSecondsTotal, center, rotAxis, degrees, matrixEnd, zoom, xTrans, yTrans, newRotationRadius, navCenter, xNav, yNav, navDepth, cameraDepth, cameraX, cameraY) {
if (matrixEnd == null) {
matrixEnd =  new J.util.Matrix3f ();
var axis = J.util.V3.newV (rotAxis);
if (Float.isNaN (degrees)) {
matrixEnd.m00 = NaN;
} else if (degrees < 0.01 && degrees > -0.01) {
matrixEnd.setIdentity ();
} else {
if (axis.x == 0 && axis.y == 0 && axis.z == 0) {
return;
}var aaMoveTo =  new J.util.AxisAngle4f ();
aaMoveTo.setVA (axis, (degrees / 57.29577951308232));
matrixEnd.setAA (aaMoveTo);
}}if (cameraX == this.cameraSetting.x) cameraX = NaN;
if (cameraY == this.cameraSetting.y) cameraY = NaN;
if (cameraDepth == this.cameraDepth) cameraDepth = NaN;
if (!Float.isNaN (cameraX)) xTrans = cameraX * 50 / newRotationRadius / this.width * this.screenPixelCount;
if (!Float.isNaN (cameraY)) yTrans = cameraY * 50 / newRotationRadius / this.height * this.screenPixelCount;
var pixelScale = (center == null ? this.scaleDefaultPixelsPerAngstrom : this.defaultScaleToScreen (newRotationRadius));
if (floatSecondsTotal <= 0) {
this.setAll (center, matrixEnd, navCenter, zoom, xTrans, yTrans, newRotationRadius, pixelScale, navDepth, xNav, yNav, cameraDepth, cameraX, cameraY);
this.viewer.moveUpdate (floatSecondsTotal);
this.viewer.finalizeTransformParameters ();
return;
}try {
if (this.motion == null) this.motion = J.api.Interface.getOptionInterface ("thread.MoveToThread");
var nSteps = this.motion.setManager (this, this.viewer, [center, matrixEnd, navCenter, [floatSecondsTotal, zoom, xTrans, yTrans, newRotationRadius, pixelScale, navDepth, xNav, yNav, cameraDepth, cameraX, cameraY]]);
if (nSteps <= 0 || this.viewer.global.waitForMoveTo) {
if (nSteps > 0) this.motion.setEval (eval);
this.motion.run ();
if (!this.viewer.isSingleThreaded) this.motion = null;
} else {
this.motion.start ();
}} catch (e) {
if (Clazz.exceptionOf (e, Exception)) {
} else {
throw e;
}
}
}, "J.api.JmolScriptEvaluator,~N,J.util.P3,J.util.Tuple3f,~N,J.util.Matrix3f,~N,~N,~N,~N,J.util.P3,~N,~N,~N,~N,~N,~N");
$_M(c$, "stopMotion", 
function () {
this.motion = null;
});
$_M(c$, "getRotationQuaternion", 
function () {
return J.util.Quaternion.newM (this.matrixRotate);
});
$_M(c$, "getRotationText", 
function () {
this.axisangleT.setM (this.matrixRotate);
var degrees = (this.axisangleT.angle * 57.29577951308232);
var sb =  new J.util.SB ();
this.vectorT.set (this.axisangleT.x, this.axisangleT.y, this.axisangleT.z);
if (degrees < 0.01) return "{0 0 1 0}";
this.vectorT.normalize ();
this.vectorT.scale (1000);
sb.append ("{");
J.viewer.TransformManager.truncate0 (sb, this.vectorT.x);
J.viewer.TransformManager.truncate0 (sb, this.vectorT.y);
J.viewer.TransformManager.truncate0 (sb, this.vectorT.z);
J.viewer.TransformManager.truncate2 (sb, degrees);
sb.append ("}");
return sb.toString ();
});
$_M(c$, "getMoveToText", 
function (timespan, addComments) {
this.finalizeTransformParameters ();
var sb =  new J.util.SB ();
sb.append ("moveto ");
if (addComments) sb.append ("/* time, axisAngle */ ");
sb.appendF (timespan);
sb.append (" ").append (this.getRotationText ());
if (addComments) sb.append (" /* zoom, translation */ ");
J.viewer.TransformManager.truncate2 (sb, this.zoomPercentSetting);
J.viewer.TransformManager.truncate2 (sb, this.getTranslationXPercent ());
J.viewer.TransformManager.truncate2 (sb, this.getTranslationYPercent ());
sb.append (" ");
if (addComments) sb.append (" /* center, rotationRadius */ ");
sb.append (this.getCenterText ());
sb.append (" ").appendF (this.modelRadius);
sb.append (this.getNavigationText (addComments));
if (addComments) sb.append (" /* cameraDepth, cameraX, cameraY */ ");
J.viewer.TransformManager.truncate2 (sb, this.cameraDepth);
J.viewer.TransformManager.truncate2 (sb, this.cameraSetting.x);
J.viewer.TransformManager.truncate2 (sb, this.cameraSetting.y);
sb.append (";");
return sb.toString ();
}, "~N,~B");
$_M(c$, "getCenterText", 
($fz = function () {
return J.util.Escape.eP (this.fixedRotationCenter);
}, $fz.isPrivate = true, $fz));
$_M(c$, "getRotateXyzText", 
($fz = function () {
var sb =  new J.util.SB ();
var m20 = this.matrixRotate.m20;
var rY = -(Math.asin (m20) * 57.29577951308232);
var rX;
var rZ;
if (m20 > .999 || m20 < -0.999) {
rX = -(Math.atan2 (this.matrixRotate.m12, this.matrixRotate.m11) * 57.29577951308232);
rZ = 0;
} else {
rX = (Math.atan2 (this.matrixRotate.m21, this.matrixRotate.m22) * 57.29577951308232);
rZ = (Math.atan2 (this.matrixRotate.m10, this.matrixRotate.m00) * 57.29577951308232);
}sb.append ("reset");
sb.append (";center ").append (this.getCenterText ());
if (rX != 0) {
sb.append ("; rotate x");
J.viewer.TransformManager.truncate2 (sb, rX);
}if (rY != 0) {
sb.append ("; rotate y");
J.viewer.TransformManager.truncate2 (sb, rY);
}if (rZ != 0) {
sb.append ("; rotate z");
J.viewer.TransformManager.truncate2 (sb, rZ);
}sb.append (";");
this.addZoomTranslationNavigationText (sb);
return sb.toString ();
}, $fz.isPrivate = true, $fz));
$_M(c$, "addZoomTranslationNavigationText", 
($fz = function (sb) {
if (this.zoomPercent != 100) {
sb.append (" zoom");
J.viewer.TransformManager.truncate2 (sb, this.zoomPercent);
sb.append (";");
}var tX = this.getTranslationXPercent ();
if (tX != 0) {
sb.append (" translate x");
J.viewer.TransformManager.truncate2 (sb, tX);
sb.append (";");
}var tY = this.getTranslationYPercent ();
if (tY != 0) {
sb.append (" translate y");
J.viewer.TransformManager.truncate2 (sb, tY);
sb.append (";");
}if (this.modelRadius != this.rotationRadiusDefault || this.modelRadius == 10) {
sb.append (" set rotationRadius");
J.viewer.TransformManager.truncate2 (sb, this.modelRadius);
sb.append (";");
}if (this.mode == 1) {
sb.append ("navigate 0 center ").append (J.util.Escape.eP (this.navigationCenter));
sb.append (";navigate 0 translate");
J.viewer.TransformManager.truncate2 (sb, this.getNavigationOffsetPercent ('X'));
J.viewer.TransformManager.truncate2 (sb, this.getNavigationOffsetPercent ('Y'));
sb.append (";navigate 0 depth ");
J.viewer.TransformManager.truncate2 (sb, this.getNavigationDepthPercent ());
sb.append (";");
}}, $fz.isPrivate = true, $fz), "J.util.SB");
$_M(c$, "getRotateZyzText", 
($fz = function (iAddComment) {
var sb =  new J.util.SB ();
var m = this.viewer.getModelSetAuxiliaryInfoValue ("defaultOrientationMatrix");
if (m == null) {
m = this.matrixRotate;
} else {
m = J.util.Matrix3f.newM (m);
m.invert ();
m.mul2 (this.matrixRotate, m);
}var m22 = m.m22;
var rY = (Math.acos (m22) * 57.29577951308232);
var rZ1;
var rZ2;
if (m22 > .999 || m22 < -0.999) {
rZ1 = (Math.atan2 (m.m10, m.m11) * 57.29577951308232);
rZ2 = 0;
} else {
rZ1 = (Math.atan2 (m.m21, -m.m20) * 57.29577951308232);
rZ2 = (Math.atan2 (m.m12, m.m02) * 57.29577951308232);
}if (rZ1 != 0 && rY != 0 && rZ2 != 0 && iAddComment) sb.append ("#Follows Z-Y-Z convention for Euler angles\n");
sb.append ("reset");
sb.append (";center ").append (this.getCenterText ());
if (rZ1 != 0) {
sb.append ("; rotate z");
J.viewer.TransformManager.truncate2 (sb, rZ1);
}if (rY != 0) {
sb.append ("; rotate y");
J.viewer.TransformManager.truncate2 (sb, rY);
}if (rZ2 != 0) {
sb.append ("; rotate z");
J.viewer.TransformManager.truncate2 (sb, rZ2);
}sb.append (";");
this.addZoomTranslationNavigationText (sb);
return sb.toString ();
}, $fz.isPrivate = true, $fz), "~B");
c$.truncate0 = $_M(c$, "truncate0", 
($fz = function (sb, val) {
sb.appendC (' ');
sb.appendI (Math.round (val));
}, $fz.isPrivate = true, $fz), "J.util.SB,~N");
c$.truncate2 = $_M(c$, "truncate2", 
($fz = function (sb, val) {
sb.appendC (' ');
sb.appendF (Math.round (val * 100) / 100);
}, $fz.isPrivate = true, $fz), "J.util.SB,~N");
$_M(c$, "setSpinXYZ", 
function (x, y, z) {
if (!Float.isNaN (x)) this.spinX = x;
if (!Float.isNaN (y)) this.spinY = y;
if (!Float.isNaN (z)) this.spinZ = z;
if (this.isSpinInternal || this.isSpinFixed) this.clearSpin ();
}, "~N,~N,~N");
$_M(c$, "setSpinFps", 
function (value) {
if (value <= 0) value = 1;
 else if (value > 50) value = 50;
this.spinFps = value;
}, "~N");
$_M(c$, "setNavXYZ", 
function (x, y, z) {
if (!Float.isNaN (x)) this.navX = x;
if (!Float.isNaN (y)) this.navY = y;
if (!Float.isNaN (z)) this.navZ = z;
}, "~N,~N,~N");
$_M(c$, "clearSpin", 
($fz = function () {
this.setSpinOff ();
this.setNavOn (false);
this.isSpinInternal = false;
this.isSpinFixed = false;
}, $fz.isPrivate = true, $fz));
$_M(c$, "getSpinOn", 
function () {
return this.spinOn;
});
$_M(c$, "getNavOn", 
function () {
return this.navOn;
});
$_M(c$, "setSpinOn", 
function () {
this.setSpin (null, true, 3.4028235E38, null, null, null, false);
});
$_M(c$, "setSpinOff", 
function () {
this.setSpin (null, false, 3.4028235E38, null, null, null, false);
});
$_M(c$, "setSpin", 
($fz = function (eval, spinOn, endDegrees, endPositions, dihedralList, bsAtoms, isGesture) {
if (this.navOn && spinOn) this.setNavOn (false);
if (this.spinOn == spinOn) return;
this.spinOn = spinOn;
this.viewer.getGlobalSettings ().setB ("_spinning", spinOn);
if (spinOn) {
if (this.spinThread == null) {
this.spinThread = J.api.Interface.getOptionInterface ("thread.SpinThread");
this.spinThread.setManager (this, this.viewer, [Float.$valueOf (endDegrees), endPositions, dihedralList, bsAtoms, isGesture ? Boolean.TRUE : null]);
this.spinIsGesture = isGesture;
if (bsAtoms == null && dihedralList == null) {
this.spinThread.start ();
} else {
this.spinThread.setEval (eval);
this.spinThread.run ();
}}} else if (this.spinThread != null) {
this.spinThread.reset ();
this.spinThread = null;
}}, $fz.isPrivate = true, $fz), "J.api.JmolScriptEvaluator,~B,~N,J.util.JmolList,~A,J.util.BS,~B");
$_M(c$, "setNavOn", 
function (navOn) {
if (Float.isNaN (this.navFps)) return;
var wasOn = this.navOn;
if (navOn && this.spinOn) this.setSpin (null, false, 0, null, null, null, false);
this.navOn = navOn;
this.viewer.getGlobalSettings ().setB ("_navigating", navOn);
if (!navOn) this.navInterrupt ();
if (navOn) {
if (this.navX == 0 && this.navY == 0 && this.navZ == 0) this.navZ = 1;
if (this.navFps == 0) this.navFps = 10;
if (this.spinThread == null) {
this.spinThread = J.api.Interface.getOptionInterface ("thread.SpinThread");
this.spinThread.setManager (this, this.viewer, null);
this.spinThread.start ();
}} else if (wasOn) {
if (this.spinThread != null) {
this.spinThread.interrupt ();
this.spinThread = null;
}}}, "~B");
$_M(c$, "setVibrationScale", 
function (scale) {
this.vibrationScale = scale;
}, "~N");
$_M(c$, "setVibrationPeriod", 
function (period) {
if (Float.isNaN (period)) {
period = this.vibrationPeriod;
} else if (period == 0) {
this.vibrationPeriod = 0;
this.vibrationPeriodMs = 0;
} else {
this.vibrationPeriod = Math.abs (period);
this.vibrationPeriodMs = Clazz.floatToInt (this.vibrationPeriod * 1000);
if (period > 0) return;
period = -period;
}this.setVibrationOn (period > 0 && this.viewer.modelGetLastVibrationIndex (this.viewer.getCurrentModelIndex (), 0) >= 0);
}, "~N");
$_M(c$, "setVibrationT", 
function (t) {
this.vibrationT = t;
if (this.vibrationScale == 0) this.vibrationScale = this.viewer.global.vibrationScale;
}, "~N");
$_M(c$, "isVibrationOn", 
function () {
return this.vibrationOn;
});
$_M(c$, "setVibrationOn", 
($fz = function (vibrationOn) {
if (!vibrationOn) {
if (this.vibrationThread != null) {
this.vibrationThread.interrupt ();
this.vibrationThread = null;
}this.vibrationOn = false;
return;
}if (this.viewer.getModelCount () < 1) {
this.vibrationOn = false;
return;
}if (this.vibrationThread == null) {
this.vibrationThread = J.api.Interface.getOptionInterface ("thread.VibrationThread");
this.vibrationThread.setManager (this, this.viewer, null);
this.vibrationThread.start ();
}this.vibrationOn = true;
}, $fz.isPrivate = true, $fz), "~B");
$_M(c$, "clearVibration", 
($fz = function () {
this.setVibrationOn (false);
this.vibrationScale = 0;
}, $fz.isPrivate = true, $fz));
$_M(c$, "setStereoMode2", 
function (twoColors) {
this.stereoMode = J.constant.EnumStereoMode.CUSTOM;
this.stereoColors = twoColors;
}, "~A");
$_M(c$, "setStereoMode", 
function (stereoMode) {
this.stereoColors = null;
this.stereoMode = stereoMode;
}, "J.constant.EnumStereoMode");
$_M(c$, "setStereoDegrees", 
function (stereoDegrees) {
this.stereoDegrees = stereoDegrees;
this.stereoRadians = stereoDegrees * 0.017453292;
}, "~N");
$_M(c$, "getStereoRotationMatrix", 
function (stereoFrame) {
this.stereoFrame = stereoFrame;
if (!stereoFrame) return this.matrixRotate;
this.matrixTemp3.rotY (-this.stereoRadians);
this.matrixStereo.mul2 (this.matrixTemp3, this.matrixRotate);
return this.matrixStereo;
}, "~B");
$_M(c$, "isWindowCentered", 
function () {
return this.windowCentered;
});
$_M(c$, "setWindowCentered", 
function (TF) {
this.windowCentered = TF;
this.resetNavigationPoint (true);
}, "~B");
$_M(c$, "getRotationCenter", 
function () {
return this.fixedRotationCenter;
});
$_M(c$, "getRotationRadius", 
function () {
return this.modelRadius;
});
$_M(c$, "setRotationRadius", 
function (angstroms, doAll) {
angstroms = (this.modelRadius = (angstroms <= 0 ? this.viewer.calcRotationRadius (this.fixedRotationCenter) : angstroms));
if (doAll) this.viewer.setRotationRadius (angstroms, false);
return angstroms;
}, "~N,~B");
$_M(c$, "setRotationCenterAndRadiusXYZ", 
($fz = function (newCenterOfRotation, andRadius) {
this.resetNavigationPoint (false);
if (newCenterOfRotation == null) {
this.setFixedRotationCenter (this.rotationCenterDefault);
this.modelRadius = this.rotationRadiusDefault;
return;
}this.setFixedRotationCenter (newCenterOfRotation);
if (andRadius && this.windowCentered) this.modelRadius = this.viewer.calcRotationRadius (this.fixedRotationCenter);
}, $fz.isPrivate = true, $fz), "J.util.P3,~B");
$_M(c$, "setRotCenterRel", 
($fz = function (relativeTo, pt) {
var pt1 = J.util.P3.newP (pt);
if (relativeTo === "average") pt1.add (this.viewer.getAverageAtomPoint ());
 else if (relativeTo === "boundbox") pt1.add (this.viewer.getBoundBoxCenter ());
 else if (relativeTo !== "absolute") pt1.setT (this.rotationCenterDefault);
this.setRotationCenterAndRadiusXYZ (pt1, true);
}, $fz.isPrivate = true, $fz), "~S,J.util.P3");
$_M(c$, "setNewRotationCenter", 
function (center, doScale) {
if (center == null) center = this.rotationCenterDefault;
if (this.windowCentered) {
this.translateToPercent ('x', 0);
this.translateToPercent ('y', 0);
this.setRotationCenterAndRadiusXYZ (center, true);
if (doScale) this.resetFitToScreen (true);
} else {
this.moveRotationCenter (center, true);
}}, "J.util.P3,~B");
$_M(c$, "moveRotationCenter", 
function (center, toXY) {
this.setRotationCenterAndRadiusXYZ (center, false);
if (toXY) this.setRotationPointXY (this.fixedRotationCenter);
}, "J.util.P3,~B");
$_M(c$, "setCenter", 
function () {
this.setRotationCenterAndRadiusXYZ (this.fixedRotationCenter, true);
});
$_M(c$, "setCenterAt", 
function (relativeTo, pt) {
this.setRotCenterRel (relativeTo, pt);
this.resetFitToScreen (true);
}, "~S,J.util.P3");
$_M(c$, "setFrameOffset", 
function (modelIndex) {
if (this.frameOffsets == null || modelIndex < 0 || modelIndex >= this.frameOffsets.length) this.frameOffset.set (0, 0, 0);
 else this.frameOffset.setT (this.frameOffsets[modelIndex]);
}, "~N");
$_M(c$, "setFrameOffsets", 
function (offsets) {
this.frameOffsets = offsets;
}, "~A");
$_M(c$, "setSelectedTranslation", 
function (bsAtoms, xyz, xy) {
this.bsSelectedAtoms = bsAtoms;
switch (xyz) {
case 'X':
case 'x':
this.ptOffset.x += xy;
break;
case 'Y':
case 'y':
this.ptOffset.y += xy;
break;
case 'Z':
case 'z':
this.ptOffset.z += xy;
break;
}
}, "J.util.BS,~S,~N");
$_M(c$, "setNavFps", 
function (navFps) {
this.navFps = navFps;
}, "~N");
$_M(c$, "calcCameraFactors", 
function () {
if (Float.isNaN (this.cameraDepth)) {
this.cameraDepth = this.cameraDepthSetting;
this.zoomFactor = 3.4028235E38;
}this.cameraDistance = this.cameraDepth * this.screenPixelCount;
this.referencePlaneOffset = this.cameraDistance + this.screenPixelCount / 2;
this.scalePixelsPerAngstrom = (this.scale3D && !this.perspectiveDepth && this.mode != 1 ? 72 / this.scale3DAngstromsPerInch * (this.antialias ? 2 : 1) : this.screenPixelCount / this.visualRange);
if (this.mode != 1) this.mode = (this.camera.z == 0 ? 0 : 2);
this.perspectiveShiftXY.set (this.camera.z == 0 ? 0 : this.camera.x * this.scalePixelsPerAngstrom / this.screenWidth * 100, this.camera.z == 0 ? 0 : this.camera.y * this.scalePixelsPerAngstrom / this.screenHeight * 100, 0);
this.modelRadiusPixels = this.modelRadius * this.scalePixelsPerAngstrom;
var offset100 = (2 * this.modelRadius) / this.visualRange * this.referencePlaneOffset;
if (this.mode == 1) {
this.calcNavCameraFactors (offset100);
return;
}this.zoomFactor = 3.4028235E38;
this.modelCenterOffset = this.referencePlaneOffset;
if (!this.scale3D || this.perspectiveDepth) this.scalePixelsPerAngstrom *= (this.modelCenterOffset / offset100) * this.zoomPercent / 100;
this.modelRadiusPixels = this.modelRadius * this.scalePixelsPerAngstrom;
});
$_M(c$, "calcNavCameraFactors", 
($fz = function (offset100) {
if (this.zoomFactor == 3.4028235E38) {
if (this.zoomPercent > 10000) this.zoomPercent = 10000;
this.modelCenterOffset = offset100 * 100 / this.zoomPercent;
} else if (this.prevZoomSetting != this.zoomPercentSetting) {
if (this.zoomRatio == 0) this.modelCenterOffset = offset100 * 100 / this.zoomPercentSetting;
 else this.modelCenterOffset += (1 - this.zoomRatio) * this.referencePlaneOffset;
this.navMode = -1;
}this.prevZoomSetting = this.zoomPercentSetting;
this.zoomFactor = this.modelCenterOffset / this.referencePlaneOffset;
this.zoomPercent = (this.zoomFactor == 0 ? 10000 : offset100 / this.modelCenterOffset * 100);
}, $fz.isPrivate = true, $fz), "~N");
$_M(c$, "getPerspectiveFactor", 
function (z) {
return (z <= 0 ? this.referencePlaneOffset : this.referencePlaneOffset / z);
}, "~N");
$_M(c$, "getTemporaryScreenPoint", 
function (pointAngstroms, pt0) {
this.matrixTransform.transform2 (pointAngstroms, this.point3fScreenTemp);
var z = this.point3fScreenTemp.z;
if (Float.isNaN (z)) {
if (!this.haveNotifiedNaN && J.util.Logger.debugging) J.util.Logger.debug ("NaN seen in TransformPoint");
this.haveNotifiedNaN = true;
z = this.point3fScreenTemp.z = 1;
} else if (z <= 0) {
z = this.point3fScreenTemp.z = 1;
}switch (this.mode) {
case 1:
this.point3fScreenTemp.x -= this.navigationShiftXY.x;
this.point3fScreenTemp.y -= this.navigationShiftXY.y;
break;
case 2:
this.point3fScreenTemp.x += this.perspectiveShiftXY.x;
this.point3fScreenTemp.y += this.perspectiveShiftXY.y;
break;
}
if (this.perspectiveDepth) {
var factor = this.getPerspectiveFactor (z);
this.point3fScreenTemp.x *= factor;
this.point3fScreenTemp.y *= factor;
}switch (this.mode) {
case 1:
this.point3fScreenTemp.x += this.navigationOffset.x;
this.point3fScreenTemp.y += this.navigationOffset.y;
break;
case 2:
this.point3fScreenTemp.x -= this.perspectiveShiftXY.x;
this.point3fScreenTemp.y -= this.perspectiveShiftXY.y;
case 0:
this.point3fScreenTemp.x += this.fixedRotationOffset.x;
this.point3fScreenTemp.y += this.fixedRotationOffset.y;
break;
}
if (Float.isNaN (this.point3fScreenTemp.x) && !this.haveNotifiedNaN) {
if (J.util.Logger.debugging) J.util.Logger.debug ("NaN found in transformPoint ");
this.haveNotifiedNaN = true;
}this.point3iScreenTemp.set (Clazz.floatToInt (this.point3fScreenTemp.x), Clazz.floatToInt (this.point3fScreenTemp.y), Clazz.floatToInt (this.point3fScreenTemp.z));
if (pt0 != null && (this.slabPlane != null && pt0.x * this.slabPlane.x + pt0.y * this.slabPlane.y + pt0.z * this.slabPlane.z + this.slabPlane.w > 0 || this.depthPlane != null && pt0.x * this.depthPlane.x + pt0.y * this.depthPlane.y + pt0.z * this.depthPlane.z + this.depthPlane.w < 0)) this.point3iScreenTemp.z = 1;
}, "J.util.P3,J.util.P3");
$_M(c$, "unTransformPoint", 
function (screenPt, coordPt) {
this.untransformedPoint.setT (screenPt);
switch (this.mode) {
case 1:
this.untransformedPoint.x -= this.navigationOffset.x;
this.untransformedPoint.y -= this.navigationOffset.y;
break;
case 2:
this.point3fScreenTemp.x += this.perspectiveShiftXY.x;
this.point3fScreenTemp.y += this.perspectiveShiftXY.y;
case 0:
this.untransformedPoint.x -= this.fixedRotationOffset.x;
this.untransformedPoint.y -= this.fixedRotationOffset.y;
}
if (this.perspectiveDepth) {
var factor = this.getPerspectiveFactor (this.untransformedPoint.z);
this.untransformedPoint.x /= factor;
this.untransformedPoint.y /= factor;
}switch (this.mode) {
case 1:
this.untransformedPoint.x += this.navigationShiftXY.x;
this.untransformedPoint.y += this.navigationShiftXY.y;
break;
case 2:
this.untransformedPoint.x -= this.perspectiveShiftXY.x;
this.untransformedPoint.y -= this.perspectiveShiftXY.y;
break;
}
this.matrixTransformInv.transform2 (this.untransformedPoint, coordPt);
}, "J.util.P3,J.util.P3");
$_M(c$, "canNavigate", 
function () {
return true;
});
$_M(c$, "resetNavigationPoint", 
function (doResetSlab) {
if (this.zoomPercent < 5 && this.mode != 1) {
this.perspectiveDepth = true;
this.mode = 1;
return;
}if (this.mode == 1) {
this.navMode = 1;
this.slabPercentSetting = 0;
this.perspectiveDepth = true;
} else if (doResetSlab) {
this.slabPercentSetting = 100;
}this.viewer.setFloatProperty ("slabRange", 0);
if (doResetSlab) {
this.setSlabEnabled (this.mode == 1);
}this.zoomFactor = 3.4028235E38;
this.zoomPercentSetting = this.zoomPercent;
}, "~B");
$_M(c$, "setNavigatePt", 
function (pt) {
this.navigationCenter.setT (pt);
this.navMode = 3;
this.navigating = true;
this.finalizeTransformParameters ();
this.navigating = false;
}, "J.util.P3");
$_M(c$, "getNavigationCenter", 
function () {
return this.navigationCenter;
});
$_M(c$, "getNavigationDepthPercent", 
function () {
return this.navigationDepth;
});
$_M(c$, "setNavigationSlabOffsetPercent", 
function (percent) {
this.viewer.getGlobalSettings ().setF ("navigationSlab", percent);
this.calcCameraFactors ();
this.navigationSlabOffset = percent / 50 * this.modelRadiusPixels;
}, "~N");
$_M(c$, "getNavigationOffset", 
function () {
this.transformPoint2 (this.navigationCenter, this.navigationOffset);
return this.navigationOffset;
});
$_M(c$, "getNavPtHeight", 
function () {
return this.height / 2;
});
$_M(c$, "getNavigationOffsetPercent", 
function (XorY) {
this.getNavigationOffset ();
if (this.width == 0 || this.height == 0) return 0;
return (XorY == 'X' ? (this.navigationOffset.x - this.width / 2) * 100 / this.width : (this.navigationOffset.y - this.getNavPtHeight ()) * 100 / this.height);
}, "~S");
$_M(c$, "getNavigationText", 
function (addComments) {
var s = (addComments ? " /* navigation center, translation, depth */ " : " ");
if (this.mode != 1) return s + "{0 0 0} 0 0 0";
this.getNavigationOffset ();
return s + J.util.Escape.eP (this.navigationCenter) + " " + this.getNavigationOffsetPercent ('X') + " " + this.getNavigationOffsetPercent ('Y') + " " + this.getNavigationDepthPercent ();
}, "~B");
$_M(c$, "setScreenParameters", 
function (screenWidth, screenHeight, useZoomLarge, antialias, resetSlab, resetZoom) {
var pt = (this.mode == 1 ? J.util.P3.newP (this.navigationCenter) : null);
var ptoff = J.util.P3.newP (this.navigationOffset);
ptoff.x = ptoff.x / this.width;
ptoff.y = ptoff.y / this.height;
this.setScreenParameters0 (screenWidth, screenHeight, useZoomLarge, antialias, resetSlab, resetZoom);
if (pt != null) {
this.navigationCenter.setT (pt);
this.navTranslatePercentOrTo (-1, ptoff.x * this.width, ptoff.y * this.height);
this.setNavigatePt (pt);
}}, "~N,~N,~B,~B,~B,~B");
$_M(c$, "navInterrupt", 
($fz = function () {
if (this.nav != null) this.nav.interrupt ();
}, $fz.isPrivate = true, $fz));
$_M(c$, "getNav", 
($fz = function () {
if (this.nav != null) return true;
this.nav = J.api.Interface.getOptionInterface ("navigate.Navigator");
if (this.nav == null) return false;
this.nav.set (this, this.viewer);
return true;
}, $fz.isPrivate = true, $fz));
$_M(c$, "navigateList", 
function (eval, list) {
if (this.getNav ()) this.nav.navigateList (eval, list);
}, "J.api.JmolScriptEvaluator,J.util.JmolList");
$_M(c$, "navigateAxis", 
function (rotAxis, degrees) {
if (this.getNav ()) this.nav.navigateAxis (rotAxis, degrees);
}, "J.util.V3,~N");
$_M(c$, "zoomByFactor", 
function (factor, x, y) {
if (this.mode != 1 || !this.zoomEnabled || factor <= 0) this.zoomByFactor0 (factor, x, y);
 else if (this.getNav ()) this.nav.zoomByFactor (factor, x, y);
}, "~N,~N,~N");
$_M(c$, "setNavigationOffsetRelative", 
function () {
if (this.getNav ()) this.nav.setNavigationOffsetRelative ();
});
$_M(c$, "navigateKey", 
function (keyCode, modifiers) {
if (this.getNav ()) this.nav.navigateKey (keyCode, modifiers);
}, "~N,~N");
$_M(c$, "setNavigationDepthPercent", 
function (percent) {
if (this.getNav ()) this.nav.setNavigationDepthPercent (percent);
}, "~N");
$_M(c$, "navTranslatePercentOrTo", 
function (seconds, x, y) {
if (this.getNav ()) this.nav.navTranslatePercentOrTo (seconds, x, y);
}, "~N,~N,~N");
$_M(c$, "calcNavigationPoint", 
function () {
if (this.getNav ()) this.nav.calcNavigationPoint ();
});
$_M(c$, "getNavigationState", 
function () {
return (this.mode == 1 && this.getNav () ? this.nav.getNavigationState () : "");
});
$_M(c$, "setZoomHeight", 
function (zoomHeight, zoomLarge) {
this.zoomHeight = zoomHeight;
this.scaleFitToScreen (false, zoomLarge, false, true);
}, "~B,~B");
$_M(c$, "setAll", 
function (center, m, navCenter, zoom, xTrans, yTrans, rotationRadius, pixelScale, navDepth, xNav, yNav, cameraDepth, cameraX, cameraY) {
this.setRotation (m);
if (center != null) this.moveRotationCenter (center, !this.windowCentered);
if (navCenter != null && this.mode == 1) this.navigationCenter.setT (navCenter);
if (!Float.isNaN (cameraDepth)) this.setCameraDepthPercent (cameraDepth, false);
if (!Float.isNaN (cameraX) && !Float.isNaN (cameraY)) this.setCamera (cameraX, cameraY);
if (!Float.isNaN (zoom)) this.zoomToPercent (zoom);
if (!Float.isNaN (rotationRadius)) this.modelRadius = rotationRadius;
if (!Float.isNaN (pixelScale)) this.scaleDefaultPixelsPerAngstrom = pixelScale;
if (!Float.isNaN (xTrans) && !Float.isNaN (yTrans)) {
this.translateToPercent ('x', xTrans);
this.translateToPercent ('y', yTrans);
}if (!Float.isNaN (xNav) && !Float.isNaN (yNav)) this.navTranslatePercentOrTo (0, xNav, yNav);
if (!Float.isNaN (navDepth)) this.setNavigationDepthPercent (navDepth);
}, "J.util.P3,J.util.Matrix3f,J.util.P3,~N,~N,~N,~N,~N,~N,~N,~N,~N,~N,~N");
Clazz.defineStatics (c$,
"degreesPerRadian", 57.29577951308232,
"DEFAULT_NAV_FPS", 10,
"DEFAULT_SPIN_Y", 30,
"DEFAULT_SPIN_FPS", 30,
"MAXIMUM_ZOOM_PERCENTAGE", 200000,
"MAXIMUM_ZOOM_PERSPECTIVE_DEPTH", 10000,
"MODE_STANDARD", 0,
"MODE_NAVIGATION", 1,
"MODE_PERSPECTIVE_PYMOL", 2,
"DEFAULT_PERSPECTIVE_MODEL", 11,
"NAV_MODE_IGNORE", -2,
"NAV_MODE_ZOOMED", -1,
"NAV_MODE_NONE", 0,
"NAV_MODE_RESET", 1,
"NAV_MODE_NEWXY", 2,
"NAV_MODE_NEWXYZ", 3,
"NAV_MODE_NEWZ", 4);
});
