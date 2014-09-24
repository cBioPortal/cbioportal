/** Copyright (c) 2012 Memorial Sloan-Kettering Cancer Center.
 *
 * This library is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY, WITHOUT EVEN THE IMPLIED WARRANTY OF
 * MERCHANTABILITY OR FITNESS FOR A PARTICULAR PURPOSE.  The software and
 * documentation provided hereunder is on an "as is" basis, and
 * Memorial Sloan-Kettering Cancer Center 
 * has no obligations to provide maintenance, support,
 * updates, enhancements or modifications.  In no event shall
 * Memorial Sloan-Kettering Cancer Center
 * be liable to any party for direct, indirect, special,
 * incidental or consequential damages, including lost profits, arising
 * out of the use of this software and its documentation, even if
 * Memorial Sloan-Kettering Cancer Center 
 * has been advised of the possibility of such damage.
*/

// package
package org.mskcc.cbio.importer.fetcher.internal;

// imports
import org.mskcc.cbio.importer.*;
import org.mskcc.cbio.importer.model.*;
import org.mskcc.cbio.importer.util.*;
import org.mskcc.cbio.importer.util.soap.*;
import org.mskcc.cbio.importer.dao.ImportDataRecordDAO;
import org.mskcc.cbio.maf.*;
import org.mskcc.cbio.portal.scripts.ImportClinicalData;

import org.w3c.dom.*;
import org.xml.sax.*;
import org.foundation.*;
import org.apache.commons.logging.*;
import com.sun.xml.ws.fault.ServerSOAPFaultException;
import org.springframework.beans.factory.annotation.Value;

import java.io.*;
import java.util.*;
import javax.xml.parsers.*;

class FoundationFetcherImpl implements Fetcher
{
	public static final String CANCER_STUDY = "prad/mskcc/foundation";
	public static final String MUTATION_METADATA = "mutation-foundation";
	public static final String CNA_METADATA = "cna-foundation";
	public static final String FUSION_METADATA = "fusion";

	// some default data values
	public static final String DEFAULT_CENTER = "foundation";
	public static final String DEFAULT_BUILD = "37";
	public static final String DEFAULT_METHOD = "NA";
	public static final String UNKNOWN = "unknown";
	public static final String IN_FRAME = "in-frame";
	public static final String OUT_OF_FRAME = "out of frame";

	private static final String FOUNDATION_FILE_EXTENSION = ".xml";
	private static final Log LOG = LogFactory.getLog(FoundationFetcherImpl.class);

    private static final List<String> patientClinicalAttributes = initializePatientClinicalAttributes();
    private static List<String> initializePatientClinicalAttributes()
    {
        String[] attributes = { "PATIENT_ID", "GENDER", "SAMPLE_ID", "FMI_CASE_ID", "PIPELINE_VER",
        						"TUMOR_NUCLEI_PERCENT", "MEDIAN_COV", "COV>100X", "ERROR_PERCENT" };
        return Arrays.asList(attributes);
    }

	private Config config;
	private FileUtils fileUtils;
	private ImportDataRecordDAO importDataRecordDAO;
	private DatabaseUtils databaseUtils;
	private DataSourcesMetadata dataSourceMetadata;

	public FoundationFetcherImpl(Config config, FileUtils fileUtils,
								 DatabaseUtils databaseUtils, ImportDataRecordDAO importDataRecordDAO) {

		// set members
		this.config = config;
		this.fileUtils = fileUtils;
		this.databaseUtils = databaseUtils;
		this.importDataRecordDAO = importDataRecordDAO;
	}

	// service username
	private String serviceUser;
	@Value("${foundation.service.user}")
	public void setServiceUser(String serviceUser) { this.serviceUser = serviceUser; }

	public String getServiceUser() { return this.serviceUser; }

	// service password
	private String servicePassword;
	@Value("${foundation.service.password}")
	public void setServicePassword(String servicePassword) { this.servicePassword = servicePassword; }

	public String getServicePassword() { return this.servicePassword; }

