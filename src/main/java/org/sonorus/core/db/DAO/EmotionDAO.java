/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.sonorus.core.db.DAO;

import java.util.List;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import org.sonorus.core.JPA.EmohurstvaluesJpaController;

/**
 *
 * @author dgrfiv
 */
public class EmotionDAO extends EmohurstvaluesJpaController {

    public EmotionDAO(EntityManagerFactory emf) {
        super(emf);
    }

    public List getAvgEmohurst() {
        EntityManager em = getEntityManager();
        List list = em.createQuery("SELECT AVG(e.hurstValue) AvgHurst,e.emotion Emotion,e.subjectType subjecttype  FROM Emohurstvalues e GROUP BY e.subjectType,e.emotion order by AvgHurst").getResultList();
        return list;
    }

}
