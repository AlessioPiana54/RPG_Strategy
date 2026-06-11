package it.unicam.cs.mpgc.rpg125627.persistence;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlElementWrapper;
import jakarta.xml.bind.annotation.XmlRootElement;

import java.util.ArrayList;
import java.util.List;

/**
 * Radice JAXB di un file di definizione mappa ({@code maps/*.xml}).
 *
 * <p>Formato atteso:</p>
 * <pre>{@code
 * <map rows="8" cols="8">
 *   <tiles>
 *     <tile row="0" col="2" type="FOREST"/>
 *   </tiles>
 *   <spawns>
 *     <spawn row="0" col="0" unitClass="WARRIOR" team="PLAYER"/>
 *   </spawns>
 * </map>
 * }</pre>
 *
 * <p>La deserializzazione avviene tramite il metodo tipizzato
 * {@code unmarshaller.unmarshal(source, XmlMap.class)} per evitare
 * conflitti di nome con {@link it.unicam.cs.mpgc.rpg125627.model.GridMap},
 * che condivide l'elemento radice {@code "map"} nel contesto JAXB condiviso.</p>
 */
@XmlRootElement(name = "map")
@XmlAccessorType(XmlAccessType.FIELD)
public class XmlMap {

    @XmlAttribute public int rows;
    @XmlAttribute public int cols;

    @XmlElementWrapper(name = "tiles")
    @XmlElement(name = "tile")
    public List<XmlTile> tiles = new ArrayList<>();

    @XmlElementWrapper(name = "spawns")
    @XmlElement(name = "spawn")
    public List<XmlUnitSpawn> spawns = new ArrayList<>();

    public XmlMap() {}
}
