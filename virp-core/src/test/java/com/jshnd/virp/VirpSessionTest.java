package com.jshnd.virp;

import com.google.common.collect.Iterables;
import com.google.common.collect.Sets;
import com.jshnd.virp.annotation.AnnotationDrivenRowMapperMetaDataReader;
import com.jshnd.virp.annotation.Key;
import com.jshnd.virp.annotation.NamedColumn;
import com.jshnd.virp.annotation.RowMapper;
import com.jshnd.virp.config.ConfiguredRowMapperSource;
import com.jshnd.virp.config.RowMapperMetaData;
import com.jshnd.virp.config.SessionAttachmentMode;
import com.jshnd.virp.exception.VirpOperationException;
import com.jshnd.virp.query.Query;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.util.*;

import static org.easymock.EasyMock.*;
import static org.junit.Assert.*;

public class VirpSessionTest {

	@SuppressWarnings("unchecked")
	private static class TestSession extends VirpSession {

		boolean saveDone = false;

		boolean closed = false;

		boolean getDone = false;

		private Object getReturns;

		private List<Object> getManyReturns;

		private Map<Object, Set<ColumnAccessor<?, ?>>> modifications = new HashMap<Object, Set<ColumnAccessor<?, ?>>>();

		TestSession(VirpConfig config, SessionAttachmentMode mode) {
			super(config, mode);
		}

		@Override
		protected <T> void doSave(RowMapperMetaData<T> type, T row) {
			saveDone = true;
		}

		@Override
		protected <T, K> T doGet(RowMapperMetaData<T> type, K key) {
			getDone = true;
			return (T) getReturns;
		}

		@Override
		protected <T, K> List<T> doGet(RowMapperMetaData<T> type, K... keys) {
			return (List<T>) getManyReturns;
		}

		@Override
		protected <T> List<T> doFind(Query<T> query, RowMapperMetaData<T> meta) {
			return (List<T>) getManyReturns;
		}

		@Override
		protected VirpActionResult doFlush() {
			closed = true;
			return createMock(VirpActionResult.class);
		}

		@Override
		protected <T> void doChange(RowMapperMetaData<T> type, T toChange,
									ColumnAccessor<?, ?> columnAccessor) {
			if(!modifications.containsKey(toChange)) {
				modifications.put(toChange, new HashSet<ColumnAccessor<?, ?>>());
			}
			modifications.get(toChange).add(columnAccessor);
		}
	}

	private TestSession testObj;

	private VirpConfig config;

	@Rule
	public ExpectedException expectedException = ExpectedException.none();

	@Before
	public void setup() {
		testObj = null;
		config = null;
	}

	private void basicSetup() {
		config = createMock(VirpConfig.class);
		testObj = new TestSession(config, SessionAttachmentMode.NONE);
	}

	@Test
	public void testSaveUnconfigured() {
		basicSetup();
		expectedException.expect(VirpOperationException.class);
		expectedException.expectMessage("Unconfigured class java.lang.Integer");

		testObj.save(Integer.valueOf(20));
	}

	@Test
	public void testSaveClosed() {
		basicSetup();
		expectedException.expect(VirpOperationException.class);
		expectedException.expectMessage("Session has been closed");

		testObj.close();

		testObj.save(Integer.valueOf(20));
	}

	@Test
	public void testSave() {
		basicSetup();
		expectedException.expect(VirpOperationException.class);
		expectedException.expectMessage("Session has been closed");

		RowMapperMetaData<Integer> meta = createNiceMock(RowMapperMetaData.class);
		expect(config.getMetaData(Integer.class)).andReturn(meta).once();
		replay(config);

		testObj.save(Integer.valueOf(20));
		assertTrue(testObj.saveDone);
		testObj.close();
		testObj.save(Integer.valueOf(21));
	}

