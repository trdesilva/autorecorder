/*
 * Copyright (c) 2022 Thomas DeSilva.
 * Distributed under GPLv3.
 */

package io.github.trdesilva.autorecorder.upload;

public interface UploadJobValidator
{
    boolean validate(UploadJob job);
}
