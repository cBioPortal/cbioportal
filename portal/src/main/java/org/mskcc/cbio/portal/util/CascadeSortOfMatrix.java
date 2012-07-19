package org.mskcc.portal.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;

//import org.mskcc.portal.test.util.TestCascadeSortOfMatrix;

//import static org.junit.Assert.assertArrayEquals;

/**
 * Given a matrix represented by an ArrayList of rows in ArrayLists, sort the
 * columns so that the values in the rows are arranged in a 'cascade', that is,
 * the elements in row i are sorted, and within each group of constant values in
 * row i, the elements in row i+1 are sorted, etc. Used to present the data in our
 * 'CancerPrint's.
 * 
 * TODO: To scale for large datasets, convert matrix into a set of columns, and radix sort.
 * @author Arthur Goldberg  goldberg@cbio.mskcc.org
 */
public class CascadeSortOfMatrix {

   private Comparator<Object> theObjectComparator;
   
   /**
    * Construct as CascadeSortOfMatrix. 
    * 
    * @param aObjectComparator a Comparator that compares elements of the matrix that will be Cascade sorted, i.e., the argument to sort().
    * It will eventually be passed in a call to 
    * 
    * arrayListData, used for sorting.
    */
   public CascadeSortOfMatrix(Comparator<Object> aObjectComparator) {
      theObjectComparator = aObjectComparator;
   }
   
   /**
    * Perform the Cascade sort.
    * This data documents the concept. It shows the state of the sort before each call to sortExtent, and before the return 
    * from sort. 

Original data:
  2  1  2  1  2
  4  5  5  4  4
  8  8  8  8  7

The first call to sortExtent sorts row 0:
  1  1  2  2  2
  5  4  4  5  4
  8  8  8  8  7

Next the first extent is sorted in row 1:
  1  1  2  2  2
  4  5  4  5  4
  8  8  8  8  7

Then the 2nd extent is sorted in row 1:
  1  1  2  2  2
  4  5  4  4  5
  8  8  8  7  8

Finally, the first and only extent of rows 0 and 1 is sorted in row 2:
  1  1  2  2  2
  4  5  4  4  5
  8  8  7  8  8

    * 
    * @param arrayData
    * @return
    */

   public Object[][] sort( Object[][] arrayData) {

      // CascadeSort
      // sort columns by row 1
      int width = arrayData[0].length; // assume a rectangular matrix
      
      // DEFINITION of cumulativeChanges: after processing row r (cumulativeChanges[i] == true) <==> 
      // the value in some row <= r changed between column i-1 and column i 
      boolean[] cumulativeChanges = new boolean[width]; 

      sortExtent(arrayData, 0, 0, width );
      recordChangeInThisRowOfData( arrayData, 0, 0, width, cumulativeChanges );

      for (int rowIndex = 1; rowIndex < arrayData.length; rowIndex++) {

         // an 'extent' is a sequence of adjacent elements in previousRow
         // that have the same value
         int extentStart = 0; // index of first element in an extent
         int nextExtentStart = 0; // index of first element in next extent
         
         while( true ){
            nextExtentStart++;

            // ALL previous rows must be the same across the entire extent
            while( (nextExtentStart < width) && !cumulativeChanges[nextExtentStart] ){
               nextExtentStart++;
            }

            sortExtent(arrayData, rowIndex, extentStart, nextExtentStart);
            recordChangeInThisRowOfData( arrayData, rowIndex, extentStart, nextExtentStart, cumulativeChanges );
            
            extentStart = nextExtentStart;
            if (nextExtentStart == width ) {
               break;
            }
         }
         
      }

      return arrayData;
   }
   
   /*
    * Record and accumulate (as a true bit) in cumulativeChanges columns at which data changes; 
    * that is, the cumulativeChanges is true for each column in which an extent ends. 
    * see DEFINITION of cumulativeChanges
    */
   private void recordChangeInThisRowOfData( Object[][] data, int row, int extentStart, int nextExtentStart, boolean[] cumulativeChanges ){

      for( int col=extentStart+1; col<nextExtentStart; col++){
         if( !data[row][col-1].equals( data[row][col] ) ){
            cumulativeChanges[col] = true;
         }
      }
   }
   
   /* 
    * was used for internal unit tests that recordChangeInThisRowOfData works

   private boolean[] changesInPreviousRows( Object[][] matrix, int lastPreviousRow ){
      int w = matrix[0].length;
      boolean[] changePoints = new boolean[ w ];
      for( int row = 0; row <= lastPreviousRow; row++ ){
         for( int col = 1; col < w; col++ ){
            if( !(matrix[row][col].equals( matrix[row][col-1] ))){
               changePoints[col]=true;
            }
         }
      }
      return changePoints;
   }

    */
   
