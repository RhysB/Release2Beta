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

package com.github.dirtpowered.releasetobeta.bootstrap.bukkit;

import com.github.dirtpowered.releasetobeta.ReleaseToBeta;
import com.github.dirtpowered.releasetobeta.bootstrap.AbstractBootstrap;
import com.github.dirtpowered.releasetobeta.bootstrap.Platform;
import com.github.dirtpowered.releasetobeta.logger.AbstractLogger;
import com.github.dirtpowered.releasetobeta.logger.BukkitLogger;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.logging.Logger;

public class BukkitBootstrap extends JavaPlugin implements AbstractBootstrap {
    private static final Logger LOGGER = Logger.getLogger("Minecraft");

    private ReleaseToBeta main;
    private BukkitLogger logger;

    @Override
    public AbstractLogger getAppLogger() {
        return logger;
    }

    @Override
    public String getConfigPath() {
        return getDataFolder().getAbsolutePath();
    }

    @Override
    public Platform getPlatform() {
        return Platform.BUKKIT;
    }

    @Override
    public int getOnline() {
        return Bukkit.getOnlinePlayers().length;
    }

    @Override
    public void onEnable() {
        logger = new BukkitLogger(LOGGER);

        if (!getDataFolder().mkdir() && !getDataFolder().exists())
            logger.error("unable to create default plugin directory");

        main = new ReleaseToBeta(this);
    }

    @Override
    public void onDisable() {
        main.stop();
    }
}
