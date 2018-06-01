package com.oath.client.harmony;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Credentials;
import okhttp3.HttpUrl;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.ResponseBody;
import okhttp3.logging.HttpLoggingInterceptor;
import okhttp3.logging.HttpLoggingInterceptor.Level;


/**
 * Client for send messages to Epsilon Harmony and read their responses.
 *
 * <h3>HarmonyClient should be shared</h3>
 *
 * <p>HarmonyClient performs best when you create a single {@code HarmonyClient} instance and reuse it for
 * all of your calls. This is because each client holds its own connection pool and thread
 * pools. Reusing connections and threads reduces latency and saves memory. Conversely, creating a
 * client for each request wastes resources on idle pools.

 * <h3>Shutdown isn't necessary</h3>
 *
 * @author Mike Shauneu
 */
public class HarmonyClient {

    private static final Logger LOGGER = LoggerFactory.getLogger(HarmonyClient.class);

    private static final HttpLoggingInterceptor
			HTTP_LOGGER_DEBUG = new HttpLoggingInterceptor(LOGGER::debug).setLevel(Level.BODY),
			HTTP_LOGGER_INFO = new HttpLoggingInterceptor(LOGGER::info).setLevel(Level.BASIC);

    private static final MediaType
    		MT_JSON = MediaType.parse("application/json"),
    		MT_FU = MediaType.parse("application/x-www-form-urlencoded");

    private final OkHttpClient httpClient;

    private AccessTokenResponse token = new AccessTokenResponse();

    private final ReadWriteLock tokenReadWriteLock = new ReentrantReadWriteLock();

    private String authBaseUrl;
    private String msgBaseUrl;
    private String clientId;
    private String clientPass;
    private String userName;
    private String userPass;

    /**
     * Constructor.
     * @param builder Builder
     */
    HarmonyClient(Builder builder) {
		Objects.requireNonNull(builder.authBaseUrl);
		this.authBaseUrl = builder.authBaseUrl;
    	Objects.requireNonNull(builder.msgBaseUrl);
		this.msgBaseUrl = builder.msgBaseUrl;
    	Objects.requireNonNull(builder.clientId);
		this.clientId = builder.clientId;
    	Objects.requireNonNull(builder.clientPass);
		this.clientPass = builder.clientPass;
    	Objects.requireNonNull(builder.userName);
		this.userName = builder.userName;
    	Objects.requireNonNull(builder.userPass);
		this.userPass = builder.userPass;

        this.httpClient = new OkHttpClient.Builder()
                .addInterceptor(chain -> {
                    if (chain.request().method().equals("POST")) {
                        return chain.proceed(chain.request());
                    }

                    Response response = chain.proceed(chain.request().newBuilder()
                        .header("Authorization", "Bearer " +  getToken(false).getAccessToken())
                        .build()
                    );
                    if (response.code() == HttpURLConnection.HTTP_FORBIDDEN) {
                        response = chain.proceed(chain.request().newBuilder()
                            .header("Authorization", "Bearer " + getToken(true).getAccessToken())
                            .build()
                        );
                    }
                    return response;
                })
                .addNetworkInterceptor(c -> {
                	return LOGGER.isDebugEnabled()
                			? HTTP_LOGGER_DEBUG.intercept(c)
                			: HTTP_LOGGER_INFO.intercept(c);
                })
                .build();
    }

    /**
     * Send RTM to Epsilon Harmony system asynchronously.
     *
     * @param campaign Harmony campaign id
     * @param request {@code SendMailRequest}
     * @return {@code CompletableFuture} of {@code SendMailResponse}
     */
    public CompletableFuture<SendMailResponse> sendMail(String campaign, SendMailRequest request)  {

        CompletableFuture<SendMailResponse> futureResponse = new CompletableFuture<>();

        HttpUrl httpUrl = HttpUrl.parse(msgBaseUrl).newBuilder()
                .addEncodedPathSegments(String.format("v3/messages/%s/send/", request.getId()))
                .build();

        RequestBody httpRequestBody;
        try {
            httpRequestBody = RequestBody.create(MT_JSON, new ObjectMapper().writeValueAsBytes(request));
        } catch (JsonProcessingException e) {
            futureResponse.completeExceptionally(new SendMailException("HARMONY: Invalid request", e));
            return futureResponse;
        }

        Request httpRequest = new Request.Builder()
                .url(httpUrl)
                .header("Accept", MT_JSON.toString())
                .header("X-OUID", campaign)
                .put(httpRequestBody)
                .build();

        httpClient.newCall(httpRequest).enqueue(new Callback() {
            @Override
            public void onResponse(Call call, Response httpResponse) {
                ResponseBody httpResponseBody = httpResponse.body();
                if (httpResponse.isSuccessful()) {
                    try {
                    	futureResponse.complete(
                            new ObjectMapper().readValue(httpResponseBody.byteStream(), SendMailResponse.class));
	                } catch (Exception e) {
	                    futureResponse.completeExceptionally(new SendMailException(e));
	                }
                } else {
                	String error = null;
                	try {
                		error = new ObjectMapper().readValue(httpResponseBody.byteStream(), SendMailResponse.class)
                					.getErrors().get(0).getResultString();
                	} catch (Exception e) {
	                    error = e.getMessage();
					}
                	futureResponse.completeExceptionally(new SendMailException(error));
                }
            }
            @Override
            public void onFailure(Call call, IOException e) {
                futureResponse.completeExceptionally(new SendMailException(e));
            }
        });
        return futureResponse;
    }

