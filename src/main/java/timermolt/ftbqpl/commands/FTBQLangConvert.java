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
import timermolt.ftbqpl.utils.PackUtils;
//import net.minecraft.client.Minecraft;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
//import net.minecraft.network.chat.ChatType;
import net.minecraft.network.chat.Component;
//import net.minecraft.network.chat.OutgoingChatMessage;
//import net.minecraft.network.chat.PlayerChatMessage;
//import net.minecraft.client.player.LocalPlayer;
import net.minecraftforge.fml.loading.FMLPaths;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Locale;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FTBQLangConvert {
    public static String langStr = "en_us";
    public static String prefixStr = "ftbquests";

    private static final Logger log = LoggerFactory.getLogger(BackPortUtils.class);

    public FTBQLangConvert(CommandDispatcher<CommandSourceStack> dispatcher) {


        dispatcher.register(Commands.literal("ftblang")
                        .then(Commands.argument("lang", StringArgumentType.word())
                        .then(Commands.argument("prefix", StringArgumentType.word())

//                .requires(s->s.getServer() != null && s.getServer().isSingleplayer() || s.hasPermission(2))
                                        .executes(ctx ->{
                                            try{
                                                
                                                ServerQuestFile serverQuestFile = ServerQuestFile.INSTANCE;
                                                serverQuestFile.markDirty();
                                                serverQuestFile.saveNow();
                                                File parent = new File(Constants.PackMCMeta.GAMEDIR, Constants.PackMCMeta.OUTPUTFOLDER);
                                                File questsFolder = new File(Constants.PackMCMeta.GAMEDIR, "config\\" + Constants.PackMCMeta.QUESTFOLDER);
                                                File overridesFolder = new File(Constants.PackMCMeta.GAMEDIR, Constants.PackMCMeta.OVERRIDESFOLDER);

                                                langStr = ctx.getArgument("lang", String.class);
                                                prefixStr = ctx.getArgument("prefix", String.class);
                                                BackPortUtils.backport();
                                                
                                                ConvertQuestsInFolder(questsFolder, langStr, prefixStr, "config");

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

        ));

    }
    private void saveLang(String lang, File parent) throws IOException
    {
        File fe = new File(parent, lang.toLowerCase(Locale.ROOT) + ".json");
        FileUtils.write(fe, FTBQuestPrecisionLocalizerMod.gson.toJson(HandlerHelper.transKeys), StandardCharsets.UTF_8);
        PackUtils.createResourcePack(fe, FMLPaths.GAMEDIR.get().toFile()+"\\FTBLang\\FTB-Quests-Localization-Resourcepack.zip");
    }

    private void ConvertQuestsInFolder(File folder, String lang, String prefix, String folder_name) throws IOException {
        Handler handler = new Handler(folder_name);
        File parent = new File(Constants.PackMCMeta.GAMEDIR, Constants.PackMCMeta.OUTPUTFOLDER);
        File kubejsOutput = new File(parent, Constants.PackMCMeta.KUBEJSFOLDER);
        File kubejsBackupFile = new File(parent, Constants.PackMCMeta.KUBEJSBACKUPFOLDER);
        //File mcKubeJsOut = new File(Constants.PackMCMeta.KUBEJSFOLDER);
        Boolean is_main_config = (folder_name == "config"); 

        if (kubejsOutput.exists()){
            FileUtils.copyDirectory(kubejsOutput, kubejsBackupFile);
        }

        if(folder.exists()){
            File backup = new File(parent, Constants.PackMCMeta.BACKUPFOLDER);
            FileUtils.copyDirectory(folder, backup);
        }
        HandlerHelper.setPrefix(prefix);
        BaseQuestFile questFile = FTBQuestsAPI.api().getQuestFile(false);
        log.info("Quest file for folder " + folder_name + " is " + questFile.getPath());
        handler.handleRewardTables(questFile.getRewardTables());
        questFile.forAllChapterGroups(handler::handleChapterGroup);
        HandlerHelper.setCounter(0);
        questFile.forAllChapters(chapter -> {
            handler.handleChapter(chapter);
            handler.handleQuests(chapter.getQuests());
        });

        File output = new File(parent, (is_main_config ? "config\\" : (Constants.PackMCMeta.OVERRIDESFOLDER + folder_name + "\\" + Constants.PackMCMeta.QUESTFOLDER)));
        //File output2 = new File(Constants.PackMCMeta.GAMEDIR, (is_main_config ? "config\\" : (folder.getPath() + "\\")) + Constants.PackMCMeta.QUESTFOLDER);
        questFile.writeDataFull(output.toPath());
        //questFile.writeDataFull(output2.toPath());
        ServerQuestFile.INSTANCE.load();
        saveLang(lang, kubejsOutput);
        //saveLang(lang, mcKubeJsOut);
        if(!langStr.equalsIgnoreCase("en_us")){
            saveLang("en_us", kubejsOutput);
        }
    }
}
