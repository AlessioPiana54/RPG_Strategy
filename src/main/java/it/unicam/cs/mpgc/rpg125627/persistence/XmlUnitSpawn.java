package it.unicam.cs.mpgc.rpg125627.persistence;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlAttribute;

/**
 * DTO JAXB per una posizione di spawn in un file mappa XML.
 * Corrisponde all'elemento {@code <spawn row="…" col="…" unitClass="…" team="…"/>}.
 */
@XmlAccessorType(XmlAccessType.FIELD)
public class XmlUnitSpawn {

    @XmlAttribute public int row;
    @XmlAttribute public int col;
    /** Nome testuale del valore {@link it.unicam.cs.mpgc.rpg125627.model.UnitClass} (es. "WARRIOR"). */
    @XmlAttribute public String unitClass;
    /** Nome testuale del valore {@link it.unicam.cs.mpgc.rpg125627.model.Team} (es. "PLAYER"). */
    @XmlAttribute public String team;

    public XmlUnitSpawn() {}
}
