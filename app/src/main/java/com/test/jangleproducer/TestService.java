package com.test.jangleproducer;

import com.test.jangleproducer.model.dispatch.AuthModel;
import com.test.jangleproducer.model.dispatch.profile.HideAccountModel;
import com.test.jangleproducer.model.dispatch.RegisterModel;
import com.test.jangleproducer.model.dispatch.UuidModel;
import com.test.jangleproducer.model.dispatch.profile.UpdateNameModel;
import com.test.jangleproducer.model.dispatch.vote.VoteModel;
import com.test.jangleproducer.model.result.AuthResponse;
import com.test.jangleproducer.model.result.UUID;
import com.test.jangleproducer.model.result.UploadResponse;
import com.test.jangleproducer.model.result.del.JangleInfo;
import com.test.jangleproducer.model.result.del.UserResponse;
import com.test.jangleproducer.model.result.vote.Voting;

import java.util.List;
import java.util.Map;

import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.HeaderMap;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;
import retrofit2.http.Path;

public interface TestService {


    @POST("/api/authenticate")
    Call<AuthResponse> authenticate(@Body AuthModel model);

    @POST("/api/register")
    Call<AuthResponse> register(@Body RegisterModel model);

    @GET("/api/account")
    Call<UUID> getUUIDs(@HeaderMap Map<String, String> authMap);


    @Multipart
    @POST("/upload/api/upload-resource/upload2")
    Call<UploadResponse> uploadJangle(@Part("uploadVM") RequestBody model, @Part MultipartBody.Part file, @Part MultipartBody.Part preview,
                                      @Part MultipartBody.Part thumbnail, @HeaderMap Map<String, String> headerMap);

    @GET("/social/api/getJanglersOfCurrentUser?limit=1")
    Call<JangleInfo> getUserLastJangle(@HeaderMap Map<String, String> authMap);

    @DELETE("/social/api/documents/{uuid}")
    Call<Void> delJangle(@HeaderMap Map<String, String> authMap,@Path("uuid") String jangleId);

    @GET("/social/api/getCompletionsForVotingFromOthers?limit=1")
    Call<Voting> getVoteAllOthers(@HeaderMap Map<String, String> authMap);

    @GET("/social/api/getCompletionsForVotingFromFollowings?limit=1")
    Call<Voting> getVoteAllFollowing(@HeaderMap Map<String, String> authMap);

    @POST("/social/api/completions/updateApproveInformation")
    Call<Void> voteCompletion(@Body VoteModel model, @HeaderMap Map<String, String> authMap);




    @GET("/api/users?page=0&size=3")
    Call<List<UserResponse>> getAllUsers(@HeaderMap Map<String, String> authMap);

    @POST("/api/users/{login}")
    Call<Void> delUser(@HeaderMap Map<String, String> authMap, @Path("login") String login);


    //region Inter users follow
    @POST("/social/api/follows/sendFollowingRequest")
    Call<Void> sendFollowRequest(@Body UuidModel model, @HeaderMap Map<String, String> authMap);
    //endregion


    //region update profile
    @POST("/api/account/updateProfileInformation")
    Call<Void> updateProfileName(@Body UpdateNameModel model, @HeaderMap Map<String, String> authMap);

    @POST("/api/account/updateHiddenAccount")
    Call<Void> hideAccount(@Body HideAccountModel model, @HeaderMap Map<String, String> authMap);

    @Multipart
    @POST("/api/account/uploadProfileImage")
    Call<Void> uploadImage(@Part MultipartBody.Part file, @Part MultipartBody.Part thumbnail,
                                      @HeaderMap Map<String, String> headerMap);
    //endregion

    //like
    @POST("/social/api/likeds")
    Call<Void> likeJangle(@Body UuidModel model, @HeaderMap Map<String, String> authMap);
    //endregion


}
