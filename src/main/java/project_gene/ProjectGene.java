package project_gene;

import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.AgeableEntity;
import net.minecraft.entity.EntityClassification;
import net.minecraft.entity.EntitySpawnPlacementRegistry;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.passive.AnimalEntity;
import net.minecraft.item.*;
import net.minecraft.network.datasync.IDataSerializer;
import net.minecraft.potion.*;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.gen.GenerationStage;
import net.minecraft.world.gen.Heightmap;
import net.minecraft.world.gen.feature.Feature;
import net.minecraft.world.gen.feature.FlowersFeature;
import net.minecraft.world.gen.feature.IFeatureConfig;
import net.minecraft.world.gen.feature.NoFeatureConfig;
import net.minecraft.world.gen.placement.ChanceConfig;
import net.minecraft.world.gen.placement.Placement;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.client.registry.RenderingRegistry;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.DataSerializerEntry;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.IForgeRegistry;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import project_gene.client.renderer.ChickenRenderer;
import project_gene.client.renderer.CowRenderer;
import project_gene.client.renderer.PigRenderer;
import project_gene.client.renderer.SheepRenderer;
import project_gene.config.PGConfig;
import project_gene.effect.EffectGene;
import project_gene.effect.EffectGrowing;
import project_gene.effect.EffectLove;
import project_gene.effect.EffectSelfing;
import project_gene.entity.core.IGenePooled;
import project_gene.entity.core.IShearable;
import project_gene.entity.living.Chicken;
import project_gene.entity.living.Cow;
import project_gene.entity.living.Pig;
import project_gene.entity.living.Sheep;
import project_gene.gene.GenePool;
import project_gene.item.EntityProcessorItem;
import project_gene.item.ItemBat;
import project_gene.item.ItemSausage;

import java.util.List;
import java.util.Random;

@Mod(ProjectGene.MODID)
@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD)
public class ProjectGene {

	public static final String MODID = "project_gene";
	public static Logger logger = LogManager.getLogger();
	public static IDataSerializer<GenePool> gene_pool = new GenePool.Serializer();
	public static ItemGroup group = new ItemGroup(MODID) {
		@Override
		public ItemStack createIcon() {
			return new ItemStack(gene_mirror);
		}
	};
	
	public static EntityType<Sheep> sheep = EntityType.Builder.create(Sheep::new, EntityClassification.CREATURE).size(0.9F, 1.3F).build("sheep");
	public static EntityType<Cow> cow = EntityType.Builder.create(Cow::new, EntityClassification.CREATURE).size(0.9F, 1.4F).build("cow");
	public static EntityType<Chicken> chicken = EntityType.Builder.create(Chicken::new, EntityClassification.CREATURE).size(0.4F, 0.7F).build("chicken");
	public static EntityType<Pig> pig = EntityType.Builder.create(Pig::new, EntityClassification.CREATURE).size(0.9F, 0.9F).build("pig");
	
	public static Item sheep_spawn_egg = new SpawnEggItem(sheep, 15198183, 16758197, new Item.Properties().group(group));
	public static Item cow_spawn_egg = new SpawnEggItem(cow, 4470310, 10592673, new Item.Properties().group(group));
	public static Item chicken_spawn_egg = new SpawnEggItem(chicken, 10592673, 16711680, new Item.Properties().group(group));
	public static Item pig_spawn_egg = new SpawnEggItem(pig, 15771042, 14377823, new Item.Properties().group(group));
	public static Item gene_mirror = EntityProcessorItem.<IGenePooled>of(IGenePooled.class, (player, living) -> living.sendTo(player));
	public static Item golden_wheat = EntityProcessorItem.<AgeableEntity>of(AgeableEntity.class, (player, living) -> living.setGrowingAge(0));
	public static Item golden_feather = EntityProcessorItem.<IShearable>of(IShearable.class, (player, living) -> living.setSheared(false));
	public static Item sausage = new ItemSausage();
	public static Item bat = new ItemBat();
	
	public static Effect growing = new EffectGrowing();
	public static Effect love = new EffectLove();
	public static Effect selfing = new EffectSelfing();
	public static Effect stable = new EffectGene(0x00DDFF);
	public static Effect mutable = new EffectGene(0x010101);
	
