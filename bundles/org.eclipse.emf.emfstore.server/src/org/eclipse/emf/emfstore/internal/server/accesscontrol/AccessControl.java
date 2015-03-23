/*******************************************************************************
 * Copyright (c) 2015 EclipseSource Muenchen GmbH and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Edgar Mueller - initial API and implementation
 ******************************************************************************/
package org.eclipse.emf.emfstore.internal.server.accesscontrol;

import java.text.MessageFormat;
import java.util.List;

import org.eclipse.emf.emfstore.common.extensionpoint.ESExtensionPoint;
import org.eclipse.emf.emfstore.common.extensionpoint.ESExtensionPointException;
import org.eclipse.emf.emfstore.internal.common.model.util.ModelUtil;
import org.eclipse.emf.emfstore.internal.server.ServerConfiguration;
import org.eclipse.emf.emfstore.internal.server.impl.api.ESOrgUnitProviderImpl;
import org.eclipse.emf.emfstore.internal.server.model.ServerSpace;
import org.eclipse.emf.emfstore.server.auth.ESAuthenticationControlType;
import org.eclipse.emf.emfstore.server.auth.ESAuthorizationService;
import org.eclipse.emf.emfstore.server.auth.ESOrgUnitResolver;
import org.eclipse.emf.emfstore.server.model.ESOrgUnitProvider;

/**
 * Access control class holding references to the customizable access control related services.
 *
 * @author emueller
 *
 */
public class AccessControl {

	private static final String ORG_UNIT_RESOLVER_SERVICE_CLASS = "orgUnitResolverServiceClass"; //$NON-NLS-1$

	private static final String ORG_UNIT_PROVIDER_CLASS = "orgUnitProviderClass"; //$NON-NLS-1$

	private static final String AUTHORIZATION_SERVICE_CLASS = "authorizationServiceClass"; //$NON-NLS-1$

	private static final String ACCESSCONTROL_EXTENSION_ID = "org.eclipse.emf.emfstore.server.accessControl"; //$NON-NLS-1$

	private final ESOrgUnitProvider orgUnitProvider;

	private final ESAuthorizationService authorizationService;
	private final ESOrgUnitResolver orgUnitResolver;

	private final Sessions sessions;

	private final LoginService loginService;

	private final ServerSpace serverSpace;

	private ESAuthenticationControlType authenticationControlType;

	/**
	 * Constructor.
	 *
	 * @param serverSpace
	 *            the server space
	 */
	public AccessControl(ServerSpace serverSpace) {
		this.serverSpace = serverSpace;
		sessions = new Sessions();

		orgUnitProvider = initOrgUnitProviderService();
		orgUnitResolver = initOrgUnitResolverService();
		authorizationService = initAuthorizationService();
		loginService = initLoginService();
	}

	/**
	 * Constructor.
	 *
	 * @param authenticationControlType
	 *            the type of the login service to be used
	 * @param serverSpace
	 *            the server space
	 */
	public AccessControl(
		ESAuthenticationControlType authenticationControlType,
		ServerSpace serverSpace) {

		this.authenticationControlType = authenticationControlType;
		this.serverSpace = serverSpace;
		sessions = new Sessions();

		orgUnitProvider = initOrgUnitProviderService();
		orgUnitResolver = initOrgUnitResolverService();
		authorizationService = initAuthorizationService();
		loginService = initLoginService();
	}

	/**
	 * @return
	 */
	private LoginService initLoginService() {

		if (authenticationControlType == null) {
			final String[] splittedProperty = ServerConfiguration
				.getSplittedProperty(ServerConfiguration.AUTHENTICATION_POLICY);
			authenticationControlType = ESAuthenticationControlType.valueOf(splittedProperty[0]);
		}
		return new LoginService(
			authenticationControlType,
			sessions,
			orgUnitProvider,
			getOrgUnitResolverServive());
	}

