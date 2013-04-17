Clazz.declarePackage ("J.util");
c$ = Clazz.decorateAsClass (function () {
this.x = 0;
this.y = 0;
this.width = 0;
this.height = 0;
Clazz.instantialize (this, arguments);
}, J.util, "Rectangle");
$_M(c$, "contains", 
function (X, Y) {
return (X >= this.x && Y >= this.y && X - this.x < this.width && Y - this.y < this.height);
}, "~N,~N");
