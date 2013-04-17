Clazz.declarePackage ("J.viewer.binding");
Clazz.load (["J.viewer.binding.JmolBinding"], "J.viewer.binding.PfaatBinding", null, function () {
c$ = Clazz.declareType (J.viewer.binding, "PfaatBinding", J.viewer.binding.JmolBinding);
Clazz.makeConstructor (c$, 
function () {
Clazz.superConstructor (this, J.viewer.binding.PfaatBinding, ["extendedSelect"]);
});
Clazz.overrideMethod (c$, "setSelectBindings", 
function () {
this.bindAction (272, 16);
this.bindAction (272, 17);
this.bindAction (273, 18);
this.bindAction (281, 19);
this.bindAction (280, 20);
});
});
