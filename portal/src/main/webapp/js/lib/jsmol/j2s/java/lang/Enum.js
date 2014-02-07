$_L(null,"java.lang.Enum",["java.lang.CloneNotSupportedException","$.IllegalArgumentException","$.NullPointerException","java.security.AccessController","$.PrivilegedExceptionAction"],function(){
c$=$_C(function(){
this.$name=null;
this.$ordinal=0;
$_Z(this,arguments);
},java.lang,"Enum",null,[java.io.Serializable,Comparable]);
$_K(c$,
function(name,ordinal){
this.$name=name;
this.$ordinal=ordinal;
},"~S,~N");
$_M(c$,"name",
function(){
return this.$name;
});
$_M(c$,"ordinal",
function(){
return this.$ordinal;
});
$_V(c$,"toString",
function(){
return this.$name;
});
$_V(c$,"equals",
function(other){
return this===other;
},"~O");
$_V(c$,"hashCode",
function(){
return this.$ordinal+(this.$name==null?0:this.$name.hashCode());
});
$_V(c$,"clone",
function(){
throw new CloneNotSupportedException(("KA004"));
});
$_V(c$,"compareTo",
function(o){
return this.$ordinal-o.$ordinal;
},"~O");
$_M(c$,"getDeclaringClass",
function(){
var myClass=this.getClass();
var mySuperClass=myClass.getSuperclass();
if(Enum===mySuperClass){
return myClass;
}return mySuperClass;
});
c$.$valueOf=$_M(c$,"$valueOf",
function(enumType,name){
if((enumType==null)||(name==null)){
throw new NullPointerException(("KA001"));
}var values=Enum.getValues(enumType);
if(values==null){
throw new IllegalArgumentException(("KA005"));
}for(var enumConst,$enumConst=0,$$enumConst=values;$enumConst<$$enumConst.length&&((enumConst=$$enumConst[$enumConst])||true);$enumConst++){
if(enumConst.$name.equals(name)){
return enumConst;
}}
throw new IllegalArgumentException(("KA006"));
},"Class,~S");
c$.getValues=$_M(c$,"getValues",
function(enumType){
try{
var values=java.security.AccessController.doPrivileged((($_D("Enum$1")?0:java.lang.Enum.$Enum$1$()),$_N(Enum$1,this,$_F("enumType",enumType))));
return values.invoke(enumType,Clazz.castNullAs("Array"));
}catch(e){
if($_O(e,Exception)){
return null;
}else{
throw e;
}
}
},"Class");
c$.$Enum$1$=function(){
$_H();
c$=$_W(null,"Enum$1",null,java.security.PrivilegedExceptionAction);
$_V(c$,"run",
function(){
var valsMethod=this.f$.enumType.getMethod("values",null);
valsMethod.setAccessible(true);
return valsMethod;
});
c$=$_P();
};
});
