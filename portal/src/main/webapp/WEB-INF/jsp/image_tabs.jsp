<%@ page import="java.util.HashSet" %>
<%@ page import="java.util.ArrayList" %>
<%@ page import="org.mskcc.cbio.portal.servlet.QueryBuilder" %>
<%@ page import="org.mskcc.cbio.cgds.model.GeneticProfile" %>
<%@ page import="org.mskcc.cbio.cgds.model.GeneticAlterationType" %>
<%
      String imageDataTypes[] = { "PROTEIN_LEVEL:Protein Level", "PHOSPHORYLATION:Phosphorylation" }; 
      for( String imageDataTypeAndDesc : imageDataTypes ){
         String fields[] = imageDataTypeAndDesc.split( ":" );
         String imageDataType = fields[0];
         String desc = fields[1];
         
         // an image Tab Should Only Appear if a User Selects the Data Type in Step 2
         // use profileList
         GeneticAlterationType theGeneticAlterationType =
                 GeneticAlterationType.getType( imageDataType );

         for(GeneticProfile theGeneticProfile : profileList){
            if( theGeneticProfile.getGeneticAlterationType() == theGeneticAlterationType &&
                     geneticProfileIdSet.contains( theGeneticProfile.getStableId())){
               out.println("<li class=\"ui-state-default ui-corner-top\"><a href='#" + imageDataType + "'>" + desc + "</a></li>");
            }
         }
      }
%>
      