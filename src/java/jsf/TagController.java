package jsf;

import jpa.entity.Tag;
import jsf.util.JsfUtil;
import jsf.util.JsfUtil.PersistAction;
import jpa.session.TagFacade;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.ejb.EJB;
import javax.ejb.EJBException;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;
import javax.faces.convert.FacesConverter;
import jpa.entity.Isbn;
import org.primefaces.model.DefaultTreeNode;
import org.primefaces.model.TreeNode;
import util.IsbnsTitleComparator;

@ManagedBean(name = "tagController")
@ViewScoped
public class TagController implements Serializable {

    @EJB
    private jpa.session.TagFacade ejbFacade;
    private List<Tag> items = null;
    private Tag selected;
    private TreeNode root = null;

    public TagController() {
    }

    public Tag getSelected() {
        return selected;
    }

    public void setSelected(Tag selected) {
        this.selected = selected;
    }

    protected void setEmbeddableKeys() {
    }

    protected void initializeEmbeddableKey() {
    }

    private TagFacade getFacade() {
        return ejbFacade;
    }

    public Tag prepareCreate() {
        selected = new Tag();
        initializeEmbeddableKey();
        return selected;
    }

    public void create() {
        persist(PersistAction.CREATE, ResourceBundle.getBundle("/resources/Bundle").getString("TagCreated"));
        if (!JsfUtil.isValidationFailed()) {
            items = null;    // Invalidate list of items to trigger re-query.
            root = null;
        }
    }

    public void update() {
        persist(PersistAction.UPDATE, ResourceBundle.getBundle("/resources/Bundle").getString("TagUpdated"));
        root = null;
    }

    public void destroy() {
        persist(PersistAction.DELETE, ResourceBundle.getBundle("/resources/Bundle").getString("TagDeleted"));
        if (!JsfUtil.isValidationFailed()) {
            selected = null; // Remove selection
            items = null;    // Invalidate list of items to trigger re-query.
        }
        root = null;
    }

    public List<Tag> getItems() {
        if (items == null) {
            items = getFacade().findAll();
        }
        return items;
    }

    public TreeNode getRoot() {
        if (root == null) {
            if (items == null) {
                items = getItems();
            }
            Map<String, List<Isbn>> tagsIsbns = new HashMap<>();
            for (Tag tag : items) {
                if (tagsIsbns.containsKey(tag.getTagName())) {
                    tagsIsbns.get(tag.getTagName()).add(tag.getIsbn());
                } else {
                    List<Isbn> isbns = new ArrayList<>();
                    isbns.add(tag.getIsbn());
                    tagsIsbns.put(tag.getTagName(), isbns);
                }
            }
            for (List<Isbn> isbns : tagsIsbns.values()) {
                Collections.sort(isbns, new IsbnsTitleComparator());
            }
            root = new DefaultTreeNode(ResourceBundle.getBundle("/resources/Bundle").getString("TagRoot"), null);
            for (Map.Entry<String, List<Isbn>> entry : tagsIsbns.entrySet()) {
                TreeNode firstNode = new DefaultTreeNode(new Object[]{entry.getKey(), null}, root);
                TreeNode secondNode = new DefaultTreeNode(new Object[]{entry.getValue(), entry.getKey()}, firstNode);
                firstNode.getChildren().add(secondNode);
                root.getChildren().add(firstNode);
            }
        }
        return root;
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

    public List<Tag> getItemsAvailableSelectMany() {
        return getFacade().findAll();
    }

    public List<Tag> getItemsAvailableSelectOne() {
        return getFacade().findAll();
    }

    @FacesConverter(forClass = Tag.class)
    public static class TagControllerConverter implements Converter {

        @Override
        public Object getAsObject(FacesContext facesContext, UIComponent component, String value) {
            if (value == null || value.length() == 0) {
                return null;
            }
            TagController controller = (TagController) facesContext.getApplication().getELResolver().
                    getValue(facesContext.getELContext(), null, "tagController");
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
            if (object instanceof Tag) {
                Tag o = (Tag) object;
                return getStringKey(o.getId());
            } else {
                Logger.getLogger(this.getClass().getName()).log(Level.SEVERE, "object {0} is of type {1}; expected type: {2}", new Object[]{object, object.getClass().getName(), Tag.class.getName()});
                return null;
            }
        }

    }

}
