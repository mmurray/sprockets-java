package com.hazardousholdings.sprockets;

import java.io.File;
import java.io.IOException;
//import java.nio.file.FileSystems;
//import java.nio.file.Path;
//import java.nio.file.Paths;
//import java.nio.file.StandardWatchEventKinds;
//import java.nio.file.WatchEvent;
//import java.nio.file.WatchKey;
//import java.nio.file.WatchService;
import java.util.List;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.google.inject.AbstractModule;
import com.google.inject.Binder;
import com.google.inject.Module;
import com.google.inject.Provider;
import com.google.inject.Singleton;
import com.google.inject.servlet.RequestScoped;

public class SprocketsModule extends AbstractModule {

	private List<String> loadPath = Lists.newArrayList();
	private String outputPath;
	private String externsPath;
	private DirectiveProcessor processor;
	private String urlRoot = "";
	private boolean liveCompilation = true;

	protected void configureSprockets() {
	}

	protected void addPath(String dir) {
		loadPath.add(dir);
	}

	protected void setOutputPath(String dir) {
		outputPath = dir;
	}

	protected void setExternsPath(String dir) {
		externsPath = dir;
	}
	
	protected void setUrlRoot(String root) {
		urlRoot = root;
	}
	
	protected void setLiveCompilation(boolean live) {
		liveCompilation = live;
	}

	@Override
	protected void configure() {
		configureSprockets();
		if (outputPath == null) {
			throw new RuntimeException("Must configure an output path.");
		}
		if (loadPath.size() < 1) {
			throw new RuntimeException(
					"Must configure at least one input path.");
		}
		processor = new DirectiveProcessor(loadPath, outputPath);
		if (externsPath != null) {
			processor.setExternsPath(externsPath);
		}
		List<Asset> assets = processor.process();
		System.out.println("##### assets: "+assets.size());
		for (Asset asset : assets) {
			install(new AssetBindingModule(
					urlRoot,
					asset,
					processor,
					liveCompilation));
		}
	}

	private static class AssetBindingModule implements Module {
		final Asset asset;
		final String urlRoot;
		final DirectiveProcessor processor;
		final boolean live;
		

		AssetBindingModule(String urlRoot,
				Asset asset,
				DirectiveProcessor processor,
				boolean live) {
			this.asset = Preconditions.checkNotNull(asset);
			this.urlRoot = urlRoot;
			this.processor = processor;
			this.live = live;
		}

		public void configure(Binder binder) {
			System.out.println("### binding: "+asset.getOriginalPath());
			System.out.println("##### to: "+ (asset.getType().equals("js") ? new JsBundleImpl(asset).value() : new CssBundleImpl(asset).value()));
			
			binder.bind(String.class)
				.annotatedWith(asset.getType().equals("js") ? new JsBundleImpl(asset) : new CssBundleImpl(asset))
				.toProvider(new AssetProvider(urlRoot, asset, processor, live))
				.in(live ? RequestScoped.class : Singleton.class);
		}

	}
	
	private static class AssetProvider implements Provider<String> {
		private Asset asset;
		final String urlRoot;
		final DirectiveProcessor processor;
		final boolean live;
		
		private String tag;
		
		AssetProvider(String urlRoot,
				Asset asset,
				DirectiveProcessor processor,
				boolean live) {
			this.asset = asset;
			this.urlRoot = urlRoot;
			this.processor = processor;
			this.live = live;
		}

		public String get() {
			if (live) {
				processor.process();
				System.out.println("live process:" + asset.getOriginalPath());
				System.out.println(asset.getTopLevelPath());
			}
			if (tag == null) {
				StringBuilder tagBuilder = new StringBuilder();
				if (asset.getType().equals("js")) {
					tagBuilder.append("<script type='text/javascript' src='");
				} else {
					tagBuilder.append("<link rel='stylesheet' href='");
				}
				// TODO: support optional /
				tagBuilder.append(urlRoot + '/' + this.asset.getPath());
				if (asset.getType().equals("js")) {
					tagBuilder.append("'></script>");
				} else {
					tagBuilder.append("' />");
				}
				tag = tagBuilder.toString();
			}
			return tag.toString();
		}
		
	}
}
