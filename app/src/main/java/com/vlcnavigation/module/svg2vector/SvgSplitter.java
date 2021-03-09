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
        List<String> svgs = new ArrayList<String>();

        parser.require(XmlPullParser.START_TAG, ns, "svg");
        while (parser.next() != XmlPullParser.END_TAG) {
            String gEntry = parser.getName();
            if(gEntry.equals("g"))
            {
                parser.require(XmlPullParser.START_TAG, ns, "g");
                while (parser.next() != XmlPullParser.END_TAG) {
                    String svgEntry = parser.getName();
                    Timber.d(parser.getInputEncoding());
                }
            }
            else { Timber.d(gEntry); skip(parser); }
        }
        return null;
    }


}
