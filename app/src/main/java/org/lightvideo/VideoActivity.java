package org.lightvideo;


import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;

import androidx.annotation.WorkerThread;
import androidx.appcompat.app.AppCompatActivity;
import androidx.media3.common.MediaItem;
import androidx.media3.common.Player;
import androidx.media3.common.util.UnstableApi;
import androidx.media3.common.util.Util;
import androidx.media3.database.DatabaseProvider;
import androidx.media3.database.StandaloneDatabaseProvider;
import androidx.media3.datasource.DataSpec;
import androidx.media3.datasource.DefaultHttpDataSource;
import androidx.media3.datasource.cache.Cache;
import androidx.media3.datasource.cache.CacheDataSource;
import androidx.media3.datasource.cache.CacheWriter;
import androidx.media3.datasource.cache.LeastRecentlyUsedCacheEvictor;
import androidx.media3.datasource.cache.SimpleCache;
import androidx.media3.datasource.okhttp.OkHttpDataSource;
import androidx.media3.exoplayer.DefaultLoadControl;
import androidx.media3.exoplayer.ExoPlayer;
import androidx.media3.exoplayer.LoadControl;
import androidx.media3.exoplayer.source.DefaultMediaSourceFactory;
import androidx.media3.exoplayer.trackselection.DefaultTrackSelector;
import androidx.media3.exoplayer.trackselection.TrackSelector;
import androidx.media3.ui.PlayerView;

import com.google.gson.Gson;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;


@UnstableApi
public class VideoActivity extends AppCompatActivity {

    private static final int MIN_DISTANCE = 150;
    ExoPlayer player;
    LoadControl loadControl;
    File cacheDir;
    DatabaseProvider databaseProvider;
    Cache cache;
    DefaultHttpDataSource.Factory httpDataSourceFactory;
    CacheDataSource.Factory cacheDataSourceFactory;
    CacheDataSource cacheDataSource;
    OkHttpClient okHttpClient;
    OkHttpDataSource.Factory okHttpDataSourceFactory;
    private GestureDetector gestureDetector;
    private long startTime;
    private long endTime;
    private PlayerView playerView;
    private ArrayList<VideoInfo> videoInfos;
    private int idx;
    private ExecutorService executor;
    //private HttpProxyCacheServer proxy;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video);

        idx = 0;

        playerView = findViewById(R.id.playerView);

        playerView.setShowNextButton(false);
        playerView.setShowPreviousButton(false);

        executor = Executors.newCachedThreadPool();

        initVideoUrls(new VideoUrlsCallback() {
            @Override
            public void onVideoUrlsLoaded(ArrayList<VideoInfo> infos) {
                videoInfos = infos;
                idx = 0;

                // 初始化播放器和其他 UI 组件
                initializePlayer();

                // 播放第一个视频
                if (!videoInfos.isEmpty()) {
                    playVideo(videoInfos.get(idx).getUrl());
                }
            }
        });

