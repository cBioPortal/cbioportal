Clazz.declarePackage ("J.awtjs2d");
Clazz.load (["J.popup.GenericPopup"], "J.awtjs2d.JSPopup", null, function () {
c$ = Clazz.declareType (J.awtjs2d, "JSPopup", J.popup.GenericPopup);
Clazz.makeConstructor (c$, 
function () {
Clazz.superConstructor (this, J.awtjs2d.JSPopup, []);
});
$_M(c$, "updateButton", 
function (b, entry, script) {
var ret = [entry];
var icon = this.getEntryIcon (ret);
entry = ret[0];
{
if (icon != null) b.setIcon(icon);
if (entry != null) b.setText(entry);
if (script != null) b.setActionCommand(script);
this.thisPopup.tainted = true;
}}, "~O,~S,~S");
$_M(c$, "newMenuItem", 
($fz = function (menu, item, text, script, id) {
this.updateButton (item, text, script);
{
if (id != null && id.startsWith("Focus")) {
item.addMouseListener(this); id = menu.getName() + "." + id; }
item.setName(id == null ? menu.getName() + "." : id);
}this.menuAddItem (menu, item);
return item;
}, $fz.isPrivate = true, $fz), "~O,~O,~S,~S,~S");
Clazz.overrideMethod (c$, "menuAddButtonGroup", 
function (newMenu) {
{
if (this.buttonGroup == null) this.buttonGroup = new
Jmol.Menu.ButtonGroup(this.thisPopup);
this.buttonGroup.add(newMenu);
}}, "~O");
Clazz.overrideMethod (c$, "menuAddItem", 
function (menu, item) {
{
menu.add(item); this.thisPopup.tainted = true;
}}, "~O,~O");
Clazz.overrideMethod (c$, "menuAddSeparator", 
function (menu) {
{
menu.add(new Jmol.Menu.MenuItem(this.thisPopup, null, false,
false)); this.thisPopup.tainted = true;
}}, "~O");
Clazz.overrideMethod (c$, "menuAddSubMenu", 
function (menu, subMenu) {
this.menuAddItem (menu, subMenu);
}, "~O,~O");
Clazz.overrideMethod (c$, "menuClearListeners", 
function (menu) {
}, "~O");
Clazz.overrideMethod (c$, "menuCreateCheckboxItem", 
function (menu, entry, basename, id, state, isRadio) {
var item = null;
{
item = new Jmol.Menu.MenuItem(this.thisPopup, entry, !isRadio,
isRadio); item.setSelected(state); item.addItemListener(this);
}return this.newMenuItem (menu, item, entry, basename, id);
}, "~O,~S,~S,~S,~B,~B");
Clazz.overrideMethod (c$, "menuCreateItem", 
function (menu, entry, script, id) {
var item = null;
{
item = new Jmol.Menu.MenuItem(this.thisPopup, entry);
item.addActionListener(this);
}return this.newMenuItem (menu, item, entry, script, id);
}, "~O,~S,~S,~S");
Clazz.overrideMethod (c$, "menuCreatePopup", 
function (name) {
{
return new Jmol.Menu.PopupMenu(this.viewer.applet, name);
}}, "~S");
Clazz.overrideMethod (c$, "menuEnable", 
function (menu, enable) {
{
if (menu.isItem) { this.menuEnableItem(menu, enable); return;
} try { menu.setEnabled(enable); } catch (e) {
}
this.thisPopup.tainted = true;
}}, "~O,~B");
Clazz.overrideMethod (c$, "menuEnableItem", 
function (item, enable) {
{
try { item.setEnabled(enable); } catch (e) { }
this.thisPopup.tainted = true;
}}, "~O,~B");
Clazz.overrideMethod (c$, "menuGetAsText", 
function (sb, level, menu, menuName) {
{
var name = menuName; var subMenus = menu.getComponents(); for
(var i = 0; i < subMenus.length; i++) { var m = subMenus[i];
var flags = null; if (m.isMenu) { name = m.getName(); flags =
"enabled:" + m.isEnabled(); this.addItemText(sb, 'M', level,
name, m.getText(), null, flags); this.menuGetAsText(sb, level
+ 1, m.getPopupMenu(), name); } else if (m.isItem) { flags =
"enabled:" + m.isEnabled(); if (m.isCheckBox) flags +=
";checked:" + m.getState(); var script =
this.fixScript(m.getName(), m.getActionCommand());
this.addItemText(sb, 'I', level, m.getName(), m.getText(),
script, flags); } else { this.addItemText(sb, 'S', level,
name, null, null, null); } }
}}, "J.util.SB,~N,~O,~S");
Clazz.overrideMethod (c$, "menuGetId", 
function (menu) {
{
return menu.getName();
}}, "~O");
Clazz.overrideMethod (c$, "menuGetItemCount", 
function (menu) {
{
return menu.getItemCount();
}}, "~O");
Clazz.overrideMethod (c$, "menuGetParent", 
function (menu) {
{
return menu.getParent();
}}, "~O");
Clazz.overrideMethod (c$, "menuGetPosition", 
function (menu) {
{
var p = menuGetParent(menu); if (p != null) for (var i =
p.getItemCount(); --i >= 0;) if (p.getItem(i) == menu) return
i;
}return -1;
}, "~O");
Clazz.overrideMethod (c$, "menuInsertSubMenu", 
function (menu, subMenu, index) {
}, "~O,~O,~N");
Clazz.overrideMethod (c$, "menuNewSubMenu", 
function (entry, id) {
{
var menu = new Jmol.Menu.SubMenu(this.thisPopup, entry);
this.updateButton(menu, entry, null); menu.setName(id);
menu.setAutoscrolls(true); return menu;
}}, "~S,~S");
Clazz.overrideMethod (c$, "menuRemoveAll", 
function (menu, indexFrom) {
{
menu.removeAll(indexFrom); this.thisPopup.tainted = true;
}}, "~O,~N");
Clazz.overrideMethod (c$, "menuSetAutoscrolls", 
function (menu) {
{
menu.setAutoscrolls(true); this.thisPopup.tainted = true;
}}, "~O");
Clazz.overrideMethod (c$, "menuSetCheckBoxState", 
function (item, state) {
{
item.setSelected(state); this.thisPopup.tainted = true;
}}, "~O,~B");
Clazz.overrideMethod (c$, "menuSetCheckBoxOption", 
function (item, name, what) {
return null;
}, "~O,~S,~S");
Clazz.overrideMethod (c$, "menuSetCheckBoxValue", 
function (source) {
{
this.setCheckBoxValue(source, source.getActionCommand(),
source.isSelected()); this.thisPopup.tainted = true;
}}, "~O");
Clazz.overrideMethod (c$, "menuSetLabel", 
function (menu, entry) {
{
menu.setText(entry); this.thisPopup.tainted = true;
}}, "~O,~S");
Clazz.overrideMethod (c$, "menuSetListeners", 
function () {
});
Clazz.overrideMethod (c$, "menuShowPopup", 
function (popup, x, y) {
{
popup.menuShowPopup(x, y);
}}, "~O,~N,~N");
});
