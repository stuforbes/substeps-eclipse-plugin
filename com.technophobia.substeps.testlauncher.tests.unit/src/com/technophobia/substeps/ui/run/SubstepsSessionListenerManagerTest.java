package com.technophobia.substeps.ui.run;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertThat;

import java.util.List;

import org.jmock.Expectations;
import org.jmock.Mockery;
import org.jmock.integration.junit4.JMock;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import com.technophobia.substeps.junit.ui.SubstepsRunSession;
import com.technophobia.substeps.junit.ui.SubstepsRunSessionListener;
import com.technophobia.substeps.model.SubstepsModel;
import com.technophobia.substeps.model.SubstepsRunListener;
import com.technophobia.substeps.model.SubstepsSessionListener;

@RunWith(JMock.class)
public class SubstepsSessionListenerManagerTest {

    private Mockery context;

    private SubstepsSessionListener sessionListener;

    private SubstepsSessionListenerManager sessionListenerManager;

    private FakeSubstepsModel substepsModel;


    @Before
    public void initialise() {
        this.context = new Mockery();

        this.sessionListener = context.mock(SubstepsSessionListener.class);
        this.substepsModel = new FakeSubstepsModel();

        this.sessionListenerManager = new SubstepsSessionListenerManager(sessionListener);
    }


    @Test
    public void addsSessionListenerToNewlyCreatedSessions() {

        sessionListenerManager.registerListenersOn(substepsModel);

        final SubstepsRunSession runSession = context.mock(SubstepsRunSession.class);

        context.checking(new Expectations() {
            {
                oneOf(runSession).addTestSessionListener(sessionListener);
            }
        });

        assertThat(substepsModel.getListener(), is(notNullValue()));
        substepsModel.getListener().sessionAdded(runSession);
    }


    @Test
    public void removesSessionListenerToRecentlyDeactivatedSessions() {

        sessionListenerManager.registerListenersOn(substepsModel);

        final SubstepsRunSession runSession = context.mock(SubstepsRunSession.class);

        context.checking(new Expectations() {
            {
                oneOf(runSession).removeTestSessionListener(sessionListener);
            }
        });

        assertThat(substepsModel.getListener(), is(notNullValue()));
        substepsModel.getListener().sessionRemoved(runSession);
    }

    private static final class FakeSubstepsModel implements SubstepsModel {

        private SubstepsRunSessionListener runSessionListener;


        SubstepsRunSessionListener getListener() {
            return runSessionListener;
        }


        @Override
        public void start() {
            // TODO Auto-generated method stub

        }


        @Override
        public void stop() {
            // TODO Auto-generated method stub

        }


        @Override
        public void addTestRunSessionListener(final SubstepsRunSessionListener listener) {
            this.runSessionListener = listener;
        }


        @Override
        public void removeTestRunSessionListener(final SubstepsRunSessionListener listener) {
            assertThat(listener, is(runSessionListener));
            this.runSessionListener = null;
        }


        @Override
        @Deprecated
        public List<SubstepsRunSession> getTestRunSessions() {
            // TODO Auto-generated method stub
            return null;
        }


        @Override
        public void addTestRunSession(final SubstepsRunSession substepsRunSession) {
            // TODO Auto-generated method stub

        }


        @Override
        public void removeTestRunSession(final SubstepsRunSession testRunSession) {
            // TODO Auto-generated method stub

        }


        @Override
        @Deprecated
        public SubstepsRunListener[] getTestRunListeners() {
            // TODO Auto-generated method stub
            return null;
        }

    }
}
