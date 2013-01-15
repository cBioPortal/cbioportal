/** Copyright (c) 2012 Memorial Sloan-Kettering Cancer Center.
**
** This library is free software; you can redistribute it and/or modify it
** under the terms of the GNU Lesser General Public License as published
** by the Free Software Foundation; either version 2.1 of the License, or
** any later version.
**
** This library is distributed in the hope that it will be useful, but
** WITHOUT ANY WARRANTY, WITHOUT EVEN THE IMPLIED WARRANTY OF
** MERCHANTABILITY OR FITNESS FOR A PARTICULAR PURPOSE.  The software and
** documentation provided hereunder is on an "as is" basis, and
** Memorial Sloan-Kettering Cancer Center 
** has no obligations to provide maintenance, support,
** updates, enhancements or modifications.  In no event shall
** Memorial Sloan-Kettering Cancer Center
** be liable to any party for direct, indirect, special,
** incidental or consequential damages, including lost profits, arising
** out of the use of this software and its documentation, even if
** Memorial Sloan-Kettering Cancer Center 
** has been advised of the possibility of such damage.  See
** the GNU Lesser General Public License for more details.
**
** You should have received a copy of the GNU Lesser General Public License
** along with this library; if not, write to the Free Software Foundation,
** Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA.
**/

// package
package org.mskcc.cbio.importer.util;

// imports
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.lang.reflect.Method;
import java.lang.reflect.Constructor;

/**
 * Class which provides class loader services.
 */
public class ClassLoader {

	// our logger
	private static Log LOG = LogFactory.getLog(ClassLoader.class);

	/**
	 * Method to return the given class method (if it exists), null otherwise.
	 * It assumes that there are no overloaded functions in the class.
	 *
	 * @param className String
	 * @param methodName String
	 * @return Method
	 */
	public static Method getMethod(String className, String methodName) {

		// sanity check
		if (className == null || className.length() == 0) {
			throw new IllegalArgumentException("className must not be null");
		}
		if (methodName == null || methodName.length() == 0) {
			throw new IllegalArgumentException("methodName must not be null");
		}

		if (LOG.isInfoEnabled()) {
			LOG.info("getMethod(), className:methodName " + className + ":" + methodName);
		}
		
		Method toReturn = null;
		try {
			Class<?> clazz = Class.forName(className);
			for (Method method : clazz.getMethods()) {
				if (method.getName().equals(methodName)) {
					toReturn = method;
					break;
				}
			}
		}
		catch (Exception e) {
			if (LOG.isInfoEnabled()) {
				LOG.info("Cannot find class ormethod in class.");
			}
		}

		// outta here
		return toReturn;
	}
	

	/**
	 * Creates a new instance of given class with given arguments.
	 *
	 * @param className String
	 * @param args Object[]
	 * @return Object
	 */
	public static Object getInstance(String className, Object[] args) throws Exception {

		// sanity check
		if (className == null || className.length() == 0) {
			throw new IllegalArgumentException("className must not be null");
		}

		if (LOG.isInfoEnabled()) {
			LOG.info("getInstance(), className: " + className);
		}

		try {
			Class<?> clazz = Class.forName(className);
			Constructor[] constructors = clazz.getConstructors();
			// our classes only have the one constructor
			return constructors[0].newInstance(args);
		}
		catch (Exception e) {
			LOG.error(("Failed to instantiate " + className), e) ;
			throw e;
		}
	}
}
