package com.technophobia.substeps.ui.component;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.eclipse.jface.text.Position;
import org.jmock.Expectations;
import org.jmock.Mockery;
import org.jmock.integration.junit4.JMock;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import com.technophobia.substeps.supplier.Predicate;
import com.technophobia.substeps.ui.TextHighlighter;
import com.technophobia.substeps.ui.session.SubstepsTestExecutionReporter;

@RunWith(JMock.class)
public class StyledDocumentSubstepsTextExecutionReporterTest {

    private final Mockery context = new Mockery();

    private TextHighlighter textHighlighter;
    private HierarchicalTextCollection textCollection;

    private SubstepsTestExecutionReporter executionReporter;

    private HierarchicalTextStructureFactory textFactory;


    @Before
    public void initialise() {
        // this.context = new Mockery();

        this.textHighlighter = context.mock(TextHighlighter.class);
        this.textCollection = context.mock(HierarchicalTextCollection.class);
        this.textFactory = context.mock(HierarchicalTextStructureFactory.class);

        this.executionReporter = new StyledDocumentSubstepsTextExecutionReporter(textCollection, textFactory,
                textHighlighter);
    }


    @Test
    public void noExecutionNodesProducesEmptyTextModelString() {

        final String text = "";
        final List<Position> positions = new ArrayList<Position>();

        context.checking(new Expectations() {
            {
                oneOf(textHighlighter).documentChanged(text, Collections.<HierarchicalTextStructure> emptyList(),
                        positions);

                oneOf(textCollection).items();
                will(returnValue(Collections.emptyList()));
            }
        });

        executionReporter.allExecutionNodesAdded();
    }


    @Test
    public void singleExecutionNodeProducesCorrectTextModelString() {

        final String text = "Feature: This is a feature";

        final HierarchicalTextStructure textStructure = TextModelFragment.createRootFragment("0", text, 0, 20, null);
        final List<HierarchicalTextStructure> textStructures = Arrays.asList(textStructure);

        final Position position = new Position(0, 19);
        final List<Position> positions = Collections.singletonList(position);

        context.checking(new Expectations() {
            {
                oneOf(textHighlighter).documentChanged(text, textStructures, positions);

                oneOf(textFactory).createTextStructureFor(0, 0, "1", null, text);
                will(returnValue(textStructure));

                oneOf(textCollection).add(textStructure);

                oneOf(textCollection).items();
                will(returnValue(textStructures));

                oneOf(textCollection).positionFor(textStructure);
                will(returnValue(position));
            }
        });

        executionReporter.addExecutionNode("1", null, "Feature: This is a feature");
        executionReporter.allExecutionNodesAdded();
    }


    @SuppressWarnings("unchecked")
    @Test
    public void multipleExecutionNodesProduceCorrectTextModelString() {

        final String feature = "Feature: This is a feature";
        final String scenario = "Scenario: A scenario";
        final String given = "Given something";
        final String then = "Then something else";

        final String text = feature + "\n\t" + scenario + "\n\t\t" + given + "\n\t\t" + then;

        final TextModelFragment featureStructure = TextModelFragment.createRootFragment("0", feature, 0, 0, null);
        final TextModelFragment scenarioStructure = featureStructure.createChild("1", scenario, 27, 1);
        final TextModelFragment givenStructure = scenarioStructure.createChild("2", given, 49, 2);
        final TextModelFragment thenStructure = scenarioStructure.createChild("3", then, 67, 3);

        final List<? extends HierarchicalTextStructure> textStructures = Arrays.asList(featureStructure,
                scenarioStructure, givenStructure, thenStructure);

        final Position featurePosition = new Position(0, 88);
        final Position scenarioPosition = new Position(27, 61);
        final Position givenPosition = new Position(49, 17);
        final Position thenPosition = new Position(67, 21);

        final List<Position> positions = Arrays.asList(featurePosition, scenarioPosition, givenPosition, thenPosition);

        context.checking(new Expectations() {
            {
                oneOf(textFactory).createTextStructureFor(0, 0, "0", null, feature);
                will(returnValue(featureStructure));

                oneOf(textFactory).createTextStructureFor(27, 1, "1", "0", scenario);
                will(returnValue(scenarioStructure));

                oneOf(textFactory).createTextStructureFor(49, 2, "2", "1", given);
                will(returnValue(givenStructure));

                oneOf(textFactory).createTextStructureFor(67, 3, "3", "2", then);
                will(returnValue(thenStructure));

                oneOf(textCollection).add(featureStructure);
                oneOf(textCollection).add(scenarioStructure);
                oneOf(textCollection).add(givenStructure);
                oneOf(textCollection).add(thenStructure);

                oneOf(textCollection).items();
                will(returnValue(Arrays.asList(featureStructure, scenarioStructure, givenStructure, thenStructure)));

                oneOf(textCollection).positionFor(featureStructure);
                will(returnValue(featurePosition));
                oneOf(textCollection).positionFor(scenarioStructure);
                will(returnValue(scenarioPosition));
                oneOf(textCollection).positionFor(givenStructure);
                will(returnValue(givenPosition));
                oneOf(textCollection).positionFor(thenStructure);
                will(returnValue(thenPosition));

                oneOf(textHighlighter).documentChanged(text, (Iterable<HierarchicalTextStructure>) textStructures,
                        positions);
            }
        });

        executionReporter.addExecutionNode("0", null, "Feature: This is a feature");
        executionReporter.addExecutionNode("1", "0", "Scenario: A scenario");
        executionReporter.addExecutionNode("2", "1", "Given something");
        executionReporter.addExecutionNode("3", "2", "Then something else");
        executionReporter.allExecutionNodesAdded();
    }


