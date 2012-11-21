package org.mskcc.mondrian.internal.gui.heatmap;

import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.Shape;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.border.Border;

import org.cytoscape.view.presentation.property.BasicVisualLexicon;
import org.cytoscape.view.vizmap.VisualMappingFunctionFactory;
import org.cytoscape.view.vizmap.mappings.BoundaryRangeValues;
import org.cytoscape.view.vizmap.mappings.ContinuousMapping;
import org.mskcc.mondrian.internal.MondrianApp;
import org.mskcc.mondrian.internal.configuration.MondrianConfiguration;

@SuppressWarnings("serial")
public class HeatmapTableCellRenderer extends JLabel implements javax.swing.table.TableCellRenderer {

	public static final int MAX_NUM_TRIANGLES = 4;
	// members
	private List<Color> backgroundColors;
	private final boolean isBordered;
	private final Color selectedColor;
	private Border selectedBorder = null;
	private Border unselectedBorder = null;
	private final java.awt.BasicStroke stroke = new java.awt.BasicStroke(0.5f);
	private final Color BORDER_COLOR = Color.GRAY;
	private final java.text.NumberFormat formatter = new java.text.DecimalFormat("#,###,###.##");
	private Color paint;
	private ColorGradientTheme colorTheme;
	private CELL_DISPLAY heatmapMode = CELL_DISPLAY.SINGLE;		
	private String label;

	/**
	 * Constructor.
	 *
	 * @param isBordered boolean
	 */
	public HeatmapTableCellRenderer(boolean isBordered) {

		// init members
		this.isBordered = isBordered;
		this.selectedColor = Color.ORANGE;
		// setup widget props
		setOpaque(true); // must do this for background to show up.
	}

	/**
	 * Our implementation of TableCellRenderer interface.
	 *
	 * @param table JTable
	 * @param object Object
	 * @param isSelected boolean
	 * @param hasFocus boolean
	 * @param row int
	 * @param column int
	 */
	public Component getTableCellRendererComponent(JTable table, Object object,
												   boolean isSelected, boolean hasFocus,
												   int row, int column) {
		if (object instanceof String) {
			String str = object.toString();
			javax.swing.table.DefaultTableCellRenderer renderer = new javax.swing.table.DefaultTableCellRenderer();
			return renderer.getTableCellRendererComponent(table, str, isSelected,hasFocus, row, column); 
		}

		// get ref to cell object
		//cellObject = (CellObject)object;

		// get range of values
		// 
		HeatmapTableModel model = (HeatmapTableModel)table.getModel();
		this.setText(String.valueOf(object));
		if (object.equals(Double.NaN)) {
			this.paint = Color.LIGHT_GRAY;
		} else {
			ContinuousMapping<Double, Paint> cMapping = model.getContinuousMapping(column);
			this.paint = (Color)cMapping.getMappedValue(model.getCyRow(row, column));
		}

		// swap/set borders
		if (isBordered) {
			if (isSelected) {
				if (selectedBorder == null) {
					selectedBorder = BorderFactory.createMatteBorder(3,3,3,3, selectedColor);
				}
				setBorder(selectedBorder);
			}
			else {	
				if (unselectedBorder == null) {
					unselectedBorder = BorderFactory.createEmptyBorder(0,0,0,0);
				}
				setBorder(unselectedBorder);
			}
		}
    
		// set tooltip
		//TODO: setToolTipText(getToolTipText(measurements));

		return this;
	}
	
	/**
	 * Method called when exporting image (like to a pdf).
	 *
	 * @param g Graphics
	 */
	public void export(Graphics g) {
		paintComponent(g);

		Graphics2D g2d = (Graphics2D)g;
		g2d.setPaint(Color.WHITE);
		g2d.drawRect(0,0,getWidth(),getHeight());
	}

