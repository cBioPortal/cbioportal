$_L(["java.util.Map"],"java.util.MapEntry",null,function(){
c$=$_C(function(){
this.key=null;
this.value=null;
$_Z(this,arguments);
},java.util,"MapEntry",null,[java.util.Map.Entry,Cloneable]);
$_K(c$,
function(theKey){
this.key=theKey;
},"~O");
$_K(c$,
function(theKey,theValue){
this.key=theKey;
this.value=theValue;
},"~O,~O");
$_M(c$,"clone",
function(){
try{
return $_U(this,java.util.MapEntry,"clone",[]);
}catch(e){
if($_O(e,CloneNotSupportedException)){
return null;
}else{
throw e;
}
}
});
$_V(c$,"equals",
function(object){
if(this===object){
return true;
}if($_O(object,java.util.Map.Entry)){
var entry=object;
return(this.key==null?entry.getKey()==null:this.key.equals(entry.getKey()))&&(this.value==null?entry.getValue()==null:this.value.equals(entry.getValue()));
}return false;
},"~O");
$_V(c$,"getKey",
function(){
return this.key;
});
$_V(c$,"getValue",
function(){
return this.value;
});
$_V(c$,"hashCode",
function(){
return(this.key==null?0:this.key.hashCode())^(this.value==null?0:this.value.hashCode());
});
$_V(c$,"setValue",
function(object){
var result=this.value;
this.value=object;
return result;
},"~O");
$_V(c$,"toString",
function(){
return this.key+"="+this.value;
});
$_I(java.util.MapEntry,"Type");
});
