/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jsf;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.ResourceBundle;
import javax.ejb.EJB;
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

    /**
     * Creates a new instance of BookCartController
     */
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
                if (book.getIsInWarehouse()) {
                    book.setIsInWarehouse(false);
                    book.setIsOnShelf(true);
                    book.setIsBorrowed(false);
                    Borrow borrow = new Borrow();
                    borrow.setBook(book);
                    borrow.setPerson(user);
                    borrowFacade.create(borrow);
                    bookFacade.edit(book);
                    JsfUtil.addSuccessMessage(ResourceBundle.getBundle("/resources/Bundle").getString("RentalSuccessful") + " (" + selected.getTitle() + ")");
                    items.remove(selected);
                    selected = null;
                    return;
                }
            }
            if (queue) {
                Reservation reservation = new Reservation();
                reservation.setReservationDate(new Date());
                reservation.setIsbn(selected);
                reservation.setPerson(user);
                reservationFacade.create(reservation);
                JsfUtil.addSuccessMessage(ResourceBundle.getBundle("/resources/Bundle").getString("ReservationSuccessful") + " (" + selected.getTitle() + ")");
                items.remove(selected);
                selected = null;
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
                    if (book.getIsInWarehouse()) {
                        book.setIsInWarehouse(false);
                        book.setIsOnShelf(true);
                        book.setIsBorrowed(false);
                        Borrow borrow = new Borrow();
                        borrow.setBook(book);
                        borrow.setPerson(user);
                        borrowFacade.create(borrow);
                        bookFacade.edit(book);
                        JsfUtil.addSuccessMessage(ResourceBundle.getBundle("/resources/Bundle").getString("RentalSuccessful") + " (" + isbn.getTitle() + ")");
                        it.remove();
                        rented = true;
                        break;
                    }
                }
                boolean reserved = false;
                if (queue && !rented) {
                    Reservation reservation = new Reservation();
                    reservation.setReservationDate(new Date());
                    reservation.setIsbn(isbn);
                    reservation.setPerson(user);
                    reservationFacade.create(reservation);
                    JsfUtil.addSuccessMessage(ResourceBundle.getBundle("/resources/Bundle").getString("ReservationSuccessful") + " (" + isbn.getTitle() + ")");
                    it.remove();
                    reserved = true;
                }
                if (!rented && !reserved) {
                    JsfUtil.addErrorMessage(ResourceBundle.getBundle("/resources/Bundle").getString("RentalUnsuccessful") + " (" + isbn.getTitle() + ")");
                }
            }
        } else {
            FacesContext.getCurrentInstance().getApplication().getNavigationHandler().handle‌​Navigation(FacesContext.getCurrentInstance(), null, "/login");
        }
    }

}