    @SuppressWarnings("unchecked")
    @Test
    public void nestedExecutionNodesProduceCorrectTextModelString() {

        final String feature = "Feature: This is a feature";
        final String scenario = "Scenario: A scenario";
        final String given = "Given something";
        final String givenSubstep = "Given some substep";
        final String then = "Then something else";

        final String text = feature + "\n\t" + scenario + "\n\t\t" + given + "\n\t\t\t" + givenSubstep + "\n\t\t"
                + then;

        final TextModelFragment featureStructure = TextModelFragment.createRootFragment("0", feature, 0, 0, null);
        final TextModelFragment scenarioStructure = featureStructure.createChild("1", scenario, 27, 1);
        final TextModelFragment givenStructure = scenarioStructure.createChild("2", given, 49, 2);
        final TextModelFragment givenSubstepStructure = givenStructure.createChild("3", givenSubstep, 66, 3);
        final TextModelFragment thenStructure = scenarioStructure.createChild("4", then, 89, 4);

        final List<? extends HierarchicalTextStructure> textStructures = Arrays.asList(featureStructure,
                scenarioStructure, givenStructure, givenSubstepStructure, thenStructure);

        final Position featurePosition = new Position(0, 88);
        final Position scenarioPosition = new Position(27, 61);
        final Position givenPosition = new Position(49, 38);
        final Position givenSubstepPosition = new Position(66, 17);
        final Position thenPosition = new Position(89, 21);

        final List<Position> positions = Arrays.asList(featurePosition, scenarioPosition, givenPosition,
                givenSubstepPosition, thenPosition);

        context.checking(new Expectations() {
            {
                oneOf(textFactory).createTextStructureFor(0, 0, "0", null, feature);
                will(returnValue(featureStructure));

                oneOf(textFactory).createTextStructureFor(27, 1, "1", "0", scenario);
                will(returnValue(scenarioStructure));

                oneOf(textFactory).createTextStructureFor(49, 2, "2", "1", given);
                will(returnValue(givenStructure));

                oneOf(textFactory).createTextStructureFor(67, 3, "3", "2", givenSubstep);
                will(returnValue(givenSubstepStructure));

                oneOf(textFactory).createTextStructureFor(89, 4, "4", "1", then);
                will(returnValue(thenStructure));

                oneOf(textCollection).add(featureStructure);
                oneOf(textCollection).add(scenarioStructure);
                oneOf(textCollection).add(givenStructure);
                oneOf(textCollection).add(givenSubstepStructure);
                oneOf(textCollection).add(thenStructure);

                oneOf(textCollection).items();
                will(returnValue(Arrays.asList(featureStructure, scenarioStructure, givenStructure,
                        givenSubstepStructure, thenStructure)));

                oneOf(textCollection).positionFor(featureStructure);
                will(returnValue(featurePosition));
                oneOf(textCollection).positionFor(scenarioStructure);
                will(returnValue(scenarioPosition));
                oneOf(textCollection).positionFor(givenStructure);
                will(returnValue(givenPosition));
                oneOf(textCollection).positionFor(givenSubstepStructure);
                will(returnValue(givenSubstepPosition));
                oneOf(textCollection).positionFor(thenStructure);
                will(returnValue(thenPosition));

                oneOf(textHighlighter).documentChanged(text, (Iterable<HierarchicalTextStructure>) textStructures,
                        positions);
            }
        });

        executionReporter.addExecutionNode("0", null, "Feature: This is a feature");
        executionReporter.addExecutionNode("1", "0", "Scenario: A scenario");
        executionReporter.addExecutionNode("2", "1", "Given something");
        executionReporter.addExecutionNode("3", "2", "Given some substep");
        executionReporter.addExecutionNode("4", "1", "Then something else");
        executionReporter.allExecutionNodesAdded();
    }


