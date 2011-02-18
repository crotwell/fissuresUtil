/*
 * The Apache Software License, Version 1.1
 *
 *
 * Copyright (c) 1999-2002 The Apache Software Foundation.  All rights
 * reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in
 *    the documentation and/or other materials provided with the
 *    distribution.
 *
 * 3. The end-user documentation included with the redistribution,
 *    if any, must include the following acknowledgment:
 *       "This product includes software developed by the
 *        Apache Software Foundation (http://www.apache.org/)."
 *    Alternately, this acknowledgment may appear in the software itself,
 *    if and wherever such third-party acknowledgments normally appear.
 *
 * 4. The names "Xerces" and "Apache Software Foundation" must
 *    not be used to endorse or promote products derived from this
 *    software without prior written permission. For written
 *    permission, please contact apache@apache.org.
 *
 * 5. Products derived from this software may not be called "Apache",
 *    nor may "Apache" appear in their name, without prior written
 *    permission of the Apache Software Foundation.
 *
 * THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED.  IN NO EVENT SHALL THE APACHE SOFTWARE FOUNDATION OR
 * ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF
 * USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT
 * OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
 * SUCH DAMAGE.
 * ====================================================================
 *
 * This software consists of voluntary contributions made by many
 * individuals on behalf of the Apache Software Foundation and was
 * originally based on software copyright (c) 1999, International
 * Business Machines, Inc., http://www.apache.org.  For more
 * information on the Apache Software Foundation, please see
 * <http://www.apache.org/>.
 */

//package dom;
package edu.sc.seis.fissuresUtil.xml;

import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;

import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.DocumentType;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

/**
 * A sample DOM writer. This sample program illustrates how to
 * traverse a DOM tree in order to print a document that is parsed.
 *
 * @author Andy Clark, IBM
 *
 * @version $Id: Writer.java 22072 2011-02-18 15:43:18Z crotwell $
 */
public class Writer {

    //
    // Constants
    //

    // feature ids

    /** Namespaces feature id (http://xml.org/sax/features/namespaces). */
    protected static final String NAMESPACES_FEATURE_ID = "http://xml.org/sax/features/namespaces";

    /** Validation feature id (http://xml.org/sax/features/validation). */
    protected static final String VALIDATION_FEATURE_ID = "http://xml.org/sax/features/validation";

    /** Schema validation feature id (http://apache.org/xml/features/validation/schema). */
    protected static final String SCHEMA_VALIDATION_FEATURE_ID = "http://apache.org/xml/features/validation/schema";

    /** Schema full checking feature id (http://apache.org/xml/features/validation/schema-full-checking). */
    protected static final String SCHEMA_FULL_CHECKING_FEATURE_ID = "http://apache.org/xml/features/validation/schema-full-checking";

    // property ids

    /** Lexical handler property id (http://xml.org/sax/properties/lexical-handler). */
    protected static final String LEXICAL_HANDLER_PROPERTY_ID = "http://xml.org/sax/properties/lexical-handler";

    // default settings

    /** Default parser name. */
    protected static final String DEFAULT_PARSER_NAME = "dom.wrappers.Xerces";

    /** Default namespaces support (true). */
    protected static final boolean DEFAULT_NAMESPACES = true;

    /** Default validation support (false). */
    protected static final boolean DEFAULT_VALIDATION = false;

    /** Default Schema validation support (false). */
    protected static final boolean DEFAULT_SCHEMA_VALIDATION = false;

    /** Default Schema full checking support (false). */
    protected static final boolean DEFAULT_SCHEMA_FULL_CHECKING = false;

    /** Default canonical output (false). */
    protected static final boolean DEFAULT_CANONICAL = false;

    //
    // Data
    //

    /** Print writer. */
    protected PrintWriter fOut;

    /** Canonical output. */
    protected boolean fCanonical;

    /** Print newlines after end elements **/
    protected boolean printNewlines;

    //
    // Constructors
    //

    /** Default constructor. */
    public Writer() {
        this(false);
    } // <init>()

    public Writer(boolean canonical) {
        this(canonical, false);
    } // <init>(boolean)

    public Writer(boolean canonical, boolean printNewlines){
        fCanonical = canonical;
        this.printNewlines = printNewlines;
    }

    //
    // Public methods
    //

    /** Sets whether output is canonical. */
    public void setCanonical(boolean canonical) {
        fCanonical = canonical;
    } // setCanonical(boolean)

    /** Sets the output stream for printing. */
    public void setOutput(OutputStream stream, String encoding)
        throws UnsupportedEncodingException {

        if (encoding == null) {
            encoding = "UTF8";
        }

        java.io.Writer writer = new OutputStreamWriter(stream, encoding);
        fOut = new PrintWriter(writer);

    } // setOutput(OutputStream,String)

    /** Sets the output writer. */
    public void setOutput(java.io.Writer writer) {

        fOut = writer instanceof PrintWriter
            ? (PrintWriter)writer : new PrintWriter(writer);

    } // setOutput(java.io.Writer)

