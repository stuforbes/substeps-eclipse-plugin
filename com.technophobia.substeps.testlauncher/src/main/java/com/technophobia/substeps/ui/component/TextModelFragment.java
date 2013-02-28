package com.technophobia.substeps.ui.component;

import com.technophobia.eclipse.transformer.Callback1;

public class TextModelFragment {

    public enum TextState {
        Unprocessed, //
        InProgress, //
        Passed, //
        Failed, //
        SubNodeFailed; //
    }

    private final String id;
    private final String text;

    private final int depth;

    private final int startPos;
    private final int lineNumber;
    private final TextModelFragment parentTextFragment;

    private TextState textState;

    private int numChildren;
    private int numChildrenPassed;
    private final Callback1<TextModelFragment> stateChangedCallback;


    public static TextModelFragment createRootFragment(final String id, final String text, final int startPos,
            final int lineNumber, final Callback1<TextModelFragment> stateChangedCallback) {
        return new TextModelFragment(id, text, 0, startPos, lineNumber, null, stateChangedCallback);
    }


    private TextModelFragment(final String id, final String text, final int depth, final int startPos,
            final int lineNumber, final TextModelFragment parentTextFragment,
            final Callback1<TextModelFragment> stateChangedCallback) {
        this.id = id;
        this.text = text;
        this.depth = depth;
        this.startPos = startPos;
        this.lineNumber = lineNumber;
        this.parentTextFragment = parentTextFragment;
        this.stateChangedCallback = stateChangedCallback;
        this.textState = TextState.Unprocessed;
        this.numChildren = 0;
        this.numChildrenPassed = 0;
    }


    // is start pos really needed? or length for that matter
    public TextModelFragment createChild(final String id, final String text, final int startPos, final int lineNumber) {
        numChildren++;
        return new TextModelFragment(id, text, depth + 1, startPos, lineNumber, this, stateChangedCallback);
    }


    public String id() {
        return id;
    }


    public String text() {
        return text;
    }


    public String indentedText() {
        final StringBuilder sb = new StringBuilder();
        for (int i = 0; i < depth; i++) {
            sb.append("\t");
        }
        sb.append(text);
        return sb.toString();
    }


    public int depth() {
        return depth;
    }


    public int length() {
        // for each level of depth, there's one extra char (\t)
        return text.length() + depth;
    }


    public int startPosition() {
        return startPos;
    }


    public int lineNumber() {
        return lineNumber;
    }


    public TextState textState() {
        return textState;
    }


    public void markInProgress() {
        doToHierarchy(new Callback1<TextModelFragment>() {
            @Override
            public void callback(final TextModelFragment t) {
                updateStateTo(TextState.InProgress);
            }
        });
    }


    public void markComplete() {
        updateStateTo(TextState.Passed);
        if (parentTextFragment != null) {
            parentTextFragment.incrementChildPassed();
        }
    }


    public void markFailed() {
        // only do this for leaf nodes - parents can sort themselves out from
        // that
        if (numChildren == 0) {
            updateStateTo(TextState.Failed);
            doToAncestry(new Callback1<TextModelFragment>() {
                @Override
                public void callback(final TextModelFragment t) {
                    t.updateStateTo(TextState.SubNodeFailed);
                }
            });
        }
    }


    protected void doToHierarchy(final Callback1<TextModelFragment> callback) {
        callback.callback(this);
        if (parentTextFragment != null) {
            parentTextFragment.doToHierarchy(callback);
        }
    }


    protected void doToAncestry(final Callback1<TextModelFragment> callback) {
        if (parentTextFragment != null) {
            parentTextFragment.doToHierarchy(callback);
        }
    }


    private void updateStateTo(final TextState newState) {
        this.textState = newState;
        stateChangedCallback.callback(this);
    }


    protected void incrementChildPassed() {
        numChildrenPassed++;
        if (numChildrenPassed >= numChildren) {
            markComplete();
        }
    }


    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + depth;
        result = prime * result + ((id == null) ? 0 : id.hashCode());
        result = prime * result + ((parentTextFragment == null) ? 0 : parentTextFragment.hashCode());
        result = prime * result + startPos;
        result = prime * result + ((text == null) ? 0 : text.hashCode());
        result = prime * result + ((textState == null) ? 0 : textState.hashCode());
        return result;
    }


    @Override
    public boolean equals(final Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        final TextModelFragment other = (TextModelFragment) obj;
        if (depth != other.depth)
            return false;
        if (id == null) {
            if (other.id != null)
                return false;
        } else if (!id.equals(other.id))
            return false;
        if (parentTextFragment == null) {
            if (other.parentTextFragment != null)
                return false;
        } else if (!parentTextFragment.equals(other.parentTextFragment))
            return false;
        if (startPos != other.startPos)
            return false;
        if (text == null) {
            if (other.text != null)
                return false;
        } else if (!text.equals(other.text))
            return false;
        if (textState != other.textState)
            return false;
        return true;
    }


    @Override
    public String toString() {
        return id + ": " + text + "(" + textState + ") - " + startPos + ")";
    }
}
