package org.mskcc.mondrian.internal.gui.heatmap;

import java.util.List;

import org.mskcc.mondrian.client.CancerStudy;

public class HeatmapPanelConfiguration {
	/**
	 * Constant Element Enumeration
	 */
	public static enum PROPERTY_TYPE {
		DATA_TYPE("Constant:datatype"), GENE("Constant:gene"), SAMPLE(
				"Constant:sample");

		// string ref for readable name
		private String name;

		// constructor
		PROPERTY_TYPE(String name) {
			this.name = name;
		}

		// method to get enum readable name
		public String toString() {
			return name;
		}
	}

	// the ref of the heatmap panel this configuration applies too
	private HeatmapPanel heatmapPanel;
	
	private CancerStudy cancerStudy;
	private PROPERTY_TYPE constantPropertyType;
	private String constantProperty;

	// the new list of "row" properties
	private PROPERTY_TYPE rowPropertyType;
	private List<String> rowProperties;
	private List<String> rowPropertiesWO; // List of row properties with data
											// only
	private String rowPropertiesFocus;

	// the new list of "column" properties
	private PROPERTY_TYPE columnPropertyType;
	private List<String> columnProperties;
	private List<String> columnPropertiesWO; // List of col properties with data
												// only
	private String columnPropertiesFocus;

	// hide no data booleans
	private boolean hideGenesWithoutData;
	private boolean hideSamplesWithoutData;


	/**
	 * Gets the heatmap panel this configuration represents.
	 * 
	 * @return HeatmapPanel
	 */
	public HeatmapPanel getHeatmapPanel() {
		return heatmapPanel;
	}

	/**
	 * Sets the heatmap panel this configuration represents.
	 * 
	 * @param heatmapPanel
	 *            HeatmapPanel
	 */
	public void getHeatmapPanel(HeatmapPanel heatmapPanel) {
		this.heatmapPanel = heatmapPanel;
	}

	/**
	 * Gets the constant property type.
	 * 
	 * @return PROPERTY_TYPE
	 */
	public PROPERTY_TYPE getConstantPropertyType() {
		return constantPropertyType;
	}

	/**
	 * Sets the constant property type.
	 * 
	 * @param constantPropertyType
	 *            PROPERTY_TYPE
	 */
	public void setConstantPropertyType(PROPERTY_TYPE constantPropertyType) {
		this.constantPropertyType = constantPropertyType;
	}

	/**
	 * Gets the constant property.
	 * 
	 * @return String
	 */
	public String getConstantProperty() {
		return constantProperty;
	}

	/**
	 * Sets the constant property.
	 * 
	 * @param constantProperty
	 *            String
	 */
	public void setConstantProperty(String constantProperty) {
		this.constantProperty = constantProperty;
	}

	/**
	 * Gets the row property type.
	 * 
	 * @return PROPERTY_TYPE
	 */
	public PROPERTY_TYPE getRowPropertyType() {
		return rowPropertyType;
	}

	/**
	 * Sets the row property type.
	 * 
	 * @param rowPropertyType
	 *            PROPERTY_TYPE
	 */
	public void setRowPropertyType(PROPERTY_TYPE rowPropertyType) {
		this.rowPropertyType = rowPropertyType;
	}

	/**
	 * Gets the row properties.
	 * 
	 * @return List<String>
	 */
	public List<String> getRowProperties() {
		if (rowPropertyType == PROPERTY_TYPE.GENE) {
			return (hideGenesWithoutData) ? rowPropertiesWO : rowProperties;
		} else if (rowPropertyType == PROPERTY_TYPE.SAMPLE) {
			return (hideSamplesWithoutData) ? rowPropertiesWO : rowProperties;
		} else {
			return rowProperties;
		}
	}

	/**
	 * Sets the row properties.
	 * 
	 * WO: like a genes list without genes that are missing data, else
	 * equivalent to arg 1
	 * 
	 * @param rowProperties
	 *            List<String>
	 * @param rowPropertiesWO
	 *            List<String>
	 */
	public void setRowProperties(List<String> rowProperties,
			List<String> rowPropertiesWO) {
		this.rowProperties = rowProperties;
		this.rowPropertiesWO = rowPropertiesWO;
	}

