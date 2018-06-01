package com.oath.client.harmony;

import java.util.Arrays;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

/**
 * Harmony Real Time Message (RTM) request payload.
 *
 * @author Mike Shauneu
 */
@JsonInclude(Include.NON_NULL)
public class SendMailRequest {

	private String id;
	private List<Recipient> recipients;
	private List<Attribute> defaultAttributes;


	/**
	 * Default constructor for JSON serialization.
	 */
	public SendMailRequest() {
	}

    /**
     * Constructor.
     *
     * @param id
     *            message id
     * @param recipients
     *            {@link List} of {@link Recipient}
     */
    public SendMailRequest(String id, Recipient ...recipients) {
        this(id, Arrays.asList(recipients), null);
    }

    /**
     * Constructor.
     *
     * @param id
     *            message id
     * @param recipients
     *            {@link List} of {@link Recipient}
     * @param defaultAttributes
     *            {@link List} of {@link Attribute}s
     */
	public SendMailRequest(String id, List<Recipient> recipients, List<Attribute> defaultAttributes) {
	    this.id = id;
		this.recipients = recipients;
		this.defaultAttributes = defaultAttributes;
	}

    /**
     * Message Id.
     *
     * @return message id
     */
	public String getId() {
		return id;
	}

	/**
     * @see #getId()
     *
     * @param id
     *            Message Id
     */
	public void setId(String id) {
		this.id = id;
	}

    /**
     * Portion of the request payload to specify the recipients of the message.
     * There is a maximum of 10 recipients per request.
     *
     * @return {@link List} of {@link Recipient}
     */
	public List<Recipient> getRecipients() {
		return recipients;
	}

	/**
	 * @see #getRecipients()
	 *
	 * @param recipients
	 *         {@link List} of {@link Recipient}
	 */
	public void setRecipients(List<Recipient> recipients) {
		this.recipients = recipients;
	}

    /**
     * Default attributes to use for each recipient. Used when a recipient does not
     * have attributes.
     *
     * @return {@link List} of {@link Attribute}s
     */
	public List<Attribute> getDefaultAttributes() {
		return defaultAttributes;
	}

    /**
     * @see #getDefaultAttributes()
     *
     * @param defaultAttributes {@link List} of {@link Attribute}s
     */
	public void setDefaultAttributes(List<Attribute> defaultAttributes) {
		this.defaultAttributes = defaultAttributes;
	}

	@JsonInclude(Include.NON_NULL)
	public static class Recipient {

	    private String emailAddress;
        private String customerKey;
	    private List<Attribute> attributes;

	    public Recipient() {
	    }

	    public Recipient(String emailAddress) {
	        this(emailAddress, emailAddress, (Attribute) null);
	    }

        public Recipient(String emailAddress, Attribute ...attributes) {
            this(emailAddress, emailAddress, attributes);
        }

	    public Recipient(String emailAddress, String customerKey, Attribute ...attributes) {
	    	this(emailAddress, customerKey, Arrays.asList(attributes));
	    }

	    public Recipient(String emailAddress, String customerKey, List<Attribute> attributes) {
	        this.emailAddress = emailAddress;
	        this.customerKey = customerKey;
	        this.attributes = attributes;
	    }


	    public String getEmailAddress() {
	        return emailAddress;
	    }

	    public void setEmailAddress(String emailAddress) {
	        this.emailAddress = emailAddress;
	    }

        public String getCustomerKey() {
            return customerKey;
        }

        public void setCustomerKey(String customerKey) {
            this.customerKey = customerKey;
        }

	    public List<Attribute> getAttributes() {
	        return attributes;
	    }

	    public void setAttributes(List<Attribute> attributes) {
	        this.attributes = attributes;
	    }
	}

	@JsonInclude(Include.NON_NULL)
	public static class Attribute {

	    private String attributeName;
	    private String attributeValue;
	    private String attributeType;

	    public Attribute() {
	        this(null, null, null);
	    }

	    public Attribute(String name, String value) {
	        this(name, value, "String");
	    }

	    public Attribute(String attributeName, String attributeValue, String attributeType) {
	        this.attributeName = attributeName;
	        this.attributeValue = attributeValue;
	        this.attributeType = attributeType;
	    }

	    public String getAttributeName() {
	        return attributeName;
	    }

	    public void setAttributeName(String attributeName) {
	        this.attributeName = attributeName;
	    }

	    public String getAttributeValue() {
	        return attributeValue;
	    }

	    public void setAttributeValue(String attributeValue) {
	        this.attributeValue = attributeValue;
	    }

	    public String getAttributeType() {
	        return attributeType;
	    }

	    public void setAttributeType(String attributeType) {
	        this.attributeType = attributeType;
	    }
	}

}
