package project_gene.item;

import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.item.Food;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.EffectInstance;
import net.minecraft.potion.Effects;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;
import project_gene.ProjectGene;

import javax.annotation.Nullable;
import java.util.List;

public class ItemBat extends Item {
	public ItemBat() {
		super(new Properties().group(ProjectGene.group)
				.food(new Food.Builder().hunger(1).saturation(0.05F).setAlwaysEdible()
						.effect(new EffectInstance(Effects.POISON, 200, 3), 1)
						.effect(new EffectInstance(Effects.WITHER, 200, 3), 1)
						.effect(new EffectInstance(Effects.SLOWNESS, 300), 1)
						.effect(new EffectInstance(Effects.BLINDNESS, 600), 1)
						.effect(new EffectInstance(Effects.NAUSEA, 600), 1).build()));
	}

	@Override
	public void addInformation(ItemStack p_77624_1_, @Nullable World p_77624_2_, List<ITextComponent> p_77624_3_, ITooltipFlag p_77624_4_) {
		p_77624_3_.add(new TranslationTextComponent("project_gene.bat.tooltip"));
	}
}
