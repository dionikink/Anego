package org.g29.anego2.application;

import android.graphics.drawable.Drawable;
import android.support.design.widget.FloatingActionButton;
import android.util.Log;
import android.widget.ImageButton;

/**
 * Created by Dion Ikink on 14-12-2015.
 */
public class ToggleButton {

    public static final int STATE_NOT_PRESSED = 0;
    public static final int STATE_PRESSED = 1;

    private Drawable[] icons;
    private boolean pressed;
    private ImageButton button;

    //First entry of icon array is non-pressed, second entry is pressed
    public ToggleButton(ImageButton button, Drawable[] icons) {
        this.button = button;
        this.icons = icons;
        this.pressed = false;
    }

    public boolean toggle() {
        if(!this.pressed) {
            this.pressed = true;
            this.button.setImageDrawable(icons[STATE_PRESSED]);
        } else {
            this.pressed = false;
            this.button.setImageDrawable(icons[STATE_NOT_PRESSED]);
        }

        return pressed;
    }


}