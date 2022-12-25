/* Modified version of TypedXmlSerializer
 * Original License:
 *
 * Copyright (C) 2020 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.rosstonovsky.abxUtils;

import org.xmlpull.v1.XmlSerializer;

import java.io.IOException;

/**
 * Specialization of {@link XmlSerializer} which adds explicit methods to
 * support consistent and efficient conversion of primitive data types.
 *
 */
public interface TypedXmlSerializer extends XmlSerializer {
	/**
	 * Functionally equivalent to {@link #attribute(String, String, String)} but
	 * with the additional signal that the given value is a candidate for being
	 * canonicalized, similar to {@link String#intern()}.
	 */
	XmlSerializer attributeInterned( String namespace,  String name,
	                                 String value) throws IOException;

	/**
	 * Encode the given strongly-typed value and serialize using
	 * {@link #attribute(String, String, String)}.
	 */
	XmlSerializer attributeBytesHex( String namespace,  String name,
	                                 byte[] value) throws IOException;

	/**
	 * Encode the given strongly-typed value and serialize using
	 * {@link #attribute(String, String, String)}.
	 */
	XmlSerializer attributeBytesBase64( String namespace,  String name,
	                                    byte[] value) throws IOException;

	/**
	 * Encode the given strongly-typed value and serialize using
	 * {@link #attribute(String, String, String)}.
	 */
	XmlSerializer attributeInt( String namespace,  String name,
	                            int value) throws IOException;

	/**
	 * Encode the given strongly-typed value and serialize using
	 * {@link #attribute(String, String, String)}.
	 */
	XmlSerializer attributeIntHex( String namespace,  String name,
	                               int value) throws IOException;

	/**
	 * Encode the given strongly-typed value and serialize using
	 * {@link #attribute(String, String, String)}.
	 */
	XmlSerializer attributeLong( String namespace,  String name,
	                             long value) throws IOException;

	/**
	 * Encode the given strongly-typed value and serialize using
	 * {@link #attribute(String, String, String)}.
	 */
	XmlSerializer attributeLongHex( String namespace,  String name,
	                                long value) throws IOException;

	/**
	 * Encode the given strongly-typed value and serialize using
	 * {@link #attribute(String, String, String)}.
	 */
	XmlSerializer attributeFloat( String namespace,  String name,
	                              float value) throws IOException;

	/**
	 * Encode the given strongly-typed value and serialize using
	 * {@link #attribute(String, String, String)}.
	 */
	XmlSerializer attributeDouble( String namespace,  String name,
	                               double value) throws IOException;

	/**
	 * Encode the given strongly-typed value and serialize using
	 * {@link #attribute(String, String, String)}.
	 */
	XmlSerializer attributeBoolean( String namespace,  String name,
	                                boolean value) throws IOException;
}