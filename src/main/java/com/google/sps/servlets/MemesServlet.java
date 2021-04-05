package com.google.sps.servlets;

import com.google.api.gax.paging.Page;

import com.google.cloud.datastore.Datastore;
import com.google.cloud.datastore.DatastoreOptions;
import com.google.cloud.datastore.Entity;
import com.google.cloud.datastore.FullEntity;
import com.google.cloud.datastore.KeyFactory;
import com.google.cloud.datastore.Query;
import com.google.cloud.datastore.QueryResults;
import com.google.cloud.datastore.StructuredQuery.OrderBy;
import org.jsoup.Jsoup;
import com.google.cloud.vision.v1.AnnotateImageRequest;
import com.google.cloud.vision.v1.AnnotateImageResponse;
import com.google.cloud.vision.v1.BatchAnnotateImagesResponse;
import com.google.cloud.vision.v1.EntityAnnotation;
import com.google.cloud.vision.v1.Feature;
import com.google.cloud.vision.v1.Image;
import com.google.cloud.vision.v1.ImageAnnotatorClient;
import com.google.protobuf.ByteString;
import org.jsoup.safety.Whitelist;
import com.google.cloud.storage.Blob;
import com.google.cloud.storage.BlobId;
import com.google.cloud.storage.BlobInfo;
import com.google.cloud.storage.Bucket;
import com.google.cloud.storage.Storage;
import com.google.cloud.storage.StorageOptions;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.time.ZonedDateTime;
import java.time.format.*;
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
  public static final String PROJECT_ID = "spring21-sps-43";
  public static final String BUCKET_NAME = "spring21-sps-43.appspot.com";

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

    // Get tags for image
    InputStream fileInput = filePart.getInputStream();
    byte[] imageBytes = fileInput.readAllBytes();
    List<EntityAnnotation> imageLabels = getImageLabels(imageBytes);
    List<String> tags = new ArrayList<String>();
    for (int i = 0; i < 5; i++) {
        tags.add(imageLabels.get(i).getDescription());
    }

    // get the current date
    ZonedDateTime date = ZonedDateTime.now();
    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss");
    String dateString = date.format(formatter);

    // Create a datastore to store the meme
    // Replace caption, likes, and user with functions when ready
    Datastore datastore = DatastoreOptions.getDefaultInstance().getService();
    KeyFactory keyFactory = datastore.newKeyFactory().setKind("Meme");
    FullEntity memeEntity =
        Entity.newBuilder(keyFactory.newKey())
            .set("id", createId())
            .set("url", uploadedFileUrl)
            .set("caption","Hello World!")
            .set("tags", tags.toString())
            .set("likes", 0l)
            .set("upload-date", dateString)
            .set("user", "unknown")
            .build();
    datastore.put(memeEntity);

    // Output some HTML that shows the data the user entered.
    // You could also store the uploadedFileUrl in Datastore instead.
    PrintWriter out = response.getWriter();
    out.println("<head>");
    out.println("<link rel=\"stylesheet\" href=\"style.css\">");
    out.println("</head>");
    out.println("<div id=\"content\">");
    out.println("<h1 class=\"main-page-title \">Meme Generator</h1>");
    out.println("<a href=\"" + uploadedFileUrl + "\">");
    out.println("<img class=\"center-image\" src=\"" + uploadedFileUrl + "\" />");
    out.println("</a><br>");
    out.println("<p class=\"body-text\">");
    out.println("Auto-generated tags: " + tags.toString());
    out.println("</p>");
    out.println("<form action=\"/memes\" method=\"POST\" enctype=\"multipart/form-data\">");
    out.println("<label for=\"uploadImage\" class=\"upload-button\" style=\"top: 25px;\">Upload Meme</label>");
    out.println("<input type=\"file\" value=\"Upload Meme\" id=\"uploadImage\" name=\"image\" class=\"hidden\" onchange=\"form.submit()\">");
    out.println("</form>");
    out.println("<a href=\"/memes\" class=\"no-underline\">");
    out.println("<p class=\"see-memes-button\" style=\"top: 35px;\">See All Memes</p>");
    out.println("</a>");
  }

  /** createId() checks how many Memes are in datastore, and returns that number.
   *  Example: If the datastore has no memes: the first time createId() is called will return 0.
   *           If datastore contains one meme: createId() will return 1.
   *           If datastore contains 154 memes: createId() will return 154.
   */
  private static int createId() {
    Datastore datastore = DatastoreOptions.getDefaultInstance().getService();
    Query<Entity> query = Query.newEntityQueryBuilder().setKind("Meme").build();
    QueryResults<Entity> results = datastore.run(query);

    int counter = 0;
    while (results.hasNext()) {
        Entity entity = results.next();
        counter++;
    }
    return counter;
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
    
    PrintWriter out = response.getWriter();
    out.println("<head>");
    out.println("<link rel=\"stylesheet\" href=\"style.css\">");
    out.println("</head>");
    out.println("<div id=\"content\">");
    out.println("<a href=\"/\" class=\"no-underline\">");
    out.println("<h1 class=\"main-page-title \">Meme Generator</h1>");
    out.println("</a>");
    out.println("<a class=\"body-text\">");

    // Instantiate Datastore
    Datastore datastore = DatastoreOptions.getDefaultInstance().getService();
    Query<Entity> query = Query.newEntityQueryBuilder().setKind("Meme").build();
    QueryResults<Entity> results = datastore.run(query);

    // Output every meme stored in DataStore
    while (results.hasNext()) {
        Entity entity = results.next();
        out.println("<img class=\"center-image\" src=\"" + entity.getString("url") + "\"/>");
        out.println("<p>User: " + entity.getString("user") + "</p>");
        out.println("<p>Uploaded: " + entity.getString("upload-date"));
        out.println("<p>Likes: " + Long.toString(entity.getLong("likes")));
        out.println("<p>Autogenerated caption: " + entity.getString("caption") + "</p>");
        out.println("<p>Tags: "+ entity.getString("tags") + "</p>");
    }
    out.println("</a>");
    
    // // List all of the uploaded files.
    // Storage storage = StorageOptions.newBuilder().setProjectId(PROJECT_ID).build().getService();
    // Bucket bucket = storage.get(BUCKET_NAME);
    // Page<Blob> blobs = bucket.list();

    // // Output <img> elements as HTML.
    // for (Blob blob : blobs.iterateAll()) {
    //   String imgTag = String.format("<img src=\"%s\" width = \"300\"/>", blob.getMediaLink());
    //   response.getWriter().println(imgTag);
    // }
  }

  /**
   * Uses the Google Cloud Vision API to generate a list of labels that apply to the image
   * represented by the binary data stored in imgBytes.
   */
  private List<EntityAnnotation> getImageLabels(byte[] imageBytes) throws IOException {
        ByteString byteString = ByteString.copyFrom(imageBytes);
        Image image = Image.newBuilder().setContent(byteString).build();

        Feature feature = Feature.newBuilder().setType(Feature.Type.LABEL_DETECTION).build();
        AnnotateImageRequest request =
            AnnotateImageRequest.newBuilder().addFeatures(feature).setImage(image).build();
        List<AnnotateImageRequest> requests = new ArrayList<>();
        requests.add(request);

        ImageAnnotatorClient client = ImageAnnotatorClient.create();
        BatchAnnotateImagesResponse batchResponse = client.batchAnnotateImages(requests);
        client.close();
        List<AnnotateImageResponse> imageResponses = batchResponse.getResponsesList();
        AnnotateImageResponse imageResponse = imageResponses.get(0);

        if (imageResponse.hasError()) {
            System.err.println("Error getting image labels: " + imageResponse.getError().getMessage());
            return null;
        }

        return imageResponse.getLabelAnnotationsList();
  }
}
