package timermolt.ftbqpl.handler.impl;

import dev.ftb.mods.ftbquests.quest.Chapter;
import dev.ftb.mods.ftbquests.quest.ChapterGroup;
import dev.ftb.mods.ftbquests.quest.Quest;
import dev.ftb.mods.ftbquests.quest.loot.RewardTable;
import dev.ftb.mods.ftbquests.quest.reward.Reward;
import dev.ftb.mods.ftbquests.quest.task.Task;
import dev.ftb.mods.ftbquests.util.TextUtils;
import timermolt.ftbqpl.handler.FtbQHandler;
import timermolt.ftbqpl.service.impl.JSONService;
import timermolt.ftbqpl.utils.HandlerHelper;
import timermolt.ftbqpl.utils.BackPortUtils;
import timermolt.ftbqpl.utils.Constants;
import net.minecraft.network.chat.Component;

import java.io.BufferedReader;
import java.io.FileReader;
//import java.io.FileReader;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.TreeMap;
import java.util.regex.Pattern;
import java.util.HashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.*;

public class Handler implements FtbQHandler {
    private final TreeMap<String, String> transKeys = HandlerHelper.transKeys;
    private final JSONService handleJSON = new JSONService();
    private static final Logger log = LoggerFactory.getLogger(BackPortUtils.class);

    /**
     * The folder in which Handler is working right now.
    */
    public String current_mode = "normal";
    private Gson gson = new Gson();
    private HashMap<String, String> main_lang_file;

    public Handler(String mode, String lang) {
        current_mode = mode;
        if(current_mode.equals("normal")) {
            return;
        }

        try {
            BufferedReader reader = new BufferedReader(new FileReader(Constants.PackMCMeta.NORMALMODEFOLDER + lang + ".json"));

            main_lang_file = gson.fromJson(reader, HashMap.class);
        }
        catch(Exception e) {
            log.info("Exception when reading json: " + e);
        }
    }

    @Override
    public void handleRewardTables(List<RewardTable> rewardTables) {
        rewardTables.forEach(rewardTable -> {
            String textKey = HandlerHelper.getPrefix() + ".reward_table." + RewardTable.getCodeString(rewardTable);
            if(ShouldAddLangKey(textKey, rewardTable.getRawTitle())) {
                textKey += AddOverrideName();
                transKeys.put(textKey, rewardTable.getRawTitle());
                rewardTable.setRawTitle("{" + textKey + "}");
            }
            else {
                rewardTable.setRawTitle("{" + textKey + "}");
            }
        });
    }

    @Override
    public void handleChapterGroup(ChapterGroup chapterGroup) {
        if(chapterGroup.getTitle() != null){
            if (!chapterGroup.getRawTitle().isEmpty()){
                String textKey = HandlerHelper.getPrefix() + ".chapter_group." + ChapterGroup.getCodeString(chapterGroup);
                if(ShouldAddLangKey(textKey, chapterGroup.getRawTitle())) {
                    textKey += AddOverrideName();
                    transKeys.put(textKey, chapterGroup.getRawTitle());
                    chapterGroup.setRawTitle("{" + textKey + "}");
                }
                else {
                    chapterGroup.setRawTitle("{" + textKey + "}");
                }
            }
        }
    }

    @Override
    public void handleChapter(Chapter chapter) {
        //HandlerHelper.setPrefix("ftbquests.chapter."+chapter.getFilename());
        String prefix = HandlerHelper.getPrefix() + ".chapter." + Chapter.getCodeString(chapter);
        if(chapter.getTitle() != null){
            if(ShouldAddLangKey(prefix + ".title", chapter.getRawTitle())) {
                String title_prefix = prefix + AddOverrideName();
                transKeys.put(title_prefix + ".title", chapter.getRawTitle());
                chapter.setRawTitle("{" + title_prefix + ".title" + "}");
            }
            else {
                chapter.setRawTitle("{" + prefix + ".title" + "}");
            }
        }
        if(!chapter.getRawSubtitle().isEmpty()){
            if(ShouldAddLangKey(prefix + ".subtitle", String.join("\n", chapter.getRawSubtitle()))) {
                String subtitle_prefix = prefix + AddOverrideName();
                transKeys.put(subtitle_prefix + ".subtitle", String.join("\n", chapter.getRawSubtitle()));
            }
            try {
                Field rawSubtitle = chapter.getClass().getDeclaredField("rawSubtitle");
                rawSubtitle.setAccessible(true);
                List<String> subTitleList = new ArrayList<>();
                subTitleList.add("{" + prefix + ".subtitle" + "}");
                rawSubtitle.set(chapter, subTitleList);
            } catch (NoSuchFieldException | IllegalAccessException e) {
                throw new RuntimeException(e);
            }
        }
    }

