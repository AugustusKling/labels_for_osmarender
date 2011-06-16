package runables;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;

import utilities.gui.FileChooser;
import utilities.gui.FileChooser.TYPES;

/**
 * Provides a simple GUI to download a portion of OSM's map, invoke Osmarender
 * and run the evolutionary algorithm on the generated image
 */
public class Gui extends JFrame {

	/**
	 * Identifier just in case someone wants to serialize
	 */
	private static final long serialVersionUID = -3394563266035218973L;

	/**
	 * Displays the window
	 * 
	 * @param args
	 */
	public static void main(String[] args) {
		Gui window = new Gui();
		window.setVisible(true);
	}

	private FileChooser mapData;
	private FileChooser styleChooser;
	private FileChooser target;

	private Gui() {
		this.setTitle("Playground GUI");
		this.setSize(new Dimension(300, 500));
		this.setDefaultCloseOperation(EXIT_ON_CLOSE);
		this.setLayout(new BoxLayout(this.getContentPane(), BoxLayout.Y_AXIS));

		// Add usage hint
		JTextArea hint = new JTextArea(
				"Select map data, styles and rules, then run calculations."
						+ "The newly created file is saved to „{oldFile}-processed.svg“.");
		hint.setLineWrap(true);
		hint.setEditable(false);
		this.add(hint);

		// Add tabs to select local or remote OSM data
		JTabbedPane mode = new JTabbedPane();
		mapData = new FileChooser("Choose OSM file", TYPES.OPEN);
		mapData.setFile(new File("sample.osm"));
		mode.add("Local", mapData);
		mode.add("Remote",
				new JLabel("Remote sources are not implemented yet."));
		this.add(mode);

		// Allow to choose map style and rules
		styleChooser = new FileChooser("Choose Osmarender style", TYPES.OPEN);
		styleChooser.setFile(new File("sample.xml"));
		styleChooser.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				// TODO Auto-generated method stub

			}
		});
		this.add(styleChooser);

		// Allow to choose target map
		target = new FileChooser("Choose Rendering Target", TYPES.SAVE);
		target.setFile(new File("sample.svg"));
		this.add(target);

		// Add execute button
		this.addButtons();
	}

	private void addButtons() {
		JPanel buttonHolder = new JPanel();
		buttonHolder.setLayout(new BoxLayout(buttonHolder, BoxLayout.X_AXIS));
		JButton process = new JButton("Create map");
		final Gui gui = this;
		process.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if (!mapData.getSelectedFile().exists()
						|| !styleChooser.getSelectedFile().exists()) {
					JOptionPane.showMessageDialog(gui,
							"You need to select map data and a stylesheet.");
					return;
				}
				if (!target.getSelectedFile().canWrite()) {
					JOptionPane.showMessageDialog(gui,
							"Target file is not writeable");
					return;
				} else {
					int overwrite = JOptionPane.showConfirmDialog(gui,
							"Overwrite the target file?", "",
							JOptionPane.YES_NO_OPTION);
					if (overwrite != JOptionPane.YES_OPTION) {
						return;
					}
				}
				ProcessMap processMap = new ProcessMap();
				processMap.setMapData(mapData.getSelectedFile());
				processMap.setStyleFile(styleChooser.getSelectedFile());
				try {
					processMap.createMap();
				} catch (Exception e1) {
					JOptionPane.showMessageDialog(gui,
							"Could not generate map (see console for details): "
									+ e1.getMessage());
					e1.printStackTrace();
				}
			}
		});
		buttonHolder.add(process);
		buttonHolder.add(new JButton("Create & display map"));
		this.add(buttonHolder);
	}
}
