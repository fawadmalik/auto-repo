package com.e2eqa.autoRepoZiad.duoPageTests;

import static org.testng.Assert.assertTrue;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import org.apache.commons.io.FileUtils;
import org.openqa.selenium.By;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import com.e2eqa.autoRepoZiad.base.BrowserDriverFactory;
import com.e2eqa.autoRepoZiad.pages.DuoPageObject;
import com.google.gson.Gson;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

import lombok.extern.log4j.Log4j2;

@Log4j2
public class DuoLogic {
	
	protected WebDriver driver;
	private String browser;
	private boolean headless;
	private DuoConfig duoConfig;

	private Map<String, String> answers;
	private String language;
	private String SMART_LANG_FILENAME = "src/main/resources/answer-memory/smartStoryChoices.";
	private final String NAME = "name";
	private final String POSITION = "position";
	private final String SCORE = "score";
	private String  winner = "";
	private int DAYS_LEFT_LB;

	public DuoLogic(String browser, boolean headless, DuoConfig duoConfig) {
		this.browser = browser;
		this.headless = headless;
		this.duoConfig = duoConfig;

		this.language = "";

		createDriver();
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

	public void completeStories() {
		log.info("Starting");

		// open main page
		DuoPageObject duoPage = new DuoPageObject(driver, log);
		duoPage.openPage();

		// find the "have account" button and click on it
		log.info("click have account");
		WebElement haveAcctBtn = findElementByXpath("//a[@data-test='have-account']");
		haveAcctBtn.click();

		// find accountname input element and enter username
		log.info("input user name");
		WebElement usernameElem = findElementByXpath("//input[@data-test='email-input']");
		usernameElem.sendKeys(duoConfig.getUsername());

		// find password input and enter password
		log.info("input password");
		WebElement pwrdElem = findElementByXpath("//input[@data-test='password-input']");
		pwrdElem.sendKeys(duoConfig.getPassword());

		// find and click on the login button
		log.info("click login button");
		WebElement loginBtn = findElementByXpath("//button[@data-test='register-button']");
		loginBtn.click();
		
		this.winner = duoConfig.getWinner();

		boolean quittingTime = false;
		final LocalDateTime startTime = LocalDateTime.now();
		// time limit to calculate if timeIsUp to set quittingTime = true
		final long timeLimit = (long) duoConfig.getSessionLength();
		final ChronoUnit timeUnit = duoConfig.getChronoUnit();
		int pointLimit = duoConfig.getPointLimit();		
		int pointDiffConfig = duoConfig.getPointDiff();
		int pointDiffFin = duoConfig.getPointDiffFin();
		
		List<Map<String, String>> leaderBoard  = getLeaderBoard();
		log.info(DAYS_LEFT_LB);
		leaderBoard.stream().forEach(map -> log.info(map));
		
		Map<String,String> infoLBFirst = leaderBoard.get(0);
		Map<String,String> infoLBSecond = leaderBoard.get(1);
		
		if(infoLBFirst.get(NAME).equals(this.winner)) {
			int myPoints = Integer.parseInt(infoLBFirst.get(SCORE));
			int theirPoints = Integer.parseInt(infoLBSecond.get(SCORE));
			int pointDiffLB = myPoints - theirPoints;

			// you are the winner and now should you be worried that #2 will catch up
			if(pointDiffLB < pointDiffConfig) {
				log.info(String.format("BEF::pointLimit=%s, pointDiffConfig=%s, pointDiffLB=%s", 
						pointLimit, pointDiffConfig, pointDiffLB));
				pointLimit = pointDiffConfig - pointDiffLB;
				log.info(String.format("AFT::pointLimit=%s, pointDiffConfig=%s, pointDiffLB=%s", 
						pointLimit, pointDiffConfig, pointDiffLB));
			}
			else { 
				quittingTime = true;
				log.info("No need to work. You are #1 and distance to #2 = " 
				+ pointDiffLB + " which is >= " + pointDiffConfig);
			}
		}
		else {
			int myPoints = Integer.parseInt(infoLBSecond.get(SCORE));
			int theirPoints = Integer.parseInt(infoLBFirst.get(SCORE));
			int pointDiffLB = theirPoints - myPoints;

			log.info(String.format("BEF::pointLimit=%s, pointDiffConfig=%s, pointDiffLB=%s", 
					pointLimit, pointDiffConfig, pointDiffLB));
			pointLimit = pointDiffLB + pointDiffConfig;
			log.info(String.format("AFT::pointLimit=%s, pointDiffConfig=%s, pointDiffLB=%s", 
					pointLimit, pointDiffConfig, pointDiffLB));
			if(pointLimit <= 0) {
				quittingTime = true;
				log.info(String.format("Already ahead of the pointDiffConfig of %s by %s", pointDiffConfig, pointDiffLB));
			}
		}
		
		// is this the last day of the LB i.e. days=1 or less
		if(DAYS_LEFT_LB == 1) {
			pointLimit += pointDiffFin;
		}
				
		int totalPoints = 0;
		int storyPoints = 0;
		int totalStoriesCompleted = 0;
		List<String> badStories = new ArrayList<String>();

		while (quittingTime != true) {
			try {
				log.info("click stories link");
				By storiesNavLoc = By.xpath("//a[@data-test='stories-nav']");
				WebDriverWait wait15Sec = new WebDriverWait(driver, 15, 1000L);
				try {
					wait15Sec.until(ExpectedConditions.visibilityOfElementLocated(storiesNavLoc));
				} catch (Exception ex) {
					duoPage.openPage();
					wait15Sec.until(ExpectedConditions.visibilityOfElementLocated(storiesNavLoc));
				}
				wait15Sec.until(ExpectedConditions.elementToBeClickable(storiesNavLoc));
				driver.findElements(storiesNavLoc).get(0).click();

				// go to the stories page
				By allStoriesLoc = By.xpath("//a[@data-test='story-container' and starts-with(@href, '/stories')]");
				wait15Sec.until(ExpectedConditions.presenceOfElementLocated(allStoriesLoc));
				wait15Sec.until(ExpectedConditions.elementToBeClickable(allStoriesLoc));
				List<WebElement> allStoryLinkElems = driver.findElements(allStoriesLoc);


				boolean storyFound = false;
				if (allStoryLinkElems.size() != 0) {
					for (WebElement storyLink : allStoryLinkElems) {
						String storyLinkText = storyLink.getText();
						// skip bad stories(0 XP), no storyLinkText
						if (storyLinkText == null || storyLinkText.length() < 1
								|| storyLinkText.contains("+0 XP") == true) {
							continue;
						} else {
							String[] storyLinkTextParts = storyLinkText.split("\\+");
							String storyName = storyLinkTextParts[0].replaceAll("\\n", "");
							String points = storyLinkTextParts[1].replace("XP", "").trim();

							if(badStories.contains(storyName) == true) {
								continue;
							}

							log.info("story: " + storyName + ", points: " + points);
							storyPoints = Integer.parseInt(points);

							log.info("points:" + storyPoints);
							storyFound = true;
							String hostUrl = 
									new String (Base64.getDecoder().decode("aHR0cHM6Ly93d3cuZHVvbGluZ28uY29tLw"));							
							String storyURL = storyLink.getAttribute("href").replace(hostUrl, "");

							log.info("storyURL:" + storyURL);
							duoPage.openPage(storyURL);

							this.language = storyURL.replace("stories/", "").substring(0, 2);
							answers = readAnswers();

							By startStoryLoc = By.xpath("//button[@data-test='story-start']");
							WebDriverWait wdWait = new WebDriverWait(driver, 5L);
							wdWait.until(ExpectedConditions.presenceOfElementLocated(startStoryLoc));
							wdWait.until(ExpectedConditions.elementToBeClickable(startStoryLoc));
							driver.findElements(startStoryLoc).get(0).click();

							int steps = 0;
							boolean storyDone = false;
							while (storyDone == false) {
								boolean isWarning = continueStoryWithCheck();
								if(isWarning) {
									badStories.add(storyName);
									log.info("Skip this bad story next time:" + storyName);
								}
								storyDone = clickStoryDone();
								if(storyDone && !isWarning) {
									totalPoints += storyPoints;
									totalStoriesCompleted++;
								}
								if (++steps > 40) {
									log.info("Story not completed. Quitting because steps > 40");
									storyDone = true;
								}
							}
							write2Smart();
							log.info("totals: points:" + totalPoints + " stories:" + totalStoriesCompleted);
							duoPage.openPage("stories");
							break;
						}
					}
					if (storyFound == false) {
						quittingTime = true;
					}
				}
				if (timeIsUp(startTime, timeLimit, timeUnit)) {
					quittingTime = true;
					log.info("Time's up. Limit of " + timeLimit + " " + duoConfig.getSessionTimeUnit()
							+ " reached. Quitting...");
					log.info("Total pionts: " + totalPoints);
					log.info("Total StoriesCompleted: " + totalStoriesCompleted);
				} else if (pointLimitIsUp(pointLimit, totalPoints)) {
					quittingTime = true;
					log.info("Points up. Limit of " + pointLimit + "XP" + " reached. Quitting...");
					log.info("Total pionts: " + totalPoints);
					log.info("Total StoriesCompleted: " + totalStoriesCompleted);

				}

			} catch (Exception e) {
				Date now = new Date();
				String filename = "screenCaps/img_" + Long.toString(now.getTime()) + ".png";
				takeSnapshot(filename);
			}
		}
//		log.info("Leaderboard at quitting time");
//		getLeaderBoard().stream().forEach(map -> log.info(map));
//		
//		driver.manage().timeouts().implicitlyWait(10, TimeUnit.SECONDS);
	}

	/**
	 * if total points is >= point limit then point limit is up and
	 * 
	 * @param pointLimit
	 * @param totalPoints
	 * @return returns true if point limit is up other wise false if point limit is
	 *         not up
	 */
	private boolean pointLimitIsUp(int pointLimit, int totalPoints) {
		return totalPoints >= pointLimit;
	}

	private WebElement findElementByXpath(String xpath) {
		WebDriverWait wait15Sec = new WebDriverWait(driver, 15, 500L);
		wait15Sec.until(ExpectedConditions.presenceOfElementLocated(By.xpath(xpath)));
		wait15Sec.until(ExpectedConditions.elementToBeClickable(By.xpath(xpath)));

		return driver.findElements(By.xpath(xpath)).get(0);
	}

	private Map<String, String> readAnswers() {
		answers = new HashMap<String, String>();
		Gson gson = new Gson();
		String langFile = SMART_LANG_FILENAME + language + ".json";
		try {
			JsonReader jsonReader = new JsonReader(new FileReader(langFile));
			// Type type = new TypeToken<HashMap<Integer, Employee>>(){}.getType();
			answers = gson.fromJson(jsonReader, Map.class);
			jsonReader.close();
			log.info("Reading answers from " + langFile);
		} catch (FileNotFoundException e1) {
			e1.printStackTrace();
			log.error("failed to read from " + langFile + "\nCreating lang file! " + langFile);
			answers.put("keyz", "valuez");
			write2Smart();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return answers;
	}

	private boolean continueStoryWithCheck() {
		boolean isWarning = false;
		By contBtnLoc = By.xpath("//button[@data-test='stories-player-continue']");
		try {
			List<WebElement> storyCompleteWarnElems =
					driver.findElements(By.xpath("//h2[contains(.,'Try a new story to earn XP!')]"));
			isWarning = storyCompleteWarnElems.size() > 0;

			WebElement contBtn = driver.findElements(contBtnLoc).get(0);

			int stage = 0;
			while (contBtn.getAttribute("disabled") != null) {
				boolean blocked = detectStage(stage);

				if (blocked) {
					switch (stage) {
					case 0:
						clickStoryChoices();
						stage = 0;
						break;
					case 1:
						clickPhrases();
						stage = 0;
						break;
					case 2:
						clickSelectablePhrases();
						stage = 0;
						break;
					case 3:
						clickPairs();
						stage = 0;
						break;

					default:
						log.warn("No intermediate stages found; Continue Story.");
						break;
					}
				} else {
					if (stage > 3) {
						stage = 0;
					} else {
						stage++;
					}
				}
				contBtn = driver.findElements(contBtnLoc).get(0);
			}
			contBtn.click();
		} catch (Exception e) {
			System.out.println(e.getMessage());
		}
		return isWarning;
	}

	private boolean clickStoryDone() {
		By doneBtnLoc = By.xpath("//button[@data-test='stories-player-done']");
		List<WebElement> elems = driver.findElements(doneBtnLoc);
		if(elems.size() == 0) { return false; }
		else {
			elems.get(0).click();
			return true;
		}
	}

	private void write2Smart() {
		Gson gson = new Gson();
		String langFile = SMART_LANG_FILENAME + language + ".json";
		try {
			JsonWriter jsonWriter = new JsonWriter(new FileWriter(langFile, false));

			gson.toJson(answers, Map.class, jsonWriter);
			jsonWriter.close();
			log.info("Writing to " + langFile);
		} catch (IOException e) {
			log.error(e);
		}
	}

	private boolean timeIsUp(LocalDateTime startTime, long timeLimit, ChronoUnit chronoUnit) {
		long span = startTime.until(LocalDateTime.now(), chronoUnit);
		return span >= timeLimit;
	}

	private void takeSnapshot(String filename) {
		TakesScreenshot scrShot = ((TakesScreenshot) driver);
		// Call getScreenshotAs method to create image file
		File SrcFile = scrShot.getScreenshotAs(OutputType.FILE);
		// Move image file to new destination
		File DestFile = new File(filename);
		// Copy file at destination
		try {
			FileUtils.copyFile(SrcFile, DestFile);
		} catch (IOException e) {
			System.out.println("couldn't save screen shot from FileUtils copyFile");
			e.printStackTrace();
		}
	}

	private boolean detectStage(int stage) {
		boolean blocked = false;
		switch (stage) {
		case 0:
			blocked = isStoryChoicesPresent();
			break;
		case 1:
			blocked = isPhrasesPresent();
			break;
		case 2:
			blocked = isSelectablePhrasesPresent();
			break;
		case 3:
			blocked = isPairsPresent();
			break;
		default:
			break;
		}
		return blocked;
	}

	private void clickStoryChoices() {
		String stChUlXpath = "//button[@data-test='stories-choice']/parent::li/parent::ul";
		List<WebElement> prec_sib = driver.findElements(By.xpath(stChUlXpath + "/preceding-sibling::div"));
		String question = "";
		String answer = "";
		if (prec_sib.size() > 0) {
			question = driver.findElements(By.xpath(stChUlXpath + "/preceding-sibling::div[1]")).get(0).getText();
			if (answers.containsKey(question)) {
				answer = answers.get(question);
				log.info("Smart use:..[Q]" + question + " ..[A]" + answer);
				if (clickStChWithAns(stChUlXpath, answer) == true) {
					return;
				}
			}
		} else {
			String sentence = driver
					.findElements(By.xpath(stChUlXpath + "/parent::div/parent::div/preceding-sibling::div[1]")).get(0)
					.getText();

			List<WebElement> liElems = driver.findElements(By.xpath(stChUlXpath + "/li"));
			int liCount = liElems.size();

			String fillerAnswer = "";
			String message = "";
			for (int i = 0; i < liCount; i++) {
				String liThisFiller = liElems.get(i).getText();
				message += liThisFiller + ",";
				if (sentence.contains(liThisFiller)) {
					driver.findElements(By.xpath("//button[@data-test='stories-choice']")).get(i).click();
					fillerAnswer = liThisFiller;
				}
			}
			if (fillerAnswer != "") {
				return;
			} else {
				String msg = String.format("Smart fail..[Q]%s..[Answers]%s", sentence, message);
				log.error(msg);
			}
		}
		answer = getStChAnswer(stChUlXpath);
		if (!question.equals("")) {
			if (answer != "") {
				answers.put(question, answer);
			}
		}
	}

	private boolean clickStChWithAns(String stChUlXpath, String answer) {
		List<WebElement> liElems = driver.findElements(By.xpath(stChUlXpath + "/li"));
		if (liElems.get(0).getText().equals(answer)) {
			driver.findElements(By.xpath("//button[@data-test='stories-choice']")).get(0).click();
			return true;
		} else if (liElems.get(1).getText().equals(answer)) {
			driver.findElements(By.xpath("//button[@data-test='stories-choice']")).get(1).click();
			return true;
		} else {
			log.error("Couldn't use the given answer");
			return false;
		}
	}

	private String getStChAnswer(String stChUlXpath) {
		List<WebElement> liElems = driver.findElements(By.xpath(stChUlXpath + "/li"));
		if (liElems.size() == 2) {
			String answer;
			DuoData classBef = getClassHashes(stChUlXpath);
			classBef.getBtns().get(0).click();
			DuoData classAft = getClassHashes(stChUlXpath);

			if (classBef.getHashes()[0] != classAft.getHashes()[0]
					&& classBef.getHashes()[1] != classAft.getHashes()[1]) {
				answer = classBef.getAnswers()[0];
			} else {
				answer = classBef.getAnswers()[1];
				classAft.getBtns().get(1).click();
			}
			return answer;
		} else if (liElems.size() == 3) {
			String answer;
			DuoData classBef = getClassHashesToo(stChUlXpath);
			classBef.getBtns().get(0).click();
			DuoData classAft = getClassHashesToo(stChUlXpath);

			if (classBef.getHashes()[0] != classAft.getHashes()[0] && classBef.getHashes()[1] != classAft.getHashes()[1]
					&& classBef.getHashes()[2] != classAft.getHashes()[2]) {
				answer = classBef.getAnswers()[0];
			} else {
				classBef = getClassHashesToo(stChUlXpath);
				classBef.getBtns().get(1).click();
				classAft = getClassHashesToo(stChUlXpath);

				if (classBef.getHashes()[0] != classAft.getHashes()[0]
						&& classBef.getHashes()[1] != classAft.getHashes()[1]
						&& classBef.getHashes()[2] != classAft.getHashes()[2]) {
					answer = classBef.getAnswers()[1];
				} else {
					answer = classBef.getAnswers()[2];
					classAft.getBtns().get(2).click();
				}
			}
			return answer;
		} else {
			log.error("found " + liElems.size() + " and this is unexpected. should be 2 or 3");
		}
		return "";
	}

	private DuoData getClassHashes(String stChUlXpath) {
		List<WebElement> ulElems = driver.findElements(By.xpath(stChUlXpath));
		List<WebElement> liElms = ulElems.get(0).findElements(By.xpath("//li"));
		List<WebElement> btns = ulElems.get(0).findElements(By.xpath("//button"));

		int class0 = (liElms.get(0).getAttribute("class") + btns.get(0).getAttribute("class")).hashCode();
		int class1 = (liElms.get(1).getAttribute("class") + btns.get(1).getAttribute("class")).hashCode();

		String[] answers = new String[] { liElms.get(0).getText(), liElms.get(1).getText() };
		return new DuoData(new int[] { class0, class1 }, btns, answers);
	}

	private DuoData getClassHashesToo(String stChUlXpath) {
		List<WebElement> ulElems = driver.findElements(By.xpath(stChUlXpath));
		List<WebElement> liElms = ulElems.get(0).findElements(By.xpath("//li"));
		List<WebElement> btns = ulElems.get(0).findElements(By.xpath("//button"));

		int class0 = (liElms.get(0).getAttribute("class") + btns.get(0).getAttribute("class")).hashCode();
		int class1 = (liElms.get(1).getAttribute("class") + btns.get(1).getAttribute("class")).hashCode();
		int class2 = (liElms.get(2).getAttribute("class") + btns.get(2).getAttribute("class")).hashCode();

		String[] answers = new String[] { liElms.get(0).getText(), liElms.get(1).getText(), liElms.get(2).getText() };
		return new DuoData(new int[] { class0, class1, class2 }, btns, answers);
	}

	private void clickPhrases() {
		By locator = By.xpath("//div[@class='_2l5CB']//span");
		List<WebElement> phrases = driver.findElements(locator);
		Actions actions = new Actions(driver);
		if (phrases.size() > 0) {
			for (int i = 0; i < phrases.size(); i++) {
				for (WebElement phrase : phrases) {
					actions.moveToElement(phrase).click().build().perform();
					driver.manage().timeouts().implicitlyWait(2, TimeUnit.SECONDS);
				}
			}
		}
	}

	private void clickSelectablePhrases() {
		By locator = By.xpath("//div[@data-test='stories-selectable-phrase']//span");
		List<WebElement> selectablePhrases = driver.findElements(locator);
		if (selectablePhrases.size() > 0) {
			for (WebElement selectablePhrase : selectablePhrases) {
				selectablePhrase.click();
				driver.manage().timeouts().implicitlyWait(1, TimeUnit.SECONDS);
			}
		}
	}

	private void clickPairs() {
		By locator = By.xpath("//button[@data-test='stories-token']");
		List<WebElement> pairBtns = driver.findElements(locator);
		int buttonCount = pairBtns.size();
		if (buttonCount > 0) {
			for (int i = 0; i < buttonCount - 1; i++) {
				WebElement btn1 = pairBtns.get(i);
				String btn1Class = btn1.getAttribute("class");
				if (btn1Class.contains("_1CGKV")) {
					continue;
				}

				for (int j = i + 1; j < buttonCount; j++) {
					btn1.click();

					WebElement btn2 = pairBtns.get(j);
					String btn2Class = btn2.getAttribute("class");
					if (btn2Class.contains("_1CGKV")) {
						btn1.click();
						continue;
					}
					btn2.click();
					btn2 = pairBtns.get(j);
					btn2Class = btn2.getAttribute("class");
					if (btn2Class.contains("_1CGKV")) {
						break;
					}
				}
			}
		}
	}

	private boolean isStoryChoicesPresent() {
		return driver.findElements(By.xpath("//button[@data-test='stories-choice']")).size() > 0;
	}

	private boolean isPhrasesPresent() {
		return driver.findElements(By.xpath("//div[@class='_2l5CB']//span")).size() > 0;
	}

	private boolean isSelectablePhrasesPresent() {
		return driver.findElements(By.xpath("//div[@data-test='stories-selectable-phrase']//span")).size() > 0;
	}

	private boolean isPairsPresent() {
		return driver.findElements(By.xpath("//button[@data-test='stories-token']")).size() > 0;
	}

	private List<Map<String, String>> getLeaderBoard() {
		List<Map<String, String>> leaderBoard = new ArrayList<Map<String,String>>();
		String xpathLB = "//h2[contains(.,'Diamond League')]";
		new WebDriverWait(driver, 20).until(
				ExpectedConditions.visibilityOfElementLocated(
						By.xpath(xpathLB)));
		//$x("//h2[contains(.,'Diamond League')]/parent::div/following-sibling::div/span")[0].innerText
		String xpathLBDaysLeft = xpathLB + "/parent::div/following-sibling::div/span";
		String timeLeftLB = driver.findElement(By.xpath(xpathLBDaysLeft)).getText()
				.replace("D", "")
				.replace("H", "")
				.replace("M", "");
		String[] partsLB = timeLeftLB.split(" ");
		DAYS_LEFT_LB = Integer.parseInt(partsLB[0].replace("D", ""));
		
		String xpathAll = "//h2[contains(.,'Diamond League')]/parent::div/parent::div/following-sibling::div[contains(@class,'_2Rsru')]/div/*";
		List<WebElement> elems = driver.findElements(By.xpath(xpathAll));
		
		assertTrue(elems.size() > 0, "Leaderboard entries not found in getLeaderBoard");
		
		WebElement firstPositionElem = elems.get(0);
		
		WebElement secondPositionElem = null;
		if(firstPositionElem.getText().contains(this.winner)) {
			secondPositionElem = elems.get(1);
		}
		else {
			List<WebElement> myElems = elems
					.stream()
					.filter(elem -> elem.getText().contains(this.winner))
					.collect(Collectors.toList());
			assertTrue(myElems.size() > 0, String.format("Missing from %d leaderboard entries; %s", myElems.size(), this.winner));
			secondPositionElem = myElems.get(0);
		}
		
		String[] firstTextElems = firstPositionElem.getText().split("\\n");
		String[] secondTextElems = secondPositionElem.getText().split("\\n");
		
		log.info("firstText: " + Arrays.toString(firstTextElems));
		log.info("secondText: " + Arrays.toString(secondTextElems));
		
		Map<String, String> infoLBFirst = new HashMap<String, String>();
		Map<String, String> infoLBSecond = new HashMap<String, String>();
		
		infoLBFirst.put(POSITION, firstTextElems[0]);
		if(firstTextElems.length == 3) {
			infoLBFirst.put(NAME, firstTextElems[1]);
			infoLBFirst.put(SCORE, firstTextElems[2].replace(" XP", ""));
		}
		else {
			infoLBFirst.put(NAME, firstTextElems[2]);
			infoLBFirst.put(SCORE, firstTextElems[3].replace(" XP", ""));
		}				
		leaderBoard.add(infoLBFirst);
		
		infoLBSecond.put(POSITION, secondTextElems[0]);
		if(secondTextElems.length == 3) {
			infoLBSecond.put(NAME, secondTextElems[1]);
			infoLBSecond.put(SCORE, secondTextElems[2].replace(" XP", ""));
		}
		else {
			infoLBSecond.put(NAME, secondTextElems[2]);
			infoLBSecond.put(SCORE, secondTextElems[3].replace(" XP", ""));
		}
		leaderBoard.add(infoLBSecond);

		return leaderBoard;
	}
}
