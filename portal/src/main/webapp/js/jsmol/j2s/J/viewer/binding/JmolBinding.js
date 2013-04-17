Clazz.declarePackage ("J.viewer.binding");
Clazz.load (["J.viewer.binding.Binding"], "J.viewer.binding.JmolBinding", null, function () {
c$ = Clazz.declareType (J.viewer.binding, "JmolBinding", J.viewer.binding.Binding);
Clazz.makeConstructor (c$, 
function (name) {
Clazz.superConstructor (this, J.viewer.binding.JmolBinding, [name]);
this.setGeneralBindings ();
this.setSelectBindings ();
}, "~S");
$_M(c$, "setSelectBindings", 
function () {
this.bindAction (528, 16);
this.bindAction (272, 21);
});
$_M(c$, "setGeneralBindings", 
($fz = function () {
this.bindAction (528, 0);
this.bindAction (282, 1);
this.bindAction (262, 1);
this.bindAction (529, 1);
this.bindAction (520, 1);
this.bindAction (272, 2);
this.bindAction (280, 3);
this.bindAction (261, 3);
this.bindAction (273, 4);
this.bindAction (264, 4);
this.bindAction (32, 5);
this.bindAction (272, 6);
this.bindAction (272, 7);
this.bindAction (274, 14);
this.bindAction (260, 14);
this.bindAction (272, 15);
this.bindAction (275, 11);
this.bindAction (531, 12);
this.bindAction (283, 13);
this.bindAction (272, 8);
this.bindAction (272, 10);
this.bindAction (273, 9);
this.bindAction (281, 22);
this.bindAction (273, 24);
this.bindAction (280, 25);
this.bindAction (273, 26);
this.bindAction (273, 30);
this.bindAction (280, 31);
this.bindAction (273, 32);
this.bindAction (529, 44);
this.bindAction (520, 44);
this.bindAction (528, 45);
this.bindAction (272, 27);
this.bindAction (272, 28);
this.bindAction (272, 29);
this.bindAction (272, 33);
this.bindAction (272, 34);
this.bindAction (272, 35);
this.bindAction (272, 36);
this.bindAction (528, 37);
this.bindAction (272, 38);
this.bindAction (275, 39);
this.bindAction (272, 40);
this.bindAction (272, 41);
this.bindAction (272, 42);
this.bindAction (272, 43);
}, $fz.isPrivate = true, $fz));
});
