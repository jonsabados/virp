package com.jshnd.virp;

import com.google.common.collect.Sets;
import com.jshnd.virp.config.RowMapperMetaData;
import com.jshnd.virp.config.RowMapperMetaDataReader;
import com.jshnd.virp.config.RowMapperSource;
import com.jshnd.virp.config.dummyclasses.mapped.SomeClass;
import com.jshnd.virp.config.dummyclasses.mappedsubpackage.subpackage.MappedSubclass;
import org.easymock.EasyMock;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.util.Map;
import java.util.Set;

import static org.easymock.EasyMock.*;
import static org.junit.Assert.assertEquals;

public class VirpSessionTest {

	private VirpSession testObj;

	private RowMapperSource rowMapperSource;

	private RowMapperMetaDataReader metaReader;

	private VirpActionFactory actionFactory;

	@Rule
	public ExpectedException expectedException = ExpectedException.none();

	@Before
	public void setup() {
		testObj = new VirpSession();
		rowMapperSource = createMock(RowMapperSource.class);
		metaReader = EasyMock.createMock(RowMapperMetaDataReader.class);
		actionFactory = EasyMock.createMock(VirpActionFactory.class);
		testObj.setRowMapperSource(rowMapperSource);
		testObj.setMetaDataReader(metaReader);
		testObj.setActionFactory(actionFactory);
	}

	@Test
	public void testWriteRowNotInitialized() {
		expectedException.expect(VirpException.class);
		expectedException.expectMessage("Session has not been initialized - call init() first.");

		testObj.writeRow(new SomeClass());
	}

	@Test
	public void testWriteRowUnconfigured() {
		expectedException.expect(VirpException.class);
		expectedException.expectMessage(Integer.class.getCanonicalName() + " has not been configured");
		Set<Class<?>> classes = Sets.<Class<?>>newHashSet(MappedSubclass.class, SomeClass.class);
		expect(rowMapperSource.getRowMapperClasses()).andReturn(classes).once();
		RowMapperMetaData one = new RowMapperMetaData(MappedSubclass.class);
		RowMapperMetaData two = new RowMapperMetaData(SomeClass.class);
		expect(metaReader.readClass(MappedSubclass.class)).andReturn(one).once();
		expect(metaReader.readClass(SomeClass.class)).andReturn(two).once();
		replay(rowMapperSource, metaReader);

		testObj.init();
		testObj.writeRow(Integer.valueOf(10));
	}

	@Test
	public void testWriteRow() {
		Set<Class<?>> classes = Sets.<Class<?>>newHashSet(MappedSubclass.class, SomeClass.class);
		expect(rowMapperSource.getRowMapperClasses()).andReturn(classes).once();
		RowMapperMetaData one = new RowMapperMetaData(MappedSubclass.class);
		RowMapperMetaData two = new RowMapperMetaData(SomeClass.class);
		expect(metaReader.readClass(MappedSubclass.class)).andReturn(one).once();
		expect(metaReader.readClass(SomeClass.class)).andReturn(two).once();
		replay(rowMapperSource, metaReader);

		testObj.init();

		SomeClass toWrite = new SomeClass();
		VirpAction writer = EasyMock.createMock(VirpAction.class);
		VirpActionResult result = EasyMock.createMock(VirpActionResult.class);
		expect(actionFactory.newAction()).andReturn(writer).once();

		writer.writeRow(toWrite, two);
		expectLastCall().once();
		expect(writer.complete()).andReturn(result).once();
		replay(writer, actionFactory, result);
		assertEquals(result, testObj.writeRow(toWrite));
		verify(writer, actionFactory, result);
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

	@Test
	public void testDuplicateInit() {
		expectedException.expect(VirpException.class);
		expectedException.expectMessage("Already initialized");
		Set<Class<?>> classes = Sets.<Class<?>>newHashSet(MappedSubclass.class, SomeClass.class);
		expect(rowMapperSource.getRowMapperClasses()).andReturn(classes).once();
		RowMapperMetaData one = new RowMapperMetaData(MappedSubclass.class);
		RowMapperMetaData two = new RowMapperMetaData(SomeClass.class);
		expect(metaReader.readClass(MappedSubclass.class)).andReturn(one).once();
		expect(metaReader.readClass(SomeClass.class)).andReturn(two).once();
		replay(rowMapperSource, metaReader);

		testObj.init();
		testObj.init();
	}

}
