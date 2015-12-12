package jpa.session;

import java.util.List;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import javax.persistence.criteria.Root;
import jpa.entity.Isbn;

/**
 *
 * @author Damian Terlecki
 */
@Stateless
public class IsbnFacade extends AbstractFacade<Isbn> {

    @PersistenceContext(unitName = "BookRentalPU")
    private EntityManager em;

    @Override
    protected EntityManager getEntityManager() {
        return em;
    }

    public IsbnFacade() {
        super(Isbn.class);
    }

    public List<Isbn> findIsbnByQuery(String query) {
        EntityManager em = getEntityManager();
        String wholeQuery = "SELECT * FROM APP.ISBN WHERE " + query;
        Query runQuery = em.createNativeQuery(wholeQuery, Isbn.class);
        return runQuery.getResultList();
    }

    @Override
    public List<Isbn> findRange(int[] range) {
        javax.persistence.criteria.CriteriaQuery cq = getEntityManager().getCriteriaBuilder().createQuery();
        Root<Isbn> c = cq.from(entityClass);
        cq.select(c);
        cq.orderBy(getEntityManager().getCriteriaBuilder().desc(c.get("addDate")));
        javax.persistence.Query q = getEntityManager().createQuery(cq);
        q.setMaxResults(range[1] - range[0] + 1);
        q.setFirstResult(range[0]);
        return q.getResultList();
    }

}
