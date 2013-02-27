/** Copyright (c) 2012 Memorial Sloan-Kettering Cancer Center.
**
** This library is free software; you can redistribute it and/or modify it
** under the terms of the GNU Lesser General Public License as published
** by the Free Software Foundation; either version 2.1 of the License, or
** any later version.
**
** This library is distributed in the hope that it will be useful, but
** WITHOUT ANY WARRANTY, WITHOUT EVEN THE IMPLIED WARRANTY OF
** MERCHANTABILITY OR FITNESS FOR A PARTICULAR PURPOSE.  The software and
** documentation provided hereunder is on an "as is" basis, and
** Memorial Sloan-Kettering Cancer Center 
** has no obligations to provide maintenance, support,
** updates, enhancements or modifications.  In no event shall
** Memorial Sloan-Kettering Cancer Center
** be liable to any party for direct, indirect, special,
** incidental or consequential damages, including lost profits, arising
** out of the use of this software and its documentation, even if
** Memorial Sloan-Kettering Cancer Center 
** has been advised of the possibility of such damage.  See
** the GNU Lesser General Public License for more details.
**
** You should have received a copy of the GNU Lesser General Public License
** along with this library; if not, write to the Free Software Foundation,
** Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA.
**/

// package
package org.mskcc.cbio.importer.fetcher.internal;

// imports
import org.mskcc.cbio.importer.Config;
import org.mskcc.cbio.importer.Fetcher;
import org.mskcc.cbio.importer.FileUtils;
import org.mskcc.cbio.importer.DatabaseUtils;
import org.mskcc.cbio.importer.model.DatatypeMetadata;
import org.mskcc.cbio.importer.model.ReferenceMetadata;
import org.mskcc.cbio.importer.model.DataSourcesMetadata;
import org.mskcc.cbio.importer.dao.ImportDataRecordDAO;

import org.foundation.*;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.sun.xml.ws.fault.ServerSOAPFaultException;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.StringReader;
import java.util.*;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 * Class which implements the fetcher interface.
 */
class FoundationFetcherImpl implements Fetcher {

	// our logger
	private static final Log LOG = LogFactory.getLog(FoundationFetcherImpl.class);

	// foundation data file extension
	private static final String FOUNDATION_FILE_EXTENSION = ".xml";

	// not all fields in ImportDataRecord will be used
	private static final String UNUSED_IMPORT_DATA_FIELD = "NA";

	// ref to configuration
	private Config config;

	// ref to file utils
	private FileUtils fileUtils;

	// ref to import data
	private ImportDataRecordDAO importDataRecordDAO;

	// ref to database utils
	private DatabaseUtils databaseUtils;

	// download directories
	private DataSourcesMetadata dataSourceMetadata;

	/**
	 * Constructor.
     *
     * @param config Config
	 * @param fileUtils FileUtils
	 * @param databaseUtils DatabaseUtils
	 * @param importDataRecordDAO ImportDataRecordDAO;
	 */
	public FoundationFetcherImpl(Config config, FileUtils fileUtils,
								 DatabaseUtils databaseUtils, ImportDataRecordDAO importDataRecordDAO) {

		// set members
		this.config = config;
		this.fileUtils = fileUtils;
		this.databaseUtils = databaseUtils;
		this.importDataRecordDAO = importDataRecordDAO;
	}

