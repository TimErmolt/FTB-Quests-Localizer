package timermolt.ftbqpl.events;


import timermolt.ftbqpl.commands.FTBQLangConvert;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.server.command.ConfigCommand;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;


@EventBusSubscriber
public class ModEvents {

    @SubscribeEvent
    public static void onCommandsRegister(RegisterCommandsEvent event){
        new FTBQLangConvert(event.getDispatcher());

        ConfigCommand.register(event.getDispatcher());
    }
}
