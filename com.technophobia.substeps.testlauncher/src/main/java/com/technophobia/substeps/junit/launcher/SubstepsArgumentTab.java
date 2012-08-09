package com.technophobia.substeps.junit.launcher;

import java.text.MessageFormat;
import java.util.Iterator;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.ui.AbstractLaunchConfigurationTab;
import org.eclipse.jdt.core.IJavaModel;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.search.IJavaSearchConstants;
import org.eclipse.jdt.core.search.IJavaSearchScope;
import org.eclipse.jdt.internal.core.search.JavaSearchScope;
import org.eclipse.jdt.internal.ui.dialogs.OpenTypeSelectionDialog;
import org.eclipse.jdt.ui.JavaElementLabelProvider;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ListViewer;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.List;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.dialogs.ElementListSelectionDialog;
import org.eclipse.ui.dialogs.ElementTreeSelectionDialog;
import org.eclipse.ui.dialogs.ISelectionStatusValidator;
import org.eclipse.ui.model.BaseWorkbenchContentProvider;
import org.eclipse.ui.model.WorkbenchLabelProvider;

import com.technophobia.eclipse.launcher.config.SubstepsLaunchConfigurationConstants;
import com.technophobia.eclipse.transformer.Transformer;
import com.technophobia.substeps.FeatureRunnerPlugin;
import com.technophobia.substeps.junit.ui.SubstepsFeatureMessages;

public class SubstepsArgumentTab extends AbstractLaunchConfigurationTab {

    private Text projectText = null;
    private Text featureFileLocationText = null;
    private Text substepsLocationText = null;
    private Button substepsLocationButton = null;

    private Button addBeforeAndAfterProcessorButton;

    private Button removeBeforeAndAfterProcessorButton;

    private ListViewer beforeAndAfterProcessorsList;

    private Button featureFileLocationButton;

    private final Transformer<IProject, String> defaultSubstepsLocationFinder;


    public SubstepsArgumentTab() {
        this.defaultSubstepsLocationFinder = new DefaultSubstepsLocationFinder();
    }


    @Override
    public void createControl(final Composite parent) {
        final Composite comp = new Composite(parent, SWT.NONE);
        setControl(comp);
        PlatformUI.getWorkbench().getHelpSystem().setHelp(getControl(), getHelpContextId());
        final GridLayout layout = new GridLayout();
        layout.numColumns = 3;
        comp.setLayout(layout);
        comp.setFont(parent.getFont());

        createProjectConfigComponent(comp);
        createFeatureFileConfigComponent(comp);
        createSubstepsConfigComponent(comp);
        createBeforeAndAfterProcessorsConfigComponent(comp);
    }


    @Override
    public void initializeFrom(final ILaunchConfiguration config) {
        final String projectName = getConfigAttribute(config, SubstepsLaunchConfigurationConstants.ATTR_FEATURE_PROJECT);
        final String featureFile = getConfigAttribute(config, SubstepsFeatureLaunchShortcut.ATTR_FEATURE_FILE);
        final String substepsFilename = getConfigAttribute(config,
                SubstepsLaunchConfigurationConstants.ATTR_SUBSTEPS_FILE);
        final String beforeAndAfterProcessors = getConfigAttribute(config,
                SubstepsLaunchConfigurationConstants.ATTR_BEFORE_AND_AFTER_PROCESSORS);

        projectText.setText(projectName);
        featureFileLocationText.setText(featureFile);
        substepsLocationText.setText(substepsFilename);

        if (!beforeAndAfterProcessors.trim().isEmpty()) {
            final String[] split = beforeAndAfterProcessors.split(";");
            for (final String processor : split) {
                beforeAndAfterProcessorsList.add(processor);
            }
        }

        validatePage();
    }


    @Override
    public String getName() {
        return SubstepsFeatureMessages.SubstepsLaunchConfigurationTab_tab_label;
    }


