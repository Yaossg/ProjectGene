package project_gene.gene;

import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.datasync.IDataSerializer;
import net.minecraftforge.common.util.INBTSerializable;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.function.BiConsumer;

public class GenePool implements INBTSerializable<CompoundNBT> {
	private final Map<String, Genotype> pool = new TreeMap<>();

	public GenePool put(String geneType) {
		return put(geneType, Genotype.recessive);
	}

	public GenePool put(String geneType, Genotype genotype) {
		pool.put(geneType, genotype);
		return this;
	}
	
	public GenePool putAll(String... geneTypes) {
		for (String geneType : geneTypes) put(geneType);
		return this;
	}
	
	public GenePool put(Class<?> _interface) {
		Arrays.stream(_interface.getFields()).map(Field::getName).forEach(this::put);
		return this;
	}
	
	public GenePool putAll(Class<?>... interfaces) {
		for (Class<?> _interface : interfaces) {
			put(_interface);
		}
		return this;
	}
	
	public GenePool randomize(String geneType) {
		return put(geneType, Genotype.randomize());
	}

	public GenePool randomizeAll(String... geneTypes) {
		for (String geneType : geneTypes) randomize(geneType);
		return this;
	}
	
	public GenePool randomize(Class<?> _interface) {
		Arrays.stream(_interface.getFields()).map(Field::getName).forEach(this::randomize);
		return this;
	}
	
	
	public GenePool randomizeAll(Class<?>... interfaces) {
		for (Class<?> _interface : interfaces) {
			randomize(_interface);
		}
		return this;
	}

	public GenePool randomizeAll() {
		for (String geneType : geneTypes()) {
			randomize(geneType);
		}
		return this;
	}
	
	public Genotype get(String geneType) {
		return pool.get(geneType);
	}

	public int size() {
		return pool.size();
	}

	public Set<String> geneTypes() {
		return pool.keySet();
	}

	public void forEach(BiConsumer<? super String, ? super Genotype> action) {
		pool.forEach(action);
	}

	public boolean isSimilarTo(GenePool other) {
		if (other == null) return false;
		if (other == this) return true;
		return geneTypes().equals(other.geneTypes());
	}
	
	public static GenePool seg_comb_mut(GenePool P1, GenePool P2, double rate1, double rate2) {
		if (!P1.isSimilarTo(P2)) throw new IllegalArgumentException("P1 X P2 born nothing but the dead");
		GenePool child = P1.copy();
		P2.forEach((geneType, genotype) -> child.pool.merge(geneType, genotype, 
				(genotype1, genotype2) -> Genotype.combine(genotype1.seg_mut(rate1), genotype2.seg_mut(rate2))));
		return child;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		for (String name : pool.keySet())
			if (name.length() == 1)
				builder.append(pool.get(name).nameOf(name));
		for (String name : pool.keySet())
			if (name.length() > 1)
				builder.append(pool.get(name).nameOf(name));
		return builder.toString();
	}
	
	public GenePool copy() {
		GenePool copy = new GenePool();
		copy.pool.putAll(pool);
		return copy;
	}

	@Override
	public CompoundNBT serializeNBT() {
		CompoundNBT nbt = new CompoundNBT();
		forEach((geneType, genotype) -> nbt.putByte(geneType, (byte) genotype.ordinal()));
		return nbt;
	}

	@Override
	public void deserializeNBT(CompoundNBT nbt) {
		pool.clear();
		nbt.keySet().forEach(key -> pool.put(key, Genotype.values()[nbt.getByte(key)]));
	}

	public static class Serializer implements IDataSerializer<GenePool> {

		@Override
		public void write(PacketBuffer packetBuffer, GenePool genePool) {
			packetBuffer.writeVarInt(genePool.size());
			genePool.forEach((geneType, genotype) -> {
				packetBuffer.writeString(geneType);
				packetBuffer.writeEnumValue(genotype);
			});
		}

		@Override
		public GenePool read(PacketBuffer packetBuffer) {
			GenePool genePool = new GenePool();
			int size = packetBuffer.readVarInt();
			for (int i = 0; i < size; ++i) {
				String name = packetBuffer.readString(16);
				Genotype genotype = packetBuffer.readEnumValue(Genotype.class);
				genePool.put(name, genotype);
			}
			return genePool;
		}

		@Override
		public GenePool copyValue(GenePool genePool) {
			return genePool.copy();
		}
	}
}
