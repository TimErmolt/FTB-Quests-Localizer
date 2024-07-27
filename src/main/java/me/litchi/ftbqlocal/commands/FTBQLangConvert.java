package me.litchi.ftbqlocal.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import dev.ftb.mods.ftbquests.FTBQuests;
import dev.ftb.mods.ftbquests.FTBQuestsCommon;
import dev.ftb.mods.ftbquests.client.ClientQuestFile;
import dev.ftb.mods.ftbquests.client.FTBQuestsClient;
import dev.ftb.mods.ftbquests.client.FTBQuestsClientEventHandler;
import dev.ftb.mods.ftbquests.client.QuestFileCacheReloader;
import dev.ftb.mods.ftbquests.forge.FTBQuestsForge;
import dev.ftb.mods.ftbquests.quest.*;
import me.litchi.ftbqlocal.FtbQuestLocalizerMod;
import me.litchi.ftbqlocal.handler.impl.Handler;
import me.litchi.ftbqlocal.utils.Constants;
import me.litchi.ftbqlocal.utils.HandlerCounter;
import me.litchi.ftbqlocal.utils.PackUtils;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraftforge.fml.loading.FMLPaths;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.TreeMap;

public class FTBQLangConvert {

    public FTBQLangConvert(CommandDispatcher<CommandSourceStack> dispatcher) {

        dispatcher.register(Commands.literal("ftblang")
                        .then(Commands.argument("lang", StringArgumentType.word())

//                .requires(s->s.getServer() != null && s.getServer().isSingleplayer() || s.hasPermission(2))
                                        .executes(ctx ->{
                                            try{
                                                Handler handler = new Handler();
                                                // File Prep
                                                File parent = new File(Constants.PackMCMeta.GAMEDIR, Constants.PackMCMeta.OUTPUTFOLDER);
                                                File kubejsOutput = new File(parent, Constants.PackMCMeta.KUBEJSFOLDER);
                                                File questsFolder = new File(Constants.PackMCMeta.GAMEDIR, Constants.PackMCMeta.QUESTFOLDER);
                                                if(questsFolder.exists()){
                                                    File backup = new File(parent, Constants.PackMCMeta.BACKUPFOLDER);
                                                    FileUtils.copyDirectory(questsFolder, backup);
                                                }
                                                QuestFile questFile = FTBQuests.PROXY.getQuestFile(false);
                                                handler.handleRewardTables(questFile.rewardTables);
                                                List<ChapterGroup> chapterGroups = questFile.chapterGroups;
                                                chapterGroups.forEach(handler::handleChapterGroup);
                                                HandlerCounter.setCounter(0);
                                                List<Chapter> allChapters = questFile.getAllChapters();
                                                allChapters.forEach(chapter -> {
                                                    System.out.println(chapter.filename);
                                                    handler.handleChapter(chapter);
                                                    handler.handleQuests(chapter.getQuests());
                                                    HandlerCounter.addChapters();
                                                });

                                                File output = new File(parent, Constants.PackMCMeta.QUESTFOLDER);
                                                questFile.writeDataFull(output.toPath());

                                                String lang = ctx.getArgument("lang", String.class);
                                                saveLang(HandlerCounter.transKeys, lang, kubejsOutput);

                                                if(!lang.equalsIgnoreCase("en_us")){
                                                    saveLang(HandlerCounter.transKeys, "en_us", kubejsOutput);
                                                }

                                                ctx.getSource().getPlayerOrException().displayClientMessage(Component.literal("FTB quests files exported to: " + parent.getAbsolutePath()), true);

                                            }catch(Exception e){
                                                e.printStackTrace();
                                            }

                                            return 1;

                                        })
                        )

        );

    }
    private void saveLang(TreeMap<String, String> transKeys, String lang, File parent) throws IOException
    {
        File fe = new File(parent, lang.toLowerCase(Locale.ROOT) + ".json");
        FileUtils.write(fe, FtbQuestLocalizerMod.gson.toJson(transKeys), StandardCharsets.UTF_8);
        PackUtils.createResourcePack(fe, FMLPaths.GAMEDIR.get().toFile()+"\\FTBLang\\FTB-Quests-Localization-Resourcepack.zip");
    }
}
