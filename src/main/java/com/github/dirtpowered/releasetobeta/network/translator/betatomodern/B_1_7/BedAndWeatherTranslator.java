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

package com.github.dirtpowered.releasetobeta.network.translator.betatomodern.B_1_7;

import com.github.dirtpowered.betaprotocollib.packet.Version_B1_7.data.BedAndWeatherPacketData;
import com.github.dirtpowered.releasetobeta.ReleaseToBeta;
import com.github.dirtpowered.releasetobeta.network.session.BetaClientSession;
import com.github.dirtpowered.releasetobeta.network.translator.model.BetaToModern;
import com.github.steveice10.mc.protocol.data.game.world.notify.ClientNotification;
import com.github.steveice10.mc.protocol.data.game.world.notify.ThunderStrengthValue;
import com.github.steveice10.mc.protocol.packet.ingame.server.world.ServerNotifyClientPacket;
import com.github.steveice10.packetlib.Session;

public class BedAndWeatherTranslator implements BetaToModern<BedAndWeatherPacketData> {

    @Override
    public void translate(ReleaseToBeta main, BedAndWeatherPacketData packet, BetaClientSession session, Session modernSession) {
        int state = packet.getWeatherState();

        ClientNotification notification;
        switch (state) {
            case 0:
                notification = ClientNotification.INVALID_BED;
                break;
            case 1:
                notification = ClientNotification.START_RAIN;
                break;
            case 2:
                notification = ClientNotification.STOP_RAIN;
                break;
            default:
                notification = ClientNotification.ENTER_CREDITS; //never happens, so...
                break;
        }

        // ignore this packet in nether
        if (session.getPlayer().getDimension() != 0) {
            modernSession.send(new ServerNotifyClientPacket(ClientNotification.STOP_RAIN, new ThunderStrengthValue(0)));
            return;
        }

        modernSession.send(new ServerNotifyClientPacket(notification, new ThunderStrengthValue(0)));
    }
}
