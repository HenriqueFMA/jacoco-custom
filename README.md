# JaCoCo Custom Report

Plugin Maven desenvolvido por **Henrique Furtado** para estender os relatórios do JaCoCo, gerando uma visualização HTML que separa testes positivos e negativos, além de apresentar dashboards de cobertura por pacote e por classe.

> O projeto foi criado originalmente para atender às necessidades de outro projeto Back-end e posteriormente disponibilizado como um projeto independente.

---

## Preview

![JaCoCo Custom Report](https://drive.google.com/uc?export=view&id=1kEQ81Sb0U4MYqCPTJ3wwCVE83F7YPFOP)
---

## Funcionalidades

- ✅ Relatório HTML personalizado
- ✅ Separação entre testes positivos e negativos
- ✅ Dashboard de cobertura
- ✅ Cobertura por pacote
- ✅ Cobertura por classe
- ✅ Busca em tempo real
- ✅ Filtros por tipo de teste
- ✅ Barras de progresso coloridas
- ✅ Integração com Maven

---

## Estrutura esperada

| Arquivo | Tipo |
|---------|------|
| `*Tests.java` | ✓ Positivo |
| `*Test.java` | ✓ Positivo |
| `*NegativeTests.java` | ✗ Negativo |
| `*NegativeTest.java` | ✗ Negativo |

---

## Instalação

### 1. Instale o plugin

```bash
mvn clean install
```

### 2. Adicione ao `pom.xml`

```xml
<plugin>
    <groupId>br.com.jacoco</groupId>
    <artifactId>jacoco-custom-report</artifactId>
    <version>1.0.0</version>

    <executions>
        <execution>
            <phase>verify</phase>
            <goals>
                <goal>custom-report</goal>
            </goals>
        </execution>
    </executions>
</plugin>
```

---

## Gerando o relatório

```bash
mvn verify -DskipTests
```

O relatório será criado em

```text
target/custom-jacoco-report/index.html
```

---

## O relatório inclui

### Dashboard

- Cobertura geral
- Instruções
- Branches
- Linhas
- Métodos
- Classes

### Pacotes

- Cobertura por pacote
- Status visual
- Indicadores por cores

### Classes

- Cobertura por classe
- Tipo de teste
- Busca dinâmica
- Filtros

---

## Configuração

```xml
<configuration>
    <execFile>${project.build.directory}/jacoco.exec</execFile>

    <reportDir>${project.build.directory}/custom-jacoco-report</reportDir>
</configuration>
```

---

## Tecnologias

- Java
- Maven
- JaCoCo
- HTML
- CSS
- JavaScript

---

## Autor

Desenvolvido por **Henrique Furtado**.

Originalmente criado para um projeto Back-end e posteriormente disponibilizado como projeto independente.
