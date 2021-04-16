package main.java.com.google.sps.servlets;

import com.google.api.gax.paging.Page;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.Part;

import com.google.cloud.datastore.Datastore;
import com.google.cloud.datastore.DatastoreOptions;
import com.google.cloud.datastore.Entity;
import com.google.cloud.datastore.FullEntity;
import com.google.cloud.datastore.KeyFactory;
import com.google.cloud.datastore.Query;
import com.google.cloud.datastore.QueryResults;
import com.google.cloud.datastore.StructuredQuery.OrderBy;

import java.util.*;

@WebServlet("/comment")
@MultipartConfig
public class CommentsServlet extends HttpServlet {

  /** doGet()
   * Gets a usercomments from datastore kind 'Comment' (which are organized by time posted).
   * Prints all comments stored in datastore.
   */
  @Override
  public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {

    response.setContentType("text/html;");
    response.getWriter().println("<h1>Comments:</h1>");
    response.getWriter().println("<ul>"); // <ul> --> 'unordered list element'
    
    Datastore datastore = DatastoreOptions.getDefaultInstance().getService();

    // Setting an order by time posted
    Query<Entity> query = Query.newEntityQueryBuilder()
      .setKind("Comment")
      .setOrderBy(OrderBy.desc("timestamp"))
      .build();
    QueryResults<Entity> results = datastore.run(query);

    // Basicallly stored like a list. This is printing comments.
    while (results.hasNext()) {
      Entity entity = results.next();
      String message = entity.getString("comment");
      response.getWriter().println("<li>" + message + "</li>");
    }
    response.getWriter().println("</ul>");
    //response.getWriter().println("<p><a href=\"/\">Back</a></p>"); // tbh a bit confused about this.
    
    
  }

  /** doPost()
   * Gets a user's inputed comment and adds it to datastore along with its timestamp.
   * It then calls doGet() so the user has an immediate view of their comment now posted. 
   */
  @Override
  public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {

    // Getting comment input
    String comment = request.getParameter("comment");

    // Storing ordered by time posted
    Datastore datastore = DatastoreOptions.getDefaultInstance().getService();
    KeyFactory keyFactory = datastore.newKeyFactory().setKind("Comment");
    FullEntity commentEntity = Entity.newBuilder(keyFactory.newKey())
      .set("comment", comment)
      .set("timestamp", System.currentTimeMillis())
      .build();
    datastore.put(commentEntity); // Adding comment to Data Store
 
    // Redirect to /comment.
    // The request will be routed to the doGet() function.
    // response.sendRedirect("/comment");

  }

}