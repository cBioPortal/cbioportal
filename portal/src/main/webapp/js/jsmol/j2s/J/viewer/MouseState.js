Clazz.declarePackage ("J.viewer");
c$ = Clazz.decorateAsClass (function () {
this.x = -1000;
this.y = -1000;
this.modifiers = 0;
this.time = -1;
Clazz.instantialize (this, arguments);
}, J.viewer, "MouseState");
Clazz.makeConstructor (c$, 
function () {
});
$_M(c$, "set", 
function (time, x, y, modifiers) {
this.time = time;
this.x = x;
this.y = y;
this.modifiers = modifiers;
}, "~N,~N,~N,~N");
$_M(c$, "setCurrent", 
function (current, why) {
this.time = current.time;
this.x = current.x;
this.y = current.y;
this.modifiers = current.modifiers;
}, "J.viewer.MouseState,~N");
$_M(c$, "inRange", 
function (xyRange, x, y) {
return (Math.abs (this.x - x) <= xyRange && Math.abs (this.y - y) <= xyRange);
}, "~N,~N,~N");
$_M(c$, "check", 
function (xyRange, x, y, modifiers, time, delayMax) {
return (this.modifiers == modifiers && (delayMax >= 2147483647 ? this.inRange (xyRange, x, y) : (time - this.time) < delayMax));
}, "~N,~N,~N,~N,~N,~N");
$_M(c$, "is", 
function (current) {
return (current.x == this.x && current.y == this.y && current.time == this.time);
}, "J.viewer.MouseState");
