package com.technophobia.substeps.junit.launcher.tab.component;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.search.IJavaSearchConstants;
import org.eclipse.jdt.core.search.IJavaSearchScope;
import org.eclipse.jdt.internal.core.search.JavaSearchScope;
import org.eclipse.jdt.internal.ui.dialogs.OpenTypeSelectionDialog;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ListViewer;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.List;
import org.eclipse.ui.PlatformUI;

import com.technophobia.eclipse.transformer.Callback;
import com.technophobia.substeps.FeatureRunnerPlugin;
import com.technophobia.substeps.junit.launcher.model.SubstepsLaunchModel;
import com.technophobia.substeps.junit.ui.SubstepsFeatureMessages;
import com.technophobia.substeps.supplier.Supplier;

@SuppressWarnings("restriction")
public class BeforeAndAfterProcessorsComponent extends AbstractTabComponent {

    private static final String[] DEFAULT_BEFORE_AND_AFTERS = new String[] { "com.technophobia.webdriver.substeps.runner.DefaultExecutionSetupTearDown" };

    private Button addBeforeAndAfterProcessorButton;
    private Button removeBeforeAndAfterProcessorButton;
    private ListViewer beforeAndAfterProcessorsList;


    public BeforeAndAfterProcessorsComponent(final Callback onChangeCallback, final Supplier<IProject> projectSupplier) {
        super(onChangeCallback, projectSupplier);
    }


    @Override
    public void initializeFrom(final SubstepsLaunchModel model) {
        final Collection<String> beforeAndAfterProcessors = model.getBeforeAndAfterProcessors();
        if (!beforeAndAfterProcessors.isEmpty()) {
            beforeAndAfterProcessorsList.getList().removeAll();
            beforeAndAfterProcessorsList.add(beforeAndAfterProcessors.toArray(new String[beforeAndAfterProcessors
                    .size()]));
        }
    }


    @Override
    public void saveTo(final SubstepsLaunchModel model) {
        model.setBeforeAndAfterProcessors(Arrays.asList(beforeAndAfterProcessorsList.getList().getItems()));
    }


    @Override
    public void setDefaultOn(final SubstepsLaunchModel model, final IResource currentResource) {
        final IProject project = currentResource.getProject();
        final IJavaProject javaProject = JavaCore.create(project);

        final Collection<String> beforeAndAfters = new ArrayList<String>();
        for (final String defaultBeforeAndAfter : DEFAULT_BEFORE_AND_AFTERS) {
            if (typeExistsInProject(defaultBeforeAndAfter, javaProject)) {
                beforeAndAfters.add(defaultBeforeAndAfter);
            }
        }
        model.setBeforeAndAfterProcessors(beforeAndAfters);
    }


    @Override
    public void validate(final Collection<String> errorMessageList) {
        final IJavaProject javaProject = JavaCore.create(project());

        final List list = beforeAndAfterProcessorsList.getList();
        try {
            for (final String item : list.getItems()) {
                if (javaProject.findType(item) == null) {
                    errorMessageList.add(MessageFormat.format(
                            SubstepsFeatureMessages.SubstepsLaunchConfigurationTab_error_beforeafterprocessornotexists,
                            item));
                }
            }
        } catch (final JavaModelException ex) {
            FeatureRunnerPlugin.log(ex);
        }
    }


    @Override
    public void create(final Composite comp) {
        final Label beforeAndAfterProcessorsLabel = new Label(comp, SWT.NONE);
        beforeAndAfterProcessorsLabel
                .setText(SubstepsFeatureMessages.SubstepsLaunchConfigurationTab_label_before_and_after_processors);
        GridData gd = new GridData(GridData.VERTICAL_ALIGN_BEGINNING | GridData.HORIZONTAL_ALIGN_FILL);
        gd.horizontalIndent = 25;
        beforeAndAfterProcessorsLabel.setLayoutData(gd);

        beforeAndAfterProcessorsList = new ListViewer(comp);
        final List list = beforeAndAfterProcessorsList.getList();
        list.setLayoutData(new GridData(GridData.FILL_BOTH));
        beforeAndAfterProcessorsList.addSelectionChangedListener(new ISelectionChangedListener() {
            @Override
            public void selectionChanged(final SelectionChangedEvent event) {
                removeBeforeAndAfterProcessorButton.setEnabled(event.getSelection() != null);
            }
        });

        final Composite pathButtonComp = new Composite(comp, SWT.NONE);
        final GridLayout pathButtonLayout = new GridLayout();
        pathButtonLayout.marginHeight = 0;
        pathButtonLayout.marginWidth = 0;
        pathButtonComp.setLayout(pathButtonLayout);
        gd = new GridData(GridData.VERTICAL_ALIGN_BEGINNING | GridData.HORIZONTAL_ALIGN_FILL);
        pathButtonComp.setLayoutData(gd);

        createBeforeAndAfterProcessorButtons(pathButtonComp);
    }


