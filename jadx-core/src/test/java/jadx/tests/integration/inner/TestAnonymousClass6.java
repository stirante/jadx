package jadx.tests.integration.inner;

import jadx.core.dex.nodes.ClassNode;
import jadx.tests.api.IntegrationTest;

import org.junit.Test;

import static jadx.tests.api.utils.JadxMatchers.containsOne;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.junit.Assert.assertThat;

public class TestAnonymousClass6 extends IntegrationTest {

	public static class TestCls {
		public Runnable test(final double d) {
			return () -> System.out.println(d);
		}
	}

	@Test
	public void test() {
		ClassNode cls = getClassNode(TestCls.class);
		String code = cls.getCode().toString();

		assertThat(code, containsOne("public Runnable test(final double d) {"));
		assertThat(code, containsOne("return new Runnable() {"));
		assertThat(code, containsOne("public void run() {"));
		assertThat(code, containsOne("System.out.println(d);"));
		assertThat(code, not(containsString("synthetic")));
	}
}