	/**
	 * Our implementation of paint component.
	 *
	 * @param g Graphics
	 */
	protected void paintComponent(Graphics g) {

		super.paintComponent(g);
		
		Graphics2D g2d = (Graphics2D)g;
		g2d.setStroke(stroke);
		int width = this.getWidth();
		int height = this.getHeight();
		switch (heatmapMode) {
		    case SINGLE:
		    	g2d.setPaint(this.paint);
			    g2d.fillRect(0, 0, getWidth(), getHeight());
			    g.setColor(Color.black);
			    g.drawString(this.getText(), 10, 10);
			    break;
			case TRIANGLES:
				g2d.setRenderingHint(java.awt.RenderingHints.KEY_ANTIALIASING, java.awt.RenderingHints.VALUE_ANTIALIAS_ON);
				displayTriangles(g2d);
				g2d.setRenderingHint(java.awt.RenderingHints.KEY_ANTIALIASING, java.awt.RenderingHints.VALUE_ANTIALIAS_OFF);
				break;
			case NUMERIC:
		    //case HEATSTRIP:
			//	displayHeatStrip(g2d);
			//	break;
		    //case TILES:
			//	displayTiles(g2d);
			//	break;
		}
	}

	/**
	 * Method to render heatstrips
	 *
	 * @param g2d Graphics2D
	 */
	private void displayHeatStrip(Graphics2D g2d) {

		double regionHeight = (backgroundColors.size() > 1) ? getHeight() * 0.5D : getHeight();
		double regionWidth = getWidth() / backgroundColors.size();
		boolean positive = false;
		int lc = -1;
		for (Color color : backgroundColors) {
			positive = !positive;
			g2d.setPaint(color);
			Shape shape = new java.awt.geom.Rectangle2D.Double(++lc*regionWidth, // positive x moves right
															   (positive) ? 0 : regionHeight, // postivie y moves down
															   regionWidth,
															   regionHeight);
			g2d.fill(shape);
		}
		if (backgroundColors.size() > 1) {
			// draw horizontal line down center
			g2d.setPaint(BORDER_COLOR);
			g2d.drawLine(0, (int)regionHeight, getWidth(), (int)regionHeight);
		}
	}

	/**
	 * Method to render triangles.
	 *
	 * @param g2d Graphics2D
	 */
	private void displayTriangles(Graphics2D g2d) {
		MondrianConfiguration mondrianConfiguration = MondrianApp.getInstance().getMondrianConfiguration();
		// if we only have 3 colors (data types) add a fourth, no data colored triangle
		if (backgroundColors.size() == MAX_NUM_TRIANGLES-1) {
			backgroundColors.add(mondrianConfiguration.getColorTheme().getNoDataColor());
		}

		int lc = -1;
		for (Color color : backgroundColors) {
			Shape shape = getShape(backgroundColors.size(), ++lc, 0, 0, getWidth(), getHeight());
			g2d.setPaint(color);
			g2d.fill(shape);
			// we dont attempt to draw more than 4 triangles
			if (lc == MAX_NUM_TRIANGLES-1) break;
		}
		g2d.setPaint(BORDER_COLOR);
		if (backgroundColors.size() > 1) {
			// draw line top right to lower left
			g2d.drawLine(getWidth(), 0, 0, getHeight());
		}
		if (backgroundColors.size() == 3) {
			// draw line center to lower right
			g2d.drawLine(getWidth() / 2, getHeight() / 2, getWidth(), getHeight());
		}
		if (backgroundColors.size() >= 4) {
			// draw another line top left, lower right
			g2d.drawLine(0, 0, getWidth(), getHeight());
		}
	}

