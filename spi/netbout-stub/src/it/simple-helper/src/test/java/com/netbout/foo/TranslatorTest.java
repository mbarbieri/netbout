/**
 * Copyright (c) 2009-2011, NetBout.com
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met: 1) Redistributions of source code must retain the above
 * copyright notice, this list of conditions and the following
 * disclaimer. 2) Redistributions in binary form must reproduce the above
 * copyright notice, this list of conditions and the following
 * disclaimer in the documentation and/or other materials provided
 * with the distribution. 3) Neither the name of the NetBout.com nor
 * the names of its contributors may be used to endorse or promote
 * products derived from this software without specific prior written
 * permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT
 * NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
 * FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL
 * THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT,
 * INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
 * HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT,
 * STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED
 * OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package com.netbout.foo;

import com.netbout.spi.Bout;
import com.netbout.spi.Entry;
import com.netbout.spi.Identity;
import com.netbout.spi.User;
import com.netbout.spi.cpa.CpaHelper;
import com.netbout.spi.stub.InMemoryEntry;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.Before;
import org.junit.Test;

/**
 * Test case for {@link Translator}.
 * @author Yegor Bugayenko (yegor@netbout.com)
 * @version $Id$
 */
public final class TranslatorTest {

    /**
     * How this helper is called in netbout.
     */
    private static final String HELPER_IDENTITY = "Translator";

    /**
     * Netbout test harness.
     */
    private final Entry entry = new InMemoryEntry();

    /**
     * Register a helper.
     * @throws Exception If there is some problem inside
     */
    @Before
    public void registerHelper() throws Exception {
        final String name = "Owner Of The Helper";
        final User user = this.entry.user(name);
        user.identity(this.HELPER_IDENTITY).promote(
            new CpaHelper(this.getClass().getPackage().getName())
        );
    }

    /**
     * Test full cycle of translation.
     * @throws Exception If there is some problem inside
     */
    @Test
    public void testMessageTranslation() throws Exception {
        final String name = "John Doe";
        final User user = this.entry.user(name);
        final Identity identity = user.identity(name);
        final Bout bout = identity.start();
        bout.rename("let's talk about...");
        bout.invite(this.entry.identity(this.HELPER_IDENTITY));
        bout.post("Hello, how are you?");
        MatcherAssert.assertThat(
            bout.messages("").get(0).text(),
            Matchers.equalTo("Bonjour, how are you?")
        );
    }

}