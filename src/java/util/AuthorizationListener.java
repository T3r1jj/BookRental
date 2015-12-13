package util;

import javax.faces.application.NavigationHandler;
import javax.faces.context.FacesContext;
import javax.faces.event.PhaseEvent;
import javax.faces.event.PhaseId;
import javax.faces.event.PhaseListener;
import javax.servlet.http.HttpSession;
import jpa.entity.Person;

/**
 *
 * @author Damian Terlecki
 */
public class AuthorizationListener implements PhaseListener {

    @Override
    public void afterPhase(PhaseEvent event) {

        FacesContext facesContext = event.getFacesContext();
        String currentPage = facesContext.getViewRoot().getViewId();

        HttpSession session = (HttpSession) facesContext.getExternalContext().getSession(true);
        Person loggedUser = (Person) session.getAttribute("user");
        if (loggedUser == null) {
            if (currentPage.contains("/user/") || currentPage.contains("/worker/") || currentPage.contains("/admin/")) {
                redirectToLogin(facesContext);
            }
        } else {
            if (currentPage.contains("/worker/")) {
                if ("USER".equals(loggedUser.getPermissions())) {
                    redirectAccessDenied(facesContext);
                    return;
                }
            } else if (currentPage.contains("/admin/")) {
                if (!"ADMIN".equals(loggedUser.getPermissions())) {
                    redirectAccessDenied(facesContext);
                    return;
                }
            }

            if (!loggedUser.getActivated()) {
                redirectNotActivated(facesContext);
                return;
            }

            if (loggedUser.getBanned() && !currentPage.contains("/user/borrow/") && !currentPage.contains("/user/reservation/")) {
                redirectBanned(facesContext);
                return;
            }
        }
    }

    private void redirectBanned(FacesContext facesContext) {
        redirect(facesContext, "/WEB-INF/view/accountBanned");
    }

    private void redirectNotActivated(FacesContext facesContext) {
        redirect(facesContext, "/WEB-INF/view/accountNotActivated");
    }

    private void redirectToLogin(FacesContext facesContext) {
        redirect(facesContext, "/login");
    }

    private void redirectAccessDenied(FacesContext facesContext) {
        redirect(facesContext, "/WEB-INF/view/accessDenied");
    }

    private void redirect(FacesContext facesContext, String redirectDestination) {
        NavigationHandler navigationHandler = facesContext.getApplication().getNavigationHandler();
        navigationHandler.handleNavigation(facesContext, null, redirectDestination);
    }

    @Override
    public void beforePhase(PhaseEvent event) {
    }

    @Override
    public PhaseId getPhaseId() {
        return PhaseId.RESTORE_VIEW;
    }
}
