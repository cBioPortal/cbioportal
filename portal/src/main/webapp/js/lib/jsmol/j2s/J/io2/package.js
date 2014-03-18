var path = ClazzLoader.getClasspathFor ("J.io2.package");
path = path.substring (0, path.lastIndexOf ("package.js"));
ClazzLoader.jarClasspath (path + "JpegEncoder.js", [
"J.io2.Huffman",
"$.JpegEncoder",
"$.JpegObj",
"$.DCT"]);
