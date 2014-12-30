package org.mskcc.cbio.importer.icgc.support;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;

/**
 * Copyright (c) 2014 Memorial Sloan-Kettering Cancer Center.
 * <p/>
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
 * <p/>
 * Created by criscuof on 12/22/14.
 */
public class IcgcUtil {
    /*
    represents a collection of static utility methods specific for ICGC importing
     */

    /*
    determines if internet access to ICGC for file/data transfer is available
     */
    public static boolean isIcgcConnectionWorking() {
        Socket sock = new Socket();
        String icgc = "dcc.icgc.org";
        InetSocketAddress addr = new InetSocketAddress(icgc,80);
        try {
            sock.connect(addr,3000);
            return true;
        } catch (IOException e) {
            return false;
        } finally {
            try {sock.close();}
            catch (IOException e) {}
        }
    }

    public static void main(String...args){
        System.out.println("Connection to ICGC: " +isIcgcConnectionWorking());
    }
}
