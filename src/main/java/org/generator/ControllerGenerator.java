package org.generator;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

public final class ControllerGenerator {

    private ControllerGenerator() {}

    public static void generatorController(Class<?> entityClass) throws IOException {
        String controllerClassName = entityClass.getName() + "Controller";
        File directory = new File("../controller/");
        boolean exceptionOccurred = false;
        if (!directory.exists()) {
            boolean mkdirRes = directory.mkdir();
            exceptionOccurred = !mkdirRes;
        }
        if (!exceptionOccurred) {
            File controllerJavaFile = new File("../controller/" + controllerClassName + ".java");
            try (FileOutputStream fos = new FileOutputStream(controllerJavaFile)) {
                String packageDeclare = "package org.controller";
                String importDeclare = "import org.springframework.stereotype.Controller";
                fos.flush();
            }

        } else {
            throw new IOException("Failed to create directory!");
        }
    }

}