	public static FlowersFeature allium = new FlowersFeature(NoFeatureConfig::deserialize) {
		@Override
		public BlockState getRandomFlower(Random random, BlockPos blockPos) {
			return Blocks.ALLIUM.getDefaultState();
		}
	};
	
	public ProjectGene() {
		FMLJavaModLoadingContext.get().getModEventBus().addListener(this::onClientSetup);
		ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, PGConfig.CONFIG_SPEC);
	}
	
	public void onClientSetup(FMLClientSetupEvent event) {
		RenderingRegistry.registerEntityRenderingHandler(Sheep.class, SheepRenderer::new);
		RenderingRegistry.registerEntityRenderingHandler(Cow.class, CowRenderer::new);
		RenderingRegistry.registerEntityRenderingHandler(Chicken.class, ChickenRenderer::new);
		RenderingRegistry.registerEntityRenderingHandler(Pig.class, PigRenderer::new);
	}
	
	@SubscribeEvent
	public static void registerItem(RegistryEvent.Register<Item> event) {
		IForgeRegistry<Item> registry = event.getRegistry();
		registry.register(sheep_spawn_egg.setRegistryName("sheep_spawn_egg"));
		registry.register(cow_spawn_egg.setRegistryName("cow_spawn_egg"));
		registry.register(chicken_spawn_egg.setRegistryName("chicken_spawn_egg"));
		registry.register(pig_spawn_egg.setRegistryName("pig_spawn_egg"));
		registry.register(gene_mirror.setRegistryName("gene_mirror"));
		registry.register(golden_wheat.setRegistryName("golden_wheat"));
		registry.register(golden_feather.setRegistryName("golden_feather"));
		registry.register(sausage.setRegistryName("sausage"));
		registry.register(bat.setRegistryName("bat"));
	}
	
	private static void replace(Biome biome, EntityType<?> src, EntityType<?> dst) {
		List<Biome.SpawnListEntry> spawns = biome.getSpawns(EntityClassification.CREATURE);
		for (int i = 0; i < spawns.size(); ++i) {
			Biome.SpawnListEntry spawn = spawns.get(i);
			if (spawn.entityType == src) {
				spawns.add(new Biome.SpawnListEntry(dst, spawn.itemWeight, spawn.minGroupCount, spawn.maxGroupCount));
				spawns.remove(i);
				break;
			}
		}
	}
	
	@SubscribeEvent
	public static void registerEntityType(RegistryEvent.Register<EntityType<?>> event) {
		IForgeRegistry<EntityType<?>> registry = event.getRegistry();
		registry.register(sheep.setRegistryName("sheep"));
		registry.register(cow.setRegistryName("cow"));
		registry.register(chicken.setRegistryName("chicken"));
		registry.register(pig.setRegistryName("pig"));
		
		EntitySpawnPlacementRegistry.register(sheep, EntitySpawnPlacementRegistry.PlacementType.ON_GROUND, Heightmap.Type.MOTION_BLOCKING_NO_LEAVES, AnimalEntity::func_223316_b);
		EntitySpawnPlacementRegistry.register(cow, EntitySpawnPlacementRegistry.PlacementType.ON_GROUND, Heightmap.Type.MOTION_BLOCKING_NO_LEAVES, Cow::canSpawn);
		EntitySpawnPlacementRegistry.register(chicken, EntitySpawnPlacementRegistry.PlacementType.ON_GROUND, Heightmap.Type.MOTION_BLOCKING_NO_LEAVES, AnimalEntity::func_223316_b);
		EntitySpawnPlacementRegistry.register(pig, EntitySpawnPlacementRegistry.PlacementType.ON_GROUND, Heightmap.Type.MOTION_BLOCKING_NO_LEAVES, AnimalEntity::func_223316_b);
		
		for (Biome biome : ForgeRegistries.BIOMES) {
			replace(biome, EntityType.SHEEP, sheep);
			replace(biome, EntityType.COW, cow);
			replace(biome, EntityType.MOOSHROOM, cow);
			replace(biome, EntityType.CHICKEN, chicken);
			replace(biome, EntityType.PIG, pig);
			
			if (biome.getCategory() == Biome.Category.MUSHROOM) {
				biome.addFeature(GenerationStage.Decoration.VEGETAL_DECORATION, 
						Biome.createDecoratedFeature(allium, IFeatureConfig.NO_FEATURE_CONFIG, Placement.CHANCE_HEIGHTMAP_DOUBLE, new ChanceConfig(8)));
			}
		}
	}
	
	@SubscribeEvent
	public static void registerFeature(RegistryEvent.Register<Feature<?>> event) {
		event.getRegistry().register(allium.setRegistryName("allium"));
	}
	
	@SubscribeEvent
	public static void registerEffect(RegistryEvent.Register<Effect> event) {
		IForgeRegistry<Effect> registry = event.getRegistry();
		registry.register(growing.setRegistryName("growing"));
		registry.register(love.setRegistryName("love"));
		registry.register(selfing.setRegistryName("selfing"));
		registry.register(stable.setRegistryName("stable"));
		registry.register(mutable.setRegistryName("mutable"));
	}
	
	@SubscribeEvent
	public static void registerPotion(RegistryEvent.Register<Potion> event) {
		IForgeRegistry<Potion> registry = event.getRegistry();
		Potion growing = new Potion(new EffectInstance(ProjectGene.growing, 400)).setRegistryName("growing");
		registry.register(growing);
		PotionBrewing.addMix(Potions.AWKWARD, Items.KELP, growing);
		PotionBrewing.addMix(Potions.AWKWARD, Items.BAMBOO, growing);
		Potion long_growing = new Potion("growing", new EffectInstance(ProjectGene.growing, 1000)).setRegistryName("long_growing");
		registry.register(long_growing);
		PotionBrewing.addMix(growing, Items.REDSTONE, long_growing);
		Potion strong_growing = new Potion("growing", new EffectInstance(ProjectGene.growing, 250, 1)).setRegistryName("strong_growing");
		registry.register(strong_growing);
		PotionBrewing.addMix(growing, Items.GLOWSTONE_DUST, strong_growing);
		Potion creative_growing = new Potion("growing", new EffectInstance(ProjectGene.growing, 40, 59)).setRegistryName("creative_growing");
		registry.register(creative_growing);
		PotionBrewing.addMix(strong_growing, Blocks.DRAGON_EGG.asItem(), creative_growing);
		PotionBrewing.addMix(strong_growing, Blocks.DRAGON_HEAD.asItem(), creative_growing);
		Potion love = new Potion(new EffectInstance(ProjectGene.love)).setRegistryName("love");
		registry.register(love);
		PotionBrewing.addMix(Potions.AWKWARD, Items.GOLDEN_APPLE, love);
		Potion selfing = new Potion(new EffectInstance(ProjectGene.selfing)).setRegistryName("selfing");
		registry.register(selfing);
		PotionBrewing.addMix(love, Items.GHAST_TEAR, selfing);
		Potion mutable0 = new Potion(new EffectInstance(mutable, 300)).setRegistryName("mutable");
		registry.register(mutable0);
		PotionBrewing.addMix(Potions.THICK, bat, mutable0);
		addPotions(registry, mutable, mutable0, "mutable", bat);
		Potion stable0 = new Potion(new EffectInstance(stable, 300)).setRegistryName("stable");
		registry.register(stable0);
		PotionBrewing.addMix(Potions.THICK, Blocks.PACKED_ICE.asItem(), stable0);
		addPotions(registry, stable, stable0, "stable", Blocks.PACKED_ICE.asItem());
	}
	
	private static void addPotions(IForgeRegistry<Potion> registry, Effect effect, Potion base, String name, Item ingredient) {
		Potion potion;
		for (int i = 1; i < 10; ++i) {
			potion = new Potion(name, new EffectInstance(effect, 300, i)).setRegistryName(name + i);
			registry.register(potion);
			PotionBrewing.addMix(base, ingredient, potion);
			base = potion;
		}
		
	}
	
	
	@SubscribeEvent
	public static void registerDataSerializer(RegistryEvent.Register<DataSerializerEntry> event) {
		event.getRegistry().register(new DataSerializerEntry(gene_pool).setRegistryName("gene_pool"));
	}

}
