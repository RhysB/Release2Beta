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

package com.github.dirtpowered.releasetobeta.data.entity.model;

import com.github.dirtpowered.betaprotocollib.utils.Location;
import com.github.dirtpowered.releasetobeta.data.Constants;
import com.github.dirtpowered.releasetobeta.data.player.ModernPlayer;
import com.github.dirtpowered.releasetobeta.utils.interfaces.Tickable;
import com.github.steveice10.mc.protocol.data.game.entity.type.MobType;
import com.github.steveice10.mc.protocol.data.game.world.sound.BuiltinSound;
import com.github.steveice10.mc.protocol.data.game.world.sound.SoundCategory;
import com.github.steveice10.mc.protocol.packet.ingame.server.world.ServerPlayBuiltinSoundPacket;
import com.github.steveice10.packetlib.Session;
import lombok.Getter;
import lombok.Setter;

import java.util.Random;

@Getter
public abstract class Entity implements Tickable {

    @Setter
    private int entityId;

    private MobType mobType;
    private boolean betaPlayer;

    @Setter
    private Location location;

    private Random rand;

    public Entity(int entityId, MobType type) {
        this.entityId = entityId;
        this.mobType = type;
        this.betaPlayer = false;

        this.location = new Location(0, 0, 0);
        this.rand = new Random();
    }

    public Entity(int entityId) {
        this.entityId = entityId;
        this.betaPlayer = false;

        this.location = new Location(0, 0, 0);
        this.rand = new Random();
    }

    public Entity(int entityId, boolean isBetaPlayer) {
        this.entityId = entityId;
        this.betaPlayer = isBetaPlayer;

        this.location = new Location(0, 0, 0);
        this.rand = new Random();
    }

    public void onSpawn(Session session) {
        //TODO: make use of it
    }

    public void updateEntity(ModernPlayer player, Session session) {
        if (rand.nextDouble() < 0.0074D) {
            double dist = location.distanceTo(player.getLocation());
            if (dist < Constants.SOUND_RANGE) {
                if (this instanceof Mob && getMobType() != null) {
                    Mob mob = (Mob) this;

                    mob.onUpdate(session);
                }
            }
        }

        tick();
    }

    @Override
    public void tick() {
        // do nothing
    }

    protected void playSound(Session session, BuiltinSound sound, SoundCategory category) {
        session.send(new ServerPlayBuiltinSoundPacket(sound, category, location.getX(), location.getY(), location.getZ(), 1.f, 1.f));
    }
}
