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
                context.getExternalContext().getSessionMap().put("user", dbUser);
                context.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, ResourceBundle.getBundle("/resources/Bundle").getString("PersonLogged"), ResourceBundle.getBundle("/resources/Bundle").getString("PersonLogged")));
                FacesContext.getCurrentInstance().getApplication().getNavigationHandler().handle‌​Navigation(FacesContext.getCurrentInstance(), null, "/WEB-INF/view/successfulLogin");
                user = new Person();
                return;
            }
        }
        FacesContext context = FacesContext.getCurrentInstance();
        context.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, ResourceBundle.getBundle("/resources/Bundle").getString("PersonNotLogged"), ResourceBundle.getBundle("/resources/Bundle").getString("PersonNotLogged")));
        user = new Person();
    }

    public void logout() {
        FacesContext.getCurrentInstance().getApplication().getNavigationHandler().handle‌​Navigation(FacesContext.getCurrentInstance(), null, "/index");
        FacesContext.getCurrentInstance().getExternalContext().invalidateSession();
    }

    public Person getUser() {
        return user;
    }

    public void setUser(Person user) {
        this.user = user;
    }

    public String getLowercasePermissions() {
        return ((Person) FacesContext.getCurrentInstance().getExternalContext().getSessionMap().get("user")).getPermissions().toLowerCase();
    }
}
