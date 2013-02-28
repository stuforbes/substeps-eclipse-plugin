package com.technophobia.substeps.ui.component;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import org.jmock.Expectations;
import org.jmock.Mockery;
import org.jmock.Sequence;
import org.jmock.integration.junit4.JMock;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import com.technophobia.eclipse.transformer.Callback1;
import com.technophobia.substeps.ui.component.TextModelFragment.TextState;

@RunWith(JMock.class)
public class TextModelFragmentTest {

    private Mockery context;

    private Callback1<TextModelFragment> stateChangedCallback;


    @SuppressWarnings("unchecked")
    @Before
    public void initialise() {
        this.context = new Mockery();

        this.stateChangedCallback = context.mock(Callback1.class, "StateChangedCallback");
    }


    @Test
    public void createChildIncrementsDepth() {
        final TextModelFragment root = TextModelFragment.createRootFragment("1", "root", 0, 0, stateChangedCallback);
        final TextModelFragment child = root.createChild("2", "child", 5, 1);
        final TextModelFragment grandchild = child.createChild("3", "grandchild", 11, 2);

        assertThat(root.depth(), is(0));
        assertThat(child.depth(), is(1));
        assertThat(grandchild.depth(), is(2));
    }


    @Test
    public void markInProgressUpdatesTextStateAccordingly() {
        final TextModelFragment fragment = TextModelFragment
                .createRootFragment("1", "text", 0, 0, stateChangedCallback);

        context.checking(new Expectations() {
            {
                oneOf(stateChangedCallback).callback(fragment);
            }
        });

        assertThat(fragment.textState(), is(TextState.Unprocessed));
        fragment.markInProgress();
        assertThat(fragment.textState(), is(TextState.InProgress));
    }


    @Test
    public void markAsCompleteUpdatesTextStateAccordingly() {
        final TextModelFragment fragment = TextModelFragment
                .createRootFragment("1", "text", 0, 0, stateChangedCallback);

        context.checking(new Expectations() {
            {
                oneOf(stateChangedCallback).callback(fragment);
            }
        });

        assertThat(fragment.textState(), is(TextState.Unprocessed));
        fragment.markComplete();
        assertThat(fragment.textState(), is(TextState.Passed));
    }


    @Test
    public void markAsCompleteUpdatesParentsStateCorrectly() {
        final TextModelFragment root = TextModelFragment.createRootFragment("1", "root", 0, 0, stateChangedCallback);
        final TextModelFragment child1 = root.createChild("2", "child1", 5, 1);
        final TextModelFragment child2 = root.createChild("3", "child2", 12, 2);
        final TextModelFragment grandchild1 = child2.createChild("4", "grandchild1", 19, 3);
        final TextModelFragment grandchild2 = child2.createChild("5", "grandchild2", 31, 4);

        context.checking(new Expectations() {
            {
                oneOf(stateChangedCallback).callback(grandchild1);
                oneOf(stateChangedCallback).callback(grandchild2);
                oneOf(stateChangedCallback).callback(child2);
                oneOf(stateChangedCallback).callback(child1);
                oneOf(stateChangedCallback).callback(root);
            }
        });

        assertThat(root.textState(), is(TextState.Unprocessed));
        assertThat(child2.textState(), is(TextState.Unprocessed));

        grandchild1.markComplete();
        grandchild2.markComplete();

        assertThat(root.textState(), is(TextState.Unprocessed));
        assertThat(child2.textState(), is(TextState.Passed));

        child1.markComplete();
        assertThat(root.textState(), is(TextState.Passed));
    }


    @Test
    public void markAsFailedUpdatesTextStateAccordingly() {
        final TextModelFragment fragment = TextModelFragment
                .createRootFragment("1", "text", 0, 0, stateChangedCallback);

        context.checking(new Expectations() {
            {
                oneOf(stateChangedCallback).callback(fragment);
            }
        });

        assertThat(fragment.textState(), is(TextState.Unprocessed));
        fragment.markFailed();
        assertThat(fragment.textState(), is(TextState.Failed));
    }


    @Test
    public void markAsFailedUpdatesHierarchyAccordingly() {
        final TextModelFragment root = TextModelFragment.createRootFragment("1", "root", 0, 0, stateChangedCallback);
        final TextModelFragment child1 = root.createChild("2", "child1", 5, 1);
        final TextModelFragment child2 = root.createChild("3", "child2", 12, 2);
        final TextModelFragment grandchild1 = child2.createChild("4", "grandchild1", 19, 3);
        final TextModelFragment grandchild2 = child2.createChild("5", "grandchild2", 31, 4);

        context.checking(new Expectations() {
            {
                oneOf(stateChangedCallback).callback(grandchild1);
                oneOf(stateChangedCallback).callback(child2);
                oneOf(stateChangedCallback).callback(root);
            }
        });

        assertThat(root.textState(), is(TextState.Unprocessed));
        assertThat(child1.textState(), is(TextState.Unprocessed));
        assertThat(child2.textState(), is(TextState.Unprocessed));
        assertThat(grandchild1.textState(), is(TextState.Unprocessed));
        assertThat(grandchild2.textState(), is(TextState.Unprocessed));

        grandchild1.markFailed();

        assertThat(root.textState(), is(TextState.SubNodeFailed));
        assertThat(child1.textState(), is(TextState.Unprocessed));
        assertThat(child2.textState(), is(TextState.SubNodeFailed));
        assertThat(grandchild1.textState(), is(TextState.Failed));
        assertThat(grandchild2.textState(), is(TextState.Unprocessed));
    }


    @SuppressWarnings({ "unchecked", "unused" })
    @Test
    public void doToAncestorsUpdatesCurrentNodeAndAllParents() {
        final TextModelFragment root = TextModelFragment.createRootFragment("1", "root", 0, 0, stateChangedCallback);
        final TextModelFragment child1 = root.createChild("2", "child1", 5, 1);
        final TextModelFragment child2 = root.createChild("3", "child2", 12, 2);
        final TextModelFragment grandchild1 = child2.createChild("4", "grandchild1", 19, 3);
        final TextModelFragment grandchild2 = child2.createChild("5", "grandchild2", 31, 4);

        final Callback1<TextModelFragment> callback = context.mock(Callback1.class);
        final Sequence sequence = context.sequence("Sequence");
        context.checking(new Expectations() {
            {
                oneOf(callback).callback(grandchild1);
                inSequence(sequence);

                oneOf(callback).callback(child2);
                inSequence(sequence);

                oneOf(callback).callback(root);
                inSequence(sequence);
            }
        });

        grandchild1.doToHierarchy(callback);
    }
}
