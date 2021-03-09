package com.vlcnavigation.module.svg2vector;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.util.Xml;

import com.vlcnavigation.R;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.util.List;

import timber.log.Timber;

public class Utils {

    public static Drawable xmlStringToDrawable(String xml, Context context) throws XmlPullParserException, IOException {
        XmlPullParserFactory factory = XmlPullParserFactory.newInstance(); factory.setValidating(true);
        XmlPullParser parser = factory.newPullParser();
        parser.setInput(new StringReader(xml));
        return Drawable.createFromXml(context.getResources(), parser);
    }

    public static List<String> listSvgAsString(InputStream rawSvgResourceId)
    {
//        context.get
        try {
            SvgSplitter.parse(rawSvgResourceId);
        }
        catch (IOException e) {
            Timber.e(e);
        }
        return null;
    }
}
