package com.unnamednuclear.client;

import com.unnamednuclear.block.ReactorControllerBlockEntity;
import java.util.Collections;
import java.util.Set;
import java.util.WeakHashMap;

public class ClientReactorTracker {
    private static final Set<ReactorControllerBlockEntity> REACTORS = Collections.newSetFromMap(new WeakHashMap<>());

    public static void add(ReactorControllerBlockEntity be) {
        REACTORS.add(be);
    }

    public static void remove(ReactorControllerBlockEntity be) {
        REACTORS.remove(be);
    }

    public static Set<ReactorControllerBlockEntity> getReactors() {
        return REACTORS;
    }
}
