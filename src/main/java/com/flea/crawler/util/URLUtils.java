package com.flea.crawler.util;

import java.net.URI;
import java.net.URISyntaxException;

/**
 * Created by bilgi on 9/7/15.
 */
public class URLUtils {

    // Convert relative path to an absolute URI
    public static String getAbsoluteUrl(String domain, String target) {
        target = target.trim();
        URI uri;
        try {
            uri = new URI(target);
            if (!uri.isAbsolute()) {
                if (!"/".equals(target.indexOf(0)))
                    target = "/" + target;
                target = cleanUrl(domain) + target;
            }
        } catch (URISyntaxException e) {
        }
        return target;
    }

    public static String cleanUrl(String url) {
        try {
            URI uri = new URI(url);
            if (uri.getScheme() == null) {
                url = "http://" + url;
            }
        } catch (URISyntaxException e) {
        }

        // remove all trailing forward slashes
        url = url.replaceAll("/*$", "");
        return url;
    }

}
