package com.vlcnavigation.module.svg2vector;

import android.util.Xml;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import timber.log.Timber;

public class SvgSplitter extends XmlParser {

    public static List<String> parse(InputStream in) throws IOException {
        try{
            XmlPullParser parser = Xml.newPullParser();
            parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false);
            parser.setInput(in, null);
            parser.nextTag();
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

    private static List<String> readFeed(XmlPullParser parser) throws IOException, XmlPullParserException {

        String tag = getLineContent(parser);
        tag = "<" + tag.substring(1, tag.length()-1)
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "\"") + ">";
        Timber.d(tag);

        String header = String.format("<?xml version=\"1.0\" encoding=\"UTF-8\"?><!DOCTYPE svg PUBLIC \"-//W3C//DTD SVG 1.1//EN\" \"http://www.w3.org/Graphics/SVG/1.1/DTD/svg11.dtd\">%s<g>", tag);
        String footer = "</g></svg>";
        return fillSvgList(parser, header, footer);
    }

    private static List<String> fillSvgList(XmlPullParser parser, String header, String footer) throws IOException, XmlPullParserException {
        List<String> svgs = new ArrayList<String>();
        parser.require(XmlPullParser.START_TAG, ns, "svg");
        while (parser.next() != XmlPullParser.END_TAG) {
            if (parser.getEventType() != XmlPullParser.START_TAG) { continue; }
            Timber.d(getLineContent(parser));
            String gEntry = parser.getName();
            if(gEntry.equals("g"))
            {
                parser.require(XmlPullParser.START_TAG, ns, "g");
                while (parser.next() != XmlPullParser.END_TAG) {
                    if (parser.getEventType() != XmlPullParser.START_TAG) { continue; }
                    String svgContent = getLineContent(parser).substring(0, getLineContent(parser).length() - 1) + "/>";
                    Timber.d("Line: %s", svgContent);
                    svgs.add(String.format("%s%s%s", header, svgContent, footer));
                    parser.next();
                }
            }
            else { Timber.d(gEntry); skip(parser); }
        }
        return svgs;
    }

    private static String getLineContent(XmlPullParser parser) {
        return parser.getPositionDescription().substring(parser.getPositionDescription().indexOf("<"), parser.getPositionDescription().lastIndexOf(">") + 1);
    }

    private String format(String svg) {
        return svg.replace("<", "&lt;");
    }


}
