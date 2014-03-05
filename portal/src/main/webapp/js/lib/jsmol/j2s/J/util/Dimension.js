Clazz.declarePackage ("J.util");
c$ = Clazz.decorateAsClass (function () {
this.height = 0;
this.width = 0;
Clazz.instantialize (this, arguments);
}, J.util, "Dimension");
$_M(c$, "set", 
function (w, h) {
this.width = w;
this.height = h;
return this;
}, "~N,~N");
