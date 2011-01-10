/** $Id: $
 *--------------------------------------------------------------------------------------*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 *
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.components;


import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Properties;

import com.cybersource.ws.client.*;
import org.mule.components.model.TransactionType;

public class CyberSource
{
    private String merchantId;
    private String keysDirectory;
    private Boolean testMode = false;

    // This tells the client to use the Apache HTTP client instead of
    // the built in HTTP client.
    private Boolean useHttpClient = true;

    public String getMerchantId()
    {
        return merchantId;
    }

    public void setMerchantId(String merchantId)
    {
        this.merchantId = merchantId;
    }

    public String getKeysDirectory()
    {
        return keysDirectory;
    }

    public void setKeysDirectory(String keysDirectory)
    {
        this.keysDirectory = keysDirectory;
    }

    public void setTestMode(Boolean testMode)
    {
        this.testMode = testMode;
    }


    private Properties initialise()
    {
        Properties properties = new Properties();
        properties.put("merchantID", merchantId);
        properties.put("keysDirectory", keysDirectory);
        properties.put("targetAPIVersion", "1.28");
        properties.put("logDirectory", "logs");
        properties.put("useHTTPClient", useHttpClient);

        if (testMode)
        {
            properties.put("sendToProduction", "false");
            properties.put("enableLog", "true");
        } else
        {
            properties.put("sendToProduction", "true");
            properties.put("enableLog", "false");
        }

        return properties;
    }


    /**
     * This function combines the authorize and capture functions into a single call.
     *
     * @param creditCardNumber
     * @param expirationDate
     * @param amount
     * @param firstName
     * @param lastName
     * @param address
     * @param city
     * @param state
     * @param postalCode
     * @param email
     * @param country
     * @throws Exception
     */
    public HashMap makePayment(String creditCardNumber, String expirationDate, BigDecimal amount,
                               String firstName, String lastName, String address, String city,
                               String state, String postalCode, String email, String country) throws Exception
    {
        HashMap reply = authorizePayment(creditCardNumber, expirationDate, amount, firstName, lastName, address, city, state,
                postalCode, email, country);

        if ("ACCEPT".equalsIgnoreCase(reply.get("decision").toString()))
        {
            capturePayment(reply.get("requestID").toString(), amount.toString());
        }

        return reply;
    }

    /**
     * When you are ready to fulfill a customer’s order and transfer funds from the customer’s
     * bank to your bank, capture the authorization for that order.
     * <p/>
     * If you can fulfill only part of a customer’s order, do not capture the full amount of the
     * authorization. Capture only the cost of the items that you ship. When you ship the
     * remaining items, request a new authorization, then capture the new authorization.
     * <p/>
     * Due to the potential delay between authorization and capture, the authorization might
     * expire with the issuing bank before you request capture. Most authorizations expire
     * within five to seven days. If an authorization expires with the issuing bank before you
     * request the capture, your bank or processor might require you to resubmit an
     * authorization request and include a request for capture in the same message.
     *
     * @param authRequestId
     * @param amount
     * @return
     * @throws Exception
     */
    public HashMap capturePayment(String authRequestId, String amount) throws Exception
    {
        String requestID = null;

        HashMap request = new HashMap();

        request.put(TransactionType.CAPTURE.toString(), "true");

        // We will let the Client get the merchantID from props and insert it
        // into the request Map.

        request.put("merchantReferenceCode", "Standard Code");
        request.put("ccCaptureService_authRequestID", authRequestId);
        request.put("purchaseTotals_currency", "USD");
        request.put("purchaseTotals_grandTotalAmount", amount.toString());

        HashMap reply = null;

        try
        {
            reply = Client.runTransaction(request, initialise());
        } catch (ClientException e)
        {
            System.out.println(e.getMessage());
        } catch (FaultException e)
        {
            System.out.println(e.getMessage());
        }

        return reply;
    }

    /**
     * Online authorization means that when you submit an order using a credit card, you receive
     * an immediate confirmation about the availability of the funds. If the funds are available,
     * the issuing bank reduces your customer’s open to buy, which is the amount of credit
     * available on the card. Most of the common credit cards are processed online. For online
     * authorizations, you will typically start the process of order fulfillment soon after you
     * receive confirmation of the order.
     * <p/>
     * Online authorizations expire with the issuing bank after a specific length of time if they
     * have not been captured and settled. Most authorizations expire within five to seven days.
     * The issuing bank determines the length of time.
     *
     * @param creditCardNumber
     * @param expirationDate
     * @param amount
     * @param firstName
     * @param lastName
     * @param address
     * @param city
     * @param state
     * @param postalCode
     * @param email
     * @param country
     * @return
     * @throws Exception
     */
    public HashMap authorizePayment(String creditCardNumber, String expirationDate, BigDecimal amount,
                                    String firstName, String lastName, String address, String city,
                                    String state, String postalCode, String email, String country
    ) throws Exception
    {
        return runTransaction(TransactionType.AUTHORIZE, creditCardNumber, expirationDate, amount, firstName,
                lastName, address, city, state, postalCode, email, country, null);
    }


    /**
     * CyberSource supports credits for all processors except CyberSource Latin American
     * Processing.
     * <p/>
     * Request a credit when you need to give the customer a refund. When your request for a
     * credit is successful, the issuing bank for the credit card takes money out of your merchant
     * bank account and returns it to the customer. It usually takes two to four days for your
     * acquiring bank to transfer funds from your merchant bank account.
     *
     * @param orderRequestToken
     * @param creditCardNumber
     * @param expirationDate
     * @param amount
     * @param firstName
     * @param lastName
     * @param address
     * @param city
     * @param state
     * @param postalCode
     * @param email
     * @param country
     * @return
     * @throws Exception
     */
    public HashMap credit(String orderRequestToken, String creditCardNumber, String expirationDate, BigDecimal amount,
                          String firstName, String lastName, String address, String city,
                          String state, String postalCode, String email, String country
    ) throws Exception
    {
        HashMap props = new HashMap();

        return runTransaction(TransactionType.CREDIT, creditCardNumber, expirationDate, amount, firstName,
                lastName, address, city, state, postalCode, email, country, props);
    }

    /**
     * A void uses the request ID and
     * request token returned from a previous service request to link the void to the service.
     * Send the request ID value in the voidService_voidRequestID field and send the request
     * token value in the orderRequestToken field. CyberSource uses these values to look up
     * the customer’s billing and account information from the previous request or service,
     * which means that you are required to include those fields in your void request.
     *
     * @param requestId
     * @param orderRequestToken
     * @return
     * @throws Exception
     */
    public HashMap voidTransaction(String requestId, String orderRequestToken) throws Exception
    {

        String requestID = null;

        HashMap request = new HashMap();

        request.put(TransactionType.VOID.toString(), "true");

        // We will let the Client get the merchantID from props and insert it
        // into the request Map.

        request.put("merchantReferenceCode", "Standard Code");
        request.put("voidService_voidRequestID", requestId);
        request.put("orderRequestToken", orderRequestToken);

        HashMap reply = null;

        try
        {
            reply = Client.runTransaction(request, initialise());
        } catch (ClientException e)
        {
            System.out.println(e.getMessage());
        } catch (FaultException e)
        {
            System.out.println(e.getMessage());
        }

        return reply;
    }

    /**
     * This function can perform a number of different transactions.
     *
     * @param type
     * @param creditCardNumber
     * @param expirationDate
     * @param amount
     * @param firstName
     * @param lastName
     * @param address
     * @param city
     * @param state
     * @param postalCode
     * @param email
     * @param country
     * @return
     * @throws Exception
     */
    public HashMap runTransaction(TransactionType type, String creditCardNumber, String expirationDate, BigDecimal amount,
                                  String firstName, String lastName, String address, String city,
                                  String state, String postalCode, String email, String country, HashMap props
    ) throws Exception
    {
        String requestID = null;

        HashMap request = new HashMap();

        if (props != null)
        {
            request.putAll(props);
        }

        request.put(type.toString(), "true");

        // TODO Should make this available to the user.
        request.put("merchantReferenceCode", "Standard Code");

        request.put("billTo_firstName", firstName);
        request.put("billTo_lastName", lastName);
        request.put("billTo_street1", address);
        request.put("billTo_city", city);
        request.put("billTo_state", state);
        request.put("billTo_postalCode", postalCode);
        request.put("billTo_country", country);
        request.put("billTo_email", email);
        request.put("card_accountNumber", creditCardNumber);

        //Split the expirationDate
        String[] splitDate = expirationDate.split("/");

        if (splitDate.length < 2)
        {
            throw new Exception("The expiration date of: " + expirationDate + "is invalid, it should be in the format of <month>/<year>");
        }

        request.put("card_expirationMonth", splitDate[0]);
        request.put("card_expirationYear", splitDate[1]);
        request.put("purchaseTotals_currency", "USD");
        request.put("purchaseTotals_grandTotalAmount", amount.toString());

        HashMap reply = null;

        try
        {
            reply = Client.runTransaction(request, initialise());
        } catch (ClientException e)
        {
            System.out.println(e.getMessage());
        } catch (FaultException e)
        {
            System.out.println(e.getMessage());
        }

        return reply;
    }


    public Boolean getUseHttpClient()
    {
        return useHttpClient;
    }

    public void setUseHttpClient(Boolean useHttpClient)
    {
        this.useHttpClient = useHttpClient;
    }
}
