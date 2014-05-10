
package org.mskcc.cbio.portal.model;

public class CopyNumberSegmentFile
{
    public static enum ReferenceGenomeId
    {
        hg18("hg18"),
        hg19("hg19");

        private String propertyName;
        
        ReferenceGenomeId(String propertyName) { this.propertyName = propertyName; }
        public String toString() { return propertyName; }
        static public boolean has(String value)
        {
            if (value == null) return false;
            try {
                return valueOf(value) != null;
            }
            catch (IllegalArgumentException x) {
                return false;
            }
        }
    }

    public int segFileId;
    public int cancerStudyId;
    public ReferenceGenomeId referenceGenomeId;
    public String description;
    public String filename;
}