	/**
	 * Gets the row properties focus.
	 * 
	 * @return String
	 */
	public String getRowPropertiesFocus() {
		return rowPropertiesFocus;
	}

	/**
	 * Sets the row properties focus.
	 * 
	 * @param rowPropertiesFocus
	 *            String
	 */
	public void setRowPropertiesFocus(String rowPropertiesFocus) {
		this.rowPropertiesFocus = rowPropertiesFocus;
	}

	/**
	 * Gets the max row property string length.
	 * 
	 * @return String
	 */
	public String getMaxRowPropertyString() {
		return "Is this long enough?";  //TODO: fix this

//		boolean rowPropertyTypeIsGene = (rowPropertyType == HeatmapPanelConfiguration.PROPERTY_TYPE.GENE);
//		String maxString = rowPropertyType.toString();
//		for (String rowProperty : rowProperties) {
//			rowProperty = (rowPropertyTypeIsGene) ? HeatmapUtil
//					.getGeneDescriptor(rowProperty) : rowProperty;
//			maxString = (rowProperty.length() > maxString.length()) ? rowProperty
//					: maxString;
//		}
//
//		return maxString;
	}

	/**
	 * Gets the column property type.
	 * 
	 * @return PROPERTY_TYPE
	 */
	public PROPERTY_TYPE getColumnPropertyType() {
		return columnPropertyType;
	}

	/**
	 * Sets the column property type.
	 * 
	 * @param columnPropertyType
	 *            PROPERTY_TYPE
	 */
	public void setColumnPropertyType(PROPERTY_TYPE columnPropertyType) {
		this.columnPropertyType = columnPropertyType;
	}
	
	public CancerStudy getCancerStudy() {
		return cancerStudy;
	}

	public void setCancerStudy(CancerStudy cancerStudy) {
		this.cancerStudy = cancerStudy;
	}	

	/**
	 * Gets the column properties.
	 * 
	 * @return List<String>
	 */
	public List<String> getColumnProperties() {
		if (columnPropertyType == PROPERTY_TYPE.GENE) {
			return (hideGenesWithoutData) ? columnPropertiesWO
					: columnProperties;
		} else if (columnPropertyType == PROPERTY_TYPE.SAMPLE) {
			return (hideSamplesWithoutData) ? columnPropertiesWO
					: columnProperties;
		} else {
			return columnProperties;
		}
	}

	/**
	 * Sets the column properties.
	 * 
	 * WO: like a genes list without genes that are missing data, else
	 * equivalent to arg 1
	 * 
	 * @param columnProperties
	 *            List<String>
	 * @param columnPropertiesWO
	 *            List<String>
	 */
	public void setColumnProperties(List<String> columnProperties,
			List<String> columnPropertiesWO) {
		this.columnProperties = columnProperties;
		this.columnPropertiesWO = columnPropertiesWO;
	}

	/**
	 * Gets the column properties focus.
	 * 
	 * @return String
	 */
	public String getColumnPropertiesFocus() {
		return columnPropertiesFocus;
	}

	/**
	 * Sets the column properties focus.
	 * 
	 * @param columnPropertiesFocus
	 *            String
	 */
	public void setColumnPropertiesFocus(String columnPropertiesFocus) {
		this.columnPropertiesFocus = columnPropertiesFocus;
	}

	/**
	 * Returns flag indicating hide genes without data.
	 * 
	 * @return boolean
	 */
	public boolean hideGenesWithoutData() {
		return hideGenesWithoutData;
	}

