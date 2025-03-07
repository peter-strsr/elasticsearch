/*
 * Copyright Elasticsearch B.V. and/or licensed to Elasticsearch B.V. under one
 * or more contributor license agreements. Licensed under the "Elastic License
 * 2.0", the "GNU Affero General Public License v3.0 only", and the "Server Side
 * Public License v 1"; you may not use this file except in compliance with, at
 * your election, the "Elastic License 2.0", the "GNU Affero General Public
 * License v3.0 only", or the "Server Side Public License, v 1".
 */

package org.elasticsearch.ingest;

import org.elasticsearch.common.lucene.uid.Versions;
import org.elasticsearch.core.Tuple;
import org.elasticsearch.index.VersionType;
import org.elasticsearch.test.ESTestCase;

import java.util.HashMap;
import java.util.Map;

/**
 * Construct ingest documents for testing purposes
 */
public class TestIngestDocument {
    public static final long DEFAULT_VERSION = 12345L;
    private static final String VERSION = IngestDocument.Metadata.VERSION.getFieldName();

    /**
     * Create an {@link IngestDocument} from the given sourceAndMetadata and ingestMetadata and a version validator that allows null
     * _versions.  Normally null _version is not allowed, but many tests don't care about that invariant.
     */
    public static IngestDocument withDefaultVersion(Map<String, Object> sourceAndMetadata, Map<String, Object> ingestMetadata) {
        if (sourceAndMetadata.containsKey(VERSION) == false) {
            sourceAndMetadata = new HashMap<>(sourceAndMetadata);
            sourceAndMetadata.put(VERSION, DEFAULT_VERSION);
        }
        return new IngestDocument(sourceAndMetadata, ingestMetadata);
    }

    /**
     * Create an {@link IngestDocument} with {@link #DEFAULT_VERSION} as the _version metadata, if _version is not already present.
     */
    public static IngestDocument withDefaultVersion(Map<String, Object> sourceAndMetadata) {
        return withDefaultVersion(sourceAndMetadata, new HashMap<>());
    }

    /**
     * Create an empty ingest document for testing.
     * <p>
     * Adds the required {@code "_version"} metadata key with value {@link #DEFAULT_VERSION}.
     */
    public static IngestDocument emptyIngestDocument() {
        Map<String, Object> sourceAndMetadata = new HashMap<>();
        sourceAndMetadata.put(VERSION, DEFAULT_VERSION);
        return new IngestDocument(sourceAndMetadata, new HashMap<>());
    }

    public static Tuple<String, Object> randomMetadata() {
        IngestDocument.Metadata metadata = ESTestCase.randomFrom(IngestDocument.Metadata.values());
        return new Tuple<>(metadata.getFieldName(), switch (metadata) {
            case VERSION, IF_SEQ_NO, IF_PRIMARY_TERM -> ESTestCase.randomIntBetween(0, 124);
            case VERSION_TYPE -> VersionType.toString(ESTestCase.randomFrom(VersionType.values()));
            case DYNAMIC_TEMPLATES -> Map.of(ESTestCase.randomAlphaOfLengthBetween(5, 10), ESTestCase.randomAlphaOfLengthBetween(5, 10));
            default -> ESTestCase.randomAlphaOfLengthBetween(5, 10);
        });
    }

    public static long randomVersion() {
        return ESTestCase.randomLongBetween(Versions.MATCH_DELETED, Long.MAX_VALUE);
    }
}
