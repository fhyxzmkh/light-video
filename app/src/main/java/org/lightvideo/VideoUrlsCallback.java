package org.lightvideo;

import java.util.ArrayList;

public interface VideoUrlsCallback {
    void onVideoUrlsLoaded(ArrayList<VideoInfo> infos);
}