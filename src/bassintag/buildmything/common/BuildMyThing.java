package bassintag.buildmything.common;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.logging.Logger;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Color;
import org.bukkit.FireworkEffect;
import org.bukkit.FireworkEffect.Type;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Firework;
import org.bukkit.entity.Player;
import org.bukkit.inventory.meta.FireworkMeta;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import bassintag.buildmything.common.buildZone.BuildZone;
import bassintag.buildmything.common.buildZone.BuildZoneListener;
import bassintag.buildmything.common.cuboid.CuboidZone;
import bassintag.buildmything.common.signs.SignListener;

public class BuildMyThing extends JavaPlugin{
	
	private List<BuildZone> rooms = new ArrayList<BuildZone>();
	public Logger logger = Logger.getLogger("Minecraft");
	public BuildMyThing plugin = this;
	
	private List<String> words = new ArrayList<String>();
	
	@Override
	public void onDisable() {
		PluginDescriptionFile pdfFile = this.getDescription();
		this.getLogger().info(pdfFile.getName() + "Disabled !");
		
		List<String> names = new ArrayList<String>();
		
		for(BuildZone b : rooms){
			names.add(b.getName());
			b.stop();
			b.save(getConfig());
		}
		getConfig().set("rooms", names);
		this.rooms.clear();
		this.saveConfig();
	}

	@Override
	public void onEnable() {
		PluginDescriptionFile pdfFile = this.getDescription();
		this.getLogger().info(pdfFile.getName() + " Enabled !");
		
		this.getLogger().info("Current version: " + pdfFile.getVersion());
		
		List<String> defaultWords = new ArrayList<String>(Arrays.asList(new String[] {"house", "creeper", "pickaxe", "boat", "dog", "apple", "bow", "bone", "minecart", "zombie", "pig", "chicken", "skeleton", "tree", "cloud", "sun", "moon", "cave", "slime", "flower", "mountain", "volcano", "potato", "mushroom", "sword", "armor", "diamond", "cat", "book", "sheep", "squid", "enderman", "snowman", "bread", "wheat"}));
	    this.getConfig().options().copyDefaults(true);
	    this.getConfig().addDefault("words", defaultWords);
	    this.getConfig().addDefault("allow-creative", true);
		
		BuildZoneListener bListener = new BuildZoneListener(this);
		SignListener sListener = new SignListener(this);
		PluginManager pm = getServer().getPluginManager();
		pm.registerEvents(bListener, this);
		pm.registerEvents(sListener, this);
		
		if(getConfig().getList("rooms") != null){
			if(getConfig().getList("rooms").size() > 0){
				if(getConfig().getList("rooms").get(0) instanceof String){
					@SuppressWarnings("unchecked")
					List<String> rooms = (List<String>) getConfig().getList("rooms");
					
					for(String s : rooms){
						this.rooms.add(BuildZone.load(getConfig(), s, this));
					}
				}
			}
		}
		
		if(getConfig().getList("words") != null){
			if(getConfig().getList("words").size() > 0){
				if(getConfig().getList("words").get(0) instanceof String){
					@SuppressWarnings("unchecked")
					List<String> words = (List<String>) getConfig().getList("words");
					for(String s : words){
						if(s != null){
							this.words.add(s);
						}
					}
				}
			}
		}
	}
	
