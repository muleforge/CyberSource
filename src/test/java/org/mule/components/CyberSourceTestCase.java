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

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Random;

import static org.junit.Assert.assertTrue;

public class CyberSourceTestCase
{
    private CyberSource cyberSource;
    private Random rand;

    @Before
    public void init()
    {
        cyberSource = new CyberSource("mule", "src/test/resources/keys", true);
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
}