    @Override
    public void performApply(final ILaunchConfigurationWorkingCopy config) {
        if (projectText != null) {
            config.setAttribute(SubstepsLaunchConfigurationConstants.ATTR_FEATURE_PROJECT, projectText.getText().trim());
        }
        if (featureFileLocationText != null) {
            config.setAttribute(SubstepsFeatureLaunchShortcut.ATTR_FEATURE_FILE, featureFileLocationText.getText()
                    .trim());
        }
        if (substepsLocationText != null) {
            config.setAttribute(SubstepsLaunchConfigurationConstants.ATTR_SUBSTEPS_FILE, substepsLocationText.getText()
                    .trim());
        }

        final String[] items = beforeAndAfterProcessorsList.getList().getItems();
        final StringBuilder sb = new StringBuilder();
        for (final String item : items) {
            sb.append(item);
            sb.append(";");
        }
        config.setAttribute(SubstepsLaunchConfigurationConstants.ATTR_BEFORE_AND_AFTER_PROCESSORS, sb.toString());
    }


    @Override
    public void setDefaults(final ILaunchConfigurationWorkingCopy configuration) {
        final IResource currentResource = getContext();
        if (currentResource != null) {
            final IProject project = currentResource.getProject();
            configuration.setAttribute(SubstepsLaunchConfigurationConstants.ATTR_FEATURE_PROJECT, project.getName());

            if (currentResource instanceof IFile) {
                final IFile file = (IFile) currentResource;
                if (file.getFileExtension().equalsIgnoreCase("feature")) {
                    configuration.setAttribute(SubstepsFeatureLaunchShortcut.ATTR_FEATURE_FILE,
                            projectLocalisedPathFor(file));
                }
            }

            final String substepsFolder = defaultSubstepsLocationFinder.to(project);
            if (substepsFolder != null) {
                configuration.setAttribute(SubstepsLaunchConfigurationConstants.ATTR_SUBSTEPS_FILE, substepsFolder);
            } else {
                configuration.setAttribute(SubstepsLaunchConfigurationConstants.ATTR_SUBSTEPS_FILE, "");
            }
        }
    }


