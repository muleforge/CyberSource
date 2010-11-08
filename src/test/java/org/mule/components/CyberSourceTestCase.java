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

public class CyberSourceTestCase
{
    private CyberSource cyberSource;

    @Before
    public void init()
    {
        cyberSource = new CyberSource("mule", "src/test/resources/keys", true);
    }

    @Test
    public void testMakePayment() throws Exception
    {
        cyberSource.makePayment("378282246310005", "12/2012", new BigDecimal("200"), "Mule", "Man", "30 Maiden Lane", "San Francisco", "CA", "94108", "mule@muleman.com", "US");   
    }
}
