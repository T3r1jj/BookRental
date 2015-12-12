package jsf;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import javax.ejb.EJB;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.SessionScoped;
import javax.faces.context.FacesContext;
import jpa.entity.History;
import jpa.entity.Isbn;
import jpa.entity.Person;
import jpa.session.HistoryFacade;
import jpa.session.IsbnFacade;
import jpa.session.PersonFacade;

/**
 *
 * @author Damian Terlecki
 */
@ManagedBean
@SessionScoped
public class SearchController implements Serializable {

    @EJB
    private IsbnFacade isbnFacade;
    @EJB
    private HistoryFacade historyFacade;
    @EJB
    private PersonFacade personFacade;
    private List<History> historyList = new ArrayList<>();
    private List<History> savedHistory = new ArrayList<>();
    private List<Isbn> foundIsbns;
    private String query = "";
    private boolean orChosen = true;
    private boolean andChosen;
    private boolean notChosen;
    private Isbn selectedIsbn = null;
    private History selectedHistory = null;

    /**
     * Creates a new instance of SearchController
     */
    public SearchController() {
    }

    public History getSelectedHistory() {
        return selectedHistory;
    }

    public void setSelectedHistory(History selectedHistory) {
        this.selectedHistory = selectedHistory;
    }

    public void prepareSavedHistoryView() {
        selectedHistory = null;
        FacesContext context = FacesContext.getCurrentInstance();
        Person user = (Person) context.getExternalContext().getSessionMap().get("user");
        if (user != null) {
            user = personFacade.find(user.getLogin());
            user.getHistoryList().size();
            savedHistory = user.getHistoryList();
        } else {
            FacesContext.getCurrentInstance().getApplication().getNavigationHandler().handle‌​Navigation(FacesContext.getCurrentInstance(), null, "/login");
        }
    }
    
    public List<History> getSavedHistory() {
        return savedHistory;
    }

    public List<History> getHistoryList() {
        return historyList;
    }

    public void setHistoryList(List<History> historyList) {
        this.historyList = historyList;
    }

    public List<Isbn> getFoundIsbns() {
        return foundIsbns;
    }

    public void setFoundIsbns(List<Isbn> foundIsbns) {
        this.foundIsbns = foundIsbns;
    }

    public Isbn getSelectedIsbn() {
        return selectedIsbn;
    }

    public void setSelectedIsbn(Isbn selectedIsbn) {
        this.selectedIsbn = selectedIsbn;
    }

    public String getQuery() {
        return query;
    }

    public void setQuery(String query) {
        this.query = query;
    }

    public void clearSingleHistory() {
        if (selectedHistory != null) {
            FacesContext context = FacesContext.getCurrentInstance();
            Person user = (Person) context.getExternalContext().getSessionMap().get("user");
            if (user != null) {
                historyList.remove(selectedHistory);
                selectedHistory = null;
            } else {
                FacesContext.getCurrentInstance().getApplication().getNavigationHandler().handle‌​Navigation(FacesContext.getCurrentInstance(), null, "/login");
            }
        }
    }

    public void clearSingleSavedHistory() {
        if (selectedHistory != null) {
            historyList.remove(selectedHistory);
            historyFacade.remove(selectedHistory);
            selectedHistory = null;
        }
    }

    public void clearHistory() {
        selectedHistory = null;
        historyList.clear();
    }

    public void clearSavedHistory() {
        FacesContext context = FacesContext.getCurrentInstance();
        Person user = (Person) context.getExternalContext().getSessionMap().get("user");
        if (user != null) {
            for (History history : getSavedHistory()) {
                historyFacade.remove(selectedHistory);
            }
            selectedHistory = null;
            historyList.clear();
        } else {
            FacesContext.getCurrentInstance().getApplication().getNavigationHandler().handle‌​Navigation(FacesContext.getCurrentInstance(), null, "/login");
        }
    }

