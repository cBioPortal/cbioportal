package org.mskcc.cbio.cgds.scripts.drug;

import org.mskcc.cbio.cgds.dao.DaoDrug;
import org.mskcc.cbio.cgds.dao.DaoException;
import org.mskcc.cbio.cgds.dao.DaoInteraction;

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