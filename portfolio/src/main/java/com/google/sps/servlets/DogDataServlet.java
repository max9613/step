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
    String dog = getParameter(request, "dog");

    if (dog != null && (dog.equals("zoe") || dog.equals("teddy")))
    {
        long timestamp = System.currentTimeMillis();
        Query query = new Query(dog).addSort("timestamp", SortDirection.DESCENDING);
        DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
        Iterable<Entity> results = datastore.prepare(query).asIterable();
        Entity result = null;
        // Gets first entity if there is one and clears out the rest.
        for (Entity entity : results) {
            if (result == null) {
                result = entity;
            } else {
                datastore.delete(entity.getKey());
            }
        }
        long count = 0;
        if (result != null) {
            count = (long) result.getProperty("votes");
            result.setProperty("votes", count + 1);
        } else {
            result = new Entity(dog);
            result.setProperty("timestamp", timestamp);
            result.setProperty("votes", count);
        }
        count += 1;
        datastore.put(result);
        System.err.println(dog + " " + count);
    }
  }

  @Override
  public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
    response.setContentType("text/html;");
    long teddyCount = 0;
    Query teddyQuery = new Query("teddy").addSort("timestamp", SortDirection.DESCENDING);
    DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
    PreparedQuery teddyResults = datastore.prepare(teddyQuery);
    for (Entity entity : teddyResults.asIterable()) {
        long count = (long) entity.getProperty("votes");
        if (count > teddyCount) {
            teddyCount = count;
        }
    }
    long zoeCount = 0;
    Query zoeQuery = new Query("zoe").addSort("timestamp", SortDirection.DESCENDING);
    PreparedQuery zoeResults = datastore.prepare(zoeQuery);
    for (Entity entity : zoeResults.asIterable()) {
        long count = (long) entity.getProperty("votes");
        if (count > zoeCount) {
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



