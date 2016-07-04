package org.spring.boot.mybatis.rw.sample;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "data")
public class DataProperties {
	
	private String writeUrl;
	private String writeDriverClassName;
	private String writeUsername;
	private String writePassword;
	
	private String readUrl;
	private String readDriverClassName;
	private String readUsername;
	private String readPassword;
	public String getWriteUrl() {
		return writeUrl;
	}
	public void setWriteUrl(String writeUrl) {
		this.writeUrl = writeUrl;
	}
	public String getWriteDriverClassName() {
		return writeDriverClassName;
	}
	public void setWriteDriverClassName(String writeDriverClassName) {
		this.writeDriverClassName = writeDriverClassName;
	}
	public String getWriteUsername() {
		return writeUsername;
	}
	public void setWriteUsername(String writeUsername) {
		this.writeUsername = writeUsername;
	}
	public String getWritePassword() {
		return writePassword;
	}
	public void setWritePassword(String writePassword) {
		this.writePassword = writePassword;
	}
	public String getReadUrl() {
		return readUrl;
	}
	public void setReadUrl(String readUrl) {
		this.readUrl = readUrl;
	}
	public String getReadDriverClassName() {
		return readDriverClassName;
	}
	public void setReadDriverClassName(String readDriverClassName) {
		this.readDriverClassName = readDriverClassName;
	}
	public String getReadUsername() {
		return readUsername;
	}
	public void setReadUsername(String readUsername) {
		this.readUsername = readUsername;
	}
	public String getReadPassword() {
		return readPassword;
	}
	public void setReadPassword(String readPassword) {
		this.readPassword = readPassword;
	}
	

}
