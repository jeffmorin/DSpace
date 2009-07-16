/**
 * $Id: EventServiceTest.java 3540 2009-03-09 12:37:46Z azeckoski $
 * $URL: https://scm.dspace.org/svn/repo/dspace2/core/trunk/impl/src/test/java/org/dspace/services/events/EventServiceTest.java $
 * EventServiceTest.java - DS2 - Nov 20, 2008 12:24:17 PM - azeckoski
 **************************************************************************
 * Copyright (c) 2008 Aaron Zeckoski
 * Licensed under the Apache License, Version 2.0
 * 
 * A copy of the Apache License has been included in this 
 * distribution and is available at: http://www.apache.org/licenses/LICENSE-2.0.txt
 *
 * Aaron Zeckoski (azeckoski @ gmail.com) (aaronz @ vt.edu) (aaron @ caret.cam.ac.uk)
 */

package org.dspace.services.events;

import static org.junit.Assert.*;

import org.dspace.services.EventService;
import org.dspace.services.model.Event;
import org.dspace.utils.DSpace;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * Event service testing (not wrapped in requests)
 * 
 * @author Aaron Zeckoski (azeckoski@gmail.com) - azeckoski - 12:24:17 PM Nov 20, 2008
 */
public class EventServiceTest {

    private EventService eventService;
    //private RequestService requestService;
    private EventListenerNoFilter listenerNoFilter;
    private EventListenerNameFilter listenerNameFilter;
    private EventListenerBothFilters listenerBothFilters;

    @Before
    public void init() {
        eventService = new DSpace().getEventService();
        
        // create the filters
        listenerNoFilter = new EventListenerNoFilter();
        listenerNameFilter = new EventListenerNameFilter();
        listenerBothFilters = new EventListenerBothFilters();
        // register the filters
        eventService.registerEventListener(listenerNoFilter);
        eventService.registerEventListener(listenerNameFilter);
        eventService.registerEventListener(listenerBothFilters);
    }

    @After
    public void tearDown() {
        // need to do this to ensure the resources are freed up by junit
        eventService = null;
        //requestService = null;
        listenerNoFilter = null;
        listenerNameFilter = null;
        listenerBothFilters = null;
    }

    /**
     * Test method for {@link org.dspace.services.events.SystemEventService#fireEvent(org.dspace.services.model.Event)}.
     */
    @Test
    public void testFireEvent() {
        Event event1 = new Event("test.event.read", "test-resource-1", "11111", false);
        Event event2 = new Event("test.event.jump", null, "11111", false);
        Event event3 = new Event("some.event.write", "test-resource-2", "11111", true);
        Event event4 = new Event("some.event.add", "fake-resource-555", "11111", true);
        Event event5 = new Event("test.event.read", "test-resource-2", "11111", false);
        Event event6 = new Event("test.event.read", "fake-resource-555", "11111", false);
        Event event7 = new Event("aaron.event.read", "az-123456", "11111", false);

        eventService.fireEvent( event1 );
        eventService.fireEvent( event2 );
        eventService.fireEvent( event3 );
        eventService.fireEvent( event4 );
        eventService.fireEvent( event5 );
        eventService.fireEvent( event6 );
        eventService.fireEvent( event7 );

        // now check that the listeners were properly triggered
        assertEquals(7, listenerNoFilter.getReceivedEvents().size());
        assertEquals(5, listenerNameFilter.getReceivedEvents().size());
        assertEquals(3, listenerBothFilters.getReceivedEvents().size());

        // now verify the set of events and order
        assertEquals(event1, listenerNoFilter.getReceivedEvents().get(0));
        assertEquals(event2, listenerNoFilter.getReceivedEvents().get(1));
        assertEquals(event3, listenerNoFilter.getReceivedEvents().get(2));
        assertEquals(event4, listenerNoFilter.getReceivedEvents().get(3));
        assertEquals(event5, listenerNoFilter.getReceivedEvents().get(4));
        assertEquals(event6, listenerNoFilter.getReceivedEvents().get(5));
        assertEquals(event7, listenerNoFilter.getReceivedEvents().get(6));

        assertEquals(event1, listenerNameFilter.getReceivedEvents().get(0));
        assertEquals(event2, listenerNameFilter.getReceivedEvents().get(1));
        assertEquals(event5, listenerNameFilter.getReceivedEvents().get(2));
        assertEquals(event6, listenerNameFilter.getReceivedEvents().get(3));
        assertEquals(event7, listenerNameFilter.getReceivedEvents().get(4));

        assertEquals(event1, listenerBothFilters.getReceivedEvents().get(0));
        assertEquals(event2, listenerBothFilters.getReceivedEvents().get(1));
        assertEquals(event5, listenerBothFilters.getReceivedEvents().get(2));
    }

    /**
     * Test method for {@link org.dspace.services.events.SystemEventService#registerEventListener(org.dspace.services.model.EventListener)}.
     */
    @Test
    public void testRegisterEventListener() {
        // check reregister ok
        eventService.registerEventListener(listenerNoFilter);
        eventService.registerEventListener(listenerNameFilter);
        eventService.registerEventListener(listenerBothFilters);

        // check null fails
        try {
            eventService.registerEventListener(null);
            fail("should have thrown exception");
        } catch (IllegalArgumentException e) {
            assertNotNull(e.getMessage());
        }
    }

}
