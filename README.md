# 📱 Processed Foods

## 📌 Acerca de la App

**Processed Foods** es una aplicación Android diseñada para **monitorear y controlar un basurero inteligente** enfocado en empaques de alimentos procesados.  

La app genera **reportes detallados** sobre los empaques descartados que contienen octágonos y permite al usuario controlar el basurero inteligente (encender, apagar y reiniciar).  

Además, incluye **registro e inicio de sesión de usuarios**, integrando varios servicios de **Firebase**.  

---

## ✨ Funcionalidades

- 📊 **Reportes de Empaques Descartados**: Visualiza reportes de empaques de alimentos procesados con octágonos descartados en el mes.  
- 🍽️ **Reportes de Información Nutricional**: Consulta información detallada (azúcar, grasas trans, grasas saturadas, sodio en gramos) de los empaques descartados.  
- 🏷️ **Reportes por Marca**: Visualiza los empaques procesados descartados, organizados por marca.  
- 🗑️ **Control del Basurero Inteligente**: Enciende, apaga o reinicia el basurero desde la app.  
- 🔐 **Autenticación de Usuarios**: Inicia sesión con correo electrónico o cuenta de Google.  
- 📝 **Registro de Usuarios**: Registra nuevos usuarios con correo electrónico.  

---

## 🏗️ Arquitectura

La app sigue las [guías oficiales de arquitectura de Android](https://developer.android.com/topic/architecture), utilizando los siguientes componentes:  

- 🧩 **MVVM (Model-View-ViewModel)** → Separación clara entre lógica de negocio y lógica de interfaz.  
- 🎛️ **ViewModel** → Manejo de datos relacionados con la UI de forma segura con el ciclo de vida.  
- 📡 **LiveData** → Clases observables que actualizan la UI en tiempo real.  
- 🔄 **Kotlin Flows** → Manejo eficiente y reactivo de flujos de datos asincrónicos.  
- 📚 **Repository Pattern** → Gestión de operaciones de datos con una API limpia hacia el ViewModel.  
- ☁️ **Servicios Firebase** → Autenticación, Firestore Database y Storage.  

---

## 🎨 Interfaz de Usuario (UI)

La interfaz de **Processed Foods** está construida con **Material 3** y vistas personalizadas, garantizando un diseño **moderno, responsivo y amigable**.  

Elementos principales:  
- 🎨 **Componentes Material 3** → Para una experiencia visual consistente y adaptable.  
- 🖌️ **Vistas Personalizadas** → Diseñadas para ajustarse a las necesidades específicas de la app.  

---

## 🖼️ Capturas de Pantalla

![Reporte Mensual de Empaques](screenshots/monthly_discarded_packages_report.jpg)  
*Reporte mensual de empaques con octágonos descartados*  

![Reporte Nutricional](screenshots/nutritional_information_report.jpg)  
*Reporte nutricional de empaques descartados*  

![Reporte por Marca](screenshots/brand_specific_report.jpg)  
*Reporte de empaques descartados por marca*  

![Control del Basurero Inteligente](screenshots/smart_trash_can_control.jpg)  
*Opciones de control del basurero inteligente*  

![Opciones de Inicio de Sesión](screenshots/login_options.jpg)  
*Pantalla de opciones de inicio de sesión*  

![Inicio de Sesión con Correo](screenshots/email_login.jpg)  
*Ingreso con correo electrónico*  

![Inicio de Sesión con Google](screenshots/google_login.jpg)  
*Ingreso con cuenta de Google*  

![Registro con Correo](screenshots/email_registration.jpg)  
*Registro de usuario con correo electrónico*  

---

📌 Proyecto desarrollado en **Kotlin** bajo buenas prácticas de arquitectura y diseño moderno de interfaces.  