	/**
	 * Sets hideGenesWithoutData boolean.
	 * 
	 * @param hideGenesWithoutData
	 *            boolean
	 */
	public void setHideGenesWithoutData(boolean hideGenesWithoutData) {

		// set member bool
		this.hideGenesWithoutData = hideGenesWithoutData;

		// if we are going into "hiding mode", reset the focus to the first
		// property if we have genes that will be hidden
//		if (hideGenesWithoutData) {
//			if (rowPropertyType == PROPERTY_TYPE.GENE
//					&& !rowPropertiesWO.contains(rowPropertiesFocus)) {
//				rowPropertiesFocus = adjustFocus(rowProperties,
//						rowPropertiesWO, rowPropertiesFocus);
//			} else if (columnPropertyType == PROPERTY_TYPE.GENE
//					&& !columnPropertiesWO.contains(columnPropertiesFocus)) {
//				columnPropertiesFocus = adjustFocus(columnProperties,
//						columnPropertiesWO, columnPropertiesFocus);
//			}
//		}
	}

	/**
	 * Returns flag indicating hide samples without data.
	 * 
	 * @return boolean
	 */
	public boolean hideSamplesWithoutData() {
		return hideSamplesWithoutData;
	}

	/**
	 * Sets hideSamplesWithoutData boolean.
	 * 
	 * @param hideSamplesWithoutData
	 *            boolean
	 */
	public void setHideSamplesWithoutData(boolean hideSamplesWithoutData) {
		this.hideSamplesWithoutData = hideSamplesWithoutData;
		// if we are going into "hiding mode", reset the focus to the first
		// property if we have samples that will be hidden
//		if (hideSamplesWithoutData) {
//			if (rowPropertyType == PROPERTY_TYPE.SAMPLE) {
//				rowPropertiesFocus = (rowProperties.size() != rowPropertiesWO
//						.size()) ? rowPropertiesWO.firstElement()
//						: rowPropertiesFocus;
//			} else if (columnPropertyType == PROPERTY_TYPE.SAMPLE) {
//				columnPropertiesFocus = (columnProperties.size() != columnPropertiesWO
//						.size()) ? columnPropertiesWO.firstElement()
//						: columnPropertiesFocus;
//			}
//		}
	}

	/**
	 * Gets currently focused sample.
	 * 
	 * @return String
	 */
	public String getCurrentSample() {

		if (getRowPropertyType() == PROPERTY_TYPE.SAMPLE) {
			return rowPropertiesFocus;
		} else if (getColumnPropertyType() == PROPERTY_TYPE.SAMPLE) {
			return columnPropertiesFocus;
		} else {
			return constantProperty;
		}
	}

	/**
	 * Returns boolean indicating gene is constant property.
	 * 
	 * @return boolean
	 */
	public boolean constantPropertyTypeIsGene() {
		return (getConstantPropertyType() == PROPERTY_TYPE.GENE);
	}

	/**
	 * Returns boolean indicating gene is row property.
	 * 
	 * @return boolean
	 */
	public boolean rowPropertyTypeIsGene() {
		return (getRowPropertyType() == PROPERTY_TYPE.GENE);
	}

	/**
	 * Returns boolean indicating gene is row property.
	 * 
	 * @return boolean
	 */
	public boolean columnPropertyTypeIsGene() {
		return (getColumnPropertyType() == PROPERTY_TYPE.GENE);
	}

	/**
	 * Returns boolean indicating sample is constant property.
	 * 
	 * @return boolean
	 */
	public boolean constantPropertyTypeIsSample() {
		return (getConstantPropertyType() == PROPERTY_TYPE.SAMPLE);
	}

	/**
	 * Returns boolean indicating sample is row property.
	 * 
	 * @return boolean
	 */
	public boolean rowPropertyTypeIsSample() {
		return (getRowPropertyType() == PROPERTY_TYPE.SAMPLE);
	}

	/**
	 * Returns boolean indicating sample is row property.
	 * 
	 * @return boolean
	 */
	public boolean columnPropertyTypeIsSample() {
		return (getColumnPropertyType() == PROPERTY_TYPE.SAMPLE);
	}

