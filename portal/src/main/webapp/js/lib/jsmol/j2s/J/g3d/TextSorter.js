Clazz.declarePackage ("J.g3d");
c$ = Clazz.declareType (J.g3d, "TextSorter", null, java.util.Comparator);
Clazz.overrideMethod (c$, "compare", 
function (a, b) {
return (a == null || b == null ? 0 : a.z > b.z ? -1 : a.z < b.z ? 1 : 0);
}, "J.g3d.TextString,J.g3d.TextString");
