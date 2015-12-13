package jsf;

import java.io.Serializable;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import javax.annotation.PostConstruct;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.SessionScoped;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.faces.event.ValueChangeEvent;
import javax.servlet.http.Cookie;

/**
 *
 * @author Damian Terlecki
 */
@ManagedBean(eager = true)
@SessionScoped
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
        locale = "en";
    }

    @PostConstruct
    public void init() {
        ExternalContext externalContext = FacesContext.getCurrentInstance().getExternalContext();
        Map<String, Object> cookies = externalContext.getRequestCookieMap();
        Cookie cookie = (Cookie) cookies.get("locale");
        if (cookie != null) {
            locale = cookie.getValue();
        }
        for (Map.Entry<String, Object> entry : countries.entrySet()) {
            if (entry.getValue().toString().equals(locale)) {
                FacesContext.getCurrentInstance()
                        .getViewRoot().setLocale((Locale) entry.getValue());
            }
        }
    }

    public Map<String, Object> getCountries() {
        return countries;
    }

    public String getLocale() {
        return locale;
    }

    public void setLocale(String locale) {
        this.locale = locale;
        Map<String, Object> properties = new HashMap<>();
        properties.put("maxAge", new Integer(30758400));    //365 days
        FacesContext.getCurrentInstance()
                .getExternalContext()
                .addResponseCookie("locale", locale, properties);
    }

    public void localeChanged(ValueChangeEvent e) {
        String newLocaleValue = e.getNewValue().toString();
        for (Map.Entry<String, Object> entry : countries.entrySet()) {
            if (entry.getValue().toString().equals(newLocaleValue)) {
                FacesContext.getCurrentInstance()
                        .getViewRoot().setLocale((Locale) entry.getValue());
                Map<String, Object> properties = new HashMap<>();
                properties.put("maxAge", new Integer(30758400));    //365 days
                FacesContext.getCurrentInstance()
                        .getExternalContext()
                        .addResponseCookie("locale", locale, properties);
            }
        }
    }
}
