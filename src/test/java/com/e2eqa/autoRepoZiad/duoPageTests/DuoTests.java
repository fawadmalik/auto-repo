package com.e2eqa.autoRepoZiad.duoPageTests;

import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import com.e2eqa.autoRepoZiad.base.TestUtilities;

import lombok.extern.log4j.Log4j2;

@Log4j2
public class DuoTests extends TestUtilities {

	private String DUO_CONFIG_FILE = "duoConfig.json";
	private DuoConfig[] duoconfigs;

	/**
	 * This method reads the duoConfig.json file to:
	 * process the json and parse it into an
	 * array of DuoConfig objects called duoconfigs
	 * 
	 * duoconfigs is a class member variable
	 */
	private void readDuoConfigFile() {
		JSONParser parser = new JSONParser();
		try {
			Reader reader = new FileReader(DUO_CONFIG_FILE);
			JSONArray jsonArray = (JSONArray) parser.parse(reader);

			List<DuoConfig> duoConfigList = new ArrayList<DuoConfig>();

			@SuppressWarnings("unchecked")
			Iterator<JSONObject> iterator = jsonArray.iterator();
			while (iterator.hasNext()) {
				JSONObject jsonObject = iterator.next();

				log.info(jsonObject);

				String timeUnit = (String) jsonObject.get("sessionTimeUnit");
				int length;	
				int pointLimit = 0;
				int pointDiff = 0;
				int pointDiffFin = 0;
				try {
					length = Integer.parseInt((String) jsonObject.get("sessionLength"));
					pointLimit = Integer.parseInt((String) jsonObject.get("pointLimit")); 
					pointDiff = Integer.parseInt((String) jsonObject.get("pointDiff"));
					pointDiffFin = Integer.parseInt((String) jsonObject.get("pointDiffFin"));
				} catch (NumberFormatException e) {
					length = 1;
					log.info(("Bad input for session length. Looking for whole number and found "
							+ jsonObject.get("length") + "\nDefaulting to length=1 "
							+ timeUnit));
				}
				duoConfigList.add(new DuoConfig(
					(String) jsonObject.get("username"),
					(String) jsonObject.get("password"),
					length,
					timeUnit,
					 pointLimit,
					 pointDiff,
					 pointDiffFin,
					 (String) jsonObject.get("winner"))
				);
			}
			duoconfigs = duoConfigList.toArray(new DuoConfig[0]);
		} catch (IOException | ParseException e) {
			e.printStackTrace();
		}
	}
	
	@BeforeTest
	public void testSetup() {
		readDuoConfigFile();
	}

	@DataProvider(name="duoConfigData", parallel = true)
	public Object[][] getDuoConfigData(){
		int index = 0;
		Object[][] data = new Object[duoconfigs.length][7];
		for(DuoConfig duoConfig : duoconfigs) {
			Object[] dataCols = new Object[] { duoConfig };
			data[index] = dataCols;
			index++;
		}
		return data;
	}

	@Test(dataProvider = "duoConfigData")
	public void completeStoriesTest(final DuoConfig duoConfig) {
		DuoLogic duoLogic = new DuoLogic(browser, headless, duoConfig);
		duoLogic.completeStories();
		duoLogic.tearDown();
	}
}
