package me.xaspired.GravityReal.Managers;

import me.xaspired.GravityReal.Main;
import me.xaspired.GravityReal.Objects.GravityPlayer;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public class CompassManager implements Listener {
    private final Inventory inv = Bukkit.createInventory(null, 27, "§bGiocatori in Gioco");

    public static void giveCompassToPlayer(Player player) {
        ItemStack compassPlayer = createGuiItem(
                "",
                "§8| §7Che ne dici di spiare un po'",
                "§8| §7chi sta ancora giocando?"
        );
        player.getInventory().setItem(0, compassPlayer);
    }

    /* **********************************************
            Create the Compass Item
    ********************************************** */
    private static ItemStack createGuiItem(final String... lore) {
        final ItemStack item = new ItemStack(Material.COMPASS, 1);
        final ItemMeta meta = item.getItemMeta();

        assert meta != null;
        meta.setDisplayName("§aGiocatori in Gioco");
        meta.setLore(Arrays.asList(lore));
        item.setItemMeta(meta);

        return item;
    }

    /* **********************************************
        Custom GUI (Inventory) with players head
    ********************************************** */
    public void updateInventoryWithPlayerHeads(Player player) {
        inv.clear();
        for (GravityPlayer playerInGameOnline : Main.getInstance().inGamePlayers.values()) {

            // If player is himself, don't show im to the gui inventory
            if (playerInGameOnline.getPlayer().equals(player))
                continue;

            ItemStack skull = new ItemStack(Material.PLAYER_HEAD);
            SkullMeta meta = (SkullMeta) skull.getItemMeta();
            assert meta != null;
            meta.setOwningPlayer(playerInGameOnline.getPlayer());
            meta.setDisplayName("§a" + playerInGameOnline.getPlayer().getName());
            meta.setLore(List.of("§7Clicca per teletrasportarti"));
            skull.setItemMeta(meta);
            inv.addItem(skull);
        }
    }

    public void openInventory(final HumanEntity ent) {
        ent.openInventory(inv);
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent e) {
        Player player = e.getPlayer();
        ItemStack item = e.getItem();

        if (item != null && item.getType() == Material.COMPASS && item.hasItemMeta()) {
            String displayName = Objects.requireNonNull(item.getItemMeta()).getDisplayName();
            if (displayName.equalsIgnoreCase("§aGiocatori in Gioco")) {
                updateInventoryWithPlayerHeads(e.getPlayer());
                openInventory(player);
                e.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void onInventoryClick(final InventoryClickEvent e) {
        if (!e.getView().getTitle().equals("§bGiocatori in Gioco")) return;

        e.setCancelled(true);

        final ItemStack clickedItem = e.getCurrentItem();
        if (clickedItem == null || clickedItem.getType().isAir()) return;

        final Player clicker = (Player) e.getWhoClicked();

        if (!(clickedItem.getItemMeta() instanceof SkullMeta skullMeta)) return;
        OfflinePlayer targetOffline = skullMeta.getOwningPlayer();
        if (targetOffline == null || !targetOffline.isOnline()) {
            clicker.sendMessage("§cIl giocatore non è più online.");
            return;
        }

        // Status variables about player's infos
        boolean hadAllowFlight = clicker.getAllowFlight();
        boolean wasFlying = clicker.isFlying();
        GameMode previousGameMode = clicker.getGameMode();

        Player target = (Player) targetOffline;
        clicker.teleport(target.getLocation());

        // Restore old infos (gamemode, fly, etc.)
        Bukkit.getScheduler().runTaskLater(Main.getInstance(), () -> {
            clicker.setAllowFlight(hadAllowFlight);
            clicker.setFlying(wasFlying);
            clicker.setGameMode(previousGameMode);
        }, 1L);
    }


    @EventHandler
    public void onInventoryDrag(final InventoryDragEvent e) {
        if (e.getView().getTitle().equals("§bGiocatori in Gioco")) {
            e.setCancelled(true);
        }
    }
}
