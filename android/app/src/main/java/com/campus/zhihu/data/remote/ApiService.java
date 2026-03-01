package com.campus.zhihu.data.remote;

import com.google.gson.JsonObject;
import java.util.Map;
import retrofit2.Call;
import retrofit2.http.*;

/**
 * 后端 API 接口定义
 * 对应后端所有 REST Controller
 */
public interface ApiService {

    // ===== 认证 =====
    @POST("api/auth/login")
    Call<JsonObject> login(@Body Map<String, String> body);

    @POST("api/auth/register")
    Call<JsonObject> register(@Body Map<String, Object> body);

    @GET("api/auth/current")
    Call<JsonObject> getCurrentUser();

    // ===== 用户 =====
    @GET("api/users/{id}")
    Call<JsonObject> getUser(@Path("id") long id);

    @PUT("api/users/profile")
    Call<JsonObject> updateProfile(@Body Map<String, Object> body);

    @POST("api/users/{id}/follow")
    Call<JsonObject> toggleFollow(@Path("id") long id);

    @GET("api/users/{id}/followers")
    Call<JsonObject> getFollowers(@Path("id") long id, @Query("page") int page, @Query("size") int size);

    @GET("api/users/{id}/following")
    Call<JsonObject> getFollowing(@Path("id") long id, @Query("page") int page, @Query("size") int size);

    @GET("api/users/{id}/questions")
    Call<JsonObject> getUserQuestions(@Path("id") long id, @Query("page") int page, @Query("size") int size);

    @GET("api/users/{id}/answers")
    Call<JsonObject> getUserAnswers(@Path("id") long id, @Query("page") int page, @Query("size") int size);

    @GET("api/users/{id}/favorites")
    Call<JsonObject> getUserFavorites(@Path("id") long id, @Query("page") int page, @Query("size") int size);

    @GET("api/users/{id}/is-following")
    Call<JsonObject> isFollowing(@Path("id") long id);

    // ===== 问题 =====
    @GET("api/questions")
    Call<JsonObject> getQuestions(@Query("page") int page, @Query("size") int size,
                                  @Query("keyword") String keyword, @Query("tagId") Long tagId, @Query("sort") String sort);

    @GET("api/questions/{id}")
    Call<JsonObject> getQuestion(@Path("id") long id);

    @GET("api/questions/recommend")
    Call<JsonObject> getRecommend(@Query("page") int page, @Query("size") int size);

    @GET("api/questions/hot")
    Call<JsonObject> getHotQuestions(@Query("page") int page, @Query("size") int size);

    @GET("api/questions/search")
    Call<JsonObject> searchQuestions(@Query("keyword") String keyword, @Query("page") int page, @Query("size") int size);

    @GET("api/questions/following")
    Call<JsonObject> getFollowingQuestions(@Query("page") int page, @Query("size") int size);

    @POST("api/questions")
    Call<JsonObject> createQuestion(@Body Map<String, Object> body);

    @PUT("api/questions/{id}")
    Call<JsonObject> updateQuestion(@Path("id") long id, @Body Map<String, Object> body);

    @DELETE("api/questions/{id}")
    Call<JsonObject> deleteQuestion(@Path("id") long id);

    // ===== 回答 =====
    @GET("api/questions/{qid}/answers")
    Call<JsonObject> getAnswers(@Path("qid") long questionId, @Query("page") int page, @Query("size") int size);

    @POST("api/questions/{qid}/answers")
    Call<JsonObject> createAnswer(@Path("qid") long questionId, @Body Map<String, String> body);

    @PUT("api/answers/{id}")
    Call<JsonObject> updateAnswer(@Path("id") long id, @Body Map<String, String> body);

    @DELETE("api/answers/{id}")
    Call<JsonObject> deleteAnswer(@Path("id") long id);

    @PUT("api/answers/{id}/accept")
    Call<JsonObject> acceptAnswer(@Path("id") long id);

    // ===== 评论 =====
    @GET("api/comments")
    Call<JsonObject> getComments(@Query("targetType") int targetType, @Query("targetId") long targetId,
                                 @Query("page") int page, @Query("size") int size);

    @POST("api/comments")
    Call<JsonObject> createComment(@Body Map<String, Object> body);

    @DELETE("api/comments/{id}")
    Call<JsonObject> deleteComment(@Path("id") long id);

    // ===== 互动 =====
    @POST("api/like")
    Call<JsonObject> toggleLike(@Body Map<String, Object> body);

    @POST("api/favorite")
    Call<JsonObject> toggleFavorite(@Query("questionId") long questionId);

    @GET("api/like/status")
    Call<JsonObject> getLikeStatus(@Query("targetType") int targetType, @Query("targetId") long targetId);

    @GET("api/favorite/status")
    Call<JsonObject> getFavoriteStatus(@Query("questionId") long questionId);

    // ===== 标签 =====
    @GET("api/tags")
    Call<JsonObject> getTags();

    @GET("api/tags/hot")
    Call<JsonObject> getHotTags(@Query("limit") int limit);

    @GET("api/tags/{id}/questions")
    Call<JsonObject> getTagQuestions(@Path("id") long tagId, @Query("page") int page, @Query("size") int size);

    // ===== 通知 =====
    @GET("api/notifications")
    Call<JsonObject> getNotifications(@Query("page") int page, @Query("size") int size);

    @PUT("api/notifications/read")
    Call<JsonObject> markNotificationRead(@Query("id") Long id);

    @GET("api/notifications/unread-count")
    Call<JsonObject> getUnreadCount();

    // ===== 举报 =====
    @POST("api/reports")
    Call<JsonObject> createReport(@Body Map<String, Object> body);
}
