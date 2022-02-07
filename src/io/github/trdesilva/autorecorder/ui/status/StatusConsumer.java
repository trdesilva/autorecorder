/*
 * Copyright (c) 2022 Thomas DeSilva.
 * Distributed under GPLv3.
 */

package io.github.trdesilva.autorecorder.ui.status;

public interface StatusConsumer
{
    void post(StatusMessage message) throws InterruptedException;
}
