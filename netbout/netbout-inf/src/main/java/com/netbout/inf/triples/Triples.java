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
 * this code occasionally and without intent to use it, please report this
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
package com.netbout.inf.triples;

import com.netbout.spi.Message;
import java.io.Closeable;
import java.io.File;
import java.util.Iterator;

/**
 * Triples.
 *
 * @author Yegor Bugayenko (yegor@netbout.com)
 * @version $Id$
 */
public final class Triples implements Closeable {

    /**
     * Public ctor.
     * @param dir Where to keep data
     */
    public Triples(final File dir) {
        // todo
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void close() throws java.io.IOException {
        // ...
    }

    /**
     * Put new triple.
     * @param number The number
     * @param name Name of triple
     * @param value The value
     */
    public <T> void put(final Long number, final String name, final T value) {
    }

    /**
     * Get the value (the first one).
     * @param number The number
     * @param name Name of triple
     * @return The value found
     * @throws MissedTripleException If not found
     */
    public <T> T get(final Long number, final String name)
        throws MissedTripleException {
    }

    /**
     * Get all triples.
     * @param number The number
     * @param name Name of triple
     * @return The values found
     * @throws MissedTripleException If not found
     */
    public <T> Iterator<T> all(final Long number, final String name) {
    }

    /**
     * Reverse lookup.
     * @param name Name of triple
     * @param value The value to look for
     * @return Reverse sorted list of numbers found
     */
    public <T> Iterator<Long> reverse(final String name, final T value) {
    }

    /**
     * Reverse lookup, by any of the values provided.
     * @param name Name of triple
     * @param values The values to look for
     * @return Reverse sorted list of numbers found
     */
    public <T> Iterator<Long> reverse(final String name,
        final Iterator<T> values) {
    }

}