    private void handleTasks(List<Task> tasks) {
        tasks.stream().filter(task -> !task.getRawTitle().isEmpty()).forEach(task -> {
            String textKey = HandlerHelper.getPrefix() + ".task." + Task.getCodeString(task);
            if(ShouldAddLangKey(textKey, task.getRawTitle())) {
                textKey += AddOverrideName();
                transKeys.put(textKey, task.getRawTitle());
                task.setRawTitle("{" + textKey + "}");
            }
            else {
                task.setRawTitle("{" + textKey + "}");
            }
        });
        HandlerHelper.setCounter(0);
    }
    private void handleRewards(List<Reward> rewards) {
        rewards.stream().filter(reward -> !reward.getRawTitle().isEmpty()).forEach(reward -> {
            String textKey = HandlerHelper.getPrefix() + ".reward." + Reward.getCodeString(reward);
            if(ShouldAddLangKey(textKey, reward.getRawTitle())) {
                textKey += AddOverrideName();
                transKeys.put(textKey, reward.getRawTitle());
                reward.setRawTitle("{" + textKey + "}");
            }
            else {
                reward.setRawTitle("{" + textKey + "}");
            }
        });
        HandlerHelper.setCounter(0);
    }

    @Override
    public void handleQuests(List<Quest> allQuests) {
        allQuests.forEach(quest ->{
            //HandlerHelper.setPrefix("ftbquests.chapter." + quest.getChapter().getFilename() + ".quest" + HandlerHelper.getQuests());
            String prefix = HandlerHelper.getPrefix() + ".quest." + Quest.getCodeString(quest);
            if(quest.getTitle() != null){
                if (!quest.getRawTitle().isEmpty()){
                    if(ShouldAddLangKey(prefix + ".title", quest.getRawTitle())) {
                        String title_prefix = prefix + ".title" + AddOverrideName();
                        transKeys.put(title_prefix, quest.getRawTitle());
                        quest.setRawTitle("{" + title_prefix + "}");
                    }
                    else {
                        quest.setRawTitle("{" + prefix + ".title" + "}");
                    }
                }
            }
            if(!quest.getRawSubtitle().isEmpty()){
                if(ShouldAddLangKey(prefix + ".subtitle", quest.getRawSubtitle())) {
                    String subtitle_prefix = prefix + ".subtitle" + AddOverrideName();
                    transKeys.put(subtitle_prefix, quest.getRawSubtitle());
                    quest.setRawSubtitle("{" + subtitle_prefix + "}");
                }
                else {
                    quest.setRawSubtitle("{" + prefix + ".subtitle" + "}");
                }
            }
            handleTasks(quest.getTasksAsList());
            handleRewards(quest.getRewards().stream().toList());
            handleQuestDescriptions(quest.getRawDescription(), prefix);

            quest.getRawDescription().clear();
            quest.getRawDescription().addAll(HandlerHelper.descList);
            HandlerHelper.descList.clear();
        });
    }

    private void handleQuestDescriptions(List<String> descriptions, String prefix) {
        String rich_desc_regex = "\\s*[\\[\\{].*\"+.*[\\]\\}]\\s*";
        Pattern rich_desc_pattern = Pattern.compile(rich_desc_regex);

        descriptions.forEach(desc -> {

            if (desc.isBlank()) {
                HandlerHelper.descList.add("");
            }
            else if (desc.contains("{image:")){
                handleDescriptionImage(desc, prefix);
            }
            else if(desc.contains("{@pagebreak}")){
                HandlerHelper.descList.add(desc);
            }
            else if(rich_desc_pattern.matcher(desc).find()){
                HandlerHelper.addDescription();
                Component parsedText = TextUtils.parseRawText(desc);
                handleJSON.handleJSON(parsedText, prefix, this);
            }
            else {
                HandlerHelper.addDescription();
                String textKey = prefix + ".description" + HandlerHelper.getDescription();
                if(ShouldAddLangKey(textKey, desc)) {
                    textKey += AddOverrideName();
                    transKeys.put(textKey, desc);
                    HandlerHelper.descList.add("{" + textKey + "}");
                }
                else {
                    HandlerHelper.descList.add("{" + textKey + "}");
                }
            }
        });
        HandlerHelper.setDescription(0);
        HandlerHelper.setImage(0);
    }

    private void handleDescriptionImage(String desc, String prefix) {
        String imgKey = prefix + ".image" + HandlerHelper.getImage();
        if(ShouldAddLangKey(imgKey, desc)) {
            imgKey += AddOverrideName();
            transKeys.put(imgKey, desc);
            HandlerHelper.descList.add("{" + imgKey + "}");
        }
        else {
            HandlerHelper.descList.add("{" + imgKey + "}");
        }
        HandlerHelper.addImage();
    }

    /**
     * Compares the given element at given id to the main quests config.
     * `TRUE` if elements matches;
     * `FALSE` otherwise
    */
    private Boolean SameInMainConfig(String id, String element) {
        if(element.isEmpty() || element.equals(main_lang_file.get(id))) {
            return true;
        }
        return false;
    }
    /**
     * Returns `current_mode` if Handler is not currently working on the main config file ("normal").
    */
    public String AddOverrideName() {
        if(current_mode.equals("normal")) {
            return "";
        }

        return "." + current_mode;
        /*
        if(!SameInMainConfig(id, element)) {
            return "." + current_mode;
        }
        return "";
        */
    }

    public Boolean ShouldAddLangKey(String id, String element) {
        if(current_mode.equals("normal")) {
            return true;
        }

        if(SameInMainConfig(id, element)) {
            return false;
        }

        return true;
    }
}