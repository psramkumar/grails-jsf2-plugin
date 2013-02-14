package org.doc4web.grails.jsf;

import org.codehaus.groovy.grails.commons.GrailsApplication;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.context.MessageSource;
import org.springframework.web.jsf.el.SpringBeanFacesELResolver;

import javax.el.ELContext;
import javax.el.ELException;
import javax.el.PropertyNotWritableException;
import javax.faces.context.FacesContext;
import java.util.Locale;

/**
 * org.doc4web.grails.jsf.facelets.org.doc4web.grails.jsf
 * <p/>
 * Date: 15 mars 2010
 * Time: 18:58:12
 *
 * @author pred
 */
public class GrailsELResolver extends SpringBeanFacesELResolver {

    private static final Object[] NO_ARGS = new Object[]{};
    private static final String MESSAGESOURCE_SHORTCUT = "m";
    private static final String APPLICATION_SHORTCUT = "app";

    @Override
    public Object getValue(ELContext elContext, Object base, Object property) throws ELException {
        if (base == null) {
            String beanName = property.toString();

            if (beanName.equals(MESSAGESOURCE_SHORTCUT))
                beanName = "messageSource";
            else if (beanName.equals(APPLICATION_SHORTCUT)) {
                beanName = GrailsApplication.APPLICATION_ID;
            }

            BeanFactory bf = getBeanFactory(elContext);
            if (bf.containsBean(beanName)) {
                if (logger.isTraceEnabled()) {
                    logger.trace("Successfully resolved variable '" + beanName + "' in Spring BeanFactory");
                }
                elContext.setPropertyResolved(true);
                return bf.getBean(beanName);
            }
        } else if (base instanceof MessageSource) {
            MessageSource messageSource = (MessageSource) base;
            Locale locale = FacesContext.getCurrentInstance().getViewRoot()
                    .getLocale();
            elContext.setPropertyResolved(true);
            try {
                return messageSource.getMessage((String) property, NO_ARGS, locale);
            } catch (org.springframework.context.NoSuchMessageException m) {
                return (String) property;
            }
        }
        return null;
    }

    @Override
    public Class<?> getType(ELContext elContext, Object base, Object property) throws ELException {
        if (base == null) {
            String beanName = property.toString();

            if (beanName.equals(MESSAGESOURCE_SHORTCUT))
                beanName = "messageSource";

            BeanFactory bf = getBeanFactory(elContext);
            if (bf.containsBean(beanName)) {
                elContext.setPropertyResolved(true);
                return bf.getType(beanName);
            }
        } else if (base instanceof MessageSource) {
            return String.class;
        }
        return null;
    }

    @Override
    public void setValue(ELContext elContext, Object base, Object property, Object value) throws ELException {
        if (base == null) {
            String beanName = property.toString();

            if (beanName.equals(MESSAGESOURCE_SHORTCUT))
                beanName = "messageSource";

            BeanFactory bf = getBeanFactory(elContext);
            if (bf.containsBean(beanName)) {
                throw new PropertyNotWritableException(
                        "Variable '" + beanName + "' refers to a Spring bean which by definition is not writable");
            }
        } else if (base instanceof MessageSource && property != null) {
            throw new PropertyNotWritableException(
                    "MessageSource: '" + property + "' refers to a Spring bean which by definition is not writable");
        }
    }

    @Override
    public boolean isReadOnly(ELContext elContext, Object base, Object property) throws ELException {
        if (base == null) {
            String beanName = property.toString();

            if (beanName.equals(MESSAGESOURCE_SHORTCUT))
                beanName = "messageSource";

            BeanFactory bf = getBeanFactory(elContext);
            if (bf.containsBean(beanName)) {
                return true;
            }
        } else if (base instanceof MessageSource) {
            return true;
        }
        return false;
    }
}
