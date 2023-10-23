package de.intranda.goobi.plugins;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;

/**
 * This file is part of a plugin for Goobi - a Workflow tool for the support of mass digitization.
 *
 * Visit the websites for more information.
 *          - https://goobi.io
 *          - https://www.intranda.com
 *          - https://github.com/intranda/goobi
 *
 * This program is free software; you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free
 * Software Foundation; either version 2 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program; if not, write to the Free Software Foundation, Inc., 59
 * Temple Place, Suite 330, Boston, MA 02111-1307 USA
 *
 */

import java.util.HashMap;
import java.util.List;

import org.apache.commons.configuration.HierarchicalConfiguration;
import org.apache.commons.configuration.SubnodeConfiguration;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.ParseException;
import org.goobi.beans.Process;
import org.goobi.beans.Step;
import org.goobi.production.enums.LogType;
import org.goobi.production.enums.PluginGuiType;
import org.goobi.production.enums.PluginReturnValue;
import org.goobi.production.enums.PluginType;
import org.goobi.production.enums.StepReturnValue;
import org.goobi.production.plugin.interfaces.IStepPluginVersion2;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.output.Format;
import org.jdom2.output.XMLOutputter;
import org.jdom2.transform.XSLTransformException;
import org.jdom2.transform.XSLTransformer;

import de.sub.goobi.config.ConfigPlugins;
import de.sub.goobi.config.ConfigurationHelper;
import de.sub.goobi.helper.Helper;
import de.sub.goobi.helper.VariableReplacer;
import de.sub.goobi.helper.exceptions.DAOException;
import de.sub.goobi.helper.exceptions.SwapException;
import lombok.Getter;
import lombok.extern.log4j.Log4j2;
import net.xeoh.plugins.base.annotations.PluginImplementation;
import ugh.dl.DocStruct;
import ugh.dl.Fileformat;
import ugh.dl.Metadata;
import ugh.dl.MetadataType;
import ugh.exceptions.PreferencesException;
import ugh.exceptions.ReadException;
import ugh.exceptions.UGHException;
import ugh.exceptions.WriteException;

@PluginImplementation
@Log4j2
public class DoiStepPlugin implements IStepPluginVersion2 {

    private static final long serialVersionUID = -9093540848644429154L;
    @Getter
    private String title = "intranda_step_doi";
    @Getter
    private Step step;
    @Getter
    private String value;
    @Getter
    private boolean allowTaskFinishButtons;
    private String returnPath;
    private SubnodeConfiguration config;
    private Process p;
    private transient Fileformat ff;
    private transient VariableReplacer replacer;

    @Override
    public void initialize(Step step, String returnPath) {
        this.returnPath = returnPath;
        this.step = step;
        config = ConfigPlugins.getProjectAndStepConfig(title, step);
        log.info("Doi step plugin initialized");
    }

    @Override
    public PluginReturnValue run() {
        boolean successful = false;

        try {

            // Open the metadata file for the process and prepare the VariableReplacer
            p = step.getProzess();
            ff = p.readMetadataFile();
            replacer = new VariableReplacer(ff.getDigitalDocument(), p.getRegelsatz().getPreferences(), p, null);

            // load topstruct
            DocStruct topstruct = ff.getDigitalDocument().getLogicalDocStruct();
            if (topstruct.getType().isAnchor()) {
                topstruct = topstruct.getAllChildren().get(0);
            }

            // read catalogue identifier
            MetadataType idType = p.getRegelsatz().getPreferences().getMetadataTypeByName("CatalogIDDigital");
            String myId = getExistingMetadata(topstruct, idType);

            // try to read existing DOI
            String doiTypeName = config.getString("metadata", "DOI");
            MetadataType doiType = p.getRegelsatz().getPreferences().getMetadataTypeByName(doiTypeName);
            String myDoi = getExistingMetadata(topstruct, doiType);
            boolean hadDoi = StringUtils.isNotBlank(myDoi);

            // add the new or existing DOI as contentfield
            if (!hadDoi) {
                // prepare a new DOI name if not existing
                String name = config.getString("name");
                String prefix = config.getString("prefix");
                String separator = config.getString("separator", "-");
                String postfix = "";
                if (StringUtils.isNotBlank(prefix)) {
                    postfix = prefix + separator;
                }
                if (StringUtils.isNotBlank(name)) {
                    postfix += name + separator;
                }
                myDoi = config.getString("base") + "/" + postfix + myId;
            }

            // create the list of all content fields with metadata replaced in it
            List<ContentField> contentFields = createContentFieldList();
            contentFields.add(new ContentField("GOOBI-DOI", myDoi));

            // create an xml document to allow xslt transformation afterwards
            Document doc = createXmlDocumentOfContent(contentFields);

            // if debug mode is switched on write that xml file into Goobi temp folder
            if (config.getBoolean("debugMode", false)) {
                writeDocumentToFile(doc, "doi_in.xml");
            }

            // do the xslt transformation
            Document datacitedoc = doXmlTransformation(doc);

            // if debug mode is switched on write that xml file into Goobi temp folder
            if (config.getBoolean("debugMode", false)) {
                writeDocumentToFile(datacitedoc, "doi_out.xml");
            }

            // create or update DOI
            if (!hadDoi) {
                // register a complete new DOI
                successful = createDoi(topstruct, myDoi, doiType, datacitedoc);
            } else {
                // update the existing DOI
                updateDoi(myDoi, datacitedoc);
            }

            // check if doi is accessible
            log.debug("DOI is accessible: " + HelperHttp.checkUrlBasicAuth("doi/" + myDoi, config));

        } catch (UGHException | IOException | SwapException | JDOMException e) {
            log.error("Error while executing the DOI plugin", e);
            Helper.addMessageToProcessJournal(getStep().getProcessId(), LogType.ERROR,
                    "An error happend during the registration of DOIs: " + e.getMessage());
        }

        log.info("Doi step plugin executed");
        if (!successful) {
            return PluginReturnValue.ERROR;
        }
        return PluginReturnValue.FINISH;
    }

