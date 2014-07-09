package org.foundation;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.*;
import java.util.HashMap;
import java.util.Map;

/**
 * Custom implementation of ICaseInfoService to parse local xml files.
 *
 * @author Selcuk Onur Sumer
 */
public class FoundationLocalService implements ICaseInfoService
{
	private String input;
	private Map<String, String> caseMap;

	public FoundationLocalService(String input)
	{
		this.input = input;
		this.caseMap = new HashMap<String, String>();
	}

	/**
	 * Extract all cases into a map for fast future access.
	 *
	 * @param document  input document as a string
	 * @throws Exception
	 */
	public void extractCases(String document) throws Exception
	{
		DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
		DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();

		Document doc = dBuilder.parse(new InputSource(
				new StringReader(document)));

		NodeList cases = doc.getElementsByTagName("Case");

		for (int i = 0; i < cases.getLength(); i++)
		{
			Node node = cases.item(i);

			if (node.getNodeType() == Node.ELEMENT_NODE)
			{
				String caseID = ((Element)node).getAttribute("case");
				StringWriter writer = new StringWriter();
				Transformer transformer = TransformerFactory.newInstance().newTransformer();
				transformer.transform(new DOMSource(node), new StreamResult(writer));
				String xml = writer.toString();
				this.caseMap.put(caseID, xml);
			}
		}

	}

	@Override public String getCaseList()
	{
		StringBuilder sb = new StringBuilder();

		try
		{
			BufferedReader in = new BufferedReader(new FileReader(this.input));
			String line;

			while ((line = in.readLine()) != null)
			{
				sb.append(line);
			}

			// create a map of <caseID, caseNode> pairs for future access (in getCase)
			this.extractCases(sb.toString());
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}

		return sb.toString();
	}

	@Override public String getCase(String caseNo)
	{
		return caseMap.get(caseNo);
	}

	@Override public String getAll()
	{
		// no need to implement (we are not using this method)
		return null;
	}

	@Override public void results(String data)
	{
		// no need to implement (we are not using this method)
	}
}
