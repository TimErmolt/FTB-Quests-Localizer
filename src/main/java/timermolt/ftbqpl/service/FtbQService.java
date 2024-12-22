package timermolt.ftbqpl.service;

import net.minecraft.network.chat.Component;
import timermolt.ftbqpl.handler.impl.Handler;


public interface FtbQService {
    void handleJSON(Component parsedText, String prefix, Handler handler);
}
