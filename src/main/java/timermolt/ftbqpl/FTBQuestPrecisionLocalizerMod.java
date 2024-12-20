package timermolt.ftbqpl;

import com.google.gson.*;
import timermolt.ftbqpl.events.ModEvents;
import net.minecraftforge.common.MinecraftForge;

import net.minecraftforge.fml.common.Mod;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


@Mod("ftbquestprecisionlocalizer")
public class FTBQuestPrecisionLocalizerMod {
	public static final Logger log = LoggerFactory.getLogger(FTBQuestPrecisionLocalizerMod.class);
	public static final Gson gson = new GsonBuilder().setPrettyPrinting().create();

	public FTBQuestPrecisionLocalizerMod(){
		MinecraftForge.EVENT_BUS.addListener(ModEvents::onCommandsRegister);
		}

}
