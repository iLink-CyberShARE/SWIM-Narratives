package com.cybershare.ilink.database.entities;

import org.bson.types.ObjectId;
import org.mongodb.morphia.annotations.Entity;
import org.mongodb.morphia.annotations.Id;
import org.mongodb.morphia.annotations.Property;

@Entity(value = "element")
public class Element {

	@Id
	private ObjectId id;
	@Property(value = "@type")
	private String typeElement;
	private String name;
	private String type;
	
	public ObjectId getId() {
		return id;
	}
	public void setId(ObjectId id) {
		this.id = id;
	}
	public String getTypeElement() {
		return typeElement;
	}
	public void setTypeElement(String typeElement) {
		this.typeElement = typeElement;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getType() {
		return type;
	}
	public void setType(String type) {
		this.type = type;
	}
	
}
