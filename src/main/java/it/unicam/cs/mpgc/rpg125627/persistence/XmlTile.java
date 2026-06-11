package it.unicam.cs.mpgc.rpg125627.persistence;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlAttribute;

/**
 * DTO JAXB per un singolo tile in un file mappa XML.
 * Corrisponde all'elemento {@code <tile row="…" col="…" type="…"/>}.
 */
@XmlAccessorType(XmlAccessType.FIELD)
public class XmlTile {

    @XmlAttribute public int row;
    @XmlAttribute public int col;
    /** Nome testuale del valore {@link it.unicam.cs.mpgc.rpg125627.model.TileType} (es. "FOREST"). */
    @XmlAttribute(name = "type") public String tileType;

    public XmlTile() {}
}
