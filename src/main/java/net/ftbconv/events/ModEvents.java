package net.ftbconv.events;


import net.ftbconv.commands.FTBQLangConvert;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.server.command.ConfigCommand;
import net.neoforged.fml.common.Mod.EventBusSubscriber;


@EventBusSubscriber
public class ModEvents {

    @SubscribeEvent
    public static void onCommandsRegister(RegisterCommandsEvent event){
        new FTBQLangConvert(event.getDispatcher());

        ConfigCommand.register(event.getDispatcher());
    }
}
