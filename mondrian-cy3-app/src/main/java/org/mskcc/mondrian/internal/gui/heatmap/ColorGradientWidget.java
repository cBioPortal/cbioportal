package org.mskcc.mondrian.internal.gui.heatmap;

//imports

import java.awt.Font;
import java.awt.Image;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Dimension;
import java.awt.Rectangle;
import java.awt.Graphics2D;
import java.awt.GradientPaint;
import java.awt.event.MouseEvent;
import java.awt.event.MouseAdapter;
import java.awt.image.BufferedImage;
import javax.swing.JComponent;
import java.util.List;
import java.util.ArrayList;

/**
 * Class which renders data type color gradient.
 * 
 * @author Benjamin Gross
 */
public class ColorGradientWidget extends JComponent {

	/**
	 * Legend Position Enumeration.
	 */
	public static enum LEGEND_POSITION {

		// data types
		TOP("Top"), RIGHT("Right"), BOTTOM("Bottom"), LEFT("Left"), NA("NA");

		// string ref for readable name
		private String type;

		// constructor
		LEGEND_POSITION(String type) {
			this.type = type;
		}

		// method to get enum readable name
		public String toString() {
			return type;
		}
	}

	// some statics
	private static final int POSITION_LEGEND_WIDTH = 15;
	private static final int HSPACER = 5;
	private static final int VSPACER = 3;

	// other required refs
	private Image img;
	private String title;
	private String cookedTitle;
	private Dimension cookedTitleDimension;
	private int widgetWidth;
	private int widgetHeight;
	private Color borderColor;
	private final boolean variableWidth;
	private final boolean variableHeight;
	private final int verticalMargin;
	private final int horizontalMargin;
	private ColorGradientTheme colorGradientTheme;
	private ColorGradientRange colorGradientRange;
	private Rectangle minimumConditionGradientRectangle;
	private Rectangle centerConditionGradientRectangle;
	private Rectangle maximumConditionGradientRectangle;
	private final boolean isLegend;
	private final LEGEND_POSITION legendPosition;
	private final boolean renderPositionLegend;
	private Dimension minimumConditionValueDimension;
	private Dimension averageValueDimension;
	private Dimension maximumConditionValueDimension;
	private String minimumConditionValueString;
	private String averageValueString;
	private String maximumConditionValueString;
	private int maxStringHeight;
	private int gradientHeight;
	private int gradientWidth;
	private int positionLegendWidth;
	private int positionLegendHeight;

	/**
	 * Our implementation of Component setBounds(). If we don't do this, the
	 * individual canvas do not get rendered.
	 * 
	 * @param x
	 *            int
	 * @param y
	 *            int
	 * @param width
	 *            int
	 * @param height
	 *            int
	 */
	public void setBounds(int x, int y, int width, int height) {
		super.setBounds(x, y, width, height);

		if ((width > 0) && (height > 0)) {
			widgetWidth = width;
			widgetHeight = height;
			img = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
		}
	}
	
	

	/**
	 * Gets colorGradientTheme loaded.
	 * 
	 * @return ColorGradientTheme
	 */
	public ColorGradientTheme getColorGradientTheme() {
		return colorGradientTheme;
	}

	/**
	 * Gets colorGradientRange loaded.
	 * 
	 * @return ColorGradientRange
	 */
	public ColorGradientRange getColorGradientRange() {
		return colorGradientRange;
	}

	/**
	 * Let layout manager know our minimum size.
	 * 
	 * @return Dimension
	 */
	public Dimension getMinimumSize() {

		if (minimumConditionValueDimension != null) {
			int legendWidth = (isLegend && legendPosition != LEGEND_POSITION.NA) ? POSITION_LEGEND_WIDTH + HSPACER : 0;
			int minWidth = (horizontalMargin * 2 + legendWidth + (int) minimumConditionValueDimension.getWidth()
					+ HSPACER + (int) averageValueDimension.getWidth() + HSPACER + (int) maximumConditionValueDimension
					.getWidth());
			return new Dimension(minWidth, (variableHeight) ? 0 : widgetHeight);

		} else {
			return new Dimension((variableWidth) ? 0 : widgetWidth, (variableHeight) ? 0 : widgetHeight);
		}
	}

