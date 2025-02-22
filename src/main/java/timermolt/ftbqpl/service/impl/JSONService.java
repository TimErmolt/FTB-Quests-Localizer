package timermolt.ftbqpl.service.impl;

import com.google.gson.JsonObject;
//import com.mojang.serialization.JsonOps;
import timermolt.ftbqpl.service.FtbQService;
import timermolt.ftbqpl.utils.HandlerHelper;
import timermolt.ftbqpl.handler.impl.Handler;
//import net.minecraft.Util;
import net.minecraft.network.chat.*;

import java.util.List;

public class JSONService implements FtbQService {
    Handler current_handler;

    @Override
    public void handleJSON(Component parsedText, String prefix, Handler handler) {
        current_handler = handler;

        try {
            String jsonString;
            List<Component> flatList = parsedText.toFlatList();
            StringBuilder jsonStringBuilder = new StringBuilder("[\"\",");
            for(Component c : flatList){
                HandlerHelper.addCounter();
                String text = c.getContents().toString().substring(8, c.getContents().toString().length() -1);
                Style style = c.getStyle();

                if(style != Style.EMPTY) {
                    jsonStringBuilder.append("{");
                    TextColor color = style.getColor();
                    if(color != null) {
                        jsonStringBuilder.append("\"color\":\"").append(color).append("\",");
                    }
                    if (style.isUnderlined()) {
                        jsonStringBuilder.append("\"underlined\":"+ 1).append(",");
                    }
                    if (style.isStrikethrough()) {
                        jsonStringBuilder.append("\"strikethrough\":"+ 1).append(",");
                    }
                    if (style.isBold()) {
                        jsonStringBuilder.append("\"bold\":"+ 1).append(",");
                    }
                    if (style.isItalic()) {
                        jsonStringBuilder.append("\"italic\":"+ 1).append(",");
                    }
                    if (style.isObfuscated()) {
                        jsonStringBuilder.append("\"obfuscated\":"+ 1).append(",");
                    }

                    String textKey = prefix + ".rich_description" + HandlerHelper.getCounter();
                    if(current_handler.ShouldAddLangKey(textKey, text)) {
                        HandlerHelper.transKeys.put(textKey + current_handler.AddOverrideName(), text);
                    }

                    ClickEvent clickEvent = style.getClickEvent();
                    if(clickEvent != null){
                        jsonStringBuilder.append("\"translate\":\"").append(textKey).append("\",");
                        String clickEventValue = clickEvent.getValue();
                        String clickEventAction = clickEvent.getAction().getName();
                        jsonStringBuilder.append("\"clickEvent\":{\"action\":\"").append(clickEventAction).append("\",\"value\":\"").append(clickEventValue).append("\"}},");
                    }
                    else {
                        jsonStringBuilder.append("\"translate\":\"").append(textKey).append("\"},");
                    }
                    HoverEvent hoverEvent = style.getHoverEvent();
                    if(hoverEvent != null){
                        String hoverEventAction = hoverEvent.getAction().getName();
                        //TODO: Check if this is the correct way to get the JsonObject from the CODEC
                        JsonObject hoverEventJSON = hoverEvent.serialize();
                        //JsonObject hoverEventJSON = Util.getOrThrow(HoverEvent.CODEC.encodeStart(JsonOps.INSTANCE, hoverEvent), IllegalStateException::new).getAsJsonObject();
                        System.out.println(hoverEventJSON);
                        JsonObject hoverValue = hoverEventJSON.get("contents").getAsJsonObject();
                        String hoverText = hoverValue.get("text").getAsString();

                        textKey = prefix + ".rich_description.hover_text" + HandlerHelper.getCounter();
                        String hoverString;
                        if(current_handler.ShouldAddLangKey(textKey, hoverText)) {
                            textKey += current_handler.AddOverrideName();
                            hoverString = "\"hoverEvent\":{\"action\":\"" + hoverEventAction + "\",\"contents\":{\"translate\":\"" + textKey +"\"";
                            HandlerHelper.transKeys.put(textKey, hoverText);
                        }
                        else {
                            hoverString = "\"hoverEvent\":{\"action\":\"" + hoverEventAction + "\",\"contents\":{\"translate\":\"" + textKey +"\"";
                        }

                        hoverString += "}},";
                        jsonStringBuilder.append(hoverString);
                    }
                }
                else {
                    String textKey = prefix + ".rich_description" + HandlerHelper.getCounter();
                    if(current_handler.ShouldAddLangKey(textKey, text)) {
                        textKey += current_handler.AddOverrideName();
                        HandlerHelper.transKeys.put(textKey, text);
                        jsonStringBuilder.append("{\"translate\":\"").append(textKey).append("\"},");
                    }
                    else {
                        jsonStringBuilder.append("{\"translate\":\"").append(textKey).append("\"},");
                    }
                }
            }
            jsonString = jsonStringBuilder.toString();
            jsonString = jsonString.substring(0, jsonString.length()-1);
            jsonString += "]";
            HandlerHelper.descList.add(jsonString);
        }catch(Exception e){
            HandlerHelper.log.info(e.getMessage());
        }
    }
}
