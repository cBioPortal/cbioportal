

package org.mskcc.cbio.importer.extractor;

import com.google.common.base.Function;
import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.Lists;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

/** Copyright (c) 2014 Memorial Sloan-Kettering Cancer Center.
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
* @author fcriscuo
*
*/
/**
 * Represents a source of data for the import process
 * @author fcriscuo
 */
public class FileDataSource {
 private final String directoryName;
 private List<Path> filenameList;
 
 public FileDataSource(String dirName){
     Preconditions.checkArgument(!Strings.isNullOrEmpty(dirName), "A directory name is required");
     this.directoryName = dirName;
     this.filenameList = Lists.newArrayList();
 }
 
 public String getDirectoryName() { return this.directoryName;}
 
 public List<Path> getFilenameList() { return this.filenameList;}
 
 void setFilenameList (List<Path> aList){
     Preconditions.checkArgument(null != aList, "The filename list cannot be null");
     this.filenameList = aList;
 }
  
  
   
}
