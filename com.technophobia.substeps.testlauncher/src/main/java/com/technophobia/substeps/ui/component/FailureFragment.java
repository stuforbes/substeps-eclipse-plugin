package com.technophobia.substeps.ui.component;

public class FailureFragment {

    private final String expected;
    private final String actual;


    public FailureFragment(final String expected, final String actual) {
        this.expected = expected;
        this.actual = actual;
    }


    public String getExpected() {
        return expected;
    }


    public String getActual() {
        return actual;
    }


    public String formattedMessage() {
        final StringBuilder sb = new StringBuilder();
        sb.append("Expected:\n\t");
        sb.append(expected);
        sb.append("\n\nActual:\n\t");
        sb.append(actual);
        return sb.toString();
    }


    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((actual == null) ? 0 : actual.hashCode());
        result = prime * result + ((expected == null) ? 0 : expected.hashCode());
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
        final FailureFragment other = (FailureFragment) obj;
        if (actual == null) {
            if (other.actual != null)
                return false;
        } else if (!actual.equals(other.actual))
            return false;
        if (expected == null) {
            if (other.expected != null)
                return false;
        } else if (!expected.equals(other.expected))
            return false;
        return true;
    }


    @Override
    public String toString() {
        return "Failure, expected: " + expected + ", but was " + actual;
    }
}
