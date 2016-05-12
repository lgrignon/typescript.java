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
package ts.eclipse.ide.core.compiler;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;

import ts.client.Location;
import ts.compiler.ITypeScriptCompilerMessageHandler;
import ts.compiler.TypeScriptCompilerSeverity;
import ts.eclipse.ide.core.resources.jsconfig.IDETsconfigJson;
import ts.eclipse.ide.core.utils.TypeScriptResourceUtil;
import ts.eclipse.ide.internal.core.Trace;
import ts.resources.jsonconfig.TsconfigJson;

/**
 * Eclipse IDE implementation of {@link ITypeScriptCompilerMessageHandler} used
 * to track "tsc" message to:
 * 
 * <ul>
 * <li>add error marker to the *.ts files which have error.</li>
 * <li>refresh emitted files *.js and *.js.map files</li>
 * </ul>
 */
public class IDETypeScriptCompilerMessageHandler implements ITypeScriptCompilerMessageHandler {

	/** Constant for marker type. */
	private static final String MARKER_TYPE = "ts.eclipse.ide.core.typeScriptProblem";

	private final IContainer container;
	private final IDETsconfigJson tsconfig;
	private final List<IFile> filesToRefresh;

	public IDETypeScriptCompilerMessageHandler(IContainer container) throws CoreException {
		this.container = container;
		this.tsconfig = TypeScriptResourceUtil.findTsconfig(container);
		this.filesToRefresh = new ArrayList<IFile>();
		container.deleteMarkers(MARKER_TYPE, true, IResource.DEPTH_INFINITE);
	}

	@Override
	public void addFile(String filePath) {
		IFile file = getFile(filePath);
		if (file != null && !filesToRefresh.contains(file)) {
			filesToRefresh.add(file);
		}
	}

	private IFile getFile(String filePath) {
		IPath path = new Path(filePath);
		if (container.exists(path)) {
			return container.getFile(path);
		}
		return null;
	}

	public List<IFile> getFilesToRefresh() {
		return filesToRefresh;
	}

	@Override
	public void onCompilationCompleteWatchingForFileChanges() {
		refreshFiles();
	}

	private void refreshFiles() {
		for (IFile tsFile : getFilesToRefresh()) {
			try {
				TypeScriptResourceUtil.refreshAndCollectEmittedFiles(tsFile, tsconfig, true, null);
			} catch (CoreException e) {
				Trace.trace(Trace.SEVERE, "Error while tsc compilation when ts file is refreshed", e);
			}
		}
	}

	public IDETsconfigJson getTsconfig() {
		return tsconfig;
	}

	@Override
	public void addError(String filename, Location startLoc, Location endLoc, TypeScriptCompilerSeverity severity,
			String code, String message) {
		IFile file = getFile(filename);
		if (file != null) {
			try {
				IMarker marker = file.createMarker(MARKER_TYPE);
				marker.setAttribute(IMarker.MESSAGE, message);
				marker.setAttribute(IMarker.SEVERITY, getSeverity(severity));
				marker.setAttribute(IMarker.LINE_NUMBER, startLoc.getLine());
			} catch (CoreException e) {

			}
		}
	}

	private int getSeverity(TypeScriptCompilerSeverity severity) {
		switch (severity) {
		case error:
			return IMarker.SEVERITY_ERROR;
		case info:
			return IMarker.SEVERITY_INFO;
		default:
			return IMarker.SEVERITY_WARNING;
		}
	}

	public boolean isWatch() {
		TsconfigJson tsconfig = getTsconfig();
		return tsconfig != null && tsconfig.getCompilerOptions() != null
				&& tsconfig.getCompilerOptions().isWatch() != null && tsconfig.getCompilerOptions().isWatch();
	}
}