	/**
	 * Let layout manager know our preferred size.
	 * 
	 * @return Dimension
	 */
	public Dimension getPreferredSize() {

		return getMinimumSize();
	}

	/**
	 * Method to export/print color gradient widget.
	 * 
	 * @param g
	 *            Graphics
	 * @param renderStrings
	 *            boolean
	 */
	public void export(Graphics g, boolean renderStrings) {

		// paint bg white
		Graphics2D g2d = (Graphics2D) g;
		g2d.setPaint(Color.WHITE);
		g2d.fillRect(0, 0, getWidth(), getHeight());
		renderComponent(g, renderStrings);
	}

	/**
	 * This is where we render the gradient.
	 * 
	 * @param g
	 *            Graphics
	 */
	public void paintComponent(Graphics g) {
		renderComponent(g, true);
	}

	/**
	 * Gets the title string (cooked to fit to ui)
	 * 
	 * @return String
	 */
	public String getTitle() {
		return cookedTitle;
	}

	/**
	 * Gets condition value strings as list, size 3:
	 * 
	 * List(0): minimum List(1): average List(2): max
	 * 
	 * @return List<String>
	 */
	public List<String> getConditionValueStrings() {

		// list to return
		List<String> toReturn = new ArrayList<String>();

		toReturn.add(minimumConditionValueString);
		toReturn.add(averageValueString);
		toReturn.add(maximumConditionValueString);

		return toReturn;
	}

	/**
	 * Constructor (private).
	 * 
	 * @param title
	 *            String
	 * @param mondrianConfiguration
	 *            MondrianConfiguration
	 * @param heatmapWidget
	 *            HeatmapWidget
	 * @param widgetWidth
	 *            int
	 * @param widgetHeight
	 *            int
	 * @param hMargin
	 *            int
	 * @param vMargin
	 *            int
	 * @param colorGradientTheme
	 *            ColorGradientTheme
	 * @param colorGradientRange
	 *            ColorGradientRange
	 * @param isLegend
	 *            boolean
	 * @param legendPosition
	 *            LEGEND_POSITION
	 * @return ColorGradientWidget
	 */
	public ColorGradientWidget(String title, int widgetWidth, int widgetHeight, int hMargin, int vMargin,
			ColorGradientTheme colorGradientTheme, ColorGradientRange colorGradientRange, boolean isLegend,
			LEGEND_POSITION legendPosition) {

		// init member vars
		this.title = title;
		this.widgetHeight = widgetHeight;
		this.widgetWidth = widgetWidth;
		this.verticalMargin = vMargin;
		this.horizontalMargin = hMargin;
		this.borderColor = Color.BLACK;
		this.colorGradientTheme = colorGradientTheme;
		this.colorGradientRange = colorGradientRange;
		this.isLegend = isLegend;
		this.legendPosition = legendPosition;
		this.renderPositionLegend = !(legendPosition == LEGEND_POSITION.NA);

		// are we variable width ?
		variableWidth = (widgetWidth == 0);

		// are we variable height ?
		variableHeight = (widgetHeight == 0);

		// setup mouse listener
		if (!isLegend)
			attachMouseListener();

		// set condition value strings
		setConditionValueStrings();
	}
	
	public void reset(String title, int widgetWidth, int widgetHeight, 
			ColorGradientTheme colorGradientTheme, ColorGradientRange colorGradientRange) {

		// init member vars
		this.title = title;
		this.widgetWidth = widgetWidth;
		this.widgetHeight = widgetHeight;
		this.colorGradientTheme = colorGradientTheme;
		this.colorGradientRange = colorGradientRange;

		// set condition value strings
		setConditionValueStrings();
	}

	/**
	 * Attaches a Mouse Listener, used for Rollovers.
	 */
	private void attachMouseListener() {

		// new mouse listener inner class
		this.addMouseListener(new MouseAdapter() {

			/**
			 * Mouse Pressed handler.
			 * 
			 * @param e
			 *            Mouse Event Object.
			 */
			public void mousePressed(MouseEvent e) {
				//TODO: mondrianConfiguration.setColorTheme(colorGradientTheme);
			}

			/**
			 * Mouse Entered handler.
			 * 
			 * @param e
			 *            Mouse Event Object.
			 */
			public void mouseEntered(MouseEvent e) {
				borderColor = Color.GREEN;
				repaint();
			}

			/**
			 * Mouse Exit handler.
			 * 
			 * @param e
			 *            MouseEvent Object.
			 */
			public void mouseExited(MouseEvent e) {
				borderColor = Color.BLACK;
				repaint();
			}
		});
	}

