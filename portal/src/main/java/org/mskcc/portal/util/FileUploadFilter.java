package org.mskcc.portal.util;

import java.io.IOException;
import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;

/**
* Filter that wraps an underlying file upload request. 
* 
* <P>This filter should be configured only for those operations that use a 
* file upload request.
*/
public final class FileUploadFilter<W extends HttpServletRequestWrapper> implements Filter {

  public void init(FilterConfig aConfig) throws ServletException {
    //do nothing
  }
  
  public void destroy() {
    //do nothing
  }
  
  public void doFilter(
   ServletRequest aRequest, ServletResponse aResponse, FilterChain aChain
  ) throws IOException, ServletException {
    HttpServletRequest request = (HttpServletRequest) aRequest;
    if ( isFileUploadRequest(request) ) {
      FileUploadRequestWrapper wrapper = new FileUploadRequestWrapper(request);
      aChain.doFilter(wrapper, aResponse);
    }
    else {
      aChain.doFilter(aRequest, aResponse);
    }
  }
  
  private boolean isFileUploadRequest(HttpServletRequest aRequest){
    return     
      aRequest.getMethod().equalsIgnoreCase("POST") && 
      aRequest.getContentType().startsWith("multipart/form-data")
    ;
  }
}