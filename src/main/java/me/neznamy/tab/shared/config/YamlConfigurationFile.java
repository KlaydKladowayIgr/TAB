package me.neznamy.tab.shared.config;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.parser.ParserException;
import org.yaml.snakeyaml.scanner.ScannerException;

import me.neznamy.tab.shared.Shared;
import me.neznamy.tab.shared.placeholders.Placeholders;

public class YamlConfigurationFile extends ConfigurationFile {
	
	private Yaml yaml;
	
	@SuppressWarnings("unchecked")
	public YamlConfigurationFile(File dataFolder, String source, String destination, List<String> header) throws Exception{
		super(dataFolder, source, destination, header);
		FileInputStream input = null;
		try {
			DumperOptions options = new DumperOptions();
			options.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);
			yaml = new Yaml(options);
			input = new FileInputStream(file);
			values = (Map<String, Object>) yaml.load(new InputStreamReader(input, Charset.forName("UTF-8")));
			if (values == null) values = new HashMap<String, Object>();
			input.close();
			Shared.platform.convertConfig(this);
			if (!hasHeader()) fixHeader();
			Placeholders.findAllUsed(values);
		} catch (ParserException | ScannerException e) {
			input.close();
			Shared.errorManager.startupWarn("File " + destination + " has broken formatting.");
			Shared.brokenFile = file.getPath();
			Shared.platform.sendConsoleMessage("&6[TAB] Error message from yaml parser: " + e.getMessage());
			String fix = Shared.errorManager.suggestYamlFix(e, readAllLines());
			if (fix != null) {
				Shared.platform.sendConsoleMessage("&d[TAB] Suggestion: " + fix);
			}
			throw e;
		}
	}
	public YamlConfigurationFile(File dataFolder, String sourceAndDestination, List<String> header) throws Exception{
		this(dataFolder, sourceAndDestination, sourceAndDestination, header);
	}
	
	public void save() {
		try {
			Writer writer = new OutputStreamWriter(new FileOutputStream(file), StandardCharsets.UTF_8);
			yaml.dump(values, writer);
			writer.close();
			if (!hasHeader()) fixHeader();
		} catch (Throwable e) {
			Shared.errorManager.criticalError("Failed to save yaml file " + file.getPath(), e);
		}
	}
}