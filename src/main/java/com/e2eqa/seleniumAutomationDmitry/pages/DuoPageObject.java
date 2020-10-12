package com.e2eqa.seleniumAutomationDmitry.pages;

import java.util.Base64;

import org.apache.logging.log4j.Logger;
import org.openqa.selenium.WebDriver;

public class DuoPageObject extends BasePageObject {

	private String pageUrlEnc = "aHR0cHM6Ly93d3cuZHVvbGluZ28uY29t";
	private String pageUrl = new String (Base64.getDecoder().decode(pageUrlEnc));

	public DuoPageObject(WebDriver driver, Logger log) {
		super(driver, log);
	}

	/** Open WelcomePage with it's url */
	public void openPage() {
		log.info("Opening page: " + pageUrl);
		openUrl(pageUrl);
		log.info("Page opened!");
	}
	/** Open a custom path along the host */
	public IframesPage openPage(String customUrl) {
		String customPageUrl = pageUrl.concat("/").concat(customUrl); 
		log.info("Opening page: " + customPageUrl);
		openUrl(customPageUrl);
		log.info("Page opened!");
		return new IframesPage(driver, log);
	}
}
