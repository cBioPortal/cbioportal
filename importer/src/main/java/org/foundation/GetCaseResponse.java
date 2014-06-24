
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
 *         &lt;element name="GetCaseResult" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
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
    "getCaseResult"
})
@XmlRootElement(name = "GetCaseResponse")
public class GetCaseResponse {

    @XmlElementRef(name = "GetCaseResult", namespace = "http://tempuri.org/", type = JAXBElement.class)
    protected JAXBElement<String> getCaseResult;

    /**
     * Gets the value of the getCaseResult property.
     * 
     * @return
     *     possible object is
     *     {@link javax.xml.bind.JAXBElement }{@code <}{@link String }{@code >}
     *
     */
    public JAXBElement<String> getGetCaseResult() {
        return getCaseResult;
    }

    /**
     * Sets the value of the getCaseResult property.
     *
     * @param value
     *     allowed object is
     *     {@link javax.xml.bind.JAXBElement }{@code <}{@link String }{@code >}
     *     
     */
    public void setGetCaseResult(JAXBElement<String> value) {
        this.getCaseResult = ((JAXBElement<String> ) value);
    }

}
