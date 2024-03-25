package net.ftbconv.events;


import net.ftbconv.commands.FTBQLangConvert;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.server.command.ConfigCommand;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;

import static net.ftbconv.utils.Constants.PackMCMeta.MOD_NAME;


@EventBusSubscriber
public class ModEvents {

    @SubscribeEvent
    public static void onCommandsRegister(RegisterCommandsEvent event){
        new FTBQLangConvert(event.getDispatcher());

        ConfigCommand.register(event.getDispatcher());
    }
}
