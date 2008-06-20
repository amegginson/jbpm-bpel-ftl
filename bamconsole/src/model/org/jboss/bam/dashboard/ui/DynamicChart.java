package org.jboss.bam.dashboard.ui;

import java.util.ArrayList;
import java.util.List;

public class DynamicChart {
	
	private List<String> categories = new ArrayList<String>();
	private List<Data> data = new ArrayList<Data>();
	
	private boolean borderVisible = true;
	
	private boolean is3d = false;
	private boolean legend = true;
	
	private String title = "";
	private String titlePaint;
	private String titleBackgroundPaint;
	
	private String domainAxisLabel = "";
	private String domainAxisPaint;
	private boolean domainGridlinesVisible = false;
	private String domainGridlinePaint;
	private String domainGridlineStroke;
	
	private String rangeAxisLabel = "";
	private String rangeAxisPaint;
	private boolean rangeGridlinesVisible = true;
	private String rangeGridlinePaint;
	private String rangeGridlineStroke;
	
	private String orientation = "vertical";
	private String plotBackgroundPaint = "white";
	private String plotOutlinePaint = "black";
	private String plotOutlineStroke = "solid-thin";
	private String borderPaint = "black";
	private String borderBackgroundPaint = "white";
	private String borderStroke = "solid-thick";
	
	private String legendBackgroundPaint;
	private String legendItemPaint;
	
	private Float plotBackgroundAlpha = .3f;
	private Float plotForegroundAlpha = 1f;
	
	private float height;
	private float width;
	
	private String description;
	private String chartType;
	
	public boolean isBorderVisible() {
		return borderVisible;
	}
	
	public void setBorderVisible(boolean borderVisible) {
		this.borderVisible = borderVisible;
	}
	
	public boolean getIs3d() {
		return is3d;
	}
	
	public void setIs3d(boolean is3d) {
		this.is3d = is3d;
	}
	
	public boolean isLegend() {
		return legend;
	}
	
	public void setLegend(boolean legend) {
		this.legend = legend;
	}
	
	public String getDomainAxisLabel() {
		return domainAxisLabel;
	}
	
	public void setDomainAxisLabel(String categoryAxisLabel) {
		this.domainAxisLabel = categoryAxisLabel;
	}
	
	public String getRangeAxisLabel() {
		return rangeAxisLabel;
	}
	
	public void setRangeAxisLabel(String valueAxisLabel) {
		this.rangeAxisLabel = valueAxisLabel;
	}
	
	public float getHeight() {
		return height;
	}
	
	public void setHeight(float height) {
		this.height = height;
	}
	
	public float getWidth() {
		return width;
	}
	
	public void setWidth(float width) {
		this.width = width;
	}
	
	public String getTitle() {
		return this.title;
	}
	
	public void setTitle(String title) {
		this.title = title;
	}
	
	public String getOrientation() {
		return this.orientation;
	}
	
	public void setOrientation(String orientation) {
		this.orientation = orientation;
	}
	
	public String getBorderBackgroundPaint() {
		return this.borderBackgroundPaint;
	}
	
	public void setBorderBackgroundPaint(String borderBackgroundPaint) {
		this.borderBackgroundPaint = borderBackgroundPaint;
	}
	
	public String getBorderPaint() {
		return this.borderPaint;
	}
	
	public void setBorderPaint(String borderPaint) {
		this.borderPaint = borderPaint;
	}
	
	public String getPlotBackgroundPaint() {
		return this.plotBackgroundPaint;
	}
	
	public void setPlotBackgroundPaint(String plotBackgroundPaint) {
		this.plotBackgroundPaint = plotBackgroundPaint;
	}
	
	public String getPlotOutlinePaint() {
		return this.plotOutlinePaint;
	}
	
	public void setPlotOutlinePaint(String plotOutlinePaint) {
		this.plotOutlinePaint = plotOutlinePaint;
	}
	
	public String getBorderStroke() {
		return borderStroke;
	}
	
