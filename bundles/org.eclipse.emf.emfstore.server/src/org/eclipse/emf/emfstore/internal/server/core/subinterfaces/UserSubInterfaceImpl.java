/*******************************************************************************
 * Copyright (c) 2008-2011 Chair for Applied Software Engineering,
 * Technische Universitaet Muenchen.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Otto von Wesendonk
 ******************************************************************************/
package org.eclipse.emf.emfstore.internal.server.core.subinterfaces;

import org.eclipse.emf.emfstore.internal.server.accesscontrol.AccessControl;
import org.eclipse.emf.emfstore.internal.server.core.AbstractEmfstoreInterface;
import org.eclipse.emf.emfstore.internal.server.core.AbstractSubEmfstoreInterface;
import org.eclipse.emf.emfstore.internal.server.core.helper.EmfStoreMethod;
import org.eclipse.emf.emfstore.internal.server.core.helper.EmfStoreMethod.MethodId;
import org.eclipse.emf.emfstore.internal.server.exceptions.FatalESException;
import org.eclipse.emf.emfstore.internal.server.model.SessionId;
import org.eclipse.emf.emfstore.internal.server.model.accesscontrol.ACOrgUnitId;
import org.eclipse.emf.emfstore.internal.server.model.accesscontrol.ACUser;
import org.eclipse.emf.emfstore.internal.server.model.impl.api.ESUserImpl;
import org.eclipse.emf.emfstore.server.exceptions.ESException;
import org.eclipse.emf.emfstore.server.model.ESUser;

/**
 * This subinterface implements all user related functionality.
 *
 * @author wesendonk
 */
public class UserSubInterfaceImpl extends AbstractSubEmfstoreInterface {

	/**
	 * Default constructor.
	 *
	 * @param parentInterface parent interface
	 * @throws FatalESException in case of failure
	 */
	public UserSubInterfaceImpl(AbstractEmfstoreInterface parentInterface) throws FatalESException {
		super(parentInterface);
	}

	/**
	 * Resolves a given user ID to the an actual user instance.
	 *
	 * @param sessionId
	 *            the ID of the session that is used to resolve the user
	 * @param id
	 *            the user ID
	 * @return the user with the given ID
	 * @throws ESException in case of failure
	 */
	@EmfStoreMethod(MethodId.RESOLVEUSER)
	public ACUser resolveUser(SessionId sessionId, ACOrgUnitId id) throws ESException {
		sanityCheckObjects(sessionId);
		synchronized (getMonitor()) {
			final AccessControl accessControl = getAccessControl();
			final ACUser requestingUser = accessControl.getSessions().getRawUser(sessionId.toAPI());
			final ESUser resolvedUser = accessControl.getOrgUnitResolverServive().resolve(id.toAPI());
			final ACUser acUser = (ACUser) ESUserImpl.class.cast(resolvedUser).toInternalAPI();
			if (requestingUser.getId().equals(acUser.getId())) {
				return acUser;
			}
			accessControl.getAuthorizationService().checkServerAdminAccess(sessionId.toAPI());
			return acUser;
		}
	}
}