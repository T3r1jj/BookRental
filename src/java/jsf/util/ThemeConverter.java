package jsf.util;


import util.AvailableThemes;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;
import javax.faces.convert.FacesConverter;
import util.Theme;

@FacesConverter("jsf.util.ThemeConverter")
public class ThemeConverter implements Converter {

    @Override
    public Object getAsObject(FacesContext context, UIComponent component, String value) {

        return AvailableThemes.getInstance().getTheme(value);
    }

    @Override
    public String getAsString(FacesContext context, UIComponent component, Object value) {
        return ((Theme) value).getName();
    }

}