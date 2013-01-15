package org.mskcc.mondrian.internal.gui.heatmap;

/**
 * Class used to store min, center, max (& orig) values.
 * 
 * @author Benjamin Gross
 */
public class ColorGradientRange {

	// final members
	final private double origMinValue;
	final private double origCenterLowValue;
	final private double origCenterHighValue;
	final private double origMaxValue;

	// mutable members
	private double minValue;
	private double centerLowValue;
	private double centerHighValue;
	private double maxValue;

	/**
	 * Constructor (private).
	 * 
	 * @param origMinValue
	 *            double
	 * @param origCenterLowValue
	 *            double
	 * @param origCenterHighValue
	 *            double
	 * @param origMaxValue
	 *            double
	 * @param minValue
	 *            double
	 * @param centerLowValue
	 *            double
	 * @param centerHighValue
	 *            double
	 * @param maxValue
	 *            double
	 */
	public ColorGradientRange(double origMinValue, double origCenterLowValue, double origCenterHighValue,
			double origMaxValue, double minValue, double centerLowValue, double centerHighValue, double maxValue) {

		// init members
		this.origMinValue = origMinValue;
		this.origCenterLowValue = origCenterLowValue;
		this.origCenterHighValue = origCenterHighValue;
		this.origMaxValue = origMaxValue;
		this.minValue = minValue;
		this.centerLowValue = centerLowValue;
		this.centerHighValue = centerHighValue;
		this.maxValue = maxValue;
	}

	// mutators
	public void setMinValue(double minValue) {
		this.minValue = minValue;
	}

	public void setCenterLowValue(double centerLowValue) {
		this.centerLowValue = centerLowValue;
	}

	public void setCenterHighValue(double centerHighValue) {
		this.centerHighValue = centerHighValue;
	}

	public void setMaxValue(double maxValue) {
		this.maxValue = maxValue;
	}

	// accessors
	public double getOrigMinValue() {
		return origMinValue;
	}

	public double getMinValue() {
		return minValue;
	}

	public double getOrigCenterLowValue() {
		return origCenterLowValue;
	}

	public double getCenterLowValue() {
		return centerLowValue;
	}

	public double getOrigCenterHighValue() {
		return origCenterHighValue;
	}

	public double getCenterHighValue() {
		return centerHighValue;
	}

	public double getOrigMaxValue() {
		return origMaxValue;
	}

	public double getMaxValue() {
		return maxValue;
	}
}