	/** 
	 * Method to render tiles.
	 *
	 * @param g2d Graphics2D
	 */
	private void displayTiles(Graphics2D g2d) {
		MondrianConfiguration mondrianConfiguration = MondrianApp.getInstance().getMondrianConfiguration();
		int squareMatrixSize = getSquareMatrixSize(backgroundColors.size());

		// add squareMatrixSize - dataTypes no data color to background color collection
		int numColorsToAdd = (squareMatrixSize*squareMatrixSize) - backgroundColors.size();
		if (numColorsToAdd > 0) {
			Color noDataColor = mondrianConfiguration.getColorTheme().getNoDataColor();
			for (int lc = 0; lc < numColorsToAdd; lc++) {
				backgroundColors.add(noDataColor);
			}
		}

		int regionWidth = getWidth() / squareMatrixSize;
		int regionHeight = getHeight() / squareMatrixSize;

		for (int row = 0; row < squareMatrixSize; row++) {
			int yPos = row * regionHeight;
			for (int col = 0; col < squareMatrixSize; col++) {
				int xPos = col * regionWidth;
				Shape shape = new java.awt.geom.Rectangle2D.Double(xPos, // positive x moves right
																   yPos, // postivie y moves down
																   regionWidth,
																   regionHeight);
				g2d.setPaint(backgroundColors.get(row * squareMatrixSize + col));
				g2d.fill(shape);
				if (col > 0) {
					// draw vertical line left edge
					g2d.setPaint(BORDER_COLOR);
					g2d.drawLine(xPos, yPos, xPos, yPos + regionHeight);
				}
			}
			// draw horizontal line across top
			if (row > 0) {
				g2d.setPaint(BORDER_COLOR);
				g2d.drawLine(0, yPos, getWidth(), yPos);
			}
		}
	}

	/**
	 * Method to get a shape to render for triangle displays.
	 *
	 * @param numDataTypes int
	 * @param dataTypeIndex int
	 * @param originX int
	 * @param originY int
	 * @param width int
	 * @param height int
	 * @return Shape
	 */
	public Shape getShape(int numDataTypes, int dataTypeIndex, int originX, int originY, int width, int height) {

		if (numDataTypes == 1) {
			// return rectangle
			return new java.awt.geom.Rectangle2D.Double(0, 0, width, height);
		}

		// path to return
		java.awt.geom.GeneralPath toReturn = new java.awt.geom.GeneralPath();
		if (numDataTypes == 2) {
			if (dataTypeIndex == 0) {
				// return triangle left hand side
				toReturn.moveTo(originX, originY);
				toReturn.lineTo(originX + width, originY);
				toReturn.lineTo(originX, originY + height);
			}
			else {
				// return triangle right hand side
				toReturn.moveTo(originX, originY + height);
				toReturn.lineTo(originX + width, originY);
				toReturn.lineTo(originX + width, originY + height);
			}
		}
		if (numDataTypes >= 3) {
			if (dataTypeIndex == 0) {
				// return triangle top
				toReturn.moveTo(originX, originY);
				toReturn.lineTo(originX + width, originY);
				toReturn.lineTo(originX + width / 2, originY + height / 2);
			}
			else if (dataTypeIndex == 1) {
				// return triangle right
				toReturn.moveTo(originX + width / 2, originY + height / 2);
				toReturn.lineTo(originX + width, originY);
				toReturn.lineTo(originX + width, originY + height);
			}
			else if (dataTypeIndex == 2) {
				// return triangle bottom
				toReturn.moveTo(originX + width / 2, originY + height / 2);
				toReturn.lineTo(originX + width, originY + height);
				toReturn.lineTo(originX, originY + height);
			}
			else {
				// return triangle left
				toReturn.moveTo(originX, originY);
				toReturn.lineTo(originX + width / 2, originY + height / 2);
				toReturn.lineTo(originX, originY + height);
			}
		}

		// close the shape
		toReturn.closePath();

		// outta here
		return toReturn;
	}

	/**
	 * Given the number of data types,
	 * returns size of square matrix required to display datatypes in tiled display.
	 *
	 * @param numDataTypes int
	 * @return int
	 */
	private int getSquareMatrixSize(int numDataTypes) {

		int lc = 0;
		while (numDataTypes > lc*lc) {
			++lc;
		}
		return lc;
	}

