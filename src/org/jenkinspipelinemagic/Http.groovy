package org.jenkinspipelinemagic

@Grab(group='org.apache.httpcomponents', module='httpclient', version='4.5.2')
import groovy.json.*
import org.apache.http.client.methods.*
import org.apache.http.entity.*
import org.apache.http.impl.client.*
import org.apache.http.client.config.RequestConfig

/**
 * Perform an HTTP rest action and return a map of the results.
 */
def restRequest(url, method="GET", jsonBody=null) {
  if (method == "POST") {
    if (jsonBody) {
      if(jsonBody instanceof String == false) {
        jsonBody = JsonOutput.toJson(jsonBody)
      }
    }
    def postMethod = new HttpPost(url)
    postMethod.addHeader(
      "Content-type", 
      "application/json"
    )
    postMethod.setEntity(new StringEntity(jsonBody))
    return _executeRestMethod(
      postMethod
    )
  }
  if (method == "GET") {
    return _executeRestMethod(
      new HttpGet(url)
    )
  }
  if (method=="PUT") {
    if (jsonBody) {
      if(jsonBody instanceof String == false) {
        jsonBody = JsonOutput.toJson(jsonBody)
      }
    }
    def putMethod = new HttpPut(url)
    putMethod.addHeader(
      "Content-type", 
      "application/json"
    )
    putMethod.setEntity(new StringEntity(jsonBody))
    return _executeRestMethod(
      putMethod
    )
  }
  if (method=="DELETE") {
    return _executeRestMethod(
      new HttpDelete(url)
    ) 
  }
}

/**
 * Perform the dirty work of executing the rest method.
 */
def _executeRestMethod(httpMethodObject) {
  def timeout = 5*1000
  RequestConfig requestConfig = RequestConfig.custom().setConnectTimeout(timeout).setSocketTimeout(timeout).build()

  httpMethodObject.setConfig(requestConfig)

  def client = HttpClientBuilder.create().build()
  def response = client.execute(httpMethodObject)
  def bufferedReader = new BufferedReader(new InputStreamReader(response.getEntity().getContent()))
  def responseBody = bufferedReader.getText()
  def slurper = new JsonSlurper()
  def content = null
  try {
    content = new groovy.json.JsonSlurperClassic().parseText(responseBody)
  } catch (err) {
    // Swallow the error since many rest endpoints do not return a json 
    // document.
  }
  return [
    "status": response.getStatusLine().getStatusCode(),
    "content": content
  ]
}
