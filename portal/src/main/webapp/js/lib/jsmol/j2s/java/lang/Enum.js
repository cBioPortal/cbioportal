Clazz.load(null,"java.lang.Enum",["java.lang.CloneNotSupportedException","$.IllegalArgumentException","$.NullPointerException","java.security.AccessController","$.PrivilegedExceptionAction"],function(){
c$=Clazz.decorateAsClass(function(){
this.$name=null;
this.$ordinal=0;
Clazz.instantialize(this,arguments);
},java.lang,"Enum",null,[java.io.Serializable,Comparable]);
Clazz.makeConstructor(c$,
function(name,ordinal){
this.$name=name;
this.$ordinal=ordinal;
},"~S,~N");
Clazz.defineMethod(c$,"name",
function(){
return this.$name;
});
Clazz.defineMethod(c$,"ordinal",
function(){
return this.$ordinal;
});
Clazz.overrideMethod(c$,"toString",
function(){
return this.$name;
});
Clazz.overrideMethod(c$,"equals",
function(other){
return this===other;
},"~O");
Clazz.overrideMethod(c$,"hashCode",
function(){
return this.$ordinal+(this.$name==null?0:this.$name.hashCode());
});
Clazz.overrideMethod(c$,"clone",
function(){
throw new CloneNotSupportedException(("KA004"));
});
Clazz.overrideMethod(c$,"compareTo",
function(o){
return this.$ordinal-o.$ordinal;
},"~O");
Clazz.defineMethod(c$,"getDeclaringClass",
function(){
var myClass=this.getClass();
var mySuperClass=myClass.getSuperclass();
if(Enum===mySuperClass){
return myClass;
}return mySuperClass;
});
c$.$valueOf=Clazz.defineMethod(c$,"$valueOf",
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
c$.getValues=Clazz.defineMethod(c$,"getValues",
function(enumType){
try{
var values=java.security.AccessController.doPrivileged(((Clazz.isClassDefined("Enum$1")?0:java.lang.Enum.$Enum$1$()),Clazz.innerTypeInstance(Enum$1,this,Clazz.cloneFinals("enumType",enumType))));
return values.invoke(enumType,Clazz.castNullAs("Array"));
}catch(e){
if(Clazz.instanceOf(e,Exception)){
return null;
}else{
throw e;
}
}
},"Class");
c$.$Enum$1$=function(){
Clazz.pu$h(self.c$);
c$=Clazz.declareAnonymous(null,"Enum$1",null,java.security.PrivilegedExceptionAction);
Clazz.overrideMethod(c$,"run",
function(){
var valsMethod=this.f$.enumType.getMethod("values",null);
valsMethod.setAccessible(true);
return valsMethod;
});
c$=Clazz.p0p();
};
});
