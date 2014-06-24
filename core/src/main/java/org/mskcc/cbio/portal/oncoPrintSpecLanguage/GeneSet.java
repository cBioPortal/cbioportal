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

package org.mskcc.cbio.portal.oncoPrintSpecLanguage;

import java.util.ArrayList;

import org.mskcc.cbio.portal.util.EqualsUtil;

/**
 * Stores either a single gene with an OncoPrint spec, or a set of genes in
 * order.
 * 
 * @author Arthur Goldberg
 */
public class GeneSet {
    private String name;
    private boolean userGeneList;
    private ArrayList<GeneWithSpec> Genes;

    public GeneSet() {
        Genes = new ArrayList<GeneWithSpec>();
    }

    public GeneSet(String name) {
        this.name = name;
        Genes = new ArrayList<GeneWithSpec>();
    }

    /**
     * If this set contains just one gene, return it.
     * @return
     */
    public GeneWithSpec getGene() {
        //System.out.println( "getGene Genes.size(): " + Genes.size());
        
        if (Genes.size() == 1) {
            return Genes.get(0);
        } else {
            return null;
        }
    }

    public void addGeneWithSpec(GeneWithSpec aGeneWithSpec) {
       if( null != aGeneWithSpec){
          Genes.add(aGeneWithSpec);
       }
    }
    
    /**
     * return the names of genes in this GeneSet; may contain duplicates
     * @return
     */
    public ArrayList<String> listOfGenes(){
       ArrayList<String> allGenes = new ArrayList<String>();
       for( GeneWithSpec gene: Genes ){
          if( null != gene.name ){
             allGenes.add( gene.name.toUpperCase());
          }
      }
       return allGenes;
    }
    
    /**
     * return the GeneWithSpec for the first gene with the given name, if any
     * TODO: as the same gene could appear multiple times in a spec this should return a set of GeneWithSpec; for now return the 1st
     * @param geneName
     * @return first GeneWithSpec with a gene with that name, else if none, null
     */
    public GeneWithSpec getGeneWithSpec( String geneName ){
       for( GeneWithSpec gene: Genes ){
          if( null != gene.name ){
             if( gene.name.equalsIgnoreCase( geneName ) ){
                return gene; 
             }
          }
      }
       return null;
    }
    
    @Override
    public String toString(){
        StringBuffer sb = new StringBuffer();
        if (Genes.size() == 1) {
            return( this.getGene().toString() );
            
        } else {
            if( this.getName() != null ){
                // sb.append( Genes.size() ).append(" genes in '").append(this.getName()).append("':\n");
                sb.append("\"").append(this.getName()).append("\"").append( userGeneListToString() );
            }else{
                if( this.userGeneList ){
                    sb.append( userGeneListToString() );
                } else{
                    // sb.append( Genes.size() ).append(" genes:").append("\n");
                    for( GeneWithSpec gene: Genes ){
                        sb.append(gene.toString());
                    }
                }
            }
        }
        return sb.toString();
    }

    public void setUserGeneList(String name ) {
       
       this.userGeneList = true;
       if( null != name ){
          this.setName(name);
       }
       //System.out.println( "setUserGeneList: " + this.toString());
   }

    private String userGeneListToString(){
        StringBuffer sb = new StringBuffer();
        sb.append("{\n");
        for( GeneWithSpec gene: Genes ){
            sb.append(gene.toString());
        }
        sb.append("\n}\n");
        return sb.toString();
    }

    public void setName(String name) {
        this.name = stripQuotes( name );
        this.userGeneList = true;
    }
    
    private String stripQuotes( String s){
        return s.replaceAll( "\"", "");
    }

    public ArrayList<GeneWithSpec> getGenes() {
        return Genes;
    }

    public String getName() {
        return name;
    }

    public boolean isUserGeneList() {
        return userGeneList;
    }

    public void setUserGeneList(boolean userGeneList) {
        this.userGeneList = userGeneList;
    }

    /*
     * TODO: finish implementing 
          this.Genes.equals(that.Genes);
          
    @Override
    public boolean equals( Object aThat ) {
       if ( this == aThat ) return true;
       if ( !(aThat instanceof GeneSet) ) return false;
       GeneSet that = (GeneSet) aThat;
       return
          EqualsUtil.areEqual(this.name, that.name) &&
          EqualsUtil.areEqual(this.userGeneList, that.userGeneList) &&
          this.Genes.equals(that.Genes);
    }
     */
       
}