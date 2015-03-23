package org.mskcc.cbio.portal.or_analysis;

import org.mskcc.cbio.portal.stats.FisherExact;

/**
 * pre-calculate/re-format input raw data based on different profiles
 * then return result (p-value) of fisher exact test
 *
 * @date Mar 16, 2015
 * @author suny1
 */
public class ORAnalysisDiscretizedDataProxy {
    
    public static double calcCNA (double[] x, double[] y) {
        
        double pValue = -1;
            
        int a = 0, //non altered
            b = 0, //x non altered, y altered
            c = 0, //x altered, y non altered
            d = 0; //both alered

        for (int i = 0; i < x.length ; i++) {
            boolean x_altered = false, y_altered = false;

            //deep deletion or amplification is considered altered 
            if (x[i] == 2.0 || x[i] == -2.0) { 
                x_altered = true;
            } 
            if (y[i] == 2.0 || y[i] == -2.0) {
                y_altered = true;
            }

            if (x_altered && y_altered) {
                d += 1;
            } else if (x_altered && !y_altered) {
                c += 1;
            } else if (!x_altered && y_altered) {
                b += 1;
            } else if (!x_altered && !y_altered) {
                a += 1;
            }

        }

        FisherExact fisher = new FisherExact(a + b + c + d);
        pValue = fisher.getCumlativeP(a, b, c, d);
        
        return pValue;
        
    }
    
    public static double calcMut (String[] x, String[] y) {
        
        double pValue = -1;
            
        int a = 0, //non altered
            b = 0, //x non altered, y altered
            c = 0, //x altered, y non altered
            d = 0; //both alered

        for (int i = 0; i < x.length ; i++) {
            boolean x_altered = false, y_altered = false;

            //deep deletion or amplification is considered altered 
            if (!x[i].equals("Non")) { 
                x_altered = true;
            } 
            if (!y[i].equals("Non")) { 
                y_altered = true;
            } 

            if (x_altered && y_altered) {
                d += 1;
            } else if (x_altered && !y_altered) {
                c += 1;
            } else if (!x_altered && y_altered) {
                b += 1;
            } else if (!x_altered && !y_altered) {
                a += 1;
            }
        }

        FisherExact fisher = new FisherExact(a + b + c + d);
        pValue = fisher.getCumlativeP(a, b, c, d);
        
        return pValue;
    }
    
}
