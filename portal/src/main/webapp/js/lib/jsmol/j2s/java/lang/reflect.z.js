/* http://j2s.sf.net/ */$_L(null,"java.lang.Void",["java.lang.RuntimeException"],function(){
c$=$_T(java.lang,"Void");
$_S(c$,
"TYPE",null);
{
java.lang.Void.TYPE=java.lang.Void;
}});
$_I(java.lang.reflect,"GenericDeclaration");
$_I(java.lang.reflect,"AnnotatedElement");
$_L(["java.lang.reflect.AnnotatedElement"],"java.lang.reflect.AccessibleObject",null,function(){
c$=$_T(java.lang.reflect,"AccessibleObject",null,java.lang.reflect.AnnotatedElement);
$_K(c$,
function(){
});
$_M(c$,"isAccessible",
function(){
return false;
});
c$.setAccessible=$_M(c$,"setAccessible",
function(objects,flag){
return;
},"~A,~B");
$_M(c$,"setAccessible",
function(flag){
return;
},"~B");
$_V(c$,"isAnnotationPresent",
function(annotationType){
return false;
},"Class");
$_V(c$,"getDeclaredAnnotations",
function(){
return new Array(0);
});
$_V(c$,"getAnnotations",
function(){
return new Array(0);
});
$_V(c$,"getAnnotation",
function(annotationType){
return null;
},"Class");
c$.marshallArguments=$_M(c$,"marshallArguments",
function(parameterTypes,args){
return null;
},"~A,~A");
$_M(c$,"invokeV",
function(receiver,args){
return;
},"~O,~A");
$_M(c$,"invokeL",
function(receiver,args){
return null;
},"~O,~A");
$_M(c$,"invokeI",
function(receiver,args){
return 0;
},"~O,~A");
$_M(c$,"invokeJ",
function(receiver,args){
return 0;
},"~O,~A");
$_M(c$,"invokeF",
function(receiver,args){
return 0.0;
},"~O,~A");
$_M(c$,"invokeD",
function(receiver,args){
return 0.0;
},"~O,~A");
c$.emptyArgs=c$.prototype.emptyArgs=new Array(0);
});
$_I(java.lang.reflect,"InvocationHandler");
c$=$_I(java.lang.reflect,"Member");
$_S(c$,
"PUBLIC",0,
"DECLARED",1);
$_L(null,"java.lang.reflect.Modifier",["java.lang.reflect.Method"],function(){
c$=$_T(java.lang.reflect,"Modifier");
$_K(c$,
function(){
});
c$.isAbstract=$_M(c$,"isAbstract",
function(modifiers){
return((modifiers&1024)!=0);
},"~N");
c$.isFinal=$_M(c$,"isFinal",
function(modifiers){
return((modifiers&16)!=0);
},"~N");
c$.isInterface=$_M(c$,"isInterface",
function(modifiers){
return((modifiers&512)!=0);
},"~N");
c$.isNative=$_M(c$,"isNative",
function(modifiers){
return((modifiers&256)!=0);
},"~N");
c$.isPrivate=$_M(c$,"isPrivate",
function(modifiers){
return((modifiers&2)!=0);
},"~N");
c$.isProtected=$_M(c$,"isProtected",
function(modifiers){
return((modifiers&4)!=0);
},"~N");
c$.isPublic=$_M(c$,"isPublic",
function(modifiers){
return((modifiers&1)!=0);
},"~N");
c$.isStatic=$_M(c$,"isStatic",
function(modifiers){
return((modifiers&8)!=0);
},"~N");
c$.isStrict=$_M(c$,"isStrict",
function(modifiers){
return((modifiers&2048)!=0);
},"~N");
c$.isSynchronized=$_M(c$,"isSynchronized",
function(modifiers){
return((modifiers&32)!=0);
},"~N");
c$.isTransient=$_M(c$,"isTransient",
function(modifiers){
return((modifiers&128)!=0);
},"~N");
c$.isVolatile=$_M(c$,"isVolatile",
function(modifiers){
return((modifiers&64)!=0);
},"~N");
c$.toString=$_M(c$,"toString",
function(modifiers){
var sb=new Array(0);
if(java.lang.reflect.Modifier.isPublic(modifiers))sb[sb.length]="public";
if(java.lang.reflect.Modifier.isProtected(modifiers))sb[sb.length]="protected";
if(java.lang.reflect.Modifier.isPrivate(modifiers))sb[sb.length]="private";
if(java.lang.reflect.Modifier.isAbstract(modifiers))sb[sb.length]="abstract";
if(java.lang.reflect.Modifier.isStatic(modifiers))sb[sb.length]="static";
if(java.lang.reflect.Modifier.isFinal(modifiers))sb[sb.length]="final";
if(java.lang.reflect.Modifier.isTransient(modifiers))sb[sb.length]="transient";
if(java.lang.reflect.Modifier.isVolatile(modifiers))sb[sb.length]="volatile";
if(java.lang.reflect.Modifier.isSynchronized(modifiers))sb[sb.length]="synchronized";
if(java.lang.reflect.Modifier.isNative(modifiers))sb[sb.length]="native";
if(java.lang.reflect.Modifier.isStrict(modifiers))sb[sb.length]="strictfp";
if(java.lang.reflect.Modifier.isInterface(modifiers))sb[sb.length]="interface";
if(sb.length>0){
return sb.join(" ");
}return"";
},"~N");
$_S(c$,
"PUBLIC",0x1,
"PRIVATE",0x2,
"PROTECTED",0x4,
"STATIC",0x8,
"FINAL",0x10,
"SYNCHRONIZED",0x20,
"VOLATILE",0x40,
"TRANSIENT",0x80,
"NATIVE",0x100,
"INTERFACE",0x200,
"ABSTRACT",0x400,
"STRICT",0x800,
"BRIDGE",0x40,
"VARARGS",0x80,
"SYNTHETIC",0x1000,
"ANNOTATION",0x2000,
"ENUM",0x4000);
});
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
$_L(["java.lang.reflect.AccessibleObject","$.Member"],"java.lang.reflect.Field",null,function(){
c$=$_T(java.lang.reflect,"Field",java.lang.reflect.AccessibleObject,java.lang.reflect.Member);
$_V(c$,"isSynthetic",
function(){
return false;
});
$_M(c$,"toGenericString",
function(){
return null;
});
$_M(c$,"isEnumConstant",
function(){
return false;
});
$_M(c$,"getGenericType",
function(){
return null;
});
$_V(c$,"equals",
function(object){
return false;
},"~O");
$_V(c$,"getDeclaringClass",
function(){
return null;
});
$_V(c$,"getName",
function(){
return null;
});
$_M(c$,"getType",
function(){
return null;
});
$_V(c$,"hashCode",
function(){
return 0;
});
$_V(c$,"toString",
function(){
return null;
});
});
$_L(["java.lang.reflect.AccessibleObject","$.GenericDeclaration","$.Member","java.lang.Void"],"java.lang.reflect.Method",null,function(){
c$=$_C(function(){
this.clazz=null;
this.name=null;
this.returnType=null;
this.parameterTypes=null;
this.exceptionTypes=null;
this.modifiers=0;
$_Z(this,arguments);
},java.lang.reflect,"Method",java.lang.reflect.AccessibleObject,[java.lang.reflect.GenericDeclaration,java.lang.reflect.Member]);
$_K(c$,
function(declaringClass,name,parameterTypes,returnType,checkedExceptions,modifiers){
$_R(this,java.lang.reflect.Method,[]);
this.clazz=declaringClass;
this.name=name;
this.parameterTypes=parameterTypes;
this.returnType=returnType;
this.exceptionTypes=checkedExceptions;
this.modifiers=modifiers;
},"Class,~S,~A,Class,~A,~N");
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
$_M(c$,"getGenericReturnType",
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
$_M(c$,"isBridge",
function(){
return false;
});
$_V(c$,"isSynthetic",
function(){
return false;
});
$_M(c$,"getDefaultValue",
function(){
return null;
});
$_V(c$,"equals",
function(object){
if(object!=null&&$_O(object,java.lang.reflect.Method)){
var other=object;
if((this.getDeclaringClass()===other.getDeclaringClass())&&(this.getName()===other.getName())){
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
return this.name;
});
$_M(c$,"getParameterTypes",
function(){
return this.parameterTypes;
});
$_M(c$,"getReturnType",
function(){
return this.returnType;
});
$_V(c$,"hashCode",
function(){
return this.getDeclaringClass().getName().hashCode()^this.getName().hashCode();
});
$_M(c$,"invoke",
function(receiver,args){
var m=this.clazz.prototype[this.getName()];
if(m==null){
m=this.clazz[this.getName()];
}
if(m!=null){
m.apply(receiver,args);
}else{

}
},"~O,~A");
$_V(c$,"toString",
function(){
return null;
});
});
