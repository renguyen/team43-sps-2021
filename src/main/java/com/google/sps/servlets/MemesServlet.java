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

@WebServlet("/memes")
@MultipartConfig
public class MemesServlet extends HttpServlet {
  public final String PROJECT_ID = "spring21-sps-43";
  public final String BUCKET_NAME = "spring21-sps-43.appspot.com"

  @Override
  public void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
    
    // Get the file chosen by the user.
    Part filePart = request.getPart("image");
    String fileName = UUID.randomUUID().toString();
    InputStream fileInputStream = filePart.getInputStream();
    
    // Upload the file and get its URL
    String uploadedFileUrl = uploadToCloudStorage(fileName, fileInputStream);

    // Output some HTML that shows the data the user entered.
    // You could also store the uploadedFileUrl in Datastore instead.
    PrintWriter out = response.getWriter();
    out.println("<p>Your Meme:</p>");
    out.println("<a href=\"" + uploadedFileUrl + "\">");
    out.println("<img src=\"" + uploadedFileUrl + "\" />");
    out.println("</a><br>");
  }

    // Uploads a file to Cloud Storage and returns the uploaded file's URL. 
    private static String uploadToCloudStorage(String fileName, InputStream fileInputStream) {
    Storage storage = StorageOptions.newBuilder().setProjectId(PROJECT_ID).build().getService();
    BlobId blobId = BlobId.of(BUCKET_NAME, fileName);
    BlobInfo blobInfo = BlobInfo.newBuilder(blobId).build();
    
    // Upload the file to Cloud Storage.
    Blob blob = storage.create(blobInfo, fileInputStream);

    // Return the uploaded file's URL.
    return blob.getMediaLink();
  }


  @Override
  public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
    response.setContentType("text/html;");
    
    // List all of the uploaded files.
    Storage storage = StorageOptions.newBuilder().setProjectId(PROJECT_ID).build().getService();
    Bucket bucket = storage.get(BUCKET_NAME);
    Page<Blob> blobs = bucket.list();

    // Output <img> elements as HTML.
    for (Blob blob : blobs.iterateAll()) {
      String imgTag = String.format("<img src=\"%s\" width = \"300\"/>", blob.getMediaLink());
      response.getWriter().println(imgTag);
    }
  }
}
