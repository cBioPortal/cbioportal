/*
 * Copyright (c) 2015 Memorial Sloan-Kettering Cancer Center.
 *
 * This library is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY, WITHOUT EVEN THE IMPLIED WARRANTY OF MERCHANTABILITY OR FITNESS
 * FOR A PARTICULAR PURPOSE. The software and documentation provided hereunder
 * is on an "as is" basis, and Memorial Sloan-Kettering Cancer Center has no
 * obligations to provide maintenance, support, updates, enhancements or
 * modifications. In no event shall Memorial Sloan-Kettering Cancer Center be
 * liable to any party for direct, indirect, special, incidental or
 * consequential damages, including lost profits, arising out of the use of this
 * software and its documentation, even if Memorial Sloan-Kettering Cancer
 * Center has been advised of the possibility of such damage.
 */

/*
 * This file is part of cBioPortal.
 *
 * cBioPortal is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
*/

package org.mskcc.cbio.portal.servlet;

import org.mskcc.cbio.portal.model.*;
import org.mskcc.cbio.portal.dao.*;
import org.mskcc.cbio.portal.util.*;
import org.apache.log4j.Logger;
import org.codehaus.jackson.map.ObjectMapper;

import java.io.*;
import java.util.*;
import javax.servlet.http.*;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;

/**
 *
 * @author jj
 */
public class CnaJSON extends HttpServlet {
    private static Logger logger = Logger.getLogger(CnaJSON.class);
    
    public static final String CMD = "cmd";
    public static final String GET_DRUG_CMD = "get_drug";
    public static final String GET_SEGMENT_CMD = "get_segment";
    public static final String GET_CNA_FRACTION_CMD = "get_cna_fraction";
    public static final String CNA_EVENT_ID = "cna_id";
    public static final String CBIO_GENES_FILTER = "cbio_genes_filter";//Only get cna events from Cbio Cancer genes
    private static final double FRACTION_GENOME_ALTERED_CUTOFF = 0.2;
    
    // class which process access control to cancer studies
    private AccessControl accessControl;
    
    @Override
    public void init(ServletConfig config) throws ServletException {
        super.init(config);
        accessControl = SpringUtil.getAccessControl();
    }
    
