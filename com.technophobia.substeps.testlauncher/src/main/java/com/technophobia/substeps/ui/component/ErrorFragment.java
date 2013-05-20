package com.technophobia.substeps.ui.component;

public class ErrorFragment {

    private final String trace;


    public ErrorFragment(final String trace) {
        this.trace = trace;
    }


    public String getTrace() {
        return trace;
    }


    public String formattedMessage() {
        return trace;
    }


    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((trace == null) ? 0 : trace.hashCode());
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
        final ErrorFragment other = (ErrorFragment) obj;
        if (trace == null) {
            if (other.trace != null)
                return false;
        } else if (!trace.equals(other.trace))
            return false;
        return true;
    }


    @Override
    public String toString() {
        return "Error, trace is " + trace;
    }
}