	/**
	 * Fetches genomic data from an external datasource and
	 * places in download directory for processing.
	 *
	 * @param dataSource String
	 * @param desiredRunDate String
	 * @throws Exception
	 */
	@Override
	public void fetch(String dataSource, String desiredRunDate) throws Exception {

		if (LOG.isInfoEnabled()) {
			LOG.info("fetch(), dateSource:runDate: " + dataSource + ":" + desiredRunDate);
		}

		// get our DataSourcesMetadata object
		Collection<DataSourcesMetadata> dataSourcesMetadata = config.getDataSourcesMetadata(dataSource);
		if (dataSourcesMetadata.isEmpty()) {
			throw new IllegalArgumentException("cannot instantiate a proper DataSourcesMetadata object.");			
		}
		this.dataSourceMetadata = dataSourcesMetadata.iterator().next();

		if (LOG.isInfoEnabled()) {
			LOG.info("fetch(), creating CaseInfoService endpoint.");
		}

		// enable this to get data from the remote service...
//		CaseInfoService caseInfoService = new CaseInfoService();
//		this.authenticate(caseInfoService);
//		ICaseInfoService foundationService = caseInfoService.getICaseInfoService();

		for (File input: this.getStudyFiles())
		{
			this.fetchStudy(input);
		}
	}

	private void fetchStudy(File inputXml) throws Exception
	{
		String inputFilename = inputXml.getAbsolutePath();

		ICaseInfoService foundationService = new FoundationLocalService(inputFilename);

		String outputDir = inputXml.getName().
				substring(0, inputXml.getName().lastIndexOf(".")).
				replaceAll("_", "/");

		NodeList cases = this.fetchCaseList(foundationService);

		// clinical data content
		StringBuilder dataPatientClinicalContent = new StringBuilder();

		// mutation data content
		StringBuilder dataMutationsContent = new StringBuilder();

		// fusion data content
		StringBuilder dataFusionsContent = new StringBuilder();

		// CNA data content
		HashMap<String, Integer> valueMap = new HashMap<String, Integer>();
		Set<String> caseSet = new HashSet<String>();
		Set<String> geneSet = new HashSet<String>();

		DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
		DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();

		int numCases = 0; // total number of processed cases

		for (int lc = 0; lc < cases.getLength(); lc++)
		{
			String caseRecord = this.fetchCaseRecord(cases.item(lc),
				foundationService);

			// skip empty/unavailable records
			if (caseRecord != null)
			{
				Document doc = dBuilder.parse(new InputSource(
						new StringReader(caseRecord)));

				this.addClinicalData(doc, dataPatientClinicalContent);
				this.addMutationData(doc, dataMutationsContent);
				this.addFusionData(doc, dataFusionsContent);
				this.addCNAData(doc, valueMap, caseSet, geneSet);
				this.generateCaseFile(doc, caseRecord, outputDir);

				numCases++;
			}
		}

		// generate data files
		this.generateClinicalDataFile(dataPatientClinicalContent, patientClinicalAttributes, DatatypeMetadata.CLINICAL_FILENAME, outputDir);
		this.generateMutationDataFile(dataMutationsContent, outputDir);
		this.generateFusionDataFile(dataFusionsContent, outputDir);
		this.generateCNADataFile(valueMap, caseSet, geneSet, outputDir);

		// generate meta files
		this.generateStudyMetaFile(numCases, outputDir);
		this.generateMutationMetaFile(numCases, outputDir);
		this.generateFusionMetaFile(numCases, outputDir);
		this.generateCNAMetaFile(numCases, outputDir);
	}

	private File[] getStudyFiles()
	{
		File dlDir = new File(this.dataSourceMetadata.getDownloadDirectory());

		if (dlDir.isDirectory())
		{
			return dlDir.listFiles(new FilenameFilter()
			{
				@Override
				public boolean accept(File dir, String name)
				{
					return name.toLowerCase().endsWith(".xml");
				}
			});
		}

		return null;
	}

	/**
	 * Adds credentials to the outbound SOAP header for the given service.
	 *
	 * @param service   service requiring SOAP authentication
	 */
	private void authenticate(CaseInfoService service)
	{
		HeaderHandlerResolver resolver = new HeaderHandlerResolver();
		resolver.getSecurityHandler().setServiceUser(this.getServiceUser());
		resolver.getSecurityHandler().setServicePassword(this.getServicePassword());
		service.setHandlerResolver(resolver);
	}

