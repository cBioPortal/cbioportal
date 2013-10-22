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
package org.mskcc.cbio.portal.scripts;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import org.mskcc.cbio.portal.dao.DaoCoexpression;
import org.mskcc.cbio.portal.dao.DaoException;
import org.mskcc.cbio.portal.dao.DaoGeneticAlteration;
import org.mskcc.cbio.portal.dao.DaoGeneticProfile;
import org.mskcc.cbio.portal.dao.MySQLbulkLoader;
import org.mskcc.cbio.portal.model.Coexpression;
import org.mskcc.cbio.portal.model.GeneticAlterationType;
import org.mskcc.cbio.portal.model.GeneticProfile;
import org.mskcc.cbio.portal.util.ProgressMonitor;

/**
 *
 * @author jgao
 */
public class CalculateCoexpression {
    public static void main(String[] args) throws Exception {

        ProgressMonitor pMonitor = new ProgressMonitor();
        pMonitor.setConsoleMode(true);

        if (args.length == 0) {
            System.out.println ("You must specify a profile id.");
            return;
        }

        String profileId = args[0];
        GeneticProfile profile = DaoGeneticProfile.getGeneticProfileByStableId(profileId);
        if (profile==null) {
            System.out.println (profileId+" is not a valid profile id.");
            return;
        }


        // TODO: support miRNA later
        if (profile.getGeneticAlterationType()!=GeneticAlterationType.MRNA_EXPRESSION) {
            System.out.println (profileId+" is not a mrna profile id.");
            return;
        }
        
        calculate(profile, pMonitor);

    }
     
    private static void calculate(GeneticProfile profile, ProgressMonitor pMonitor) throws DaoException {
        MySQLbulkLoader.bulkLoadOn();
        DaoGeneticAlteration daoGeneticAlteration = DaoGeneticAlteration.getInstance();

        HashMap<Long,HashMap<String, String>> map = daoGeneticAlteration.getGeneticAlterationMap(profile.getGeneticProfileId(), null);
        int n = map.size();
        pMonitor.setMaxValue(map.size());
        
        List<Long> genes = new ArrayList<Long>(map.keySet());
        for (int i=0; i<n; i++) {
            for (int j=i+1; j<n; j++) {
                long gene1 = genes.get(i);
                long gene2 = genes.get(j);
                
                double pearson = 0;// calculate person
                double spearman = 0; // calculate spearman
                
                Coexpression coexpression = new Coexpression(gene1, gene2, profile.getGeneticProfileId(), pearson, spearman);
                DaoCoexpression.addCoexpression(coexpression);
            }
        }
        
        MySQLbulkLoader.flushAll();
    }
}
