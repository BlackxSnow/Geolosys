package com.oitsjustjose.geolosys.common.world;

import java.util.ArrayList;
import java.util.Random;

import com.oitsjustjose.geolosys.Geolosys;
import com.oitsjustjose.geolosys.api.world.DepositBiomeRestricted;
import com.oitsjustjose.geolosys.api.world.DepositMultiOreBiomeRestricted;
import com.oitsjustjose.geolosys.api.world.DepositStone;
import com.oitsjustjose.geolosys.api.world.IDeposit;
import com.oitsjustjose.geolosys.common.config.CommonConfig;
import com.oitsjustjose.geolosys.common.world.feature.PlutonOreFeature;
import com.oitsjustjose.geolosys.common.world.feature.PlutonStoneFeature;

import net.minecraft.util.math.BlockPos;
import net.minecraft.world.ISeedReader;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.gen.GenerationStage;
import net.minecraft.world.gen.feature.NoFeatureConfig;
import net.minecraftforge.common.world.BiomeGenerationSettingsBuilder;
import net.minecraftforge.event.world.BiomeLoadingEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class PlutonRegistry {
    private ArrayList<IDeposit> ores;
    private ArrayList<IDeposit> oreWeightList;
    private ArrayList<DepositStone> stones;
    private ArrayList<DepositStone> stoneWeightList;

    public PlutonRegistry() {
        this.ores = new ArrayList<>();
        this.oreWeightList = new ArrayList<>();
        this.stones = new ArrayList<>();
        this.stoneWeightList = new ArrayList<>();
    }

    public void clear() {
        this.ores = new ArrayList<>();
        this.oreWeightList = new ArrayList<>();
        this.stones = new ArrayList<>();
        this.stoneWeightList = new ArrayList<>();
    }

    @SuppressWarnings("unchecked")
    public ArrayList<IDeposit> getOres() {
        return (ArrayList<IDeposit>) this.ores.clone();
    }

    @SuppressWarnings("unchecked")
    public ArrayList<IDeposit> getStones() {
        return (ArrayList<IDeposit>) this.stones.clone();
    }

    public boolean addOrePluton(IDeposit ore) {
        if (ore instanceof DepositStone) {
            return addStonePluton((DepositStone) ore);
        }

        for (int i = 0; i < ore.getChance(); i++) {
            oreWeightList.add(ore);
        }

        return this.ores.add(ore);
    }

    public boolean addStonePluton(DepositStone stone) {
        for (int i = 0; i < stone.getChance(); i++) {
            stoneWeightList.add(stone);
        }
        return this.stones.add(stone);
    }

    public IDeposit pickPluton(ISeedReader reader, BlockPos pos, Random rand) {
        if (this.oreWeightList.size() > 0) {
            // Sometimes bias towards specific biomes where applicable
            if (rand.nextBoolean()) {
                Biome b = reader.getBiome(pos);
                ArrayList<IDeposit> forBiome = new ArrayList<>();
                for (IDeposit d : this.ores) {
                    if (d instanceof DepositBiomeRestricted) {
                        if (((DepositBiomeRestricted) d).canPlaceInBiome(b)) {
                            forBiome.add(d);
                        }
                    } else if (d instanceof DepositMultiOreBiomeRestricted) {
                        if (((DepositMultiOreBiomeRestricted) d).canPlaceInBiome(b)) {
                            forBiome.add(d);
                        }
                    }
                }
                if (forBiome.size() > 0) {
                    int pick = rand.nextInt(forBiome.size());
                    return forBiome.get(pick);
                }
            }
            int pick = rand.nextInt(this.oreWeightList.size());
            return this.oreWeightList.get(pick);

        }
        return null;
    }

    public DepositStone pickStone() {
        if (this.stoneWeightList.size() > 0) {
            Random random = new Random();
            int pick = random.nextInt(this.stoneWeightList.size());
            return this.stoneWeightList.get(pick);
        }
        return null;
    }

    @SubscribeEvent
    public void onBiomesLoaded(BiomeLoadingEvent evt) {
        BiomeGenerationSettingsBuilder settings = evt.getGeneration();

        if (CommonConfig.REMOVE_VANILLA_ORES.get()) {
            int removed = OreRemover.process(settings);
            // Notify the user of the removal if debug mode is on
            if (CommonConfig.DEBUG_WORLD_GEN.get()) {
                Geolosys.getInstance().LOGGER.info("Removing {} ore generation(s) from biome {}", removed,
                        evt.getName());
            }
        }

        PlutonOreFeature o = new PlutonOreFeature(NoFeatureConfig.field_236558_a_);
        PlutonStoneFeature s = new PlutonStoneFeature(NoFeatureConfig.field_236558_a_);

        settings.withFeature(GenerationStage.Decoration.UNDERGROUND_ORES,
                o.withConfiguration(NoFeatureConfig.field_236559_b_));
        settings.withFeature(GenerationStage.Decoration.UNDERGROUND_ORES,
                s.withConfiguration(NoFeatureConfig.field_236559_b_));
    }
}