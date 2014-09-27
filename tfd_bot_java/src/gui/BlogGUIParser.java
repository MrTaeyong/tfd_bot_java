package gui;

import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JTextArea;
import javax.swing.JTextField;

/**
 * 크롤링봇의 GUI
 * @author taeyong
 *
 */
public class BlogGUIParser extends JFrame {
	public JTextField threadCount;
	public JTextArea textLog;
	public JTextField currentThread;
	public BlogGUIParser(ActionListener action) {
		super("Blog Parser");
		setBounds(0, 0, 260, 295);
		setDefaultCloseOperation(EXIT_ON_CLOSE);
		getContentPane().setLayout(null);
		
		JLabel lblSetThread = new JLabel("Thread Setting");
		lblSetThread.setBounds(6, 6, 101, 16);
		getContentPane().add(lblSetThread);
		
		threadCount = new JTextField();
		threadCount.setBounds(119, 0, 40, 28);
		getContentPane().add(threadCount);
		threadCount.setColumns(10);
		
		textLog = new JTextArea();
		textLog.setEditable(false);
		textLog.setBounds(6, 72, 248, 195);
		getContentPane().add(textLog);
		
		JButton btnAddThread = new JButton("Set");
		btnAddThread.setBounds(168, 1, 75, 29);
		btnAddThread.addActionListener(action);
		getContentPane().add(btnAddThread);
		
		currentThread = new JTextField();
		currentThread.setEditable(false);
		currentThread.setBounds(119, 32, 40, 28);
		getContentPane().add(currentThread);
		currentThread.setColumns(10);
		
		JLabel lblCurrentThread = new JLabel("Current Thread");
		lblCurrentThread.setBounds(6, 34, 106, 16);
		getContentPane().add(lblCurrentThread);
		
		setResizable(false);
		setVisible(true);
	}
}
