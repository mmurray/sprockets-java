package com.hazardousholdings.sprockets.tool;

import java.io.File;
import java.io.FileFilter;

import com.google.inject.Binder;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.PrivateBinder;
import com.hazardousholdings.sprockets.SprocketsModule;

public class Tool {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		final String projectDirString = System.getProperty("user.dir") + "/src/main";
		File projectDir = new File(projectDirString);
		File[] childDirs = projectDir.listFiles(new FileFilter(){
			@Override
			public boolean accept(File pathname) {
				System.out.println(pathname.getName());
				switch (pathname.getName()) {
				case "css":
				case "javascript":
					return true;
				}
				return false;
			}
		});
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
