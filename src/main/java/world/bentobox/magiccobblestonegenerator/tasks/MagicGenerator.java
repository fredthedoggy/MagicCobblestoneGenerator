package world.bentobox.magiccobblestonegenerator.tasks;


import java.util.Random;
import java.util.TreeMap;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.inventory.ItemStack;
import org.eclipse.jdt.annotation.Nullable;

import world.bentobox.magiccobblestonegenerator.StoneGeneratorAddon;
import world.bentobox.magiccobblestonegenerator.database.objects.GeneratorTierObject;
import world.bentobox.magiccobblestonegenerator.utils.Why;


/**
 * This class process given block transforming to random object from input configuration.
 */
public class MagicGenerator
{
    /**
     * Default constructor. Inits Generator once.
     * @param addon Magic Cobblestone Generator addon.
     */
    public MagicGenerator(StoneGeneratorAddon addon)
    {
        this.addon = addon;
    }


    /**
     * This method tries to replace given block with new object and returns true if it was successful.
     * @param block Block that should be replaced.
     * @return <code>true</code> if replacing block was successful.
     */
    public boolean isCobblestoneReplacementGenerated(Block block)
    {
        GeneratorTierObject generatorTier = this.addon.getAddonManager().getGeneratorTier(
            block.getLocation(),
            GeneratorTierObject.GeneratorType.COBBLESTONE);

        return this.processBlockReplacement(block, generatorTier);
    }


    /**
     * This method tries to replace given block with new object and returns true if it was successful.
     * @param block Block that should be replaced.
     * @return <code>true</code> if replacing block was successful.
     */
    public boolean isStoneReplacementGenerated(Block block)
    {
        GeneratorTierObject generatorTier = this.addon.getAddonManager().getGeneratorTier(
            block.getLocation(),
            GeneratorTierObject.GeneratorType.STONE);

        return this.processBlockReplacement(block, generatorTier);
    }


    /**
     * This method tries to replace given block with new object and returns true if it was successful.
     * @param block Block that should be replaced.
     * @return <code>true</code> if replacing block was successful.
     */
    public boolean isBasaltReplacementGenerated(Block block)
    {
        GeneratorTierObject generatorTier = this.addon.getAddonManager().getGeneratorTier(
            block.getLocation(),
            GeneratorTierObject.GeneratorType.BASALT);

        return this.processBlockReplacement(block, generatorTier);
    }


    /**
     * This method tries to replace block from chance map and returns if it was successful.
     * @param block Block that need to be replaced.
     * @param generatorTier Object that contains all possible chances.
     * @return {@code true} if it was successful, {@code false} otherwise.
     */
    private boolean processBlockReplacement(Block block, @Nullable GeneratorTierObject generatorTier)
    {
        if (generatorTier == null)
        {
            Why.report(block.getLocation(), "Missing Generator Tier");
            // Check if generator exists.
            return false;
        }

        TreeMap<Double, Material> chanceMap = generatorTier.getBlockChanceMap();

        if (chanceMap.isEmpty())
        {
            Why.report(block.getLocation(), "Missing Block Chances in " + generatorTier.getUniqueId());

            // Check if any block has a chance to spawn
            return false;
        }

        Material newMaterial = this.getMaterialFromMap(chanceMap);

        if (newMaterial == null)
        {
            Why.report(block.getLocation(), "Cannot parse material from ChanceMap in " + generatorTier.getUniqueId());

            // Check if a material was found
            return false;
        }

        Why.report(block.getLocation(),
            "Replace " + block.getType() + " with " + newMaterial + " by " + generatorTier.getUniqueId());

        // ask config if physics should be used
        block.setType(newMaterial, this.addon.getSettings().isUsePhysics());

        if (generatorTier.getMaxTreasureAmount() > 0 &&
            generatorTier.getTreasureChance() > 0 &&
            !generatorTier.getTreasureItemChanceMap().isEmpty())
        {
            // Random check on getting treasure.
            if (this.random.nextDouble() <= generatorTier.getTreasureChance())
            {
                // Use the same variables for treasures.
                TreeMap<Double, ItemStack> treasureMap = generatorTier.getTreasureItemChanceMap();
                ItemStack itemStack = this.getMaterialFromMap(treasureMap);

                // Double check, in general it should always be a material.
                if (itemStack != null)
                {
                    ItemStack drop = itemStack.clone();
                    drop.setAmount(this.random.nextInt(generatorTier.getMaxTreasureAmount() - 1) + 1);

                    // drop item naturally in the location of the block
                    block.getWorld().dropItemNaturally(block.getLocation(), drop);

                    // TODO: Add effects like sound and particle :)
                }
            }
        }

        return true;
    }


    /**
     * This method returns a random material from given tree map.
     * @param chanceMap Map that contains all objects with their chance to drop.
     * @return <T> from map or null.
     */
    private <T> T getMaterialFromMap(TreeMap<Double, T> chanceMap)
    {
        if (chanceMap.isEmpty())
        {
            return null;
        }

        if (chanceMap.size() == 1)
        {
            // no needs to calculate. It is our material.
            return chanceMap.get(chanceMap.firstKey());
        }
        else
        {
            double rand = this.random.nextDouble() * chanceMap.lastKey();
            return chanceMap.ceilingEntry(rand).getValue();
        }
    }


    // ---------------------------------------------------------------------
    // Section: Variables
    // ---------------------------------------------------------------------

    /**
     * This variable holds stone generator addon object.
     */
    private final StoneGeneratorAddon addon;


    /**
     * Random for generator
     */
    private final Random random = new Random(System.currentTimeMillis());
}
