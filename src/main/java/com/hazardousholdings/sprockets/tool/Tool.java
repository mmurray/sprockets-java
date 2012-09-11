package com.hazardousholdings.sprockets.tool;

import com.google.inject.Guice;
import com.hazardousholdings.sprockets.SprocketsModule;

public class Tool {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		Guice.createInjector(new SprocketsModule() {
			@Override
			protected void configureSprockets() {
//				setUrlRoot("http://cdn.themusicbot.com");
				addPath("src/main/javascript");
				addPath("src/main/css");
				addPath("src/main/mustache");
				setOutputPath("src/main/webapp");
				setLiveCompilation(false);
			}
		});
	}

}
