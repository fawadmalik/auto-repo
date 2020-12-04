package com.e2eqa.autoRepoZiad.amazonMobileChargePageTests;

import java.util.List;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import com.e2eqa.autoRepoZiad.base.BrowserDriverFactory;
import com.e2eqa.autoRepoZiad.pages.AmazonMobileChargePage;

import lombok.extern.log4j.Log4j2;

@Log4j2
public class AmazonLogic {
	
	protected WebDriver driver;
	private String browser;
	private boolean headless;
	private WebDriverWait wdWait;

	private List<String> phoneNumbers;
	
	public AmazonLogic(List<String> phoneNumbers, String browser, boolean headless) {
		this.phoneNumbers = phoneNumbers;
		this.browser = browser;
		this.headless = headless;
		createDriver();
		wdWait = new WebDriverWait(driver, 10, 500);
	}

	private void createDriver() {
		BrowserDriverFactory factory = new BrowserDriverFactory(browser, headless);
		this.driver = factory.createDriver();
		this.driver.manage().window().maximize();
	}

	public void tearDown() {
		log.info("Close driver");
		driver.quit();
	}

	public void completeRunNumbers() {
		log.info("Starting");

		// open main page
		AmazonMobileChargePage amazonPage = new AmazonMobileChargePage(driver, log);
		amazonPage.openPage();

		By btnBy = By.xpath("//*[@id='a-autoid-0-announce']");
		wdWait.until(ExpectedConditions.invisibilityOfElementLocated(btnBy));
		wdWait.until(ExpectedConditions.elementToBeClickable(btnBy));
		// find the "have account" button and click on it
		log.info("Starting input and circle capture with " + phoneNumbers.size() + " cell numbers");
		
		
		for(String num : phoneNumbers) {
			WebElement phoneInput = driver.findElement(By.xpath("//*[@id='mobileNumberTextInputId']"));
			log.info("Trying " + num);
			phoneInput.clear();
			phoneInput.sendKeys(num);
			String xpath = "//*[@id='operatorAndCircleHiddenInputId']";
			WebElement circleInput = driver.findElement(By.xpath(xpath));
			log.info("Circle info: " + circleInput.getAttribute("value"));
		}
		
//		WebElement haveAcctBtn = driver.findElement(By.xpath("//a[@data-test='have-account']"));
//		haveAcctBtn.click();
	}
}
