/**
 * Licensed to Jasig under one or more contributor license
 * agreements. See the NOTICE file distributed with this work
 * for additional information regarding copyright ownership.
 * Jasig licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file
 * except in compliance with the License. You may obtain a
 * copy of the License at:
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.jasig.portlet.emailpreview.dao.exchange;

import java.io.IOException;

import jcifs.ntlmssp.NtlmFlags;
import jcifs.ntlmssp.Type1Message;
import jcifs.ntlmssp.Type2Message;
import jcifs.ntlmssp.Type3Message;
import jcifs.util.Base64;

import org.apache.http.impl.auth.NTLMEngine;
import org.apache.http.impl.auth.NTLMEngineException;

public final class JCIFSEngine implements NTLMEngine {

    private static final int TYPE_1_FLAGS = 
            NtlmFlags.NTLMSSP_NEGOTIATE_56 | 
            NtlmFlags.NTLMSSP_NEGOTIATE_128 | 
            NtlmFlags.NTLMSSP_NEGOTIATE_NTLM2 | 
            NtlmFlags.NTLMSSP_NEGOTIATE_ALWAYS_SIGN | 
            NtlmFlags.NTLMSSP_REQUEST_TARGET;

    public String generateType1Msg(final String domain, final String workstation)
            throws NTLMEngineException {
        final Type1Message type1Message = new Type1Message(TYPE_1_FLAGS, domain, workstation);
        return Base64.encode(type1Message.toByteArray());
    }

    public String generateType3Msg(final String username, final String password,
            final String domain, final String workstation, final String challenge)
            throws NTLMEngineException {
        Type2Message type2Message;
        try {
            type2Message = new Type2Message(Base64.decode(challenge));
        } catch (final IOException exception) {
            throw new NTLMEngineException("Invalid NTLM type 2 message", exception);
        }
        final int type2Flags = type2Message.getFlags();
        final int type3Flags = type2Flags
                & (0xffffffff ^ (NtlmFlags.NTLMSSP_TARGET_TYPE_DOMAIN | NtlmFlags.NTLMSSP_TARGET_TYPE_SERVER));
        final Type3Message type3Message = new Type3Message(type2Message, password, domain,
                username, workstation, type3Flags);
        return Base64.encode(type3Message.toByteArray());
    }

}
