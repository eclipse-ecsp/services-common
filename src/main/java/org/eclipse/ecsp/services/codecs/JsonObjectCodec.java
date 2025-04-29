/********************************************************************************
 * Copyright (c) 2023-24 Harman International
 * 
 * <p>Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * <p>http://www.apache.org/licenses/LICENSE-2.0
 * 
 * <p>Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and\
 * limitations under the License.
 * 
 * <p>SPDX-License-Identifier: Apache-2.0
 ********************************************************************************/

package org.eclipse.ecsp.services.codecs;

import org.bson.BsonBinary;
import org.bson.BsonReader;
import org.bson.BsonWriter;
import org.bson.codecs.Codec;
import org.bson.codecs.DecoderContext;
import org.bson.codecs.EncoderContext;
import org.json.JSONObject;
import java.nio.charset.StandardCharsets;

/**
 * Encode/Decode data to JSONObject.
 */
public class JsonObjectCodec implements Codec<JSONObject> {

    @Override
    public JSONObject decode(BsonReader bsonReader, DecoderContext decoderContext) {
        // BinaryData.getData() cannot be null
        return new JSONObject(
            new String(bsonReader.readBinaryData().getData(), StandardCharsets.UTF_8));
    }
    
    @Override
    public void encode(BsonWriter bsonWriter, JSONObject jsonObject, EncoderContext encoderContext) {
        if (jsonObject == null) {
            throw new IllegalArgumentException("JSONObject is null");
        }
        
        bsonWriter.writeBinaryData(
            new BsonBinary(jsonObject.toString().getBytes(StandardCharsets.UTF_8)));
    }
    
    @Override
    public Class<JSONObject> getEncoderClass() {
        return JSONObject.class;
    }
}
