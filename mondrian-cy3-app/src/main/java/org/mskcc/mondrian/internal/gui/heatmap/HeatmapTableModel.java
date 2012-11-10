package org.mskcc.mondrian.internal.gui.heatmap;

import java.awt.Paint;

import javax.swing.table.AbstractTableModel;

import org.cytoscape.model.CyRow;
import org.cytoscape.view.vizmap.mappings.ContinuousMapping;
import org.cytoscape.view.vizmap.mappings.DiscreteMapping;
import org.cytoscape.view.vizmap.mappings.PassthroughMapping;

@SuppressWarnings("serial")
public abstract class HeatmapTableModel extends AbstractTableModel {
	public abstract String getRowName(int row);
	public abstract CyRow getCyRow(int row, int col);
	public abstract Double getMin();
	public abstract Double getMax();
	public abstract Double getMean();
	public abstract ContinuousMapping<Double, Paint> getContinuousMapping(int col);
	public abstract PassthroughMapping<Long, Paint> getPassthroughMapping(int col);
	public abstract DiscreteMapping<Long, Paint> getDiscreteMapping(int col);
}	

