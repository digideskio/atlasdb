package com.palantir.atlasdb.keyvalue.remoting;

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.util.NavigableMap;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableSortedMap;
import com.google.common.primitives.UnsignedBytes;
import com.palantir.atlasdb.keyvalue.api.KeyValueService;
import com.palantir.atlasdb.keyvalue.impl.InMemoryKeyValueService;
import com.palantir.atlasdb.keyvalue.partition.api.PartitionMap;
import com.palantir.atlasdb.keyvalue.partition.endpoint.KeyValueEndpoint;
import com.palantir.atlasdb.keyvalue.partition.endpoint.SimpleKeyValueEndpoint;
import com.palantir.atlasdb.keyvalue.partition.map.DynamicPartitionMapImpl;
import com.palantir.atlasdb.keyvalue.partition.map.PartitionMapService;
import com.palantir.atlasdb.keyvalue.partition.map.PartitionMapServiceImpl;
import com.palantir.atlasdb.keyvalue.partition.quorum.QuorumParameters;

import io.dropwizard.testing.junit.DropwizardClientRule;

public class DynamicPartitionMapSerializeTest {

    static final KeyValueService endpointKvs = new InMemoryKeyValueService(false);
    static final PartitionMapService endpointPms = new PartitionMapServiceImpl();
    static final QuorumParameters QUORUM_PARAMETERS = new QuorumParameters(3, 2, 2);
    static final ObjectMapper mapper = RemotingKeyValueService.kvsMapper();

    @Rule public final DropwizardClientRule endpointKvsService = new DropwizardClientRule(endpointKvs);
    @Rule public final DropwizardClientRule endpointPmsService = new DropwizardClientRule(endpointPms);

    KeyValueEndpoint endpoint;
    DynamicPartitionMapImpl partitionMap;

    @Before
    public void setUp() {
        endpoint = new SimpleKeyValueEndpoint(endpointKvsService.baseUri().toString(), endpointPmsService.baseUri().toString());
        NavigableMap<byte[], KeyValueEndpoint> ring = ImmutableSortedMap
                .<byte[], KeyValueEndpoint>orderedBy(UnsignedBytes.lexicographicalComparator())
                .put(new byte[] {0}, endpoint)
                .put(new byte[] {0, 0}, endpoint)
                .put(new byte[] {0, 0, 0}, endpoint)
                .build();
        partitionMap = DynamicPartitionMapImpl.create(QUORUM_PARAMETERS, ring);
    }

    @Test
    public void testSerialize() throws JsonProcessingException {
        System.err.println(mapper.writeValueAsString(partitionMap));
    }

    @Test
    public void testDeserialize() throws IOException {
        String asString = mapper.writeValueAsString(partitionMap);
        PartitionMap deserialized = mapper.readValue(asString, DynamicPartitionMapImpl.class);
        System.err.println(deserialized);
        System.err.println(partitionMap);
        assertEquals(partitionMap, deserialized);
    }
}