	public void setBorderStroke(String borderStroke) {
		this.borderStroke = borderStroke;
	}
	
	public String getPlotOutlineStroke() {
		return plotOutlineStroke;
	}
	
	public void setPlotOutlineStroke(String plotOutlineStroke) {
		this.plotOutlineStroke = plotOutlineStroke;
	}
	
	public Float getPlotBackgroundAlpha() {
		return plotBackgroundAlpha;
	}
	
	public void setPlotBackgroundAlpha(Float plotBackgroundAlpha) {
		this.plotBackgroundAlpha = plotBackgroundAlpha;
	}
	
	public Float getPlotForegroundAlpha() {
		return plotForegroundAlpha;
	}
	
	public void setPlotForegroundAlpha(Float plotForegroundAlpha) {
		this.plotForegroundAlpha = plotForegroundAlpha;
	}
	
	public String getTitleBackgroundPaint() {
		return titleBackgroundPaint;
	}
	
	public void setTitleBackgroundPaint(String titleBackgroundPaint) {
		this.titleBackgroundPaint = titleBackgroundPaint;
	}
	
	public String getTitlePaint() {
		return titlePaint;
	}
	
	public void setTitlePaint(String titlePaint) {
		this.titlePaint = titlePaint;
	}
	
	public String getLegendBackgroundPaint() {
		return legendBackgroundPaint;
	}
	
	public void setLegendBackgroundPaint(String legendBackgroundPaint) {
		this.legendBackgroundPaint = legendBackgroundPaint;
	}
	
	public String getLegendItemPaint() {
		return legendItemPaint;
	}
	
	public void setLegendItemPaint(String legendItemPaint) {
		this.legendItemPaint = legendItemPaint;
	}
	
	public String getDomainAxisPaint() {
		return domainAxisPaint;
	}
	
	public void setDomainAxisPaint(String domainAxisPaint) {
		this.domainAxisPaint = domainAxisPaint;
	}
	
	public String getDomainGridlinePaint() {
		return domainGridlinePaint;
	}
	
	public void setDomainGridlinePaint(String domainGridlinePaint) {
		this.domainGridlinePaint = domainGridlinePaint;
	}
	
	public String getDomainGridlineStroke() {
		return domainGridlineStroke;
	}
	
	public void setDomainGridlineStroke(String domainGridlineStroke) {
		this.domainGridlineStroke = domainGridlineStroke;
	}
	
	public boolean isDomainGridlinesVisible() {
		return domainGridlinesVisible;
	}
	
	public void setDomainGridlinesVisible(boolean domainGridlineVisible) {
		this.domainGridlinesVisible = domainGridlineVisible;
	}
	
	public String getRangeAxisPaint() {
		return rangeAxisPaint;
	}
	
	public void setRangeAxisPaint(String rangeAxisPaint) {
		this.rangeAxisPaint = rangeAxisPaint;
	}
	
	public String getRangeGridlinePaint() {
		return rangeGridlinePaint;
	}
	
	public void setRangeGridlinePaint(String rangeGridlinePaint) {
		this.rangeGridlinePaint = rangeGridlinePaint;
	}
	
	public String getRangeGridlineStroke() {
		return rangeGridlineStroke;
	}
	
	public void setRangeGridlineStroke(String rangeGridlineStroke) {
		this.rangeGridlineStroke = rangeGridlineStroke;
	}
	
	public boolean isRangeGridlinesVisible() {
		return rangeGridlinesVisible;
	}
	
	public void setRangeGridlinesVisible(boolean rangeGridlineVisible) {
		this.rangeGridlinesVisible = rangeGridlineVisible;
	}
	
	public List<Data> getData() {
		return data;
	}
	
	public List<String> getCategories() {
		return categories;
	}
	
	public void setDescription(String description) {
		this.description = description;
	}
	
	public String getDescription() {
		return this.description;
	}
	
	public void setChartType(String chartType) {
		this.chartType = chartType;
	}
	
	public String getChartType() {
		return this.chartType;
	}
}
