package com.robomwm.reciperemover;

import org.bukkit.Material;
import org.bukkit.inventory.Recipe;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
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
        if (getServer().getOnlinePlayers().size() > 0)
        {
            getLogger().warning("Recipes may only be removed on server start, or when no players are present on the server!");
            getLogger().warning("Attempting to remove recipes while players are on the server will break all online players' session data, preventing them from being saved.");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        getConfig().addDefault("debug", false);
        List<String> defaultMaterials = new ArrayList<>();
        defaultMaterials.add("HOPPER");
        getConfig().addDefault("vanillaResultsToRemove", defaultMaterials);
        getConfig().options().copyDefaults(true);
        saveConfig();

        if (getConfig().getStringList("vanillaResultsToRemove").isEmpty())
            return;

        Set<Material> resultsToRemove = new HashSet<>();

        for (String materialString : getConfig().getStringList("vanillaResultsToRemove"))
        {
            Material material = Material.matchMaterial(materialString);
            if (material != null && material != Material.AIR) //Removing AIR has weird effects, not even sure why some recipes have an AIR result. https://www.spigotmc.org/threads/reciperemover.311136/#post-2947932
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
                debug("Recipe type: " + recipe.getClass().getName());
                count++;
            }
            else
            {
                debug("Adding vanilla recipe for " + recipe.getResult().getType().name() +
                        ". Recipe Type: " + recipe.getResult().getClass().getName());
                recipesToKeep.add(recipe);
            }
        }

        if (recipesToKeep.isEmpty())
            return;

        //clear recipes
        getServer().clearRecipes();

        //for debugging: doublecheck if recipes iterator is empty, and if not, report remaining recipes
        recipeIterator = getServer().recipeIterator();
        while (recipeIterator.hasNext())
        {
            Recipe recipe = recipeIterator.next();
            debug("Recipe for " + recipe.getResult().getType().name()
                    + " was not removed (Craftbukkit didn't clear it). Recipe Type: " + recipe.getResult().getClass().getName());
        }

        for (Recipe recipe : recipesToKeep)
        {
            try
            {
                getServer().addRecipe(recipe);
            }
            catch (IllegalStateException e) //vanilla recipe
            {
                debug("Could not re-add recipe for " + recipe.getResult().getType().name() + " because " + e.getMessage());
            }
        }

        getLogger().info("Removed " + count + " recipes.");
    }

    public void debug(String message)
    {
        if (getConfig().getBoolean("debug"))
            getLogger().info(message);
    }
}
