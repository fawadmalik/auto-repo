package com.e2eqa.autoRepoZiad.pages;

import org.apache.logging.log4j.Logger;
import org.openqa.selenium.WebDriver;

public class AmazonMobileChargePage extends BasePageObject {

	private String pageUrl = "https://www.amazon.in/hfc/mobileRecharge?ref_=apay_deskhome_MobileRecharge";
	
	public AmazonMobileChargePage(WebDriver driver, Logger log) {
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