	/**
	 * Constructor (private).
	 * 
	 * @param heatmapPanel
	 *            HeatmapPanel
	 * @param constantPropertyType
	 *            PROPERTY_TYPE
	 * @param constantProperty
	 *            String
	 * @param rowPropertyType
	 *            PROPERTY_TYPE
	 * @param rowProperties
	 *            List<String>
	 * @param rowPropertiesWO
	 *            List<String>
	 * @param rowPropertiesFocus
	 *            String
	 * @param columnPropertyType
	 *            PROPERTY_TYPE
	 * @param columnProperties
	 *            List<String>
	 * @param columnPropertiesWO
	 *            List<String>
	 * @param columnPropertiesFocus
	 *            String
	 */
	private HeatmapPanelConfiguration(HeatmapPanel heatmapPanel,
			PROPERTY_TYPE constantPropertyType, String constantProperty,
			PROPERTY_TYPE rowPropertyType, List<String> rowProperties,
			List<String> rowPropertiesWO, String rowPropertiesFocus,
			PROPERTY_TYPE columnPropertyType, List<String> columnProperties,
			List<String> columnPropertiesWO, String columnPropertiesFocus) {

		// init members
		this.heatmapPanel = heatmapPanel;
		this.constantPropertyType = constantPropertyType;
		this.constantProperty = constantProperty;
		this.rowPropertyType = rowPropertyType;
		this.rowProperties = rowProperties;
		this.rowPropertiesWO = rowPropertiesWO;
		this.rowPropertiesFocus = rowPropertiesFocus;
		this.columnPropertyType = columnPropertyType;
		this.columnProperties = columnProperties;
		this.columnPropertiesWO = columnPropertiesWO;
		this.columnPropertiesFocus = columnPropertiesFocus;
	}

	/**
	 * Called to adjust col or row focus when rows or columns without data are
	 * hidden
	 * 
	 * @param properties
	 *            List<String>
	 * @param propertiesWO
	 *            List<String>
	 * @param propertiesFocus
	 *            String
	 * @return String
	 */
//	private String adjustFocus(List<String> properties,
//			List<String> propertiesWO, String propertiesFocus) {
//
//		String newPropertiesFocus;
//		int index = properties.indexOf(propertiesFocus);
//		boolean directionUp = !properties.lastElement().equals(propertiesFocus);
//		DataTypeMatrix dataTypeMatrix = getDataTypeMatrix();
//
//		do {
//			if (index == properties.size() - 1 && directionUp)
//				directionUp = false;
//			newPropertiesFocus = properties.elementAt((directionUp) ? index++
//					: index--);
//		} while (!propertyHasData(newPropertiesFocus, propertiesWO,
//				dataTypeMatrix));
//
//		return newPropertiesFocus;
//	}

	/**
	 * Method to determine if property has experiment data.
	 * 
	 * @param property
	 *            String
	 * @param propertiesWO
	 *            List<String>
	 * @param dataTypeMatrix
	 *            DataTypeMatrix
	 * @return boolean
	 */
//	private boolean propertyHasData(String property, List<String> propertiesWO,
//			DataTypeMatrix dataTypeMatrix) {
//
//		// look directly in propertiesWO
//		if (propertiesWO.contains(property))
//			return true;
//
//		// otherwise property may have data indirectly, need to map id
//		property = GeneMapper.getGene(dataTypeMatrix, property,
//				cytoscape.Cytoscape.getNodeAttributes());
//		return (property != null && propertiesWO.contains(property));
//	}

	/**
	 * Returns data type matrix.
	 * 
	 * @return DataTypeMatrix
	 */
//	private DataTypeMatrix getDataTypeMatrix() {
//
//		if (getRowPropertyType() == HeatmapPanelConfiguration.PROPERTY_TYPE.DATA_TYPE) {
//			return HeatmapUtil.getDataTypeMatrix(getRowPropertiesFocus());
//		} else if (getColumnPropertyType() == HeatmapPanelConfiguration.PROPERTY_TYPE.DATA_TYPE) {
//			return HeatmapUtil.getDataTypeMatrix(getColumnPropertiesFocus());
//		}
//
//		// outta here
//		return HeatmapUtil.getDataTypeMatrix(getConstantProperty());
//	}
}
