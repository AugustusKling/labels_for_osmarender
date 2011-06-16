package utilities.gui;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JPanel;
import javax.swing.JTextField;

/**
 * A simple file chooser which offers a text field to enter a file name and a
 * button to open a file chooser
 */
public class FileChooser extends JPanel {
	/**
	 * 
	 */
	private static final long serialVersionUID = -584372460771571588L;
	private File file = new File("");
	private JTextField text;
	private TYPES type;

	private static enum COMMANDS {
		FILE_CHOSEN
	};

	public static enum TYPES {
		OPEN, SAVE
	};

	public FileChooser(String buttonCaption, TYPES type) {
		this.type = type;
		this.setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
		text = this.createTextField();
		this.add(text);
		JButton button = this.createButton(buttonCaption);
		this.add(button);
	}

	private JButton createButton(String buttonCaption) {
		final JButton button = new JButton(buttonCaption);
		button.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				JFileChooser fc = new JFileChooser();
				fc.setSelectedFile(file);
				int selection;
				switch (type) {
				case OPEN:
					selection = fc.showOpenDialog(button);
					break;
				case SAVE:
					selection = fc.showSaveDialog(button);
					break;
				default:
					// Compiler requires this line of dead code
					throw new RuntimeException();
				}
				if (selection == JFileChooser.APPROVE_OPTION) {
					update(fc.getSelectedFile());
				}
			}
		});
		return button;
	}

	private JTextField createTextField() {
		final JTextField text = new JTextField();
		text.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				file = new File(text.getText());
			}
		});
		return text;
	}

	/**
	 * Sets a new file
	 * 
	 * @param selectedFile
	 */
	private void update(File selectedFile) {
		file=selectedFile;
		text.setText(selectedFile.getAbsolutePath());
	}

	/**
	 * @return The file which was selected by the user
	 */
	public File getSelectedFile() {
		return new File(file.getAbsolutePath());
	}

	public void addActionListener(ActionListener actionListener) {
		ActionEvent e = new ActionEvent(this, COMMANDS.FILE_CHOSEN.ordinal(),
				"change");
		actionListener.actionPerformed(e);
	}

	/**
	 * Sets a new path
	 * @param defaultFile
	 */
	public void setFile(File defaultFile) {
		File selectedFile = new File(defaultFile.toURI());
		update(selectedFile);
	}
}
