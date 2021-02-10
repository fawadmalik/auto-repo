package com.e2eqa.autoRepoZiad.duoPageTests;

import java.time.temporal.ChronoUnit;
import java.util.Base64;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter @Setter @NoArgsConstructor @ToString
public class DuoConfig {

	private String username;
	private String password;
	private int sessionLength;
	private String sessionTimeUnit;
	private int pointLimit;
	private int pointDiff;
	private int pointDiffFin;
	private String winner;
	
	private ChronoUnit chronoUnit;
	
	public DuoConfig(String username, String password, int sessionLength, String sessionTimeUnit, int pointLimit, int pointDiff, int pointDiffFin, String winner) {
		this.username = new String (Base64.getDecoder().decode(username));
		this.password = new String (Base64.getDecoder().decode(password));
		this.sessionLength = sessionLength;
		this.sessionTimeUnit = sessionTimeUnit;
		this.pointLimit = pointLimit;
		this.pointDiff = pointDiff;
		this.pointDiffFin = pointDiffFin;
		this.winner = new String (Base64.getDecoder().decode(winner));
		
		setChronoUnit(this.sessionTimeUnit);
	}

	private void setChronoUnit(String timeUnit) {
		this.chronoUnit = ChronoUnit.HOURS;
		
		switch (timeUnit) {
		case "HOURS":
			this.chronoUnit = ChronoUnit.HOURS;
			break;
		case "MINUTES":
			this.chronoUnit = ChronoUnit.MINUTES;
			break;
		case "SECONDS":
			this.chronoUnit = ChronoUnit.SECONDS;
			break;
		case "DAYS":
			this.chronoUnit = ChronoUnit.DAYS;
			break;

		default:
			System.out.println(
					String.format("input time unit of %s unrecognized. Defaulting to HOURS", timeUnit));
			break;
		}		
	}
}
