package com.vlcnavigation.module.utils;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;

public class XmlParser {

    /**
     * Reminder of terminology: (source: https://en.wikipedia.org/wiki/XML#Key_terminology)
     *
     * - Tag: construct beginning with '<' and ending with '>'. Exists with 3 different forms:
     *       - start-tag, such as <section>
     *       - end-tag, such as </section>
     *       - empty-element, such as <section/>
     * - Element: logical component, either:
     *       - starting with a start-tag and ending with an end-tag, including the content inside the tags, such as <id>15</id>
     *       - an empty-element tag, such as <person id="15"/>
     * - Attribute: name-value pair existing within a start-tag or an empty-element tag, such as src="https://en.wikipedia.org/wiki/XML#Tag"
     * - XML declaration: a special tag describing the XML format and giving some informations
     */

    protected static final String ns = null;
    protected static XmlPullParser parser;

    /**
     * Skip to the next tag
     *
     * @throws XmlPullParserException
     * @throws IOException
     */
    protected static void skip() throws XmlPullParserException, IOException {
        if (parser.getEventType() != XmlPullParser.START_TAG) {
            throw new IllegalStateException();
        }
        int depth = 1;
        while (depth != 0) {
            switch (parser.next()) {
                case XmlPullParser.END_TAG:
                    depth--;
                    break;
                case XmlPullParser.START_TAG:
                    depth++;
                    break;
            }
        }
    }



    /**
     * Reads the content of an attribute inside an empty-element tag. For example:
     * readCompressedXml(parser, "entry", "id") on XML line `<entry id="myEntry" description="another attribute"/>` will return "myEntry".
     *
     * @param beacon String
     * @param attribute String attribute to read
     * @return Content of the String attribute to read
     * @throws IOException
     * @throws XmlPullParserException
     */
    protected static String readCompressedXml(String beacon, String attribute) throws IOException, XmlPullParserException{
        parser.require(XmlPullParser.START_TAG, ns, beacon);
        String content = parser.getAttributeValue(null, attribute);
        parser.nextTag();
        parser.require(XmlPullParser.END_TAG, ns, beacon);
        return content;
    }

    /**
     * Reads the content of the next XML non-empty element
     * readFullXml(parser, "entry") on XML line `<entry>Something here</entry> will return "Something here".
     *
     * @param beacon String
     * @return Content of the String attribute to read
     * @throws IOException
     * @throws XmlPullParserException
     */
    protected static String readFullXml(String beacon) throws IOException, XmlPullParserException{
        parser.require(XmlPullParser.START_TAG, ns, beacon);
        String content = readText();
        parser.require(XmlPullParser.END_TAG, ns, beacon);
        return content;
    }

    /**
     * Reads the content of the next XML non-empty element
     *
     * @return Content of the XML element
     * @throws IOException
     * @throws XmlPullParserException
     */
    private static String readText() throws IOException, XmlPullParserException {
        String result = "";
        if (parser.next() == XmlPullParser.TEXT) {
            result = parser.getText();
            parser.nextTag();
        }
        return result;
    }

    protected static String startTagToEmptyTag(String startTag)
    {
        return startTag.substring(0, startTag.length() - 1) + "/>";
    }
}
