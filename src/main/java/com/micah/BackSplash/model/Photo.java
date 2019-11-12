package com.micah.BackSplash.model;

public class Photo {

	private Long id;
	private String hash;
	private String photographerName;
	private String photographerLink;
	private Boolean saved;
	private String photoFilePath;
	
	public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
	}
	public String getHash() {
		return hash;
	}
	public void setHash(String hash) {
		this.hash = hash;
	}
	public String getPhotographerName() {
		return photographerName;
	}
	public void setPhotographerName(String photographerName) {
		this.photographerName = photographerName;
	}
	public String getPhotographerLink() {
		return photographerLink;
	}
	public void setPhotographerLink(String photographerLink) {
		this.photographerLink = photographerLink;
	}
	public Boolean getSaved() {
		return saved;
	}
	public void setSaved(Boolean saved) {
		this.saved = saved;
	}
	public String getPhotoFilePath() {
		return photoFilePath;
	}
	public void setPhotoFilePath(String photoFilePath) {
		this.photoFilePath = photoFilePath;
	}
	
	
	
}
