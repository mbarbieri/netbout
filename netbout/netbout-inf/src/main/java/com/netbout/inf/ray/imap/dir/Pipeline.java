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
package com.netbout.inf.ray.imap.dir;

import com.jcabi.log.Logger;
import com.netbout.inf.Attribute;
import com.netbout.inf.ray.imap.Numbers;
import java.io.Closeable;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.nio.channels.Channels;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;

/**
 * Pipeline used by {@link Draft} during baselining.
 *
 * <p>Class is thread-safe.
 *
 * @author Yegor Bugayenko (yegor@netbout.com)
 * @version $Id$
 * @checkstyle ClassDataAbstractionCoupling (500 lines)
 */
@SuppressWarnings("PMD.TooManyMethods")
final class Pipeline implements Closeable, Iterator<Catalog.Item> {

    /**
     * Token.
     */
    interface Token {
        /**
         * Get its value.
         * @return The value
         */
        String value();
        /**
         * Convert it to item.
         * @return The item
         * @throws IOException If some IO problem inside
         */
        Catalog.Item item() throws IOException;
    }

    /**
     * Comparable token.
     */
    interface ComparableToken
        extends Pipeline.Token, Comparable<Pipeline.Token> {
    }

    /**
     * Source draft.
     */
    private final transient Draft draft;

    /**
     * Source catalog, from Baseline.
     */
    private final transient Catalog catalog;

    /**
     * The attribute we're working with.
     */
    private final transient Attribute attribute;

    /**
     * Pre-existing data file, with data (we keep it open until the pipeline
     * is closed).
     */
    private final transient RandomAccessFile data;

    /**
     * Output stream, with new data (we keep it open until the pipeline
     * is closed).
     */
    private final transient DataOutputStream output;

    /**
     * Current position in output stream.
     */
    private final transient AtomicLong opos = new AtomicLong();

    /**
     * Value retrieved in previous calls to {@link #next()} (to consume
     * and filter out duplicated values).
     */
    private final transient Buffer buffer;

