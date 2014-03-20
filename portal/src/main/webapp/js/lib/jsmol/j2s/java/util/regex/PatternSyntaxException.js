$_J("java.util.regex");
$_L(["java.lang.IllegalArgumentException"],"java.util.regex.PatternSyntaxException",null,function(){
c$=$_C(function(){
this.desc=null;
this.pattern=null;
this.index=-1;
$_Z(this,arguments);
},java.util.regex,"PatternSyntaxException",IllegalArgumentException);
$_K(c$,
function(desc,pattern,index){
$_R(this,java.util.regex.PatternSyntaxException,[]);
this.desc=desc;
this.pattern=pattern;
this.index=index;
},"~S,~S,~N");
$_M(c$,"getPattern",
function(){
return this.pattern;
});
$_V(c$,"getMessage",
function(){
var s=this.desc;
if(this.index>=0){
s+=" near index "+this.index;
}s+="\r\n"+this.pattern;
if(this.index>=0){
s+="\r\n";
for(var i=0;i<this.index;i++)s+=(' ').charCodeAt(0);

s+=('^').charCodeAt(0);
}return s;
});
$_M(c$,"getDescription",
function(){
return this.desc;
});
$_M(c$,"getIndex",
function(){
return this.index;
});
});
