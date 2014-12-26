package org.mskcc.cbio.annotator;

import org.mskcc.cbio.oncotator.MultiFileAnnotator;

import java.io.File;
import java.util.Map;

/**
 * Designed to oncotate all MAF files (with Maf2Maf tool)
 * within a given directory.
 *
 * @author Selcuk Onur Sumer
 */
public class MultiFileMaf2Maf extends MultiFileAnnotator
{
	protected AnnotatorConfig config;

	public MultiFileMaf2Maf(AnnotatorConfig config)
	{
		this.config = config;
	}

	public MultiFileMaf2Maf()
	{
		this(new AnnotatorConfig());
	}

	/**
	 * Oncotates all input MAF files within the given map. Writes output
	 * MAFs to the mapped directory.
	 *
	 * @param map   map of input MAF files to output directories
	 */
	protected void annotateAll(Map<File, File> map)
	{
		for (File file : map.keySet())
		{
			File outDir = map.get(file);
			String inputMaf = file.getAbsolutePath();
			String outputMaf = outDir.getAbsolutePath() + "/" + file.getName();

			this.config.setInput(inputMaf);
			this.config.setOutput(outputMaf);

			AnnotateTool.driver(this.config);
		}
	}
}
