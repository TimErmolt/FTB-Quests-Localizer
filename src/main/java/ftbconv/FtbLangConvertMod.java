package ftbconv;

import com.google.gson.*;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.tree.ArgumentCommandNode;
import com.mojang.brigadier.tree.LiteralCommandNode;
import com.mojang.brigadier.tree.RootCommandNode;
import dev.ftb.mods.ftbquests.FTBQuests;
import dev.ftb.mods.ftbquests.quest.*;
import dev.ftb.mods.ftbquests.quest.loot.RewardTable;
import dev.ftb.mods.ftbquests.quest.reward.Reward;
import dev.ftb.mods.ftbquests.quest.task.Task;
import dev.ftb.mods.ftbquests.util.TextUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.language.LanguageInfo;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.network.chat.*;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.loading.FMLPaths;
import org.apache.commons.compress.utils.Lists;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Locale;
import java.util.StringJoiner;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static ftbconv.Constants.PackMCMeta.*;
import static net.minecraft.commands.Commands.literal;

@Mod("ftbquestlocalization")
public class FtbLangConvertMod{
	private Handler handler;
	private int chapters;
	private int quests;
	private static final Logger log = LoggerFactory.getLogger(FtbLangConvertMod.class);

	public FtbLangConvertMod(){
		this.handler = new Handler();
		MinecraftForge.EVENT_BUS.register(this);
	}

	public static final Gson gson = new GsonBuilder().setPrettyPrinting().create();

