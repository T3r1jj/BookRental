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

import jpa.entity.Category;
import jsf.util.JsfUtil;
import jsf.util.JsfUtil.PersistAction;
import jpa.session.CategoryFacade;

import java.io.Serializable;
import java.util.List;
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
import org.primefaces.model.DefaultTreeNode;
import org.primefaces.model.TreeNode;

@ManagedBean(name = "categoryController")
@ViewScoped
public class CategoryController implements Serializable {

    @EJB
    private CategoryFacade categoryFacade;
    private List<Category> items = null;
    private Category selected;
    private TreeNode root;

    public CategoryController() {
    }

    public Category getSelected() {
        return selected;
    }

    public void setSelected(Category selected) {
        this.selected = selected;
    }

    protected void setEmbeddableKeys() {
    }

    protected void initializeEmbeddableKey() {
    }

    private CategoryFacade getFacade() {
        return categoryFacade;
    }

    public Category prepareCreate() {
        selected = new Category();
        initializeEmbeddableKey();
        return selected;
    }

    public void create() {
        if (selected.getParentCategory() != null) {
            Category swap = selected;
            selected = selected.getParentCategory();
            selected.getCategoryList().add(swap);
            persist(PersistAction.UPDATE, ResourceBundle.getBundle("/resources/Bundle").getString("CategoryCreated"));
            selected = swap;
        } else {
            persist(PersistAction.CREATE, ResourceBundle.getBundle("/resources/Bundle").getString("CategoryCreated"));
        }
        if (!JsfUtil.isValidationFailed()) {
            items = null;    // Invalidate list of items to trigger re-query.
            root = null;
        }
        items = null;
        root = null;
    }

    public void update() {
        persist(PersistAction.UPDATE, ResourceBundle.getBundle("/resources/Bundle").getString("CategoryUpdated"));
        root = null;
    }

    public void destroy() {
        persist(PersistAction.DELETE, ResourceBundle.getBundle("/resources/Bundle").getString("CategoryDeleted"));
        if (!JsfUtil.isValidationFailed()) {
            selected = null; // Remove selection
            items = null;    // Invalidate list of items to trigger re-query.
        }
        root = null;
    }

    public List<Category> getItems() {
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
            root = new DefaultTreeNode(ResourceBundle.getBundle("/resources/Bundle").getString("CategoryRoot"), null);
            for (Category category : items) {
                if (category.getParentCategory() == null) {
                    TreeNode firstNode = new DefaultTreeNode(category, root);
                    category.getIsbnList().size();
                    TreeNode secondNode = new DefaultTreeNode(category.getIsbnList(), firstNode);
                    firstNode.getChildren().add(secondNode);
                    root.getChildren().add(firstNode);
                    recursivelyAddToTree(firstNode, category);
                }
            }
        }
        return root;
    }

    public boolean isList(Object o) {
        return o instanceof List;
    }

    private void recursivelyAddToTree(TreeNode root, Category categoryParent) {
        if (categoryParent != null) {
            for (Category sameLevelCategory : categoryParent.getCategoryList()) {
                TreeNode firstNode = new DefaultTreeNode(sameLevelCategory, root);
                sameLevelCategory.getIsbnList().size();
                TreeNode secondNode = new DefaultTreeNode(sameLevelCategory.getIsbnList(), firstNode);
                firstNode.getChildren().add(secondNode);
                root.getChildren().add(firstNode);
                recursivelyAddToTree(firstNode, sameLevelCategory);
            }
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
                if (successMessage != null) {
                    JsfUtil.addSuccessMessage(successMessage);
                }
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

    public List<Category> getItemsAvailableSelectMany() {
        return getFacade().findAll();
    }

    public List<Category> getItemsAvailableSelectOne() {
        return getFacade().findAll();
    }

    @FacesConverter(forClass = Category.class)
    public static class CategoryControllerConverter implements Converter {

        @Override
        public Object getAsObject(FacesContext facesContext, UIComponent component, String value) {
            if (value == null || value.length() == 0) {
                return null;
            }
            CategoryController controller = (CategoryController) facesContext.getApplication().getELResolver().
                    getValue(facesContext.getELContext(), null, "categoryController");
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
            if (object instanceof Category) {
                Category o = (Category) object;
                return getStringKey(o.getId());
            } else {
                Logger.getLogger(this.getClass().getName()).log(Level.SEVERE, "object {0} is of type {1}; expected type: {2}", new Object[]{object, object.getClass().getName(), Category.class.getName()});
                return null;
            }
        }

    }

}
