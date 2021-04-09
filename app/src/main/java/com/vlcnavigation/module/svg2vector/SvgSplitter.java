package com.vlcnavigation.module.svg2vector;

import android.util.Pair;
import android.util.Xml;

import com.vlcnavigation.module.utils.XmlParser;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.io.InputStream;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import timber.log.Timber;

public class SvgSplitter extends XmlParser {

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

    /**
     * Looks for the tag and sees if it is of interest for the application
     *
     * @param tagName tag name
     * @return whether or not the tag is of interest
     */
    private static boolean isSvgComponentTag(String tagName)
    {
        return !tagName.equalsIgnoreCase("xml") &&
                !tagName.equalsIgnoreCase("DOCTYPE") &&
                !tagName.equalsIgnoreCase("svg") &&
                !tagName.equalsIgnoreCase("defs") &&
                !tagName.equalsIgnoreCase("g");
    }

    /**
     * Returns the size of the image in pixels. Size returned is width x height
     *
     * @param in InputStream representing the document
     * @return Pair of integers giving the width and height of the image in pixels
     * @throws IOException
     */
    public static Pair<Integer, Integer> getMapSize(InputStream in) throws IOException {
        try{
            parser = Xml.newPullParser();
            parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false);
            parser.setInput(in, null);
            parser.nextTag(); // Read first tag

            Timber.d(parser.getName());
            parser.require(XmlPullParser.START_TAG, ns, "svg");

            String width = parser.getAttributeValue(null, "width").substring(0, parser.getAttributeValue(null, "width").length() -2);
            String height = parser.getAttributeValue(null, "height").substring(0, parser.getAttributeValue(null, "height").length() -2);
            Timber.d("Width: %s -- Height: %s", width, height);

            Pair<Integer, Integer> sizeXY = new Pair<Integer, Integer>(Integer.parseInt(width), Integer.parseInt(height));
            return sizeXY;
        }
        catch (XmlPullParserException | IOException e) {
            Timber.e(e);
            return null;
        }
        finally {
            in.close();
        }
    }


    /**
     * TODO
     *
     * @param in
     * @return
     * @throws IOException
     */
    public static Map<String, String> parse(InputStream in) throws IOException {
        try{
            parser = Xml.newPullParser();
            parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false);
            parser.setInput(in, null);
            parser.nextTag(); // Read first tag
            return readFeed();
        }
        catch (XmlPullParserException | IOException e) {
            Timber.e(e);
            return null;
        }
        finally {
            in.close();
        }
    }

    private static Map<String, String> readFeed() throws IOException, XmlPullParserException {
        String header = getLineContent();
        header = "<" + header.substring(1, header.length()-1)
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "\"") + ">";
        header = String.format("<?xml version=\"1.0\" encoding=\"UTF-8\"?><!DOCTYPE svg PUBLIC \"-//W3C//DTD SVG 1.1//EN\" \"http://www.w3.org/Graphics/SVG/1.1/DTD/svg11.dtd\">%s<g>", header);
        String footer = "</g></svg>";
        return fillSvgList(parser, header, footer);
    }

    private static Map<String, String> fillSvgList(XmlPullParser parser, String header, String footer) throws IOException, XmlPullParserException {
        // Collection to return
        Map<String, String> svgMap = new HashMap<String, String>();

        int eventType;              // Type of event to treat
        String svgContent = "";     // Contains the SVG graphic component
        String description = "";    // The room description
        String tagName = "";
        while ((eventType = parser.getEventType()) != XmlPullParser.END_DOCUMENT)
        {
            tagName = parser.getName();
            switch (eventType)
            {
                case XmlPullParser.START_TAG:
                    if(isSvgComponentTag(tagName))
                    {
                        // Convert start-tag to empty-tag
                        svgContent = startTagToEmptyTag(getLineContent());
//                        Timber.d("SVG graphic part: %s", svgContent);
                    }
                    break;
                case XmlPullParser.TEXT:
                    if(!parser.getText().trim().isEmpty())
                    {
                        description = parser.getText();
//                        Timber.d("Text: %s", description);
                    }
                    break;
                case XmlPullParser.END_TAG:
                    if(isSvgComponentTag(tagName)) { svgMap.put(description, String.format("%s%s%s", header, svgContent, footer)); }
                    break;
                default:
                    break;
            }
            parser.next();
        }
        return svgMap;
    }

    /**
     * Fully pulls an element as a start-tag
     *
     * @return content of the XML beacon
     */
    private static String getLineContent() {
        return parser.getPositionDescription().substring(parser.getPositionDescription().indexOf("<"), parser.getPositionDescription().lastIndexOf(">") + 1);
    }


}
