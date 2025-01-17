/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.dubbo.remoting.http12.message.codec;

import org.apache.dubbo.remoting.http12.message.HttpMessageCodec;
import org.apache.dubbo.remoting.http12.message.HttpMessageDecoder;
import org.apache.dubbo.rpc.model.FrameworkModel;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Map;
import java.util.Random;

import com.google.common.base.Charsets;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class CodecTest {

    final String MULTIPART_SAMPLE_1 = "--example-part-boundary\r\n"
            + "Content-Disposition: form-data; name=\"username\"\r\n"
            + "Content-Type: text/plain\r\n"
            + "\r\n"
            + "LuYue\r\n"
            + "--example-part-boundary\r\n"
            + "Content-Disposition: form-data; name=\"userdetail\"\r\n"
            + "Content-Type: application/json\r\n"
            + "\r\n"
            + "{\"location\":\"beijing\",\"username\":\"LuYue\"}\r\n"
            + "--example-part-boundary\r\n"
            + "Content-Disposition: form-data; name=\"userimg\"; filename=\"user.jpeg\"\r\n"
            + "Content-Type: image/jpeg\r\n"
            + "\r\n"
            + "<binary-image data>\r\n"
            + "--example-part-boundary--\r\n";

    final String MULTIPART_SAMPLE_2 = "--boundary123\r\n" + "Content-Disposition: form-data; name=\"text\"\r\n"
            + "Content-Type: text/plain\r\n"
            + "\r\n"
            + "simple text\r\n"
            + "--boundary123\r\n"
            + "Content-Disposition: form-data; name=\"file\"; filename=\"example.txt\"\r\n"
            + "Content-Type: text/plain\r\n"
            + "\r\n"
            + "This is the content of the file.\r\n"
            + "--boundary123--\r\n";

    final String MULTIPART_SAMPLE_3 = "--boundaryABC\r\n" + "Content-Disposition: form-data; name=\"someContent\"\r\n"
            + "\r\n"
            + "这是一些中文内容\r\n"
            + "--boundaryABC\r\n"
            + "Content-Disposition: form-data; name=\"emoji\"\r\n"
            + "\r\n"
            + "\uD83D\uDE0A\r\n"
            + "--boundaryABC--";

    final String MULTIPART_SAMPLE_4 = "--longValue\r\n" + "Content-Disposition: form-data; name=\"long\"\r\n"
            + "\r\n"
            + "This is a really long value that continues for many lines.This is a really long value that continues for many lines.This is a really long value that continues for many lines.This is a really long value that continues for many lines.This is a really long value that continues for many lines.This is a really long value that continues for many lines.This is a really long value that continues for many lines.This is a really long value that continues for many lines.This is a really long value that continues for many lines.This is a really long value that continues for many lines.This is a really long value that continues for many lines.This is a really long value that continues for many lines.This is a really long value that continues for many lines.This is a really long value that continues for many lines.This is a really long value that continues for many lines.This is a really long value that continues for many lines.This is a really long value that continues for many lines.This is a really long value that continues for many lines.This is a really long value that continues for many lines.This is a really long value that continues for many lines.This is a really long value that continues for many lines.This is a really long value that continues for many lines.This is a really long value that continues for many lines.This is a really long value that continues for many lines.This is a really long value that continues for many lines.This is a really long value that continues for many lines.This is a really long value that continues for many lines.This is a really long value that continues for many lines.This is a really long value that continues for many lines.This is a really long value that continues for many lines.This is a really long value that continues for many lines.This is a really long value that continues for many lines.This is a really long value that continues for many lines.This is a really long value that continues for many lines.This is a really long value that continues for many lines.This is a really long value that continues for many lines.This is a really long value that continues for many lines.This is a really long value that continues for many lines.This is a really long value that continues for many lines.This is a really long value that continues for many lines.This is a really long value that continues for many lines.This is a really long value that continues for many lines.This is a really long value that continues for many lines.This is a really long value that continues for many lines.This is a really long value that continues for many lines.This is a really long value that continues for many lines.\r\n"
            + "--longValue--\r\n";

    final String MULTIPART_SAMPLE_5 = "--specialChar\r\n" + "Content-Disposition: form-data; name=\"special\"\r\n"
            + "\r\n"
            + "Line 1\n"
            + "Line 2\r\n"
            + "--Line 3--\n"
            + "Line 4\n\r\n"
            + "--specialChar--";

    CodecUtils codecUtils;

    @BeforeEach
    void beforeAll() {
        codecUtils = FrameworkModel.defaultModel().getBeanFactory().getOrRegisterBean(CodecUtils.class);
    }

    @Test
    void testMultipartForm1() {
        InputStream in = new ByteArrayInputStream(MULTIPART_SAMPLE_1.getBytes());
        HttpMessageDecoder decoder = new MultipartDecoder(
                null, FrameworkModel.defaultModel(), "multipart/form-data; boundary=example-part-boundary", codecUtils);
        Object[] result = decoder.decode(in, new Class[] {String.class, User.class, byte[].class});
        Assertions.assertEquals("LuYue", result[0]);
        Assertions.assertTrue(result[1] instanceof User);
        Assertions.assertEquals("LuYue", ((User) result[1]).getUsername());
        Assertions.assertEquals("beijing", ((User) result[1]).getLocation());
        Assertions.assertEquals("<binary-image data>", new String((byte[]) result[2], Charsets.UTF_8));
    }

    @Test
    void testMultipartForm2() {
        InputStream in = new ByteArrayInputStream(MULTIPART_SAMPLE_2.getBytes());
        HttpMessageDecoder decoder = new MultipartDecoder(
                null, FrameworkModel.defaultModel(), "multipart/form-data; boundary=boundary123", codecUtils);
        Object[] result = decoder.decode(in, new Class[] {String.class, byte[].class});
        Assertions.assertEquals("simple text", result[0]);
        Assertions.assertEquals(
                "This is the content of the file.", new String((byte[]) result[1], StandardCharsets.US_ASCII));
    }

    @Test
    void testMultipartForm3() {
        InputStream in = new ByteArrayInputStream(MULTIPART_SAMPLE_3.getBytes());
        HttpMessageDecoder decoder = new MultipartDecoder(
                null, FrameworkModel.defaultModel(), "multipart/form-data; boundary=boundaryABC", codecUtils);
        Object[] result = decoder.decode(in, new Class[] {String.class, String.class});
        Assertions.assertEquals("这是一些中文内容", result[0]);
        Assertions.assertEquals("😊", result[1]);
    }

    @Test
    void testMultipartForm4() {
        InputStream in = new ByteArrayInputStream(MULTIPART_SAMPLE_4.getBytes());
        HttpMessageDecoder decoder = new MultipartDecoder(
                null, FrameworkModel.defaultModel(), "multipart/form-data; boundary=longValue", codecUtils);
        Object[] result = decoder.decode(in, new Class[] {String.class});
        Assertions.assertEquals(
                "This is a really long value that continues for many lines.This is a really long value that continues for many lines.This is a really long value that continues for many lines.This is a really long value that continues for many lines.This is a really long value that continues for many lines.This is a really long value that continues for many lines.This is a really long value that continues for many lines.This is a really long value that continues for many lines.This is a really long value that continues for many lines.This is a really long value that continues for many lines.This is a really long value that continues for many lines.This is a really long value that continues for many lines.This is a really long value that continues for many lines.This is a really long value that continues for many lines.This is a really long value that continues for many lines.This is a really long value that continues for many lines.This is a really long value that continues for many lines.This is a really long value that continues for many lines.This is a really long value that continues for many lines.This is a really long value that continues for many lines.This is a really long value that continues for many lines.This is a really long value that continues for many lines.This is a really long value that continues for many lines.This is a really long value that continues for many lines.This is a really long value that continues for many lines.This is a really long value that continues for many lines.This is a really long value that continues for many lines.This is a really long value that continues for many lines.This is a really long value that continues for many lines.This is a really long value that continues for many lines.This is a really long value that continues for many lines.This is a really long value that continues for many lines.This is a really long value that continues for many lines.This is a really long value that continues for many lines.This is a really long value that continues for many lines.This is a really long value that continues for many lines.This is a really long value that continues for many lines.This is a really long value that continues for many lines.This is a really long value that continues for many lines.This is a really long value that continues for many lines.This is a really long value that continues for many lines.This is a really long value that continues for many lines.This is a really long value that continues for many lines.This is a really long value that continues for many lines.This is a really long value that continues for many lines.This is a really long value that continues for many lines.",
                result[0]);
    }

    @Test
    void testMultipartForm5() {
        InputStream in = new ByteArrayInputStream(MULTIPART_SAMPLE_5.getBytes());
        HttpMessageDecoder decoder = new MultipartDecoder(
                null, FrameworkModel.defaultModel(), "multipart/form-data; boundary=specialChar", codecUtils);
        Object[] result = decoder.decode(in, new Class[] {String.class});
        Assertions.assertEquals("Line 1\n" + "Line 2\r\n" + "--Line 3--\n" + "Line 4\n", result[0]);
    }

    @Test
    void testMultipartFormBodyBoundary() {
        // check if codec can handle boundary correctly when the end delimiter just beyond the buffer.
        // header buffer size: 128; body buffer size: 256
        // --example-boundary\r\n   [20bytes]
        // Content-Type: plain/text [paddings]\r\n\r\n [108bytes]
        // body1r\n [238bytes, binary data]
        // --example-boundary--\r\n [22bytes] , last --\r\n [4bytes] beyond buffer
        byte[] boundary = "--example-boundary\r\n".getBytes();
        byte[] header = "Content-Type: plain/text".getBytes();
        byte[] headerPadding = new byte[128 - header.length - boundary.length - "\r\n\r\n".length()];
        byte[] headerBytes = new byte[128];
        byte[] body1 = new byte[238];
        byte[] end = "--example-boundary--\r\n".getBytes();
        byte[] bodyWithEnd = new byte[260];

        Random random = new Random();
        random.nextBytes(body1);
        body1[236] = '\r';
        body1[237] = '\n';
        Arrays.fill(headerPadding, (byte) 0);

        System.arraycopy(boundary, 0, headerBytes, 0, boundary.length);
        System.arraycopy(header, 0, headerBytes, boundary.length, header.length);

        System.arraycopy(headerPadding, 0, headerBytes, boundary.length + header.length, headerPadding.length);
        System.arraycopy(
                "\r\n\r\n".getBytes(),
                0,
                headerBytes,
                boundary.length + header.length + headerPadding.length,
                "\r\n\r\n".length());
        System.arraycopy(body1, 0, bodyWithEnd, 0, body1.length);
        System.arraycopy(end, 0, bodyWithEnd, body1.length, end.length);

        byte[] fullRequestBody = new byte[256 + 128 + 4];
        System.arraycopy(headerBytes, 0, fullRequestBody, 0, headerBytes.length);
        System.arraycopy(bodyWithEnd, 0, fullRequestBody, headerBytes.length, bodyWithEnd.length);

        HttpMessageDecoder decoder = new MultipartDecoderFactory()
                .createCodec(null, FrameworkModel.defaultModel(), "multipart/form-data; boundary=example-boundary");
        byte[] res = (byte[]) decoder.decode(new ByteArrayInputStream(fullRequestBody), byte[].class);

        for (int k = 0; k < body1.length - 2; k++) {
            Assertions.assertEquals(body1[k], res[k]);
        }
    }

    @Test
    void testMultipartFormBodyBoundary2() {
        // check if codec can handle boundary correctly when the end delimiter just beyond the buffer.
        // header buffer size: 128; body buffer size: 256
        // --example-boundary-\r\n   [21bytes]
        // Content-Type: plain/text [paddings]\r\n\r\n [107bytes]
        // body1r\n  [237bytes, binary data]
        // --example-boundary-\r\n [21bytes] , \r\n [2bytes] beyond buffer
        // body2\r\n [7bytes, text data]
        // --example-boundary--- [23bytes]
        byte[] boundary = "--example-boundary-\r\n".getBytes();
        byte[] subHeader = "Content-Type: plain/text".getBytes();
        byte[] subHeaderWithCRLF = "Content-Type: plain/text\r\n\r\n".getBytes();
        byte[] headerPadding = new byte[128 - subHeader.length - boundary.length - "\r\n\r\n".length()];
        byte[] headerBytes = new byte[128];
        byte[] body1 = new byte[237];
        byte[] body1WithEnd = new byte[body1.length + boundary.length];
        byte[] body2 = "body2\r\n".getBytes();
        byte[] end = "--example-boundary---\r\n".getBytes();

        Random random = new Random();
        random.nextBytes(body1);
        body1[body1.length - 1] = '\n';
        body1[body1.length - 2] = '\r';
        Arrays.fill(headerPadding, (byte) 0);

        System.arraycopy(boundary, 0, headerBytes, 0, boundary.length);
        System.arraycopy(subHeader, 0, headerBytes, boundary.length, subHeader.length);
        System.arraycopy(headerPadding, 0, headerBytes, boundary.length + subHeader.length, headerPadding.length);
        System.arraycopy(
                "\r\n\r\n".getBytes(),
                0,
                headerBytes,
                boundary.length + subHeader.length + headerPadding.length,
                "\r\n\r\n".length());
        System.arraycopy(body1, 0, body1WithEnd, 0, body1.length);
        System.arraycopy(boundary, 0, body1WithEnd, body1.length, boundary.length);

        byte[] fullRequestBody = new byte
                [headerBytes.length + body1WithEnd.length + subHeaderWithCRLF.length + body2.length + end.length];
        System.arraycopy(headerBytes, 0, fullRequestBody, 0, headerBytes.length);
        System.arraycopy(body1WithEnd, 0, fullRequestBody, headerBytes.length, body1WithEnd.length);
        System.arraycopy(
                subHeaderWithCRLF,
                0,
                fullRequestBody,
                headerBytes.length + body1WithEnd.length,
                subHeaderWithCRLF.length);
        System.arraycopy(
                body2,
                0,
                fullRequestBody,
                headerBytes.length + body1WithEnd.length + subHeaderWithCRLF.length,
                body2.length);
        System.arraycopy(
                end,
                0,
                fullRequestBody,
                headerBytes.length + body1WithEnd.length + subHeaderWithCRLF.length + body2.length,
                end.length);

        HttpMessageDecoder decoder = new MultipartDecoder(
                null, FrameworkModel.defaultModel(), "multipart/form-data; boundary=example-boundary-", codecUtils);
        Object[] r =
                decoder.decode(new ByteArrayInputStream(fullRequestBody), new Class[] {byte[].class, String.class});
        byte[] res = (byte[]) r[0];
        for (int k = 0; k < body1.length - 2; k++) {
            Assertions.assertEquals(body1[k], res[k]);
        }
        String res2 = (String) r[1];
        Assertions.assertEquals("body2", res2);
    }

    @Test
    void testUrlForm() {
        String content = "Hello=World&Apache=Dubbo&id=10086";
        InputStream in = new ByteArrayInputStream(content.getBytes());
        UrlEncodeFormCodecFactory factory = new UrlEncodeFormCodecFactory(FrameworkModel.defaultModel());
        HttpMessageCodec codec = factory.createCodec(null, FrameworkModel.defaultModel(), null);
        Object res = codec.decode(in, Map.class);
        Assertions.assertTrue(res instanceof Map);
        Map<String, String> r = (Map<String, String>) res;
        Assertions.assertEquals("World", r.get("Hello"));
        Assertions.assertEquals("Dubbo", r.get("Apache"));
        Assertions.assertEquals("10086", r.get("id"));
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        codec.encode(outputStream, r);
        Assertions.assertEquals(content, outputStream.toString());
        try {
            in.reset();
        } catch (IOException e) {
        }
        Object[] res2 = codec.decode(in, new Class[] {String.class, String.class, Long.class});
        Assertions.assertEquals("World", res2[0]);
        Assertions.assertEquals("Dubbo", res2[1]);
        Assertions.assertEquals(10086L, res2[2]);
        outputStream = new ByteArrayOutputStream();
        codec.encode(outputStream, content);
        Assertions.assertEquals(content, outputStream.toString());
    }

    @Test
    void testUrlForm2() {
        InputStream in = new ByteArrayInputStream("Hello=World&Apache=Dubbo&empty1=&empty2=".getBytes());
        HttpMessageCodec codec = new UrlEncodeFormCodecFactory(FrameworkModel.defaultModel())
                .createCodec(null, FrameworkModel.defaultModel(), null);
        Object res = codec.decode(in, Map.class);
        Assertions.assertTrue(res instanceof Map);
        Map<String, String> r = (Map<String, String>) res;
        Assertions.assertEquals("World", r.get("Hello"));
        Assertions.assertEquals("Dubbo", r.get("Apache"));
        Assertions.assertEquals("", r.get("empty1"));
        Assertions.assertEquals("", r.get("empty2"));
    }

    @Test
    void testUrlForm3() {
        InputStream in = new ByteArrayInputStream("empty1=&empty2=&Hello=world&empty3=&Apache=dubbo&".getBytes());
        HttpMessageCodec codec = new UrlEncodeFormCodecFactory(FrameworkModel.defaultModel())
                .createCodec(null, FrameworkModel.defaultModel(), null);
        Object res = codec.decode(in, Map.class);
        Assertions.assertTrue(res instanceof Map);
        Map<String, String> r = (Map<String, String>) res;
        Assertions.assertEquals("world", r.get("Hello"));
        Assertions.assertEquals("dubbo", r.get("Apache"));
        Assertions.assertEquals("", r.get("empty1"));
        Assertions.assertEquals("", r.get("empty2"));
        Assertions.assertEquals("", r.get("empty3"));
    }

    @Test
    void testUrlForm4() {
        InputStream in = new ByteArrayInputStream("empty1=&empty2=&Hello=world&你好=世界&empty3=&Apache=dubbo&".getBytes());
        HttpMessageCodec codec = new UrlEncodeFormCodecFactory(FrameworkModel.defaultModel())
                .createCodec(null, FrameworkModel.defaultModel(), null);
        Object res = codec.decode(in, Map.class);
        Assertions.assertTrue(res instanceof Map);
        Map<String, String> r = (Map<String, String>) res;
        Assertions.assertEquals("world", r.get("Hello"));
        Assertions.assertEquals("dubbo", r.get("Apache"));
        Assertions.assertEquals("", r.get("empty1"));
        Assertions.assertEquals("", r.get("empty2"));
        Assertions.assertEquals("", r.get("empty3"));
        Assertions.assertEquals("世界", r.get("你好"));
    }

    @Test
    void testXml() {
        String content = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>"
                + "<user><location>New York</location><username>JohnDoe</username></user>";
        InputStream in = new ByteArrayInputStream(content.getBytes());
        HttpMessageCodec codec = new XmlCodecFactory().createCodec(null, FrameworkModel.defaultModel(), null);
        User user = (User) codec.decode(in, User.class);
        Assertions.assertEquals("JohnDoe", user.getUsername());
        Assertions.assertEquals("New York", user.getLocation());

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        codec.encode(outputStream, user);
        String res = outputStream.toString();
        Assertions.assertEquals(content, res);
    }

    @Test
    void testPlainText() {
        byte[] asciiBytes = new byte[] {
            0x48, 0x65, 0x6C, 0x6C,
            0x6F, 0x2C, 0x20, 0x77,
            0x6F, 0x72, 0x6C, 0x64
        };
        byte[] utf8Bytes = new byte[] {
            (byte) 0xE4, (byte) 0xBD, (byte) 0xA0,
            (byte) 0xE5, (byte) 0xA5, (byte) 0xBD,
            (byte) 0xEF, (byte) 0xBC, (byte) 0x8C,
            (byte) 0xE4, (byte) 0xB8, (byte) 0x96,
            (byte) 0xE7, (byte) 0x95, (byte) 0x8C
        };
        byte[] utf16Bytes = new byte[] {0x4F, 0x60, 0x59, 0x7D, (byte) 0xFF, 0x0C, 0x4E, 0x16, 0x75, 0x4C};
        InputStream in = new ByteArrayInputStream(asciiBytes);
        HttpMessageCodec codec = new PlainTextCodecFactory()
                .createCodec(null, FrameworkModel.defaultModel(), "text/plain; charset=ASCII");
        String res = (String) codec.decode(in, String.class);
        Assertions.assertEquals("Hello, world", res);

        in = new ByteArrayInputStream(utf8Bytes);
        codec = new PlainTextCodec();
        res = (String) codec.decode(in, String.class, Charsets.UTF_8);
        Assertions.assertEquals("你好，世界", res);

        in = new ByteArrayInputStream(utf16Bytes);
        res = (String) codec.decode(in, String.class, Charsets.UTF_16);
        Assertions.assertEquals("你好，世界", res);
    }
}
