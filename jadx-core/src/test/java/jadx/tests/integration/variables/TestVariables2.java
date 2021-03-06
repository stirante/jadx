package jadx.tests.integration.variables;

import jadx.core.dex.nodes.ClassNode;
import jadx.tests.api.IntegrationTest;

import org.junit.Test;

import static org.hamcrest.CoreMatchers.containsString;
import static org.junit.Assert.assertThat;

public class TestVariables2 extends IntegrationTest {

	public static class TestCls {
		Object test(Object s) {
			Object store = s;
			if (store == null) {
				store = new Object();
				s = store;
			}
			return store;
		}
	}

	@Test
	public void test() {
		ClassNode cls = getClassNode(TestCls.class);
		String code = cls.getCode().toString();

		assertThat(code, containsString("Object store = s != null ? s : null;"));
	}
}
