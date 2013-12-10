Clazz.declarePackage ("J.modelset");
c$ = Clazz.decorateAsClass (function () {
this.id = "";
this.type = " ";
this.ticks = null;
this.tickLabelFormats = null;
this.scale = null;
this.first = 0;
this.signFactor = 1;
this.reference = null;
Clazz.instantialize (this, arguments);
}, J.modelset, "TickInfo");
Clazz.makeConstructor (c$, 
function (ticks) {
this.ticks = ticks;
}, "J.util.P3");