    @Override
    protected boolean isValid() {
        return beforeAndAfterProcessorsList.getList().getItemCount() > 0;
    }


    @Override
    public void enableControls() {
        addBeforeAndAfterProcessorButton.setEnabled(true);
        removeBeforeAndAfterProcessorButton.setEnabled(true);
    }


    @Override
    public void disableControls() {
        addBeforeAndAfterProcessorButton.setEnabled(false);
        removeBeforeAndAfterProcessorButton.setEnabled(false);
    }


    private void createBeforeAndAfterProcessorButtons(final Composite comp) {
        addBeforeAndAfterProcessorButton = new Button(comp, SWT.PUSH);
        addBeforeAndAfterProcessorButton.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_FILL,
                GridData.VERTICAL_ALIGN_FILL, false, false));
        addBeforeAndAfterProcessorButton
                .setText(SubstepsFeatureMessages.SubstepsLaunchConfigurationTab_button_add_before_and_after_processors);
        addBeforeAndAfterProcessorButton.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(final SelectionEvent e) {
                final String newType = handleAddBeforeAndAfterProcessor();
                if (newType != null) {
                    beforeAndAfterProcessorsList.add(newType);
                    onChange();
                }
            }
        });

        removeBeforeAndAfterProcessorButton = new Button(comp, SWT.PUSH);
        removeBeforeAndAfterProcessorButton
                .setText(SubstepsFeatureMessages.SubstepsLaunchConfigurationTab_button_remove_before_and_after_processors);
        removeBeforeAndAfterProcessorButton.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(final SelectionEvent e) {
                final IStructuredSelection selection = (IStructuredSelection) beforeAndAfterProcessorsList
                        .getSelection();
                final Iterator<?> it = selection.iterator();
                while (it.hasNext()) {
                    beforeAndAfterProcessorsList.remove(it.next());
                    onChange();
                }
            }
        });
    }


    private String handleAddBeforeAndAfterProcessor() {
        final IType javaType = chooseJavaType();
        if (javaType != null) {
            return javaType.getFullyQualifiedName();
        }
        return null;
    }


    private IType chooseJavaType() {

        final OpenTypeSelectionDialog dialog = new OpenTypeSelectionDialog(beforeAndAfterProcessorsList.getList()
                .getShell(), false, PlatformUI.getWorkbench().getProgressService(), createSearchScope(),
                IJavaSearchConstants.TYPE);
        dialog.setTitle(SubstepsFeatureMessages.SubstepsLaunchConfigurationTab_projectdialog_title);
        dialog.setMessage(SubstepsFeatureMessages.SubstepsLaunchConfigurationTab_projectdialog_message);

        final IJavaProject javaProject = getJavaProject();
        if (javaProject != null) {
            dialog.setInitialSelections(new Object[] { javaProject });
        }
        if (dialog.open() == Window.OK) {
            return (IType) dialog.getFirstResult();
        }
        return null;
    }


    private IJavaSearchScope createSearchScope() {
        final JavaSearchScope searchScope = new JavaSearchScope();
        try {
            searchScope.add(getJavaProject());
            return searchScope;
        } catch (final JavaModelException e) {
            FeatureRunnerPlugin.log(e);
            return null;
        }
    }


    private IJavaProject getJavaProject() {
        return JavaCore.create(project());
    }


    private boolean typeExistsInProject(final String typeStr, final IJavaProject javaProject) {
        try {
            final IType type = javaProject.findType(typeStr);
            return type != null && type.exists();
        } catch (final JavaModelException e) {
            FeatureRunnerPlugin.log(e);
            return false;
        }
    }
}
