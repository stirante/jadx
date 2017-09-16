package jadx.tests.integration.switches;

import jadx.core.dex.nodes.ClassNode;
import jadx.tests.api.IntegrationTest;

import org.junit.Test;

import static jadx.tests.api.utils.JadxMatchers.containsOne;
import static org.hamcrest.CoreMatchers.containsString;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

public class TestSwitchContinue extends IntegrationTest {

	public static class TestCls {
		public String test(int a) {
			StringBuilder s = new StringBuilder();
			while (a > 0) {
				switch (a % 4) {
					case 1:
						s.append("1");
						break;
					case 3:
					case 4:
						s.append("4");
						break;
					case 5:
						a -= 2;
						continue;
				}
				s.append("-");
				a--;
			}
			return s.toString();
		}
	}

	@Test
	public void test() {
		ClassNode cls = getClassNode(TestCls.class);
		String code = cls.getCode().toString();

		assertThat(code, containsString("switch (a % 4) {"));
		assertEquals(4, count(code, "case "));
		assertEquals(2, count(code, "break;"));

		assertThat(code, containsOne("a -= 2;"));
		assertThat(code, containsOne("continue;"));
	}
}
