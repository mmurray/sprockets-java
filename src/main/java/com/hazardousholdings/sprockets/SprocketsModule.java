package com.hazardousholdings.sprockets;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.inject.*;
import com.hazardousholdings.sprockets.annotations.AssetProviders;

import java.io.File;
import java.nio.file.*;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Future;

public class SprocketsModule extends AbstractModule {

	private List<String> loadPath = Lists.newArrayList();
	private String outputPath;
	private String externsPath;
	private DirectiveProcessor processor;
	private String urlRoot = "";
	private boolean liveCompilation = true;
	private boolean debug = false;
	private boolean compiling = false;
	private boolean skipCompile = false;
	private Future<List<Asset>> compileFuture;
	private WatchService watchService;

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
	
	protected void setDebug(boolean d) {
		debug = d;
	}
	
	protected void setUrlRoot(String root) {
		urlRoot = root;
	}

	protected void setLiveCompilation(boolean live) {
		liveCompilation = live;
	}
	
	protected void setSkipCompile(boolean skip) {
		skipCompile = skip;
	}

	@Override
	protected void configure() {
		configureSprockets();
		if (outputPath == null) {
			outputPath = "src/main/webapp";
//			throw new RuntimeException("Must configure an output path.");
		}
		if (loadPath.size() < 1) {
			loadPath.add("src/main/javascript");
			loadPath.add("src/main/css");
//			throw new RuntimeException(
//					"Must configure at least one input path.");
		}
		processor = new DirectiveProcessor(loadPath, outputPath, debug, skipCompile);
		if (externsPath != null) {
			processor.setExternsPath(externsPath);
		}
		List<Asset> assets = processor.process();
		Map<String, Asset> assetMap = Maps.newHashMap();
		Map<String, Provider<String>> assetProviderMap = Maps.newHashMap();
		for (Asset asset : assets) {
			install(new AssetBindingModule(
					urlRoot,
					asset,
					processor));
			for (Asset a : assets) {
				a.setUrlRoot(urlRoot);
				String key = a.getName().substring(1); // remove the preceding '/'
				assetMap.put(key, a);
				assetProviderMap.put(key, new AssetProvider(key, asset, processor));
			}
		}
		bind(new TypeLiteral<Map<String, Asset>>(){}).toInstance(assetMap);
		bind(new TypeLiteral<Map<String, Provider<String>>>(){})
			.annotatedWith(AssetProviders.class)
			.toInstance(assetProviderMap);
		if (liveCompilation) {
			setupLiveCompilation();
		}
	}
	
	private void setupLiveCompilation() {
		try {
			watchService = FileSystems.getDefault().newWatchService();
			for (final String pathString : loadPath) {
				watchAllFolders(new File(pathString));
			}
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
	
	private void watchAllFolders(File f) {
		if (!f.isDirectory()) {
			return;
		}
		watchFolder(f);
		for (File child : f.listFiles()) {
			if (child.isDirectory()) {
				watchAllFolders(child);
			}
		}
	}
	
	private void watchFolder(final File f) {
		new Thread() {
			public void run() {
				try {
					Path topLevelPath = Paths.get(f.getAbsolutePath());
					WatchKey watchKey = topLevelPath.register(watchService, StandardWatchEventKinds.ENTRY_MODIFY);
					while(true) {
						List<WatchEvent<?>> events = watchKey.pollEvents();
						for (WatchEvent<?> event : events) {
							if (event.kind() == StandardWatchEventKinds.ENTRY_MODIFY) {
								System.out.println("## detected change to: "+event.context());
								processor.process();
								System.out.println("## processed!");
							}
						}
						boolean reset = watchKey.reset();
						if (!reset) {
							break;
						}
					}
		    	  }catch (Exception e) {
		    		  throw new RuntimeException(e);
		    	  }
		      }
		}.start();
	}

	private static class AssetBindingModule implements Module {
		final Asset asset;
		final String urlRoot;
		final DirectiveProcessor processor;

		AssetBindingModule(String urlRoot,
				Asset asset,
				DirectiveProcessor processor) {
			this.asset = Preconditions.checkNotNull(asset);
			this.urlRoot = urlRoot;
			this.processor = processor;
		}

		public void configure(Binder binder) {
			binder.bind(String.class)
				.annotatedWith(asset.getType().equals("js") ? new JsBundleImpl(asset) : new CssBundleImpl(asset))
				.toProvider(new AssetProvider(urlRoot, asset, processor));
		}

	}
	
	private static class AssetProvider implements Provider<String> {
		private Asset asset;
		final String urlRoot;
		final DirectiveProcessor processor;

		private String tag;

		AssetProvider(String urlRoot,
				Asset asset,
				DirectiveProcessor processor) {
			this.asset = asset;
			this.urlRoot = urlRoot;
			this.processor = processor;
		}

		public String get() {
			if (tag == null) {
				tag = asset.getTag();
			}
			return tag.toString();
		}
		
	}
}
