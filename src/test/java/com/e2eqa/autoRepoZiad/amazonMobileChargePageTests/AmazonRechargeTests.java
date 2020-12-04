package com.e2eqa.autoRepoZiad.amazonMobileChargePageTests;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import com.e2eqa.autoRepoZiad.base.TestUtilities;
import com.e2eqa.autoRepoZiad.duoPageTests.DuoConfig;
import com.e2eqa.autoRepoZiad.pages.AmazonMobileChargePage;

import lombok.extern.log4j.Log4j2;

@Log4j2
public class AmazonRechargeTests extends TestUtilities {

//	private String dataFile = "phonenums.test";
	private String dataFile = "phonenums.p";
	private String mumbaiResultsFile= "mumbaiResults";
	private String otherResultsFile= "otherResults";
	private List<String> phoneNumbers = new ArrayList<String>();
	private WebDriverWait wdWait;

	/**
	 * This method reads the duoConfig.json file to:
	 * process the json and parse it into an
	 * array of DuoConfig objects called duoconfigs
	 * 
	 * duoconfigs is a class member variable
	 */
	private void readDataFile(int num) {
		phoneNumbers.clear();
		String dataFilename = this.dataFile + num + ".txt";
		try {
			Scanner scanner = new Scanner(new File(dataFilename));
			while (scanner.hasNextLine()) {
				String line = scanner.nextLine();
				if(line.trim().isEmpty()) continue;
				phoneNumbers.add(line);
			}
			scanner.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		log.info("Cell numbers size to process::" + phoneNumbers.size() + " from " + dataFilename);
	}
		

	@DataProvider(name="amazonProcessingData")
	public Object[][] getAmazonProcessingData(){
		 return new Object[][] {{3}, {4}, {5}, {6}, {7}, {8}, {9}, {10}, {11}};
	}

	@Test(dataProvider = "amazonProcessingData")
	public void runNumbers(int num) {
		Map<String, String> areaMap = new HashMap<String, String>();
		areaMap.put("VODAFONE_IDEA_PRE", "Vi");
		areaMap.put("AIRTEL_PRE", "Airtel");
		areaMap.put("JIO_PRE", "Jio");

		log.info("Starting");
		wdWait = new WebDriverWait(driver, 10, 500);

		// open main page
		AmazonMobileChargePage amazonPage = new AmazonMobileChargePage(driver, log);
		amazonPage.openPage();

		By btnBy = By.xpath("//*[@id='a-autoid-0-announce']");
		wdWait.until(ExpectedConditions.visibilityOfElementLocated(btnBy));
		wdWait.until(ExpectedConditions.elementToBeClickable(btnBy));
		
		try {
			readDataFile(num);
			processData(num);
		}
		catch(Exception e) {
			log.error("Failed processing at #" + num);
			e.printStackTrace();
		}
	}

	private void processData(int num) {
		String aLine = "";
		String xpath = "//*[@id='operatorAndCircleHiddenInputId']";
		for(String phoneNumber : phoneNumbers) {
			WebElement phoneInput = driver.findElement(By.xpath("//*[@id='mobileNumberTextInputId']"));
			phoneInput.clear();
			phoneInput.sendKeys(phoneNumber);
			sleep(700);
			if(aLine.isEmpty()) {
				aLine = "PhoneNumber,Operator,Circle\n";
				String filename = mumbaiResultsFile + num + ".txt";
				log.info("Initialized for writing results in " + filename);
				writeResults(aLine, filename);
				filename = otherResultsFile + num + ".txt";
				writeResults(aLine, filename);
			}
			else {
				updateResults(aLine, num);
			}
			WebElement circleInput = driver.findElement(By.xpath(xpath));
			String circleInfo = circleInput.getAttribute("value");
			aLine = phoneNumber + "," + circleInfo + "\n";
		}
		updateResults(aLine, num);
	}

	private void updateResults(String aLine, int num) {
		if(aLine.toLowerCase().contains("mumbai")) {
			writeResults(aLine, mumbaiResultsFile + num + ".txt");				
		}
		else {
			writeResults(aLine, otherResultsFile + num + ".txt");
		}
	}

	private void writeResults(String line, String resultsFile) {
		try
		{
		    FileWriter fw = new FileWriter(resultsFile,true); //the true will append the new data
		    fw.write(line);
		    fw.close();
		}
		catch(IOException ioe)
		{
		    System.err.println("IOException: " + ioe.getMessage());
		}		
	}	
}
