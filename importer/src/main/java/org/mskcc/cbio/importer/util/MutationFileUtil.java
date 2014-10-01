/** Copyright (c) 2013 Memorial Sloan-Kettering Cancer Center.
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
package org.mskcc.cbio.importer.util;

// imports
import org.mskcc.cbio.importer.Admin;
import org.mskcc.cbio.importer.FileUtils;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.commons.io.LineIterator;

import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import java.io.File;
import java.util.List;
import java.util.Arrays;

public class MutationFileUtil
{

	private static final String KNOWN_ONCOTATOR_HEADER = "ONCOTATOR_VARIANT_CLASSIFICATION";
	private static final ApplicationContext context = new ClassPathXmlApplicationContext(Admin.contextFile);

	public static boolean isOncotated(String fileName) throws Exception
	{
		return MutationFileUtil.isOncotated(Arrays.asList(getColumnHeaders(fileName)));
	}

	public static boolean isOncotated(List<String> columnHeaders)
	{
		return columnHeaders.contains(KNOWN_ONCOTATOR_HEADER);
	}
        
        public static String[] getColumnHeaders(String fileName) throws Exception {
            File file = new File(fileName);
            FileUtils fileUtils = (FileUtils)MutationFileUtil.context.getBean("fileUtils");
            LineIterator it = fileUtils.getFileContents(FileUtils.FILE_URL_PREFIX + file.getCanonicalPath());
            String line = it.next();
            while (line.startsWith("#")) {
                line = it.next();
            }
            String[] columnHeaders = line.split("\t");
            it.close();
            return columnHeaders;
        }
}
