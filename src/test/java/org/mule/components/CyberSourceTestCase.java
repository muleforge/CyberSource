/*
 * $Id: $
 * --------------------------------------------------------------------------------------
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 *
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.components;

import org.junit.Before;
import org.junit.Test;
import org.mule.api.MuleMessage;
import org.mule.module.client.MuleClient;
import org.mule.tck.FunctionalTestCase;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Random;

import static org.junit.Assert.assertTrue;

public class CyberSourceTestCase extends FunctionalTestCase
{
    private CyberSource cyberSource;
    private Random rand;

    public CyberSourceTestCase()
    {
        cyberSource = new CyberSource();
        cyberSource.setKeysDirectory("src/test/resources/keys");
        cyberSource.setMerchantId("mule");
        cyberSource.setTestMode(true);
        rand = new Random();
    }


    @Test
    public void testMakePayment() throws Exception
    {

        HashMap reply = cyberSource.makePayment("378282246310005", "12/2012", new BigDecimal(rand.nextInt(200)), "Mule", "Man",
                "30 Maiden Lane", "San Francisco", "CA", "94108", "mule@muleman.com", "US");
        assertTrue("ACCEPT".equalsIgnoreCase(reply.get("decision").toString()));
    }

    @Test
    public void testCredit() throws Exception
    {

        BigDecimal amount = new BigDecimal(rand.nextInt(200));

        HashMap reply = cyberSource.makePayment("378282246310005", "12/2012", amount, "Mule", "Man",
                       "30 Maiden Lane", "San Francisco", "CA", "94108", "mule@muleman.com", "US");

        assertTrue("ACCEPT".equalsIgnoreCase(reply.get("decision").toString()));

        reply = cyberSource.credit(reply.get("requestToken").toString(), "378282246310005", "12/2012", amount, "Mule", "Man",
                "30 Maiden Lane", "San Francisco", "CA", "94108", "mule@muleman.com", "US");

        assertTrue("ACCEPT".equalsIgnoreCase(reply.get("decision").toString()));
    }

    @Override
    protected String getConfigResources()
    {
        return "cybersource-config.xml";
    }

    public void testMakePaymentConfig() throws Exception
    {
        MuleClient client = new MuleClient(muleContext);
        BigDecimal amount = new BigDecimal(rand.nextInt(200));

        HashMap<String, String> payload = new HashMap<String, String>();
        payload.put("address", "30 Maiden Lane");
        payload.put("amount", amount.toString());
        payload.put("city", "San Francisco");
        payload.put("country", "US");
        payload.put("creditCardNumber", "378282246310005");
        payload.put("email", "mule@muleman.com");
        payload.put("expDate", "12/2012");
        payload.put("firstName", "Mule");
        payload.put("lastName", "Man");
        payload.put("postalCode", "94108");
        payload.put("state", "CA");

        MuleMessage result = client.send("vm://makePayment", payload, null);
        assertNotNull(result.getPayload());
        assertTrue(result.getPayload() instanceof HashMap);

        HashMap map = (HashMap)result.getPayload();
        assertTrue(map.get("decision").equals("ACCEPT"));

    }

}