    private void createProjectConfigComponent(final Composite comp) {
        final Label projectLabel = new Label(comp, SWT.NONE);
        projectLabel.setText(SubstepsFeatureMessages.SubstepsLaunchConfigurationTab_label_project_location);
        final GridData gd = new GridData();
        gd.horizontalIndent = 25;
        projectLabel.setLayoutData(gd);

        projectText = new Text(comp, SWT.SINGLE | SWT.BORDER);
        projectText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        projectText.addModifyListener(new ModifyListener() {
            @Override
            public void modifyText(final ModifyEvent evt) {
                validatePage();
                updateLaunchConfigurationDialog();
                final boolean isProjectSpecified = projectText.getText().length() > 0;
                featureFileLocationButton.setEnabled(isProjectSpecified);
                substepsLocationButton.setEnabled(isProjectSpecified);
                addBeforeAndAfterProcessorButton.setEnabled(isProjectSpecified);
            }
        });

        final Button projectButton = new Button(comp, SWT.PUSH);
        projectButton.setText(SubstepsFeatureMessages.SubstepsLaunchConfigurationTab_browse_project_location);
        projectButton.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(final SelectionEvent evt) {
                final String projectName = handleProjectButtonSelected();
                if (projectName != null && !projectName.isEmpty()) {
                    projectText.setText(projectName);
                }
            }
        });
    }


    private void createFeatureFileConfigComponent(final Composite comp) {
        final Label featureFileLocationLabel = new Label(comp, SWT.NONE);
        featureFileLocationLabel.setText(SubstepsFeatureMessages.SubstepsLaunchConfigurationTab_label_feature_location);
        final GridData gd = new GridData();
        gd.horizontalIndent = 25;
        featureFileLocationLabel.setLayoutData(gd);

        featureFileLocationText = new Text(comp, SWT.SINGLE | SWT.BORDER);
        featureFileLocationText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        featureFileLocationText.addModifyListener(new ModifyListener() {
            @Override
            public void modifyText(final ModifyEvent evt) {
                validatePage();
                updateLaunchConfigurationDialog();
            }
        });

        featureFileLocationButton = new Button(comp, SWT.PUSH);
        featureFileLocationButton
                .setText(SubstepsFeatureMessages.SubstepsLaunchConfigurationTab_browse_feature_location);
        featureFileLocationButton.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(final SelectionEvent evt) {
                final String newLocation = handleFeatureFileLocationButtonSelected();
                if (newLocation != null && !newLocation.isEmpty()) {
                    featureFileLocationText.setText(newLocation);
                }
            }
        });
    }


    private void createSubstepsConfigComponent(final Composite comp) {
        final Label substepsLocationLabel = new Label(comp, SWT.NONE);
        substepsLocationLabel.setText(SubstepsFeatureMessages.SubstepsLaunchConfigurationTab_label_substeps_location);
        final GridData gd = new GridData();
        gd.horizontalIndent = 25;
        substepsLocationLabel.setLayoutData(gd);

        substepsLocationText = new Text(comp, SWT.SINGLE | SWT.BORDER);
        substepsLocationText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        substepsLocationText.addModifyListener(new ModifyListener() {
            @Override
            public void modifyText(final ModifyEvent evt) {
                validatePage();
                updateLaunchConfigurationDialog();
            }
        });

        substepsLocationButton = new Button(comp, SWT.PUSH);
        substepsLocationButton.setText(SubstepsFeatureMessages.SubstepsLaunchConfigurationTab_browse_substeps_location);
        substepsLocationButton.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(final SelectionEvent evt) {
                final String newLocation = handleSubstepsLocationButtonSelected();
                if (newLocation != null && !newLocation.isEmpty()) {
                    substepsLocationText.setText(newLocation);
                }
            }
        });
    }


    private void createBeforeAndAfterProcessorsConfigComponent(final Composite comp) {

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
                }
            }
        });
    }


    /*
     * Show a dialog that lets the user select a folder or substeps file.
     */
    private String handleProjectButtonSelected() {
        final IJavaProject project = chooseJavaProject();
        if (project == null) {
            return "";
        }

        return project.getElementName();
    }


    /*
     * Show a dialog that lets the user select a folder or substeps file.
     */
    private String handleFeatureFileLocationButtonSelected() {
        final IResource resource = chooseFeatureFileResource();
        if (resource == null) {
            return "";
        }

        return projectLocalisedPathFor(resource);
    }


    /*
     * Show a dialog that lets the user select a folder or substeps file.
     */
    private String handleSubstepsLocationButtonSelected() {
        final IResource resource = chooseSubstepsResource();
        if (resource == null) {
            return "";
        }

        return projectLocalisedPathFor(resource);
    }


    private String handleAddBeforeAndAfterProcessor() {
        final IType javaType = chooseJavaType();
        if (javaType != null) {
            return javaType.getFullyQualifiedName();
        }
        return null;
    }


    /*
     * Realize a Java Project selection dialog and return the first selected
     * project, or null if there was none.
     */
    private IJavaProject chooseJavaProject() {
        IJavaProject[] projects;
        try {
            projects = JavaCore.create(getWorkspaceRoot()).getJavaProjects();
        } catch (final JavaModelException e) {
            FeatureRunnerPlugin.log(e);
            projects = new IJavaProject[0];
        }

        final ILabelProvider labelProvider = new JavaElementLabelProvider(JavaElementLabelProvider.SHOW_DEFAULT);
        final ElementListSelectionDialog dialog = new ElementListSelectionDialog(getShell(), labelProvider);
        dialog.setTitle(SubstepsFeatureMessages.SubstepsLaunchConfigurationTab_projectdialog_title);
        dialog.setMessage(SubstepsFeatureMessages.SubstepsLaunchConfigurationTab_projectdialog_message);
        dialog.setElements(projects);

        final IJavaProject javaProject = getJavaProject();
        if (javaProject != null) {
            dialog.setInitialSelections(new Object[] { javaProject });
        }
        if (dialog.open() == Window.OK) {
            return (IJavaProject) dialog.getFirstResult();
        }
        return null;
    }


    private IType chooseJavaType() {

        final ILabelProvider labelProvider = new JavaElementLabelProvider(JavaElementLabelProvider.SHOW_DEFAULT);
        final OpenTypeSelectionDialog dialog = new OpenTypeSelectionDialog(getShell(), false, PlatformUI.getWorkbench()
                .getProgressService(), createSearchScope(), IJavaSearchConstants.TYPE);
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


    private IResource chooseFeatureFileResource() {

        final ILabelProvider labelProvider = new WorkbenchLabelProvider();
        final ElementTreeSelectionDialog dialog = new ElementTreeSelectionDialog(getShell(), labelProvider,
                new BaseWorkbenchContentProvider());
        dialog.setTitle(SubstepsFeatureMessages.SubstepsLaunchConfigurationTab_featurefiledialog_title);
        dialog.setMessage(SubstepsFeatureMessages.SubstepsLaunchConfigurationTab_featurefiledialog_message);
        dialog.setInput(project());
        dialog.setAllowMultiple(false);
        dialog.setValidator(new ISelectionStatusValidator() {

            @Override
            public IStatus validate(final Object[] selection) {
                if (selection.length > 0) {
                    final Object item = selection[0];
                    if (item instanceof IFile) {
                        final IFile file = (IFile) item;
                        if ("feature".equalsIgnoreCase(file.getFileExtension())) {
                            return new Status(IStatus.OK, FeatureRunnerPlugin.PLUGIN_ID, "");
                        }
                    }
                }
                return new Status(IStatus.ERROR, FeatureRunnerPlugin.PLUGIN_ID,
                        SubstepsFeatureMessages.SubstepsLaunchConfigurationTab_error_notFeatureFile);
            }
        });

        if (dialog.open() == Window.OK) {
            return (IResource) dialog.getFirstResult();
        }
        return null;
    }


    private IProject project() {
        return ResourcesPlugin.getWorkspace().getRoot().getProject(projectText.getText().trim());
    }


    private IResource chooseSubstepsResource() {

        final ILabelProvider labelProvider = new WorkbenchLabelProvider();
        final ElementTreeSelectionDialog dialog = new ElementTreeSelectionDialog(getShell(), labelProvider,
                new BaseWorkbenchContentProvider());
        dialog.setTitle(SubstepsFeatureMessages.SubstepsLaunchConfigurationTab_substepsdialog_title);
        dialog.setMessage(SubstepsFeatureMessages.SubstepsLaunchConfigurationTab_substepsdialog_message);
        dialog.setInput(project());
        dialog.setAllowMultiple(false);
        dialog.setValidator(new ISelectionStatusValidator() {

            @Override
            public IStatus validate(final Object[] selection) {
                if (selection.length > 0) {
                    final Object item = selection[0];
                    if (item instanceof IFile) {
                        final IFile file = (IFile) item;
                        if (!"substeps".equalsIgnoreCase(file.getFileExtension())) {
                            return new Status(IStatus.ERROR, FeatureRunnerPlugin.PLUGIN_ID,
                                    SubstepsFeatureMessages.SubstepsLaunchConfigurationTab_error_notSubstepsFile);
                        }
                    }
                }
                return new Status(IStatus.OK, FeatureRunnerPlugin.PLUGIN_ID, "");
            }
        });

        if (dialog.open() == Window.OK) {
            return (IResource) dialog.getFirstResult();
        }
        return null;
    }


    private void validatePage() {

        setErrorMessage(null);
        setMessage(null);

        final String projectName = projectText.getText().trim();
        if (projectName.length() == 0) {
            setErrorMessage(SubstepsFeatureMessages.SubstepsLaunchConfigurationTab_error_projectnotdefined);
            return;
        }

        final IStatus status = ResourcesPlugin.getWorkspace().validatePath(IPath.SEPARATOR + projectName,
                IResource.PROJECT);
        if (!status.isOK() || !Path.ROOT.isValidSegment(projectName)) {
            setErrorMessage(MessageFormat.format(
                    SubstepsFeatureMessages.SubstepsLaunchConfigurationTab_error_invalidProjectName, projectName));
            return;
        }

        final IProject project = getWorkspaceRoot().getProject(projectName);
        if (!project.exists()) {
            setErrorMessage(SubstepsFeatureMessages.SubstepsLaunchConfigurationTab_error_projectnotexists);
            return;
        }
        try {
            if (!project.hasNature(JavaCore.NATURE_ID)) {
                setErrorMessage(SubstepsFeatureMessages.SubstepsLaunchConfigurationTab_error_notJavaProject);
                return;
            }
            final String substepsFileName = substepsLocationText.getText().trim();
            if (substepsFileName.length() == 0) {
                setErrorMessage(SubstepsFeatureMessages.SubstepsLaunchConfigurationTab_error_substepsnotdefined);
                return;
            }
            if (!validSubstepsFile(project, substepsFileName)) {
                return;
            }
            final String featureFileName = featureFileLocationText.getText().trim();
            if (featureFileName.length() == 0) {
                setErrorMessage(SubstepsFeatureMessages.SubstepsLaunchConfigurationTab_error_featurefilenotdefined);
                return;
            }
            if (!validFeatureFile(project, featureFileName)) {
                return;
            }
        } catch (final CoreException e) {
            FeatureRunnerPlugin.log(e);
        }

        final IJavaProject javaProject = JavaCore.create(project);

        final List list = beforeAndAfterProcessorsList.getList();
        try {
            for (final String item : list.getItems()) {
                if (javaProject.findType(item) == null) {
                    setErrorMessage(MessageFormat.format(
                            SubstepsFeatureMessages.SubstepsLaunchConfigurationTab_error_beforeafterprocessornotexists,
                            item));
                }
            }
        } catch (final JavaModelException ex) {
            FeatureRunnerPlugin.log(ex);
        }
    }


    /*
     * Convenience method to get the workspace root.
     */
    private IWorkspaceRoot getWorkspaceRoot() {
        return ResourcesPlugin.getWorkspace().getRoot();
    }


    /*
     * Return the IJavaProject corresponding to the project name in the project
     * name text field, or null if the text does not match a project name.
     */
    private IJavaProject getJavaProject() {
        final String projectName = projectText.getText().trim();
        if (projectName.length() < 1) {
            return null;
        }
        return getJavaModel().getJavaProject(projectName);
    }


    /*
     * Convenience method to get access to the java model.
     */
    private IJavaModel getJavaModel() {
        return JavaCore.create(getWorkspaceRoot());
    }


    /**
     * Determine if a substeps file folder exists under project with name
     * 
     * @param project
     *            The project where substeps lives
     * @param substepsFileName
     *            file/folder name, relative to project
     * @return true if substeps file/folder exists, otherwise false
     */
    private boolean validSubstepsFile(final IProject project, final String substepsFileName) {
        if (substepsFileName.endsWith(".substeps")) {
            if (!project.getFile(substepsFileName).exists()) {
                setErrorMessage(SubstepsFeatureMessages.SubstepsLaunchConfigurationTab_error_substepsnotexists);
                return false;
            }
        } else if (substepsFileName.indexOf('.') > -1) {
            setErrorMessage(SubstepsFeatureMessages.SubstepsLaunchConfigurationTab_error_notSubstepsFile);
            return false;
        } else {
            if (!project.getFolder(substepsFileName).exists()) {
                setErrorMessage(SubstepsFeatureMessages.SubstepsLaunchConfigurationTab_error_substepsnotexists);
                return false;
            }
        }
        return true;
    }


    /**
     * Determine if a feature file exists under project with name
     * 
     * @param project
     *            The project where substeps lives
     * @param featureFileName
     *            file name, relative to project
     * @return true if feature file exists, otherwise false
     */
    private boolean validFeatureFile(final IProject project, final String featureFileName) {
        if (featureFileName.endsWith(".feature")) {
            if (!project.getFile(featureFileName).exists()) {
                setErrorMessage(SubstepsFeatureMessages.SubstepsLaunchConfigurationTab_error_featurefilenotexists);
                return false;
            }
        } else {
            setErrorMessage(SubstepsFeatureMessages.SubstepsLaunchConfigurationTab_error_notFeatureFile);
            return false;
        }
        return true;
    }


    private String getConfigAttribute(final ILaunchConfiguration config, final String configName) {
        try {
            return config.getAttribute(configName, "");
        } catch (final CoreException e) {
            FeatureRunnerPlugin.log(e);
            return "";
        }
    }


    /*
     * Returns the current resource element context from which to initialize
     * default settings, or <code>null</code> if none.
     * 
     * @return resource context.
     */
    private IResource getContext() {
        final IWorkbenchWindow activeWorkbenchWindow = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
        if (activeWorkbenchWindow == null) {
            return null;
        }
        final IWorkbenchPage page = activeWorkbenchWindow.getActivePage();
        if (page != null) {
            final ISelection selection = page.getSelection();
            if (selection instanceof IStructuredSelection) {
                final IStructuredSelection ss = (IStructuredSelection) selection;
                if (!ss.isEmpty()) {
                    final Object obj = ss.getFirstElement();

                    if (obj instanceof IResource) {
                        return (IResource) obj;
                    }
                }
            }
            final IEditorPart part = page.getActiveEditor();
            if (part != null) {
                final IEditorInput input = part.getEditorInput();
                return (IResource) input.getAdapter(IResource.class);
            }
        }
        return null;
    }


    /**
     * Convert a resource to its os-specific project localised path string
     * 
     * @param resource
     *            to be localised
     * @return localised path
     */
    private String projectLocalisedPathFor(final IResource resource) {
        return resource.getFullPath().removeFirstSegments(1).toOSString();
    }
}
