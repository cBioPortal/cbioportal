Clazz.declarePackage ("J.viewer");
Clazz.load (["J.constant.EnumAnimationMode", "J.util.BS"], "J.viewer.AnimationManager", ["J.thread.AnimationThread", "J.util.BSUtil"], function () {
c$ = Clazz.decorateAsClass (function () {
this.viewer = null;
this.animationReplayMode = null;
this.animationOn = false;
this.animationPaused = false;
this.inMotion = false;
this.animationFps = 0;
this.animationDirection = 1;
this.currentDirection = 1;
this.currentModelIndex = 0;
this.currentFrameIndex = 0;
this.firstFrameIndex = 0;
this.lastFrameIndex = 0;
this.frameStep = 0;
this.currentMorphFrame = 0;
this.morphCount = 0;
this.firstFrameDelayMs = 0;
this.lastFrameDelayMs = 0;
this.lastFramePainted = 0;
this.animationThread = null;
this.backgroundModelIndex = -1;
this.bsVisibleFrames = null;
this.bsDisplay = null;
this.firstFrameDelay = 0;
this.intAnimThread = 0;
this.lastFrameDelay = 1;
this.movie = null;
Clazz.instantialize (this, arguments);
}, J.viewer, "AnimationManager");
Clazz.prepareFields (c$, function () {
this.animationReplayMode = J.constant.EnumAnimationMode.ONCE;
this.bsVisibleFrames =  new J.util.BS ();
});
Clazz.makeConstructor (c$, 
function (viewer) {
this.viewer = viewer;
}, "J.viewer.Viewer");
$_M(c$, "clear", 
function () {
this.setMovie (null);
this.initializePointers (0);
this.setAnimationOn (false);
this.setCurrentModelIndex (0, true);
this.currentDirection = 1;
this.setAnimationDirection (1);
this.setAnimationFps (10);
this.setAnimationReplayMode (J.constant.EnumAnimationMode.ONCE, 0, 0);
this.initializePointers (0);
});
$_M(c$, "setFrame", 
($fz = function (frameIndex) {
this.setCurrentFrame (frameIndex, true);
}, $fz.isPrivate = true, $fz), "~N");
$_M(c$, "setCurrentFrame", 
($fz = function (frameIndex, isAll) {
if (this.movie == null) {
this.setCurrentModelIndex (frameIndex, true);
return;
}if (frameIndex == -1) frameIndex = (this.movie.get ("currentFrame")).intValue ();
this.currentFrameIndex = frameIndex;
var iState = this.getMovieState (frameIndex);
if (iState < 0) return;
this.setModelIndex (iState, true);
var states = this.movie.get ("states");
if (states == null || iState < 0 || iState >= states.size ()) return;
var bs = states.get (iState);
if (this.bsDisplay != null) {
bs = J.util.BSUtil.copy (bs);
bs.and (this.bsDisplay);
}this.viewer.displayAtoms (bs, true, false, null, true);
}, $fz.isPrivate = true, $fz), "~N,~B");
$_M(c$, "getMovieState", 
($fz = function (frameIndex) {
var frames = this.movie.get ("frames");
return (frames == null || frameIndex >= frames.size () ? -1 : (frames.get (frameIndex)).intValue ());
}, $fz.isPrivate = true, $fz), "~N");
$_M(c$, "morph", 
function (frame) {
System.out.println ("morph " + frame);
var m = Clazz.floatToInt (frame);
if (Math.abs (m - frame) < 0.001) frame = m;
 else if (Math.abs (m - frame) > 0.999) frame = m = m + 1;
var f = frame - m;
m -= 1;
if (f == 0) {
this.currentMorphFrame = m;
this.setFrame (m);
return;
}var m1;
if (this.movie == null) {
this.setCurrentModelIndex (m, true);
m1 = m + 1;
this.currentMorphFrame = m + f;
} else {
this.setCurrentFrame (m, false);
this.currentMorphFrame = m + f;
m = this.getMovieState (m);
m1 = this.getMovieState (this.getFrameStep (this.animationDirection) + this.getCurrentFrame ());
}if (m1 == m || m1 < 0 || m < 0) return;
this.viewer.modelSet.morphTrajectories (m, m1, f);
}, "~N");
$_M(c$, "setCurrentModelIndex", 
function (modelIndex, clearBackgroundModel) {
if (this.movie != null) {
this.setFrame (modelIndex);
return;
}this.currentFrameIndex = 0;
this.setModelIndex (modelIndex, clearBackgroundModel);
}, "~N,~B");
$_M(c$, "setModelIndex", 
($fz = function (modelIndex, clearBackgroundModel) {
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
var toDataFrame = this.isJmolDataFrameForModel (modelIndex);
var fromDataFrame = this.isJmolDataFrameForModel (this.currentModelIndex);
if (fromDataFrame) this.viewer.setJmolDataFrame (null, -1, this.currentModelIndex);
if (this.currentModelIndex != -1) this.viewer.saveModelOrientation ();
if (fromDataFrame || toDataFrame) {
ids = this.viewer.getJmolFrameType (modelIndex) + " " + modelIndex + " <-- " + " " + this.currentModelIndex + " " + this.viewer.getJmolFrameType (this.currentModelIndex);
isSameSource = (this.viewer.getJmolDataSourceFrame (modelIndex) == this.viewer.getJmolDataSourceFrame (this.currentModelIndex));
}}this.currentModelIndex = modelIndex;
if (ids != null) {
if (modelIndex >= 0) this.viewer.restoreModelOrientation (modelIndex);
if (isSameSource && ids.indexOf ("quaternion") >= 0 && ids.indexOf ("plot") < 0 && ids.indexOf ("ramachandran") < 0 && ids.indexOf (" property ") < 0) {
this.viewer.restoreModelRotation (formerModelIndex);
}}}this.setViewer (clearBackgroundModel);
}, $fz.isPrivate = true, $fz), "~N,~B");
$_M(c$, "setViewer", 
($fz = function (clearBackgroundModel) {
this.viewer.setTrajectory (this.currentModelIndex);
this.viewer.setFrameOffset (this.currentModelIndex);
if (this.currentModelIndex == -1 && clearBackgroundModel) this.setBackgroundModelIndex (-1);
this.viewer.setTainted (true);
this.setFrameRangeVisible ();
this.viewer.setStatusFrameChanged (false);
if (this.viewer.modelSet != null && !this.viewer.getSelectAllModels ()) this.viewer.setSelectionSubset (this.viewer.getModelUndeletedAtomsBitSet (this.currentModelIndex));
}, $fz.isPrivate = true, $fz), "~B");
$_M(c$, "isJmolDataFrameForModel", 
($fz = function (i) {
return this.movie == null && this.viewer.isJmolDataFrameForModel (i);
}, $fz.isPrivate = true, $fz), "~N");
$_M(c$, "setBackgroundModelIndex", 
function (modelIndex) {
var modelSet = this.viewer.getModelSet ();
if (modelSet == null || modelIndex < 0 || modelIndex >= modelSet.modelCount) modelIndex = -1;
this.backgroundModelIndex = modelIndex;
if (modelIndex >= 0) this.viewer.setTrajectory (modelIndex);
this.viewer.setTainted (true);
this.setFrameRangeVisible ();
}, "~N");
$_M(c$, "setFrameRangeVisible", 
($fz = function () {
this.bsVisibleFrames.clearAll ();
if (this.movie != null) {
this.bsVisibleFrames.setBits (0, this.viewer.getModelCount ());
return;
}if (this.backgroundModelIndex >= 0) this.bsVisibleFrames.set (this.backgroundModelIndex);
if (this.currentModelIndex >= 0) {
this.bsVisibleFrames.set (this.currentModelIndex);
return;
}if (this.frameStep == 0) return;
var nDisplayed = 0;
var frameDisplayed = 0;
for (var i = this.firstFrameIndex; i != this.lastFrameIndex; i += this.frameStep) if (!this.isJmolDataFrameForModel (i)) {
this.bsVisibleFrames.set (i);
nDisplayed++;
frameDisplayed = i;
}
if (this.firstFrameIndex == this.lastFrameIndex || !this.isJmolDataFrameForModel (this.lastFrameIndex) || nDisplayed == 0) {
this.bsVisibleFrames.set (this.lastFrameIndex);
if (nDisplayed == 0) this.firstFrameIndex = this.lastFrameIndex;
nDisplayed = 0;
}if (nDisplayed == 1 && this.currentModelIndex < 0) this.setFrame (frameDisplayed);
}, $fz.isPrivate = true, $fz));
$_M(c$, "initializePointers", 
function (frameStep) {
this.firstFrameIndex = 0;
this.lastFrameIndex = (frameStep == 0 ? 0 : this.getFrameCount ()) - 1;
this.frameStep = frameStep;
this.viewer.setFrameVariables ();
}, "~N");
$_M(c$, "getFrameCount", 
function () {
return (this.movie == null ? this.viewer.getModelCount () : (this.movie.get ("frameCount")).intValue ());
});
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
this.lastFrameIndex = framePointer2;
this.frameStep = (framePointer2 < framePointer ? -1 : 1);
this.rewindAnimation ();
}, "~N,~N");
$_M(c$, "animation", 
($fz = function (TF) {
this.animationOn = TF;
this.viewer.setBooleanProperty ("_animating", TF);
}, $fz.isPrivate = true, $fz), "~B");
$_M(c$, "setAnimationOn", 
function (animationOn) {
if (!animationOn || !this.viewer.haveModelSet () || this.viewer.isHeadless ()) {
this.stopThread (false);
return;
}if (!this.viewer.getSpinOn ()) this.viewer.refresh (3, "Viewer:setAnimationOn");
this.setAnimationRange (-1, -1);
this.resumeAnimation ();
}, "~B");
$_M(c$, "stopThread", 
function (isPaused) {
if (this.animationThread != null) {
this.animationThread.interrupt ();
this.animationThread = null;
}this.animationPaused = isPaused;
if (!this.viewer.getSpinOn ()) this.viewer.refresh (3, "Viewer:setAnimationOff");
this.animation (false);
this.viewer.setStatusFrameChanged (false);
}, "~B");
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
this.lastFramePainted = this.getCurrentFrame ();
});
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
this.animationThread =  new J.thread.AnimationThread (this, this.viewer, this.firstFrameIndex, this.lastFrameIndex, this.intAnimThread);
this.animationThread.start ();
}});
$_M(c$, "setAnimationNext", 
function () {
return this.setAnimationRelative (this.animationDirection);
});
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
$_M(c$, "setAnimationRelative", 
($fz = function (direction) {
var frameStep = this.getFrameStep (direction);
var thisFrame = this.getCurrentFrame ();
var frameNext = thisFrame + frameStep;
var morphStep = 0;
var nextMorphFrame = 0;
var isDone;
if (this.morphCount > 0) {
morphStep = 1 / (this.morphCount + 1);
nextMorphFrame = this.currentMorphFrame + frameStep * morphStep;
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
$_M(c$, "getAnimRunTimeSeconds", 
function () {
var frameCount = this.getFrameCount ();
if (this.firstFrameIndex == this.lastFrameIndex || this.lastFrameIndex < 0 || this.firstFrameIndex < 0 || this.lastFrameIndex >= frameCount || this.firstFrameIndex >= frameCount) return 0;
var i0 = Math.min (this.firstFrameIndex, this.lastFrameIndex);
var i1 = Math.max (this.firstFrameIndex, this.lastFrameIndex);
var nsec = 1 * (i1 - i0) / this.animationFps + this.firstFrameDelay + this.lastFrameDelay;
for (var i = i0; i <= i1; i++) nsec += this.viewer.getFrameDelayMs (i) / 1000;

return nsec;
});
$_M(c$, "setMovie", 
function (info) {
this.movie = info;
if (this.movie == null) {
this.bsDisplay = null;
this.currentMorphFrame = this.morphCount = 0;
} else {
this.setFrame (-1);
}}, "java.util.Map");
$_M(c$, "getCurrentFrame", 
function () {
return (this.movie == null ? this.currentModelIndex : this.currentFrameIndex);
});
$_M(c$, "isMovie", 
function () {
return (this.movie != null);
});
$_M(c$, "currentIsLast", 
function () {
return this.lastFramePainted == this.getCurrentFrame ();
});
$_M(c$, "getModelNumber", 
function (i) {
switch (i) {
case -1:
i = this.firstFrameIndex;
break;
case 0:
if (this.morphCount > 0) return "-" + (1 + this.currentMorphFrame);
i = this.getCurrentFrame ();
break;
case 1:
i = this.lastFrameIndex;
break;
}
return (this.movie == null ? this.viewer.getModelNumberDotted (i) : "" + (i + 1));
}, "~N");
$_M(c$, "setDisplay", 
function (bs) {
this.bsDisplay = (bs == null || bs.cardinality () == 0 ? null : J.util.BSUtil.copy (bs));
}, "J.util.BS");
$_M(c$, "currentFrameIs", 
function (f) {
var i = this.getCurrentFrame ();
return (this.morphCount == 0 ? i == f : Math.abs (this.currentMorphFrame - f) < 0.001);
}, "~N");
});
