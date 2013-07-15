/*******************************************************************************
 * Copyright (c) 2008-2011 Chair for Applied Software Engineering,
 * Technische Universitaet Muenchen.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 * Julian Sommerfeldt - initial API and implementation
 ******************************************************************************/
package org.eclipse.emf.emfstore.common;

/**
 * Interface for code which should run with the {@link ESSafeRunner}.
 * 
 * @author Julian Sommerfeldt
 * 
 */
public interface ESSafeRunnable {

	/**
	 * Contains the code to execute.
	 * 
	 * @noreference This method is not intended to be referenced by clients.
	 */
	void run();

	/**
	 * Handles a exception if one occurs during execution.
	 * 
	 * @param exception the {@link Throwable} which occurred during execution.
	 * 
	 * @noreference This method is not intended to be referenced by clients.
	 */
	void handleException(Throwable exception);

}
