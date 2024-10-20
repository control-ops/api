package com.control_ops.control_system.instrument;

import java.util.HashSet;
import java.util.Set;

public record InstrumentId(String id) {

    private static final Set<String> usedIds = new HashSet<>();

    public static class IdAlreadyExistsException extends RuntimeException {
        public IdAlreadyExistsException(final String id) {
            super("Instrument IDs must be unique; an instrument with ID " + id + " already exists");
        }
    }

    public InstrumentId {
        if (usedIds.contains(id)) {
            throw new IdAlreadyExistsException(id);
        }
        usedIds.add(id);
    }

    @Override
    public String toString() {
        return id;
    }
}
