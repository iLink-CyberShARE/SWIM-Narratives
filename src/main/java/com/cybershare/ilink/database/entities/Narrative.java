package com.cybershare.ilink.database.entities;

import java.util.List;

import org.bson.types.ObjectId;
import org.mongodb.morphia.annotations.Entity;
import org.mongodb.morphia.annotations.Id;
import org.mongodb.morphia.annotations.Property;

@Entity(value = "narrative")
public class Narrative {

		@Id
		private ObjectId id;
		@Property(value = "@context")
		private String context;
		@Property(value = "@type")
		private String type;
		private Element element;
		private List<Template> templates;
		private List<Explanation> explanations;
		private List<Adjective> adjectives;
		
		public boolean isTemplatesEmpty() {
			if(templates == null ||
					templates.isEmpty())
				return true;
			return false;
		}
		
		public boolean isExplanationsEmpty() {
			if(explanations == null ||
					explanations.isEmpty())
				return true;
			return false;
		}
		
		public boolean isAdjecivesEmpty() {
			if(adjectives == null ||
					adjectives.isEmpty())
				return true;
			return false;
		}
		
		public ObjectId getId() {
			return id;
		}
		public void setId(ObjectId id) {
			this.id = id;
		}
		public String getContext() {
			return context;
		}
		public void setContext(String context) {
			this.context = context;
		}
		public String getType() {
			return type;
		}
		public void setType(String type) {
			this.type = type;
		}
		public Element getElement() {
			return element;
		}
		public void setElement(Element element) {
			this.element = element;
		}
		public List<Template> getTemplates() {
			return templates;
		}
		public void setTemplates(List<Template> templates) {
			this.templates = templates;
		}
		public List<Explanation> getExplanations() {
			return explanations;
		}
		public void setExplanations(List<Explanation> explanations) {
			this.explanations = explanations;
		}
		public List<Adjective> getAdjectives() {
			return adjectives;
		}
		public void setAdjectives(List<Adjective> adjectives) {
			this.adjectives = adjectives;
		}
}
