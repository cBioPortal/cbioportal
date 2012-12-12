package org.mskcc.mondrian.internal.gui;

import java.awt.BorderLayout;
import java.awt.Component;
import java.net.URL;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JEditorPane;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

import org.cytoscape.application.swing.CytoPanelComponent;
import org.cytoscape.application.swing.CytoPanelName;
import org.cytoscape.model.CyNetwork;
import org.mskcc.mondrian.internal.MondrianApp;
import org.mskcc.mondrian.internal.MondrianUtil;
import org.mskcc.mondrian.internal.configuration.MondrianConfiguration;
import javax.swing.border.TitledBorder;
import javax.swing.JTabbedPane;
import javax.swing.JButton;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;

/**
 * Panel in which to manage expression data.
 *
 * @author Benjamin Gross
 * @author Dazhi Jiao
 */
public class DataTypesPanel extends JPanel implements CytoPanelComponent {
	private static final long serialVersionUID = -7362884992020398542L;
	protected static DataTypesPanel instance;
	
	public DataTypesPanel() {
		
		// add a dummy panel
		JEditorPane pane = new JEditorPane();
		setLayout(new BorderLayout());
		add(pane, BorderLayout.CENTER);
		
		JTabbedPane tabbedPane = new JTabbedPane(JTabbedPane.TOP);
		tabbedPane.setBorder(new TitledBorder(null, "Data Management", TitledBorder.LEADING, TitledBorder.TOP, null, null));
		add(tabbedPane, BorderLayout.NORTH);
		
		JPanel panel = new JPanel();
		panel.setBorder(new TitledBorder(null, "Load Data from Web", TitledBorder.LEADING, TitledBorder.TOP, null, null));
		tabbedPane.addTab("Load", null, panel, null);
		
		JButton btnLoad = new JButton("Load");
		btnLoad.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				CyNetwork network = MondrianUtil.getCurrentNetwork();
				if (network == null) {
					JOptionPane.showMessageDialog(MondrianApp.getInstance().getDataTypesPane(),
							"No network found. Please open a network first before importing cBio data!");
					return;
				}
				PortalImportDialog dialog = PortalImportDialog.getInstance();
				dialog.setLocationRelativeTo(MondrianApp.getInstance().getDesktopApp().getJFrame());
				dialog.setModal(true);
				dialog.setVisible(true);
			}
		});
		panel.add(btnLoad);
		
		JPanel panel_1 = new JPanel();
		tabbedPane.addTab("Save/Unload", null, panel_1, null);
		this.setVisible(true);
	}

	@Override
	public Component getComponent() {
		return this;
	}

	@Override
	public CytoPanelName getCytoPanelName() {
		return CytoPanelName.EAST;
	}

	@Override
	public Icon getIcon() {
		URL url = this.getClass().getResource("/breakpoint_group.gif");
		return new ImageIcon(url);
	}

	@Override
	public String getTitle() {
		return "Mondrian";
	}
}
