package jpa.session;

import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import jpa.entity.Borrow;

/**
 *
 * @author Damian Terlecki
 */
@Stateless
public class BorrowFacade extends AbstractFacade<Borrow> {
    @PersistenceContext(unitName = "BookRentalPU")
    private EntityManager em;

    @Override
    protected EntityManager getEntityManager() {
        return em;
    }

    public BorrowFacade() {
        super(Borrow.class);
    }

}
