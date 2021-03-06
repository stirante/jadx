package jadx.tests.integration.inner;

import jadx.core.dex.nodes.ClassNode;
import jadx.tests.api.IntegrationTest;

import java.io.File;
import java.io.FilenameFilter;

import org.junit.Test;

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.not;
import static org.junit.Assert.assertThat;

public class TestAnonymousClass extends IntegrationTest {

	public static class TestCls {

		public int test() {
			String[] files = new File("a").list((dir, name) -> name.equals("a"));
			return files.length;
		}
	}

	@Test
	public void test() {
		ClassNode cls = getClassNode(TestCls.class);
		String code = cls.getCode().toString();

		assertThat(code, containsString("new File(\"a\").list(new FilenameFilter()"));
		assertThat(code, not(containsString("synthetic")));
		assertThat(code, not(containsString("this")));
		assertThat(code, not(containsString("null")));
		assertThat(code, not(containsString("AnonymousClass_")));
	}
}
