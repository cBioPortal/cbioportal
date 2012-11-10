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

// package
package org.mskcc.cbio.importer.model;

// imports
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Entity;
import javax.persistence.Column;
import javax.persistence.Transient;
import javax.persistence.NamedQuery;
import javax.persistence.NamedQueries; 
import javax.persistence.GeneratedValue;
import org.hibernate.annotations.NaturalId;

/**
 * Class which represents data to be imported by the importer - 
 * like a gzipped tar downloaded from the broad firehose.
 */
@Entity
@Table(name="importdata")
@NamedQueries({
                @NamedQuery(name="org.mskcc.cbio.import.model.importDataAll",
                            query="from ImportData as importdata order by tumortype"),
                @NamedQuery(name="org.mskcc.cbio.import.model.importDataByTumorAndDatatype",
							query="from ImportData as importdata where tumorType = :tumortype and datatype = :datatype order by tumortype"),
                @NamedQuery(name="org.mskcc.cbio.import.model.importDataByTumorAndDatatypeAndDataSource",
							query="from ImportData as importdata where tumorType = :tumortype and datatype = :datatype and datasource = :datasource order by tumortype"),
                @NamedQuery(name="org.mskcc.cbio.import.model.importDataByTumorAndDatatypeAndDataFilename",
							query="from ImportData as importdata where tumorType = :tumortype and datatype = :datatype and datafilename = :datafilename order by tumortype"),
                @NamedQuery(name="org.mskcc.cbio.import.model.deleteByDataSource",
                            query="delete from ImportData where dataSource = :datasource")

})
public final class ImportData {

	// bean properties
	@Id
	@GeneratedValue
	private long id;
	@Column(nullable=false)
	private String dataSource;
	@NaturalId
	@Column(nullable=false)
	private String tumorType;
	@Column(nullable=false)
	private String datatype;
	@Column(nullable=false)
	private String runDate;
	@Column(nullable=false)
	private String canonicalPath;
	@Column(length=32)
	private String digest;
	@NaturalId
    @Column(nullable=false)
    private String dataFilename;
    @Column(nullable=true)
    private String overrideFilename;

	/**
	 * Default Constructor.
	 */
	public ImportData() {}

    /**
     * Create a ImportData instance with specified properties.
     *
	 * @param dataSource String
	 * @param tumorType String
	 * @param datatype String
	 * @param runDate String
	 * @param canonicalPath String
	 * @param digest String
     * @param dataFilename String
     * @param overrideFilename
     */
    public ImportData(final String dataSource, final String tumorType, final String datatype,
					  final String runDate, final String canonicalPath, final String digest,
                      final String dataFilename, final String overrideFilename) {
        
		setDataSource(dataSource);
		setTumorType(tumorType);
		setDatatype(datatype);
		setRunDate(runDate);
		setCanonicalPathToData(canonicalPath);
		setDigest(digest);
        setDataFilename(dataFilename);
        setOverrideFilename(overrideFilename);
	}

	/**
	 * Sets the data source.
	 *
	 * @param dataSource String
	 */
	public void setDataSource(final String dataSource) {

		if (dataSource == null) {
            throw new IllegalArgumentException("data source must not be null");
		}
		this.dataSource = dataSource;
	}

	/**
	 * Gets the data source.
	 *
	 * @return String
	 */
	public String getDataSource() { return dataSource; }

	/**
	 * Sets the tumor type.
	 *
	 * @param tumorType String
	 */
	public void setTumorType(final String tumorType) {

		if (tumorType == null) {
            throw new IllegalArgumentException("tumor type must not be null");
		}
		this.tumorType = tumorType;		
	}

	/**
	 * Gets the tumor type.
	 *
	 * @return String
	 */
	public String getTumorType() { return tumorType; }

	/**
	 * Sets the datatype.
	 *
	 * @param datatype String
	 */
	public void setDatatype(final String datatype) {

		if (datatype == null) {
            throw new IllegalArgumentException("datatype must not be null");
		}
		this.datatype = datatype;
	}

	/**
	 * Gets the datatype.
	 *
	 * @return String
	 */
	public String getDatatype() { return datatype; }

	/**
	 * Sets the run date.
	 *
	 * @param runDate String
	 */
	public void setRunDate(final String runDate) {

		if (runDate == null) {
            throw new IllegalArgumentException("run date must not be null");
		}
		this.runDate = runDate;
	}

	/**
	 * Gets the run date.
	 *
	 * @return String
	 */
	public String getRunDate() { return runDate; }

	/**
	 * Sets the canonical path to data.
	 *
	 * @param canonicalPath String
	 */
	public void setCanonicalPathToData(String canonicalPath) {
       
		if (canonicalPath == null) {
            throw new IllegalArgumentException("canonical path to data must not be null");
        }
        this.canonicalPath = canonicalPath;
	}

	/**
	 * Gets the cononical path to Data.
	 *
	 * @return String
	 */
    public String getCanonicalPathToData() { return canonicalPath; }

	/**
	 * Sets the digest.
	 *
	 * @param digest String
	 */
	public void setDigest(final String digest) {

		if (digest == null) {
            throw new IllegalArgumentException("digest must not be null");
		}
		this.digest = digest;
	}

	/**
	 * Gets the digest.
	 *
	 * @return String
	 */
	public String getDigest() { return digest; }

	/**
	 * Sets the dataFilename.
	 *
	 * @param dataFileName String
	 */
	public void setDataFilename(final String dataFilename) {

		if (dataFilename == null) {
            throw new IllegalArgumentException("dataFilename must not be null");
		}
		this.dataFilename = dataFilename;
	}

	/**
	 * Gets the dataFilename.
	 *
	 * @return String
	 */
	public String getDataFilename() { return dataFilename; }

	/**
	 * Sets the override filename.
	 *
	 * @param overrideFilename String
	 */
	public void setOverrideFilename(final String overrideFilename) {

		this.overrideFilename = (overrideFilename == null) ? "" : overrideFilename;
	}

	/**
	 * Gets the override filename.
	 *
	 * @return String
	 */
	public String getOverrideFilename() { return overrideFilename; }
}