    public void saveSingleHistory() {
        if (selectedHistory != null) {
            FacesContext context = FacesContext.getCurrentInstance();
            Person user = (Person) context.getExternalContext().getSessionMap().get("user");
            if (user != null) {
                user = personFacade.find(user.getLogin());
                selectedHistory.setPerson(user);
                historyFacade.create(selectedHistory);
                historyList.remove(selectedHistory);
                selectedHistory = null;
            } else {
                FacesContext.getCurrentInstance().getApplication().getNavigationHandler().handle‌​Navigation(FacesContext.getCurrentInstance(), null, "/login");
            }
        }
    }

    public void saveHistory() {
        FacesContext context = FacesContext.getCurrentInstance();
        Person user = (Person) context.getExternalContext().getSessionMap().get("user");
        if (user != null) {
            user = personFacade.find(user.getLogin());
            for (History history : historyList) {
                history.setPerson(user);
                historyFacade.create(history);
            }
            selectedHistory = null;
            historyList.clear();
        } else {
            FacesContext.getCurrentInstance().getApplication().getNavigationHandler().handle‌​Navigation(FacesContext.getCurrentInstance(), null, "/login");
        }
    }

    public void addToQuery(String append) {
        if (orChosen) {
            if (query != null && !query.isEmpty()) {
                query = query + " OR ";
            }
        } else if (andChosen) {
            if (query != null && !query.isEmpty()) {
                query = query + " AND ";
            }
        }
        if (notChosen) {
            query = query + "-";
        }
        if (null != append) {
            switch (append) {
                case "isbn":
                    query = query + "isbn: ";
                    break;
                case "title":
                    query = query + "title: ";
                    break;
                case "author":
                    query = query + "author: ";
                    break;
            }
        }
    }

    public List<Isbn> runQuery() {
        if (query != null && !query.isEmpty()) {
            parseQueryAndRun();
        }
        return foundIsbns;
    }

    public void orEvent() {
        orChosen = true;
        andChosen = false;
    }

    public void andEvent() {
        orChosen = false;
        andChosen = true;
    }

    public boolean isOrChosen() {
        return orChosen;
    }

    public void setOrChosen(boolean orChosen) {
        this.orChosen = orChosen;
    }

    public boolean isAndChosen() {
        return andChosen;
    }

    public void setAndChosen(boolean andChosen) {
        this.andChosen = andChosen;
    }

    public boolean isNotChosen() {
        return notChosen;
    }

    public void setNotChosen(boolean notChosen) {
        this.notChosen = notChosen;
    }

    private void parseQueryAndRun() {
        try {
            StringBuilder sb = new StringBuilder();
            String[] words = query.split(" ");
            boolean close = false;
            for (String word : words) {
                if ("isbn:".equals(word)) {
                    sb.append("isbn like '%");
                    close = true;
                } else if ("title:".equals(word)) {
                    sb.append("title like '%");
                    close = true;
                } else if ("author:".equals(word)) {
                    sb.append("author like '%");
                    close = true;
                } else if ("-isbn:".equals(word)) {
                    sb.append("isbn not like '%");
                    close = true;
                } else if ("-title:".equals(word)) {
                    sb.append("title not like '%");
                    close = true;
                } else if ("-author:".equals(word)) {
                    sb.append("author not like '%");
                    close = true;
                } else {
                    if (close && ("OR".equals(word.toUpperCase())
                            || "AND".equals(word.toUpperCase()))) {
                        sb.append("%' ");
                        sb.append(word);
                        sb.append(" ");
                        close = false;
                    } else {
                        if (sb.length() > 0) {
                            char lastChar = sb.charAt(sb.length() - 1);
                            sb.append(word);
                            if (lastChar != '%') {
                                sb.append(" ");
                            }
                        }
                    }
                }
            }
            sb.append("%'");
            if (sb.length() <= 1) {
                return;
            }
            History history = new History();
            history.setPhrase(query);
            String query = sb.toString();
            foundIsbns = isbnFacade.findIsbnByQuery(query);
            history.setSearchDate(new Date());
            history.setResultsCount(foundIsbns.size());
            historyList.add(0, history);
        } catch (javax.ejb.EJBException ex) {
            foundIsbns = null;
        }
    }

}
