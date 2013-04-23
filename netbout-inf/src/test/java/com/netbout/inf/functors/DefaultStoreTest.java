/**
 * Copyright (c) 2009-2012, Netbout.com
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are PROHIBITED without prior written permission from
 * the author. This product may NOT be used anywhere and on any computer
 * except the server platform of netBout Inc. located at www.netbout.com.
 * Federal copyright law prohibits unauthorized reproduction by any means
 * and imposes fines up to $25,000 for violation. If you received
 * this code accidentally and without intent to use it, please report this
 * incident to the author by email.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
 * A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT
 * OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 * ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT
 * OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
 * SUCH DAMAGE.
 */
package com.netbout.inf.functors;

import com.netbout.inf.Store;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.Test;

/**
 * Test case of {@link DefaultStore}.
 * @author Yegor Bugayenko (yegor@netbout.com)
 * @version $Id$
 * @checkstyle ClassDataAbstractionCoupling (500 lines)
 */
@SuppressWarnings("PMD.AvoidInstantiatingObjectsInLoops")
public final class DefaultStoreTest {

    /**
     * DefaultStore can get key functors.
     * @throws Exception If there is some problem inside
     */
    @Test
    public void findsKeyFunctors() throws Exception {
        final String[] names = new String[] {
            "and",
            "bundled",
            "equal",
            "from",
            "limit",
            "matches",
            "not",
            "ns",
            "or",
            "pos",
            "talks-with",
            "unbundled",
            "unique",
        };
        final Store store = new DefaultStore();
        for (String name : names) {
            MatcherAssert.assertThat(store.get(name), Matchers.notNullValue());
        }
    }

    /**
     * DefaultStore can convert itselt to string.
     * @throws Exception If there is some problem inside
     */
    @Test
    public void convertsItseltToString() throws Exception {
        MatcherAssert.assertThat(
            new DefaultStore(),
            Matchers.hasToString(Matchers.notNullValue())
        );
    }

}