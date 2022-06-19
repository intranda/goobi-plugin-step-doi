package de.intranda.goobi.plugins;

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
import org.goobi.beans.Step;
import org.goobi.production.enums.PluginGuiType;
import org.goobi.production.enums.PluginReturnValue;
import org.goobi.production.enums.PluginType;
import org.goobi.production.enums.StepReturnValue;
import org.goobi.production.plugin.interfaces.IStepPluginVersion2;

import org.goobi.beans.Process;
import de.sub.goobi.config.ConfigPlugins;
import de.sub.goobi.helper.VariableReplacer;
import de.sub.goobi.helper.exceptions.DAOException;
import de.sub.goobi.helper.exceptions.SwapException;
import lombok.Getter;
import lombok.extern.log4j.Log4j2;
import net.xeoh.plugins.base.annotations.PluginImplementation;
import ugh.dl.Fileformat;
import ugh.exceptions.PreferencesException;
import ugh.exceptions.ReadException;
import ugh.exceptions.WriteException;

@PluginImplementation
@Log4j2
public class DoiStepPlugin implements IStepPluginVersion2 {

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

	@Override
	public void initialize(Step step, String returnPath) {
		this.returnPath = returnPath;
		this.step = step;

		// read parameters from correct block in configuration file
		config = ConfigPlugins.getProjectAndStepConfig(title, step);

		value = config.getString("value", "default value");
		allowTaskFinishButtons = config.getBoolean("allowTaskFinishButtons", false);

		log.info("Doi step plugin initialized");
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
		return null;
	}

	@Override
	public boolean execute() {
		PluginReturnValue ret = run();
		return ret != PluginReturnValue.ERROR;
	}

	@Override
	public PluginReturnValue run() {
		boolean successful = false;

		try {
			// Create a list of variables
			List<ContentField> contentFields = new ArrayList<ContentField>();

			// Open the metadata file for the process and prepare the VariableReplacer
			Process p = step.getProzess();
			Fileformat ff = p.readMetadataFile();
			VariableReplacer replacer = new VariableReplacer(ff.getDigitalDocument(), p.getRegelsatz().getPreferences(),
					p, null);

			// run through all defined fields to fill their content
			List<HierarchicalConfiguration> fields = config.configurationsAt("field");
			for (HierarchicalConfiguration field : fields) {
				ContentField cf = new ContentField();
				cf.setName(field.getString("@name"));
				// run through all data elements to fill the field value
				List<HierarchicalConfiguration> datas = field.configurationsAt("data");
				for (HierarchicalConfiguration d : datas) {
					String content = d.getString("@content");
					String result = replacer.replace(content);
					// if the content ist not ampty and it is different from the variable use it
					if (StringUtils.isNotBlank(result) && !result.equals(content)) {
						cf.setValue(result);
						break;
					}
				}

				// if no content was set yet then set the default if available
				if (StringUtils.isBlank(cf.getValue())) {
					String thedefault = field.getString("@default");
					if (!StringUtils.isBlank(thedefault)) {
						cf.setValue(thedefault);
					}
				}
				contentFields.add(cf);
			}

			for (ContentField c : contentFields) {
				System.out.println(c.getName() + ": " + c.getValue());
			}
		} catch (ReadException | PreferencesException | WriteException | IOException | InterruptedException
				| SwapException | DAOException e) {
			log.error("Error while executing the DOI plugin", e);
		}

		log.info("Doi step plugin executed");
		if (!successful) {
			return PluginReturnValue.ERROR;
		}
		return PluginReturnValue.FINISH;
	}
}
