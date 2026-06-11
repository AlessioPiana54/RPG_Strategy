package it.unicam.cs.mpgc.rpg125627.model;

import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.adapters.XmlAdapter;

/**
 * Adattatore JAXB per serializzare/deserializzare il record immutabile {@link Position}.
 * Converte tra {@link Position} (non istanziabile senza argomenti) e {@link PositionXml}
 * (POJO mutabile compatibile con JAXB).
 */
public class PositionAdapter extends XmlAdapter<PositionAdapter.PositionXml, Position> {

    /** DTO mutabile usato da JAXB per la serializzazione di {@link Position}. */
    public static class PositionXml {
        @XmlAttribute public int row;
        @XmlAttribute public int col;
    }

    @Override
    public Position unmarshal(PositionXml v) {
        return v == null ? null : new Position(v.row, v.col);
    }

    @Override
    public PositionXml marshal(Position v) {
        if (v == null) return null;
        PositionXml xml = new PositionXml();
        xml.row = v.row();
        xml.col = v.col();
        return xml;
    }
}
