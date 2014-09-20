package gui;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;

public class BlogGUIParserController extends Thread implements ActionListener {	
	private ThreadListController tListController; // 현재 생성된 스레드를 관리하는 객체
	private BlogGUIParser gui; // GUI
	private int threadCount; // 사용자가 설정한 최대 스레드 갯수
	
	public BlogGUIParserController() {
		gui = new BlogGUIParser(this);
		gui.currentThread.setText("0");
		Runtime.getRuntime().addShutdownHook(new ShutdownHookThread());
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
	
	/**
	 * 1초마다 GUI에 정보를 새로 고침하고 현재 스레드가 사용자가 설정한 스레드보다 적으면 스레드 생성
	 */
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
	
	private class ShutdownHookThread extends Thread {
		public void run() {
			tListController.stopThread();
		}
	}
	
	public static void main(String[] args) {
		new BlogGUIParserController().start();
	}
}
