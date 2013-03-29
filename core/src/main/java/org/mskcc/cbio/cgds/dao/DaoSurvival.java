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

package org.mskcc.cbio.cgds.dao;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Set;
import org.mskcc.cbio.cgds.model.Survival;

/**
 * A layer ontop of DaoClinical for survival data
 *
 * @author Gideon Dresdner dresdnerg@cbio.mskcc.org
 */
public class DaoSurvival {

    public Survival getCase(int cancerStudyId, String _case)  throws DaoException {
        ArrayList<Survival> list = getCases(cancerStudyId, Collections.singleton(_case));

        Survival dummy = new Survival(-1, "", -1.0, "", -1.0, "", -1.0);

        return dummy;
    }

    /**
     * Gets All Cases in the Specified Case Set.
     *
     * @param caseSet       Target Case Set.
     * @return  ArrayList of CaseSurvival Objects.
     * @throws DaoException Error Accessing Database.
     */
    public ArrayList<Survival> getCases(int cancerStudyId, Set<String> caseSet) throws DaoException {

        ArrayList<Survival> dummy = new ArrayList<Survival>();

        return dummy;
    }
}
