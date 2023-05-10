package org.wolflink.minecraft.wolfird.framework.subplugin;

import lombok.NoArgsConstructor;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.java.JavaPluginLoader;
import org.wolflink.minecraft.wolfird.framework.ioc.ModeContainer;

import java.io.File;

/**
 * 游戏模式拓展插件
 * 例如：生存模式、猎人游戏、时间陷阱、饥饿游戏、极限生存冠军...
 */
@NoArgsConstructor
public abstract class ModePlugin extends SubPlugin {

    protected ModePlugin(JavaPluginLoader loader, PluginDescriptionFile desc, File dataFolder, File file) {
        super(loader, desc, dataFolder, file);
    }
    @Override
    protected void init(){
        ModeContainer container = getContainer();
        container.pluginItems.put(info.getFullName(),this);
    }
}
