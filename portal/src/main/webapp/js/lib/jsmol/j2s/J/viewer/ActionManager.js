Clazz.declarePackage ("J.viewer");
Clazz.load (["J.util.Rectangle", "J.viewer.MouseState"], "J.viewer.ActionManager", ["java.lang.Character", "$.Float", "java.util.Hashtable", "J.i18n.GT", "J.modelset.MeasurementPending", "J.thread.HoverWatcherThread", "J.util.BSUtil", "$.Escape", "$.JmolList", "$.Logger", "$.P3", "$.Point3fi", "$.TextFormat", "J.viewer.binding.Binding", "$.JmolBinding"], function () {
c$ = Clazz.decorateAsClass (function () {
this.viewer = null;
this.haveMultiTouchInput = false;
this.binding = null;
this.jmolBinding = null;
this.pfaatBinding = null;
this.dragBinding = null;
this.rasmolBinding = null;
this.predragBinding = null;
this.LEFT_CLICKED = 0;
this.LEFT_DRAGGED = 0;
this.hoverWatcherThread = null;
if (!Clazz.isClassDefined ("J.viewer.ActionManager.MotionPoint")) {
J.viewer.ActionManager.$ActionManager$MotionPoint$ ();
}
this.dragGesture = null;
if (!Clazz.isClassDefined ("J.viewer.ActionManager.Gesture")) {
J.viewer.ActionManager.$ActionManager$Gesture$ ();
}
this.atomPickingMode = 1;
this.bondPickingMode = 0;
this.pickingStyle = 0;
this.pickingStyleSelect = 0;
this.pickingStyleMeasure = 5;
this.rootPickingStyle = 0;
this.pickAtomAssignType = "C";
this.pickBondAssignType = 'p';
this.isPickAtomAssignCharge = false;
this.xyRange = 0;
this.gestureSwipeFactor = 1.0;
this.mouseDragFactor = 1.0;
this.mouseWheelFactor = 1.15;
this.current = null;
this.moved = null;
this.clicked = null;
this.pressed = null;
this.dragged = null;
this.pressedCount = 0;
this.clickedCount = 0;
this.drawMode = false;
this.labelMode = false;
this.dragSelectedMode = false;
this.measuresEnabled = true;
this.haveSelection = false;
this.hoverActive = false;
this.measurementPending = null;
this.dragAtomIndex = -1;
this.rubberbandSelectionMode = false;
this.rectRubber = null;
this.isAltKeyReleased = true;
this.keyProcessing = false;
this.isMultiTouchClient = false;
this.isMultiTouchServer = false;
this.pressAction = 0;
this.dragAction = 0;
this.clickAction = 0;
this.measurementQueued = null;
this.selectionWorking = false;
Clazz.instantialize (this, arguments);
}, J.viewer, "ActionManager");
Clazz.prepareFields (c$, function () {
this.dragGesture = Clazz.innerTypeInstance (J.viewer.ActionManager.Gesture, this, null, 20);
this.current =  new J.viewer.MouseState ("current");
this.moved =  new J.viewer.MouseState ("moved");
this.clicked =  new J.viewer.MouseState ("clicked");
this.pressed =  new J.viewer.MouseState ("pressed");
this.dragged =  new J.viewer.MouseState ("dragged");
this.rectRubber =  new J.util.Rectangle ();
});
$_M(c$, "setViewer", 
function (viewer, commandOptions) {
this.viewer = viewer;
this.setBinding (this.jmolBinding =  new J.viewer.binding.JmolBinding ("toggle"));
this.LEFT_CLICKED = J.viewer.binding.Binding.getMouseAction (1, 16, 2);
this.LEFT_DRAGGED = J.viewer.binding.Binding.getMouseAction (1, 16, 1);
}, "J.viewer.Viewer,~S");
$_M(c$, "checkHover", 
function () {
if (!this.viewer.getInMotion (true) && !this.viewer.getSpinOn () && !this.viewer.getNavOn () && !this.viewer.checkObjectHovered (this.current.x, this.current.y)) {
var atomIndex = this.viewer.findNearestAtomIndex (this.current.x, this.current.y);
if (atomIndex < 0) return;
var isLabel = (this.getAtomPickingMode () == 2 && this.isBound (J.viewer.binding.Binding.getMouseAction (this.clickedCount, this.moved.modifiers, 1), 10));
this.viewer.hoverOn (atomIndex, isLabel);
}});
$_M(c$, "processMultitouchEvent", 
function (groupID, eventType, touchID, iData, pt, time) {
}, "~N,~N,~N,~N,J.util.P3,~N");
$_M(c$, "isBound", 
function (mouseAction, jmolAction) {
return this.binding.isBound (mouseAction, jmolAction);
}, "~N,~N");
$_M(c$, "bindAction", 
function (desc, name) {
var jmolAction = J.viewer.ActionManager.getActionFromName (name);
var mouseAction = J.viewer.binding.Binding.getMouseActionStr (desc);
if (mouseAction == 0) return;
if (jmolAction >= 0) {
this.binding.bindAction (mouseAction, jmolAction);
} else {
this.binding.bindName (mouseAction, name);
}}, "~S,~S");
$_M(c$, "clearBindings", 
function () {
this.setBinding (this.jmolBinding =  new J.viewer.binding.JmolBinding ("toggle"));
this.pfaatBinding = null;
this.dragBinding = null;
this.rasmolBinding = null;
});
$_M(c$, "unbindAction", 
function (desc, name) {
if (desc == null && name == null) {
this.clearBindings ();
return;
}var jmolAction = J.viewer.ActionManager.getActionFromName (name);
var mouseAction = J.viewer.binding.Binding.getMouseActionStr (desc);
if (jmolAction >= 0) this.binding.unbindAction (mouseAction, jmolAction);
 else if (mouseAction != 0) this.binding.unbindName (mouseAction, name);
if (name == null) this.binding.unbindUserAction (desc);
}, "~S,~S");
c$.newAction = $_M(c$, "newAction", 
function (i, name, info) {
J.viewer.ActionManager.actionInfo[i] = info;
J.viewer.ActionManager.actionNames[i] = name;
}, "~N,~S,~S");
c$.getActionName = $_M(c$, "getActionName", 
function (i) {
return (i < J.viewer.ActionManager.actionNames.length ? J.viewer.ActionManager.actionNames[i] : null);
}, "~N");
c$.getActionFromName = $_M(c$, "getActionFromName", 
function (name) {
for (var i = 0; i < J.viewer.ActionManager.actionNames.length; i++) if (J.viewer.ActionManager.actionNames[i].equalsIgnoreCase (name)) return i;

return -1;
}, "~S");
$_M(c$, "getBindingInfo", 
function (qualifiers) {
return this.binding.getBindingInfo (J.viewer.ActionManager.actionInfo, J.viewer.ActionManager.actionNames, qualifiers);
}, "~S");
$_M(c$, "setBinding", 
function (newBinding) {
this.binding = newBinding;
}, "J.viewer.binding.Binding");
c$.getPickingModeName = $_M(c$, "getPickingModeName", 
function (pickingMode) {
return (pickingMode < 0 || pickingMode >= J.viewer.ActionManager.pickingModeNames.length ? "off" : J.viewer.ActionManager.pickingModeNames[pickingMode]);
}, "~N");
c$.getPickingMode = $_M(c$, "getPickingMode", 
function (str) {
for (var i = J.viewer.ActionManager.pickingModeNames.length; --i >= 0; ) if (str.equalsIgnoreCase (J.viewer.ActionManager.pickingModeNames[i])) return i;

return -1;
}, "~S");
$_M(c$, "getAtomPickingMode", 
function () {
return this.atomPickingMode;
});
$_M(c$, "setPickingMode", 
function (pickingMode) {
var isNew = false;
switch (pickingMode) {
case -1:
isNew = true;
this.bondPickingMode = 35;
pickingMode = 1;
break;
case 35:
case 34:
case 33:
this.viewer.setBooleanProperty ("bondPicking", true);
this.bondPickingMode = pickingMode;
return;
case 8:
this.bondPickingMode = pickingMode;
if (this.viewer.getBondPicking ()) return;
isNew = true;
break;
}
isNew = new Boolean (isNew | (this.atomPickingMode != pickingMode)).valueOf ();
this.atomPickingMode = pickingMode;
if (isNew) this.resetMeasurement ();
}, "~N");
$_M(c$, "setAtomPickingOption", 
function (option) {
switch (this.atomPickingMode) {
case 32:
this.pickAtomAssignType = option;
this.isPickAtomAssignCharge = (this.pickAtomAssignType.equals ("Pl") || this.pickAtomAssignType.equals ("Mi"));
break;
}
}, "~S");
$_M(c$, "setBondPickingOption", 
function (option) {
switch (this.bondPickingMode) {
case 33:
this.pickBondAssignType = Character.toLowerCase (option.charAt (0));
break;
}
}, "~S");
c$.getPickingStyleName = $_M(c$, "getPickingStyleName", 
function (pickingStyle) {
return (pickingStyle < 0 || pickingStyle >= J.viewer.ActionManager.pickingStyleNames.length ? "toggle" : J.viewer.ActionManager.pickingStyleNames[pickingStyle]);
}, "~N");
c$.getPickingStyleIndex = $_M(c$, "getPickingStyleIndex", 
function (str) {
for (var i = J.viewer.ActionManager.pickingStyleNames.length; --i >= 0; ) if (str.equalsIgnoreCase (J.viewer.ActionManager.pickingStyleNames[i])) return i;

return -1;
}, "~S");
$_M(c$, "getPickingState", 
function () {
var script = ";set modelkitMode " + this.viewer.getBoolean (603979883) + ";set picking " + J.viewer.ActionManager.getPickingModeName (this.atomPickingMode);
if (this.atomPickingMode == 32) script += "_" + this.pickAtomAssignType;
script += ";";
if (this.bondPickingMode != 0) script += "set picking " + J.viewer.ActionManager.getPickingModeName (this.bondPickingMode);
if (this.bondPickingMode == 33) script += "_" + this.pickBondAssignType;
script += ";";
return script;
});
$_M(c$, "getPickingStyle", 
function () {
return this.pickingStyle;
});
$_M(c$, "setPickingStyle", 
function (pickingStyle) {
this.pickingStyle = pickingStyle;
if (pickingStyle >= 4) {
this.pickingStyleMeasure = pickingStyle;
this.resetMeasurement ();
} else {
if (pickingStyle < 3) this.rootPickingStyle = pickingStyle;
this.pickingStyleSelect = pickingStyle;
}this.rubberbandSelectionMode = false;
switch (this.pickingStyleSelect) {
case 2:
if (!this.binding.getName ().equals ("extendedSelect")) this.setBinding (this.pfaatBinding == null ? this.pfaatBinding = J.viewer.binding.Binding.newBinding ("Pfaat") : this.pfaatBinding);
break;
case 3:
if (!this.binding.getName ().equals ("drag")) this.setBinding (this.dragBinding == null ? this.dragBinding = J.viewer.binding.Binding.newBinding ("Drag") : this.dragBinding);
this.rubberbandSelectionMode = true;
break;
case 1:
if (!this.binding.getName ().equals ("selectOrToggle")) this.setBinding (this.rasmolBinding == null ? this.rasmolBinding = J.viewer.binding.Binding.newBinding ("Rasmol") : this.rasmolBinding);
break;
default:
if (this.binding !== this.jmolBinding) this.setBinding (this.jmolBinding);
}
if (!this.binding.getName ().equals ("drag")) this.predragBinding = this.binding;
}, "~N");
$_M(c$, "getMouseInfo", 
function () {
var info =  new java.util.Hashtable ();
var vb =  new J.util.JmolList ();
for (var obj, $obj = this.binding.getBindings ().values ().iterator (); $obj.hasNext () && ((obj = $obj.next ()) || true);) {
if (Clazz.instanceOf (obj, Boolean)) continue;
if (J.util.Escape.isAI (obj)) {
var binding = obj;
obj = [J.viewer.binding.Binding.getMouseActionName (binding[0], false), J.viewer.ActionManager.getActionName (binding[1])];
}vb.addLast (obj);
}
info.put ("bindings", vb);
info.put ("bindingName", this.binding.getName ());
info.put ("actionNames", J.viewer.ActionManager.actionNames);
info.put ("actionInfo", J.viewer.ActionManager.actionInfo);
info.put ("bindingInfo", J.util.TextFormat.split (this.getBindingInfo (null), '\n'));
return info;
});
$_M(c$, "setGestureSwipeFactor", 
function (factor) {
this.gestureSwipeFactor = factor;
}, "~N");
$_M(c$, "setMouseDragFactor", 
function (factor) {
this.mouseDragFactor = factor;
}, "~N");
$_M(c$, "setMouseWheelFactor", 
function (factor) {
this.mouseWheelFactor = factor;
}, "~N");
$_M(c$, "setCurrent", 
function (time, x, y, mods) {
this.viewer.hoverOff ();
this.current.set (time, x, y, mods);
}, "~N,~N,~N,~N");
$_M(c$, "getCurrentX", 
function () {
return this.current.x;
});
$_M(c$, "getCurrentY", 
function () {
return this.current.y;
});
$_M(c$, "setMouseMode", 
function () {
this.drawMode = this.labelMode = false;
this.dragSelectedMode = this.viewer.getDragSelected ();
this.measuresEnabled = !this.dragSelectedMode;
if (!this.dragSelectedMode) switch (this.atomPickingMode) {
default:
return;
case 32:
this.measuresEnabled = !this.isPickAtomAssignCharge;
return;
case 4:
this.drawMode = true;
this.measuresEnabled = false;
break;
case 2:
this.labelMode = true;
this.measuresEnabled = false;
break;
case 9:
this.measuresEnabled = false;
break;
case 19:
case 22:
case 20:
case 21:
this.measuresEnabled = false;
return;
}
this.exitMeasurementMode ();
});
$_M(c$, "clearMouseInfo", 
function () {
this.pressedCount = this.clickedCount = 0;
this.dragGesture.setAction (0, 0);
this.exitMeasurementMode ();
});
$_M(c$, "isMTClient", 
function () {
return this.isMultiTouchClient;
});
$_M(c$, "isMTServer", 
function () {
return this.isMultiTouchServer;
});
$_M(c$, "dispose", 
function () {
this.clear ();
});
$_M(c$, "clear", 
function () {
this.startHoverWatcher (false);
if (this.predragBinding != null) this.binding = this.predragBinding;
this.viewer.setPickingMode (null, 1);
this.viewer.setPickingStyle (null, this.rootPickingStyle);
this.isAltKeyReleased = true;
});
$_M(c$, "startHoverWatcher", 
function (isStart) {
if (this.viewer.isPreviewOnly ()) return;
try {
if (isStart) {
if (this.hoverWatcherThread != null) return;
this.current.time = -1;
this.hoverWatcherThread =  new J.thread.HoverWatcherThread (this, this.current, this.moved, this.viewer);
} else {
if (this.hoverWatcherThread == null) return;
this.current.time = -1;
this.hoverWatcherThread.interrupt ();
this.hoverWatcherThread = null;
}} catch (e) {
if (Clazz.exceptionOf (e, Exception)) {
} else {
throw e;
}
}
}, "~B");
$_M(c$, "setModeMouse", 
function (modeMouse) {
if (modeMouse == -1) {
this.startHoverWatcher (false);
}}, "~N");
$_M(c$, "keyPressed", 
function (key, modifiers) {
if (this.keyProcessing) return;
this.viewer.hoverOff ();
this.keyProcessing = true;
switch (key) {
case 18:
if (this.dragSelectedMode && this.isAltKeyReleased) this.viewer.moveSelected (-2147483648, 0, -2147483648, -2147483648, -2147483648, null, false, false);
this.isAltKeyReleased = false;
this.moved.modifiers |= 8;
break;
case 16:
this.dragged.modifiers |= 1;
this.moved.modifiers |= 1;
break;
case 17:
this.moved.modifiers |= 2;
}
var action = 16 | 256 | 8192 | this.moved.modifiers;
if (!this.labelMode && !this.binding.isUserAction (action)) this.checkMotionRotateZoom (action, this.current.x, 0, 0, false);
if (this.viewer.getBoolean (603979887)) {
switch (key) {
case 38:
case 40:
case 37:
case 39:
case 32:
case 46:
this.viewer.navigate (key, modifiers);
break;
}
}this.keyProcessing = false;
}, "~N,~N");
$_M(c$, "keyReleased", 
function (key) {
switch (key) {
case 18:
if (this.dragSelectedMode) this.viewer.moveSelected (2147483647, 0, -2147483648, -2147483648, -2147483648, null, false, false);
this.isAltKeyReleased = true;
this.moved.modifiers &= -9;
break;
case 16:
this.moved.modifiers &= -2;
break;
case 17:
this.moved.modifiers &= -3;
}
if (this.moved.modifiers == 0) this.viewer.setCursor (0);
if (!this.viewer.getBoolean (603979887)) return;
switch (key) {
case 38:
case 40:
case 37:
case 39:
this.viewer.navigate (0, 0);
break;
}
}, "~N");
$_M(c$, "mouseEntered", 
function (time, x, y) {
this.setCurrent (time, x, y, 0);
}, "~N,~N,~N");
$_M(c$, "mouseExited", 
function (time, x, y) {
this.setCurrent (time, x, y, 0);
if (this.measurementPending != null) {
this.exitMeasurementMode ();
this.viewer.refresh (3, "mouseExit");
}}, "~N,~N,~N");
$_M(c$, "setMouseActions", 
($fz = function (count, buttonMods, isRelease) {
this.pressAction = J.viewer.binding.Binding.getMouseAction (count, buttonMods, isRelease ? 5 : 4);
this.dragAction = J.viewer.binding.Binding.getMouseAction (count, buttonMods, 1);
this.clickAction = J.viewer.binding.Binding.getMouseAction (count, buttonMods, 2);
}, $fz.isPrivate = true, $fz), "~N,~N,~B");
$_M(c$, "mouseAction", 
function (mode, time, x, y, count, buttonMods) {
if (!this.viewer.getMouseEnabled ()) return;
switch (mode) {
case 0:
this.setCurrent (time, x, y, buttonMods);
this.moved.setCurrent (this.current, 0);
if (this.measurementPending != null || this.hoverActive) {
this.clickAction = J.viewer.binding.Binding.getMouseAction (this.clickedCount, buttonMods, 0);
this.checkClickAction (x, y, time, 0);
return;
}if (this.isZoomArea (x)) {
this.checkMotionRotateZoom (this.LEFT_DRAGGED, 0, 0, 0, false);
return;
}if (this.viewer.getCursor () == 5) this.viewer.setCursor (0);
return;
case 4:
this.setMouseMode ();
this.pressedCount = (this.pressed.check (0, 0, 0, buttonMods, time, 700) ? this.pressedCount + 1 : 1);
if (this.pressedCount == 1) {
this.viewer.checkInMotion (1);
this.setCurrent (time, x, y, buttonMods);
}this.pressAction = J.viewer.binding.Binding.getMouseAction (this.pressedCount, buttonMods, 4);
this.viewer.setCursor (1);
this.pressed.setCurrent (this.current, 1);
this.dragged.setCurrent (this.current, 1);
this.viewer.setFocus ();
this.dragGesture.setAction (this.dragAction, time);
this.checkPressedAction (x, y, time);
return;
case 1:
this.setMouseMode ();
this.setMouseActions (this.pressedCount, buttonMods, false);
var deltaX = x - this.dragged.x;
var deltaY = y - this.dragged.y;
this.setCurrent (time, x, y, buttonMods);
this.dragged.setCurrent (this.current, -1);
if (this.atomPickingMode != 32) this.exitMeasurementMode ();
this.dragGesture.add (this.dragAction, x, y, time);
this.checkDragWheelAction (this.dragAction, x, y, deltaX, deltaY, time, 1);
return;
case 5:
this.setMouseActions (this.pressedCount, buttonMods, true);
this.setCurrent (time, x, y, buttonMods);
this.viewer.spinXYBy (0, 0, 0);
var dragRelease = !this.pressed.check (this.xyRange, x, y, buttonMods, time, 9223372036854775807);
this.checkReleaseAction (x, y, time, dragRelease);
return;
case 2:
this.setMouseMode ();
this.clickedCount = (count > 1 ? count : this.clicked.check (0, 0, 0, buttonMods, time, 700) ? this.clickedCount + 1 : 1);
if (this.clickedCount == 1) {
this.setCurrent (time, x, y, buttonMods);
}this.setMouseActions (this.clickedCount, buttonMods, false);
this.clicked.setCurrent (this.current, this.clickedCount);
this.viewer.setFocus ();
if (this.atomPickingMode != 9 && this.isBound (J.viewer.binding.Binding.getMouseAction (1, buttonMods, 4), 31)) return;
this.clickAction = J.viewer.binding.Binding.getMouseAction (this.clickedCount, buttonMods, 2);
this.checkClickAction (x, y, time, this.clickedCount);
return;
case 3:
if (this.viewer.isApplet () && !this.viewer.hasFocus ()) return;
this.setCurrent (time, this.current.x, this.current.y, buttonMods);
this.checkDragWheelAction (J.viewer.binding.Binding.getMouseAction (0, buttonMods, 3), this.current.x, this.current.y, 0, y, time, 3);
return;
}
}, "~N,~N,~N,~N,~N,~N");
$_M(c$, "checkPressedAction", 
($fz = function (x, y, time) {
var buttonMods = J.viewer.binding.Binding.getButtonMods (this.pressAction);
var isSelectAndDrag = this.isBound (J.viewer.binding.Binding.getMouseAction (1, buttonMods, 4), 31);
if (buttonMods != 0) {
this.pressAction = this.viewer.notifyMouseClicked (x, y, this.pressAction, 4);
if (this.pressAction == 0) return;
buttonMods = J.viewer.binding.Binding.getButtonMods (this.pressAction);
}this.setMouseActions (this.pressedCount, buttonMods, false);
if (J.util.Logger.debugging) J.util.Logger.debug (J.viewer.binding.Binding.getMouseActionName (this.pressAction, false));
if (this.drawMode && (this.isBound (this.dragAction, 8) || this.isBound (this.dragAction, 9)) || this.labelMode && this.isBound (this.dragAction, 10)) {
this.viewer.checkObjectDragged (-2147483648, 0, x, y, this.dragAction);
return;
}this.checkUserAction (this.pressAction, x, y, 0, 0, time, 4);
var isBound = false;
switch (this.atomPickingMode) {
case 32:
isBound = this.isBound (this.clickAction, 0);
break;
case 28:
isBound = this.isBound (this.dragAction, 7) || this.isBound (this.dragAction, 14);
break;
case 26:
case 36:
case 27:
isBound = this.isBound (this.dragAction, 7) || this.isBound (this.dragAction, 27) || this.isBound (this.dragAction, 14);
break;
case 29:
isBound = this.isBound (this.dragAction, 11) || this.isBound (this.dragAction, 14);
break;
case 30:
isBound = this.isBound (this.dragAction, 12) || this.isBound (this.dragAction, 27) || this.isBound (this.dragAction, 14);
break;
}
if (isBound) {
this.dragAtomIndex = this.viewer.findNearestAtomIndexMovable (x, y, true);
if (this.dragAtomIndex >= 0 && (this.atomPickingMode == 32 || this.atomPickingMode == 31) && this.viewer.isAtomAssignable (this.dragAtomIndex)) {
this.enterMeasurementMode (this.dragAtomIndex);
this.measurementPending.addPoint (this.dragAtomIndex, null, false);
}return;
}if (this.isBound (this.pressAction, 23)) {
var type = 'j';
if (this.viewer.getBoolean (603979883)) {
var t = this.viewer.checkObjectClicked (x, y, this.LEFT_CLICKED);
type = (t != null && "bond".equals (t.get ("type")) ? 'b' : this.viewer.findNearestAtomIndex (x, y) >= 0 ? 'a' : 'm');
}this.viewer.popupMenu (x, y, type);
return;
}if (this.dragSelectedMode) {
this.haveSelection = true;
if (isSelectAndDrag) {
this.haveSelection = (this.viewer.findNearestAtomIndexMovable (x, y, true) >= 0);
}if (!this.haveSelection) return;
if (this.isBound (this.dragAction, 13) || this.isBound (this.dragAction, 14)) this.viewer.moveSelected (-2147483648, 0, -2147483648, -2147483648, -2147483648, null, false, false);
return;
}if (this.viewer.global.useArcBall) this.viewer.rotateArcBall (x, y, 0);
this.checkMotionRotateZoom (this.dragAction, x, 0, 0, true);
}, $fz.isPrivate = true, $fz), "~N,~N,~N");
$_M(c$, "checkDragWheelAction", 
($fz = function (dragWheelAction, x, y, deltaX, deltaY, time, mode) {
var buttonmods = J.viewer.binding.Binding.getButtonMods (dragWheelAction);
if (buttonmods != 0) {
var newAction = this.viewer.notifyMouseClicked (x, y, J.viewer.binding.Binding.getMouseAction (this.pressedCount, buttonmods, mode), mode);
if (newAction == 0) return;
if (newAction > 0) dragWheelAction = newAction;
}if (this.isRubberBandSelect (dragWheelAction)) {
this.calcRectRubberBand ();
this.viewer.refresh (3, "rubberBand selection");
return;
}if (this.checkUserAction (dragWheelAction, x, y, deltaX, deltaY, time, mode)) return;
if (this.viewer.getRotateBondIndex () >= 0) {
if (this.isBound (dragWheelAction, 26)) {
this.viewer.moveSelected (deltaX, deltaY, -2147483648, x, y, null, false, false);
return;
}if (!this.isBound (dragWheelAction, 25)) this.viewer.setRotateBondIndex (-1);
}var bs = null;
if (this.dragAtomIndex >= 0) {
switch (this.atomPickingMode) {
case 26:
this.setMotion (3, true);
if (this.isBound (dragWheelAction, 27) && this.viewer.getBoolean (603979785)) {
this.viewer.rotateSelected (this.getDegrees (deltaX, 0), this.getDegrees (deltaY, 1), null);
} else {
this.viewer.moveSelected (deltaX, deltaY, (this.isBound (dragWheelAction, 14) ? -deltaY : -2147483648), -2147483648, -2147483648, null, true, false);
}return;
case 36:
case 27:
case 30:
bs = this.viewer.getAtomBits (1095761934, J.util.BSUtil.newAndSetBit (this.dragAtomIndex));
if (this.atomPickingMode == 36) bs.and (this.viewer.getAtomBitSet ("ligand"));
case 28:
case 29:
if (this.dragGesture.getPointCount () == 1) this.viewer.undoMoveActionClear (this.dragAtomIndex, 2, true);
this.setMotion (3, true);
if (this.isBound (dragWheelAction, 27)) {
this.viewer.rotateSelected (this.getDegrees (deltaX, 0), this.getDegrees (deltaY, 1), bs);
} else {
switch (this.atomPickingMode) {
case 36:
case 27:
case 30:
this.viewer.select (bs, false, 0, true);
break;
}
this.viewer.moveAtomWithHydrogens (this.dragAtomIndex, deltaX, deltaY, (this.isBound (dragWheelAction, 14) ? -deltaY : -2147483648), bs);
}return;
}
}if (this.dragAtomIndex >= 0 && mode == 1 && this.isBound (this.clickAction, 0) && this.atomPickingMode == 32) {
var nearestAtomIndex = this.viewer.findNearestAtomIndexMovable (x, y, false);
if (nearestAtomIndex >= 0) {
if (this.measurementPending != null) {
this.measurementPending.setCount (1);
} else if (this.measuresEnabled) {
this.enterMeasurementMode (nearestAtomIndex);
}this.addToMeasurement (nearestAtomIndex, null, true);
this.measurementPending.colix = 20;
} else if (this.measurementPending != null) {
this.measurementPending.setCount (1);
this.measurementPending.colix = 23;
}if (this.measurementPending == null) return;
this.measurementPending.traceX = x;
this.measurementPending.traceY = y;
this.viewer.refresh (3, "assignNew");
return;
}if (!this.drawMode && !this.labelMode && this.isBound (dragWheelAction, 45)) {
this.viewer.translateXYBy (deltaX, deltaY);
return;
}if (this.dragSelectedMode && this.haveSelection && (this.isBound (dragWheelAction, 13) || this.isBound (dragWheelAction, 27))) {
var iatom = this.viewer.getSelectionSet (false).nextSetBit (0);
if (iatom < 0) return;
if (this.dragGesture.getPointCount () == 1) this.viewer.undoMoveActionClear (iatom, 2, true);
 else this.viewer.moveSelected (2147483647, 0, -2147483648, -2147483648, -2147483648, null, false, false);
this.setMotion (3, true);
if (this.isBound (dragWheelAction, 27) && this.viewer.getBoolean (603979785)) this.viewer.rotateSelected (this.getDegrees (deltaX, 0), this.getDegrees (deltaY, 1), null);
 else this.viewer.moveSelected (deltaX, deltaY, -2147483648, -2147483648, -2147483648, null, true, false);
return;
}if (this.drawMode && (this.isBound (dragWheelAction, 8) || this.isBound (dragWheelAction, 9)) || this.labelMode && this.isBound (dragWheelAction, 10)) {
this.setMotion (3, true);
this.viewer.checkObjectDragged (this.dragged.x, this.dragged.y, x, y, dragWheelAction);
return;
}if (this.checkMotionRotateZoom (dragWheelAction, x, deltaX, deltaY, true)) {
if (this.viewer.getSlabEnabled () && this.checkSlideZoom (dragWheelAction)) this.viewer.slabDepthByPixels (deltaY);
 else this.viewer.zoomBy (deltaY);
return;
}if (this.isBound (dragWheelAction, 25)) {
var degX = this.getDegrees (deltaX, 0);
var degY = this.getDegrees (deltaY, 1);
if (this.viewer.global.useArcBall) this.viewer.rotateArcBall (x, y, this.mouseDragFactor);
 else this.viewer.rotateXYBy (degX, degY);
return;
}if (this.isBound (dragWheelAction, 29)) {
if (deltaX == 0 && Math.abs (deltaY) > 1) {
this.setMotion (5, true);
this.viewer.zoomBy (deltaY + (deltaY > 0 ? -1 : 1));
} else if (deltaY == 0 && Math.abs (deltaX) > 1) {
this.setMotion (3, true);
this.viewer.rotateZBy (-deltaX + (deltaX > 0 ? 1 : -1), 2147483647, 2147483647);
}return;
} else if (this.isBound (dragWheelAction, 46)) {
this.zoomByFactor (deltaY, 2147483647, 2147483647);
return;
} else if (this.isBound (dragWheelAction, 28)) {
this.setMotion (3, true);
this.viewer.rotateZBy (-deltaX, 2147483647, 2147483647);
return;
}if (this.viewer.getSlabEnabled ()) {
if (this.isBound (dragWheelAction, 6)) {
this.viewer.depthByPixels (deltaY);
return;
}if (this.isBound (dragWheelAction, 38)) {
this.viewer.slabByPixels (deltaY);
return;
}if (this.isBound (dragWheelAction, 39)) {
this.viewer.slabDepthByPixels (deltaY);
return;
}}}, $fz.isPrivate = true, $fz), "~N,~N,~N,~N,~N,~N,~N");
$_M(c$, "checkReleaseAction", 
($fz = function (x, y, time, dragRelease) {
if (J.util.Logger.debugging) J.util.Logger.debug (J.viewer.binding.Binding.getMouseActionName (this.pressAction, false));
this.viewer.checkInMotion (0);
this.viewer.setInMotion (false);
this.viewer.setCursor (0);
this.dragGesture.add (this.dragAction, x, y, time);
if (dragRelease) this.viewer.setRotateBondIndex (-2147483648);
if (this.dragAtomIndex >= 0) {
if (this.atomPickingMode == 29 || this.atomPickingMode == 30) this.minimize (true);
}if (this.atomPickingMode == 32 && this.isBound (this.clickAction, 0)) {
if (this.measurementPending == null || this.dragAtomIndex < 0) return;
this.assignNew (x, y);
return;
}this.dragAtomIndex = -1;
var isRbAction = this.isRubberBandSelect (this.clickAction);
if (isRbAction) this.selectRb (this.clickAction);
this.rubberbandSelectionMode = (this.binding.getName ().equals ("drag"));
this.rectRubber.x = 2147483647;
if (dragRelease) {
this.viewer.notifyMouseClicked (x, y, J.viewer.binding.Binding.getMouseAction (this.pressedCount, 0, 5), 5);
}if (this.drawMode && (this.isBound (this.dragAction, 8) || this.isBound (this.dragAction, 9)) || this.labelMode && this.isBound (this.dragAction, 10)) {
this.viewer.checkObjectDragged (2147483647, 0, x, y, this.dragAction);
return;
}if (this.dragSelectedMode && this.isBound (this.dragAction, 13) && this.haveSelection) this.viewer.moveSelected (2147483647, 0, -2147483648, -2147483648, -2147483648, null, false, false);
if (dragRelease && this.checkUserAction (this.pressAction, x, y, 0, 0, time, 5)) return;
if (this.viewer.getBoolean (603979780)) {
if (this.isBound (this.dragAction, 44)) {
var speed = this.getExitRate ();
if (speed > 0) this.viewer.spinXYBy (this.dragGesture.getDX (4, 2), this.dragGesture.getDY (4, 2), speed * 30 * this.gestureSwipeFactor);
if (this.viewer.global.logGestures) this.viewer.log ("$NOW$ swipe " + this.dragGesture + " " + speed);
return;
}}}, $fz.isPrivate = true, $fz), "~N,~N,~N,~B");
$_M(c$, "checkClickAction", 
($fz = function (x, y, time, clickedCount) {
if (!this.viewer.haveModelSet ()) return;
if (clickedCount > 0) {
if (this.checkUserAction (this.clickAction, x, y, 0, 0, time, 32768)) return;
this.clickAction = this.viewer.notifyMouseClicked (x, y, this.clickAction, 32768);
if (this.clickAction == 0) return;
}if (J.util.Logger.debugging) J.util.Logger.debug (J.viewer.binding.Binding.getMouseActionName (this.clickAction, false));
if (this.isBound (this.clickAction, 2) && this.viewer.frankClicked (x, y)) {
this.viewer.popupMenu (-x, y, 'j');
return;
}if (this.isBound (this.clickAction, 2) && this.viewer.frankClickedModelKit (x, y)) {
this.viewer.popupMenu (0, 0, 'm');
return;
}var nearestPoint = null;
var isBond = false;
var isIsosurface = false;
var t = null;
if (!this.drawMode) {
t = this.viewer.checkObjectClicked (x, y, this.clickAction);
if (t != null) {
isBond = "bond".equals (t.get ("type"));
isIsosurface = "isosurface".equals (t.get ("type"));
nearestPoint = this.getPoint (t);
}}if (isBond) clickedCount = 1;
if (nearestPoint != null && Float.isNaN (nearestPoint.x)) return;
var nearestAtomIndex = this.findNearestAtom (x, y, nearestPoint, clickedCount > 0);
if (clickedCount == 0 && this.atomPickingMode != 32) {
if (this.measurementPending == null) return;
if (nearestPoint != null || this.measurementPending.getIndexOf (nearestAtomIndex) == 0) this.measurementPending.addPoint (nearestAtomIndex, nearestPoint, false);
if (this.measurementPending.haveModified ()) this.viewer.setPendingMeasurement (this.measurementPending);
this.viewer.refresh (3, "measurementPending");
return;
}this.setMouseMode ();
if (this.isBound (this.clickAction, 43)) {
this.viewer.stopMotion ();
}if (this.viewer.getBoolean (603979887) && this.atomPickingMode == 23 && this.isBound (this.clickAction, 21)) {
this.viewer.navTranslatePercent (x * 100 / this.viewer.getScreenWidth () - 50, y * 100 / this.viewer.getScreenHeight () - 50);
return;
}if (isBond) {
if (this.isBound (this.clickAction, this.bondPickingMode == 34 || this.bondPickingMode == 33 ? 0 : 5)) {
this.bondPicked ((t.get ("index")).intValue ());
return;
}} else if (isIsosurface) {
return;
} else {
if (this.atomPickingMode != 32 && this.measurementPending != null && this.isBound (this.clickAction, 20)) {
this.atomOrPointPicked (nearestAtomIndex, nearestPoint);
if (this.addToMeasurement (nearestAtomIndex, nearestPoint, false) == 4) this.toggleMeasurement ();
return;
}if (this.isBound (this.clickAction, 37)) {
if (this.measurementPending != null) {
this.addToMeasurement (nearestAtomIndex, nearestPoint, true);
this.toggleMeasurement ();
} else if (!this.drawMode && !this.labelMode && !this.dragSelectedMode && this.measuresEnabled) {
this.enterMeasurementMode (nearestAtomIndex);
this.addToMeasurement (nearestAtomIndex, nearestPoint, true);
}this.atomOrPointPicked (nearestAtomIndex, nearestPoint);
return;
}}if (this.isSelectAction (this.clickAction)) {
if (!isIsosurface) this.atomOrPointPicked (nearestAtomIndex, nearestPoint);
return;
}if (this.isBound (this.clickAction, 24)) {
if (nearestAtomIndex < 0) this.reset ();
return;
}}, $fz.isPrivate = true, $fz), "~N,~N,~N,~N");
$_M(c$, "checkUserAction", 
($fz = function (mouseAction, x, y, deltaX, deltaY, time, mode) {
if (!this.binding.isUserAction (mouseAction)) return false;
var passThrough = false;
var obj;
var ht = this.binding.getBindings ();
var mkey = mouseAction + "\t";
for (var key, $key = ht.keySet ().iterator (); $key.hasNext () && ((key = $key.next ()) || true);) {
if (key.indexOf (mkey) != 0 || !J.util.Escape.isAS (obj = ht.get (key))) continue;
var script = (obj)[1];
var nearestPoint = null;
if (script.indexOf ("_ATOM") >= 0) {
var iatom = this.findNearestAtom (x, y, null, true);
script = J.util.TextFormat.simpleReplace (script, "_ATOM", "({" + (iatom >= 0 ? "" + iatom : "") + "})");
if (iatom >= 0) script = J.util.TextFormat.simpleReplace (script, "_POINT", J.util.Escape.eP (this.viewer.getModelSet ().atoms[iatom]));
}if (!this.drawMode && (script.indexOf ("_POINT") >= 0 || script.indexOf ("_OBJECT") >= 0 || script.indexOf ("_BOND") >= 0)) {
var t = this.viewer.checkObjectClicked (x, y, mouseAction);
if (t != null && (nearestPoint = t.get ("pt")) != null) {
var isBond = t.get ("type").equals ("bond");
if (isBond) script = J.util.TextFormat.simpleReplace (script, "_BOND", "[{" + t.get ("index") + "}]");
script = J.util.TextFormat.simpleReplace (script, "_POINT", J.util.Escape.eP (nearestPoint));
script = J.util.TextFormat.simpleReplace (script, "_OBJECT", J.util.Escape.escapeMap (t));
}script = J.util.TextFormat.simpleReplace (script, "_BOND", "[{}]");
script = J.util.TextFormat.simpleReplace (script, "_OBJECT", "{}");
}script = J.util.TextFormat.simpleReplace (script, "_POINT", "{}");
script = J.util.TextFormat.simpleReplace (script, "_ACTION", "" + mouseAction);
script = J.util.TextFormat.simpleReplace (script, "_X", "" + x);
script = J.util.TextFormat.simpleReplace (script, "_Y", "" + (this.viewer.getScreenHeight () - y));
script = J.util.TextFormat.simpleReplace (script, "_DELTAX", "" + deltaX);
script = J.util.TextFormat.simpleReplace (script, "_DELTAY", "" + deltaY);
script = J.util.TextFormat.simpleReplace (script, "_TIME", "" + time);
script = J.util.TextFormat.simpleReplace (script, "_MODE", "" + mode);
if (script.startsWith ("+:")) {
passThrough = true;
script = script.substring (2);
}this.viewer.evalStringQuiet (script);
}
return !passThrough;
}, $fz.isPrivate = true, $fz), "~N,~N,~N,~N,~N,~N,~N");
$_M(c$, "checkMotionRotateZoom", 
($fz = function (mouseAction, x, deltaX, deltaY, isDrag) {
var isSlideZoom = this.checkSlideZoom (mouseAction);
var isRotateXY = this.isBound (mouseAction, 25);
var isRotateZorZoom = this.isBound (mouseAction, 29);
if (!isSlideZoom && !isRotateXY && !isRotateZorZoom) return false;
var isZoom = (isRotateZorZoom && (deltaX == 0 || Math.abs (deltaY) > 5 * Math.abs (deltaX)));
var cursor = (isZoom || this.isZoomArea (this.moved.x) || this.isBound (mouseAction, 46) ? 5 : isRotateXY || isRotateZorZoom ? 3 : this.isBound (mouseAction, 1) ? 1 : 0);
this.setMotion (cursor, isDrag);
return (isZoom || isSlideZoom);
}, $fz.isPrivate = true, $fz), "~N,~N,~N,~N,~B");
$_M(c$, "getExitRate", 
function () {
var dt = this.dragGesture.getTimeDifference (2);
return (dt > 10 ? 0 : this.dragGesture.getSpeedPixelsPerMillisecond (4, 2));
});
$_M(c$, "isRubberBandSelect", 
($fz = function (action) {
return this.rubberbandSelectionMode && (this.isBound (action, 35) || this.isBound (action, 34) || this.isBound (action, 32));
}, $fz.isPrivate = true, $fz), "~N");
$_M(c$, "getRubberBand", 
function () {
if (!this.rubberbandSelectionMode || this.rectRubber.x == 2147483647) return null;
return this.rectRubber;
});
$_M(c$, "calcRectRubberBand", 
($fz = function () {
var factor = (this.viewer.isAntialiased () ? 2 : 1);
if (this.current.x < this.pressed.x) {
this.rectRubber.x = this.current.x * factor;
this.rectRubber.width = (this.pressed.x - this.current.x) * factor;
} else {
this.rectRubber.x = this.pressed.x * factor;
this.rectRubber.width = (this.current.x - this.pressed.x) * factor;
}if (this.current.y < this.pressed.y) {
this.rectRubber.y = this.current.y * factor;
this.rectRubber.height = (this.pressed.y - this.current.y) * factor;
} else {
this.rectRubber.y = this.pressed.y * factor;
this.rectRubber.height = (this.current.y - this.pressed.y) * factor;
}}, $fz.isPrivate = true, $fz));
$_M(c$, "getDegrees", 
function (delta, i) {
var dim = (i == 0 ? this.viewer.getScreenWidth () : this.viewer.getScreenHeight ());
if (dim > 500) dim = 500;
return (delta) / dim * 180 * this.mouseDragFactor;
}, "~N,~N");
$_M(c$, "checkSlideZoom", 
($fz = function (action) {
return this.isBound (action, 40) && this.isZoomArea (this.pressed.x);
}, $fz.isPrivate = true, $fz), "~N");
$_M(c$, "isZoomArea", 
($fz = function (x) {
return x > this.viewer.getScreenWidth () * (this.viewer.isStereoDouble () ? 2 : 1) * 98 / 100;
}, $fz.isPrivate = true, $fz), "~N");
$_M(c$, "getPoint", 
($fz = function (t) {
var pt =  new J.util.Point3fi ();
pt.setT (t.get ("pt"));
pt.modelIndex = (t.get ("modelIndex")).intValue ();
return pt;
}, $fz.isPrivate = true, $fz), "java.util.Map");
$_M(c$, "findNearestAtom", 
($fz = function (x, y, nearestPoint, isClicked) {
var index = (this.drawMode || nearestPoint != null ? -1 : this.viewer.findNearestAtomIndexMovable (x, y, false));
return (index >= 0 && (isClicked || this.measurementPending == null) && !this.viewer.isInSelectionSubset (index) ? -1 : index);
}, $fz.isPrivate = true, $fz), "~N,~N,J.util.Point3fi,~B");
$_M(c$, "isSelectAction", 
($fz = function (action) {
return (this.isBound (action, 17) || !this.drawMode && !this.labelMode && this.atomPickingMode == 1 && this.isBound (action, 1) || this.dragSelectedMode && (this.isBound (this.dragAction, 27) || this.isBound (this.dragAction, 13)) || this.isBound (action, 22) || this.isBound (action, 35) || this.isBound (action, 32) || this.isBound (action, 34) || this.isBound (action, 36) || this.isBound (action, 30));
}, $fz.isPrivate = true, $fz), "~N");
$_M(c$, "enterMeasurementMode", 
($fz = function (iAtom) {
this.viewer.setPicked (-1);
this.viewer.setPicked (iAtom);
this.viewer.setCursor (2);
this.viewer.setPendingMeasurement (this.measurementPending = J.modelset.MeasurementPending.getMP (this.viewer.getModelSet ()));
this.measurementQueued = this.measurementPending;
}, $fz.isPrivate = true, $fz), "~N");
$_M(c$, "addToMeasurement", 
($fz = function (atomIndex, nearestPoint, dblClick) {
if (atomIndex == -1 && nearestPoint == null || this.measurementPending == null) {
this.exitMeasurementMode ();
return 0;
}var measurementCount = this.measurementPending.getCount ();
if (this.measurementPending.traceX != -2147483648 && measurementCount == 2) this.measurementPending.setCount (measurementCount = 1);
return (measurementCount == 4 && !dblClick ? measurementCount : this.measurementPending.addPoint (atomIndex, nearestPoint, true));
}, $fz.isPrivate = true, $fz), "~N,J.util.Point3fi,~B");
$_M(c$, "resetMeasurement", 
($fz = function () {
this.exitMeasurementMode ();
this.measurementQueued = J.modelset.MeasurementPending.getMP (this.viewer.getModelSet ());
}, $fz.isPrivate = true, $fz));
$_M(c$, "exitMeasurementMode", 
($fz = function () {
if (this.measurementPending == null) return;
this.viewer.setPendingMeasurement (this.measurementPending = null);
this.viewer.setCursor (0);
}, $fz.isPrivate = true, $fz));
$_M(c$, "getSequence", 
($fz = function () {
var a1 = this.measurementQueued.getAtomIndex (1);
var a2 = this.measurementQueued.getAtomIndex (2);
if (a1 < 0 || a2 < 0) return;
var sequence = this.viewer.getSmiles (a1, a2, null, true, false, false, false);
this.viewer.setStatusMeasuring ("measureSequence", -2, sequence, 0);
}, $fz.isPrivate = true, $fz));
$_M(c$, "minimize", 
($fz = function (dragDone) {
this.viewer.stopMinimization ();
var iAtom = this.dragAtomIndex;
if (dragDone) this.dragAtomIndex = -1;
var bs = (this.viewer.getMotionFixedAtoms ().cardinality () == 0 ? this.viewer.getAtomBits ((this.viewer.isAtomPDB (iAtom) ? 1087373318 : 1095761934), J.util.BSUtil.newAndSetBit (iAtom)) : J.util.BSUtil.setAll (this.viewer.getAtomCount ()));
this.viewer.minimize (2147483647, 0, bs, null, 0, false, false, false);
}, $fz.isPrivate = true, $fz), "~B");
$_M(c$, "queueAtom", 
($fz = function (atomIndex, ptClicked) {
var n = this.measurementQueued.addPoint (atomIndex, ptClicked, true);
if (atomIndex >= 0) this.viewer.setStatusAtomPicked (atomIndex, "Atom #" + n + ":" + this.viewer.getAtomInfo (atomIndex));
return n;
}, $fz.isPrivate = true, $fz), "~N,J.util.Point3fi");
$_M(c$, "setMotion", 
function (cursor, inMotion) {
switch (this.viewer.getCursor ()) {
case 4:
break;
default:
this.viewer.setCursor (cursor);
}
if (inMotion) this.viewer.setInMotion (true);
}, "~N,~B");
$_M(c$, "zoomByFactor", 
function (dz, x, y) {
if (dz == 0) return;
this.setMotion (5, true);
this.viewer.zoomByFactor (Math.pow (this.mouseWheelFactor, dz), x, y);
this.viewer.setInMotion (false);
}, "~N,~N,~N");
$_M(c$, "runScript", 
($fz = function (script) {
this.viewer.script (script);
}, $fz.isPrivate = true, $fz), "~S");
$_M(c$, "atomOrPointPicked", 
($fz = function (atomIndex, ptClicked) {
if (atomIndex < 0) {
this.resetMeasurement ();
if (this.isBound (this.clickAction, 33)) {
this.runScript ("select none");
return;
}if (this.atomPickingMode != 5 && this.atomPickingMode != 6) return;
}var n = 2;
switch (this.atomPickingMode) {
case 28:
case 29:
return;
case 0:
return;
case 25:
case 24:
case 8:
var isDelete = (this.atomPickingMode == 8);
var isStruts = (this.atomPickingMode == 25);
if (!this.isBound (this.clickAction, (isDelete ? 5 : 3))) return;
if (this.measurementQueued == null || this.measurementQueued.getCount () == 0 || this.measurementQueued.getCount () > 2) {
this.resetMeasurement ();
this.enterMeasurementMode (atomIndex);
}this.addToMeasurement (atomIndex, ptClicked, true);
if (this.queueAtom (atomIndex, ptClicked) != 2) return;
var cAction = (isDelete || this.measurementQueued.isConnected (this.viewer.getModelSet ().atoms, 2) ? " DELETE" : isStruts ? "STRUTS" : "");
this.runScript ("connect " + this.measurementQueued.getMeasurementScript (" ", true) + cAction);
this.resetMeasurement ();
return;
case 21:
n++;
case 20:
n++;
case 18:
case 19:
case 22:
if (!this.isBound (this.clickAction, 20)) return;
if (this.measurementQueued == null || this.measurementQueued.getCount () == 0 || this.measurementQueued.getCount () > n) {
this.resetMeasurement ();
this.enterMeasurementMode (atomIndex);
}this.addToMeasurement (atomIndex, ptClicked, true);
this.queueAtom (atomIndex, ptClicked);
var i = this.measurementQueued.getCount ();
if (i == 1) {
this.viewer.setPicked (-1);
this.viewer.setPicked (atomIndex);
}if (i < n) return;
if (this.atomPickingMode == 22) {
this.getSequence ();
} else {
this.viewer.setStatusMeasuring ("measurePicked", n, this.measurementQueued.getStringDetail (), this.measurementQueued.getValue ());
if (this.atomPickingMode == 18 || this.pickingStyleMeasure == 4) {
this.runScript ("measure " + this.measurementQueued.getMeasurementScript (" ", true));
}}this.resetMeasurement ();
return;
}
var mode = (this.measurementPending != null && this.atomPickingMode != 1 ? 1 : this.atomPickingMode);
switch (mode) {
case 3:
if (!this.isBound (this.clickAction, 17)) return;
if (ptClicked == null) {
this.zoomTo (atomIndex);
} else {
this.runScript ("zoomTo " + J.util.Escape.eP (ptClicked));
}return;
case 5:
case 6:
if (this.isBound (this.clickAction, 17)) this.checkTwoAtomAction (ptClicked, atomIndex);
}
if (ptClicked != null) return;
var bs;
switch (mode) {
case 1:
if (!this.drawMode && !this.labelMode && this.isBound (this.clickAction, 1)) this.zoomTo (atomIndex);
 else if (this.isBound (this.clickAction, 17)) this.viewer.setStatusAtomPicked (atomIndex, null);
return;
case 2:
if (this.isBound (this.clickAction, 19)) {
this.runScript ("set labeltoggle {atomindex=" + atomIndex + "}");
this.viewer.setStatusAtomPicked (atomIndex, null);
}return;
case 31:
if (this.isBound (this.clickAction, 0)) {
bs = this.viewer.getAtomBitSet ("connected(atomIndex=" + atomIndex + ") and !within(SMARTS,'[r50,R]')");
var nb = bs.cardinality ();
switch (nb) {
case 0:
case 1:
return;
case 2:
break;
case 3:
case 4:
var lengths =  Clazz.newIntArray (nb, 0);
var points =  Clazz.newIntArray (nb, 0);
var ni = 0;
for (var i = bs.nextSetBit (0); i >= 0; i = bs.nextSetBit (i + 1), ni++) {
lengths[ni] = this.viewer.getBranchBitSet (i, atomIndex, true).cardinality ();
points[ni] = i;
}
for (var j = 0; j < nb - 2; j++) {
var max = -2147483648;
var imax = 0;
for (var i = 0; i < nb; i++) if (lengths[i] >= max && bs.get (points[i])) {
imax = points[i];
max = lengths[i];
}
bs.clear (imax);
}
}
this.viewer.undoMoveActionClear (atomIndex, 2, true);
this.viewer.invertSelected (null, null, atomIndex, bs);
this.viewer.setStatusAtomPicked (atomIndex, "inverted: " + J.util.Escape.eBS (bs));
}return;
case 7:
if (this.isBound (this.clickAction, 4)) {
bs = J.util.BSUtil.newAndSetBit (atomIndex);
this.viewer.deleteAtoms (bs, false);
this.viewer.setStatusAtomPicked (atomIndex, "deleted: " + J.util.Escape.eBS (bs));
}return;
}
var spec = "atomindex=" + atomIndex;
switch (this.atomPickingMode) {
default:
return;
case 9:
this.selectAtoms (spec);
break;
case 10:
this.selectAtoms ("within(group, " + spec + ")");
break;
case 11:
this.selectAtoms ("within(chain, " + spec + ")");
break;
case 13:
this.selectAtoms ("within(polymer, " + spec + ")");
break;
case 14:
this.selectAtoms ("within(structure, " + spec + ")");
break;
case 12:
this.selectAtoms ("within(molecule, " + spec + ")");
break;
case 16:
this.selectAtoms ("within(model, " + spec + ")");
break;
case 17:
this.selectAtoms ("visible and within(element, " + spec + ")");
break;
case 15:
this.selectAtoms ("visible and within(site, " + spec + ")");
break;
}
this.viewer.clearClickCount ();
this.viewer.setStatusAtomPicked (atomIndex, null);
}, $fz.isPrivate = true, $fz), "~N,J.util.Point3fi");
$_M(c$, "assignNew", 
($fz = function (x, y) {
if (this.measurementPending.getCount () == 2) {
this.viewer.undoMoveActionClear (-1, 4146, true);
this.runScript ("assign connect " + this.measurementPending.getMeasurementScript (" ", false));
} else if (this.pickAtomAssignType.equals ("Xx")) {
this.exitMeasurementMode ();
this.viewer.refresh (3, "bond dropped");
} else {
if (this.pressed.inRange (this.xyRange, this.dragged.x, this.dragged.y)) {
var s = "assign atom ({" + this.dragAtomIndex + "}) \"" + this.pickAtomAssignType + "\"";
if (this.isPickAtomAssignCharge) {
s += ";{atomindex=" + this.dragAtomIndex + "}.label='%C'; ";
this.viewer.undoMoveActionClear (this.dragAtomIndex, 4, true);
} else {
this.viewer.undoMoveActionClear (-1, 4146, true);
}this.runScript (s);
} else if (!this.isPickAtomAssignCharge) {
this.viewer.undoMoveActionClear (-1, 4146, true);
var a = this.viewer.getModelSet ().atoms[this.dragAtomIndex];
if (a.getElementNumber () == 1) {
this.runScript ("assign atom ({" + this.dragAtomIndex + "}) \"X\"");
} else {
var ptNew = J.util.P3.new3 (x, y, a.screenZ);
this.viewer.unTransformPoint (ptNew, ptNew);
this.runScript ("assign atom ({" + this.dragAtomIndex + "}) \"" + this.pickAtomAssignType + "\" " + J.util.Escape.eP (ptNew));
}}}this.exitMeasurementMode ();
}, $fz.isPrivate = true, $fz), "~N,~N");
$_M(c$, "bondPicked", 
($fz = function (index) {
if (this.bondPickingMode == 33) this.viewer.undoMoveActionClear (-1, 4146, true);
switch (this.bondPickingMode) {
case 33:
this.runScript ("assign bond [{" + index + "}] \"" + this.pickBondAssignType + "\"");
break;
case 34:
this.viewer.setRotateBondIndex (index);
break;
case 8:
this.viewer.deleteBonds (J.util.BSUtil.newAndSetBit (index));
}
}, $fz.isPrivate = true, $fz), "~N");
$_M(c$, "checkTwoAtomAction", 
($fz = function (ptClicked, atomIndex) {
var isSpin = (this.atomPickingMode == 5);
if (this.viewer.getSpinOn () || this.viewer.getNavOn () || this.viewer.getPendingMeasurement () != null) {
this.resetMeasurement ();
if (this.viewer.getSpinOn ()) this.runScript ("spin off");
return;
}if (this.measurementQueued.getCount () >= 2) this.resetMeasurement ();
var queuedAtomCount = this.measurementQueued.getCount ();
if (queuedAtomCount == 1) {
if (ptClicked == null) {
if (this.measurementQueued.getAtomIndex (1) == atomIndex) return;
} else {
if (this.measurementQueued.getAtom (1).distance (ptClicked) == 0) return;
}}if (atomIndex >= 0 || ptClicked != null) queuedAtomCount = this.queueAtom (atomIndex, ptClicked);
if (queuedAtomCount < 2) {
if (isSpin) this.viewer.scriptStatus (queuedAtomCount == 1 ? J.i18n.GT._ ("pick one more atom in order to spin the model around an axis") : J.i18n.GT._ ("pick two atoms in order to spin the model around an axis"));
 else this.viewer.scriptStatus (queuedAtomCount == 1 ? J.i18n.GT._ ("pick one more atom in order to display the symmetry relationship") : J.i18n.GT._ ("pick two atoms in order to display the symmetry relationship between them"));
return;
}var s = this.measurementQueued.getMeasurementScript (" ", false);
if (isSpin) this.runScript ("spin" + s + " " + this.viewer.getInt (553648157));
 else this.runScript ("draw symop" + s + ";show symop" + s);
}, $fz.isPrivate = true, $fz), "J.util.Point3fi,~N");
$_M(c$, "reset", 
($fz = function () {
this.runScript ("!reset");
}, $fz.isPrivate = true, $fz));
$_M(c$, "selectAtoms", 
($fz = function (item) {
if (this.measurementPending != null || this.selectionWorking) return;
this.selectionWorking = true;
var s = (this.rubberbandSelectionMode || this.isBound (this.clickAction, 35) ? "selected and not (" + item + ") or (not selected) and " : this.isBound (this.clickAction, 32) ? "selected and not " : this.isBound (this.clickAction, 34) ? "selected or " : this.clickAction == 0 || this.isBound (this.clickAction, 36) ? "selected tog " : this.isBound (this.clickAction, 30) ? "" : null);
if (s != null) {
s += "(" + item + ")";
try {
var bs = this.viewer.getAtomBitSetEval (null, s);
this.viewer.select (bs, false, 0, false);
this.viewer.refresh (3, "selections set");
} catch (e) {
if (Clazz.exceptionOf (e, Exception)) {
} else {
throw e;
}
}
}this.selectionWorking = false;
}, $fz.isPrivate = true, $fz), "~S");
$_M(c$, "selectRb", 
($fz = function (action) {
var bs = this.viewer.findAtomsInRectangle (this.rectRubber);
if (bs.length () > 0) {
var s = J.util.Escape.eBS (bs);
if (this.isBound (action, 34)) this.runScript ("selectionHalos on;select selected or " + s);
 else if (this.isBound (action, 32)) this.runScript ("selectionHalos on;select selected and not " + s);
 else this.runScript ("selectionHalos on;select selected tog " + s);
}this.viewer.refresh (3, "mouseReleased");
}, $fz.isPrivate = true, $fz), "~N");
$_M(c$, "toggleMeasurement", 
($fz = function () {
if (this.measurementPending == null) return;
var measurementCount = this.measurementPending.getCount ();
if (measurementCount >= 2 && measurementCount <= 4) this.runScript ("!measure " + this.measurementPending.getMeasurementScript (" ", true));
this.exitMeasurementMode ();
}, $fz.isPrivate = true, $fz));
$_M(c$, "zoomTo", 
($fz = function (atomIndex) {
this.runScript ("zoomTo (atomindex=" + atomIndex + ")");
this.viewer.setStatusAtomPicked (atomIndex, null);
}, $fz.isPrivate = true, $fz), "~N");
c$.$ActionManager$MotionPoint$ = function () {
Clazz.pu$h ();
c$ = Clazz.decorateAsClass (function () {
Clazz.prepareCallback (this, arguments);
this.index = 0;
this.x = 0;
this.y = 0;
this.time = 0;
Clazz.instantialize (this, arguments);
}, J.viewer.ActionManager, "MotionPoint");
$_M(c$, "set", 
function (a, b, c, d) {
this.index = a;
this.x = b;
this.y = c;
this.time = d;
}, "~N,~N,~N,~N");
Clazz.overrideMethod (c$, "toString", 
function () {
return "[x = " + this.x + " y = " + this.y + " time = " + this.time + " ]";
});
c$ = Clazz.p0p ();
};
c$.$ActionManager$Gesture$ = function () {
Clazz.pu$h ();
c$ = Clazz.decorateAsClass (function () {
Clazz.prepareCallback (this, arguments);
this.action = 0;
this.nodes = null;
this.ptNext = 0;
this.time0 = 0;
Clazz.instantialize (this, arguments);
}, J.viewer.ActionManager, "Gesture");
Clazz.makeConstructor (c$, 
function (a) {
this.nodes =  new Array (a);
for (var b = 0; b < a; b++) this.nodes[b] = Clazz.innerTypeInstance (J.viewer.ActionManager.MotionPoint, this, null);

}, "~N");
$_M(c$, "setAction", 
function (a, b) {
this.action = a;
this.ptNext = 0;
this.time0 = b;
for (var c = 0; c < this.nodes.length; c++) this.nodes[c].index = -1;

}, "~N,~N");
$_M(c$, "getAction", 
function () {
return this.action;
});
$_M(c$, "add", 
function (a, b, c, d) {
this.action = a;
this.getNode (this.ptNext).set (this.ptNext, b, c, d - this.time0);
this.ptNext++;
return this.ptNext;
}, "~N,~N,~N,~N");
$_M(c$, "getTimeDifference", 
function (a) {
a = this.getPointCount2 (a, 0);
if (a < 2) return 0;
var b = this.getNode (this.ptNext - 1);
var c = this.getNode (this.ptNext - a);
return b.time - c.time;
}, "~N");
$_M(c$, "getSpeedPixelsPerMillisecond", 
function (a, b) {
a = this.getPointCount2 (a, b);
if (a < 2) return 0;
var c = this.getNode (this.ptNext - 1 - b);
var d = this.getNode (this.ptNext - a - b);
var e = ((c.x - d.x)) / this.b$["J.viewer.ActionManager"].viewer.getScreenWidth () * 360;
var f = ((c.y - d.y)) / this.b$["J.viewer.ActionManager"].viewer.getScreenHeight () * 360;
return Math.sqrt (e * e + f * f) / (c.time - d.time);
}, "~N,~N");
$_M(c$, "getDX", 
function (a, b) {
a = this.getPointCount2 (a, b);
if (a < 2) return 0;
var c = this.getNode (this.ptNext - 1 - b);
var d = this.getNode (this.ptNext - a - b);
return c.x - d.x;
}, "~N,~N");
$_M(c$, "getDY", 
function (a, b) {
a = this.getPointCount2 (a, b);
if (a < 2) return 0;
var c = this.getNode (this.ptNext - 1 - b);
var d = this.getNode (this.ptNext - a - b);
return c.y - d.y;
}, "~N,~N");
$_M(c$, "getPointCount", 
function () {
return this.ptNext;
});
$_M(c$, "getPointCount2", 
($fz = function (a, b) {
if (a > this.nodes.length - b) a = this.nodes.length - b;
var c = a + 1;
for (; --c >= 0; ) if (this.getNode (this.ptNext - c - b).index >= 0) break;

return c;
}, $fz.isPrivate = true, $fz), "~N,~N");
$_M(c$, "getNode", 
function (a) {
return this.nodes[(a + this.nodes.length + this.nodes.length) % this.nodes.length];
}, "~N");
Clazz.overrideMethod (c$, "toString", 
function () {
if (this.nodes.length == 0) return "" + this;
return J.viewer.binding.Binding.getMouseActionName (this.action, false) + " nPoints = " + this.ptNext + " " + this.nodes[0];
});
c$ = Clazz.p0p ();
};
Clazz.defineStatics (c$,
"ACTION_assignNew", 0,
"ACTION_center", 1,
"ACTION_clickFrank", 2,
"ACTION_connectAtoms", 3,
"ACTION_deleteAtom", 4,
"ACTION_deleteBond", 5,
"ACTION_depth", 6,
"ACTION_dragAtom", 7,
"ACTION_dragDrawObject", 8,
"ACTION_dragDrawPoint", 9,
"ACTION_dragLabel", 10,
"ACTION_dragMinimize", 11,
"ACTION_dragMinimizeMolecule", 12,
"ACTION_dragSelected", 13,
"ACTION_dragZ", 14,
"ACTION_multiTouchSimulation", 15,
"ACTION_navTranslate", 16,
"ACTION_pickAtom", 17,
"ACTION_pickIsosurface", 18,
"ACTION_pickLabel", 19,
"ACTION_pickMeasure", 20,
"ACTION_pickNavigate", 21,
"ACTION_pickPoint", 22,
"ACTION_popupMenu", 23,
"ACTION_reset", 24,
"ACTION_rotate", 25,
"ACTION_rotateBranch", 26,
"ACTION_rotateSelected", 27,
"ACTION_rotateZ", 28,
"ACTION_rotateZorZoom", 29,
"ACTION_select", 30,
"ACTION_selectAndDrag", 31,
"ACTION_selectAndNot", 32,
"ACTION_selectNone", 33,
"ACTION_selectOr", 34,
"ACTION_selectToggle", 35,
"ACTION_selectToggleExtended", 36,
"ACTION_setMeasure", 37,
"ACTION_slab", 38,
"ACTION_slabAndDepth", 39,
"ACTION_slideZoom", 40,
"ACTION_spinDrawObjectCCW", 41,
"ACTION_spinDrawObjectCW", 42,
"ACTION_stopMotion", 43,
"ACTION_swipe", 44,
"ACTION_translate", 45,
"ACTION_wheelZoom", 46,
"ACTION_count", 47);
c$.actionInfo = c$.prototype.actionInfo =  new Array (47);
c$.actionNames = c$.prototype.actionNames =  new Array (47);
{
J.viewer.ActionManager.newAction (0, "_assignNew", J.i18n.GT._ ("assign/new atom or bond (requires {0})", "set picking assignAtom_??/assignBond_?"));
J.viewer.ActionManager.newAction (1, "_center", J.i18n.GT._ ("center"));
J.viewer.ActionManager.newAction (2, "_clickFrank", J.i18n.GT._ ("pop up recent context menu (click on Jmol frank)"));
J.viewer.ActionManager.newAction (4, "_deleteAtom", J.i18n.GT._ ("delete atom (requires {0})", "set picking DELETE ATOM"));
J.viewer.ActionManager.newAction (5, "_deleteBond", J.i18n.GT._ ("delete bond (requires {0})", "set picking DELETE BOND"));
J.viewer.ActionManager.newAction (6, "_depth", J.i18n.GT._ ("adjust depth (back plane; requires {0})", "SLAB ON"));
J.viewer.ActionManager.newAction (7, "_dragAtom", J.i18n.GT._ ("move atom (requires {0})", "set picking DRAGATOM"));
J.viewer.ActionManager.newAction (8, "_dragDrawObject", J.i18n.GT._ ("move whole DRAW object (requires {0})", "set picking DRAW"));
J.viewer.ActionManager.newAction (9, "_dragDrawPoint", J.i18n.GT._ ("move specific DRAW point (requires {0})", "set picking DRAW"));
J.viewer.ActionManager.newAction (10, "_dragLabel", J.i18n.GT._ ("move label (requires {0})", "set picking LABEL"));
J.viewer.ActionManager.newAction (11, "_dragMinimize", J.i18n.GT._ ("move atom and minimize molecule (requires {0})", "set picking DRAGMINIMIZE"));
J.viewer.ActionManager.newAction (12, "_dragMinimizeMolecule", J.i18n.GT._ ("move and minimize molecule (requires {0})", "set picking DRAGMINIMIZEMOLECULE"));
J.viewer.ActionManager.newAction (13, "_dragSelected", J.i18n.GT._ ("move selected atoms (requires {0})", "set DRAGSELECTED"));
J.viewer.ActionManager.newAction (14, "_dragZ", J.i18n.GT._ ("drag atoms in Z direction (requires {0})", "set DRAGSELECTED"));
J.viewer.ActionManager.newAction (15, "_multiTouchSimulation", J.i18n.GT._ ("simulate multi-touch using the mouse)"));
J.viewer.ActionManager.newAction (16, "_navTranslate", J.i18n.GT._ ("translate navigation point (requires {0} and {1})", ["set NAVIGATIONMODE", "set picking NAVIGATE"]));
J.viewer.ActionManager.newAction (17, "_pickAtom", J.i18n.GT._ ("pick an atom"));
J.viewer.ActionManager.newAction (3, "_pickConnect", J.i18n.GT._ ("connect atoms (requires {0})", "set picking CONNECT"));
J.viewer.ActionManager.newAction (18, "_pickIsosurface", J.i18n.GT._ ("pick an ISOSURFACE point (requires {0}", "set DRAWPICKING"));
J.viewer.ActionManager.newAction (19, "_pickLabel", J.i18n.GT._ ("pick a label to toggle it hidden/displayed (requires {0})", "set picking LABEL"));
J.viewer.ActionManager.newAction (20, "_pickMeasure", J.i18n.GT._ ("pick an atom to include it in a measurement (after starting a measurement or after {0})", "set picking DISTANCE/ANGLE/TORSION"));
J.viewer.ActionManager.newAction (21, "_pickNavigate", J.i18n.GT._ ("pick a point or atom to navigate to (requires {0})", "set NAVIGATIONMODE"));
J.viewer.ActionManager.newAction (22, "_pickPoint", J.i18n.GT._ ("pick a DRAW point (for measurements) (requires {0}", "set DRAWPICKING"));
J.viewer.ActionManager.newAction (23, "_popupMenu", J.i18n.GT._ ("pop up the full context menu"));
J.viewer.ActionManager.newAction (24, "_reset", J.i18n.GT._ ("reset (when clicked off the model)"));
J.viewer.ActionManager.newAction (25, "_rotate", J.i18n.GT._ ("rotate"));
J.viewer.ActionManager.newAction (26, "_rotateBranch", J.i18n.GT._ ("rotate branch around bond (requires {0})", "set picking ROTATEBOND"));
J.viewer.ActionManager.newAction (27, "_rotateSelected", J.i18n.GT._ ("rotate selected atoms (requires {0})", "set DRAGSELECTED"));
J.viewer.ActionManager.newAction (28, "_rotateZ", J.i18n.GT._ ("rotate Z"));
J.viewer.ActionManager.newAction (29, "_rotateZorZoom", J.i18n.GT._ ("rotate Z (horizontal motion of mouse) or zoom (vertical motion of mouse)"));
J.viewer.ActionManager.newAction (30, "_select", J.i18n.GT._ ("select an atom (requires {0})", "set pickingStyle EXTENDEDSELECT"));
J.viewer.ActionManager.newAction (31, "_selectAndDrag", J.i18n.GT._ ("select and drag atoms (requires {0})", "set DRAGSELECTED"));
J.viewer.ActionManager.newAction (32, "_selectAndNot", J.i18n.GT._ ("unselect this group of atoms (requires {0})", "set pickingStyle DRAG/EXTENDEDSELECT"));
J.viewer.ActionManager.newAction (33, "_selectNone", J.i18n.GT._ ("select NONE (requires {0})", "set pickingStyle EXTENDEDSELECT"));
J.viewer.ActionManager.newAction (34, "_selectOr", J.i18n.GT._ ("add this group of atoms to the set of selected atoms (requires {0})", "set pickingStyle DRAG/EXTENDEDSELECT"));
J.viewer.ActionManager.newAction (35, "_selectToggle", J.i18n.GT._ ("toggle selection (requires {0})", "set pickingStyle DRAG/EXTENDEDSELECT/RASMOL"));
J.viewer.ActionManager.newAction (36, "_selectToggleOr", J.i18n.GT._ ("if all are selected, unselect all, otherwise add this group of atoms to the set of selected atoms (requires {0})", "set pickingStyle DRAG"));
J.viewer.ActionManager.newAction (37, "_setMeasure", J.i18n.GT._ ("pick an atom to initiate or conclude a measurement"));
J.viewer.ActionManager.newAction (38, "_slab", J.i18n.GT._ ("adjust slab (front plane; requires {0})", "SLAB ON"));
J.viewer.ActionManager.newAction (39, "_slabAndDepth", J.i18n.GT._ ("move slab/depth window (both planes; requires {0})", "SLAB ON"));
J.viewer.ActionManager.newAction (40, "_slideZoom", J.i18n.GT._ ("zoom (along right edge of window)"));
J.viewer.ActionManager.newAction (41, "_spinDrawObjectCCW", J.i18n.GT._ ("click on two points to spin around axis counterclockwise (requires {0})", "set picking SPIN"));
J.viewer.ActionManager.newAction (42, "_spinDrawObjectCW", J.i18n.GT._ ("click on two points to spin around axis clockwise (requires {0})", "set picking SPIN"));
J.viewer.ActionManager.newAction (43, "_stopMotion", J.i18n.GT._ ("stop motion (requires {0})", "set waitForMoveTo FALSE"));
J.viewer.ActionManager.newAction (44, "_swipe", J.i18n.GT._ ("spin model (swipe and release button and stop motion simultaneously)"));
J.viewer.ActionManager.newAction (45, "_translate", J.i18n.GT._ ("translate"));
J.viewer.ActionManager.newAction (46, "_wheelZoom", J.i18n.GT._ ("zoom"));
}Clazz.defineStatics (c$,
"PICKING_OFF", 0,
"PICKING_IDENTIFY", 1,
"PICKING_LABEL", 2,
"PICKING_CENTER", 3,
"PICKING_DRAW", 4,
"PICKING_SPIN", 5,
"PICKING_SYMMETRY", 6,
"PICKING_DELETE_ATOM", 7,
"PICKING_DELETE_BOND", 8,
"PICKING_SELECT_ATOM", 9,
"PICKING_SELECT_GROUP", 10,
"PICKING_SELECT_CHAIN", 11,
"PICKING_SELECT_MOLECULE", 12,
"PICKING_SELECT_POLYMER", 13,
"PICKING_SELECT_STRUCTURE", 14,
"PICKING_SELECT_SITE", 15,
"PICKING_SELECT_MODEL", 16,
"PICKING_SELECT_ELEMENT", 17,
"PICKING_MEASURE", 18,
"PICKING_MEASURE_DISTANCE", 19,
"PICKING_MEASURE_ANGLE", 20,
"PICKING_MEASURE_TORSION", 21,
"PICKING_MEASURE_SEQUENCE", 22,
"PICKING_NAVIGATE", 23,
"PICKING_CONNECT", 24,
"PICKING_STRUTS", 25,
"PICKING_DRAG_SELECTED", 26,
"PICKING_DRAG_MOLECULE", 27,
"PICKING_DRAG_ATOM", 28,
"PICKING_DRAG_MINIMIZE", 29,
"PICKING_DRAG_MINIMIZE_MOLECULE", 30,
"PICKING_INVERT_STEREO", 31,
"PICKING_ASSIGN_ATOM", 32,
"PICKING_ASSIGN_BOND", 33,
"PICKING_ROTATE_BOND", 34,
"PICKING_IDENTIFY_BOND", 35,
"PICKING_DRAG_LIGAND", 36,
"pickingModeNames", ["off", "identify", "label", "center", "draw", "spin", "symmetry", "deleteatom", "deletebond", "atom", "group", "chain", "molecule", "polymer", "structure", "site", "model", "element", "measure", "distance", "angle", "torsion", "sequence", "navigate", "connect", "struts", "dragselected", "dragmolecule", "dragatom", "dragminimize", "dragminimizemolecule", "invertstereo", "assignatom", "assignbond", "rotatebond", "identifybond", "dragligand"],
"PICKINGSTYLE_SELECT_JMOL", 0,
"PICKINGSTYLE_SELECT_CHIME", 0,
"PICKINGSTYLE_SELECT_RASMOL", 1,
"PICKINGSTYLE_SELECT_PFAAT", 2,
"PICKINGSTYLE_SELECT_DRAG", 3,
"PICKINGSTYLE_MEASURE_ON", 4,
"PICKINGSTYLE_MEASURE_OFF", 5,
"pickingStyleNames", ["toggle", "selectOrToggle", "extendedSelect", "drag", "measure", "measureoff"],
"MAX_DOUBLE_CLICK_MILLIS", 700,
"MININUM_GESTURE_DELAY_MILLISECONDS", 10,
"SLIDE_ZOOM_X_PERCENT", 98,
"DEFAULT_MOUSE_DRAG_FACTOR", 1,
"DEFAULT_MOUSE_WHEEL_FACTOR", 1.15,
"DEFAULT_GESTURE_SWIPE_FACTOR", 1);
});
