Clazz.declarePackage ("J.g3d");
Clazz.load (["java.lang.Thread"], "J.g3d.Platform3D", null, function () {
c$ = Clazz.decorateAsClass (function () {
this.windowWidth = 0;
this.windowHeight = 0;
this.windowSize = 0;
this.bufferWidth = 0;
this.bufferHeight = 0;
this.bufferSize = 0;
this.bufferSizeT = 0;
this.bufferedImage = null;
this.pBuffer = null;
this.pBufferT = null;
this.zBuffer = null;
this.zBufferT = null;
this.widthOffscreen = 0;
this.heightOffscreen = 0;
this.offscreenImage = null;
this.graphicsForTextOrImage = null;
this.useClearingThread = false;
this.clearingThread = null;
this.apiPlatform = null;
if (!Clazz.isClassDefined ("J.g3d.Platform3D.ClearingThread")) {
J.g3d.Platform3D.$Platform3D$ClearingThread$ ();
}
Clazz.instantialize (this, arguments);
}, J.g3d, "Platform3D");
Clazz.makeConstructor (c$, 
function (apiPlatform) {
this.initialize (false);
this.apiPlatform = apiPlatform;
}, "J.api.ApiPlatform");
$_M(c$, "getGraphicsForMetrics", 
function () {
return this.apiPlatform.getGraphics (this.allocateOffscreenImage (1, 1));
});
$_M(c$, "initialize", 
function (useClearingThread) {
this.useClearingThread = useClearingThread;
if (useClearingThread) {
this.clearingThread = Clazz.innerTypeInstance (J.g3d.Platform3D.ClearingThread, this, null);
this.clearingThread.start ();
}}, "~B");
$_M(c$, "allocateTBuffers", 
function (antialiasTranslucent) {
this.bufferSizeT = (antialiasTranslucent ? this.bufferSize : this.windowSize);
this.zBufferT =  Clazz.newIntArray (this.bufferSizeT, 0);
this.pBufferT =  Clazz.newIntArray (this.bufferSizeT, 0);
}, "~B");
$_M(c$, "allocateBuffers", 
function (width, height, antialias, isImageWrite) {
this.windowWidth = width;
this.windowHeight = height;
this.windowSize = width * height;
if (antialias) {
width *= 2;
height *= 2;
}this.bufferWidth = width;
this.bufferHeight = height;
this.bufferSize = this.bufferWidth * this.bufferHeight;
this.zBuffer =  Clazz.newIntArray (this.bufferSize, 0);
this.pBuffer =  Clazz.newIntArray (this.bufferSize, 0);
this.bufferedImage = this.apiPlatform.allocateRgbImage (this.windowWidth, this.windowHeight, this.pBuffer, this.windowSize, J.g3d.Platform3D.backgroundTransparent, isImageWrite);
}, "~N,~N,~B,~B");
$_M(c$, "releaseBuffers", 
function () {
this.windowWidth = this.windowHeight = this.bufferWidth = this.bufferHeight = this.bufferSize = -1;
if (this.bufferedImage != null) {
this.apiPlatform.flushImage (this.bufferedImage);
this.bufferedImage = null;
}this.pBuffer = null;
this.zBuffer = null;
this.pBufferT = null;
this.zBufferT = null;
});
$_M(c$, "hasContent", 
function () {
for (var i = this.bufferSize; --i >= 0; ) if (this.zBuffer[i] != 2147483647) return true;

return false;
});
$_M(c$, "clearScreenBuffer", 
function () {
for (var i = this.bufferSize; --i >= 0; ) {
this.zBuffer[i] = 2147483647;
this.pBuffer[i] = 0;
}
});
$_M(c$, "setBackgroundColor", 
function (bgColor) {
if (this.pBuffer == null) return;
for (var i = this.bufferSize; --i >= 0; ) if (this.pBuffer[i] == 0) this.pBuffer[i] = bgColor;

}, "~N");
$_M(c$, "clearTBuffer", 
function () {
for (var i = this.bufferSizeT; --i >= 0; ) {
this.zBufferT[i] = 2147483647;
this.pBufferT[i] = 0;
}
});
$_M(c$, "clearBuffer", 
function () {
if (this.useClearingThread) {
this.clearingThread.clearClientBuffer ();
} else {
this.clearScreenBuffer ();
}});
$_M(c$, "clearScreenBufferThreaded", 
function () {
if (this.useClearingThread) this.clearingThread.releaseBufferForClearing ();
});
$_M(c$, "notifyEndOfRendering", 
function () {
this.apiPlatform.notifyEndOfRendering ();
});
$_M(c$, "getGraphicsForTextOrImage", 
function (width, height) {
if (width > this.widthOffscreen || height > this.heightOffscreen) {
if (this.offscreenImage != null) {
this.apiPlatform.disposeGraphics (this.graphicsForTextOrImage);
this.apiPlatform.flushImage (this.offscreenImage);
}if (width > this.widthOffscreen) this.widthOffscreen = width;
if (height > this.heightOffscreen) this.heightOffscreen = height;
this.offscreenImage = this.allocateOffscreenImage (this.widthOffscreen, this.heightOffscreen);
this.graphicsForTextOrImage = this.apiPlatform.getStaticGraphics (this.offscreenImage, J.g3d.Platform3D.backgroundTransparent);
}return this.graphicsForTextOrImage;
}, "~N,~N");
$_M(c$, "allocateOffscreenImage", 
($fz = function (width, height) {
return this.apiPlatform.newOffScreenImage (width, height);
}, $fz.isPrivate = true, $fz), "~N,~N");
$_M(c$, "setBackgroundTransparent", 
function (tf) {
($t$ = J.g3d.Platform3D.backgroundTransparent = tf, J.g3d.Platform3D.prototype.backgroundTransparent = J.g3d.Platform3D.backgroundTransparent, $t$);
}, "~B");
c$.$Platform3D$ClearingThread$ = function () {
Clazz.pu$h ();
c$ = Clazz.decorateAsClass (function () {
Clazz.prepareCallback (this, arguments);
this.bufferHasBeenCleared = false;
this.clientHasBuffer = false;
Clazz.instantialize (this, arguments);
}, J.g3d.Platform3D, "ClearingThread", Thread);
$_M(c$, "notifyBackgroundChange", 
function (a) {
this.bufferHasBeenCleared = false;
this.notify ();
}, "~N");
$_M(c$, "clearClientBuffer", 
function () {
while (!this.bufferHasBeenCleared) try {
this.wait ();
} catch (ie) {
if (Clazz.exceptionOf (ie, InterruptedException)) {
} else {
throw ie;
}
}

this.clientHasBuffer = true;
});
$_M(c$, "releaseBufferForClearing", 
function () {
this.clientHasBuffer = false;
this.bufferHasBeenCleared = false;
this.notify ();
});
$_M(c$, "waitForClientRelease", 
function () {
while (this.clientHasBuffer || this.bufferHasBeenCleared) try {
this.wait ();
} catch (ie) {
if (Clazz.exceptionOf (ie, InterruptedException)) {
} else {
throw ie;
}
}

});
$_M(c$, "notifyBufferReady", 
function () {
this.bufferHasBeenCleared = true;
this.notify ();
});
Clazz.overrideMethod (c$, "run", 
function () {
while (true) {
this.waitForClientRelease ();
this.b$["J.g3d.Platform3D"].clearScreenBuffer ();
this.notifyBufferReady ();
}
});
c$ = Clazz.p0p ();
};
Clazz.defineStatics (c$,
"desireClearingThread", false,
"backgroundTransparent", false);
});
