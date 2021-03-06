/*
 * Copyright 2006-2010 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.consol.citrus.validation.builder;

import com.consol.citrus.context.TestContext;
import com.consol.citrus.message.Message;
import com.consol.citrus.message.MessageDirection;
import com.consol.citrus.validation.interceptor.MessageConstructionInterceptor;
import com.consol.citrus.variable.dictionary.DataDictionary;

/**
 * Interface for classes beeing able to build control messages for validation.
 * 
 * @author Christoph Deppisch
 */
public interface MessageContentBuilder {
    
    /**
     * Builds the control message. 
     * @param context the current test context.
     * @param messageType the message type to build.
     * @param direction
     * @return the constructed message object.
     */
    Message buildMessageContent(TestContext context, String messageType, MessageDirection direction);

    /**
     * Builds the control message.
     * @param context the current test context.
     * @param messageType the message type to build.
     * @return the constructed message object.
     * @deprecated in favor of using {@link #buildMessageContent(TestContext, String, MessageDirection)}.
     */
    @Deprecated
    default Message buildMessageContent(TestContext context, String messageType){
        return buildMessageContent(context, messageType, MessageDirection.UNBOUND);
    }

    /**
     * Adds a message construction interceptor.
     * @param interceptor
     */
    void add(MessageConstructionInterceptor interceptor);

    /**
     * Sets explicit data dictionary for this message builder.
     * @param dataDictionary
     */
    void setDataDictionary(DataDictionary dataDictionary);
}
