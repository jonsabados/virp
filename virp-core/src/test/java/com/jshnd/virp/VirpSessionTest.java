package com.jshnd.virp;

import com.jshnd.virp.config.RowMapperMetaData;
import com.jshnd.virp.exception.VirpOperationException;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.math.BigDecimal;

import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.junit.Assert.assertTrue;

public class VirpSessionTest {

	private static class TestSession extends VirpSession {

		boolean saveDone = false;

		boolean closed = false;

		TestSession(RowMapperMetaData metaData) {
			super(metaData);
		}

		@Override
		protected void doSave(Object row) {
			saveDone = true;
		}

		@Override
		protected VirpActionResult doClose() {
			closed = true;
			return createMock(VirpActionResult.class);
		}
	}

	private TestSession testObj;

	private RowMapperMetaData meta;

	@Rule
	public ExpectedException expectedException = ExpectedException.none();

	@Before
	public void setup() {
		meta = createMock(RowMapperMetaData.class);
		testObj = new TestSession(meta);
	}

	@Test
	public void testSaveUnknownClass() {
		expectedException.expect(VirpOperationException.class);
		expectedException.expectMessage("May only operate on objects of type java.math.BigDecimal");

		expect(meta.getRowMapperClass()).andReturn((Class) BigDecimal.class).anyTimes();
		replay(meta);

		testObj.save(Integer.valueOf(20));
	}

	@Test
	public void testClose() {
		expectedException.expect(VirpOperationException.class);
		expectedException.expectMessage("Session has been closed");

		replay(meta);
		testObj.close();
		assertTrue(testObj.closed);
		testObj.close();
	}

}
