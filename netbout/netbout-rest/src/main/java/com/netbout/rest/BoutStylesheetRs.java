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

import com.netbout.spi.Bout;
import com.netbout.spi.Urn;
import com.netbout.utils.TextUtils;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import org.apache.velocity.VelocityContext;

/**
 * Stage-related requests.
 *
 * @author Yegor Bugayenko (yegor@netbout.com)
 * @version $Id$
 */
@Path("/{num : [0-9]+}/xsl/{stage : [\\w:\\.\\-]+}")
public final class BoutStylesheetRs extends AbstractRs {

    /**
     * Number of the bout.
     */
    private transient Long number;

    /**
     * Name of the stage.
     */
    private transient Urn stage;

    /**
     * Set bout number, and verify that this bout is accessible by this
     * identity.
     * @param num The number
     */
    @PathParam("num")
    public void setBout(final Long num) {
        this.number = num;
    }

    /**
     * Set stage name.
     * @param name Name of the stage
     */
    @PathParam("stage")
    public void setStage(final Urn name) {
        this.stage = name;
    }

    /**
     * Get wrapper XSL.
     * @return The XSL
     */
    @GET
    @Path("/wrapper.xsl")
    @Produces("text/xsl")
    public String boutXsl() {
        final VelocityContext context = new VelocityContext();
        context.put(
            "boutXsl",
            TextUtils.ucode(this.base().path("/xsl/bout.xsl").build())
        );
        context.put(
            "stageXsl",
            TextUtils.ucode(
                this.base()
                    .path("/{bout}/xsl/{stage}/stage.xsl")
                    .build(this.bout().number(), this.stage)
            )
        );
        return TextUtils.format("com/netbout/rest/wrapper.xsl.vm", context);
    }

    /**
     * Get stage XSL.
     * @return The XSL
     */
    @GET
    @Path("/stage.xsl")
    @Produces("text/xsl")
    public String stageXsl() {
        String xsl =
            "<stylesheet xmlns='http://www.w3.org/1999/XSL/Transform'/>";
        if (!this.stage.isEmpty()) {
            xsl = this.bus().make("render-stage-xsl")
                .synchronously()
                .arg(this.bout().number())
                .arg(this.stage)
                .asDefault(xsl)
                .exec();
        }
        return xsl;
    }

    /**
     * Get bout to work with.
     * @return The bout
     */
    private Bout bout() {
        try {
            return this.identity().bout(this.number);
        } catch (com.netbout.spi.BoutNotFoundException ex) {
            throw new ForwardException(this, this.base(), ex);
        }
    }

}