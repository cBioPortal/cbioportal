package org.mskcc.cbio.portal.model;

// TODO: I think this can be deleted
public class GeneCounterPrefs {

    private boolean mrnaUp;
    private boolean mrnaDown;
    private boolean ignoreMrna;
    private boolean cnaUp;
    private boolean cnaDown;
    private boolean ignoreCna;


    public boolean mrnaUp() {
        return mrnaUp;
    }

    public void setMrnaUp(boolean flag) {
        this.mrnaUp = flag;
    }

    public boolean mrnaDown() {
        return mrnaDown;
    }

    public void setMrnaDown(boolean flag) {
        this.mrnaDown = flag;
    }

    public boolean ignoreMrna() {
        return ignoreMrna;
    }

    public void setIgnoreMrna(boolean flag) {
        this.ignoreMrna = flag;
    }

    public boolean cnaUp() {
        return cnaUp;
    }

    public void setCnaUp(boolean flag) {
        this.cnaUp = flag;
    }

    public boolean cnaDown() {
        return cnaDown;
    }

    public void setCnaDown(boolean flag) {
        this.cnaDown = flag;
    }

    public boolean ignoreCna() {
        return ignoreCna;
    }

    public void setIgnoreCna(boolean flag) {
        this.ignoreCna = flag;
    }
}
