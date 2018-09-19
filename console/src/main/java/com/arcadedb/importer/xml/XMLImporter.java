/*
 * Copyright (c) 2018 - Arcade Analytics LTD (https://arcadeanalytics.com)
 */

package com.arcadedb.importer.xml;

import com.arcadedb.graph.MutableVertex;
import com.arcadedb.importer.*;
import com.arcadedb.utility.FileUtils;
import com.arcadedb.utility.LogManager;
import com.sun.xml.internal.stream.XMLInputFactoryImpl;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import java.util.LinkedHashMap;
import java.util.Map;

public class XMLImporter extends AbstractImporter {
  private Parser  parser;
  private int     objectNestLevel = 1;
  private boolean trimText        = true;
  private long    limitBytes;
  private long    limitEntries;

  public XMLImporter(final String[] args) {
    super(args);
  }

  public XMLImporter(final String url) {
    super(null);
    this.url = url;
  }

  public static void main(final String[] args) {
    new XMLImporter(args).load();
  }

  protected void load() {
    openDatabase();
    try {
      final SourceDiscovery analyzer = new SourceDiscovery(url);

      final SourceSchema sourceSchema = analyzer.getSchema();
      if (sourceSchema == null) {
        LogManager.instance().warn(this, "XML importing aborted because unable to determine the schema");
        return;
      }

      updateDatabaseSchema(sourceSchema.getSchema());

      source = analyzer.getSource();
      parser = new Parser(source, 0);

      parser.reset();

      final XMLInputFactoryImpl xmlFactory = new XMLInputFactoryImpl();
      final XMLStreamReader xmlReader = xmlFactory.createXMLStreamReader(source.inputStream);

      startImporting();

      int nestLevel = 0;

      String entityName = null;
      String lastName = null;
      String lastContent = null;

      final Map<String, Object> object = new LinkedHashMap<>();

      while (xmlReader.hasNext()) {
        final int eventType = xmlReader.next();

        switch (eventType) {
        case XMLStreamReader.COMMENT:
        case XMLStreamReader.SPACE:
          // IGNORE IT
          break;

        case XMLStreamReader.START_ELEMENT:
          if (nestLevel == objectNestLevel) {
            entityName = "v_" + xmlReader.getName().toString();

            // GET ELEMENT'S ATTRIBUTES AS PROPERTIES
            for (int i = 0; i < xmlReader.getAttributeCount(); ++i) {
              object.put(xmlReader.getAttributeName(i).toString(), xmlReader.getAttributeValue(i));
              lastName = null;
            }
          } else if (nestLevel == objectNestLevel + 1) {
            // GET ELEMENT'S SUB-NODES AS PROPERTIES
            if (lastName != null)
              object.put(lastName, lastContent);

            lastName = xmlReader.getName().toString();
          }

          ++nestLevel;
          break;

        case XMLStreamReader.END_ELEMENT:
          if (lastName != null)
            object.put(lastName, lastContent);

          LogManager.instance().debug(this, "</%s> (nestLevel=%d)", xmlReader.getName(), nestLevel);

          --nestLevel;

          if (nestLevel == objectNestLevel) {
            ++parsed;

            final MutableVertex record = database.newVertex(entityName);
            record.fromMap(object);
            database.asynch().createRecord(record);
            ++createdVertices;
          }
          break;

        case XMLStreamReader.ATTRIBUTE:
          ++nestLevel;
          LogManager.instance().debug(this, "- attribute %s attributes=%d (nestLevel=%d)", xmlReader.getName(), xmlReader.getAttributeCount(), nestLevel);
          break;

        case XMLStreamReader.CHARACTERS:
        case XMLStreamReader.CDATA:
          final String text = xmlReader.getText();
          if (!text.isEmpty() && !text.equals("\n")) {
            if (trimText)
              lastContent = text.trim();
            else
              lastContent = text;
          } else
            lastContent = null;
          break;

        default:
          // IGNORE IT
        }

        if (limitEntries > 0 && parsed > limitEntries)
          break;
      }
    } catch (Exception e) {
      LogManager.instance().error(this, "Error on parsing XML", e);
    } finally {
      database.asynch().waitCompletion();
      stopImporting();
      closeDatabase();
      closeInputFile();
    }
  }

