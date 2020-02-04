package project_gene.item;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Hand;
import project_gene.ProjectGene;

import java.util.function.Predicate;

public class EntityProcessorItem<E> extends Item {
	public interface InteractProcessor<E> {
		void process(PlayerEntity player, E living);
	}
	private final Predicate<LivingEntity> filter;
	private final InteractProcessor<E> processor;
	
	private EntityProcessorItem(Predicate<LivingEntity> filter, InteractProcessor<E> processor) {
		super(new Properties().group(ProjectGene.group));
		this.filter = filter;
		this.processor = processor;
	}
	
	public static <E> EntityProcessorItem<E> of(Predicate<LivingEntity> filter, InteractProcessor<E> processor) {
		return new EntityProcessorItem<>(filter, processor);
	}

	public static <E> EntityProcessorItem<E> of(Class<?> filter, InteractProcessor<E> processor) {
		return new EntityProcessorItem<>(filter::isInstance, processor);
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public boolean itemInteractionForEntity(ItemStack p_111207_1_, PlayerEntity p_111207_2_, LivingEntity p_111207_3_, Hand p_111207_4_) {
		if (!filter.test(p_111207_3_)) return false;
		processor.process(p_111207_2_, (E) p_111207_3_);
		return true;
	}
}
