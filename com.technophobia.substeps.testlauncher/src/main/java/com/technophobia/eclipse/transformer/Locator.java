package com.technophobia.eclipse.transformer;

public interface Locator<T, Context> {

    T one(Context c);
}
