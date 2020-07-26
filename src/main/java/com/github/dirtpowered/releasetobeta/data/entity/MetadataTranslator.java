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

package com.github.dirtpowered.releasetobeta.data.entity;

import com.github.dirtpowered.betaprotocollib.data.WatchableObject;
import com.github.dirtpowered.releasetobeta.data.Constants;
import com.github.dirtpowered.releasetobeta.data.entity.model.Entity;
import com.github.dirtpowered.releasetobeta.data.entity.model.Rideable;
import com.github.dirtpowered.releasetobeta.data.entity.monster.EntityCreeper;
import com.github.dirtpowered.releasetobeta.data.entity.monster.EntityEnderDragon;
import com.github.dirtpowered.releasetobeta.data.mapping.flattening.DataConverter;
import com.github.dirtpowered.releasetobeta.data.player.BetaPlayer;
import com.github.dirtpowered.releasetobeta.data.player.ModernPlayer;
import com.github.dirtpowered.releasetobeta.utils.Utils;
import com.github.steveice10.mc.protocol.data.game.entity.metadata.EntityMetadata;
import com.github.steveice10.mc.protocol.data.game.entity.metadata.MetadataType;
import com.github.steveice10.mc.protocol.data.game.entity.metadata.Pose;
import com.github.steveice10.mc.protocol.data.game.entity.type.EntityType;
import com.github.steveice10.mc.protocol.data.game.world.block.BlockState;
import com.github.steveice10.mc.protocol.packet.ingame.server.entity.ServerEntitySetPassengersPacket;
import com.github.steveice10.packetlib.Session;

import java.util.ArrayList;
import java.util.List;

public class MetadataTranslator {

