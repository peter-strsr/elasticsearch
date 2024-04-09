/*
 * Copyright Elasticsearch B.V. and/or licensed to Elasticsearch B.V. under one
 * or more contributor license agreements. Licensed under the Elastic License
 * 2.0; you may not use this file except in compliance with the Elastic License
 * 2.0.
 */

package org.elasticsearch.xpack.esql.enrich;

import org.apache.lucene.util.RamUsageEstimator;
import org.elasticsearch.common.util.ObjectArray;
import org.elasticsearch.compute.data.Block;
import org.elasticsearch.compute.data.BlockFactory;
import org.elasticsearch.compute.data.IntBlock;
import org.elasticsearch.compute.data.IntVector;
import org.elasticsearch.compute.data.Page;
import org.elasticsearch.core.Releasables;

import java.util.Arrays;

/**
 * {@link EnrichResultBuilder} for Ints.
 * This class is generated. Edit `X-EnrichResultBuilder.java.st` instead.
 */
final class EnrichResultBuilderForInt extends EnrichResultBuilder {
    private ObjectArray<int[]> cells;

    EnrichResultBuilderForInt(BlockFactory blockFactory, int channel, int totalPositions) {
        super(blockFactory, channel, totalPositions);
        this.cells = blockFactory.bigArrays().newObjectArray(totalPositions);
    }

    @Override
    void addInputPage(IntVector positions, Page page) {
        IntBlock block = page.getBlock(channel);
        for (int i = 0; i < positions.getPositionCount(); i++) {
            int valueCount = block.getValueCount(i);
            if (valueCount == 0) {
                continue;
            }
            int cellPosition = positions.getInt(i);
            final var oldCell = cells.get(cellPosition);
            final var newCell = extendCell(oldCell, valueCount);
            cells.set(cellPosition, newCell);
            int dstIndex = oldCell != null ? oldCell.length : 0;
            adjustBreaker(RamUsageEstimator.sizeOf(newCell) - (oldCell != null ? RamUsageEstimator.sizeOf(oldCell) : 0));
            int firstValueIndex = block.getFirstValueIndex(i);
            for (int v = 0; v < valueCount; v++) {
                newCell[dstIndex + v] = block.getInt(firstValueIndex + v);
            }
        }
    }

    private int[] extendCell(int[] oldCell, int newValueCount) {
        if (oldCell == null) {
            return new int[newValueCount];
        } else {
            return Arrays.copyOf(oldCell, oldCell.length + newValueCount);
        }
    }

    @Override
    Block build() {
        try (IntBlock.Builder builder = blockFactory.newIntBlockBuilder(totalPositions)) {
            for (int i = 0; i < totalPositions; i++) {
                final var cell = cells.get(i);
                if (cell == null) {
                    builder.appendNull();
                    continue;
                }
                if (cell.length > 1) {
                    builder.beginPositionEntry();
                }
                // TODO: sort and dedup
                for (var v : cell) {
                    builder.appendInt(v);
                }
                if (cell.length > 1) {
                    builder.endPositionEntry();
                }
            }
            return builder.build();
        }
    }

    @Override
    public void close() {
        Releasables.close(cells, super::close);
    }
}
