package com.e2eqa.autoRepoZiad.pages;

import java.util.List;

import org.apache.logging.log4j.Logger;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

public class IframesPage extends BasePageObject {

	private By iframe = By.xpath("//iframe");

	public IframesPage(WebDriver driver, Logger log) {
		super(driver, log);
	}

	@Override
	protected void openUrl(String url) {
		driver.get(url);
	}

	public void addText(String message) {
		List<WebElement> iframes = findAll(iframe);
		WebElement iframe = iframes.get(0);
		driver.switchTo().frame(iframe);
		driver.findElement(By.xpath("//*[@id='tinymce']")).sendKeys(message);
	}
}
