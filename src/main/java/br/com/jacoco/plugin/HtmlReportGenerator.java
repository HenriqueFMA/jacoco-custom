package br.com.jacoco.plugin;

import org.jacoco.core.analysis.*;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class HtmlReportGenerator {

    public static String generate(IBundleCoverage bundle,
                                   Map<String, CustomReportMojo.TestClassInfo> testMap,
                                   String projectName) {

        ICounter instrTotal  = bundle.getInstructionCounter();
        ICounter branchTotal = bundle.getBranchCounter();
        ICounter lineTotal   = bundle.getLineCounter();
        ICounter methodTotal = bundle.getMethodCounter();
        ICounter classTotal  = bundle.getClassCounter();

        int coveredClasses = classTotal.getCoveredCount();
        int totalClasses   = classTotal.getTotalCount();
        int coveredLines   = lineTotal.getCoveredCount();
        int totalLines     = lineTotal.getTotalCount();

        int instrPct  = pct(instrTotal);
        int branchPct = pct(branchTotal);
        int linePct   = pct(lineTotal);
        int methodPct = pct(methodTotal);

        // Porcentagem geral: média das 5 métricas
        int overallPct = (instrPct + branchPct + linePct + methodPct + pct(classTotal)) / 5;

        int positiveOnly = 0, negativeOnly = 0, both = 0, none = 0;
        for (CustomReportMojo.TestClassInfo ti : testMap.values()) {
            if (ti.hasPositive && ti.hasNegative) both++;
            else if (ti.hasPositive) positiveOnly++;
            else if (ti.hasNegative) negativeOnly++;
            else none++;
        }

        String generated = LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"));

        StringBuilder sb = new StringBuilder();
        sb.append(htmlHead(projectName));

        sb.append("<body><div style=\"display:flex\">");
        sb.append("<div class=\"sidebar\">");
        sb.append("<div class=\"logo\">");
        sb.append("<svg width=\"28\" height=\"28\" viewBox=\"0 0 28 28\" fill=\"none\">");
        sb.append("<rect width=\"28\" height=\"28\" rx=\"8\" fill=\"#A100FF\"/>");
        sb.append("<path d=\"M7 14 L12 19 L21 9\" stroke=\"white\" stroke-width=\"2.5\" stroke-linecap=\"round\" stroke-linejoin=\"round\"/>");
        sb.append("</svg><span>JaCoCo+</span></div>");
        sb.append("<nav>");
        sb.append("<a href=\"#overview\" class=\"nav-item active\">");
        sb.append("<svg width=\"18\" height=\"18\" viewBox=\"0 0 24 24\" fill=\"none\" stroke=\"currentColor\" stroke-width=\"2\"><rect x=\"3\" y=\"3\" width=\"7\" height=\"7\"/><rect x=\"14\" y=\"3\" width=\"7\" height=\"7\"/><rect x=\"14\" y=\"14\" width=\"7\" height=\"7\"/><rect x=\"3\" y=\"14\" width=\"7\" height=\"7\"/></svg>");
        sb.append("Vis\u00e3o Geral</a>");
        sb.append("<a href=\"#packages\" class=\"nav-item\">");
        sb.append("<svg width=\"18\" height=\"18\" viewBox=\"0 0 24 24\" fill=\"none\" stroke=\"currentColor\" stroke-width=\"2\"><path d=\"M3 9l9-7 9 7v11a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2z\"/></svg>");
        sb.append("Pacotes</a>");
        sb.append("<a href=\"#classes\" class=\"nav-item\">");
        sb.append("<svg width=\"18\" height=\"18\" viewBox=\"0 0 24 24\" fill=\"none\" stroke=\"currentColor\" stroke-width=\"2\"><polyline points=\"16 18 22 12 16 6\"/><polyline points=\"8 6 2 12 8 18\"/></svg>");
        sb.append("Classes</a>");
        sb.append("</nav>");
        sb.append("<div class=\"sidebar-footer\">Gerado em<br><strong>").append(generated).append("</strong></div>");
        sb.append("</div>");

        sb.append("<main class=\"main\">");
        sb.append("<header class=\"topbar\">");
        sb.append("<div><h1>").append(escHtml(projectName)).append("</h1>");
        sb.append("<p class=\"subtitle\">Relat\u00f3rio de cobertura com an\u00e1lise positivo \u00d7 negativo</p></div>");
        sb.append("<div class=\"legend-pills\">");
        sb.append("<span class=\"pill pill-pos\">\u2713 Positivos</span>");
        sb.append("<span class=\"pill pill-neg\">\u2717 Negativos</span>");
        sb.append("<span class=\"pill pill-both\">\u00b1 Ambos</span>");
        sb.append("<span class=\"pill pill-none\">\u2014 Sem teste</span>");
        sb.append("</div></header>");

        sb.append("<section id=\"overview\">");
        sb.append("<h2 class=\"section-title\">Cobertura geral</h2>");

        // Card de porcentagem geral
        sb.append(overallCard(overallPct));

        sb.append("<div class=\"metrics-grid\">");
        sb.append(metricCard("Instru\u00e7\u00f5es", instrPct,  instrTotal.getCoveredCount(),  instrTotal.getTotalCount()));
        sb.append(metricCard("Branches",    branchPct, branchTotal.getCoveredCount(), branchTotal.getTotalCount()));
        sb.append(metricCard("Linhas",      linePct,   coveredLines,                  totalLines));
        sb.append(metricCard("M\u00e9todos",    methodPct, methodTotal.getCoveredCount(), methodTotal.getTotalCount()));
        sb.append(metricCard("Classes",     pct(classTotal), coveredClasses,          totalClasses));
        sb.append("</div>");

        sb.append("<div class=\"test-summary\">");
        sb.append(tsCard("ts-pos",  "+",       String.valueOf(positiveOnly), "S\u00f3 positivos"));
        sb.append(tsCard("ts-neg",  "-",       String.valueOf(negativeOnly), "S\u00f3 negativos"));
        sb.append(tsCard("ts-both", "\u00b1",  String.valueOf(both),         "Ambos os tipos"));
        sb.append(tsCard("ts-none", "?",       String.valueOf(none),         "Sem teste mapeado"));
        sb.append("</div></section>");

        sb.append("<section id=\"packages\">");
        sb.append("<h2 class=\"section-title\">Cobertura por pacote</h2>");
        sb.append("<div class=\"table-wrap\"><table>");
        sb.append("<thead><tr><th>Pacote</th><th>Status</th><th>Instru\u00e7\u00f5es</th><th>Branches</th><th>Linhas</th><th>M\u00e9todos</th><th>Classes</th></tr></thead><tbody>");

        List<IPackageCoverage> packages = new ArrayList<>(bundle.getPackages());
        packages.sort(Comparator.comparingInt(p -> pct(p.getInstructionCounter())));

        for (IPackageCoverage pkg : packages) {
            int ip = pct(pkg.getInstructionCounter());
            int bp = pct(pkg.getBranchCounter());
            String shortName = pkg.getName().replace('/', '.').replaceAll("^acc\\.br\\.projetoFinal\\.Accenture\\.?", "");
            if (shortName.isEmpty()) shortName = "(root)";
            sb.append("<tr>");
            sb.append("<td class=\"pkg-name\"><code>").append(escHtml(shortName)).append("</code></td>");
            sb.append("<td>").append(statusBadge(ip)).append("</td>");
            sb.append("<td>").append(progressCell(ip)).append("</td>");
            sb.append("<td>").append(progressCell(bp)).append("</td>");
            sb.append("<td>").append(pct(pkg.getLineCounter())).append("%</td>");
            sb.append("<td>").append(pct(pkg.getMethodCounter())).append("%</td>");
            sb.append("<td>").append(pct(pkg.getClassCounter())).append("%</td>");
            sb.append("</tr>\n");
        }
        sb.append("</tbody></table></div></section>");

        sb.append("<section id=\"classes\">");
        sb.append("<h2 class=\"section-title\">Cobertura por classe</h2>");
        sb.append("<div class=\"filter-bar\">");
        sb.append("<input type=\"text\" id=\"classFilter\" placeholder=\"Filtrar classe...\" oninput=\"filterClasses(this.value)\">");
        sb.append("<div class=\"filter-pills\">");
        sb.append("<button class=\"fpill active\" onclick=\"filterByTest('all',this)\">Todos</button>");
        sb.append("<button class=\"fpill fpill-pos\" onclick=\"filterByTest('positive',this)\">Positivos</button>");
        sb.append("<button class=\"fpill fpill-neg\" onclick=\"filterByTest('negative',this)\">Negativos</button>");
        sb.append("<button class=\"fpill fpill-both\" onclick=\"filterByTest('both',this)\">Ambos</button>");
        sb.append("<button class=\"fpill fpill-none\" onclick=\"filterByTest('none',this)\">Sem teste</button>");
        sb.append("</div></div>");
        sb.append("<div class=\"table-wrap\"><table id=\"classTable\">");
        sb.append("<thead><tr><th>Classe</th><th>Pacote</th><th>Testes</th><th>Instru\u00e7\u00f5es</th><th>Branches</th><th>Linhas</th><th>M\u00e9todos</th></tr></thead><tbody>");

        for (IPackageCoverage pkg : bundle.getPackages()) {
            String pkgShort = pkg.getName().replace('/', '.').replaceAll("^acc\\.br\\.projetoFinal\\.Accenture\\.?", "");
            if (pkgShort.isEmpty()) pkgShort = "(root)";

            List<IClassCoverage> classes = new ArrayList<>(pkg.getClasses());
            classes.sort(Comparator.comparingInt(c -> pct(c.getInstructionCounter())));

            for (IClassCoverage cls : classes) {
                String simpleName = cls.getName().substring(cls.getName().lastIndexOf('/') + 1);
                CustomReportMojo.TestClassInfo ti = testMap.get(simpleName);
                String badge = ti != null ? ti.getTestBadge() : "none";
                int ip2 = pct(cls.getInstructionCounter());

                sb.append("<tr data-testbadge=\"").append(badge).append("\" data-classname=\"").append(escHtml(simpleName.toLowerCase())).append("\">");
                sb.append("<td class=\"class-name\"><code>").append(escHtml(simpleName)).append("</code></td>");
                sb.append("<td class=\"pkg-name\"><span class=\"pkg-tag\">").append(escHtml(pkgShort)).append("</span></td>");
                sb.append("<td>").append(testBadgeHtml(badge)).append("</td>");
                sb.append("<td>").append(progressCell(ip2)).append("</td>");
                sb.append("<td>").append(progressCell(pct(cls.getBranchCounter()))).append("</td>");
                sb.append("<td>").append(pct(cls.getLineCounter())).append("%</td>");
                sb.append("<td>").append(pct(cls.getMethodCounter())).append("%</td>");
                sb.append("</tr>\n");
            }
        }
        sb.append("</tbody></table></div></section>");
        sb.append("</main></div>");
        sb.append(htmlScript());
        sb.append("</body></html>");
        return sb.toString();
    }

    private static String overallCard(int pct) {
        String cls   = pct >= 70 ? "pass" : pct >= 30 ? "warn" : "fail";
        String label = pct >= 70 ? "Cobertura satisfat\u00f3ria"
                     : pct >= 30 ? "Cobertura parcial"
                     :             "Cobertura insuficiente";
        return "<div class=\"overall-card oc-" + cls + "\">" +
               "<div class=\"oc-left\">" +
               "<div class=\"oc-title\">Cobertura Total</div>" +
               "<div class=\"oc-subtitle\">" + label + "</div>" +
               "<div class=\"oc-bar-bg\"><div class=\"oc-bar-fill oc-bar-" + cls + "\" style=\"width:" + pct + "%\"></div></div>" +
               "<div class=\"oc-legend\">M\u00e9dia de instru\u00e7\u00f5es, branches, linhas, m\u00e9todos e classes</div>" +
               "</div>" +
               "<div class=\"oc-right\">" +
               "<div class=\"oc-pct oc-pct-" + cls + "\">" + pct + "<span class=\"oc-unit\">%</span></div>" +
               "</div>" +
               "</div>";
    }

    private static String tsCard(String cssClass, String icon, String count, String label) {
        return "<div class=\"ts-card " + cssClass + "\">" +
               "<div class=\"ts-icon\">" + icon + "</div>" +
               "<div class=\"ts-info\">" +
               "<span class=\"ts-count\">" + count + "</span>" +
               "<span class=\"ts-label\">" + label + "</span>" +
               "</div></div>";
    }

    private static int pct(ICounter c) {
        if (c.getTotalCount() == 0) return 100;
        return (int) Math.round(c.getCoveredRatio() * 100);
    }

    private static String statusBadge(int pct) {
        if (pct >= 70) return "<span class=\"badge badge-pass\">\u2713 Positivo</span>";
        if (pct >= 30) return "<span class=\"badge badge-warn\">\u26a0 Aten\u00e7\u00e3o</span>";
        return "<span class=\"badge badge-fail\">\u2717 Negativo</span>";
    }

    private static String progressCell(int pct) {
        String barCls  = pct >= 70 ? "bar-pass"  : pct >= 30 ? "bar-warn"  : "bar-fail";
        String textCls = pct >= 70 ? "pct-pass"  : pct >= 30 ? "pct-warn"  : "pct-fail";
        return "<div class=\"bar-wrap\"><div class=\"bar-bg\"><div class=\"bar-fill " + barCls + "\" style=\"width:" + pct + "%\"></div></div>" +
               "<span class=\"pct " + textCls + "\">" + pct + "%</span></div>";
    }

    private static String testBadgeHtml(String badge) {
        if ("positive".equals(badge)) return "<span class=\"badge badge-pos\">\u2713 Positivo</span>";
        if ("negative".equals(badge)) return "<span class=\"badge badge-neg\">\u2717 Negativo</span>";
        if ("both".equals(badge))     return "<span class=\"badge badge-both\">\u00b1 Ambos</span>";
        return "<span class=\"badge badge-none\">\u2014 Nenhum</span>";
    }

    private static String metricCard(String label, int pct, int covered, int total) {
        String cls = pct >= 70 ? "pass" : pct >= 30 ? "warn" : "fail";
        return "<div class=\"metric-card mc-" + cls + "\">" +
               "<div class=\"mc-label\">" + label + "</div>" +
               "<div class=\"mc-value mc-value-" + cls + "\">" + pct + "<span class=\"mc-unit\">%</span></div>" +
               "<div class=\"mc-bar\"><div class=\"mc-bar-fill\" style=\"width:" + pct + "%\"></div></div>" +
               "<div class=\"mc-sub\">" + covered + " / " + total + "</div>" +
               "</div>";
    }

    private static String escHtml(String s) {
        if (s == null) return "";
        return s.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
    }

    private static String htmlHead(String title) {
        return "<!DOCTYPE html>" +
            "<html lang=\"pt-BR\"><head>" +
            "<meta charset=\"UTF-8\">" +
            "<meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">" +
            "<title>JaCoCo \u2014 " + escHtml(title) + "</title>" +
            "<link rel=\"preconnect\" href=\"https://fonts.googleapis.com\">" +
            "<link rel=\"preconnect\" href=\"https://fonts.gstatic.com\" crossorigin>" +
            "<link href=\"https://fonts.googleapis.com/css2?family=JetBrains+Mono:wght@400;500&family=Inter:wght@400;500;600&display=swap\" rel=\"stylesheet\">" +
            "<style>" +
            "*,*::before,*::after{box-sizing:border-box;margin:0;padding:0}" +
            ":root{--bg:#0a0a0a;--bg2:#111111;--bg3:#1a1a1a;--border:rgba(255,255,255,0.08);--border2:rgba(255,255,255,0.15);--text:#f0f0f0;--text2:#a0a0a0;--text3:#606060;--accent:#A100FF;--accent2:#C84DFF;--pass:#00C853;--pass-bg:rgba(0,200,83,0.12);--pass-border:rgba(0,200,83,0.30);--warn:#FF9D00;--warn-bg:rgba(255,157,0,0.12);--warn-border:rgba(255,157,0,0.30);--fail:#FF3D3D;--fail-bg:rgba(255,61,61,0.12);--fail-border:rgba(255,61,61,0.30);--pos:#00C853;--pos-bg:rgba(0,200,83,0.10);--neg:#FF3D3D;--neg-bg:rgba(255,61,61,0.10);--both:#C84DFF;--both-bg:rgba(200,77,255,0.10);--none:#606060;--none-bg:rgba(96,96,96,0.10)}" +
            "html{font-family:'Inter',sans-serif;background:var(--bg);color:var(--text);scroll-behavior:smooth}" +
            "body{display:flex;min-height:100vh}" +
            ".sidebar{width:220px;min-height:100vh;background:var(--bg2);border-right:1px solid var(--border);display:flex;flex-direction:column;padding:1.5rem 1rem;position:sticky;top:0;height:100vh}" +
            ".logo{display:flex;align-items:center;gap:10px;font-weight:600;font-size:16px;color:var(--text);margin-bottom:2.5rem;padding-left:4px}" +
            "nav{display:flex;flex-direction:column;gap:4px}" +
            ".nav-item{display:flex;align-items:center;gap:10px;padding:8px 12px;border-radius:8px;color:var(--text2);text-decoration:none;font-size:14px;transition:all 0.15s}" +
            ".nav-item:hover,.nav-item.active{background:var(--bg3);color:var(--text)}" +
            ".nav-item.active{color:var(--accent2);border-left:2px solid var(--accent);padding-left:10px}" +
            ".sidebar-footer{margin-top:auto;font-size:11px;color:var(--text3);line-height:1.6;padding:12px;background:var(--bg3);border-radius:8px}" +
            ".main{flex:1;padding:2rem 2.5rem;max-width:1100px}" +
            ".topbar{display:flex;align-items:flex-start;justify-content:space-between;gap:1rem;margin-bottom:2rem;flex-wrap:wrap}" +
            "h1{font-size:22px;font-weight:600;color:var(--text)}" +
            ".subtitle{font-size:13px;color:var(--text2);margin-top:4px}" +
            ".legend-pills{display:flex;gap:8px;flex-wrap:wrap;align-items:center}" +
            ".pill{font-size:12px;font-weight:500;padding:4px 12px;border-radius:20px}" +
            ".pill-pos{background:var(--pos-bg);color:var(--pos);border:1px solid rgba(0,200,83,0.25)}" +
            ".pill-neg{background:var(--neg-bg);color:var(--neg);border:1px solid rgba(255,61,61,0.25)}" +
            ".pill-both{background:var(--both-bg);color:var(--both);border:1px solid rgba(200,77,255,0.25)}" +
            ".pill-none{background:var(--none-bg);color:var(--none);border:1px solid rgba(96,96,96,0.25)}" +
            "section{margin-bottom:3rem}" +
            ".section-title{font-size:15px;font-weight:600;color:var(--text2);letter-spacing:0.04em;text-transform:uppercase;margin-bottom:1rem}" +
            ".overall-card{display:flex;align-items:center;justify-content:space-between;gap:2rem;background:var(--bg2);border:1px solid var(--border);border-radius:14px;padding:20px 24px;margin-bottom:1.5rem}" +
            ".oc-pass{border-left:4px solid var(--pass)}.oc-warn{border-left:4px solid var(--warn)}.oc-fail{border-left:4px solid var(--fail)}" +
            ".oc-left{flex:1}" +
            ".oc-title{font-size:11px;font-weight:600;color:var(--text3);text-transform:uppercase;letter-spacing:0.08em;margin-bottom:4px}" +
            ".oc-subtitle{font-size:18px;font-weight:600;color:var(--text);margin-bottom:14px}" +
            ".oc-bar-bg{height:8px;background:var(--bg3);border-radius:4px;overflow:hidden;margin-bottom:8px}" +
            ".oc-bar-fill{height:100%;border-radius:4px}" +
            ".oc-bar-pass{background:linear-gradient(90deg,var(--pass),#00E676)}" +
            ".oc-bar-warn{background:linear-gradient(90deg,var(--warn),#FFB300)}" +
            ".oc-bar-fail{background:linear-gradient(90deg,var(--fail),#FF6B6B)}" +
            ".oc-legend{font-size:11px;color:var(--text3)}" +
            ".oc-right{text-align:right;flex-shrink:0}" +
            ".oc-pct{font-size:56px;font-weight:700;font-family:'JetBrains Mono',monospace;line-height:1}" +
            ".oc-pct-pass{color:var(--pass)}.oc-pct-warn{color:var(--warn)}.oc-pct-fail{color:var(--fail)}" +
            ".oc-unit{font-size:26px;font-weight:400}" +
            ".metrics-grid{display:grid;grid-template-columns:repeat(auto-fit,minmax(150px,1fr));gap:12px;margin-bottom:1.5rem}" +
            ".metric-card{background:var(--bg2);border:1px solid var(--border);border-radius:12px;padding:16px}" +
            ".mc-pass{border-top:2px solid var(--pass)}.mc-warn{border-top:2px solid var(--warn)}.mc-fail{border-top:2px solid var(--fail)}" +
            ".mc-label{font-size:11px;color:var(--text3);text-transform:uppercase;letter-spacing:0.06em;margin-bottom:6px}" +
            ".mc-value{font-size:28px;font-weight:600;font-family:'JetBrains Mono',monospace}" +
            ".mc-value-pass{color:var(--pass)}.mc-value-warn{color:var(--warn)}.mc-value-fail{color:var(--fail)}" +
            ".mc-unit{font-size:16px}" +
            ".mc-bar{height:4px;background:var(--bg3);border-radius:2px;margin:8px 0 6px;overflow:hidden}" +
            ".mc-bar-fill{height:100%;border-radius:2px}" +
            ".mc-pass .mc-bar-fill{background:var(--pass)}.mc-warn .mc-bar-fill{background:var(--warn)}.mc-fail .mc-bar-fill{background:var(--fail)}" +
            ".mc-sub{font-size:11px;color:var(--text3)}" +
            ".test-summary{display:grid;grid-template-columns:repeat(auto-fit,minmax(130px,1fr));gap:12px}" +
            ".ts-card{background:var(--bg2);border:1px solid var(--border);border-radius:12px;padding:14px 16px;display:flex;align-items:center;gap:12px}" +
            ".ts-icon{font-size:20px;font-weight:700;width:36px;height:36px;border-radius:8px;display:flex;align-items:center;justify-content:center}" +
            ".ts-pos .ts-icon{background:var(--pos-bg);color:var(--pos)}" +
            ".ts-neg .ts-icon{background:var(--neg-bg);color:var(--neg)}" +
            ".ts-both .ts-icon{background:var(--both-bg);color:var(--both)}" +
            ".ts-none .ts-icon{background:var(--none-bg);color:var(--none)}" +
            ".ts-info{display:flex;flex-direction:column}" +
            ".ts-count{font-size:22px;font-weight:600;font-family:'JetBrains Mono',monospace;color:var(--text)}" +
            ".ts-label{font-size:11px;color:var(--text3)}" +
            ".table-wrap{overflow-x:auto;border:1px solid var(--border);border-radius:12px;margin-top:1rem}" +
            "table{width:100%;border-collapse:collapse;font-size:13px}" +
            "th{text-align:left;padding:10px 14px;font-size:11px;font-weight:500;color:var(--text3);background:var(--bg2);text-transform:uppercase;letter-spacing:0.05em;border-bottom:1px solid var(--border)}" +
            "td{padding:10px 14px;border-bottom:1px solid var(--border);color:var(--text);vertical-align:middle}" +
            "tr:last-child td{border-bottom:none}" +
            "tbody tr:hover td{background:var(--bg2)}" +
            ".pkg-name code{font-family:'JetBrains Mono',monospace;font-size:12px;color:var(--text2)}" +
            ".class-name code{font-family:'JetBrains Mono',monospace;font-size:12px;color:var(--accent2)}" +
            ".pkg-tag{font-size:11px;background:var(--bg3);color:var(--text3);padding:2px 8px;border-radius:4px}" +
            ".badge{display:inline-flex;align-items:center;gap:4px;padding:3px 10px;border-radius:20px;font-size:11px;font-weight:500}" +
            ".badge-pass{background:var(--pass-bg);color:var(--pass);border:1px solid var(--pass-border)}" +
            ".badge-warn{background:var(--warn-bg);color:var(--warn);border:1px solid var(--warn-border)}" +
            ".badge-fail{background:var(--fail-bg);color:var(--fail);border:1px solid var(--fail-border)}" +
            ".badge-pos{background:var(--pos-bg);color:var(--pos);border:1px solid rgba(0,200,83,0.25)}" +
            ".badge-neg{background:var(--neg-bg);color:var(--neg);border:1px solid rgba(255,61,61,0.25)}" +
            ".badge-both{background:var(--both-bg);color:var(--both);border:1px solid rgba(200,77,255,0.25)}" +
            ".badge-none{background:var(--none-bg);color:var(--none);border:1px solid rgba(96,96,96,0.25)}" +
            ".bar-wrap{display:flex;align-items:center;gap:8px}" +
            ".bar-bg{flex:1;height:6px;background:var(--bg3);border-radius:3px;overflow:hidden;min-width:70px}" +
            ".bar-fill{height:100%;border-radius:3px}" +
            ".bar-pass{background:var(--pass)}.bar-warn{background:var(--warn)}.bar-fail{background:var(--fail)}" +
            ".pct{font-size:12px;font-family:'JetBrains Mono',monospace;min-width:34px;text-align:right}" +
            ".pct-pass{color:var(--pass)}.pct-warn{color:var(--warn)}.pct-fail{color:var(--fail)}" +
            ".filter-bar{display:flex;gap:12px;align-items:center;margin-bottom:12px;flex-wrap:wrap}" +
            ".filter-bar input{background:var(--bg2);border:1px solid var(--border2);border-radius:8px;padding:7px 14px;color:var(--text);font-size:13px;outline:none;width:240px}" +
            ".filter-bar input:focus{border-color:var(--accent)}" +
            ".filter-pills{display:flex;gap:6px;flex-wrap:wrap}" +
            ".fpill{background:var(--bg2);border:1px solid var(--border);border-radius:20px;padding:4px 14px;font-size:12px;cursor:pointer;color:var(--text2);transition:all 0.15s}" +
            ".fpill:hover{border-color:var(--border2);color:var(--text)}" +
            ".fpill.active{background:var(--accent);color:white;border-color:var(--accent)}" +
            ".fpill-pos.active{background:rgba(0,200,83,0.20);border-color:rgba(0,200,83,0.40);color:var(--pos)}" +
            ".fpill-neg.active{background:rgba(255,61,61,0.20);border-color:rgba(255,61,61,0.40);color:var(--neg)}" +
            ".fpill-both.active{background:rgba(200,77,255,0.20);border-color:rgba(200,77,255,0.40);color:var(--both)}" +
            ".fpill-none.active{background:rgba(96,96,96,0.20);border-color:rgba(96,96,96,0.40);color:var(--none)}" +
            "tr.hidden{display:none}" +
            "</style></head>";
    }

    private static String htmlScript() {
        return "<script>" +
            "var currentFilter='all';var currentText='';" +
            "function applyFilters(){" +
            "document.querySelectorAll('#classTable tbody tr').forEach(function(row){" +
            "var badge=row.dataset.testbadge||'none';" +
            "var name=row.dataset.classname||'';" +
            "var matchBadge=currentFilter==='all'||badge===currentFilter;" +
            "var matchText=name.indexOf(currentText.toLowerCase())>=0;" +
            "row.classList.toggle('hidden',!(matchBadge&&matchText));});}" +
            "function filterClasses(val){currentText=val;applyFilters();}" +
            "function filterByTest(type,btn){" +
            "currentFilter=type;" +
            "document.querySelectorAll('.fpill').forEach(function(b){b.classList.remove('active');});" +
            "btn.classList.add('active');applyFilters();}" +
            "var sections=['overview','packages','classes'];" +
            "window.addEventListener('scroll',function(){" +
            "var current='';" +
            "sections.forEach(function(id){" +
            "var el=document.getElementById(id);" +
            "if(el&&window.scrollY>=el.offsetTop-120)current=id;});" +
            "document.querySelectorAll('.nav-item').forEach(function(a){" +
            "a.classList.toggle('active',a.getAttribute('href')==='#'+current);});});" +
            "</script>";
    }
}