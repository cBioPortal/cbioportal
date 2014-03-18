Clazz.declarePackage ("J.util");
Clazz.load (null, "J.util.TempArray", ["J.util.P3", "$.P3i"], function () {
c$ = Clazz.decorateAsClass (function () {
this.lengthsFreePoints = null;
this.freePoints = null;
this.lengthsFreeScreens = null;
this.freeScreens = null;
this.lengthsFreeEnum = null;
this.freeEnum = null;
Clazz.instantialize (this, arguments);
}, J.util, "TempArray");
Clazz.prepareFields (c$, function () {
this.lengthsFreePoints =  Clazz.newIntArray (6, 0);
this.freePoints =  new Array (6);
this.lengthsFreeScreens =  Clazz.newIntArray (6, 0);
this.freeScreens =  new Array (6);
this.lengthsFreeEnum =  Clazz.newIntArray (2, 0);
this.freeEnum =  new Array (2);
});
Clazz.makeConstructor (c$, 
function () {
});
$_M(c$, "clear", 
function () {
this.clearTempPoints ();
this.clearTempScreens ();
});
c$.findBestFit = $_M(c$, "findBestFit", 
($fz = function (size, lengths) {
var iFit = -1;
var fitLength = 2147483647;
for (var i = lengths.length; --i >= 0; ) {
var freeLength = lengths[i];
if (freeLength >= size && freeLength < fitLength) {
fitLength = freeLength;
iFit = i;
}}
if (iFit >= 0) lengths[iFit] = 0;
return iFit;
}, $fz.isPrivate = true, $fz), "~N,~A");
c$.findShorter = $_M(c$, "findShorter", 
($fz = function (size, lengths) {
for (var i = lengths.length; --i >= 0; ) if (lengths[i] == 0) {
lengths[i] = size;
return i;
}
var iShortest = 0;
var shortest = lengths[0];
for (var i = lengths.length; --i > 0; ) if (lengths[i] < shortest) {
shortest = lengths[i];
iShortest = i;
}
if (shortest < size) {
lengths[iShortest] = size;
return iShortest;
}return -1;
}, $fz.isPrivate = true, $fz), "~N,~A");
$_M(c$, "clearTempPoints", 
($fz = function () {
for (var i = 0; i < 6; i++) {
this.lengthsFreePoints[i] = 0;
this.freePoints[i] = null;
}
}, $fz.isPrivate = true, $fz));
$_M(c$, "allocTempPoints", 
function (size) {
var tempPoints;
var iFit = J.util.TempArray.findBestFit (size, this.lengthsFreePoints);
if (iFit > 0) {
tempPoints = this.freePoints[iFit];
} else {
tempPoints =  new Array (size);
for (var i = size; --i >= 0; ) tempPoints[i] =  new J.util.P3 ();

}return tempPoints;
}, "~N");
$_M(c$, "freeTempPoints", 
function (tempPoints) {
for (var i = 0; i < this.freePoints.length; i++) if (this.freePoints[i] === tempPoints) {
this.lengthsFreePoints[i] = tempPoints.length;
return;
}
var iFree = J.util.TempArray.findShorter (tempPoints.length, this.lengthsFreePoints);
if (iFree >= 0) this.freePoints[iFree] = tempPoints;
}, "~A");
$_M(c$, "clearTempScreens", 
($fz = function () {
for (var i = 0; i < 6; i++) {
this.lengthsFreeScreens[i] = 0;
this.freeScreens[i] = null;
}
}, $fz.isPrivate = true, $fz));
$_M(c$, "allocTempScreens", 
function (size) {
var tempScreens;
var iFit = J.util.TempArray.findBestFit (size, this.lengthsFreeScreens);
if (iFit > 0) {
tempScreens = this.freeScreens[iFit];
} else {
tempScreens =  new Array (size);
for (var i = size; --i >= 0; ) tempScreens[i] =  new J.util.P3i ();

}return tempScreens;
}, "~N");
$_M(c$, "freeTempScreens", 
function (tempScreens) {
for (var i = 0; i < this.freeScreens.length; i++) if (this.freeScreens[i] === tempScreens) {
this.lengthsFreeScreens[i] = tempScreens.length;
return;
}
var iFree = J.util.TempArray.findShorter (tempScreens.length, this.lengthsFreeScreens);
if (iFree >= 0) this.freeScreens[iFree] = tempScreens;
}, "~A");
$_M(c$, "allocTempEnum", 
function (size) {
var tempEnum;
var iFit = J.util.TempArray.findBestFit (size, this.lengthsFreeEnum);
if (iFit > 0) {
tempEnum = this.freeEnum[iFit];
} else {
tempEnum =  new Array (size);
}return tempEnum;
}, "~N");
$_M(c$, "freeTempEnum", 
function (tempEnum) {
for (var i = 0; i < this.freeEnum.length; i++) if (this.freeEnum[i] === tempEnum) {
this.lengthsFreeEnum[i] = tempEnum.length;
return;
}
var iFree = J.util.TempArray.findShorter (tempEnum.length, this.lengthsFreeEnum);
if (iFree >= 0) this.freeEnum[iFree] = tempEnum;
}, "~A");
Clazz.defineStatics (c$,
"freePointsSize", 6,
"freeScreensSize", 6,
"freeEnumSize", 2);
});
