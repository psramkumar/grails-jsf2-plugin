package org.doc4web.grails.jsf.faces;

/**
 * org.doc4web.grails.jsf.faces
 * <p/>
 * Date: 29 mars 2010
 * Time: 14:16:42
 *
 * @author Stephane MALDINI
 */
import java.util.HashMap;
import java.util.Map;

import javax.faces.context.FacesContext;

import org.springframework.beans.factory.ObjectFactory;
import org.springframework.beans.factory.config.Scope;

public class ViewScope implements Scope {

	public final String VIEW_SCOPE_KEY = "JSF2_VIEW_SCOPE";

    private long timestamp = 0;

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

	public Object get(String name, ObjectFactory objectFactory) {

		if (FacesContext.getCurrentInstance().getViewRoot() != null) {
			Map<String, Object> viewScope = extractViewScope();

			if (viewScope.get(name) == null) {
				Object object = objectFactory.getObject();
				viewScope.put(name, object);
				return object;
			} else {
				return viewScope.get(name);
			}
		} else {
			return null;
		}

	}

	@SuppressWarnings("unchecked")
	private Map<String, Object> extractViewScope() {
		Map<String, Object> attributes = FacesContext.getCurrentInstance().getViewRoot().getAttributes();

		Map<String, Object> viewScope = null;

		if (attributes.get(VIEW_SCOPE_KEY)==null) {
			viewScope = new HashMap<String, Object>();
			attributes.put(VIEW_SCOPE_KEY, viewScope);
		} else {
			viewScope = (Map<String, Object>) attributes.get(VIEW_SCOPE_KEY);
		}
		return viewScope;
	}

	public String getConversationId() {
		return null;
	}

	public void registerDestructionCallback(String name, Runnable callback) {
	}

    /**
     * Resolve the contextual object for the given key, if any.
     * E.g. the HttpServletRequest object for key "request".
     *
     * @param key the contextual key
     * @return the corresponding object, or <code>null</code> if none found
     */
    public Object resolveContextualObject(String key) {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public Object remove(String name) {
		if (FacesContext.getCurrentInstance().getViewRoot() != null) {
			Map<String, Object> viewScope = extractViewScope();
			return viewScope.remove(name);
		} else {
			return null;
		}
	}

}