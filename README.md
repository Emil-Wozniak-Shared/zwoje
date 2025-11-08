# 🌀 Zwoje

Zwoje is a **multi-template HTML-to-PDF rendering engine** and **IntelliJ IDEA plugin** that helps developers
preview and generate PDFs directly from HTML templates — using multiple templating engines such as 
**Thymeleaf**, **Freemarker**, **Mustache**, **Pebble**, **Groovy**, and **Kotlinx HTML**.

---

## 🧩 Project Overview

Zwoje consists of two main components:

1. **Core Library (`pl.ejdev.zwoje.core`)**
    - Provides a unified interface for compiling and rendering HTML templates across multiple engines.
    - Supports flexible data binding (e.g., JSON context) and PDF generation.
    - Includes integrations for:
        - Thymeleaf
        - Freemarker
        - Mustache
        - Pebble
        - Groovy templates
        - Kotlinx HTML
    - Uses `OpenHtmlToPdf` under the hood for converting rendered HTML into PDF.

2. **IntelliJ Plugin (`ZwojeIJPlugin`)**
    - Integrates Zwoje directly into the IntelliJ IDE.
    - Allows developers to **preview** or **generate PDFs** from templates in real-time.
    - Provides a simple UI to load context data (JSON), render templates, and view PDF output within the IDE.

---

## 💡 What It Helps With

Zwoje is built for developers who:
- Work with **server-side HTML template engines**.
- Need to **preview or test templates** (like invoice generators, reports, emails) without spinning up a full backend.
- Want to **generate PDFs** directly from templates using their preferred engine.
- Develop **multi-template systems** and need a consistent API for different engines.

By integrating the core library and IntelliJ plugin, Zwoje streamlines the workflow of designing, testing, and exporting dynamic templates.

---

## 🛠️ Local Development Setup

### Requirements
- **Java 21 or higher**
- **Gradle**
- **IntelliJ IDEA**

### Steps

1. **Publish the Core Library locally**
    ```bash
       ./gradlew :core:publishToMavenLocal
    ```

2. Run the IntelliJ Plugin
    ```bash
   ./gradlew :ZwojeIJPlugin:runIde
    ```
   This will launch a sandboxed IntelliJ instance with the Zwoje plugin loaded.

3. Test your templates

   - Open a template file (e.g., index.html).
   - Provide context data (e.g., index.json).
   - Click Preview in Zwoje or Generate PDF in the plugin toolbar.

### 📂 Project Structure

```sh
Core
└─pl.ejdev.zwoje.core
  ├── engine/ # html to pdf engines
  ├── template/ # html templates engines
  └── exception/ # library exceptions
ZwojeIJPlugin
└─pl.ejdev.zwoje.core
  ├── actions/ # IntelliJ Actions
  ├── components/ # Intellij Swing components
  ├── notifications/ # Intellij notifications
  └── window/ # Intellij windows
```
Each template engine has its own parser, resolver, and template implementation — all unified under a common interface for easy extensibility.

### 🧾 Example Usage

Template (index.html):

```html
<h1>Invoice</h1>
<p><strong>Invoice Number:</strong> <span th:text="${invoice.number}"></span></p>
<p><strong>Date:</strong> <span th:text="${invoice.date}"></span></p>
<p><strong>Customer:</strong> <span th:text="${invoice.customerName}"></span></p>
```
Context (index.json):
```json
{
  "invoice": {
    "number": "EJDEV-001",
    "date": "10.10.2020",
    "customerName": "Me"
  }
}
```
Preview Output (inside IntelliJ):
Displays rendered HTML and PDF side-by-side — great for template debugging and design validation.

![preview](./assets/zwoje.png)

### 🧱 Architecture Diagram


### 🚀 Future Goals
- Add support for additional template engines.
- Extend IntelliJ plugin with live reload and inline variable inspection.
- Provide Gradle/Maven CLI tools for PDF generation.

### 📜 License
MIT License © 