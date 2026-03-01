package com.campus.zhihu.data.local;

import androidx.room.*;
import java.util.List;

@Dao
public interface CachedQuestionDao {
    @Query("SELECT * FROM cached_question ORDER BY cachedAt DESC LIMIT :limit")
    List<CachedQuestion> getRecent(int limit);

    @Query("SELECT * FROM cached_question WHERE id = :id")
    CachedQuestion getById(long id);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertOrUpdate(CachedQuestion question);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAll(List<CachedQuestion> questions);

    @Query("DELETE FROM cached_question WHERE cachedAt < :before")
    void deleteExpired(long before);

    @Query("DELETE FROM cached_question")
    void deleteAll();

    @Query("SELECT * FROM cached_question WHERE title LIKE '%' || :keyword || '%' OR content LIKE '%' || :keyword || '%'")
    List<CachedQuestion> search(String keyword);
}
