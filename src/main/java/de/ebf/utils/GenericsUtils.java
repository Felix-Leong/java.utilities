package de.ebf.utils;

import java.lang.reflect.ParameterizedType;

public class GenericsUtils<T> {
	
	@SuppressWarnings("unchecked")
	protected Class<T> getGenericSuperClass(Class<?> baseClass){
		Class<?> cl = getClass();
		while (!baseClass.getSimpleName().equals(cl.getSuperclass().getSimpleName())) {
			// case of multiple inheritance, we are trying to get the first available generic info
			if (cl.getGenericSuperclass() instanceof ParameterizedType) {
				break;
			}
			cl = cl.getSuperclass();
		}
		if (cl.getGenericSuperclass() instanceof ParameterizedType) {
			return (Class<T>) ((ParameterizedType) cl.getGenericSuperclass()).getActualTypeArguments()[0];
		}
		//this shouldn't happen
		throw new RuntimeException("Could not get generic superclass for base class "+baseClass.getSimpleName());
	}
	
	protected String getGenericSuperClassName(Class<?> baseClass){	
		return getGenericSuperClass(baseClass).getSimpleName();
	}

	protected T getGenericSuperClassInstance(Class<?> baseClass) {
		Class<T> clazz = getGenericSuperClass(baseClass);
		try {
			return clazz.newInstance();
		} catch (InstantiationException | IllegalAccessException e) {
			throw new RuntimeException("Unable to instantiate model class ("+clazz.getSimpleName()+") using reflection "+e);
		}
	}
}
