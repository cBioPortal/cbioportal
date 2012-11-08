package org.mskcc.mondrian.internal.gui.heatmap;

import org.cytoscape.model.CyRow;
import org.cytoscape.view.vizmap.mappings.ContinuousMapping;
import org.cytoscape.view.vizmap.mappings.DiscreteMapping;
import org.cytoscape.view.vizmap.mappings.PassthroughMapping;

import java.awt.*;

/*
 * TODO: Check when this table is initiated and whether it will cause a NullPointerException at any time.
 */

public class EmptyHeatmapTableModel extends HeatmapTableModel {
    @Override
    public String getRowName(int row) {
        return null;
    }

    @Override
    public CyRow getCyRow(int row, int col) {
        return null;
    }

    @Override
    public Double getMin() {
        return null;
    }

    @Override
    public Double getMax() {
        return null;
    }

    @Override
    public Double getMean() {
        return null;
    }

    @Override
    public ContinuousMapping<Double, Paint> getContinuousMapping(int col) {
        return null;
    }

    @Override
    public PassthroughMapping<Long, Paint> getPassthroughMapping(int col) {
        return null;
    }

    @Override
    public DiscreteMapping<Long, Paint> getDiscreteMapping(int col) {
        return null;
    }

    @Override
    public int getRowCount() {
        return 0;
    }

    @Override
    public int getColumnCount() {
        return 0;
    }

    @Override
    public Object getValueAt(int i, int i1) {
        return null;
    }
}