    private AccessTokenResponse getToken(boolean refresh) throws IOException {
        if (refresh) {
            try {
                tokenReadWriteLock.writeLock().lock();
                token = fetchToken();
                return token;
            } finally {
                tokenReadWriteLock.writeLock().unlock();
            }
        } else {
            try {
                tokenReadWriteLock.readLock().lock();
                return token;
            } finally {
                tokenReadWriteLock.readLock().unlock();
            }
        }
    }

    private AccessTokenResponse fetchToken() throws IOException {
        HttpUrl url = HttpUrl.parse(authBaseUrl).newBuilder()
                .addEncodedPathSegments("Epsilon/oauth2/access_token")
                .addQueryParameter("scope", "cn mail sn givenname uid employeeNumber")
                .addQueryParameter("grant_type", "password")
                .addQueryParameter("username", userName)
                .addQueryParameter("password", userPass)
                .build();

        Request request = new Request.Builder()
                .url(url)
                .header("Authorization", Credentials.basic(clientId, clientPass))
                .post(RequestBody.create(MT_FU, new byte[0]))
                .build();

        Response response = httpClient.newCall(request).execute();

        return response.isSuccessful()
                ? new ObjectMapper().readValue(response.body().byteStream(), AccessTokenResponse.class) : token;

    }

    public static class Builder {
        String authBaseUrl;
        String msgBaseUrl;
        String clientId;
        String clientPass;
        String userName;
        String userPass;

        public Builder() {
            authBaseUrl = "https://api-public.epsilon.com";
            msgBaseUrl = "https://api.harmony.epsilon.com";
		}
        public Builder withAuthBaseUrl(String authBaseUrl) {
            this.authBaseUrl = authBaseUrl;
            return this;
        }
        public Builder withClientId(String clientId) {
            this.clientId = clientId;
            return this;
        }
        public Builder withClientPass(String clientPass) {
            this.clientPass = clientPass;
            return this;
        }
        public Builder withMsgBaseUrl(String msgBaseUrl) {
            this.msgBaseUrl = msgBaseUrl;
            return this;
        }
        public Builder withUserName(String userName) {
            this.userName = userName;
            return this;
        }
        public Builder withUserPass(String userPass) {
            this.userPass = userPass;
            return this;
        }
        public HarmonyClient build() {
        	return new HarmonyClient(this);
        }
    }

    /**
     * Harmony Authorization Response Payload.
     */
    static class AccessTokenResponse {

        @JsonProperty("expires_in")
        private String expiresIn;

        @JsonProperty("token_type")
        private String tokenType;

        @JsonProperty("refresh_token")
        private String refreshToken;

        @JsonProperty("access_token")
        private String accessToken;

        /**
         * @return amount of time before the access token expires
         */
        public String getExpiresIn() {
            return expiresIn;
        }

        /**
         * Defines the amount of time before the access token expires.
         *
         * @param expiresIn
         *            amount of time before the access token expires
         */
        public void setExpiresIn(String expiresIn) {
            this.expiresIn = expiresIn;
        }

        /**
         *
         * @return token type
         */
        public String getTokenType() {
            return tokenType;
        }

        /**
         * Defines the type of token returned.
         *
         * @param tokenType
         *            Token Type
         */
        public void setTokenType(String tokenType) {
            this.tokenType = tokenType;
        }

        /**
         * This is for future use.
         *
         * @return Refresh Token
         */
        public String getRefreshToken() {
            return refreshToken;
        }

        /**
         * This is for future use.
         *
         * @param refreshToken
         *            Refresh Token
         */
        public void setRefreshToken(String refreshToken) {
            this.refreshToken = refreshToken;
        }

        /**
         *
         * @return Access Token
         */
        public String getAccessToken() {
            return accessToken;
        }

        /**
         * This field must be passed into all Agility Harmony APIs with an authorization
         * header.
         *
         * @param accessToken
         *            Access Token
         */
        public void setAccessToken(String accessToken) {
            this.accessToken = accessToken;
        }

    }

}
