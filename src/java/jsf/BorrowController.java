/* 
 * Copyright 2016 Damian Terlecki.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package jsf;

import jpa.entity.Borrow;
import jsf.util.JsfUtil;
import jsf.util.JsfUtil.PersistAction;
import jpa.session.BorrowFacade;
import jpa.session.BookFacade;
import jpa.session.PersonFacade;
import jpa.session.ReservationFacade;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.ejb.EJB;
import javax.ejb.EJBException;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.faces.bean.ViewScoped;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;
import javax.faces.convert.FacesConverter;
import jpa.entity.Book;
import jpa.entity.Person;
import jpa.entity.Reservation;
import org.primefaces.context.RequestContext;

@ManagedBean(name = "borrowController")
@ViewScoped
public class BorrowController implements Serializable {

    @EJB
    private BorrowFacade borrowFacade;
    @EJB
    private PersonFacade personFacade;
    @EJB
    private BookFacade bookFacade;
    @EJB
    private ReservationFacade reservationFacade;
    @ManagedProperty(value = "#{optionsController.maxBorrowDays}")
    private int maxBorrowDays;
    @ManagedProperty(value = "#{optionsController.penaltyDayValue}")
    private int penaltyDayValue;
    private List<Borrow> items = null;
    private Borrow selected;
    private int daysLate;
    private BigDecimal penalty;

    public BorrowController() {
    }

    public Borrow getSelected() {
        return selected;
    }

    public void setSelected(Borrow selected) {
        this.selected = selected;
    }

    public int getDaysLate() {
        return daysLate;
    }

    public void setDaysLate(int daysLate) {
        this.daysLate = daysLate;
    }

    public BigDecimal getPenalty() {
        return penalty;
    }

    public void setPenalty(BigDecimal penalty) {
        this.penalty = penalty;
    }

    public int getMaxBorrowDays() {
        return maxBorrowDays;
    }

    public void setMaxBorrowDays(int maxBorrowDays) {
        this.maxBorrowDays = maxBorrowDays;
    }

    public int getPenaltyDayValue() {
        return penaltyDayValue;
    }

    public void setPenaltyDayValue(int penaltyDayValue) {
        this.penaltyDayValue = penaltyDayValue;
    }

    protected void setEmbeddableKeys() {
    }

    protected void initializeEmbeddableKey() {
    }

    private BorrowFacade getFacade() {
        return borrowFacade;
    }

    public Borrow prepareCreate() {
        selected = new Borrow();
        initializeEmbeddableKey();
        return selected;
    }

    public void registerRetrieve() {
        Date date = new Date();
        selected.setBorrowDate(date);
        Calendar c = Calendar.getInstance();
        c.setTime(date);
        c.add(Calendar.DATE, maxBorrowDays);
        selected.setReturnDate(c.getTime());
        update();
        Book book = selected.getBook();
        book.setIsBorrowed(true);
        book.setIsOnShelf(false);
        book.setIsInWarehouse(false);
        bookFacade.edit(book);
    }

    public void registerReturn() {
        selected.setReturnedDate(new Date());
        update();
        if (selected.getReturnedDate().after(selected.getReturnDate())) {
            daysLate = daysBetween(DateToCalendar(selected.getReturnDate()), DateToCalendar(selected.getReturnedDate()));
            penalty = new BigDecimal(penaltyDayValue).multiply(new BigDecimal(daysLate));
            RequestContext context = RequestContext.getCurrentInstance();
            context.execute("PF('BorrowPenaltyDialog').show();");
        }
        Book book = selected.getBook();
        book.getIsbn().getReservationList().size();
        if (book.getIsbn().getReservationList().isEmpty()) {
            book.setIsOnShelf(false);
            book.setIsInWarehouse(true);
        } else {
            book.setIsOnShelf(true);
            book.setIsInWarehouse(false);
            selected = new Borrow();
            selected.setBook(book);
            Reservation reservation = book.getIsbn().getReservationList().get(0);
            selected.setPerson(reservation.getPerson());
            create();
            reservationFacade.remove(reservation);
        }
        book.setIsBorrowed(false);
        bookFacade.edit(book);
    }

    public void savePenalty() {
        Person user = selected.getPerson();
        user.setPenalty(penalty.add(user.getPenalty()));
        if (user.getPenalty().compareTo(BigDecimal.ZERO) == 1) {
            user.setBanned(true);
        } else {
            user.setBanned(false);
        }
        personFacade.edit(user);
        JsfUtil.addSuccessMessage(ResourceBundle.getBundle("/resources/Bundle").getString("PenaltyUpdated"));
    }

    public void create() {
        persist(PersistAction.CREATE, ResourceBundle.getBundle("/resources/Bundle").getString("BorrowCreated"));
        if (!JsfUtil.isValidationFailed()) {
            items = null;    // Invalidate list of items to trigger re-query.
        }
    }

    public void update() {
        persist(PersistAction.UPDATE, ResourceBundle.getBundle("/resources/Bundle").getString("BorrowUpdated"));
    }

    public void destroy() {
        Book book = selected.getBook();
        persist(PersistAction.DELETE, ResourceBundle.getBundle("/resources/Bundle").getString("BorrowDeleted"));
        if (!JsfUtil.isValidationFailed()) {
            book.setIsOnShelf(false);
            book.setIsInWarehouse(true);
            book.setIsBorrowed(false);
            bookFacade.edit(book);
            selected = null; // Remove selection
            items = null;    // Invalidate list of items to trigger re-query.
        }
    }

    public List<Borrow> getItems() {
        if (items == null) {
            items = getFacade().findAll();
        }
        return items;
    }

    public List<Borrow> getUserItems() {
        FacesContext context = FacesContext.getCurrentInstance();
        Person user = (Person) context.getExternalContext().getSessionMap().get("user");
        if (user != null) {
            user = personFacade.find(user.getLogin());
            user.getReservationList().size();
            return user.getBorrowList();
        } else {
            FacesContext.getCurrentInstance().getApplication().getNavigationHandler().handle‌​Navigation(FacesContext.getCurrentInstance(), null, "/login");
            return new ArrayList<>();
        }
    }

    private void persist(PersistAction persistAction, String successMessage) {
        if (selected != null) {
            setEmbeddableKeys();
            try {
                if (persistAction != PersistAction.DELETE) {
                    getFacade().edit(selected);
                } else {
                    getFacade().remove(selected);
                }
                JsfUtil.addSuccessMessage(successMessage);
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

    public List<Borrow> getItemsAvailableSelectMany() {
        return getFacade().findAll();
    }

    public List<Borrow> getItemsAvailableSelectOne() {
        return getFacade().findAll();
    }

    @FacesConverter(forClass = Borrow.class)
    public static class BorrowControllerConverter implements Converter {

        @Override
        public Object getAsObject(FacesContext facesContext, UIComponent component, String value) {
            if (value == null || value.length() == 0) {
                return null;
            }
            BorrowController controller = (BorrowController) facesContext.getApplication().getELResolver().
                    getValue(facesContext.getELContext(), null, "borrowController");
            return controller.getFacade().find(getKey(value));
        }

        java.lang.Integer getKey(String value) {
            java.lang.Integer key;
            key = Integer.valueOf(value);
            return key;
        }

        String getStringKey(java.lang.Integer value) {
            StringBuilder sb = new StringBuilder();
            sb.append(value);
            return sb.toString();
        }

        @Override
        public String getAsString(FacesContext facesContext, UIComponent component, Object object) {
            if (object == null) {
                return null;
            }
            if (object instanceof Borrow) {
                Borrow o = (Borrow) object;
                return getStringKey(o.getId());
            } else {
                Logger.getLogger(this.getClass().getName()).log(Level.SEVERE, "object {0} is of type {1}; expected type: {2}", new Object[]{object, object.getClass().getName(), Borrow.class.getName()});
                return null;
            }
        }

    }

    private static Calendar DateToCalendar(Date date) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        return cal;
    }

    private static int daysBetween(Calendar day1, Calendar day2) {
        Calendar dayOne = (Calendar) day1.clone(),
                dayTwo = (Calendar) day2.clone();

        if (dayOne.get(Calendar.YEAR) == dayTwo.get(Calendar.YEAR)) {
            return Math.abs(dayOne.get(Calendar.DAY_OF_YEAR) - dayTwo.get(Calendar.DAY_OF_YEAR));
        } else {
            if (dayTwo.get(Calendar.YEAR) > dayOne.get(Calendar.YEAR)) {
                //swap them
                Calendar temp = dayOne;
                dayOne = dayTwo;
                dayTwo = temp;
            }
            int extraDays = 0;

            int dayOneOriginalYearDays = dayOne.get(Calendar.DAY_OF_YEAR);

            while (dayOne.get(Calendar.YEAR) > dayTwo.get(Calendar.YEAR)) {
                dayOne.add(Calendar.YEAR, -1);
                // getActualMaximum() important for leap years
                extraDays += dayOne.getActualMaximum(Calendar.DAY_OF_YEAR);
            }

            return extraDays - dayTwo.get(Calendar.DAY_OF_YEAR) + dayOneOriginalYearDays;
        }
    }

}