	/**
	 * Sets max string height
	 */
	private void setMaxStringHeight() {
		// calc max string height
		maxStringHeight = (int) Math.max(minimumConditionValueDimension.getHeight(),
				(int) Math.max(averageValueDimension.getHeight(), maximumConditionValueDimension.getHeight()));
		maxStringHeight = (int) Math.max(maxStringHeight, cookedTitleDimension.getHeight());
	}

	/**
	 * Sets position legend width.
	 */
	private void setPositionLegendDimensions() {
		if (renderPositionLegend) {
			int width = (variableWidth) ? getSize().width : widgetWidth;
			width -= horizontalMargin * 2;
			positionLegendWidth = 15 + HSPACER;
		} else {
			positionLegendWidth = 0;
		}
	}

	/**
	 * Set cooked title string.
	 * 
	 * @param g2d
	 *            Graphics2D
	 */
	private void setCookedTitleString(Graphics2D g2d) {

		// setup some vars used below
		int width = (variableWidth) ? getSize().width : widgetWidth;
		width -= horizontalMargin * 2 + positionLegendWidth;
		java.awt.FontMetrics fontMetrics = g2d.getFontMetrics();

		String tmpStr = "";
		for (int lc = 0; lc <= title.length(); lc++) {
			tmpStr = title.substring(0, lc);
			if (fontMetrics.stringWidth(tmpStr) <= width) {
				cookedTitle = tmpStr;
			} else {
				// we've gone over, replace last 3 chars with "."
				cookedTitle = tmpStr.substring(0, lc - 4);
				cookedTitle += "...";
				break;
			}
		}
	}

	/**
	 * Sets the cooked string dimension.
	 * 
	 * @param g2d
	 *            Graphics2D
	 */
	private void setCookedTitleStringDimension(Graphics2D g2d) {

		// Rectangle reference
		java.awt.geom.Rectangle2D rect;

		// get graphics context font metrics
		java.awt.FontMetrics fontMetrics = g2d.getFontMetrics();

		// min value string dimensions
		rect = fontMetrics.getStringBounds(cookedTitle, g2d);
		cookedTitleDimension = new Dimension((int) rect.getWidth(), (int) rect.getHeight());
	}

	/**
	 * Set condition value strings.
	 */
	private void setConditionValueStrings() {

		// we only want 2 significant digits
		final java.text.NumberFormat formatter = new java.text.DecimalFormat("#,###,###.##");
		minimumConditionValueString = formatter.format(colorGradientRange.getMinValue());
		averageValueString = formatter.format(colorGradientRange.getCenterLowValue()
				+ (colorGradientRange.getCenterHighValue() - colorGradientRange.getCenterLowValue()) / 2);
		maximumConditionValueString = formatter.format(colorGradientRange.getMaxValue());
	}

	/**
	 * Sets the min and max condition value strings dimensions.
	 * 
	 * @param g2d
	 *            Graphics2D
	 */
	private void setConditionValueStringDimensions(Graphics2D g2d) {

		// Rectangle reference
		java.awt.geom.Rectangle2D rect;

		// get graphics context font metrics
		java.awt.FontMetrics fontMetrics = g2d.getFontMetrics();

		// min value string dimensions
		rect = fontMetrics.getStringBounds(minimumConditionValueString, g2d);
		minimumConditionValueDimension = new Dimension((int) rect.getWidth(), (int) rect.getHeight());

		// average value dimension
		rect = fontMetrics.getStringBounds(averageValueString, g2d);
		averageValueDimension = new Dimension((int) rect.getWidth(), (int) rect.getHeight());

		// max value string dimensions
		rect = fontMetrics.getStringBounds(maximumConditionValueString, g2d);
		maximumConditionValueDimension = new Dimension((int) rect.getWidth(), (int) rect.getHeight());
	}

