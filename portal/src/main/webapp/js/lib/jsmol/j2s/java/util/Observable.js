$_L(["java.util.Vector"],"java.util.Observable",["java.lang.NullPointerException"],function(){
c$=$_C(function(){
this.observers=null;
this.changed=false;
$_Z(this,arguments);
},java.util,"Observable");
$_Y(c$,function(){
this.observers=new java.util.Vector();
});
$_K(c$,
function(){
});
$_M(c$,"addObserver",
function(observer){
if(observer==null){
throw new NullPointerException();
}if(!this.observers.contains(observer))this.observers.addElement(observer);
},"java.util.Observer");
$_M(c$,"clearChanged",
function(){
this.changed=false;
});
$_M(c$,"countObservers",
function(){
return this.observers.size();
});
$_M(c$,"deleteObserver",
function(observer){
this.observers.removeElement(observer);
},"java.util.Observer");
$_M(c$,"deleteObservers",
function(){
this.observers.setSize(0);
});
$_M(c$,"hasChanged",
function(){
return this.changed;
});
$_M(c$,"notifyObservers",
function(){
this.notifyObservers(null);
});
$_M(c$,"notifyObservers",
function(data){
if(this.changed){
var clone=this.observers.clone();
var size=clone.size();
for(var i=0;i<size;i++){
clone.elementAt(i).update(this,data);
}
this.clearChanged();
}},"~O");
$_M(c$,"setChanged",
function(){
this.changed=true;
});
});
