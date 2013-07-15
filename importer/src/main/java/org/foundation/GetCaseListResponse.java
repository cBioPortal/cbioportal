
package org.foundation;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.annotation.*;


/**
 * <p>Java class for anonymous complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType>
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="GetCaseListResult" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {
    "getCaseListResult"
})
@XmlRootElement(name = "GetCaseListResponse")
public class GetCaseListResponse {

    @XmlElementRef(name = "GetCaseListResult", namespace = "http://tempuri.org/", type = JAXBElement.class)
    protected JAXBElement<String> getCaseListResult;

    /**
     * Gets the value of the getCaseListResult property.
     * 
     * @return
     *     possible object is
     *     {@link javax.xml.bind.JAXBElement }{@code <}{@link String }{@code >}
     *
     */
    public JAXBElement<String> getGetCaseListResult() {
        return getCaseListResult;
    }

    /**
     * Sets the value of the getCaseListResult property.
     *
     * @param value
     *     allowed object is
     *     {@link javax.xml.bind.JAXBElement }{@code <}{@link String }{@code >}
     *     
     */
    public void setGetCaseListResult(JAXBElement<String> value) {
        this.getCaseListResult = ((JAXBElement<String> ) value);
    }

}
