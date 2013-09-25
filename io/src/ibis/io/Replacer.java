/* $Id: Replacer.java 5760 2007-06-04 08:33:19Z ceriel $ */

package ibis.io;

/**
 * Object replacer, used in object serialization..
 */

public interface Replacer {
    /**
     * Replaces an object. To be used when serializing an object, to determine
     * if the object should be replaced with a stub. If so, the replace method
     * returns the stub, otherwise it returns the parameter object.
     *
     * @param v the object to be replaced
     * @return the replaced object.
     */
    public Object replace(Object v);
}
