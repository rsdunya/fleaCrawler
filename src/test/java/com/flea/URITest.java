package com.flea;

import java.net.URI;
import java.net.URISyntaxException;

/**
 * Created by bilgi on 8/30/15.
 */
public class URITest {
    public static void main(String[] args){
        try {
            URI uri = new URI("http://sayyac.mynet.com/tiklama/546?tags=mynetanasayfa_sticker_gold_cumhuriyet-altini&url=http%3A%2F%2Ffinans.mynet.com%2Faltin%2Fscum-cumhuriyet-altini");
            URI uri2 = new URI("http://sayyac.mynet.com/tiklama/546?tags=mynetanasayfa_sticker_gold_cumhuriyet-altini&url= http%3A%2F%2Ffinans.mynet.com%2Faltin%2Fscum-cumhuriyet-altini");
            System.out.println(uri.toString());
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
    }
}
