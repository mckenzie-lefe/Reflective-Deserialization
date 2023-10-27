# Reflective-Deserialization
 This program makes a socket connection with the sender program (repo Reflective-serialization) and converts the incoming byte stream into a JDOM document, deserialize the document into objects, and display the objects to screen.


This program deserializes an XML document, returning the reconstituted object
(and any objects it refers to). It uses the facilities provided by JDOM, in particular the Document and SAXBuilder classes.
In the Java class called Deserializer, invoked deserializes using the method:
    public Object deserialize(org.jdom.Document document)

The program can also display the deserialized objects to screen by ...