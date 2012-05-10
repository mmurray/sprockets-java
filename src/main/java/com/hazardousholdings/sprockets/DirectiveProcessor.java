package com.hazardousholdings.sprockets;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.google.common.base.Charsets;
import com.google.common.base.Joiner;
import com.google.common.collect.Lists;
import com.google.common.io.Files;

public class DirectiveProcessor {
	
	private static final Pattern HEADER_PATTERN =
		Pattern.compile("^\\/\\*(.*)\\*\\/", Pattern.DOTALL);
	
	private static final Pattern DIRECTIVE_PATTERN =
		Pattern.compile("([\\W]*=\\s*(\\w+.*?)\\s([\\w\\/]+))");
	
	private final List<String> loadPath;
	private final String outputPath;
	private final AssetReader reader;
	private final AssetCompiler compiler;
	private File currentTopLevelFile;
	private String currentTopLevelPath;	
	
	public DirectiveProcessor(List<String> loadPath, String outputPath) {
		this.loadPath = loadPath;
		this.outputPath = outputPath;
		this.reader = new AssetReader(loadPath);
		this.compiler = new AssetCompiler();
	}
	
	public void setExternsPath(String path) {
		this.compiler.setExternsPath(path);
	}
	
	public List<Asset> process() {
		List<Asset> assets = Lists.newArrayList();
		for (String path : loadPath) {
			currentTopLevelPath = path;
			currentTopLevelFile = new File(path);
			assets.addAll(processFolder(currentTopLevelFile));
		}
		return assets;
	}
	
	public List<Asset> processFolder(File dir) {
		List<Asset> assets = Lists.newArrayList();
		if (dir.isDirectory()) {
			for (File f : dir.listFiles()) {
				assets.addAll(processFolder(f));
			}
		} else {
			Asset asset = processFile(dir);
			if (asset != null) {
				assets.add(asset);
			}
		}
		return assets;
	}
	
	public Asset processFile(File dir) {
		return this.processFile(dir, currentTopLevelFile.getAbsolutePath());
	}
	
	public Asset processFile(File dir, String topLevel) {
		if (dir.exists()) {
			String currentAssetName = dir.getAbsolutePath().replace(topLevel, "");
			String currentAssetType = currentAssetName.substring(currentAssetName.lastIndexOf('.') + 1);
			String currentAssetContent = reader.getContents(dir, false /* compile */);
			System.out.println("processing: "+currentAssetName);
			File topLevelFile = new File(topLevel);
			Asset asset = new Asset(currentAssetName, currentAssetType, topLevelFile.getName() + currentAssetName, dir.getAbsolutePath(), topLevel);
			
			// look for a header
			Matcher headerMatcher = HEADER_PATTERN.matcher(currentAssetContent);
			if (headerMatcher.find()) {
				String header = headerMatcher.group();
				// found a header, parse the directives out of it
				Matcher directiveMatcher = DIRECTIVE_PATTERN.matcher(header);
				System.out.println("###### FOUND ##### "+directiveMatcher.groupCount());
				while (directiveMatcher.find()) {
					String directive = directiveMatcher.group(2);
					String argument = directiveMatcher.group(3);
					if (directive.equals("require")) {
						asset.appendContent(argument, require(argument, currentAssetType));
					} else if (directive.equals("include")) {
						asset.includeContent(require(argument, currentAssetType));
					} else if (directive.equals("require_self")) {
						asset.appendContent(argument, reader.getContents(dir, true), true);
					} else if (directive.equals("require_directory")) {
						// TODO: require_directory
					} else if (directive.equals("require_tree")) {
						// TODO: require_tree
					} else if (directive.equals("depend_on")) {
						// TODO: depend_on
					} else if (directive.equals("stub")) {
						// TODO: stub
					}
				}
			}
			
			if(!asset.selfAppended) {
				asset.appendContent(currentAssetName, reader.getContents(dir, true), true);
			}
			
			if (currentAssetType.equals("less")) {
				currentAssetName = currentAssetName.replace(".less", ".css");
			}
			asset.setName(currentAssetName);
			asset.setPath(topLevelFile.getName() + currentAssetName);
			File output = new File(outputPath + '/' + asset.getPath());
			output.mkdirs();
			if (output.exists()) {
				output.delete();
			}
			try {
				System.out.println("###WRITING: "+output.getAbsolutePath());
				output.createNewFile();
				String compiled = compiler.compile(asset.getContents(), currentAssetType);
				Files.write(compiled.getBytes(), output);
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
			return asset;
		}
		return null;
	}
	
	private String require(String name, String type) {
		File file = null;
		for (String path : loadPath) {
			// TODO: support .less -> .css
			System.out.println("trying: "+path + '/' + name + '.' + type);
			file = new File(path + '/' + name + '.' + type);
			if (file.exists()) {
				break;
			}
		}
		if (file == null || !file.exists()) {
			return "";
		}
		
		return reader.getContents(file);
	}

}
