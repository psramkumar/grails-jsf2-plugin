package org.doc4web.grails.jsf.faces;

import org.hibernate.FlushMode;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.springframework.orm.hibernate3.SessionFactoryUtils;
import org.springframework.orm.hibernate3.SessionHolder;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import javax.faces.event.PhaseEvent;
import javax.faces.event.PhaseId;
import javax.faces.event.PhaseListener;

/**
 * org.doc4web.grails.jsf.faces
 * <p/>
 * Date: 21 mars 2010
 * Time: 02:40:30
 *
 * @author Stephane MALDINI
 */
public class GrailsHibernatePhaseListener implements PhaseListener {


    /**
     * <p>Handle a notification that the processing for a particular
     * phase has just been completed.</p>
     */


    public void afterPhase(PhaseEvent event) {
        this.endSession(event);
    }

    /**
     * <p>Handle a notification that the processing for a particular
     * phase of the request processing lifecycle is about to begin.</p>
     */
    public void beforePhase(PhaseEvent event) {
        this.initSession(event);
    }

    /**
     * <p>Return the identifier of the request processing phase during
     * which this listener is interested in processing {@link javax.faces.event.PhaseEvent}
     * events.  Legal values are the singleton instances defined by the
     * {@link javax.faces.event.PhaseId} class, including <code>PhaseId.ANY_PHASE</code>
     * to indicate an interest in being notified for all standard phases.</p>
     */
    public PhaseId getPhaseId() {
        return PhaseId.ANY_PHASE;
    }

    private static ThreadLocal<Boolean> boundByMe = new ThreadLocal<Boolean>() {
        protected synchronized Boolean initialValue() {
            return false;
        }
    };


    private void initSession(PhaseEvent event) {
        if (event.getPhaseId().equals(PhaseId.RESTORE_VIEW))
            boundByMe.set(bindSession());
    }

    private void endSession(PhaseEvent event) {
        if (event.getPhaseId().equals(PhaseId.RENDER_RESPONSE)) {
            final SessionHolder sessionHolder = (SessionHolder) TransactionSynchronizationManager.getResource(sessionFactory);
            if (sessionHolder != null && !FlushMode.MANUAL.equals(sessionHolder.getSession().getFlushMode())) {
                sessionHolder.getSession().flush();
            }
            if (boundByMe.get()) unbindSession();
        }

    }

    public void setSessionFactory(SessionFactory sessionFactory) {
        this.sessionFactory = sessionFactory;
    }

    private SessionFactory sessionFactory;


    private boolean bindSession() {
        if (sessionFactory == null) {
            throw new IllegalStateException("No sessionFactory property provided");
        }
        final Object inStorage = TransactionSynchronizationManager.getResource(sessionFactory);
        if (inStorage != null) {
            ((SessionHolder) inStorage).getSession().flush();
            return false;
        } else {
            Session session = SessionFactoryUtils.getSession(sessionFactory, true);
            session.setFlushMode(FlushMode.AUTO);
            TransactionSynchronizationManager.bindResource(sessionFactory, new SessionHolder(session));
            return true;
        }
    }

    private void unbindSession() {
        if (sessionFactory == null) {
            throw new IllegalStateException("No sessionFactory property provided");
        }
        SessionHolder sessionHolder = (SessionHolder) TransactionSynchronizationManager.getResource(sessionFactory);
        try {
            if (!FlushMode.MANUAL.equals(sessionHolder.getSession().getFlushMode())) {
                sessionHolder.getSession().flush();
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            TransactionSynchronizationManager.unbindResource(sessionFactory);
            SessionFactoryUtils.closeSession(sessionHolder.getSession());
        }
    }
}
