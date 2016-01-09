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

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.PostConstruct;
import javax.ejb.EJB;
import javax.faces.bean.ApplicationScoped;
import javax.faces.bean.ManagedBean;
import javax.faces.context.FacesContext;
import jpa.entity.Person;
import jpa.session.PersonFacade;
import jsf.util.JsfUtil;

@ManagedBean(eager = true)
@ApplicationScoped
public class OptionsController {

    @EJB
    private PersonFacade personFacade;  //only for convenience of initializing db with admin
    private int maxBorrowDays;
    private int penaltyDayValue;
    private String propertiesDirectory;

    public OptionsController() {
    }

    @PostConstruct
    private void init() {
        FacesContext context = FacesContext.getCurrentInstance();
        propertiesDirectory = context.getExternalContext().getInitParameter("propertiesDirectory");
        File file = new File(propertiesDirectory);
        createPropertiesIfNotExist(file, propertiesDirectory);
        try (BufferedReader reader = new BufferedReader(new FileReader(propertiesDirectory + "library.properties"))) {
            reader.readLine();
            maxBorrowDays = Integer.parseInt(reader.readLine());
            reader.readLine();
            penaltyDayValue = Integer.parseInt(reader.readLine());
            reader.readLine();
            reader.close();
        } catch (IOException ex) {
            Logger.getLogger(OptionsController.class.getName()).log(Level.SEVERE, null, ex);
        }

        // only for convenience, if error - restart server
        if (personFacade.findAll().isEmpty()) {
            Person admin = new Person();
            admin.setActivated(true);
            admin.setBanned(false);
            admin.setLogin("admin");
            admin.setPassword("admin");
            admin.setFirstName("admin");
            admin.setLastName("admin");
            admin.setPermissions("ADMIN");
            try {
                personFacade.create(admin);
            } catch (Exception ex) {
                Logger.getLogger(OptionsController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }
    
    public void save() {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(propertiesDirectory + "library.properties", false))) {
            writer.write("max_borrow_time_days");
            writer.newLine();
            writer.write(Integer.toString(maxBorrowDays));
            writer.newLine();
            writer.write("penalty_day_value");
            writer.newLine();
            writer.write(Integer.toString(penaltyDayValue));
            writer.newLine();
            writer.flush();
            writer.close();
            JsfUtil.addSuccessMessage(ResourceBundle.getBundle("/resources/Bundle").getString("OptionsUpdated"));
        } catch (IOException ex) {
            Logger.getLogger(OptionsController.class.getName()).log(Level.SEVERE, null, ex);
            JsfUtil.addErrorMessage(ResourceBundle.getBundle("/resources/Bundle").getString("OptionsNotCreated") + ": " + propertiesDirectory);
        }
    }

    private void createPropertiesIfNotExist(File file, String propertiesDirectory) {
        if (!file.exists() || !new File(propertiesDirectory + "library.properties").exists()) {
            file.mkdirs();
            file = new File(propertiesDirectory + "library.properties");
            try {
                file.createNewFile();
            } catch (IOException ex) {
                Logger.getLogger(OptionsController.class.getName()).log(Level.SEVERE, null, ex);
            }
            try (BufferedWriter writer = new BufferedWriter(new FileWriter(propertiesDirectory + "library.properties"))) {
                writer.write("max_borrow_time_days");
                writer.newLine();
                writer.write("31");
                writer.newLine();
                writer.write("penalty_day_value");
                writer.newLine();
                writer.write("1");
                writer.newLine();
                writer.flush();
                writer.close();
            } catch (IOException ex) {
                Logger.getLogger(OptionsController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
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

}
