package project_gene.reflect;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

public class TrickyReflect {
	static Constructor<MethodHandles.Lookup> Lookup_constructor;
	static {
		try {
			Lookup_constructor = MethodHandles.Lookup.class.getDeclaredConstructor(Class.class);
			Lookup_constructor.setAccessible(true);
		} catch (NoSuchMethodException e) {
			throw new AssertionError(e);
		}
	}
	
	public static MethodHandle ancestor(Class<?> ancestor, String name, MethodType methodType) 
			throws InstantiationException, IllegalAccessException, InvocationTargetException, NoSuchMethodException {
		return Lookup_constructor.newInstance(ancestor).findSpecial(ancestor, name, methodType, ancestor);
	}
	
	public static MethodHandle ancestorEnsured(Class<?> ancestor, String name, MethodType methodType) {
		try {
			return ancestor(ancestor, name, methodType);
		} catch (InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
			throw new AssertionError(e);
		}
	}

}
