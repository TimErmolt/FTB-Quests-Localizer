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
import timermolt.ftbqpl.utils.HandlerCounter;
import net.minecraft.network.chat.Component;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.TreeMap;
import java.util.regex.Pattern;

public class Handler implements FtbQHandler {
    private final TreeMap<String, String> transKeys = HandlerCounter.transKeys;
    private final JSONService handleJSON = new JSONService();

    @Override
    public void handleRewardTables(List<RewardTable> rewardTables) {
        rewardTables.forEach(rewardTable -> {
            HandlerCounter.addCounter();
            transKeys.put("ftbquests.loot_table.title"+HandlerCounter.getCounter(), rewardTable.getRawTitle());
            rewardTable.setRawTitle("{" + "ftbquests.loot_table.title" + HandlerCounter.getCounter() + "}");
        });
        HandlerCounter.setCounter(0);
    }

    @Override
    public void handleChapterGroup(ChapterGroup chapterGroup) {
        if(chapterGroup.getTitle() != null){
            if (!chapterGroup.getRawTitle().isEmpty()){
                transKeys.put("ftbquests.chapter_groups.title" + HandlerCounter.getCounter(), chapterGroup.getRawTitle());
                chapterGroup.setRawTitle("{" + "ftbquests.chapter_groups.title" + HandlerCounter.getCounter() + "}");
                HandlerCounter.addCounter();
            }
        }
    }

    @Override
    public void handleChapter(Chapter chapter) {
        HandlerCounter.setPrefix("ftbquests.chapter."+chapter.getFilename());
        String prefix = HandlerCounter.getPrefix();
        if(chapter.getTitle() != null){
            transKeys.put(prefix + ".title", chapter.getRawTitle());
            chapter.setRawTitle("{" + prefix + ".title" + "}");
        }
        if(!chapter.getRawSubtitle().isEmpty()){
            transKeys.put(prefix + ".subtitle", String.join("\n", chapter.getRawSubtitle()));
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
            HandlerCounter.addCounter();
            String textKey = HandlerCounter.getPrefix() + ".task.title" + HandlerCounter.getCounter();
            transKeys.put(textKey, task.getRawTitle());
            task.setRawTitle("{"+textKey+"}");
        });
        HandlerCounter.setCounter(0);
    }
    private void handleRewards(List<Reward> rewards) {
        rewards.stream().filter(reward -> !reward.getRawTitle().isEmpty()).forEach(reward -> {
            HandlerCounter.addCounter();
            String textKey = HandlerCounter.getPrefix() + ".reward.title" + HandlerCounter.getCounter();
            transKeys.put(textKey, reward.getRawTitle());
            reward.setRawTitle("{"+textKey+"}");
        });
        HandlerCounter.setCounter(0);
    }

    @Override
    public void handleQuests(List<Quest> allQuests) {
        allQuests.forEach(quest ->{
            HandlerCounter.addQuests();
            HandlerCounter.setPrefix("ftbquests.chapter." + quest.getChapter().getFilename() + ".quest" + HandlerCounter.getQuests());
            String prefix = HandlerCounter.getPrefix();
            if(quest.getTitle() != null){
                if (!quest.getRawTitle().isEmpty()){
                    transKeys.put(prefix + ".title", quest.getRawTitle());
                    quest.setRawTitle("{" + prefix + ".title" + "}");
                }
            }
            if(!quest.getRawSubtitle().isEmpty()){
                transKeys.put(prefix + ".subtitle", quest.getRawSubtitle());
                quest.setRawSubtitle("{" + prefix + ".subtitle" + "}");
            }
            handleTasks(quest.getTasksAsList());
            handleRewards(quest.getRewards().stream().toList());
            handleQuestDescriptions(quest.getRawDescription());

            quest.getRawDescription().clear();
            quest.getRawDescription().addAll(HandlerCounter.descList);
            HandlerCounter.descList.clear();
        });
        HandlerCounter.setQuests(0);
    }

    private void handleQuestDescriptions(List<String> descriptions) {
        String rich_desc_regex = "\\s*[\\[\\{].*\"+.*[\\]\\}]\\s*";
        Pattern rich_desc_pattern = Pattern.compile(rich_desc_regex);
        descriptions.forEach(desc -> {

            if (desc.isBlank()) {
                HandlerCounter.descList.add("");
            }
            else if (desc.contains("{image:")){
                handleDescriptionImage(desc);
            }
            else if(desc.contains("{@pagebreak}")){
                HandlerCounter.descList.add(desc);
            }
            else if(rich_desc_pattern.matcher(desc).find()){
                HandlerCounter.addDescription();
                Component parsedText = TextUtils.parseRawText(desc);
                handleJSON.handleJSON(parsedText);
            }
            else {
                HandlerCounter.addDescription();
                String textKey = HandlerCounter.getPrefix() + ".description" + HandlerCounter.getDescription();
                transKeys.put(textKey, desc);
                HandlerCounter.descList.add("{"+textKey+"}");
            }
        });
        HandlerCounter.setDescription(0);
        HandlerCounter.setImage(0);
    }
    private void handleDescriptionImage(String desc){
        String imgKey = HandlerCounter.getPrefix() + ".image" + HandlerCounter.getImage();
        transKeys.put(imgKey, desc);
        HandlerCounter.descList.add("{" + imgKey + "}");
        HandlerCounter.addImage();
    }
}
