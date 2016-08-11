/**
 * Copyright (c) 2011 committers of YAKINDU and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     committers of YAKINDU - initial API and implementation
 */
package org.yakindu.sct.generator.core.extensions;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.Platform;
import org.yakindu.sct.generator.core.ISCTGenerator;

import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;

/**
 * @author holger willebrandt - Initial contribution and API
 */
public class GeneratorExtensions {

	private static final String EXTENSION_POINT_ID = "org.yakindu.sct.generator.core.generator";
	private static final String ATTRIBUTE_CLASS = "class";
	private static final String ATTRIBUTE_ID = "id";
	private static final String LIBRARY_CONFIG_ELEMENT = "FeatureLibrary";
	private static final String ATTRIBUTE_LIBRARY_ID = "library_id";
	private static final String ATTRIBUTE_NAME = "name";
	private static final String ATTRIBUTE_CONTENT_TYPE = "contentType";
	private static final String ATTRIBUTE_ELEMENT_REF_TYPE = "elementRefType";
	private static final String ATTRIBUTE_ICON = "icon";
	private static final String ATTRIBUTE_DESCRIPTION = "description";

	private static List<IGeneratorDescriptor> descriptors;

	private static class GeneratorDescriptor implements IGeneratorDescriptor {

		private final IConfigurationElement configElement;

		private URL path;

		GeneratorDescriptor(IConfigurationElement configElement) {
			this.configElement = configElement;
		}

		@Override
		public ISCTGenerator createGenerator() {
			try {
				return (ISCTGenerator) configElement.createExecutableExtension(ATTRIBUTE_CLASS);
			} catch (CoreException e) {
				e.printStackTrace();
			}
			return null;
		}

		@Override
		public List<String> getLibraryIDs() {
			List<String> libs = new ArrayList<String>();
			for (IConfigurationElement child : configElement.getChildren(LIBRARY_CONFIG_ELEMENT)) {
				String lib_id = child.getAttribute(ATTRIBUTE_LIBRARY_ID);
				if (lib_id != null && !lib_id.isEmpty()) {
					libs.add(lib_id);
				}
			}
			return libs;
		}

		@Override
		public String getId() {
			return configElement.getAttribute(ATTRIBUTE_ID);
		}

		@Override
		public String getName() {
			return configElement.getAttribute(ATTRIBUTE_NAME);
		}

		@Override
		public URL getImagePath() {
			if (path != null)
				return path;
			String pathString = configElement.getAttribute(ATTRIBUTE_ICON);
			path = Platform.getBundle(configElement.getContributor().getName()).getEntry(pathString);
			return path;
		}

		@Override
		public String getContentType() {
			return configElement.getAttribute(ATTRIBUTE_CONTENT_TYPE);
		}

		@Override
		public String getDescription() {
			return configElement.getAttribute(ATTRIBUTE_DESCRIPTION);
		}

		@Override
		public String getElementRefType() {
			try {
				return configElement.getAttribute(ATTRIBUTE_ELEMENT_REF_TYPE);
			} catch (Exception e) {
				e.printStackTrace();
				return null;
			}
		}

	}

	public static List<IGeneratorDescriptor> getGeneratorDescriptors() {
		if (descriptors == null) {
			descriptors = Lists.newArrayList();
			if (Platform.isRunning()) {
				initFromExtensions();
			}
		}
		return descriptors;
	}

	protected static void initFromExtensions() {
		IConfigurationElement[] configurationElements = Platform.getExtensionRegistry()
				.getConfigurationElementsFor(EXTENSION_POINT_ID);
		for (IConfigurationElement iConfigurationElement : configurationElements) {
			descriptors.add(new GeneratorDescriptor(iConfigurationElement));
		}
	}

	/**
	 * returns the Generator Descriptor for the given generator id, or null, if
	 * the id is unknown
	 */
	public static IGeneratorDescriptor getGeneratorDescriptor(final String generatorId) {
		try {
			return Iterables.find(getGeneratorDescriptors(), new Predicate<IGeneratorDescriptor>() {
				public boolean apply(IGeneratorDescriptor input) {
					return input != null && input.getId() != null && input.getId().equals(generatorId);
				}
			});
		} catch (NoSuchElementException ex) {
			return null;
		}
	}

}
