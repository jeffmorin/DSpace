/**
 * $Id: NotProvider.java 3533 2009-03-06 12:06:27Z azeckoski $
 * $URL: https://scm.dspace.org/svn/repo/dspace2/core/trunk/utils/src/main/java/org/dspace/utils/servicemanager/NotProvider.java $
 * NotProvider.java - DS2 - Mar 6, 2009 11:40:10 AM - azeckoski
 **************************************************************************
 * Copyright (c) 2002-2009, The Duraspace Foundation.  All rights reserved.
 * Licensed under the Duraspace Foundation License.
 * 
 * A copy of the Duraspace License has been included in this
 * distribution and is available at: http://scm.dspace.org/svn/repo/licenses/LICENSE.txt
 *
 * 
 */

package org.dspace.utils.servicemanager;


/**
 * This is a special marker interface which is used to indicate that this
 * service is not a provider and thus should not be included in the provider stacks
 * which are being setup and stored, any service which implements this interface
 * will not be able to be added to the provider stack, in some cases this results in
 * an exception but it mostly just results in the object being ignored and not placed
 * into the stack
 * 
 * @author Aaron Zeckoski (azeckoski @ gmail.com)
 */
public interface NotProvider {

    // This area intentionally left blank

}
