/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.uvnode.uvgeartomats;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.FurnaceSmeltEvent;
import org.bukkit.inventory.FurnaceRecipe;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

/**
 *
 * @author jcornwell
 */
public final class UVGearToMats extends JavaPlugin implements Listener {
    Map<String, ItemStack> _items;
    Random _randomizer;
    
    @Override
    public void onEnable() {
        saveDefaultConfig(); // Write the default configs to file if it doesn't exist.
        getServer().getPluginManager().registerEvents(this, this); // Register for events
        _items = new HashMap<>();
        _randomizer = new Random();
        loadRecipes();
    }

    private void loadRecipes() {
        for (String item : getConfig().getStringList("items")) {
            if (getConfig().contains("typelessItems."+item.toUpperCase())) {
                if (getConfig().contains("typelessItems."+item.toUpperCase()+".material") && getConfig().contains("typelessItems."+item.toUpperCase()+".maxAmount")) {
                    Material outputMaterial = Material.getMaterial(getConfig().getString("typelessItems."+item.toUpperCase()+".material"));
                    Integer amount = getConfig().getInt("typelessItems."+item.toUpperCase()+".material");
                    ItemStack output = new ItemStack(outputMaterial, amount);
                    _items.put(item, output);
                }
                break;
            } else {
                String[] parts = item.split("_");
                if (parts.length == 2) {
                    if (getConfig().contains("types."+parts[0])) {
                        Material outputMaterial = Material.getMaterial(getConfig().getString("types."+parts[0]));
                        if (getConfig().contains("slots."+parts[1])) {
                            Integer amount = getConfig().getInt("slots."+parts[1]);
                            ItemStack output = new ItemStack(outputMaterial, amount);
                            _items.put(item, output);
                            FurnaceRecipe recipe = new FurnaceRecipe(output, Material.getMaterial(item));
                            getServer().addRecipe(recipe);
                        } else {
                            getLogger().info(item + " load error. " + parts[1] + " is not configured.");
                        }
                    } else {
                        getLogger().info(item + " load error. " + parts[0] + " is not configured.");
                    }
                } else {
                    getLogger().info(item + " is invalid. Must be a tool or armor name.");
                }
            }
        }
    }
    
    @EventHandler
    private void onFurnaceSmeltEvent(FurnaceSmeltEvent event) {
        if (_items.containsKey(event.getSource().getType().toString())) {
            ItemStack newOutput = _items.get(event.getSource().getType().toString()).clone();
            Integer newAmount = (Integer)(_randomizer.nextInt(newOutput.getAmount())*event.getSource().getDurability());
            newOutput.setAmount(newAmount);
            if (newOutput.getAmount() <= 0)
                newOutput.setAmount(1);
            event.setResult(newOutput);
        }
    }
}
    