	@Test
	public void testGetUnconfigured() {
		basicSetup();
		expectedException.expect(VirpOperationException.class);
		expectedException.expectMessage("Unconfigured class java.lang.Integer");

		testObj.get(Integer.class, "20");
	}

	@Test
	public void testGetClosed() {
		basicSetup();
		expectedException.expect(VirpOperationException.class);
		expectedException.expectMessage("Session has been closed");

		testObj.close();

		testObj.get(Integer.class, Integer.valueOf(20));
	}

	@Test
	public void testGet() {
		basicSetup();
		RowMapperMetaData<Object> meta = createNiceMock(RowMapperMetaData.class);
		expect(config.getMetaData(Object.class)).andReturn(meta).once();
		replay(config);

		testObj.getReturns = new Object();

		Object result = testObj.get(Object.class, "foo");
		assertSame(testObj.getReturns, result);
	}

	@Test
	public void testGetAsListUnconfigured() {
		basicSetup();
		expectedException.expect(VirpOperationException.class);
		expectedException.expectMessage("Unconfigured class java.lang.Integer");

		testObj.get(Integer.class, "20", "20");
	}

	@Test
	public void testGetAsListClosed() {
		basicSetup();
		expectedException.expect(VirpOperationException.class);
		expectedException.expectMessage("Session has been closed");

		testObj.close();

		testObj.get(Integer.class, "foo", "bar");
	}

	@Test
	public void testGetAsList() {
		basicSetup();
		RowMapperMetaData<Object> meta = createMock(RowMapperMetaData.class);
		expect(config.getMetaData(Object.class)).andReturn(meta).once();
		replay(config, meta);

		Object foo = new Object();
		Object bar = new Object();
		testObj.getManyReturns = Arrays.asList(foo, bar);

		List result = testObj.get(Object.class, "foo", "bar");
		assertSame(testObj.getManyReturns, result);
		assertEquals(2, result.size());
		assertSame(foo, result.get(0));
		assertSame(bar, result.get(1));
		verify(config, meta);
	}

	@Test
	public void testGetAsMapUnconfigured() {
		basicSetup();
		expectedException.expect(VirpOperationException.class);
		expectedException.expectMessage("Unconfigured class java.lang.Integer");

		testObj.getAsMap(Integer.class, "20", "20");
	}

	@Test
	public void testGetAsMapClosed() {
		basicSetup();
		expectedException.expect(VirpOperationException.class);
		expectedException.expectMessage("Session has been closed");

		testObj.close();

		testObj.getAsMap(Integer.class, "foo", "bar");
	}

	@Test
	public void testGetAsMap() {
		basicSetup();

		Object foo = new Object();
		Object bar = new Object();
		RowMapperMetaData<Object> meta = createMock(RowMapperMetaData.class);
		ValueManipulator<String> keyAccessor = createMock(ValueManipulator.class);
		expect(meta.<String>getKeyValueManipulator()).andReturn(keyAccessor).once();
		expect(keyAccessor.getValue(foo)).andReturn("foo").once();
		expect(keyAccessor.getValue(bar)).andReturn("bar").once();
		expect(config.getMetaData(Object.class)).andReturn(meta).once();
		replay(config, meta, keyAccessor);

		testObj.getManyReturns = Arrays.asList(foo, bar);

		Map<String, Object> result = testObj.getAsMap(Object.class, "foo", "bar");

		assertEquals(2, result.size());
		assertSame(foo, result.get("foo"));
		assertSame(bar, result.get("bar"));
		verify(config, meta, keyAccessor);
	}

	@Test
	public void testClose() {
		basicSetup();
		expectedException.expect(VirpOperationException.class);
		expectedException.expectMessage("Session has been closed");

		testObj.close();
		assertTrue(testObj.closed);
		testObj.close();
	}

	@RowMapper(columnFamily = "someFamily")
	public static class ProxiedGetTester {

		@Key
		private String key;

		@NamedColumn(name = "foo")
		private String valueOne;

