package org.doc4web.grails.jsf.faces;

import com.sun.faces.application.view.MultiViewHandler;

import javax.faces.context.FacesContext;

/**
 * org.doc4web.grails.jsf.faces
 * <p/>
 * Date: 30 mars 2010
 * Time: 17:39:16
 *
 * @author Stephane MALDINI
 */
public class ExtMultiViewHandler extends MultiViewHandler {
    public ExtMultiViewHandler() {
        super();
    }

    @Override
    public String deriveViewId(FacesContext context, String rawViewId) {
        return derivePhysicalViewId(context, rawViewId, false);
    }

}
