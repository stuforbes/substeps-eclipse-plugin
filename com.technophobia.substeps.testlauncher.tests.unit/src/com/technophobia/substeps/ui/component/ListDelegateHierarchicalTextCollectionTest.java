package com.technophobia.substeps.ui.component;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import org.eclipse.jface.text.Position;
import org.jmock.Expectations;
import org.jmock.Mockery;
import org.jmock.integration.junit4.JMock;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import com.google.common.collect.Iterables;
import com.technophobia.substeps.supplier.Predicate;

@RunWith(JMock.class)
public class ListDelegateHierarchicalTextCollectionTest {

    private Mockery context;

    HierarchicalTextStructure text1;
    HierarchicalTextStructure text2;
    HierarchicalTextStructure text3;

    private HierarchicalTextCollection textCollection;


    @Before
    public void initialise() {
        this.context = new Mockery();

        text1 = context.mock(HierarchicalTextStructure.class, "text1");
        text2 = context.mock(HierarchicalTextStructure.class, "text2");
        text3 = context.mock(HierarchicalTextStructure.class, "text3");

        this.textCollection = new ListDelegateHierarchicalTextCollection();

        textCollection.add(text1);
        textCollection.add(text2);
        textCollection.add(text3);
    }


    @Test
    public void canAddItemsToCollection() {

        final Iterable<HierarchicalTextStructure> items = textCollection.items();

        assertThat(Iterables.size(items), is(3));
        assertTrue(Iterables.contains(items, text1));
        assertTrue(Iterables.contains(items, text2));
        assertTrue(Iterables.contains(items, text3));
    }


    @Test
    public void resettingCollectionRemovesAllItems() {
        final Iterable<HierarchicalTextStructure> items = textCollection.items();

        assertThat(Iterables.size(items), is(3));

        textCollection.reset();

        assertThat(Iterables.size(items), is(0));
    }


    @SuppressWarnings("unchecked")
    @Test
    public void canFindAnItemInCollection() {
        final Predicate<HierarchicalTextStructure> predicate = context.mock(Predicate.class);

        context.checking(new Expectations() {
            {
                oneOf(predicate).forModel(text1);
                will(returnValue(false));

                oneOf(predicate).forModel(text2);
                will(returnValue(false));

                oneOf(predicate).forModel(text3);
                will(returnValue(true));
            }
        });

        assertThat(textCollection.findFirstOrNull(predicate), is(text3));
    }


    @SuppressWarnings("unchecked")
    @Test
    public void nullReturnedIfNoItemIsMatched() {
        final Predicate<HierarchicalTextStructure> predicate = context.mock(Predicate.class);

        context.checking(new Expectations() {
            {
                oneOf(predicate).forModel(text1);
                will(returnValue(false));

                oneOf(predicate).forModel(text2);
                will(returnValue(false));

                oneOf(predicate).forModel(text3);
                will(returnValue(false));
            }
        });

        assertThat(textCollection.findFirstOrNull(predicate), is(nullValue()));
    }


    @Test
    public void findsPositionForItemWithNoChildren() {

        context.checking(new Expectations() {
            {
                oneOf(text2).depth();
                will(returnValue(1));

                oneOf(text2).offset();
                will(returnValue(10));

                oneOf(text3).depth();
                will(returnValue(1));

                oneOf(text3).offset();
                will(returnValue(20));
            }
        });

        assertThat(textCollection.positionFor(text2), is(new Position(10, 10)));
    }


    @Test
    public void findsPositionForItemWithChildrenAndNoSiblingsFollowing() {
        context.checking(new Expectations() {
            {
                oneOf(text1).depth();
                will(returnValue(2));

                oneOf(text1).offset();
                will(returnValue(10));

                oneOf(text2).depth();
                will(returnValue(3));

                oneOf(text3).depth();
                will(returnValue(1));

                oneOf(text3).offset();
                will(returnValue(40));
            }
        });

        assertThat(textCollection.positionFor(text1), is(new Position(10, 30)));
    }


    @Test
    public void findsPositionForItemWithChildrenAndSiblingsFollowing() {
        context.checking(new Expectations() {
            {
                oneOf(text1).depth();
                will(returnValue(2));

                oneOf(text1).offset();
                will(returnValue(10));

                oneOf(text2).depth();
                will(returnValue(3));

                oneOf(text3).depth();
                will(returnValue(2));

                oneOf(text3).offset();
                will(returnValue(40));
            }
        });

        assertThat(textCollection.positionFor(text1), is(new Position(10, 30)));
    }


    @Test
    public void findsPositionForItemWithChildrenAtEndOfList() {
        context.checking(new Expectations() {
            {
                oneOf(text2).depth();
                will(returnValue(2));

                oneOf(text2).offset();
                will(returnValue(20));

                oneOf(text3).depth();
                will(returnValue(3));

                oneOf(text3).offset();
                will(returnValue(50));

                oneOf(text3).length();
                will(returnValue(30));
            }
        });

        assertThat(textCollection.positionFor(text2), is(new Position(20, 60)));
    }


    @Test
    public void returnsNullPositionIfTextItemNotFound() {
        final HierarchicalTextStructure text4 = context.mock(HierarchicalTextStructure.class, "text4");

        assertThat(textCollection.positionFor(text4), is(nullValue()));
    }
}
