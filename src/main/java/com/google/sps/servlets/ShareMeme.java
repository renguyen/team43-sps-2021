package com.google.sps.servlets;

import com.google.api.gax.paging.Page;

import com.google.cloud.storage.Blob;
import com.google.cloud.storage.BlobId;
import com.google.cloud.storage.BlobInfo;
import com.google.cloud.storage.Bucket;
import com.google.cloud.storage.Storage;
import com.google.cloud.storage.StorageOptions;
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
import java.util.*;

@WebServlet("/share")
@MultipartConfig
public class ShareMeme extends HttpServlet {
  public static final String PROJECT_ID = "spring21-sps-43";
  public static final String BUCKET_NAME = "spring21-sps-43.appspot.com";

  /**
  Handles the upload of the meme to the Cloud Storage
  it require an HTTP request and response from the client-side and will return a 
  string containing the HTML of the response that will contain the meme URL that will be displayed in the page
  **/
  
  @Override
  public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
    response.setContentType("text/html;");
    
    // List all of the uploaded files.
    Storage storage = StorageOptions.newBuilder().setProjectId(PROJECT_ID).build().getService();
    Bucket bucket = storage.get(BUCKET_NAME);
    Page<Blob> blobs = bucket.list();
    String memeName = request.getParameter("meme");
    // Output <img> elements as HTML.
    response.getWriter().println("<div id=\"fb-root\"></div><script async defer crossorigin=\"anonymous\" src=\"https://connect.facebook.net/en_US/sdk.js#xfbml=1&version=v10.0\" nonce=\"ZRUaVmmR\"></script>");
    for (Blob blob : blobs.iterateAll()) {

      if(blob.getName().equals(memeName)){
      String imgTag = String.format("<img src=\"%s\" width = \"300\"/>", blob.getMediaLink());
      response.getWriter().println(imgTag);
      response.getWriter().println("<div class=\"fb-share-button\" data-href=\"" + "https://spring21-sps-43.appspot.com/share?meme=" + blob.getName() + "\" data-layout=\"button_count\"></div>");
      }
    }
  }
  
}
