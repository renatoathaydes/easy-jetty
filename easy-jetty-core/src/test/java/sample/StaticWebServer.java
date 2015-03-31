package sample;

import com.athaydes.easyjetty.EasyJetty;

/**
 * Sample showing how to setup a very simple static web server that will just serve
 * the content found in the src/ directory.
 */
public class StaticWebServer {

    public static void main(String[] args) {
        new EasyJetty().resourcesLocation("build/").start();
    }

}
