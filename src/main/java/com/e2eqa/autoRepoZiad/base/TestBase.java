package com.e2eqa.autoRepoZiad.base;

import org.openqa.selenium.WebDriver;
import org.testng.ITestContext;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Optional;
import org.testng.annotations.Parameters;

import lombok.extern.log4j.Log4j2;

@Log4j2
public class TestBase {

	protected WebDriver driver;
	protected String testName;
	protected boolean headless;
	protected String browser;

	@Parameters({ "browser", "headless" })
	@BeforeMethod(alwaysRun = true)
	public void setUp(@Optional("chrome") String browser, @Optional("false") boolean headless,ITestContext ctx) {
		System.out.println(String.format("Params: browser: %s, headlesss: %b", browser, headless));
		this.headless = headless;
		this.browser = browser;
		this.testName = ctx.getCurrentXmlTest().getName();

		BrowserDriverFactory factory = new BrowserDriverFactory(browser, headless);
		driver = factory.createDriver();
		driver.manage().window().maximize();
	}

	@AfterMethod(alwaysRun = true)
	public void tearDown() {
		log.info("Close driver");
		// Close browser
		driver.quit();
	}

}
