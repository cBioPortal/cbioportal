// package
package org.mskcc.cbio.importer.model;

// imports
import javax.persistence.Id;
import javax.persistence.Lob;
import javax.persistence.Table;
import javax.persistence.Entity;
import javax.persistence.Column;
import javax.persistence.NamedQuery;
import javax.persistence.NamedQueries;

/**
 * Class which represents data to be imported by the importer - 
 * like a gzipped tar downloaded from the broad firehose.
 */
@Entity
@Table(name="importdata")
@NamedQueries({
                @NamedQuery(name="org.mskcc.cbio.import.model.importDataByCancerAndDatatype",
							query="from ImportData as importdata where cancertype = :cancertype and datatype = :datatype order by cancertype")
})
public final class ImportData {

	// bean properties
	@Column(nullable=false)
	private String cancerType;
	@Column(nullable=false)
	private String datatype;
	@Column(nullable=false)
	private String runDate;
	@Column(nullable=false)
	private String urlToData;
	@Id
	@Column(length=32)
	private String digest;


	/**
	 * Default Constructor.
	 */
	public ImportData() {}

    /**
     * Create a ImportData instance with specified properties.
     *
	 * @param cancerType String
	 * @param runDate String
	 * @param filename String
	 * @param datatype String
	 * @param digest String
	 * @param content ByteBuffer
     */
    public ImportData(final String cancerType, final String datatype,
					  final String runDate, final String urlToData, final String digest) {

		setCancerType(cancerType);
		setDatatype(datatype);
		setRunDate(runDate);
		setURLToData(urlToData);
		setDigest(digest);
	}

	/**
	 * Sets the cancer type.
	 *
	 * @param cancerType String
	 */
	public void setCancerType(final String cancerType) {

		if (cancerType == null) {
            throw new IllegalArgumentException("cancer type must not be null");
		}
		this.cancerType = cancerType;		
	}

	/**
	 * Gets the cancer type.
	 *
	 * @return String
	 */
	public String getCancerType() { return cancerType; }

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
	 * Sets the URL to data.
	 *
	 * @param urlToData String
	 */
	public void setURLToData(String urlToData) {
       
		if (urlToData == null) {
            throw new IllegalArgumentException("URL to data must not be null");
        }
        this.urlToData = urlToData;
	}

	/**
	 * Gets the URL to Data.
	 *
	 * @return String
	 */
    public String getURLToData() { return urlToData; }

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
}
