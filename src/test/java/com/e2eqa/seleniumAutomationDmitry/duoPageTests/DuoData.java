package com.e2eqa.seleniumAutomationDmitry.duoPageTests;

import java.util.List;

import org.openqa.selenium.WebElement;

public class DuoData {
	int[] hashes;
	List<WebElement> btns;
	String[] answers;

	public DuoData(int[] hashes, List<WebElement> btns, String[] answers) {
		super();
		this.hashes = hashes;
		this.btns = btns;
		this.answers = answers;
	}

	public int[] getHashes() {
		return hashes;
	}

	public List<WebElement> getBtns() {
		return btns;
	}

	public String[] getAnswers() {
		return answers;
	}
}