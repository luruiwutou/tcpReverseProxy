package com.forward.core.utils;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;

public class UrlEncodeTest {

    //Sand Box client ID
    //l76c0af75f7d3d41679688e68eaf8d2361
    //client secret :
    //1d309c1b91594eeb9de623a3c200b20f
    //
    //Production client ID
    //l784147a8dadea4c6eac61ab8d606ab914
    //client secret :
    //81aef4a2d25046bebfb9deb209ec6f82

    public static void main(String[] args) throws UnsupportedEncodingException {
        String encode = URLEncoder.encode("grant_type=client_credentials&client_id=l784147a8dadea4c6eac61ab8d606ab914&client_secret=81aef4a2d25046bebfb9deb209ec6f82", "utf-8");
        System.out.println(encode);
    }
}
