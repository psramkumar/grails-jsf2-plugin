package org.doc4web.grails.jsf;

import grails.util.GrailsNameUtils;
import groovy.lang.MissingMethodException;
import org.codehaus.groovy.grails.commons.ConfigurationHolder;
import org.codehaus.groovy.grails.commons.ControllerArtefactHandler;
import org.codehaus.groovy.grails.web.servlet.mvc.GrailsWebRequest;
import org.codehaus.groovy.grails.web.servlet.mvc.exceptions.CannotRedirectException;
import org.springframework.context.ApplicationContext;
import org.springframework.web.context.request.RequestContextHolder;

import javax.faces.context.FacesContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Collections;
import java.util.Map;

/**
 * org.doc4web.grails.jsf
 * <p/>
 * Date: 21 mars 2010
 * Time: 16:55:08
 *
 * @author Stephane MALDINI
 */
public class RenderDynamicMethod extends org.codehaus.groovy.grails.web.metaclass.RedirectDynamicMethod {

    private static final String METHOD_SIGNATURE = "render";

    private String extension = (String) ConfigurationHolder.getFlatConfig().get("jsf.extension");

    public RenderDynamicMethod(ApplicationContext applicationContext) {
        super(applicationContext);
    }

    @Override
    public Object invoke(Object target, String methodName, Object[] arguments) {
        if (arguments.length == 0)
            throw new MissingMethodException(METHOD_SIGNATURE, target.getClass(), arguments);

        Map argMap = arguments[0] instanceof Map ? (Map) arguments[0] : Collections.EMPTY_MAP;
        if (argMap.size() == 0) {
            throw new MissingMethodException(METHOD_SIGNATURE, target.getClass(), arguments);
        }

        GrailsWebRequest webRequest = (GrailsWebRequest) RequestContextHolder.currentRequestAttributes();

        HttpServletRequest request = webRequest.getCurrentRequest();
        HttpServletResponse response = webRequest.getCurrentResponse();

        if (request.getAttribute(GRAILS_REDIRECT_ISSUED) != null) {
            throw new CannotRedirectException("Cannot issue a redirect(..) here. A previous call to redirect(..) has already redirected the response.");
        }
        if (response.isCommitted()) {
            throw new CannotRedirectException("Cannot issue a redirect(..) here. The response has already been committed either by another redirect or by directly writing to the response.");
        }

        String bean = (String) argMap.get(RedirectDynamicMethod.BEAN_ARG);
        String action = (String) argMap.get(RedirectDynamicMethod.ACTION_ARG);
        String view = (String) argMap.get(RedirectDynamicMethod.VIEW_ARG);
        String sourceBean = GrailsNameUtils.getLogicalPropertyName(target.getClass().getName(), ControllerArtefactHandler.TYPE);

        if (action == null) {
            if (bean == null && view != null) {
                bean = sourceBean;
            } else if (bean != null && view == null) {
                view = RedirectDynamicMethod.DEFAULT_VIEW;
            }
            if (bean != null) {
                action = "/" + bean + "/" + view;
            }
        }
        FacesContext fc = FacesContext.getCurrentInstance();
        if (action != null) {
            fc.getApplication().getNavigationHandler().handleNavigation(fc, fc.getViewRoot().getViewId(),
                    action);
            return null;
        }

        return null;
    }
}