package com.robomwm.reciperemover;

import org.bukkit.Material;
import org.bukkit.inventory.Recipe;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

/**
 * Created on 3/27/2018.
 *
 * @author RoboMWM
 */
public class RecipeRemover extends JavaPlugin
{

    public void onEnable()
    {
        saveConfig();
        if (getServer().getOnlinePlayers().size() > 0)
        {
            getLogger().warning("Recipes may only be removed on server start, or when no players are present on the server!");
            getLogger().warning("Attempting to remove recipes while players are on the server will break all online player's session data, preventing them from being saved.");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }
        List<String> defaultMaterials = new ArrayList<>();
        defaultMaterials.add("HOPPER");
        getConfig().addDefault("vanillaResultsToRemove", defaultMaterials);
        getConfig().options().copyDefaults(true);

        if (getConfig().getStringList("vanillaResultsToRemove").isEmpty())
            return;

        Set<Material> resultsToRemove = new HashSet<>();

        for (String materialString : getConfig().getStringList("vanillaResultsToRemove"))
        {
            Material material = Material.matchMaterial(materialString);
            if (material != null)
                resultsToRemove.add(material);
        }

        int count = 0;

        //Taken from CustomItemRecipes#RecipeBlocker
        //https://github.com/MLG-Fortress/CustomItemRecipes
        List<Recipe> recipesToKeep = new LinkedList<>();
        Iterator<Recipe> recipeIterator = getServer().recipeIterator();
        while (recipeIterator.hasNext())
        {
            Recipe recipe = recipeIterator.next();
            if (resultsToRemove.contains(recipe.getResult().getType()))
            {
                getLogger().info("Removed a vanilla recipe for " + recipe.getResult().getType().name());
                count++;
            }
            else
                recipesToKeep.add(recipe);
        }

        if (recipesToKeep.isEmpty())
            return;

        getServer().clearRecipes();

        for (Recipe recipe : recipesToKeep)
        {
            try
            {
                getServer().addRecipe(recipe);
            }
            catch (IllegalStateException ignored){} //vanilla recipe
        }

        getLogger().info("Removed " + count + " recipes.");
        return;
    }
}
