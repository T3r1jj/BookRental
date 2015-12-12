package jsf;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import jpa.entity.Resource;
import jsf.util.JsfUtil;
import jsf.util.JsfUtil.PersistAction;
import jpa.session.ResourceFacade;
import java.io.Serializable;
import java.util.List;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.ejb.EJB;
import javax.ejb.EJBException;
import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import javax.faces.component.UIComponent;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;
import javax.faces.convert.FacesConverter;
import org.primefaces.model.DefaultStreamedContent;
import org.primefaces.model.StreamedContent;
import org.primefaces.model.UploadedFile;

@ManagedBean(name = "resourceController")
@ViewScoped
public class ResourceController implements Serializable {

    @EJB
    private jpa.session.ResourceFacade ejbFacade;
    private List<Resource> items = null;
    private Resource selected;
    private UploadedFile file;

    public ResourceController() {
    }

    public Resource getSelected() {
        return selected;
    }

    public void setSelected(Resource selected) {
        this.selected = selected;
    }

    public UploadedFile getFile() {
        return file;
    }

    public void setFile(UploadedFile file) {
        this.file = file;
    }

    protected void setEmbeddableKeys() {
    }

    protected void initializeEmbeddableKey() {
    }

    private ResourceFacade getFacade() {
        return ejbFacade;
    }

    public Resource prepareCreate() {
        selected = new Resource();
        initializeEmbeddableKey();
        return selected;
    }

    public void handleFileUpload() {
        ExternalContext externalContext = FacesContext.getCurrentInstance().getExternalContext();
        String directory = externalContext.getInitParameter("uploadDirectory");
        String fileName = file.getFileName();
        String[] tokens = fileName.split("\\.(?=[^\\.]+$)");
        File destinationFile;
        try {
            if (tokens.length < 2) {
                destinationFile = File.createTempFile(tokens[0] + "-", ".dat", new File(directory));
            } else {
                destinationFile = File.createTempFile(tokens[0] + "-", "." + tokens[1], new File(directory));
            }
            InputStream input = file.getInputstream();
            copyFile(destinationFile, input);
        } catch (IOException ex) {
            FacesMessage message = new FacesMessage("Unsuccesful", file.getFileName() + " not uploaded.");
            FacesContext.getCurrentInstance().addMessage(null, message);
            return;
        }
        selected.setFilePath(destinationFile.getAbsolutePath());
        selected.setResourceName(fileName);
        FacesMessage message = new FacesMessage("Succesful", file.getFileName() + " is uploaded.");
        FacesContext.getCurrentInstance().addMessage(null, message);
        create();
    }

    private void copyFile(File destination, InputStream input) throws IOException {
        try (OutputStream output = new FileOutputStream(destination)) {
            int read = 0;
            byte[] bytes = new byte[1024];

            while ((read = input.read(bytes)) != -1) {
                output.write(bytes, 0, read);
            }
            input.close();
            output.flush();
            output.close();
        } catch (IOException e) {
            throw e;
        }
    }

    public void create() {
        persist(PersistAction.CREATE, ResourceBundle.getBundle("/resources/Bundle").getString("ResourceCreated"));
        if (!JsfUtil.isValidationFailed()) {
            items = null;    // Invalidate list of items to trigger re-query.
        }
    }

    public void update() {
        persist(PersistAction.UPDATE, ResourceBundle.getBundle("/resources/Bundle").getString("ResourceUpdated"));
    }

    public void destroy() {
        persist(PersistAction.DELETE, ResourceBundle.getBundle("/resources/Bundle").getString("ResourceDeleted"));
        if (!JsfUtil.isValidationFailed()) {
            selected = null; // Remove selection
            items = null;    // Invalidate list of items to trigger re-query.
        }
    }

    public List<Resource> getItems() {
        if (items == null) {
            items = getFacade().findAll();
        }
        return items;
    }

    public StreamedContent downloadResource() {
        try {
            InputStream stream = new FileInputStream(new File(selected.getFilePath()));
            String contentType = FacesContext.getCurrentInstance().getExternalContext().getMimeType(selected.getFilePath());
            return new DefaultStreamedContent(stream, contentType, selected.getResourceName());
        } catch (FileNotFoundException ex) {
            JsfUtil.addErrorMessage(selected.getResourceName() + " " + ResourceBundle.getBundle("/resources/Bundle").getString("ResourceNotFound"));
            Logger.getLogger(IsbnController.class.getName()).severe(selected.getFilePath() + " not found");
            return null;
        }
    }

    private void persist(PersistAction persistAction, String successMessage) {
        if (selected != null) {
            setEmbeddableKeys();
            try {
                if (persistAction != PersistAction.DELETE) {
                    getFacade().edit(selected);
                } else {
                    String filePath = selected.getFilePath();
                    getFacade().remove(selected);
                    new File(filePath).delete();
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

    public List<Resource> getItemsAvailableSelectMany() {
        return getFacade().findAll();
    }

    public List<Resource> getItemsAvailableSelectOne() {
        return getFacade().findAll();
    }

    @FacesConverter(forClass = Resource.class)
    public static class ResourceControllerConverter implements Converter {

        @Override
        public Object getAsObject(FacesContext facesContext, UIComponent component, String value) {
            if (value == null || value.length() == 0) {
                return null;
            }
            ResourceController controller = (ResourceController) facesContext.getApplication().getELResolver().
                    getValue(facesContext.getELContext(), null, "resourceController");
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
            if (object instanceof Resource) {
                Resource o = (Resource) object;
                return getStringKey(o.getId());
            } else {
                Logger.getLogger(this.getClass().getName()).log(Level.SEVERE, "object {0} is of type {1}; expected type: {2}", new Object[]{object, object.getClass().getName(), Resource.class.getName()});
                return null;
            }
        }

    }

}
