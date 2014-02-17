Clazz.declarePackage ("J.awtjs2d");
Clazz.load (["J.api.JmolMouseInterface", "$.Event"], "J.awtjs2d.Mouse", ["java.lang.Character", "J.util.Escape", "$.Logger", "$.V3"], function () {
c$ = Clazz.decorateAsClass (function () {
this.viewer = null;
this.actionManager = null;
this.keyBuffer = "";
this.isMouseDown = false;
this.xWhenPressed = 0;
this.yWhenPressed = 0;
this.modifiersWhenPressed10 = 0;
Clazz.instantialize (this, arguments);
}, J.awtjs2d, "Mouse", null, J.api.JmolMouseInterface);
Clazz.makeConstructor (c$, 
function (viewer, actionManager) {
this.viewer = viewer;
this.actionManager = actionManager;
}, "J.viewer.Viewer,J.viewer.ActionManager");
Clazz.overrideMethod (c$, "clear", 
function () {
});
Clazz.overrideMethod (c$, "dispose", 
function () {
this.actionManager.dispose ();
});
Clazz.overrideMethod (c$, "handleOldJvm10Event", 
function (id, x, y, modifiers, time) {
if (id != -1) modifiers = J.awtjs2d.Mouse.applyLeftMouse (modifiers);
switch (id) {
case -1:
this.wheeled (time, x, modifiers | 32);
break;
case 501:
this.xWhenPressed = x;
this.yWhenPressed = y;
this.modifiersWhenPressed10 = modifiers;
this.pressed (time, x, y, modifiers, false);
break;
case 506:
this.dragged (time, x, y, modifiers);
break;
case 504:
this.entered (time, x, y);
break;
case 505:
this.exited (time, x, y);
break;
case 503:
this.moved (time, x, y, modifiers);
break;
case 502:
this.released (time, x, y, modifiers);
if (x == this.xWhenPressed && y == this.yWhenPressed && modifiers == this.modifiersWhenPressed10) {
this.clicked (time, x, y, modifiers, 1);
}break;
default:
return false;
}
return true;
}, "~N,~N,~N,~N,~N");
$_M(c$, "processTwoPointGesture", 
function (touches) {
if (touches[0].length < 2) return;
var t1 = touches[0];
var t2 = touches[1];
var t1first = t1[0];
var t1last = t1[t2.length - 1];
var x1first = t1first[0];
var x1last = t1last[0];
var dx1 = x1last - x1first;
var y1first = t1first[1];
var y1last = t1last[1];
var dy1 = y1last - y1first;
var v1 = J.util.V3.new3 (dx1, dy1, 0);
var d1 = v1.length ();
var t2first = t2[0];
var t2last = t2[t2.length - 1];
var x2first = t2first[0];
var x2last = t2last[0];
var dx2 = x2last - x2first;
var y2first = t2first[1];
var y2last = t2last[1];
var dy2 = y2last - y2first;
var v2 = J.util.V3.new3 (dx2, dy2, 0);
var d2 = v2.length ();
if (d1 < 1 || d2 < 1) return;
v1.normalize ();
v2.normalize ();
var cos12 = (v1.dot (v2));
if (cos12 > 0.8) {
var deltaX = Clazz.floatToInt (x1last - t1[t1.length - 2][0]);
var deltaY = Clazz.floatToInt (y1last - t1[t1.length - 2][1]);
this.viewer.translateXYBy (deltaX, deltaY);
} else if (cos12 < -0.8) {
v1 = J.util.V3.new3 (x2first - x1first, y2first - y1first, 0);
v2 = J.util.V3.new3 (x2last - x1last, y2last - y1last, 0);
var dx = v2.length () - v1.length ();
this.wheeled (System.currentTimeMillis (), dx < 0 ? -1 : 1, 32);
}}, "~A");
$_M(c$, "mouseClicked", 
function (e) {
this.clicked (e.getWhen (), e.getX (), e.getY (), e.getModifiers (), e.getClickCount ());
}, "java.awt.event.MouseEvent");
$_M(c$, "mouseEntered", 
function (e) {
this.entered (e.getWhen (), e.getX (), e.getY ());
}, "java.awt.event.MouseEvent");
$_M(c$, "mouseExited", 
function (e) {
this.exited (e.getWhen (), e.getX (), e.getY ());
}, "java.awt.event.MouseEvent");
$_M(c$, "mousePressed", 
function (e) {
this.pressed (e.getWhen (), e.getX (), e.getY (), e.getModifiers (), e.isPopupTrigger ());
}, "java.awt.event.MouseEvent");
$_M(c$, "mouseReleased", 
function (e) {
this.released (e.getWhen (), e.getX (), e.getY (), e.getModifiers ());
}, "java.awt.event.MouseEvent");
$_M(c$, "mouseDragged", 
function (e) {
var modifiers = e.getModifiers ();
if ((modifiers & 28) == 0) modifiers |= 16;
this.dragged (e.getWhen (), e.getX (), e.getY (), modifiers);
}, "java.awt.event.MouseEvent");
$_M(c$, "mouseMoved", 
function (e) {
this.moved (e.getWhen (), e.getX (), e.getY (), e.getModifiers ());
}, "java.awt.event.MouseEvent");
$_M(c$, "mouseWheelMoved", 
function (e) {
e.consume ();
this.wheeled (e.getWhen (), e.getWheelRotation (), e.getModifiers () | 32);
}, "java.awt.event.MouseWheelEvent");
$_M(c$, "keyTyped", 
function (ke) {
ke.consume ();
if (!this.viewer.menuEnabled ()) return;
var ch = ke.getKeyChar ();
var modifiers = ke.getModifiers ();
if (J.util.Logger.debuggingHigh) J.util.Logger.debug ("MouseManager keyTyped: " + ch + " " + (0 + ch.charCodeAt (0)) + " " + modifiers);
if (modifiers != 0 && modifiers != 1) {
switch (ch) {
case String.fromCharCode (11):
case 'k':
var isON = !this.viewer.getBooleanProperty ("allowKeyStrokes");
switch (modifiers) {
case 2:
this.viewer.setBooleanProperty ("allowKeyStrokes", isON);
this.viewer.setBooleanProperty ("showKeyStrokes", true);
break;
case 10:
case 8:
this.viewer.setBooleanProperty ("allowKeyStrokes", isON);
this.viewer.setBooleanProperty ("showKeyStrokes", false);
break;
}
this.clearKeyBuffer ();
this.viewer.refresh (3, "showkey");
break;
case 22:
case 'v':
switch (modifiers) {
case 2:
break;
}
break;
case 26:
case 'z':
switch (modifiers) {
case 2:
this.viewer.undoMoveAction (4165, 1);
break;
case 3:
this.viewer.undoMoveAction (4139, 1);
break;
}
break;
case 25:
case 'y':
switch (modifiers) {
case 2:
this.viewer.undoMoveAction (4139, 1);
break;
}
break;
}
return;
}if (!this.viewer.getBooleanProperty ("allowKeyStrokes")) return;
this.addKeyBuffer (ke.getModifiers () == 1 ? Character.toUpperCase (ch) : ch);
}, "java.awt.event.KeyEvent");
$_M(c$, "keyPressed", 
function (ke) {
if (this.viewer.isApplet ()) ke.consume ();
this.actionManager.keyPressed (ke.getKeyCode (), ke.getModifiers ());
}, "java.awt.event.KeyEvent");
$_M(c$, "keyReleased", 
function (ke) {
ke.consume ();
this.actionManager.keyReleased (ke.getKeyCode ());
}, "java.awt.event.KeyEvent");
$_M(c$, "clearKeyBuffer", 
($fz = function () {
if (this.keyBuffer.length == 0) return;
this.keyBuffer = "";
if (this.viewer.getBooleanProperty ("showKeyStrokes")) this.viewer.evalStringQuiet ("!set echo _KEYSTROKES; set echo bottom left;echo \"\"");
}, $fz.isPrivate = true, $fz));
$_M(c$, "addKeyBuffer", 
($fz = function (ch) {
if (ch.charCodeAt (0) == 10) {
this.sendKeyBuffer ();
return;
}if (ch.charCodeAt (0) == 8) {
if (this.keyBuffer.length > 0) this.keyBuffer = this.keyBuffer.substring (0, this.keyBuffer.length - 1);
} else {
this.keyBuffer += ch;
}if (this.viewer.getBooleanProperty ("showKeyStrokes")) this.viewer.evalStringQuiet ("!set echo _KEYSTROKES; set echo bottom left;echo " + J.util.Escape.eS ("\1" + this.keyBuffer));
}, $fz.isPrivate = true, $fz), "~S");
$_M(c$, "sendKeyBuffer", 
($fz = function () {
var kb = this.keyBuffer;
if (this.viewer.getBooleanProperty ("showKeyStrokes")) this.viewer.evalStringQuiet ("!set echo _KEYSTROKES; set echo bottom left;echo " + J.util.Escape.eS (this.keyBuffer));
this.clearKeyBuffer ();
this.viewer.script (kb);
}, $fz.isPrivate = true, $fz));
$_M(c$, "entered", 
($fz = function (time, x, y) {
this.actionManager.mouseEntered (time, x, y);
}, $fz.isPrivate = true, $fz), "~N,~N,~N");
$_M(c$, "exited", 
($fz = function (time, x, y) {
this.actionManager.mouseExited (time, x, y);
}, $fz.isPrivate = true, $fz), "~N,~N,~N");
$_M(c$, "clicked", 
($fz = function (time, x, y, modifiers, clickCount) {
this.clearKeyBuffer ();
this.actionManager.mouseAction (2, time, x, y, 1, modifiers);
}, $fz.isPrivate = true, $fz), "~N,~N,~N,~N,~N");
$_M(c$, "moved", 
($fz = function (time, x, y, modifiers) {
this.clearKeyBuffer ();
if (this.isMouseDown) this.actionManager.mouseAction (1, time, x, y, 0, J.awtjs2d.Mouse.applyLeftMouse (modifiers));
 else this.actionManager.mouseAction (0, time, x, y, 0, modifiers);
}, $fz.isPrivate = true, $fz), "~N,~N,~N,~N");
$_M(c$, "wheeled", 
($fz = function (time, rotation, modifiers) {
this.clearKeyBuffer ();
this.actionManager.mouseAction (3, time, 0, rotation, 0, modifiers);
}, $fz.isPrivate = true, $fz), "~N,~N,~N");
$_M(c$, "pressed", 
($fz = function (time, x, y, modifiers, isPopupTrigger) {
this.clearKeyBuffer ();
this.isMouseDown = true;
this.actionManager.mouseAction (4, time, x, y, 0, modifiers);
}, $fz.isPrivate = true, $fz), "~N,~N,~N,~N,~B");
$_M(c$, "released", 
($fz = function (time, x, y, modifiers) {
this.isMouseDown = false;
this.actionManager.mouseAction (5, time, x, y, 0, modifiers);
}, $fz.isPrivate = true, $fz), "~N,~N,~N,~N");
$_M(c$, "dragged", 
($fz = function (time, x, y, modifiers) {
if ((modifiers & 20) == 20) modifiers = modifiers & -5 | 2;
this.actionManager.mouseAction (1, time, x, y, 0, modifiers);
}, $fz.isPrivate = true, $fz), "~N,~N,~N,~N");
c$.applyLeftMouse = $_M(c$, "applyLeftMouse", 
($fz = function (modifiers) {
return ((modifiers & 28) == 0) ? (modifiers | 16) : modifiers;
}, $fz.isPrivate = true, $fz), "~N");
});
