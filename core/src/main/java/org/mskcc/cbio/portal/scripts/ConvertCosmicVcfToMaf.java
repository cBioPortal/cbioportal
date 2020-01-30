/*
 * Copyright (c) 2015 Memorial Sloan-Kettering Cancer Center.
 *
 * This library is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY, WITHOUT EVEN THE IMPLIED WARRANTY OF MERCHANTABILITY OR FITNESS
 * FOR A PARTICULAR PURPOSE. The software and documentation provided hereunder
 * is on an "as is" basis, and Memorial Sloan-Kettering Cancer Center has no
 * obligations to provide maintenance, support, updates, enhancements or
 * modifications. In no event shall Memorial Sloan-Kettering Cancer Center be
 * liable to any party for direct, indirect, special, incidental or
 * consequential damages, including lost profits, arising out of the use of this
 * software and its documentation, even if Memorial Sloan-Kettering Cancer
 * Center has been advised of the possibility of such damage.
 */

/*
 * This file is part of cBioPortal.
 *
 * cBioPortal is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.mskcc.cbio.portal.scripts;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.mskcc.cbio.portal.util.ConsoleUtil;
import org.mskcc.cbio.portal.util.FileUtil;
import org.mskcc.cbio.portal.util.ProgressMonitor;

public class ConvertCosmicVcfToMaf {
    private File vcf, maf;

    public ConvertCosmicVcfToMaf(File vcf, File maf) {
        this.vcf = vcf;
        this.maf = maf;
    }

    public void convert() throws IOException {
        Pattern p = Pattern.compile(".+;CNT=([0-9]+)");
        FileWriter writer = new FileWriter(maf);
        BufferedWriter bufWriter = new BufferedWriter(writer);
        bufWriter.append(
            "COSMIC_ID\tCOSMIC_COUNT\tChromosome\tStart_Position\tEnd_Position\t" +
            "Reference_Allele\tTumor_Seq_Allele1\tTumor_Seq_Allele2\tNCBI_Build\tCOSMIC_Info\n"
        );

        FileReader reader = new FileReader(vcf);
        BufferedReader buf = new BufferedReader(reader);
        String line;
        while ((line = buf.readLine()) != null) {
            ProgressMonitor.incrementCurValue();
            ConsoleUtil.showProgress();
            if (!line.startsWith("#")) {
                String parts[] = line.split("\t", -1);
                if (parts.length < 8) {
                    System.err.println("Wrong line in cosmic: " + line);
                    continue;
                }

                String info = parts[7];
                Matcher m = p.matcher(info);
                if (m.find()) {
                    String id = parts[2];
                    String count = m.group(1);
                    String chr = parts[0];
                    long start = Long.parseLong(parts[1]);
                    String ref = parts[3];
                    String alt = parts[4].equals(".") ? "-" : parts[4];
                    long end = start + ref.length() - 1;

                    bufWriter
                        .append(id)
                        .append("\t")
                        .append(count)
                        .append("\t")
                        .append(chr)
                        .append("\t")
                        .append(Long.toString(start))
                        .append("\t")
                        .append(Long.toString(end))
                        .append("\t")
                        .append(ref)
                        .append("\t")
                        .append(alt)
                        .append("\t")
                        .append(alt)
                        .append("\t")
                        .append("37\t")
                        .append(info)
                        .append("\n");
                }
            }
        }
        buf.close();
        bufWriter.close();
    }

    public static void main(String[] args) throws Exception {
        if (args.length < 2) {
            System.out.println(
                "command line usage:  importCosmicData.pl <CosmicCodingMuts.vcf> <CosmicCodingMuts.maf>"
            );
            // an extra --noprogress option can be given to avoid the messages regarding memory usage and % complete
            return;
        }
        ProgressMonitor.setConsoleModeAndParseShowProgress(args);

        File vcf = new File(args[0]);
        System.out.println("Reading data from:  " + vcf.getAbsolutePath());
        int numLines = FileUtil.getNumLines(vcf);
        System.out.println(" --> total number of lines:  " + numLines);
        ProgressMonitor.setMaxValue(numLines);
        ConvertCosmicVcfToMaf parser = new ConvertCosmicVcfToMaf(
            vcf,
            new File(args[1])
        );
        parser.convert();
        ConsoleUtil.showWarnings();
        System.err.println("Done.");
    }
}
