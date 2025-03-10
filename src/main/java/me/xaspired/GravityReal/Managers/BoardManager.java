package me.xaspired.GravityReal.Managers;

import me.xaspired.GravityReal.Main;
import me.xaspired.GravityReal.UsefulMethods;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.*;

public class BoardManager {

    public static Player[] scorePlayer = {null, null, null, null, null};
    static int[] scoreIndex = {0, 0, 0, 0, 0};
    public static void createBoard(Player player) {

        ScoreboardManager manager = Bukkit.getScoreboardManager();
        Scoreboard board = manager.getNewScoreboard();
        Objective obj = board.registerNewObjective("GravityScore", "forDummy", ChatColor.translateAlternateColorCodes('&', "&a&lGra&b&lvity"));
        obj.setDisplaySlot(DisplaySlot.SIDEBAR);
        Score score11 = obj.getScore(ChatColor.DARK_GRAY + " ");
        score11.setScore(11);
        Score score10 = obj.getScore(ChatColor.YELLOW + ChatColor.BOLD.toString() + "Your Stats:");
        score10.setScore(10);
        Score score9 = obj.getScore("  " + ChatColor.WHITE + player.getName());
        score9.setScore(9);
        Score score8 = obj.getScore("   ");
        score8.setScore(8);
        Score score7 = obj.getScore("  ");
        score7.setScore(7);
        Score score6 = obj.getScore(ChatColor.DARK_AQUA + ChatColor.BOLD.toString() + "Ranking:");
        score6.setScore(6);

        for (Player playerInFor : Main.getInstance().getServer().getOnlinePlayers()) {

            // Check if the player pass 1st map(index=0)
            Integer playerMapValue = Main.playerMap.get(playerInFor); // Retrieve the value

            if (playerMapValue == null || playerMapValue < 1) {
                continue; // If null or less than 1, skip this player
            }

            // If the player was in scoreboard, update the index
            boolean flag = false;
            for (int j = 0; j < 5; ++j) {
                if (scorePlayer[j] == playerInFor) {
                    scoreIndex[j] = playerMapValue; // Use the safe value
                    flag = true;
                    break;
                }
            }

            // Otherwise, add place player to the last position if they have a map > last map player
            if (!flag) {
                if (playerMapValue > scoreIndex[4] || scorePlayer[4] == null) {
                    scorePlayer[4] = playerInFor;
                    scoreIndex[4] = playerMapValue;
                }
            }
        }

        for (int i = 0; i < 4; ++i) {
            boolean swapped = false;

            for (int j = 0; j < 4 - i; ++j) {
                if (scorePlayer[j + 1] == null)
                    continue;

                boolean swapNeeded = false;

                // Same index, check time
                if (scoreIndex[j + 1] == scoreIndex[j] && Main.playerTime.get(scorePlayer[j + 1]) < Main.playerTime.get(scorePlayer[j])) {
                    swapNeeded = true;
                }

                else if (scoreIndex[j + 1] > scoreIndex[j] || scorePlayer[j] == null) {
                    swapNeeded = true;
                }

                if (swapNeeded) {
                    // Swap using temp
                    int tempScore = scoreIndex[j];
                    scoreIndex[j] = scoreIndex[j + 1];
                    scoreIndex[j + 1] = tempScore;

                    Player tempPlayer = scorePlayer[j];
                    scorePlayer[j] = scorePlayer[j + 1];
                    scorePlayer[j + 1] = tempPlayer;

                    swapped = true;
                }
            }

            // If no swap has been done, the list is ordered
            if (!swapped)
                break;
        }


        // Set up scoreboard 5 rows @TODO fix variable
        Score scores[] = new Score[5];
        for (int i = 0; i < 5; ++i) {

            // Check if player is null (not arrived)
            if (scorePlayer[i] == null) {
                scores[i] = obj.getScore(ChatColor.WHITE + String.valueOf(i + 1) + "#  Waiting...");
            }
            else {
                // Check if player has finished
                if (Main.playerMap.get(scorePlayer[i]) == Main.getInstance().getConfig().getInt("maps-per-game")) {
                    int realpos = i+1;
                    // @TODO Testare lo switch
                    switch(realpos) {
                        case 1:
                            scores[i] = obj.getScore(ChatColor.GOLD + "1#  " + scorePlayer[i].getName() + " " + UsefulMethods.returnTimeFormatted(Main.playerTime.get(scorePlayer[i])));
                        case 2:
                            scores[i] = obj.getScore(ChatColor.GRAY + "2#  " + scorePlayer[i].getName() + " " + UsefulMethods.returnTimeFormatted(Main.playerTime.get(scorePlayer[i])));
                        case 3:
                            scores[i] = obj.getScore(ChatColor.DARK_RED + "3#  " + scorePlayer[i].getName() + " " + UsefulMethods.returnTimeFormatted(Main.playerTime.get(scorePlayer[i])));
                        default:
                            scores[i] = obj.getScore(ChatColor.GREEN + String.valueOf(realpos) + "#  " + scorePlayer[i].getName() + " " + ChatColor.GRAY + UsefulMethods.returnTimeFormatted(Main.playerTime.get(scorePlayer[i])));
                    }
                }
                else {
                    scores[i] = obj.getScore(ChatColor.WHITE + String.valueOf(i + 1) + "#  " + scorePlayer[i].getName());
                }
            }

            // Add row to scoreboard
            scores[i].setScore(5 - i);
        }

        player.setScoreboard(board);
    }

}
