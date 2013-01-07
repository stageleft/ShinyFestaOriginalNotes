package com.github.ShinyFestaOriginalNotes;

import java.awt.EventQueue;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JProgressBar;
import java.awt.Button;
import java.awt.TextField;
import java.awt.TextArea;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.io.PrintWriter;
import java.io.StringWriter;

public class ApplicationMain extends JFrame {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private static JPanel contentPane;
	private static TextField textField_inputFileName;
	private static TextField textField_outputFileName;
	private static TextArea textArea;
	private static ApplicationMain frame;
	private static AllNotes op = null;
	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					frame = new ApplicationMain();
					frame.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	/**
	 * Create the frame.
	 */
	public ApplicationMain() {
		setTitle("ShinyFesta Original Notes Maker");
		// エラー表示とかしたいので先にテキストエリアの定義(だけ)
		textArea = new TextArea();
		textArea.setText("");

		// 環境変数の呼び出し 
	//	String homedir=System.getenv("HOME");
	//	String documentdir = homedir + "\\Documents\\";	// 通常環境
	//	String videodir    = homedir + "\\Videos\\";	// 通常環境
		String documentdir = "D:\\Documents\\";			// stageleftの環境
		String videodir    = "D:\\Videos\\";			// stageleftの環境

		// GUI系の設定
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(100, 100, 450, 450);
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(contentPane);
		contentPane.setLayout(null);
		
		JLabel lblShinyfestaoriginalnotesMaker = new JLabel("ShinyFesta Original Notes Maker");
		lblShinyfestaoriginalnotesMaker.setBounds(5, 5, 429, 30);
		contentPane.add(lblShinyfestaoriginalnotesMaker);
		
		JLabel label = new JLabel("Score:");
		label.setBounds(5, 45, 50, 13);
		contentPane.add(label);
		
		JLabel label_1 = new JLabel("Output");
		label_1.setBounds(5, 85, 50, 13);
		contentPane.add(label_1);
		
		JButton btnExec = new JButton("Exec");
		btnExec.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				// user input code
				try {
					textArea.setText("translating...");
					op = new AllNotes(textField_inputFileName.getText(), textField_outputFileName.getText());
					op.makeMovingNotes();
					op = null;
					textArea.setText("translate completed.");
				} catch (Throwable e) {
					e.printStackTrace();
					final StringWriter error_log = new StringWriter();
					e.printStackTrace(new PrintWriter(error_log));
					textArea.setText(error_log.toString());
				}
			}
		});
		btnExec.setBounds(140, 375, 141, 37);
		contentPane.add(btnExec);
		
		JProgressBar progressBar = new JProgressBar();
		progressBar.setBounds(61, 121, 361, 13);
		contentPane.add(progressBar);
		
		JButton btnExit = new JButton("Exit");
		btnExit.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				frame.setVisible(false);
			}
		});
		btnExit.setBounds(293, 375, 141, 37);
		contentPane.add(btnExit);
		
		textField_inputFileName = new TextField();
		textField_inputFileName.setBounds(61, 41, 294, 23);
		contentPane.add(textField_inputFileName);
		textField_inputFileName.setText(documentdir + "score.txt");
	
		Button button = new Button("Open");
		button.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				JFileChooser openDialog = new JFileChooser(textField_inputFileName.getText());
				openDialog.addChoosableFileFilter(new FileNameExtensionFilter("スコアファイル(*.txt)", "txt"));
				int openDialogResult = openDialog.showOpenDialog(null);
				if (openDialogResult == JFileChooser.APPROVE_OPTION)
				{
					textField_inputFileName.setText(openDialog.getSelectedFile().getAbsolutePath());
				}
			}
		});
		button.setBounds(358, 41, 76, 23);
		contentPane.add(button);

		textField_outputFileName = new TextField();
		textField_outputFileName.setBounds(61, 81, 294, 23);
		contentPane.add(textField_outputFileName);
		textField_outputFileName.setText(videodir + "MovingImage.avi");
		
		Button button_1 = new Button("Save");
		button_1.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				JFileChooser openDialog = new JFileChooser(textField_outputFileName.getText());
				openDialog.addChoosableFileFilter(new FileNameExtensionFilter("結果ファイル(*.avi)", "avi"));
				int openDialogResult = openDialog.showSaveDialog(null);
				if (openDialogResult == JFileChooser.APPROVE_OPTION)
				{
					textField_outputFileName.setText(openDialog.getSelectedFile().getAbsolutePath());
				}
			}
		});
		button_1.setBounds(358, 81, 76, 23);
		contentPane.add(button_1);
		
		JLabel label_2 = new JLabel("Progress...");
		label_2.setBounds(5, 121, 50, 13);
		contentPane.add(label_2);
		
		JLabel label_3 = new JLabel("Status");
		label_3.setBounds(5, 144, 50, 13);
		contentPane.add(label_3);
		
		textArea.setBounds(61, 140, 363, 229);
		contentPane.add(textArea);
	}
}
