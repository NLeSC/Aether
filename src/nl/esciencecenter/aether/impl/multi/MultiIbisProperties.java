package nl.esciencecenter.aether.impl.multi;

import nl.esciencecenter.aether.IbisProperties;

public interface MultiIbisProperties {
    public static final String PREFIX = IbisProperties.PREFIX + "multi.";
    public static final String STARTERS = PREFIX + "implementations";
    public static final String IMPLEMENTATION_JARS = PREFIX + "jars.";
    public static final String PROPERTIES = PREFIX + "properties.";
    public static final String PROPERTIES_FILE = PREFIX + "propertiesfile.";
}