	/**
	 * @return
	 */
	private ESAuthorizationService initAuthorizationService() {
		ESAuthorizationService authorizationService;
		try {
			final List<ESAuthorizationService> services = new ESExtensionPoint(ACCESSCONTROL_EXTENSION_ID, false)
				.getClasses(AUTHORIZATION_SERVICE_CLASS, ESAuthorizationService.class);
			if (services.isEmpty()) {
				authorizationService = new DefaultESAuthorizationService();
			} else if (services.size() == 1) {
				authorizationService = services.get(0);
			} else {
				throw new IllegalStateException(
					MessageFormat.format(
						Messages.AccessControl_MultipleExtensionsDiscovered,
						ACCESSCONTROL_EXTENSION_ID + "." + AUTHORIZATION_SERVICE_CLASS)); //$NON-NLS-1$
			}
		} catch (final ESExtensionPointException e) {
			final String message = Messages.AccessControl_CustomAuthorizationInitFailed;
			ModelUtil.logException(message, e);
			authorizationService = new DefaultESAuthorizationService();
		}

		authorizationService.init(
			sessions,
			getOrgUnitResolverServive(),
			orgUnitProvider);

		return authorizationService;
	}

	private ESOrgUnitResolver initOrgUnitResolverService() {
		ESOrgUnitResolver resolver;
		try {
			final List<ESOrgUnitResolver> resolvers =
				new ESExtensionPoint(ACCESSCONTROL_EXTENSION_ID, false).getClasses(
					ORG_UNIT_RESOLVER_SERVICE_CLASS, ESOrgUnitResolver.class);
			if (resolvers.isEmpty()) {
				resolver = new DefaultESOrgUnitResolverService();
			} else if (resolvers.size() == 1) {
				resolver = resolvers.get(0);
			} else {
				throw new IllegalStateException(
					MessageFormat.format(
						Messages.AccessControl_MultipleExtensionsDiscovered,
						ACCESSCONTROL_EXTENSION_ID + "." + ORG_UNIT_RESOLVER_SERVICE_CLASS //$NON-NLS-1$
						));
			}
		} catch (final ESExtensionPointException e) {
			final String message = "Custom org unit resolver class not be initialized"; //$NON-NLS-1$
			ModelUtil.logException(message, e);
			resolver = new DefaultESOrgUnitResolverService();
		}

		resolver.init(orgUnitProvider);
		return resolver;
	}

	private ESOrgUnitProvider initOrgUnitProviderService() {
		ESOrgUnitProvider orgUnitProvider = null;
		try {
			final List<ESOrgUnitProvider> providers =
				new ESExtensionPoint(ACCESSCONTROL_EXTENSION_ID, false).getClasses(
					ORG_UNIT_PROVIDER_CLASS, ESOrgUnitProvider.class);
			if (providers.isEmpty()) {
				orgUnitProvider = new ESOrgUnitProviderImpl();
			} else if (providers.size() == 1) {
				orgUnitProvider = providers.get(0);
			} else {
				throw new IllegalStateException(
					MessageFormat.format(
						Messages.AccessControl_MultipleExtensionsDiscovered,
						ACCESSCONTROL_EXTENSION_ID + "." + ORG_UNIT_PROVIDER_CLASS)); //$NON-NLS-1$
			}
		} catch (final ESExtensionPointException e) {
			final String message = Messages.AccessControl_CustomOrgUnitProviderInitFailed;
			ModelUtil.logException(message, e);
			orgUnitProvider = new ESOrgUnitProviderImpl();
		}

		orgUnitProvider.init(serverSpace);
		return orgUnitProvider;
	}

	/**
	 * Returns the {@link ESOrgUnitResolver}.
	 *
	 * @return the {@link ESOrgUnitResolver} in use.
	 */
	public ESOrgUnitResolver getOrgUnitResolverServive() {
		return orgUnitResolver;
	}

	/**
	 * Returns the {@link ESAuthorizationService}.
	 *
	 * @return the {@link ESAuthorizationService} in use.
	 */
	public ESAuthorizationService getAuthorizationService() {
		return authorizationService;
	}

	/**
	 * Returns the current session mapping.
	 *
	 * @return the session mapping
	 */
	public Sessions getSessions() {
		return sessions;
	}

	/**
	 * Returns the login service.
	 *
	 * @return the login service.
	 */
	public LoginService getLoginService() {
		return loginService;
	}

}