    public EntityMetadata[] toModernMetadata(ModernPlayer target, Session modernSession, Entity e, List<WatchableObject> oldMetadata) {
        if (oldMetadata == null)
            return new EntityMetadata[0];

        EntityType entityType = null;

        if (e != null)
            entityType = e.getEntityType();

        List<EntityMetadata> metadataList = new ArrayList<>();

        for (WatchableObject watchableObject : oldMetadata) {
            MetadataType type = IntegerToDataType(watchableObject.getType());
            int index = watchableObject.getIndex();
            Object value = watchableObject.getValue();

            if (type == MetadataType.BYTE && index == 0) {
                if (((Byte) value).intValue() == 0x04) { //entity mount
                    if (e instanceof ModernPlayer) {
                        ModernPlayer modernPlayer = (ModernPlayer) e;
                        target.sendPacket(new ServerEntitySetPassengersPacket(modernPlayer.getVehicleEntityId(), new int[]{e.getEntityId()}));
                    } else if (e instanceof BetaPlayer) {
                        Entity nearbyEntity = Utils.getNearestEntity(target.getSession().getEntityCache(), e.getLocation());

                        if (nearbyEntity instanceof Rideable) {
                            BetaPlayer betaPlayer = (BetaPlayer) e;

                            int vehicleId = nearbyEntity.getEntityId();
                            betaPlayer.setVehicleEntityId(vehicleId);
                            betaPlayer.setInVehicle(true);

                            target.sendPacket(new ServerEntitySetPassengersPacket(vehicleId, new int[]{e.getEntityId()}));
                        }
                    }
                } else if (((Byte) value).intValue() == 0x00) {
                    if (e instanceof ModernPlayer) {
                        ModernPlayer modernPlayer = (ModernPlayer) e;

                        if (!modernPlayer.isInVehicle()) { //entity un-mount
                            if (modernPlayer.getVehicleEntityId() != -1) {
                                target.sendPacket(new ServerEntitySetPassengersPacket(modernPlayer.getVehicleEntityId(), new int[0]));

                                modernPlayer.setVehicleEntityId(-1);
                            }
                        }
                    } else if (e instanceof BetaPlayer) {
                        BetaPlayer betaPlayer = (BetaPlayer) e;
                        if (betaPlayer.isInVehicle()) {
                            target.sendPacket(new ServerEntitySetPassengersPacket(betaPlayer.getVehicleEntityId(), new int[0]));
                        }

                        if (betaPlayer.isSneaking()) {
                            metadataList.add(new EntityMetadata(6, MetadataType.POSE, Pose.STANDING));
                        }
                    }

                    metadataList.add(new EntityMetadata(0, MetadataType.BYTE, value));
                } else if (((Byte) value).intValue() == 0x02) {
                    if (e instanceof BetaPlayer) {
                        BetaPlayer betaPlayer = (BetaPlayer) e;
                        betaPlayer.setSneaking(true);

                        metadataList.add(new EntityMetadata(6, MetadataType.POSE, Pose.SNEAKING));
                    }
                } else {
                    metadataList.add(new EntityMetadata(0, MetadataType.BYTE, value));
                }
            }

            if ((!(e instanceof BetaPlayer)) && (!(e instanceof ModernPlayer))) {

                if (type == MetadataType.BYTE && index == 16) {
                    //sheep color
                    if (entityType == EntityType.SHEEP) {
                        metadataList.add(new EntityMetadata(16, MetadataType.BYTE, value));
                    } else if (entityType == EntityType.CREEPER) {
                        //creeper fuse
                        Byte b = (Byte) value;
                        if (e instanceof EntityCreeper) {
                            double dist = target.getLocation().distanceTo(e.getLocation());
                            if (dist < Constants.SOUND_RANGE) {
                                ((EntityCreeper) e).onPrime(modernSession);
                            }
                        }

                        metadataList.add(new EntityMetadata(15, MetadataType.INT, b.intValue()));
                    } else if (entityType == EntityType.WOLF) {
                        metadataList.add(new EntityMetadata(16, MetadataType.BYTE, value));
                    } else if (entityType == EntityType.PIG) {
                        boolean hasSaddle = ((Byte) value).intValue() == 1;

                        metadataList.add(new EntityMetadata(16, MetadataType.BOOLEAN, hasSaddle));
                    } else if (entityType == EntityType.SLIME || entityType == EntityType.MAGMA_CUBE) {
                        Byte b = (Byte) value;

                        metadataList.add(new EntityMetadata(15, MetadataType.INT, b.intValue()));
                    } else if (entityType == EntityType.GHAST) {
                        boolean isAggressive = ((Byte) value).intValue() == 1;

                        metadataList.add(new EntityMetadata(15, MetadataType.BOOLEAN, isAggressive));
                    } else if (entityType == EntityType.ENDERMAN) {
                        int itemId = ((Byte) value).intValue();

                        if (((Byte) value).intValue() > 0) {
                            metadataList.add(new EntityMetadata(15, MetadataType.BLOCK_STATE, new BlockState(DataConverter.getNewItemId(itemId, 0))));
                        }
                    } else if (entityType == EntityType.BLAZE) {
                        metadataList.add(new EntityMetadata(15, MetadataType.BYTE, value));
                    }
                }

                if (type == MetadataType.BYTE && index == 17) {
                    if (entityType == EntityType.CREEPER || entityType == EntityType.ENDERMAN) {
                        //is powered or enderman screaming
                        boolean state = ((Byte) value).intValue() == 1;
                        metadataList.add(new EntityMetadata(16, MetadataType.BOOLEAN, state));
                    }
                }

                if (type == MetadataType.INT && index == 15) {
                    //baby animals
                    metadataList.add(new EntityMetadata(15, MetadataType.BOOLEAN, (int) value < 0));
                }

                if (type == MetadataType.INT && index == 16) {
                    if (entityType == EntityType.ENDER_DRAGON) {
                        int health = (int) value;
                        EntityEnderDragon enderDragon = (EntityEnderDragon) e;
                        enderDragon.updateHealth(modernSession, health);
                    }
                }
            }
        }

        return metadataList.toArray(new EntityMetadata[0]);
    }

    private MetadataType IntegerToDataType(int type) {
        MetadataType metadataType;

        switch (type) {
            case 0:
                metadataType = MetadataType.BYTE;
                break;
            case 2:
                metadataType = MetadataType.INT;
                break;
            case 3:
                metadataType = MetadataType.FLOAT;
                break;
            case 4:
                metadataType = MetadataType.STRING;
                break;
            case 5:
                metadataType = MetadataType.ITEM;
                break;
            case 6:
                metadataType = MetadataType.POSITION;
                break;
            default:
                metadataType = null;
                break;
        }
        return metadataType;
    }
}
