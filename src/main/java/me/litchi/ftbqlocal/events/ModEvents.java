package me.litchi.ftbqlocal.events;


import me.litchi.ftbqlocal.commands.FTBQLangConvert;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod.EventBusSubscriber;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.neoforged.neoforge.server.command.ConfigCommand;


@EventBusSubscriber
public class ModEvents {

    @SubscribeEvent
    public static void onCommandsRegister(RegisterCommandsEvent event){
        new FTBQLangConvert(event.getDispatcher());

        ConfigCommand.register(event.getDispatcher());
    }
}
