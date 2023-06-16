package committee.nova.neutron.util.collection;

import java.util.LinkedList;

public class LtdLinkedList<T> extends LinkedList<T> {
    public boolean addWithLimit(T t, int limit) {
        final int rLimit = limit < 0 ? Integer.MAX_VALUE : limit;
        this.add(t);
        while (size() > rLimit) this.remove();
        return true;
    }
}
