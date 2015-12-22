package com.example.service;

public class UpgradeService {
	private String currentVersion;

	public String getCurrentVersion() {
		return currentVersion;
	}

	public void setCurrentVersion(String currentVersion) {
		this.currentVersion = currentVersion;
	}

	public boolean checkUpgrade(String version) {
		if (version.compareToIgnoreCase(currentVersion) < 0) {
			return true;
		}
		return false;
	}

}
