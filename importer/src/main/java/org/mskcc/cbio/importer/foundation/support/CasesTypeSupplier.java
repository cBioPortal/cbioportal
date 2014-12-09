

package org.mskcc.cbio.importer.foundation.support;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.common.base.Supplier;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import org.apache.log4j.Logger;
import org.mskcc.cbio.foundation.jaxb.CasesType;
import org.mskcc.cbio.foundation.jaxb.ClientCaseInfoType;


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
*/

/**
 * The intent of this class is to provide global access to the JAXB representation 
 * of a Foundation Medicine XML file. This class should be invoked using the Google
 * Guava Suppliers.memoize method to avoid repeated unmarshalling of the XML data
 * @author criscuof
 */
public class CasesTypeSupplier implements Supplier<CasesType>{
    private final String xmlFileName;
    private static final Logger logger = Logger.getLogger(CasesTypeSupplier.class);

    public CasesTypeSupplier(String xmlFileName){
        Preconditions.checkArgument(!Strings.isNullOrEmpty(xmlFileName), "A Foundation xml file name is required");
        this.xmlFileName = xmlFileName;
    }
    
    @Override
    public CasesType get() {
          try {
            JAXBContext context = JAXBContext.newInstance(ClientCaseInfoType.class);
            Unmarshaller jaxbUnmarshaller = context.createUnmarshaller();
            JAXBElement obj = (JAXBElement) jaxbUnmarshaller.unmarshal(new FileInputStream(this.xmlFileName));
           ClientCaseInfoType ccit = (ClientCaseInfoType) obj.getValue();
           return ccit.getCases();
        } catch (JAXBException | FileNotFoundException ex) {
            logger.error(ex.getMessage());
        }
          return null; // TODO: change to Optional
    }

}
