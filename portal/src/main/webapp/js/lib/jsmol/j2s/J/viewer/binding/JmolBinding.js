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
this.bindAction (33296, 30);
this.bindAction (33040, 36);
});
$_M(c$, "setGeneralBindings", 
($fz = function () {
this.bindAction (8474, 45);
this.bindAction (8454, 45);
this.bindAction (8721, 45);
this.bindAction (8712, 45);
this.bindAction (8464, 25);
this.bindAction (8472, 28);
this.bindAction (8453, 28);
this.bindAction (8465, 29);
this.bindAction (8456, 29);
this.bindAction (288, 46);
this.bindAction (8464, 40);
this.bindAction (8464, 16);
this.bindAction (4370, 23);
this.bindAction (4356, 23);
this.bindAction (33040, 2);
this.bindAction (8467, 38);
this.bindAction (8723, 6);
this.bindAction (8475, 39);
this.bindAction (8464, 44);
this.bindAction (8464, 41);
this.bindAction (8465, 42);
this.bindAction (8473, 13);
this.bindAction (8465, 14);
this.bindAction (8472, 27);
this.bindAction (8465, 26);
this.bindAction (8465, 10);
this.bindAction (8472, 9);
this.bindAction (8465, 8);
this.bindAction (33297, 24);
this.bindAction (33288, 24);
this.bindAction (33296, 43);
this.bindAction (8464, 7);
this.bindAction (8464, 11);
this.bindAction (8464, 12);
this.bindAction (33040, 17);
this.bindAction (33040, 22);
this.bindAction (33040, 19);
this.bindAction (33040, 20);
this.bindAction (33296, 37);
this.bindAction (33040, 18);
this.bindAction (33043, 21);
this.bindAction (33040, 4);
this.bindAction (33040, 5);
this.bindAction (33040, 3);
this.bindAction (33040, 0);
this.bindAction (33043, 1);
}, $fz.isPrivate = true, $fz));
});