	/**
	 * Computes min, max, legend rectangles
	 */
	private void computeGradientRectangles() {

		// get widget dimension
		widgetWidth = (variableWidth) ? getSize().width : widgetWidth;
		widgetHeight = (variableHeight) ? getSize().height : widgetHeight;
		gradientHeight = widgetHeight - verticalMargin * 2;
		gradientWidth = widgetWidth - horizontalMargin * 2;

		// minimum rectangle
		minimumConditionGradientRectangle = new Rectangle(horizontalMargin, verticalMargin, widgetWidth / 2
				- horizontalMargin, gradientHeight);

		// maximum rectangle
		maximumConditionGradientRectangle = new Rectangle(widgetWidth / 2, verticalMargin, widgetWidth / 2
				- horizontalMargin, gradientHeight);
	}

	/**
	 * Computes min, center, max, legend rectangles
	 */
	private void computeLegendGradientRectangles() {

		// get widget dimension
		widgetWidth = (variableWidth) ? getSize().width : widgetWidth;
		widgetHeight = (variableHeight) ? getSize().height : widgetHeight;

		// set gradient height
		gradientWidth = widgetWidth - horizontalMargin * 2 - positionLegendWidth;
		gradientHeight = widgetHeight - verticalMargin * 2 - maxStringHeight - VSPACER;
		if (cookedTitle != null && cookedTitle.length() > 0) {
			gradientHeight -= maxStringHeight + VSPACER;
		}

		// set rectangle y
		int rectYPos = verticalMargin + VSPACER;
		if (cookedTitle != null && cookedTitle.length() > 0) {
			rectYPos += maxStringHeight + VSPACER;
		}

		// compute normals
		double centerLowNormal = (colorGradientRange.getCenterLowValue() - colorGradientRange.getMinValue())
				/ (colorGradientRange.getMaxValue() - colorGradientRange.getMinValue());
		double centerHighNormal = (colorGradientRange.getCenterHighValue() - colorGradientRange.getMinValue())
				/ (colorGradientRange.getMaxValue() - colorGradientRange.getMinValue());

		// minimum to center low rectangle
		int minRectXPos = horizontalMargin + positionLegendWidth;
		int minRectWidth = (int) (centerLowNormal * gradientWidth);
		minimumConditionGradientRectangle = new Rectangle(minRectXPos, rectYPos, minRectWidth, gradientHeight);

		// center low to center high rectangle
		int centerRectXPos = minRectXPos + minRectWidth;
		int centerRectWidth = (int) (centerHighNormal * gradientWidth - minRectWidth);
		centerConditionGradientRectangle = new Rectangle(centerRectXPos, rectYPos, centerRectWidth, gradientHeight);

		// center high to maximum rectangle
		int maxRectXPos = centerRectXPos + centerRectWidth;
		maximumConditionGradientRectangle = new Rectangle(maxRectXPos, rectYPos, gradientWidth - centerRectWidth
				- minRectWidth, gradientHeight);
	}

	/**
	 * This is where we render the gradient.
	 * 
	 * @param g
	 *            Graphics
	 * @param renderStrings
	 *            boolean
	 */
	private void renderComponent(Graphics g, boolean renderStrings) {

		if (img != null) {

			// set our graphics context
			Graphics2D g2d = ((BufferedImage) img).createGraphics();

			// clear background
			clearImage(g2d);

			// save font
			Font savedFont = g2d.getFont();

			// set new font
			g2d.setFont(new Font("Default", Font.BOLD, 10));

			if (isLegend) {
				setPositionLegendDimensions(); // should come first
				setCookedTitleString(g2d);
				setCookedTitleStringDimension(g2d);
				setConditionValueStringDimensions(g2d);
				setMaxStringHeight();
				computeLegendGradientRectangles();
				if (renderPositionLegend) {
					renderPositionLegend(g2d);
				}
				renderLegendGradient(g2d);
				if (renderStrings) {
					renderCookedTitleString(g2d);
					renderConditionValueStrings(g2d);
				}
			} else {
				computeGradientRectangles();
				renderGradient(g2d);
			}

			// restore font
			g2d.setFont(savedFont);

			// render image
			g.drawImage(img, 0, 0, null);
		}
	}

