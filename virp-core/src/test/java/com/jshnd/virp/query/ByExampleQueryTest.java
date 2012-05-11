package com.jshnd.virp.query;

import com.jshnd.virp.ColumnAccessor;
import com.jshnd.virp.ValueManipulator;
import com.jshnd.virp.config.RowMapperMetaData;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import static org.easymock.EasyMock.expect;
import static org.junit.Assert.*;
import static org.powermock.api.easymock.PowerMock.*;

@RunWith(PowerMockRunner.class)
@PrepareForTest({ByExampleQuery.class})
public class ByExampleQueryTest {

	@Test
	public void testConstruction() throws Exception {
		RowMapperMetaData<Object> meta = createMock(RowMapperMetaData.class);
		ColumnAccessor<Object, Object> notNull = createMock(ColumnAccessor.class);
		ColumnAccessor<Object, Object> returnsNull = createMock(ColumnAccessor.class);
		ValueManipulator<Object> forNotNull = createMock(ValueManipulator.class);
		ValueManipulator<Object> forNull = createMock(ValueManipulator.class);

		Object example = new Object();
		Set<ColumnAccessor<?, ?>> accessors = new HashSet<ColumnAccessor<?, ?>>();
		accessors.add(notNull);
		accessors.add(returnsNull);

		expect(meta.getColumnAccessors()).andReturn(accessors).once();
		expect(notNull.getValueManipulator()).andReturn(forNotNull).once();
		expect(returnsNull.getValueManipulator()).andReturn(forNull).once();
		expect(forNotNull.getValue(example)).andReturn(new Object()).once();
		expect(forNull.getValue(example)).andReturn(null).once();

		ColumnAccessorQueryParameter param = createMock(ColumnAccessorQueryParameter.class);
		expectNew(ColumnAccessorQueryParameter.class, notNull, example, Criteria.EQUAL).andReturn(param).once();

		replay(ColumnAccessorQueryParameter.class, meta, notNull, returnsNull, forNotNull, forNull, param);

		ByExampleQuery<Object> testObj = new ByExampleQuery<Object>(meta, example);

		verify(meta, notNull, returnsNull, forNotNull, forNull, param, ColumnAccessorQueryParameter.class);
		assertSame(meta, testObj.getMeta());
	   	Collection<QueryParameter<?, ?>> params = testObj.getParameters();
		assertNotNull(params);
		assertEquals(Integer.valueOf(1), Integer.valueOf(params.size()));
		assertTrue(params.contains(param));
	}

}
