package com.oath.client.harmony;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

/**
 * Harmony Real Time Message (RTM) response payload. Containing information
 * about the message that was just sent.
 *
 * @author Mike Shauneu
 */
@JsonInclude(Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class SendMailResponse {

    public static final String RESULT_CODE_SUCCESS = "OK";

	private String resultCode;
	private String resultSubCode;
	private String resultString;
	private String serviceTransactionId;
	private String clientRequestId;
	private String messageId;
	private String deploymentName;
	private String deploymentId;
	private long deploymentDate;
	private long deploymentExpirationDate;
	private List<Error> errors;

	public String getResultCode() {
		return resultCode;
	}

	public void setResultCode(String resultCode) {
		this.resultCode = resultCode;
	}

	public String getResultSubCode() {
		return resultSubCode;
	}

	public String getResultString() {
        return resultString;
    }

	public void setResultString(String resultString) {
        this.resultString = resultString;
    }

	public void setResultSubCode(String resultSubCode) {
		this.resultSubCode = resultSubCode;
	}

	public String getServiceTransactionId() {
		return serviceTransactionId;
	}

	public void setServiceTransactionId(String serviceTransactionId) {
		this.serviceTransactionId = serviceTransactionId;
	}

	public String getClientRequestId() {
		return clientRequestId;
	}

	public void setClientRequestId(String clientRequestId) {
		this.clientRequestId = clientRequestId;
	}

    /**
     * MessageID of message that was just sent.
     *
     * @return messge id
     */
	public String getMessageId() {
		return messageId;
	}

    /**
     * @see #getMessageId()
     *
     * @param messageId
     *            message id
     */
	public void setMessageId(String messageId) {
		this.messageId = messageId;
	}

    /**
     * Deployment name of they deployment used to send message.
     *
     * @return deployment name
     */
	public String getDeploymentName() {
		return deploymentName;
	}

    /**
     * @see #getDeploymentName()
     *
     * @param deploymentName
     *            deployment name
     */
	public void setDeploymentName(String deploymentName) {
		this.deploymentName = deploymentName;
	}

    /**
     * DeploymentID of the deployment used to send message.
     *
     * @return deployment id
     */
	public String getDeploymentId() {
		return deploymentId;
	}

	/**
	 * @see #getDeploymentId()
	 *
	 * @param deploymentId
	 *         deployment id
	 */
	public void setDeploymentId(String deploymentId) {
		this.deploymentId = deploymentId;
	}

    /**
     * Deployment date of the deployment used to send the message.
     *
     * @return deployment date
     */
	public long getDeploymentDate() {
		return deploymentDate;
	}

    /**
     * @see #getDeploymentDate()
     *
     * @param deploymentDate
     *         deployment date
     */
	public void setDeploymentDate(long deploymentDate) {
		this.deploymentDate = deploymentDate;
	}

	public long getDeploymentExpirationDate() {
		return deploymentExpirationDate;
	}

	public void setDeploymentExpirationDate(long deploymentExpirationDate) {
		this.deploymentExpirationDate = deploymentExpirationDate;
	}

    /**
     * Convenient method to set result code.
     *
     * @param resultCode
     *            result code
     * @return {@link SendMailResponse}
     */
    public SendMailResponse withResultCode(String resultCode) {
        this.resultCode = resultCode;
        return this;
    }

    /**
     * Convenient method to set service transaction id.
     *
     * @param serviceTransactionId
     *            service transaction id
     * @return {@link SendMailResponse}
     */
    public SendMailResponse withServiceTransactionId(String serviceTransactionId) {
        this.serviceTransactionId = serviceTransactionId;
        return this;
    }

    public List<Error> getErrors() {
		return errors;
	}

    public void setErrors(List<Error> errors) {
		this.errors = errors;
	}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Error {
    	String resultString;

    	public String getResultString() {
			return resultString;
		}

    	public void setResultString(String resultString) {
			this.resultString = resultString;
		}
    }

}