        // 设置手势检测器
        gestureDetector = new GestureDetector(this, new GestureDetector.SimpleOnGestureListener() {
            private static final int SWIPE_THRESHOLD = 100;
            private static final int SWIPE_VELOCITY_THRESHOLD = 100;

            @Override
            public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
                float diffX = e2.getX() - e1.getX();
                if (Math.abs(diffX) > SWIPE_THRESHOLD && Math.abs(velocityX) > SWIPE_VELOCITY_THRESHOLD) {
                    if (diffX > 0) {
                        // 向右滑动
                        onSwipeRight();
                    } else {
                        // 向左滑动
                        onSwipeLeft();
                    }
                    return true;
                }
                return false;
            }
        });

        // 设置触摸事件监听器
        playerView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                return gestureDetector.onTouchEvent(event);
            }
        });

    }

    // ================================================================================

    private void initializePlayer() {
        // ==========================================================================
        cacheDir = new File(getCacheDir(), "media");

        databaseProvider = new StandaloneDatabaseProvider(this);

        cache = new SimpleCache(cacheDir,
                new LeastRecentlyUsedCacheEvictor(200 * 1024 * 1024),
                databaseProvider
        );

//        httpDataSourceFactory = new DefaultHttpDataSource.Factory()
//                .setUserAgent(Util.getUserAgent(this, "LightVideo"))
//                .setConnectTimeoutMs(DefaultHttpDataSource.DEFAULT_CONNECT_TIMEOUT_MILLIS)
//                .setReadTimeoutMs(DefaultHttpDataSource.DEFAULT_READ_TIMEOUT_MILLIS)
//                .setAllowCrossProtocolRedirects(true);

        okHttpClient = new OkHttpClient.Builder()
                .connectTimeout(5, TimeUnit.SECONDS)
                .readTimeout(10, TimeUnit.SECONDS)
                .build();

        okHttpDataSourceFactory =
                new OkHttpDataSource.Factory(okHttpClient)
                        .setUserAgent(Util.getUserAgent(this, "LightVideo"));

        cacheDataSourceFactory =
                new CacheDataSource.Factory()
                        .setCache(cache)
                        .setUpstreamDataSourceFactory(okHttpDataSourceFactory)
                        .setFlags(CacheDataSource.FLAG_IGNORE_CACHE_ON_ERROR);

        cacheDataSource = cacheDataSourceFactory.createDataSource();

        loadControl = new DefaultLoadControl.Builder()
                .setPrioritizeTimeOverSizeThresholds(true)
                .setBufferDurationsMs(
                        25000,
                        25000,
                        1000,
                        1000
                ).build();

        TrackSelector trackSelector = new DefaultTrackSelector(this);

        player = new ExoPlayer.Builder(this)
                .setTrackSelector(trackSelector)
                .setLoadControl(loadControl)
                .setMediaSourceFactory(new DefaultMediaSourceFactory(this).setDataSourceFactory(cacheDataSourceFactory))
                .build();

        // ==========================================================================

        //proxy = new HttpProxyCacheServer(this);

        playerView.setPlayer(player);
        player.clearMediaItems();
        player.setRepeatMode(Player.REPEAT_MODE_ALL);


        player.addListener(new Player.Listener() {
            @Override
            public void onPlaybackStateChanged(int playbackState) {
                if (playbackState == Player.STATE_READY) {
                    startTime = System.currentTimeMillis();
                    Log.d("WatchDuration", "Start");
                }
            }
        });

        playVideo(videoInfos.get(idx).getUrl());

    }

    private void initVideoUrls(VideoUrlsCallback callback) {
        ArrayList<VideoInfo> infos = new ArrayList<>();

        // 创建 OkHttpClient 实例
        OkHttpClient client = new OkHttpClient();

        // 构建请求
        Request request = new Request.Builder()
                .url("https://beta.tikhub.io/api/v1/douyin/web/fetch_video_billboard?date=24&page=1&page_size=20&sub_type=1001")
                .addHeader("accept", "application/json")
                .addHeader("Authorization", "Bearer 9p89QFpb+FgunQIfyXntwEE1wyQAjLPnob1Y5g/Dn1xi2g0rYaYswi2ODA==")
                .build();

        // 发送异步请求
        client.newCall(request).enqueue(new okhttp3.Callback() {
            @Override
            public void onResponse(okhttp3.Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    ResponseBody responseBody = response.body();
                    if (responseBody != null) {
                        String json = responseBody.string();
                        Gson gson = new Gson();
                        VideoResponse videoResponse = gson.fromJson(json, VideoResponse.class);

                        // 获取视频 URL 列表
                        List<VideoResponse.VideoItem> videoItems = videoResponse.getData().getObjs();
                        for (VideoResponse.VideoItem item : videoItems) {
                            VideoInfo info = new VideoInfo();
                            info.setUrl(item.getItemUrl());
                            info.setTitle(item.getItemTitle());

                            infos.add(info);
                        }

                        // 在主线程中调用回调方法
                        new Handler(Looper.getMainLooper()).post(() -> {
                            callback.onVideoUrlsLoaded(infos);
                        });
                    }
                } else {
                    // 处理错误
                    Log.e("GetVideo", "Failed to fetch video URLs: " + response.message());
                }
            }

            @Override
            public void onFailure(okhttp3.Call call, IOException e) {
                // 处理请求失败
                Log.e("GetVideo", "Request failed: " + e.getMessage());
            }
        });
    }

    private void playVideo(String url) {
        player.setMediaItem(MediaItem.fromUri(url));
        player.prepare();
        player.play();

        executor.submit(() -> preloadVideo(videoInfos.get((idx + 1) % videoInfos.size()).getUrl()));
    }

    @WorkerThread
    private void preloadVideo(String videoUrl) {
        // 创建一个 DataSpec 对象，指定要预加载的视频 URL
        DataSpec dataSpec = new DataSpec.Builder()
                .setUri(videoUrl)
                .setLength(5 * 1024 * 1024) // 预加载 5MB 的数据
                .build();

        // 创建一个 CacheWriter 对象来执行预加载
        CacheWriter cacheWriter = new CacheWriter(
                cacheDataSource,
                dataSpec,
                new byte[1048576],
                (requestLength, bytesCached, newBytesCached) -> {
                    // 缓冲进度变化时回调
                    // requestLength 请求总大小
                    // bytesCached 已缓冲的字节数
                    // newBytesCached 新缓冲的字节数

                    Log.d("Preload", "Preloading... " + bytesCached / 1024 + "KB");

                }
        );

        try {
            cacheWriter.cache(); // 执行预加载
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    private void onSwipeLeft() {
        endTime = System.currentTimeMillis();
        long durationInMillis = endTime - startTime;
        long durationInSeconds = durationInMillis / 1000; // 转换为秒
        Log.d("WatchDuration", "Video watched for: " + durationInSeconds + " seconds");

        // 插入观看时长到数据库
        insertWatchDuration(videoInfos.get(idx).getTitle(), durationInSeconds);

        idx = (idx - 1 + videoInfos.size()) % videoInfos.size();
        playVideo(videoInfos.get(idx).getUrl());
    }

    private void onSwipeRight() {
        endTime = System.currentTimeMillis();
        long durationInMillis = endTime - startTime;
        long durationInSeconds = durationInMillis / 1000; // 转换为秒
        Log.d("WatchDuration", "Video watched for: " + durationInSeconds + " seconds");

        // 插入观看时长到数据库
        insertWatchDuration(videoInfos.get(idx).getTitle(), durationInSeconds);

        idx = (idx + 1) % videoInfos.size();
        playVideo(videoInfos.get(idx).getUrl());
    }

    private void insertWatchDuration(String videoTitle, long durationInSeconds) {
        executor.submit(() -> {
            MysqlHelp.createUserTableIfNotExists();
            MysqlHelp.insertUserInfo(videoTitle, durationInSeconds);
        });
    }

    @Override
    protected void onStop() {
        super.onStop();

        executor.shutdown();
        player.stop();
        player.release();
        cache.release();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        executor.shutdown();
        player.stop();
        player.release();
        cache.release();
    }

}