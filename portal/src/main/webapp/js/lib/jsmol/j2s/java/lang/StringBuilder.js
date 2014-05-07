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
