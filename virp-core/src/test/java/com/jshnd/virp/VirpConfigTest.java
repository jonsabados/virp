package com.jshnd.virp;

import com.google.common.collect.Sets;
import com.jshnd.virp.config.RowMapperMetaData;
import com.jshnd.virp.config.RowMapperMetaDataReader;
import com.jshnd.virp.config.RowMapperSource;
import com.jshnd.virp.config.dummyclasses.mapped.SomeClass;
import com.jshnd.virp.config.dummyclasses.mappedsubpackage.subpackage.MappedSubclass;
import com.jshnd.virp.exception.VirpException;
import org.easymock.EasyMock;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.util.Map;
import java.util.Set;

import static org.easymock.EasyMock.*;
import static org.junit.Assert.assertEquals;

public class VirpConfigTest {

	private VirpConfig testObj;

	private RowMapperSource rowMapperSource;

	private RowMapperMetaDataReader metaReader;

	private VirpSessionFactory sessionFactory;

	@Rule
	public ExpectedException expectedException = ExpectedException.none();

	@Before
	public void setup() {
		testObj = new VirpConfig();
		rowMapperSource = createMock(RowMapperSource.class);
		metaReader = EasyMock.createMock(RowMapperMetaDataReader.class);
		sessionFactory = EasyMock.createMock(VirpSessionFactory.class);
		testObj.setRowMapperSource(rowMapperSource);
		testObj.setMetaDataReader(metaReader);
		testObj.setSessionFactory(sessionFactory);
	}

	@Test
	public void testNewSessionNotInitialized() {
		expectedException.expect(VirpException.class);
		expectedException.expectMessage("Session has not been initialized - call init() first.");

		testObj.newSession();
	}

	@Test
	public void testInit() {
		Set<Class<?>> classes = Sets.newHashSet(MappedSubclass.class, SomeClass.class);
		expect(rowMapperSource.getRowMapperClasses()).andReturn(classes).once();
		RowMapperMetaData<MappedSubclass> one = new RowMapperMetaData<MappedSubclass>(MappedSubclass.class);
		RowMapperMetaData<SomeClass> two = new RowMapperMetaData<SomeClass>(SomeClass.class);
		expect(metaReader.readClass(MappedSubclass.class)).andReturn(one).once();
		expect(metaReader.readClass(SomeClass.class)).andReturn(two).once();
		sessionFactory.setupClass(one);
		expectLastCall();
		sessionFactory.setupClass(two);
		expectLastCall();
		replay(rowMapperSource, metaReader, sessionFactory);

		testObj.init();
		Map<Class<?>, RowMapperMetaData<?>> result = testObj.getConfiguredClasses();

		assertEquals(2, result.size());
		assertEquals(one, result.get(MappedSubclass.class));
		assertEquals(two, result.get(SomeClass.class));

		verify(rowMapperSource, metaReader, sessionFactory);
	}

	@Test
	public void testDuplicateInit() {
		expectedException.expect(VirpException.class);
		expectedException.expectMessage("Already initialized");
		Set<Class<?>> classes = Sets.newHashSet(MappedSubclass.class, SomeClass.class);
		expect(rowMapperSource.getRowMapperClasses()).andReturn(classes).once();
		RowMapperMetaData<MappedSubclass> one = new RowMapperMetaData<MappedSubclass>(MappedSubclass.class);
		RowMapperMetaData<SomeClass> two = new RowMapperMetaData<SomeClass>(SomeClass.class);
		expect(metaReader.readClass(MappedSubclass.class)).andReturn(one).once();
		expect(metaReader.readClass(SomeClass.class)).andReturn(two).once();
		replay(rowMapperSource, metaReader);

		testObj.init();
		testObj.init();
	}

}
