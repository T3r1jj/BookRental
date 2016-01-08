package jsf;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.ejb.EJB;
import javax.ejb.EJBException;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.SessionScoped;
import javax.faces.context.FacesContext;
import jpa.entity.Book;
import jpa.entity.Borrow;
import jpa.entity.Isbn;
import jpa.entity.Person;
import jpa.entity.Reservation;
import jpa.session.BookFacade;
import jpa.session.BorrowFacade;
import jpa.session.ReservationFacade;
import jsf.util.JsfUtil;

@ManagedBean
@SessionScoped
public class BooksCartController implements Serializable {

    @EJB
    private BookFacade bookFacade;
    @EJB
    private BorrowFacade borrowFacade;
    @EJB
    private ReservationFacade reservationFacade;
    private List<Isbn> items = new ArrayList<>();
    private Isbn selected;
    private boolean queue;

    public BooksCartController() {
    }

    public boolean isQueue() {
        return queue;
    }

    public void setQueue(boolean queue) {
        this.queue = queue;
    }

    public List<Isbn> getItems() {
        return items;
    }

    public void setItems(List<Isbn> items) {
        this.items = items;
    }

    public Isbn getSelected() {
        return selected;
    }

    public void setSelected(Isbn selected) {
        this.selected = selected;
    }

    public void remove() {
        items.remove(selected);
    }

    public void add(Isbn isbn) {
        items.add(isbn);
        JsfUtil.addSuccessMessage(ResourceBundle.getBundle("/resources/Bundle").getString("AddedToCart"));
    }

    public boolean isInCart(Isbn isbn) {
        return items.contains(isbn);
    }

    public void borrow() {
        FacesContext context = FacesContext.getCurrentInstance();
        Person user = (Person) context.getExternalContext().getSessionMap().get("user");
        if (user != null) {
            selected.getBookList().size();
            for (Book book : selected.getBookList()) {
                book = bookFacade.find(book.getId());
                if (book != null && book.getIsInWarehouse()) {
                    makeRental(book, user);
                    return;
                }
            }
            if (queue) {
                makeReservation(user);
                return;
            }
            JsfUtil.addErrorMessage(ResourceBundle.getBundle("/resources/Bundle").getString("RentalUnsuccessful") + " (" + selected.getTitle() + ")");
        } else {
            FacesContext.getCurrentInstance().getApplication().getNavigationHandler().handle‌​Navigation(FacesContext.getCurrentInstance(), null, "/login");
        }
    }

    public void borrowAll() {
        FacesContext context = FacesContext.getCurrentInstance();
        Person user = (Person) context.getExternalContext().getSessionMap().get("user");
        if (user != null) {
            for (Iterator<Isbn> it = items.iterator(); it.hasNext();) {
                Isbn isbn = it.next();
                isbn.getBookList().size();
                boolean rented = false;
                for (Book book : isbn.getBookList()) {
                    book = bookFacade.find(book.getId());
                    if (book != null && book.getIsInWarehouse()) {
                        rented = makeRental(book, user, isbn, it);
                        break;
                    }
                }
                boolean reserved = false;
                if (queue && !rented) {
                    reserved = makeReservation(isbn, user, it);
                }
                if (!rented && !reserved) {
                    JsfUtil.addErrorMessage(ResourceBundle.getBundle("/resources/Bundle").getString("RentalUnsuccessful") + " (" + isbn.getTitle() + ")");
                }
            }
        } else {
            FacesContext.getCurrentInstance().getApplication().getNavigationHandler().handle‌​Navigation(FacesContext.getCurrentInstance(), null, "/login");
        }
    }

    private boolean makeReservation(Isbn isbn, Person user, Iterator<Isbn> it) {
        try {
            boolean reserved;
            Reservation reservation = new Reservation();
            reservation.setReservationDate(new Date());
            reservation.setIsbn(isbn);
            reservation.setPerson(user);
            reservationFacade.create(reservation);
            JsfUtil.addSuccessMessage(ResourceBundle.getBundle("/resources/Bundle").getString("ReservationSuccessful") + " (" + isbn.getTitle() + ")");
            it.remove();
            reserved = true;
            return reserved;
        } catch (EJBException ex) {
            String msg = "";
            Throwable cause = ex.getCause();
            if (cause != null) {
                msg = cause.getLocalizedMessage();
            }
            if (msg.length() > 0) {
                JsfUtil.addErrorMessage(msg);
            } else {
                JsfUtil.addErrorMessage(ex, ResourceBundle.getBundle("/resources/Bundle").getString("PersistenceErrorOccured"));
            }
        } catch (Exception ex) {
            Logger.getLogger(this.getClass().getName()).log(Level.SEVERE, null, ex);
            JsfUtil.addErrorMessage(ex, ResourceBundle.getBundle("/resources/Bundle").getString("PersistenceErrorOccured"));
        }
        return false;
    }

