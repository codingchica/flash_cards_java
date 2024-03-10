package com.codingchica.flashcards.component.model;

import io.cucumber.core.options.CurlOption;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Map;
import java.util.TreeMap;

/** This class houses the state data that needs to be passed between cucumber steps. */
public class APICallWorld {
  public String protocol = null;
  public String server = "localhost";
  public int port = 0;
  public String path = null;
  public String endpoint = null;
  public CurlOption.HttpMethod httpMethod = null;
  public Map<String, String> requestHeaders = new TreeMap<>();
  public URL url = null;
  public HttpURLConnection connection;
  public String responseBody = null;
}