	/**
	 * Writes a single data file for the given case to the download directory.
	 *
	 * @param caseDoc   document object containing case data
	 * @param content   actual content of the file to generate
	 * @return          data file representing a single case
	 */
	protected File generateCaseFile(Document caseDoc,
			String content,
			String outputDir) throws Exception
	{
		File caseFile = null;
		Element caseNode = this.extractCaseNode(caseDoc);

		if (caseNode != null)
		{
			String fmiCaseID = caseNode.getAttribute("fmiCase");

			caseFile = fileUtils.createFileWithContents(dataSourceMetadata.getDownloadDirectory() +
				File.separator + fmiCaseID + FOUNDATION_FILE_EXTENSION,
					content);
		}

		return caseFile;
	}

	protected File generateClinicalDataFile(StringBuilder content, List<String> clinicalAttributes, String filename, String outputDir) throws Exception
	{
		StringBuilder headerBuilder = new StringBuilder();
        headerBuilder.append(MetadataUtils.getClinicalMetadataHeaders(config, clinicalAttributes));
        for (String attribute : clinicalAttributes) {
            headerBuilder.append(attribute.toUpperCase() + ImportClinicalData.DELIMITER);
        }
        String header = headerBuilder.toString().trim() + "\n";

		File clinicalFile = fileUtils.createFileWithContents(
			dataSourceMetadata.getDownloadDirectory() + File.separator + outputDir + File.separator + filename,
			header + content.toString());

		return clinicalFile;
	}

	protected File generateFusionDataFile(StringBuilder content,
			String outputDir) throws Exception
	{
		String header = MafUtil.HUGO_SYMBOL + "\t" +
		                MafUtil.ENTREZ_GENE_ID + "\t" +
		                MafUtil.CENTER + "\t" +
		                MafUtil.TUMOR_SAMPLE_BARCODE + "\t" +
		                FusionFileUtil.FUSION + "\t" +
		                FusionFileUtil.DNA_SUPPORT + "\t" +
		                FusionFileUtil.RNA_SUPPORT + "\t" +
		                FusionFileUtil.METHOD + "\t" +
		                FusionFileUtil.FRAME + "\n";

		File fusionFile = fileUtils.createFileWithContents(
				dataSourceMetadata.getDownloadDirectory() + File.separator +
					outputDir + File.separator +
					DatatypeMetadata.FUSIONS_STAGING_FILENAME,
				header + content.toString());

		return fusionFile;
	}

	protected File generateMutationDataFile(StringBuilder content,
			String outputDir) throws Exception
	{
		String header = MafUtil.HUGO_SYMBOL + "\t" +
		                MafUtil.CENTER + "\t" +
		                MafUtil.NCBI_BUILD + "\t" +
		                MafUtil.CHROMOSOME+ "\t" +
		                MafUtil.START_POSITION + "\t" +
		                MafUtil.END_POSITION + "\t" +
		                MafUtil.STRAND + "\t" +
		                MafUtil.VARIANT_CLASSIFICATION + "\t" +
		                MafUtil.REFERENCE_ALLELE + "\t" +
		                MafUtil.TUMOR_SEQ_ALLELE_1 + "\t" +
		                MafUtil.TUMOR_SEQ_ALLELE_2 + "\t" +
		                MafUtil.TUMOR_SAMPLE_BARCODE + "\t" +
		                MafUtil.VALIDATION_STATUS + "\t" +
		                MafUtil.MUTATION_STATUS + "\t" +
		                MafUtil.AMINO_ACID_CHANGE_MANNUAL + "\t" +
		                MafUtil.TRANSCRIPT + "\t" +
		                MafUtil.T_REF_COUNT + "\t" +
		                MafUtil.T_ALT_COUNT + "\n";

		File mafFile = fileUtils.createFileWithContents(
			dataSourceMetadata.getDownloadDirectory() + File.separator +
				outputDir + File.separator +
				DatatypeMetadata.MUTATIONS_STAGING_FILENAME,
			header + content.toString());

		return mafFile;
	}

	protected File generateCNADataFile(HashMap<String, Integer> valueMap,
			Set<String> caseSet,
			Set<String> geneSet,
			String outputDir) throws Exception
	{
		StringBuilder content = new StringBuilder();

		// generate header line

		content.append(Converter.GENE_SYMBOL_COLUMN_HEADER_NAME);

		for (String caseID : caseSet)
		{
			content.append("\t");
			content.append(caseID);
		}

		content.append("\n");

		// generate line for each gene
		for (String gene : geneSet)
		{
			content.append(gene);

			for (String caseId : caseSet)
			{
				Integer value = valueMap.get(gene + "_" + caseId);

				if (value == null)
				{
					value = 0;
				}

				content.append("\t");
				content.append(value.toString());
			}

			content.append("\n");
		}

		DatatypeMetadata metadata = this.getDatatypeMetadata("cna-foundation");

		File cnaFile = fileUtils.createFileWithContents(
				dataSourceMetadata.getDownloadDirectory() + File.separator +
					outputDir + File.separator +
					metadata.getStagingFilename(),
				content.toString());

		return cnaFile;
	}

