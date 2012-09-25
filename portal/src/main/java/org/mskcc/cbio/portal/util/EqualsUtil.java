/** Copyright (c) 2012 Memorial Sloan-Kettering Cancer Center.
**
** This library is free software; you can redistribute it and/or modify it
** under the terms of the GNU Lesser General Public License as published
** by the Free Software Foundation; either version 2.1 of the License, or
** any later version.
**
** This library is distributed in the hope that it will be useful, but
** WITHOUT ANY WARRANTY, WITHOUT EVEN THE IMPLIED WARRANTY OF
** MERCHANTABILITY OR FITNESS FOR A PARTICULAR PURPOSE.  The software and
** documentation provided hereunder is on an "as is" basis, and
** Memorial Sloan-Kettering Cancer Center 
** has no obligations to provide maintenance, support,
** updates, enhancements or modifications.  In no event shall
** Memorial Sloan-Kettering Cancer Center
** be liable to any party for direct, indirect, special,
** incidental or consequential damages, including lost profits, arising
** out of the use of this software and its documentation, even if
** Memorial Sloan-Kettering Cancer Center 
** has been advised of the possibility of such damage.  See
** the GNU Lesser General Public License for more details.
**
** You should have received a copy of the GNU Lesser General Public License
** along with this library; if not, write to the Free Software Foundation,
** Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA.
**/

package org.mskcc.cbio.portal.util;

/**
 * Collected methods which allow easy implementation of <code>equals</code>.
 * 
 * From http://www.javapractices.com/topic/TopicAction.do?Id=17
 *
 * Example use case in a class called Car:
 * <pre>
 public boolean equals(Object aThat){
   if ( this == aThat ) return true;
   if ( !(aThat instanceof Car) ) return false;
   Car that = (Car)aThat;
   return
     EqualsUtil.areEqual(this.fName, that.fName) &&
     EqualsUtil.areEqual(this.fNumDoors, that.fNumDoors) &&
     EqualsUtil.areEqual(this.fGasMileage, that.fGasMileage) &&
     EqualsUtil.areEqual(this.fColor, that.fColor) &&
     Arrays.equals(this.fMaintenanceChecks, that.fMaintenanceChecks); //array!
 }
 * </pre>
 *
 * <em>Arrays are not handled by this class</em>.
 * This is because the <code>Arrays.equals</code> methods should be used for
 * array fields.
 */
 public final class EqualsUtil {

   static public boolean areEqual(boolean aThis, boolean aThat){
     //System.out.println("boolean");
     return aThis == aThat;
   }

   static public boolean areEqual(char aThis, char aThat){
     //System.out.println("char");
     return aThis == aThat;
   }

   static public boolean areEqual(long aThis, long aThat){
     /*
     * Note that byte, short, and int are handled by this method, through
     * implicit conversion.
     */
     return aThis == aThat;
   }

   static public boolean areEqual(float aThis, float aThat){
     return Float.floatToIntBits(aThis) == Float.floatToIntBits(aThat);
   }

   static public boolean areEqual(double aThis, double aThat){
     return Double.doubleToLongBits(aThis) == Double.doubleToLongBits(aThat);
   }

   /**
   * Possibly-null object field.
   *
   * Includes type-safe enumerations and collections, but does not include
   * arrays. See class comment.
   */
   static public boolean areEqual(Object aThis, Object aThat){
     return aThis == null ? aThat == null : aThis.equals(aThat);
   }
 }
  
