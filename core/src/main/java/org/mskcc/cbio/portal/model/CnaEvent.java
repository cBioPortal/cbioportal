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

import org.cbioportal.model.CNA;
import org.mskcc.cbio.portal.dao.DaoGeneOptimized;

/**
 *
 * @author jgao
 */
public class CnaEvent {
    private int sampleId;
    private int cnaProfileId;
    private Event event;
    private String driverFilter;
    private String driverFilterAnnotation;
    private String driverTiersFilter;
    private String driverTiersFilterAnnotation;
    
    public static class Event {
        private long eventId;
        private CanonicalGene gene;
        private CNA alteration;

        public long getEventId() {
            return eventId;
        }

        public void setEventId(long eventId) {
            this.eventId = eventId;
        }

        public CanonicalGene getGene() {
            return gene;
        }

        public void setGene(CanonicalGene gene) {
            this.gene = gene;
        }

        public void setEntrezGeneId(long entrezGeneId) {
            setGene(DaoGeneOptimized.getInstance().getGene(entrezGeneId));
            if (gene == null) {
                throw new IllegalArgumentException("Could not find entrez gene id: "+entrezGeneId);
            } 
        }

        public CNA getAlteration() {
            return alteration;
        }

        public void setAlteration(CNA alteration) {
            this.alteration = alteration;
        }
        
        public void setAlteration(short alteration) {
            this.alteration = CNA.getByCode(alteration);
            if (this.alteration == null) {
                throw new IllegalArgumentException("wrong copy number alteration");
            }
        }

        @Override
        public int hashCode() {
            int hash = 3;
            hash = 97 * hash + (this.gene != null ? this.gene.hashCode() : 0);
            hash = 97 * hash + (this.alteration != null ? this.alteration.hashCode() : 0);
            return hash;
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == null) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            final Event other = (Event) obj;
            if (this.gene != other.gene && (this.gene == null || !this.gene.equals(other.gene))) {
                return false;
            }
            if (this.alteration != other.alteration) {
                return false;
            }
            return true;
        }
        
    }

    public CnaEvent(int sampleId, int cnaProfileId, long entrezGeneId, short alteration) {
        event = new Event();
        setEntrezGeneId(entrezGeneId);
        this.sampleId = sampleId;
        this.cnaProfileId = cnaProfileId;
        event.setAlteration(alteration);
    }

    public CNA getAlteration() {
        return event.alteration;
    }

    public void setAlteration(CNA alteration) {
        event.setAlteration(alteration);
    }

    public void setAlteration(short alteration) {
        event.setAlteration(CNA.getByCode(alteration));
    }

    public int getSampleId() {
        return sampleId;
    }

    public void setSampleId(int sampleId) {
        this.sampleId = sampleId;
    }

    public int getCnaProfileId() {
        return cnaProfileId;
    }

    public void setCnaProfileId(int cnaProfileId) {
        this.cnaProfileId = cnaProfileId;
    }

    public long getEntrezGeneId() {
        return event.getGene().getEntrezGeneId();
    }
    
    public String getGeneSymbol() {
        return event.getGene().getHugoGeneSymbolAllCaps();
    }

    public void setEntrezGeneId(long entrezGeneId) {
        event.setEntrezGeneId(entrezGeneId);
        if (event.gene == null) {
            throw new IllegalArgumentException("Could not find entrez gene id: "+entrezGeneId);
        } 
    }

    public long getEventId() {
        return event.getEventId();
    }

    public void setEventId(long eventId) {
        event.setEventId(eventId);
    }

    public Event getEvent() {
        return event;
    }

    public void setEvent(Event event) {
        this.event = event;
    }

    public String getDriverFilter() {
        return driverFilter;
    }

    public void setDriverFilter(String driverFilter) {
        this.driverFilter = driverFilter;
    }

    public String getDriverFilterAnnotation() {
        return driverFilterAnnotation;
    }

    public void setDriverFilterAnnotation(String driverFilterAnnotation) {
        this.driverFilterAnnotation = driverFilterAnnotation;
    }

    public String getDriverTiersFilter() {
        return driverTiersFilter;
    }

    public void setDriverTiersFilter(String driverTiersFilter) {
        this.driverTiersFilter = driverTiersFilter;
    }

    public String getDriverTiersFilterAnnotation() {
        return driverTiersFilterAnnotation;
    }

    public void setDriverTiersFilterAnnotation(String driverTiersFilterAnnotation) {
        this.driverTiersFilterAnnotation = driverTiersFilterAnnotation;
    }

    @Override
    public int hashCode() {
        int hash = 5;
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final CnaEvent other = (CnaEvent) obj;
        if (!(this.sampleId == other.sampleId)) {
            return false;
        }
        if (this.cnaProfileId != other.cnaProfileId) {
            return false;
        }
        if (this.event != other.event && (this.event == null || !this.event.equals(other.event))) {
            return false;
        }
        return true;
    }
    
    
}