    /** Writes the specified node, recursively. */
    public void write(Node node) {

        // is there anything to do?
        if (node == null) {
            return;
        }

        short type = node.getNodeType();
        switch (type) {
            case Node.DOCUMENT_NODE: {
                    Document document = (Document)node;
                    if (!fCanonical) {
                        fOut.println("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
                        fOut.flush();
                        write(document.getDoctype());
                    }
                    write(document.getDocumentElement());
                    break;
                }

            case Node.DOCUMENT_TYPE_NODE: {
                    DocumentType doctype = (DocumentType)node;
                    fOut.print("<!DOCTYPE ");
                    fOut.print(doctype.getName());
                    String publicId = doctype.getPublicId();
                    String systemId = doctype.getSystemId();
                    if (publicId != null) {
                        fOut.print(" PUBLIC '");
                        fOut.print(publicId);
                        fOut.print("' '");
                        fOut.print(systemId);
                        fOut.print('\'');
                    }
                    else {
                        fOut.print(" SYSTEM '");
                        fOut.print(systemId);
                        fOut.print('\'');
                    }
                    String internalSubset = doctype.getInternalSubset();
                    if (internalSubset != null) {
                        if (printNewlines){
                            fOut.println(" [");
                        } else{
                            fOut.print(" [");
                        }
                        fOut.print(internalSubset);
                        fOut.print(']');
                    }
                    fOut.println('>');
                    break;
                }

            case Node.ELEMENT_NODE: {
                    fOut.print('<');
                    fOut.print(node.getNodeName());
                    Attr attrs[] = sortAttributes(node.getAttributes());
                    for (int i = 0; i < attrs.length; i++) {
                        Attr attr = attrs[i];
                        fOut.print(' ');
                        fOut.print(attr.getNodeName());
                        fOut.print("=\"");
                        normalizeAndPrint(attr.getNodeValue());
                        fOut.print('"');
                    }
                    fOut.print('>');
                    fOut.flush();

                    Node child = node.getFirstChild();
                    while (child != null) {
                        write(child);
                        child = child.getNextSibling();
                    }
                    break;
                }

            case Node.ENTITY_REFERENCE_NODE: {
                    if (fCanonical) {
                        Node child = node.getFirstChild();
                        while (child != null) {
                            write(child);
                            child = child.getNextSibling();
                        }
                    }
                    else {
                        fOut.print('&');
                        fOut.print(node.getNodeName());
                        fOut.print(';');
                        fOut.flush();
                    }
                    break;
                }

            case Node.CDATA_SECTION_NODE: {
                    if (fCanonical) {
                        normalizeAndPrint(node.getNodeValue());
                    }
                    else {
                        fOut.print("<![CDATA[");
                        fOut.print(node.getNodeValue());
                        fOut.print("]]>");
                    }
                    fOut.flush();
                    break;
                }

            case Node.TEXT_NODE: {
                    normalizeAndPrint(node.getNodeValue());
                    fOut.flush();
                    break;
                }

            case Node.PROCESSING_INSTRUCTION_NODE: {
                    fOut.print("<?");
                    fOut.print(node.getNodeName());
                    String data = node.getNodeValue();
                    if (data != null && data.length() > 0) {
                        fOut.print(' ');
                        fOut.print(data);
                    }
                    if (printNewlines){
                        fOut.println("?>");
                    } else {
                        fOut.print("?>");
                    }
                    fOut.flush();
                    break;
                }
        }

        if (type == Node.ELEMENT_NODE) {
            fOut.print("</");
            fOut.print(node.getNodeName());
            if (printNewlines){
                fOut.println('>');
            } else {
                fOut.print('>');
            }
            fOut.flush();
        }

    } // write(Node)

    /** Returns a sorted list of attributes. */
    protected Attr[] sortAttributes(NamedNodeMap attrs) {

        int len = (attrs != null) ? attrs.getLength() : 0;
        Attr array[] = new Attr[len];
        for (int i = 0; i < len; i++) {
            array[i] = (Attr)attrs.item(i);
        }
        for (int i = 0; i < len - 1; i++) {
            String name = array[i].getNodeName();
            int index = i;
            for (int j = i + 1; j < len; j++) {
                String curName = array[j].getNodeName();
                if (curName.compareTo(name) < 0) {
                    name = curName;
                    index = j;
                }
            }
            if (index != i) {
                Attr temp = array[i];
                array[i] = array[index];
                array[index] = temp;
            }
        }

        return array;

    } // sortAttributes(NamedNodeMap):Attr[]

    //
    // Protected methods
    //

    /** Normalizes and prints the given string. */
    protected void normalizeAndPrint(String s) {

        int len = (s != null) ? s.length() : 0;
        for (int i = 0; i < len; i++) {
            char c = s.charAt(i);
            normalizeAndPrint(c);
        }

    } // normalizeAndPrint(String)

    /** Normalizes and print the given character. */
    protected void normalizeAndPrint(char c) {

        switch (c) {
            case '<': {
                    fOut.print("&lt;");
                    break;
                }
            case '>': {
                    fOut.print("&gt;");
                    break;
                }
            case '&': {
                    fOut.print("&amp;");
                    break;
                }
            case '"': {
                    fOut.print("&quot;");
                    break;
                }
            case '\r':
            case '\n': {
                    if (fCanonical) {
                        fOut.print("&#");
                        fOut.print(Integer.toString(c));
                        fOut.print(';');
                        break;
                    }
                    // else, default print char
                }
            default: {
                    fOut.print(c);
                }
        }

    } // normalizeAndPrint(char)

}  // class Writer
