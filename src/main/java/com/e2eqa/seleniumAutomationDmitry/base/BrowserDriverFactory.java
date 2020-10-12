package com.e2eqa.seleniumAutomationDmitry.base;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.firefox.FirefoxDriver;

import lombok.extern.log4j.Log4j2;

@Log4j2
public class BrowserDriverFactory {

	private ThreadLocal<WebDriver> driver = new ThreadLocal<WebDriver>();
	private String browser;
	private boolean headless;

	public BrowserDriverFactory(String browser, boolean headless) {
		this.browser = browser.toLowerCase();
		this.headless = headless;
	}

	public WebDriver createDriver() {
		log.info("Create driver: " + browser);

		switch (browser) {
		case "chrome":
			System.setProperty("webdriver.chrome.driver", "src/main/resources/chromedriver.exe");
			ChromeOptions options = new ChromeOptions();
			options.setHeadless(headless);
			driver.set(new ChromeDriver(options));
			break;

		case "firefox":
			System.setProperty("webdriver.gecko.driver", "src/main/resources/geckodriver.exe");
			driver.set(new FirefoxDriver());
			break;

		default:
			System.out.println("Do not know how to start: " + browser + ", starting chrome.");
			System.setProperty("webdriver.chrome.driver", "src/main/resources/chromedriver.exe");
			driver.set(new ChromeDriver());
			break;
		}

		return driver.get();
	}
}
