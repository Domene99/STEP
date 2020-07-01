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

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.EntityNotFoundException;
import com.google.appengine.api.datastore.FetchOptions;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;
import com.google.appengine.api.datastore.PreparedQuery;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.api.datastore.Query.SortDirection;
import com.google.gson.Gson;
import com.google.sps.classes.Comment;
import java.io.IOException;
import static java.lang.Math.toIntExact;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.List;

@WebServlet("/comment")
public class DataServlet extends HttpServlet {
  private final DatastoreService dataStore = DatastoreServiceFactory.getDatastoreService();
  private Comment comment;

  @Override
  public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
    response.setContentType("application/json");
    Query query = new Query("Comment").addSort("likes", SortDirection.DESCENDING);
    query.addSort("time", SortDirection.DESCENDING);

    PreparedQuery comments = dataStore.prepare(query);
    List<Comment> commentsToSet = new ArrayList<>();
    for (Entity entity : comments.asIterable()) {
        comment = new Comment(entity);

        commentsToSet.add(comment);
    }

    Gson gson = new Gson();
    String payload = gson.toJson(commentsToSet);

    response.getWriter().println(payload);
  }

  @Override
  public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
    response.setContentType("application/json");
    String commentText = request.getParameter("comment");
    String user = request.getParameter("user");
    long commentSize = commentText.length();
    long time = System.currentTimeMillis();

    comment = new Comment(commentText, user, commentSize, 0, time);

    dataStore.put(comment.toEntity());

    String payload = new Gson().toJson(comment);
    response.getWriter().println(payload);

    response.sendRedirect("/index.html");
  }

  @Override
  public void doDelete(HttpServletRequest request, HttpServletResponse response) throws IOException {
    response.setContentType("application/json;");

    try {
      Key key = KeyFactory.createKey("Comment", Long.parseLong(request.getHeader("id")));
      dataStore.delete(key);
    } catch (Exception e) {
      System.out.println(e);
    }

    response.sendRedirect("/index.html");
  }

  private String getParameter(HttpServletRequest request, String parameter, String defaultValue) {
    String value = request.getParameter(parameter);
    if (value == null) {
        return defaultValue;
    }

    return value;
  }
}
