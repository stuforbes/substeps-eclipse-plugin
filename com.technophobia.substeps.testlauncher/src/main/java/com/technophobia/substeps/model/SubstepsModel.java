package com.technophobia.substeps.model;

import java.util.List;

import com.technophobia.substeps.FeatureRunnerPlugin;
import com.technophobia.substeps.junit.ui.SubstepsRunSession;
import com.technophobia.substeps.junit.ui.SubstepsRunSessionListener;

public interface SubstepsModel {

    /**
     * Starts the model (called by the {@link FeatureRunnerPlugin} on startup).
     */
    void start();


    /**
     * Stops the model (called by the {@link FeatureRunnerPlugin} on shutdown).
     */
    void stop();


    void addTestRunSessionListener(SubstepsRunSessionListener listener);


    void removeTestRunSessionListener(SubstepsRunSessionListener listener);


    /**
     * @return a list of active {@link SubstepsRunSession}s. The list is a copy
     *         of the internal data structure and modifications do not affect
     *         the global list of active sessions. The list is sorted by age,
     *         youngest first.
     * @deprecated - Shouldn't be exposing internal state - relic of v1 runner
     */
    @Deprecated
    List<SubstepsRunSession> getTestRunSessions();


    /**
     * Adds the given {@link SubstepsRunSession} and notifies all registered
     * {@link SubstepsRunSessionListener}s.
     * 
     * @param substepsRunSession
     *            the session to add
     */
    void addTestRunSession(SubstepsRunSession substepsRunSession);


    /**
     * Removes the given {@link SubstepsRunSession} and notifies all registered
     * {@link SubstepsRunSessionListener}s.
     * 
     * @param testRunSession
     *            the session to remove
     */
    void removeTestRunSession(SubstepsRunSession testRunSession);


    /**
     * @deprecated - Shouldn't be exposing internal state - relic of v1 runner
     */
    @Deprecated
    SubstepsRunListener[] getTestRunListeners();

}