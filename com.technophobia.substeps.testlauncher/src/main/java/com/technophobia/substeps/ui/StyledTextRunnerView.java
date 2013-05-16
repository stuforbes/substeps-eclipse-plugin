package com.technophobia.substeps.ui;

import org.eclipse.core.resources.IProject;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.custom.ViewForm;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Layout;
import org.eclipse.ui.IViewSite;
import org.eclipse.ui.IWorkbenchPartSite;

import com.technophobia.eclipse.transformer.Callback1;
import com.technophobia.eclipse.transformer.ProjectByNameLocator;
import com.technophobia.substeps.colour.ColourManager;
import com.technophobia.substeps.junit.ui.SubstepsIconProvider;
import com.technophobia.substeps.supplier.Callback;
import com.technophobia.substeps.ui.action.ShowErrorsAction;
import com.technophobia.substeps.ui.component.ListDelegateHierarchicalTextCollection;
import com.technophobia.substeps.ui.component.StyledDocumentSubstepsTextExecutionReporter;
import com.technophobia.substeps.ui.component.TextModelFragmentFactory;
import com.technophobia.substeps.ui.highlight.TextChangedToDocumentUpdater;
import com.technophobia.substeps.ui.results.StandardTestResultsView;
import com.technophobia.substeps.ui.session.SubstepsTestExecutionReporter;

public class StyledTextRunnerView implements RunnerView {

    private StandardTestResultsView resultsView;
    private StyledText errorComponent;

    private ViewForm topPanel;
    private ViewForm bottomPanel;

    private SashForm sash;
    private final IWorkbenchPartSite site;
    private final SubstepsIconProvider iconProvider;


    public StyledTextRunnerView(final ColourManager colourManager, final SubstepsIconProvider iconProvider,
            final IWorkbenchPartSite site) {
        this.iconProvider = iconProvider;
        this.site = site;
        this.resultsView = createTestResultsView(site, iconProvider, colourManager, errorViewCallback());
    }


    @Override
    public void createPartControl(final Composite parent) {

        this.sash = createComposite(parent);

        this.topPanel = createPanel(sash);
        this.bottomPanel = createPanel(sash);
        this.errorComponent = createErrorComponent(bottomPanel);
        initialiseTestResultsView(topPanel);

        sash.setMaximizedControl(topPanel);

        final IToolBarManager toolBarManager = ((IViewSite) site).getActionBars().getToolBarManager();
        toolBarManager.add(new ShowErrorsAction(setMaximisedControlOnSashCallback(null),
                setMaximisedControlOnSashCallback(topPanel), iconProvider));
    }


    @Override
    public void dispose() {
        resultsView.dispose();
        resultsView = null;

        topPanel.dispose();
        bottomPanel.dispose();
    }


    @Override
    public SubstepsTestExecutionReporter executionReporter() {
        final ListDelegateHierarchicalTextCollection textCollection = new ListDelegateHierarchicalTextCollection();
        final TextChangedToDocumentUpdater stateChangeHighlighter = new TextChangedToDocumentUpdater(
                resultsView.documentUpdater());
        final TextModelFragmentFactory textModelFragmentFactory = new TextModelFragmentFactory(textCollection,
                stateChangeHighlighter);
        return new StyledDocumentSubstepsTextExecutionReporter(textCollection, textModelFragmentFactory,
                stateChangeHighlighter, updateCurrentProjectCallback(), new ProjectByNameLocator());
    }


    protected ViewForm createPanel(final Composite parent) {
        final ViewForm viewForm = new ViewForm(parent, SWT.NONE);
        viewForm.setTopLeft(empty(viewForm));
        return viewForm;
    }


    protected SashForm createComposite(final Composite parent) {
        return new SashForm(parent, SWT.VERTICAL | SWT.NO_SCROLL);
    }


    protected StandardTestResultsView createTestResultsView(final IWorkbenchPartSite site,
            final SubstepsIconProvider iconProvider, final ColourManager colourManager,
            final Callback1<String> errorViewCallback) {
        return new StandardTestResultsView(site, iconProvider, colourManager, errorViewCallback);
    }


    protected void initialiseTestResultsView(final ViewForm parent) {
        resultsView.initialise(parent);
        parent.setContent(resultsView.getControl());
    }


    protected StyledText createErrorComponent(final ViewForm parent) {
        final StyledText text = new StyledText(parent, SWT.NONE);
        parent.setContent(text);
        return text;
    }


    private Callback setMaximisedControlOnSashCallback(final Control control) {
        return new Callback() {
            @Override
            public void doCallback() {
                sash.setMaximizedControl(control);
            }
        };
    }


    private Control empty(final Composite parent) {
        final Composite empty = new Composite(parent, SWT.NONE);
        empty.setLayout(new Layout() {
            @Override
            protected Point computeSize(final Composite composite, final int wHint, final int hHint,
                    final boolean flushCache) {
                return new Point(1, 1); // (0, 0) does not work with
                                        // super-intelligent ViewForm
            }


            @Override
            protected void layout(final Composite composite, final boolean flushCache) {
                // No-op
            }
        });
        return empty;
    }


    private Callback1<IProject> updateCurrentProjectCallback() {
        return new Callback1<IProject>() {
            @Override
            public void callback(final IProject project) {
                StyledTextRunnerView.this.resultsView.updateCurrentProject(project);
            }
        };
    }


    private Callback1<String> errorViewCallback() {
        return new Callback1<String>() {
            @Override
            public void callback(final String t) {
                errorComponent.setText(t);
            }
        };
    }
}
