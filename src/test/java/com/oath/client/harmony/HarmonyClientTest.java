package com.oath.client.harmony;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.startsWith;
import static org.junit.Assert.assertThat;

import java.net.HttpURLConnection;
import java.util.HashMap;
import java.util.Map;

import org.junit.Test;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import okhttp3.Credentials;
import okhttp3.HttpUrl;
import okhttp3.mockwebserver.Dispatcher;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;

/**
 * {@code HarmonyClient} test.
 *
 * @author Mike Shauneu
 */
public class HarmonyClientTest {

    @Test
    public void test() throws Exception {

        String messageId = "message_id";
        String clientId = "client_id";
        String clientPass = "client_pass";
        String userName = "user_name";
        String userPass = "user_pass";

        String ouId = "ou_id";
        String campaign = "campaign";
        Map<String, String> organizations = new HashMap<>();
        organizations.put(campaign, ouId);

        String authPath = "/Epsilon/oauth2/access_token?" + new  HttpUrl.Builder()
                .scheme("http")
                .host("localhost")
                .addQueryParameter("scope", "cn mail sn givenname uid employeeNumber")
                .addQueryParameter("grant_type", "password")
                .addQueryParameter("username", userName)
                .addQueryParameter("password", userPass)
                .build().toString().substring(18);

        String sendMessagePath = "/v3/messages/" + messageId + "/send/";

        try (MockWebServer server = new MockWebServer()) {

            server.setDispatcher(new Dispatcher() {
                @Override
                public MockResponse dispatch(RecordedRequest request) {
                    if ("POST".equals(request.getMethod())) {
                        return new MockResponse().setResponseCode(200).setBody("{\n" +
                                "   \"expires_in\":59,\n" +
                                "   \"token_type\":\"Bearer\",\n" +
                                "   \"refresh_token\":\"refresh_token\",\n" +
                                "   \"access_token\":\"access_token\"\n" +
                                "}");
                    } else if ("PUT".equals(request.getMethod())) {
                        if (request.getHeader("Authorization").equals("Bearer null")) {
                            return new MockResponse().setResponseCode(HttpURLConnection.HTTP_FORBIDDEN);
                        } else {
                            return new MockResponse().setResponseCode(200).setBody("{\n" +
                                    "  \"resultCode\": \"OK\",\n" +
                                    "  \"resultSubCode\": \"\",\n" +
                                    "  \"serviceTransactionId\": \"service_transaction_id\",\n" +
                                    "  \"clientRequestId\": \"client_request_id\",\n" +
                                    "  \"messageId\": \"" + messageId + "\",\n" +
                                    "  \"deploymentName\": \"Standard RTM\",\n" +
                                    "  \"deploymentId\": \"deployment_id\",\n" +
                                    "  \"deploymentDate\": 1456933140000,\n" +
                                    "  \"deploymentExpirationDate\": 1457019540000\n" +
                                    "}");
                        }
                    }
                    return new MockResponse().setResponseCode(404);            }
            });


            HarmonyClient harmonyClient = new HarmonyClient.Builder()
            		.withAuthBaseUrl("http://localhost:" + server.getPort())
            		.withMsgBaseUrl("http://localhost:" + server.getPort())
            		.withClientId(clientId)
            		.withClientPass(clientPass)
            		.withUserName(userName)
            		.withUserPass(userPass)
            		.build();

            SendMailRequest sentMailRequest = new SendMailRequest(messageId,
                    new SendMailRequest.Recipient("user@email.io",
                    		new SendMailRequest.Attribute("!att_name!", "att_value")));

            SendMailResponse sendMailResponse = harmonyClient.sendMail(ouId, sentMailRequest).get();

            assertThat(sendMailResponse.getResultCode(), equalTo(SendMailResponse.RESULT_CODE_SUCCESS));
            assertThat(sendMailResponse.getMessageId(), equalTo(messageId));
            assertThat(sendMailResponse.getClientRequestId(), equalTo("client_request_id"));
            assertThat(sendMailResponse.getServiceTransactionId(), equalTo("service_transaction_id"));

            RecordedRequest request;

            request = server.takeRequest();
            assertThat(request.getMethod(), equalTo("PUT"));
            assertThat(request.getPath(), equalTo(sendMessagePath));

            request = server.takeRequest();
            assertThat(request.getMethod(), equalTo("POST"));
            assertThat(request.getPath(), equalTo(authPath));
            assertThat(request.getHeader("Authorization"), equalTo(Credentials.basic(clientId, clientPass)));
            assertThat(request.getHeader("Content-Type"), startsWith("application/x-www-form-urlencoded"));
            assertThat(request.getRequestUrl().queryParameter("scope"), equalTo("cn mail sn givenname uid employeeNumber"));
            assertThat(request.getRequestUrl().queryParameter("grant_type"), equalTo("password"));
            assertThat(request.getRequestUrl().queryParameter("username"), equalTo(userName));
            assertThat(request.getRequestUrl().queryParameter("password"), equalTo(userPass));
            assertThat(request.getBody().size(), equalTo(0L));

            request = server.takeRequest();
            assertThat(request.getMethod(), equalTo("PUT"));
            assertThat(request.getPath(), equalTo(sendMessagePath));
            assertThat(request.getHeader("Authorization"), equalTo("Bearer access_token"));
            assertThat(request.getHeader("X-OUID"), equalTo("ou_id"));
            assertThat(request.getHeader("Content-Type"), startsWith("application/json"));

            JsonNode body = new ObjectMapper().readTree(request.getBody().inputStream());
            assertThat(body.get("id").asText(), equalTo(messageId));
            assertThat(body.get("recipients").get(0).get("customerKey").asText(), equalTo("user@email.io"));
            assertThat(body.get("recipients").get(0).get("emailAddress").asText(), equalTo("user@email.io"));
            assertThat(body.get("recipients").get(0).get("attributes").get(0).get("attributeName").asText(), equalTo("!att_name!"));
            assertThat(body.get("recipients").get(0).get("attributes").get(0).get("attributeValue").asText(), equalTo("att_value"));
        }
    }

}
