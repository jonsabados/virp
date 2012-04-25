package com.jshnd.virp;

import com.jshnd.virp.config.RowMapperMetaData;
import com.jshnd.virp.exception.VirpOperationException;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.util.List;

import static org.easymock.EasyMock.*;
import static org.junit.Assert.assertTrue;

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
	public void testSaveUnconfigured() {
		expectedException.expect(VirpOperationException.class);
		expectedException.expectMessage("Unconfigured class java.lang.Integer");

		testObj.save(Integer.valueOf(20));
		assertTrue(testObj.saveDone);
		testObj.close();
		testObj.save(Integer.valueOf(21));
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