    @SuppressWarnings("unchecked")
    @Test
    public void resetExecutionStateRemovesAllTextFragments() {

        final String feature = "Feature: This is a feature";
        final String scenario = "Scenario: A scenario";
        final String given = "Given something";
        final String then = "Then something else";

        final TextModelFragment featureStructure = TextModelFragment.createRootFragment("0", feature, 0, 0, null);
        final TextModelFragment scenarioStructure = featureStructure.createChild("1", scenario, 27, 1);
        final TextModelFragment givenStructure = scenarioStructure.createChild("2", given, 49, 2);
        final TextModelFragment thenStructure = scenarioStructure.createChild("3", then, 67, 3);

        final List<? extends HierarchicalTextStructure> textStructures = Collections.emptyList();

        final String text = "";

        final List<Position> positions = new ArrayList<Position>();

        context.checking(new Expectations() {
            {
                oneOf(textFactory).createTextStructureFor(0, 0, "0", null, feature);
                will(returnValue(featureStructure));

                oneOf(textFactory).createTextStructureFor(27, 1, "1", "0", scenario);
                will(returnValue(scenarioStructure));

                oneOf(textFactory).createTextStructureFor(49, 2, "2", "1", given);
                will(returnValue(givenStructure));

                oneOf(textFactory).createTextStructureFor(67, 3, "3", "2", then);
                will(returnValue(thenStructure));

                oneOf(textCollection).add(featureStructure);
                oneOf(textCollection).add(scenarioStructure);
                oneOf(textCollection).add(givenStructure);
                oneOf(textCollection).add(thenStructure);

                oneOf(textCollection).reset();

                oneOf(textCollection).items();
                will(returnValue(textStructures));

                oneOf(textHighlighter).documentChanged(text, (Iterable<HierarchicalTextStructure>) textStructures,
                        positions);
            }
        });

        executionReporter.addExecutionNode("0", null, feature);
        executionReporter.addExecutionNode("1", "0", scenario);
        executionReporter.addExecutionNode("2", "1", given);
        executionReporter.addExecutionNode("3", "2", then);
        executionReporter.resetExecutionState();
        executionReporter.allExecutionNodesAdded();
    }


    @SuppressWarnings("unchecked")
    @Test
    public void executingNodeUpdatesCorrectHighlight() {

        final String feature = "Feature: This is a feature";
        final String scenario = "Scenario: A scenario";
        final String given = "Given something";
        final String then = "Then something else";

        final TextModelFragment featureStructure = TextModelFragment.createRootFragment("0", feature, 0, 0,
                textHighlighter);
        final TextModelFragment scenarioStructure = featureStructure.createChild("1", scenario, 27, 1);
        final TextModelFragment givenStructure = scenarioStructure.createChild("2", given, 49, 2);
        final TextModelFragment thenStructure = scenarioStructure.createChild("3", then, 67, 3);

        context.checking(new Expectations() {
            {
                oneOf(textFactory).createTextStructureFor(0, 0, "0", null, feature);
                will(returnValue(featureStructure));

                oneOf(textFactory).createTextStructureFor(27, 1, "1", "0", scenario);
                will(returnValue(scenarioStructure));

                oneOf(textFactory).createTextStructureFor(49, 2, "2", "1", given);
                will(returnValue(givenStructure));

                oneOf(textFactory).createTextStructureFor(67, 3, "3", "2", then);
                will(returnValue(thenStructure));

                oneOf(textCollection).add(featureStructure);
                oneOf(textCollection).add(scenarioStructure);
                oneOf(textCollection).add(givenStructure);
                oneOf(textCollection).add(thenStructure);

                oneOf(textCollection).findFirstOrNull(with(any(Predicate.class)));
                will(returnValue(thenStructure));

                oneOf(textHighlighter).highlight(thenStructure);
                oneOf(textHighlighter).highlight(scenarioStructure);
                oneOf(textHighlighter).highlight(featureStructure);
            }
        });

        executionReporter.addExecutionNode("0", null, feature);
        executionReporter.addExecutionNode("1", "0", scenario);
        executionReporter.addExecutionNode("2", "1", given);
        executionReporter.addExecutionNode("3", "2", then);
        executionReporter.executingNode("3");
    }
}