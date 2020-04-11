/*
 * Copyright (c) 2020 Dirt Powered
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package com.github.dirtpowered.releasetobeta;

import com.github.dirtpowered.betaprotocollib.BetaLib;
import com.github.dirtpowered.releasetobeta.configuration.R2BConfiguration;
import com.github.dirtpowered.releasetobeta.data.mapping.BlockMap;
import com.github.dirtpowered.releasetobeta.data.mapping.EntityEffectMap;
import com.github.dirtpowered.releasetobeta.data.mapping.MetadataMap;
import com.github.dirtpowered.releasetobeta.data.mapping.SoundEffectMap;
import com.github.dirtpowered.releasetobeta.network.protocol.B_1_7;
import com.github.dirtpowered.releasetobeta.network.protocol.B_1_8;
import com.github.dirtpowered.releasetobeta.network.server.ModernServer;
import com.github.dirtpowered.releasetobeta.network.server.ping.LegacyPing.PingPassthroughThread;
import com.github.dirtpowered.releasetobeta.network.session.SessionRegistry;
import com.github.dirtpowered.releasetobeta.network.translator.internal.ClientResourcePackStatusTranslator;
import com.github.dirtpowered.releasetobeta.network.translator.internal.ClientTabCompleteTranslator;
import com.github.dirtpowered.releasetobeta.network.translator.registry.BetaToModernTranslatorRegistry;
import com.github.dirtpowered.releasetobeta.network.translator.registry.ModernToBetaTranslatorRegistry;
import com.github.steveice10.mc.protocol.packet.ingame.client.ClientResourcePackStatusPacket;
import com.github.steveice10.mc.protocol.packet.ingame.client.ClientTabCompletePacket;
import lombok.Getter;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Getter
public class ReleaseToBeta implements Runnable {
    private final ScheduledExecutorService scheduledExecutorService;
    private SessionRegistry sessionRegistry;
    private BetaToModernTranslatorRegistry betaToModernTranslatorRegistry;
    private ModernToBetaTranslatorRegistry modernToBetaTranslatorRegistry;
    private SoundEffectMap soundEffectMap;
    private EntityEffectMap entityEffectMap;
    private BlockMap blockMap;
    private MetadataMap metadataMap;
    private ModernServer server;
    private PingPassthroughThread pingPassthroughThread;

    ReleaseToBeta() {
        new R2BConfiguration().loadConfiguration(); //load config

        this.scheduledExecutorService = Executors.newScheduledThreadPool(32);
        this.sessionRegistry = new SessionRegistry();
        this.betaToModernTranslatorRegistry = new BetaToModernTranslatorRegistry();
        this.modernToBetaTranslatorRegistry = new ModernToBetaTranslatorRegistry();
        this.soundEffectMap = new SoundEffectMap();
        this.blockMap = new BlockMap();
        this.metadataMap = new MetadataMap();
        this.entityEffectMap = new EntityEffectMap();
        this.server = new ModernServer(this);

        BetaLib.inject(R2BConfiguration.version);

        switch (R2BConfiguration.version) {
            case B_1_7_3:
                new B_1_7(betaToModernTranslatorRegistry, modernToBetaTranslatorRegistry);
                break;
            case B_1_8_1:
                new B_1_8(betaToModernTranslatorRegistry, modernToBetaTranslatorRegistry);
                break;
        }

        //internal translators
        if (R2BConfiguration.tabComplete) {
            modernToBetaTranslatorRegistry.registerTranslator(ClientTabCompletePacket.class, new ClientTabCompleteTranslator());
        }

        modernToBetaTranslatorRegistry.registerTranslator(ClientResourcePackStatusPacket.class, new ClientResourcePackStatusTranslator());

        ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor(r -> new Thread(r, "Main Thread"));
        executor.scheduleAtFixedRate(this, 0L, 50L, TimeUnit.MILLISECONDS);

        if (R2BConfiguration.ver1_8PingPassthrough) {
            this.pingPassthroughThread = new PingPassthroughThread();
            scheduledExecutorService.scheduleAtFixedRate(pingPassthroughThread, 0L, 5L, TimeUnit.SECONDS);
        }
    }

    void stop() {
        sessionRegistry.getSessions().clear();
    }

    @Override
    public void run() {
        sessionRegistry.getSessions().forEach((username, internalSession) -> {
            internalSession.getBetaClientSession().tick();
        });

        this.server.getServerConnection().tick();
    }
}
