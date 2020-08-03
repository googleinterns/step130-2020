// Copyright 2019 Google LLC
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     https://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package com.google.sps.servlets;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import javax.servlet.http.HttpServletResponse;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.EmbeddedEntity;
import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;
import com.google.sps.data.GivrUser;
import com.google.sps.data.EventUpdater;
import com.google.sps.data.HistoryManager;
import java.io.IOException;
import java.time.Instant;

@WebServlet("/edit-event")
public class EditEventServlet extends HttpServlet {

  public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
    GivrUser user = GivrUser.getCurrentLoggedInUser();

    if (!user.isLoggedIn()) {
      throw new IllegalArgumentException("ERROR: Unable to edit event if user is not logged in.");
    }

    //Get the proper event entity for updating
    DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
    long eventId = Long.parseLong(request.getParameter("id"));
    Key eventKey = KeyFactory.createKey("Event", eventId);
    Entity eventEntity = null;

    // try catch for compilation purposes, servlet will not be called without a valid id param
    try {
      eventEntity = datastore.get(eventKey);
    } catch(com.google.appengine.api.datastore.EntityNotFoundException err) {
        response.sendError(HttpServletResponse.SC_NOT_FOUND);
        return;
    }

    EventUpdater eventUpdater = new EventUpdater(eventEntity);
    long millisecondsSinceEpoch = Instant.now().toEpochMilli();
    HistoryManager history = new HistoryManager();
    EmbeddedEntity historyUpdate = history.recordHistory("Event was edited.", millisecondsSinceEpoch);

    // Update rest of Event properties from request form. Will not update event if user that
    // requested is not a moderator for the event or a maintainer
    try {
      eventUpdater.updateEvent(request, user, /* forRegistration */ false, historyUpdate);
    } catch (IllegalArgumentException err) {
      response.sendError(HttpServletResponse.SC_NOT_FOUND);
      return;
    }

    eventEntity = eventUpdater.getEntity();

    datastore.put(eventEntity);

    response.sendRedirect("/events.html");
  }
}