	protected void generateStudyMetaFile(Integer numCases, String outputDir) throws Exception
	{
		CancerStudyMetadata cancerMetadata = this.config.getCancerStudyMetadataByName(CANCER_STUDY);

		this.fileUtils.writeCancerStudyMetadataFile(
			dataSourceMetadata.getDownloadDirectory() + File.separator + outputDir,
			cancerMetadata,
			numCases);
	}

	protected void generateCNAMetaFile(Integer numCases, String outputDir) throws Exception
	{
		DatatypeMetadata datatypeMetadata = this.getDatatypeMetadata(CNA_METADATA);
		CancerStudyMetadata cancerMetadata = this.config.getCancerStudyMetadataByName(CANCER_STUDY);

		this.fileUtils.writeMetadataFile(
			dataSourceMetadata.getDownloadDirectory() + File.separator + outputDir,
			cancerMetadata,
			datatypeMetadata,
			numCases);
	}

	protected void generateMutationMetaFile(Integer numCases, String outputDir) throws Exception
	{
		DatatypeMetadata datatypeMetadata = this.getDatatypeMetadata(MUTATION_METADATA);
		CancerStudyMetadata cancerMetadata = this.config.getCancerStudyMetadataByName(CANCER_STUDY);

		this.fileUtils.writeMetadataFile(
			dataSourceMetadata.getDownloadDirectory() + File.separator + outputDir,
			cancerMetadata,
			datatypeMetadata,
			numCases);
	}

	protected void generateFusionMetaFile(Integer numCases, String outputDir) throws Exception
	{
		DatatypeMetadata datatypeMetadata = this.getDatatypeMetadata(FUSION_METADATA);
		CancerStudyMetadata cancerMetadata = this.config.getCancerStudyMetadataByName(CANCER_STUDY);

		this.fileUtils.writeMetadataFile(
			dataSourceMetadata.getDownloadDirectory() + File.separator + outputDir,
			cancerMetadata,
			datatypeMetadata,
			numCases);
	}

	protected void addClinicalData(Document caseDoc, StringBuilder patientContent)
	{
		String fmiCaseID = "";
		String caseID = "";
		String gender = "";
		String pipelineVer = "";
		String percentTumorNuclei = "";
		String medianCov = "";
		String covGreaterThan100x = "";
		String errorPercent = "";

		Element caseNode = this.extractCaseNode(caseDoc);

		if (caseNode == null)
		{
			return; // no case to process
		}

		fmiCaseID = caseNode.getAttribute("fmiCase");
		caseID = this.extractCaseId(caseNode);

		Element variantReport = this.extractVariantReport(caseNode);

		if (variantReport != null)
		{
			gender = variantReport.getAttribute("gender");
			pipelineVer = variantReport.getAttribute("pipeline-version");

			NodeList samples = variantReport.getElementsByTagName("sample");
			Node sample = samples.item(0); // assuming there is only one sample

			if (sample.getNodeType() == Node.ELEMENT_NODE)
			{
				percentTumorNuclei = ((Element)sample).getAttribute("percent-tumor-nuclei");
			}

			NodeList metrics = variantReport.getElementsByTagName("metric");

			for (int i = 0; i < metrics.getLength(); i++)
			{
				Node metric = metrics.item(i);

				if (metric.getNodeType() == Node.ELEMENT_NODE)
				{
					String name = ((Element)metric).getAttribute("name");
					String value = ((Element)metric).getAttribute("value");

					if (name.equalsIgnoreCase("median coverage"))
					{
						medianCov = value;
					}
					else if (name.equalsIgnoreCase("coverage >100x"))
					{
						covGreaterThan100x = value;
					}
					else if (name.equalsIgnoreCase("error"))
					{
						errorPercent = value;
					}
				}
			}
		}

        patientContent.append(caseID);
		patientContent.append("\t");
        patientContent.append(gender);
		patientContent.append("\t");
        patientContent.append(caseID);
		patientContent.append("\t");
		patientContent.append(fmiCaseID);
		patientContent.append("\t");
		patientContent.append(pipelineVer);
		patientContent.append("\t");
		patientContent.append(percentTumorNuclei);
		patientContent.append("\t");
		patientContent.append(medianCov);
		patientContent.append("\t");
		patientContent.append(covGreaterThan100x);
		patientContent.append("\t");
		patientContent.append(errorPercent);
		patientContent.append("\n");
	}

