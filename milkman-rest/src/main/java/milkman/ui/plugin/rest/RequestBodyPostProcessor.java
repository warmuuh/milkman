package milkman.ui.plugin.rest;

import java.util.List;

public interface RequestBodyPostProcessor {

    List<RequestBodyPostProcessor> ALL_PROCESSORS = List.of(
            new MultipartRequestBodyPostProcessor(),
            new FormDataRequestBodyPostProcessor()
    );

    boolean canProcess(String contentType);

    String process(String body);


    class MultipartRequestBodyPostProcessor implements RequestBodyPostProcessor {

        @Override
        public boolean canProcess(String contentType) {
            return contentType.contains("multipart/");
        }

        @Override
        public String process(String body) {
            if (!body.contains("\r\n")) {
                return body.replace("\n", "\r\n");
            }
            return body;
        }
    }

    class FormDataRequestBodyPostProcessor implements RequestBodyPostProcessor {

        @Override
        public boolean canProcess(String contentType) {
            return contentType.contains("application/x-www-form-urlencoded");
        }

        @Override
        public String process(String body) {
            return body.replace("\r", "").replace("\n", "");
        }
    }


    static String processBody(String contentType, String body) {
        for (RequestBodyPostProcessor processor : processors()) {
            if (processor.canProcess(contentType)) {
                return processor.process(body);
            }
        }
        return body;
    }

    static List<RequestBodyPostProcessor> processors() {
        return ALL_PROCESSORS;
    }

}
