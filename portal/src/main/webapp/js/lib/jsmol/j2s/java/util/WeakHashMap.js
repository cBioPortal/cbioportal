$_L(["java.lang.ref.WeakReference","java.util.AbstractMap","$.Iterator","$.Map"],"java.util.WeakHashMap",["java.lang.IllegalArgumentException","$.IllegalStateException","java.lang.ref.ReferenceQueue","java.util.AbstractCollection","$.AbstractSet","$.Arrays","$.ConcurrentModificationException","$.NoSuchElementException"],function(){
c$=$_C(function(){
this.referenceQueue=null;
this.elementCount=0;
this.elementData=null;
this.loadFactor=0;
this.threshold=0;
this.modCount=0;
if(!$_D("java.util.WeakHashMap.HashIterator")){
java.util.WeakHashMap.$WeakHashMap$HashIterator$();
}
$_Z(this,arguments);
},java.util,"WeakHashMap",java.util.AbstractMap,java.util.Map);
c$.newEntryArray=$_M(c$,"newEntryArray",
($fz=function(size){
return new Array(size);
},$fz.isPrivate=true,$fz),"~N");
$_K(c$,
function(){
this.construct(16);
});
$_K(c$,
function(capacity){
$_R(this,java.util.WeakHashMap,[]);
if(capacity>=0){
this.elementCount=0;
this.elementData=java.util.WeakHashMap.newEntryArray(capacity==0?1:capacity);
this.loadFactor=7500;
this.computeMaxSize();
this.referenceQueue=new java.lang.ref.ReferenceQueue();
}else{
throw new IllegalArgumentException();
}},"~N");
$_K(c$,
function(capacity,loadFactor){
$_R(this,java.util.WeakHashMap,[]);
if(capacity>=0&&loadFactor>0){
this.elementCount=0;
this.elementData=java.util.WeakHashMap.newEntryArray(capacity==0?1:capacity);
this.loadFactor=Math.round((loadFactor*10000));
this.computeMaxSize();
this.referenceQueue=new java.lang.ref.ReferenceQueue();
}else{
throw new IllegalArgumentException();
}},"~N,~N");
$_K(c$,
function(map){
this.construct(map.size()<6?11:map.size()*2);
this.putAllImpl(map);
},"java.util.Map");
$_V(c$,"clear",
function(){
if(this.elementCount>0){
this.elementCount=0;
java.util.Arrays.fill(this.elementData,null);
this.modCount++;
while(this.referenceQueue.poll()!=null){
}
}});
$_M(c$,"computeMaxSize",
($fz=function(){
this.threshold=(Math.floor(this.elementData.length*this.loadFactor/10000));
},$fz.isPrivate=true,$fz));
$_V(c$,"containsKey",
function(key){
return this.getEntry(key)!=null;
},"~O");
$_V(c$,"entrySet",
function(){
this.poll();
return(($_D("java.util.WeakHashMap$1")?0:java.util.WeakHashMap.$WeakHashMap$1$()),$_N(java.util.WeakHashMap$1,this,null));
});
$_V(c$,"keySet",
function(){
this.poll();
if(this.$keySet==null){
this.$keySet=(($_D("java.util.WeakHashMap$2")?0:java.util.WeakHashMap.$WeakHashMap$2$()),$_N(java.util.WeakHashMap$2,this,null));
}return this.$keySet;
});
$_V(c$,"values",
function(){
this.poll();
if(this.valuesCollection==null){
this.valuesCollection=(($_D("java.util.WeakHashMap$3")?0:java.util.WeakHashMap.$WeakHashMap$3$()),$_N(java.util.WeakHashMap$3,this,null));
}return this.valuesCollection;
});
$_V(c$,"get",
function(key){
this.poll();
if(key!=null){
var index=(key.hashCode()&0x7FFFFFFF)%this.elementData.length;
var entry=this.elementData[index];
while(entry!=null){
if(key.equals(entry.get())){
return entry.value;
}entry=entry.$next;
}
return null;
}var entry=this.elementData[0];
while(entry!=null){
if(entry.isNull){
return entry.value;
}entry=entry.$next;
}
return null;
},"~O");
$_M(c$,"getEntry",
function(key){
this.poll();
if(key!=null){
var index=(key.hashCode()&0x7FFFFFFF)%this.elementData.length;
var entry=this.elementData[index];
while(entry!=null){
if(key.equals(entry.get())){
return entry;
}entry=entry.$next;
}
return null;
}var entry=this.elementData[0];
while(entry!=null){
if(entry.isNull){
return entry;
}entry=entry.$next;
}
return null;
},"~O");
$_V(c$,"containsValue",
function(value){
this.poll();
if(value!=null){
for(var i=this.elementData.length;--i>=0;){
var entry=this.elementData[i];
while(entry!=null){
var key=entry.get();
if((key!=null||entry.isNull)&&value.equals(entry.value)){
return true;
}entry=entry.$next;
}
}
}else{
for(var i=this.elementData.length;--i>=0;){
var entry=this.elementData[i];
while(entry!=null){
var key=entry.get();
if((key!=null||entry.isNull)&&entry.value==null){
return true;
}entry=entry.$next;
}
}
}return false;
},"~O");
$_V(c$,"isEmpty",
function(){
return this.size()==0;
});
$_M(c$,"poll",
function(){
var toRemove;
while((toRemove=this.referenceQueue.poll())!=null){
this.removeEntry(toRemove);
}
});
$_M(c$,"removeEntry",
function(toRemove){
var entry;
var last=null;
var index=(toRemove.hash&0x7FFFFFFF)%this.elementData.length;
entry=this.elementData[index];
while(entry!=null){
if(toRemove===entry){
this.modCount++;
if(last==null){
this.elementData[index]=entry.$next;
}else{
last.$next=entry.$next;
}this.elementCount--;
break;
}last=entry;
entry=entry.$next;
}
},"java.util.WeakHashMap.Entry");
$_V(c$,"put",
function(key,value){
this.poll();
var index=0;
var entry;
if(key!=null){
index=(key.hashCode()&0x7FFFFFFF)%this.elementData.length;
entry=this.elementData[index];
while(entry!=null&&!key.equals(entry.get())){
entry=entry.$next;
}
}else{
entry=this.elementData[0];
while(entry!=null&&!entry.isNull){
entry=entry.$next;
}
}if(entry==null){
this.modCount++;
if(++this.elementCount>this.threshold){
this.rehash();
index=key==null?0:(key.hashCode()&0x7FFFFFFF)%this.elementData.length;
}entry=new java.util.WeakHashMap.Entry(key,value,this.referenceQueue);
entry.$next=this.elementData[index];
this.elementData[index]=entry;
return null;
}var result=entry.value;
entry.value=value;
return result;
},"~O,~O");
$_M(c$,"rehash",
($fz=function(){
var length=this.elementData.length<<1;
if(length==0){
length=1;
}var newData=java.util.WeakHashMap.newEntryArray(length);
for(var i=0;i<this.elementData.length;i++){
var entry=this.elementData[i];
while(entry!=null){
var index=entry.isNull?0:(entry.hash&0x7FFFFFFF)%length;
var next=entry.$next;
entry.$next=newData[index];
newData[index]=entry;
entry=next;
}
}
this.elementData=newData;
this.computeMaxSize();
},$fz.isPrivate=true,$fz));
$_V(c$,"putAll",
function(map){
this.putAllImpl(map);
},"java.util.Map");
$_V(c$,"remove",
function(key){
this.poll();
var index=0;
var entry;
var last=null;
if(key!=null){
index=(key.hashCode()&0x7FFFFFFF)%this.elementData.length;
entry=this.elementData[index];
while(entry!=null&&!key.equals(entry.get())){
last=entry;
entry=entry.$next;
}
}else{
entry=this.elementData[0];
while(entry!=null&&!entry.isNull){
last=entry;
entry=entry.$next;
}
}if(entry!=null){
this.modCount++;
if(last==null){
this.elementData[index]=entry.$next;
}else{
last.$next=entry.$next;
}this.elementCount--;
return entry.value;
}return null;
},"~O");
$_V(c$,"size",
function(){
this.poll();
return this.elementCount;
});
$_M(c$,"putAllImpl",
($fz=function(map){
if(map.entrySet()!=null){
$_U(this,java.util.WeakHashMap,"putAll",[map]);
}},$fz.isPrivate=true,$fz),"java.util.Map");
c$.$WeakHashMap$HashIterator$=function(){
$_H();
c$=$_C(function(){
$_B(this,arguments);
this.position=0;
this.expectedModCount=0;
this.currentEntry=null;
this.nextEntry=null;
this.nextKey=null;
this.type=null;
$_Z(this,arguments);
},java.util.WeakHashMap,"HashIterator",null,java.util.Iterator);
$_K(c$,
function(a){
this.type=a;
this.expectedModCount=this.b$["java.util.WeakHashMap"].modCount;
},"java.util.WeakHashMap.Entry.Type");
$_V(c$,"hasNext",
function(){
if(this.nextEntry!=null){
return true;
}while(true){
if(this.nextEntry==null){
while(this.position<this.b$["java.util.WeakHashMap"].elementData.length){
if((this.nextEntry=this.b$["java.util.WeakHashMap"].elementData[this.position++])!=null){
break;
}}
if(this.nextEntry==null){
return false;
}}this.nextKey=this.nextEntry.get();
if(this.nextKey!=null||this.nextEntry.isNull){
return true;
}this.nextEntry=this.nextEntry.$next;
}
});
$_V(c$,"next",
function(){
if(this.expectedModCount==this.b$["java.util.WeakHashMap"].modCount){
if(this.hasNext()){
this.currentEntry=this.nextEntry;
this.nextEntry=this.currentEntry.$next;
var a=this.type.get(this.currentEntry);
this.nextKey=null;
return a;
}throw new java.util.NoSuchElementException();
}throw new java.util.ConcurrentModificationException();
});
$_V(c$,"remove",
function(){
if(this.expectedModCount==this.b$["java.util.WeakHashMap"].modCount){
if(this.currentEntry!=null){
this.b$["java.util.WeakHashMap"].removeEntry(this.currentEntry);
this.currentEntry=null;
this.expectedModCount++;
}else{
throw new IllegalStateException();
}}else{
throw new java.util.ConcurrentModificationException();
}});
c$=$_P();
};
c$.$WeakHashMap$1$=function(){
$_H();
c$=$_W(java.util,"WeakHashMap$1",java.util.AbstractSet);
$_V(c$,"size",
function(){
return this.b$["java.util.WeakHashMap"].size();
});
$_V(c$,"clear",
function(){
this.b$["java.util.WeakHashMap"].clear();
});
$_V(c$,"remove",
function(object){
if(this.contains(object)){
this.b$["java.util.WeakHashMap"].remove((object).getKey());
return true;
}return false;
},"~O");
$_V(c$,"contains",
function(object){
if($_O(object,java.util.Map.Entry)){
var entry=this.b$["java.util.WeakHashMap"].getEntry((object).getKey());
if(entry!=null){
var key=entry.get();
if(key!=null||entry.isNull){
return object.equals(entry);
}}}return false;
},"~O");
$_V(c$,"iterator",
function(){
return $_N(java.util.WeakHashMap.HashIterator,this,null,(($_D("java.util.WeakHashMap$1$1")?0:java.util.WeakHashMap.$WeakHashMap$1$1$()),$_N(java.util.WeakHashMap$1$1,this,null)));
});
c$=$_P();
};
c$.$WeakHashMap$1$1$=function(){
$_H();
c$=$_W(java.util,"WeakHashMap$1$1",null,java.util.WeakHashMap.Entry.Type);
$_V(c$,"get",
function(entry){
return entry;
},"java.util.Map.Entry");
c$=$_P();
};
c$.$WeakHashMap$2$=function(){
$_H();
c$=$_W(java.util,"WeakHashMap$2",java.util.AbstractSet);
$_V(c$,"contains",
function(object){
return this.b$["java.util.WeakHashMap"].containsKey(object);
},"~O");
$_V(c$,"size",
function(){
return this.b$["java.util.WeakHashMap"].size();
});
$_V(c$,"clear",
function(){
this.b$["java.util.WeakHashMap"].clear();
});
$_V(c$,"remove",
function(key){
if(this.b$["java.util.WeakHashMap"].containsKey(key)){
this.b$["java.util.WeakHashMap"].remove(key);
return true;
}return false;
},"~O");
$_V(c$,"iterator",
function(){
return $_N(java.util.WeakHashMap.HashIterator,this,null,(($_D("java.util.WeakHashMap$2$1")?0:java.util.WeakHashMap.$WeakHashMap$2$1$()),$_N(java.util.WeakHashMap$2$1,this,null)));
});
c$=$_P();
};
c$.$WeakHashMap$2$1$=function(){
$_H();
c$=$_W(java.util,"WeakHashMap$2$1",null,java.util.WeakHashMap.Entry.Type);
$_V(c$,"get",
function(entry){
return entry.getKey();
},"java.util.Map.Entry");
c$=$_P();
};
c$.$WeakHashMap$3$=function(){
$_H();
c$=$_W(java.util,"WeakHashMap$3",java.util.AbstractCollection);
$_V(c$,"size",
function(){
return this.b$["java.util.WeakHashMap"].size();
});
$_V(c$,"clear",
function(){
this.b$["java.util.WeakHashMap"].clear();
});
$_V(c$,"contains",
function(object){
return this.b$["java.util.WeakHashMap"].containsValue(object);
},"~O");
$_V(c$,"iterator",
function(){
return $_N(java.util.WeakHashMap.HashIterator,this,null,(($_D("java.util.WeakHashMap$3$1")?0:java.util.WeakHashMap.$WeakHashMap$3$1$()),$_N(java.util.WeakHashMap$3$1,this,null)));
});
c$=$_P();
};
c$.$WeakHashMap$3$1$=function(){
$_H();
c$=$_W(java.util,"WeakHashMap$3$1",null,java.util.WeakHashMap.Entry.Type);
$_V(c$,"get",
function(entry){
return entry.getValue();
},"java.util.Map.Entry");
c$=$_P();
};
$_H();
c$=$_C(function(){
this.hash=0;
this.isNull=false;
this.value=null;
this.$next=null;
$_Z(this,arguments);
},java.util.WeakHashMap,"Entry",java.lang.ref.WeakReference,java.util.Map.Entry);
$_K(c$,
function(a,b,c){
$_R(this,java.util.WeakHashMap.Entry,[a,c]);
this.isNull=a==null;
this.hash=this.isNull?0:a.hashCode();
this.value=b;
},"~O,~O,java.lang.ref.ReferenceQueue");
$_V(c$,"getKey",
function(){
return $_U(this,java.util.WeakHashMap.Entry,"get",[]);
});
$_V(c$,"getValue",
function(){
return this.value;
});
$_V(c$,"setValue",
function(a){
var b=this.value;
this.value=a;
return b;
},"~O");
$_V(c$,"equals",
function(a){
if(!($_O(a,java.util.Map.Entry))){
return false;
}var b=a;
var c=$_U(this,java.util.WeakHashMap.Entry,"get",[]);
return(c==null?c===b.getKey():c.equals(b.getKey()))&&(this.value==null?this.value===b.getValue():this.value.equals(b.getValue()));
},"~O");
$_V(c$,"hashCode",
function(){
return this.hash+(this.value==null?0:this.value.hashCode());
});
$_V(c$,"toString",
function(){
return $_U(this,java.util.WeakHashMap.Entry,"get",[])+"="+this.value;
});
$_I(java.util.WeakHashMap.Entry,"Type");
c$=$_P();
$_S(c$,
"DEFAULT_SIZE",16);
});