	protected void addFusionData(Document caseDoc, StringBuilder content)
	{
		Element caseNode = this.extractCaseNode(caseDoc);

		if (caseNode == null)
		{
			return; // no case to process
		}

		String caseID = this.extractCaseId(caseNode);

		Element variantReport = this.extractVariantReport(caseNode);

		if (variantReport != null)
		{
			NodeList rearrangements = variantReport.getElementsByTagName("rearrangement");

			for (int i = 0; i < rearrangements.getLength(); i++)
			{
				Node rearrangement = rearrangements.item(i);

				if (rearrangement.getNodeType() == Node.ELEMENT_NODE)
				{
					String inFrame = ((Element)rearrangement).getAttribute("in-frame");
					String targetedGene = ((Element)rearrangement).getAttribute("targeted-gene");
					String otherGene = ((Element)rearrangement).getAttribute("other-gene");
					//String position1 = ((Element)shortVar).getAttribute("pos1");
					//String position2 = ((Element)shortVar).getAttribute("pos2");
					String supportingReadPairs = ((Element)rearrangement).getAttribute("supporting-read-pairs");
					String description = ((Element)rearrangement).getAttribute("description");
					String status = ((Element)rearrangement).getAttribute("status");

					String frame = this.parseInFrame(inFrame);
					this.appendFusionData(content, targetedGene, otherGene, caseID, frame);
				}
			}
		}
	}

	protected void appendFusionData(StringBuilder content,
			String targetedGene,
			String otherGene,
			String caseId,
			String frame)
	{
		// append the data as a single line
		content.append(targetedGene); // Hugo_Symbol
		content.append("\t");
		content.append(""); // Entrez_Gene_Id
		content.append("\t");
		content.append(DEFAULT_CENTER); // Center
		content.append("\t");
		content.append(caseId); // Tumor_Sample_Barcode
		content.append("\t");
		content.append(targetedGene); // Fusion
		content.append("-");
		content.append(otherGene);
		content.append(" fusion"); // Fusion
		content.append("\t");
		content.append("yes"); // DNA support
		content.append("\t");
		content.append(UNKNOWN); // RNA support
		content.append("\t");
		content.append(DEFAULT_METHOD); // Method
		content.append("\t");
		content.append(frame);
		content.append("\n");
	}

	protected String parseInFrame(String inFrame)
	{
		String value = UNKNOWN;

		if (inFrame.equalsIgnoreCase("yes"))
		{
			value = IN_FRAME;
		}
		else if (inFrame.equalsIgnoreCase("no"))
		{
			value = OUT_OF_FRAME;
		}

		return value;
	}

	protected void addMutationData(Document caseDoc, StringBuilder content)
	{
		Element caseNode = this.extractCaseNode(caseDoc);

		if (caseNode == null)
		{
			return; // no case to process
		}

		//fmiCaseID = caseNode.getAttribute("fmiCase");
		String caseID = this.extractCaseId(caseNode);

		Element variantReport = this.extractVariantReport(caseNode);

		if (variantReport != null)
		{
			NodeList shortVariants = variantReport.getElementsByTagName("short-variant");

			for (int i = 0; i < shortVariants.getLength(); i++)
			{
				Node shortVar = shortVariants.item(i);

				if (shortVar.getNodeType() == Node.ELEMENT_NODE)
				{
					String depth = ((Element)shortVar).getAttribute("depth");
					String percentReads = ((Element)shortVar).getAttribute("percent-reads");
					String position = ((Element)shortVar).getAttribute("position");
					String cdsEffect = ((Element)shortVar).getAttribute("cds-effect");

					String gene = ((Element)shortVar).getAttribute("gene");
					String proteinEffect = ((Element)shortVar).getAttribute("protein-effect");
					String status = ((Element)shortVar).getAttribute("status");
					String transcript = ((Element)shortVar).getAttribute("transcript");
					String strand = ((Element)shortVar).getAttribute("strand");
					String functionalEffect = ((Element)shortVar).getAttribute("functional-effect");

					Map<String, String> posInfo = this.parsePosition(position);

					String chromosome = this.parseChr(posInfo.get("chromosome"));
					String startPos = posInfo.get("startPos");
					String endPos = posInfo.get("endPos");
					String tAltCount = this.calcTumorAltCount(percentReads, depth);
					String tRefCount = this.calcTumorRefCount(percentReads, depth);

					Map<String, String> alleleInfo = this.parseCdsEffect(cdsEffect, strand);

					String refAllele = alleleInfo.get("refAllele");
					String varAllele = alleleInfo.get("varAllele");

					// make sure we have an end position
					if (endPos.length() == 0)
					{
						endPos = this.calcEndPos(startPos, refAllele);
					}

					this.appendMutationData(content,
							gene,
							chromosome,
							startPos,
							endPos,
							strand,
							functionalEffect,
							refAllele,
							varAllele,
							caseID,
							proteinEffect,
							transcript,
							tRefCount,
							tAltCount);
				}
			}
		}
	}

