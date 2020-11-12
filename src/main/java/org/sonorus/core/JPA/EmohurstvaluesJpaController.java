/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.sonorus.core.JPA;

import java.io.Serializable;
import java.util.List;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Query;
import javax.persistence.EntityNotFoundException;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import org.sonorus.core.JPA.exceptions.NonexistentEntityException;
import org.sonorus.core.JPA.exceptions.PreexistingEntityException;
import org.sonorus.core.entities.Emohurstvalues;

/**
 *
 * @author dgrfiv
 */
public class EmohurstvaluesJpaController implements Serializable {

    public EmohurstvaluesJpaController(EntityManagerFactory emf) {
        this.emf = emf;
    }
    private EntityManagerFactory emf = null;

    public EntityManager getEntityManager() {
        return emf.createEntityManager();
    }

    public void create(Emohurstvalues emohurstvalues) throws PreexistingEntityException, Exception {
        EntityManager em = null;
        try {
            em = getEntityManager();
            em.getTransaction().begin();
            em.persist(emohurstvalues);
            em.getTransaction().commit();
        } catch (Exception ex) {
            if (findEmohurstvalues(emohurstvalues.getEmotionId()) != null) {
                throw new PreexistingEntityException("Emohurstvalues " + emohurstvalues + " already exists.", ex);
            }
            throw ex;
        } finally {
            if (em != null) {
                em.close();
            }
        }
    }

    public void edit(Emohurstvalues emohurstvalues) throws NonexistentEntityException, Exception {
        EntityManager em = null;
        try {
            em = getEntityManager();
            em.getTransaction().begin();
            emohurstvalues = em.merge(emohurstvalues);
            em.getTransaction().commit();
        } catch (Exception ex) {
            String msg = ex.getLocalizedMessage();
            if (msg == null || msg.length() == 0) {
                Integer id = emohurstvalues.getEmotionId();
                if (findEmohurstvalues(id) == null) {
                    throw new NonexistentEntityException("The emohurstvalues with id " + id + " no longer exists.");
                }
            }
            throw ex;
        } finally {
            if (em != null) {
                em.close();
            }
        }
    }

    public void destroy(Integer id) throws NonexistentEntityException {
        EntityManager em = null;
        try {
            em = getEntityManager();
            em.getTransaction().begin();
            Emohurstvalues emohurstvalues;
            try {
                emohurstvalues = em.getReference(Emohurstvalues.class, id);
                emohurstvalues.getEmotionId();
            } catch (EntityNotFoundException enfe) {
                throw new NonexistentEntityException("The emohurstvalues with id " + id + " no longer exists.", enfe);
            }
            em.remove(emohurstvalues);
            em.getTransaction().commit();
        } finally {
            if (em != null) {
                em.close();
            }
        }
    }

    public List<Emohurstvalues> findEmohurstvaluesEntities() {
        return findEmohurstvaluesEntities(true, -1, -1);
    }

    public List<Emohurstvalues> findEmohurstvaluesEntities(int maxResults, int firstResult) {
        return findEmohurstvaluesEntities(false, maxResults, firstResult);
    }

    private List<Emohurstvalues> findEmohurstvaluesEntities(boolean all, int maxResults, int firstResult) {
        EntityManager em = getEntityManager();
        try {
            CriteriaQuery cq = em.getCriteriaBuilder().createQuery();
            cq.select(cq.from(Emohurstvalues.class));
            Query q = em.createQuery(cq);
            if (!all) {
                q.setMaxResults(maxResults);
                q.setFirstResult(firstResult);
            }
            return q.getResultList();
        } finally {
            em.close();
        }
    }

    public Emohurstvalues findEmohurstvalues(Integer id) {
        EntityManager em = getEntityManager();
        try {
            return em.find(Emohurstvalues.class, id);
        } finally {
            em.close();
        }
    }

    public int getEmohurstvaluesCount() {
        EntityManager em = getEntityManager();
        try {
            CriteriaQuery cq = em.getCriteriaBuilder().createQuery();
            Root<Emohurstvalues> rt = cq.from(Emohurstvalues.class);
            cq.select(em.getCriteriaBuilder().count(rt));
            Query q = em.createQuery(cq);
            return ((Long) q.getSingleResult()).intValue();
        } finally {
            em.close();
        }
    }
    
}
