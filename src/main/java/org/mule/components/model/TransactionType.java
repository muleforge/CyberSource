/*
 * $Id: $
 * --------------------------------------------------------------------------------------
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 *
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.components.model;

import java.util.HashMap;
import java.util.Map;

public enum TransactionType
{
    AUTHORIZE("ccAuthService_run"),
    CREDIT("ccCreditService_run"),
    VOID("voidService_run"),
    CAPTURE("ccCaptureService_run");

    private final String value;
    private static final Map<String, TransactionType> lookup = new HashMap<String, TransactionType>();

    static
    {
        for (TransactionType rc : TransactionType.values())
        {
            lookup.put(rc.toString(), rc);
        }
    }

    private TransactionType(String value)
    {
        this.value = value;
    }

    public static TransactionType get(String value)
    {
        return lookup.get(value);
    }

    public String toString()
    {
        return value;
    }
}
