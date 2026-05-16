# JaCoCo Custom Report — Positivo × Negativo

Relatório HTML que diferencia visualmente testes positivos (`*Tests.java`)
de testes negativos (`*NegativeTests.java`), com dashboard por pacote e por classe.

---

## Estrutura do projeto de testes esperada

O plugin detecta automaticamente os tipos de teste pelo nome do arquivo:

| Arquivo                              | Tipo detectado |
|--------------------------------------|----------------|
| `BoletoControllerTests.java`         | ✓ Positivo     |
| `BoletoControllerNegativeTests.java` | ✗ Negativo     |
| `ClienteControllerTests.java`        | ✓ Positivo     |
| `ClienteControllerNegativeTests.java`| ✗ Negativo     |

Sufixos reconhecidos: `*Tests`, `*Test`, `*NegativeTests`, `*NegativeTest`

---

## Passo a passo de instalação

### 1. Instalar o plugin no repositório local Maven

Na pasta raiz do plugin (onde está o `pom.xml`):

```bash
cd ~/Área\ de\ Trabalho/Accenture/projeto/jacoco-custom-report
mvn clean install
```

Aguarde o `BUILD SUCCESS` antes de continuar.

---

### 2. Adicionar o plugin ao pom.xml do projeto Accenture

Dentro de `<build><plugins>` no `pom.xml` do Back-End, adicione o bloco abaixo
**após** o plugin do JaCoCo padrão:

```xml
<!-- Plugin customizado — relatório positivo × negativo -->
<plugin>
    <groupId>br.com.jacoco</groupId>
    <artifactId>jacoco-custom-report</artifactId>
    <version>1.0.0</version>
    <executions>
        <execution>
            <id>custom-jacoco-report</id>
            <phase>verify</phase>
            <goals>
                <goal>custom-report</goal>
            </goals>
        </execution>
    </executions>
</plugin>
```

> O projeto ficará com dois plugins JaCoCo — isso é esperado:
> - `jacoco-maven-plugin` → coleta os dados e gera o `.exec`
> - `jacoco-custom-report` → lê o `.exec` e gera o HTML customizado

---

### 3. Gerar o relatório

Na pasta do projeto Back-End:

```bash
cd ~/Área\ de\ Trabalho/Accenture/projeto/Back-End/Accenture
mvn verify -DskipTests
```

> Use `-DskipTests` para não reexecutar os testes — o `.exec` já existe de uma
> execução anterior com `mvn clean verify`.

O relatório será gerado em:

```
target/custom-jacoco-report/index.html
```

Para abrir direto no browser:

```bash
Linux

xdg-open ~/Área\ de\ Trabalho/Accenture/projeto/Back-End/Accenture/target/custom-jacoco-report/index.html

Windows
start "" "%USERPROFILE%\Desktop\Accenture\projeto\Back-End\Accenture\target\custom-jacoco-report\index.html"

obs: Ajustar caminho
```

---

## O que o relatório mostra

### Visão Geral
- Métricas globais: Instruções, Branches, Linhas, Métodos, Classes
- Cards de cobertura com barra colorida (verde / amarelo / vermelho)
- Resumo: quantas classes têm só positivos, só negativos, ambos ou nenhum

### Cobertura por Pacote
- Tabela com status visual por pacote
- Barra de progresso colorida por nível de cobertura:
  - 🟢 Verde ≥ 70% — cobertura adequada
  - 🟡 Amarelo 30–69% — atenção necessária
  - 🔴 Vermelho < 30% — cobertura crítica

### Cobertura por Classe
- Filtro por nome de classe (busca em tempo real)
- Filtro por tipo de teste (Todos / Positivos / Negativos / Ambos / Sem teste)
- Badge de tipo de teste por classe
- Cobertura de instruções e branches por classe

---

## Configuração avançada no pom.xml

```xml
<configuration>
    <!-- Caminho do .exec — altere se usar suíte de testes customizada -->
    <execFile>${project.build.directory}/jacoco.exec</execFile>

    <!-- Pasta de saída do HTML -->
    <reportDir>${project.build.directory}/custom-jacoco-report</reportDir>
</configuration>
```
