package project_gene.hook;

import net.minecraft.entity.AgeableEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.entity.passive.AnimalEntity;
import net.minecraft.entity.passive.BatEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.text.StringTextComponent;
import net.minecraftforge.event.entity.living.LivingDropsEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import project_gene.ProjectGene;
import project_gene.entity.core.IGenePooled;
import project_gene.gene.GenePool;

@Mod.EventBusSubscriber(modid = ProjectGene.MODID, 
		bus = Mod.EventBusSubscriber.Bus.FORGE)
public class VanillaHook {
	
	public static boolean selfing(Entity entity) {
		if (entity instanceof AnimalEntity) {
			AgeableEntity ageable = (AgeableEntity) entity;
			if (ageable.getGrowingAge() == 0) {
				AgeableEntity child = ageable.createChild(ageable);
				if (child != null) {
					ageable.setGrowingAge(6000);
					child.setGrowingAge(-24000);
					child.setLocationAndAngles(ageable.posX, ageable.posY, ageable.posZ, 0, 0);
					ageable.world.addEntity(child);
					ageable.world.setEntityState(ageable, (byte) 18);
					return true;
				}
			}
		}
		return false;
	}

	@SubscribeEvent
	public static void interact(PlayerInteractEvent.EntityInteract event) {
		ItemStack stack = event.getItemStack();
		Entity entity = event.getTarget();
		PlayerEntity player = event.getPlayer();
		if (stack.getItem() == Items.GHAST_TEAR && selfing(entity) && !player.isCreative()) 
			stack.shrink(1);
		if (stack.getItem() == Items.DEBUG_STICK && entity instanceof IGenePooled) {
			IGenePooled genePooled = (IGenePooled) entity;
			GenePool genePool = genePooled.getDefaultGenePool().randomizeAll();
			genePooled.setGenePool(genePool);
			player.sendStatusMessage(new StringTextComponent(genePool.toString()), true);
		}
	}
	
	@SubscribeEvent
	public static void drop(LivingDropsEvent event) {
		Entity entity = event.getEntity();
		if (entity instanceof BatEntity) {
			event.getDrops().add(new ItemEntity(entity.world, entity.posX, entity.posY, entity.posZ, new ItemStack(ProjectGene.bat)));
		}
	}
}