    /** 
     * Processes requests for both HTTP <code>GET</code> and <code>POST</code> methods.
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String cmd = request.getParameter(CMD);
        if (cmd!=null) {
            if (cmd.equalsIgnoreCase(GET_SEGMENT_CMD)) {
                processGetSegmentsRequest(request, response);
                return;
            }
            
            if (cmd.equalsIgnoreCase(GET_CNA_FRACTION_CMD)) {
                processCnaFractionsRequest(request, response);
                return;
            }
        }
            
        processGetCnaRequest(request, response);
    }
    
    private void processGetCnaRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String[] sampleIds = null;
        String cnaProfileId = request.getParameter(PatientView.CNA_PROFILE);
        String mrnaProfileId = request.getParameter(PatientView.MRNA_PROFILE);
        String drugType = request.getParameter(PatientView.DRUG_TYPE);
        Boolean filterByCbioGene = Boolean.parseBoolean(request.getParameter(CBIO_GENES_FILTER));
        boolean fdaOnly = false;
        boolean cancerDrug = true;
        if (drugType!=null && drugType.equalsIgnoreCase(PatientView.DRUG_TYPE_FDA_ONLY)) {
            fdaOnly = true;
            cancerDrug = false;
        }
        if(request.getParameterMap().containsKey(PatientView.SAMPLE_ID)) {
            sampleIds = request.getParameter(PatientView.SAMPLE_ID).split(" +");
        }
                
        GeneticProfile cnaProfile;
        CancerStudy cancerStudy = null;
        DaoGeneOptimized daoGeneOptimized = DaoGeneOptimized.getInstance();
        List<CnaEvent> cnaEvents = Collections.emptyList();
        Map<Long, Set<String>> drugs = Collections.emptyMap();
        Map<Long, Integer>  contextMap = Collections.emptyMap();
        Map<Long, Map<String,Object>> mrnaContext = Collections.emptyMap();

        try {
        	if(cnaProfileId != null) {
        		//  Get the Genetic Profile
        		cnaProfile =
    	                DaoGeneticProfile.getGeneticProfileByStableId(cnaProfileId);
        		if(cnaProfile != null) {
        			cancerStudy = DaoCancerStudy
        	                .getCancerStudyByInternalId(cnaProfile.getCancerStudyId());
        	        if (accessControl.isAccessibleCancerStudy(cancerStudy.getCancerStudyStableId()).size() == 1) {
        	        	List<Integer> internalSampleIds = new ArrayList<>();
        	            if(sampleIds == null){
        	                internalSampleIds = InternalIdUtil.getInternalNonNormalSampleIds(cancerStudy.getInternalId());
        	            }else{
        	                internalSampleIds = InternalIdUtil.getInternalSampleIds(cancerStudy.getInternalId(), Arrays.asList(sampleIds));
        	            }
        	            cnaEvents = DaoCnaEvent.getCnaEvents(internalSampleIds, null, cnaProfile.getGeneticProfileId(), Arrays.asList((short)-2,(short)2));
        	            if (!cnaEvents.isEmpty()) {
                            String concatEventIds = getConcatEventIds(cnaEvents);
                            int profileId = cnaProfile.getGeneticProfileId();
//                            drugs = getDrugs(cnaEvents, fdaOnly, cancerDrug);
                            contextMap = DaoCnaEvent.countSamplesWithCnaEvents(concatEventIds, profileId);
                            if (mrnaProfileId!=null && sampleIds.length==1) {
                                mrnaContext = getMrnaContext(internalSampleIds.get(0), cnaEvents, mrnaProfileId);
                            }
                        }
        	        }
        		}
        	}
            
        } catch (DaoException ex) {
            throw new ServletException(ex);
        }
        
        Map<String,List> data = initMap();
        Map<Long, Integer> mapEventIndex = new HashMap<Long, Integer>();
        for (CnaEvent cnaEvent : cnaEvents) {
            Set<String> drug = Collections.emptySet();
            try {
                drug = drugs.get(cnaEvent.getEntrezGeneId());
            } catch (Exception e) {
                logger.error(e.getMessage());
            }
            exportCnaEvent(data, mapEventIndex, cnaEvent, cancerStudy, drug,
                    contextMap.get(cnaEvent.getEventId()),
                    mrnaContext.get(cnaEvent.getEntrezGeneId()),
                    daoGeneOptimized);
        }

        response.setContentType("application/json");
        PrintWriter out = response.getWriter();
        ObjectMapper mapper = new ObjectMapper();
        try {
            out.write(mapper.writeValueAsString(data));
        } finally {            
            out.close();
        }
    }

    private void processGetSegmentsRequest(HttpServletRequest request,
            HttpServletResponse response)
            throws ServletException, IOException {

        String[] sampleIds = request.getParameter(PatientView.SAMPLE_ID).split(" +");
        String cancerStudyId = request.getParameter(QueryBuilder.CANCER_STUDY_ID);
        
        List<CopyNumberSegment> segs = Collections.emptyList();
        
        List<Integer> internalSampleIds = null;
        
        try {
        	if(cancerStudyId != null) {
        		 CancerStudy cancerStudy = DaoCancerStudy
        	                .getCancerStudyByStableId(cancerStudyId);
        		if(cancerStudy != null && accessControl.isAccessibleCancerStudy(cancerStudy.getCancerStudyStableId()).size() == 1) {
        			internalSampleIds = InternalIdUtil.getInternalSampleIds(cancerStudy.getInternalId(), Arrays.asList(sampleIds));
                    segs = DaoCopyNumberSegment.getSegmentForSamples(internalSampleIds, cancerStudy.getInternalId());
        		}
        	}
        } catch (DaoException ex) {
            throw new ServletException(ex);
        }
        
        Map<String,List> map = new HashMap<String,List>();
        for (Integer sampleId : internalSampleIds) {
            String stableId = DaoSample.getSampleById(sampleId).getStableId();
            map.put(stableId, new ArrayList());
        }
        
        for (CopyNumberSegment seg : segs) {
            String stableId = DaoSample.getSampleById(seg.getSampleId()).getStableId();
            exportCopyNumberSegment(map.get(stableId), seg);
        }

        response.setContentType("application/json");
        
        PrintWriter out = response.getWriter();
        ObjectMapper mapper = new ObjectMapper();
        try {
            out.write(mapper.writeValueAsString(map));
        } finally {            
            out.close();
        }
    }
    
    private void processCnaFractionsRequest(HttpServletRequest request,
            HttpServletResponse response)
            throws ServletException, IOException {
        String strSampleIds = request.getParameter(QueryBuilder.CASE_IDS);
        List<Integer> sampleIds = null;
        String cancerStudyId = request.getParameter(QueryBuilder.CANCER_STUDY_ID);
        
        Map<Integer, Double> fraction = Collections.emptyMap();
        
        try {
			if (cancerStudyId != null) {
				CancerStudy cancerStudy = DaoCancerStudy.getCancerStudyByStableId(cancerStudyId);
				if (cancerStudy != null
						&& accessControl.isAccessibleCancerStudy(cancerStudy.getCancerStudyStableId()).size() == 1) {
					if (strSampleIds != null) {
						List<String> stableSampleIds = Arrays.asList(strSampleIds.split("[ ,]+"));
						sampleIds = InternalIdUtil.getInternalNonNormalSampleIds(cancerStudy.getInternalId(),
								stableSampleIds);
					} else {
						sampleIds = InternalIdUtil.getInternalNonNormalSampleIds(cancerStudy.getInternalId());
					}
                    fraction = DaoCopyNumberSegment.getCopyNumberActeredFraction(sampleIds, cancerStudy.getInternalId(), 
                        FRACTION_GENOME_ALTERED_CUTOFF);
				}
			}
        } catch (DaoException ex) {
            throw new ServletException(ex);
        }

        response.setContentType("application/json");
        
        PrintWriter out = response.getWriter();
        ObjectMapper mapper = new ObjectMapper();
        try {
            out.write(mapper.writeValueAsString(replaceInternalSampleIdsWithStableIds(fraction)));
        } finally {            
            out.close();
        }
    }

    private Map<String, Double> replaceInternalSampleIdsWithStableIds(Map<Integer, Double> fraction)
    {
        Map<String, Double> toReturn = new HashMap<String, Double>();
        for (Integer internalId : fraction.keySet()) {
            toReturn.put(DaoSample.getSampleById(internalId).getStableId(),
                        fraction.get(internalId));
        }
        return toReturn;
    }
    
    private String getConcatEventIds(List<CnaEvent> cnaEvents) {
        if (cnaEvents.isEmpty()) {
            return "";
        }
        
        StringBuilder sb = new StringBuilder();
        for (CnaEvent cna : cnaEvents) {
            sb.append(cna.getEventId()).append(',');
        }
        sb.deleteCharAt(sb.length()-1);
        return sb.toString();
    }
    
    private Map<Long, Set<String>> getDrugs(List<CnaEvent> cnaEvents, boolean fdaOnly, boolean cancerDrug)
            throws DaoException {
        DaoDrugInteraction daoDrugInteraction = DaoDrugInteraction.getInstance();
        Set<Long> genes = new HashSet<Long>();
        
        // Temporary way of handling cases such as akt inhibitor for pten loss
        Map<Long,Set<Long>> mapTargetToEventGenes = new HashMap<Long,Set<Long>>();
        // end Temporary way of handling cases such as akt inhibitor for pten loss
        
        for (CnaEvent cnaEvent : cnaEvents) {
            long gene = cnaEvent.getEntrezGeneId();
            if (cnaEvent.getAlteration()==CnaEvent.CNA.AMP
                    ||cnaEvent.getAlteration()==CnaEvent.CNA.GAIN) { // since drugs are usually intibiting
                genes.add(gene);
            }
            
            // Temporary way of handling cases such as akt inhibitor for pten loss
            Set<Long> targets = daoDrugInteraction.getMoreTargets(gene, cnaEvent.getAlteration().name());
            genes.addAll(targets);
            for (Long target : targets) {
                Set<Long> eventGenes = mapTargetToEventGenes.get(target);
                if (eventGenes==null) {
                    eventGenes = new HashSet<Long>();
                    mapTargetToEventGenes.put(target, eventGenes);
                }
                eventGenes.add(gene);
            }
            // end Temporary way of handling cases such as akt inhibitor for pten loss
        }
        
        Map<Long, List<String>> map = daoDrugInteraction.getDrugs(genes,fdaOnly,cancerDrug);
        Map<Long, Set<String>> ret = new HashMap<Long, Set<String>>(map.size());
        for (Map.Entry<Long, List<String>> entry : map.entrySet()) {
            ret.put(entry.getKey(), new HashSet<String>(entry.getValue()));
        }
        
        // Temporary way of handling cases such as akt inhibitor for pten loss
        for (Map.Entry<Long, List<String>> entry : map.entrySet()) {
            Set<Long> eventGenes = mapTargetToEventGenes.get(entry.getKey());
            if (eventGenes!=null) {
                for (long eventGene : eventGenes) {
                    Set<String> drugs = ret.get(eventGene);
                    if (drugs==null) {
                        drugs = new HashSet<String>();
                        ret.put(eventGene, drugs);
                    }
                    drugs.addAll(entry.getValue());
                }
            }
        }
        // end Temporary way of handling cases such as akt inhibitor for pten loss
        
        return ret;
    }
    
    private Map<Long, Map<String,Object>> getMrnaContext(Integer internalSampleId, List<CnaEvent> cnaEvents,
            String mrnaProfileId) throws DaoException {
        Map<Long, Map<String,Object>> mapGenePercentile = new HashMap<Long, Map<String,Object>>();
        DaoGeneticAlteration daoGeneticAlteration = DaoGeneticAlteration.getInstance();
        for (CnaEvent cnaEvent : cnaEvents) {
            long gene = cnaEvent.getEntrezGeneId();
            if (mapGenePercentile.containsKey(gene)) {
                continue;
            }
            
            Map<Integer,String> mrnaMap = daoGeneticAlteration.getGeneticAlterationMap(
                    DaoGeneticProfile.getGeneticProfileByStableId(mrnaProfileId).getGeneticProfileId(),
                    gene);
            double mrnaCase = parseNumber(mrnaMap.get(internalSampleId));
            if (Double.isNaN(mrnaCase)) {
                continue;
            }
            
            Map<String,Object> map = new HashMap<String,Object>();
            mapGenePercentile.put(gene, map);
            
            map.put("zscore", mrnaCase);
            
            int total = 0, below = 0;
            for (String strMrna : mrnaMap.values()) {
                double mrna = parseNumber(strMrna);
                if (Double.isNaN(mrna)) {
                    continue;
                }
                
                total++;
                if (mrna <= mrnaCase) {
                    below++;
                }
            }
            
            map.put("perc", 100*below/total);
        }
        
        return mapGenePercentile;
    }
    
    private double parseNumber(String mrna) {
        try {
            return Double.parseDouble(mrna);
        } catch (Exception e) {
            return Double.NaN;
        }
    }
    
    private Map<String,List> initMap() {
        Map<String,List> map = new HashMap<String,List>();
        map.put("id", new ArrayList());
        map.put("caseIds", new ArrayList());
        map.put("entrez", new ArrayList());
        map.put("gene", new ArrayList());
        map.put("cytoband", new ArrayList());
        map.put("alter", new ArrayList());
        map.put("mrna", new ArrayList());
        map.put("gistic", new ArrayList());
        map.put("sanger", new ArrayList());
        map.put("cancer-gene", new ArrayList());
        map.put("drug", new ArrayList());
        map.put("altrate", new ArrayList());
        return map;
    }
    
    private void exportCnaEvent(Map<String,List> data, Map<Long, Integer> mapMutationEventIndex,
            CnaEvent cnaEvent, CancerStudy cancerStudy, Set<String> drugs, Integer context,
            Map<String,Object> mrna, DaoGeneOptimized daoGeneOptimized) 
            throws ServletException {
        Long eventId = cnaEvent.getEventId();
        Integer ix = mapMutationEventIndex.get(eventId);
        if (ix!=null) { // multiple samples
            Sample sample = DaoSample.getSampleById(cnaEvent.getSampleId());
            List.class.cast(data.get("caseIds").get(ix)).add(sample.getStableId());
            return;
        }
        
        mapMutationEventIndex.put(eventId, data.get("id").size());
        
        List<String> samples = new ArrayList<String>();
        Sample sample = DaoSample.getSampleById(cnaEvent.getSampleId());
        samples.add(sample.getStableId());
        data.get("caseIds").add(samples);
        
        data.get("id").add(cnaEvent.getEventId());
        CanonicalGene gene = daoGeneOptimized.getGene(cnaEvent.getEntrezGeneId());
        String symbol = gene.getHugoGeneSymbolAllCaps();
        data.get("gene").add(symbol);
        data.get("entrez").add(cnaEvent.getEntrezGeneId());
        data.get("alter").add(cnaEvent.getAlteration().getCode());
        data.get("mrna").add(mrna);
        
        // TODO: GISTIC
        List gistic;
        try {
            gistic = getGistic(cancerStudy.getInternalId(),
                    cnaEvent.getGeneSymbol(), cnaEvent.getAlteration());
        } catch (DaoException ex) {
            throw new ServletException(ex);
        }
        data.get("gistic").add(gistic);
        
        data.get("altrate").add(context);
        
        boolean isSangerGene = false;
        try {
            isSangerGene = DaoSangerCensus.getInstance().getCancerGeneSet().containsKey(symbol);
        } catch (DaoException ex) {
            throw new ServletException(ex);
        }
        data.get("sanger").add(isSangerGene);
        data.get("cancer-gene").add(false);
        
        // drug
        data.get("drug").add(drugs);
    }
    
    private void exportCopyNumberSegment(List list, CopyNumberSegment seg) 
            throws ServletException {
        List row = new ArrayList();
        row.add(DaoSample.getSampleById(seg.getSampleId()).getStableId());
        row.add(seg.getChr());
        row.add(seg.getStart());
        row.add(seg.getEnd());
        row.add(seg.getNumProbes());
        row.add(seg.getSegMean());
        list.add(row);
    }
    
    private static final Map<Integer,Map<String,Map<CnaEvent.CNA,List>>> gisticMap // map from cancer study id
            = new HashMap<Integer,Map<String,Map<CnaEvent.CNA,List>>>();     // to map from gene to a list of params
    
    private static List getGistic(int cancerStudyId, String gene, CnaEvent.CNA cna) throws DaoException {
        Map<String,Map<CnaEvent.CNA,List>> mapGeneGistic;
        synchronized(gisticMap) {
            mapGeneGistic = gisticMap.get(cancerStudyId);
            if (mapGeneGistic == null) {
                mapGeneGistic = new HashMap<String,Map<CnaEvent.CNA,List>>();
                gisticMap.put(cancerStudyId, mapGeneGistic);
                List<Gistic> gistics = DaoGistic.getAllGisticByCancerStudyId(cancerStudyId);
                for (Gistic g : gistics) {
                    List<String> genes = new ArrayList<String>(g.getGenes_in_ROI().size());
                    for (CanonicalGene cg : g.getGenes_in_ROI()) {
                        genes.add(cg.getHugoGeneSymbolAllCaps());
                    }
                    List l = new ArrayList();
                    l.add(g.getqValue());
                    l.add(genes.size());
                    for (String hugo : genes) {
                        Map<CnaEvent.CNA,List> mapCC = mapGeneGistic.get(hugo);
                        if (mapCC==null) {
                            mapCC = new EnumMap<CnaEvent.CNA,List>(CnaEvent.CNA.class);
                            mapGeneGistic.put(hugo, mapCC);
                        }
                        mapCC.put(cna,l);
                    }
                }
            }
        }
        
        Map<CnaEvent.CNA,List> m = mapGeneGistic.get(gene);
        return m==null ? null : m.get(cna);
    }
    
    // <editor-fold defaultstate="collapsed" desc="HttpServlet methods. Click on the + sign on the left to edit the code.">
    /** 
     * Handles the HTTP <code>GET</code> method.
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        processRequest(request, response);
    }

    /** 
     * Handles the HTTP <code>POST</code> method.
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        processRequest(request, response);
    }

    /** 
     * Returns a short description of the servlet.
     * @return a String containing servlet description
     */
    @Override
    public String getServletInfo() {
        return "Short description";
    }// </editor-fold>
}