    /**
     * Public ctor.
     * @param drft Draft to use for data
     * @param dest Destination
     * @param src Source
     * @param attr Attribute
     * @throws IOException If some IO problem inside
     * @checkstyle ParameterNumber (4 lines)
     */
    public Pipeline(final Draft drft, final Baseline dest, final Baseline src,
        final Attribute attr) throws IOException {
        this.attribute = attr;
        this.catalog = src.catalog(this.attribute);
        this.draft = drft;
        this.buffer = new Buffer(
            this.reordered(
                this.draft.backlog(this.attribute).iterator()
            ),
            this.catalog.iterator()
        );
        this.data = new RandomAccessFile(src.data(this.attribute), "r");
        this.output = new DataOutputStream(
            new FileOutputStream(dest.data(this.attribute))
        );
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void close() throws IOException {
        this.data.close();
        this.output.close();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean hasNext() {
        return this.buffer.hasNext();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Catalog.Item next() {
        synchronized (this.buffer) {
            try {
                return this.buffer.next().item();
            } catch (java.io.IOException ex) {
                throw new IllegalStateException(ex);
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void remove() {
        throw new UnsupportedOperationException("#remove");
    }

    /**
     * Create reordered iterator.
     * @param origin The iterator to read from
     * @return Reordered iterator
     */
    private Iterator<Backlog.Item> reordered(
        final Iterator<Backlog.Item> origin) {
        final List<Backlog.Item> items = new LinkedList<Backlog.Item>();
        final Comparator<Backlog.Item> comp = new Comparator<Backlog.Item>() {
            public int compare(final Backlog.Item left,
                final Backlog.Item right) {
                return left.value().compareTo(right.value());
            }
        };
        while (origin.hasNext()) {
            final Backlog.Item item = origin.next();
            final int idx = Collections.binarySearch(items, item, comp);
            if (idx > 0) {
                items.remove(idx);
            }
            items.add(item);
        }
        Collections.sort(items);
        return items.iterator();
    }

    /**
     * Buffer of tokens, an iterator actually.
     *
     * <p>The class is NOT thread-safe.
     */
    private final class Buffer {
        /**
         * Backlog with data, from the source Draft.
         */
        private final transient Iterator<Backlog.Item> biterator;
        /**
         * Pre-existing catalog with data, from the source Baseline.
         */
        private final transient Iterator<Catalog.Item> citerator;
        /**
         * Information about the item already retrieved from iterators.
         */
        private final transient AtomicReference<Pipeline.Token> token =
            new AtomicReference<Pipeline.Token>();
        /**
         * Value retrieved in previous calls to {@link #next()} (to consume
         * and filter out duplicated values).
         */
        private final transient ConcurrentMap<String, Pipeline.Token> tokens =
            new ConcurrentHashMap<String, Pipeline.Token>();
        /**
         * One token ahead of buffer.
         */
        private final transient AtomicReference<Pipeline.Token> ahead =
            new AtomicReference<Pipeline.Token>();
        /**
         * Public ctor.
         * @param backlog Backlog iterator
         * @param ctlg Catalog iterator
         */
        public Buffer(final Iterator<Backlog.Item> backlog,
            final Iterator<Catalog.Item> ctlg) {
            this.biterator = backlog;
            this.citerator = ctlg;
        }
        /**
         * Is it empty?
         * @return TRUE if the buffer is empty
         */
        public boolean hasNext() {
            return !this.isEmpty() || !this.tokens.isEmpty()
                || this.ahead.get() != null;
        }
        /**
         * Fetch next token.
         * @return The token fetched (through buffer)
         * @throws IOException If some IO problem inside
         */
        public Pipeline.Token next() throws IOException {
            if (!this.consume()) {
                throw new NoSuchElementException();
            }
            final String value = this.tokens.keySet().iterator().next();
            final Pipeline.Token fetched = this.tokens.get(value);
            this.tokens.remove(value);
            return fetched;
        }
        /**
         * Consume as many tokens as possible from input.
         * @return TRUE if something is ready in the buffer
         * @throws IOException If some IO problem inside
         */
        private boolean consume() throws IOException {
            if (this.tokens.isEmpty() && this.ahead.get() != null) {
                this.shift();
            }
            if (this.ahead.get() == null) {
                while (!this.isEmpty()) {
                    this.ahead.set(this.fetch());
                    if (!this.tokens.isEmpty() && this.ahead.get().hashCode()
                        != this.tokens.keySet().iterator().next().hashCode()) {
                        break;
                    }
                    this.shift();
                }
            }
            return !this.tokens.isEmpty();
        }
        /**
         * Shift ahead token to the buffer.
         */
        private void shift() {
            final Pipeline.Token tkn = this.ahead.getAndSet(null);
            this.tokens.put(tkn.value(), tkn);
        }
        /**
         * Iterators are empty, next call to fetch() will throw an exception.
         * @return TRUE if iterators are empty
         */
        private boolean isEmpty() {
            return !this.biterator.hasNext() && !this.citerator.hasNext()
                && this.token.get() == null;
        }
        /**
         * Fetch next token from one of two iterators (called from
         * {@link Pipeline.Buffer#consume()}).
         *
         * <p>This method is merging two iterators, sorting elements according
         * to their hash codes. Catalog iterator {@code this.citerator} has
         * higher priority. We keep items retrieved from iterators in
         * {@code this.token} variable. When variable is set to null it means
         * that we should to one of two iterators for the next value. If
         * the variable holds some value - it is a candidate for the next
         * result of this {@code next()} method.
         *
         * @return The token fetched
         * @throws IOException If some IO problem inside
         * @see Pipeline.Buffer#consume()
         * @checkstyle ExecutableStatementCount (50 lines)
         */
        private Pipeline.Token fetch() throws IOException {
            if (this.token.get() == null) {
                if (this.citerator.hasNext()) {
                    this.token.set(new CatalogToken(this.citerator.next()));
                } else if (this.biterator.hasNext()) {
                    this.token.set(new BacklogToken(this.biterator.next()));
                } else {
                    throw new NoSuchElementException();
                }
            }
            Pipeline.Token next;
            final Pipeline.Token saved = this.token.get();
            if (saved instanceof Pipeline.ComparableToken
                && this.biterator.hasNext()) {
                final Pipeline.ComparableToken comparable =
                    Pipeline.ComparableToken.class.cast(saved);
                final Token btoken = new BacklogToken(this.biterator.next());
                if (comparable.compareTo(btoken) > 0) {
                    next = btoken;
                } else {
                    next = comparable;
                    this.token.set(btoken);
                }
            } else if (this.citerator.hasNext()) {
                final Pipeline.ComparableToken ctoken =
                    new CatalogToken(this.citerator.next());
                if (ctoken.compareTo(saved) > 0) {
                    next = saved;
                    this.token.set(ctoken);
                } else {
                    next = ctoken;
                }
            } else {
                next = saved;
                this.token.set(null);
            }
            return next;
        }
    }

    /**
     * Catalog token.
     */
    private final class CatalogToken implements Pipeline.ComparableToken {
        /**
         * Item to work with.
         */
        private final transient Catalog.Item origin;
        /**
         * Public ctor.
         * @param itm The item
         */
        public CatalogToken(final Catalog.Item itm) {
            this.origin = itm;
        }
        /**
         * {@inheritDoc}
         */
        @Override
        public String value() {
            return this.origin.value();
        }
        /**
         * {@inheritDoc}
         *
         * <p>We don't close the input stream here, because such a closing
         * operation will lead the closing of the entire
         * {@link RandomAccessFile} ({@code Pipeline.this.data}).
         */
        @Override
        public Catalog.Item item() throws IOException {
            final long pos = Pipeline.this.catalog.seek(this.origin.value());
            Pipeline.this.data.seek(pos);
            final DataInputStream input = new DataInputStream(
                Channels.newInputStream(Pipeline.this.data.getChannel())
            );
            long copied = 0;
            while (true) {
                final long num = input.readLong();
                Pipeline.this.output.writeLong(num);
                ++copied;
                if (num == 0) {
                    break;
                }
            }
            Logger.debug(
                this,
                // @checkstyle LineLength (1 line)
                "#item('%s'): copied %d nums (%d bytes) from pos #%d ('%[text]s')",
                Pipeline.this.attribute,
                copied,
                copied * Numbers.SIZE,
                pos,
                this.origin.value()
            );
            return new Catalog.Item(
                this.origin.value(),
                Pipeline.this.opos.getAndAdd(copied * Numbers.SIZE)
            );
        }
        /**
         * {@inheritDoc}
         */
        @Override
        public int compareTo(final Token tkn) {
            return new Integer(this.hashCode()).compareTo(
                new Integer(tkn.hashCode())
            );
        }
        /**
         * {@inheritDoc}
         */
        @Override
        public int hashCode() {
            return this.origin.hashCode();
        }
        /**
         * {@inheritDoc}
         */
        @Override
        public boolean equals(final Object tkn) {
            return this == tkn || tkn.hashCode() == this.hashCode();
        }
    }

    /**
     * Backlog token.
     */
    private final class BacklogToken implements Pipeline.Token {
        /**
         * Item to work with.
         */
        private final transient Backlog.Item origin;
        /**
         * Public ctor.
         * @param itm The item
         */
        public BacklogToken(final Backlog.Item itm) {
            this.origin = itm;
        }
        /**
         * {@inheritDoc}
         */
        @Override
        public String value() {
            return this.origin.value();
        }
        /**
         * {@inheritDoc}
         */
        @Override
        public Catalog.Item item() throws IOException {
            final File file = Pipeline.this.draft.numbers(
                Pipeline.this.attribute, this.origin.path()
            );
            final InputStream input = new FileInputStream(file);
            final int len = IOUtils.copy(
                input,
                Pipeline.this.output
            );
            input.close();
            Logger.debug(
                this,
                "#item('%s'): copied %d bytes from '/%s' ('%[text]s')",
                Pipeline.this.attribute,
                len,
                FilenameUtils.getName(file.getPath()),
                this.origin.value()
            );
            return new Catalog.Item(
                this.origin.value(),
                Pipeline.this.opos.getAndAdd(len)
            );
        }
        /**
         * {@inheritDoc}
         */
        @Override
        public int hashCode() {
            return this.origin.hashCode();
        }
        /**
         * {@inheritDoc}
         */
        @Override
        public boolean equals(final Object tkn) {
            return this == tkn || tkn.hashCode() == this.hashCode();
        }
    }

}
