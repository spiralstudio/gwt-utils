//
// $Id$

package com.threerings.gwt.ui;

import com.google.gwt.user.client.ui.HTML;

import com.threerings.gwt.util.Value;

/**
 * Displays a dynamically changing value. Automatically handles regenerating its contents when the
 * value changes and registers and clears its listenership when it is added to and removed from the
 * DOM.
 */
public class ValueHTML<T> extends HTML
    implements Value.Listener<T>
{
    /**
     * Creates a value HTML with the supplied value. When this widget is added to the DOM, its HTML
     * will be generated by a call to {@link #getHTML} with the value's current value. It will also
     * add itself as a listener at that point and update its text whenever the value changes.
     */
    public ValueHTML (Value<T> value)
    {
        _value = value;
    }

    // from Value.Listener<T>
    public void valueChanged (T value)
    {
        setHTML(getHTML(value));
    }

    @Override // from Widget
    public void onLoad ()
    {
        super.onLoad();
        _value.addListener(this);
        valueChanged(_value.get());
    }

    @Override // from Widget
    public void onUnload ()
    {
        super.onLoad();
        _value.removeListener(this);
    }

    /**
     * Called to generate our text when the value changes. The default implementation simply
     * converts the value to a string via {@link String#valueOf}.
     */
    protected String getHTML (T value)
    {
        return String.valueOf(value);
    }

    /** The value we're displaying. */
    protected Value<T> _value;
}