	/**
	 * Fetchers genomic data from an external datasource and
	 * places in database for processing.
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

		// TODO temporary test to bypass foundation service
//		if (this.testXMLParsers())
//			return;

		CaseInfoService caseInfoService = new CaseInfoService();
		ICaseInfoService foundationService = caseInfoService.getICaseInfoService();

		NodeList cases = this.fetchCaseList(foundationService);

		// clinical data content
		StringBuilder dataClinicalContent = new StringBuilder();

		// mutation data content
		StringBuilder dataMutationsContent = new StringBuilder();

		// CNA data content
		HashMap<String, Integer> valueMap = new HashMap<String, Integer>();
		Set<String> caseSet = new HashSet<String>();
		Set<String> geneSet = new HashSet<String>();

		DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
		DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();

		for (int lc = 0; lc < cases.getLength(); lc++)
		{
			String caseRecord = this.fetchCaseRecord(cases.item(lc),
				foundationService);

			// check for empty records
			if (caseRecord != null &&
			    caseRecord.contains("variant-report"))
			{
				Document doc = dBuilder.parse(new InputSource(
						new StringReader(caseRecord)));

				this.addClinicalData(doc, dataClinicalContent);
				this.addMutationData(doc, dataMutationsContent);
				this.addCNAData(doc, valueMap, caseSet, geneSet);
			}

//			if (caseRecord != null && caseRecord.length() > 250) {
//				File caseFile = fileUtils.createFileWithContents(dataSourceMetadata.getDownloadDirectory() +
//				                                                 File.separator +
//				                                                 fmiCaseID + FOUNDATION_FILE_EXTENSION, caseRecord);
//				if (LOG.isInfoEnabled()) {
//					LOG.info("fetch(), successfully fetched data for case: " + caseID + ", persisting...");
//				}
//				ImportDataRecord importDataRecord = new ImportDataRecord(dataSource, dataSource, UNUSED_IMPORT_DATA_FIELD,
//				                                                         UNUSED_IMPORT_DATA_FIELD, UNUSED_IMPORT_DATA_FIELD,
//				                                                         caseFile.getCanonicalPath(), UNUSED_IMPORT_DATA_FIELD,
//				                                                         fmiCaseID + FOUNDATION_FILE_EXTENSION);
//				importDataRecordDAO.importDataRecord(importDataRecord);
//			}
		}

		// generate data files
		this.generateClinicalDataFile(dataClinicalContent);
		this.generateMutationDataFile(dataMutationsContent);
		this.generateCNADataFile(valueMap, caseSet, geneSet);

		// TODO generate meta files
		this.generateStudyMetaFile();
		this.generateMutationMetaFile();
		this.generateCNAMetaFile();
	}

	// TODO temporary test function (remove when ready)
	protected boolean testXMLParsers() throws Exception
	{
		File dataDir = new File("foundation");

		String[] extensions = {"xml"};
		Collection<File> dirContent = fileUtils.listFiles(dataDir, extensions, false);

		DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
		DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();

		StringBuilder dataClinicalContent = new StringBuilder();
		StringBuilder dataMutationsContent = new StringBuilder();

		HashMap<String, Integer> valueMap = new HashMap<String, Integer>();
		Set<String> caseSet = new HashSet<String>();
		Set<String> geneSet = new HashSet<String>();

		for (File xmlFile : dirContent)
		{
			char[] buffer = new char[(int)xmlFile.length()]; // assuming a short file
			FileReader reader = new FileReader(xmlFile);
			reader.read(buffer, 0, (int)xmlFile.length());
			reader.close();

			String xmlString = new String(buffer);
			Document doc = dBuilder.parse(new InputSource(new StringReader(xmlString)));

			this.addClinicalData(doc, dataClinicalContent);
			this.addMutationData(doc, dataMutationsContent);
			this.addCNAData(doc, valueMap, caseSet, geneSet);
		}

		this.generateClinicalDataFile(dataClinicalContent);
		this.generateMutationDataFile(dataMutationsContent);
		this.generateCNADataFile(valueMap, caseSet, geneSet);

		this.generateStudyMetaFile();
		this.generateMutationMetaFile();
		this.generateCNAMetaFile();

		return true;
	}

	protected File generateClinicalDataFile(StringBuilder content) throws Exception
	{
		String header = "case_id\tgender\tfmi_case_id\tpipeline_ver\t" +
		                "tumor_nuclei_percent\tmedian cov\tcov>100x\terror_percent\n";

		File clinicalFile = fileUtils.createFileWithContents(dataSourceMetadata.getDownloadDirectory() +
			File.separator + "data_clinical.txt", header + content.toString());

		return clinicalFile;
	}

	protected File generateMutationDataFile(StringBuilder content) throws Exception
	{
		String header = "hugo_symbol\tchromosome\tstart_position\tend_position\t" +
		                "strand\tvariant_classification\tmutation_status?\tamino_acid_change\t" +
		                "transcript\tt_ref_count\tt_alt_count\n";

		File mafFile = fileUtils.createFileWithContents(dataSourceMetadata.getDownloadDirectory() +
			File.separator + "data_mutations_extended.txt", header + content.toString());

		return mafFile;
	}

	protected File generateCNADataFile(HashMap<String, Integer> valueMap, Set<String> caseSet,
			Set<String> geneSet) throws Exception
	{
		StringBuilder content = new StringBuilder();

		// generate header line

		content.append("Gene Symbol");

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

		File cnaFile = fileUtils.createFileWithContents(dataSourceMetadata.getDownloadDirectory() +
			File.separator + "data_CNA.txt", content.toString());

		return cnaFile;
	}

	protected File generateStudyMetaFile() throws Exception
	{
		StringBuilder content = new StringBuilder();

		content.append("type_of_cancer: prad\n");
		content.append("cancer_study_identifier: prad_foundation\n");
		content.append("name: Prostate Adenocarcinoma (Foundation)\n");
		content.append("description: TODO\n");

		File studyFile = fileUtils.createFileWithContents(dataSourceMetadata.getDownloadDirectory() +
			File.separator + "meta_study.txt", content.toString());

		return studyFile;
	}

	protected File generateCNAMetaFile() throws Exception
	{
		Collection<DatatypeMetadata> list = this.config.getDatatypeMetadata("cna-foundation");
		DatatypeMetadata metadata = list.iterator().next();

		File cnaFile = fileUtils.createFileWithContents(dataSourceMetadata.getDownloadDirectory() +
				File.separator + metadata.getMetaFilename(), "TODO");

		return cnaFile;
	}

	protected File generateMutationMetaFile() throws Exception
	{
		Collection<DatatypeMetadata> list = this.config.getDatatypeMetadata("mutation");
		DatatypeMetadata metadata = list.iterator().next();

		File cnaFile = fileUtils.createFileWithContents(dataSourceMetadata.getDownloadDirectory() +
			File.separator + metadata.getMetaFilename(), "TODO");

		return cnaFile;
	}

	protected void addClinicalData(Document caseDoc, StringBuilder content)
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
		caseID = caseNode.getAttribute("case");

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

		// append the data as a single line
		content.append(caseID);
		content.append("\t");
		content.append(gender);
		content.append("\t");
		content.append(fmiCaseID);
		content.append("\t");
		content.append(pipelineVer);
		content.append("\t");
		content.append(percentTumorNuclei);
		content.append("\t");
		content.append(medianCov);
		content.append("\t");
		content.append(covGreaterThan100x);
		content.append("\t");
		content.append(errorPercent);
		content.append("\n");
	}

	protected void addMutationData(Document caseDoc, StringBuilder content)
	{
		String gene = "";
		String chromosome = "";
		String startPos = "";
		String endPos = "";
		String proteinEffect = "";
		String status = "";
		String transcript = "";
		String strand = "";
		String functionalEffect = "";
		String tAltCount = "";
		String tRefCount = "";

		Element caseNode = this.extractCaseNode(caseDoc);

		if (caseNode == null)
		{
			return; // no case to process
		}

		// TODO sample barcode?
		//fmiCaseID = caseNode.getAttribute("fmiCase");
		String caseID = caseNode.getAttribute("case");

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

					gene = ((Element)shortVar).getAttribute("gene");
					proteinEffect = ((Element)shortVar).getAttribute("protein-effect");
					status = ((Element)shortVar).getAttribute("status");
					transcript = ((Element)shortVar).getAttribute("transcript");
					strand = ((Element)shortVar).getAttribute("strand");
					functionalEffect = ((Element)shortVar).getAttribute("functional-effect");

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

					// try to calculate allele counts
					try {
						long tumorAltCount = Math.round(Long.parseLong(depth) *
						                                Double.parseDouble(percentReads) / 100);
						long tumorRefCount = Long.parseLong(depth) - tumorAltCount;

						tAltCount = Long.toString(tumorAltCount);
						tRefCount = Long.toString(tumorRefCount);
					} catch (NumberFormatException e) {
						// empty or invalid depth & percent values
					}
				}
			}
		}

		// append the data as a single line
		content.append(gene); // hugo_symbol
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
		content.append(status); // TODO mutation status or validation status or smt else?
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
		String caseID = caseNode.getAttribute("case");

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

		// TODO process all cases instead of only the first one?

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
			System.out.println(caseID);

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
	 * Fetchers reference data from an external datasource.
	 *
     * @param referenceMetadata ReferenceMetadata
	 * @throws Exception
	 */
	@Override
	public void fetchReferenceData(ReferenceMetadata referenceMetadata) throws Exception {
		throw new UnsupportedOperationException();
	}
}
