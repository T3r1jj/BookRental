package jsf;

import java.io.Serializable;
import java.util.List;
import java.util.Map;
import javax.annotation.PostConstruct;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.SessionScoped;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import util.AvailableThemes;
import util.Theme;

@ManagedBean
@SessionScoped
public class ThemeSwitcherBean implements Serializable {

    private List<Theme> themes;
    private Theme pickedTheme;

    @PostConstruct
    public void init() {
        themes = AvailableThemes.getInstance().getThemes();
        ExternalContext externalContext = FacesContext.getCurrentInstance().getExternalContext();
        Map<String, Object> cookies = externalContext.getRequestCookieMap();
        String theme = (String) cookies.get("theme");
        if (theme != null) {
            System.out.println("+++COOKIE READ: " + theme);
            pickedTheme = AvailableThemes.getInstance().getTheme(theme);
        }
        if (pickedTheme == null) {
            pickedTheme = themes.get(7);
        }
    }

    public List<Theme> getThemes() {
        return themes;
    }

    public Theme getPickedTheme() {
        return pickedTheme;
    }

    public void setPickedTheme(Theme pickedTheme) {
        this.pickedTheme = pickedTheme;
        if (pickedTheme != null) {
            FacesContext.getCurrentInstance()
                    .getExternalContext()
                    .addResponseCookie("theme", pickedTheme.getName(), null);
        }
    }

}
