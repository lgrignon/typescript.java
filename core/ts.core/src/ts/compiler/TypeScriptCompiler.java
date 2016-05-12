/**
 *  Copyright (c) 2015-2016 Angelo ZERR.
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 *  which accompanies this distribution, and is available at
 *  http://www.eclipse.org/legal/epl-v10.html
 *
 *  Contributors:
 *  Angelo Zerr <angelo.zerr@gmail.com> - initial API and implementation
 */
package ts.compiler;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import ts.TypeScriptException;
import ts.nodejs.INodejsLaunchConfiguration;
import ts.nodejs.INodejsProcess;
import ts.nodejs.INodejsProcessListener;
import ts.nodejs.NodejsProcess;
import ts.nodejs.NodejsProcessManager;
import ts.utils.StringUtils;

public class TypeScriptCompiler implements ITypeScriptCompiler {

	private static final String TSC_FILE_TYPE = "tsc";
	private final File tscFile;
	private final File nodejsFile;

	public TypeScriptCompiler(File tscFile, File nodejsFile) {
		this.tscFile = tscFile;
		this.nodejsFile = nodejsFile;
	}

	@Override
	public List<String> createCommands(CompilerOptions options, List<String> filenames) {
		List<String> cmds = NodejsProcess.createNodeCommands(nodejsFile, tscFile);
		fillOptions(options, filenames, cmds);
		return cmds;
	}

	@Override
	public INodejsProcess compile(File baseDir, final CompilerOptions options, final List<String> filenames,
			INodejsProcessListener listener) throws TypeScriptException {
		INodejsProcess process = NodejsProcessManager.getInstance().create(baseDir, tscFile, nodejsFile,
				new INodejsLaunchConfiguration() {

					@Override
					public List<String> createNodeArgs() {
						List<String> args = new ArrayList<String>();
						fillOptions(options, filenames, args);
						return args;
					}
				}, TSC_FILE_TYPE);

		if (listener != null) {
			process.addProcessListener(listener);
		}
		process.start();
		try {
			process.join();
			// Thread.sleep(5000);
		} catch (InterruptedException e) {
			throw new TypeScriptException(e);
		}
		return process;
	}

	@Override
	public void dispose() {

	}

	private void fillOptions(final CompilerOptions options, final List<String> filenames, List<String> args) {
		if (filenames != null) {
			args.addAll(filenames);
		}
		if (options != null) {
			if (options.isListFiles()) {
				args.add("--listFiles");
			}
			if (!StringUtils.isEmpty(options.getOutDir())) {
				args.add("--outDir");
				args.add(options.getOutDir());
			}
			if (options.isSourceMap()) {
				args.add("--sourceMap");
			}
			Boolean watch = options.isWatch();
			if (watch != null && watch) {
				args.add("--watch");
			}
		}
	}
}
