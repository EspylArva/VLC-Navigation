package com.vlcnavigation.module.svg2vector;

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

    private static String text;
    private static boolean isSvgComponentTag(String tagName)
    {
        return !tagName.equalsIgnoreCase("xml") &&
                !tagName.equalsIgnoreCase("DOCTYPE") &&
                !tagName.equalsIgnoreCase("svg") &&
                !tagName.equalsIgnoreCase("defs") &&
                !tagName.equalsIgnoreCase("g");
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
            XmlPullParser parser = Xml.newPullParser();
            parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false);
            parser.setInput(in, null);
            parser.nextTag(); // Read first tag
            return readFeed(parser);
        }
        catch (XmlPullParserException | IOException e) {
            Timber.e(e);
            return null;
        }
        finally {
            in.close();
        }
    }

    private static Map<String, String> readFeed(XmlPullParser parser) throws IOException, XmlPullParserException {
        String header = getLineContent(parser);
        header = "<" + header.substring(1, header.length()-1)
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "\"") + ">";
        header = String.format("<?xml version=\"1.0\" encoding=\"UTF-8\"?><!DOCTYPE svg PUBLIC \"-//W3C//DTD SVG 1.1//EN\" \"http://www.w3.org/Graphics/SVG/1.1/DTD/svg11.dtd\">%s<g>", header);

        Timber.d("Header: %s", header);
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
                        svgContent = getLineContent(parser).substring(0, getLineContent(parser).length() - 1) + "/>";
                        Timber.d("SVG graphic part: %s", svgContent);
                    }
                    break;
                case XmlPullParser.TEXT:
                    if(!parser.getText().trim().isEmpty())
                    {
                        description = parser.getText();
                        Timber.d("Text: %s", description);
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
     * Pulls the content of a XML beacon
     * @param parser
     * @return content of the XML beacon
     */
    private static String getLineContent(XmlPullParser parser) {
        return parser.getPositionDescription().substring(parser.getPositionDescription().indexOf("<"), parser.getPositionDescription().lastIndexOf(">") + 1);
    }
}
