package timermolt.ftbqpl.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import dev.ftb.mods.ftbquests.api.FTBQuestsAPI;
import dev.ftb.mods.ftbquests.quest.*;
import timermolt.ftbqpl.FTBQuestPrecisionLocalizerMod;
import timermolt.ftbqpl.handler.impl.Handler;
import timermolt.ftbqpl.utils.BackPortUtils;
import timermolt.ftbqpl.utils.Constants;
import timermolt.ftbqpl.utils.HandlerCounter;
import timermolt.ftbqpl.utils.PackUtils;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraftforge.fml.loading.FMLPaths;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Locale;

public class FTBQLangConvert {
    public static String langStr = "en_us";

    public FTBQLangConvert(CommandDispatcher<CommandSourceStack> dispatcher) {


        dispatcher.register(Commands.literal("ftblang")
                        .then(Commands.argument("lang", StringArgumentType.word())

//                .requires(s->s.getServer() != null && s.getServer().isSingleplayer() || s.hasPermission(2))
                                        .executes(ctx ->{
                                            try{
                                                File output2 = new File(Constants.PackMCMeta.GAMEDIR, Constants.PackMCMeta.QUESTFOLDER);
                                                ServerQuestFile serverQuestFile = ServerQuestFile.INSTANCE;
                                                serverQuestFile.markDirty();
                                                serverQuestFile.saveNow();
                                                Handler handler = new Handler();
                                                File parent = new File(Constants.PackMCMeta.GAMEDIR, Constants.PackMCMeta.OUTPUTFOLDER);
                                                File kubejsOutput = new File(parent, Constants.PackMCMeta.KUBEJSFOLDER);
                                                File kubejsBackupFile = new File(parent,Constants.PackMCMeta.KUBEJSBACKUPFOLDER);
                                                File mcKubeJsOut = new File(Constants.PackMCMeta.KUBEJSFOLDER);
                                                File questsFolder = new File(Constants.PackMCMeta.GAMEDIR, Constants.PackMCMeta.QUESTFOLDER);
                                                if (kubejsOutput.exists()){
                                                    FileUtils.copyDirectory(kubejsOutput,kubejsBackupFile);
                                                }
                                                langStr = ctx.getArgument("lang", String.class);
                                                BackPortUtils.backport();
                                                if(questsFolder.exists()){
                                                    File backup = new File(parent, Constants.PackMCMeta.BACKUPFOLDER);
                                                    FileUtils.copyDirectory(questsFolder, backup);
                                                }
                                                BaseQuestFile questFile = FTBQuestsAPI.api().getQuestFile(false);
                                                handler.handleRewardTables(questFile.getRewardTables());
                                                questFile.forAllChapterGroups(handler::handleChapterGroup);
                                                HandlerCounter.setCounter(0);
                                                questFile.forAllChapters(chapter -> {
                                                    handler.handleChapter(chapter);
                                                    handler.handleQuests(chapter.getQuests());
                                                    HandlerCounter.addChapters();
                                                });

                                                File output = new File(parent, Constants.PackMCMeta.QUESTFOLDER);
                                                questFile.writeDataFull(output.toPath());
                                                questFile.writeDataFull(output2.toPath());
                                                ServerQuestFile.INSTANCE.load();
                                                saveLang(langStr, kubejsOutput);
                                                saveLang(langStr, mcKubeJsOut);
                                                if(!langStr.equalsIgnoreCase("en_us")){
                                                    saveLang("en_us", kubejsOutput);
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
    private void saveLang(String lang, File parent) throws IOException
    {
        File fe = new File(parent, lang.toLowerCase(Locale.ROOT) + ".json");
        FileUtils.write(fe, FTBQuestPrecisionLocalizerMod.gson.toJson(HandlerCounter.transKeys), StandardCharsets.UTF_8);
        PackUtils.createResourcePack(fe, FMLPaths.GAMEDIR.get().toFile()+"\\FTBLang\\FTB-Quests-Localization-Resourcepack.zip");
    }
}
