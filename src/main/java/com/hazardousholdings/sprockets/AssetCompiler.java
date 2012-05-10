package com.hazardousholdings.sprockets;

import java.io.File;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import com.google.common.base.Joiner;
import com.google.javascript.jscomp.CommandLineRunner;
import com.google.javascript.jscomp.CompilationLevel;
import com.google.javascript.jscomp.Compiler;
import com.google.javascript.jscomp.CompilerOptions;
import com.google.javascript.jscomp.JSSourceFile;
import com.google.javascript.jscomp.SourceFile;
import com.yahoo.platform.yui.compressor.CssCompressor;

public class AssetCompiler {
	
	private String externsPath;
	
	public void setExternsPath(String path) {
		this.externsPath = externsPath;
	}

	public String compile(List<String> contents, String type) {
		if (type.equals("js")) {
			Compiler compiler = new Compiler();
			CompilerOptions options = new CompilerOptions();
			CompilationLevel.SIMPLE_OPTIMIZATIONS.setOptionsForCompilationLevel(
			        options);
			List<SourceFile> jsExterns = new ArrayList<SourceFile>();
			try {
				jsExterns.addAll(CommandLineRunner.getDefaultExterns());
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
			jsExterns.add(JSSourceFile.fromCode("externs.js", "function alert(x) {};"));
//			if (externsPath != null) {
//				File dir = new File(externsPath);
//				if (dir.exists()) {
//					for (File extern : dir.listFiles()) {
//						jsExterns.add(JSSourceFile.fromFile(extern));
//					}
//				}
//			}
			List<SourceFile> sourceFiles = new ArrayList<SourceFile>();
			for (String content : contents) {
				sourceFiles.add(SourceFile.fromCode(UUID.randomUUID().toString(), content));
			}
//			return Joiner.on(' ').join(contents);
			compiler.compile(jsExterns, sourceFiles, options);
			return compiler.toSource();
		} else {
			try {
				Writer out = new StringWriter();
				Reader reader = new StringReader(Joiner.on(' ').join(contents));
				CssCompressor compressor = new CssCompressor(reader);
				reader.close(); reader = null;
	
				compressor.compress(out,
						/* linebreak */ -1);
				String result = out.toString();
				out.close(); out = null;
				return result;
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		}
	}
	
	
}
