/*
 *     Copyright (C) 2018  Hyperium <https://hyperium.cc/>
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU Lesser General Public License as published
 *     by the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU Lesser General Public License for more details.
 *
 *     You should have received a copy of the GNU Lesser General Public License
 *     along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

// Created by RDIL in November 2018.  
// Created in hope to limit toxicity.  

package cc.hyperium.integrations.betterchatfilter;
import cc.hyperium.mods.sk1ercommon.Multithreading;
import cc.hyperium.Hyperium;
import cc.hyperium.event.EventBus;
import cc.hyperium.handlers.handlers.HypixelDetector;
import cc.hyperium.config.Settings;
import cc.hyperium.event.ChatEvent;
import cc.hyperium.event.InvokeEvent;
import net.minecraft.client.Minecraft;
import cc.hyperium.utils.ChatColor;
import java.lang.*;
import java.lang.System;
import java.io.*;
import java.util.*;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.apache.commons.io.IOUtils;

/*
 *  Better chat filter.  
 *  For chat filtering.  
 */

public class BetterChatFilter {
  private List<String> badwords;
  
  private static final String BAD_WORDS_URL = "https://raw.githubusercontent.com/HyperiumClient/Hyperium-Repo/master/files/BadWords.txt";
  
  /*
   * Download file from Hyperium-Repo
   */
  public BetterChatFilter() {
    try {
      final String rawBadwords = IOUtils.toString(new URL(BAD_WORDS_URL), Charset.forName("UTF-8"));
      badWords = new ArrayList<>(Arrays.asList(rawBadwords.split("\n")));
    } catch (Exception e) {
      e.printStackTrace();
      // After failing to download file, make arraylist to make up for it.  
      ArrayList rawBadwords = new ArrayList<String>();
      rawBadwords.add("fuck");
      rawBadwords.add("shit"); 
      rawBadwords.add("damn");
      rawBadwords.add("bitch");
      badWords = new ArrayList<>(Arrays.asList(rawBadwords.split("\n")));
    }
  }
  
  public List<String> getBadwords() {
    return badwords;
  }
  
  @InvokeEvent
  public void onChat(ChatEvent e) { 
    String unformattedMessage = ChatColor.stripColor(e.getChat().getUnformattedText());
    if(getBadwords().stream().anyMatch(unformattedMessage::contains) && Settings.BETTER_CHAT_FILTER) {
      e.setCancelled(true);
    } 
  } 
} 
