package com.campus.zhihu.data.local;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

/**
 * Room 本地缓存 - 问题表
 */
@Entity(tableName = "cached_question")
public class CachedQuestion {
    @PrimaryKey
    public long id;
    public String title;
    public String content;
    public long userId;
    public String authorNickname;
    public String authorAvatar;
    public int status;
    public int viewCount;
    public int answerCount;
    public int likeCount;
    public int favoriteCount;
    public int commentCount;
    public String tags; // JSON array string
    public String createdAt;
    public long cachedAt;
}
