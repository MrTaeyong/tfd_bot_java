package gui;

import java.util.Vector;

public class ThreadListController extends Thread {
	private Vector<BlogParseThread> threadList;
	private int threadCount;
	
	public ThreadListController() {
		threadList = new Vector<BlogParseThread>();
	}
	
	public void addThread() {
		BlogParseThread thread = new BlogParseThread();
		thread.start();
		threadList.add(thread);
		threadCount++;
	}
	
	public void run() {
		while(true) {
			try {
				Thread.sleep(10000);
				Vector<BlogParseThread> tempThreadList = new Vector<BlogParseThread>();
				for(BlogParseThread bpt : threadList) {
					if(bpt.isAlive())
						tempThreadList.add(bpt);
				}
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
