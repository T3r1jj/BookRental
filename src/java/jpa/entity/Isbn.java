package jpa.entity;

import java.io.Serializable;
import java.util.Date;
import java.util.List;
import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

/**
 *
 * @author Damian Terlecki
 */
@Entity
@Table(name = "ISBN")
@NamedQueries({
    @NamedQuery(name = "Isbn.findAll", query = "SELECT i FROM Isbn i")})
public class Isbn implements Serializable {
    private static final long serialVersionUID = 1L;
    @Id
    @Basic(optional = false)
    @NotNull
    @Size(min = 1, max = 255)
    @Column(name = "ISBN")
    private String isbn;
    @Column(name = "ADDDATE")
    @Temporal(TemporalType.TIMESTAMP)
    private Date addDate;
    @Size(max = 255)
    @Column(name = "AUTHOR")
    @Basic(optional = false)
    private String author;
    @Size(max = 255)
    @Column(name = "DESCRIPTION")
    private String description;
    @Size(max = 255)
    @Column(name = "PUBLISHINGHOUSE")
    private String publishingHouse;
    @Size(max = 255)
    @Column(name = "RELEASE")
    private String release;
    @Size(max = 255)
    @Column(name = "TITLE")
    @Basic(optional = false)
    private String title;
    @JoinColumn(name = "CATEGORYID", referencedColumnName = "ID")
    @ManyToOne
    private Category category;
    @OneToMany(mappedBy = "isbn")
    private List<Reservation> reservationList;
    @OneToMany(mappedBy = "isbn")
    private List<Book> bookList;
    @OneToMany(mappedBy = "isbn")
    private List<Tag> tagList;
    @OneToMany(mappedBy = "isbn")
    private List<Resource> resourceList;

    public Isbn() {
    }

    public Isbn(String isbn) {
        this.isbn = isbn;
    }

    public String getIsbn() {
        return isbn;
    }

    public void setIsbn(String isbn) {
        this.isbn = isbn;
    }

    public Date getAddDate() {
        return addDate;
    }

    public void setAddDate(Date adddate) {
        this.addDate = adddate;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getPublishingHouse() {
        return publishingHouse;
    }

    public void setPublishingHouse(String publishingHouse) {
        this.publishingHouse = publishingHouse;
    }

    public String getRelease() {
        return release;
    }

    public void setRelease(String release) {
        this.release = release;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public Category getCategory() {
        return category;
    }

    public void setCategory(Category category) {
        this.category = category;
    }

    public List<Reservation> getReservationList() {
        return reservationList;
    }

    public void setReservationList(List<Reservation> reservationList) {
        this.reservationList = reservationList;
    }

    public List<Book> getBookList() {
        return bookList;
    }

    public void setBookList(List<Book> bookList) {
        this.bookList = bookList;
    }

    public List<Tag> getTagList() {
        return tagList;
    }

    public void setTagList(List<Tag> tagList) {
        this.tagList = tagList;
    }

    public List<Resource> getResourceList() {
        return resourceList;
    }

    public void setResourceList(List<Resource> resourceList) {
        this.resourceList = resourceList;
    }

    @Override
    public int hashCode() {
        int hash = 0;
        hash += (isbn != null ? isbn.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof Isbn)) {
            return false;
        }
        Isbn other = (Isbn) object;
        if ((this.isbn == null && other.isbn != null) || (this.isbn != null && !this.isbn.equals(other.isbn))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "jpa.entity.Isbn[ isbn=" + isbn + " ]";
    }

}
