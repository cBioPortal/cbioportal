$_L(["java.lang.reflect.AccessibleObject","$.GenericDeclaration","$.Member","java.lang.Void"],"java.lang.reflect.Constructor",null,function(){
c$=$_C(function(){
this.clazz=null;
this.parameterTypes=null;
this.exceptionTypes=null;
this.modifiers=0;
$_Z(this,arguments);
},java.lang.reflect,"Constructor",java.lang.reflect.AccessibleObject,[java.lang.reflect.GenericDeclaration,java.lang.reflect.Member]);
$_K(c$,
function(declaringClass,parameterTypes,checkedExceptions,modifiers){
$_R(this,java.lang.reflect.Constructor,[]);
this.clazz=declaringClass;
this.parameterTypes=parameterTypes;
this.exceptionTypes=checkedExceptions;
this.modifiers=modifiers;
},"Class,~A,~A,~N");
$_V(c$,"getTypeParameters",
function(){
return null;
});
$_M(c$,"toGenericString",
function(){
return null;
});
$_M(c$,"getGenericParameterTypes",
function(){
return null;
});
$_M(c$,"getGenericExceptionTypes",
function(){
return null;
});
$_M(c$,"getParameterAnnotations",
function(){
return null;
});
$_M(c$,"isVarArgs",
function(){
return false;
});
$_V(c$,"isSynthetic",
function(){
return false;
});
$_V(c$,"equals",
function(object){
if(object!=null&&$_O(object,java.lang.reflect.Constructor)){
var other=object;
if(this.getDeclaringClass()===other.getDeclaringClass()){
var params1=this.parameterTypes;
var params2=other.parameterTypes;
if(params1.length==params2.length){
for(var i=0;i<params1.length;i++){
if(params1[i]!==params2[i])return false;
}
return true;
}}}return false;
},"~O");
$_V(c$,"getDeclaringClass",
function(){
return this.clazz;
});
$_M(c$,"getExceptionTypes",
function(){
return this.exceptionTypes;
});
$_V(c$,"getModifiers",
function(){
return this.modifiers;
});
$_V(c$,"getName",
function(){
return this.getDeclaringClass().getName();
});
$_M(c$,"getParameterTypes",
function(){
return this.parameterTypes;
});
$_V(c$,"hashCode",
function(){
return this.getDeclaringClass().getName().hashCode();
});
$_M(c$,"newInstance",
function(args){
var instance=new this.clazz($_G);
$_Z(instance,args);
return instance;
},"~A");
$_V(c$,"toString",
function(){
return null;
});
});
