---
title: Plugin zur DOI-Registrierung
identifier: intranda_step_doi
published: true
description: Dies ist ein Goobi Step-Plugin, um die Registrierung von digitalen Objekten beim DataCite DOI-Dienst zu ermöglichen.
---
## Einführung
Die vorliegende Dokumentation beschreibt die Installation, Konfiguration und den Einsatz eines Plugins zum Registrieren von DOIs über die DataCite API.

**ACHTUNG:** Zu beachten ist, dass dieses Plugin eine Neuimplementierung des [datacite-doi-Plugins](https://docs.goobi.io/goobi-workflow-plugins-de/step/intranda_step_datacite_doi) ist, die mittels XSLT arbeitet. Diese Implementierung ist bisher darauf beschränkt, dass DOIs für eigenständige Werke (z.B. Monographien und Zeitschriftenbände) registriert werden können. Eine Registrierung von DOIs für Strukturelemente (z.B. für Zeitschriftenartikel) ist mit diesem Plugin bisher nicht möglich.


## Installation
Das Plugin besteht aus den folgenden Dateien:

```bash
plugin_intranda_step_doi-base.jar
plugin_intranda_step_doi.xml
doi.xsl
```

Die Datei `plugin_intranda_step_doi-base.jar` enthält die Programmlogik. Sie muss unter folgendem Pfad installiert werden:

```bash
/opt/digiverso/goobi/plugins/step/plugin_intranda_step_doi-base.jar
```

Bei der Datei `doi.xsl` handelt es sich um die Transformationsdatei, die das Grundgerüst der DataCite-Metadaten darstellt, in die das Plugin die individuellen Metdaten des jeweiligen Vorgangs einfügt, um anschließend die DOIs damit zu registrieren. Sie muss unter diesem Pfad installiert werden:

```bash
/opt/digiverso/goobi/xslt/doi.xsl
```

Die Datei `plugin_intranda_step_doi.xml` ist die Hauptkonfigurationsdatei für das Plugin. Sie muss unter diesem Pfad installiert werden:

```bash
/opt/digiverso/goobi/config/plugin_intranda_step_doi.xml
```


## Überblick und Funktionsweise
Um das Plugin in Betrieb zu nehmen, muss es für eine oder mehrere gewünschte Aufgaben im Workflow aktiviert werden. Dies geschieht wie im folgenden Screenshot gezeigt durch Auswahl des Plugins `intranda_step_doi` aus der Liste der installierten Plugins.

![Zuweisen des Plugins zu einer bestimmten Aufgabe](screen1_de.png)

Da dieses Plugin in der Regel automatisch ausgeführt werden soll, sollte der Workflow-Schritt im Workflow als automatisch konfiguriert werden. Da das Plugin den DOI in die Metadatendatei des Vorgangs schreibt, sollte das Kontrollkästchen für `Update metadata index when finishing` ebenfalls aktiviert sein.

Dieses Plugin liest zunächst seine Konfigurationsdatei ein und versucht die Field-Variablen mit denjenigen Inhalten der METS-Datei zu füllen, die in der Konfiguration definiert wurden. Die Field-Variablen werden dabei von oben nach unten durchlaufen. Sobald ein Wert in einem definierten Feld ermittelt wurde, wird dieser der Variable zugewiesen. Wurde kein Wert in keine der Felder ermittelt, wird stattdessen der default-Wert verwendet. Ist kein default-Wert definiert für eine Field-Variable, bleibt diese leer.

Nach der Erzeugung der Field-Variablen werden diese als eine xml-Datei der Transformationsdatei übergeben. Diese nutzt die definierten Field-Variablen, um die Inhalte aus der METS-Datei einzufügen. Die somit erzeugte DataCite-xml-Datei wird anschließend für die Registrierung bzw. das Update der DOIs bei DataCite genutzt, wobei die Zugangsdaten und URL-Informationen aus der Konfigurationsdatei zum Einsatz kommen.


## Konfiguration des Plugins


### Hauptkonfiguration
Die Konfiguration erfolgt über die Konfigurationsdatei `plugin_intranda_step_doi.xml` und kann im laufenden Betrieb angepasst werden. Sie ist wie folgt aufgebaut:

```xml
<config_plugin>

    <config>
    		<!-- which projects to use for (can be more then one, otherwise use *) -->
    		<project>*</project>
    		<step>*</step>

    		<!-- use debug mode if the temporary xml shall be saved in the Goobi tmp folder -->
    		<debugMode>true</debugMode>

            <!-- use draft if the doi should only be registered in draft state -->
		    <draft>true</draft>

    		<!-- authentication and main information -->
    		<!-- For testing: https://mds.test.datacite.org/ -->
    		<!-- For production https://mds.datacite.org/ -->
    		<serviceAddress>https://mds.test.datacite.org/</serviceAddress>

    		<!-- authentication and main information -->
    		<base>10.12345678</base>
    		<viewer>https://viewer.example.org/resolver?field=MD_PI_DOI&amp;identifier=</viewer>
    		<username>USER</username>
    		<password>PASSWORD</password>

    		<!-- name parts for DOI composition -->
    		<prefix>go</prefix>
    		<name>goobi</name>
    		<separator>-</separator>

    		<!-- metadata field from ruleset where to store the DOI -->
    		<metadata>DOI</metadata>

    		<!-- Path to the xsl file that shall be used for the datacite xml generation
    		(file must be located inside of the central Goobi xslt folder) -->
    		<xslt>doi.xsl</xslt>

    		<field name="LANGUAGE" default="- UNKNOWN LANGUAGE -">
    			<data content="{meta.DocLanguage}"/>
    		</field>

    		<field name="TITLE" default="- UNKNOWN TITLE -">
    			<data content="{meta.TitleDocMain}"/>
    		</field>

    		<field name="ANCHORTITLE" default="- UNKNOWN ANCHOR TITLE -">
    			<data content="{meta.topstruct.TitleDocMain}"/>
    		</field>

    		<field name="ANCHORSUBTITLE" default="- UNKNOWN ANCHOR SUB TITLE -">
    			<data content="{meta.topstruct.TitleDocSub1}"/>
    		</field>

    		<field name="IDENTIFIER" default="- NO IDENTIFIER DEFINED -">
    			<data content="{meta.CatalogIDDigital}"/>
    		</field>

    		<field name="FORMAT" default="- NO FORMAT DEFINED -">
    			<data content="{meta.FormatSourcePrint}"/>
    		</field>

    		<field name="PUBLICATIONYEAR" default="- NO FORMAT DEFINED -">
    			<data content="{meta.PublicationYear}"/>
    		</field>

    		<field name="CREATOR" default="- NO CREATOR DEFINED -" repeatable="true">
    			<data content="{metas.Author}"/>
    		</field>

    		<field name="PUBLISHER" default="- NO PUBLISHER DEFINED -">
    			<data content="{meta.PublisherName}"/>
    		</field>

    		<field name="SERIES" default="- NO SERIES DEFINED -">
    			<data content="{meta.PublicationSeries}"/>
    		</field>

    		<field name="NUMBER">
    			<data content="{meta.CurrentNo}"/>
    			<data content="{meta.CurrentNoSorting}"/>
    		</field>

            <field name="SUBJECT" default="- UNKNOWN SUBJECT -" repeatable="true">
        	    <data content="{metas.SubjectTopic}"/>
            </field>
    </config>
</config_plugin>
```

Der Block `<config>` kann für verschiedene Projekte oder Workflow-Schritte mehrfach vorkommen, um unterschiedliche Aktionen innerhalb verschiedener Workflows durchführen zu können. Die weiteren Parameter innerhalb dieser Konfigurationsdatei haben die folgende Bedeutung:

| Wert | Beschreibung |
| :--- | :--- |
| `project` | Dieser Parameter legt fest, für welches Projekt der aktuelle Block `<config>` gelten soll. Verwendet wird hierbei der Name des Projektes. Dieser Parameter kann mehrfach pro `<config>` Block vorkommen. |
| `step` | Dieser Parameter steuert, für welche Arbeitsschritte der Block `<config>` gelten soll. Verwendet wird hier der Name des Arbeitsschritts. Dieser Parameter kann mehrfach pro `<config>` Block vorkommen. |
| `serviceAddress` | Dieser Parameter definiert die URL für den DataCite-Dienst. Im obigen Beispiel ist es der Testserver. |
| `debugMode` | Mit diesem Parameter kann der Debug-Modus aktiviert werden. Dieser erlaubt, dass die XML-Datei mit den definierten Field-Variablen (`doi_in.xml`) sowie die transformierte DataCite-XML-Datei (`doi_out.xml`) innerhalb des Verzeichnisses `tmp` von Goobi workflow gespeichert werden. Dies erlaubt einen Einblick in die tatsächlich verwendeten oder angepassten Metadaten, die zur DOI-Registrierung verwendet werden. |
| `draft` | Mit diesem Parameter kann festgelegt werden, dass die DOIs bisher nur als Draft reserviert werden aber noch nicht offiziell registriert werden. Sie werden somit von DataCite noch nicht öffentlich erreichbar aufgelöst und auch noch nicht in Rechnung gestellt. |
| `base` | Dieser Parameter definiert die DOI-Basis für die Einrichtung, die bei DataCite registriert wurde. |
| `viewer` | Der Parameter `viewer` definiert den Präfix, den jeder DOI-Link erhält. Ein DOI "10.80831/goobi-1" erhält hier z. B. den Hyperlink "[https://viewer.goobi.io/idresolver?doi=10.80831/goobi-1](https://viewer.goobi.io/idresolver?doi=10.80831/goobi-1)" |
| `username` | Dies ist der Benutzername, der für die DataCite-Registrierung verwendet wird. |
| `password` | Dies ist das Passwort, das für die DataCite-Registrierung verwendet wird. |
| `prefix` | Dies ist das Präfix, das dem DOI vor dem Namen und der ID des Dokuments gegeben werden soll. |
| `name` | Dies ist der Name, der dem DOI vor der ID des Dokuments gegeben werden soll. |
| `separator` | Definieren Sie hier ein Trennzeichen, das zwischen den verschiedenen Teilen des DOI verwendet werden soll. |
| `metadata` | Dieser Parameter gibt an, unter welchem Metadatennamen die DOI in der METS-MODS-Datei gespeichert werden soll. Standard ist `DOI`. |
| `xslt` | Mit diesem Parameter wird die Transformationsdatei festgelegt, die für die DOI-Registrierung verwendet werden soll. |
| `field` - `name` | Mit dem Parameter `name` kann eine Field-Variable benannt werden, die für das Mapping zur Verfügung stehen soll. |
| `field` - `default` | Mit diesem Parameter kann ein Wert festgelegt werden, den die Field-Variable erhalten soll, wenn keines der aufgeführten Metadaten aus den Elementen `data` gefunden werden kann. |
| `field` - `repeatable` | Hiermit kann gesteuert werden, dass mehrfach vorkommende Werte (abgefragt z.B. durch Verwendung von `{metas.SubjectTopic}` anstelle von `{meta.SubjectTopic}`) anhand eines Semikolons separiert und als Einzelwerte verwendet werden. |
| `field` - `data` - `content` | Innerhalb dieses Elements können Metadaten oder auch statische Texte festgelegt werden, die als Wert der Field-Variable zugewiesen werden sollen. Hierbei ist die Reihenfolge der aufgeführten `data`-Elemente entscheidend. Sobald ein Feld mit dem Inhalt gefunden werden konnte, werden die nachfolgenden `data`-Elemente übersprungen. Es handelt sich hier also um eine absteigende Priorität der aufgeführten Elemente. |


### Konfiguration innerhalb der Transformationsdatei
Die Transformationsdatei `doi.xsl` sieht in etwa so aus:

```xml
<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="2.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
<xsl:output indent="yes"/>
<xsl:template match="/">
<resource xmlns="http://datacite.org/schema/kernel-4" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xsi:schemaLocation="http://datacite.org/schema/kernel-4 http://schema.datacite.org/meta/kernel-4.2/metadata.xsd">

  <identifier identifierType="DOI"><xsl:value-of select="//GOOBI-DOI"/></identifier>
  <titles>
      <title><xsl:value-of select="//TITLE"/></title>
  </titles>
  <publisher><xsl:value-of select="//PUBLISHER"/></publisher>
  <publicationYear><xsl:value-of select="//PUBLICATIONYEAR"/></publicationYear>
  <subjects>
      <xsl:for-each select="//SUBJECT">
          <subject xml:lang="de-DE"><xsl:value-of select="."/></subject>
      </xsl:for-each>
  </subjects>
  <resourceType resourceTypeGeneral="Text"><xsl:value-of select="//GOOBI-DOCTYPE"/></resourceType>
  <language><xsl:value-of select="//LANGUAGE"/></language>
  <creators>
      <xsl:for-each select="//CREATOR">
          <creator>
            <creatorName><xsl:value-of select="."/></creatorName>
            <givenName><xsl:value-of select="substring-before(., ', ')"/></givenName>
            <familyName><xsl:value-of select="substring-after(., ', ')"/></familyName>
          </creator>
      </xsl:for-each>
  </creators>
  <sizes>
      <size><xsl:value-of select="//FORMAT"/></size>
  </sizes>
  <alternateIdentifiers>
      <alternateIdentifier alternateIdentifierType="Goobi identifier"><xsl:value-of select="//IDENTIFIER"/></alternateIdentifier>
  </alternateIdentifiers>
  <contributors>
      <contributor contributorType="HostingInstitution">
          <contributorName>intranda GmbH</contributorName>
      </contributor>
  </contributors>

<!--
  <xsl:if test="//NUMBER != ''">
	  <relatedItem relatedItemType="Collection" relationType="IsPartOf">
      <title><xsl:value-of select="//ANCHORTITLE"/></title>
      <title titleType="Subtitle"><xsl:value-of select="//ANCHORSUBTITLE"/></title>
      <volume><xsl:value-of select="//SERIES"/></volume>
      <number><xsl:value-of select="//NUMBER"/></number>
    </relatedItem>
  </xsl:if>
-->

</resource>
</xsl:template>
</xsl:stylesheet>


<!--
========================== Available internal elements ==========================

- Publication type of anchor document (e.g. Periodical)
<xsl:value-of select="//GOOBI-ANCHOR-DOCTYPE"/>

- Publication type of document (e.g. Monograph or Volume)
<xsl:value-of select="//GOOBI-DOCTYPE"/>

- Generated DOI
<xsl:value-of select="//GOOBI-DOI"/>

========================== // Available internal elements ==========================
-->
```

Innerhalb dieser Transformationsdatei ist das DataCite-XML als Grundgerüst aufgeführt. Inhalte der einzelnen xml-Elemente werden dabei automatisch aus den Field-Variablen eingefügt, die in der Hauptkonfigurationsdatei definiert wurden. Neben diese verwendbaren definierbaren Field-Variablen existieren auch einige zusätzliche Variablen, die verwendet werden können:

| Field-Variable | Beschreibung |
| :--- | :--- |
| `//GOOBI-ANCHOR-DOCTYPE` | Diese Variable enthält den internen Namen des Publikationstyps des übergeordneten Anchor-Elements (z.B. Periodical). |
| `//GOOBI-DOCTYPE` | Diese Variable enthält den internen Namen des Publikationstyps  des Werks (z.B. Monograph). |
| `//GOOBI-DOI` | Diese Variable enthält die zu verwendende DOI. |


## Nützliche Zusatzinformationen
- Dokumentation von DataCite: https://support.datacite.org/docs/getting-started
- Metadatenschema-Übersicht: https://schema.datacite.org/
- Metadatenschema für die Version 4.4 mit Beispieldateien: https://schema.datacite.org/meta/kernel-4.4/
- Admin-Bereich für DataCite-Kunden: https://doi.datacite.org/
- Admin-Bereich im Testsystem für DataCite-Kunden: https://doi.test.datacite.org/


## Beispiel
- Beispiel für eine DataCite-XML-Datei aus Goobi:

```xml
<?xml version="1.0" encoding="UTF-8"?>
<resource
    xmlns="http://datacite.org/schema/kernel-4"
    xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
    xsi:schemaLocation="http://datacite.org/schema/kernel-4 http://schema.datacite.org/meta/kernel-4.2/metadata.xsd">
    <identifier identifierType="DOI">10.48644/1776214552</identifier>
    <titles>
        <title>Della forza de'corpi che chiamano viva libri tre</title>
    </titles>
    <publisher>Pisarri</publisher>
    <publicationYear>2022</publicationYear>
    <resourceType resourceTypeGeneral="Text">document</resourceType>
    <creators>
        <creator>
            <creatorName>Zanotti, Francesco Maria</creatorName>
            <givenName>Francesco Maria</givenName>
            <familyName>Zanotti</familyName>
        </creator>
    </creators>
    <dates>
        <date dateType="Created">1752</date>
    </dates>
    <alternateIdentifiers>
        <alternateIdentifier alternateIdentifierType="Goobi identifier">1776214552</alternateIdentifier>
    </alternateIdentifiers>
    <contributors>
        <contributor contributorType="HostingInstitution">
            <contributorName>Max-Planck-Institut für Wissenschaftsgeschichte</contributorName>
        </contributor>
    </contributors>
</resource>
```