   /**
    * Given the matrix 'data', a row rowIndex and a range of columns from
    * extentStart to extentEnd, arrange data so that the elements of columns
    * [extentStart, extentEnd] are sorted by the values in row rowIndex.
    * 
    * @param data
    * @param rowIndex
    * @param extentStart
    * @param extentEnd
    * @return
    */
   public Object[][] sortExtent(Object[][] data, int rowIndex, int extentStart, int extentEnd) {

      /*
       * approach: 
       * 1) sort range of row rowIndex: 
       * copy data[rowIndex][extentStart, extentEnd] to an array, 
       * mark the order of elements, and then sort it 
       * 2) order columns by sort: rearrange order of all
       * columns by the sort for row rowIndex
       */
      
      // sort range of row rowIndex
      int len = extentEnd - extentStart;
      if (len < 2) {
         return data;
      }
      //System.out.println( TestCascadeSortOfMatrix.matrixToString( data, 3 ) );

      ElementAndIndex[] extentCopy = new ElementAndIndex[len];
      for (int i = 0; i < len; i++) {
         extentCopy[i] = new ElementAndIndex( data[rowIndex][i + extentStart], i);
      }

      //System.out.println( "extentCopy is:\n" + pToString(extentCopy ));
      SortExtentComparator mySortExtentComparator = new SortExtentComparator( theObjectComparator);
      Arrays.sort(extentCopy, mySortExtentComparator);
      //System.out.println( "sorted extentCopy is:\n" + pToString(extentCopy ));

      // order columns by sort
      Object[] buffer = new Object[len];
      for (int row = 0; row < data.length; row++) {

         // copy row to buffer and copy back in right order
         System.arraycopy(data[row], extentStart, buffer, 0, len);

         for (int offset = 0; offset < len; offset++) {
            data[row][extentStart+offset] = buffer[extentCopy[offset].index];
         }
      }

      return data;
   }
   
   /**
    * just copy a matrix from and ArrayList representation to an Array representation
    * @param arrayListData
    * @return
    */
   // NOT USED
   public static Object[][] copyToArrayOfArray( ArrayList<ArrayList<Object>> arrayListData ) {
      Object[][] data = new Object[arrayListData.size()][arrayListData.get(0).size()];
      int rowNum = 0;
      for (ArrayList<Object> row : arrayListData) {
         int colNum = 0;
         for (Object value : row) {
            data[rowNum][colNum++] = value;
         }
         rowNum++;
      }
      return data;
   }
   
   /**
    * just copy a matrix from and Array representation to an ArrayList representation
    * @param arrayListData
    * @return
    */
   // NOT USED
   public static ArrayList<ArrayList<Object>> copyToArrayListOfArrayList( Object[][] arrayData ) {

      ArrayList<ArrayList<Object>> arrayListData = new ArrayList<ArrayList<Object>>();
      
      for (int i = 0; i < arrayData.length; i++) {
         ArrayList<Object> row = new ArrayList<Object>();
         for (int j = 0; j < arrayData[i].length; j++) {
            row.add( arrayData[ i ][ j ] );
         }
         arrayListData.add( row );
      }
      return arrayListData;
   }  
   
   // for debugging
   // NOT USED
   public String pToString( ElementAndIndex[] extentCopy ){
      StringBuffer sb = new StringBuffer(); 

      for( ElementAndIndex e : extentCopy ){
         sb.append( e.toString() + "\n" );
      }
      return sb.toString();
   }
}

/**
 * Little class to hold the pair (Object, index), so that objects and their
 * original position can be found after sorting.
 * 
 * @author arthurgoldberg
 */
class ElementAndIndex {
   Object element;
   int index;

   ElementAndIndex(Object e, int i) {
      element = e;
      index = i;
   }
   public String toString(){
      return( "element: " + element.toString() + "; index: " + index );
   }
}

/**
 * compare ElementAndIndex objects for sorting, etc.
 * 
 * @author arthurgoldberg
 */
class SortExtentComparator implements Comparator<ElementAndIndex> {
   private Comparator<Object> theObjectComparator;

   SortExtentComparator(Comparator<Object> c) {
      theObjectComparator = c;
   }

   public int compare(ElementAndIndex e1, ElementAndIndex e2) {
      return (theObjectComparator.compare( e1.element, e2.element ));
   }
}
