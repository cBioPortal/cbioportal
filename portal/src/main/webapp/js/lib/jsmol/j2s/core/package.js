// BH 12/15/2012 1:56:28 PM  adds corezip.z.js and corebio.z.js
// later additions include coresym.z.js, coresurface.z.js, coremenu.z.js

// NOTE: Any changes here must also be reflected in buildtojs.xml

if (!window["java.registered"])
 window["java.registered"] = false;

(function () {

if (window["java.packaged"]) return;
window["java.packaged"] = true;


	ClazzLoader.registerPackages ("java", [
			"io", "lang", 
			//"lang.annotation", 
			"lang.reflect",
			"util", 
			//"util.concurrent", "util.concurrent.atomic", "util.concurrent.locks",
			//"util.jar", "util.logging", "util.prefs", 
			"util.regex",
			"util.zip",
			"net", "text"]);
			
	window["reflect"] = java.lang.reflect;

var	base = ClazzLoader.fastGetJ2SLibBase ();//ClazzLoader.getClasspathFor ("core.*");
	base += "core/"
var	basefile = base + "core.z.js";

	ClazzLoader.ignore([
		"net.sf.j2s.ajax.HttpRequest",
		"java.util.MapEntry.Type",
		"java.net.UnknownServiceException",
		"java.lang.Runtime",
		"java.security.AccessController",
		"java.security.PrivilegedExceptionAction",
		"java.io.File",
		"java.io.FileInputStream",
		"java.io.FileWriter",
		"java.io.OutputStreamWriter",
		"java.util.Calendar", // bypassed in ModelCollection
		"java.text.SimpleDateFormat", // not used
		"java.text.DateFormat", // not used
		"java.util.concurrent.Executors"
	])
	
	ClazzLoader.loadZJar (basefile, ClazzLoader.runtimeKeyClass);

  if (Jmol.debugCode)
    return;

	ClazzLoader.jarClasspath (base + "corescript.z.js",	[  
    "java.util.regex.Pattern", 
    "$.Matcher", 
    "$.MatchResult",     
    "J.api.JmolScriptManager", 
    "$.JmolScriptEvaluator",
    "$.JmolScriptFunction",
    "J.script.ScriptEvaluator", 
    "$.ScriptCompiler", 
    "$.ScriptCompilationTokenParser",
    "$.ScriptFlowContext", 
    "$.ScriptFunction", 
    "$.ScriptInterruption", 
    "$.ScriptMathProcessor", 
    "$.CommandWatcherThread", 
    "$.ScriptQueueThread", 
    "$.ScriptDelayThread", 
    "$.ScriptManager" 
	]);
	
	ClazzLoader.jarClasspath (base + "corestate.z.js",	[  
    "J.api.JmolStateCreator", 
    "J.viewer.StateCreator" 
	]);
	
	ClazzLoader.jarClasspath (base + "coreprop.z.js",	[  
    "J.api.JmolPropertyManager", 
    "J.viewer.PropertyManager" 
	]);  
  
	ClazzLoader.jarClasspath (base + "coreconsole.z.js",	[
		"J.api.JmolAppConsoleInterface",
		"J.console.GenericTextArea",
		"$.GenericConsole",
		"J.consolejs.AppletConsole"
	]);

	ClazzLoader.jarClasspath (base + "coremenu.z.js",	[
		"J.api.JmolPopupInterface",
		"J.awtjs2d.JSmolPopup",		
		"$.JSPopup",
		"$.JSmolPopup",
		"J.popup.JmolAbstractMenu",
		"$.GenericPopup",
		"$.PopupResource",
		"$.MainPopupResourceBundle"
	]);

	ClazzLoader.jarClasspath (base + "corebinary.z.js",	[
    "java.io.DataInputStream",
    "J.api.JmolDocument",
    "J.io2.BinaryDocument"
	]);

	ClazzLoader.jarClasspath (base + "corepymol.z.js",	[
    "J.api.JmolSceneGenerator",
    "J.api.PymolAtomReader", // -- required by J.adapter.readers.pymol.PyMOLReader
    "J.adapter.readers.pymol.PickleReader",
    "$.PyMOL",
    "$.JmolObject",
    "$.PyMOLGroup",
    "$.PyMOLScene",
    "$.PyMOLReader"
	]);

	ClazzLoader.jarClasspath (base + "coremin.z.js",	[
		"J.api.MinimizerInterface", // -- required by J.minimize.Minimizer
		"J.minimize.Minimizer",
		"$.MinObject", // -- required by $.MinAngle
		"$.MinAngle",
		"$.MinAtom",
		"$.MinBond",
		"$.MinTorsion",
		"$.Util",
		"J.minimize.forcefield.AtomType",
		"$.Calculation", // -- required by $.CalculationsMMFF
		"$.Calculations", // -- required by $.CalculationsMMFF
		"$.CalculationsMMFF",
		"$.CalculationsUFF",
		"$.FFParam",
		"$.ForceField", // -- required by $.forcefield.ForceFieldMMFF
		"$.ForceFieldUFF",
		"$.ForceFieldMMFF",
		"J.thread.MinimizationThread"
	]);

	ClazzLoader.jarClasspath (base + "corezip.z.js",	[
		"JZ.Checksum",
		"$.CRC32",
		"$.InflaterInputStream",
		"$.ZStream",
		"$.Inflater",
		"$.Adler32",
		"$.Tree",
		"$.Deflate",
		"$.GZIPHeader",
		"$.StaticTree",
		"$.Inflate",
		"$.InfTree",
		"$.InfBlocks",
		"$.InfCodes",
		"$.Inflater",
		"$.InflaterInputStream",
		"$.GZIPInputStream",
		"$.Deflater",
		"$.DeflaterOutputStream",

		"java.io.ByteArrayOutputStream",
		"$.PushbackInputStream",
		"java.util.zip.CRC32",
		"$.CheckedInputStream",
		"$.GZIPInputStream",
		"$.Inflater",
		"$.InflaterInputStream",
		"$.ZipException",
		"$.ZipConstants",
		"$.ZipEntry",
		"$.ZipConstants64",
		"$.ZipInputStream",
		"$.Deflater",
		"$.DeflaterOutputStream",
		"$.ZipOutputStream",

		"J.api.JmolZipUtility",
		"$.ZInputStream",
		"$.JmolImageCreatorInterface",
		"J.export.image.GenericCRCEncoder",
		"$.GenericPngEncoder",
		"$.GenericImageCreator",
		"J.exportjs.JSImageCreator",
		"J.io2.ZipUtil",
		"$.JpegEncoder",
		"$.JmolZipInputStream"		
	]);
	
	ClazzLoader.jarClasspath (base + "corebio.z.js",	[
		"J.adapter.readers.pdb.PdbReader",
		"J.adapter.smarter.Structure",
		"J.api.JmolBioResolver",
		"J.modelsetbio.Resolver",
		"$.Monomer",
		"$.AlphaMonomer",
		"$.ProteinStructure",
		"$.Helix",
		"$.Sheet",
		"$.Turn",
		"$.BioPolymer", 
		"$.AlphaPolymer",
		"$.AminoMonomer",
		"$.AminoPolymer",
		"$.APBridge",
		"$.BioModel",
		"$.CarbohydrateMonomer",
		"$.CarbohydratePolymer",
		"$.PhosphorusMonomer", 
		"$.NucleicMonomer",
		"$.NucleicPolymer",
		"$.PhosphorusPolymer",
		"J.shapebio.BioShape",
		"$.BioShapeCollection",
		"$.Ribbons",	
		"$.MeshRibbons",
		"$.Strands",
		"$.Rockets",
		"$.Cartoon",
	    "$.Backbone",
	    "$.Trace",
		"J.renderbio.BioShapeRenderer",
		"$.StrandsRenderer",
		"$.RibbonsRenderer",
		"$.MeshRibbonsRenderer",
		"$.RocketsRenderer",
		"$.CartoonRenderer",
	    "$.BackboneRenderer",
	    "$.TraceRenderer"
	]);


	ClazzLoader.jarClasspath (base + "coresurface.z.js",	[
		"J.api.VolumeDataInterface",
		"J.jvxl.api.VertexDataServer",
		"$.MeshDataServer",
		"J.jvxl.calc.MarchingCubes",
		"$.MarchingSquares",
		"J.jvxl.data.JvxlCoder",
		"$.VolumeData",
		"$.JvxlData",
		"$.MeshData",
    "J.io.XmlReader",
		"J.jvxl.readers.SurfaceGenerator",
		"$.Parameters",
		"$.SurfaceReader",
		"$.VolumeDataReader",
		"$.AtomDataReader",
		"$.IsoSolventReader",
    "$.SurfaceFileReader",
    "$.VolumeFileReader",
    "$.JvxlXmlReader",
		"J.shapesurface.Isosurface",
		"$.IsosurfaceMesh",
		"J.rendersurface.IsosurfaceRenderer"
	]);

	ClazzLoader.jarClasspath (base + "coresym.z.js",	[
		"J.api.SymmetryInterface",
		"J.symmetry.Symmetry",
		"$.PointGroup",
		"$.SpaceGroup",
		"$.HallInfo",
		"$.HallRotationTerm",
		"$.HallRotation",
		"$.HallTranslation",
		"$.SymmetryOperation",
		"$.SymmetryInfo",
		"$.UnitCell"
	]);

	ClazzLoader.jarClasspath (base + "coresmiles.z.js",	[
    "J.api.SmilesMatcherInterface",
    "J.smiles.SmilesMatcher",
    "$.InvalidSmilesException",
    "$.SmilesSearch",
    "$.SmilesGenerator",
    "$.SmilesAromatic",
    "$.SmilesAtom",
    "$.SmilesBond",
    "$.SmilesMeasure",
    "$.SmilesParser"
	]);

}) ();
window["java.registered"] = true;
