package org.lightvideo;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class VideoResponse {
    @SerializedName("code")
    private int code;

    @SerializedName("data")
    private Data data;

    public Data getData() {
        return data;
    }

    public static class Data {
        @SerializedName("objs")
        private List<VideoItem> objs;

        public List<VideoItem> getObjs() {
            return objs;
        }
    }

    public static class VideoItem {
        @SerializedName("item_url")
        private String itemUrl;

        @SerializedName("item_title")
        private String itemTitle;

        public String getItemUrl() {
            return itemUrl;
        }

        public String getItemTitle() {
            return itemTitle;
        }
    }
}


//{
//        "code": 200,
//        "router": "/api/v1/douyin/web/fetch_video_billboard",
//        "params": {
//        "date": "24",
//        "page": "1",
//        "page_size": "20",
//        "sub_type": "1001"
//        },
//        "data": {
//        "page": {
//        "page": 1,
//        "page_size": 20,
//        "total": 1000
//        },
//        "objs": [
//        {
//        "item_id": "7432513015351692596",
//        "item_title": "追了这么久电子黄历终于让我蹲到黄道吉日了！",
//        "item_cover_url": "https://p9-sign.douyinpic.com/tos-cn-i-dy/6933afc08bcd4367bac1a64055ef9224~tplv-dy-resize-walign-adapt-aq:540:q75.jpeg?x-expires=1731826800&x-signature=IMIMjFmNsxfLaW2AVGITfyMgk2Y%3D&from=327834062&s=PackSourceEnum_PUBLISH&se=false&sc=cover&biz_tag=aweme_video&l=20241103153601C928435595F020EFA023",
//        "item_duration": 12214,
//        "nick_name": "我的偶像巨顽皮",
//        "avatar_url": "https://p11.douyinpic.com/aweme/100x100/aweme-avatar/tos-cn-avt-0015_78e46fbb5fb2a01ff01cfaf3143259c4.jpeg?from=2956013662",
//        "fans_cnt": 8337400,
//        "play_cnt": 10156939,
//        "publish_time": 1730517088,
//        "score": 592665,
//        "item_url": "https://v3.douyinvod.com/20349723bd44753b63d1bf3f3c3e17b6/672735fd/video/tos/cn/tos-cn-ve-15/oUnoMBZyTIAT6i1QpiYh8joI6PlBPAWtHQarE/?a=1128&ch=10&cr=3&dr=0&lr=all&cd=0%7C0%7C0%7C3&cv=1&br=1099&bt=1099&cs=0&ds=6&ft=OXXf~77JWH6BMcWLRcr0PD1IN&mime_type=video_mp4&qs=0&rc=aGg6ODM3Njk5ZDw7OzpkNUBpajQ5cHQ5cm1zdjMzNGkzM0AuYl4xMjMvNTYxMzBjMDZfYSNuZWlqMmQ0ZGZgLS1kLWFzcw%3D%3D&btag=c0010e00088000&cc=20&cquery=100b_105O_103Q_103W_103Y&dy_q=1730619361&feature_id=f0150a16a324336cda5d6dd0b69ed299&l=20241103153601C928435595F020EFA023&req_cdn_type=",
//        "like_cnt": 763796,
//        "follow_cnt": 1097,
//        "follow_rate": 0.000127,
//        "like_rate": 0.075199,
//        "media_type": 4,
//        "favorite_id": 0,
//        "is_favorite": false,
//        "image_cnt": 0
//        }
//}