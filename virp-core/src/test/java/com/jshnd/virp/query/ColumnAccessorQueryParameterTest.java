package com.jshnd.virp.query;

import com.jshnd.virp.ColumnAccessor;
import com.jshnd.virp.StaticValueAccessor;
import com.jshnd.virp.ValueManipulator;
import org.junit.Test;

import static org.easymock.EasyMock.*;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;

public class ColumnAccessorQueryParameterTest {

	@Test
	public void testConstruction() throws Exception {
		ColumnAccessor<Object, Object> accessor = createMock(ColumnAccessor.class);
		StaticValueAccessor<Object> identifier = createMock(StaticValueAccessor.class);
		ValueManipulator<Object> manipulator = createMock(ValueManipulator.class);
		Object example = new Object();
		Object arg = new Object();

		expect(accessor.getColumnIdentifier()).andReturn(identifier).once();
		expect(accessor.getValueManipulator()).andReturn(manipulator).once();
		expect(manipulator.getValue(example)).andReturn(arg).once();
		replay(accessor, identifier, manipulator);

		ColumnAccessorQueryParameter<Object, Object> testObj =
			new ColumnAccessorQueryParameter<Object, Object>(accessor, example, Criteria.EQUAL);

		verify(accessor, identifier, manipulator);
		assertSame(identifier, testObj.getColumnIdentifier());
		assertSame(manipulator, testObj.getSessionFactoryData());
		assertSame(arg, testObj.getArgument());
		assertEquals(Criteria.EQUAL, testObj.getCriteria());
	}

}
