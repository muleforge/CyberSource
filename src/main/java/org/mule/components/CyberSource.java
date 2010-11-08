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
        }
        else
        {
            properties.put("sendToProduction", "true");
            properties.put("enableLog", "false");
        }

    }

    
    public void makePayment(String creditCardNumber, String expirationDate, BigDecimal amount,
                            String firstName, String lastName, String address, String city,
                            String state, String postalCode, String email, String country) throws Exception
    {
        String requestId = authorizePayment(creditCardNumber, expirationDate, amount, firstName, lastName, address, city, state,
                postalCode, email, country);

        if (requestId != null)
        {
            capturePayment(requestId, amount.toString());
        }
    }

    public void capturePayment(String authRequestId, String amount) throws Exception
    {
        String requestID = null;

        HashMap request = new HashMap();

        request.put("ccCaptureService_run", "true");

        // We will let the Client get the merchantID from props and insert it
        // into the request Map.

        request.put("merchantReferenceCode", "Standard Code");
        request.put("ccCaptureService_authRequestID", authRequestId);
        request.put("purchaseTotals_currency", "USD");
        request.put("purchaseTotals_grandTotalAmount", amount.toString());

        try
        {
            HashMap reply = Client.runTransaction(request, properties);
        }
        catch (ClientException e)
        {
            System.out.println(e.getMessage());
        }
        catch (FaultException e)
        {
            System.out.println(e.getMessage());
        }
    }

    public String authorizePayment(String creditCardNumber, String expirationDate, BigDecimal amount,
                                   String firstName, String lastName, String address, String city,
                                   String state, String postalCode, String email, String country
                                   ) throws Exception
    {
        String requestID = null;

        HashMap request = new HashMap();

        request.put("ccAuthService_run", "true");

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

        try
        {

            HashMap reply = Client.runTransaction(request, properties);

            // if the authorization was successful, obtain the request id
            // for the follow-on capture later.
            String decision = (String) reply.get("decision");
            if ("ACCEPT".equalsIgnoreCase(decision))
            {
                requestID = (String) reply.get("requestID");
            }

        }
        catch (ClientException e)
        {
            System.out.println(e.getMessage());
        }
        catch (FaultException e)
        {
            System.out.println(e.getMessage());
        }

        return (requestID);

    }
}
