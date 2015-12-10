package jsf;

import java.io.Serializable;
import java.util.ResourceBundle;
import javax.ejb.EJB;
import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.SessionScoped;
import javax.faces.context.FacesContext;
import jpa.entity.Person;
import jpa.session.PersonFacade;

@SessionScoped
@ManagedBean
public class LoggedUserController implements Serializable {

    private Person user = new Person();

    @EJB
    private PersonFacade userFacade;

    public void login() {
        Person dbUser = userFacade.find(user.getLogin());
        if (dbUser != null) {
            if (dbUser.getPassword().equals(user.getPassword())) {
                FacesContext context = FacesContext.getCurrentInstance();
                context.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, ResourceBundle.getBundle("/resources/Bundle").getString("PersonLogged"), ResourceBundle.getBundle("/resources/Bundle").getString("PersonLogged")));
                FacesContext.getCurrentInstance().getApplication().getNavigationHandler().handle‌​Navigation(FacesContext.getCurrentInstance(), null, "/WEB-INF/view/successfulLogin");
                return;
            }
        }
        FacesContext context = FacesContext.getCurrentInstance();
        context.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, ResourceBundle.getBundle("/resources/Bundle").getString("PersonNotLogged"), ResourceBundle.getBundle("/resources/Bundle").getString("PersonNotLogged")));
        user = new Person();
    }

    public Person getUser() {
        return user;
    }

    public void setUser(Person user) {
        this.user = user;
    }

}
