package net.ftbconv;

import com.google.gson.*;
import com.mojang.brigadier.CommandDispatcher;
import net.ftbconv.commands.FTBQLangConvert;
import net.ftbconv.events.ModEvents;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.neoforged.neoforge.common.NeoForge;

import net.neoforged.fml.common.Mod;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


@Mod("ftbquestlocalization")
public class FtbLangConvertMod{
	private static final Logger log = LoggerFactory.getLogger(FtbLangConvertMod.class);
	public static final Gson gson = new GsonBuilder().setPrettyPrinting().create();

	public FtbLangConvertMod(){
		NeoForge.EVENT_BUS.addListener(ModEvents::onCommandsRegister);
		}

}

