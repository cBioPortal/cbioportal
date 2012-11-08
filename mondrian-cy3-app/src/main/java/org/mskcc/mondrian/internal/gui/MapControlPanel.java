package org.mskcc.mondrian.internal.gui;

import java.awt.BorderLayout;
import java.awt.Component;
import java.net.URL;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JEditorPane;
import javax.swing.JPanel;

import org.cytoscape.application.swing.CytoPanelComponent;
import org.cytoscape.application.swing.CytoPanelName;
import org.mskcc.mondrian.internal.configuration.MondrianConfiguration;

/**
 * Panel in which to manage expression data.
 *
 * @author Benjamin Gross
 * @author Dazhi Jiao
 */
public class MapControlPanel extends JPanel implements CytoPanelComponent {
	private static final long serialVersionUID = -6950355019232851316L;
	protected static MapControlPanel instance;
	
	public static MapControlPanel getInstance(MondrianConfiguration mondrianConfiguration) {
		if (instance == null) {
			instance = new MapControlPanel(mondrianConfiguration);
		}
		return instance;
	}
	
	private MapControlPanel(MondrianConfiguration mondrianConfiguration) {
		
		// add a dummy panel
		JEditorPane pane = new JEditorPane();
		setLayout(new BorderLayout());
		add(pane, BorderLayout.CENTER);
		this.setVisible(true);
	}

	@Override
	public Component getComponent() {
		return this;
	}

	@Override
	public CytoPanelName getCytoPanelName() {
		return CytoPanelName.SOUTH;
	}

	@Override
	public Icon getIcon() {
		URL url = this.getClass().getResource("/breakpoint_group.gif");
		return new ImageIcon(url);
	}

	@Override
	public String getTitle() {
		return "Mondrian Control Panel";
	}

}
