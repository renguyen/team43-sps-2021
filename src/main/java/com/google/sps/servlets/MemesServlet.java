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
<<<<<<< HEAD
  public final String PROJECT_ID = "spring21-sps-43";
  public final String BUCKET_NAME = "spring21-sps-43.appspot.com"
=======
  static final String PROJECT_ID = "spring21-sps-43";
  static final String BUCKET_NAME = "spring21-sps-43.appspot.com";
>>>>>>> ac2cd0ffba474995ca5e94bdfe941ce924b86195

  /**
  Handles the upload of the meme to the Cloud Storage
  it require an HTTP request and response from the client-side and will return a 
  string containing the HTML of the response that will contain the meme URL that will be displayed in the page
  **/
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

    /**
    Handles the uploads a file to Cloud Storage and returns the uploaded file's URL. 
    It require a string of the file name and the image itself to be uploaded and will return a string containing the URL of the image 
    **/
    private static String uploadToCloudStorage(String fileName, InputStream fileInputStream) {
    Storage storage = StorageOptions.newBuilder().setProjectId(PROJECT_ID).build().getService();
    BlobId blobId = BlobId.of(BUCKET_NAME, fileName);
    BlobInfo blobInfo = BlobInfo.newBuilder(blobId).build();
    
    // Upload the file to Cloud Storage.
    Blob blob = storage.create(blobInfo, fileInputStream);

    // Return the uploaded file's URL.
    return blob.getMediaLink();
  }

  /**
  Handles the display of all the memes from the Cloud Storage
  it require an HTTP request and response from the client-side and will return a 
  string containing the HTML of the response that will contain all the memes URL in almost the same size
  this will be require in the same for show the meme in the main page
  **/
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

/** This funtions  randomly  get a string  captions from an array of string. When the function is called
 * it randomly return memes caption from an array
 * input: null
 * return: strings..
 * 
 */
  

  public static String getCaption(){
//     initilaise an array  of captions
        final String[] proper_noun = {"Fred", 
        
        "When the professor is passionate about teaching and you genuinely understan and enjoy the class.", 
        " When ur best friend calls u and have some gossip to tell u.", 
        "When you go off on him and later on realize you were actually wrong",
        "When you tell everyone about your summer body goals, but you didn’t tell them which summer",

        "  When you’ve been eating healthy for the past 15 minutes and STILL see no progress.",

        
    
    };
    //   import the random module
        Random random = new Random();
        // get random  index of array element
        int index = random.nextInt(proper_noun.length);
       // System.out.println(proper_noun[index]);

    //    return random caption
        return proper_noun[index];
        
        

  }
}
