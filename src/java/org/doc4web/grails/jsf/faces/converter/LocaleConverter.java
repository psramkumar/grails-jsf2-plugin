package org.doc4web.grails.jsf.faces.converter;

import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;
import java.util.Locale;

/**
 * org.doc4web.grails.jsf.faces
 * <p/>
 * Date: 23 mars 2010
 * Time: 12:58:08
 *
 * @author Stephane MALDINI
 */
public class LocaleConverter implements Converter {
    /**
     * <p>Convert the specified string value, which is associated with
     * the specified {@link javax.faces.component.UIComponent}, into a model data object that
     * is appropriate for being stored during the <em>Apply Request
     * Values</em> phase of the request processing lifecycle.</p>
     *
     * @param context   {@link javax.faces.context.FacesContext} for the request being processed
     * @param component {@link javax.faces.component.UIComponent} with which this model object
     *                  value is associated
     * @param value     String value to be converted (may be <code>null</code>)
     * @return <code>null</code> if the value to convert is <code>null</code>,
     *         otherwise the result of the conversion
     * @throws javax.faces.convert.ConverterException
     *                              if conversion cannot be successfully
     *                              performed
     * @throws NullPointerException if <code>context</code> or
     *                              <code>component</code> is <code>null</code>
     */
    public Object getAsObject(FacesContext context, UIComponent component, String value) {
        String[] values = value.split("_");
        if (values.length > 2)
            return new Locale(values[0], values[1]);  //To change body of implemented methods use File | Settings | File Templates.
        else
            return new Locale(value);
    }

    /**
     * <p>Convert the specified model object value, which is associated with
     * the specified {@link javax.faces.component.UIComponent}, into a String that is suitable
     * for being included in the response generated during the
     * <em>Render Response</em> phase of the request processing
     * lifeycle.</p>
     *
     * @param context   {@link javax.faces.context.FacesContext} for the request being processed
     * @param component {@link javax.faces.component.UIComponent} with which this model object
     *                  value is associated
     * @param value     Model object value to be converted
     *                  (may be <code>null</code>)
     * @return a zero-length String if value is <code>null</code>,
     *         otherwise the result of the conversion
     * @throws javax.faces.convert.ConverterException
     *                              if conversion cannot be successfully
     *                              performed
     * @throws NullPointerException if <code>context</code> or
     *                              <code>component</code> is <code>null</code>
     */
    public String getAsString(FacesContext context, UIComponent component, Object value) {
        return value.toString();
    }
}