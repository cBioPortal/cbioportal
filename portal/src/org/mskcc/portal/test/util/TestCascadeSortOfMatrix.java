package org.mskcc.portal.test.util;


import static org.junit.Assert.assertArrayEquals;

import java.util.Comparator;
import java.util.Formatter;
import java.util.Locale;
import java.util.Random;

import junit.framework.TestCase;

import org.junit.Test;
import org.mskcc.portal.model.GeneticEvent;
import org.mskcc.portal.model.GeneticEventComparator;
import org.mskcc.portal.model.GeneticEventImpl;
import org.mskcc.portal.util.CascadeSortOfMatrix;

public class TestCascadeSortOfMatrix extends TestCase{
   
   // TODO: add a performance test with a 10000 x 10000 matrix

   @Test
   public void testSortWithGeneticEvents() {

      GeneticEventComparator aGeneticEventComparator = new GeneticEventComparator(
            GeneticEventComparator.defaultCNASortOrder(),
            GeneticEventComparator.defaultMRNASortOrder(),
            GeneticEventComparator.defaultRPPASortOrder(),
            GeneticEventComparator.defaultMutationsSortOrder());

      CascadeSortOfMatrix c = new CascadeSortOfMatrix( aGeneticEventComparator );
      
      // make a matrix OF GeneticEventImpls
      int rows = 3;
      int cols = 4;
      GeneticEvent[][] m= new GeneticEvent[rows][cols]; 
      for( int i=0; i<rows; i++ ){
         for( int j=0; j<cols; j++ ){
            GeneticEventImpl ge1 = new GeneticEventImpl( 1, 1, true );
            ge1.setGeneticEventComparator(aGeneticEventComparator);
            m[i][j] = ge1; 
         }
      }
      Object[][] m2 = c.sort( (Object[][])m);
      assertArrayEquals( m, m2 );
      
     
   }

   @Test
   public void testSort() {

      IntegerComparator ic = new IntegerComparator();
      CascadeSortOfMatrix c = new CascadeSortOfMatrix( ic );

      Object[][] m2 = trySort( c, initM() );
      Object[][] e1 = {
         {   1,    1,    2,    2,    3, },
         {   1,    2,    1,    4,    3, },
         {   2,    1,    4,    3,    2, },
      };
      assertArrayEquals( e1, m2 );

      Object[][] m3 = {
            {  1, 1, 1, 1, 1 },
            {  3, 1, 2, 2, 1 },
            {  3, 2, 4, 1, 1 },
            {  2, 1, 3, 4, 2 },
      };
      m2 = trySort( c, m3 );
      Object[][] e2 = {
         {   1,    1,    1,    1,    1, },
         {   1,    1,    2,    2,    3, },
         {   1,    2,    1,    4,    3, },
         {   2,    1,    4,    3,    2, },
      };
      assertArrayEquals( e2, m2 );
   
      Object[][] m4 = {
            {  3, 1, 2, 2, 1 },
            {  1, 1, 1, 1, 1 },
            {  3, 2, 4, 1, 1 },
            {  2, 1, 3, 4, 2 },
      };
      m2 = trySort( c, m4 );
      Object[][] e4 = {
         {   1,    1,    2,    2,    3, },
         {   1,    1,    1,    1,    1  },
         {   1,    2,    1,    4,    3, },
         {   2,    1,    4,    3,    2, },
      };
      assertArrayEquals( e4, m4 );
   
      Integer[][] m5 = {
            {  2, 1, 2, 1, 2 },
            {  4, 5, 5, 4, 4 },
            {  8, 8, 8, 8, 7 },
      };
      m2 = trySort( c, m5 );
   }
   
   private Object[][] trySort( CascadeSortOfMatrix c, Object[][] m ){
      Object[][] m2 = c.sort( m );
      //System.out.println( "input is:\n" + matrixToString( m, 5 ) );
      //System.out.println( "output is:\n" + matrixToString( m2, 5 ) );
      return m2;
   }

