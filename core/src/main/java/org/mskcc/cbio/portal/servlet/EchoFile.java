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
import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.codehaus.jackson.map.ObjectMapper;
import org.owasp.validator.html.PolicyException;

import javax.servlet.http.HttpServlet;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.util.*;

public class EchoFile extends HttpServlet {

    private ServletXssUtil servletXssUtil;
    public static final int MAX_NO_GENES = 30;

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
     * 1st line is hugo_symbol	entrez_gene_id  sample_id_1	sample_id_2	...
     * data follows matching this header and is assumed to be numerical
     *
     * NB : only takes the first 50 lines (genes)
     *
     * @param reader CSVReader
     * @param datatype String
     * @return List of maps with keys {sample_id, hugo, value, datatype}
     */
    public static List<Map<String, String>> processStagingCsv(CSVReader reader, String datatype) throws IOException {

        String[] header = reader.readNext();

        String hugo = header[0];
        String entrez = header[1];

        // validation
        if  ( !(hugo.toLowerCase().equals("hugo_symbol") && entrez.toLowerCase().equals("entrez_gene_id")) ) {
            throw new IOException("validation error, missing column header(s) Hugo_Symbol or Entrez_Gene_Id");
        }

        // grab the sampleIds
//        String[] sampleIds = (String[]) ArrayUtils.subarray(header, 2, header.length);

        List<Map<String, String>> data = new ArrayList<Map<String, String>>();

        String[] row = reader.readNext();
        int row_count = 0;
        while (row !=null && row_count < MAX_NO_GENES) {
            String thisHugo = row[0];
            String thisEntrez = row[1];

            // skip past the hugo and the entrez
            for (int i = 2; i < row.length; i+=1) {
                Map<String, String> datum = new HashMap<String, String>();
                datum.put("sample_id", header[i]);
                datum.put("hugo", thisHugo);
                datum.put("value", String.valueOf(row[i]));
                datum.put("datatype", datatype);
                data.add(datum);
            }

            row = reader.readNext();
            row_count += 1;
        }

        return data;
    }

    /**
     * Assumes that there are columns sample_id, protein_change, hugo_symbol (caps insensitive)
     *
     * NB : only takes the first 50 lines (genes)
     *
     * @param reader
     * @return List of maps with keys {sample_id, hugo, value, datatype}
     */
    public static List<Map<String, String>> processMutationStagingCsv(CSVReader reader) throws IOException {

        int proteinChangeColumnIndex = -1;
        int sampleIdColumnIndex = -1;
        int hugoColumnIndex = -1;

        String[] header = reader.readNext();

        for (int i = 0; i < header.length; i+=1) {
            String curr = header[i].toLowerCase();

            if (curr.equals("protein_change")) {
                proteinChangeColumnIndex = i;
            }

            if (curr.equals("sample_id")) {
                sampleIdColumnIndex = i;
            }

            if (curr.equals("hugo_symbol")) {
                hugoColumnIndex = i;
            }
        }

        // make sure everything is initialized
        if (proteinChangeColumnIndex == -1) {
            throw new IOException("could not find column 'protein_change'");
        }
        if (sampleIdColumnIndex == -1) {
            throw new IOException("could not find column 'sample_id'");
        }
        if (sampleIdColumnIndex == -1) {
            throw new IOException("could not find column 'hugo_symbol'");
        }

        String[] row = reader.readNext();
        List<Map<String, String>> data = new ArrayList<Map<String, String>>();
        int row_count = 0;
        while (row != null && row_count < MAX_NO_GENES) {

            Map<String, String> datum = new HashMap<String, String>();
            datum.put("sample_id", row[sampleIdColumnIndex]);
            datum.put("hugo", row[hugoColumnIndex]);
            datum.put("value", row[proteinChangeColumnIndex]);
            datum.put("datatype", "mutation");
            data.add(datum);

            row = reader.readNext();
            row_count += 1;
        }

        return data;
    }

//    /**
//     * Takes a list of cna, mutation, mrna, rppa data and reduces
//     * into one list of maps indexed by gene and sample_id
//     *
//     * @param list
//     * @return List of maps with keys {sample_id, hugo, [cna], [mutation], [mrna], [rppa]}
//     */
//    public static List<ImmutableMap<String, String>> reduceGenomicData(List<ImmutableMap<String, String>> list) {
//
//        return new ArrayList<ImmutableMap<String, String>>();
//    }


    protected void doPost(HttpServletRequest request,
                          HttpServletResponse response) throws ServletException, IOException {

        Writer writer = response.getWriter();

        try {
            List<Map<String, String>> data = new ArrayList<Map<String, String>>();
            List<FileItem> items = new ServletFileUpload(new DiskFileItemFactory()).parseRequest(request);

            for (FileItem item : items) {

                if (item.getSize() == 0) {
                    // skip empty files
                    continue;
                }

                CSVReader reader = new CSVReader(new InputStreamReader(item.getInputStream()), '\t');
                String fieldName = item.getFieldName();

                if (fieldName.equals("cna")) {
                    // handle cna data
                    data.addAll(processStagingCsv(reader, "cna"));
                }

                else if (fieldName.equals("mutation")) {
                    // handle mutation
                    data.addAll(processMutationStagingCsv(reader));
                }

                else if (fieldName.equals("mrna")) {
                    // handle mrna
                    data.addAll(processStagingCsv(reader, "mrna"));
                }

                else if (fieldName.equals("rppa")) {
                    // ??? Composite.Element.REF ???
                    int x = -1;
                }

                else {
                    // echo back the raw string
                    InputStream content = item.getInputStream();
                    java.util.Scanner s = new java.util.Scanner(content, "UTF-8").useDelimiter("\\A");
                    writer.write(s.hasNext() ? s.next() : "");
                }
            }

            // write the objects out as json
            response.setContentType("application/json");
            ObjectMapper mapper = new ObjectMapper();
            mapper.writeValue(writer, data);
        }

        // catch all exceptions
        catch (Exception e) {

            // log it
            System.out.println(e);

            // hide details from user
            throw new ServletException("there was an error processing your request");
        }
    }
}
