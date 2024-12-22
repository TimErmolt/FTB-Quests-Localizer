package timermolt.ftbqpl.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import dev.ftb.mods.ftbquests.api.FTBQuestsAPI;
import dev.ftb.mods.ftbquests.quest.*;
import timermolt.ftbqpl.FTBQuestPrecisionLocalizerMod;
import timermolt.ftbqpl.handler.impl.Handler;
import timermolt.ftbqpl.utils.BackPortUtils;
import timermolt.ftbqpl.utils.Constants;
import timermolt.ftbqpl.utils.HandlerHelper;
//import timermolt.ftbqpl.utils.PackUtils;
//import net.minecraft.client.Minecraft;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
//import net.minecraft.network.chat.ChatType;
import net.minecraft.network.chat.Component;
//import net.minecraft.network.chat.OutgoingChatMessage;
//import net.minecraft.network.chat.PlayerChatMessage;
//import net.minecraft.client.player.LocalPlayer;
//import net.minecraftforge.fml.loading.FMLPaths;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Locale;

//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;

public class FTBQLangConvert {
    public static String langStr = "en_us";
    public static String prefixStr = "ftbquests";
    public static String current_mode = "normal";

    //private static final Logger log = LoggerFactory.getLogger(BackPortUtils.class);

    public FTBQLangConvert(CommandDispatcher<CommandSourceStack> dispatcher) {


        dispatcher.register(Commands.literal("ftblang")
                        .then(Commands.argument("lang", StringArgumentType.word())
                        .then(Commands.argument("prefix", StringArgumentType.word())
                        .then(Commands.argument("current_mode", StringArgumentType.word())

//                .requires(s->s.getServer() != null && s.getServer().isSingleplayer() || s.hasPermission(2))
                                        .executes(ctx ->{
                                            try{
                                                
                                                ServerQuestFile serverQuestFile = ServerQuestFile.INSTANCE;
                                                serverQuestFile.markDirty();
                                                serverQuestFile.saveNow();
                                                File parent = new File(Constants.PackMCMeta.GAMEDIR, Constants.PackMCMeta.OUTPUTFOLDER);
                                                File questsFolder = new File(Constants.PackMCMeta.GAMEDIR, "config\\" + Constants.PackMCMeta.QUESTFOLDER);
                                                //File overridesFolder = new File(Constants.PackMCMeta.GAMEDIR, Constants.PackMCMeta.OVERRIDESFOLDER);

                                                langStr = ctx.getArgument("lang", String.class);
                                                prefixStr = ctx.getArgument("prefix", String.class);
                                                current_mode = ctx.getArgument("current_mode", String.class);
                                                BackPortUtils.backport();
                                                
                                                ConvertQuestsInFolder(questsFolder, langStr, prefixStr, current_mode);

                                                //LocalPlayer player = Minecraft.getInstance().player;

                                                //PlayerChatMessage chatMessage = PlayerChatMessage.unsigned(player.getUUID(), "Now looking for the overrides folder...");
                                                //player.createCommandSourceStack().sendChatMessage(new OutgoingChatMessage.Player(chatMessage), false, ChatType.bind(ChatType.CHAT, player));

                                                /*

                                                if(overridesFolder.exists()) {
                                                    //chatMessage = PlayerChatMessage.unsigned(player.getUUID(), "Found the overrides folder! " + overridesFolder.getName());
                                                    //player.createCommandSourceStack().sendChatMessage(new OutgoingChatMessage.Player(chatMessage), false, ChatType.bind(ChatType.CHAT, player));
                                                    for (File file : overridesFolder.listFiles()) {
                                                        //chatMessage = PlayerChatMessage.unsigned(player.getUUID(), "Checking file " + file.getName());
                                                        //player.createCommandSourceStack().sendChatMessage(new OutgoingChatMessage.Player(chatMessage), false, ChatType.bind(ChatType.CHAT, player));
                                                        if(file.isDirectory()) {
                                                            File overrideQuests = new File(file.getPath() + "\\" + Constants.PackMCMeta.QUESTFOLDER);
                                                            //chatMessage = PlayerChatMessage.unsigned(player.getUUID(), "Checking for FTB quests in override... " + overrideQuests.getPath());
                                                            //player.createCommandSourceStack().sendChatMessage(new OutgoingChatMessage.Player(chatMessage), false, ChatType.bind(ChatType.CHAT, player));
                                                            if(overrideQuests.exists()) {
                                                                ConvertQuestsInFolder(overrideQuests, langStr, prefixStr, file.getName());
                                                                //chatMessage = PlayerChatMessage.unsigned(player.getUUID(), "Converted successfully!");
                                                                //player.createCommandSourceStack().sendChatMessage(new OutgoingChatMessage.Player(chatMessage), false, ChatType.bind(ChatType.CHAT, player));
                                                            }
                                                        }
                                                    }
                                                }
                                                
                                                */

                                                ctx.getSource().getPlayerOrException().displayClientMessage(Component.literal("FTB quests files exported to: " + parent.getAbsolutePath()), true);
                                            }catch(Exception e){
                                                e.printStackTrace();
                                            }

                                            return 1;

                                        })
                        )

        )));

    }
    private void saveLang(String lang, File parent) throws IOException
    {
        File fe = new File(parent, lang.toLowerCase(Locale.ROOT) + ".json");
        FileUtils.write(fe, FTBQuestPrecisionLocalizerMod.gson.toJson(HandlerHelper.transKeys), StandardCharsets.UTF_8);

        // Resourcepack creation got broken by something. I'm not sure what. It's disabled for now. ~TE 22.12.2024
        // PackUtils.createResourcePack(fe, FMLPaths.GAMEDIR.get().toFile()+"\\FTBLang\\FTB-Quests-Localization-Resourcepack.zip");
    }

