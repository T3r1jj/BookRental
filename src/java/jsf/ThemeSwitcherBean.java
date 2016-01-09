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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.annotation.PostConstruct;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.SessionScoped;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.servlet.http.Cookie;
import util.AvailableThemes;
import util.Theme;

@ManagedBean(eager = true)
@SessionScoped
public class ThemeSwitcherBean implements Serializable {

    transient private List<Theme> themes;
    transient private Theme pickedTheme;

    @PostConstruct
    public void init() {
        themes = AvailableThemes.getInstance().getThemes();
        ExternalContext externalContext = FacesContext.getCurrentInstance().getExternalContext();
        Map<String, Object> cookies = externalContext.getRequestCookieMap();
        Cookie cookie = (Cookie) cookies.get("theme");
        if (cookie != null) {
            String theme = cookie.getValue();
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
            Map<String, Object> properties = new HashMap<>();
            properties.put("maxAge", new Integer(30758400));    //365 days
            FacesContext.getCurrentInstance()
                    .getExternalContext()
                    .addResponseCookie("theme", pickedTheme.getName(), properties);
        }
    }

    private void writeObject(java.io.ObjectOutputStream stream) {
    }

    private void readObject(java.io.ObjectInputStream stream) {
        init();
    }
}
