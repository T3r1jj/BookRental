/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jsf;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.SessionScoped;
import jpa.entity.Isbn;
import jsf.util.JsfUtil;

/**
 *
 * @author Damian Terlecki
 */
@ManagedBean
@SessionScoped
public class BooksCartController implements Serializable {

    private List<Isbn> items = new ArrayList<>();
    private Isbn selected;

    public List<Isbn> getItems() {
        return items;
    }

    public void setItems(List<Isbn> items) {
        this.items = items;
    }

    public Isbn getSelected() {
        return selected;
    }

    public void setSelected(Isbn selected) {
        this.selected = selected;
    }

    public void remove() {
        items.remove(selected);
    }

    public void add(Isbn isbn) {
        items.add(isbn);
        JsfUtil.addSuccessMessage(ResourceBundle.getBundle("/resources/Bundle").getString("AddedToCart"));
    }
    
    public boolean isInCart(Isbn isbn) {
        return items.contains(isbn);
    }

    /**
     * Creates a new instance of BookCartController
     */
    public BooksCartController() {
    }

}