	protected String parseChr(String chromosome)
	{
		String chr = chromosome;

		if (chromosome == null)
		{
			chr = "";
		}
		else if (chromosome.toLowerCase().startsWith("chr"))
		{
			chr = chromosome.substring(("chr").length());
		}

		return chr;
	}

	protected void appendMutationData(StringBuilder content,
			String gene,
			String chromosome,
			String startPos,
			String endPos,
			String strand,
			String functionalEffect,
			String refAllele,
			String varAllele,
			String caseID,
			String proteinEffect,
			String transcript,
			String tRefCount,
			String tAltCount)
	{
		// append the data as a single line
		content.append(gene); // hugo_symbol
		content.append("\t");
		content.append(DEFAULT_CENTER); // Center
		content.append("\t");
		content.append(DEFAULT_BUILD); // NCBI_build (assuming it is always hg19/37)
		content.append("\t");
		content.append(chromosome);
		content.append("\t");
		content.append(startPos);
		content.append("\t");
		content.append(endPos);
		content.append("\t");
		content.append(strand);
		content.append("\t");
		content.append(functionalEffect); // variant_classification
		content.append("\t");
		content.append(refAllele); // reference_allele
		content.append("\t");
		content.append(varAllele); // tumor_seq_allele1
		content.append("\t");
		content.append(varAllele); // tumor_seq_allele2 (copy first one, we have no other var allele)
		content.append("\t");
		content.append(caseID); // tumor_sample_barcode
		content.append("\t");
		content.append(UNKNOWN); // validation_status
		content.append("\t");
		content.append(UNKNOWN); // mutation_status
		content.append("\t");
		content.append(proteinEffect); // amino_acid_change
		content.append("\t");
		content.append(transcript);
		content.append("\t");
		content.append(tRefCount);
		content.append("\t");
		content.append(tAltCount);
		content.append("\n");
	}

	/**
	 * Parses cds-effect string and creates a map for reference and variant
	 * alleles. If the input string has a missing value, corresponding value
	 * in the map will be an empty string.
	 *
	 * @param cdsEffect string to parse
	 * @param strand    - or +
	 * @return          map of parsed values
	 */
	protected Map<String, String> parseCdsEffect(String cdsEffect, String strand)
	{
		String refAllele = "";
		String varAllele = "";

		cdsEffect = cdsEffect.toLowerCase();

		int insIdx = cdsEffect.indexOf("ins");
		int delIdx = cdsEffect.indexOf("del");
		int changeIdx = cdsEffect.indexOf(">");

		// insertion
		if (insIdx > 0)
		{
			refAllele = "-";
			varAllele = cdsEffect.substring(insIdx + ("ins").length());
		}
		// deletion
		else if (delIdx > 0)
		{
			varAllele = "-";
			refAllele = cdsEffect.substring(delIdx + ("del").length());
		}
		// nucleotide change
		else if (changeIdx > 0)
		{
			// rip off everything except nucleotides
			String ripped = cdsEffect.replaceAll("[^tcga]", " ").trim();
			String[] parts = ripped.split(" ");

			if (parts.length < 2)
			{
				LOG.info("parseCdsEffect(), unable to process: " + cdsEffect);
			}
			else
			{
				refAllele = parts[0];
				varAllele = parts[1];
			}
		}

		// take complement if strand is minus
		if (strand.equals("-"))
		{
			refAllele = this.complementOf(refAllele);
			varAllele = this.complementOf(varAllele);
		}

		// make them uppercase for consistency
		varAllele = varAllele.toUpperCase();
		refAllele = refAllele.toUpperCase();

		Map<String, String> map = new HashMap<String, String>();

		map.put("refAllele", refAllele);
		map.put("varAllele", varAllele);

		return map;
	}

