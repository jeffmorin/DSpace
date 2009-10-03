/**
 * $Id: ServiceManagerReadyAware.java 3497 2009-02-25 17:39:08Z azeckoski $
 * $URL: https://scm.dspace.org/svn/repo/dspace2/core/trunk/api/src/main/java/org/dspace/kernel/mixins/ServiceManagerReadyAware.java $
 * ServiceManagerReadyAware.java - DS2 - Feb 25, 2009 11:41:35 AM - azeckoski
 **************************************************************************
 * Copyright (c) 2002-2009, The Duraspace Foundation.  All rights reserved.
 * Licensed under the Duraspace Foundation License.
 * 
 * A copy of the Duraspace License has been included in this
 * distribution and is available at: http://scm.dspace.org/svn/repo/licenses/LICENSE.txt
 *
 * 
 */

package org.dspace.kernel.mixins;

import org.dspace.kernel.ServiceManager;

/**
 * This service manager mixin will cause a service to be notified when the servicemanager has started
 * up all known services (including activators), this is useful if a service wants to do lookups
 * of all known services or providers of a certain type when the system startup is complete,
 * it can also be used as a way to execute some code when the service manager startup is complete
 * 
 * @author Aaron Zeckoski (azeckoski @ gmail.com)
 */
public interface ServiceManagerReadyAware {

    /**
     * This is called when the startup of all core services and activators is complete <br/>
     * WARNING: This will never be called for providers which are registered with a servicemanager
     * which is already running!
     * 
     * @param serviceManager the completely started service manager
     */
    public void serviceManagerReady(ServiceManager serviceManager);

}
