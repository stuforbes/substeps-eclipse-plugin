package com.technophobia.substeps.ui.run;

import org.eclipse.core.runtime.IStatus;

import com.technophobia.substeps.FeatureRunnerPlugin;
import com.technophobia.substeps.junit.ui.SubstepsRunSession;
import com.technophobia.substeps.junit.ui.SubstepsRunSessionListener;
import com.technophobia.substeps.model.SubstepsModel;
import com.technophobia.substeps.model.SubstepsSessionListener;

/**
 * Handles the process of adding {@link SubstepsSessionListener}'s to new
 * {@link SubstepsRunSession} instances, and removing old ones when the Run
 * Session is deactivated
 * 
 * @author sforbes
 * 
 */
public class SubstepsSessionListenerManager {

    public SubstepsSessionListenerManager(final SubstepsModel substepsModel,
            final SubstepsSessionListener sessionListener) {
        substepsModel.addTestRunSessionListener(new SessionListenerProvidedRunSessionListener(sessionListener));
    }

    private static final class SessionListenerProvidedRunSessionListener implements SubstepsRunSessionListener {
        private final SubstepsSessionListener sessionListener;


        public SessionListenerProvidedRunSessionListener(final SubstepsSessionListener sessionListener) {
            this.sessionListener = sessionListener;
        }


        @Override
        public void sessionAdded(final SubstepsRunSession substepsRunSession) {
            FeatureRunnerPlugin.log(IStatus.INFO, "Session added - adding session listener");
            substepsRunSession.addTestSessionListener(sessionListener);
        }


        @Override
        public void sessionRemoved(final SubstepsRunSession substepsRunSession) {
            FeatureRunnerPlugin.log(IStatus.INFO, "Session removed - removing session listener");
            substepsRunSession.removeTestSessionListener(sessionListener);
        }
    }
}
