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

package org.mskcc.cbio.portal.scripts.drug;

import org.mskcc.cbio.portal.dao.DaoDrug;
import org.mskcc.cbio.portal.dao.DaoException;
import org.mskcc.cbio.portal.dao.DaoInteraction;

abstract public class AbstractDrugInfoImporter {
    public static String DRUG_INTERACTION_TYPE = "DRUG_TARGET";

    private DrugDataResource dataResource;
    private DaoDrug drugDao;
    private DaoInteraction daoInteraction;

    public AbstractDrugInfoImporter(DrugDataResource dataResource) throws DaoException {
        this(dataResource, DaoDrug.getInstance(), DaoInteraction.getInstance());
    }

    public AbstractDrugInfoImporter(DrugDataResource dataResource, DaoDrug drugDao, DaoInteraction daoInteraction) {
        this.dataResource = dataResource;
        this.drugDao = drugDao;
        this.daoInteraction = daoInteraction;
    }

    public DrugDataResource getDataResource() {
        return dataResource;
    }

    public void setDataResource(DrugDataResource dataResource) {
        this.dataResource = dataResource;
    }

    public DaoDrug getDrugDao() {
        return drugDao;
    }

    public void setDrugDao(DaoDrug drugDao) {
        this.drugDao = drugDao;
    }

    public DaoInteraction getDaoInteraction() {
        return daoInteraction;
    }

    public void setDaoInteraction(DaoInteraction daoInteraction) {
        this.daoInteraction = daoInteraction;
    }

    abstract public void importData() throws Exception;
}