	/**
	 * Returns complement of the given nucleotide sequence.
	 *
	 * @param sequence  a nucleotide sequence
	 * @return          complement of the sequence
	 */
	protected String complementOf(String sequence)
	{
		// check if it is a nucleotide sequence
		if (!sequence.matches("[TCGAtcga]+"))
		{
			return sequence;
		}

		sequence = sequence.toUpperCase();

		Map<Character, Character> map = new HashMap<Character, Character>();

		map.put('T', 'A');
		map.put('A', 'T');
		map.put('G', 'C');
		map.put('C', 'G');

		StringBuilder complement = new StringBuilder();

		for (int i = 0; i < sequence.length(); i++)
		{
			complement.append(map.get(sequence.charAt(i)));
		}

		return complement.toString();
	}

	/**
	 * Parses position string and creates a map for chromosome, startPos and
	 * endPos values. If the position string has a missing value, corresponding
	 * value in the map will be an empty string.
	 *
	 * @param position  expected format chrPos:startPos-endPos
	 * @return  map of parsed values
	 */
	protected Map<String, String> parsePosition(String position)
	{
		String chromosome = "";
		String startPos = "";
		String endPos = "";

		// extract position information

		String[] parts = position.split(":");

		if (parts.length > 1)
		{
			chromosome = parts[0];
			parts = parts[1].split("-");

			if (parts.length > 0)
			{
				startPos = parts[0];
			}

			if (parts.length > 1)
			{
				endPos = parts[1];
			}
		}

		Map<String, String> map = new HashMap<String, String>();

		map.put("chromosome", chromosome);
		map.put("startPos", startPos);
		map.put("endPos", endPos);

		return map;
	}

	/**
	 * Calculates the end position of the mutation by the help of
	 * start position and reference allele info.
	 *
	 * @param startPos  start position as a string
	 * @param refAllele reference allele (assumed to be T, C, G, A, or -
	 * @return  end position as a string
	 */
	protected String calcEndPos(String startPos, String refAllele)
	{
		long startPosition = Long.parseLong(startPos);
		long endPosition = -1L;

		if (refAllele.matches("[TCGAtcga]+"))
		{
			endPosition = startPosition + refAllele.length() - 1;
		}
		else if (refAllele.equals("-"))
		{
			endPosition = startPosition + 1;
		}

		String endPos = "";

		if (endPosition > 0)
		{
			endPos = Long.toString(endPosition);
		}

		return endPos;
	}

	/**
	 * Calculates tumor alt count value by using percent reads and depth.
	 *
	 * @param percentReads
	 * @param depth
	 * @return  calculated tumor alt count
	 */
	protected String calcTumorAltCount(String percentReads, String depth)
	{
		String tAltCount = "";

		// try to calculate allele counts
		try {
			long tumorAltCount = Math.round(Long.parseLong(depth) *
			                                Double.parseDouble(percentReads) / 100);

			tAltCount = Long.toString(tumorAltCount);
		} catch (NumberFormatException e) {
			// empty or invalid depth & percent values
		}

		return tAltCount;
	}

	/**
	 * Calculates tumor ref count value by using percent reads and depth.
	 *
	 * @param percentReads
	 * @param depth
	 * @return  calculated tumor ref count
	 */
	protected String calcTumorRefCount(String percentReads, String depth)
	{
		String tRefCount = "";

		// try to calculate allele counts
		try {
			long tumorAltCount = Math.round(Long.parseLong(depth) *
			                                Double.parseDouble(percentReads) / 100);
			long tumorRefCount = Long.parseLong(depth) - tumorAltCount;

			tRefCount = Long.toString(tumorRefCount);
		} catch (NumberFormatException e) {
			// empty or invalid depth & percent values
		}

		return tRefCount;
	}

