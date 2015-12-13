package util;

import java.util.Comparator;
import jpa.entity.Isbn;

/**
 *
 * @author Damian Terlecki
 */
public class IsbnsTitleComparator implements Comparator<Isbn> {
    @Override
    public int compare(Isbn o1, Isbn o2) {
        return o1.getTitle().compareTo(o2.getTitle());
    }
}