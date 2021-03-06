/**
 * Copyright 2015 Palantir Technologies
 *
 * Licensed under the BSD-3 License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://opensource.org/licenses/BSD-3-Clause
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.palantir.atlasdb.keyvalue.dbkvs.impl;

import java.io.Closeable;

import com.palantir.nexus.db.DBType;

public interface DbTableFactory extends Closeable {
    DbMetadataTable createMetadata(String tableName, ConnectionSupplier conns);
    DbDdlTable createDdl(String tableName, ConnectionSupplier conns);
    DbReadTable createRead(String tableName, ConnectionSupplier conns);
    DbWriteTable createWrite(String tableName, ConnectionSupplier conns);
    DBType getDbType();
    @Override
    void close();
}
