package com.jshnd.virp.config;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.Collection;

import org.junit.Before;
import org.junit.Test;

import com.jshnd.virp.config.ReflectionsRowMapperSource;
import com.jshnd.virp.config.dummyclasses.mapped.SomeClass;
import com.jshnd.virp.config.dummyclasses.mapped.SomeOtherClass;
import com.jshnd.virp.config.dummyclasses.mappedsubpackage.subpackage.MappedSubclass;

public class ReflectionsRowMapperSourceTest {

	private ReflectionsRowMapperSource testObj;
	
	@Before
	public void setup() {
		testObj = new ReflectionsRowMapperSource();
	}
	
	@Test
	public void testGetRowMapperClasses() {
		testObj.setBasePackage("com.jshnd.casrom.config.dummyclasses.mapped");
		Collection<Class<?>> classes = testObj.getRowMapperClasses();
		assertEquals("More classes than expected" + classes, 2, classes.size());
		assertTrue(classes.contains(SomeClass.class));
		assertTrue(classes.contains(SomeOtherClass.class));
	}

	@Test
	public void testGetRowMapperSubpackageClasses() {
		testObj.setBasePackage("com.jshnd.casrom.config.dummyclasses.mappedsubpackage");
		Collection<Class<?>> classes = testObj.getRowMapperClasses();
		assertEquals(1, classes.size());
		assertTrue(classes.contains(MappedSubclass.class));
	}
	
}
