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

package org.mskcc.cbio.cgds.scripts;

import org.mskcc.cbio.cgds.dao.DaoException;
import org.mskcc.cbio.cgds.dao.DaoGeneOptimized;
import org.mskcc.cbio.cgds.model.CanonicalGene;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.File;
import java.io.IOException;

/**
 * Command Line Tool to Convert Gene IDs.
 */
public class ConvertGeneIds {

    public static void convertGeneIds(File file, String goCategory) throws IOException, DaoException {
        BufferedReader buf = new BufferedReader (new FileReader(file));
        String line = buf.readLine();
        DaoGeneOptimized daoGene = DaoGeneOptimized.getInstance();
        System.out.println ("GO_CATEGORY");
        while (line != null) {
            line = line.trim();
            CanonicalGene canonicalGene = daoGene.getNonAmbiguousGene(line);
            if (canonicalGene != null) {
                System.out.println (canonicalGene.getHugoGeneSymbolAllCaps() + " = " + goCategory);
            }
            line = buf.readLine();
        }
    }

    public static void main(String[] args) throws Exception {
        if (args.length< 2) {
            System.out.println ("command line usage:  convertGeneIds.pl file_name attribute_name");
            System.exit(1);
        }
        String file = args[0];
        String attribute = args[1];
        convertGeneIds(new File (file), attribute);
    }
}
