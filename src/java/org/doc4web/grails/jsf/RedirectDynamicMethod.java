package org.doc4web.grails.jsf;

import com.sun.faces.util.Util;
import grails.util.GrailsNameUtils;
import groovy.lang.MissingMethodException;
import org.codehaus.groovy.grails.commons.ConfigurationHolder;
import org.codehaus.groovy.grails.web.servlet.GrailsApplicationAttributes;
import org.codehaus.groovy.grails.web.servlet.mvc.GrailsWebRequest;
import org.codehaus.groovy.grails.web.servlet.mvc.exceptions.CannotRedirectException;
import org.springframework.context.ApplicationContext;
import org.springframework.web.context.request.RequestContextHolder;

import javax.faces.application.ViewHandler;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
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
public class RedirectDynamicMethod extends org.codehaus.groovy.grails.web.metaclass.RedirectDynamicMethod {
	public static final String METHOD_SIGNATURE = "redirect";
	public static final String ARGUMENT_URI = "uri";
	public static final String ARGUMENT_URL = "url";
	public static final String BEAN_ARG = "bean";
	public static final String VIEW_ARG = "view";
	public static final String ACTION_ARG = "action";
	public static final String DEFAULT_VIEW = "index";

	private String extension = (String) ConfigurationHolder.getFlatConfig().get("icefaces.extension");

	public RedirectDynamicMethod(ApplicationContext applicationContext) {
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

		Object uri = argMap.get(ARGUMENT_URI);
		String url = argMap.containsKey(ARGUMENT_URL) ? argMap.get(ARGUMENT_URL).toString() : null;
		String bean = (String) argMap.get(BEAN_ARG);
		String action = (String) argMap.get(ACTION_ARG);
		String view = (String) argMap.get(VIEW_ARG);
		String sourceBean = GrailsNameUtils.getLogicalPropertyName(target.getClass().getName(), BeanArtefactHandler.TYPE);

		if (action == null) {
			if (bean != null) {
				action = "/" + bean + "/" + (view == null ? DEFAULT_VIEW : view );
			}else{
				action = view;
			}
		}
		GrailsApplicationAttributes attrs = webRequest.getAttributes();

		FacesContext fc = FacesContext.getCurrentInstance();
		if (action != null) {
			//fc.getApplication().getNavigationHandler().handleNavigation(fc, fc.getViewRoot().getViewId(), action);
			ViewHandler viewHandler = Util.getViewHandler(fc);
			ExternalContext extContext = fc.getExternalContext();
			String newPath = viewHandler.getActionURL(fc, action);
			try {
				extContext.redirect(extContext.encodeActionURL(newPath));
				fc.responseComplete();
			} catch (IOException e) {
				throw new CannotRedirectException("IO Exception " + e.getMessage());
			}
			return null;
		}
		try {
			String actualUri = null;
			if (uri != null) {
				actualUri = attrs.getApplicationUri(request) + uri.toString();
			} else if (url != null) {
				actualUri = url;
			}
			fc.getExternalContext().redirect(actualUri);
		} catch (IOException e) {
			throw new CannotRedirectException("IO Exception " + e.getMessage());
		}
		return null;
	}
}