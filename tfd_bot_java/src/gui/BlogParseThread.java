package gui;

import controller.BlogParseManager;

public class BlogParseThread extends Thread {
	public BlogParseManager blogManager;
	public void run() {
		blogManager = new BlogParseManager();
		blogManager.getBlog();
		blogManager = null;
	}
}
