package com.hazardousholdings.sprockets;

import java.io.File;
import java.util.List;

import com.google.common.collect.Lists;

public class Asset {
	private String name;
	private String path;
	private String urlRoot = "";
	private final String ogPath;
	private final String type;
	private final String topLevelPath;
	private final List<String> contents = Lists.newArrayList();
	private final List<String> includedFiles = Lists.newArrayList();
	
	public boolean selfAppended = false;

	public Asset(String name, String type, String path, String ogPath, String topLevelPath) {
		this.name = name;
		this.type = type;
		this.path = path;
		this.ogPath = ogPath;
		this.topLevelPath = topLevelPath;
	}
	
	public String getName() {
		return name;
	}

	public String getType() {
		return type;
	}
	
	public String getTopLevelPath() {
		return topLevelPath;
	}
	
	public String getPath() {
		return path;
	}
	
	public String getOriginalPath() {
		return ogPath;
	}
	
	public void setName(String name) {
		this.name = name;
	}

	public void setPath(String path) {
		this.path = path;
	}
	
	public void setUrlRoot(String urlRoot) {
		this.urlRoot = urlRoot;
	}
	
	public String getTag() {
		StringBuilder tagBuilder = new StringBuilder();
		if (this.getType().equals("js")) {
			tagBuilder.append("<script type='text/javascript' src='");
		} else {
			tagBuilder.append("<link rel='stylesheet' href='");
		}
		// TODO: support optional /
		tagBuilder.append(urlRoot + '/' + this.getPath());
		if (this.getType().equals("js")) {
			tagBuilder.append("'></script>");
		} else {
			tagBuilder.append("' />");
		}
		return tagBuilder.toString();
	}
	
	public void appendContent(String file, String content) {
		this.appendContent(file, content, false);
	}
	
	public void appendContent(String file, String content, boolean self) {
		if (includedFiles.contains(file)) {
			return;
		}
		this.includeContent(content, self);
		includedFiles.add(file);
	}
	
	public void includeContent(String content) {
		this.includeContent(content);
	}
	
	public void includeContent(String content, boolean self) {
		contents.add(content);
		
		if (self) {
			selfAppended = true;
		}
	}
	
	public List<String> getContents() {
		return contents;
	}
}