	/**
	 * Renders the position legend.
	 * 
	 * @param g2d
	 *            Graphics2D
	 */
	private void renderPositionLegend(Graphics2D g2d) {

		final int renderWidth = positionLegendWidth - HSPACER;
		final int renderHeight = renderWidth;

		final int xPos = horizontalMargin;
		int gradientCenter = verticalMargin + VSPACER;
		if (cookedTitle != null && cookedTitle.length() > 0) {
			gradientCenter += maxStringHeight + VSPACER;
		}
		gradientCenter += gradientHeight / 2;
		final int yPos = gradientCenter - renderHeight / 2;

		// set num data types
//		final DataTypeMatrixManager dataTypeMatrixManager = DataTypeMatrixManager.getInstance();
//		final java.util.Vector<String> dataTypes = dataTypeMatrixManager.getLoadedDataTypes(false);
//
//		// set index
//		final int index = (dataTypes.size() == 2) ? ((legendPosition == LEGEND_POSITION.LEFT) ? 0 : 1) : legendPosition
//				.ordinal();
//
//		// render background
//		g2d.setPaint(Color.WHITE);
//		g2d.fillRect(xPos, yPos, renderWidth - 1, renderHeight - 1);
//		// render triangle
//		g2d.setPaint(Color.GRAY);
//		final java.awt.Shape triangle = ((HeatmapWidget.HeatmapWidgetCellRenderer) heatmapWidget
//				.getDefaultRenderer(Color.class)).getShape(dataTypes.size(), index, xPos, yPos, renderWidth,
//				renderHeight);
//		g2d.fill(triangle);
//		// render border
//		g2d.setPaint(Color.BLACK);
//		final Rectangle rect = new Rectangle(xPos, yPos, renderWidth - 1, renderHeight - 1);
//		g2d.draw(rect);

	}

	/**
	 * Renders the gradient.
	 * 
	 * @param g2d
	 *            Graphics2D
	 */
	private void renderGradient(Graphics2D g2d) {

		// create the gradient from min to center
		final GradientPaint gradientLow = new GradientPaint((float) minimumConditionGradientRectangle.getX(),
				(float) minimumConditionGradientRectangle.getY(), colorGradientTheme.getMinColor(),
				(float) maximumConditionGradientRectangle.getX(), (float) minimumConditionGradientRectangle.getY(),
				colorGradientTheme.getCenterColor());

		g2d.setPaint(gradientLow);
		g2d.fillRect((int) minimumConditionGradientRectangle.getX(), (int) minimumConditionGradientRectangle.getY(),
				(int) minimumConditionGradientRectangle.getWidth(), (int) minimumConditionGradientRectangle.getHeight());

		// create the gradient from center to max
		final GradientPaint gradientHigh = new GradientPaint((float) maximumConditionGradientRectangle.getX(),
				(float) maximumConditionGradientRectangle.getY(), colorGradientTheme.getCenterColor(),
				(float) (maximumConditionGradientRectangle.getX() + maximumConditionGradientRectangle.getWidth()),
				(float) maximumConditionGradientRectangle.getY(), colorGradientTheme.getMaxColor());
		g2d.setPaint(gradientHigh);
		g2d.fillRect((int) maximumConditionGradientRectangle.getX(), (int) maximumConditionGradientRectangle.getY(),
				(int) maximumConditionGradientRectangle.getWidth(), (int) maximumConditionGradientRectangle.getHeight());

		// draw outline around gradient
		final Rectangle rect = new Rectangle(horizontalMargin, verticalMargin, gradientWidth - 1, gradientHeight - 1);
		g2d.setPaint(borderColor);
		g2d.draw(rect);
	}

