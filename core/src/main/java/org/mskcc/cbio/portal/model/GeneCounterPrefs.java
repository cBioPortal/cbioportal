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
