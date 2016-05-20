/**
 * Copyright 2016 Palantir Technologies
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
package com.palantir.timestamp;

import static java.util.concurrent.TimeUnit.MINUTES;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.core.Is.is;
import static org.mockito.Matchers.longThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

public class AvailableTimestampsTest {

    public static final long UPPER_LIMIT = 1000 * 1000;
    public static final long LAST_RETURNED =  UPPER_LIMIT - 1000;

    @Rule
    public ExpectedException exception = ExpectedException.none();

    private final LastReturnedTimestamp lastReturnedTimestamp = new LastReturnedTimestamp(LAST_RETURNED);
    private final PersistentUpperLimit persistentUpperLimit = upperLimitOf(UPPER_LIMIT);

    private final AvailableTimestamps availableTimestamps = new AvailableTimestamps(
            lastReturnedTimestamp,
            persistentUpperLimit
    );

    @Test public void
    shouldRefreshTheBufferIfHalfOfItIsUsedUp() {
        availableTimestamps.handOut(UPPER_LIMIT - 10);
        availableTimestamps.refreshBuffer();

        verify(persistentUpperLimit).increaseToAtLeast(
                UPPER_LIMIT - 10 + AvailableTimestamps.ALLOCATION_BUFFER_SIZE
        );
    }

    @Test public void
    shouldRefreshTheBufferIfNoIncreaseHasHappenedWithin1Minute() {
        when(persistentUpperLimit.hasIncreasedWithin(1, MINUTES)).thenReturn(false);

        availableTimestamps.refreshBuffer();

        verify(persistentUpperLimit).increaseToAtLeast(
                longThat(is(greaterThan(UPPER_LIMIT)))
        );
    }

    @Test public void
    shouldHandOutTheCorrectRange() {
        final TimestampRange timestampRange = availableTimestamps.handOut(LAST_RETURNED + 10);
        assertThat(timestampRange.getLowerBound(), is(LAST_RETURNED + 1));
        assertThat(timestampRange.getUpperBound(), is(LAST_RETURNED + 10));
    }

    @Test public void
    shouldIncreaseTheMaximumToHandOutNewTimestamps() {
        assertThat(availableTimestamps.handOut(UPPER_LIMIT + 10).getUpperBound(), is(UPPER_LIMIT + 10));

        verify(persistentUpperLimit).increaseToAtLeast(UPPER_LIMIT + 10);
    }

    @Test public void
    shouldNotHandOutMoreThanTenThousandTimestampsAtATime() {
        exception.expect(IllegalArgumentException.class);
        exception.expectMessage("Can only hand out 10000 timestamps at a time");
        exception.expectMessage("1050000");

        availableTimestamps.handOut(1050000);
    }

    @Test public void
    shouldNotHandOutATimestampEarlierThanTheLastHandedOutTimestamp() {
        exception.expect(IllegalArgumentException.class);
        exception.expectMessage("Could not hand out timestamp '1'");
        exception.expectMessage("earlier than the last handed out timestamp: " + LAST_RETURNED);

        availableTimestamps.handOut(1);
    }

    @Test public void
    canFastForwardToANewMinimumTimestamp() {
        long newMinimum = 2 * UPPER_LIMIT;
        availableTimestamps.fastForwardTo(newMinimum);

        assertThat(lastReturnedTimestamp.get(), is(newMinimum));
        verify(persistentUpperLimit).increaseToAtLeast(longGreaterThan(newMinimum));
    }

    private long longGreaterThan(long n) {
        return longThat(is(greaterThan(n)));
    }

    private PersistentUpperLimit upperLimitOf(long timestamp) {
        PersistentUpperLimit upperLimit = mock(PersistentUpperLimit.class);
        when(upperLimit.get()).thenReturn(timestamp);
        return upperLimit;
    }
}