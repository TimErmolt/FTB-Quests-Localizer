package me.litchi.ftbqlocal.utils;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import dev.ftb.mods.ftbquests.FTBQuests;
import dev.ftb.mods.ftbquests.quest.*;
import dev.ftb.mods.ftbquests.quest.loot.RewardTable;
import me.litchi.ftbqlocal.handler.FtbQHandler;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.ZipFile;

import static me.litchi.ftbqlocal.commands.FTBQLangConvert.langStr;

public class BackPortUtils implements FtbQHandler {
    private static final String KUBEJS_LANG_DIR = Constants.PackMCMeta.GAMEDIR+"\\FTBLang\\backup\\"+Constants.PackMCMeta.KUBEJSFOLDER+"\\"+langStr+".json";
    private static final String RESOURCE_LANG_DIR = Constants.PackMCMeta.GAMEDIR+"\\resourcepacks\\"+Constants.PackMCMeta.PACKNAME;
    private static final Logger log = LoggerFactory.getLogger(BackPortUtils.class);
    private static JsonObject enJSON = null;
    private static final List<String> descList = new ArrayList<>();
    private static final BackPortUtils backportU = new BackPortUtils();
    public static void backport(){
        try {
            QuestFile questFile = FTBQuests.PROXY.getQuestFile(false);
            File kubefile = new File(KUBEJS_LANG_DIR);
            String en_us;
            if (!kubefile.exists()){
                try (ZipFile zipFile = new ZipFile(RESOURCE_LANG_DIR)){
                    zipFile.stream().forEach(zipEntry -> {
                        if (!zipEntry.isDirectory()){
                            try {
                                byte[] bytes = zipFile.getInputStream(zipEntry).readAllBytes();
                                enJSON =JsonParser.parseString(new String(bytes)).getAsJsonObject();
                            } catch (IOException e) {
                                log.info("JsonFile error");
                            }
                        }
                    });
                }catch (Exception e){
                    log.info("ZIPFile error");
                }
            } else {
                en_us = FileUtils.readFileToString(kubefile, StandardCharsets.UTF_8);
                enJSON =JsonParser.parseString(en_us).getAsJsonObject();
            }
            if (enJSON == null){
                throw new IOException();
            }
            backportU.handleRewardTables(questFile.rewardTables);
            questFile.chapterGroups.forEach(backportU::handleChapterGroup);
            questFile.getAllChapters().forEach(chapter -> {
                backportU.handleChapter(chapter);
                backportU.handleQuests(chapter.getQuests());
            });
            File output = new File(Constants.PackMCMeta.GAMEDIR, Constants.PackMCMeta.QUESTFOLDER);
            questFile.writeDataFull(output.toPath());
            ServerQuestFile.INSTANCE.save();
            ServerQuestFile.INSTANCE.saveNow();
        } catch (IOException e) {
            log.info("This is first port!");
        }
    }

    @Override
    public void handleRewardTables(List<RewardTable> rewardTables) {
        rewardTables.forEach(rewardTable -> {
            try {
                rewardTable.title = enJSON.get(rewardTable.title.replaceAll("[{}]","")).getAsString();
            }catch (Exception e){
                log.info("RewardTables is null");
            }
        });
    }

    @Override
    public void handleChapterGroup(ChapterGroup chapterGroup) {
        try {
            chapterGroup.title = enJSON.get(chapterGroup.title.replaceAll("[{}]","")).getAsString();
        }catch (Exception e){
            log.info("ChapterGroup is null");
        }
    }

    @Override
    public void handleChapter(Chapter chapter) {
        try {
            chapter.title=enJSON.get(chapter.title.replaceAll("[{}]","")).getAsString();
        }catch (Exception e){
            log.info("Chapter is null");
        }
    }

    @Override
    public void handleQuests(List<Quest> allQuests) {
        allQuests.forEach(quest -> {
            try {
                try {
                    quest.title = enJSON.get(quest.title.replaceAll("[{}]","")).getAsString();
                }catch (Exception e){
                    log.info("questTitle is null");
                }
                try {
                    quest.subtitle = enJSON.get(quest.subtitle.replaceAll("[{}]","")).getAsString();
                }catch (Exception e){
                    log.info("questSubtitle is null");
                }
                quest.rewards
                        .stream()
                        .filter(reward -> !reward.title.isEmpty())
                        .forEach(reward -> reward.title = enJSON.get(reward.title.replaceAll("[{}]","")).getAsString());

                quest.tasks
                        .stream()
                        .filter(task -> !task.title.isEmpty())
                        .forEach(task -> task.title = enJSON.get(task.title.replaceAll("[{}]","")).getAsString());
                List<String> rawDescription = quest.description;
                handleQuestDescriptions(rawDescription);
                quest.description.clear();
                quest.description.addAll(descList);
                descList.clear();
            }catch (Exception e){
                log.info("quests is null");
            }
        });
    }
    private void handleQuestDescriptions(List<String> descriptions) {
        String rich_desc_regex = "\\s*[\\[{].*\"+.*[]}]\\s*";
        Pattern rich_desc_pattern = Pattern.compile(rich_desc_regex);
        descriptions.forEach(desc -> {
            try {
                if (desc.isBlank()) {
                    descList.add("");
                }
                else if(desc.contains("{@pagebreak}")){
                    descList.add(desc);
                }
                else if(rich_desc_pattern.matcher(desc).find()){
                    Pattern pattern = Pattern.compile("ftbquests\\.chapter\\.[a-zA-Z0-9_]+\\.quest\\d+\\.[a-zA-Z_]+description\\d");
                    Matcher matcher = pattern.matcher(desc);
                    while (matcher.find()){
                        desc = desc.replace(matcher.group(0),enJSON.get(matcher.group(0)).getAsString()).replace("translate","text");
                    }
                    descList.add(desc);
                } else if (desc.contains("ftbquests")){
                    String key = desc.replaceAll("[{}]","");
                    descList.add(enJSON.get(key).getAsString());
                } else {
                    descList.add(desc);
                }
            }catch (Exception e){
                log.info("rich_desc is null");
            }
        });
    }
}