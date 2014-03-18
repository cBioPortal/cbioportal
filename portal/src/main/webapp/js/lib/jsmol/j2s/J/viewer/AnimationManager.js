Clazz.declarePackage ("J.viewer");
Clazz.load (["J.constant.EnumAnimationMode", "J.util.BS"], "J.viewer.AnimationManager", ["J.api.Interface", "J.util.BSUtil"], function () {
c$ = Clazz.decorateAsClass (function () {
this.animationThread = null;
this.modulationThread = null;
this.viewer = null;
this.animationOn = false;
this.animationFps = 0;
this.firstFrameDelayMs = 0;
this.lastFrameDelayMs = 0;
this.bsVisibleModels = null;
this.animationReplayMode = null;
this.bsDisplay = null;
this.animationFrames = null;
this.isMovie = false;
this.animationPaused = false;
this.currentModelIndex = 0;
this.currentAnimationFrame = 0;
this.morphCount = 0;
this.animationDirection = 1;
this.currentDirection = 1;
this.firstFrameIndex = 0;
this.lastFrameIndex = 0;
this.frameStep = 0;
this.backgroundModelIndex = -1;
this.currentMorphModel = 0;
this.firstFrameDelay = 0;
this.lastFrameDelay = 1;
this.lastFramePainted = 0;
this.lastModelPainted = 0;
this.intAnimThread = 0;
this.modulationPlay = false;
this.modulationFps = 1;
this.bsModulating = null;
Clazz.instantialize (this, arguments);
}, J.viewer, "AnimationManager");
Clazz.prepareFields (c$, function () {
this.bsVisibleModels =  new J.util.BS ();
this.animationReplayMode = J.constant.EnumAnimationMode.ONCE;
});
Clazz.makeConstructor (c$, 
function (viewer) {
this.viewer = viewer;
}, "J.viewer.Viewer");
$_M(c$, "setAnimationOn", 
function (animationOn) {
if (animationOn == this.animationOn) return;
if (!animationOn || !this.viewer.haveModelSet () || this.viewer.isHeadless ()) {
this.stopThread (false);
return;
}if (!this.viewer.getSpinOn ()) this.viewer.refresh (3, "Anim:setAnimationOn");
this.setAnimationRange (-1, -1);
this.resumeAnimation ();
}, "~B");
$_M(c$, "stopThread", 
function (isPaused) {
var stopped = false;
if (this.animationThread != null) {
this.animationThread.interrupt ();
this.animationThread = null;
stopped = true;
}this.animationPaused = isPaused;
if (stopped && !this.viewer.getSpinOn ()) this.viewer.refresh (3, "Viewer:setAnimationOff");
this.animation (false);
this.stopModulationThread ();
this.viewer.setStatusFrameChanged (false);
}, "~B");
$_M(c$, "setAnimationNext", 
function () {
return this.setAnimationRelative (this.animationDirection);
});
$_M(c$, "getCurrentModelIndex", 
function () {
return this.currentModelIndex;
});
$_M(c$, "currentIsLast", 
function () {
return (this.isMovie ? this.lastFramePainted == this.currentAnimationFrame : this.lastModelPainted == this.currentModelIndex);
});
$_M(c$, "currentFrameIs", 
function (f) {
var i = this.getCurrentModelIndex ();
return (this.morphCount == 0 ? i == f : Math.abs (this.currentMorphModel - f) < 0.001);
}, "~N");
$_M(c$, "clear", 
function () {
this.setMovie (null);
this.initializePointers (0);
this.setAnimationOn (false);
this.setModel (0, true);
this.currentDirection = 1;
this.setAnimationDirection (1);
this.setAnimationFps (10);
this.setAnimationReplayMode (J.constant.EnumAnimationMode.ONCE, 0, 0);
this.initializePointers (0);
});
$_M(c$, "getModelSpecial", 
function (i) {
switch (i) {
case -1:
i = this.firstFrameIndex;
break;
case 0:
if (this.morphCount > 0) return "-" + (1 + this.currentMorphModel);
i = this.getCurrentModelIndex ();
break;
case 1:
i = this.lastFrameIndex;
break;
}
return this.viewer.getModelNumberDotted (i);
}, "~N");
$_M(c$, "setDisplay", 
function (bs) {
this.bsDisplay = (bs == null || bs.cardinality () == 0 ? null : J.util.BSUtil.copy (bs));
}, "J.util.BS");
$_M(c$, "setMorphCount", 
function (n) {
this.morphCount = (this.isMovie ? 0 : n);
}, "~N");
$_M(c$, "morph", 
function (modelIndex) {
var m = Clazz.floatToInt (modelIndex);
if (Math.abs (m - modelIndex) < 0.001) modelIndex = m;
 else if (Math.abs (m - modelIndex) > 0.999) modelIndex = m = m + 1;
var f = modelIndex - m;
m -= 1;
if (f == 0) {
this.currentMorphModel = m;
this.setModel (m, true);
return;
}var m1;
this.setModel (m, true);
m1 = m + 1;
this.currentMorphModel = m + f;
if (m1 == m || m1 < 0 || m < 0) return;
this.viewer.modelSet.morphTrajectories (m, m1, f);
}, "~N");
$_M(c$, "setModel", 
function (modelIndex, clearBackgroundModel) {
if (modelIndex < 0) this.stopThread (false);
var formerModelIndex = this.currentModelIndex;
var modelSet = this.viewer.getModelSet ();
var modelCount = (modelSet == null ? 0 : modelSet.modelCount);
if (modelCount == 1) this.currentModelIndex = modelIndex = 0;
 else if (modelIndex < 0 || modelIndex >= modelCount) modelIndex = -1;
var ids = null;
var isSameSource = false;
if (this.currentModelIndex != modelIndex) {
if (modelCount > 0) {
var toDataModel = this.viewer.isJmolDataFrameForModel (modelIndex);
var fromDataModel = this.viewer.isJmolDataFrameForModel (this.currentModelIndex);
if (fromDataModel) this.viewer.setJmolDataFrame (null, -1, this.currentModelIndex);
if (this.currentModelIndex != -1) this.viewer.saveModelOrientation ();
if (fromDataModel || toDataModel) {
ids = this.viewer.getJmolFrameType (modelIndex) + " " + modelIndex + " <-- " + " " + this.currentModelIndex + " " + this.viewer.getJmolFrameType (this.currentModelIndex);
isSameSource = (this.viewer.getJmolDataSourceFrame (modelIndex) == this.viewer.getJmolDataSourceFrame (this.currentModelIndex));
}}this.currentModelIndex = modelIndex;
if (ids != null) {
if (modelIndex >= 0) this.viewer.restoreModelOrientation (modelIndex);
if (isSameSource && (ids.indexOf ("quaternion") >= 0 || ids.indexOf ("plot") < 0 && ids.indexOf ("ramachandran") < 0 && ids.indexOf (" property ") < 0)) {
this.viewer.restoreModelRotation (formerModelIndex);
}}}this.setViewer (clearBackgroundModel);
}, "~N,~B");
$_M(c$, "setBackgroundModelIndex", 
function (modelIndex) {
var modelSet = this.viewer.getModelSet ();
if (modelSet == null || modelIndex < 0 || modelIndex >= modelSet.modelCount) modelIndex = -1;
this.backgroundModelIndex = modelIndex;
if (modelIndex >= 0) this.viewer.setTrajectory (modelIndex);
this.viewer.setTainted (true);
this.setFrameRangeVisible ();
}, "~N");
$_M(c$, "initializePointers", 
function (frameStep) {
this.firstFrameIndex = 0;
this.lastFrameIndex = (frameStep == 0 ? 0 : this.getFrameCount ()) - 1;
this.frameStep = frameStep;
this.viewer.setFrameVariables ();
}, "~N");
$_M(c$, "setAnimationDirection", 
function (animationDirection) {
this.animationDirection = animationDirection;
}, "~N");
$_M(c$, "setAnimationFps", 
function (animationFps) {
this.animationFps = animationFps;
}, "~N");
$_M(c$, "setAnimationReplayMode", 
function (animationReplayMode, firstFrameDelay, lastFrameDelay) {
this.firstFrameDelay = firstFrameDelay > 0 ? firstFrameDelay : 0;
this.firstFrameDelayMs = Clazz.floatToInt (this.firstFrameDelay * 1000);
this.lastFrameDelay = lastFrameDelay > 0 ? lastFrameDelay : 0;
this.lastFrameDelayMs = Clazz.floatToInt (this.lastFrameDelay * 1000);
this.animationReplayMode = animationReplayMode;
this.viewer.setFrameVariables ();
}, "J.constant.EnumAnimationMode,~N,~N");
$_M(c$, "setAnimationRange", 
function (framePointer, framePointer2) {
var frameCount = this.getFrameCount ();
if (framePointer < 0) framePointer = 0;
if (framePointer2 < 0) framePointer2 = frameCount;
if (framePointer >= frameCount) framePointer = frameCount - 1;
if (framePointer2 >= frameCount) framePointer2 = frameCount - 1;
this.firstFrameIndex = framePointer;
this.currentMorphModel = this.firstFrameIndex;
this.lastFrameIndex = framePointer2;
this.frameStep = (framePointer2 < framePointer ? -1 : 1);
this.rewindAnimation ();
}, "~N,~N");
$_M(c$, "pauseAnimation", 
function () {
this.stopThread (true);
});
$_M(c$, "reverseAnimation", 
function () {
this.currentDirection = -this.currentDirection;
if (!this.animationOn) this.resumeAnimation ();
});
$_M(c$, "repaintDone", 
function () {
this.lastModelPainted = this.currentModelIndex;
this.lastFramePainted = this.currentAnimationFrame;
});
$_M(c$, "setModulationPlay", 
function (modT1, modT2) {
if (modT1 == 2147483647 || !this.viewer.haveModelSet () || this.viewer.isHeadless ()) {
this.stopThread (false);
return;
}if (this.modulationThread == null) {
this.modulationPlay = true;
this.modulationThread = J.api.Interface.getOptionInterface ("thread.ModulationThread");
this.modulationThread.setManager (this, this.viewer, [modT1, modT2]);
this.modulationThread.start ();
}}, "~N,~N");
$_M(c$, "resumeAnimation", 
function () {
if (this.currentModelIndex < 0) this.setAnimationRange (this.firstFrameIndex, this.lastFrameIndex);
if (this.getFrameCount () <= 1) {
this.animation (false);
return;
}this.animation (true);
this.animationPaused = false;
if (this.animationThread == null) {
this.intAnimThread++;
this.animationThread = J.api.Interface.getOptionInterface ("thread.AnimationThread");
this.animationThread.setManager (this, this.viewer, [this.firstFrameIndex, this.lastFrameIndex, this.intAnimThread]);
this.animationThread.start ();
}});
$_M(c$, "setAnimationLast", 
function () {
this.setFrame (this.animationDirection > 0 ? this.lastFrameIndex : this.firstFrameIndex);
});
$_M(c$, "rewindAnimation", 
function () {
this.setFrame (this.animationDirection > 0 ? this.firstFrameIndex : this.lastFrameIndex);
this.currentDirection = 1;
this.viewer.setFrameVariables ();
});
$_M(c$, "setAnimationPrevious", 
function () {
return this.setAnimationRelative (-this.animationDirection);
});
$_M(c$, "getAnimRunTimeSeconds", 
function () {
var frameCount = this.getFrameCount ();
if (this.firstFrameIndex == this.lastFrameIndex || this.lastFrameIndex < 0 || this.firstFrameIndex < 0 || this.lastFrameIndex >= frameCount || this.firstFrameIndex >= frameCount) return 0;
var i0 = Math.min (this.firstFrameIndex, this.lastFrameIndex);
var i1 = Math.max (this.firstFrameIndex, this.lastFrameIndex);
var nsec = 1 * (i1 - i0) / this.animationFps + this.firstFrameDelay + this.lastFrameDelay;
for (var i = i0; i <= i1; i++) nsec += this.viewer.getFrameDelayMs (this.modelIndexForFrame (i)) / 1000;

return nsec;
});
$_M(c$, "setMovie", 
function (info) {
this.isMovie = (info != null && info.get ("scripts") == null);
if (this.isMovie) {
this.animationFrames = info.get ("frames");
if (this.animationFrames == null || this.animationFrames.length == 0) {
this.isMovie = false;
} else {
this.currentAnimationFrame = (info.get ("currentFrame")).intValue ();
if (this.currentAnimationFrame < 0 || this.currentAnimationFrame >= this.animationFrames.length) this.currentAnimationFrame = 0;
}this.setFrame (this.currentAnimationFrame);
}if (!this.isMovie) {
this.animationFrames = null;
}this.viewer.setBooleanProperty ("_ismovie", this.isMovie);
this.bsDisplay = null;
this.currentMorphModel = this.morphCount = 0;
}, "java.util.Map");
$_M(c$, "getAnimationFrames", 
function () {
return this.animationFrames;
});
$_M(c$, "getCurrentFrameIndex", 
function () {
return this.currentAnimationFrame;
});
$_M(c$, "modelIndexForFrame", 
function (i) {
return (this.isMovie ? this.animationFrames[i] - 1 : i);
}, "~N");
$_M(c$, "getFrameCount", 
function () {
return (this.isMovie ? this.animationFrames.length : this.viewer.getModelCount ());
});
$_M(c$, "setFrame", 
function (i) {
try {
if (this.isMovie) {
var iModel = this.modelIndexForFrame (i);
this.currentAnimationFrame = i;
i = iModel;
} else {
this.currentAnimationFrame = i;
}this.setModel (i, true);
} catch (e) {
if (Clazz.exceptionOf (e, Exception)) {
} else {
throw e;
}
}
}, "~N");
$_M(c$, "setModulationFps", 
function (fps) {
if (fps > 0) this.modulationFps = fps;
 else this.stopModulationThread ();
}, "~N");
$_M(c$, "setViewer", 
($fz = function (clearBackgroundModel) {
this.viewer.setTrajectory (this.currentModelIndex);
this.viewer.setFrameOffset (this.currentModelIndex);
if (this.currentModelIndex == -1 && clearBackgroundModel) this.setBackgroundModelIndex (-1);
this.viewer.setTainted (true);
this.setFrameRangeVisible ();
this.viewer.setStatusFrameChanged (false);
if (this.viewer.modelSet != null && !this.viewer.global.selectAllModels) this.viewer.setSelectionSubset (this.viewer.getModelUndeletedAtomsBitSet (this.currentModelIndex));
}, $fz.isPrivate = true, $fz), "~B");
$_M(c$, "setFrameRangeVisible", 
($fz = function () {
this.bsVisibleModels.clearAll ();
if (this.backgroundModelIndex >= 0) this.bsVisibleModels.set (this.backgroundModelIndex);
if (this.currentModelIndex >= 0) {
this.bsVisibleModels.set (this.currentModelIndex);
return;
}if (this.frameStep == 0) return;
var nDisplayed = 0;
var frameDisplayed = 0;
for (var iframe = this.firstFrameIndex; iframe != this.lastFrameIndex; iframe += this.frameStep) {
var i = this.modelIndexForFrame (iframe);
if (!this.viewer.isJmolDataFrameForModel (i)) {
this.bsVisibleModels.set (i);
nDisplayed++;
frameDisplayed = iframe;
}}
var i = this.modelIndexForFrame (this.lastFrameIndex);
if (this.firstFrameIndex == this.lastFrameIndex || !this.viewer.isJmolDataFrameForModel (i) || nDisplayed == 0) {
this.bsVisibleModels.set (i);
if (nDisplayed == 0) this.firstFrameIndex = this.lastFrameIndex;
nDisplayed = 0;
}if (nDisplayed == 1 && this.currentModelIndex < 0) this.setFrame (frameDisplayed);
}, $fz.isPrivate = true, $fz));
$_M(c$, "animation", 
($fz = function (TF) {
this.animationOn = TF;
this.viewer.setBooleanProperty ("_animating", TF);
}, $fz.isPrivate = true, $fz), "~B");
$_M(c$, "setAnimationRelative", 
($fz = function (direction) {
var frameStep = this.getFrameStep (direction);
var thisFrame = (this.isMovie ? this.currentAnimationFrame : this.currentModelIndex);
var frameNext = thisFrame + frameStep;
var morphStep = 0;
var nextMorphFrame = 0;
var isDone;
if (this.morphCount > 0) {
morphStep = 1 / (this.morphCount + 1);
nextMorphFrame = this.currentMorphModel + frameStep * morphStep;
isDone = this.isNotInRange (nextMorphFrame);
} else {
isDone = this.isNotInRange (frameNext);
}if (isDone) {
switch (this.animationReplayMode) {
case J.constant.EnumAnimationMode.ONCE:
return false;
case J.constant.EnumAnimationMode.LOOP:
nextMorphFrame = frameNext = (this.animationDirection == this.currentDirection ? this.firstFrameIndex : this.lastFrameIndex);
break;
case J.constant.EnumAnimationMode.PALINDROME:
this.currentDirection = -this.currentDirection;
frameNext -= 2 * frameStep;
nextMorphFrame -= 2 * frameStep * morphStep;
}
}if (this.morphCount < 1) {
if (frameNext < 0 || frameNext >= this.getFrameCount ()) return false;
this.setFrame (frameNext);
return true;
}this.morph (nextMorphFrame + 1);
return true;
}, $fz.isPrivate = true, $fz), "~N");
$_M(c$, "isNotInRange", 
($fz = function (frameNext) {
var f = frameNext - 0.001;
return (f > this.firstFrameIndex && f > this.lastFrameIndex || (f = frameNext + 0.001) < this.firstFrameIndex && f < this.lastFrameIndex);
}, $fz.isPrivate = true, $fz), "~N");
$_M(c$, "getFrameStep", 
($fz = function (direction) {
return this.frameStep * direction * this.currentDirection;
}, $fz.isPrivate = true, $fz), "~N");
$_M(c$, "stopModulationThread", 
function () {
if (this.modulationThread != null) {
this.modulationThread.interrupt ();
this.modulationThread = null;
}this.modulationPlay = false;
});
Clazz.defineStatics (c$,
"FRAME_FIRST", -1,
"FRAME_LAST", 1,
"MODEL_CURRENT", 0);
});
