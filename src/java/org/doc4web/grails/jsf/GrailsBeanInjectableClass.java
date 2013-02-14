package org.doc4web.grails.jsf;

import org.codehaus.groovy.grails.commons.AbstractInjectableGrailsClass;
import org.codehaus.groovy.grails.commons.InjectableGrailsClass;

/**
 * org.doc4web.grails.jsf
 * <p/>
 * Date: 17 mars 2010
 * Time: 16:16:53
 *
 * @author pred
 */

public class GrailsBeanInjectableClass extends AbstractInjectableGrailsClass
        implements InjectableGrailsClass {

    public static final String BEAN = "Bean";

    public GrailsBeanInjectableClass(Class clazz) {
        super(clazz, BEAN);
    }

}
