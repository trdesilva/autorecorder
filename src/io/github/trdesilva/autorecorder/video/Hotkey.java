/*
 * Copyright (c) 2022 Thomas DeSilva.
 * Distributed under GPLv3.
 */

package io.github.trdesilva.autorecorder.video;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.github.kwhat.jnativehook.keyboard.NativeKeyEvent;

import java.awt.event.KeyEvent;
import java.util.Objects;

public class Hotkey
{
    public static final int UNSET = -1;
    
    private boolean shiftHeld;
    private boolean ctrlHeld;
    private boolean altHeld;
    private int keyCode;
    
    @JsonIgnore
    private int modifierMask;
    
    public Hotkey()
    {
        shiftHeld = false;
        ctrlHeld = false;
        altHeld = false;
        keyCode = UNSET;
        
        modifierMask = 0;
    }
    
    // this uses KeyEvent because we set the binding with a Swing KeyListener in SettingsPanel
    public Hotkey(KeyEvent event)
    {
        this(event.isShiftDown(), event.isControlDown(), event.isAltDown(), event.getKeyCode());
    }
    
    public Hotkey(boolean shiftHeld, boolean ctrlHeld, boolean altHeld, int keyCode)
    {
        this.shiftHeld = shiftHeld;
        this.ctrlHeld = ctrlHeld;
        this.altHeld = altHeld;
        this.keyCode = keyCode;
        
        recalculateMask();
    }
    
    public boolean isShiftHeld()
    {
        return shiftHeld;
    }
    
    public void setShiftHeld(boolean shiftHeld)
    {
        this.shiftHeld = shiftHeld;
        recalculateMask();
    }
    
    public boolean isCtrlHeld()
    {
        return ctrlHeld;
    }
    
    public void setCtrlHeld(boolean ctrlHeld)
    {
        this.ctrlHeld = ctrlHeld;
        recalculateMask();
    }
    
    public boolean isAltHeld()
    {
        return altHeld;
    }
    
    public void setAltHeld(boolean altHeld)
    {
        this.altHeld = altHeld;
        recalculateMask();
    }
    
    public int getKeyCode()
    {
        return keyCode;
    }
    
    public void setKeyCode(int keyCode)
    {
        this.keyCode = keyCode;
    }
    
    // this one uses NativeKeyEvent because BookmarkListener does
    public boolean eventMatches(NativeKeyEvent event)
    {
        if(keyCode == UNSET || keyCode != event.getRawCode())
        {
            return false;
        }

        return (event.getModifiers() & modifierMask) == modifierMask;
    }
    
    private void recalculateMask()
    {
        int mask = 0x0;
        if(ctrlHeld)
        {
            mask |= NativeKeyEvent.CTRL_MASK;
        }
        if(shiftHeld)
        {
            mask |= NativeKeyEvent.SHIFT_MASK;
        }
        if(altHeld)
        {
            mask |= NativeKeyEvent.ALT_MASK;
        }
        this.modifierMask = mask;
    }
    
    @Override
    public String toString()
    {
        if(keyCode == UNSET)
        {
            return "UNSET";
        }
        StringBuilder builder = new StringBuilder();
        if(ctrlHeld)
        {
            builder.append("CTRL+");
        }
        if(shiftHeld)
        {
            builder.append("SHIFT+");
        }
        if(altHeld)
        {
            builder.append("ALT+");
        }
        builder.append(KeyEvent.getKeyText(keyCode));
        return builder.toString();
    }
    
    @Override
    public boolean equals(Object o)
    {
        if(this == o)
        {
            return true;
        }
        if(o == null || getClass() != o.getClass())
        {
            return false;
        }
        Hotkey hotkey = (Hotkey) o;
        return isShiftHeld() == hotkey.isShiftHeld() && isCtrlHeld() == hotkey.isCtrlHeld() && isAltHeld() == hotkey.isAltHeld() && getKeyCode() == hotkey.getKeyCode();
    }
    
    @Override
    public int hashCode()
    {
        return Objects.hash(isShiftHeld(), isCtrlHeld(), isAltHeld(), getKeyCode());
    }
}
