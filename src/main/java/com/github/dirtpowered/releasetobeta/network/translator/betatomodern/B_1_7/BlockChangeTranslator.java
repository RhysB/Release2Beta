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

import com.github.dirtpowered.betaprotocollib.packet.Version_B1_7.data.BlockChangePacketData;
import com.github.dirtpowered.betaprotocollib.utils.BlockLocation;
import com.github.dirtpowered.releasetobeta.ReleaseToBeta;
import com.github.dirtpowered.releasetobeta.data.blockstorage.BlockDataFixer;
import com.github.dirtpowered.releasetobeta.data.blockstorage.model.CachedBlock;
import com.github.dirtpowered.releasetobeta.network.session.BetaClientSession;
import com.github.dirtpowered.releasetobeta.network.translator.model.BetaToModern;
import com.github.steveice10.mc.protocol.data.game.entity.metadata.Position;
import com.github.steveice10.mc.protocol.data.game.world.block.BlockChangeRecord;
import com.github.steveice10.mc.protocol.data.game.world.block.BlockState;
import com.github.steveice10.mc.protocol.packet.ingame.server.world.ServerBlockChangePacket;
import com.github.steveice10.packetlib.Session;

public class BlockChangeTranslator implements BetaToModern<BlockChangePacketData> {

    @Override
    public void translate(ReleaseToBeta main, BlockChangePacketData packet, BetaClientSession session, Session modernSession) {
        int x = packet.getXPosition();
        int y = packet.getYPosition();
        int z = packet.getZPosition();

        int typeId = packet.getType();
        int data = packet.getMetadata();

        boolean dataFix = false;

        int internalBlockId = main.getServer().convertBlockData(typeId, data, false);

        if (BlockDataFixer.canFix(typeId)) dataFix = true;

        modernSession.send(
                new ServerBlockChangePacket(
                        new BlockChangeRecord(new Position(x, y, z), new BlockState(internalBlockId))
                )
        );

        session.getClientWorldTracker().onBlockUpdate(new BlockLocation(x, y, z), typeId, data);

        if (dataFix) {
            CachedBlock block = BlockDataFixer.fixSingleBlockData(session.getClientWorldTracker(), new CachedBlock(new BlockLocation(x, y, z), typeId, data));
            if (block != null) {
                int newId = main.getServer().convertBlockData(block.getTypeId(), block.getData(), false);

                BlockLocation b = block.getBlockLocation();
                modernSession.send(new ServerBlockChangePacket(
                        new BlockChangeRecord(new Position(b.getX(), b.getY(), b.getZ()), new BlockState(newId))
                ));
            }
        }
    }
}
