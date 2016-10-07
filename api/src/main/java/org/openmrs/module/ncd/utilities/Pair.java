package org.openmrs.module.ncd.utilities;

/** A template class for an ordered pair of objects. Designed to be usable
 * as a Map key.
 *
 * @param <CodeFrequency> The data type for the first element of each Pair.
 * @param <Y> The data type for the second element of each Pair.
 */
public class Pair<X,Y> {
    
    private X first;
    private Y second;
    
    public Pair(X first, Y second) {
        this.first = first;
        this.second = second;
    }
    
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((first == null) ? 0 : first.hashCode());
        result = prime * result + ((second == null) ? 0 : second.hashCode());
        return result;
    }

    public X getFirst() {
        return first;
    }

    public void setFirst(X first) {
        this.first = first;
    }

    public Y getSecond() {
        return second;
    }

    public void setSecond(Y second) {
        this.second = second;
    }

    @SuppressWarnings("unchecked")
    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        final Pair<X,Y> other = (Pair<X,Y>) obj;
        if (first == null) {
            if (other.first != null)
                return false;
        } else if (!first.equals(other.first))
            return false;
        if (second == null) {
            if (other.second != null)
                return false;
        } else if (!second.equals(other.second))
            return false;
        return true;
    }

    public String toString() {
        
        return "Pair(" + first + "," + second + ")";
    }
}