		@NamedColumn(name = "bar")
		private String valueTwo;

		private String notMapped;

		public String getKey() {
			return key;
		}

		public void setKey(String key) {
			this.key = key;
		}

		public String getValueOne() {
			return valueOne;
		}

		public void setValueOne(String valueOne) {
			this.valueOne = valueOne;
		}

		public String getValueTwo() {
			return valueTwo;
		}

		public void setValueTwo(String valueTwo) {
			this.valueTwo = valueTwo;
		}

		public String getNotMapped() {
			return notMapped;
		}

		public void setNotMapped(String notMapped) {
			this.notMapped = notMapped;
		}
	}

	private void setupProxied() {
		VirpSessionFactory factory = createMock(VirpSessionFactory.class);
		VirpConfig config = new VirpConfig();
		config.setSessionFactory(factory);
		ConfiguredRowMapperSource source = new ConfiguredRowMapperSource();
		source.setRowMapperClasses(Sets.<Class<?>>newHashSet(ProxiedGetTester.class));
		config.setMetaDataReader(new AnnotationDrivenRowMapperMetaDataReader());
		config.setRowMapperSource(source);
		config.init();

		testObj = new TestSession(config, SessionAttachmentMode.MANUAL_FLUSH);
	}

	@Test
	public void testProxiedGet() throws Exception {
		setupProxied();
		ProxiedGetTester testRow = new ProxiedGetTester();
		testRow.setKey("abc");
		testRow.setValueOne("one");
		testRow.setValueTwo("two");
		testObj.getReturns = testRow;

		ProxiedGetTester result = testObj.get(ProxiedGetTester.class, "abc");

		runProxyTests(result, testRow);
	}

	@Test
	public void testProxiedList() throws Exception {
		setupProxied();
		ProxiedGetTester testRow1 = new ProxiedGetTester();
		testRow1.setKey("abc");
		testRow1.setValueOne("one");
		testRow1.setValueTwo("two");
		ProxiedGetTester testRow2 = new ProxiedGetTester();
		testRow2.setKey("dabc");
		testRow2.setValueOne("oned");
		testRow2.setValueTwo("twoo");
		testObj.getManyReturns = Arrays.<Object>asList(testRow1, testRow2);

		List<ProxiedGetTester> result = testObj.get(ProxiedGetTester.class, "abc", "dabc");

		assertEquals(2, result.size());
		runProxyTests(result.get(0), testRow1);
		runProxyTests(result.get(1), testRow2);
	}

	private void runProxyTests(ProxiedGetTester fromSession, ProxiedGetTester base) {

		assertNotSame(fromSession, base);

		assertEquals(base.getKey(), fromSession.getKey());
		assertEquals(base.getValueOne(), fromSession.getValueOne());
		assertEquals(base.getValueTwo(), fromSession.getValueTwo());

		fromSession.setValueOne(base.getKey() + "Modification");
		fromSession.setNotMapped("shouldntmatter");

		Map<Object, Set<ColumnAccessor<?, ?>>> modifications = testObj.modifications;
		assertTrue(modifications.containsKey(base));
		Set<ColumnAccessor<?,?>> modified = modifications.get(base);
		assertNotNull(modified);
		assertEquals(1, modified.size());
		ColumnAccessor<?, ?> column = Iterables.getFirst(modified, null);
		assertNotNull(column);
		assertEquals("foo", column.getColumnIdentifier().getValue());
		assertEquals(base.getKey() + "Modification", column.getValueManipulator().getValue(base));

		boolean exceptionCaught = false;
		try {
			fromSession.setKey("-NONONONO-");
		} catch(VirpOperationException e) {
			assertEquals("Attempt to modify key for "
					+ ProxiedGetTester.class.getCanonicalName()
					+ ", key value: " + base.getKey(), e.getMessage());
			exceptionCaught = true;
		}
		assertTrue(exceptionCaught);
	}

}
