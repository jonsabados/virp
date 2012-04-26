package com.jshnd.virp;

import com.jshnd.virp.config.RowMapperMetaData;
import com.jshnd.virp.exception.VirpOperationException;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

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

		TestSession(VirpConfig config) {
			super(config);
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
		protected VirpActionResult doClose() {
			closed = true;
			return createMock(VirpActionResult.class);
		}


	}

	private TestSession testObj;

	private VirpConfig config;

	@Rule
	public ExpectedException expectedException = ExpectedException.none();

	@Before
	public void setup() {
		config = createMock(VirpConfig.class);
		testObj = new TestSession(config);
	}

	@Test
	public void testSaveUnconfigured() {
		expectedException.expect(VirpOperationException.class);
		expectedException.expectMessage("Unconfigured class java.lang.Integer");

		testObj.save(Integer.valueOf(20));
	}

	@Test
	public void testSaveClosed() {
		expectedException.expect(VirpOperationException.class);
		expectedException.expectMessage("Session has been closed");

		testObj.close();

		testObj.save(Integer.valueOf(20));
	}

	@Test
	public void testSave() {
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
		expectedException.expect(VirpOperationException.class);
		expectedException.expectMessage("Unconfigured class java.lang.Integer");

		testObj.get(Integer.class, "20");
	}

	@Test
	public void testGetClosed() {
		expectedException.expect(VirpOperationException.class);
		expectedException.expectMessage("Session has been closed");

		testObj.close();

		testObj.get(Integer.class, Integer.valueOf(20));
	}

	@Test
	public void testGet() {
		RowMapperMetaData<Object> meta = createNiceMock(RowMapperMetaData.class);
		expect(config.getMetaData(Object.class)).andReturn(meta).once();
		replay(config);

		testObj.getReturns = new Object();

		Object result = testObj.get(Object.class, "foo");
		assertSame(testObj.getReturns, result);
	}

	@Test
	public void testGetAsListUnconfigured() {
		expectedException.expect(VirpOperationException.class);
		expectedException.expectMessage("Unconfigured class java.lang.Integer");

		testObj.get(Integer.class, "20", "20");
	}

	@Test
	public void testGetAsListClosed() {
		expectedException.expect(VirpOperationException.class);
		expectedException.expectMessage("Session has been closed");

		testObj.close();

		testObj.get(Integer.class, "foo", "bar");
	}

	@Test
	public void testGetAsList() {
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
		expectedException.expect(VirpOperationException.class);
		expectedException.expectMessage("Unconfigured class java.lang.Integer");

		testObj.getAsMap(Integer.class, "20", "20");
	}

	@Test
	public void testGetAsMapClosed() {
		expectedException.expect(VirpOperationException.class);
		expectedException.expectMessage("Session has been closed");

		testObj.close();

		testObj.getAsMap(Integer.class, "foo", "bar");
	}

	@Test
	public void testGetAsMap() {
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
		expectedException.expect(VirpOperationException.class);
		expectedException.expectMessage("Session has been closed");

		testObj.close();
		assertTrue(testObj.closed);
		testObj.close();
	}

}
