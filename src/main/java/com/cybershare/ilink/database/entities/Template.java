package com.cybershare.ilink.database.entities;

import java.util.List;

import org.bson.types.ObjectId;
import org.mongodb.morphia.annotations.Entity;
import org.mongodb.morphia.annotations.Id;
import org.mongodb.morphia.annotations.Property;

@Entity(value = "template")
public class Template {

	@Id
	private ObjectId id;
	@Property(value = "@type")
	private String type;
	private String text;
	@Property(value = "user_type")
	private List<String> userType;
	@Property(value = "focus_area")
	private List<String> focusArea;	
	private List<String> region;
	private String lang;
	
	public String toString() {
		return text;
	}
	
	public String getText() {
		return text;
	}
	public void setText(String text) {
		this.text = text;
	}
	public List<String> getUserType() {
		return userType;
	}
	public void setUserType(List<String> userType) {
		this.userType = userType;
	}
	public List<String> getFocusArea() {
		return focusArea;
	}

	public void setFocusArea(List<String> focusArea) {
		this.focusArea = focusArea;
	}
	public List<String> getRegion() {
		return region;
	}

	public void setRegion(List<String> region) {
		this.region = region;
	}

	public ObjectId getId() {
		return id;
	}

	public void setId(ObjectId id) {
		this.id = id;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getLang() {
		return lang;
	}
	public void setLang(String lang) {
		this.lang = lang;
	}	
}