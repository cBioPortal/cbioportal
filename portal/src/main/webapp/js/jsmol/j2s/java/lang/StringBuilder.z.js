/* http://j2s.sf.net/ */$_L(null,"java.lang.AbstractStringBuilder",["java.io.InvalidObjectException","java.lang.ArrayIndexOutOfBoundsException","$.IndexOutOfBoundsException","$.NegativeArraySizeException","$.NullPointerException","$.StringIndexOutOfBoundsException"],function(){
c$=$_C(function(){
this.value=null;
this.count=0;
this.shared=false;
$_Z(this,arguments);
},java.lang,"AbstractStringBuilder");
$_M(c$,"getValue",
function(){
return this.value;
});
$_M(c$,"shareValue",
function(){
this.shared=true;
return this.value;
});
$_M(c$,"set",
function(val,len){
if(val==null)val=$_A(0,'\0');
if(val.length<len)throw new java.io.InvalidObjectException(("K0199"));
this.shared=false;
this.value=val;
this.count=len;
},"~A,~N");
$_K(c$,
function(){
this.value=$_A(16,'\0');
});
$_K(c$,
function(capacity){
if(capacity<0)throw new NegativeArraySizeException();
this.value=$_A(capacity,'\0');
},"~N");
$_K(c$,
function(string){
this.count=string.length;
this.shared=false;
this.value=$_A(this.count+16,'\0');
string.getChars(0,this.count,this.value,0);
},"~S");
$_M(c$,"enlargeBuffer",
($fz=function(min){
var twice=(this.value.length<<1)+2;
var newData=$_A(min>twice?min:twice,'\0');
System.arraycopy(this.value,0,newData,0,this.count);
this.value=newData;
this.shared=false;
},$fz.isPrivate=true,$fz),"~N");
$_M(c$,"appendNull",
function(){
var newSize=this.count+4;
if(newSize>this.value.length){
this.enlargeBuffer(newSize);
}else if(this.shared){
this.value=this.value.clone();
this.shared=false;
}this.value[this.count++]='n';
this.value[this.count++]='u';
this.value[this.count++]='l';
this.value[this.count++]='l';
});
$_M(c$,"append0",
function(chars){
var newSize=this.count+chars.length;
if(newSize>this.value.length){
this.enlargeBuffer(newSize);
}else if(this.shared){
this.value=this.value.clone();
this.shared=false;
}System.arraycopy(chars,0,this.value,this.count,chars.length);
this.count=newSize;
},"~A");
$_M(c$,"append0",
function(chars,start,length){
if(chars==null){
throw new NullPointerException();
}if(start>=0&&0<=length&&length<=chars.length-start){
var newSize=this.count+length;
if(newSize>this.value.length){
this.enlargeBuffer(newSize);
}else if(this.shared){
this.value=this.value.clone();
this.shared=false;
}System.arraycopy(chars,start,this.value,this.count,length);
this.count=newSize;
}else{
throw new ArrayIndexOutOfBoundsException();
}},"~A,~N,~N");
$_M(c$,"append0",
function(ch){
if(this.count==this.value.length){
this.enlargeBuffer(this.count+1);
}if(this.shared){
this.value=this.value.clone();
this.shared=false;
}this.value[this.count++]=ch;
},"~N");
$_M(c$,"append0",
function(string){
if(string==null){
this.appendNull();
return;
}var adding=string.length;
var newSize=this.count+adding;
if(newSize>this.value.length){
this.enlargeBuffer(newSize);
}else if(this.shared){
this.value=this.value.clone();
this.shared=false;
}string.getChars(0,adding,this.value,this.count);
this.count=newSize;
},"~S");
$_M(c$,"append0",
function(s,start,end){
if(s==null)s="null";
if(start<0||end<0||start>end||end>s.length())throw new IndexOutOfBoundsException();
this.append0(s.subSequence(start,end).toString());
},"CharSequence,~N,~N");
$_M(c$,"capacity",
function(){
return this.value.length;
});
$_M(c$,"charAt",
function(index){
if(index<0||index>=this.count)throw new StringIndexOutOfBoundsException(index);
return this.value[index];
},"~N");
$_M(c$,"delete0",
function(start,end){
if(start>=0){
if(end>this.count){
end=this.count;
}if(end==start){
return;
}if(end>start){
var length=this.count-end;
if(length>0){
if(!this.shared){
System.arraycopy(this.value,end,this.value,start,length);
}else{
var newData=$_A(this.value.length,'\0');
System.arraycopy(this.value,0,newData,0,start);
System.arraycopy(this.value,end,newData,start,length);
this.value=newData;
this.shared=false;
}}this.count-=end-start;
return;
}}throw new StringIndexOutOfBoundsException();
},"~N,~N");
$_M(c$,"deleteCharAt0",
function(location){
if(0>location||location>=this.count)throw new StringIndexOutOfBoundsException(location);
var length=this.count-location-1;
if(length>0){
if(!this.shared){
System.arraycopy(this.value,location+1,this.value,location,length);
}else{
var newData=$_A(this.value.length,'\0');
System.arraycopy(this.value,0,newData,0,location);
System.arraycopy(this.value,location+1,newData,location,length);
this.value=newData;
this.shared=false;
}}this.count--;
},"~N");
$_M(c$,"ensureCapacity",
function(min){
if(min>this.value.length){
this.enlargeBuffer(min);
}},"~N");
$_M(c$,"getChars",
function(start,end,dest,destStart){
if(start>this.count||end>this.count||start>end){
throw new StringIndexOutOfBoundsException();
}System.arraycopy(this.value,start,dest,destStart,end-start);
},"~N,~N,~A,~N");
$_M(c$,"insert0",
function(index,chars){
if(0>index||index>this.count){
throw new StringIndexOutOfBoundsException(index);
}if(chars.length!=0){
this.move(chars.length,index);
System.arraycopy(chars,0,this.value,index,chars.length);
this.count+=chars.length;
}},"~N,~A");
$_M(c$,"insert0",
function(index,chars,start,length){
if(0<=index&&index<=this.count){
if(start>=0&&0<=length&&length<=chars.length-start){
if(length!=0){
this.move(length,index);
System.arraycopy(chars,start,this.value,index,length);
this.count+=length;
}return;
}throw new StringIndexOutOfBoundsException("offset "+start+", len "+length+", array.length "+chars.length);
}throw new StringIndexOutOfBoundsException(index);
},"~N,~A,~N,~N");
$_M(c$,"insert0",
function(index,ch){
if(0>index||index>this.count){
throw new ArrayIndexOutOfBoundsException(index);
}this.move(1,index);
this.value[index]=ch;
this.count++;
},"~N,~N");
$_M(c$,"insert0",
function(index,string){
if(0<=index&&index<=this.count){
if(string==null)string="null";
var min=string.length;
if(min!=0){
this.move(min,index);
string.getChars(0,min,this.value,index);
this.count+=min;
}}else{
throw new StringIndexOutOfBoundsException(index);
}},"~N,~S");
$_M(c$,"insert0",
function(index,s,start,end){
if(s==null)s="null";
if(index<0||index>this.count||start<0||end<0||start>end||end>s.length())throw new IndexOutOfBoundsException();
this.insert0(index,s.subSequence(start,end).toString());
},"~N,CharSequence,~N,~N");
$_M(c$,"length",
function(){
return this.count;
});
$_M(c$,"move",
($fz=function(size,index){
var newSize;
if(this.value.length-this.count>=size){
if(!this.shared){
System.arraycopy(this.value,index,this.value,index+size,this.count-index);
return;
}newSize=this.value.length;
}else{
var a=this.count+size;
var b=(this.value.length<<1)+2;
newSize=a>b?a:b;
}var newData=$_A(newSize,'\0');
System.arraycopy(this.value,0,newData,0,index);
System.arraycopy(this.value,index,newData,index+size,this.count-index);
this.value=newData;
this.shared=false;
},$fz.isPrivate=true,$fz),"~N,~N");
$_M(c$,"replace0",
function(start,end,string){
if(start>=0){
if(end>this.count)end=this.count;
if(end>start){
var stringLength=string.length;
var diff=end-start-stringLength;
if(diff>0){
if(!this.shared){
System.arraycopy(this.value,end,this.value,start+stringLength,this.count-end);
}else{
var newData=$_A(this.value.length,'\0');
System.arraycopy(this.value,0,newData,0,start);
System.arraycopy(this.value,end,newData,start+stringLength,this.count-end);
this.value=newData;
this.shared=false;
}}else if(diff<0){
this.move(-diff,end);
}else if(this.shared){
this.value=this.value.clone();
this.shared=false;
}string.getChars(0,stringLength,this.value,start);
this.count-=diff;
return;
}if(start==end){
if(string==null)throw new NullPointerException();
this.insert0(start,string);
return;
}}throw new StringIndexOutOfBoundsException();
},"~N,~N,~S");
$_M(c$,"reverse0",
function(){
if(this.count<2){
return;
}if(!this.shared){
for(var i=0,end=this.count,mid=Math.floor(this.count/2);i<mid;i++){
var temp=this.value[--end];
this.value[end]=this.value[i];
this.value[i]=temp;
}
}else{
var newData=$_A(this.value.length,'\0');
for(var i=0,end=this.count;i<this.count;i++){
newData[--end]=this.value[i];
}
this.value=newData;
this.shared=false;
}});
$_M(c$,"setCharAt",
function(index,ch){
if(0>index||index>=this.count){
throw new StringIndexOutOfBoundsException(index);
}if(this.shared){
this.value=this.value.clone();
this.shared=false;
}this.value[index]=ch;
},"~N,~N");
$_M(c$,"setLength",
function(length){
if(length<0)throw new StringIndexOutOfBoundsException(length);
if(this.count<length){
if(length>this.value.length){
this.enlargeBuffer(length);
}else{
if(this.shared){
var newData=$_A(this.value.length,'\0');
System.arraycopy(this.value,0,newData,0,this.count);
this.value=newData;
this.shared=false;
}else{
for(var i=this.count;i<length;i++){
this.value[i]=String.fromCharCode(0);
}
}}}this.count=length;
},"~N");
$_M(c$,"substring",
function(start){
if(0<=start&&start<=this.count){
if(start==this.count)return"";
this.shared=true;
return String.instantialize(start,this.count-start,this.value);
}throw new StringIndexOutOfBoundsException(start);
},"~N");
$_M(c$,"substring",
function(start,end){
if(0<=start&&start<=end&&end<=this.count){
if(start==end)return"";
this.shared=true;
return String.instantialize(this.value,start,end-start);
}throw new StringIndexOutOfBoundsException();
},"~N,~N");
$_V(c$,"toString",
function(){
if(this.count==0)return"";
if(this.count>=256&&this.count<=(this.value.length>>1))return String.instantialize(this.value,0,this.count);
this.shared=true;
return String.instantialize(0,this.count,this.value);
});
$_M(c$,"subSequence",
function(start,end){
return this.substring(start,end);
},"~N,~N");
$_M(c$,"indexOf",
function(string){
return this.indexOf(string,0);
},"~S");
$_M(c$,"indexOf",
function(subString,start){
if(start<0)start=0;
var subCount=subString.length;
if(subCount>0){
if(subCount+start>this.count)return-1;
var firstChar=subString.charAt(0);
while(true){
var i=start;
var found=false;
for(;i<this.count;i++)if((this.value[i]).charCodeAt(0)==(firstChar).charCodeAt(0)){
found=true;
break;
}
if(!found||subCount+i>this.count)return-1;
var o1=i;
var o2=0;
while(++o2<subCount&&(this.value[++o1]).charCodeAt(0)==(subString.charAt(o2)).charCodeAt(0)){
}
if(o2==subCount)return i;
start=i+1;
}
}return(start<this.count||start==0)?start:this.count;
},"~S,~N");
$_M(c$,"lastIndexOf",
function(string){
return this.lastIndexOf(string,this.count);
},"~S");
$_M(c$,"lastIndexOf",
function(subString,start){
var subCount=subString.length;
if(subCount<=this.count&&start>=0){
if(subCount>0){
if(start>this.count-subCount)start=this.count-subCount;
var firstChar=subString.charAt(0);
while(true){
var i=start;
var found=false;
for(;i>=0;--i)if((this.value[i]).charCodeAt(0)==(firstChar).charCodeAt(0)){
found=true;
break;
}
if(!found)return-1;
var o1=i;
var o2=0;
while(++o2<subCount&&(this.value[++o1]).charCodeAt(0)==(subString.charAt(o2)).charCodeAt(0)){
}
if(o2==subCount)return i;
start=i-1;
}
}return start<this.count?start:this.count;
}return-1;
},"~S,~N");
$_M(c$,"trimToSize",
function(){
if(this.count<this.value.length){
var newValue=$_A(this.count,'\0');
System.arraycopy(this.value,0,newValue,0,this.count);
this.value=newValue;
this.shared=false;
}});
$_S(c$,
"INITIAL_CAPACITY",16);
});
$_L(["java.lang.AbstractStringBuilder","$.Appendable"],"java.lang.StringBuilder",["java.lang.Double","$.Float","$.Long"],function(){
c$=$_T(java.lang,"StringBuilder",AbstractStringBuilder,[Appendable,CharSequence,java.io.Serializable]);
$_K(c$,
function(seq){
$_R(this,StringBuilder,[seq.toString()]);
},"CharSequence");
$_M(c$,"append",
function(b){
this.append0(b?"true":"false");
return this;
},"~B");
$_M(c$,"append",
function(c){
this.append0(c);
return this;
},"~N");
$_M(c$,"append",
function(i){
this.append0(Integer.toString(i));
return this;
},"~N");
$_M(c$,"append",
function(lng){
this.append0(Long.toString(lng));
return this;
},"~N");
$_M(c$,"append",
function(f){
this.append0(Float.toString(f));
return this;
},"~N");
$_M(c$,"append",
function(d){
this.append0(Double.toString(d));
return this;
},"~N");
$_M(c$,"append",
function(obj){
if(obj==null){
this.appendNull();
}else{
this.append0(obj.toString());
}return this;
},"~O");
$_M(c$,"append",
function(str){
this.append0(str);
return this;
},"~S");
$_M(c$,"append",
function(sb){
if(sb==null){
this.appendNull();
}else{
this.append0(sb.getValue(),0,sb.length());
}return this;
},"StringBuffer");
$_M(c$,"append",
function(ch){
this.append0(ch);
return this;
},"~A");
$_M(c$,"append",
function(str,offset,len){
this.append0(str,offset,len);
return this;
},"~A,~N,~N");
$_M(c$,"append",
function(csq){
if(csq==null){
this.appendNull();
}else{
this.append0(csq.toString());
}return this;
},"CharSequence");
$_M(c$,"append",
function(csq,start,end){
this.append0(csq,start,end);
return this;
},"CharSequence,~N,~N");
$_M(c$,"$delete",
function(start,end){
this.delete0(start,end);
return this;
},"~N,~N");
$_M(c$,"deleteCharAt",
function(index){
this.deleteCharAt0(index);
return this;
},"~N");
$_M(c$,"insert",
function(offset,b){
this.insert0(offset,b?"true":"false");
return this;
},"~N,~B");
$_M(c$,"insert",
function(offset,c){
this.insert0(offset,c);
return this;
},"~N,~N");
$_M(c$,"insert",
function(offset,i){
this.insert0(offset,Integer.toString(i));
return this;
},"~N,~N");
$_M(c$,"insert",
function(offset,l){
this.insert0(offset,Long.toString(l));
return this;
},"~N,~N");
$_M(c$,"insert",
function(offset,f){
this.insert0(offset,Float.toString(f));
return this;
},"~N,~N");
$_M(c$,"insert",
function(offset,d){
this.insert0(offset,Double.toString(d));
return this;
},"~N,~N");
$_M(c$,"insert",
function(offset,obj){
this.insert0(offset,obj==null?"null":obj.toString());
return this;
},"~N,~O");
$_M(c$,"insert",
function(offset,str){
this.insert0(offset,str);
return this;
},"~N,~S");
$_M(c$,"insert",
function(offset,ch){
this.insert0(offset,ch);
return this;
},"~N,~A");
$_M(c$,"insert",
function(offset,str,strOffset,strLen){
this.insert0(offset,str,strOffset,strLen);
return this;
},"~N,~A,~N,~N");
$_M(c$,"insert",
function(offset,s){
this.insert0(offset,s==null?"null":s.toString());
return this;
},"~N,CharSequence");
$_M(c$,"insert",
function(offset,s,start,end){
this.insert0(offset,s,start,end);
return this;
},"~N,CharSequence,~N,~N");
$_M(c$,"replace",
function(start,end,str){
this.replace0(start,end,str);
return this;
},"~N,~N,~S");
$_M(c$,"reverse",
function(){
this.reverse0();
return this;
});
});
