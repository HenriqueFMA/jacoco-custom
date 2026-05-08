package br.com.jacoco.plugin;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.*;
import org.jacoco.core.analysis.*;
import org.jacoco.core.data.ExecutionDataStore;
import org.jacoco.core.data.SessionInfoStore;
import org.jacoco.core.tools.ExecFileLoader;

import java.io.*;
import java.nio.file.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * Gera relatório HTML customizado do JaCoCo diferenciando
 * testes positivos (*Tests.java) dos negativos (*NegativeTests.java).
 *
 * @goal custom-report
 * @phase verify
 */
@Mojo(name = "custom-report", defaultPhase = LifecyclePhase.VERIFY)
public class CustomReportMojo extends AbstractMojo {

    /** Diretório raiz do projeto Maven */
    @Parameter(defaultValue = "${project.basedir}", readonly = true)
    private File basedir;

    /** Diretório dos .class compilados */
    @Parameter(defaultValue = "${project.build.outputDirectory}", readonly = true)
    private File classesDir;

    /** Arquivo .exec gerado pelo JaCoCo */
    @Parameter(defaultValue = "${project.build.directory}/jacoco.exec", property = "jacoco.execFile")
    private File execFile;

    /** Pasta de saída do relatório */
    @Parameter(defaultValue = "${project.build.directory}/custom-jacoco-report", property = "jacoco.reportDir")
    private File reportDir;

    /** Diretório de fontes dos testes (para identificar positivo/negativo) */
    @Parameter(defaultValue = "${project.build.testSourceDirectory}", readonly = true)
    private File testSourceDir;

    /** Nome do projeto exibido no relatório */
    @Parameter(defaultValue = "${project.name}", readonly = true)
    private String projectName;

    @Override
    public void execute() throws MojoExecutionException {
        getLog().info("=== JaCoCo Custom Report — Positivo vs Negativo ===");

        if (!execFile.exists()) {
            getLog().warn("Arquivo jacoco.exec não encontrado em: " + execFile);
            getLog().warn("Execute os testes com JaCoCo antes de gerar o relatório.");
            return;
        }

        try {
            reportDir.mkdirs();

            // 1. Carregar dados de execução
            ExecFileLoader loader = new ExecFileLoader();
            loader.load(execFile);
            ExecutionDataStore executionData = loader.getExecutionDataStore();
            SessionInfoStore sessionInfo = loader.getSessionInfoStore();

            // 2. Analisar classes
            CoverageBuilder coverageBuilder = new CoverageBuilder();
            Analyzer analyzer = new Analyzer(executionData, coverageBuilder);
            analyzer.analyzeAll(classesDir);

            IBundleCoverage bundle = coverageBuilder.getBundle(projectName);

            // 3. Mapear classes de teste (positivo / negativo)
            Map<String, TestClassInfo> testMap = scanTestClasses(testSourceDir);

            // 4. Gerar HTML
            File reportFile = new File(reportDir, "index.html");
            String html = HtmlReportGenerator.generate(bundle, testMap, projectName);
            Files.write(reportFile.toPath(), html.getBytes("UTF-8"));

            getLog().info("Relatório gerado em: " + reportFile.getAbsolutePath());

        } catch (IOException e) {
            throw new MojoExecutionException("Erro ao gerar relatório", e);
        }
    }

    private Map<String, TestClassInfo> scanTestClasses(File testDir) {
        Map<String, TestClassInfo> result = new HashMap<>();
        if (testDir == null || !testDir.exists()) return result;

        scanDir(testDir, result);
        return result;
    }

    private void scanDir(File dir, Map<String, TestClassInfo> result) {
        if (dir == null || !dir.exists()) return;
        for (File f : Objects.requireNonNull(dir.listFiles())) {
            if (f.isDirectory()) {
                scanDir(f, result);
            } else if (f.getName().endsWith(".java")) {
                String name = f.getName().replace(".java", "");
                boolean isNegative = name.endsWith("NegativeTests") || name.endsWith("NegativeTest");
                boolean isPositive = (name.endsWith("Tests") || name.endsWith("Test")) && !isNegative;
                if (isPositive || isNegative) {
                    // Deriva a classe alvo removendo sufixos
                    String target = name
                        .replaceAll("NegativeTests$", "")
                        .replaceAll("NegativeTest$", "")
                        .replaceAll("Tests$", "")
                        .replaceAll("Test$", "");
                    result.merge(target, new TestClassInfo(target, isPositive, isNegative),
                        (a, b) -> new TestClassInfo(target, a.hasPositive || b.hasPositive, a.hasNegative || b.hasNegative));
                }
            }
        }
    }

    public static class TestClassInfo {
        public final String targetClass;
        public final boolean hasPositive;
        public final boolean hasNegative;

        public TestClassInfo(String targetClass, boolean hasPositive, boolean hasNegative) {
            this.targetClass = targetClass;
            this.hasPositive = hasPositive;
            this.hasNegative = hasNegative;
        }

        public String getTestBadge() {
            if (hasPositive && hasNegative) return "both";
            if (hasPositive) return "positive";
            if (hasNegative) return "negative";
            return "none";
        }
    }
}
