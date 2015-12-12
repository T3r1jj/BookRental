package jsf;

import java.io.Serializable;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.RequestScoped;
import javax.faces.context.FacesContext;
import javax.faces.event.ValueChangeEvent;

/**
 *
 * @author Damian Terlecki
 */
@ManagedBean
@RequestScoped
public class LanguageSwitcherBean implements Serializable {

    private static final long serialVersionUID = 1L;
    private static final Map<String, Object> countries;
    private String locale;

    static {
        countries = new LinkedHashMap<>();
        countries.put("English", Locale.ENGLISH);
        countries.put("Polski", new Locale("pl"));
    }

    public LanguageSwitcherBean() {
    }

    public Map<String, Object> getCountries() {
        return countries;
    }

    public String getLocale() {
        return locale;
    }

    public void setLocale(String locale) {
        this.locale = locale;
    }

    public void localeChanged(ValueChangeEvent e) {
        String newLocaleValue = e.getNewValue().toString();
        countries.entrySet().stream().filter((entry) -> (entry.getValue().toString().equals(newLocaleValue))).forEach((entry) -> {
            FacesContext.getCurrentInstance()
                    .getViewRoot().setLocale((Locale) entry.getValue());
        });
    }
}
