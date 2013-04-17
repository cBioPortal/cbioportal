$_J("java.text");
c$=$_C(function(){
this.pattern=null;
$_Z(this,arguments);
},java.text,"MessageFormat");
$_K(c$,
function(pattern){
this.pattern=pattern;
},"~S");
$_K(c$,
function(pattern,locale){
this.pattern=pattern;
},"~S,java.util.Locale");
c$.format=$_M(c$,"format",
function(pattern,args){
return pattern.replace(/\{(\d+)\}/g,function($0,$1){
var i=parseInt($1);
if(args==null)return null;
return args[i];
});
},"~S,~A");
$_M(c$,"format",
function(obj){
return java.text.MessageFormat.format(this.pattern,[obj]);
},"~O");
