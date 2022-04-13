/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.ververica.cdc.connectors.tidb.table;

import org.apache.flink.table.data.GenericRowData;
import org.apache.flink.table.data.RowData;
import org.apache.flink.table.data.utils.JoinedRowData;
import org.apache.flink.util.Collector;

import java.io.Serializable;

/** Emits a row with physical fields and metadata fields. */
public class TiKVAppendMetadataCollector implements Collector<RowData>, Serializable {

    private static final long serialVersionUID = 1L;

    private final TiKVMetadataConverter[] metadataConverters;

    public transient TiKVMetadataConverter.TiKVRowValue row;
    public transient Collector<RowData> outputCollector;

    public TiKVAppendMetadataCollector(TiKVMetadataConverter[] metadataConverters) {
        this.metadataConverters = metadataConverters;
    }

    @Override
    public void collect(RowData physicalRow) {
        GenericRowData metaRow = new GenericRowData(metadataConverters.length);
        for (int i = 0; i < metadataConverters.length; i++) {
            Object meta = metadataConverters[i].read(row);
            metaRow.setField(i, meta);
        }
        RowData outRow = new JoinedRowData(physicalRow.getRowKind(), physicalRow, metaRow);
        outputCollector.collect(outRow);
    }

    @Override
    public void close() {
        // nothing to do
    }
}