	@SubscribeEvent
	public void serverRegisterCommandsEvent(RegisterCommandsEvent event){
		CommandDispatcher<CommandSourceStack> commandDispatcher = event.getDispatcher();

		RootCommandNode<CommandSourceStack> rootCommandNode = commandDispatcher.getRoot();
		LiteralCommandNode<CommandSourceStack> commandNode = literal("ftblang").executes(context -> {
			return 0;
		}).build();

		ArgumentCommandNode<CommandSourceStack, String> argumentCommandNode = Commands.argument("lang", StringArgumentType.word()).suggests((C1, c2) -> {
			return SharedSuggestionProvider.suggest(Minecraft.getInstance().getLanguageManager().getLanguages().stream().map(LanguageInfo::getCode).toList().toArray(new String[0]), c2);
		}).executes(Ctx -> {
			try{

				// File Prep
				File parent = new File(FMLPaths.GAMEDIR.get().toFile(), OUTPUTFOLDER);
				File kubeJSOutput = new File(parent, KUBEJSFOLDER);
				File questsFolder = new File(FMLPaths.GAMEDIR.get().toFile(), QUESTFOLDER);
				if(questsFolder.exists()){
					File backup = new File(parent, BACKUPFOLDER);
					FileUtils.copyDirectory(questsFolder, backup);
				}


				QuestFile file = FTBQuests.PROXY.getQuestFile(false);
				handler.handleRewardTables(file.rewardTables);
				handler.handleChapterGroups(file.chapterGroups);
				handler.handleChapters(file.getAllChapters());


				/*
				for(int i = 0; i < file.getAllChapters().size(); i++){

					Chapter chapter = file.getAllChapters().get(chapters);
					chapters++;

					String prefix = "chapter." + (chapters);
					// deprecated.
					if(!chapter.title.isBlank()){
						transKeys.put(prefix + ".title", chapter.title);
						chapter.title = "{" + prefix + ".title" + "}";
					}
					// deprecated.
					if(chapter.subtitle.size() > 0){
						transKeys.put(prefix + ".subtitle", String.join("\n", chapter.subtitle));
						chapter.subtitle.clear();
						chapter.subtitle.add("{" + prefix + ".subtitle" + "}");
					}

					// deprecated.
					for(int i1 = 0; i1 < chapter.images.size(); i1++){
						ChapterImage chapterImage = chapter.images.get(i1);

						if(!chapterImage.hover.isEmpty()){
							transKeys.put(prefix + ".image." + (i1+1), String.join("\n", chapterImage.hover));
							chapterImage.hover.clear();
							chapterImage.hover.add("{" + prefix + ".image." + (i1+1) + "}");
						}
					}

					quests = 0;
					for(int i1 = 0; i1 < chapter.getQuests().size(); i1++){
						Quest quest = chapter.getQuests().get(quests);

						quests++;
						handler.handleTasks(quest.tasks);
						handler.handleRewards(quest.rewards);
						if(!quest.title.isBlank()){
							transKeys.put(prefix + ".quest." + (quests) + ".title", quest.title);
							quest.title = "{" + prefix + ".quest." + (quests) + ".title}";
						}
						if(!quest.subtitle.isBlank()){
							transKeys.put(prefix + ".quest." + (quests) + ".subtitle", quest.subtitle);
							quest.subtitle = "{" + prefix + ".quest." + (quests) + ".subtitle" + "}";
						}

						if(quest.description.size() > 0){
							List<String> descList = Lists.newArrayList();

							StringJoiner joiner = new StringJoiner("\n");
							int num = 1;

							// Quest Description
							for(int i2 = 0; i2 < quest.description.size(); i2++){
								String desc = quest.description.get(i2);
								Component parsedText = TextUtils.parseRawText(desc);

								final String regex = "\\{image:.*?}";

								// Image Handler
								if(desc.contains("{image:")){
									if(!joiner.toString().isBlank()){
										transKeys.put(prefix + ".quest." + (quests) + ".description." + num, joiner.toString());
										descList.add("{" + prefix + ".quest." + (quests) + ".description." + num + "}");
										joiner = new StringJoiner("\n");
										num++;
									}

									final Pattern pattern = Pattern.compile(regex, Pattern.MULTILINE);
									final Matcher matcher = pattern.matcher(desc);

									while (matcher.find()) {
										desc = desc.replace(matcher.group(0), "");
										descList.add(matcher.group(0));
									}
								}
								// Pagebreak Handler
								else if(desc.contains("{@pagebreak}")){
									descList.add(desc);
								}
								// JSON String Handler
								else if (desc.startsWith("[\"") && desc.endsWith("\"]")){
									try{
										log.info("Starting with a JSON at");
										log.info(desc);
										String jsonString = "[\"\",";
										List<Component> flatList = parsedText.toFlatList();
										int styleInt = 1;
										for(Component c : flatList){
//											String text = c.toString().substring(8, c.toString().length() - 1);
											String text = c.getContents().toString().substring(8, c.getContents().toString().length() -1);
											Style style = c.getStyle();

											if(style != Style.EMPTY){
												log.info("Description Part has Style()");
												log.info(text);
												jsonString += "{";
												TextColor color = style.getColor();
												if(color != null){
													jsonString += "\"color\":\"" + color.toString() + "\",";
												}

												joiner.add(text);
												String textKey = prefix + ".quest." + (quests) + ".description." + num + ".style." + styleInt;
												transKeys.put(textKey, joiner.toString());
												joiner = new StringJoiner("\n");
												jsonString += "\"translate\":\"" + textKey + "\",";
												ClickEvent clickEvent = style.getClickEvent();
												if(clickEvent != null){
													String clickEventValue = clickEvent.getValue();
													String clickEventAction = clickEvent.getAction().getName();
													log.info("clickEvent " + clickEvent.getValue());
													jsonString += "\"clickEvent\":{\"action\":\"" + clickEventAction + "\",\"value\":\"" + clickEventValue + "\"},";
												}
												HoverEvent hoverEvent = style.getHoverEvent();
												if(hoverEvent != null){
													log.info("Also has hoverEvent");
													String hoverEventAction = hoverEvent.getAction().getName();
													JsonObject hoverEventJSON = hoverEvent.serialize();
													JsonObject hoverValue = hoverEventJSON.get("contents").getAsJsonObject();
//													JsonArray additionalText = hoverValue.getAsJsonArray("extra");

													String hoverText = hoverValue.get("text").getAsString();
													log.info(hoverText);
													joiner.add(hoverText);
													textKey = prefix + ".quest." + (quests) + ".description" + ".style." + styleInt + ".hover.text." + num;
													String hoverString = "\"hoverEvent\":{\"action\":\"" + hoverEventAction + "\",\"contents\":{\"translate\":\"" + textKey +"\"";
													transKeys.put(textKey, joiner.toString());
													joiner = new StringJoiner("\n");



													hoverString += "}}},";
													jsonString += hoverString;

												}
												styleInt++;
											}
											else{
												joiner.add(text);
												String textKey = prefix + ".quest." + (quests) + ".description." + num;
												transKeys.put(textKey, joiner.toString());
												joiner = new StringJoiner("\n");
												jsonString += "{\"translate\":\"" + textKey + "\"},";
												num++;
												log.info(textKey);
											}

										}
										jsonString = jsonString.substring(0, jsonString.length()-1);
										jsonString += "]";
										descList.add(jsonString);
									}catch(Exception e){
										log.info(e.getMessage());
									}
								}
								// Normal Handler
								else{
									if(desc.isBlank()){
										descList.add("");
									}else{
										joiner.add(desc);
										String textKey = prefix + ".quest." + (quests) + ".description." + num;
										transKeys.put(textKey, joiner.toString());
										joiner = new StringJoiner("\n");
										descList.add("{"+textKey+"}");
										num++;
									}
								}
							}

							if(!joiner.toString().isBlank()){
								transKeys.put(prefix + ".quest." + (quests) + ".description." + num, joiner.toString());
								descList.add("{" + prefix + ".quest." + (quests) + ".description." + num + "}");
							}

							quest.description.clear();
							quest.description.addAll(descList);
						}


					}
				}
				*/

				File output = new File(parent, QUESTFOLDER);
				file.writeDataFull(output.toPath());

				String lang = Ctx.getArgument("lang", String.class);
				saveLang(handler.getTransKeys(), lang, kubeJSOutput);

				Ctx.getSource().getPlayerOrException().displayClientMessage(Component.literal("FTB quests files exported to: " + parent.getAbsolutePath()), true);

			}catch(Exception e){
				e.printStackTrace();
			}

			return 1;
		}).build();

		rootCommandNode.addChild(commandNode);
		commandNode.addChild(argumentCommandNode);
	}

	private void saveLang(TreeMap<String, String> transKeys, String lang, File parent) throws IOException{
		log.info("Saving Language File");
		File fe = new File(parent, lang.toLowerCase(Locale.ROOT) + ".json");
		FileUtils.write(fe, FtbLangConvertMod.gson.toJson(transKeys), StandardCharsets.UTF_8);
		PackUtils.createResourcePack(fe, FMLPaths.GAMEDIR.get().toFile()+"\\FTBLang\\FTB Quests Localization Keys.zip");
	}
}

