/*
 * Copyright (c) 2022 Thomas DeSilva.
 * Distributed under GPLv3.
 */

package io.github.trdesilva.autorecorder.ui.status;

import java.util.Set;

public interface EventConsumer
{
    void post(Event event);
    Set<EventType> getSubscriptions();
}
