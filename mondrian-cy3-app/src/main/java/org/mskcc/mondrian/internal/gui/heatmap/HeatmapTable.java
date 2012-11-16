package org.mskcc.mondrian.internal.gui.heatmap;

import java.awt.Component;
import java.awt.Font;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.List;

import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JViewport;
import javax.swing.UIManager;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableColumn;

/*
 *  The table that shows the Heatmap
 */
public class HeatmapTable implements ChangeListener, PropertyChangeListener {
	private JTable main;
	private JTable fixed;
	private JScrollPane scrollPane;
	private HeatmapTableModel model;
	private ColorGradientWidget legend;

	/*
	 * Specify the number of columns to be fixed and the scroll pane containing
	 * the table.
	 */
	public HeatmapTable(JScrollPane scrollPane, HeatmapTableModel model) {
		this.scrollPane = scrollPane;
		this.model = model;
		updateCyTable();
	}
	
	public void setDefaultCellRenderer(HeatmapTableCellRenderer renderer) {
		main.setDefaultRenderer(Double.class, renderer);
	}
	
	@SuppressWarnings("serial")
	public void updateCyTable() {
		//int nRow = cyTable.getRowCount();
		this.main = new JTable(model.getRowCount(), model.getColumnCount()) {
			@SuppressWarnings({ "unchecked", "rawtypes" })
			public Class getColumnClass(int c) {
				return Double.class;
			}
		};
		main.addPropertyChangeListener(this);

		// Use the existing table to create a new table sharing
		// the DataModel and ListSelectionModel
		main.setModel(this.model);
		scrollPane.getViewport().setView(main);

		// Add the fixed table to the scroll pane
		fixed = new RowHeaderTable(main);
		
		scrollPane.setRowHeaderView(fixed);
		scrollPane.setCorner(JScrollPane.UPPER_LEFT_CORNER, fixed.getTableHeader());

		// Synchronize scrolling of the row header with the main table
		scrollPane.getRowHeader().addChangeListener(this);
		
		// set our default renderer
		
		//JSparklinesBarChartTableCellRenderer renderer = new JSparklinesBarChartTableCellRenderer(PlotOrientation.HORIZONTAL, -100d, 100d);
		//renderer.showAsHeatMap(ColorGradient.RedBlackGreen);
		//main.setDefaultRenderer(Double.class, renderer);
		main.setDefaultRenderer(Double.class, new HeatmapTableCellRenderer(true));

		// we are not focusable
		main.setFocusable(true);

		// only allow single cell selection
		main.setCellSelectionEnabled(true);
		main.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);

		// need to set autoresizing to off so that 
		// width of table is not forced to be size of viewport
		main.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
		
		/*
		if (columnWidths.size() == 0) {
			setAllColumnWidths(getMaxColumnWidth());
		}
		else {
			setAllColumnWidths(columnWidths);
		}
		
		// set selected
		if (selectedCell.x >= 0 && selectedCell.y >= 0) {
			changeSelection(selectedCell.y, selectedCell.x, false, false);
		}
		*/		
	}

	/*
	 * Return the table being used in the row header
	 */
	public JTable getFixedTable() {
		return fixed;
	}
	
	public JScrollPane getScrollPane() {
		return this.scrollPane;
	}

	//
	// Implement the ChangeListener
	//
	public void stateChanged(ChangeEvent e) {
		// Sync the scroll pane scrollbar with the row header

		JViewport viewport = (JViewport) e.getSource();
		scrollPane.getVerticalScrollBar()
				.setValue(viewport.getViewPosition().y);
	}

	//
	// Implement the PropertyChangeListener
	//
	public void propertyChange(PropertyChangeEvent e) {
		// Keep the fixed table in sync with the main table

		if ("selectionModel".equals(e.getPropertyName())) {
			fixed.setSelectionModel(main.getSelectionModel());
		}

		if ("model".equals(e.getPropertyName())) {
			//fixed.setModel(main.getModel());
		}
	}
	
	/**
	 * Method used to get column widths for all columns in table.
	 *
	 * @return List<Integer>
	 */
	public List<Integer> getColumnWidths() {

		// to return
		List<Integer> toReturn = new java.util.ArrayList<Integer>();

		for (int lc = 0; lc < main.getColumnCount(); lc++) {
			toReturn.add(main.getColumnModel().getColumn(lc).getPreferredWidth());
		}

		// out of here
		return toReturn;
	}
	
	public JTable getMain() {
		return main;
	}

	public void setMain(JTable main) {
		this.main = main;
	}
	
	@SuppressWarnings("serial")
	class RowHeaderTable extends JTable {
		private JTable main;
		public RowHeaderTable(JTable main) {
			this.main = main;
			
			setFocusable( false );
			setAutoCreateColumnsFromModel( false );
			setSelectionModel( main.getSelectionModel() );
			
			TableColumn column = new TableColumn();
			column.setHeaderValue(" ");
			addColumn( column );
			column.setCellRenderer(new RowNumberRenderer());
			
			getColumnModel().getColumn(0).setPreferredWidth(150);
			setPreferredScrollableViewportSize(getPreferredSize());
		}
		
		/*
		 *  Delegate method to main table
		 */
		@Override
		public int getRowCount() {
			return main.getRowCount();
		}	
		
		@Override
		public int getRowHeight(int row) {
			return main.getRowHeight(row);
		}	
		
		@Override
		public Object getValueAt(int row, int column) {
			if (column >= 1) {
				return "";
			} else {
				return model.getRowName(row);
			}
		}
		
		/*
		 *  Don't edit data in the main TableModel by mistake
		 */
		@Override
		public boolean isCellEditable(int row, int column) {
			return false;
		}		
		
		/*
		 *  Borrow the renderer from JDK1.4.2 table header
		 */
		@SuppressWarnings("serial")
		private class RowNumberRenderer extends DefaultTableCellRenderer {
			public RowNumberRenderer() {
				setHorizontalAlignment(JLabel.CENTER);
			}

			public Component getTableCellRendererComponent(JTable table,
					Object value, boolean isSelected, boolean hasFocus,
					int row, int column) {
				if (table != null) {
					JTableHeader header = table.getTableHeader();

					if (header != null) {
						setForeground(header.getForeground());
						setBackground(header.getBackground());
						setFont(header.getFont());
					}
				}

				if (isSelected) {
					setFont( getFont().deriveFont(Font.BOLD) );
				}

				setText((value == null) ? "" : value.toString());
				setBorder(UIManager.getBorder("TableHeader.cellBorder"));

				return this;
			}
		}		
	}
}


