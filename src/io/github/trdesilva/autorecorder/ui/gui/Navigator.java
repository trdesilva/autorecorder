/*
 * Copyright (c) 2022 Thomas DeSilva.
 * Distributed under GPLv3.
 */

package io.github.trdesilva.autorecorder.ui.gui;

import java.io.File;

public interface Navigator
{
    void showClipView(File videoFile);
    void showUploadView(File videoFile);
    void showLicenseView();
    void showMainView();
}
