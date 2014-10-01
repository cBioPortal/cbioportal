/** Copyright (c) 2012 Memorial Sloan-Kettering Cancer Center.
 *
 * This library is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY, WITHOUT EVEN THE IMPLIED WARRANTY OF
 * MERCHANTABILITY OR FITNESS FOR A PARTICULAR PURPOSE.  The software and
 * documentation provided hereunder is on an "as is" basis, and
 * Memorial Sloan-Kettering Cancer Center 
 * has no obligations to provide maintenance, support,
 * updates, enhancements or modifications.  In no event shall
 * Memorial Sloan-Kettering Cancer Center
 * be liable to any party for direct, indirect, special,
 * incidental or consequential damages, including lost profits, arising
 * out of the use of this software and its documentation, even if
 * Memorial Sloan-Kettering Cancer Center 
 * has been advised of the possibility of such damage.
*/

package org.mskcc.cbio.portal.util;

import java.io.IOException;
import java.util.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;

/**
* Wrapper for a file upload request.
* 
* <P>This class uses the Apache Commons 
* <a href='http://commons.apache.org/fileupload/'>File Upload tool</a>.
* The generous Apache License will very likely allow you to use it in your 
* applications as well. 
*/
public class FileUploadRequestWrapper extends HttpServletRequestWrapper {
  
  /** Constructor.  */
  public FileUploadRequestWrapper(HttpServletRequest aRequest) throws IOException {
    super(aRequest);
    ServletFileUpload upload = new ServletFileUpload( new DiskFileItemFactory());
    try {
      List<FileItem> fileItems = upload.parseRequest(aRequest);
      convertToMaps(fileItems);
    }
    catch(FileUploadException ex){
      throw new IOException("Cannot parse underlying request: " + ex.toString());
    }
  }
  
  /**
  * Return all request parameter names, for both regular controls and file upload 
  * controls.
  */
  @Override public Enumeration getParameterNames() {
    Set<String> allNames = new LinkedHashSet<String>();
    allNames.addAll(fRegularParams.keySet());
    allNames.addAll(fFileParams.keySet());
    return Collections.enumeration(allNames);
  }
  
  /**
  * Return the parameter value. Applies only to regular parameters, not to 
  * file upload parameters. 
  * 
  * <P>If the parameter is not present in the underlying request, 
  * then <tt>null</tt> is returned.
  * <P>If the parameter is present, but has no  associated value, 
  * then an empty string is returned.
  * <P>If the parameter is multivalued, return the first value that 
  * appears in the request.  
  */
  @Override public String getParameter(String aName) {
    String result = null;
    List<String> values = fRegularParams.get(aName);
    if( values == null ){
      //you might try the wrappee, to see if it has a value 
    }
    else if ( values.isEmpty() ) {
      //param name known, but no values present
      result = "";
    }
    else {
      //return first value in list
      result = values.get(FIRST_VALUE);
    }
    return result;
  }
  
  /**
  * Return the parameter values. Applies only to regular parameters, 
  * not to file upload parameters.
  */
  @Override public String[] getParameterValues(String aName) {
    String[] result = null;
    List<String> values = fRegularParams.get(aName);
    if( values != null ) {
      result = values.toArray(new String[values.size()]);
    }
    return result;
  }
  
  /**
  * Return a {@code Map<String, String>} for all regular parameters.
  * Does not return any file upload paramters at all. 
  */
  @Override public Map getParameterMap() {
    return Collections.unmodifiableMap(fRegularParams);
  }
  
  /**
  * Return a {@code List<FileItem>}, in the same order as they appear
  *  in the underlying request.
  */
  public List<FileItem> getFileItems(){
    return new ArrayList<FileItem>(fFileParams.values());
  }
  
  /**
  * Return the {@link FileItem} of the given name.
  * <P>If the name is unknown, then return <tt>null</tt>.
  */
  public FileItem getFileItem(String aFieldName){
    return fFileParams.get(aFieldName);
  }
  
  
  // PRIVATE //
  
  /** Store regular params only. May be multivalued (hence the List).  */
  private final Map<String, List<String>> fRegularParams = 
    new LinkedHashMap<String, List<String>>()
  ;
  /** Store file params only. */
  private final Map<String, FileItem> fFileParams = 
    new LinkedHashMap<String, FileItem>()
  ;
  private static final int FIRST_VALUE = 0;
  
  private void convertToMaps(List<FileItem> aFileItems){
    for(FileItem item: aFileItems) {
      if ( isFileUploadField(item) ) {
        fFileParams.put(item.getFieldName(), item);
      }
      else {
        if( alreadyHasValue(item) ){
          addMultivaluedItem(item);
        }
        else {
          addSingleValueItem(item);
        }
      }
    }
  }
  
  private boolean isFileUploadField(FileItem aFileItem){
    return ! aFileItem.isFormField();
  }
  
  private boolean alreadyHasValue(FileItem aItem){
    return fRegularParams.get(aItem.getFieldName()) != null;
  }
  
  private void addSingleValueItem(FileItem aItem){
    List<String> list = new ArrayList<String>();
    list.add(aItem.getString());
    fRegularParams.put(aItem.getFieldName(), list);
  }
  
  private void addMultivaluedItem(FileItem aItem){
    List<String> values = fRegularParams.get(aItem.getFieldName());
    values.add(aItem.getString());
  }
} 