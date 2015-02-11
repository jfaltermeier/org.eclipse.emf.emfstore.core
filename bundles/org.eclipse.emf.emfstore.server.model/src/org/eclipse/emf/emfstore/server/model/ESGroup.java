/*******************************************************************************
 * Copyright (c) 2011-2015 EclipseSource Muenchen GmbH and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Edgar Mueller - initial API and implementation
 ******************************************************************************/
package org.eclipse.emf.emfstore.server.model;

import java.util.Collection;

/**
 * A group consisting of multiple users. A group maybe empty.
 *
 * @author emueller
 * @since 1.5
 *
 */
public interface ESGroup extends ESOrgUnit {

	/**
	 * Returns a collection of roles associated with the group.
	 *
	 * @return a collection of roles associated with the group
	 */
	Collection<? extends ESRole> getRoles();

}
