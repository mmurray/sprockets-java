package com.hazardousholdings.sprockets;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.StringWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.zkoss.zuss.Zuss;
import org.zkoss.zuss.impl.out.BuiltinResolver;
import org.zkoss.zuss.metainfo.ZussDefinition;
import org.mozilla.javascript.ErrorReporter;
import org.mozilla.javascript.EvaluatorException;

import com.bazaarvoice.jless.LessProcessor;
import com.google.common.base.Charsets;
import com.google.common.base.Joiner;
import com.google.common.collect.Lists;
import com.google.common.io.Files;
import com.google.javascript.jscomp.CommandLineRunner;
import com.google.javascript.jscomp.CompilationLevel;
import com.google.javascript.jscomp.Compiler;
import com.google.javascript.jscomp.CompilerOptions;
import com.google.javascript.jscomp.JSSourceFile;
import com.google.javascript.jscomp.SourceFile;
import com.yahoo.platform.yui.compressor.CssCompressor;
import com.yahoo.platform.yui.compressor.JavaScriptCompressor;

/**
 * Reads and compiles files, converting zuss -> css in the process 
 *
 */
public class AssetReader {
	
	private static Logger logger = Logger.getLogger(AssetReader.class.getName());
	
	private final List<String> paths;
	
	public AssetReader(List<String> paths) {
		this.paths = paths;
	}
	
	public String getContents(File f) {
		return this.getContents(f, true);
	}

	public String getContents(File f, boolean compiled) {
		try {
			StringWriter contentWriter = new StringWriter();
			String type = f.getName().substring(f.getName().lastIndexOf('.') + 1);
			String name = f.getName().substring(0, f.getName().lastIndexOf('.'));
			
			if (type.equals("zuss") && compiled) {
				ZussDefinition zussDef = Zuss.parse(f, "UTF-8");
				Zuss.translate(zussDef, contentWriter, new BuiltinResolver());
				type = "css"; // consider it css from this point on
			} else {
				contentWriter.write(Joiner.on('\n').join(Files.readLines(f, Charsets.UTF_8)));
			}
			return contentWriter.toString();
		} catch (IOException e) {
			return "";
		}
	}
}
