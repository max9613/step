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

import java.io.IOException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import com.google.gson.Gson;
import java.util.ArrayList;
import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.PreparedQuery;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.api.datastore.Query.SortDirection;

/** Servlet that handles dog voting */
@WebServlet("/dog-data")
public class DogDataServlet extends HttpServlet {

  @Override
  public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
    // Get the input from the form.
    String dogName = getParameter(request, "dog");

    if (dogName != null)
    {
        long timestamp = System.currentTimeMillis();
        Query query = new Query("dog").addSort("timestamp", SortDirection.DESCENDING);
        DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
        Iterable<Entity> results = datastore.prepare(query).asIterable();
        Entity result = null;
        // Gets entity for passed in dog.
        for (Entity entity : results) {
            if (result == null && ((String) (entity.getProperty("name"))).equals(dogName)) {
                result = entity;
            }
        }
        if (result != null) {
            long count = (long) result.getProperty("votes");
            result.setProperty("votes", count + 1);
        } else { // If the entity does not exist, creates one.
            result = new Entity("dog");
            result.setProperty("timestamp", timestamp);
            result.setProperty("name", dogName);
            result.setProperty("votes", 1);
        }
        datastore.put(result);
    }
  }

  @Override
  public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
    response.setContentType("text/html;");
    long teddyCount = 0;
    long zoeCount = 0;
    Query query = new Query("dog").addSort("timestamp", SortDirection.DESCENDING);
    DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
    PreparedQuery results = datastore.prepare(query);
    for (Entity entity : results.asIterable()) {
        long count = (long) entity.getProperty("votes");
        String name = (String) entity.getProperty("name");
        if (name.equals("teddy") && count > teddyCount) {
            teddyCount = count;
        } else if (name.equals("zoe") && count > zoeCount) {
            zoeCount = count;
        }
    }
    ArrayList<Long> votes = new ArrayList<Long>();
    votes.add(teddyCount);
    votes.add(zoeCount);
    Gson gson = new Gson();
    String output = gson.toJson(votes); 
    response.getWriter().println(output);
  }

  /**
   * @return the request parameter, or the default value if the parameter
   *         was not specified by the client
   */
  private String getParameter(HttpServletRequest request, String name) {
    String value = request.getParameter(name);
    return value;
  }
}



