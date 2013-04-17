$_L(null,"java.lang.ThreadGroup",["java.lang.NullPointerException","$.Thread"],function(){
c$=$_C(function(){
this.parent=null;
this.name=null;
this.maxPriority=0;
$_Z(this,arguments);
},java.lang,"ThreadGroup");
$_K(c$,
function(){
this.name="system";
this.maxPriority=10;
});
$_K(c$,
function(name){
this.construct(Thread.currentThread().getThreadGroup(),name);
},"~S");
$_K(c$,
function(parent,name){
if(parent==null){
throw new NullPointerException();
}this.name=name;
this.parent=parent;
this.maxPriority=10;
},"ThreadGroup,~S");
$_M(c$,"getName",
function(){
return this.name;
});
$_M(c$,"getParent",
function(){
return this.parent;
});
$_M(c$,"getMaxPriority",
function(){
return this.maxPriority;
});
});
