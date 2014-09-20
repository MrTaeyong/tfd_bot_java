package gui;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class BlogGUIParserController extends Thread implements ActionListener{	
	private ThreadListController tListController;
	private BlogGUIParser gui;
	private int threadCount;
	
	public BlogGUIParserController() {
		gui = new BlogGUIParser(this);
		gui.currentThread.setText("0");
	}
	
	@Override
	public void actionPerformed(ActionEvent e) {
		if(e.getActionCommand().equals("Set")) {
			try {
				threadCount = Integer.parseInt(gui.threadCount.getText());
				gui.threadCount.setText("");
				gui.currentThread.setText(String.valueOf(tListController.getThreadCount()));
			} catch (Exception ex){
				gui.textLog.setText("Input error\n" + gui.textLog.getText());
				gui.threadCount.setText("");
			}
		}
	}
	
	public void run() {
		tListController = new ThreadListController();
		tListController.start();
		
		while(true) {
			try {
				Thread.sleep(1000);
				if(threadCount > tListController.getThreadCount())
					tListController.addThread();
				gui.textLog.setText(tListController.getLog());
				gui.currentThread.setText(String.valueOf(threadCount));
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}
	
	public static void main(String[] args) {
		new BlogGUIParserController().start();
	}
}
