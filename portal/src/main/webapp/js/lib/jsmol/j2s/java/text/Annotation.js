$_J("java.text");
c$=$_C(function(){
this.value=null;
$_Z(this,arguments);
},java.text,"Annotation");
$_K(c$,
function(attribute){
this.value=attribute;
},"~O");
$_M(c$,"getValue",
function(){
return this.value;
});
$_V(c$,"toString",
function(){
return this.getClass().getName()+"[value="+this.value+']';
});
