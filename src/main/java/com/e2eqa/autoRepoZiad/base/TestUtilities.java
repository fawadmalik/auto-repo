package com.e2eqa.autoRepoZiad.base;

public class TestUtilities extends TestBase {

	// STATIC SLEEP 
	protected void sleep(long millis) {
		try {
			Thread.sleep(millis);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
}