	/**
	 * Renders the gradient.
	 * 
	 * @param g2d
	 *            Graphics2D
	 */
	private void renderLegendGradient(Graphics2D g2d) {

		// minimum gradient
		final GradientPaint gradientLow = new GradientPaint((float) minimumConditionGradientRectangle.getX(),
				(float) minimumConditionGradientRectangle.getY(), colorGradientTheme.getMinColor(),
				(float) centerConditionGradientRectangle.getX(), (float) centerConditionGradientRectangle.getY(),
				colorGradientTheme.getCenterColor());

		g2d.setPaint(gradientLow);
		g2d.fillRect((int) minimumConditionGradientRectangle.getX(), (int) minimumConditionGradientRectangle.getY(),
				(int) minimumConditionGradientRectangle.getWidth(), (int) minimumConditionGradientRectangle.getHeight());

		// center gradient
		g2d.setPaint(colorGradientTheme.getCenterColor());
		g2d.fillRect((int) centerConditionGradientRectangle.getX(), (int) centerConditionGradientRectangle.getY(),
				(int) centerConditionGradientRectangle.getWidth(), (int) centerConditionGradientRectangle.getHeight());

		// max gradient
		final GradientPaint gradientHigh = new GradientPaint((float) centerConditionGradientRectangle.getX(),
				(float) centerConditionGradientRectangle.getY(), colorGradientTheme.getCenterColor(),
				(float) (maximumConditionGradientRectangle.getX() + maximumConditionGradientRectangle.getWidth()),
				(float) maximumConditionGradientRectangle.getY(), colorGradientTheme.getMaxColor());
		g2d.setPaint(gradientHigh);
		g2d.fillRect((int) maximumConditionGradientRectangle.getX(), (int) maximumConditionGradientRectangle.getY(),
				(int) maximumConditionGradientRectangle.getWidth(), (int) maximumConditionGradientRectangle.getHeight());

		// draw outline around gradient - use any rectangle for starting y
		final Rectangle rect = new Rectangle(horizontalMargin + positionLegendWidth,
				(int) minimumConditionGradientRectangle.getY(), gradientWidth - 1, gradientHeight - 1);
		g2d.setPaint(borderColor);
		g2d.draw(rect);
	}

	/**
	 * Renders the cooked title string.
	 * 
	 * @param g2d
	 *            Graphics2D
	 */
	private void renderCookedTitleString(Graphics2D g2d) {

		// set the paint
		g2d.setPaint(Color.BLACK);

		// compute drawstring x pos - centered over gradient
		int startingPos = horizontalMargin + positionLegendWidth;
		int width = (variableWidth) ? getSize().width : widgetWidth;
		width -= horizontalMargin * 2 + positionLegendWidth;
		int xPos = startingPos + width / 2 - (int) cookedTitleDimension.getWidth() / 2;

		// compute drawstring y pos
		int yPos = verticalMargin + maxStringHeight;

		// render min string - above gradient
		g2d.drawString(cookedTitle, xPos, yPos);
	}

	/**
	 * Renders the condition values.
	 * 
	 * @param g2d
	 *            Graphics2D
	 */
	private void renderConditionValueStrings(Graphics2D g2d) {

		// set the paint
		g2d.setPaint(Color.BLACK);

		// compute drawstring y pos - we can use the height of any gradient
		// rectangle
		int yPos = verticalMargin + VSPACER + gradientHeight + maxStringHeight;
		if (cookedTitle != null && cookedTitle.length() > 0) {
			yPos += VSPACER + maxStringHeight;
		}

		// render min string - above gradient
		g2d.drawString(minimumConditionValueString, horizontalMargin + positionLegendWidth, yPos);

		// render center low string - below gradient
		g2d.drawString(averageValueString, (horizontalMargin + positionLegendWidth + gradientWidth / 2)
				- (int) averageValueDimension.getWidth() / 2, yPos);

		// render max string - above gradient
		g2d.drawString(maximumConditionValueString, widgetWidth - horizontalMargin
				- (int) maximumConditionValueDimension.getWidth(), yPos);
	}

	/**
	 * Utility function to clean the background of the image, using
	 * m_backgroundColor
	 * 
	 * @param image2D
	 *            Graphics2D
	 */
	private void clearImage(Graphics2D image2D) {

		// set the alpha composite on the image, and clear its area
		java.awt.Composite origComposite = image2D.getComposite();
		image2D.setComposite(java.awt.AlphaComposite.getInstance(java.awt.AlphaComposite.SRC));
		image2D.setPaint(getBackground());
		image2D.fillRect(0, 0, img.getWidth(null), img.getHeight(null));
		image2D.setComposite(origComposite);
	}
}
