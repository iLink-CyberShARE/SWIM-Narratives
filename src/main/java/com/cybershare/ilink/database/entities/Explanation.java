package com.cybershare.ilink.database.entities;

import java.util.List;

import org.bson.types.ObjectId;
import org.mongodb.morphia.annotations.Entity;
import org.mongodb.morphia.annotations.Id;
import org.mongodb.morphia.annotations.Property;

@Entity(value = "explanation")
public class Explanation {

	@Id
	private ObjectId id;
	@Property(value = "@type")
	private String type;
	private String text;
	@Property(value = "user_type")
	private List<String> userType;
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
