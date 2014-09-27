package gui;

import java.util.Vector;

import controller.BlogParseManager;
import controller.DBController;

public class ThreadListController extends Thread {
	private final String _PLACE_TABLE_NAME = "place_info_2";
	private Vector<BlogParseManager> threadList;
	private int threadCount;
	
	public ThreadListController() {
		threadList = new Vector<BlogParseManager>();
	}
	
	/**
	 * 새로운 스레드를 생성하여 실행하고 스레드 리스트에 넣음
	 */
	public void addThread() {
		BlogParseManager thread = new BlogParseManager();
		thread.start();
		threadList.add(thread);
		threadCount++;
	}
	
	/**
	 * 10초마다 스레드 리스트를 확인하여 동작이 완료된 스레드를 리스트에서 제거
	 */
	public void run() {
		Vector<BlogParseManager> tempThreadList;
		while(true) {
			try {
				Thread.sleep(10000);
				tempThreadList = new Vector<BlogParseManager>();
				for(BlogParseManager bpt : threadList) {
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
	
	/**
	 * 현재 실행중인 스레드 갯수를 반환
	 * @return
	 */
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
			for(BlogParseManager blogThread : threadList)
				log += blogThread.getCurrentPlaceName() + "\t" + blogThread.getCurrentStartNumber() + "\n";
			return log;
		} catch(Exception e) {
			return "";
		}
	}
	
	/**
	 * 현재 실행중인 스레드에 인터럽트를 호출하고 DB의 update flag를 0으로 돌려놓음
	 */
	public void stopThread() {
		DBController dbcon = DBController.newInstance(DBController.Type.TFD);
		String placeName;
		for(BlogParseManager bpt : threadList) {
			bpt.interrupt();
			placeName = bpt.getCurrentPlaceName();
			dbcon.queryExecute("update " + _PLACE_TABLE_NAME + " set update_flag=0 where name='" + placeName + "'");
		}
	}
}