   @Test
   public void testSortExtent() {

      IntegerComparator ic = new IntegerComparator();
      CascadeSortOfMatrix c = new CascadeSortOfMatrix( ic );

      Object[][] m = initM();

      Object[][] m2 = (Object[][])c.sortExtent( m, 0, 0, 5 );
      Object[][] e = {
         {   1,    1,    2,    2,    3, },
         {   2,    1,    4,    1,    3, },
         {   1,    2,    3,    4,    2, },
      };
      assertArrayEquals( e, m2 );
      
      // won't change matrix
      m = initM();
      m2 = (Object[][])c.sortExtent( m, 0, 4, 5 );
      assertArrayEquals( m, m2 );
      
      // 2nd row
      m = initM();
      m2 = (Object[][])c.sortExtent( m, 1, 1, 4 );

      Object[][] e2 = {
         {   3,    2,    1,    2,    1, },
         {   3,    1,    2,    4,    1, },
         {   2,    4,    1,    3,    2, },
      };
      assertArrayEquals( e2, m2 );
      
      // last row
      m = initM();
      //System.out.println( "m is:\n" + matrixToStringForInit( m ) );
      m2 = (Integer[][])c.sortExtent( m, 2, 0, 3 );
      //System.out.println( "m2 is:\n" + matrixToStringForInit( m2 ) );

      Integer[][] e3 = {
         {   1,    3,    2,    2,    1, },
         {   2,    3,    4,    1,    1, },
         {   1,    2,    3,    4,    2, },
      };
      assertArrayEquals( e3, m2 );
      
      // last row again
      m = initM();
      //System.out.println( "m is:\n" + matrixToStringForInit( m ) );
      m2 = (Integer[][])c.sortExtent( m, 2, 1, 5 );
      //System.out.println( "m2 is:\n" + matrixToStringForInit( m2 ) );

      Integer[][] e4 = {
         {   3,    1,    1,    2,    2, },
         {   3,    2,    1,    4,    1, },
         {   2,    1,    2,    3,    4, },            
      };
      assertArrayEquals( e4, m2 );
      
   }
   
   private Integer[][] initM(){
      Integer[][] m = {
            {  3, 1, 2, 2, 1 },
            {  3, 2, 4, 1, 1 },
            {  2, 1, 3, 4, 2 },
      };
      return m;
   }

   @Test
   public void testCopyBetweenToArrayOfArrayAndArrayListOfArrayList() {

      // known matrices
      checkMatrixConversionAndInverse( makeMatrix( 3, 1, 5 ) );
      checkMatrixConversionAndInverse( makeMatrix( 1, 3, 5 ) );
      checkMatrixConversionAndInverse( makeMatrix( 4, 3, 5 ) );
      // random matrices
      checkMatrixConversionAndInverse( makeRandomMatrix( 5 ) );
      checkMatrixConversionAndInverse( makeRandomMatrix( 20 ) );
      checkMatrixConversionAndInverse( makeRandomMatrix( 79 ) );

   }
   
   public static void checkMatrixConversionAndInverse( Integer[][] m ){
      // TODO: why's the class name needed? fix.
      Object[][] m2 = CascadeSortOfMatrix.copyToArrayOfArray( CascadeSortOfMatrix.copyToArrayListOfArrayList( m ) );
      // System.out.println( "m is:\n" + matrixToString( m, 6 ) );
      // System.out.println( "m2 is:\n" + matrixToString( m2, 6 ) );
      assertArrayEquals( m2, m );
   }

   public static Integer[][] makeRandomMatrix( int size ) {
      Random r = new Random();
      
      Integer[][] data = new Integer[ r.nextInt( size )+1 ][r.nextInt( size )+1 ]; 
      for (int i = 0; i < data.length; i++) {
         for (int j = 0; j < data[i].length; j++) {
            data[i][j] = r.nextInt( size );
         }
      }
      return data;
   }
   
   public static Integer[][] makeMatrix( int w, int h, int x ){
      
      Integer[][] data = new Integer[ w ][ h ]; 
      for (int i = 0; i < data.length; i++) {
         for (int j = 0; j < data[i].length; j++) {
            data[i][j] = x;
         }
      }
      return data;
   }
   
   public static String matrixToString( Object[][] m2, int width ) {
      
      if( m2 instanceof Integer[][] ) {
         StringBuffer sb = new StringBuffer(); 
         // Send all output to the Appendable object sb
         Formatter formatter = new Formatter(sb, Locale.US);
         String format = new String( "%" + width + "d");
         for( Object[] row: m2 ){
            for( Object element: row ){
               formatter.format( format, element);
            }
            sb.append("\n");
         }
         return sb.toString();
      } else {
         return "";
      }
   }
   
   public static String matrixToStringForInit( Integer[][] data ) {
      
      StringBuffer sb = new StringBuffer(); 
      // Send all output to the Appendable object sb
      Formatter formatter = new Formatter(sb, Locale.US);
      int width = 4;
      String format = new String( "%" + width + "d, ");
      sb.append("{\n");
      for( Integer[] row: data ){
         sb.append("{");
         for( Integer element: row ){
            formatter.format( format, element);
         }
         sb.append("},\n");
      }
      sb.append("}\n");
      return sb.toString();
   }
   
}

class IntegerComparator implements Comparator<Object> {

   public int compare(Object o1, Object o2) {
      return ( ((Integer)o1).compareTo( (Integer)o2 ) );
   }

   public boolean equals(Object obj) {
      return (false);
   }
}
