package com.e2eqa.autoRepoZiad;

import static org.testng.Assert.assertEquals;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.testng.annotations.Test;

public class SimpleTest {

	@Test
	public void firstTest() {
		String expectedTitle = "Google";
		System.setProperty("webdriver.chrome.driver", "src/test/resources/chromedriver.exe");
		WebDriver driver = new ChromeDriver();
		
		driver.get("http://Google.ca");
		System.out.println("Page opened");
		String actualTitle = driver.getTitle();
		assertEquals(actualTitle, expectedTitle);

		driver.quit();
	}	
}
