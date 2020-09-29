package com.cybershare.ilink;

import java.util.HashMap;
import java.util.Map;
import java.util.NavigableMap;

public class Variable {
	private String mBehavior;
	private boolean mTimeSeries = false;
	private int mMinYear, mMaxYear;
	private float mMin, mMax, mAverage;
	private NavigableMap<String, Float> mValues;
	private boolean trendIncreasing = false;
	private String trend;
	private float constantValue;
	private int constantValueYear;
	private String label;
	private String description;
	private String explanation;
	private String adjectiveTrend;
	private String adjectiveBehaviour;
	private String region;
	private String elementUnit;
	private Double baseLineValue;

	private Map<String,String> data;
	
	private String getAsString(Object value) {
		return String.valueOf(value);
	}

	public Variable(float min, int minYear, float max, int maxYear, 
			float average, NavigableMap<String, Float> varValue, 
			boolean timeSeries, float trend, float previousValue, 
			int previousValueYear, String label, String description,
			String explanation, boolean isTrendIncreasing,
			String adjectiveTrend, String adjectiveBehaviour,
			String region, String elementUnit, double baseLineValue) {
		
		setmValues(varValue);
		setmMax(max);
		setmMaxYear(maxYear);
		setmMin(min);
		setmMinYear(minYear);
		setmAverage(average);
		setmTimeSeries(timeSeries);
		setTrendIncreasing(isTrendIncreasing);
		setTrend(trend);
		setConstantValue(previousValue);
		setConstantValueYear(previousValueYear);
		setLabel(label);
		setDescription(description);
		setExplanation(explanation);
		setAdjectiveTrend(adjectiveTrend);
		setAdjectiveBehaviour(adjectiveBehaviour);
		setRegion(region);
		setElementUnit(elementUnit);
		setBaseLineValue(baseLineValue);
		
		data = new HashMap<>();
		data.put("~historical_avg", getAsString(getBaseLineValue()));
		data.put("~average", getAsString(getmAverage()));
		data.put("~element_unit", getAsString(getElementUnit()));
		data.put("~constant_year", getAsString(getConstantValueYear()));
		data.put("~constant_value", getAsString(getConstantValue()));
		data.put("~region", getAsString(getRegion()));
		data.put("~adjective_trend",getAsString(getAdjectiveTrend()));
		data.put("~adjective_behaviour",getAsString(getAdjectiveBehaviour()));
		data.put("~explanation",getAsString(getExplanation()));
		data.put("~element_label",getAsString(getLabel()));
		data.put("~element_description",getAsString(getDescription()));
		data.put("~percent",getAsString(getTrend()));
		data.put("~maxValue",getAsString(getmMax()));
		data.put("~minValue",getAsString(getmMin()));
		data.put("~minYear",getAsString(getmMinYear()));
		data.put("~maxYear",getAsString(getmMaxYear()));
	}
	
	public Double getBaseLineValue() {
		return baseLineValue;
	}

	public void setBaseLineValue(Double baseLineValue) {
		this.baseLineValue = baseLineValue;
	}

	public String getElementUnit() {
		return elementUnit;
	}

	public void setElementUnit(String elementUnit) {
		this.elementUnit = elementUnit;
	}

	public String getRegion() {
		return region;
	}

	public void setRegion(String region) {
		this.region = region;
	}

	public String getAdjectiveTrend() {
		return adjectiveTrend;
	}

	public void setAdjectiveTrend(String adjectiveTrend) {
		this.adjectiveTrend = adjectiveTrend;
	}

	public String getAdjectiveBehaviour() {
		return adjectiveBehaviour;
	}

	public void setAdjectiveBehaviour(String adjectiveBehaviour) {
		this.adjectiveBehaviour = adjectiveBehaviour;
	}

	public String getExplanation() {
		return explanation;
	}

	public void setExplanation(String explanation) {
		this.explanation = explanation;
	}

	public String getLabel() {
		return label;
	}

	public void setLabel(String label) {
		this.label = label;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}
	
	public Map<String, String> getData() {
		return data;
	}

	public void setData(Map<String, String> data) {
		this.data = data;
	}

	public boolean isTrendIncreasing() {
		return trendIncreasing;
	}
	public void setTrendIncreasing(boolean trendIncreasing) {
		this.trendIncreasing = trendIncreasing;
	}
	public String getTrend() {
		return trend;
	}
	public void setTrend(float trend) {
		this.trend = String.valueOf(trend).concat("%");
	}
	public String getmBehavior() {
		return mBehavior;
	}
	public void setmBehavior(String mBehavior) {
		this.mBehavior = mBehavior;
	}
	public boolean ismTimeSeries() {
		return mTimeSeries;
	}
	public void setmTimeSeries(boolean mTimeSeries) {
		this.mTimeSeries = mTimeSeries;
	}
	public int getmMinYear() {
		return mMinYear;
	}

	public void setmMinYear(int mMinYear) {
		this.mMinYear = mMinYear;
	}

	public int getmMaxYear() {
		return mMaxYear;
	}

	public void setmMaxYear(int mMaxYear) {
		this.mMaxYear = mMaxYear;
	}

	public float getmMin() {
		return mMin;
	}
	public void setmMin(float mMin) {
		this.mMin = mMin;
	}
	public float getmMax() {
		return mMax;
	}
	public void setmMax(float mMax) {
		this.mMax = mMax;
	}
	public float getmAverage() {
		return mAverage;
	}
	public void setmAverage(float mAverage) {
		this.mAverage = mAverage;
	}

	public NavigableMap<String, Float> getmValues() {
		return mValues;
	}

	public void setmValues(NavigableMap<String, Float> mValues) {
		this.mValues = mValues;
	}

	public float getConstantValue() {
		return constantValue;
	}

	public void setConstantValue(float constantValue) {
		this.constantValue = constantValue;
	}

	public int getConstantValueYear() {
		return constantValueYear;
	}

	public void setConstantValueYear(int constantValueYear) {
		this.constantValueYear = constantValueYear;
	}

	public void setTrend(String trend) {
		this.trend = trend;
	}
}
