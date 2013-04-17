$_L(["java.util.AbstractList","$.List","$.RandomAccess"],"java.util.Vector",["java.lang.ArrayIndexOutOfBoundsException","$.IllegalArgumentException","$.IndexOutOfBoundsException","$.StringBuffer","java.lang.reflect.Array","java.util.Arrays","$.Collections","$.Enumeration","$.NoSuchElementException"],function(){
c$=$_C(function(){
this.elementCount=0;
this.elementData=null;
this.capacityIncrement=0;
$_Z(this,arguments);
},java.util,"Vector",java.util.AbstractList,[java.util.List,java.util.RandomAccess,Cloneable,java.io.Serializable]);
$_K(c$,
function(){
this.construct(10,0);
});
$_K(c$,
function(capacity){
this.construct(capacity,0);
},"~N");
$_K(c$,
function(capacity,capacityIncrement){
$_R(this,java.util.Vector,[]);
this.elementCount=0;
try{
this.elementData=this.newElementArray(capacity);
}catch(e){
if($_O(e,NegativeArraySizeException)){
throw new IllegalArgumentException();
}else{
throw e;
}
}
this.capacityIncrement=capacityIncrement;
},"~N,~N");
$_K(c$,
function(collection){
this.construct(collection.size(),0);
var it=collection.iterator();
while(it.hasNext()){
this.elementData[this.elementCount++]=it.next();
}
},"java.util.Collection");
$_M(c$,"newElementArray",
($fz=function(size){
return new Array(size);
},$fz.isPrivate=true,$fz),"~N");
$_M(c$,"add",
function(location,object){
this.insertElementAt(object,location);
},"~N,~O");
$_M(c$,"add",
function(object){
this.addElement(object);
return true;
},"~O");
$_M(c$,"addAll",
function(location,collection){
if(0<=location&&location<=this.elementCount){
var size=collection.size();
if(size==0){
return false;
}var required=size-(this.elementData.length-this.elementCount);
if(required>0){
this.growBy(required);
}var count=this.elementCount-location;
if(count>0){
System.arraycopy(this.elementData,location,this.elementData,location+size,count);
}var it=collection.iterator();
while(it.hasNext()){
this.elementData[location++]=it.next();
}
this.elementCount+=size;
this.modCount++;
return true;
}throw new ArrayIndexOutOfBoundsException(location);
},"~N,java.util.Collection");
$_M(c$,"addAll",
function(collection){
return this.addAll(this.elementCount,collection);
},"java.util.Collection");
$_M(c$,"addElement",
function(object){
if(this.elementCount==this.elementData.length){
this.growByOne();
}this.elementData[this.elementCount++]=object;
this.modCount++;
},"~O");
$_M(c$,"capacity",
function(){
return this.elementData.length;
});
$_V(c$,"clear",
function(){
this.removeAllElements();
});
$_M(c$,"clone",
function(){
try{
var vector=$_U(this,java.util.Vector,"clone",[]);
vector.elementData=this.elementData.clone();
return vector;
}catch(e){
if($_O(e,CloneNotSupportedException)){
return null;
}else{
throw e;
}
}
});
$_V(c$,"contains",
function(object){
return this.indexOf(object,0)!=-1;
},"~O");
$_M(c$,"copyInto",
function(elements){
System.arraycopy(this.elementData,0,elements,0,this.elementCount);
},"~A");
$_M(c$,"elementAt",
function(location){
if(location<this.elementCount){
return this.elementData[location];
}throw new ArrayIndexOutOfBoundsException(location);
},"~N");
$_M(c$,"elements",
function(){
return(($_D("java.util.Vector$1")?0:java.util.Vector.$Vector$1$()),$_N(java.util.Vector$1,this,null));
});
$_M(c$,"ensureCapacity",
function(minimumCapacity){
if(this.elementData.length<minimumCapacity){
var next=(this.capacityIncrement<=0?this.elementData.length:this.capacityIncrement)+this.elementData.length;
this.grow(minimumCapacity>next?minimumCapacity:next);
}},"~N");
$_V(c$,"equals",
function(object){
if(this===object){
return true;
}if($_O(object,java.util.List)){
var list=object;
if(list.size()!=this.size()){
return false;
}var index=0;
var it=list.iterator();
while(it.hasNext()){
var e1=this.elementData[index++];
var e2=it.next();
if(!(e1==null?e2==null:e1.equals(e2))){
return false;
}}
return true;
}return false;
},"~O");
$_M(c$,"firstElement",
function(){
if(this.elementCount>0){
return this.elementData[0];
}throw new java.util.NoSuchElementException();
});
$_V(c$,"get",
function(location){
return this.elementAt(location);
},"~N");
$_M(c$,"grow",
($fz=function(newCapacity){
var newData=this.newElementArray(newCapacity);
System.arraycopy(this.elementData,0,newData,0,this.elementCount);
this.elementData=newData;
},$fz.isPrivate=true,$fz),"~N");
$_M(c$,"growByOne",
($fz=function(){
var adding=0;
if(this.capacityIncrement<=0){
if((adding=this.elementData.length)==0){
adding=1;
}}else{
adding=this.capacityIncrement;
}var newData=this.newElementArray(this.elementData.length+adding);
System.arraycopy(this.elementData,0,newData,0,this.elementCount);
this.elementData=newData;
},$fz.isPrivate=true,$fz));
$_M(c$,"growBy",
($fz=function(required){
var adding=0;
if(this.capacityIncrement<=0){
if((adding=this.elementData.length)==0){
adding=required;
}while(adding<required){
adding+=adding;
}
}else{
adding=(Math.floor(required/this.capacityIncrement))*this.capacityIncrement;
if(adding<required){
adding+=this.capacityIncrement;
}}var newData=this.newElementArray(this.elementData.length+adding);
System.arraycopy(this.elementData,0,newData,0,this.elementCount);
this.elementData=newData;
},$fz.isPrivate=true,$fz),"~N");
$_V(c$,"hashCode",
function(){
var result=1;
for(var i=0;i<this.elementCount;i++){
result=(31*result)+(this.elementData[i]==null?0:this.elementData[i].hashCode());
}
return result;
});
$_M(c$,"indexOf",
function(object){
return this.indexOf(object,0);
},"~O");
$_M(c$,"indexOf",
function(object,location){
if(object!=null){
for(var i=location;i<this.elementCount;i++){
if(object.equals(this.elementData[i])){
return i;
}}
}else{
for(var i=location;i<this.elementCount;i++){
if(this.elementData[i]==null){
return i;
}}
}return-1;
},"~O,~N");
$_M(c$,"insertElementAt",
function(object,location){
if(0<=location&&location<=this.elementCount){
if(this.elementCount==this.elementData.length){
this.growByOne();
}var count=this.elementCount-location;
if(count>0){
System.arraycopy(this.elementData,location,this.elementData,location+1,count);
}this.elementData[location]=object;
this.elementCount++;
this.modCount++;
}else{
throw new ArrayIndexOutOfBoundsException(location);
}},"~O,~N");
$_V(c$,"isEmpty",
function(){
return this.elementCount==0;
});
$_M(c$,"lastElement",
function(){
try{
return this.elementData[this.elementCount-1];
}catch(e){
if($_O(e,IndexOutOfBoundsException)){
throw new java.util.NoSuchElementException();
}else{
throw e;
}
}
});
$_M(c$,"lastIndexOf",
function(object){
return this.lastIndexOf(object,this.elementCount-1);
},"~O");
$_M(c$,"lastIndexOf",
function(object,location){
if(location<this.elementCount){
if(object!=null){
for(var i=location;i>=0;i--){
if(object.equals(this.elementData[i])){
return i;
}}
}else{
for(var i=location;i>=0;i--){
if(this.elementData[i]==null){
return i;
}}
}return-1;
}throw new ArrayIndexOutOfBoundsException(location);
},"~O,~N");
$_M(c$,"remove",
function(location){
if(location<this.elementCount){
var result=this.elementData[location];
this.elementCount--;
var size=this.elementCount-location;
if(size>0){
System.arraycopy(this.elementData,location+1,this.elementData,location,size);
}this.elementData[this.elementCount]=null;
this.modCount++;
return result;
}throw new ArrayIndexOutOfBoundsException(location);
},"~N");
$_M(c$,"remove",
function(object){
return this.removeElement(object);
},"~O");
$_M(c$,"removeAllElements",
function(){
java.util.Arrays.fill(this.elementData,0,this.elementCount,null);
this.modCount++;
this.elementCount=0;
});
$_M(c$,"removeElement",
function(object){
var index;
if((index=this.indexOf(object,0))==-1){
return false;
}this.removeElementAt(index);
return true;
},"~O");
$_M(c$,"removeElementAt",
function(location){
if(0<=location&&location<this.elementCount){
this.elementCount--;
var size=this.elementCount-location;
if(size>0){
System.arraycopy(this.elementData,location+1,this.elementData,location,size);
}this.elementData[this.elementCount]=null;
this.modCount++;
}else{
throw new ArrayIndexOutOfBoundsException(location);
}},"~N");
$_V(c$,"removeRange",
function(start,end){
if(start>=0&&start<=end&&end<=this.size()){
if(start==end){
return;
}if(end!=this.elementCount){
System.arraycopy(this.elementData,end,this.elementData,start,this.elementCount-end);
var newCount=this.elementCount-(end-start);
java.util.Arrays.fill(this.elementData,newCount,this.elementCount,null);
this.elementCount=newCount;
}else{
java.util.Arrays.fill(this.elementData,start,this.elementCount,null);
this.elementCount=start;
}this.modCount++;
}else{
throw new IndexOutOfBoundsException();
}},"~N,~N");
$_V(c$,"set",
function(location,object){
if(location<this.elementCount){
var result=this.elementData[location];
this.elementData[location]=object;
return result;
}throw new ArrayIndexOutOfBoundsException(location);
},"~N,~O");
$_M(c$,"setElementAt",
function(object,location){
if(location<this.elementCount){
this.elementData[location]=object;
}else{
throw new ArrayIndexOutOfBoundsException(location);
}},"~O,~N");
$_M(c$,"setSize",
function(length){
if(length==this.elementCount){
return;
}this.ensureCapacity(length);
if(this.elementCount>length){
java.util.Arrays.fill(this.elementData,length,this.elementCount,null);
}this.elementCount=length;
this.modCount++;
},"~N");
$_V(c$,"size",
function(){
return this.elementCount;
});
$_V(c$,"subList",
function(start,end){
return new java.util.Collections.SynchronizedRandomAccessList($_U(this,java.util.Vector,"subList",[start,end]),this);
},"~N,~N");
$_M(c$,"toArray",
function(){
var result=new Array(this.elementCount);
System.arraycopy(this.elementData,0,result,0,this.elementCount);
return result;
});
$_M(c$,"toArray",
function(contents){
if(this.elementCount>contents.length){
var ct=contents.getClass().getComponentType();
contents=java.lang.reflect.Array.newInstance(ct,this.elementCount);
}System.arraycopy(this.elementData,0,contents,0,this.elementCount);
if(this.elementCount<contents.length){
contents[this.elementCount]=null;
}return contents;
},"~A");
$_V(c$,"toString",
function(){
if(this.elementCount==0){
return"[]";
}var length=this.elementCount-1;
var buffer=new StringBuffer(this.size()*16);
buffer.append('[');
for(var i=0;i<length;i++){
if(this.elementData[i]===this){
buffer.append("(this Collection)");
}else{
buffer.append(this.elementData[i]);
}buffer.append(", ");
}
if(this.elementData[length]===this){
buffer.append("(this Collection)");
}else{
buffer.append(this.elementData[length]);
}buffer.append(']');
return buffer.toString();
});
$_M(c$,"trimToSize",
function(){
if(this.elementData.length!=this.elementCount){
this.grow(this.elementCount);
}});
c$.$Vector$1$=function(){
$_H();
c$=$_C(function(){
$_B(this,arguments);
this.pos=0;
$_Z(this,arguments);
},java.util,"Vector$1",null,java.util.Enumeration);
$_V(c$,"hasMoreElements",
function(){
return this.pos<this.b$["java.util.Vector"].elementCount;
});
$_V(c$,"nextElement",
function(){
{
if(this.pos<this.b$["java.util.Vector"].elementCount){
return this.b$["java.util.Vector"].elementData[this.pos++];
}}throw new java.util.NoSuchElementException();
});
c$=$_P();
};
$_S(c$,
"DEFAULT_SIZE",10);
});
