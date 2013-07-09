/*
 * Copyright (c) 2012 Memorial Sloan-Kettering Cancer Center.
 * This library is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published
 * by the Free Software Foundation; either version 2.1 of the License, or
 * any later version.
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
 * has been advised of the possibility of such damage.  See
 * the GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this library; if not, write to the Free Software Foundation,
 * Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA.
 */
package org.mskcc.cbio.portal.servlet;

import au.com.bytecode.opencsv.CSVReader;
import com.google.common.collect.ImmutableMap;
import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.commons.lang.ArrayUtils;
import org.owasp.validator.html.PolicyException;
import sun.misc.IOUtils;

import javax.servlet.http.HttpServlet;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.util.*;

public class EchoFile extends HttpServlet {

    private ServletXssUtil servletXssUtil;

    /**
     * Initializes the servlet.
     *
     * @throws ServletException
     */
    public void init() throws ServletException {

        super.init();
        try {
            servletXssUtil = ServletXssUtil.getInstance();
        }
        catch (PolicyException e) {
            throw new ServletException (e);
        }
    }

    /**
     *
     * Takes a csv reader of the staging file format:
     * 1st line is Hugo_Symbol	Entrez_Gene_Id  sample_id_1	sample_id_2	...
     * data follows matching this header
     *
     * @param reader CSVReader
     * @return
     */
    public static List<ImmutableMap<String, String>> processStagingCsv(CSVReader reader) throws IOException {

        String[] header = reader.readNext();

        String hugo = header[0];
        String entrez = header[1];

        // validation
        if  ( !(hugo && header[1].equals("Entrez_Gene_Id")) ) {
            throw new IOException("validation error, missing column header(s) Hugo_Symbol or Entrez_Gene_Id");
        }

        String[] sampleIds = (String[]) ArrayUtils.subarray(header, 2, header.length);

        ArrayList<ImmutableMap<String, String>> data = new ArrayList<ImmutableMap<String, String>>();

        data.add(
                ImmutableMap.of("sample_id", "TCGA-BL-A0CB", "hugo", "ACAP3", "value", "-1")
        );

        return data;
    };

    public Map<String, Map<String, String>> processCnaString(String cnaData) {

        Map<String, Map<String, String>> sample2gene2cna = new HashMap<String, Map<String, String>>();

        List<String> lines =  Arrays.asList(cnaData.split("\n"));
        List<String> samples = Arrays.asList(lines.get(0).split("\t"));
        samples = samples.subList(2, samples.size());

        for (String line : lines.subList(1,lines.size())) {     // the first line is the samples line
            List<String> values = Arrays.asList(line.split("\t"));
            String hugo = values.get(0);
            String entrez = values.get(1);

            //gene2cnaData.put(hugo, values.subList(2, values.size()));
        }

        return sample2gene2cna;
    };

    protected void doPost(HttpServletRequest request,
                          HttpServletResponse response) throws ServletException, IOException {

        try {
            List<FileItem> items = new ServletFileUpload(new DiskFileItemFactory()).parseRequest(request);

            for (FileItem item : items) {
                if (item.getFieldName().equals("cna")) {
                    // handle cna data
                }
                else if (item.getFieldName().equals("mutation")) {
                    // handle mutation
                }
                else if (item.getFieldName().equals("mrna")) {
                    // handle mrna
                }
                else if (item.getFieldName().equals("rppa")) {

                }
                else {
                    // echo back the string
                }
            }

            InputStream content = items.get(0).getInputStream();        // this might be bad

            new CSVReader( new InputStreamReader(content) );

            java.util.Scanner s = new java.util.Scanner(content, "UTF-8").useDelimiter("\\A");
            Writer writer = response.getWriter();

            writer.write(s.hasNext() ? s.next() : "");
        } catch (FileUploadException e) {
            throw new ServletException(e);
        }
    }
}
