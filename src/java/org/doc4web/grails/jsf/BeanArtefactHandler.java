package org.doc4web.grails.jsf;

import org.codehaus.groovy.grails.commons.ArtefactHandlerAdapter;
import org.codehaus.groovy.grails.commons.DomainClassArtefactHandler;
import org.codehaus.groovy.grails.commons.InjectableGrailsClass;

/**
 * org.doc4web.grails.jsf
 * <p/>
 * Date: 17 mars 2010
 * Time: 17:01:59
 *
 * @author pred
 */
public class BeanArtefactHandler extends ArtefactHandlerAdapter {

    public static final String TYPE = "Bean";
    public static final String PLUGIN_NAME = "icefaces";

    public BeanArtefactHandler() {
        super(TYPE, InjectableGrailsClass.class, GrailsBeanInjectableClass.class, GrailsBeanInjectableClass.BEAN,
                false);
    }

    @Override
    public String getPluginName() {
        return PLUGIN_NAME;
    }

    public boolean isArtefactClass(Class clazz) {
        return super.isArtefactClass(clazz) && !DomainClassArtefactHandler.isDomainClass(clazz);
    }
}
