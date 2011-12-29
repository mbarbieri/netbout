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
package com.netbout.rest;

import com.netbout.bus.Bus;
import com.netbout.spi.Bout;
import com.netbout.spi.Participant;
import com.netbout.spi.Urn;
import com.netbout.utils.TextUtils;
import java.util.ArrayList;
import java.util.Collection;

/**
 * Coordinates of a stage.
 *
 * @author Yegor Bugayenko (yegor@netbout.com)
 * @version $Id$
 */
public final class StageCoordinates {

    /**
     * Separator between stage and place.
     */
    private static final String SEPARATOR = "::";

    /**
     * List of all stages.
     */
    private transient Collection<Urn> stages;

    /**
     * Name of stage.
     */
    private transient Urn istage = new Urn();

    /**
     * Place of stage.
     */
    private transient String iplace = "";

    /**
     * Get stage.
     * @return The name of it
     */
    public Urn stage() {
        if (this.stages == null) {
            throw new IllegalStateException(
                "Call #normalize() before #stage()"
            );
        }
        return this.istage;
    }

    /**
     * Set stage.
     * @param name The name of it
     */
    public void setStage(final Urn name) {
        this.istage = name;
    }

    /**
     * Get place.
     * @return The name of it
     */
    public String place() {
        if (this.stages == null) {
            throw new IllegalStateException(
                "Call #normalize() before #place()"
            );
        }
        return this.iplace;
    }

    /**
     * Set stage place.
     * @param place The place name
     */
    public void setPlace(final String place) {
        this.iplace = place;
    }

    /**
     * Create stage coordinates from string.
     * @param pair The information from cookie
     * @return The object just built
     */
    public static StageCoordinates valueOf(final String pair) {
        final StageCoordinates coords = new StageCoordinates();
        if (pair != null && pair.contains(StageCoordinates.SEPARATOR)) {
            try {
                coords.setStage(
                    new Urn(
                        TextUtils.unpack(
                            pair.substring(
                                0,
                                pair.indexOf(StageCoordinates.SEPARATOR)
                            )
                        )
                    )
                );
                coords.setPlace(
                    TextUtils.unpack(
                        pair.substring(
                            pair.indexOf(StageCoordinates.SEPARATOR)
                            + StageCoordinates.SEPARATOR.length()
                        )
                    )
                );
            } catch (java.net.URISyntaxException ex) {
                coords.setStage(new Urn());
                coords.setPlace("");
            }
        }
        return coords;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        if (this.stages == null) {
            throw new IllegalStateException("Call #normalize() before");
        }
        return String.format(
            "%s%s%s",
            TextUtils.pack(this.stage().toString()),
            this.SEPARATOR,
            TextUtils.pack(this.place())
        );
    }

    /**
     * List of all stages, their names.
     * @return The list
     */
    public Collection<Urn> all() {
        if (this.stages == null) {
            throw new IllegalStateException("Call #normalize() before #all()");
        }
        return this.stages;
    }

    /**
     * Normalize it according to the bout.
     * @param bus The bus
     * @param bout The bout
     */
    public void normalize(final Bus bus, final Bout bout) {
        if (this.stages != null) {
            throw new IllegalStateException("Duplicate call to #normalize()");
        }
        this.stages = new ArrayList<Urn>();
        for (Participant dude : bout.participants()) {
            final Urn name = dude.identity().name();
            final Boolean exists = bus.make("does-stage-exist")
                .synchronously()
                .arg(bout.number())
                .arg(name)
                .inBout(bout)
                .asDefault(false)
                .exec();
            if (exists) {
                this.stages.add(name);
            }
        }
        if (this.istage.isEmpty() && this.stages.size() > 0) {
            this.istage = this.stages.iterator().next();
        }
        if (!this.stages.contains(this.istage)) {
            this.istage = new Urn();
        }
    }

}
