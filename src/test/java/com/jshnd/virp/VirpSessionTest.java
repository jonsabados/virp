package com.jshnd.virp;

import com.google.common.collect.Sets;
import com.jshnd.virp.config.RowMapperMetaData;
import com.jshnd.virp.config.RowMapperMetaDataReader;
import com.jshnd.virp.config.RowMapperSource;
import com.jshnd.virp.config.dummyclasses.mapped.SomeClass;
import com.jshnd.virp.config.dummyclasses.mappedsubpackage.subpackage.MappedSubclass;
import org.easymock.EasyMock;
import org.junit.Before;
import org.junit.Test;

import java.util.Map;
import java.util.Set;

import static org.easymock.EasyMock.*;
import static org.junit.Assert.assertEquals;

public class VirpSessionTest {

	private VirpSession testObj;

	private RowMapperSource rowMapperSource;

	private RowMapperMetaDataReader metaReader;

	@Before
	public void setup() {
		testObj = new VirpSession();
		rowMapperSource = createMock(RowMapperSource.class);
		metaReader = EasyMock.createMock(RowMapperMetaDataReader.class);
		testObj.setRowMapperSource(rowMapperSource);
		testObj.setMetaDataReader(metaReader);
	}

	@Test
	public void testInit() {
		Set<Class<?>> classes = Sets.<Class<?>>newHashSet(MappedSubclass.class, SomeClass.class);
		expect(rowMapperSource.getRowMapperClasses()).andReturn(classes).once();
		RowMapperMetaData one = new RowMapperMetaData(MappedSubclass.class);
		RowMapperMetaData two = new RowMapperMetaData(SomeClass.class);
		expect(metaReader.readClass(MappedSubclass.class)).andReturn(one).once();
		expect(metaReader.readClass(SomeClass.class)).andReturn(two).once();
		replay(rowMapperSource, metaReader);

		testObj.init();
		Map<Class<?>, RowMapperMetaData> result = testObj.getConfiguredClasses();

		assertEquals(2, result.size());
		assertEquals(one, result.get(MappedSubclass.class));
		assertEquals(two, result.get(SomeClass.class));

		verify(rowMapperSource, metaReader);
	}

}
