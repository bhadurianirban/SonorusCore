/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.sonorus.core.entities;

import java.io.Serializable;
import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;
import javax.xml.bind.annotation.XmlRootElement;

/**
 *
 * @author dgrfiv
 */
@Entity
@Table(name = "emohurstvalues")
@XmlRootElement
@NamedQueries({
    @NamedQuery(name = "Emohurstvalues.findAll", query = "SELECT e FROM Emohurstvalues e")})
public class Emohurstvalues implements Serializable {

    private static final long serialVersionUID = 1L;
    @Id
    @Basic(optional = false)
    @Column(name = "emotion_id")
    private Integer emotionId;
    @Basic(optional = false)
    @Column(name = "subject_type")
    private String subjectType;
    @Basic(optional = false)
    @Column(name = "emotion")
    private String emotion;
    @Basic(optional = false)
    @Column(name = "hurst_value")
    private double hurstValue;
    @Basic(optional = false)
    @Column(name = "mfdfa")
    private double mfdfa;

    public Emohurstvalues() {
    }

    public Emohurstvalues(Integer emotionId) {
        this.emotionId = emotionId;
    }

    public Emohurstvalues(Integer emotionId, String subjectType, String emotion, double hurstValue, double mfdfa) {
        this.emotionId = emotionId;
        this.subjectType = subjectType;
        this.emotion = emotion;
        this.hurstValue = hurstValue;
        this.mfdfa = mfdfa;
    }

    public Integer getEmotionId() {
        return emotionId;
    }

    public void setEmotionId(Integer emotionId) {
        this.emotionId = emotionId;
    }

    public String getSubjectType() {
        return subjectType;
    }

    public void setSubjectType(String subjectType) {
        this.subjectType = subjectType;
    }

    public String getEmotion() {
        return emotion;
    }

    public void setEmotion(String emotion) {
        this.emotion = emotion;
    }

    public double getHurstValue() {
        return hurstValue;
    }

    public void setHurstValue(double hurstValue) {
        this.hurstValue = hurstValue;
    }

    public double getMfdfa() {
        return mfdfa;
    }

    public void setMfdfa(double mfdfa) {
        this.mfdfa = mfdfa;
    }

    @Override
    public int hashCode() {
        int hash = 0;
        hash += (emotionId != null ? emotionId.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof Emohurstvalues)) {
            return false;
        }
        Emohurstvalues other = (Emohurstvalues) object;
        if ((this.emotionId == null && other.emotionId != null) || (this.emotionId != null && !this.emotionId.equals(other.emotionId))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "org.dgrf.speechcore.entities.Emohurstvalues[ emotionId=" + emotionId + " ]";
    }
    
}
