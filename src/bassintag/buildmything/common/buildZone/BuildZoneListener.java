package bassintag.buildmything.common.buildZone;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;

import bassintag.buildmything.common.BuildMyThing;
import bassintag.buildmything.common.ChatUtil;

public class BuildZoneListener implements Listener{
	
	private BuildMyThing instance;
	
	public BuildZoneListener(BuildMyThing instance){
		this.instance = instance;
	}
	
	/*
	 * 	###################
	 * 	# EVENTS HANDLERS #
	 * 	###################
	 */
	
	
	@EventHandler
	public void onPlayerLogOut(PlayerQuitEvent event){
		if(event.getPlayer().hasMetadata("inbmt")){
			instance.getRoomByName(event.getPlayer().getMetadata("inbmt").get(0).asString()).leave(event.getPlayer());
		}
	}
	
	@EventHandler
	public void onPlayerPlaceBlock(BlockPlaceEvent event){
		if(event.getPlayer().hasMetadata("inbmt")){
			if(instance.getRoomByName(event.getPlayer().getMetadata("inbmt").get(0).asString()) != null){
				if(instance.getRoomByName(event.getPlayer().getMetadata("inbmt").get(0).asString()).getBuilder().getName() == event.getPlayer().getName()){
					if(instance.getRoomByName(event.getPlayer().getMetadata("inbmt").get(0).asString()).getBuildZone().contains(event.getBlock())){
						event.getPlayer().getInventory().addItem(new ItemStack(event.getBlockPlaced().getType(), 1, event.getBlockPlaced().getData()));
						return;
					}
				}
			}
			event.setCancelled(true);
		}
	}
	
	@EventHandler
	public void onPlayerInteract(PlayerInteractEvent event){
		if(event.getAction() == Action.LEFT_CLICK_BLOCK){
			if(event.getPlayer().hasMetadata("inbmt")){
				if(instance.getRoomByName(event.getPlayer().getMetadata("inbmt").get(0).asString()) != null){
					if(instance.getRoomByName(event.getPlayer().getMetadata("inbmt").get(0).asString()).getBuilder() != null){
						if(instance.getRoomByName(event.getPlayer().getMetadata("inbmt").get(0).asString()).getBuilder().getName() == event.getPlayer().getName()){
							if(instance.getRoomByName(event.getPlayer().getMetadata("inbmt").get(0).asString()).getBuildZone().contains(event.getClickedBlock())){
								event.getClickedBlock().setType(Material.AIR);
								return;
							}
						}
					}
				}
			}
		}
	}
	
    @EventHandler
    public void onPlayerCommand(PlayerCommandPreprocessEvent event){
		if(event.getPlayer().hasMetadata("inbmt")){
			if(!event.getMessage().startsWith("/bmt")){
				ChatUtil.send(event.getPlayer(), "Commands are disabled while in-game");
				event.setCancelled(true);
			}
	    }
    }
	
	@EventHandler
	public void onPlayerBreakBlock(BlockBreakEvent event){
		if(event.getPlayer().hasMetadata("inbmt")){
			event.setCancelled(true);
		}
	}
	
	@EventHandler
	public void onPlayerDropItem(PlayerDropItemEvent event){
		if(event.getPlayer().hasMetadata("inbmt")){
			event.setCancelled(true);
		}
	}
	
	@EventHandler
	public void onPlayerHit(EntityDamageEvent event){
		if(event.getEntity() instanceof Player){
			Player p = (Player) event.getEntity();
			if(p.hasMetadata("inbmt")){
				event.setCancelled(true);
			}
		}
	}
	
	@EventHandler
	public void onPlayerChat(AsyncPlayerChatEvent event){
		if(event.getPlayer().hasMetadata("inbmt")){
			if(instance.getRoomByName(event.getPlayer().getMetadata("inbmt").get(0).asString()) != null){
				if(instance.getRoomByName(event.getPlayer().getMetadata("inbmt").get(0).asString()).isStarted()){
					if(instance.getRoomByName(event.getPlayer().getMetadata("inbmt").get(0).asString()).getBuilder().getName() == event.getPlayer().getName()){
						ChatUtil.send(event.getPlayer(), "You can't chat while being the builder");
						event.setCancelled(true);
					} else {
						String word = instance.getRoomByName(event.getPlayer().getMetadata("inbmt").get(0).asString()).getWord();
						if(event.getMessage().toLowerCase().contains(word)){
							instance.getRoomByName(event.getPlayer().getMetadata("inbmt").get(0).asString()).wordFoundBy(event.getPlayer());
							event.setCancelled(true);
						} else {
							instance.getRoomByName(event.getPlayer().getMetadata("inbmt").get(0).asString()).sendMessage(ChatColor.BOLD + event.getPlayer().getName() + ": "+ ChatColor.RESET + event.getMessage().toLowerCase());
							event.setCancelled(true);
						}
					}
				}
			}
		}
	}
}