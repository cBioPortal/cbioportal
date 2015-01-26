package org.mskcc.cbio.annotator;

import org.kohsuke.args4j.Option;

/**
 * Configuration parameters (command line arguments).
 *
 * @author Selcuk Onur Sumer
 */
public class AnnotatorConfig
{
	// default config values
	public static final String DEFAULT_ANNOTATOR = "vep";
	public static final String DEFAULT_MAF2MAF = "maf2maf.pl";
	public static final String DEFAULT_VCF2MAF = "vcf2maf.pl";
	public static final String DEFAULT_VEP_PATH = ".";
	public static final String DEFAULT_VEP_DATA = ".";
	public static final String DEFAULT_REF_FASTA = "Homo_sapiens.GRCh37.75.dna.primary_assembly.fa";
	public static final String DEFAULT_EXCLUDE_COLS = "oncotator";
	public static final String DEFAULT_INTERMEDIATE_MAF = "annotator_out.maf";
	public static final String DEFAULT_INTERMEDIATE_DIR = "annotator_dir";
	public static final String DEFAULT_VEP_FORKS = "8";

	// TODO allele count col options?

	@Option(name="-i",
	        aliases={"--input-file"},
	        usage="Input filename",
	        required=true)
	protected String input;

	@Option(name="-o",
	        aliases={"--output-file"},
	        usage="Output filename",
	        required=true)
	protected String output;

	@Option(name="-s",
	        aliases={"--sort-output"},
	        usage="Sort standard MAF columns in the output file")
	protected boolean sort = false;

	@Option(name="-a",
	        aliases={"--add-standard"},
	        usage="Add missing standard MAF columns to the output file")
	protected boolean addMissing = false;

	@Option(name="-nc",
	        aliases={"--no-cache"},
	        usage="Use cache for oncotator")
	protected boolean noCache = false;

	@Option(name="-m2m",
	        aliases={"--maf2maf-script"},
	        usage="Name of the maf2maf script file")
	protected String maf2maf = DEFAULT_MAF2MAF;

	@Option(name="-v2m",
	        aliases={"--vcf2maf-script"},
	        usage="Name of the vcf2maf script file")
	protected String vcf2maf = DEFAULT_VCF2MAF;

	@Option(name="-vp",
	        aliases={"--vep-path"},
	        usage="Directory containing variant_effect_predictor.pl")
	protected String vepPath = DEFAULT_VEP_PATH;

	@Option(name="-vd",
	        aliases={"--vep-data"},
	        usage="VEP's base cache/plugin directory")
	protected String vepData = DEFAULT_VEP_DATA;

	@Option(name="-rf",
	        aliases={"--ref-fasta"},
	        usage="Reference FASTA file")
	protected String refFasta = DEFAULT_REF_FASTA;

	@Option(name="-ec",
	        aliases={"--exclude-cols"},
	        usage="Comma-delimited list of columns to exclude from the input MAF")
	protected String excludeCols = DEFAULT_EXCLUDE_COLS;

	@Option(name="-t",
	        aliases={"--annotator"},
	        usage="Type of the annotator (default: VEP)")
	protected String annotator = DEFAULT_ANNOTATOR;

	@Option(name="-im",
	        aliases={"--intermediate-maf"},
	        usage="Intermediate MAF filename")
	protected String intermediateMaf = DEFAULT_INTERMEDIATE_MAF;

	@Option(name="-id",
	        aliases={"--intermediate-dir"},
	        usage="Directory name for intermediate output files")
	protected String intermediateDir = DEFAULT_INTERMEDIATE_DIR;

	@Option(name="-td",
	        aliases={"--tmp-dir"},
	        usage="Folder to retain intermediate VCFs/MAFs after runtime")
	protected String tmpDir = DEFAULT_INTERMEDIATE_DIR;

	@Option(name="-vf",
	        aliases={"--vep-forks"},
	        usage="Number of forked processes to use when running VEP")
	protected String vepForks = DEFAULT_VEP_FORKS;

	public String getIntermediateMaf()
	{
		return intermediateMaf;
	}

	public void setIntermediateMaf(String intermediateMaf)
	{
		this.intermediateMaf = intermediateMaf;
	}

	public String getIntermediateDir()
	{
		return intermediateDir;
	}

	public void setIntermediateDir(String intermediateDir)
	{
		this.intermediateDir = intermediateDir;
	}

	public String getTmpDir()
	{
		return tmpDir;
	}

	public void setTmpDir(String tmpDir)
	{
		this.tmpDir = tmpDir;
	}

	public String getVepForks()
	{
		return vepForks;
	}

	public void setVepForks(String vepForks)
	{
		this.vepForks = vepForks;
	}

	public String getInput()
	{
		return input;
	}

	public void setInput(String input)
	{
		this.input = input;
	}

	public String getOutput()
	{
		return output;
	}

	public void setOutput(String output)
	{
		this.output = output;
	}

	public boolean isSort()
	{
		return sort;
	}

	public void setSort(boolean sort)
	{
		this.sort = sort;
	}

	public boolean isAddMissing()
	{
		return addMissing;
	}

	public void setAddMissing(boolean addMissing)
	{
		this.addMissing = addMissing;
	}

	public boolean isNoCache()
	{
		return noCache;
	}

	public void setNoCache(boolean noCache)
	{
		this.noCache = noCache;
	}

	public String getMaf2maf()
	{
		return maf2maf;
	}

	public void setMaf2maf(String maf2maf)
	{
		this.maf2maf = maf2maf;
	}

	public String getVcf2maf()
	{
		return vcf2maf;
	}

	public void setVcf2maf(String vcf2maf)
	{
		this.vcf2maf = vcf2maf;
	}

	public String getVepPath()
	{
		return vepPath;
	}

	public void setVepPath(String vepPath)
	{
		this.vepPath = vepPath;
	}

	public String getVepData()
	{
		return vepData;
	}

	public void setVepData(String vepData)
	{
		this.vepData = vepData;
	}

	public String getRefFasta()
	{
		return refFasta;
	}

	public void setRefFasta(String refFasta)
	{
		this.refFasta = refFasta;
	}

	public String getExcludeCols()
	{
		return excludeCols;
	}

	public void setExcludeCols(String excludeCols)
	{
		this.excludeCols = excludeCols;
	}

	public String getAnnotator()
	{
		return annotator;
	}

	public void setAnnotator(String annotator)
	{
		this.annotator = annotator;
	}
}
