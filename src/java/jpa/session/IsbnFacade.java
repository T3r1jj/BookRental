package jpa.session;

import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
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

}
