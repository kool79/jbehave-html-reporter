package org.jbehave.reports;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import org.apache.commons.io.IOUtils;
import org.jbehave.core.reporters.StepdocReporter;
import org.jbehave.core.steps.Stepdoc;

import java.io.IOException;
import java.io.Writer;
import java.net.URISyntaxException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class JsonStepDocAggregator implements StepdocReporter {

    private static final String DATA_FILE_NAME = "stepdocs.json";

    private final ObjectMapper objectMapper;

    public JsonStepDocAggregator(){
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.configure(SerializationFeature.FLUSH_AFTER_WRITE_VALUE, true);

        this.objectMapper = objectMapper;
    }

    public JsonStepDocAggregator(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public void stepdocs(List<Stepdoc> stepdocs, List<Object> stepsInstances) {
        List<Map> steps = new ArrayList<>();

        for(Stepdoc stepdoc: stepdocs){
            steps.add(convertStepDoc(stepdoc));
        }

        try(Writer wr = createJsonWriter()){
            objectMapper.writer().withDefaultPrettyPrinter().writeValue(wr, steps);

            new CopyTarget(ReportConfig.getInstance().getOutputPath())
                    .copyClasspathResource("css")
                    .copyClasspathResource("js")
                    .copyClasspathResource("stepdoc-report.html");
        } catch (IOException | URISyntaxException ex){
            throw new RuntimeException("Failed generate report", ex);
        }
    }

    public void stepdocsMatching(String stepAsString, List<Stepdoc> matching, List<Object> stepsIntances) {}

    private Writer createJsonWriter() throws IOException {
        return Files.newBufferedWriter(ReportConfig.getInstance().getOutputPath().resolveSibling(DATA_FILE_NAME),
                Charset.forName("UTF-8"));
    }

    private Map<String, Object> convertStepDoc(Stepdoc stepdoc){
        HashMap<String, Object> stepMap = new HashMap<>();

        stepMap.put("type", stepdoc.getStepType());
        stepMap.put("pattern", stepdoc.getPattern());
        stepMap.put("method", stepdoc.getMethod().getName());
        stepMap.put("library", stepdoc.getMethod().getDeclaringClass().getSimpleName());

        return stepMap;
    }
}
