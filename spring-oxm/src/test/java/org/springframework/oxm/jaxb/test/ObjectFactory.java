package org.springframework.oxm.jaxb.test;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.annotation.XmlElementDecl;
import javax.xml.bind.annotation.XmlRegistry;
import javax.xml.namespace.QName;

@XmlRegistry
public class ObjectFactory {
    private static final QName _Flight_QNAME = new QName("http://samples.springframework.org/flight", "flight");

    public ObjectFactory() {
    }

    public Flights createFlights() {
        return new Flights();
    }

    public FlightType createFlightType() {
        return new FlightType();
    }

    @XmlElementDecl(
            namespace = "http://samples.springframework.org/flight",
            name = "flight"
    )
    public JAXBElement<FlightType> createFlight(FlightType value) {
        return new JAXBElement(_Flight_QNAME, FlightType.class, (Class)null, value);
    }
}
