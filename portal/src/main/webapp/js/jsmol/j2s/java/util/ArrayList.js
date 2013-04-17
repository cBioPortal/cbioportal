$_L(["java.util.AbstractList","$.List","$.RandomAccess"],"java.util.ArrayList",["java.lang.IllegalArgumentException","$.IndexOutOfBoundsException","java.lang.reflect.Array","java.util.Arrays"],function(){
c$=$_C(function(){
this.firstIndex=0;
this.lastIndex=0;
this.array=null;
$_Z(this,arguments);
},java.util,"ArrayList",java.util.AbstractList,[java.util.List,Cloneable,java.io.Serializable,java.util.RandomAccess]);

$_k(c$,
function(){
this.setup(0);
});

$_M(c$, "setup",
function(capacity){
$_R(this,java.util.ArrayList,[]);
this.firstIndex=this.lastIndex=0;
try{
this.array=this.newElementArray(capacity);
}catch(e){
if($_O(e,NegativeArraySizeException)){
throw new IllegalArgumentException();
}else{
throw e;
}
}
},"~N");
/*
$_K(c$,
function(collection){
$_R(this,java.util.ArrayList,[]);
var size=collection.size();
this.firstIndex=this.lastIndex=0;
this.array=this.newElementArray(size+(Math.floor(size/10)));
this.addAll(collection);
},"java.util.Collection");

*/

$_M(c$,"newElementArray",
($fz=function(size){
return new Array(size);
},$fz.isPrivate=true,$fz),"~N");

$_V(c$,"add",
function(location,object){

if (arguments.length == 1) {
	// coming from Java methods, e.g. Collections.list()
	// location is actually the object
	return this.add1(location);
}
var size=this.size();
if(0<location&&location<size){
if(this.firstIndex==0&&this.lastIndex==this.array.length){
this.growForInsert(location,1);
}else if((location<Math.floor(size/2)&&this.firstIndex>0)||this.lastIndex==this.array.length){
System.arraycopy(this.array,this.firstIndex,this.array,--this.firstIndex,location);
}else{
var index=location+this.firstIndex;
System.arraycopy(this.array,index,this.array,index+1,size-location);
this.lastIndex++;
}this.array[location+this.firstIndex]=object;
}else if(location==0){
if(this.firstIndex==0){
this.growAtFront(1);
}this.array[--this.firstIndex]=object;
}else if(location==size){
if(this.lastIndex==this.array.length){
this.growAtEnd(1);
}this.array[this.lastIndex++]=object;
}else{
throw new IndexOutOfBoundsException();
}this.modCount++;
},"~N,~O");

$_V(c$,"add1",
function(object){
if(this.lastIndex==this.array.length){
this.growAtEnd(1);
}this.array[this.lastIndex++]=object;
this.modCount++;
return true;
},"~O");

/* BH disallow addAll(int,List)
 * 
$_M(c$,"addAll",
function(location,collection){
var size=this.size();
if(location<0||location>size){
throw new IndexOutOfBoundsException();
}var growSize=collection.size();
if(0<location&&location<size){
if(this.array.length-size<growSize){
this.growForInsert(location,growSize);
}else if((location<Math.floor(size/2)&&this.firstIndex>0)||this.lastIndex>this.array.length-growSize){
var newFirst=this.firstIndex-growSize;
if(newFirst<0){
var index=location+this.firstIndex;
System.arraycopy(this.array,index,this.array,index-newFirst,size-location);
this.lastIndex-=newFirst;
newFirst=0;
}System.arraycopy(this.array,this.firstIndex,this.array,newFirst,location);
this.firstIndex=newFirst;
}else{
var index=location+this.firstIndex;
System.arraycopy(this.array,index,this.array,index+growSize,size-location);
this.lastIndex+=growSize;
}}else if(location==0){
this.growAtFront(growSize);
this.firstIndex-=growSize;
}else if(location==size){
if(this.lastIndex>this.array.length-growSize){
this.growAtEnd(growSize);
}this.lastIndex+=growSize;
}if(growSize>0){
var it=collection.iterator();
var index=location+this.firstIndex;
var end=index+growSize;
while(index<end){
this.array[index++]=it.next();
}
this.modCount++;
return true;
}return false;
},"~N,java.util.Collection");

 */

$_V(c$,"addAll",
function(collection){
var growSize=collection.size();
if(growSize>0){
	if(this.lastIndex>this.array.length-growSize){
		this.growAtEnd(growSize);
}
var it=collection.iterator();
var end=this.lastIndex+growSize;
while(this.lastIndex<end){
	this.array[this.lastIndex++]=it.next();
}
this.modCount++;
return true;
}return false;
},"java.util.Collection");

$_V(c$,"clear",
function(){
if(this.firstIndex!=this.lastIndex){
this.fill(this.firstIndex,this.lastIndex);
this.firstIndex=this.lastIndex=0;
this.modCount++;
}});

$_M(c$,"fill", function(i1, i2) { // BH
for (var i = i2; --i >= i1;)
this.array[i] = null;
},"~N,~N");

$_M(c$,"clone",
function(){
try{
var newList=$_U(this,java.util.ArrayList,"clone",[]);
newList.array=this.array.clone();
return newList;
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
if(object!=null){
for(var i=this.firstIndex;i<this.lastIndex;i++){
if(object.equals(this.array[i])){
return true;
}}
}else{
for(var i=this.firstIndex;i<this.lastIndex;i++){
if(this.array[i]==null){
return true;
}}
}return false;
},"~O");
$_M(c$,"ensureCapacity",
function(minimumCapacity){
if(this.array.length<minimumCapacity){
if(this.firstIndex>0){
this.growAtFront(minimumCapacity-this.array.length);
}else{
this.growAtEnd(minimumCapacity-this.array.length);
}}},"~N");
$_V(c$,"get",
function(location){
if(0<=location&&location<this.size()){
return this.array[this.firstIndex+location];
}throw new IndexOutOfBoundsException();
},"~N");


$_M(c$,"growAtEnd",
($fz=function(required){
var size=this.size();
if(this.firstIndex>=required-(this.array.length-this.lastIndex)){
	var newLast=this.lastIndex-this.firstIndex;
	if(size>0){
		System.arraycopy(this.array,this.firstIndex,this.array,0,size);
		var start=newLast<this.firstIndex?this.firstIndex:newLast;
		this.fill(start,this.array.length);
	}
	this.firstIndex=0;
	this.lastIndex=newLast;
}else{
	var increment=Math.floor(size/2);
	if(required>increment){
		increment=required;
	}
	if(increment<12){
		increment=12;
	}
	var newArray=this.newElementArray(size+increment);
	if(size>0){
		System.arraycopy(this.array,this.firstIndex,newArray,this.firstIndex,size);
	}
	this.array=newArray;
}

},$fz.isPrivate=true,$fz),"~N");
$_M(c$,"growAtFront",
($fz=function(required){
var size=this.size();
if(this.array.length-this.lastIndex>=required){
var newFirst=this.array.length-size;
if(size>0){
System.arraycopy(this.array,this.firstIndex,this.array,newFirst,size);
var length=this.firstIndex+size>newFirst?newFirst:this.firstIndex+size;
this.fill(this.firstIndex,length);
}this.firstIndex=newFirst;
this.lastIndex=this.array.length;
}else{
var increment=Math.floor(size/2);
if(required>increment){
increment=required;
}if(increment<12){
increment=12;
}var newArray=this.newElementArray(size+increment);
if(size>0){
System.arraycopy(this.array,this.firstIndex,newArray,newArray.length-size,size);
}this.firstIndex=newArray.length-size;
this.lastIndex=newArray.length;
this.array=newArray;
}},$fz.isPrivate=true,$fz),"~N");
$_M(c$,"growForInsert",
($fz=function(location,required){
var size=this.size();
var increment=Math.floor(size/2);
if(required>increment){
increment=required;
}if(increment<12){
increment=12;
}var newArray=this.newElementArray(size+increment);
if(location<Math.floor(size/2)){
var newFirst=newArray.length-(size+required);
System.arraycopy(this.array,location,newArray,location+increment,size-location);
System.arraycopy(this.array,this.firstIndex,newArray,newFirst,location);
this.firstIndex=newFirst;
this.lastIndex=newArray.length;
}else{
System.arraycopy(this.array,this.firstIndex,newArray,0,location);
System.arraycopy(this.array,location,newArray,location+required,size-location);
this.firstIndex=0;
this.lastIndex+=required;
}this.array=newArray;
},$fz.isPrivate=true,$fz),"~N,~N");
$_V(c$,"indexOf",
function(object){
if(object!=null){
for(var i=this.firstIndex;i<this.lastIndex;i++){
if(object.equals(this.array[i])){
return i-this.firstIndex;
}}
}else{
for(var i=this.firstIndex;i<this.lastIndex;i++){
if(this.array[i]==null){
return i-this.firstIndex;
}}
}return-1;
},"~O");
$_V(c$,"isEmpty",
function(){
return this.lastIndex==this.firstIndex;
});
$_V(c$,"lastIndexOf",
function(object){
if(object!=null){
for(var i=this.lastIndex-1;i>=this.firstIndex;i--){
if(object.equals(this.array[i])){
return i-this.firstIndex;
}}
}else{
for(var i=this.lastIndex-1;i>=this.firstIndex;i--){
if(this.array[i]==null){
return i-this.firstIndex;
}}
}return-1;
},"~O");
$_V(c$,"remove",
function(location){
var result;
var size=this.size();
if(0<=location&&location<size){
if(location==size-1){
result=this.array[--this.lastIndex];
this.array[this.lastIndex]=null;
}else if(location==0){
result=this.array[this.firstIndex];
this.array[this.firstIndex++]=null;
}else{
var elementIndex=this.firstIndex+location;
result=this.array[elementIndex];
if(location<Math.floor(size/2)){
System.arraycopy(this.array,this.firstIndex,this.array,this.firstIndex+1,location);
this.array[this.firstIndex++]=null;
}else{
System.arraycopy(this.array,elementIndex+1,this.array,elementIndex,size-location-1);
this.array[--this.lastIndex]=null;
}}}else{
throw new IndexOutOfBoundsException();
}this.modCount++;
return result;
},"~N"); 

//$_M(c$, "removeObject")
$_V(c$,"removeRange",
function(start,end){
if(start>=0&&start<=end&&end<=this.size()){
if(start==end){
return;
}var size=this.size();
if(end==size){
	this.fill(this.firstIndex+start,this.lastIndex);
this.lastIndex=this.firstIndex+start;
}else if(start==0){
	this.fill(this.firstIndex,this.firstIndex+end);
this.firstIndex+=end;
}else{
System.arraycopy(this.array,this.firstIndex+end,this.array,this.firstIndex+start,size-end);
var newLast=this.lastIndex+start-end;
this.fill(newLast,this.lastIndex);
this.lastIndex=newLast;
}this.modCount++;
}else{
throw new IndexOutOfBoundsException();
}},"~N,~N");
$_V(c$,"set",
function(location,object){
if(0<=location&&location<this.size()){
var result=this.array[this.firstIndex+location];
this.array[this.firstIndex+location]=object;
return result;
}throw new IndexOutOfBoundsException();
},"~N,~O");
$_V(c$,"size",
function(){
return this.lastIndex-this.firstIndex;
});
/*
$_M(c$,"toArray",
function(){
var size=this.size();
var result=new Array(size);
System.arraycopy(this.array,this.firstIndex,result,0,size);
return result;
});
*/

$_V(c$,"toArray",
function(contents){
var size=this.size();
if(size>contents.length){
var ct=contents.getClass().getComponentType();
contents=java.lang.reflect.Array.newInstance(ct,size);
}System.arraycopy(this.array,this.firstIndex,contents,0,size);
if(size<contents.length){
contents[size]=null;
}return contents;
},"~O");
$_M(c$,"trimToSize",
function(){
var size=this.size();
var newArray=this.newElementArray(size);
System.arraycopy(this.array,this.firstIndex,newArray,0,size);
this.array=newArray;
this.firstIndex=0;
this.lastIndex=this.array.length;
});
});
