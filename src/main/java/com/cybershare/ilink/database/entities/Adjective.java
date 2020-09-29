package com.cybershare.ilink.database.entities;

import org.mongodb.morphia.annotations.Entity;
import org.mongodb.morphia.annotations.Property;

@Entity(value = "adjectives")
public class Adjective {

	@Property(value = "@type")
	private String typeAdjective;
	private String type;
	private String value;
	private String lang;
	private String behaviour;

	
	public Adjective() {
		
	}

	public String getBehaviour() {
		return behaviour;
	}



	public void setBehaviour(String behaviour) {
		this.behaviour = behaviour;
	}



	public String getTypeAdjective() {
		return typeAdjective;
	}


	public void setTypeAdjective(String typeAdjective) {
		this.typeAdjective = typeAdjective;
	}


	public String getType() {
		return type;
	}


	public void setType(String type) {
		this.type = type;
	}


	public String getValue() {
		return value;
	}


	public void setValue(String value) {
		this.value = value;
	}


	public String getLang() {
		return lang;
	}


	public void setLang(String lang) {
		this.lang = lang;
	}
	
}
