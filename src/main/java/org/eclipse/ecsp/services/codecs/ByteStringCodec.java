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

import com.google.protobuf.ByteString;
import org.bson.BsonBinary;
import org.bson.BsonReader;
import org.bson.BsonWriter;
import org.bson.codecs.Codec;
import org.bson.codecs.DecoderContext;
import org.bson.codecs.EncoderContext;

/**
 * Codec for encoding/decoding data to/from byteString.
 * ref https://confluence.harman.com/confluence/display/HCP/Morphia+Upgrade+From+1.6.1+to+2.2.3
 */
public class ByteStringCodec implements Codec<ByteString> {

    @Override
    public ByteString decode(BsonReader bsonReader, DecoderContext decoderContext) {
        // BinaryData.getData() cannot be null
        return ByteString.copyFrom(bsonReader.readBinaryData().getData());
    }
    
    @Override
    public void encode(BsonWriter bsonWriter, ByteString byteString, EncoderContext encoderContext) {
        if (byteString == null) {
            throw new IllegalArgumentException("ByteString is null");
        }
        
        bsonWriter.writeBinaryData(new BsonBinary(byteString.toByteArray()));
    }
    
    @Override
    public Class<ByteString> getEncoderClass() {
        return ByteString.class;
    }
}
