Clazz.declarePackage ("J.console");
Clazz.load (["J.api.JmolAppConsoleInterface", "$.JmolCallbackListener", "java.util.Hashtable"], "J.console.GenericConsole", ["java.lang.Boolean", "J.constant.EnumCallback", "J.i18n.GT", "J.script.T", "J.util.TextFormat", "J.viewer.Viewer"], function () {
c$ = Clazz.decorateAsClass (function () {
this.input = null;
this.output = null;
this.viewer = null;
this.labels = null;
this.menuMap = null;
this.editButton = null;
this.runButton = null;
this.historyButton = null;
this.stateButton = null;
this.clearOutButton = null;
this.clearInButton = null;
this.loadButton = null;
this.defaultMessage = null;
this.label1 = null;
this.nTab = 0;
this.incompleteCmd = null;
Clazz.instantialize (this, arguments);
}, J.console, "GenericConsole", null, [J.api.JmolAppConsoleInterface, J.api.JmolCallbackListener]);
Clazz.prepareFields (c$, function () {
this.menuMap =  new java.util.Hashtable ();
});
$_M(c$, "setViewer", 
function (viewer) {
this.viewer = viewer;
}, "J.api.JmolViewer");
$_M(c$, "addButton", 
function (b, label) {
b.addConsoleListener (this);
this.menuMap.put (label, b);
return b;
}, "J.api.JmolAbstractButton,~S");
$_M(c$, "getLabel1", 
function () {
return null;
});
$_M(c$, "setupLabels", 
function () {
this.labels.put ("help", J.i18n.GT._ ("&Help"));
this.labels.put ("search", J.i18n.GT._ ("&Search..."));
this.labels.put ("commands", J.i18n.GT._ ("&Commands"));
this.labels.put ("functions", J.i18n.GT._ ("Math &Functions"));
this.labels.put ("parameters", J.i18n.GT._ ("Set &Parameters"));
this.labels.put ("more", J.i18n.GT._ ("&More"));
this.labels.put ("Editor", J.i18n.GT._ ("Editor"));
this.labels.put ("State", J.i18n.GT._ ("State"));
this.labels.put ("Run", J.i18n.GT._ ("Run"));
this.labels.put ("Clear Output", J.i18n.GT._ ("Clear Output"));
this.labels.put ("Clear Input", J.i18n.GT._ ("Clear Input"));
this.labels.put ("History", J.i18n.GT._ ("History"));
this.labels.put ("Load", J.i18n.GT._ ("Load"));
this.labels.put ("label1", J.i18n.GT._ ("press CTRL-ENTER for new line or paste model data and press Load"));
this.labels.put ("default", J.i18n.GT._ ("Messages will appear here. Enter commands in the box below. Click the console Help menu item for on-line help, which will appear in a new browser window."));
});
$_M(c$, "setLabels", 
function () {
var doTranslate = J.i18n.GT.setDoTranslate (true);
this.editButton = this.setButton ("Editor");
this.stateButton = this.setButton ("State");
this.runButton = this.setButton ("Run");
this.clearOutButton = this.setButton ("Clear Output");
this.clearInButton = this.setButton ("Clear Input");
this.historyButton = this.setButton ("History");
this.loadButton = this.setButton ("Load");
this.defaultMessage = this.getLabel ("default");
this.setTitle ();
J.i18n.GT.setDoTranslate (false);
{
this.defaultMessage = this.getLabel("default").split("Click")[0];
}J.i18n.GT.setDoTranslate (doTranslate);
this.defaultMessage = this.getLabel ("default");
});
$_M(c$, "getLabel", 
function (key) {
if (this.labels == null) {
this.labels =  new java.util.Hashtable ();
this.labels.put ("title", J.i18n.GT._ ("Jmol Script Console") + " " + J.viewer.Viewer.getJmolVersion ());
this.setupLabels ();
}return this.labels.get (key);
}, "~S");
$_M(c$, "displayConsole", 
function () {
this.layoutWindow (null);
this.outputMsg (this.defaultMessage);
System.out.println ("AppConsole displayConsole");
});
$_M(c$, "updateLabels", 
function () {
return;
});
$_M(c$, "completeCommand", 
function (thisCmd) {
if (thisCmd.length == 0) return null;
var strCommand = (this.nTab <= 0 || this.incompleteCmd == null ? thisCmd : this.incompleteCmd);
this.incompleteCmd = strCommand;
var splitCmd = J.util.TextFormat.splitCommandLine (thisCmd);
if (splitCmd == null) return null;
var asCommand = splitCmd[2] == null;
var notThis = splitCmd[asCommand ? 1 : 2];
var s = splitCmd[1];
if (notThis.length == 0) return null;
splitCmd = J.util.TextFormat.splitCommandLine (strCommand);
var cmd = null;
if (!asCommand && (notThis.charAt (0) == '"' || notThis.charAt (0) == '\'')) {
var q = notThis.charAt (0);
notThis = J.util.TextFormat.trim (notThis, "\"\'");
var stub = J.util.TextFormat.trim (splitCmd[2], "\"\'");
cmd = this.nextFileName (stub, this.nTab);
if (cmd != null) cmd = splitCmd[0] + splitCmd[1] + q + cmd + q;
} else {
var map = null;
if (!asCommand) {
notThis = s;
if (splitCmd[2].startsWith ("$") || s.equalsIgnoreCase ("isosurface ") || s.equalsIgnoreCase ("contact ") || s.equalsIgnoreCase ("draw ")) {
map =  new java.util.Hashtable ();
this.viewer.getObjectMap (map, splitCmd[2].startsWith ("$"));
}}cmd = J.script.T.completeCommand (map, s.equalsIgnoreCase ("set "), asCommand, asCommand ? splitCmd[1] : splitCmd[2], this.nTab);
cmd = splitCmd[0] + (cmd == null ? notThis : asCommand ? cmd : splitCmd[1] + cmd);
}return (cmd == null || cmd.equals (strCommand) ? null : cmd);
}, "~S");
$_M(c$, "doAction", 
function (source) {
if (source === this.runButton) {
this.execute (null);
} else if (source === this.editButton) {
this.viewer.getProperty ("DATA_API", "scriptEditor", null);
} else if (source === this.historyButton) {
this.clearContent (this.viewer.getSetHistory (2147483647));
} else if (source === this.stateButton) {
this.clearContent (this.viewer.getStateInfo ());
} else if (source === this.clearInButton) {
this.input.setText ("");
return;
}if (source === this.clearOutButton) {
this.output.setText ("");
return;
}if (source === this.loadButton) {
this.viewer.loadInline (this.input.getText (), false);
return;
}if (this.isMenuItem (source)) {
this.execute ((source).getName ());
return;
}}, "~O");
$_M(c$, "execute", 
function (strCommand) {
var cmd = (strCommand == null ? this.input.getText () : strCommand);
if (strCommand == null) this.input.setText (null);
var strErrorMessage = this.viewer.script (cmd + "\u0001## EDITOR_IGNORE ##");
if (strErrorMessage != null && !strErrorMessage.equals ("pending")) this.outputMsg (strErrorMessage);
}, "~S");
$_M(c$, "destroyConsole", 
function () {
if (this.viewer.isApplet ()) this.viewer.getProperty ("DATA_API", "getAppConsole", Boolean.FALSE);
});
c$.setAbstractButtonLabels = $_M(c$, "setAbstractButtonLabels", 
function (menuMap, labels) {
var e = menuMap.keySet ().iterator ();
while (e.hasNext ()) {
var key = e.next ();
var m = menuMap.get (key);
var label = labels.get (key);
if (key.indexOf ("Tip") == key.length - 3) {
m.setToolTipText (labels.get (key));
} else {
var mnemonic = J.console.GenericConsole.getMnemonic (label);
if (mnemonic != ' ') m.setMnemonic (mnemonic);
label = J.console.GenericConsole.getLabelWithoutMnemonic (label);
m.setText (label);
}}
}, "java.util.Map,java.util.Map");
c$.getLabelWithoutMnemonic = $_M(c$, "getLabelWithoutMnemonic", 
function (label) {
if (label == null) {
return null;
}var index = label.indexOf ('&');
if (index == -1) {
return label;
}return label.substring (0, index) + ((index < label.length - 1) ? label.substring (index + 1) : "");
}, "~S");
c$.getMnemonic = $_M(c$, "getMnemonic", 
function (label) {
if (label == null) {
return ' ';
}var index = label.indexOf ('&');
if ((index == -1) || (index == label.length - 1)) {
return ' ';
}return label.charAt (index + 1);
}, "~S");
c$.map = $_M(c$, "map", 
function (button, key, label, menuMap) {
var mnemonic = J.console.GenericConsole.getMnemonic (label);
if (mnemonic != ' ') (button).setMnemonic (mnemonic);
menuMap.put (key, button);
}, "~O,~S,~S,java.util.Map");
Clazz.overrideMethod (c$, "notifyEnabled", 
function (type) {
switch (type) {
case J.constant.EnumCallback.ECHO:
case J.constant.EnumCallback.MEASURE:
case J.constant.EnumCallback.MESSAGE:
case J.constant.EnumCallback.PICK:
return true;
case J.constant.EnumCallback.ANIMFRAME:
case J.constant.EnumCallback.APPLETREADY:
case J.constant.EnumCallback.ATOMMOVED:
case J.constant.EnumCallback.CLICK:
case J.constant.EnumCallback.ERROR:
case J.constant.EnumCallback.EVAL:
case J.constant.EnumCallback.HOVER:
case J.constant.EnumCallback.LOADSTRUCT:
case J.constant.EnumCallback.MINIMIZATION:
case J.constant.EnumCallback.RESIZE:
case J.constant.EnumCallback.SCRIPT:
case J.constant.EnumCallback.SYNC:
break;
}
return false;
}, "J.constant.EnumCallback");
Clazz.overrideMethod (c$, "getText", 
function () {
return this.output.getText ();
});
Clazz.overrideMethod (c$, "sendConsoleEcho", 
function (strEcho) {
if (strEcho == null) {
this.updateLabels ();
this.outputMsg (null);
strEcho = this.defaultMessage;
}this.outputMsg (strEcho);
}, "~S");
$_M(c$, "outputMsg", 
($fz = function (message) {
if (message == null || message.length == 0) {
this.output.setText ("");
return;
}if (message.charAt (message.length - 1) != '\n') message += "\n";
this.output.append (message);
}, $fz.isPrivate = true, $fz), "~S");
$_M(c$, "clearContent", 
function (text) {
this.output.setText (text);
}, "~S");
Clazz.overrideMethod (c$, "sendConsoleMessage", 
function (strInfo) {
if (strInfo != null && this.output.getText ().startsWith (this.defaultMessage)) this.outputMsg (null);
this.outputMsg (strInfo);
}, "~S");
Clazz.overrideMethod (c$, "notifyCallback", 
function (type, data) {
var strInfo = (data == null || data[1] == null ? null : data[1].toString ());
switch (type) {
case J.constant.EnumCallback.ECHO:
this.sendConsoleEcho (strInfo);
break;
case J.constant.EnumCallback.MEASURE:
var mystatus = data[3];
if (mystatus.indexOf ("Picked") >= 0 || mystatus.indexOf ("Sequence") >= 0) this.sendConsoleMessage (strInfo);
 else if (mystatus.indexOf ("Completed") >= 0) this.sendConsoleEcho (strInfo.substring (strInfo.lastIndexOf (",") + 2, strInfo.length - 1));
break;
case J.constant.EnumCallback.MESSAGE:
this.sendConsoleMessage (data == null ? null : strInfo);
break;
case J.constant.EnumCallback.PICK:
this.sendConsoleMessage (strInfo);
break;
}
}, "J.constant.EnumCallback,~A");
Clazz.overrideMethod (c$, "setCallbackFunction", 
function (callbackType, callbackFunction) {
}, "~S,~S");
Clazz.overrideMethod (c$, "zap", 
function () {
});
$_M(c$, "recallCommand", 
function (up) {
var cmd = this.viewer.getSetHistory (up ? -1 : 1);
if (cmd == null) return;
this.input.setText (cmd);
}, "~B");
$_M(c$, "processKey", 
function (kcode, kid, isControlDown) {
var mode = 0;
switch (kid) {
case 401:
switch (kcode) {
case 9:
mode = 1;
if (this.input.getCaretPosition () == this.input.getText ().length) {
var cmd = this.completeCommand (this.getText ());
if (cmd != null) this.input.setText (cmd.$replace ('\t', ' '));
this.nTab++;
return mode;
}break;
case 27:
mode = 1;
this.input.setText ("");
break;
}
this.nTab = 0;
if (kcode == 10 && !isControlDown) {
this.execute (null);
return mode;
}if (kcode == 38 || kcode == 40) {
this.recallCommand (kcode == 38);
return mode;
}break;
case 402:
if (kcode == 10 && !isControlDown) return mode;
break;
}
return mode | 2;
}, "~N,~N,~B");
});
