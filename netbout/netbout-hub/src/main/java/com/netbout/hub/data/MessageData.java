/**
 * Copyright (c) 2009-2011, netBout.com
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
package com.netbout.hub.data;

import com.netbout.hub.queue.HelpQueue;
import com.ymock.util.Logger;
import java.util.Date;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * One message in a bout.
 *
 * @author Yegor Bugayenko (yegor@netbout.com)
 * @version $Id$
 */
public final class MessageData implements Comparable<MessageData> {

    /**
     * Number of the message.
     */
    private final Long number;

    /**
     * The date.
     */
    private Date date;

    /**
     * The author.
     */
    private String author;

    /**
     * The text.
     */
    private String text;

    /**
     * Who already have seen this message, and who haven't?
     */
    private ConcurrentMap<String, Boolean> seenBy =
        new ConcurrentHashMap<String, Boolean>();

    /**
     * Public ctor.
     * @param num The number of this message
     */
    protected MessageData(final Long num) {
        assert num != null;
        this.number = num;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int compareTo(final MessageData data) {
        return this.getDate().compareTo(data.getDate());
    }

    /**
     * Get message number.
     * @return The number of it
     */
    public Long getNumber() {
        return this.number;
    }

    /**
     * Set date of the message.
     * @param dte The identity
     */
    public void setDate(final Date dte) {
        this.date = dte;
        HelpQueue.make("changed-message-date")
            .priority(HelpQueue.Priority.ASAP)
            .arg(this.number)
            .arg(this.date)
            .exec();
        Logger.debug(
            this,
            "#setDate('%s'): set",
            this.date
        );
    }

    /**
     * Get date of the message.
     * @return The date
     */
    public Date getDate() {
        if (this.date == null) {
            this.date = HelpQueue.make("get-message-date")
                .priority(HelpQueue.Priority.SYNCHRONOUSLY)
                .arg(this.number)
                .exec(Date.class);
            Logger.debug(
                this,
                "#getDate(): date '%s' loaded for msg #%d",
                this.date,
                this.number
            );
        }
        return this.date;
    }

    /**
     * Set identity.
     * @param idnt The identity
     */
    public void setAuthor(final String idnt) {
        this.author = idnt;
        HelpQueue.make("changed-message-author")
            .priority(HelpQueue.Priority.ASAP)
            .arg(this.number)
            .arg(this.author)
            .exec();
        Logger.debug(
            this,
            "#setAuthor('%s'): set for msg #%d",
            this.author,
            this.number
        );
    }

    /**
     * Get identity.
     * @return The identity
     */
    public String getAuthor() {
        if (this.author == null) {
            this.author = HelpQueue.make("get-message-author")
                .priority(HelpQueue.Priority.SYNCHRONOUSLY)
                .arg(this.number)
                .exec(String.class);
            Logger.debug(
                this,
                "#getAuthor(): author '%s' loaded for msg #%d",
                this.author,
                this.number
            );
        }
        return this.author;
    }

    /**
     * Set text.
     * @param txt The text
     */
    public void setText(final String txt) {
        this.text = txt;
        HelpQueue.make("changed-message-text")
            .priority(HelpQueue.Priority.ASAP)
            .arg(this.number)
            .arg(this.text)
            .exec();
        Logger.debug(
            this,
            "#setText('%s'): set for msg #%d",
            this.text,
            this.number
        );
    }

    /**
     * Get text.
     * @return The text
     */
    public String getText() {
        if (this.text == null) {
            this.text = HelpQueue.make("get-message-text")
                .priority(HelpQueue.Priority.SYNCHRONOUSLY)
                .arg(this.number)
                .exec(String.class);
            Logger.debug(
                this,
                "#getText(): text '%s' loaded for msg #%d",
                this.text,
                this.number
            );
        }
        return this.text;
    }

    /**
     * Add indentity, who has seen the message.
     * @param identity The identity
     */
    public void addSeenBy(final String identity) {
        if (!this.seenBy.containsKey(identity) || !this.seenBy.get(identity)) {
            HelpQueue.make("message-was-seen")
                .priority(HelpQueue.Priority.ASAP)
                .arg(this.number)
                .arg(identity)
                .exec();
            Logger.debug(
                this,
                "#addSeenBy('%s'): set for msg #%d",
                identity,
                this.number
            );
        }
        this.seenBy.put(identity, true);
    }

    /**
     * Was it seen by this identity?
     * @param identity The identity
     * @return Was it seen?
     */
    public Boolean isSeenBy(final String identity) {
        if (!this.seenBy.containsKey(identity)) {
            final Boolean status = HelpQueue.make("was-message-seen")
                .priority(HelpQueue.Priority.SYNCHRONOUSLY)
                .arg(this.number)
                .arg(identity)
                .asDefault(Boolean.FALSE)
                .exec(Boolean.class);
            this.seenBy.put(identity, status);
            Logger.debug(
                this,
                "#isSeenBy('%s'): %b loaded for msg #%d",
                identity,
                status,
                this.number
            );
        }
        return this.seenBy.get(identity);
    }

}