	public String getRandomWord(){
		int i = this.words.size();
		Random r = new Random();
		return this.words.get(r.nextInt(i));
	}

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args){
		if(cmd.getName().equals("bmt")){
			if(sender instanceof Player){
				Player player  = (Player) sender;
				if(args.length > 0){
						if(args[0].equals("p1") && player.hasPermission("bmt.admin")){
							ChatUtil.send(player, "Point 1 set to your actual feet position");
							player.setMetadata("bmtp1", new FixedMetadataValue(this, LocationUtil.LocationToString(player.getLocation())));
							return true;
						} else if(args[0].equals("p2") && player.hasPermission("bmt.admin")){
							ChatUtil.send(player, "Point 2 set to your actual feet position");
							player.setMetadata("bmtp2", new FixedMetadataValue(this, LocationUtil.LocationToString(player.getLocation())));
							return true;
						} else if(args[0].equals("spawn") && player.hasPermission("bmt.admin")){
							ChatUtil.send(player, "Room spawn location set to your actual feet position");
							player.setMetadata("bmtspec", new FixedMetadataValue(this, LocationUtil.LocationToString(player.getLocation())));
							return true;
						} else if(args[0].equals("create") && player.hasPermission("bmt.admin")){
							if(args.length > 1){
								if(player.hasMetadata("bmtp1") && player.hasMetadata("bmtp2") && player.hasMetadata("bmtspec")){
									if(this.getRoomByName(args[1]) != null){
										ChatUtil.send(player, "A room with this name already exist!");
									} else {
										Location loc1 = LocationUtil.StringToLoc(player.getMetadata("bmtp1").get(0).asString());
										Location loc2 = LocationUtil.StringToLoc(player.getMetadata("bmtp2").get(0).asString());
										Location spawn = LocationUtil.StringToLoc(player.getMetadata("bmtspec").get(0).asString());
										this.rooms.add(new BuildZone(new CuboidZone(loc1.getBlock(), loc2.getBlock()), spawn, args[1], this));
										ChatUtil.send(player, "Room created!");
									}
								} else {
									ChatUtil.send(player, "Make sure you selected the 2 points of the build zone and the room spawn location");
									ChatUtil.send(player, "Commands are: /bmt p1, /bmt p2, /bmt spawn");
								}
							} else {
								ChatUtil.send(player, "You must precize a room name");
							}
						} else if(args[0].equals("remove") && player.hasPermission("bmt.admin")){
							if(args.length > 1){
								if(this.getRoomByName(args[1]) != null){
									this.getRoomByName(args[1]).remove(getConfig());
									this.rooms.remove(this.getRoomByName(args[1]));
								} else {
									ChatUtil.send(player, "This room doesn't exist!");
								}
							} else {
								ChatUtil.send(player, "You must precize a room name");
							}
						} else if(args[0].equals("join") && player.hasPermission("bmt.default")){
							if(player.hasMetadata("inbmt")){
								ChatUtil.send(player, "You are already in a game room");
							} else {
								if(args.length > 1){
									if(this.getRoomByName(args[1]) != null){
										this.getRoomByName(args[1]).join(player);
									} else {
										ChatUtil.send(player, "This room doesn't exist");
									}
								} else {
									ChatUtil.send(player, "You must precize a room name");
								}
							}
						} else if(args[0].equals("leave") && player.hasPermission("bmt.default")){
							if(player.hasMetadata("inbmt")){
								this.getRoomByName(player.getMetadata("inbmt").get(0).asString()).leave(player);
							} else {
								ChatUtil.send(player, "You aren't in a game room");
							}
						} else if(args[0].equals("ready") && player.hasPermission("bmt.default")){
							if(player.hasMetadata("inbmt")){
								this.getRoomByName(player.getMetadata("inbmt").get(0).asString()).setReady(player);
							} else {
								ChatUtil.send(player, "You aren't in an game room");
							}
						} else if(args[0].equals("list") && player.hasPermission("bmt.default")){
							ChatUtil.send(player, "Room list:");
							for(BuildZone b : this.rooms){
								player.sendMessage(ChatColor.YELLOW + "* " + ChatColor.AQUA + b.getName() + ChatColor.RESET +  " | " + b.getPlayers().size() + ChatColor.YELLOW + "/" + ChatColor.RESET + b.getMaxPlayers() + " players | (" + (b.isStarted() ? ChatColor.RED + "STARTED" : ChatColor.GREEN + "OPEN") + ChatColor.RESET + ")");
							}
						} else if(args[0].equals("invite") && player.hasPermission("bmt.default")){
							if(player.hasMetadata("inbmt")){
								ChatUtil.broadcast(player.getName() + " wants to play Build My Thing, use \"" + ChatColor.YELLOW + "/bmt playwith " + player.getName() + ChatColor.RESET + "\" to play with him");
							} else {
								ChatUtil.send(player, "You aren't in an game room");
							}
						} else if(args[0].equals("playwith") && player.hasPermission("bmt.default")){
							if(player.hasMetadata("inbmt")){
								ChatUtil.send(player, "You are in a game room");
							} else {
								if(args.length > 1){
									if(Bukkit.getPlayer(args[1]) != null){
										if(Bukkit.getPlayer(args[1]).isOnline()){
											Player p = Bukkit.getPlayer(args[1]);
											if(p.hasMetadata("inbmt")){
												this.getRoomByName(p.getMetadata("inbmt").get(0).asString()).join(player);
											} else {
												ChatUtil.send(player, "This player isn't playing Build My Thing");
											}
										} else {
											ChatUtil.send(player, "This player isn't online");
										}
									} else {
										ChatUtil.send(player, "This player isn't online");
									}
								} else {
									ChatUtil.send(player, "You must precize a player name");
								}
							}
						} else if(args[0].equals("help")) {
							ChatUtil.send(player, "List of commands:");
							
							player.sendMessage(ChatColor.GOLD + "/bmt help " + ChatColor.GRAY + "display the plugin help");
							
							if(player.hasPermission("bmt.admin")){
								player.sendMessage(ChatColor.GOLD + "/bmt p1 " + ChatColor.GRAY + "Set the first point to your current position");
								player.sendMessage(ChatColor.GOLD + "/bmt p2 " + ChatColor.GRAY + "Set the second point to your current position");
								player.sendMessage(ChatColor.GOLD + "/bmt spawn " + ChatColor.GRAY + "Set the spawn point to your current position");
								player.sendMessage(ChatColor.GOLD + "/bmt create [room name] " + ChatColor.GRAY + "Create a new room with the supplied name");
								player.sendMessage(ChatColor.GOLD + "/bmt remove [room name] " + ChatColor.GRAY + "Remove the room with the supplied name");
							}
							
							if(player.hasPermission("bmt.default")){
								player.sendMessage(ChatColor.GOLD + "/bmt join [room name] " + ChatColor.GRAY + "Join the room with the supplied name");
								player.sendMessage(ChatColor.GOLD + "/bmt leave " + ChatColor.GRAY + "Leave your current room");
								player.sendMessage(ChatColor.GOLD + "/bmt ready " + ChatColor.GRAY + "Toggle if you are ready ore not");
								player.sendMessage(ChatColor.GOLD + "/bmt invite " + ChatColor.GRAY + "Broadcast a message to invite other players to join you");
								player.sendMessage(ChatColor.GOLD + "/bmt playwith [username] " + ChatColor.GRAY + "Play with another player");
							}
						}else {
							ChatUtil.send(player, "Unknown sub-command");
						}
				} else {
					ChatUtil.send(player, "No sub-command, use \"" + ChatColor.YELLOW + "/bmt help" + ChatColor.RESET + "\"to get a list of commands");
				}
			} else {
				sender.sendMessage("Sorry this command can only be run by a player");
			}
		}
		return false;
	}
	
    public void spawnRandomFirework(Location loc) {               

        Firework fw = (Firework) loc.getWorld().spawnEntity(loc, EntityType.FIREWORK);
        FireworkMeta fwm = fw.getFireworkMeta();

        Random r = new Random();   

        Type type = Type.BALL;       
        
        Color c1 = Color.GREEN;
        Color c2 = Color.YELLOW;

        FireworkEffect effect = FireworkEffect.builder().flicker(r.nextBoolean()).withColor(c1).withFade(c2).with(type).trail(r.nextBoolean()).build();

        fwm.addEffect(effect);

        fwm.setPower(1);

        fw.setFireworkMeta(fwm);           
    }           
	
	public BuildZone getRoomByName(String name){
		BuildZone result = null;
		for(BuildZone b : this.rooms){
			if(b.getName().equals(name)){
				result = b;
			}
		}
		return result;
	}
}