    /**
     * create doi and save it in the docstruct.
     * 
     * @param prefs
     * @param iIndex
     * @param anchor
     * @param anchor
     * 
     * @return Returns the doi.
     * @throws JDOMException
     * @throws UGHException
     * @throws DAOException
     * @throws SwapException
     * @throws InterruptedException
     */
    private boolean createDoi(DocStruct docstruct, String doi, MetadataType doiType, Document doc) throws IOException, UGHException, SwapException {

        // draft for DOI
        String result = HelperHttp.postXmlBasicAuth(doc, "metadata/" + doi, config);

        // if drafting was successful write metadata or make it findable
        if (StringUtils.isBlank(result)) {

            // if draft is configured finish here
            if (config.getBoolean("draft", false)) {
                // Write DOI metadata into the docstruct.
                Metadata md = new Metadata(doiType);
                md.setValue(doi);
                docstruct.addMetadata(md);
                p.writeMetadataFile(ff);
                Helper.addMessageToProcessJournal(p.getId(), LogType.INFO, "A new DOI was drafted: " + doi);

                // if drafting was successful then make it findable
            } else {
                String viewer = config.getString("viewer");
                String text = "doi=" + doi + "\n" + "url=" + viewer + doi;
                result = HelperHttp.putTxtBasicAuth(text, "doi/" + doi, config);

                // if findable then update METS file
                if (StringUtils.isBlank(result)) {
                    // Write DOI metadata into the docstruct.
                    Metadata md = new Metadata(doiType);
                    md.setValue(doi);
                    docstruct.addMetadata(md);
                    p.writeMetadataFile(ff);
                    Helper.addMessageToProcessJournal(p.getId(), LogType.INFO, "A new DOI was registered: " + doi);
                }
            }
        }

        // if no draft or not findable report error
        if (StringUtils.isNotBlank(result)) {
            Helper.addMessageToProcessJournal(p.getId(), LogType.ERROR, "A new DOI could not get registered: " + result);
            return false;
        }
        return true;
    }

    /**
     * Update an existing DOI
     *
     * @param anchor
     * @throws IOException
     * @throws ParseException
     */
    private boolean updateDoi(String doi, Document doc) throws ParseException, IOException {

        String result = HelperHttp.putXmlBasicAuth(doc, "metadata/" + doi, config);

        // if draft is not configured and doi is not findable, make it findable
        if (StringUtils.isBlank(result) && !config.getBoolean("draft", false) && !HelperHttp.checkUrlBasicAuth("doi/" + doi, config)) {
            String viewer = config.getString("viewer");
            String text = "doi=" + doi + "\n" + "url=" + viewer + doi;
            result = HelperHttp.putTxtBasicAuth(text, "doi/" + doi, config);
        }

        // if no draft or not findable report error
        if (StringUtils.isNotBlank(result)) {
            Helper.addMessageToProcessJournal(p.getId(), LogType.ERROR, "The existing DOI could not get updated: " + result);
            return false;
        } else {
            Helper.addMessageToProcessJournal(p.getId(), LogType.INFO, "The existing DOI was updated: " + doi);
            return true;
        }
    }

    /**
     * If the element already has a DOI, return it, otherwise return null.
     * 
     * @param docstruct
     * @return
     */
    private String getExistingMetadata(DocStruct docstruct, MetadataType type) {
        List<? extends Metadata> list = docstruct.getAllMetadataByType(type);
        if (!list.isEmpty()) {
            return list.get(0).getValue();
        }
        return null;
    }

