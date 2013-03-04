package com.technophobia.substeps.ui.component;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import org.jmock.Expectations;
import org.jmock.Mockery;
import org.jmock.integration.junit4.JMock;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import com.technophobia.substeps.supplier.Predicate;
import com.technophobia.substeps.ui.TextHighlighter;

@RunWith(JMock.class)
public class TextModelFragmentFactoryTest {

    private Mockery context;

    private HierarchicalTextCollection textCollection;

    private HierarchicalTextStructureFactory factory;

    private TextHighlighter textHighlighter;


    @Before
    public void initialise() {
        this.context = new Mockery();

        this.textCollection = context.mock(HierarchicalTextCollection.class);
        this.textHighlighter = context.mock(TextHighlighter.class);

        this.factory = new TextModelFragmentFactory(textCollection, textHighlighter);
    }


    @SuppressWarnings("unchecked")
    @Test
    public void createsRootFragmentIfParentNotFound() {

        context.checking(new Expectations() {
            {
                oneOf(textCollection).findFirstOrNull(with(any(Predicate.class)));
                will(returnValue(null));
            }
        });

        final TextModelFragment fragment = (TextModelFragment) factory.createTextStructureFor(10, 2, "2", "1",
                "Some text");
        assertThat(fragment.id(), is("2"));
        assertThat(fragment.text(), is("Some text"));
        assertThat(fragment.depth(), is(0));
    }


    @SuppressWarnings("unchecked")
    @Test
    public void createsChildFragmentIfParentFound() {

        final TextModelFragment parent = TextModelFragment.createRootFragment("1", "Parent", 0, 0, textHighlighter);

        context.checking(new Expectations() {
            {
                oneOf(textCollection).findFirstOrNull(with(any(Predicate.class)));
                will(returnValue(parent));
            }
        });

        final TextModelFragment fragment = (TextModelFragment) factory.createTextStructureFor(10, 2, "2", "1",
                "Some text");
        assertThat(fragment.id(), is("2"));
        assertThat(fragment.text(), is("Some text"));
        assertThat(fragment.depth(), is(1));
    }
}
