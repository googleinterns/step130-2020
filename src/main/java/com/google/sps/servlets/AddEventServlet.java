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
import com.google.sps.data.GivrUser;
import com.google.sps.data.EventUpdater;
import com.google.sps.data.HistoryManager;
import java.io.IOException;
import java.time.Instant;

@WebServlet("/add-event")
public class AddEventServlet extends HttpServlet {

  public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
    GivrUser user = GivrUser.getCurrentLoggedInUser();

    if (!user.isLoggedIn()) {
      throw new IllegalArgumentException("ERROR: Unable to register event if user is not logged in.");
    }

    // Only Moderators can add Events.
    if (user.isModeratorOfAnyOrg()) {
      // TODO: Can Maintainers add Events?
      throw new IllegalArgumentException("ERROR: User does not have the permission to perform addition of Events.");
    }

    Entity newEventEntity = new Entity("Event");

    EventUpdater eventUpdater = new EventUpdater(newEventEntity);
    long millisecondsSinceEpoch = Instant.now().toEpochMilli();
    HistoryManager history = new HistoryManager();
    EmbeddedEntity historyUpdate = history.recordHistory("Event was registered.", millisecondsSinceEpoch);

    // Update rest of Event properties from request form.
    try {
      eventUpdater.updateEvent(request, user, true /* forRegistration */,historyUpdate);
    } catch (IllegalArgumentException err) {
      response.sendError(HttpServletResponse.SC_NOT_FOUND);
      return;
    }

    newEventEntity = eventUpdater.getEntity();

    DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
    datastore.put(newEventEntity);

    response.sendRedirect("/index.html");
  }
}