	protected void addCNAData(Document caseDoc,
			HashMap<String,Integer> valueMap,
			Set<String> caseSet,
			Set<String> geneSet)
	{
		String gene = "";
		String type = "";

		Element caseNode = this.extractCaseNode(caseDoc);

		if (caseNode == null)
		{
			return; // no case to process
		}

		//fmiCaseID = caseNode.getAttribute("fmiCase");
		String caseID = this.extractCaseId(caseNode);

		caseSet.add(caseID);

		Element variantReport = this.extractVariantReport(caseNode);

		if (variantReport != null)
		{
			NodeList cnas = variantReport.getElementsByTagName("copy-number-alteration");

			for (int i = 0; i < cnas.getLength(); i++)
			{
				Node cna = cnas.item(i);

				if (cna.getNodeType() == Node.ELEMENT_NODE)
				{
					gene = ((Element)cna).getAttribute("gene");
					type = ((Element)cna).getAttribute("type");

					if (gene.length() > 0)
					{
						geneSet.add(gene);

						valueMap.put(gene + "_" + caseID, this.cnaValue(type));
					}
				}
			}
		}
	}

	protected Integer cnaValue(String type)
	{
		if (type.equalsIgnoreCase("loss"))
		{
			return -2;
		}
		else if (type.equalsIgnoreCase("amplification"))
		{
			return 2;
		}
		else
		{
			return 0;
		}
	}

	protected Element extractCaseNode(Document caseDoc)
	{
		NodeList cases = caseDoc.getElementsByTagName("Case");

		if (cases.getLength() == 0)
		{
			return null; // no case to process
		}

		// process only the first case
		// (assuming each document has only one distinct case)
		Node caseNode = cases.item(0);

		if (caseNode.getNodeType() != Node.ELEMENT_NODE)
		{
			return null; // no case to process
		}

		return (Element)caseNode;
	}

	protected Element extractVariantReport(Element caseNode)
	{
		NodeList variantReports = caseNode.getElementsByTagName("variant-report");
		Node variantReport = variantReports.item(0); // assuming there is only one variant report

		if (variantReport.getNodeType() != Node.ELEMENT_NODE)
		{
			return null;
		}

		return (Element)variantReport;
	}

	protected String fetchCaseRecord(Node caseNode, ICaseInfoService foundationService)
	{
		String record = null;

		if (caseNode.getNodeType() == Node.ELEMENT_NODE)
		{
			String fmiCaseID = ((Element)caseNode).getAttribute("fmiCase");
			String caseID = ((Element)caseNode).getAttribute("case");
			String hasVariant = ((Element)caseNode).getAttribute("hasVariant");

			// skip cases with no information
			if (hasVariant.equals("false"))
			{
				LOG.info("fetch(), record has no variant data: " + fmiCaseID);
				return null;
			}

			if (LOG.isInfoEnabled()) {
				LOG.info("fetch(), fetching case : " + caseID);
			}

			try {
				record =  foundationService.getCase(caseID);
			} catch (ServerSOAPFaultException e) {
				// we get here if record does not exist on server side (yet)
				if (LOG.isInfoEnabled()) {
					LOG.info("fetch(), Cannot fetch case record for case: " + caseID);
				}
			}
		}

		return record;
	}

	protected NodeList fetchCaseList(ICaseInfoService foundationService)
			throws ParserConfigurationException, IOException, SAXException
	{
		if (LOG.isInfoEnabled()) {
			LOG.info("fetch(), fetching case list.");
		}

		DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
		DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
		Document doc = dBuilder.parse(new InputSource(new StringReader(foundationService.getCaseList())));

		return doc.getElementsByTagName("Case");
	}

	/**
	 * Fetches reference data from an external datasource.
	 *
     * @param referenceMetadata ReferenceMetadata
	 * @throws Exception
	 */
	@Override
	public void fetchReferenceData(ReferenceMetadata referenceMetadata) throws Exception {
		throw new UnsupportedOperationException();
	}

	/**
	 * Retrieves the first matching DatatypeMetadata instance
	 * for the given datatype string.
	 *
	 * @param datatype  data type as a string
	 * @return          corresponding DatatypeMetadata
	 */
	private DatatypeMetadata getDatatypeMetadata(String datatype)
	{
		Collection<DatatypeMetadata> list = this.config.getDatatypeMetadata(datatype);
		return list.iterator().next();
	}

	/**
	 * Extracts case ID from the given node. Also replaces any whitespace
	 * character with an underscore.
	 *
	 * @param caseNode  caseNode containing the case ID information
	 * @return          case ID as a string
	 */
	private String extractCaseId(Element caseNode)
	{
		return caseNode.getAttribute("case").replaceAll("\\s", "_");
	}
}
