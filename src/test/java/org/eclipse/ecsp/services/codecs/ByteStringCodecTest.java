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
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.only;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ByteStringCodecTest {
    private final ByteStringCodec codec = new ByteStringCodec();
    
    @Test
    void decode() {
        byte[] bytes = "ABCD".getBytes();
        
        BsonReader bsonReader = mock(BsonReader.class);
        when(bsonReader.readBinaryData()).thenReturn(new BsonBinary(bytes));
        
        ByteString byteString = codec.decode(bsonReader, null);
        
        assertArrayEquals(bytes, byteString.toByteArray());
    }
    
    @Test
    void decode_EmptyByteArray() {
        BsonBinary bsonBinary = mock(BsonBinary.class);
        when(bsonBinary.getData()).thenReturn(new byte[0]);
        
        BsonReader bsonReader = mock(BsonReader.class);
        when(bsonReader.readBinaryData()).thenReturn(bsonBinary);
        
        assertNotNull(codec.decode(bsonReader, null));
    }
    
    @Test
    void encode() {
        BsonWriter bsonWriter = mock(BsonWriter.class);
        
        byte[] bytes = "ABCD".getBytes();
        
        codec.encode(bsonWriter, ByteString.copyFrom(bytes), null);
        
        verify(bsonWriter, only()).writeBinaryData(argThat(bsonBinary -> {
            assertArrayEquals(bytes, bsonBinary.getData());
            return true;
        }));
    }
    
    @Test
    void encodeNull() {
        assertThrows(IllegalArgumentException.class, () -> {
            codec.encode(null, null, null);
        });
    }
    
    
    @Test
    void getEncoderClass() {
        assertEquals(ByteString.class, codec.getEncoderClass());
    }
}