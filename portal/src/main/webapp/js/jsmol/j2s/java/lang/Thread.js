$_L(null,"java.lang.Thread",["java.lang.IllegalArgumentException","$.ThreadGroup","java.util.Date"],function(){
c$=$_C(function(){
this.target=null;
this.group=null;
this.name=null;
this.priority=0;
$_Z(this,arguments);
},java.lang,"Thread",null,Runnable);
c$.currentThread=$_M(c$,"currentThread",
function(){
if(Thread.J2S_THREAD==null){
($t$=Thread.J2S_THREAD=new Thread(),Thread.prototype.J2S_THREAD=Thread.J2S_THREAD,$t$);
}return Thread.J2S_THREAD;
});
c$.sleep=$_M(c$,"sleep",
function(millis){
Clazz.alert("Thread.sleep is not implemented in Java2Script!");
},"~N");
$_K(c$,
function(){
});
$_K(c$,
function(target){
this.init(null,target,"Thread-"+new java.util.Date().getTime()+Math.random(),0);
},"Runnable");
$_K(c$,
function(group,target){
this.init(group,target,"Thread-"+new java.util.Date().getTime()+Math.random(),0);
},"ThreadGroup,Runnable");
$_K(c$,
function(name){
this.init(null,null,name,0);
},"~S");
$_K(c$,
function(group,name){
this.init(group,null,name,0);
},"ThreadGroup,~S");
$_K(c$,
function(target,name){
this.init(null,target,name,0);
},"Runnable,~S");
$_K(c$,
function(group,target,name){
this.init(group,target,name,0);
},"ThreadGroup,Runnable,~S");
$_K(c$,
function(group,target,name,stackSize){
this.init(group,target,name,stackSize);
},"ThreadGroup,Runnable,~S,~N");
$_M(c$,"init",
($fz=function(g,target,name,stackSize){
if(g==null){
g=new ThreadGroup();
}this.group=g;
this.target=target;
this.name=name;
this.priority=5;
},$fz.isPrivate=true,$fz),"ThreadGroup,Runnable,~S,~N");
$_M(c$,"start",
function(){
window.setTimeout((function(runnable){
return function(){
runnable.run();
};
})(this),0);
});
$_M(c$,"run",
function(){
if(this.target!=null){
this.target.run();
}});
$_M(c$,"setPriority",
function(newPriority){
if(newPriority>10||newPriority<1){
throw new IllegalArgumentException();
}this.priority=newPriority;
},"~N");
$_M(c$,"getPriority",
function(){
return this.priority;
});


$_M(c$,"interrupt",
function(){
 //not implemented
});

$_M(c$,"setName",
function(name){
this.name=name;
},"~S");
$_M(c$,"getName",
function(){
return String.valueOf(this.name);
});
$_M(c$,"getThreadGroup",
function(){
return this.group;
});
$_V(c$,"toString",
function(){
var group=this.getThreadGroup();
if(group!=null){
return"Thread["+this.getName()+","+this.getPriority()+","+group.getName()+"]";
}else{
return"Thread["+this.getName()+","+this.getPriority()+","+""+"]";
}});
$_S(c$,
"MIN_PRIORITY",1,
"NORM_PRIORITY",5,
"MAX_PRIORITY",10,
"J2S_THREAD",null);
});