    private void ConvertQuestsInFolder(File folder, String lang, String prefix, String current_mode) throws IOException {
        Handler handler = new Handler(current_mode, lang);
        File parent = new File(Constants.PackMCMeta.GAMEDIR, Constants.PackMCMeta.OUTPUTFOLDER);
        File kubejsOutput = new File(parent, Constants.PackMCMeta.KUBEJSFOLDER);
        File kubejsBackupFile = new File(parent, Constants.PackMCMeta.KUBEJSBACKUPFOLDER);
        Boolean is_main_config = (current_mode.equals("normal")); 

        if (kubejsOutput.exists()){
            FileUtils.copyDirectory(kubejsOutput, kubejsBackupFile);
        }

        if(folder.exists()){
            File backup = new File(parent, Constants.PackMCMeta.BACKUPFOLDER);
            FileUtils.copyDirectory(folder, backup);
        }
        HandlerHelper.setPrefix(prefix);
        BaseQuestFile questFile = FTBQuestsAPI.api().getQuestFile(false);
        //log.info("Quest file for folder " + current_mode + " is " + questFile.getPath());
        handler.handleRewardTables(questFile.getRewardTables());
        questFile.forAllChapterGroups(handler::handleChapterGroup);
        HandlerHelper.setCounter(0);
        questFile.forAllChapters(chapter -> {
            handler.handleChapter(chapter);
            handler.handleQuests(chapter.getQuests());
        });

        File output = new File(parent, (is_main_config ? "config\\" : (Constants.PackMCMeta.OVERRIDESFOLDER + current_mode + "\\" + Constants.PackMCMeta.QUESTFOLDER)));
        questFile.writeDataFull(output.toPath());
        ServerQuestFile.INSTANCE.load();
        saveLang(lang + (is_main_config ? "" : "_" + current_mode), kubejsOutput);

        // If we've just generated a lang file not for `en_us` -- make `en_us`, too!
        if(!langStr.equalsIgnoreCase("en_us")){
            saveLang("en_us" + (is_main_config ? "" : "_" + current_mode), kubejsOutput);
        }
    }
}
