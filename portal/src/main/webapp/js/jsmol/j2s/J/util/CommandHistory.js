Clazz.declarePackage ("J.util");
Clazz.load (null, "J.util.CommandHistory", ["J.util.JmolList"], function () {
c$ = Clazz.decorateAsClass (function () {
this.commandList = null;
this.maxSize = 100;
this.nextCommand = 0;
this.cursorPos = 0;
this.isOn = true;
Clazz.instantialize (this, arguments);
}, J.util, "CommandHistory");
Clazz.makeConstructor (c$, 
function () {
this.reset (100);
});
Clazz.makeConstructor (c$, 
function (maxSize) {
this.reset (maxSize);
}, "~N");
$_M(c$, "clear", 
function () {
this.reset (this.maxSize);
});
$_M(c$, "reset", 
function (maxSize) {
this.maxSize = maxSize;
this.commandList =  new J.util.JmolList ();
this.nextCommand = 0;
this.commandList.addLast ("");
this.cursorPos = 0;
}, "~N");
$_M(c$, "setMaxSize", 
function (maxSize) {
if (maxSize == this.maxSize) return;
if (maxSize < 2) maxSize = 2;
while (this.nextCommand > maxSize) {
this.commandList.remove (0);
this.nextCommand--;
}
if (this.nextCommand > maxSize) this.nextCommand = maxSize - 1;
this.cursorPos = this.nextCommand;
this.maxSize = maxSize;
}, "~N");
$_M(c$, "getCommandUp", 
function () {
if (this.cursorPos <= 0) return null;
this.cursorPos--;
var str = this.getCommand ();
if (str.endsWith ("#??")) this.removeCommand (this.cursorPos--);
if (this.cursorPos < 0) this.cursorPos = 0;
return str;
});
$_M(c$, "getCommandDown", 
function () {
if (this.cursorPos >= this.nextCommand) return null;
this.cursorPos++;
return this.getCommand ();
});
$_M(c$, "getCommand", 
($fz = function () {
return this.commandList.get (this.cursorPos);
}, $fz.isPrivate = true, $fz));
$_M(c$, "addCommand", 
function (strCommand) {
if (!this.isOn && !strCommand.endsWith ("#??")) return;
if (strCommand.endsWith ("#----")) return;
var i;
while ((i = strCommand.indexOf ("\n")) >= 0) {
var str = strCommand.substring (0, i);
if (str.length > 0) this.addCommandLine (str);
strCommand = strCommand.substring (i + 1);
}
if (strCommand.length > 0) this.addCommandLine (strCommand);
}, "~S");
$_M(c$, "getSetHistory", 
function (n) {
this.isOn = (n == -2 ? this.isOn : true);
switch (n) {
case 0:
this.isOn = false;
this.clear ();
return "";
case -2147483648:
case -2:
this.clear ();
return "";
case -1:
return this.getCommandUp ();
case 1:
return this.getCommandDown ();
default:
if (n < 0) {
this.setMaxSize (-2 - n);
return "";
}n = Math.max (this.nextCommand - n, 0);
}
var str = "";
for (var i = n; i < this.nextCommand; i++) if (!this.commandList.get (i).toUpperCase ().startsWith ("WRITE HISTORY")) str += this.commandList.get (i) + "\n";

return str;
}, "~N");
$_M(c$, "removeCommand", 
function () {
return this.removeCommand (this.nextCommand - 1);
});
$_M(c$, "removeCommand", 
function (n) {
if (n < 0 || n >= this.nextCommand) return "";
var str = this.commandList.remove (n);
this.nextCommand--;
return str;
}, "~N");
$_M(c$, "addCommandLine", 
($fz = function (command) {
if (command == null || command.length == 0) return;
if (command.endsWith ("#--")) return;
if (this.nextCommand >= this.maxSize) {
this.commandList.remove (0);
this.nextCommand = this.maxSize - 1;
}this.commandList.add (this.nextCommand, command);
this.nextCommand++;
this.cursorPos = this.nextCommand;
this.commandList.add (this.nextCommand, "");
}, $fz.isPrivate = true, $fz), "~S");
Clazz.defineStatics (c$,
"ERROR_FLAG", "#??",
"NOHISTORYLINE_FLAG", "#--",
"NOHISTORYATALL_FLAG", "#----",
"DEFAULT_MAX_SIZE", 100);
});
