/*
 * Copyright (c) 2022 Thomas DeSilva.
 * Distributed under GPLv3.
 */

package io.github.trdesilva.autorecorder.ui.gui;

public class ReportableException extends Exception
{
    public ReportableException(String message)
    {
        super(message);
    }
    
    public ReportableException(String message, Throwable cause)
    {
        super(message, cause);
    }
}
