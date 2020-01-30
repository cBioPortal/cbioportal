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
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import org.mskcc.cbio.portal.dao.DaoException;
import org.mskcc.cbio.portal.dao.DaoGeneOptimized;
import org.mskcc.cbio.portal.model.CanonicalGene;

/**
 * Command Line Tool to Convert Gene IDs.
 */
public class ConvertGeneIds {

    public static void convertGeneIds(File file, String goCategory)
        throws IOException, DaoException {
        BufferedReader buf = new BufferedReader(new FileReader(file));
        String line = buf.readLine();
        DaoGeneOptimized daoGene = DaoGeneOptimized.getInstance();
        System.out.println("GO_CATEGORY");
        while (line != null) {
            line = line.trim();
            CanonicalGene canonicalGene = daoGene.getNonAmbiguousGene(line);
            if (canonicalGene != null) {
                System.out.println(
                    canonicalGene.getHugoGeneSymbolAllCaps() +
                    " = " +
                    goCategory
                );
            }
            line = buf.readLine();
        }
    }

    public static void main(String[] args) throws Exception {
        if (args.length < 2) {
            System.out.println(
                "command line usage:  convertGeneIds.pl file_name attribute_name"
            );
            return;
        }
        String file = args[0];
        String attribute = args[1];
        convertGeneIds(new File(file), attribute);
    }
}
