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
  public static final String LINK = "https://spring21-sps-43.appspot.com/share?meme=";

  /**
  Handles the projection of only one meme. It search with the parameter of memeName the meme with that name
  and return and HTML with the image and the FACEBOOK and TWITTER share buttons of that link
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
      response.getWriter().println("<br><div class=\"fb-share-button\" data-href=\"" + LINK + blob.getName() + "\" data-layout=\"button_count\"></div>");
      response.getWriter().println("<a href=\"https://twitter.com/intent/tweet?ref_src=twsrc%5Etfw\" class=\"twitter-hashtag-button\" data-url=\"" + LINK + blob.getName() +  "data-show-count=\"false\"></a><script async src=\"https://platform.twitter.com/widgets.js\" charset=\"utf-8\"></script>");
      }
    }
  }
  
}
