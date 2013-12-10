$_L(["java.util.AbstractMap","$.AbstractSet","$.Iterator","$.Map","$.MapEntry"],"java.util.IdentityHashMap",["java.lang.IllegalArgumentException","$.IllegalStateException","java.util.AbstractCollection","$.ConcurrentModificationException","java.util.MapEntry.Type","java.util.NoSuchElementException"],function(){
c$=$_C(function(){
this.elementData=null;
this.$size=0;
this.threshold=0;
this.modCount=0;
$_Z(this,arguments);
},java.util,"IdentityHashMap",java.util.AbstractMap,[java.util.Map,java.io.Serializable,Cloneable]);
$_K(c$,
function(){
this.construct(21);
});
$_K(c$,
function(maxSize){
$_R(this,java.util.IdentityHashMap,[]);
if(maxSize>=0){
this.$size=0;
this.threshold=this.getThreshold(maxSize);
this.elementData=this.newElementArray(this.computeElementArraySize());
}else{
throw new IllegalArgumentException();
}},"~N");
$_M(c$,"getThreshold",
($fz=function(maxSize){
return maxSize>3?maxSize:3;
},$fz.isPrivate=true,$fz),"~N");
$_M(c$,"computeElementArraySize",
($fz=function(){
return(Math.floor((this.threshold*10000)/7500))*2;
},$fz.isPrivate=true,$fz));
$_M(c$,"newElementArray",
($fz=function(s){
return new Array(s);
},$fz.isPrivate=true,$fz),"~N");
$_K(c$,
function(map){
this.construct(map.size()<6?11:map.size()*2);
this.putAllImpl(map);
},"java.util.Map");
$_M(c$,"massageValue",
($fz=function(value){
return((value===java.util.IdentityHashMap.NULL_OBJECT)?null:value);
},$fz.isPrivate=true,$fz),"~O");
$_V(c$,"clear",
function(){
this.$size=0;
for(var i=0;i<this.elementData.length;i++){
this.elementData[i]=null;
}
this.modCount++;
});
$_V(c$,"containsKey",
function(key){
if(key==null){
key=java.util.IdentityHashMap.NULL_OBJECT;
}var index=this.findIndex(key,this.elementData);
return this.elementData[index]===key;
},"~O");
$_V(c$,"containsValue",
function(value){
if(value==null){
value=java.util.IdentityHashMap.NULL_OBJECT;
}for(var i=1;i<this.elementData.length;i=i+2){
if(this.elementData[i]===value){
return true;
}}
return false;
},"~O");
$_V(c$,"get",
function(key){
if(key==null){
key=java.util.IdentityHashMap.NULL_OBJECT;
}var index=this.findIndex(key,this.elementData);
if(this.elementData[index]===key){
var result=this.elementData[index+1];
return this.massageValue(result);
}return null;
},"~O");
$_M(c$,"getEntry",
($fz=function(key){
if(key==null){
key=java.util.IdentityHashMap.NULL_OBJECT;
}var index=this.findIndex(key,this.elementData);
if(this.elementData[index]===key){
return this.getEntry(index);
}return null;
},$fz.isPrivate=true,$fz),"~O");
$_M(c$,"getEntry",
($fz=function(index){
var key=this.elementData[index];
var value=this.elementData[index+1];
if(key===java.util.IdentityHashMap.NULL_OBJECT){
key=null;
}if(value===java.util.IdentityHashMap.NULL_OBJECT){
value=null;
}return new java.util.IdentityHashMap.IdentityHashMapEntry(key,value);
},$fz.isPrivate=true,$fz),"~N");
$_M(c$,"findIndex",
($fz=function(key,array){
var length=array.length;
var index=this.getModuloHash(key,length);
var last=(index+length-2)%length;
while(index!=last){
if(array[index]===key||(array[index]==null)){
break;
}index=(index+2)%length;
}
return index;
},$fz.isPrivate=true,$fz),"~O,~A");
$_M(c$,"getModuloHash",
($fz=function(key,length){
return((System.identityHashCode(key)&0x7FFFFFFF)%(Math.floor(length/2)))*2;
},$fz.isPrivate=true,$fz),"~O,~N");
$_V(c$,"put",
function(key,value){
var _key=key;
var _value=value;
if(_key==null){
_key=java.util.IdentityHashMap.NULL_OBJECT;
}if(_value==null){
_value=java.util.IdentityHashMap.NULL_OBJECT;
}var index=this.findIndex(_key,this.elementData);
if(this.elementData[index]!==_key){
this.modCount++;
if(++this.$size>this.threshold){
this.rehash();
index=this.findIndex(_key,this.elementData);
}this.elementData[index]=_key;
this.elementData[index+1]=null;
}var result=this.elementData[index+1];
this.elementData[index+1]=_value;
return this.massageValue(result);
},"~O,~O");
$_V(c$,"putAll",
function(map){
this.putAllImpl(map);
},"java.util.Map");
$_M(c$,"rehash",
($fz=function(){
var newlength=this.elementData.length<<1;
if(newlength==0){
newlength=1;
}var newData=this.newElementArray(newlength);
for(var i=0;i<this.elementData.length;i=i+2){
var key=this.elementData[i];
if(key!=null){
var index=this.findIndex(key,newData);
newData[index]=key;
newData[index+1]=this.elementData[i+1];
}}
this.elementData=newData;
this.computeMaxSize();
},$fz.isPrivate=true,$fz));
$_M(c$,"computeMaxSize",
($fz=function(){
this.threshold=(Math.floor((Math.floor(this.elementData.length/ 2)) * 7500 /10000));
},$fz.isPrivate=true,$fz));
$_V(c$,"remove",
function(key){
if(key==null){
key=java.util.IdentityHashMap.NULL_OBJECT;
}var hashedOk;
var index;
var next;
var hash;
var result;
var object;
index=next=this.findIndex(key,this.elementData);
if(this.elementData[index]!==key){
return null;
}result=this.elementData[index+1];
var length=this.elementData.length;
while(true){
next=(next+2)%length;
object=this.elementData[next];
if(object==null){
break;
}hash=this.getModuloHash(object,length);
hashedOk=hash>index;
if(next<index){
hashedOk=hashedOk||(hash<=next);
}else{
hashedOk=hashedOk&&(hash<=next);
}if(!hashedOk){
this.elementData[index]=object;
this.elementData[index+1]=this.elementData[next+1];
index=next;
}}
this.$size--;
this.modCount++;
this.elementData[index]=null;
this.elementData[index+1]=null;
return this.massageValue(result);
},"~O");
$_V(c$,"entrySet",
function(){
return new java.util.IdentityHashMap.IdentityHashMapEntrySet(this);
});
$_V(c$,"keySet",
function(){
if(this.$keySet==null){
this.$keySet=(($_D("java.util.IdentityHashMap$1")?0:java.util.IdentityHashMap.$IdentityHashMap$1$()),$_N(java.util.IdentityHashMap$1,this,null));
}return this.$keySet;
});
$_V(c$,"values",
function(){
if(this.valuesCollection==null){
this.valuesCollection=(($_D("java.util.IdentityHashMap$2")?0:java.util.IdentityHashMap.$IdentityHashMap$2$()),$_N(java.util.IdentityHashMap$2,this,null));
}return this.valuesCollection;
});
$_V(c$,"equals",
function(object){
if(this===object){
return true;
}if($_O(object,java.util.Map)){
var map=object;
if(this.size()!=map.size()){
return false;
}var set=this.entrySet();
return set.equals(map.entrySet());
}return false;
},"~O");
$_M(c$,"clone",
function(){
try{
return $_U(this,java.util.IdentityHashMap,"clone",[]);
}catch(e){
if($_O(e,CloneNotSupportedException)){
return null;
}else{
throw e;
}
}
});
$_V(c$,"isEmpty",
function(){
return this.$size==0;
});
$_V(c$,"size",
function(){
return this.$size;
});
$_M(c$,"putAllImpl",
($fz=function(map){
if(map.entrySet()!=null){
$_U(this,java.util.IdentityHashMap,"putAll",[map]);
}},$fz.isPrivate=true,$fz),"java.util.Map");
c$.$IdentityHashMap$1$=function(){
$_H();
c$=$_W(java.util,"IdentityHashMap$1",java.util.AbstractSet);
$_V(c$,"contains",
function(object){
return this.b$["java.util.IdentityHashMap"].containsKey(object);
},"~O");
$_V(c$,"size",
function(){
return this.b$["java.util.IdentityHashMap"].size();
});
$_V(c$,"clear",
function(){
this.b$["java.util.IdentityHashMap"].clear();
});
$_V(c$,"remove",
function(key){
if(this.b$["java.util.IdentityHashMap"].containsKey(key)){
this.b$["java.util.IdentityHashMap"].remove(key);
return true;
}return false;
},"~O");
$_V(c$,"iterator",
function(){
return new java.util.IdentityHashMap.IdentityHashMapIterator((($_D("java.util.IdentityHashMap$1$1")?0:java.util.IdentityHashMap.$IdentityHashMap$1$1$()),$_N(java.util.IdentityHashMap$1$1,this,null)),this.b$["java.util.IdentityHashMap"]);
});
c$=$_P();
};
c$.$IdentityHashMap$1$1$=function(){
$_H();
c$=$_W(java.util,"IdentityHashMap$1$1",null,java.util.MapEntry.Type);
$_V(c$,"get",
function(entry){
return entry.key;
},"java.util.MapEntry");
c$=$_P();
};
c$.$IdentityHashMap$2$=function(){
$_H();
c$=$_W(java.util,"IdentityHashMap$2",java.util.AbstractCollection);
$_V(c$,"contains",
function(object){
return this.b$["java.util.IdentityHashMap"].containsValue(object);
},"~O");
$_V(c$,"size",
function(){
return this.b$["java.util.IdentityHashMap"].size();
});
$_V(c$,"clear",
function(){
this.b$["java.util.IdentityHashMap"].clear();
});
$_V(c$,"iterator",
function(){
return new java.util.IdentityHashMap.IdentityHashMapIterator((($_D("java.util.IdentityHashMap$2$1")?0:java.util.IdentityHashMap.$IdentityHashMap$2$1$()),$_N(java.util.IdentityHashMap$2$1,this,null)),this.b$["java.util.IdentityHashMap"]);
});
$_V(c$,"remove",
function(object){
var it=this.iterator();
while(it.hasNext()){
if(object===it.next()){
it.remove();
return true;
}}
return false;
},"~O");
c$=$_P();
};
c$.$IdentityHashMap$2$1$=function(){
$_H();
c$=$_W(java.util,"IdentityHashMap$2$1",null,java.util.MapEntry.Type);
$_V(c$,"get",
function(entry){
return entry.value;
},"java.util.MapEntry");
c$=$_P();
};
$_H();
c$=$_T(java.util.IdentityHashMap,"IdentityHashMapEntry",java.util.MapEntry);
$_V(c$,"equals",
function(a){
if(this===a){
return true;
}if($_O(a,java.util.Map.Entry)){
var b=a;
return(this.key===b.getKey())&&(this.value===b.getValue());
}return false;
},"~O");
$_V(c$,"hashCode",
function(){
return System.identityHashCode(this.key)^System.identityHashCode(this.value);
});
$_V(c$,"toString",
function(){
return this.key+"="+this.value;
});
c$=$_P();
$_H();
c$=$_C(function(){
this.position=0;
this.lastPosition=0;
this.associatedMap=null;
this.expectedModCount=0;
this.type=null;
this.canRemove=false;
$_Z(this,arguments);
},java.util.IdentityHashMap,"IdentityHashMapIterator",null,java.util.Iterator);
$_K(c$,
function(a,b){
this.associatedMap=b;
this.type=a;
this.expectedModCount=b.modCount;
},"java.util.MapEntry.Type,java.util.IdentityHashMap");
$_V(c$,"hasNext",
function(){
while(this.position<this.associatedMap.elementData.length){
if(this.associatedMap.elementData[this.position]==null){
this.position+=2;
}else{
return true;
}}
return false;
});
$_M(c$,"checkConcurrentMod",
function(){
if(this.expectedModCount!=this.associatedMap.modCount){
throw new java.util.ConcurrentModificationException();
}});
$_V(c$,"next",
function(){
this.checkConcurrentMod();
if(!this.hasNext()){
throw new java.util.NoSuchElementException();
}var a=this.associatedMap.getEntry(this.position);
this.lastPosition=this.position;
this.position+=2;
this.canRemove=true;
return this.type.get(a);
});
$_V(c$,"remove",
function(){
this.checkConcurrentMod();
if(!this.canRemove){
throw new IllegalStateException();
}this.canRemove=false;
this.associatedMap.remove(this.associatedMap.elementData[this.lastPosition]);
this.position=this.lastPosition;
this.expectedModCount++;
});
c$=$_P();
$_H();
c$=$_C(function(){
this.associatedMap=null;
$_Z(this,arguments);
},java.util.IdentityHashMap,"IdentityHashMapEntrySet",java.util.AbstractSet);
$_K(c$,
function(a){
$_R(this,java.util.IdentityHashMap.IdentityHashMapEntrySet,[]);
this.associatedMap=a;
},"java.util.IdentityHashMap");
$_M(c$,"hashMap",
function(){
return this.associatedMap;
});
$_V(c$,"size",
function(){
return this.associatedMap.$size;
});
$_V(c$,"clear",
function(){
this.associatedMap.clear();
});
$_V(c$,"remove",
function(a){
if(this.contains(a)){
this.associatedMap.remove((a).getKey());
return true;
}return false;
},"~O");
$_V(c$,"contains",
function(a){
if($_O(a,java.util.Map.Entry)){
var b=this.associatedMap.getEntry((a).getKey());
return b!=null&&b.equals(a);
}return false;
},"~O");
$_V(c$,"iterator",
function(){
return new java.util.IdentityHashMap.IdentityHashMapIterator((($_D("java.util.IdentityHashMap$IdentityHashMapEntrySet$1")?0:java.util.IdentityHashMap.IdentityHashMapEntrySet.$IdentityHashMap$IdentityHashMapEntrySet$1$()),$_N(java.util.IdentityHashMap$IdentityHashMapEntrySet$1,this,null)),this.associatedMap);
});
c$.$IdentityHashMap$IdentityHashMapEntrySet$1$=function(){
$_H();
c$=$_W(java.util,"IdentityHashMap$IdentityHashMapEntrySet$1",null,java.util.MapEntry.Type);
$_V(c$,"get",
function(a){
return a;
},"java.util.MapEntry");
c$=$_P();
};
c$=$_P();
$_S(c$,
"DEFAULT_MAX_SIZE",21,
"loadFactor",7500);
c$.NULL_OBJECT=c$.prototype.NULL_OBJECT=new JavaObject();
});
