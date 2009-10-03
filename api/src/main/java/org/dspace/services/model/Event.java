/**
 * $Id: Event.java 3688 2009-04-07 10:06:23Z grahamtriggs $
 * $URL: https://scm.dspace.org/svn/repo/dspace2/core/trunk/api/src/main/java/org/dspace/services/model/Event.java $
 * Event.java - DSpace2 - Oct 9, 2008 2:22:48 PM - azeckoski
 **************************************************************************
 * Copyright (c) 2002-2009, The Duraspace Foundation.  All rights reserved.
 * Licensed under the Duraspace Foundation License.
 * 
 * A copy of the Duraspace License has been included in this
 * distribution and is available at: http://scm.dspace.org/svn/repo/licenses/LICENSE.txt
 *
 * 
 */

package org.dspace.services.model;

import java.io.Serializable;
import java.util.Map;


/**
 * This holds all the settings related to an event in the system
 * 
 * @author Aaron Zeckoski (azeckoski @ gmail.com)
 */
public class Event implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * The event scopes to send events to
     */
    public static enum Scope {LOCAL, CLUSTER, EXTERNAL};

    private String id;
    private String name;
    private String userId;
    private String resourceReference;
    private Scope[] scopes;
    private boolean modify = false;
    private Map<String, String> properties;

    /**
     * Create a new event with the given name,
     * will default to a read event (instead of modify event),
     * also defaults to the current user and local/cluster scope
     * 
     * @param name an event name (e.g. collection.read, item.modify)
     */
    public Event(String name) {
        this(name, null, null, null, null, false);
    }

    /**
     * Create a new event with the given name for the given resource (item, collection, etc.),
     * defaults to the current user and local/cluster scope
     * 
     * @param name an event name (e.g. collection.read, item.modify)
     * @param resourceReference the reference (identifier) for the resource affected by this event
     * @param modify if true then this is a modify event, if false it is just a read event
     */
    public Event(String name, String resourceReference, boolean modify) {
        this(name, resourceReference, null, null, null, modify);
    }

    /**
     * Create a new event with the given name for the given resource (item, collection, etc.),
     * defaults to the current user and local/cluster scope
     * 
     * @param name an event name (e.g. collection.read, item.modify)
     * @param resourceReference the reference (identifier) for the resource affected by this event
     * @param modify if true then this is a modify event, if false it is just a read event
     * @param localOnly if true then this is a local event only, otherwise it defaults to local/cluster
     */
    public Event(String name, String resourceReference, boolean modify, boolean localOnly) {
        this(name, resourceReference, null, null, (localOnly ? new Scope[] {Scope.LOCAL} : null), modify);
    }

    /**
     * Create a new event with the given name for the given user on the given resource (item, collection, etc.)
     * 
     * @param name an event name (e.g. collection.read, item.modify)
     * @param resourceReference the reference (identifier) for the resource affected by this event
     * @param userId the internal user id for the user who caused this event
     * @param modify if true then this is a modify event, if false it is just a read event
     */
    public Event(String name, String resourceReference, String userId, boolean modify) {
        this(name, resourceReference, userId, null, null, modify);
    }

    /**
     * Create a new event with the given name for the given user on the given resource (item, collection, etc.)
     * 
     * @param name an event name (e.g. collection.read, item.modify)
     * @param resourceReference the reference (identifier) for the resource affected by this event
     * @param userId the internal user id for the user who caused this event
     * @param properties additional properties to send along with this event
     * @param scopes the scopes to send this event to
     * @param modify if true then this is a modify event, if false it is just a read event
     */
    public Event(String name, String resourceReference, String userId, Map<String, String> properties, Scope[] scopes, boolean modify) {
        this.name = name;
        this.userId = userId;
        this.modify = modify;
        this.resourceReference = resourceReference;
        this.properties = properties;
        if (scopes == null || scopes.length == 0) {
            this.scopes = new Scope[] {Scope.LOCAL, Scope.CLUSTER};
        } else {
            this.scopes = scopes.clone();
        }
    }

    /**
     * @return the unique identifier for this event (generated by the system)
     */
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    /**
     * @return the name of this event (e.g. collection.modify, item.delete)
     */
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    /**
     * @return the internal user id for the user who caused this event
     */
    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    /**
     * @return the reference (identifier) for the resource affected by this event OR null if none specified
     */
    public String getResourceReference() {
        return resourceReference;
    }

    public void setResourceReference(String resourceReference) {
        this.resourceReference = resourceReference;
    }

    /**
     * @return true if this event modified soemthing in the system, false if it was read only
     */
    public boolean isModify() {
        return modify;
    }

    public void setModify(boolean modify) {
        this.modify = modify;
    }

    /**
     * @return the additional properties included with this event if there are any
     */
    public Map<String, String> getProperties() {
        return properties;
    }

    public void setProperties(Map<String, String> properties) {
        this.properties = properties;
    }

    /**
     * @return the scopes the this event is sent to
     */
    public Scope[] getScopes() {
        return scopes == null ? null : scopes.clone();
    }

    public void setScopes(Scope[] scopes) {
        this.scopes = (scopes == null ? null : scopes.clone());
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < scopes.length; i++) {
            if (sb.length() > 0) {
                sb.append(",");
            }
            sb.append(scopes[i]);
        }
        return id+":name="+name+":user="+userId+":resRef="+resourceReference+":mod="+modify+":scopes="+sb;
    }

}
