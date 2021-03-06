package jadx.fxgui.ui.syntax;

import jadx.fxgui.utils.AsyncTask;
import org.fxmisc.richtext.CodeArea;
import org.fxmisc.richtext.model.StyleSpans;
import org.fxmisc.richtext.model.StyleSpansBuilder;

import java.util.Collection;
import java.util.Collections;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by stirante
 */
public class XmlSyntax extends BaseSyntax {

    static final BaseSyntax instance = new XmlSyntax();

    private static final Pattern XML_TAG = Pattern.compile("(?<ELEMENT>(</?\\h*)(\\w+)([^<>]*)(\\h*/?>))"
            + "|(?<COMMENT><!--[^<>]+-->)");

    private static final Pattern ATTRIBUTES = Pattern.compile("(\\w+\\h*)(=)(\\h*\"[^\"]+\")");

    private static final int GROUP_OPEN_BRACKET = 2;
    private static final int GROUP_ELEMENT_NAME = 3;
    private static final int GROUP_ATTRIBUTES_SECTION = 4;
    private static final int GROUP_CLOSE_BRACKET = 5;
    private static final int GROUP_ATTRIBUTE_NAME = 1;
    private static final int GROUP_EQUAL_SYMBOL = 2;
    private static final int GROUP_ATTRIBUTE_VALUE = 3;

    @Override
    public void computeHighlighting(CodeArea text) {
        final String str = text.getText();
        new AsyncTask<Void, Void, StyleSpans<Collection<String>>>() {
            @Override
            public StyleSpans<Collection<String>> doInBackground(Void[] params) {
                Matcher matcher = XML_TAG.matcher(str);
                int lastKwEnd = 0;
                StyleSpansBuilder<Collection<String>> spansBuilder = new StyleSpansBuilder<>();
                while (matcher.find()) {

                    spansBuilder.add(Collections.emptyList(), matcher.start() - lastKwEnd);
                    if (matcher.group("COMMENT") != null) {
                        spansBuilder.add(Collections.singleton("comment"), matcher.end() - matcher.start());
                    } else {
                        if (matcher.group("ELEMENT") != null) {
                            String attributesText = matcher.group(GROUP_ATTRIBUTES_SECTION);

                            spansBuilder.add(Collections.singleton("tagmark"), matcher.end(GROUP_OPEN_BRACKET) - matcher.start(GROUP_OPEN_BRACKET));
                            spansBuilder.add(Collections.singleton("anytag"), matcher.end(GROUP_ELEMENT_NAME) - matcher.end(GROUP_OPEN_BRACKET));

                            if (!attributesText.isEmpty()) {

                                lastKwEnd = 0;

                                Matcher amatcher = ATTRIBUTES.matcher(attributesText);
                                while (amatcher.find()) {
                                    spansBuilder.add(Collections.emptyList(), amatcher.start() - lastKwEnd);
                                    spansBuilder.add(Collections.singleton("attribute"), amatcher.end(GROUP_ATTRIBUTE_NAME) - amatcher.start(GROUP_ATTRIBUTE_NAME));
                                    spansBuilder.add(Collections.singleton("tagmark"), amatcher.end(GROUP_EQUAL_SYMBOL) - amatcher.end(GROUP_ATTRIBUTE_NAME));
                                    spansBuilder.add(Collections.singleton("avalue"), amatcher.end(GROUP_ATTRIBUTE_VALUE) - amatcher.end(GROUP_EQUAL_SYMBOL));
                                    lastKwEnd = amatcher.end();
                                }
                                if (attributesText.length() > lastKwEnd)
                                    spansBuilder.add(Collections.emptyList(), attributesText.length() - lastKwEnd);
                            }

                            lastKwEnd = matcher.end(GROUP_ATTRIBUTES_SECTION);

                            spansBuilder.add(Collections.singleton("tagmark"), matcher.end(GROUP_CLOSE_BRACKET) - lastKwEnd);
                        }
                    }
                    lastKwEnd = matcher.end();
                }
                spansBuilder.add(Collections.emptyList(), str.length() - lastKwEnd);
                return spansBuilder.create();
            }

            @Override
            public void onPostExecute(StyleSpans<Collection<String>> result) {
                text.setStyleSpans(0, result);
            }
        }.execute();
    }
}
