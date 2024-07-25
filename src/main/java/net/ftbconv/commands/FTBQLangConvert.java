package net.ftbconv.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import dev.ftb.mods.ftbquests.api.FTBQuestsAPI;
import dev.ftb.mods.ftbquests.quest.BaseQuestFile;
import net.ftbconv.FtbLangConvertMod;
import net.ftbconv.utils.Handler;
import net.ftbconv.utils.PackUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.neoforged.fml.loading.FMLPaths;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Locale;
import java.util.TreeMap;

import static net.ftbconv.utils.Constants.PackMCMeta.*;

public class FTBQLangConvert {

    public FTBQLangConvert(CommandDispatcher<CommandSourceStack> dispatcher) {

        dispatcher.register(Commands.literal("ftblang")
                        .then(Commands.argument("lang", StringArgumentType.word())

//                .requires(s->s.getServer() != null && s.getServer().isSingleplayer() || s.hasPermission(2))
                                        .executes(ctx ->{
                                            try{
                                                Handler handler = new Handler();
                                                // File Prep
                                                File parent = new File(GAMEDIR, OUTPUTFOLDER);
                                                File kubejsOutput = new File(parent, KUBEJSFOLDER);
                                                File questsFolder = new File(GAMEDIR, QUESTFOLDER);
                                                if(questsFolder.exists()){
                                                    File backup = new File(parent, BACKUPFOLDER);
                                                    FileUtils.copyDirectory(questsFolder, backup);
                                                }

                                                BaseQuestFile questFile = FTBQuestsAPI.api().getQuestFile(false);

                                                handler.handleRewardTables(questFile.getRewardTables());
                                                //handler.setCounter(0);
                                                questFile.forAllChapterGroups(handler::handleChapterGroup);
                                                handler.setCounter(0);
                                                //handler.setCounter(0);
                                                questFile.forAllChapters(chapter -> {
                                                    handler.handleChapter(chapter);
                                                    handler.addChapters();
                                                });

                                                File output = new File(parent, QUESTFOLDER);
                                                questFile.writeDataFull(output.toPath());

                                                String lang = ctx.getArgument("lang", String.class);
                                                saveLang(handler.getTransKeys(), lang, kubejsOutput);

                                                if(!lang.equalsIgnoreCase("en_us")){
                                                    saveLang(handler.getTransKeys(), "en_us", kubejsOutput);
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
        FileUtils.write(fe, FtbLangConvertMod.gson.toJson(transKeys), StandardCharsets.UTF_8);
        PackUtils.createResourcePack(fe, FMLPaths.GAMEDIR.get().toFile()+"\\FTBLang\\FTB Quests Localization Keys.zip");
    }
    static List<Locale> getAllLocales() {
        return PackUtils.map(Minecraft.getInstance().getLanguageManager().getLanguages().entrySet(), entry ->
                new Locale(entry.getKey()));
    }
}