    /**
     * create a list lf ContentField that contains each configured field with its preferred value in it the values are filled using the variable
     * replacer
     * 
     * @return
     * @throws ReadException
     * @throws IOException
     * @throws InterruptedException
     * @throws PreferencesException
     * @throws SwapException
     * @throws DAOException
     * @throws WriteException
     */
    private List<ContentField> createContentFieldList() throws PreferencesException {
        // Create a list of variables
        List<ContentField> contentFields = new ArrayList<>();
        String separator = "; ";
        replacer.setSEPARATOR(separator);

        // run through all defined fields to fill their content
        List<HierarchicalConfiguration> fields = config.configurationsAt("field");
        for (HierarchicalConfiguration field : fields) {

            // run through all data elements to get a field value
            String val = null;
            List<HierarchicalConfiguration> datas = field.configurationsAt("data");
            for (HierarchicalConfiguration d : datas) {
                String content = d.getString("@content");
                String result = replacer.replace(content);
                // if the content ist not empty and it is different from the variable use it
                if (StringUtils.isNotBlank(result) && !result.equals(content)) {
                    val = result;
                    break;
                }
            }
            // if no content was set yet then set the default if available
            if (StringUtils.isBlank(val)) {
                String thedefault = field.getString("@default");
                if (!StringUtils.isBlank(thedefault)) {
                    val = thedefault;
                }
            }
            if (val != null) {
                // if the field is repeatable create multiple ContentFields for each entry (separated by semicolon)
                boolean repeatable = field.getBoolean("@repeatable", false);
                if (!repeatable || !val.contains(separator)) {
                    ContentField cf = new ContentField();
                    cf.setName(field.getString("@name"));
                    cf.setValue(val);
                    contentFields.add(cf);
                } else {
                    String[] vlist = val.split(separator);
                    for (String v : vlist) {
                        ContentField cf = new ContentField();
                        cf.setName(field.getString("@name"));
                        cf.setValue(v);
                        contentFields.add(cf);
                    }
                }
            }
        }

        // find out publication type
        DocStruct top = ff.getDigitalDocument().getLogicalDocStruct();
        String topType = top.getType().getName();
        if (top.getType().isAnchor() && top.getAllChildren() != null && !top.getAllChildren().isEmpty()) {
            contentFields.add(new ContentField("GOOBI-ANCHOR-DOCTYPE", topType));
            topType = top.getAllChildren().get(0).getType().getName();
        }
        contentFields.add(new ContentField("GOOBI-DOCTYPE", topType));
        return contentFields;
    }

    /**
     * create an XML Document of all contentfields
     * 
     * @param contentFields
     * @return
     */
    private Document createXmlDocumentOfContent(List<ContentField> contentFields) {
        Element mainElement = new Element("goobi");
        Document doc = new Document(mainElement);
        for (ContentField c : contentFields) {
            Element e = new Element(c.getName());
            e.setText(c.getValue());
            mainElement.addContent(e);
        }
        return doc;
    }

    /**
     * do the xslt transformation and pass back the transformation result as xml document
     * 
     * @param doc
     * @return
     * @throws XSLTransformException
     * @throws IOException
     */
    private Document doXmlTransformation(Document doc) throws XSLTransformException {
        String xsltfile = config.getString("xslt");
        String xsltpath = ConfigurationHelper.getInstance().getXsltFolder() + xsltfile;
        XSLTransformer transformer;
        transformer = new XSLTransformer(xsltpath);
        return transformer.transform(doc);
    }

    /**
     * write xml document into the file system
     *
     * @param doc
     * @param filename
     * @throws IOException
     * @throws FileNotFoundException
     */
    private void writeDocumentToFile(Document doc, String filename) throws IOException {
        XMLOutputter xmlOutputter = new XMLOutputter(Format.getPrettyFormat());
        File f = new File(ConfigurationHelper.getInstance().getTemporaryFolder(), filename);
        try (FileOutputStream fileOutputStream = new FileOutputStream(f)) {
            xmlOutputter.output(doc, fileOutputStream);
        }
    }

    @Override
    public PluginGuiType getPluginGuiType() {
        return PluginGuiType.NONE;
    }

    @Override
    public String getPagePath() {
        return "/uii/plugin_step_doi.xhtml";
    }

    @Override
    public PluginType getType() {
        return PluginType.Step;
    }

    @Override
    public String cancel() {
        return "/uii" + returnPath;
    }

    @Override
    public String finish() {
        return "/uii" + returnPath;
    }

    @Override
    public int getInterfaceVersion() {
        return 0;
    }

    @Override
    public HashMap<String, StepReturnValue> validate() {
        return null; //NOSONAR
    }

    @Override
    public boolean execute() {
        PluginReturnValue ret = run();
        return ret != PluginReturnValue.ERROR;
    }
}
