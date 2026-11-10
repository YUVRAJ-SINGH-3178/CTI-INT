package dao;

import java.util.Comparator;
import java.util.List;

/**
 * Interface defining ranking and ordering contracts for alerts and threat events.
 *
 * @param <T> Domain entity type
 */
public interface Ranker<T> {

    /**
     * Ranks items according to their natural priority (e.g. descending risk score).
     */
    List<T> rank(List<T> items);

    /**
     * Ranks items using a custom comparator.
     */
    List<T> rankWithComparator(List<T> items, Comparator<T> comparator);
}

