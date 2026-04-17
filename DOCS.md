# Documentación del Compilador - Software de Sistemas

## Índice

1. [Descripción General](#descripción-general)
2. [Arquitectura del Proyecto](#arquitectura-del-proyecto)
3. [Fase 1: Preprocesamiento](#fase-1-preprocesamiento)
4. [Fase 2: Análisis Léxico](#fase-2-análisis-léxico)
5. [Fase 3: Análisis Sintáctico](#fase-3-análisis-sintáctico)
6. [Fase 4: Generación del AST](#fase-4-generación-del-ast)
7. [Ejecución del Compilador](#ejecución-del-compilador)
8. [Estados Pendientes](#estados-pendientes)

---

## Descripción General

Este proyecto implementa un compilador para un lenguaje personalizado inspirado en C, desarrollado en **Java** como parte de la materia Software de Sistemas. El compilador procesa código fuente y lo transforma a través de múltiples fases hasta generar un Árbol Sintáctico Abstracto (AST).

### Características del Lenguaje

- **Tipos de datos**: `entero`, `flotante`, `cadena`, `booleano`, `void`
- **Estructuras de control**: `si` (if)
- **Funciones**: declaración con `main()`
- **Entrada/Salida**: `escribir()`
- **Operadores**: aritméticos, asignación compuesta, comparación

---

## Arquitectura del Proyecto

```mermaid
flowchart TB
    subgraph Entrada["📁 Entrada"]
        A[entrada.c]
    end
    
    subgraph Preproceso["⚙️ Preprocesador"]
        B[AnalizadorSistemas<br/>preprocesarArchivo]
    end
    
    subgraph Lexico["🔤 Análisis Léxico"]
        C[AnalizadorLexico]
        D[AutomataIdentificador]
        E[AutomataNumero]
        F[AutomataCadena]
        G[AutomataSimbolo]
    end
    
    subgraph Sintactico["📊 Análisis Sintáctico"]
        H[AnalizadorSintactico]
        I[NodoAST]
        J[AbstractTree]
    end
    
    subgraph Salida["📤 Salida"]
        K[Tabla de Tokens<br/>LinkedList]
        L[AST]
        M[Tabla de Errores]
    end
    
    A --> B
    B --> C
    C --> D & E & F & G
    D & E & F & G --> K
    K --> H
    H --> I
    I --> J
    J --> L
    D & E & F & G & H --> M
    
    style A fill:#e3f2fd,stroke:#1976d2
    style K fill:#fff3e0,stroke:#f57c00
    style L fill:#e8f5e9,stroke:#388e3c
    style M fill:#ffebee,stroke:#d32f2f
```

### Estructura de Directorios

```
SoftwareDeSistemas/
├── AnalizadorSistemas.java          # Punto de entrada, preprocesador
├── entrada.c                         # Archivo de prueba
├── salida_limpia.txt                 # Archivo preprocesado
├── T1_Equipo_Desarrollo_de_Funciones/
│   └── src/
│       ├── lexico/
│       │   ├── AnalizadorLexico.java
│       │   ├── AutomataIdentificador.java
│       │   ├── AutomataNumero.java
│       │   ├── AutomataCadena.java
│       │   ├── AutomataSimbolo.java
│       │   ├── Token.java
│       │   ├── TipoToken.java
│       │   ├── LinkedList.java
│       │   ├── Nodo.java
│       │   ├── TablaErrores.java
│       │   ├── TipoError.java
│       │   └── ErrorLexico.java
│       └── sintactico/
│           ├── AnalizadorSintactico.java
│           ├── NodoAST.java
│           └── AbstractTree.java
```

---

## Fase 1: Preprocesamiento

### Propósito

Elimina comentarios, normaliza espacios en blanco y prepara el archivo para el análisis léxico.

### Funcionalidades

| Característica | Descripción |
|----------------|-------------|
| Comentarios de línea | Elimina líneas que comienzan con `//` |
| Comentarios de bloque | Elimina contenido entre `/*` y `*/` |
| Normalización | Reduce múltiples espacios/tabs a uno solo |

### Implementación

```mermaid
flowchart TD
    A[Inicio lectura<br/>archivo] --> B{¿Archivo existe?}
    B -->|No| Z[Error]
    B -->|Sí| C{¿Más líneas?}
    C -->|Sí| D{¿Línea contiene<br/>/* ?}
    D -->|Sí| E[Marcar enComentarioBloque]
    E --> F{¿Línea contiene<br/>*/ ?}
    F -->|Sí| G[Desmarcar enComentarioBloque]
    F -->|No| C
    D -->|No| H{¿Línea empieza<br/>con // ?}
    H -->|Sí| C
    H -->|No| I[Normalizar espacios]
    I --> J[¿Línea vacía?]
    J -->|No| K[Escribir línea]
    K --> C
    J -->|Sí| C
    C -->|No| L[Fin]
    
    style Z fill:#ffcdd2,stroke:#d32f2f
    style L fill:#c8e6c9,stroke:#388e3c
```

### Código Principal (`AnalizadorSistemas.java:58-121`)

```java
public static void preprocesarArchivo(String rutaOrigen, String rutaDestino) {
    // 1. Lee línea por línea
    // 2. Detecta y elimina comentarios de bloque /* */
    // 3. Elimina comentarios de línea //
    // 4. Normaliza espacios múltiples
    // 5. Escribe resultado en archivo destino
}
```

---
  if (t.lexema.equals("(")) {
                    next();
                    NodoAST exp = 
## Fase 2: Análisis Léxico

### Propósito

Convierte la secuencia de caracteres en una lista ordenada de **tokens** (unidades léxicas significativas).

### Tokens Reconocidos

```mermaid
flowchart LR
    subgraph Tokens
        T1[IDENTIFICADOR]
        T2[NUMERO_ENTERO]
        T3[NUMERO_DECIMAL]
        T4[CADENA]
        T5[PALABRA_RESERVADA]
        T6[SIMBOLO]
        T7[SIMBOLO_COMPUESTO]
        T8[SIMBOLO_ASSIGN]
        T9[SIMBOLO_COMPARISON]
        T10[OP_ARITMETICO]
    end
```

### Palabras Reservadas

| Palabra | Descripción |
|---------|-------------|
| `entero`, `flotante`, `cadena`, `booleano`, `void` | Tipos de datos |
| `si`, `sino`, `hacer`, `mientras` | Control de flujo |
| `leer`, `escribir` | Entrada/Salida |

### Autómatas Implementados

#### 1. AutomataIdentificador

Reconoce identificadores y palabras reservadas.

```mermaid
stateDiagram-v2
    [*] --> q0
    q0 --> q1: letra [a-zA-Z]
    q1 --> q1: letra o dígito [a-zA-Z0-9]
    q1 --> [*]: otro carácter
    
    note right of q1: Si está en tabla de reservadas → PALABRA_RESERVADA<br/>Si no → IDENTIFICADOR
```

#### 2. AutomataNumero

Reconoce números enteros y decimales.

```mermaid
stateDiagram-v2
    [*] --> q0
    q0 --> q1: dígito [0-9]
    q0 --> [*]: no dígito (rechazo)
    q1 --> q1: dígito [0-9]
    q1 --> q2: punto .
    q2 --> q3: dígito [0-9]
    q3 --> q3: dígito [0-9]
    q2 --> [*]: no dígito (error)
    q1 --> [*]: otro carácter (aceptar entero)
    q3 --> [*]: otro carácter (aceptar decimal)
```

#### 3. AutomataCadena

Reconoce literales de cadena entre comillas.

```mermaid
stateDiagram-v2
    [*] --> q0
    q0 --> q1: comilla "
    q1 --> q1: cualquier carácter menos "
    q1 --> q2: comilla "
    q1 --> [*]: fin de línea (error: cadena sin cerrar)
    q2 --> [*]: cadena reconocida
```

#### 4. AutomataSimbolo

Reconoce símbolos simples y compuestos.

```mermaid
flowchart TD
    A[Inicio] --> B{¿2 caracteres<br/>disponibles?}
    B -->|Sí| C{¿Es símbolo<br/>compuesto?}
    C -->|Sí| D[SIMBOLO_COMPUESTO<br/>++ -- \|\| && ->]
    C -->|No| E{¿Es operador<br/>comparación?}
    E -->|Sí| F[SIMBOLO_COMPARISON<br/>>= <= == !=]
    E -->|No| G{¿Es asignación<br/>compuesta?}
    G -->|Sí| H[SIMBOLO_ASSIGN<br/>+= -= *= /=]
    G -->|No| I[Operador simple]
    B -->|No| I
    I --> J{¿Aritmético?}
    J -->|Sí| K[OP_ARITMETICO<br/>+ - * /]
    J -->|No| L{¿Asignación?}
    L -->|Sí| M[SIMBOLO_ASSIGN<br/>=]
    L -->|No| N{¿Otro símbolo?}
    N -->|Sí| O[SIMBOLO<br/>( ) { } ; , &]
    N -->|No| P[No reconocido]
    
    style D fill:#e3f2fd
    style F fill:#fff3e0
    style K fill:#e8f5e9
    style O fill:#fce4ec
    style P fill:#ffcdd2
```

### Flujo del Análisis Léxico

```mermaid
sequenceDiagram
    participant AL as AnalizadorLexico
    participant AI as AutomataIdentificador
    participant AN as AutomataNumero
    participant AC as AutomataCadena
    participant AS as AutomataSimbolo
    participant LL as LinkedList
    
    AL->>AL: Leer línea por línea
    loop Para cada carácter
        AL->>AC: ¿Es cadena "?
        alt Es cadena
            AC-->>AL: Token CADENA
            AL->>LL: agregarNodoFinal
        end
        
        alt No es cadena
            AL->>AN: ¿Es número?
            alt Es número
                AN-->>AL: Token NUMERO
                AL->>LL: agregarNodoFinal
            else No es número
                AL->>AI: ¿Es letra?
                alt Es identificador
                    AI-->>AL: Token IDENTIFICADOR o RESERVADA
                    AL->>LL: agregarNodoFinal
                else No es letra
                    AL->>AS: ¿Es símbolo?
                    alt Es símbolo
                        AS-->>AL: Token SÍMBOLO
                        AL->>LL: agregarNodoFinal
                    else No reconocido
                        AL->>TablaErrores: Registrar error
                    end
                end
            end
        end
    end
```

### Manejo de Errores Léxicos

```mermaid
flowchart TD
    subgraph Tipos de Error
        E1[CARACTER_NO_RECONOCIDO]
        E2[CADENA_SIN_CERRAR]
        E3[NUMERO_MAL_FORMADO]
    end
    
    E1 --> R1[Registrar en TablaErrores<br/>Continuar análisis]
    E2 --> R2[Registrar error<br/>Saltar resto de línea]
    E3 --> R3[Registrar error<br/>Consumir caracteres válidos]
```

### Estructuras de Datos del Léxico

```mermaid
classDiagram
    class Token {
        +TipoToken tipo
        +String lexema
        +String value
        +int fila
        +int columna
    }
    
    class Nodo {
        +Token data
        +Nodo prev
        +Nodo next
    }
    
    class LinkedList {
        -Nodo head
        -Nodo tail
        +int count
        +agregarNodoFinal(Token)
        +obtenerHead() Nodo
    }
    
    class ErrorLexico {
        +TipoError tipo
        +String lexema
        +int fila
        +int columna
        +String descripcion
    }
    
    class TablaErrores {
        -ErrorLexico[] errores
        +agregar(TipoError, String, int, int, String)
        +imprimir()
    }
    
    Nodo --> Token : data
    LinkedList --> Nodo
    TablaErrores --> ErrorLexico
```

---

## Fase 3: Análisis Sintáctico

### Propósito

Verifica que la secuencia de tokens forme oraciones válidas según la gramática del lenguaje y construye el Árbol Sintáctico Abstracto (AST).

### Tipo de Parser

**Parser Descendente Recursivo** (top-down, sin backtracking en la mayoría de casos).

### Gramática del Lenguaje (BNF Simplificado)

```mermaid
flowchart TB
    subgraph Gramática
        P[PROGRAMA] --> D1[DECLARACION]
        D1 --> D2[DECLARACION]
        D1 --> ε
        
        D2 --> DV[declaracionVariable]
        D2 --> AS[asignacion]
        D2 --> ES[escribir]
        D2 --> SI[si]
        
        DV --> T[tipo]
        T -->|"entero"| TE[ENTERO]
        T -->|"flotante"| TF[FLOTANTE]
        T -->|"cadena"| TC[CADENA]
        T -->|"booleano"| TB[BOOLEANO]
        T -->|"void"| TV[VOID]
        
        DV --> ID[identificador]
        DV -->|"main ("| PAR[parametros]
        PAR -->|")"| B[bloque]
        
        B -->|"{"| S1[SENTENCIA]
        B -->|"}"| ε
        S1 --> S2[SENTENCIA]
        S1 --> ε
        
        SI -->|"si ("| C[condicion]
        SI -->|")"| B2[bloque]
        
        ES -->|"escribir ("| ARG[argumentos]
        ES -->|")"| ;[;]
        
        ARG --> E[expresion]
        ARG -->|","| ARG2[argumentos]
        ARG --> ε
        
        E --> T1[termino]
        E -->|"+"| T2[termino]
        E -->|"-"| T3[termino]
    end
```

### Reglas de Producción Implementadas

```java
programa        → declaracion*
declaracion     → declaracionVariable | asignacion | escribir | si
declaracionVariable → tipo identificador ("=" expresion)? ";" 
                    | tipo "main" "(" ")" bloque
asignacion      → identificador ("=" | "+=" | "-=" | "*=" | "/=") expresion ";"
escribir        → "escribir" "(" expresion ("," expresion)* ")" ";"
si              → "si" "(" expresion ")" bloque
bloque          → "{" declaracion* "}"
expresion       → termino (opAritmetico | opComparacion)* | expresion "&&" expresion | expresion "||" expresion
termino         → factor
factor          → NUMERO | IDENTIFICADOR | CADENA | "(" expresion ")" | "&" identificador
tipo            → "entero" | "flotante" | "cadena" | "booleano" | "void"
```

### Estructura del Parser

```mermaid
flowchart TD
    subgraph AnalizadorSintactico
        A[analizar] --> P[programa]
        P --> D1[declaracion]
        P --> P
        
        D1 --> DV[declaracionVariable]
        D1 --> AS[asignacion]
        D1 --> ES[escribir]
        D1 --> SI[si]
        
        DV --> T[tipo]
        DV --> FM[funcMain]
        DV --> ID
        
        FM --> B[bloque]
        B --> D2[declaracion]
        
        ES --> E[expresion]
        SI --> E
        
        E --> T[termino]
        T --> F[factor]
    end
    
    subgraph Gestión de Tokens
        PEEK[peek] --> |"ver siguiente"| TOK[Token]
        NEXT[next] --> |"consumir"| TOK
    end
```

### Manejo de Errores Sintácticos

```mermaid
flowchart LR
    subgraph Detección
        E1[Token inesperado]
        E2[Token faltante]
        E3[Expresión mal formada]
    end
    
    subgraph Recuperación
        E1 --> R1[Reportar error<br/>Avanzar al siguiente token]
        E2 --> R2[Insertar token esperado<br/>o usar nodo de error]
        E3 --> R3[Crear nodo ERROR_EXPR<br/>Continuar análisis]
    end
    
    R1 --> T[Tabla de Errores]
    R2 --> T
    R3 --> T
```

---

## Fase 4: Generación del AST

### Propósito

Construir una representación jerárquica del programa que captura la estructura sintáctica.

### Estructura del Nodo AST

```mermaid
classDiagram
    class NodoAST {
        +String tipo
        +String valor
        +Token token
        +NodoAST hijo
        +NodoAST sig
        +preOrder()
        +inOrder()
        +postOrder()
        +printTree()
    }
    
    class AbstractTree {
        +NodoAST raiz
        +preOrder()
        +inOrder()
        +postOrder()
        +printTree()
    }
    
    AbstractTree --> NodoAST : contiene
    
    note for NodoAST "hijo = primer hijo\nsig = hermano siguiente"
```

### Tipos de Nodos del AST

| Tipo de Nodo | Descripción | Hijos |
|--------------|-------------|-------|
| `PROGRAMA` | Raíz del árbol | Declaraciones |
| `FUNCION_MAIN` | Función principal | Bloque |
| `DECLARACION_*` | Declaración de variable | Identificador, valor |
| `ASIGNACION` | Asignación simple o compuesta | Identificador, valor |
| `ESCRIBIR` | Llamada a función escribir | Argumentos |
| `SI` | Estructura condicional | Condición, bloque |
| `BLOQUE` | Bloque de código | Sentencias |
| `OP_*` | Operadores | Operando izquierdo, derecho |
| `ID` | Identificador | - |
| `NUMERO` | Literal numérico | - |
| `CADENA` | Literal de cadena | - |
| `REF` | Referencia (&) | - |

### Ejemplo: AST del archivo `entrada.c`

```
entrada.c:
---
entero main() {
    flotante x = 5.67;
    x += 1;
    cadena nombre = "Juan Carlos Bodoque";
    si (x > 10) {
        escribir("%d es mayor a 5", &x);
    }
}
```

```mermaid
graph TD
    ROOT["PROGRAMA"]
    
    ROOT --> MAIN["FUNCION_MAIN"]
    
    MAIN --> BLOCK1["BLOQUE"]
    
    BLOCK1 --> DEC1["DECLARACION_FLOTANTE"]
    DEC1 --> ID_X1["ID: x"]
    ID_X1 --> NUM["NUMERO: 5.67"]
    
    BLOCK1 --> ASIGN1["ASIGNACION"]
    ASIGN1 --> ID_X2["ID: x"]
    ASIGN1 --> OP_PLUS["OP_+"]
    OP_PLUS --> ID_X3["ID: x"]
    OP_PLUS --> NUM1["NUMERO: 1"]
    
    BLOCK1 --> DEC2["DECLARACION_CADENA"]
    DEC2 --> ID_NOMBRE["ID: nombre"]
    ID_NOMBRE --> CADENA["CADENA: \"Juan Carlos Bodoque\""]
    
    BLOCK1 --> SI1["SI"]
    SI1 --> OP_GT["OP_>"]
    OP_GT --> ID_X4["ID: x"]
    OP_GT --> NUM10["NUMERO: 10"]
    
    SI1 --> BLOCK2["BLOQUE"]
    
    BLOCK2 --> ESCRIBIR["ESCRIBIR"]
    ESCRIBIR --> ARGS["ARGUMENTOS"]
    ARGS --> CAD1["CADENA: \"%d es mayor a 5\""]
    ARGS --> REF["REF: x"]
    
    style ROOT fill:#ff9800,color:#fff,stroke:#f57c00
    style MAIN fill:#2196f3,color:#fff,stroke:#1976d2
    style BLOCK1 fill:#e3f2fd,stroke:#1976d2
    style BLOCK2 fill:#e3f2fd,stroke:#1976d2
    style DEC1 fill:#4caf50,color:#fff,stroke:#388e3c
    style DEC2 fill:#4caf50,color:#fff,stroke:#388e3c
    style ASIGN1 fill:#ff9800,color:#fff,stroke:#f57c00
    style SI1 fill:#9c27b0,color:#fff,stroke:#7b1fa2
    style ESCRIBIR fill:#00bcd4,color:#fff,stroke:#00838f
```

### Recorridos del AST

```mermaid
flowchart LR
    subgraph Recorridos
        PRE["preOrder()<br/>Padre → Hijo → Hermano"]
        IN["inOrder()<br/>Hijo → Padre → Hermano"]
        POST["postOrder()<br/>Hijo → Hermano → Padre"]
    end
    
    PRE -->|"Usado para"| G1[Generación de código]
    IN -->|"Usado para"| G2[Evaluación de expresiones]
    POST -->|"Usado para"| G3[Optimización]
```

---

## Ejecución del Compilador

### Compilación y Ejecución

```bash
cd T1_Equipo_Desarrollo_de_Funciones

# Compilar
javac -d bin src/AnalizadorSistemas.java src/lexico/*.java src/sintactico/*.java

# Ejecutar
java -cp bin AnalizadorSistemas
```

### Salida Esperada

```
Archivo preprocesado con éxito.

===== TABLA DE SIMBOLOS =====
PALABRA_RESERVADA -> entero (1,1)
IDENTIFICADOR -> main (1,8)
SIMBOLO -> ( (1,12)
SIMBOLO -> ) (1,13)
...
Total de tokens: 35

===== AST =====
PROGRAMA
└── FUNCION_MAIN
    └── BLOQUE
        └── DECLARACION_FLOTANTE
            └── ID: x
                └── NUMERO: 5.67
        └── ASIGNACION
            └── ID: x
                └── OP_+
                    └── ID: x
                        └── NUMERO: 1
        └── DECLARACION_CADENA
            └── ID: nombre
                └── CADENA: "Juan Carlos Bodoque"
        └── SI
            └── OP_>
                └── ID: x
                    └── NUMERO: 10
            └── BLOQUE
                └── ESCRIBIR
                    └── ARGUMENTOS
                        └── CADENA: "%d es mayor a 5"
                            └── REF
                                └── ID: x

========== TABLA DE ERRORES LEXICOS ==========
  No se encontraron errores lexicos.
==============================================
```

---

## Estados Pendientes

### Fases por Implementar

```mermaid
flowchart LR
    subgraph Implementado
        A["✅ Preprocesamiento"]
        B["✅ Análisis Léxico"]
        C["✅ Análisis Sintáctico"]
        D["✅ Generación AST"]
    end
    
    subgraph Pendiente
        E["📋 Análisis Semántico"]
        F["📋 Código Intermedio"]
        G["📋 Optimización"]
        H["📋 Generación de Código"]
    end
    
    style A fill:#c8e6c9,stroke:#4caf50
    style B fill:#c8e6c9,stroke:#4caf50
    style C fill:#c8e6c9,stroke:#4caf50
    style D fill:#c8e6c9,stroke:#4caf50
    style E fill:#fff9c4,stroke:#fbc02d
    style F fill:#fff9c4,stroke:#fbc02d
    style G fill:#fff9c4,stroke:#fbc02d
    style H fill:#fff9c4,stroke:#fbc02d
```

### Análisis Semántico (Por implementar)

| Componente | Descripción |
|------------|-------------|
| **Tabla de símbolos** | Almacenar variables, tipos, alcances |
| **Verificación de tipos** | Comprobar compatibilidad de operandos |
| **Control de alcances** | Verificar variables definidas |
| **Verificación de funciones** | Parámetros y retorno correctos |

### Código Intermedio (Por implementar)

| Representación | Descripción |
|----------------|-------------|
| **Código de tres direcciones** | Expresiones binarias en forma `x = y op z` |
| **Notación postfija** | Para evaluación de expresiones |
| **Representación SSA** | Forma estática de una sola asignación |

### Generación de Código (Por implementar)

- Generación de código ensamblador o bytecode
- Máquina virtual o código objeto
- Optimizaciones específicas de arquitectura

---

## Anexo: Referencia de Clases

### Módulo Léxico

| Clase | Responsabilidad |
|-------|-----------------|
| `AnalizadorLexico` | Coordina el análisis, lee archivo línea por línea |
| `AutomataIdentificador` | Reconoce identificadores y palabras reservadas |
| `AutomataNumero` | Reconoce números enteros y decimales |
| `AutomataCadena` | Reconoce literales de cadena |
| `AutomataSimbolo` | Reconoce operadores y símbolos |
| `Token` | Representa una unidad léxica |
| `TipoToken` | Enum con tipos de tokens |
| `LinkedList` | Lista enlazada para almacenar tokens |
| `TablaErrores` | Colección de errores léxicos |
| `TipoError` | Enum con tipos de errores |

### Módulo Sintáctico

| Clase | Responsabilidad |
|-------|-----------------|
| `AnalizadorSintactico` | Parser descendente recursivo |
| `NodoAST` | Nodo del árbol sintáctico |
| `AbstractTree` | Contenedor del AST completo |

---

*Documentación generada para el proyecto de Software de Sistemas*
