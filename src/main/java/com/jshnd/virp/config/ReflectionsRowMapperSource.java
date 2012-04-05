package com.jshnd.virp.config;

import com.jshnd.virp.annotation.RowMapper;
import org.reflections.Reflections;
import org.reflections.util.ClasspathHelper;
import org.reflections.util.ConfigurationBuilder;
import org.reflections.util.FilterBuilder;

import java.util.Collection;

public class ReflectionsRowMapperSource implements RowMapperSource {

	private String basePackage;

	public Collection<Class<?>> getRowMapperClasses() {
		ConfigurationBuilder builder = new ConfigurationBuilder();
		builder.setUrls(ClasspathHelper.forJavaClassPath());
		builder.filterInputsBy(new FilterBuilder.Include(FilterBuilder.prefix(basePackage + ".")));
		Reflections reflections = new Reflections(builder);
		return reflections.getTypesAnnotatedWith(RowMapper.class);
	}

	public void setBasePackage(String basePackage) {
		this.basePackage = basePackage;
	}

}
