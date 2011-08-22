<%@ page import="java.util.ArrayList" %>
<%@ page import="java.util.HashSet" %>
<%@ page import="org.mskcc.portal.servlet.QueryBuilder" %>
<%@ page import="org.mskcc.portal.model.ProfileData" %>
<%@ page import="org.mskcc.portal.tool.RenderImageDataType" %>
<%@ page import="org.mskcc.cgds.model.GeneticProfile" %>
<%@ page import="org.mskcc.cgds.model.GeneticAlterationType" %>

<%
      String imageDataTypes[] = { "PROTEIN_LEVEL:Protein Level:protein levels:Protein levels obtained by immunohistochemical staining.",
         "PHOSPHORYLATION:Phosphorylation:phosphorylation data:Phosphorylation obtained by immunohistochemical staining." }; 
      
      for( String imageDataTypeAndDesc : imageDataTypes ){
         String fields[] = imageDataTypeAndDesc.split( ":" );
         String imageDataType = fields[0];
         String missingDataText = fields[2];
         String summary = fields[3];

         // an image Tab Should Only Appear if a User Selects the Data Type in Step 2; use profileList
         GeneticAlterationType theGeneticAlterationType =
                 GeneticAlterationType.getType( imageDataType );
         
         ArrayList<GeneticProfile> profileList2 =
                 (ArrayList<GeneticProfile>) request.getAttribute(QueryBuilder.PROFILE_LIST_INTERNAL);
         HashSet<String> geneticProfileIdSet2 = (HashSet<String>) request.getAttribute(QueryBuilder.GENETIC_PROFILE_IDS);
         for(GeneticProfile theGeneticProfile : profileList2){
            if( theGeneticProfile.getGeneticAlterationType() == theGeneticAlterationType &&
                     geneticProfileIdSet2.contains( theGeneticProfile.getStableId())){

               out.println( "<div class=\"section\" id=\"" + imageDataType + "\">");
               out.println( "<div class=\"map\">");

               String result = RenderImageDataType.render( imageDataType, mergedProfile );
               if( null == result ){
                  out.println( "There are no " + missingDataText + " available for the gene set entered.<p>" );
               }else{
                  out.println( summary + "<p>" );
                  out.println( result );
               }
               
               out.println( "</div>" );
               out.println( "</div>" );
            }
         }
         
      }
%>
