package gui;

import java.util.Vector;

public class ThreadListController extends Thread {
	private Vector<BlogParseThread> threadList;
	private int threadCount;
	
	public ThreadListController() {
		threadList = new Vector<BlogParseThread>();
	}
	
	/**
	 * 새로운 스레드를 생성하여 실행하고 스레드 리스트에 넣음
	 */
	public void addThread() {
		BlogParseThread thread = new BlogParseThread();
		thread.start();
		threadList.add(thread);
		threadCount++;
	}
	
	/**
	 * 10초마다 스레드 리스트를 확인하여 동작이 완료된 스레드를 리스트에서 제거
	 */
	public void run() {
		while(true) {
			try {
				Thread.sleep(10000);
				Vector<BlogParseThread> tempThreadList = new Vector<BlogParseThread>();
				for(BlogParseThread bpt : threadList) {
					if(bpt.isAlive())
						tempThreadList.add(bpt);
				}
				threadList = null;
				threadList = tempThreadList;
				tempThreadList = null;
				threadCount = threadList.size();
			} catch (InterruptedException e) {
//				e.printStackTrace();
			}
		}
	}
	
	public int getThreadCount() {
		return threadCount;
	}
	
	/**
	 * 현재 리스트에 있는 스레드의 장소명과 진행사항을 문자열로 만들어 반환
	 * @return
	 */
	public String getLog() {
		String log = "";
		try {
			for(BlogParseThread blogThread : threadList)
				log += blogThread.blogManager.getCurrentPlaceName() + "\t" + blogThread.blogManager.getCurrentStartNumber() + "\n";
			return log;
		} catch(Exception e) {
			return "";
		}
	}
}
