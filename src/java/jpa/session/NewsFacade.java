package jpa.session;

import java.util.List;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.criteria.Root;
import jpa.entity.News;

/**
 *
 * @author Damian Terlecki
 */
@Stateless
public class NewsFacade extends AbstractFacade<News> {

    @PersistenceContext(unitName = "BookRentalPU")
    private EntityManager em;

    @Override
    protected EntityManager getEntityManager() {
        return em;
    }

    public NewsFacade() {
        super(News.class);
    }

    @Override
    public List<News> findAll() {
        javax.persistence.criteria.CriteriaQuery cq = getEntityManager().getCriteriaBuilder().createQuery();
        Root<News> c = cq.from(entityClass);
        cq.select(c);
        cq.orderBy(getEntityManager().getCriteriaBuilder().desc(c.get("publishDate")));
        return getEntityManager().createQuery(cq).getResultList();
    }

}
