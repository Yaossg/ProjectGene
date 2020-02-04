package project_gene.item;

import net.minecraft.item.Food;
import net.minecraft.item.Item;
import project_gene.ProjectGene;

public class ItemSausage extends Item {
	public ItemSausage() {
		super(new Properties().group(ProjectGene.group)
				.food(new Food.Builder().hunger(10).saturation(1).setAlwaysEdible().build()));
	}
}
