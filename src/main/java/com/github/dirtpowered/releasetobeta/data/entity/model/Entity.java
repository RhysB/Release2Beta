package com.github.dirtpowered.releasetobeta.data.entity.model;

import com.github.steveice10.mc.protocol.data.game.entity.type.MobType;
import com.github.steveice10.mc.protocol.data.game.world.sound.BuiltinSound;
import com.github.steveice10.mc.protocol.data.game.world.sound.SoundCategory;
import com.github.steveice10.mc.protocol.packet.ingame.server.world.ServerPlayBuiltinSoundPacket;
import com.github.steveice10.packetlib.Session;

public abstract class Entity {

    private int entityId;
    private MobType mobType;
    private boolean betaPlayer;
    private int x, y, z;

    public Entity(int entityId, MobType type) {
        this.entityId = entityId;
        this.mobType = type;
        this.betaPlayer = false;
    }

    public Entity(int entityId) {
        this.entityId = entityId;
        this.betaPlayer = false;
    }

    public Entity(int entityId, boolean isBetaPlayer) {
        this.entityId = entityId;
        this.betaPlayer = isBetaPlayer;
    }

    public abstract void onSpawn(Session session);

    public boolean isBetaPlayer() {
        return betaPlayer;
    }

    public MobType getMobType() {
        return mobType;
    }

    public int getEntityId() {
        return entityId;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public int getZ() {
        return z;
    }

    public void setLocation(int x, int y, int z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public void setLocation(double x, double y, double z) {
        this.x = (int) x;
        this.y = (int) y;
        this.z = (int) z;
    }

    protected void playSound(Session session, BuiltinSound sound, SoundCategory category) {
        session.send(new ServerPlayBuiltinSoundPacket(sound, category, x, y, z, 1.f, 1.f));
    }
}