package com.jshnd.virp;

import com.jshnd.virp.config.RowMapperMetaData;
import com.jshnd.virp.exception.VirpOperationException;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import static org.easymock.EasyMock.createMock;
import static org.junit.Assert.assertTrue;

public class VirpSessionTest {

	private static class TestSession extends VirpSession<Object> {

		boolean saveDone = false;

		boolean closed = false;

		boolean getDone = false;

		TestSession(RowMapperMetaData<Object> metaData) {
			super(metaData);
		}

		@Override
		protected void doSave(Object row) {
			saveDone = true;
		}

		@Override
		protected Object doGet(Object key) {
			getDone = true;
			return new Object();
		}

		@Override
		protected VirpActionResult doClose() {
			closed = true;
			return createMock(VirpActionResult.class);
		}
	}

	private TestSession testObj;

	private RowMapperMetaData<Object> meta;

	@Rule
	public ExpectedException expectedException = ExpectedException.none();

	@Before
	public void setup() {
		meta = new RowMapperMetaData<Object>(Object.class);
		testObj = new TestSession(meta);
	}

	@Test
	public void testSave() {
		expectedException.expect(VirpOperationException.class);
		expectedException.expectMessage("Session has been closed");

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
