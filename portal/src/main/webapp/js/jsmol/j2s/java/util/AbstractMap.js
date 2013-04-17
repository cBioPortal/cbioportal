$_L(["java.util.Map"],"java.util.AbstractMap",["java.lang.StringBuilder","$.UnsupportedOperationException","java.util.AbstractCollection","$.AbstractSet","$.Iterator"],function(){
c$=$_C(function(){
this.$keySet=null;
this.valuesCollection=null;
$_Z(this,arguments);
},java.util,"AbstractMap",null,java.util.Map);
$_K(c$,
function(){
});
$_V(c$,"clear",
function(){
this.entrySet().clear();
});
$_V(c$,"containsKey",
function(key){
var it=this.entrySet().iterator();
if(key!=null){
while(it.hasNext()){
if(key.equals(it.next().getKey())){
return true;
}}
}else{
while(it.hasNext()){
if(it.next().getKey()==null){
return true;
}}
}return false;
},"~O");
$_V(c$,"containsValue",
function(value){
var it=this.entrySet().iterator();
if(value!=null){
while(it.hasNext()){
if(value.equals(it.next().getValue())){
return true;
}}
}else{
while(it.hasNext()){
if(it.next().getValue()==null){
return true;
}}
}return false;
},"~O");
$_V(c$,"equals",
function(object){
if(this===object){
return true;
}if($_O(object,java.util.Map)){
var map=object;
if(this.size()!=map.size()){
return false;
}var objectSet=map.entrySet();
var it=this.entrySet().iterator();
while(it.hasNext()){
if(!objectSet.contains(it.next())){
return false;
}}
return true;
}return false;
},"~O");
$_V(c$,"get",
function(key){
var it=this.entrySet().iterator();
if(key!=null){
while(it.hasNext()){
var entry=it.next();
if(key.equals(entry.getKey())){
return entry.getValue();
}}
}else{
while(it.hasNext()){
var entry=it.next();
if(entry.getKey()==null){
return entry.getValue();
}}
}return null;
},"~O");
$_V(c$,"hashCode",
function(){
var result=0;
var it=this.entrySet().iterator();
while(it.hasNext()){
result+=it.next().hashCode();
}
return result;
});
$_V(c$,"isEmpty",
function(){
return this.size()==0;
});
$_V(c$,"keySet",
function(){
if(this.$keySet==null){
this.$keySet=(($_D("java.util.AbstractMap$1")?0:java.util.AbstractMap.$AbstractMap$1$()),$_N(java.util.AbstractMap$1,this,null));
}return this.$keySet;
});
$_V(c$,"put",
function(key,value){
throw new UnsupportedOperationException();
},"~O,~O");
$_V(c$,"putAll",
function(map){
for(var entry,$entry=map.entrySet().iterator();$entry.hasNext()&&((entry=$entry.next())||true);){
this.put(entry.getKey(),entry.getValue());
}
},"java.util.Map");
$_V(c$,"remove",
function(key){
var it=this.entrySet().iterator();
if(key!=null){
while(it.hasNext()){
var entry=it.next();
if(key.equals(entry.getKey())){
it.remove();
return entry.getValue();
}}
}else{
while(it.hasNext()){
var entry=it.next();
if(entry.getKey()==null){
it.remove();
return entry.getValue();
}}
}return null;
},"~O");
$_V(c$,"size",
function(){
return this.entrySet().size();
});
$_V(c$,"toString",
function(){
if(this.isEmpty()){
return"{}";
}var buffer=new StringBuilder(this.size()*28);
buffer.append('{');
var it=this.entrySet().iterator();
while(it.hasNext()){
var entry=it.next();
var key=entry.getKey();
if(key!==this){
buffer.append(key);
}else{
buffer.append("(this Map)");
}buffer.append('=');
var value=entry.getValue();
if(value!==this){
buffer.append(value);
}else{
buffer.append("(this Map)");
}if(it.hasNext()){
buffer.append(", ");
}}
buffer.append('}');
return buffer.toString();
});
$_V(c$,"values",
function(){
if(this.valuesCollection==null){
this.valuesCollection=(($_D("java.util.AbstractMap$2")?0:java.util.AbstractMap.$AbstractMap$2$()),$_N(java.util.AbstractMap$2,this,null));
}return this.valuesCollection;
});
$_M(c$,"clone",
function(){
var result=$_U(this,java.util.AbstractMap,"clone",[]);
result.$keySet=null;
result.valuesCollection=null;
return result;
});
c$.$AbstractMap$1$=function(){
$_H();
c$=$_W(java.util,"AbstractMap$1",java.util.AbstractSet);
$_V(c$,"contains",
function(object){
return this.b$["java.util.AbstractMap"].containsKey(object);
},"~O");
$_V(c$,"size",
function(){
return this.b$["java.util.AbstractMap"].size();
});
$_V(c$,"iterator",
function(){
return(($_D("java.util.AbstractMap$1$1")?0:java.util.AbstractMap.$AbstractMap$1$1$()),$_N(java.util.AbstractMap$1$1,this,null));
});
c$=$_P();
};
c$.$AbstractMap$1$1$=function(){
$_H();
c$=$_C(function(){
$_B(this,arguments);
this.setIterator=null;
$_Z(this,arguments);
},java.util,"AbstractMap$1$1",null,java.util.Iterator);
$_Y(c$,function(){
this.setIterator=this.b$["java.util.AbstractMap"].entrySet().iterator();
});
$_V(c$,"hasNext",
function(){
return this.setIterator.hasNext();
});
$_V(c$,"next",
function(){
return this.setIterator.next().getKey();
});
$_V(c$,"remove",
function(){
this.setIterator.remove();
});
c$=$_P();
};
c$.$AbstractMap$2$=function(){
$_H();
c$=$_W(java.util,"AbstractMap$2",java.util.AbstractCollection);
$_V(c$,"size",
function(){
return this.b$["java.util.AbstractMap"].size();
});
$_V(c$,"contains",
function(object){
return this.b$["java.util.AbstractMap"].containsValue(object);
},"~O");
$_V(c$,"iterator",
function(){
return(($_D("java.util.AbstractMap$2$1")?0:java.util.AbstractMap.$AbstractMap$2$1$()),$_N(java.util.AbstractMap$2$1,this,null));
});
c$=$_P();
};
c$.$AbstractMap$2$1$=function(){
$_H();
c$=$_C(function(){
$_B(this,arguments);
this.setIterator=null;
$_Z(this,arguments);
},java.util,"AbstractMap$2$1",null,java.util.Iterator);
$_Y(c$,function(){
this.setIterator=this.b$["java.util.AbstractMap"].entrySet().iterator();
});
$_V(c$,"hasNext",
function(){
return this.setIterator.hasNext();
});
$_V(c$,"next",
function(){
return this.setIterator.next().getValue();
});
$_V(c$,"remove",
function(){
this.setIterator.remove();
});
c$=$_P();
};
});
