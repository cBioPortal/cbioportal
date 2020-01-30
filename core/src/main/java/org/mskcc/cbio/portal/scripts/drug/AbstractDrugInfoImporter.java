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

package org.mskcc.cbio.portal.scripts.drug;

import org.mskcc.cbio.portal.dao.DaoDrug;
import org.mskcc.cbio.portal.dao.DaoException;
import org.mskcc.cbio.portal.dao.DaoInteraction;

public abstract class AbstractDrugInfoImporter {
    public static String DRUG_INTERACTION_TYPE = "DRUG_TARGET";

    private DrugDataResource dataResource;
    private DaoDrug drugDao;
    private DaoInteraction daoInteraction;

    public AbstractDrugInfoImporter(DrugDataResource dataResource)
        throws DaoException {
        this(dataResource, DaoDrug.getInstance(), DaoInteraction.getInstance());
    }

    public AbstractDrugInfoImporter(
        DrugDataResource dataResource,
        DaoDrug drugDao,
        DaoInteraction daoInteraction
    ) {
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

    public abstract void importData() throws Exception;
}
