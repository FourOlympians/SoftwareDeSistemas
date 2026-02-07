# Software de Sistemas – Construcción de un Compilador en Java

## 📌 Descripción
Este repositorio contiene el desarrollo teórico y práctico de los temas abordados en la materia **Software de Sistemas**, con énfasis en el **diseño e implementación de un compilador**.  

A lo largo del proyecto se documentan y desarrollan **todas las fases del proceso de compilación**, desde los pasos previos al compilador hasta, en el mejor de los casos, la construcción de un **compilador funcional escrito en Java**.

El repositorio incluye análisis formales, autómatas, algoritmos, ejemplos prácticos y código fuente, siguiendo una estructura clara y profesional.

---

## 🎯 Objetivos del Proyecto

### Objetivo General
Comprender, diseñar e implementar las etapas fundamentales de un compilador, aplicando conceptos de software de sistemas y teoría de lenguajes formales.

### Objetivos Específicos
- Analizar el proceso completo de traducción de un lenguaje.
- Diseñar autómatas finitos para el análisis léxico.
- Construir analizadores sintácticos basados en gramáticas formales.
- Implementar estructuras de datos para tablas de símbolos.
- Aplicar técnicas de análisis semántico.
- Generar código intermedio y/o código objetivo.
- Desarrollar un compilador modular utilizando **Java**.

---

## 🧠 Contenido del Repositorio

El repositorio está organizado de acuerdo con las **fases clásicas de un compilador**:

### 1️⃣ Introducción a los Compiladores
- Conceptos básicos
- Tipos de traductores (compiladores, intérpretes, ensambladores)
- Arquitectura general de un compilador
- Flujo del proceso de compilación

### 2️⃣ Pasos Previos al Compilador
- Teoría de lenguajes formales
- Alfabetos, cadenas y lenguajes
- Expresiones regulares
- Gramáticas formales (GLC)
- Notación BNF y EBNF

### 3️⃣ Análisis Léxico
- Función del analizador léxico
- Tokens, lexemas y patrones
- Palabras reservadas, identificadores y símbolos
- Manejo de errores léxicos
- **Autómatas Finitos Deterministas (AFD)**
- Conversión de ER → AFN → AFD
- Implementación del lexer en Java

### 4️⃣ Análisis Sintáctico
- Función del analizador sintáctico
- Árboles de derivación y árboles sintácticos
- Gramáticas libres de contexto
- Eliminación de ambigüedad
- Análisis descendente y ascendente
- Parsers LL y LR
- Manejo de errores sintácticos

### 5️⃣ Análisis Semántico
- Verificación de tipos
- Alcances y ámbitos
- Tabla de símbolos
- Reglas semánticas
- Detección de errores semánticos

### 6️⃣ Generación de Código Intermedio
- Representaciones intermedias
- Código de tres direcciones
- Árboles sintácticos anotados
- Optimización básica

### 7️⃣ Generación de Código (Opcional / Avanzado)
- Traducción a código objetivo o pseudocódigo
- Consideraciones de arquitectura
- Optimización de código

### 8️⃣ El Compilador
- Integración de todas las fases
- Diseño modular
- Flujo completo de compilación
- Ejecución y pruebas

---

## 🧩 Tecnologías Utilizadas
- **Lenguaje:** Java  
- **Paradigma:** Programación Orientada a Objetos  
- **Herramientas:**  
  - JDK 8 o superior  
  - IDE (IntelliJ IDEA, Eclipse o NetBeans)  
  - Git y GitHub  

---

## 📂 Estructura del Proyecto

```text
Software-de-Sistemas/
│
├── docs/
│   ├── teoria/
│   ├── automatas/
│   └── gramaticas/
│
├── src/
├── ├── T1_Equipo_Desarrollo_de_Funciones
│   ├── lexico/
│   ├── sintactico/
│   ├── semantico/
│   ├── intermedio/
│   └── compilador/
│
├── ejemplos/
│
├── pruebas/
│
├── README.md
└── LICENSE