  public SourceSchema analyze(final Parser parser, final int maxValueSampling) {
    long parsedObjects = 0;
    final AnalyzedSchema schema = new AnalyzedSchema(maxValueSampling);

    final String currentUnit = parser.isCompressed() ? "uncompressed " : "";
    final String totalUnit = parser.isCompressed() ? "compressed " : "";

    try {

      parser.reset();

      final XMLInputFactoryImpl xmlFactory = new XMLInputFactoryImpl();
      final XMLStreamReader xmlReader = xmlFactory.createXMLStreamReader(parser.getInputStream());

      int nestLevel = 0;

      boolean parsedStructure = false;

      String entityName = null;
      String lastName = null;
      String lastContent = null;

      while (xmlReader.hasNext()) {
        final int eventType = xmlReader.next();

        switch (eventType) {
        case XMLStreamReader.COMMENT:
        case XMLStreamReader.SPACE:
          // IGNORE IT
          break;

        case XMLStreamReader.START_ELEMENT:
          LogManager.instance().debug(this, "<%s> attributes=%d (nestLevel=%d)", xmlReader.getName(), xmlReader.getAttributeCount(), nestLevel);

          if (nestLevel == objectNestLevel) {
            entityName = xmlReader.getName().toString();

            // GET ELEMENT'S ATTRIBUTES AS PROPERTIES
            for (int i = 0; i < xmlReader.getAttributeCount(); ++i) {
              schema.setProperty(entityName, xmlReader.getAttributeName(i).toString(), xmlReader.getAttributeValue(i));
              lastName = null;
            }
          } else if (nestLevel == objectNestLevel + 1) {
            // GET ELEMENT'S SUB-NODES AS PROPERTIES
            if (lastName != null)
              schema.setProperty(entityName, lastName, lastContent);

            lastName = xmlReader.getName().toString();
          }

          ++nestLevel;
          break;

        case XMLStreamReader.END_ELEMENT:
          if (lastName != null)
            schema.setProperty(entityName, lastName, lastContent);

          LogManager.instance().debug(this, "</%s> (nestLevel=%d)", xmlReader.getName(), nestLevel);

          --nestLevel;

          if (nestLevel == objectNestLevel) {
            ++parsedObjects;

            if (!parsedStructure)
              parsedStructure = true;

            if (parsedObjects % 10000 == 0) {
              LogManager.instance()
                  .info(this, "- Parsed %d XML objects (%s%s/%s%s)", parsedObjects, currentUnit, FileUtils.getSizeAsString(parser.getPosition()), totalUnit,
                      FileUtils.getSizeAsString(parser.getTotal()));

              if (parsedObjects % 100000 == 0)
                dumpSchema(schema, parsedObjects);
            }
          }
          break;

        case XMLStreamReader.ATTRIBUTE:
          ++nestLevel;
          LogManager.instance().debug(this, "- attribute %s attributes=%d (nestLevel=%d)", xmlReader.getName(), xmlReader.getAttributeCount(), nestLevel);
          break;

        case XMLStreamReader.CHARACTERS:
        case XMLStreamReader.CDATA:
          final String text = xmlReader.getText();
          if (!text.isEmpty() && !text.equals("\n")) {
            if (trimText)
              lastContent = text.trim();
            else
              lastContent = text;
          } else
            lastContent = null;
          break;

        default:
          // IGNORE IT
        }

        if (limitEntries > 0 && parsedObjects > limitEntries)
          break;
      }

    } catch (XMLStreamException e) {
      // IGNORE IT

    } catch (Exception e) {
      LogManager.instance().error(this, "Error on parsing XML", e);
      return null;
    }

    // END OF PARSING. THIS DETERMINES THE TYPE
    schema.endParsing();

    dumpSchema(schema, parsedObjects);

    return new SourceSchema(parser.getSource(), SourceDiscovery.FILE_TYPE.XML, schema);
  }

  @Override
  protected long getInputFilePosition() {
    return parser.getPosition();
  }

  @Override
  protected void parseParameter(final String name, final String value) {
    if ("-recordType".equals(name))
      recordType = RECORD_TYPE.valueOf(value.toUpperCase());
    else if ("-analyzeTrimText".equals(name))
      trimText = Boolean.parseBoolean(value);
    else if ("-limitBytes".equals(name))
      limitBytes = FileUtils.getSizeAsNumber(value);
    else if ("-limitEntries".equals(name))
      limitEntries = Long.parseLong(value);
    else if ("-objectNestLevel".equals(name))
      objectNestLevel = Integer.parseInt(value);
    else
      super.parseParameter(name, value);
  }
}