    private boolean makeRental(Book book, Person user, Isbn isbn, Iterator<Isbn> it) {
        try {
            boolean rented;
            book.setIsInWarehouse(false);
            book.setIsOnShelf(true);
            book.setIsBorrowed(false);
            Borrow borrow = new Borrow();
            borrow.setBook(book);
            borrow.setPerson(user);
            bookFacade.edit(book);
            borrowFacade.create(borrow);
            JsfUtil.addSuccessMessage(ResourceBundle.getBundle("/resources/Bundle").getString("RentalSuccessful") + " (" + isbn.getTitle() + ")");
            it.remove();
            rented = true;
            return rented;
        } catch (EJBException ex) {
            String msg = "";
            Throwable cause = ex.getCause();
            if (cause != null) {
                msg = cause.getLocalizedMessage();
            }
            if (msg.length() > 0) {
                JsfUtil.addErrorMessage(msg);
            } else {
                JsfUtil.addErrorMessage(ex, ResourceBundle.getBundle("/resources/Bundle").getString("PersistenceErrorOccured"));
            }
        } catch (Exception ex) {
            Logger.getLogger(this.getClass().getName()).log(Level.SEVERE, null, ex);
            JsfUtil.addErrorMessage(ex, ResourceBundle.getBundle("/resources/Bundle").getString("PersistenceErrorOccured"));
        }
        return false;
    }

    private void makeRental(Book book, Person user) {
        try {
            book.setIsInWarehouse(false);
            book.setIsOnShelf(true);
            book.setIsBorrowed(false);
            Borrow borrow = new Borrow();
            borrow.setBook(book);
            borrow.setPerson(user);
            bookFacade.edit(book);
            borrowFacade.create(borrow);
            JsfUtil.addSuccessMessage(ResourceBundle.getBundle("/resources/Bundle").getString("RentalSuccessful") + " (" + selected.getTitle() + ")");
            items.remove(selected);
            selected = null;
        } catch (EJBException ex) {
            String msg = "";
            Throwable cause = ex.getCause();
            if (cause != null) {
                msg = cause.getLocalizedMessage();
            }
            if (msg.length() > 0) {
                JsfUtil.addErrorMessage(msg);
            } else {
                JsfUtil.addErrorMessage(ex, ResourceBundle.getBundle("/resources/Bundle").getString("PersistenceErrorOccured"));
            }
        } catch (Exception ex) {
            Logger.getLogger(this.getClass().getName()).log(Level.SEVERE, null, ex);
            JsfUtil.addErrorMessage(ex, ResourceBundle.getBundle("/resources/Bundle").getString("PersistenceErrorOccured"));
        }
    }

    private void makeReservation(Person user) {
        try {
            Reservation reservation = new Reservation();
            reservation.setReservationDate(new Date());
            reservation.setIsbn(selected);
            reservation.setPerson(user);
            reservationFacade.create(reservation);
            JsfUtil.addSuccessMessage(ResourceBundle.getBundle("/resources/Bundle").getString("ReservationSuccessful") + " (" + selected.getTitle() + ")");
            items.remove(selected);
            selected = null;
        } catch (EJBException ex) {
            String msg = "";
            Throwable cause = ex.getCause();
            if (cause != null) {
                msg = cause.getLocalizedMessage();
            }
            if (msg.length() > 0) {
                JsfUtil.addErrorMessage(msg);
            } else {
                JsfUtil.addErrorMessage(ex, ResourceBundle.getBundle("/resources/Bundle").getString("PersistenceErrorOccured"));
            }
        } catch (Exception ex) {
            Logger.getLogger(this.getClass().getName()).log(Level.SEVERE, null, ex);
            JsfUtil.addErrorMessage(ex, ResourceBundle.getBundle("/resources/Bundle").getString("PersistenceErrorOccured"));
        }
    }

}
