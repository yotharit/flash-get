package flashget;

import javax.swing.JFrame;
import javax.swing.JPanel;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Observable;
import java.util.Observer;

import javax.swing.JTextField;
import javax.swing.JLabel;
import javax.swing.BoxLayout;
import javax.swing.SwingConstants;

import com.sun.prism.paint.Color;

import javax.swing.JButton;
import javax.swing.JTextArea;
import javax.swing.JTextPane;
import javax.swing.JScrollPane;
import javax.swing.JProgressBar;

/**
 * GUI FOR FLASHGET
 * @author Tharit Pongsaneh
 *
 */
public class DownloadManagerGui extends JFrame implements Observer {
	private DownloadManager manager;
	private JTextField textField;
	private JProgressBar progressBar;
	private JTextArea textArea;
	private Thread download;

	public DownloadManagerGui() {
		manager = new DownloadManager();
		manager.addObserver(this);
		JPanel panel = new JPanel();
		getContentPane().add(panel, BorderLayout.NORTH);
		panel.setLayout(new BorderLayout(0, 0));

		JLabel lblDownloadManager = new JLabel("DOWNLOAD MANAGER");
		lblDownloadManager.setHorizontalAlignment(SwingConstants.CENTER);
		panel.add(lblDownloadManager, BorderLayout.NORTH);

		textField = new JTextField();
		textField.setToolTipText("ENTER URL\n");
		panel.add(textField, BorderLayout.CENTER);
		textField.setColumns(10);

		JPanel panel_1 = new JPanel();
		getContentPane().add(panel_1, BorderLayout.SOUTH);

		JButton btnDownload = new JButton("DOWNLOAD");
		panel_1.add(btnDownload);

		JButton btnClear = new JButton("CLEAR");
		panel_1.add(btnClear);

		JButton btnCancle = new JButton("CANCLE");
		panel_1.add(btnCancle);

		btnCancle.setEnabled(false);

		JPanel panel_2 = new JPanel();
		getContentPane().add(panel_2, BorderLayout.CENTER);
		panel_2.setLayout(new BorderLayout(0, 0));

		progressBar = new JProgressBar();
		panel_2.add(progressBar, BorderLayout.NORTH);

		textArea = new JTextArea();
		textArea.setAutoscrolls(true);
		panel_2.add(textArea, BorderLayout.CENTER);
		btnClear.addActionListener(e -> {
			textField.setText("");
			progressBar.setValue(0);
			textArea.setText("");
		});
		btnDownload.addActionListener(e -> {
			try {
				textArea.setText("");
				manager.setURL(textField.getText());
				progressBar.setMaximum((int) manager.getFilesize());
				progressBar.setStringPainted(true);
				btnCancle.setEnabled(true);
				textArea.setText("FROM: "+manager.getUrlName() + "\n" +"FILE: " +manager.getFilename() + "\n" + "FILESIZE "
						+ manager.getFilesize() + " Byte");
				download = new Thread(new Runnable() {
					public void run() {
						try {
							manager.download();
						} catch (InterruptedException e) {
							textArea.setText("Cancle Download");
						}
					}
				});
				download.start();
			} catch (RuntimeException e1) {
				textArea.setText("ERROR");
			}
		});
		btnCancle.addActionListener(e -> {
			download.interrupt();
			manager.cancle();
		});

	}

	public void run() {
		this.setDefaultCloseOperation(EXIT_ON_CLOSE);
		this.setPreferredSize(new Dimension(600, 200));
		this.pack();
		this.setVisible(true);
	}

	@Override
	public void update(Observable o, Object arg) {
		if (o instanceof DownloadManager) {
			progressBar.setValue((int) ((DownloadManager) o).getCurrentSize());
		}
	}
}
