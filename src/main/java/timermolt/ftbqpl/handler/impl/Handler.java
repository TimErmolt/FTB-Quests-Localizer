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
import net.minecraft.network.chat.Component;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.TreeMap;
import java.util.regex.Pattern;

public class Handler implements FtbQHandler {
    private final TreeMap<String, String> transKeys = HandlerHelper.transKeys;
    private final JSONService handleJSON = new JSONService();

    /**
     * The folder in which Handler is working right now.
    */
    public String current_folder = "config";

    public Handler(String folder) {
        current_folder = folder;
    }

    @Override
    public void handleRewardTables(List<RewardTable> rewardTables) {
        rewardTables.forEach(rewardTable -> {
            String textKey = HandlerHelper.getPrefix() + ".reward_table." + RewardTable.getCodeString(rewardTable);
            textKey += AddOverrideNameIfNeeded(textKey, rewardTable.getRawTitle());
            transKeys.put(textKey, rewardTable.getRawTitle());
            rewardTable.setRawTitle("{" + textKey + "}");
        });
    }

    @Override
    public void handleChapterGroup(ChapterGroup chapterGroup) {
        if(chapterGroup.getTitle() != null){
            if (!chapterGroup.getRawTitle().isEmpty()){
                String textKey = HandlerHelper.getPrefix() + ".chapter_group." + ChapterGroup.getCodeString(chapterGroup);
                textKey += AddOverrideNameIfNeeded(textKey, chapterGroup.getRawTitle());
                transKeys.put(textKey, chapterGroup.getRawTitle());
                chapterGroup.setRawTitle("{" + textKey + "}");
            }
        }
    }

    @Override
    public void handleChapter(Chapter chapter) {
        //HandlerHelper.setPrefix("ftbquests.chapter."+chapter.getFilename());
        String prefix = HandlerHelper.getPrefix() + ".chapter." + Chapter.getCodeString(chapter);
        if(chapter.getTitle() != null){
            String title_prefix = prefix + AddOverrideNameIfNeeded(prefix + ".title", chapter.getRawTitle());
            transKeys.put(title_prefix + ".title", chapter.getRawTitle());
            chapter.setRawTitle("{" + title_prefix + ".title" + "}");
        }
        if(!chapter.getRawSubtitle().isEmpty()){
            String subtitle_prefix = prefix + AddOverrideNameIfNeeded(prefix + ".subtitle", String.join("\n", chapter.getRawSubtitle()));
            transKeys.put(subtitle_prefix + ".subtitle", String.join("\n", chapter.getRawSubtitle()));
            try {
                Field rawSubtitle = chapter.getClass().getDeclaredField("rawSubtitle");
                rawSubtitle.setAccessible(true);
                List<String> subTitleList = new ArrayList<>();
                subTitleList.add("{" + prefix + ".subtitle" + "}");
                rawSubtitle.set(chapter,subTitleList);
            } catch (NoSuchFieldException | IllegalAccessException e) {
                throw new RuntimeException(e);
            }
        }
    }

    private void handleTasks(List<Task> tasks) {
        tasks.stream().filter(task -> !task.getRawTitle().isEmpty()).forEach(task -> {
            String textKey = HandlerHelper.getPrefix() + ".task." + Task.getCodeString(task);
            textKey += AddOverrideNameIfNeeded(textKey, task.getRawTitle());
            transKeys.put(textKey, task.getRawTitle());
            task.setRawTitle("{" + textKey + "}");
        });
        HandlerHelper.setCounter(0);
    }
    private void handleRewards(List<Reward> rewards) {
        rewards.stream().filter(reward -> !reward.getRawTitle().isEmpty()).forEach(reward -> {
            String textKey = HandlerHelper.getPrefix() + ".reward." + Reward.getCodeString(reward);
            textKey += AddOverrideNameIfNeeded(textKey, reward.getRawTitle());
            transKeys.put(textKey, reward.getRawTitle());
            reward.setRawTitle("{" + textKey + "}");
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
                    String title_prefix = prefix + AddOverrideNameIfNeeded(prefix + ".title", quest.getRawTitle());
                    transKeys.put(title_prefix + ".title", quest.getRawTitle());
                    quest.setRawTitle("{" + title_prefix + ".title" + "}");
                }
            }
            if(!quest.getRawSubtitle().isEmpty()){
                String subtitle_prefix = prefix + AddOverrideNameIfNeeded(prefix + ".subtitle", quest.getRawSubtitle());
                transKeys.put(subtitle_prefix + ".subtitle", quest.getRawSubtitle());
                quest.setRawSubtitle("{" + subtitle_prefix + ".subtitle" + "}");
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
                handleJSON.handleJSON(parsedText, prefix);
            }
            else {
                HandlerHelper.addDescription();
                String textKey = prefix + ".description" + HandlerHelper.getDescription();
                textKey += AddOverrideNameIfNeeded(textKey, desc);
                transKeys.put(textKey, desc);
                HandlerHelper.descList.add("{" + textKey + "}");
            }
        });
        HandlerHelper.setDescription(0);
        HandlerHelper.setImage(0);
    }

    private void handleDescriptionImage(String desc, String prefix) {
        String imgKey = prefix + ".image" + HandlerHelper.getImage();
        imgKey += AddOverrideNameIfNeeded(imgKey, desc);
        transKeys.put(imgKey, desc);
        HandlerHelper.descList.add("{" + imgKey + "}");
        HandlerHelper.addImage();
    }

    /**
     * Compares the given element at given id to the main quests config.
     * `TRUE` if elements matches;
     * `FALSE` otherwise
    */
    private Boolean SameInMainConfig(String id, String element) {
        if(element.isEmpty() || element.equals(transKeys.get(id))) {
            return true;
        }
        return false;
    }
    /**
     * Returns `current_folder` if the given element at given id is not the same in main quests config and Handler is currently not working in it.
    */
    private String AddOverrideNameIfNeeded(String id, String element) {
        if(current_folder != "config" && !SameInMainConfig(id, element)) {
            return "." + current_folder;
        }
        return "";
    }
}