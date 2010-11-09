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
    private Properties properties;

    public CyberSource(String merchantId, String keysDirectory)
    {
        this(merchantId, keysDirectory, false);
    }

    public CyberSource(String merchantId, String keysDirectory, Boolean testMode)
    {
        properties = new Properties();
        properties.put("merchantID", merchantId);
        properties.put("keysDirectory", keysDirectory);
        properties.put("targetAPIVersion", "1.28");
        properties.put("logDirectory", "logs");

        if (testMode)
        {
            properties.put("sendToProduction", "false");
            properties.put("enableLog", "true");
        } else
        {
            properties.put("sendToProduction", "true");
            properties.put("enableLog", "false");
        }

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
            reply = Client.runTransaction(request, properties);
        }
        catch (ClientException e)
        {
            System.out.println(e.getMessage());
        }
        catch (FaultException e)
        {
            System.out.println(e.getMessage());
        }

        return reply;
    }

    public HashMap authorizePayment(String creditCardNumber, String expirationDate, BigDecimal amount,
                                    String firstName, String lastName, String address, String city,
                                    String state, String postalCode, String email, String country
    ) throws Exception
    {
        return runTransaction(TransactionType.AUTHORIZE, creditCardNumber, expirationDate, amount, firstName,
                lastName, address, city, state, postalCode, email, country, null);
    }


    public HashMap credit(String orderRequestToken, String creditCardNumber, String expirationDate, BigDecimal amount,
                          String firstName, String lastName, String address, String city,
                          String state, String postalCode, String email, String country
    ) throws Exception
    {
        HashMap props = new HashMap();

        return runTransaction(TransactionType.CREDIT, creditCardNumber, expirationDate, amount, firstName,
                lastName, address, city, state, postalCode, email, country, props);
    }

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
            reply = Client.runTransaction(request, properties);
        }
        catch (ClientException e)
        {
            System.out.println(e.getMessage());
        }
        catch (FaultException e)
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
            reply = Client.runTransaction(request, properties);
        }
        catch (ClientException e)
        {
            System.out.println(e.getMessage());
        }
        catch (FaultException e)
        {
            System.out.println(e.getMessage());
        }

        return reply;
    }
}
