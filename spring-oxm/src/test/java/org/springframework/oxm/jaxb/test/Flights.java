package org.springframework.oxm.jaxb.test;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(
        name = "",
        propOrder = {"flight"}
)
@XmlRootElement(
        name = "flights"
)
public class Flights {
    @XmlElement(
            required = true
    )
    protected List<FlightType> flight;

    public Flights() {
    }

    public List<FlightType> getFlight() {
        if (this.flight == null) {
            this.flight = new ArrayList();
        }

        return this.flight;
    }
}
