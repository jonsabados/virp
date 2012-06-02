package com.jshnd.virp;

import com.google.common.collect.Sets;
import com.jshnd.virp.config.*;
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
import static org.junit.Assert.assertSame;

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
	public void testNewSessionDefaults() {
		VirpSession session = createMock(VirpSession.class);
		Set<Class<?>> classes = Sets.newHashSet();
		expect(rowMapperSource.getRowMapperClasses()).andReturn(classes).once();
		VirpSessionSpec expected =
				new VirpSessionSpec(SessionAttachmentMode.NONE, NullColumnSaveBehavior.NO_COLUMN, false);
		expect(sessionFactory.newSession(testObj, expected)).andReturn(session).once();
		replay(rowMapperSource, sessionFactory);
		testObj.init();

		assertSame(session, testObj.newSession());
		verify(sessionFactory);
	}

	@Test
	public void testNewSessionFlushMode() {
		VirpSession session = createMock(VirpSession.class);
		Set<Class<?>> classes = Sets.newHashSet();
		VirpSessionSpec expected =
				new VirpSessionSpec(SessionAttachmentMode.MANUAL_FLUSH, NullColumnSaveBehavior.NO_COLUMN,
						false);
		expect(rowMapperSource.getRowMapperClasses()).andReturn(classes).once();
		expect(sessionFactory.newSession(testObj, expected)).andReturn(session).once();
		replay(rowMapperSource, sessionFactory);
		testObj.init();

		VirpSessionSpec spec =
				new VirpSessionSpec(testObj).withSessionAttachmentMode(SessionAttachmentMode.MANUAL_FLUSH);
		assertSame(session, testObj.newSession(spec));
		verify(sessionFactory);
	}

	@Test
	public void testNewSessionNullBehavior() {
		VirpSession session = createMock(VirpSession.class);
		Set<Class<?>> classes = Sets.newHashSet();
		VirpSessionSpec expected =
				new VirpSessionSpec(SessionAttachmentMode.NONE, NullColumnSaveBehavior.DO_NOTHING, false);
		expect(rowMapperSource.getRowMapperClasses()).andReturn(classes).once();
		expect(sessionFactory.newSession(testObj, expected)).andReturn(session).once();
		replay(rowMapperSource, sessionFactory);
		testObj.init();

		VirpSessionSpec spec =
				new VirpSessionSpec(testObj).withNullColumnSaveBehavior(NullColumnSaveBehavior.DO_NOTHING);
		assertSame(session, testObj.newSession(spec));
		verify(sessionFactory);
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
