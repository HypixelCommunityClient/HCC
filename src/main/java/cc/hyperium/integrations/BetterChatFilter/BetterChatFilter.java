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

package cc.hyperium.integrations.BetterChatFilter;
import cc.hyperium.mods.sk1ercommon.Multithreading;
import cc.hyperium.Hyperium;
import cc.hyperium.config.Settings;
import cc.hyperium.event.ChatEvent;
import cc.hyperium.event.InvokeEvent;
import net.minecraft.client.Minecraft;
import cc.hyperium.utils.ChatColor;
import java.lang.*;
import java.io.*;
import java.util.*;

/*
 *  Better chat filter
 *  For chat filtering.  
 *  Its kinda self explanitory
 */

public class BetterChatFilter {
  
  // Set file URL
  private static final String badWordsURL = "https://raw.githubusercontent.com/HyperiumClient/Hyperium-Repo/master/files/BadWords.txt";
  
  // try to download file from hyperium repo
      try {
        Multithreading.POOL.submit(() -> {
          try {
            private final String rawBadwords = IOUtils.toString(new URL(badWordsURL), Charset.forName("UTF-8"));
            badWords = new ArrayList<>(Arrays.asList(rawBadwords.split("\n")));
          } catch (Exception depressionHits) {
            depressionHits.printStackTrace(); // happens upon depression
          }
        });
      } catch (IOException ohNoAnError) {
        // IOException triggered so lets print the stacktrace 
        ohNoAnError.printStackTrace();
      }
  
  @InvokeEvent
  public void onChat(ChatEvent chatty) {
    if (Settings.BETTER_CHAT_FILTER) {
      // if the code makes it to this point then something has happened with the chat and B.C.F. is enabled in user settings
      
      // for each bad word test to see if it contains a bad word and if it does cancel the event
      String unformattedMessage = ChatColor.stripColor(event.getChat().getUnformattedText());
      if (getBadwords().stream().anyMatch(unformattedMessage::contains) && unformattedMessage.startsWith(" ")) {
        chatty.setCancelled(true); 
      }
    }
  } 
}

// DISCLAIMER: PLEASE DO NOT JUDGE ME BASED ON MY VARIABLE NAMES AND CUSTOMIZATION AND ALL THAT kthxbye
