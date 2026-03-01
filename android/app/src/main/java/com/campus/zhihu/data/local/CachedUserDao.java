package com.campus.zhihu.data.local;

import androidx.room.*;
import java.util.List;

@Dao
public interface CachedUserDao {
    @Query("SELECT * FROM cached_user WHERE id = :id")
    CachedUser getById(long id);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertOrUpdate(CachedUser user);

    @Delete
    void delete(CachedUser user);

    @Query("DELETE FROM cached_user WHERE cachedAt < :before")
    void deleteExpired(long before);
}
