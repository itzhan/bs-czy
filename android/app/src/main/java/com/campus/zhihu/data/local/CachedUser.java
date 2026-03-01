package com.campus.zhihu.data.local;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

/**
 * Room 本地缓存 - 用户表
 */
@Entity(tableName = "cached_user")
public class CachedUser {
    @PrimaryKey
    public long id;
    public String username;
    public String nickname;
    public String avatar;
    public String email;
    public String bio;
    public int role;
    public int questionCount;
    public int answerCount;
    public int followerCount;
    public int followingCount;
    public int likeCount;
    public String department;
    public long cachedAt; // 缓存时间戳
}
