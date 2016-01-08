package jsf;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import jpa.entity.Isbn;
import jsf.util.JsfUtil;
import jsf.util.JsfUtil.PersistAction;
import jpa.session.IsbnFacade;
import jpa.session.TagFacade;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
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
import jpa.entity.Resource;
import jpa.entity.Tag;
import org.primefaces.model.DefaultStreamedContent;
import org.primefaces.model.StreamedContent;

@ManagedBean(name = "isbnController")
@SessionScoped
public class IsbnController implements Serializable {

    @EJB
    private IsbnFacade isbnFacade;
    @EJB
    private TagFacade tagFacade;
    private List<Isbn> items = null;
    private Isbn selected;
    private String tags = "";

    public IsbnController() {
    }

    public Isbn getSelected() {
        return selected;
    }

    public void setSelected(Isbn selected) {
        this.selected = selected;
        if (selected != null) {
            tags = selected.getTags();
        }
    }

    protected void setEmbeddableKeys() {
    }

    protected void initializeEmbeddableKey() {
    }

    private IsbnFacade getFacade() {
        return isbnFacade;
    }

    public StreamedContent downloadResource(Resource resource) {
        try {
            InputStream stream = new FileInputStream(new File(resource.getFilePath()));
            String contentType = FacesContext.getCurrentInstance().getExternalContext().getMimeType(resource.getFilePath());
            return new DefaultStreamedContent(stream, contentType, resource.getResourceName());
        } catch (FileNotFoundException ex) {
            JsfUtil.addErrorMessage(resource.getResourceName() + " " + ResourceBundle.getBundle("/resources/Bundle").getString("ResourceNotFound"));
            Logger.getLogger(IsbnController.class.getName()).log(Level.SEVERE, "{0} not found", resource.getFilePath());
            return null;
        }
    }

    public List<Isbn> getNewestIsbns() {
        return getFacade().findRange(new int[]{0, 5});
    }

    public Isbn prepareCreate() {
        selected = new Isbn();
        selected.setAddDate(new Date());
        initializeEmbeddableKey();
        return selected;
    }

    public void create() {
        createTags();
        persist(PersistAction.CREATE, ResourceBundle.getBundle("/resources/Bundle").getString("IsbnCreated"));
        if (!JsfUtil.isValidationFailed()) {
            items = null;    // Invalidate list of items to trigger re-query.
        }
    }

    private void removeOldTags() {
        for (Tag tag : selected.getTagList()) {
            tagFacade.remove(tag);
        }
    }

    private void createTags() {
        String[] tagStrings = tags.replace(" ", "").toLowerCase().split(",");
        List<Tag> tagsList = new ArrayList<>();
        for (String tagString : tagStrings) {
            if (!tagString.isEmpty()) {
                Tag tag = new Tag();
                tag.setIsbn(selected);
                tag.setTagName(tagString);
                tagsList.add(tag);
            }
        }
        selected.setTagList(tagsList);
    }

    public void update() {
        removeOldTags();
        createTags();
        persist(PersistAction.UPDATE, ResourceBundle.getBundle("/resources/Bundle").getString("IsbnUpdated"));
    }

    public void destroy() {
        for (Resource resource : selected.getResourceList()) {
            new File(resource.getFilePath()).delete();
        }
        persist(PersistAction.DELETE, ResourceBundle.getBundle("/resources/Bundle").getString("IsbnDeleted"));
        if (!JsfUtil.isValidationFailed()) {
            selected = null; // Remove selection
            items = null;    // Invalidate list of items to trigger re-query.
        }
    }

    public List<Isbn> getItems() {
        if (items == null) {
            items = getFacade().findAll();
        }
        return items;
    }

    public String getTags() {
        return tags;
    }

    public void setTags(String tags) {
        this.tags = tags;
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

    public List<Isbn> getItemsAvailableSelectMany() {
        return getFacade().findAll();
    }

    public List<Isbn> getItemsAvailableSelectOne() {
        return getFacade().findAll();
    }

    @FacesConverter(forClass = Isbn.class)
    public static class IsbnControllerConverter implements Converter {

        @Override
        public Object getAsObject(FacesContext facesContext, UIComponent component, String value) {
            if (value == null || value.length() == 0) {
                return null;
            }
            IsbnController controller = (IsbnController) facesContext.getApplication().getELResolver().
                    getValue(facesContext.getELContext(), null, "isbnController");
            return controller.getFacade().find(getKey(value));
        }

        java.lang.String getKey(String value) {
            java.lang.String key;
            key = value;
            return key;
        }

        String getStringKey(java.lang.String value) {
            StringBuilder sb = new StringBuilder();
            sb.append(value);
            return sb.toString();
        }

        @Override
        public String getAsString(FacesContext facesContext, UIComponent component, Object object) {
            if (object == null) {
                return null;
            }
            if (object instanceof Isbn) {
                Isbn o = (Isbn) object;
                return getStringKey(o.getIsbn());
            } else {
                Logger.getLogger(this.getClass().getName()).log(Level.SEVERE, "object {0} is of type {1}; expected type: {2}", new Object[]{object, object.getClass().getName(), Isbn.class.getName()});
                return null;
            }
        }

    }

}
