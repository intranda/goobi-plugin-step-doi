<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="2.0"
	xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
	<xsl:output indent="yes" />
	<xsl:template match="/">
		<resource xmlns="http://datacite.org/schema/kernel-4"
			xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
			xsi:schemaLocation="http://datacite.org/schema/kernel-4 http://schema.datacite.org/meta/kernel-4.2/metadata.xsd">

			<!-- DOI -->
			<identifier identifierType="DOI">
				<xsl:value-of select="//GOOBI-DOI" />
			</identifier>

			<!-- IF TOP ELEMENT -->
			<xsl:if test="not(//SUBELEMENT)">

				<!-- TITLE -->
				<titles>
					<title>
						<xsl:value-of select="//TITLE" />
					</title>
				</titles>

				<!-- PUBLISHER AND PUBLICATION YEAR -->
				<publisher>
					<xsl:value-of select="//PUBLISHER" />
				</publisher>
				<publicationYear>
					<xsl:value-of select="//PUBLICATIONYEAR" />
				</publicationYear>

				<!-- SUBJECTS IF AVAILABLE -->
				<xsl:if test="//SUBJECT">
					<subjects>
						<xsl:for-each select="//SUBJECT">
							<subject xml:lang="de-DE">
								<xsl:value-of select="." />
							</subject>
						</xsl:for-each>
					</subjects>
				</xsl:if>
				<!-- // SUBJECTS IF AVAILABLE -->

				<!-- PUBLICATION TYPE -->
				<resourceType resourceTypeGeneral="Text">
					<xsl:value-of select="//GOOBI-DOCTYPE" />
				</resourceType>

				<!-- LANGUAGE -->
				<language>
					<xsl:value-of select="//LANGUAGE" />
				</language>

				<!-- CREATORS: AUTHORS AND EDITORS -->
				<creators>
					<xsl:for-each select="//CREATOR">
						<creator>
							<creatorName>
								<xsl:value-of select="." />
							</creatorName>
							<givenName>
								<xsl:value-of select="substring-after(., ', ')" />
							</givenName>
							<familyName>
								<xsl:value-of select="substring-before(., ', ')" />
							</familyName>
						</creator>
					</xsl:for-each>
				</creators>
				<!-- // CREATORS: AUTHORS AND EDITORS -->

				<!-- ORIGINAL PUBLICATION YEAR -->
				<dates>
					<date dateType="Created">
						<xsl:value-of select="//PUBLICATIONYEAR" />
					</date>
				</dates>

				<!-- FORMAT IF AVAILABLE -->
				<xsl:if test="//FORMAT">
					<sizes>
						<size>
							<xsl:value-of select="//FORMAT" />
						</size>
					</sizes>
				</xsl:if>
				<!-- // FORMAT IF AVAILABLE -->

				<!-- GOOBI IDENTIFIER -->
				<alternateIdentifiers>
					<alternateIdentifier
						alternateIdentifierType="Goobi identifier">
						<xsl:value-of select="//IDENTIFIER" />
					</alternateIdentifier>
				</alternateIdentifiers>
				<!-- // GOOBI IDENTIFIER -->

				<!-- HOSTING INSTITUTION -->
				<contributors>
					<contributor contributorType="HostingInstitution">
						<contributorName>Universitätsbibliothek Stuttgart</contributorName>
					</contributor>
				</contributors>
				<!-- // HOSTING INSTITUTION -->

				<!-- ANCHOR INFORMATION IF AVAILABLE -->
				<xsl:if test="//NUMBER != ''">
					<relatedItems>
						<relatedItem relatedItemType="Collection"
							relationType="IsPartOf">
							<titles>
								<title>
									<xsl:value-of select="//ANCHORTITLE" />
								</title>
								<xsl:if test="//ANCHORSUBTITLE">
									<title titleType="Subtitle">
										<xsl:value-of select="//ANCHORSUBTITLE" />
									</title>
								</xsl:if>
							</titles>
							<xsl:if test="//SERIES">
								<volume>
									<xsl:value-of select="//SERIES" />
								</volume>
							</xsl:if>
							<number>
								<xsl:value-of select="//NUMBER" />
							</number>
						</relatedItem>
					</relatedItems>
				</xsl:if>
				<!-- // ANCHOR INFORMATION IF AVAILABLE -->


			</xsl:if>
			<!-- IF TOP ELEMENT -->


			<!-- IF SUB ELEMENT -->
			<xsl:if test="//SUBELEMENT">

				<!-- TITLE OF SUB ELEMENT -->
				<titles>
					<title>
						<xsl:value-of select="//METADATA-TitleDocMain" />
					</title>
				</titles>

				<!-- PUBLISHER AND PUBLICATION YEAR -->
				<publisher>
					<xsl:value-of select="//PUBLISHER" />
				</publisher>
				<publicationYear>
					<xsl:value-of select="//PUBLICATIONYEAR" />
				</publicationYear>

				<!-- RESOURCE TYPE FOR SUB ELEMENT -->
				<resourceType resourceTypeGeneral="Text">document</resourceType>

				<!-- CREATORS FOR SUB ELEMENT -->
				<creators>
					<xsl:if test="//PERSON-Author">
						<xsl:for-each select="//PERSON-Author">
							<creator>
								<creatorName>
									<xsl:value-of select="." />
								</creatorName>
								<givenName>
									<xsl:value-of select="substring-after(., ', ')" />
								</givenName>
								<familyName>
									<xsl:value-of select="substring-before(., ', ')" />
								</familyName>
							</creator>
						</xsl:for-each>
					</xsl:if>
					<xsl:if test="not(//PERSON-Author)">
						<creator>
							<creatorName>- CREATOR UNKNOWN -</creatorName>
						</creator>
					</xsl:if>
				</creators>
				<!-- // CREATORS FOR SUB ELEMENT -->

				<!-- ORIGINAL PUBLICATION YEAR -->
				<dates>
					<date dateType="Created">
						<xsl:value-of select="//PUBLICATIONYEAR" />
					</date>
				</dates>

				<!-- GOOBI IDENTIFIER -->
				<alternateIdentifiers>
					<alternateIdentifier
						alternateIdentifierType="Goobi identifier">
						<xsl:value-of select="//IDENTIFIER" />
					</alternateIdentifier>
				</alternateIdentifiers>
				<!-- // GOOBI IDENTIFIER -->

				<!-- HOSTING INSTITUTION -->
				<contributors>
					<contributor contributorType="HostingInstitution">
						<contributorName>Universitätsbibliothek Stuttgart</contributorName>
					</contributor>
				</contributors>
				<!-- // HOSTING INSTITUTION -->

				<!-- ANCHOR INFORMATION IF AVAILABLE -->
				<xsl:if test="//NUMBER != ''">
					<descriptions>
						<description descriptionType="SeriesInformation">
							<xsl:value-of select="//ANCHORTITLE" />
						</description>
						<description descriptionType="SeriesInformation">
							<xsl:value-of select="//NUMBER" />
						</description>
					</descriptions>
				</xsl:if>
				<!-- // ANCHOR INFORMATION IF AVAILABLE -->

				<!-- VOLUME INFORMATION WHERE THE SUB ELEMENT IS PART OF -->
				<relatedItems>
					<relatedItem relationType="IsPublishedIn"
						relatedItemType="Journal">
						<titles>
							<title>
								<xsl:value-of select="//TITLE" />
							</title>
						</titles>
						<publicationYear>
							<xsl:value-of select="//PUBLICATIONYEAR" />
						</publicationYear>
						<volume>
							<xsl:value-of select="//NUMBER" />
						</volume>
						<firstPage>
							<xsl:value-of select="//SUBELEMENT-PAGE-START" />
						</firstPage>
						<lastPage>
							<xsl:value-of select="//SUBELEMENT-PAGE-END" />
						</lastPage>
					</relatedItem>
				</relatedItems>
				<!-- // VOLUME INFORMATION WHERE THE SUB ELEMENT IS PART OF -->

			</xsl:if>
			<!-- IF SUB ELEMENT -->


		</resource>
	</xsl:template>
</xsl:stylesheet>


<!-- ========================== Available internal elements ========================== 
	
	- Publication type of anchor document (e.g. Periodical) <xsl:value-of select="//GOOBI-ANCHOR-DOCTYPE"/> 
	- Publication type of document (e.g. Monograph or Volume) <xsl:value-of select="//GOOBI-DOCTYPE"/> 
	- Generated DOI <xsl:value-of select="//GOOBI-DOI"/> 
	- Specific metadata (e.g. TitleDocMain) <xsl:value-of select="//METADATA-TitleDocMain"/> 
	- Specific person (e.g. Author) <xsl:value-of select="//PERSON-Author"/> 
	- Type of Structure element (e.g. Article) if it is one <xsl:value-of select="//SUBELEMENT"/> 
	- Start page of a structure element (e.g. [1]) <xsl:value-of select="//SUBELEMENT-PAGE-START"/> 
	- End page of a structure element (e.g. 23) <xsl:value-of select="//SUBELEMENT-PAGE-END"/> 

========================== // Available internal elements ========================== -->