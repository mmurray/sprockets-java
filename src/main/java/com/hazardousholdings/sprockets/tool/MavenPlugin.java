package com.hazardousholdings.sprockets.tool;

import com.google.inject.Guice;
import com.hazardousholdings.sprockets.SprocketsModule;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;

import java.io.File;

/**
 * @goal asset-compile
 * @phase prepare-package
 */
public class MavenPlugin extends AbstractMojo {

	/**
	 * @parameter expression="${basedir}/src/main/javascript"
	 */
	private File javascriptDir;

	/**
	 * @parameter expression="${basedir}/src/main/css"
	 */
	private File cssDir;

	/**
	 * @parameter expression="${basedir}/src/main/mustache"
	 */
	private File mustacheDir;

	/**
	 * @parameter expression="${basedir}/src/main/webapp"
	 */
	private File outDir;

	@Override
	public void execute() throws MojoExecutionException, MojoFailureException {
		Guice.createInjector(new SprocketsModule() {
			@Override
			protected void configureSprockets() {
//				setUrlRoot("http://cdn.themusicbot.com");
				addPath(javascriptDir.getPath());
				addPath(cssDir.getPath());
				addPath(mustacheDir.getPath());
				setOutputPath(outDir.getPath());
				setLiveCompilation(false);
			}
		});
	}

}
