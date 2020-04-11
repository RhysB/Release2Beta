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

package com.github.dirtpowered.releasetobeta.network.server;

import com.github.dirtpowered.releasetobeta.ReleaseToBeta;
import com.github.dirtpowered.releasetobeta.data.command.CommandRegistry;
import com.github.dirtpowered.releasetobeta.data.command.R2BCommand;
import com.github.dirtpowered.releasetobeta.data.command.model.Command;
import com.github.dirtpowered.releasetobeta.data.entity.EntityRegistry;
import com.github.dirtpowered.releasetobeta.data.player.ModernPlayer;
import com.github.dirtpowered.releasetobeta.data.skin.ProfileCache;
import lombok.Getter;

@Getter
public class ModernServer {
    private ReleaseToBeta main;
    private ServerConnection serverConnection;
    private EntityRegistry entityRegistry;
    private ProfileCache profileCache;
    private CommandRegistry commandRegistry;
    private String[] commands;

    public ModernServer(ReleaseToBeta releaseToBeta) {
        this.main = releaseToBeta;

        this.serverConnection = new ServerConnection(this);
        this.entityRegistry = new EntityRegistry();
        this.profileCache = new ProfileCache();
        this.commandRegistry = new CommandRegistry();
        this.commands = commandRegistry.getCommands().keySet().toArray(new String[0]);

        registerInternalCommands();
    }

    private void registerInternalCommands() {
        commandRegistry.register("releasetobeta", new R2BCommand());
    }

    public boolean executeCommand(ModernPlayer player, String message) {
        message = message.substring(1);
        String[] args = message.trim().split("\\s+");

        Command command;
        if (commandRegistry.getCommands().containsKey(args[0])) {
            command = commandRegistry.getCommands().get(args[0]);
            command.execute(player, args);
            return true;
        }

        return false;
    }
}
