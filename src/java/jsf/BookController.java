package jsf;

import jpa.entity.Book;
import jsf.util.JsfUtil;
import jsf.util.JsfUtil.PersistAction;
import jpa.session.BookFacade;

import java.io.Serializable;
import java.util.List;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.ejb.EJB;
import javax.ejb.EJBException;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.SessionScoped;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;
import javax.faces.convert.FacesConverter;
import jpa.entity.Isbn;

@ManagedBean(name = "bookController")
@SessionScoped
public class BookController implements Serializable {

    @EJB
    private jpa.session.BookFacade ejbFacade;
    private List<Book> items = null;
    private Book selected;
    private int count = 1;

    public BookController() {
    }

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }

    public Book getSelected() {
        return selected;
    }

    public void setSelected(Book selected) {
        this.selected = selected;
    }

    protected void setEmbeddableKeys() {
    }

    protected void initializeEmbeddableKey() {
    }

    private BookFacade getFacade() {
        return ejbFacade;
    }

    public Book prepareCreate() {
        selected = new Book();
        initializeEmbeddableKey();
        return selected;
    }

    public void create() {
        Isbn isbn = selected.getIsbn();
        for (int i = 0; i < count; i++) {
            selected = new Book();
            selected.setIsbn(isbn);
            selected.setIsBorrowed(false);
            selected.setIsReserved(false);
            selected.setIsOnShelf(true);
            persist(PersistAction.CREATE, ResourceBundle.getBundle("/resources/Bundle").getString("BookCreated"));
        }
        if (!JsfUtil.isValidationFailed()) {
            items = null;    // Invalidate list of items to trigger re-query.
        }
    }

    public void update() {
        persist(PersistAction.UPDATE, ResourceBundle.getBundle("/resources/Bundle").getString("BookUpdated"));
    }

    public void destroy() {
        persist(PersistAction.DELETE, ResourceBundle.getBundle("/resources/Bundle").getString("BookDeleted"));
        if (!JsfUtil.isValidationFailed()) {
            selected = null; // Remove selection
            items = null;    // Invalidate list of items to trigger re-query.
        }
    }

    public List<Book> getItems() {
        if (items == null) {
            items = getFacade().findAll();
        }
        return items;
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

    public List<Book> getItemsAvailableSelectMany() {
        return getFacade().findAll();
    }

    public List<Book> getItemsAvailableSelectOne() {
        return getFacade().findAll();
    }

    public void isBorrowedStatusChange() {
        if (selected.getIsBorrowed()) {
            selected.setIsOnShelf(false);
            selected.setIsReserved(false);
        }
    }
    
    public void isOnShelfStatusChange() {
        if (selected.getIsOnShelf()) {
            selected.setIsBorrowed(false);
            selected.setIsReserved(false);
        }
    }
    public void isReservedStatusChange() {
        if (selected.getIsReserved()) {
            selected.setIsOnShelf(false);
            selected.setIsBorrowed(false);
        }
    }

    @FacesConverter(forClass = Book.class)
    public static class BookControllerConverter implements Converter {

        @Override
        public Object getAsObject(FacesContext facesContext, UIComponent component, String value) {
            if (value == null || value.length() == 0) {
                return null;
            }
            BookController controller = (BookController) facesContext.getApplication().getELResolver().
                    getValue(facesContext.getELContext(), null, "bookController");
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
            if (object instanceof Book) {
                Book o = (Book) object;
                return getStringKey(o.getId());
            } else {
                Logger.getLogger(this.getClass().getName()).log(Level.SEVERE, "object {0} is of type {1}; expected type: {2}", new Object[]{object, object.getClass().getName(), Book.class.getName()});
                return null;
            }
        }

    }

}