	/**
	 * Method to get tooltip text.
	 *
	 * @param measurements List<String>
	 * @return String
	 */
//	private String getToolTipText(List<String> measurements) {
//		String rowProperty = cellObject.getRowName();
//		String columnProperty = cellObject.getColumnName();
//
//		String measurement = "";
//		if (mondrianConfiguration.getHeatmapCellDisplay() == CELL_DISPLAY.SINGLE) {
//			// do we have mutations ?
//			if (mondrianConfiguration.getDataTypeMatrix().isMutation()) {
//				measurement = getMutationString();
//			}
//			else {
//				Double m = cellObject.getMeasurement();
//				measurement = (m.toString().equalsIgnoreCase("NaN")) ? "None" : formatter.format(m);
//				measurement = "Measurement: " + measurement + "<br>";
//			}
//		}
//		else {
//			int lc = -1;
//			for (String mStr : measurements) {
//				if (++lc == MAX_NUM_TRIANGLES) break;
//				measurement += mStr + ((!mStr.endsWith("<br>")) ? "<br>" : "");
//			}
//		}
//
//		// outta here
//		return ("<html>Row: " + rowProperty + "<br>" +
//				"Column: " + columnProperty + "<br>" +
//				measurement + "</html>");
//	}

	/**
	 * Gets mutation string.
	 * 
	 * @return String
	 */
	private String getMutationString() {
		// string to return
		String toReturn = "";

		// setup some refs
//		HeatmapPanelConfiguration heatmapPanelConfiguration = mondrianConfiguration.getActiveHeatmapPanelConfiguration();
//		if (heatmapPanelConfiguration == null) return toReturn;
//		DataTypeMatrix matrix = mondrianConfiguration.getDataTypeMatrix();
//		Map<String, String> mutationsMap = matrix.getMutationMap();
//		
//		// we need a gene and sample to look up mutation string in mutationsMap
//		String gene;
//		if (heatmapPanelConfiguration.rowPropertyTypeIsGene()) {
//			gene = cellObject.getRowProperty();
//		}
//		else if (heatmapPanelConfiguration.columnPropertyTypeIsGene()) {
//			gene = cellObject.getColumnName();
//		}
//		else {
//			gene = heatmapPanelConfiguration.getConstantProperty();
//		}
//		gene = org.mskcc.mondrian.maps.GeneMapper.getGene(matrix,
//														  geneDescriptorsToGeneNamesMap.get(gene),
//														  Cytoscape.getNodeAttributes());
//		String sample;
//		if (heatmapPanelConfiguration.rowPropertyTypeIsSample()) {
//			sample = cellObject.getRowProperty();
//		}
//		else if (heatmapPanelConfiguration.columnPropertyTypeIsSample()) {
//			sample = cellObject.getColumnName();
//		}
//		else {
//			sample = heatmapPanelConfiguration.getConstantProperty();
//		}
//
//		// get mutation
//		String mutation = mutationsMap.get(gene + MutationUtil.MUTATION_MAP_KEY_DELIMETER + sample);
//		if (mutation == null || mutation.length() == 0) {
//			toReturn = "Mutations: None";
//		}
//		else {
//			toReturn = MutationUtil.getMutationHtml(mutation, false);
//		}

		// outta here
		return toReturn;
	}
	

	/**
	 * Cell Display Enumeration.
	 */
	public static enum CELL_DISPLAY {

		// data types
		NUMERIC("Numeric"), SINGLE("Single"), TRIANGLES("Triangles");
		// HEATSTRIP("Heat Strips"),
		// TILES("Tiles");

		// string ref for readable name
		private String type;

		// constructor
		CELL_DISPLAY(String type) {
			this.type = type;
		}

		// method to get enum readable name
		public String toString() {
			return type;
		}
	}	
}	


